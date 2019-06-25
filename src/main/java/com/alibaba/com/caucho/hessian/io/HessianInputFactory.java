/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.HessianInput;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class HessianInputFactory {
    public static final Logger log = Logger.getLogger(HessianInputFactory.class.getName());
    private SerializerFactory _serializerFactory;

    public void setSerializerFactory(SerializerFactory factory) {
        this._serializerFactory = factory;
    }

    public SerializerFactory getSerializerFactory() {
        return this._serializerFactory;
    }

    public AbstractHessianInput open(InputStream is) throws IOException {
        int code = is.read();
        int major = is.read();
        int minor = is.read();
        switch (code) {
            case 67: 
            case 82: 
            case 99: 
            case 114: {
                if (major >= 2) {
                    Hessian2Input in = new Hessian2Input(is);
                    ((AbstractHessianInput)in).setSerializerFactory(this._serializerFactory);
                    return in;
                }
                HessianInput in = new HessianInput(is);
                ((AbstractHessianInput)in).setSerializerFactory(this._serializerFactory);
                return in;
            }
        }
        throw new IOException((char)code + " is an unknown Hessian message code.");
    }
}

