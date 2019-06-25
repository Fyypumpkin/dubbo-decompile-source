/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io.java8;

import com.alibaba.com.caucho.hessian.io.HessianHandle;
import java.io.Serializable;
import java.lang.reflect.Method;

public class LocalDateHandle
implements HessianHandle,
Serializable {
    private static final long serialVersionUID = 166018689500019951L;
    private int year;
    private int month;
    private int day;

    public LocalDateHandle() {
    }

    public LocalDateHandle(Object o) {
        try {
            Class<?> c = Class.forName("java.time.LocalDate");
            Method m = c.getDeclaredMethod("getYear", new Class[0]);
            this.year = (Integer)m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getMonthValue", new Class[0]);
            this.month = (Integer)m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getDayOfMonth", new Class[0]);
            this.day = (Integer)m.invoke(o, new Object[0]);
        }
        catch (Throwable c) {
            // empty catch block
        }
    }

    public Object readResolve() {
        try {
            Class<?> c = Class.forName("java.time.LocalDate");
            Method m = c.getDeclaredMethod("of", Integer.TYPE, Integer.TYPE, Integer.TYPE);
            return m.invoke(null, this.year, this.month, this.day);
        }
        catch (Throwable c) {
            return null;
        }
    }
}

