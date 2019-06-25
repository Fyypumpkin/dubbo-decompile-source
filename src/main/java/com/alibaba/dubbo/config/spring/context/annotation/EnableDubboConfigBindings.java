/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Import
 */
package com.alibaba.dubbo.config.spring.context.annotation;

import com.alibaba.dubbo.config.spring.context.annotation.DubboConfigBindingsRegistrar;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubboConfigBinding;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
@Documented
@Import(value={DubboConfigBindingsRegistrar.class})
public @interface EnableDubboConfigBindings {
    public EnableDubboConfigBinding[] value();
}

