/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.context.ApplicationEvent
 *  org.springframework.context.ApplicationListener
 *  org.springframework.context.event.ContextClosedEvent
 *  org.springframework.core.Ordered
 */
package com.alibaba.dubbo.config.spring.initializer;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.container.ContainerStatus;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;

public class DubboApplicationListener
implements ApplicationListener<ApplicationEvent>,
Ordered {
    private static final Logger logger = LoggerFactory.getLogger(DubboApplicationListener.class);

    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextClosedEvent) {
            this.destroyDubboResource();
        }
        this.destroyDubboResource0(applicationEvent);
    }

    private void destroyDubboResource0(ApplicationEvent applicationEvent) {
        try {
            Class<?> failedEvent = Class.forName("org.springframework.boot.context.event.ApplicationFailedEvent");
            if (failedEvent != null && applicationEvent.getClass().isAssignableFrom(failedEvent)) {
                this.destroyDubboResource();
            }
        }
        catch (Exception failedEvent) {
            // empty catch block
        }
    }

    private void destroyDubboResource() {
        this.destroy();
        ContainerStatus.protocolEnd();
    }

    private void destroy() {
        logger.info("destroy dubbo resource.");
        ProtocolConfig.destroyAll();
    }

    public int getOrder() {
        return -2147483548;
    }
}

