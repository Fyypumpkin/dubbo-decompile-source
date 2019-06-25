/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.esotericsoftware.kryo.Kryo
 */
package com.alibaba.dubbo.common.serialize.support.kryo;

import com.alibaba.dubbo.common.serialize.support.kryo.KryoFactory;
import com.esotericsoftware.kryo.Kryo;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PooledKryoFactory
extends KryoFactory {
    private final Queue<Kryo> pool = new ConcurrentLinkedQueue<Kryo>();

    @Override
    public void returnKryo(Kryo kryo) {
        this.pool.offer(kryo);
    }

    @Override
    public void close() {
        this.pool.clear();
    }

    @Override
    public Kryo getKryo() {
        Kryo kryo = this.pool.poll();
        if (kryo == null) {
            kryo = this.createKryo();
        }
        return kryo;
    }
}

