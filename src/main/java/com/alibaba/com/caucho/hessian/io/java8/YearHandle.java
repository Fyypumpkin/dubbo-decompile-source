/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io.java8;

import com.alibaba.com.caucho.hessian.io.HessianHandle;
import java.io.Serializable;
import java.lang.reflect.Method;

public class YearHandle
implements HessianHandle,
Serializable {
    private static final long serialVersionUID = -6299552890287487926L;
    private int year;

    public YearHandle() {
    }

    public YearHandle(Object o) {
        try {
            Class<?> c = Class.forName("java.time.Year");
            Method m = c.getDeclaredMethod("getValue", new Class[0]);
            this.year = (Integer)m.invoke(o, new Object[0]);
        }
        catch (Throwable c) {
            // empty catch block
        }
    }

    private Object readResolve() {
        try {
            Class<?> c = Class.forName("java.time.Year");
            Method m = c.getDeclaredMethod("of", Integer.TYPE);
            return m.invoke(null, this.year);
        }
        catch (Throwable c) {
            return null;
        }
    }
}

