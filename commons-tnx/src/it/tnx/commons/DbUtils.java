/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author test1
 */
public class DbUtils {

    static public boolean debug = false;
//    static public boolean debug = true;
    static public boolean bench = false;

//attenzione, non abiltiare in produzione perchè causa ritenzione di memoria (vedi problemi con heap space in allegati
//ATTENZIONE, MAI MAI MAI ABILITARE IN PRODUZIONE !!!
    static public boolean conta = false;

    static public boolean cache = false;
    static public Callable resetConn = null;

    static public final String DIFF_TYPE_INSERT = "insert";
    static public final String DIFF_TYPE_UPDATE = "update";
    static public final String DIFF_TYPE_DELETE = "delete";

    static public Map<String, Object> mapcache = new HashMap() {
    };

    static public Map<String, Integer> mapconta = new HashMap() {
    };
    
    /* eventuale cache di google guava
     Cache<String, Graph> graphs = CacheBuilder.newBuilder()
     .concurrencyLevel(4)
     .weakKeys()
     .maximumSize(10000)
     .expireAfterWrite(10, TimeUnit.MINUTES)
     .build(
     new CacheLoader<Key, Graph>() {
     public Graph load(Key key) throws AnyException {
     return createExpensiveGraph(key);
     }
     });    
     */
    static public List tryJpaQuery(Object query) throws Exception {
        int max = 5;
        int tries = 0;
        Object ret = null;
        do {
            tries++;
            try {
                ret = query.getClass().getMethod("getResultList").invoke(query);
                tries = max + 1;
            } catch (Exception ex) {
                try {
                    if (ex.getCause().getCause() instanceof SQLException) {
                        SQLException sqlex = (SQLException) ex.getCause().getCause();
                        String sqlState = sqlex.getSQLState();
                        if (!("08S01".equals(sqlState)) && !("40001".equals(sqlState))) {
                            tries = max + 1;
                        }
                    } else {
                        throw ex;
                    }
                } catch (Exception ex0) {
                    throw ex0;
                }
            }
        } while (tries < max);
        return (List) ret;
    }

    static public ResultSet tryQuery(Statement stat, String sql) throws Exception {
        conteggio(sql);

        int max = 5;
        int tries = 0;
        ResultSet ret = null;
        do {
            tries++;
            try {
                ret = stat.executeQuery(sql);
                tries = max + 1;
            } catch (Exception ex) {
                try {
                    if (ex.getCause() instanceof SQLException) {
                        SQLException sqlex = (SQLException) ex.getCause();
                        String sqlState = sqlex.getSQLState();
                        if (!("08S01".equals(sqlState)) && !("40001".equals(sqlState))) {
                            tries = max + 1;
                        }
                    } else {
                        throw ex;
                    }
                } catch (Exception ex0) {
                    throw ex0;
                }
            }
        } while (tries < max);
        return ret;
    }

    static public boolean tryExecQuery(Connection conn, String sql) throws Exception {
        return tryExecQuery(conn, sql, true, false);
    }

    static public boolean tryExecQuery(Connection conn, String sql, boolean retry) throws Exception {
        return tryExecQuery(conn, sql, true, false);
    }

    static public boolean tryExecQuery(Connection conn, String sql, boolean retry, boolean ignoresync) throws Exception {
        conteggio(sql);

        //controllo se arriva da dbchanges non considerare sync
        if (!ignoresync) {
            try {
                Object retissync = ReflectUtils.runMethod("it.tnx.invoicex.sync.Sync", "isQueryToSync", new Object[]{sql});
                if ((Boolean) retissync) {
                    Object retexec = ReflectUtils.runMethod("it.tnx.Db", "executeSqlDialogExcSync", new Object[]{conn, sql, false, true});
                    return (Boolean) retexec;
                }
            } catch (ClassNotFoundException cex) {
                if (!cex.getMessage().endsWith(".Sync")) {
                    cex.printStackTrace();
                }
            } catch (IllegalAccessException cex) {
                System.out.println("");
            } catch (IllegalArgumentException cex) {
                System.out.println("");
            } catch (InvocationTargetException cex) {
                throw (Exception) cex.getTargetException();
            } catch (Exception ex) {
                throw ex;
            }
        }

        int max = 5;
        int tries = 0;
        if (!retry) {
            tries = max;
        }

        Statement stat = null;
        boolean ret = false;
        MicroBench mb = null;
        if (bench) {
            mb = new MicroBench(true);
        }
        do {
            tries++;
            try {
                stat = conn.createStatement();

                //ret = stat.execute(sql);
                stat.execute(sql);
                ret = true;

                //debug
                if (debug) {
                    StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
                    System.out.println("---START SQL TRACE---");
                    System.out.println("sql: " + sql);
                    for (StackTraceElement e : stacks) {
                        if (e.getClassName().startsWith("gestioneFatture") || e.getClassName().startsWith("it.tnx") || e.getClassName().startsWith("invoicex")) {
                            System.out.println("TRACE: \t\t" + e.toString());
                        }
                    }
                    System.out.println("---END SQL TRACE---");
                }

                tries = max + 1;
            } catch (Exception ex) {
                System.err.println("sql di errore:" + sql);
                try {
                    if (ex.getCause() instanceof SQLException) {
                        SQLException sqlex = (SQLException) ex.getCause();
                        String sqlState = sqlex.getSQLState();
                        if (!("08S01".equals(sqlState)) && !("40001".equals(sqlState))) {
                            tries = max + 1;
                        }
                    } else if (ex.getClass().getName().equals("com.mysql.jdbc.exceptions.jdbc4.CommunicationsException")) {
                        //resetto connessione e ritento                        
                        if (resetConn != null) {
                            conn = (Connection) resetConn.call();
                        }
                    } else {
                        throw ex;
                    }
                } catch (Exception ex0) {
                    throw ex0;
                }
            } finally {
                if (stat != null) {
                    try {
                        stat.close();
                    } catch (Exception ex) {
                        throw ex;
                    }
                }
            }
        } while (tries < max);
        if (bench) {
            mb.out("BENCH: tryExecQuery");
        }
        return ret;
    }

