/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.support.Parameter;
import java.util.Map;

public class RegistryConfig
extends AbstractConfig {
    public static final String NO_AVAILABLE = "N/A";
    private static final long serialVersionUID = 5508512956753757169L;
    private String address;
    private String username;
    private String password;
    private Integer port;
    private String protocol;
    private String transporter;
    private String server;
    private String client;
    private String cluster;
    private String group;
    private String version;
    private Integer timeout;
    private Integer session;
    private String file;
    private Integer wait;
    private Boolean check;
    private Boolean dynamic;
    private Boolean register;
    private Boolean subscribe;
    private Map<String, String> parameters;
    private Boolean isDefault;
    private Boolean total;
    private String subscribes;
    private String excludes;
    private Boolean isDns;

    public RegistryConfig() {
    }

    public RegistryConfig(String address) {
        this.setAddress(address);
    }

    public RegistryConfig(String address, String protocol) {
        this.setAddress(address);
        this.setProtocol(protocol);
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        RegistryConfig.checkName("protocol", protocol);
        this.protocol = protocol;
    }

    @Parameter(excluded=true)
    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        RegistryConfig.checkName("username", username);
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        RegistryConfig.checkLength("password", password);
        this.password = password;
    }

    @Deprecated
    public Integer getWait() {
        return this.wait;
    }

    @Deprecated
    public void setWait(Integer wait) {
        this.wait = wait;
        if (wait != null && wait > 0) {
            System.setProperty("dubbo.service.shutdown.wait", String.valueOf(wait));
        }
    }

    public Boolean isCheck() {
        return this.check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getFile() {
        return this.file;
    }

    public void setFile(String file) {
        RegistryConfig.checkPathLength("file", file);
        this.file = file;
    }

    @Deprecated
    @Parameter(excluded=true)
    public String getTransport() {
        return this.getTransporter();
    }

    @Deprecated
    public void setTransport(String transport) {
        this.setTransporter(transport);
    }

    public String getTransporter() {
        return this.transporter;
    }

    public void setTransporter(String transporter) {
        RegistryConfig.checkName("transporter", transporter);
        this.transporter = transporter;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        RegistryConfig.checkName("server", server);
        this.server = server;
    }

    public String getClient() {
        return this.client;
    }

    public void setClient(String client) {
        RegistryConfig.checkName("client", client);
        this.client = client;
    }

    public Integer getTimeout() {
        return this.timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getSession() {
        return this.session;
    }

    public void setSession(Integer session) {
        this.session = session;
    }

    public Boolean isDynamic() {
        return this.dynamic;
    }

    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }

    public Boolean isRegister() {
        return this.register;
    }

    public void setRegister(Boolean register) {
        this.register = register;
    }

    public Boolean isSubscribe() {
        return this.subscribe;
    }

    public void setSubscribe(Boolean subscribe) {
        this.subscribe = subscribe;
    }

    public String getCluster() {
        return this.cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        RegistryConfig.checkParameterName(parameters);
        this.parameters = parameters;
    }

    public Boolean isDefault() {
        return this.isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getTotal() {
        return this.total;
    }

    public void setTotal(Boolean total) {
        this.total = total;
    }

    @Parameter(excluded=true)
    public String getSubscribes() {
        return this.subscribes;
    }

    public void setSubscribes(String subscribes) {
        this.subscribes = subscribes;
    }

    @Parameter(excluded=true)
    public String getExcludes() {
        return this.excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    public Boolean isDns() {
        return this.isDns;
    }

    public void setDns(Boolean dns) {
        this.isDns = dns;
    }
}

