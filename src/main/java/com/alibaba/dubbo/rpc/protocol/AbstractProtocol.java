/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractProtocol
implements Protocol {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final Map<String, Exporter<?>> exporterMap = new ConcurrentHashMap();
    protected final Set<Invoker<?>> invokers = new ConcurrentHashSet();

    protected static String serviceKey(URL url) {
        return ProtocolUtils.serviceKey(url);
    }

    protected static String serviceKey(int port, String serviceName, String serviceVersion, String serviceGroup) {
        return ProtocolUtils.serviceKey(port, serviceName, serviceVersion, serviceGroup);
    }

    @Override
    public void destroy() {
        for (Invoker<?> invoker : this.invokers) {
            if (invoker == null) continue;
            this.invokers.remove(invoker);
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Destroy reference: " + invoker.getUrl());
                }
                invoker.destroy();
            }
            catch (Throwable t) {
                this.logger.warn(t.getMessage(), t);
            }
        }
        for (String key : new ArrayList<String>(this.exporterMap.keySet())) {
            Exporter<?> exporter = this.exporterMap.remove(key);
            if (exporter == null) continue;
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Unexport service: " + exporter.getInvoker().getUrl());
                }
                exporter.unexport();
            }
            catch (Throwable t) {
                this.logger.warn(t.getMessage(), t);
            }
        }
    }

    protected static int getServerShutdownTimeout() {
        int timeout = 10000;
        String value = ConfigUtils.getProperty("dubbo.service.shutdown.wait");
        if (value != null && value.length() > 0) {
            try {
                timeout = Integer.parseInt(value);
            }
            catch (Exception exception) {}
        } else {
            value = ConfigUtils.getProperty("dubbo.service.shutdown.wait.seconds");
            if (value != null && value.length() > 0) {
                try {
                    timeout = Integer.parseInt(value) * 1000;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return timeout;
    }
}

