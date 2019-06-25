/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.io;

import com.alibaba.dubbo.common.io.UnsafeByteArrayInputStream;
import com.alibaba.dubbo.common.io.UnsafeByteArrayOutputStream;
import com.alibaba.dubbo.common.utils.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Bytes {
    private static final String C64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    private static final char[] BASE16 = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final char[] BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();
    private static final int MASK4 = 15;
    private static final int MASK6 = 63;
    private static final int MASK8 = 255;
    private static final Map<Integer, byte[]> DECODE_TABLE_MAP = new ConcurrentHashMap<Integer, byte[]>();
    private static ThreadLocal<MessageDigest> MD = new ThreadLocal();

    public static byte[] copyOf(byte[] src, int length) {
        byte[] dest = new byte[length];
        System.arraycopy(src, 0, dest, 0, Math.min(src.length, length));
        return dest;
    }

    public static byte[] short2bytes(short v) {
        byte[] ret = new byte[]{0, 0};
        Bytes.short2bytes(v, ret);
        return ret;
    }

    public static void short2bytes(short v, byte[] b) {
        Bytes.short2bytes(v, b, 0);
    }

    public static void short2bytes(short v, byte[] b, int off) {
        b[off + 1] = (byte)v;
        b[off + 0] = (byte)(v >>> 8);
    }

    public static byte[] int2bytes(int v) {
        byte[] ret = new byte[]{0, 0, 0, 0};
        Bytes.int2bytes(v, ret);
        return ret;
    }

    public static void int2bytes(int v, byte[] b) {
        Bytes.int2bytes(v, b, 0);
    }

    public static void int2bytes(int v, byte[] b, int off) {
        b[off + 3] = (byte)v;
        b[off + 2] = (byte)(v >>> 8);
        b[off + 1] = (byte)(v >>> 16);
        b[off + 0] = (byte)(v >>> 24);
    }

    public static byte[] float2bytes(float v) {
        byte[] ret = new byte[]{0, 0, 0, 0};
        Bytes.float2bytes(v, ret);
        return ret;
    }

    public static void float2bytes(float v, byte[] b) {
        Bytes.float2bytes(v, b, 0);
    }

    public static void float2bytes(float v, byte[] b, int off) {
        int i = Float.floatToIntBits(v);
        b[off + 3] = (byte)i;
        b[off + 2] = (byte)(i >>> 8);
        b[off + 1] = (byte)(i >>> 16);
        b[off + 0] = (byte)(i >>> 24);
    }

    public static byte[] long2bytes(long v) {
        byte[] ret = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        Bytes.long2bytes(v, ret);
        return ret;
    }

    public static void long2bytes(long v, byte[] b) {
        Bytes.long2bytes(v, b, 0);
    }

    public static void long2bytes(long v, byte[] b, int off) {
        b[off + 7] = (byte)v;
        b[off + 6] = (byte)(v >>> 8);
        b[off + 5] = (byte)(v >>> 16);
        b[off + 4] = (byte)(v >>> 24);
        b[off + 3] = (byte)(v >>> 32);
        b[off + 2] = (byte)(v >>> 40);
        b[off + 1] = (byte)(v >>> 48);
        b[off + 0] = (byte)(v >>> 56);
    }

    public static byte[] double2bytes(double v) {
        byte[] ret = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        Bytes.double2bytes(v, ret);
        return ret;
    }

    public static void double2bytes(double v, byte[] b) {
        Bytes.double2bytes(v, b, 0);
    }

    public static void double2bytes(double v, byte[] b, int off) {
        long j = Double.doubleToLongBits(v);
        b[off + 7] = (byte)j;
        b[off + 6] = (byte)(j >>> 8);
        b[off + 5] = (byte)(j >>> 16);
        b[off + 4] = (byte)(j >>> 24);
        b[off + 3] = (byte)(j >>> 32);
        b[off + 2] = (byte)(j >>> 40);
        b[off + 1] = (byte)(j >>> 48);
        b[off + 0] = (byte)(j >>> 56);
    }

    public static short bytes2short(byte[] b) {
        return Bytes.bytes2short(b, 0);
    }

    public static short bytes2short(byte[] b, int off) {
        return (short)(((b[off + 1] & 255) << 0) + (b[off + 0] << 8));
    }

    public static int bytes2int(byte[] b) {
        return Bytes.bytes2int(b, 0);
    }

    public static int bytes2int(byte[] b, int off) {
        return ((b[off + 3] & 255) << 0) + ((b[off + 2] & 255) << 8) + ((b[off + 1] & 255) << 16) + (b[off + 0] << 24);
    }

    public static float bytes2float(byte[] b) {
        return Bytes.bytes2float(b, 0);
    }

    public static float bytes2float(byte[] b, int off) {
        int i = ((b[off + 3] & 255) << 0) + ((b[off + 2] & 255) << 8) + ((b[off + 1] & 255) << 16) + (b[off + 0] << 24);
        return Float.intBitsToFloat(i);
    }

    public static long bytes2long(byte[] b) {
        return Bytes.bytes2long(b, 0);
    }

    public static long bytes2long(byte[] b, int off) {
        return (((long)b[off + 7] & 255L) << 0) + (((long)b[off + 6] & 255L) << 8) + (((long)b[off + 5] & 255L) << 16) + (((long)b[off + 4] & 255L) << 24) + (((long)b[off + 3] & 255L) << 32) + (((long)b[off + 2] & 255L) << 40) + (((long)b[off + 1] & 255L) << 48) + ((long)b[off + 0] << 56);
    }

    public static double bytes2double(byte[] b) {
        return Bytes.bytes2double(b, 0);
    }

    public static double bytes2double(byte[] b, int off) {
        long j = (((long)b[off + 7] & 255L) << 0) + (((long)b[off + 6] & 255L) << 8) + (((long)b[off + 5] & 255L) << 16) + (((long)b[off + 4] & 255L) << 24) + (((long)b[off + 3] & 255L) << 32) + (((long)b[off + 2] & 255L) << 40) + (((long)b[off + 1] & 255L) << 48) + ((long)b[off + 0] << 56);
        return Double.longBitsToDouble(j);
    }

    public static String bytes2hex(byte[] bs) {
        return Bytes.bytes2hex(bs, 0, bs.length);
    }

    public static String bytes2hex(byte[] bs, int off, int len) {
        if (off < 0) {
            throw new IndexOutOfBoundsException("bytes2hex: offset < 0, offset is " + off);
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException("bytes2hex: length < 0, length is " + len);
        }
        if (off + len > bs.length) {
            throw new IndexOutOfBoundsException("bytes2hex: offset + length > array length.");
        }
        int r = off;
        int w = 0;
        char[] cs = new char[len * 2];
        for (int i = 0; i < len; ++i) {
            byte b = bs[r++];
            cs[w++] = BASE16[b >> 4 & 15];
            cs[w++] = BASE16[b & 15];
        }
        return new String(cs);
    }

    public static byte[] hex2bytes(String str) {
        return Bytes.hex2bytes(str, 0, str.length());
    }

    public static byte[] hex2bytes(String str, int off, int len) {
        if ((len & 1) == 1) {
            throw new IllegalArgumentException("hex2bytes: ( len & 1 ) == 1.");
        }
        if (off < 0) {
            throw new IndexOutOfBoundsException("hex2bytes: offset < 0, offset is " + off);
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException("hex2bytes: length < 0, length is " + len);
        }
        if (off + len > str.length()) {
            throw new IndexOutOfBoundsException("hex2bytes: offset + length > array length.");
        }
        int num = len / 2;
        int r = off;
        int w = 0;
        byte[] b = new byte[num];
        for (int i = 0; i < num; ++i) {
            b[w++] = (byte)(Bytes.hex(str.charAt(r++)) << 4 | Bytes.hex(str.charAt(r++)));
        }
        return b;
    }

    public static String bytes2base64(byte[] b) {
        return Bytes.bytes2base64(b, 0, b.length, BASE64);
    }

    public static String bytes2base64(byte[] b, int offset, int length) {
        return Bytes.bytes2base64(b, offset, length, BASE64);
    }

    public static String bytes2base64(byte[] b, String code) {
        return Bytes.bytes2base64(b, 0, b.length, code);
    }

    public static String bytes2base64(byte[] b, int offset, int length, String code) {
        if (code.length() < 64) {
            throw new IllegalArgumentException("Base64 code length < 64.");
        }
        return Bytes.bytes2base64(b, offset, length, code.toCharArray());
    }

    public static String bytes2base64(byte[] b, char[] code) {
        return Bytes.bytes2base64(b, 0, b.length, code);
    }

    public static String bytes2base64(byte[] bs, int off, int len, char[] code) {
        int b1;
        if (off < 0) {
            throw new IndexOutOfBoundsException("bytes2base64: offset < 0, offset is " + off);
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException("bytes2base64: length < 0, length is " + len);
        }
        if (off + len > bs.length) {
            throw new IndexOutOfBoundsException("bytes2base64: offset + length > array length.");
        }
        if (code.length < 64) {
            throw new IllegalArgumentException("Base64 code length < 64.");
        }
        boolean pad = code.length > 64;
        int num = len / 3;
        int rem = len % 3;
        int r = off;
        int w = 0;
        char[] cs = new char[num * 4 + (rem == 0 ? 0 : (pad ? 4 : rem + 1))];
        for (int i = 0; i < num; ++i) {
            int b12 = bs[r++] & 255;
            int b2 = bs[r++] & 255;
            int b3 = bs[r++] & 255;
            cs[w++] = code[b12 >> 2];
            cs[w++] = code[b12 << 4 & 63 | b2 >> 4];
            cs[w++] = code[b2 << 2 & 63 | b3 >> 6];
            cs[w++] = code[b3 & 63];
        }
        if (rem == 1) {
            b1 = bs[r++] & 255;
            cs[w++] = code[b1 >> 2];
            cs[w++] = code[b1 << 4 & 63];
            if (pad) {
                cs[w++] = code[64];
                cs[w++] = code[64];
            }
        } else if (rem == 2) {
            b1 = bs[r++] & 255;
            int b2 = bs[r++] & 255;
            cs[w++] = code[b1 >> 2];
            cs[w++] = code[b1 << 4 & 63 | b2 >> 4];
            cs[w++] = code[b2 << 2 & 63];
            if (pad) {
                cs[w++] = code[64];
            }
        }
        return new String(cs);
    }

    public static byte[] base642bytes(String str) {
        return Bytes.base642bytes(str, 0, str.length());
    }

    public static byte[] base642bytes(String str, int offset, int length) {
        return Bytes.base642bytes(str, offset, length, C64);
    }

    public static byte[] base642bytes(String str, String code) {
        return Bytes.base642bytes(str, 0, str.length(), code);
    }

    public static byte[] base642bytes(String str, int off, int len, String code) {
        byte c1;
        byte c2;
        if (off < 0) {
            throw new IndexOutOfBoundsException("base642bytes: offset < 0, offset is " + off);
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException("base642bytes: length < 0, length is " + len);
        }
        if (off + len > str.length()) {
            throw new IndexOutOfBoundsException("base642bytes: offset + length > string length.");
        }
        if (code.length() < 64) {
            throw new IllegalArgumentException("Base64 code length < 64.");
        }
        int rem = len % 4;
        if (rem == 1) {
            throw new IllegalArgumentException("base642bytes: base64 string length % 4 == 1.");
        }
        int num = len / 4;
        int size = num * 3;
        if (code.length() > 64) {
            if (rem != 0) {
                throw new IllegalArgumentException("base642bytes: base64 string length error.");
            }
            char pc = code.charAt(64);
            if (str.charAt(off + len - 2) == pc) {
                size -= 2;
                --num;
                rem = 2;
            } else if (str.charAt(off + len - 1) == pc) {
                --size;
                --num;
                rem = 3;
            }
        } else if (rem == 2) {
            ++size;
        } else if (rem == 3) {
            size += 2;
        }
        int r = off;
        int w = 0;
        byte[] b = new byte[size];
        byte[] t = Bytes.decodeTable(code);
        for (int i = 0; i < num; ++i) {
            byte c12 = t[str.charAt(r++)];
            byte c22 = t[str.charAt(r++)];
            byte c3 = t[str.charAt(r++)];
            byte c4 = t[str.charAt(r++)];
            b[w++] = (byte)(c12 << 2 | c22 >> 4);
            b[w++] = (byte)(c22 << 4 | c3 >> 2);
            b[w++] = (byte)(c3 << 6 | c4);
        }
        if (rem == 2) {
            c1 = t[str.charAt(r++)];
            c2 = t[str.charAt(r++)];
            b[w++] = (byte)(c1 << 2 | c2 >> 4);
        } else if (rem == 3) {
            c1 = t[str.charAt(r++)];
            c2 = t[str.charAt(r++)];
            byte c3 = t[str.charAt(r++)];
            b[w++] = (byte)(c1 << 2 | c2 >> 4);
            b[w++] = (byte)(c2 << 4 | c3 >> 2);
        }
        return b;
    }

    public static byte[] base642bytes(String str, char[] code) {
        return Bytes.base642bytes(str, 0, str.length(), code);
    }

    public static byte[] base642bytes(String str, int off, int len, char[] code) {
        int c2;
        int c1;
        if (off < 0) {
            throw new IndexOutOfBoundsException("base642bytes: offset < 0, offset is " + off);
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException("base642bytes: length < 0, length is " + len);
        }
        if (off + len > str.length()) {
            throw new IndexOutOfBoundsException("base642bytes: offset + length > string length.");
        }
        if (code.length < 64) {
            throw new IllegalArgumentException("Base64 code length < 64.");
        }
        int rem = len % 4;
        if (rem == 1) {
            throw new IllegalArgumentException("base642bytes: base64 string length % 4 == 1.");
        }
        int num = len / 4;
        int size = num * 3;
        if (code.length > 64) {
            if (rem != 0) {
                throw new IllegalArgumentException("base642bytes: base64 string length error.");
            }
            char pc = code[64];
            if (str.charAt(off + len - 2) == pc) {
                size -= 2;
            } else if (str.charAt(off + len - 1) == pc) {
                --size;
            }
        } else if (rem == 2) {
            ++size;
        } else if (rem == 3) {
            size += 2;
        }
        int r = off;
        int w = 0;
        byte[] b = new byte[size];
        for (int i = 0; i < num; ++i) {
            int c12 = Bytes.indexOf(code, str.charAt(r++));
            int c22 = Bytes.indexOf(code, str.charAt(r++));
            int c3 = Bytes.indexOf(code, str.charAt(r++));
            int c4 = Bytes.indexOf(code, str.charAt(r++));
            b[w++] = (byte)(c12 << 2 | c22 >> 4);
            b[w++] = (byte)(c22 << 4 | c3 >> 2);
            b[w++] = (byte)(c3 << 6 | c4);
        }
        if (rem == 2) {
            c1 = Bytes.indexOf(code, str.charAt(r++));
            c2 = Bytes.indexOf(code, str.charAt(r++));
            b[w++] = (byte)(c1 << 2 | c2 >> 4);
        } else if (rem == 3) {
            c1 = Bytes.indexOf(code, str.charAt(r++));
            c2 = Bytes.indexOf(code, str.charAt(r++));
            int c3 = Bytes.indexOf(code, str.charAt(r++));
            b[w++] = (byte)(c1 << 2 | c2 >> 4);
            b[w++] = (byte)(c2 << 4 | c3 >> 2);
        }
        return b;
    }

    public static byte[] zip(byte[] bytes) throws IOException {
        UnsafeByteArrayOutputStream bos;
        bos = new UnsafeByteArrayOutputStream();
        DeflaterOutputStream os = new DeflaterOutputStream(bos);
        try {
            ((OutputStream)os).write(bytes);
        }
        finally {
            ((OutputStream)os).close();
            bos.close();
        }
        return bos.toByteArray();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] unzip(byte[] bytes) throws IOException {
        UnsafeByteArrayInputStream bis = new UnsafeByteArrayInputStream(bytes);
        UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream();
        InflaterInputStream is = new InflaterInputStream(bis);
        try {
            IOUtils.write(is, bos);
            byte[] arrby = bos.toByteArray();
            return arrby;
        }
        finally {
            ((InputStream)is).close();
            bis.close();
            bos.close();
        }
    }

    public static byte[] getMD5(String str) {
        return Bytes.getMD5(str.getBytes());
    }

    public static byte[] getMD5(byte[] source) {
        MessageDigest md = Bytes.getMessageDigest();
        return md.digest(source);
    }

    public static byte[] getMD5(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        try {
            byte[] arrby = Bytes.getMD5(is);
            return arrby;
        }
        finally {
            ((InputStream)is).close();
        }
    }

    public static byte[] getMD5(InputStream is) throws IOException {
        return Bytes.getMD5(is, 8192);
    }

    private static byte hex(char c) {
        if (c <= '9') {
            return (byte)(c - 48);
        }
        if (c >= 'a' && c <= 'f') {
            return (byte)(c - 97 + 10);
        }
        if (c >= 'A' && c <= 'F') {
            return (byte)(c - 65 + 10);
        }
        throw new IllegalArgumentException("hex string format error [" + c + "].");
    }

    private static int indexOf(char[] cs, char c) {
        int len = cs.length;
        for (int i = 0; i < len; ++i) {
            if (cs[i] != c) continue;
            return i;
        }
        return -1;
    }

    private static byte[] decodeTable(String code) {
        int hash = code.hashCode();
        byte[] ret = DECODE_TABLE_MAP.get(hash);
        if (ret == null) {
            int i;
            if (code.length() < 64) {
                throw new IllegalArgumentException("Base64 code length < 64.");
            }
            ret = new byte[128];
            for (i = 0; i < 128; ++i) {
                ret[i] = -1;
            }
            for (i = 0; i < 64; ++i) {
                ret[code.charAt((int)i)] = (byte)i;
            }
            DECODE_TABLE_MAP.put(hash, ret);
        }
        return ret;
    }

    private static byte[] getMD5(InputStream is, int bs) throws IOException {
        MessageDigest md = Bytes.getMessageDigest();
        byte[] buf = new byte[bs];
        while (is.available() > 0) {
            int read;
            int total = 0;
            while ((read = is.read(buf, total, bs - total)) > 0 && (total += read) < bs) {
            }
            md.update(buf);
        }
        return md.digest();
    }

    private static MessageDigest getMessageDigest() {
        MessageDigest ret = MD.get();
        if (ret == null) {
            try {
                ret = MessageDigest.getInstance("MD5");
                MD.set(ret);
            }
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    private Bytes() {
    }
}

