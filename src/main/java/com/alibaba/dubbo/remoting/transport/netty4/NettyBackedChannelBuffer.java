/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package com.alibaba.dubbo.remoting.transport.netty4;

import com.alibaba.dubbo.common.utils.Assert;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffers;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class NettyBackedChannelBuffer
implements ChannelBuffer {
    private ByteBuf buffer;

    public NettyBackedChannelBuffer(ByteBuf buffer) {
        Assert.notNull((Object)buffer, "buffer == null");
        this.buffer = buffer;
    }

    @Override
    public int capacity() {
        return this.buffer.capacity();
    }

    @Override
    public ChannelBuffer copy(int index, int length) {
        return new NettyBackedChannelBuffer(this.buffer.copy(index, length));
    }

    @Override
    public ChannelBufferFactory factory() {
        return null;
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
        byte[] data = new byte[length];
        this.buffer.getBytes(index, data, 0, length);
        dst.setBytes(dstIndex, data, 0, length);
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
        byte[] data = new byte[length];
        this.buffer.getBytes(srcIndex, data, 0, length);
        this.setBytes(0, data, index, length);
    }

    @Override
    public int setBytes(int index, InputStream src, int length) throws IOException {
        return this.buffer.setBytes(index, src, length);
    }

    @Override
    public ByteBuffer toByteBuffer(int index, int length) {
        return this.buffer.nioBuffer(index, length);
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

    @Override
    public void clear() {
        this.buffer.clear();
    }

    @Override
    public ChannelBuffer copy() {
        return new NettyBackedChannelBuffer(this.buffer.copy());
    }

    @Override
    public void discardReadBytes() {
        this.buffer.discardReadBytes();
    }

    @Override
    public void ensureWritableBytes(int writableBytes) {
        this.buffer.ensureWritable(writableBytes);
    }

    @Override
    public void getBytes(int index, byte[] dst) {
        this.buffer.getBytes(index, dst);
    }

    @Override
    public void getBytes(int index, ChannelBuffer dst) {
        this.getBytes(index, dst, dst.writableBytes());
    }

    @Override
    public void getBytes(int index, ChannelBuffer dst, int length) {
        if (length > dst.writableBytes()) {
            throw new IndexOutOfBoundsException();
        }
        this.getBytes(index, dst, dst.writerIndex(), length);
        dst.writerIndex(dst.writerIndex() + length);
    }

    @Override
    public void markReaderIndex() {
        this.buffer.markReaderIndex();
    }

    @Override
    public void markWriterIndex() {
        this.buffer.markWriterIndex();
    }

    @Override
    public boolean readable() {
        return this.buffer.isReadable();
    }

    @Override
    public int readableBytes() {
        return this.buffer.readableBytes();
    }

    @Override
    public byte readByte() {
        return this.buffer.readByte();
    }

    @Override
    public void readBytes(byte[] dst) {
        this.buffer.readBytes(dst);
    }

    @Override
    public void readBytes(byte[] dst, int dstIndex, int length) {
        this.buffer.readBytes(dst, dstIndex, length);
    }

    @Override
    public void readBytes(ByteBuffer dst) {
        this.buffer.readBytes(dst);
    }

    @Override
    public void readBytes(ChannelBuffer dst) {
        this.readBytes(dst, dst.writableBytes());
    }

    @Override
    public void readBytes(ChannelBuffer dst, int length) {
        if (length > dst.writableBytes()) {
            throw new IndexOutOfBoundsException();
        }
        this.readBytes(dst, dst.writerIndex(), length);
        dst.writerIndex(dst.writerIndex() + length);
    }

    @Override
    public void readBytes(ChannelBuffer dst, int dstIndex, int length) {
        if (this.readableBytes() < length) {
            throw new IndexOutOfBoundsException();
        }
        byte[] data = new byte[length];
        this.buffer.readBytes(data, 0, length);
        dst.setBytes(dstIndex, data, 0, length);
    }

    @Override
    public ChannelBuffer readBytes(int length) {
        return new NettyBackedChannelBuffer(this.buffer.readBytes(length));
    }

    @Override
    public void resetReaderIndex() {
        this.buffer.resetReaderIndex();
    }

    @Override
    public void resetWriterIndex() {
        this.buffer.resetWriterIndex();
    }

    @Override
    public int readerIndex() {
        return this.buffer.readerIndex();
    }

    @Override
    public void readerIndex(int readerIndex) {
        this.buffer.readerIndex(readerIndex);
    }

    @Override
    public void readBytes(OutputStream dst, int length) throws IOException {
        this.buffer.readBytes(dst, length);
    }

    @Override
    public void setBytes(int index, byte[] src) {
        this.buffer.setBytes(index, src);
    }

    @Override
    public void setBytes(int index, ChannelBuffer src) {
        this.setBytes(index, src, src.readableBytes());
    }

    @Override
    public void setBytes(int index, ChannelBuffer src, int length) {
        if (length > src.readableBytes()) {
            throw new IndexOutOfBoundsException();
        }
        this.setBytes(index, src, src.readerIndex(), length);
        src.readerIndex(src.readerIndex() + length);
    }

    @Override
    public void setIndex(int readerIndex, int writerIndex) {
        this.buffer.setIndex(readerIndex, writerIndex);
    }

    @Override
    public void skipBytes(int length) {
        this.buffer.skipBytes(length);
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return this.buffer.nioBuffer();
    }

    @Override
    public boolean writable() {
        return this.buffer.isWritable();
    }

    @Override
    public int writableBytes() {
        return this.buffer.writableBytes();
    }

    @Override
    public void writeByte(int value) {
        this.buffer.writeByte(value);
    }

    @Override
    public void writeBytes(byte[] src) {
        this.buffer.writeBytes(src);
    }

    @Override
    public void writeBytes(byte[] src, int index, int length) {
        this.buffer.writeBytes(src, index, length);
    }

    @Override
    public void writeBytes(ByteBuffer src) {
        this.buffer.writeBytes(src);
    }

    @Override
    public void writeBytes(ChannelBuffer src) {
        this.writeBytes(src, src.readableBytes());
    }

    @Override
    public void writeBytes(ChannelBuffer src, int length) {
        if (length > src.readableBytes()) {
            throw new IndexOutOfBoundsException();
        }
        this.writeBytes(src, src.readerIndex(), length);
        src.readerIndex(src.readerIndex() + length);
    }

    @Override
    public void writeBytes(ChannelBuffer src, int srcIndex, int length) {
        byte[] data = new byte[length];
        src.getBytes(srcIndex, data, 0, length);
        this.writeBytes(data, 0, length);
    }

    @Override
    public int writeBytes(InputStream src, int length) throws IOException {
        return this.buffer.writeBytes(src, length);
    }

    @Override
    public int writerIndex() {
        return this.buffer.writerIndex();
    }

    @Override
    public void writerIndex(int writerIndex) {
        this.buffer.writerIndex(writerIndex);
    }

    @Override
    public int compareTo(ChannelBuffer o) {
        return ChannelBuffers.compare(this, o);
    }
}

