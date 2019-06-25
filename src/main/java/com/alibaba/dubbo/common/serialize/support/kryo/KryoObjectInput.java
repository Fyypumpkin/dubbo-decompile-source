/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.esotericsoftware.kryo.Kryo
 *  com.esotericsoftware.kryo.KryoException
 *  com.esotericsoftware.kryo.io.Input
 */
package com.alibaba.dubbo.common.serialize.support.kryo;

import com.alibaba.dubbo.common.serialize.Cleanable;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.support.kryo.KryoFactory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public class KryoObjectInput
implements ObjectInput,
Cleanable {
    private Kryo kryo = KryoFactory.getDefaultFactory().getKryo();
    private Input input;

    public KryoObjectInput(InputStream inputStream) {
        this.input = new Input(inputStream);
    }

    @Override
    public boolean readBool() throws IOException {
        try {
            return this.input.readBoolean();
        }
        catch (KryoException e) {
            throw new IOException((Throwable)e);
        }
    }

    @Override
    public byte readByte() throws IOException {
        try {
            return this.input.readByte();
        }
        catch (KryoException e) {
            throw new IOException((Throwable)e);
        }
    }

    @Override
    public short readShort() throws IOException {
        try {
            return this.input.readShort();
        }
        catch (KryoException e) {
            throw new IOException((Throwable)e);
        }
    }

    @Override
    public int readInt() throws IOException {
        try {
            return this.input.readInt();
        }
        catch (KryoException e) {
            throw new IOException((Throwable)e);
        }
    }

    @Override
    public long readLong() throws IOException {
        try {
            return this.input.readLong();
        }
        catch (KryoException e) {
            throw new IOException((Throwable)e);
        }
    }

    @Override
    public float readFloat() throws IOException {
        try {
            return this.input.readFloat();
        }
        catch (KryoException e) {
            throw new IOException((Throwable)e);
        }
    }

    @Override
    public double readDouble() throws IOException {
        try {
            return this.input.readDouble();
        }
        catch (KryoException e) {
            throw new IOException((Throwable)e);
        }
    }

    @Override
    public byte[] readBytes() throws IOException {
        try {
            int len = this.input.readInt();
            if (len < 0) {
                return null;
            }
            if (len == 0) {
                return new byte[0];
            }
            return this.input.readBytes(len);
        }
        catch (KryoException e) {
            throw new IOException((Throwable)e);
        }
    }

    @Override
    public String readUTF() throws IOException {
        try {
            return this.input.readString();
        }
        catch (KryoException e) {
            throw new IOException((Throwable)e);
        }
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        try {
            return this.kryo.readClassAndObject(this.input);
        }
        catch (KryoException e) {
            throw new IOException((Throwable)e);
        }
    }

    @Override
    public <T> T readObject(Class<T> clazz) throws IOException, ClassNotFoundException {
        return (T)this.readObject();
    }

    @Override
    public <T> T readObject(Class<T> clazz, Type type) throws IOException, ClassNotFoundException {
        return this.readObject(clazz);
    }

    @Override
    public void cleanup() {
        KryoFactory.getDefaultFactory().returnKryo(this.kryo);
        this.kryo = null;
    }
}

