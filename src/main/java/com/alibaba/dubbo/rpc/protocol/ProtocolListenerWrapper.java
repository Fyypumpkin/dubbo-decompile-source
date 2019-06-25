/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.ExporterListener;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.InvokerListener;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.listener.ListenerExporterWrapper;
import com.alibaba.dubbo.rpc.listener.ListenerInvokerWrapper;
import java.util.Collections;
import java.util.List;

public class ProtocolListenerWrapper
implements Protocol {
    private final Protocol protocol;

    public ProtocolListenerWrapper(Protocol protocol) {
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
        return new ListenerExporterWrapper<T>(this.protocol.export(invoker), Collections.unmodifiableList(ExtensionLoader.getExtensionLoader(ExporterListener.class).getActivateExtension(invoker.getUrl(), "exporter.listener")));
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        if ("registry".equals(url.getProtocol())) {
            return this.protocol.refer(type, url);
        }
        return new ListenerInvokerWrapper<T>(this.protocol.refer(type, url), Collections.unmodifiableList(ExtensionLoader.getExtensionLoader(InvokerListener.class).getActivateExtension(url, "invoker.listener")));
    }

    @Override
    public void destroy() {
        this.protocol.destroy();
    }

    @Override
    public void destroyServer() {
        this.protocol.destroyServer();
    }
}

