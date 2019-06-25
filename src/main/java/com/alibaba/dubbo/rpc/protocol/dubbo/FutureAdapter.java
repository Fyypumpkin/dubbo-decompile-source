/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureAdapter<V>
implements Future<V> {
    private final ResponseFuture future;

    public FutureAdapter(ResponseFuture future) {
        this.future = future;
    }

    public ResponseFuture getFuture() {
        return this.future;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return this.future.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        try {
            return (V)((Result)this.future.get()).recreate();
        }
        catch (RemotingException e) {
            throw new ExecutionException(e.getMessage(), e);
        }
        catch (Throwable e) {
            throw new RpcException(e);
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        int timeoutInMillis = (int)unit.convert(timeout, TimeUnit.MILLISECONDS);
        try {
            return (V)((Result)this.future.get(timeoutInMillis)).recreate();
        }
        catch (com.alibaba.dubbo.remoting.TimeoutException e) {
            throw new TimeoutException(StringUtils.toString(e));
        }
        catch (RemotingException e) {
            throw new ExecutionException(e.getMessage(), e);
        }
        catch (Throwable e) {
            throw new RpcException(e);
        }
    }
}

