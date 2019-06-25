/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.model;

import java.lang.reflect.Method;

public class ProviderMethodModel {
    private final transient Method method;
    private final String methodName;
    private final String[] methodArgTypes;
    private final String serviceName;

    public ProviderMethodModel(Method method, String serviceName) {
        this.method = method;
        this.serviceName = serviceName;
        this.methodName = method.getName();
        this.methodArgTypes = ProviderMethodModel.getArgTypes(method);
    }

    public Method getMethod() {
        return this.method;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String[] getMethodArgTypes() {
        return this.methodArgTypes;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    private static String[] getArgTypes(Method method) {
        String[] methodArgTypes = new String[]{};
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 0) {
            methodArgTypes = new String[parameterTypes.length];
            int index = 0;
            for (Class<?> paramType : parameterTypes) {
                methodArgTypes[index++] = paramType.getName();
            }
        }
        return methodArgTypes;
    }
}

