/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.MutablePropertyValues
 *  org.springframework.beans.PropertyValue
 *  org.springframework.beans.factory.config.BeanDefinition
 *  org.springframework.beans.factory.config.BeanDefinitionHolder
 *  org.springframework.beans.factory.config.RuntimeBeanReference
 *  org.springframework.beans.factory.config.TypedStringValue
 *  org.springframework.beans.factory.support.BeanDefinitionRegistry
 *  org.springframework.beans.factory.support.ManagedList
 *  org.springframework.beans.factory.support.ManagedMap
 *  org.springframework.beans.factory.support.RootBeanDefinition
 *  org.springframework.beans.factory.xml.BeanDefinitionParser
 *  org.springframework.beans.factory.xml.ParserContext
 */
package com.alibaba.dubbo.config.spring.schema;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.ArgumentConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.rpc.Protocol;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Pattern;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DubboBeanDefinitionParser
implements BeanDefinitionParser {
    private static final Logger logger = LoggerFactory.getLogger(DubboBeanDefinitionParser.class);
    private static final Pattern GROUP_AND_VERION = Pattern.compile("^[\\-.0-9_a-zA-Z]+(\\:[\\-.0-9_a-zA-Z]+)?$");
    private final Class<?> beanClass;
    private final boolean required;

    public DubboBeanDefinitionParser(Class<?> beanClass, boolean required) {
        this.beanClass = beanClass;
        this.required = required;
    }

    private static BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass, boolean required) {
        String name;
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);
        Object id = element.getAttribute("id");
        if ((id == null || ((String)id).length() == 0) && required) {
            Object generatedBeanName = element.getAttribute("name");
            if (generatedBeanName == null || generatedBeanName.length() == 0) {
                generatedBeanName = ProtocolConfig.class.equals(beanClass) ? "dubbo" : element.getAttribute("interface");
            }
            if (generatedBeanName == null || generatedBeanName.length() == 0) {
                generatedBeanName = beanClass.getName();
            }
            id = generatedBeanName;
            int counter = 2;
            while (parserContext.getRegistry().containsBeanDefinition((String)id)) {
                id = (String)generatedBeanName + counter++;
            }
        }
        if (id != null && ((String)id).length() > 0) {
            if (parserContext.getRegistry().containsBeanDefinition((String)id)) {
                throw new IllegalStateException("Duplicate spring bean id " + (String)id);
            }
            parserContext.getRegistry().registerBeanDefinition((String)id, (BeanDefinition)beanDefinition);
            beanDefinition.getPropertyValues().addPropertyValue("id", id);
        }
        if (ProtocolConfig.class.equals(beanClass)) {
            for (String name2 : parserContext.getRegistry().getBeanDefinitionNames()) {
                Object value;
                BeanDefinition definition = parserContext.getRegistry().getBeanDefinition(name2);
                PropertyValue property = definition.getPropertyValues().getPropertyValue("protocol");
                if (property == null || !((value = property.getValue()) instanceof ProtocolConfig) || !((String)id).equals(((ProtocolConfig)value).getName())) continue;
                definition.getPropertyValues().addPropertyValue("protocol", (Object)new RuntimeBeanReference((String)id));
            }
        } else if (ServiceBean.class.equals(beanClass)) {
            String className = element.getAttribute("class");
            if (className != null && className.length() > 0) {
                RootBeanDefinition classDefinition = new RootBeanDefinition();
                classDefinition.setBeanClass(ReflectUtils.forName(className));
                classDefinition.setLazyInit(false);
                DubboBeanDefinitionParser.parseProperties(element.getChildNodes(), classDefinition);
                beanDefinition.getPropertyValues().addPropertyValue("ref", (Object)new BeanDefinitionHolder((BeanDefinition)classDefinition, (String)id + "Impl"));
            }
        } else if (ProviderConfig.class.equals(beanClass)) {
            DubboBeanDefinitionParser.parseNested(element, parserContext, ServiceBean.class, true, "service", "provider", (String)id, (BeanDefinition)beanDefinition);
        } else if (ConsumerConfig.class.equals(beanClass)) {
            DubboBeanDefinitionParser.parseNested(element, parserContext, ReferenceBean.class, false, "reference", "consumer", (String)id, (BeanDefinition)beanDefinition);
        }
        HashSet<String> props = new HashSet<String>();
        ManagedMap parameters = null;
        for (Method setter : beanClass.getMethods()) {
            String reference;
            int index;
            name = setter.getName();
            if (name.length() <= 3 || !name.startsWith("set") || !Modifier.isPublic(setter.getModifiers()) || setter.getParameterTypes().length != 1) continue;
            Class<?> type = setter.getParameterTypes()[0];
            String property = StringUtils.camelToSplitName(name.substring(3, 4).toLowerCase() + name.substring(4), "-");
            props.add(property);
            Method getter = null;
            try {
                getter = beanClass.getMethod("get" + name.substring(3), new Class[0]);
            }
            catch (NoSuchMethodException e) {
                try {
                    getter = beanClass.getMethod("is" + name.substring(3), new Class[0]);
                }
                catch (NoSuchMethodException noSuchMethodException) {
                    // empty catch block
                }
            }
            if (getter == null || !Modifier.isPublic(getter.getModifiers()) || !type.equals(getter.getReturnType())) continue;
            if ("parameters".equals(property)) {
                parameters = DubboBeanDefinitionParser.parseParameters(element.getChildNodes(), beanDefinition);
                continue;
            }
            if ("methods".equals(property)) {
                DubboBeanDefinitionParser.parseMethods((String)id, element.getChildNodes(), beanDefinition, parserContext);
                continue;
            }
            if ("arguments".equals(property)) {
                DubboBeanDefinitionParser.parseArguments((String)id, element.getChildNodes(), beanDefinition, parserContext);
                continue;
            }
            String value = element.getAttribute(property);
            if (value == null || (value = value.trim()).length() <= 0) continue;
            if ("registry".equals(property) && "N/A".equalsIgnoreCase(value)) {
                RegistryConfig registryConfig = new RegistryConfig();
                registryConfig.setAddress("N/A");
                beanDefinition.getPropertyValues().addPropertyValue(property, (Object)registryConfig);
                continue;
            }
            if ("registry".equals(property) && value.indexOf(44) != -1) {
                DubboBeanDefinitionParser.parseMultiRef("registries", value, beanDefinition, parserContext);
                continue;
            }
            if ("provider".equals(property) && value.indexOf(44) != -1) {
                DubboBeanDefinitionParser.parseMultiRef("providers", value, beanDefinition, parserContext);
                continue;
            }
            if ("protocol".equals(property) && value.indexOf(44) != -1) {
                DubboBeanDefinitionParser.parseMultiRef("protocols", value, beanDefinition, parserContext);
                continue;
            }
            if (DubboBeanDefinitionParser.isPrimitive(type)) {
                if ("async".equals(property) && "false".equals(value) || "timeout".equals(property) && "0".equals(value) || "delay".equals(property) && "0".equals(value) || "version".equals(property) && "0.0.0".equals(value) || "stat".equals(property) && "-1".equals(value) || "reliable".equals(property) && "false".equals(value)) {
                    value = null;
                }
                reference = value;
            } else if ("protocol".equals(property) && ExtensionLoader.getExtensionLoader(Protocol.class).hasExtension(value) && (!parserContext.getRegistry().containsBeanDefinition(value) || !ProtocolConfig.class.getName().equals(parserContext.getRegistry().getBeanDefinition(value).getBeanClassName()))) {
                if ("dubbo:provider".equals(element.getTagName())) {
                    logger.warn("Recommended replace <dubbo:provider protocol=\"" + value + "\" ... /> to <dubbo:protocol name=\"" + value + "\" ... />");
                }
                reference = new RuntimeBeanReference(value);
            } else if ("onreturn".equals(property)) {
                index = value.lastIndexOf(".");
                String returnRef = value.substring(0, index);
                String returnMethod = value.substring(index + 1);
                reference = new RuntimeBeanReference(returnRef);
                beanDefinition.getPropertyValues().addPropertyValue("onreturnMethod", (Object)returnMethod);
            } else if ("onthrow".equals(property)) {
                index = value.lastIndexOf(".");
                String throwRef = value.substring(0, index);
                String throwMethod = value.substring(index + 1);
                reference = new RuntimeBeanReference(throwRef);
                beanDefinition.getPropertyValues().addPropertyValue("onthrowMethod", (Object)throwMethod);
            } else if ("oninvoke".equals(property)) {
                index = value.lastIndexOf(".");
                String invokeRef = value.substring(0, index);
                String invokeRefMethod = value.substring(index + 1);
                reference = new RuntimeBeanReference(invokeRef);
                beanDefinition.getPropertyValues().addPropertyValue("oninvokeMethod", (Object)invokeRefMethod);
            } else {
                BeanDefinition refBean;
                if ("ref".equals(property) && parserContext.getRegistry().containsBeanDefinition(value) && !(refBean = parserContext.getRegistry().getBeanDefinition(value)).isSingleton()) {
                    throw new IllegalStateException("The exported service ref " + value + " must be singleton! Please set the " + value + " bean scope to singleton, eg: <bean id=\"" + value + "\" scope=\"singleton\" ...>");
                }
                reference = new RuntimeBeanReference(value);
            }
            beanDefinition.getPropertyValues().addPropertyValue(property, (Object)reference);
        }
        NamedNodeMap attributes = element.getAttributes();
        int len = attributes.getLength();
        for (int i = 0; i < len; ++i) {
            Node node = attributes.item(i);
            name = node.getLocalName();
            if (props.contains(name)) continue;
            if (parameters == null) {
                parameters = new ManagedMap();
            }
            String value = node.getNodeValue();
            parameters.put((Object)name, (Object)new TypedStringValue(value, String.class));
        }
        if (parameters != null) {
            beanDefinition.getPropertyValues().addPropertyValue("parameters", parameters);
        }
        return beanDefinition;
    }

    private static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == Boolean.class || cls == Byte.class || cls == Character.class || cls == Short.class || cls == Integer.class || cls == Long.class || cls == Float.class || cls == Double.class || cls == String.class || cls == Date.class || cls == Class.class;
    }

    private static void parseMultiRef(String property, String value, RootBeanDefinition beanDefinition, ParserContext parserContext) {
        String[] values = value.split("\\s*[,]+\\s*");
        ManagedList list = null;
        for (int i = 0; i < values.length; ++i) {
            String v = values[i];
            if (v == null || v.length() <= 0) continue;
            if (list == null) {
                list = new ManagedList();
            }
            list.add((Object)new RuntimeBeanReference(v));
        }
        beanDefinition.getPropertyValues().addPropertyValue(property, list);
    }

    private static void parseNested(Element element, ParserContext parserContext, Class<?> beanClass, boolean required, String tag, String property, String ref, BeanDefinition beanDefinition) {
        NodeList nodeList = element.getChildNodes();
        if (nodeList != null && nodeList.getLength() > 0) {
            boolean first = true;
            for (int i = 0; i < nodeList.getLength(); ++i) {
                BeanDefinition subDefinition;
                Node node = nodeList.item(i);
                if (!(node instanceof Element) || !tag.equals(node.getNodeName()) && !tag.equals(node.getLocalName())) continue;
                if (first) {
                    first = false;
                    String isDefault = element.getAttribute("default");
                    if (isDefault == null || isDefault.length() == 0) {
                        beanDefinition.getPropertyValues().addPropertyValue("default", (Object)"false");
                    }
                }
                if ((subDefinition = DubboBeanDefinitionParser.parse((Element)node, parserContext, beanClass, required)) == null || ref == null || ref.length() <= 0) continue;
                subDefinition.getPropertyValues().addPropertyValue(property, (Object)new RuntimeBeanReference(ref));
            }
        }
    }

    private static void parseProperties(NodeList nodeList, RootBeanDefinition beanDefinition) {
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); ++i) {
                String name;
                Node node = nodeList.item(i);
                if (!(node instanceof Element) || !"property".equals(node.getNodeName()) && !"property".equals(node.getLocalName()) || (name = ((Element)node).getAttribute("name")) == null || name.length() <= 0) continue;
                String value = ((Element)node).getAttribute("value");
                String ref = ((Element)node).getAttribute("ref");
                if (value != null && value.length() > 0) {
                    beanDefinition.getPropertyValues().addPropertyValue(name, (Object)value);
                    continue;
                }
                if (ref != null && ref.length() > 0) {
                    beanDefinition.getPropertyValues().addPropertyValue(name, (Object)new RuntimeBeanReference(ref));
                    continue;
                }
                throw new UnsupportedOperationException("Unsupported <property name=\"" + name + "\"> sub tag, Only supported <property name=\"" + name + "\" ref=\"...\" /> or <property name=\"" + name + "\" value=\"...\" />");
            }
        }
    }

    private static ManagedMap parseParameters(NodeList nodeList, RootBeanDefinition beanDefinition) {
        if (nodeList != null && nodeList.getLength() > 0) {
            ManagedMap parameters = null;
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                if (!(node instanceof Element) || !"parameter".equals(node.getNodeName()) && !"parameter".equals(node.getLocalName())) continue;
                if (parameters == null) {
                    parameters = new ManagedMap();
                }
                String key = ((Element)node).getAttribute("key");
                String value = ((Element)node).getAttribute("value");
                boolean hide = "true".equals(((Element)node).getAttribute("hide"));
                if (hide) {
                    key = "." + key;
                }
                parameters.put((Object)key, (Object)new TypedStringValue(value, String.class));
            }
            return parameters;
        }
        return null;
    }

    private static void parseMethods(String id, NodeList nodeList, RootBeanDefinition beanDefinition, ParserContext parserContext) {
        if (nodeList != null && nodeList.getLength() > 0) {
            ManagedList methods = null;
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                if (!(node instanceof Element)) continue;
                Element element = (Element)node;
                if (!"method".equals(node.getNodeName()) && !"method".equals(node.getLocalName())) continue;
                String methodName = element.getAttribute("name");
                if (methodName == null || methodName.length() == 0) {
                    throw new IllegalStateException("<dubbo:method> name attribute == null");
                }
                if (methods == null) {
                    methods = new ManagedList();
                }
                BeanDefinition methodBeanDefinition = DubboBeanDefinitionParser.parse((Element)node, parserContext, MethodConfig.class, false);
                String name = id + "." + methodName;
                BeanDefinitionHolder methodBeanDefinitionHolder = new BeanDefinitionHolder(methodBeanDefinition, name);
                methods.add((Object)methodBeanDefinitionHolder);
            }
            if (methods != null) {
                beanDefinition.getPropertyValues().addPropertyValue("methods", methods);
            }
        }
    }

    private static void parseArguments(String id, NodeList nodeList, RootBeanDefinition beanDefinition, ParserContext parserContext) {
        if (nodeList != null && nodeList.getLength() > 0) {
            ManagedList arguments = null;
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                if (!(node instanceof Element)) continue;
                Element element = (Element)node;
                if (!"argument".equals(node.getNodeName()) && !"argument".equals(node.getLocalName())) continue;
                String argumentIndex = element.getAttribute("index");
                if (arguments == null) {
                    arguments = new ManagedList();
                }
                BeanDefinition argumentBeanDefinition = DubboBeanDefinitionParser.parse((Element)node, parserContext, ArgumentConfig.class, false);
                String name = id + "." + argumentIndex;
                BeanDefinitionHolder argumentBeanDefinitionHolder = new BeanDefinitionHolder(argumentBeanDefinition, name);
                arguments.add((Object)argumentBeanDefinitionHolder);
            }
            if (arguments != null) {
                beanDefinition.getPropertyValues().addPropertyValue("arguments", arguments);
            }
        }
    }

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return DubboBeanDefinitionParser.parse(element, parserContext, this.beanClass, this.required);
    }
}

