/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StaticContext
extends ConcurrentHashMap<Object, Object> {
    private static final long serialVersionUID = 1L;
    private static final String SYSTEMNAME = "system";
    private String name;
    private static final ConcurrentMap<String, StaticContext> context_map = new ConcurrentHashMap<String, StaticContext>();

    private StaticContext(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static StaticContext getSystemContext() {
        return StaticContext.getContext(SYSTEMNAME);
    }

    public static StaticContext getContext(String name) {
        StaticContext appContext = (StaticContext)context_map.get(name);
        if (appContext == null && (appContext = context_map.putIfAbsent(name, new StaticContext(name))) == null) {
            appContext = (StaticContext)context_map.get(name);
        }
        return appContext;
    }

    public static StaticContext remove(String name) {
        return (StaticContext)context_map.remove(name);
    }

    public static String getKey(URL url, String methodName, String suffix) {
        return StaticContext.getKey(url.getServiceKey(), methodName, suffix);
    }

    public static String getKey(Map<String, String> paras, String methodName, String suffix) {
        return StaticContext.getKey(StringUtils.getServiceKey(paras), methodName, suffix);
    }

    private static String getKey(String servicekey, String methodName, String suffix) {
        StringBuffer sb = new StringBuffer().append(servicekey).append(".").append(methodName).append(".").append(suffix);
        return sb.toString();
    }
}

