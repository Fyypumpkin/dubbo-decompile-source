/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.zookeeper;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.zookeeper.ChildListener;
import com.alibaba.dubbo.remoting.zookeeper.StateListener;
import java.util.List;

public interface ZookeeperClient {
    public void create(String var1, boolean var2);

    public void delete(String var1);

    public List<String> getChildren(String var1);

    public List<String> addChildListener(String var1, ChildListener var2);

    public void removeChildListener(String var1, ChildListener var2);

    public void addStateListener(StateListener var1);

    public void removeStateListener(StateListener var1);

    public boolean isConnected();

    public void close();

    public URL getUrl();
}

