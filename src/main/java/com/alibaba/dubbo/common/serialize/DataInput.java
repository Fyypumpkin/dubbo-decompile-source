/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize;

import java.io.IOException;

public interface DataInput {
    public boolean readBool() throws IOException;

    public byte readByte() throws IOException;

    public short readShort() throws IOException;

    public int readInt() throws IOException;

    public long readLong() throws IOException;

    public float readFloat() throws IOException;

    public double readDouble() throws IOException;

    public String readUTF() throws IOException;

    public byte[] readBytes() throws IOException;
}

