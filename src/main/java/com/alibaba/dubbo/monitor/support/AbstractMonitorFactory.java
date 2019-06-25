/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.monitor.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.monitor.Monitor;
import com.alibaba.dubbo.monitor.MonitorFactory;
import com.alibaba.dubbo.monitor.MonitorService;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractMonitorFactory
implements MonitorFactory {
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Map<String, Monitor> MONITORS = new ConcurrentHashMap<String, Monitor>();

    public static Collection<Monitor> getMonitors() {
        return Collections.unmodifiableCollection(MONITORS.values());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Monitor getMonitor(URL url) {
        url = url.setPath(MonitorService.class.getName()).addParameter("interface", MonitorService.class.getName());
        String key = url.toServiceStringWithoutResolving();
        LOCK.lock();
        try {
            Monitor monitor = MONITORS.get(key);
            if (monitor != null) {
                Monitor monitor2 = monitor;
                return monitor2;
            }
            monitor = this.createMonitor(url);
            if (monitor == null) {
                throw new IllegalStateException("Can not create monitor " + url);
            }
            MONITORS.put(key, monitor);
            Monitor monitor3 = monitor;
            return monitor3;
        }
        finally {
            LOCK.unlock();
        }
    }

    protected abstract Monitor createMonitor(URL var1);
}

