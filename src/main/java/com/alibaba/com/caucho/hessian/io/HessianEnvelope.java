/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import java.io.IOException;

public abstract class HessianEnvelope {
    public abstract Hessian2Output wrap(Hessian2Output var1) throws IOException;

    public abstract Hessian2Input unwrap(Hessian2Input var1) throws IOException;

    public abstract Hessian2Input unwrapHeaders(Hessian2Input var1) throws IOException;
}

