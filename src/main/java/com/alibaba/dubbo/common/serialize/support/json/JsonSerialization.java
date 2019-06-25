/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.json;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.serialize.support.json.JsonObjectInput;
import com.alibaba.dubbo.common.serialize.support.json.JsonObjectOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JsonSerialization
implements Serialization {
    @Override
    public byte getContentTypeId() {
        return 5;
    }

    @Override
    public String getContentType() {
        return "text/json";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream output) throws IOException {
        return new JsonObjectOutput(output, url.getParameter("with.class", true));
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream input) throws IOException {
        return new JsonObjectInput(input);
    }
}

