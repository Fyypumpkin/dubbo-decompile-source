/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.service;

import com.alibaba.dubbo.rpc.service.GenericException;

public interface GenericService {
    public Object $invoke(String var1, String[] var2, Object[] var3) throws GenericException;

    @Deprecated
    default public byte[] $invokeWithJsonArgs(String method, String[] parameterTypes, byte[] jsonArgs) {
        return null;
    }
}

