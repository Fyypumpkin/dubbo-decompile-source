/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.remoting.ChannelHandler;

@SPI(value="all")
public interface Dispatcher {
    @Adaptive(value={"dispatcher", "dispather", "channel.handler"})
    public ChannelHandler dispatch(ChannelHandler var1, URL var2);
}

