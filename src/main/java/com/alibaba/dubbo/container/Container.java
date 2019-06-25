/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.container;

import com.alibaba.dubbo.common.extension.SPI;

@SPI(value="spring")
public interface Container {
    public void start();

    public void stop();
}

