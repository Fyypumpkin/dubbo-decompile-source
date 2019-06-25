/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.alibaba.dubbo.common.logger.slf4j;

import com.alibaba.dubbo.common.logger.Level;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerAdapter;
import com.alibaba.dubbo.common.logger.slf4j.Slf4jLogger;
import java.io.File;
import org.slf4j.LoggerFactory;

public class Slf4jLoggerAdapter
implements LoggerAdapter {
    private Level level;
    private File file;

    @Override
    public Logger getLogger(String key) {
        return new Slf4jLogger(LoggerFactory.getLogger((String)key));
    }

    @Override
    public Logger getLogger(Class<?> key) {
        return new Slf4jLogger(LoggerFactory.getLogger(key));
    }

    @Override
    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }
}

