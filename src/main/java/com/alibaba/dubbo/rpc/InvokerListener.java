/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;

@SPI
public interface InvokerListener {
    public void referred(Invoker<?> var1) throws RpcException;

    public void destroyed(Invoker<?> var1);
}

