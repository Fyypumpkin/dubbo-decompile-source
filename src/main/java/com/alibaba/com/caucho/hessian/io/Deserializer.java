/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import java.io.IOException;

public interface Deserializer {
    public Class getType();

    public Object readObject(AbstractHessianInput var1) throws IOException;

    public Object readList(AbstractHessianInput var1, int var2) throws IOException;

    public Object readList(AbstractHessianInput var1, int var2, Class<?> var3) throws IOException;

    public Object readLengthList(AbstractHessianInput var1, int var2) throws IOException;

    public Object readLengthList(AbstractHessianInput var1, int var2, Class<?> var3) throws IOException;

    public Object readMap(AbstractHessianInput var1) throws IOException;

    public Object readMap(AbstractHessianInput var1, Class<?> var2, Class<?> var3) throws IOException;

    public Object readObject(AbstractHessianInput var1, String[] var2) throws IOException;
}

