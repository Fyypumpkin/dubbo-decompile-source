/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.netty.buffer.ChannelBuffer
 *  org.jboss.netty.buffer.ChannelBuffers
 */
package com.alibaba.dubbo.remoting.transport.netty;

import com.alibaba.dubbo.remoting.buffer.ChannelBufferFactory;
import com.alibaba.dubbo.remoting.transport.netty.NettyBackedChannelBuffer;
import java.nio.ByteBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class NettyBackedChannelBufferFactory
implements ChannelBufferFactory {
    private static final NettyBackedChannelBufferFactory INSTANCE = new NettyBackedChannelBufferFactory();

    public static ChannelBufferFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public com.alibaba.dubbo.remoting.buffer.ChannelBuffer getBuffer(int capacity) {
        return new NettyBackedChannelBuffer(ChannelBuffers.dynamicBuffer((int)capacity));
    }

    @Override
    public com.alibaba.dubbo.remoting.buffer.ChannelBuffer getBuffer(byte[] array, int offset, int length) {
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer((int)length);
        buffer.writeBytes(array, offset, length);
        return new NettyBackedChannelBuffer(buffer);
    }

    @Override
    public com.alibaba.dubbo.remoting.buffer.ChannelBuffer getBuffer(ByteBuffer nioBuffer) {
        return new NettyBackedChannelBuffer(ChannelBuffers.wrappedBuffer((ByteBuffer)nioBuffer));
    }
}

