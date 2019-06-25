/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.dubbo;

import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.support.dubbo.Builder;
import com.alibaba.dubbo.common.serialize.support.dubbo.ClassDescriptorMapper;
import com.alibaba.dubbo.common.serialize.support.dubbo.GenericDataOutput;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GenericObjectOutput
extends GenericDataOutput
implements ObjectOutput {
    private ClassDescriptorMapper mMapper;
    private Map<Object, Integer> mRefs = new ConcurrentHashMap<Object, Integer>();
    private final boolean isAllowNonSerializable;

    public GenericObjectOutput(OutputStream out) {
        this(out, Builder.DEFAULT_CLASS_DESCRIPTOR_MAPPER);
    }

    public GenericObjectOutput(OutputStream out, ClassDescriptorMapper mapper) {
        super(out);
        this.mMapper = mapper;
        this.isAllowNonSerializable = false;
    }

    public GenericObjectOutput(OutputStream out, int buffSize) {
        this(out, buffSize, Builder.DEFAULT_CLASS_DESCRIPTOR_MAPPER, false);
    }

    public GenericObjectOutput(OutputStream out, int buffSize, ClassDescriptorMapper mapper) {
        this(out, buffSize, mapper, false);
    }

    public GenericObjectOutput(OutputStream out, int buffSize, ClassDescriptorMapper mapper, boolean isAllowNonSerializable) {
        super(out, buffSize);
        this.mMapper = mapper;
        this.isAllowNonSerializable = isAllowNonSerializable;
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
            this.write0((byte)-108);
            return;
        }
        Class<?> c = obj.getClass();
        if (c == Object.class) {
            this.write0((byte)-107);
        } else {
            String desc = ReflectUtils.getDesc(c);
            int index = this.mMapper.getDescriptorIndex(desc);
            if (index < 0) {
                this.write0((byte)-118);
                this.writeUTF(desc);
            } else {
                this.write0((byte)-117);
                this.writeUInt(index);
            }
            Builder<?> b = Builder.register(c, this.isAllowNonSerializable);
            b.writeTo(obj, this);
        }
    }

    public void addRef(Object obj) {
        this.mRefs.put(obj, this.mRefs.size());
    }

    public int getRef(Object obj) {
        Integer ref = this.mRefs.get(obj);
        if (ref == null) {
            return -1;
        }
        return ref;
    }
}

