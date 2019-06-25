/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.validation.support.jvalidation;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.validation.Validator;
import com.alibaba.dubbo.validation.support.AbstractValidation;
import com.alibaba.dubbo.validation.support.jvalidation.JValidator;

public class JValidation
extends AbstractValidation {
    @Override
    protected Validator createValidator(URL url) {
        return new JValidator(url);
    }
}

