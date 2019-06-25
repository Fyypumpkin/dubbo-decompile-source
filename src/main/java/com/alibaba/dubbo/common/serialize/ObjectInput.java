/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize;

import com.alibaba.dubbo.common.serialize.DataInput;
import java.io.IOException;
import java.lang.reflect.Type;

public interface ObjectInput
extends DataInput {
    public Object readObject() throws IOException, ClassNotFoundException;

    public <T> T readObject(Class<T> var1) throws IOException, ClassNotFoundException;

    public <T> T readObject(Class<T> var1, Type var2) throws IOException, ClassNotFoundException;
}

