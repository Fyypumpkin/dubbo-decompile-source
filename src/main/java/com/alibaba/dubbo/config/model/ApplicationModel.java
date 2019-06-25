/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.model;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.config.model.ConsumerModel;
import com.alibaba.dubbo.config.model.ProviderModel;
import com.alibaba.dubbo.rpc.Invoker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ApplicationModel {
    protected static final Logger logger = LoggerFactory.getLogger(ApplicationModel.class);
    private static final ConcurrentMap<String, ProviderModel> providedServices = new ConcurrentHashMap<String, ProviderModel>();
    private static final ConcurrentMap<String, ConsumerModel> consumedServices = new ConcurrentHashMap<String, ConsumerModel>();
    public static final ConcurrentMap<String, Set<Invoker>> providedServicesInvoker = new ConcurrentHashMap<String, Set<Invoker>>();

    public static List<ConsumerModel> allConsumerModels() {
        return new ArrayList<ConsumerModel>(consumedServices.values());
    }

    public static ProviderModel getProviderModel(String serviceName) {
        return (ProviderModel)providedServices.get(serviceName);
    }

    public static ConsumerModel getConsumerModel(String serviceName) {
        return (ConsumerModel)consumedServices.get(serviceName);
    }

    public static List<ProviderModel> allProviderModels() {
        return new ArrayList<ProviderModel>(providedServices.values());
    }

    public static boolean initConsumerModel(String serviceName, ConsumerModel consumerModel) {
        if (consumedServices.putIfAbsent(serviceName, consumerModel) != null) {
            logger.warn("Already register the same consumer:" + serviceName);
            return false;
        }
        return true;
    }

    public static void initProviderModel(String serviceName, ProviderModel providerModel) {
        if (providedServices.put(serviceName, providerModel) != null) {
            logger.warn("already register the provider service: " + serviceName);
            return;
        }
    }

    public static void addProviderInvoker(String serviceName, Invoker invoker) {
        Set invokers = (Set)providedServicesInvoker.get(serviceName);
        if (invokers == null) {
            providedServicesInvoker.putIfAbsent(serviceName, new ConcurrentHashSet());
            invokers = (Set)providedServicesInvoker.get(serviceName);
        }
        invokers.add(invoker);
    }

    public Set<Invoker> getProviderInvoker(String serviceName) {
        Set invokers = (Set)providedServicesInvoker.get(serviceName);
        if (invokers == null) {
            return Collections.emptySet();
        }
        return invokers;
    }
}

