/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractDeserializer;
import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import com.alibaba.com.caucho.hessian.io.HessianProtocolException;
import java.io.IOException;

public class AbstractListDeserializer
extends AbstractDeserializer {
    @Override
    public Object readObject(AbstractHessianInput in) throws IOException {
        Object obj = in.readObject();
        if (obj != null) {
            throw this.error("expected list at " + obj.getClass().getName() + " (" + obj + ")");
        }
        throw this.error("expected list at null");
    }
}

