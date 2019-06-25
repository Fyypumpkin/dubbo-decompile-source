/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.thrift.io;

import com.alibaba.dubbo.common.io.Bytes;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class RandomAccessByteArrayOutputStream
extends OutputStream {
    protected byte[] buffer;
    protected int count;

    public RandomAccessByteArrayOutputStream() {
        this(32);
    }

    public RandomAccessByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        this.buffer = new byte[size];
    }

    @Override
    public void write(int b) {
        int newcount = this.count + 1;
        if (newcount > this.buffer.length) {
            this.buffer = Bytes.copyOf(this.buffer, Math.max(this.buffer.length << 1, newcount));
        }
        this.buffer[this.count] = (byte)b;
        this.count = newcount;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        int newcount = this.count + len;
        if (newcount > this.buffer.length) {
            this.buffer = Bytes.copyOf(this.buffer, Math.max(this.buffer.length << 1, newcount));
        }
        System.arraycopy(b, off, this.buffer, this.count, len);
        this.count = newcount;
    }

    public int size() {
        return this.count;
    }

    public void setWriteIndex(int index) {
        this.count = index;
    }

    public void reset() {
        this.count = 0;
    }

    public byte[] toByteArray() {
        return Bytes.copyOf(this.buffer, this.count);
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(this.buffer, 0, this.count);
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(this.buffer, 0, this.count);
    }

    public String toString() {
        return new String(this.buffer, 0, this.count);
    }

    public String toString(String charset) throws UnsupportedEncodingException {
        return new String(this.buffer, 0, this.count, charset);
    }

    @Override
    public void close() throws IOException {
    }
}

