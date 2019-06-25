/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.validation.filter;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.validation.Validation;
import com.alibaba.dubbo.validation.Validator;

@Activate(group={"consumer", "provider"}, value={"validation"}, order=10000)
public class ValidationFilter
implements Filter {
    private Validation validation;

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (this.validation != null && !invocation.getMethodName().startsWith("$") && ConfigUtils.isNotEmpty(invoker.getUrl().getMethodParameter(invocation.getMethodName(), "validation"))) {
            try {
                Validator validator = this.validation.getValidator(invoker.getUrl());
                if (validator != null) {
                    validator.validate(invocation.getMethodName(), invocation.getParameterTypes(), invocation.getArguments());
                }
            }
            catch (RpcException e) {
                throw e;
            }
            catch (Throwable t) {
                return new RpcResult(t);
            }
        }
        return invoker.invoke(invocation);
    }
}

