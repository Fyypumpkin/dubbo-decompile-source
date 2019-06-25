/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.telnet.support.command;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Level;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.telnet.support.Help;
import java.io.File;
import java.io.FileInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

@Activate
@Help(parameter="level", summary="Change log level or show log ", detail="Change log level or show log")
public class LogTelnetHandler
implements TelnetHandler {
    public static final String SERVICE_KEY = "telnet.log";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String telnet(Channel channel, String message) {
        StringBuffer buf;
        File file;
        long size = 0L;
        file = LoggerFactory.getFile();
        buf = new StringBuffer();
        if (message == null || message.trim().length() == 0) {
            buf.append("EXAMPLE: log error / log 100");
        } else {
            String[] str = message.split(" ");
            if (!StringUtils.isInteger(str[0])) {
                LoggerFactory.setLevel(Level.valueOf(message.toUpperCase()));
            } else {
                int SHOW_LOG_LENGTH = Integer.parseInt(str[0]);
                if (file != null && file.exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        try {
                            FileChannel filechannel = fis.getChannel();
                            try {
                                ByteBuffer bb;
                                size = filechannel.size();
                                if (size <= (long)SHOW_LOG_LENGTH) {
                                    bb = ByteBuffer.allocate((int)size);
                                    filechannel.read(bb, 0L);
                                } else {
                                    int pos = (int)(size - (long)SHOW_LOG_LENGTH);
                                    bb = ByteBuffer.allocate(SHOW_LOG_LENGTH);
                                    filechannel.read(bb, pos);
                                }
                                bb.flip();
                                String content = new String(bb.array()).replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/><br/>");
                                buf.append("\r\ncontent:" + content);
                                buf.append("\r\nmodified:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified())));
                                buf.append("\r\nsize:" + size + "\r\n");
                            }
                            finally {
                                filechannel.close();
                            }
                        }
                        finally {
                            fis.close();
                        }
                    }
                    catch (Exception e) {
                        buf.append(e.getMessage());
                    }
                } else {
                    size = 0L;
                    buf.append("\r\nMESSAGE: log file not exists or log appender is console .");
                }
            }
        }
        buf.append("\r\nCURRENT LOG LEVEL:" + (Object)((Object)LoggerFactory.getLevel())).append("\r\nCURRENT LOG APPENDER:" + (file == null ? "console" : file.getAbsolutePath()));
        return buf.toString();
    }
}

