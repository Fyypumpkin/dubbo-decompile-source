/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.router.file;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.IOUtils;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.RouterFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileRouterFactory
implements RouterFactory {
    public static final String NAME = "file";
    private RouterFactory routerFactory;

    public void setRouterFactory(RouterFactory routerFactory) {
        this.routerFactory = routerFactory;
    }

    @Override
    public Router getRouter(URL url) {
        try {
            int i;
            String protocol = url.getParameter("router", "script");
            String type = null;
            String path = url.getPath();
            if (path != null && (i = path.lastIndexOf(46)) > 0) {
                type = path.substring(i + 1);
            }
            String rule = IOUtils.read(new FileReader(new File(url.getAbsolutePath())));
            URL script = url.setProtocol(protocol).addParameter("type", type).addParameterAndEncoded("rule", rule);
            return this.routerFactory.getRouter(script);
        }
        catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}

