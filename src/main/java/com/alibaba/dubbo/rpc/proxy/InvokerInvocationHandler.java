/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.proxy;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcInvocation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class InvokerInvocationHandler
implements InvocationHandler {
    private final Invoker<?> invoker;

    public InvokerInvocationHandler(Invoker<?> handler) {
        this.invoker = handler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this.invoker, args);
        }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return this.invoker.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return this.invoker.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return this.invoker.equals(args[0]);
        }
        return this.invoker.invoke(new RpcInvocation(method, args)).recreate();
    }
}

