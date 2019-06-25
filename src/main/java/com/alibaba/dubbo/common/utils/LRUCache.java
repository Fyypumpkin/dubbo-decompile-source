/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LRUCache<K, V>
extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = -5167631809472116969L;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_MAX_CAPACITY = 1000;
    private volatile int maxCapacity;
    private final Lock lock = new ReentrantLock();

    public LRUCache() {
        this(1000);
    }

    public LRUCache(int maxCapacity) {
        super(16, 0.75f, true);
        this.maxCapacity = maxCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.size() > this.maxCapacity;
    }

    @Override
    public boolean containsKey(Object key) {
        try {
            this.lock.lock();
            boolean bl = super.containsKey(key);
            return bl;
        }
        finally {
            this.lock.unlock();
        }
    }

    @Override
    public V get(Object key) {
        try {
            this.lock.lock();
            Object v = super.get(key);
            return v;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public V put(K key, V value) {
        try {
            this.lock.lock();
            V v = super.put(key, value);
            return v;
        }
        finally {
            this.lock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        try {
            this.lock.lock();
            Object v = super.remove(key);
            return v;
        }
        finally {
            this.lock.unlock();
        }
    }

    @Override
    public int size() {
        try {
            this.lock.lock();
            int n = super.size();
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }

    @Override
    public void clear() {
        try {
            this.lock.lock();
            super.clear();
        }
        finally {
            this.lock.unlock();
        }
    }

    public int getMaxCapacity() {
        return this.maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}

