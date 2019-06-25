/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.config.AbstractInterfaceConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.support.Parameter;
import com.alibaba.dubbo.rpc.ExporterListener;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractServiceConfig
extends AbstractInterfaceConfig {
    private static final long serialVersionUID = 1L;
    protected String version;
    protected String group;
    protected Boolean deprecated;
    protected Integer delay;
    protected Boolean export;
    protected Integer weight;
    protected String document;
    protected Boolean dynamic;
    protected String token;
    protected String accesslog;
    protected List<ProtocolConfig> protocols;
    private Integer executes;
    protected Boolean register;
    private Integer warmup;
    private String serialization;

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        AbstractServiceConfig.checkKey("version", version);
        this.version = version;
    }

    @Parameter(excluded=true)
    public String getTag() {
        return this.version;
    }

    public void setTag(String tag) {
        this.version = tag;
    }

    @Parameter(excluded=true)
    public String getValue() {
        return this.group;
    }

    public void setValue(String value) {
        this.setGroup(value);
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        AbstractServiceConfig.checkKey("group", group);
        this.group = group;
    }

    public Integer getDelay() {
        return this.delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Boolean getExport() {
        return this.export;
    }

    public void setExport(Boolean export) {
        this.export = export;
    }

    public Integer getWeight() {
        return this.weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Parameter(escaped=true)
    public String getDocument() {
        return this.document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        AbstractServiceConfig.checkName("token", token);
        this.token = token;
    }

    public void setToken(Boolean token) {
        if (token == null) {
            this.setToken((String)null);
        } else {
            this.setToken(String.valueOf(token));
        }
    }

    public Boolean isDeprecated() {
        return this.deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Boolean isDynamic() {
        return this.dynamic;
    }

    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }

    public List<ProtocolConfig> getProtocols() {
        return this.protocols;
    }

    public void setProtocols(List<? extends ProtocolConfig> protocols) {
        this.protocols = protocols;
    }

    public ProtocolConfig getProtocol() {
        return this.protocols == null || this.protocols.isEmpty() ? null : this.protocols.get(0);
    }

    public void setProtocol(ProtocolConfig protocol) {
        this.protocols = Arrays.asList(protocol);
    }

    public String getAccesslog() {
        return this.accesslog;
    }

    public void setAccesslog(String accesslog) {
        this.accesslog = accesslog;
    }

    public void setAccesslog(Boolean accesslog) {
        if (accesslog == null) {
            this.setAccesslog((String)null);
        } else {
            this.setAccesslog(String.valueOf(accesslog));
        }
    }

    public Integer getExecutes() {
        return this.executes;
    }

    public void setExecutes(Integer executes) {
        this.executes = executes;
    }

    @Parameter(key="service.filter", append=true)
    @Override
    public String getFilter() {
        return super.getFilter();
    }

    @Parameter(key="exporter.listener", append=true)
    @Override
    public String getListener() {
        return this.listener;
    }

    @Override
    public void setListener(String listener) {
        AbstractServiceConfig.checkMultiExtension(ExporterListener.class, "listener", listener);
        this.listener = listener;
    }

    public Boolean isRegister() {
        return this.register;
    }

    public void setRegister(Boolean register) {
        this.register = register;
    }

    public Integer getWarmup() {
        return this.warmup;
    }

    public void setWarmup(Integer warmup) {
        this.warmup = warmup;
    }

    public String getSerialization() {
        return this.serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }
}

