/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.java;

import com.alibaba.dubbo.common.serialize.support.java.CompactedObjectOutputStream;
import com.alibaba.dubbo.common.serialize.support.nativejava.NativeJavaObjectOutput;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class JavaObjectOutput
extends NativeJavaObjectOutput {
    public JavaObjectOutput(OutputStream os) throws IOException {
        super(new ObjectOutputStream(os));
    }

    public JavaObjectOutput(OutputStream os, boolean compact) throws IOException {
        super(compact ? new CompactedObjectOutputStream(os) : new ObjectOutputStream(os));
    }

    @Override
    public void writeUTF(String v) throws IOException {
        if (v == null) {
            this.getObjectOutputStream().writeInt(-1);
        } else {
            this.getObjectOutputStream().writeInt(v.length());
            this.getObjectOutputStream().writeUTF(v);
        }
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
            this.getObjectOutputStream().writeByte(0);
        } else {
            this.getObjectOutputStream().writeByte(1);
            this.getObjectOutputStream().writeObject(obj);
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        this.getObjectOutputStream().flush();
    }
}

