/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.support.BeanDefinitionBuilder
 *  org.springframework.beans.factory.support.BeanDefinitionRegistry
 *  org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser
 *  org.springframework.beans.factory.xml.ParserContext
 *  org.springframework.util.StringUtils
 */
package com.alibaba.dubbo.config.spring.schema;

import com.alibaba.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor;
import com.alibaba.dubbo.config.spring.util.BeanRegistrar;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class AnnotationBeanDefinitionParser
extends AbstractSingleBeanDefinitionParser {
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String packageToScan = element.getAttribute("package");
        String[] packagesToScan = StringUtils.trimArrayElements((String[])StringUtils.commaDelimitedListToStringArray((String)packageToScan));
        builder.addConstructorArgValue((Object)packagesToScan);
        builder.setRole(2);
        this.registerReferenceAnnotationBeanPostProcessor(parserContext.getRegistry());
    }

    protected boolean shouldGenerateIdAsFallback() {
        return true;
    }

    private void registerReferenceAnnotationBeanPostProcessor(BeanDefinitionRegistry registry) {
        BeanRegistrar.registerInfrastructureBean(registry, "referenceAnnotationBeanPostProcessor", ReferenceAnnotationBeanPostProcessor.class);
    }

    protected Class<?> getBeanClass(Element element) {
        return ServiceAnnotationBeanPostProcessor.class;
    }
}

