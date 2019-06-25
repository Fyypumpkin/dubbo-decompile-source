/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster;

import com.alibaba.dubbo.common.URL;

public interface Configurator
extends Comparable<Configurator> {
    public URL getUrl();

    public URL configure(URL var1);
}

