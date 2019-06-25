/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.config.BeanDefinition
 *  org.springframework.beans.factory.config.BeanDefinitionHolder
 *  org.springframework.beans.factory.support.BeanDefinitionRegistry
 *  org.springframework.context.annotation.AnnotationConfigUtils
 *  org.springframework.context.annotation.ClassPathBeanDefinitionScanner
 *  org.springframework.core.env.Environment
 *  org.springframework.core.io.ResourceLoader
 */
package com.alibaba.dubbo.config.spring.context.annotation;

import java.util.Set;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

public class DubboClassPathBeanDefinitionScanner
extends ClassPathBeanDefinitionScanner {
    public DubboClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment, ResourceLoader resourceLoader) {
        super(registry, useDefaultFilters);
        this.setEnvironment(environment);
        this.setResourceLoader(resourceLoader);
        AnnotationConfigUtils.registerAnnotationConfigProcessors((BeanDefinitionRegistry)registry);
    }

    public DubboClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, Environment environment, ResourceLoader resourceLoader) {
        this(registry, false, environment, resourceLoader);
    }

    public /* varargs */ Set<BeanDefinitionHolder> doScan(String ... basePackages) {
        return super.doScan(basePackages);
    }

    public boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        return super.checkCandidate(beanName, beanDefinition);
    }
}

