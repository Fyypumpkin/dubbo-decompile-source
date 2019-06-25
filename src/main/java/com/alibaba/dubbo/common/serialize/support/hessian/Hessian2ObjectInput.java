/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.hessian;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.support.hessian.Hessian2SerializerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public class Hessian2ObjectInput
implements ObjectInput {
    private final Hessian2Input mH2i;

    public Hessian2ObjectInput(InputStream is) {
        this.mH2i = new Hessian2Input(is);
        this.mH2i.setSerializerFactory(Hessian2SerializerFactory.SERIALIZER_FACTORY);
    }

    @Override
    public boolean readBool() throws IOException {
        return this.mH2i.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return (byte)this.mH2i.readInt();
    }

    @Override
    public short readShort() throws IOException {
        return (short)this.mH2i.readInt();
    }

    @Override
    public int readInt() throws IOException {
        return this.mH2i.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return this.mH2i.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return (float)this.mH2i.readDouble();
    }

    @Override
    public double readDouble() throws IOException {
        return this.mH2i.readDouble();
    }

    @Override
    public byte[] readBytes() throws IOException {
        return this.mH2i.readBytes();
    }

    @Override
    public String readUTF() throws IOException {
        return this.mH2i.readString();
    }

    @Override
    public Object readObject() throws IOException {
        return this.mH2i.readObject();
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        return (T)this.mH2i.readObject(cls);
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return this.readObject(cls);
    }
}

