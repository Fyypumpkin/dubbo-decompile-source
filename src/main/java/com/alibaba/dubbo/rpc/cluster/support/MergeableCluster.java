/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.support.MergeableClusterInvoker;

public class MergeableCluster
implements Cluster {
    public static final String NAME = "mergeable";

    @Override
    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return new MergeableClusterInvoker<T>(directory);
    }
}

