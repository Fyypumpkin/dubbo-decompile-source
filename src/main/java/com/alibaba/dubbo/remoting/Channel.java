/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.remoting.Endpoint;
import java.net.InetSocketAddress;

public interface Channel
extends Endpoint {
    public InetSocketAddress getRemoteAddress();

    public boolean isConnected();

    public boolean hasAttribute(String var1);

    public Object getAttribute(String var1);

    public void setAttribute(String var1, Object var2);

    public void removeAttribute(String var1);
}

