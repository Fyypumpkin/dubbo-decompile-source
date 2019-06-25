/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.mina.common.ByteBuffer
 *  org.apache.mina.common.IoSession
 *  org.apache.mina.common.WriteFuture
 *  org.apache.mina.filter.codec.ProtocolCodecFactory
 *  org.apache.mina.filter.codec.ProtocolDecoder
 *  org.apache.mina.filter.codec.ProtocolDecoderOutput
 *  org.apache.mina.filter.codec.ProtocolEncoder
 *  org.apache.mina.filter.codec.ProtocolEncoderOutput
 */
package com.alibaba.dubbo.remoting.transport.mina;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffers;
import com.alibaba.dubbo.remoting.buffer.DynamicChannelBuffer;
import com.alibaba.dubbo.remoting.transport.mina.MinaChannel;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

final class MinaCodecAdapter
implements ProtocolCodecFactory {
    private final ProtocolEncoder encoder = new InternalEncoder();
    private final ProtocolDecoder decoder = new InternalDecoder();
    private final Codec2 codec;
    private final URL url;
    private final ChannelHandler handler;
    private final int bufferSize;

    public MinaCodecAdapter(Codec2 codec, URL url, ChannelHandler handler) {
        this.codec = codec;
        this.url = url;
        this.handler = handler;
        int b = url.getPositiveParameter("buffer", 8192);
        this.bufferSize = b >= 1024 && b <= 16384 ? b : 8192;
    }

    public ProtocolEncoder getEncoder() {
        return this.encoder;
    }

    public ProtocolDecoder getDecoder() {
        return this.decoder;
    }

    private class InternalDecoder
    implements ProtocolDecoder {
        private ChannelBuffer buffer = ChannelBuffers.EMPTY_BUFFER;

        private InternalDecoder() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void decode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
            ChannelBuffer frame;
            int readable = in.limit();
            if (readable <= 0) {
                return;
            }
            if (this.buffer.readable()) {
                if (this.buffer instanceof DynamicChannelBuffer) {
                    this.buffer.writeBytes(in.buf());
                    frame = this.buffer;
                } else {
                    int size = this.buffer.readableBytes() + in.remaining();
                    frame = ChannelBuffers.dynamicBuffer(size > MinaCodecAdapter.this.bufferSize ? size : MinaCodecAdapter.this.bufferSize);
                    frame.writeBytes(this.buffer, this.buffer.readableBytes());
                    frame.writeBytes(in.buf());
                }
            } else {
                frame = ChannelBuffers.wrappedBuffer(in.buf());
            }
            MinaChannel channel = MinaChannel.getOrAddChannel(session, MinaCodecAdapter.this.url, MinaCodecAdapter.this.handler);
            try {
                do {
                    Object msg;
                    int savedReadIndex = frame.readerIndex();
                    try {
                        msg = MinaCodecAdapter.this.codec.decode(channel, frame);
                    }
                    catch (Exception e) {
                        this.buffer = ChannelBuffers.EMPTY_BUFFER;
                        throw e;
                    }
                    if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                        frame.readerIndex(savedReadIndex);
                        break;
                    }
                    if (savedReadIndex == frame.readerIndex()) {
                        this.buffer = ChannelBuffers.EMPTY_BUFFER;
                        throw new Exception("Decode without read data.");
                    }
                    if (msg == null) continue;
                    out.write(msg);
                } while (frame.readable());
            }
            finally {
                if (frame.readable()) {
                    frame.discardReadBytes();
                    this.buffer = frame;
                } else {
                    this.buffer = ChannelBuffers.EMPTY_BUFFER;
                }
                MinaChannel.removeChannelIfDisconnectd(session);
            }
        }

        public void dispose(IoSession session) throws Exception {
        }

        public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
        }
    }

    private class InternalEncoder
    implements ProtocolEncoder {
        private InternalEncoder() {
        }

        public void dispose(IoSession session) throws Exception {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void encode(IoSession session, Object msg, ProtocolEncoderOutput out) throws Exception {
            ChannelBuffer buffer;
            buffer = ChannelBuffers.dynamicBuffer(1024);
            MinaChannel channel = MinaChannel.getOrAddChannel(session, MinaCodecAdapter.this.url, MinaCodecAdapter.this.handler);
            try {
                MinaCodecAdapter.this.codec.encode(channel, buffer, msg);
            }
            finally {
                MinaChannel.removeChannelIfDisconnectd(session);
            }
            out.write(ByteBuffer.wrap((java.nio.ByteBuffer)buffer.toByteBuffer()));
            out.flush();
        }
    }

}

