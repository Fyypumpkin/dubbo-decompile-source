/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.kryo;

import java.lang.reflect.Constructor;

public abstract class ReflectionUtils {
    public static boolean checkZeroArgConstructor(Class clazz) {
        try {
            clazz.getDeclaredConstructor(new Class[0]);
            return true;
        }
        catch (NoSuchMethodException e) {
            return false;
        }
    }
}

