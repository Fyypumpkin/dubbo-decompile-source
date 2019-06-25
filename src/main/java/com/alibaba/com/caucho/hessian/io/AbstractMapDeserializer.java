/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractDeserializer;
import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import com.alibaba.com.caucho.hessian.io.HessianProtocolException;
import java.io.IOException;
import java.util.HashMap;

public class AbstractMapDeserializer
extends AbstractDeserializer {
    @Override
    public Class getType() {
        return HashMap.class;
    }

    @Override
    public Object readObject(AbstractHessianInput in) throws IOException {
        Object obj = in.readObject();
        if (obj != null) {
            throw this.error("expected map/object at " + obj.getClass().getName() + " (" + obj + ")");
        }
        throw this.error("expected map/object at null");
    }
}

