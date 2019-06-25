/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractDeserializer;
import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import java.io.IOException;

public abstract class ValueDeserializer
extends AbstractDeserializer {
    @Override
    public Object readMap(AbstractHessianInput in) throws IOException {
        String initValue = null;
        while (!in.isEnd()) {
            String key = in.readString();
            if (key.equals("value")) {
                initValue = in.readString();
                continue;
            }
            in.readObject();
        }
        in.readMapEnd();
        return this.create(initValue);
    }

    @Override
    public Object readObject(AbstractHessianInput in, String[] fieldNames) throws IOException {
        String initValue = null;
        for (int i = 0; i < fieldNames.length; ++i) {
            if ("value".equals(fieldNames[i])) {
                initValue = in.readString();
                continue;
            }
            in.readObject();
        }
        return this.create(initValue);
    }

    abstract Object create(String var1) throws IOException;
}

