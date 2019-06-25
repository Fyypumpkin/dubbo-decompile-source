/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.config.AbstractMethodConfig;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.support.Parameter;
import com.alibaba.dubbo.config.utils.ReferenceConfigHelper;
import com.alibaba.dubbo.monitor.MonitorFactory;
import com.alibaba.dubbo.monitor.MonitorService;
import com.alibaba.dubbo.registry.RegistryFactory;
import com.alibaba.dubbo.registry.RegistryService;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.InvokerListener;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.support.MockInvoker;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public abstract class AbstractInterfaceConfig
extends AbstractMethodConfig {
    private static final long serialVersionUID = -1559314110797223229L;
    protected String local;
    protected String stub;
    protected MonitorConfig monitor;
    protected String proxy;
    protected String cluster;
    protected String filter;
    protected String listener;
    protected String owner;
    protected Integer connections;
    protected String layer;
    protected ApplicationConfig application;
    protected ModuleConfig module;
    protected List<RegistryConfig> registries;
    protected String onconnect;
    protected String ondisconnect;
    private Integer callbacks;
    private String scope;
    private String dc;
    private String servicechain;
    private String appversion;

    protected void checkRegistry() {
        Object address;
        if ((this.registries == null || this.registries.isEmpty()) && (address = ConfigUtils.getProperty("dubbo.registry.address")) != null && ((String)address).length() > 0) {
            String[] as;
            this.registries = new ArrayList<RegistryConfig>();
            for (String a : as = ((String)address).split("\\s*[|]+\\s*")) {
                RegistryConfig registryConfig = new RegistryConfig();
                registryConfig.setAddress(a);
                this.registries.add(registryConfig);
            }
        }
        if (this.registries == null || this.registries.isEmpty()) {
            throw new IllegalStateException((this.getClass().getSimpleName().startsWith("Reference") ? "No such any registry to refer service in consumer " : "No such any registry to export service in provider ") + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + ", Please add <dubbo:registry address=\"...\" /> to your spring config. If you want unregister, please set <dubbo:service registry=\"N/A\" />");
        }
        for (RegistryConfig registryConfig : this.registries) {
            AbstractInterfaceConfig.appendProperties(registryConfig);
        }
    }

    protected void checkApplication() {
        String applicationName;
        if (this.application == null && (applicationName = ConfigUtils.getProperty("dubbo.application.name")) != null && applicationName.length() > 0) {
            this.application = new ApplicationConfig();
        }
        if (this.application == null) {
            throw new IllegalStateException("No such application config! Please add <dubbo:application name=\"...\" /> to your spring config.");
        }
        AbstractInterfaceConfig.appendProperties(this.application);
        String wait = ConfigUtils.getProperty("dubbo.service.shutdown.wait");
        if (wait != null && wait.trim().length() > 0) {
            System.setProperty("dubbo.service.shutdown.wait", wait.trim());
        } else {
            wait = ConfigUtils.getProperty("dubbo.service.shutdown.wait.seconds");
            if (wait != null && wait.trim().length() > 0) {
                System.setProperty("dubbo.service.shutdown.wait.seconds", wait.trim());
            }
        }
    }

    protected List<URL> loadRegistries(boolean provider) {
        this.checkRegistry();
        ArrayList<URL> registryList = new ArrayList<URL>();
        if (this.registries != null && !this.registries.isEmpty()) {
            boolean containsHauntRegistry = false;
            boolean containsEtcd3Registry = false;
            for (RegistryConfig registry : this.registries) {
                if ("etcd3".equals(registry.getProtocol())) {
                    containsEtcd3Registry = true;
                }
                if (!"haunt".equals(registry.getProtocol())) continue;
                containsHauntRegistry = true;
            }
            if (containsHauntRegistry && !containsEtcd3Registry) {
                RegistryConfig etcdRegistry = ReferenceConfigHelper.findConfig(RegistryConfig.class, "etcd3");
                ArrayList<RegistryConfig> injectRegisties = new ArrayList<RegistryConfig>();
                if (etcdRegistry != null) {
                    injectRegisties.add(etcdRegistry);
                }
                injectRegisties.addAll(this.registries);
                this.registries = injectRegisties;
            }
            for (RegistryConfig config : this.registries) {
                String sysaddress;
                String address = config.getAddress();
                if (address == null || address.length() == 0) {
                    address = "0.0.0.0";
                }
                if ((sysaddress = System.getProperty("dubbo.registry.address")) != null && sysaddress.length() > 0) {
                    address = sysaddress;
                }
                if (address.length() <= 0 || "N/A".equalsIgnoreCase(address)) continue;
                HashMap<String, String> map = new HashMap<String, String>();
                AbstractInterfaceConfig.appendParameters(map, this.application);
                AbstractInterfaceConfig.appendParameters(map, config);
                map.put("path", RegistryService.class.getName());
                map.put("dubbo", Version.getVersion());
                map.put("timestamp", String.valueOf(System.currentTimeMillis()));
                if (ConfigUtils.getPid() > 0) {
                    map.put("pid", String.valueOf(ConfigUtils.getPid()));
                }
                if (!map.containsKey("protocol")) {
                    if (ExtensionLoader.getExtensionLoader(RegistryFactory.class).hasExtension("remote")) {
                        map.put("protocol", "remote");
                    } else {
                        map.put("protocol", "dubbo");
                    }
                }
                List<URL> urls = UrlUtils.parseURLs(address, map);
                for (int i = 0; i < urls.size(); ++i) {
                    URL url = urls.get(i);
                    url = url.addParameter("registry", url.getProtocol());
                    url = url.setProtocol("registry");
                    boolean subscribe = true;
                    boolean exclude = false;
                    if ("etcd3".equals(config.getProtocol())) {
                        List<Boolean> subscribes = StringUtils.parseBooleanArray(config.getSubscribes());
                        List<Set<String>> excludeApplications = StringUtils.parseSetArray(config.getExcludes());
                        subscribe = this.isSubscribe(subscribes, i);
                        exclude = this.isExclude(excludeApplications, url, i);
                    }
                    if ((!provider || !url.getParameter("register", true)) && (provider || !url.getParameter("subscribe", true))) continue;
                    if (!subscribe) {
                        url = url.addParameter("subscribe", Boolean.toString(subscribe));
                    }
                    if (exclude) {
                        url = url.addParameter("exclude", Boolean.toString(exclude));
                    }
                    registryList.add(url);
                }
            }
        }
        return registryList;
    }

    private boolean isSubscribe(List<Boolean> subscribes, int i) {
        boolean subscribe = true;
        try {
            subscribe = subscribes.size() == 1 ? subscribes.get(0).booleanValue() : subscribes.get(i).booleanValue();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return subscribe;
    }

    private boolean isExclude(List<Set<String>> excludeApplications, URL url, int i) {
        boolean exclude = false;
        try {
            Set<String> excludes;
            Set<String> set = excludes = excludeApplications.size() == 1 ? excludeApplications.get(0) : excludeApplications.get(i);
            if (excludes != null && !excludes.isEmpty()) {
                String application = url.getParameter("application");
                exclude = application != null && excludes.contains(application.toLowerCase());
            }
        }
        catch (Exception excludes) {
            // empty catch block
        }
        return exclude;
    }

    protected URL loadMonitor(URL registryURL) {
        if (this.monitor == null) {
            String monitorAddress = ConfigUtils.getProperty("dubbo.monitor.address");
            String monitorProtocol = ConfigUtils.getProperty("dubbo.monitor.protocol");
            if (!(monitorAddress != null && monitorAddress.length() != 0 || monitorProtocol != null && monitorProtocol.length() != 0)) {
                return null;
            }
            this.monitor = new MonitorConfig();
            if (monitorAddress != null && monitorAddress.length() > 0) {
                this.monitor.setAddress(monitorAddress);
            }
            if (monitorProtocol != null && monitorProtocol.length() > 0) {
                this.monitor.setProtocol(monitorProtocol);
            }
        }
        AbstractInterfaceConfig.appendProperties(this.monitor);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("interface", MonitorService.class.getName());
        map.put("dubbo", Version.getVersion());
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        if (ConfigUtils.getPid() > 0) {
            map.put("pid", String.valueOf(ConfigUtils.getPid()));
        }
        AbstractInterfaceConfig.appendParameters(map, this.monitor);
        String address = this.monitor.getAddress();
        String sysaddress = System.getProperty("dubbo.monitor.address");
        if (sysaddress != null && sysaddress.length() > 0) {
            address = sysaddress;
        }
        if (ConfigUtils.isNotEmpty(address)) {
            if (!map.containsKey("protocol")) {
                if (ExtensionLoader.getExtensionLoader(MonitorFactory.class).hasExtension("logstat")) {
                    map.put("protocol", "logstat");
                } else {
                    map.put("protocol", "dubbo");
                }
            }
            return UrlUtils.parseURL(address, map);
        }
        if ("registry".equals(this.monitor.getProtocol()) && registryURL != null) {
            return registryURL.setProtocol("dubbo").addParameter("protocol", "registry").addParameterAndEncoded("refer", StringUtils.toQueryString(map));
        }
        return null;
    }

    protected void checkInterfaceAndMethods(Class<?> interfaceClass, List<MethodConfig> methods) {
        if (interfaceClass == null) {
            throw new IllegalStateException("interface not allow null!");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        if (methods != null && !methods.isEmpty()) {
            for (MethodConfig methodBean : methods) {
                String methodName = methodBean.getName();
                if (methodName == null || methodName.length() == 0) {
                    throw new IllegalStateException("<dubbo:method> name attribute is required! Please check: <dubbo:service interface=\"" + interfaceClass.getName() + "\" ... ><dubbo:method name=\"\" ... /></<dubbo:reference>");
                }
                boolean hasMethod = false;
                for (Method method : interfaceClass.getMethods()) {
                    if (!method.getName().equals(methodName)) continue;
                    hasMethod = true;
                    break;
                }
                if (hasMethod) continue;
                throw new IllegalStateException("The interface " + interfaceClass.getName() + " not found method " + methodName);
            }
        }
    }

    protected void checkStubAndMock(Class<?> interfaceClass) {
        Class<?> localClass;
        if (ConfigUtils.isNotEmpty(this.local)) {
            Class<?> class_ = localClass = ConfigUtils.isDefault(this.local) ? ReflectUtils.forName(interfaceClass.getName() + "Local") : ReflectUtils.forName(this.local);
            if (!interfaceClass.isAssignableFrom(localClass)) {
                throw new IllegalStateException("The local implementation class " + localClass.getName() + " not implement interface " + interfaceClass.getName());
            }
            try {
                ReflectUtils.findConstructor(localClass, interfaceClass);
            }
            catch (NoSuchMethodException e) {
                throw new IllegalStateException("No such constructor \"public " + localClass.getSimpleName() + "(" + interfaceClass.getName() + ")\" in local implementation class " + localClass.getName());
            }
        }
        if (ConfigUtils.isNotEmpty(this.stub)) {
            Class<?> class_ = localClass = ConfigUtils.isDefault(this.stub) ? ReflectUtils.forName(interfaceClass.getName() + "Stub") : ReflectUtils.forName(this.stub);
            if (!interfaceClass.isAssignableFrom(localClass)) {
                throw new IllegalStateException("The local implementation class " + localClass.getName() + " not implement interface " + interfaceClass.getName());
            }
            try {
                ReflectUtils.findConstructor(localClass, interfaceClass);
            }
            catch (NoSuchMethodException e) {
                throw new IllegalStateException("No such constructor \"public " + localClass.getSimpleName() + "(" + interfaceClass.getName() + ")\" in local implementation class " + localClass.getName());
            }
        }
        if (ConfigUtils.isNotEmpty(this.mock)) {
            Class<?> mockClass;
            if (this.mock.startsWith("return ")) {
                String value = this.mock.substring("return ".length());
                try {
                    MockInvoker.parseMockValue(value);
                }
                catch (Exception e) {
                    throw new IllegalStateException("Illegal mock json value in <dubbo:service ... mock=\"" + this.mock + "\" />");
                }
            }
            Class<?> class_ = mockClass = ConfigUtils.isDefault(this.mock) ? ReflectUtils.forName(interfaceClass.getName() + "Mock") : ReflectUtils.forName(this.mock);
            if (!interfaceClass.isAssignableFrom(mockClass)) {
                throw new IllegalStateException("The mock implementation class " + mockClass.getName() + " not implement interface " + interfaceClass.getName());
            }
            try {
                mockClass.getConstructor(new Class[0]);
            }
            catch (NoSuchMethodException e) {
                throw new IllegalStateException("No such empty constructor \"public " + mockClass.getSimpleName() + "()\" in mock implementation class " + mockClass.getName());
            }
        }
    }

    @Deprecated
    public String getLocal() {
        return this.local;
    }

    @Deprecated
    public void setLocal(Boolean local) {
        if (local == null) {
            this.setLocal((String)null);
        } else {
            this.setLocal(String.valueOf(local));
        }
    }

    @Deprecated
    public void setLocal(String local) {
        AbstractInterfaceConfig.checkName("local", local);
        this.local = local;
    }

    public String getStub() {
        return this.stub;
    }

    public void setStub(Boolean stub) {
        if (stub == null) {
            this.setStub((String)null);
        } else {
            this.setStub(String.valueOf(stub));
        }
    }

    public void setStub(String stub) {
        AbstractInterfaceConfig.checkName("stub", stub);
        this.stub = stub;
    }

    public String getCluster() {
        return this.cluster;
    }

    public void setCluster(String cluster) {
        AbstractInterfaceConfig.checkExtension(Cluster.class, "cluster", cluster);
        this.cluster = cluster;
    }

    public String getProxy() {
        return this.proxy;
    }

    public void setProxy(String proxy) {
        AbstractInterfaceConfig.checkExtension(ProxyFactory.class, "proxy", proxy);
        this.proxy = proxy;
    }

    public Integer getConnections() {
        return this.connections;
    }

    public void setConnections(Integer connections) {
        this.connections = connections;
    }

    @Parameter(key="reference.filter", append=true)
    public String getFilter() {
        return this.filter;
    }

    public void setFilter(String filter) {
        AbstractInterfaceConfig.checkMultiExtension(Filter.class, "filter", filter);
        this.filter = filter;
    }

    @Parameter(key="invoker.listener", append=true)
    public String getListener() {
        return this.listener;
    }

    public void setListener(String listener) {
        AbstractInterfaceConfig.checkMultiExtension(InvokerListener.class, "listener", listener);
        this.listener = listener;
    }

    public String getLayer() {
        return this.layer;
    }

    public void setLayer(String layer) {
        AbstractInterfaceConfig.checkNameHasSymbol("layer", layer);
        this.layer = layer;
    }

    public ApplicationConfig getApplication() {
        return this.application;
    }

    public void setApplication(ApplicationConfig application) {
        this.application = application;
    }

    public ModuleConfig getModule() {
        return this.module;
    }

    public void setModule(ModuleConfig module) {
        this.module = module;
    }

    public RegistryConfig getRegistry() {
        return this.registries == null || this.registries.isEmpty() ? null : this.registries.get(0);
    }

    public void setRegistry(RegistryConfig registry) {
        ArrayList<RegistryConfig> registries = new ArrayList<RegistryConfig>(1);
        registries.add(registry);
        this.registries = registries;
    }

    public List<RegistryConfig> getRegistries() {
        return this.registries;
    }

    public void setRegistries(List<? extends RegistryConfig> registries) {
        ArrayList<RegistryConfig> _registries = new ArrayList<RegistryConfig>();
        _registries.addAll(registries);
        this.registries = _registries;
    }

    public MonitorConfig getMonitor() {
        return this.monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = new MonitorConfig(monitor);
    }

    public void setMonitor(MonitorConfig monitor) {
        this.monitor = monitor;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        AbstractInterfaceConfig.checkMultiName("owner", owner);
        this.owner = owner;
    }

    public Integer getCallbacks() {
        return this.callbacks;
    }

    public void setCallbacks(Integer callbacks) {
        this.callbacks = callbacks;
    }

    public String getOnconnect() {
        return this.onconnect;
    }

    public void setOnconnect(String onconnect) {
        this.onconnect = onconnect;
    }

    public String getOndisconnect() {
        return this.ondisconnect;
    }

    public void setOndisconnect(String ondisconnect) {
        this.ondisconnect = ondisconnect;
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getDc() {
        return this.dc;
    }

    public void setDc(String dc) {
        this.dc = dc;
    }

    public String getServicechain() {
        return this.servicechain;
    }

    public void setServicechain(String servicechain) {
        this.servicechain = servicechain;
    }

    public String getAppversion() {
        return this.appversion;
    }

    public void setAppversion(String appversion) {
        this.appversion = appversion;
    }
}

