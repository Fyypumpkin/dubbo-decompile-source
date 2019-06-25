/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Reference {
    public Class<?> interfaceClass() default void.class;

    public String interfaceName() default "";

    public String version() default "";

    public String group() default "";

    public String url() default "";

    public String client() default "";

    public boolean generic() default false;

    public boolean injvm() default false;

    public boolean check() default true;

    public boolean init() default false;

    public boolean lazy() default false;

    public boolean stubevent() default false;

    public String reconnect() default "";

    public boolean sticky() default false;

    public String proxy() default "";

    public String stub() default "";

    public String cluster() default "";

    public int connections() default 0;

    public int callbacks() default 0;

    public String onconnect() default "";

    public String ondisconnect() default "";

    public String owner() default "";

    public String layer() default "";

    public int retries() default 0;

    public String loadbalance() default "";

    public boolean async() default false;

    public int actives() default 0;

    public boolean sent() default false;

    public String mock() default "";

    public String validation() default "";

    public int timeout() default 0;

    public String cache() default "";

    public String[] filter() default {};

    public String[] listener() default {};

    public String[] parameters() default {};

    public String application() default "";

    public String module() default "";

    public String consumer() default "";

    public String monitor() default "";

    public String protocol() default "";

    public String[] registry() default {};
}

