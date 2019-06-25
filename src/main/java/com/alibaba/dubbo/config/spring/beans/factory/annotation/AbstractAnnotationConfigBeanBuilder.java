/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.commons.logging.Log
 *  org.apache.commons.logging.LogFactory
 *  org.springframework.beans.factory.ListableBeanFactory
 *  org.springframework.context.ApplicationContext
 *  org.springframework.util.Assert
 */
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.config.AbstractInterfaceConfig;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.spring.util.BeanFactoryUtils;
import java.lang.annotation.Annotation;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

abstract class AbstractAnnotationConfigBeanBuilder<A extends Annotation, B extends AbstractInterfaceConfig> {
    protected final Log logger = LogFactory.getLog(this.getClass());
    protected final A annotation;
    protected final ApplicationContext applicationContext;
    protected final ClassLoader classLoader;
    protected Object bean;
    protected Class<?> interfaceClass;

    protected AbstractAnnotationConfigBeanBuilder(A annotation, ClassLoader classLoader, ApplicationContext applicationContext) {
        Assert.notNull(annotation, (String)"The Annotation must not be null!");
        Assert.notNull((Object)classLoader, (String)"The ClassLoader must not be null!");
        Assert.notNull((Object)applicationContext, (String)"The ApplicationContext must not be null!");
        this.annotation = annotation;
        this.applicationContext = applicationContext;
        this.classLoader = classLoader;
    }

    public final B build() throws Exception {
        this.checkDependencies();
        B bean = this.doBuild();
        this.configureBean(bean);
        if (this.logger.isInfoEnabled()) {
            this.logger.info((Object)(bean + " has been built."));
        }
        return bean;
    }

    private void checkDependencies() {
    }

    protected abstract B doBuild();

    protected void configureBean(B bean) throws Exception {
        this.preConfigureBean(this.annotation, bean);
        this.configureRegistryConfigs(bean);
        this.configureMonitorConfig(bean);
        this.configureApplicationConfig(bean);
        this.configureModuleConfig(bean);
        this.postConfigureBean(this.annotation, bean);
    }

    protected abstract void preConfigureBean(A var1, B var2) throws Exception;

    private void configureRegistryConfigs(B bean) {
        String[] registryConfigBeanIds = this.resolveRegistryConfigBeanNames(this.annotation);
        List<RegistryConfig> registryConfigs = BeanFactoryUtils.getBeans((ListableBeanFactory)this.applicationContext, registryConfigBeanIds, RegistryConfig.class);
        ((AbstractInterfaceConfig)bean).setRegistries(registryConfigs);
    }

    private void configureMonitorConfig(B bean) {
        String monitorBeanName = this.resolveMonitorConfigBeanName(this.annotation);
        MonitorConfig monitorConfig = BeanFactoryUtils.getOptionalBean((ListableBeanFactory)this.applicationContext, monitorBeanName, MonitorConfig.class);
        ((AbstractInterfaceConfig)bean).setMonitor(monitorConfig);
    }

    private void configureApplicationConfig(B bean) {
        String applicationConfigBeanName = this.resolveApplicationConfigBeanName(this.annotation);
        ApplicationConfig applicationConfig = BeanFactoryUtils.getOptionalBean((ListableBeanFactory)this.applicationContext, applicationConfigBeanName, ApplicationConfig.class);
        ((AbstractInterfaceConfig)bean).setApplication(applicationConfig);
    }

    private void configureModuleConfig(B bean) {
        String moduleConfigBeanName = this.resolveModuleConfigBeanName(this.annotation);
        ModuleConfig moduleConfig = BeanFactoryUtils.getOptionalBean((ListableBeanFactory)this.applicationContext, moduleConfigBeanName, ModuleConfig.class);
        ((AbstractInterfaceConfig)bean).setModule(moduleConfig);
    }

    protected abstract String resolveModuleConfigBeanName(A var1);

    protected abstract String resolveApplicationConfigBeanName(A var1);

    protected abstract String[] resolveRegistryConfigBeanNames(A var1);

    protected abstract String resolveMonitorConfigBeanName(A var1);

    protected abstract void postConfigureBean(A var1, B var2) throws Exception;

    public <T extends AbstractAnnotationConfigBeanBuilder<A, B>> T bean(Object bean) {
        this.bean = bean;
        return (T)this;
    }

    public <T extends AbstractAnnotationConfigBeanBuilder<A, B>> T interfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        return (T)this;
    }
}

