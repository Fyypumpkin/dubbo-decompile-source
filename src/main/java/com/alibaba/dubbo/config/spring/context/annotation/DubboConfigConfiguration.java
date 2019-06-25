/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.spring.context.annotation;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubboConfigBinding;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubboConfigBindings;

public class DubboConfigConfiguration {

    @EnableDubboConfigBindings(value={@EnableDubboConfigBinding(prefix="dubbo.applications", type=ApplicationConfig.class, multiple=true), @EnableDubboConfigBinding(prefix="dubbo.modules", type=ModuleConfig.class, multiple=true), @EnableDubboConfigBinding(prefix="dubbo.registries", type=RegistryConfig.class, multiple=true), @EnableDubboConfigBinding(prefix="dubbo.protocols", type=ProtocolConfig.class, multiple=true), @EnableDubboConfigBinding(prefix="dubbo.monitors", type=MonitorConfig.class, multiple=true), @EnableDubboConfigBinding(prefix="dubbo.providers", type=ProviderConfig.class, multiple=true), @EnableDubboConfigBinding(prefix="dubbo.consumers", type=ConsumerConfig.class, multiple=true)})
    public static class Multiple {
    }

    @EnableDubboConfigBindings(value={@EnableDubboConfigBinding(prefix="dubbo.application", type=ApplicationConfig.class), @EnableDubboConfigBinding(prefix="dubbo.module", type=ModuleConfig.class), @EnableDubboConfigBinding(prefix="dubbo.registry", type=RegistryConfig.class), @EnableDubboConfigBinding(prefix="dubbo.protocol", type=ProtocolConfig.class), @EnableDubboConfigBinding(prefix="dubbo.monitor", type=MonitorConfig.class), @EnableDubboConfigBinding(prefix="dubbo.provider", type=ProviderConfig.class), @EnableDubboConfigBinding(prefix="dubbo.consumer", type=ConsumerConfig.class)})
    public static class Single {
    }

}

