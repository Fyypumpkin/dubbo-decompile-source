/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io.java8;

import com.alibaba.com.caucho.hessian.io.HessianHandle;
import java.io.Serializable;
import java.lang.reflect.Method;

public class ZoneIdHandle
implements HessianHandle,
Serializable {
    private static final long serialVersionUID = 8789182864066905552L;
    private String zoneId;

    public ZoneIdHandle() {
    }

    public ZoneIdHandle(Object o) {
        try {
            Class<?> c = Class.forName("java.time.ZoneId");
            Method m = c.getDeclaredMethod("getId", new Class[0]);
            this.zoneId = (String)m.invoke(o, new Object[0]);
        }
        catch (Throwable c) {
            // empty catch block
        }
    }

    private Object readResolve() {
        try {
            Class<?> c = Class.forName("java.time.ZoneId");
            Method m = c.getDeclaredMethod("of", String.class);
            return m.invoke(null, this.zoneId);
        }
        catch (Throwable c) {
            return null;
        }
    }
}

