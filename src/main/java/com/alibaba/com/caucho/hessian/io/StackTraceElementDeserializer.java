/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.JavaDeserializer;

public class StackTraceElementDeserializer
extends JavaDeserializer {
    public StackTraceElementDeserializer() {
        super(StackTraceElement.class);
    }

    @Override
    protected Object instantiate() throws Exception {
        return new StackTraceElement("", "", "", 0);
    }
}

