/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.threadpool;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import java.util.concurrent.Executor;

@SPI(value="fixed")
public interface ThreadPool {
    @Adaptive(value={"threadpool"})
    public Executor getExecutor(URL var1);
}

