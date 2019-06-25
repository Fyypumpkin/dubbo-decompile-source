/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.HessianRemote;
import com.alibaba.com.caucho.hessian.io.HessianRemoteResolver;
import java.io.IOException;

public class AbstractHessianResolver
implements HessianRemoteResolver {
    @Override
    public Object lookup(String type, String url) throws IOException {
        return new HessianRemote(type, url);
    }
}

