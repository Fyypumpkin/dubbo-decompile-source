/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.listener;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.InvokerListener;
import com.alibaba.dubbo.rpc.RpcException;

public abstract class InvokerListenerAdapter
implements InvokerListener {
    @Override
    public void referred(Invoker<?> invoker) throws RpcException {
    }

    @Override
    public void destroyed(Invoker<?> invoker) {
    }
}

