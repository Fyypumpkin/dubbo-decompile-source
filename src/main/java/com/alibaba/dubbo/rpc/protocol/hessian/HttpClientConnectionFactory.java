/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.caucho.hessian.client.HessianConnection
 *  com.caucho.hessian.client.HessianConnectionFactory
 *  com.caucho.hessian.client.HessianProxyFactory
 *  org.apache.http.client.HttpClient
 *  org.apache.http.impl.client.DefaultHttpClient
 *  org.apache.http.params.HttpConnectionParams
 *  org.apache.http.params.HttpParams
 */
package com.alibaba.dubbo.rpc.protocol.hessian;

import com.alibaba.dubbo.rpc.protocol.hessian.HttpClientConnection;
import com.caucho.hessian.client.HessianConnection;
import com.caucho.hessian.client.HessianConnectionFactory;
import com.caucho.hessian.client.HessianProxyFactory;
import java.io.IOException;
import java.net.URL;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpClientConnectionFactory
implements HessianConnectionFactory {
    private final HttpClient httpClient = new DefaultHttpClient();

    public void setHessianProxyFactory(HessianProxyFactory factory) {
        HttpConnectionParams.setConnectionTimeout((HttpParams)this.httpClient.getParams(), (int)((int)factory.getConnectTimeout()));
        HttpConnectionParams.setSoTimeout((HttpParams)this.httpClient.getParams(), (int)((int)factory.getReadTimeout()));
    }

    public HessianConnection open(URL url) throws IOException {
        return new HttpClientConnection(this.httpClient, url);
    }
}

