/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.buffer;

import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffers;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public abstract class AbstractChannelBuffer
implements ChannelBuffer {
    private int readerIndex;
    private int writerIndex;
    private int markedReaderIndex;
    private int markedWriterIndex;

    @Override
    public int readerIndex() {
        return this.readerIndex;
    }

    @Override
    public void readerIndex(int readerIndex) {
        if (readerIndex < 0 || readerIndex > this.writerIndex) {
            throw new IndexOutOfBoundsException();
        }
        this.readerIndex = readerIndex;
    }

    @Override
    public int writerIndex() {
        return this.writerIndex;
    }

    @Override
    public void writerIndex(int writerIndex) {
        if (writerIndex < this.readerIndex || writerIndex > this.capacity()) {
            throw new IndexOutOfBoundsException();
        }
        this.writerIndex = writerIndex;
    }

    @Override
    public void setIndex(int readerIndex, int writerIndex) {
        if (readerIndex < 0 || readerIndex > writerIndex || writerIndex > this.capacity()) {
            throw new IndexOutOfBoundsException();
        }
        this.readerIndex = readerIndex;
        this.writerIndex = writerIndex;
    }

    @Override
    public void clear() {
        this.writerIndex = 0;
        this.readerIndex = 0;
    }

    @Override
    public boolean readable() {
        return this.readableBytes() > 0;
    }

    @Override
    public boolean writable() {
        return this.writableBytes() > 0;
    }

    @Override
    public int readableBytes() {
        return this.writerIndex - this.readerIndex;
    }

    @Override
    public int writableBytes() {
        return this.capacity() - this.writerIndex;
    }

    @Override
    public void markReaderIndex() {
        this.markedReaderIndex = this.readerIndex;
    }

    @Override
    public void resetReaderIndex() {
        this.readerIndex(this.markedReaderIndex);
    }

    @Override
    public void markWriterIndex() {
        this.markedWriterIndex = this.writerIndex;
    }

    @Override
    public void resetWriterIndex() {
        this.writerIndex = this.markedWriterIndex;
    }

    @Override
    public void discardReadBytes() {
        if (this.readerIndex == 0) {
            return;
        }
        this.setBytes(0, this, this.readerIndex, this.writerIndex - this.readerIndex);
        this.writerIndex -= this.readerIndex;
        this.markedReaderIndex = Math.max(this.markedReaderIndex - this.readerIndex, 0);
        this.markedWriterIndex = Math.max(this.markedWriterIndex - this.readerIndex, 0);
        this.readerIndex = 0;
    }

    @Override
    public void ensureWritableBytes(int writableBytes) {
        if (writableBytes > this.writableBytes()) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void getBytes(int index, byte[] dst) {
        this.getBytes(index, dst, 0, dst.length);
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
    public void setBytes(int index, byte[] src) {
        this.setBytes(index, src, 0, src.length);
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
    public byte readByte() {
        if (this.readerIndex == this.writerIndex) {
            throw new IndexOutOfBoundsException();
        }
        return this.getByte(this.readerIndex++);
    }

    @Override
    public ChannelBuffer readBytes(int length) {
        this.checkReadableBytes(length);
        if (length == 0) {
            return ChannelBuffers.EMPTY_BUFFER;
        }
        ChannelBuffer buf = this.factory().getBuffer(length);
        buf.writeBytes(this, this.readerIndex, length);
        this.readerIndex += length;
        return buf;
    }

    @Override
    public void readBytes(byte[] dst, int dstIndex, int length) {
        this.checkReadableBytes(length);
        this.getBytes(this.readerIndex, dst, dstIndex, length);
        this.readerIndex += length;
    }

    @Override
    public void readBytes(byte[] dst) {
        this.readBytes(dst, 0, dst.length);
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
        this.checkReadableBytes(length);
        this.getBytes(this.readerIndex, dst, dstIndex, length);
        this.readerIndex += length;
    }

    @Override
    public void readBytes(ByteBuffer dst) {
        int length = dst.remaining();
        this.checkReadableBytes(length);
        this.getBytes(this.readerIndex, dst);
        this.readerIndex += length;
    }

    @Override
    public void readBytes(OutputStream out, int length) throws IOException {
        this.checkReadableBytes(length);
        this.getBytes(this.readerIndex, out, length);
        this.readerIndex += length;
    }

    @Override
    public void skipBytes(int length) {
        int newReaderIndex = this.readerIndex + length;
        if (newReaderIndex > this.writerIndex) {
            throw new IndexOutOfBoundsException();
        }
        this.readerIndex = newReaderIndex;
    }

    @Override
    public void writeByte(int value) {
        this.setByte(this.writerIndex++, value);
    }

    @Override
    public void writeBytes(byte[] src, int srcIndex, int length) {
        this.setBytes(this.writerIndex, src, srcIndex, length);
        this.writerIndex += length;
    }

    @Override
    public void writeBytes(byte[] src) {
        this.writeBytes(src, 0, src.length);
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
        this.setBytes(this.writerIndex, src, srcIndex, length);
        this.writerIndex += length;
    }

    @Override
    public void writeBytes(ByteBuffer src) {
        int length = src.remaining();
        this.setBytes(this.writerIndex, src);
        this.writerIndex += length;
    }

    @Override
    public int writeBytes(InputStream in, int length) throws IOException {
        int writtenBytes = this.setBytes(this.writerIndex, in, length);
        if (writtenBytes > 0) {
            this.writerIndex += writtenBytes;
        }
        return writtenBytes;
    }

    @Override
    public ChannelBuffer copy() {
        return this.copy(this.readerIndex, this.readableBytes());
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return this.toByteBuffer(this.readerIndex, this.readableBytes());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ChannelBuffer && ChannelBuffers.equals(this, (ChannelBuffer)o);
    }

    @Override
    public int compareTo(ChannelBuffer that) {
        return ChannelBuffers.compare(this, that);
    }

    public String toString() {
        return this.getClass().getSimpleName() + '(' + "ridx=" + this.readerIndex + ", widx=" + this.writerIndex + ", cap=" + this.capacity() + ')';
    }

    protected void checkReadableBytes(int minimumReadableBytes) {
        if (this.readableBytes() < minimumReadableBytes) {
            throw new IndexOutOfBoundsException();
        }
    }
}

