/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.logger;

import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.common.logger.Level;
import com.alibaba.dubbo.common.logger.Logger;
import java.io.File;

@SPI
public interface LoggerAdapter {
    public Logger getLogger(Class<?> var1);

    public Logger getLogger(String var1);

    public void setLevel(Level var1);

    public Level getLevel();

    public File getFile();

    public void setFile(File var1);
}

