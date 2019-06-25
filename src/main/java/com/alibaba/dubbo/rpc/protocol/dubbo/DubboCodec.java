/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.io.Bytes;
import com.alibaba.dubbo.common.io.UnsafeByteArrayInputStream;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.OptimizedSerialization;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.exchange.codec.ExchangeCodec;
import com.alibaba.dubbo.remoting.transport.CodecSupport;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.protocol.dubbo.CallbackServiceCodec;
import com.alibaba.dubbo.rpc.protocol.dubbo.DecodeableRpcInvocation;
import com.alibaba.dubbo.rpc.protocol.dubbo.DecodeableRpcResult;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class DubboCodec
extends ExchangeCodec
implements Codec2 {
    private static final Logger log = LoggerFactory.getLogger(DubboCodec.class);
    public static final String NAME = "dubbo";
    public static final String DUBBO_VERSION = Version.getVersion(DubboCodec.class, Version.getVersion());
    public static final byte RESPONSE_WITH_EXCEPTION = 0;
    public static final byte RESPONSE_VALUE = 1;
    public static final byte RESPONSE_NULL_VALUE = 2;
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    @Override
    protected Object decodeBody(Channel channel, InputStream is, byte[] header) throws IOException {
        byte flag = header[2];
        byte proto = (byte)(flag & 31);
        Serialization s = CodecSupport.getSerialization(channel.getUrl(), proto);
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
                    Object data;
                    if (res.isHeartbeat()) {
                        data = this.decodeHeartbeatData(channel, this.deserialize(s, channel.getUrl(), is));
                    } else if (res.isEvent()) {
                        data = this.decodeEventData(channel, this.deserialize(s, channel.getUrl(), is));
                    } else {
                        DecodeableRpcResult result;
                        if (channel.getUrl().getParameter("decode.in.io", true)) {
                            result = new DecodeableRpcResult(channel, res, is, (Invocation)this.getRequestData(id), proto);
                            result.decode();
                        } else {
                            result = new DecodeableRpcResult(channel, res, new UnsafeByteArrayInputStream(this.readMessageData(is)), (Invocation)this.getRequestData(id), proto);
                        }
                        data = result;
                    }
                    res.setResult(data);
                }
                catch (Throwable t) {
                    if (log.isWarnEnabled()) {
                        log.warn("Decode response failed: " + t.getMessage(), t);
                    }
                    res.setStatus((byte)90);
                    res.setErrorMessage(StringUtils.toString(t));
                }
            } else {
                res.setErrorMessage(this.deserialize(s, channel.getUrl(), is).readUTF());
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
            Object data;
            if (req.isHeartbeat()) {
                data = null;
                if (is.available() > 0) {
                    data = this.decodeHeartbeatData(channel, this.deserialize(s, channel.getUrl(), is));
                }
            } else if (req.isEvent()) {
                data = this.decodeEventData(channel, this.deserialize(s, channel.getUrl(), is));
            } else {
                DecodeableRpcInvocation inv;
                if (channel.getUrl().getParameter("decode.in.io", true)) {
                    inv = new DecodeableRpcInvocation(channel, req, is, proto);
                    inv.decode();
                } else {
                    inv = new DecodeableRpcInvocation(channel, req, new UnsafeByteArrayInputStream(this.readMessageData(is)), proto);
                }
                this.attachInvocation(channel, inv, req);
                data = inv;
            }
            req.setData(data);
        }
        catch (Throwable t) {
            if (log.isWarnEnabled()) {
                log.warn("Decode request failed: " + t.getMessage(), t);
            }
            req.setBroken(true);
            req.setData(t);
        }
        return req;
    }

    private ObjectInput deserialize(Serialization serialization, URL url, InputStream is) throws IOException {
        return serialization.deserialize(url, is);
    }

    private byte[] readMessageData(InputStream is) throws IOException {
        if (is.available() > 0) {
            byte[] result = new byte[is.available()];
            is.read(result);
            return result;
        }
        return new byte[0];
    }

    @Override
    protected void encodeRequestData(Channel channel, ObjectOutput out, Object data) throws IOException {
        RpcInvocation inv = (RpcInvocation)data;
        out.writeUTF(inv.getAttachment(NAME, DUBBO_VERSION));
        out.writeUTF(inv.getAttachment("path"));
        out.writeUTF(inv.getAttachment("version"));
        out.writeUTF(inv.getMethodName());
        if (this.getSerialization(channel) instanceof OptimizedSerialization) {
            if (!this.containComplexArguments(inv)) {
                out.writeInt(inv.getParameterTypes().length);
            } else {
                out.writeInt(-1);
            }
        } else {
            out.writeUTF(ReflectUtils.getDesc(inv.getParameterTypes()));
        }
        Object[] args = inv.getArguments();
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                out.writeObject(CallbackServiceCodec.encodeInvocationArgument(channel, inv, i));
            }
        }
        out.writeObject(inv.getAttachments());
    }

    @Override
    protected void encodeResponseData(Channel channel, ObjectOutput out, Object data) throws IOException {
        Result result = (Result)data;
        Throwable th = result.getException();
        if (th == null) {
            Object ret = result.getValue();
            if (ret == null) {
                out.writeByte((byte)2);
            } else {
                out.writeByte((byte)1);
                out.writeObject(ret);
            }
        } else {
            out.writeByte((byte)0);
            out.writeObject(th);
        }
    }

    private boolean containComplexArguments(RpcInvocation invocation) {
        for (int i = 0; i < invocation.getParameterTypes().length; ++i) {
            if (invocation.getArguments()[i] != null && invocation.getParameterTypes()[i] == invocation.getArguments()[i].getClass()) continue;
            return true;
        }
        return false;
    }

    protected void attachInvocation(Channel channel, Invocation requestData, Request req) {
    }
}

