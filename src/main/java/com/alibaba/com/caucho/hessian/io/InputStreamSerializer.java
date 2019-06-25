/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianOutput;
import com.alibaba.com.caucho.hessian.io.AbstractSerializer;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamSerializer
extends AbstractSerializer {
    @Override
    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
        InputStream is = (InputStream)obj;
        if (is == null) {
            out.writeNull();
        } else {
            int len;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf, 0, buf.length)) > 0) {
                out.writeByteBufferPart(buf, 0, len);
            }
            out.writeByteBufferEnd(buf, 0, 0);
        }
    }
}

