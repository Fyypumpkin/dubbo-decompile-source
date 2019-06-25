/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ClassHelper;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    private static Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\s*\\{?\\s*([\\._0-9a-zA-Z]+)\\s*\\}?");
    private static volatile Properties PROPERTIES;
    private static int PID;

    public static boolean isNotEmpty(String value) {
        return !ConfigUtils.isEmpty(value);
    }

    public static boolean isEmpty(String value) {
        return value == null || value.length() == 0 || "false".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value) || "null".equalsIgnoreCase(value) || "N/A".equalsIgnoreCase(value);
    }

    public static boolean isDefault(String value) {
        return "true".equalsIgnoreCase(value) || "default".equalsIgnoreCase(value);
    }

    public static List<String> mergeValues(Class<?> type, String cfg, List<String> def) {
        String[] configs;
        ArrayList<String> defaults = new ArrayList<String>();
        if (def != null) {
            for (String name : def) {
                if (!ExtensionLoader.getExtensionLoader(type).hasExtension(name)) continue;
                defaults.add(name);
            }
        }
        ArrayList<String> names = new ArrayList<String>();
        for (String config : configs = cfg == null || cfg.trim().length() == 0 ? new String[0] : Constants.COMMA_SPLIT_PATTERN.split(cfg)) {
            if (config == null || config.trim().length() <= 0) continue;
            names.add(config);
        }
        if (!names.contains("-default")) {
            int i = names.indexOf("default");
            if (i > 0) {
                names.addAll(i, defaults);
            } else {
                names.addAll(0, defaults);
            }
            names.remove("default");
        } else {
            names.remove("default");
        }
        for (String name : new ArrayList(names)) {
            if (!name.startsWith("-")) continue;
            names.remove(name);
            names.remove(name.substring(1));
        }
        return names;
    }

    public static String replaceProperty(String expression, Map<String, String> params) {
        if (expression == null || expression.length() == 0 || expression.indexOf(36) < 0) {
            return expression;
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(expression);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = System.getProperty(key);
            if (value == null && params != null) {
                value = params.get(key);
            }
            if (value == null) {
                value = "";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static Properties getProperties() {
        if (PROPERTIES != null) return PROPERTIES;
        Class<ConfigUtils> class_ = ConfigUtils.class;
        synchronized (ConfigUtils.class) {
            if (PROPERTIES != null) return PROPERTIES;
            {
                String path = System.getProperty("dubbo.properties.file");
                if (!(path != null && path.length() != 0 || (path = System.getenv("dubbo.properties.file")) != null && path.length() != 0)) {
                    path = "dubbo.properties";
                }
                PROPERTIES = ConfigUtils.loadProperties(path, false, true);
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return PROPERTIES;
        }
    }

    public static void setProperties(Properties properties) {
        PROPERTIES = properties;
    }

    public static void addProperties(Properties properties) {
        if (properties != null) {
            ConfigUtils.getProperties().putAll(properties);
        }
    }

    public static String getProperty(String key) {
        return ConfigUtils.getProperty(key, null);
    }

    public static String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null && value.length() > 0) {
            return value;
        }
        Properties properties = ConfigUtils.getProperties();
        return ConfigUtils.replaceProperty(properties.getProperty(key, defaultValue), properties);
    }

    public static String getSystemProperty(String key) {
        String value = System.getenv(key);
        if (value == null || value.length() == 0) {
            value = System.getProperty(key);
        }
        return value;
    }

    public static Properties loadProperties(String fileName) {
        return ConfigUtils.loadProperties(fileName, false, false);
    }

    public static Properties loadProperties(String fileName, boolean allowMultiFile) {
        return ConfigUtils.loadProperties(fileName, allowMultiFile, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Properties loadProperties(String fileName, boolean allowMultiFile, boolean optional) {
        Properties properties = new Properties();
        if (fileName.startsWith("/")) {
            try {
                FileInputStream input = new FileInputStream(fileName);
                try {
                    properties.load(input);
                }
                finally {
                    input.close();
                }
            }
            catch (Throwable e) {
                logger.warn("Failed to load " + fileName + " file from " + fileName + "(ignore this file): " + e.getMessage(), e);
            }
            return properties;
        }
        ArrayList<URL> list = new ArrayList<URL>();
        try {
            Enumeration<URL> urls = ClassHelper.getClassLoader().getResources(fileName);
            list = new ArrayList();
            while (urls.hasMoreElements()) {
                list.add(urls.nextElement());
            }
        }
        catch (Throwable t) {
            logger.warn("Fail to load " + fileName + " file: " + t.getMessage(), t);
        }
        if (list.isEmpty()) {
            if (!optional) {
                logger.warn("No " + fileName + " found on the class path.");
            }
            return properties;
        }
        if (!allowMultiFile) {
            if (list.size() > 1) {
                String errMsg = String.format("only 1 %s file is expected, but %d dubbo.properties files found on class path: %s", fileName, list.size(), ((Object)list).toString());
                logger.warn(errMsg);
            }
            try {
                properties.load(ClassHelper.getClassLoader().getResourceAsStream(fileName));
            }
            catch (Throwable e) {
                logger.warn("Failed to load " + fileName + " file from " + fileName + "(ignore this file): " + e.getMessage(), e);
            }
            return properties;
        }
        logger.info("load " + fileName + " properties file from " + list);
        for (URL url : list) {
            try {
                Properties p = new Properties();
                InputStream input = url.openStream();
                if (input == null) continue;
                try {
                    p.load(input);
                    properties.putAll(p);
                }
                finally {
                    try {
                        input.close();
                    }
                    catch (Throwable throwable) {}
                }
            }
            catch (Throwable e) {
                logger.warn("Fail to load " + fileName + " file from " + url + "(ignore this file): " + e.getMessage(), e);
            }
        }
        return properties;
    }

    public static int getPid() {
        if (PID < 0) {
            try {
                RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
                String name = runtime.getName();
                PID = Integer.parseInt(name.substring(0, name.indexOf(64)));
            }
            catch (Throwable e) {
                PID = 0;
            }
        }
        return PID;
    }

    public static int getServerShutdownTimeout() {
        int timeout = 10000;
        String value = ConfigUtils.getProperty("dubbo.service.shutdown.wait");
        if (value != null && value.length() > 0) {
            try {
                timeout = Integer.parseInt(value);
            }
            catch (Exception exception) {}
        } else {
            value = ConfigUtils.getProperty("dubbo.service.shutdown.wait.seconds");
            if (value != null && value.length() > 0) {
                try {
                    timeout = Integer.parseInt(value) * 1000;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return timeout;
    }

    private ConfigUtils() {
    }

    static {
        PID = -1;
    }
}

