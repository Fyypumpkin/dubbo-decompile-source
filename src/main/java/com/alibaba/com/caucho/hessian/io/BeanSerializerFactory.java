/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.BeanDeserializer;
import com.alibaba.com.caucho.hessian.io.BeanSerializer;
import com.alibaba.com.caucho.hessian.io.Deserializer;
import com.alibaba.com.caucho.hessian.io.Serializer;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;

public class BeanSerializerFactory
extends SerializerFactory {
    @Override
    protected Serializer getDefaultSerializer(Class cl) {
        return new BeanSerializer(cl, this.getClassLoader());
    }

    @Override
    protected Deserializer getDefaultDeserializer(Class cl) {
        return new BeanDeserializer(cl);
    }
}

