/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractDeserializer;
import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import com.alibaba.com.caucho.hessian.io.IOExceptionWrapper;
import java.io.IOException;
import java.lang.reflect.Constructor;

public class SqlDateDeserializer
extends AbstractDeserializer {
    private Class _cl;
    private Constructor _constructor;

    public SqlDateDeserializer(Class cl) throws NoSuchMethodException {
        this._cl = cl;
        this._constructor = cl.getConstructor(Long.TYPE);
    }

    @Override
    public Class getType() {
        return this._cl;
    }

    @Override
    public Object readMap(AbstractHessianInput in) throws IOException {
        int ref = in.addRef(null);
        long initValue = Long.MIN_VALUE;
        while (!in.isEnd()) {
            String key = in.readString();
            if (key.equals("value")) {
                initValue = in.readUTCDate();
                continue;
            }
            in.readString();
        }
        in.readMapEnd();
        Object value = this.create(initValue);
        in.setRef(ref, value);
        return value;
    }

    @Override
    public Object readObject(AbstractHessianInput in, String[] fieldNames) throws IOException {
        int ref = in.addRef(null);
        long initValue = Long.MIN_VALUE;
        for (int i = 0; i < fieldNames.length; ++i) {
            String key = fieldNames[i];
            if (key.equals("value")) {
                initValue = in.readUTCDate();
                continue;
            }
            in.readObject();
        }
        Object value = this.create(initValue);
        in.setRef(ref, value);
        return value;
    }

    private Object create(long initValue) throws IOException {
        if (initValue == Long.MIN_VALUE) {
            throw new IOException(this._cl.getName() + " expects name.");
        }
        try {
            return this._constructor.newInstance(new Long(initValue));
        }
        catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }
}

