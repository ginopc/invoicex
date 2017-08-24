/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.awt.Component;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JWindow;

/**
 *
 * @author mceccarelli
 */
public class ReflectUtils {

    public static Object runMethod(String class_name, String method, Object[] args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class _class = Class.forName(class_name);
        Method[] methods = _class.getMethods();
        Method mtorun = null;
        for (Method m : methods) {
            if (m.getName().equals(method)) {
                if (args == null) {
                    if (m.getParameterTypes() == null || m.getParameterTypes().length == 0) {
                        mtorun = m;
                        break;
                    }
                } else {
                    Class[] param_classes = m.getParameterTypes();
                    if (param_classes.length == args.length) {
                        boolean ok = true;
//                        for (int i = 0; i < args.length; i++) {
//                            if (!param_classes[i].isInstance(args[i])) {
//                                ok = false;
//                                break;
//                            }
//                        }
                        if (ok) {
                            mtorun = m;
                            break;
                        }
                    }
                }
            }
        }
        if (mtorun != null) {
            return mtorun.invoke(_class, args);
        } else {
            System.err.println("ReflectUtils.runMethod non trovato metodo " + method + " nella classe " + class_name);
        }
        return null;
    }

    public static Object getProp(String prop, Component c) {
        try {
            return c.getClass().getField(prop).get(c);
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
        
    }
}
