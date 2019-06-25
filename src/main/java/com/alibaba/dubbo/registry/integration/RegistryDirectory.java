/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.integration;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Configurator;
import com.alibaba.dubbo.rpc.cluster.ConfiguratorFactory;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.RouterFactory;
import com.alibaba.dubbo.rpc.cluster.directory.AbstractDirectory;
import com.alibaba.dubbo.rpc.cluster.directory.StaticDirectory;
import com.alibaba.dubbo.rpc.cluster.support.ClusterUtils;
import com.alibaba.dubbo.rpc.protocol.InvokerWrapper;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class RegistryDirectory<T>
extends AbstractDirectory<T>
implements NotifyListener {
    private static final Logger logger = LoggerFactory.getLogger(RegistryDirectory.class);
    private static final Cluster cluster = ExtensionLoader.getExtensionLoader(Cluster.class).getAdaptiveExtension();
    private static final RouterFactory routerFactory = ExtensionLoader.getExtensionLoader(RouterFactory.class).getAdaptiveExtension();
    private static final ConfiguratorFactory configuratorFactory = ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class).getAdaptiveExtension();
    private Protocol protocol;
    private Registry registry;
    private final String serviceKey;
    private final Class<T> serviceType;
    private final Map<String, String> queryMap;
    private final URL directoryUrl;
    private final String[] serviceMethods;
    private final boolean multiGroup;
    private volatile boolean forbidden = false;
    private volatile URL overrideDirectoryUrl;
    private volatile List<Configurator> configurators;
    private volatile Map<String, Invoker<T>> urlInvokerMap;
    private volatile Map<String, List<Invoker<T>>> methodInvokerMap;
    private volatile Set<URL> cachedInvokerUrls;
    private volatile URL registeredConsumerUrl;

    public RegistryDirectory(Class<T> serviceType, URL url) {
        super(url);
        if (serviceType == null) {
            throw new IllegalArgumentException("service type is null.");
        }
        if (url.getServiceKey() == null || url.getServiceKey().length() == 0) {
            throw new IllegalArgumentException("registry serviceKey is null.");
        }
        this.serviceType = serviceType;
        this.serviceKey = url.getServiceKey();
        this.queryMap = StringUtils.parseQueryString(url.getParameterAndDecoded("refer"));
        this.overrideDirectoryUrl = this.directoryUrl = url.setPath(url.getServiceInterface()).clearParameters().addParameters(this.queryMap).removeParameter("monitor");
        String group = this.directoryUrl.getParameter("group", "");
        this.multiGroup = group != null && ("*".equals(group) || group.contains(","));
        String methods = this.queryMap.get("methods");
        this.serviceMethods = methods == null ? null : Constants.COMMA_SPLIT_PATTERN.split(methods);
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public void subscribe(URL url) {
        this.setConsumerUrl(url);
        this.registry.subscribe(url, this);
    }

    @Override
    public void destroy() {
        if (this.isDestroyed()) {
            return;
        }
        try {
            if (this.getRegisteredConsumerUrl() != null && this.registry != null && this.registry.isAvailable()) {
                this.registry.unregister(this.getRegisteredConsumerUrl());
            }
        }
        catch (Throwable t) {
            logger.warn("unexpected error when unregister service " + this.serviceKey + "from registry" + this.registry.getUrl(), t);
        }
        try {
            if (this.getConsumerUrl() != null && this.registry != null && this.registry.isAvailable()) {
                this.registry.unsubscribe(this.getConsumerUrl(), this);
            }
        }
        catch (Throwable t) {
            logger.warn("unexpeced error when unsubscribe service " + this.serviceKey + "from registry" + this.registry.getUrl(), t);
        }
        super.destroy();
        try {
            this.destroyAllInvokers();
        }
        catch (Throwable t) {
            logger.warn("Failed to destroy service " + this.serviceKey, t);
        }
    }

    @Override
    public synchronized void notify(List<URL> urls) {
        List<Router> routers;
        ArrayList<URL> invokerUrls = new ArrayList<URL>();
        ArrayList<URL> routerUrls = new ArrayList<URL>();
        ArrayList<URL> configuratorUrls = new ArrayList<URL>();
        for (URL url : urls) {
            String protocol = url.getProtocol();
            String category = url.getParameter("category", "providers");
            if ("routers".equals(category) || "route".equals(protocol)) {
                routerUrls.add(url);
                continue;
            }
            if ("configurators".equals(category) || "override".equals(protocol)) {
                configuratorUrls.add(url);
                continue;
            }
            if ("providers".equals(category)) {
                invokerUrls.add(url);
                continue;
            }
            logger.warn("Unsupported category " + category + " in notified url: " + url + " from registry " + this.getUrl().getAddress() + " to consumer " + NetUtils.getLocalHost());
        }
        if (configuratorUrls != null && configuratorUrls.size() > 0) {
            this.configurators = RegistryDirectory.toConfigurators(configuratorUrls);
        }
        if (routerUrls != null && routerUrls.size() > 0 && (routers = this.toRouters(routerUrls)) != null) {
            this.setRouters(routers);
        }
        List<Configurator> localConfigurators = this.configurators;
        this.overrideDirectoryUrl = this.directoryUrl;
        if (localConfigurators != null && localConfigurators.size() > 0) {
            for (Configurator configurator : localConfigurators) {
                this.overrideDirectoryUrl = configurator.configure(this.overrideDirectoryUrl);
            }
        }
        this.refreshInvoker(invokerUrls);
    }

    private void refreshInvoker(List<URL> invokerUrls) {
        if (invokerUrls != null && invokerUrls.size() == 1 && invokerUrls.get(0) != null && "empty".equals(invokerUrls.get(0).getProtocol())) {
            this.forbidden = true;
            this.methodInvokerMap = null;
            this.destroyAllInvokers();
        } else {
            this.forbidden = false;
            Map<String, Invoker<T>> oldUrlInvokerMap = this.urlInvokerMap;
            if (invokerUrls.size() == 0 && this.cachedInvokerUrls != null) {
                invokerUrls.addAll(this.cachedInvokerUrls);
            } else {
                this.cachedInvokerUrls = new HashSet<URL>();
                this.cachedInvokerUrls.addAll(invokerUrls);
            }
            if (invokerUrls.size() == 0) {
                return;
            }
            Map<String, Invoker<T>> newUrlInvokerMap = this.toInvokers(invokerUrls);
            Map<String, List<Invoker<T>>> newMethodInvokerMap = this.toMethodInvokers(newUrlInvokerMap);
            if (newUrlInvokerMap.isEmpty()) {
                logger.warn(new IllegalStateException("urls to invokers error .invokerUrls.size :" + invokerUrls.size() + ", invoker.size :0. urls :" + invokerUrls.toString()));
                return;
            }
            this.methodInvokerMap = this.multiGroup ? this.toMergeMethodInvokerMap(newMethodInvokerMap) : newMethodInvokerMap;
            this.urlInvokerMap = newUrlInvokerMap;
            try {
                this.destroyUnusedInvokers(oldUrlInvokerMap, newUrlInvokerMap);
            }
            catch (Exception e) {
                logger.warn("destroyUnusedInvokers error. ", e);
            }
        }
    }

    private Map<String, List<Invoker<T>>> toMergeMethodInvokerMap(Map<String, List<Invoker<T>>> methodMap) {
        HashMap<String, List<Invoker<T>>> result = new HashMap<String, List<Invoker<T>>>();
        for (Map.Entry<String, List<Invoker<T>>> entry : methodMap.entrySet()) {
            String method = entry.getKey();
            List<Invoker<T>> invokers = entry.getValue();
            HashMap<String, ArrayList<Object>> groupMap = new HashMap<String, ArrayList<Object>>();
            for (Invoker<T> invoker : invokers) {
                String group = invoker.getUrl().getParameter("group", "");
                ArrayList<Object> groupInvokers = (ArrayList<Object>)groupMap.get(group);
                if (groupInvokers == null) {
                    groupInvokers = new ArrayList<Object>();
                    groupMap.put(group, groupInvokers);
                }
                groupInvokers.add(invoker);
            }
            if (groupMap.size() == 1) {
                result.put(method, (List<Invoker<T>>)groupMap.values().iterator().next());
                continue;
            }
            if (groupMap.size() > 1) {
                Invoker<T> invoker;
                ArrayList groupInvokers = new ArrayList();
                invoker = groupMap.values().iterator();
                while (invoker.hasNext()) {
                    List groupList = (List)invoker.next();
                    groupInvokers.add(cluster.join(new StaticDirectory(groupList)));
                }
                result.put(method, groupInvokers);
                continue;
            }
            result.put(method, invokers);
        }
        return result;
    }

    public static List<Configurator> toConfigurators(List<URL> urls) {
        ArrayList<Configurator> configurators = new ArrayList<Configurator>(urls.size());
        if (urls == null || urls.size() == 0) {
            return configurators;
        }
        for (URL url : urls) {
            if ("empty".equals(url.getProtocol())) {
                configurators.clear();
                break;
            }
            HashMap<String, String> override = new HashMap<String, String>(url.getParameters());
            override.remove("anyhost");
            if (override.size() == 0) {
                configurators.clear();
                continue;
            }
            configurators.add(configuratorFactory.getConfigurator(url));
        }
        Collections.sort(configurators);
        return configurators;
    }

    private List<Router> toRouters(List<URL> urls) {
        ArrayList<Router> routers = new ArrayList<Router>();
        if (urls == null || urls.size() < 1) {
            return routers;
        }
        if (urls != null && urls.size() > 0) {
            for (URL url : urls) {
                if ("empty".equals(url.getProtocol())) continue;
                String routerType = url.getParameter("router");
                if (routerType != null && routerType.length() > 0) {
                    url = url.setProtocol(routerType);
                }
                try {
                    Router router = routerFactory.getRouter(url);
                    if (routers.contains(router)) continue;
                    routers.add(router);
                }
                catch (Throwable t) {
                    logger.error("convert router url to router error, url: " + url, t);
                }
            }
        }
        return routers;
    }

    private Map<String, Invoker<T>> toInvokers(List<URL> urls) {
        HashMap<String, Invoker<T>> newUrlInvokerMap = new HashMap<String, Invoker<T>>();
        if (urls == null || urls.size() == 0) {
            return newUrlInvokerMap;
        }
        HashSet<String> keys = new HashSet<String>();
        String queryProtocols = this.queryMap.get("protocol");
        for (URL providerUrl : urls) {
            Invoker<T> invoker;
            if (queryProtocols != null && queryProtocols.length() > 0) {
                String[] acceptProtocols;
                boolean accept = false;
                for (String acceptProtocol : acceptProtocols = queryProtocols.split(",")) {
                    if (!providerUrl.getProtocol().equals(acceptProtocol)) continue;
                    accept = true;
                    break;
                }
                if (!accept) continue;
            }
            if ("empty".equals(providerUrl.getProtocol())) continue;
            if (!ExtensionLoader.getExtensionLoader(Protocol.class).hasExtension(providerUrl.getProtocol())) {
                logger.error(new IllegalStateException("Unsupported protocol " + providerUrl.getProtocol() + " in notified url: " + providerUrl + " from registry " + this.getUrl().getAddress() + " to consumer " + NetUtils.getLocalHost() + ", supported protocol: " + ExtensionLoader.getExtensionLoader(Protocol.class).getSupportedExtensions()));
                continue;
            }
            URL url = this.mergeUrl(providerUrl);
            String key = url.toFullString();
            if (keys.contains(key)) continue;
            keys.add(key);
            Map<String, Invoker<T>> localUrlInvokerMap = this.urlInvokerMap;
            Invoker<T> invoker2 = invoker = localUrlInvokerMap == null ? null : localUrlInvokerMap.get(key);
            if (invoker == null) {
                try {
                    boolean enabled = true;
                    enabled = url.hasParameter("disabled") ? !url.getParameter("disabled", false) : url.getParameter("enabled", true);
                    if (enabled) {
                        invoker = new InvokerDelegete<T>(this.protocol.refer(this.serviceType, url), url, providerUrl);
                    }
                }
                catch (Throwable t) {
                    logger.error("Failed to refer invoker for interface:" + this.serviceType + ",url:(" + url + ")" + t.getMessage(), t);
                }
                if (invoker == null) continue;
                newUrlInvokerMap.put(key, invoker);
                continue;
            }
            newUrlInvokerMap.put(key, invoker);
        }
        keys.clear();
        return newUrlInvokerMap;
    }

    private URL mergeUrl(URL providerUrl) {
        String path;
        providerUrl = providerUrl.addParameter("provider.application", providerUrl.getParameter("application"));
        providerUrl = ClusterUtils.mergeUrl(providerUrl, this.queryMap);
        List<Configurator> localConfigurators = this.configurators;
        if (localConfigurators != null && localConfigurators.size() > 0) {
            for (Configurator configurator : localConfigurators) {
                providerUrl = configurator.configure(providerUrl);
            }
        }
        providerUrl = providerUrl.addParameter("check", String.valueOf(false));
        this.overrideDirectoryUrl = this.overrideDirectoryUrl.addParametersIfAbsent(providerUrl.getParameters());
        if ((providerUrl.getPath() == null || providerUrl.getPath().length() == 0) && ("dubbo".equals(providerUrl.getProtocol()) || "tether".equals(providerUrl.getProtocol())) && (path = this.directoryUrl.getParameter("interface")) != null) {
            int i = path.indexOf(47);
            if (i >= 0) {
                path = path.substring(i + 1);
            }
            if ((i = path.lastIndexOf(58)) >= 0) {
                path = path.substring(0, i);
            }
            providerUrl = providerUrl.setPath(path);
        }
        return providerUrl;
    }

    private Map<String, List<Invoker<T>>> toMethodInvokers(Map<String, Invoker<T>> invokersMap) {
        HashMap newMethodInvokerMap = new HashMap();
        ArrayList invokersList = new ArrayList();
        if (invokersMap != null && invokersMap.size() > 0) {
            for (Invoker<T> invoker : invokersMap.values()) {
                String[] methods;
                String parameter = invoker.getUrl().getParameter("methods");
                if (parameter != null && parameter.length() > 0 && (methods = Constants.COMMA_SPLIT_PATTERN.split(parameter)) != null && methods.length > 0) {
                    for (String method : methods) {
                        if (method == null || method.length() <= 0 || "*".equals(method)) continue;
                        ArrayList<Invoker<T>> methodInvokers = (ArrayList<Invoker<T>>)newMethodInvokerMap.get(method);
                        if (methodInvokers == null) {
                            methodInvokers = new ArrayList<Invoker<T>>();
                            newMethodInvokerMap.put(method, methodInvokers);
                        }
                        methodInvokers.add(invoker);
                    }
                }
                invokersList.add(invoker);
            }
        }
        List newInvokersList = this.route(invokersList, null);
        newMethodInvokerMap.put("*", newInvokersList);
        if (this.serviceMethods != null && this.serviceMethods.length > 0) {
            for (String method : this.serviceMethods) {
                List methodInvokers = (List)newMethodInvokerMap.get(method);
                if (methodInvokers == null || methodInvokers.size() == 0) {
                    methodInvokers = newInvokersList;
                }
                newMethodInvokerMap.put(method, this.route(methodInvokers, method));
            }
        }
        for (String method : new HashSet(newMethodInvokerMap.keySet())) {
            List methodInvokers = (List)newMethodInvokerMap.get(method);
            Collections.sort(methodInvokers, InvokerComparator.getComparator());
            newMethodInvokerMap.put(method, Collections.unmodifiableList(methodInvokers));
        }
        return Collections.unmodifiableMap(newMethodInvokerMap);
    }

    private void destroyAllInvokers() {
        Map<String, Invoker<T>> localUrlInvokerMap = this.urlInvokerMap;
        if (localUrlInvokerMap != null) {
            for (Invoker<T> invoker : new ArrayList<Invoker<T>>(localUrlInvokerMap.values())) {
                try {
                    invoker.destroy();
                }
                catch (Throwable t) {
                    logger.warn("Failed to destroy service " + this.serviceKey + " to provider " + invoker.getUrl(), t);
                }
            }
            localUrlInvokerMap.clear();
        }
        this.methodInvokerMap = null;
    }

    private void destroyUnusedInvokers(Map<String, Invoker<T>> oldUrlInvokerMap, Map<String, Invoker<T>> newUrlInvokerMap) {
        if (newUrlInvokerMap == null || newUrlInvokerMap.size() == 0) {
            this.destroyAllInvokers();
            return;
        }
        ArrayList<String> deleted = null;
        if (oldUrlInvokerMap != null) {
            Collection<Invoker<T>> newInvokers = newUrlInvokerMap.values();
            for (Map.Entry<String, Invoker<T>> entry : oldUrlInvokerMap.entrySet()) {
                if (newInvokers.contains(entry.getValue())) continue;
                if (deleted == null) {
                    deleted = new ArrayList<String>();
                }
                deleted.add(entry.getKey());
            }
        }
        if (deleted != null) {
            for (String url : deleted) {
                Invoker<T> invoker;
                if (url == null || (invoker = oldUrlInvokerMap.remove(url)) == null) continue;
                try {
                    invoker.destroy();
                    if (!logger.isDebugEnabled()) continue;
                    logger.debug("destory invoker[" + invoker.getUrl() + "] success. ");
                }
                catch (Exception e) {
                    logger.warn("destory invoker[" + invoker.getUrl() + "] faild. " + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<Invoker<T>> doList(Invocation invocation) {
        if (this.forbidden) {
            throw new RpcException(4, "No provider available from registry " + this.getUrl().getAddress() + " for service " + this.getConsumerUrl().getServiceKey() + " on consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + ", please check status of providers(disabled, not registered or in blacklist).");
        }
        List<Object> invokers = null;
        Map<String, List<Invoker<T>>> localMethodInvokerMap = this.methodInvokerMap;
        if (localMethodInvokerMap != null && localMethodInvokerMap.size() > 0) {
            Iterator<List<Invoker<T>>> iterator;
            String methodName = RpcUtils.getMethodName(invocation);
            Object[] args = RpcUtils.getArguments(invocation);
            if (args != null && args.length > 0 && args[0] != null && (args[0] instanceof String || args[0].getClass().isEnum())) {
                invokers = localMethodInvokerMap.get(methodName + "." + args[0]);
            }
            if (invokers == null) {
                invokers = localMethodInvokerMap.get(methodName);
            }
            if (invokers == null) {
                invokers = localMethodInvokerMap.get("*");
            }
            if (invokers == null && (iterator = localMethodInvokerMap.values().iterator()).hasNext()) {
                invokers = iterator.next();
            }
        }
        return invokers == null ? new ArrayList(0) : invokers;
    }

    @Override
    public Class<T> getInterface() {
        return this.serviceType;
    }

    @Override
    public URL getUrl() {
        return this.overrideDirectoryUrl;
    }

    public URL getRegisteredConsumerUrl() {
        return this.registeredConsumerUrl;
    }

    public void setRegisteredConsumerUrl(URL registeredConsumerUrl) {
        this.registeredConsumerUrl = registeredConsumerUrl;
    }

    @Override
    public boolean isAvailable() {
        if (this.isDestroyed()) {
            return false;
        }
        Map<String, Invoker<T>> localUrlInvokerMap = this.urlInvokerMap;
        if (localUrlInvokerMap != null && localUrlInvokerMap.size() > 0) {
            for (Invoker<T> invoker : new ArrayList<Invoker<T>>(localUrlInvokerMap.values())) {
                if (!invoker.isAvailable()) continue;
                return true;
            }
        }
        return false;
    }

    public Map<String, Invoker<T>> getUrlInvokerMap() {
        return this.urlInvokerMap;
    }

    public Map<String, List<Invoker<T>>> getMethodInvokerMap() {
        return this.methodInvokerMap;
    }

    public static class InvokerDelegete<T>
    extends InvokerWrapper<T> {
        private URL providerUrl;

        public InvokerDelegete(Invoker<T> invoker, URL url, URL providerUrl) {
            super(invoker, url);
            this.providerUrl = providerUrl;
        }

        public URL getProviderUrl() {
            return this.providerUrl;
        }
    }

    private static class InvokerComparator
    implements Comparator<Invoker<?>> {
        private static final InvokerComparator comparator = new InvokerComparator();

        public static InvokerComparator getComparator() {
            return comparator;
        }

        private InvokerComparator() {
        }

        @Override
        public int compare(Invoker<?> o1, Invoker<?> o2) {
            return o1.getUrl().toString().compareTo(o2.getUrl().toString());
        }
    }

}

