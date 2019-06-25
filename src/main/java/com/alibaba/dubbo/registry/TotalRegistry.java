/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.Registry;
import java.util.List;

public interface TotalRegistry
extends Registry {
    public void totalRegister(List<URL> var1);

    public void totalUnRegister();
}

