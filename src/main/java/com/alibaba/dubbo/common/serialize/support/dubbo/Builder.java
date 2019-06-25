/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.serialize.support.dubbo;

import com.alibaba.dubbo.common.bytecode.ClassGenerator;
import com.alibaba.dubbo.common.io.UnsafeByteArrayInputStream;
import com.alibaba.dubbo.common.io.UnsafeByteArrayOutputStream;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.serialize.support.dubbo.ClassDescriptorMapper;
import com.alibaba.dubbo.common.serialize.support.dubbo.GenericDataFlags;
import com.alibaba.dubbo.common.serialize.support.dubbo.GenericObjectInput;
import com.alibaba.dubbo.common.serialize.support.dubbo.GenericObjectOutput;
import com.alibaba.dubbo.common.serialize.support.java.CompactedObjectInputStream;
import com.alibaba.dubbo.common.serialize.support.java.CompactedObjectOutputStream;
import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.common.utils.IOUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Builder<T>
implements GenericDataFlags {
    protected static Logger logger = LoggerFactory.getLogger(Builder.class);
    private static final AtomicLong BUILDER_CLASS_COUNTER = new AtomicLong(0L);
    private static final String BUILDER_CLASS_NAME = Builder.class.getName();
    private static final Map<Class<?>, Builder<?>> BuilderMap = new ConcurrentHashMap<Class<?>, Builder<?>>();
    private static final Map<Class<?>, Builder<?>> nonSerializableBuilderMap = new ConcurrentHashMap<Class<?>, Builder<?>>();
    private static final String FIELD_CONFIG_SUFFIX = ".fc";
    private static final int MAX_FIELD_CONFIG_FILE_SIZE = 16384;
    private static final Comparator<String> FNC = new Comparator<String>(){

        @Override
        public int compare(String n1, String n2) {
            return Builder.compareFieldName(n1, n2);
        }
    };
    private static final Comparator<Field> FC = new Comparator<Field>(){

        @Override
        public int compare(Field f1, Field f2) {
            return Builder.compareFieldName(f1.getName(), f2.getName());
        }
    };
    private static final Comparator<Constructor> CC = new Comparator<Constructor>(){

        @Override
        public int compare(Constructor o1, Constructor o2) {
            return o1.getParameterTypes().length - o2.getParameterTypes().length;
        }
    };
    private static final List<String> mDescList = new ArrayList<String>();
    private static final Map<String, Integer> mDescMap = new ConcurrentHashMap<String, Integer>();
    public static ClassDescriptorMapper DEFAULT_CLASS_DESCRIPTOR_MAPPER = new ClassDescriptorMapper(){

        @Override
        public String getDescriptor(int index) {
            if (index < 0 || index >= mDescList.size()) {
                return null;
            }
            return (String)mDescList.get(index);
        }

        @Override
        public int getDescriptorIndex(String desc) {
            Integer ret = (Integer)mDescMap.get(desc);
            return ret == null ? -1 : ret;
        }
    };
    static final Builder<Object> GenericBuilder = new Builder<Object>(){

        @Override
        public Class<Object> getType() {
            return Object.class;
        }

        @Override
        public void writeTo(Object obj, GenericObjectOutput out) throws IOException {
            out.writeObject(obj);
        }

        @Override
        public Object parseFrom(GenericObjectInput in) throws IOException {
            return in.readObject();
        }
    };
    static final Builder<Object[]> GenericArrayBuilder = new AbstractObjectBuilder<Object[]>(){

        @Override
        public Class<Object[]> getType() {
            return Object[].class;
        }

        @Override
        protected Object[] newInstance(GenericObjectInput in) throws IOException {
            return new Object[in.readUInt()];
        }

        @Override
        protected void readObject(Object[] ret, GenericObjectInput in) throws IOException {
            for (int i = 0; i < ret.length; ++i) {
                ret[i] = in.readObject();
            }
        }

        @Override
        protected void writeObject(Object[] obj, GenericObjectOutput out) throws IOException {
            out.writeUInt(obj.length);
            for (Object item : obj) {
                out.writeObject(item);
            }
        }
    };
    static final Builder<Serializable> SerializableBuilder = new Builder<Serializable>(){

        @Override
        public Class<Serializable> getType() {
            return Serializable.class;
        }

        @Override
        public void writeTo(Serializable obj, GenericObjectOutput out) throws IOException {
            if (obj == null) {
                out.write0((byte)-108);
            } else {
                out.write0((byte)-126);
                UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream();
                CompactedObjectOutputStream oos = new CompactedObjectOutputStream(bos);
                oos.writeObject(obj);
                oos.flush();
                bos.close();
                byte[] b = bos.toByteArray();
                out.writeUInt(b.length);
                out.write0(b, 0, b.length);
            }
        }

        @Override
        public Serializable parseFrom(GenericObjectInput in) throws IOException {
            byte b = in.read0();
            if (b == -108) {
                return null;
            }
            if (b != -126) {
                throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_STREAM, get " + b + ".");
            }
            UnsafeByteArrayInputStream bis = new UnsafeByteArrayInputStream(in.read0(in.readUInt()));
            CompactedObjectInputStream ois = new CompactedObjectInputStream(bis);
            try {
                return (Serializable)ois.readObject();
            }
            catch (ClassNotFoundException e) {
                throw new IOException(StringUtils.toString(e));
            }
        }
    };

    protected Builder() {
    }

    public abstract Class<T> getType();

    public void writeTo(T obj, OutputStream os) throws IOException {
        GenericObjectOutput out = new GenericObjectOutput(os);
        this.writeTo(obj, out);
        out.flushBuffer();
    }

    public T parseFrom(byte[] b) throws IOException {
        return this.parseFrom(new UnsafeByteArrayInputStream(b));
    }

    public T parseFrom(InputStream is) throws IOException {
        return this.parseFrom(new GenericObjectInput(is));
    }

    public abstract void writeTo(T var1, GenericObjectOutput var2) throws IOException;

    public abstract T parseFrom(GenericObjectInput var1) throws IOException;

    public static <T> Builder<T> register(Class<T> c, boolean isAllowNonSerializable) {
        if (c == Object.class || c.isInterface()) {
            return GenericBuilder;
        }
        if (c == Object[].class) {
            return GenericArrayBuilder;
        }
        Builder<Object> b = BuilderMap.get(c);
        if (null != b) {
            return b;
        }
        boolean isSerializable = Serializable.class.isAssignableFrom(c);
        if (!isAllowNonSerializable && !isSerializable) {
            throw new IllegalStateException("Serialized class " + c.getName() + " must implement java.io.Serializable (dubbo codec setting: isAllowNonSerializable = false)");
        }
        b = nonSerializableBuilderMap.get(c);
        if (null != b) {
            return b;
        }
        b = Builder.newBuilder(c);
        if (isSerializable) {
            BuilderMap.put(c, b);
        } else {
            nonSerializableBuilderMap.put(c, b);
        }
        return b;
    }

    public static <T> Builder<T> register(Class<T> c) {
        return Builder.register(c, false);
    }

    public static <T> void register(Class<T> c, Builder<T> b) {
        if (Serializable.class.isAssignableFrom(c)) {
            BuilderMap.put(c, b);
        } else {
            nonSerializableBuilderMap.put(c, b);
        }
    }

    private static <T> Builder<T> newBuilder(Class<T> c) {
        if (c.isPrimitive()) {
            throw new RuntimeException("Can not create builder for primitive type: " + c);
        }
        if (logger.isInfoEnabled()) {
            logger.info("create Builder for class: " + c);
        }
        Builder<T> builder = c.isArray() ? Builder.newArrayBuilder(c) : Builder.newObjectBuilder(c);
        return builder;
    }

    private static Builder<?> newArrayBuilder(Class<?> c) {
        Class<?> cc = c.getComponentType();
        if (cc.isInterface()) {
            return GenericArrayBuilder;
        }
        ClassLoader cl = ClassHelper.getCallerClassLoader(Builder.class);
        String cn = ReflectUtils.getName(c);
        String ccn = ReflectUtils.getName(cc);
        String bcn = BUILDER_CLASS_NAME + "$bc" + BUILDER_CLASS_COUNTER.getAndIncrement();
        int ix = cn.indexOf(93);
        String s1 = cn.substring(0, ix);
        String s2 = cn.substring(ix);
        StringBuilder cwt = new StringBuilder("public void writeTo(Object obj, ").append(GenericObjectOutput.class.getName()).append(" out) throws java.io.IOException{");
        StringBuilder cpf = new StringBuilder("public Object parseFrom(").append(GenericObjectInput.class.getName()).append(" in) throws java.io.IOException{");
        cwt.append("if( $1 == null ){ $2.write0(OBJECT_NULL); return; }");
        cwt.append(cn).append(" v = (").append(cn).append(")$1; int len = v.length; $2.write0(OBJECT_VALUES); $2.writeUInt(len); for(int i=0;i<len;i++){ ");
        cpf.append("byte b = $1.read0(); if( b == OBJECT_NULL ) return null; if( b != OBJECT_VALUES ) throw new java.io.IOException(\"Input format error, expect OBJECT_NULL|OBJECT_VALUES, get \" + b + \".\");");
        cpf.append("int len = $1.readUInt(); if( len == 0 ) return new ").append(s1).append('0').append(s2).append("; ");
        cpf.append(cn).append(" ret = new ").append(s1).append("len").append(s2).append("; for(int i=0;i<len;i++){ ");
        Builder<?> builder = null;
        if (cc.isPrimitive()) {
            if (cc == Boolean.TYPE) {
                cwt.append("$2.writeBool(v[i]);");
                cpf.append("ret[i] = $1.readBool();");
            } else if (cc == Byte.TYPE) {
                cwt.append("$2.writeByte(v[i]);");
                cpf.append("ret[i] = $1.readByte();");
            } else if (cc == Character.TYPE) {
                cwt.append("$2.writeShort((short)v[i]);");
                cpf.append("ret[i] = (char)$1.readShort();");
            } else if (cc == Short.TYPE) {
                cwt.append("$2.writeShort(v[i]);");
                cpf.append("ret[i] = $1.readShort();");
            } else if (cc == Integer.TYPE) {
                cwt.append("$2.writeInt(v[i]);");
                cpf.append("ret[i] = $1.readInt();");
            } else if (cc == Long.TYPE) {
                cwt.append("$2.writeLong(v[i]);");
                cpf.append("ret[i] = $1.readLong();");
            } else if (cc == Float.TYPE) {
                cwt.append("$2.writeFloat(v[i]);");
                cpf.append("ret[i] = $1.readFloat();");
            } else if (cc == Double.TYPE) {
                cwt.append("$2.writeDouble(v[i]);");
                cpf.append("ret[i] = $1.readDouble();");
            }
        } else {
            builder = Builder.register(cc);
            cwt.append("builder.writeTo(v[i], $2);");
            cpf.append("ret[i] = (").append(ccn).append(")builder.parseFrom($1);");
        }
        cwt.append(" } }");
        cpf.append(" } return ret; }");
        ClassGenerator cg = ClassGenerator.newInstance(cl);
        cg.setClassName(bcn);
        cg.setSuperClass(Builder.class);
        cg.addDefaultConstructor();
        if (builder != null) {
            cg.addField("public static " + BUILDER_CLASS_NAME + " builder;");
        }
        cg.addMethod("public Class getType(){ return " + cn + ".class; }");
        cg.addMethod(cwt.toString());
        cg.addMethod(cpf.toString());
        try {
            Class<?> wc = cg.toClass();
            if (builder != null) {
                wc.getField("builder").set(null, builder);
            }
            Builder builder2 = (Builder)wc.newInstance();
            return builder2;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
        finally {
            cg.release();
        }
    }

    private static Builder<?> newObjectBuilder(Class<?> c) {
        String[] fns;
        Field f;
        boolean ism;
        boolean isc;
        String cn;
        boolean isp;
        Constructor<?>[] cs;
        String bcn;
        boolean dn;
        boolean iss;
        Field[] fs;
        ClassLoader cl;
        if (c.isEnum()) {
            return Builder.newEnumBuilder(c);
        }
        if (c.isAnonymousClass()) {
            throw new RuntimeException("Can not instantiation anonymous class: " + c);
        }
        if (c.getEnclosingClass() != null && !Modifier.isStatic(c.getModifiers())) {
            throw new RuntimeException("Can not instantiation inner and non-static class: " + c);
        }
        if (Throwable.class.isAssignableFrom(c)) {
            return SerializableBuilder;
        }
        cl = ClassHelper.getCallerClassLoader(Builder.class);
        cn = c.getName();
        if (c.getClassLoader() == null) {
            isp = false;
            bcn = BUILDER_CLASS_NAME + "$bc" + BUILDER_CLASS_COUNTER.getAndIncrement();
        } else {
            isp = true;
            bcn = cn + "$bc" + BUILDER_CLASS_COUNTER.getAndIncrement();
        }
        isc = Collection.class.isAssignableFrom(c);
        ism = !isc && Map.class.isAssignableFrom(c);
        iss = !isc && !ism && Serializable.class.isAssignableFrom(c);
        fns = null;
        InputStream is = c.getResourceAsStream(c.getSimpleName() + ".fc");
        if (is != null) {
            try {
                int len = is.available();
                if (len > 0) {
                    if (len > 16384) {
                        throw new RuntimeException("Load [" + c.getName() + "] field-config file error: File-size too larger");
                    }
                    String[] lines = IOUtils.readLines(is);
                    if (lines != null && lines.length > 0) {
                        ArrayList<String> list = new ArrayList<String>();
                        for (int i = 0; i < lines.length; ++i) {
                            fns = lines[i].split(",");
                            Arrays.sort(fns, FNC);
                            for (int j = 0; j < fns.length; ++j) {
                                list.add(fns[j]);
                            }
                        }
                        fns = list.toArray(new String[0]);
                    }
                }
            }
            catch (IOException e) {
                throw new RuntimeException("Load [" + c.getName() + "] field-config file error: " + e.getMessage());
            }
            finally {
                try {
                    is.close();
                }
                catch (IOException iOException) {}
            }
        }
        if (fns != null) {
            fs = new Field[fns.length];
            for (int i = 0; i < fns.length; ++i) {
                void fn = fns[i];
                try {
                    f = c.getDeclaredField((String)fn);
                    int mod = f.getModifiers();
                    if (Modifier.isStatic(mod) || Builder.serializeIgnoreFinalModifier(c) && Modifier.isFinal(mod)) {
                        throw new RuntimeException("Field [" + c.getName() + "." + (String)fn + "] is static/final field.");
                    }
                    if (Modifier.isTransient(mod)) {
                        if (iss) {
                            return SerializableBuilder;
                        }
                        throw new RuntimeException("Field [" + c.getName() + "." + (String)fn + "] is transient field.");
                    }
                    f.setAccessible(true);
                    fs[i] = f;
                    continue;
                }
                catch (SecurityException e) {
                    throw new RuntimeException(e.getMessage());
                }
                catch (NoSuchFieldException e) {
                    throw new RuntimeException("Field [" + c.getName() + "." + (String)fn + "] not found.");
                }
            }
        } else {
            Class<?> t = c;
            ArrayList<Field> fl = new ArrayList<Field>();
            do {
                for (Field tf : fs = t.getDeclaredFields()) {
                    int mod = tf.getModifiers();
                    if (Modifier.isStatic(mod) || Builder.serializeIgnoreFinalModifier(c) && Modifier.isFinal(mod) || tf.getName().equals("this$0") || !Modifier.isPublic(tf.getType().getModifiers())) continue;
                    if (Modifier.isTransient(mod)) {
                        if (!iss) continue;
                        return SerializableBuilder;
                    }
                    tf.setAccessible(true);
                    fl.add(tf);
                }
            } while ((t = t.getSuperclass()) != Object.class);
            fs = fl.toArray(new Field[0]);
            if (fs.length > 1) {
                Arrays.sort(fs, FC);
            }
        }
        if ((cs = c.getDeclaredConstructors()).length == 0) {
            Class<?> t = c;
            do {
                if ((t = t.getSuperclass()) != null) continue;
                throw new RuntimeException("Can not found Constructor?");
            } while ((cs = t.getDeclaredConstructors()).length == 0);
        }
        if (cs.length > 1) {
            Arrays.sort(cs, CC);
        }
        StringBuilder cwf = new StringBuilder("protected void writeObject(Object obj, ").append(GenericObjectOutput.class.getName()).append(" out) throws java.io.IOException{");
        cwf.append(cn).append(" v = (").append(cn).append(")$1; ");
        cwf.append("$2.writeInt(fields.length);");
        StringBuilder crf = new StringBuilder("protected void readObject(Object ret, ").append(GenericObjectInput.class.getName()).append(" in) throws java.io.IOException{");
        crf.append("int fc = $2.readInt();");
        crf.append("if( fc != ").append(fs.length).append(" ) throw new IllegalStateException(\"Deserialize Class [").append(cn).append("], field count not matched. Expect ").append(fs.length).append(" but get \" + fc +\".\");");
        crf.append(cn).append(" ret = (").append(cn).append(")$1;");
        StringBuilder cni = new StringBuilder("protected Object newInstance(").append(GenericObjectInput.class.getName()).append(" in){ return ");
        Constructor<?> con = cs[0];
        int mod = con.getModifiers();
        boolean bl = dn = Modifier.isPublic(mod) || isp && !Modifier.isPrivate(mod);
        if (dn) {
            cni.append("new ").append(cn).append("(");
        } else {
            con.setAccessible(true);
            cni.append("constructor.newInstance(new Object[]{");
        }
        Class<?>[] pts = con.getParameterTypes();
        for (int i = 0; i < pts.length; ++i) {
            if (i > 0) {
                cni.append(',');
            }
            cni.append(Builder.defaultArg(pts[i]));
        }
        if (!dn) {
            cni.append("}");
        }
        cni.append("); }");
        Map<String, PropertyMetadata> pms = Builder.propertyMetadatas(c);
        ArrayList<Builder<?>> builders = new ArrayList<Builder<?>>(fs.length);
        for (int i = 0; i < fs.length; ++i) {
            PropertyMetadata pm;
            boolean da;
            f = fs[i];
            String fn = f.getName();
            Class<?> ft = f.getType();
            String ftn = ReflectUtils.getName(ft);
            boolean bl2 = da = isp && f.getDeclaringClass() == c && !Modifier.isPrivate(f.getModifiers());
            if (da) {
                pm = null;
            } else {
                pm = pms.get(fn);
                if (pm != null && (pm.type != ft || pm.setter == null || pm.getter == null)) {
                    pm = null;
                }
            }
            crf.append("if( fc == ").append(i).append(" ) return;");
            if (ft.isPrimitive()) {
                if (ft == Boolean.TYPE) {
                    if (da) {
                        cwf.append("$2.writeBool(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readBool();");
                        continue;
                    }
                    if (pm != null) {
                        cwf.append("$2.writeBool(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readBool());");
                        continue;
                    }
                    cwf.append("$2.writeBool(((Boolean)fields[").append(i).append("].get($1)).booleanValue());");
                    crf.append("fields[").append(i).append("].set(ret, ($w)$2.readBool());");
                    continue;
                }
                if (ft == Byte.TYPE) {
                    if (da) {
                        cwf.append("$2.writeByte(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readByte();");
                        continue;
                    }
                    if (pm != null) {
                        cwf.append("$2.writeByte(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readByte());");
                        continue;
                    }
                    cwf.append("$2.writeByte(((Byte)fields[").append(i).append("].get($1)).byteValue());");
                    crf.append("fields[").append(i).append("].set(ret, ($w)$2.readByte());");
                    continue;
                }
                if (ft == Character.TYPE) {
                    if (da) {
                        cwf.append("$2.writeShort((short)v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = (char)$2.readShort();");
                        continue;
                    }
                    if (pm != null) {
                        cwf.append("$2.writeShort((short)v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("((char)$2.readShort());");
                        continue;
                    }
                    cwf.append("$2.writeShort((short)((Character)fields[").append(i).append("].get($1)).charValue());");
                    crf.append("fields[").append(i).append("].set(ret, ($w)((char)$2.readShort()));");
                    continue;
                }
                if (ft == Short.TYPE) {
                    if (da) {
                        cwf.append("$2.writeShort(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readShort();");
                        continue;
                    }
                    if (pm != null) {
                        cwf.append("$2.writeShort(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readShort());");
                        continue;
                    }
                    cwf.append("$2.writeShort(((Short)fields[").append(i).append("].get($1)).shortValue());");
                    crf.append("fields[").append(i).append("].set(ret, ($w)$2.readShort());");
                    continue;
                }
                if (ft == Integer.TYPE) {
                    if (da) {
                        cwf.append("$2.writeInt(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readInt();");
                        continue;
                    }
                    if (pm != null) {
                        cwf.append("$2.writeInt(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readInt());");
                        continue;
                    }
                    cwf.append("$2.writeInt(((Integer)fields[").append(i).append("].get($1)).intValue());");
                    crf.append("fields[").append(i).append("].set(ret, ($w)$2.readInt());");
                    continue;
                }
                if (ft == Long.TYPE) {
                    if (da) {
                        cwf.append("$2.writeLong(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readLong();");
                        continue;
                    }
                    if (pm != null) {
                        cwf.append("$2.writeLong(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readLong());");
                        continue;
                    }
                    cwf.append("$2.writeLong(((Long)fields[").append(i).append("].get($1)).longValue());");
                    crf.append("fields[").append(i).append("].set(ret, ($w)$2.readLong());");
                    continue;
                }
                if (ft == Float.TYPE) {
                    if (da) {
                        cwf.append("$2.writeFloat(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readFloat();");
                        continue;
                    }
                    if (pm != null) {
                        cwf.append("$2.writeFloat(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readFloat());");
                        continue;
                    }
                    cwf.append("$2.writeFloat(((Float)fields[").append(i).append("].get($1)).floatValue());");
                    crf.append("fields[").append(i).append("].set(ret, ($w)$2.readFloat());");
                    continue;
                }
                if (ft != Double.TYPE) continue;
                if (da) {
                    cwf.append("$2.writeDouble(v.").append(fn).append(");");
                    crf.append("ret.").append(fn).append(" = $2.readDouble();");
                    continue;
                }
                if (pm != null) {
                    cwf.append("$2.writeDouble(v.").append(pm.getter).append("());");
                    crf.append("ret.").append(pm.setter).append("($2.readDouble());");
                    continue;
                }
                cwf.append("$2.writeDouble(((Double)fields[").append(i).append("].get($1)).doubleValue());");
                crf.append("fields[").append(i).append("].set(ret, ($w)$2.readDouble());");
                continue;
            }
            if (ft == c) {
                if (da) {
                    cwf.append("this.writeTo(v.").append(fn).append(", $2);");
                    crf.append("ret.").append(fn).append(" = (").append(ftn).append(")this.parseFrom($2);");
                    continue;
                }
                if (pm != null) {
                    cwf.append("this.writeTo(v.").append(pm.getter).append("(), $2);");
                    crf.append("ret.").append(pm.setter).append("((").append(ftn).append(")this.parseFrom($2));");
                    continue;
                }
                cwf.append("this.writeTo((").append(ftn).append(")fields[").append(i).append("].get($1), $2);");
                crf.append("fields[").append(i).append("].set(ret, this.parseFrom($2));");
                continue;
            }
            int bc = builders.size();
            builders.add(Builder.register(ft));
            if (da) {
                cwf.append("builders[").append(bc).append("].writeTo(v.").append(fn).append(", $2);");
                crf.append("ret.").append(fn).append(" = (").append(ftn).append(")builders[").append(bc).append("].parseFrom($2);");
                continue;
            }
            if (pm != null) {
                cwf.append("builders[").append(bc).append("].writeTo(v.").append(pm.getter).append("(), $2);");
                crf.append("ret.").append(pm.setter).append("((").append(ftn).append(")builders[").append(bc).append("].parseFrom($2));");
                continue;
            }
            cwf.append("builders[").append(bc).append("].writeTo((").append(ftn).append(")fields[").append(i).append("].get($1), $2);");
            crf.append("fields[").append(i).append("].set(ret, builders[").append(bc).append("].parseFrom($2));");
        }
        crf.append("for(int i=").append(fs.length).append(";i<fc;i++) $2.skipAny();");
        if (isc) {
            cwf.append("$2.writeInt(v.size()); for(java.util.Iterator it=v.iterator();it.hasNext();){ $2.writeObject(it.next()); }");
            crf.append("int len = $2.readInt(); for(int i=0;i<len;i++) ret.add($2.readObject());");
        } else if (ism) {
            cwf.append("$2.writeInt(v.size()); for(java.util.Iterator it=v.entrySet().iterator();it.hasNext();){ java.util.Map.Entry entry = (java.util.Map.Entry)it.next(); $2.writeObject(entry.getKey()); $2.writeObject(entry.getValue()); }");
            crf.append("int len = $2.readInt(); for(int i=0;i<len;i++) ret.put($2.readObject(), $2.readObject());");
        }
        cwf.append(" }");
        crf.append(" }");
        ClassGenerator cg = ClassGenerator.newInstance(cl);
        cg.setClassName(bcn);
        cg.setSuperClass(AbstractObjectBuilder.class);
        cg.addDefaultConstructor();
        cg.addField("public static java.lang.reflect.Field[] fields;");
        cg.addField("public static " + BUILDER_CLASS_NAME + "[] builders;");
        if (!dn) {
            cg.addField("public static java.lang.reflect.Constructor constructor;");
        }
        cg.addMethod("public Class getType(){ return " + cn + ".class; }");
        cg.addMethod(cwf.toString());
        cg.addMethod(crf.toString());
        cg.addMethod(cni.toString());
        try {
            Class<?> wc = cg.toClass();
            wc.getField("fields").set(null, fs);
            wc.getField("builders").set(null, builders.toArray(new Builder[0]));
            if (!dn) {
                wc.getField("constructor").set(null, con);
            }
            Builder builder = (Builder)wc.newInstance();
            return builder;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        finally {
            cg.release();
        }
    }

    private static Builder<?> newEnumBuilder(Class<?> c) {
        ClassLoader cl = ClassHelper.getCallerClassLoader(Builder.class);
        String cn = c.getName();
        String bcn = BUILDER_CLASS_NAME + "$bc" + BUILDER_CLASS_COUNTER.getAndIncrement();
        StringBuilder cwt = new StringBuilder("public void writeTo(Object obj, ").append(GenericObjectOutput.class.getName()).append(" out) throws java.io.IOException{");
        cwt.append(cn).append(" v = (").append(cn).append(")$1;");
        cwt.append("if( $1 == null ){ $2.writeUTF(null); }else{ $2.writeUTF(v.name()); } }");
        StringBuilder cpf = new StringBuilder("public Object parseFrom(").append(GenericObjectInput.class.getName()).append(" in) throws java.io.IOException{");
        cpf.append("String name = $1.readUTF(); if( name == null ) return null; return (").append(cn).append(")Enum.valueOf(").append(cn).append(".class, name); }");
        ClassGenerator cg = ClassGenerator.newInstance(cl);
        cg.setClassName(bcn);
        cg.setSuperClass(Builder.class);
        cg.addDefaultConstructor();
        cg.addMethod("public Class getType(){ return " + cn + ".class; }");
        cg.addMethod(cwt.toString());
        cg.addMethod(cpf.toString());
        try {
            Class<?> wc = cg.toClass();
            Builder builder = (Builder)wc.newInstance();
            return builder;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        finally {
            cg.release();
        }
    }

    private static Map<String, PropertyMetadata> propertyMetadatas(Class<?> c) {
        HashMap<String, Method> mm = new HashMap<String, Method>();
        HashMap<String, PropertyMetadata> ret = new HashMap<String, PropertyMetadata>();
        for (Method m : c.getMethods()) {
            if (m.getDeclaringClass() == Object.class) continue;
            mm.put(ReflectUtils.getDesc(m), m);
        }
        for (Map.Entry<K, V> entry : mm.entrySet()) {
            PropertyMetadata pm;
            String pn;
            Class<?> pt;
            String desc = (String)entry.getKey();
            Method method = (Method)entry.getValue();
            Matcher matcher = ReflectUtils.GETTER_METHOD_DESC_PATTERN.matcher(desc);
            if (matcher.matches() || (matcher = ReflectUtils.IS_HAS_CAN_METHOD_DESC_PATTERN.matcher(desc)).matches()) {
                pn = Builder.propertyName(matcher.group(1));
                pt = method.getReturnType();
                pm = (PropertyMetadata)ret.get(pn);
                if (pm == null) {
                    pm = new PropertyMetadata();
                    pm.type = pt;
                    ret.put(pn, pm);
                } else if (pm.type != pt) continue;
                pm.getter = method.getName();
                continue;
            }
            matcher = ReflectUtils.SETTER_METHOD_DESC_PATTERN.matcher(desc);
            if (!matcher.matches()) continue;
            pn = Builder.propertyName(matcher.group(1));
            pt = method.getParameterTypes()[0];
            pm = (PropertyMetadata)ret.get(pn);
            if (pm == null) {
                pm = new PropertyMetadata();
                pm.type = pt;
                ret.put(pn, pm);
            } else if (pm.type != pt) continue;
            pm.setter = method.getName();
        }
        return ret;
    }

    private static String propertyName(String s) {
        return s.length() == 1 || Character.isLowerCase(s.charAt(1)) ? Character.toLowerCase(s.charAt(0)) + s.substring(1) : s;
    }

    private static boolean serializeIgnoreFinalModifier(Class cl) {
        return false;
    }

    private static boolean isPrimitiveOrPrimitiveArray1(Class<?> cl) {
        if (cl.isPrimitive()) {
            return true;
        }
        Class<?> clazz = cl.getClass().getComponentType();
        return clazz != null && clazz.isPrimitive();
    }

    private static String defaultArg(Class<?> cl) {
        if (Boolean.TYPE == cl) {
            return "false";
        }
        if (Integer.TYPE == cl) {
            return "0";
        }
        if (Long.TYPE == cl) {
            return "0l";
        }
        if (Double.TYPE == cl) {
            return "(double)0";
        }
        if (Float.TYPE == cl) {
            return "(float)0";
        }
        if (Short.TYPE == cl) {
            return "(short)0";
        }
        if (Character.TYPE == cl) {
            return "(char)0";
        }
        if (Byte.TYPE == cl) {
            return "(byte)0";
        }
        if (byte[].class == cl) {
            return "new byte[]{0}";
        }
        if (!cl.isPrimitive()) {
            return "null";
        }
        throw new UnsupportedOperationException();
    }

    private static int compareFieldName(String n1, String n2) {
        int l = Math.min(n1.length(), n2.length());
        for (int i = 0; i < l; ++i) {
            int t = n1.charAt(i) - n2.charAt(i);
            if (t == 0) continue;
            return t;
        }
        return n1.length() - n2.length();
    }

    private static void addDesc(Class<?> c) {
        String desc = ReflectUtils.getDesc(c);
        int index = mDescList.size();
        mDescList.add(desc);
        mDescMap.put(desc, index);
    }

    static {
        Builder.addDesc(boolean[].class);
        Builder.addDesc(byte[].class);
        Builder.addDesc(char[].class);
        Builder.addDesc(short[].class);
        Builder.addDesc(int[].class);
        Builder.addDesc(long[].class);
        Builder.addDesc(float[].class);
        Builder.addDesc(double[].class);
        Builder.addDesc(Boolean.class);
        Builder.addDesc(Byte.class);
        Builder.addDesc(Character.class);
        Builder.addDesc(Short.class);
        Builder.addDesc(Integer.class);
        Builder.addDesc(Long.class);
        Builder.addDesc(Float.class);
        Builder.addDesc(Double.class);
        Builder.addDesc(String.class);
        Builder.addDesc(String[].class);
        Builder.addDesc(ArrayList.class);
        Builder.addDesc(HashMap.class);
        Builder.addDesc(HashSet.class);
        Builder.addDesc(java.util.Date.class);
        Builder.addDesc(Date.class);
        Builder.addDesc(Time.class);
        Builder.addDesc(Timestamp.class);
        Builder.addDesc(LinkedList.class);
        Builder.addDesc(LinkedHashMap.class);
        Builder.addDesc(LinkedHashSet.class);
        Builder.register(byte[].class, new Builder<byte[]>(){

            @Override
            public Class<byte[]> getType() {
                return byte[].class;
            }

            @Override
            public void writeTo(byte[] obj, GenericObjectOutput out) throws IOException {
                out.writeBytes(obj);
            }

            @Override
            public byte[] parseFrom(GenericObjectInput in) throws IOException {
                return in.readBytes();
            }
        });
        Builder.register(Boolean.class, new Builder<Boolean>(){

            @Override
            public Class<Boolean> getType() {
                return Boolean.class;
            }

            @Override
            public void writeTo(Boolean obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)24);
                } else if (obj.booleanValue()) {
                    out.write0((byte)26);
                } else {
                    out.write0((byte)25);
                }
            }

            @Override
            public Boolean parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                switch (b) {
                    case 24: {
                        return null;
                    }
                    case 25: {
                        return Boolean.FALSE;
                    }
                    case 26: {
                        return Boolean.TRUE;
                    }
                }
                throw new IOException("Input format error, expect VARINT_N1|VARINT_0|VARINT_1, get " + b + ".");
            }
        });
        Builder.register(Byte.class, new Builder<Byte>(){

            @Override
            public Class<Byte> getType() {
                return Byte.class;
            }

            @Override
            public void writeTo(Byte obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-124);
                    out.writeByte(obj);
                }
            }

            @Override
            public Byte parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -124) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUE, get " + b + ".");
                }
                return in.readByte();
            }
        });
        Builder.register(Character.class, new Builder<Character>(){

            @Override
            public Class<Character> getType() {
                return Character.class;
            }

            @Override
            public void writeTo(Character obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-124);
                    out.writeShort((short)obj.charValue());
                }
            }

            @Override
            public Character parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -124) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUE, get " + b + ".");
                }
                return Character.valueOf((char)in.readShort());
            }
        });
        Builder.register(Short.class, new Builder<Short>(){

            @Override
            public Class<Short> getType() {
                return Short.class;
            }

            @Override
            public void writeTo(Short obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-124);
                    out.writeShort(obj);
                }
            }

            @Override
            public Short parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -124) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUE, get " + b + ".");
                }
                return in.readShort();
            }
        });
        Builder.register(Integer.class, new Builder<Integer>(){

            @Override
            public Class<Integer> getType() {
                return Integer.class;
            }

            @Override
            public void writeTo(Integer obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-124);
                    out.writeInt(obj);
                }
            }

            @Override
            public Integer parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -124) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUE, get " + b + ".");
                }
                return in.readInt();
            }
        });
        Builder.register(Long.class, new Builder<Long>(){

            @Override
            public Class<Long> getType() {
                return Long.class;
            }

            @Override
            public void writeTo(Long obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-124);
                    out.writeLong(obj);
                }
            }

            @Override
            public Long parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -124) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUE, get " + b + ".");
                }
                return in.readLong();
            }
        });
        Builder.register(Float.class, new Builder<Float>(){

            @Override
            public Class<Float> getType() {
                return Float.class;
            }

            @Override
            public void writeTo(Float obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-124);
                    out.writeFloat(obj.floatValue());
                }
            }

            @Override
            public Float parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -124) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUE, get " + b + ".");
                }
                return new Float(in.readFloat());
            }
        });
        Builder.register(Double.class, new Builder<Double>(){

            @Override
            public Class<Double> getType() {
                return Double.class;
            }

            @Override
            public void writeTo(Double obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-124);
                    out.writeDouble(obj);
                }
            }

            @Override
            public Double parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -124) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUE, get " + b + ".");
                }
                return new Double(in.readDouble());
            }
        });
        Builder.register(String.class, new Builder<String>(){

            @Override
            public Class<String> getType() {
                return String.class;
            }

            @Override
            public String parseFrom(GenericObjectInput in) throws IOException {
                return in.readUTF();
            }

            @Override
            public void writeTo(String obj, GenericObjectOutput out) throws IOException {
                out.writeUTF(obj);
            }
        });
        Builder.register(StringBuilder.class, new Builder<StringBuilder>(){

            @Override
            public Class<StringBuilder> getType() {
                return StringBuilder.class;
            }

            @Override
            public StringBuilder parseFrom(GenericObjectInput in) throws IOException {
                return new StringBuilder(in.readUTF());
            }

            @Override
            public void writeTo(StringBuilder obj, GenericObjectOutput out) throws IOException {
                out.writeUTF(obj.toString());
            }
        });
        Builder.register(StringBuffer.class, new Builder<StringBuffer>(){

            @Override
            public Class<StringBuffer> getType() {
                return StringBuffer.class;
            }

            @Override
            public StringBuffer parseFrom(GenericObjectInput in) throws IOException {
                return new StringBuffer(in.readUTF());
            }

            @Override
            public void writeTo(StringBuffer obj, GenericObjectOutput out) throws IOException {
                out.writeUTF(obj.toString());
            }
        });
        Builder.register(ArrayList.class, new Builder<ArrayList>(){

            @Override
            public Class<ArrayList> getType() {
                return ArrayList.class;
            }

            @Override
            public void writeTo(ArrayList obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-123);
                    out.writeUInt(obj.size());
                    for (Object item : obj) {
                        out.writeObject(item);
                    }
                }
            }

            @Override
            public ArrayList parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -123) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUES, get " + b + ".");
                }
                int len = in.readUInt();
                ArrayList<Object> ret = new ArrayList<Object>(len);
                for (int i = 0; i < len; ++i) {
                    ret.add(in.readObject());
                }
                return ret;
            }
        });
        Builder.register(HashMap.class, new Builder<HashMap>(){

            @Override
            public Class<HashMap> getType() {
                return HashMap.class;
            }

            @Override
            public void writeTo(HashMap obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-122);
                    out.writeUInt(obj.size());
                    for (Map.Entry entry : obj.entrySet()) {
                        out.writeObject(entry.getKey());
                        out.writeObject(entry.getValue());
                    }
                }
            }

            @Override
            public HashMap parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -122) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_MAP, get " + b + ".");
                }
                int len = in.readUInt();
                HashMap<Object, Object> ret = new HashMap<Object, Object>(len);
                for (int i = 0; i < len; ++i) {
                    ret.put(in.readObject(), in.readObject());
                }
                return ret;
            }
        });
        Builder.register(HashSet.class, new Builder<HashSet>(){

            @Override
            public Class<HashSet> getType() {
                return HashSet.class;
            }

            @Override
            public void writeTo(HashSet obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-123);
                    out.writeUInt(obj.size());
                    for (Object item : obj) {
                        out.writeObject(item);
                    }
                }
            }

            @Override
            public HashSet parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -123) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUES, get " + b + ".");
                }
                int len = in.readUInt();
                HashSet<Object> ret = new HashSet<Object>(len);
                for (int i = 0; i < len; ++i) {
                    ret.add(in.readObject());
                }
                return ret;
            }
        });
        Builder.register(java.util.Date.class, new Builder<java.util.Date>(){

            @Override
            public Class<java.util.Date> getType() {
                return java.util.Date.class;
            }

            @Override
            public void writeTo(java.util.Date obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-124);
                    out.writeLong(obj.getTime());
                }
            }

            @Override
            public java.util.Date parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -124) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUE, get " + b + ".");
                }
                return new java.util.Date(in.readLong());
            }
        });
        Builder.register(Date.class, new Builder<Date>(){

            @Override
            public Class<Date> getType() {
                return Date.class;
            }

            @Override
            public void writeTo(Date obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-124);
                    out.writeLong(obj.getTime());
                }
            }

            @Override
            public Date parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -124) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUE, get " + b + ".");
                }
                return new Date(in.readLong());
            }
        });
        Builder.register(Timestamp.class, new Builder<Timestamp>(){

            @Override
            public Class<Timestamp> getType() {
                return Timestamp.class;
            }

            @Override
            public void writeTo(Timestamp obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-124);
                    out.writeLong(obj.getTime());
                }
            }

            @Override
            public Timestamp parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -124) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUE, get " + b + ".");
                }
                return new Timestamp(in.readLong());
            }
        });
        Builder.register(Time.class, new Builder<Time>(){

            @Override
            public Class<Time> getType() {
                return Time.class;
            }

            @Override
            public void writeTo(Time obj, GenericObjectOutput out) throws IOException {
                if (obj == null) {
                    out.write0((byte)-108);
                } else {
                    out.write0((byte)-124);
                    out.writeLong(obj.getTime());
                }
            }

            @Override
            public Time parseFrom(GenericObjectInput in) throws IOException {
                byte b = in.read0();
                if (b == -108) {
                    return null;
                }
                if (b != -124) {
                    throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_VALUE, get " + b + ".");
                }
                return new Time(in.readLong());
            }
        });
    }

    public static abstract class AbstractObjectBuilder<T>
    extends Builder<T> {
        @Override
        public abstract Class<T> getType();

        @Override
        public void writeTo(T obj, GenericObjectOutput out) throws IOException {
            if (obj == null) {
                out.write0((byte)-108);
            } else {
                int ref = out.getRef(obj);
                if (ref < 0) {
                    out.addRef(obj);
                    out.write0((byte)-128);
                    this.writeObject(obj, out);
                } else {
                    out.write0((byte)-127);
                    out.writeUInt(ref);
                }
            }
        }

        @Override
        public T parseFrom(GenericObjectInput in) throws IOException {
            byte b = in.read0();
            switch (b) {
                case -128: {
                    T ret = this.newInstance(in);
                    in.addRef(ret);
                    this.readObject(ret, in);
                    return ret;
                }
                case -127: {
                    return (T)in.getRef(in.readUInt());
                }
                case -108: {
                    return null;
                }
            }
            throw new IOException("Input format error, expect OBJECT|OBJECT_REF|OBJECT_NULL, get " + b);
        }

        protected abstract void writeObject(T var1, GenericObjectOutput var2) throws IOException;

        protected abstract T newInstance(GenericObjectInput var1) throws IOException;

        protected abstract void readObject(T var1, GenericObjectInput var2) throws IOException;
    }

    static class PropertyMetadata {
        Class<?> type;
        String setter;
        String getter;

        PropertyMetadata() {
        }
    }

}

