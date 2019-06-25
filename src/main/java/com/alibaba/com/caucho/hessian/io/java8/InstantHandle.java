/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io.java8;

import com.alibaba.com.caucho.hessian.io.HessianHandle;
import java.io.Serializable;
import java.lang.reflect.Method;

public class InstantHandle
implements HessianHandle,
Serializable {
    private static final long serialVersionUID = -4367309317780077156L;
    private long seconds;
    private int nanos;

    public InstantHandle() {
    }

    public InstantHandle(Object o) {
        try {
            Class<?> c = Class.forName("java.time.Instant");
            Method m = c.getDeclaredMethod("getEpochSecond", new Class[0]);
            this.seconds = (Long)m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getNano", new Class[0]);
            this.nanos = (Integer)m.invoke(o, new Object[0]);
        }
        catch (Throwable c) {
            // empty catch block
        }
    }

    private Object readResolve() {
        try {
            Class<?> c = Class.forName("java.time.Instant");
            Method m = c.getDeclaredMethod("ofEpochSecond", Long.TYPE, Long.TYPE);
            return m.invoke(null, this.seconds, this.nanos);
        }
        catch (Throwable c) {
            return null;
        }
    }
}

