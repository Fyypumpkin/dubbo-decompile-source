/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javassist.ClassPath
 *  javassist.ClassPool
 *  javassist.CtClass
 *  javassist.CtConstructor
 *  javassist.CtField
 *  javassist.CtMethod
 *  javassist.CtNewConstructor
 *  javassist.CtNewMethod
 *  javassist.LoaderClassPath
 */
package com.alibaba.dubbo.common.compiler.support;

import com.alibaba.dubbo.common.compiler.support.AbstractCompiler;
import com.alibaba.dubbo.common.compiler.support.ClassUtils;
import com.alibaba.dubbo.common.utils.ClassHelper;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;

public class JavassistCompiler
extends AbstractCompiler {
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.\\*]+);\n");
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("\\s+extends\\s+([\\w\\.]+)[^\\{]*\\{\n");
    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\s+implements\\s+([\\w\\.]+)\\s*\\{\n");
    private static final Pattern METHODS_PATTERN = Pattern.compile("\n(private|public|protected)\\s+");
    private static final Pattern FIELD_PATTERN = Pattern.compile("[^\n]+=[^\n]+;");

    @Override
    public Class<?> doCompile(String name, String source) throws Throwable {
        String[] methods;
        CtClass cls;
        int i = name.lastIndexOf(46);
        String className = i < 0 ? name : name.substring(i + 1);
        ClassPool pool = new ClassPool(true);
        pool.insertClassPath((ClassPath)new LoaderClassPath(ClassHelper.getCallerClassLoader(this.getClass())));
        Matcher matcher = IMPORT_PATTERN.matcher(source);
        ArrayList<String> importPackages = new ArrayList<String>();
        HashMap<String, String> fullNames = new HashMap<String, String>();
        while (matcher.find()) {
            String pkg = matcher.group(1);
            if (pkg.endsWith(".*")) {
                String pkgName = pkg.substring(0, pkg.length() - 2);
                pool.importPackage(pkgName);
                importPackages.add(pkgName);
                continue;
            }
            int pi = pkg.lastIndexOf(46);
            if (pi <= 0) continue;
            String pkgName = pkg.substring(0, pi);
            pool.importPackage(pkgName);
            importPackages.add(pkgName);
            fullNames.put(pkg.substring(pi + 1), pkg);
        }
        String[] packages = importPackages.toArray(new String[0]);
        matcher = EXTENDS_PATTERN.matcher(source);
        if (matcher.find()) {
            String[] extend = matcher.group(1).trim();
            Object extendClass = extend.contains(".") ? extend : (fullNames.containsKey(extend) ? (String)fullNames.get(extend) : ClassUtils.forName(packages, (String)extend).getName());
            cls = pool.makeClass(name, pool.get((String)extendClass));
        } else {
            cls = pool.makeClass(name);
        }
        matcher = IMPLEMENTS_PATTERN.matcher(source);
        if (matcher.find()) {
            String[] ifaces;
            for (String iface : ifaces = matcher.group(1).trim().split("\\,")) {
                String ifaceClass = (iface = iface.trim()).contains(".") ? iface : (fullNames.containsKey(iface) ? (String)fullNames.get(iface) : ClassUtils.forName(packages, iface).getName());
                cls.addInterface(pool.get(ifaceClass));
            }
        }
        String body = source.substring(source.indexOf("{") + 1, source.length() - 1);
        for (String method : methods = METHODS_PATTERN.split(body)) {
            if ((method = method.trim()).length() <= 0) continue;
            if (method.startsWith(className)) {
                cls.addConstructor(CtNewConstructor.make((String)("public " + method), (CtClass)cls));
                continue;
            }
            if (FIELD_PATTERN.matcher(method).matches()) {
                cls.addField(CtField.make((String)("private " + method), (CtClass)cls));
                continue;
            }
            cls.addMethod(CtNewMethod.make((String)("public " + method), (CtClass)cls));
        }
        return cls.toClass(ClassHelper.getCallerClassLoader(this.getClass()), JavassistCompiler.class.getProtectionDomain());
    }
}

