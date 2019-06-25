/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.telnet.support.command;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.telnet.support.Help;

@Activate
@Help(parameter="[lines]", summary="Clear screen.", detail="Clear screen.")
public class ClearTelnetHandler
implements TelnetHandler {
    @Override
    public String telnet(Channel channel, String message) {
        int lines = 100;
        if (message.length() > 0) {
            if (!StringUtils.isInteger(message)) {
                return "Illegal lines " + message + ", must be integer.";
            }
            lines = Integer.parseInt(message);
        }
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < lines; ++i) {
            buf.append("\r\n");
        }
        return buf.toString();
    }
}

