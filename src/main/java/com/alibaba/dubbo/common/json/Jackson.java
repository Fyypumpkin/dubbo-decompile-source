/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.DeserializationFeature
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  com.fasterxml.jackson.databind.SerializationFeature
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.json.JacksonObjectMapperProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Jackson {
    private static Logger logger = LoggerFactory.getLogger(Jackson.class);
    private static ObjectMapper objectMapper;

    private static JacksonObjectMapperProvider getJacksonProvider() {
        return ExtensionLoader.getExtensionLoader(JacksonObjectMapperProvider.class).getDefaultExtension();
    }

    public static ObjectMapper getObjectMapper() {
        JacksonObjectMapperProvider jacksonObjectMapperProvider;
        if (objectMapper == null && (jacksonObjectMapperProvider = Jackson.getJacksonProvider()) != null) {
            objectMapper = jacksonObjectMapperProvider.getObjectMapper();
        }
        if (objectMapper == null) {
            logger.warn("load objectMapper failed, use default config.");
            Jackson.buildDefaultObjectMapper();
        }
        return objectMapper;
    }

    private static synchronized void buildDefaultObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setTimeZone(TimeZone.getDefault());
    }
}

