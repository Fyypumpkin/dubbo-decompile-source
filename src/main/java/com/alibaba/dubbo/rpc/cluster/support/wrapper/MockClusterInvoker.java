/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.support.wrapper;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.support.MockInvoker;
import java.util.ArrayList;
import java.util.List;

public class MockClusterInvoker<T>
implements Invoker<T> {
    private static final Logger logger = LoggerFactory.getLogger(MockClusterInvoker.class);
    private final Directory<T> directory;
    private final Invoker<T> invoker;

    public MockClusterInvoker(Directory<T> directory, Invoker<T> invoker) {
        this.directory = directory;
        this.invoker = invoker;
    }

    @Override
    public URL getUrl() {
        return this.directory.getUrl();
    }

    @Override
    public boolean isAvailable() {
        return this.directory.isAvailable();
    }

    @Override
    public void destroy() {
        this.invoker.destroy();
    }

    @Override
    public Class<T> getInterface() {
        return this.directory.getInterface();
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        Result result = null;
        String value = this.directory.getUrl().getMethodParameter(invocation.getMethodName(), "mock", Boolean.FALSE.toString()).trim();
        if (value.length() == 0 || value.equalsIgnoreCase("false")) {
            result = this.invoker.invoke(invocation);
        } else if (value.startsWith("force")) {
            if (logger.isWarnEnabled()) {
                logger.info("force-mock: " + invocation.getMethodName() + " force-mock enabled , url : " + this.directory.getUrl());
            }
            result = this.doMockInvoke(invocation, null);
        } else {
            try {
                result = this.invoker.invoke(invocation);
            }
            catch (RpcException e) {
                if (e.isBiz()) {
                    throw e;
                }
                if (logger.isWarnEnabled()) {
                    logger.info("fail-mock: " + invocation.getMethodName() + " fail-mock enabled , url : " + this.directory.getUrl(), e);
                }
                result = this.doMockInvoke(invocation, e);
            }
        }
        return result;
    }

    private Result doMockInvoke(Invocation invocation, RpcException e) {
        Result result = null;
        List<Invoker<T>> mockInvokers = this.selectMockInvoker(invocation);
        Invoker minvoker = mockInvokers == null || mockInvokers.size() == 0 ? new MockInvoker(this.directory.getUrl()) : mockInvokers.get(0);
        try {
            result = minvoker.invoke(invocation);
        }
        catch (RpcException me) {
            if (me.isBiz()) {
                result = new RpcResult(me.getCause());
            }
            throw new RpcException(me.getCode(), this.getMockExceptionMessage(e, me), me.getCause());
        }
        catch (Throwable me) {
            throw new RpcException(this.getMockExceptionMessage(e, me), me.getCause());
        }
        return result;
    }

    private String getMockExceptionMessage(Throwable t, Throwable mt) {
        String msg = "mock error : " + mt.getMessage();
        if (t != null) {
            msg = msg + ", invoke error is :" + StringUtils.toString(t);
        }
        return msg;
    }

    private List<Invoker<T>> selectMockInvoker(Invocation invocation) {
        if (invocation instanceof RpcInvocation) {
            ((RpcInvocation)invocation).setAttachment("invocation.need.mock", Boolean.TRUE.toString());
            List<Invoker<T>> invokers = this.directory.list(invocation);
            return invokers;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List<Invoker<T>> findInvokers(Invocation invocation) {
        if (invocation instanceof RpcInvocation) {
            try {
                List<Invoker<T>> invokers;
                ((RpcInvocation)invocation).setAttachment("invocation.skip.route", Boolean.TRUE.toString());
                List<Invoker<T>> list = invokers = this.directory.list(invocation);
                return list;
            }
            finally {
                ((RpcInvocation)invocation).setAttachment("invocation.skip.route", null);
            }
        }
        return new ArrayList<Invoker<T>>();
    }

    public String toString() {
        return "invoker :" + this.invoker + ",directory: " + this.directory;
    }
}

