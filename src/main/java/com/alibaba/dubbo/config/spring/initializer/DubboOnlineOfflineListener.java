/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationEvent
 *  org.springframework.context.ApplicationListener
 */
package com.alibaba.dubbo.config.spring.initializer;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.model.ApplicationModel;
import com.alibaba.dubbo.config.model.ProviderModel;
import com.alibaba.dubbo.config.spring.initializer.DubboOnlineOfflineEvent;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryFactory;
import com.alibaba.dubbo.registry.TotalRegistry;
import com.alibaba.dubbo.registry.support.ProviderConsumerRegTable;
import com.alibaba.dubbo.registry.support.ProviderInvokerWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class DubboOnlineOfflineListener
implements ApplicationListener<DubboOnlineOfflineEvent> {
    private static Logger logger = LoggerFactory.getLogger(DubboOnlineOfflineListener.class);
    private static RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getAdaptiveExtension();
    private static transient ApplicationContext applicationContext;
    private static Map<TotalRegistry, List<URL>> registryServiceMap;
    private static volatile boolean isRunning;

    public void onApplicationEvent(DubboOnlineOfflineEvent event) {
        if (event.getType().equals("online")) {
            DubboOnlineOfflineListener.online();
        } else if (event.getType().equals("offline")) {
            DubboOnlineOfflineListener.offline();
        }
    }

    public static void afterContainerStarted(String type, ApplicationContext applicationContext) {
        DubboOnlineOfflineListener.applicationContext = applicationContext;
        DubboOnlineOfflineListener.afterContainerStarted(type);
    }

    public static void afterContainerStarted(String type) {
        DubboOnlineOfflineEvent event = new DubboOnlineOfflineEvent(type, applicationContext);
        applicationContext.publishEvent((ApplicationEvent)event);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void online() {
        if (!isRunning) {
            ProviderConsumerRegTable.setAfterInitRegister(true);
            Object object = ProviderConsumerRegTable.registerLock;
            synchronized (object) {
                if (!isRunning) {
                    logger.info("Begin register all services: ");
                    List<ProviderModel> providerModelList = ApplicationModel.allProviderModels();
                    for (ProviderModel providerModel : providerModelList) {
                        Set<ProviderInvokerWrapper> providerInvokerWrapperSet = ProviderConsumerRegTable.getProviderInvoker(providerModel.getServiceName());
                        for (ProviderInvokerWrapper providerInvokerWrapper : providerInvokerWrapperSet) {
                            URL registryUrl = providerInvokerWrapper.getRegistryUrl();
                            URL providerUrl = providerInvokerWrapper.getProviderUrl();
                            Registry registry = registryFactory.getRegistry(registryUrl);
                            if (registry instanceof TotalRegistry) {
                                DubboOnlineOfflineListener.refreshRegistryMap(registryUrl, providerUrl);
                                continue;
                            }
                            if (providerInvokerWrapper.isReg()) continue;
                            registry.register(providerUrl);
                            providerInvokerWrapper.setReg(true);
                        }
                    }
                    DubboOnlineOfflineListener.DoTotalRegister();
                    isRunning = true;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void offline() {
        if (isRunning) {
            ProviderConsumerRegTable.setAfterInitRegister(false);
            Object object = ProviderConsumerRegTable.registerLock;
            synchronized (object) {
                if (isRunning) {
                    logger.info("Begin unregister all services: ");
                    List<ProviderModel> providerModelList = ApplicationModel.allProviderModels();
                    for (ProviderModel providerModel : providerModelList) {
                        Set<ProviderInvokerWrapper> providerInvokerWrapperSet = ProviderConsumerRegTable.getProviderInvoker(providerModel.getServiceName());
                        for (ProviderInvokerWrapper providerInvokerWrapper : providerInvokerWrapperSet) {
                            URL registryUrl = providerInvokerWrapper.getRegistryUrl();
                            URL providerUrl = providerInvokerWrapper.getProviderUrl();
                            Registry registry = registryFactory.getRegistry(registryUrl);
                            if (registry instanceof TotalRegistry) {
                                DubboOnlineOfflineListener.refreshRegistryMap(registryUrl, providerUrl);
                                continue;
                            }
                            if (!providerInvokerWrapper.isReg()) continue;
                            if (!registryUrl.getProtocol().equals("haunt")) {
                                try {
                                    registry.unregister(providerUrl);
                                }
                                catch (Throwable t) {
                                    logger.error("Failed to Unregister url: " + providerUrl + ", cause: " + t.getMessage(), t);
                                }
                            }
                            providerInvokerWrapper.setReg(false);
                        }
                    }
                    DubboOnlineOfflineListener.DoTotalUnRegister();
                    isRunning = false;
                }
            }
        }
    }

    private static void refreshRegistryMap(URL registryUrl, URL providerUrl) {
        TotalRegistry totalRegistry = (TotalRegistry)registryFactory.getRegistry(registryUrl);
        List<URL> invokers = registryServiceMap.get(totalRegistry);
        if (invokers == null) {
            registryServiceMap.putIfAbsent(totalRegistry, new ArrayList());
            invokers = registryServiceMap.get(totalRegistry);
        }
        invokers.add(providerUrl);
    }

    private static void DoTotalRegister() {
        if (!registryServiceMap.isEmpty()) {
            for (Map.Entry<TotalRegistry, List<URL>> registryEntrySet : registryServiceMap.entrySet()) {
                TotalRegistry registry = registryEntrySet.getKey();
                List<URL> serviceList = registryEntrySet.getValue();
                registry.totalRegister(serviceList);
                if (!logger.isInfoEnabled()) continue;
                logger.info("Totally register services to registry " + registry.getUrl());
            }
        }
    }

    private static void DoTotalUnRegister() {
        if (!registryServiceMap.isEmpty()) {
            for (Map.Entry<TotalRegistry, List<URL>> registryEntrySet : registryServiceMap.entrySet()) {
                TotalRegistry registry = registryEntrySet.getKey();
                try {
                    registry.totalUnRegister();
                }
                catch (Throwable t) {
                    logger.error("Failed to Unregister registry: " + registry.getUrl() + ", cause: " + t.getMessage(), t);
                }
                if (!logger.isInfoEnabled()) continue;
                logger.info("Totally unregister services to registry " + registry.getUrl());
            }
        }
    }

    static {
        registryServiceMap = new ConcurrentHashMap<TotalRegistry, List<URL>>();
        isRunning = false;
    }
}

