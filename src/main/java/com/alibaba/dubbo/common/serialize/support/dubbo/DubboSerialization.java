/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.serialize.support.dubbo.GenericObjectInput;
import com.alibaba.dubbo.common.serialize.support.dubbo.GenericObjectOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DubboSerialization
implements Serialization {
    @Override
    public byte getContentTypeId() {
        return 1;
    }

    @Override
    public String getContentType() {
        return "x-application/dubbo";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
        return new GenericObjectOutput(out);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream is) throws IOException {
        return new GenericObjectInput(is);
    }
}

