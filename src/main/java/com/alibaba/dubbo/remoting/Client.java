/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.Parameters;
import com.alibaba.dubbo.common.Resetable;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Endpoint;
import com.alibaba.dubbo.remoting.RemotingException;

public interface Client
extends Endpoint,
Channel,
Resetable {
    public void reconnect() throws RemotingException;

    @Deprecated
    public void reset(Parameters var1);
}

