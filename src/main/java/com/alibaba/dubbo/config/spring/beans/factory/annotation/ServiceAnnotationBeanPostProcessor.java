/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.youzan.api.rpc.annotation.ExtensionService
 *  com.youzan.api.rpc.annotation.Service
 *  org.springframework.beans.BeansException
 *  org.springframework.beans.MutablePropertyValues
 *  org.springframework.beans.PropertyValues
 *  org.springframework.beans.factory.BeanClassLoaderAware
 *  org.springframework.beans.factory.config.BeanDefinition
 *  org.springframework.beans.factory.config.BeanDefinitionHolder
 *  org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 *  org.springframework.beans.factory.config.RuntimeBeanReference
 *  org.springframework.beans.factory.config.SingletonBeanRegistry
 *  org.springframework.beans.factory.support.AbstractBeanDefinition
 *  org.springframework.beans.factory.support.BeanDefinitionBuilder
 *  org.springframework.beans.factory.support.BeanDefinitionRegistry
 *  org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
 *  org.springframework.beans.factory.support.BeanNameGenerator
 *  org.springframework.beans.factory.support.ManagedList
 *  org.springframework.context.EnvironmentAware
 *  org.springframework.context.ResourceLoaderAware
 *  org.springframework.context.annotation.AnnotationBeanNameGenerator
 *  org.springframework.context.annotation.ClassPathBeanDefinitionScanner
 *  org.springframework.core.annotation.AnnotationUtils
 *  org.springframework.core.env.Environment
 *  org.springframework.core.env.PropertyResolver
 *  org.springframework.core.io.ResourceLoader
 *  org.springframework.core.type.filter.AnnotationTypeFilter
 *  org.springframework.core.type.filter.TypeFilter
 *  org.springframework.util.Assert
 *  org.springframework.util.ClassUtils
 *  org.springframework.util.CollectionUtils
 *  org.springframework.util.ObjectUtils
 *  org.springframework.util.StringUtils
 */
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.AnnotationPropertyValuesAdapter;
import com.alibaba.dubbo.config.spring.context.annotation.DubboClassPathBeanDefinitionScanner;
import com.youzan.api.rpc.annotation.ExtensionService;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class ServiceAnnotationBeanPostProcessor
implements BeanDefinitionRegistryPostProcessor,
EnvironmentAware,
ResourceLoaderAware,
BeanClassLoaderAware {
    private static final String SEPARATOR = ":";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Set<String> packagesToScan;
    private Environment environment;
    private ResourceLoader resourceLoader;
    private ClassLoader classLoader;

    public /* varargs */ ServiceAnnotationBeanPostProcessor(String ... packagesToScan) {
        this(Arrays.asList(packagesToScan));
    }

    public ServiceAnnotationBeanPostProcessor(Collection<String> packagesToScan) {
        this((Set<String>)new LinkedHashSet<String>(packagesToScan));
    }

    public ServiceAnnotationBeanPostProcessor(Set<String> packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Set<String> resolvedPackagesToScan = this.resolvePackagesToScan(this.packagesToScan);
        if (!CollectionUtils.isEmpty(resolvedPackagesToScan)) {
            this.registerServiceBeans(resolvedPackagesToScan, registry);
        } else if (this.logger.isWarnEnabled()) {
            this.logger.warn("packagesToScan is empty , ServiceBean registry will be ignored!");
        }
    }

    private void registerServiceBeans(Set<String> packagesToScan, BeanDefinitionRegistry registry) {
        DubboClassPathBeanDefinitionScanner scanner = new DubboClassPathBeanDefinitionScanner(registry, this.environment, this.resourceLoader);
        BeanNameGenerator beanNameGenerator = this.resolveBeanNameGenerator(registry);
        scanner.setBeanNameGenerator(beanNameGenerator);
        scanner.addIncludeFilter((TypeFilter)new AnnotationTypeFilter(Service.class));
        scanner.addIncludeFilter((TypeFilter)new AnnotationTypeFilter(com.youzan.api.rpc.annotation.Service.class));
        scanner.addIncludeFilter((TypeFilter)new AnnotationTypeFilter(ExtensionService.class));
        for (String packageToScan : packagesToScan) {
            scanner.scan(new String[]{packageToScan});
            Set<BeanDefinitionHolder> beanDefinitionHolders = this.findServiceBeanDefinitionHolders(scanner, packageToScan, registry, beanNameGenerator);
            if (!CollectionUtils.isEmpty(beanDefinitionHolders)) {
                for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
                    this.registerServiceBean(beanDefinitionHolder, registry, scanner);
                }
                if (!this.logger.isInfoEnabled()) continue;
                this.logger.info(beanDefinitionHolders.size() + " annotated Dubbo's @Service Components { " + beanDefinitionHolders + " } were scanned under package[" + packageToScan + "]");
                continue;
            }
            if (!this.logger.isWarnEnabled()) continue;
            this.logger.warn("No Spring Bean annotating Dubbo's @Service was found under package[" + packageToScan + "]");
        }
    }

    private BeanNameGenerator resolveBeanNameGenerator(BeanDefinitionRegistry registry) {
        BeanNameGenerator beanNameGenerator = null;
        if (registry instanceof SingletonBeanRegistry) {
            SingletonBeanRegistry singletonBeanRegistry = (SingletonBeanRegistry)SingletonBeanRegistry.class.cast((Object)registry);
            beanNameGenerator = (BeanNameGenerator)singletonBeanRegistry.getSingleton("org.springframework.context.annotation.internalConfigurationBeanNameGenerator");
        }
        if (beanNameGenerator == null) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("BeanNameGenerator bean can't be found in BeanFactory with name [org.springframework.context.annotation.internalConfigurationBeanNameGenerator]");
                this.logger.info("BeanNameGenerator will be a instance of " + AnnotationBeanNameGenerator.class.getName() + " , it maybe a potential problem on bean name generation.");
            }
            beanNameGenerator = new AnnotationBeanNameGenerator();
        }
        return beanNameGenerator;
    }

    private Set<BeanDefinitionHolder> findServiceBeanDefinitionHolders(ClassPathBeanDefinitionScanner scanner, String packageToScan, BeanDefinitionRegistry registry, BeanNameGenerator beanNameGenerator) {
        Set beanDefinitions = scanner.findCandidateComponents(packageToScan);
        LinkedHashSet<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet<BeanDefinitionHolder>(beanDefinitions.size());
        for (BeanDefinition beanDefinition : beanDefinitions) {
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
            beanDefinitionHolders.add(beanDefinitionHolder);
        }
        return beanDefinitionHolders;
    }

    private void registerServiceBean(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry registry, DubboClassPathBeanDefinitionScanner scanner) {
        Class<?> beanClass = this.resolveClass(beanDefinitionHolder);
        Service service = (Service)AnnotationUtils.findAnnotation(beanClass, Service.class);
        if (service != null) {
            this.registerServiceBean(beanDefinitionHolder, registry, scanner, beanClass, service);
            return;
        }
        ExtensionService extensionService = (ExtensionService)AnnotationUtils.findAnnotation(beanClass, ExtensionService.class);
        if (extensionService != null) {
            this.registerServiceBean(beanDefinitionHolder, registry, scanner, beanClass, extensionService);
            return;
        }
        com.youzan.api.rpc.annotation.Service service0 = (com.youzan.api.rpc.annotation.Service)AnnotationUtils.findAnnotation(beanClass, com.youzan.api.rpc.annotation.Service.class);
        if (service0 != null) {
            this.registerServiceBean(beanDefinitionHolder, registry, scanner, beanClass, service0);
            return;
        }
    }

    private void registerServiceBean(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry registry, DubboClassPathBeanDefinitionScanner scanner, Class<?> beanClass, Service service) {
        Class<?> interfaceClass = this.resolveServiceInterfaceClass(beanClass, service);
        String annotatedServiceBeanName = beanDefinitionHolder.getBeanName();
        AbstractBeanDefinition serviceBeanDefinition = this.buildServiceBeanDefinition(service, interfaceClass, annotatedServiceBeanName);
        String beanName = this.generateServiceBeanName(service, interfaceClass, annotatedServiceBeanName);
        if (scanner.checkCandidate(beanName, (BeanDefinition)serviceBeanDefinition)) {
            registry.registerBeanDefinition(beanName, (BeanDefinition)serviceBeanDefinition);
            if (this.logger.isInfoEnabled()) {
                this.logger.warn("The BeanDefinition[" + (Object)serviceBeanDefinition + "] of ServiceBean has been registered with name : " + beanName);
            }
        } else if (this.logger.isWarnEnabled()) {
            this.logger.warn("The Duplicated BeanDefinition[" + (Object)serviceBeanDefinition + "] of ServiceBean[ bean name : " + beanName + "] was be found , Did @DubboComponentScan scan to same package in many times?");
        }
    }

    private void registerServiceBean(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry registry, DubboClassPathBeanDefinitionScanner scanner, Class<?> beanClass, com.youzan.api.rpc.annotation.Service service) {
        Class<?> interfaceClass = this.resolveServiceInterfaceClass(beanClass, service);
        String annotatedServiceBeanName = beanDefinitionHolder.getBeanName();
        AbstractBeanDefinition serviceBeanDefinition = this.buildServiceBeanDefinition(service, interfaceClass, annotatedServiceBeanName);
        String beanName = this.generateServiceBeanName(service, interfaceClass, annotatedServiceBeanName);
        if (scanner.checkCandidate(beanName, (BeanDefinition)serviceBeanDefinition)) {
            registry.registerBeanDefinition(beanName, (BeanDefinition)serviceBeanDefinition);
            if (this.logger.isInfoEnabled()) {
                this.logger.warn("The BeanDefinition[" + (Object)serviceBeanDefinition + "] of ServiceBean has been registered with name : " + beanName);
            }
        } else if (this.logger.isWarnEnabled()) {
            this.logger.warn("The Duplicated BeanDefinition[" + (Object)serviceBeanDefinition + "] of ServiceBean[ bean name : " + beanName + "] was be found , Did @DubboComponentScan scan to same package in many times?");
        }
    }

    private void registerServiceBean(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry registry, DubboClassPathBeanDefinitionScanner scanner, Class<?> beanClass, ExtensionService service) {
        Class<?> interfaceClass = this.resolveServiceInterfaceClass(beanClass, service);
        String annotatedServiceBeanName = beanDefinitionHolder.getBeanName();
        AbstractBeanDefinition serviceBeanDefinition = this.buildServiceBeanDefinition(service, interfaceClass, annotatedServiceBeanName);
        String beanName = this.generateServiceBeanName(service, interfaceClass, annotatedServiceBeanName);
        if (scanner.checkCandidate(beanName, (BeanDefinition)serviceBeanDefinition)) {
            registry.registerBeanDefinition(beanName, (BeanDefinition)serviceBeanDefinition);
            if (this.logger.isInfoEnabled()) {
                this.logger.warn("The BeanDefinition[" + (Object)serviceBeanDefinition + "] of ServiceBean has been registered with name : " + beanName);
            }
        } else if (this.logger.isWarnEnabled()) {
            this.logger.warn("The Duplicated BeanDefinition[" + (Object)serviceBeanDefinition + "] of ServiceBean[ bean name : " + beanName + "] was be found , Did @DubboComponentScan scan to same package in many times?");
        }
    }

    private Class<?> resolveServiceInterfaceClass(Class<?> annotatedServiceBeanClass, ExtensionService service) {
        Class<?>[] allInterfaces;
        Class interfaceClass = service.interfaceClass();
        if (Void.TYPE.equals((Object)interfaceClass)) {
            interfaceClass = null;
            String interfaceClassName = service.interfaceName();
            if (StringUtils.hasText((String)interfaceClassName) && ClassUtils.isPresent((String)interfaceClassName, (ClassLoader)this.classLoader)) {
                interfaceClass = ClassUtils.resolveClassName((String)interfaceClassName, (ClassLoader)this.classLoader);
            }
        }
        if (interfaceClass == null && (allInterfaces = annotatedServiceBeanClass.getInterfaces()).length > 0) {
            interfaceClass = allInterfaces[0];
        }
        Assert.notNull((Object)interfaceClass, (String)"@Service interfaceClass() or interfaceName() or interface class must be present!");
        Assert.isTrue((boolean)interfaceClass.isInterface(), (String)"The type that was annotated @Service is not an interface!");
        return interfaceClass;
    }

    private String generateServiceBeanName(Service service, Class<?> interfaceClass, String annotatedServiceBeanName) {
        String group;
        StringBuilder beanNameBuilder = new StringBuilder(ServiceBean.class.getSimpleName());
        beanNameBuilder.append(SEPARATOR).append(annotatedServiceBeanName);
        String interfaceClassName = interfaceClass.getName();
        beanNameBuilder.append(SEPARATOR).append(interfaceClassName);
        String version = service.version();
        if (StringUtils.hasText((String)version)) {
            beanNameBuilder.append(SEPARATOR).append(version);
        }
        if (StringUtils.hasText((String)(group = service.group()))) {
            beanNameBuilder.append(SEPARATOR).append(group);
        }
        return beanNameBuilder.toString();
    }

    private String generateServiceBeanName(com.youzan.api.rpc.annotation.Service service, Class<?> interfaceClass, String annotatedServiceBeanName) {
        String group;
        StringBuilder beanNameBuilder = new StringBuilder(ServiceBean.class.getSimpleName());
        beanNameBuilder.append(SEPARATOR).append(annotatedServiceBeanName);
        String interfaceClassName = interfaceClass.getName();
        beanNameBuilder.append(SEPARATOR).append(interfaceClassName);
        String version = service.version();
        if (StringUtils.hasText((String)version)) {
            beanNameBuilder.append(SEPARATOR).append(version);
        }
        if (StringUtils.hasText((String)(group = service.group()))) {
            beanNameBuilder.append(SEPARATOR).append(group);
        }
        return beanNameBuilder.toString();
    }

    private String generateServiceBeanName(ExtensionService service, Class<?> interfaceClass, String annotatedServiceBeanName) {
        String group;
        StringBuilder beanNameBuilder = new StringBuilder(ServiceBean.class.getSimpleName());
        beanNameBuilder.append(SEPARATOR).append(annotatedServiceBeanName);
        String interfaceClassName = interfaceClass.getName();
        beanNameBuilder.append(SEPARATOR).append(interfaceClassName);
        String version = service.tag();
        if (StringUtils.hasText((String)version)) {
            beanNameBuilder.append(SEPARATOR).append(version);
        }
        if (StringUtils.hasText((String)(group = service.value()))) {
            beanNameBuilder.append(SEPARATOR).append(group);
        }
        return beanNameBuilder.toString();
    }

    private Class<?> resolveServiceInterfaceClass(Class<?> annotatedServiceBeanClass, Service service) {
        Class<?>[] allInterfaces;
        Class interfaceClass = service.interfaceClass();
        if (Void.TYPE.equals(interfaceClass)) {
            interfaceClass = null;
            String interfaceClassName = service.interfaceName();
            if (StringUtils.hasText((String)interfaceClassName) && ClassUtils.isPresent((String)interfaceClassName, (ClassLoader)this.classLoader)) {
                interfaceClass = ClassUtils.resolveClassName((String)interfaceClassName, (ClassLoader)this.classLoader);
            }
        }
        if (interfaceClass == null && (allInterfaces = annotatedServiceBeanClass.getInterfaces()).length > 0) {
            interfaceClass = allInterfaces[0];
        }
        Assert.notNull(interfaceClass, (String)"@Service interfaceClass() or interfaceName() or interface class must be present!");
        Assert.isTrue((boolean)interfaceClass.isInterface(), (String)"The type that was annotated @Service is not an interface!");
        return interfaceClass;
    }

    private Class<?> resolveServiceInterfaceClass(Class<?> annotatedServiceBeanClass, com.youzan.api.rpc.annotation.Service service) {
        Class<?>[] allInterfaces;
        Class interfaceClass = service.interfaceClass();
        if (Void.TYPE.equals((Object)interfaceClass)) {
            interfaceClass = null;
            String interfaceClassName = service.interfaceName();
            if (StringUtils.hasText((String)interfaceClassName) && ClassUtils.isPresent((String)interfaceClassName, (ClassLoader)this.classLoader)) {
                interfaceClass = ClassUtils.resolveClassName((String)interfaceClassName, (ClassLoader)this.classLoader);
            }
        }
        if (interfaceClass == null && (allInterfaces = annotatedServiceBeanClass.getInterfaces()).length > 0) {
            interfaceClass = allInterfaces[0];
        }
        Assert.notNull((Object)interfaceClass, (String)"@Service interfaceClass() or interfaceName() or interface class must be present!");
        Assert.isTrue((boolean)interfaceClass.isInterface(), (String)"The type that was annotated @Service is not an interface!");
        return interfaceClass;
    }

    private Class<?> resolveClass(BeanDefinitionHolder beanDefinitionHolder) {
        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
        return this.resolveClass(beanDefinition);
    }

    private Class<?> resolveClass(BeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        return ClassUtils.resolveClassName((String)beanClassName, (ClassLoader)this.classLoader);
    }

    private Set<String> resolvePackagesToScan(Set<String> packagesToScan) {
        LinkedHashSet<String> resolvedPackagesToScan = new LinkedHashSet<String>(packagesToScan.size());
        for (String packageToScan : packagesToScan) {
            if (!StringUtils.hasText((String)packageToScan)) continue;
            String resolvedPackageToScan = this.environment.resolvePlaceholders(packageToScan.trim());
            resolvedPackagesToScan.add(resolvedPackageToScan);
        }
        return resolvedPackagesToScan;
    }

    private AbstractBeanDefinition buildServiceBeanDefinition(Service service, Class<?> interfaceClass, String annotatedServiceBeanName) {
        ManagedList<RuntimeBeanReference> registryRuntimeBeanReferences;
        String[] registryConfigBeanNames;
        ManagedList<RuntimeBeanReference> protocolRuntimeBeanReferences;
        String monitorConfigBeanName;
        String moduleConfigBeanName;
        String[] protocolConfigBeanNames;
        String applicationConfigBeanName;
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServiceBean.class);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        String[] ignoreAttributeNames = com.alibaba.dubbo.config.spring.util.ObjectUtils.of("provider", "monitor", "application", "module", "registry", "protocol", "interface");
        propertyValues.addPropertyValues((PropertyValues)new AnnotationPropertyValuesAdapter((Annotation)service, (PropertyResolver)this.environment, ignoreAttributeNames));
        this.addPropertyReference(builder, "ref", annotatedServiceBeanName);
        builder.addPropertyValue("interface", (Object)interfaceClass.getName());
        String providerConfigBeanName = service.provider();
        if (StringUtils.hasText((String)providerConfigBeanName)) {
            this.addPropertyReference(builder, "provider", providerConfigBeanName);
        }
        if (StringUtils.hasText((String)(monitorConfigBeanName = service.monitor()))) {
            this.addPropertyReference(builder, "monitor", monitorConfigBeanName);
        }
        if (StringUtils.hasText((String)(applicationConfigBeanName = service.application()))) {
            this.addPropertyReference(builder, "application", applicationConfigBeanName);
        }
        if (StringUtils.hasText((String)(moduleConfigBeanName = service.module()))) {
            this.addPropertyReference(builder, "module", moduleConfigBeanName);
        }
        if (!(registryRuntimeBeanReferences = this.toRuntimeBeanReferences(registryConfigBeanNames = service.registry())).isEmpty()) {
            builder.addPropertyValue("registries", registryRuntimeBeanReferences);
        }
        if (!(protocolRuntimeBeanReferences = this.toRuntimeBeanReferences(protocolConfigBeanNames = service.protocol())).isEmpty()) {
            builder.addPropertyValue("protocols", protocolRuntimeBeanReferences);
        }
        return builder.getBeanDefinition();
    }

    private AbstractBeanDefinition buildServiceBeanDefinition(com.youzan.api.rpc.annotation.Service service, Class<?> interfaceClass, String annotatedServiceBeanName) {
        ManagedList<RuntimeBeanReference> registryRuntimeBeanReferences;
        String[] registryConfigBeanNames;
        ManagedList<RuntimeBeanReference> protocolRuntimeBeanReferences;
        String monitorConfigBeanName;
        String moduleConfigBeanName;
        String[] protocolConfigBeanNames;
        String applicationConfigBeanName;
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServiceBean.class);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        String[] ignoreAttributeNames = com.alibaba.dubbo.config.spring.util.ObjectUtils.of("provider", "monitor", "application", "module", "registry", "protocol", "interface");
        propertyValues.addPropertyValues((PropertyValues)new AnnotationPropertyValuesAdapter((Annotation)service, (PropertyResolver)this.environment, ignoreAttributeNames));
        this.addPropertyReference(builder, "ref", annotatedServiceBeanName);
        builder.addPropertyValue("interface", (Object)interfaceClass.getName());
        String providerConfigBeanName = service.provider();
        if (StringUtils.hasText((String)providerConfigBeanName)) {
            this.addPropertyReference(builder, "provider", providerConfigBeanName);
        }
        if (StringUtils.hasText((String)(monitorConfigBeanName = service.monitor()))) {
            this.addPropertyReference(builder, "monitor", monitorConfigBeanName);
        }
        if (StringUtils.hasText((String)(applicationConfigBeanName = service.application()))) {
            this.addPropertyReference(builder, "application", applicationConfigBeanName);
        }
        if (StringUtils.hasText((String)(moduleConfigBeanName = service.module()))) {
            this.addPropertyReference(builder, "module", moduleConfigBeanName);
        }
        if (!(registryRuntimeBeanReferences = this.toRuntimeBeanReferences(registryConfigBeanNames = service.registry())).isEmpty()) {
            builder.addPropertyValue("registries", registryRuntimeBeanReferences);
        }
        if (!(protocolRuntimeBeanReferences = this.toRuntimeBeanReferences(protocolConfigBeanNames = service.protocol())).isEmpty()) {
            builder.addPropertyValue("protocols", protocolRuntimeBeanReferences);
        }
        return builder.getBeanDefinition();
    }

    private AbstractBeanDefinition buildServiceBeanDefinition(ExtensionService service, Class<?> interfaceClass, String annotatedServiceBeanName) {
        ManagedList<RuntimeBeanReference> registryRuntimeBeanReferences;
        String[] registryConfigBeanNames;
        ManagedList<RuntimeBeanReference> protocolRuntimeBeanReferences;
        String monitorConfigBeanName;
        String moduleConfigBeanName;
        String[] protocolConfigBeanNames;
        String providerConfigBeanName;
        String applicationConfigBeanName;
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServiceBean.class);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        String[] ignoreAttributeNames = com.alibaba.dubbo.config.spring.util.ObjectUtils.of("provider", "monitor", "application", "module", "registry", "protocol", "interface");
        propertyValues.addPropertyValues((PropertyValues)new AnnotationPropertyValuesAdapter((Annotation)service, (PropertyResolver)this.environment, ignoreAttributeNames));
        this.addPropertyReference(builder, "ref", annotatedServiceBeanName);
        builder.addPropertyValue("interface", (Object)interfaceClass.getName());
        builder.addPropertyValue("extensionService", (Object)"true");
        if ("default".equals(service.value())) {
            builder.addPropertyValue("group", (Object)"default");
        }
        if (StringUtils.hasText((String)(providerConfigBeanName = service.provider()))) {
            this.addPropertyReference(builder, "provider", providerConfigBeanName);
        }
        if (StringUtils.hasText((String)(monitorConfigBeanName = service.monitor()))) {
            this.addPropertyReference(builder, "monitor", monitorConfigBeanName);
        }
        if (StringUtils.hasText((String)(applicationConfigBeanName = service.application()))) {
            this.addPropertyReference(builder, "application", applicationConfigBeanName);
        }
        if (StringUtils.hasText((String)(moduleConfigBeanName = service.module()))) {
            this.addPropertyReference(builder, "module", moduleConfigBeanName);
        }
        if (!(registryRuntimeBeanReferences = this.toRuntimeBeanReferences(registryConfigBeanNames = service.registry())).isEmpty()) {
            builder.addPropertyValue("registries", registryRuntimeBeanReferences);
        }
        if (!(protocolRuntimeBeanReferences = this.toRuntimeBeanReferences(protocolConfigBeanNames = service.protocol())).isEmpty()) {
            builder.addPropertyValue("protocols", protocolRuntimeBeanReferences);
        }
        return builder.getBeanDefinition();
    }

    private /* varargs */ ManagedList<RuntimeBeanReference> toRuntimeBeanReferences(String ... beanNames) {
        ManagedList runtimeBeanReferences = new ManagedList();
        if (!ObjectUtils.isEmpty((Object[])beanNames)) {
            for (String beanName : beanNames) {
                String resolvedBeanName = this.environment.resolvePlaceholders(beanName);
                runtimeBeanReferences.add((Object)new RuntimeBeanReference(resolvedBeanName));
            }
        }
        return runtimeBeanReferences;
    }

    private void addPropertyReference(BeanDefinitionBuilder builder, String propertyName, String beanName) {
        String resolvedBeanName = this.environment.resolvePlaceholders(beanName);
        builder.addPropertyReference(propertyName, resolvedBeanName);
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}

