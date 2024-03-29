/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractSerializerFactory;
import com.alibaba.com.caucho.hessian.io.Deserializer;
import com.alibaba.com.caucho.hessian.io.HessianProtocolException;
import com.alibaba.com.caucho.hessian.io.Serializer;
import java.util.HashMap;

public class ExtSerializerFactory
extends AbstractSerializerFactory {
    private HashMap _serializerMap = new HashMap();
    private HashMap _deserializerMap = new HashMap();

    public void addSerializer(Class cl, Serializer serializer) {
        this._serializerMap.put(cl, serializer);
    }

    public void addDeserializer(Class cl, Deserializer deserializer) {
        this._deserializerMap.put(cl, deserializer);
    }

    @Override
    public Serializer getSerializer(Class cl) throws HessianProtocolException {
        return (Serializer)this._serializerMap.get(cl);
    }

    @Override
    public Deserializer getDeserializer(Class cl) throws HessianProtocolException {
        return (Deserializer)this._deserializerMap.get(cl);
    }
}

