/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.PojoUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MockInvoker<T>
implements Invoker<T> {
    private static final ProxyFactory proxyFactory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();
    private static final Map<String, Invoker<?>> mocks = new ConcurrentHashMap();
    private static final Map<String, Throwable> throwables = new ConcurrentHashMap<String, Throwable>();
    private final URL url;

    public MockInvoker(URL url) {
        this.url = url;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        String mock = this.getUrl().getParameter(invocation.getMethodName() + "." + "mock");
        if (invocation instanceof RpcInvocation) {
            ((RpcInvocation)invocation).setInvoker(this);
        }
        if (StringUtils.isBlank(mock)) {
            mock = this.getUrl().getParameter("mock");
        }
        if (StringUtils.isBlank(mock)) {
            throw new RpcException(new IllegalAccessException("mock can not be null. url :" + this.url));
        }
        mock = this.normallizeMock(URL.decode(mock));
        if ("return ".trim().equalsIgnoreCase(mock.trim())) {
            RpcResult result = new RpcResult();
            result.setValue(null);
            return result;
        }
        if (mock.startsWith("return ")) {
            mock = mock.substring("return ".length()).trim();
            mock = mock.replace('`', '\"');
            try {
                Type[] returnTypes = RpcUtils.getReturnTypes(invocation);
                Object value = MockInvoker.parseMockValue(mock, returnTypes);
                return new RpcResult(value);
            }
            catch (Exception ew) {
                throw new RpcException("mock return invoke error. method :" + invocation.getMethodName() + ", mock:" + mock + ", url: " + this.url, (Throwable)ew);
            }
        }
        if (mock.startsWith("throw")) {
            mock = mock.substring("throw".length()).trim();
            if (StringUtils.isBlank(mock = mock.replace('`', '\"'))) {
                throw new RpcException(" mocked exception for Service degradation. ");
            }
            Throwable t = this.getThrowable(mock);
            throw new RpcException(3, t);
        }
        try {
            Invoker<T> invoker = this.getInvoker(mock);
            return invoker.invoke(invocation);
        }
        catch (Throwable t) {
            throw new RpcException("Failed to create mock implemention class " + mock, t);
        }
    }

    private Throwable getThrowable(String throwstr) {
        Throwable throwable = throwables.get(throwstr);
        if (throwable != null) {
            return throwable;
        }
        Throwable t = null;
        try {
            Class<?> bizException = ReflectUtils.forName(throwstr);
            Constructor<?> constructor = ReflectUtils.findConstructor(bizException, String.class);
            t = (Throwable)constructor.newInstance(" mocked exception for Service degradation. ");
            if (throwables.size() < 1000) {
                throwables.put(throwstr, t);
            }
        }
        catch (Exception e) {
            throw new RpcException("mock throw error :" + throwstr + " argument error.", (Throwable)e);
        }
        return t;
    }

    private Invoker<T> getInvoker(String mockService) {
        Class<?> mockClass;
        Invoker<?> invoker = mocks.get(mockService);
        if (invoker != null) {
            return invoker;
        }
        Class<?> serviceType = ReflectUtils.forName(this.url.getServiceInterface());
        if (ConfigUtils.isDefault(mockService)) {
            mockService = serviceType.getName() + "Mock";
        }
        if (!serviceType.isAssignableFrom(mockClass = ReflectUtils.forName(mockService))) {
            throw new IllegalArgumentException("The mock implemention class " + mockClass.getName() + " not implement interface " + serviceType.getName());
        }
        if (!serviceType.isAssignableFrom(mockClass)) {
            throw new IllegalArgumentException("The mock implemention class " + mockClass.getName() + " not implement interface " + serviceType.getName());
        }
        try {
            Object mockObject = mockClass.newInstance();
            invoker = proxyFactory.getInvoker(mockObject, serviceType, this.url);
            if (mocks.size() < 10000) {
                mocks.put(mockService, invoker);
            }
            return invoker;
        }
        catch (InstantiationException e) {
            throw new IllegalStateException("No such empty constructor \"public " + mockClass.getSimpleName() + "()\" in mock implemention class " + mockClass.getName(), e);
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private String normallizeMock(String mock) {
        if (mock == null || mock.trim().length() == 0) {
            return mock;
        }
        if (ConfigUtils.isDefault(mock) || "fail".equalsIgnoreCase(mock.trim()) || "force".equalsIgnoreCase(mock.trim())) {
            mock = this.url.getServiceInterface() + "Mock";
        }
        if (mock.startsWith("fail:")) {
            mock = mock.substring("fail:".length()).trim();
        } else if (mock.startsWith("force:")) {
            mock = mock.substring("force:".length()).trim();
        }
        return mock;
    }

    public static Object parseMockValue(String mock) throws Exception {
        return MockInvoker.parseMockValue(mock, null);
    }

    public static Object parseMockValue(String mock, Type[] returnTypes) throws Exception {
        Object value = null;
        value = "empty".equals(mock) ? ReflectUtils.getEmptyObject(returnTypes != null && returnTypes.length > 0 ? (Class)returnTypes[0] : null) : ("null".equals(mock) ? null : ("true".equals(mock) ? Boolean.valueOf(true) : ("false".equals(mock) ? Boolean.valueOf(false) : (mock.length() >= 2 && (mock.startsWith("\"") && mock.endsWith("\"") || mock.startsWith("'") && mock.endsWith("'")) ? mock.subSequence(1, mock.length() - 1) : (returnTypes != null && returnTypes.length > 0 && returnTypes[0] == String.class ? mock : (StringUtils.isNumeric(mock) ? JSON.parse(mock) : (mock.startsWith("{") ? JSON.parse(mock, Map.class) : (mock.startsWith("[") ? JSON.parse(mock, List.class) : mock))))))));
        if (returnTypes != null && returnTypes.length > 0) {
            value = PojoUtils.realize(value, (Class)returnTypes[0], returnTypes.length > 1 ? returnTypes[1] : null);
        }
        return value;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {
    }

    @Override
    public Class<T> getInterface() {
        return null;
    }
}

