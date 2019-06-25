/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.bytecode.Wrapper;
import com.alibaba.dubbo.common.io.Bytes;
import com.alibaba.dubbo.common.json.JSONConverter;
import com.alibaba.dubbo.common.json.JSONNode;
import com.alibaba.dubbo.common.json.JSONWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GenericJSONConverter
implements JSONConverter {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final Map<Class<?>, Encoder> GlobalEncoderMap = new HashMap();
    private static final Map<Class<?>, Decoder> GlobalDecoderMap = new HashMap();

    @Override
    public void writeValue(Object obj, JSONWriter jb, boolean writeClass) throws IOException {
        if (obj == null) {
            jb.valueNull();
            return;
        }
        Class<?> c = obj.getClass();
        Encoder encoder = GlobalEncoderMap.get(c);
        if (encoder != null) {
            encoder.encode(obj, jb);
        } else if (obj instanceof JSONNode) {
            ((JSONNode)obj).writeJSON(this, jb, writeClass);
        } else if (c.isEnum()) {
            jb.valueString(((Enum)obj).name());
        } else if (c.isArray()) {
            int len = Array.getLength(obj);
            jb.arrayBegin();
            for (int i = 0; i < len; ++i) {
                this.writeValue(Array.get(obj, i), jb, writeClass);
            }
            jb.arrayEnd();
        } else if (Map.class.isAssignableFrom(c)) {
            jb.objectBegin();
            for (Map.Entry entry : ((Map)obj).entrySet()) {
                Object key = entry.getKey();
                if (key == null) continue;
                jb.objectItem(key.toString());
                Object value = entry.getValue();
                if (value == null) {
                    jb.valueNull();
                    continue;
                }
                this.writeValue(value, jb, writeClass);
            }
            jb.objectEnd();
        } else if (Collection.class.isAssignableFrom(c)) {
            jb.arrayBegin();
            for (Object item : (Collection)obj) {
                if (item == null) {
                    jb.valueNull();
                    continue;
                }
                this.writeValue(item, jb, writeClass);
            }
            jb.arrayEnd();
        } else {
            String[] pns;
            jb.objectBegin();
            Wrapper w = Wrapper.getWrapper(c);
            for (String pn : pns = w.getPropertyNames()) {
                if (obj instanceof Throwable && ("localizedMessage".equals(pn) || "cause".equals(pn) || "stackTrace".equals(pn))) continue;
                jb.objectItem(pn);
                Object value = w.getPropertyValue(obj, pn);
                if (value == null || value == obj) {
                    jb.valueNull();
                    continue;
                }
                this.writeValue(value, jb, writeClass);
            }
            if (writeClass) {
                jb.objectItem("class");
                this.writeValue(obj.getClass().getName(), jb, writeClass);
            }
            jb.objectEnd();
        }
    }

    @Override
    public Object readValue(Class<?> c, Object jv) throws IOException {
        if (jv == null) {
            return null;
        }
        Decoder decoder = GlobalDecoderMap.get(c);
        if (decoder != null) {
            return decoder.decode(jv);
        }
        if (c.isEnum()) {
            return Enum.valueOf(c, String.valueOf(jv));
        }
        return jv;
    }

    static {
        Encoder e = new Encoder(){

            @Override
            public void encode(Object obj, JSONWriter jb) throws IOException {
                jb.valueBoolean((Boolean)obj);
            }
        };
        GlobalEncoderMap.put(Boolean.TYPE, e);
        GlobalEncoderMap.put(Boolean.class, e);
        e = new Encoder(){

            @Override
            public void encode(Object obj, JSONWriter jb) throws IOException {
                jb.valueInt(((Number)obj).intValue());
            }
        };
        GlobalEncoderMap.put(Integer.TYPE, e);
        GlobalEncoderMap.put(Integer.class, e);
        GlobalEncoderMap.put(Short.TYPE, e);
        GlobalEncoderMap.put(Short.class, e);
        GlobalEncoderMap.put(Byte.TYPE, e);
        GlobalEncoderMap.put(Byte.class, e);
        GlobalEncoderMap.put(AtomicInteger.class, e);
        e = new Encoder(){

            @Override
            public void encode(Object obj, JSONWriter jb) throws IOException {
                jb.valueString(Character.toString(((Character)obj).charValue()));
            }
        };
        GlobalEncoderMap.put(Character.TYPE, e);
        GlobalEncoderMap.put(Character.class, e);
        e = new Encoder(){

            @Override
            public void encode(Object obj, JSONWriter jb) throws IOException {
                jb.valueLong(((Number)obj).longValue());
            }
        };
        GlobalEncoderMap.put(Long.TYPE, e);
        GlobalEncoderMap.put(Long.class, e);
        GlobalEncoderMap.put(AtomicLong.class, e);
        GlobalEncoderMap.put(BigInteger.class, e);
        e = new Encoder(){

            @Override
            public void encode(Object obj, JSONWriter jb) throws IOException {
                jb.valueFloat(((Number)obj).floatValue());
            }
        };
        GlobalEncoderMap.put(Float.TYPE, e);
        GlobalEncoderMap.put(Float.class, e);
        e = new Encoder(){

            @Override
            public void encode(Object obj, JSONWriter jb) throws IOException {
                jb.valueDouble(((Number)obj).doubleValue());
            }
        };
        GlobalEncoderMap.put(Double.TYPE, e);
        GlobalEncoderMap.put(Double.class, e);
        GlobalEncoderMap.put(BigDecimal.class, e);
        e = new Encoder(){

            @Override
            public void encode(Object obj, JSONWriter jb) throws IOException {
                jb.valueString(obj.toString());
            }
        };
        GlobalEncoderMap.put(String.class, e);
        GlobalEncoderMap.put(StringBuilder.class, e);
        GlobalEncoderMap.put(StringBuffer.class, e);
        e = new Encoder(){

            @Override
            public void encode(Object obj, JSONWriter jb) throws IOException {
                jb.valueString(Bytes.bytes2base64((byte[])obj));
            }
        };
        GlobalEncoderMap.put(byte[].class, e);
        e = new Encoder(){

            @Override
            public void encode(Object obj, JSONWriter jb) throws IOException {
                jb.valueString(new SimpleDateFormat(GenericJSONConverter.DATE_FORMAT).format((Date)obj));
            }
        };
        GlobalEncoderMap.put(Date.class, e);
        Decoder d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                return jv.toString();
            }
        };
        GlobalDecoderMap.put(String.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Boolean) {
                    return (boolean)((Boolean)jv);
                }
                return false;
            }
        };
        GlobalDecoderMap.put(Boolean.TYPE, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Boolean) {
                    return (Boolean)jv;
                }
                return null;
            }
        };
        GlobalDecoderMap.put(Boolean.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof String && ((String)jv).length() > 0) {
                    return Character.valueOf(((String)jv).charAt(0));
                }
                return Character.valueOf('\u0000');
            }
        };
        GlobalDecoderMap.put(Character.TYPE, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof String && ((String)jv).length() > 0) {
                    return Character.valueOf(((String)jv).charAt(0));
                }
                return null;
            }
        };
        GlobalDecoderMap.put(Character.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return ((Number)jv).intValue();
                }
                return 0;
            }
        };
        GlobalDecoderMap.put(Integer.TYPE, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return ((Number)jv).intValue();
                }
                return null;
            }
        };
        GlobalDecoderMap.put(Integer.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return ((Number)jv).shortValue();
                }
                return (short)0;
            }
        };
        GlobalDecoderMap.put(Short.TYPE, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return ((Number)jv).shortValue();
                }
                return null;
            }
        };
        GlobalDecoderMap.put(Short.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return ((Number)jv).longValue();
                }
                return 0L;
            }
        };
        GlobalDecoderMap.put(Long.TYPE, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return ((Number)jv).longValue();
                }
                return null;
            }
        };
        GlobalDecoderMap.put(Long.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return Float.valueOf(((Number)jv).floatValue());
                }
                return Float.valueOf(0.0f);
            }
        };
        GlobalDecoderMap.put(Float.TYPE, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return new Float(((Number)jv).floatValue());
                }
                return null;
            }
        };
        GlobalDecoderMap.put(Float.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return ((Number)jv).doubleValue();
                }
                return 0.0;
            }
        };
        GlobalDecoderMap.put(Double.TYPE, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return new Double(((Number)jv).doubleValue());
                }
                return null;
            }
        };
        GlobalDecoderMap.put(Double.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return ((Number)jv).byteValue();
                }
                return (byte)0;
            }
        };
        GlobalDecoderMap.put(Byte.TYPE, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) {
                if (jv instanceof Number) {
                    return ((Number)jv).byteValue();
                }
                return null;
            }
        };
        GlobalDecoderMap.put(Byte.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) throws IOException {
                if (jv instanceof String) {
                    return Bytes.base642bytes((String)jv);
                }
                return null;
            }
        };
        GlobalDecoderMap.put(byte[].class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) throws IOException {
                return new StringBuilder(jv.toString());
            }
        };
        GlobalDecoderMap.put(StringBuilder.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) throws IOException {
                return new StringBuffer(jv.toString());
            }
        };
        GlobalDecoderMap.put(StringBuffer.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) throws IOException {
                if (jv instanceof Number) {
                    return BigInteger.valueOf(((Number)jv).longValue());
                }
                return null;
            }
        };
        GlobalDecoderMap.put(BigInteger.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) throws IOException {
                if (jv instanceof Number) {
                    return BigDecimal.valueOf(((Number)jv).doubleValue());
                }
                return null;
            }
        };
        GlobalDecoderMap.put(BigDecimal.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) throws IOException {
                if (jv instanceof Number) {
                    return new AtomicInteger(((Number)jv).intValue());
                }
                return null;
            }
        };
        GlobalDecoderMap.put(AtomicInteger.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) throws IOException {
                if (jv instanceof Number) {
                    return new AtomicLong(((Number)jv).longValue());
                }
                return null;
            }
        };
        GlobalDecoderMap.put(AtomicLong.class, d);
        d = new Decoder(){

            @Override
            public Object decode(Object jv) throws IOException {
                if (jv instanceof String) {
                    try {
                        return new SimpleDateFormat(GenericJSONConverter.DATE_FORMAT).parse((String)jv);
                    }
                    catch (ParseException e) {
                        throw new IllegalArgumentException(e.getMessage(), e);
                    }
                }
                if (jv instanceof Number) {
                    return new Date(((Number)jv).longValue());
                }
                return null;
            }
        };
        GlobalDecoderMap.put(Date.class, d);
    }

    protected static interface Decoder {
        public Object decode(Object var1) throws IOException;
    }

    protected static interface Encoder {
        public void encode(Object var1, JSONWriter var2) throws IOException;
    }

}

