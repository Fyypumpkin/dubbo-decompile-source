/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.cache.filter;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.cache.CacheFactory;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;

@Activate(group={"consumer", "provider"}, value={"cache"})
public class CacheFilter
implements Filter {
    private CacheFactory cacheFactory;

    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Cache cache;
        if (this.cacheFactory != null && ConfigUtils.isNotEmpty(invoker.getUrl().getMethodParameter(invocation.getMethodName(), "cache")) && (cache = this.cacheFactory.getCache(invoker.getUrl(), invocation)) != null) {
            String key = StringUtils.toArgumentString(invocation.getArguments());
            Object value = cache.get(key);
            if (value != null) {
                return new RpcResult(value);
            }
            Result result = invoker.invoke(invocation);
            if (!result.hasException()) {
                cache.put(key, result.getValue());
            }
            return result;
        }
        return invoker.invoke(invocation);
    }
}

