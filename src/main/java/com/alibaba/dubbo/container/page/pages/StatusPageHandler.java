/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.container.page.pages;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.status.Status;
import com.alibaba.dubbo.common.status.StatusChecker;
import com.alibaba.dubbo.common.status.support.StatusUtils;
import com.alibaba.dubbo.container.page.Menu;
import com.alibaba.dubbo.container.page.Page;
import com.alibaba.dubbo.container.page.PageHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Menu(name="Status", desc="Show system status.", order=2147471647)
public class StatusPageHandler
implements PageHandler {
    @Override
    public Page handle(URL url) {
        ArrayList<List<String>> rows = new ArrayList<List<String>>();
        Set<String> names = ExtensionLoader.getExtensionLoader(StatusChecker.class).getSupportedExtensions();
        HashMap<String, Status> statuses = new HashMap<String, Status>();
        for (String name : names) {
            StatusChecker checker = ExtensionLoader.getExtensionLoader(StatusChecker.class).getExtension(name);
            ArrayList<String> row = new ArrayList<String>();
            row.add(name);
            Status status = checker.check();
            if (status == null || Status.Level.UNKNOWN.equals((Object)status.getLevel())) continue;
            statuses.put(name, status);
            row.add(this.getLevelHtml(status.getLevel()));
            row.add(status.getMessage());
            rows.add(row);
        }
        Status status = StatusUtils.getSummaryStatus(statuses);
        if ("status".equals(url.getPath())) {
            return new Page("", "", "", status.getLevel().toString());
        }
        ArrayList<String> row = new ArrayList<String>();
        row.add("summary");
        row.add(this.getLevelHtml(status.getLevel()));
        row.add("<a href=\"/status\" target=\"_blank\">summary</a>");
        rows.add(row);
        return new Page("Status (<a href=\"/status\" target=\"_blank\">summary</a>)", "Status", new String[]{"Name", "Status", "Description"}, rows);
    }

    private String getLevelHtml(Status.Level level) {
        return "<font color=\"" + this.getLevelColor(level) + "\">" + level.name() + "</font>";
    }

    private String getLevelColor(Status.Level level) {
        if (level == Status.Level.OK) {
            return "green";
        }
        if (level == Status.Level.ERROR) {
            return "red";
        }
        if (level == Status.Level.WARN) {
            return "yellow";
        }
        return "gray";
    }
}

