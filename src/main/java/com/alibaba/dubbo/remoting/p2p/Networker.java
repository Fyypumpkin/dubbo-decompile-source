/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.p2p.Group;

@SPI
public interface Networker {
    public Group lookup(URL var1) throws RemotingException;
}

