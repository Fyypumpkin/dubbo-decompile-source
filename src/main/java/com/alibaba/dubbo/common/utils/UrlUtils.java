/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class UrlUtils {
    public static URL parseURL(String address, Map<String, String> defaults) {
        String defaultProtocol;
        HashMap<String, String> defaultParameters;
        String url;
        if (address == null || address.length() == 0) {
            return null;
        }
        if (address.indexOf("://") >= 0) {
            url = address;
        } else {
            String[] addresses = Constants.COMMA_SPLIT_PATTERN.split(address);
            url = addresses[0];
            if (addresses.length > 1) {
                StringBuilder backup = new StringBuilder();
                for (int i = 1; i < addresses.length; ++i) {
                    if (i > 1) {
                        backup.append(",");
                    }
                    backup.append(addresses[i]);
                }
                url = url + "?backup=" + backup.toString();
            }
        }
        String string = defaultProtocol = defaults == null ? null : defaults.get("protocol");
        if (defaultProtocol == null || defaultProtocol.length() == 0) {
            defaultProtocol = "dubbo";
        }
        String defaultUsername = defaults == null ? null : defaults.get("username");
        String defaultPassword = defaults == null ? null : defaults.get("password");
        int defaultPort = StringUtils.parseInteger(defaults == null ? null : defaults.get("port"));
        String defaultPath = defaults == null ? null : defaults.get("path");
        HashMap<String, String> hashMap = defaultParameters = defaults == null ? null : new HashMap<String, String>(defaults);
        if (defaultParameters != null) {
            defaultParameters.remove("protocol");
            defaultParameters.remove("username");
            defaultParameters.remove("password");
            defaultParameters.remove("host");
            defaultParameters.remove("port");
            defaultParameters.remove("path");
        }
        URL u = URL.valueOf(url);
        boolean changed = false;
        String protocol = u.getProtocol();
        String username = u.getUsername();
        String password = u.getPassword();
        String host = u.getHost();
        int port = u.getPort();
        String path = u.getPath();
        HashMap<String, String> parameters = new HashMap<String, String>(u.getParameters());
        if ((protocol == null || protocol.length() == 0) && defaultProtocol != null && defaultProtocol.length() > 0) {
            changed = true;
            protocol = defaultProtocol;
        }
        if ((username == null || username.length() == 0) && defaultUsername != null && defaultUsername.length() > 0) {
            changed = true;
            username = defaultUsername;
        }
        if ((password == null || password.length() == 0) && defaultPassword != null && defaultPassword.length() > 0) {
            changed = true;
            password = defaultPassword;
        }
        if (port <= 0) {
            if (defaultPort > 0) {
                changed = true;
                port = defaultPort;
            } else {
                changed = true;
                port = 9090;
            }
        }
        if ((path == null || path.length() == 0) && defaultPath != null && defaultPath.length() > 0) {
            changed = true;
            path = defaultPath;
        }
        if (defaultParameters != null && defaultParameters.size() > 0) {
            for (Map.Entry entry : defaultParameters.entrySet()) {
                String value;
                String key = (String)entry.getKey();
                String defaultValue = (String)entry.getValue();
                if (defaultValue == null || defaultValue.length() <= 0 || (value = (String)parameters.get(key)) != null && value.length() != 0) continue;
                changed = true;
                parameters.put(key, defaultValue);
            }
        }
        if (changed) {
            u = new URL(protocol, username, password, host, port, path, parameters);
        }
        return u;
    }

    public static List<URL> parseURLs(String address, Map<String, String> defaults) {
        if (address == null || address.length() == 0) {
            return null;
        }
        String[] addresses = Constants.REGISTRY_SPLIT_PATTERN.split(address);
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        ArrayList<URL> registries = new ArrayList<URL>();
        for (String addr : addresses) {
            registries.add(UrlUtils.parseURL(addr, defaults));
        }
        return registries;
    }

    public static Map<String, Map<String, String>> convertRegister(Map<String, Map<String, String>> register) {
        HashMap<String, Map<String, String>> newRegister = new HashMap<String, Map<String, String>>();
        for (Map.Entry<String, Map<String, String>> entry : register.entrySet()) {
            String serviceName = entry.getKey();
            Map<String, String> serviceUrls = entry.getValue();
            if (!serviceName.contains(":") && !serviceName.contains("/")) {
                for (Map.Entry<String, String> entry2 : serviceUrls.entrySet()) {
                    HashMap<String, String> newUrls;
                    String serviceUrl = entry2.getKey();
                    String serviceQuery = entry2.getValue();
                    Map<String, String> params = StringUtils.parseQueryString(serviceQuery);
                    String group = params.get("group");
                    String version = params.get("version");
                    String name = serviceName;
                    if (group != null && group.length() > 0) {
                        name = group + "/" + name;
                    }
                    if (version != null && version.length() > 0) {
                        name = name + ":" + version;
                    }
                    if ((newUrls = (HashMap<String, String>)newRegister.get(name)) == null) {
                        newUrls = new HashMap<String, String>();
                        newRegister.put(name, newUrls);
                    }
                    newUrls.put(serviceUrl, StringUtils.toQueryString(params));
                }
                continue;
            }
            newRegister.put(serviceName, serviceUrls);
        }
        return newRegister;
    }

    public static Map<String, String> convertSubscribe(Map<String, String> subscribe) {
        HashMap<String, String> newSubscribe = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : subscribe.entrySet()) {
            String serviceName = entry.getKey();
            String serviceQuery = entry.getValue();
            if (!serviceName.contains(":") && !serviceName.contains("/")) {
                Map<String, String> params = StringUtils.parseQueryString(serviceQuery);
                String group = params.get("group");
                String version = params.get("version");
                String name = serviceName;
                if (group != null && group.length() > 0) {
                    name = group + "/" + name;
                }
                if (version != null && version.length() > 0) {
                    name = name + ":" + version;
                }
                newSubscribe.put(name, StringUtils.toQueryString(params));
                continue;
            }
            newSubscribe.put(serviceName, serviceQuery);
        }
        return newSubscribe;
    }

    public static Map<String, Map<String, String>> revertRegister(Map<String, Map<String, String>> register) {
        HashMap<String, Map<String, String>> newRegister = new HashMap<String, Map<String, String>>();
        for (Map.Entry<String, Map<String, String>> entry : register.entrySet()) {
            String serviceName = entry.getKey();
            Map<String, String> serviceUrls = entry.getValue();
            if (serviceName.contains(":") || serviceName.contains("/")) {
                for (Map.Entry<String, String> entry2 : serviceUrls.entrySet()) {
                    HashMap<String, String> newUrls;
                    String serviceUrl = entry2.getKey();
                    String serviceQuery = entry2.getValue();
                    Map<String, String> params = StringUtils.parseQueryString(serviceQuery);
                    String name = serviceName;
                    int i = name.indexOf(47);
                    if (i >= 0) {
                        params.put("group", name.substring(0, i));
                        name = name.substring(i + 1);
                    }
                    if ((i = name.lastIndexOf(58)) >= 0) {
                        params.put("version", name.substring(i + 1));
                        name = name.substring(0, i);
                    }
                    if ((newUrls = (HashMap<String, String>)newRegister.get(name)) == null) {
                        newUrls = new HashMap<String, String>();
                        newRegister.put(name, newUrls);
                    }
                    newUrls.put(serviceUrl, StringUtils.toQueryString(params));
                }
                continue;
            }
            newRegister.put(serviceName, serviceUrls);
        }
        return newRegister;
    }

    public static Map<String, String> revertSubscribe(Map<String, String> subscribe) {
        HashMap<String, String> newSubscribe = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : subscribe.entrySet()) {
            String serviceName = entry.getKey();
            String serviceQuery = entry.getValue();
            if (serviceName.contains(":") || serviceName.contains("/")) {
                Map<String, String> params = StringUtils.parseQueryString(serviceQuery);
                String name = serviceName;
                int i = name.indexOf(47);
                if (i >= 0) {
                    params.put("group", name.substring(0, i));
                    name = name.substring(i + 1);
                }
                if ((i = name.lastIndexOf(58)) >= 0) {
                    params.put("version", name.substring(i + 1));
                    name = name.substring(0, i);
                }
                newSubscribe.put(name, StringUtils.toQueryString(params));
                continue;
            }
            newSubscribe.put(serviceName, serviceQuery);
        }
        return newSubscribe;
    }

    public static Map<String, Map<String, String>> revertNotify(Map<String, Map<String, String>> notify) {
        if (notify != null && notify.size() > 0) {
            HashMap<String, Map<String, String>> newNotify = new HashMap<String, Map<String, String>>();
            for (Map.Entry<String, Map<String, String>> entry : notify.entrySet()) {
                String serviceName = entry.getKey();
                Map<String, String> serviceUrls = entry.getValue();
                if (!serviceName.contains(":") && !serviceName.contains("/")) {
                    if (serviceUrls == null || serviceUrls.size() <= 0) continue;
                    for (Map.Entry<String, String> entry2 : serviceUrls.entrySet()) {
                        HashMap<String, String> newUrls;
                        String url = entry2.getKey();
                        String query = entry2.getValue();
                        Map<String, String> params = StringUtils.parseQueryString(query);
                        String group = params.get("group");
                        String version = params.get("version");
                        String name = serviceName;
                        if (group != null && group.length() > 0) {
                            name = group + "/" + name;
                        }
                        if (version != null && version.length() > 0) {
                            name = name + ":" + version;
                        }
                        if ((newUrls = (HashMap<String, String>)newNotify.get(name)) == null) {
                            newUrls = new HashMap<String, String>();
                            newNotify.put(name, newUrls);
                        }
                        newUrls.put(url, StringUtils.toQueryString(params));
                    }
                    continue;
                }
                newNotify.put(serviceName, serviceUrls);
            }
            return newNotify;
        }
        return notify;
    }

    public static List<String> revertForbid(List<String> forbid, Set<URL> subscribed) {
        if (forbid != null && forbid.size() > 0) {
            ArrayList<String> newForbid = new ArrayList<String>();
            block0 : for (String serviceName : forbid) {
                if (!serviceName.contains(":") && !serviceName.contains("/")) {
                    for (URL url : subscribed) {
                        if (!serviceName.equals(url.getServiceInterface())) continue;
                        newForbid.add(url.getServiceKey());
                        continue block0;
                    }
                    continue;
                }
                newForbid.add(serviceName);
            }
            return newForbid;
        }
        return forbid;
    }

    public static URL getEmptyUrl(String service, String category) {
        String group = null;
        String version = null;
        int i = service.indexOf(47);
        if (i > 0) {
            group = service.substring(0, i);
            service = service.substring(i + 1);
        }
        if ((i = service.lastIndexOf(58)) > 0) {
            version = service.substring(i + 1);
            service = service.substring(0, i);
        }
        return URL.valueOf("empty://0.0.0.0/" + service + "?" + "category" + "=" + category + (group == null ? "" : new StringBuilder().append("&group=").append(group).toString()) + (version == null ? "" : new StringBuilder().append("&version=").append(version).toString()));
    }

    public static boolean isMatchCategory(String category, String categories) {
        if (categories == null || categories.length() == 0) {
            return "providers".equals(category);
        }
        if (categories.contains("*")) {
            return true;
        }
        if (categories.contains("-")) {
            return !categories.contains("-" + category);
        }
        return categories.contains(category);
    }

    public static boolean isMatch(URL consumerUrl, URL providerUrl) {
        String consumerInterface = consumerUrl.getServiceInterface();
        String providerInterface = providerUrl.getServiceInterface();
        if (!"*".equals(consumerInterface) && !StringUtils.isEquals(consumerInterface, providerInterface)) {
            return false;
        }
        if (!UrlUtils.isMatchCategory(providerUrl.getParameter("category", "providers"), consumerUrl.getParameter("category", "providers"))) {
            return false;
        }
        if (!providerUrl.getParameter("enabled", true) && !"*".equals(consumerUrl.getParameter("enabled"))) {
            return false;
        }
        String consumerGroup = consumerUrl.getParameter("group");
        String consumerVersion = consumerUrl.getParameter("version");
        String consumerClassifier = consumerUrl.getParameter("classifier", "*");
        String providerGroup = providerUrl.getParameter("group");
        String providerVersion = providerUrl.getParameter("version");
        String providerClassifier = providerUrl.getParameter("classifier", "*");
        return !(!"*".equals(consumerGroup) && !StringUtils.isEquals(consumerGroup, providerGroup) && !StringUtils.isContains(consumerGroup, providerGroup) || !"*".equals(consumerVersion) && !StringUtils.isEquals(consumerVersion, providerVersion) || consumerClassifier != null && !"*".equals(consumerClassifier) && !StringUtils.isEquals(consumerClassifier, providerClassifier));
    }

    public static boolean isMatchGlobPattern(String pattern, String value, URL param) {
        if (param != null && pattern.startsWith("$")) {
            pattern = param.getRawParameter(pattern.substring(1));
        }
        return UrlUtils.isMatchGlobPattern(pattern, value);
    }

    public static boolean isMatchGlobPattern(String pattern, String value) {
        if ("*".equals(pattern)) {
            return true;
        }
        if (!(pattern != null && pattern.length() != 0 || value != null && value.length() != 0)) {
            return true;
        }
        if (pattern == null || pattern.length() == 0 || value == null || value.length() == 0) {
            return false;
        }
        int i = pattern.lastIndexOf(42);
        if (i == -1) {
            return value.equals(pattern);
        }
        if (i == pattern.length() - 1) {
            return value.startsWith(pattern.substring(0, i));
        }
        if (i == 0) {
            return value.endsWith(pattern.substring(i + 1));
        }
        String prefix = pattern.substring(0, i);
        String suffix = pattern.substring(i + 1);
        return value.startsWith(prefix) && value.endsWith(suffix);
    }

    public static boolean isServiceKeyMatch(URL pattern, URL value) {
        return pattern.getParameter("interface").equals(value.getParameter("interface")) && UrlUtils.isItemMatch(pattern.getParameter("group"), value.getParameter("group")) && UrlUtils.isItemMatch(pattern.getParameter("version"), value.getParameter("version"));
    }

    static boolean isItemMatch(String pattern, String value) {
        if (pattern == null) {
            return value == null;
        }
        return "*".equals(pattern) || pattern.equals(value);
    }
}

