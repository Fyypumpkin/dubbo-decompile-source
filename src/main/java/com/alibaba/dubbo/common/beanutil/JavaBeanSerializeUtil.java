/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.beanutil;

import com.alibaba.dubbo.common.beanutil.JavaBeanAccessor;
import com.alibaba.dubbo.common.beanutil.JavaBeanDescriptor;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.LogHelper;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public final class JavaBeanSerializeUtil {
    private static final Logger logger = LoggerFactory.getLogger(JavaBeanSerializeUtil.class);
    private static final Map<String, Class<?>> TYPES = new HashMap();
    private static final String ARRAY_PREFIX = "[";
    private static final String REFERENCE_TYPE_PREFIX = "L";
    private static final String REFERENCE_TYPE_SUFFIX = ";";

    public static JavaBeanDescriptor serialize(Object obj) {
        JavaBeanDescriptor result = JavaBeanSerializeUtil.serialize(obj, JavaBeanAccessor.FIELD);
        return result;
    }

    public static JavaBeanDescriptor serialize(Object obj, JavaBeanAccessor accessor) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof JavaBeanDescriptor) {
            return (JavaBeanDescriptor)obj;
        }
        IdentityHashMap<Object, JavaBeanDescriptor> cache = new IdentityHashMap<Object, JavaBeanDescriptor>();
        JavaBeanDescriptor result = JavaBeanSerializeUtil.createDescriptorIfAbsent(obj, accessor, cache);
        return result;
    }

    private static JavaBeanDescriptor createDescriptorForSerialize(Class<?> cl) {
        if (cl.isEnum()) {
            return new JavaBeanDescriptor(cl.getName(), 2);
        }
        if (cl.isArray()) {
            return new JavaBeanDescriptor(cl.getComponentType().getName(), 5);
        }
        if (ReflectUtils.isPrimitive(cl)) {
            return new JavaBeanDescriptor(cl.getName(), 6);
        }
        if (Class.class.equals(cl)) {
            return new JavaBeanDescriptor(Class.class.getName(), 1);
        }
        if (Collection.class.isAssignableFrom(cl)) {
            return new JavaBeanDescriptor(cl.getName(), 3);
        }
        if (Map.class.isAssignableFrom(cl)) {
            return new JavaBeanDescriptor(cl.getName(), 4);
        }
        return new JavaBeanDescriptor(cl.getName(), 7);
    }

    private static JavaBeanDescriptor createDescriptorIfAbsent(Object obj, JavaBeanAccessor accessor, IdentityHashMap<Object, JavaBeanDescriptor> cache) {
        if (cache.containsKey(obj)) {
            return cache.get(obj);
        }
        if (obj instanceof JavaBeanDescriptor) {
            return (JavaBeanDescriptor)obj;
        }
        JavaBeanDescriptor result = JavaBeanSerializeUtil.createDescriptorForSerialize(obj.getClass());
        cache.put(obj, result);
        JavaBeanSerializeUtil.serializeInternal(result, obj, accessor, cache);
        return result;
    }

    private static void serializeInternal(JavaBeanDescriptor descriptor, Object obj, JavaBeanAccessor accessor, IdentityHashMap<Object, JavaBeanDescriptor> cache) {
        block14 : {
            JavaBeanDescriptor valueDescriptor;
            Object value;
            block19 : {
                block18 : {
                    block17 : {
                        block16 : {
                            block15 : {
                                block13 : {
                                    if (obj == null || descriptor == null) {
                                        return;
                                    }
                                    if (!obj.getClass().isEnum()) break block13;
                                    descriptor.setEnumNameProperty(((Enum)obj).name());
                                    break block14;
                                }
                                if (!ReflectUtils.isPrimitive(obj.getClass())) break block15;
                                descriptor.setPrimitiveProperty(obj);
                                break block14;
                            }
                            if (!Class.class.equals(obj.getClass())) break block16;
                            descriptor.setClassNameProperty(((Class)obj).getName());
                            break block14;
                        }
                        if (!obj.getClass().isArray()) break block17;
                        int len = Array.getLength(obj);
                        for (int i = 0; i < len; ++i) {
                            Object item = Array.get(obj, i);
                            if (item == null) {
                                descriptor.setProperty(i, null);
                                continue;
                            }
                            JavaBeanDescriptor itemDescriptor = JavaBeanSerializeUtil.createDescriptorIfAbsent(item, accessor, cache);
                            descriptor.setProperty(i, itemDescriptor);
                        }
                        break block14;
                    }
                    if (!(obj instanceof Collection)) break block18;
                    Collection collection = (Collection)obj;
                    int index = 0;
                    for (Object item : collection) {
                        if (item == null) {
                            descriptor.setProperty(index++, null);
                            continue;
                        }
                        JavaBeanDescriptor itemDescriptor = JavaBeanSerializeUtil.createDescriptorIfAbsent(item, accessor, cache);
                        descriptor.setProperty(index++, itemDescriptor);
                    }
                    break block14;
                }
                if (!(obj instanceof Map)) break block19;
                Map map = (Map)obj;
                for (Object key : map.keySet()) {
                    Object value2 = map.get(key);
                    JavaBeanDescriptor keyDescriptor = key == null ? null : JavaBeanSerializeUtil.createDescriptorIfAbsent(key, accessor, cache);
                    JavaBeanDescriptor valueDescriptor2 = value2 == null ? null : JavaBeanSerializeUtil.createDescriptorIfAbsent(value2, accessor, cache);
                    descriptor.setProperty(keyDescriptor, valueDescriptor2);
                }
                break block14;
            }
            if (JavaBeanAccessor.isAccessByMethod(accessor)) {
                Map<String, Method> methods = ReflectUtils.getBeanPropertyReadMethods(obj.getClass());
                for (Map.Entry<String, AccessibleObject> entry : methods.entrySet()) {
                    try {
                        value = ((Method)entry.getValue()).invoke(obj, new Object[0]);
                        if (value == null) continue;
                        valueDescriptor = JavaBeanSerializeUtil.createDescriptorIfAbsent(value, accessor, cache);
                        descriptor.setProperty(entry.getKey(), valueDescriptor);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            }
            if (!JavaBeanAccessor.isAccessByField(accessor)) break block14;
            Map<String, Field> fields = ReflectUtils.getBeanPropertyFields(obj.getClass());
            for (Map.Entry<String, AccessibleObject> entry : fields.entrySet()) {
                if (descriptor.containsProperty(entry.getKey())) continue;
                try {
                    value = ((Field)entry.getValue()).get(obj);
                    if (value == null) continue;
                    valueDescriptor = JavaBeanSerializeUtil.createDescriptorIfAbsent(value, accessor, cache);
                    descriptor.setProperty(entry.getKey(), valueDescriptor);
                }
                catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }

    public static Object deserialize(JavaBeanDescriptor beanDescriptor) {
        Object result = JavaBeanSerializeUtil.deserialize(beanDescriptor, Thread.currentThread().getContextClassLoader());
        return result;
    }

    public static Object deserialize(JavaBeanDescriptor beanDescriptor, ClassLoader loader) {
        if (beanDescriptor == null) {
            return null;
        }
        IdentityHashMap<JavaBeanDescriptor, Object> cache = new IdentityHashMap<JavaBeanDescriptor, Object>();
        Object result = JavaBeanSerializeUtil.instantiateForDeserialize(beanDescriptor, loader, cache);
        JavaBeanSerializeUtil.deserializeInternal(result, beanDescriptor, loader, cache);
        return result;
    }

    private static void deserializeInternal(Object result, JavaBeanDescriptor beanDescriptor, ClassLoader loader, IdentityHashMap<JavaBeanDescriptor, Object> cache) {
        if (beanDescriptor.isEnumType() || beanDescriptor.isClassType() || beanDescriptor.isPrimitiveType()) {
            return;
        }
        if (beanDescriptor.isArrayType()) {
            int index = 0;
            for (Map.Entry<Object, Object> entry : beanDescriptor) {
                Object item = entry.getValue();
                if (item instanceof JavaBeanDescriptor) {
                    JavaBeanDescriptor itemDescriptor = (JavaBeanDescriptor)entry.getValue();
                    item = JavaBeanSerializeUtil.instantiateForDeserialize(itemDescriptor, loader, cache);
                    JavaBeanSerializeUtil.deserializeInternal(item, itemDescriptor, loader, cache);
                }
                Array.set(result, index++, item);
            }
        } else if (beanDescriptor.isCollectionType()) {
            Collection collection = (Collection)result;
            for (Map.Entry<Object, Object> entry : beanDescriptor) {
                Object item = entry.getValue();
                if (item instanceof JavaBeanDescriptor) {
                    JavaBeanDescriptor itemDescriptor = (JavaBeanDescriptor)entry.getValue();
                    item = JavaBeanSerializeUtil.instantiateForDeserialize(itemDescriptor, loader, cache);
                    JavaBeanSerializeUtil.deserializeInternal(item, itemDescriptor, loader, cache);
                }
                collection.add(item);
            }
        } else if (beanDescriptor.isMapType()) {
            Map map = (Map)result;
            for (Map.Entry<Object, Object> entry : beanDescriptor) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (key != null && key instanceof JavaBeanDescriptor) {
                    JavaBeanDescriptor keyDescriptor = (JavaBeanDescriptor)entry.getKey();
                    key = JavaBeanSerializeUtil.instantiateForDeserialize(keyDescriptor, loader, cache);
                    JavaBeanSerializeUtil.deserializeInternal(key, keyDescriptor, loader, cache);
                }
                if (value != null && value instanceof JavaBeanDescriptor) {
                    JavaBeanDescriptor valueDescriptor = (JavaBeanDescriptor)entry.getValue();
                    value = JavaBeanSerializeUtil.instantiateForDeserialize(valueDescriptor, loader, cache);
                    JavaBeanSerializeUtil.deserializeInternal(value, valueDescriptor, loader, cache);
                }
                map.put(key, value);
            }
        } else if (beanDescriptor.isBeanType()) {
            for (Map.Entry<Object, Object> entry : beanDescriptor) {
                String property = entry.getKey().toString();
                Object value = entry.getValue();
                if (value == null) continue;
                if (value instanceof JavaBeanDescriptor) {
                    JavaBeanDescriptor valueDescriptor = (JavaBeanDescriptor)entry.getValue();
                    value = JavaBeanSerializeUtil.instantiateForDeserialize(valueDescriptor, loader, cache);
                    JavaBeanSerializeUtil.deserializeInternal(value, valueDescriptor, loader, cache);
                }
                Method method = JavaBeanSerializeUtil.getSetterMethod(result.getClass(), property, value.getClass());
                boolean setByMethod = false;
                try {
                    if (method != null) {
                        method.invoke(result, value);
                        setByMethod = true;
                    }
                }
                catch (Exception e) {
                    LogHelper.warn(logger, "Failed to set property through method " + method, e);
                }
                if (setByMethod) continue;
                try {
                    Field field = result.getClass().getField(property);
                    if (field == null) continue;
                    field.set(result, value);
                }
                catch (NoSuchFieldException e1) {
                    LogHelper.warn(logger, "Failed to set field value", e1);
                }
                catch (IllegalAccessException e1) {
                    LogHelper.warn(logger, "Failed to set field value", e1);
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported type " + beanDescriptor.getClassName() + ":" + beanDescriptor.getType());
        }
    }

    private static Method getSetterMethod(Class<?> cls, String property, Class<?> valueCls) {
        String name = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
        Method method = null;
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
            method.setAccessible(true);
        }
        return method;
    }

    private static Object instantiate(Class<?> cl) throws Exception {
        Constructor<?>[] constructors = cl.getDeclaredConstructors();
        Constructor<?> constructor = null;
        int argc = Integer.MAX_VALUE;
        for (Constructor<?> c : constructors) {
            if (c.getParameterTypes().length >= argc) continue;
            argc = c.getParameterTypes().length;
            constructor = c;
        }
        if (constructor != null) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] constructorArgs = new Object[paramTypes.length];
            for (int i = 0; i < constructorArgs.length; ++i) {
                constructorArgs[i] = JavaBeanSerializeUtil.getConstructorArg(paramTypes[i]);
            }
            try {
                constructor.setAccessible(true);
                return constructor.newInstance(constructorArgs);
            }
            catch (InstantiationException e) {
                LogHelper.warn(logger, e.getMessage(), e);
            }
            catch (IllegalAccessException e) {
                LogHelper.warn(logger, e.getMessage(), e);
            }
            catch (InvocationTargetException e) {
                LogHelper.warn(logger, e.getMessage(), e);
            }
        }
        return cl.newInstance();
    }

    private static Object getConstructorArg(Class<?> cl) {
        if (Boolean.TYPE.equals(cl) || Boolean.class.equals(cl)) {
            return Boolean.FALSE;
        }
        if (Byte.TYPE.equals(cl) || Byte.class.equals(cl)) {
            return (byte)0;
        }
        if (Short.TYPE.equals(cl) || Short.class.equals(cl)) {
            return (short)0;
        }
        if (Integer.TYPE.equals(cl) || Integer.class.equals(cl)) {
            return 0;
        }
        if (Long.TYPE.equals(cl) || Long.class.equals(cl)) {
            return 0L;
        }
        if (Float.TYPE.equals(cl) || Float.class.equals(cl)) {
            return Float.valueOf(0.0f);
        }
        if (Double.TYPE.equals(cl) || Double.class.equals(cl)) {
            return 0.0;
        }
        if (Character.TYPE.equals(cl) || Character.class.equals(cl)) {
            return new Character('\u0000');
        }
        return null;
    }

    private static Object instantiateForDeserialize(JavaBeanDescriptor beanDescriptor, ClassLoader loader, IdentityHashMap<JavaBeanDescriptor, Object> cache) {
        if (cache.containsKey(beanDescriptor)) {
            return cache.get(beanDescriptor);
        }
        Object result = null;
        if (beanDescriptor.isClassType()) {
            try {
                result = JavaBeanSerializeUtil.name2Class(loader, beanDescriptor.getClassNameProperty());
                return result;
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        if (beanDescriptor.isEnumType()) {
            try {
                Class<?> enumType = JavaBeanSerializeUtil.name2Class(loader, beanDescriptor.getClassName());
                Method method = JavaBeanSerializeUtil.getEnumValueOfMethod(enumType);
                result = method.invoke(null, enumType, beanDescriptor.getEnumPropertyName());
                return result;
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        if (beanDescriptor.isPrimitiveType()) {
            result = beanDescriptor.getPrimitiveProperty();
            return result;
        }
        if (beanDescriptor.isArrayType()) {
            Class<?> componentType;
            try {
                componentType = JavaBeanSerializeUtil.name2Class(loader, beanDescriptor.getClassName());
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            result = Array.newInstance(componentType, beanDescriptor.propertySize());
            cache.put(beanDescriptor, result);
        } else {
            try {
                Class<?> cl = JavaBeanSerializeUtil.name2Class(loader, beanDescriptor.getClassName());
                result = JavaBeanSerializeUtil.instantiate(cl);
                cache.put(beanDescriptor, result);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return result;
    }

    public static Class<?> name2Class(ClassLoader loader, String name) throws ClassNotFoundException {
        if (TYPES.containsKey(name)) {
            return TYPES.get(name);
        }
        if (JavaBeanSerializeUtil.isArray(name)) {
            int dimension = 0;
            while (JavaBeanSerializeUtil.isArray(name)) {
                ++dimension;
                name = name.substring(1);
            }
            Class<?> type = JavaBeanSerializeUtil.name2Class(loader, name);
            int[] dimensions = new int[dimension];
            for (int i = 0; i < dimension; ++i) {
                dimensions[i] = 0;
            }
            return Array.newInstance(type, dimensions).getClass();
        }
        if (JavaBeanSerializeUtil.isReferenceType(name)) {
            name = name.substring(1, name.length() - 1);
        }
        return Class.forName(name, false, loader);
    }

    private static boolean isArray(String type) {
        return type != null && type.startsWith(ARRAY_PREFIX);
    }

    private static boolean isReferenceType(String type) {
        return type != null && type.startsWith(REFERENCE_TYPE_PREFIX) && type.endsWith(REFERENCE_TYPE_SUFFIX);
    }

    private static Method getEnumValueOfMethod(Class cl) throws NoSuchMethodException {
        return cl.getMethod("valueOf", Class.class, String.class);
    }

    private JavaBeanSerializeUtil() {
    }

    static {
        TYPES.put(Boolean.TYPE.getName(), Boolean.TYPE);
        TYPES.put(Byte.TYPE.getName(), Byte.TYPE);
        TYPES.put(Short.TYPE.getName(), Short.TYPE);
        TYPES.put(Integer.TYPE.getName(), Integer.TYPE);
        TYPES.put(Long.TYPE.getName(), Long.TYPE);
        TYPES.put(Float.TYPE.getName(), Float.TYPE);
        TYPES.put(Double.TYPE.getName(), Double.TYPE);
        TYPES.put(Void.TYPE.getName(), Void.TYPE);
        TYPES.put("Z", Boolean.TYPE);
        TYPES.put("B", Byte.TYPE);
        TYPES.put("C", Character.TYPE);
        TYPES.put("D", Double.TYPE);
        TYPES.put("F", Float.TYPE);
        TYPES.put("I", Integer.TYPE);
        TYPES.put("J", Long.TYPE);
        TYPES.put("S", Short.TYPE);
    }
}

