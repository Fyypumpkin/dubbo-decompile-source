/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryFactory;
import com.alibaba.dubbo.registry.RegistryService;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractRegistryFactory
implements RegistryFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegistryFactory.class);
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Map<String, Registry> REGISTRIES = new ConcurrentHashMap<String, Registry>();

    public static Collection<Registry> getRegistries() {
        return Collections.unmodifiableCollection(REGISTRIES.values());
    }

    public static void destroyAll() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Close all registries " + AbstractRegistryFactory.getRegistries());
        }
        LOCK.lock();
        try {
            for (Registry registry : AbstractRegistryFactory.getRegistries()) {
                try {
                    registry.destroy();
                }
                catch (Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            REGISTRIES.clear();
        }
        finally {
            LOCK.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Registry getRegistry(URL url) {
        url = url.setPath(RegistryService.class.getName()).addParameter("interface", RegistryService.class.getName()).removeParameters("export", "refer");
        String key = url.toServiceStringWithoutResolving();
        LOCK.lock();
        try {
            Registry registry = REGISTRIES.get(key);
            if (registry != null) {
                Registry registry2 = registry;
                return registry2;
            }
            registry = this.createRegistry(url);
            if (registry == null) {
                throw new IllegalStateException("Can not create registry " + url);
            }
            REGISTRIES.put(key, registry);
            Registry registry3 = registry;
            return registry3;
        }
        finally {
            LOCK.unlock();
        }
    }

    protected abstract Registry createRegistry(URL var1);
}

