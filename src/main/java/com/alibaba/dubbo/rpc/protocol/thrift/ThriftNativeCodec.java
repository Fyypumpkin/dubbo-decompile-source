/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.thrift.TException
 *  org.apache.thrift.protocol.TBinaryProtocol
 *  org.apache.thrift.protocol.TMessage
 *  org.apache.thrift.protocol.TProtocol
 *  org.apache.thrift.protocol.TStruct
 *  org.apache.thrift.transport.TIOStreamTransport
 *  org.apache.thrift.transport.TTransport
 */
package com.alibaba.dubbo.rpc.protocol.thrift;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferOutputStream;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.rpc.Invocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;

public class ThriftNativeCodec
implements Codec2 {
    private final AtomicInteger thriftSeq = new AtomicInteger(0);

    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException {
        if (message instanceof Request) {
            this.encodeRequest(channel, buffer, (Request)message);
        } else if (message instanceof Response) {
            this.encodeResponse(channel, buffer, (Response)message);
        } else {
            throw new IOException("Unsupported message type " + message.getClass().getName());
        }
    }

    protected void encodeRequest(Channel channel, ChannelBuffer buffer, Request request) throws IOException {
        Invocation invocation = (Invocation)request.getData();
        TProtocol protocol = ThriftNativeCodec.newProtocol(channel.getUrl(), buffer);
        try {
            protocol.writeMessageBegin(new TMessage(invocation.getMethodName(), 1, this.thriftSeq.getAndIncrement()));
            protocol.writeStructBegin(new TStruct(invocation.getMethodName() + "_args"));
            for (int i = 0; i < invocation.getParameterTypes().length; ++i) {
                Class<?> class_ = invocation.getParameterTypes()[i];
            }
        }
        catch (TException e) {
            throw new IOException(e.getMessage(), (Throwable)e);
        }
    }

    protected void encodeResponse(Channel channel, ChannelBuffer buffer, Response response) throws IOException {
    }

    @Override
    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {
        return null;
    }

    protected static TProtocol newProtocol(URL url, ChannelBuffer buffer) throws IOException {
        String protocol = url.getParameter("thrift.protocol", "binary");
        if ("binary".equals(protocol)) {
            return new TBinaryProtocol((TTransport)new TIOStreamTransport((OutputStream)new ChannelBufferOutputStream(buffer)));
        }
        throw new IOException("Unsupported protocol type " + protocol);
    }
}

