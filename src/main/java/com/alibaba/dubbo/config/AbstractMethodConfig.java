/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.support.Parameter;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import java.util.Map;

public abstract class AbstractMethodConfig
extends AbstractConfig {
    private static final long serialVersionUID = 1L;
    protected Integer timeout;
    protected Integer retries;
    protected Integer actives;
    protected String loadbalance;
    protected String router;
    protected Boolean async;
    protected Boolean sent;
    protected String mock;
    protected String merger;
    protected String cache;
    protected String validation;
    protected Map<String, String> parameters;

    public Integer getTimeout() {
        return this.timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getRetries() {
        return this.retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public String getLoadbalance() {
        return this.loadbalance;
    }

    public void setLoadbalance(String loadbalance) {
        AbstractMethodConfig.checkExtension(LoadBalance.class, "loadbalance", loadbalance);
        this.loadbalance = loadbalance;
    }

    public Boolean isAsync() {
        return this.async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Integer getActives() {
        return this.actives;
    }

    public void setActives(Integer actives) {
        this.actives = actives;
    }

    public Boolean getSent() {
        return this.sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    @Parameter(escaped=true)
    public String getMock() {
        return this.mock;
    }

    public void setMock(Boolean mock) {
        if (mock == null) {
            this.setMock((String)null);
        } else {
            this.setMock(String.valueOf(mock));
        }
    }

    public void setMock(String mock) {
        if (mock != null && mock.startsWith("return ")) {
            AbstractMethodConfig.checkLength("mock", mock);
        } else {
            AbstractMethodConfig.checkName("mock", mock);
        }
        this.mock = mock;
    }

    public String getMerger() {
        return this.merger;
    }

    public void setMerger(String merger) {
        this.merger = merger;
    }

    public String getCache() {
        return this.cache;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public String getValidation() {
        return this.validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        AbstractMethodConfig.checkParameterName(parameters);
        this.parameters = parameters;
    }

    public String getRouter() {
        return this.router;
    }

    public void setRouter(String router) {
        this.router = router;
    }
}

