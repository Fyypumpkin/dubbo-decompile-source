/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson.JSON
 */
package com.alibaba.dubbo.rpc.protocol.dubbo.telnet;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.PojoUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.telnet.support.Help;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import com.alibaba.fastjson.JSON;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Activate
@Help(parameter="[service.]method(args)", summary="Invoke the service method.", detail="Invoke the service method.")
public class InvokeTelnetHandler
implements TelnetHandler {
    @Override
    public String telnet(Channel channel, String message) {
        List list;
        int i;
        if (message == null || message.length() == 0) {
            return "Please input method name, eg: \r\ninvoke xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})\r\ninvoke XxxService.xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})\r\ninvoke com.xxx.XxxService.xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})";
        }
        StringBuilder buf = new StringBuilder();
        String service = (String)channel.getAttribute("telnet.service");
        if (service != null && service.length() > 0) {
            buf.append("Use default service " + service + ".\r\n");
        }
        if ((i = message.indexOf("(")) < 0 || !message.endsWith(")")) {
            return "Invalid parameters, format: service.method(args)";
        }
        String method = message.substring(0, i).trim();
        String args = message.substring(i + 1, message.length() - 1).trim();
        i = method.lastIndexOf(".");
        if (i >= 0) {
            service = method.substring(0, i).trim();
            method = method.substring(i + 1).trim();
        }
        try {
            list = (List)JSON.parseObject((String)("[" + args + "]"), List.class);
        }
        catch (Throwable t) {
            return "Invalid json argument, cause: " + t.getMessage();
        }
        Invoker<?> invoker = null;
        Method invokeMethod = null;
        for (Exporter<?> exporter : DubboProtocol.getDubboProtocol().getExporters()) {
            if (service == null || service.length() == 0) {
                invokeMethod = InvokeTelnetHandler.findMethod(exporter, method, list);
                if (invokeMethod == null) continue;
                invoker = exporter.getInvoker();
                break;
            }
            if (!service.equals(exporter.getInvoker().getInterface().getSimpleName()) && !service.equals(exporter.getInvoker().getInterface().getName()) && !service.equals(exporter.getInvoker().getUrl().getPath())) continue;
            invokeMethod = InvokeTelnetHandler.findMethod(exporter, method, list);
            invoker = exporter.getInvoker();
            break;
        }
        if (invoker != null) {
            if (invokeMethod != null) {
                try {
                    Object[] array = PojoUtils.realize(list.toArray(), invokeMethod.getParameterTypes(), invokeMethod.getGenericParameterTypes());
                    RpcContext.getContext().setLocalAddress(channel.getLocalAddress()).setRemoteAddress(channel.getRemoteAddress());
                    long start = System.currentTimeMillis();
                    Object result = invoker.invoke(new RpcInvocation(invokeMethod, array)).recreate();
                    long end = System.currentTimeMillis();
                    buf.append(JSON.toJSON((Object)result));
                    buf.append("\r\nelapsed: ");
                    buf.append(end - start);
                    buf.append(" ms.");
                }
                catch (Throwable t) {
                    return "Failed to invoke method " + invokeMethod.getName() + ", cause: " + StringUtils.toString(t);
                }
            } else {
                buf.append("No such method " + method + " in service " + service);
            }
        } else {
            buf.append("No such service " + service);
        }
        return buf.toString();
    }

    private static Method findMethod(Exporter<?> exporter, String method, List<Object> args) {
        Invoker<?> invoker = exporter.getInvoker();
        Method[] methods = invoker.getInterface().getMethods();
        Method invokeMethod = null;
        for (Method m : methods) {
            if (!m.getName().equals(method) || m.getParameterTypes().length != args.size()) continue;
            if (invokeMethod != null) {
                if (InvokeTelnetHandler.isMatch(invokeMethod.getParameterTypes(), args)) {
                    invokeMethod = m;
                    break;
                }
            } else {
                invokeMethod = m;
            }
            invoker = exporter.getInvoker();
        }
        return invokeMethod;
    }

    private static boolean isMatch(Class<?>[] types, List<Object> args) {
        if (types.length != args.size()) {
            return false;
        }
        for (int i = 0; i < types.length; ++i) {
            Class<?> type = types[i];
            Object arg = args.get(i);
            if (ReflectUtils.isPrimitive(arg.getClass())) {
                if (ReflectUtils.isPrimitive(type)) continue;
                return false;
            }
            if (arg instanceof Map) {
                String name = (String)((Map)arg).get("class");
                Class<?> cls = arg.getClass();
                if (name != null && name.length() > 0) {
                    cls = ReflectUtils.forName(name);
                }
                if (type.isAssignableFrom(cls)) continue;
                return false;
            }
            if (!(arg instanceof Collection ? !type.isArray() && !type.isAssignableFrom(arg.getClass()) : !type.isAssignableFrom(arg.getClass()))) continue;
            return false;
        }
        return true;
    }
}

