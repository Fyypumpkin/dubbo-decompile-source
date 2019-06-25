/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.common.URL;

public interface RestServer {
    public void start(URL var1);

    public void deploy(Class var1, Object var2, String var3);

    public void undeploy(Class var1);

    public void stop();
}

