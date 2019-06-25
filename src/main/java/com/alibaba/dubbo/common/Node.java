/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common;

import com.alibaba.dubbo.common.URL;

public interface Node {
    public URL getUrl();

    public boolean isAvailable();

    public void destroy();
}

