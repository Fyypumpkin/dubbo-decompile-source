/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry;

import com.alibaba.dubbo.common.URL;
import java.util.List;

public interface NotifyListener {
    public void notify(List<URL> var1);
}

