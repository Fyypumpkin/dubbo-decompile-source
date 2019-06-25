/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.configurator.absent;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.cluster.configurator.AbstractConfigurator;
import java.util.Map;

public class AbsentConfigurator
extends AbstractConfigurator {
    public AbsentConfigurator(URL url) {
        super(url);
    }

    @Override
    public URL doConfigure(URL currentUrl, URL configUrl) {
        return currentUrl.addParametersIfAbsent(configUrl.getParameters());
    }
}

