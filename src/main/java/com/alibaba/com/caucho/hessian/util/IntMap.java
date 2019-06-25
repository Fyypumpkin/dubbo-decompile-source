/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.util;

public class IntMap {
    public static final int NULL = -559038737;
    private static final Object DELETED = new Object();
    private Object[] _keys = new Object[256];
    private int[] _values = new int[256];
    private int _size = 0;
    private int _mask = this._keys.length - 1;

    public void clear() {
        Object[] keys = this._keys;
        int[] values = this._values;
        for (int i = keys.length - 1; i >= 0; --i) {
            keys[i] = null;
            values[i] = 0;
        }
        this._size = 0;
    }

    public int size() {
        return this._size;
    }

    public int get(Object key) {
        int mask = this._mask;
        int hash = key.hashCode() % mask & mask;
        Object[] keys = this._keys;
        Object mapKey;
        while ((mapKey = keys[hash]) != null) {
            if (mapKey == key || mapKey.equals(key)) {
                return this._values[hash];
            }
            hash = (hash + 1) % mask;
        }
        return -559038737;
    }

    private void resize(int newSize) {
        Object[] newKeys = new Object[newSize];
        int[] newValues = new int[newSize];
        int mask = this._mask = newKeys.length - 1;
        Object[] keys = this._keys;
        int[] values = this._values;
        block0 : for (int i = keys.length - 1; i >= 0; --i) {
            Object key = keys[i];
            if (key == null || key == DELETED) continue;
            int hash = key.hashCode() % mask & mask;
            do {
                if (newKeys[hash] == null) {
                    newKeys[hash] = key;
                    newValues[hash] = values[i];
                    continue block0;
                }
                hash = (hash + 1) % mask;
            } while (true);
        }
        this._keys = newKeys;
        this._values = newValues;
    }

    public int put(Object key, int value) {
        int mask = this._mask;
        int hash = key.hashCode() % mask & mask;
        Object[] keys = this._keys;
        do {
            Object testKey;
            if ((testKey = keys[hash]) == null || testKey == DELETED) {
                keys[hash] = key;
                this._values[hash] = value;
                ++this._size;
                if (keys.length <= 4 * this._size) {
                    this.resize(4 * keys.length);
                }
                return -559038737;
            }
            if (key == testKey || key.equals(testKey)) break;
            hash = (hash + 1) % mask;
        } while (true);
        int old = this._values[hash];
        this._values[hash] = value;
        return old;
    }

    public int remove(Object key) {
        int mask = this._mask;
        int hash = key.hashCode() % mask & mask;
        Object mapKey;
        while ((mapKey = this._keys[hash]) != null) {
            if (mapKey == key) {
                this._keys[hash] = DELETED;
                --this._size;
                return this._values[hash];
            }
            hash = (hash + 1) % mask;
        }
        return -559038737;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("IntMap[");
        boolean isFirst = true;
        for (int i = 0; i <= this._mask; ++i) {
            if (this._keys[i] == null || this._keys[i] == DELETED) continue;
            if (!isFirst) {
                sbuf.append(", ");
            }
            isFirst = false;
            sbuf.append(this._keys[i]);
            sbuf.append(":");
            sbuf.append(this._values[i]);
        }
        sbuf.append("]");
        return sbuf.toString();
    }
}

