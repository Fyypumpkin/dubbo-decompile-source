/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.compiler.support;

import com.alibaba.dubbo.common.compiler.support.AbstractCompiler;
import com.alibaba.dubbo.common.compiler.support.ClassUtils;
import com.alibaba.dubbo.common.utils.ClassHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public class JdkCompiler
extends AbstractCompiler {
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector();
    private final ClassLoaderImpl classLoader;
    private final JavaFileManagerImpl javaFileManager;
    private volatile List<String> options = new ArrayList<String>();

    public JdkCompiler() {
        this.options.add("-target");
        this.options.add("1.6");
        StandardJavaFileManager manager = this.compiler.getStandardFileManager(this.diagnosticCollector, null, null);
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader instanceof URLClassLoader && !loader.getClass().getName().equals("sun.misc.Launcher$AppClassLoader")) {
            try {
                URLClassLoader urlClassLoader = (URLClassLoader)loader;
                ArrayList<File> files = new ArrayList<File>();
                for (URL url : urlClassLoader.getURLs()) {
                    files.add(new File(url.getFile()));
                }
                manager.setLocation(StandardLocation.CLASS_PATH, files);
            }
            catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        this.classLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoaderImpl>(){

            @Override
            public ClassLoaderImpl run() {
                return new ClassLoaderImpl(loader);
            }
        });
        this.javaFileManager = new JavaFileManagerImpl(manager, this.classLoader);
    }

    @Override
    public Class<?> doCompile(String name, String sourceCode) throws Throwable {
        int i = name.lastIndexOf(46);
        String packageName = i < 0 ? "" : name.substring(0, i);
        String className = i < 0 ? name : name.substring(i + 1);
        JavaFileObjectImpl javaFileObject = new JavaFileObjectImpl(className, sourceCode);
        this.javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName, className + ".java", javaFileObject);
        Boolean result = this.compiler.getTask(null, this.javaFileManager, this.diagnosticCollector, this.options, null, Arrays.asList(javaFileObject)).call();
        if (result == null || !result.booleanValue()) {
            throw new IllegalStateException("Compilation failed. class: " + name + ", diagnostics: " + this.diagnosticCollector);
        }
        return this.classLoader.loadClass(name);
    }

    private static final class JavaFileManagerImpl
    extends ForwardingJavaFileManager<JavaFileManager> {
        private final ClassLoaderImpl classLoader;
        private final Map<URI, JavaFileObject> fileObjects = new HashMap<URI, JavaFileObject>();

        public JavaFileManagerImpl(JavaFileManager fileManager, ClassLoaderImpl classLoader) {
            super(fileManager);
            this.classLoader = classLoader;
        }

        @Override
        public FileObject getFileForInput(JavaFileManager.Location location, String packageName, String relativeName) throws IOException {
            FileObject o = this.fileObjects.get(this.uri(location, packageName, relativeName));
            if (o != null) {
                return o;
            }
            return super.getFileForInput(location, packageName, relativeName);
        }

        public void putFileForInput(StandardLocation location, String packageName, String relativeName, JavaFileObject file) {
            this.fileObjects.put(this.uri(location, packageName, relativeName), file);
        }

        private URI uri(JavaFileManager.Location location, String packageName, String relativeName) {
            return ClassUtils.toURI(location.getName() + '/' + packageName + '/' + relativeName);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String qualifiedName, JavaFileObject.Kind kind, FileObject outputFile) throws IOException {
            JavaFileObjectImpl file = new JavaFileObjectImpl(qualifiedName, kind);
            this.classLoader.add(qualifiedName, file);
            return file;
        }

        @Override
        public ClassLoader getClassLoader(JavaFileManager.Location location) {
            return this.classLoader;
        }

        @Override
        public String inferBinaryName(JavaFileManager.Location loc, JavaFileObject file) {
            if (file instanceof JavaFileObjectImpl) {
                return file.getName();
            }
            return super.inferBinaryName(loc, file);
        }

        @Override
        public Iterable<JavaFileObject> list(JavaFileManager.Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
            Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            ArrayList<URL> urlList = new ArrayList<URL>();
            Enumeration<URL> e = contextClassLoader.getResources("com");
            while (e.hasMoreElements()) {
                urlList.add(e.nextElement());
            }
            ArrayList<JavaFileObject> files = new ArrayList<JavaFileObject>();
            if (location == StandardLocation.CLASS_PATH && kinds.contains((Object)JavaFileObject.Kind.CLASS)) {
                for (JavaFileObject file : this.fileObjects.values()) {
                    if (file.getKind() != JavaFileObject.Kind.CLASS || !file.getName().startsWith(packageName)) continue;
                    files.add(file);
                }
                files.addAll(this.classLoader.files());
            } else if (location == StandardLocation.SOURCE_PATH && kinds.contains((Object)JavaFileObject.Kind.SOURCE)) {
                for (JavaFileObject file : this.fileObjects.values()) {
                    if (file.getKind() != JavaFileObject.Kind.SOURCE || !file.getName().startsWith(packageName)) continue;
                    files.add(file);
                }
            }
            for (JavaFileObject file : result) {
                files.add(file);
            }
            return files;
        }
    }

    private static final class JavaFileObjectImpl
    extends SimpleJavaFileObject {
        private ByteArrayOutputStream bytecode;
        private final CharSequence source;

        public JavaFileObjectImpl(String baseName, CharSequence source) {
            super(ClassUtils.toURI(baseName + ".java"), JavaFileObject.Kind.SOURCE);
            this.source = source;
        }

        JavaFileObjectImpl(String name, JavaFileObject.Kind kind) {
            super(ClassUtils.toURI(name), kind);
            this.source = null;
        }

        public JavaFileObjectImpl(URI uri, JavaFileObject.Kind kind) {
            super(uri, kind);
            this.source = null;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws UnsupportedOperationException {
            if (this.source == null) {
                throw new UnsupportedOperationException("source == null");
            }
            return this.source;
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(this.getByteCode());
        }

        @Override
        public OutputStream openOutputStream() {
            this.bytecode = new ByteArrayOutputStream();
            return this.bytecode;
        }

        public byte[] getByteCode() {
            return this.bytecode.toByteArray();
        }
    }

    private final class ClassLoaderImpl
    extends ClassLoader {
        private final Map<String, JavaFileObject> classes;

        ClassLoaderImpl(ClassLoader parentClassLoader) {
            super(parentClassLoader);
            this.classes = new HashMap<String, JavaFileObject>();
        }

        Collection<JavaFileObject> files() {
            return Collections.unmodifiableCollection(this.classes.values());
        }

        @Override
        protected Class<?> findClass(String qualifiedClassName) throws ClassNotFoundException {
            JavaFileObject file = this.classes.get(qualifiedClassName);
            if (file != null) {
                byte[] bytes = ((JavaFileObjectImpl)file).getByteCode();
                return this.defineClass(qualifiedClassName, bytes, 0, bytes.length);
            }
            try {
                return ClassHelper.forNameWithCallerClassLoader(qualifiedClassName, this.getClass());
            }
            catch (ClassNotFoundException nf) {
                return super.findClass(qualifiedClassName);
            }
        }

        void add(String qualifiedClassName, JavaFileObject javaFile) {
            this.classes.put(qualifiedClassName, javaFile);
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            JavaFileObjectImpl file;
            String qualifiedClassName;
            if (name.endsWith(".class") && (file = (JavaFileObjectImpl)this.classes.get(qualifiedClassName = name.substring(0, name.length() - ".class".length()).replace('/', '.'))) != null) {
                return new ByteArrayInputStream(file.getByteCode());
            }
            return super.getResourceAsStream(name);
        }
    }

}

