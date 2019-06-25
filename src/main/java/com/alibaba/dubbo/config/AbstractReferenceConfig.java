/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.config.AbstractInterfaceConfig;
import com.alibaba.dubbo.config.support.Parameter;
import com.alibaba.dubbo.rpc.InvokerListener;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;

public abstract class AbstractReferenceConfig
extends AbstractInterfaceConfig {
    private static final long serialVersionUID = -2786526984373031126L;
    protected Boolean check;
    protected Boolean init;
    protected String generic;
    protected Boolean injvm;
    protected Boolean lazy;
    protected String reconnect;
    protected Boolean sticky;
    protected Boolean stubevent;
    protected String version;
    protected String group;

    public Boolean isCheck() {
        return this.check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public Boolean isInit() {
        return this.init;
    }

    public void setInit(Boolean init) {
        this.init = init;
    }

    @Parameter(excluded=true)
    public Boolean isGeneric() {
        return ProtocolUtils.isGeneric(this.generic);
    }

    public void setGeneric(Boolean generic) {
        if (generic != null) {
            this.generic = generic.toString();
        }
    }

    public String getGeneric() {
        return this.generic;
    }

    public void setGeneric(String generic) {
        this.generic = generic;
    }

    @Deprecated
    public Boolean isInjvm() {
        return this.injvm;
    }

    @Deprecated
    public void setInjvm(Boolean injvm) {
        this.injvm = injvm;
    }

    @Parameter(key="reference.filter", append=true)
    @Override
    public String getFilter() {
        return super.getFilter();
    }

    @Parameter(key="invoker.listener", append=true)
    @Override
    public String getListener() {
        return super.getListener();
    }

    @Override
    public void setListener(String listener) {
        AbstractReferenceConfig.checkMultiExtension(InvokerListener.class, "listener", listener);
        super.setListener(listener);
    }

    @Parameter(key="lazy")
    public Boolean getLazy() {
        return this.lazy;
    }

    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }

    @Override
    public void setOnconnect(String onconnect) {
        if (onconnect != null && onconnect.length() > 0) {
            this.stubevent = true;
        }
        super.setOnconnect(onconnect);
    }

    @Override
    public void setOndisconnect(String ondisconnect) {
        if (ondisconnect != null && ondisconnect.length() > 0) {
            this.stubevent = true;
        }
        super.setOndisconnect(ondisconnect);
    }

    @Parameter(key="dubbo.stub.event")
    public Boolean getStubevent() {
        return this.stubevent;
    }

    @Parameter(key="reconnect")
    public String getReconnect() {
        return this.reconnect;
    }

    public void setReconnect(String reconnect) {
        this.reconnect = reconnect;
    }

    @Parameter(key="sticky")
    public Boolean getSticky() {
        return this.sticky;
    }

    public void setSticky(Boolean sticky) {
        this.sticky = sticky;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        AbstractReferenceConfig.checkKey("version", version);
        this.version = version;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        AbstractReferenceConfig.checkKey("group", group);
        this.group = group;
    }
}

