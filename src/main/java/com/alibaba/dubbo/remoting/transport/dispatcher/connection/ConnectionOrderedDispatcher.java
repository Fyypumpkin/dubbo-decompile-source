/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport.dispatcher.connection;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Dispatcher;
import com.alibaba.dubbo.remoting.transport.dispatcher.connection.ConnectionOrderedChannelHandler;

public class ConnectionOrderedDispatcher
implements Dispatcher {
    public static final String NAME = "connection";

    @Override
    public ChannelHandler dispatch(ChannelHandler handler, URL url) {
        return new ConnectionOrderedChannelHandler(handler, url);
    }
}

