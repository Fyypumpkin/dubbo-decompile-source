/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.buffer;

import com.alibaba.dubbo.remoting.buffer.AbstractChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory;
import com.alibaba.dubbo.remoting.buffer.DirectChannelBufferFactory;
import com.alibaba.dubbo.remoting.buffer.HeapChannelBufferFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ByteBufferBackedChannelBuffer
extends AbstractChannelBuffer {
    private final ByteBuffer buffer;
    private final int capacity;

    public ByteBufferBackedChannelBuffer(ByteBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        this.buffer = buffer.slice();
        this.capacity = buffer.remaining();
        this.writerIndex(this.capacity);
    }

    private ByteBufferBackedChannelBuffer(ByteBufferBackedChannelBuffer buffer) {
        this.buffer = buffer.buffer;
        this.capacity = buffer.capacity;
        this.setIndex(buffer.readerIndex(), buffer.writerIndex());
    }

    @Override
    public ChannelBufferFactory factory() {
        if (this.buffer.isDirect()) {
            return DirectChannelBufferFactory.getInstance();
        }
        return HeapChannelBufferFactory.getInstance();
    }

    @Override
    public int capacity() {
        return this.capacity;
    }

    @Override
    public ChannelBuffer copy(int index, int length) {
        ByteBuffer src;
        try {
            src = (ByteBuffer)this.buffer.duplicate().position(index).limit(index + length);
        }
        catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
        ByteBuffer dst = this.buffer.isDirect() ? ByteBuffer.allocateDirect(length) : ByteBuffer.allocate(length);
        dst.put(src);
        dst.clear();
        return new ByteBufferBackedChannelBuffer(dst);
    }

    @Override
    public byte getByte(int index) {
        return this.buffer.get(index);
    }

    @Override
    public void getBytes(int index, byte[] dst, int dstIndex, int length) {
        ByteBuffer data = this.buffer.duplicate();
        try {
            data.limit(index + length).position(index);
        }
        catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
        data.get(dst, dstIndex, length);
    }

    @Override
    public void getBytes(int index, ByteBuffer dst) {
        ByteBuffer data = this.buffer.duplicate();
        int bytesToCopy = Math.min(this.capacity() - index, dst.remaining());
        try {
            data.limit(index + bytesToCopy).position(index);
        }
        catch (IllegalArgumentException e) {
            throw new IndexOutOfBoundsException();
        }
        dst.put(data);
    }

    @Override
    public void getBytes(int index, ChannelBuffer dst, int dstIndex, int length) {
        if (dst instanceof ByteBufferBackedChannelBuffer) {
            ByteBufferBackedChannelBuffer bbdst = (ByteBufferBackedChannelBuffer)dst;
            ByteBuffer data = bbdst.buffer.duplicate();
            data.limit(dstIndex + length).position(dstIndex);
            this.getBytes(index, data);
        } else if (this.buffer.hasArray()) {
            dst.setBytes(dstIndex, this.buffer.array(), index + this.buffer.arrayOffset(), length);
        } else {
            dst.setBytes(dstIndex, this, index, length);
        }
    }

    @Override
    public void getBytes(int index, OutputStream out, int length) throws IOException {
        if (length == 0) {
            return;
        }
        if (this.buffer.hasArray()) {
            out.write(this.buffer.array(), index + this.buffer.arrayOffset(), length);
        } else {
            byte[] tmp = new byte[length];
            ((ByteBuffer)this.buffer.duplicate().position(index)).get(tmp);
            out.write(tmp);
        }
    }

    @Override
    public boolean isDirect() {
        return this.buffer.isDirect();
    }

    @Override
    public void setByte(int index, int value) {
        this.buffer.put(index, (byte)value);
    }

    @Override
    public void setBytes(int index, byte[] src, int srcIndex, int length) {
        ByteBuffer data = this.buffer.duplicate();
        data.limit(index + length).position(index);
        data.put(src, srcIndex, length);
    }

    @Override
    public void setBytes(int index, ByteBuffer src) {
        ByteBuffer data = this.buffer.duplicate();
        data.limit(index + src.remaining()).position(index);
        data.put(src);
    }

    @Override
    public void setBytes(int index, ChannelBuffer src, int srcIndex, int length) {
        if (src instanceof ByteBufferBackedChannelBuffer) {
            ByteBufferBackedChannelBuffer bbsrc = (ByteBufferBackedChannelBuffer)src;
            ByteBuffer data = bbsrc.buffer.duplicate();
            data.limit(srcIndex + length).position(srcIndex);
            this.setBytes(index, data);
        } else if (this.buffer.hasArray()) {
            src.getBytes(srcIndex, this.buffer.array(), index + this.buffer.arrayOffset(), length);
        } else {
            src.getBytes(srcIndex, this, index, length);
        }
    }

    @Override
    public ByteBuffer toByteBuffer(int index, int length) {
        if (index == 0 && length == this.capacity()) {
            return this.buffer.duplicate();
        }
        return ((ByteBuffer)this.buffer.duplicate().position(index).limit(index + length)).slice();
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        int readBytes;
        readBytes = 0;
        if (this.buffer.hasArray()) {
            int localReadBytes;
            index += this.buffer.arrayOffset();
            do {
                if ((localReadBytes = in.read(this.buffer.array(), index, length)) < 0) {
                    if (readBytes == 0) {
                        return -1;
                    }
                    break;
                }
                readBytes += localReadBytes;
                index += localReadBytes;
            } while ((length -= localReadBytes) > 0);
        } else {
            int localReadBytes;
            byte[] tmp = new byte[length];
            int i = 0;
            do {
                if ((localReadBytes = in.read(tmp, i, tmp.length - i)) >= 0) continue;
                if (readBytes != 0) break;
                return -1;
            } while ((i += (readBytes += localReadBytes)) < tmp.length);
            ((ByteBuffer)this.buffer.duplicate().position(index)).put(tmp);
        }
        return readBytes;
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

