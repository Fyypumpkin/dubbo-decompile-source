/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

public class Stack<E> {
    private int mSize = 0;
    private List<E> mElements = new ArrayList();

    public void push(E ele) {
        if (this.mElements.size() > this.mSize) {
            this.mElements.set(this.mSize, ele);
        } else {
            this.mElements.add(ele);
        }
        ++this.mSize;
    }

    public E pop() {
        if (this.mSize == 0) {
            throw new EmptyStackException();
        }
        return this.mElements.set(--this.mSize, null);
    }

    public E peek() {
        if (this.mSize == 0) {
            throw new EmptyStackException();
        }
        return this.mElements.get(this.mSize - 1);
    }

    public E get(int index) {
        if (index >= this.mSize) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.mSize);
        }
        return index < 0 ? this.mElements.get(index + this.mSize) : this.mElements.get(index);
    }

    public E set(int index, E value) {
        if (index >= this.mSize) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.mSize);
        }
        return this.mElements.set(index < 0 ? index + this.mSize : index, value);
    }

    public E remove(int index) {
        if (index >= this.mSize) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.mSize);
        }
        E ret = this.mElements.remove(index < 0 ? index + this.mSize : index);
        --this.mSize;
        return ret;
    }

    public int size() {
        return this.mSize;
    }

    public boolean isEmpty() {
        return this.mSize == 0;
    }

    public void clear() {
        this.mSize = 0;
        this.mElements.clear();
    }
}

