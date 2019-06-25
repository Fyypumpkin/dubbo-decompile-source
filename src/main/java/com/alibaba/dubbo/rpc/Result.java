/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import java.util.Map;

public interface Result {
    public Object getValue();

    public Throwable getException();

    public boolean hasException();

    public Object recreate() throws Throwable;

    @Deprecated
    public Object getResult();

    public Map<String, String> getAttachments();

    public String getAttachment(String var1);

    public String getAttachment(String var1, String var2);
}

