/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
@Inherited
public @interface Service {
    public Class<?> interfaceClass() default void.class;

    public String interfaceName() default "";

    public String version() default "";

    public String group() default "";

    public String path() default "";

    public boolean export() default false;

    public String token() default "";

    public boolean deprecated() default false;

    public boolean dynamic() default false;

    public String accesslog() default "";

    public int executes() default 0;

    public boolean register() default true;

    public int weight() default 0;

    public String document() default "";

    public int delay() default 0;

    public String local() default "";

    public String stub() default "";

    public String cluster() default "";

    public String proxy() default "";

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

    public String provider() default "";

    public String[] protocol() default {};

    public String monitor() default "";

    public String[] registry() default {};
}

