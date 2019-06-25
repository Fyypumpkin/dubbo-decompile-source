/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.injvm;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import com.alibaba.dubbo.rpc.protocol.injvm.InjvmExporter;
import com.alibaba.dubbo.rpc.protocol.injvm.InjvmProtocol;
import java.util.Map;

class InjvmInvoker<T>
extends AbstractInvoker<T> {
    private final String key;
    private final Map<String, Exporter<?>> exporterMap;

    InjvmInvoker(Class<T> type, URL url, String key, Map<String, Exporter<?>> exporterMap) {
        super(type, url);
        this.key = key;
        this.exporterMap = exporterMap;
    }

    @Override
    public boolean isAvailable() {
        InjvmExporter exporter = (InjvmExporter)this.exporterMap.get(this.key);
        if (exporter == null) {
            return false;
        }
        return super.isAvailable();
    }

    @Override
    public Result doInvoke(Invocation invocation) throws Throwable {
        Exporter<?> exporter = InjvmProtocol.getExporter(this.exporterMap, this.getUrl());
        if (exporter == null) {
            throw new RpcException("Service [" + this.key + "] not found.");
        }
        RpcContext.getContext().setRemoteAddress("127.0.0.1", 0);
        return exporter.getInvoker().invoke(invocation);
    }
}

