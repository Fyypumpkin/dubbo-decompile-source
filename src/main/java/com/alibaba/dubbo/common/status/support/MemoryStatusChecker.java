/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.status.support;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.status.Status;
import com.alibaba.dubbo.common.status.StatusChecker;

@Activate
public class MemoryStatusChecker
implements StatusChecker {
    @Override
    public Status check() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();
        boolean ok = maxMemory - (totalMemory - freeMemory) > 2048L;
        String msg = "max:" + maxMemory / 1024L / 1024L + "M,total:" + totalMemory / 1024L / 1024L + "M,used:" + (totalMemory / 1024L / 1024L - freeMemory / 1024L / 1024L) + "M,free:" + freeMemory / 1024L / 1024L + "M";
        return new Status(ok ? Status.Level.OK : Status.Level.WARN, msg);
    }
}

