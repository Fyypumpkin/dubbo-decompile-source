/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.Transporter;
import com.alibaba.dubbo.remoting.transport.ChannelHandlerAdapter;
import com.alibaba.dubbo.remoting.transport.ChannelHandlerDispatcher;

public class Transporters {
    public static /* varargs */ Server bind(String url, ChannelHandler ... handler) throws RemotingException {
        return Transporters.bind(URL.valueOf(url), handler);
    }

    public static /* varargs */ Server bind(URL url, ChannelHandler ... handlers) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handlers == null || handlers.length == 0) {
            throw new IllegalArgumentException("handlers == null");
        }
        ChannelHandler handler = handlers.length == 1 ? handlers[0] : new ChannelHandlerDispatcher(handlers);
        return Transporters.getTransporter().bind(url, handler);
    }

    public static /* varargs */ Client connect(String url, ChannelHandler ... handler) throws RemotingException {
        return Transporters.connect(URL.valueOf(url), handler);
    }

    public static /* varargs */ Client connect(URL url, ChannelHandler ... handlers) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        ChannelHandler handler = handlers == null || handlers.length == 0 ? new ChannelHandlerAdapter() : (handlers.length == 1 ? handlers[0] : new ChannelHandlerDispatcher(handlers));
        return Transporters.getTransporter().connect(url, handler);
    }

    public static Transporter getTransporter() {
        return ExtensionLoader.getExtensionLoader(Transporter.class).getAdaptiveExtension();
    }

    private Transporters() {
    }

    static {
        Version.checkDuplicate(Transporters.class);
        Version.checkDuplicate(RemotingException.class);
    }
}

