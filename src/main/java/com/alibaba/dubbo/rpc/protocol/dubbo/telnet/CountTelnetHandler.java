/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo.telnet;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.telnet.support.Help;
import com.alibaba.dubbo.remoting.telnet.support.TelnetUtils;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcStatus;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Activate
@Help(parameter="[service] [method] [times]", summary="Count the service.", detail="Count the service.")
public class CountTelnetHandler
implements TelnetHandler {
    @Override
    public String telnet(final Channel channel, String message) {
        String method;
        String times;
        String service = (String)channel.getAttribute("telnet.service");
        if (!(service != null && service.length() != 0 || message != null && message.length() != 0)) {
            return "Please input service name, eg: \r\ncount XxxService\r\ncount XxxService xxxMethod\r\ncount XxxService xxxMethod 10\r\nor \"cd XxxService\" firstly.";
        }
        StringBuilder buf = new StringBuilder();
        if (service != null && service.length() > 0) {
            buf.append("Use default service " + service + ".\r\n");
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
        final int t = Integer.parseInt(times);
        Invoker<?> invoker = null;
        for (Exporter<?> exporter : DubboProtocol.getDubboProtocol().getExporters()) {
            if (!service.equals(exporter.getInvoker().getInterface().getSimpleName()) && !service.equals(exporter.getInvoker().getInterface().getName()) && !service.equals(exporter.getInvoker().getUrl().getPath())) continue;
            invoker = exporter.getInvoker();
            break;
        }
        if (invoker != null) {
            if (t > 0) {
                final String mtd = method;
                final Invoker<?> inv = invoker;
                final String prompt = channel.getUrl().getParameter("prompt", "telnet");
                Thread thread = new Thread(new Runnable(){

                    @Override
                    public void run() {
                        for (int i = 0; i < t; ++i) {
                            String result = CountTelnetHandler.this.count(inv, mtd);
                            try {
                                channel.send("\r\n" + result);
                            }
                            catch (RemotingException e1) {
                                return;
                            }
                            if (i >= t - 1) continue;
                            try {
                                Thread.sleep(1000L);
                                continue;
                            }
                            catch (InterruptedException e1) {
                                // empty catch block
                            }
                        }
                        try {
                            channel.send("\r\n" + prompt + "> ");
                        }
                        catch (RemotingException e1) {
                            return;
                        }
                    }
                }, "TelnetCount");
                thread.setDaemon(true);
                thread.start();
            }
        } else {
            buf.append("No such service " + service);
        }
        return buf.toString();
    }

    private String count(Invoker<?> invoker, String method) {
        URL url = invoker.getUrl();
        ArrayList<List<String>> table = new ArrayList<List<String>>();
        ArrayList<String> header = new ArrayList<String>();
        header.add("method");
        header.add("total");
        header.add("failed");
        header.add("active");
        header.add("average");
        header.add("max");
        if (method == null || method.length() == 0) {
            for (Method m : invoker.getInterface().getMethods()) {
                RpcStatus count = RpcStatus.getStatus(url, m.getName());
                ArrayList<String> row = new ArrayList<String>();
                row.add(m.getName());
                row.add(String.valueOf(count.getTotal()));
                row.add(String.valueOf(count.getFailed()));
                row.add(String.valueOf(count.getActive()));
                row.add(String.valueOf(count.getSucceededAverageElapsed()) + "ms");
                row.add(String.valueOf(count.getSucceededMaxElapsed()) + "ms");
                table.add(row);
            }
        } else {
            boolean found = false;
            for (Method m : invoker.getInterface().getMethods()) {
                if (!m.getName().equals(method)) continue;
                found = true;
                break;
            }
            if (found) {
                RpcStatus count = RpcStatus.getStatus(url, method);
                ArrayList<String> row = new ArrayList<String>();
                row.add(method);
                row.add(String.valueOf(count.getTotal()));
                row.add(String.valueOf(count.getFailed()));
                row.add(String.valueOf(count.getActive()));
                row.add(String.valueOf(count.getSucceededAverageElapsed()) + "ms");
                row.add(String.valueOf(count.getSucceededMaxElapsed()) + "ms");
                table.add(row);
            } else {
                return "No such method " + method + " in class " + invoker.getInterface().getName();
            }
        }
        return TelnetUtils.toTable(header, table);
    }

}

