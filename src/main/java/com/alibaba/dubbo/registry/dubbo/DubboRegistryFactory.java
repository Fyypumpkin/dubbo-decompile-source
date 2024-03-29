/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.bytecode.Wrapper;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryService;
import com.alibaba.dubbo.registry.dubbo.DubboRegistry;
import com.alibaba.dubbo.registry.integration.RegistryDirectory;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DubboRegistryFactory
extends AbstractRegistryFactory {
    private Protocol protocol;
    private ProxyFactory proxyFactory;
    private Cluster cluster;

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public void setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public Registry createRegistry(URL url) {
        url = DubboRegistryFactory.getRegistryURL(url);
        ArrayList<URL> urls = new ArrayList<URL>();
        urls.add(url.removeParameter("backup"));
        String backup = url.getParameter("backup");
        if (backup != null && backup.length() > 0) {
            String[] addresses;
            for (String address : addresses = Constants.COMMA_SPLIT_PATTERN.split(backup)) {
                urls.add(url.setAddress(address));
            }
        }
        RegistryDirectory<RegistryService> directory = new RegistryDirectory<RegistryService>(RegistryService.class, url.addParameter("interface", RegistryService.class.getName()).addParameterAndEncoded("refer", url.toParameterString()));
        Invoker<RegistryService> registryInvoker = this.cluster.join(directory);
        RegistryService registryService = this.proxyFactory.getProxy(registryInvoker);
        DubboRegistry registry = new DubboRegistry(registryInvoker, registryService);
        directory.setRegistry(registry);
        directory.setProtocol(this.protocol);
        directory.notify(urls);
        directory.subscribe(new URL("consumer", NetUtils.getLocalHost(), 0, RegistryService.class.getName(), url.getParameters()));
        return registry;
    }

    private static URL getRegistryURL(URL url) {
        return url.setPath(RegistryService.class.getName()).removeParameter("export").removeParameter("refer").addParameter("interface", RegistryService.class.getName()).addParameter("sticky", "true").addParameter("lazy", "true").addParameter("reconnect", "false").addParameterIfAbsent("timeout", "10000").addParameterIfAbsent("callbacks", "10000").addParameterIfAbsent("connect.timeout", "10000").addParameter("methods", StringUtils.join(new HashSet<String>(Arrays.asList(Wrapper.getWrapper(RegistryService.class).getDeclaredMethodNames())), ",")).addParameter("subscribe.1.callback", "true").addParameter("unsubscribe.1.callback", "false");
    }
}

