/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.injvm;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProtocol;
import com.alibaba.dubbo.rpc.protocol.injvm.InjvmExporter;
import com.alibaba.dubbo.rpc.protocol.injvm.InjvmInvoker;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import java.util.Collection;
import java.util.Map;

public class InjvmProtocol
extends AbstractProtocol
implements Protocol {
    public static final String NAME = "injvm";
    public static final int DEFAULT_PORT = 0;
    private static InjvmProtocol INSTANCE;

    @Override
    public int getDefaultPort() {
        return 0;
    }

    public InjvmProtocol() {
        INSTANCE = this;
    }

    public static InjvmProtocol getInjvmProtocol() {
        if (INSTANCE == null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(NAME);
        }
        return INSTANCE;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        return new InjvmExporter<T>(invoker, invoker.getUrl().getServiceKey(), this.exporterMap);
    }

    @Override
    public <T> Invoker<T> refer(Class<T> serviceType, URL url) throws RpcException {
        return new InjvmInvoker<T>(serviceType, url, url.getServiceKey(), this.exporterMap);
    }

    static Exporter<?> getExporter(Map<String, Exporter<?>> map, URL key) {
        Exporter<?> result = null;
        if (!key.getServiceKey().contains("*")) {
            result = map.get(key.getServiceKey());
        } else if (map != null && !map.isEmpty()) {
            for (Exporter<?> exporter : map.values()) {
                if (!UrlUtils.isServiceKeyMatch(key, exporter.getInvoker().getUrl())) continue;
                result = exporter;
                break;
            }
        }
        if (result == null) {
            return null;
        }
        if (ProtocolUtils.isGeneric(result.getInvoker().getUrl().getParameter("generic"))) {
            return null;
        }
        return result;
    }

    public boolean isInjvmRefer(URL url) {
        String scope = url.getParameter("scope");
        boolean isJvmRefer = NAME.toString().equals(url.getProtocol()) ? false : ("local".equals(scope) || url.getParameter(NAME, false) ? true : ("remote".equals(scope) ? false : (url.getParameter("generic", false) ? false : InjvmProtocol.getExporter(this.exporterMap, url) != null)));
        return isJvmRefer;
    }
}

