/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.config.support.Parameter;
import java.io.Serializable;

public class ArgumentConfig
implements Serializable {
    private static final long serialVersionUID = -2165482463925213595L;
    private Integer index = -1;
    private String type;
    private Boolean callback;

    @Parameter(excluded=true)
    public Integer getIndex() {
        return this.index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @Parameter(excluded=true)
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCallback(Boolean callback) {
        this.callback = callback;
    }

    public Boolean isCallback() {
        return this.callback;
    }
}

