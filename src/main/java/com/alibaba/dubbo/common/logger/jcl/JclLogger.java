/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.commons.logging.Log
 */
package com.alibaba.dubbo.common.logger.jcl;

import com.alibaba.dubbo.common.logger.Logger;
import java.io.Serializable;
import org.apache.commons.logging.Log;

public class JclLogger
implements Logger,
Serializable {
    private static final long serialVersionUID = 1L;
    private final Log logger;

    public JclLogger(Log logger) {
        this.logger = logger;
    }

    @Override
    public void trace(String msg) {
        this.logger.trace((Object)msg);
    }

    @Override
    public void trace(Throwable e) {
        this.logger.trace((Object)e);
    }

    @Override
    public void trace(String msg, Throwable e) {
        this.logger.trace((Object)msg, e);
    }

    @Override
    public void debug(String msg) {
        this.logger.debug((Object)msg);
    }

    @Override
    public void debug(Throwable e) {
        this.logger.debug((Object)e);
    }

    @Override
    public void debug(String msg, Throwable e) {
        this.logger.debug((Object)msg, e);
    }

    @Override
    public void info(String msg) {
        this.logger.info((Object)msg);
    }

    @Override
    public void info(Throwable e) {
        this.logger.info((Object)e);
    }

    @Override
    public void info(String msg, Throwable e) {
        this.logger.info((Object)msg, e);
    }

    @Override
    public void warn(String msg) {
        this.logger.warn((Object)msg);
    }

    @Override
    public void warn(Throwable e) {
        this.logger.warn((Object)e);
    }

    @Override
    public void warn(String msg, Throwable e) {
        this.logger.warn((Object)msg, e);
    }

    @Override
    public void error(String msg) {
        this.logger.error((Object)msg);
    }

    @Override
    public void error(Throwable e) {
        this.logger.error((Object)e);
    }

    @Override
    public void error(String msg, Throwable e) {
        this.logger.error((Object)msg, e);
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
        return this.logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }
}

