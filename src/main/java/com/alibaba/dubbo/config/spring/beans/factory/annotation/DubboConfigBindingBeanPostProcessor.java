/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.commons.logging.Log
 *  org.apache.commons.logging.LogFactory
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.factory.InitializingBean
 *  org.springframework.beans.factory.config.BeanPostProcessor
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 *  org.springframework.core.env.Environment
 */
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.common.utils.Assert;
import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.spring.context.properties.DefaultDubboConfigBinder;
import com.alibaba.dubbo.config.spring.context.properties.DubboConfigBinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

public class DubboConfigBindingBeanPostProcessor
implements BeanPostProcessor,
ApplicationContextAware,
InitializingBean {
    private final Log log = LogFactory.getLog(this.getClass());
    private final String prefix;
    private final String beanName;
    private DubboConfigBinder dubboConfigBinder;
    private ApplicationContext applicationContext;
    private boolean ignoreUnknownFields = true;
    private boolean ignoreInvalidFields = true;

    public DubboConfigBindingBeanPostProcessor(String prefix, String beanName) {
        Assert.notNull(prefix, "The prefix of Configuration Properties must not be null");
        Assert.notNull(beanName, "The name of bean must not be null");
        this.prefix = prefix;
        this.beanName = beanName;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals(this.beanName) && bean instanceof AbstractConfig) {
            AbstractConfig dubboConfig = (AbstractConfig)bean;
            this.dubboConfigBinder.bind(this.prefix, dubboConfig);
            if (this.log.isInfoEnabled()) {
                this.log.info((Object)("The properties of bean [name : " + beanName + "] have been binding by prefix of configuration properties : " + this.prefix));
            }
        }
        return bean;
    }

    public boolean isIgnoreUnknownFields() {
        return this.ignoreUnknownFields;
    }

    public void setIgnoreUnknownFields(boolean ignoreUnknownFields) {
        this.ignoreUnknownFields = ignoreUnknownFields;
    }

    public boolean isIgnoreInvalidFields() {
        return this.ignoreInvalidFields;
    }

    public void setIgnoreInvalidFields(boolean ignoreInvalidFields) {
        this.ignoreInvalidFields = ignoreInvalidFields;
    }

    public DubboConfigBinder getDubboConfigBinder() {
        return this.dubboConfigBinder;
    }

    public void setDubboConfigBinder(DubboConfigBinder dubboConfigBinder) {
        this.dubboConfigBinder = dubboConfigBinder;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.dubboConfigBinder == null) {
            try {
                this.dubboConfigBinder = (DubboConfigBinder)this.applicationContext.getBean(DubboConfigBinder.class);
            }
            catch (BeansException ignored) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug((Object)"DubboConfigBinder Bean can't be found in ApplicationContext.");
                }
                this.dubboConfigBinder = this.createDubboConfigBinder(this.applicationContext.getEnvironment());
            }
        }
        this.dubboConfigBinder.setIgnoreUnknownFields(this.ignoreUnknownFields);
        this.dubboConfigBinder.setIgnoreInvalidFields(this.ignoreInvalidFields);
    }

    protected DubboConfigBinder createDubboConfigBinder(Environment environment) {
        DefaultDubboConfigBinder defaultDubboConfigBinder = new DefaultDubboConfigBinder();
        defaultDubboConfigBinder.setEnvironment(environment);
        return defaultDubboConfigBinder;
    }
}

