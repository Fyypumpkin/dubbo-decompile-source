/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.annotation.Priority
 *  javax.ws.rs.WebApplicationException
 *  javax.ws.rs.client.ClientRequestContext
 *  javax.ws.rs.client.ClientRequestFilter
 *  javax.ws.rs.client.ClientResponseContext
 *  javax.ws.rs.client.ClientResponseFilter
 *  javax.ws.rs.container.ContainerRequestContext
 *  javax.ws.rs.container.ContainerRequestFilter
 *  javax.ws.rs.container.ContainerResponseContext
 *  javax.ws.rs.container.ContainerResponseFilter
 *  javax.ws.rs.core.MultivaluedMap
 *  javax.ws.rs.ext.ReaderInterceptor
 *  javax.ws.rs.ext.ReaderInterceptorContext
 *  javax.ws.rs.ext.WriterInterceptor
 *  javax.ws.rs.ext.WriterInterceptorContext
 *  org.apache.commons.io.IOUtils
 */
package com.alibaba.dubbo.rpc.protocol.rest.support;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.apache.commons.io.IOUtils;

@Priority(value=Integer.MIN_VALUE)
public class LoggingFilter
implements ContainerRequestFilter,
ClientRequestFilter,
ContainerResponseFilter,
ClientResponseFilter,
WriterInterceptor,
ReaderInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    public void filter(ClientRequestContext context) throws IOException {
        this.logHttpHeaders((MultivaluedMap<String, String>)context.getStringHeaders());
    }

    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        this.logHttpHeaders((MultivaluedMap<String, String>)responseContext.getHeaders());
    }

    public void filter(ContainerRequestContext context) throws IOException {
        this.logHttpHeaders((MultivaluedMap<String, String>)context.getHeaders());
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        this.logHttpHeaders((MultivaluedMap<String, String>)responseContext.getStringHeaders());
    }

    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        byte[] buffer = IOUtils.toByteArray((InputStream)context.getInputStream());
        logger.info("The contents of request body is: \n" + new String(buffer, "UTF-8") + "\n");
        context.setInputStream((InputStream)new ByteArrayInputStream(buffer));
        return context.proceed();
    }

    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        OutputStreamWrapper wrapper = new OutputStreamWrapper(context.getOutputStream());
        context.setOutputStream((OutputStream)wrapper);
        context.proceed();
        logger.info("The contents of response body is: \n" + new String(wrapper.getBytes(), "UTF-8") + "\n");
    }

    protected void logHttpHeaders(MultivaluedMap<String, String> headers) {
        StringBuilder msg = new StringBuilder("The HTTP headers are: \n");
        for (Map.Entry entry : headers.entrySet()) {
            msg.append((String)entry.getKey()).append(": ");
            for (int i = 0; i < ((List)entry.getValue()).size(); ++i) {
                msg.append((String)((List)entry.getValue()).get(i));
                if (i >= ((List)entry.getValue()).size() - 1) continue;
                msg.append(", ");
            }
            msg.append("\n");
        }
        logger.info(msg.toString());
    }

    protected static class OutputStreamWrapper
    extends OutputStream {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final OutputStream output;

        private OutputStreamWrapper(OutputStream output) {
            this.output = output;
        }

        @Override
        public void write(int i) throws IOException {
            this.buffer.write(i);
            this.output.write(i);
        }

        @Override
        public void write(byte[] b) throws IOException {
            this.buffer.write(b);
            this.output.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            this.buffer.write(b, off, len);
            this.output.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            this.output.flush();
        }

        @Override
        public void close() throws IOException {
            this.output.close();
        }

        public byte[] getBytes() {
            return this.buffer.toByteArray();
        }
    }

}

