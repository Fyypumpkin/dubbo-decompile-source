/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.serialize.Cleanable;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.OptimizedSerialization;
import com.alibaba.dubbo.common.utils.Assert;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec;
import com.alibaba.dubbo.remoting.Decodeable;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.transport.CodecSupport;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.protocol.dubbo.CallbackServiceCodec;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboCodec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class DecodeableRpcInvocation
extends RpcInvocation
implements Codec,
Decodeable {
    private static final Logger log = LoggerFactory.getLogger(DecodeableRpcInvocation.class);
    private Channel channel;
    private byte serializationType;
    private InputStream inputStream;
    private Request request;
    private volatile boolean hasDecoded;

    public DecodeableRpcInvocation(Channel channel, Request request, InputStream is, byte id) {
        Assert.notNull(channel, "channel == null");
        Assert.notNull(request, "request == null");
        Assert.notNull(is, "inputStream == null");
        this.channel = channel;
        this.request = request;
        this.inputStream = is;
        this.serializationType = id;
    }

    @Override
    public void decode() throws Exception {
        if (!this.hasDecoded && this.channel != null && this.inputStream != null) {
            try {
                this.decode(this.channel, this.inputStream);
            }
            catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("Decode rpc invocation failed: " + e.getMessage(), e);
                }
                this.request.setBroken(true);
                this.request.setData(e);
            }
            finally {
                this.hasDecoded = true;
            }
        }
    }

    @Override
    public void encode(Channel channel, OutputStream output, Object message) throws IOException {
        throw new UnsupportedOperationException();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object decode(Channel channel, InputStream input) throws IOException {
        ObjectInput in = CodecSupport.getSerialization(channel.getUrl(), this.serializationType).deserialize(channel.getUrl(), input);
        try {
            this.setAttachment("dubbo", in.readUTF());
            this.setAttachment("path", in.readUTF());
            this.setAttachment("version", in.readUTF());
            this.setMethodName(in.readUTF());
            try {
                Object[] args;
                Class<?>[] pts;
                int argNum = -1;
                if (CodecSupport.getSerialization(channel.getUrl(), this.serializationType) instanceof OptimizedSerialization) {
                    argNum = in.readInt();
                }
                if (argNum >= 0) {
                    if (argNum == 0) {
                        pts = DubboCodec.EMPTY_CLASS_ARRAY;
                        args = DubboCodec.EMPTY_OBJECT_ARRAY;
                    } else {
                        args = new Object[argNum];
                        pts = new Class[argNum];
                        for (int i = 0; i < args.length; ++i) {
                            try {
                                args[i] = in.readObject();
                                pts[i] = args[i].getClass();
                                continue;
                            }
                            catch (Exception e) {
                                if (!log.isWarnEnabled()) continue;
                                log.warn("Decode argument failed: " + e.getMessage(), e);
                            }
                        }
                    }
                } else {
                    String desc = in.readUTF();
                    if (desc.length() == 0) {
                        pts = DubboCodec.EMPTY_CLASS_ARRAY;
                        args = DubboCodec.EMPTY_OBJECT_ARRAY;
                    } else {
                        pts = ReflectUtils.desc2classArray(desc);
                        args = new Object[pts.length];
                        for (int i = 0; i < args.length; ++i) {
                            try {
                                args[i] = in.readObject(pts[i]);
                                continue;
                            }
                            catch (Exception e) {
                                if (!log.isWarnEnabled()) continue;
                                log.warn("Decode argument failed: " + e.getMessage(), e);
                            }
                        }
                    }
                }
                this.setParameterTypes(pts);
                Map map = in.readObject(Map.class);
                if (map != null && map.size() > 0) {
                    Map<String, String> attachment = this.getAttachments();
                    if (attachment == null) {
                        attachment = new HashMap<String, String>();
                    }
                    attachment.putAll(map);
                    this.setAttachments(attachment);
                }
                for (int i = 0; i < args.length; ++i) {
                    args[i] = CallbackServiceCodec.decodeInvocationArgument(channel, this, pts, i, args[i]);
                }
                this.setArguments(args);
            }
            catch (ClassNotFoundException e) {
                throw new IOException(StringUtils.toString("Read invocation data failed.", e));
            }
        }
        finally {
            if (in instanceof Cleanable) {
                ((Cleanable)((Object)in)).cleanup();
            }
        }
        return this;
    }
}