    static public int tryExecQueryWithResult(Connection conn, String sql) throws Exception {
        return tryExecQueryWithResult(conn, sql, false);
    }

    static public int tryExecQueryWithResult(Connection conn, String sql, boolean ignoresync) throws Exception {
        conteggio(sql);

        //controllo se arriva da dbchanges non considerare sync
        if (!ignoresync) {
            try {
                Object retissync = ReflectUtils.runMethod("it.tnx.invoicex.sync.Sync", "isQueryToSync", new Object[]{sql});
                if ((Boolean) retissync) {
                    Object retexec = ReflectUtils.runMethod("it.tnx.Db", "executeSqlWithResultDialogExcSync", new Object[]{conn, sql, false, true});
                    return (Integer) retexec;
                }
            } catch (ClassNotFoundException cex) {
                System.out.println("");
            } catch (IllegalAccessException cex) {
                System.out.println("");
            } catch (IllegalArgumentException cex) {
                System.out.println("");
            } catch (InvocationTargetException cex) {
                throw (Exception) cex.getTargetException();
            } catch (Exception ex) {
                throw ex;
            }
        }

        int max = 5;
        int tries = 0;
        Statement stat = null;
        boolean ret = false;
        int uc = -1;
        do {
            tries++;
            try {
                stat = conn.createStatement();
                ret = stat.execute(sql);

                //debug
                StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
                System.out.println("---START SQL TRACE---");
                System.out.println("sql: " + sql);
                for (StackTraceElement e : stacks) {
                    if (e.getClassName().startsWith("gestioneFatture") || e.getClassName().startsWith("it.tnx")) {
                        System.out.println("TRACE: \t\t" + e.toString());
                    }
                }
                System.out.println("---END SQL TRACE---");

                uc = stat.getUpdateCount();
                tries = max + 1;
            } catch (Exception ex) {
                try {
                    if (ex.getCause() instanceof SQLException) {
                        SQLException sqlex = (SQLException) ex.getCause();
                        String sqlState = sqlex.getSQLState();
                        if (!("08S01".equals(sqlState)) && !("40001".equals(sqlState))) {
                            tries = max + 1;
                        }
                    } else if (ex.getClass().getName().equals("com.mysql.jdbc.exceptions.jdbc4.CommunicationsException")) {
                        //resetto connessione e ritento                        
                        if (resetConn != null) {
                            conn = (Connection) resetConn.call();
                        }
                    } else {
                        throw ex;
                    }
                } catch (Exception ex0) {
                    throw ex0;
                }
            } finally {
                if (stat != null) {
                    try {
                        stat.close();
                    } catch (Exception ex) {
                        throw ex;
                    }
                }
            }
        } while (tries < max);
        return uc;
    }

    static public ResultSet tryOpenResultSet(Connection conn, String sql) throws Exception {
        conteggio(sql);
        if (checkCache(sql)) {
            return (ResultSet) fromCache(sql);
        }

        int max = 5;
        int tries = 0;
        Statement stat = null;
        ResultSet ret = null;
        MicroBench mb = null;
        if (bench) {
            mb = new MicroBench(true);
        }

        //debug
        if (debug) {
            StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
            System.out.println("---START SQL TRACE---");
            System.out.println("sql: " + sql);
            for (StackTraceElement e : stacks) {
                if (e.getClassName().startsWith("gestioneFatture") || e.getClassName().startsWith("it.tnx") || e.getClassName().startsWith("invoicex")) {
                    System.out.println("TRACE: \t\t" + e.toString());
                }
            }
            System.out.println("---END SQL TRACE---");
        }

        do {
            tries++;
            try {
                stat = conn.createStatement();
                ret = stat.executeQuery(sql);
                tries = max + 1;
            } catch (Exception ex) {
                try {
                    System.out.println("sql di errore: " + sql);
                    System.out.println("ex.getClass().getName() = " + ex.getClass().getName());
                    if (ex.getCause() instanceof SQLException) {
                        SQLException sqlex = (SQLException) ex.getCause();
                        String sqlState = sqlex.getSQLState();
                        if (!("08S01".equals(sqlState)) && !("40001".equals(sqlState))) {
                            tries = max + 1;
                        }
                    } else if (ex.getClass().getName().equals("com.mysql.jdbc.exceptions.jdbc4.CommunicationsException")) {
                        //resetto connessione e ritento                        
                        if (resetConn != null) {
                            conn = (Connection) resetConn.call();
                        }
                    } else {
                        throw ex;
                    }
                } catch (Exception ex0) {
                    throw ex0;
                }
            }
        } while (tries < max);
        if (bench) {
            mb.out("BENCH: tryOpenResultSet");
        }
        inCache(sql, ret);
        return ret;
    }

    static public ResultSet tryOpenResultSetEditable(Connection conn, String sql) throws Exception {
        conteggio(sql);

        int max = 5;
        int tries = 0;
        Statement stat = null;
        ResultSet ret = null;

        do {
            tries++;
            try {
                stat = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stat.setFetchSize(50);
                ret = stat.executeQuery(sql);
                tries = max + 1;
            } catch (Exception ex) {
                try {
                    if (ex.getCause() instanceof SQLException) {
                        SQLException sqlex = (SQLException) ex.getCause();
                        String sqlState = sqlex.getSQLState();
                        if (!("08S01".equals(sqlState)) && !("40001".equals(sqlState))) {
                            tries = max + 1;
                        }
                    } else if (ex.getClass().getName().equals("com.mysql.jdbc.exceptions.jdbc4.CommunicationsException")) {
                        //resetto connessione e ritento                        
                        if (resetConn != null) {
                            conn = (Connection) resetConn.call();
                        }
                    } else {
                        if (stat != null) {
                            stat.close();
                        }
                        throw ex;
                    }
                } catch (Exception ex0) {
                    if (stat != null) {
                        stat.close();
                    }
                    throw ex0;
                }
            }
        } while (tries < max);
        return ret;
    }

