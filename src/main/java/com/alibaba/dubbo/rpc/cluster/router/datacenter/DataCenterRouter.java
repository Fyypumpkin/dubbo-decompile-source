/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.router.datacenter;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.router.condition.ConditionRouter;
import com.alibaba.dubbo.rpc.cluster.support.wrapper.MockClusterInvoker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataCenterRouter
extends ConditionRouter
implements Router {
    private static final Logger logger = LoggerFactory.getLogger(DataCenterRouter.class);
    public static final URL ROUTER_URL = new URL("condition", "0.0.0.0", 0, "*").addParameters("rule", URL.encode("=> dc = $dc & methods = $methods"), "runtime", "true");

    public DataCenterRouter() {
        this(ROUTER_URL);
    }

    private DataCenterRouter(URL url) {
        super(ROUTER_URL);
    }

    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if (invokers == null || invokers.isEmpty()) {
            return invokers;
        }
        try {
            if (!this.matchWhen(url, invocation)) {
                return invokers;
            }
            ArrayList<Invoker<T>> result = new ArrayList<Invoker<T>>();
            for (Invoker<T> invoker : invokers) {
                if (!this.matchThen(invoker.getUrl(), url, invocation)) continue;
                result.add(invoker);
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        catch (Throwable t) {
            logger.error("Failed to execute datacenter router rule: " + this.getUrl() + ", invokers: " + invokers + ", consumer datacenter: " + url.getParameter("dc", "global") + ", cause: " + t.getMessage(), t);
        }
        return this.filterInvokers(invokers, url, invocation);
    }

    public boolean matchThen(URL url, URL param, Invocation invocation) {
        return this.thenCondition != null && !this.thenCondition.isEmpty() && this.matchCondition(this.thenCondition, url, param, invocation);
    }

    @Override
    public boolean matchCondition(Map<String, ConditionRouter.MatchPair> condition, URL url, URL param, Invocation inv) {
        Map<String, String> sample = url.toMap();
        boolean matched = false;
        for (Map.Entry<String, ConditionRouter.MatchPair> matchPair : condition.entrySet()) {
            String sampleValue;
            boolean providerCondition;
            String key = matchPair.getKey();
            URL consumerUrl = param == null ? url : param;
            boolean bl = providerCondition = "methods".equals(key) && consumerUrl != url;
            if (inv != null && ("method".equals(key) || "methods".equals(key))) {
                sampleValue = inv.getMethodName();
                if (sampleValue == null) {
                    return false;
                }
                if (providerCondition) {
                    String serviceMethod;
                    if ((inv.getMethodName().equals("$invoke") || inv.getMethodName().equals("$invokeWithJsonArgs")) && inv.getArguments() != null && inv.getArguments().length == 3) {
                        sampleValue = ((String)inv.getArguments()[0]).trim();
                    }
                    if (matched = this.strictMatch(serviceMethod = sample.get(key), sampleValue)) continue;
                    return false;
                }
            }
            if ((sampleValue = sample.get(key)) == null) {
                sampleValue = sample.get("default." + key);
            }
            if (!matchPair.getValue().isMatch(sampleValue, consumerUrl)) {
                if ("dc".equals(key)) {
                    String dc = consumerUrl.getParameter("dc");
                    boolean bl2 = matched = "global".equals(dc) || "default".equals(dc) || sampleValue == null || "global".equals(sampleValue) || "default".equals(sampleValue);
                    if (matched) continue;
                }
                return false;
            }
            matched = true;
        }
        return matched;
    }

    private <T> List<Invoker<T>> filterInvokers(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        for (Invoker<T> invoker : invokers) {
            if (invoker instanceof MockClusterInvoker) continue;
            return invokers;
        }
        ArrayList<Invoker<T>> filteredInvokers = new ArrayList<Invoker<T>>();
        ArrayList<Invoker<T>> clusterInvokers = new ArrayList<Invoker<T>>();
        URL consumerUrl = this.parseConsumerUrl(url);
        for (int i = invokers.size() - 1; i >= 0; --i) {
            Invoker<T> invoker = invokers.get(i);
            if (!(invoker instanceof MockClusterInvoker)) continue;
            List foundInvokers = ((MockClusterInvoker)invoker).findInvokers(invocation);
            boolean matched = false;
            for (Invoker found : foundInvokers) {
                matched = this.matchThen(found.getUrl(), consumerUrl, invocation);
                if (!matched) continue;
                filteredInvokers.add(invoker);
                break;
            }
            if (matched) continue;
            clusterInvokers.add(invoker);
        }
        if (!clusterInvokers.isEmpty()) {
            filteredInvokers.addAll(clusterInvokers);
        }
        return filteredInvokers.isEmpty() ? invokers : filteredInvokers;
    }

    private URL parseConsumerUrl(URL url) {
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

    private boolean strictMatch(String serviceMethod, String invokedMethod) {
        if (serviceMethod == null) {
            return false;
        }
        if (serviceMethod.indexOf(",") >= 0) {
            String[] methods;
            for (String method : methods = serviceMethod.split(",")) {
                if (!method.equals(invokedMethod)) continue;
                return true;
            }
        }
        return serviceMethod.equals(invokedMethod);
    }
}

