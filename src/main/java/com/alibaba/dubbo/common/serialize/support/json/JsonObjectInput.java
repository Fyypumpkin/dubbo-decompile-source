/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.json;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.ParseException;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.utils.PojoUtils;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;

public class JsonObjectInput
implements ObjectInput {
    private final BufferedReader reader;

    public JsonObjectInput(InputStream in) {
        this(new InputStreamReader(in));
    }

    public JsonObjectInput(Reader reader) {
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
        try {
            String json = this.readLine();
            if (json.startsWith("{")) {
                return JSON.parse(json, Map.class);
            }
            json = "{\"value\":" + json + "}";
            Map map = JSON.parse(json, Map.class);
            return map.get("value");
        }
        catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        Object value = this.readObject();
        return (T)PojoUtils.realize(value, cls);
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        Object value = this.readObject();
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

