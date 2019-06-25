/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.converter.Converter
 *  org.springframework.util.ObjectUtils
 *  org.springframework.util.StringUtils
 */
package com.alibaba.dubbo.config.spring.convert.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class StringArrayToStringConverter
implements Converter<String[], String> {
    public String convert(String[] source) {
        return ObjectUtils.isEmpty((Object[])source) ? null : StringUtils.arrayToCommaDelimitedString((Object[])source);
    }
}

