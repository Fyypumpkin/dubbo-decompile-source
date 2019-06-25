/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.telnet.support.command;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.status.Status;
import com.alibaba.dubbo.common.status.StatusChecker;
import com.alibaba.dubbo.common.status.support.StatusUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.telnet.support.Help;
import com.alibaba.dubbo.remoting.telnet.support.TelnetUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@Activate
@Help(parameter="[-l]", summary="Show status.", detail="Show status.")
public class StatusTelnetHandler
implements TelnetHandler {
    private final ExtensionLoader<StatusChecker> extensionLoader = ExtensionLoader.getExtensionLoader(StatusChecker.class);

    @Override
    public String telnet(Channel channel, String message) {
        if (message.equals("-l")) {
            List<StatusChecker> checkers = this.extensionLoader.getActivateExtension(channel.getUrl(), "status");
            String[] header = new String[]{"resource", "status", "message"};
            ArrayList<List<String>> table = new ArrayList<List<String>>();
            HashMap<String, Status> statuses = new HashMap<String, Status>();
            if (checkers != null && checkers.size() > 0) {
                for (StatusChecker checker : checkers) {
                    Status stat;
                    String name = this.extensionLoader.getExtensionName(checker);
                    try {
                        stat = checker.check();
                    }
                    catch (Throwable t) {
                        stat = new Status(Status.Level.ERROR, t.getMessage());
                    }
                    statuses.put(name, stat);
                    if (stat.getLevel() == null || stat.getLevel() == Status.Level.UNKNOWN) continue;
                    ArrayList<String> row = new ArrayList<String>();
                    row.add(name);
                    row.add(String.valueOf((Object)stat.getLevel()));
                    row.add(stat.getMessage() == null ? "" : stat.getMessage());
                    table.add(row);
                }
            }
            Status stat = StatusUtils.getSummaryStatus(statuses);
            ArrayList<String> row = new ArrayList<String>();
            row.add("summary");
            row.add(String.valueOf((Object)stat.getLevel()));
            row.add(stat.getMessage());
            table.add(row);
            return TelnetUtils.toTable(header, table);
        }
        if (message.length() > 0) {
            return "Unsupported parameter " + message + " for status.";
        }
        String status = channel.getUrl().getParameter("status");
        HashMap<String, Status> statuses = new HashMap<String, Status>();
        if (status != null && status.length() > 0) {
            String[] ss;
            for (String s : ss = Constants.COMMA_SPLIT_PATTERN.split(status)) {
                Status stat;
                StatusChecker handler = this.extensionLoader.getExtension(s);
                try {
                    stat = handler.check();
                }
                catch (Throwable t) {
                    stat = new Status(Status.Level.ERROR, t.getMessage());
                }
                statuses.put(s, stat);
            }
        }
        Status stat = StatusUtils.getSummaryStatus(statuses);
        return String.valueOf((Object)stat.getLevel());
    }
}

