/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.monitor.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.monitor.Monitor;
import com.alibaba.dubbo.monitor.MonitorService;
import com.alibaba.dubbo.monitor.dubbo.DubboMonitor;
import com.alibaba.dubbo.monitor.support.AbstractMonitorFactory;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;

public class DubboMonitorFactroy
extends AbstractMonitorFactory {
    private Protocol protocol;
    private ProxyFactory proxyFactory;

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public void setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    @Override
    protected Monitor createMonitor(URL url) {
        String filter;
        if ((url = url.setProtocol(url.getParameter("protocol", "dubbo"))).getPath() == null || url.getPath().length() == 0) {
            url = url.setPath(MonitorService.class.getName());
        }
        filter = (filter = url.getParameter("reference.filter")) == null || filter.length() == 0 ? "" : filter + ",";
        url = url.addParameters("cluster", "failsafe", "check", String.valueOf(false), "reference.filter", filter + "-monitor");
        Invoker<MonitorService> monitorInvoker = this.protocol.refer(MonitorService.class, url);
        MonitorService monitorService = this.proxyFactory.getProxy(monitorInvoker);
        return new DubboMonitor(monitorInvoker, monitorService);
    }
}

