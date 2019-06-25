/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorUtil {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorUtil.class);
    private static final ThreadPoolExecutor shutdownExecutor = new ThreadPoolExecutor(0, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100), new NamedThreadFactory("Close-ExecutorService-Timer", true));

    public static boolean isShutdown(Executor executor) {
        return executor instanceof ExecutorService && ((ExecutorService)executor).isShutdown();
    }

    public static void gracefulShutdown(Executor executor, int timeout) {
        if (!(executor instanceof ExecutorService) || ExecutorUtil.isShutdown(executor)) {
            return;
        }
        ExecutorService es = (ExecutorService)executor;
        try {
            es.shutdown();
        }
        catch (SecurityException ex2) {
            return;
        }
        catch (NullPointerException ex2) {
            return;
        }
        try {
            if (!es.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                es.shutdownNow();
            }
        }
        catch (InterruptedException ex) {
            es.shutdownNow();
            Thread.currentThread().interrupt();
        }
        if (!ExecutorUtil.isShutdown(es)) {
            ExecutorUtil.newThreadToCloseExecutor(es);
        }
    }

    public static void shutdownNow(Executor executor, int timeout) {
        if (!(executor instanceof ExecutorService) || ExecutorUtil.isShutdown(executor)) {
            return;
        }
        ExecutorService es = (ExecutorService)executor;
        try {
            es.shutdownNow();
        }
        catch (SecurityException ex2) {
            return;
        }
        catch (NullPointerException ex2) {
            return;
        }
        try {
            es.awaitTermination(timeout, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (!ExecutorUtil.isShutdown(es)) {
            ExecutorUtil.newThreadToCloseExecutor(es);
        }
    }

    private static void newThreadToCloseExecutor(final ExecutorService es) {
        if (!ExecutorUtil.isShutdown(es)) {
            shutdownExecutor.execute(new Runnable(){

                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 1000; ++i) {
                            es.shutdownNow();
                            if (!es.awaitTermination(10L, TimeUnit.MILLISECONDS)) {
                                continue;
                            }
                            break;
                        }
                    }
                    catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    catch (Throwable e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            });
        }
    }

    public static URL setThreadName(URL url, String defaultName) {
        String name = url.getParameter("threadname", defaultName);
        name = new StringBuilder(32).append(name).append("-").append(url.getAddress()).toString();
        url = url.addParameter("threadname", name);
        return url;
    }

}

