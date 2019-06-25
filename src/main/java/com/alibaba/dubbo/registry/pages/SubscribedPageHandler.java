/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.pages;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.container.page.Page;
import com.alibaba.dubbo.container.page.PageHandler;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.support.AbstractRegistry;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SubscribedPageHandler
implements PageHandler {
    @Override
    public Page handle(URL url) {
        Set<URL> services;
        String registryAddress = url.getParameter("registry", "");
        ArrayList<List<String>> rows = new ArrayList<List<String>>();
        Collection<Registry> registries = AbstractRegistryFactory.getRegistries();
        StringBuilder select = new StringBuilder();
        Registry registry = null;
        if (registries != null && registries.size() > 0) {
            if (registries.size() == 1) {
                registry = registries.iterator().next();
                select.append(" &gt; " + registry.getUrl().getAddress());
            } else {
                select.append(" &gt; <select onchange=\"window.location.href='subscribed.html?registry=' + this.value;\">");
                for (Registry r : registries) {
                    String sp = r.getUrl().getAddress();
                    select.append("<option value=\">");
                    select.append(sp);
                    if ((registryAddress == null || registryAddress.length() == 0) && registry == null || registryAddress.equals(sp)) {
                        registry = r;
                        select.append("\" selected=\"selected");
                    }
                    select.append("\">");
                    select.append(sp);
                    select.append("</option>");
                }
                select.append("</select>");
            }
        }
        if (registry instanceof AbstractRegistry && (services = ((AbstractRegistry)registry).getSubscribed().keySet()) != null && services.size() > 0) {
            for (URL u : services) {
                ArrayList<String> row = new ArrayList<String>();
                row.add(u.toFullString().replace("<", "&lt;").replace(">", "&gt;"));
                rows.add(row);
            }
        }
        return new Page("<a href=\"registries.html\">Registries</a>" + select.toString() + " &gt; <a href=\"registered.html?registry=" + registryAddress + "\">Registered</a> | Subscribed", "Subscribed (" + rows.size() + ")", new String[]{"Consumer URL:"}, rows);
    }
}

