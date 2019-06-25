/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.codec;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.io.Bytes;
import com.alibaba.dubbo.common.io.StreamUtils;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.serialize.Cleanable;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferInputStream;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferOutputStream;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.exchange.support.DefaultFuture;
import com.alibaba.dubbo.remoting.telnet.codec.TelnetCodec;
import com.alibaba.dubbo.remoting.transport.CodecSupport;
import com.alibaba.dubbo.remoting.transport.ExceedPayloadLimitException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExchangeCodec
extends TelnetCodec {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeCodec.class);
    protected static final int HEADER_LENGTH = 16;
    protected static final short MAGIC = -9541;
    protected static final byte MAGIC_HIGH = Bytes.short2bytes((short)-9541)[0];
    protected static final byte MAGIC_LOW = Bytes.short2bytes((short)-9541)[1];
    protected static final byte FLAG_REQUEST = -128;
    protected static final byte FLAG_TWOWAY = 64;
    protected static final byte FLAG_EVENT = 32;
    protected static final int SERIALIZATION_MASK = 31;

    public Short getMagicCode() {
        return (short)-9541;
    }

    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object msg) throws IOException {
        if (msg instanceof Request) {
            this.encodeRequest(channel, buffer, (Request)msg);
        } else if (msg instanceof Response) {
            this.encodeResponse(channel, buffer, (Response)msg);
        } else {
            super.encode(channel, buffer, msg);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("the resulting byte size of encoding is " + buffer.readableBytes());
        }
    }

    @Override
    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {
        int readable = buffer.readableBytes();
        byte[] header = new byte[Math.min(readable, 16)];
        buffer.readBytes(header);
        return this.decode(channel, buffer, readable, header);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected Object decode(Channel channel, ChannelBuffer buffer, int readable, byte[] header) throws IOException {
        if (readable > 0 && header[0] != MAGIC_HIGH || readable > 1 && header[1] != MAGIC_LOW) {
            int length = header.length;
            if (header.length < readable) {
                header = Bytes.copyOf(header, readable);
                buffer.readBytes(header, length, readable - length);
            }
            for (int i = 1; i < header.length - 1; ++i) {
                if (header[i] != MAGIC_HIGH || header[i + 1] != MAGIC_LOW) continue;
                buffer.readerIndex(buffer.readerIndex() - header.length + i);
                header = Bytes.copyOf(header, i);
                break;
            }
            return super.decode(channel, buffer, readable, header);
        }
        if (readable < 16) {
            return Codec2.DecodeResult.NEED_MORE_INPUT;
        }
        int len = Bytes.bytes2int(header, 12);
        ExchangeCodec.checkPayload(channel, len);
        int tt = len + 16;
        if (readable < tt) {
            return Codec2.DecodeResult.NEED_MORE_INPUT;
        }
        ChannelBufferInputStream is = new ChannelBufferInputStream(buffer, len);
        try {
            Object object = this.decodeBody(channel, is, header);
            return object;
        }
        finally {
            if (is.available() > 0) {
                try {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Skip input stream " + is.available());
                    }
                    StreamUtils.skipUnusedStream(is);
                }
                catch (IOException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
    }

    protected Object decodeBody(Channel channel, InputStream is, byte[] header) throws IOException {
        byte flag = header[2];
        byte proto = (byte)(flag & 31);
        Serialization s = CodecSupport.getSerialization(channel.getUrl(), proto);
        ObjectInput in = s.deserialize(channel.getUrl(), is);
        long id = Bytes.bytes2long(header, 4);
        if ((flag & -128) == 0) {
            Response res = new Response(id);
            if ((flag & 32) != 0) {
                res.setEvent(Response.HEARTBEAT_EVENT);
            }
            byte status = header[3];
            res.setStatus(status);
            if (status == 20) {
                try {
                    Object data = res.isHeartbeat() ? this.decodeHeartbeatData(channel, in) : (res.isEvent() ? this.decodeEventData(channel, in) : this.decodeResponseData(channel, in, this.getRequestData(id)));
                    res.setResult(data);
                }
                catch (Throwable t) {
                    res.setStatus((byte)90);
                    res.setErrorMessage(StringUtils.toString(t));
                }
            } else {
                res.setErrorMessage(in.readUTF());
            }
            return res;
        }
        Request req = new Request(id);
        req.setVersion("2.0.0");
        req.setTwoWay((flag & 64) != 0);
        if ((flag & 32) != 0) {
            req.setEvent(Request.HEARTBEAT_EVENT);
        }
        try {
            Object data = req.isHeartbeat() ? this.decodeHeartbeatData(channel, in) : (req.isEvent() ? this.decodeEventData(channel, in) : this.decodeRequestData(channel, in));
            req.setData(data);
        }
        catch (Throwable t) {
            req.setBroken(true);
            req.setData(t);
        }
        return req;
    }

    protected Object getRequestData(long id) {
        DefaultFuture future = DefaultFuture.getFuture(id);
        if (future == null) {
            return null;
        }
        Request req = future.getRequest();
        if (req == null) {
            return null;
        }
        return req.getData();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void encodeRequest(Channel channel, ChannelBuffer buffer, Request req) throws IOException {
        ChannelBufferOutputStream bos;
        byte[] header;
        int savedWriteIndex;
        Serialization serialization = this.getSerialization(channel);
        header = new byte[16];
        Bytes.short2bytes((short)-9541, header);
        header[2] = (byte)(-128 | serialization.getContentTypeId());
        if (req.isTwoWay()) {
            byte[] arrby = header;
            arrby[2] = (byte)(arrby[2] | 64);
        }
        if (req.isEvent()) {
            byte[] arrby = header;
            arrby[2] = (byte)(arrby[2] | 32);
        }
        Bytes.long2bytes(req.getId(), header, 4);
        savedWriteIndex = buffer.writerIndex();
        buffer.writerIndex(savedWriteIndex + 16);
        bos = new ChannelBufferOutputStream(buffer);
        ObjectOutput out = serialization.serialize(channel.getUrl(), bos);
        try {
            if (req.isEvent()) {
                this.encodeEventData(channel, out, req.getData());
            } else {
                this.encodeRequestData(channel, out, req.getData());
            }
            out.flushBuffer();
        }
        finally {
            if (out instanceof Cleanable) {
                ((Cleanable)((Object)out)).cleanup();
            }
        }
        bos.flush();
        bos.close();
        int len = bos.writtenBytes();
        ExchangeCodec.checkPayload(channel, len, req);
        Bytes.int2bytes(len, header, 12);
        buffer.writerIndex(savedWriteIndex);
        buffer.writeBytes(header);
        buffer.writerIndex(savedWriteIndex + 16 + len);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void encodeResponse(Channel channel, ChannelBuffer buffer, Response res) throws IOException {
        int savedWriteIndex = buffer.writerIndex();
        try {
            byte[] header;
            ChannelBufferOutputStream bos;
            byte status;
            Serialization serialization = this.getSerialization(channel);
            header = new byte[16];
            Bytes.short2bytes((short)-9541, header);
            header[2] = serialization.getContentTypeId();
            if (res.isHeartbeat()) {
                byte[] arrby = header;
                arrby[2] = (byte)(arrby[2] | 32);
            }
            header[3] = status = res.getStatus();
            Bytes.long2bytes(res.getId(), header, 4);
            buffer.writerIndex(savedWriteIndex + 16);
            bos = new ChannelBufferOutputStream(buffer);
            ObjectOutput out = serialization.serialize(channel.getUrl(), bos);
            try {
                if (status == 20) {
                    if (res.isHeartbeat()) {
                        this.encodeHeartbeatData(channel, out, res.getResult());
                    } else {
                        this.encodeResponseData(channel, out, res.getResult());
                    }
                } else {
                    out.writeUTF(res.getErrorMessage());
                }
                out.flushBuffer();
            }
            finally {
                if (out instanceof Cleanable) {
                    ((Cleanable)((Object)out)).cleanup();
                }
            }
            bos.flush();
            bos.close();
            int len = bos.writtenBytes();
            ExchangeCodec.checkPayload(channel, len, res);
            Bytes.int2bytes(len, header, 12);
            buffer.writerIndex(savedWriteIndex);
            buffer.writeBytes(header);
            buffer.writerIndex(savedWriteIndex + 16 + len);
        }
        catch (Throwable t) {
            buffer.writerIndex(savedWriteIndex);
            if (!res.isEvent() && res.getStatus() != 50) {
                Response r = new Response(res.getId(), res.getVersion());
                r.setStatus((byte)50);
                if (t instanceof ExceedPayloadLimitException) {
                    logger.warn(t.getMessage(), t);
                    try {
                        r.setErrorMessage(t.getMessage());
                        channel.send(r);
                        return;
                    }
                    catch (RemotingException e) {
                        logger.warn("Failed to send bad_response info back: " + t.getMessage() + ", cause: " + e.getMessage(), e);
                    }
                } else {
                    logger.warn("Fail to encode response: " + res + ", send bad_response info instead, cause: " + t.getMessage(), t);
                    try {
                        r.setErrorMessage("Failed to send response: " + res + ", cause: " + StringUtils.toString(t));
                        channel.send(r);
                        return;
                    }
                    catch (RemotingException e) {
                        logger.warn("Failed to send bad_response info back: " + res + ", cause: " + e.getMessage(), e);
                    }
                }
            }
            if (t instanceof IOException) {
                throw (IOException)t;
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            }
            if (t instanceof Error) {
                throw (Error)t;
            }
            throw new RuntimeException(t.getMessage(), t);
        }
    }

    @Override
    protected Object decodeData(ObjectInput in) throws IOException {
        return this.decodeRequestData(in);
    }

    @Deprecated
    protected Object decodeHeartbeatData(ObjectInput in) throws IOException {
        try {
            return in.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    protected Object decodeRequestData(ObjectInput in) throws IOException {
        try {
            return in.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    protected Object decodeResponseData(ObjectInput in) throws IOException {
        try {
            return in.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    @Override
    protected void encodeData(ObjectOutput out, Object data) throws IOException {
        this.encodeRequestData(out, data);
    }

    private void encodeEventData(ObjectOutput out, Object data) throws IOException {
        out.writeObject(data);
    }

    @Deprecated
    protected void encodeHeartbeatData(ObjectOutput out, Object data) throws IOException {
        this.encodeEventData(out, data);
    }

    protected void encodeRequestData(ObjectOutput out, Object data) throws IOException {
        out.writeObject(data);
    }

    protected void encodeResponseData(ObjectOutput out, Object data) throws IOException {
        out.writeObject(data);
    }

    @Override
    protected Object decodeData(Channel channel, ObjectInput in) throws IOException {
        return this.decodeRequestData(channel, in);
    }

    protected Object decodeEventData(Channel channel, ObjectInput in) throws IOException {
        try {
            return in.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    @Deprecated
    protected Object decodeHeartbeatData(Channel channel, ObjectInput in) throws IOException {
        try {
            return in.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read object failed.", e));
        }
    }

    protected Object decodeRequestData(Channel channel, ObjectInput in) throws IOException {
        return this.decodeRequestData(in);
    }

    protected Object decodeResponseData(Channel channel, ObjectInput in) throws IOException {
        return this.decodeResponseData(in);
    }

    protected Object decodeResponseData(Channel channel, ObjectInput in, Object requestData) throws IOException {
        return this.decodeResponseData(channel, in);
    }

    @Override
    protected void encodeData(Channel channel, ObjectOutput out, Object data) throws IOException {
        this.encodeRequestData(channel, out, data);
    }

    private void encodeEventData(Channel channel, ObjectOutput out, Object data) throws IOException {
        this.encodeEventData(out, data);
    }

    @Deprecated
    protected void encodeHeartbeatData(Channel channel, ObjectOutput out, Object data) throws IOException {
        this.encodeHeartbeatData(out, data);
    }

    protected void encodeRequestData(Channel channel, ObjectOutput out, Object data) throws IOException {
        this.encodeRequestData(out, data);
    }

    protected void encodeResponseData(Channel channel, ObjectOutput out, Object data) throws IOException {
        this.encodeResponseData(out, data);
    }
}

