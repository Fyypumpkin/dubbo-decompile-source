/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class URL
implements Serializable {
    private static final long serialVersionUID = -1985165475234910535L;
    private final String protocol;
    private final String username;
    private final String password;
    private final String host;
    private final int port;
    private final String path;
    private final Map<String, String> parameters;
    private volatile transient Map<String, Number> numbers;
    private volatile transient Map<String, URL> urls;
    private volatile transient String ip;
    private volatile transient String full;
    private volatile transient String identity;
    private volatile transient String parameter;
    private volatile transient String string;

    protected URL() {
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }

    public URL(String protocol, String host, int port) {
        this(protocol, null, null, host, port, null, (Map<String, String>)null);
    }

    public URL(String protocol, String host, int port, String[] pairs) {
        this(protocol, null, null, host, port, null, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    public URL(String protocol, String host, int port, String path) {
        this(protocol, null, null, host, port, path, (Map<String, String>)null);
    }

    public /* varargs */ URL(String protocol, String host, int port, String path, String ... pairs) {
        this(protocol, null, null, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    public URL(String protocol, String username, String password, String host, int port, String path) {
        this(protocol, username, password, host, port, path, (Map<String, String>)null);
    }

    public /* varargs */ URL(String protocol, String username, String password, String host, int port, String path, String ... pairs) {
        this(protocol, username, password, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public URL(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {
        if ((username == null || username.length() == 0) && password != null && password.length() > 0) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port < 0 ? 0 : port;
        this.path = path;
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        parameters = parameters == null ? new HashMap<String, String>() : new HashMap<String, String>(parameters);
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public static URL valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        HashMap<String, String> parameters = null;
        int i = url.indexOf("?");
        if (i >= 0) {
            String[] parts = url.substring(i + 1).split("\\&");
            parameters = new HashMap<String, String>();
            for (String part : parts) {
                if ((part = part.trim()).length() <= 0) continue;
                int j = part.indexOf(61);
                if (j >= 0) {
                    parameters.put(part.substring(0, j), part.substring(j + 1));
                    continue;
                }
                parameters.put(part, part);
            }
            url = url.substring(0, i);
        }
        if ((i = url.indexOf("://")) >= 0) {
            if (i == 0) {
                throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            }
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else {
            i = url.indexOf(":/");
            if (i >= 0) {
                if (i == 0) {
                    throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                }
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }
        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }
        if ((i = url.indexOf("@")) >= 0) {
            username = url.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            url = url.substring(i + 1);
        }
        if ((i = url.indexOf(":")) >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        if (url.length() > 0) {
            host = url;
        }
        return new URL(protocol, username, password, host, port, path, parameters);
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getAuthority() {
        if (!(this.username != null && this.username.length() != 0 || this.password != null && this.password.length() != 0)) {
            return null;
        }
        return (this.username == null ? "" : this.username) + ":" + (this.password == null ? "" : this.password);
    }

    public String getHost() {
        return this.host;
    }

    public String getIp() {
        if (this.ip == null) {
            this.ip = NetUtils.getIpByHost(this.host);
        }
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    public int getPort(int defaultPort) {
        return this.port <= 0 ? defaultPort : this.port;
    }

    public String getAddress() {
        return this.port <= 0 ? this.host : this.host + ":" + this.port;
    }

    public String getBackupAddress() {
        return this.getBackupAddress(0);
    }

    public String getBackupAddress(int defaultPort) {
        StringBuilder address = new StringBuilder(this.appendDefaultPort(this.getAddress(), defaultPort));
        String[] backups = this.getParameter("backup", new String[0]);
        if (backups != null && backups.length > 0) {
            for (String backup : backups) {
                address.append(",");
                address.append(this.appendDefaultPort(backup, defaultPort));
            }
        }
        return address.toString();
    }

    public List<URL> getBackupUrls() {
        ArrayList<URL> urls = new ArrayList<URL>();
        urls.add(this);
        String[] backups = this.getParameter("backup", new String[0]);
        if (backups != null && backups.length > 0) {
            for (String backup : backups) {
                urls.add(this.setAddress(backup));
            }
        }
        return urls;
    }

    private String appendDefaultPort(String address, int defaultPort) {
        if (address != null && address.length() > 0 && defaultPort > 0) {
            int i = address.indexOf(58);
            if (i < 0) {
                return address + ":" + defaultPort;
            }
            if (Integer.parseInt(address.substring(i + 1)) == 0) {
                return address.substring(0, i + 1) + defaultPort;
            }
        }
        return address;
    }

    public String getPath() {
        return this.path;
    }

    public String getAbsolutePath() {
        if (this.path != null && !this.path.startsWith("/")) {
            return "/" + this.path;
        }
        return this.path;
    }

    public URL setProtocol(String protocol) {
        return new URL(protocol, this.username, this.password, this.host, this.port, this.path, this.getParameters());
    }

    public URL setUsername(String username) {
        return new URL(this.protocol, username, this.password, this.host, this.port, this.path, this.getParameters());
    }

    public URL setPassword(String password) {
        return new URL(this.protocol, this.username, password, this.host, this.port, this.path, this.getParameters());
    }

    public URL setAddress(String address) {
        String host;
        int i = address.lastIndexOf(58);
        int port = this.port;
        if (i >= 0) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
        }
        return new URL(this.protocol, this.username, this.password, host, port, this.path, this.getParameters());
    }

    public URL setHost(String host) {
        return new URL(this.protocol, this.username, this.password, host, this.port, this.path, this.getParameters());
    }

    public URL setPort(int port) {
        return new URL(this.protocol, this.username, this.password, this.host, port, this.path, this.getParameters());
    }

    public URL setPath(String path) {
        return new URL(this.protocol, this.username, this.password, this.host, this.port, path, this.getParameters());
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public String getParameterAndDecoded(String key) {
        return this.getParameterAndDecoded(key, null);
    }

    public String getParameterAndDecoded(String key, String defaultValue) {
        return URL.decode(this.getParameter(key, defaultValue));
    }

    public String getParameter(String key) {
        String value = this.parameters.get(key);
        if (value == null || value.length() == 0) {
            value = this.parameters.get("default." + key);
        }
        return value;
    }

    public String getParameter(String key, String defaultValue) {
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public String[] getParameter(String key, String[] defaultValue) {
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Constants.COMMA_SPLIT_PATTERN.split(value);
    }

    private Map<String, Number> getNumbers() {
        if (this.numbers == null) {
            this.numbers = new ConcurrentHashMap<String, Number>();
        }
        return this.numbers;
    }

    private Map<String, URL> getUrls() {
        if (this.urls == null) {
            this.urls = new ConcurrentHashMap<String, URL>();
        }
        return this.urls;
    }

    public URL getUrlParameter(String key) {
        URL u = this.getUrls().get(key);
        if (u != null) {
            return u;
        }
        String value = this.getParameterAndDecoded(key);
        if (value == null || value.length() == 0) {
            return null;
        }
        u = URL.valueOf(value);
        this.getUrls().put(key, u);
        return u;
    }

    public double getParameter(String key, double defaultValue) {
        Number n = this.getNumbers().get(key);
        if (n != null) {
            return n.doubleValue();
        }
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        double d = Double.parseDouble(value);
        this.getNumbers().put(key, d);
        return d;
    }

    public float getParameter(String key, float defaultValue) {
        Number n = this.getNumbers().get(key);
        if (n != null) {
            return n.floatValue();
        }
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        this.getNumbers().put(key, Float.valueOf(f));
        return f;
    }

    public long getParameter(String key, long defaultValue) {
        Number n = this.getNumbers().get(key);
        if (n != null) {
            return n.longValue();
        }
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        this.getNumbers().put(key, l);
        return l;
    }

    public int getParameter(String key, int defaultValue) {
        Number n = this.getNumbers().get(key);
        if (n != null) {
            return n.intValue();
        }
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        this.getNumbers().put(key, i);
        return i;
    }

    public short getParameter(String key, short defaultValue) {
        Number n = this.getNumbers().get(key);
        if (n != null) {
            return n.shortValue();
        }
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        short s = Short.parseShort(value);
        this.getNumbers().put(key, s);
        return s;
    }

    public byte getParameter(String key, byte defaultValue) {
        Number n = this.getNumbers().get(key);
        if (n != null) {
            return n.byteValue();
        }
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        byte b = Byte.parseByte(value);
        this.getNumbers().put(key, b);
        return b;
    }

    public float getPositiveParameter(String key, float defaultValue) {
        if (defaultValue <= 0.0f) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        float value = this.getParameter(key, defaultValue);
        if (value <= 0.0f) {
            return defaultValue;
        }
        return value;
    }

    public double getPositiveParameter(String key, double defaultValue) {
        if (defaultValue <= 0.0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        double value = this.getParameter(key, defaultValue);
        if (value <= 0.0) {
            return defaultValue;
        }
        return value;
    }

    public long getPositiveParameter(String key, long defaultValue) {
        if (defaultValue <= 0L) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        long value = this.getParameter(key, defaultValue);
        if (value <= 0L) {
            return defaultValue;
        }
        return value;
    }

    public int getPositiveParameter(String key, int defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        int value = this.getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public short getPositiveParameter(String key, short defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        short value = this.getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public byte getPositiveParameter(String key, byte defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        byte value = this.getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public char getParameter(String key, char defaultValue) {
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    public boolean getParameter(String key, boolean defaultValue) {
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean hasParameter(String key) {
        String value = this.getParameter(key);
        return value != null && value.length() > 0;
    }

    public String getMethodParameterAndDecoded(String method, String key) {
        return URL.decode(this.getMethodParameter(method, key));
    }

    public String getMethodParameterAndDecoded(String method, String key, String defaultValue) {
        return URL.decode(this.getMethodParameter(method, key, defaultValue));
    }

    public String getMethodParameter(String method, String key) {
        String value = this.parameters.get(method + "." + key);
        if (value == null || value.length() == 0) {
            return this.getParameter(key);
        }
        return value;
    }

    public String getMethodParameter(String method, String key, String defaultValue) {
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public double getMethodParameter(String method, String key, double defaultValue) {
        String methodKey = method + "." + key;
        Number n = this.getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        double d = Double.parseDouble(value);
        this.getNumbers().put(methodKey, d);
        return d;
    }

    public float getMethodParameter(String method, String key, float defaultValue) {
        String methodKey = method + "." + key;
        Number n = this.getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        this.getNumbers().put(methodKey, Float.valueOf(f));
        return f;
    }

    public long getMethodParameter(String method, String key, long defaultValue) {
        String methodKey = method + "." + key;
        Number n = this.getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        this.getNumbers().put(methodKey, l);
        return l;
    }

    public int getMethodParameter(String method, String key, int defaultValue) {
        String methodKey = method + "." + key;
        Number n = this.getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        this.getNumbers().put(methodKey, i);
        return i;
    }

    public short getMethodParameter(String method, String key, short defaultValue) {
        String methodKey = method + "." + key;
        Number n = this.getNumbers().get(methodKey);
        if (n != null) {
            return n.shortValue();
        }
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        short s = Short.parseShort(value);
        this.getNumbers().put(methodKey, s);
        return s;
    }

    public byte getMethodParameter(String method, String key, byte defaultValue) {
        String methodKey = method + "." + key;
        Number n = this.getNumbers().get(methodKey);
        if (n != null) {
            return n.byteValue();
        }
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        byte b = Byte.parseByte(value);
        this.getNumbers().put(methodKey, b);
        return b;
    }

    public double getMethodPositiveParameter(String method, String key, double defaultValue) {
        if (defaultValue <= 0.0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        double value = this.getMethodParameter(method, key, defaultValue);
        if (value <= 0.0) {
            return defaultValue;
        }
        return value;
    }

    public float getMethodPositiveParameter(String method, String key, float defaultValue) {
        if (defaultValue <= 0.0f) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        float value = this.getMethodParameter(method, key, defaultValue);
        if (value <= 0.0f) {
            return defaultValue;
        }
        return value;
    }

    public long getMethodPositiveParameter(String method, String key, long defaultValue) {
        if (defaultValue <= 0L) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        long value = this.getMethodParameter(method, key, defaultValue);
        if (value <= 0L) {
            return defaultValue;
        }
        return value;
    }

    public int getMethodPositiveParameter(String method, String key, int defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        int value = this.getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public short getMethodPositiveParameter(String method, String key, short defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        short value = this.getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public byte getMethodPositiveParameter(String method, String key, byte defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        byte value = this.getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public char getMethodParameter(String method, String key, char defaultValue) {
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    public boolean getMethodParameter(String method, String key, boolean defaultValue) {
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean hasMethodParameter(String method, String key) {
        if (method == null) {
            String suffix = "." + key;
            for (String fullKey : this.parameters.keySet()) {
                if (!fullKey.endsWith(suffix)) continue;
                return true;
            }
            return false;
        }
        if (key == null) {
            String prefix = method + ".";
            for (String fullKey : this.parameters.keySet()) {
                if (!fullKey.startsWith(prefix)) continue;
                return true;
            }
            return false;
        }
        String value = this.getMethodParameter(method, key);
        return value != null && value.length() > 0;
    }

    public boolean isLocalHost() {
        return NetUtils.isLocalHost(this.host) || this.getParameter("localhost", false);
    }

    public boolean isAnyHost() {
        return "0.0.0.0".equals(this.host) || this.getParameter("anyhost", false);
    }

    public URL addParameterAndEncoded(String key, String value) {
        if (value == null || value.length() == 0) {
            return this;
        }
        return this.addParameter(key, URL.encode(value));
    }

    public URL addParameter(String key, boolean value) {
        return this.addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, char value) {
        return this.addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, byte value) {
        return this.addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, short value) {
        return this.addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, int value) {
        return this.addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, long value) {
        return this.addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, float value) {
        return this.addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, double value) {
        return this.addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, Enum<?> value) {
        if (value == null) {
            return this;
        }
        return this.addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, Number value) {
        if (value == null) {
            return this;
        }
        return this.addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, CharSequence value) {
        if (value == null || value.length() == 0) {
            return this;
        }
        return this.addParameter(key, String.valueOf(value));
    }

    public URL addParameter(String key, String value) {
        if (key == null || key.length() == 0 || value == null || value.length() == 0) {
            return this;
        }
        if (value.equals(this.getParameters().get(key))) {
            return this;
        }
        HashMap<String, String> map = new HashMap<String, String>(this.getParameters());
        map.put(key, value);
        return new URL(this.protocol, this.username, this.password, this.host, this.port, this.path, map);
    }

    public URL addParameterIfAbsent(String key, String value) {
        if (key == null || key.length() == 0 || value == null || value.length() == 0) {
            return this;
        }
        if (this.hasParameter(key)) {
            return this;
        }
        HashMap<String, String> map = new HashMap<String, String>(this.getParameters());
        map.put(key, value);
        return new URL(this.protocol, this.username, this.password, this.host, this.port, this.path, map);
    }

    public URL addParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }
        boolean hasAndEqual = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String value = this.getParameters().get(entry.getKey());
            if ((value != null || entry.getValue() == null) && value.equals(entry.getValue())) continue;
            hasAndEqual = false;
            break;
        }
        if (hasAndEqual) {
            return this;
        }
        HashMap<String, String> map = new HashMap<String, String>(this.getParameters());
        map.putAll(parameters);
        return new URL(this.protocol, this.username, this.password, this.host, this.port, this.path, map);
    }

    public URL addParametersIfAbsent(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }
        HashMap<String, String> map = new HashMap<String, String>(parameters);
        map.putAll(this.getParameters());
        return new URL(this.protocol, this.username, this.password, this.host, this.port, this.path, map);
    }

    public /* varargs */ URL addParameters(String ... pairs) {
        if (pairs == null || pairs.length == 0) {
            return this;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        HashMap<String, String> map = new HashMap<String, String>();
        int len = pairs.length / 2;
        for (int i = 0; i < len; ++i) {
            map.put(pairs[2 * i], pairs[2 * i + 1]);
        }
        return this.addParameters(map);
    }

    public URL addParameterString(String query) {
        if (query == null || query.length() == 0) {
            return this;
        }
        return this.addParameters(StringUtils.parseQueryString(query));
    }

    public URL removeParameter(String key) {
        if (key == null || key.length() == 0) {
            return this;
        }
        return this.removeParameters(key);
    }

    public URL removeParameters(Collection<String> keys) {
        if (keys == null || keys.size() == 0) {
            return this;
        }
        return this.removeParameters(keys.toArray(new String[0]));
    }

    public /* varargs */ URL removeParameters(String ... keys) {
        if (keys == null || keys.length == 0) {
            return this;
        }
        HashMap<String, String> map = new HashMap<String, String>(this.getParameters());
        for (String key : keys) {
            map.remove(key);
        }
        if (map.size() == this.getParameters().size()) {
            return this;
        }
        return new URL(this.protocol, this.username, this.password, this.host, this.port, this.path, map);
    }

    public URL clearParameters() {
        return new URL(this.protocol, this.username, this.password, this.host, this.port, this.path, new HashMap<String, String>());
    }

    public String getRawParameter(String key) {
        if ("protocol".equals(key)) {
            return this.protocol;
        }
        if ("username".equals(key)) {
            return this.username;
        }
        if ("password".equals(key)) {
            return this.password;
        }
        if ("host".equals(key)) {
            return this.host;
        }
        if ("port".equals(key)) {
            return String.valueOf(this.port);
        }
        if ("path".equals(key)) {
            return this.path;
        }
        return this.getParameter(key);
    }

    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<String, String>(this.parameters);
        if (this.protocol != null) {
            map.put("protocol", this.protocol);
        }
        if (this.username != null) {
            map.put("username", this.username);
        }
        if (this.password != null) {
            map.put("password", this.password);
        }
        if (this.host != null) {
            map.put("host", this.host);
        }
        if (this.port > 0) {
            map.put("port", String.valueOf(this.port));
        }
        if (this.path != null) {
            map.put("path", this.path);
        }
        return map;
    }

    public String toString() {
        if (this.string != null) {
            return this.string;
        }
        this.string = this.buildString(false, true, new String[0]);
        return this.string;
    }

    public /* varargs */ String toString(String ... parameters) {
        return this.buildString(false, true, parameters);
    }

    public String toIdentityString() {
        if (this.identity != null) {
            return this.identity;
        }
        this.identity = this.buildString(true, false, new String[0]);
        return this.identity;
    }

    public /* varargs */ String toIdentityString(String ... parameters) {
        return this.buildString(true, false, parameters);
    }

    public String toFullString() {
        if (this.full != null) {
            return this.full;
        }
        this.full = this.buildString(true, true, new String[0]);
        return this.full;
    }

    public /* varargs */ String toFullString(String ... parameters) {
        return this.buildString(true, true, parameters);
    }

    public String toParameterString() {
        if (this.parameter != null) {
            return this.parameter;
        }
        this.parameter = this.toParameterString(new String[0]);
        return this.parameter;
    }

    public /* varargs */ String toParameterString(String ... parameters) {
        StringBuilder buf = new StringBuilder();
        this.buildParameters(buf, false, parameters);
        return buf.toString();
    }

    private void buildParameters(StringBuilder buf, boolean concat, String[] parameters) {
        if (this.getParameters() != null && this.getParameters().size() > 0) {
            List<String> includes = parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters);
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(this.getParameters()).entrySet()) {
                if (entry.getKey() == null || entry.getKey().length() <= 0 || includes != null && !includes.contains(entry.getKey())) continue;
                if (first) {
                    if (concat) {
                        buf.append("?");
                    }
                    first = false;
                } else {
                    buf.append("&");
                }
                buf.append(entry.getKey());
                buf.append("=");
                buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
            }
        }
    }

    private /* varargs */ String buildString(boolean appendUser, boolean appendParameter, String ... parameters) {
        return this.buildString(appendUser, appendParameter, false, false, parameters);
    }

    private /* varargs */ String buildString(boolean appendUser, boolean appendParameter, boolean useIP, boolean useService, String ... parameters) {
        String host;
        String path;
        StringBuilder buf = new StringBuilder();
        if (this.protocol != null && this.protocol.length() > 0) {
            buf.append(this.protocol);
            buf.append("://");
        }
        if (appendUser && this.username != null && this.username.length() > 0) {
            buf.append(this.username);
            if (this.password != null && this.password.length() > 0) {
                buf.append(":");
                buf.append(this.password);
            }
            buf.append("@");
        }
        if ((host = useIP ? this.getIp() : this.getHost()) != null && host.length() > 0) {
            buf.append(host);
            if (this.port > 0) {
                buf.append(":");
                buf.append(this.port);
            }
        }
        if ((path = useService ? this.getServiceKey() : this.getPath()) != null && path.length() > 0) {
            buf.append("/");
            buf.append(path);
        }
        if (appendParameter) {
            this.buildParameters(buf, true, parameters);
        }
        return buf.toString();
    }

    public java.net.URL toJavaURL() {
        try {
            return new java.net.URL(this.toString());
        }
        catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(this.host, this.port);
    }

    public String getServiceKey() {
        String inf = this.getServiceInterface();
        if (inf == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        String group = this.getParameter("group");
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(inf);
        String version = this.getParameter("version");
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }

    public String toServiceStringWithoutResolving() {
        return this.buildString(true, false, false, true, new String[0]);
    }

    public String toServiceString() {
        return this.buildString(true, false, true, true, new String[0]);
    }

    @Deprecated
    public String getServiceName() {
        return this.getServiceInterface();
    }

    public String getServiceInterface() {
        return this.getParameter("interface", this.path);
    }

    public URL setServiceInterface(String service) {
        return this.addParameter("interface", service);
    }

    @Deprecated
    public int getIntParameter(String key) {
        return this.getParameter(key, 0);
    }

    @Deprecated
    public int getIntParameter(String key, int defaultValue) {
        return this.getParameter(key, defaultValue);
    }

    @Deprecated
    public int getPositiveIntParameter(String key, int defaultValue) {
        return this.getPositiveParameter(key, defaultValue);
    }

    @Deprecated
    public boolean getBooleanParameter(String key) {
        return this.getParameter(key, false);
    }

    @Deprecated
    public boolean getBooleanParameter(String key, boolean defaultValue) {
        return this.getParameter(key, defaultValue);
    }

    @Deprecated
    public int getMethodIntParameter(String method, String key) {
        return this.getMethodParameter(method, key, 0);
    }

    @Deprecated
    public int getMethodIntParameter(String method, String key, int defaultValue) {
        return this.getMethodParameter(method, key, defaultValue);
    }

    @Deprecated
    public int getMethodPositiveIntParameter(String method, String key, int defaultValue) {
        return this.getMethodPositiveParameter(method, key, defaultValue);
    }

    @Deprecated
    public boolean getMethodBooleanParameter(String method, String key) {
        return this.getMethodParameter(method, key, false);
    }

    @Deprecated
    public boolean getMethodBooleanParameter(String method, String key, boolean defaultValue) {
        return this.getMethodParameter(method, key, defaultValue);
    }

    public static String encode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String decode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.host == null ? 0 : this.host.hashCode());
        result = 31 * result + (this.parameters == null ? 0 : this.parameters.hashCode());
        result = 31 * result + (this.password == null ? 0 : this.password.hashCode());
        result = 31 * result + (this.path == null ? 0 : this.path.hashCode());
        result = 31 * result + this.port;
        result = 31 * result + (this.protocol == null ? 0 : this.protocol.hashCode());
        result = 31 * result + (this.username == null ? 0 : this.username.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        URL other = (URL)obj;
        if (this.host == null ? other.host != null : !this.host.equals(other.host)) {
            return false;
        }
        if (this.parameters == null ? other.parameters != null : !this.parameters.equals(other.parameters)) {
            return false;
        }
        if (this.password == null ? other.password != null : !this.password.equals(other.password)) {
            return false;
        }
        if (this.path == null ? other.path != null : !this.path.equals(other.path)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if (this.protocol == null ? other.protocol != null : !this.protocol.equals(other.protocol)) {
            return false;
        }
        return !(this.username == null ? other.username != null : !this.username.equals(other.username));
    }
}

