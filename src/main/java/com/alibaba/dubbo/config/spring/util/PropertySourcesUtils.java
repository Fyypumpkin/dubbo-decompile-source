/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.core.env.EnumerablePropertySource
 *  org.springframework.core.env.PropertySource
 */
package com.alibaba.dubbo.config.spring.util;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

public abstract class PropertySourcesUtils {
    public static Map<String, String> getSubProperties(Iterable<PropertySource<?>> propertySources, String prefix) {
        LinkedHashMap<String, String> subProperties = new LinkedHashMap<String, String>();
        String normalizedPrefix = PropertySourcesUtils.normalizePrefix(prefix);
        for (PropertySource<?> source : propertySources) {
            if (!(source instanceof EnumerablePropertySource)) continue;
            for (String name : ((EnumerablePropertySource)source).getPropertyNames()) {
                if (!name.startsWith(normalizedPrefix)) continue;
                String subName = name.substring(normalizedPrefix.length());
                Object value = source.getProperty(name);
                subProperties.put(subName, String.valueOf(value));
            }
        }
        return subProperties;
    }

    public static String normalizePrefix(String prefix) {
        return prefix.endsWith(".") ? prefix : prefix + ".";
    }
}

