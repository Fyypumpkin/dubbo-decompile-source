/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;
import java.net.InetSocketAddress;

public class TimeoutException
extends RemotingException {
    private static final long serialVersionUID = 3122966731958222692L;
    public static final int CLIENT_SIDE = 0;
    public static final int SERVER_SIDE = 1;
    private final int phase;

    public TimeoutException(boolean serverSide, Channel channel, String message) {
        super(channel, message);
        this.phase = serverSide ? 1 : 0;
    }

    public TimeoutException(boolean serverSide, InetSocketAddress localAddress, InetSocketAddress remoteAddress, String message) {
        super(localAddress, remoteAddress, message);
        this.phase = serverSide ? 1 : 0;
    }

    public int getPhase() {
        return this.phase;
    }

    public boolean isServerSide() {
        return this.phase == 1;
    }

    public boolean isClientSide() {
        return this.phase == 0;
    }
}

