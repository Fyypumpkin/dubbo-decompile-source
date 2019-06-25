/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.Lifecycle
 */
package com.alibaba.dubbo.config.spring.status;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.status.Status;
import com.alibaba.dubbo.common.status.StatusChecker;
import com.alibaba.dubbo.config.spring.ServiceBean;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import org.springframework.context.ApplicationContext;
import org.springframework.context.Lifecycle;

@Activate
public class SpringStatusChecker
implements StatusChecker {
    private static final Logger logger = LoggerFactory.getLogger(SpringStatusChecker.class);

    @Override
    public Status check() {
        ApplicationContext context = ServiceBean.getSpringContext();
        if (context == null) {
            return new Status(Status.Level.UNKNOWN);
        }
        Status.Level level = Status.Level.OK;
        level = context instanceof Lifecycle ? (((Lifecycle)context).isRunning() ? Status.Level.OK : Status.Level.ERROR) : Status.Level.UNKNOWN;
        StringBuilder buf = new StringBuilder();
        try {
            Class<?> cls = context.getClass();
            AccessibleObject method = null;
            while (cls != null && method == null) {
                try {
                    method = cls.getDeclaredMethod("getConfigLocations", new Class[0]);
                }
                catch (NoSuchMethodException t) {
                    cls = cls.getSuperclass();
                }
            }
            if (method != null) {
                String[] configs;
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                if ((configs = (String[])((Method)method).invoke((Object)context, new Object[0])) != null && configs.length > 0) {
                    for (String config : configs) {
                        if (buf.length() > 0) {
                            buf.append(",");
                        }
                        buf.append(config);
                    }
                }
            }
        }
        catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
        return new Status(level, buf.toString());
    }
}

