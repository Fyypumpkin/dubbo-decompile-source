/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson.serializer.JSONSerializer
 *  com.alibaba.fastjson.serializer.SerializeWriter
 *  com.alibaba.fastjson.serializer.SerializerFeature
 */
package com.alibaba.dubbo.common.serialize.support.json;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

public class FastJsonObjectOutput
implements ObjectOutput {
    private final PrintWriter writer;

    public FastJsonObjectOutput(OutputStream out) {
        this(new OutputStreamWriter(out));
    }

    public FastJsonObjectOutput(Writer writer) {
        this.writer = new PrintWriter(writer);
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        this.writeObject(v);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        this.writeObject(v);
    }

    @Override
    public void writeShort(short v) throws IOException {
        this.writeObject(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.writeObject(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.writeObject(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.writeObject(Float.valueOf(v));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.writeObject(v);
    }

    @Override
    public void writeUTF(String v) throws IOException {
        this.writeObject(v);
    }

    @Override
    public void writeBytes(byte[] b) throws IOException {
        this.writer.println(new String(b));
    }

    @Override
    public void writeBytes(byte[] b, int off, int len) throws IOException {
        this.writer.println(new String(b, off, len));
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        SerializeWriter out = new SerializeWriter();
        JSONSerializer serializer = new JSONSerializer(out);
        serializer.config(SerializerFeature.WriteEnumUsingToString, true);
        serializer.write(obj);
        out.writeTo((Writer)this.writer);
        this.writer.println();
        this.writer.flush();
    }

    @Override
    public void flushBuffer() throws IOException {
        this.writer.flush();
    }
}

