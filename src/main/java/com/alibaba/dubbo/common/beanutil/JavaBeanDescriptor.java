/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.beanutil;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class JavaBeanDescriptor
implements Serializable,
Iterable<Map.Entry<Object, Object>> {
    private static final long serialVersionUID = -8505586483570518029L;
    public static final int TYPE_CLASS = 1;
    public static final int TYPE_ENUM = 2;
    public static final int TYPE_COLLECTION = 3;
    public static final int TYPE_MAP = 4;
    public static final int TYPE_ARRAY = 5;
    public static final int TYPE_PRIMITIVE = 6;
    public static final int TYPE_BEAN = 7;
    private static final String ENUM_PROPERTY_NAME = "name";
    private static final String CLASS_PROPERTY_NAME = "name";
    private static final String PRIMITIVE_PROPERTY_VALUE = "value";
    private static final int TYPE_MAX = 7;
    private static final int TYPE_MIN = 1;
    private String className;
    private int type;
    private Map<Object, Object> properties = new LinkedHashMap<Object, Object>();

    public JavaBeanDescriptor() {
    }

    public JavaBeanDescriptor(String className, int type) {
        this.notEmpty(className, "class name is empty");
        if (!this.isValidType(type)) {
            throw new IllegalArgumentException(new StringBuilder(16).append("type [ ").append(type).append(" ] is unsupported").toString());
        }
        this.className = className;
        this.type = type;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isClassType() {
        return 1 == this.type;
    }

    public boolean isEnumType() {
        return 2 == this.type;
    }

    public boolean isCollectionType() {
        return 3 == this.type;
    }

    public boolean isMapType() {
        return 4 == this.type;
    }

    public boolean isArrayType() {
        return 5 == this.type;
    }

    public boolean isPrimitiveType() {
        return 6 == this.type;
    }

    public boolean isBeanType() {
        return 7 == this.type;
    }

    public int getType() {
        return this.type;
    }

    public String getClassName() {
        return this.className;
    }

    public Object setProperty(Object propertyName, Object propertyValue) {
        this.notNull(propertyName, "Property name is null");
        Object oldValue = this.properties.put(propertyName, propertyValue);
        return oldValue;
    }

    public String setEnumNameProperty(String name) {
        if (this.isEnumType()) {
            Object result = this.setProperty("name", name);
            return result == null ? null : result.toString();
        }
        throw new IllegalStateException("The instance is not a enum wrapper");
    }

    public String getEnumPropertyName() {
        if (this.isEnumType()) {
            String result = this.getProperty("name").toString();
            return result == null ? null : result.toString();
        }
        throw new IllegalStateException("The instance is not a enum wrapper");
    }

    public String setClassNameProperty(String name) {
        if (this.isClassType()) {
            Object result = this.setProperty("name", name);
            return result == null ? null : result.toString();
        }
        throw new IllegalStateException("The instance is not a class wrapper");
    }

    public String getClassNameProperty() {
        if (this.isClassType()) {
            Object result = this.getProperty("name");
            return result == null ? null : result.toString();
        }
        throw new IllegalStateException("The instance is not a class wrapper");
    }

    public Object setPrimitiveProperty(Object primitiveValue) {
        if (this.isPrimitiveType()) {
            return this.setProperty(PRIMITIVE_PROPERTY_VALUE, primitiveValue);
        }
        throw new IllegalStateException("The instance is not a primitive type wrapper");
    }

    public Object getPrimitiveProperty() {
        if (this.isPrimitiveType()) {
            return this.getProperty(PRIMITIVE_PROPERTY_VALUE);
        }
        throw new IllegalStateException("The instance is not a primitive type wrapper");
    }

    public Object getProperty(Object propertyName) {
        this.notNull(propertyName, "Property name is null");
        Object propertyValue = this.properties.get(propertyName);
        return propertyValue;
    }

    public boolean containsProperty(Object propertyName) {
        this.notNull(propertyName, "Property name is null");
        return this.properties.containsKey(propertyName);
    }

    @Override
    public Iterator<Map.Entry<Object, Object>> iterator() {
        return this.properties.entrySet().iterator();
    }

    public int propertySize() {
        return this.properties.size();
    }

    private boolean isValidType(int type) {
        return 1 <= type && type <= 7;
    }

    private void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void notEmpty(String string, String message) {
        if (this.isEmpty(string)) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean isEmpty(String string) {
        return string == null || "".equals(string.trim());
    }
}

