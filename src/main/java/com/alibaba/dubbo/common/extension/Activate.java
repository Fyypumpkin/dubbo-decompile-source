/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.extension;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE, ElementType.METHOD})
public @interface Activate {
    public String[] group() default {};

    public String[] value() default {};

    public String[] before() default {};

    public String[] after() default {};

    public int order() default 0;
}
