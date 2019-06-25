/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractDeserializer;
import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamDeserializer
extends AbstractDeserializer {
    @Override
    public Object readObject(AbstractHessianInput in) throws IOException {
        return in.readInputStream();
    }
}

