/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractClusterInvoker<T>
implements Invoker<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractClusterInvoker.class);
    protected final Directory<T> directory;
    protected final boolean availablecheck;
    private AtomicBoolean destroyed = new AtomicBoolean(false);
    private volatile Invoker<T> stickyInvoker = null;

    public AbstractClusterInvoker(Directory<T> directory) {
        this(directory, directory.getUrl());
    }

    public AbstractClusterInvoker(Directory<T> directory, URL url) {
        if (directory == null) {
            throw new IllegalArgumentException("service directory == null");
        }
        this.directory = directory;
        this.availablecheck = url.getParameter("cluster.availablecheck", true);
    }

    @Override
    public Class<T> getInterface() {
        return this.directory.getInterface();
    }

    @Override
    public URL getUrl() {
        return this.directory.getUrl();
    }

    @Override
    public boolean isAvailable() {
        Invoker<T> invoker = this.stickyInvoker;
        if (invoker != null) {
            return invoker.isAvailable();
        }
        return this.directory.isAvailable();
    }

    @Override
    public void destroy() {
        if (this.destroyed.compareAndSet(false, true)) {
            this.directory.destroy();
        }
    }

    protected Invoker<T> select(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {
        if (invokers == null || invokers.isEmpty()) {
            return null;
        }
        String methodName = invocation == null ? "" : invocation.getMethodName();
        boolean sticky = invokers.get(0).getUrl().getMethodParameter(methodName, "sticky", false);
        if (this.stickyInvoker != null && !invokers.contains(this.stickyInvoker)) {
            this.stickyInvoker = null;
        }
        if (sticky && this.stickyInvoker != null && (selected == null || !selected.contains(this.stickyInvoker)) && this.availablecheck && this.stickyInvoker.isAvailable()) {
            return this.stickyInvoker;
        }
        Invoker<T> invoker = this.doSelect(loadbalance, invocation, invokers, selected);
        if (sticky) {
            this.stickyInvoker = invoker;
        }
        return invoker;
    }

    private Invoker<T> doSelect(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {
        if (invokers == null || invokers.isEmpty()) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        if (invokers.size() == 2 && selected != null && !selected.isEmpty()) {
            return selected.get(0) == invokers.get(0) ? invokers.get(1) : invokers.get(0);
        }
        if (loadbalance == null) {
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("random");
        }
        Invoker<T> invoker = loadbalance.select(invokers, this.getUrl(), invocation);
        if (selected != null && selected.contains(invoker) || !invoker.isAvailable() && this.getUrl() != null && this.availablecheck) {
            try {
                Invoker<T> rinvoker = this.reselect(loadbalance, invocation, invokers, selected, this.availablecheck);
                if (rinvoker != null) {
                    invoker = rinvoker;
                } else {
                    int index = invokers.indexOf(invoker);
                    try {
                        invoker = index < invokers.size() - 1 ? invokers.get(index + 1) : invoker;
                    }
                    catch (Exception e) {
                        logger.warn(e.getMessage() + " may because invokers list dynamic change, ignore.", e);
                    }
                }
            }
            catch (Throwable t) {
                logger.error("cluster reselect fail reason is :" + t.getMessage() + " if can not solve, you can set cluster.availablecheck=false in url", t);
            }
        }
        return invoker;
    }

    private Invoker<T> reselect(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected, boolean availablecheck) throws RpcException {
        ArrayList reselectInvokers = new ArrayList(invokers.size() > 1 ? invokers.size() - 1 : invokers.size());
        if (availablecheck) {
            for (Invoker<T> invoker : invokers) {
                if (!invoker.isAvailable() || selected != null && selected.contains(invoker)) continue;
                reselectInvokers.add(invoker);
            }
            if (!reselectInvokers.isEmpty()) {
                return loadbalance.select(reselectInvokers, this.getUrl(), invocation);
            }
        } else {
            for (Invoker<T> invoker : invokers) {
                if (selected != null && selected.contains(invoker)) continue;
                reselectInvokers.add(invoker);
            }
            if (!reselectInvokers.isEmpty()) {
                return loadbalance.select(reselectInvokers, this.getUrl(), invocation);
            }
        }
        if (selected != null) {
            for (Invoker<T> invoker : selected) {
                if (!invoker.isAvailable() || reselectInvokers.contains(invoker)) continue;
                reselectInvokers.add(invoker);
            }
        }
        if (!reselectInvokers.isEmpty()) {
            return loadbalance.select(reselectInvokers, this.getUrl(), invocation);
        }
        return null;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        this.checkWhetherDestroyed();
        LoadBalance loadbalance = null;
        List<Invoker<T>> invokers = this.list(invocation);
        if (invokers != null && !invokers.isEmpty()) {
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(invokers.get(0).getUrl().getMethodParameter(invocation.getMethodName(), "loadbalance", "random"));
        }
        RpcUtils.attachInvocationIdIfAsync(this.getUrl(), invocation);
        return this.doInvoke(invocation, invokers, loadbalance);
    }

    protected void checkWhetherDestroyed() {
        if (this.destroyed.get()) {
            throw new RpcException("Rpc cluster invoker for " + this.getInterface() + " on consumer " + NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + " is now destroyed! Can not invoke any more.");
        }
    }

    public String toString() {
        return this.getInterface() + " -> " + this.getUrl().toString();
    }

    protected void checkInvokers(List<Invoker<T>> invokers, Invocation invocation) {
        if (invokers == null || invokers.isEmpty()) {
            throw new RpcException("Failed to invoke the method " + invocation.getMethodName() + " in the service " + this.getInterface().getName() + ". No provider available for the service " + this.directory.getUrl().getServiceKey() + " from registry " + this.directory.getUrl().getAddress() + " on the consumer " + NetUtils.getLocalHost() + " using the dubbo version " + Version.getVersion() + ". Please check if the providers have been started and registered.");
        }
    }

    protected abstract Result doInvoke(Invocation var1, List<Invoker<T>> var2, LoadBalance var3) throws RpcException;

    protected List<Invoker<T>> list(Invocation invocation) throws RpcException {
        List<Invoker<T>> invokers = this.directory.list(invocation);
        return invokers;
    }
}

