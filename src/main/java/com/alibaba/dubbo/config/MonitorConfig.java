/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.support.Parameter;
import java.util.Map;

public class MonitorConfig
extends AbstractConfig {
    private static final long serialVersionUID = -1184681514659198203L;
    private String protocol;
    private String address;
    private String username;
    private String password;
    private String group;
    private String version;
    private String interval;
    private Map<String, String> parameters;
    private Boolean isDefault;

    public MonitorConfig() {
    }

    public MonitorConfig(String address) {
        this.address = address;
    }

    @Parameter(excluded=true)
    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Parameter(excluded=true)
    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Parameter(excluded=true)
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Parameter(excluded=true)
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        MonitorConfig.checkParameterName(parameters);
        this.parameters = parameters;
    }

    public Boolean isDefault() {
        return this.isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getInterval() {
        return this.interval;
    }
}

