/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p.exchange.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.p2p.exchange.ExchangePeer;
import com.alibaba.dubbo.remoting.p2p.exchange.support.AbstractExchangeGroup;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;

public class MulticastExchangeGroup
extends AbstractExchangeGroup {
    private static final String JOIN = "join";
    private static final String LEAVE = "leave";
    private InetAddress mutilcastAddress;
    private MulticastSocket mutilcastSocket;

    public MulticastExchangeGroup(URL url) {
        super(url);
        if (!MulticastExchangeGroup.isMulticastAddress(url.getHost())) {
            throw new IllegalArgumentException("Invalid multicast address " + url.getHost() + ", scope: 224.0.0.0 - 239.255.255.255");
        }
        try {
            this.mutilcastAddress = InetAddress.getByName(url.getHost());
            this.mutilcastSocket = new MulticastSocket(url.getPort());
            this.mutilcastSocket.setLoopbackMode(false);
            this.mutilcastSocket.joinGroup(this.mutilcastAddress);
            Thread thread = new Thread(new Runnable(){

                @Override
                public void run() {
                    byte[] buf = new byte[1024];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
                    do {
                        try {
                            do {
                                MulticastExchangeGroup.this.mutilcastSocket.receive(recv);
                                MulticastExchangeGroup.this.receive(new String(recv.getData()).trim(), (InetSocketAddress)recv.getSocketAddress());
                            } while (true);
                        }
                        catch (Exception e) {
                            AbstractExchangeGroup.logger.error(e.getMessage(), e);
                            continue;
                        }
                        break;
                    } while (true);
                }
            }, "MulticastGroupReceiver");
            thread.setDaemon(true);
            thread.start();
        }
        catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
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

    private void send(String msg) throws RemotingException {
        DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), this.mutilcastAddress, this.mutilcastSocket.getLocalPort());
        try {
            this.mutilcastSocket.send(hi);
        }
        catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void receive(String msg, InetSocketAddress remoteAddress) throws RemotingException {
        if (msg.startsWith(JOIN)) {
            String url = msg.substring(JOIN.length()).trim();
            this.connect(URL.valueOf(url));
        } else if (msg.startsWith(LEAVE)) {
            String url = msg.substring(LEAVE.length()).trim();
            this.disconnect(URL.valueOf(url));
        }
    }

    @Override
    public ExchangePeer join(URL url, ExchangeHandler handler) throws RemotingException {
        ExchangePeer peer = super.join(url, handler);
        this.send("join " + url.toFullString());
        return peer;
    }

    @Override
    public void leave(URL url) throws RemotingException {
        super.leave(url);
        this.send("leave " + url.toFullString());
    }

}

