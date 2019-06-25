/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  de.ruedigermoeller.serialization.FSTObjectInput
 */
package com.alibaba.dubbo.common.serialize.support.fst;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.support.fst.FstFactory;
import de.ruedigermoeller.serialization.FSTObjectInput;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public class FstObjectInput
implements ObjectInput {
    private FSTObjectInput input;

    public FstObjectInput(InputStream inputStream) {
        this.input = FstFactory.getDefaultFactory().getObjectInput(inputStream);
    }

    @Override
    public boolean readBool() throws IOException {
        return this.input.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return this.input.readByte();
    }

    @Override
    public short readShort() throws IOException {
        return this.input.readShort();
    }

    @Override
    public int readInt() throws IOException {
        return this.input.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return this.input.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return this.input.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return this.input.readDouble();
    }

    @Override
    public byte[] readBytes() throws IOException {
        int len = this.input.readInt();
        if (len < 0) {
            return null;
        }
        if (len == 0) {
            return new byte[0];
        }
        byte[] b = new byte[len];
        this.input.readFully(b);
        return b;
    }

    @Override
    public String readUTF() throws IOException {
        return this.input.readUTF();
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        return this.input.readObject();
    }

    @Override
    public <T> T readObject(Class<T> clazz) throws IOException, ClassNotFoundException {
        return (T)this.readObject();
    }

    @Override
    public <T> T readObject(Class<T> clazz, Type type) throws IOException, ClassNotFoundException {
        return (T)this.readObject();
    }
}

