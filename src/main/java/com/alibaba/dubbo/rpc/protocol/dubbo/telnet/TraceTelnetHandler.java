/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo.telnet;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.telnet.support.Help;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import com.alibaba.dubbo.rpc.protocol.dubbo.filter.TraceFilter;
import java.lang.reflect.Method;
import java.util.Collection;

@Activate
@Help(parameter="[service] [method] [times]", summary="Trace the service.", detail="Trace the service.")
public class TraceTelnetHandler
implements TelnetHandler {
    @Override
    public String telnet(Channel channel, String message) {
        String method;
        String times;
        String service = (String)channel.getAttribute("telnet.service");
        if (!(service != null && service.length() != 0 || message != null && message.length() != 0)) {
            return "Please input service name, eg: \r\ntrace XxxService\r\ntrace XxxService xxxMethod\r\ntrace XxxService xxxMethod 10\r\nor \"cd XxxService\" firstly.";
        }
        String[] parts = message.split("\\s+");
        if (service == null || service.length() == 0) {
            service = parts.length > 0 ? parts[0] : null;
            method = parts.length > 1 ? parts[1] : null;
        } else {
            String string = method = parts.length > 0 ? parts[0] : null;
        }
        if (StringUtils.isInteger(method)) {
            times = method;
            method = null;
        } else {
            String string = times = parts.length > 2 ? parts[2] : "1";
        }
        if (!StringUtils.isInteger(times)) {
            return "Illegal times " + times + ", must be integer.";
        }
        Invoker<?> invoker = null;
        for (Exporter<?> exporter : DubboProtocol.getDubboProtocol().getExporters()) {
            if (!service.equals(exporter.getInvoker().getInterface().getSimpleName()) && !service.equals(exporter.getInvoker().getInterface().getName()) && !service.equals(exporter.getInvoker().getUrl().getPath())) continue;
            invoker = exporter.getInvoker();
            break;
        }
        if (invoker != null) {
            if (method != null && method.length() > 0) {
                boolean found = false;
                for (Method m : invoker.getInterface().getMethods()) {
                    if (!m.getName().equals(method)) continue;
                    found = true;
                    break;
                }
                if (!found) {
                    return "No such method " + method + " in class " + invoker.getInterface().getName();
                }
            }
        } else {
            return "No such service " + service;
        }
        TraceFilter.addTracer(invoker.getInterface(), method, channel, Integer.parseInt(times));
        return null;
    }
}

