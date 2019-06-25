/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.io;

import java.io.IOException;
import java.io.Writer;

public class UnsafeStringWriter
extends Writer {
    private StringBuilder mBuffer;

    public UnsafeStringWriter() {
        this.mBuffer = new StringBuilder();
        this.lock = this.mBuffer;
    }

    public UnsafeStringWriter(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative buffer size");
        }
        this.mBuffer = new StringBuilder();
        this.lock = this.mBuffer;
    }

    @Override
    public void write(int c) {
        this.mBuffer.append((char)c);
    }

    @Override
    public void write(char[] cs) throws IOException {
        this.mBuffer.append(cs, 0, cs.length);
    }

    @Override
    public void write(char[] cs, int off, int len) throws IOException {
        if (off < 0 || off > cs.length || len < 0 || off + len > cs.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len > 0) {
            this.mBuffer.append(cs, off, len);
        }
    }

    @Override
    public void write(String str) {
        this.mBuffer.append(str);
    }

    @Override
    public void write(String str, int off, int len) {
        this.mBuffer.append(str.substring(off, off + len));
    }

    @Override
    public Writer append(CharSequence csq) {
        if (csq == null) {
            this.write("null");
        } else {
            this.write(csq.toString());
        }
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) {
        CharSequence cs = csq == null ? "null" : csq;
        this.write(cs.subSequence(start, end).toString());
        return this;
    }

    @Override
    public Writer append(char c) {
        this.mBuffer.append(c);
        return this;
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    public String toString() {
        return this.mBuffer.toString();
    }
}

