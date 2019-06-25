/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.io;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
    private StreamUtils() {
    }

    public static InputStream limitedInputStream(final InputStream is, final int limit) throws IOException {
        return new InputStream(){
            private int mPosition = 0;
            private int mMark = 0;
            private int mLimit = Math.min(limit, is.available());

            @Override
            public int read() throws IOException {
                if (this.mPosition < this.mLimit) {
                    ++this.mPosition;
                    return is.read();
                }
                return -1;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (b == null) {
                    throw new NullPointerException();
                }
                if (off < 0 || len < 0 || len > b.length - off) {
                    throw new IndexOutOfBoundsException();
                }
                if (this.mPosition >= this.mLimit) {
                    return -1;
                }
                if (this.mPosition + len > this.mLimit) {
                    len = this.mLimit - this.mPosition;
                }
                if (len <= 0) {
                    return 0;
                }
                is.read(b, off, len);
                this.mPosition += len;
                return len;
            }

            @Override
            public long skip(long len) throws IOException {
                if ((long)this.mPosition + len > (long)this.mLimit) {
                    len = this.mLimit - this.mPosition;
                }
                if (len <= 0L) {
                    return 0L;
                }
                is.skip(len);
                this.mPosition = (int)((long)this.mPosition + len);
                return len;
            }

            @Override
            public int available() {
                return this.mLimit - this.mPosition;
            }

            @Override
            public boolean markSupported() {
                return is.markSupported();
            }

            @Override
            public void mark(int readlimit) {
                is.mark(readlimit);
                this.mMark = this.mPosition;
            }

            @Override
            public void reset() throws IOException {
                is.reset();
                this.mPosition = this.mMark;
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    public static InputStream markSupportedInputStream(final InputStream is, final int markBufferSize) {
        if (is.markSupported()) {
            return is;
        }
        return new InputStream(){
            byte[] mMarkBuffer;
            boolean mInMarked = false;
            boolean mInReset = false;
            private int mPosition = 0;
            private int mCount = 0;
            boolean mDry = false;

            @Override
            public int read() throws IOException {
                if (!this.mInMarked) {
                    return is.read();
                }
                if (this.mPosition < this.mCount) {
                    byte b = this.mMarkBuffer[this.mPosition++];
                    return b & 255;
                }
                if (!this.mInReset) {
                    if (this.mDry) {
                        return -1;
                    }
                    if (null == this.mMarkBuffer) {
                        this.mMarkBuffer = new byte[markBufferSize];
                    }
                    if (this.mPosition >= markBufferSize) {
                        throw new IOException("Mark buffer is full!");
                    }
                    int read = is.read();
                    if (-1 == read) {
                        this.mDry = true;
                        return -1;
                    }
                    this.mMarkBuffer[this.mPosition++] = (byte)read;
                    ++this.mCount;
                    return read;
                }
                this.mInMarked = false;
                this.mInReset = false;
                this.mPosition = 0;
                this.mCount = 0;
                return is.read();
            }

            @Override
            public synchronized void mark(int readlimit) {
                this.mInMarked = true;
                this.mInReset = false;
                int count = this.mCount - this.mPosition;
                if (count > 0) {
                    System.arraycopy(this.mMarkBuffer, this.mPosition, this.mMarkBuffer, 0, count);
                    this.mCount = count;
                    this.mPosition = 0;
                }
            }

            @Override
            public synchronized void reset() throws IOException {
                if (!this.mInMarked) {
                    throw new IOException("should mark befor reset!");
                }
                this.mInReset = true;
                this.mPosition = 0;
            }

            @Override
            public boolean markSupported() {
                return true;
            }

            @Override
            public int available() throws IOException {
                int available = is.available();
                if (this.mInMarked && this.mInReset) {
                    available += this.mCount - this.mPosition;
                }
                return available;
            }
        };
    }

    public static InputStream markSupportedInputStream(InputStream is) {
        return StreamUtils.markSupportedInputStream(is, 1024);
    }

    public static void skipUnusedStream(InputStream is) throws IOException {
        if (is.available() > 0) {
            is.skip(is.available());
        }
    }

}

