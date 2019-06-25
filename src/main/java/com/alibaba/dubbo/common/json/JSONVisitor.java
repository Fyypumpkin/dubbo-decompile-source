/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.json.ParseException;

public interface JSONVisitor {
    public static final String CLASS_PROPERTY = "class";

    public void begin();

    public Object end(Object var1, boolean var2) throws ParseException;

    public void objectBegin() throws ParseException;

    public Object objectEnd(int var1) throws ParseException;

    public void objectItem(String var1) throws ParseException;

    public void objectItemValue(Object var1, boolean var2) throws ParseException;

    public void arrayBegin() throws ParseException;

    public Object arrayEnd(int var1) throws ParseException;

    public void arrayItem(int var1) throws ParseException;

    public void arrayItemValue(int var1, Object var2, boolean var3) throws ParseException;
}

