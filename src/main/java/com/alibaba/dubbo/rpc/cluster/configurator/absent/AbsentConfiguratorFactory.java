/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.configurator.absent;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.cluster.Configurator;
import com.alibaba.dubbo.rpc.cluster.ConfiguratorFactory;
import com.alibaba.dubbo.rpc.cluster.configurator.absent.AbsentConfigurator;

public class AbsentConfiguratorFactory
implements ConfiguratorFactory {
    @Override
    public Configurator getConfigurator(URL url) {
        return new AbsentConfigurator(url);
    }
}

