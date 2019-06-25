/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.json.JSONWriter;
import java.io.IOException;

public interface JSONConverter {
    public void writeValue(Object var1, JSONWriter var2, boolean var3) throws IOException;

    public Object readValue(Class<?> var1, Object var2) throws IOException;
}

