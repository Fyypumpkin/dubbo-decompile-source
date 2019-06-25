/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.thrift;

import java.util.HashMap;
import java.util.Map;

public enum ThriftType {
    BOOL,
    BYTE,
    I16,
    I32,
    I64,
    DOUBLE,
    STRING;
    
    private static final Map<Class<?>, ThriftType> types;

    public static ThriftType get(Class<?> key) {
        if (key != null) {
            return types.get(key);
        }
        throw new NullPointerException("key == null");
    }

    private static void put(Class<?> key, ThriftType value) {
        types.put(key, value);
    }

    static {
        types = new HashMap();
        ThriftType.put(Boolean.TYPE, BOOL);
        ThriftType.put(Boolean.class, BOOL);
        ThriftType.put(Byte.TYPE, BYTE);
        ThriftType.put(Byte.class, BYTE);
        ThriftType.put(Short.TYPE, I16);
    }
}

