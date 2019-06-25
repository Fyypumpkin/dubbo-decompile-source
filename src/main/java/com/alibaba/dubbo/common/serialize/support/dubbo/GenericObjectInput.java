/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.dubbo;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.support.dubbo.Builder;
import com.alibaba.dubbo.common.serialize.support.dubbo.ClassDescriptorMapper;
import com.alibaba.dubbo.common.serialize.support.dubbo.GenericDataInput;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GenericObjectInput
extends GenericDataInput
implements ObjectInput {
    private static Object SKIPPED_OBJECT = new Object();
    private ClassDescriptorMapper mMapper;
    private List<Object> mRefs = new ArrayList<Object>();

    public GenericObjectInput(InputStream is) {
        this(is, Builder.DEFAULT_CLASS_DESCRIPTOR_MAPPER);
    }

    public GenericObjectInput(InputStream is, ClassDescriptorMapper mapper) {
        super(is);
        this.mMapper = mapper;
    }

    public GenericObjectInput(InputStream is, int buffSize) {
        this(is, buffSize, Builder.DEFAULT_CLASS_DESCRIPTOR_MAPPER);
    }

    public GenericObjectInput(InputStream is, int buffSize, ClassDescriptorMapper mapper) {
        super(is, buffSize);
        this.mMapper = mapper;
    }

    @Override
    public Object readObject() throws IOException {
        String desc;
        byte b = this.read0();
        switch (b) {
            case -108: {
                return null;
            }
            case -107: {
                return new Object();
            }
            case -118: {
                desc = this.readUTF();
                break;
            }
            case -117: {
                int index = this.readUInt();
                desc = this.mMapper.getDescriptor(index);
                if (desc != null) break;
                throw new IOException("Can not find desc id: " + index);
            }
            default: {
                throw new IOException("Flag error, expect OBJECT_NULL|OBJECT_DUMMY|OBJECT_DESC|OBJECT_DESC_ID, get " + b);
            }
        }
        try {
            Class<?> c = ReflectUtils.desc2class(desc);
            return Builder.register(c).parseFrom(this);
        }
        catch (ClassNotFoundException e) {
            throw new IOException("Read object failed, class not found. " + StringUtils.toString(e));
        }
    }

    @Override
    public <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException {
        return (T)this.readObject();
    }

    @Override
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return (T)this.readObject();
    }

    public void addRef(Object obj) {
        this.mRefs.add(obj);
    }

    public Object getRef(int index) throws IOException {
        if (index < 0 || index >= this.mRefs.size()) {
            return null;
        }
        Object ret = this.mRefs.get(index);
        if (ret == SKIPPED_OBJECT) {
            throw new IOException("Ref skipped-object.");
        }
        return ret;
    }

    public void skipAny() throws IOException {
        byte b = this.read0();
        switch (b) {
            case -108: 
            case -107: 
            case 10: 
            case 11: 
            case 12: 
            case 13: 
            case 14: 
            case 15: 
            case 16: 
            case 17: 
            case 18: 
            case 19: 
            case 20: 
            case 21: 
            case 22: 
            case 23: 
            case 24: 
            case 25: 
            case 26: 
            case 27: 
            case 28: 
            case 29: 
            case 30: 
            case 31: 
            case 32: 
            case 33: 
            case 34: 
            case 35: 
            case 36: 
            case 37: 
            case 38: 
            case 39: 
            case 40: 
            case 41: 
            case 42: 
            case 43: 
            case 44: 
            case 45: 
            case 46: 
            case 47: 
            case 48: 
            case 49: 
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 54: 
            case 55: 
            case 56: {
                break;
            }
            case 0: {
                this.read0();
                break;
            }
            case 1: {
                this.read0();
                this.read0();
                break;
            }
            case 2: {
                this.read0();
                this.read0();
                this.read0();
                break;
            }
            case 3: {
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                break;
            }
            case 4: {
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                break;
            }
            case 5: {
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                break;
            }
            case 6: {
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                break;
            }
            case 7: {
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                this.read0();
                break;
            }
            case -128: {
                this.addRef(SKIPPED_OBJECT);
                int len = this.readUInt();
                for (int i = 0; i < len; ++i) {
                    this.skipAny();
                }
                break;
            }
            case -127: {
                this.readUInt();
                break;
            }
            case -126: 
            case -125: {
                this.read0(this.readUInt());
                break;
            }
            case -124: {
                this.skipAny();
                break;
            }
            case -123: {
                int len = this.readUInt();
                for (int i = 0; i < len; ++i) {
                    this.skipAny();
                }
                break;
            }
            case -122: {
                int len = this.readUInt();
                for (int i = 0; i < len; ++i) {
                    this.skipAny();
                    this.skipAny();
                }
                break;
            }
            case -118: {
                this.readUTF();
                int len = this.readUInt();
                for (int i = 0; i < len; ++i) {
                    this.skipAny();
                }
                break;
            }
            case -117: {
                this.readUInt();
                int len = this.readUInt();
                for (int i = 0; i < len; ++i) {
                    this.skipAny();
                }
                break;
            }
            default: {
                throw new IOException("Flag error, get " + b);
            }
        }
    }
}

