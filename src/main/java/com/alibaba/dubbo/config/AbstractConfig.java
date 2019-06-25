/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.common.utils.SystemUtils;
import com.alibaba.dubbo.config.support.Parameter;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractConfig
implements Serializable {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractConfig.class);
    private static final long serialVersionUID = 4267533505537413570L;
    private static final int MAX_LENGTH = 200;
    private static final int MAX_PATH_LENGTH = 200;
    private static final Pattern PATTERN_NAME = Pattern.compile("[\\-._0-9a-zA-Z]+");
    private static final Pattern PATTERN_MULTI_NAME = Pattern.compile("[,\\-._0-9a-zA-Z]+");
    private static final Pattern PATTERN_METHOD_NAME = Pattern.compile("[a-zA-Z][0-9a-zA-Z]*");
    private static final Pattern PATTERN_PATH = Pattern.compile("[/\\-$._0-9a-zA-Z]+");
    private static final Pattern PATTERN_NAME_HAS_SYMBOL = Pattern.compile("[:*,/\\-._0-9a-zA-Z]+");
    private static final Pattern PATTERN_KEY = Pattern.compile("[*,\\-._0-9a-zA-Z]+");
    private static final Map<String, String> legacyProperties = new HashMap<String, String>();
    private static final String[] SUFFIXES = new String[]{"Config", "Bean"};
    protected String id;

    private static String convertLegacyValue(String key, String value) {
        if (value != null && value.length() > 0) {
            if ("dubbo.service.max.retry.providers".equals(key)) {
                return String.valueOf(Integer.parseInt(value) - 1);
            }
            if ("dubbo.service.allow.no.provider".equals(key)) {
                return String.valueOf(!Boolean.parseBoolean(value));
            }
        }
        return value;
    }

    protected static void appendProperties(AbstractConfig config) {
        Method[] methods;
        if (config == null) {
            return;
        }
        String prefix = "dubbo." + AbstractConfig.getTagName(config.getClass()) + ".";
        for (Method method : methods = config.getClass().getMethods()) {
            try {
                String pn;
                String name = method.getName();
                if (name.length() <= 3 || !name.startsWith("set") || !Modifier.isPublic(method.getModifiers()) || method.getParameterTypes().length != 1 || !AbstractConfig.isPrimitive(method.getParameterTypes()[0])) continue;
                String property = StringUtils.camelToSplitName(name.substring(3, 4).toLowerCase() + name.substring(4), ".");
                String value = null;
                if (config.getId() != null && config.getId().length() > 0 && !StringUtils.isBlank(value = System.getProperty(pn = prefix + config.getId() + "." + property))) {
                    logger.info("Use System Property " + pn + " to config dubbo");
                }
                if (!(value != null && value.length() != 0 || StringUtils.isBlank(value = System.getProperty(pn = prefix + property)))) {
                    logger.info("Use System Property " + pn + " to config dubbo");
                }
                if (value == null || value.length() == 0) {
                    Method getter;
                    try {
                        getter = config.getClass().getMethod("get" + name.substring(3), new Class[0]);
                    }
                    catch (NoSuchMethodException e) {
                        try {
                            getter = config.getClass().getMethod("is" + name.substring(3), new Class[0]);
                        }
                        catch (NoSuchMethodException e2) {
                            getter = null;
                        }
                    }
                    if (getter != null && getter.invoke(config, new Object[0]) == null) {
                        String legacyKey;
                        if (config.getId() != null && config.getId().length() > 0) {
                            value = ConfigUtils.getProperty(prefix + config.getId() + "." + property);
                        }
                        if (value == null || value.length() == 0) {
                            value = ConfigUtils.getProperty(prefix + property);
                        }
                        if ((value == null || value.length() == 0) && (legacyKey = legacyProperties.get(prefix + property)) != null && legacyKey.length() > 0) {
                            value = AbstractConfig.convertLegacyValue(legacyKey, ConfigUtils.getProperty(legacyKey));
                        }
                    }
                }
                if (value == null || value.length() <= 0) continue;
                method.invoke(config, AbstractConfig.convertPrimitive(method.getParameterTypes()[0], value));
            }
            catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private static String getTagName(Class<?> cls) {
        String tag = cls.getSimpleName();
        for (String suffix : SUFFIXES) {
            if (!tag.endsWith(suffix)) continue;
            tag = tag.substring(0, tag.length() - suffix.length());
            break;
        }
        tag = tag.toLowerCase();
        return tag;
    }

    protected static void appendParameters(Map<String, String> parameters, Object config) {
        AbstractConfig.appendParameters(parameters, config, null);
    }

    protected static void activeGlobalGroupIfNeed(Map<String, String> parameters, Object config) {
        String group = SystemUtils.getString("APPLICATION_GROUP");
        if (StringUtils.isNotEmpty(group)) {
            String current = parameters.get("group");
            if (StringUtils.isNotEmpty(current)) {
                logger.warn("use global group:'" + group + "' override current group:'" + current + "', dubbo framwork(" + Version.getVersion() + ")");
            }
            parameters.put("group", group);
        }
    }

    protected static void activeGlobalDatacenterIfNeed(Map<String, String> parameters, Object config) {
        String dc = SystemUtils.getString("APPLICATION_IDC");
        if (StringUtils.isNotEmpty(dc)) {
            String current = parameters.get("dc");
            if (StringUtils.isNotEmpty(current)) {
                logger.warn("use global dc:'" + dc + "' override current dc:'" + current + "', dubbo framwork(" + Version.getVersion() + ")");
            }
            parameters.put("dc", dc);
        }
    }

    protected static void disableGenericRegisterIfNeed(Map<String, String> parameters, Object config) {
        String registerGeneric = SystemUtils.getString("generic.register");
        if (StringUtils.isNotEmpty(registerGeneric)) {
            parameters.put("generic.register", registerGeneric);
        }
    }

    protected static void appendParameters(Map<String, String> parameters, Object config, String prefix) {
        Method[] methods;
        if (config == null) {
            return;
        }
        for (Method method : methods = config.getClass().getMethods()) {
            try {
                Map map;
                String name = method.getName();
                if ((name.startsWith("get") || name.startsWith("is")) && !"getClass".equals(name) && Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0 && AbstractConfig.isPrimitive(method.getReturnType())) {
                    Parameter parameter = method.getAnnotation(Parameter.class);
                    if (method.getReturnType() == Object.class || parameter != null && parameter.excluded()) continue;
                    int i = name.startsWith("get") ? 3 : 2;
                    String prop = StringUtils.camelToSplitName(name.substring(i, i + 1).toLowerCase() + name.substring(i + 1), ".");
                    String key = parameter != null && parameter.key().length() > 0 ? parameter.key() : prop;
                    Object value = method.invoke(config, new Object[0]);
                    String str = String.valueOf(value).trim();
                    if (value != null && str.length() > 0) {
                        if (parameter != null && parameter.escaped()) {
                            str = URL.encode(str);
                        }
                        if (parameter != null && parameter.append()) {
                            String pre = parameters.get("default." + (String)key);
                            if (pre != null && pre.length() > 0) {
                                str = pre + "," + str;
                            }
                            if ((pre = parameters.get(key)) != null && pre.length() > 0) {
                                str = pre + "," + str;
                            }
                        }
                        if (prefix != null && prefix.length() > 0) {
                            key = prefix + "." + (String)key;
                        }
                        parameters.put(key, str);
                        continue;
                    }
                    if (parameter == null || !parameter.required()) continue;
                    throw new IllegalStateException(config.getClass().getSimpleName() + "." + (String)key + " == null");
                }
                if (!"getParameters".equals(name) || !Modifier.isPublic(method.getModifiers()) || method.getParameterTypes().length != 0 || method.getReturnType() != Map.class || (map = (Map)method.invoke(config, new Object[0])) == null || map.size() <= 0) continue;
                String pre = prefix != null && prefix.length() > 0 ? prefix + "." : "";
                for (Map.Entry entry : map.entrySet()) {
                    parameters.put(pre + ((String)entry.getKey()).replace('-', '.'), (String)entry.getValue());
                }
            }
            catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    protected static void appendAttributes(Map<Object, Object> parameters, Object config) {
        AbstractConfig.appendAttributes(parameters, config, null);
    }

    protected static void appendAttributes(Map<Object, Object> parameters, Object config, String prefix) {
        Method[] methods;
        if (config == null) {
            return;
        }
        for (Method method : methods = config.getClass().getMethods()) {
            try {
                Parameter parameter;
                String key;
                String name = method.getName();
                if (!name.startsWith("get") && !name.startsWith("is") || "getClass".equals(name) || !Modifier.isPublic(method.getModifiers()) || method.getParameterTypes().length != 0 || !AbstractConfig.isPrimitive(method.getReturnType()) || (parameter = method.getAnnotation(Parameter.class)) == null || !parameter.attribute()) continue;
                parameter.key();
                if (parameter.key().length() > 0) {
                    key = parameter.key();
                } else {
                    int i = name.startsWith("get") ? 3 : 2;
                    key = name.substring(i, i + 1).toLowerCase() + name.substring(i + 1);
                }
                Object value = method.invoke(config, new Object[0]);
                if (value == null) continue;
                if (prefix != null && prefix.length() > 0) {
                    key = prefix + "." + key;
                }
                parameters.put(key, value);
            }
            catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() || type == String.class || type == Character.class || type == Boolean.class || type == Byte.class || type == Short.class || type == Integer.class || type == Long.class || type == Float.class || type == Double.class || type == Object.class;
    }

    private static Object convertPrimitive(Class<?> type, String value) {
        if (type == Character.TYPE || type == Character.class) {
            return Character.valueOf(value.length() > 0 ? value.charAt(0) : (char)'\u0000');
        }
        if (type == Boolean.TYPE || type == Boolean.class) {
            return Boolean.valueOf(value);
        }
        if (type == Byte.TYPE || type == Byte.class) {
            return Byte.valueOf(value);
        }
        if (type == Short.TYPE || type == Short.class) {
            return Short.valueOf(value);
        }
        if (type == Integer.TYPE || type == Integer.class) {
            return Integer.valueOf(value);
        }
        if (type == Long.TYPE || type == Long.class) {
            return Long.valueOf(value);
        }
        if (type == Float.TYPE || type == Float.class) {
            return Float.valueOf(value);
        }
        if (type == Double.TYPE || type == Double.class) {
            return Double.valueOf(value);
        }
        return value;
    }

    protected static void checkExtension(Class<?> type, String property, String value) {
        AbstractConfig.checkName(property, value);
        if (value != null && value.length() > 0 && !ExtensionLoader.getExtensionLoader(type).hasExtension(value)) {
            throw new IllegalStateException("No such extension " + value + " for " + property + "/" + type.getName());
        }
    }

    protected static void checkMultiExtension(Class<?> type, String property, String value) {
        AbstractConfig.checkMultiName(property, value);
        if (value != null && value.length() > 0) {
            String[] values;
            for (String v : values = value.split("\\s*[,]+\\s*")) {
                if (v.startsWith("-")) {
                    v = v.substring(1);
                }
                if ("default".equals(v) || ExtensionLoader.getExtensionLoader(type).hasExtension(v)) continue;
                throw new IllegalStateException("No such extension " + v + " for " + property + "/" + type.getName());
            }
        }
    }

    protected static void checkLength(String property, String value) {
        AbstractConfig.checkProperty(property, value, 200, null);
    }

    protected static void checkPathLength(String property, String value) {
        AbstractConfig.checkProperty(property, value, 200, null);
    }

    protected static void checkName(String property, String value) {
        AbstractConfig.checkProperty(property, value, 200, PATTERN_NAME);
    }

    protected static void checkNameHasSymbol(String property, String value) {
        AbstractConfig.checkProperty(property, value, 200, PATTERN_NAME_HAS_SYMBOL);
    }

    protected static void checkKey(String property, String value) {
        AbstractConfig.checkProperty(property, value, 200, PATTERN_KEY);
    }

    protected static void checkMultiName(String property, String value) {
        AbstractConfig.checkProperty(property, value, 200, PATTERN_MULTI_NAME);
    }

    protected static void checkPathName(String property, String value) {
        AbstractConfig.checkProperty(property, value, 200, PATTERN_PATH);
    }

    protected static void checkMethodName(String property, String value) {
        AbstractConfig.checkProperty(property, value, 200, PATTERN_METHOD_NAME);
    }

    protected static void checkParameterName(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return;
        }
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            AbstractConfig.checkNameHasSymbol(entry.getKey(), entry.getValue());
        }
    }

    protected static void checkProperty(String property, String value, int maxlength, Pattern pattern) {
        Matcher matcher;
        if (value == null || value.length() == 0) {
            return;
        }
        if (value.length() > maxlength) {
            throw new IllegalStateException("Invalid " + property + "=\"" + value + "\" is longer than " + maxlength);
        }
        if (pattern != null && !(matcher = pattern.matcher(value)).matches()) {
            throw new IllegalStateException("Invalid " + property + "=\"" + value + "\" contains illegal character, only digit, letter, '-', '_' or '.' is legal.");
        }
    }

    @Parameter(excluded=true)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected void appendAnnotation(Class<?> annotationClass, Object annotation) {
        Method[] methods;
        for (Method method : methods = annotationClass.getMethods()) {
            if (method.getDeclaringClass() == Object.class || method.getReturnType() == Void.TYPE || method.getParameterTypes().length != 0 || !Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) continue;
            try {
                String property = method.getName();
                if ("interfaceClass".equals(property) || "interfaceName".equals(property)) {
                    property = "interface";
                }
                String setter = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
                Map<String, String> value = method.invoke(annotation, new Object[0]);
                if (value == null || ((Object)value).equals(method.getDefaultValue())) continue;
                Class<Object> parameterType = ReflectUtils.getBoxedClass(method.getReturnType());
                if ("filter".equals(property) || "listener".equals(property)) {
                    parameterType = String.class;
                    value = StringUtils.join((String[])value, ",");
                } else if ("parameters".equals(property)) {
                    parameterType = Map.class;
                    value = CollectionUtils.toStringMap((String[])value);
                }
                try {
                    Method setterMethod = this.getClass().getMethod(setter, parameterType);
                    setterMethod.invoke(this, value);
                }
                catch (NoSuchMethodException setterMethod) {}
            }
            catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public String toString() {
        try {
            Method[] methods;
            StringBuilder buf = new StringBuilder();
            buf.append("<dubbo:");
            buf.append(AbstractConfig.getTagName(this.getClass()));
            for (Method method : methods = this.getClass().getMethods()) {
                try {
                    String name = method.getName();
                    if (!name.startsWith("get") && !name.startsWith("is") || "getClass".equals(name) || "get".equals(name) || "is".equals(name) || !Modifier.isPublic(method.getModifiers()) || method.getParameterTypes().length != 0 || !AbstractConfig.isPrimitive(method.getReturnType())) continue;
                    int i = name.startsWith("get") ? 3 : 2;
                    String key = name.substring(i, i + 1).toLowerCase() + name.substring(i + 1);
                    Object value = method.invoke(this, new Object[0]);
                    if (value == null) continue;
                    buf.append(" ");
                    buf.append(key);
                    buf.append("=\"");
                    buf.append(value);
                    buf.append("\"");
                }
                catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
            buf.append(" />");
            return buf.toString();
        }
        catch (Throwable t) {
            logger.warn(t.getMessage(), t);
            return super.toString();
        }
    }

    static {
        legacyProperties.put("dubbo.protocol.name", "dubbo.service.protocol");
        legacyProperties.put("dubbo.protocol.host", "dubbo.service.server.host");
        legacyProperties.put("dubbo.protocol.port", "dubbo.service.server.port");
        legacyProperties.put("dubbo.protocol.threads", "dubbo.service.max.thread.pool.size");
        legacyProperties.put("dubbo.consumer.timeout", "dubbo.service.invoke.timeout");
        legacyProperties.put("dubbo.consumer.retries", "dubbo.service.max.retry.providers");
        legacyProperties.put("dubbo.consumer.check", "dubbo.service.allow.no.provider");
        legacyProperties.put("dubbo.service.url", "dubbo.service.address");
    }
}

