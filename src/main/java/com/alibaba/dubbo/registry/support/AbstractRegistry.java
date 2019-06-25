/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.Registry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractRegistry
implements Registry {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final char URL_SEPARATOR = ' ';
    private static final String URL_SPLIT = "\\s+";
    private URL registryUrl;
    private File file;
    private final Properties properties = new Properties();
    private final ExecutorService registryCacheExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("DubboSaveRegistryCache", true));
    private final boolean syncSaveFile;
    private final AtomicLong lastCacheChanged = new AtomicLong();
    private final Set<URL> registered = new ConcurrentHashSet<URL>();
    private final ConcurrentMap<URL, Set<NotifyListener>> subscribed = new ConcurrentHashMap<URL, Set<NotifyListener>>();
    private final ConcurrentMap<URL, Map<String, List<URL>>> notified = new ConcurrentHashMap<URL, Map<String, List<URL>>>();
    private static Boolean closeFileCache;

    public AbstractRegistry(URL url) {
        this.setUrl(url);
        this.syncSaveFile = url.getParameter("save.file", false);
        String filename = url.getParameter("file", System.getProperty("user.home") + "/.dubbo/dubbo-registry-" + url.getHost() + ".cache");
        closeFileCache = url.getParameter("registry.cache.close", Boolean.parseBoolean(System.getProperty("registry.cache.close")));
        File file = null;
        if (ConfigUtils.isNotEmpty(filename) && !(file = new File(filename)).exists() && file.getParentFile() != null && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IllegalArgumentException("Invalid registry store file " + file + ", cause: Failed to create directory " + file.getParentFile() + "!");
        }
        this.file = file;
        this.loadProperties();
        this.notify(url.getBackupUrls());
    }

    protected void setUrl(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("registry url == null");
        }
        this.registryUrl = url;
    }

    @Override
    public URL getUrl() {
        return this.registryUrl;
    }

    public Set<URL> getRegistered() {
        return this.registered;
    }

    public Map<URL, Set<NotifyListener>> getSubscribed() {
        return this.subscribed;
    }

    public Map<URL, Map<String, List<URL>>> getNotified() {
        return this.notified;
    }

    public File getCacheFile() {
        return this.file;
    }

    public Properties getCacheProperties() {
        return this.properties;
    }

    public AtomicLong getLastCacheChanged() {
        return this.lastCacheChanged;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doSaveProperties(long version) {
        Properties newProperties;
        if (version < this.lastCacheChanged.get()) {
            return;
        }
        if (this.file == null) {
            return;
        }
        newProperties = new Properties();
        InputStream in = null;
        try {
            if (this.file.exists()) {
                in = new FileInputStream(this.file);
                newProperties.load(in);
            }
        }
        catch (Throwable e) {
            this.logger.warn("Failed to load registry store file, cause: " + e.getMessage(), e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    this.logger.warn(e.getMessage(), e);
                }
            }
        }
        try {
            newProperties.putAll(this.properties);
            File lockfile = new File(this.file.getAbsolutePath() + ".lock");
            if (!lockfile.exists()) {
                lockfile.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(lockfile, "rw");
            try {
                FileChannel channel = raf.getChannel();
                try {
                    FileLock lock = channel.tryLock();
                    if (lock == null) {
                        throw new IOException("Can not lock the registry cache file " + this.file.getAbsolutePath() + ", ignore and retry later, maybe multi java process use the file, please config: dubbo.registry.file=xxx.properties");
                    }
                    try {
                        if (!this.file.exists()) {
                            this.file.createNewFile();
                        }
                        FileOutputStream outputFile = new FileOutputStream(this.file);
                        try {
                            newProperties.store(outputFile, "Dubbo Registry Cache");
                        }
                        finally {
                            outputFile.close();
                        }
                    }
                    finally {
                        lock.release();
                    }
                }
                finally {
                    channel.close();
                }
            }
            finally {
                raf.close();
            }
        }
        catch (Throwable e) {
            if (version < this.lastCacheChanged.get()) {
                return;
            }
            this.registryCacheExecutor.execute(new SaveProperties(this.lastCacheChanged.incrementAndGet()));
            this.logger.warn("Failed to save registry store file, cause: " + e.getMessage(), e);
        }
    }

    private void loadProperties() {
        if (this.file != null && this.file.exists()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(this.file);
                this.properties.load(in);
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Load registry store file " + this.file + ", data: " + this.properties);
                }
            }
            catch (Throwable e) {
                this.logger.warn("Failed to load registry store file " + this.file, e);
            }
            finally {
                if (in != null) {
                    try {
                        ((InputStream)in).close();
                    }
                    catch (IOException e) {
                        this.logger.warn(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public List<URL> getCacheUrls(URL url) {
        for (Map.Entry entry : this.properties.entrySet()) {
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            if (key == null || key.length() <= 0 || !key.equals(url.getServiceKey()) || !Character.isLetter(key.charAt(0)) && key.charAt(0) != '_' || value == null || value.length() <= 0) continue;
            String[] arr = value.trim().split(URL_SPLIT);
            ArrayList<URL> urls = new ArrayList<URL>();
            for (String u : arr) {
                urls.add(URL.valueOf(u));
            }
            return urls;
        }
        return null;
    }

    @Override
    public List<URL> lookup(URL url) {
        ArrayList<URL> result;
        block4 : {
            block3 : {
                result = new ArrayList<URL>();
                Map<String, List<URL>> notifiedUrls = this.getNotified().get(url);
                if (notifiedUrls == null || notifiedUrls.size() <= 0) break block3;
                for (List<URL> urls : notifiedUrls.values()) {
                    for (URL u : urls) {
                        if ("empty".equals(u.getProtocol())) continue;
                        result.add(u);
                    }
                }
                break block4;
            }
            final AtomicReference reference = new AtomicReference();
            NotifyListener listener = new NotifyListener(){

                @Override
                public void notify(List<URL> urls) {
                    reference.set(urls);
                }
            };
            this.subscribe(url, listener);
            List urls = (List)reference.get();
            if (urls == null || urls.size() <= 0) break block4;
            for (URL u : urls) {
                if ("empty".equals(u.getProtocol())) continue;
                result.add(u);
            }
        }
        return result;
    }

    @Override
    public void register(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("register url == null");
        }
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Register: " + url);
        }
        this.registered.add(url);
    }

    @Override
    public void unregister(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("unregister url == null");
        }
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Unregister: " + url);
        }
        this.registered.remove(url);
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        Set listeners;
        if (url == null) {
            throw new IllegalArgumentException("subscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener == null");
        }
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Subscribe: " + url);
        }
        if ((listeners = (Set)this.subscribed.get(url)) == null) {
            this.subscribed.putIfAbsent(url, new ConcurrentHashSet());
            listeners = (Set)this.subscribed.get(url);
        }
        listeners.add(listener);
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        Set listeners;
        if (url == null) {
            throw new IllegalArgumentException("unsubscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("unsubscribe listener == null");
        }
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Unsubscribe: " + url);
        }
        if ((listeners = (Set)this.subscribed.get(url)) != null) {
            listeners.remove(listener);
        }
    }

    protected void recover() throws Exception {
        HashMap<URL, Set<NotifyListener>> recoverSubscribed;
        HashSet<URL> recoverRegistered = new HashSet<URL>(this.getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Recover register url " + recoverRegistered);
            }
            for (URL url : recoverRegistered) {
                this.register(url);
            }
        }
        if (!(recoverSubscribed = new HashMap<URL, Set<NotifyListener>>(this.getSubscribed())).isEmpty()) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Recover subscribe url " + recoverSubscribed.keySet());
            }
            for (Map.Entry entry : recoverSubscribed.entrySet()) {
                URL url = (URL)entry.getKey();
                for (NotifyListener listener : (Set)entry.getValue()) {
                    this.subscribe(url, listener);
                }
            }
        }
    }

    protected static List<URL> filterEmpty(URL url, List<URL> urls) {
        if (urls == null || urls.size() == 0) {
            ArrayList<URL> result = new ArrayList<URL>(1);
            result.add(url.setProtocol("empty"));
            return result;
        }
        return urls;
    }

    protected void notify(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        for (Map.Entry<URL, Set<NotifyListener>> entry : this.getSubscribed().entrySet()) {
            Set<NotifyListener> listeners;
            URL url = entry.getKey();
            if (!UrlUtils.isMatch(url, urls.get(0)) || (listeners = entry.getValue()) == null) continue;
            for (NotifyListener listener : listeners) {
                try {
                    this.notify(url, listener, AbstractRegistry.filterEmpty(url, urls));
                }
                catch (Throwable t) {
                    this.logger.error("Failed to notify registry event, urls: " + urls + ", cause: " + t.getMessage(), t);
                }
            }
        }
    }

    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        if (!(urls != null && urls.size() != 0 || "*".equals(url.getServiceInterface()))) {
            this.logger.warn("Ignore empty notify urls for subscribe url " + url);
            return;
        }
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Notify urls for subscribe url " + url + ", urls: " + urls);
        }
        HashMap<String, ArrayList<URL>> result = new HashMap<String, ArrayList<URL>>();
        for (URL u : urls) {
            if (!UrlUtils.isMatch(url, u)) continue;
            String category = u.getParameter("category", "providers");
            ArrayList<URL> categoryList = (ArrayList<URL>)result.get(category);
            if (categoryList == null) {
                categoryList = new ArrayList<URL>();
                result.put(category, categoryList);
            }
            categoryList.add(u);
        }
        if (result.size() == 0) {
            return;
        }
        Map categoryNotified = (Map)this.notified.get(url);
        if (!closeFileCache.booleanValue() && categoryNotified == null) {
            this.notified.putIfAbsent(url, new ConcurrentHashMap());
            categoryNotified = (Map)this.notified.get(url);
        }
        for (Map.Entry entry : result.entrySet()) {
            String category = (String)entry.getKey();
            List categoryList = (List)entry.getValue();
            if (!closeFileCache.booleanValue()) {
                categoryNotified.put(category, categoryList);
                this.saveProperties(url);
            }
            listener.notify(categoryList);
        }
    }

    private void saveProperties(URL url) {
        if (this.file == null) {
            return;
        }
        try {
            StringBuilder buf = new StringBuilder();
            Map categoryNotified = (Map)this.notified.get(url);
            if (categoryNotified != null) {
                for (List us : categoryNotified.values()) {
                    for (URL u : us) {
                        if (buf.length() > 0) {
                            buf.append(' ');
                        }
                        buf.append(u.toFullString());
                    }
                }
            }
            this.properties.setProperty(url.getServiceKey(), buf.toString());
            long version = this.lastCacheChanged.incrementAndGet();
            if (this.syncSaveFile) {
                this.doSaveProperties(version);
            } else {
                this.registryCacheExecutor.execute(new SaveProperties(version));
            }
        }
        catch (Throwable t) {
            this.logger.warn(t.getMessage(), t);
        }
    }

    @Override
    public void destroy() {
        HashSet<URL> destroyRegistered;
        HashMap<URL, Set<NotifyListener>> destroySubscribed;
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Destroy registry:" + this.getUrl());
        }
        if (!(destroyRegistered = new HashSet<URL>(this.getRegistered())).isEmpty()) {
            for (URL url : new HashSet<URL>(this.getRegistered())) {
                if (!url.getParameter("dynamic", true)) continue;
                try {
                    this.unregister(url);
                    if (!this.logger.isInfoEnabled()) continue;
                    this.logger.info("Destroy unregister url " + url);
                }
                catch (Throwable t) {
                    this.logger.warn("Failed to unregister url " + url + " to registry " + this.getUrl() + " on destroy, cause: " + t.getMessage(), t);
                }
            }
        }
        if (!(destroySubscribed = new HashMap<URL, Set<NotifyListener>>(this.getSubscribed())).isEmpty()) {
            for (Map.Entry entry : destroySubscribed.entrySet()) {
                URL url = (URL)entry.getKey();
                for (NotifyListener listener : (Set)entry.getValue()) {
                    try {
                        this.unsubscribe(url, listener);
                        if (!this.logger.isInfoEnabled()) continue;
                        this.logger.info("Destroy unsubscribe url " + url);
                    }
                    catch (Throwable t) {
                        this.logger.warn("Failed to unsubscribe url " + url + " to registry " + this.getUrl() + " on destroy, cause: " + t.getMessage(), t);
                    }
                }
            }
        }
    }

    public String toString() {
        return this.getUrl().toString();
    }

    private class SaveProperties
    implements Runnable {
        private long version;

        private SaveProperties(long version) {
            this.version = version;
        }

        @Override
        public void run() {
            AbstractRegistry.this.doSaveProperties(this.version);
        }
    }

}

