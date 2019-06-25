/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.RegistryService;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import com.alibaba.dubbo.rpc.Invoker;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class DubboRegistry
extends FailbackRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DubboRegistry.class);
    private static final int RECONNECT_PERIOD_DEFAULT = 3000;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory("DubboRegistryReconnectTimer", true));
    private final ScheduledFuture<?> reconnectFuture;
    private final ReentrantLock clientLock = new ReentrantLock();
    private final Invoker<RegistryService> registryInvoker;
    private final RegistryService registryService;

    public DubboRegistry(Invoker<RegistryService> registryInvoker, RegistryService registryService) {
        super(registryInvoker.getUrl());
        this.registryInvoker = registryInvoker;
        this.registryService = registryService;
        int reconnectPeriod = registryInvoker.getUrl().getParameter("reconnect.period", 3000);
        this.reconnectFuture = this.scheduledExecutorService.scheduleWithFixedDelay(new Runnable(){

            @Override
            public void run() {
                try {
                    DubboRegistry.this.connect();
                }
                catch (Throwable t) {
                    logger.error("Unexpected error occur at reconnect, cause: " + t.getMessage(), t);
                }
            }
        }, reconnectPeriod, reconnectPeriod, TimeUnit.MILLISECONDS);
    }

    protected final void connect() {
        try {
            if (this.isAvailable()) {
                return;
            }
            if (logger.isInfoEnabled()) {
                logger.info("Reconnect to registry " + this.getUrl());
            }
            this.clientLock.lock();
            try {
                if (this.isAvailable()) {
                    return;
                }
                this.recover();
            }
            finally {
                this.clientLock.unlock();
            }
        }
        catch (Throwable t) {
            if (this.getUrl().getParameter("check", true)) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                }
                throw new RuntimeException(t.getMessage(), t);
            }
            logger.error("Failed to connect to registry " + this.getUrl().getAddress() + " from provider/consumer " + NetUtils.getLocalHost() + " use dubbo " + Version.getVersion() + ", cause: " + t.getMessage(), t);
        }
    }

    @Override
    public boolean isAvailable() {
        if (this.registryInvoker == null) {
            return false;
        }
        return this.registryInvoker.isAvailable();
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            if (!this.reconnectFuture.isCancelled()) {
                this.reconnectFuture.cancel(true);
            }
        }
        catch (Throwable t) {
            logger.warn("Failed to cancel reconnect timer", t);
        }
        this.registryInvoker.destroy();
    }

    @Override
    protected void doRegister(URL url) {
        this.registryService.register(url);
    }

    @Override
    protected void doUnregister(URL url) {
        this.registryService.unregister(url);
    }

    @Override
    protected void doSubscribe(URL url, NotifyListener listener) {
        this.registryService.subscribe(url, listener);
    }

    @Override
    protected void doUnsubscribe(URL url, NotifyListener listener) {
        this.registryService.unsubscribe(url, listener);
    }

    @Override
    public List<URL> lookup(URL url) {
        return this.registryService.lookup(url);
    }

}

