/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.container.page.pages;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.container.page.Menu;
import com.alibaba.dubbo.container.page.Page;
import com.alibaba.dubbo.container.page.PageHandler;
import com.alibaba.dubbo.container.page.PageServlet;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

@Menu(name="Home", desc="Home page.", order=Integer.MIN_VALUE)
public class HomePageHandler
implements PageHandler {
    @Override
    public Page handle(URL url) {
        ArrayList<List<String>> rows = new ArrayList<List<String>>();
        for (PageHandler handler : PageServlet.getInstance().getMenus()) {
            String uri = ExtensionLoader.getExtensionLoader(PageHandler.class).getExtensionName(handler);
            Menu menu = handler.getClass().getAnnotation(Menu.class);
            ArrayList<String> row = new ArrayList<String>();
            row.add("<a href=\"" + uri + ".html\">" + menu.name() + "</a>");
            row.add(menu.desc());
            rows.add(row);
        }
        return new Page("Home", "Menus", new String[]{"Menu Name", "Menu Desc"}, rows);
    }
}

