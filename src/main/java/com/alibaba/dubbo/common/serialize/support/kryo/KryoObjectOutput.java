/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.esotericsoftware.kryo.Kryo
 *  com.esotericsoftware.kryo.io.Output
 */
package com.alibaba.dubbo.common.serialize.support.kryo;

import com.alibaba.dubbo.common.serialize.Cleanable;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.support.kryo.KryoFactory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import java.io.IOException;
import java.io.OutputStream;

public class KryoObjectOutput
implements ObjectOutput,
Cleanable {
    private Kryo kryo = KryoFactory.getDefaultFactory().getKryo();
    private Output output;

    public KryoObjectOutput(OutputStream outputStream) {
        this.output = new Output(outputStream);
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        this.output.writeBoolean(v);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        this.output.writeByte(v);
    }

    @Override
    public void writeShort(short v) throws IOException {
        this.output.writeShort((int)v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.output.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.output.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.output.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.output.writeDouble(v);
    }

    @Override
    public void writeBytes(byte[] v) throws IOException {
        if (v == null) {
            this.output.writeInt(-1);
        } else {
            this.writeBytes(v, 0, v.length);
        }
    }

    @Override
    public void writeBytes(byte[] v, int off, int len) throws IOException {
        if (v == null) {
            this.output.writeInt(-1);
        } else {
            this.output.writeInt(len);
            this.output.write(v, off, len);
        }
    }

    @Override
    public void writeUTF(String v) throws IOException {
        this.output.writeString(v);
    }

    @Override
    public void writeObject(Object v) throws IOException {
        this.kryo.writeClassAndObject(this.output, v);
    }

    @Override
    public void flushBuffer() throws IOException {
        this.output.flush();
    }

    @Override
    public void cleanup() {
        KryoFactory.getDefaultFactory().returnKryo(this.kryo);
        this.kryo = null;
    }
}

