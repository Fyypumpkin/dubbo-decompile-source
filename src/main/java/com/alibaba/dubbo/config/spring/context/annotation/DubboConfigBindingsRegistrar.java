/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.support.BeanDefinitionRegistry
 *  org.springframework.context.EnvironmentAware
 *  org.springframework.context.annotation.ImportBeanDefinitionRegistrar
 *  org.springframework.core.annotation.AnnotationAttributes
 *  org.springframework.core.env.ConfigurableEnvironment
 *  org.springframework.core.env.Environment
 *  org.springframework.core.type.AnnotationMetadata
 *  org.springframework.util.Assert
 */
package com.alibaba.dubbo.config.spring.context.annotation;

import com.alibaba.dubbo.config.spring.context.annotation.DubboConfigBindingRegistrar;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubboConfigBindings;
import java.util.Map;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

public class DubboConfigBindingsRegistrar
implements ImportBeanDefinitionRegistrar,
EnvironmentAware {
    private ConfigurableEnvironment environment;

    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap((Map)importingClassMetadata.getAnnotationAttributes(EnableDubboConfigBindings.class.getName()));
        AnnotationAttributes[] annotationAttributes = attributes.getAnnotationArray("value");
        DubboConfigBindingRegistrar registrar = new DubboConfigBindingRegistrar();
        registrar.setEnvironment((Environment)this.environment);
        for (AnnotationAttributes element : annotationAttributes) {
            registrar.registerBeanDefinitions(element, registry);
        }
    }

    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, (Object)environment);
        this.environment = (ConfigurableEnvironment)environment;
    }
}

