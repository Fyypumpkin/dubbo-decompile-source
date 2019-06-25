/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io.java8;

import com.alibaba.com.caucho.hessian.io.HessianHandle;
import java.io.Serializable;
import java.lang.reflect.Method;

public class PeriodHandle
implements HessianHandle,
Serializable {
    private static final long serialVersionUID = 4399720381283781186L;
    private int years;
    private int months;
    private int days;

    public PeriodHandle() {
    }

    public PeriodHandle(Object o) {
        try {
            Class<?> c = Class.forName("java.time.Period");
            Method m = c.getDeclaredMethod("getYears", new Class[0]);
            this.years = (Integer)m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getMonths", new Class[0]);
            this.months = (Integer)m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getDays", new Class[0]);
            this.days = (Integer)m.invoke(o, new Object[0]);
        }
        catch (Throwable c) {
            // empty catch block
        }
    }

    private Object readResolve() {
        try {
            Class<?> c = Class.forName("java.time.Period");
            Method m = c.getDeclaredMethod("of", Integer.TYPE, Integer.TYPE, Integer.TYPE);
            return m.invoke(null, this.years, this.months, this.days);
        }
        catch (Throwable c) {
            return null;
        }
    }
}

