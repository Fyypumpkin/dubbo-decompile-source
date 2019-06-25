/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.listener;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.InvokerListener;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import java.util.List;

public class ListenerInvokerWrapper<T>
implements Invoker<T> {
    private static final Logger logger = LoggerFactory.getLogger(ListenerInvokerWrapper.class);
    private final Invoker<T> invoker;
    private final List<InvokerListener> listeners;

    public ListenerInvokerWrapper(Invoker<T> invoker, List<InvokerListener> listeners) {
        if (invoker == null) {
            throw new IllegalArgumentException("invoker == null");
        }
        this.invoker = invoker;
        this.listeners = listeners;
        if (listeners != null && listeners.size() > 0) {
            for (InvokerListener listener : listeners) {
                if (listener == null) continue;
                try {
                    listener.referred(invoker);
                }
                catch (Throwable t) {
                    logger.error(t.getMessage(), t);
                }
            }
        }
    }

    @Override
    public Class<T> getInterface() {
        return this.invoker.getInterface();
    }

    @Override
    public URL getUrl() {
        return this.invoker.getUrl();
    }

    @Override
    public boolean isAvailable() {
        return this.invoker.isAvailable();
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        return this.invoker.invoke(invocation);
    }

    public String toString() {
        return this.getInterface() + " -> " + this.getUrl() == null ? " " : this.getUrl().toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void destroy() {
        try {
            this.invoker.destroy();
        }
        finally {
            if (this.listeners != null && this.listeners.size() > 0) {
                for (InvokerListener listener : this.listeners) {
                    if (listener == null) continue;
                    try {
                        listener.destroyed(this.invoker);
                    }
                    catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                    }
                }
            }
        }
    }
}

