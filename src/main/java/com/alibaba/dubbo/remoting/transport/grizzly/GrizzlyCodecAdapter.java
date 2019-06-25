/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.glassfish.grizzly.Buffer
 *  org.glassfish.grizzly.Connection
 *  org.glassfish.grizzly.Transport
 *  org.glassfish.grizzly.filterchain.BaseFilter
 *  org.glassfish.grizzly.filterchain.FilterChainContext
 *  org.glassfish.grizzly.filterchain.NextAction
 *  org.glassfish.grizzly.memory.MemoryManager
 */
package com.alibaba.dubbo.remoting.transport.grizzly;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffers;
import com.alibaba.dubbo.remoting.buffer.DynamicChannelBuffer;
import com.alibaba.dubbo.remoting.transport.grizzly.GrizzlyChannel;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.memory.MemoryManager;

public class GrizzlyCodecAdapter
extends BaseFilter {
    private final Codec2 codec;
    private final URL url;
    private final ChannelHandler handler;
    private final int bufferSize;
    private ChannelBuffer previousData = ChannelBuffers.EMPTY_BUFFER;

    public GrizzlyCodecAdapter(Codec2 codec, URL url, ChannelHandler handler) {
        this.codec = codec;
        this.url = url;
        this.handler = handler;
        int b = url.getPositiveParameter("buffer", 8192);
        this.bufferSize = b >= 1024 && b <= 16384 ? b : 8192;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public NextAction handleWrite(FilterChainContext context) throws IOException {
        Connection connection = context.getConnection();
        GrizzlyChannel channel = GrizzlyChannel.getOrAddChannel(connection, this.url, this.handler);
        try {
            ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer(1024);
            Object msg = context.getMessage();
            this.codec.encode(channel, channelBuffer, msg);
            GrizzlyChannel.removeChannelIfDisconnectd(connection);
            Buffer buffer = connection.getTransport().getMemoryManager().allocate(channelBuffer.readableBytes());
            buffer.put(channelBuffer.toByteBuffer());
            buffer.flip();
            buffer.allowBufferDispose(true);
            context.setMessage((Object)buffer);
        }
        finally {
            GrizzlyChannel.removeChannelIfDisconnectd(connection);
        }
        return context.getInvokeAction();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public NextAction handleRead(FilterChainContext context) throws IOException {
        Object message = context.getMessage();
        Connection connection = context.getConnection();
        GrizzlyChannel channel = GrizzlyChannel.getOrAddChannel(connection, this.url, this.handler);
        try {
            if (message instanceof Buffer) {
                ChannelBuffer frame;
                Object msg;
                Buffer grizzlyBuffer = (Buffer)message;
                if (this.previousData.readable()) {
                    if (this.previousData instanceof DynamicChannelBuffer) {
                        this.previousData.writeBytes(grizzlyBuffer.toByteBuffer());
                        frame = this.previousData;
                    } else {
                        int size = this.previousData.readableBytes() + grizzlyBuffer.remaining();
                        frame = ChannelBuffers.dynamicBuffer(size > this.bufferSize ? size : this.bufferSize);
                        frame.writeBytes(this.previousData, this.previousData.readableBytes());
                        frame.writeBytes(grizzlyBuffer.toByteBuffer());
                    }
                } else {
                    frame = ChannelBuffers.wrappedBuffer(grizzlyBuffer.toByteBuffer());
                }
                int savedReadIndex = frame.readerIndex();
                try {
                    msg = this.codec.decode(channel, frame);
                }
                catch (Exception e) {
                    this.previousData = ChannelBuffers.EMPTY_BUFFER;
                    throw new IOException(e.getMessage(), e);
                }
                if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                    frame.readerIndex(savedReadIndex);
                    NextAction e = context.getStopAction();
                    return e;
                }
                if (savedReadIndex == frame.readerIndex()) {
                    this.previousData = ChannelBuffers.EMPTY_BUFFER;
                    throw new IOException("Decode without read data.");
                }
                if (msg != null) {
                    context.setMessage(msg);
                    NextAction e = context.getInvokeAction();
                    return e;
                }
                NextAction e = context.getInvokeAction();
                return e;
            }
            NextAction grizzlyBuffer = context.getInvokeAction();
            return grizzlyBuffer;
        }
        finally {
            GrizzlyChannel.removeChannelIfDisconnectd(connection);
        }
    }
}

