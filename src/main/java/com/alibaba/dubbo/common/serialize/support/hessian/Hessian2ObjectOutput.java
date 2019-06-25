/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.hessian;

import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.support.hessian.Hessian2SerializerFactory;
import java.io.IOException;
import java.io.OutputStream;

public class Hessian2ObjectOutput
implements ObjectOutput {
    private final Hessian2Output mH2o;

    public Hessian2ObjectOutput(OutputStream os) {
        this.mH2o = new Hessian2Output(os);
        this.mH2o.setSerializerFactory(Hessian2SerializerFactory.SERIALIZER_FACTORY);
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        this.mH2o.writeBoolean(v);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        this.mH2o.writeInt(v);
    }

    @Override
    public void writeShort(short v) throws IOException {
        this.mH2o.writeInt(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.mH2o.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.mH2o.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.mH2o.writeDouble(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.mH2o.writeDouble(v);
    }

    @Override
    public void writeBytes(byte[] b) throws IOException {
        this.mH2o.writeBytes(b);
    }

    @Override
    public void writeBytes(byte[] b, int off, int len) throws IOException {
        this.mH2o.writeBytes(b, off, len);
    }

    @Override
    public void writeUTF(String v) throws IOException {
        this.mH2o.writeString(v);
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        this.mH2o.writeObject(obj);
    }

    @Override
    public void flushBuffer() throws IOException {
        this.mH2o.flushBuffer();
    }
}

