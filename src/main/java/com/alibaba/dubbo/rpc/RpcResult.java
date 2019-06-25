/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.rpc.Result;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RpcResult
implements Result,
Serializable {
    private static final long serialVersionUID = -6925924956850004727L;
    private Object result;
    private Throwable exception;
    private Map<String, String> attachments = new HashMap<String, String>();

    public RpcResult() {
    }

    public RpcResult(Object result) {
        this.result = result;
    }

    public RpcResult(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public Object recreate() throws Throwable {
        if (this.exception != null) {
            throw this.exception;
        }
        return this.result;
    }

    @Deprecated
    @Override
    public Object getResult() {
        return this.getValue();
    }

    @Deprecated
    public void setResult(Object result) {
        this.setValue(result);
    }

    @Override
    public Object getValue() {
        return this.result;
    }

    public void setValue(Object value) {
        this.result = value;
    }

    @Override
    public Throwable getException() {
        return this.exception;
    }

    public void setException(Throwable e) {
        this.exception = e;
    }

    @Override
    public boolean hasException() {
        return this.exception != null;
    }

    @Override
    public Map<String, String> getAttachments() {
        return this.attachments;
    }

    @Override
    public String getAttachment(String key) {
        return this.attachments.get(key);
    }

    @Override
    public String getAttachment(String key, String defaultValue) {
        String result = this.attachments.get(key);
        if (result == null || result.length() == 0) {
            result = defaultValue;
        }
        return result;
    }

    public void setAttachments(Map<String, String> map) {
        if (map != null && map.size() > 0) {
            this.attachments.putAll(map);
        }
    }

    public void setAttachment(String key, String value) {
        this.attachments.put(key, value);
    }

    public String toString() {
        return "RpcResult [result=" + this.result + ", exception=" + this.exception + "]";
    }
}

