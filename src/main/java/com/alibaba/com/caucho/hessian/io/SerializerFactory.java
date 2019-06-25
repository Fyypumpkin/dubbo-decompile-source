/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import com.alibaba.com.caucho.hessian.io.AbstractSerializerFactory;
import com.alibaba.com.caucho.hessian.io.ArrayDeserializer;
import com.alibaba.com.caucho.hessian.io.ArraySerializer;
import com.alibaba.com.caucho.hessian.io.BasicDeserializer;
import com.alibaba.com.caucho.hessian.io.BasicSerializer;
import com.alibaba.com.caucho.hessian.io.BigIntegerDeserializer;
import com.alibaba.com.caucho.hessian.io.CalendarSerializer;
import com.alibaba.com.caucho.hessian.io.ClassDeserializer;
import com.alibaba.com.caucho.hessian.io.ClassSerializer;
import com.alibaba.com.caucho.hessian.io.CollectionDeserializer;
import com.alibaba.com.caucho.hessian.io.CollectionSerializer;
import com.alibaba.com.caucho.hessian.io.Deserializer;
import com.alibaba.com.caucho.hessian.io.EnumDeserializer;
import com.alibaba.com.caucho.hessian.io.EnumSerializer;
import com.alibaba.com.caucho.hessian.io.EnumerationDeserializer;
import com.alibaba.com.caucho.hessian.io.EnumerationSerializer;
import com.alibaba.com.caucho.hessian.io.HessianHandle;
import com.alibaba.com.caucho.hessian.io.HessianProtocolException;
import com.alibaba.com.caucho.hessian.io.HessianRemoteObject;
import com.alibaba.com.caucho.hessian.io.InputStreamDeserializer;
import com.alibaba.com.caucho.hessian.io.InputStreamSerializer;
import com.alibaba.com.caucho.hessian.io.IteratorSerializer;
import com.alibaba.com.caucho.hessian.io.JavaDeserializer;
import com.alibaba.com.caucho.hessian.io.JavaSerializer;
import com.alibaba.com.caucho.hessian.io.LocaleSerializer;
import com.alibaba.com.caucho.hessian.io.MapDeserializer;
import com.alibaba.com.caucho.hessian.io.MapSerializer;
import com.alibaba.com.caucho.hessian.io.ObjectDeserializer;
import com.alibaba.com.caucho.hessian.io.RemoteSerializer;
import com.alibaba.com.caucho.hessian.io.Serializer;
import com.alibaba.com.caucho.hessian.io.SqlDateDeserializer;
import com.alibaba.com.caucho.hessian.io.SqlDateSerializer;
import com.alibaba.com.caucho.hessian.io.StackTraceElementDeserializer;
import com.alibaba.com.caucho.hessian.io.StringValueDeserializer;
import com.alibaba.com.caucho.hessian.io.StringValueSerializer;
import com.alibaba.com.caucho.hessian.io.ThrowableSerializer;
import com.alibaba.com.caucho.hessian.io.java8.DurationHandle;
import com.alibaba.com.caucho.hessian.io.java8.InstantHandle;
import com.alibaba.com.caucho.hessian.io.java8.Java8TimeSerializer;
import com.alibaba.com.caucho.hessian.io.java8.LocalDateHandle;
import com.alibaba.com.caucho.hessian.io.java8.LocalDateTimeHandle;
import com.alibaba.com.caucho.hessian.io.java8.LocalTimeHandle;
import com.alibaba.com.caucho.hessian.io.java8.MonthDayHandle;
import com.alibaba.com.caucho.hessian.io.java8.OffsetDateTimeHandle;
import com.alibaba.com.caucho.hessian.io.java8.OffsetTimeHandle;
import com.alibaba.com.caucho.hessian.io.java8.PeriodHandle;
import com.alibaba.com.caucho.hessian.io.java8.YearHandle;
import com.alibaba.com.caucho.hessian.io.java8.YearMonthHandle;
import com.alibaba.com.caucho.hessian.io.java8.ZoneIdSerializer;
import com.alibaba.com.caucho.hessian.io.java8.ZoneOffsetHandle;
import com.alibaba.com.caucho.hessian.io.java8.ZonedDateTimeHandle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.ObjectName;

