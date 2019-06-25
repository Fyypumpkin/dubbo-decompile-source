/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Appender
 *  org.apache.log4j.FileAppender
 *  org.apache.log4j.Level
 *  org.apache.log4j.LogManager
 *  org.apache.log4j.Logger
 */
package com.alibaba.dubbo.common.logger.log4j;

import com.alibaba.dubbo.common.logger.Level;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerAdapter;
import com.alibaba.dubbo.common.logger.log4j.Log4jLogger;
import java.io.File;
import java.util.Enumeration;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;

public class Log4jLoggerAdapter
implements LoggerAdapter {
    private File file;

    public Log4jLoggerAdapter() {
        block3 : {
            try {
                Enumeration appenders;
                org.apache.log4j.Logger logger = LogManager.getRootLogger();
                if (logger == null || (appenders = logger.getAllAppenders()) == null) break block3;
                while (appenders.hasMoreElements()) {
                    Appender appender = (Appender)appenders.nextElement();
                    if (!(appender instanceof FileAppender)) continue;
                    FileAppender fileAppender = (FileAppender)appender;
                    String filename = fileAppender.getFile();
                    this.file = new File(filename);
                    break;
                }
            }
            catch (Throwable logger) {
                // empty catch block
            }
        }
    }

    @Override
    public Logger getLogger(Class<?> key) {
        return new Log4jLogger(LogManager.getLogger(key));
    }

    @Override
    public Logger getLogger(String key) {
        return new Log4jLogger(LogManager.getLogger((String)key));
    }

    @Override
    public void setLevel(Level level) {
        LogManager.getRootLogger().setLevel(Log4jLoggerAdapter.toLog4jLevel(level));
    }

    @Override
    public Level getLevel() {
        return Log4jLoggerAdapter.fromLog4jLevel(LogManager.getRootLogger().getLevel());
    }

    @Override
    public File getFile() {
        return this.file;
    }

    private static org.apache.log4j.Level toLog4jLevel(Level level) {
        if (level == Level.ALL) {
            return org.apache.log4j.Level.ALL;
        }
        if (level == Level.TRACE) {
            return org.apache.log4j.Level.TRACE;
        }
        if (level == Level.DEBUG) {
            return org.apache.log4j.Level.DEBUG;
        }
        if (level == Level.INFO) {
            return org.apache.log4j.Level.INFO;
        }
        if (level == Level.WARN) {
            return org.apache.log4j.Level.WARN;
        }
        if (level == Level.ERROR) {
            return org.apache.log4j.Level.ERROR;
        }
        return org.apache.log4j.Level.OFF;
    }

    private static Level fromLog4jLevel(org.apache.log4j.Level level) {
        if (level == org.apache.log4j.Level.ALL) {
            return Level.ALL;
        }
        if (level == org.apache.log4j.Level.TRACE) {
            return Level.TRACE;
        }
        if (level == org.apache.log4j.Level.DEBUG) {
            return Level.DEBUG;
        }
        if (level == org.apache.log4j.Level.INFO) {
            return Level.INFO;
        }
        if (level == org.apache.log4j.Level.WARN) {
            return Level.WARN;
        }
        if (level == org.apache.log4j.Level.ERROR) {
            return Level.ERROR;
        }
        return Level.OFF;
    }

    @Override
    public void setFile(File file) {
    }
}

