/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.nativejava;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.utils.Assert;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class NativeJavaObjectOutput
implements ObjectOutput {
    private final ObjectOutputStream outputStream;

    public NativeJavaObjectOutput(OutputStream os) throws IOException {
        this(new ObjectOutputStream(os));
    }

    protected NativeJavaObjectOutput(ObjectOutputStream out) {
        Assert.notNull(out, "output == null");
        this.outputStream = out;
    }

    protected ObjectOutputStream getObjectOutputStream() {
        return this.outputStream;
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        this.outputStream.writeObject(obj);
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        this.outputStream.writeBoolean(v);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        this.outputStream.writeByte(v);
    }

    @Override
    public void writeShort(short v) throws IOException {
        this.outputStream.writeShort(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.outputStream.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.outputStream.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.outputStream.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.outputStream.writeDouble(v);
    }

    @Override
    public void writeUTF(String v) throws IOException {
        this.outputStream.writeUTF(v);
    }

    @Override
    public void writeBytes(byte[] v) throws IOException {
        if (v == null) {
            this.outputStream.writeInt(-1);
        } else {
            this.writeBytes(v, 0, v.length);
        }
    }

    @Override
    public void writeBytes(byte[] v, int off, int len) throws IOException {
        if (v == null) {
            this.outputStream.writeInt(-1);
        } else {
            this.outputStream.writeInt(len);
            this.outputStream.write(v, off, len);
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        this.outputStream.flush();
    }
}

