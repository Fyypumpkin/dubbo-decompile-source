/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import com.alibaba.com.caucho.hessian.io.AbstractMapDeserializer;
import com.alibaba.com.caucho.hessian.io.HessianFieldException;
import com.alibaba.com.caucho.hessian.io.HessianProtocolException;
import com.alibaba.com.caucho.hessian.io.IOExceptionWrapper;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JavaDeserializer
extends AbstractMapDeserializer {
    private static final Logger log = Logger.getLogger(JavaDeserializer.class.getName());
    private Class _type;
    private HashMap _fieldMap;
    private Method _readResolve;
    private Constructor _constructor;
    private Object[] _constructorArgs;
    static final Map<String, Boolean> PRIMITIVE_TYPE = new HashMap<String, Boolean>(){
        {
            this.put(Boolean.class.getName(), true);
            this.put(Character.class.getName(), true);
            this.put(Byte.class.getName(), true);
            this.put(Short.class.getName(), true);
            this.put(Integer.class.getName(), true);
            this.put(Long.class.getName(), true);
            this.put(Float.class.getName(), true);
            this.put(Double.class.getName(), true);
            this.put(Void.class.getName(), true);
        }
    };

    public JavaDeserializer(Class cl) {
        this._type = cl;
        this._fieldMap = this.getFieldMap(cl);
        this._readResolve = this.getReadResolve(cl);
        if (this._readResolve != null) {
            this._readResolve.setAccessible(true);
        }
        Constructor<?>[] constructors = cl.getDeclaredConstructors();
        long bestCost = Long.MAX_VALUE;
        for (int i = 0; i < constructors.length; ++i) {
            Class<?>[] param = constructors[i].getParameterTypes();
            long cost = 0L;
            for (int j = 0; j < param.length; ++j) {
                cost = 4L * cost;
                if (Object.class.equals(param[j])) {
                    ++cost;
                    continue;
                }
                if (String.class.equals(param[j])) {
                    cost += 2L;
                    continue;
                }
                if (Integer.TYPE.equals(param[j])) {
                    cost += 3L;
                    continue;
                }
                if (Long.TYPE.equals(param[j])) {
                    cost += 4L;
                    continue;
                }
                if (param[j].isPrimitive()) {
                    cost += 5L;
                    continue;
                }
                cost += 6L;
            }
            if (cost < 0L || cost > 65536L) {
                cost = 65536L;
            }
            if ((cost += (long)param.length << 48) >= bestCost) continue;
            this._constructor = constructors[i];
            bestCost = cost;
        }
        if (this._constructor != null) {
            this._constructor.setAccessible(true);
            Class<?>[] params = this._constructor.getParameterTypes();
            this._constructorArgs = new Object[params.length];
            for (int i = 0; i < params.length; ++i) {
                this._constructorArgs[i] = JavaDeserializer.getParamArg(params[i]);
            }
        }
    }

    @Override
    public Class getType() {
        return this._type;
    }

    @Override
    public Object readMap(AbstractHessianInput in) throws IOException {
        try {
            Object obj = this.instantiate();
            return this.readMap(in, obj);
        }
        catch (IOException e) {
            throw e;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOExceptionWrapper(this._type.getName() + ":" + e.getMessage(), e);
        }
    }

    @Override
    public Object readObject(AbstractHessianInput in, String[] fieldNames) throws IOException {
        try {
            Object obj = this.instantiate();
            return this.readObject(in, obj, fieldNames);
        }
        catch (IOException e) {
            throw e;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOExceptionWrapper(this._type.getName() + ":" + e.getMessage(), e);
        }
    }

    protected Method getReadResolve(Class cl) {
        while (cl != null) {
            Method[] methods = cl.getDeclaredMethods();
            for (int i = 0; i < methods.length; ++i) {
                Method method = methods[i];
                if (!method.getName().equals("readResolve") || method.getParameterTypes().length != 0) continue;
                return method;
            }
            cl = cl.getSuperclass();
        }
        return null;
    }

    public Object readMap(AbstractHessianInput in, Object obj) throws IOException {
        try {
            int ref = in.addRef(obj);
            while (!in.isEnd()) {
                Object key = in.readObject();
                FieldDeserializer deser = (FieldDeserializer)this._fieldMap.get(key);
                if (deser != null) {
                    deser.deserialize(in, obj);
                    continue;
                }
                in.readObject();
            }
            in.readMapEnd();
            Object resolve = this.resolve(obj);
            if (obj != resolve) {
                in.setRef(ref, resolve);
            }
            return resolve;
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }

    public Object readObject(AbstractHessianInput in, Object obj, String[] fieldNames) throws IOException {
        try {
            int ref = in.addRef(obj);
            for (int i = 0; i < fieldNames.length; ++i) {
                String name = fieldNames[i];
                FieldDeserializer deser = (FieldDeserializer)this._fieldMap.get(name);
                if (deser != null) {
                    deser.deserialize(in, obj);
                    continue;
                }
                in.readObject();
            }
            Object resolve = this.resolve(obj);
            if (obj != resolve) {
                in.setRef(ref, resolve);
            }
            return resolve;
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOExceptionWrapper(obj.getClass().getName() + ":" + e, e);
        }
    }

    private Object resolve(Object obj) throws Exception {
        block3 : {
            try {
                if (this._readResolve != null) {
                    return this._readResolve.invoke(obj, new Object[0]);
                }
            }
            catch (InvocationTargetException e) {
                if (e.getTargetException() == null) break block3;
                throw e;
            }
        }
        return obj;
    }

    protected Object instantiate() throws Exception {
        try {
            if (this._constructor != null) {
                return this._constructor.newInstance(this._constructorArgs);
            }
            return this._type.newInstance();
        }
        catch (Exception e) {
            throw new HessianProtocolException("'" + this._type.getName() + "' could not be instantiated", e);
        }
    }

    protected HashMap getFieldMap(Class cl) {
        HashMap<String, StringFieldDeserializer> fieldMap = new HashMap<String, StringFieldDeserializer>();
        while (cl != null) {
            Field[] fields = cl.getDeclaredFields();
            for (int i = 0; i < fields.length; ++i) {
                Field field = fields[i];
                if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()) || fieldMap.get(field.getName()) != null) continue;
                try {
                    field.setAccessible(true);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
                Class<?> type = field.getType();
                FieldDeserializer deser = String.class.equals(type) ? new StringFieldDeserializer(field) : (Byte.TYPE.equals(type) ? new ByteFieldDeserializer(field) : (Short.TYPE.equals(type) ? new ShortFieldDeserializer(field) : (Integer.TYPE.equals(type) ? new IntFieldDeserializer(field) : (Long.TYPE.equals(type) ? new LongFieldDeserializer(field) : (Float.TYPE.equals(type) ? new FloatFieldDeserializer(field) : (Double.TYPE.equals(type) ? new DoubleFieldDeserializer(field) : (Boolean.TYPE.equals(type) ? new BooleanFieldDeserializer(field) : (Date.class.equals(type) ? new SqlDateFieldDeserializer(field) : (Timestamp.class.equals(type) ? new SqlTimestampFieldDeserializer(field) : (Time.class.equals(type) ? new SqlTimeFieldDeserializer(field) : (Map.class.equals(type) && field.getGenericType() != field.getType() ? new ObjectMapFieldDeserializer(field) : (List.class.equals(type) && field.getGenericType() != field.getType() ? new ObjectListFieldDeserializer(field) : new ObjectFieldDeserializer(field)))))))))))));
                fieldMap.put(field.getName(), (StringFieldDeserializer)deser);
            }
            cl = cl.getSuperclass();
        }
        return fieldMap;
    }

    protected static Object getParamArg(Class cl) {
        if (!cl.isPrimitive()) {
            return null;
        }
        if (Boolean.TYPE.equals((Object)cl)) {
            return Boolean.FALSE;
        }
        if (Byte.TYPE.equals((Object)cl)) {
            return new Byte(0);
        }
        if (Short.TYPE.equals((Object)cl)) {
            return new Short(0);
        }
        if (Character.TYPE.equals((Object)cl)) {
            return new Character('\u0000');
        }
        if (Integer.TYPE.equals((Object)cl)) {
            return 0;
        }
        if (Long.TYPE.equals((Object)cl)) {
            return 0L;
        }
        if (Float.TYPE.equals((Object)cl)) {
            return Float.valueOf(0.0f);
        }
        if (Double.TYPE.equals((Object)cl)) {
            return 0.0;
        }
        throw new UnsupportedOperationException();
    }

    static void logDeserializeError(Field field, Object obj, Object value, Throwable e) throws IOException {
        String fieldName = field.getDeclaringClass().getName() + "." + field.getName();
        if (e instanceof HessianFieldException) {
            throw (HessianFieldException)e;
        }
        if (e instanceof IOException) {
            throw new HessianFieldException(fieldName + ": " + e.getMessage(), e);
        }
        if (value != null) {
            throw new HessianFieldException(fieldName + ": " + value.getClass().getName() + " (" + value + ") cannot be assigned to '" + field.getType().getName() + "'", e);
        }
        throw new HessianFieldException(fieldName + ": " + field.getType().getName() + " cannot be assigned from null", e);
    }

    private static boolean isPrimitive(Type type) {
        try {
            if (type != null && type instanceof Class) {
                Class clazz = (Class)type;
                return clazz.isPrimitive() || PRIMITIVE_TYPE.containsKey(clazz.getName());
            }
        }
        catch (Exception clazz) {
            // empty catch block
        }
        return false;
    }

    static class SqlTimeFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        SqlTimeFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            Time value = null;
            try {
                java.util.Date date = (java.util.Date)in.readObject();
                value = new Time(date.getTime());
                this._field.set(obj, value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class SqlTimestampFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        SqlTimestampFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            Timestamp value = null;
            try {
                java.util.Date date = (java.util.Date)in.readObject();
                if (date != null) {
                    value = new Timestamp(date.getTime());
                }
                this._field.set(obj, value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class SqlDateFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        SqlDateFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            Date value = null;
            try {
                java.util.Date date = (java.util.Date)in.readObject();
                value = new Date(date.getTime());
                this._field.set(obj, value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class StringFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        StringFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            String value = null;
            try {
                value = in.readString();
                this._field.set(obj, value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class DoubleFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        DoubleFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            double value = 0.0;
            try {
                value = in.readDouble();
                this._field.setDouble(obj, value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class FloatFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        FloatFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            double value = 0.0;
            try {
                value = in.readDouble();
                this._field.setFloat(obj, (float)value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class LongFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        LongFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            long value = 0L;
            try {
                value = in.readLong();
                this._field.setLong(obj, value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class IntFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        IntFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            int value = 0;
            try {
                value = in.readInt();
                this._field.setInt(obj, value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class ObjectListFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        ObjectListFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            Object value = null;
            try {
                Type[] types = ((ParameterizedType)this._field.getGenericType()).getActualTypeArguments();
                value = in.readObject(this._field.getType(), JavaDeserializer.isPrimitive(types[0]) ? (Class)types[0] : null);
                this._field.set(obj, value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class ObjectMapFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        ObjectMapFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            Object value = null;
            try {
                Type[] types = ((ParameterizedType)this._field.getGenericType()).getActualTypeArguments();
                value = in.readObject(this._field.getType(), JavaDeserializer.isPrimitive(types[0]) ? (Class)types[0] : null, JavaDeserializer.isPrimitive(types[1]) ? (Class)types[1] : null);
                this._field.set(obj, value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class ShortFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        ShortFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            int value = 0;
            try {
                value = in.readInt();
                this._field.setShort(obj, (short)value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class ByteFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        ByteFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            int value = 0;
            try {
                value = in.readInt();
                this._field.setByte(obj, (byte)value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class BooleanFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        BooleanFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            boolean value = false;
            try {
                value = in.readBoolean();
                this._field.setBoolean(obj, value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static class ObjectFieldDeserializer
    extends FieldDeserializer {
        private final Field _field;

        ObjectFieldDeserializer(Field field) {
            this._field = field;
        }

        @Override
        void deserialize(AbstractHessianInput in, Object obj) throws IOException {
            Object value = null;
            try {
                value = in.readObject(this._field.getType());
                this._field.set(obj, value);
            }
            catch (Exception e) {
                JavaDeserializer.logDeserializeError(this._field, obj, value, e);
            }
        }
    }

    static abstract class FieldDeserializer {
        FieldDeserializer() {
        }

        abstract void deserialize(AbstractHessianInput var1, Object var2) throws IOException;
    }

}

