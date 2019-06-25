/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson.JSON
 *  com.alibaba.fastjson.serializer.SerializerFeature
 */
package com.alibaba.dubbo.rpc.filter;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.beanutil.JavaBeanAccessor;
import com.alibaba.dubbo.common.beanutil.JavaBeanDescriptor;
import com.alibaba.dubbo.common.beanutil.JavaBeanSerializeUtil;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.io.UnsafeByteArrayInputStream;
import com.alibaba.dubbo.common.io.UnsafeByteArrayOutputStream;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.utils.PojoUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

@Activate(group={"provider"}, order=-20000)
public class GenericFilter
implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation inv) throws RpcException {
        if (inv.getMethodName().equals("$invoke") && inv.getArguments() != null && inv.getArguments().length == 3 && !ProtocolUtils.isGeneric(invoker.getUrl().getParameter("generic"))) {
            String name = ((String)inv.getArguments()[0]).trim();
            String[] types = (String[])inv.getArguments()[1];
            Object[] args = (Object[])inv.getArguments()[2];
            try {
                String generic;
                int i;
                Method method = ReflectUtils.findMethodByMethodSignature(invoker.getInterface(), name, types);
                Class<?>[] params = method.getParameterTypes();
                if (args == null) {
                    args = new Object[params.length];
                }
                if (StringUtils.isEmpty(generic = inv.getAttachment("generic")) || ProtocolUtils.isDefaultGenericSerialization(generic)) {
                    args = PojoUtils.realize(args, params, method.getGenericParameterTypes());
                } else if (ProtocolUtils.isResultNoChangeGenericSerialization(generic)) {
                    args = PojoUtils.realize(args, params, method.getGenericParameterTypes());
                } else if (ProtocolUtils.isJavaGenericSerialization(generic)) {
                    for (i = 0; i < args.length; ++i) {
                        if (byte[].class == args[i].getClass()) {
                            try {
                                UnsafeByteArrayInputStream is = new UnsafeByteArrayInputStream((byte[])args[i]);
                                args[i] = ExtensionLoader.getExtensionLoader(Serialization.class).getExtension("nativejava").deserialize(null, is).readObject();
                                continue;
                            }
                            catch (Exception e) {
                                throw new RpcException("Deserialize argument [" + (i + 1) + "] failed.", (Throwable)e);
                            }
                        }
                        throw new RpcException(new StringBuilder(32).append("Generic serialization [").append("nativejava").append("] only support message type ").append(byte[].class).append(" and your message type is ").append(args[i].getClass()).toString());
                    }
                } else if (ProtocolUtils.isBeanGenericSerialization(generic)) {
                    for (i = 0; i < args.length; ++i) {
                        if (!(args[i] instanceof JavaBeanDescriptor)) {
                            throw new RpcException(new StringBuilder(32).append("Generic serialization [").append("bean").append("] only support message type ").append(JavaBeanDescriptor.class.getName()).append(" and your message type is ").append(args[i].getClass().getName()).toString());
                        }
                        args[i] = JavaBeanSerializeUtil.deserialize((JavaBeanDescriptor)args[i]);
                    }
                }
                Result result = invoker.invoke(new RpcInvocation(method, args, inv.getAttachments()));
                if (result.hasException() && !(result.getException() instanceof GenericException)) {
                    return new RpcResult(new GenericException(result.getException()));
                }
                if (ProtocolUtils.isJavaGenericSerialization(generic)) {
                    try {
                        UnsafeByteArrayOutputStream os = new UnsafeByteArrayOutputStream(512);
                        ExtensionLoader.getExtensionLoader(Serialization.class).getExtension("nativejava").serialize(null, os).writeObject(result.getValue());
                        return new RpcResult(os.toByteArray());
                    }
                    catch (IOException e) {
                        throw new RpcException("Serialize result failed.", (Throwable)e);
                    }
                }
                if (ProtocolUtils.isBeanGenericSerialization(generic)) {
                    return new RpcResult(JavaBeanSerializeUtil.serialize(result.getValue(), JavaBeanAccessor.METHOD));
                }
                if (ProtocolUtils.isResultNoChangeGenericSerialization(generic)) {
                    return new RpcResult(result.getValue());
                }
                if (ProtocolUtils.isJsonResultGenericSerialization(generic)) {
                    String optimizeNull = inv.getAttachments().get("application.serialize.null");
                    if (Boolean.parseBoolean(optimizeNull)) {
                        return new RpcResult(JSON.toJSONString((Object)result.getValue(), (SerializerFeature[])new SerializerFeature[]{SerializerFeature.WriteNonStringKeyAsString, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullStringAsEmpty}));
                    }
                    return new RpcResult(JSON.toJSONString((Object)result.getValue(), (SerializerFeature[])new SerializerFeature[]{SerializerFeature.WriteNonStringKeyAsString, SerializerFeature.DisableCircularReferenceDetect}));
                }
                return new RpcResult(PojoUtils.generalize(result.getValue()));
            }
            catch (NoSuchMethodException e) {
                throw new RpcException(e.getMessage(), (Throwable)e);
            }
            catch (ClassNotFoundException e) {
                throw new RpcException(e.getMessage(), (Throwable)e);
            }
        }
        return invoker.invoke(inv);
    }
}

