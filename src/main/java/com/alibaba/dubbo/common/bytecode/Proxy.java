/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.bytecode;

import com.alibaba.dubbo.common.bytecode.ClassGenerator;
import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Proxy {
    protected static final AtomicLong PROXY_CLASS_COUNTER = new AtomicLong(0L);
    protected static final String PACKAGE_NAME = Proxy.class.getPackage().getName();
    public static final InvocationHandler RETURN_NULL_INVOKER = new InvocationHandler(){

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return null;
        }
    };
    public static final InvocationHandler THROW_UNSUPPORTED_INVOKER = new InvocationHandler(){

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            throw new UnsupportedOperationException("Method [" + ReflectUtils.getName(method) + "] unimplemented.");
        }
    };
    protected static final Map<ClassLoader, Map<String, Object>> ProxyCacheMap = new WeakHashMap<ClassLoader, Map<String, Object>>();
    protected static final Object PendingGenerationMarker = new Object();

    public static /* varargs */ Proxy getProxy(Class<?> ... ics) {
        return Proxy.getProxy(ClassHelper.getCallerClassLoader(Proxy.class), ics);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static /* varargs */ Proxy getProxy(ClassLoader cl, Class<?> ... ics) {
        Map<String, Object> cache;
        Object tmp;
        if (ics.length > 65535) {
            throw new IllegalArgumentException("interface limit exceeded");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ics.length; ++i) {
            String itf = ics[i].getName();
            if (!ics[i].isInterface()) {
                throw new RuntimeException(itf + " is not a interface.");
            }
            tmp = null;
            try {
                tmp = Class.forName(itf, false, cl);
            }
            catch (ClassNotFoundException classNotFoundException) {
                // empty catch block
            }
            if (tmp != ics[i]) {
                throw new IllegalArgumentException(ics[i] + " is not visible from class loader");
            }
            sb.append(itf).append(';');
        }
        String key = sb.toString();
        tmp = ProxyCacheMap;
        synchronized (tmp) {
            cache = ProxyCacheMap.get(cl);
            if (cache == null) {
                cache = new HashMap<String, Object>();
                ProxyCacheMap.put(cl, cache);
            }
        }
        Proxy proxy = null;
        Map<String, Object> map = cache;
        synchronized (map) {
            do {
                Object value;
                if ((value = cache.get(key)) instanceof Reference && (proxy = (Proxy)((Reference)value).get()) != null) {
                    return proxy;
                }
                if (value != PendingGenerationMarker) break;
                try {
                    cache.wait();
                }
                catch (InterruptedException interruptedException) {}
            } while (true);
            cache.put(key, PendingGenerationMarker);
        }
        long id = PROXY_CLASS_COUNTER.getAndIncrement();
        String pkg = null;
        ClassGenerator ccp = null;
        ClassGenerator ccm = null;
        try {
            ccp = ClassGenerator.newInstance(cl);
            HashSet<String> worked = new HashSet<String>();
            ArrayList<Method> methods = new ArrayList<Method>();
            int i = 0;
            do {
                int n;
                Method[] npkg;
                if (i < ics.length) {
                    if (!Modifier.isPublic(ics[i].getModifiers())) {
                        npkg = ics[i].getPackage().getName();
                        if (pkg == null) {
                            pkg = npkg;
                        } else if (!pkg.equals(npkg)) {
                            throw new IllegalArgumentException("non-public interfaces from different packages");
                        }
                    }
                    ccp.addInterface(ics[i]);
                    npkg = ics[i].getMethods();
                    n = npkg.length;
                } else {
                    if (pkg == null) {
                        pkg = PACKAGE_NAME;
                    }
                    String pcn = pkg + ".proxy" + id;
                    ccp.setClassName(pcn);
                    ccp.addField("public static java.lang.reflect.Method[] methods;");
                    ccp.addField("private " + InvocationHandler.class.getName() + " handler;");
                    ccp.addConstructor(1, new Class[]{InvocationHandler.class}, new Class[0], "handler=$1;");
                    ccp.addDefaultConstructor();
                    Class<?> clazz = ccp.toClass();
                    clazz.getField("methods").set(null, methods.toArray(new Method[0]));
                    String fcn = Proxy.class.getName() + id;
                    ccm = ClassGenerator.newInstance(cl);
                    ccm.setClassName(fcn);
                    ccm.addDefaultConstructor();
                    ccm.setSuperClass(Proxy.class);
                    ccm.addMethod("public Object newInstance(" + InvocationHandler.class.getName() + " h){ return new " + pcn + "($1); }");
                    Class<?> pc = ccm.toClass();
                    proxy = (Proxy)pc.newInstance();
                    return proxy;
                }
                for (int j = 0; j < n; ++j) {
                    Method method = npkg[j];
                    String desc = ReflectUtils.getDesc(method);
                    if (worked.contains(desc)) continue;
                    worked.add(desc);
                    int ix = methods.size();
                    Class<?> rt = method.getReturnType();
                    Class<?>[] pts = method.getParameterTypes();
                    StringBuilder code = new StringBuilder("Object[] args = new Object[").append(pts.length).append("];");
                    for (int j2 = 0; j2 < pts.length; ++j2) {
                        code.append(" args[").append(j2).append("] = ($w)$").append(j2 + 1).append(";");
                    }
                    code.append(" Object ret = handler.invoke(this, methods[" + ix + "], args);");
                    if (!Void.TYPE.equals(rt)) {
                        code.append(" return ").append(Proxy.asArgument(rt, "ret")).append(";");
                    }
                    methods.add(method);
                    ccp.addMethod(method.getName(), method.getModifiers(), rt, pts, method.getExceptionTypes(), code.toString());
                }
                ++i;
            } while (true);
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
            Map<String, Object> map2 = cache;
            synchronized (map2) {
                if (proxy == null) {
                    cache.remove(key);
                } else {
                    cache.put(key, new WeakReference<Proxy>(proxy));
                }
                cache.notifyAll();
            }
        }
    }

    public Object newInstance() {
        return this.newInstance(THROW_UNSUPPORTED_INVOKER);
    }

    public abstract Object newInstance(InvocationHandler var1);

    protected Proxy() {
    }

    protected static String asArgument(Class<?> cl, String name) {
        if (cl.isPrimitive()) {
            if (Boolean.TYPE == cl) {
                return name + "==null?false:((Boolean)" + name + ").booleanValue()";
            }
            if (Byte.TYPE == cl) {
                return name + "==null?(byte)0:((Byte)" + name + ").byteValue()";
            }
            if (Character.TYPE == cl) {
                return name + "==null?(char)0:((Character)" + name + ").charValue()";
            }
            if (Double.TYPE == cl) {
                return name + "==null?(double)0:((Double)" + name + ").doubleValue()";
            }
            if (Float.TYPE == cl) {
                return name + "==null?(float)0:((Float)" + name + ").floatValue()";
            }
            if (Integer.TYPE == cl) {
                return name + "==null?(int)0:((Integer)" + name + ").intValue()";
            }
            if (Long.TYPE == cl) {
                return name + "==null?(long)0:((Long)" + name + ").longValue()";
            }
            if (Short.TYPE == cl) {
                return name + "==null?(short)0:((Short)" + name + ").shortValue()";
            }
            throw new RuntimeException(name + " is unknown primitive type.");
        }
        return "(" + ReflectUtils.getName(cl) + ")" + name;
    }

}

