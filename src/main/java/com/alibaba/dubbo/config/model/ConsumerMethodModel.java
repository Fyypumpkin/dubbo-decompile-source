/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.model;

import com.alibaba.dubbo.config.ReferenceConfig;
import java.lang.reflect.Method;

public class ConsumerMethodModel {
    private final Method method;
    private final ReferenceConfig metadata;
    private final String[] parameterTypes;
    private final Class<?>[] parameterClasses;
    private final Class<?> returnClass;
    private final String methodName;
    private final boolean generic;

    public ConsumerMethodModel(Method method, ReferenceConfig metadata) {
        this.method = method;
        this.parameterClasses = method.getParameterTypes();
        this.returnClass = method.getReturnType();
        this.parameterTypes = this.createParamSignature(this.parameterClasses);
        this.methodName = method.getName();
        this.metadata = metadata;
        this.generic = this.methodName.equals("$invoke") && this.parameterTypes != null && this.parameterTypes.length == 3;
    }

    public Method getMethod() {
        return this.method;
    }

    public Class<?> getReturnClass() {
        return this.returnClass;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String[] getParameterTypes() {
        return this.parameterTypes;
    }

    public ReferenceConfig getMetadata() {
        return this.metadata;
    }

    private String[] createParamSignature(Class<?>[] args) {
        if (args == null || args.length == 0) {
            return new String[0];
        }
        String[] paramSig = new String[args.length];
        for (int x = 0; x < args.length; ++x) {
            paramSig[x] = args[x].getName();
        }
        return paramSig;
    }

    public boolean isGeneric() {
        return this.generic;
    }

    public Class<?>[] getParameterClasses() {
        return this.parameterClasses;
    }
}

