/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author test1
 */
public class DebugFastUtils {

    static public void dump(Object obj) {
        System.out.println(dumpAsString(obj));
    }

    static public String dumpAsString(Object obj) {
        return dumpAsString(obj, 0);
    }

    static public String dumpAsString(Object obj, int level) {
        if (obj == null) {
            return "";
        }

        StringBuilder out = new StringBuilder();

        if (obj instanceof Object[]) {
            for (Object o : (Object[]) obj) {
                out.append(dumpAsString(o, level));
            }
        } else if (obj instanceof Collection) {
            for (Object o : (Collection) obj) {
                out.append(dumpAsString(o, level));
            }
        } else if (obj instanceof Vector) {
            for (Object o : (Vector) obj) {
                out.append(dumpAsString(o, level));
            }
        } else if (obj instanceof Map) {
//            out.append(dumpAsString(((Map) obj).entrySet().toArray(), level));
            out.append(dumpAsString(obj.toString(), level));
        } else if (obj instanceof Enumeration) {
            Enumeration e = (Enumeration) obj;
            while (e.hasMoreElements()) {
                out.append(dumpAsString(e.nextElement(), level));
            }
        } else if (obj instanceof ResultSet) {
            try {
                ResultSet r = (ResultSet) obj;
                ResultSetMetaData m = r.getMetaData();
                for (int c = 1; c <= m.getColumnCount(); c++) {
                    out.append(FormatUtils.fill(m.getColumnName(c), MathUtils.inRange(m.getColumnDisplaySize(c), 6, 30)));
                    out.append("|");
                }
                out.append("\n");
                if (r.getRow() == 0) {
                    while (r.next()) {
                        for (int c = 1; c <= m.getColumnCount(); c++) {
                            out.append(FormatUtils.fill(r.getString(c), MathUtils.inRange(m.getColumnDisplaySize(c), 6, 30)));
                            out.append("|");
                        }
                        out.append("\n");
                    }
                    r.beforeFirst();
                } else {
                    for (int c = 1; c <= m.getColumnCount(); c++) {
                        out.append(FormatUtils.fill(r.getString(c), MathUtils.inRange(m.getColumnDisplaySize(c), 6, 30)));
                        out.append("|");
                    }
                    out.append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (obj instanceof String) {
            out.append((String)obj);
            out.append("\n");
        } else {
            for (int i = 0; i < level; i++) {
                out.append("\t");
            }
            out.append(obj);
            out.append("\n");

//        Field[] fields = obj.getClass().getDeclaredFields();
//        for (Field f : fields) {
//            try {
//                if (f.get(obj) != null) {
//                    for (int i = 0; i < level; i++) {
//                        out.append("\t");
//                    }
//                    out.append("\t");
//                    out.append(f.getName());
//                    out.append("\t\t:");
//                    out.append(String.valueOf(f.get(obj)));
//                    out.append("\n");
//                }
//            } catch (IllegalArgumentException ex) {
//            } catch (IllegalAccessException ex) {
//            }
//        }

            Method[] methods = obj.getClass().getMethods();
            for (Method m : methods) {
                if ((m.getName().startsWith("get") || m.getName().startsWith("is")) && m.getParameterTypes().length == 0) {
                    if (m.getName().equalsIgnoreCase("getClass")) continue;
                    try {
                        Object ret = m.invoke(obj);
                        if (ret instanceof Object[]) {
                            for (int i = 0; i < level; i++) {
                                out.append("\t");
                            }
                            out.append("\t");
                            out.append(m.getName());
                            for (Object o : (Object[]) ret) {
                                out.append(dumpAsString(o, level + 1));
                            }
                        } else if (ret instanceof Collection) {
                            for (int i = 0; i < level; i++) {
                                out.append("\t");
                            }
                            out.append("\t");
                            out.append(m.getName());
                            for (Object o : (Collection) ret) {
                                out.append(dumpAsString(o, level + 1));
                            }
                        } else {
                            for (int i = 0; i < level; i++) {
                                out.append("\t");
                            }
                            out.append("\t");
                            out.append(m.getName());
                            out.append(":\t");
                            out.append(String.valueOf(ret));
                            out.append("\n");
                        }
                    } catch (IllegalAccessException ex) {
                    } catch (IllegalArgumentException ex) {
                    } catch (InvocationTargetException ex) {
                    } catch (Exception ex) {
                    }
                }
            }

        }

        return out.toString();
    }

    public static void trace(String startsWith, String message) {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        System.out.println("TRACE: " + message);
        for (StackTraceElement e : stacks) {
            if (e.getClassName().startsWith(startsWith)) {
                System.out.println("TRACE: \t\t" + e.toString());
            }
        }
    }

    static public void dumpMem() {
        System.out.println(getMem());
    }

    static public String getMem() {
        double f = ((double) Runtime.getRuntime().freeMemory()) / 1024d / 1024d;
        double t = ((double) Runtime.getRuntime().totalMemory()) / 1024d / 1024d;
        double m = ((double) Runtime.getRuntime().maxMemory()) / 1024d / 1024d;
        double u = t - f;
        String out = "dumpMem: max: " + FormatUtils.formatPerc(m) + " tot:" + FormatUtils.formatPerc(t) + " free:" + FormatUtils.formatPerc(f) + " in use:" + FormatUtils.formatPerc(u);
        return out;
    }

    static public long getSize(Object obj) {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        XMLEncoder xenc = null;
        try {
            //binary
            File f = File.createTempFile("java_debug_utils_", ".ser");
            System.err.println("getSize f = " + f);
            fos = new FileOutputStream(f);
            out = new ObjectOutputStream(fos);
            out.writeObject(obj);
            out.close();
            return f.length();

//            //xml
//            File fxml = File.createTempFile("java_debug_utils_getSize_", ".xml");
//            fos = new FileOutputStream(fxml);
//            xenc = new XMLEncoder(fos);
//            xenc.writeObject(obj);
//            xenc.close();
//            return fxml.length();
        } catch (Exception ex) {
            System.err.println("ex = " + ex);
        }
        return 0;
    }

    public static void hexDump(String todump) {
        System.out.println("todump:" + todump + " stringToHex:" + stringToHex(todump));
    }

    public static void hexDump(byte[] todump) {
        System.out.println("todump:" + todump + " stringToHex:" + bytesToHex(todump));
    }

    public static String stringToHex(String todump) {
        int BYTES_PER_LINE = 16;
        int i;
        String out = "";
        for (i = 0; i < todump.length(); i++) {
            if (BYTES_PER_LINE == 0) continue;
            if (i == 0) out += "";
            if (i % BYTES_PER_LINE == 0) out += "\n" + StringUtils.replace(todump.substring(i, (todump.length() < (i + BYTES_PER_LINE) ? todump.length() : i + BYTES_PER_LINE)), "\n", "*") + " > ";
            else out += " ";
            char c = todump.charAt(i);
            out += String.format("%02x", c & 0xff).toUpperCase();
        }
        return out;
    }

    public static String bytesToHex(byte[] todump) {
        int BYTES_PER_LINE = 16;
        int i;
        String out = "";
        for (i = 0; i < todump.length; i++) {
            if (BYTES_PER_LINE == 0) continue;
            if (i == 0) out += "";
//            if (i % BYTES_PER_LINE == 0) out += "\n" + StringUtils.replace(todump.substring(i, (todump.length < (i + BYTES_PER_LINE) ? todump.length : i + BYTES_PER_LINE)), "\n", "*") + " > ";
            else out += " ";
            char c = (char)todump[i];
            out += String.format("%02x", c & 0xff).toUpperCase();
        }
        return out;
    }

    public static void main(String[] args) {
        HashMap test = new HashMap();
        test.put("voce1", "asasasa");
        test.put("voce2", 323232);
        test.put("voce3", 32332);
        DebugFastUtils.dump(test);
    }
}
