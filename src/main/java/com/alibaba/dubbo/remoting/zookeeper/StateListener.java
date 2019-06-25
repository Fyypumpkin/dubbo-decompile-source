/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.zookeeper;

public interface StateListener {
    public static final int DISCONNECTED = 0;
    public static final int CONNECTED = 1;
    public static final int RECONNECTED = 2;

    public void stateChanged(int var1);
}

