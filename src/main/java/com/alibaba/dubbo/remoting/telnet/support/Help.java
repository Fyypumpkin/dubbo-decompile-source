/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.telnet.support;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface Help {
    public String parameter() default "";

    public String summary();

    public String detail() default "";
}

