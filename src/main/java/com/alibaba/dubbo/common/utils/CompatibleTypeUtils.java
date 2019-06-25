/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.utils.ReflectUtils;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class CompatibleTypeUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private CompatibleTypeUtils() {
    }

    public static Object compatibleTypeConvert(Object value, Class<?> type) {
        if (value == null || type == null || type.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (value instanceof String) {
            String string = (String)value;
            if (Character.TYPE.equals(type) || Character.class.equals(type)) {
                if (string.length() != 1) {
                    throw new IllegalArgumentException(String.format("CAN NOT convert String(%s) to char! when convert String to char, the String MUST only 1 char.", string));
                }
                return Character.valueOf(string.charAt(0));
            }
            if (type.isEnum()) {
                return Enum.valueOf(type, string);
            }
            if (type == BigInteger.class) {
                return new BigInteger(string);
            }
            if (type == BigDecimal.class) {
                return new BigDecimal(string);
            }
            if (type == Short.class || type == Short.TYPE) {
                return new Short(string);
            }
            if (type == Integer.class || type == Integer.TYPE) {
                return new Integer(string);
            }
            if (type == Long.class || type == Long.TYPE) {
                return new Long(string);
            }
            if (type == Double.class || type == Double.TYPE) {
                return new Double(string);
            }
            if (type == Float.class || type == Float.TYPE) {
                return new Float(string);
            }
            if (type == Byte.class || type == Byte.TYPE) {
                return new Byte(string);
            }
            if (type == Boolean.class || type == Boolean.TYPE) {
                return new Boolean(string);
            }
            if (type == Date.class) {
                try {
                    return new SimpleDateFormat(DATE_FORMAT).parse((String)value);
                }
                catch (ParseException e) {
                    throw new IllegalStateException("Failed to parse date " + value + " by format " + DATE_FORMAT + ", cause: " + e.getMessage(), e);
                }
            }
            if (type == Class.class) {
                try {
                    return ReflectUtils.name2class((String)value);
                }
                catch (ClassNotFoundException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        } else if (value instanceof Number) {
            Number number = (Number)value;
            if (type == Byte.TYPE || type == Byte.class) {
                return number.byteValue();
            }
            if (type == Short.TYPE || type == Short.class) {
                return number.shortValue();
            }
            if (type == Integer.TYPE || type == Integer.class) {
                return number.intValue();
            }
            if (type == Long.TYPE || type == Long.class) {
                return number.longValue();
            }
            if (type == Float.TYPE || type == Float.class) {
                return Float.valueOf(number.floatValue());
            }
            if (type == Double.TYPE || type == Double.class) {
                return number.doubleValue();
            }
            if (type == BigInteger.class) {
                return BigInteger.valueOf(number.longValue());
            }
            if (type == BigDecimal.class) {
                return BigDecimal.valueOf(number.doubleValue());
            }
            if (type == Date.class) {
                return new Date(number.longValue());
            }
            if (type == LocalDateTime.class) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()), TimeZone.getDefault().toZoneId());
            }
            if (type == LocalDate.class) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()), TimeZone.getDefault().toZoneId()).toLocalDate();
            }
            if (type == LocalTime.class) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()), TimeZone.getDefault().toZoneId()).toLocalTime();
            }
        } else if (value instanceof Collection) {
            Collection collection = (Collection)value;
            if (type.isArray()) {
                int length = collection.size();
                Object array = Array.newInstance(type.getComponentType(), length);
                int i = 0;
                for (Object item : collection) {
                    Array.set(array, i++, item);
                }
                return array;
            }
            if (!type.isInterface()) {
                try {
                    Collection result = (Collection)type.newInstance();
                    result.addAll(collection);
                    return result;
                }
                catch (Throwable result) {}
            } else {
                if (type == List.class) {
                    return new ArrayList(collection);
                }
                if (type == Set.class) {
                    return new HashSet(collection);
                }
            }
        } else if (value.getClass().isArray() && Collection.class.isAssignableFrom(type)) {
            AbstractCollection collection;
            if (!type.isInterface()) {
                try {
                    collection = (ArrayList)type.newInstance();
                }
                catch (Throwable e) {
                    collection = new ArrayList();
                }
            } else {
                collection = type == Set.class ? new HashSet() : new ArrayList();
            }
            int length = Array.getLength(value);
            for (int i = 0; i < length; ++i) {
                collection.add(Array.get(value, i));
            }
            return collection;
        }
        return value;
    }
}

