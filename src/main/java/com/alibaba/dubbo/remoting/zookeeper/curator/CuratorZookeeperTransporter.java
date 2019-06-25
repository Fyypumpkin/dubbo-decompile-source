/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.zookeeper.curator;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperClient;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperTransporter;
import com.alibaba.dubbo.remoting.zookeeper.curator.CuratorZookeeperClient;

public class CuratorZookeeperTransporter
implements ZookeeperTransporter {
    @Override
    public ZookeeperClient connect(URL url) {
        return new CuratorZookeeperClient(url);
    }
}

