/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support;

import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.support.Replier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ReplierDispatcher
implements Replier<Object> {
    private final Replier<?> defaultReplier;
    private final Map<Class<?>, Replier<?>> repliers = new ConcurrentHashMap();

    public ReplierDispatcher() {
        this(null, null);
    }

    public ReplierDispatcher(Replier<?> defaultReplier) {
        this(defaultReplier, null);
    }

    public ReplierDispatcher(Replier<?> defaultReplier, Map<Class<?>, Replier<?>> repliers) {
        this.defaultReplier = defaultReplier;
        if (repliers != null && repliers.size() > 0) {
            this.repliers.putAll(repliers);
        }
    }

    public <T> ReplierDispatcher addReplier(Class<T> type, Replier<T> replier) {
        this.repliers.put(type, replier);
        return this;
    }

    public <T> ReplierDispatcher removeReplier(Class<T> type) {
        this.repliers.remove(type);
        return this;
    }

    private Replier<?> getReplier(Class<?> type) {
        for (Map.Entry<Class<?>, Replier<?>> entry : this.repliers.entrySet()) {
            if (!entry.getKey().isAssignableFrom(type)) continue;
            return entry.getValue();
        }
        if (this.defaultReplier != null) {
            return this.defaultReplier;
        }
        throw new IllegalStateException("Replier not found, Unsupported message object: " + type);
    }

    @Override
    public Object reply(ExchangeChannel channel, Object request) throws RemotingException {
        return this.getReplier(request.getClass()).reply(channel, request);
    }
}

