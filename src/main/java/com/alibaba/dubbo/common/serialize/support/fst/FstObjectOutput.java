/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  de.ruedigermoeller.serialization.FSTObjectOutput
 */
package com.alibaba.dubbo.common.serialize.support.fst;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.support.fst.FstFactory;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import java.io.IOException;
import java.io.OutputStream;

public class FstObjectOutput
implements ObjectOutput {
    private FSTObjectOutput output;

    public FstObjectOutput(OutputStream outputStream) {
        this.output = FstFactory.getDefaultFactory().getObjectOutput(outputStream);
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        this.output.writeBoolean(v);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        this.output.writeByte((int)v);
    }

    @Override
    public void writeShort(short v) throws IOException {
        this.output.writeShort((int)v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.output.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.output.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.output.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.output.writeDouble(v);
    }

    @Override
    public void writeBytes(byte[] v) throws IOException {
        if (v == null) {
            this.output.writeInt(-1);
        } else {
            this.writeBytes(v, 0, v.length);
        }
    }

    @Override
    public void writeBytes(byte[] v, int off, int len) throws IOException {
        if (v == null) {
            this.output.writeInt(-1);
        } else {
            this.output.writeInt(len);
            this.output.write(v, off, len);
        }
    }

    @Override
    public void writeUTF(String v) throws IOException {
        this.output.writeUTF(v);
    }

    @Override
    public void writeObject(Object v) throws IOException {
        this.output.writeObject(v);
    }

    @Override
    public void flushBuffer() throws IOException {
        this.output.flush();
    }
}

