/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.remoting.ChannelHandler;

public interface ChannelHandlerDelegate
extends ChannelHandler {
    public ChannelHandler getHandler();
}

