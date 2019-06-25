/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.curator.CuratorZookeeperClient
 *  org.apache.curator.RetryPolicy
 *  org.apache.curator.framework.CuratorFramework
 *  org.apache.curator.framework.CuratorFrameworkFactory
 *  org.apache.curator.framework.CuratorFrameworkFactory$Builder
 *  org.apache.curator.framework.api.ACLBackgroundPathAndBytesable
 *  org.apache.curator.framework.api.BackgroundPathable
 *  org.apache.curator.framework.api.CreateBuilder
 *  org.apache.curator.framework.api.CuratorWatcher
 *  org.apache.curator.framework.api.DeleteBuilder
 *  org.apache.curator.framework.api.GetChildrenBuilder
 *  org.apache.curator.framework.listen.Listenable
 *  org.apache.curator.framework.state.ConnectionState
 *  org.apache.curator.framework.state.ConnectionStateListener
 *  org.apache.curator.retry.RetryNTimes
 *  org.apache.zookeeper.CreateMode
 *  org.apache.zookeeper.KeeperException
 *  org.apache.zookeeper.KeeperException$NoNodeException
 *  org.apache.zookeeper.KeeperException$NodeExistsException
 *  org.apache.zookeeper.WatchedEvent
 */
package com.alibaba.dubbo.remoting.zookeeper.curator;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.zookeeper.ChildListener;
import com.alibaba.dubbo.remoting.zookeeper.support.AbstractZookeeperClient;
import java.util.List;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.framework.api.BackgroundPathable;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

public class CuratorZookeeperClient
extends AbstractZookeeperClient<CuratorWatcher> {
    private final CuratorFramework client;

    public CuratorZookeeperClient(URL url) {
        super(url);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString(url.getBackupAddress()).retryPolicy((RetryPolicy)new RetryNTimes(Integer.MAX_VALUE, 1000)).connectionTimeoutMs(url.getParameter("timeout", 5000)).sessionTimeoutMs(url.getParameter("session", 60000));
        String authority = url.getAuthority();
        if (authority != null && authority.length() > 0) {
            builder = builder.authorization("digest", authority.getBytes());
        }
        this.client = builder.build();
        this.client.getConnectionStateListenable().addListener((Object)new ConnectionStateListener(){

            public void stateChanged(CuratorFramework client, ConnectionState state) {
                if (state == ConnectionState.LOST) {
                    CuratorZookeeperClient.this.stateChanged(0);
                } else if (state == ConnectionState.CONNECTED) {
                    CuratorZookeeperClient.this.stateChanged(1);
                } else if (state == ConnectionState.RECONNECTED) {
                    CuratorZookeeperClient.this.stateChanged(2);
                }
            }
        });
        this.client.start();
    }

    @Override
    public void createPersistent(String path) {
        try {
            this.client.create().forPath(path);
        }
        catch (KeeperException.NodeExistsException nodeExistsException) {
        }
        catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void createEphemeral(String path) {
        try {
            ((ACLBackgroundPathAndBytesable)this.client.create().withMode(CreateMode.EPHEMERAL)).forPath(path);
        }
        catch (KeeperException.NodeExistsException nodeExistsException) {
        }
        catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            this.client.delete().forPath(path);
        }
        catch (KeeperException.NoNodeException noNodeException) {
        }
        catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return (List)this.client.getChildren().forPath(path);
        }
        catch (KeeperException.NoNodeException e) {
            return null;
        }
        catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return this.client.getZookeeperClient().isConnected();
    }

    @Override
    public void doClose() {
        this.client.close();
    }

    @Override
    public CuratorWatcher createTargetChildListener(String path, ChildListener listener) {
        return new CuratorWatcherImpl(listener);
    }

    @Override
    public List<String> addTargetChildListener(String path, CuratorWatcher listener) {
        try {
            return (List)((BackgroundPathable)this.client.getChildren().usingWatcher(listener)).forPath(path);
        }
        catch (KeeperException.NoNodeException e) {
            return null;
        }
        catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void removeTargetChildListener(String path, CuratorWatcher listener) {
        ((CuratorWatcherImpl)listener).unwatch();
    }

    private class CuratorWatcherImpl
    implements CuratorWatcher {
        private volatile ChildListener listener;

        public CuratorWatcherImpl(ChildListener listener) {
            this.listener = listener;
        }

        public void unwatch() {
            this.listener = null;
        }

        public void process(WatchedEvent event) throws Exception {
            if (this.listener != null) {
                this.listener.childChanged(event.getPath(), (List)((BackgroundPathable)CuratorZookeeperClient.this.client.getChildren().usingWatcher((CuratorWatcher)this)).forPath(event.getPath()));
            }
        }
    }

}

