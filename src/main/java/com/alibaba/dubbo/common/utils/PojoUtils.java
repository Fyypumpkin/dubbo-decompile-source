/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson.JSONArray
 */
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.common.utils.CompatibleTypeUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.fastjson.JSONArray;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class PojoUtils {
    private static final ConcurrentMap<String, Method> NAME_METHODS_CACHE = new ConcurrentHashMap<String, Method>();
    private static final ConcurrentMap<Class<?>, ConcurrentMap<String, Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap();

    public static Object[] generalize(Object[] objs) {
        Object[] dests = new Object[objs.length];
        for (int i = 0; i < objs.length; ++i) {
            dests[i] = PojoUtils.generalize(objs[i]);
        }
        return dests;
    }

    public static Object[] realize(Object[] objs, Class<?>[] types) {
        if (objs.length != types.length) {
            throw new IllegalArgumentException("args.length != types.length");
        }
        Object[] dests = new Object[objs.length];
        for (int i = 0; i < objs.length; ++i) {
            dests[i] = PojoUtils.realize(objs[i], types[i]);
        }
        return dests;
    }

    public static Object[] realize(Object[] objs, Class<?>[] types, Type[] gtypes) {
        if (objs.length != types.length || objs.length != gtypes.length) {
            throw new IllegalArgumentException("args.length != types.length");
        }
        Object[] dests = new Object[objs.length];
        for (int i = 0; i < objs.length; ++i) {
            dests[i] = PojoUtils.realize(objs[i], types[i], gtypes[i]);
        }
        return dests;
    }

    public static Object generalize(Object pojo) {
        return PojoUtils.generalize(pojo, new IdentityHashMap<Object, Object>());
    }

    private static Object generalize(Object pojo, Map<Object, Object> history) {
        if (pojo == null) {
            return null;
        }
        if (pojo instanceof Enum) {
            return ((Enum)pojo).name();
        }
        if (pojo.getClass().isArray() && Enum.class.isAssignableFrom(pojo.getClass().getComponentType())) {
            int len = Array.getLength(pojo);
            String[] values = new String[len];
            for (int i = 0; i < len; ++i) {
                values[i] = ((Enum)Array.get(pojo, i)).name();
            }
            return values;
        }
        if (ReflectUtils.isPrimitives(pojo.getClass())) {
            return pojo;
        }
        if (pojo instanceof Class) {
            return ((Class)pojo).getName();
        }
        Object o = history.get(pojo);
        if (o != null) {
            return o;
        }
        history.put(pojo, pojo);
        if (pojo.getClass().isArray()) {
            int len = Array.getLength(pojo);
            Object[] dest = new Object[len];
            history.put(pojo, dest);
            for (int i = 0; i < len; ++i) {
                Object obj = Array.get(pojo, i);
                dest[i] = PojoUtils.generalize(obj, history);
            }
            return dest;
        }
        if (pojo instanceof Collection) {
            Collection src = (Collection)pojo;
            int len = src.size();
            AbstractCollection dest = pojo instanceof List ? new ArrayList(len) : new HashSet(len);
            history.put(pojo, dest);
            for (Object obj : src) {
                dest.add(PojoUtils.generalize(obj, history));
            }
            return dest;
        }
        if (pojo instanceof Map) {
            Map src = (Map)pojo;
            Map dest = PojoUtils.createMap(src);
            history.put(pojo, dest);
            for (Map.Entry obj : src.entrySet()) {
                dest.put(PojoUtils.generalize(obj.getKey(), history), PojoUtils.generalize(obj.getValue(), history));
            }
            return dest;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        history.put(pojo, map);
        map.put("class", pojo.getClass().getName());
        for (Method method : pojo.getClass().getMethods()) {
            if (!ReflectUtils.isBeanPropertyReadMethod(method)) continue;
            try {
                map.put(ReflectUtils.getPropertyNameFromBeanReadMethod(method), PojoUtils.generalize(method.invoke(pojo, new Object[0]), history));
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        for (AccessibleObject field : pojo.getClass().getFields()) {
            if (!ReflectUtils.isPublicInstanceField((Field)field)) continue;
            try {
                Object pojoGenerilizedValue;
                Object fieldValue = ((Field)field).get(pojo);
                if (history.containsKey(pojo) && (pojoGenerilizedValue = history.get(pojo)) instanceof Map && ((Map)pojoGenerilizedValue).containsKey(((Field)field).getName()) || fieldValue == null) continue;
                map.put(((Field)field).getName(), PojoUtils.generalize(fieldValue, history));
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return map;
    }

    public static Object realize(Object pojo, Class<?> type) {
        return PojoUtils.realize0(pojo, type, null, new IdentityHashMap<Object, Object>());
    }

    public static Object realize(Object pojo, Class<?> type, Type genericType) {
        return PojoUtils.realize0(pojo, type, genericType, new IdentityHashMap<Object, Object>());
    }

    private static Collection<Object> createCollection(Class<?> type, int len) {
        if (type.isAssignableFrom(ArrayList.class)) {
            return new ArrayList<Object>(len);
        }
        if (type.isAssignableFrom(HashSet.class)) {
            return new HashSet<Object>(len);
        }
        if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
            try {
                return (Collection)type.newInstance();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return new ArrayList<Object>();
    }

    private static Map createMap(Map src) {
        Class<?> cl = src.getClass();
        Map result = null;
        if (HashMap.class == cl) {
            result = new HashMap();
        } else if (Hashtable.class == cl) {
            result = new Hashtable();
        } else if (IdentityHashMap.class == cl) {
            result = new IdentityHashMap();
        } else if (LinkedHashMap.class == cl) {
            result = new LinkedHashMap();
        } else if (Properties.class == cl) {
            result = new Properties();
        } else if (TreeMap.class == cl) {
            result = new TreeMap();
        } else {
            if (WeakHashMap.class == cl) {
                return new WeakHashMap();
            }
            if (ConcurrentHashMap.class == cl) {
                result = new ConcurrentHashMap();
            } else if (ConcurrentSkipListMap.class == cl) {
                result = new ConcurrentSkipListMap();
            } else {
                try {
                    result = (Map)cl.newInstance();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (result == null) {
                    try {
                        Constructor<?> constructor = cl.getConstructor(Map.class);
                        result = (Map)constructor.newInstance(Collections.EMPTY_MAP);
                    }
                    catch (Exception constructor) {
                        // empty catch block
                    }
                }
            }
        }
        if (result == null) {
            result = new HashMap();
        }
        return result;
    }

    private static Object realize0(Object pojo, Class<?> type, Type genericType, Map<Object, Object> history) {
        if (pojo == null) {
            return null;
        }
        if (type != null && type.isEnum() && pojo.getClass() == String.class) {
            return Enum.valueOf(type, (String)pojo);
        }
        if (!(!ReflectUtils.isPrimitives(pojo.getClass()) || type != null && type.isArray() && type.getComponentType().isEnum() && pojo.getClass() == String[].class)) {
            return CompatibleTypeUtils.compatibleTypeConvert(pojo, type);
        }
        Object o = history.get(pojo);
        if (o != null) {
            return o;
        }
        history.put(pojo, pojo);
        if (pojo.getClass().isArray()) {
            if (Collection.class.isAssignableFrom(type)) {
                Class<?> ctype = pojo.getClass().getComponentType();
                int len = Array.getLength(pojo);
                Collection<Object> dest = PojoUtils.createCollection(type, len);
                history.put(pojo, dest);
                for (int i = 0; i < len; ++i) {
                    Object obj = Array.get(pojo, i);
                    Object value = PojoUtils.realize0(obj, ctype, null, history);
                    dest.add(value);
                }
                return dest;
            }
            Class<?> ctype = type != null && type.isArray() ? type.getComponentType() : pojo.getClass().getComponentType();
            int len = Array.getLength(pojo);
            Object dest = Array.newInstance(ctype, len);
            history.put(pojo, dest);
            for (int i = 0; i < len; ++i) {
                Object obj = Array.get(pojo, i);
                Object value = PojoUtils.realize0(obj, ctype, null, history);
                Array.set(dest, i, value);
            }
            return dest;
        }
        if (pojo instanceof Collection) {
            if (type.isArray()) {
                Class<?> ctype = type.getComponentType();
                Collection src = (Collection)pojo;
                int len = src.size();
                Object dest = Array.newInstance(ctype, len);
                history.put(pojo, dest);
                int i = 0;
                for (Object obj : src) {
                    Object value = PojoUtils.realize0(obj, ctype, null, history);
                    Array.set(dest, i, value);
                    ++i;
                }
                return dest;
            }
            Collection src = (Collection)pojo;
            int len = src.size();
            if (len == 0 && pojo instanceof JSONArray && !Collection.class.isAssignableFrom(type)) {
                return null;
            }
            Collection<Object> dest = PojoUtils.createCollection(type, len);
            history.put(pojo, dest);
            for (Object obj : src) {
                Type keyType = PojoUtils.getGenericClassByIndex(genericType, 0);
                Class keyClazz = obj.getClass();
                if (keyType instanceof Class) {
                    keyClazz = (Class)keyType;
                }
                Object value = PojoUtils.realize0(obj, keyClazz, keyType, history);
                dest.add(value);
            }
            return dest;
        }
        if (pojo instanceof Map && type != null) {
            Object message;
            Object dest;
            Map map;
            Object className = ((Map)pojo).get("class");
            if (className instanceof String) {
                try {
                    type = ClassHelper.forName((String)className);
                }
                catch (ClassNotFoundException len) {
                    // empty catch block
                }
            }
            if (!type.isInterface() && !type.isAssignableFrom(pojo.getClass())) {
                try {
                    map = (Map)type.newInstance();
                    Map mapPojo = (Map)pojo;
                    map.putAll(mapPojo);
                    map.remove("class");
                }
                catch (Exception e) {
                    map = (Map)pojo;
                }
            } else {
                map = (Map)pojo;
            }
            if (Map.class.isAssignableFrom(type) || type == Object.class) {
                Map result = PojoUtils.createMap(map);
                history.put(pojo, result);
                for (Map.Entry entry : map.entrySet()) {
                    Class keyClazz;
                    Type keyType = PojoUtils.getGenericClassByIndex(genericType, 0);
                    Type valueType = PojoUtils.getGenericClassByIndex(genericType, 1);
                    if (keyType instanceof Class) {
                        keyClazz = (Class)keyType;
                    } else {
                        Class class_ = keyClazz = entry.getKey() == null ? null : entry.getKey().getClass();
                    }
                    Class<Object> valueClazz = valueType instanceof Class ? (Class<Object>)valueType : (entry.getValue() == null ? null : entry.getValue().getClass());
                    Object key = keyClazz == null ? entry.getKey() : PojoUtils.realize0(entry.getKey(), keyClazz, keyType, history);
                    Object value = valueClazz == null ? entry.getValue() : PojoUtils.realize0(entry.getValue(), valueClazz, valueType, history);
                    result.put(key, value);
                }
                return result;
            }
            if (type.isInterface()) {
                dest = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{type}, new PojoInvocationHandler(map));
                history.put(pojo, dest);
                return dest;
            }
            dest = PojoUtils.newInstance(type);
            history.put(pojo, dest);
            for (Map.Entry entry : map.entrySet()) {
                Object key = entry.getKey();
                if (!(key instanceof String)) continue;
                String name = (String)key;
                Object value = entry.getValue();
                if (value == null) continue;
                Method method = PojoUtils.getSetterMethod(dest.getClass(), name, value.getClass());
                Field field = PojoUtils.getField(dest.getClass(), name);
                if (method != null) {
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    Type ptype = method.getGenericParameterTypes()[0];
                    value = PojoUtils.realize0(value, method.getParameterTypes()[0], ptype, history);
                    try {
                        method.invoke(dest, value);
                        continue;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to set pojo " + dest.getClass().getSimpleName() + " property " + name + " value " + value + "(" + value.getClass() + "), cause: " + e.getMessage(), e);
                    }
                }
                if (field == null) continue;
                value = PojoUtils.realize0(value, field.getType(), field.getGenericType(), history);
                try {
                    field.set(dest, value);
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(new StringBuilder(32).append("Failed to set filed ").append(name).append(" of pojo ").append(dest.getClass().getName()).append(" : ").append(e.getMessage()).toString(), e);
                }
            }
            if (dest instanceof Throwable && (message = map.get("message")) instanceof String) {
                try {
                    Field filed = Throwable.class.getDeclaredField("detailMessage");
                    if (!filed.isAccessible()) {
                        filed.setAccessible(true);
                    }
                    filed.set(dest, (String)message);
                }
                catch (Exception filed) {
                    // empty catch block
                }
            }
            return dest;
        }
        return pojo;
    }

    private static Type getGenericClassByIndex(Type genericType, int index) {
        Type clazz = null;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType)genericType;
            Type[] types = t.getActualTypeArguments();
            clazz = types[index];
        }
        return clazz;
    }

    private static Object newInstance(Class<?> cls) {
        try {
            return cls.newInstance();
        }
        catch (Throwable t) {
            try {
                Constructor<?>[] constructors = cls.getConstructors();
                if (constructors != null && constructors.length == 0) {
                    throw new RuntimeException("Illegal constructor: " + cls.getName());
                }
                Constructor<?> constructor = constructors[0];
                if (constructor.getParameterTypes().length > 0) {
                    for (Constructor<?> c : constructors) {
                        if (c.getParameterTypes().length < constructor.getParameterTypes().length && (constructor = c).getParameterTypes().length == 0) break;
                    }
                }
                return constructor.newInstance(new Object[constructor.getParameterTypes().length]);
            }
            catch (InstantiationException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            catch (InvocationTargetException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private static Method getSetterMethod(Class<?> cls, String property, Class<?> valueCls) {
        String name = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
        Method method = (Method)NAME_METHODS_CACHE.get(cls.getName() + "." + name + "(" + valueCls.getName() + ")");
        if (method == null) {
            try {
                method = cls.getMethod(name, valueCls);
            }
            catch (NoSuchMethodException e) {
                for (Method m : cls.getMethods()) {
                    if (!ReflectUtils.isBeanPropertyWriteMethod(m) || !m.getName().equals(name)) continue;
                    method = m;
                }
            }
            if (method != null) {
                NAME_METHODS_CACHE.put(cls.getName() + "." + name + "(" + valueCls.getName() + ")", method);
            }
        }
        return method;
    }

    private static Field getField(Class<?> cls, String fieldName) {
        Field result = null;
        if (CLASS_FIELD_CACHE.containsKey(cls) && ((ConcurrentMap)CLASS_FIELD_CACHE.get(cls)).containsKey(fieldName)) {
            return (Field)((ConcurrentMap)CLASS_FIELD_CACHE.get(cls)).get(fieldName);
        }
        try {
            result = cls.getField(fieldName);
        }
        catch (NoSuchFieldException e) {
            for (Field field : cls.getFields()) {
                if (!fieldName.equals(field.getName()) || !ReflectUtils.isPublicInstanceField(field)) continue;
                result = field;
                break;
            }
        }
        if (result != null) {
            ConcurrentMap<String, Field> fields = (ConcurrentHashMap)CLASS_FIELD_CACHE.get(cls);
            if (fields == null) {
                fields = new ConcurrentHashMap();
                CLASS_FIELD_CACHE.putIfAbsent(cls, fields);
            }
            fields = (ConcurrentMap)CLASS_FIELD_CACHE.get(cls);
            fields.putIfAbsent(fieldName, result);
        }
        return result;
    }

    public static boolean isPojo(Class<?> cls) {
        return !ReflectUtils.isPrimitives(cls) && !Collection.class.isAssignableFrom(cls) && !Map.class.isAssignableFrom(cls);
    }

    private static class PojoInvocationHandler
    implements InvocationHandler {
        private Map<Object, Object> map;

        public PojoInvocationHandler(Map<Object, Object> map) {
            this.map = map;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this.map, args);
            }
            String methodName = method.getName();
            Object value = null;
            value = methodName.length() > 3 && methodName.startsWith("get") ? this.map.get(methodName.substring(3, 4).toLowerCase() + methodName.substring(4)) : (methodName.length() > 2 && methodName.startsWith("is") ? this.map.get(methodName.substring(2, 3).toLowerCase() + methodName.substring(3)) : this.map.get(methodName.substring(0, 1).toLowerCase() + methodName.substring(1)));
            if (value instanceof Map && !Map.class.isAssignableFrom(method.getReturnType())) {
                value = PojoUtils.realize0((Map)value, method.getReturnType(), null, new IdentityHashMap());
            }
            return value;
        }
    }

}

