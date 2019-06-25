/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Level
 *  org.apache.log4j.Logger
 *  org.apache.log4j.Priority
 */
package com.alibaba.dubbo.common.logger.log4j;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.support.FailsafeLogger;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;

public class Log4jLogger
implements Logger {
    private static final String FQCN = FailsafeLogger.class.getName();
    private final org.apache.log4j.Logger logger;

    public Log4jLogger(org.apache.log4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void trace(String msg) {
        this.logger.log(FQCN, (Priority)Level.TRACE, (Object)msg, null);
    }

    @Override
    public void trace(Throwable e) {
        this.logger.log(FQCN, (Priority)Level.TRACE, (Object)(e == null ? null : e.getMessage()), e);
    }

    @Override
    public void trace(String msg, Throwable e) {
        this.logger.log(FQCN, (Priority)Level.TRACE, (Object)msg, e);
    }

    @Override
    public void debug(String msg) {
        this.logger.log(FQCN, (Priority)Level.DEBUG, (Object)msg, null);
    }

    @Override
    public void debug(Throwable e) {
        this.logger.log(FQCN, (Priority)Level.DEBUG, (Object)(e == null ? null : e.getMessage()), e);
    }

    @Override
    public void debug(String msg, Throwable e) {
        this.logger.log(FQCN, (Priority)Level.DEBUG, (Object)msg, e);
    }

    @Override
    public void info(String msg) {
        this.logger.log(FQCN, (Priority)Level.INFO, (Object)msg, null);
    }

    @Override
    public void info(Throwable e) {
        this.logger.log(FQCN, (Priority)Level.INFO, (Object)(e == null ? null : e.getMessage()), e);
    }

    @Override
    public void info(String msg, Throwable e) {
        this.logger.log(FQCN, (Priority)Level.INFO, (Object)msg, e);
    }

    @Override
    public void warn(String msg) {
        this.logger.log(FQCN, (Priority)Level.WARN, (Object)msg, null);
    }

    @Override
    public void warn(Throwable e) {
        this.logger.log(FQCN, (Priority)Level.WARN, (Object)(e == null ? null : e.getMessage()), e);
    }

    @Override
    public void warn(String msg, Throwable e) {
        this.logger.log(FQCN, (Priority)Level.WARN, (Object)msg, e);
    }

    @Override
    public void error(String msg) {
        this.logger.log(FQCN, (Priority)Level.ERROR, (Object)msg, null);
    }

    @Override
    public void error(Throwable e) {
        this.logger.log(FQCN, (Priority)Level.ERROR, (Object)(e == null ? null : e.getMessage()), e);
    }

    @Override
    public void error(String msg, Throwable e) {
        this.logger.log(FQCN, (Priority)Level.ERROR, (Object)msg, e);
    }

    @Override
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return this.logger.isEnabledFor((Priority)Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return this.logger.isEnabledFor((Priority)Level.ERROR);
    }
}

