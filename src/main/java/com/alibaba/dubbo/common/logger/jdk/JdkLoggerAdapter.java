/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.logger.jdk;

import com.alibaba.dubbo.common.logger.Level;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerAdapter;
import com.alibaba.dubbo.common.logger.jdk.JdkLogger;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;

public class JdkLoggerAdapter
implements LoggerAdapter {
    private static final String GLOBAL_LOGGER_NAME = "global";
    private File file;

    public JdkLoggerAdapter() {
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties");
            if (in != null) {
                LogManager.getLogManager().readConfiguration(in);
            } else {
                System.err.println("No such logging.properties in classpath for jdk logging config!");
            }
        }
        catch (Throwable t) {
            System.err.println("Failed to load logging.properties in classpath for jdk logging config, cause: " + t.getMessage());
        }
        try {
            Handler[] handlers;
            for (Handler handler : handlers = java.util.logging.Logger.getLogger(GLOBAL_LOGGER_NAME).getHandlers()) {
                FileHandler fileHandler;
                Field field;
                File[] files;
                if (!(handler instanceof FileHandler) || (files = (File[])(field = (fileHandler = (FileHandler)handler).getClass().getField("files")).get(fileHandler)) == null || files.length <= 0) continue;
                this.file = files[0];
            }
        }
        catch (Throwable handlers) {
            // empty catch block
        }
    }

    @Override
    public Logger getLogger(Class<?> key) {
        return new JdkLogger(java.util.logging.Logger.getLogger(key == null ? "" : key.getName()));
    }

    @Override
    public Logger getLogger(String key) {
        return new JdkLogger(java.util.logging.Logger.getLogger(key));
    }

    @Override
    public void setLevel(Level level) {
        java.util.logging.Logger.getLogger(GLOBAL_LOGGER_NAME).setLevel(JdkLoggerAdapter.toJdkLevel(level));
    }

    @Override
    public Level getLevel() {
        return JdkLoggerAdapter.fromJdkLevel(java.util.logging.Logger.getLogger(GLOBAL_LOGGER_NAME).getLevel());
    }

    @Override
    public File getFile() {
        return this.file;
    }

    private static java.util.logging.Level toJdkLevel(Level level) {
        if (level == Level.ALL) {
            return java.util.logging.Level.ALL;
        }
        if (level == Level.TRACE) {
            return java.util.logging.Level.FINER;
        }
        if (level == Level.DEBUG) {
            return java.util.logging.Level.FINE;
        }
        if (level == Level.INFO) {
            return java.util.logging.Level.INFO;
        }
        if (level == Level.WARN) {
            return java.util.logging.Level.WARNING;
        }
        if (level == Level.ERROR) {
            return java.util.logging.Level.SEVERE;
        }
        return java.util.logging.Level.OFF;
    }

    private static Level fromJdkLevel(java.util.logging.Level level) {
        if (level == java.util.logging.Level.ALL) {
            return Level.ALL;
        }
        if (level == java.util.logging.Level.FINER) {
            return Level.TRACE;
        }
        if (level == java.util.logging.Level.FINE) {
            return Level.DEBUG;
        }
        if (level == java.util.logging.Level.INFO) {
            return Level.INFO;
        }
        if (level == java.util.logging.Level.WARNING) {
            return Level.WARN;
        }
        if (level == java.util.logging.Level.SEVERE) {
            return Level.ERROR;
        }
        return Level.OFF;
    }

    @Override
    public void setFile(File file) {
    }
}

