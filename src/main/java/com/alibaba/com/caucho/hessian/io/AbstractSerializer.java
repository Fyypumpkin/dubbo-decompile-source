/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianOutput;
import com.alibaba.com.caucho.hessian.io.Serializer;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class AbstractSerializer
implements Serializer {
    protected static final Logger log = Logger.getLogger(AbstractSerializer.class.getName());

    @Override
    public abstract void writeObject(Object var1, AbstractHessianOutput var2) throws IOException;
}

