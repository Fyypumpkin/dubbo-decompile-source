/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.cluster.Router;

@SPI
public interface RouterFactory {
    @Adaptive(value={"protocol"})
    public Router getRouter(URL var1);
}

