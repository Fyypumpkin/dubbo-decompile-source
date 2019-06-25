/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io.java8;

import com.alibaba.com.caucho.hessian.io.HessianHandle;
import java.io.Serializable;
import java.lang.reflect.Method;

public class MonthDayHandle
implements HessianHandle,
Serializable {
    private static final long serialVersionUID = 5288238558666577745L;
    private int month;
    private int day;

    public MonthDayHandle() {
    }

    public MonthDayHandle(Object o) {
        try {
            Class<?> c = Class.forName("java.time.MonthDay");
            Method m = c.getDeclaredMethod("getMonthValue", new Class[0]);
            this.month = (Integer)m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getDayOfMonth", new Class[0]);
            this.day = (Integer)m.invoke(o, new Object[0]);
        }
        catch (Throwable c) {
            // empty catch block
        }
    }

    private Object readResolve() {
        try {
            Class<?> c = Class.forName("java.time.MonthDay");
            Method m = c.getDeclaredMethod("of", Integer.TYPE, Integer.TYPE);
            return m.invoke(null, this.month, this.day);
        }
        catch (Throwable c) {
            return null;
        }
    }
}

