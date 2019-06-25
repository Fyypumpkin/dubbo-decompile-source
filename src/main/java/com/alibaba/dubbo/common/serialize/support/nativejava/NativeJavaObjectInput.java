/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.nativejava;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.utils.Assert;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;

public class NativeJavaObjectInput
implements ObjectInput {
    private final ObjectInputStream inputStream;

    public NativeJavaObjectInput(InputStream is) throws IOException {
        this(new ObjectInputStream(is));
    }

    protected NativeJavaObjectInput(ObjectInputStream is) {
        Assert.notNull(is, "input == null");
        this.inputStream = is;
    }

    protected ObjectInputStream getObjectInputStream() {
        return this.inputStream;
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        return this.inputStream.readObject();
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        return (T)this.readObject();
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return (T)this.readObject();
    }

    @Override
    public boolean readBool() throws IOException {
        return this.inputStream.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return this.inputStream.readByte();
    }

    @Override
    public short readShort() throws IOException {
        return this.inputStream.readShort();
    }

    @Override
    public int readInt() throws IOException {
        return this.inputStream.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return this.inputStream.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return this.inputStream.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return this.inputStream.readDouble();
    }

    @Override
    public String readUTF() throws IOException {
        return this.inputStream.readUTF();
    }

    @Override
    public byte[] readBytes() throws IOException {
        int len = this.inputStream.readInt();
        if (len < 0) {
            return null;
        }
        if (len == 0) {
            return new byte[0];
        }
        byte[] result = new byte[len];
        this.inputStream.readFully(result);
        return result;
    }
}

