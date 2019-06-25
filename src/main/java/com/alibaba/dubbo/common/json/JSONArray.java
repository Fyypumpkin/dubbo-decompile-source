/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.json.JSONConverter;
import com.alibaba.dubbo.common.json.JSONNode;
import com.alibaba.dubbo.common.json.JSONObject;
import com.alibaba.dubbo.common.json.JSONWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JSONArray
implements JSONNode {
    private List<Object> mArray = new ArrayList<Object>();

    public Object get(int index) {
        return this.mArray.get(index);
    }

    public boolean getBoolean(int index, boolean def) {
        Object tmp = this.mArray.get(index);
        return tmp != null && tmp instanceof Boolean ? (Boolean)tmp : def;
    }

    public int getInt(int index, int def) {
        Object tmp = this.mArray.get(index);
        return tmp != null && tmp instanceof Number ? ((Number)tmp).intValue() : def;
    }

    public long getLong(int index, long def) {
        Object tmp = this.mArray.get(index);
        return tmp != null && tmp instanceof Number ? ((Number)tmp).longValue() : def;
    }

    public float getFloat(int index, float def) {
        Object tmp = this.mArray.get(index);
        return tmp != null && tmp instanceof Number ? ((Number)tmp).floatValue() : def;
    }

    public double getDouble(int index, double def) {
        Object tmp = this.mArray.get(index);
        return tmp != null && tmp instanceof Number ? ((Number)tmp).doubleValue() : def;
    }

    public String getString(int index) {
        Object tmp = this.mArray.get(index);
        return tmp == null ? null : tmp.toString();
    }

    public JSONArray getArray(int index) {
        Object tmp = this.mArray.get(index);
        return tmp == null ? null : (tmp instanceof JSONArray ? (JSONArray)tmp : null);
    }

    public JSONObject getObject(int index) {
        Object tmp = this.mArray.get(index);
        return tmp == null ? null : (tmp instanceof JSONObject ? (JSONObject)tmp : null);
    }

    public int length() {
        return this.mArray.size();
    }

    public void add(Object ele) {
        this.mArray.add(ele);
    }

    public void addAll(Object[] eles) {
        for (Object ele : eles) {
            this.mArray.add(ele);
        }
    }

    public void addAll(Collection<?> c) {
        this.mArray.addAll(c);
    }

    @Override
    public void writeJSON(JSONConverter jc, JSONWriter jb, boolean writeClass) throws IOException {
        jb.arrayBegin();
        for (Object item : this.mArray) {
            if (item == null) {
                jb.valueNull();
                continue;
            }
            jc.writeValue(item, jb, writeClass);
        }
        jb.arrayEnd();
    }
}

