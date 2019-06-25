/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.resteasy.spi.HttpRequest
 *  org.jboss.resteasy.spi.HttpResponse
 *  org.jboss.resteasy.spi.ResourceFactory
 *  org.jboss.resteasy.spi.ResteasyProviderFactory
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class DubboResourceFactory
implements ResourceFactory {
    private Object resourceInstance;
    private Class scannableClass;

    public DubboResourceFactory(Object resourceInstance, Class scannableClass) {
        this.resourceInstance = resourceInstance;
        this.scannableClass = scannableClass;
    }

    public Object createResource(HttpRequest request, HttpResponse response, ResteasyProviderFactory factory) {
        return this.resourceInstance;
    }

    public Class<?> getScannableClass() {
        return this.scannableClass;
    }

    public void registered(ResteasyProviderFactory factory) {
    }

    public void requestFinished(HttpRequest request, HttpResponse response, Object resource) {
    }

    public void unregistered() {
    }
}

