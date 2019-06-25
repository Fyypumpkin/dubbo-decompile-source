/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.zookeeper;

import java.util.List;

public interface ChildListener {
    public void childChanged(String var1, List<String> var2);
}

