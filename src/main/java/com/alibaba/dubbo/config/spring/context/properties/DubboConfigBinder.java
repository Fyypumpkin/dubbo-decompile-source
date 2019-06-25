/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.context.EnvironmentAware
 */
package com.alibaba.dubbo.config.spring.context.properties;

import com.alibaba.dubbo.config.AbstractConfig;
import org.springframework.context.EnvironmentAware;

public interface DubboConfigBinder
extends EnvironmentAware {
    public void setIgnoreUnknownFields(boolean var1);

    public void setIgnoreInvalidFields(boolean var1);

    public <C extends AbstractConfig> void bind(String var1, C var2);
}

