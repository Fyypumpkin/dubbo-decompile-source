/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.support;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
public @interface Parameter {
    public String key() default "";

    public boolean required() default false;

    public boolean excluded() default false;

    public boolean escaped() default false;

    public boolean attribute() default false;

    public boolean append() default false;
}

