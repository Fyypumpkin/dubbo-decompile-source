/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.io.UnsafeStringWriter;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {
    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Pattern KVP_PATTERN = Pattern.compile("([_.a-zA-Z0-9][-_.a-zA-Z0-9]*)[=](.*)");
    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

    public static boolean isBlank(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return str != null && str.length() > 0;
    }

    public static boolean isEquals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }

    public static boolean isInteger(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        return INT_PATTERN.matcher(str).matches();
    }

    public static int parseInteger(String str) {
        if (!StringUtils.isInteger(str)) {
            return 0;
        }
        return Integer.parseInt(str);
    }

    public static List<Boolean> parseBooleanArray(String str) {
        String[] subscribes;
        ArrayList<Boolean> parsed = new ArrayList<Boolean>();
        if (str == null || str.isEmpty()) {
            return parsed;
        }
        for (String subscribe : subscribes = Constants.REGISTRY_SPLIT_PATTERN.split(str)) {
            try {
                subscribe = subscribe.trim();
                parsed.add(StringUtils.isEmpty(subscribe) ? true : Boolean.parseBoolean(subscribe));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return parsed;
    }

    public static List<Set<String>> parseSetArray(String str) {
        ArrayList<Set<String>> parsed = new ArrayList<Set<String>>();
        if (str == null || str.isEmpty()) {
            return parsed;
        }
        String[] excludes = Constants.REGISTRY_SPLIT_PATTERN.split(str);
        if (excludes != null) {
            for (String exclude : excludes) {
                String[] applications = Constants.COMMA_SPLIT_PATTERN.split(exclude.replace("\uff0c", ","));
                if (applications == null) continue;
                HashSet<String> set = new HashSet<String>();
                for (String app : applications) {
                    set.add(app.trim().toLowerCase());
                }
                parsed.add(set);
            }
        }
        return parsed;
    }

    public static boolean isJavaIdentifier(String s) {
        if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (int i = 1; i < s.length(); ++i) {
            if (Character.isJavaIdentifierPart(s.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public static boolean isContains(String values, String value) {
        if (values == null || values.length() == 0) {
            return false;
        }
        return StringUtils.isContains(Constants.COMMA_SPLIT_PATTERN.split(values), value);
    }

    public static boolean isContains(String[] values, String value) {
        if (value != null && value.length() > 0 && values != null && values.length > 0) {
            for (String v : values) {
                if (!value.equals(v)) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; ++i) {
            if (Character.isDigit(str.charAt(i))) continue;
            return false;
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String toString(Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(": " + e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            String string = w.toString();
            return string;
        }
        finally {
            p.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String toString(String msg, Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        w.write(msg + "\n");
        PrintWriter p = new PrintWriter(w);
        try {
            e.printStackTrace(p);
            String string = w.toString();
            return string;
        }
        finally {
            p.close();
        }
    }

    public static String translat(String src, String from, String to) {
        if (StringUtils.isEmpty(src)) {
            return src;
        }
        StringBuilder sb = null;
        int len = src.length();
        for (int i = 0; i < len; ++i) {
            char c = src.charAt(i);
            int ix = from.indexOf(c);
            if (ix == -1) {
                if (sb == null) continue;
                sb.append(c);
                continue;
            }
            if (sb == null) {
                sb = new StringBuilder(len);
                sb.append(src, 0, i);
            }
            if (ix >= to.length()) continue;
            sb.append(to.charAt(ix));
        }
        return sb == null ? src : sb.toString();
    }

    public static String[] split(String str, char ch) {
        ArrayList<String> list = null;
        int ix = 0;
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            if (c != ch) continue;
            if (list == null) {
                list = new ArrayList<String>();
            }
            list.add(str.substring(ix, i));
            ix = i + 1;
        }
        if (ix > 0) {
            list.add(str.substring(ix));
        }
        return list == null ? EMPTY_STRING_ARRAY : list.toArray(EMPTY_STRING_ARRAY);
    }

    public static String join(String[] array) {
        if (array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String join(String[] array, char split) {
        if (array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; ++i) {
            if (i > 0) {
                sb.append(split);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

    public static String join(String[] array, String split) {
        if (array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; ++i) {
            if (i > 0) {
                sb.append(split);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

    public static String join(Collection<String> coll, String split) {
        if (coll.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String s : coll) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(split);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private static Map<String, String> parseKeyValuePair(String str, String itemSeparator) {
        String[] tmp = str.split(itemSeparator);
        HashMap<String, String> map = new HashMap<String, String>(tmp.length);
        for (int i = 0; i < tmp.length; ++i) {
            Matcher matcher = KVP_PATTERN.matcher(tmp[i]);
            if (!matcher.matches()) continue;
            map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    private static boolean parseKeyValuePair(String str, String itemSeparator, String key) {
        String[] tmp = str.split(itemSeparator);
        String defaultKey = key != null && !key.startsWith("default.") ? "default." + key : key;
        for (int i = 0; i < tmp.length; ++i) {
            String matchedKey;
            Matcher matcher = KVP_PATTERN.matcher(tmp[i]);
            if (!matcher.matches() || !(matchedKey = matcher.group(1)).equals(key) && !matchedKey.equals(defaultKey)) continue;
            return true;
        }
        return false;
    }

    public static String getQueryStringValue(String qs, String key) {
        Map<String, String> map = StringUtils.parseQueryString(qs);
        return map.get(key);
    }

    public static Map<String, String> parseQueryString(String qs) {
        if (qs == null || qs.length() == 0) {
            return new HashMap<String, String>();
        }
        return StringUtils.parseKeyValuePair(qs, "\\&");
    }

    public static boolean containsParseKey(String qs, String key) {
        if (qs == null || qs.length() == 0) {
            return false;
        }
        return StringUtils.parseKeyValuePair(qs, "\\&", key);
    }

    public static String getServiceKey(Map<String, String> ps) {
        StringBuilder buf = new StringBuilder();
        String group = ps.get("group");
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(ps.get("interface"));
        String version = ps.get("version");
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }

    public static String toQueryString(Map<String, String> ps) {
        StringBuilder buf = new StringBuilder();
        if (ps != null && ps.size() > 0) {
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(ps).entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key == null || key.length() <= 0 || value == null || value.length() <= 0) continue;
                if (buf.length() > 0) {
                    buf.append("&");
                }
                buf.append(key);
                buf.append("=");
                buf.append(value);
            }
        }
        return buf.toString();
    }

    public static String camelToSplitName(String camelName, String split) {
        if (camelName == null || camelName.length() == 0) {
            return camelName;
        }
        StringBuilder buf = null;
        for (int i = 0; i < camelName.length(); ++i) {
            char ch = camelName.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                if (buf == null) {
                    buf = new StringBuilder();
                    if (i > 0) {
                        buf.append(camelName.substring(0, i));
                    }
                }
                if (i > 0) {
                    buf.append(split);
                }
                buf.append(Character.toLowerCase(ch));
                continue;
            }
            if (buf == null) continue;
            buf.append(ch);
        }
        return buf == null ? camelName : buf.toString();
    }

    public static String toArgumentString(Object[] args) {
        StringBuilder buf = new StringBuilder();
        for (Object arg : args) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            if (arg == null || ReflectUtils.isPrimitives(arg.getClass())) {
                buf.append(arg);
                continue;
            }
            try {
                buf.append(JSON.json(arg));
            }
            catch (IOException e) {
                logger.warn(e.getMessage(), e);
                buf.append(arg);
            }
        }
        return buf.toString();
    }

    public static String nullToEmpty(String string) {
        return string == null ? "" : string;
    }

    public static String nullToEmpty(CharSequence string) {
        return string == null ? "" : string.toString();
    }

    private StringUtils() {
    }
}

