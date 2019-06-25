/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.thrift;

import com.alibaba.dubbo.common.extension.SPI;

@SPI(value="dubbo")
public interface ClassNameGenerator {
    public String generateArgsClassName(String var1, String var2);

    public String generateResultClassName(String var1, String var2);
}

