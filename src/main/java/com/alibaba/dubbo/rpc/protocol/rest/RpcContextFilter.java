/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.annotation.Priority
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.ws.rs.client.ClientRequestContext
 *  javax.ws.rs.client.ClientRequestFilter
 *  javax.ws.rs.container.ContainerRequestContext
 *  javax.ws.rs.container.ContainerRequestFilter
 *  javax.ws.rs.core.MultivaluedMap
 *  org.jboss.resteasy.spi.ResteasyProviderFactory
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.RpcContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

@Priority(value=-2147483647)
public class RpcContextFilter
implements ContainerRequestFilter,
ClientRequestFilter {
    private static final String DUBBO_ATTACHMENT_HEADER = "Dubbo-Attachments";
    private static final int MAX_HEADER_SIZE = 8192;

    public void filter(ContainerRequestContext requestContext) throws IOException {
        HttpServletRequest request = (HttpServletRequest)ResteasyProviderFactory.getContextData(HttpServletRequest.class);
        RpcContext.getContext().setRequest((Object)request);
        if (request != null && RpcContext.getContext().getRemoteAddress() == null) {
            RpcContext.getContext().setRemoteAddress(request.getRemoteAddr(), request.getRemotePort());
        }
        RpcContext.getContext().setResponse(ResteasyProviderFactory.getContextData(HttpServletResponse.class));
        String headers = requestContext.getHeaderString(DUBBO_ATTACHMENT_HEADER);
        if (headers != null) {
            for (String header : headers.split(",")) {
                int index = header.indexOf("=");
                if (index <= 0) continue;
                String key = header.substring(0, index);
                String value = header.substring(index + 1);
                if (StringUtils.isEmpty(key)) continue;
                RpcContext.getContext().setAttachment(key.trim(), value.trim());
            }
        }
    }

    public void filter(ClientRequestContext requestContext) throws IOException {
        int size = 0;
        for (Map.Entry<String, String> entry : RpcContext.getContext().getAttachments().entrySet()) {
            if (entry.getValue().contains(",") || entry.getValue().contains("=") || entry.getKey().contains(",") || entry.getKey().contains("=")) {
                throw new IllegalArgumentException("The attachments of " + RpcContext.class.getSimpleName() + " must not contain ',' or '=' when using rest protocol");
            }
            if ((size += entry.getValue().getBytes("UTF-8").length) > 8192) {
                throw new IllegalArgumentException("The attachments of " + RpcContext.class.getSimpleName() + " is too big");
            }
            StringBuilder attachments = new StringBuilder();
            attachments.append(entry.getKey());
            attachments.append("=");
            attachments.append(entry.getValue());
            requestContext.getHeaders().add((Object)DUBBO_ATTACHMENT_HEADER, (Object)attachments.toString());
        }
    }
}

