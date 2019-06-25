/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractDeserializer;
import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import com.alibaba.com.caucho.hessian.io.IOExceptionWrapper;
import java.io.IOException;
import java.lang.reflect.Constructor;

public class StringValueDeserializer
extends AbstractDeserializer {
    private Class _cl;
    private Constructor _constructor;

    public StringValueDeserializer(Class cl) {
        try {
            this._cl = cl;
            this._constructor = cl.getConstructor(String.class);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class getType() {
        return this._cl;
    }

    @Override
    public Object readMap(AbstractHessianInput in) throws IOException {
        String value = null;
        while (!in.isEnd()) {
            String key = in.readString();
            if (key.equals("value")) {
                value = in.readString();
                continue;
            }
            in.readObject();
        }
        in.readMapEnd();
        Object object = this.create(value);
        in.addRef(object);
        return object;
    }

    @Override
    public Object readObject(AbstractHessianInput in, String[] fieldNames) throws IOException {
        String value = null;
        for (int i = 0; i < fieldNames.length; ++i) {
            if ("value".equals(fieldNames[i])) {
                value = in.readString();
                continue;
            }
            in.readObject();
        }
        Object object = this.create(value);
        in.addRef(object);
        return object;
    }

    private Object create(String value) throws IOException {
        if (value == null) {
            throw new IOException(this._cl.getName() + " expects name.");
        }
        try {
            return this._constructor.newInstance(value);
        }
        catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }
}

