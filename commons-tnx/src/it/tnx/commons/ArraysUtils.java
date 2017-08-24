/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.tnx.commons;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * @author test2
 */
public class ArraysUtils {

    static public String toString(Collection list, String separator) {
        if (list == null) return "";
        if (separator == null) separator = "|";
        if (list.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Object o : list) {
                sb.append(o);
                sb.append(separator);
            }
            sb.delete(sb.length() - separator.length(), sb.length());
            return sb.toString();
        } else return "";
    }
    
    static public String toString(Object[] array, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Object o : array) {
            sb.append(o);
            sb.append(separator);
        }
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }

    public static ArrayList toArrayList(ResultSet tables1, String col) {
        ArrayList ret = new ArrayList();
        try {
            while (tables1.next()) {
                ret.add(tables1.getObject(col));
            }
            tables1.beforeFirst();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public static boolean containString(String str, List l) {
        for (Object o : l) {
            if (o instanceof String) {
                String os = (String)o;
                if (os.equalsIgnoreCase(str)) return true;
            }
        }
        return false;
    }
}
