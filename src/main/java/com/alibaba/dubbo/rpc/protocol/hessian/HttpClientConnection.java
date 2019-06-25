/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.caucho.hessian.client.HessianConnection
 *  org.apache.http.Header
 *  org.apache.http.HttpEntity
 *  org.apache.http.HttpResponse
 *  org.apache.http.StatusLine
 *  org.apache.http.client.HttpClient
 *  org.apache.http.client.methods.HttpPost
 *  org.apache.http.client.methods.HttpUriRequest
 *  org.apache.http.entity.ByteArrayEntity
 *  org.apache.http.message.BasicHeader
 */
package com.alibaba.dubbo.rpc.protocol.hessian;

import com.caucho.hessian.client.HessianConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;

public class HttpClientConnection
implements HessianConnection {
    private final HttpClient httpClient;
    private final ByteArrayOutputStream output;
    private final HttpPost request;
    private volatile HttpResponse response;

    public HttpClientConnection(HttpClient httpClient, URL url) {
        this.httpClient = httpClient;
        this.output = new ByteArrayOutputStream();
        this.request = new HttpPost(url.toString());
    }

    public void addHeader(String key, String value) {
        this.request.addHeader((Header)new BasicHeader(key, value));
    }

    public OutputStream getOutputStream() throws IOException {
        return this.output;
    }

    public void sendRequest() throws IOException {
        this.request.setEntity((HttpEntity)new ByteArrayEntity(this.output.toByteArray()));
        this.response = this.httpClient.execute((HttpUriRequest)this.request);
    }

    public int getStatusCode() {
        return this.response == null || this.response.getStatusLine() == null ? 0 : this.response.getStatusLine().getStatusCode();
    }

    public String getStatusMessage() {
        return this.response == null || this.response.getStatusLine() == null ? null : this.response.getStatusLine().getReasonPhrase();
    }

    public InputStream getInputStream() throws IOException {
        return this.response == null || this.response.getEntity() == null ? null : this.response.getEntity().getContent();
    }

    public void close() throws IOException {
        HttpPost request = this.request;
        if (request != null) {
            request.abort();
        }
    }

    public void destroy() throws IOException {
    }
}

