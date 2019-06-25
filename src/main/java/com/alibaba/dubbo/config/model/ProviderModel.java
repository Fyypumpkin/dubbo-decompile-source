/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.model;

import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.config.model.ProviderMethodModel;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderModel {
    private final String serviceName;
    private final Object serviceInstance;
    private final ServiceConfig metadata;
    private final Map<String, List<ProviderMethodModel>> methods = new HashMap<String, List<ProviderMethodModel>>();

    public ProviderModel(String serviceName, ServiceConfig metadata, Object serviceInstance) {
        if (null == serviceInstance) {
            throw new IllegalArgumentException("Service[" + serviceName + "]Target is NULL.");
        }
        this.serviceName = serviceName;
        this.metadata = metadata;
        this.serviceInstance = serviceInstance;
        this.initMethod();
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public ServiceConfig getMetadata() {
        return this.metadata;
    }

    public Object getServiceInstance() {
        return this.serviceInstance;
    }

    public List<ProviderMethodModel> getAllMethods() {
        ArrayList<ProviderMethodModel> result = new ArrayList<ProviderMethodModel>();
        for (List<ProviderMethodModel> models : this.methods.values()) {
            result.addAll(models);
        }
        return result;
    }

    public ProviderMethodModel getMethodModel(String methodName, String[] argTypes) {
        List<ProviderMethodModel> methodModels = this.methods.get(methodName);
        if (methodModels != null) {
            for (ProviderMethodModel methodModel : methodModels) {
                if (!Arrays.equals(argTypes, methodModel.getMethodArgTypes())) continue;
                return methodModel;
            }
        }
        return null;
    }

    private void initMethod() {
        Method[] methodsToExport = null;
        for (Method method : methodsToExport = this.metadata.getInterfaceClass().getMethods()) {
            method.setAccessible(true);
            List<ProviderMethodModel> methodModels = this.methods.get(method.getName());
            if (methodModels == null) {
                methodModels = new ArrayList<ProviderMethodModel>(1);
                this.methods.put(method.getName(), methodModels);
            }
            methodModels.add(new ProviderMethodModel(method, this.serviceName));
        }
    }
}

