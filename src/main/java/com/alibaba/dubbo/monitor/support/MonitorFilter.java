/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.monitor.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.monitor.Monitor;
import com.alibaba.dubbo.monitor.MonitorFactory;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Activate(group={"provider", "consumer"})
public class MonitorFilter
implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(MonitorFilter.class);
    private final ConcurrentMap<String, AtomicInteger> concurrents = new ConcurrentHashMap<String, AtomicInteger>();
    private MonitorFactory monitorFactory;
    public static final String retryKey = "isLastCall";
    public static final String rejectKey = "limitedReject";
    public static final String threadPoolExhaustedKey = "serverThreadPoolExhausted";
    public static final String businessExceptionKey = "hasBusinessException";
    public static final String fromAppKey = "from_app";
    public static final String toAppKey = "to_app";

    public void setMonitorFactory(MonitorFactory monitorFactory) {
        this.monitorFactory = monitorFactory;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (invoker.getUrl().hasParameter("monitor")) {
            RpcContext context = RpcContext.getContext();
            long start = System.currentTimeMillis();
            this.getConcurrent(invoker, invocation).incrementAndGet();
            try {
                Result result = invoker.invoke(invocation);
                this.collect(invoker, invocation, result, context, start, false);
                Result result2 = result;
                return result2;
            }
            catch (RpcException e) {
                if (e.getCode() == 6) {
                    ((RpcInvocation)invocation).setAttachment(rejectKey, "yes");
                } else if (e.getCode() == 8) {
                    ((RpcInvocation)invocation).setAttachment(threadPoolExhaustedKey, "true");
                }
                this.collect(invoker, invocation, null, context, start, true);
                throw e;
            }
            finally {
                this.getConcurrent(invoker, invocation).decrementAndGet();
            }
        }
        return invoker.invoke(invocation);
    }

    private void collect(Invoker<?> invoker, Invocation invocation, Result result, RpcContext context, long start, boolean error) {
        try {
            String remoteValue;
            String remoteKey;
            int localPort;
            long elapsed = System.currentTimeMillis() - start;
            int concurrent = this.getConcurrent(invoker, invocation).get();
            String application = invoker.getUrl().getParameter("application");
            String service = invoker.getInterface().getName();
            String method = RpcUtils.getMethodName(invocation);
            URL url = invoker.getUrl().getUrlParameter("monitor");
            Monitor monitor = this.monitorFactory.getMonitor(url);
            if ("consumer".equals(invoker.getUrl().getParameter("side"))) {
                context = RpcContext.getContext();
                localPort = 0;
                remoteKey = "provider";
                remoteValue = invoker.getUrl().getAddress();
            } else {
                localPort = invoker.getUrl().getPort();
                remoteKey = "consumer";
                remoteValue = context.getRemoteHost();
            }
            String input = "";
            String output = "";
            if (invocation.getAttachment("input") != null) {
                input = invocation.getAttachment("input");
            }
            if (result != null && result.getAttachment("output") != null) {
                output = result.getAttachment("output");
            }
            String businessExceptionValue = Boolean.toString(result != null && result.hasException());
            String protocol = invoker.getUrl().getProtocol();
            monitor.collect(new URL("count", NetUtils.getLocalHost(), localPort, service + "/" + method, "application", application, "interface", service, "method", method, remoteKey, remoteValue, businessExceptionKey, businessExceptionValue, error ? "failure" : "success", "1", retryKey, invocation.getAttachment(retryKey), rejectKey, invocation.getAttachment(rejectKey, "no"), threadPoolExhaustedKey, invocation.getAttachment(threadPoolExhaustedKey, "false"), fromAppKey, invocation.getAttachment(fromAppKey), toAppKey, invoker.getUrl().getParameter("application"), "protocol", protocol, "elapsed", String.valueOf(elapsed), "concurrent", String.valueOf(concurrent), "input", input, "output", output));
        }
        catch (Throwable t) {
            logger.error("Failed to monitor count service " + invoker.getUrl() + ", cause: " + t.getMessage(), t);
        }
    }

    private AtomicInteger getConcurrent(Invoker<?> invoker, Invocation invocation) {
        String key = invoker.getInterface().getName() + "." + invocation.getMethodName();
        AtomicInteger concurrent = (AtomicInteger)this.concurrents.get(key);
        if (concurrent == null) {
            this.concurrents.putIfAbsent(key, new AtomicInteger());
            concurrent = (AtomicInteger)this.concurrents.get(key);
        }
        return concurrent;
    }
}

