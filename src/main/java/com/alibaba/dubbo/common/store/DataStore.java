/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.store;

import com.alibaba.dubbo.common.extension.SPI;
import java.util.Map;

@SPI(value="simple")
public interface DataStore {
    public Map<String, Object> get(String var1);

    public Object get(String var1, String var2);

    public void put(String var1, String var2, Object var3);

    public void remove(String var1, String var2);
}

