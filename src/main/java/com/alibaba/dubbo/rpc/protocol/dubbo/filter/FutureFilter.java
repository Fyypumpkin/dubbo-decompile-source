/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo.filter;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.StaticContext;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

@Activate(group={"consumer"})
public class FutureFilter
implements Filter {
    protected static final Logger logger = LoggerFactory.getLogger(FutureFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);
        this.fireInvokeCallback(invoker, invocation);
        Result result = invoker.invoke(invocation);
        if (isAsync) {
            this.asyncCallback(invoker, invocation);
        } else {
            this.syncCallback(invoker, invocation, result);
        }
        return result;
    }

    private void syncCallback(Invoker<?> invoker, Invocation invocation, Result result) {
        if (result.hasException()) {
            this.fireThrowCallback(invoker, invocation, result.getException());
        } else {
            this.fireReturnCallback(invoker, invocation, result.getValue());
        }
    }

    private void asyncCallback(final Invoker<?> invoker, final Invocation invocation) {
        Future f = RpcContext.getContext().getFuture();
        if (f instanceof FutureAdapter) {
            ResponseFuture future = ((FutureAdapter)f).getFuture();
            future.setCallback(new ResponseCallback(){

                @Override
                public void done(Object rpcResult) {
                    if (rpcResult == null) {
                        FutureFilter.logger.error(new IllegalStateException("invalid result value : null, expected " + Result.class.getName()));
                        return;
                    }
                    if (!(rpcResult instanceof Result)) {
                        FutureFilter.logger.error(new IllegalStateException("invalid result type :" + rpcResult.getClass() + ", expected " + Result.class.getName()));
                        return;
                    }
                    Result result = (Result)rpcResult;
                    if (result.hasException()) {
                        FutureFilter.this.fireThrowCallback(invoker, invocation, result.getException());
                    } else {
                        FutureFilter.this.fireReturnCallback(invoker, invocation, result.getValue());
                    }
                }

                @Override
                public void caught(Throwable exception) {
                    FutureFilter.this.fireThrowCallback(invoker, invocation, exception);
                }
            });
        }
    }

    private void fireInvokeCallback(Invoker<?> invoker, Invocation invocation) {
        Method onInvokeMethod = (Method)StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), "oninvoke.method"));
        Object onInvokeInst = StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), "oninvoke.instance"));
        if (onInvokeMethod == null && onInvokeInst == null) {
            return;
        }
        if (onInvokeMethod == null || onInvokeInst == null) {
            throw new IllegalStateException("service:" + invoker.getUrl().getServiceKey() + " has a onreturn callback config , but no such " + (onInvokeMethod == null ? "method" : "instance") + " found. url:" + invoker.getUrl());
        }
        if (onInvokeMethod != null && !onInvokeMethod.isAccessible()) {
            onInvokeMethod.setAccessible(true);
        }
        Object[] params = invocation.getArguments();
        try {
            onInvokeMethod.invoke(onInvokeInst, params);
        }
        catch (InvocationTargetException e) {
            this.fireThrowCallback(invoker, invocation, e.getTargetException());
        }
        catch (Throwable e) {
            this.fireThrowCallback(invoker, invocation, e);
        }
    }

    private void fireReturnCallback(Invoker<?> invoker, Invocation invocation, Object result) {
        Object[] params;
        Method onReturnMethod = (Method)StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), "onreturn.method"));
        Object onReturnInst = StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), "onreturn.instance"));
        if (onReturnMethod == null && onReturnInst == null) {
            return;
        }
        if (onReturnMethod == null || onReturnInst == null) {
            throw new IllegalStateException("service:" + invoker.getUrl().getServiceKey() + " has a onreturn callback config , but no such " + (onReturnMethod == null ? "method" : "instance") + " found. url:" + invoker.getUrl());
        }
        if (onReturnMethod != null && !onReturnMethod.isAccessible()) {
            onReturnMethod.setAccessible(true);
        }
        Object[] args = invocation.getArguments();
        Class<?>[] rParaTypes = onReturnMethod.getParameterTypes();
        if (rParaTypes.length > 1) {
            if (rParaTypes.length == 2 && rParaTypes[1].isAssignableFrom(Object[].class)) {
                params = new Object[]{result, args};
            } else {
                params = new Object[args.length + 1];
                params[0] = result;
                System.arraycopy(args, 0, params, 1, args.length);
            }
        } else {
            params = new Object[]{result};
        }
        try {
            onReturnMethod.invoke(onReturnInst, params);
        }
        catch (InvocationTargetException e) {
            this.fireThrowCallback(invoker, invocation, e.getTargetException());
        }
        catch (Throwable e) {
            this.fireThrowCallback(invoker, invocation, e);
        }
    }

    private void fireThrowCallback(Invoker<?> invoker, Invocation invocation, Throwable exception) {
        Class<?>[] rParaTypes;
        Method onthrowMethod = (Method)StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), "onthrow.method"));
        Object onthrowInst = StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), "onthrow.instance"));
        if (onthrowMethod == null && onthrowInst == null) {
            return;
        }
        if (onthrowMethod == null || onthrowInst == null) {
            throw new IllegalStateException("service:" + invoker.getUrl().getServiceKey() + " has a onthrow callback config , but no such " + (onthrowMethod == null ? "method" : "instance") + " found. url:" + invoker.getUrl());
        }
        if (onthrowMethod != null && !onthrowMethod.isAccessible()) {
            onthrowMethod.setAccessible(true);
        }
        if ((rParaTypes = onthrowMethod.getParameterTypes())[0].isAssignableFrom(exception.getClass())) {
            try {
                Object[] params;
                Object[] args = invocation.getArguments();
                if (rParaTypes.length > 1) {
                    if (rParaTypes.length == 2 && rParaTypes[1].isAssignableFrom(Object[].class)) {
                        params = new Object[]{exception, args};
                    } else {
                        params = new Object[args.length + 1];
                        params[0] = exception;
                        System.arraycopy(args, 0, params, 1, args.length);
                    }
                } else {
                    params = new Object[]{exception};
                }
                onthrowMethod.invoke(onthrowInst, params);
            }
            catch (Throwable e) {
                logger.error(invocation.getMethodName() + ".call back method invoke error . callback method :" + onthrowMethod + ", url:" + invoker.getUrl(), e);
            }
        } else {
            logger.error(invocation.getMethodName() + ".call back method invoke error . callback method :" + onthrowMethod + ", url:" + invoker.getUrl(), exception);
        }
    }

}

