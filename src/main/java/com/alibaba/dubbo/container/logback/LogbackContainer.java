/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  ch.qos.logback.classic.Level
 *  ch.qos.logback.classic.Logger
 *  ch.qos.logback.classic.LoggerContext
 *  ch.qos.logback.classic.encoder.PatternLayoutEncoder
 *  ch.qos.logback.core.Appender
 *  ch.qos.logback.core.Context
 *  ch.qos.logback.core.FileAppender
 *  ch.qos.logback.core.encoder.Encoder
 *  ch.qos.logback.core.rolling.RollingFileAppender
 *  ch.qos.logback.core.rolling.RollingPolicy
 *  ch.qos.logback.core.rolling.TimeBasedRollingPolicy
 *  org.slf4j.LoggerFactory
 */
package com.alibaba.dubbo.container.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.container.Container;
import org.slf4j.LoggerFactory;

public class LogbackContainer
implements Container {
    public static final String LOGBACK_FILE = "dubbo.logback.file";
    public static final String LOGBACK_LEVEL = "dubbo.logback.level";
    public static final String LOGBACK_MAX_HISTORY = "dubbo.logback.maxhistory";
    public static final String DEFAULT_LOGBACK_LEVEL = "ERROR";

    @Override
    public void start() {
        String file = ConfigUtils.getProperty(LOGBACK_FILE);
        if (file != null && file.length() > 0) {
            String level = ConfigUtils.getProperty(LOGBACK_LEVEL);
            if (level == null || level.length() == 0) {
                level = DEFAULT_LOGBACK_LEVEL;
            }
            int maxHistory = StringUtils.parseInteger(ConfigUtils.getProperty(LOGBACK_MAX_HISTORY));
            this.doInitializer(file, level, maxHistory);
        }
    }

    @Override
    public void stop() {
    }

    private void doInitializer(String file, String level, int maxHistory) {
        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("ROOT");
        rootLogger.detachAndStopAllAppenders();
        RollingFileAppender fileAppender = new RollingFileAppender();
        fileAppender.setContext((Context)loggerContext);
        fileAppender.setName("application");
        fileAppender.setFile(file);
        fileAppender.setAppend(true);
        TimeBasedRollingPolicy policy = new TimeBasedRollingPolicy();
        policy.setContext((Context)loggerContext);
        policy.setMaxHistory(maxHistory);
        policy.setFileNamePattern(file + ".%d{yyyy-MM-dd}");
        policy.setParent((FileAppender)fileAppender);
        policy.start();
        fileAppender.setRollingPolicy((RollingPolicy)policy);
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext((Context)loggerContext);
        encoder.setPattern("%date [%thread] %-5level %logger (%file:%line\\) - %msg%n");
        encoder.start();
        fileAppender.setEncoder((Encoder)encoder);
        fileAppender.start();
        rootLogger.addAppender((Appender)fileAppender);
        rootLogger.setLevel(Level.toLevel((String)level));
        rootLogger.setAdditive(false);
    }
}

