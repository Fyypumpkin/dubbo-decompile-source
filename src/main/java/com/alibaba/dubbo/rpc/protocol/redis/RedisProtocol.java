/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.commons.pool.impl.GenericObjectPool
 *  org.apache.commons.pool.impl.GenericObjectPool$Config
 *  redis.clients.jedis.Jedis
 *  redis.clients.jedis.JedisPool
 *  redis.clients.jedis.exceptions.JedisConnectionException
 *  redis.clients.jedis.exceptions.JedisDataException
 */
package com.alibaba.dubbo.rpc.protocol.redis;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import com.alibaba.dubbo.rpc.protocol.AbstractProtocol;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.apache.commons.pool.impl.GenericObjectPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

public class RedisProtocol
extends AbstractProtocol {
    public static final int DEFAULT_PORT = 6379;

    @Override
    public int getDefaultPort() {
        return 6379;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        throw new UnsupportedOperationException("Unsupported export redis service. url: " + invoker.getUrl());
    }

    private Serialization getSerialization(URL url) {
        return ExtensionLoader.getExtensionLoader(Serialization.class).getExtension(url.getParameter("serialization", "java"));
    }

    @Override
    public <T> Invoker<T> refer(final Class<T> type, final URL url) throws RpcException {
        try {
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
            if (url.getParameter("max.wait", 0) > 0) {
                config.maxWait = url.getParameter("max.wait", 0);
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
            final JedisPool jedisPool = new JedisPool(config, url.getHost(), url.getPort(6379), url.getParameter("timeout", 1000));
            final int expiry = url.getParameter("expiry", 0);
            final String get = url.getParameter("get", "get");
            final String set = url.getParameter("set", Map.class.equals(type) ? "put" : "set");
            final String delete = url.getParameter("delete", Map.class.equals(type) ? "remove" : "delete");
            return new AbstractInvoker<T>(type, url){

                @Override
                protected Result doInvoke(Invocation invocation) throws Throwable {
                    Jedis resource = null;
                    try {
                        resource = (Jedis)jedisPool.getResource();
                        if (get.equals(invocation.getMethodName())) {
                            if (invocation.getArguments().length != 1) {
                                throw new IllegalArgumentException("The redis get method arguments mismatch, must only one arguments. interface: " + type.getName() + ", method: " + invocation.getMethodName() + ", url: " + url);
                            }
                            byte[] value = resource.get(String.valueOf(invocation.getArguments()[0]).getBytes());
                            if (value == null) {
                                RpcResult rpcResult = new RpcResult();
                                return rpcResult;
                            }
                            ObjectInput oin = RedisProtocol.this.getSerialization(url).deserialize(url, new ByteArrayInputStream(value));
                            RpcResult t = new RpcResult(oin.readObject());
                            return t;
                        }
                        if (set.equals(invocation.getMethodName())) {
                            if (invocation.getArguments().length != 2) {
                                throw new IllegalArgumentException("The redis set method arguments mismatch, must be two arguments. interface: " + type.getName() + ", method: " + invocation.getMethodName() + ", url: " + url);
                            }
                            byte[] key = String.valueOf(invocation.getArguments()[0]).getBytes();
                            ByteArrayOutputStream output = new ByteArrayOutputStream();
                            ObjectOutput value = RedisProtocol.this.getSerialization(url).serialize(url, output);
                            value.writeObject(invocation.getArguments()[1]);
                            resource.set(key, output.toByteArray());
                            if (expiry > 1000) {
                                resource.expire(key, expiry / 1000);
                            }
                            RpcResult t = new RpcResult();
                            return t;
                        }
                        if (delete.equals(invocation.getMethodName())) {
                            if (invocation.getArguments().length != 1) {
                                throw new IllegalArgumentException("The redis delete method arguments mismatch, must only one arguments. interface: " + type.getName() + ", method: " + invocation.getMethodName() + ", url: " + url);
                            }
                            resource.del((byte[][])new byte[][]{String.valueOf(invocation.getArguments()[0]).getBytes()});
                            RpcResult key = new RpcResult();
                            return key;
                        }
                        try {
                            throw new UnsupportedOperationException("Unsupported method " + invocation.getMethodName() + " in redis service.");
                        }
                        catch (Throwable t) {
                            RpcException re = new RpcException("Failed to invoke memecached service method. interface: " + type.getName() + ", method: " + invocation.getMethodName() + ", url: " + url + ", cause: " + t.getMessage(), t);
                            if (t instanceof TimeoutException || t instanceof SocketTimeoutException) {
                                re.setCode(2);
                            } else if (t instanceof JedisConnectionException || t instanceof IOException) {
                                re.setCode(1);
                            } else if (t instanceof JedisDataException) {
                                re.setCode(5);
                            }
                            throw re;
                        }
                    }
                    finally {
                        if (resource != null) {
                            try {
                                jedisPool.returnResource((Object)resource);
                            }
                            catch (Throwable t) {
                                this.logger.warn("returnResource error: " + t.getMessage(), t);
                            }
                        }
                    }
                }

                @Override
                public void destroy() {
                    super.destroy();
                    try {
                        jedisPool.destroy();
                    }
                    catch (Throwable e) {
                        this.logger.warn(e.getMessage(), e);
                    }
                }
            };
        }
        catch (Throwable t) {
            throw new RpcException("Failed to refer memecached service. interface: " + type.getName() + ", url: " + url + ", cause: " + t.getMessage(), t);
        }
    }

}

