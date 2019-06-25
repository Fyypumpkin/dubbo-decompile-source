/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianOutput;
import com.alibaba.com.caucho.hessian.io.AbstractSerializer;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class CollectionSerializer
extends AbstractSerializer {
    private boolean _sendJavaType = true;

    public void setSendJavaType(boolean sendJavaType) {
        this._sendJavaType = sendJavaType;
    }

    public boolean getSendJavaType() {
        return this._sendJavaType;
    }

    @Override
    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
        if (out.addRef(obj)) {
            return;
        }
        Collection list = (Collection)obj;
        Class<?> cl = obj.getClass();
        boolean hasEnd = cl.equals(ArrayList.class) || !this._sendJavaType || !Serializable.class.isAssignableFrom(cl) ? out.writeListBegin(list.size(), null) : out.writeListBegin(list.size(), obj.getClass().getName());
        for (Object value : list) {
            out.writeObject(value);
        }
        if (hasEnd) {
            out.writeListEnd();
        }
    }
}

