/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.buffer;

import com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public interface ChannelBuffer
extends Comparable<ChannelBuffer> {
    public int capacity();

    public void clear();

    public ChannelBuffer copy();

    public ChannelBuffer copy(int var1, int var2);

    public void discardReadBytes();

    public void ensureWritableBytes(int var1);

    public boolean equals(Object var1);

    public ChannelBufferFactory factory();

    public byte getByte(int var1);

    public void getBytes(int var1, byte[] var2);

    public void getBytes(int var1, byte[] var2, int var3, int var4);

    public void getBytes(int var1, ByteBuffer var2);

    public void getBytes(int var1, ChannelBuffer var2);

    public void getBytes(int var1, ChannelBuffer var2, int var3);

    public void getBytes(int var1, ChannelBuffer var2, int var3, int var4);

    public void getBytes(int var1, OutputStream var2, int var3) throws IOException;

    public boolean isDirect();

    public void markReaderIndex();

    public void markWriterIndex();

    public boolean readable();

    public int readableBytes();

    public byte readByte();

    public void readBytes(byte[] var1);

    public void readBytes(byte[] var1, int var2, int var3);

    public void readBytes(ByteBuffer var1);

    public void readBytes(ChannelBuffer var1);

    public void readBytes(ChannelBuffer var1, int var2);

    public void readBytes(ChannelBuffer var1, int var2, int var3);

    public ChannelBuffer readBytes(int var1);

    public void resetReaderIndex();

    public void resetWriterIndex();

    public int readerIndex();

    public void readerIndex(int var1);

    public void readBytes(OutputStream var1, int var2) throws IOException;

    public void setByte(int var1, int var2);

    public void setBytes(int var1, byte[] var2);

    public void setBytes(int var1, byte[] var2, int var3, int var4);

    public void setBytes(int var1, ByteBuffer var2);

    public void setBytes(int var1, ChannelBuffer var2);

    public void setBytes(int var1, ChannelBuffer var2, int var3);

    public void setBytes(int var1, ChannelBuffer var2, int var3, int var4);

    public int setBytes(int var1, InputStream var2, int var3) throws IOException;

    public void setIndex(int var1, int var2);

    public void skipBytes(int var1);

    public ByteBuffer toByteBuffer();

    public ByteBuffer toByteBuffer(int var1, int var2);

    public boolean writable();

    public int writableBytes();

    public void writeByte(int var1);

    public void writeBytes(byte[] var1);

    public void writeBytes(byte[] var1, int var2, int var3);

    public void writeBytes(ByteBuffer var1);

    public void writeBytes(ChannelBuffer var1);

    public void writeBytes(ChannelBuffer var1, int var2);

    public void writeBytes(ChannelBuffer var1, int var2, int var3);

    public int writeBytes(InputStream var1, int var2) throws IOException;

    public int writerIndex();

    public void writerIndex(int var1);

    public byte[] array();

    public boolean hasArray();

    public int arrayOffset();
}

