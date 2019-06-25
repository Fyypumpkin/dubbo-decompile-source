/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.support;

import com.alibaba.dubbo.common.URL;

public class ProtocolUtils {
    private ProtocolUtils() {
    }

    public static String serviceKey(URL url) {
        return ProtocolUtils.serviceKey(url.getPort(), url.getPath(), url.getParameter("version"), url.getParameter("group"));
    }

    public static String serviceKey(int port, String serviceName, String serviceVersion, String serviceGroup) {
        StringBuilder buf = new StringBuilder();
        if (serviceGroup != null && serviceGroup.length() > 0) {
            buf.append(serviceGroup);
            buf.append("/");
        }
        buf.append(serviceName);
        if (serviceVersion != null && serviceVersion.length() > 0 && !"0.0.0".equals(serviceVersion)) {
            buf.append(":");
            buf.append(serviceVersion);
        }
        buf.append(":");
        buf.append(port);
        return buf.toString();
    }

    public static boolean isGeneric(String generic) {
        return generic != null && !"".equals(generic) && ("true".equalsIgnoreCase(generic) || "nativejava".equalsIgnoreCase(generic) || "bean".equalsIgnoreCase(generic)) || "result_no_change".equals(generic) || "json".equals(generic);
    }

    public static boolean isDefaultGenericSerialization(String generic) {
        return ProtocolUtils.isGeneric(generic) && "true".equalsIgnoreCase(generic);
    }

    public static boolean isJavaGenericSerialization(String generic) {
        return ProtocolUtils.isGeneric(generic) && "nativejava".equalsIgnoreCase(generic);
    }

    public static boolean isBeanGenericSerialization(String generic) {
        return ProtocolUtils.isGeneric(generic) && "bean".equals(generic);
    }

    public static boolean isResultNoChangeGenericSerialization(String generic) {
        return ProtocolUtils.isGeneric(generic) && "result_no_change".equals(generic);
    }

    public static boolean isJsonResultGenericSerialization(String generic) {
        return ProtocolUtils.isGeneric(generic) && "json".equals(generic);
    }
}

