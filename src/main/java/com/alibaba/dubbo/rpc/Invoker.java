/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.Node;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

public interface Invoker<T>
extends Node {
    public Class<T> getInterface();

    public Result invoke(Invocation var1) throws RpcException;
}

