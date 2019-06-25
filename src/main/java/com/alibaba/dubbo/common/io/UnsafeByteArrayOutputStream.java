/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.io;

import com.alibaba.dubbo.common.io.Bytes;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class UnsafeByteArrayOutputStream
extends OutputStream {
    protected byte[] mBuffer;
    protected int mCount;

    public UnsafeByteArrayOutputStream() {
        this(32);
    }

    public UnsafeByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        this.mBuffer = new byte[size];
    }

    @Override
    public void write(int b) {
        int newcount = this.mCount + 1;
        if (newcount > this.mBuffer.length) {
            this.mBuffer = Bytes.copyOf(this.mBuffer, Math.max(this.mBuffer.length << 1, newcount));
        }
        this.mBuffer[this.mCount] = (byte)b;
        this.mCount = newcount;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        int newcount = this.mCount + len;
        if (newcount > this.mBuffer.length) {
            this.mBuffer = Bytes.copyOf(this.mBuffer, Math.max(this.mBuffer.length << 1, newcount));
        }
        System.arraycopy(b, off, this.mBuffer, this.mCount, len);
        this.mCount = newcount;
    }

    public int size() {
        return this.mCount;
    }

    public void reset() {
        this.mCount = 0;
    }

    public byte[] toByteArray() {
        return Bytes.copyOf(this.mBuffer, this.mCount);
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(this.mBuffer, 0, this.mCount);
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(this.mBuffer, 0, this.mCount);
    }

    public String toString() {
        return new String(this.mBuffer, 0, this.mCount);
    }

    public String toString(String charset) throws UnsupportedEncodingException {
        return new String(this.mBuffer, 0, this.mCount, charset);
    }

    @Override
    public void close() throws IOException {
    }
}

