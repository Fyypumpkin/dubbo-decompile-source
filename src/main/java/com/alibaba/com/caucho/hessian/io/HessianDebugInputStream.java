/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.HessianDebugState;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HessianDebugInputStream
extends InputStream {
    private InputStream _is;
    private HessianDebugState _state;

    public HessianDebugInputStream(InputStream is, PrintWriter dbg) {
        this._is = is;
        if (dbg == null) {
            dbg = new PrintWriter(System.out);
        }
        this._state = new HessianDebugState(dbg);
    }

    public HessianDebugInputStream(InputStream is, Logger log, Level level) {
        this(is, new PrintWriter(new LogWriter(log, level)));
    }

    public void startTop2() {
        this._state.startTop2();
    }

    @Override
    public int read() throws IOException {
        InputStream is = this._is;
        if (is == null) {
            return -1;
        }
        int ch = is.read();
        this._state.next(ch);
        return ch;
    }

    @Override
    public void close() throws IOException {
        InputStream is = this._is;
        this._is = null;
        if (is != null) {
            is.close();
        }
        this._state.println();
    }

    static class LogWriter
    extends Writer {
        private Logger _log;
        private Level _level;
        private StringBuilder _sb = new StringBuilder();

        LogWriter(Logger log, Level level) {
            this._log = log;
            this._level = level;
        }

        public void write(char ch) {
            if (ch == '\n' && this._sb.length() > 0) {
                this._log.log(this._level, this._sb.toString());
                this._sb.setLength(0);
            } else {
                this._sb.append(ch);
            }
        }

        @Override
        public void write(char[] buffer, int offset, int length) {
            for (int i = 0; i < length; ++i) {
                char ch = buffer[offset + i];
                if (ch == '\n' && this._sb.length() > 0) {
                    this._log.log(this._level, this._sb.toString());
                    this._sb.setLength(0);
                    continue;
                }
                this._sb.append(ch);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }

}

