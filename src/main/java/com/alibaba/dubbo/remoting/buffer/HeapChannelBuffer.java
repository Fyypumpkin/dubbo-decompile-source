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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

public class HeapChannelBuffer
extends AbstractChannelBuffer {
    protected final byte[] array;

    public HeapChannelBuffer(int length) {
        this(new byte[length], 0, 0);
    }

    public HeapChannelBuffer(byte[] array) {
        this(array, 0, array.length);
    }

    protected HeapChannelBuffer(byte[] array, int readerIndex, int writerIndex) {
        if (array == null) {
            throw new NullPointerException("array");
        }
        this.array = array;
        this.setIndex(readerIndex, writerIndex);
    }

    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    public int capacity() {
        return this.array.length;
    }

    @Override
    public boolean hasArray() {
        return true;
    }

    @Override
    public byte[] array() {
        return this.array;
    }

    @Override
    public int arrayOffset() {
        return 0;
    }

    @Override
    public byte getByte(int index) {
        return this.array[index];
    }

    @Override
    public void getBytes(int index, ChannelBuffer dst, int dstIndex, int length) {
        if (dst instanceof HeapChannelBuffer) {
            this.getBytes(index, ((HeapChannelBuffer)dst).array, dstIndex, length);
        } else {
            dst.setBytes(dstIndex, this.array, index, length);
        }
    }

    @Override
    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        System.arraycopy(this.array, index, dst, dstIndex, length);
    }

    @Override
    public void getBytes(int index, ByteBuffer dst) {
        dst.put(this.array, index, Math.min(this.capacity() - index, dst.remaining()));
    }

    @Override
    public void getBytes(int index, OutputStream out, int length) throws IOException {
        out.write(this.array, index, length);
    }

    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return out.write(ByteBuffer.wrap(this.array, index, length));
    }

    @Override
    public void setByte(int index, int value) {
        this.array[index] = (byte)value;
    }

    @Override
    public void setBytes(int index, ChannelBuffer src, int srcIndex, int length) {
        if (src instanceof HeapChannelBuffer) {
            this.setBytes(index, ((HeapChannelBuffer)src).array, srcIndex, length);
        } else {
            src.getBytes(srcIndex, this.array, index, length);
        }
    }

    @Override
    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        System.arraycopy(src, srcIndex, this.array, index, length);
    }

    @Override
    public void setBytes(int index, ByteBuffer src) {
        src.get(this.array, index, src.remaining());
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        int localReadBytes;
        int readBytes = 0;
        do {
            if ((localReadBytes = in.read(this.array, index, length)) < 0) {
                if (readBytes != 0) break;
                return -1;
            }
            readBytes += localReadBytes;
            index += localReadBytes;
        } while ((length -= localReadBytes) > 0);
        return readBytes;
    }

    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        int localReadBytes;
        ByteBuffer buf = ByteBuffer.wrap(this.array, index, length);
        int readBytes = 0;
        do {
            try {
                localReadBytes = in.read(buf);
            }
            catch (ClosedChannelException e) {
                localReadBytes = -1;
            }
            if (localReadBytes >= 0) continue;
            if (readBytes != 0) break;
            return -1;
        } while (localReadBytes != 0 && (readBytes += localReadBytes) < length);
        return readBytes;
    }

    @Override
    public ChannelBuffer copy(int index, int length) {
        if (index < 0 || length < 0 || index + length > this.array.length) {
            throw new IndexOutOfBoundsException();
        }
        byte[] copiedArray = new byte[length];
        System.arraycopy(this.array, index, copiedArray, 0, length);
        return new HeapChannelBuffer(copiedArray);
    }

    @Override
    public ChannelBufferFactory factory() {
        return HeapChannelBufferFactory.getInstance();
    }

    @Override
    public ByteBuffer toByteBuffer(int index, int length) {
        return ByteBuffer.wrap(this.array, index, length);
    }
}

