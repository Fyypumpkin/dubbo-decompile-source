/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.logger;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Level;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerAdapter;
import com.alibaba.dubbo.common.logger.jcl.JclLoggerAdapter;
import com.alibaba.dubbo.common.logger.jdk.JdkLoggerAdapter;
import com.alibaba.dubbo.common.logger.log4j.Log4jLoggerAdapter;
import com.alibaba.dubbo.common.logger.slf4j.Slf4jLoggerAdapter;
import com.alibaba.dubbo.common.logger.support.FailsafeLogger;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LoggerFactory {
    private static volatile LoggerAdapter LOGGER_ADAPTER;
    private static final ConcurrentMap<String, FailsafeLogger> LOGGERS;

    private LoggerFactory() {
    }

    public static void setLoggerAdapter(String loggerAdapter) {
        if (loggerAdapter != null && loggerAdapter.length() > 0) {
            LoggerFactory.setLoggerAdapter(ExtensionLoader.getExtensionLoader(LoggerAdapter.class).getExtension(loggerAdapter));
        }
    }

    public static void setLoggerAdapter(LoggerAdapter loggerAdapter) {
        if (loggerAdapter != null) {
            Logger logger = loggerAdapter.getLogger(LoggerFactory.class.getName());
            logger.info("using logger: " + loggerAdapter.getClass().getName());
            LOGGER_ADAPTER = loggerAdapter;
            for (Map.Entry entry : LOGGERS.entrySet()) {
                ((FailsafeLogger)entry.getValue()).setLogger(LOGGER_ADAPTER.getLogger((String)entry.getKey()));
            }
        }
    }

    public static Logger getLogger(Class<?> key) {
        FailsafeLogger logger = (FailsafeLogger)LOGGERS.get(key.getName());
        if (logger == null) {
            LOGGERS.putIfAbsent(key.getName(), new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
            logger = (FailsafeLogger)LOGGERS.get(key.getName());
        }
        return logger;
    }

    public static Logger getLogger(String key) {
        FailsafeLogger logger = (FailsafeLogger)LOGGERS.get(key);
        if (logger == null) {
            LOGGERS.putIfAbsent(key, new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
            logger = (FailsafeLogger)LOGGERS.get(key);
        }
        return logger;
    }

    public static void setLevel(Level level) {
        LOGGER_ADAPTER.setLevel(level);
    }

    public static Level getLevel() {
        return LOGGER_ADAPTER.getLevel();
    }

    public static File getFile() {
        return LOGGER_ADAPTER.getFile();
    }

    static {
        LOGGERS = new ConcurrentHashMap<String, FailsafeLogger>();
        String logger = System.getProperty("dubbo.application.logger");
        if ("slf4j".equals(logger)) {
            LoggerFactory.setLoggerAdapter(new Slf4jLoggerAdapter());
        } else if ("jcl".equals(logger)) {
            LoggerFactory.setLoggerAdapter(new JclLoggerAdapter());
        } else if ("log4j".equals(logger)) {
            LoggerFactory.setLoggerAdapter(new Log4jLoggerAdapter());
        } else if ("jdk".equals(logger)) {
            LoggerFactory.setLoggerAdapter(new JdkLoggerAdapter());
        } else {
            try {
                LoggerFactory.setLoggerAdapter(new Log4jLoggerAdapter());
            }
            catch (Throwable e1) {
                try {
                    LoggerFactory.setLoggerAdapter(new Slf4jLoggerAdapter());
                }
                catch (Throwable e2) {
                    try {
                        LoggerFactory.setLoggerAdapter(new JclLoggerAdapter());
                    }
                    catch (Throwable e3) {
                        LoggerFactory.setLoggerAdapter(new JdkLoggerAdapter());
                    }
                }
            }
        }
    }
}

