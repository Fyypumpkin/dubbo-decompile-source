/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange;

public class Response {
    public static final String HEARTBEAT_EVENT = null;
    public static final String READONLY_EVENT = "R";
    public static final byte OK = 20;
    public static final byte CLIENT_TIMEOUT = 30;
    public static final byte SERVER_TIMEOUT = 31;
    public static final byte BAD_REQUEST = 40;
    public static final byte BAD_RESPONSE = 50;
    public static final byte SERVICE_NOT_FOUND = 60;
    public static final byte SERVICE_ERROR = 70;
    public static final byte SERVER_ERROR = 80;
    public static final byte CLIENT_ERROR = 90;
    public static final byte SERVER_THREADPOOL_EXHAUSTED_ERROR = 100;
    private long mId = 0L;
    private String mVersion;
    private byte mStatus = (byte)20;
    private boolean mEvent = false;
    private String mErrorMsg;
    private Object mResult;
    private int clientIp;
    private int clientPort;
    private String serviceName;
    private String methodName;

    public Response() {
    }

    public Response(long id) {
        this.mId = id;
    }

    public Response(long id, String version) {
        this.mId = id;
        this.mVersion = version;
    }

    public long getId() {
        return this.mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getVersion() {
        return this.mVersion;
    }

    public void setVersion(String version) {
        this.mVersion = version;
    }

    public byte getStatus() {
        return this.mStatus;
    }

    public void setStatus(byte status) {
        this.mStatus = status;
    }

    public boolean isEvent() {
        return this.mEvent;
    }

    public void setEvent(String event) {
        this.mEvent = true;
        this.mResult = event;
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

    public boolean isHeartbeat() {
        boolean flg1 = this.mEvent && HEARTBEAT_EVENT == this.mResult;
        boolean flg2 = "pong".equals(this.methodName) && "com.youzan.service.test".equals(this.serviceName);
        return flg1 || flg2;
    }

    @Deprecated
    public void setHeartbeat(boolean isHeartbeat) {
        if (isHeartbeat) {
            this.setEvent(HEARTBEAT_EVENT);
        }
    }

    public int getClientPort() {
        return this.clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public int getClientIp() {
        return this.clientIp;
    }

    public void setClientIp(int clientIp) {
        this.clientIp = clientIp;
    }

    public Object getResult() {
        return this.mResult;
    }

    public void setResult(Object msg) {
        this.mResult = msg;
    }

    public String getErrorMessage() {
        return this.mErrorMsg;
    }

    public void setErrorMessage(String msg) {
        this.mErrorMsg = msg;
    }

    public String toString() {
        return "Response [id=" + this.mId + ", version=" + this.mVersion + ", status=" + this.mStatus + ", event=" + this.mEvent + ", clientIp=" + this.clientIp + ", clientPort=" + this.clientPort + ", serviceName='" + this.serviceName + ", methodName='" + this.methodName + ", error=" + this.mErrorMsg + ", result=" + (this.mResult == this ? "this" : this.mResult) + "]";
    }
}

