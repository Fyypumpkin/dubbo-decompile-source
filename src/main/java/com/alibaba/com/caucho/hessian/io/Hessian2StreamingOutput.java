/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import java.io.IOException;
import java.io.OutputStream;

public class Hessian2StreamingOutput {
    private Hessian2Output _out;

    public Hessian2StreamingOutput(OutputStream os) {
        this._out = new Hessian2Output(os);
    }

    public void setCloseStreamOnClose(boolean isClose) {
        this._out.setCloseStreamOnClose(isClose);
    }

    public boolean isCloseStreamOnClose() {
        return this._out.isCloseStreamOnClose();
    }

    public void writeObject(Object object) throws IOException {
        this._out.writeStreamingObject(object);
    }

    public void flush() throws IOException {
        this._out.flush();
    }

    public void close() throws IOException {
        this._out.close();
    }
}

