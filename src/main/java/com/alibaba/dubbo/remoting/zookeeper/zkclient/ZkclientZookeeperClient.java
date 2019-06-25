/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.I0Itec.zkclient.IZkChildListener
 *  org.I0Itec.zkclient.IZkStateListener
 *  org.I0Itec.zkclient.ZkClient
 *  org.I0Itec.zkclient.exception.ZkNoNodeException
 *  org.I0Itec.zkclient.exception.ZkNodeExistsException
 *  org.apache.zookeeper.Watcher
 *  org.apache.zookeeper.Watcher$Event
 *  org.apache.zookeeper.Watcher$Event$KeeperState
 */
package com.alibaba.dubbo.remoting.zookeeper.zkclient;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.zookeeper.ChildListener;
import com.alibaba.dubbo.remoting.zookeeper.support.AbstractZookeeperClient;
import java.util.List;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.Watcher;

public class ZkclientZookeeperClient
extends AbstractZookeeperClient<IZkChildListener> {
    private final ZkClient client;
    private volatile Watcher.Event.KeeperState state = Watcher.Event.KeeperState.SyncConnected;

    public ZkclientZookeeperClient(URL url) {
        super(url);
        this.client = new ZkClient(url.getBackupAddress(), url.getParameter("session", 60000), url.getParameter("timeout", 5000));
        this.client.subscribeStateChanges(new IZkStateListener(){

            public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
                ZkclientZookeeperClient.this.state = state;
                if (state == Watcher.Event.KeeperState.Disconnected) {
                    ZkclientZookeeperClient.this.stateChanged(0);
                } else if (state == Watcher.Event.KeeperState.SyncConnected) {
                    ZkclientZookeeperClient.this.stateChanged(1);
                }
            }

            public void handleNewSession() throws Exception {
                ZkclientZookeeperClient.this.stateChanged(2);
            }
        });
    }

    @Override
    public void createPersistent(String path) {
        try {
            this.client.createPersistent(path, true);
        }
        catch (ZkNodeExistsException zkNodeExistsException) {
            // empty catch block
        }
    }

    @Override
    public void createEphemeral(String path) {
        try {
            this.client.createEphemeral(path);
        }
        catch (ZkNodeExistsException zkNodeExistsException) {
            // empty catch block
        }
    }

    @Override
    public void delete(String path) {
        try {
            this.client.delete(path);
        }
        catch (ZkNoNodeException zkNoNodeException) {
            // empty catch block
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return this.client.getChildren(path);
        }
        catch (ZkNoNodeException e) {
            return null;
        }
    }

    @Override
    public boolean isConnected() {
        return this.state == Watcher.Event.KeeperState.SyncConnected;
    }

    @Override
    public void doClose() {
        this.client.close();
    }

    @Override
    public IZkChildListener createTargetChildListener(String path, final ChildListener listener) {
        return new IZkChildListener(){

            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                listener.childChanged(parentPath, currentChilds);
            }
        };
    }

    @Override
    public List<String> addTargetChildListener(String path, IZkChildListener listener) {
        return this.client.subscribeChildChanges(path, listener);
    }

    @Override
    public void removeTargetChildListener(String path, IZkChildListener listener) {
        this.client.unsubscribeChildChanges(path, listener);
    }

}

