/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FailoverClusterInvoker<T>
extends AbstractClusterInvoker<T> {
    private static final Logger logger = LoggerFactory.getLogger(FailoverClusterInvoker.class);

    public FailoverClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
        List<Invoker<T>> copyinvokers = invokers;
        this.checkInvokers(copyinvokers, invocation);
        int len = this.getUrl().getMethodParameter(invocation.getMethodName(), "retries", 0) + 1;
        if (len <= 0) {
            len = 1;
        }
        RpcException le = null;
        ArrayList invoked = new ArrayList(copyinvokers.size());
        HashSet<String> providers = new HashSet<String>(len);
        for (int i = 0; i < len; ++i) {
            if (i > 0) {
                this.checkWhetherDestroyed();
                copyinvokers = this.list(invocation);
                this.checkInvokers(copyinvokers, invocation);
            }
            Invoker<T> invoker = this.select(loadbalance, invocation, copyinvokers, invoked);
            invoked.add(invoker);
            RpcContext.getContext().setInvokers(invoked);
            try {
                if (invocation instanceof RpcInvocation) {
                    ((RpcInvocation)invocation).setAttachment("isLastCall", Boolean.toString(i == len - 1));
                }
                Result result = invoker.invoke(invocation);
                if (le != null && logger.isWarnEnabled()) {
                    logger.warn("Although retry the method " + invocation.getMethodName() + " in the service " + this.getInterface().getName() + " was successful by the provider " + invoker.getUrl().getAddress() + ", but there have been failed providers " + providers + " (" + providers.size() + "/" + copyinvokers.size() + ") from the registry " + this.directory.getUrl().getAddress() + " on the consumer " + NetUtils.getLocalHost() + " using the dubbo version " + Version.getVersion() + ". Last error is: " + le.getMessage(), le);
                }
                Result result2 = result;
                return result2;
            }
            catch (RpcException e) {
                if (e.isBiz() || e.isNovaValidate() || e.isTpsLimit()) {
                    throw e;
                }
                le = e;
                continue;
            }
            catch (Throwable e) {
                le = new RpcException(e.getMessage(), e);
                continue;
            }
            finally {
                providers.add(invoker.getUrl().getAddress());
            }
        }
        throw new RpcException(le != null ? le.getCode() : 0, "Failed to invoke the method " + invocation.getMethodName() + " in the service " + this.getInterface().getName() + ". Tried " + len + " times of the providers " + providers + " (" + providers.size() + "/" + copyinvokers.size() + ") from the registry " + this.directory.getUrl().getAddress() + " on the consumer " + NetUtils.getLocalHost() + " using the dubbo version " + Version.getVersion() + ". Last error is: " + (le != null ? le.getMessage() : ""), le != null && le.getCause() != null ? le.getCause() : le);
    }
}

