/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.BeanFactoryUtils
 *  org.springframework.beans.factory.ListableBeanFactory
 */
package com.alibaba.dubbo.config.spring.util;

import com.alibaba.dubbo.common.utils.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ListableBeanFactory;

public class BeanFactoryUtils {
    public static <T> T getOptionalBean(ListableBeanFactory beanFactory, String beanName, Class<T> beanType) {
        String[] allBeanNames = org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors((ListableBeanFactory)beanFactory, beanType);
        if (!StringUtils.isContains(allBeanNames, beanName)) {
            return null;
        }
        Map beansOfType = org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors((ListableBeanFactory)beanFactory, beanType);
        return (T)beansOfType.get(beanName);
    }

    public static <T> List<T> getBeans(ListableBeanFactory beanFactory, String[] beanNames, Class<T> beanType) {
        String[] allBeanNames = org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors((ListableBeanFactory)beanFactory, beanType);
        ArrayList<Object> beans = new ArrayList<Object>(beanNames.length);
        for (String beanName : beanNames) {
            if (!StringUtils.isContains(allBeanNames, beanName)) continue;
            beans.add(beanFactory.getBean(beanName, beanType));
        }
        return Collections.unmodifiableList(beans);
    }
}

