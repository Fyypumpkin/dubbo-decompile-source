/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support;

import java.util.Collection;

public interface SerializationOptimizer {
    public Collection<Class> getSerializableClasses();
}

