/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

/**
 *
 * @author mceccarelli
 */
public class cu extends CastUtils {
    //abbreviazione per CastUtils
    public static Double d(Object val) {
        return toDouble(val);
    }
    public static Double d0(Object val) {
        return toDouble0(val);
    }
    public static Double d0e(Object val) {
        return toDouble0Eng(val);
    }
    public static Double d0a(Object val) {
        return toDouble0All(val);
    }
    public static String s(Object val) {
        return toString(val);
    }
    public static Integer i(Object val) {
        return toInteger(val);
    }
    public static Integer i0(Object val) {
        return toInteger0(val);
    }    
    
    public static Long l(Object val) {
        return toLong(val);
    }
    public static Long l0(Object val) {
        return toLong0(val);
    }        
    
    public static void main(String[] args) {
        System.out.println(s(1500));
    }
}
