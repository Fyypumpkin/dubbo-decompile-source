/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.registry.Registry;

@SPI(value="dubbo")
public interface RegistryFactory {
    @Adaptive(value={"protocol"})
    public Registry getRegistry(URL var1);
}

