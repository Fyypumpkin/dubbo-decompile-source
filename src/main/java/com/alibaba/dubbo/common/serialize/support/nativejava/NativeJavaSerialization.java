/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.nativejava;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.serialize.support.nativejava.NativeJavaObjectInput;
import com.alibaba.dubbo.common.serialize.support.nativejava.NativeJavaObjectOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NativeJavaSerialization
implements Serialization {
    public static final String NAME = "nativejava";

    @Override
    public byte getContentTypeId() {
        return 7;
    }

    @Override
    public String getContentType() {
        return "x-application/nativejava";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream output) throws IOException {
        return new NativeJavaObjectOutput(output);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream input) throws IOException {
        return new NativeJavaObjectInput(input);
    }
}

