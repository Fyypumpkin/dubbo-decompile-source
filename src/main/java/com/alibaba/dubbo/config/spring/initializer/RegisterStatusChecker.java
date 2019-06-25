/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.spring.initializer;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.model.ApplicationModel;
import com.alibaba.dubbo.config.model.ProviderModel;
import com.alibaba.dubbo.registry.support.ProviderConsumerRegTable;
import com.alibaba.dubbo.registry.support.ProviderInvokerWrapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegisterStatusChecker {
    private static Logger logger = LoggerFactory.getLogger(RegisterStatusChecker.class);

    public static Boolean check() {
        HashSet<String> unRegisterServices = new HashSet<String>();
        List<ProviderModel> providerModelList = ApplicationModel.allProviderModels();
        for (ProviderModel providerModel : providerModelList) {
            Set<ProviderInvokerWrapper> providerInvokerWrapperSet = ProviderConsumerRegTable.getProviderInvoker(providerModel.getServiceName());
            for (ProviderInvokerWrapper providerInvokerWrapper : providerInvokerWrapperSet) {
                URL providerUrl = providerInvokerWrapper.getProviderUrl();
                if (providerInvokerWrapper.isReg()) continue;
                unRegisterServices.add(providerUrl.getServiceInterface());
            }
        }
        if (!unRegisterServices.isEmpty()) {
            logger.error("Unregister services: " + ((Object)unRegisterServices).toString());
            return false;
        }
        return true;
    }
}

