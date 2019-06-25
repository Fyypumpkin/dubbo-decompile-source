/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.store.support;

import com.alibaba.dubbo.common.store.DataStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimpleDataStore
implements DataStore {
    private ConcurrentMap<String, ConcurrentMap<String, Object>> data = new ConcurrentHashMap<String, ConcurrentMap<String, Object>>();

    @Override
    public Map<String, Object> get(String componentName) {
        ConcurrentMap value = (ConcurrentMap)this.data.get(componentName);
        if (value == null) {
            return new HashMap<String, Object>();
        }
        return new HashMap<String, Object>(value);
    }

    @Override
    public Object get(String componentName, String key) {
        if (!this.data.containsKey(componentName)) {
            return null;
        }
        return ((ConcurrentMap)this.data.get(componentName)).get(key);
    }

    @Override
    public void put(String componentName, String key, Object value) {
        Map componentData = (Map)this.data.get(componentName);
        if (null == componentData) {
            this.data.putIfAbsent(componentName, new ConcurrentHashMap());
            componentData = (Map)this.data.get(componentName);
        }
        componentData.put(key, value);
    }

    @Override
    public void remove(String componentName, String key) {
        if (!this.data.containsKey(componentName)) {
            return;
        }
        ((ConcurrentMap)this.data.get(componentName)).remove(key);
    }
}

