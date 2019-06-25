/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class MultiMessage
implements Iterable {
    private final List messages = new ArrayList();

    public static MultiMessage createFromCollection(Collection collection) {
        MultiMessage result = new MultiMessage();
        result.addMessages(collection);
        return result;
    }

    public static /* varargs */ MultiMessage createFromArray(Object ... args) {
        return MultiMessage.createFromCollection(Arrays.asList(args));
    }

    public static MultiMessage create() {
        return new MultiMessage();
    }

    private MultiMessage() {
    }

    public void addMessage(Object msg) {
        this.messages.add(msg);
    }

    public void addMessages(Collection collection) {
        this.messages.addAll(collection);
    }

    public Collection getMessages() {
        return Collections.unmodifiableCollection(this.messages);
    }

    public int size() {
        return this.messages.size();
    }

    public Object get(int index) {
        return this.messages.get(index);
    }

    public boolean isEmpty() {
        return this.messages.isEmpty();
    }

    public Collection removeMessages() {
        Collection result = Collections.unmodifiableCollection(this.messages);
        this.messages.clear();
        return result;
    }

    public Iterator iterator() {
        return this.messages.iterator();
    }
}

