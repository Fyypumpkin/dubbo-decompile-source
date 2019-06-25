/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.logger;

public interface Logger {
    public void trace(String var1);

    public void trace(Throwable var1);

    public void trace(String var1, Throwable var2);

    public void debug(String var1);

    public void debug(Throwable var1);

    public void debug(String var1, Throwable var2);

    public void info(String var1);

    public void info(Throwable var1);

    public void info(String var1, Throwable var2);

    public void warn(String var1);

    public void warn(Throwable var1);

    public void warn(String var1, Throwable var2);

    public void error(String var1);

    public void error(Throwable var1);

    public void error(String var1, Throwable var2);

    public boolean isTraceEnabled();

    public boolean isDebugEnabled();

    public boolean isInfoEnabled();

    public boolean isWarnEnabled();

    public boolean isErrorEnabled();
}

