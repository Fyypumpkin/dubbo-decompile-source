/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  de.ruedigermoeller.serialization.FSTConfiguration
 *  de.ruedigermoeller.serialization.FSTObjectInput
 *  de.ruedigermoeller.serialization.FSTObjectOutput
 */
package com.alibaba.dubbo.common.serialize.support.fst;

import com.alibaba.dubbo.common.serialize.support.SerializableClassRegistry;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import java.io.InputStream;
import java.io.OutputStream;

public class FstFactory {
    private static final FstFactory factory = new FstFactory();
    private final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    public static FstFactory getDefaultFactory() {
        return factory;
    }

    public FstFactory() {
        for (Class clazz : SerializableClassRegistry.getRegisteredClasses()) {
            this.conf.registerClass(new Class[]{clazz});
        }
    }

    public FSTObjectOutput getObjectOutput(OutputStream outputStream) {
        return this.conf.getObjectOutput(outputStream);
    }

    public FSTObjectInput getObjectInput(InputStream inputStream) {
        return this.conf.getObjectInput(inputStream);
    }
}

