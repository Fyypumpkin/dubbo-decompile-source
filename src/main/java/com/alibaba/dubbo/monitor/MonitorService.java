/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.monitor;

import com.alibaba.dubbo.common.URL;
import java.util.List;

public interface MonitorService {
    public static final String APPLICATION = "application";
    public static final String INTERFACE = "interface";
    public static final String METHOD = "method";
    public static final String GROUP = "group";
    public static final String VERSION = "version";
    public static final String CONSUMER = "consumer";
    public static final String PROVIDER = "provider";
    public static final String TIMESTAMP = "timestamp";
    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";
    public static final String INPUT = "input";
    public static final String OUTPUT = "output";
    public static final String ELAPSED = "elapsed";
    public static final String CONCURRENT = "concurrent";
    public static final String MAX_INPUT = "max.input";
    public static final String MAX_OUTPUT = "max.output";
    public static final String MAX_ELAPSED = "max.elapsed";
    public static final String MAX_CONCURRENT = "max.concurrent";

    public void collect(URL var1);

    public List<URL> lookup(URL var1);
}

