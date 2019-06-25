/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo.telnet;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.telnet.support.Help;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import java.net.InetSocketAddress;
import java.util.Collection;

@Activate
@Help(parameter="[-l] [port]", summary="Print server ports and connections.", detail="Print server ports and connections.")
public class PortTelnetHandler
implements TelnetHandler {
    @Override
    public String telnet(Channel channel, String message) {
        StringBuilder buf = new StringBuilder();
        String port = null;
        boolean detail = false;
        if (message.length() > 0) {
            String[] parts = message.split("\\s+");
            for (String part : parts) {
                if ("-l".equals(part)) {
                    detail = true;
                    continue;
                }
                if (!StringUtils.isInteger(part)) {
                    return "Illegal port " + part + ", must be integer.";
                }
                port = part;
            }
        }
        if (port == null || port.length() == 0) {
            for (ExchangeServer server : DubboProtocol.getDubboProtocol().getServers()) {
                if (buf.length() > 0) {
                    buf.append("\r\n");
                }
                if (detail) {
                    buf.append(server.getUrl().getProtocol() + "://" + server.getUrl().getAddress());
                    continue;
                }
                buf.append(server.getUrl().getPort());
            }
        } else {
            ExchangeServer server;
            int p = Integer.parseInt(port);
            server = null;
            for (ExchangeServer s : DubboProtocol.getDubboProtocol().getServers()) {
                if (p != s.getUrl().getPort()) continue;
                server = s;
                break;
            }
            if (server != null) {
                Collection<ExchangeChannel> channels = server.getExchangeChannels();
                for (ExchangeChannel c : channels) {
                    if (buf.length() > 0) {
                        buf.append("\r\n");
                    }
                    if (detail) {
                        buf.append(c.getRemoteAddress() + " -> " + c.getLocalAddress());
                        continue;
                    }
                    buf.append(c.getRemoteAddress());
                }
            } else {
                buf.append("No such port " + port);
            }
        }
        return buf.toString();
    }
}

