/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io.java8;

import com.alibaba.com.caucho.hessian.io.HessianHandle;
import java.io.Serializable;
import java.lang.reflect.Method;

public class OffsetDateTimeHandle
implements HessianHandle,
Serializable {
    private static final long serialVersionUID = -7823900532640515312L;
    private Object dateTime;
    private Object offset;

    public OffsetDateTimeHandle() {
    }

    public OffsetDateTimeHandle(Object o) {
        try {
            Class<?> c = Class.forName("java.time.OffsetDateTime");
            Method m = c.getDeclaredMethod("toLocalDateTime", new Class[0]);
            this.dateTime = m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getOffset", new Class[0]);
            this.offset = m.invoke(o, new Object[0]);
        }
        catch (Throwable c) {
            // empty catch block
        }
    }

    private Object readResolve() {
        try {
            Class<?> c = Class.forName("java.time.OffsetDateTime");
            Method m = c.getDeclaredMethod("of", Class.forName("java.time.LocalDateTime"), Class.forName("java.time.ZoneOffset"));
            return m.invoke(null, this.dateTime, this.offset);
        }
        catch (Throwable c) {
            return null;
        }
    }
}

