/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.esotericsoftware.kryo.Kryo
 */
package com.alibaba.dubbo.common.serialize.support.kryo;

import com.alibaba.dubbo.common.serialize.support.kryo.KryoFactory;
import com.esotericsoftware.kryo.Kryo;

public class SingletonKryoFactory
extends KryoFactory {
    private Kryo instance;

    @Override
    public Kryo getKryo() {
        if (this.instance == null) {
            this.instance = this.createKryo();
        }
        return this.instance;
    }
}

