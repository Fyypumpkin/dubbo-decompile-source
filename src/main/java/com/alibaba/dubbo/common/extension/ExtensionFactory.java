/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.extension;

import com.alibaba.dubbo.common.extension.SPI;

@SPI
public interface ExtensionFactory {
    public <T> T getExtension(Class<T> var1, String var2);
}

