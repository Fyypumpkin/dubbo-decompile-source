/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.integration;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryFactory;
import com.alibaba.dubbo.registry.RegistryService;
import com.alibaba.dubbo.registry.integration.RegistryDirectory;
import com.alibaba.dubbo.registry.support.ProviderConsumerRegTable;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Configurator;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.protocol.InvokerWrapper;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class RegistryProtocol
implements Protocol {
    private Cluster cluster;
    private Protocol protocol;
    private RegistryFactory registryFactory;
    private ProxyFactory proxyFactory;
    private static RegistryProtocol INSTANCE;
    private final Map<URL, NotifyListener> overrideListeners = new ConcurrentHashMap<URL, NotifyListener>();
    private final Map<String, ExporterChangeableWrapper<?>> bounds = new ConcurrentHashMap();
    private static final Logger logger;

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public void setRegistryFactory(RegistryFactory registryFactory) {
        this.registryFactory = registryFactory;
    }

    public void setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    @Override
    public int getDefaultPort() {
        return 9090;
    }

    public RegistryProtocol() {
        INSTANCE = this;
    }

    public static RegistryProtocol getRegistryProtocol() {
        if (INSTANCE == null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension("registry");
        }
        return INSTANCE;
    }

    public Map<URL, NotifyListener> getOverrideListeners() {
        return this.overrideListeners;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T> Exporter<T> export(Invoker<T> originInvoker) throws RpcException {
        final ExporterChangeableWrapper<T> exporter = this.doLocalExport(originInvoker);
        final Registry registry = this.getRegistry(originInvoker);
        final URL registedProviderUrl = this.getRegistedProviderUrl(originInvoker);
        URL registryUrl = this.getRegistryUrl(originInvoker);
        boolean register = registedProviderUrl.getParameter("register", true);
        if (register) {
            if (this.shouldRegister(originInvoker.getUrl(), true)) {
                if (!registryUrl.getParameter("total", true) || ProviderConsumerRegTable.isAfterInitRegister()) {
                    Object object = ProviderConsumerRegTable.registerLock;
                    synchronized (object) {
                        ProviderConsumerRegTable.registerProvider(originInvoker, registryUrl, registedProviderUrl);
                        registry.register(registedProviderUrl);
                        ProviderConsumerRegTable.getProviderWrapper(originInvoker).setReg(true);
                        if (logger.isInfoEnabled()) {
                            logger.info("Register dubbo service " + registedProviderUrl.getParameter("interface") + " url: " + registedProviderUrl + " to registry " + registry.getUrl());
                        }
                    }
                } else {
                    ProviderConsumerRegTable.registerProvider(originInvoker, registryUrl, registedProviderUrl);
                    if (logger.isInfoEnabled()) {
                        logger.info("haven't Register dubbo service " + registedProviderUrl.getParameter("interface") + " url: " + registedProviderUrl + " to registry " + registry.getUrl() + " due to total register");
                    }
                }
            } else if (logger.isInfoEnabled()) {
                logger.info("Cancel register dubbo service " + registedProviderUrl.getParameter("interface") + " url: " + registedProviderUrl + " to registry " + registry.getUrl());
            }
        }
        final URL overrideSubscribeUrl = this.getSubscribedOverrideUrl(registedProviderUrl);
        final OverrideListener overrideSubscribeListener = new OverrideListener(overrideSubscribeUrl);
        this.overrideListeners.put(overrideSubscribeUrl, overrideSubscribeListener);
        if (!"haunt".equals(originInvoker.getUrl().getParameter("registry"))) {
            if (originInvoker.getUrl().getParameter("subscribe", true)) {
                registry.subscribe(overrideSubscribeUrl, overrideSubscribeListener);
            } else if (logger.isInfoEnabled()) {
                logger.info("Cancel subscribe dubbo service " + registedProviderUrl.getParameter("interface") + " url: " + registedProviderUrl + " to registry " + registry.getUrl());
            }
        }
        return new Exporter<T>(){

            @Override
            public Invoker<T> getInvoker() {
                return exporter.getInvoker();
            }

            @Override
            public void unexport() {
                try {
                    exporter.unexport();
                }
                catch (Throwable t) {
                    logger.warn(t.getMessage(), t);
                }
                try {
                    registry.unregister(registedProviderUrl);
                }
                catch (Throwable t) {
                    logger.warn(t.getMessage(), t);
                }
                try {
                    RegistryProtocol.this.overrideListeners.remove(overrideSubscribeUrl);
                    registry.unsubscribe(overrideSubscribeUrl, overrideSubscribeListener);
                }
                catch (Throwable t) {
                    logger.warn(t.getMessage(), t);
                }
            }
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private <T> ExporterChangeableWrapper<T> doLocalExport(Invoker<T> originInvoker) {
        String key = this.getCacheKey(originInvoker);
        ExporterChangeableWrapper<Object> exporter = this.bounds.get(key);
        if (exporter == null) {
            Map<String, ExporterChangeableWrapper<?>> map = this.bounds;
            synchronized (map) {
                exporter = this.bounds.get(key);
                if (exporter == null) {
                    InvokerDelegete<T> invokerDelegete = new InvokerDelegete<T>(originInvoker, this.getProviderUrl(originInvoker));
                    exporter = new ExporterChangeableWrapper<T>(this.protocol.export(invokerDelegete), originInvoker);
                    this.bounds.put(key, exporter);
                }
            }
        }
        return exporter;
    }

    private <T> void doChangeLocalExport(Invoker<T> originInvoker, URL newInvokerUrl) {
        String key = this.getCacheKey(originInvoker);
        ExporterChangeableWrapper<?> exporter = this.bounds.get(key);
        if (exporter == null) {
            logger.warn(new IllegalStateException("error state, exporter should not be null"));
            return;
        }
        InvokerDelegete<T> invokerDelegete = new InvokerDelegete<T>(originInvoker, newInvokerUrl);
        exporter.setExporter(this.protocol.export(invokerDelegete));
    }

    private Registry getRegistry(Invoker<?> originInvoker) {
        URL registryUrl = originInvoker.getUrl();
        if ("registry".equals(registryUrl.getProtocol())) {
            String protocol = registryUrl.getParameter("registry", "dubbo");
            registryUrl = registryUrl.setProtocol(protocol).removeParameter("registry");
        }
        return this.registryFactory.getRegistry(registryUrl);
    }

    private URL getRegistryUrl(Invoker<?> originInvoker) {
        URL registryUrl = originInvoker.getUrl();
        if ("registry".equals(registryUrl.getProtocol())) {
            String protocol = registryUrl.getParameter("registry", "dubbo");
            registryUrl = registryUrl.setProtocol(protocol).removeParameter("registry");
        }
        return registryUrl;
    }

    private URL getRegistedProviderUrl(Invoker<?> originInvoker) {
        URL providerUrl = this.getProviderUrl(originInvoker);
        URL registedProviderUrl = providerUrl.removeParameters(RegistryProtocol.getFilteredKeys(providerUrl)).removeParameter("monitor");
        return registedProviderUrl;
    }

    private URL getSubscribedOverrideUrl(URL registedProviderUrl) {
        return registedProviderUrl.setProtocol("provider").addParameters("category", "configurators", "check", String.valueOf(false));
    }

    private URL getProviderUrl(Invoker<?> origininvoker) {
        String export = origininvoker.getUrl().getParameterAndDecoded("export");
        if (export == null || export.length() == 0) {
            throw new IllegalArgumentException("The registry export url is null! registry: " + origininvoker.getUrl());
        }
        URL providerUrl = URL.valueOf(export);
        return providerUrl;
    }

    private String getCacheKey(Invoker<?> originInvoker) {
        URL providerUrl = this.getProviderUrl(originInvoker);
        String key = providerUrl.removeParameters("dynamic", "enabled").toFullString();
        return key;
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        url = url.setProtocol(url.getParameter("registry", "dubbo")).removeParameter("registry");
        Registry registry = this.registryFactory.getRegistry(url);
        if (RegistryService.class.equals(type)) {
            return this.proxyFactory.getInvoker(registry, type, url);
        }
        Map<String, String> qs = StringUtils.parseQueryString(url.getParameterAndDecoded("refer"));
        String group = qs.get("group");
        if (group != null && group.length() > 0 && (Constants.COMMA_SPLIT_PATTERN.split(group).length > 1 || "*".equals(group))) {
            return this.doRefer(this.getMergeableCluster(), registry, type, url);
        }
        return this.doRefer(this.cluster, registry, type, url);
    }

    private Cluster getMergeableCluster() {
        return ExtensionLoader.getExtensionLoader(Cluster.class).getExtension("mergeable");
    }

    private <T> Invoker<T> doRefer(Cluster cluster, Registry registry, Class<T> type, URL url) {
        RegistryDirectory<T> directory = new RegistryDirectory<T>(type, url);
        directory.setRegistry(registry);
        directory.setProtocol(this.protocol);
        HashMap<String, String> parameters = new HashMap<String, String>(directory.getUrl().getParameters());
        URL subscribeUrl = new URL("consumer", (String)parameters.remove("register.ip"), 0, type.getName(), directory.getUrl().getParameters());
        if (!"*".equals(url.getServiceInterface()) && url.getParameter("register", true)) {
            if (this.shouldRegister(url, false)) {
                directory.setRegisteredConsumerUrl(subscribeUrl.addParameters("category", "consumers", "check", String.valueOf(false)));
                registry.register(directory.getRegisteredConsumerUrl());
            } else if (logger.isInfoEnabled()) {
                logger.info("Cancel register dubbo reference " + type.getName() + " url: " + url + " to registry " + registry.getUrl());
            }
        }
        if (url.getParameter("subscribe", true)) {
            directory.subscribe(subscribeUrl.addParameter("category", "providers,configurators,routers"));
        } else if (logger.isInfoEnabled()) {
            logger.info("Cancel subscribe dubbo service " + type.getName() + " category: " + "providers" + "," + "configurators" + "," + "routers" + " url: " + url + " to registry " + registry.getUrl());
        }
        return cluster.join(directory);
    }

    private boolean shouldRegister(URL url, boolean provider) {
        if (!this.isRegisterGeneric(url, provider)) {
            return false;
        }
        String containsExclude = url.getParameter("exclude");
        if (containsExclude != null && !containsExclude.isEmpty()) {
            boolean exclude = Boolean.parseBoolean(containsExclude);
            return !exclude;
        }
        return provider || url.getParameter("subscribe", true);
    }

    private boolean isRegisterGeneric(URL url, boolean provider) {
        Map<String, String> qs = StringUtils.parseQueryString(url.getParameterAndDecoded(provider ? "export" : "refer"));
        boolean registerGeneric = true;
        boolean isGeneric = ProtocolUtils.isGeneric(qs.get("generic"));
        if (!isGeneric) {
            return true;
        }
        registerGeneric = provider || qs.get("generic.register") == null || Boolean.parseBoolean(qs.get("generic.register"));
        return registerGeneric;
    }

    private static String[] getFilteredKeys(URL url) {
        Map<String, String> params = url.getParameters();
        if (params != null && !params.isEmpty()) {
            ArrayList<String> filteredKeys = new ArrayList<String>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry == null || entry.getKey() == null || !entry.getKey().startsWith(".")) continue;
                filteredKeys.add(entry.getKey());
            }
            return filteredKeys.toArray(new String[filteredKeys.size()]);
        }
        return new String[0];
    }

    @Override
    public void destroy() {
        ArrayList exporters = new ArrayList(this.bounds.values());
        for (Exporter exporter : exporters) {
            exporter.unexport();
        }
        this.bounds.clear();
    }

    static {
        logger = LoggerFactory.getLogger(RegistryProtocol.class);
    }

    private class ExporterChangeableWrapper<T>
    implements Exporter<T> {
        private Exporter<T> exporter;
        private final Invoker<T> originInvoker;

        public ExporterChangeableWrapper(Exporter<T> exporter, Invoker<T> originInvoker) {
            this.exporter = exporter;
            this.originInvoker = originInvoker;
        }

        public Invoker<T> getOriginInvoker() {
            return this.originInvoker;
        }

        @Override
        public Invoker<T> getInvoker() {
            return this.exporter.getInvoker();
        }

        public void setExporter(Exporter<T> exporter) {
            this.exporter = exporter;
        }

        @Override
        public void unexport() {
            String key = RegistryProtocol.this.getCacheKey(this.originInvoker);
            RegistryProtocol.this.bounds.remove(key);
            this.exporter.unexport();
        }
    }

    public static class InvokerDelegete<T>
    extends InvokerWrapper<T> {
        private final Invoker<T> invoker;

        public InvokerDelegete(Invoker<T> invoker, URL url) {
            super(invoker, url);
            this.invoker = invoker;
        }

        public Invoker<T> getInvoker() {
            if (this.invoker instanceof InvokerDelegete) {
                return ((InvokerDelegete)this.invoker).getInvoker();
            }
            return this.invoker;
        }
    }

    private class OverrideListener
    implements NotifyListener {
        private volatile List<Configurator> configurators;
        private final URL subscribeUrl;

        public OverrideListener(URL subscribeUrl) {
            this.subscribeUrl = subscribeUrl;
        }

        @Override
        public void notify(List<URL> urls) {
            ArrayList<URL> result = null;
            Iterator<URL> iterator = urls.iterator();
            while (iterator.hasNext()) {
                URL url;
                URL overrideUrl = url = iterator.next();
                if (url.getParameter("category") == null && "override".equals(url.getProtocol())) {
                    overrideUrl = url.addParameter("category", "configurators");
                }
                if (UrlUtils.isMatch(this.subscribeUrl, overrideUrl)) continue;
                if (result == null) {
                    result = new ArrayList<URL>(urls);
                }
                result.remove(url);
                logger.warn("Subsribe category=configurator, but notifed non-configurator urls. may be registry bug. unexcepted url: " + url);
            }
            if (result != null) {
                urls = result;
            }
            this.configurators = RegistryDirectory.toConfigurators(urls);
            ArrayList exporters = new ArrayList(RegistryProtocol.this.bounds.values());
            for (ExporterChangeableWrapper exporter : exporters) {
                URL newUrl;
                Invoker invoker = exporter.getOriginInvoker();
                Invoker originInvoker = invoker instanceof InvokerDelegete ? ((InvokerDelegete)invoker).getInvoker() : invoker;
                URL originUrl = RegistryProtocol.this.getProviderUrl(originInvoker);
                if (originUrl.equals(newUrl = this.getNewInvokerUrl(originUrl, urls))) continue;
                RegistryProtocol.this.doChangeLocalExport(originInvoker, newUrl);
            }
        }

        private URL getNewInvokerUrl(URL url, List<URL> urls) {
            List<Configurator> localConfigurators = this.configurators;
            if (localConfigurators != null && localConfigurators.size() > 0) {
                for (Configurator configurator : localConfigurators) {
                    url = configurator.configure(url);
                }
            }
            return url;
        }
    }

}

