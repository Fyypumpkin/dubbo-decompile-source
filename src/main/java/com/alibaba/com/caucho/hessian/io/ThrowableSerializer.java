/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianOutput;
import com.alibaba.com.caucho.hessian.io.JavaSerializer;
import java.io.IOException;

public class ThrowableSerializer
extends JavaSerializer {
    public ThrowableSerializer(Class cl, ClassLoader loader) {
        super(cl, loader);
    }

    @Override
    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
        Throwable e = (Throwable)obj;
        e.getStackTrace();
        super.writeObject(obj, out);
    }
}

