/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.esotericsoftware.kryo.Kryo
 *  com.esotericsoftware.kryo.Registration
 *  com.esotericsoftware.kryo.Serializer
 *  com.esotericsoftware.kryo.serializers.DefaultSerializers
 *  com.esotericsoftware.kryo.serializers.DefaultSerializers$BigDecimalSerializer
 *  com.esotericsoftware.kryo.serializers.DefaultSerializers$BigIntegerSerializer
 *  de.javakaffee.kryoserializers.ArraysAsListSerializer
 *  de.javakaffee.kryoserializers.BitSetSerializer
 *  de.javakaffee.kryoserializers.GregorianCalendarSerializer
 *  de.javakaffee.kryoserializers.JdkProxySerializer
 *  de.javakaffee.kryoserializers.RegexSerializer
 *  de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer
 *  de.javakaffee.kryoserializers.URISerializer
 *  de.javakaffee.kryoserializers.UUIDSerializer
 *  de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer
 */
package com.alibaba.dubbo.common.serialize.support.kryo;

import com.alibaba.dubbo.common.serialize.support.SerializableClassRegistry;
import com.alibaba.dubbo.common.serialize.support.kryo.CompatibleKryo;
import com.alibaba.dubbo.common.serialize.support.kryo.PooledKryoFactory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.BitSetSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.JdkProxySerializer;
import de.javakaffee.kryoserializers.RegexSerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.URISerializer;
import de.javakaffee.kryoserializers.UUIDSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import java.lang.reflect.InvocationHandler;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public abstract class KryoFactory {
    private static final KryoFactory factory = new PooledKryoFactory();
    private final Set<Class> registrations = new LinkedHashSet<Class>();
    private boolean registrationRequired;
    private volatile boolean kryoCreated;

    protected KryoFactory() {
    }

    public static KryoFactory getDefaultFactory() {
        return factory;
    }

    public void registerClass(Class clazz) {
        if (this.kryoCreated) {
            throw new IllegalStateException("Can't register class after creating kryo instance");
        }
        this.registrations.add(clazz);
    }

    protected Kryo createKryo() {
        if (!this.kryoCreated) {
            this.kryoCreated = true;
        }
        CompatibleKryo kryo = new CompatibleKryo();
        kryo.setRegistrationRequired(this.registrationRequired);
        kryo.register(Arrays.asList("").getClass(), (Serializer)new ArraysAsListSerializer());
        kryo.register(GregorianCalendar.class, (Serializer)new GregorianCalendarSerializer());
        kryo.register(InvocationHandler.class, (Serializer)new JdkProxySerializer());
        kryo.register(BigDecimal.class, (Serializer)new DefaultSerializers.BigDecimalSerializer());
        kryo.register(BigInteger.class, (Serializer)new DefaultSerializers.BigIntegerSerializer());
        kryo.register(Pattern.class, (Serializer)new RegexSerializer());
        kryo.register(BitSet.class, (Serializer)new BitSetSerializer());
        kryo.register(URI.class, (Serializer)new URISerializer());
        kryo.register(UUID.class, (Serializer)new UUIDSerializer());
        UnmodifiableCollectionsSerializer.registerSerializers((Kryo)kryo);
        SynchronizedCollectionsSerializer.registerSerializers((Kryo)kryo);
        kryo.register(HashMap.class);
        kryo.register(ArrayList.class);
        kryo.register(LinkedList.class);
        kryo.register(HashSet.class);
        kryo.register(TreeSet.class);
        kryo.register(Hashtable.class);
        kryo.register(Date.class);
        kryo.register(Calendar.class);
        kryo.register(ConcurrentHashMap.class);
        kryo.register(SimpleDateFormat.class);
        kryo.register(GregorianCalendar.class);
        kryo.register(Vector.class);
        kryo.register(BitSet.class);
        kryo.register(StringBuffer.class);
        kryo.register(StringBuilder.class);
        kryo.register(Object.class);
        kryo.register(Object[].class);
        kryo.register(String[].class);
        kryo.register(byte[].class);
        kryo.register(char[].class);
        kryo.register(int[].class);
        kryo.register(float[].class);
        kryo.register(double[].class);
        for (Class clazz : this.registrations) {
            kryo.register(clazz);
        }
        for (Class clazz : SerializableClassRegistry.getRegisteredClasses()) {
            kryo.register(clazz);
        }
        return kryo;
    }

    public void returnKryo(Kryo kryo) {
    }

    public void setRegistrationRequired(boolean registrationRequired) {
        this.registrationRequired = registrationRequired;
    }

    public void close() {
    }

    public abstract Kryo getKryo();
}

