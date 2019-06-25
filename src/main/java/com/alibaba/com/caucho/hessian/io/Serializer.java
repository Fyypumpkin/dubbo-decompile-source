/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianOutput;
import java.io.IOException;

public interface Serializer {
    public void writeObject(Object var1, AbstractHessianOutput var2) throws IOException;
}

