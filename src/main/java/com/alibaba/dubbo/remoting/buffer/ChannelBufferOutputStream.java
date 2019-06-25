/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.buffer;

import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import java.io.IOException;
import java.io.OutputStream;

public class ChannelBufferOutputStream
extends OutputStream {
    private final ChannelBuffer buffer;
    private final int startIndex;

    public ChannelBufferOutputStream(ChannelBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        this.buffer = buffer;
        this.startIndex = buffer.writerIndex();
    }

    public int writtenBytes() {
        return this.buffer.writerIndex() - this.startIndex;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        this.buffer.writeBytes(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.buffer.writeBytes(b);
    }

    @Override
    public void write(int b) throws IOException {
        this.buffer.writeByte((byte)b);
    }

    public ChannelBuffer buffer() {
        return this.buffer;
    }
}

