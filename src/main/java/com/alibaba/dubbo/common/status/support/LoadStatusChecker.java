/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.status.support;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.status.Status;
import com.alibaba.dubbo.common.status.StatusChecker;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

@Activate
public class LoadStatusChecker
implements StatusChecker {
    @Override
    public Status check() {
        double load;
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        try {
            Method method = OperatingSystemMXBean.class.getMethod("getSystemLoadAverage", new Class[0]);
            load = (Double)method.invoke(operatingSystemMXBean, new Object[0]);
        }
        catch (Throwable e) {
            load = -1.0;
        }
        int cpu = operatingSystemMXBean.getAvailableProcessors();
        return new Status(load < 0.0 ? Status.Level.UNKNOWN : (load < (double)cpu ? Status.Level.OK : Status.Level.WARN), (load < 0.0 ? "" : new StringBuilder().append("load:").append(load).append(",").toString()) + "cpu:" + cpu);
    }
}

