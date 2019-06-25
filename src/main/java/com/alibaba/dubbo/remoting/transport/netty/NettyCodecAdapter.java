/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.netty.buffer.ChannelBuffer
 *  org.jboss.netty.buffer.ChannelBuffers
 *  org.jboss.netty.channel.Channel
 *  org.jboss.netty.channel.ChannelEvent
 *  org.jboss.netty.channel.ChannelHandler
 *  org.jboss.netty.channel.ChannelHandler$Sharable
 *  org.jboss.netty.channel.ChannelHandlerContext
 *  org.jboss.netty.channel.Channels
 *  org.jboss.netty.channel.ExceptionEvent
 *  org.jboss.netty.channel.MessageEvent
 *  org.jboss.netty.channel.SimpleChannelUpstreamHandler
 *  org.jboss.netty.handler.codec.oneone.OneToOneEncoder
 */
package com.alibaba.dubbo.remoting.transport.netty;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.DynamicChannelBuffer;
import com.alibaba.dubbo.remoting.transport.netty.NettyChannel;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

final class NettyCodecAdapter {
    private final org.jboss.netty.channel.ChannelHandler encoder = new InternalEncoder();
    private final org.jboss.netty.channel.ChannelHandler decoder = new InternalDecoder();
    private final Codec2 codec;
    private final URL url;
    private final int bufferSize;
    private final ChannelHandler handler;

    public NettyCodecAdapter(Codec2 codec, URL url, ChannelHandler handler) {
        this.codec = codec;
        this.url = url;
        this.handler = handler;
        int b = url.getPositiveParameter("buffer", 8192);
        this.bufferSize = b >= 1024 && b <= 16384 ? b : 8192;
    }

    public org.jboss.netty.channel.ChannelHandler getEncoder() {
        return this.encoder;
    }

    public org.jboss.netty.channel.ChannelHandler getDecoder() {
        return this.decoder;
    }

    private class InternalDecoder
    extends SimpleChannelUpstreamHandler {
        private com.alibaba.dubbo.remoting.buffer.ChannelBuffer buffer = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;

        private InternalDecoder() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
            com.alibaba.dubbo.remoting.buffer.ChannelBuffer message;
            Object o = event.getMessage();
            if (!(o instanceof ChannelBuffer)) {
                ctx.sendUpstream((ChannelEvent)event);
                return;
            }
            ChannelBuffer input = (ChannelBuffer)o;
            int readable = input.readableBytes();
            if (readable <= 0) {
                return;
            }
            if (this.buffer.readable()) {
                if (this.buffer instanceof DynamicChannelBuffer) {
                    this.buffer.writeBytes(input.toByteBuffer());
                    message = this.buffer;
                } else {
                    int size = this.buffer.readableBytes() + input.readableBytes();
                    message = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.dynamicBuffer(size > NettyCodecAdapter.this.bufferSize ? size : NettyCodecAdapter.this.bufferSize);
                    message.writeBytes(this.buffer, this.buffer.readableBytes());
                    message.writeBytes(input.toByteBuffer());
                }
            } else {
                message = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.wrappedBuffer(input.toByteBuffer());
            }
            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), NettyCodecAdapter.this.url, NettyCodecAdapter.this.handler);
            try {
                do {
                    Object msg;
                    int saveReaderIndex = message.readerIndex();
                    try {
                        msg = NettyCodecAdapter.this.codec.decode(channel, message);
                    }
                    catch (IOException e) {
                        this.buffer = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                        throw e;
                    }
                    if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                        message.readerIndex(saveReaderIndex);
                        break;
                    }
                    if (saveReaderIndex == message.readerIndex()) {
                        this.buffer = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                        throw new IOException("Decode without read data.");
                    }
                    if (msg == null) continue;
                    Channels.fireMessageReceived((ChannelHandlerContext)ctx, (Object)msg, (SocketAddress)event.getRemoteAddress());
                } while (message.readable());
            }
            finally {
                if (message.readable()) {
                    message.discardReadBytes();
                    this.buffer = message;
                } else {
                    this.buffer = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                }
                NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
            }
        }

        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            ctx.sendUpstream((ChannelEvent)e);
        }
    }

    @ChannelHandler.Sharable
    private class InternalEncoder
    extends OneToOneEncoder {
        private InternalEncoder() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        protected Object encode(ChannelHandlerContext ctx, org.jboss.netty.channel.Channel ch, Object msg) throws Exception {
            com.alibaba.dubbo.remoting.buffer.ChannelBuffer buffer;
            buffer = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.dynamicBuffer(1024);
            NettyChannel channel = NettyChannel.getOrAddChannel(ch, NettyCodecAdapter.this.url, NettyCodecAdapter.this.handler);
            try {
                NettyCodecAdapter.this.codec.encode(channel, buffer, msg);
            }
            finally {
                NettyChannel.removeChannelIfDisconnected(ch);
            }
            return ChannelBuffers.wrappedBuffer((ByteBuffer)buffer.toByteBuffer());
        }
    }

}

