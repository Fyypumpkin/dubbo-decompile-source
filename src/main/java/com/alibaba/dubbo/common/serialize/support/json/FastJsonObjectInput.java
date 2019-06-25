/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson.JSON
 */
package com.alibaba.dubbo.common.serialize.support.json;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.utils.PojoUtils;
import com.alibaba.fastjson.JSON;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;

public class FastJsonObjectInput
implements ObjectInput {
    private final BufferedReader reader;

    public FastJsonObjectInput(InputStream in) {
        this(new InputStreamReader(in));
    }

    public FastJsonObjectInput(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    @Override
    public boolean readBool() throws IOException {
        try {
            return this.readObject(Boolean.TYPE);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public byte readByte() throws IOException {
        try {
            return this.readObject(Byte.TYPE);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public short readShort() throws IOException {
        try {
            return this.readObject(Short.TYPE);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public int readInt() throws IOException {
        try {
            return this.readObject(Integer.TYPE);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public long readLong() throws IOException {
        try {
            return this.readObject(Long.TYPE);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public float readFloat() throws IOException {
        try {
            return this.readObject(Float.TYPE).floatValue();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public double readDouble() throws IOException {
        try {
            return this.readObject(Double.TYPE);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String readUTF() throws IOException {
        try {
            return this.readObject(String.class);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public byte[] readBytes() throws IOException {
        return this.readLine().getBytes();
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        String json = this.readLine();
        return JSON.parse((String)json);
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        String json = this.readLine();
        return (T)JSON.parseObject((String)json, cls);
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        T value = this.readObject(cls);
        return (T)PojoUtils.realize(value, cls, type);
    }

    private String readLine() throws IOException, EOFException {
        String line = this.reader.readLine();
        if (line == null || line.trim().length() == 0) {
            throw new EOFException();
        }
        return line;
    }
}

