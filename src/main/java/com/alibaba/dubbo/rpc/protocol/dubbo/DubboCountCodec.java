/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo;

import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.exchange.support.MultiMessage;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboCodec;
import java.io.IOException;

public final class DubboCountCodec
implements Codec2 {
    private DubboCodec codec = new DubboCodec();

    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object msg) throws IOException {
        this.codec.encode(channel, buffer, msg);
    }

    @Override
    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {
        int save = buffer.readerIndex();
        MultiMessage result = MultiMessage.create();
        do {
            Object obj;
            if (Codec2.DecodeResult.NEED_MORE_INPUT == (obj = this.codec.decode(channel, buffer))) break;
            result.addMessage(obj);
            this.logMessageLength(obj, buffer.readerIndex() - save);
            save = buffer.readerIndex();
        } while (true);
        buffer.readerIndex(save);
        if (result.isEmpty()) {
            return Codec2.DecodeResult.NEED_MORE_INPUT;
        }
        if (result.size() == 1) {
            return result.get(0);
        }
        return result;
    }

    private void logMessageLength(Object result, int bytes) {
        if (bytes <= 0) {
            return;
        }
        if (result instanceof Request) {
            try {
                ((RpcInvocation)((Request)result).getData()).setAttachment("input", String.valueOf(bytes));
            }
            catch (Throwable throwable) {}
        } else if (result instanceof Response) {
            try {
                ((RpcResult)((Response)result).getResult()).setAttachment("output", String.valueOf(bytes));
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }
}

