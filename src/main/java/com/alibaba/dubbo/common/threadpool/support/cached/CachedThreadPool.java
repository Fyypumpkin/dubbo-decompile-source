/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.threadpool.support.cached;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.threadpool.ThreadPool;
import com.alibaba.dubbo.common.threadpool.support.AbortPolicyWithReport;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CachedThreadPool
implements ThreadPool {
    @Override
    public Executor getExecutor(URL url) {
        String name = url.getParameter("threadname", "Dubbo");
        int cores = url.getParameter("corethreads", 0);
        int threads = url.getParameter("threads", Integer.MAX_VALUE);
        int queues = url.getParameter("queues", 0);
        int alive = url.getParameter("alive", 60000);
        return new ThreadPoolExecutor(cores, threads, alive, TimeUnit.MILLISECONDS, (BlockingQueue<Runnable>)((Object)(queues == 0 ? new SynchronousQueue() : (queues < 0 ? new LinkedBlockingQueue() : new LinkedBlockingQueue(queues)))), new NamedThreadFactory(name, true), new AbortPolicyWithReport(name, url));
    }
}

