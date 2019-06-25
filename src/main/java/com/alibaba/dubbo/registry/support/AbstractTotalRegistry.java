/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.TotalRegistry;
import com.alibaba.dubbo.registry.support.AbstractRegistry;
import java.util.List;

public abstract class AbstractTotalRegistry
extends AbstractRegistry
implements TotalRegistry {
    public AbstractTotalRegistry(URL url) {
        super(url);
    }

    @Override
    public void register(URL url) {
        super.register(url);
        this.doRegister(url);
    }

    @Override
    public void totalRegister(List<URL> urls) {
        this.doTotalRegister(urls);
    }

    @Override
    public void unregister(URL url) {
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
    }

    @Override
    public void totalUnRegister() {
    }

    @Override
    public List<URL> lookup(URL url) {
        return null;
    }

    protected abstract void doRegister(URL var1);

    protected abstract void doTotalRegister(List<URL> var1);
}

