/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.telnet;

import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;

@SPI
public interface TelnetHandler {
    public String telnet(Channel var1, String var2) throws RemotingException;
}

