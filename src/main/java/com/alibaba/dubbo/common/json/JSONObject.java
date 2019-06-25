/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.json.JSONArray;
import com.alibaba.dubbo.common.json.JSONConverter;
import com.alibaba.dubbo.common.json.JSONNode;
import com.alibaba.dubbo.common.json.JSONWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JSONObject
implements JSONNode {
    private Map<String, Object> mMap = new HashMap<String, Object>();

    public Object get(String key) {
        return this.mMap.get(key);
    }

    public boolean getBoolean(String key, boolean def) {
        Object tmp = this.mMap.get(key);
        return tmp != null && tmp instanceof Boolean ? (Boolean)tmp : def;
    }

    public int getInt(String key, int def) {
        Object tmp = this.mMap.get(key);
        return tmp != null && tmp instanceof Number ? ((Number)tmp).intValue() : def;
    }

    public long getLong(String key, long def) {
        Object tmp = this.mMap.get(key);
        return tmp != null && tmp instanceof Number ? ((Number)tmp).longValue() : def;
    }

    public float getFloat(String key, float def) {
        Object tmp = this.mMap.get(key);
        return tmp != null && tmp instanceof Number ? ((Number)tmp).floatValue() : def;
    }

    public double getDouble(String key, double def) {
        Object tmp = this.mMap.get(key);
        return tmp != null && tmp instanceof Number ? ((Number)tmp).doubleValue() : def;
    }

    public String getString(String key) {
        Object tmp = this.mMap.get(key);
        return tmp == null ? null : tmp.toString();
    }

    public JSONArray getArray(String key) {
        Object tmp = this.mMap.get(key);
        return tmp == null ? null : (tmp instanceof JSONArray ? (JSONArray)tmp : null);
    }

    public JSONObject getObject(String key) {
        Object tmp = this.mMap.get(key);
        return tmp == null ? null : (tmp instanceof JSONObject ? (JSONObject)tmp : null);
    }

    public Iterator<String> keys() {
        return this.mMap.keySet().iterator();
    }

    public boolean contains(String key) {
        return this.mMap.containsKey(key);
    }

    public void put(String name, Object value) {
        this.mMap.put(name, value);
    }

    public void putAll(String[] names, Object[] values) {
        int len = Math.min(names.length, values.length);
        for (int i = 0; i < len; ++i) {
            this.mMap.put(names[i], values[i]);
        }
    }

    public void putAll(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            this.mMap.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void writeJSON(JSONConverter jc, JSONWriter jb, boolean writeClass) throws IOException {
        jb.objectBegin();
        for (Map.Entry<String, Object> entry : this.mMap.entrySet()) {
            String key = entry.getKey();
            jb.objectItem(key);
            Object value = entry.getValue();
            if (value == null) {
                jb.valueNull();
                continue;
            }
            jc.writeValue(value, jb, writeClass);
        }
        jb.objectEnd();
    }
}

