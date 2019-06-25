/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.SPI;
import java.util.List;

@SPI
public interface RuleConverter {
    public List<URL> convert(URL var1, Object var2);
}

