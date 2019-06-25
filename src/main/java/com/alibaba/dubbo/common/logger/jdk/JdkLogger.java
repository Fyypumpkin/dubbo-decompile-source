/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.logger.jdk;

import com.alibaba.dubbo.common.logger.Logger;
import java.util.logging.Level;

public class JdkLogger
implements Logger {
    private final java.util.logging.Logger logger;

    public JdkLogger(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void trace(String msg) {
        this.logger.log(Level.FINER, msg);
    }

    @Override
    public void trace(Throwable e) {
        this.logger.log(Level.FINER, e.getMessage(), e);
    }

    @Override
    public void trace(String msg, Throwable e) {
        this.logger.log(Level.FINER, msg, e);
    }

    @Override
    public void debug(String msg) {
        this.logger.log(Level.FINE, msg);
    }

    @Override
    public void debug(Throwable e) {
        this.logger.log(Level.FINE, e.getMessage(), e);
    }

    @Override
    public void debug(String msg, Throwable e) {
        this.logger.log(Level.FINE, msg, e);
    }

    @Override
    public void info(String msg) {
        this.logger.log(Level.INFO, msg);
    }

    @Override
    public void info(String msg, Throwable e) {
        this.logger.log(Level.INFO, msg, e);
    }

    @Override
    public void warn(String msg) {
        this.logger.log(Level.WARNING, msg);
    }

    @Override
    public void warn(String msg, Throwable e) {
        this.logger.log(Level.WARNING, msg, e);
    }

    @Override
    public void error(String msg) {
        this.logger.log(Level.SEVERE, msg);
    }

    @Override
    public void error(String msg, Throwable e) {
        this.logger.log(Level.SEVERE, msg, e);
    }

    @Override
    public void error(Throwable e) {
        this.logger.log(Level.SEVERE, e.getMessage(), e);
    }

    @Override
    public void info(Throwable e) {
        this.logger.log(Level.INFO, e.getMessage(), e);
    }

    @Override
    public void warn(Throwable e) {
        this.logger.log(Level.WARNING, e.getMessage(), e);
    }

    @Override
    public boolean isTraceEnabled() {
        return this.logger.isLoggable(Level.FINER);
    }

    @Override
    public boolean isDebugEnabled() {
        return this.logger.isLoggable(Level.FINE);
    }

    @Override
    public boolean isInfoEnabled() {
        return this.logger.isLoggable(Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return this.logger.isLoggable(Level.WARNING);
    }

    @Override
    public boolean isErrorEnabled() {
        return this.logger.isLoggable(Level.SEVERE);
    }
}

