/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize;

import java.io.IOException;

public interface DataOutput {
    public void writeBool(boolean var1) throws IOException;

    public void writeByte(byte var1) throws IOException;

    public void writeShort(short var1) throws IOException;

    public void writeInt(int var1) throws IOException;

    public void writeLong(long var1) throws IOException;

    public void writeFloat(float var1) throws IOException;

    public void writeDouble(double var1) throws IOException;

    public void writeUTF(String var1) throws IOException;

    public void writeBytes(byte[] var1) throws IOException;

    public void writeBytes(byte[] var1, int var2, int var3) throws IOException;

    public void flushBuffer() throws IOException;
}

