/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.multicast;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MulticastRegistry
extends FailbackRegistry {
    private static final Logger logger = LoggerFactory.getLogger(MulticastRegistry.class);
    private static final int DEFAULT_MULTICAST_PORT = 1234;
    private final InetAddress mutilcastAddress;
    private final MulticastSocket mutilcastSocket;
    private final int mutilcastPort;
    private final ConcurrentMap<URL, Set<URL>> received = new ConcurrentHashMap<URL, Set<URL>>();
    private final ScheduledExecutorService cleanExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("DubboMulticastRegistryCleanTimer", true));
    private final ScheduledFuture<?> cleanFuture;
    private final int cleanPeriod;
    private volatile boolean admin = false;

    public MulticastRegistry(URL url) {
        super(url);
        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null");
        }
        if (!MulticastRegistry.isMulticastAddress(url.getHost())) {
            throw new IllegalArgumentException("Invalid multicast address " + url.getHost() + ", scope: 224.0.0.0 - 239.255.255.255");
        }
        try {
            this.mutilcastAddress = InetAddress.getByName(url.getHost());
            this.mutilcastPort = url.getPort() <= 0 ? 1234 : url.getPort();
            this.mutilcastSocket = new MulticastSocket(this.mutilcastPort);
            this.mutilcastSocket.setLoopbackMode(false);
            this.mutilcastSocket.joinGroup(this.mutilcastAddress);
            Thread thread = new Thread(new Runnable(){

                @Override
                public void run() {
                    byte[] buf = new byte[2048];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
                    while (!MulticastRegistry.this.mutilcastSocket.isClosed()) {
                        try {
                            MulticastRegistry.this.mutilcastSocket.receive(recv);
                            String msg = new String(recv.getData()).trim();
                            int i = msg.indexOf(10);
                            if (i > 0) {
                                msg = msg.substring(0, i).trim();
                            }
                            MulticastRegistry.this.receive(msg, (InetSocketAddress)recv.getSocketAddress());
                            Arrays.fill(buf, (byte)0);
                        }
                        catch (Throwable e) {
                            if (MulticastRegistry.this.mutilcastSocket.isClosed()) continue;
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }, "DubboMulticastRegistryReceiver");
            thread.setDaemon(true);
            thread.start();
        }
        catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        this.cleanPeriod = url.getParameter("session", 60000);
        this.cleanFuture = url.getParameter("clean", true) ? this.cleanExecutor.scheduleWithFixedDelay(new Runnable(){

            @Override
            public void run() {
                try {
                    MulticastRegistry.this.clean();
                }
                catch (Throwable t) {
                    logger.error("Unexpected exception occur at clean expired provider, cause: " + t.getMessage(), t);
                }
            }
        }, this.cleanPeriod, this.cleanPeriod, TimeUnit.MILLISECONDS) : null;
    }

    private static boolean isMulticastAddress(String ip) {
        String prefix;
        int i = ip.indexOf(46);
        if (i > 0 && StringUtils.isInteger(prefix = ip.substring(0, i))) {
            int p = Integer.parseInt(prefix);
            return p >= 224 && p <= 239;
        }
        return false;
    }

    private void clean() {
        if (this.admin) {
            for (Set providers : new HashSet(this.received.values())) {
                for (URL url : new HashSet(providers)) {
                    if (!this.isExpired(url)) continue;
                    if (logger.isWarnEnabled()) {
                        logger.warn("Clean expired provider " + url);
                    }
                    this.doUnregister(url);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean isExpired(URL url) {
        if (!url.getParameter("dynamic", true) || url.getPort() <= 0 || "consumer".equals(url.getProtocol()) || "route".equals(url.getProtocol()) || "override".equals(url.getProtocol())) {
            return false;
        }
        Socket socket = null;
        socket = new Socket(url.getHost(), url.getPort());
        if (socket == null) return false;
        try {
            socket.close();
            return false;
        }
        catch (Throwable throwable) {}
        return false;
        catch (Throwable e2) {
            try {
                try {
                    Thread.sleep(100L);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                Socket socket2 = null;
                socket2 = new Socket(url.getHost(), url.getPort());
                if (socket2 == null) return false;
                try {
                    socket2.close();
                    return false;
                }
                catch (Throwable throwable) {}
                return false;
                catch (Throwable e2) {
                    boolean bl;
                    block28 : {
                        try {
                            bl = true;
                            if (socket2 == null) break block28;
                        }
                        catch (Throwable throwable) {
                            if (socket2 == null) throw throwable;
                            try {
                                socket2.close();
                                throw throwable;
                            }
                            catch (Throwable throwable2) {
                                // empty catch block
                            }
                            throw throwable;
                        }
                        try {
                            socket2.close();
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    }
                    if (socket == null) return bl;
                    try {
                        socket.close();
                        return bl;
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                    return bl;
                }
            }
            finally {
                if (socket != null) {
                    try {
                        socket.close();
                    }
                    catch (Throwable e2) {}
                }
            }
        }
    }

    private void receive(String msg, InetSocketAddress remoteAddress) {
        if (logger.isInfoEnabled()) {
            logger.info("Receive multicast message: " + msg + " from " + remoteAddress);
        }
        if (msg.startsWith("register")) {
            URL url = URL.valueOf(msg.substring("register".length()).trim());
            this.registered(url);
        } else if (msg.startsWith("unregister")) {
            URL url = URL.valueOf(msg.substring("unregister".length()).trim());
            this.unregistered(url);
        } else if (msg.startsWith("subscribe")) {
            URL url = URL.valueOf(msg.substring("subscribe".length()).trim());
            Set<URL> urls = this.getRegistered();
            if (urls != null && urls.size() > 0) {
                for (URL u : urls) {
                    String host;
                    if (!UrlUtils.isMatch(url, u)) continue;
                    String string = host = remoteAddress != null && remoteAddress.getAddress() != null ? remoteAddress.getAddress().getHostAddress() : url.getIp();
                    if (url.getParameter("unicast", true) && !NetUtils.getLocalHost().equals(host)) {
                        this.unicast("register " + u.toFullString(), host);
                        continue;
                    }
                    this.broadcast("register " + u.toFullString());
                }
            }
        }
    }

    private void broadcast(String msg) {
        if (logger.isInfoEnabled()) {
            logger.info("Send broadcast message: " + msg + " to " + this.mutilcastAddress + ":" + this.mutilcastPort);
        }
        try {
            byte[] data = (msg + "\n").getBytes();
            DatagramPacket hi = new DatagramPacket(data, data.length, this.mutilcastAddress, this.mutilcastPort);
            this.mutilcastSocket.send(hi);
        }
        catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void unicast(String msg, String host) {
        if (logger.isInfoEnabled()) {
            logger.info("Send unicast message: " + msg + " to " + host + ":" + this.mutilcastPort);
        }
        try {
            byte[] data = (msg + "\n").getBytes();
            DatagramPacket hi = new DatagramPacket(data, data.length, InetAddress.getByName(host), this.mutilcastPort);
            this.mutilcastSocket.send(hi);
        }
        catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    protected void doRegister(URL url) {
        this.broadcast("register " + url.toFullString());
    }

    @Override
    protected void doUnregister(URL url) {
        this.broadcast("unregister " + url.toFullString());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doSubscribe(URL url, NotifyListener listener) {
        if ("*".equals(url.getServiceInterface())) {
            this.admin = true;
        }
        this.broadcast("subscribe " + url.toFullString());
        NotifyListener notifyListener = listener;
        synchronized (notifyListener) {
            try {
                listener.wait(url.getParameter("timeout", 1000));
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
    }

    @Override
    protected void doUnsubscribe(URL url, NotifyListener listener) {
        if (!"*".equals(url.getServiceInterface()) && url.getParameter("register", true)) {
            this.unregister(url);
        }
        this.broadcast("unsubscribe " + url.toFullString());
    }

    @Override
    public boolean isAvailable() {
        try {
            return this.mutilcastSocket != null;
        }
        catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            if (this.cleanFuture != null) {
                this.cleanFuture.cancel(true);
            }
        }
        catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
        try {
            this.mutilcastSocket.leaveGroup(this.mutilcastAddress);
            this.mutilcastSocket.close();
        }
        catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void registered(URL url) {
        for (Map.Entry<URL, Set<NotifyListener>> entry : this.getSubscribed().entrySet()) {
            URL key = entry.getKey();
            if (!UrlUtils.isMatch(key, url)) continue;
            Set urls = (Set)this.received.get(key);
            if (urls == null) {
                this.received.putIfAbsent(key, new ConcurrentHashSet());
                urls = (Set)this.received.get(key);
            }
            urls.add(url);
            List<URL> list = this.toList(urls);
            for (NotifyListener listener : entry.getValue()) {
                this.notify(key, listener, list);
                NotifyListener notifyListener = listener;
                synchronized (notifyListener) {
                    listener.notify();
                }
            }
        }
    }

    protected void unregistered(URL url) {
        for (Map.Entry<URL, Set<NotifyListener>> entry : this.getSubscribed().entrySet()) {
            URL key = entry.getKey();
            if (!UrlUtils.isMatch(key, url)) continue;
            Set urls = (Set)this.received.get(key);
            if (urls != null) {
                urls.remove(url);
            }
            List<URL> list = this.toList(urls);
            for (NotifyListener listener : entry.getValue()) {
                this.notify(key, listener, list);
            }
        }
    }

    protected void subscribed(URL url, NotifyListener listener) {
        List<URL> urls = this.lookup(url);
        this.notify(url, listener, urls);
    }

    private List<URL> toList(Set<URL> urls) {
        ArrayList<URL> list = new ArrayList<URL>();
        if (urls != null && urls.size() > 0) {
            for (URL url : urls) {
                list.add(url);
            }
        }
        return list;
    }

    @Override
    public void register(URL url) {
        super.register(url);
        this.registered(url);
    }

    @Override
    public void unregister(URL url) {
        super.unregister(url);
        this.unregistered(url);
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        super.subscribe(url, listener);
        this.subscribed(url, listener);
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        super.unsubscribe(url, listener);
        this.received.remove(url);
    }

    @Override
    public List<URL> lookup(URL url) {
        Iterator<URL> cacheUrls;
        ArrayList<URL> urls = new ArrayList<URL>();
        Map<String, List<URL>> notifiedUrls = this.getNotified().get(url);
        if (notifiedUrls != null && notifiedUrls.size() > 0) {
            for (List<URL> values : notifiedUrls.values()) {
                urls.addAll(values);
            }
        }
        if ((urls == null || urls.size() == 0) && (cacheUrls = this.getCacheUrls(url)) != null && cacheUrls.size() > 0) {
            urls.addAll((Collection<URL>)((Object)cacheUrls));
        }
        if (urls == null || urls.size() == 0) {
            for (URL u : this.getRegistered()) {
                if (!UrlUtils.isMatch(url, u)) continue;
                urls.add(u);
            }
        }
        if ("*".equals(url.getServiceInterface())) {
            for (URL u : this.getSubscribed().keySet()) {
                if (!UrlUtils.isMatch(url, u)) continue;
                urls.add(u);
            }
        }
        return urls;
    }

    public MulticastSocket getMutilcastSocket() {
        return this.mutilcastSocket;
    }

    public Map<URL, Set<URL>> getReceived() {
        return this.received;
    }

}

