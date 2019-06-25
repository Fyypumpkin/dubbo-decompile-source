/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.dubbo;

import com.alibaba.dubbo.common.serialize.DataInput;
import com.alibaba.dubbo.common.serialize.support.dubbo.GenericDataFlags;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

public class GenericDataInput
implements DataInput,
GenericDataFlags {
    private static final String EMPTY_STRING = "";
    private static final byte[] EMPTY_BYTES = new byte[0];
    private final InputStream mInput;
    private final byte[] mBuffer;
    private int mRead = 0;
    private int mPosition = 0;

    public GenericDataInput(InputStream is) {
        this(is, 1024);
    }

    public GenericDataInput(InputStream is, int buffSize) {
        this.mInput = is;
        this.mBuffer = new byte[buffSize];
    }

    @Override
    public boolean readBool() throws IOException {
        byte b = this.read0();
        switch (b) {
            case 25: {
                return false;
            }
            case 26: {
                return true;
            }
        }
        throw new IOException("Tag error, expect BYTE_TRUE|BYTE_FALSE, but get " + b);
    }

    @Override
    public byte readByte() throws IOException {
        byte b = this.read0();
        switch (b) {
            case 0: {
                return this.read0();
            }
            case 25: {
                return 0;
            }
            case 26: {
                return 1;
            }
            case 27: {
                return 2;
            }
            case 28: {
                return 3;
            }
            case 29: {
                return 4;
            }
            case 30: {
                return 5;
            }
            case 31: {
                return 6;
            }
            case 32: {
                return 7;
            }
            case 33: {
                return 8;
            }
            case 34: {
                return 9;
            }
            case 35: {
                return 10;
            }
            case 36: {
                return 11;
            }
            case 37: {
                return 12;
            }
            case 38: {
                return 13;
            }
            case 39: {
                return 14;
            }
            case 40: {
                return 15;
            }
            case 41: {
                return 16;
            }
            case 42: {
                return 17;
            }
            case 43: {
                return 18;
            }
            case 44: {
                return 19;
            }
            case 45: {
                return 20;
            }
            case 46: {
                return 21;
            }
            case 47: {
                return 22;
            }
            case 48: {
                return 23;
            }
            case 49: {
                return 24;
            }
            case 50: {
                return 25;
            }
            case 51: {
                return 26;
            }
            case 52: {
                return 27;
            }
            case 53: {
                return 28;
            }
            case 54: {
                return 29;
            }
            case 55: {
                return 30;
            }
            case 56: {
                return 31;
            }
        }
        throw new IOException("Tag error, expect VARINT, but get " + b);
    }

    @Override
    public short readShort() throws IOException {
        return (short)this.readVarint32();
    }

    @Override
    public int readInt() throws IOException {
        return this.readVarint32();
    }

    @Override
    public long readLong() throws IOException {
        return this.readVarint64();
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(this.readVarint32());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(this.readVarint64());
    }

    @Override
    public String readUTF() throws IOException {
        byte b = this.read0();
        switch (b) {
            case -125: {
                int len = this.readUInt();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < len; ++i) {
                    byte b2;
                    byte b1 = this.read0();
                    if ((b1 & 128) == 0) {
                        sb.append((char)b1);
                        continue;
                    }
                    if ((b1 & 224) == 192) {
                        b2 = this.read0();
                        sb.append((char)((b1 & 31) << 6 | b2 & 63));
                        continue;
                    }
                    if ((b1 & 240) == 224) {
                        b2 = this.read0();
                        byte b3 = this.read0();
                        sb.append((char)((b1 & 15) << 12 | (b2 & 63) << 6 | b3 & 63));
                        continue;
                    }
                    throw new UTFDataFormatException("Bad utf-8 encoding at " + b1);
                }
                return sb.toString();
            }
            case -108: {
                return null;
            }
            case -107: {
                return EMPTY_STRING;
            }
        }
        throw new IOException("Tag error, expect BYTES|BYTES_NULL|BYTES_EMPTY, but get " + b);
    }

    @Override
    public byte[] readBytes() throws IOException {
        byte b = this.read0();
        switch (b) {
            case -125: {
                return this.read0(this.readUInt());
            }
            case -108: {
                return null;
            }
            case -107: {
                return EMPTY_BYTES;
            }
        }
        throw new IOException("Tag error, expect BYTES|BYTES_NULL|BYTES_EMPTY, but get " + b);
    }

    public int readUInt() throws IOException {
        byte tmp = this.read0();
        if (tmp < 0) {
            return tmp & 127;
        }
        int ret = tmp & 127;
        tmp = this.read0();
        if (tmp < 0) {
            ret |= (tmp & 127) << 7;
        } else {
            ret |= tmp << 7;
            tmp = this.read0();
            if (tmp < 0) {
                ret |= (tmp & 127) << 14;
            } else {
                ret |= tmp << 14;
                tmp = this.read0();
                if (tmp < 0) {
                    ret |= (tmp & 127) << 21;
                } else {
                    ret |= tmp << 21;
                    ret |= (this.read0() & 127) << 28;
                }
            }
        }
        return ret;
    }

    protected byte read0() throws IOException {
        if (this.mPosition == this.mRead) {
            this.fillBuffer();
        }
        return this.mBuffer[this.mPosition++];
    }

    protected byte[] read0(int len) throws IOException {
        int rem = this.mRead - this.mPosition;
        byte[] ret = new byte[len];
        if (len <= rem) {
            System.arraycopy(this.mBuffer, this.mPosition, ret, 0, len);
            this.mPosition += len;
        } else {
            System.arraycopy(this.mBuffer, this.mPosition, ret, 0, rem);
            this.mPosition = this.mRead;
            len -= rem;
            int pos = rem;
            while (len > 0) {
                int read = this.mInput.read(ret, pos, len);
                if (read == -1) {
                    throw new EOFException();
                }
                pos += read;
                len -= read;
            }
        }
        return ret;
    }

    private int readVarint32() throws IOException {
        byte b = this.read0();
        switch (b) {
            case 0: {
                return this.read0();
            }
            case 1: {
                byte b1 = this.read0();
                byte b2 = this.read0();
                return (short)(b1 & 255 | (b2 & 255) << 8);
            }
            case 2: {
                byte b1 = this.read0();
                byte b2 = this.read0();
                byte b3 = this.read0();
                int ret = b1 & 255 | (b2 & 255) << 8 | (b3 & 255) << 16;
                if (b3 < 0) {
                    return ret | -16777216;
                }
                return ret;
            }
            case 3: {
                byte b1 = this.read0();
                byte b2 = this.read0();
                byte b3 = this.read0();
                byte b4 = this.read0();
                return b1 & 255 | (b2 & 255) << 8 | (b3 & 255) << 16 | (b4 & 255) << 24;
            }
            case 10: {
                return -15;
            }
            case 11: {
                return -14;
            }
            case 12: {
                return -13;
            }
            case 13: {
                return -12;
            }
            case 14: {
                return -11;
            }
            case 15: {
                return -10;
            }
            case 16: {
                return -9;
            }
            case 17: {
                return -8;
            }
            case 18: {
                return -7;
            }
            case 19: {
                return -6;
            }
            case 20: {
                return -5;
            }
            case 21: {
                return -4;
            }
            case 22: {
                return -3;
            }
            case 23: {
                return -2;
            }
            case 24: {
                return -1;
            }
            case 25: {
                return 0;
            }
            case 26: {
                return 1;
            }
            case 27: {
                return 2;
            }
            case 28: {
                return 3;
            }
            case 29: {
                return 4;
            }
            case 30: {
                return 5;
            }
            case 31: {
                return 6;
            }
            case 32: {
                return 7;
            }
            case 33: {
                return 8;
            }
            case 34: {
                return 9;
            }
            case 35: {
                return 10;
            }
            case 36: {
                return 11;
            }
            case 37: {
                return 12;
            }
            case 38: {
                return 13;
            }
            case 39: {
                return 14;
            }
            case 40: {
                return 15;
            }
            case 41: {
                return 16;
            }
            case 42: {
                return 17;
            }
            case 43: {
                return 18;
            }
            case 44: {
                return 19;
            }
            case 45: {
                return 20;
            }
            case 46: {
                return 21;
            }
            case 47: {
                return 22;
            }
            case 48: {
                return 23;
            }
            case 49: {
                return 24;
            }
            case 50: {
                return 25;
            }
            case 51: {
                return 26;
            }
            case 52: {
                return 27;
            }
            case 53: {
                return 28;
            }
            case 54: {
                return 29;
            }
            case 55: {
                return 30;
            }
            case 56: {
                return 31;
            }
        }
        throw new IOException("Tag error, expect VARINT, but get " + b);
    }

    private long readVarint64() throws IOException {
        byte b = this.read0();
        switch (b) {
            case 0: {
                return this.read0();
            }
            case 1: {
                byte b1 = this.read0();
                byte b2 = this.read0();
                return (short)(b1 & 255 | (b2 & 255) << 8);
            }
            case 2: {
                byte b1 = this.read0();
                byte b2 = this.read0();
                byte b3 = this.read0();
                int ret = b1 & 255 | (b2 & 255) << 8 | (b3 & 255) << 16;
                if (b3 < 0) {
                    return ret | -16777216;
                }
                return ret;
            }
            case 3: {
                byte b1 = this.read0();
                byte b2 = this.read0();
                byte b3 = this.read0();
                byte b4 = this.read0();
                return b1 & 255 | (b2 & 255) << 8 | (b3 & 255) << 16 | (b4 & 255) << 24;
            }
            case 4: {
                byte b1 = this.read0();
                byte b2 = this.read0();
                byte b3 = this.read0();
                byte b4 = this.read0();
                byte b5 = this.read0();
                long ret = (long)b1 & 255L | ((long)b2 & 255L) << 8 | ((long)b3 & 255L) << 16 | ((long)b4 & 255L) << 24 | ((long)b5 & 255L) << 32;
                if (b5 < 0) {
                    return ret | -1099511627776L;
                }
                return ret;
            }
            case 5: {
                byte b1 = this.read0();
                byte b2 = this.read0();
                byte b3 = this.read0();
                byte b4 = this.read0();
                byte b5 = this.read0();
                byte b6 = this.read0();
                long ret = (long)b1 & 255L | ((long)b2 & 255L) << 8 | ((long)b3 & 255L) << 16 | ((long)b4 & 255L) << 24 | ((long)b5 & 255L) << 32 | ((long)b6 & 255L) << 40;
                if (b6 < 0) {
                    return ret | -281474976710656L;
                }
                return ret;
            }
            case 6: {
                byte b1 = this.read0();
                byte b2 = this.read0();
                byte b3 = this.read0();
                byte b4 = this.read0();
                byte b5 = this.read0();
                byte b6 = this.read0();
                byte b7 = this.read0();
                long ret = (long)b1 & 255L | ((long)b2 & 255L) << 8 | ((long)b3 & 255L) << 16 | ((long)b4 & 255L) << 24 | ((long)b5 & 255L) << 32 | ((long)b6 & 255L) << 40 | ((long)b7 & 255L) << 48;
                if (b7 < 0) {
                    return ret | -72057594037927936L;
                }
                return ret;
            }
            case 7: {
                byte b1 = this.read0();
                byte b2 = this.read0();
                byte b3 = this.read0();
                byte b4 = this.read0();
                byte b5 = this.read0();
                byte b6 = this.read0();
                byte b7 = this.read0();
                byte b8 = this.read0();
                return (long)b1 & 255L | ((long)b2 & 255L) << 8 | ((long)b3 & 255L) << 16 | ((long)b4 & 255L) << 24 | ((long)b5 & 255L) << 32 | ((long)b6 & 255L) << 40 | ((long)b7 & 255L) << 48 | ((long)b8 & 255L) << 56;
            }
            case 10: {
                return -15L;
            }
            case 11: {
                return -14L;
            }
            case 12: {
                return -13L;
            }
            case 13: {
                return -12L;
            }
            case 14: {
                return -11L;
            }
            case 15: {
                return -10L;
            }
            case 16: {
                return -9L;
            }
            case 17: {
                return -8L;
            }
            case 18: {
                return -7L;
            }
            case 19: {
                return -6L;
            }
            case 20: {
                return -5L;
            }
            case 21: {
                return -4L;
            }
            case 22: {
                return -3L;
            }
            case 23: {
                return -2L;
            }
            case 24: {
                return -1L;
            }
            case 25: {
                return 0L;
            }
            case 26: {
                return 1L;
            }
            case 27: {
                return 2L;
            }
            case 28: {
                return 3L;
            }
            case 29: {
                return 4L;
            }
            case 30: {
                return 5L;
            }
            case 31: {
                return 6L;
            }
            case 32: {
                return 7L;
            }
            case 33: {
                return 8L;
            }
            case 34: {
                return 9L;
            }
            case 35: {
                return 10L;
            }
            case 36: {
                return 11L;
            }
            case 37: {
                return 12L;
            }
            case 38: {
                return 13L;
            }
            case 39: {
                return 14L;
            }
            case 40: {
                return 15L;
            }
            case 41: {
                return 16L;
            }
            case 42: {
                return 17L;
            }
            case 43: {
                return 18L;
            }
            case 44: {
                return 19L;
            }
            case 45: {
                return 20L;
            }
            case 46: {
                return 21L;
            }
            case 47: {
                return 22L;
            }
            case 48: {
                return 23L;
            }
            case 49: {
                return 24L;
            }
            case 50: {
                return 25L;
            }
            case 51: {
                return 26L;
            }
            case 52: {
                return 27L;
            }
            case 53: {
                return 28L;
            }
            case 54: {
                return 29L;
            }
            case 55: {
                return 30L;
            }
            case 56: {
                return 31L;
            }
        }
        throw new IOException("Tag error, expect VARINT, but get " + b);
    }

    private void fillBuffer() throws IOException {
        this.mPosition = 0;
        this.mRead = this.mInput.read(this.mBuffer);
        if (this.mRead == -1) {
            this.mRead = 0;
            throw new EOFException();
        }
    }
}

