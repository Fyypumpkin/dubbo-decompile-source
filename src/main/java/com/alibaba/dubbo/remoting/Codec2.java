/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import java.io.IOException;

@SPI
public interface Codec2 {
    @Adaptive(value={"codec"})
    public void encode(Channel var1, ChannelBuffer var2, Object var3) throws IOException;

    @Adaptive(value={"codec"})
    public Object decode(Channel var1, ChannelBuffer var2) throws IOException;

    public static enum DecodeResult {
        NEED_MORE_INPUT,
        SKIP_SOME_INPUT;
        
    }

}

