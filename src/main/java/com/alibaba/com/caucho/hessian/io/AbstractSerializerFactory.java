/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.Deserializer;
import com.alibaba.com.caucho.hessian.io.HessianProtocolException;
import com.alibaba.com.caucho.hessian.io.Serializer;

public abstract class AbstractSerializerFactory {
    public abstract Serializer getSerializer(Class var1) throws HessianProtocolException;

    public abstract Deserializer getDeserializer(Class var1) throws HessianProtocolException;
}

