/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.support.Parameter;
import java.util.ArrayList;
import java.util.List;

public class ModuleConfig
extends AbstractConfig {
    private static final long serialVersionUID = 5508512956753757169L;
    private String name;
    private String version;
    private String owner;
    private String organization;
    private List<RegistryConfig> registries;
    private MonitorConfig monitor;
    private Boolean isDefault;

    public ModuleConfig() {
    }

    public ModuleConfig(String name) {
        this.setName(name);
    }

    @Parameter(key="module", required=true)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        ModuleConfig.checkName("name", name);
        this.name = name;
        if (this.id == null || this.id.length() == 0) {
            this.id = name;
        }
    }

    @Parameter(key="module.version")
    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        ModuleConfig.checkName("owner", owner);
        this.owner = owner;
    }

    public String getOrganization() {
        return this.organization;
    }

    public void setOrganization(String organization) {
        ModuleConfig.checkName("organization", organization);
        this.organization = organization;
    }

    public RegistryConfig getRegistry() {
        return this.registries == null || this.registries.isEmpty() ? null : this.registries.get(0);
    }

    public void setRegistry(RegistryConfig registry) {
        ArrayList<RegistryConfig> registries = new ArrayList<RegistryConfig>(1);
        registries.add(registry);
        this.registries = registries;
    }

    public List<RegistryConfig> getRegistries() {
        return this.registries;
    }

    public void setRegistries(List<? extends RegistryConfig> registries) {
        this.registries = registries;
    }

    public MonitorConfig getMonitor() {
        return this.monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = new MonitorConfig(monitor);
    }

    public void setMonitor(MonitorConfig monitor) {
        this.monitor = monitor;
    }

    public Boolean isDefault() {
        return this.isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}

