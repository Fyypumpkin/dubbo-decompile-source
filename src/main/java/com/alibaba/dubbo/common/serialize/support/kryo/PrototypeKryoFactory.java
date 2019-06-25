/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.esotericsoftware.kryo.Kryo
 */
package com.alibaba.dubbo.common.serialize.support.kryo;

import com.alibaba.dubbo.common.serialize.support.kryo.KryoFactory;
import com.esotericsoftware.kryo.Kryo;

public class PrototypeKryoFactory
extends KryoFactory {
    @Override
    public Kryo getKryo() {
        return this.createKryo();
    }
}

