/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.netty.logging.AbstractInternalLogger
 *  org.jboss.netty.logging.InternalLogger
 *  org.jboss.netty.logging.InternalLoggerFactory
 */
package com.alibaba.dubbo.remoting.transport.netty;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import org.jboss.netty.logging.AbstractInternalLogger;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

final class NettyHelper {
    NettyHelper() {
    }

    public static void setNettyLoggerFactory() {
        InternalLoggerFactory factory = InternalLoggerFactory.getDefaultFactory();
        if (factory == null || !(factory instanceof DubboLoggerFactory)) {
            InternalLoggerFactory.setDefaultFactory((InternalLoggerFactory)new DubboLoggerFactory());
        }
    }

    static class DubboLogger
    extends AbstractInternalLogger {
        private Logger logger;

        DubboLogger(Logger logger) {
            this.logger = logger;
        }

        public boolean isDebugEnabled() {
            return this.logger.isDebugEnabled();
        }

        public boolean isInfoEnabled() {
            return this.logger.isInfoEnabled();
        }

        public boolean isWarnEnabled() {
            return this.logger.isWarnEnabled();
        }

        public boolean isErrorEnabled() {
            return this.logger.isErrorEnabled();
        }

        public void debug(String msg) {
            this.logger.debug(msg);
        }

        public void debug(String msg, Throwable cause) {
            this.logger.debug(msg, cause);
        }

        public void info(String msg) {
            this.logger.info(msg);
        }

        public void info(String msg, Throwable cause) {
            this.logger.info(msg, cause);
        }

        public void warn(String msg) {
            this.logger.warn(msg);
        }

        public void warn(String msg, Throwable cause) {
            this.logger.warn(msg, cause);
        }

        public void error(String msg) {
            this.logger.error(msg);
        }

        public void error(String msg, Throwable cause) {
            this.logger.error(msg, cause);
        }

        public String toString() {
            return this.logger.toString();
        }
    }

    static class DubboLoggerFactory
    extends InternalLoggerFactory {
        DubboLoggerFactory() {
        }

        public InternalLogger newInstance(String name) {
            return new DubboLogger(LoggerFactory.getLogger(name));
        }
    }

}

