/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RpcInvocation
implements Invocation,
Serializable {
    private static final long serialVersionUID = -4355285085441097045L;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] arguments;
    private Map<String, String> attachments;
    private transient Invoker<?> invoker;

    public RpcInvocation() {
    }

    public RpcInvocation(Invocation invocation, Invoker<?> invoker) {
        this(invocation.getMethodName(), invocation.getParameterTypes(), invocation.getArguments(), new HashMap<String, String>(invocation.getAttachments()), invocation.getInvoker());
        if (invoker != null) {
            URL url = invoker.getUrl();
            this.setAttachment("path", url.getPath());
            if (url.hasParameter("interface")) {
                this.setAttachment("interface", url.getParameter("interface"));
            }
            if (url.hasParameter("group")) {
                this.setAttachment("group", url.getParameter("group"));
            }
            if (url.hasParameter("version")) {
                this.setAttachment("version", url.getParameter("version", "0.0.0"));
            }
            if (url.hasParameter("timeout")) {
                this.setAttachment("timeout", url.getParameter("timeout"));
            }
            if (url.hasParameter("token")) {
                this.setAttachment("token", url.getParameter("token"));
            }
            if (url.hasParameter("application")) {
                this.setAttachment("application", url.getParameter("application"));
            }
        }
    }

    public RpcInvocation(Invocation invocation) {
        this(invocation.getMethodName(), invocation.getParameterTypes(), invocation.getArguments(), invocation.getAttachments(), invocation.getInvoker());
    }

    public RpcInvocation(Method method, Object[] arguments) {
        this(method.getName(), method.getParameterTypes(), arguments, null, null);
    }

    public RpcInvocation(Method method, Object[] arguments, Map<String, String> attachment) {
        this(method.getName(), method.getParameterTypes(), arguments, attachment, null);
    }

    public RpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments) {
        this(methodName, parameterTypes, arguments, null, null);
    }

    public RpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments, Map<String, String> attachments) {
        this(methodName, parameterTypes, arguments, attachments, null);
    }

    public RpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments, Map<String, String> attachments, Invoker<?> invoker) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes == null ? new Class[0] : parameterTypes;
        this.arguments = arguments == null ? new Object[0] : arguments;
        this.attachments = attachments == null ? new HashMap() : attachments;
        this.invoker = invoker;
    }

    @Override
    public Invoker<?> getInvoker() {
        return this.invoker;
    }

    public void setInvoker(Invoker<?> invoker) {
        this.invoker = invoker;
    }

    @Override
    public String getMethodName() {
        return this.methodName;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return this.parameterTypes;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public Map<String, String> getAttachments() {
        return this.attachments;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes == null ? new Class[0] : parameterTypes;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments == null ? new Object[0] : arguments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments == null ? new HashMap() : attachments;
    }

    public void setAttachment(String key, String value) {
        if (this.attachments == null) {
            this.attachments = new HashMap<String, String>();
        }
        this.attachments.put(key, value);
    }

    public void setAttachmentIfAbsent(String key, String value) {
        if (this.attachments == null) {
            this.attachments = new HashMap<String, String>();
        }
        if (!this.attachments.containsKey(key)) {
            this.attachments.put(key, value);
        }
    }

    public void addAttachments(Map<String, String> attachments) {
        if (attachments == null) {
            return;
        }
        if (this.attachments == null) {
            this.attachments = new HashMap<String, String>();
        }
        this.attachments.putAll(attachments);
    }

    public void addAttachmentsIfAbsent(Map<String, String> attachments) {
        if (attachments == null) {
            return;
        }
        for (Map.Entry<String, String> entry : attachments.entrySet()) {
            this.setAttachmentIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String getAttachment(String key) {
        if (this.attachments == null) {
            return null;
        }
        return this.attachments.get(key);
    }

    @Override
    public String getAttachment(String key, String defaultValue) {
        if (this.attachments == null) {
            return defaultValue;
        }
        String value = this.attachments.get(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public String toString() {
        return "RpcInvocation [methodName=" + this.methodName + ", parameterTypes=" + Arrays.toString(this.parameterTypes) + ", arguments=" + Arrays.toString(this.arguments) + ", attachments=" + this.attachments + "]";
    }
}

