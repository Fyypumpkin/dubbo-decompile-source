/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import com.alibaba.com.caucho.hessian.io.AbstractMapDeserializer;
import com.alibaba.com.caucho.hessian.io.Deserializer;
import com.alibaba.com.caucho.hessian.io.IOExceptionWrapper;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MapDeserializer
extends AbstractMapDeserializer {
    private Class _type;
    private Constructor _ctor;

    public MapDeserializer(Class type) {
        if (type == null) {
            type = HashMap.class;
        }
        this._type = type;
        Constructor<?>[] ctors = type.getConstructors();
        for (int i = 0; i < ctors.length; ++i) {
            if (ctors[i].getParameterTypes().length != 0) continue;
            this._ctor = ctors[i];
        }
        if (this._ctor == null) {
            try {
                this._ctor = HashMap.class.getConstructor(new Class[0]);
            }
            catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public Class getType() {
        if (this._type != null) {
            return this._type;
        }
        return HashMap.class;
    }

    @Override
    public Object readMap(AbstractHessianInput in) throws IOException {
        return this.readMap(in, null, null);
    }

    @Override
    public Object readMap(AbstractHessianInput in, Class<?> expectKeyType, Class<?> expectValueType) throws IOException {
        Map map;
        if (this._type == null) {
            map = new HashMap();
        } else if (this._type.equals(Map.class)) {
            map = new HashMap();
        } else if (this._type.equals(SortedMap.class)) {
            map = new TreeMap();
        } else {
            try {
                map = (Map)this._ctor.newInstance(new Object[0]);
            }
            catch (Exception e) {
                throw new IOExceptionWrapper(e);
            }
        }
        in.addRef(map);
        this.doReadMap(in, map, expectKeyType, expectValueType);
        in.readEnd();
        return map;
    }

    protected void doReadMap(AbstractHessianInput in, Map map, Class<?> keyType, Class<?> valueType) throws IOException {
        Deserializer keyDeserializer = null;
        Deserializer valueDeserializer = null;
        SerializerFactory factory = this.findSerializerFactory(in);
        if (keyType != null) {
            keyDeserializer = factory.getDeserializer(keyType.getName());
        }
        if (valueType != null) {
            valueDeserializer = factory.getDeserializer(valueType.getName());
        }
        while (!in.isEnd()) {
            map.put(keyDeserializer != null ? keyDeserializer.readObject(in) : in.readObject(), valueDeserializer != null ? valueDeserializer.readObject(in) : in.readObject());
        }
    }

    @Override
    public Object readObject(AbstractHessianInput in, String[] fieldNames) throws IOException {
        Map map = this.createMap();
        int ref = in.addRef(map);
        for (int i = 0; i < fieldNames.length; ++i) {
            String name = fieldNames[i];
            map.put(name, in.readObject());
        }
        return map;
    }

    private Map createMap() throws IOException {
        if (this._type == null) {
            return new HashMap();
        }
        if (this._type.equals(Map.class)) {
            return new HashMap();
        }
        if (this._type.equals(SortedMap.class)) {
            return new TreeMap();
        }
        try {
            return (Map)this._ctor.newInstance(new Object[0]);
        }
        catch (Exception e) {
            throw new IOExceptionWrapper(e);
        }
    }
}