    static public ResultSet tryOpenResultSetEditableSensitive(Connection conn, String sql) throws Exception {
        conteggio(sql);

        int max = 5;
        int tries = 0;
        Statement stat = null;
        ResultSet ret = null;

        do {
            tries++;
            try {
                stat = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stat.setFetchSize(50);
                ret = stat.executeQuery(sql);
                tries = max + 1;
            } catch (Exception ex) {
                try {
                    if (ex.getCause() instanceof SQLException) {
                        SQLException sqlex = (SQLException) ex.getCause();
                        String sqlState = sqlex.getSQLState();
                        if (!("08S01".equals(sqlState)) && !("40001".equals(sqlState))) {
                            tries = max + 1;
                        }
                    } else if (ex.getClass().getName().equals("com.mysql.jdbc.exceptions.jdbc4.CommunicationsException")) {
                        //resetto connessione e ritento                        
                        if (resetConn != null) {
                            conn = (Connection) resetConn.call();
                        }
                    } else {
                        throw ex;
                    }
                } catch (Exception ex0) {
                    throw ex0;
                }
            }
        } while (tries < max);
        return ret;
    }

    public static Connection getMysqlJdbcConn(String server, String database, String user, String password) throws Exception {
        return getMysqlJdbcConn(server, database, user, password, false);
    }

    public static Connection getMysqlJdbcConn(String server, String database, String user, String password, boolean profile) throws Exception {
        return getMysqlJdbcConn(server, database, user, password, false, false);
    }
    public static Connection getMysqlJdbcConn(String server, String database, String user, String password, boolean profile, boolean compression) throws Exception {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        String url = "jdbc:mysql://" + server + "/" + database + "?jdbcCompliantTruncation=false&zeroDateTimeBehavior=round&emptyStringsConvertToZero=true&autoReconnect=true&allowMultiQueries=true";
        if (profile) {
            url += "&profileSql=true";
        }
        if (compression) {
            url += "&useCompression=true";
        } else {
            url += "&useCompression=false";
        }
        url += "&connectTimeout=5000";
        url += "&socketTimeout=10000";
        url += "&useAffectedRows=true";
        Connection conn = conn = DriverManager.getConnection(url, user, password);
        return conn;
    }

    public static Object getObject(Connection conn, String sql) throws Exception {
        return getObject(conn, sql, true);
    }

    public static Object getObject(Connection conn, String sql, boolean throwsExc) throws Exception {
        if (checkCache(sql + "|obj")) {
            return fromCache(sql + "|obj");
        }

        ResultSet r = null;
        try {
            r = tryOpenResultSet(conn, sql);
            if (r.next()) {
                Object o = r.getObject(1);
                inCache(sql + "|obj", o);
                return o;
            }
            if (throwsExc) {
                throw new Exception("record non trovato sql:" + sql);
            } else {
                return null;
            }
        } finally {
            if (r != null) {
                close(r);
            }
        }
    }

    public static String getString(Connection conn, String sql) {
        if (checkCache(sql + "|str")) {
            return (String) fromCache(sql + "|str");
        }

        ResultSet r = null;
        try {
            try {
                r = tryOpenResultSet(conn, sql);
                if (r.next()) {
                    String o = r.getString(1);
                    inCache(sql + "|str", o);
                    return o;
                }
            } catch (Exception ex) {
            }
            return null;
        } finally {
            if (r != null) {
                close(r);
            }
        }
    }

    public static ArrayList getList(Connection conn, String sql) throws Exception {
        conteggio(sql);

        ResultSet r = null;
        ArrayList list = new ArrayList();
        try {
            r = tryOpenResultSet(conn, sql);
            while (r.next()) {
                list.add(r.getObject(1));
            }
            return list;
        } finally {
            if (r != null) {
                close(r);
            }
        }
    }

    public static ArrayList getListInt(Connection conn, String sql) throws Exception {
        conteggio(sql);

        ResultSet r = null;
        ArrayList list = new ArrayList();
        try {
            r = tryOpenResultSet(conn, sql);
            while (r.next()) {
                list.add(r.getInt(1));
            }
            return list;
        } finally {
            if (r != null) {
                close(r);
            }
        }
    }

    public static ArrayList<Object[]> getListArray(Connection conn, String sql) throws Exception {
        conteggio(sql);

        ResultSet r = null;
        ArrayList list = new ArrayList();
        try {
            r = tryOpenResultSet(conn, sql);
            while (r.next()) {
                list.add(getRow(r));
            }
            return list;
        } finally {
            if (r != null) {
                close(r);
            }
        }
    }

    public static ArrayList<Object[]> getListArray(ResultSet r) throws Exception {
        ArrayList list = new ArrayList();
        while (r.next()) {
            list.add(getRow(r));
        }
        return list;
    }

    public static ArrayList<Object[]> getListKV(Connection conn, String sql) throws Exception {
        conteggio(sql);

        ResultSet r = null;
        ArrayList list = new ArrayList();
        try {
            r = tryOpenResultSet(conn, sql);
            while (r.next()) {
                list.add(new Object[]{r.getObject(1), r.getObject(2)});
            }
            return list;
        } finally {
            if (r != null) {
                close(r);
            }
        }
    }
    
    public static ArrayList<KeyValuePair> getListKeyValuePair(Connection conn, String sql) throws Exception {
        conteggio(sql);

        ResultSet r = null;
        ArrayList list = new ArrayList();
        try {
            r = tryOpenResultSet(conn, sql);
            while (r.next()) {
                list.add(new KeyValuePair(r.getObject(1), r.getObject(2)));
            }
            return list;
        } finally {
            if (r != null) {
                close(r);
            }
        }
    }    

    public static ArrayList<Map> getListMap(Connection conn, String sql) throws Exception {
        return getListMap(conn, sql, false);
    }

