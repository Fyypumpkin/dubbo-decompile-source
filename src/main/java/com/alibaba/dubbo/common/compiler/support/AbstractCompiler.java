/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.compiler.support;

import com.alibaba.dubbo.common.compiler.Compiler;
import com.alibaba.dubbo.common.compiler.support.ClassUtils;
import com.alibaba.dubbo.common.utils.ClassHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractCompiler
implements Compiler {
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9\\.]*);");
    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");

    @Override
    public Class<?> compile(String code, ClassLoader classLoader) {
        Matcher matcher = PACKAGE_PATTERN.matcher(code = code.trim());
        String pkg = matcher.find() ? matcher.group(1) : "";
        matcher = CLASS_PATTERN.matcher(code);
        if (!matcher.find()) {
            throw new IllegalArgumentException("No such class name in " + code);
        }
        String cls = matcher.group(1);
        String className = pkg != null && pkg.length() > 0 ? pkg + "." + cls : cls;
        try {
            return Class.forName(className, true, ClassHelper.getCallerClassLoader(this.getClass()));
        }
        catch (ClassNotFoundException e) {
            if (!code.endsWith("}")) {
                throw new IllegalStateException("The java code not endsWith \"}\", code: \n" + code + "\n");
            }
            try {
                return this.doCompile(className, code);
            }
            catch (RuntimeException t) {
                throw t;
            }
            catch (Throwable t) {
                throw new IllegalStateException("Failed to compile class, cause: " + t.getMessage() + ", class: " + className + ", code: \n" + code + "\n, stack: " + ClassUtils.toString(t));
            }
        }
    }

    protected abstract Class<?> doCompile(String var1, String var2) throws Throwable;
}

