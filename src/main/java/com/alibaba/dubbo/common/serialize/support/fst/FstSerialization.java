/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.fst;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.OptimizedSerialization;
import com.alibaba.dubbo.common.serialize.support.fst.FstObjectInput;
import com.alibaba.dubbo.common.serialize.support.fst.FstObjectOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FstSerialization
implements OptimizedSerialization {
    @Override
    public byte getContentTypeId() {
        return 9;
    }

    @Override
    public String getContentType() {
        return "x-application/fst";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
        return new FstObjectOutput(out);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream is) throws IOException {
        return new FstObjectInput(is);
    }
}