    public static ArrayList<Map> getListMap(Connection conn, String sql, boolean date_as_string) throws Exception {
        conteggio(sql);
        if (checkCache(sql + "|listmap")) {
            return (ArrayList<Map>) fromCache(sql + "|listmap");
        }

        ResultSet r = null;
        ArrayList list = new ArrayList();
        MicroBench mb = null;
        if (bench) {
            mb = new MicroBench(true);
        }
        try {
            r = tryOpenResultSet(conn, sql);
            ResultSetMetaData m = r.getMetaData();
            while (r.next()) {
                HashMap map = new HashMap();
                for (int i = 0; i < m.getColumnCount(); i++) {
                    Object o = r.getObject(i + 1);
                    if (date_as_string && (o instanceof java.util.Date || o instanceof Timestamp || o instanceof java.sql.Date)) {
//                        o = ((java.util.Date) o).getTime() / 1000;
                        o = FormatUtils.formatMysqlTimestamp((java.util.Date) o);
                    }
                    map.put(m.getColumnLabel(i + 1), o);
                }
                list.add(map);
            }
            if (bench) {
                mb.out("BENCH: getListMap");
            }
            inCache(sql + "|listmap", list);
            return list;
        } finally {
            if (r != null) {
                close(r);
            }
        }
    }

    public static HashMap getListMapMap(Connection conn, String sql, String pk) throws Exception {
        return getListMapMap(conn, sql, pk, false);
    }

    public static HashMap getListMapMap(Connection conn, String sql, String pk, boolean forcePkToInteger) throws Exception {
        conteggio(sql);

        ResultSet r = null;
        HashMap list = new HashMap();
        MicroBench mb = null;
        if (bench) {
            mb = new MicroBench(true);
        }
        try {
            r = tryOpenResultSet(conn, sql);
            ResultSetMetaData m = r.getMetaData();
            while (r.next()) {
                HashMap map = new HashMap();
                for (int i = 0; i < m.getColumnCount(); i++) {
//                    map.put(m.getColumnName(i + 1), r.getObject(i + 1));
                    map.put(m.getColumnLabel(i + 1), r.getObject(i + 1));
                }
//                list.put(r.getObject(pk), map);
                if (forcePkToInteger) {
                    list.put(CastUtils.toInteger(map.get(pk)), map);
                } else {
                    list.put(map.get(pk), map);
                }
            }
            if (bench) {
                mb.out("BENCH: getListMapMap");
            }
            return list;
        } finally {
            if (r != null) {
                close(r);
            }
        }
    }

    public static List<Map> getListMapFromTableModel(TableModel model) throws Exception {
        return getListMapFromTableModel(model, null, null, null, null);
    }

    public static List<Map> getListMapFromTableModel(TableModel model, String[] includere, String[] escludere, Map tras, Map fixed) throws Exception {
        ArrayList list = new ArrayList();
        List listinc = includere == null ? null : Arrays.asList(includere);
        List listesc = escludere == null ? null : Arrays.asList(escludere);
        MicroBench mb = null;
        if (bench) {
            mb = new MicroBench(true);
        }
        for (int r = 0; r < model.getRowCount(); r++) {
            HashMap map = new HashMap();
            for (int i = 0; i < model.getColumnCount(); i++) {
                String colname = model.getColumnName(i);
                boolean inc = true;
                if (listinc != null) {
                    inc = false;
                    if (listinc.contains(colname)) {
                        inc = true;
                    }
                }
                if (listesc != null) {
                    if (listesc.contains(colname)) {
                        inc = false;
                    }
                }
                if (inc) {
                    if (tras != null && tras.containsKey(colname)) {
                        colname = cu.s(tras.get(colname));
                    }
                    map.put(colname, model.getValueAt(r, i));
                }
            }
            if (fixed != null) {
                Set fixedkeys = fixed.keySet();
                for (Object fkey : fixedkeys) {
                    map.put(fkey, fixed.get(fkey));
                }
            }
            list.add(map);
        }
        if (bench) {
            mb.out("BENCH: getListMapFromTableModel");
        }
        return list;
    }

    public static HashMap getListMapMapFromTableModel(TableModel model, String pk) throws Exception {
        return getListMapMapFromTableModel(model, pk, null, null, null, null);
    }

    public static HashMap getListMapMapFromTableModel(TableModel model, String pk, String[] includere, String[] escludere, Map tras, Map fixed) throws Exception {
        HashMap list = new HashMap();
        List listinc = includere == null ? null : Arrays.asList(includere);
        List listesc = escludere == null ? null : Arrays.asList(escludere);
        MicroBench mb = null;
        if (bench) {
            mb = new MicroBench(true);
        }
        for (int r = 0; r < model.getRowCount(); r++) {
            HashMap map = new HashMap();
            for (int i = 0; i < model.getColumnCount(); i++) {
                String colname = model.getColumnName(i);
                boolean inc = true;
                if (listinc != null) {
                    inc = false;
                    if (listinc.contains(colname)) {
                        inc = true;
                    }
                }
                if (listesc != null) {
                    if (listesc.contains(colname)) {
                        inc = false;
                    }
                }
                if (inc) {
                    if (tras != null && tras.containsKey(colname)) {
                        colname = cu.s(tras.get(colname));
                    }
                    map.put(colname, model.getValueAt(r, i));
                }
            }
            if (fixed != null) {
                Set fixedkeys = fixed.keySet();
                for (Object fkey : fixedkeys) {
                    map.put(fkey, fixed.get(fkey));
                }
            }
            
/* problema venuto fuori su clienti fornitori: se si inseriscono più contatti memorizzava solo l'ultimo perchè la hashmpa non può avere due chiavi uguali e quindi non può avere due null come chiave
//            if (map.get(pk) != null) {
                list.put(map.get(pk), map);
//            }
*/

            if (map.get(pk) != null) {
                list.put(map.get(pk), map);
            } else {
                list.put(new NullValue(), map);
            }

        }
        if (bench) {
            mb.out("BENCH: getListMapFromTableModel");
        }
        return list;
    }







    public static Map getListMapMapFromList(List<Map> list, String pk) throws Exception {
        return getListMapMapFromList(list, pk, null, null, null, null);
    }

