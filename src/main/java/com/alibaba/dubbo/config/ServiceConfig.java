/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.bytecode.Wrapper;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.AbstractServiceConfig;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ArgumentConfig;
import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.invoker.DelegateProviderMetaDataInvoker;
import com.alibaba.dubbo.config.model.ApplicationModel;
import com.alibaba.dubbo.config.model.ProviderModel;
import com.alibaba.dubbo.config.support.Parameter;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.ServiceClassHolder;
import com.alibaba.dubbo.rpc.cluster.Configurator;
import com.alibaba.dubbo.rpc.cluster.ConfiguratorFactory;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ServiceConfig<T>
extends AbstractServiceConfig {
    private static final long serialVersionUID = 3033787999037024738L;
    private static final Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();
    private static final ProxyFactory proxyFactory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();
    private static final Map<String, Integer> RANDOM_PORT_MAP = new HashMap<String, Integer>();
    private String interfaceName;
    private Class<?> interfaceClass;
    private T ref;
    private String path;
    private Boolean ssl;
    private String certificate;
    private String privateKey;
    private String keyPassword;
    private List<MethodConfig> methods;
    private static final ScheduledExecutorService delayExportExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("DubboServiceDelayExporter", true));
    private final List<URL> urls = new ArrayList<URL>();
    private final List<Exporter<?>> exporters = new ArrayList();
    private ProviderConfig provider;
    private volatile transient boolean exported;
    private volatile transient boolean unexported;
    private volatile String generic;
    private Boolean extensionService;

    public ServiceConfig() {
    }

    public ServiceConfig(Service service) {
        this.appendAnnotation(Service.class, service);
    }

    @Deprecated
    private static List<ProtocolConfig> convertProviderToProtocol(List<ProviderConfig> providers) {
        if (providers == null || providers.isEmpty()) {
            return null;
        }
        ArrayList<ProtocolConfig> protocols = new ArrayList<ProtocolConfig>(providers.size());
        for (ProviderConfig provider : providers) {
            protocols.add(ServiceConfig.convertProviderToProtocol(provider));
        }
        return protocols;
    }

    @Deprecated
    private static List<ProviderConfig> convertProtocolToProvider(List<ProtocolConfig> protocols) {
        if (protocols == null || protocols.isEmpty()) {
            return null;
        }
        ArrayList<ProviderConfig> providers = new ArrayList<ProviderConfig>(protocols.size());
        for (ProtocolConfig provider : protocols) {
            providers.add(ServiceConfig.convertProtocolToProvider(provider));
        }
        return providers;
    }

    @Deprecated
    private static ProtocolConfig convertProviderToProtocol(ProviderConfig provider) {
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setName(provider.getProtocol().getName());
        protocol.setServer(provider.getServer());
        protocol.setClient(provider.getClient());
        protocol.setCodec(provider.getCodec());
        protocol.setHost(provider.getHost());
        protocol.setPort(provider.getPort());
        protocol.setPath(provider.getPath());
        protocol.setPayload(provider.getPayload());
        protocol.setThreads(provider.getThreads());
        protocol.setParameters(provider.getParameters());
        return protocol;
    }

    @Deprecated
    private static ProviderConfig convertProtocolToProvider(ProtocolConfig protocol) {
        ProviderConfig provider = new ProviderConfig();
        provider.setProtocol(protocol);
        provider.setServer(protocol.getServer());
        provider.setClient(protocol.getClient());
        provider.setCodec(protocol.getCodec());
        provider.setHost(protocol.getHost());
        provider.setPort(protocol.getPort());
        provider.setPath(protocol.getPath());
        provider.setPayload(protocol.getPayload());
        provider.setThreads(protocol.getThreads());
        provider.setParameters(protocol.getParameters());
        return provider;
    }

    public URL toUrl() {
        return this.urls.isEmpty() ? null : this.urls.iterator().next();
    }

    public List<URL> toUrls() {
        return this.urls;
    }

    @Parameter(excluded=true)
    public boolean isExported() {
        return this.exported;
    }

    @Parameter(excluded=true)
    public boolean isUnexported() {
        return this.unexported;
    }

    public synchronized void export() {
        if (this.provider != null) {
            if (this.export == null) {
                this.export = this.provider.getExport();
            }
            if (this.delay == null) {
                this.delay = this.provider.getDelay();
            }
        }
        if (this.export != null && !this.export.booleanValue()) {
            return;
        }
        if (this.delay != null && this.delay > 0) {
            delayExportExecutor.schedule(() -> this.doExport(), (long)this.delay.intValue(), TimeUnit.MILLISECONDS);
        } else {
            this.doExport();
        }
    }

    protected synchronized void doExport() {
        if (this.unexported) {
            throw new IllegalStateException("Already unexported!");
        }
        if (this.exported) {
            return;
        }
        if (this.interfaceName == null || this.interfaceName.length() == 0) {
            throw new IllegalStateException("<dubbo:service interface=\"\" /> interface not allow null!");
        }
        this.exported = true;
        this.checkDefault();
        if (this.provider != null) {
            if (this.application == null) {
                this.application = this.provider.getApplication();
            }
            if (this.module == null) {
                this.module = this.provider.getModule();
            }
            if (this.registries == null || this.registries.isEmpty()) {
                this.registries = this.provider.getRegistries();
            }
            if (this.monitor == null) {
                this.monitor = this.provider.getMonitor();
            }
            if (this.protocols == null) {
                this.protocols = this.provider.getProtocols();
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
        if (this.ref instanceof GenericService) {
            this.interfaceClass = GenericService.class;
            if (StringUtils.isEmpty(this.generic)) {
                this.generic = Boolean.TRUE.toString();
            }
        } else {
            try {
                this.interfaceClass = Class.forName(this.interfaceName, true, Thread.currentThread().getContextClassLoader());
            }
            catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            this.checkInterfaceAndMethods(this.interfaceClass, this.methods);
            this.checkRef();
            this.generic = Boolean.FALSE.toString();
        }
        if (this.local != null) {
            Class<?> localClass;
            if ("true".equals(this.local)) {
                this.local = this.interfaceName + "Local";
            }
            try {
                localClass = ClassHelper.forNameWithThreadContextClassLoader(this.local);
            }
            catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            if (!this.interfaceClass.isAssignableFrom(localClass)) {
                throw new IllegalStateException("The local implementation class " + localClass.getName() + " not implement interface " + this.interfaceName);
            }
        }
        if (this.stub != null) {
            Class<?> stubClass;
            if ("true".equals(this.stub)) {
                this.stub = this.interfaceName + "Stub";
            }
            try {
                stubClass = ClassHelper.forNameWithThreadContextClassLoader(this.stub);
            }
            catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            if (!this.interfaceClass.isAssignableFrom(stubClass)) {
                throw new IllegalStateException("The stub implementation class " + stubClass.getName() + " not implement interface " + this.interfaceName);
            }
        }
        this.checkApplication();
        this.checkRegistry();
        this.checkProtocol();
        ServiceConfig.appendProperties(this);
        this.checkStubAndMock(this.interfaceClass);
        if (this.path == null || this.path.length() == 0) {
            this.path = this.interfaceName;
        }
        this.doExportUrls();
        ProviderModel providerModel = new ProviderModel(this.getUniqueServiceName(), this, this.ref);
        ApplicationModel.initProviderModel(this.getUniqueServiceName(), providerModel);
    }

    private void checkRef() {
        if (this.ref == null) {
            throw new IllegalStateException("ref not allow null!");
        }
        if (!this.interfaceClass.isInstance(this.ref)) {
            throw new IllegalStateException("The class " + this.ref.getClass().getName() + " unimplemented interface " + this.interfaceClass + "!");
        }
    }

    public synchronized void unexport() {
        if (!this.exported) {
            return;
        }
        if (this.unexported) {
            return;
        }
        if (!this.exporters.isEmpty()) {
            for (Exporter<?> exporter : this.exporters) {
                try {
                    exporter.unexport();
                }
                catch (Throwable t) {
                    logger.warn("unexpected err when unexport" + exporter, t);
                }
            }
            this.exporters.clear();
        }
        this.unexported = true;
    }

    private void doExportUrls() {
        List<URL> registryURLs = this.loadRegistries(true);
        for (ProtocolConfig protocolConfig : this.protocols) {
            this.doExportUrlsFor1Protocol(protocolConfig, registryURLs);
        }
    }

    private void doExportUrlsFor1Protocol(ProtocolConfig protocolConfig, List<URL> registryURLs) {
        String contextPath;
        String name = protocolConfig.getName();
        if (name == null || name.length() == 0) {
            name = "dubbo";
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("side", "provider");
        map.put("dubbo", Version.getVersion());
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        if (ConfigUtils.getPid() > 0) {
            map.put("pid", String.valueOf(ConfigUtils.getPid()));
        }
        ServiceConfig.appendParameters(map, this.application);
        ServiceConfig.appendParameters(map, this.module);
        ServiceConfig.appendParameters(map, this.provider, "default");
        ServiceConfig.appendParameters(map, protocolConfig);
        ServiceConfig.appendParameters(map, this);
        ServiceConfig.activeGlobalGroupIfNeed(map, this);
        ServiceConfig.activeGlobalDatacenterIfNeed(map, this);
        if (this.methods != null && !this.methods.isEmpty()) {
            for (MethodConfig method : this.methods) {
                String retryValue;
                List<ArgumentConfig> arguments;
                ServiceConfig.appendParameters(map, method, method.getName());
                String retryKey = method.getName() + ".retry";
                if (map.containsKey(retryKey) && "false".equals(retryValue = (String)map.remove(retryKey))) {
                    map.put(method.getName() + ".retries", "0");
                }
                if ((arguments = method.getArguments()) == null || arguments.isEmpty()) continue;
                for (ArgumentConfig argument : arguments) {
                    if (argument.getType() != null && argument.getType().length() > 0) {
                        Method[] methods = this.interfaceClass.getMethods();
                        if (methods == null || methods.length <= 0) continue;
                        for (int i = 0; i < methods.length; ++i) {
                            String methodName = methods[i].getName();
                            if (!methodName.equals(method.getName())) continue;
                            Class<?>[] argtypes = methods[i].getParameterTypes();
                            if (argument.getIndex() != -1) {
                                if (argtypes[argument.getIndex()].getName().equals(argument.getType())) {
                                    ServiceConfig.appendParameters(map, argument, method.getName() + "." + argument.getIndex());
                                    continue;
                                }
                                throw new IllegalArgumentException("argument config error : the index attribute and type attribute not match :index :" + argument.getIndex() + ", type:" + argument.getType());
                            }
                            for (int j = 0; j < argtypes.length; ++j) {
                                Class<?> argclazz = argtypes[j];
                                if (!argclazz.getName().equals(argument.getType())) continue;
                                ServiceConfig.appendParameters(map, argument, method.getName() + "." + j);
                                if (argument.getIndex() == -1 || argument.getIndex() == j) continue;
                                throw new IllegalArgumentException("argument config error : the index attribute and type attribute not match :index :" + argument.getIndex() + ", type:" + argument.getType());
                            }
                        }
                        continue;
                    }
                    if (argument.getIndex() != -1) {
                        ServiceConfig.appendParameters(map, argument, method.getName() + "." + argument.getIndex());
                        continue;
                    }
                    throw new IllegalArgumentException("argument config must set index or type attribute.eg: <dubbo:argument index='0' .../> or <dubbo:argument type=xxx .../>");
                }
            }
        }
        if (ProtocolUtils.isGeneric(this.generic)) {
            map.put("generic", this.generic);
            map.put("methods", "*");
        } else {
            String[] methods;
            String revision = Version.getVersion(this.interfaceClass, this.version);
            if (revision != null && revision.length() > 0) {
                map.put("revision", revision);
            }
            if ((methods = Wrapper.getWrapper(this.interfaceClass).getMethodNames()).length == 0) {
                logger.warn("NO method found in service interface " + this.interfaceClass.getName());
                map.put("methods", "*");
            } else {
                map.put("methods", StringUtils.join(new HashSet<String>(Arrays.asList(methods)), ","));
            }
        }
        if (!ConfigUtils.isEmpty(this.token)) {
            if (ConfigUtils.isDefault(this.token)) {
                map.put("token", UUID.randomUUID().toString());
            } else {
                map.put("token", this.token);
            }
        }
        if ("injvm".equals(protocolConfig.getName())) {
            protocolConfig.setRegister(false);
            map.put("notify", "false");
        }
        if (((contextPath = protocolConfig.getContextpath()) == null || contextPath.length() == 0) && this.provider != null) {
            contextPath = this.provider.getContextpath();
        }
        String host = this.findConfigedHosts(protocolConfig, registryURLs, map);
        Integer port = this.findConfigedPorts(protocolConfig, name, map);
        URL url = new URL(name, host, (int)port, (contextPath == null || contextPath.length() == 0 ? "" : new StringBuilder().append(contextPath).append("/").toString()) + this.path, map);
        if (ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class).hasExtension(url.getProtocol())) {
            url = ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class).getExtension(url.getProtocol()).getConfigurator(url).configure(url);
        }
        String scope = url.getParameter("scope");
        if (!"none".toString().equalsIgnoreCase(scope)) {
            if (!"remote".toString().equalsIgnoreCase(scope)) {
                this.exportLocal(url);
            }
            if (!"local".toString().equalsIgnoreCase(scope)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Export dubbo service " + this.interfaceClass.getName() + " to url " + url);
                }
                if (registryURLs != null && !registryURLs.isEmpty()) {
                    for (URL registryURL : registryURLs) {
                        url = url.addParameterIfAbsent("dynamic", registryURL.getParameter("dynamic"));
                        URL monitorUrl = this.loadMonitor(registryURL);
                        if (monitorUrl != null) {
                            url = url.addParameterAndEncoded("monitor", monitorUrl.toFullString());
                        }
                        Invoker<T> invoker = proxyFactory.getInvoker(this.ref, this.interfaceClass, registryURL.addParameterAndEncoded("export", url.toFullString()));
                        DelegateProviderMetaDataInvoker<T> wrapperInvoker = new DelegateProviderMetaDataInvoker<T>(invoker, this);
                        Exporter<T> exporter = protocol.export(wrapperInvoker);
                        this.exporters.add(exporter);
                    }
                } else {
                    Invoker<T> invoker = proxyFactory.getInvoker(this.ref, this.interfaceClass, url);
                    DelegateProviderMetaDataInvoker<T> wrapperInvoker = new DelegateProviderMetaDataInvoker<T>(invoker, this);
                    Exporter<T> exporter = protocol.export(wrapperInvoker);
                    this.exporters.add(exporter);
                }
            }
        }
        this.urls.add(url);
    }

    private void exportLocal(URL url) {
        if (!"injvm".equalsIgnoreCase(url.getProtocol())) {
            URL local = URL.valueOf(url.toFullString()).setProtocol("injvm").setHost("127.0.0.1").setPort(0);
            ServiceClassHolder.getInstance().pushServiceClass(this.getServiceClass(this.ref));
            Exporter<T> exporter = protocol.export(proxyFactory.getInvoker(this.ref, this.interfaceClass, local));
            this.exporters.add(exporter);
            logger.info("Export dubbo service " + this.interfaceClass.getName() + " to local registry");
        }
    }

    protected Class getServiceClass(T ref) {
        return ref.getClass();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String findConfigedHosts(ProtocolConfig protocolConfig, List<URL> registryURLs, Map<String, String> map) {
        boolean anyhost = false;
        String hostToBind = this.getValueFromConfig(protocolConfig, "DUBBO_IP_TO_BIND");
        if (hostToBind != null && hostToBind.length() > 0 && NetUtils.isInvalidLocalHost(hostToBind)) {
            throw new IllegalArgumentException("Specified invalid bind ip from property:DUBBO_IP_TO_BIND, value:" + hostToBind);
        }
        if (hostToBind == null || hostToBind.length() == 0) {
            hostToBind = protocolConfig.getHost();
            if (this.provider != null && (hostToBind == null || hostToBind.length() == 0)) {
                hostToBind = this.provider.getHost();
            }
            if (NetUtils.isInvalidLocalHost(hostToBind)) {
                anyhost = true;
                try {
                    hostToBind = InetAddress.getLocalHost().getHostAddress();
                }
                catch (UnknownHostException e) {
                    logger.warn(e.getMessage(), e);
                }
                if (NetUtils.isInvalidLocalHost(hostToBind)) {
                    if (registryURLs != null && !registryURLs.isEmpty()) {
                        for (URL registryURL : registryURLs) {
                            if ("multicast".equalsIgnoreCase(registryURL.getParameter("registry"))) continue;
                            try {
                                Socket socket = new Socket();
                                try {
                                    InetSocketAddress addr = new InetSocketAddress(registryURL.getHost(), registryURL.getPort());
                                    socket.connect(addr, 1000);
                                    hostToBind = socket.getLocalAddress().getHostAddress();
                                    break;
                                }
                                finally {
                                    try {
                                        socket.close();
                                    }
                                    catch (Throwable throwable) {}
                                }
                            }
                            catch (Exception e) {
                                logger.warn(e.getMessage(), e);
                            }
                        }
                    }
                    if (NetUtils.isInvalidLocalHost(hostToBind)) {
                        hostToBind = NetUtils.getLocalHost();
                    }
                }
            }
        }
        map.put("bind.ip", hostToBind);
        String hostToRegistry = this.getValueFromConfig(protocolConfig, "DUBBO_IP_TO_REGISTRY");
        if (hostToRegistry != null && hostToRegistry.length() > 0 && NetUtils.isInvalidLocalHost(hostToRegistry)) {
            throw new IllegalArgumentException("Specified invalid registry ip from property:DUBBO_IP_TO_REGISTRY, value:" + hostToRegistry);
        }
        if (hostToRegistry == null || hostToRegistry.length() == 0) {
            hostToRegistry = hostToBind;
        }
        map.put("anyhost", String.valueOf(anyhost));
        return hostToRegistry;
    }

    private Integer findConfigedPorts(ProtocolConfig protocolConfig, String name, Map<String, String> map) {
        Integer portToBind = null;
        String port = this.getValueFromConfig(protocolConfig, "DUBBO_PORT_TO_BIND");
        portToBind = this.parsePort(port);
        if (portToBind == null) {
            portToBind = protocolConfig.getPort();
            if (this.provider != null && (portToBind == null || portToBind == 0)) {
                portToBind = this.provider.getPort();
            }
            int defaultPort = ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(name).getDefaultPort();
            if (portToBind == null || portToBind == 0) {
                portToBind = defaultPort;
            }
            if (portToBind == null || portToBind <= 0) {
                portToBind = ServiceConfig.getRandomPort(name);
                if (portToBind == null || portToBind < 0) {
                    portToBind = NetUtils.getAvailablePort(defaultPort);
                    ServiceConfig.putRandomPort(name, portToBind);
                }
                logger.warn("Use random available port(" + portToBind + ") for protocol " + name);
            }
        }
        map.put("bind.port", String.valueOf(portToBind));
        String portToRegistryStr = this.getValueFromConfig(protocolConfig, "DUBBO_PORT_TO_REGISTRY");
        Integer portToRegistry = this.parsePort(portToRegistryStr);
        if (portToRegistry == null) {
            portToRegistry = portToBind;
        }
        return portToRegistry;
    }

    private Integer parsePort(String configPort) {
        Integer port = null;
        if (configPort != null && configPort.length() > 0) {
            try {
                Integer intPort = Integer.parseInt(configPort);
                if (NetUtils.isInvalidPort(intPort)) {
                    throw new IllegalArgumentException("Specified invalid port from env value:" + configPort);
                }
                port = intPort;
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Specified invalid port from env value:" + configPort);
            }
        }
        return port;
    }

    private String getValueFromConfig(ProtocolConfig protocolConfig, String key) {
        String protocolPrefix = protocolConfig.getName().toUpperCase() + "_";
        String port = ConfigUtils.getSystemProperty(protocolPrefix + key);
        if (port == null || port.length() == 0) {
            port = ConfigUtils.getSystemProperty(key);
        }
        return port;
    }

    private void checkDefault() {
        if (this.provider == null) {
            this.provider = new ProviderConfig();
        }
        ServiceConfig.appendProperties(this.provider);
    }

    private void checkProtocol() {
        if ((this.protocols == null || this.protocols.isEmpty()) && this.provider != null) {
            this.setProtocols(this.provider.getProtocols());
        }
        if (this.protocols == null || this.protocols.isEmpty()) {
            this.setProtocol(new ProtocolConfig());
        }
        for (ProtocolConfig protocolConfig : this.protocols) {
            if (StringUtils.isEmpty(protocolConfig.getName())) {
                protocolConfig.setName("dubbo");
            }
            ServiceConfig.appendProperties(protocolConfig);
        }
    }

    public Class<?> getInterfaceClass() {
        if (this.interfaceClass != null) {
            return this.interfaceClass;
        }
        if (this.ref instanceof GenericService) {
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

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.setInterface(interfaceClass);
    }

    public String getInterface() {
        return this.interfaceName;
    }

    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
        if (this.id == null || this.id.length() == 0) {
            this.id = interfaceName;
        }
    }

    public void setInterface(Class<?> interfaceClass) {
        if (interfaceClass != null && !interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        this.interfaceClass = interfaceClass;
        this.setInterface(interfaceClass == null ? null : interfaceClass.getName());
    }

    public T getRef() {
        return this.ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    @Parameter(excluded=true)
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        ServiceConfig.checkPathName("path", path);
        this.path = path;
    }

    public List<MethodConfig> getMethods() {
        return this.methods;
    }

    public void setMethods(List<? extends MethodConfig> methods) {
        this.methods = methods;
    }

    public ProviderConfig getProvider() {
        return this.provider;
    }

    public void setProvider(ProviderConfig provider) {
        this.provider = provider;
    }

    public String getGeneric() {
        return this.generic;
    }

    public void setGeneric(String generic) {
        if (StringUtils.isEmpty(generic)) {
            return;
        }
        if (!ProtocolUtils.isGeneric(generic)) {
            throw new IllegalArgumentException("Unsupported generic type " + generic);
        }
        this.generic = generic;
    }

    public List<URL> getExportedUrls() {
        return this.urls;
    }

    @Deprecated
    public List<ProviderConfig> getProviders() {
        return ServiceConfig.convertProtocolToProvider(this.protocols);
    }

    @Deprecated
    public void setProviders(List<ProviderConfig> providers) {
        this.protocols = ServiceConfig.convertProviderToProtocol(providers);
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

    public Boolean isExtensionService() {
        return this.extensionService;
    }

    public void setExtensionService(Boolean extensionService) {
        this.extensionService = extensionService;
    }

    private static Integer getRandomPort(String protocol) {
        if (RANDOM_PORT_MAP.containsKey(protocol = protocol.toLowerCase())) {
            return RANDOM_PORT_MAP.get(protocol);
        }
        return Integer.MIN_VALUE;
    }

    private static void putRandomPort(String protocol, Integer port) {
        if (!RANDOM_PORT_MAP.containsKey(protocol = protocol.toLowerCase())) {
            RANDOM_PORT_MAP.put(protocol, port);
        }
    }

    public Boolean isSsl() {
        return this.ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
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
}

