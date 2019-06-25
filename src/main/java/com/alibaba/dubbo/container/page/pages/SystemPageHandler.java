/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.container.page.pages;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.container.page.Menu;
import com.alibaba.dubbo.container.page.Page;
import com.alibaba.dubbo.container.page.PageHandler;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Menu(name="System", desc="Show system environment information.", order=2147473647)
public class SystemPageHandler
implements PageHandler {
    private static final long SECOND = 1000L;
    private static final long MINUTE = 60000L;
    private static final long HOUR = 3600000L;
    private static final long DAY = 86400000L;

    @Override
    public Page handle(URL url) {
        ArrayList<List<String>> rows = new ArrayList<List<String>>();
        ArrayList<String> row = new ArrayList<String>();
        row.add("Version");
        row.add(Version.getVersion(SystemPageHandler.class, "2.0.0"));
        rows.add(row);
        row = new ArrayList();
        row.add("Host");
        String address = NetUtils.getLocalHost();
        row.add(NetUtils.getHostName(address) + "/" + address);
        rows.add(row);
        row = new ArrayList();
        row.add("OS");
        row.add(System.getProperty("os.name") + " " + System.getProperty("os.version"));
        rows.add(row);
        row = new ArrayList();
        row.add("JVM");
        row.add(System.getProperty("java.runtime.name") + " " + System.getProperty("java.runtime.version") + ",<br/>" + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + " " + System.getProperty("java.vm.info", ""));
        rows.add(row);
        row = new ArrayList();
        row.add("CPU");
        row.add(System.getProperty("os.arch", "") + ", " + String.valueOf(Runtime.getRuntime().availableProcessors()) + " cores");
        rows.add(row);
        row = new ArrayList();
        row.add("Locale");
        row.add(Locale.getDefault().toString() + "/" + System.getProperty("file.encoding"));
        rows.add(row);
        row = new ArrayList();
        row.add("Uptime");
        row.add(this.formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()));
        rows.add(row);
        row = new ArrayList();
        row.add("Time");
        row.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").format(new Date()));
        rows.add(row);
        return new Page("System", "System", new String[]{"Property", "Value"}, rows);
    }

    private String formatUptime(long uptime) {
        StringBuilder buf = new StringBuilder();
        if (uptime > 86400000L) {
            long days = (uptime - uptime % 86400000L) / 86400000L;
            buf.append(days);
            buf.append(" Days");
            uptime %= 86400000L;
        }
        if (uptime > 3600000L) {
            long hours = (uptime - uptime % 3600000L) / 3600000L;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(hours);
            buf.append(" Hours");
            uptime %= 3600000L;
        }
        if (uptime > 60000L) {
            long minutes = (uptime - uptime % 60000L) / 60000L;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(minutes);
            buf.append(" Minutes");
            uptime %= 60000L;
        }
        if (uptime > 1000L) {
            long seconds = (uptime - uptime % 1000L) / 1000L;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(seconds);
            buf.append(" Seconds");
            uptime %= 1000L;
        }
        if (uptime > 0L) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(uptime);
            buf.append(" Milliseconds");
        }
        return buf.toString();
    }
}

