/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.filter;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.beanutil.JavaBeanAccessor;
import com.alibaba.dubbo.common.beanutil.JavaBeanDescriptor;
import com.alibaba.dubbo.common.beanutil.JavaBeanSerializeUtil;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.PojoUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

@Activate(group={"consumer"}, value={"generic"}, order=20000)
public class GenericImplFilter
implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(GenericImplFilter.class);
    private static final Class<?>[] GENERIC_PARAMETER_TYPES = new Class[]{String.class, String[].class, Object[].class};

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String generic = invoker.getUrl().getParameter("generic");
        if (ProtocolUtils.isGeneric(generic) && !"$invoke".equals(invocation.getMethodName()) && invocation instanceof RpcInvocation) {
            Result result;
            block30 : {
                Object[] args;
                RpcInvocation invocation2 = (RpcInvocation)invocation;
                String methodName = invocation2.getMethodName();
                Class<?>[] parameterTypes = invocation2.getParameterTypes();
                Object[] arguments = invocation2.getArguments();
                String[] types = new String[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; ++i) {
                    types[i] = ReflectUtils.getName(parameterTypes[i]);
                }
                if (ProtocolUtils.isBeanGenericSerialization(generic)) {
                    args = new Object[arguments.length];
                    for (int i = 0; i < arguments.length; ++i) {
                        args[i] = JavaBeanSerializeUtil.serialize(arguments[i], JavaBeanAccessor.METHOD);
                    }
                } else {
                    args = PojoUtils.generalize(arguments);
                }
                invocation2.setMethodName("$invoke");
                invocation2.setParameterTypes(GENERIC_PARAMETER_TYPES);
                invocation2.setArguments(new Object[]{methodName, types, args});
                result = invoker.invoke(invocation2);
                if (!result.hasException()) {
                    Object value = result.getValue();
                    try {
                        Method method = invoker.getInterface().getMethod(methodName, parameterTypes);
                        if (ProtocolUtils.isBeanGenericSerialization(generic)) {
                            if (value == null) {
                                return new RpcResult(value);
                            }
                            if (value instanceof JavaBeanDescriptor) {
                                return new RpcResult(JavaBeanSerializeUtil.deserialize((JavaBeanDescriptor)value));
                            }
                            throw new RpcException(new StringBuilder(64).append("The type of result value is ").append(value.getClass().getName()).append(" other than ").append(JavaBeanDescriptor.class.getName()).append(", and the result is ").append(value).toString());
                        }
                        return new RpcResult(PojoUtils.realize(value, method.getReturnType(), method.getGenericReturnType()));
                    }
                    catch (NoSuchMethodException e) {
                        throw new RpcException(e.getMessage(), (Throwable)e);
                    }
                }
                if (result.getException() instanceof GenericException) {
                    GenericException exception = (GenericException)result.getException();
                    try {
                        String className = exception.getExceptionClass();
                        Class<?> clazz = ReflectUtils.forName(className);
                        Throwable targetException = null;
                        Throwable lastException = null;
                        try {
                            targetException = (Throwable)clazz.newInstance();
                        }
                        catch (Throwable e) {
                            lastException = e;
                            for (Constructor<?> constructor : clazz.getConstructors()) {
                                try {
                                    targetException = (Throwable)constructor.newInstance(new Object[constructor.getParameterTypes().length]);
                                    break;
                                }
                                catch (Throwable e1) {
                                    lastException = e1;
                                }
                            }
                        }
                        if (targetException != null) {
                            try {
                                Field field = Throwable.class.getDeclaredField("detailMessage");
                                if (!field.isAccessible()) {
                                    field.setAccessible(true);
                                }
                                field.set(targetException, exception.getExceptionMessage());
                            }
                            catch (Throwable e) {
                                logger.warn(e.getMessage(), e);
                            }
                            result = new RpcResult(targetException);
                            break block30;
                        }
                        if (lastException != null) {
                            throw lastException;
                        }
                    }
                    catch (Throwable e) {
                        throw new RpcException("Can not deserialize exception " + exception.getExceptionClass() + ", message: " + exception.getExceptionMessage(), e);
                    }
                }
            }
            return result;
        }
        if (invocation.getMethodName().equals("$invoke") && invocation.getArguments() != null && invocation.getArguments().length == 3 && ProtocolUtils.isGeneric(generic)) {
            Object[] args = (Object[])invocation.getArguments()[2];
            if (ProtocolUtils.isJavaGenericSerialization(generic)) {
                for (Object arg : args) {
                    if (byte[].class == arg.getClass()) continue;
                    this.error(byte[].class.getName(), arg.getClass().getName());
                }
            } else if (ProtocolUtils.isBeanGenericSerialization(generic)) {
                for (Object arg : args) {
                    if (arg instanceof JavaBeanDescriptor) continue;
                    this.error(JavaBeanDescriptor.class.getName(), arg.getClass().getName());
                }
            }
            ((RpcInvocation)invocation).setAttachment("generic", invoker.getUrl().getParameter("generic"));
        }
        return invoker.invoke(invocation);
    }

    private void error(String expected, String actual) throws RpcException {
        throw new RpcException(new StringBuilder(32).append("Generic serialization [").append("nativejava").append("] only support message type ").append(expected).append(" and your message type is ").append(actual).toString());
    }
}

