/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.compiler;

import com.alibaba.dubbo.common.extension.SPI;

@SPI(value="javassist")
public interface Compiler {
    public Class<?> compile(String var1, ClassLoader var2);
}

