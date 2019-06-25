/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.rpc.protocol.rest.RestConstraintViolation;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="violationReport")
@XmlAccessorType(value=XmlAccessType.FIELD)
public class ViolationReport
implements Serializable {
    private static final long serialVersionUID = -130498234L;
    private List<RestConstraintViolation> constraintViolations;

    public List<RestConstraintViolation> getConstraintViolations() {
        return this.constraintViolations;
    }

    public void setConstraintViolations(List<RestConstraintViolation> constraintViolations) {
        this.constraintViolations = constraintViolations;
    }

    public void addConstraintViolation(RestConstraintViolation constraintViolation) {
        if (this.constraintViolations == null) {
            this.constraintViolations = new LinkedList<RestConstraintViolation>();
        }
        this.constraintViolations.add(constraintViolation);
    }
}

