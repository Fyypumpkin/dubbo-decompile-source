/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianOutput;
import com.alibaba.com.caucho.hessian.io.AbstractSerializer;
import java.io.IOException;

public class StringValueSerializer
extends AbstractSerializer {
    @Override
    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
        if (obj == null) {
            out.writeNull();
        } else {
            if (out.addRef(obj)) {
                return;
            }
            Class<?> cl = obj.getClass();
            int ref = out.writeObjectBegin(cl.getName());
            if (ref < -1) {
                out.writeString("value");
                out.writeString(obj.toString());
                out.writeMapEnd();
            } else {
                if (ref == -1) {
                    out.writeInt(1);
                    out.writeString("value");
                    out.writeObjectBegin(cl.getName());
                }
                out.writeString(obj.toString());
            }
        }
    }
}

