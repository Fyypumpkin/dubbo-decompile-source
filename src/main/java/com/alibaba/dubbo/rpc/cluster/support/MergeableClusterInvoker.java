/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.Merger;
import com.alibaba.dubbo.rpc.cluster.merger.MergerFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MergeableClusterInvoker<T>
implements Invoker<T> {
    private static final Logger log = LoggerFactory.getLogger(MergeableClusterInvoker.class);
    private ExecutorService executor = Executors.newCachedThreadPool(new NamedThreadFactory("mergeable-cluster-executor", true));
    private final Directory<T> directory;

    public MergeableClusterInvoker(Directory<T> directory) {
        this.directory = directory;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public Result invoke(final Invocation invocation) throws RpcException {
        Class<?> returnType;
        List<Invoker<T>> invokers = this.directory.list(invocation);
        String merger = this.getUrl().getMethodParameter(invocation.getMethodName(), "merger");
        if (ConfigUtils.isEmpty(merger)) {
            for (Invoker<T> invoker : invokers) {
                if (!invoker.isAvailable()) continue;
                return invoker.invoke(invocation);
            }
            return invokers.iterator().next().invoke(invocation);
        }
        try {
            returnType = this.getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes()).getReturnType();
        }
        catch (NoSuchMethodException e) {
            returnType = null;
        }
        HashMap<String, Future<Result>> results = new HashMap<String, Future<Result>>();
        for (final Invoker<T> invoker : invokers) {
            Future<Result> future = this.executor.submit(new Callable<Result>(){

                @Override
                public Result call() throws Exception {
                    return invoker.invoke(new RpcInvocation(invocation, invoker));
                }
            });
            results.put(invoker.getUrl().getServiceKey(), future);
        }
        Object result = null;
        ArrayList<Result> resultList = new ArrayList<Result>(results.size());
        int timeout = this.getUrl().getMethodParameter(invocation.getMethodName(), "timeout", 1000);
        for (Map.Entry entry : results.entrySet()) {
            Future future = (Future)entry.getValue();
            try {
                Result r = (Result)future.get(timeout, TimeUnit.MILLISECONDS);
                if (r.hasException()) {
                    log.error(new StringBuilder(32).append("Invoke ").append(this.getGroupDescFromServiceKey((String)entry.getKey())).append(" failed: ").append(r.getException().getMessage()).toString(), r.getException());
                    continue;
                }
                resultList.add(r);
            }
            catch (Exception e) {
                throw new RpcException(new StringBuilder(32).append("Failed to invoke service ").append((String)entry.getKey()).append(": ").append(e.getMessage()).toString(), (Throwable)e);
            }
        }
        if (resultList.size() == 0) {
            return new RpcResult((Object)null);
        }
        if (resultList.size() == 1) {
            return (Result)resultList.iterator().next();
        }
        if (returnType == Void.TYPE) {
            return new RpcResult((Object)null);
        }
        if (merger.startsWith(".")) {
            Method method;
            merger = merger.substring(1);
            try {
                method = returnType.getMethod(merger, returnType);
            }
            catch (NoSuchMethodException e) {
                throw new RpcException(new StringBuilder(32).append("Can not merge result because missing method [ ").append(merger).append(" ] in class [ ").append(returnType.getClass().getName()).append(" ]").toString());
            }
            if (method == null) throw new RpcException(new StringBuilder(32).append("Can not merge result because missing method [ ").append(merger).append(" ] in class [ ").append(returnType.getClass().getName()).append(" ]").toString());
            if (!Modifier.isPublic(method.getModifiers())) {
                method.setAccessible(true);
            }
            result = ((Result)resultList.remove(0)).getValue();
            try {
                if (method.getReturnType() != Void.TYPE && method.getReturnType().isAssignableFrom(result.getClass())) {
                    for (Result r : resultList) {
                        result = method.invoke(result, r.getValue());
                    }
                    return new RpcResult(result);
                }
                for (Result r : resultList) {
                    method.invoke(result, r.getValue());
                }
                return new RpcResult(result);
            }
            catch (Exception e) {
                throw new RpcException(new StringBuilder(32).append("Can not merge result: ").append(e.getMessage()).toString(), (Throwable)e);
            }
        }
        Merger resultMerger = ConfigUtils.isDefault(merger) ? MergerFactory.getMerger(returnType) : ExtensionLoader.getExtensionLoader(Merger.class).getExtension(merger);
        if (resultMerger == null) throw new RpcException("There is no merger to merge result.");
        ArrayList<Object> rets = new ArrayList<Object>(resultList.size());
        for (Result r : resultList) {
            rets.add(r.getValue());
        }
        result = resultMerger.merge(rets.toArray((Object[])Array.newInstance(returnType, 0)));
        return new RpcResult(result);
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
        return this.directory.isAvailable();
    }

    @Override
    public void destroy() {
        this.directory.destroy();
    }

    private String getGroupDescFromServiceKey(String key) {
        int index = key.indexOf("/");
        if (index > 0) {
            return new StringBuilder(32).append("group [ ").append(key.substring(0, index)).append(" ]").toString();
        }
        return key;
    }

}

