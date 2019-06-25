/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange;

import com.alibaba.dubbo.common.utils.StringUtils;
import java.util.concurrent.atomic.AtomicLong;

public class Request {
    public static final String HEARTBEAT_EVENT = null;
    public static final String READONLY_EVENT = "R";
    private static final AtomicLong INVOKE_ID = new AtomicLong(0L);
    private final long mId;
    private String mVersion;
    private boolean mTwoWay = true;
    private boolean mEvent = false;
    private boolean mBroken = false;
    private Object mData;
    private int clientIp;
    private int clientPort;
    private String serviceName;
    private String methodName;

    public Request() {
        this.mId = Request.newId();
    }

    public Request(long id) {
        this.mId = id;
    }

    public long getId() {
        return this.mId;
    }

    public String getVersion() {
        return this.mVersion;
    }

    public void setVersion(String version) {
        this.mVersion = version;
    }

    public boolean isTwoWay() {
        return this.mTwoWay;
    }

    public void setTwoWay(boolean twoWay) {
        this.mTwoWay = twoWay;
    }

    public boolean isEvent() {
        return this.mEvent;
    }

    public void setEvent(String event) {
        this.mEvent = true;
        this.mData = event;
    }

    public boolean isBroken() {
        return this.mBroken;
    }

    public void setBroken(boolean mBroken) {
        this.mBroken = mBroken;
    }

    public Object getData() {
        return this.mData;
    }

    public void setData(Object msg) {
        this.mData = msg;
    }

    public int getClientIp() {
        return this.clientIp;
    }

    public void setClientIp(int clientIp) {
        this.clientIp = clientIp;
    }

    public int getClientPort() {
        return this.clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public boolean isHeartbeat() {
        boolean flg1 = this.mEvent && HEARTBEAT_EVENT == this.mData;
        boolean flg2 = "ping".equals(this.methodName) && "com.youzan.service.test".equals(this.serviceName);
        return flg1 || flg2;
    }

    public void setHeartbeat(boolean isHeartbeat) {
        if (isHeartbeat) {
            this.setEvent(HEARTBEAT_EVENT);
        }
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    private static long newId() {
        return INVOKE_ID.getAndIncrement();
    }

    public String toString() {
        return "Request [id=" + this.mId + ",clientIp=" + this.clientIp + " version=" + this.mVersion + ", twoway=" + this.mTwoWay + ", event=" + this.mEvent + ", broken=" + this.mBroken + ", clientIp=" + this.clientIp + ", clientPort=" + this.clientPort + ", serviceName='" + this.serviceName + ", methodName='" + this.methodName + ", data=" + (this.mData == this ? "this" : Request.safeToString(this.mData)) + "]";
    }

    private static String safeToString(Object data) {
        String dataStr;
        if (data == null) {
            return null;
        }
        try {
            dataStr = data.toString();
        }
        catch (Throwable e) {
            dataStr = "<Fail toString of " + data.getClass() + ", cause: " + StringUtils.toString(e) + ">";
        }
        return dataStr;
    }
}

