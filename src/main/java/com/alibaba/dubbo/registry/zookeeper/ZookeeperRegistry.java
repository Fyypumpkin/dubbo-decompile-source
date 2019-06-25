/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.zookeeper;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import com.alibaba.dubbo.remoting.zookeeper.ChildListener;
import com.alibaba.dubbo.remoting.zookeeper.StateListener;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperClient;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperTransporter;
import com.alibaba.dubbo.rpc.RpcException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZookeeperRegistry
extends FailbackRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class);
    private static final int DEFAULT_ZOOKEEPER_PORT = 2181;
    private static final String DEFAULT_ROOT = "dubbo";
    private final String root;
    private final Set<String> anyServices = new ConcurrentHashSet<String>();
    private final ConcurrentMap<URL, ConcurrentMap<NotifyListener, ChildListener>> zkListeners = new ConcurrentHashMap<URL, ConcurrentMap<NotifyListener, ChildListener>>();
    private final ZookeeperClient zkClient;

    public ZookeeperRegistry(URL url, ZookeeperTransporter zookeeperTransporter) {
        super(url);
        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null");
        }
        String group = url.getParameter("group", DEFAULT_ROOT);
        if (!group.startsWith("/")) {
            group = "/" + group;
        }
        this.root = group;
        this.zkClient = zookeeperTransporter.connect(url);
        this.zkClient.addStateListener(new StateListener(){

            @Override
            public void stateChanged(int state) {
                if (state == 2) {
                    try {
                        ZookeeperRegistry.this.recover();
                    }
                    catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

    @Override
    public boolean isAvailable() {
        return this.zkClient.isConnected();
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            this.zkClient.close();
        }
        catch (Exception e) {
            logger.warn("Failed to close zookeeper client " + this.getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doRegister(URL url) {
        try {
            this.zkClient.create(this.toUrlPath(url), url.getParameter("dynamic", true));
        }
        catch (Throwable e) {
            throw new RpcException("Failed to register " + url + " to zookeeper " + this.getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doUnregister(URL url) {
        try {
            this.zkClient.delete(this.toUrlPath(url));
        }
        catch (Throwable e) {
            throw new RpcException("Failed to unregister " + url + " to zookeeper " + this.getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doSubscribe(final URL url, final NotifyListener listener) {
        try {
            if ("*".equals(url.getServiceInterface())) {
                ChildListener zkListener;
                String root = this.toRootPath();
                ConcurrentMap listeners = (ConcurrentMap)this.zkListeners.get(url);
                if (listeners == null) {
                    this.zkListeners.putIfAbsent(url, new ConcurrentHashMap());
                    listeners = (ConcurrentMap)this.zkListeners.get(url);
                }
                if ((zkListener = (ChildListener)listeners.get(listener)) == null) {
                    listeners.putIfAbsent(listener, new ChildListener(){

                        @Override
                        public void childChanged(String parentPath, List<String> currentChilds) {
                            for (String child : currentChilds) {
                                child = URL.decode(child);
                                if (ZookeeperRegistry.this.anyServices.contains(child)) continue;
                                ZookeeperRegistry.this.anyServices.add(child);
                                ZookeeperRegistry.this.subscribe(url.setPath(child).addParameters("interface", child, "check", String.valueOf(false)), listener);
                            }
                        }
                    });
                    zkListener = (ChildListener)listeners.get(listener);
                }
                this.zkClient.create(root, false);
                List<String> services = this.zkClient.addChildListener(root, zkListener);
                if (services != null && services.size() > 0) {
                    for (String service : services) {
                        service = URL.decode(service);
                        this.anyServices.add(service);
                        this.subscribe(url.setPath(service).addParameters("interface", service, "check", String.valueOf(false)), listener);
                    }
                }
            } else {
                ArrayList<URL> urls = new ArrayList<URL>();
                for (String path : this.toCategoriesPath(url)) {
                    ChildListener zkListener;
                    ConcurrentMap listeners = (ConcurrentMap)this.zkListeners.get(url);
                    if (listeners == null) {
                        this.zkListeners.putIfAbsent(url, new ConcurrentHashMap());
                        listeners = (ConcurrentMap)this.zkListeners.get(url);
                    }
                    if ((zkListener = (ChildListener)listeners.get(listener)) == null) {
                        listeners.putIfAbsent(listener, new ChildListener(){

                            @Override
                            public void childChanged(String parentPath, List<String> currentChilds) {
                                ZookeeperRegistry.this.notify(url, listener, ZookeeperRegistry.this.toUrlsWithEmpty(url, parentPath, currentChilds));
                            }
                        });
                        zkListener = (ChildListener)listeners.get(listener);
                    }
                    this.zkClient.create(path, false);
                    List<String> children = this.zkClient.addChildListener(path, zkListener);
                    if (children == null) continue;
                    urls.addAll(this.toUrlsWithEmpty(url, path, children));
                }
                this.notify(url, listener, urls);
            }
        }
        catch (Throwable e) {
            throw new RpcException("Failed to subscribe " + url + " to zookeeper " + this.getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doUnsubscribe(URL url, NotifyListener listener) {
        ChildListener zkListener;
        ConcurrentMap listeners = (ConcurrentMap)this.zkListeners.get(url);
        if (listeners != null && (zkListener = (ChildListener)listeners.get(listener)) != null) {
            this.zkClient.removeChildListener(this.toUrlPath(url), zkListener);
        }
    }

    @Override
    public List<URL> lookup(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("lookup url == null");
        }
        try {
            ArrayList<String> providers = new ArrayList<String>();
            for (String path : this.toCategoriesPath(url)) {
                List<String> children = this.zkClient.getChildren(path);
                if (children == null) continue;
                providers.addAll(children);
            }
            return this.toUrlsWithoutEmpty(url, providers);
        }
        catch (Throwable e) {
            throw new RpcException("Failed to lookup " + url + " from zookeeper " + this.getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    private String toRootDir() {
        if (this.root.equals("/")) {
            return this.root;
        }
        return this.root + "/";
    }

    private String toRootPath() {
        return this.root;
    }

    private String toServicePath(URL url) {
        String name = url.getServiceInterface();
        if ("*".equals(name)) {
            return this.toRootPath();
        }
        return this.toRootDir() + URL.encode(name);
    }

    private String[] toCategoriesPath(URL url) {
        String[] categroies = "*".equals(url.getParameter("category")) ? new String[]{"providers", "consumers", "routers", "configurators"} : url.getParameter("category", new String[]{"providers"});
        String[] paths = new String[categroies.length];
        for (int i = 0; i < categroies.length; ++i) {
            paths[i] = this.toServicePath(url) + "/" + categroies[i];
        }
        return paths;
    }

    private String toCategoryPath(URL url) {
        return this.toServicePath(url) + "/" + url.getParameter("category", "providers");
    }

    private String toUrlPath(URL url) {
        return this.toCategoryPath(url) + "/" + URL.encode(url.toFullString());
    }

    private List<URL> toUrlsWithoutEmpty(URL consumer, List<String> providers) {
        ArrayList<URL> urls = new ArrayList<URL>();
        if (providers != null && providers.size() > 0) {
            for (String provider : providers) {
                URL url;
                if (!(provider = URL.decode(provider)).contains("://") || !UrlUtils.isMatch(consumer, url = URL.valueOf(provider))) continue;
                urls.add(url);
            }
        }
        return urls;
    }

    private List<URL> toUrlsWithEmpty(URL consumer, String path, List<String> providers) {
        List<URL> urls = this.toUrlsWithoutEmpty(consumer, providers);
        if (urls.isEmpty()) {
            int i = path.lastIndexOf(47);
            String category = i < 0 ? path : path.substring(i + 1);
            URL empty = consumer.setProtocol("empty").addParameter("category", category);
            urls.add(empty);
        }
        return urls;
    }

    static String appendDefaultPort(String address) {
        if (address != null && address.length() > 0) {
            int i = address.indexOf(58);
            if (i < 0) {
                return address + ":" + 2181;
            }
            if (Integer.parseInt(address.substring(i + 1)) == 0) {
                return address.substring(0, i + 1) + 2181;
            }
        }
        return address;
    }

}

