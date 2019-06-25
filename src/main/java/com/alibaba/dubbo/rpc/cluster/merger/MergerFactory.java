/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.rpc.cluster.Merger;
import com.alibaba.dubbo.rpc.cluster.merger.ArrayMerger;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MergerFactory {
    private static final ConcurrentMap<Class<?>, Merger<?>> mergerCache = new ConcurrentHashMap();

    public static <T> Merger<T> getMerger(Class<T> returnType) {
        Merger result;
        if (returnType.isArray()) {
            Class<?> type = returnType.getComponentType();
            result = (Merger)mergerCache.get(type);
            if (result == null) {
                MergerFactory.loadMergers();
                result = (Merger)mergerCache.get(type);
            }
            if (result == null && !type.isPrimitive()) {
                result = ArrayMerger.INSTANCE;
            }
        } else {
            result = (Merger)mergerCache.get(returnType);
            if (result == null) {
                MergerFactory.loadMergers();
                result = (Merger)mergerCache.get(returnType);
            }
        }
        return result;
    }

    static void loadMergers() {
        Set<String> names = ExtensionLoader.getExtensionLoader(Merger.class).getSupportedExtensions();
        for (String name : names) {
            Merger m = ExtensionLoader.getExtensionLoader(Merger.class).getExtension(name);
            mergerCache.putIfAbsent(ReflectUtils.getGenericClass(m.getClass()), m);
        }
    }
}

