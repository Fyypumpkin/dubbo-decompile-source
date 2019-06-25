/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.factory.DisposableBean
 *  org.springframework.beans.factory.config.BeanFactoryPostProcessor
 *  org.springframework.beans.factory.config.BeanPostProcessor
 *  org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 *  org.springframework.beans.factory.support.BeanDefinitionRegistry
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 */
package com.alibaba.dubbo.config.spring;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.config.spring.ServiceBean;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Deprecated
public class AnnotationBean
extends AbstractConfig
implements DisposableBean,
BeanFactoryPostProcessor,
BeanPostProcessor,
ApplicationContextAware {
    private static final long serialVersionUID = -7582802454287589552L;
    private static final Logger logger = LoggerFactory.getLogger(Logger.class);
    private final Set<ServiceConfig<?>> serviceConfigs = new ConcurrentHashSet();
    private final ConcurrentMap<String, ReferenceBean<?>> referenceConfigs = new ConcurrentHashMap();
    private String annotationPackage;
    private String[] annotationPackages;
    private ApplicationContext applicationContext;

    public String getPackage() {
        return this.annotationPackage;
    }

    public void setPackage(String annotationPackage) {
        this.annotationPackage = annotationPackage;
        this.annotationPackages = annotationPackage == null || annotationPackage.length() == 0 ? null : Constants.COMMA_SPLIT_PATTERN.split(annotationPackage);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (this.annotationPackage == null || this.annotationPackage.length() == 0) {
            return;
        }
        if (beanFactory instanceof BeanDefinitionRegistry) {
            try {
                Class<?> scannerClass = ReflectUtils.forName("org.springframework.context.annotation.ClassPathBeanDefinitionScanner");
                Object scanner = scannerClass.getConstructor(BeanDefinitionRegistry.class, Boolean.TYPE).newInstance(new Object[]{(BeanDefinitionRegistry)beanFactory, true});
                Class<?> filterClass = ReflectUtils.forName("org.springframework.core.type.filter.AnnotationTypeFilter");
                Object filter = filterClass.getConstructor(Class.class).newInstance(Service.class);
                Method addIncludeFilter = scannerClass.getMethod("addIncludeFilter", ReflectUtils.forName("org.springframework.core.type.filter.TypeFilter"));
                addIncludeFilter.invoke(scanner, filter);
                String[] packages = Constants.COMMA_SPLIT_PATTERN.split(this.annotationPackage);
                Method scan = scannerClass.getMethod("scan", String[].class);
                scan.invoke(scanner, new Object[]{packages});
            }
            catch (Throwable scannerClass) {
                // empty catch block
            }
        }
    }

    public void destroy() {
        for (ReferenceConfig referenceConfig : this.referenceConfigs.values()) {
            try {
                referenceConfig.destroy();
            }
            catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!this.isMatchPackage(bean)) {
            return bean;
        }
        Service service = bean.getClass().getAnnotation(Service.class);
        if (service != null) {
            ServiceBean<Object> serviceConfig = new ServiceBean<Object>(service);
            serviceConfig.setRef(bean);
            if (Void.TYPE.equals(service.interfaceClass()) && "".equals(service.interfaceName())) {
                if (bean.getClass().getInterfaces().length > 0) {
                    serviceConfig.setInterface(bean.getClass().getInterfaces()[0]);
                } else {
                    throw new IllegalStateException("Failed to export remote service class " + bean.getClass().getName() + ", cause: The @Service undefined interfaceClass or interfaceName, and the service class unimplemented any interfaces.");
                }
            }
            if (this.applicationContext != null) {
                serviceConfig.setApplicationContext(this.applicationContext);
                if (service.registry().length > 0) {
                    ArrayList<Object> registryConfigs = new ArrayList<Object>();
                    for (String registryId : service.registry()) {
                        if (registryId == null || registryId.length() <= 0) continue;
                        registryConfigs.add(this.applicationContext.getBean(registryId, RegistryConfig.class));
                    }
                    serviceConfig.setRegistries(registryConfigs);
                }
                if (service.provider().length() > 0) {
                    serviceConfig.setProvider((ProviderConfig)this.applicationContext.getBean(service.provider(), ProviderConfig.class));
                }
                if (service.monitor().length() > 0) {
                    serviceConfig.setMonitor((MonitorConfig)this.applicationContext.getBean(service.monitor(), MonitorConfig.class));
                }
                if (service.application().length() > 0) {
                    serviceConfig.setApplication((ApplicationConfig)this.applicationContext.getBean(service.application(), ApplicationConfig.class));
                }
                if (service.module().length() > 0) {
                    serviceConfig.setModule((ModuleConfig)this.applicationContext.getBean(service.module(), ModuleConfig.class));
                }
                if (service.provider().length() > 0) {
                    serviceConfig.setProvider((ProviderConfig)this.applicationContext.getBean(service.provider(), ProviderConfig.class));
                }
                if (service.protocol().length > 0) {
                    ArrayList<Object> protocolConfigs = new ArrayList<Object>();
                    for (String protocolId : service.protocol()) {
                        if (protocolId == null || protocolId.length() <= 0) continue;
                        protocolConfigs.add(this.applicationContext.getBean(protocolId, ProtocolConfig.class));
                    }
                    serviceConfig.setProtocols(protocolConfigs);
                }
                try {
                    serviceConfig.afterPropertiesSet();
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
            this.serviceConfigs.add(serviceConfig);
            serviceConfig.export();
        }
        return bean;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods;
        Field[] fields;
        Reference reference;
        Object value;
        if (!this.isMatchPackage(bean)) {
            return bean;
        }
        for (Method method : methods = bean.getClass().getMethods()) {
            String name = method.getName();
            if (name.length() <= 3 || !name.startsWith("set") || method.getParameterTypes().length != 1 || !Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) continue;
            try {
                reference = method.getAnnotation(Reference.class);
                if (reference == null || (value = this.refer(reference, method.getParameterTypes()[0])) == null) continue;
                method.invoke(bean, value);
            }
            catch (Throwable e) {
                logger.error("Failed to init remote service reference at method " + name + " in class " + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
            }
        }
        for (Field field : fields = bean.getClass().getDeclaredFields()) {
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                if ((reference = field.getAnnotation(Reference.class)) == null || (value = this.refer(reference, field.getType())) == null) continue;
                field.set(bean, value);
            }
            catch (Throwable e) {
                logger.error("Failed to init remote service reference at filed " + field.getName() + " in class " + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
            }
        }
        return bean;
    }

    private Object refer(Reference reference, Class<?> referenceClass) {
        String interfaceName;
        if (!"".equals(reference.interfaceName())) {
            interfaceName = reference.interfaceName();
        } else if (!Void.TYPE.equals(reference.interfaceClass())) {
            interfaceName = reference.interfaceClass().getName();
        } else if (referenceClass.isInterface()) {
            interfaceName = referenceClass.getName();
        } else {
            throw new IllegalStateException("The @Reference undefined interfaceClass or interfaceName, and the property type " + referenceClass.getName() + " is not a interface.");
        }
        String key = reference.group() + "/" + interfaceName + ":" + reference.version();
        ReferenceBean referenceConfig = (ReferenceBean)this.referenceConfigs.get(key);
        if (referenceConfig == null) {
            referenceConfig = new ReferenceBean(reference);
            if (Void.TYPE.equals(reference.interfaceClass()) && "".equals(reference.interfaceName()) && referenceClass.isInterface()) {
                referenceConfig.setInterface(referenceClass);
            }
            if (this.applicationContext != null) {
                referenceConfig.setApplicationContext(this.applicationContext);
                if (reference.registry().length > 0) {
                    ArrayList<Object> registryConfigs = new ArrayList<Object>();
                    for (String registryId : reference.registry()) {
                        if (registryId == null || registryId.length() <= 0) continue;
                        registryConfigs.add(this.applicationContext.getBean(registryId, RegistryConfig.class));
                    }
                    referenceConfig.setRegistries(registryConfigs);
                }
                if (reference.consumer().length() > 0) {
                    referenceConfig.setConsumer((ConsumerConfig)this.applicationContext.getBean(reference.consumer(), ConsumerConfig.class));
                }
                if (reference.monitor().length() > 0) {
                    referenceConfig.setMonitor((MonitorConfig)this.applicationContext.getBean(reference.monitor(), MonitorConfig.class));
                }
                if (reference.application().length() > 0) {
                    referenceConfig.setApplication((ApplicationConfig)this.applicationContext.getBean(reference.application(), ApplicationConfig.class));
                }
                if (reference.module().length() > 0) {
                    referenceConfig.setModule((ModuleConfig)this.applicationContext.getBean(reference.module(), ModuleConfig.class));
                }
                if (reference.consumer().length() > 0) {
                    referenceConfig.setConsumer((ConsumerConfig)this.applicationContext.getBean(reference.consumer(), ConsumerConfig.class));
                }
                try {
                    referenceConfig.afterPropertiesSet();
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
            this.referenceConfigs.putIfAbsent(key, referenceConfig);
            referenceConfig = (ReferenceBean)this.referenceConfigs.get(key);
        }
        return referenceConfig.get();
    }

    private boolean isMatchPackage(Object bean) {
        if (this.annotationPackages == null || this.annotationPackages.length == 0) {
            return true;
        }
        String beanClassName = bean.getClass().getName();
        for (String pkg : this.annotationPackages) {
            if (!beanClassName.startsWith(pkg)) continue;
            return true;
        }
        return false;
    }
}

