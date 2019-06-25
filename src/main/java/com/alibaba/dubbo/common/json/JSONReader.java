/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.json.JSONToken;
import com.alibaba.dubbo.common.json.ParseException;
import com.alibaba.dubbo.common.json.Yylex;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class JSONReader {
    private static ThreadLocal<Yylex> LOCAL_LEXER = new ThreadLocal<Yylex>(){};
    private Yylex mLex;

    public JSONReader(InputStream is, String charset) throws UnsupportedEncodingException {
        this(new InputStreamReader(is, charset));
    }

    public JSONReader(Reader reader) {
        this.mLex = JSONReader.getLexer(reader);
    }

    public JSONToken nextToken() throws IOException, ParseException {
        return this.mLex.yylex();
    }

    public JSONToken nextToken(int expect) throws IOException, ParseException {
        JSONToken ret = this.mLex.yylex();
        if (ret == null) {
            throw new ParseException("EOF error.");
        }
        if (expect != 0 && expect != ret.type) {
            throw new ParseException("Unexcepted token.");
        }
        return ret;
    }

    private static Yylex getLexer(Reader reader) {
        Yylex ret = LOCAL_LEXER.get();
        if (ret == null) {
            ret = new Yylex(reader);
            LOCAL_LEXER.set(ret);
        } else {
            ret.yyreset(reader);
        }
        return ret;
    }

}

