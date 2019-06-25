/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.Parameters;
import com.alibaba.dubbo.common.Resetable;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Endpoint;
import java.net.InetSocketAddress;
import java.util.Collection;

public interface Server
extends Endpoint,
Resetable {
    public boolean isBound();

    public Collection<Channel> getChannels();

    public Channel getChannel(InetSocketAddress var1);

    @Deprecated
    public void reset(Parameters var1);
}

