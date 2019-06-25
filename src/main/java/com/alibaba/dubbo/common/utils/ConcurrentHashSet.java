/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<E>
extends AbstractSet<E>
implements Set<E>,
Serializable {
    private static final long serialVersionUID = -8672117787651310382L;
    private static final Object PRESENT = new Object();
    private final ConcurrentHashMap<E, Object> map;

    public ConcurrentHashSet() {
        this.map = new ConcurrentHashMap();
    }

    public ConcurrentHashSet(int initialCapacity) {
        this.map = new ConcurrentHashMap(initialCapacity);
    }

    @Override
    public Iterator<E> iterator() {
        return ((ConcurrentHashMap.KeySetView)this.map.keySet()).iterator();
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.map.containsKey(o);
    }

    @Override
    public boolean add(E e) {
        return this.map.put(e, PRESENT) == null;
    }

    @Override
    public boolean remove(Object o) {
        return this.map.remove(o) == PRESENT;
    }

    @Override
    public void clear() {
        this.map.clear();
    }
}

