/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="constraintViolation")
@XmlAccessorType(value=XmlAccessType.FIELD)
public class RestConstraintViolation
implements Serializable {
    private static final long serialVersionUID = -23497234978L;
    private String path;
    private String message;
    private String value;

    public RestConstraintViolation(String path, String message, String value) {
        this.path = path;
        this.message = message;
        this.value = value;
    }

    public RestConstraintViolation() {
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

