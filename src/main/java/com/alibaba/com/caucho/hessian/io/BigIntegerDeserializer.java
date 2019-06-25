/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.JavaDeserializer;
import java.math.BigInteger;

public class BigIntegerDeserializer
extends JavaDeserializer {
    public BigIntegerDeserializer() {
        super(BigInteger.class);
    }

    @Override
    protected Object instantiate() throws Exception {
        return new BigInteger("0");
    }
}

