/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io.java8;

import com.alibaba.com.caucho.hessian.io.HessianHandle;
import java.io.Serializable;
import java.lang.reflect.Method;

public class LocalTimeHandle
implements HessianHandle,
Serializable {
    private static final long serialVersionUID = -5892919085390462315L;
    private int hour;
    private int minute;
    private int second;
    private int nano;

    public LocalTimeHandle() {
    }

    public LocalTimeHandle(Object o) {
        try {
            Class<?> c = Class.forName("java.time.LocalTime");
            Method m = c.getDeclaredMethod("getHour", new Class[0]);
            this.hour = (Integer)m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getMinute", new Class[0]);
            this.minute = (Integer)m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getSecond", new Class[0]);
            this.second = (Integer)m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getNano", new Class[0]);
            this.nano = (Integer)m.invoke(o, new Object[0]);
        }
        catch (Throwable c) {
            // empty catch block
        }
    }

    private Object readResolve() {
        try {
            Class<?> c = Class.forName("java.time.LocalTime");
            Method m = c.getDeclaredMethod("of", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            return m.invoke(null, this.hour, this.minute, this.second, this.nano);
        }
        catch (Throwable c) {
            return null;
        }
    }
}

