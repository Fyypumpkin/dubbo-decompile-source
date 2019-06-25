/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport.codec;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.Cleanable;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferInputStream;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferOutputStream;
import com.alibaba.dubbo.remoting.transport.AbstractCodec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TransportCodec
extends AbstractCodec {
    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException {
        ChannelBufferOutputStream output = new ChannelBufferOutputStream(buffer);
        ObjectOutput objectOutput = this.getSerialization(channel).serialize(channel.getUrl(), output);
        this.encodeData(channel, objectOutput, message);
        objectOutput.flushBuffer();
        if (objectOutput instanceof Cleanable) {
            ((Cleanable)((Object)objectOutput)).cleanup();
        }
    }

    @Override
    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {
        ChannelBufferInputStream input = new ChannelBufferInputStream(buffer);
        ObjectInput objectInput = this.getSerialization(channel).deserialize(channel.getUrl(), input);
        Object object = this.decodeData(channel, objectInput);
        if (objectInput instanceof Cleanable) {
            ((Cleanable)((Object)objectInput)).cleanup();
        }
        return object;
    }

    protected void encodeData(Channel channel, ObjectOutput output, Object message) throws IOException {
        this.encodeData(output, message);
    }

    protected Object decodeData(Channel channel, ObjectInput input) throws IOException {
        return this.decodeData(input);
    }

    protected void encodeData(ObjectOutput output, Object message) throws IOException {
        output.writeObject(message);
    }

    protected Object decodeData(ObjectInput input) throws IOException {
        try {
            return input.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException("ClassNotFoundException: " + StringUtils.toString(e));
        }
    }
}

