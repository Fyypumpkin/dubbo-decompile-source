/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;

@SPI(value="javassist")
public interface ProxyFactory {
    @Adaptive(value={"proxy"})
    public <T> T getProxy(Invoker<T> var1) throws RpcException;

    @Adaptive(value={"proxy"})
    public <T> Invoker<T> getInvoker(T var1, Class<T> var2, URL var3) throws RpcException;
}

