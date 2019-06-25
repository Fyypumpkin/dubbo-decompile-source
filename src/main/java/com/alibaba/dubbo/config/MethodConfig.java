/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.config.AbstractMethodConfig;
import com.alibaba.dubbo.config.ArgumentConfig;
import com.alibaba.dubbo.config.support.Parameter;
import java.util.List;

public class MethodConfig
extends AbstractMethodConfig {
    private static final long serialVersionUID = 884908855422675941L;
    private String name;
    private Integer stat;
    private Boolean retry;
    private Boolean reliable;
    private Integer executes;
    private Boolean deprecated;
    private Boolean sticky;
    private Boolean isReturn;
    private Object oninvoke;
    private String oninvokeMethod;
    private Object onreturn;
    private String onreturnMethod;
    private Object onthrow;
    private String onthrowMethod;
    private List<ArgumentConfig> arguments;

    @Parameter(excluded=true)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        MethodConfig.checkMethodName("name", name);
        this.name = name;
        if (this.id == null || this.id.length() == 0) {
            this.id = name;
        }
    }

    public Integer getStat() {
        return this.stat;
    }

    @Deprecated
    public void setStat(Integer stat) {
        this.stat = stat;
    }

    @Deprecated
    public Boolean isRetry() {
        return this.retry;
    }

    @Deprecated
    public void setRetry(Boolean retry) {
        this.retry = retry;
    }

    @Deprecated
    public Boolean isReliable() {
        return this.reliable;
    }

    @Deprecated
    public void setReliable(Boolean reliable) {
        this.reliable = reliable;
    }

    public Integer getExecutes() {
        return this.executes;
    }

    public void setExecutes(Integer executes) {
        this.executes = executes;
    }

    public Boolean getDeprecated() {
        return this.deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public List<ArgumentConfig> getArguments() {
        return this.arguments;
    }

    public void setArguments(List<? extends ArgumentConfig> arguments) {
        this.arguments = arguments;
    }

    public Boolean getSticky() {
        return this.sticky;
    }

    public void setSticky(Boolean sticky) {
        this.sticky = sticky;
    }

    @Parameter(key="onreturn.instance", excluded=true, attribute=true)
    public Object getOnreturn() {
        return this.onreturn;
    }

    public void setOnreturn(Object onreturn) {
        this.onreturn = onreturn;
    }

    @Parameter(key="onreturn.method", excluded=true, attribute=true)
    public String getOnreturnMethod() {
        return this.onreturnMethod;
    }

    public void setOnreturnMethod(String onreturnMethod) {
        this.onreturnMethod = onreturnMethod;
    }

    @Parameter(key="onthrow.instance", excluded=true, attribute=true)
    public Object getOnthrow() {
        return this.onthrow;
    }

    public void setOnthrow(Object onthrow) {
        this.onthrow = onthrow;
    }

    @Parameter(key="onthrow.method", excluded=true, attribute=true)
    public String getOnthrowMethod() {
        return this.onthrowMethod;
    }

    public void setOnthrowMethod(String onthrowMethod) {
        this.onthrowMethod = onthrowMethod;
    }

    @Parameter(key="oninvoke.instance", excluded=true, attribute=true)
    public Object getOninvoke() {
        return this.oninvoke;
    }

    public void setOninvoke(Object oninvoke) {
        this.oninvoke = oninvoke;
    }

    @Parameter(key="oninvoke.method", excluded=true, attribute=true)
    public String getOninvokeMethod() {
        return this.oninvokeMethod;
    }

    public void setOninvokeMethod(String oninvokeMethod) {
        this.oninvokeMethod = oninvokeMethod;
    }

    public Boolean isReturn() {
        return this.isReturn;
    }

    public void setReturn(Boolean isReturn) {
        this.isReturn = isReturn;
    }
}

