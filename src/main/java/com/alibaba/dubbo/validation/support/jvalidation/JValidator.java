/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javassist.ClassPool
 *  javassist.CtClass
 *  javassist.CtConstructor
 *  javassist.CtField
 *  javassist.CtNewConstructor
 *  javassist.Modifier
 *  javassist.NotFoundException
 *  javassist.bytecode.AnnotationsAttribute
 *  javassist.bytecode.AttributeInfo
 *  javassist.bytecode.ClassFile
 *  javassist.bytecode.ConstPool
 *  javassist.bytecode.FieldInfo
 *  javassist.bytecode.annotation.Annotation
 *  javassist.bytecode.annotation.ArrayMemberValue
 *  javassist.bytecode.annotation.BooleanMemberValue
 *  javassist.bytecode.annotation.ByteMemberValue
 *  javassist.bytecode.annotation.CharMemberValue
 *  javassist.bytecode.annotation.ClassMemberValue
 *  javassist.bytecode.annotation.DoubleMemberValue
 *  javassist.bytecode.annotation.EnumMemberValue
 *  javassist.bytecode.annotation.FloatMemberValue
 *  javassist.bytecode.annotation.IntegerMemberValue
 *  javassist.bytecode.annotation.LongMemberValue
 *  javassist.bytecode.annotation.MemberValue
 *  javassist.bytecode.annotation.ShortMemberValue
 *  javassist.bytecode.annotation.StringMemberValue
 *  javax.validation.Configuration
 *  javax.validation.Constraint
 *  javax.validation.ConstraintViolation
 *  javax.validation.ConstraintViolationException
 *  javax.validation.Validation
 *  javax.validation.Validator
 *  javax.validation.ValidatorFactory
 *  javax.validation.groups.Default
 */
package com.alibaba.dubbo.validation.support.jvalidation;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.bytecode.ClassGenerator;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.validation.MethodValidated;
import com.alibaba.dubbo.validation.Validator;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import javax.validation.Configuration;
import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

