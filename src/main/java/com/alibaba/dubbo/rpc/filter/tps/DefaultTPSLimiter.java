/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.filter.tps;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.filter.tps.StatItem;
import com.alibaba.dubbo.rpc.filter.tps.TPSLimiter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultTPSLimiter
implements TPSLimiter {
    private final ConcurrentMap<String, StatItem> stats = new ConcurrentHashMap<String, StatItem>();

    @Override
    public boolean isAllowable(URL url, Invocation invocation) {
        int rate = url.getParameter("tps", -1);
        long interval = url.getParameter("tps.interval", 60000L);
        String serviceKey = url.getServiceKey();
        if (rate > 0) {
            StatItem statItem = (StatItem)this.stats.get(serviceKey);
            if (statItem == null) {
                this.stats.putIfAbsent(serviceKey, new StatItem(serviceKey, rate, interval));
                statItem = (StatItem)this.stats.get(serviceKey);
            }
            return statItem.isAllowable(url, invocation);
        }
        StatItem statItem = (StatItem)this.stats.get(serviceKey);
        if (statItem != null) {
            this.stats.remove(serviceKey);
        }
        return true;
    }
}

