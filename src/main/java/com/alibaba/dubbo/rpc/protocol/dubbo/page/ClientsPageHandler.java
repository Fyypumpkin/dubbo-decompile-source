/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo.page;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.container.page.Page;
import com.alibaba.dubbo.container.page.PageHandler;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ClientsPageHandler
implements PageHandler {
    @Override
    public Page handle(URL url) {
        String port = url.getParameter("port");
        int p = port == null || port.length() == 0 ? 0 : Integer.parseInt(port);
        Collection<ExchangeServer> servers = DubboProtocol.getDubboProtocol().getServers();
        ExchangeServer server = null;
        StringBuilder select = new StringBuilder();
        if (servers != null && servers.size() > 0) {
            if (servers.size() == 1) {
                server = servers.iterator().next();
                String address = server.getUrl().getAddress();
                select.append(" &gt; " + NetUtils.getHostName(address) + "/" + (String)address);
            } else {
                select.append(" &gt; <select onchange=\"window.location.href='clients.html?port=' + this.value;\">");
                for (ExchangeServer s : servers) {
                    int sp = s.getUrl().getPort();
                    select.append("<option value=\">");
                    select.append(sp);
                    if (p == 0 && server == null || p == sp) {
                        server = s;
                        select.append("\" selected=\"selected");
                    }
                    select.append("\">");
                    select.append(s.getUrl().getAddress());
                    select.append("</option>");
                }
                select.append("</select>");
            }
        }
        ArrayList<List<String>> rows = new ArrayList<List<String>>();
        if (server != null) {
            Collection<ExchangeChannel> channels = server.getExchangeChannels();
            for (ExchangeChannel c : channels) {
                ArrayList<String> row = new ArrayList<String>();
                String address = NetUtils.toAddressString(c.getRemoteAddress());
                row.add(NetUtils.getHostName(address) + "/" + address);
                rows.add(row);
            }
        }
        return new Page("<a href=\"servers.html\">Servers</a>" + select.toString() + " &gt; Clients", "Clients (" + rows.size() + ")", new String[]{"Client Address:"}, rows);
    }
}

