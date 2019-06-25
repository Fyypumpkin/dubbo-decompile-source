/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianOutput;
import com.alibaba.com.caucho.hessian.io.AbstractSerializer;
import java.io.IOException;
import java.util.Iterator;

public class IteratorSerializer
extends AbstractSerializer {
    private static IteratorSerializer _serializer;

    public static IteratorSerializer create() {
        if (_serializer == null) {
            _serializer = new IteratorSerializer();
        }
        return _serializer;
    }

    @Override
    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
        Iterator iter = (Iterator)obj;
        boolean hasEnd = out.writeListBegin(-1, null);
        while (iter.hasNext()) {
            Object value = iter.next();
            out.writeObject(value);
        }
        if (hasEnd) {
            out.writeListEnd();
        }
    }
}

