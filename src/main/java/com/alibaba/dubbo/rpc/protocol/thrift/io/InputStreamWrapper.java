/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.thrift.io;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapper
extends InputStream {
    private InputStream is;

    public InputStreamWrapper(InputStream is) {
        if (is == null) {
            throw new NullPointerException("is == null");
        }
        this.is = is;
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (this.is.available() >= b.length) {
            return this.is.read(b);
        }
        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.is.available() >= len) {
            return this.is.read(b, off, len);
        }
        return -1;
    }

    @Override
    public long skip(long n) throws IOException {
        return this.is.skip(n);
    }

    @Override
    public int available() throws IOException {
        return this.is.available();
    }

    @Override
    public void close() throws IOException {
        this.is.close();
    }

    @Override
    public void mark(int readlimit) {
        this.is.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        this.is.reset();
    }

    @Override
    public boolean markSupported() {
        return this.is.markSupported();
    }

    @Override
    public int read() throws IOException {
        if (this.is.available() >= 1) {
            return this.is.read();
        }
        return -1;
    }
}

