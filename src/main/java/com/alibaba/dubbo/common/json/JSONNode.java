/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.json.JSONConverter;
import com.alibaba.dubbo.common.json.JSONWriter;
import java.io.IOException;

interface JSONNode {
    public void writeJSON(JSONConverter var1, JSONWriter var2, boolean var3) throws IOException;
}

