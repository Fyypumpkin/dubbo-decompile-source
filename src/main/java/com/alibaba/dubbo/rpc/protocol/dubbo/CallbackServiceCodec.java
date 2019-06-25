/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.bytecode.Wrapper;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.protocol.dubbo.ChannelWrappedInvoker;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class CallbackServiceCodec {
    private static final Logger logger = LoggerFactory.getLogger(CallbackServiceCodec.class);
    private static final ProxyFactory proxyFactory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();
    private static final Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();
    private static final byte CALLBACK_NONE = 0;
    private static final byte CALLBACK_CREATE = 1;
    private static final byte CALLBACK_DESTROY = 2;
    private static final String INV_ATT_CALLBACK_KEY = "sys_callback_arg-";

    CallbackServiceCodec() {
    }

    private static byte isCallBack(URL url, String methodName, int argIndex) {
        String callback;
        int isCallback = 0;
        if (url != null && (callback = url.getParameter(methodName + "." + argIndex + ".callback")) != null) {
            if (callback.equalsIgnoreCase("true")) {
                isCallback = 1;
            } else if (callback.equalsIgnoreCase("false")) {
                isCallback = 2;
            }
        }
        return (byte)isCallback;
    }

    private static String exportOrunexportCallbackService(Channel channel, URL url, Class clazz, Object inst, Boolean export) throws IOException {
        int instid = System.identityHashCode(inst);
        HashMap<String, String> params = new HashMap<String, String>(3);
        params.put("isserver", Boolean.FALSE.toString());
        params.put("is_callback_service", Boolean.TRUE.toString());
        String group = url.getParameter("group");
        if (group != null && group.length() > 0) {
            params.put("group", group);
        }
        params.put("methods", StringUtils.join(Wrapper.getWrapper(clazz).getDeclaredMethodNames(), ","));
        HashMap<String, String> tmpmap = new HashMap<String, String>(url.getParameters());
        tmpmap.putAll(params);
        tmpmap.remove("version");
        tmpmap.put("interface", clazz.getName());
        URL exporturl = new URL(url.getProtocol() == null ? "dubbo" : url.getProtocol(), channel.getLocalAddress().getAddress().getHostAddress(), channel.getLocalAddress().getPort(), clazz.getName() + "." + instid, tmpmap);
        String cacheKey = CallbackServiceCodec.getClientSideCallbackServiceCacheKey(instid);
        String countkey = CallbackServiceCodec.getClientSideCountKey(clazz.getName());
        if (export.booleanValue()) {
            if (!channel.hasAttribute(cacheKey) && !CallbackServiceCodec.isInstancesOverLimit(channel, url, clazz.getName(), instid, false)) {
                Invoker<Object> invoker = proxyFactory.getInvoker(inst, clazz, exporturl);
                Exporter<Object> exporter = protocol.export(invoker);
                channel.setAttribute(cacheKey, exporter);
                logger.info("export a callback service :" + exporturl + ", on " + channel + ", url is: " + url);
                CallbackServiceCodec.increaseInstanceCount(channel, countkey);
            }
        } else if (channel.hasAttribute(cacheKey)) {
            Exporter exporter = (Exporter)channel.getAttribute(cacheKey);
            exporter.unexport();
            channel.removeAttribute(cacheKey);
            CallbackServiceCodec.decreaseInstanceCount(channel, countkey);
        }
        return String.valueOf(instid);
    }

    private static Object referOrdestroyCallbackService(Channel channel, URL url, Class<?> clazz, Invocation inv, int instid, boolean isRefer) {
        Object proxy = null;
        String invokerCacheKey = CallbackServiceCodec.getServerSideCallbackInvokerCacheKey(channel, clazz.getName(), instid);
        String proxyCacheKey = CallbackServiceCodec.getServerSideCallbackServiceCacheKey(channel, clazz.getName(), instid);
        proxy = channel.getAttribute(proxyCacheKey);
        String countkey = CallbackServiceCodec.getServerSideCountKey(channel, clazz.getName());
        if (isRefer) {
            if (proxy == null) {
                URL referurl = URL.valueOf("callback://" + url.getAddress() + "/" + clazz.getName() + "?" + "interface" + "=" + clazz.getName());
                if (!CallbackServiceCodec.isInstancesOverLimit(channel, referurl = referurl.addParametersIfAbsent(url.getParameters()).removeParameter("methods"), clazz.getName(), instid, true)) {
                    ChannelWrappedInvoker invoker = new ChannelWrappedInvoker(clazz, channel, referurl, String.valueOf(instid));
                    proxy = proxyFactory.getProxy(invoker);
                    channel.setAttribute(proxyCacheKey, proxy);
                    channel.setAttribute(invokerCacheKey, invoker);
                    CallbackServiceCodec.increaseInstanceCount(channel, countkey);
                    ConcurrentHashSet callbackInvokers = (ConcurrentHashSet)channel.getAttribute("channel.callback.invokers.key");
                    if (callbackInvokers == null) {
                        callbackInvokers = new ConcurrentHashSet(1);
                        callbackInvokers.add(invoker);
                        channel.setAttribute("channel.callback.invokers.key", callbackInvokers);
                    }
                    logger.info("method " + inv.getMethodName() + " include a callback service :" + invoker.getUrl() + ", a proxy :" + invoker + " has been created.");
                }
            }
        } else if (proxy != null) {
            Invoker invoker = (Invoker)channel.getAttribute(invokerCacheKey);
            try {
                Set callbackInvokers = (Set)channel.getAttribute("channel.callback.invokers.key");
                if (callbackInvokers != null) {
                    callbackInvokers.remove(invoker);
                }
                invoker.destroy();
            }
            catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            channel.removeAttribute(proxyCacheKey);
            channel.removeAttribute(invokerCacheKey);
            CallbackServiceCodec.decreaseInstanceCount(channel, countkey);
        }
        return proxy;
    }

    private static String getClientSideCallbackServiceCacheKey(int instid) {
        return "callback.service.instid." + instid;
    }

    private static String getServerSideCallbackServiceCacheKey(Channel channel, String interfaceClass, int instid) {
        return "callback.service.proxy." + System.identityHashCode(channel) + "." + interfaceClass + "." + instid;
    }

    private static String getServerSideCallbackInvokerCacheKey(Channel channel, String interfaceClass, int instid) {
        return CallbackServiceCodec.getServerSideCallbackServiceCacheKey(channel, interfaceClass, instid) + ".invoker";
    }

    private static String getClientSideCountKey(String interfaceClass) {
        return "callback.service.instid." + interfaceClass + ".COUNT";
    }

    private static String getServerSideCountKey(Channel channel, String interfaceClass) {
        return "callback.service.proxy." + System.identityHashCode(channel) + "." + interfaceClass + ".COUNT";
    }

    private static boolean isInstancesOverLimit(Channel channel, URL url, String interfaceClass, int instid, boolean isServer) {
        Integer count = (Integer)channel.getAttribute(isServer ? CallbackServiceCodec.getServerSideCountKey(channel, interfaceClass) : CallbackServiceCodec.getClientSideCountKey(interfaceClass));
        int limit = url.getParameter("callbacks", 1);
        if (count != null && count >= limit) {
            throw new IllegalStateException("interface " + interfaceClass + " `s callback instances num exceed providers limit :" + limit + " ,current num: " + (count + 1) + ". The new callback service will not work !!! you can cancle the callback service which exported before. channel :" + channel);
        }
        return false;
    }

    private static void increaseInstanceCount(Channel channel, String countkey) {
        try {
            Integer count = (Integer)channel.getAttribute(countkey);
            if (count == null) {
                count = 1;
            } else {
                Integer n = count;
                Integer n2 = count = Integer.valueOf(count + 1);
            }
            channel.setAttribute(countkey, count);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void decreaseInstanceCount(Channel channel, String countkey) {
        try {
            Integer count = (Integer)channel.getAttribute(countkey);
            if (count == null || count <= 0) {
                return;
            }
            Integer n = count;
            Integer n2 = count = Integer.valueOf(count - 1);
            channel.setAttribute(countkey, count);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static Object encodeInvocationArgument(Channel channel, RpcInvocation inv, int paraIndex) throws IOException {
        URL url = inv.getInvoker() == null ? null : inv.getInvoker().getUrl();
        byte callbackstatus = CallbackServiceCodec.isCallBack(url, inv.getMethodName(), paraIndex);
        Object[] args = inv.getArguments();
        Class<?>[] pts = inv.getParameterTypes();
        switch (callbackstatus) {
            case 0: {
                return args[paraIndex];
            }
            case 1: {
                inv.setAttachment(INV_ATT_CALLBACK_KEY + paraIndex, CallbackServiceCodec.exportOrunexportCallbackService(channel, url, pts[paraIndex], args[paraIndex], true));
                return null;
            }
            case 2: {
                inv.setAttachment(INV_ATT_CALLBACK_KEY + paraIndex, CallbackServiceCodec.exportOrunexportCallbackService(channel, url, pts[paraIndex], args[paraIndex], false));
                return null;
            }
        }
        return args[paraIndex];
    }

    public static Object decodeInvocationArgument(Channel channel, RpcInvocation inv, Class<?>[] pts, int paraIndex, Object inObject) throws IOException {
        URL url = null;
        try {
            String name = channel.getUrl().getProtocol();
            if ("dubbo".equals(name)) {
                url = DubboProtocol.getDubboProtocol().getInvoker(channel, inv).getUrl();
            } else if ("tether".equals(name)) {
                try {
                    Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(channel.getUrl().getProtocol());
                    protocol = CallbackServiceCodec.nestedProtocol(protocol);
                    Method method = protocol.getClass().getDeclaredMethod("getInvoker", Channel.class, Invocation.class);
                    Invoker invoker = (Invoker)method.invoke(protocol, channel, inv);
                    url = invoker.getUrl();
                }
                catch (Exception protocol) {}
            }
        }
        catch (RemotingException e) {
            if (logger.isInfoEnabled()) {
                logger.info(e.getMessage(), e);
            }
            return inObject;
        }
        byte callbackstatus = CallbackServiceCodec.isCallBack(url, inv.getMethodName(), paraIndex);
        switch (callbackstatus) {
            case 0: {
                return inObject;
            }
            case 1: {
                try {
                    return CallbackServiceCodec.referOrdestroyCallbackService(channel, url, pts[paraIndex], inv, Integer.parseInt(inv.getAttachment(INV_ATT_CALLBACK_KEY + paraIndex)), true);
                }
                catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw new IOException(StringUtils.toString(e));
                }
            }
            case 2: {
                try {
                    return CallbackServiceCodec.referOrdestroyCallbackService(channel, url, pts[paraIndex], inv, Integer.parseInt(inv.getAttachment(INV_ATT_CALLBACK_KEY + paraIndex)), false);
                }
                catch (Exception e) {
                    throw new IOException(StringUtils.toString(e));
                }
            }
        }
        return inObject;
    }

    private static Protocol nestedProtocol(Protocol protocol) {
        if (protocol == null) {
            return null;
        }
        Protocol found = protocol;
        try {
            Field field = protocol.getClass().getDeclaredField("protocol");
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            Protocol search = (Protocol)field.get(protocol);
            while (search != null) {
                found = search;
                field = search.getClass().getDeclaredField("protocol");
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                search = (Protocol)field.get(search);
            }
        }
        catch (NoSuchFieldException field) {
        }
        catch (IllegalAccessException field) {
            // empty catch block
        }
        return found;
    }
}

