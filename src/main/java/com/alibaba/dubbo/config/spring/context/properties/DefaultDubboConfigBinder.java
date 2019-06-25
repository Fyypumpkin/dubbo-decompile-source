/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.MutablePropertyValues
 *  org.springframework.beans.PropertyValues
 *  org.springframework.core.env.PropertySource
 *  org.springframework.validation.DataBinder
 */
package com.alibaba.dubbo.config.spring.context.properties;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.spring.context.properties.AbstractDubboConfigBinder;
import com.alibaba.dubbo.config.spring.util.PropertySourcesUtils;
import java.util.Map;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.core.env.PropertySource;
import org.springframework.validation.DataBinder;

public class DefaultDubboConfigBinder
extends AbstractDubboConfigBinder {
    @Override
    public <C extends AbstractConfig> void bind(String prefix, C dubboConfig) {
        DataBinder dataBinder = new DataBinder(dubboConfig);
        dataBinder.setIgnoreInvalidFields(this.isIgnoreInvalidFields());
        dataBinder.setIgnoreUnknownFields(this.isIgnoreUnknownFields());
        Map<String, String> properties = PropertySourcesUtils.getSubProperties(this.getPropertySources(), prefix);
        MutablePropertyValues propertyValues = new MutablePropertyValues(properties);
        dataBinder.bind((PropertyValues)propertyValues);
    }
}

