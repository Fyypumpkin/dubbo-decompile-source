/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.utils;

import com.alibaba.dubbo.common.extension.ExtensionFactory;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.config.AbstractConfig;
import java.util.Set;

public class ReferenceConfigHelper {
    private static final ExtensionLoader<ExtensionFactory> extensionFactoryLoader = ExtensionLoader.getExtensionLoader(ExtensionFactory.class);

    public static <T extends AbstractConfig> T findConfig(Class<T> type, String id) {
        Set<String> names = extensionFactoryLoader.getSupportedExtensions();
        for (String name : names) {
            ExtensionFactory factory = extensionFactoryLoader.getExtension(name);
            AbstractConfig instance = (AbstractConfig)factory.getExtension(type, id);
            if (instance == null) continue;
            return (T)instance;
        }
        return null;
    }
}

