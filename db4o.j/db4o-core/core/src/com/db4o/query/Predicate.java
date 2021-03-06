/* Copyright (C) 2004 - 2005  Versant Inc.  http://www.db4o.com */
package com.db4o.query;

import com.db4o.internal.Platform4;
import com.db4o.nativequery.optimization.Db4oOnTheFlyEnhancer;
import com.db4o.reflect.jdk.SerializedLambda;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Base class for native queries. See
 * {@link com.db4o.ObjectContainer#query(Predicate)}
 * <br><br>
 *
 * @see com.db4o.ObjectContainer#query(Predicate)
 */
@SuppressWarnings("serial")
public abstract class Predicate<ExtentType> implements Serializable {

    public IPredicate<ExtentType> ExtentInterface = null;
    public ArrayList ExtentArgs;
    /**
     * public for implementation reasons, please ignore.
     */
    public final static String PREDICATEMETHOD_NAME = "match";

    private Class<? extends ExtentType> _extentType;

    private transient Method cachedFilterMethod = null;

    public Predicate() {
        this(null);
    }

    public Predicate(Class<? extends ExtentType> extentType) {
        _extentType = extentType;
    }

    public static Class getMatchType(Class t) {
        if (t == null) {
            return null;
        }
        Method[] methods = t.getDeclaredMethods();
        for (int methodIdx = 0; methodIdx < methods.length; methodIdx++) {
            Method method = methods[methodIdx];
            if ((!method.getName().equals(PredicatePlatform.PREDICATEMETHOD_NAME)) || method.getParameterTypes().length != 1) {
                continue;
            }
            Class r = method.getParameterTypes()[0];
            if (r == Object.class) {
                return null;
            } else {
                return r;
            }
        }
        return null;
    }

    public Method getFilterMethod() {
        if (cachedFilterMethod != null) {
            return cachedFilterMethod;
        }
        Method[] methods = getClass().getDeclaredMethods(); //.getMethods();
        for (int methodIdx = 0; methodIdx < methods.length; methodIdx++) {
            Method method = methods[methodIdx];
            if ((!method.getName().equals(PredicatePlatform.PREDICATEMETHOD_NAME)) || method.getParameterTypes().length != 1) {
                continue;
            }
            cachedFilterMethod = method;
            String targetName = method.getParameterTypes()[0].getName();
            if (!"java.lang.Object".equals(targetName)) {
                break;
            }
        }
        if (cachedFilterMethod == null) {
            throw new IllegalArgumentException("Invalid predicate.");
        }
        return cachedFilterMethod;
    }

    /**
     * public for implementation reasons, please ignore.
     */
    public Class<? extends ExtentType> extentType() {

        if (ExtentInterface != null) {
            if (_extentType == null) {
                Object me = SerializedLambda.Instance.getMe(ExtentInterface);
                if (me != null) {
                    _extentType = SerializedLambda.Instance.getMeClass(me);
                    ExtentArgs = SerializedLambda.Instance.getMeArgs(me);
                }
            }
            if (_extentType == null) {
                _extentType = getMatchType(ExtentInterface.getClass());
            }
        }
        if (_extentType == null) {
            _extentType = figureOutExtentType();
        }

        return _extentType;
    }

    /**
     * The match method that needs to be implemented by the user.
     *
     * @param candidate the candidate object passed from db4o
     * @return true to include an object in the resulting ObjectSet
     *
     * @sharpen.ignore
     */
    @decaf.Ignore(platforms = {decaf.Platform.JDK11, decaf.Platform.JDK12})
    public abstract boolean match(ExtentType candidate);

    /**
     * @sharpen.remove FilterParameterType()
     */
    @decaf.ReplaceFirst(value = "return filterParameterType();", platforms = {decaf.Platform.JDK11, decaf.Platform.JDK12})
    private Class<? extends ExtentType> figureOutExtentType() {
        return extentTypeFromGenericParameter();
    }

    /**
     * @sharpen.ignore
     */
    @decaf.Ignore(platforms = {decaf.Platform.JDK11, decaf.Platform.JDK12})
    private Class<? extends ExtentType> extentTypeFromGenericParameter() {
        Class<? extends ExtentType> extentType = filterParameterType();
        try {
            Type genericType = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            if ((genericType instanceof Class) && (extentType.isAssignableFrom((Class) genericType))) {
                extentType = (Class<? extends ExtentType>) genericType;
            }
        } catch (RuntimeException e) {
        }
        return extentType;
    }

    private Class<? extends ExtentType> filterParameterType() {
        return (Class<? extends ExtentType>) getFilterMethod().getParameterTypes()[0];
    }

    /**
     * public for implementation reasons, please ignore.
     */
    public boolean appliesTo(ExtentType candidate) {
        try {
            return match(candidate);
            /*
			Method filterMethod=getFilterMethod();
			Platform4.setAccessible(filterMethod);
			Object ret=filterMethod.invoke(this,new Object[]{candidate});
			return ((Boolean)ret).booleanValue();
             */
        } catch (Throwable e) {
            // TODO: log this exception somewhere?
//			e.printStackTrace();
            return false;
        }
    }
}
