/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.commons.logging.Log
 *  org.apache.commons.logging.LogFactory
 *  org.springframework.beans.factory.config.BeanDefinition
 *  org.springframework.beans.factory.support.AbstractBeanDefinition
 *  org.springframework.beans.factory.support.BeanDefinitionBuilder
 *  org.springframework.beans.factory.support.BeanDefinitionReaderUtils
 *  org.springframework.beans.factory.support.BeanDefinitionRegistry
 *  org.springframework.context.EnvironmentAware
 *  org.springframework.context.annotation.ImportBeanDefinitionRegistrar
 *  org.springframework.core.annotation.AnnotationAttributes
 *  org.springframework.core.env.ConfigurableEnvironment
 *  org.springframework.core.env.Environment
 *  org.springframework.core.env.MutablePropertySources
 *  org.springframework.core.type.AnnotationMetadata
 *  org.springframework.util.Assert
 *  org.springframework.util.CollectionUtils
 *  org.springframework.util.StringUtils
 */
package com.alibaba.dubbo.config.spring.context.annotation;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.DubboConfigBindingBeanPostProcessor;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubboConfigBinding;
import com.alibaba.dubbo.config.spring.util.PropertySourcesUtils;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class DubboConfigBindingRegistrar
implements ImportBeanDefinitionRegistrar,
EnvironmentAware {
    private final Log log = LogFactory.getLog(this.getClass());
    private ConfigurableEnvironment environment;

    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap((Map)importingClassMetadata.getAnnotationAttributes(EnableDubboConfigBinding.class.getName()));
        this.registerBeanDefinitions(attributes, registry);
    }

    protected void registerBeanDefinitions(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        String prefix = this.environment.resolvePlaceholders(attributes.getString("prefix"));
        Class configClass = attributes.getClass("type");
        boolean multiple = attributes.getBoolean("multiple");
        this.registerDubboConfigBeans(prefix, configClass, multiple, registry);
    }

    private void registerDubboConfigBeans(String prefix, Class<? extends AbstractConfig> configClass, boolean multiple, BeanDefinitionRegistry registry) {
        Map<String, String> properties = PropertySourcesUtils.getSubProperties(this.environment.getPropertySources(), prefix);
        if (CollectionUtils.isEmpty(properties)) {
            if (this.log.isDebugEnabled()) {
                this.log.debug((Object)("There is no property for binding to dubbo config class [" + configClass.getName() + "] within prefix [" + prefix + "]"));
            }
            return;
        }
        Set<String> beanNames = multiple ? this.resolveMultipleBeanNames(properties) : Collections.singleton(this.resolveSingleBeanName(properties, configClass, registry));
        for (String beanName : beanNames) {
            this.registerDubboConfigBean(beanName, configClass, registry);
            this.registerDubboConfigBindingBeanPostProcessor(prefix, beanName, multiple, registry);
        }
    }

    private void registerDubboConfigBean(String beanName, Class<? extends AbstractConfig> configClass, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(configClass);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        registry.registerBeanDefinition(beanName, (BeanDefinition)beanDefinition);
        if (this.log.isInfoEnabled()) {
            this.log.info((Object)("The dubbo config bean definition [name : " + beanName + ", class : " + configClass.getName() + "] has been registered."));
        }
    }

    private void registerDubboConfigBindingBeanPostProcessor(String prefix, String beanName, boolean multiple, BeanDefinitionRegistry registry) {
        Class<DubboConfigBindingBeanPostProcessor> processorClass = DubboConfigBindingBeanPostProcessor.class;
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(processorClass);
        String actualPrefix = multiple ? PropertySourcesUtils.normalizePrefix(prefix) + beanName : prefix;
        builder.addConstructorArgValue((Object)actualPrefix).addConstructorArgValue((Object)beanName);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinition.setRole(2);
        BeanDefinitionReaderUtils.registerWithGeneratedName((AbstractBeanDefinition)beanDefinition, (BeanDefinitionRegistry)registry);
        if (this.log.isInfoEnabled()) {
            this.log.info((Object)("The BeanPostProcessor bean definition [" + processorClass.getName() + "] for dubbo config bean [name : " + beanName + "] has been registered."));
        }
    }

    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, (Object)environment);
        this.environment = (ConfigurableEnvironment)environment;
    }

    private Set<String> resolveMultipleBeanNames(Map<String, String> properties) {
        LinkedHashSet<String> beanNames = new LinkedHashSet<String>();
        for (String propertyName : properties.keySet()) {
            int index = propertyName.indexOf(".");
            if (index <= 0) continue;
            String beanName = propertyName.substring(0, index);
            beanNames.add(beanName);
        }
        return beanNames;
    }

    private String resolveSingleBeanName(Map<String, String> properties, Class<? extends AbstractConfig> configClass, BeanDefinitionRegistry registry) {
        String beanName = properties.get("id");
        if (!StringUtils.hasText((String)beanName)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(configClass);
            beanName = BeanDefinitionReaderUtils.generateBeanName((BeanDefinition)builder.getRawBeanDefinition(), (BeanDefinitionRegistry)registry);
        }
        return beanName;
    }
}

