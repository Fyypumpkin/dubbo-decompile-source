/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.thrift.TApplicationException
 *  org.apache.thrift.TBase
 *  org.apache.thrift.TException
 *  org.apache.thrift.TFieldIdEnum
 *  org.apache.thrift.protocol.TBinaryProtocol
 *  org.apache.thrift.protocol.TMessage
 *  org.apache.thrift.protocol.TProtocol
 *  org.apache.thrift.transport.TFramedTransport
 *  org.apache.thrift.transport.TIOStreamTransport
 *  org.apache.thrift.transport.TTransport
 */
package com.alibaba.dubbo.rpc.protocol.thrift;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferInputStream;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.thrift.ClassNameGenerator;
import com.alibaba.dubbo.rpc.protocol.thrift.ThriftUtils;
import com.alibaba.dubbo.rpc.protocol.thrift.io.RandomAccessByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;

public class ThriftCodec
implements Codec2 {
    private static final AtomicInteger THRIFT_SEQ_ID = new AtomicInteger(0);
    private static final ConcurrentMap<String, Class<?>> cachedClass = new ConcurrentHashMap();
    static final ConcurrentMap<Long, RequestData> cachedRequest = new ConcurrentHashMap<Long, RequestData>();
    public static final int MESSAGE_LENGTH_INDEX = 2;
    public static final int MESSAGE_HEADER_LENGTH_INDEX = 6;
    public static final int MESSAGE_SHORTEST_LENGTH = 10;
    public static final String NAME = "thrift";
    public static final String PARAMETER_CLASS_NAME_GENERATOR = "class.name.generator";
    public static final byte VERSION = 1;
    public static final short MAGIC = -9540;

    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException {
        if (message instanceof Request) {
            this.encodeRequest(channel, buffer, (Request)message);
        } else if (message instanceof Response) {
            this.encodeResponse(channel, buffer, (Response)message);
        } else {
            throw new UnsupportedOperationException(new StringBuilder(32).append("Thrift codec only support encode ").append(Request.class.getName()).append(" and ").append(Response.class.getName()).toString());
        }
    }

    @Override
    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {
        short magic;
        int messageLength;
        int available = buffer.readableBytes();
        if (available < 10) {
            return Codec2.DecodeResult.NEED_MORE_INPUT;
        }
        TIOStreamTransport transport = new TIOStreamTransport((InputStream)new ChannelBufferInputStream(buffer));
        TBinaryProtocol protocol = new TBinaryProtocol((TTransport)transport);
        try {
            byte[] bytes = new byte[4];
            transport.read(bytes, 0, 4);
            magic = protocol.readI16();
            messageLength = protocol.readI32();
        }
        catch (TException e) {
            throw new IOException(e.getMessage(), (Throwable)e);
        }
        if (-9540 != magic) {
            throw new IOException(new StringBuilder(32).append("Unknown magic code ").append(magic).toString());
        }
        if (available < messageLength) {
            return Codec2.DecodeResult.NEED_MORE_INPUT;
        }
        return this.decode((TProtocol)protocol);
    }

    private Object decode(TProtocol protocol) throws IOException {
        TMessage message;
        long id;
        String serviceName;
        try {
            protocol.readI16();
            protocol.readByte();
            serviceName = protocol.readString();
            id = protocol.readI64();
            message = protocol.readMessageBegin();
        }
        catch (TException e) {
            throw new IOException(e.getMessage(), (Throwable)e);
        }
        if (message.type == 1) {
            TFieldIdEnum fieldIdEnum;
            TBase args;
            RpcInvocation result = new RpcInvocation();
            result.setAttachment("interface", serviceName);
            result.setMethodName(message.name);
            String argsClassName = ExtensionLoader.getExtensionLoader(ClassNameGenerator.class).getExtension(NAME).generateArgsClassName(serviceName, message.name);
            if (StringUtils.isEmpty((String)argsClassName)) {
                throw new RpcException(5, "The specified interface name incorrect.");
            }
            Class<?> clazz = (Class<?>)cachedClass.get(argsClassName);
            if (clazz == null) {
                try {
                    clazz = ClassHelper.forNameWithThreadContextClassLoader(argsClassName);
                    cachedClass.putIfAbsent(argsClassName, clazz);
                }
                catch (ClassNotFoundException e) {
                    throw new RpcException(5, e.getMessage(), e);
                }
            }
            try {
                args = (TBase)clazz.newInstance();
            }
            catch (InstantiationException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
            catch (IllegalAccessException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
            try {
                args.read(protocol);
                protocol.readMessageEnd();
            }
            catch (TException e) {
                throw new RpcException(5, e.getMessage(), (Throwable)e);
            }
            ArrayList<Object> parameters = new ArrayList<Object>();
            ArrayList parameterTypes = new ArrayList();
            int index = 1;
            while ((fieldIdEnum = args.fieldForId(index++)) != null) {
                Method getMethod;
                String fieldName = fieldIdEnum.getFieldName();
                String getMethodName = ThriftUtils.generateGetMethodName(fieldName);
                try {
                    getMethod = clazz.getMethod(getMethodName, new Class[0]);
                }
                catch (NoSuchMethodException e) {
                    throw new RpcException(5, e.getMessage(), e);
                }
                parameterTypes.add(getMethod.getReturnType());
                try {
                    parameters.add(getMethod.invoke((Object)args, new Object[0]));
                }
                catch (IllegalAccessException e) {
                    throw new RpcException(5, e.getMessage(), e);
                }
                catch (InvocationTargetException e) {
                    throw new RpcException(5, e.getMessage(), e);
                }
            }
            result.setArguments(parameters.toArray());
            result.setParameterTypes(parameterTypes.toArray(new Class[parameterTypes.size()]));
            Request request = new Request(id);
            request.setData(result);
            cachedRequest.putIfAbsent(id, RequestData.create(message.seqid, serviceName, message.name));
            return request;
        }
        if (message.type == 3) {
            TApplicationException exception;
            try {
                exception = TApplicationException.read((TProtocol)protocol);
                protocol.readMessageEnd();
            }
            catch (TException e) {
                throw new IOException(e.getMessage(), (Throwable)e);
            }
            RpcResult result = new RpcResult();
            result.setException(new RpcException(exception.getMessage()));
            Response response = new Response();
            response.setResult(result);
            response.setId(id);
            return response;
        }
        if (message.type == 2) {
            TBase result;
            TFieldIdEnum fieldIdEnum;
            String resultClassName = ExtensionLoader.getExtensionLoader(ClassNameGenerator.class).getExtension(NAME).generateResultClassName(serviceName, message.name);
            if (StringUtils.isEmpty((String)resultClassName)) {
                throw new IllegalArgumentException(new StringBuilder(32).append("Could not infer service result class name from service name ").append(serviceName).append(", the service name you specified may not generated by thrift idl compiler").toString());
            }
            Class<?> clazz = (Class<?>)cachedClass.get(resultClassName);
            if (clazz == null) {
                try {
                    clazz = ClassHelper.forNameWithThreadContextClassLoader(resultClassName);
                    cachedClass.putIfAbsent(resultClassName, clazz);
                }
                catch (ClassNotFoundException e) {
                    throw new RpcException(5, e.getMessage(), e);
                }
            }
            try {
                result = (TBase)clazz.newInstance();
            }
            catch (InstantiationException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
            catch (IllegalAccessException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
            try {
                result.read(protocol);
                protocol.readMessageEnd();
            }
            catch (TException e) {
                throw new RpcException(5, e.getMessage(), (Throwable)e);
            }
            Object realResult = null;
            int index = 0;
            while ((fieldIdEnum = result.fieldForId(index++)) != null) {
                Field field;
                try {
                    field = clazz.getDeclaredField(fieldIdEnum.getFieldName());
                    field.setAccessible(true);
                }
                catch (NoSuchFieldException e) {
                    throw new RpcException(5, e.getMessage(), e);
                }
                try {
                    realResult = field.get((Object)result);
                }
                catch (IllegalAccessException e) {
                    throw new RpcException(5, e.getMessage(), e);
                }
                if (realResult == null) continue;
                break;
            }
            Response response = new Response();
            response.setId(id);
            RpcResult rpcResult = new RpcResult();
            if (realResult instanceof Throwable) {
                rpcResult.setException((Throwable)realResult);
            } else {
                rpcResult.setValue(realResult);
            }
            response.setResult(rpcResult);
            return response;
        }
        throw new IOException();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void encodeRequest(Channel channel, ChannelBuffer buffer, Request request) throws IOException {
        byte[] bytes;
        RandomAccessByteArrayOutputStream bos;
        TBase args;
        RpcInvocation inv = (RpcInvocation)request.getData();
        int seqId = ThriftCodec.nextSeqId();
        String serviceName = inv.getAttachment("interface");
        if (StringUtils.isEmpty((String)serviceName)) {
            throw new IllegalArgumentException(new StringBuilder(32).append("Could not find service name in attachment with key ").append("interface").toString());
        }
        TMessage message = new TMessage(inv.getMethodName(), 1, seqId);
        String methodArgs = ExtensionLoader.getExtensionLoader(ClassNameGenerator.class).getExtension(channel.getUrl().getParameter(PARAMETER_CLASS_NAME_GENERATOR, NAME)).generateArgsClassName(serviceName, inv.getMethodName());
        if (StringUtils.isEmpty((String)methodArgs)) {
            throw new RpcException(5, new StringBuilder(32).append("Could not encode request, the specified interface may be incorrect.").toString());
        }
        Class<?> clazz = (Class<?>)cachedClass.get(methodArgs);
        if (clazz == null) {
            try {
                clazz = ClassHelper.forNameWithThreadContextClassLoader(methodArgs);
                cachedClass.putIfAbsent(methodArgs, clazz);
            }
            catch (ClassNotFoundException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
        }
        try {
            args = (TBase)clazz.newInstance();
        }
        catch (InstantiationException e) {
            throw new RpcException(5, e.getMessage(), e);
        }
        catch (IllegalAccessException e) {
            throw new RpcException(5, e.getMessage(), e);
        }
        for (int i = 0; i < inv.getArguments().length; ++i) {
            Method method;
            Object obj = inv.getArguments()[i];
            if (obj == null) continue;
            TFieldIdEnum field = args.fieldForId(i + 1);
            String setMethodName = ThriftUtils.generateSetMethodName(field.getFieldName());
            try {
                method = clazz.getMethod(setMethodName, inv.getParameterTypes()[i]);
            }
            catch (NoSuchMethodException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
            try {
                method.invoke((Object)args, obj);
                continue;
            }
            catch (IllegalAccessException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
            catch (InvocationTargetException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
        }
        bos = new RandomAccessByteArrayOutputStream(1024);
        TIOStreamTransport transport = new TIOStreamTransport((OutputStream)bos);
        TBinaryProtocol protocol = new TBinaryProtocol((TTransport)transport);
        bytes = new byte[4];
        try {
            int messageLength;
            protocol.writeI16((short)-9540);
            protocol.writeI32(Integer.MAX_VALUE);
            protocol.writeI16((short)32767);
            protocol.writeByte((byte)1);
            protocol.writeString(serviceName);
            protocol.writeI64(request.getId());
            protocol.getTransport().flush();
            int headerLength = bos.size();
            protocol.writeMessageBegin(message);
            args.write((TProtocol)protocol);
            protocol.writeMessageEnd();
            protocol.getTransport().flush();
            int oldIndex = messageLength = bos.size();
            try {
                TFramedTransport.encodeFrameSize((int)messageLength, (byte[])bytes);
                bos.setWriteIndex(2);
                protocol.writeI32(messageLength);
                bos.setWriteIndex(6);
                protocol.writeI16((short)(65535 & headerLength));
            }
            finally {
                bos.setWriteIndex(oldIndex);
            }
        }
        catch (TException e) {
            throw new RpcException(5, e.getMessage(), (Throwable)e);
        }
        buffer.writeBytes(bytes);
        buffer.writeBytes(bos.toByteArray());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void encodeResponse(Channel channel, ChannelBuffer buffer, Response response) throws IOException {
        byte[] bytes;
        RandomAccessByteArrayOutputStream bos;
        TBase resultObj;
        RpcResult result = (RpcResult)response.getResult();
        RequestData rd = (RequestData)cachedRequest.get(response.getId());
        String resultClassName = ExtensionLoader.getExtensionLoader(ClassNameGenerator.class).getExtension(channel.getUrl().getParameter(PARAMETER_CLASS_NAME_GENERATOR, NAME)).generateResultClassName(rd.serviceName, rd.methodName);
        if (StringUtils.isEmpty((String)resultClassName)) {
            throw new RpcException(5, new StringBuilder(32).append("Could not encode response, the specified interface may be incorrect.").toString());
        }
        Class<?> clazz = (Class<?>)cachedClass.get(resultClassName);
        if (clazz == null) {
            try {
                clazz = ClassHelper.forNameWithThreadContextClassLoader(resultClassName);
                cachedClass.putIfAbsent(resultClassName, clazz);
            }
            catch (ClassNotFoundException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
        }
        try {
            resultObj = (TBase)clazz.newInstance();
        }
        catch (InstantiationException e) {
            throw new RpcException(5, e.getMessage(), e);
        }
        catch (IllegalAccessException e) {
            throw new RpcException(5, e.getMessage(), e);
        }
        TApplicationException applicationException = null;
        if (result.hasException()) {
            TFieldIdEnum fieldIdEnum;
            Throwable throwable = result.getException();
            int index = 1;
            boolean found = false;
            while ((fieldIdEnum = resultObj.fieldForId(index++)) != null) {
                String fieldName = fieldIdEnum.getFieldName();
                String getMethodName = ThriftUtils.generateGetMethodName(fieldName);
                String setMethodName = ThriftUtils.generateSetMethodName(fieldName);
                try {
                    Method getMethod = clazz.getMethod(getMethodName, new Class[0]);
                    if (!getMethod.getReturnType().equals(throwable.getClass())) continue;
                    found = true;
                    Method setMethod = clazz.getMethod(setMethodName, throwable.getClass());
                    setMethod.invoke((Object)resultObj, throwable);
                }
                catch (NoSuchMethodException e) {
                    throw new RpcException(5, e.getMessage(), e);
                }
                catch (InvocationTargetException e) {
                    throw new RpcException(5, e.getMessage(), e);
                }
                catch (IllegalAccessException e) {
                    throw new RpcException(5, e.getMessage(), e);
                }
            }
            if (!found) {
                applicationException = new TApplicationException(throwable.getMessage());
            }
        } else {
            Object realResult = result.getResult();
            String fieldName = resultObj.fieldForId(0).getFieldName();
            String setMethodName = ThriftUtils.generateSetMethodName(fieldName);
            String getMethodName = ThriftUtils.generateGetMethodName(fieldName);
            try {
                Method getMethod = clazz.getMethod(getMethodName, new Class[0]);
                Method setMethod = clazz.getMethod(setMethodName, getMethod.getReturnType());
                setMethod.invoke((Object)resultObj, realResult);
            }
            catch (NoSuchMethodException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
            catch (InvocationTargetException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
            catch (IllegalAccessException e) {
                throw new RpcException(5, e.getMessage(), e);
            }
        }
        TMessage message = applicationException != null ? new TMessage(rd.methodName, 3, rd.id) : new TMessage(rd.methodName, 2, rd.id);
        bos = new RandomAccessByteArrayOutputStream(1024);
        TIOStreamTransport transport = new TIOStreamTransport((OutputStream)bos);
        TBinaryProtocol protocol = new TBinaryProtocol((TTransport)transport);
        bytes = new byte[4];
        try {
            int messageLength;
            protocol.writeI16((short)-9540);
            protocol.writeI32(Integer.MAX_VALUE);
            protocol.writeI16((short)32767);
            protocol.writeByte((byte)1);
            protocol.writeString(rd.serviceName);
            protocol.writeI64(response.getId());
            protocol.getTransport().flush();
            int headerLength = bos.size();
            protocol.writeMessageBegin(message);
            switch (message.type) {
                case 3: {
                    applicationException.write((TProtocol)protocol);
                    break;
                }
                case 2: {
                    resultObj.write((TProtocol)protocol);
                }
            }
            protocol.writeMessageEnd();
            protocol.getTransport().flush();
            int oldIndex = messageLength = bos.size();
            try {
                TFramedTransport.encodeFrameSize((int)messageLength, (byte[])bytes);
                bos.setWriteIndex(2);
                protocol.writeI32(messageLength);
                bos.setWriteIndex(6);
                protocol.writeI16((short)(65535 & headerLength));
            }
            finally {
                bos.setWriteIndex(oldIndex);
            }
        }
        catch (TException e) {
            throw new RpcException(5, e.getMessage(), (Throwable)e);
        }
        buffer.writeBytes(bytes);
        buffer.writeBytes(bos.toByteArray());
    }

    private static int nextSeqId() {
        return THRIFT_SEQ_ID.incrementAndGet();
    }

    static int getSeqId() {
        return THRIFT_SEQ_ID.get();
    }

    static class RequestData {
        int id;
        String serviceName;
        String methodName;

        RequestData() {
        }

        static RequestData create(int id, String sn, String mn) {
            RequestData result = new RequestData();
            result.id = id;
            result.serviceName = sn;
            result.methodName = mn;
            return result;
        }
    }

}

