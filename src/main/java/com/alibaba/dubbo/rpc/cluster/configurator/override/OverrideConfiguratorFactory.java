/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.configurator.override;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.cluster.Configurator;
import com.alibaba.dubbo.rpc.cluster.ConfiguratorFactory;
import com.alibaba.dubbo.rpc.cluster.configurator.override.OverrideConfigurator;

public class OverrideConfiguratorFactory
implements ConfiguratorFactory {
    @Override
    public Configurator getConfigurator(URL url) {
        return new OverrideConfigurator(url);
    }
}

