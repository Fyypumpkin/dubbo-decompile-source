/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.serialize.Cleanable;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.utils.Assert;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec;
import com.alibaba.dubbo.remoting.Decodeable;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.transport.CodecSupport;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public class DecodeableRpcResult
extends RpcResult
implements Codec,
Decodeable {
    private static final Logger log = LoggerFactory.getLogger(DecodeableRpcResult.class);
    private Channel channel;
    private byte serializationType;
    private InputStream inputStream;
    private Response response;
    private Invocation invocation;
    private volatile boolean hasDecoded;

    public DecodeableRpcResult(Channel channel, Response response, InputStream is, Invocation invocation, byte id) {
        Assert.notNull(channel, "channel == null");
        Assert.notNull(response, "response == null");
        Assert.notNull(is, "inputStream == null");
        this.channel = channel;
        this.response = response;
        this.inputStream = is;
        this.invocation = invocation;
        this.serializationType = id;
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
            byte flag = in.readByte();
            switch (flag) {
                case 2: {
                    break;
                }
                case 1: {
                    try {
                        Type[] returnType = RpcUtils.getReturnTypes(this.invocation);
                        this.setValue(returnType == null || returnType.length == 0 ? in.readObject() : (returnType.length == 1 ? in.readObject((Class)returnType[0]) : in.readObject((Class)returnType[0], returnType[1])));
                        break;
                    }
                    catch (ClassNotFoundException e) {
                        throw new IOException(StringUtils.toString("Read response data failed.", e));
                    }
                }
                case 0: {
                    try {
                        Object obj = in.readObject();
                        if (!(obj instanceof Throwable)) {
                            throw new IOException("Response data error, expect Throwable, but get " + obj);
                        }
                        this.setException((Throwable)obj);
                        break;
                    }
                    catch (ClassNotFoundException e) {
                        throw new IOException(StringUtils.toString("Read response data failed.", e));
                    }
                }
                default: {
                    throw new IOException("Unknown result flag, expect '0' '1' '2', get " + flag);
                }
            }
            DecodeableRpcResult e = this;
            return e;
        }
        finally {
            if (in instanceof Cleanable) {
                ((Cleanable)((Object)in)).cleanup();
            }
        }
    }

    @Override
    public void decode() throws Exception {
        if (!this.hasDecoded && this.channel != null && this.inputStream != null) {
            try {
                this.decode(this.channel, this.inputStream);
            }
            catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("Decode rpc result failed: " + e.getMessage(), e);
                }
                this.response.setStatus((byte)90);
                this.response.setErrorMessage(StringUtils.toString(e));
            }
            finally {
                this.hasDecoded = true;
            }
        }
    }
}

