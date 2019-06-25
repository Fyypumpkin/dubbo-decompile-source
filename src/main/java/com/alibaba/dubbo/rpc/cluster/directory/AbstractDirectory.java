/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.directory;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.RouterFactory;
import com.alibaba.dubbo.rpc.cluster.router.MockInvokersSelector;
import com.alibaba.dubbo.rpc.cluster.router.datacenter.DataCenterRouter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class AbstractDirectory<T>
implements Directory<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDirectory.class);
    private final URL url;
    private volatile boolean destroyed = false;
    protected volatile URL consumerUrl;
    private volatile List<Router> routers;

    public AbstractDirectory(URL url) {
        this(url, null);
    }

    public AbstractDirectory(URL url, List<Router> routers) {
        this(url, url, routers);
    }

    public AbstractDirectory(URL url, URL consumerUrl, List<Router> routers) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        this.url = url;
        this.consumerUrl = consumerUrl;
        this.setRouters(routers);
    }

    @Override
    public List<Invoker<T>> list(Invocation invocation) throws RpcException {
        if (this.destroyed) {
            throw new RpcException("Directory already destroyed .url: " + this.getUrl());
        }
        List<Invoker<T>> invokers = this.doList(invocation);
        if (StringUtils.isEquals(Boolean.TRUE.toString(), invocation.getAttachment("invocation.skip.route"))) {
            return invokers;
        }
        List<Router> localRouters = this.routers;
        if (localRouters != null && !localRouters.isEmpty()) {
            for (Router router : localRouters) {
                try {
                    if (router.getUrl() != null && !router.getUrl().getParameter("runtime", false)) continue;
                    invokers = router.route(invokers, this.getConsumerUrl(), invocation);
                }
                catch (Throwable t) {
                    logger.error("Failed to execute router: " + this.getUrl() + ", cause: " + t.getMessage(), t);
                }
            }
        }
        return invokers;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    public List<Router> getRouters() {
        return this.routers;
    }

    public URL getConsumerUrl() {
        return this.consumerUrl;
    }

    public void setConsumerUrl(URL consumerUrl) {
        this.consumerUrl = consumerUrl;
    }

    protected void setRouters(List<Router> routers) {
        String defaultRouters;
        routers = routers == null ? new ArrayList<Router>() : new ArrayList<Router>(routers);
        String routerkey = this.url.getParameter("router");
        if (routerkey != null && routerkey.length() > 0) {
            RouterFactory routerFactory = ExtensionLoader.getExtensionLoader(RouterFactory.class).getExtension(routerkey);
            routers.add(routerFactory.getRouter(this.url));
        }
        routers.add(new MockInvokersSelector());
        Collections.sort(routers);
        Map<String, String> queryMap = StringUtils.parseQueryString(this.url.getParameterAndDecoded("refer"));
        if (null != queryMap && queryMap.containsKey("default.router") && StringUtils.isNotEmpty(defaultRouters = queryMap.get("default.router"))) {
            for (String defaultRouter : Constants.COMMA_SPLIT_PATTERN.split(defaultRouters)) {
                RouterFactory routerFactory = ExtensionLoader.getExtensionLoader(RouterFactory.class).getExtension(defaultRouter);
                routers.add(routerFactory.getRouter(this.url));
            }
        }
        if (this.detectDataCenterRouter()) {
            routers.add(new DataCenterRouter());
        }
        this.routers = routers;
    }

    protected List<Invoker<T>> route(List<Invoker<T>> invokers, String method) {
        if (invokers == null || invokers.isEmpty()) {
            return invokers;
        }
        RpcInvocation invocation = new RpcInvocation(method, new Class[0], new Object[0]);
        List<Router> routers = this.getRouters();
        if (routers != null) {
            for (Router router : routers) {
                if (router.getUrl() == null || router.getUrl().getParameter("runtime", false)) continue;
                invokers = router.route(invokers, this.getConsumerUrl(), invocation);
            }
        }
        return invokers;
    }

    private boolean detectDataCenterRouter() {
        String dataCenterKey = this.url.getParameter("dc");
        boolean enableDatacenterRouter = this.url.getParameter("dc.enable", true);
        return enableDatacenterRouter && (dataCenterKey != null && dataCenterKey.length() > 0 || StringUtils.containsParseKey(this.url.getParameterAndDecoded("refer"), "dc"));
    }

    public URL parseConsumerUrl(URL url) {
        URL invokerUrl = url.setProtocol(url.getParameter("registry", "dubbo")).removeParameter("registry");
        String refer = invokerUrl.getParameterAndDecoded("refer");
        if (refer != null && refer.length() > 0) {
            Map<String, String> parameters = StringUtils.parseQueryString(refer);
            parameters.remove("monitor");
            String path = parameters.remove("interface");
            if (!"*".equals(path)) {
                URL consumerUrl = new URL("consumer", parameters.remove("register.ip"), 0, path, parameters).addParameters("category", "consumers", "check", String.valueOf(false));
                return consumerUrl;
            }
        }
        return url;
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }

    @Override
    public void destroy() {
        this.destroyed = true;
    }

    protected abstract List<Invoker<T>> doList(Invocation var1) throws RpcException;
}

