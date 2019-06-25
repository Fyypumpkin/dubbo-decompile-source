/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.model;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.model.ConsumerMethodModel;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ConsumerModel {
    private ReferenceConfig metadata;
    private Object proxyObject;
    private String serviceName;
    private final Map<Method, ConsumerMethodModel> methodModels = new IdentityHashMap<Method, ConsumerMethodModel>();

    public ConsumerModel(String serviceName, ReferenceConfig metadata, Object proxyObject, Method[] methods) {
        this.serviceName = serviceName;
        this.metadata = metadata;
        this.proxyObject = proxyObject;
        if (proxyObject != null) {
            for (Method method : methods) {
                this.methodModels.put(method, new ConsumerMethodModel(method, metadata));
            }
        }
    }

    public ReferenceConfig getMetadata() {
        return this.metadata;
    }

    public Object getProxyObject() {
        return this.proxyObject;
    }

    public ConsumerMethodModel getMethodModel(Method method) {
        return this.methodModels.get(method);
    }

    public List<ConsumerMethodModel> getAllMethods() {
        return new ArrayList<ConsumerMethodModel>(this.methodModels.values());
    }

    public String getServiceName() {
        return this.serviceName;
    }
}

