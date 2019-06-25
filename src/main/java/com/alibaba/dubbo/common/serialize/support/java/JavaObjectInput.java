/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.java;

import com.alibaba.dubbo.common.serialize.support.java.CompactedObjectInputStream;
import com.alibaba.dubbo.common.serialize.support.nativejava.NativeJavaObjectInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;

public class JavaObjectInput
extends NativeJavaObjectInput {
    public static final int MAX_BYTE_ARRAY_LENGTH = 8388608;

    public JavaObjectInput(InputStream is) throws IOException {
        super(new ObjectInputStream(is));
    }

    public JavaObjectInput(InputStream is, boolean compacted) throws IOException {
        super(compacted ? new CompactedObjectInputStream(is) : new ObjectInputStream(is));
    }

    @Override
    public byte[] readBytes() throws IOException {
        int len = this.getObjectInputStream().readInt();
        if (len < 0) {
            return null;
        }
        if (len == 0) {
            return new byte[0];
        }
        if (len > 8388608) {
            throw new IOException("Byte array length too large. " + len);
        }
        byte[] b = new byte[len];
        this.getObjectInputStream().readFully(b);
        return b;
    }

    @Override
    public String readUTF() throws IOException {
        int len = this.getObjectInputStream().readInt();
        if (len < 0) {
            return null;
        }
        return this.getObjectInputStream().readUTF();
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        byte b = this.getObjectInputStream().readByte();
        if (b == 0) {
            return null;
        }
        return this.getObjectInputStream().readObject();
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        return (T)this.readObject();
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return (T)this.readObject();
    }
}

