/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.dubbo;

import com.alibaba.dubbo.common.serialize.DataOutput;
import com.alibaba.dubbo.common.serialize.support.dubbo.GenericDataFlags;
import java.io.IOException;
import java.io.OutputStream;

public class GenericDataOutput
implements DataOutput,
GenericDataFlags {
    private static final int CHAR_BUF_SIZE = 256;
    private final byte[] mBuffer;
    private final byte[] mTemp = new byte[9];
    private final char[] mCharBuf = new char[256];
    private final OutputStream mOutput;
    private final int mLimit;
    private int mPosition = 0;

    public GenericDataOutput(OutputStream out) {
        this(out, 1024);
    }

    public GenericDataOutput(OutputStream out, int buffSize) {
        this.mOutput = out;
        this.mLimit = buffSize;
        this.mBuffer = new byte[buffSize];
    }

    @Override
    public void writeBool(boolean v) throws IOException {
        this.write0(v ? (byte)26 : 25);
    }

    @Override
    public void writeByte(byte v) throws IOException {
        switch (v) {
            case 0: {
                this.write0((byte)25);
                break;
            }
            case 1: {
                this.write0((byte)26);
                break;
            }
            case 2: {
                this.write0((byte)27);
                break;
            }
            case 3: {
                this.write0((byte)28);
                break;
            }
            case 4: {
                this.write0((byte)29);
                break;
            }
            case 5: {
                this.write0((byte)30);
                break;
            }
            case 6: {
                this.write0((byte)31);
                break;
            }
            case 7: {
                this.write0((byte)32);
                break;
            }
            case 8: {
                this.write0((byte)33);
                break;
            }
            case 9: {
                this.write0((byte)34);
                break;
            }
            case 10: {
                this.write0((byte)35);
                break;
            }
            case 11: {
                this.write0((byte)36);
                break;
            }
            case 12: {
                this.write0((byte)37);
                break;
            }
            case 13: {
                this.write0((byte)38);
                break;
            }
            case 14: {
                this.write0((byte)39);
                break;
            }
            case 15: {
                this.write0((byte)40);
                break;
            }
            case 16: {
                this.write0((byte)41);
                break;
            }
            case 17: {
                this.write0((byte)42);
                break;
            }
            case 18: {
                this.write0((byte)43);
                break;
            }
            case 19: {
                this.write0((byte)44);
                break;
            }
            case 20: {
                this.write0((byte)45);
                break;
            }
            case 21: {
                this.write0((byte)46);
                break;
            }
            case 22: {
                this.write0((byte)47);
                break;
            }
            case 23: {
                this.write0((byte)48);
                break;
            }
            case 24: {
                this.write0((byte)49);
                break;
            }
            case 25: {
                this.write0((byte)50);
                break;
            }
            case 26: {
                this.write0((byte)51);
                break;
            }
            case 27: {
                this.write0((byte)52);
                break;
            }
            case 28: {
                this.write0((byte)53);
                break;
            }
            case 29: {
                this.write0((byte)54);
                break;
            }
            case 30: {
                this.write0((byte)55);
                break;
            }
            case 31: {
                this.write0((byte)56);
                break;
            }
            default: {
                this.write0((byte)0);
                this.write0(v);
            }
        }
    }

    @Override
    public void writeShort(short v) throws IOException {
        this.writeVarint32(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.writeVarint32(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.writeVarint64(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.writeVarint32(Float.floatToRawIntBits(v));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.writeVarint64(Double.doubleToRawLongBits(v));
    }

    @Override
    public void writeUTF(String v) throws IOException {
        if (v == null) {
            this.write0((byte)-108);
        } else {
            int len = v.length();
            if (len == 0) {
                this.write0((byte)-107);
            } else {
                int size;
                this.write0((byte)-125);
                this.writeUInt(len);
                int off = 0;
                int limit = this.mLimit - 3;
                char[] buf = this.mCharBuf;
                do {
                    size = Math.min(len - off, 256);
                    v.getChars(off, off + size, buf, 0);
                    for (int i = 0; i < size; ++i) {
                        char c = buf[i];
                        if (this.mPosition > limit) {
                            if (c < '') {
                                this.write0((byte)c);
                                continue;
                            }
                            if (c < '\u0800') {
                                this.write0((byte)(192 | c >> 6 & 31));
                                this.write0((byte)(128 | c & 63));
                                continue;
                            }
                            this.write0((byte)(224 | c >> 12 & 15));
                            this.write0((byte)(128 | c >> 6 & 63));
                            this.write0((byte)(128 | c & 63));
                            continue;
                        }
                        if (c < '') {
                            this.mBuffer[this.mPosition++] = (byte)c;
                            continue;
                        }
                        if (c < '\u0800') {
                            this.mBuffer[this.mPosition++] = (byte)(192 | c >> 6 & 31);
                            this.mBuffer[this.mPosition++] = (byte)(128 | c & 63);
                            continue;
                        }
                        this.mBuffer[this.mPosition++] = (byte)(224 | c >> 12 & 15);
                        this.mBuffer[this.mPosition++] = (byte)(128 | c >> 6 & 63);
                        this.mBuffer[this.mPosition++] = (byte)(128 | c & 63);
                    }
                } while ((off += size) < len);
            }
        }
    }

    @Override
    public void writeBytes(byte[] b) throws IOException {
        if (b == null) {
            this.write0((byte)-108);
        } else {
            this.writeBytes(b, 0, b.length);
        }
    }

    @Override
    public void writeBytes(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            this.write0((byte)-107);
        } else {
            this.write0((byte)-125);
            this.writeUInt(len);
            this.write0(b, off, len);
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        if (this.mPosition > 0) {
            this.mOutput.write(this.mBuffer, 0, this.mPosition);
            this.mPosition = 0;
        }
    }

    public void writeUInt(int v) throws IOException {
        do {
            byte tmp = (byte)(v & 127);
            if ((v >>>= 7) == 0) {
                this.write0((byte)(tmp | 128));
                return;
            }
            this.write0(tmp);
        } while (true);
    }

    protected void write0(byte b) throws IOException {
        if (this.mPosition == this.mLimit) {
            this.flushBuffer();
        }
        this.mBuffer[this.mPosition++] = b;
    }

    protected void write0(byte[] b, int off, int len) throws IOException {
        int rem = this.mLimit - this.mPosition;
        if (rem > len) {
            System.arraycopy(b, off, this.mBuffer, this.mPosition, len);
            this.mPosition += len;
        } else {
            System.arraycopy(b, off, this.mBuffer, this.mPosition, rem);
            this.mPosition = this.mLimit;
            this.flushBuffer();
            off += rem;
            if (this.mLimit > (len -= rem)) {
                System.arraycopy(b, off, this.mBuffer, 0, len);
                this.mPosition = len;
            } else {
                this.mOutput.write(b, off, len);
            }
        }
    }

    private void writeVarint32(int v) throws IOException {
        switch (v) {
            case -15: {
                this.write0((byte)10);
                break;
            }
            case -14: {
                this.write0((byte)11);
                break;
            }
            case -13: {
                this.write0((byte)12);
                break;
            }
            case -12: {
                this.write0((byte)13);
                break;
            }
            case -11: {
                this.write0((byte)14);
                break;
            }
            case -10: {
                this.write0((byte)15);
                break;
            }
            case -9: {
                this.write0((byte)16);
                break;
            }
            case -8: {
                this.write0((byte)17);
                break;
            }
            case -7: {
                this.write0((byte)18);
                break;
            }
            case -6: {
                this.write0((byte)19);
                break;
            }
            case -5: {
                this.write0((byte)20);
                break;
            }
            case -4: {
                this.write0((byte)21);
                break;
            }
            case -3: {
                this.write0((byte)22);
                break;
            }
            case -2: {
                this.write0((byte)23);
                break;
            }
            case -1: {
                this.write0((byte)24);
                break;
            }
            case 0: {
                this.write0((byte)25);
                break;
            }
            case 1: {
                this.write0((byte)26);
                break;
            }
            case 2: {
                this.write0((byte)27);
                break;
            }
            case 3: {
                this.write0((byte)28);
                break;
            }
            case 4: {
                this.write0((byte)29);
                break;
            }
            case 5: {
                this.write0((byte)30);
                break;
            }
            case 6: {
                this.write0((byte)31);
                break;
            }
            case 7: {
                this.write0((byte)32);
                break;
            }
            case 8: {
                this.write0((byte)33);
                break;
            }
            case 9: {
                this.write0((byte)34);
                break;
            }
            case 10: {
                this.write0((byte)35);
                break;
            }
            case 11: {
                this.write0((byte)36);
                break;
            }
            case 12: {
                this.write0((byte)37);
                break;
            }
            case 13: {
                this.write0((byte)38);
                break;
            }
            case 14: {
                this.write0((byte)39);
                break;
            }
            case 15: {
                this.write0((byte)40);
                break;
            }
            case 16: {
                this.write0((byte)41);
                break;
            }
            case 17: {
                this.write0((byte)42);
                break;
            }
            case 18: {
                this.write0((byte)43);
                break;
            }
            case 19: {
                this.write0((byte)44);
                break;
            }
            case 20: {
                this.write0((byte)45);
                break;
            }
            case 21: {
                this.write0((byte)46);
                break;
            }
            case 22: {
                this.write0((byte)47);
                break;
            }
            case 23: {
                this.write0((byte)48);
                break;
            }
            case 24: {
                this.write0((byte)49);
                break;
            }
            case 25: {
                this.write0((byte)50);
                break;
            }
            case 26: {
                this.write0((byte)51);
                break;
            }
            case 27: {
                this.write0((byte)52);
                break;
            }
            case 28: {
                this.write0((byte)53);
                break;
            }
            case 29: {
                this.write0((byte)54);
                break;
            }
            case 30: {
                this.write0((byte)55);
                break;
            }
            case 31: {
                this.write0((byte)56);
                break;
            }
            default: {
                int t = v;
                int ix = 0;
                byte[] b = this.mTemp;
                do {
                    b[++ix] = (byte)(v & 255);
                } while ((v >>>= 8) != 0);
                if (t > 0) {
                    if (b[ix] < 0) {
                        b[++ix] = 0;
                    }
                } else {
                    while (b[ix] == -1 && b[ix - 1] < 0) {
                        --ix;
                    }
                }
                b[0] = (byte)(0 + ix - 1);
                this.write0(b, 0, ix + 1);
            }
        }
    }

    private void writeVarint64(long v) throws IOException {
        int i = (int)v;
        if (v == (long)i) {
            this.writeVarint32(i);
        } else {
            long t = v;
            int ix = 0;
            byte[] b = this.mTemp;
            do {
                b[++ix] = (byte)(v & 255L);
            } while ((v >>>= 8) != 0L);
            if (t > 0L) {
                if (b[ix] < 0) {
                    b[++ix] = 0;
                }
            } else {
                while (b[ix] == -1 && b[ix - 1] < 0) {
                    --ix;
                }
            }
            b[0] = (byte)(0 + ix - 1);
            this.write0(b, 0, ix + 1);
        }
    }
}

