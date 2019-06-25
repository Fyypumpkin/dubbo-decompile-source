/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.logger.support;

import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.utils.NetUtils;

public class FailsafeLogger
implements Logger {
    private Logger logger;

    public FailsafeLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    private String appendContextMessage(String msg) {
        return " [DUBBO] " + msg + ", dubbo version: " + Version.getVersion() + ", current host: " + NetUtils.getLogHost();
    }

    @Override
    public void trace(String msg, Throwable e) {
        try {
            this.logger.trace(this.appendContextMessage(msg), e);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void trace(Throwable e) {
        try {
            this.logger.trace(e);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void trace(String msg) {
        try {
            this.logger.trace(this.appendContextMessage(msg));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void debug(String msg, Throwable e) {
        try {
            this.logger.debug(this.appendContextMessage(msg), e);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void debug(Throwable e) {
        try {
            this.logger.debug(e);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void debug(String msg) {
        try {
            this.logger.debug(this.appendContextMessage(msg));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void info(String msg, Throwable e) {
        try {
            this.logger.info(this.appendContextMessage(msg), e);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void info(String msg) {
        try {
            this.logger.info(this.appendContextMessage(msg));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void warn(String msg, Throwable e) {
        try {
            this.logger.warn(this.appendContextMessage(msg), e);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void warn(String msg) {
        try {
            this.logger.warn(this.appendContextMessage(msg));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void error(String msg, Throwable e) {
        try {
            this.logger.error(this.appendContextMessage(msg), e);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void error(String msg) {
        try {
            this.logger.error(this.appendContextMessage(msg));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void error(Throwable e) {
        try {
            this.logger.error(e);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void info(Throwable e) {
        try {
            this.logger.info(e);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void warn(Throwable e) {
        try {
            this.logger.warn(e);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public boolean isTraceEnabled() {
        try {
            return this.logger.isTraceEnabled();
        }
        catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean isDebugEnabled() {
        try {
            return this.logger.isDebugEnabled();
        }
        catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean isInfoEnabled() {
        try {
            return this.logger.isInfoEnabled();
        }
        catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean isWarnEnabled() {
        try {
            return this.logger.isWarnEnabled();
        }
        catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean isErrorEnabled() {
        try {
            return this.logger.isErrorEnabled();
        }
        catch (Throwable t) {
            return false;
        }
    }
}

