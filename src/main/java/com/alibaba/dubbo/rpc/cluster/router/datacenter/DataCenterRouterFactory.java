/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.router.datacenter;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.RouterFactory;
import com.alibaba.dubbo.rpc.cluster.router.datacenter.DataCenterRouter;

public class DataCenterRouterFactory
implements RouterFactory {
    public static final String NAME = "datacenter";

    @Override
    public Router getRouter(URL url) {
        return new DataCenterRouter();
    }
}

