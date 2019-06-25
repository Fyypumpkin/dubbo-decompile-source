/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RpcStatus {
    private static final ConcurrentMap<String, RpcStatus> SERVICE_STATISTICS = new ConcurrentHashMap<String, RpcStatus>();
    private static final ConcurrentMap<String, ConcurrentMap<String, RpcStatus>> METHOD_STATISTICS = new ConcurrentHashMap<String, ConcurrentMap<String, RpcStatus>>();
    private final ConcurrentMap<String, Object> values = new ConcurrentHashMap<String, Object>();
    private final AtomicInteger active = new AtomicInteger();
    private final AtomicLong total = new AtomicLong();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicLong totalElapsed = new AtomicLong();
    private final AtomicLong failedElapsed = new AtomicLong();
    private final AtomicLong maxElapsed = new AtomicLong();
    private final AtomicLong failedMaxElapsed = new AtomicLong();
    private final AtomicLong succeededMaxElapsed = new AtomicLong();

    public static RpcStatus getStatus(URL url) {
        String uri = url.toIdentityString();
        RpcStatus status = (RpcStatus)SERVICE_STATISTICS.get(uri);
        if (status == null) {
            SERVICE_STATISTICS.putIfAbsent(uri, new RpcStatus());
            status = (RpcStatus)SERVICE_STATISTICS.get(uri);
        }
        return status;
    }

    public static void removeStatus(URL url) {
        String uri = url.toIdentityString();
        SERVICE_STATISTICS.remove(uri);
    }

    public static RpcStatus getStatus(URL url, String methodName) {
        RpcStatus status;
        String uri = url.toIdentityString();
        ConcurrentMap map = (ConcurrentMap)METHOD_STATISTICS.get(uri);
        if (map == null) {
            METHOD_STATISTICS.putIfAbsent(uri, new ConcurrentHashMap());
            map = (ConcurrentMap)METHOD_STATISTICS.get(uri);
        }
        if ((status = (RpcStatus)map.get(methodName)) == null) {
            map.putIfAbsent(methodName, new RpcStatus());
            status = (RpcStatus)map.get(methodName);
        }
        return status;
    }

    public static void removeStatus(URL url, String methodName) {
        String uri = url.toIdentityString();
        ConcurrentMap map = (ConcurrentMap)METHOD_STATISTICS.get(uri);
        if (map != null) {
            map.remove(methodName);
        }
    }

    public static void beginCount(URL url, String methodName) {
        RpcStatus.beginCount(RpcStatus.getStatus(url));
        RpcStatus.beginCount(RpcStatus.getStatus(url, methodName));
    }

    private static void beginCount(RpcStatus status) {
        status.active.incrementAndGet();
    }

    public static void endCount(URL url, String methodName, long elapsed, boolean succeeded) {
        RpcStatus.endCount(RpcStatus.getStatus(url), elapsed, succeeded);
        RpcStatus.endCount(RpcStatus.getStatus(url, methodName), elapsed, succeeded);
    }

    private static void endCount(RpcStatus status, long elapsed, boolean succeeded) {
        status.active.decrementAndGet();
        status.total.incrementAndGet();
        status.totalElapsed.addAndGet(elapsed);
        if (status.maxElapsed.get() < elapsed) {
            status.maxElapsed.set(elapsed);
        }
        if (succeeded) {
            if (status.succeededMaxElapsed.get() < elapsed) {
                status.succeededMaxElapsed.set(elapsed);
            }
        } else {
            status.failed.incrementAndGet();
            status.failedElapsed.addAndGet(elapsed);
            if (status.failedMaxElapsed.get() < elapsed) {
                status.failedMaxElapsed.set(elapsed);
            }
        }
    }

    private RpcStatus() {
    }

    public void set(String key, Object value) {
        this.values.put(key, value);
    }

    public Object get(String key) {
        return this.values.get(key);
    }

    public int getActive() {
        return this.active.get();
    }

    public long getTotal() {
        return this.total.longValue();
    }

    public long getTotalElapsed() {
        return this.totalElapsed.get();
    }

    public long getAverageElapsed() {
        long total = this.getTotal();
        if (total == 0L) {
            return 0L;
        }
        return this.getTotalElapsed() / total;
    }

    public long getMaxElapsed() {
        return this.maxElapsed.get();
    }

    public int getFailed() {
        return this.failed.get();
    }

    public long getFailedElapsed() {
        return this.failedElapsed.get();
    }

    public long getFailedAverageElapsed() {
        long failed = this.getFailed();
        if (failed == 0L) {
            return 0L;
        }
        return this.getFailedElapsed() / failed;
    }

    public long getFailedMaxElapsed() {
        return this.failedMaxElapsed.get();
    }

    public long getSucceeded() {
        return this.getTotal() - (long)this.getFailed();
    }

    public long getSucceededElapsed() {
        return this.getTotalElapsed() - this.getFailedElapsed();
    }

    public long getSucceededAverageElapsed() {
        long succeeded = this.getSucceeded();
        if (succeeded == 0L) {
            return 0L;
        }
        return this.getSucceededElapsed() / succeeded;
    }

    public long getSucceededMaxElapsed() {
        return this.succeededMaxElapsed.get();
    }

    public long getAverageTps() {
        if (this.getTotalElapsed() >= 1000L) {
            return this.getTotal() / (this.getTotalElapsed() / 1000L);
        }
        return this.getTotal();
    }
}

