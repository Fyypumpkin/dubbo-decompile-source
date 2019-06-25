/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Appender
 *  org.apache.log4j.FileAppender
 *  org.apache.log4j.LogManager
 *  org.apache.log4j.Logger
 *  org.apache.log4j.PropertyConfigurator
 */
package com.alibaba.dubbo.container.log4j;

import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.container.Container;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log4jContainer
implements Container {
    public static final String LOG4J_FILE = "dubbo.log4j.file";
    public static final String LOG4J_LEVEL = "dubbo.log4j.level";
    public static final String LOG4J_SUBDIRECTORY = "dubbo.log4j.subdirectory";
    public static final String DEFAULT_LOG4J_LEVEL = "ERROR";

    @Override
    public void start() {
        String subdirectory;
        String file = ConfigUtils.getProperty(LOG4J_FILE);
        if (file != null && file.length() > 0) {
            String level = ConfigUtils.getProperty(LOG4J_LEVEL);
            if (level == null || level.length() == 0) {
                level = DEFAULT_LOG4J_LEVEL;
            }
            Properties properties = new Properties();
            properties.setProperty("log4j.rootLogger", level + ",application");
            properties.setProperty("log4j.appender.application", "org.apache.log4j.DailyRollingFileAppender");
            properties.setProperty("log4j.appender.application.File", file);
            properties.setProperty("log4j.appender.application.Append", "true");
            properties.setProperty("log4j.appender.application.DatePattern", "'.'yyyy-MM-dd");
            properties.setProperty("log4j.appender.application.layout", "org.apache.log4j.PatternLayout");
            properties.setProperty("log4j.appender.application.layout.ConversionPattern", "%d [%t] %-5p %C{6} (%F:%L) - %m%n");
            PropertyConfigurator.configure((Properties)properties);
        }
        if ((subdirectory = ConfigUtils.getProperty(LOG4J_SUBDIRECTORY)) != null && subdirectory.length() > 0) {
            Enumeration ls = LogManager.getCurrentLoggers();
            while (ls.hasMoreElements()) {
                Logger l = (Logger)ls.nextElement();
                if (l == null) continue;
                Enumeration as = l.getAllAppenders();
                while (as.hasMoreElements()) {
                    String path;
                    FileAppender fa;
                    String f;
                    Appender a = (Appender)as.nextElement();
                    if (!(a instanceof FileAppender) || (f = (fa = (FileAppender)a).getFile()) == null || f.length() <= 0) continue;
                    int i = f.replace('\\', '/').lastIndexOf(47);
                    if (i == -1) {
                        path = subdirectory;
                    } else {
                        path = f.substring(0, i);
                        if (!path.endsWith(subdirectory)) {
                            path = path + "/" + subdirectory;
                        }
                        f = f.substring(i + 1);
                    }
                    fa.setFile(path + "/" + f);
                    fa.activateOptions();
                }
            }
        }
    }

    @Override
    public void stop() {
    }
}

