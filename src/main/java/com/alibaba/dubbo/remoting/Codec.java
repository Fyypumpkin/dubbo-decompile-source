/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.remoting.Channel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Deprecated
@SPI
public interface Codec {
    public static final Object NEED_MORE_INPUT = new Object();

    @Adaptive(value={"codec"})
    public void encode(Channel var1, OutputStream var2, Object var3) throws IOException;

    @Adaptive(value={"codec"})
    public Object decode(Channel var1, InputStream var2) throws IOException;
}

