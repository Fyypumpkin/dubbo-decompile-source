/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.validation.ConstraintViolation
 *  javax.validation.ConstraintViolationException
 *  javax.validation.Path
 *  javax.ws.rs.core.Response
 *  javax.ws.rs.core.Response$ResponseBuilder
 *  javax.ws.rs.core.Response$Status
 *  javax.ws.rs.ext.ExceptionMapper
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.rest.RestConstraintViolation;
import com.alibaba.dubbo.rpc.protocol.rest.ViolationReport;
import java.util.Iterator;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RpcExceptionMapper
implements ExceptionMapper<RpcException> {
    public Response toResponse(RpcException e) {
        if (e.getCause() instanceof ConstraintViolationException) {
            return this.handleConstraintViolationException((ConstraintViolationException)e.getCause());
        }
        if (e.isTpsLimit()) {
            return Response.status((int)429).entity((Object)("Too Many Requests: " + e.getMessage())).type("text/plain; charset=UTF-8").build();
        }
        return Response.status((Response.Status)Response.Status.INTERNAL_SERVER_ERROR).entity((Object)("Internal server error: " + e.getMessage())).type("text/plain; charset=UTF-8").build();
    }

    protected Response handleConstraintViolationException(ConstraintViolationException cve) {
        ViolationReport report = new ViolationReport();
        Iterator iterator = cve.getConstraintViolations().iterator();
        while (iterator.hasNext()) {
            ConstraintViolation cv;
            report.addConstraintViolation(new RestConstraintViolation(cv.getPropertyPath().toString(), cv.getMessage(), (cv = (ConstraintViolation)iterator.next()).getInvalidValue() == null ? "null" : cv.getInvalidValue().toString()));
        }
        return Response.status((Response.Status)Response.Status.INTERNAL_SERVER_ERROR).entity((Object)report).type("text/xml; charset=UTF-8").build();
    }
}

