/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.java;

import com.alibaba.dubbo.common.utils.ClassHelper;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

public class CompactedObjectInputStream
extends ObjectInputStream {
    private ClassLoader mClassLoader;

    public CompactedObjectInputStream(InputStream in) throws IOException {
        this(in, Thread.currentThread().getContextClassLoader());
    }

    public CompactedObjectInputStream(InputStream in, ClassLoader cl) throws IOException {
        super(in);
        this.mClassLoader = cl == null ? ClassHelper.getClassLoader() : cl;
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        int type = this.read();
        if (type < 0) {
            throw new EOFException();
        }
        switch (type) {
            case 0: {
                return super.readClassDescriptor();
            }
            case 1: {
                Class<?> clazz = this.loadClass(this.readUTF());
                return ObjectStreamClass.lookup(clazz);
            }
        }
        throw new StreamCorruptedException("Unexpected class descriptor type: " + type);
    }

    private Class<?> loadClass(String className) throws ClassNotFoundException {
        return this.mClassLoader.loadClass(className);
    }
}

