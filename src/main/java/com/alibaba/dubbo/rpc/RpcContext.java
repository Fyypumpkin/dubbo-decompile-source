/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.youzan.platform.service_chain.context.ServiceChainContext
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.youzan.platform.service_chain.context.ServiceChainContext;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RpcContext {
    private static final ThreadLocal<RpcContext> LOCAL = new ThreadLocal<RpcContext>(){

        @Override
        protected RpcContext initialValue() {
            return new RpcContext();
        }
    };
    private Future<?> future;
    private List<URL> urls;
    private URL url;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] arguments;
    private InetSocketAddress localAddress;
    private InetSocketAddress remoteAddress;
    private final Map<String, String> attachments = new HashMap<String, String>();
    private final Map<String, Object> values = new HashMap<String, Object>();
    private Object request;
    private Object response;
    @Deprecated
    private List<Invoker<?>> invokers;
    @Deprecated
    private Invoker<?> invoker;
    @Deprecated
    private Invocation invocation;

    public static RpcContext getContext() {
        return LOCAL.get();
    }

    public static void removeContext() {
        LOCAL.remove();
    }

    protected RpcContext() {
    }

    public Object getRequest() {
        return this.request;
    }

    public <T> T getRequest(Class<T> clazz) {
        return (T)(this.request != null && clazz.isAssignableFrom(this.request.getClass()) ? this.request : null);
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public Object getResponse() {
        return this.response;
    }

    public <T> T getResponse(Class<T> clazz) {
        return (T)(this.response != null && clazz.isAssignableFrom(this.response.getClass()) ? this.response : null);
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public boolean isProviderSide() {
        URL url = this.getUrl();
        if (url == null) {
            return false;
        }
        InetSocketAddress address = this.getRemoteAddress();
        if (address == null) {
            return false;
        }
        String host = address.getAddress() == null ? address.getHostName() : address.getAddress().getHostAddress();
        return url.getPort() != address.getPort() || !NetUtils.filterLocalHost(url.getIp()).equals(NetUtils.filterLocalHost(host));
    }

    public boolean isConsumerSide() {
        URL url = this.getUrl();
        if (url == null) {
            return false;
        }
        InetSocketAddress address = this.getRemoteAddress();
        if (address == null) {
            return false;
        }
        String host = address.getAddress() == null ? address.getHostName() : address.getAddress().getHostAddress();
        return url.getPort() == address.getPort() && NetUtils.filterLocalHost(url.getIp()).equals(NetUtils.filterLocalHost(host));
    }

    public <T> Future<T> getFuture() {
        return this.future;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    public List<URL> getUrls() {
        return this.urls == null && this.url != null ? Arrays.asList(this.url) : this.urls;
    }

    public void setUrls(List<URL> urls) {
        this.urls = urls;
    }

    public URL getUrl() {
        return this.url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return this.parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArguments() {
        return this.arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public RpcContext setLocalAddress(InetSocketAddress address) {
        this.localAddress = address;
        return this;
    }

    public RpcContext setLocalAddress(String host, int port) {
        if (port < 0) {
            port = 0;
        }
        this.localAddress = InetSocketAddress.createUnresolved(host, port);
        return this;
    }

    public InetSocketAddress getLocalAddress() {
        return this.localAddress;
    }

    public String getLocalAddressString() {
        return this.getLocalHost() + ":" + this.getLocalPort();
    }

    public String getLocalHostName() {
        String host;
        String string = host = this.localAddress == null ? null : this.localAddress.getHostName();
        if (host == null || host.length() == 0) {
            return this.getLocalHost();
        }
        return host;
    }

    public RpcContext setRemoteAddress(InetSocketAddress address) {
        this.remoteAddress = address;
        return this;
    }

    public RpcContext setRemoteAddress(String host, int port) {
        if (port < 0) {
            port = 0;
        }
        this.remoteAddress = InetSocketAddress.createUnresolved(host, port);
        return this;
    }

    public InetSocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public String getRemoteAddressString() {
        return this.getRemoteHost() + ":" + this.getRemotePort();
    }

    public String getRemoteHostName() {
        return this.remoteAddress == null ? null : this.remoteAddress.getHostName();
    }

    public String getLocalHost() {
        String host;
        String string = this.localAddress == null ? null : (host = this.localAddress.getAddress() == null ? this.localAddress.getHostName() : NetUtils.filterLocalHost(this.localAddress.getAddress().getHostAddress()));
        if (host == null || host.length() == 0) {
            return NetUtils.getLocalHost();
        }
        return host;
    }

    public int getLocalPort() {
        return this.localAddress == null ? 0 : this.localAddress.getPort();
    }

    public String getRemoteHost() {
        return this.remoteAddress == null ? null : (this.remoteAddress.getAddress() == null ? this.remoteAddress.getHostName() : NetUtils.filterLocalHost(this.remoteAddress.getAddress().getHostAddress()));
    }

    public int getRemotePort() {
        return this.remoteAddress == null ? 0 : this.remoteAddress.getPort();
    }

    public String getAttachment(String key) {
        return this.attachments.get(key);
    }

    public RpcContext setAttachment(String key, String value) {
        if (value == null) {
            this.attachments.remove(key);
        } else {
            this.attachments.put(key, value);
        }
        return this;
    }

    public RpcContext removeAttachment(String key) {
        this.attachments.remove(key);
        return this;
    }

    public Map<String, String> getAttachments() {
        return this.attachments;
    }

    public RpcContext setAttachments(Map<String, String> attachment) {
        this.attachments.clear();
        if (attachment != null && attachment.size() > 0) {
            this.attachments.putAll(attachment);
        }
        return this;
    }

    public void clearAttachments() {
        this.attachments.clear();
    }

    public Map<String, Object> get() {
        return this.values;
    }

    public RpcContext set(String key, Object value) {
        if (value == null) {
            this.values.remove(key);
        } else {
            this.values.put(key, value);
        }
        return this;
    }

    public RpcContext remove(String key) {
        this.values.remove(key);
        return this;
    }

    public Object get(String key) {
        return this.values.get(key);
    }

    public RpcContext setInvokers(List<Invoker<?>> invokers) {
        this.invokers = invokers;
        if (invokers != null && invokers.size() > 0) {
            ArrayList<URL> urls = new ArrayList<URL>(invokers.size());
            for (Invoker<?> invoker : invokers) {
                urls.add(invoker.getUrl());
            }
            this.setUrls(urls);
        }
        return this;
    }

    public RpcContext setInvoker(Invoker<?> invoker) {
        this.invoker = invoker;
        if (invoker != null) {
            this.setUrl(invoker.getUrl());
        }
        return this;
    }

    public RpcContext setInvocation(Invocation invocation) {
        this.invocation = invocation;
        if (invocation != null) {
            this.setMethodName(invocation.getMethodName());
            this.setParameterTypes(invocation.getParameterTypes());
            this.setArguments(invocation.getArguments());
        }
        return this;
    }

    @Deprecated
    public boolean isServerSide() {
        return this.isProviderSide();
    }

    @Deprecated
    public boolean isClientSide() {
        return this.isConsumerSide();
    }

    @Deprecated
    public List<Invoker<?>> getInvokers() {
        return this.invokers == null && this.invoker != null ? Arrays.asList(this.invoker) : this.invokers;
    }

    @Deprecated
    public Invoker<?> getInvoker() {
        return this.invoker;
    }

    @Deprecated
    public Invocation getInvocation() {
        return this.invocation;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public <T> Future<T> asyncCall(Callable<T> callable) {
        try {
            try {
                this.setAttachment("async", Boolean.TRUE.toString());
                final T o = callable.call();
                if (o == null) return RpcContext.getContext().getFuture();
                FutureTask f = new FutureTask(new Callable<T>(){

                    @Override
                    public T call() throws Exception {
                        return (T)o;
                    }
                });
                f.run();
                FutureTask futureTask = f;
                return futureTask;
            }
            catch (Exception e) {
                throw new RpcException(e);
            }
            finally {
                this.removeAttachment("async");
            }
        }
        catch (RpcException e) {
            return new Future<T>(){

                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return false;
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }

                @Override
                public boolean isDone() {
                    return true;
                }

                @Override
                public T get() throws InterruptedException, ExecutionException {
                    throw new ExecutionException(e.getCause());
                }

                @Override
                public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return this.get();
                }
            };
        }
    }

    public void asyncCall(Runnable runable) {
        try {
            this.setAttachment("return", Boolean.FALSE.toString());
            runable.run();
        }
        catch (Throwable e) {
            throw new RpcException("oneway call error ." + e.getMessage(), e);
        }
        finally {
            this.removeAttachment("return");
        }
    }

    public String getServiceChainName() {
        Map serviceChainMap = this.getServiceChainContext();
        if (null != serviceChainMap && serviceChainMap.get("name") instanceof String) {
            return (String)serviceChainMap.get("name");
        }
        return null;
    }

    public Map getServiceChainContext() {
        return ServiceChainContext.getInvocationServiceChainContext();
    }

    public void setServiceChainContext(Map serviceChainMap) {
        ServiceChainContext.setInvocationServiceChainContext((Map)serviceChainMap);
    }

}

