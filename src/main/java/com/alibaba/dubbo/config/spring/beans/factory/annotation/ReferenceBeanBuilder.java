/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.PropertyValues
 *  org.springframework.beans.factory.ListableBeanFactory
 *  org.springframework.context.ApplicationContext
 *  org.springframework.core.convert.ConversionService
 *  org.springframework.core.convert.converter.Converter
 *  org.springframework.core.convert.support.DefaultConversionService
 *  org.springframework.core.env.Environment
 *  org.springframework.core.env.PropertyResolver
 *  org.springframework.util.Assert
 *  org.springframework.util.ClassUtils
 *  org.springframework.util.StringUtils
 *  org.springframework.validation.DataBinder
 */
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.config.AbstractInterfaceConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.AbstractAnnotationConfigBeanBuilder;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.AnnotationPropertyValuesAdapter;
import com.alibaba.dubbo.config.spring.convert.converter.StringArrayToMapConverter;
import com.alibaba.dubbo.config.spring.convert.converter.StringArrayToStringConverter;
import com.alibaba.dubbo.config.spring.util.BeanFactoryUtils;
import com.alibaba.dubbo.config.spring.util.ObjectUtils;
import java.lang.annotation.Annotation;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;

class ReferenceBeanBuilder
extends AbstractAnnotationConfigBeanBuilder<Reference, ReferenceBean> {
    private ReferenceBeanBuilder(Reference annotation, ClassLoader classLoader, ApplicationContext applicationContext) {
        super(annotation, classLoader, applicationContext);
    }

    private void configureInterface(Reference reference, ReferenceBean referenceBean) {
        Class interfaceClass = reference.interfaceClass();
        if (Void.TYPE.equals(interfaceClass)) {
            interfaceClass = null;
            String interfaceClassName = reference.interfaceName();
            if (StringUtils.hasText((String)interfaceClassName) && ClassUtils.isPresent((String)interfaceClassName, (ClassLoader)this.classLoader)) {
                interfaceClass = ClassUtils.resolveClassName((String)interfaceClassName, (ClassLoader)this.classLoader);
            }
        }
        if (interfaceClass == null) {
            interfaceClass = this.interfaceClass;
        }
        Assert.isTrue((boolean)interfaceClass.isInterface(), (String)"The class of field or method that was annotated @Reference is not an interface!");
        referenceBean.setInterface(interfaceClass);
    }

    private void configureConsumerConfig(Reference reference, ReferenceBean<?> referenceBean) {
        String consumerBeanName = reference.consumer();
        ConsumerConfig consumerConfig = BeanFactoryUtils.getOptionalBean((ListableBeanFactory)this.applicationContext, consumerBeanName, ConsumerConfig.class);
        referenceBean.setConsumer(consumerConfig);
    }

    @Override
    protected ReferenceBean doBuild() {
        return new ReferenceBean();
    }

    @Override
    protected void preConfigureBean(Reference reference, ReferenceBean referenceBean) {
        Assert.notNull((Object)this.interfaceClass, (String)"The interface class must set first!");
        DataBinder dataBinder = new DataBinder((Object)referenceBean);
        dataBinder.setConversionService(this.getConversionService());
        String[] ignoreAttributeNames = ObjectUtils.of("application", "module", "consumer", "monitor", "registry");
        dataBinder.bind((PropertyValues)new AnnotationPropertyValuesAdapter((Annotation)reference, (PropertyResolver)this.applicationContext.getEnvironment(), ignoreAttributeNames));
    }

    private ConversionService getConversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter((Converter)new StringArrayToStringConverter());
        conversionService.addConverter((Converter)new StringArrayToMapConverter());
        return conversionService;
    }

    @Override
    protected String resolveModuleConfigBeanName(Reference annotation) {
        return annotation.module();
    }

    @Override
    protected String resolveApplicationConfigBeanName(Reference annotation) {
        return annotation.application();
    }

    @Override
    protected String[] resolveRegistryConfigBeanNames(Reference annotation) {
        return annotation.registry();
    }

    @Override
    protected String resolveMonitorConfigBeanName(Reference annotation) {
        return annotation.monitor();
    }

    @Override
    protected void postConfigureBean(Reference annotation, ReferenceBean bean) throws Exception {
        bean.setApplicationContext(this.applicationContext);
        this.configureInterface(annotation, bean);
        this.configureConsumerConfig(annotation, bean);
        bean.afterPropertiesSet();
    }

    public static ReferenceBeanBuilder create(Reference annotation, ClassLoader classLoader, ApplicationContext applicationContext) {
        return new ReferenceBeanBuilder(annotation, classLoader, applicationContext);
    }
}

