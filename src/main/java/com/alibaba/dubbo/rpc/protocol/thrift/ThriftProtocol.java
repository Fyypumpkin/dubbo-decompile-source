/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.thrift;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Transporter;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeClient;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;
import com.alibaba.dubbo.remoting.exchange.Exchangers;
import com.alibaba.dubbo.remoting.exchange.support.ExchangeHandlerAdapter;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProtocol;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboExporter;
import com.alibaba.dubbo.rpc.protocol.thrift.ThriftInvoker;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThriftProtocol
extends AbstractProtocol {
    public static final int DEFAULT_PORT = 40880;
    public static final String NAME = "thrift";
    private final ConcurrentMap<String, ExchangeServer> serverMap = new ConcurrentHashMap<String, ExchangeServer>();
    private ExchangeHandler handler = new ExchangeHandlerAdapter(){

        @Override
        public Object reply(ExchangeChannel channel, Object msg) throws RemotingException {
            if (msg instanceof Invocation) {
                Invocation inv = (Invocation)msg;
                String serviceName = inv.getAttachments().get("interface");
                String serviceKey = ThriftProtocol.serviceKey(channel.getLocalAddress().getPort(), serviceName, null, null);
                DubboExporter exporter = (DubboExporter)ThriftProtocol.this.exporterMap.get(serviceKey);
                if (exporter == null) {
                    throw new RemotingException((Channel)channel, "Not found exported service: " + serviceKey + " in " + ThriftProtocol.this.exporterMap.keySet() + ", may be version or group mismatch , channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress() + ", message:" + msg);
                }
                RpcContext.getContext().setRemoteAddress(channel.getRemoteAddress());
                return exporter.getInvoker().invoke(inv);
            }
            throw new RemotingException((Channel)channel, "Unsupported request: " + msg.getClass().getName() + ": " + msg + ", channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress());
        }

        @Override
        public void received(Channel channel, Object message) throws RemotingException {
            if (message instanceof Invocation) {
                this.reply((ExchangeChannel)channel, message);
            } else {
                super.received(channel, message);
            }
        }
    };

    @Override
    public int getDefaultPort() {
        return 40880;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        URL url = invoker.getUrl().addParameter("codec", NAME);
        String key = url.getAddress();
        boolean isServer = url.getParameter("isserver", true);
        if (isServer && !this.serverMap.containsKey(key)) {
            this.serverMap.put(key, this.getServer(url));
        }
        key = ThriftProtocol.serviceKey(url);
        DubboExporter<T> exporter = new DubboExporter<T>(invoker, key, this.exporterMap);
        this.exporterMap.put(key, exporter);
        return exporter;
    }

    @Override
    public void destroy() {
        super.destroy();
        for (String key : new ArrayList(this.serverMap.keySet())) {
            ExchangeServer server = (ExchangeServer)this.serverMap.remove(key);
            if (server == null) continue;
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Close dubbo server: " + server.getLocalAddress());
                }
                server.close(ThriftProtocol.getServerShutdownTimeout());
            }
            catch (Throwable t) {
                this.logger.warn(t.getMessage(), t);
            }
        }
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        ThriftInvoker<T> invoker = new ThriftInvoker<T>(type, url, this.getClients(url), this.invokers);
        this.invokers.add(invoker);
        return invoker;
    }

    private ExchangeClient[] getClients(URL url) {
        int connections = url.getParameter("connections", 1);
        ExchangeClient[] clients = new ExchangeClient[connections];
        for (int i = 0; i < clients.length; ++i) {
            clients[i] = this.initClient(url);
        }
        return clients;
    }

    private ExchangeClient initClient(URL url) {
        ExchangeClient client;
        url = url.addParameter("codec", NAME);
        try {
            client = Exchangers.connect(url);
        }
        catch (RemotingException e) {
            throw new RpcException("Fail to create remoting client for service(" + url + "): " + e.getMessage(), (Throwable)e);
        }
        return client;
    }

    private ExchangeServer getServer(URL url) {
        Set<String> supportedTypes;
        ExchangeServer server;
        String str = (url = url.addParameterIfAbsent("channel.readonly.sent", Boolean.TRUE.toString())).getParameter("server", "netty");
        if (str != null && str.length() > 0 && !ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str)) {
            throw new RpcException("Unsupported server type: " + str + ", url: " + url);
        }
        try {
            server = Exchangers.bind(url, this.handler);
        }
        catch (RemotingException e) {
            throw new RpcException("Fail to start server(url: " + url + ") " + e.getMessage(), (Throwable)e);
        }
        str = url.getParameter("client");
        if (str != null && str.length() > 0 && !(supportedTypes = ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions()).contains(str)) {
            throw new RpcException("Unsupported client type: " + str);
        }
        return server;
    }

}

