/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.buffer;

import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import java.nio.ByteBuffer;

public interface ChannelBufferFactory {
    public ChannelBuffer getBuffer(int var1);

    public ChannelBuffer getBuffer(byte[] var1, int var2, int var3);

    public ChannelBuffer getBuffer(ByteBuffer var1);
}

