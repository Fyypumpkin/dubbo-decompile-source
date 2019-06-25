/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.core.annotation.AliasFor
 */
package com.alibaba.dubbo.config.spring.context.annotation;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubboConfig;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
@Inherited
@Documented
@EnableDubboConfig
@DubboComponentScan
public @interface EnableDubbo {
    @AliasFor(annotation=DubboComponentScan.class, attribute="basePackages")
    public String[] scanBasePackages() default {};

    @AliasFor(annotation=DubboComponentScan.class, attribute="basePackageClasses")
    public Class<?>[] scanBasePackageClasses() default {};

    @AliasFor(annotation=EnableDubboConfig.class, attribute="multiple")
    public boolean multipleConfig() default false;
}

