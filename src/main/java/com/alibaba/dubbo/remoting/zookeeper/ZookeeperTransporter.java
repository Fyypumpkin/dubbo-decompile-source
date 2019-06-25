/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.zookeeper;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperClient;

@SPI(value="zkclient")
public interface ZookeeperTransporter {
    @Adaptive(value={"client", "transporter"})
    public ZookeeperClient connect(URL var1);
}

