/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

public class ServiceClassHolder {
    private static final ServiceClassHolder INSTANCE = new ServiceClassHolder();
    private final ThreadLocal<Class> holder = new ThreadLocal();

    public static ServiceClassHolder getInstance() {
        return INSTANCE;
    }

    private ServiceClassHolder() {
    }

    public Class popServiceClass() {
        Class clazz = this.holder.get();
        this.holder.remove();
        return clazz;
    }

    public void pushServiceClass(Class clazz) {
        this.holder.set(clazz);
    }
}

