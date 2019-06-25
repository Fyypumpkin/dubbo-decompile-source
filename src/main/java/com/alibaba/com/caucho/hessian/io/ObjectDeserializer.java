/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractDeserializer;
import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import java.io.IOException;

public class ObjectDeserializer
extends AbstractDeserializer {
    private Class _cl;

    public ObjectDeserializer(Class cl) {
        this._cl = cl;
    }

    @Override
    public Class getType() {
        return this._cl;
    }

    @Override
    public Object readObject(AbstractHessianInput in) throws IOException {
        return in.readObject();
    }

    @Override
    public Object readObject(AbstractHessianInput in, String[] fieldNames) throws IOException {
        throw new UnsupportedOperationException(String.valueOf(this));
    }

    @Override
    public Object readList(AbstractHessianInput in, int length) throws IOException {
        throw new UnsupportedOperationException(String.valueOf(this));
    }

    @Override
    public Object readLengthList(AbstractHessianInput in, int length) throws IOException {
        throw new UnsupportedOperationException(String.valueOf(this));
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[" + this._cl + "]";
    }
}

