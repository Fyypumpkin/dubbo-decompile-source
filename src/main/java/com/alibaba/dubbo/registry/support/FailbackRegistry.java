/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.AbstractRegistry;
import com.alibaba.dubbo.registry.support.SkipFailbackWrapperException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class FailbackRegistry
extends AbstractRegistry {
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("DubboRegistryFailedRetryTimer", true));
    private final ScheduledFuture<?> retryFuture;
    private final Set<URL> failedRegistered = new ConcurrentHashSet<URL>();
    private final Set<URL> failedUnregistered = new ConcurrentHashSet<URL>();
    private final ConcurrentMap<URL, Set<NotifyListener>> failedSubscribed = new ConcurrentHashMap<URL, Set<NotifyListener>>();
    private final ConcurrentMap<URL, Set<NotifyListener>> failedUnsubscribed = new ConcurrentHashMap<URL, Set<NotifyListener>>();
    private final ConcurrentMap<URL, Map<NotifyListener, List<URL>>> failedNotified = new ConcurrentHashMap<URL, Map<NotifyListener, List<URL>>>();

    public FailbackRegistry(URL url) {
        super(url);
        int retryPeriod = url.getParameter("retry.period", 5000);
        this.retryFuture = this.retryExecutor.scheduleWithFixedDelay(new Runnable(){

            @Override
            public void run() {
                try {
                    FailbackRegistry.this.retry();
                }
                catch (Throwable t) {
                    FailbackRegistry.this.logger.error("Unexpected error occur at failed retry, cause: " + t.getMessage(), t);
                }
            }
        }, retryPeriod, retryPeriod, TimeUnit.MILLISECONDS);
    }

    public Future<?> getRetryFuture() {
        return this.retryFuture;
    }

    public Set<URL> getFailedRegistered() {
        return this.failedRegistered;
    }

    public Set<URL> getFailedUnregistered() {
        return this.failedUnregistered;
    }

    public Map<URL, Set<NotifyListener>> getFailedSubscribed() {
        return this.failedSubscribed;
    }

    public Map<URL, Set<NotifyListener>> getFailedUnsubscribed() {
        return this.failedUnsubscribed;
    }

    public Map<URL, Map<NotifyListener, List<URL>>> getFailedNotified() {
        return this.failedNotified;
    }

    private void addFailedSubscribed(URL url, NotifyListener listener) {
        Set listeners = (Set)this.failedSubscribed.get(url);
        if (listeners == null) {
            this.failedSubscribed.putIfAbsent(url, new ConcurrentHashSet());
            listeners = (Set)this.failedSubscribed.get(url);
        }
        listeners.add(listener);
    }

    private void removeFailedSubscribed(URL url, NotifyListener listener) {
        Map notified;
        Set listeners = (Set)this.failedSubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        if ((listeners = (Set)this.failedUnsubscribed.get(url)) != null) {
            listeners.remove(listener);
        }
        if ((notified = (Map)this.failedNotified.get(url)) != null) {
            notified.remove(listener);
        }
    }

    @Override
    public void register(URL url) {
        super.register(url);
        this.failedRegistered.remove(url);
        this.failedUnregistered.remove(url);
        try {
            this.doRegister(url);
        }
        catch (Exception e) {
            Throwable t = e;
            boolean check = this.getUrl().getParameter("check", true) && url.getParameter("check", true) && !"consumer".equals(url.getProtocol());
            boolean skipFailback = t instanceof SkipFailbackWrapperException;
            if (check || skipFailback) {
                if (skipFailback) {
                    t = t.getCause();
                }
                throw new IllegalStateException("Failed to register " + url + " to registry " + this.getUrl().getAddress() + ", cause: " + t.getMessage(), t);
            }
            this.logger.error("Failed to register " + url + ", waiting for retry, cause: " + t.getMessage(), t);
            this.failedRegistered.add(url);
        }
    }

    @Override
    public void unregister(URL url) {
        super.unregister(url);
        this.failedRegistered.remove(url);
        this.failedUnregistered.remove(url);
        try {
            this.doUnregister(url);
        }
        catch (Exception e) {
            Throwable t = e;
            boolean check = this.getUrl().getParameter("check", true) && url.getParameter("check", true) && !"consumer".equals(url.getProtocol());
            boolean skipFailback = t instanceof SkipFailbackWrapperException;
            if (check || skipFailback) {
                if (skipFailback) {
                    t = t.getCause();
                }
                throw new IllegalStateException("Failed to unregister " + url + " to registry " + this.getUrl().getAddress() + ", cause: " + t.getMessage(), t);
            }
            this.logger.error("Failed to uregister " + url + ", waiting for retry, cause: " + t.getMessage(), t);
            this.failedUnregistered.add(url);
        }
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        super.subscribe(url, listener);
        this.removeFailedSubscribed(url, listener);
        try {
            this.doSubscribe(url, listener);
        }
        catch (Exception e) {
            Throwable t = e;
            List<URL> urls = this.getCacheUrls(url);
            if (urls != null && urls.size() > 0) {
                this.notify(url, listener, urls);
                this.logger.error("Failed to subscribe " + url + ", Using cached list: " + urls + " from cache file: " + this.getUrl().getParameter("file", new StringBuilder().append(System.getProperty("user.home")).append("/dubbo-registry-").append(url.getHost()).append(".cache").toString()) + ", cause: " + t.getMessage(), t);
            } else {
                boolean check = this.getUrl().getParameter("check", true) && url.getParameter("check", true);
                boolean skipFailback = t instanceof SkipFailbackWrapperException;
                if (check || skipFailback) {
                    if (skipFailback) {
                        t = t.getCause();
                    }
                    throw new IllegalStateException("Failed to subscribe " + url + ", cause: " + t.getMessage(), t);
                }
                this.logger.error("Failed to subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
            }
            this.addFailedSubscribed(url, listener);
        }
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        super.unsubscribe(url, listener);
        this.removeFailedSubscribed(url, listener);
        try {
            this.doUnsubscribe(url, listener);
        }
        catch (Exception e) {
            Throwable t = e;
            boolean check = this.getUrl().getParameter("check", true) && url.getParameter("check", true);
            boolean skipFailback = t instanceof SkipFailbackWrapperException;
            if (check || skipFailback) {
                if (skipFailback) {
                    t = t.getCause();
                }
                throw new IllegalStateException("Failed to unsubscribe " + url + " to registry " + this.getUrl().getAddress() + ", cause: " + t.getMessage(), t);
            }
            this.logger.error("Failed to unsubscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
            Set listeners = (Set)this.failedUnsubscribed.get(url);
            if (listeners == null) {
                this.failedUnsubscribed.putIfAbsent(url, new ConcurrentHashSet());
                listeners = (Set)this.failedUnsubscribed.get(url);
            }
            listeners.add(listener);
        }
    }

    @Override
    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        try {
            this.doNotify(url, listener, urls);
        }
        catch (Exception t) {
            Map listeners = (Map)this.failedNotified.get(url);
            if (listeners == null) {
                this.failedNotified.putIfAbsent(url, new ConcurrentHashMap());
                listeners = (Map)this.failedNotified.get(url);
            }
            listeners.put(listener, urls);
            this.logger.error("Failed to notify for subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
        }
    }

    protected void doNotify(URL url, NotifyListener listener, List<URL> urls) {
        super.notify(url, listener, urls);
    }

    @Override
    protected void recover() throws Exception {
        HashMap<URL, Set<NotifyListener>> recoverSubscribed;
        HashSet<URL> recoverRegistered = new HashSet<URL>(this.getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Recover register url " + recoverRegistered);
            }
            for (URL url : recoverRegistered) {
                this.failedRegistered.add(url);
            }
        }
        if (!(recoverSubscribed = new HashMap<URL, Set<NotifyListener>>(this.getSubscribed())).isEmpty()) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Recover subscribe url " + recoverSubscribed.keySet());
            }
            for (Map.Entry entry : recoverSubscribed.entrySet()) {
                URL url = (URL)entry.getKey();
                for (NotifyListener listener : (Set)entry.getValue()) {
                    this.addFailedSubscribed(url, listener);
                }
            }
        }
    }

    protected void retry() {
        Cloneable failed;
        Object url;
        if (!this.failedRegistered.isEmpty() && (failed = new HashSet<URL>(this.failedRegistered)).size() > 0) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Retry register " + failed);
            }
            try {
                Iterator iterator = failed.iterator();
                while (iterator.hasNext()) {
                    URL url2 = (URL)iterator.next();
                    try {
                        this.doRegister(url2);
                        this.failedRegistered.remove(url2);
                    }
                    catch (Throwable t) {
                        this.logger.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                    }
                }
            }
            catch (Throwable t) {
                this.logger.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(), t);
            }
        }
        if (!this.failedUnregistered.isEmpty() && (failed = new HashSet<URL>(this.failedUnregistered)).size() > 0) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Retry unregister " + failed);
            }
            try {
                Iterator<Object> t = failed.iterator();
                while (t.hasNext()) {
                    URL url2 = (URL)t.next();
                    try {
                        this.doUnregister(url2);
                        this.failedUnregistered.remove(url2);
                    }
                    catch (Throwable t2) {
                        this.logger.warn("Failed to retry unregister  " + failed + ", waiting for again, cause: " + t2.getMessage(), t2);
                    }
                }
            }
            catch (Throwable t) {
                this.logger.warn("Failed to retry unregister  " + failed + ", waiting for again, cause: " + t.getMessage(), t);
            }
        }
        if (!this.failedSubscribed.isEmpty()) {
            failed = new HashMap<URL, Set<NotifyListener>>(this.failedSubscribed);
            for (Map.Entry entry : new HashMap(failed).entrySet()) {
                if (entry.getValue() != null && ((Set)entry.getValue()).size() != 0) continue;
                failed.remove(entry.getKey());
            }
            if (failed.size() > 0) {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Retry subscribe " + failed);
                }
                try {
                    for (Map.Entry entry : failed.entrySet()) {
                        url = (URL)entry.getKey();
                        Set listeners = (Set)entry.getValue();
                        for (NotifyListener listener : listeners) {
                            try {
                                this.doSubscribe((URL)url, listener);
                                listeners.remove(listener);
                            }
                            catch (Throwable t) {
                                this.logger.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                }
                catch (Throwable t) {
                    this.logger.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if (!this.failedUnsubscribed.isEmpty()) {
            failed = new HashMap<URL, Set<NotifyListener>>(this.failedUnsubscribed);
            for (Map.Entry entry : new HashMap(failed).entrySet()) {
                if (entry.getValue() != null && ((Set)entry.getValue()).size() != 0) continue;
                failed.remove(entry.getKey());
            }
            if (failed.size() > 0) {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Retry unsubscribe " + failed);
                }
                try {
                    for (Map.Entry entry : failed.entrySet()) {
                        url = (URL)entry.getKey();
                        Set listeners = (Set)entry.getValue();
                        for (NotifyListener listener : listeners) {
                            try {
                                this.doUnsubscribe((URL)url, listener);
                                listeners.remove(listener);
                            }
                            catch (Throwable t) {
                                this.logger.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                }
                catch (Throwable t) {
                    this.logger.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if (!this.failedNotified.isEmpty()) {
            failed = new HashMap<URL, Map<NotifyListener, List<URL>>>(this.failedNotified);
            for (Map.Entry entry : new HashMap(failed).entrySet()) {
                if (entry.getValue() != null && ((Map)entry.getValue()).size() != 0) continue;
                failed.remove(entry.getKey());
            }
            if (failed.size() > 0) {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Retry notify " + failed);
                }
                try {
                    for (Map values : failed.values()) {
                        for (Map.Entry entry : values.entrySet()) {
                            try {
                                NotifyListener listener = (NotifyListener)entry.getKey();
                                List urls = (List)entry.getValue();
                                listener.notify(urls);
                                values.remove(listener);
                            }
                            catch (Throwable t) {
                                this.logger.warn("Failed to retry notify " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                }
                catch (Throwable t) {
                    this.logger.warn("Failed to retry notify " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            this.retryFuture.cancel(true);
        }
        catch (Throwable t) {
            this.logger.warn(t.getMessage(), t);
        }
    }

    protected abstract void doRegister(URL var1);

    protected abstract void doUnregister(URL var1);

    protected abstract void doSubscribe(URL var1, NotifyListener var2);

    protected abstract void doUnsubscribe(URL var1, NotifyListener var2);

}

