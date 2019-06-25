/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractInvoker<T>
implements Invoker<T> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Class<T> type;
    private final URL url;
    private final Map<String, String> attachment;
    private volatile boolean available = true;
    private volatile boolean destroyed = false;

    public AbstractInvoker(Class<T> type, URL url) {
        this(type, url, (Map<String, String>)null);
    }

    public AbstractInvoker(Class<T> type, URL url, String[] keys) {
        this(type, url, AbstractInvoker.convertAttachment(url, keys));
    }

    public AbstractInvoker(Class<T> type, URL url, Map<String, String> attachment) {
        if (type == null) {
            throw new IllegalArgumentException("service type == null");
        }
        if (url == null) {
            throw new IllegalArgumentException("service url == null");
        }
        this.type = type;
        this.url = url;
        this.attachment = attachment == null ? null : Collections.unmodifiableMap(attachment);
    }

    private static Map<String, String> convertAttachment(URL url, String[] keys) {
        if (keys == null || keys.length == 0) {
            return null;
        }
        HashMap<String, String> attachment = new HashMap<String, String>();
        for (String key : keys) {
            String value = url.getParameter(key);
            if (value == null || value.length() <= 0) continue;
            attachment.put(key, value);
        }
        return attachment;
    }

    @Override
    public Class<T> getInterface() {
        return this.type;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public boolean isAvailable() {
        return this.available;
    }

    protected void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public void destroy() {
        if (this.isDestroyed()) {
            return;
        }
        this.destroyed = true;
        this.setAvailable(false);
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }

    public String toString() {
        return this.getInterface() + " -> " + (this.getUrl() == null ? "" : this.getUrl().toString());
    }

    @Override
    public Result invoke(Invocation inv) throws RpcException {
        String applicationName;
        Map<String, String> context;
        if (this.destroyed) {
            throw new RpcException("Rpc invoker for service " + this + " on consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + " is DESTROYED, can not be invoked any more!");
        }
        RpcInvocation invocation = (RpcInvocation)inv;
        invocation.setInvoker(this);
        if (this.attachment != null && this.attachment.size() > 0) {
            invocation.addAttachmentsIfAbsent(this.attachment);
        }
        if ((context = RpcContext.getContext().getAttachments()) != null) {
            invocation.addAttachmentsIfAbsent(context);
        }
        if (this.getUrl().getMethodParameter(invocation.getMethodName(), "async", false)) {
            invocation.setAttachment("async", Boolean.TRUE.toString());
        }
        if (StringUtils.isNotEmpty(applicationName = this.getUrl().getParameter("application"))) {
            invocation.setAttachment("from_app", applicationName);
        }
        RpcUtils.attachInvocationIdIfAsync(this.getUrl(), invocation);
        try {
            return this.doInvoke(invocation);
        }
        catch (InvocationTargetException e) {
            Throwable te = e.getTargetException();
            if (te == null) {
                return new RpcResult(e);
            }
            if (te instanceof RpcException) {
                ((RpcException)te).setCode(3);
            }
            return new RpcResult(te);
        }
        catch (RpcException e) {
            if (e.isBiz()) {
                return new RpcResult(e);
            }
            throw e;
        }
        catch (Throwable e) {
            return new RpcResult(e);
        }
    }

    protected abstract Result doInvoke(Invocation var1) throws Throwable;
}

