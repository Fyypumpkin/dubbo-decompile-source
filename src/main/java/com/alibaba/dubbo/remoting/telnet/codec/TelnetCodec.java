/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.telnet.codec;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.transport.codec.TransportCodec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TelnetCodec
extends TransportCodec {
    private static final Logger logger = LoggerFactory.getLogger(TelnetCodec.class);
    private static final String HISTORY_LIST_KEY = "telnet.history.list";
    private static final String HISTORY_INDEX_KEY = "telnet.history.index";
    private static final byte[] UP = new byte[]{27, 91, 65};
    private static final byte[] DOWN = new byte[]{27, 91, 66};
    private static final List<?> ENTER = Arrays.asList(new byte[]{13, 10}, new byte[]{10});
    private static final List<?> EXIT = Arrays.asList(new byte[]{3}, new byte[]{-1, -12, -1, -3, 6}, new byte[]{-1, -19, -1, -3, 6});

    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException {
        if (message instanceof String) {
            if (this.isClientSide(channel)) {
                message = message + "\r\n";
            }
            byte[] msgData = ((String)message).getBytes(TelnetCodec.getCharset(channel).name());
            buffer.writeBytes(msgData);
        } else {
            super.encode(channel, buffer, message);
        }
    }

    @Override
    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {
        int readable = buffer.readableBytes();
        byte[] message = new byte[readable];
        buffer.readBytes(message);
        return this.decode(channel, buffer, readable, message);
    }

    protected Object decode(Channel channel, ChannelBuffer buffer, int readable, byte[] message) throws IOException {
        String value;
        String result;
        if (this.isClientSide(channel)) {
            return TelnetCodec.toString(message, TelnetCodec.getCharset(channel));
        }
        TelnetCodec.checkPayload(channel, readable);
        if (message == null || message.length == 0) {
            return Codec2.DecodeResult.NEED_MORE_INPUT;
        }
        if (message[message.length - 1] == 8) {
            try {
                byte[] arrby;
                boolean doublechar;
                boolean bl = doublechar = message.length >= 3 && message[message.length - 3] < 0;
                if (doublechar) {
                    byte[] arrby2 = new byte[4];
                    arrby2[0] = 32;
                    arrby2[1] = 32;
                    arrby2[2] = 8;
                    arrby = arrby2;
                    arrby2[3] = 8;
                } else {
                    byte[] arrby3 = new byte[2];
                    arrby3[0] = 32;
                    arrby = arrby3;
                    arrby3[1] = 8;
                }
                channel.send(new String(arrby, TelnetCodec.getCharset(channel).name()));
            }
            catch (RemotingException e) {
                throw new IOException(StringUtils.toString(e));
            }
            return Codec2.DecodeResult.NEED_MORE_INPUT;
        }
        for (Object command : EXIT) {
            if (!TelnetCodec.isEquals(message, (byte[])command)) continue;
            if (logger.isInfoEnabled()) {
                logger.info(new Exception("Close channel " + channel + " on exit command: " + Arrays.toString((byte[])command)));
            }
            channel.close();
            return null;
        }
        boolean up = TelnetCodec.endsWith(message, UP);
        boolean down = TelnetCodec.endsWith(message, DOWN);
        if (up || down) {
            Integer index;
            LinkedList history = (LinkedList)channel.getAttribute(HISTORY_LIST_KEY);
            if (history == null || history.size() == 0) {
                return Codec2.DecodeResult.NEED_MORE_INPUT;
            }
            Integer old = index = (Integer)channel.getAttribute(HISTORY_INDEX_KEY);
            if (index == null) {
                index = history.size() - 1;
            } else if (up) {
                if ((index = Integer.valueOf(index - 1)) < 0) {
                    index = history.size() - 1;
                }
            } else if ((index = Integer.valueOf(index + 1)) > history.size() - 1) {
                index = 0;
            }
            if (old == null || !old.equals(index)) {
                channel.setAttribute(HISTORY_INDEX_KEY, index);
                String value2 = (String)history.get(index);
                if (old != null && old >= 0 && old < history.size()) {
                    int i;
                    String ov = (String)history.get(old);
                    StringBuilder buf = new StringBuilder();
                    for (i = 0; i < ov.length(); ++i) {
                        buf.append("\b");
                    }
                    for (i = 0; i < ov.length(); ++i) {
                        buf.append(" ");
                    }
                    for (i = 0; i < ov.length(); ++i) {
                        buf.append("\b");
                    }
                    value2 = buf.toString() + value2;
                }
                try {
                    channel.send(value2);
                }
                catch (RemotingException e) {
                    throw new IOException(StringUtils.toString(e));
                }
            }
            return Codec2.DecodeResult.NEED_MORE_INPUT;
        }
        for (Object command : EXIT) {
            if (!TelnetCodec.isEquals(message, (byte[])command)) continue;
            if (logger.isInfoEnabled()) {
                logger.info(new Exception("Close channel " + channel + " on exit command " + command));
            }
            channel.close();
            return null;
        }
        byte[] enter = null;
        for (Object command : ENTER) {
            if (!TelnetCodec.endsWith(message, (byte[])command)) continue;
            enter = (byte[])command;
            break;
        }
        if (enter == null) {
            return Codec2.DecodeResult.NEED_MORE_INPUT;
        }
        LinkedList<String> history = (LinkedList<String>)channel.getAttribute(HISTORY_LIST_KEY);
        Integer index = (Integer)channel.getAttribute(HISTORY_INDEX_KEY);
        channel.removeAttribute(HISTORY_INDEX_KEY);
        if (history != null && history.size() > 0 && index != null && index >= 0 && index < history.size() && (value = (String)history.get(index)) != null) {
            byte[] b1 = value.getBytes();
            if (message != null && message.length > 0) {
                byte[] b2 = new byte[b1.length + message.length];
                System.arraycopy(b1, 0, b2, 0, b1.length);
                System.arraycopy(message, 0, b2, b1.length, message.length);
                message = b2;
            } else {
                message = b1;
            }
        }
        if ((result = TelnetCodec.toString(message, TelnetCodec.getCharset(channel))) != null && result.trim().length() > 0) {
            if (history == null) {
                history = new LinkedList<String>();
                channel.setAttribute(HISTORY_LIST_KEY, history);
            }
            if (history.size() == 0) {
                history.addLast(result);
            } else if (!result.equals(history.getLast())) {
                history.remove(result);
                history.addLast(result);
                if (history.size() > 10) {
                    history.removeFirst();
                }
            }
        }
        return result;
    }

    private static Charset getCharset(Channel channel) {
        if (channel != null) {
            String parameter;
            Object attribute = channel.getAttribute("charset");
            if (attribute instanceof String) {
                try {
                    return Charset.forName((String)attribute);
                }
                catch (Throwable t) {
                    logger.warn(t.getMessage(), t);
                }
            } else if (attribute instanceof Charset) {
                return (Charset)attribute;
            }
            URL url = channel.getUrl();
            if (url != null && (parameter = url.getParameter("charset")) != null && parameter.length() > 0) {
                try {
                    return Charset.forName(parameter);
                }
                catch (Throwable t) {
                    logger.warn(t.getMessage(), t);
                }
            }
        }
        try {
            return Charset.forName("GBK");
        }
        catch (Throwable t) {
            logger.warn(t.getMessage(), t);
            return Charset.defaultCharset();
        }
    }

    private static String toString(byte[] message, Charset charset) throws UnsupportedEncodingException {
        byte[] copy = new byte[message.length];
        int index = 0;
        for (int i = 0; i < message.length; ++i) {
            byte b = message[i];
            if (b == 8) {
                if (index > 0) {
                    --index;
                }
                if (i <= 2 || message[i - 2] >= 0 || index <= 0) continue;
                --index;
                continue;
            }
            if (b == 27) {
                if (i < message.length - 4 && message[i + 4] == 126) {
                    i += 4;
                    continue;
                }
                if (i < message.length - 3 && message[i + 3] == 126) {
                    i += 3;
                    continue;
                }
                if (i >= message.length - 2) continue;
                i += 2;
                continue;
            }
            if (b == -1 && i < message.length - 2 && (message[i + 1] == -3 || message[i + 1] == -5)) {
                i += 2;
                continue;
            }
            copy[index++] = message[i];
        }
        if (index == 0) {
            return "";
        }
        return new String(copy, 0, index, charset.name()).trim();
    }

    private static boolean isEquals(byte[] message, byte[] command) throws IOException {
        return message.length == command.length && TelnetCodec.endsWith(message, command);
    }

    private static boolean endsWith(byte[] message, byte[] command) throws IOException {
        if (message.length < command.length) {
            return false;
        }
        int offset = message.length - command.length;
        for (int i = command.length - 1; i >= 0; --i) {
            if (message[offset + i] == command[i]) continue;
            return false;
        }
        return true;
    }
}

