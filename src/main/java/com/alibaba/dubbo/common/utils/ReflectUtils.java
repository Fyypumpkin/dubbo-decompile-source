/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javassist.CtClass
 *  javassist.CtConstructor
 *  javassist.CtMethod
 *  javassist.NotFoundException
 */
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.common.utils.StringUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

public final class ReflectUtils {
    public static final char JVM_VOID = 'V';
    public static final char JVM_BOOLEAN = 'Z';
    public static final char JVM_BYTE = 'B';
    public static final char JVM_CHAR = 'C';
    public static final char JVM_DOUBLE = 'D';
    public static final char JVM_FLOAT = 'F';
    public static final char JVM_INT = 'I';
    public static final char JVM_LONG = 'J';
    public static final char JVM_SHORT = 'S';
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    public static final String JAVA_IDENT_REGEX = "(?:[_$a-zA-Z][_$a-zA-Z0-9]*)";
    public static final String JAVA_NAME_REGEX = "(?:(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\.(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*)";
    public static final String CLASS_DESC = "(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)";
    public static final String ARRAY_DESC = "(?:\\[+(?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)))";
    public static final String DESC_REGEX = "(?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)|(?:\\[+(?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;))))";
    public static final Pattern DESC_PATTERN = Pattern.compile("(?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)|(?:\\[+(?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;))))");
    public static final String METHOD_DESC_REGEX = "(?:((?:[_$a-zA-Z][_$a-zA-Z0-9]*))?\\(((?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)|(?:\\[+(?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;))))*)\\)((?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)|(?:\\[+(?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)))))?)";
    public static final Pattern METHOD_DESC_PATTERN = Pattern.compile("(?:((?:[_$a-zA-Z][_$a-zA-Z0-9]*))?\\(((?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)|(?:\\[+(?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;))))*)\\)((?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)|(?:\\[+(?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)))))?)");
    public static final Pattern GETTER_METHOD_DESC_PATTERN = Pattern.compile("get([A-Z][_a-zA-Z0-9]*)\\(\\)((?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)|(?:\\[+(?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)))))");
    public static final Pattern SETTER_METHOD_DESC_PATTERN = Pattern.compile("set([A-Z][_a-zA-Z0-9]*)\\(((?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)|(?:\\[+(?:(?:[VZBCDFIJS])|(?:L(?:[_$a-zA-Z][_$a-zA-Z0-9]*)(?:\\/(?:[_$a-zA-Z][_$a-zA-Z0-9]*))*;)))))\\)V");
    public static final Pattern IS_HAS_CAN_METHOD_DESC_PATTERN = Pattern.compile("(?:is|has|can)([A-Z][_a-zA-Z0-9]*)\\(\\)Z");
    private static final ConcurrentMap<String, Class<?>> DESC_CLASS_CACHE = new ConcurrentHashMap();
    private static final ConcurrentMap<String, Class<?>> NAME_CLASS_CACHE = new ConcurrentHashMap();
    private static final ConcurrentMap<String, Method> Signature_METHODS_CACHE = new ConcurrentHashMap<String, Method>();

