/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.buffer;

import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import java.io.IOException;
import java.io.InputStream;

public class ChannelBufferInputStream
extends InputStream {
    private final ChannelBuffer buffer;
    private final int startIndex;
    private final int endIndex;

    public ChannelBufferInputStream(ChannelBuffer buffer) {
        this(buffer, buffer.readableBytes());
    }

    public ChannelBufferInputStream(ChannelBuffer buffer, int length) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        if (length > buffer.readableBytes()) {
            throw new IndexOutOfBoundsException();
        }
        this.buffer = buffer;
        this.startIndex = buffer.readerIndex();
        this.endIndex = this.startIndex + length;
        buffer.markReaderIndex();
    }

    public int readBytes() {
        return this.buffer.readerIndex() - this.startIndex;
    }

    @Override
    public int available() throws IOException {
        return this.endIndex - this.buffer.readerIndex();
    }

    @Override
    public void mark(int readlimit) {
        this.buffer.markReaderIndex();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        if (!this.buffer.readable()) {
            return -1;
        }
        return this.buffer.readByte() & 255;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int available = this.available();
        if (available == 0) {
            return -1;
        }
        len = Math.min(available, len);
        this.buffer.readBytes(b, off, len);
        return len;
    }

    @Override
    public void reset() throws IOException {
        this.buffer.resetReaderIndex();
    }

    @Override
    public long skip(long n) throws IOException {
        if (n > Integer.MAX_VALUE) {
            return this.skipBytes(Integer.MAX_VALUE);
        }
        return this.skipBytes((int)n);
    }

    private int skipBytes(int n) throws IOException {
        int nBytes = Math.min(this.available(), n);
        this.buffer.skipBytes(nBytes);
        return nBytes;
    }
}

