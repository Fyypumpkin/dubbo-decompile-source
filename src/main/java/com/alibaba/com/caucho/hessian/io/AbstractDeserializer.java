/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import com.alibaba.com.caucho.hessian.io.Deserializer;
import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.HessianInput;
import com.alibaba.com.caucho.hessian.io.HessianProtocolException;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;
import java.io.IOException;

public abstract class AbstractDeserializer
implements Deserializer {
    @Override
    public Class getType() {
        return Object.class;
    }

    @Override
    public Object readObject(AbstractHessianInput in) throws IOException {
        Object obj = in.readObject();
        String className = this.getClass().getName();
        if (obj != null) {
            throw this.error(className + ": unexpected object " + obj.getClass().getName() + " (" + obj + ")");
        }
        throw this.error(className + ": unexpected null value");
    }

    @Override
    public Object readList(AbstractHessianInput in, int length) throws IOException {
        throw new UnsupportedOperationException(String.valueOf(this));
    }

    @Override
    public Object readList(AbstractHessianInput in, int length, Class<?> expectType) throws IOException {
        if (expectType == null) {
            return this.readList(in, length);
        }
        throw new UnsupportedOperationException(String.valueOf(this));
    }

    @Override
    public Object readLengthList(AbstractHessianInput in, int length) throws IOException {
        throw new UnsupportedOperationException(String.valueOf(this));
    }

    @Override
    public Object readLengthList(AbstractHessianInput in, int length, Class<?> expectType) throws IOException {
        if (expectType == null) {
            return this.readLengthList(in, length);
        }
        throw new UnsupportedOperationException(String.valueOf(this));
    }

    @Override
    public Object readMap(AbstractHessianInput in) throws IOException {
        Object obj = in.readObject();
        String className = this.getClass().getName();
        if (obj != null) {
            throw this.error(className + ": unexpected object " + obj.getClass().getName() + " (" + obj + ")");
        }
        throw this.error(className + ": unexpected null value");
    }

    @Override
    public Object readMap(AbstractHessianInput in, Class<?> expectKeyType, Class<?> expectValueType) throws IOException {
        if (expectKeyType == null && expectValueType == null) {
            return this.readMap(in);
        }
        throw new UnsupportedOperationException(String.valueOf(this));
    }

    @Override
    public Object readObject(AbstractHessianInput in, String[] fieldNames) throws IOException {
        throw new UnsupportedOperationException(String.valueOf(this));
    }

    protected HessianProtocolException error(String msg) {
        return new HessianProtocolException(msg);
    }

    protected String codeName(int ch) {
        if (ch < 0) {
            return "end of file";
        }
        return "0x" + Integer.toHexString(ch & 255);
    }

    protected SerializerFactory findSerializerFactory(AbstractHessianInput in) {
        SerializerFactory serializerFactory = null;
        if (in instanceof Hessian2Input) {
            serializerFactory = ((Hessian2Input)in).findSerializerFactory();
        } else if (in instanceof HessianInput) {
            serializerFactory = ((HessianInput)in).getSerializerFactory();
        }
        return serializerFactory == null ? new SerializerFactory() : serializerFactory;
    }
}

