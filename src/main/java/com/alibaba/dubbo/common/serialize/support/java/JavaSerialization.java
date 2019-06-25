/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.java;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.serialize.support.java.JavaObjectInput;
import com.alibaba.dubbo.common.serialize.support.java.JavaObjectOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JavaSerialization
implements Serialization {
    @Override
    public byte getContentTypeId() {
        return 3;
    }

    @Override
    public String getContentType() {
        return "x-application/java";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
        return new JavaObjectOutput(out);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream is) throws IOException {
        return new JavaObjectInput(is);
    }
}

