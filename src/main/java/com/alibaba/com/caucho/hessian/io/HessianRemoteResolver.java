/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import java.io.IOException;

public interface HessianRemoteResolver {
    public Object lookup(String var1, String var2) throws IOException;
}

