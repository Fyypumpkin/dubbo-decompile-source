/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io.java8;

import com.alibaba.com.caucho.hessian.io.HessianHandle;
import java.io.Serializable;
import java.lang.reflect.Method;

public class ZoneOffsetHandle
implements HessianHandle,
Serializable {
    private static final long serialVersionUID = 8841589723587858789L;
    private int seconds;

    public ZoneOffsetHandle() {
    }

    public ZoneOffsetHandle(Object o) {
        try {
            Class<?> c = Class.forName("java.time.ZoneOffset");
            Method m = c.getDeclaredMethod("getTotalSeconds", new Class[0]);
            this.seconds = (Integer)m.invoke(o, new Object[0]);
        }
        catch (Throwable c) {
            // empty catch block
        }
    }

    private Object readResolve() {
        try {
            Class<?> c = Class.forName("java.time.ZoneOffset");
            Method m = c.getDeclaredMethod("ofTotalSeconds", Integer.TYPE);
            return m.invoke(null, this.seconds);
        }
        catch (Throwable c) {
            return null;
        }
    }
}

