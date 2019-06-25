/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.serialize.support.SerializableClassRegistry;
import com.alibaba.dubbo.common.serialize.support.SerializationOptimizer;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
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
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.protocol.AbstractProtocol;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboExporter;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboInvoker;
import com.alibaba.dubbo.rpc.protocol.dubbo.LazyConnectExchangeClient;
import com.alibaba.dubbo.rpc.protocol.dubbo.ReferenceCountExchangeClient;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class DubboProtocol
extends AbstractProtocol {
    public static final String NAME = "dubbo";
    public static final String COMPATIBLE_CODEC_NAME = "dubbo1compatible";
    public static final int DEFAULT_PORT = 20880;
    public final ReentrantLock lock = new ReentrantLock();
    private final Map<String, ExchangeServer> serverMap = new ConcurrentHashMap<String, ExchangeServer>();
    private final Map<String, ReferenceCountExchangeClient> referenceClientMap = new ConcurrentHashMap<String, ReferenceCountExchangeClient>();
    private final ConcurrentMap<String, LazyConnectExchangeClient> ghostClientMap = new ConcurrentHashMap<String, LazyConnectExchangeClient>();
    private final Set<String> optimizers = new ConcurrentHashSet<String>();
    private final ConcurrentMap<String, String> stubServiceMethodsMap = new ConcurrentHashMap<String, String>();
    private static final String IS_CALLBACK_SERVICE_INVOKE = "_isCallBackServiceInvoke";
    private ExchangeHandler requestHandler = new ExchangeHandlerAdapter(){

        @Override
        public Object reply(ExchangeChannel channel, Object message) throws RemotingException {
            if (message instanceof Invocation) {
                Invocation inv = (Invocation)message;
                Invoker<?> invoker = DubboProtocol.this.getInvoker(channel, inv);
                if (Boolean.TRUE.toString().equals(inv.getAttachments().get(DubboProtocol.IS_CALLBACK_SERVICE_INVOKE))) {
                    String methodsStr = invoker.getUrl().getParameters().get("methods");
                    boolean hasMethod = false;
                    if (methodsStr == null || methodsStr.indexOf(",") == -1) {
                        hasMethod = inv.getMethodName().equals(methodsStr);
                    } else {
                        String[] methods;
                        for (String method : methods = methodsStr.split(",")) {
                            if (!inv.getMethodName().equals(method)) continue;
                            hasMethod = true;
                            break;
                        }
                    }
                    if (!hasMethod) {
                        DubboProtocol.this.logger.warn(new IllegalStateException(new StringBuilder().append("The methodName ").append(inv.getMethodName()).append(" not found in callback service interface ,invoke will be ignored. please update the api interface. url is:").append(invoker.getUrl()).toString()) + " ,invocation is :" + inv);
                        return null;
                    }
                }
                RpcContext.getContext().setRemoteAddress(channel.getRemoteAddress());
                return invoker.invoke(inv);
            }
            throw new RemotingException((Channel)channel, "Unsupported request: " + message == null ? null : message.getClass().getName() + ": " + message + ", channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress());
        }

        @Override
        public void received(Channel channel, Object message) throws RemotingException {
            if (message instanceof Invocation) {
                this.reply((ExchangeChannel)channel, message);
            } else {
                super.received(channel, message);
            }
        }

        @Override
        public void connected(Channel channel) throws RemotingException {
            this.invoke(channel, "onconnect");
        }

        @Override
        public void disconnected(Channel channel) throws RemotingException {
            if (DubboProtocol.this.logger.isInfoEnabled()) {
                DubboProtocol.this.logger.info("disconected from " + channel.getRemoteAddress() + ",url:" + channel.getUrl());
            }
            this.invoke(channel, "ondisconnect");
        }

        private void invoke(Channel channel, String methodKey) {
            Invocation invocation = this.createInvocation(channel, channel.getUrl(), methodKey);
            if (invocation != null) {
                try {
                    this.received(channel, invocation);
                }
                catch (Throwable t) {
                    DubboProtocol.this.logger.warn("Failed to invoke event method " + invocation.getMethodName() + "(), cause: " + t.getMessage(), t);
                }
            }
        }

        private Invocation createInvocation(Channel channel, URL url, String methodKey) {
            String method = url.getParameter(methodKey);
            if (method == null || method.length() == 0) {
                return null;
            }
            RpcInvocation invocation = new RpcInvocation(method, new Class[0], new Object[0]);
            invocation.setAttachment("path", url.getPath());
            invocation.setAttachment("group", url.getParameter("group"));
            invocation.setAttachment("interface", url.getParameter("interface"));
            invocation.setAttachment("version", url.getParameter("version"));
            if (url.getParameter("dubbo.stub.event", false)) {
                invocation.setAttachment("dubbo.stub.event", Boolean.TRUE.toString());
            }
            return invocation;
        }
    };
    private static DubboProtocol INSTANCE;

    public DubboProtocol() {
        INSTANCE = this;
    }

    public static DubboProtocol getDubboProtocol() {
        if (INSTANCE == null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(NAME);
        }
        return INSTANCE;
    }

    public Collection<ExchangeServer> getServers() {
        return Collections.unmodifiableCollection(this.serverMap.values());
    }

    public Collection<Exporter<?>> getExporters() {
        return Collections.unmodifiableCollection(this.exporterMap.values());
    }

    Map<String, Exporter<?>> getExporterMap() {
        return this.exporterMap;
    }

    private boolean isClientSide(Channel channel) {
        InetSocketAddress address = channel.getRemoteAddress();
        URL url = channel.getUrl();
        return url.getPort() == address.getPort() && NetUtils.filterLocalHost(channel.getUrl().getIp()).equals(NetUtils.filterLocalHost(address.getAddress().getHostAddress()));
    }

    Invoker<?> getInvoker(Channel channel, Invocation inv) throws RemotingException {
        DubboExporter exporter;
        String serviceKey;
        boolean isCallBackServiceInvoke = false;
        boolean isStubServiceInvoke = false;
        int port = channel.getLocalAddress().getPort();
        String path = inv.getAttachments().get("path");
        isStubServiceInvoke = Boolean.TRUE.toString().equals(inv.getAttachments().get("dubbo.stub.event"));
        if (isStubServiceInvoke) {
            port = channel.getRemoteAddress().getPort();
        }
        boolean bl = isCallBackServiceInvoke = this.isClientSide(channel) && !isStubServiceInvoke;
        if (isCallBackServiceInvoke) {
            path = inv.getAttachments().get("path") + "." + inv.getAttachments().get("callback.service.instid");
            inv.getAttachments().put(IS_CALLBACK_SERVICE_INVOKE, Boolean.TRUE.toString());
        }
        if ((exporter = (DubboExporter)this.exporterMap.get(serviceKey = DubboProtocol.serviceKey(port, path, inv.getAttachments().get("version"), inv.getAttachments().get("group")))) == null) {
            throw new RemotingException(channel, "Not found exported service: " + serviceKey + " in " + this.exporterMap.keySet() + ", may be version or group mismatch , channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress() + ", message:" + inv);
        }
        return exporter.getInvoker();
    }

    public Collection<Invoker<?>> getInvokers() {
        return Collections.unmodifiableCollection(this.invokers);
    }

    @Override
    public int getDefaultPort() {
        return 20880;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        URL url = invoker.getUrl();
        String key = DubboProtocol.serviceKey(url);
        DubboExporter<T> exporter = new DubboExporter<T>(invoker, key, this.exporterMap);
        this.exporterMap.put(key, exporter);
        Boolean isStubSupportEvent = url.getParameter("dubbo.stub.event", false);
        Boolean isCallbackservice = url.getParameter("is_callback_service", false);
        if (isStubSupportEvent.booleanValue() && !isCallbackservice.booleanValue()) {
            String stubServiceMethods = url.getParameter("dubbo.stub.event.methods");
            if (stubServiceMethods == null || stubServiceMethods.length() == 0) {
                if (this.logger.isWarnEnabled()) {
                    this.logger.warn(new IllegalStateException("consumer [" + url.getParameter("interface") + "], has set stubproxy support event ,but no stub methods founded."));
                }
            } else {
                this.stubServiceMethodsMap.put(url.getServiceKey(), stubServiceMethods);
            }
        }
        this.openServer(url);
        this.optimizeSerialization(url);
        return exporter;
    }

    private void optimizeSerialization(URL url) throws RpcException {
        String className = url.getParameter("optimizer", "");
        if (StringUtils.isEmpty(className) || this.optimizers.contains(className)) {
            return;
        }
        this.logger.info("Optimizing the serialization process for Kryo, FST, etc...");
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            if (!SerializationOptimizer.class.isAssignableFrom(clazz)) {
                throw new RpcException("The serialization optimizer " + className + " isn't an instance of " + SerializationOptimizer.class.getName());
            }
            SerializationOptimizer optimizer = (SerializationOptimizer)clazz.newInstance();
            if (optimizer.getSerializableClasses() == null) {
                return;
            }
            for (Class c : optimizer.getSerializableClasses()) {
                SerializableClassRegistry.registerClass(c);
            }
            this.optimizers.add(className);
        }
        catch (ClassNotFoundException e) {
            throw new RpcException("Cannot find the serialization optimizer class: " + className, (Throwable)e);
        }
        catch (InstantiationException e) {
            throw new RpcException("Cannot instantiate the serialization optimizer class: " + className, (Throwable)e);
        }
        catch (IllegalAccessException e) {
            throw new RpcException("Cannot instantiate the serialization optimizer class: " + className, (Throwable)e);
        }
    }

    private void openServer(URL url) {
        String key = url.getAddress();
        boolean isServer = url.getParameter("isserver", true);
        if (isServer) {
            ExchangeServer server = this.serverMap.get(key);
            if (server == null) {
                this.serverMap.put(key, this.createServer(url));
            } else {
                server.reset(url);
            }
        }
    }

    private ExchangeServer createServer(URL url) {
        Set<String> supportedTypes;
        ExchangeServer server;
        String str = (url = url.addParameterIfAbsent("channel.readonly.sent", Boolean.TRUE.toString())).getParameter("server", "netty");
        if (str != null && str.length() > 0 && !ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str)) {
            throw new RpcException("Unsupported server type: " + str + ", url: " + url);
        }
        url = url.addParameter("codec", Version.isCompatibleVersion() ? COMPATIBLE_CODEC_NAME : NAME);
        try {
            server = Exchangers.bind(url, this.requestHandler);
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

    @Override
    public <T> Invoker<T> refer(Class<T> serviceType, URL url) throws RpcException {
        this.optimizeSerialization(url);
        DubboInvoker<T> invoker = new DubboInvoker<T>(serviceType, url, this.getClients(url), this.invokers);
        this.invokers.add(invoker);
        return invoker;
    }

    private ExchangeClient[] getClients(URL url) {
        boolean service_share_connect = false;
        int connections = url.getParameter("connections", 0);
        if (connections == 0) {
            service_share_connect = true;
            connections = 1;
        }
        ExchangeClient[] clients = new ExchangeClient[connections];
        for (int i = 0; i < clients.length; ++i) {
            clients[i] = service_share_connect ? this.getSharedClient(url) : this.initClient(url);
        }
        return clients;
    }

    private ExchangeClient getSharedClient(URL url) {
        String key = url.getAddress();
        ReferenceCountExchangeClient client = this.referenceClientMap.get(key);
        if (client != null) {
            if (!client.isClosed()) {
                client.incrementAndGetCount();
                return client;
            }
            this.referenceClientMap.remove(key);
        }
        ExchangeClient exchagneclient = this.initClient(url);
        client = new ReferenceCountExchangeClient(exchagneclient, this.ghostClientMap);
        this.referenceClientMap.put(key, client);
        this.ghostClientMap.remove(key);
        return client;
    }

    private ExchangeClient initClient(URL url) {
        ExchangeClient client;
        String str = url.getParameter("client", url.getParameter("server", "netty"));
        String version = url.getParameter(NAME);
        boolean compatible = version != null && version.startsWith("1.0.");
        url = url.addParameter("codec", Version.isCompatibleVersion() && compatible ? COMPATIBLE_CODEC_NAME : NAME);
        url = url.addParameterIfAbsent("heartbeat", String.valueOf(60000));
        if (str != null && str.length() > 0 && !ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str)) {
            throw new RpcException("Unsupported client type: " + str + ", supported client type is " + StringUtils.join(ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions(), " "));
        }
        try {
            client = url.getParameter("lazy", false) ? new LazyConnectExchangeClient(url, this.requestHandler) : Exchangers.connect(url, this.requestHandler);
        }
        catch (RemotingException e) {
            throw new RpcException("Fail to create remoting client for service(" + url + "): " + e.getMessage(), (Throwable)e);
        }
        return client;
    }

    @Override
    public void destroy() {
        ExchangeClient client;
        for (String key : new ArrayList<String>(this.referenceClientMap.keySet())) {
            client = this.referenceClientMap.remove(key);
            if (client == null) continue;
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Close dubbo connect: " + client.getLocalAddress() + "-->" + client.getRemoteAddress());
                }
                client.close();
            }
            catch (Throwable t) {
                this.logger.warn(t.getMessage(), t);
            }
        }
        for (String key : new ArrayList(this.ghostClientMap.keySet())) {
            client = (ExchangeClient)this.ghostClientMap.remove(key);
            if (client == null) continue;
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Close dubbo connect: " + client.getLocalAddress() + "-->" + client.getRemoteAddress());
                }
                client.close();
            }
            catch (Throwable t) {
                this.logger.warn(t.getMessage(), t);
            }
        }
        this.stubServiceMethodsMap.clear();
        super.destroy();
    }

    @Override
    public void destroyServer() {
        for (String key : new ArrayList<String>(this.serverMap.keySet())) {
            ExchangeServer server = this.serverMap.remove(key);
            if (server == null) continue;
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Close dubbo server: " + server.getLocalAddress());
                }
                server.close(DubboProtocol.getServerShutdownTimeout());
            }
            catch (Throwable t) {
                this.logger.warn(t.getMessage(), t);
            }
        }
    }

}

