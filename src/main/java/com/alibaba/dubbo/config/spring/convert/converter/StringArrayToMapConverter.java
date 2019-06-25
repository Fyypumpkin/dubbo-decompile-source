/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.converter.Converter
 *  org.springframework.util.ObjectUtils
 */
package com.alibaba.dubbo.config.spring.convert.converter;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.ObjectUtils;

public class StringArrayToMapConverter
implements Converter<String[], Map<String, String>> {
    public Map<String, String> convert(String[] source) {
        return ObjectUtils.isEmpty((Object[])source) ? null : CollectionUtils.toStringMap(source);
    }
}

