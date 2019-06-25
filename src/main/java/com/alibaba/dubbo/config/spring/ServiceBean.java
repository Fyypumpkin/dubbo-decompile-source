/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.aop.support.AopUtils
 *  org.springframework.beans.factory.BeanFactoryUtils
 *  org.springframework.beans.factory.BeanNameAware
 *  org.springframework.beans.factory.DisposableBean
 *  org.springframework.beans.factory.InitializingBean
 *  org.springframework.beans.factory.ListableBeanFactory
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 *  org.springframework.context.ApplicationEvent
 *  org.springframework.context.ApplicationListener
 *  org.springframework.context.event.ContextRefreshedEvent
 *  org.springframework.context.event.ContextStartedEvent
 *  org.springframework.context.support.AbstractApplicationContext
 */
package com.alibaba.dubbo.config.spring;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.extension.SpringExtensionFactory;
import com.alibaba.dubbo.config.spring.initializer.DubboOnlineOfflineListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.AbstractApplicationContext;

public class ServiceBean<T>
extends ServiceConfig<T>
implements InitializingBean,
DisposableBean,
ApplicationContextAware,
ApplicationListener,
BeanNameAware {
    private static final long serialVersionUID = 213195494150089726L;
    private static transient ApplicationContext SPRING_CONTEXT;
    private final transient Service service;
    private transient ApplicationContext applicationContext;
    private transient String beanName;
    private transient boolean supportedApplicationListener;
    private static transient List<String> exportedServiceList;
    private static volatile AtomicBoolean springContainerStarted;

    public ServiceBean() {
        this.service = null;
    }

    public ServiceBean(Service service) {
        super(service);
        this.service = service;
    }

    public static ApplicationContext getSpringContext() {
        return SPRING_CONTEXT;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        block6 : {
            this.applicationContext = applicationContext;
            SpringExtensionFactory.addApplicationContext(applicationContext);
            if (applicationContext != null) {
                SPRING_CONTEXT = applicationContext;
                try {
                    Method method = applicationContext.getClass().getMethod("addApplicationListener", ApplicationListener.class);
                    method.invoke((Object)applicationContext, this);
                    this.supportedApplicationListener = true;
                }
                catch (Throwable t) {
                    if (!(applicationContext instanceof AbstractApplicationContext)) break block6;
                    try {
                        Method method = AbstractApplicationContext.class.getDeclaredMethod("addListener", ApplicationListener.class);
                        if (!method.isAccessible()) {
                            method.setAccessible(true);
                        }
                        method.invoke((Object)applicationContext, this);
                        this.supportedApplicationListener = true;
                    }
                    catch (Throwable method) {
                        // empty catch block
                    }
                }
            }
        }
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    public Service getService() {
        return this.service;
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent && this.isDelay() && !this.isExported() && !this.isUnexported()) {
            if (logger.isInfoEnabled()) {
                logger.info("The service ready on spring started. service: " + this.getInterface());
            }
            this.export();
        }
        if (event instanceof ContextStartedEvent) {
            this.afterSpringContainerStarted();
        }
    }

    private void afterSpringContainerStarted() {
        if (springContainerStarted.get()) {
            return;
        }
        Map totalServiceBean = BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)ServiceBean.getSpringContext(), ServiceBean.class, (boolean)false, (boolean)false);
        if (exportedServiceList.size() < totalServiceBean.size() && logger.isErrorEnabled()) {
            Set totalServiceBeanNames = totalServiceBean.keySet();
            totalServiceBeanNames.removeAll(exportedServiceList);
            logger.error("Found some services exported failed or delay: " + totalServiceBeanNames);
        }
        DubboOnlineOfflineListener.afterContainerStarted("online", this.applicationContext);
        springContainerStarted.compareAndSet(false, true);
    }

    @Override
    protected synchronized void doExport() {
        super.doExport();
        exportedServiceList.add(this.beanName);
    }

    private boolean isDelay() {
        Integer delay = this.getDelay();
        ProviderConfig provider = this.getProvider();
        if (delay == null && provider != null) {
            delay = provider.getDelay();
        }
        return this.supportedApplicationListener && (delay == null || delay == -1);
    }

    public void afterPropertiesSet() throws Exception {
        List<RegistryConfig> registries;
        Map protocolConfigMap;
        if (this.getProvider() == null) {
            Map providerConfigMap;
            Map map = providerConfigMap = this.applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)this.applicationContext, ProviderConfig.class, (boolean)false, (boolean)false);
            if (providerConfigMap != null && providerConfigMap.size() > 0) {
                Map map2 = protocolConfigMap = this.applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)this.applicationContext, ProtocolConfig.class, (boolean)false, (boolean)false);
                if ((protocolConfigMap == null || protocolConfigMap.size() == 0) && providerConfigMap.size() > 1) {
                    ArrayList<ProviderConfig> providerConfigs = new ArrayList<ProviderConfig>();
                    for (Object config : providerConfigMap.values()) {
                        if (((ProviderConfig)config).isDefault() == null || !((ProviderConfig)config).isDefault().booleanValue()) continue;
                        providerConfigs.add((ProviderConfig)config);
                    }
                    if (!providerConfigs.isEmpty()) {
                        this.setProviders(providerConfigs);
                    }
                } else {
                    Object providerConfig = null;
                    for (Object config : providerConfigMap.values()) {
                        if (((ProviderConfig)config).isDefault() != null && !((ProviderConfig)config).isDefault().booleanValue()) continue;
                        if (providerConfig != null) {
                            throw new IllegalStateException("Duplicate provider configs: " + providerConfig + " and " + config);
                        }
                        providerConfig = config;
                    }
                    if (providerConfig != null) {
                        this.setProvider((ProviderConfig)providerConfig);
                    }
                }
            }
        }
        if (this.getApplication() == null && (this.getProvider() == null || this.getProvider().getApplication() == null)) {
            Map applicationConfigMap;
            Map map = applicationConfigMap = this.applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)this.applicationContext, ApplicationConfig.class, (boolean)false, (boolean)false);
            if (applicationConfigMap != null && applicationConfigMap.size() > 0) {
                AbstractConfig applicationConfig = null;
                for (AbstractConfig config : applicationConfigMap.values()) {
                    if (((ApplicationConfig)config).isDefault() != null && !((ApplicationConfig)config).isDefault().booleanValue()) continue;
                    if (applicationConfig != null) {
                        throw new IllegalStateException("Duplicate application configs: " + applicationConfig + " and " + config);
                    }
                    applicationConfig = config;
                }
                if (applicationConfig != null) {
                    this.setApplication((ApplicationConfig)applicationConfig);
                }
            }
        }
        if (this.getModule() == null && (this.getProvider() == null || this.getProvider().getModule() == null)) {
            Map moduleConfigMap;
            Map map = moduleConfigMap = this.applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)this.applicationContext, ModuleConfig.class, (boolean)false, (boolean)false);
            if (moduleConfigMap != null && moduleConfigMap.size() > 0) {
                AbstractConfig moduleConfig = null;
                for (AbstractConfig config : moduleConfigMap.values()) {
                    if (((ModuleConfig)config).isDefault() != null && !((ModuleConfig)config).isDefault().booleanValue()) continue;
                    if (moduleConfig != null) {
                        throw new IllegalStateException("Duplicate module configs: " + moduleConfig + " and " + config);
                    }
                    moduleConfig = config;
                }
                if (moduleConfig != null) {
                    this.setModule((ModuleConfig)moduleConfig);
                }
            }
        }
        if (!(this.getRegistries() != null && !this.getRegistries().isEmpty() || this.getProvider() != null && this.getProvider().getRegistries() != null && !this.getProvider().getRegistries().isEmpty() || this.getApplication() != null && this.getApplication().getRegistries() != null && !this.getApplication().getRegistries().isEmpty())) {
            Map registryConfigMap;
            Map map = registryConfigMap = this.applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)this.applicationContext, RegistryConfig.class, (boolean)false, (boolean)false);
            if (registryConfigMap != null && registryConfigMap.size() > 0) {
                ArrayList<AbstractConfig> registryConfigs = new ArrayList<AbstractConfig>();
                for (AbstractConfig config : registryConfigMap.values()) {
                    if (((RegistryConfig)config).isDefault() != null && !((RegistryConfig)config).isDefault().booleanValue()) continue;
                    registryConfigs.add(config);
                }
                if (registryConfigs != null && !registryConfigs.isEmpty()) {
                    super.setRegistries(registryConfigs);
                }
            }
        }
        if ((registries = super.getRegistries()) != null && !registries.isEmpty()) {
            boolean containsHauntRegistry = false;
            boolean containsEtcd3Registry = false;
            Iterator etcd3 = "etcd3";
            for (RegistryConfig registry : registries) {
                if (((String)((Object)etcd3)).equals(registry.getProtocol())) {
                    containsEtcd3Registry = true;
                }
                if (!"haunt".equals(registry.getProtocol())) continue;
                containsHauntRegistry = true;
            }
            if (containsHauntRegistry) {
                Map registryConfigMap;
                Map map = registryConfigMap = this.applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)this.applicationContext, RegistryConfig.class, (boolean)false, (boolean)false);
                if (registryConfigMap != null && registryConfigMap.size() > 0) {
                    for (RegistryConfig config : registryConfigMap.values()) {
                        if (config.getId() == null || !config.getId().equals(etcd3) || containsEtcd3Registry) continue;
                        registries.add(0, config);
                        break;
                    }
                }
            }
        }
        if (!(this.getMonitor() != null || this.getProvider() != null && this.getProvider().getMonitor() != null || this.getApplication() != null && this.getApplication().getMonitor() != null)) {
            Map monitorConfigMap;
            Map map = monitorConfigMap = this.applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)this.applicationContext, MonitorConfig.class, (boolean)false, (boolean)false);
            if (monitorConfigMap != null && monitorConfigMap.size() > 0) {
                Object monitorConfig = null;
                for (Object config : monitorConfigMap.values()) {
                    if (((MonitorConfig)config).isDefault() != null && !((MonitorConfig)config).isDefault().booleanValue()) continue;
                    if (monitorConfig != null) {
                        throw new IllegalStateException("Duplicate monitor configs: " + monitorConfig + " and " + config);
                    }
                    monitorConfig = config;
                }
                if (monitorConfig != null) {
                    this.setMonitor((MonitorConfig)monitorConfig);
                }
            }
        }
        if ((this.getProtocols() == null || this.getProtocols().isEmpty()) && (this.getProvider() == null || this.getProvider().getProtocols() == null || this.getProvider().getProtocols().isEmpty())) {
            Map map = protocolConfigMap = this.applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)this.applicationContext, ProtocolConfig.class, (boolean)false, (boolean)false);
            if (protocolConfigMap != null && protocolConfigMap.size() > 0) {
                ArrayList<Object> protocolConfigs = new ArrayList<Object>();
                for (Object config : protocolConfigMap.values()) {
                    if (((ProtocolConfig)config).isDefault() != null && !((ProtocolConfig)config).isDefault().booleanValue()) continue;
                    protocolConfigs.add(config);
                }
                if (protocolConfigs != null && !protocolConfigs.isEmpty()) {
                    super.setProtocols(protocolConfigs);
                }
            }
        }
        if ((this.getPath() == null || this.getPath().length() == 0) && this.beanName != null && this.beanName.length() > 0 && this.getInterface() != null && this.getInterface().length() > 0 && this.beanName.startsWith(this.getInterface())) {
            this.setPath(this.beanName);
        }
        if (!this.isDelay()) {
            this.export();
        }
    }

    public void destroy() throws Exception {
    }

    @Override
    protected Class getServiceClass(T ref) {
        if (AopUtils.isAopProxy(ref)) {
            return AopUtils.getTargetClass(ref);
        }
        return super.getServiceClass(ref);
    }

    static {
        exportedServiceList = Collections.synchronizedList(new ArrayList());
        springContainerStarted = new AtomicBoolean(false);
    }
}

