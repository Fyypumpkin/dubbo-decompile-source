/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.extension.SPI;
import com.fasterxml.jackson.databind.ObjectMapper;

@SPI(value="jackson")
public interface JacksonObjectMapperProvider {
    public ObjectMapper getObjectMapper();
}

