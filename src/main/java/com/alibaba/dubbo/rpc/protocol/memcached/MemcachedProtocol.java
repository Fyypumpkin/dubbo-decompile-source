/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  net.rubyeye.xmemcached.MemcachedClient
 *  net.rubyeye.xmemcached.XMemcachedClientBuilder
 *  net.rubyeye.xmemcached.exception.MemcachedException
 *  net.rubyeye.xmemcached.utils.AddrUtil
 */
package com.alibaba.dubbo.rpc.protocol.memcached;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import com.alibaba.dubbo.rpc.protocol.AbstractProtocol;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

public class MemcachedProtocol
extends AbstractProtocol {
    public static final int DEFAULT_PORT = 11211;

    @Override
    public int getDefaultPort() {
        return 11211;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        throw new UnsupportedOperationException("Unsupported export memcached service. url: " + invoker.getUrl());
    }

    @Override
    public <T> Invoker<T> refer(final Class<T> type, final URL url) throws RpcException {
        try {
            String address = url.getAddress();
            String backup = url.getParameter("backup");
            if (backup != null && backup.length() > 0) {
                address = address + "," + backup;
            }
            XMemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses((String)address));
            final MemcachedClient memcachedClient = builder.build();
            final int expiry = url.getParameter("expiry", 0);
            final String get = url.getParameter("get", "get");
            final String set = url.getParameter("set", Map.class.equals(type) ? "put" : "set");
            final String delete = url.getParameter("delete", Map.class.equals(type) ? "remove" : "delete");
            return new AbstractInvoker<T>(type, url){

                @Override
                protected Result doInvoke(Invocation invocation) throws Throwable {
                    try {
                        if (get.equals(invocation.getMethodName())) {
                            if (invocation.getArguments().length != 1) {
                                throw new IllegalArgumentException("The memcached get method arguments mismatch, must only one arguments. interface: " + type.getName() + ", method: " + invocation.getMethodName() + ", url: " + url);
                            }
                            return new RpcResult((Throwable)memcachedClient.get(String.valueOf(invocation.getArguments()[0])));
                        }
                        if (set.equals(invocation.getMethodName())) {
                            if (invocation.getArguments().length != 2) {
                                throw new IllegalArgumentException("The memcached set method arguments mismatch, must be two arguments. interface: " + type.getName() + ", method: " + invocation.getMethodName() + ", url: " + url);
                            }
                            memcachedClient.set(String.valueOf(invocation.getArguments()[0]), expiry, invocation.getArguments()[1]);
                            return new RpcResult();
                        }
                        if (delete.equals(invocation.getMethodName())) {
                            if (invocation.getArguments().length != 1) {
                                throw new IllegalArgumentException("The memcached delete method arguments mismatch, must only one arguments. interface: " + type.getName() + ", method: " + invocation.getMethodName() + ", url: " + url);
                            }
                            memcachedClient.delete(String.valueOf(invocation.getArguments()[0]));
                            return new RpcResult();
                        }
                        throw new UnsupportedOperationException("Unsupported method " + invocation.getMethodName() + " in memcached service.");
                    }
                    catch (Throwable t) {
                        RpcException re = new RpcException("Failed to invoke memecached service method. interface: " + type.getName() + ", method: " + invocation.getMethodName() + ", url: " + url + ", cause: " + t.getMessage(), t);
                        if (t instanceof TimeoutException || t instanceof SocketTimeoutException) {
                            re.setCode(2);
                        } else if (t instanceof MemcachedException || t instanceof IOException) {
                            re.setCode(1);
                        }
                        throw re;
                    }
                }

                @Override
                public void destroy() {
                    super.destroy();
                    try {
                        memcachedClient.shutdown();
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

