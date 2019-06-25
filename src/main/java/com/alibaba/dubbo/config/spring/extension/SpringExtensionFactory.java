/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.context.ApplicationContext
 */
package com.alibaba.dubbo.config.spring.extension;

import com.alibaba.dubbo.common.extension.ExtensionFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import java.util.Set;
import org.springframework.context.ApplicationContext;

public class SpringExtensionFactory
implements ExtensionFactory {
    private static final Set<ApplicationContext> contexts = new ConcurrentHashSet<ApplicationContext>();

    public static void addApplicationContext(ApplicationContext context) {
        contexts.add(context);
    }

    public static void removeApplicationContext(ApplicationContext context) {
        contexts.remove((Object)context);
    }

    public Set<ApplicationContext> getContexts() {
        return contexts;
    }

    @Override
    public <T> T getExtension(Class<T> type, String name) {
        for (ApplicationContext context : contexts) {
            Object bean;
            if (!context.containsBean(name) || !type.isInstance(bean = context.getBean(name))) continue;
            return (T)bean;
        }
        return null;
    }
}

