/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class Parameters {
    private final Map<String, String> parameters;
    protected static final Logger logger = LoggerFactory.getLogger(Parameters.class);

    public /* varargs */ Parameters(String ... pairs) {
        this(Parameters.toMap(pairs));
    }

    public Parameters(Map<String, String> parameters) {
        this.parameters = Collections.unmodifiableMap(parameters != null ? new HashMap<String, String>(parameters) : new HashMap(0));
    }

    private static /* varargs */ Map<String, String> toMap(String ... pairs) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        if (pairs.length > 0) {
            if (pairs.length % 2 != 0) {
                throw new IllegalArgumentException("pairs must be even.");
            }
            for (int i = 0; i < pairs.length; i += 2) {
                parameters.put(pairs[i], pairs[i + 1]);
            }
        }
        return parameters;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public <T> T getExtension(Class<T> type, String key) {
        String name = this.getParameter(key);
        return ExtensionLoader.getExtensionLoader(type).getExtension(name);
    }

    public <T> T getExtension(Class<T> type, String key, String defaultValue) {
        String name = this.getParameter(key, defaultValue);
        return ExtensionLoader.getExtensionLoader(type).getExtension(name);
    }

    public <T> T getMethodExtension(Class<T> type, String method, String key) {
        String name = this.getMethodParameter(method, key);
        return ExtensionLoader.getExtensionLoader(type).getExtension(name);
    }

    public <T> T getMethodExtension(Class<T> type, String method, String key, String defaultValue) {
        String name = this.getMethodParameter(method, key, defaultValue);
        return ExtensionLoader.getExtensionLoader(type).getExtension(name);
    }

    public String getDecodedParameter(String key) {
        return this.getDecodedParameter(key, null);
    }

    public String getDecodedParameter(String key, String defaultValue) {
        String value = this.getParameter(key, defaultValue);
        if (value != null && value.length() > 0) {
            try {
                value = URLDecoder.decode(value, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return value;
    }

    public String getParameter(String key) {
        String value = this.parameters.get(key);
        if (value == null || value.length() == 0) {
            value = this.parameters.get("." + key);
        }
        if (value == null || value.length() == 0) {
            value = this.parameters.get("default." + key);
        }
        if (value == null || value.length() == 0) {
            value = this.parameters.get(".default." + key);
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

    public int getIntParameter(String key) {
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public int getIntParameter(String key, int defaultValue) {
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public int getPositiveIntParameter(String key, int defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        if (i > 0) {
            return i;
        }
        return defaultValue;
    }

    public boolean getBooleanParameter(String key) {
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean getBooleanParameter(String key, boolean defaultValue) {
        String value = this.getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean hasParamter(String key) {
        String value = this.getParameter(key);
        return value != null && value.length() > 0;
    }

    public String getMethodParameter(String method, String key) {
        String value = this.parameters.get(method + "." + key);
        if (value == null || value.length() == 0) {
            value = this.parameters.get("." + method + "." + key);
        }
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

    public int getMethodIntParameter(String method, String key) {
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public int getMethodIntParameter(String method, String key, int defaultValue) {
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public int getMethodPositiveIntParameter(String method, String key, int defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        if (i > 0) {
            return i;
        }
        return defaultValue;
    }

    public boolean getMethodBooleanParameter(String method, String key) {
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean getMethodBooleanParameter(String method, String key, boolean defaultValue) {
        String value = this.getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean hasMethodParamter(String method, String key) {
        String value = this.getMethodParameter(method, key);
        return value != null && value.length() > 0;
    }

    public static Parameters parseParameters(String query) {
        return new Parameters(StringUtils.parseQueryString(query));
    }

    public boolean equals(Object o) {
        return this.parameters.equals(o);
    }

    public int hashCode() {
        return this.parameters.hashCode();
    }

    public String toString() {
        return StringUtils.toQueryString(this.getParameters());
    }
}

