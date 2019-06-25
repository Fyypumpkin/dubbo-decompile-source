/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianOutput;
import com.alibaba.com.caucho.hessian.io.AbstractSerializer;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapSerializer
extends AbstractSerializer {
    private boolean _isSendJavaType = true;

    public void setSendJavaType(boolean sendJavaType) {
        this._isSendJavaType = sendJavaType;
    }

    public boolean getSendJavaType() {
        return this._isSendJavaType;
    }

    @Override
    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
        if (out.addRef(obj)) {
            return;
        }
        Map map = (Map)obj;
        Class<?> cl = obj.getClass();
        if (cl.equals(HashMap.class) || !this._isSendJavaType || !(obj instanceof Serializable)) {
            out.writeMapBegin(null);
        } else {
            out.writeMapBegin(obj.getClass().getName());
        }
        for (Map.Entry entry : map.entrySet()) {
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
        }
        out.writeMapEnd();
    }
}

