/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.p2p.Group;
import com.alibaba.dubbo.remoting.p2p.Networker;
import com.alibaba.dubbo.remoting.p2p.support.FileGroup;

public class FileNetworker
implements Networker {
    @Override
    public Group lookup(URL url) throws RemotingException {
        return new FileGroup(url);
    }
}

