/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.proxy.wrapper;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.bytecode.Wrapper;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.service.GenericService;
import java.lang.reflect.Constructor;

public class StubProxyFactoryWrapper
implements ProxyFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubProxyFactoryWrapper.class);
    private final ProxyFactory proxyFactory;
    private Protocol protocol;

    public StubProxyFactoryWrapper(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public <T> T getProxy(Invoker<T> invoker) throws RpcException {
        Object proxy;
        String stub;
        proxy = this.proxyFactory.getProxy(invoker);
        if (GenericService.class != invoker.getInterface() && ConfigUtils.isNotEmpty(stub = invoker.getUrl().getParameter("stub", invoker.getUrl().getParameter("local")))) {
            Class<T> serviceType = invoker.getInterface();
            if (ConfigUtils.isDefault(stub)) {
                stub = invoker.getUrl().hasParameter("stub") ? serviceType.getName() + "Stub" : serviceType.getName() + "Local";
            }
            try {
                Class<?> stubClass = ReflectUtils.forName(stub);
                if (!serviceType.isAssignableFrom(stubClass)) {
                    throw new IllegalStateException("The stub implemention class " + stubClass.getName() + " not implement interface " + serviceType.getName());
                }
                try {
                    Constructor<?> constructor = ReflectUtils.findConstructor(stubClass, serviceType);
                    proxy = constructor.newInstance(proxy);
                    URL url = invoker.getUrl();
                    if (url.getParameter("dubbo.stub.event", false)) {
                        url = url.addParameter("dubbo.stub.event.methods", StringUtils.join(Wrapper.getWrapper(proxy.getClass()).getDeclaredMethodNames(), ","));
                        url = url.addParameter("isserver", Boolean.FALSE.toString());
                        try {
                            this.export(proxy, invoker.getInterface(), url);
                        }
                        catch (Exception e) {
                            LOGGER.error("export a stub service error.", e);
                        }
                    }
                }
                catch (NoSuchMethodException e) {
                    throw new IllegalStateException("No such constructor \"public " + stubClass.getSimpleName() + "(" + serviceType.getName() + ")\" in stub implemention class " + stubClass.getName(), e);
                }
            }
            catch (Throwable t) {
                LOGGER.error("Failed to create stub implemention class " + stub + " in consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + ", cause: " + t.getMessage(), t);
            }
        }
        return (T)proxy;
    }

    @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException {
        return this.proxyFactory.getInvoker(proxy, type, url);
    }

    private <T> Exporter<T> export(T instance, Class<T> type, URL url) {
        return this.protocol.export(this.proxyFactory.getInvoker(instance, type, url));
    }
}

