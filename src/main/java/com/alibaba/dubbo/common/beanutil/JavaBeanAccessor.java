/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.beanutil;

public enum JavaBeanAccessor {
    FIELD,
    METHOD,
    ALL;
    

    public static boolean isAccessByMethod(JavaBeanAccessor accessor) {
        return METHOD.equals((Object)accessor) || ALL.equals((Object)accessor);
    }

    public static boolean isAccessByField(JavaBeanAccessor accessor) {
        return FIELD.equals((Object)accessor) || ALL.equals((Object)accessor);
    }
}

