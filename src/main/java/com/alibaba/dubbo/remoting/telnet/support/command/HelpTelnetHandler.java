/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.telnet.support.command;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.telnet.support.Help;
import com.alibaba.dubbo.remoting.telnet.support.TelnetUtils;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

@Activate
@Help(parameter="[command]", summary="Show help.", detail="Show help.")
public class HelpTelnetHandler
implements TelnetHandler {
    private final ExtensionLoader<TelnetHandler> extensionLoader = ExtensionLoader.getExtensionLoader(TelnetHandler.class);

    @Override
    public String telnet(Channel channel, String message) {
        if (message.length() > 0) {
            if (!this.extensionLoader.hasExtension(message)) {
                return "No such command " + message;
            }
            TelnetHandler handler = this.extensionLoader.getExtension(message);
            Help help = handler.getClass().getAnnotation(Help.class);
            StringBuilder buf = new StringBuilder();
            buf.append("Command:\r\n    ");
            buf.append(message + " " + help.parameter().replace("\r\n", " ").replace("\n", " "));
            buf.append("\r\nSummary:\r\n    ");
            buf.append(help.summary().replace("\r\n", " ").replace("\n", " "));
            buf.append("\r\nDetail:\r\n    ");
            buf.append(help.detail().replace("\r\n", "    \r\n").replace("\n", "    \n"));
            return buf.toString();
        }
        ArrayList<List<String>> table = new ArrayList<List<String>>();
        List<TelnetHandler> handlers = this.extensionLoader.getActivateExtension(channel.getUrl(), "telnet");
        if (handlers != null && handlers.size() > 0) {
            for (TelnetHandler handler : handlers) {
                Help help = handler.getClass().getAnnotation(Help.class);
                ArrayList<String> row = new ArrayList<String>();
                String parameter = " " + this.extensionLoader.getExtensionName(handler) + " " + (help != null ? help.parameter().replace("\r\n", " ").replace("\n", " ") : "");
                row.add(parameter.length() > 50 ? parameter.substring(0, 50) + "..." : parameter);
                String summary = help != null ? help.summary().replace("\r\n", " ").replace("\n", " ") : "";
                row.add(summary.length() > 50 ? summary.substring(0, 50) + "..." : summary);
                table.add(row);
            }
        }
        return "Please input \"help [command]\" show detail.\r\n" + TelnetUtils.toList(table);
    }
}

