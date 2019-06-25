/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.event.ApplicationContextEvent
 */
package com.alibaba.dubbo.config.spring.initializer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

public class DubboOnlineOfflineEvent
extends ApplicationContextEvent {
    private String type;

    public DubboOnlineOfflineEvent(String type, ApplicationContext source) {
        super(source);
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}

