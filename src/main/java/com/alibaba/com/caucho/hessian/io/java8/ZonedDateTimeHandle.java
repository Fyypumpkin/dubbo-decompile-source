/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io.java8;

import com.alibaba.com.caucho.hessian.io.HessianHandle;
import java.io.Serializable;
import java.lang.reflect.Method;

public class ZonedDateTimeHandle
implements HessianHandle,
Serializable {
    private static final long serialVersionUID = -6933460123278647569L;
    private Object dateTime;
    private Object offset;
    private String zoneId;

    public ZonedDateTimeHandle() {
    }

    public ZonedDateTimeHandle(Object o) {
        try {
            Class<?> c = Class.forName("java.time.ZonedDateTime");
            Method m = c.getDeclaredMethod("toLocalDateTime", new Class[0]);
            this.dateTime = m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getOffset", new Class[0]);
            this.offset = m.invoke(o, new Object[0]);
            m = c.getDeclaredMethod("getZone", new Class[0]);
            Object zone = m.invoke(o, new Object[0]);
            if (zone != null) {
                Class<?> zoneId = Class.forName("java.time.ZoneId");
                m = zoneId.getDeclaredMethod("getId", new Class[0]);
                this.zoneId = (String)m.invoke(zone, new Object[0]);
            }
        }
        catch (Throwable c) {
            // empty catch block
        }
    }

    private Object readResolve() {
        try {
            Class<?> zoneDateTime = Class.forName("java.time.ZonedDateTime");
            Method ofLocal = zoneDateTime.getDeclaredMethod("ofLocal", Class.forName("java.time.LocalDateTime"), Class.forName("java.time.ZoneId"), Class.forName("java.time.ZoneOffset"));
            Class<?> c = Class.forName("java.time.ZoneId");
            Method of = c.getDeclaredMethod("of", String.class);
            return ofLocal.invoke(null, this.dateTime, of.invoke(null, this.zoneId), this.offset);
        }
        catch (Throwable zoneDateTime) {
            return null;
        }
    }
}