public class JValidator
implements Validator {
    private static final Logger logger = LoggerFactory.getLogger(JValidator.class);
    private final Class<?> clazz;
    private final javax.validation.Validator validator;

    public JValidator(URL url) {
        this.clazz = ReflectUtils.forName(url.getServiceInterface());
        String jvalidation = url.getParameter("jvalidation");
        ValidatorFactory factory = jvalidation != null && jvalidation.length() > 0 ? Validation.byProvider(ReflectUtils.forName(jvalidation)).configure().buildValidatorFactory() : Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    private static boolean isPrimitives(Class<?> cls) {
        if (cls.isArray()) {
            return JValidator.isPrimitive(cls.getComponentType());
        }
        return JValidator.isPrimitive(cls);
    }

    private static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class || cls == Character.class || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }

    private static Object getMethodParameterBean(Class<?> clazz, Method method, Object[] args) {
        if (!JValidator.hasConstraintParameter(method)) {
            return null;
        }
        try {
            Class parameterClass;
            String parameterClassName = JValidator.generateMethodParameterClassName(clazz, method);
            try {
                parameterClass = Class.forName(parameterClassName, true, clazz.getClassLoader());
            }
            catch (ClassNotFoundException e) {
                ClassPool pool = ClassGenerator.getClassPool(clazz.getClassLoader());
                CtClass ctClass = pool.makeClass(parameterClassName);
                ClassFile classFile = ctClass.getClassFile();
                classFile.setVersionToJava5();
                ctClass.addConstructor(CtNewConstructor.defaultConstructor((CtClass)pool.getCtClass(parameterClassName)));
                Class<?>[] parameterTypes = method.getParameterTypes();
                java.lang.annotation.Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                for (int i = 0; i < parameterTypes.length; ++i) {
                    Class<?> type = parameterTypes[i];
                    java.lang.annotation.Annotation[] annotations = parameterAnnotations[i];
                    AnnotationsAttribute attribute = new AnnotationsAttribute(classFile.getConstPool(), "RuntimeVisibleAnnotations");
                    for (java.lang.annotation.Annotation annotation : annotations) {
                        Method[] members;
                        if (!annotation.annotationType().isAnnotationPresent(Constraint.class)) continue;
                        Annotation ja = new Annotation(classFile.getConstPool(), pool.getCtClass(annotation.annotationType().getName()));
                        for (Method member : members = annotation.annotationType().getMethods()) {
                            Object value;
                            if (!Modifier.isPublic((int)member.getModifiers()) || member.getParameterTypes().length != 0 || member.getDeclaringClass() != annotation.annotationType() || null == (value = member.invoke(annotation, new Object[0]))) continue;
                            MemberValue memberValue = JValidator.createMemberValue(classFile.getConstPool(), pool.get(member.getReturnType().getName()), value);
                            ja.addMemberValue(member.getName(), memberValue);
                        }
                        attribute.addAnnotation(ja);
                    }
                    String fieldName = method.getName() + "Argument" + i;
                    CtField ctField = CtField.make((String)("public " + type.getCanonicalName() + " " + fieldName + ";"), (CtClass)pool.getCtClass(parameterClassName));
                    ctField.getFieldInfo().addAttribute((AttributeInfo)attribute);
                    ctClass.addField(ctField);
                }
                parameterClass = ctClass.toClass(clazz.getClassLoader(), null);
            }
            Object parameterBean = parameterClass.newInstance();
            for (int i = 0; i < args.length; ++i) {
                Field field = parameterClass.getField(method.getName() + "Argument" + i);
                field.set(parameterBean, args[i]);
            }
            return parameterBean;
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            return null;
        }
    }

    private static String generateMethodParameterClassName(Class<?> clazz, Method method) {
        Class<?>[] parameterTypes;
        StringBuilder builder = new StringBuilder().append(clazz.getName()).append("_").append(JValidator.toUpperMethoName(method.getName())).append("Parameter");
        for (Class<?> parameterType : parameterTypes = method.getParameterTypes()) {
            builder.append("_").append(parameterType.getName());
        }
        return builder.toString();
    }

    private static boolean hasConstraintParameter(Method method) {
        java.lang.annotation.Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (parameterAnnotations != null && parameterAnnotations.length > 0) {
            java.lang.annotation.Annotation[][] arrannotation = parameterAnnotations;
            int n = arrannotation.length;
            for (int i = 0; i < n; ++i) {
                java.lang.annotation.Annotation[] annotations;
                for (java.lang.annotation.Annotation annotation : annotations = arrannotation[i]) {
                    if (!annotation.annotationType().isAnnotationPresent(Constraint.class)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private static String toUpperMethoName(String methodName) {
        return methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
    }

    private static MemberValue createMemberValue(ConstPool cp, CtClass type, Object value) throws NotFoundException {
        MemberValue memberValue = Annotation.createMemberValue((ConstPool)cp, (CtClass)type);
        if (memberValue instanceof BooleanMemberValue) {
            ((BooleanMemberValue)memberValue).setValue(((Boolean)value).booleanValue());
        } else if (memberValue instanceof ByteMemberValue) {
            ((ByteMemberValue)memberValue).setValue(((Byte)value).byteValue());
        } else if (memberValue instanceof CharMemberValue) {
            ((CharMemberValue)memberValue).setValue(((Character)value).charValue());
        } else if (memberValue instanceof ShortMemberValue) {
            ((ShortMemberValue)memberValue).setValue(((Short)value).shortValue());
        } else if (memberValue instanceof IntegerMemberValue) {
            ((IntegerMemberValue)memberValue).setValue(((Integer)value).intValue());
        } else if (memberValue instanceof LongMemberValue) {
            ((LongMemberValue)memberValue).setValue(((Long)value).longValue());
        } else if (memberValue instanceof FloatMemberValue) {
            ((FloatMemberValue)memberValue).setValue(((Float)value).floatValue());
        } else if (memberValue instanceof DoubleMemberValue) {
            ((DoubleMemberValue)memberValue).setValue(((Double)value).doubleValue());
        } else if (memberValue instanceof ClassMemberValue) {
            ((ClassMemberValue)memberValue).setValue(((Class)value).getName());
        } else if (memberValue instanceof StringMemberValue) {
            ((StringMemberValue)memberValue).setValue((String)value);
        } else if (memberValue instanceof EnumMemberValue) {
            ((EnumMemberValue)memberValue).setValue(((Enum)value).name());
        } else if (memberValue instanceof ArrayMemberValue) {
            CtClass arrayType = type.getComponentType();
            int len = Array.getLength(value);
            MemberValue[] members = new MemberValue[len];
            for (int i = 0; i < len; ++i) {
                members[i] = JValidator.createMemberValue(cp, arrayType, Array.get(value, i));
            }
            ((ArrayMemberValue)memberValue).setValue(members);
        }
        return memberValue;
    }

    @Override
    public void validate(String methodName, Class<?>[] parameterTypes, Object[] arguments) throws Exception {
        ArrayList groups = new ArrayList();
        String methodClassName = this.clazz.getName() + "$" + JValidator.toUpperMethoName(methodName);
        Class<?> methodClass = null;
        try {
            methodClass = Class.forName(methodClassName, false, Thread.currentThread().getContextClassLoader());
            groups.add(methodClass);
        }
        catch (ClassNotFoundException classNotFoundException) {
            // empty catch block
        }
        HashSet violations = new HashSet();
        Method method = this.clazz.getMethod(methodName, parameterTypes);
        Class<?>[] methodClasses = null;
        if (method.isAnnotationPresent(MethodValidated.class)) {
            methodClasses = method.getAnnotation(MethodValidated.class).value();
            groups.addAll(Arrays.asList(methodClasses));
        }
        groups.add(0, Default.class);
        groups.add(1, this.clazz);
        Class[] classgroups = groups.toArray(new Class[0]);
        Object parameterBean = JValidator.getMethodParameterBean(this.clazz, method, arguments);
        if (parameterBean != null) {
            violations.addAll(this.validator.validate(parameterBean, classgroups));
        }
        for (Object arg : arguments) {
            this.validate(violations, arg, classgroups);
        }
        if (!violations.isEmpty()) {
            logger.error("Failed to validate service: " + this.clazz.getName() + ", method: " + methodName + ", cause: " + violations);
            throw new ConstraintViolationException("Failed to validate service: " + this.clazz.getName() + ", method: " + methodName + ", cause: " + violations, violations);
        }
    }

    private /* varargs */ void validate(Set<ConstraintViolation<?>> violations, Object arg, Class<?> ... groups) {
        if (arg != null && !JValidator.isPrimitives(arg.getClass())) {
            if (Object[].class.isInstance(arg)) {
                for (Object item : (Object[])arg) {
                    this.validate(violations, item, groups);
                }
            } else if (Collection.class.isInstance(arg)) {
                for (Object item : (Collection)arg) {
                    this.validate(violations, item, groups);
                }
            } else if (Map.class.isInstance(arg)) {
                for (Map.Entry entry : ((Map)arg).entrySet()) {
                    this.validate(violations, entry.getKey(), groups);
                    this.validate(violations, entry.getValue(), groups);
                }
            } else {
                violations.addAll(this.validator.validate(arg, (Class[])groups));
            }
        }
    }
}

