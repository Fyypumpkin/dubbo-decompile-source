/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.rpc.Invoker;
import java.util.Map;

public interface Invocation {
    public String getMethodName();

    public Class<?>[] getParameterTypes();

    public Object[] getArguments();

    public Map<String, String> getAttachments();

    public String getAttachment(String var1);

    public String getAttachment(String var1, String var2);

    public Invoker<?> getInvoker();
}

