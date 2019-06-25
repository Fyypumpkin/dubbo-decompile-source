/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.resteasy.spi.Registry
 *  org.jboss.resteasy.spi.ResourceFactory
 *  org.jboss.resteasy.spi.ResteasyDeployment
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.protocol.rest.DubboResourceFactory;
import com.alibaba.dubbo.rpc.protocol.rest.RestServer;
import com.alibaba.dubbo.rpc.protocol.rest.RpcContextFilter;
import com.alibaba.dubbo.rpc.protocol.rest.RpcExceptionMapper;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyDeployment;

public abstract class BaseRestServer
implements RestServer {
    @Override
    public void start(URL url) {
        this.getDeployment().getMediaTypeMappings().put("json", "application/json");
        this.getDeployment().getMediaTypeMappings().put("xml", "text/xml");
        this.getDeployment().getProviderClasses().add(RpcContextFilter.class.getName());
        this.getDeployment().getProviderClasses().add(RpcExceptionMapper.class.getName());
        this.loadProviders(url.getParameter("extension", ""));
        this.registerInjectorFactory(url.getParameter("injector.factory", ""));
        this.doStart(url);
    }

    public void registerInjectorFactory(String injectorFactory) {
        if (StringUtils.isNotEmpty(injectorFactory)) {
            this.getDeployment().setInjectorFactoryClass(injectorFactory);
        }
    }

    @Override
    public void deploy(Class resourceDef, Object resourceInstance, String contextPath) {
        if (StringUtils.isEmpty(contextPath)) {
            this.getDeployment().getRegistry().addResourceFactory((ResourceFactory)new DubboResourceFactory(resourceInstance, resourceDef));
        } else {
            this.getDeployment().getRegistry().addResourceFactory((ResourceFactory)new DubboResourceFactory(resourceInstance, resourceDef), contextPath);
        }
    }

    @Override
    public void undeploy(Class resourceDef) {
        this.getDeployment().getRegistry().removeRegistrations(resourceDef);
    }

    protected void loadProviders(String value) {
        for (String clazz : Constants.COMMA_SPLIT_PATTERN.split(value)) {
            if (StringUtils.isEmpty(clazz)) continue;
            this.getDeployment().getProviderClasses().add(clazz.trim());
        }
    }

    protected abstract ResteasyDeployment getDeployment();

    protected abstract void doStart(URL var1);
}