    public static Map getListMapMapFromList(List<Map> list, String pk, String[] includere, String[] escludere, Map tras, Map fixed) throws Exception {
        Map list_out = new LinkedHashMap();
        List listinc = includere == null ? null : Arrays.asList(includere);
        List listesc = escludere == null ? null : Arrays.asList(escludere);
        MicroBench mb = null;
        if (bench) {
            mb = new MicroBench(true);
        }
        for (int r = 0; r < list.size(); r++) {
            HashMap map = new HashMap();
            
            for (Object key : list.get(r).keySet()) {
                String colname = cu.s(key);
                boolean inc = true;
                if (listinc != null) {
                    inc = false;
                    if (listinc.contains(colname)) {
                        inc = true;
                    }
                }
                if (listesc != null) {
                    if (listesc.contains(colname)) {
                        inc = false;
                    }
                }
                if (inc) {
                    if (tras != null && tras.containsKey(colname)) {
                        colname = cu.s(tras.get(colname));
                    }
                    map.put(colname, list.get(r).get(key));
                }
            }
            if (fixed != null) {
                Set fixedkeys = fixed.keySet();
                for (Object fkey : fixedkeys) {
                    map.put(fkey, fixed.get(fkey));
                }
            }
            
/* problema venuto fuori su clienti fornitori: se si inseriscono più contatti memorizzava solo l'ultimo perchè la hashmpa non può avere due chiavi uguali e quindi non può avere due null come chiave
//            if (map.get(pk) != null) {
                list.put(map.get(pk), map);
//            }
*/

            if (map.get(pk) != null) {
                list_out.put(map.get(pk), map);
            } else {
                list_out.put(new NullValue(), map);
            }

        }
        if (bench) {
            mb.out("BENCH: getListMapMapFromList");
        }
        return list_out;
    }




    public static HashMap getListMapKV(Connection conn, String sql) throws Exception {
        conteggio(sql);

        ResultSet r = null;
        HashMap map = new HashMap();
        try {
            r = tryOpenResultSet(conn, sql);
            ResultSetMetaData m = r.getMetaData();
            while (r.next()) {
                map.put(r.getObject(1), r.getObject(2));
            }
            return map;
        } finally {
            if (r != null) {
                close(r);
            }
        }
    }

    public static boolean containRows(Connection conn, String sql) throws Exception {
        conteggio(sql);

        ResultSet r = null;
        try {
            r = tryOpenResultSet(conn, sql);
            if (r.next()) {
                return true;
            } else {
                return false;
            }
        } finally {
            if (r != null) {
                close(r);
            }
        }
    }

