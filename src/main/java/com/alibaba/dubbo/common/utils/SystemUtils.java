/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SystemUtils {
    static final Map<String, Boolean> PRIMITIVE_TYPE = new HashMap<String, Boolean>(){
        {
            this.put(Boolean.class.getName(), true);
            this.put(Character.class.getName(), true);
            this.put(Byte.class.getName(), true);
            this.put(Short.class.getName(), true);
            this.put(Integer.class.getName(), true);
            this.put(Long.class.getName(), true);
            this.put(Float.class.getName(), true);
            this.put(Double.class.getName(), true);
            this.put(Void.class.getName(), true);
        }
    };
    private static final SystemUtils DEFAULT = new SystemUtils();
    private volatile boolean initialized = false;
    private String fileName;
    private Map<String, Object> resolvedValuePair;
    private Map<String, String> unResolvedValuePair;
    public static final String DEFAULT_FILE_PATH_NAME = "/etc/profile.d/yzapp.sh";
    public static final String DEFAULT_FILE_PATH = "application.profile.resolve.path";
    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    public static String getString(String key) {
        Object value = SystemUtils.resolvedValuePair().get(key);
        if (value == null && (value = SystemUtils.find(key)) != null) {
            return (String)SystemUtils.putAndGet(key, (String)value, String.class, null, new Class[0]);
        }
        return (String)value;
    }

    public static String[] getArrayString(String key) {
        return SystemUtils.getArrayString(key, null);
    }

    public static String[] getArrayString(String key, String separator) {
        Object value = SystemUtils.resolvedValuePair().get(key);
        if (value == null && (value = SystemUtils.find(key)) != null) {
            return (String[])SystemUtils.putAndGet(key, (String)value, String[].class, separator, new Class[0]);
        }
        return (String[])value;
    }

    public static List<String> getListString(String key) {
        return SystemUtils.getListString(key, null);
    }

    public static List<String> getListString(String key, String separator) {
        Object value = SystemUtils.resolvedValuePair().get(key);
        if (value == null && (value = SystemUtils.find(key)) != null) {
            return (List)SystemUtils.putAndGet(key, (String)value, ArrayList.class, separator, String.class);
        }
        return (List)value;
    }

    public static Integer getInteger(String key) {
        Object value = SystemUtils.resolvedValuePair().get(key);
        if (value == null && (value = SystemUtils.find(key)) != null) {
            return (Integer)SystemUtils.putAndGet(key, (String)value, Integer.class, null, new Class[0]);
        }
        return (Integer)value;
    }

    public static Long getLong(String key) {
        Object value = SystemUtils.resolvedValuePair().get(key);
        if (value == null && (value = SystemUtils.find(key)) != null) {
            return (Long)SystemUtils.putAndGet(key, (String)value, Long.class, null, new Class[0]);
        }
        return (Long)value;
    }

    public static Float getFloat(String key) {
        Object value = SystemUtils.resolvedValuePair().get(key);
        if (value == null && (value = SystemUtils.find(key)) != null) {
            return (Float)SystemUtils.putAndGet(key, (String)value, Float.class, null, new Class[0]);
        }
        return (Float)value;
    }

    public static Double getDouble(String key) {
        Object value = SystemUtils.resolvedValuePair().get(key);
        if (value == null && (value = SystemUtils.find(key)) != null) {
            return (Double)SystemUtils.putAndGet(key, (String)value, Double.class, null, new Class[0]);
        }
        return (Double)value;
    }

    public static Boolean getBoolean(String key) {
        Object value = SystemUtils.resolvedValuePair().get(key);
        if (value == null && (value = SystemUtils.find(key)) != null) {
            return (Boolean)SystemUtils.putAndGet(key, (String)value, Boolean.class, null, new Class[0]);
        }
        return (Boolean)value;
    }

    public static Short getShort(String key) {
        Object value = SystemUtils.resolvedValuePair().get(key);
        if (value == null && (value = SystemUtils.find(key)) != null) {
            return (Short)SystemUtils.putAndGet(key, (String)value, Short.class, null, new Class[0]);
        }
        return (Short)value;
    }

    public static void clear() {
        SystemUtils.unResolvedValuePair().clear();
        SystemUtils.resolvedValuePair().clear();
    }

    public static void initialize() {
        SystemUtils.initialize(null);
    }

    public static void initialize(String fileName) {
        SystemUtils.DEFAULT.initialized = false;
        DEFAULT.initialize0(fileName);
    }

    private static String find(String key) {
        String value = ConfigUtils.getSystemProperty(key);
        if (value == null) {
            value = SystemUtils.unResolvedValuePair().get(key);
        }
        return value;
    }

    private static /* varargs */ <T> T putAndGet(String key, String value, Class<?> type, String separator, Class<?> ... rawTypes) {
        Object parsedValue = null;
        if (type.isPrimitive() || SystemUtils.isPrimitive(type)) {
            parsedValue = SystemUtils.parsePrimitive((String)value, type);
        } else if (type.isArray()) {
            String defaultSeparator = StringUtils.isEmpty(separator) ? "," : separator;
            String[] values = value.split(defaultSeparator);
            if (String.class.isAssignableFrom(type.getComponentType())) {
                parsedValue = values;
            } else {
                parsedValue = Array.newInstance(type.getComponentType(), values.length);
                Class<?> rawType = type.getComponentType();
                for (int i = 0; i < values.length; ++i) {
                    Array.set(parsedValue, i, SystemUtils.parsePrimitive(values[i], rawType));
                }
            }
        } else if (List.class.isAssignableFrom(type)) {
            try {
                Class<String> rawType = rawTypes.length == 1 ? rawTypes[0] : String.class;
                String defaultSeparator = StringUtils.isEmpty(separator) ? "," : separator;
                String[] values = value.split(defaultSeparator);
                parsedValue = (List)type.newInstance();
                for (int i = 0; i < values.length; ++i) {
                    ((List)parsedValue).add(SystemUtils.parsePrimitive(values[i], rawType));
                }
            }
            catch (Throwable e) {
                throw new IllegalArgumentException("Failed to parse key '" + key + "', value '" + (String)value + "' from file " + SystemUtils.DEFAULT.fileName, e);
            }
        } else {
            parsedValue = value;
        }
        SystemUtils.resolvedValuePair().put(key, parsedValue);
        return (T)parsedValue;
    }

    private static <T> T parsePrimitive(String value, Class<T> type) {
        Object parsedValue = null;
        if (Boolean.TYPE.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)) {
            parsedValue = Boolean.valueOf(value);
        } else if (Integer.TYPE.isAssignableFrom(type) || Integer.class.isAssignableFrom(type)) {
            parsedValue = Integer.valueOf(value);
        } else if (Long.TYPE.isAssignableFrom(type) || Long.class.isAssignableFrom(type)) {
            parsedValue = Long.valueOf(value);
        } else if (Short.TYPE.isAssignableFrom(type) || Short.class.isAssignableFrom(type)) {
            parsedValue = Short.valueOf(value);
        } else if (Float.TYPE.isAssignableFrom(type) || Float.class.isAssignableFrom(type)) {
            parsedValue = Float.valueOf(value);
        } else if (Double.TYPE.isAssignableFrom(type) || Double.class.isAssignableFrom(type)) {
            parsedValue = Double.valueOf(value);
        } else if (String.class.isAssignableFrom(type)) {
            parsedValue = value;
        }
        return (T)parsedValue;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadFile(InputStream in) {
        if (in != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
                try {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        int ci = line.indexOf(35);
                        if (ci >= 0) {
                            line = line.substring(0, ci);
                        }
                        if ((line = line.trim()).length() <= 0) continue;
                        try {
                            String key = null;
                            int i = line.indexOf(61);
                            if (i > 0) {
                                key = line.substring(0, i).trim();
                                line = line.substring(i + 1).trim();
                            }
                            if (line.length() <= 0) continue;
                            this.unResolvedValuePair.put(key, line);
                        }
                        catch (Throwable t) {
                            logger.error("Failed to parse file " + this.fileName + " cause: " + t.getMessage());
                        }
                    }
                }
                finally {
                    reader.close();
                }
            }
            catch (Throwable t) {
                logger.error("Failed to parse file " + this.fileName + " cause: " + t.getMessage());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void resolveSystemProperties() {
        ArrayList<URL> list;
        this.resolvedValuePair = new ConcurrentHashMap<String, Object>();
        this.unResolvedValuePair = new ConcurrentHashMap<String, String>();
        if (this.fileName.startsWith("/")) {
            try {
                FileInputStream input = new FileInputStream(this.fileName);
                try {
                    this.loadFile(input);
                }
                finally {
                    input.close();
                }
            }
            catch (Throwable e) {
                if (e instanceof FileNotFoundException) {
                    logger.warn("No such file " + this.fileName + " found. ");
                }
                logger.warn("Failed to load " + this.fileName + " file from " + this.fileName + "(ingore this file): " + e.getMessage(), e);
            }
        }
        list = new ArrayList<URL>();
        try {
            Enumeration<URL> urls = ClassHelper.getClassLoader(SystemUtils.class).getResources(this.fileName);
            list = new ArrayList();
            while (urls.hasMoreElements()) {
                list.add(urls.nextElement());
            }
        }
        catch (Throwable t) {
            if (t instanceof FileNotFoundException) {
                logger.warn("No such file " + this.fileName + " found. ");
            }
            logger.warn("Fail to load " + this.fileName + " file: " + t.getMessage(), t);
        }
        if (list.size() == 0) {
            logger.warn("No " + this.fileName + " found on the class path.");
            return;
        }
        if (list.size() > 1) {
            String errMsg = String.format("only 1 %s file is expected, but %d %s files found on class path: %s", this.fileName, list.size(), this.fileName, ((Object)list).toString());
            throw new IllegalStateException(errMsg);
        }
        InputStream in = null;
        try {
            in = ClassHelper.getClassLoader().getResourceAsStream(this.fileName);
            this.loadFile(in);
        }
        catch (Throwable e) {
            if (e instanceof FileNotFoundException) {
                logger.warn("No such file " + this.fileName + " found. ");
            } else {
                logger.warn("Failed to load " + this.fileName + " file from " + this.fileName + "(ingore this file): " + e.getMessage(), e);
            }
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (Throwable e) {
                logger.warn("Failed to close " + this.fileName + " file from " + this.fileName + "(ingore this file): " + e.getMessage(), e);
            }
        }
        logger.info("load " + this.fileName + " file from " + list);
        for (URL url : list) {
            try {
                InputStream input = url.openStream();
                if (input == null) continue;
                try {
                    this.loadFile(input);
                }
                finally {
                    try {
                        input.close();
                    }
                    catch (Throwable throwable) {}
                }
            }
            catch (Throwable e) {
                if (e instanceof FileNotFoundException) {
                    logger.warn("No such file " + this.fileName + " found. ");
                    continue;
                }
                logger.warn("Fail to load " + this.fileName + " file from " + url + "(ingore this file): " + e.getMessage(), e);
            }
        }
    }

    private static boolean isPrimitive(Type type) {
        try {
            if (type != null && type instanceof Class) {
                Class clazz = (Class)type;
                return clazz.isPrimitive() || PRIMITIVE_TYPE.containsKey(clazz.getName());
            }
        }
        catch (Exception clazz) {
            // empty catch block
        }
        return false;
    }

    private synchronized void initialize0(String fileName) {
        if (!this.initialized) {
            String path;
            this.initialized = true;
            this.fileName = fileName == null ? (StringUtils.isEmpty(path = ConfigUtils.getSystemProperty(DEFAULT_FILE_PATH)) ? DEFAULT_FILE_PATH_NAME : path) : fileName;
            this.resolveSystemProperties();
        }
    }

    public static Map<String, Object> resolvedValuePair() {
        if (!SystemUtils.DEFAULT.initialized) {
            SystemUtils.initialize();
        }
        return SystemUtils.DEFAULT.resolvedValuePair;
    }

    public static Map<String, String> unResolvedValuePair() {
        if (!SystemUtils.DEFAULT.initialized) {
            SystemUtils.initialize();
        }
        return SystemUtils.DEFAULT.unResolvedValuePair;
    }

    private SystemUtils() {
    }

}

