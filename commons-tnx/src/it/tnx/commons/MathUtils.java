/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.tnx.commons;

/**
 *
 * @author mceccarelli
 */
public class MathUtils {

    public static int inRange(int val, int min , int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }
}
