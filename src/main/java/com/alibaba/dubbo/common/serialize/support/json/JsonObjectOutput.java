/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.json;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

public class JsonObjectOutput
implements ObjectOutput {
    private final PrintWriter writer;
    private final boolean writeClass;

    public JsonObjectOutput(OutputStream out) {
        this(new OutputStreamWriter(out), false);
    }

    public JsonObjectOutput(Writer writer) {
        this(writer, false);
    }

    public JsonObjectOutput(OutputStream out, boolean writeClass) {
        this(new OutputStreamWriter(out), writeClass);
    }

    public JsonObjectOutput(Writer writer, boolean writeClass) {
        this.writer = new PrintWriter(writer);
        this.writeClass = writeClass;
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
        JSON.json(obj, this.writer, this.writeClass);
        this.writer.println();
        this.writer.flush();
    }

    @Override
    public void flushBuffer() throws IOException {
        this.writer.flush();
    }
}

