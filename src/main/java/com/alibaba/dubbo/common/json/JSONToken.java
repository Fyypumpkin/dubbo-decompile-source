/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

public class JSONToken {
    public static final int ANY = 0;
    public static final int IDENT = 1;
    public static final int LBRACE = 2;
    public static final int LSQUARE = 3;
    public static final int RBRACE = 4;
    public static final int RSQUARE = 5;
    public static final int COMMA = 6;
    public static final int COLON = 7;
    public static final int NULL = 16;
    public static final int BOOL = 17;
    public static final int INT = 18;
    public static final int FLOAT = 19;
    public static final int STRING = 20;
    public static final int ARRAY = 21;
    public static final int OBJECT = 22;
    public final int type;
    public final Object value;

    JSONToken(int t) {
        this(t, null);
    }

    JSONToken(int t, Object v) {
        this.type = t;
        this.value = v;
    }

    static String token2string(int t) {
        switch (t) {
            case 2: {
                return "{";
            }
            case 4: {
                return "}";
            }
            case 3: {
                return "[";
            }
            case 5: {
                return "]";
            }
            case 6: {
                return ",";
            }
            case 7: {
                return ":";
            }
            case 1: {
                return "IDENT";
            }
            case 16: {
                return "NULL";
            }
            case 17: {
                return "BOOL VALUE";
            }
            case 18: {
                return "INT VALUE";
            }
            case 19: {
                return "FLOAT VALUE";
            }
            case 20: {
                return "STRING VALUE";
            }
        }
        return "ANY";
    }
}

