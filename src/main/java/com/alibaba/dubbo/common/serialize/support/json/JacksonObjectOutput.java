/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 */
package com.alibaba.dubbo.common.serialize.support.json;

import com.alibaba.dubbo.common.json.Jackson;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class JacksonObjectOutput
implements ObjectOutput {
    private final ObjectMapper objectMapper = Jackson.getObjectMapper();
    private final Map<String, Object> data;
    private static final String KEY_PREFIX = "$";
    private int index = 0;
    private final PrintWriter writer;

    public JacksonObjectOutput(OutputStream out) {
        this(new OutputStreamWriter(out));
    }

    public JacksonObjectOutput(Writer writer) {
        this.writer = new PrintWriter(writer);
        this.data = new HashMap<String, Object>();
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        this.writeObject0(v);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        this.writeObject0(v);
    }

    @Override
    public void writeShort(short v) throws IOException {
        this.writeObject0(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.writeObject0(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.writeObject0(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.writeObject0(Float.valueOf(v));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.writeObject0(v);
    }

    @Override
    public void writeUTF(String v) throws IOException {
        this.writeObject0(v);
    }

    @Override
    public void writeBytes(byte[] b) throws IOException {
        this.writeObject0(new String(b));
    }

    @Override
    public void writeBytes(byte[] b, int off, int len) throws IOException {
        this.writeObject0(new String(b, off, len));
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
            this.writeObject0(obj);
            return;
        }
        this.writeObject0(obj);
        Class<?> c = obj.getClass();
        String desc = ReflectUtils.getDesc(c);
        this.data.put(KEY_PREFIX + this.index + "t", desc);
    }

    private void writeObject0(Object obj) throws IOException {
        this.data.put(KEY_PREFIX + ++this.index, this.objectMapper.writeValueAsString(obj));
    }

    @Override
    public void flushBuffer() throws IOException {
        this.objectMapper.writeValue((Writer)this.writer, this.data);
        this.writer.println();
        this.writer.flush();
    }
}

