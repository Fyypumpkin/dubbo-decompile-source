/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.extension.factory;

import com.alibaba.dubbo.common.extension.ExtensionFactory;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.extension.SPI;
import java.lang.annotation.Annotation;
import java.util.Set;

public class SpiExtensionFactory
implements ExtensionFactory {
    @Override
    public <T> T getExtension(Class<T> type, String name) {
        ExtensionLoader<T> loader;
        if (type.isInterface() && type.isAnnotationPresent(SPI.class) && (loader = ExtensionLoader.getExtensionLoader(type)).getSupportedExtensions().size() > 0) {
            return loader.getAdaptiveExtension();
        }
        return null;
    }
}

