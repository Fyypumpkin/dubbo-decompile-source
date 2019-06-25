/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.bytecode;

import com.alibaba.dubbo.common.bytecode.ClassGenerator;
import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Mixin {
    private static AtomicLong MIXIN_CLASS_COUNTER = new AtomicLong(0L);
    private static final String PACKAGE_NAME = Mixin.class.getPackage().getName();

    public static Mixin mixin(Class<?>[] ics, Class<?> dc) {
        return Mixin.mixin(ics, new Class[]{dc});
    }

    public static Mixin mixin(Class<?>[] ics, Class<?> dc, ClassLoader cl) {
        return Mixin.mixin(ics, new Class[]{dc}, cl);
    }

    public static Mixin mixin(Class<?>[] ics, Class<?>[] dcs) {
        return Mixin.mixin(ics, dcs, ClassHelper.getCallerClassLoader(Mixin.class));
    }

    public static Mixin mixin(Class<?>[] ics, Class<?>[] dcs, ClassLoader cl) {
        Mixin.assertInterfaceArray(ics);
        long id = MIXIN_CLASS_COUNTER.getAndIncrement();
        String pkg = null;
        ClassGenerator ccp = null;
        ClassGenerator ccm = null;
        try {
            ccp = ClassGenerator.newInstance(cl);
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < dcs.length; ++i) {
                if (!Modifier.isPublic(dcs[i].getModifiers())) {
                    String npkg = dcs[i].getPackage().getName();
                    if (pkg == null) {
                        pkg = npkg;
                    } else if (!pkg.equals(npkg)) {
                        throw new IllegalArgumentException("non-public interfaces class from different packages");
                    }
                }
                ccp.addField("private " + dcs[i].getName() + " d" + i + ";");
                code.append("d").append(i).append(" = (").append(dcs[i].getName()).append(")$1[").append(i).append("];\n");
                if (!MixinAware.class.isAssignableFrom(dcs[i])) continue;
                code.append("d").append(i).append(".setMixinInstance(this);\n");
            }
            ccp.addConstructor(1, new Class[]{Object[].class}, code.toString());
            HashSet<String> worked = new HashSet<String>();
            for (int i = 0; i < ics.length; ++i) {
                if (!Modifier.isPublic(ics[i].getModifiers())) {
                    Method[] npkg = ics[i].getPackage().getName();
                    if (pkg == null) {
                        pkg = npkg;
                    } else if (!pkg.equals(npkg)) {
                        throw new IllegalArgumentException("non-public delegate class from different packages");
                    }
                }
                ccp.addInterface(ics[i]);
                for (Method method : ics[i].getMethods()) {
                    String desc;
                    if ("java.lang.Object".equals(method.getDeclaringClass().getName()) || worked.contains(desc = ReflectUtils.getDesc(method))) continue;
                    worked.add(desc);
                    int ix = Mixin.findMethod(dcs, desc);
                    if (ix < 0) {
                        throw new RuntimeException("Missing method [" + desc + "] implement.");
                    }
                    Class<?> rt = method.getReturnType();
                    String mn = method.getName();
                    if (Void.TYPE.equals(rt)) {
                        ccp.addMethod(mn, method.getModifiers(), rt, method.getParameterTypes(), method.getExceptionTypes(), "d" + ix + "." + mn + "($$);");
                        continue;
                    }
                    ccp.addMethod(mn, method.getModifiers(), rt, method.getParameterTypes(), method.getExceptionTypes(), "return ($r)d" + ix + "." + mn + "($$);");
                }
            }
            if (pkg == null) {
                pkg = PACKAGE_NAME;
            }
            String micn = pkg + ".mixin" + id;
            ccp.setClassName(micn);
            ccp.toClass();
            String fcn = Mixin.class.getName() + id;
            ccm = ClassGenerator.newInstance(cl);
            ccm.setClassName(fcn);
            ccm.addDefaultConstructor();
            ccm.setSuperClass(Mixin.class.getName());
            ccm.addMethod("public Object newInstance(Object[] delegates){ return new " + micn + "($1); }");
            Class<?> mixin = ccm.toClass();
            Mixin mixin2 = (Mixin)mixin.newInstance();
            return mixin2;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        finally {
            if (ccp != null) {
                ccp.release();
            }
            if (ccm != null) {
                ccm.release();
            }
        }
    }

    public abstract Object newInstance(Object[] var1);

    protected Mixin() {
    }

    private static int findMethod(Class<?>[] dcs, String desc) {
        for (int i = 0; i < dcs.length; ++i) {
            Method[] methods;
            Class<?> cl = dcs[i];
            for (Method method : methods = cl.getMethods()) {
                if (!desc.equals(ReflectUtils.getDesc(method))) continue;
                return i;
            }
        }
        return -1;
    }

    private static void assertInterfaceArray(Class<?>[] ics) {
        for (int i = 0; i < ics.length; ++i) {
            if (ics[i].isInterface()) continue;
            throw new RuntimeException("Class " + ics[i].getName() + " is not a interface.");
        }
    }

    public static interface MixinAware {
        public void setMixinInstance(Object var1);
    }

}

