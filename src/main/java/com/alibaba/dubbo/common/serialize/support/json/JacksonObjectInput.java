/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.alibaba.dubbo.common.serialize.support.json;

import com.alibaba.dubbo.common.json.Jackson;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JacksonObjectInput
implements ObjectInput {
    private static Logger logger = LoggerFactory.getLogger(JacksonObjectInput.class);
    private final ObjectMapper objectMapper = Jackson.getObjectMapper();
    private final Map<String, String> data;
    private static final String KEY_PREFIX = "$";
    private int index = 0;

    public JacksonObjectInput(InputStream inputstream) throws IOException {
        try {
            this.data = (Map)this.objectMapper.readValue(inputstream, Map.class);
        }
        catch (IOException e) {
            logger.error("parse inputstream error.", (Throwable)e);
            throw e;
        }
    }

    @Override
    public boolean readBool() throws IOException {
        try {
            return this.readObject(Boolean.class);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public byte readByte() throws IOException {
        try {
            return this.readObject(Byte.class);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public short readShort() throws IOException {
        try {
            return this.readObject(Short.class);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public int readInt() throws IOException {
        try {
            return this.readObject(Integer.class);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public long readLong() throws IOException {
        try {
            return this.readObject(Long.class);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public float readFloat() throws IOException {
        try {
            return this.readObject(Float.class).floatValue();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public double readDouble() throws IOException {
        try {
            return this.readObject(Double.class);
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
        return this.readUTF().getBytes();
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        try {
            return this.readObject(Object.class);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        String json = this.data.get(KEY_PREFIX + ++this.index);
        String dataType = this.data.get(KEY_PREFIX + this.index + "t");
        if (dataType != null) {
            Class<?> clazz = ReflectUtils.desc2class(dataType);
            if (cls.isAssignableFrom(clazz)) {
                cls = clazz;
            } else {
                throw new IllegalArgumentException("Class \"" + clazz + "\" is not inherited from \"" + cls + "\"");
            }
        }
        logger.debug("index:{}, value:{}", (Object)this.index, (Object)json);
        return (T)this.objectMapper.readValue(json, cls);
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return this.readObject(cls);
    }
}

