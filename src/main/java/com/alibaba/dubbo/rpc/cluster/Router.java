/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import java.util.List;

public interface Router
extends Comparable<Router> {
    public URL getUrl();

    public <T> List<Invoker<T>> route(List<Invoker<T>> var1, URL var2, Invocation var3) throws RpcException;
}

