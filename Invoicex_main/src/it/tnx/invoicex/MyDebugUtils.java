/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex;

import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.JUtil;
import it.tnx.commons.MathUtils;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.JTable;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;

/**
 *
 * @author test1
 */
public class MyDebugUtils {

    static public void dump(Object obj) {
        System.out.println(dumpAsString(obj));
    }

    static public void dump(Object obj, boolean deep) {
        //se deep = false si ferma al primo livello
        System.out.println(dumpAsString(obj, deep));
    }

    static public String dumpAsString(Object obj) {
        return dumpAsString(obj, true, 0);
    }

    static public String dumpAsString(Object obj, boolean deep) {
        return dumpAsString(obj, deep, 0);
    }

    static public String dumpAsString(Object obj, boolean deep, int level) {
        if (obj == null) {
            return "";
        }

        if (deep == false && level > 1) {
            return "";
        }

        StringBuffer out = new StringBuffer();

        if (obj instanceof Object[]) {
            if (level == 0) {
                out.append("[Array:");
                out.append(((Object[]) obj).length);
                out.append("]\n");
            }
            for (Object o : (Object[]) obj) {
                out.append(dumpAsString(o, deep, level + 1));
            }
        } else if (obj instanceof Collection) {
            if (level == 0) {
                out.append("[Collection:");
                out.append(((Collection) obj).size());
                out.append("]\n");
            }
            for (Object o : (Collection) obj) {
                out.append(dumpAsString(o, deep, level + 1));
            }
        } else if (obj instanceof Map || obj instanceof JSONObject) {
            if (level == 0) {
                out.append("[Map:");
                out.append(((Map) obj).size());
                out.append("]\n");
            }
            for (Object o : ((Map) obj).entrySet()) {
                String tmp = CastUtils.toString(((Map.Entry) o).getKey()) + " = ";
                if (((Map.Entry) o).getValue() instanceof Object[]) {
                    for (Object o2 : (Object[]) ((Map.Entry) o).getValue()) {
                        tmp += CastUtils.toString(o2) + ",";
                    }
                    tmp = StringUtils.chop(tmp);
                } else {
                    if (((Map.Entry) o).getValue() instanceof JSONObject) {
                        tmp += "\n" + dumpAsString(((Map.Entry) o).getValue(), deep, level + 1);
                    } else {
                        tmp += CastUtils.toString(((Map.Entry) o).getValue()) + ",";
                    }
                }
                out.append(dumpAsString(tmp, deep, level + 1));
            }
        } else if (obj instanceof java.util.Map.Entry) {
            out.append(dumpAsString(CastUtils.toString(((Map.Entry) obj).getKey()) + " = " + CastUtils.toString(((Map.Entry) obj).getValue()), deep, level + 1));
        } else if (obj instanceof Enumeration) {
            if (level == 0) {
                out.append("[Enumeration:");
                out.append("]\n");
            }
            Enumeration e = (Enumeration) obj;
            while (e.hasMoreElements()) {
                out.append(dumpAsString(e.nextElement(), deep, level));
            }
        } else if (obj instanceof ResultSet) {
            try {
                ResultSet r = (ResultSet) obj;
                out.append("[ResultSet " + r.toString() + ":");
                out.append("]\n");

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
            for (int i = 0; i < level; i++) {
                out.append("\t");
            }
            out.append((String) obj);
            out.append("\n");
        } else if (obj instanceof Date) {
            for (int i = 0; i < level; i++) {
                out.append("\t");
            }
            out.append(DateUtils.formatDate((Date) obj));
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
                    try {
                        Object ret = m.invoke(obj);

                        if (ret instanceof Object[]) {
                            for (int i = 0; i < level; i++) {
                                out.append("\t");
                            }
                            out.append("\t");
                            out.append(m.getName());
                            out.append(" [Array:");
                            out.append(((Object[]) ret).length);
                            out.append("]\n");
                            for (Object o : (Object[]) ret) {
                                out.append(dumpAsString(o, deep, level + 1));
                            }
                        } else if (ret instanceof Collection) {
                            for (int i = 0; i < level; i++) {
                                out.append("\t");
                            }
                            out.append("\t");
                            out.append(m.getName());
                            out.append(" [Collection:");
                            out.append(((Collection) ret).size());
                            out.append("]\n");
                            for (Object o : (Collection) ret) {
                                out.append(dumpAsString(o, deep, level + 1));
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

    static public void dumpJTable(JTable table, Integer row) {
        System.out.println(dumpJTableAsString(table, row));
    }

    static public String dumpJTableAsString(JTable table, Integer row) {
        StringBuffer out = new StringBuffer();
        int col = 0;
        for (col = 0; col < table.getColumnCount(); col++) {
            out.append(FormatUtils.fill(table.getColumnName(col), 20));
            out.append("|");
        }
        out.append("\n");
        for (col = 0; col < table.getColumnCount(); col++) {
            out.append(FormatUtils.fill(CastUtils.toString(table.getValueAt(row, col)), 20));
            out.append("|");
        }
        out.append("\n");
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

    public static String stringToHex(String todump) {
        int BYTES_PER_LINE = 16;
        int i;
        String out = "";
        for (i = 0; i < todump.length(); i++) {
            if (BYTES_PER_LINE == 0) {
                continue;
            }
            if (i == 0) {
                out += "";
            }
            if (i % BYTES_PER_LINE == 0) {
                out += "\n" + StringUtils.replace(todump.substring(i, (todump.length() < (i + BYTES_PER_LINE) ? todump.length() : i + BYTES_PER_LINE)), "\n", "*") + " > ";
            } else {
                out += " ";
            }
            char c = todump.charAt(i);
            out += String.format("%02x", c & 0xff).toUpperCase();
        }
        return out;
    }

    public static Map mapDiff(Map m1, Map m2) {
        Map mdiff = new TreeMap();
        int conta = 1;

        Iterator iter1 = (new TreeSet(m1.keySet())).iterator();
        while (iter1.hasNext()) {
            try {
                Object k1 = iter1.next();
                if (!m2.containsKey(k1)) {
                    mdiff.put(FormatUtils.zeroFill(conta, 3) + " - in m2 non c'�", k1.toString() + ":" + m1.get(k1));
                    conta++;
                } else if (!JUtil.equalsToString(m1.get(k1), m2.get(k1))) {
                    mdiff.put(FormatUtils.zeroFill(conta, 3) + " - in m2 diverso", k1.toString() + ":" + m1.get(k1) + " -> " + m2.get(k1));
                    conta++;
                }
            } catch (Exception e) {
            }
        }

        Iterator iter2 = m2.keySet().iterator();
        while (iter2.hasNext()) {
            Object k2 = iter2.next();
            if (!m1.containsKey(k2)) {
                mdiff.put(FormatUtils.zeroFill(conta, 3) + " - in m1 non c'�", k2.toString() + ":" + m2.get(k2));
                conta++;
            }
        }

        return mdiff;
    }

    public static void main(String[] args) {
        HashMap test = new HashMap();
        test.put("voce1", "asasasa");
        test.put("voce2", 323232);
        test.put("voce3", 32332);

        HashMap test2 = new HashMap();
        test2.put("voce2", 323232);
        test2.put("voce3", 323333333);
        test2.put("voce4", "aaa");

        MyDebugUtils.dump(test);
        System.out.println(mapDiff(test, test2));
    }

    public static void dumpaclip(Object var) {
        System.out.println("*** inizio dumpaclip ***");
        dump(var);
        StringSelection stringSelection = new StringSelection(String.valueOf(var));
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        System.out.println("*** fine dumpaclip ***");
    }

    static public String stackTraceToStringFull(Throwable t) {
        return stackTraceToStringFull(t, 0);
    }

    static public String stackTraceToStringFull(Throwable t, Integer livello) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] traces = t.getStackTrace();
        sb.append(StringUtils.repeat("\t", livello));
        sb.append(t.toString());
        sb.append("\n\n");
        for (StackTraceElement trace : traces) {
            sb.append(StringUtils.repeat("\t", livello));
            sb.append(trace);
            sb.append("\n");
        }
        if (t.getCause() != null) {
            sb.append(StringUtils.repeat("\t", livello));
            sb.append(" *** causato da " + t.getCause() + " ***");
            sb.append(stackTraceToStringFull(t.getCause(), livello + 1));
        }
        return sb.toString();
    }
}