    public static boolean isPrimitives(Class<?> cls) {
        if (cls.isArray()) {
            return ReflectUtils.isPrimitive(cls.getComponentType());
        }
        return ReflectUtils.isPrimitive(cls);
    }

    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class || cls == Character.class || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }

    public static Class<?> getBoxedClass(Class<?> c) {
        if (c == Integer.TYPE) {
            c = Integer.class;
        } else if (c == Boolean.TYPE) {
            c = Boolean.class;
        } else if (c == Long.TYPE) {
            c = Long.class;
        } else if (c == Float.TYPE) {
            c = Float.class;
        } else if (c == Double.TYPE) {
            c = Double.class;
        } else if (c == Character.TYPE) {
            c = Character.class;
        } else if (c == Byte.TYPE) {
            c = Byte.class;
        } else if (c == Short.TYPE) {
            c = Short.class;
        }
        return c;
    }

    public static boolean isCompatible(Class<?> c, Object o) {
        boolean pt = c.isPrimitive();
        if (o == null) {
            return !pt;
        }
        if (pt) {
            if (c == Integer.TYPE) {
                c = Integer.class;
            } else if (c == Boolean.TYPE) {
                c = Boolean.class;
            } else if (c == Long.TYPE) {
                c = Long.class;
            } else if (c == Float.TYPE) {
                c = Float.class;
            } else if (c == Double.TYPE) {
                c = Double.class;
            } else if (c == Character.TYPE) {
                c = Character.class;
            } else if (c == Byte.TYPE) {
                c = Byte.class;
            } else if (c == Short.TYPE) {
                c = Short.class;
            }
        }
        if (c == o.getClass()) {
            return true;
        }
        return c.isInstance(o);
    }

    public static boolean isCompatible(Class<?>[] cs, Object[] os) {
        int len = cs.length;
        if (len != os.length) {
            return false;
        }
        if (len == 0) {
            return true;
        }
        for (int i = 0; i < len; ++i) {
            if (ReflectUtils.isCompatible(cs[i], os[i])) continue;
            return false;
        }
        return true;
    }

    public static String getCodeBase(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        ProtectionDomain domain = cls.getProtectionDomain();
        if (domain == null) {
            return null;
        }
        CodeSource source = domain.getCodeSource();
        if (source == null) {
            return null;
        }
        URL location = source.getLocation();
        if (location == null) {
            return null;
        }
        return location.getFile();
    }

    public static String getName(Class<?> c) {
        if (c.isArray()) {
            StringBuilder sb = new StringBuilder();
            do {
                sb.append("[]");
            } while ((c = c.getComponentType()).isArray());
            return c.getName() + sb.toString();
        }
        return c.getName();
    }

    public static Class<?> getGenericClass(Class<?> cls) {
        return ReflectUtils.getGenericClass(cls, 0);
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
            if (((Class)genericClass).isArray()) {
                return ((Class)genericClass).getComponentType();
            }
            return (Class)genericClass;
        }
        catch (Throwable e) {
            throw new IllegalArgumentException(cls.getName() + " generic type undefined!", e);
        }
    }

    public static String getName(Method m) {
        StringBuilder ret = new StringBuilder();
        ret.append(ReflectUtils.getName(m.getReturnType())).append(' ');
        ret.append(m.getName()).append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (i > 0) {
                ret.append(',');
            }
            ret.append(ReflectUtils.getName(parameterTypes[i]));
        }
        ret.append(')');
        return ret.toString();
    }

    public static String getSignature(String methodName, Class<?>[] parameterTypes) {
        StringBuilder sb = new StringBuilder(methodName);
        sb.append("(");
        if (parameterTypes != null && parameterTypes.length > 0) {
            boolean first = true;
            for (Class<?> type : parameterTypes) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(type.getName());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String getName(Constructor<?> c) {
        StringBuilder ret = new StringBuilder("(");
        Class<?>[] parameterTypes = c.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (i > 0) {
                ret.append(',');
            }
            ret.append(ReflectUtils.getName(parameterTypes[i]));
        }
        ret.append(')');
        return ret.toString();
    }

    public static String getDesc(Class<?> c) {
        StringBuilder ret = new StringBuilder();
        while (c.isArray()) {
            ret.append('[');
            c = c.getComponentType();
        }
        if (c.isPrimitive()) {
            String t = c.getName();
            if ("void".equals(t)) {
                ret.append('V');
            } else if ("boolean".equals(t)) {
                ret.append('Z');
            } else if ("byte".equals(t)) {
                ret.append('B');
            } else if ("char".equals(t)) {
                ret.append('C');
            } else if ("double".equals(t)) {
                ret.append('D');
            } else if ("float".equals(t)) {
                ret.append('F');
            } else if ("int".equals(t)) {
                ret.append('I');
            } else if ("long".equals(t)) {
                ret.append('J');
            } else if ("short".equals(t)) {
                ret.append('S');
            }
        } else {
            ret.append('L');
            ret.append(c.getName().replace('.', '/'));
            ret.append(';');
        }
        return ret.toString();
    }

    public static String getDesc(Class<?>[] cs) {
        if (cs.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(64);
        for (Class<?> c : cs) {
            sb.append(ReflectUtils.getDesc(c));
        }
        return sb.toString();
    }

    public static String getDesc(Method m) {
        StringBuilder ret = new StringBuilder(m.getName()).append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            ret.append(ReflectUtils.getDesc(parameterTypes[i]));
        }
        ret.append(')').append(ReflectUtils.getDesc(m.getReturnType()));
        return ret.toString();
    }

    public static String getDesc(Constructor<?> c) {
        StringBuilder ret = new StringBuilder("(");
        Class<?>[] parameterTypes = c.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            ret.append(ReflectUtils.getDesc(parameterTypes[i]));
        }
        ret.append(')').append('V');
        return ret.toString();
    }

    public static String getDescWithoutMethodName(Method m) {
        StringBuilder ret = new StringBuilder();
        ret.append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            ret.append(ReflectUtils.getDesc(parameterTypes[i]));
        }
        ret.append(')').append(ReflectUtils.getDesc(m.getReturnType()));
        return ret.toString();
    }

    public static String getDesc(CtClass c) throws NotFoundException {
        StringBuilder ret = new StringBuilder();
        if (c.isArray()) {
            ret.append('[');
            ret.append(ReflectUtils.getDesc(c.getComponentType()));
        } else if (c.isPrimitive()) {
            String t = c.getName();
            if ("void".equals(t)) {
                ret.append('V');
            } else if ("boolean".equals(t)) {
                ret.append('Z');
            } else if ("byte".equals(t)) {
                ret.append('B');
            } else if ("char".equals(t)) {
                ret.append('C');
            } else if ("double".equals(t)) {
                ret.append('D');
            } else if ("float".equals(t)) {
                ret.append('F');
            } else if ("int".equals(t)) {
                ret.append('I');
            } else if ("long".equals(t)) {
                ret.append('J');
            } else if ("short".equals(t)) {
                ret.append('S');
            }
        } else {
            ret.append('L');
            ret.append(c.getName().replace('.', '/'));
            ret.append(';');
        }
        return ret.toString();
    }

    public static String getDesc(CtMethod m) throws NotFoundException {
        StringBuilder ret = new StringBuilder(m.getName()).append('(');
        CtClass[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            ret.append(ReflectUtils.getDesc(parameterTypes[i]));
        }
        ret.append(')').append(ReflectUtils.getDesc(m.getReturnType()));
        return ret.toString();
    }

    public static String getDesc(CtConstructor c) throws NotFoundException {
        StringBuilder ret = new StringBuilder("(");
        CtClass[] parameterTypes = c.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            ret.append(ReflectUtils.getDesc(parameterTypes[i]));
        }
        ret.append(')').append('V');
        return ret.toString();
    }

    public static String getDescWithoutMethodName(CtMethod m) throws NotFoundException {
        StringBuilder ret = new StringBuilder();
        ret.append('(');
        CtClass[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            ret.append(ReflectUtils.getDesc(parameterTypes[i]));
        }
        ret.append(')').append(ReflectUtils.getDesc(m.getReturnType()));
        return ret.toString();
    }

    public static String name2desc(String name) {
        StringBuilder sb = new StringBuilder();
        int c = 0;
        int index = name.indexOf(91);
        if (index > 0) {
            c = (name.length() - index) / 2;
            name = name.substring(0, index);
        }
        while (c-- > 0) {
            sb.append("[");
        }
        if ("void".equals(name)) {
            sb.append('V');
        } else if ("boolean".equals(name)) {
            sb.append('Z');
        } else if ("byte".equals(name)) {
            sb.append('B');
        } else if ("char".equals(name)) {
            sb.append('C');
        } else if ("double".equals(name)) {
            sb.append('D');
        } else if ("float".equals(name)) {
            sb.append('F');
        } else if ("int".equals(name)) {
            sb.append('I');
        } else if ("long".equals(name)) {
            sb.append('J');
        } else if ("short".equals(name)) {
            sb.append('S');
        } else {
            sb.append('L').append(name.replace('.', '/')).append(';');
        }
        return sb.toString();
    }

    public static String desc2name(String desc) {
        int c;
        StringBuilder sb;
        block13 : {
            block12 : {
                sb = new StringBuilder();
                c = desc.lastIndexOf(91) + 1;
                if (desc.length() != c + 1) break block12;
                switch (desc.charAt(c)) {
                    case 'V': {
                        sb.append("void");
                        break block13;
                    }
                    case 'Z': {
                        sb.append("boolean");
                        break block13;
                    }
                    case 'B': {
                        sb.append("byte");
                        break block13;
                    }
                    case 'C': {
                        sb.append("char");
                        break block13;
                    }
                    case 'D': {
                        sb.append("double");
                        break block13;
                    }
                    case 'F': {
                        sb.append("float");
                        break block13;
                    }
                    case 'I': {
                        sb.append("int");
                        break block13;
                    }
                    case 'J': {
                        sb.append("long");
                        break block13;
                    }
                    case 'S': {
                        sb.append("short");
                        break block13;
                    }
                    default: {
                        throw new RuntimeException();
                    }
                }
            }
            sb.append(desc.substring(c + 1, desc.length() - 1).replace('/', '.'));
        }
        while (c-- > 0) {
            sb.append("[]");
        }
        return sb.toString();
    }

    public static Class<?> forName(String name) {
        try {
            return ReflectUtils.name2class(name);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException("Not found class " + name + ", cause: " + e.getMessage(), e);
        }
    }

    public static Class<?> name2class(String name) throws ClassNotFoundException {
        return ReflectUtils.name2class(ClassHelper.getClassLoader(), name);
    }

    private static Class<?> name2class(ClassLoader cl, String name) throws ClassNotFoundException {
        Class<?> clazz;
        int c = 0;
        int index = name.indexOf(91);
        if (index > 0) {
            c = (name.length() - index) / 2;
            name = name.substring(0, index);
        }
        if (c > 0) {
            StringBuilder sb = new StringBuilder();
            while (c-- > 0) {
                sb.append("[");
            }
            if ("void".equals(name)) {
                sb.append('V');
            } else if ("boolean".equals(name)) {
                sb.append('Z');
            } else if ("byte".equals(name)) {
                sb.append('B');
            } else if ("char".equals(name)) {
                sb.append('C');
            } else if ("double".equals(name)) {
                sb.append('D');
            } else if ("float".equals(name)) {
                sb.append('F');
            } else if ("int".equals(name)) {
                sb.append('I');
            } else if ("long".equals(name)) {
                sb.append('J');
            } else if ("short".equals(name)) {
                sb.append('S');
            } else {
                sb.append('L').append(name).append(';');
            }
            name = sb.toString();
        } else {
            if ("void".equals(name)) {
                return Void.TYPE;
            }
            if ("boolean".equals(name)) {
                return Boolean.TYPE;
            }
            if ("byte".equals(name)) {
                return Byte.TYPE;
            }
            if ("char".equals(name)) {
                return Character.TYPE;
            }
            if ("double".equals(name)) {
                return Double.TYPE;
            }
            if ("float".equals(name)) {
                return Float.TYPE;
            }
            if ("int".equals(name)) {
                return Integer.TYPE;
            }
            if ("long".equals(name)) {
                return Long.TYPE;
            }
            if ("short".equals(name)) {
                return Short.TYPE;
            }
        }
        if (cl == null) {
            cl = ClassHelper.getClassLoader();
        }
        if ((clazz = (Class<?>)NAME_CLASS_CACHE.get(name)) == null) {
            clazz = Class.forName(name, true, cl);
            NAME_CLASS_CACHE.put(name, clazz);
        }
        return clazz;
    }

    public static Class<?> desc2class(String desc) throws ClassNotFoundException {
        return ReflectUtils.desc2class(ClassHelper.getClassLoader(), desc);
    }

    private static Class<?> desc2class(ClassLoader cl, String desc) throws ClassNotFoundException {
        Class<?> clazz;
        switch (desc.charAt(0)) {
            case 'V': {
                return Void.TYPE;
            }
            case 'Z': {
                return Boolean.TYPE;
            }
            case 'B': {
                return Byte.TYPE;
            }
            case 'C': {
                return Character.TYPE;
            }
            case 'D': {
                return Double.TYPE;
            }
            case 'F': {
                return Float.TYPE;
            }
            case 'I': {
                return Integer.TYPE;
            }
            case 'J': {
                return Long.TYPE;
            }
            case 'S': {
                return Short.TYPE;
            }
            case 'L': {
                desc = desc.substring(1, desc.length() - 1).replace('/', '.');
                break;
            }
            case '[': {
                desc = desc.replace('/', '.');
                break;
            }
            default: {
                throw new ClassNotFoundException("Class not found: " + desc);
            }
        }
        if (cl == null) {
            cl = ClassHelper.getClassLoader();
        }
        if ((clazz = (Class<?>)DESC_CLASS_CACHE.get(desc)) == null) {
            clazz = Class.forName(desc, true, cl);
            DESC_CLASS_CACHE.put(desc, clazz);
        }
        return clazz;
    }

    public static Class<?>[] desc2classArray(String desc) throws ClassNotFoundException {
        Class<?>[] ret = ReflectUtils.desc2classArray(ClassHelper.getClassLoader(), desc);
        return ret;
    }

    private static Class<?>[] desc2classArray(ClassLoader cl, String desc) throws ClassNotFoundException {
        if (desc.length() == 0) {
            return EMPTY_CLASS_ARRAY;
        }
        ArrayList cs = new ArrayList();
        Matcher m = DESC_PATTERN.matcher(desc);
        while (m.find()) {
            cs.add(ReflectUtils.desc2class(cl, m.group()));
        }
        return cs.toArray(EMPTY_CLASS_ARRAY);
    }

    public static Method findMethodByMethodSignature(Class<?> clazz, String methodName, String[] parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        Method method;
        String signature = methodName;
        if (parameterTypes != null && parameterTypes.length > 0) {
            signature = methodName + StringUtils.join(parameterTypes);
        }
        if ((method = (Method)Signature_METHODS_CACHE.get(signature)) != null) {
            return method;
        }
        if (parameterTypes == null) {
            ArrayList<Method> finded = new ArrayList<Method>();
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equals(methodName)) continue;
                finded.add(m);
            }
            if (finded.isEmpty()) {
                throw new NoSuchMethodException("No such method " + methodName + " in class " + clazz);
            }
            if (finded.size() > 1) {
                String msg = String.format("Not unique method for method name(%s) in class(%s), find %d methods.", methodName, clazz.getName(), finded.size());
                throw new IllegalStateException(msg);
            }
            method = (Method)finded.get(0);
        } else {
            Class[] types = new Class[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; ++i) {
                types[i] = ReflectUtils.name2class(parameterTypes[i]);
            }
            method = clazz.getMethod(methodName, types);
        }
        Signature_METHODS_CACHE.put(signature, method);
        return method;
    }

    public static Method findMethodByMethodSignature(Class<?> clazz, String methodName, String[] parameterTypes, int parameterLength) throws NoSuchMethodException, ClassNotFoundException {
        Method method;
        String signature = clazz.getName() + "." + methodName;
        if (parameterTypes != null && parameterTypes.length > 0) {
            signature = signature + StringUtils.join(parameterTypes);
        }
        if ((method = (Method)Signature_METHODS_CACHE.get(signature)) != null) {
            return method;
        }
        if (parameterTypes == null) {
            ArrayList<Method> finded = new ArrayList<Method>();
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equals(methodName)) continue;
                finded.add(m);
            }
            if (finded.isEmpty()) {
                throw new NoSuchMethodException("No such method " + methodName + " in class " + clazz);
            }
            if (finded.size() > 1) {
                String msg;
                int sameParameterLengthMethodCount = 0;
                for (Method fm : finded) {
                    if (fm.getParameterTypes().length != parameterLength) continue;
                    ++sameParameterLengthMethodCount;
                    method = fm;
                }
                if (sameParameterLengthMethodCount == 0) {
                    msg = String.format("Not matching method for method name(%s) in class(%s), expected parameter length %d.", methodName, clazz.getName(), parameterLength);
                    throw new IllegalStateException(msg);
                }
                if (sameParameterLengthMethodCount == 1) {
                    return method;
                }
                msg = String.format("Not unique method for method name(%s) in class(%s), find %d methods, expected parameter length %d.", methodName, clazz.getName(), finded.size(), parameterLength);
                throw new IllegalStateException(msg);
            }
            method = (Method)finded.get(0);
        } else {
            Class[] types = new Class[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; ++i) {
                types[i] = ReflectUtils.name2class(parameterTypes[i]);
            }
            method = clazz.getMethod(methodName, types);
        }
        Signature_METHODS_CACHE.put(signature, method);
        return method;
    }

    public static Method findMethodByMethodName(Class<?> clazz, String methodName) throws NoSuchMethodException, ClassNotFoundException {
        return ReflectUtils.findMethodByMethodSignature(clazz, methodName, null);
    }

    public static Constructor<?> findConstructor(Class<?> clazz, Class<?> paramType) throws NoSuchMethodException {
        Constructor<?> targetConstructor;
        block3 : {
            try {
                targetConstructor = clazz.getConstructor(paramType);
            }
            catch (NoSuchMethodException e) {
                Constructor<?>[] constructors;
                targetConstructor = null;
                for (Constructor<?> constructor : constructors = clazz.getConstructors()) {
                    if (!Modifier.isPublic(constructor.getModifiers()) || constructor.getParameterTypes().length != 1 || !constructor.getParameterTypes()[0].isAssignableFrom(paramType)) continue;
                    targetConstructor = constructor;
                    break;
                }
                if (targetConstructor != null) break block3;
                throw e;
            }
        }
        return targetConstructor;
    }

    public static boolean isInstance(Object obj, String interfaceClazzName) {
        for (Class<?> clazz = obj.getClass(); clazz != null && !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
            Class<?>[] interfaces;
            for (Class<?> itf : interfaces = clazz.getInterfaces()) {
                if (!itf.getName().equals(interfaceClazzName)) continue;
                return true;
            }
        }
        return false;
    }

    public static Object getEmptyObject(Class<?> returnType) {
        return ReflectUtils.getEmptyObject(returnType, new HashMap(), 0);
    }

    private static Object getEmptyObject(Class<?> returnType, Map<Class<?>, Object> emptyInstances, int level) {
        if (level > 2) {
            return null;
        }
        if (returnType == null) {
            return null;
        }
        if (returnType == Boolean.TYPE || returnType == Boolean.class) {
            return false;
        }
        if (returnType == Character.TYPE || returnType == Character.class) {
            return Character.valueOf('\u0000');
        }
        if (returnType == Byte.TYPE || returnType == Byte.class) {
            return (byte)0;
        }
        if (returnType == Short.TYPE || returnType == Short.class) {
            return (short)0;
        }
        if (returnType == Integer.TYPE || returnType == Integer.class) {
            return 0;
        }
        if (returnType == Long.TYPE || returnType == Long.class) {
            return 0L;
        }
        if (returnType == Float.TYPE || returnType == Float.class) {
            return Float.valueOf(0.0f);
        }
        if (returnType == Double.TYPE || returnType == Double.class) {
            return 0.0;
        }
        if (returnType.isArray()) {
            return Array.newInstance(returnType.getComponentType(), 0);
        }
        if (returnType.isAssignableFrom(ArrayList.class)) {
            return new ArrayList(0);
        }
        if (returnType.isAssignableFrom(HashSet.class)) {
            return new HashSet(0);
        }
        if (returnType.isAssignableFrom(HashMap.class)) {
            return new HashMap(0);
        }
        if (String.class.equals(returnType)) {
            return "";
        }
        if (!returnType.isInterface()) {
            try {
                Object value = emptyInstances.get(returnType);
                if (value == null) {
                    value = returnType.newInstance();
                    emptyInstances.put(returnType, value);
                }
                for (Class<?> cls = value.getClass(); cls != null && cls != Object.class; cls = cls.getSuperclass()) {
                    Field[] fields;
                    for (Field field : fields = cls.getDeclaredFields()) {
                        Object property = ReflectUtils.getEmptyObject(field.getType(), emptyInstances, level + 1);
                        if (property == null) continue;
                        try {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            field.set(value, property);
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    }
                }
                return value;
            }
            catch (Throwable e) {
                return null;
            }
        }
        return null;
    }

    public static boolean isBeanPropertyReadMethod(Method method) {
        return method != null && Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()) && method.getReturnType() != Void.TYPE && method.getDeclaringClass() != Object.class && method.getParameterTypes().length == 0 && (method.getName().startsWith("get") && method.getName().length() > 3 || method.getName().startsWith("is") && method.getName().length() > 2);
    }

    public static String getPropertyNameFromBeanReadMethod(Method method) {
        if (ReflectUtils.isBeanPropertyReadMethod(method)) {
            if (method.getName().startsWith("get")) {
                return method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
            }
            if (method.getName().startsWith("is")) {
                return method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3);
            }
        }
        return null;
    }

    public static boolean isBeanPropertyWriteMethod(Method method) {
        return method != null && Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass() != Object.class && method.getParameterTypes().length == 1 && method.getName().startsWith("set") && method.getName().length() > 3;
    }

    public static String getPropertyNameFromBeanWriteMethod(Method method) {
        if (ReflectUtils.isBeanPropertyWriteMethod(method)) {
            return method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
        }
        return null;
    }

    public static boolean isPublicInstanceField(Field field) {
        return Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && !field.isSynthetic();
    }

    public static Map<String, Field> getBeanPropertyFields(Class cl) {
        HashMap<String, Field> properties = new HashMap<String, Field>();
        while (cl != null) {
            Field[] fields;
            for (Field field : fields = cl.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                properties.put(field.getName(), field);
            }
            cl = cl.getSuperclass();
        }
        return properties;
    }

    public static Map<String, Method> getBeanPropertyReadMethods(Class cl) {
        HashMap<String, Method> properties = new HashMap<String, Method>();
        while (cl != null) {
            Method[] methods;
            for (Method method : methods = cl.getDeclaredMethods()) {
                if (!ReflectUtils.isBeanPropertyReadMethod(method)) continue;
                method.setAccessible(true);
                String property = ReflectUtils.getPropertyNameFromBeanReadMethod(method);
                properties.put(property, method);
            }
            cl = cl.getSuperclass();
        }
        return properties;
    }

    private ReflectUtils() {
    }
}

