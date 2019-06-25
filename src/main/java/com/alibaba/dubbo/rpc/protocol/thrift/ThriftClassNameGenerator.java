/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.thrift;

import com.alibaba.dubbo.rpc.protocol.thrift.ClassNameGenerator;
import com.alibaba.dubbo.rpc.protocol.thrift.ThriftUtils;

public class ThriftClassNameGenerator
implements ClassNameGenerator {
    public static final String NAME = "thrift";

    @Override
    public String generateArgsClassName(String serviceName, String methodName) {
        return ThriftUtils.generateMethodArgsClassNameThrift(serviceName, methodName);
    }

    @Override
    public String generateResultClassName(String serviceName, String methodName) {
        return ThriftUtils.generateMethodResultClassNameThrift(serviceName, methodName);
    }
}

