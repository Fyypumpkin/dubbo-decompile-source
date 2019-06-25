/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServlet
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.alibaba.dubbo.remoting.http.servlet;

import com.alibaba.dubbo.remoting.http.HttpHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DispatcherServlet
extends HttpServlet {
    private static final long serialVersionUID = 5766349180380479888L;
    private static DispatcherServlet INSTANCE;
    private static final Map<Integer, HttpHandler> handlers;

    public static void addHttpHandler(int port, HttpHandler processor) {
        handlers.put(port, processor);
    }

    public static void removeHttpHandler(int port) {
        handlers.remove(port);
    }

    public static DispatcherServlet getInstance() {
        return INSTANCE;
    }

    public DispatcherServlet() {
        INSTANCE = this;
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpHandler handler = handlers.get(request.getLocalPort());
        if (handler == null) {
            response.sendError(404, "Service not found.");
        } else {
            handler.handle(request, response);
        }
    }

    static {
        handlers = new ConcurrentHashMap<Integer, HttpHandler>();
    }
}

