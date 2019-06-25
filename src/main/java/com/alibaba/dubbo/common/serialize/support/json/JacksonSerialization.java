/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.json;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.serialize.support.json.JacksonObjectInput;
import com.alibaba.dubbo.common.serialize.support.json.JacksonObjectOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JacksonSerialization
implements Serialization {
    @Override
    public byte getContentTypeId() {
        return 20;
    }

    @Override
    public String getContentType() {
        return "text/json";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream output) throws IOException {
        return new JacksonObjectOutput(output);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream input) throws IOException {
        return new JacksonObjectInput(input);
    }
}

