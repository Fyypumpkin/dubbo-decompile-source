/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize;

import com.alibaba.dubbo.common.serialize.DataOutput;
import java.io.IOException;

public interface ObjectOutput
extends DataOutput {
    public void writeObject(Object var1) throws IOException;
}

