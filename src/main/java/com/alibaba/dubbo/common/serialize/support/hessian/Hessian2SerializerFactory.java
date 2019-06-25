/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.hessian;

import com.alibaba.com.caucho.hessian.io.SerializerFactory;

public class Hessian2SerializerFactory
extends SerializerFactory {
    public static final SerializerFactory SERIALIZER_FACTORY = new Hessian2SerializerFactory();

    private Hessian2SerializerFactory() {
    }

    @Override
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}

