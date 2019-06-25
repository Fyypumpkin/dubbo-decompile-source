/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.bytecode.Wrapper;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.AbstractReferenceConfig;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.model.ApplicationModel;
import com.alibaba.dubbo.config.model.ConsumerModel;
import com.alibaba.dubbo.config.support.Parameter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.StaticContext;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.directory.StaticDirectory;
import com.alibaba.dubbo.rpc.cluster.support.ClusterUtils;
import com.alibaba.dubbo.rpc.protocol.injvm.InjvmProtocol;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class ReferenceConfig<T>
extends AbstractReferenceConfig {
    private static final long serialVersionUID = -5864351140409987595L;
    private static final Protocol refprotocol = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();
    private static final Cluster cluster = ExtensionLoader.getExtensionLoader(Cluster.class).getAdaptiveExtension();
    private static final ProxyFactory proxyFactory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();
    private String interfaceName;
    private Class<?> interfaceClass;
    private String client;
    private String url;
    private List<MethodConfig> methods;
    private ConsumerConfig consumer;
    private String protocol;
    private Boolean ssl;
    private String certificate;
    private String privateKey;
    private String keyPassword;
    private final List<URL> urls = new ArrayList<URL>();
    private volatile transient T ref;
    private volatile transient Invoker<?> invoker;
    private volatile transient boolean initialized;
    private volatile transient boolean destroyed;
    private final Object finalizerGuardian = new Object(){

        protected void finalize() throws Throwable {
            super.finalize();
            if (!ReferenceConfig.this.destroyed) {
                AbstractConfig.logger.warn("ReferenceConfig(" + ReferenceConfig.this.url + ") is not DESTROYED when FINALIZE");
            }
        }
    };

    public ReferenceConfig() {
    }

    public ReferenceConfig(Reference reference) {
        this.appendAnnotation(Reference.class, reference);
    }

    private static void checkAndConvertImplicitConfig(MethodConfig method, Map<String, String> map, Map<Object, Object> attributes) {
        String onThrowMethodKey;
        Object onInvokeMethod;
        Object onThrowMethod;
        String onInvokeMethodKey;
        if (Boolean.FALSE.equals(method.isReturn()) && (method.getOnreturn() != null || method.getOnthrow() != null)) {
            throw new IllegalStateException("method config error : return attribute must be set true when onreturn or onthrow has been setted.");
        }
        String onReturnMethodKey = StaticContext.getKey(map, method.getName(), "onreturn.method");
        Object onReturnMethod = attributes.get(onReturnMethodKey);
        if (onReturnMethod instanceof String) {
            attributes.put(onReturnMethodKey, ReferenceConfig.getMethodByName(method.getOnreturn().getClass(), onReturnMethod.toString()));
        }
        if ((onThrowMethod = attributes.get(onThrowMethodKey = StaticContext.getKey(map, method.getName(), "onthrow.method"))) instanceof String) {
            attributes.put(onThrowMethodKey, ReferenceConfig.getMethodByName(method.getOnthrow().getClass(), onThrowMethod.toString()));
        }
        if ((onInvokeMethod = attributes.get(onInvokeMethodKey = StaticContext.getKey(map, method.getName(), "oninvoke.method"))) instanceof String) {
            attributes.put(onInvokeMethodKey, ReferenceConfig.getMethodByName(method.getOninvoke().getClass(), onInvokeMethod.toString()));
        }
    }

    private static Method getMethodByName(Class<?> clazz, String methodName) {
        try {
            return ReflectUtils.findMethodByMethodName(clazz, methodName);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public URL toUrl() {
        return this.urls.isEmpty() ? null : this.urls.iterator().next();
    }

    public List<URL> toUrls() {
        return this.urls;
    }

    public synchronized T get() {
        if (this.destroyed) {
            throw new IllegalStateException("Already destroyed!");
        }
        if (this.ref == null) {
            this.init();
        }
        return this.ref;
    }

    public synchronized void destroy() {
        if (this.ref == null) {
            return;
        }
        if (this.destroyed) {
            return;
        }
        this.destroyed = true;
        try {
            this.invoker.destroy();
        }
        catch (Throwable t) {
            logger.warn("Unexpected err when destroy invoker of ReferenceConfig(" + this.url + ").", t);
        }
        this.invoker = null;
        this.ref = null;
    }

    private void init() {
        String hostToRegistry;
        if (this.initialized) {
            return;
        }
        if (this.interfaceName == null || this.interfaceName.length() == 0) {
            throw new IllegalStateException("<dubbo:reference interface=\"\" /> interface not allow null!");
        }
        this.initialized = true;
        this.checkDefault();
        ReferenceConfig.appendProperties(this);
        if (this.getGeneric() == null && this.getConsumer() != null) {
            this.setGeneric(this.getConsumer().getGeneric());
        }
        if (ProtocolUtils.isGeneric(this.getGeneric())) {
            this.interfaceClass = GenericService.class;
        } else {
            try {
                this.interfaceClass = Class.forName(this.interfaceName, true, Thread.currentThread().getContextClassLoader());
            }
            catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            this.checkInterfaceAndMethods(this.interfaceClass, this.methods);
        }
        String resolve = System.getProperty(this.interfaceName);
        String resolveFile = null;
        if (resolve == null || resolve.length() == 0) {
            File userResolveFile;
            resolveFile = System.getProperty("dubbo.resolve.file");
            if ((resolveFile == null || resolveFile.length() == 0) && (userResolveFile = new File(new File(System.getProperty("user.home")), "dubbo-resolve.properties")).exists()) {
                resolveFile = userResolveFile.getAbsolutePath();
            }
            if (resolveFile != null && resolveFile.length() > 0) {
                Properties properties;
                properties = new Properties();
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new File(resolveFile));
                    properties.load(fis);
                }
                catch (IOException e) {
                    throw new IllegalStateException("Unload " + resolveFile + ", cause: " + e.getMessage(), e);
                }
                finally {
                    try {
                        if (null != fis) {
                            fis.close();
                        }
                    }
                    catch (IOException e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
                resolve = properties.getProperty(this.interfaceName);
            }
        }
        if (resolve != null && resolve.length() > 0) {
            this.url = resolve;
            if (logger.isWarnEnabled()) {
                if (resolveFile != null) {
                    logger.warn("Using default dubbo resolve file " + resolveFile + " replace " + this.interfaceName + "" + resolve + " to p2p invoke remote service.");
                } else {
                    logger.warn("Using -D" + this.interfaceName + "=" + resolve + " to p2p invoke remote service.");
                }
            }
        }
        if (this.consumer != null) {
            if (this.application == null) {
                this.application = this.consumer.getApplication();
            }
            if (this.module == null) {
                this.module = this.consumer.getModule();
            }
            if (this.registries == null || this.registries.isEmpty()) {
                this.registries = this.consumer.getRegistries();
            }
            if (this.monitor == null) {
                this.monitor = this.consumer.getMonitor();
            }
        }
        if (this.module != null) {
            if (this.registries == null || this.registries.isEmpty()) {
                this.registries = this.module.getRegistries();
            }
            if (this.monitor == null) {
                this.monitor = this.module.getMonitor();
            }
        }
        if (this.application != null) {
            if (this.registries == null || this.registries.isEmpty()) {
                this.registries = this.application.getRegistries();
            }
            if (this.monitor == null) {
                this.monitor = this.application.getMonitor();
            }
        }
        this.checkApplication();
        this.checkStubAndMock(this.interfaceClass);
        HashMap<String, String> map = new HashMap<String, String>();
        HashMap<Object, Object> attributes = new HashMap<Object, Object>();
        map.put("side", "consumer");
        map.put("dubbo", Version.getVersion());
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        if (ConfigUtils.getPid() > 0) {
            map.put("pid", String.valueOf(ConfigUtils.getPid()));
        }
        if (!this.isGeneric().booleanValue()) {
            Object methods;
            String revision = Version.getVersion(this.interfaceClass, this.version);
            if (revision != null && revision.length() > 0) {
                map.put("revision", revision);
            }
            if (((String[])(methods = Wrapper.getWrapper(this.interfaceClass).getMethodNames())).length == 0) {
                logger.warn("NO method found in service interface " + this.interfaceClass.getName());
                map.put("methods", "*");
            } else {
                map.put("methods", StringUtils.join(new HashSet<String>(Arrays.asList(methods)), ","));
            }
        }
        map.put("interface", this.interfaceName);
        ReferenceConfig.appendParameters(map, this.application);
        ReferenceConfig.appendParameters(map, this.module);
        ReferenceConfig.appendParameters(map, this.consumer, "default");
        ReferenceConfig.appendParameters(map, this);
        ReferenceConfig.activeGlobalDatacenterIfNeed(map, this);
        ReferenceConfig.disableGenericRegisterIfNeed(map, this);
        String prefix = StringUtils.getServiceKey(map);
        if (this.methods != null && !this.methods.isEmpty()) {
            for (MethodConfig method : this.methods) {
                String retryValue;
                ReferenceConfig.appendParameters(map, method, method.getName());
                String retryKey = method.getName() + ".retry";
                if (map.containsKey(retryKey) && "false".equals(retryValue = (String)map.remove(retryKey))) {
                    map.put(method.getName() + ".retries", "0");
                }
                ReferenceConfig.appendAttributes(attributes, method, prefix + "." + method.getName());
                ReferenceConfig.checkAndConvertImplicitConfig(method, map, attributes);
            }
        }
        if ((hostToRegistry = ConfigUtils.getSystemProperty("DUBBO_IP_TO_REGISTRY")) == null || hostToRegistry.length() == 0) {
            hostToRegistry = NetUtils.getLocalHost();
        } else if (NetUtils.isInvalidLocalHost(hostToRegistry)) {
            throw new IllegalArgumentException("Specified invalid registry ip from property:DUBBO_IP_TO_REGISTRY, value:" + hostToRegistry);
        }
        map.put("register.ip", hostToRegistry);
        StaticContext.getSystemContext().putAll(attributes);
        this.ref = this.createProxy(map);
        ConsumerModel consumerModel = new ConsumerModel(this.getUniqueServiceName(), this, this.ref, this.interfaceClass.getMethods());
        ApplicationModel.initConsumerModel(this.getUniqueServiceName(), consumerModel);
    }

    private T createProxy(Map<String, String> map) {
        URL tmpUrl = new URL("temp", "localhost", 0, map);
        boolean isJvmRefer = this.isInjvm() == null ? (this.url != null && this.url.length() > 0 ? false : InjvmProtocol.getInjvmProtocol().isInjvmRefer(tmpUrl)) : this.isInjvm();
        if (isJvmRefer) {
            URL url = new URL("injvm", "127.0.0.1", 0, this.interfaceClass.getName()).addParameters(map);
            this.invoker = refprotocol.refer(this.interfaceClass, url);
            if (logger.isInfoEnabled()) {
                logger.info("Using injvm service " + this.interfaceClass.getName());
            }
        } else {
            String[] us;
            if (this.url != null && this.url.length() > 0) {
                us = Constants.SEMICOLON_SPLIT_PATTERN.split(this.url);
                if (us != null && us.length > 0) {
                    for (String u : us) {
                        URL url = URL.valueOf(u);
                        if (url.getPath() == null || url.getPath().length() == 0) {
                            url = url.setPath(this.interfaceName);
                        }
                        if ("registry".equals(url.getProtocol())) {
                            this.urls.add(url.addParameterAndEncoded("refer", StringUtils.toQueryString(map)));
                            continue;
                        }
                        this.urls.add(ClusterUtils.mergeUrl(url, map));
                    }
                }
            } else {
                us = this.loadRegistries(false);
                if (us != null && !us.isEmpty()) {
                    for (URL u : us) {
                        URL monitorUrl = this.loadMonitor(u);
                        if (monitorUrl != null) {
                            map.put("monitor", URL.encode(monitorUrl.toFullString()));
                        }
                        this.urls.add(u.addParameterAndEncoded("refer", StringUtils.toQueryString(map)));
                    }
                }
                if (this.urls.isEmpty()) {
                    throw new IllegalStateException("No such any registry to reference " + this.interfaceName + " on the consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + ", please config <dubbo:registry address=\"...\" /> to your spring config.");
                }
            }
            if (this.urls.size() == 1) {
                this.invoker = refprotocol.refer(this.interfaceClass, this.urls.get(0));
            } else {
                ArrayList invokers = new ArrayList();
                URL registryURL = null;
                for (URL url : this.urls) {
                    invokers.add(refprotocol.refer(this.interfaceClass, url));
                    if (!"registry".equals(url.getProtocol())) continue;
                    registryURL = url;
                }
                if (registryURL != null) {
                    URL u = registryURL.addParameter("cluster", "available");
                    this.invoker = cluster.join(new StaticDirectory(u, invokers));
                } else {
                    this.invoker = cluster.join(new StaticDirectory(invokers));
                }
            }
        }
        Boolean c = this.check;
        if (c == null && this.consumer != null) {
            c = this.consumer.isCheck();
        }
        if (c == null) {
            c = true;
        }
        if (c.booleanValue() && !this.invoker.isAvailable()) {
            throw new IllegalStateException("Failed to check the status of the service " + this.interfaceName + ". No provider available for the service " + (this.group == null ? "" : new StringBuilder().append(this.group).append("/").toString()) + this.interfaceName + (this.version == null ? "" : new StringBuilder().append(":").append(this.version).toString()) + " from the url " + this.invoker.getUrl() + " to the consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion());
        }
        if (logger.isInfoEnabled()) {
            logger.info("Refer dubbo service " + this.interfaceClass.getName() + " from url " + this.invoker.getUrl());
        }
        return (T)proxyFactory.getProxy(this.invoker);
    }

    private void checkDefault() {
        if (this.consumer == null) {
            this.consumer = new ConsumerConfig();
        }
        ReferenceConfig.appendProperties(this.consumer);
    }

    public Class<?> getInterfaceClass() {
        if (this.interfaceClass != null) {
            return this.interfaceClass;
        }
        if (this.isGeneric().booleanValue() || this.getConsumer() != null && this.getConsumer().isGeneric().booleanValue()) {
            return GenericService.class;
        }
        try {
            if (this.interfaceName != null && this.interfaceName.length() > 0) {
                this.interfaceClass = Class.forName(this.interfaceName, true, Thread.currentThread().getContextClassLoader());
            }
        }
        catch (ClassNotFoundException t) {
            throw new IllegalStateException(t.getMessage(), t);
        }
        return this.interfaceClass;
    }

    @Deprecated
    public void setInterfaceClass(Class<?> interfaceClass) {
        this.setInterface(interfaceClass);
    }

    public String getInterface() {
        return this.interfaceName;
    }

    public void setInterface(Class<?> interfaceClass) {
        if (interfaceClass != null && !interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        this.interfaceClass = interfaceClass;
        this.setInterface(interfaceClass == null ? null : interfaceClass.getName());
    }

    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
        if (this.id == null || this.id.length() == 0) {
            this.id = interfaceName;
        }
    }

    public String getClient() {
        return this.client;
    }

    public void setClient(String client) {
        ReferenceConfig.checkName("client", client);
        this.client = client;
    }

    @Parameter(excluded=true)
    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<MethodConfig> getMethods() {
        return this.methods;
    }

    public void setMethods(List<? extends MethodConfig> methods) {
        this.methods = methods;
    }

    public ConsumerConfig getConsumer() {
        return this.consumer;
    }

    public void setConsumer(ConsumerConfig consumer) {
        String defaultProtocol;
        this.consumer = consumer;
        if (consumer != null && StringUtils.isNotEmpty(defaultProtocol = consumer.getProtocol())) {
            this.setProtocol(defaultProtocol);
        }
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    Invoker<?> getInvoker() {
        return this.invoker;
    }

    @Parameter(excluded=true)
    public String getUniqueServiceName() {
        StringBuilder buf = new StringBuilder();
        if (this.group != null && this.group.length() > 0) {
            buf.append(this.group).append("/");
        }
        buf.append(this.interfaceName);
        if (this.version != null && this.version.length() > 0) {
            buf.append(":").append(this.version);
        }
        return buf.toString();
    }

    @Parameter(excluded=true)
    public String getCertificate() {
        return this.certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    @Parameter(excluded=true)
    public String getPrivateKey() {
        return this.privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Parameter(excluded=true)
    public String getKeyPassword() {
        return this.keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public Boolean isSsl() {
        return this.ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

}

