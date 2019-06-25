/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.validation;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.validation.Validator;

@SPI(value="jvalidation")
public interface Validation {
    @Adaptive(value={"validation"})
    public Validator getValidator(URL var1);
}

