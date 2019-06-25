/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.proxy;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.service.EchoService;
import java.util.regex.Pattern;

public abstract class AbstractProxyFactory
implements ProxyFactory {
    @Override
    public <T> T getProxy(Invoker<T> invoker) throws RpcException {
        String[] types;
        Class[] interfaces = null;
        String config = invoker.getUrl().getParameter("interfaces");
        if (config != null && config.length() > 0 && (types = Constants.COMMA_SPLIT_PATTERN.split(config)) != null && types.length > 0) {
            interfaces = new Class[types.length + 2];
            interfaces[0] = invoker.getInterface();
            interfaces[1] = EchoService.class;
            for (int i = 0; i < types.length; ++i) {
                interfaces[i + 1] = ReflectUtils.forName(types[i]);
            }
        }
        if (interfaces == null) {
            interfaces = new Class[]{invoker.getInterface(), EchoService.class};
        }
        return this.getProxy(invoker, interfaces);
    }

    public abstract <T> T getProxy(Invoker<T> var1, Class<?>[] var2);
}

