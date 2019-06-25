/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.json.JSONToken;
import com.alibaba.dubbo.common.json.ParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Yylex {
    public static final int YYEOF = -1;
    private static final int ZZ_BUFFERSIZE = 16384;
    public static final int STR2 = 4;
    public static final int STR1 = 2;
    public static final int YYINITIAL = 0;
    private static final int[] ZZ_LEXSTATE = new int[]{0, 0, 1, 1, 2, 2};
    private static final String ZZ_CMAP_PACKED = "\t\u0000\u0001\u000b\u0001\u000b\u0002\u0000\u0001\u000b\u0012\u0000\u0001\u000b\u0001\u0000\u0001\b\u0001\u0000\u0001\u0002\u0002\u0000\u0001\t\u0003\u0000\u0001\u0007\u0001#\u0001\u0004\u0001\u0005\u0001\f\n\u0001\u0001$\u0006\u0000\u0001\u001b\u0003\u0003\u0001\u0006\u0001\u001a\u0005\u0002\u0001\u001c\u0001\u0002\u0001\u001e\u0003\u0002\u0001\u0015\u0001\u001d\u0001\u0014\u0001\u0016\u0005\u0002\u0001!\u0001\n\u0001\"\u0001\u0000\u0001\u0002\u0001\u0000\u0001\u0017\u0001\r\u0002\u0003\u0001\u0013\u0001\u000e\u0005\u0002\u0001\u0018\u0001\u0002\u0001\u000f\u0003\u0002\u0001\u0010\u0001\u0019\u0001\u0011\u0001\u0012\u0005\u0002\u0001\u001f\u0001\u0000\u0001 \uff82\u0000";
    private static final char[] ZZ_CMAP = Yylex.zzUnpackCMap("\t\u0000\u0001\u000b\u0001\u000b\u0002\u0000\u0001\u000b\u0012\u0000\u0001\u000b\u0001\u0000\u0001\b\u0001\u0000\u0001\u0002\u0002\u0000\u0001\t\u0003\u0000\u0001\u0007\u0001#\u0001\u0004\u0001\u0005\u0001\f\n\u0001\u0001$\u0006\u0000\u0001\u001b\u0003\u0003\u0001\u0006\u0001\u001a\u0005\u0002\u0001\u001c\u0001\u0002\u0001\u001e\u0003\u0002\u0001\u0015\u0001\u001d\u0001\u0014\u0001\u0016\u0005\u0002\u0001!\u0001\n\u0001\"\u0001\u0000\u0001\u0002\u0001\u0000\u0001\u0017\u0001\r\u0002\u0003\u0001\u0013\u0001\u000e\u0005\u0002\u0001\u0018\u0001\u0002\u0001\u000f\u0003\u0002\u0001\u0010\u0001\u0019\u0001\u0011\u0001\u0012\u0005\u0002\u0001\u001f\u0001\u0000\u0001 \uff82\u0000");
    private static final int[] ZZ_ACTION = Yylex.zzUnpackAction();
    private static final String ZZ_ACTION_PACKED_0 = "\u0003\u0000\u0001\u0001\u0001\u0002\u0001\u0003\u0001\u0001\u0001\u0004\u0001\u0005\u0001\u0006\u0006\u0003\u0001\u0007\u0001\b\u0001\t\u0001\n\u0001\u000b\u0001\f\u0001\r\u0001\u000e\u0001\u0000\u0001\r\u0003\u0000\u0006\u0003\u0001\u000f\u0001\u0010\u0001\u0011\u0001\u0012\u0001\u0013\u0001\u0014\u0001\u0015\u0001\u0016\u0001\u0000\u0001\u0017\u0002\u0018\u0001\u0000\u0006\u0003\u0001\u0000\u0001\u0003\u0001\u0019\u0001\u001a\u0001\u0003\u0001\u0000\u0001\u001b\u0001\u0000\u0001\u001c";
    private static final int[] ZZ_ROWMAP = Yylex.zzUnpackRowMap();
    private static final String ZZ_ROWMAP_PACKED_0 = "\u0000\u0000\u0000%\u0000J\u0000o\u0000\u0094\u0000\u00b9\u0000\u00de\u0000o\u0000o\u0000\u0103\u0000\u0128\u0000\u014d\u0000\u0172\u0000\u0197\u0000\u01bc\u0000\u01e1\u0000o\u0000o\u0000o\u0000o\u0000o\u0000o\u0000\u0206\u0000o\u0000\u022b\u0000\u0250\u0000\u0275\u0000\u029a\u0000\u02bf\u0000\u02e4\u0000\u0309\u0000\u032e\u0000\u0353\u0000\u0378\u0000\u039d\u0000o\u0000o\u0000o\u0000o\u0000o\u0000o\u0000o\u0000o\u0000\u03c2\u0000o\u0000\u03e7\u0000\u040c\u0000\u040c\u0000\u0431\u0000\u0456\u0000\u047b\u0000\u04a0\u0000\u04c5\u0000\u04ea\u0000\u050f\u0000\u0534\u0000\u00b9\u0000\u00b9\u0000\u0559\u0000\u057e\u0000\u00b9\u0000\u05a3\u0000o";
    private static final int[] ZZ_TRANS = new int[]{3, 4, 5, 5, 6, 3, 5, 3, 7, 8, 3, 9, 3, 5, 10, 11, 5, 12, 5, 5, 13, 5, 5, 5, 5, 5, 14, 5, 5, 5, 15, 16, 17, 18, 19, 20, 21, 22, 22, 22, 22, 22, 22, 22, 22, 23, 22, 24, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 25, 25, 25, 25, 25, 25, 25, 25, 25, 23, 26, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, -1, -1, -1, 27, 28, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 28, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 29, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 30, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 31, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 32, 5, 5, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 33, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 34, 5, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, 22, 22, 22, 22, 22, 22, 22, 22, -1, 22, -1, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, -1, -1, -1, -1, -1, -1, -1, -1, 35, -1, 36, -1, 37, 38, 39, 40, 41, 42, 43, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 25, 25, 25, 25, 25, 25, 25, 25, 25, -1, -1, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, -1, -1, -1, -1, -1, -1, -1, -1, -1, 44, 36, -1, 37, 38, 39, 40, 41, 42, 43, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 45, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 46, -1, -1, 47, -1, -1, 47, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 48, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 49, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 50, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 51, 5, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 52, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 53, 5, 5, -1, -1, -1, -1, -1, -1, -1, 54, -1, 54, -1, -1, 54, -1, -1, -1, -1, -1, -1, 54, 54, -1, -1, -1, -1, 54, -1, -1, -1, 54, -1, -1, 54, 54, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 45, -1, -1, -1, -1, 28, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 28, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 46, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 55, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 56, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 57, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 57, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 58, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 56, 5, 5, -1, -1, -1, -1, -1, -1, -1, 59, -1, 59, -1, -1, 59, -1, -1, -1, -1, -1, -1, 59, 59, -1, -1, -1, -1, 59, -1, -1, -1, 59, -1, -1, 59, 59, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 5, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 60, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 5, 5, 5, -1, -1, 60, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, -1, -1, -1, -1, -1, -1, -1, 61, -1, 61, -1, -1, 61, -1, -1, -1, -1, -1, -1, 61, 61, -1, -1, -1, -1, 61, -1, -1, -1, 61, -1, -1, 61, 61, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, -1, 62, -1, -1, -1, -1, -1, -1, 62, 62, -1, -1, -1, -1, 62, -1, -1, -1, 62, -1, -1, 62, 62, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    private static final int ZZ_UNKNOWN_ERROR = 0;
    private static final int ZZ_NO_MATCH = 1;
    private static final int ZZ_PUSHBACK_2BIG = 2;
    private static final String[] ZZ_ERROR_MSG = new String[]{"Unkown internal scanner error", "Error: could not match input", "Error: pushback value was too large"};
    private static final int[] ZZ_ATTRIBUTE = Yylex.zzUnpackAttribute();
    private static final String ZZ_ATTRIBUTE_PACKED_0 = "\u0003\u0000\u0001\t\u0003\u0001\u0002\t\u0007\u0001\u0006\t\u0001\u0001\u0001\t\u0001\u0000\u0001\u0001\u0003\u0000\u0006\u0001\b\t\u0001\u0000\u0001\t\u0002\u0001\u0001\u0000\u0006\u0001\u0001\u0000\u0004\u0001\u0001\u0000\u0001\u0001\u0001\u0000\u0001\t";
    private Reader zzReader;
    private int zzState;
    private int zzLexicalState = 0;
    private char[] zzBuffer = new char[16384];
    private int zzMarkedPos;
    private int zzCurrentPos;
    private int zzStartRead;
    private int zzEndRead;
    private boolean zzAtEOF;
    private StringBuffer sb;

    private static int[] zzUnpackAction() {
        int[] result = new int[63];
        int offset = 0;
        offset = Yylex.zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
        return result;
    }

    private static int zzUnpackAction(String packed, int offset, int[] result) {
        int i = 0;
        int j = offset;
        int l = packed.length();
        while (i < l) {
            int count = packed.charAt(i++);
            char value = packed.charAt(i++);
            do {
                result[j++] = value;
            } while (--count > 0);
        }
        return j;
    }

    private static int[] zzUnpackRowMap() {
        int[] result = new int[63];
        int offset = 0;
        offset = Yylex.zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
        return result;
    }

    private static int zzUnpackRowMap(String packed, int offset, int[] result) {
        int i = 0;
        int j = offset;
        int l = packed.length();
        while (i < l) {
            int high = packed.charAt(i++) << 16;
            result[j++] = high | packed.charAt(i++);
        }
        return j;
    }

    private static int[] zzUnpackAttribute() {
        int[] result = new int[63];
        int offset = 0;
        offset = Yylex.zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
        return result;
    }

    private static int zzUnpackAttribute(String packed, int offset, int[] result) {
        int i = 0;
        int j = offset;
        int l = packed.length();
        while (i < l) {
            int count = packed.charAt(i++);
            char value = packed.charAt(i++);
            do {
                result[j++] = value;
            } while (--count > 0);
        }
        return j;
    }

    Yylex(Reader in) {
        this.zzReader = in;
    }

    Yylex(InputStream in) {
        this(new InputStreamReader(in));
    }

    private static char[] zzUnpackCMap(String packed) {
        char[] map = new char[65536];
        int i = 0;
        int j = 0;
        while (i < 122) {
            int count = packed.charAt(i++);
            char value = packed.charAt(i++);
            do {
                map[j++] = value;
            } while (--count > 0);
        }
        return map;
    }

    private boolean zzRefill() throws IOException {
        int numRead;
        if (this.zzStartRead > 0) {
            System.arraycopy(this.zzBuffer, this.zzStartRead, this.zzBuffer, 0, this.zzEndRead - this.zzStartRead);
            this.zzEndRead -= this.zzStartRead;
            this.zzCurrentPos -= this.zzStartRead;
            this.zzMarkedPos -= this.zzStartRead;
            this.zzStartRead = 0;
        }
        if (this.zzCurrentPos >= this.zzBuffer.length) {
            char[] newBuffer = new char[this.zzCurrentPos * 2];
            System.arraycopy(this.zzBuffer, 0, newBuffer, 0, this.zzBuffer.length);
            this.zzBuffer = newBuffer;
        }
        if ((numRead = this.zzReader.read(this.zzBuffer, this.zzEndRead, this.zzBuffer.length - this.zzEndRead)) > 0) {
            this.zzEndRead += numRead;
            return false;
        }
        if (numRead == 0) {
            int c = this.zzReader.read();
            if (c == -1) {
                return true;
            }
            this.zzBuffer[this.zzEndRead++] = (char)c;
            return false;
        }
        return true;
    }

    public final void yyclose() throws IOException {
        this.zzAtEOF = true;
        this.zzEndRead = this.zzStartRead;
        if (this.zzReader != null) {
            this.zzReader.close();
        }
    }

    public final void yyreset(Reader reader) {
        this.zzReader = reader;
        this.zzAtEOF = false;
        this.zzStartRead = 0;
        this.zzEndRead = 0;
        this.zzMarkedPos = 0;
        this.zzCurrentPos = 0;
        this.zzLexicalState = 0;
    }

    public final int yystate() {
        return this.zzLexicalState;
    }

    public final void yybegin(int newState) {
        this.zzLexicalState = newState;
    }

    public final String yytext() {
        return new String(this.zzBuffer, this.zzStartRead, this.zzMarkedPos - this.zzStartRead);
    }

    public final char yycharat(int pos) {
        return this.zzBuffer[this.zzStartRead + pos];
    }

    public final int yylength() {
        return this.zzMarkedPos - this.zzStartRead;
    }

    private void zzScanError(int errorCode) {
        String message;
        try {
            message = ZZ_ERROR_MSG[errorCode];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            message = ZZ_ERROR_MSG[0];
        }
        throw new Error(message);
    }

    public void yypushback(int number) {
        if (number > this.yylength()) {
            this.zzScanError(2);
        }
        this.zzMarkedPos -= number;
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Lifted jumps to return sites
     */
    public JSONToken yylex() throws IOException, ParseException {
        zzEndReadL = this.zzEndRead;
        zzBufferL = this.zzBuffer;
        zzCMapL = Yylex.ZZ_CMAP;
        zzTransL = Yylex.ZZ_TRANS;
        zzRowMapL = Yylex.ZZ_ROWMAP;
        zzAttrL = Yylex.ZZ_ATTRIBUTE;
        block59 : do {
            zzMarkedPosL = this.zzMarkedPos;
            zzAction = -1;
            this.zzCurrentPos = this.zzStartRead = zzMarkedPosL;
            zzCurrentPosL = this.zzStartRead;
            this.zzState = Yylex.ZZ_LEXSTATE[this.zzLexicalState];
            do lbl-1000: // 3 sources:
            {
                if (zzCurrentPosL < zzEndReadL) {
                    zzInput = zzBufferL[zzCurrentPosL++];
                } else {
                    if (this.zzAtEOF) {
                        zzInput = -1;
                        break;
                    }
                    this.zzCurrentPos = zzCurrentPosL;
                    this.zzMarkedPos = zzMarkedPosL;
                    eof = this.zzRefill();
                    zzCurrentPosL = this.zzCurrentPos;
                    zzMarkedPosL = this.zzMarkedPos;
                    zzBufferL = this.zzBuffer;
                    zzEndReadL = this.zzEndRead;
                    if (eof) {
                        zzInput = -1;
                        break;
                    }
                    zzInput = zzBufferL[zzCurrentPosL++];
                }
                zzNext = zzTransL[zzRowMapL[this.zzState] + zzCMapL[zzInput]];
                if (zzNext == -1) break;
                this.zzState = zzNext;
                zzAttributes = zzAttrL[this.zzState];
                if ((zzAttributes & 1) != 1) ** GOTO lbl-1000
                zzAction = this.zzState;
                zzMarkedPosL = zzCurrentPosL;
            } while ((zzAttributes & 8) != 8);
            this.zzMarkedPos = zzMarkedPosL;
            switch (zzAction < 0 ? zzAction : Yylex.ZZ_ACTION[zzAction]) {
                case 25: {
                    return new JSONToken(16, null);
                }
                case 29: {
                    continue block59;
                }
                case 13: {
                    this.sb.append(this.yytext());
                }
                case 30: {
                    continue block59;
                }
                case 18: {
                    this.sb.append('\b');
                }
                case 31: {
                    continue block59;
                }
                case 9: {
                    return new JSONToken(3);
                }
                case 32: {
                    continue block59;
                }
                case 2: {
                    val = Long.valueOf(this.yytext());
                    return new JSONToken(18, val);
                }
                case 33: {
                    continue block59;
                }
                case 16: {
                    this.sb.append('\\');
                }
                case 34: {
                    continue block59;
                }
                case 8: {
                    return new JSONToken(4);
                }
                case 35: {
                    continue block59;
                }
                case 26: {
                    return new JSONToken(17, Boolean.TRUE);
                }
                case 36: {
                    continue block59;
                }
                case 23: {
                    this.sb.append('\'');
                }
                case 37: {
                    continue block59;
                }
                case 5: {
                    this.sb = new StringBuffer();
                    this.yybegin(4);
                }
                case 38: {
                    continue block59;
                }
                case 27: {
                    return new JSONToken(17, Boolean.FALSE);
                }
                case 39: {
                    continue block59;
                }
                case 12: {
                    return new JSONToken(7);
                }
                case 40: {
                    continue block59;
                }
                case 21: {
                    this.sb.append('\r');
                }
                case 41: {
                    continue block59;
                }
                case 3: {
                    return new JSONToken(1, this.yytext());
                }
                case 42: {
                    continue block59;
                }
                case 28: {
                    try {
                        this.sb.append((char)Integer.parseInt(this.yytext().substring(2), 16));
                    }
                    catch (Exception e) {
                        throw new ParseException(e.getMessage());
                    }
                }
                case 43: {
                    continue block59;
                }
                case 10: {
                    return new JSONToken(5);
                }
                case 44: {
                    continue block59;
                }
                case 17: {
                    this.sb.append('/');
                }
                case 45: {
                    continue block59;
                }
                case 11: {
                    return new JSONToken(6);
                }
                case 46: {
                    continue block59;
                }
                case 15: {
                    this.sb.append('\"');
                }
                case 47: {
                    continue block59;
                }
                case 24: {
                    val = Double.valueOf(this.yytext());
                    return new JSONToken(19, val);
                }
                case 48: {
                    continue block59;
                }
                case 1: {
                    throw new ParseException("Unexpected char [" + this.yytext() + "]");
                }
                case 49: {
                    continue block59;
                }
                case 19: {
                    this.sb.append('\f');
                }
                case 50: {
                    continue block59;
                }
                case 7: {
                    return new JSONToken(2);
                }
                case 51: {
                    continue block59;
                }
                case 14: {
                    this.yybegin(0);
                    return new JSONToken(20, this.sb.toString());
                }
                case 52: {
                    continue block59;
                }
                case 22: {
                    this.sb.append('\t');
                }
                case 53: {
                    continue block59;
                }
                case 4: {
                    this.sb = new StringBuffer();
                    this.yybegin(2);
                }
                case 54: {
                    continue block59;
                }
                case 20: {
                    this.sb.append('\n');
                }
                case 55: {
                    continue block59;
                }
                case 6: 
                case 56: {
                    continue block59;
                }
            }
            if (zzInput == -1 && this.zzStartRead == this.zzCurrentPos) {
                this.zzAtEOF = true;
                return null;
            }
            this.zzScanError(1);
        } while (true);
    }
}

