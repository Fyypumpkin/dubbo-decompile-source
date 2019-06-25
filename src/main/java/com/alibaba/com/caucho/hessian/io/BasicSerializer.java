/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianOutput;
import com.alibaba.com.caucho.hessian.io.AbstractSerializer;
import java.io.IOException;
import java.util.Date;

public class BasicSerializer
extends AbstractSerializer {
    public static final int NULL = 0;
    public static final int BOOLEAN = 1;
    public static final int BYTE = 2;
    public static final int SHORT = 3;
    public static final int INTEGER = 4;
    public static final int LONG = 5;
    public static final int FLOAT = 6;
    public static final int DOUBLE = 7;
    public static final int CHARACTER = 8;
    public static final int CHARACTER_OBJECT = 9;
    public static final int STRING = 10;
    public static final int DATE = 11;
    public static final int NUMBER = 12;
    public static final int OBJECT = 13;
    public static final int BOOLEAN_ARRAY = 14;
    public static final int BYTE_ARRAY = 15;
    public static final int SHORT_ARRAY = 16;
    public static final int INTEGER_ARRAY = 17;
    public static final int LONG_ARRAY = 18;
    public static final int FLOAT_ARRAY = 19;
    public static final int DOUBLE_ARRAY = 20;
    public static final int CHARACTER_ARRAY = 21;
    public static final int STRING_ARRAY = 22;
    public static final int OBJECT_ARRAY = 23;
    private int code;

    public BasicSerializer(int code) {
        this.code = code;
    }

    @Override
    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
        switch (this.code) {
            case 1: {
                out.writeBoolean((Boolean)obj);
                break;
            }
            case 2: 
            case 3: 
            case 4: {
                out.writeInt(((Number)obj).intValue());
                break;
            }
            case 5: {
                out.writeLong(((Number)obj).longValue());
                break;
            }
            case 6: 
            case 7: {
                out.writeDouble(((Number)obj).doubleValue());
                break;
            }
            case 8: 
            case 9: {
                out.writeString(String.valueOf(obj));
                break;
            }
            case 10: {
                out.writeString((String)obj);
                break;
            }
            case 11: {
                out.writeUTCDate(((Date)obj).getTime());
                break;
            }
            case 14: {
                if (out.addRef(obj)) {
                    return;
                }
                boolean[] data = (boolean[])obj;
                boolean hasEnd = out.writeListBegin(data.length, "[boolean");
                for (int i = 0; i < data.length; ++i) {
                    out.writeBoolean(data[i]);
                }
                if (!hasEnd) break;
                out.writeListEnd();
                break;
            }
            case 15: {
                byte[] data = (byte[])obj;
                out.writeBytes(data, 0, data.length);
                break;
            }
            case 16: {
                if (out.addRef(obj)) {
                    return;
                }
                short[] data = (short[])obj;
                boolean hasEnd = out.writeListBegin(data.length, "[short");
                for (int i = 0; i < data.length; ++i) {
                    out.writeInt(data[i]);
                }
                if (!hasEnd) break;
                out.writeListEnd();
                break;
            }
            case 17: {
                if (out.addRef(obj)) {
                    return;
                }
                int[] data = (int[])obj;
                boolean hasEnd = out.writeListBegin(data.length, "[int");
                for (int i = 0; i < data.length; ++i) {
                    out.writeInt(data[i]);
                }
                if (!hasEnd) break;
                out.writeListEnd();
                break;
            }
            case 18: {
                if (out.addRef(obj)) {
                    return;
                }
                long[] data = (long[])obj;
                boolean hasEnd = out.writeListBegin(data.length, "[long");
                for (int i = 0; i < data.length; ++i) {
                    out.writeLong(data[i]);
                }
                if (!hasEnd) break;
                out.writeListEnd();
                break;
            }
            case 19: {
                if (out.addRef(obj)) {
                    return;
                }
                float[] data = (float[])obj;
                boolean hasEnd = out.writeListBegin(data.length, "[float");
                for (int i = 0; i < data.length; ++i) {
                    out.writeDouble(data[i]);
                }
                if (!hasEnd) break;
                out.writeListEnd();
                break;
            }
            case 20: {
                if (out.addRef(obj)) {
                    return;
                }
                double[] data = (double[])obj;
                boolean hasEnd = out.writeListBegin(data.length, "[double");
                for (int i = 0; i < data.length; ++i) {
                    out.writeDouble(data[i]);
                }
                if (!hasEnd) break;
                out.writeListEnd();
                break;
            }
            case 22: {
                if (out.addRef(obj)) {
                    return;
                }
                String[] data = (String[])obj;
                boolean hasEnd = out.writeListBegin(data.length, "[string");
                for (int i = 0; i < data.length; ++i) {
                    out.writeString(data[i]);
                }
                if (!hasEnd) break;
                out.writeListEnd();
                break;
            }
            case 21: {
                char[] data = (char[])obj;
                out.writeString(data, 0, data.length);
                break;
            }
            case 23: {
                if (out.addRef(obj)) {
                    return;
                }
                Object[] data = (Object[])obj;
                boolean hasEnd = out.writeListBegin(data.length, "[object");
                for (int i = 0; i < data.length; ++i) {
                    out.writeObject(data[i]);
                }
                if (!hasEnd) break;
                out.writeListEnd();
                break;
            }
            case 0: {
                out.writeNull();
                break;
            }
            default: {
                throw new RuntimeException(this.code + " " + String.valueOf(obj.getClass()));
            }
        }
    }
}

