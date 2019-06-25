/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.json;

import com.alibaba.dubbo.common.bytecode.Wrapper;
import com.alibaba.dubbo.common.json.GenericJSONConverter;
import com.alibaba.dubbo.common.json.J2oVisitor;
import com.alibaba.dubbo.common.json.JSONArray;
import com.alibaba.dubbo.common.json.JSONConverter;
import com.alibaba.dubbo.common.json.JSONNode;
import com.alibaba.dubbo.common.json.JSONObject;
import com.alibaba.dubbo.common.json.JSONReader;
import com.alibaba.dubbo.common.json.JSONToken;
import com.alibaba.dubbo.common.json.JSONVisitor;
import com.alibaba.dubbo.common.json.JSONWriter;
import com.alibaba.dubbo.common.json.ParseException;
import com.alibaba.dubbo.common.utils.Stack;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class JSON {
    public static final char LBRACE = '{';
    public static final char RBRACE = '}';
    public static final char LSQUARE = '[';
    public static final char RSQUARE = ']';
    public static final char COMMA = ',';
    public static final char COLON = ':';
    public static final char QUOTE = '\"';
    public static final String NULL = "null";
    static final JSONConverter DEFAULT_CONVERTER = new GenericJSONConverter();
    public static final byte END = 0;
    public static final byte START = 1;
    public static final byte OBJECT_ITEM = 2;
    public static final byte OBJECT_VALUE = 3;
    public static final byte ARRAY_ITEM = 4;

    private JSON() {
    }

    public static String json(Object obj) throws IOException {
        if (obj == null) {
            return NULL;
        }
        StringWriter sw = new StringWriter();
        try {
            JSON.json(obj, sw);
            String string = sw.getBuffer().toString();
            return string;
        }
        finally {
            sw.close();
        }
    }

    public static void json(Object obj, Writer writer) throws IOException {
        JSON.json(obj, writer, false);
    }

    public static void json(Object obj, Writer writer, boolean writeClass) throws IOException {
        if (obj == null) {
            writer.write(NULL);
        } else {
            JSON.json(obj, new JSONWriter(writer), writeClass);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String json(Object obj, String[] properties) throws IOException {
        if (obj == null) {
            return NULL;
        }
        StringWriter sw = new StringWriter();
        try {
            JSON.json(obj, properties, sw);
            String string = sw.getBuffer().toString();
            return string;
        }
        finally {
            sw.close();
        }
    }

    public static void json(Object obj, String[] properties, Writer writer) throws IOException {
        JSON.json(obj, properties, writer, false);
    }

    public static void json(Object obj, String[] properties, Writer writer, boolean writeClass) throws IOException {
        if (obj == null) {
            writer.write(NULL);
        } else {
            JSON.json(obj, properties, new JSONWriter(writer), writeClass);
        }
    }

    private static void json(Object obj, JSONWriter jb, boolean writeClass) throws IOException {
        if (obj == null) {
            jb.valueNull();
        } else {
            DEFAULT_CONVERTER.writeValue(obj, jb, writeClass);
        }
    }

    private static void json(Object obj, String[] properties, JSONWriter jb, boolean writeClass) throws IOException {
        if (obj == null) {
            jb.valueNull();
        } else {
            Wrapper wrapper = Wrapper.getWrapper(obj.getClass());
            jb.objectBegin();
            for (String prop : properties) {
                jb.objectItem(prop);
                Object value = wrapper.getPropertyValue(obj, prop);
                if (value == null) {
                    jb.valueNull();
                    continue;
                }
                DEFAULT_CONVERTER.writeValue(value, jb, writeClass);
            }
            jb.objectEnd();
        }
    }

    public static Object parse(String json) throws ParseException {
        StringReader reader = new StringReader(json);
        try {
            Object object = JSON.parse(reader);
            return object;
        }
        catch (IOException e) {
            throw new ParseException(e.getMessage());
        }
        finally {
            reader.close();
        }
    }

    public static Object parse(Reader reader) throws IOException, ParseException {
        return JSON.parse(reader, 0);
    }

    public static <T> T parse(String json, Class<T> type) throws ParseException {
        StringReader reader = new StringReader(json);
        try {
            T t = JSON.parse((Reader)reader, type);
            return t;
        }
        catch (IOException e) {
            throw new ParseException(e.getMessage());
        }
        finally {
            reader.close();
        }
    }

    public static <T> T parse(Reader reader, Class<T> type) throws IOException, ParseException {
        return (T)JSON.parse(reader, new J2oVisitor(type, DEFAULT_CONVERTER), 0);
    }

    public static Object[] parse(String json, Class<?>[] types) throws ParseException {
        StringReader reader = new StringReader(json);
        try {
            Object[] arrobject = JSON.parse((Reader)reader, types);
            return arrobject;
        }
        catch (IOException e) {
            throw new ParseException(e.getMessage());
        }
        finally {
            reader.close();
        }
    }

    public static Object[] parse(Reader reader, Class<?>[] types) throws IOException, ParseException {
        return (Object[])JSON.parse(reader, new J2oVisitor(types, DEFAULT_CONVERTER), 3);
    }

    public static Object parse(String json, JSONVisitor handler) throws ParseException {
        StringReader reader = new StringReader(json);
        try {
            Object object = JSON.parse((Reader)reader, handler);
            return object;
        }
        catch (IOException e) {
            throw new ParseException(e.getMessage());
        }
        finally {
            reader.close();
        }
    }

    public static Object parse(Reader reader, JSONVisitor handler) throws IOException, ParseException {
        return JSON.parse(reader, handler, 0);
    }

    private static Object parse(Reader reader, int expect) throws IOException, ParseException {
        JSONReader jr = new JSONReader(reader);
        JSONToken token = jr.nextToken(expect);
        int state = 1;
        Object value = null;
        Stack stack = new Stack();
        do {
            block0 : switch (state) {
                Entry entry;
                JSONNode tmp;
                case 0: {
                    throw new ParseException("JSON source format error.");
                }
                case 1: {
                    switch (token.type) {
                        case 16: 
                        case 17: 
                        case 18: 
                        case 19: 
                        case 20: {
                            state = 0;
                            value = token.value;
                            break block0;
                        }
                        case 3: {
                            state = 4;
                            value = new JSONArray();
                            break block0;
                        }
                        case 2: {
                            state = 2;
                            value = new JSONObject();
                            break block0;
                        }
                    }
                    throw new ParseException("Unexcepted token expect [ VALUE or '[' or '{' ] get '" + JSONToken.token2string(token.type) + "'");
                }
                case 4: {
                    switch (token.type) {
                        case 6: {
                            break block0;
                        }
                        case 16: 
                        case 17: 
                        case 18: 
                        case 19: 
                        case 20: {
                            ((JSONArray)value).add(token.value);
                            break block0;
                        }
                        case 5: {
                            if (stack.isEmpty()) {
                                state = 0;
                                break block0;
                            }
                            entry = (Entry)stack.pop();
                            state = entry.state;
                            value = entry.value;
                            break block0;
                        }
                        case 3: {
                            tmp = new JSONArray();
                            ((JSONArray)value).add(tmp);
                            stack.push(new Entry((byte)state, value));
                            state = 4;
                            value = tmp;
                            break block0;
                        }
                        case 2: {
                            tmp = new JSONObject();
                            ((JSONArray)value).add(tmp);
                            stack.push(new Entry((byte)state, value));
                            state = 2;
                            value = tmp;
                            break block0;
                        }
                    }
                    throw new ParseException("Unexcepted token expect [ VALUE or ',' or ']' or '[' or '{' ] get '" + JSONToken.token2string(token.type) + "'");
                }
                case 2: {
                    switch (token.type) {
                        case 6: {
                            break block0;
                        }
                        case 1: {
                            stack.push(new Entry(2, (String)token.value));
                            state = 3;
                            break block0;
                        }
                        case 16: {
                            stack.push(new Entry(2, NULL));
                            state = 3;
                            break block0;
                        }
                        case 17: 
                        case 18: 
                        case 19: 
                        case 20: {
                            stack.push(new Entry(2, token.value.toString()));
                            state = 3;
                            break block0;
                        }
                        case 4: {
                            if (stack.isEmpty()) {
                                state = 0;
                                break block0;
                            }
                            entry = (Entry)stack.pop();
                            state = entry.state;
                            value = entry.value;
                            break block0;
                        }
                    }
                    throw new ParseException("Unexcepted token expect [ IDENT or VALUE or ',' or '}' ] get '" + JSONToken.token2string(token.type) + "'");
                }
                case 3: {
                    switch (token.type) {
                        case 7: {
                            break block0;
                        }
                        case 16: 
                        case 17: 
                        case 18: 
                        case 19: 
                        case 20: {
                            ((JSONObject)value).put((String)((Entry)stack.pop()).value, token.value);
                            state = 2;
                            break block0;
                        }
                        case 3: {
                            tmp = new JSONArray();
                            ((JSONObject)value).put((String)((Entry)stack.pop()).value, tmp);
                            stack.push(new Entry(2, value));
                            state = 4;
                            value = tmp;
                            break block0;
                        }
                        case 2: {
                            tmp = new JSONObject();
                            ((JSONObject)value).put((String)((Entry)stack.pop()).value, tmp);
                            stack.push(new Entry(2, value));
                            state = 2;
                            value = tmp;
                            break block0;
                        }
                    }
                    throw new ParseException("Unexcepted token expect [ VALUE or '[' or '{' ] get '" + JSONToken.token2string(token.type) + "'");
                }
                default: {
                    throw new ParseException("Unexcepted state.");
                }
            }
        } while ((token = jr.nextToken()) != null);
        stack.clear();
        return value;
    }

    private static Object parse(Reader reader, JSONVisitor handler, int expect) throws IOException, ParseException {
        JSONReader jr = new JSONReader(reader);
        JSONToken token = jr.nextToken(expect);
        Object value = null;
        int state = 1;
        int index = 0;
        Stack states = new Stack();
        boolean pv = false;
        handler.begin();
        do {
            block0 : switch (state) {
                int[] tmp;
                case 0: {
                    throw new ParseException("JSON source format error.");
                }
                case 1: {
                    switch (token.type) {
                        case 16: {
                            value = token.value;
                            state = 0;
                            pv = true;
                            break block0;
                        }
                        case 17: {
                            value = token.value;
                            state = 0;
                            pv = true;
                            break block0;
                        }
                        case 18: {
                            value = token.value;
                            state = 0;
                            pv = true;
                            break block0;
                        }
                        case 19: {
                            value = token.value;
                            state = 0;
                            pv = true;
                            break block0;
                        }
                        case 20: {
                            value = token.value;
                            state = 0;
                            pv = true;
                            break block0;
                        }
                        case 3: {
                            handler.arrayBegin();
                            state = 4;
                            break block0;
                        }
                        case 2: {
                            handler.objectBegin();
                            state = 2;
                            break block0;
                        }
                    }
                    throw new ParseException("Unexcepted token expect [ VALUE or '[' or '{' ] get '" + JSONToken.token2string(token.type) + "'");
                }
                case 4: {
                    switch (token.type) {
                        case 6: {
                            break block0;
                        }
                        case 16: {
                            handler.arrayItem(index++);
                            handler.arrayItemValue(index, token.value, true);
                            break block0;
                        }
                        case 17: {
                            handler.arrayItem(index++);
                            handler.arrayItemValue(index, token.value, true);
                            break block0;
                        }
                        case 18: {
                            handler.arrayItem(index++);
                            handler.arrayItemValue(index, token.value, true);
                            break block0;
                        }
                        case 19: {
                            handler.arrayItem(index++);
                            handler.arrayItemValue(index, token.value, true);
                            break block0;
                        }
                        case 20: {
                            handler.arrayItem(index++);
                            handler.arrayItemValue(index, token.value, true);
                            break block0;
                        }
                        case 3: {
                            handler.arrayItem(index++);
                            states.push(new int[]{state, index});
                            index = 0;
                            state = 4;
                            handler.arrayBegin();
                            break block0;
                        }
                        case 2: {
                            handler.arrayItem(index++);
                            states.push(new int[]{state, index});
                            index = 0;
                            state = 2;
                            handler.objectBegin();
                            break block0;
                        }
                        case 5: {
                            if (states.isEmpty()) {
                                value = handler.arrayEnd(index);
                                state = 0;
                                break block0;
                            }
                            value = handler.arrayEnd(index);
                            tmp = (int[])states.pop();
                            state = tmp[0];
                            index = tmp[1];
                            switch (state) {
                                case 4: {
                                    handler.arrayItemValue(index, value, false);
                                    break block0;
                                }
                                case 2: {
                                    handler.objectItemValue(value, false);
                                }
                            }
                            break block0;
                        }
                    }
                    throw new ParseException("Unexcepted token expect [ VALUE or ',' or ']' or '[' or '{' ] get '" + JSONToken.token2string(token.type) + "'");
                }
                case 2: {
                    switch (token.type) {
                        case 6: {
                            break block0;
                        }
                        case 1: {
                            handler.objectItem((String)token.value);
                            state = 3;
                            break block0;
                        }
                        case 16: {
                            handler.objectItem(NULL);
                            state = 3;
                            break block0;
                        }
                        case 17: 
                        case 18: 
                        case 19: 
                        case 20: {
                            handler.objectItem(token.value.toString());
                            state = 3;
                            break block0;
                        }
                        case 4: {
                            if (states.isEmpty()) {
                                value = handler.objectEnd(index);
                                state = 0;
                                break block0;
                            }
                            value = handler.objectEnd(index);
                            tmp = (int[])states.pop();
                            state = tmp[0];
                            index = tmp[1];
                            switch (state) {
                                case 4: {
                                    handler.arrayItemValue(index, value, false);
                                    break block0;
                                }
                                case 2: {
                                    handler.objectItemValue(value, false);
                                }
                            }
                            break block0;
                        }
                    }
                    throw new ParseException("Unexcepted token expect [ IDENT or VALUE or ',' or '}' ] get '" + JSONToken.token2string(token.type) + "'");
                }
                case 3: {
                    switch (token.type) {
                        case 7: {
                            break block0;
                        }
                        case 16: {
                            handler.objectItemValue(token.value, true);
                            state = 2;
                            break block0;
                        }
                        case 17: {
                            handler.objectItemValue(token.value, true);
                            state = 2;
                            break block0;
                        }
                        case 18: {
                            handler.objectItemValue(token.value, true);
                            state = 2;
                            break block0;
                        }
                        case 19: {
                            handler.objectItemValue(token.value, true);
                            state = 2;
                            break block0;
                        }
                        case 20: {
                            handler.objectItemValue(token.value, true);
                            state = 2;
                            break block0;
                        }
                        case 3: {
                            states.push(new int[]{2, index});
                            index = 0;
                            state = 4;
                            handler.arrayBegin();
                            break block0;
                        }
                        case 2: {
                            states.push(new int[]{2, index});
                            index = 0;
                            state = 2;
                            handler.objectBegin();
                            break block0;
                        }
                    }
                    throw new ParseException("Unexcepted token expect [ VALUE or '[' or '{' ] get '" + JSONToken.token2string(token.type) + "'");
                }
                default: {
                    throw new ParseException("Unexcepted state.");
                }
            }
        } while ((token = jr.nextToken()) != null);
        states.clear();
        return handler.end(value, pv);
    }

    private static class Entry {
        byte state;
        Object value;

        Entry(byte s, Object v) {
            this.state = s;
            this.value = v;
        }
    }

}

