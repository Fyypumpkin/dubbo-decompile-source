/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.container.page;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.container.page.Page;

@SPI
public interface PageHandler {
    public Page handle(URL var1);
}

