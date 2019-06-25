/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.status.StatusChecker;
import com.alibaba.dubbo.common.threadpool.ThreadPool;
import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.support.Parameter;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;
import com.alibaba.dubbo.remoting.Codec;
import com.alibaba.dubbo.remoting.Dispatcher;
import com.alibaba.dubbo.remoting.Transporter;
import com.alibaba.dubbo.remoting.exchange.Exchanger;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.rpc.Protocol;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProtocolConfig
extends AbstractConfig {
    private static final long serialVersionUID = 6913423882496634749L;
    private String name;
    private String host;
    private Integer port;
    private String contextpath;
    private String threadpool;
    private Integer threads;
    private Integer iothreads;
    private Integer queues;
    private Integer accepts;
    private String codec;
    private String serialization;
    private String charset;
    private Integer payload;
    private Integer buffer;
    private Integer heartbeat;
    private String accesslog;
    private String transporter;
    private String exchanger;
    private String dispatcher;
    private String networker;
    private String server;
    private String client;
    private String telnet;
    private String prompt;
    private String status;
    private Boolean register;
    private Boolean keepAlive;
    private String optimizer;
    private String extension;
    private Map<String, String> parameters;
    private Boolean isDefault;
    private Integer backlog;
    private Boolean isEpoll;
    private String injectorFactory;
    private static final AtomicBoolean destroyed = new AtomicBoolean(false);

    public ProtocolConfig() {
    }

    public ProtocolConfig(String name) {
        this.setName(name);
    }

    public ProtocolConfig(String name, int port) {
        this.setName(name);
        this.setPort(port);
    }

    public Integer getBacklog() {
        return this.backlog;
    }

    public void setBacklog(Integer backlog) {
        this.backlog = backlog;
    }

    @Parameter(excluded=true)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        ProtocolConfig.checkName("name", name);
        this.name = name;
        if (this.id == null || this.id.length() == 0) {
            this.id = name;
        }
    }

    @Parameter(excluded=true)
    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        ProtocolConfig.checkName("host", host);
        this.host = host;
    }

    @Parameter(excluded=true)
    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Deprecated
    @Parameter(excluded=true)
    public String getPath() {
        return this.getContextpath();
    }

    @Deprecated
    public void setPath(String path) {
        this.setContextpath(path);
    }

    @Parameter(excluded=true)
    public String getContextpath() {
        return this.contextpath;
    }

    public void setContextpath(String contextpath) {
        ProtocolConfig.checkPathName("contextpath", contextpath);
        this.contextpath = contextpath;
    }

    public String getThreadpool() {
        return this.threadpool;
    }

    public void setThreadpool(String threadpool) {
        ProtocolConfig.checkExtension(ThreadPool.class, "threadpool", threadpool);
        this.threadpool = threadpool;
    }

    public Integer getThreads() {
        return this.threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public Integer getIothreads() {
        return this.iothreads;
    }

    public void setIothreads(Integer iothreads) {
        this.iothreads = iothreads;
    }

    public Integer getQueues() {
        return this.queues;
    }

    public void setQueues(Integer queues) {
        this.queues = queues;
    }

    public Integer getAccepts() {
        return this.accepts;
    }

    public void setAccepts(Integer accepts) {
        this.accepts = accepts;
    }

    public String getCodec() {
        return this.codec;
    }

    public void setCodec(String codec) {
        if ("dubbo".equals(this.name)) {
            ProtocolConfig.checkMultiExtension(Codec.class, "codec", codec);
        }
        this.codec = codec;
    }

    public String getSerialization() {
        return this.serialization;
    }

    public void setSerialization(String serialization) {
        if ("dubbo".equals(this.name)) {
            ProtocolConfig.checkMultiExtension(Serialization.class, "serialization", serialization);
        }
        this.serialization = serialization;
    }

    public String getCharset() {
        return this.charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public Integer getPayload() {
        return this.payload;
    }

    public void setPayload(Integer payload) {
        this.payload = payload;
    }

    public Integer getBuffer() {
        return this.buffer;
    }

    public void setBuffer(Integer buffer) {
        this.buffer = buffer;
    }

    public Integer getHeartbeat() {
        return this.heartbeat;
    }

    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        if ("dubbo".equals(this.name)) {
            ProtocolConfig.checkMultiExtension(Transporter.class, "server", server);
        }
        this.server = server;
    }

    public String getClient() {
        return this.client;
    }

    public void setClient(String client) {
        if ("dubbo".equals(this.name)) {
            ProtocolConfig.checkMultiExtension(Transporter.class, "client", client);
        }
        this.client = client;
    }

    public String getAccesslog() {
        return this.accesslog;
    }

    public void setAccesslog(String accesslog) {
        this.accesslog = accesslog;
    }

    public String getTelnet() {
        return this.telnet;
    }

    public void setTelnet(String telnet) {
        ProtocolConfig.checkMultiExtension(TelnetHandler.class, "telnet", telnet);
        this.telnet = telnet;
    }

    @Parameter(escaped=true)
    public String getPrompt() {
        return this.prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        ProtocolConfig.checkMultiExtension(StatusChecker.class, "status", status);
        this.status = status;
    }

    public Boolean getRegister() {
        return this.register;
    }

    public void setRegister(Boolean register) {
        this.register = register;
    }

    public String getTransporter() {
        return this.transporter;
    }

    public void setTransporter(String transporter) {
        ProtocolConfig.checkExtension(Transporter.class, "transporter", transporter);
        this.transporter = transporter;
    }

    public String getExchanger() {
        return this.exchanger;
    }

    public void setExchanger(String exchanger) {
        ProtocolConfig.checkExtension(Exchanger.class, "exchanger", exchanger);
        this.exchanger = exchanger;
    }

    @Deprecated
    @Parameter(excluded=true)
    public String getDispather() {
        return this.getDispatcher();
    }

    @Deprecated
    public void setDispather(String dispather) {
        this.setDispatcher(dispather);
    }

    public String getDispatcher() {
        return this.dispatcher;
    }

    public void setDispatcher(String dispatcher) {
        ProtocolConfig.checkExtension(Dispatcher.class, "dispacther", dispatcher);
        this.dispatcher = dispatcher;
    }

    public String getNetworker() {
        return this.networker;
    }

    public void setNetworker(String networker) {
        this.networker = networker;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Boolean isDefault() {
        return this.isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getKeepAlive() {
        return this.keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getOptimizer() {
        return this.optimizer;
    }

    public void setOptimizer(String optimizer) {
        this.optimizer = optimizer;
    }

    public String getExtension() {
        return this.extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Boolean isEpoll() {
        return this.isEpoll;
    }

    public void setEpoll(Boolean epoll) {
        this.isEpoll = epoll;
    }

    public String getInjectorFactory() {
        return this.injectorFactory;
    }

    public void setInjectorFactory(String injectorFactory) {
        this.injectorFactory = injectorFactory;
    }

    public void destory() {
        if (this.name != null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(this.name).destroy();
        }
    }

    public static void destroyAll() {
        Protocol protocol;
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }
        AbstractRegistryFactory.destroyAll();
        ExtensionLoader<Protocol> loader = ExtensionLoader.getExtensionLoader(Protocol.class);
        for (String protocolName : loader.getLoadedExtensions()) {
            try {
                protocol = loader.getLoadedExtension(protocolName);
                if (protocol == null) continue;
                protocol.destroyServer();
            }
            catch (Throwable t) {
                logger.warn(t.getMessage(), t);
            }
        }
        for (String protocolName : loader.getLoadedExtensions()) {
            try {
                protocol = loader.getLoadedExtension(protocolName);
                if (protocol == null) continue;
                protocol.destroy();
            }
            catch (Throwable t) {
                logger.warn(t.getMessage(), t);
            }
        }
    }
}

