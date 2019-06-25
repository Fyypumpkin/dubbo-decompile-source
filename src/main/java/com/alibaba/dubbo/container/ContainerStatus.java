/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.container;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContainerStatus {
    private static Logger logger = LoggerFactory.getLogger(ContainerStatus.class);
    private static AtomicBoolean protocolRunning = new AtomicBoolean(false);
    private static AtomicBoolean springRunning = new AtomicBoolean(false);

    public static void protocolStart() {
        protocolRunning.compareAndSet(false, true);
    }

    public static void protocolEnd() {
        protocolRunning.compareAndSet(true, false);
    }

    public static void springStart() {
        springRunning.compareAndSet(false, true);
    }

    public static void springEnd() {
        springRunning.compareAndSet(true, false);
    }

    public static boolean getProtocolRunning() {
        return protocolRunning.get();
    }

    public static boolean getSpringRunning() {
        return springRunning.get();
    }
}

