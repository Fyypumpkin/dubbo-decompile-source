/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.extension.support;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.extension.SPI;
import java.lang.annotation.Annotation;
import java.util.Comparator;

public class ActivateComparator
implements Comparator<Object> {
    public static final Comparator<Object> COMPARATOR = new ActivateComparator();

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        if (o1.equals(o2)) {
            return 0;
        }
        Activate a1 = o1.getClass().getAnnotation(Activate.class);
        Activate a2 = o2.getClass().getAnnotation(Activate.class);
        if ((a1.before().length > 0 || a1.after().length > 0 || a2.before().length > 0 || a2.after().length > 0) && o1.getClass().getInterfaces().length > 0 && o1.getClass().getInterfaces()[0].isAnnotationPresent(SPI.class)) {
            ExtensionLoader<?> extensionLoader = ExtensionLoader.getExtensionLoader(o1.getClass().getInterfaces()[0]);
            if (a1.before().length > 0 || a1.after().length > 0) {
                String n2 = extensionLoader.getExtensionName(o2.getClass());
                for (String before : a1.before()) {
                    if (!before.equals(n2)) continue;
                    return -1;
                }
                for (String after : a1.after()) {
                    if (!after.equals(n2)) continue;
                    return 1;
                }
            }
            if (a2.before().length > 0 || a2.after().length > 0) {
                String n1 = extensionLoader.getExtensionName(o1.getClass());
                for (String before : a2.before()) {
                    if (!before.equals(n1)) continue;
                    return 1;
                }
                for (String after : a2.after()) {
                    if (!after.equals(n1)) continue;
                    return -1;
                }
            }
        }
        int n1 = a1 == null ? 0 : a1.order();
        int n2 = a2 == null ? 0 : a2.order();
        return n1 > n2 ? 1 : -1;
    }
}

