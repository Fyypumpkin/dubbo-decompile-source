/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.listener;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.ExporterListener;
import com.alibaba.dubbo.rpc.Invoker;
import java.util.List;

public class ListenerExporterWrapper<T>
implements Exporter<T> {
    private static final Logger logger = LoggerFactory.getLogger(ListenerExporterWrapper.class);
    private final Exporter<T> exporter;
    private final List<ExporterListener> listeners;

    public ListenerExporterWrapper(Exporter<T> exporter, List<ExporterListener> listeners) {
        if (exporter == null) {
            throw new IllegalArgumentException("exporter == null");
        }
        this.exporter = exporter;
        this.listeners = listeners;
        if (listeners != null && listeners.size() > 0) {
            RuntimeException exception = null;
            for (ExporterListener listener : listeners) {
                if (listener == null) continue;
                try {
                    listener.exported(this);
                }
                catch (RuntimeException t) {
                    logger.error(t.getMessage(), t);
                    exception = t;
                }
            }
            if (exception != null) {
                throw exception;
            }
        }
    }

    @Override
    public Invoker<T> getInvoker() {
        return this.exporter.getInvoker();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void unexport() {
        try {
            this.exporter.unexport();
        }
        finally {
            if (this.listeners != null && this.listeners.size() > 0) {
                RuntimeException exception = null;
                for (ExporterListener listener : this.listeners) {
                    if (listener == null) continue;
                    try {
                        listener.unexported(this);
                    }
                    catch (RuntimeException t) {
                        logger.error(t.getMessage(), t);
                        exception = t;
                    }
                }
                if (exception != null) {
                    throw exception;
                }
            }
        }
    }
}

