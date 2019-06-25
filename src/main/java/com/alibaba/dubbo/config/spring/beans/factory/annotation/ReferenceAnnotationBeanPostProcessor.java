/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.youzan.api.rpc.annotation.Reference
 *  org.apache.commons.logging.Log
 *  org.apache.commons.logging.LogFactory
 *  org.springframework.beans.BeanUtils
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.PropertyValues
 *  org.springframework.beans.factory.BeanClassLoaderAware
 *  org.springframework.beans.factory.BeanCreationException
 *  org.springframework.beans.factory.DisposableBean
 *  org.springframework.beans.factory.annotation.InjectionMetadata
 *  org.springframework.beans.factory.annotation.InjectionMetadata$InjectedElement
 *  org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter
 *  org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor
 *  org.springframework.beans.factory.support.RootBeanDefinition
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 *  org.springframework.core.BridgeMethodResolver
 *  org.springframework.core.PriorityOrdered
 *  org.springframework.core.annotation.AnnotationUtils
 *  org.springframework.core.env.Environment
 *  org.springframework.util.ClassUtils
 *  org.springframework.util.ReflectionUtils
 *  org.springframework.util.ReflectionUtils$FieldCallback
 *  org.springframework.util.ReflectionUtils$MethodCallback
 *  org.springframework.util.StringUtils
 */
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.AbstractAnnotationConfigBeanBuilder;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.InternalReferenceBeanBuilder;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.ReferenceBeanBuilder;
import com.youzan.api.rpc.annotation.Reference;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public class ReferenceAnnotationBeanPostProcessor
extends InstantiationAwareBeanPostProcessorAdapter
implements MergedBeanDefinitionPostProcessor,
PriorityOrdered,
ApplicationContextAware,
BeanClassLoaderAware,
DisposableBean {
    public static final String BEAN_NAME = "referenceAnnotationBeanPostProcessor";
    private final Log logger = LogFactory.getLog(((Object)((Object)this)).getClass());
    private ApplicationContext applicationContext;
    private ClassLoader classLoader;
    private final ConcurrentMap<String, ReferenceInjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<String, ReferenceInjectionMetadata>(256);
    private final ConcurrentMap<String, ReferenceBean<?>> referenceBeansCache = new ConcurrentHashMap();

    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeanCreationException {
        InjectionMetadata metadata = this.findReferenceMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        }
        catch (BeanCreationException ex) {
            throw ex;
        }
        catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of @Reference dependencies failed", ex);
        }
        return pvs;
    }

    private List<ReferenceFieldElement> findFieldReferenceMetadata(Class<?> beanClass) {
        final LinkedList<ReferenceFieldElement> elements = new LinkedList<ReferenceFieldElement>();
        ReflectionUtils.doWithFields(beanClass, (ReflectionUtils.FieldCallback)new ReflectionUtils.FieldCallback(){

            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                Reference ref = (Reference)AnnotationUtils.getAnnotation((AnnotatedElement)field, Reference.class);
                if (ref != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (ReferenceAnnotationBeanPostProcessor.this.logger.isWarnEnabled()) {
                            ReferenceAnnotationBeanPostProcessor.this.logger.warn((Object)("@Reference annotation is not supported on static fields: " + field));
                        }
                        return;
                    }
                    elements.add(new ReferenceFieldElement(field, ref));
                    return;
                }
                com.alibaba.dubbo.config.annotation.Reference reference = (com.alibaba.dubbo.config.annotation.Reference)AnnotationUtils.getAnnotation((AnnotatedElement)field, com.alibaba.dubbo.config.annotation.Reference.class);
                if (reference != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (ReferenceAnnotationBeanPostProcessor.this.logger.isWarnEnabled()) {
                            ReferenceAnnotationBeanPostProcessor.this.logger.warn((Object)("@Reference annotation is not supported on static fields: " + field));
                        }
                        return;
                    }
                    elements.add(new ReferenceFieldElement(field, reference));
                }
            }
        });
        return elements;
    }

    private List<ReferenceMethodElement> findMethodReferenceMetadata(final Class<?> beanClass) {
        final LinkedList<ReferenceMethodElement> elements = new LinkedList<ReferenceMethodElement>();
        ReflectionUtils.doWithMethods(beanClass, (ReflectionUtils.MethodCallback)new ReflectionUtils.MethodCallback(){

            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod((Method)method);
                if (!BridgeMethodResolver.isVisibilityBridgeMethodPair((Method)method, (Method)bridgedMethod)) {
                    return;
                }
                Reference reference0 = (Reference)AnnotationUtils.findAnnotation((Method)bridgedMethod, Reference.class);
                if (reference0 != null && method.equals(ClassUtils.getMostSpecificMethod((Method)method, (Class)beanClass))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        if (ReferenceAnnotationBeanPostProcessor.this.logger.isWarnEnabled()) {
                            ReferenceAnnotationBeanPostProcessor.this.logger.warn((Object)("@Reference annotation is not supported on static methods: " + method));
                        }
                        return;
                    }
                    if (method.getParameterTypes().length == 0 && ReferenceAnnotationBeanPostProcessor.this.logger.isWarnEnabled()) {
                        ReferenceAnnotationBeanPostProcessor.this.logger.warn((Object)("@Reference  annotation should only be used on methods with parameters: " + method));
                    }
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod((Method)bridgedMethod, (Class)beanClass);
                    elements.add(new ReferenceMethodElement(method, pd, reference0));
                    return;
                }
                com.alibaba.dubbo.config.annotation.Reference reference = (com.alibaba.dubbo.config.annotation.Reference)AnnotationUtils.findAnnotation((Method)bridgedMethod, com.alibaba.dubbo.config.annotation.Reference.class);
                if (reference != null && method.equals(ClassUtils.getMostSpecificMethod((Method)method, (Class)beanClass))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        if (ReferenceAnnotationBeanPostProcessor.this.logger.isWarnEnabled()) {
                            ReferenceAnnotationBeanPostProcessor.this.logger.warn((Object)("@Reference annotation is not supported on static methods: " + method));
                        }
                        return;
                    }
                    if (method.getParameterTypes().length == 0 && ReferenceAnnotationBeanPostProcessor.this.logger.isWarnEnabled()) {
                        ReferenceAnnotationBeanPostProcessor.this.logger.warn((Object)("@Reference  annotation should only be used on methods with parameters: " + method));
                    }
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod((Method)bridgedMethod, (Class)beanClass);
                    elements.add(new ReferenceMethodElement(method, pd, reference));
                }
            }
        });
        return elements;
    }

    private ReferenceInjectionMetadata buildReferenceMetadata(Class<?> beanClass) {
        List<ReferenceFieldElement> fieldElements = this.findFieldReferenceMetadata(beanClass);
        List<ReferenceMethodElement> methodElements = this.findMethodReferenceMetadata(beanClass);
        return new ReferenceInjectionMetadata(beanClass, fieldElements, methodElements);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private InjectionMetadata findReferenceMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        String cacheKey = StringUtils.hasLength((String)beanName) ? beanName : clazz.getName();
        ReferenceInjectionMetadata metadata = (ReferenceInjectionMetadata)((Object)this.injectionMetadataCache.get(cacheKey));
        if (InjectionMetadata.needsRefresh((InjectionMetadata)metadata, clazz)) {
            ConcurrentMap<String, ReferenceInjectionMetadata> concurrentMap = this.injectionMetadataCache;
            synchronized (concurrentMap) {
                metadata = (ReferenceInjectionMetadata)((Object)this.injectionMetadataCache.get(cacheKey));
                if (InjectionMetadata.needsRefresh((InjectionMetadata)metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    try {
                        metadata = this.buildReferenceMetadata(clazz);
                        this.injectionMetadataCache.put(cacheKey, metadata);
                    }
                    catch (NoClassDefFoundError err) {
                        throw new IllegalStateException("Failed to introspect bean class [" + clazz.getName() + "] for reference metadata: could not find class that it depends on", err);
                    }
                }
            }
        }
        return metadata;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = this.findReferenceMetadata(beanName, beanType, null);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    public void destroy() throws Exception {
        for (ReferenceBean referenceBean : this.referenceBeansCache.values()) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info((Object)(referenceBean + " was destroying!"));
            }
            referenceBean.destroy();
        }
        this.injectionMetadataCache.clear();
        this.referenceBeansCache.clear();
        if (this.logger.isInfoEnabled()) {
            this.logger.info((Object)(((Object)((Object)this)).getClass() + " was destroying!"));
        }
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Collection<ReferenceBean<?>> getReferenceBeans() {
        return this.referenceBeansCache.values();
    }

    private ReferenceBean<?> buildReferenceBean(com.alibaba.dubbo.config.annotation.Reference reference, Class<?> referenceClass) throws Exception {
        String referenceBeanCacheKey = this.generateReferenceBeanCacheKey(reference, referenceClass);
        ReferenceBean referenceBean = (ReferenceBean)this.referenceBeansCache.get(referenceBeanCacheKey);
        if (referenceBean == null) {
            ReferenceBeanBuilder beanBuilder = (ReferenceBeanBuilder)ReferenceBeanBuilder.create(reference, this.classLoader, this.applicationContext).interfaceClass(referenceClass);
            referenceBean = (ReferenceBean)beanBuilder.build();
            this.referenceBeansCache.putIfAbsent(referenceBeanCacheKey, referenceBean);
        }
        return referenceBean;
    }

    private ReferenceBean<?> buildReferenceBean(Reference reference, Class<?> referenceClass) throws Exception {
        String referenceBeanCacheKey = this.generateReferenceBeanCacheKey(reference, referenceClass);
        ReferenceBean referenceBean = (ReferenceBean)this.referenceBeansCache.get(referenceBeanCacheKey);
        if (referenceBean == null) {
            InternalReferenceBeanBuilder beanBuilder = (InternalReferenceBeanBuilder)InternalReferenceBeanBuilder.create(reference, this.classLoader, this.applicationContext).interfaceClass(referenceClass);
            referenceBean = (ReferenceBean)beanBuilder.build();
            this.referenceBeansCache.putIfAbsent(referenceBeanCacheKey, referenceBean);
        }
        return referenceBean;
    }

    private String generateReferenceBeanCacheKey(com.alibaba.dubbo.config.annotation.Reference reference, Class<?> beanClass) {
        String interfaceName = ReferenceAnnotationBeanPostProcessor.resolveInterfaceName(reference, beanClass);
        String key = reference.url() + "/" + interfaceName + "/" + reference.version() + "/" + reference.group();
        Environment environment = this.applicationContext.getEnvironment();
        key = environment.resolvePlaceholders(key);
        return key;
    }

    private String generateReferenceBeanCacheKey(Reference reference, Class<?> beanClass) {
        String interfaceName = ReferenceAnnotationBeanPostProcessor.resolveInterfaceName(reference, beanClass);
        String key = reference.url() + "/" + interfaceName + "/" + reference.version() + "/" + reference.group();
        Environment environment = this.applicationContext.getEnvironment();
        key = environment.resolvePlaceholders(key);
        return key;
    }

    private static String resolveInterfaceName(Reference reference, Class<?> beanClass) throws IllegalStateException {
        String interfaceName;
        if (!"".equals(reference.interfaceName())) {
            interfaceName = reference.interfaceName();
        } else if (!Void.TYPE.equals((Object)reference.interfaceClass())) {
            interfaceName = reference.interfaceClass().getName();
        } else if (beanClass.isInterface()) {
            interfaceName = beanClass.getName();
        } else {
            throw new IllegalStateException("The @Reference undefined interfaceClass or interfaceName, and the property type " + beanClass.getName() + " is not a interface.");
        }
        return interfaceName;
    }

    private static String resolveInterfaceName(com.alibaba.dubbo.config.annotation.Reference reference, Class<?> beanClass) throws IllegalStateException {
        String interfaceName;
        if (!"".equals(reference.interfaceName())) {
            interfaceName = reference.interfaceName();
        } else if (!Void.TYPE.equals(reference.interfaceClass())) {
            interfaceName = reference.interfaceClass().getName();
        } else if (beanClass.isInterface()) {
            interfaceName = beanClass.getName();
        } else {
            throw new IllegalStateException("The @Reference undefined interfaceClass or interfaceName, and the property type " + beanClass.getName() + " is not a interface.");
        }
        return interfaceName;
    }

    public Map<InjectionMetadata.InjectedElement, ReferenceBean<?>> getInjectedFieldReferenceBeanMap() {
        LinkedHashMap injectedElementReferenceBeanMap = new LinkedHashMap();
        for (ReferenceInjectionMetadata metadata : this.injectionMetadataCache.values()) {
            Collection<ReferenceFieldElement> fieldElements = metadata.getFieldElements();
            for (ReferenceFieldElement fieldElement : fieldElements) {
                injectedElementReferenceBeanMap.put(fieldElement, fieldElement.referenceBean);
            }
        }
        return injectedElementReferenceBeanMap;
    }

    public Map<InjectionMetadata.InjectedElement, ReferenceBean<?>> getInjectedMethodReferenceBeanMap() {
        LinkedHashMap injectedElementReferenceBeanMap = new LinkedHashMap();
        for (ReferenceInjectionMetadata metadata : this.injectionMetadataCache.values()) {
            Collection<ReferenceMethodElement> methodElements = metadata.getMethodElements();
            for (ReferenceMethodElement methodElement : methodElements) {
                injectedElementReferenceBeanMap.put(methodElement, methodElement.referenceBean);
            }
        }
        return injectedElementReferenceBeanMap;
    }

    private <T> T getFieldValue(Object object, String fieldName, Class<T> fieldType) {
        Field field = ReflectionUtils.findField(object.getClass(), (String)fieldName, fieldType);
        ReflectionUtils.makeAccessible((Field)field);
        return (T)ReflectionUtils.getField((Field)field, (Object)object);
    }

    private class ReferenceFieldElement
    extends InjectionMetadata.InjectedElement {
        private final Field field;
        private com.alibaba.dubbo.config.annotation.Reference reference;
        private Reference reference0;
        private volatile ReferenceBean<?> referenceBean;

        protected ReferenceFieldElement(Field field, com.alibaba.dubbo.config.annotation.Reference reference) {
            super((Member)field, null);
            this.field = field;
            this.reference = reference;
        }

        protected ReferenceFieldElement(Field field, Reference reference) {
            super((Member)field, null);
            this.field = field;
            this.reference0 = reference;
        }

        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
            Class<?> referenceClass = this.field.getType();
            if (this.reference != null) {
                this.referenceBean = ReferenceAnnotationBeanPostProcessor.this.buildReferenceBean(this.reference, referenceClass);
            } else if (this.reference0 != null) {
                this.referenceBean = ReferenceAnnotationBeanPostProcessor.this.buildReferenceBean(this.reference0, referenceClass);
            }
            ReflectionUtils.makeAccessible((Field)this.field);
            this.field.set(bean, this.referenceBean.getObject());
        }
    }

    private class ReferenceMethodElement
    extends InjectionMetadata.InjectedElement {
        private final Method method;
        private com.alibaba.dubbo.config.annotation.Reference reference;
        private Reference reference0;
        private volatile ReferenceBean<?> referenceBean;

        protected ReferenceMethodElement(Method method, PropertyDescriptor pd, com.alibaba.dubbo.config.annotation.Reference reference) {
            super((Member)method, pd);
            this.method = method;
            this.reference = reference;
        }

        protected ReferenceMethodElement(Method method, PropertyDescriptor pd, Reference reference) {
            super((Member)method, pd);
            this.method = method;
            this.reference0 = reference;
        }

        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
            Class<?> referenceClass = this.pd.getPropertyType();
            if (this.reference != null) {
                this.referenceBean = ReferenceAnnotationBeanPostProcessor.this.buildReferenceBean(this.reference, referenceClass);
            } else if (this.reference0 != null) {
                this.referenceBean = ReferenceAnnotationBeanPostProcessor.this.buildReferenceBean(this.reference0, referenceClass);
            }
            ReflectionUtils.makeAccessible((Method)this.method);
            this.method.invoke(bean, this.referenceBean.getObject());
        }
    }

    private static class ReferenceInjectionMetadata
    extends InjectionMetadata {
        private final Collection<ReferenceFieldElement> fieldElements;
        private final Collection<ReferenceMethodElement> methodElements;

        public ReferenceInjectionMetadata(Class<?> targetClass, Collection<ReferenceFieldElement> fieldElements, Collection<ReferenceMethodElement> methodElements) {
            super(targetClass, ReferenceInjectionMetadata.combine(fieldElements, methodElements));
            this.fieldElements = fieldElements;
            this.methodElements = methodElements;
        }

        private static /* varargs */ <T> Collection<T> combine(Collection<? extends T> ... elements) {
            ArrayList<? extends T> allElements = new ArrayList<T>();
            for (Collection<? extends T> e : elements) {
                allElements.addAll(e);
            }
            return allElements;
        }

        public Collection<ReferenceFieldElement> getFieldElements() {
            return this.fieldElements;
        }

        public Collection<ReferenceMethodElement> getMethodElements() {
            return this.methodElements;
        }
    }

}

