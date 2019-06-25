/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.MutablePropertyValues
 *  org.springframework.beans.PropertyValue
 *  org.springframework.beans.PropertyValues
 *  org.springframework.core.env.PropertyResolver
 */
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.config.spring.util.AnnotationUtils;
import java.lang.annotation.Annotation;
import java.util.Map;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.core.env.PropertyResolver;

class AnnotationPropertyValuesAdapter
implements PropertyValues {
    private final Annotation annotation;
    private final PropertyResolver propertyResolver;
    private final boolean ignoreDefaultValue;
    private final PropertyValues delegate;

    public /* varargs */ AnnotationPropertyValuesAdapter(Annotation annotation, PropertyResolver propertyResolver, boolean ignoreDefaultValue, String ... ignoreAttributeNames) {
        this.annotation = annotation;
        this.propertyResolver = propertyResolver;
        this.ignoreDefaultValue = ignoreDefaultValue;
        this.delegate = this.adapt(annotation, ignoreDefaultValue, ignoreAttributeNames);
    }

    public /* varargs */ AnnotationPropertyValuesAdapter(Annotation annotation, PropertyResolver propertyResolver, String ... ignoreAttributeNames) {
        this(annotation, propertyResolver, true, ignoreAttributeNames);
    }

    private /* varargs */ PropertyValues adapt(Annotation annotation, boolean ignoreDefaultValue, String ... ignoreAttributeNames) {
        return new MutablePropertyValues(AnnotationUtils.getAttributes(annotation, this.propertyResolver, ignoreDefaultValue, ignoreAttributeNames));
    }

    public Annotation getAnnotation() {
        return this.annotation;
    }

    public boolean isIgnoreDefaultValue() {
        return this.ignoreDefaultValue;
    }

    public PropertyValue[] getPropertyValues() {
        return this.delegate.getPropertyValues();
    }

    public PropertyValue getPropertyValue(String propertyName) {
        return this.delegate.getPropertyValue(propertyName);
    }

    public PropertyValues changesSince(PropertyValues old) {
        return this.delegate.changesSince(old);
    }

    public boolean contains(String propertyName) {
        return this.delegate.contains(propertyName);
    }

    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }
}

