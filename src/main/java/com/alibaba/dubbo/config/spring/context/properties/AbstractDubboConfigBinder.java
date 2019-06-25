/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.core.env.ConfigurableEnvironment
 *  org.springframework.core.env.Environment
 *  org.springframework.core.env.MutablePropertySources
 *  org.springframework.core.env.PropertySource
 */
package com.alibaba.dubbo.config.spring.context.properties;

import com.alibaba.dubbo.config.spring.context.properties.DubboConfigBinder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

public abstract class AbstractDubboConfigBinder
implements DubboConfigBinder {
    private Iterable<PropertySource<?>> propertySources;
    private boolean ignoreUnknownFields = true;
    private boolean ignoreInvalidFields = false;

    protected Iterable<PropertySource<?>> getPropertySources() {
        return this.propertySources;
    }

    public boolean isIgnoreUnknownFields() {
        return this.ignoreUnknownFields;
    }

    @Override
    public void setIgnoreUnknownFields(boolean ignoreUnknownFields) {
        this.ignoreUnknownFields = ignoreUnknownFields;
    }

    public boolean isIgnoreInvalidFields() {
        return this.ignoreInvalidFields;
    }

    @Override
    public void setIgnoreInvalidFields(boolean ignoreInvalidFields) {
        this.ignoreInvalidFields = ignoreInvalidFields;
    }

    public final void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            this.propertySources = ((ConfigurableEnvironment)environment).getPropertySources();
        }
    }
}

