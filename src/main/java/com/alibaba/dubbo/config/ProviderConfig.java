/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.common.status.StatusChecker;
import com.alibaba.dubbo.common.threadpool.ThreadPool;
import com.alibaba.dubbo.config.AbstractServiceConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.support.Parameter;
import com.alibaba.dubbo.remoting.Dispatcher;
import com.alibaba.dubbo.remoting.Transporter;
import com.alibaba.dubbo.remoting.exchange.Exchanger;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import java.util.Arrays;
import java.util.List;

public class ProviderConfig
extends AbstractServiceConfig {
    private static final long serialVersionUID = 6913423882496634749L;
    private String host;
    private Integer port;
    private String contextpath;
    private String threadpool;
    private Integer threads;
    private Integer iothreads;
    private Integer queues;
    private Integer accepts;
    private String codec;
    private String charset;
    private Integer payload;
    private Integer buffer;
    private String transporter;
    private String exchanger;
    private String dispatcher;
    private String networker;
    private String server;
    private String client;
    private String telnet;
    private String prompt;
    private String status;
    private Integer wait;
    private Boolean isDefault;
    private Boolean isEpoll;

    @Deprecated
    public void setProtocol(String protocol) {
        this.protocols = Arrays.asList(new ProtocolConfig(protocol));
    }

    @Parameter(excluded=true)
    public Boolean isDefault() {
        return this.isDefault;
    }

    @Deprecated
    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Parameter(excluded=true)
    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Parameter(excluded=true)
    public Integer getPort() {
        return this.port;
    }

    @Deprecated
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
        ProviderConfig.checkPathName("contextpath", contextpath);
        this.contextpath = contextpath;
    }

    public String getThreadpool() {
        return this.threadpool;
    }

    public void setThreadpool(String threadpool) {
        ProviderConfig.checkExtension(ThreadPool.class, "threadpool", threadpool);
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
        this.codec = codec;
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

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getClient() {
        return this.client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getTelnet() {
        return this.telnet;
    }

    public void setTelnet(String telnet) {
        ProviderConfig.checkMultiExtension(TelnetHandler.class, "telnet", telnet);
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
        ProviderConfig.checkMultiExtension(StatusChecker.class, "status", status);
        this.status = status;
    }

    @Override
    public String getCluster() {
        return super.getCluster();
    }

    @Override
    public Integer getConnections() {
        return super.getConnections();
    }

    @Override
    public Integer getTimeout() {
        return super.getTimeout();
    }

    @Override
    public Integer getRetries() {
        return super.getRetries();
    }

    @Override
    public String getLoadbalance() {
        return super.getLoadbalance();
    }

    @Override
    public Boolean isAsync() {
        return super.isAsync();
    }

    @Override
    public Integer getActives() {
        return super.getActives();
    }

    public String getTransporter() {
        return this.transporter;
    }

    public void setTransporter(String transporter) {
        ProviderConfig.checkExtension(Transporter.class, "transporter", transporter);
        this.transporter = transporter;
    }

    public String getExchanger() {
        return this.exchanger;
    }

    public void setExchanger(String exchanger) {
        ProviderConfig.checkExtension(Exchanger.class, "exchanger", exchanger);
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
        ProviderConfig.checkExtension(Dispatcher.class, "dispatcher", this.exchanger);
        ProviderConfig.checkExtension(Dispatcher.class, "dispather", this.exchanger);
        this.dispatcher = dispatcher;
    }

    public String getNetworker() {
        return this.networker;
    }

    public void setNetworker(String networker) {
        this.networker = networker;
    }

    public Integer getWait() {
        return this.wait;
    }

    public void setWait(Integer wait) {
        this.wait = wait;
    }

    public Boolean getEpoll() {
        return this.isEpoll;
    }

    public void setEpoll(Boolean epoll) {
        this.isEpoll = epoll;
    }
}

