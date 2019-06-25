/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.monitor.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.monitor.Monitor;
import com.alibaba.dubbo.monitor.MonitorService;
import com.alibaba.dubbo.monitor.dubbo.Statistics;
import com.alibaba.dubbo.rpc.Invoker;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DubboMonitor
implements Monitor {
    private static final Logger logger = LoggerFactory.getLogger(DubboMonitor.class);
    private static final int LENGTH = 10;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3, new NamedThreadFactory("DubboMonitorSendTimer", true));
    private final ScheduledFuture<?> sendFuture;
    private final Invoker<MonitorService> monitorInvoker;
    private final MonitorService monitorService;
    private final long monitorInterval;
    private final ConcurrentMap<Statistics, AtomicReference<long[]>> statisticsMap = new ConcurrentHashMap<Statistics, AtomicReference<long[]>>();

    public DubboMonitor(Invoker<MonitorService> monitorInvoker, MonitorService monitorService) {
        this.monitorInvoker = monitorInvoker;
        this.monitorService = monitorService;
        this.monitorInterval = monitorInvoker.getUrl().getPositiveParameter("interval", 60000);
        this.sendFuture = this.scheduledExecutorService.scheduleWithFixedDelay(new Runnable(){

            @Override
            public void run() {
                try {
                    DubboMonitor.this.send();
                }
                catch (Throwable t) {
                    logger.error("Unexpected error occur at send statistic, cause: " + t.getMessage(), t);
                }
            }
        }, this.monitorInterval, this.monitorInterval, TimeUnit.MILLISECONDS);
    }

    public void send() {
        if (logger.isInfoEnabled()) {
            logger.info("Send statistics to monitor " + this.getUrl());
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        for (Map.Entry entry : this.statisticsMap.entrySet()) {
            long[] current;
            Statistics statistics = (Statistics)entry.getKey();
            AtomicReference reference = (AtomicReference)entry.getValue();
            long[] numbers = (long[])reference.get();
            long success = numbers[0];
            long failure = numbers[1];
            long input = numbers[2];
            long output = numbers[3];
            long elapsed = numbers[4];
            long concurrent = numbers[5];
            long maxInput = numbers[6];
            long maxOutput = numbers[7];
            long maxElapsed = numbers[8];
            long maxConcurrent = numbers[9];
            URL url = statistics.getUrl().addParameters("timestamp", timestamp, "success", String.valueOf(success), "failure", String.valueOf(failure), "input", String.valueOf(input), "output", String.valueOf(output), "elapsed", String.valueOf(elapsed), "concurrent", String.valueOf(concurrent), "max.input", String.valueOf(maxInput), "max.output", String.valueOf(maxOutput), "max.elapsed", String.valueOf(maxElapsed), "max.concurrent", String.valueOf(maxConcurrent));
            this.monitorService.collect(url);
            long[] update = new long[10];
            do {
                if ((current = (long[])reference.get()) == null) {
                    update[0] = 0L;
                    update[1] = 0L;
                    update[2] = 0L;
                    update[3] = 0L;
                    update[4] = 0L;
                    update[5] = 0L;
                    continue;
                }
                update[0] = current[0] - success;
                update[1] = current[1] - failure;
                update[2] = current[2] - input;
                update[3] = current[3] - output;
                update[4] = current[4] - elapsed;
                update[5] = current[5] - concurrent;
            } while (!reference.compareAndSet(current, update));
        }
    }

    @Override
    public void collect(URL url) {
        long[] current;
        int success = url.getParameter("success", 0);
        int failure = url.getParameter("failure", 0);
        int input = url.getParameter("input", 0);
        int output = url.getParameter("output", 0);
        int elapsed = url.getParameter("elapsed", 0);
        int concurrent = url.getParameter("concurrent", 0);
        Statistics statistics = new Statistics(url);
        AtomicReference reference = (AtomicReference)this.statisticsMap.get(statistics);
        if (reference == null) {
            this.statisticsMap.putIfAbsent(statistics, new AtomicReference());
            reference = (AtomicReference)this.statisticsMap.get(statistics);
        }
        long[] update = new long[10];
        do {
            if ((current = (long[])reference.get()) == null) {
                update[0] = success;
                update[1] = failure;
                update[2] = input;
                update[3] = output;
                update[4] = elapsed;
                update[5] = concurrent;
                update[6] = input;
                update[7] = output;
                update[8] = elapsed;
                update[9] = concurrent;
                continue;
            }
            update[0] = current[0] + (long)success;
            update[1] = current[1] + (long)failure;
            update[2] = current[2] + (long)input;
            update[3] = current[3] + (long)output;
            update[4] = current[4] + (long)elapsed;
            update[5] = (current[5] + (long)concurrent) / 2L;
            update[6] = current[6] > (long)input ? current[6] : (long)input;
            update[7] = current[7] > (long)output ? current[7] : (long)output;
            update[8] = current[8] > (long)elapsed ? current[8] : (long)elapsed;
            long l = update[9] = current[9] > (long)concurrent ? current[9] : (long)concurrent;
        } while (!reference.compareAndSet(current, update));
    }

    @Override
    public List<URL> lookup(URL query) {
        return this.monitorService.lookup(query);
    }

    @Override
    public URL getUrl() {
        return this.monitorInvoker.getUrl();
    }

    @Override
    public boolean isAvailable() {
        return this.monitorInvoker.isAvailable();
    }

    @Override
    public void destroy() {
        try {
            this.sendFuture.cancel(true);
        }
        catch (Throwable t) {
            logger.error("Unexpected error occur at cancel sender timer, cause: " + t.getMessage(), t);
        }
        this.monitorInvoker.destroy();
    }

}

