/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;

public abstract class AbstractExporter<T>
implements Exporter<T> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Invoker<T> invoker;
    private volatile boolean unexported = false;

    public AbstractExporter(Invoker<T> invoker) {
        if (invoker == null) {
            throw new IllegalStateException("service invoker == null");
        }
        if (invoker.getInterface() == null) {
            throw new IllegalStateException("service type == null");
        }
        if (invoker.getUrl() == null) {
            throw new IllegalStateException("service url == null");
        }
        this.invoker = invoker;
    }

    @Override
    public Invoker<T> getInvoker() {
        return this.invoker;
    }

    @Override
    public void unexport() {
        if (this.unexported) {
            return;
        }
        this.unexported = true;
        this.getInvoker().destroy();
    }

    public String toString() {
        return this.getInvoker().toString();
    }
}

