/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicPositiveInteger
extends Number {
    private static final long serialVersionUID = -3038533876489105940L;
    private final AtomicInteger i;

    public AtomicPositiveInteger() {
        this.i = new AtomicInteger();
    }

    public AtomicPositiveInteger(int initialValue) {
        this.i = new AtomicInteger(initialValue);
    }

    public final int getAndIncrement() {
        int next;
        int current;
        while (!this.i.compareAndSet(current, next = (current = this.i.get()) >= Integer.MAX_VALUE ? 0 : current + 1)) {
        }
        return current;
    }

    public final int getAndDecrement() {
        int next;
        int current;
        while (!this.i.compareAndSet(current, next = (current = this.i.get()) <= 0 ? Integer.MAX_VALUE : current - 1)) {
        }
        return current;
    }

    public final int incrementAndGet() {
        int next;
        int current;
        while (!this.i.compareAndSet(current, next = (current = this.i.get()) >= Integer.MAX_VALUE ? 0 : current + 1)) {
        }
        return next;
    }

    public final int decrementAndGet() {
        int next;
        int current;
        while (!this.i.compareAndSet(current, next = (current = this.i.get()) <= 0 ? Integer.MAX_VALUE : current - 1)) {
        }
        return next;
    }

    public final int get() {
        return this.i.get();
    }

    public final void set(int newValue) {
        if (newValue < 0) {
            throw new IllegalArgumentException("new value " + newValue + " < 0");
        }
        this.i.set(newValue);
    }

    public final int getAndSet(int newValue) {
        if (newValue < 0) {
            throw new IllegalArgumentException("new value " + newValue + " < 0");
        }
        return this.i.getAndSet(newValue);
    }

    public final int getAndAdd(int delta) {
        int current;
        int next;
        if (delta < 0) {
            throw new IllegalArgumentException("delta " + delta + " < 0");
        }
        while (!this.i.compareAndSet(current, next = (current = this.i.get()) >= Integer.MAX_VALUE - delta + 1 ? delta - 1 : current + delta)) {
        }
        return current;
    }

    public final int addAndGet(int delta) {
        int current;
        int next;
        if (delta < 0) {
            throw new IllegalArgumentException("delta " + delta + " < 0");
        }
        while (!this.i.compareAndSet(current, next = (current = this.i.get()) >= Integer.MAX_VALUE - delta + 1 ? delta - 1 : current + delta)) {
        }
        return next;
    }

    public final boolean compareAndSet(int expect, int update) {
        if (update < 0) {
            throw new IllegalArgumentException("update value " + update + " < 0");
        }
        return this.i.compareAndSet(expect, update);
    }

    public final boolean weakCompareAndSet(int expect, int update) {
        if (update < 0) {
            throw new IllegalArgumentException("update value " + update + " < 0");
        }
        return this.i.weakCompareAndSet(expect, update);
    }

    @Override
    public byte byteValue() {
        return this.i.byteValue();
    }

    @Override
    public short shortValue() {
        return this.i.shortValue();
    }

    @Override
    public int intValue() {
        return this.i.intValue();
    }

    @Override
    public long longValue() {
        return this.i.longValue();
    }

    @Override
    public float floatValue() {
        return this.i.floatValue();
    }

    @Override
    public double doubleValue() {
        return this.i.doubleValue();
    }

    public String toString() {
        return this.i.toString();
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.i == null ? 0 : this.i.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        AtomicPositiveInteger other = (AtomicPositiveInteger)obj;
        return !(this.i == null ? other.i != null : !this.i.equals(other.i));
    }
}

