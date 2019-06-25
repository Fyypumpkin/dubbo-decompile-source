/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.AnnotationConfigApplicationContext
 */
package com.alibaba.dubbo.container.javaconfig;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.container.Container;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class JavaConfigContainer
implements Container {
    private static final Logger logger = LoggerFactory.getLogger(JavaConfigContainer.class);
    public static final String SPRING_JAVACONFIG = "dubbo.spring.javaconfig";
    public static final String DEFAULT_SPRING_JAVACONFIG = "dubbo.spring.javaconfig";
    static AnnotationConfigApplicationContext context;

    public static AnnotationConfigApplicationContext getContext() {
        return context;
    }

    @Override
    public void start() {
        String configPath = ConfigUtils.getProperty("dubbo.spring.javaconfig");
        if (configPath == null || configPath.length() == 0) {
            configPath = "dubbo.spring.javaconfig";
        }
        context = new AnnotationConfigApplicationContext(new String[]{configPath});
        context.start();
    }

    @Override
    public void stop() {
        try {
            if (context != null) {
                context.stop();
                context.close();
                context = null;
            }
        }
        catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }
}

