/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.core.annotation.AnnotationUtils
 *  org.springframework.core.env.PropertyResolver
 *  org.springframework.util.CollectionUtils
 *  org.springframework.util.ObjectUtils
 *  org.springframework.util.StringUtils
 */
package com.alibaba.dubbo.config.spring.util;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class AnnotationUtils {
    public static /* varargs */ Map<String, Object> getAttributes(Annotation annotation, PropertyResolver propertyResolver, boolean ignoreDefaultValue, String ... ignoreAttributeNames) {
        HashSet ignoreAttributeNamesSet = new HashSet(CollectionUtils.arrayToList((Object)ignoreAttributeNames));
        Map attributes = org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes((Annotation)annotation);
        LinkedHashMap<String, Object> actualAttributes = new LinkedHashMap<String, Object>();
        boolean requiredResolve = propertyResolver != null;
        for (Map.Entry entry : attributes.entrySet()) {
            String attributeName = (String)entry.getKey();
            Object attributeValue = entry.getValue();
            if (ignoreDefaultValue && ObjectUtils.nullSafeEquals(attributeValue, (Object)org.springframework.core.annotation.AnnotationUtils.getDefaultValue((Annotation)annotation, (String)attributeName)) || ignoreAttributeNamesSet.contains(attributeName)) continue;
            if (requiredResolve && attributeValue instanceof String) {
                String resolvedValue = propertyResolver.resolvePlaceholders(String.valueOf(attributeValue));
                attributeValue = StringUtils.trimAllWhitespace((String)resolvedValue);
            }
            actualAttributes.put(attributeName, attributeValue);
        }
        return actualAttributes;
    }
}

