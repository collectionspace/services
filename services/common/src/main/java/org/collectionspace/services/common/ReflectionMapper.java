/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common;

import org.collectionspace.services.common.api.Tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** User: laramie
 *  $LastChangedRevision:  $
 *  $LastChangedDate:  $
 */
public class ReflectionMapper {

    public static boolean isSetter(Method method) {
        if (!method.getName().startsWith("set")) return false;
        if (method.getParameterTypes().length != 1) return false;
        return true;
    }

    public static Object fireSetMethod(Method setMethod, Object target, Object arg) throws Exception {
        if (target != null && setMethod != null) {
            return setMethod.invoke(target, arg);
        }
        return null;
    }

    public static Object fireGetMethod(Method getMethod, Object target) throws Exception {
        if (target != null && getMethod != null) {
            Object[] arg = new Object[0];
            //System.out.println("\r\n~~~~~~~~ fireGetMethod("+getMethod+")");
            return getMethod.invoke(target, arg);
        }
        return null;
    }

    public static enum STATUS {OK, NO_SUCH_METHOD, EXCEPTION};
                                                                           
    public static STATUS callSetterUncaught(Object target, String name, Object arg) throws Exception {
        if (Tools.isEmpty(name)){
            return STATUS.NO_SUCH_METHOD;
        }
        if (target==null){
            return STATUS.EXCEPTION;
        }
        Class aClass = target.getClass();
        Method m;

        Class[] cls = new Class[1];
        cls[0] = String.class;
        try {
            m = aClass.getMethod(name, cls);
        } catch (NoSuchMethodException nsm){
            m = null;
        }
        if (m==null){
             m = aClass.getMethod(name, Object.class); //throws NoSuchMethodException if not found.
        }
        //todo: if m==null, call callSetterCaseInsensitive().
        fireSetMethod(m, target, arg);
        return STATUS.OK;
    }

    public static STATUS callSetter(Object target, String name, Object arg) {
        try {
            callSetterUncaught(target, name, arg);
        } catch (NoSuchMethodException nsm) {
            System.out.println("ERROR in ReflectionMapper.callSetter(target:"+target+", name:"+name+", arg:"+arg+"): "+nsm);
            return STATUS.NO_SUCH_METHOD;
        } catch (Exception e) {
            System.out.println("ERROR in ReflectionMapper.callSetter(target:"+target+", name:"+name+", arg:"+arg+"): "+e);
            return STATUS.EXCEPTION;
        }
        return STATUS.OK;
    }

    public static Object instantiate(String classname) throws Exception {
        if (classname == null) {
            throw new Exception("classname was null in ReflectionMapper.instantiate()");
        }
        classname = classname.trim();
        Class cl = Class.forName(classname);
        Class [] classParm = null;
        Object [] objectParm = null;
        Constructor co = cl.getConstructor(classParm);
        Object item = co.newInstance(objectParm);
        return item;
    }

    // EXPERIMENTAL:
    public static void callSetterCaseInsensitive(Class aClass, Object target, Object args){
        boolean showInheritedMethods = false;
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            Class dc = method.getDeclaringClass();
            boolean sameClass = aClass.equals(dc);
            if (showInheritedMethods || sameClass) {
                if (isSetter(method) && method.getParameterTypes().length==1) {
                    String id = method.getName();
                    Method setMethod = method;
                    String dataClass = method.getParameterTypes()[0].getName();

                    args = "MyNewValue";
                    //todo: fireSetMethod(setMethod, target, args);
                    System.out.println("setter: " + method + " in class: " + dc);
                }
            }
        }
    }
}
