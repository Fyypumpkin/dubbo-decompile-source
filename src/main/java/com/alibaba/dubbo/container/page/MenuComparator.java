/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.container.page;

import com.alibaba.dubbo.container.page.Menu;
import com.alibaba.dubbo.container.page.PageHandler;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Comparator;

public class MenuComparator
implements Comparator<PageHandler>,
Serializable {
    private static final long serialVersionUID = -3161526932904414029L;

    @Override
    public int compare(PageHandler o1, PageHandler o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return o1.equals(o2) ? 0 : (o1.getClass().getAnnotation(Menu.class).order() > o2.getClass().getAnnotation(Menu.class).order() ? 1 : -1);
    }
}

