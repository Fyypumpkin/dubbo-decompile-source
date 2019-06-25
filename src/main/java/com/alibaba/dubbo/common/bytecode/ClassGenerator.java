/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javassist.CannotCompileException
 *  javassist.ClassPath
 *  javassist.ClassPool
 *  javassist.CtClass
 *  javassist.CtConstructor
 *  javassist.CtField
 *  javassist.CtMethod
 *  javassist.CtNewConstructor
 *  javassist.CtNewMethod
 *  javassist.LoaderClassPath
 *  javassist.Modifier
 *  javassist.NotFoundException
 */
package com.alibaba.dubbo.common.bytecode;

import com.alibaba.dubbo.common.utils.ReflectUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

public final class ClassGenerator {
    private static final AtomicLong CLASS_NAME_COUNTER = new AtomicLong(0L);
    private static final String SIMPLE_NAME_TAG = "<init>";
    private static final Map<ClassLoader, ClassPool> POOL_MAP = new ConcurrentHashMap<ClassLoader, ClassPool>();
    private ClassPool mPool;
    private CtClass mCtc;
    private String mClassName;
    private String mSuperClass;
    private Set<String> mInterfaces;
    private List<String> mFields;
    private List<String> mConstructors;
    private List<String> mMethods;
    private Map<String, Method> mCopyMethods;
    private Map<String, Constructor<?>> mCopyConstructors;
    private boolean mDefaultConstructor = false;

    public static ClassGenerator newInstance() {
        return new ClassGenerator(ClassGenerator.getClassPool(Thread.currentThread().getContextClassLoader()));
    }

    public static ClassGenerator newInstance(ClassLoader loader) {
        return new ClassGenerator(ClassGenerator.getClassPool(loader));
    }

    public static boolean isDynamicClass(Class<?> cl) {
        return DC.class.isAssignableFrom(cl);
    }

    public static ClassPool getClassPool(ClassLoader loader) {
        if (loader == null) {
            return ClassPool.getDefault();
        }
        ClassPool pool = POOL_MAP.get(loader);
        if (pool == null) {
            pool = new ClassPool(true);
            pool.insertClassPath((ClassPath)new LoaderClassPath(loader));
            POOL_MAP.put(loader, pool);
        }
        return pool;
    }

    private ClassGenerator() {
    }

    private ClassGenerator(ClassPool pool) {
        this.mPool = pool;
    }

    public String getClassName() {
        return this.mClassName;
    }

    public ClassGenerator setClassName(String name) {
        this.mClassName = name;
        return this;
    }

    public ClassGenerator addInterface(String cn) {
        if (this.mInterfaces == null) {
            this.mInterfaces = new HashSet<String>();
        }
        this.mInterfaces.add(cn);
        return this;
    }

    public ClassGenerator addInterface(Class<?> cl) {
        return this.addInterface(cl.getName());
    }

    public ClassGenerator setSuperClass(String cn) {
        this.mSuperClass = cn;
        return this;
    }

    public ClassGenerator setSuperClass(Class<?> cl) {
        this.mSuperClass = cl.getName();
        return this;
    }

    public ClassGenerator addField(String code) {
        if (this.mFields == null) {
            this.mFields = new ArrayList<String>();
        }
        this.mFields.add(code);
        return this;
    }

    public ClassGenerator addField(String name, int mod, Class<?> type) {
        return this.addField(name, mod, type, null);
    }

    public ClassGenerator addField(String name, int mod, Class<?> type, String def) {
        StringBuilder sb = new StringBuilder();
        sb.append(ClassGenerator.modifier(mod)).append(' ').append(ReflectUtils.getName(type)).append(' ');
        sb.append(name);
        if (def != null && def.length() > 0) {
            sb.append('=');
            sb.append(def);
        }
        sb.append(';');
        return this.addField(sb.toString());
    }

    public ClassGenerator addMethod(String code) {
        if (this.mMethods == null) {
            this.mMethods = new ArrayList<String>();
        }
        this.mMethods.add(code);
        return this;
    }

    public ClassGenerator addMethod(String name, int mod, Class<?> rt, Class<?>[] pts, String body) {
        return this.addMethod(name, mod, rt, pts, null, body);
    }

