/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  io.netty.handler.codec.MessageToByteEncoder
 */
package com.alibaba.dubbo.remoting.transport.netty4;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.transport.netty4.NettyBackedChannelBuffer;
import com.alibaba.dubbo.remoting.transport.netty4.NettyChannel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import java.util.List;

public final class NettyCodecAdapter {
    private final io.netty.channel.ChannelHandler encoder = new InternalEncoder();
    private final io.netty.channel.ChannelHandler decoder = new InternalDecoder();
    private final Codec2 codec;
    private final URL url;
    private final ChannelHandler handler;

    public NettyCodecAdapter(Codec2 codec, URL url, ChannelHandler handler) {
        this.codec = codec;
        this.url = url;
        this.handler = handler;
    }

    public io.netty.channel.ChannelHandler getEncoder() {
        return this.encoder;
    }

    public io.netty.channel.ChannelHandler getDecoder() {
        return this.decoder;
    }

    private class InternalDecoder
    extends ByteToMessageDecoder {
        private InternalDecoder() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {
            NettyBackedChannelBuffer message = new NettyBackedChannelBuffer(input);
            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), NettyCodecAdapter.this.url, NettyCodecAdapter.this.handler);
            try {
                do {
                    int saveReaderIndex = message.readerIndex();
                    Object msg = NettyCodecAdapter.this.codec.decode(channel, message);
                    if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                        message.readerIndex(saveReaderIndex);
                        break;
                    }
                    if (saveReaderIndex == message.readerIndex()) {
                        throw new IOException("Decode without read data.");
                    }
                    if (msg == null) continue;
                    out.add(msg);
                } while (message.readable());
            }
            finally {
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        }
    }

    private class InternalEncoder
    extends MessageToByteEncoder {
        private InternalEncoder() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
            NettyBackedChannelBuffer buffer = new NettyBackedChannelBuffer(out);
            io.netty.channel.Channel ch = ctx.channel();
            NettyChannel channel = NettyChannel.getOrAddChannel(ch, NettyCodecAdapter.this.url, NettyCodecAdapter.this.handler);
            try {
                NettyCodecAdapter.this.codec.encode(channel, buffer, msg);
            }
            finally {
                NettyChannel.removeChannelIfDisconnected(ch);
            }
        }
    }

}

