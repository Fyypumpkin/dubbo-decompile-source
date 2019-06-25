/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.ImportSelector
 *  org.springframework.core.Ordered
 *  org.springframework.core.annotation.AnnotationAttributes
 *  org.springframework.core.type.AnnotationMetadata
 */
package com.alibaba.dubbo.config.spring.context.annotation;

import com.alibaba.dubbo.config.spring.context.annotation.DubboConfigConfiguration;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubboConfig;
import java.util.Map;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class DubboConfigConfigurationSelector
implements ImportSelector,
Ordered {
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap((Map)importingClassMetadata.getAnnotationAttributes(EnableDubboConfig.class.getName()));
        boolean multiple = attributes.getBoolean("multiple");
        if (multiple) {
            return DubboConfigConfigurationSelector.of(DubboConfigConfiguration.Multiple.class.getName());
        }
        return DubboConfigConfigurationSelector.of(DubboConfigConfiguration.Single.class.getName());
    }

    private static /* varargs */ <T> T[] of(T ... values) {
        return values;
    }

    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}

