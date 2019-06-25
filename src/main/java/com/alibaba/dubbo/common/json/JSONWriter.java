/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.utils.Stack;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class JSONWriter {
    private static final byte UNKNOWN = 0;
    private static final byte ARRAY = 1;
    private static final byte OBJECT = 2;
    private static final byte OBJECT_VALUE = 3;
    private Writer mWriter;
    private State mState = new State(0);
    private Stack<State> mStack = new Stack();
    private static final String[] CONTROL_CHAR_MAP = new String[]{"\\u0000", "\\u0001", "\\u0002", "\\u0003", "\\u0004", "\\u0005", "\\u0006", "\\u0007", "\\b", "\\t", "\\n", "\\u000b", "\\f", "\\r", "\\u000e", "\\u000f", "\\u0010", "\\u0011", "\\u0012", "\\u0013", "\\u0014", "\\u0015", "\\u0016", "\\u0017", "\\u0018", "\\u0019", "\\u001a", "\\u001b", "\\u001c", "\\u001d", "\\u001e", "\\u001f"};

    public JSONWriter(Writer writer) {
        this.mWriter = writer;
    }

    public JSONWriter(OutputStream is, String charset) throws UnsupportedEncodingException {
        this.mWriter = new OutputStreamWriter(is, charset);
    }

    public JSONWriter objectBegin() throws IOException {
        this.beforeValue();
        this.mWriter.write(123);
        this.mStack.push(this.mState);
        this.mState = new State(2);
        return this;
    }

    public JSONWriter objectEnd() throws IOException {
        this.mWriter.write(125);
        this.mState = this.mStack.pop();
        return this;
    }

    public JSONWriter objectItem(String name) throws IOException {
        this.beforeObjectItem();
        this.mWriter.write(34);
        this.mWriter.write(JSONWriter.escape(name));
        this.mWriter.write(34);
        this.mWriter.write(58);
        return this;
    }

    public JSONWriter arrayBegin() throws IOException {
        this.beforeValue();
        this.mWriter.write(91);
        this.mStack.push(this.mState);
        this.mState = new State(1);
        return this;
    }

    public JSONWriter arrayEnd() throws IOException {
        this.mWriter.write(93);
        this.mState = this.mStack.pop();
        return this;
    }

    public JSONWriter valueNull() throws IOException {
        this.beforeValue();
        this.mWriter.write("null");
        return this;
    }

    public JSONWriter valueString(String value) throws IOException {
        this.beforeValue();
        this.mWriter.write(34);
        this.mWriter.write(JSONWriter.escape(value));
        this.mWriter.write(34);
        return this;
    }

    public JSONWriter valueBoolean(boolean value) throws IOException {
        this.beforeValue();
        this.mWriter.write(value ? "true" : "false");
        return this;
    }

    public JSONWriter valueInt(int value) throws IOException {
        this.beforeValue();
        this.mWriter.write(String.valueOf(value));
        return this;
    }

    public JSONWriter valueLong(long value) throws IOException {
        this.beforeValue();
        this.mWriter.write(String.valueOf(value));
        return this;
    }

    public JSONWriter valueFloat(float value) throws IOException {
        this.beforeValue();
        this.mWriter.write(String.valueOf(value));
        return this;
    }

    public JSONWriter valueDouble(double value) throws IOException {
        this.beforeValue();
        this.mWriter.write(String.valueOf(value));
        return this;
    }

    private void beforeValue() throws IOException {
        switch (this.mState.type) {
            case 1: {
                if (this.mState.itemCount++ > 0) {
                    this.mWriter.write(44);
                }
                return;
            }
            case 2: {
                throw new IOException("Must call objectItem first.");
            }
            case 3: {
                this.mState.type = (byte)2;
                return;
            }
        }
    }

    private void beforeObjectItem() throws IOException {
        switch (this.mState.type) {
            case 3: {
                this.mWriter.write("null");
            }
            case 2: {
                this.mState.type = (byte)3;
                if (this.mState.itemCount++ > 0) {
                    this.mWriter.write(44);
                }
                return;
            }
        }
        throw new IOException("Must call objectBegin first.");
    }

    private static String escape(String str) {
        if (str == null) {
            return str;
        }
        int len = str.length();
        if (len == 0) {
            return str;
        }
        StringBuilder sb = null;
        block3 : for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            if (c < ' ') {
                if (sb == null) {
                    sb = new StringBuilder(len << 1);
                    sb.append(str, 0, i);
                }
                sb.append(CONTROL_CHAR_MAP[c]);
                continue;
            }
            switch (c) {
                case '\"': 
                case '/': 
                case '\\': {
                    if (sb == null) {
                        sb = new StringBuilder(len << 1);
                        sb.append(str, 0, i);
                    }
                    sb.append('\\').append(c);
                    continue block3;
                }
                default: {
                    if (sb == null) continue block3;
                    sb.append(c);
                }
            }
        }
        return sb == null ? str : sb.toString();
    }

    private static class State {
        private byte type;
        private int itemCount = 0;

        State(byte t) {
            this.type = t;
        }
    }

}

