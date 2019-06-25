/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.filter.tps;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import java.util.concurrent.atomic.AtomicInteger;

class StatItem {
    private String name;
    private long lastResetTime;
    private long interval;
    private AtomicInteger token;
    private int rate;

    StatItem(String name, int rate, long interval) {
        this.name = name;
        this.rate = rate;
        this.interval = interval;
        this.lastResetTime = System.currentTimeMillis();
        this.token = new AtomicInteger(rate);
    }

    public boolean isAllowable(URL url, Invocation invocation) {
        long now = System.currentTimeMillis();
        if (now > this.lastResetTime + this.interval) {
            this.token.set(this.rate);
            this.lastResetTime = now;
        }
        int value = this.token.get();
        boolean flag = false;
        while (value > 0 && !flag) {
            flag = this.token.compareAndSet(value, value - 1);
            value = this.token.get();
        }
        return flag;
    }

    long getLastResetTime() {
        return this.lastResetTime;
    }

    int getToken() {
        return this.token.get();
    }

    public String toString() {
        return new StringBuilder(32).append("StatItem ").append("[name=").append(this.name).append(", ").append("rate = ").append(this.rate).append(", ").append("interval = ").append(this.interval).append("]").toString();
    }
}

