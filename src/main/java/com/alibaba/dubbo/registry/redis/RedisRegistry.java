/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.commons.pool.impl.GenericObjectPool
 *  org.apache.commons.pool.impl.GenericObjectPool$Config
 *  redis.clients.jedis.Jedis
 *  redis.clients.jedis.JedisPool
 *  redis.clients.jedis.JedisPubSub
 */
package com.alibaba.dubbo.registry.redis;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import com.alibaba.dubbo.rpc.RpcException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.pool.impl.GenericObjectPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

public class RedisRegistry
extends FailbackRegistry {
    private static final Logger logger = LoggerFactory.getLogger(RedisRegistry.class);
    private static final int DEFAULT_REDIS_PORT = 6379;
    private static final String DEFAULT_ROOT = "dubbo";
    private final ScheduledExecutorService expireExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("DubboRegistryExpireTimer", true));
    private final ScheduledFuture<?> expireFuture;
    private final String root;
    private final Map<String, JedisPool> jedisPools = new ConcurrentHashMap<String, JedisPool>();
    private final ConcurrentMap<String, Notifier> notifiers = new ConcurrentHashMap<String, Notifier>();
    private final int reconnectPeriod;
    private final int expirePeriod;
    private volatile boolean admin = false;
    private boolean replicate;

    public RedisRegistry(URL url) {
        String cluster;
        super(url);
        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null");
        }
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        config.testOnBorrow = url.getParameter("test.on.borrow", true);
        config.testOnReturn = url.getParameter("test.on.return", false);
        config.testWhileIdle = url.getParameter("test.while.idle", false);
        if (url.getParameter("max.idle", 0) > 0) {
            config.maxIdle = url.getParameter("max.idle", 0);
        }
        if (url.getParameter("min.idle", 0) > 0) {
            config.minIdle = url.getParameter("min.idle", 0);
        }
        if (url.getParameter("max.active", 0) > 0) {
            config.maxActive = url.getParameter("max.active", 0);
        }
        if (url.getParameter("max.wait", url.getParameter("timeout", 0)) > 0) {
            config.maxWait = url.getParameter("max.wait", url.getParameter("timeout", 0));
        }
        if (url.getParameter("num.tests.per.eviction.run", 0) > 0) {
            config.numTestsPerEvictionRun = url.getParameter("num.tests.per.eviction.run", 0);
        }
        if (url.getParameter("time.between.eviction.runs.millis", 0) > 0) {
            config.timeBetweenEvictionRunsMillis = url.getParameter("time.between.eviction.runs.millis", 0);
        }
        if (url.getParameter("min.evictable.idle.time.millis", 0) > 0) {
            config.minEvictableIdleTimeMillis = url.getParameter("min.evictable.idle.time.millis", 0);
        }
        if (!"failover".equals(cluster = url.getParameter("cluster", "failover")) && !"replicate".equals(cluster)) {
            throw new IllegalArgumentException("Unsupported redis cluster: " + cluster + ". The redis cluster only supported failover or replicate.");
        }
        this.replicate = "replicate".equals(cluster);
        ArrayList<String> addresses = new ArrayList<String>();
        addresses.add(url.getAddress());
        String[] backups = url.getParameter("backup", new String[0]);
        if (backups != null && backups.length > 0) {
            addresses.addAll(Arrays.asList(backups));
        }
        for (String address : addresses) {
            int port;
            String host;
            int i = address.indexOf(58);
            if (i > 0) {
                host = address.substring(0, i);
                port = Integer.parseInt(address.substring(i + 1));
            } else {
                host = address;
                port = 6379;
            }
            this.jedisPools.put(address, new JedisPool(config, host, port, url.getParameter("timeout", 1000)));
        }
        this.reconnectPeriod = url.getParameter("reconnect.period", 3000);
        String group = url.getParameter("group", DEFAULT_ROOT);
        if (!group.startsWith("/")) {
            group = "/" + group;
        }
        if (!group.endsWith("/")) {
            group = group + "/";
        }
        this.root = group;
        this.expirePeriod = url.getParameter("session", 60000);
        this.expireFuture = this.expireExecutor.scheduleWithFixedDelay(new Runnable(){

            @Override
            public void run() {
                try {
                    RedisRegistry.this.deferExpired();
                }
                catch (Throwable t) {
                    logger.error("Unexpected exception occur at defer expire time, cause: " + t.getMessage(), t);
                }
            }
        }, this.expirePeriod / 2, this.expirePeriod / 2, TimeUnit.MILLISECONDS);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void deferExpired() {
        for (Map.Entry<String, JedisPool> entry : this.jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = (Jedis)jedisPool.getResource();
                try {
                    for (URL url : new HashSet<URL>(this.getRegistered())) {
                        String key;
                        if (!url.getParameter("dynamic", true) || jedis.hset(key = this.toCategoryPath(url), url.toFullString(), String.valueOf(System.currentTimeMillis() + (long)this.expirePeriod)) != 1L) continue;
                        jedis.publish(key, "register");
                    }
                    if (this.admin) {
                        this.clean(jedis);
                    }
                    if (this.replicate) continue;
                    break;
                }
                finally {
                    jedisPool.returnResource((Object)jedis);
                }
            }
            catch (Throwable t) {
                logger.warn("Failed to write provider heartbeat to redis registry. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
            }
        }
    }

    private void clean(Jedis jedis) {
        Set keys = jedis.keys(this.root + "*");
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                Map values = jedis.hgetAll(key);
                if (values == null || values.size() <= 0) continue;
                boolean delete = false;
                long now = System.currentTimeMillis();
                for (Map.Entry entry : values.entrySet()) {
                    long expire;
                    URL url = URL.valueOf((String)entry.getKey());
                    if (!url.getParameter("dynamic", true) || (expire = Long.parseLong((String)entry.getValue())) >= now) continue;
                    jedis.hdel(key, new String[]{(String)entry.getKey()});
                    delete = true;
                    if (!logger.isWarnEnabled()) continue;
                    logger.warn("Delete expired key: " + key + " -> value: " + (String)entry.getKey() + ", expire: " + new Date(expire) + ", now: " + new Date(now));
                }
                if (!delete) continue;
                jedis.publish(key, "unregister");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean isAvailable() {
        Iterator<JedisPool> iterator = this.jedisPools.values().iterator();
        while (iterator.hasNext()) {
            JedisPool jedisPool = iterator.next();
            try {
                Jedis jedis222 = (Jedis)jedisPool.getResource();
                try {
                    if (!jedis222.isConnected()) continue;
                    boolean bl = true;
                    return bl;
                }
                finally {
                    jedisPool.returnResource((Object)jedis222);
                }
            }
            catch (Throwable jedis222) {}
        }
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            this.expireFuture.cancel(true);
        }
        catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
        try {
            for (Notifier notifier : this.notifiers.values()) {
                notifier.shutdown();
            }
        }
        catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
        for (Map.Entry entry : this.jedisPools.entrySet()) {
            JedisPool jedisPool = (JedisPool)entry.getValue();
            try {
                jedisPool.destroy();
            }
            catch (Throwable t) {
                logger.warn("Failed to destroy the redis registry client. registry: " + (String)entry.getKey() + ", cause: " + t.getMessage(), t);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void doRegister(URL url) {
        String key = this.toCategoryPath(url);
        String value = url.toFullString();
        String expire = String.valueOf(System.currentTimeMillis() + (long)this.expirePeriod);
        boolean success = false;
        Throwable exception = null;
        for (Map.Entry<String, JedisPool> entry : this.jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = (Jedis)jedisPool.getResource();
                try {
                    jedis.hset(key, value, expire);
                    jedis.publish(key, "register");
                    success = true;
                    if (this.replicate) continue;
                    break;
                }
                finally {
                    jedisPool.returnResource((Object)jedis);
                }
            }
            catch (Throwable t) {
                exception = new RpcException("Failed to register service to redis registry. registry: " + entry.getKey() + ", service: " + url + ", cause: " + t.getMessage(), t);
            }
        }
        if (exception != null) {
            if (success) {
                logger.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void doUnregister(URL url) {
        String key = this.toCategoryPath(url);
        String value = url.toFullString();
        Throwable exception = null;
        boolean success = false;
        for (Map.Entry<String, JedisPool> entry : this.jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = (Jedis)jedisPool.getResource();
                try {
                    jedis.hdel(key, new String[]{value});
                    jedis.publish(key, "unregister");
                    success = true;
                    if (this.replicate) continue;
                    break;
                }
                finally {
                    jedisPool.returnResource((Object)jedis);
                }
            }
            catch (Throwable t) {
                exception = new RpcException("Failed to unregister service to redis registry. registry: " + entry.getKey() + ", service: " + url + ", cause: " + t.getMessage(), t);
            }
        }
        if (exception != null) {
            if (success) {
                logger.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void doSubscribe(URL url, NotifyListener listener) {
        String service = this.toServicePath(url);
        Notifier notifier = (Notifier)this.notifiers.get(service);
        if (notifier == null) {
            Notifier newNotifier = new Notifier(service);
            this.notifiers.putIfAbsent(service, newNotifier);
            notifier = (Notifier)this.notifiers.get(service);
            if (notifier == newNotifier) {
                notifier.start();
            }
        }
        boolean success = false;
        Throwable exception = null;
        for (Map.Entry<String, JedisPool> entry : this.jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                Jedis jedis = (Jedis)jedisPool.getResource();
                try {
                    if (service.endsWith("*")) {
                        this.admin = true;
                        Set keys = jedis.keys(service);
                        if (keys != null && keys.size() > 0) {
                            HashMap<String, HashSet<String>> serviceKeys = new HashMap<String, HashSet<String>>();
                            for (String key : keys) {
                                String serviceKey = this.toServicePath(key);
                                HashSet<String> sk = (HashSet<String>)serviceKeys.get(serviceKey);
                                if (sk == null) {
                                    sk = new HashSet<String>();
                                    serviceKeys.put(serviceKey, sk);
                                }
                                sk.add(key);
                            }
                            for (Set sk : serviceKeys.values()) {
                                this.doNotify(jedis, sk, url, Arrays.asList(listener));
                            }
                        }
                    } else {
                        this.doNotify(jedis, jedis.keys(service + "/" + "*"), url, Arrays.asList(listener));
                    }
                    success = true;
                    break;
                }
                finally {
                    jedisPool.returnResource((Object)jedis);
                }
            }
            catch (Throwable t) {
                exception = new RpcException("Failed to subscribe service from redis registry. registry: " + entry.getKey() + ", service: " + url + ", cause: " + t.getMessage(), t);
            }
        }
        if (exception != null) {
            if (success) {
                logger.warn(exception.getMessage(), exception);
            } else {
                throw exception;
            }
        }
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
    }

    private void doNotify(Jedis jedis, String key) {
        for (Map.Entry<URL, Set<NotifyListener>> entry : new HashMap<URL, Set<NotifyListener>>(this.getSubscribed()).entrySet()) {
            this.doNotify(jedis, Arrays.asList(key), entry.getKey(), new HashSet<NotifyListener>((Collection)entry.getValue()));
        }
    }

    private void doNotify(Jedis jedis, Collection<String> keys, URL url, Collection<NotifyListener> listeners) {
        if (keys == null || keys.size() == 0 || listeners == null || listeners.size() == 0) {
            return;
        }
        long now = System.currentTimeMillis();
        ArrayList<URL> result = new ArrayList<URL>();
        List<String> categories = Arrays.asList(url.getParameter("category", new String[0]));
        String consumerService = url.getServiceInterface();
        for (String key : keys) {
            String prvoiderService;
            if (!"*".equals(consumerService) && !(prvoiderService = this.toServiceName(key)).equals(consumerService)) continue;
            String category = this.toCategoryName(key);
            if (!categories.contains("*") && !categories.contains(category)) continue;
            ArrayList<URL> urls = new ArrayList<URL>();
            Map values = jedis.hgetAll(key);
            if (values != null && values.size() > 0) {
                for (Map.Entry entry : values.entrySet()) {
                    URL u = URL.valueOf((String)entry.getKey());
                    if (u.getParameter("dynamic", true) && Long.parseLong((String)entry.getValue()) < now || !UrlUtils.isMatch(url, u)) continue;
                    urls.add(u);
                }
            }
            if (urls.isEmpty()) {
                urls.add(url.setProtocol("empty").setAddress("0.0.0.0").setPath(this.toServiceName(key)).addParameter("category", category));
            }
            result.addAll(urls);
            if (!logger.isInfoEnabled()) continue;
            logger.info("redis notify: " + key + " = " + urls);
        }
        if (result == null || result.size() == 0) {
            return;
        }
        for (NotifyListener listener : listeners) {
            this.notify(url, listener, result);
        }
    }

    private String toServiceName(String categoryPath) {
        String servicePath = this.toServicePath(categoryPath);
        return servicePath.startsWith(this.root) ? servicePath.substring(this.root.length()) : servicePath;
    }

    private String toCategoryName(String categoryPath) {
        int i = categoryPath.lastIndexOf("/");
        return i > 0 ? categoryPath.substring(i + 1) : categoryPath;
    }

    private String toServicePath(String categoryPath) {
        int i = categoryPath.startsWith(this.root) ? categoryPath.indexOf("/", this.root.length()) : categoryPath.indexOf("/");
        return i > 0 ? categoryPath.substring(0, i) : categoryPath;
    }

    private String toServicePath(URL url) {
        return this.root + url.getServiceInterface();
    }

    private String toCategoryPath(URL url) {
        return this.toServicePath(url) + "/" + url.getParameter("category", "providers");
    }

    private class Notifier
    extends Thread {
        private final String service;
        private volatile Jedis jedis;
        private volatile boolean first = true;
        private volatile boolean running = true;
        private final AtomicInteger connectSkip = new AtomicInteger();
        private final AtomicInteger connectSkiped = new AtomicInteger();
        private final Random random = new Random();
        private volatile int connectRandom;

        private void resetSkip() {
            this.connectSkip.set(0);
            this.connectSkiped.set(0);
            this.connectRandom = 0;
        }

        private boolean isSkip() {
            int skip = this.connectSkip.get();
            if (skip >= 10) {
                if (this.connectRandom == 0) {
                    this.connectRandom = this.random.nextInt(10);
                }
                skip = 10 + this.connectRandom;
            }
            if (this.connectSkiped.getAndIncrement() < skip) {
                return true;
            }
            this.connectSkip.incrementAndGet();
            this.connectSkiped.set(0);
            this.connectRandom = 0;
            return false;
        }

        public Notifier(String service) {
            super.setDaemon(true);
            super.setName("DubboRedisSubscribe");
            this.service = service;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            block9 : while (this.running) {
                try {
                    if (this.isSkip()) continue;
                    try {
                        for (Map.Entry entry : RedisRegistry.this.jedisPools.entrySet()) {
                            JedisPool jedisPool = (JedisPool)entry.getValue();
                            try {
                                this.jedis = (Jedis)jedisPool.getResource();
                                try {
                                    if (this.service.endsWith("*")) {
                                        if (!this.first) {
                                            this.first = false;
                                            Set keys = this.jedis.keys(this.service);
                                            if (keys != null && keys.size() > 0) {
                                                for (String s : keys) {
                                                    RedisRegistry.this.doNotify(this.jedis, s);
                                                }
                                            }
                                            this.resetSkip();
                                        }
                                        this.jedis.psubscribe((JedisPubSub)new NotifySub(jedisPool), new String[]{this.service});
                                        continue block9;
                                    }
                                    if (!this.first) {
                                        this.first = false;
                                        RedisRegistry.this.doNotify(this.jedis, this.service);
                                        this.resetSkip();
                                    }
                                    this.jedis.psubscribe((JedisPubSub)new NotifySub(jedisPool), new String[]{this.service + "/" + "*"});
                                    continue block9;
                                }
                                finally {
                                    jedisPool.returnBrokenResource((Object)this.jedis);
                                    continue block9;
                                }
                            }
                            catch (Throwable t) {
                                logger.warn("Failed to subscribe service from redis registry. registry: " + (String)entry.getKey() + ", cause: " + t.getMessage(), t);
                                Notifier.sleep(RedisRegistry.this.reconnectPeriod);
                            }
                        }
                    }
                    catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                        Notifier.sleep(RedisRegistry.this.reconnectPeriod);
                    }
                }
                catch (Throwable t) {
                    logger.error(t.getMessage(), t);
                }
            }
        }

        public void shutdown() {
            try {
                this.running = false;
                this.jedis.disconnect();
            }
            catch (Throwable t) {
                logger.warn(t.getMessage(), t);
            }
        }
    }

    private class NotifySub
    extends JedisPubSub {
        private final JedisPool jedisPool;

        public NotifySub(JedisPool jedisPool) {
            this.jedisPool = jedisPool;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void onMessage(String key, String msg) {
            if (logger.isInfoEnabled()) {
                logger.info("redis event: " + key + " = " + msg);
            }
            if (msg.equals("register") || msg.equals("unregister")) {
                try {
                    Jedis jedis = (Jedis)this.jedisPool.getResource();
                    try {
                        RedisRegistry.this.doNotify(jedis, key);
                    }
                    finally {
                        this.jedisPool.returnResource((Object)jedis);
                    }
                }
                catch (Throwable t) {
                    logger.error(t.getMessage(), t);
                }
            }
        }

        public void onPMessage(String pattern, String key, String msg) {
            this.onMessage(key, msg);
        }

        public void onSubscribe(String key, int num) {
        }

        public void onPSubscribe(String pattern, int num) {
        }

        public void onUnsubscribe(String key, int num) {
        }

        public void onPUnsubscribe(String pattern, int num) {
        }
    }

}

