/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster;

import com.alibaba.dubbo.common.Node;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import java.util.List;

public interface Directory<T>
extends Node {
    public Class<T> getInterface();

    public List<Invoker<T>> list(Invocation var1) throws RpcException;
}

