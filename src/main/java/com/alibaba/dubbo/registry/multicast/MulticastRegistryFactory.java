/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.multicast;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.multicast.MulticastRegistry;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;

public class MulticastRegistryFactory
extends AbstractRegistryFactory {
    @Override
    public Registry createRegistry(URL url) {
        return new MulticastRegistry(url);
    }
}

