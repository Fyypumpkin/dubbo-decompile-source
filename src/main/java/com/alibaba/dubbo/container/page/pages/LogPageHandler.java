/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Appender
 *  org.apache.log4j.FileAppender
 *  org.apache.log4j.Level
 *  org.apache.log4j.LogManager
 *  org.apache.log4j.Logger
 */
package com.alibaba.dubbo.container.page.pages;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.container.page.Menu;
import com.alibaba.dubbo.container.page.Page;
import com.alibaba.dubbo.container.page.PageHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Menu(name="Log", desc="Show system log.", order=2147472647)
public class LogPageHandler
implements PageHandler {
    private static final int SHOW_LOG_LENGTH = 30000;
    private File file;

    public LogPageHandler() {
        block3 : {
            try {
                Enumeration appenders;
                Logger logger = LogManager.getRootLogger();
                if (logger == null || (appenders = logger.getAllAppenders()) == null) break block3;
                while (appenders.hasMoreElements()) {
                    Appender appender = (Appender)appenders.nextElement();
                    if (!(appender instanceof FileAppender)) continue;
                    FileAppender fileAppender = (FileAppender)appender;
                    String filename = fileAppender.getFile();
                    this.file = new File(filename);
                    break;
                }
            }
            catch (Throwable logger) {
                // empty catch block
            }
        }
    }

    @Override
    public Page handle(URL url) {
        long size = 0L;
        String content = "";
        String modified = "Not exist";
        if (this.file != null && this.file.exists()) {
            try {
                ByteBuffer bb;
                FileInputStream fis = new FileInputStream(this.file);
                FileChannel channel = fis.getChannel();
                size = channel.size();
                if (size <= 30000L) {
                    bb = ByteBuffer.allocate((int)size);
                    channel.read(bb, 0L);
                } else {
                    int pos = (int)(size - 30000L);
                    bb = ByteBuffer.allocate(30000);
                    channel.read(bb, pos);
                }
                bb.flip();
                content = new String(bb.array()).replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/><br/>");
                modified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(this.file.lastModified()));
            }
            catch (IOException fis) {
                // empty catch block
            }
        }
        Level level = LogManager.getRootLogger().getLevel();
        ArrayList<List<String>> rows = new ArrayList<List<String>>();
        ArrayList<String> row = new ArrayList<String>();
        row.add(content);
        rows.add(row);
        return new Page("Log", "Log", new String[]{(this.file == null ? "" : this.file.getName()) + ", " + size + " bytes, " + modified + ", " + (Object)level}, rows);
    }
}

