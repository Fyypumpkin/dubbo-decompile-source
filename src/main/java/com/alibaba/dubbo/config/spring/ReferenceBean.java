/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.BeanFactoryUtils
 *  org.springframework.beans.factory.DisposableBean
 *  org.springframework.beans.factory.FactoryBean
 *  org.springframework.beans.factory.InitializingBean
 *  org.springframework.beans.factory.ListableBeanFactory
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 */
package com.alibaba.dubbo.config.spring;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.extension.SpringExtensionFactory;
import com.alibaba.dubbo.config.support.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ReferenceBean<T>
extends ReferenceConfig<T>
implements FactoryBean,
ApplicationContextAware,
InitializingBean,
DisposableBean {
    private static final long serialVersionUID = 213195494150089726L;
    private transient ApplicationContext applicationContext;

    public ReferenceBean() {
    }

    public ReferenceBean(Reference reference) {
        super(reference);
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        SpringExtensionFactory.addApplicationContext(applicationContext);
    }

    public Object getObject() throws Exception {
        return this.get();
    }

    public Class<?> getObjectType() {
        return this.getInterfaceClass();
    }

    @Parameter(excluded=true)
    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        Boolean b;
        List<RegistryConfig> registries;
        if (this.getConsumer() == null) {
            Map consumerConfigMap;
            Map map = consumerConfigMap = this.applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)this.applicationContext, ConsumerConfig.class, (boolean)false, (boolean)false);
            if (consumerConfigMap != null && consumerConfigMap.size() > 0) {
                ConsumerConfig consumerConfig = null;
                for (AbstractConfig config : consumerConfigMap.values()) {
                    if (((ConsumerConfig)config).isDefault() != null && !((ConsumerConfig)config).isDefault().booleanValue()) continue;
                    if (consumerConfig != null) {
                        throw new IllegalStateException("Duplicate consumer configs: " + consumerConfig + " and " + config);
                    }
                    consumerConfig = config;
                }
                if (consumerConfig != null) {
                    this.setConsumer(consumerConfig);
                }
            }
        }
        if (this.getApplication() == null && (this.getConsumer() == null || this.getConsumer().getApplication() == null)) {
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
        if (this.getModule() == null && (this.getConsumer() == null || this.getConsumer().getModule() == null)) {
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
        if (!(this.getRegistries() != null && !this.getRegistries().isEmpty() || this.getConsumer() != null && this.getConsumer().getRegistries() != null && !this.getConsumer().getRegistries().isEmpty() || this.getApplication() != null && this.getApplication().getRegistries() != null && !this.getApplication().getRegistries().isEmpty())) {
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
            String etcd3 = "etcd3";
            for (RegistryConfig registry : registries) {
                if (etcd3.equals(registry.getProtocol())) {
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
        if (!(this.getMonitor() != null || this.getConsumer() != null && this.getConsumer().getMonitor() != null || this.getApplication() != null && this.getApplication().getMonitor() != null)) {
            Map monitorConfigMap;
            Map map = monitorConfigMap = this.applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)this.applicationContext, MonitorConfig.class, (boolean)false, (boolean)false);
            if (monitorConfigMap != null && monitorConfigMap.size() > 0) {
                MonitorConfig monitorConfig = null;
                for (MonitorConfig config : monitorConfigMap.values()) {
                    if (config.isDefault() != null && !config.isDefault().booleanValue()) continue;
                    if (monitorConfig != null) {
                        throw new IllegalStateException("Duplicate monitor configs: " + monitorConfig + " and " + config);
                    }
                    monitorConfig = config;
                }
                if (monitorConfig != null) {
                    this.setMonitor(monitorConfig);
                }
            }
        }
        if ((b = this.isInit()) == null && this.getConsumer() != null) {
            b = this.getConsumer().isInit();
        }
        if (b != null && b.booleanValue()) {
            this.getObject();
        }
    }

    @Override
    public void destroy() {
    }
}

