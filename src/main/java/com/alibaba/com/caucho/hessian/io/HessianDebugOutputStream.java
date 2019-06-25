/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import com.alibaba.com.caucho.hessian.io.HessianDebugState;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HessianDebugOutputStream
extends OutputStream {
    private OutputStream _os;
    private HessianDebugState _state;

    public HessianDebugOutputStream(OutputStream os, PrintWriter dbg) {
        this._os = os;
        this._state = new HessianDebugState(dbg);
    }

    public HessianDebugOutputStream(OutputStream os, Logger log, Level level) {
        this(os, new PrintWriter(new LogWriter(log, level)));
    }

    public void startTop2() {
        this._state.startTop2();
    }

    @Override
    public void write(int ch) throws IOException {
        this._os.write(ch &= 255);
        this._state.next(ch);
    }

    @Override
    public void flush() throws IOException {
        this._os.flush();
    }

    @Override
    public void close() throws IOException {
        OutputStream os = this._os;
        this._os = null;
        if (os != null) {
            os.close();
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

