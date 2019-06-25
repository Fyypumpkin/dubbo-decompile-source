/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.router;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MockInvokersSelector
implements Router {
    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if (invocation.getAttachments() == null) {
            return this.getNormalInvokers(invokers);
        }
        String value = invocation.getAttachments().get("invocation.need.mock");
        if (value == null) {
            return this.getNormalInvokers(invokers);
        }
        if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
            return this.getMockedInvokers(invokers);
        }
        return invokers;
    }

    private <T> List<Invoker<T>> getMockedInvokers(List<Invoker<T>> invokers) {
        if (!this.hasMockProviders(invokers)) {
            return null;
        }
        ArrayList<Invoker<T>> sInvokers = new ArrayList<Invoker<T>>(1);
        for (Invoker<T> invoker : invokers) {
            if (!invoker.getUrl().getProtocol().equals("mock")) continue;
            sInvokers.add(invoker);
        }
        return sInvokers;
    }

    private <T> List<Invoker<T>> getNormalInvokers(List<Invoker<T>> invokers) {
        if (!this.hasMockProviders(invokers)) {
            return invokers;
        }
        ArrayList<Invoker<T>> sInvokers = new ArrayList<Invoker<T>>(invokers.size());
        for (Invoker<T> invoker : invokers) {
            if (invoker.getUrl().getProtocol().equals("mock")) continue;
            sInvokers.add(invoker);
        }
        return sInvokers;
    }

    private <T> boolean hasMockProviders(List<Invoker<T>> invokers) {
        boolean hasMockProvider = false;
        for (Invoker<T> invoker : invokers) {
            if (!invoker.getUrl().getProtocol().equals("mock")) continue;
            hasMockProvider = true;
            break;
        }
        return hasMockProvider;
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public int compareTo(Router o) {
        return 1;
    }
}

