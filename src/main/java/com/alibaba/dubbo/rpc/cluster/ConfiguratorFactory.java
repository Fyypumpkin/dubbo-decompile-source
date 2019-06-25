/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.cluster.Configurator;

@SPI
public interface ConfiguratorFactory {
    @Adaptive(value={"protocol"})
    public Configurator getConfigurator(URL var1);
}

