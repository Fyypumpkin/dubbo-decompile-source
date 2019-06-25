/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.configurator;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.cluster.Configurator;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractConfigurator
implements Configurator {
    private final URL configuratorUrl;

    public AbstractConfigurator(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("configurator url == null");
        }
        this.configuratorUrl = url;
    }

    @Override
    public URL getUrl() {
        return this.configuratorUrl;
    }

    @Override
    public URL configure(URL url) {
        if (this.configuratorUrl == null || this.configuratorUrl.getHost() == null || url == null || url.getHost() == null) {
            return url;
        }
        if ("0.0.0.0".equals(this.configuratorUrl.getHost()) || url.getHost().equals(this.configuratorUrl.getHost())) {
            String configApplication = this.configuratorUrl.getParameter("application", this.configuratorUrl.getUsername());
            String currentApplication = url.getParameter("application", url.getUsername());
            if ((configApplication == null || "*".equals(configApplication) || configApplication.equals(currentApplication)) && (this.configuratorUrl.getPort() == 0 || url.getPort() == this.configuratorUrl.getPort())) {
                HashSet<String> condtionKeys = new HashSet<String>();
                condtionKeys.add("category");
                condtionKeys.add("check");
                condtionKeys.add("dynamic");
                condtionKeys.add("enabled");
                for (Map.Entry<String, String> entry : this.configuratorUrl.getParameters().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (!key.startsWith("~") && !"application".equals(key) && !"side".equals(key)) continue;
                    condtionKeys.add(key);
                    if (value == null || "*".equals(value) || value.equals(url.getParameter(key.startsWith("~") ? key.substring(1) : key))) continue;
                    return url;
                }
                return this.doConfigure(url, this.configuratorUrl.removeParameters(condtionKeys));
            }
        }
        return url;
    }

    @Override
    public int compareTo(Configurator o) {
        if (o == null) {
            return -1;
        }
        return this.getUrl().getHost().compareTo(o.getUrl().getHost());
    }

    protected abstract URL doConfigure(URL var1, URL var2);

    public static void main(String[] args) {
        System.out.println(URL.encode("timeout=100"));
    }
}

