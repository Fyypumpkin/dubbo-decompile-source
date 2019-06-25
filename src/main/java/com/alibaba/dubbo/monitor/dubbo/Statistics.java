/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.monitor.dubbo;

import com.alibaba.dubbo.common.URL;
import java.io.Serializable;

public class Statistics
implements Serializable {
    private static final long serialVersionUID = -6921183057683641441L;
    private URL url;
    private String application;
    private String service;
    private String method;
    private String group;
    private String version;
    private String client;
    private String server;

    public Statistics(URL url) {
        this.url = url;
        this.application = url.getParameter("application");
        this.service = url.getParameter("interface");
        this.method = url.getParameter("method");
        this.group = url.getParameter("group");
        this.version = url.getParameter("version");
        this.client = url.getParameter("consumer", url.getAddress());
        this.server = url.getParameter("provider", url.getAddress());
    }

    public URL getUrl() {
        return this.url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getApplication() {
        return this.application;
    }

    public Statistics setApplication(String application) {
        this.application = application;
        return this;
    }

    public String getService() {
        return this.service;
    }

    public Statistics setService(String service) {
        this.service = service;
        return this;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMethod() {
        return this.method;
    }

    public Statistics setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getClient() {
        return this.client;
    }

    public Statistics setClient(String client) {
        this.client = client;
        return this;
    }

    public String getServer() {
        return this.server;
    }

    public Statistics setServer(String server) {
        this.server = server;
        return this;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.application == null ? 0 : this.application.hashCode());
        result = 31 * result + (this.client == null ? 0 : this.client.hashCode());
        result = 31 * result + (this.group == null ? 0 : this.group.hashCode());
        result = 31 * result + (this.method == null ? 0 : this.method.hashCode());
        result = 31 * result + (this.server == null ? 0 : this.server.hashCode());
        result = 31 * result + (this.service == null ? 0 : this.service.hashCode());
        result = 31 * result + (this.version == null ? 0 : this.version.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Statistics other = (Statistics)obj;
        if (this.application == null ? other.application != null : !this.application.equals(other.application)) {
            return false;
        }
        if (this.client == null ? other.client != null : !this.client.equals(other.client)) {
            return false;
        }
        if (this.group == null ? other.group != null : !this.group.equals(other.group)) {
            return false;
        }
        if (this.method == null ? other.method != null : !this.method.equals(other.method)) {
            return false;
        }
        if (this.server == null ? other.server != null : !this.server.equals(other.server)) {
            return false;
        }
        if (this.service == null ? other.service != null : !this.service.equals(other.service)) {
            return false;
        }
        return !(this.version == null ? other.version != null : !this.version.equals(other.version));
    }

    public String toString() {
        return this.url.toString();
    }
}

