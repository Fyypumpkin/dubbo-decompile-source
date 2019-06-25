/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.servlet.Filter
 *  javax.servlet.FilterChain
 *  javax.servlet.FilterConfig
 *  javax.servlet.ServletException
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.alibaba.dubbo.container.page;

import com.alibaba.dubbo.common.Constants;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResourceFilter
implements Filter {
    private static final String CLASSPATH_PREFIX = "classpath:";
    private final long start = System.currentTimeMillis();
    private final List<String> resources = new ArrayList<String>();

    public void init(FilterConfig filterConfig) throws ServletException {
        String config = filterConfig.getInitParameter("resources");
        if (config != null && config.length() > 0) {
            String[] configs;
            for (String c : configs = Constants.COMMA_SPLIT_PATTERN.split(config)) {
                if (c == null || c.length() <= 0) continue;
                if ((c = c.replace('\\', '/')).endsWith("/")) {
                    c = c.substring(0, c.length() - 1);
                }
                this.resources.add(c);
            }
        }
    }

    public void destroy() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        byte[] data;
        HttpServletResponse response;
        long lastModified;
        ByteArrayOutputStream output;
        HttpServletRequest request = (HttpServletRequest)req;
        response = (HttpServletResponse)res;
        if (response.isCommitted()) {
            return;
        }
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (uri.endsWith("/favicon.ico")) {
            uri = "/favicon.ico";
        } else if (context != null && !"/".equals(context)) {
            uri = uri.substring(context.length());
        }
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }
        lastModified = this.getLastModified(uri);
        long since = request.getDateHeader("If-Modified-Since");
        if (since >= lastModified) {
            response.sendError(304);
            return;
        }
        InputStream input = this.getInputStream(uri);
        if (input == null) {
            chain.doFilter(req, res);
            return;
        }
        try {
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            data = output.toByteArray();
        }
        finally {
            input.close();
        }
        response.setDateHeader("Last-Modified", lastModified);
        output = response.getOutputStream();
        output.write(data);
        output.flush();
    }

    private boolean isFile(String path) {
        return path.startsWith("/") || path.indexOf(":") <= 1;
    }

    private long getLastModified(String uri) {
        for (String resource : this.resources) {
            String path;
            File file;
            if (resource == null || resource.length() <= 0 || !this.isFile(path = resource + uri) || !(file = new File(path)).exists()) continue;
            return file.lastModified();
        }
        return this.start;
    }

    private InputStream getInputStream(String uri) {
        for (String resource : this.resources) {
            String path = resource + uri;
            try {
                if (this.isFile(path)) {
                    return new FileInputStream(path);
                }
                if (path.startsWith(CLASSPATH_PREFIX)) {
                    return Thread.currentThread().getContextClassLoader().getResourceAsStream(path.substring(CLASSPATH_PREFIX.length()));
                }
                return new URL(path).openStream();
            }
            catch (IOException iOException) {
            }
        }
        return null;
    }
}

