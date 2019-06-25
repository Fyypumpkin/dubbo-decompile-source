/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.monitor;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.monitor.Monitor;

@SPI(value="dubbo")
public interface MonitorFactory {
    @Adaptive(value={"protocol"})
    public Monitor getMonitor(URL var1);
}

