/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.common.compiler.support.AdaptiveCompiler;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.support.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplicationConfig
extends AbstractConfig {
    private static final long serialVersionUID = 5508512956753757169L;
    private String name;
    private String version;
    private String owner;
    private String organization;
    private String architecture;
    private String environment;
    private String compiler;
    private String logger;
    private List<RegistryConfig> registries;
    private MonitorConfig monitor;
    private Boolean isDefault;
    private String dumpDirectory;
    private Boolean qosEnable;
    private Integer qosPort;
    private Boolean qosAcceptForeignIp;
    private Map<String, String> parameters;

    public ApplicationConfig() {
    }

    public ApplicationConfig(String name) {
        this.setName(name);
    }

    @Parameter(key="application", required=true)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        ApplicationConfig.checkName("name", name);
        this.name = name;
        if (this.id == null || this.id.length() == 0) {
            this.id = name;
        }
    }

    @Parameter(key="application.version")
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
        ApplicationConfig.checkMultiName("owner", owner);
        this.owner = owner;
    }

    public String getOrganization() {
        return this.organization;
    }

    public void setOrganization(String organization) {
        ApplicationConfig.checkName("organization", organization);
        this.organization = organization;
    }

    public String getArchitecture() {
        return this.architecture;
    }

    public void setArchitecture(String architecture) {
        ApplicationConfig.checkName("architecture", architecture);
        this.architecture = architecture;
    }

    public String getEnvironment() {
        return this.environment;
    }

    public void setEnvironment(String environment) {
        ApplicationConfig.checkName("environment", environment);
        if (!(environment == null || "develop".equals(environment) || "test".equals(environment) || "product".equals(environment))) {
            throw new IllegalStateException("Unsupported environment: " + environment + ", only support develop/test/product, default is product.");
        }
        this.environment = environment;
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

    public void setMonitor(MonitorConfig monitor) {
        this.monitor = monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = new MonitorConfig(monitor);
    }

    public String getCompiler() {
        return this.compiler;
    }

    public void setCompiler(String compiler) {
        this.compiler = compiler;
        AdaptiveCompiler.setDefaultCompiler(compiler);
    }

    public String getLogger() {
        return this.logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
        LoggerFactory.setLoggerAdapter(logger);
    }

    public Boolean isDefault() {
        return this.isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Parameter(key="dump.directory")
    public String getDumpDirectory() {
        return this.dumpDirectory;
    }

    public void setDumpDirectory(String dumpDirectory) {
        this.dumpDirectory = dumpDirectory;
    }

    @Parameter(key="qos.enable")
    public Boolean getQosEnable() {
        return this.qosEnable;
    }

    public void setQosEnable(Boolean qosEnable) {
        this.qosEnable = qosEnable;
    }

    @Parameter(key="qos.port")
    public Integer getQosPort() {
        return this.qosPort;
    }

    public void setQosPort(Integer qosPort) {
        this.qosPort = qosPort;
    }

    @Parameter(key="qos.accept.foreign.ip")
    public Boolean getQosAcceptForeignIp() {
        return this.qosAcceptForeignIp;
    }

    public void setQosAcceptForeignIp(Boolean qosAcceptForeignIp) {
        this.qosAcceptForeignIp = qosAcceptForeignIp;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        ApplicationConfig.checkParameterName(parameters);
        this.parameters = parameters;
    }
}

