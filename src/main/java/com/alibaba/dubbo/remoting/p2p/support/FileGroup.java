/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.utils.IOUtils;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.p2p.Peer;
import com.alibaba.dubbo.remoting.p2p.support.AbstractGroup;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FileGroup
extends AbstractGroup {
    private final File file;
    private volatile long last;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3, new NamedThreadFactory("FileGroupModifiedChecker", true));
    private final ScheduledFuture<?> checkModifiedFuture;

    public FileGroup(URL url) {
        super(url);
        String path = url.getAbsolutePath();
        this.file = new File(path);
        this.checkModifiedFuture = this.scheduledExecutorService.scheduleWithFixedDelay(new Runnable(){

            @Override
            public void run() {
                try {
                    FileGroup.this.check();
                }
                catch (Throwable t) {
                    AbstractGroup.logger.error("Unexpected error occur at reconnect, cause: " + t.getMessage(), t);
                }
            }
        }, 2000L, 2000L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        super.close();
        try {
            if (!this.checkModifiedFuture.isCancelled()) {
                this.checkModifiedFuture.cancel(true);
            }
        }
        catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }

    private void check() throws RemotingException {
        long modified = this.file.lastModified();
        if (modified > this.last) {
            this.last = modified;
            this.changed();
        }
    }

    private void changed() throws RemotingException {
        try {
            String[] lines;
            for (String line : lines = IOUtils.readLines(this.file)) {
                this.connect(URL.valueOf(line));
            }
        }
        catch (IOException e) {
            throw new RemotingException(new InetSocketAddress(NetUtils.getLocalHost(), 0), this.getUrl().toInetSocketAddress(), e.getMessage(), e);
        }
    }

    @Override
    public Peer join(URL url, ChannelHandler handler) throws RemotingException {
        Peer peer = super.join(url, handler);
        try {
            String[] lines;
            String full = url.toFullString();
            for (String line : lines = IOUtils.readLines(this.file)) {
                if (!full.equals(line)) continue;
                return peer;
            }
            IOUtils.appendLines(this.file, new String[]{full});
        }
        catch (IOException e) {
            throw new RemotingException(new InetSocketAddress(NetUtils.getLocalHost(), 0), this.getUrl().toInetSocketAddress(), e.getMessage(), e);
        }
        return peer;
    }

    @Override
    public void leave(URL url) throws RemotingException {
        super.leave(url);
        try {
            String full = url.toFullString();
            String[] lines = IOUtils.readLines(this.file);
            ArrayList<String> saves = new ArrayList<String>();
            for (String line : lines) {
                if (full.equals(line)) {
                    return;
                }
                saves.add(line);
            }
            IOUtils.appendLines(this.file, saves.toArray(new String[0]));
        }
        catch (IOException e) {
            throw new RemotingException(new InetSocketAddress(NetUtils.getLocalHost(), 0), this.getUrl().toInetSocketAddress(), e.getMessage(), e);
        }
    }

}