    public static boolean existTable(Connection conn, String table) throws SQLException {
        try {
            List tables = DbUtils.getList(conn, "show tables");
            if (tables.contains(table)) {
                return true;
            }
        } catch (Exception ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean existColumn(ResultSet r, String column) throws SQLException {
        ResultSetMetaData m = r.getMetaData();
        for (int i = 1; i <= m.getColumnCount(); i++) {
//            if (m.getColumnName(i).equalsIgnoreCase(column)) {
//                return true;
//            }
            if (m.getColumnLabel(i).equalsIgnoreCase(column)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean existColumn(Connection conn, String table, String column) throws Exception {
        List fields = dbu.getList(conn, "describe " + table);
        if (fields.contains(column)) {
            return true;
        }
        return false;
    }    

    public static String pc2(java.util.Date campo, int tipoCampo) {
        if (tipoCampo == Types.DATE) {
            DateFormat myFormat = new SimpleDateFormat("dd/MM/yy");
            DateFormat myFormatSql = new SimpleDateFormat("yyyy-MM-dd");
            myFormat.setLenient(false);
            try {
                return "'" + myFormatSql.format(campo) + "'";
            } catch (Exception err) {
                System.out.println("errore in campo: " + campo);
                err.printStackTrace();
                return ("0");
            }
        } else if (tipoCampo == Types.TIME) {
            DateFormat myFormatSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return "'" + myFormatSql.format(campo) + "'";
            } catch (Exception err) {
                System.out.println("errore in campo: " + campo);
                err.printStackTrace();
                return ("0");
            }
        } else {
            return ("'" + aa(campo.toString()) + "'");
        }
    }

    public static String aaOld(String stringa) {
        if (stringa != null) {
            if (stringa.length() > 0) {

//                if (stringa.indexOf("amicizia") >= 0) {
//                    System.out.println("stop");
//                }
                stringa = StringUtils.replace(stringa, "\\'", "'");
                stringa = StringUtils.replace(stringa, "'", "\\'");

                return stringa;
            }
        }
        return ("");
    }

    public static String aa(String stringa) {
        if (stringa != null) {
            if (stringa.length() > 0) {

//                if (stringa.indexOf("amicizia") >= 0) {
//                    System.out.println("stop");
//                }
                stringa = StringUtils.replace(stringa, "\\'", "'");
                stringa = StringUtils.replace(stringa, "\\", "\\\\");
                stringa = StringUtils.replace(stringa, "'", "\\'");

                //da https://dev.mysql.com/doc/refman/5.0/en/string-literals.html
                stringa = StringUtils.replace(stringa, "\0", "\\0");
                stringa = StringUtils.replace(stringa, "\b", "\\b");
                stringa = StringUtils.replace(stringa, "\n", "\\n");
                stringa = StringUtils.replace(stringa, "\r", "\\r");
                stringa = StringUtils.replace(stringa, "\t", "\\t");
                stringa = StringUtils.replace(stringa, "\u001A", "\\Z");

                return stringa;
            }
        }
        return ("");
    }

    public static String aaAcc(String stringa) {
        if (stringa != null) {
            if (stringa.length() > 0) {
                stringa = StringUtils.replace(stringa, "\\'", "'");
                stringa = StringUtils.replace(stringa, "'", "''");
                return stringa;
            }
        }
        return ("");
    }

    public static String aaJava(String str) {
        String temp = StringUtils.replace(str, "\\'", "'");
        temp = StringUtils.replace(str, "'", "\\\\'");
        temp = StringUtils.replace(str, "\n", "\\n\" +\n\"");
        return temp;
    }

    public static String dbCompare(String host1, String db1, String user1, String passwd1, String host2, String db2, String user2, String passwd2) {
        return dbCompare(host1, db1, user1, passwd1, host2, db2, user2, passwd2, null, null);
    }

    public static String dbCompare(String host1, String db1, String user1, String passwd1, String host2, String db2, String user2, String passwd2, String tab1, String tab2) {
        try {
            Connection c1 = DbUtils.getMysqlJdbcConn(host1, db1, user1, passwd1);
            Connection c2 = DbUtils.getMysqlJdbcConn(host2, db2, user2, passwd2);

            ArrayList tables1a = null;
            ArrayList tables2a = null;

            String ret = "";

            if (tab1 == null) {
                ResultSet tables1 = c1.getMetaData().getTables("", "", "", null);
                ResultSet tables2 = c2.getMetaData().getTables("", "", "", null);
                tables1a = ArraysUtils.toArrayList(tables1, "TABLE_NAME");
                tables2a = ArraysUtils.toArrayList(tables2, "TABLE_NAME");
            } else {
                tables1a = new ArrayList();
                tables1a.add(tab1);
                tables2a = new ArrayList();
                tables2a.add(tab2);
            }

//            DebugUtils.dump(tables1);
//            DebugUtils.dump(tables2);
            //controllo
            ArrayList tab_mancanti_in_2 = new ArrayList();
            ArrayList tab_mancanti_in_1 = new ArrayList();
            TreeSet tab_comuni = new TreeSet();
            tab_comuni.addAll(tables1a);
            tab_comuni.addAll(tables2a);

            for (Object o : tables1a) {
                String s = (String) o;
                if (!ArraysUtils.containString(s, tables2a)) {
                    tab_mancanti_in_2.add(s);
                    tab_comuni.remove(s);
                }
            }
            for (Object o : tables2a) {
                String s = (String) o;
                if (!ArraysUtils.containString(s, tables1a)) {
                    tab_mancanti_in_1.add(s);
                    tab_comuni.remove(s);
                }
            }
            System.out.println("tabelle mancanti in 2");
            System.err.println(ArraysUtils.toString(tab_mancanti_in_2, "|"));
            System.out.println("tabelle mancanti in 1");
            System.err.println(ArraysUtils.toString(tab_mancanti_in_1, "|"));
            System.out.println("tabelle comuni");
            System.out.println(ArraysUtils.toString(tab_comuni, "|"));

            for (Object otab : tab_comuni) {
                String tab = (String) otab;
                System.out.println("tab:" + tab);
                String sql = "select * from " + tab + " limit 0";
                ResultSet r1 = DbUtils.tryOpenResultSet(c1, sql);
                ResultSet r2 = DbUtils.tryOpenResultSet(c2, sql);
                ResultSetMetaData m1 = r1.getMetaData();
                ResultSetMetaData m2 = r2.getMetaData();
                for (int c = 1; c <= m2.getColumnCount(); c++) {
                    String colname1 = m2.getColumnName(c);
                    //prima la cerco nel 2
                    boolean found = false;
                    int ic2 = 0;
                    for (ic2 = 1; ic2 <= m1.getColumnCount(); ic2++) {
                        if (m1.getColumnName(ic2).equalsIgnoreCase(colname1)) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        String coltype1 = m2.getColumnTypeName(c);
                        String coltype2 = m1.getColumnTypeName(ic2);
                        if (coltype1.equalsIgnoreCase(coltype2)) {
                            int colprec2 = m2.getPrecision(c);
                            int colprec1 = m1.getPrecision(ic2);
                            if (colprec1 != colprec2) {
                                System.err.println(colname1 + " precisione diversa dal 2 -> " + colprec1 + " != " + colprec2);
                                ret += "\nalter table " + tab1 + " modify column " + getColumnDef(c2, tab2, colname1) + ";";
                            }
                        } else {
                            System.err.println(colname1 + " tipo diverso dal 2 -> " + coltype1 + " != " + coltype2);
                            ret += "\nalter table " + tab1 + " modify column " + getColumnDef(c2, tab2, colname1) + ";";
                        }
                    } else {
                        System.err.println(colname1 + " non trovata nel 2");
                        ret += "\nalter table " + tab1 + " add column " + getColumnDef(c2, tab2, colname1) + ";";
                    }
                }
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object[] getRow(ResultSet r) throws Exception {
        int cols = r.getMetaData().getColumnCount();
        Object[] row = new Object[cols];
        for (int c = 1; c <= cols; c++) {
            row[c - 1] = r.getObject(c);
        }
        return row;
    }

    public static Map getRowMap(ResultSet r) throws Exception {
        HashMap map = new HashMap();
        ResultSetMetaData m = r.getMetaData();
        int cols = m.getColumnCount();
        boolean norow = r.isBeforeFirst();
        System.out.println(r.isAfterLast());
        System.out.println(r.isBeforeFirst());
        for (int c = 1; c <= cols; c++) {
            Object val = null;
            if (!norow) {
                val = r.getObject(c);
            }
            map.put(m.getColumnLabel(c), val);
        }
        return map;
    }

    public static void dumpResultSet(Connection conn, String sql) {
        ResultSet r = null;
        ResultSetMetaData m = null;
        try {
            r = tryOpenResultSet(conn, sql);
            m = r.getMetaData();
            for (int i = 1; i <= m.getColumnCount(); i++) {
                System.out.print(FormatUtils.fill(m.getColumnLabel(i), Math.min(m.getColumnDisplaySize(i), 50)));
                System.out.print("|");
            }
            System.out.println("");
            while (r.next()) {
                for (int i = 1; i <= m.getColumnCount(); i++) {
                    System.out.print(FormatUtils.fill(r.getString(i), Math.min(m.getColumnDisplaySize(i), 50)));
                    System.out.print("|");
                }
                System.out.println("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(r);
        }
    }

    public static String prepareSqlFromMap(Map mkeyvalue) {
        return prepareSqlFromMap(mkeyvalue, ", ");
    }

    public static String prepareSqlFromMap(Map mkeyvalue, String separator) {
        String sql = "";
        Set set = mkeyvalue.keySet();
        Iterator iter = set.iterator();
        int max = set.size();
        int conta = 0;
        while (iter.hasNext()) {
            conta++;
            String key = (String) iter.next();
            Object value = mkeyvalue.get(key);
            sql += " `" + key + "` = ";
            sql += sql(value);
            if (conta < max) {
                sql += separator;
            }
        }
        return sql;
    }

    public static String sql(Object value) {
        String sql = "";
        if (value instanceof String) {
            sql += "'" + DbUtils.aa((String) value) + "'";
        } else if (value instanceof Integer) {
            sql += "(" + value + ")";
        } else if (value instanceof Float || value instanceof Double || value instanceof BigInteger || value instanceof Long || value instanceof BigDecimal) {
            sql += "(" + value + ")";
        } else if (value instanceof Timestamp) {
            value = FormatUtils.formatMysqlTimestamp((Timestamp) value);
            sql += "'" + value + "'";
        } else if (value instanceof Date) {
            value = FormatUtils.formatMysqlDate((Date) value);
            sql += "'" + value + "'";
        } else if (value instanceof java.util.Date) {
            value = FormatUtils.formatMysqlDate((java.util.Date) value);
            sql += "'" + value + "'";
        } else if (value instanceof Boolean) {
            sql += "'" + value + "'";
        } else if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            if (bytes.length == 0) {
                return "null";
            }
            sql += "0x" + bytesToHex(bytes);
//TODO, testare binario diretto in stringa            
//            sql += "'" + DbUtils.aa(new String(bytes)) + "'";
        } else if (value == null) {
            sql += "null";
        } else {
            System.err.println("sql(): " + value.getClass().toString() + " non riconosciuto");
            sql += "'" + DbUtils.aa((String) value) + "'";
        }
        return sql;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String h = Integer.toHexString(bytes[i] & 0xFF);
            if (h.length() == 1) {
                h = "0" + h;
            }
            buffer.append(h);
        }
        return buffer.toString().toUpperCase();
    }

    public static String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            String h = Integer.toHexString(bytes[i + offset] & 0xFF);
            if (h.length() == 1) {
                h = "0" + h;
            }
            buffer.append(h);
        }
        return buffer.toString().toUpperCase();
    }

    public static byte[] hexToByte(String hex) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int i = 2; i < hex.length(); i++) {
            String hexp = hex.substring(i, i + 2);
            int b = Integer.decode("0x" + hexp);
            bout.write(b);
            i++;
        }
        try {
            return bout.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String prepareSqlForCopy(Map mkeyvalueFrom, Map tras) {
        String sql = "";
//        Set set = mkeyvalueFrom.keySet();
        Set set = tras.keySet();
        Iterator iter = set.iterator();
        int max = set.size();
        int conta = 0;
        while (iter.hasNext()) {
            conta++;
            String key = (String) iter.next();
            Object value = mkeyvalueFrom.get(key);
//            sql += " " + key + " = ";
            sql += " " + tras.get(key) + " = ";
            if (value instanceof String) {
                sql += "'" + DbUtils.aa((String) value) + "'";
            } else if (value instanceof Integer) {
                sql += "(" + value + ")";
            } else if (value instanceof Float || value instanceof Double || value instanceof BigInteger || value instanceof Long || value instanceof BigDecimal) {
                sql += "(" + value + ")";
            } else if (value instanceof Timestamp) {
                value = FormatUtils.formatMysqlTimestamp((Timestamp) value);
                sql += "'" + value + "'";
            } else if (value instanceof Date) {
                value = FormatUtils.formatMysqlDate((Date) value);
                sql += "'" + value + "'";
            } else if (value instanceof java.util.Date) {
                value = FormatUtils.formatMysqlDate((java.util.Date) value);
                sql += "'" + value + "'";
            } else if (value instanceof Boolean) {
                sql += "'" + value + "'";
            } else if (value == null) {
                sql += "null";
            } else {
                System.err.println("prepareSqlForCopy: " + value.getClass().toString() + " non riconosciuto");
                sql += "'" + DbUtils.aa((String) value) + "'";
            }
            if (conta < max) {
                sql += ",";
            }
        }
        return sql;
    }

    public static String prepareSqlFromMapJava(Map mkeyvalue) {
        String sql = "";
        Set set = mkeyvalue.keySet();
        Iterator iter = set.iterator();
        int max = set.size();
        int conta = 0;
        while (iter.hasNext()) {
            conta++;
            String key = (String) iter.next();
            Object value = mkeyvalue.get(key);
            sql += " " + key + " = ";
            if (value instanceof String) {
                sql += "'" + DbUtils.aaJava((String) value) + "'";
            } else if (value instanceof Integer) {
                sql += "(" + value + ")";
            } else if (value instanceof Float || value instanceof Double || value instanceof BigInteger || value instanceof Long || value instanceof BigDecimal) {
                sql += "(" + value + ")";
            } else if (value instanceof Timestamp) {
                FormatUtils.formatMysqlTimestamp((Timestamp) value);
                sql += "'" + value + "'";
            } else if (value == null) {
                sql += "null";
            } else {
                System.err.println("prepareSqlFromMap: " + value.getClass().toString() + " non riconosciuto");
                sql += "'" + DbUtils.aaJava((String) value) + "'";
            }
            if (conta < max) {
                sql += ",";
            }
        }
        return sql;
    }

    public static String getCreateTable(String tableName, Connection dbConnection) {
        ResultSet show = null;
        Statement stat = null;
        String sqlCreate = "", sqlPrimary, sqlNull, sqlDefault, sqlExtra;
        int campiPK = 0;

        try {
            stat = dbConnection.createStatement();
            show = stat.executeQuery("show columns from " + tableName);
            sqlCreate = "create table " + tableName + " (\n";
            sqlPrimary = "PRIMARY KEY (";
            while (show.next()) {
                if (show.getString("Key").equals("PRI")) {
                    sqlPrimary += show.getString("Field") + ",";
                    campiPK++;
                }
                sqlDefault = "";
                if (StringUtils.isNotEmpty(show.getString("Default"))) {
                    if (show.getString("Default").indexOf("CURRENT") >= 0) {
                        sqlDefault = "DEFAULT " + show.getString("Default") + "";
                    } else {
                        sqlDefault = "DEFAULT '" + show.getString("Default") + "'";
                    }
                }
                sqlNull = "NOT NULL";
                if (StringUtils.isNotEmpty(show.getString("Null"))) {
                    sqlNull = "NULL";
                }
                sqlExtra = " " + show.getString("Extra");
                sqlCreate += "   `" + show.getString("Field") + "` " + show.getString("Type") + " " + sqlNull + sqlExtra + " " + sqlDefault + ",\n";
            }
            //tolgo ultima virgola
            sqlPrimary = sqlPrimary.substring(0, sqlPrimary.length() - 1) + ")";
            //creo sql finale di create
            if (campiPK > 0) {
                sqlCreate = sqlCreate + sqlPrimary + ")";
            } else {
                sqlCreate = sqlCreate.substring(0, sqlCreate.length() - 2) + ")";
            }
        } catch (Exception err) {
            err.printStackTrace();
        } finally {
            try {
                if (show != null) {
                    show.close();
                }
            } catch (Exception e) {
            }
            try {
                if (stat != null) {
                    stat.close();
                }
            } catch (Exception e) {
            }
        }

        return sqlCreate;
    }

    public static void main(String[] args) {
    }

    private static String getColumnDef(Connection c1, String tab1, String colname1) throws Exception {
        ResultSet r = tryOpenResultSet(c1, "show columns from " + tab1 + " where Field = '" + aa(colname1) + "'");
        String sql = "", sqlNull, sqlDefault = "", sqlExtra;
        if (r.next()) {
            if (StringUtils.isNotEmpty(r.getString("Default"))) {
                if (r.getString("Default").indexOf("CURRENT") >= 0) {
                    sqlDefault = "DEFAULT " + r.getString("Default") + "";
                } else {
                    sqlDefault = "DEFAULT '" + r.getString("Default") + "'";
                }
            }
            sqlNull = "NOT NULL";
            if (StringUtils.isNotEmpty(r.getString("Null"))) {
                sqlNull = "NULL";
            }
            sqlExtra = " " + r.getString("Extra");
            sql += "`" + r.getString("Field") + "` " + r.getString("Type") + " " + sqlNull + sqlExtra + " " + sqlDefault;
            return sql;
        }
        return null;
    }

    public static void duplicateColumn(Connection conn, String tab, String source_column, String dest_column) {
        try {
            Object dest = getObject(conn, "show columns from " + tab + " like '" + dest_column + "'", false);
            if (dest == null) {
                String def_s = getColumnDef(conn, tab, source_column);
                System.out.println("def_s = " + def_s);
                String def_d = StringUtils.replace(def_s, source_column, dest_column) + " after " + source_column;
                System.out.println("def_d = " + def_d);
                String sql = "alter table " + tab + " add column " + def_d;
                System.out.println("sql = " + sql);
                DbUtils.tryExecQuery(conn, sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close(ResultSet r) {
        try {
            r.getStatement().close();
        } catch (NullPointerException npe) {
            //ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            r.close();
        } catch (NullPointerException npe) {
            //ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toTable(Connection conn, String sql, JTable table) throws Exception {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        List<Object[]> list = getListArray(conn, sql);
        for (Object[] row : list) {
            model.addRow(row);
        }
    }

    public static void tableChanged(Connection conn, TableModelEvent e, String table, String[] colonne, String pk, int pk_col_id, JTable jtable, int row, int col) throws Exception {
        if (e.getType() == TableModelEvent.UPDATE) {
            Object pkv = jtable.getValueAt(row, pk_col_id);
            Object value = jtable.getValueAt(row, col);
            String sql = "update " + table + " set " + colonne[e.getColumn()] + " = " + sql(value) + " where " + pk + " = " + sql(pkv);
            System.out.println("sql = " + sql);
            DbUtils.tryExecQuery(conn, sql);
        } else {
        }
    }

    static public Integer getColumnIndex(ResultSetMetaData m, String campo) {
        try {
            for (int i = 0; i < m.getColumnCount(); i++) {
                String campon = m.getColumnName(i + 1);
                if (campo != null && campo.equalsIgnoreCase(campon)) {
                    return i + 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void conteggio(String sql) {
        //conteggio
        if (conta) {
            if (mapconta.containsKey(sql)) {
                mapconta.put(sql, mapconta.get(sql) + 1);
            } else {
                mapconta.put(sql, 1);
            }
        }
    }

    static public Map getTopQuery() {
        final Map<String, Integer> map = mapconta;
        TreeMap tm = new TreeMap(new Comparator<String>() {
            public int compare(String k1, String k2) {
                Integer i1 = map.get(k1);
                Integer i2 = map.get(k2);
                return i1.compareTo(i2) == 0 ? 1 : i1.compareTo(i2);
            }
        });
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            try {
                tm.put(e.getKey(), e.getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return tm;
    }

    private static boolean checkCache(String sql) {
        if (cache) {
            if (mapcache.containsKey(sql)) {
                return true;
            }
        }
        return false;
    }

    private static Object fromCache(String sql) {
        System.err.println("DBUTILS FROM CACHE sql:" + sql);
        return mapcache.get(sql);
    }

    private static void inCache(String sql, Object o) {
        if (cache) {
            if (o instanceof ResultSet) {
                DbCacheResultSet cres = new DbCacheResultSet(null, sql, (ResultSet) o);
                o = cres;
            }
            mapcache.put(sql, o);
        }
    }

    public static boolean isAutoInc(Connection conn, String tab, String field) {
        String sql = "describe " + tab;
        try {
            List<Map> list = getListMap(conn, sql);
            if (list != null && list.size() > 0) {
                for (Map m : list) {
                    if (cu.s(m.get("Field")).equalsIgnoreCase(field)) {
                        if (cu.s(m.get("Extra")).indexOf("auto_increment") >= 0) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean isInt(Connection conn, String tab, String field) {
        String sql = "describe " + tab;
        try {
            List<Map> list = getListMap(conn, sql);
            if (list != null && list.size() > 0) {
                for (Map m : list) {
                    if (cu.s(m.get("Field")).equalsIgnoreCase(field)) {
                        if (cu.s(m.get("Type")).toLowerCase().startsWith("int")) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }    

}
