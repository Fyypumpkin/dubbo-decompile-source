/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.HessianProtocolException;
import java.io.IOException;
import java.io.InputStream;

public class Hessian2StreamingInput {
    private Hessian2Input _in;

    public Hessian2StreamingInput(InputStream is) {
        this._in = new Hessian2Input(new StreamingInputStream(is));
    }

    public Object readObject() throws IOException {
        return this._in.readStreamingObject();
    }

    public void close() throws IOException {
        this._in.close();
    }

    static class StreamingInputStream
    extends InputStream {
        private InputStream _is;
        private int _length;

        StreamingInputStream(InputStream is) {
            this._is = is;
        }

        @Override
        public int read() throws IOException {
            InputStream is = this._is;
            while (this._length == 0) {
                int code = is.read();
                if (code < 0) {
                    return -1;
                }
                if (code != 112 && code != 80) {
                    throw new HessianProtocolException("expected streaming packet at 0x" + Integer.toHexString(code & 255));
                }
                int d1 = is.read();
                int d2 = is.read();
                if (d2 < 0) {
                    return -1;
                }
                this._length = (d1 << 8) + d2;
            }
            --this._length;
            return is.read();
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            InputStream is = this._is;
            while (this._length == 0) {
                int code = is.read();
                if (code < 0) {
                    return -1;
                }
                if (code != 112 && code != 80) {
                    throw new HessianProtocolException("expected streaming packet at 0x" + Integer.toHexString(code & 255) + " (" + (char)code + ")");
                }
                int d1 = is.read();
                int d2 = is.read();
                if (d2 < 0) {
                    return -1;
                }
                this._length = (d1 << 8) + d2;
            }
            int sublen = this._length;
            if (length < sublen) {
                sublen = length;
            }
            if ((sublen = is.read(buffer, offset, sublen)) < 0) {
                return -1;
            }
            this._length -= sublen;
            return sublen;
        }
    }

}

