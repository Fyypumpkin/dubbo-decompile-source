/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Level
 */
package com.alibaba.dubbo.common.utils;

import java.io.Serializable;
import org.apache.log4j.Level;

public class Log
implements Serializable {
    private static final long serialVersionUID = -534113138054377073L;
    private String logName;
    private Level logLevel;
    private String logMessage;
    private String logThread;

    public String getLogName() {
        return this.logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public Level getLogLevel() {
        return this.logLevel;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogMessage() {
        return this.logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getLogThread() {
        return this.logThread;
    }

    public void setLogThread(String logThread) {
        this.logThread = logThread;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.logLevel == null ? 0 : this.logLevel.hashCode());
        result = 31 * result + (this.logMessage == null ? 0 : this.logMessage.hashCode());
        result = 31 * result + (this.logName == null ? 0 : this.logName.hashCode());
        result = 31 * result + (this.logThread == null ? 0 : this.logThread.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Log other = (Log)obj;
        if (this.logLevel == null ? other.logLevel != null : !this.logLevel.equals((Object)other.logLevel)) {
            return false;
        }
        if (this.logMessage == null ? other.logMessage != null : !this.logMessage.equals(other.logMessage)) {
            return false;
        }
        if (this.logName == null ? other.logName != null : !this.logName.equals(other.logName)) {
            return false;
        }
        return !(this.logThread == null ? other.logThread != null : !this.logThread.equals(other.logThread));
    }
}

