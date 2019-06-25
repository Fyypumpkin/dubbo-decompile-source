/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.bytecode.Wrapper;
import com.alibaba.dubbo.common.json.JSONConverter;
import com.alibaba.dubbo.common.json.JSONVisitor;
import com.alibaba.dubbo.common.json.ParseException;
import com.alibaba.dubbo.common.utils.Stack;
import com.alibaba.dubbo.common.utils.StringUtils;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class J2oVisitor
implements JSONVisitor {
    public static final boolean[] EMPTY_BOOL_ARRAY = new boolean[0];
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    private Class<?>[] mTypes;
    private Class<?> mType = Object[].class;
    private Object mValue;
    private Wrapper mWrapper;
    private JSONConverter mConverter;
    private Stack<Object> mStack = new Stack();

    J2oVisitor(Class<?> type, JSONConverter jc) {
        this.mType = type;
        this.mConverter = jc;
    }

    J2oVisitor(Class<?>[] types, JSONConverter jc) {
        this.mTypes = types;
        this.mConverter = jc;
    }

    @Override
    public void begin() {
    }

    @Override
    public Object end(Object obj, boolean isValue) throws ParseException {
        this.mStack.clear();
        try {
            return this.mConverter.readValue(this.mType, obj);
        }
        catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void objectBegin() throws ParseException {
        this.mStack.push(this.mValue);
        this.mStack.push(this.mType);
        this.mStack.push(this.mWrapper);
        if (this.mType == Object.class || Map.class.isAssignableFrom(this.mType)) {
            if (!this.mType.isInterface() && this.mType != Object.class) {
                try {
                    this.mValue = this.mType.newInstance();
                }
                catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            } else {
                this.mValue = this.mType == ConcurrentMap.class ? new ConcurrentHashMap() : new HashMap();
            }
            this.mWrapper = null;
        } else {
            try {
                this.mValue = this.mType.newInstance();
                this.mWrapper = Wrapper.getWrapper(this.mType);
            }
            catch (IllegalAccessException e) {
                throw new ParseException(StringUtils.toString(e));
            }
            catch (InstantiationException e) {
                throw new ParseException(StringUtils.toString(e));
            }
        }
    }

    @Override
    public Object objectEnd(int count) {
        Object ret = this.mValue;
        this.mWrapper = (Wrapper)this.mStack.pop();
        this.mType = (Class)this.mStack.pop();
        this.mValue = this.mStack.pop();
        return ret;
    }

    @Override
    public void objectItem(String name) {
        this.mStack.push(name);
        this.mType = this.mWrapper == null ? Object.class : this.mWrapper.getPropertyType(name);
    }

    @Override
    public void objectItemValue(Object obj, boolean isValue) throws ParseException {
        String name = (String)this.mStack.pop();
        if (this.mWrapper == null) {
            ((Map)this.mValue).put(name, obj);
        } else if (this.mType != null) {
            if (isValue && obj != null) {
                try {
                    obj = this.mConverter.readValue(this.mType, obj);
                }
                catch (IOException e) {
                    throw new ParseException(StringUtils.toString(e));
                }
            }
            if (this.mValue instanceof Throwable && "message".equals(name)) {
                try {
                    Field field = Throwable.class.getDeclaredField("detailMessage");
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    field.set(this.mValue, obj);
                }
                catch (NoSuchFieldException e) {
                    throw new ParseException(StringUtils.toString(e));
                }
                catch (IllegalAccessException e) {
                    throw new ParseException(StringUtils.toString(e));
                }
            } else if (!(this.mValue instanceof Throwable && "suppressed".equals(name) || "class".equals(name))) {
                this.mWrapper.setPropertyValue(this.mValue, name, obj);
            }
        }
    }

    @Override
    public void arrayBegin() throws ParseException {
        this.mStack.push(this.mType);
        if (this.mType.isArray()) {
            this.mType = this.mType.getComponentType();
        } else if (this.mType == Object.class || Collection.class.isAssignableFrom(this.mType)) {
            this.mType = Object.class;
        } else {
            throw new ParseException("Convert error, can not load json array data into class [" + this.mType.getName() + "].");
        }
    }

    @Override
    public Object arrayEnd(int count) throws ParseException {
        AbstractCollection ret;
        this.mType = (Class)this.mStack.get(-1 - count);
        if (this.mType.isArray()) {
            ret = J2oVisitor.toArray(this.mType.getComponentType(), this.mStack, count);
        } else {
            AbstractCollection items;
            if (this.mType == Object.class || Collection.class.isAssignableFrom(this.mType)) {
                if (!this.mType.isInterface() && this.mType != Object.class) {
                    try {
                        items = (ArrayList)this.mType.newInstance();
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                } else {
                    items = this.mType.isAssignableFrom(ArrayList.class) ? new ArrayList(count) : (this.mType.isAssignableFrom(HashSet.class) ? new HashSet(count) : (this.mType.isAssignableFrom(LinkedList.class) ? new LinkedList() : new ArrayList(count)));
                }
            } else {
                throw new ParseException("Convert error, can not load json array data into class [" + this.mType.getName() + "].");
            }
            for (int i = 0; i < count; ++i) {
                items.add(this.mStack.remove(i - count));
            }
            ret = items;
        }
        this.mStack.pop();
        return ret;
    }

    @Override
    public void arrayItem(int index) throws ParseException {
        if (this.mTypes != null && this.mStack.size() == index + 1) {
            if (index < this.mTypes.length) {
                this.mType = this.mTypes[index];
            } else {
                throw new ParseException("Can not load json array data into [" + J2oVisitor.name(this.mTypes) + "].");
            }
        }
    }

    @Override
    public void arrayItemValue(int index, Object obj, boolean isValue) throws ParseException {
        if (isValue && obj != null) {
            try {
                obj = this.mConverter.readValue(this.mType, obj);
            }
            catch (IOException e) {
                throw new ParseException(e.getMessage());
            }
        }
        this.mStack.push(obj);
    }

    private static Object toArray(Class<?> c, Stack<Object> list, int len) throws ParseException {
        if (c == String.class) {
            if (len == 0) {
                return EMPTY_STRING_ARRAY;
            }
            String[] ss = new String[len];
            for (int i = len - 1; i >= 0; --i) {
                Object o = list.pop();
                ss[i] = o == null ? null : o.toString();
            }
            return ss;
        }
        if (c == Boolean.TYPE) {
            if (len == 0) {
                return EMPTY_BOOL_ARRAY;
            }
            boolean[] ret = new boolean[len];
            for (int i = len - 1; i >= 0; --i) {
                Object o = list.pop();
                if (!(o instanceof Boolean)) continue;
                ret[i] = (Boolean)o;
            }
            return ret;
        }
        if (c == Integer.TYPE) {
            if (len == 0) {
                return EMPTY_INT_ARRAY;
            }
            int[] ret = new int[len];
            for (int i = len - 1; i >= 0; --i) {
                Object o = list.pop();
                if (!(o instanceof Number)) continue;
                ret[i] = ((Number)o).intValue();
            }
            return ret;
        }
        if (c == Long.TYPE) {
            if (len == 0) {
                return EMPTY_LONG_ARRAY;
            }
            long[] ret = new long[len];
            for (int i = len - 1; i >= 0; --i) {
                Object o = list.pop();
                if (!(o instanceof Number)) continue;
                ret[i] = ((Number)o).longValue();
            }
            return ret;
        }
        if (c == Float.TYPE) {
            if (len == 0) {
                return EMPTY_FLOAT_ARRAY;
            }
            float[] ret = new float[len];
            for (int i = len - 1; i >= 0; --i) {
                Object o = list.pop();
                if (!(o instanceof Number)) continue;
                ret[i] = ((Number)o).floatValue();
            }
            return ret;
        }
        if (c == Double.TYPE) {
            if (len == 0) {
                return EMPTY_DOUBLE_ARRAY;
            }
            double[] ret = new double[len];
            for (int i = len - 1; i >= 0; --i) {
                Object o = list.pop();
                if (!(o instanceof Number)) continue;
                ret[i] = ((Number)o).doubleValue();
            }
            return ret;
        }
        if (c == Byte.TYPE) {
            if (len == 0) {
                return EMPTY_BYTE_ARRAY;
            }
            byte[] ret = new byte[len];
            for (int i = len - 1; i >= 0; --i) {
                Object o = list.pop();
                if (!(o instanceof Number)) continue;
                ret[i] = ((Number)o).byteValue();
            }
            return ret;
        }
        if (c == Character.TYPE) {
            if (len == 0) {
                return EMPTY_CHAR_ARRAY;
            }
            char[] ret = new char[len];
            for (int i = len - 1; i >= 0; --i) {
                Object o = list.pop();
                if (!(o instanceof Character)) continue;
                ret[i] = ((Character)o).charValue();
            }
            return ret;
        }
        if (c == Short.TYPE) {
            if (len == 0) {
                return EMPTY_SHORT_ARRAY;
            }
            short[] ret = new short[len];
            for (int i = len - 1; i >= 0; --i) {
                Object o = list.pop();
                if (!(o instanceof Number)) continue;
                ret[i] = ((Number)o).shortValue();
            }
            return ret;
        }
        Object ret = Array.newInstance(c, len);
        for (int i = len - 1; i >= 0; --i) {
            Array.set(ret, i, list.pop());
        }
        return ret;
    }

    private static String name(Class<?>[] types) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.length; ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(types[i].getName());
        }
        return sb.toString();
    }
}

