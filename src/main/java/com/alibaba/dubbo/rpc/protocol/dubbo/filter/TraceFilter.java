/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo.filter;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Activate(group={"provider"})
public class TraceFilter
implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(TraceFilter.class);
    private static final String TRACE_MAX = "trace.max";
    private static final String TRACE_COUNT = "trace.count";
    private static final ConcurrentMap<String, Set<Channel>> tracers = new ConcurrentHashMap<String, Set<Channel>>();

    public static void addTracer(Class<?> type, String method, Channel channel, int max) {
        channel.setAttribute(TRACE_MAX, max);
        channel.setAttribute(TRACE_COUNT, new AtomicInteger());
        String key = method != null && method.length() > 0 ? type.getName() + "." + method : type.getName();
        Set channels = (Set)tracers.get(key);
        if (channels == null) {
            tracers.putIfAbsent(key, new ConcurrentHashSet());
            channels = (Set)tracers.get(key);
        }
        channels.add(channel);
    }

    public static void removeTracer(Class<?> type, String method, Channel channel) {
        channel.removeAttribute(TRACE_MAX);
        channel.removeAttribute(TRACE_COUNT);
        String key = method != null && method.length() > 0 ? type.getName() + "." + method : type.getName();
        Set channels = (Set)tracers.get(key);
        if (channels != null) {
            channels.remove(channel);
        }
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        long start = System.currentTimeMillis();
        Result result = invoker.invoke(invocation);
        long end = System.currentTimeMillis();
        if (tracers.size() > 0) {
            String key = invoker.getInterface().getName() + "." + invocation.getMethodName();
            Set channels = (Set)tracers.get(key);
            if (channels == null || channels.size() == 0) {
                key = invoker.getInterface().getName();
                channels = (Set)tracers.get(key);
            }
            if (channels != null && channels.size() > 0) {
                for (Channel channel : new ArrayList(channels)) {
                    if (channel.isConnected()) {
                        try {
                            int max = 1;
                            Integer m = (Integer)channel.getAttribute(TRACE_MAX);
                            if (m != null) {
                                max = m;
                            }
                            int count = 0;
                            AtomicInteger c = (AtomicInteger)channel.getAttribute(TRACE_COUNT);
                            if (c == null) {
                                c = new AtomicInteger();
                                channel.setAttribute(TRACE_COUNT, c);
                            }
                            if ((count = c.getAndIncrement()) < max) {
                                String prompt = channel.getUrl().getParameter("prompt", "dubbo>");
                                channel.send("\r\n" + RpcContext.getContext().getRemoteAddress() + " -> " + invoker.getInterface().getName() + "." + invocation.getMethodName() + "(" + JSON.json(invocation.getArguments()) + ") -> " + JSON.json(result.getValue()) + "\r\nelapsed: " + (end - start) + " ms.\r\n\r\n" + prompt);
                            }
                            if (count < max - 1) continue;
                            channels.remove(channel);
                        }
                        catch (Throwable e) {
                            channels.remove(channel);
                            logger.warn(e.getMessage(), e);
                        }
                        continue;
                    }
                    channels.remove(channel);
                }
            }
        }
        return result;
    }
}

