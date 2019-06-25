/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.extension;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.compiler.Compiler;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.ExtensionFactory;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.common.extension.support.ActivateComparator;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.Holder;
import com.alibaba.dubbo.common.utils.StringUtils;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

public class ExtensionLoader<T> {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);
    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";
    private static final String DUBBO_INTERNAL_DIRECTORY = "META-INF/dubbo/internal/";
    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap();
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap();
    private final Class<?> type;
    private final ExtensionFactory objectFactory;
    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap();
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder();
    private final Map<String, Activate> cachedActivates = new ConcurrentHashMap<String, Activate>();
    private volatile Class<?> cachedAdaptiveClass = null;
    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<String, Holder<Object>>();
    private String cachedDefaultName;
    private final Holder<Object> cachedAdaptiveInstance = new Holder();
    private volatile Throwable createAdaptiveInstanceError;
    private Set<Class<?>> cachedWrapperClasses;
    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        if (!ExtensionLoader.withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
        }
        ExtensionLoader loader = (ExtensionLoader)EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader)EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    private ExtensionLoader(Class<?> type) {
        this.type = type;
        this.objectFactory = type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension();
    }

    public String getExtensionName(T extensionInstance) {
        return this.getExtensionName(extensionInstance.getClass());
    }

    public String getExtensionName(Class<?> extensionClass) {
        return (String)this.cachedNames.get(extensionClass);
    }

    public List<T> getActivateExtension(URL url, String key) {
        return this.getActivateExtension(url, key, null);
    }

    public List<T> getActivateExtension(URL url, String[] values) {
        return this.getActivateExtension(url, values, null);
    }

    public List<T> getActivateExtension(URL url, String key, String group) {
        String value = url.getParameter(key);
        return this.getActivateExtension(url, value == null || value.length() == 0 ? null : Constants.COMMA_SPLIT_PATTERN.split(value), group);
    }

    public List<T> getActivateExtension(URL url, String[] values, String group) {
        List<Object> names;
        String name;
        ArrayList<Object> exts = new ArrayList<Object>();
        List<Object> list = names = values == null ? new ArrayList(0) : Arrays.asList(values);
        if (!names.contains("-default")) {
            this.getExtensionClasses();
            for (Map.Entry<String, Activate> entry : this.cachedActivates.entrySet()) {
                name = entry.getKey();
                Activate activate = entry.getValue();
                if (!this.isMatchGroup(group, activate.group())) continue;
                T ext = this.getExtension(name);
                if (names.contains(name) || names.contains("-" + name) || !this.isActive(activate, url)) continue;
                exts.add(ext);
            }
            Collections.sort(exts, ActivateComparator.COMPARATOR);
        }
        ArrayList<T> usrs = new ArrayList<T>();
        for (int i = 0; i < names.size(); ++i) {
            name = (String)names.get(i);
            if (name.startsWith("-") || names.contains("-" + name)) continue;
            if ("default".equals(name)) {
                if (usrs.size() <= 0) continue;
                exts.addAll(0, usrs);
                usrs.clear();
                continue;
            }
            T ext = this.getExtension(name);
            usrs.add(ext);
        }
        if (usrs.size() > 0) {
            exts.addAll(usrs);
        }
        return exts;
    }

    private boolean isMatchGroup(String group, String[] groups) {
        if (group == null || group.length() == 0) {
            return true;
        }
        if (groups != null && groups.length > 0) {
            for (String g : groups) {
                if (!group.equals(g)) continue;
                return true;
            }
        }
        return false;
    }

    private boolean isActive(Activate activate, URL url) {
        String[] keys = activate.value();
        if (keys == null || keys.length == 0) {
            return true;
        }
        for (String key : keys) {
            for (Map.Entry<String, String> entry : url.getParameters().entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                if (!k.equals(key) && !k.endsWith("." + key) || !ConfigUtils.isNotEmpty(v)) continue;
                return true;
            }
        }
        return false;
    }

    public T getLoadedExtension(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Extension name == null");
        }
        Holder holder = (Holder)this.cachedInstances.get(name);
        if (holder == null) {
            this.cachedInstances.putIfAbsent(name, new Holder());
            holder = (Holder)this.cachedInstances.get(name);
        }
        return holder.get();
    }

    public Set<String> getLoadedExtensions() {
        return Collections.unmodifiableSet(new TreeSet(this.cachedInstances.keySet()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public T getExtension(String name) {
        Object instance;
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Extension name == null");
        }
        if ("true".equals(name)) {
            return this.getDefaultExtension();
        }
        Holder holder = (Holder)this.cachedInstances.get(name);
        if (holder == null) {
            this.cachedInstances.putIfAbsent(name, new Holder());
            holder = (Holder)this.cachedInstances.get(name);
        }
        if ((instance = holder.get()) == null) {
            Holder holder2 = holder;
            synchronized (holder2) {
                instance = holder.get();
                if (instance == null) {
                    instance = this.createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return instance;
    }

    public T getDefaultExtension() {
        this.getExtensionClasses();
        if (null == this.cachedDefaultName || this.cachedDefaultName.length() == 0 || "true".equals(this.cachedDefaultName)) {
            return null;
        }
        return this.getExtension(this.cachedDefaultName);
    }

    public boolean hasExtension(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Extension name == null");
        }
        try {
            return this.getExtensionClass(name) != null;
        }
        catch (Throwable t) {
            return false;
        }
    }

    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> clazzes = this.getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<String>(clazzes.keySet()));
    }

    public String getDefaultExtensionName() {
        this.getExtensionClasses();
        return this.cachedDefaultName;
    }

    public void addExtension(String name, Class<?> clazz) {
        this.getExtensionClasses();
        if (!this.type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Input type " + clazz + "not implement Extension " + this.type);
        }
        if (clazz.isInterface()) {
            throw new IllegalStateException("Input type " + clazz + "can not be interface!");
        }
        if (!clazz.isAnnotationPresent(Adaptive.class)) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalStateException("Extension name is blank (Extension " + this.type + ")!");
            }
            if (this.cachedClasses.get().containsKey(name)) {
                throw new IllegalStateException("Extension name " + name + " already existed(Extension " + this.type + ")!");
            }
            this.cachedNames.put(clazz, name);
            this.cachedClasses.get().put(name, clazz);
        } else {
            if (this.cachedAdaptiveClass != null) {
                throw new IllegalStateException("Adaptive Extension already existed(Extension " + this.type + ")!");
            }
            this.cachedAdaptiveClass = clazz;
        }
    }

    @Deprecated
    public void replaceExtension(String name, Class<?> clazz) {
        this.getExtensionClasses();
        if (!this.type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Input type " + clazz + "not implement Extension " + this.type);
        }
        if (clazz.isInterface()) {
            throw new IllegalStateException("Input type " + clazz + "can not be interface!");
        }
        if (!clazz.isAnnotationPresent(Adaptive.class)) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalStateException("Extension name is blank (Extension " + this.type + ")!");
            }
            if (!this.cachedClasses.get().containsKey(name)) {
                throw new IllegalStateException("Extension name " + name + " not existed(Extension " + this.type + ")!");
            }
            this.cachedNames.put(clazz, name);
            this.cachedClasses.get().put(name, clazz);
            this.cachedInstances.remove(name);
        } else {
            if (this.cachedAdaptiveClass == null) {
                throw new IllegalStateException("Adaptive Extension not existed(Extension " + this.type + ")!");
            }
            this.cachedAdaptiveClass = clazz;
            this.cachedAdaptiveInstance.set(null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public T getAdaptiveExtension() {
        Object instance = this.cachedAdaptiveInstance.get();
        if (instance == null) {
            if (this.createAdaptiveInstanceError == null) {
                Holder<Object> holder = this.cachedAdaptiveInstance;
                synchronized (holder) {
                    instance = this.cachedAdaptiveInstance.get();
                    if (instance == null) {
                        try {
                            instance = this.createAdaptiveExtension();
                            this.cachedAdaptiveInstance.set(instance);
                        }
                        catch (Throwable t) {
                            this.createAdaptiveInstanceError = t;
                            throw new IllegalStateException("fail to create adaptive instance: " + t.toString(), t);
                        }
                    }
                }
            }
            throw new IllegalStateException("fail to create adaptive instance: " + this.createAdaptiveInstanceError.toString(), this.createAdaptiveInstanceError);
        }
        return (T)instance;
    }

    private IllegalStateException findException(String name) {
        for (Map.Entry<String, IllegalStateException> entry : this.exceptions.entrySet()) {
            if (!entry.getKey().toLowerCase().contains(name.toLowerCase())) continue;
            return entry.getValue();
        }
        StringBuilder buf = new StringBuilder("No such extension " + this.type.getName() + " by name " + name);
        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : this.exceptions.entrySet()) {
            if (i == 1) {
                buf.append(", possible causes: ");
            }
            buf.append("\r\n(");
            buf.append(i++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(StringUtils.toString(entry.getValue()));
        }
        return new IllegalStateException(buf.toString());
    }

    private T createExtension(String name) {
        Class<?> clazz = this.getExtensionClasses().get(name);
        if (clazz == null) {
            throw this.findException(name);
        }
        try {
            Object instance = EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = EXTENSION_INSTANCES.get(clazz);
            }
            this.injectExtension(instance);
            Set<Class<?>> wrapperClasses = this.cachedWrapperClasses;
            if (wrapperClasses != null && wrapperClasses.size() > 0) {
                for (Class<?> wrapperClass : wrapperClasses) {
                    instance = this.injectExtension(wrapperClass.getConstructor(this.type).newInstance(instance));
                }
            }
            return (T)instance;
        }
        catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " + this.type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    private T injectExtension(T instance) {
        try {
            if (this.objectFactory != null) {
                for (Method method : instance.getClass().getMethods()) {
                    if (!method.getName().startsWith("set") || method.getParameterTypes().length != 1 || !Modifier.isPublic(method.getModifiers())) continue;
                    Class<?> pt = method.getParameterTypes()[0];
                    try {
                        String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                        Object object = this.objectFactory.getExtension(pt, property);
                        if (object == null) continue;
                        method.invoke(instance, object);
                    }
                    catch (Exception e) {
                        logger.error("fail to inject via method " + method.getName() + " of interface " + this.type.getName() + ": " + e.getMessage(), e);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return instance;
    }

    private Class<?> getExtensionClass(String name) {
        if (this.type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Extension name == null");
        }
        Class<?> clazz = this.getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalStateException("No such extension \"" + name + "\" for " + this.type.getName() + "!");
        }
        return clazz;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = this.cachedClasses.get();
        if (classes == null) {
            Holder<Map<String, Class<?>>> holder = this.cachedClasses;
            synchronized (holder) {
                classes = this.cachedClasses.get();
                if (classes == null) {
                    classes = this.loadExtensionClasses();
                    this.cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClasses() {
        String value;
        SPI defaultAnnotation = this.type.getAnnotation(SPI.class);
        if (defaultAnnotation != null && (value = defaultAnnotation.value()) != null && (value = value.trim()).length() > 0) {
            Object[] names = NAME_SEPARATOR.split(value);
            if (names.length > 1) {
                throw new IllegalStateException("more than 1 default extension name on extension " + this.type.getName() + ": " + Arrays.toString(names));
            }
            if (names.length == 1) {
                this.cachedDefaultName = names[0];
            }
        }
        HashMap extensionClasses = new HashMap();
        this.loadFile(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
        this.loadFile(extensionClasses, DUBBO_DIRECTORY);
        this.loadFile(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadFile(Map<String, Class<?>> extensionClasses, String dir) {
        String fileName = dir + this.type.getName();
        try {
            ClassLoader classLoader = ExtensionLoader.findClassLoader();
            Enumeration<java.net.URL> urls = classLoader != null ? classLoader.getResources(fileName) : ClassLoader.getSystemResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL url = urls.nextElement();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        try {
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                int ci = line.indexOf(35);
                                if (ci >= 0) {
                                    line = line.substring(0, ci);
                                }
                                if ((line = line.trim()).length() <= 0) continue;
                                try {
                                    String name = null;
                                    int i = line.indexOf(61);
                                    if (i > 0) {
                                        name = line.substring(0, i).trim();
                                        line = line.substring(i + 1).trim();
                                    }
                                    if (line.length() <= 0) continue;
                                    Class<?> clazz = Class.forName(line, true, classLoader);
                                    if (!this.type.isAssignableFrom(clazz)) {
                                        throw new IllegalStateException("Error when load extension class(interface: " + this.type + ", class line: " + clazz.getName() + "), class " + clazz.getName() + "is not subtype of interface.");
                                    }
                                    if (clazz.isAnnotationPresent(Adaptive.class)) {
                                        if (this.cachedAdaptiveClass == null) {
                                            this.cachedAdaptiveClass = clazz;
                                            continue;
                                        }
                                        if (this.cachedAdaptiveClass.equals(clazz)) continue;
                                        throw new IllegalStateException("More than 1 adaptive class found: " + this.cachedAdaptiveClass.getClass().getName() + ", " + clazz.getClass().getName());
                                    }
                                    try {
                                        clazz.getConstructor(this.type);
                                        Set<Class<?>> wrappers = this.cachedWrapperClasses;
                                        if (wrappers == null) {
                                            this.cachedWrapperClasses = new ConcurrentHashSet();
                                            wrappers = this.cachedWrapperClasses;
                                        }
                                        wrappers.add(clazz);
                                    }
                                    catch (NoSuchMethodException e) {
                                        String[] names;
                                        clazz.getConstructor(new Class[0]);
                                        if (!(name != null && name.length() != 0 || (name = this.findAnnotationName(clazz)) != null && name.length() != 0)) {
                                            if (clazz.getSimpleName().length() > this.type.getSimpleName().length() && clazz.getSimpleName().endsWith(this.type.getSimpleName())) {
                                                name = clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - this.type.getSimpleName().length()).toLowerCase();
                                            } else {
                                                throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + url);
                                            }
                                        }
                                        if ((names = NAME_SEPARATOR.split(name)) == null || names.length <= 0) continue;
                                        Activate activate = clazz.getAnnotation(Activate.class);
                                        if (activate != null) {
                                            this.cachedActivates.put(names[0], activate);
                                        }
                                        for (String n : names) {
                                            Class<?> c;
                                            if (!this.cachedNames.containsKey(clazz)) {
                                                this.cachedNames.put(clazz, n);
                                            }
                                            if ((c = extensionClasses.get(n)) == null) {
                                                extensionClasses.put(n, clazz);
                                                continue;
                                            }
                                            if (c == clazz) continue;
                                            throw new IllegalStateException("Duplicate extension " + this.type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                                        }
                                    }
                                }
                                catch (Throwable t) {
                                    IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + this.type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                    this.exceptions.put(line, e);
                                }
                            }
                        }
                        finally {
                            reader.close();
                        }
                    }
                    catch (Throwable t) {
                        logger.error("Exception when load extension class(interface: " + this.type + ", class file: " + url + ") in " + url, t);
                    }
                }
            }
        }
        catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " + this.type + ", description file: " + fileName + ").", t);
        }
    }

    private String findAnnotationName(Class<?> clazz) {
        Extension extension = clazz.getAnnotation(Extension.class);
        if (extension == null) {
            String name = clazz.getSimpleName();
            if (name.endsWith(this.type.getSimpleName())) {
                name = name.substring(0, name.length() - this.type.getSimpleName().length());
            }
            return name.toLowerCase();
        }
        return extension.value();
    }

    private T createAdaptiveExtension() {
        try {
            return (T)this.injectExtension(this.getAdaptiveExtensionClass().newInstance());
        }
        catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extenstion " + this.type + ", cause: " + e.getMessage(), e);
        }
    }

    private Class<?> getAdaptiveExtensionClass() {
        this.getExtensionClasses();
        if (this.cachedAdaptiveClass != null) {
            return this.cachedAdaptiveClass;
        }
        this.cachedAdaptiveClass = this.createAdaptiveExtensionClass();
        return this.cachedAdaptiveClass;
    }

    private Class<?> createAdaptiveExtensionClass() {
        String code = this.createAdaptiveExtensionClassCode();
        ClassLoader classLoader = ExtensionLoader.findClassLoader();
        Compiler compiler = ExtensionLoader.getExtensionLoader(Compiler.class).getAdaptiveExtension();
        return compiler.compile(code, classLoader);
    }

    private String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuidler = new StringBuilder();
        Method[] methods = this.type.getMethods();
        boolean hasAdaptiveAnnotation = false;
        for (Method m : methods) {
            if (!m.isAnnotationPresent(Adaptive.class)) continue;
            hasAdaptiveAnnotation = true;
            break;
        }
        if (!hasAdaptiveAnnotation) {
            throw new IllegalStateException("No adaptive method on extension " + this.type.getName() + ", refuse to create the adaptive class!");
        }
        codeBuidler.append("package " + this.type.getPackage().getName() + ";");
        codeBuidler.append("\nimport " + ExtensionLoader.class.getName() + ";");
        codeBuidler.append("\npublic class " + this.type.getSimpleName() + "$Adpative implements " + this.type.getCanonicalName() + " {");
        for (Method method : methods) {
            int i;
            Class<?> rt = method.getReturnType();
            Class<?>[] pts = method.getParameterTypes();
            Class<?>[] ets = method.getExceptionTypes();
            Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
            StringBuilder code = new StringBuilder(512);
            if (adaptiveAnnotation == null) {
                code.append("throw new UnsupportedOperationException(\"method ").append(method.toString()).append(" of interface ").append(this.type.getName()).append(" is not adaptive method!\");");
            } else {
                int urlTypeIndex = -1;
                for (int i2 = 0; i2 < pts.length; ++i2) {
                    if (!pts[i2].equals(URL.class)) continue;
                    urlTypeIndex = i2;
                    break;
                }
                if (urlTypeIndex != -1) {
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");", urlTypeIndex);
                    code.append(s);
                    s = String.format("\n%s url = arg%d;", URL.class.getName(), urlTypeIndex);
                    code.append(s);
                } else {
                    String attribMethod = null;
                    block3 : for (int i3 = 0; i3 < pts.length; ++i3) {
                        Method[] ms;
                        for (Method m : ms = pts[i3].getMethods()) {
                            String name = m.getName();
                            if (!name.startsWith("get") && name.length() <= 3 || !Modifier.isPublic(m.getModifiers()) || Modifier.isStatic(m.getModifiers()) || m.getParameterTypes().length != 0 || m.getReturnType() != URL.class) continue;
                            urlTypeIndex = i3;
                            attribMethod = name;
                            break block3;
                        }
                    }
                    if (attribMethod == null) {
                        throw new IllegalStateException("fail to create adative class for interface " + this.type.getName() + ": not found url parameter or url attribute in parameters of method " + method.getName());
                    }
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");", urlTypeIndex, pts[urlTypeIndex].getName());
                    code.append(s);
                    s = String.format("\nif (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");", urlTypeIndex, attribMethod, pts[urlTypeIndex].getName(), attribMethod);
                    code.append(s);
                    s = String.format("%s url = arg%d.%s();", URL.class.getName(), urlTypeIndex, attribMethod);
                    code.append(s);
                }
                Object[] value = adaptiveAnnotation.value();
                if (value.length == 0) {
                    char[] charArray = this.type.getSimpleName().toCharArray();
                    StringBuilder sb = new StringBuilder(128);
                    for (int i4 = 0; i4 < charArray.length; ++i4) {
                        if (Character.isUpperCase(charArray[i4])) {
                            if (i4 != 0) {
                                sb.append(".");
                            }
                            sb.append(Character.toLowerCase(charArray[i4]));
                            continue;
                        }
                        sb.append(charArray[i4]);
                    }
                    value = new String[]{sb.toString()};
                }
                boolean hasInvocation = false;
                for (int i5 = 0; i5 < pts.length; ++i5) {
                    if (!pts[i5].getName().equals("com.alibaba.dubbo.rpc.Invocation")) continue;
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"invocation == null\");", i5);
                    code.append(s);
                    s = String.format("\nString methodName = arg%d.getMethodName();", i5);
                    code.append(s);
                    hasInvocation = true;
                    break;
                }
                String defaultExtName = this.cachedDefaultName;
                String getNameCode = null;
                for (int i6 = value.length - 1; i6 >= 0; --i6) {
                    if (i6 == value.length - 1) {
                        if (null != defaultExtName) {
                            if (!"protocol".equals(value[i6])) {
                                if (hasInvocation) {
                                    getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i6], defaultExtName);
                                    continue;
                                }
                                getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i6], defaultExtName);
                                continue;
                            }
                            getNameCode = String.format("( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
                            continue;
                        }
                        if (!"protocol".equals(value[i6])) {
                            if (hasInvocation) {
                                getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i6], defaultExtName);
                                continue;
                            }
                            getNameCode = String.format("url.getParameter(\"%s\")", value[i6]);
                            continue;
                        }
                        getNameCode = "url.getProtocol()";
                        continue;
                    }
                    if (!"protocol".equals(value[i6])) {
                        if (hasInvocation) {
                            getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i6], defaultExtName);
                            continue;
                        }
                        getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i6], getNameCode);
                        continue;
                    }
                    getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                }
                code.append("\nString extName = ").append(getNameCode).append(";");
                String s = String.format("\nif(extName == null) throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");", this.type.getName(), Arrays.toString(value));
                code.append(s);
                s = String.format("\n%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);", this.type.getName(), ExtensionLoader.class.getSimpleName(), this.type.getName());
                code.append(s);
                if (!rt.equals(Void.TYPE)) {
                    code.append("\nreturn ");
                }
                s = String.format("extension.%s(", method.getName());
                code.append(s);
                for (int i7 = 0; i7 < pts.length; ++i7) {
                    if (i7 != 0) {
                        code.append(", ");
                    }
                    code.append("arg").append(i7);
                }
                code.append(");");
            }
            codeBuidler.append("\npublic " + rt.getCanonicalName() + " " + method.getName() + "(");
            for (i = 0; i < pts.length; ++i) {
                if (i > 0) {
                    codeBuidler.append(", ");
                }
                codeBuidler.append(pts[i].getCanonicalName());
                codeBuidler.append(" ");
                codeBuidler.append("arg" + i);
            }
            codeBuidler.append(")");
            if (ets.length > 0) {
                codeBuidler.append(" throws ");
                for (i = 0; i < ets.length; ++i) {
                    if (i > 0) {
                        codeBuidler.append(", ");
                    }
                    codeBuidler.append(pts[i].getCanonicalName());
                }
            }
            codeBuidler.append(" {");
            codeBuidler.append(code.toString());
            codeBuidler.append("\n}");
        }
        codeBuidler.append("\n}");
        if (logger.isDebugEnabled()) {
            logger.debug(codeBuidler.toString());
        }
        return codeBuidler.toString();
    }

    private static ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }

    public String toString() {
        return this.getClass().getName() + "[" + this.type.getName() + "]";
    }
}

