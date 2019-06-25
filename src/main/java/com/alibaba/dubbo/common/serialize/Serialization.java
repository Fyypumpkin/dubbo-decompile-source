/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SPI(value="hessian2")
public interface Serialization {
    public byte getContentTypeId();

    public String getContentType();

    @Adaptive
    public ObjectOutput serialize(URL var1, OutputStream var2) throws IOException;

    @Adaptive
    public ObjectInput deserialize(URL var1, InputStream var2) throws IOException;
}

