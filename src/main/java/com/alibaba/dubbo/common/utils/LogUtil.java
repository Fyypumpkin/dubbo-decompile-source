/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Level
 */
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.DubboAppender;
import com.alibaba.dubbo.common.utils.Log;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Level;

public class LogUtil {
    private static Logger Log = LoggerFactory.getLogger(LogUtil.class);

    public static void start() {
        DubboAppender.doStart();
    }

    public static void stop() {
        DubboAppender.doStop();
    }

    public static boolean checkNoError() {
        return LogUtil.findLevel(Level.ERROR) == 0;
    }

    public static int findName(String expectedLogName) {
        int count = 0;
        List<Log> logList = DubboAppender.logList;
        for (int i = 0; i < logList.size(); ++i) {
            String logName = logList.get(i).getLogName();
            if (!logName.contains(expectedLogName)) continue;
            ++count;
        }
        return count;
    }

    public static int findLevel(Level expectedLevel) {
        int count = 0;
        List<Log> logList = DubboAppender.logList;
        for (int i = 0; i < logList.size(); ++i) {
            Level logLevel = logList.get(i).getLogLevel();
            if (!logLevel.equals((Object)expectedLevel)) continue;
            ++count;
        }
        return count;
    }

    public static int findLevelWithThreadName(Level expectedLevel, String threadName) {
        int count = 0;
        List<Log> logList = DubboAppender.logList;
        for (int i = 0; i < logList.size(); ++i) {
            Log log = logList.get(i);
            if (!log.getLogLevel().equals((Object)expectedLevel) || !log.getLogThread().equals(threadName)) continue;
            ++count;
        }
        return count;
    }

    public static int findThread(String expectedThread) {
        int count = 0;
        List<Log> logList = DubboAppender.logList;
        for (int i = 0; i < logList.size(); ++i) {
            String logThread = logList.get(i).getLogThread();
            if (!logThread.contains(expectedThread)) continue;
            ++count;
        }
        return count;
    }

    public static int findMessage(String expectedMessage) {
        int count = 0;
        List<Log> logList = DubboAppender.logList;
        for (int i = 0; i < logList.size(); ++i) {
            String logMessage = logList.get(i).getLogMessage();
            if (!logMessage.contains(expectedMessage)) continue;
            ++count;
        }
        return count;
    }

    public static int findMessage(Level expectedLevel, String expectedMessage) {
        int count = 0;
        List<Log> logList = DubboAppender.logList;
        for (int i = 0; i < logList.size(); ++i) {
            String logMessage;
            Level logLevel = logList.get(i).getLogLevel();
            if (!logLevel.equals((Object)expectedLevel) || !(logMessage = logList.get(i).getLogMessage()).contains(expectedMessage)) continue;
            ++count;
        }
        return count;
    }

    public static <T> void printList(List<T> list) {
        Log.info("PrintList:");
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            Log.info(it.next().toString());
        }
    }
}

