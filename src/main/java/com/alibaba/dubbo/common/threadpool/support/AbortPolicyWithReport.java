/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.threadpool.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.JVMUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

public class AbortPolicyWithReport
extends ThreadPoolExecutor.AbortPolicy {
    protected static final Logger logger = LoggerFactory.getLogger(AbortPolicyWithReport.class);
    private final String threadName;
    private final URL url;
    private static volatile long lastPrintTime = 0L;
    private static Semaphore guard = new Semaphore(1);

    public AbortPolicyWithReport(String threadName, URL url) {
        this.threadName = threadName;
        this.url = url;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        String msg = String.format("Thread pool is EXHAUSTED! Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d), Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s), in %s://%s:%d!", this.threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(), e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating(), this.url.getProtocol(), this.url.getIp(), this.url.getPort());
        logger.warn(msg);
        this.dumpJStack();
        throw new RejectedExecutionException(msg);
    }

    private void dumpJStack() {
        long now = System.currentTimeMillis();
        if (now - lastPrintTime < 600000L) {
            return;
        }
        if (!guard.tryAcquire()) {
            return;
        }
        Executors.newSingleThreadExecutor().execute(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                String dumpPath = AbortPolicyWithReport.this.url.getParameter("dump.directory", System.getProperty("user.home"));
                String OS = System.getProperty("os.name").toLowerCase();
                SimpleDateFormat sdf = OS.contains("win") ? new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss") : new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String dateStr = sdf.format(new Date());
                OutputStream jstackStream = null;
                try {
                    jstackStream = new FileOutputStream(new File(dumpPath, "Dubbo_JStack.log." + dateStr));
                    JVMUtil.jstack(jstackStream);
                }
                catch (Throwable t) {
                    AbortPolicyWithReport.logger.error("dump jstack error", t);
                }
                finally {
                    guard.release();
                    if (jstackStream != null) {
                        try {
                            jstackStream.flush();
                            ((FileOutputStream)jstackStream).close();
                        }
                        catch (IOException iOException) {}
                    }
                }
                lastPrintTime = System.currentTimeMillis();
            }
        });
    }

}

