/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Category
 *  org.apache.log4j.ConsoleAppender
 *  org.apache.log4j.Level
 *  org.apache.log4j.spi.LoggingEvent
 */
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.utils.Log;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Category;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class DubboAppender
extends ConsoleAppender {
    public static boolean available = false;
    public static List<Log> logList = new ArrayList<Log>();

    public static void doStart() {
        available = true;
    }

    public static void doStop() {
        available = false;
    }

    public static void clear() {
        logList.clear();
    }

    public void append(LoggingEvent event) {
        super.append(event);
        if (available) {
            Log temp = this.parseLog(event);
            logList.add(temp);
        }
    }

    private Log parseLog(LoggingEvent event) {
        Log log = new Log();
        log.setLogName(event.getLogger().getName());
        log.setLogLevel(event.getLevel());
        log.setLogThread(event.getThreadName());
        log.setLogMessage(event.getMessage().toString());
        return log;
    }
}

