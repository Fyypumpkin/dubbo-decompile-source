/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.buffer;

import com.alibaba.dubbo.remoting.buffer.AbstractChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory;
import com.alibaba.dubbo.remoting.buffer.HeapChannelBufferFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class DynamicChannelBuffer
extends AbstractChannelBuffer {
    private final ChannelBufferFactory factory;
    private ChannelBuffer buffer;

    public DynamicChannelBuffer(int estimatedLength) {
        this(estimatedLength, HeapChannelBufferFactory.getInstance());
    }

    public DynamicChannelBuffer(int estimatedLength, ChannelBufferFactory factory) {
        if (estimatedLength < 0) {
            throw new IllegalArgumentException("estimatedLength: " + estimatedLength);
        }
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        this.factory = factory;
        this.buffer = factory.getBuffer(estimatedLength);
    }

    @Override
    public void ensureWritableBytes(int minWritableBytes) {
        int newCapacity;
        if (minWritableBytes <= this.writableBytes()) {
            return;
        }
        int minNewCapacity = this.writerIndex() + minWritableBytes;
        for (newCapacity = this.capacity() == 0 ? 1 : this.capacity(); newCapacity < minNewCapacity; newCapacity <<= 1) {
        }
        ChannelBuffer newBuffer = this.factory().getBuffer(newCapacity);
        newBuffer.writeBytes(this.buffer, 0, this.writerIndex());
        this.buffer = newBuffer;
    }

    @Override
    public int capacity() {
        return this.buffer.capacity();
    }

    @Override
    public ChannelBuffer copy(int index, int length) {
        DynamicChannelBuffer copiedBuffer = new DynamicChannelBuffer(Math.max(length, 64), this.factory());
        copiedBuffer.buffer = this.buffer.copy(index, length);
        copiedBuffer.setIndex(0, length);
        return copiedBuffer;
    }

    @Override
    public ChannelBufferFactory factory() {
        return this.factory;
    }

    @Override
    public byte getByte(int index) {
        return this.buffer.getByte(index);
    }

    @Override
    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        this.buffer.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public void getBytes(int index, ByteBuffer dst) {
        this.buffer.getBytes(index, dst);
    }

    @Override
    public void getBytes(int index, ChannelBuffer dst, int dstIndex, int length) {
        this.buffer.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public void getBytes(int index, OutputStream dst, int length) throws IOException {
        this.buffer.getBytes(index, dst, length);
    }

    @Override
    public boolean isDirect() {
        return this.buffer.isDirect();
    }

    @Override
    public void setByte(int index, int value) {
        this.buffer.setByte(index, value);
    }

    @Override
    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        this.buffer.setBytes(index, src, srcIndex, length);
    }

    @Override
    public void setBytes(int index, ByteBuffer src) {
        this.buffer.setBytes(index, src);
    }

    @Override
    public void setBytes(int index, ChannelBuffer src, int srcIndex, int length) {
        this.buffer.setBytes(index, src, srcIndex, length);
    }

    @Override
    public int setBytes(int index, InputStream src, int length) throws IOException {
        return this.buffer.setBytes(index, src, length);
    }

    @Override
    public ByteBuffer toByteBuffer(int index, int length) {
        return this.buffer.toByteBuffer(index, length);
    }

    @Override
    public void writeByte(int value) {
        this.ensureWritableBytes(1);
        super.writeByte(value);
    }

    @Override
    public void writeBytes(byte[] src, int srcIndex, int length) {
        this.ensureWritableBytes(length);
        super.writeBytes(src, srcIndex, length);
    }

    @Override
    public void writeBytes(ChannelBuffer src, int srcIndex, int length) {
        this.ensureWritableBytes(length);
        super.writeBytes(src, srcIndex, length);
    }

    @Override
    public void writeBytes(ByteBuffer src) {
        this.ensureWritableBytes(src.remaining());
        super.writeBytes(src);
    }

    @Override
    public int writeBytes(InputStream in, int length) throws IOException {
        this.ensureWritableBytes(length);
        return super.writeBytes(in, length);
    }

    @Override
    public byte[] array() {
        return this.buffer.array();
    }

    @Override
    public boolean hasArray() {
        return this.buffer.hasArray();
    }

    @Override
    public int arrayOffset() {
        return this.buffer.arrayOffset();
    }
}