public class SerializerFactory
extends AbstractSerializerFactory {
    private static final Logger log = Logger.getLogger(SerializerFactory.class.getName());
    private static Deserializer OBJECT_DESERIALIZER = new BasicDeserializer(13);
    private static HashMap _staticSerializerMap;
    private static HashMap _staticDeserializerMap;
    private static HashMap _staticTypeMap;
    private ClassLoader _loader;
    protected Serializer _defaultSerializer;
    protected ArrayList _factories = new ArrayList();
    protected CollectionSerializer _collectionSerializer;
    protected MapSerializer _mapSerializer;
    private Deserializer _hashMapDeserializer;
    private Deserializer _arrayListDeserializer;
    private HashMap _cachedSerializerMap;
    private HashMap _cachedDeserializerMap;
    private HashMap _cachedTypeDeserializerMap;
    private static Object _unknown;
    private static ConcurrentHashMap<String, Object> _cachedUnkownClassMap;
    private boolean _isAllowNonSerializable;

    public SerializerFactory() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public SerializerFactory(ClassLoader loader) {
        this._loader = loader;
    }

    public ClassLoader getClassLoader() {
        return this._loader;
    }

    public void setSendCollectionType(boolean isSendType) {
        if (this._collectionSerializer == null) {
            this._collectionSerializer = new CollectionSerializer();
        }
        this._collectionSerializer.setSendJavaType(isSendType);
        if (this._mapSerializer == null) {
            this._mapSerializer = new MapSerializer();
        }
        this._mapSerializer.setSendJavaType(isSendType);
    }

    public void addFactory(AbstractSerializerFactory factory) {
        this._factories.add(factory);
    }

    public void setAllowNonSerializable(boolean allow) {
        this._isAllowNonSerializable = allow;
    }

    public boolean isAllowNonSerializable() {
        return this._isAllowNonSerializable;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Serializer getSerializer(Class cl) throws HessianProtocolException {
        Serializer serializer = (Serializer)_staticSerializerMap.get(cl);
        if (serializer != null) {
            return serializer;
        }
        if (this._cachedSerializerMap != null) {
            HashMap hashMap = this._cachedSerializerMap;
            synchronized (hashMap) {
                serializer = (Serializer)this._cachedSerializerMap.get(cl);
            }
            if (serializer != null) {
                return serializer;
            }
        }
        for (int i = 0; serializer == null && this._factories != null && i < this._factories.size(); ++i) {
            AbstractSerializerFactory factory = (AbstractSerializerFactory)this._factories.get(i);
            serializer = factory.getSerializer(cl);
        }
        if (serializer == null) {
            if (SerializerFactory.isZoneId(cl)) {
                serializer = ZoneIdSerializer.getInstance();
            } else if (JavaSerializer.getWriteReplace(cl) != null) {
                serializer = new JavaSerializer(cl, this._loader);
            } else if (HessianRemoteObject.class.isAssignableFrom(cl)) {
                serializer = new RemoteSerializer();
            } else if (Map.class.isAssignableFrom(cl)) {
                if (this._mapSerializer == null) {
                    this._mapSerializer = new MapSerializer();
                }
                serializer = this._mapSerializer;
            } else if (Collection.class.isAssignableFrom(cl)) {
                if (this._collectionSerializer == null) {
                    this._collectionSerializer = new CollectionSerializer();
                }
                serializer = this._collectionSerializer;
            } else if (cl.isArray()) {
                serializer = new ArraySerializer();
            } else if (Throwable.class.isAssignableFrom(cl)) {
                serializer = new ThrowableSerializer(cl, this.getClassLoader());
            } else if (InputStream.class.isAssignableFrom(cl)) {
                serializer = new InputStreamSerializer();
            } else if (Iterator.class.isAssignableFrom(cl)) {
                serializer = IteratorSerializer.create();
            } else if (Enumeration.class.isAssignableFrom(cl)) {
                serializer = EnumerationSerializer.create();
            } else if (Calendar.class.isAssignableFrom(cl)) {
                serializer = CalendarSerializer.create();
            } else if (Locale.class.isAssignableFrom(cl)) {
                serializer = LocaleSerializer.create();
            } else if (Enum.class.isAssignableFrom(cl)) {
                serializer = new EnumSerializer(cl);
            }
        }
        if (serializer == null) {
            serializer = this.getDefaultSerializer(cl);
        }
        if (this._cachedSerializerMap == null) {
            this._cachedSerializerMap = new HashMap(8);
        }
        HashMap i = this._cachedSerializerMap;
        synchronized (i) {
            this._cachedSerializerMap.put(cl, serializer);
        }
        return serializer;
    }

    protected Serializer getDefaultSerializer(Class cl) {
        if (this._defaultSerializer != null) {
            return this._defaultSerializer;
        }
        if (!Serializable.class.isAssignableFrom(cl) && !this._isAllowNonSerializable) {
            throw new IllegalStateException("Serialized class " + cl.getName() + " must implement java.io.Serializable");
        }
        return new JavaSerializer(cl, this._loader);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Deserializer getDeserializer(Class cl) throws HessianProtocolException {
        Deserializer deserializer = (Deserializer)_staticDeserializerMap.get(cl);
        if (deserializer != null) {
            return deserializer;
        }
        if (this._cachedDeserializerMap != null) {
            HashMap hashMap = this._cachedDeserializerMap;
            synchronized (hashMap) {
                deserializer = (Deserializer)this._cachedDeserializerMap.get(cl);
            }
            if (deserializer != null) {
                return deserializer;
            }
        }
        for (int i = 0; deserializer == null && this._factories != null && i < this._factories.size(); ++i) {
            AbstractSerializerFactory factory = (AbstractSerializerFactory)this._factories.get(i);
            deserializer = factory.getDeserializer(cl);
        }
        if (deserializer == null) {
            deserializer = Collection.class.isAssignableFrom(cl) ? new CollectionDeserializer(cl) : (Map.class.isAssignableFrom(cl) ? new MapDeserializer(cl) : (cl.isInterface() ? new ObjectDeserializer(cl) : (cl.isArray() ? new ArrayDeserializer(cl.getComponentType()) : (Enumeration.class.isAssignableFrom(cl) ? EnumerationDeserializer.create() : (Enum.class.isAssignableFrom(cl) ? new EnumDeserializer(cl) : (Class.class.equals((Object)cl) ? new ClassDeserializer(this._loader) : this.getDefaultDeserializer(cl)))))));
        }
        if (this._cachedDeserializerMap == null) {
            this._cachedDeserializerMap = new HashMap(8);
        }
        HashMap i = this._cachedDeserializerMap;
        synchronized (i) {
            this._cachedDeserializerMap.put(cl, deserializer);
        }
        return deserializer;
    }

    protected Deserializer getDefaultDeserializer(Class cl) {
        return new JavaDeserializer(cl);
    }

    public Object readList(AbstractHessianInput in, int length, String type) throws HessianProtocolException, IOException {
        Deserializer deserializer = this.getDeserializer(type);
        if (deserializer != null) {
            return deserializer.readList(in, length);
        }
        return new CollectionDeserializer(ArrayList.class).readList(in, length);
    }

    public Object readMap(AbstractHessianInput in, String type) throws HessianProtocolException, IOException {
        return this.readMap(in, type, null, null);
    }

    public Object readMap(AbstractHessianInput in, String type, Class<?> expectKeyType, Class<?> expectValueType) throws HessianProtocolException, IOException {
        Deserializer deserializer = this.getDeserializer(type);
        if (deserializer != null) {
            return deserializer.readMap(in);
        }
        if (this._hashMapDeserializer != null) {
            return this._hashMapDeserializer.readMap(in, expectKeyType, expectValueType);
        }
        this._hashMapDeserializer = new MapDeserializer(HashMap.class);
        return this._hashMapDeserializer.readMap(in, expectKeyType, expectValueType);
    }

    public Object readObject(AbstractHessianInput in, String type, String[] fieldNames) throws HessianProtocolException, IOException {
        Deserializer deserializer = this.getDeserializer(type);
        if (deserializer != null) {
            return deserializer.readObject(in, fieldNames);
        }
        if (this._hashMapDeserializer != null) {
            return this._hashMapDeserializer.readObject(in, fieldNames);
        }
        this._hashMapDeserializer = new MapDeserializer(HashMap.class);
        return this._hashMapDeserializer.readObject(in, fieldNames);
    }

    public Deserializer getObjectDeserializer(String type, Class cl) throws HessianProtocolException {
        Deserializer reader = this.getObjectDeserializer(type);
        if (cl == null || cl.equals(reader.getType()) || cl.isAssignableFrom(reader.getType()) || HessianHandle.class.isAssignableFrom(reader.getType())) {
            return reader;
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("hessian: expected '" + cl.getName() + "' at '" + type + "' (" + reader.getType().getName() + ")");
        }
        return this.getDeserializer(cl);
    }

    public Deserializer getObjectDeserializer(String type) throws HessianProtocolException {
        Deserializer deserializer = this.getDeserializer(type);
        if (deserializer != null) {
            return deserializer;
        }
        if (this._hashMapDeserializer != null) {
            return this._hashMapDeserializer;
        }
        this._hashMapDeserializer = new MapDeserializer(HashMap.class);
        return this._hashMapDeserializer;
    }

    public Deserializer getListDeserializer(String type, Class cl) throws HessianProtocolException {
        Deserializer reader = this.getListDeserializer(type);
        if (cl == null || cl.equals(reader.getType()) || cl.isAssignableFrom(reader.getType())) {
            return reader;
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("hessian: expected '" + cl.getName() + "' at '" + type + "' (" + reader.getType().getName() + ")");
        }
        return this.getDeserializer(cl);
    }

    public Deserializer getListDeserializer(String type) throws HessianProtocolException {
        Deserializer deserializer = this.getDeserializer(type);
        if (deserializer != null) {
            return deserializer;
        }
        if (this._arrayListDeserializer != null) {
            return this._arrayListDeserializer;
        }
        this._arrayListDeserializer = new CollectionDeserializer(ArrayList.class);
        return this._arrayListDeserializer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Deserializer getDeserializer(String type) throws HessianProtocolException {
        Deserializer deserializer;
        block17 : {
            if (type == null || type.equals("")) {
                return null;
            }
            if (this._cachedTypeDeserializerMap != null) {
                HashMap hashMap = this._cachedTypeDeserializerMap;
                synchronized (hashMap) {
                    deserializer = (Deserializer)this._cachedTypeDeserializerMap.get(type);
                }
                if (deserializer != null) {
                    return deserializer;
                }
            }
            if ((deserializer = (Deserializer)_staticTypeMap.get(type)) != null) {
                return deserializer;
            }
            if (type.startsWith("[")) {
                Deserializer subDeserializer = this.getDeserializer(type.substring(1));
                deserializer = subDeserializer != null ? new ArrayDeserializer(subDeserializer.getType()) : new ArrayDeserializer(Object.class);
            } else {
                boolean isUnkownType = _cachedUnkownClassMap.get(type) != null;
                try {
                    if (!isUnkownType) {
                        Class<?> cl = Class.forName(type, false, this._loader);
                        deserializer = this.getDeserializer(cl);
                    }
                }
                catch (Exception e) {
                    if (isUnkownType || _cachedUnkownClassMap.putIfAbsent(type, _unknown) != null) break block17;
                    log.warning("Hessian/Burlap: '" + type + "' is an unknown class in " + this._loader + ":\n" + e);
                    log.log(Level.FINER, e.toString(), e);
                }
            }
        }
        if (deserializer != null) {
            if (this._cachedTypeDeserializerMap == null) {
                this._cachedTypeDeserializerMap = new HashMap(8);
            }
            HashMap isUnkownType = this._cachedTypeDeserializerMap;
            synchronized (isUnkownType) {
                this._cachedTypeDeserializerMap.put(type, deserializer);
            }
        }
        return deserializer;
    }

    private static void addBasic(Class cl, String typeName, int type) {
        _staticSerializerMap.put(cl, new BasicSerializer(type));
        BasicDeserializer deserializer = new BasicDeserializer(type);
        _staticDeserializerMap.put(cl, deserializer);
        _staticTypeMap.put(typeName, deserializer);
    }

    private static boolean isJava8() {
        String javaVersion = System.getProperty("java.specification.version");
        return Double.valueOf(javaVersion) >= 1.8;
    }

    private static boolean isZoneId(Class cl) {
        try {
            return SerializerFactory.isJava8() && Class.forName("java.time.ZoneId").isAssignableFrom(cl);
        }
        catch (ClassNotFoundException classNotFoundException) {
            return false;
        }
    }

    static {
        _unknown = new Object();
        _cachedUnkownClassMap = new ConcurrentHashMap();
        _staticSerializerMap = new HashMap();
        _staticDeserializerMap = new HashMap();
        _staticTypeMap = new HashMap();
        SerializerFactory.addBasic(Void.TYPE, "void", 0);
        SerializerFactory.addBasic(Boolean.class, "boolean", 1);
        SerializerFactory.addBasic(Byte.class, "byte", 2);
        SerializerFactory.addBasic(Short.class, "short", 3);
        SerializerFactory.addBasic(Integer.class, "int", 4);
        SerializerFactory.addBasic(Long.class, "long", 5);
        SerializerFactory.addBasic(Float.class, "float", 6);
        SerializerFactory.addBasic(Double.class, "double", 7);
        SerializerFactory.addBasic(Character.class, "char", 9);
        SerializerFactory.addBasic(String.class, "string", 10);
        SerializerFactory.addBasic(Object.class, "object", 13);
        SerializerFactory.addBasic(Date.class, "date", 11);
        SerializerFactory.addBasic(Boolean.TYPE, "boolean", 1);
        SerializerFactory.addBasic(Byte.TYPE, "byte", 2);
        SerializerFactory.addBasic(Short.TYPE, "short", 3);
        SerializerFactory.addBasic(Integer.TYPE, "int", 4);
        SerializerFactory.addBasic(Long.TYPE, "long", 5);
        SerializerFactory.addBasic(Float.TYPE, "float", 6);
        SerializerFactory.addBasic(Double.TYPE, "double", 7);
        SerializerFactory.addBasic(Character.TYPE, "char", 8);
        SerializerFactory.addBasic(boolean[].class, "[boolean", 14);
        SerializerFactory.addBasic(byte[].class, "[byte", 15);
        SerializerFactory.addBasic(short[].class, "[short", 16);
        SerializerFactory.addBasic(int[].class, "[int", 17);
        SerializerFactory.addBasic(long[].class, "[long", 18);
        SerializerFactory.addBasic(float[].class, "[float", 19);
        SerializerFactory.addBasic(double[].class, "[double", 20);
        SerializerFactory.addBasic(char[].class, "[char", 21);
        SerializerFactory.addBasic(String[].class, "[string", 22);
        SerializerFactory.addBasic(Object[].class, "[object", 23);
        _staticSerializerMap.put(Class.class, new ClassSerializer());
        _staticDeserializerMap.put(Number.class, new BasicDeserializer(12));
        _staticSerializerMap.put(BigDecimal.class, new StringValueSerializer());
        try {
            _staticDeserializerMap.put(BigDecimal.class, new StringValueDeserializer(BigDecimal.class));
            _staticDeserializerMap.put(BigInteger.class, new BigIntegerDeserializer());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        _staticSerializerMap.put(File.class, new StringValueSerializer());
        try {
            _staticDeserializerMap.put(File.class, new StringValueDeserializer(File.class));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        _staticSerializerMap.put(ObjectName.class, new StringValueSerializer());
        try {
            _staticDeserializerMap.put(ObjectName.class, new StringValueDeserializer(ObjectName.class));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        _staticSerializerMap.put(java.sql.Date.class, new SqlDateSerializer());
        _staticSerializerMap.put(Time.class, new SqlDateSerializer());
        _staticSerializerMap.put(Timestamp.class, new SqlDateSerializer());
        _staticSerializerMap.put(InputStream.class, new InputStreamSerializer());
        _staticDeserializerMap.put(InputStream.class, new InputStreamDeserializer());
        try {
            _staticDeserializerMap.put(java.sql.Date.class, new SqlDateDeserializer(java.sql.Date.class));
            _staticDeserializerMap.put(Time.class, new SqlDateDeserializer(Time.class));
            _staticDeserializerMap.put(Timestamp.class, new SqlDateDeserializer(Timestamp.class));
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            Class<StackTraceElement> stackTrace = StackTraceElement.class;
            _staticDeserializerMap.put(stackTrace, new StackTraceElementDeserializer());
        }
        catch (Throwable stackTrace) {
            // empty catch block
        }
        try {
            if (SerializerFactory.isJava8()) {
                _staticSerializerMap.put(Class.forName("java.time.LocalTime"), Java8TimeSerializer.create(LocalTimeHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.LocalDate"), Java8TimeSerializer.create(LocalDateHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.LocalDateTime"), Java8TimeSerializer.create(LocalDateTimeHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.Instant"), Java8TimeSerializer.create(InstantHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.Duration"), Java8TimeSerializer.create(DurationHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.Period"), Java8TimeSerializer.create(PeriodHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.Year"), Java8TimeSerializer.create(YearHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.YearMonth"), Java8TimeSerializer.create(YearMonthHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.MonthDay"), Java8TimeSerializer.create(MonthDayHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.OffsetDateTime"), Java8TimeSerializer.create(OffsetDateTimeHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.ZoneOffset"), Java8TimeSerializer.create(ZoneOffsetHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.OffsetTime"), Java8TimeSerializer.create(OffsetTimeHandle.class));
                _staticSerializerMap.put(Class.forName("java.time.ZonedDateTime"), Java8TimeSerializer.create(ZonedDateTimeHandle.class));
            }
        }
        catch (Throwable t) {
            log.warning(String.valueOf(t.getCause()));
        }
    }
}

