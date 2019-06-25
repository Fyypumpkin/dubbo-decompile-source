/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.io;

import java.io.IOException;
import java.io.Reader;

public class UnsafeStringReader
extends Reader {
    private String mString;
    private int mPosition;
    private int mLimit;
    private int mMark;

    public UnsafeStringReader(String str) {
        this.mString = str;
        this.mLimit = str.length();
        this.mMark = 0;
        this.mPosition = 0;
    }

    @Override
    public int read() throws IOException {
        this.ensureOpen();
        if (this.mPosition >= this.mLimit) {
            return -1;
        }
        return this.mString.charAt(this.mPosition++);
    }

    @Override
    public int read(char[] cs, int off, int len) throws IOException {
        this.ensureOpen();
        if (off < 0 || off > cs.length || len < 0 || off + len > cs.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        if (this.mPosition >= this.mLimit) {
            return -1;
        }
        int n = Math.min(this.mLimit - this.mPosition, len);
        this.mString.getChars(this.mPosition, this.mPosition + n, cs, off);
        this.mPosition += n;
        return n;
    }

    @Override
    public long skip(long ns) throws IOException {
        this.ensureOpen();
        if (this.mPosition >= this.mLimit) {
            return 0L;
        }
        long n = Math.min((long)(this.mLimit - this.mPosition), ns);
        n = Math.max((long)(-this.mPosition), n);
        this.mPosition = (int)((long)this.mPosition + n);
        return n;
    }

    @Override
    public boolean ready() throws IOException {
        this.ensureOpen();
        return true;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        this.ensureOpen();
        this.mMark = this.mPosition;
    }

    @Override
    public void reset() throws IOException {
        this.ensureOpen();
        this.mPosition = this.mMark;
    }

    @Override
    public void close() throws IOException {
        this.mString = null;
    }

    private void ensureOpen() throws IOException {
        if (this.mString == null) {
            throw new IOException("Stream closed");
        }
    }
}

