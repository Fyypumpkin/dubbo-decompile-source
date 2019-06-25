/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import java.util.List;

public class ProtocolFilterWrapper
implements Protocol {
    private final Protocol protocol;

    public ProtocolFilterWrapper(Protocol protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException("protocol == null");
        }
        this.protocol = protocol;
    }

    @Override
    public int getDefaultPort() {
        return this.protocol.getDefaultPort();
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        if ("registry".equals(invoker.getUrl().getProtocol())) {
            return this.protocol.export(invoker);
        }
        return this.protocol.export(ProtocolFilterWrapper.buildInvokerChain(invoker, "service.filter", "provider"));
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        if ("registry".equals(url.getProtocol())) {
            return this.protocol.refer(type, url);
        }
        return ProtocolFilterWrapper.buildInvokerChain(this.protocol.refer(type, url), "reference.filter", "consumer");
    }

    @Override
    public void destroy() {
        this.protocol.destroy();
    }

    @Override
    public void destroyServer() {
        this.protocol.destroyServer();
    }

    private static <T> Invoker<T> buildInvokerChain(final Invoker<T> invoker, String key, String group) {
        Invoker last = invoker;
        List<Filter> filters = ExtensionLoader.getExtensionLoader(Filter.class).getActivateExtension(invoker.getUrl(), key, group);
        if (filters.size() > 0) {
            for (int i = filters.size() - 1; i >= 0; --i) {
                final Filter filter = filters.get(i);
                final Invoker next = last;
                last = new Invoker<T>(){

                    @Override
                    public Class<T> getInterface() {
                        return invoker.getInterface();
                    }

                    @Override
                    public URL getUrl() {
                        return invoker.getUrl();
                    }

                    @Override
                    public boolean isAvailable() {
                        return invoker.isAvailable();
                    }

                    @Override
                    public Result invoke(Invocation invocation) throws RpcException {
                        return filter.invoke(next, invocation);
                    }

                    @Override
                    public void destroy() {
                        invoker.destroy();
                    }

                    public String toString() {
                        return invoker.toString();
                    }
                };
            }
        }
        return last;
    }

}

