/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.compiler.support;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClassUtils {
    public static final String CLASS_EXTENSION = ".class";
    public static final String JAVA_EXTENSION = ".java";
    private static final int JIT_LIMIT = 5120;

    public static Object newInstance(String name) {
        try {
            return ClassUtils.forName(name).newInstance();
        }
        catch (InstantiationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Class<?> forName(String[] packages, String className) {
        try {
            return ClassUtils._forName(className);
        }
        catch (ClassNotFoundException e) {
            if (packages != null && packages.length > 0) {
                for (String pkg : packages) {
                    try {
                        return ClassUtils._forName(pkg + "." + className);
                    }
                    catch (ClassNotFoundException classNotFoundException) {
                    }
                }
            }
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Class<?> forName(String className) {
        try {
            return ClassUtils._forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Class<?> _forName(String className) throws ClassNotFoundException {
        if ("boolean".equals(className)) {
            return Boolean.TYPE;
        }
        if ("byte".equals(className)) {
            return Byte.TYPE;
        }
        if ("char".equals(className)) {
            return Character.TYPE;
        }
        if ("short".equals(className)) {
            return Short.TYPE;
        }
        if ("int".equals(className)) {
            return Integer.TYPE;
        }
        if ("long".equals(className)) {
            return Long.TYPE;
        }
        if ("float".equals(className)) {
            return Float.TYPE;
        }
        if ("double".equals(className)) {
            return Double.TYPE;
        }
        if ("boolean[]".equals(className)) {
            return boolean[].class;
        }
        if ("byte[]".equals(className)) {
            return byte[].class;
        }
        if ("char[]".equals(className)) {
            return char[].class;
        }
        if ("short[]".equals(className)) {
            return short[].class;
        }
        if ("int[]".equals(className)) {
            return int[].class;
        }
        if ("long[]".equals(className)) {
            return long[].class;
        }
        if ("float[]".equals(className)) {
            return float[].class;
        }
        if ("double[]".equals(className)) {
            return double[].class;
        }
        try {
            return ClassUtils.arrayForName(className);
        }
        catch (ClassNotFoundException e) {
            if (className.indexOf(46) == -1) {
                try {
                    return ClassUtils.arrayForName("java.lang." + className);
                }
                catch (ClassNotFoundException classNotFoundException) {
                    // empty catch block
                }
            }
            throw e;
        }
    }

    private static Class<?> arrayForName(String className) throws ClassNotFoundException {
        return Class.forName(className.endsWith("[]") ? "[L" + className.substring(0, className.length() - 2) + ";" : className, true, Thread.currentThread().getContextClassLoader());
    }

    public static Class<?> getBoxedClass(Class<?> type) {
        if (type == Boolean.TYPE) {
            return Boolean.class;
        }
        if (type == Character.TYPE) {
            return Character.class;
        }
        if (type == Byte.TYPE) {
            return Byte.class;
        }
        if (type == Short.TYPE) {
            return Short.class;
        }
        if (type == Integer.TYPE) {
            return Integer.class;
        }
        if (type == Long.TYPE) {
            return Long.class;
        }
        if (type == Float.TYPE) {
            return Float.class;
        }
        if (type == Double.TYPE) {
            return Double.class;
        }
        return type;
    }

    public static Boolean boxed(boolean v) {
        return v;
    }

    public static Character boxed(char v) {
        return Character.valueOf(v);
    }

    public static Byte boxed(byte v) {
        return v;
    }

    public static Short boxed(short v) {
        return v;
    }

    public static Integer boxed(int v) {
        return v;
    }

    public static Long boxed(long v) {
        return v;
    }

    public static Float boxed(float v) {
        return Float.valueOf(v);
    }

    public static Double boxed(double v) {
        return v;
    }

    public static Object boxed(Object v) {
        return v;
    }

    public static boolean unboxed(Boolean v) {
        return v == null ? false : v;
    }

    public static char unboxed(Character v) {
        return v == null ? (char)'\u0000' : v.charValue();
    }

    public static byte unboxed(Byte v) {
        return v == null ? (byte)0 : v;
    }

    public static short unboxed(Short v) {
        return v == null ? (short)0 : v;
    }

    public static int unboxed(Integer v) {
        return v == null ? 0 : v;
    }

    public static long unboxed(Long v) {
        return v == null ? 0L : v;
    }

    public static float unboxed(Float v) {
        return v == null ? 0.0f : v.floatValue();
    }

    public static double unboxed(Double v) {
        return v == null ? 0.0 : v;
    }

    public static Object unboxed(Object v) {
        return v;
    }

    public static boolean isNotEmpty(Object object) {
        return ClassUtils.getSize(object) > 0;
    }

    public static int getSize(Object object) {
        if (object == null) {
            return 0;
        }
        if (object instanceof Collection) {
            return ((Collection)object).size();
        }
        if (object instanceof Map) {
            return ((Map)object).size();
        }
        if (object.getClass().isArray()) {
            return Array.getLength(object);
        }
        return -1;
    }

    public static URI toURI(String name) {
        try {
            return new URI(name);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getGenericClass(Class<?> cls) {
        return ClassUtils.getGenericClass(cls, 0);
    }

    public static Class<?> getGenericClass(Class<?> cls, int i) {
        try {
            ParameterizedType parameterizedType = (ParameterizedType)cls.getGenericInterfaces()[0];
            Type genericClass = parameterizedType.getActualTypeArguments()[i];
            if (genericClass instanceof ParameterizedType) {
                return (Class)((ParameterizedType)genericClass).getRawType();
            }
            if (genericClass instanceof GenericArrayType) {
                return (Class)((GenericArrayType)genericClass).getGenericComponentType();
            }
            if (genericClass != null) {
                return (Class)genericClass;
            }
        }
        catch (Throwable parameterizedType) {
            // empty catch block
        }
        if (cls.getSuperclass() != null) {
            return ClassUtils.getGenericClass(cls.getSuperclass(), i);
        }
        throw new IllegalArgumentException(cls.getName() + " generic type undefined!");
    }

    public static boolean isBeforeJava5(String javaVersion) {
        return javaVersion == null || javaVersion.length() == 0 || "1.0".equals(javaVersion) || "1.1".equals(javaVersion) || "1.2".equals(javaVersion) || "1.3".equals(javaVersion) || "1.4".equals(javaVersion);
    }

    public static boolean isBeforeJava6(String javaVersion) {
        return ClassUtils.isBeforeJava5(javaVersion) || "1.5".equals(javaVersion);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String toString(Throwable e) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName() + ": ");
        if (e.getMessage() != null) {
            p.print(e.getMessage() + "\n");
        }
        p.println();
        try {
            e.printStackTrace(p);
            String string = w.toString();
            return string;
        }
        finally {
            p.close();
        }
    }

    public static void checkBytecode(String name, byte[] bytecode) {
        if (bytecode.length > 5120) {
            System.err.println("The template bytecode too long, may be affect the JIT compiler. template class: " + name);
        }
    }

    public static String getSizeMethod(Class<?> cls) {
        try {
            return cls.getMethod("size", new Class[0]).getName() + "()";
        }
        catch (NoSuchMethodException e) {
            try {
                return cls.getMethod("length", new Class[0]).getName() + "()";
            }
            catch (NoSuchMethodException e2) {
                try {
                    return cls.getMethod("getSize", new Class[0]).getName() + "()";
                }
                catch (NoSuchMethodException e3) {
                    try {
                        return cls.getMethod("getLength", new Class[0]).getName() + "()";
                    }
                    catch (NoSuchMethodException e4) {
                        return null;
                    }
                }
            }
        }
    }

    public static String getMethodName(Method method, Class<?>[] parameterClasses, String rightCode) {
        if (method.getParameterTypes().length > parameterClasses.length) {
            Class<?>[] types = method.getParameterTypes();
            StringBuilder buf = new StringBuilder(rightCode);
            for (int i = parameterClasses.length; i < types.length; ++i) {
                Class<?> type;
                if (buf.length() > 0) {
                    buf.append(",");
                }
                String def = (type = types[i]) == Boolean.TYPE ? "false" : (type == Character.TYPE ? "'\\0'" : (type == Byte.TYPE || type == Short.TYPE || type == Integer.TYPE || type == Long.TYPE || type == Float.TYPE || type == Double.TYPE ? "0" : "null"));
                buf.append(def);
            }
        }
        return method.getName() + "(" + rightCode + ")";
    }

    public static Method searchMethod(Class<?> currentClass, String name, Class<?>[] parameterTypes) throws NoSuchMethodException {
        if (currentClass == null) {
            throw new NoSuchMethodException("class == null");
        }
        try {
            return currentClass.getMethod(name, parameterTypes);
        }
        catch (NoSuchMethodException e) {
            for (Method method : currentClass.getMethods()) {
                if (!method.getName().equals(name) || parameterTypes.length != method.getParameterTypes().length || !Modifier.isPublic(method.getModifiers())) continue;
                if (parameterTypes.length > 0) {
                    Class<?>[] types = method.getParameterTypes();
                    boolean match = true;
                    for (int i = 0; i < parameterTypes.length; ++i) {
                        if (types[i].isAssignableFrom(parameterTypes[i])) continue;
                        match = false;
                        break;
                    }
                    if (!match) continue;
                }
                return method;
            }
            throw e;
        }
    }

    public static String getInitCode(Class<?> type) {
        if (Byte.TYPE.equals(type) || Short.TYPE.equals(type) || Integer.TYPE.equals(type) || Long.TYPE.equals(type) || Float.TYPE.equals(type) || Double.TYPE.equals(type)) {
            return "0";
        }
        if (Character.TYPE.equals(type)) {
            return "'\\0'";
        }
        if (Boolean.TYPE.equals(type)) {
            return "false";
        }
        return "null";
    }

    public static <K, V> Map<K, V> toMap(Map.Entry<K, V>[] entries) {
        HashMap<K, V> map = new HashMap<K, V>();
        if (entries != null && entries.length > 0) {
            for (Map.Entry<K, V> enrty : entries) {
                map.put(enrty.getKey(), enrty.getValue());
            }
        }
        return map;
    }

    private ClassUtils() {
    }
}

