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

@Activate(group={"provider"}, value={"executes"})
public class ExecuteLimitFilter
implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcStatus count;
        String methodName;
        URL url = invoker.getUrl();
        int max = url.getMethodParameter(methodName = invocation.getMethodName(), "executes", 0);
        if (max > 0 && (count = RpcStatus.getStatus(url, invocation.getMethodName())).getActive() >= max) {
            throw new RpcException("Failed to invoke method " + invocation.getMethodName() + " in provider " + url + ", cause: The service using threads greater than <dubbo:service executes=\"" + max + "\" /> limited.");
        }
        long begin = System.currentTimeMillis();
        boolean isException = false;
        RpcStatus.beginCount(url, methodName);
        try {
            Result result;
            Result result2 = result = invoker.invoke(invocation);
            return result2;
        }
        catch (Throwable t) {
            isException = true;
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            }
            throw new RpcException("unexpected exception when ExecuteLimitFilter", t);
        }
        finally {
            RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, isException);
        }
    }
}

