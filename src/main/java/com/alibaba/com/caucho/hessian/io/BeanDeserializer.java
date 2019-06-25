/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import com.alibaba.com.caucho.hessian.io.AbstractMapDeserializer;
import com.alibaba.com.caucho.hessian.io.IOExceptionWrapper;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class BeanDeserializer
extends AbstractMapDeserializer {
    private Class _type;
    private HashMap _methodMap;
    private Method _readResolve;
    private Constructor _constructor;
    private Object[] _constructorArgs;

    public BeanDeserializer(Class cl) {
        this._type = cl;
        this._methodMap = this.getMethodMap(cl);
        this._readResolve = this.getReadResolve(cl);
        Constructor<?>[] constructors = cl.getConstructors();
        int bestLength = Integer.MAX_VALUE;
        for (int i = 0; i < constructors.length; ++i) {
            if (constructors[i].getParameterTypes().length >= bestLength) continue;
            this._constructor = constructors[i];
            bestLength = this._constructor.getParameterTypes().length;
        }
        if (this._constructor != null) {
            this._constructor.setAccessible(true);
            Class<?>[] params = this._constructor.getParameterTypes();
            this._constructorArgs = new Object[params.length];
            for (int i = 0; i < params.length; ++i) {
                this._constructorArgs[i] = BeanDeserializer.getParamArg(params[i]);
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
        catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }

    public Object readMap(AbstractHessianInput in, Object obj) throws IOException {
        try {
            int ref = in.addRef(obj);
            while (!in.isEnd()) {
                Object value;
                Object key = in.readObject();
                Method method = (Method)this._methodMap.get(key);
                if (method != null) {
                    value = in.readObject(method.getParameterTypes()[0]);
                    method.invoke(obj, value);
                    continue;
                }
                value = in.readObject();
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

    private Object resolve(Object obj) {
        try {
            if (this._readResolve != null) {
                return this._readResolve.invoke(obj, new Object[0]);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return obj;
    }

    protected Object instantiate() throws Exception {
        return this._constructor.newInstance(this._constructorArgs);
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

    protected HashMap getMethodMap(Class cl) {
        HashMap<String, Method> methodMap = new HashMap<String, Method>();
        while (cl != null) {
            Method[] methods = cl.getDeclaredMethods();
            for (int i = 0; i < methods.length; ++i) {
                Class<?>[] paramTypes;
                String name;
                int j;
                Method method = methods[i];
                if (Modifier.isStatic(method.getModifiers()) || !(name = method.getName()).startsWith("set") || (paramTypes = method.getParameterTypes()).length != 1 || !method.getReturnType().equals(Void.TYPE) || this.findGetter(methods, name, paramTypes[0]) == null) continue;
                try {
                    method.setAccessible(true);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
                name = name.substring(3);
                for (j = 0; j < name.length() && Character.isUpperCase(name.charAt(j)); ++j) {
                }
                if (j == 1) {
                    name = name.substring(0, j).toLowerCase() + name.substring(j);
                } else if (j > 1) {
                    name = name.substring(0, j - 1).toLowerCase() + name.substring(j - 1);
                }
                methodMap.put(name, method);
            }
            cl = cl.getSuperclass();
        }
        return methodMap;
    }

    private Method findGetter(Method[] methods, String setterName, Class arg) {
        String getterName = "get" + setterName.substring(3);
        for (int i = 0; i < methods.length; ++i) {
            Class<?>[] params;
            Method method = methods[i];
            if (!method.getName().equals(getterName) || !method.getReturnType().equals((Object)arg) || (params = method.getParameterTypes()).length != 0) continue;
            return method;
        }
        return null;
    }

    protected static Object getParamArg(Class cl) {
        if (!cl.isPrimitive()) {
            return null;
        }
        if (Boolean.TYPE.equals((Object)cl)) {
            return Boolean.FALSE;
        }
        if (Byte.TYPE.equals((Object)cl)) {
            return (byte)0;
        }
        if (Short.TYPE.equals((Object)cl)) {
            return (short)0;
        }
        if (Character.TYPE.equals((Object)cl)) {
            return Character.valueOf('\u0000');
        }
        if (Integer.TYPE.equals((Object)cl)) {
            return 0;
        }
        if (Long.TYPE.equals((Object)cl)) {
            return 0L;
        }
        if (Float.TYPE.equals((Object)cl)) {
            return 0.0;
        }
        if (Double.TYPE.equals((Object)cl)) {
            return 0.0;
        }
        throw new UnsupportedOperationException();
    }
}

