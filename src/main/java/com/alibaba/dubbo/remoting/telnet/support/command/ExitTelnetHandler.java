/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.telnet.support.command;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.telnet.support.Help;

@Activate
@Help(parameter="", summary="Exit the telnet.", detail="Exit the telnet.")
public class ExitTelnetHandler
implements TelnetHandler {
    @Override
    public String telnet(Channel channel, String message) {
        channel.close();
        return null;
    }
}

