/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.tnx.commons;

import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author mceccarelli
 */
public class MapUtils {
    static public boolean equals(Map map1, Map map2) {
        Iterator i1 = map1.keySet().iterator();
        while (i1.hasNext()) {
            Object k1 = i1.next();
            if (!map2.containsKey(k1)) return false;
            if (!map2.get(k1).equals(map1.get(k1))) return false;
        }

        Iterator i2 = map2.keySet().iterator();
        while (i2.hasNext()) {
            Object k2 = i2.next();
            if (!map1.containsKey(k2)) return false;
        }

        return true;
    }

    static public boolean equals2(Map map1, Map map2) {
        Iterator i1 = map1.keySet().iterator();
        while (i1.hasNext()) {
            Object k1 = i1.next();
            if (!map2.containsKey(k1)) continue;
            if (!String.valueOf(map2.get(k1)).equalsIgnoreCase(String.valueOf(map1.get(k1)))) {
                System.out.println("equals2: diverse: " + k1 + " map2:" + map2.get(k1) +  " map1:" + map1.get(k1));
                return false;
            }
        }

//        Iterator i2 = map2.keySet().iterator();
//        while (i2.hasNext()) {
//            Object k2 = i2.next();
//            if (!map1.containsKey(k2)) return false;
//        }

        return true;
    }
}
