/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.pages;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.container.page.Menu;
import com.alibaba.dubbo.container.page.Page;
import com.alibaba.dubbo.container.page.PageHandler;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.support.AbstractRegistry;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Menu(name="Registries", desc="Show connected registries.", order=10000)
public class RegistriesPageHandler
implements PageHandler {
    @Override
    public Page handle(URL url) {
        ArrayList<List<String>> rows = new ArrayList<List<String>>();
        Collection<Registry> registries = AbstractRegistryFactory.getRegistries();
        int registeredCount = 0;
        int subscribedCount = 0;
        if (registries != null && registries.size() > 0) {
            for (Registry registry : registries) {
                String server = registry.getUrl().getAddress();
                ArrayList<String> row = new ArrayList<String>();
                row.add(NetUtils.getHostName(server) + "/" + server);
                if (registry.isAvailable()) {
                    row.add("<font color=\"green\">Connected</font>");
                } else {
                    row.add("<font color=\"red\">Disconnected</font>");
                }
                int registeredSize = 0;
                int subscribedSize = 0;
                if (registry instanceof AbstractRegistry) {
                    registeredSize = ((AbstractRegistry)registry).getRegistered().size();
                    registeredCount += registeredSize;
                    subscribedSize = ((AbstractRegistry)registry).getSubscribed().size();
                    subscribedCount += subscribedSize;
                }
                row.add("<a href=\"registered.html?registry=" + server + "\">Registered(" + registeredSize + ")</a>");
                row.add("<a href=\"subscribed.html?registry=" + server + "\">Subscribed(" + subscribedSize + ")</a>");
                rows.add(row);
            }
        }
        return new Page("Registries", "Registries (" + rows.size() + ")", new String[]{"Registry Address:", "Status", "Registered(" + registeredCount + ")", "Subscribed(" + subscribedCount + ")"}, rows);
    }
}

