/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo.telnet;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.telnet.support.Help;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import java.lang.reflect.Method;
import java.util.Collection;

@Activate
@Help(parameter="[-l] [service]", summary="List services and methods.", detail="List services and methods.")
public class ListTelnetHandler
implements TelnetHandler {
    @Override
    public String telnet(Channel channel, String message) {
        StringBuilder buf = new StringBuilder();
        String service = null;
        boolean detail = false;
        if (message.length() > 0) {
            String[] parts = message.split("\\s+");
            for (String part : parts) {
                if ("-l".equals(part)) {
                    detail = true;
                    continue;
                }
                if (service != null && service.length() > 0) {
                    return "Invaild parameter " + part;
                }
                service = part;
            }
        } else {
            service = (String)channel.getAttribute("telnet.service");
            if (service != null && service.length() > 0) {
                buf.append("Use default service " + service + ".\r\n");
            }
        }
        if (service == null || service.length() == 0) {
            for (Exporter exporter : DubboProtocol.getDubboProtocol().getExporters()) {
                if (buf.length() > 0) {
                    buf.append("\r\n");
                }
                buf.append(exporter.getInvoker().getInterface().getName());
                if (!detail) continue;
                buf.append(" -> ");
                buf.append(exporter.getInvoker().getUrl());
            }
        } else {
            Invoker<?> invoker = null;
            for (Exporter<?> exporter : DubboProtocol.getDubboProtocol().getExporters()) {
                if (!service.equals(exporter.getInvoker().getInterface().getSimpleName()) && !service.equals(exporter.getInvoker().getInterface().getName()) && !service.equals(exporter.getInvoker().getUrl().getPath())) continue;
                invoker = exporter.getInvoker();
                break;
            }
            if (invoker != null) {
                Method[] methods;
                for (Method method : methods = invoker.getInterface().getMethods()) {
                    if (buf.length() > 0) {
                        buf.append("\r\n");
                    }
                    if (detail) {
                        buf.append(ReflectUtils.getName(method));
                        continue;
                    }
                    buf.append(method.getName());
                }
            } else {
                buf.append("No such service " + service);
            }
        }
        return buf.toString();
    }
}

