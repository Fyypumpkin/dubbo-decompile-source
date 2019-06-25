/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import java.util.List;

public interface RegistryService {
    public void register(URL var1);

    public void unregister(URL var1);

    public void subscribe(URL var1, NotifyListener var2);

    public void unsubscribe(URL var1, NotifyListener var2);

    public List<URL> lookup(URL var1);
}

