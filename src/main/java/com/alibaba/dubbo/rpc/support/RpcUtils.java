/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcInvocation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLong;

public class RpcUtils {
    private static final Logger logger = LoggerFactory.getLogger(RpcUtils.class);
    private static final AtomicLong INVOKE_ID = new AtomicLong(0L);

    public static Class<?> getReturnType(Invocation invocation) {
        try {
            String service;
            if (invocation != null && invocation.getInvoker() != null && invocation.getInvoker().getUrl() != null && !invocation.getMethodName().startsWith("$") && (service = invocation.getInvoker().getUrl().getServiceInterface()) != null && service.length() > 0) {
                Class<?> cls = ReflectUtils.forName(service);
                Method method = cls.getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                if (method.getReturnType() == Void.TYPE) {
                    return null;
                }
                return method.getReturnType();
            }
        }
        catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
        return null;
    }

    public static Type[] getReturnTypes(Invocation invocation) {
        try {
            String service;
            if (invocation != null && invocation.getInvoker() != null && invocation.getInvoker().getUrl() != null && !invocation.getMethodName().startsWith("$") && (service = invocation.getInvoker().getUrl().getServiceInterface()) != null && service.length() > 0) {
                Class<?> cls = ReflectUtils.forName(service);
                Method method = cls.getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                if (method.getReturnType() == Void.TYPE) {
                    return null;
                }
                return new Type[]{method.getReturnType(), method.getGenericReturnType()};
            }
        }
        catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
        return null;
    }

    public static Long getInvocationId(Invocation inv) {
        String id = inv.getAttachment("id");
        return id == null ? null : new Long(id);
    }

    public static void attachInvocationIdIfAsync(URL url, Invocation inv) {
        if (RpcUtils.isAttachInvocationId(url, inv) && RpcUtils.getInvocationId(inv) == null && inv instanceof RpcInvocation) {
            ((RpcInvocation)inv).setAttachment("id", String.valueOf(INVOKE_ID.getAndIncrement()));
        }
    }

    private static boolean isAttachInvocationId(URL url, Invocation invocation) {
        String value = url.getMethodParameter(invocation.getMethodName(), "invocationid.autoattach");
        if (value == null) {
            return RpcUtils.isAsync(url, invocation);
        }
        return Boolean.TRUE.toString().equalsIgnoreCase(value);
    }

    public static String getMethodName(Invocation invocation) {
        if ("$invoke".equals(invocation.getMethodName()) && invocation.getArguments() != null && invocation.getArguments().length > 0 && invocation.getArguments()[0] instanceof String) {
            return (String)invocation.getArguments()[0];
        }
        return invocation.getMethodName();
    }

    public static Object[] getArguments(Invocation invocation) {
        if ("$invoke".equals(invocation.getMethodName()) && invocation.getArguments() != null && invocation.getArguments().length > 2 && invocation.getArguments()[2] instanceof Object[]) {
            return (Object[])invocation.getArguments()[2];
        }
        return invocation.getArguments();
    }

    public static Class<?>[] getParameterTypes(Invocation invocation) {
        if ("$invoke".equals(invocation.getMethodName()) && invocation.getArguments() != null && invocation.getArguments().length > 1 && invocation.getArguments()[1] instanceof String[]) {
            String[] types = (String[])invocation.getArguments()[1];
            if (types == null) {
                return new Class[0];
            }
            Class[] parameterTypes = new Class[types.length];
            for (int i = 0; i < types.length; ++i) {
                parameterTypes[i] = ReflectUtils.forName(types[0]);
            }
            return parameterTypes;
        }
        return invocation.getParameterTypes();
    }

    public static boolean isAsync(URL url, Invocation inv) {
        boolean isAsync = Boolean.TRUE.toString().equals(inv.getAttachment("async")) ? true : url.getMethodParameter(RpcUtils.getMethodName(inv), "async", false);
        return isAsync;
    }

    public static boolean isOneway(URL url, Invocation inv) {
        boolean isOneway = Boolean.FALSE.toString().equals(inv.getAttachment("return")) ? true : !url.getMethodParameter(RpcUtils.getMethodName(inv), "return", true);
        return isOneway;
    }
}

