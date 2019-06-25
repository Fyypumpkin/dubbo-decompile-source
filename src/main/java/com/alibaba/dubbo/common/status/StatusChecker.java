/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.status;

import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.common.status.Status;

@SPI
public interface StatusChecker {
    public Status check();
}

