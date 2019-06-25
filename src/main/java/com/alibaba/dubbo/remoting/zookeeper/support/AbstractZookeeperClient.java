/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.zookeeper.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.zookeeper.ChildListener;
import com.alibaba.dubbo.remoting.zookeeper.StateListener;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperClient;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractZookeeperClient<TargetChildListener>
implements ZookeeperClient {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractZookeeperClient.class);
    private final URL url;
    private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<StateListener>();
    private final ConcurrentMap<String, ConcurrentMap<ChildListener, TargetChildListener>> childListeners = new ConcurrentHashMap<String, ConcurrentMap<ChildListener, TargetChildListener>>();
    private volatile boolean closed = false;

    public AbstractZookeeperClient(URL url) {
        this.url = url;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public void create(String path, boolean ephemeral) {
        int i = path.lastIndexOf(47);
        if (i > 0) {
            this.create(path.substring(0, i), false);
        }
        if (ephemeral) {
            this.createEphemeral(path);
        } else {
            this.createPersistent(path);
        }
    }

    @Override
    public void addStateListener(StateListener listener) {
        this.stateListeners.add(listener);
    }

    @Override
    public void removeStateListener(StateListener listener) {
        this.stateListeners.remove(listener);
    }

    public Set<StateListener> getSessionListeners() {
        return this.stateListeners;
    }

    @Override
    public List<String> addChildListener(String path, ChildListener listener) {
        Object targetListener;
        ConcurrentMap listeners = (ConcurrentMap)this.childListeners.get(path);
        if (listeners == null) {
            this.childListeners.putIfAbsent(path, new ConcurrentHashMap());
            listeners = (ConcurrentMap)this.childListeners.get(path);
        }
        if ((targetListener = listeners.get(listener)) == null) {
            listeners.putIfAbsent(listener, this.createTargetChildListener(path, listener));
            targetListener = listeners.get(listener);
        }
        return this.addTargetChildListener(path, targetListener);
    }

    @Override
    public void removeChildListener(String path, ChildListener listener) {
        Object targetListener;
        ConcurrentMap listeners = (ConcurrentMap)this.childListeners.get(path);
        if (listeners != null && (targetListener = listeners.remove(listener)) != null) {
            this.removeTargetChildListener(path, targetListener);
        }
    }

    protected void stateChanged(int state) {
        for (StateListener sessionListener : this.getSessionListeners()) {
            sessionListener.stateChanged(state);
        }
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        try {
            this.doClose();
        }
        catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
    }

    protected abstract void doClose();

    protected abstract void createPersistent(String var1);

    protected abstract void createEphemeral(String var1);

    protected abstract TargetChildListener createTargetChildListener(String var1, ChildListener var2);

    protected abstract List<String> addTargetChildListener(String var1, TargetChildListener var2);

    protected abstract void removeTargetChildListener(String var1, TargetChildListener var2);
}