    public ClassGenerator addMethod(String name, int mod, Class<?> rt, Class<?>[] pts, Class<?>[] ets, String body) {
        int i;
        StringBuilder sb = new StringBuilder();
        sb.append(ClassGenerator.modifier(mod)).append(' ').append(ReflectUtils.getName(rt)).append(' ').append(name);
        sb.append('(');
        for (i = 0; i < pts.length; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(ReflectUtils.getName(pts[i]));
            sb.append(" arg").append(i);
        }
        sb.append(')');
        if (ets != null && ets.length > 0) {
            sb.append(" throws ");
            for (i = 0; i < ets.length; ++i) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(ReflectUtils.getName(ets[i]));
            }
        }
        sb.append('{').append(body).append('}');
        return this.addMethod(sb.toString());
    }

    public ClassGenerator addMethod(Method m) {
        this.addMethod(m.getName(), m);
        return this;
    }

    public ClassGenerator addMethod(String name, Method m) {
        String desc = name + ReflectUtils.getDescWithoutMethodName(m);
        this.addMethod(':' + desc);
        if (this.mCopyMethods == null) {
            this.mCopyMethods = new ConcurrentHashMap<String, Method>(8);
        }
        this.mCopyMethods.put(desc, m);
        return this;
    }

    public ClassGenerator addConstructor(String code) {
        if (this.mConstructors == null) {
            this.mConstructors = new LinkedList<String>();
        }
        this.mConstructors.add(code);
        return this;
    }

    public ClassGenerator addConstructor(int mod, Class<?>[] pts, String body) {
        return this.addConstructor(mod, pts, null, body);
    }

    public ClassGenerator addConstructor(int mod, Class<?>[] pts, Class<?>[] ets, String body) {
        int i;
        StringBuilder sb = new StringBuilder();
        sb.append(ClassGenerator.modifier(mod)).append(' ').append(SIMPLE_NAME_TAG);
        sb.append('(');
        for (i = 0; i < pts.length; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(ReflectUtils.getName(pts[i]));
            sb.append(" arg").append(i);
        }
        sb.append(')');
        if (ets != null && ets.length > 0) {
            sb.append(" throws ");
            for (i = 0; i < ets.length; ++i) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(ReflectUtils.getName(ets[i]));
            }
        }
        sb.append('{').append(body).append('}');
        return this.addConstructor(sb.toString());
    }

    public ClassGenerator addConstructor(Constructor<?> c) {
        String desc = ReflectUtils.getDesc(c);
        this.addConstructor(":" + desc);
        if (this.mCopyConstructors == null) {
            this.mCopyConstructors = new ConcurrentHashMap(4);
        }
        this.mCopyConstructors.put(desc, c);
        return this;
    }

    public ClassGenerator addDefaultConstructor() {
        this.mDefaultConstructor = true;
        return this;
    }

    public ClassPool getClassPool() {
        return this.mPool;
    }

    public Class<?> toClass() {
        return this.toClass(this.getClass().getClassLoader(), this.getClass().getProtectionDomain());
    }

    public Class<?> toClass(ClassLoader loader, ProtectionDomain pd) {
        if (this.mCtc != null) {
            this.mCtc.detach();
        }
        long id = CLASS_NAME_COUNTER.getAndIncrement();
        try {
            CtClass ctcs;
            CtClass ctClass = ctcs = this.mSuperClass == null ? null : this.mPool.get(this.mSuperClass);
            if (this.mClassName == null) {
                this.mClassName = (this.mSuperClass == null || Modifier.isPublic((int)ctcs.getModifiers()) ? ClassGenerator.class.getName() : new StringBuilder().append(this.mSuperClass).append("$sc").toString()) + id;
            }
            this.mCtc = this.mPool.makeClass(this.mClassName);
            if (this.mSuperClass != null) {
                this.mCtc.setSuperclass(ctcs);
            }
            this.mCtc.addInterface(this.mPool.get(DC.class.getName()));
            if (this.mInterfaces != null) {
                for (String cl : this.mInterfaces) {
                    this.mCtc.addInterface(this.mPool.get(cl));
                }
            }
            if (this.mFields != null) {
                for (String code : this.mFields) {
                    this.mCtc.addField(CtField.make((String)code, (CtClass)this.mCtc));
                }
            }
            if (this.mMethods != null) {
                for (String code : this.mMethods) {
                    if (code.charAt(0) == ':') {
                        this.mCtc.addMethod(CtNewMethod.copy((CtMethod)this.getCtMethod(this.mCopyMethods.get(code.substring(1))), (String)code.substring(1, code.indexOf(40)), (CtClass)this.mCtc, null));
                        continue;
                    }
                    this.mCtc.addMethod(CtNewMethod.make((String)code, (CtClass)this.mCtc));
                }
            }
            if (this.mDefaultConstructor) {
                this.mCtc.addConstructor(CtNewConstructor.defaultConstructor((CtClass)this.mCtc));
            }
            if (this.mConstructors != null) {
                for (String code : this.mConstructors) {
                    if (code.charAt(0) == ':') {
                        this.mCtc.addConstructor(CtNewConstructor.copy((CtConstructor)this.getCtConstructor(this.mCopyConstructors.get(code.substring(1))), (CtClass)this.mCtc, null));
                        continue;
                    }
                    String[] sn = this.mCtc.getSimpleName().split("\\$+");
                    this.mCtc.addConstructor(CtNewConstructor.make((String)code.replaceFirst(SIMPLE_NAME_TAG, sn[sn.length - 1]), (CtClass)this.mCtc));
                }
            }
            return this.mCtc.toClass(loader, pd);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (NotFoundException e) {
            throw new RuntimeException(e.getMessage(), (Throwable)e);
        }
        catch (CannotCompileException e) {
            throw new RuntimeException(e.getMessage(), (Throwable)e);
        }
    }

    public void release() {
        if (this.mCtc != null) {
            this.mCtc.detach();
        }
        if (this.mInterfaces != null) {
            this.mInterfaces.clear();
        }
        if (this.mFields != null) {
            this.mFields.clear();
        }
        if (this.mMethods != null) {
            this.mMethods.clear();
        }
        if (this.mConstructors != null) {
            this.mConstructors.clear();
        }
        if (this.mCopyMethods != null) {
            this.mCopyMethods.clear();
        }
        if (this.mCopyConstructors != null) {
            this.mCopyConstructors.clear();
        }
    }

    private CtClass getCtClass(Class<?> c) throws NotFoundException {
        return this.mPool.get(c.getName());
    }

    private CtMethod getCtMethod(Method m) throws NotFoundException {
        return this.getCtClass(m.getDeclaringClass()).getMethod(m.getName(), ReflectUtils.getDescWithoutMethodName(m));
    }

    private CtConstructor getCtConstructor(Constructor<?> c) throws NotFoundException {
        return this.getCtClass(c.getDeclaringClass()).getConstructor(ReflectUtils.getDesc(c));
    }

    private static String modifier(int mod) {
        if (java.lang.reflect.Modifier.isPublic(mod)) {
            return "public";
        }
        if (java.lang.reflect.Modifier.isProtected(mod)) {
            return "protected";
        }
        if (java.lang.reflect.Modifier.isPrivate(mod)) {
            return "private";
        }
        return "";
    }

    public static interface DC {
    }

}

