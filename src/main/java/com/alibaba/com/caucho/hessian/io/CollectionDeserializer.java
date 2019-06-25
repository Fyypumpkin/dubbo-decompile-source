/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.AbstractHessianInput;
import com.alibaba.com.caucho.hessian.io.AbstractListDeserializer;
import com.alibaba.com.caucho.hessian.io.Deserializer;
import com.alibaba.com.caucho.hessian.io.IOExceptionWrapper;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CollectionDeserializer
extends AbstractListDeserializer {
    private Class _type;

    public CollectionDeserializer(Class type) {
        this._type = type;
    }

    @Override
    public Class getType() {
        return this._type;
    }

    @Override
    public Object readList(AbstractHessianInput in, int length) throws IOException {
        return this.readList(in, length, null);
    }

    @Override
    public Object readList(AbstractHessianInput in, int length, Class<?> expectType) throws IOException {
        Collection list = this.createList();
        in.addRef(list);
        Deserializer deserializer = null;
        SerializerFactory factory = this.findSerializerFactory(in);
        if (expectType != null) {
            deserializer = factory.getDeserializer(expectType.getName());
        }
        while (!in.isEnd()) {
            list.add(deserializer != null ? deserializer.readObject(in) : in.readObject());
        }
        in.readEnd();
        return list;
    }

    @Override
    public Object readLengthList(AbstractHessianInput in, int length) throws IOException {
        return this.readList(in, length, null);
    }

    @Override
    public Object readLengthList(AbstractHessianInput in, int length, Class<?> expectType) throws IOException {
        Collection list = this.createList();
        in.addRef(list);
        Deserializer deserializer = null;
        SerializerFactory factory = this.findSerializerFactory(in);
        if (expectType != null) {
            deserializer = factory.getDeserializer(expectType.getName());
        }
        while (length > 0) {
            list.add(deserializer != null ? deserializer.readObject(in) : in.readObject());
            --length;
        }
        return list;
    }

    private Collection createList() throws IOException {
        Collection list = null;
        if (this._type == null) {
            list = new ArrayList();
        } else if (!this._type.isInterface()) {
            try {
                list = (Collection)this._type.newInstance();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (list == null) {
            if (SortedSet.class.isAssignableFrom(this._type)) {
                list = new TreeSet();
            } else if (Set.class.isAssignableFrom(this._type)) {
                list = new HashSet();
            } else if (List.class.isAssignableFrom(this._type)) {
                list = new ArrayList();
            } else if (Collection.class.isAssignableFrom(this._type)) {
                list = new ArrayList();
            } else {
                try {
                    list = (Collection)this._type.newInstance();
                }
                catch (Exception e) {
                    throw new IOExceptionWrapper(e);
                }
            }
        }
        return list;
    }
}

