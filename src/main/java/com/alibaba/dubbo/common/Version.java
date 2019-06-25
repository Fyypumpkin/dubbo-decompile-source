/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.common.utils.StringUtils;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.HashSet;

public final class Version {
    private static final Logger logger = LoggerFactory.getLogger(Version.class);
    private static final String VERSION = Version.getVersion(Version.class, "3.2.0");
    private static final int LOWEST_VERSION_NUMBER = 3200;
    private static final boolean INTERNAL = Version.hasResource("com/alibaba/dubbo/registry/internal/RemoteRegistry.class");
    private static final boolean COMPATIBLE = Version.hasResource("com/taobao/remoting/impl/ConnectionRequest.class");

    private Version() {
    }

    public static String getVersion() {
        return VERSION;
    }

    public static boolean isInternalVersion() {
        return INTERNAL;
    }

    public static boolean isCompatibleVersion() {
        return COMPATIBLE;
    }

    private static boolean hasResource(String path) {
        try {
            return Version.class.getClassLoader().getResource(path) != null;
        }
        catch (Throwable t) {
            return false;
        }
    }

    public static String getVersion(Class<?> cls, String defaultVersion) {
        try {
            String version = cls.getPackage().getImplementationVersion();
            if (version == null || version.length() == 0) {
                version = cls.getPackage().getSpecificationVersion();
            }
            if (version == null || version.length() == 0) {
                CodeSource codeSource = cls.getProtectionDomain().getCodeSource();
                if (codeSource == null) {
                    logger.info("No codeSource for class " + cls.getName() + " when getVersion, use default version " + defaultVersion);
                } else {
                    String file = codeSource.getLocation().getFile();
                    if (file != null && file.length() > 0 && file.endsWith(".jar")) {
                        int i = (file = file.substring(0, file.length() - 4)).lastIndexOf(47);
                        if (i >= 0) {
                            file = file.substring(i + 1);
                        }
                        if ((i = file.indexOf("-")) >= 0) {
                            file = file.substring(i + 1);
                        }
                        while (file.length() > 0 && !Character.isDigit(file.charAt(0)) && (i = file.indexOf("-")) >= 0) {
                            file = file.substring(i + 1);
                        }
                        version = file;
                    }
                }
            }
            return version == null || version.length() == 0 ? defaultVersion : version;
        }
        catch (Throwable e) {
            logger.error("return default version, ignore exception " + e.getMessage(), e);
            return defaultVersion;
        }
    }

    public static void checkDuplicate(Class<?> cls, boolean failOnError) {
        Version.checkDuplicate(cls.getName().replace('.', '/') + ".class", failOnError);
    }

    public static void checkDuplicate(Class<?> cls) {
        Version.checkDuplicate(cls, false);
    }

    public static void checkDuplicate(String path, boolean failOnError) {
        try {
            Enumeration<URL> urls = ClassHelper.getCallerClassLoader(Version.class).getResources(path);
            HashSet<String> files = new HashSet<String>();
            while (urls.hasMoreElements()) {
                String file;
                URL url = urls.nextElement();
                if (url == null || (file = url.getFile()) == null || file.length() <= 0) continue;
                files.add(file);
            }
            if (files.size() > 1) {
                String error = "Duplicate class " + path + " in " + files.size() + " jar " + files;
                if (failOnError) {
                    throw new IllegalStateException(error);
                }
                logger.error(error);
            }
        }
        catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static boolean isUpgraded(String version) {
        return Version.isUpgraded(version, 3200);
    }

    public static boolean isUpgraded(String version, int versionScore) {
        if (StringUtils.isEmpty(version)) {
            return false;
        }
        if ((version = version.toLowerCase()).startsWith("dubbo-")) {
            version = version.replaceAll("dubbo-", "");
        }
        if (version.endsWith("-alpha")) {
            version = version.replace("-alpha", "");
        }
        if (version.endsWith("-release")) {
            version = version.replace("-release", "");
        }
        if (version.endsWith("-snapshot")) {
            version = version.replace("-snapshot", "");
        }
        int score = 0;
        int n = 0;
        char[] chars = version.toCharArray();
        for (int i = chars.length - 1; i >= 0; --i) {
            if (chars[i] != '.' || ++n <= 3) continue;
            version = version.substring(0, i);
            break;
        }
        while (n++ < 3) {
            version = version + ".0";
        }
        String[] array = version.split("\\.");
        int len = array.length;
        for (int i = 1; i <= len; ++i) {
            score = (int)((double)score + (double)Integer.parseInt(array[len - i]) * Math.pow(10.0, i - 1));
        }
        return score >= versionScore;
    }

    static {
        Version.checkDuplicate(Version.class);
    }
}

