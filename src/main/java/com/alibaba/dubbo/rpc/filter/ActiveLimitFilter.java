/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.filter;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcStatus;

@Activate(group={"consumer"}, value={"actives"})
public class ActiveLimitFilter
implements Filter {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        URL url = invoker.getUrl();
        String methodName = invocation.getMethodName();
        int max = invoker.getUrl().getMethodParameter(methodName, "actives", 0);
        RpcStatus count = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName());
        if (max > 0) {
            long timeout = invoker.getUrl().getMethodParameter(invocation.getMethodName(), "timeout", 0);
            long start = System.currentTimeMillis();
            long remain = timeout;
            int active = count.getActive();
            if (active >= max) {
                RpcStatus rpcStatus = count;
                synchronized (rpcStatus) {
                    while ((active = count.getActive()) >= max) {
                        long elapsed;
                        try {
                            count.wait(remain);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                        if ((remain = timeout - (elapsed = System.currentTimeMillis() - start)) > 0L) continue;
                        throw new RpcException("Waiting concurrent invoke timeout in client-side for service:  " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName() + ", elapsed: " + elapsed + ", timeout: " + timeout + ". concurrent invokes: " + active + ". max concurrent invoke limit: " + max);
                    }
                }
            }
        }
        try {
            long begin = System.currentTimeMillis();
            RpcStatus.beginCount(url, methodName);
            try {
                Result result = invoker.invoke(invocation);
                RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, true);
                Result result2 = result;
                return result2;
            }
            catch (RuntimeException t) {
                RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, false);
                throw t;
            }
        }
        finally {
            if (max > 0) {
                RpcStatus remain = count;
                synchronized (remain) {
                    count.notify();
                }
            }
        }
    }
}

