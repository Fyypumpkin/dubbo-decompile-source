/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.remoting.RemoteAccessException
 *  org.springframework.remoting.rmi.RmiProxyFactoryBean
 *  org.springframework.remoting.rmi.RmiServiceExporter
 */
package com.alibaba.dubbo.rpc.protocol.rmi;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.remoting.rmi.RmiServiceExporter;

public class RmiProtocol
extends AbstractProxyProtocol {
    public static final int DEFAULT_PORT = 1099;

    public RmiProtocol() {
        super(RemoteAccessException.class, RemoteException.class);
    }

    @Override
    public int getDefaultPort() {
        return 1099;
    }

    @Override
    protected <T> Runnable doExport(T impl, Class<T> type, URL url) throws RpcException {
        final RmiServiceExporter rmiServiceExporter = new RmiServiceExporter();
        rmiServiceExporter.setRegistryPort(url.getPort());
        rmiServiceExporter.setServiceName(url.getPath());
        rmiServiceExporter.setServiceInterface(type);
        rmiServiceExporter.setService(impl);
        try {
            rmiServiceExporter.afterPropertiesSet();
        }
        catch (RemoteException e) {
            throw new RpcException(e.getMessage(), (Throwable)e);
        }
        return new Runnable(){

            @Override
            public void run() {
                try {
                    rmiServiceExporter.destroy();
                }
                catch (Throwable e) {
                    RmiProtocol.this.logger.warn(e.getMessage(), e);
                }
            }
        };
    }

    @Override
    protected <T> T doRefer(Class<T> serviceType, URL url) throws RpcException {
        RmiProxyFactoryBean rmiProxyFactoryBean = new RmiProxyFactoryBean();
        rmiProxyFactoryBean.setServiceUrl(url.toIdentityString());
        rmiProxyFactoryBean.setServiceInterface(serviceType);
        rmiProxyFactoryBean.setCacheStub(true);
        rmiProxyFactoryBean.setLookupStubOnStartup(true);
        rmiProxyFactoryBean.setRefreshStubOnConnectFailure(true);
        rmiProxyFactoryBean.afterPropertiesSet();
        return (T)rmiProxyFactoryBean.getObject();
    }

    @Override
    protected int getErrorCode(Throwable e) {
        if (e instanceof RemoteAccessException) {
            e = e.getCause();
        }
        if (e != null && e.getCause() != null) {
            Class<?> cls = e.getCause().getClass();
            if (SocketTimeoutException.class.equals(cls)) {
                return 2;
            }
            if (IOException.class.isAssignableFrom(cls)) {
                return 1;
            }
            if (ClassNotFoundException.class.isAssignableFrom(cls)) {
                return 5;
            }
        }
        return super.getErrorCode(e);
    }

}

