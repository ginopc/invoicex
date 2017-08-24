/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.sync;

import gestioneFatture.Db;
import gestioneFatture.GenericFrmTest;
import gestioneFatture.main;
import static gestioneFatture.main.debug;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugFastUtils;
import it.tnx.commons.DebugUtils;
import static it.tnx.commons.HttpUtils.getHttpClient;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.SystemUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import tnxbeans.tnxDbPanel;

/**
 *
 * @author Marco
 */
public class Sync {

    static public String server;
    static public String nomedb;
    static public String username;
    static public String password;
    static public Integer client_id = null;

    public static final String INSERT = "INSERT";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";

    public static boolean active = false;

    static private Map tablesinfo = null;
    static private Map tablespk = null;

    public static boolean test1 = false;        //per testare wizard iniziale

    public static boolean verbose = true;

    private static String getPrimaryField(String table_name) {
        return cu.s(tablespk.get(table_name));
    }

    static public class DatiRegistrazione {

        public Integer id;
        public String client_name;
    }
    public static DatiRegistrazione dati_registrazione = null;

    public static void setActive(boolean _active) {
        System.out.println("Sync:" + _active);
        active = _active;
    }

    public static boolean isActive() {
        return active;
    }

//    static public boolean add(String tipo, String table, String field, String primary_field, Object primary_value, Object old_value, Object new_value) {
//        if (tipo.equals(UPDATE) && cu.s(old_value).equals(cu.s(new_value))) {
//            //ignoro modifica per campo uguale
//            return true;
//        }
//
//        Map m = new HashMap();
//        m.put("type", tipo);
//        m.put("table_name", table);
//        m.put("field_name", field);
//        m.put("primary_field", primary_field);
//        m.put("primary_value", primary_value);
//        m.put("old_value", old_value);
//        m.put("new_value", new_value);
//        String sql = "insert into sync_slave set " + DbUtils.prepareSqlFromMap(m);
//        System.out.println("sql = " + sql);
//        try {
//            DbUtils.tryExecQuery(Db.getConn(), sql);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
    static public boolean add(Connection conn, String tipo, String table, String field, Object primary_value, Map old_value, Map new_value) {
        try {
            if (tipo.equals(UPDATE)) {
                //se UPDATE salvo un record per ogni campo modificato
                for (Object key : old_value.keySet()) {
                    Object old_value_n = old_value.get(key);
                    Object new_value_n = new_value.get(key);
                    if (old_value_n instanceof byte[]) {
                        //su campi blob controllo md5
                        String old_md5 = DigestUtils.md5Hex((byte[]) old_value_n);
                        String new_md5 = DigestUtils.md5Hex((byte[]) new_value_n);
                        if (verbose) {
                            System.out.println("SYNC confronto " + StringUtils.leftPad(cu.s(key), 20) + " " + (old_md5.equals(cu.s(new_md5)) ? "         " : " DIVERSI ") + " \t " + old_md5 + " -> " + new_md5 + "");
                        }
                        if (old_md5.equals(cu.s(new_md5))) {
                            //ignoro modifica per campo uguale
                            continue;
                        }
                    } else {
                        if (verbose) {
                            System.out.println("SYNC confronto " + StringUtils.leftPad(cu.s(key), 20) + " " + (cu.s(old_value_n).equals(cu.s(new_value_n)) ? "         " : " DIVERSI ") + " \t " + old_value_n + " -> " + new_value_n + "");
                        }
                        if (cu.s(old_value_n).equals(cu.s(new_value_n))) {
                            //ignoro modifica per campo uguale
                            continue;
                        }
                    }
                    Map m = new HashMap();
                    m.put("type", tipo);
                    m.put("table_name", table);
                    m.put("field_name", key);
//                    m.put("primary_field", primary_field);
                    m.put("primary_value", primary_value);
                    m.put("old_value", old_value_n);
                    m.put("new_value", new_value_n);
                    String sql = "insert into sync_slave set " + DbUtils.prepareSqlFromMap(m);
                    System.out.println("sql = " + sql);
                    DbUtils.tryExecQuery(conn, sql, false, true);
                }
            } else if (tipo.equals(INSERT)) {
                for (Object key : new_value.keySet()) {
                    Object new_value_n = new_value.get(key);
                    Map m = new HashMap();
                    m.put("type", tipo);
                    m.put("table_name", table);
                    m.put("field_name", key);
//                    m.put("primary_field", primary_field);
                    m.put("primary_value", primary_value);
                    m.put("new_value", new_value_n);
                    String sql = "insert into sync_slave set " + DbUtils.prepareSqlFromMap(m);
                    System.out.println("sql = " + sql);
                    DbUtils.tryExecQuery(conn, sql, false, true);
                }
            } else if (tipo.equals(DELETE)) {
                Map m = new HashMap();
                m.put("type", tipo);
                m.put("table_name", table);
                m.put("field_name", field);
//                m.put("primary_field", primary_field);
                m.put("primary_value", primary_value);
                String sql = "insert into sync_slave set " + DbUtils.prepareSqlFromMap(m);
                System.out.println("sql = " + sql);
                DbUtils.tryExecQuery(conn, sql, false, true);
            }
            return true;
        } catch (Exception e) {
            SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore in sync: " + e.toString(), true);
            e.printStackTrace();
            return false;
        }
    }

    public static void sync() {

        Connection dbconn = null;
        try {
            dbconn = Db.getConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
            SwingUtils.showExceptionMessage(main.getPadreFrame(), ex);
            return;
        }

        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public boolean isClientTrusted(X509Certificate[] xcs) {
                    return true;
                }

                public boolean isServerTrusted(X509Certificate[] xcs) {
                    return true;
                }
            }
        };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            System.out.println(e);
        }

        URLCodec u = new URLCodec();
//        String url = InvoicexUtil.getUrlWsd();
//        String url = "https://secure.tnx.it/invoicex/index.php?p=wsd_sync";
//        String url = "https://demo.tnx.it/invoicex/index.php?p=wsd_sync";
//        String url = "http://www.demo.tnx.it/invoicex_rest/?c=sync";

//PROCEDURA    
//0 se non abbiamo il client_id in sync_config chiedere al master che venga assegnato        
//1 ricevo dal master gli aggiornamenti da fare (prendere da master quelli con id_master > max(id_master sul client))
//2 eseguire opportune alterazioni struttura db (invoicex: dbchanges sarà disabilitato perchè altrimenti riceverà modifiche db doppie), eseguire opportune rettifiche su id (ma solo se autoinc, se fosse non autoinc deve sovrascrivere): se sul master c'è un nuovo record con id = ad un record tra quelli che devo inviare aggiorno il record che devo inviare cambiandolo al max(id sul server per questa tabella)+1, per cambiare id devo analizzare le foreign key per andare a cambiare le tabelle collegate o farlo fare in automatico alle foreign key
//3 eseguire gli aggiornmaenti ricevuti dal master
//4 assegnare eventuali numeri di documento rimasti da assegnare sul client
//5 inviare modifiche dal cliente verso il master
        //0 se non abbiamo il client_id in sync_config chiedere al master che venga assegnato        
        //TODO
        client_id = null;
        try {
            client_id = cu.i(dbu.getObject(dbconn, "select client_id from sync_config"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (client_id == null) {
            client_id = presentaRegistrazioneClient();
            if (client_id == null) {
                return;
            } else {
                String sql = "delete from sync_config";
                try {
                    dbu.tryExecQuery(Db.getConn(), sql);
                    Map rec = new HashMap();
                    rec.put("client_id", Sync.dati_registrazione.id);
                    rec.put("client_name", Sync.dati_registrazione.client_name);
                    sql = "insert into sync_config set " + dbu.prepareSqlFromMap(rec);
                    dbu.tryExecQuery(Db.getConn(), sql);
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
                    main.exitMain();
                }
            }
        }

        //1 ricevo dal master aggiornamenti da fare
        String url = geturl();
        HttpClientSync h = null;
        String out = null;
        try {
            Integer id_master = cu.i(dbu.getObject(dbconn, "select max(id_master) from sync_slave"));
            if (id_master == null) {
                //lo leggo dall'autoinc di sync_master
                List<Map> list = dbu.getListMap(dbconn, "SHOW TABLE STATUS FROM `" + Db.dbNameDB + "` LIKE 'sync_master'");
                System.out.println("list:" + list);
                id_master = cu.i(list.get(0).get("Auto_increment"));
            }

            h = getHttpClientSync();
            h.q.add(new NameValuePair("c", "Sync"));
            h.q.add(new NameValuePair("m", "syncGetChanges"));
            h.q.add(new NameValuePair("lastMsId", cu.s(id_master)));
            h.q.add(new NameValuePair("clientId", cu.s(client_id)));
            h.get.setQueryString(h.q.toArray(new NameValuePair[h.q.size()]));
            int retcode = 0;
            String message = null;

            System.out.println("get = " + h.get);
            System.out.println("url = " + url + "?" + h.get.getQueryString());
            retcode = h.httpclient.executeMethod(h.get);
            if (debug) {
                System.out.println("httpClient getURL: status: " + h.get.getStatusLine());
                for (Header hd : h.get.getResponseHeaders()) {
                    System.out.print("httpClient getURL: header: " + hd);
                }
            }
            retcode = h.get.getStatusLine().getStatusCode();
            message = h.get.getStatusLine().getReasonPhrase();
            out = h.get.getResponseBodyAsString();
            System.out.println("out = " + out);

            String xinvoicex = cu.s(h.get.getResponseHeader("X-Invoicex"));
            System.out.println("xinvoicex = " + xinvoicex);

            if (retcode != 200) {
                System.out.println("getURL: errore retcode:" + retcode);
                return;
            }

            //analizzo risposta ed inserisco in sync_slave
            if (out != null && out.trim().length() > 0) {
                //provo a json decodare
                JSONParser parser = new JSONParser();
                Object ret = parser.parse(out);
                System.out.println("ret = " + ret);

                JSONObject retjso = (JSONObject) ret;

                Integer lastSsId = cu.i(retjso.get("lastSsId"));
                System.out.println("lastSsId = " + lastSsId);

                JSONArray retjsa = (JSONArray) retjso.get("righe");
                for (Object objn : retjsa) {
                    System.out.println("objn = " + objn);
                    Map m = (Map) objn;
                    System.out.println("m = " + m);

                    //inserisco in sync_slave ma traslo alcuni campi
                    m.put("id_master", m.get("id"));
                    m.remove("id");
                    m.put("sync_date_master", m.get("sync_date"));
                    m.remove("sync_date");
                    m.remove("id_slave");

                    SortedSet<String> keys = new TreeSet<String>(m.keySet());
                    for (String key : keys) {
                        Object value = m.get(key);
                        System.out.println(key + "\t\t\t = " + value);
                    }

                    String sql = "insert into sync_slave set " + dbu.prepareSqlFromMap(m);
                    System.out.println("sql = " + sql);
                    try {
                        dbu.tryExecQuery(dbconn, sql, false, true);
                    } catch (SQLException ex1) {
                        if (ex1.getErrorCode() == 1062) {   //duplicate entry
                            //record già arrivato, probabilmente sync interrotta
                            //quindi ignoro e proseguo i passi della sync
                        } else {
                            ex1.printStackTrace();
                            SwingUtils.showExceptionMessage(main.getPadreFrame(), ex1);
                            return;
                        }
                    }
                }
            } else {
                //non ci sono aggiornamenti
            }
        } catch (ParseException pex) {
            pex.printStackTrace();
            SwingUtils.showErrorMessage(main.getPadreFrame(), "Error parsing response:\n" + out);
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            SwingUtils.showExceptionMessage(main.getPadreFrame(), ex);
            return;
        } finally {
            if (h != null && h.get != null) {
                h.get.releaseConnection();
            }
        }

        //2
//        mmm... 2.1 eseguire opportune alterazioni struttura db (invoicex: dbchanges sarà disabilitato perchè altrimenti riceverà modifiche db doppie), 
//        2.1 eseguire opportune alterazioni struttura db (in locale le fa invoicex con dbchanges, online le invio dal primo invoicex aggiornato)
//        se invoicex è indietro rispetto al server fare aggiornamento programma prima di eseguire il sync
        //controllo versione locale vs versione online del db
        try {
            List<Map> maxidlog_slave = dbu.getListMap(dbconn, "select max(id_log), id_email from log2 group by id_email");
            DebugUtils.dump(maxidlog_slave);
            List<Map> maxidlog_master = masterGetListMap("select max(id_log), id_email from log2 group by id_email");
            DebugUtils.dump(maxidlog_master);
//TODO 05/060/2015 ero rimasto qui           
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
            return;
        }

        //TODO
        //2.2 eseguire opportune rettifiche su id (ma solo se autoinc, se fosse non autoinc deve sovrascrivere o chiedere): 
        //    se sul master c'è un nuovo record con id = ad un record tra quelli che devo inviare aggiorno il record che devo inviare cambiandolo 
        //    al max(id sul server per questa tabella)+1, per cambiare id devo analizzare le foreign key per andare a cambiare le tabelle collegate 
        //    o farlo fare in automatico alle foreign key
        try {
            String sql = "select distinct type, table_name, primary_value from sync_slave where id_master is not null && sync_date is null";
            List<Map> damaster = dbu.getListMap(dbconn, sql);
            //se uno di questi record è presente negli update da fare sul master devo cambiare gli id
            for (Map recm : damaster) {
                //cerco fra gli update da inviare al master
                sql = "select distinct table_name, primary_value from sync_slave "
                        + " where table_name = " + dbu.sql(recm.get("table_name"))
                        + " and primary_value = " + dbu.sql(recm.get("primary_value"))
                        + " and id_master is null and sync_date is null"
                        + " order by primary_value desc";
                System.out.println("sql = " + sql);
                List<Map> dainviare = dbu.getListMap(dbconn, sql);
                if (dainviare != null && dainviare.size() > 0) {
                    System.out.println("dainviare = " + dainviare);
                    //controllo se campo autoinc
                    //TODO chiedere all'utente
                    String primary_field = getPrimaryField(cu.s(recm.get("table_name")));
                    boolean isnumeric = dbu.isInt(dbconn, cu.s(recm.get("table_name")), primary_field);
                    if (isnumeric) {
                        boolean autoinc = dbu.isAutoInc(dbconn, cu.s(recm.get("table_name")), primary_field);
                        if (autoinc) {
                            //aggiorno la tabella effettiva
                            //quindi prendo id max da master + 1
                            sql = "select IFNULL(max(primary_value),0) as maxid1 from sync_slave where table_name = " + dbu.sql(recm.get("table_name")) + " and id_master is not null && sync_date is null";
                            System.out.println("sql max1 = " + sql);
                            Integer maxid1 = cu.i0(dbu.getObject(dbconn, sql));
                            System.out.println("maxid1 = " + maxid1);
                            //confronto con max id nella tabella effettiva in locale
                            sql = "select IFNULL(max(" + primary_field + "),0) as maxid2 from " + recm.get("table_name");
                            System.out.println("sql max2 = " + sql);
                            Integer maxid2 = cu.i0(dbu.getObject(dbconn, sql));
                            System.out.println("maxid2 = " + maxid2);

                            Integer maxid = Math.max(maxid1, maxid2);
                            System.out.println("maxid = " + maxid);

                            //e sommo quanti record devo modificare su slave (potrebbero esserci più id da inserire
                            //quindi devo modificare dal più alto al più basso
                            maxid += dainviare.size();
                            System.out.println("maxid = " + maxid + " (ho sommato:" + dainviare.size() + ")");
                            //TODO controllare se è una tabella che posso modificare in automatico (vedi doc\sync.sql)
                            for (Map m : dainviare) {
                                try {
                                    dbconn.setAutoCommit(false);
                                    sql = "update " + cu.s(recm.get("table_name")) + " set " + primary_field + " = " + maxid + " where " + primary_field + " = " + m.get("primary_value");
                                    System.out.println("sql tab   = " + sql);
                                    dbu.tryExecQuery(dbconn, sql, false, true);
                                    //aggiorno la sync_slave
                                    sql = "update sync_slave set primary_value = " + maxid + ", debug = 'cambiata pk da [" + m.get("primary_value") + "] a [" + maxid + "]'"
                                            + " where table_name = " + dbu.sql(recm.get("table_name"))
                                            + " and primary_value = " + dbu.sql(m.get("primary_value"))
                                            + " and id_master is null and sync_date is null";
                                    System.out.println("sql slave = " + sql);
                                    dbu.tryExecQuery(dbconn, sql, false, true);
                                    sql = "update sync_slave set new_value = " + maxid
                                            + " where table_name = " + dbu.sql(recm.get("table_name"))
                                            + " and primary_value = " + maxid
                                            + " and id_master is null and sync_date is null";
                                    System.out.println("sql slave = " + sql);
                                    dbu.tryExecQuery(dbconn, sql, false, true);

                                    //cambiare la pk cambiata
                                    //TODO
                                    dbconn.commit();
                                } catch (Exception ex2) {
                                    dbconn.rollback();
                                    ex2.printStackTrace();
                                    SwingUtils.showExceptionMessage(main.getPadreFrame(), ex2);
                                    return;
                                } finally {
                                    dbconn.setAutoCommit(false);
                                }

                                maxid--;
                            }
                        } else {
                            //sovrascrivere o chiedere all'utente
                            //TODO
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SwingUtils.showExceptionMessage(main.getPadreFrame(), ex);
            return;
        }

        //3 eseguire gli aggiornmaenti ricevuti dal master
        //TODO paginare ma problema insert che potrebbe spezzarsi su due pagine...        
        try {
            String sql = "select * from sync_slave where id_master is not null and sync_date is null order by id_master";
            List<Map> list = dbu.getListMap(dbconn, sql);
            for (int i = 0; i < list.size(); i++) {
                Timestamp data_sync = new Timestamp(System.currentTimeMillis());

                Map m = list.get(i);
                String type = cu.s(m.get("type"));

                sql = "";
                String oldkey = null;
                String key = null;
                List<Integer> id_master_to_update = new ArrayList();
                if (type.equalsIgnoreCase("INSERT")) {
                    //leggere per comporre la insert
//TODO cecca: usare date come uuid per ricomporre query di insert (quindi raggruppare per data, tipo, client_id, primay_value, tablename)                    
//anche l'update della sync date farlo per where della group
                    oldkey = cu.s(m.get("type")) + "|" + cu.s(m.get("table_name")) + "|" + cu.s(m.get("primary_value"));
                    System.out.println("entro in ciclo insert " + m.get("id") + " oldkey:" + oldkey);
                    Map rec = new HashMap();
                    while (true) {
                        Map m2 = list.get(i);
                        System.out.println("   ciclo insert i:" + i + " id:" + m2.get("id"));
                        key = cu.s(m2.get("type")) + "|" + cu.s(m2.get("table_name")) + "|" + cu.s(m2.get("primary_value"));
                        if (!oldkey.equalsIgnoreCase(key)) {
                            System.out.println("   ciclo insert i:" + i + " esco per chiave diversa");
                            break;
                        }
                        rec.put(cu.s(m2.get("field_name")), m2.get("new_value"));
                        System.out.println("   ciclo insert i:" + i + " aggiunto a rec " + cu.s(m2.get("field_name")) + ":" + cu.s(m2.get("new_value")));
                        id_master_to_update.add(cu.i(m2.get("id_master")));
                        oldkey = cu.s(m2.get("type")) + "|" + cu.s(m2.get("table_name")) + "|" + cu.s(m2.get("primary_value"));
                        i++;
                        if (i >= list.size()) {
                            System.out.println("   ciclo insert i:" + i + " esco per i >= list.size()");
                            break;
                        }
                    }
                    System.out.println("fine ciclo insert");
                    i--;
                    sql = "insert into " + m.get("table_name") + " set " + dbu.prepareSqlFromMap(rec);
                } else if (type.equalsIgnoreCase("UPDATE")) {
                    Map mupdate = new HashMap();
                    mupdate.put(m.get("field_name"), m.get("new_value"));
                    String pkfield = getPrimaryField(cu.s(m.get("table_name")));
                    String pkvalue = dbu.sql(m.get("primary_value"));
                    sql = "update " + cu.s(m.get("table_name")) + " set " + dbu.prepareSqlFromMap(mupdate) + " where " + pkfield + " = " + pkvalue;
                    id_master_to_update.add(cu.i(m.get("id_master")));
                } else if (type.equalsIgnoreCase("DELETE")) {
                    String pkfield = getPrimaryField(cu.s(m.get("table_name")));
                    String pkvalue = dbu.sql(m.get("primary_value"));
                    sql = "delete from " + cu.s(m.get("table_name")) + " where " + pkfield + " = " + pkvalue;
                    id_master_to_update.add(cu.i(m.get("id_master")));
                }

                String errore = null;
                try {
                    dbconn.setAutoCommit(false);
                    //eseguo istruzione
                    System.out.println("sql = " + sql);
                    dbu.tryExecQuery(dbconn, sql, false, true);
                    //memorizzo che è stata eseguita
                    Map msync = new HashMap();
                    msync.put("sync_date", data_sync);
                    for (Integer id_master : id_master_to_update) {
                        sql = "update sync_slave set " + dbu.prepareSqlFromMap(msync) + " where id_master = " + id_master;
                        System.out.println("sql = " + sql);
                        dbu.tryExecQuery(dbconn, sql, false, true);
                    }
                    dbconn.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    errore = e.toString();
                }
                if (errore != null) {
                    dbconn.rollback();
                    SwingUtils.showErrorMessage(main.getPadreFrame(), errore);
                }
                dbconn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
            return;
        }

        //4 assegnare eventuali numeri di documento rimasti da assegnare sul client (fatture ddt (prevntivi?))
        //TODO
//TODO mettere prima del 3        
        //5 inviare modifiche dal client verso il master
        try {
            h = getHttpClientSync(HttpClientMethod.POST);
            h.q.add(new NameValuePair("c", "Sync"));
            h.q.add(new NameValuePair("m", "masterSync"));
            h.q.add(new NameValuePair("clientId", cu.s(client_id)));
            h.get.setQueryString(h.q.toArray(new NameValuePair[h.q.size()]));
            int retcode = 0;
            String message = null;

//        NameValuePair[] data = {
//          new NameValuePair("user", "joe"),
//          new NameValuePair("password", "bloggs")
//        };
//        post.setRequestBody(data);            
            //conversione da java a php via json
            ArrayList<Map> listdb = DbUtils.getListMap(Db.getConn(), "select * from sync_slave where sync_date is null order by id", true);
            JSONArray list = new JSONArray();
            System.out.println("id che invio a master");
            for (Map rec : listdb) {
                list.add(rec);
                System.out.println("\t " + rec.get("id"));
            }

            h.getPost().addParameter("lista", list.toJSONString());

            System.out.println("post.length():" + h.getPost().getParameters().length);

            retcode = h.httpclient.executeMethod(h.get);

            if (debug) {
                System.out.println("httpClient getURL: status: " + h.get.getStatusLine());
                for (Header hd : h.get.getResponseHeaders()) {
                    System.out.print("httpClient getURL: header: " + hd);
                }
            }

            String xinvoicex = cu.s(h.get.getRequestHeader("X-Invoicex"));
            System.out.println("xinvoicex = " + xinvoicex);

            if (retcode != 200) {
                SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore passaggio 4: " + h.get.getStatusLine());
                return;
            }

            out = h.get.getResponseBodyAsString();
            System.out.println("out = " + out);

            //testo decode della lista di id assegnati dal master per riscrivermeli
            if (out.startsWith("errore:")) {
                SwingUtils.showErrorMessage(main.getPadreFrame(), out);
            } else if (out.startsWith("p:100") || out.equals("Nessun modifica ricevuta dal server")) {
                //niente, non ci sono cambiamenti
                System.out.println("no changes sul master");
            } else {
                try {
                    dbconn.setAutoCommit(false);
                    JSONParser parser = new JSONParser();
                    Object ret = parser.parse(out);
                    System.out.println("ret = " + ret);
                    JSONObject retjsa = (JSONObject) ret;
                    retjsa = (JSONObject) retjsa.get("ids");
                    SortedSet<String> keys = new TreeSet<String>(retjsa.keySet());
                    System.out.println("id ricevuti da master");
                    for (String key : keys) {
                        System.out.println("\t key = " + key + " -> " + retjsa.get(key));
                    }
                    for (String key : keys) {
                        Object value = retjsa.get(key);
                        System.out.println(key + "\t\t\t = " + value);
                        //key è l'id del client e value è l'id assegnato dal master
                        String sql = "update sync_slave set id_master = " + value + ", sync_date = CURRENT_TIMESTAMP where id = " + key;
                        System.out.println("sql = " + sql);
                        dbu.tryExecQuery(dbconn, sql, false, true);
                    }

                    dbconn.commit();

                    SwingUtils.showInfoMessage(main.getPadreFrame(), "Sync finita");
                } catch (ParseException parseex) {
                    dbconn.rollback();
                    parseex.printStackTrace();
                    //SwingUtils.showErrorMessage(main.getPadreFrame(), "<html>" + out + "</html>");
                    JLabel text = new JLabel();
                    text.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    String outhtml = StringUtils.replace(out, "<br />", "<br>");
                    text.setText("<html>" + outhtml + "</html>");
                    JDialog dialog = new JDialog(main.getPadreFrame());
                    dialog.getContentPane().add(text);
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setTitle("Errore dal server");
                    dialog.setVisible(true);
                } catch (Exception e) {
                    dbconn.rollback();
                    e.printStackTrace();
                    SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
                }
                dbconn.setAutoCommit(true);
            }

            /*
             if (!errors) {
                
             //memorizzo che sul server è stato eseguito il sync
             for (Map m : listdb) {
             System.out.println("m = " + m);
             String sql = "update sync_slave set ...";
             }
                
             SwingUtils.inEdt(new Runnable() {
             public void run() {
             System.out.println("aggiornamento completato");
             }
             });
             } else {
                
             NativeInterface.open();
                
             JFrame frame = new JFrame("Browser");
             JPanel webBrowserPanel = new JPanel(new BorderLayout());
             webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
             final JWebBrowser webBrowser = new JWebBrowser();
             //webBrowser.navigate("http://www.google.com");
                
             webBrowser.setHTMLContent(out);
                
             webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
             frame.add(webBrowserPanel, BorderLayout.CENTER);
             JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
             JCheckBox menuBarCheckBox = new JCheckBox("Menu Bar", webBrowser.isMenuBarVisible());
             menuBarCheckBox.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent e) {
             webBrowser.setMenuBarVisible(e.getStateChange() == ItemEvent.SELECTED);
             }
             });
             buttonPanel.add(menuBarCheckBox);
             frame.add(buttonPanel, BorderLayout.SOUTH);
             frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
             frame.setVisible(true);
                
             Thread t = new Thread("djnative") {

             @Override
             public void run() {
             NativeInterface.runEventPump();
             }
                    
             };
             t.start();
                

             }
             */
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            h.get.releaseConnection();
        }
    }

    //dbutils
    //per sync
    public static List<Map> getDifferences(Map listmm_pre, Map listmm_post) {
        List ret = new ArrayList();

        //ciclo 
        for (Object k : listmm_pre.keySet()) {
            //vedo quali mancano dopo per capire chi eliminare
            if (!listmm_post.keySet().contains(k)) {
                Map m = new HashMap();
                m.put("type", DELETE);
                m.put("k", k);
                ret.add(m);
            } else {
                //presente ancora, confornto campo per campo
                Map mpre = (Map) listmm_pre.get(k);
                Map mpost = (Map) listmm_post.get(k);

                //raggruppa i campi modificati
                Map m = new HashMap();
                Map post = new HashMap();
                m.put("type", UPDATE);
                m.put("k", k);
                for (Object fk : mpre.keySet()) {
                    Object opre = mpre.get(fk);
                    Object opost = mpost.get(fk);
                    if ((opre != null && opost == null) || (opre == null && opost != null) || (opre != null && opost != null && !opre.equals(opost))) {
                        if (!m.containsKey("list")) {
                            m.put("list", new ArrayList());
                        }
                        Map mf = new HashMap();
                        mf.put("fk", fk);
                        mf.put("pre", opre);
                        mf.put("post", opost);
                        ((ArrayList) m.get("list")).add(mf);
                        post.put(fk, opost);
                    }
                }

                if (m.containsKey("list") && m.get("list") instanceof ArrayList && ((ArrayList) m.get("list")).size() > 0) {
                    m.put("post", post);
                    ret.add(m);
                }

            }
        }

        //secondo ciclo per vedere quali inserire
        for (Object k : listmm_post.keySet()) {
            if (!listmm_pre.keySet().contains(k)) {
                Map m = new HashMap();
                m.put("type", INSERT);
                m.put("k", k);
                m.put("post", listmm_post.get(k));
                ret.add(m);
            }
        }

        return ret;
    }

    public static void execDifferences(Connection conn, List<Map> diffs, String tab) throws Exception {
        execDifferences(conn, diffs, tab, false);
    }

    public static void execDifferences(Connection conn, List<Map> diffs, String tab, boolean remove_pk_insert) throws Exception {
        for (Map rec : diffs) {
            String sql = "";
            String type = cu.s(rec.get("type"));
            System.out.println("type:" + type + " tab:" + tab + " rec:" + rec);
            if (type.equals(DELETE)) {
                exec(conn, type, tab, rec);
            } else if (type.equals(UPDATE)) {
                exec(conn, type, tab, rec);
            } else if (type.equals(INSERT)) {
                if (remove_pk_insert) {
                    ((Map) rec.get("post")).remove(getPrimaryField(tab));
                }
                exec(conn, type, tab, rec);
            }
        }
    }

    public static void exec(Connection conn, String type, String tab, Map m) throws Exception {
        System.out.println("exec m = " + m);
        String primary_field = getPrimaryField(tab);
        String sql = "";
        if (type.equals(DELETE)) {
            sql = "delete from " + tab + " where " + primary_field + " = " + dbu.sql(m.get("k"));
        } else if (type.equals(UPDATE)) {
            sql = "update " + tab + " set " + dbu.prepareSqlFromMap((Map) m.get("post")) + " where " + primary_field + " = " + dbu.sql(m.get("k"));
        } else if (type.equals(INSERT)) {
            sql = "insert into " + tab + " set " + dbu.prepareSqlFromMap((Map) m.get("post"));
        } else {
            throw new Exception("exec tipo non supportato: " + type);
        }
        System.out.println("sql = " + sql);

        if (Sync.isActive()) {
            conn.setAutoCommit(false);
        }

        dbu.tryExecQuery(conn, sql, false, true);

        if (!Sync.isActive()) {
            return;
        }

        Object insk = null;
        if (type.equals(INSERT)) {
            if (m.get("pk") != null) {
                insk = m.get("pk");
                System.out.println("Sync exec type:" + type + " tab:" + tab + " pk:" + insk);
            } else {
                insk = cu.i(dbu.getObject(conn, "SELECT LAST_INSERT_ID()"));
                System.out.println("Sync exec type:" + type + " tab:" + tab + " LAST_INSERT_ID:" + insk);
                m.put("k_new_id", insk);
                m.put("k_old_id", m.get("k"));
            }
            m.put("k", insk);
            ((Map) m.get("post")).put(primary_field, insk);
        }

        if (type.equals(DELETE)) {
            if (!Sync.add(conn, type, tab, null, m.get("k"), null, null)) {
                conn.rollback();
                return;
            }
        } else if (type.equals(UPDATE)) {
            List<Map> list = (List<Map>) m.get("list");
            HashMap mpost = new HashMap();
            HashMap mpre = new HashMap();
            for (Map m2 : list) {
                mpost.put(m2.get("fk"), m2.get("post"));
                mpre.put(m2.get("fk"), m2.get("pre"));
            }
            if (!Sync.add(conn, type, tab, null, m.get("k"), mpre, mpost)) {
                conn.rollback();
                return;
            }
        } else if (type.equals(INSERT)) {
            if (!Sync.add(conn, type, tab, null, m.get("k"), null, (Map) m.get("post"))) {
                conn.rollback();
                return;
            }
        }

        conn.commit();

        conn.setAutoCommit(true);
    }

    public static String geturl() {
        return geturl(null, null);
    }

    public static String geturl(String _class, String method) {
        //return "https://demo.tnx.it/invoicex/index.php?p=wsd_sync&f=" + func;
        //return "http://www.demo.tnx.it/invoicex_rest/?c=sync&m=" + func;
//        return "http://www.demo.tnx.it/invoicex_rest/?c=" + _class + "&m=" + method;        

        String url = "http://www.demo.tnx.it/invoicex_rest/";
        if (server != null && server.equals("due.tnx.it")) {
            url = "http://server.invoicex.it/invoicex_rest/";
        } else if (server != null && server.equals("tre.tnx.it")) {
            url = "http://tre.tnx.it/invoicex_rest/";
        }

        if (_class != null) {
            return url + "?c=" + _class + "&m=" + method;
        } else {
            return url;
        }
    }

    public static Integer presentaRegistrazioneClient() {
        //inviare richiesta di registrazione al master
        JFrame dummyframe = new JFrame();
        dummyframe.setIconImage(main.getLogoIcon());
        JDialogRegistraCient dialog = new JDialogRegistraCient(dummyframe, true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        if (dialog.esito) {
            return dialog.idclient;
        }
        return null;
    }

    public static void inittabpk(Connection conn) throws Exception {
        tablespk = new HashMap();
        tablesinfo = new HashMap();
        ArrayList tables = dbu.getList(conn, "show full tables where Table_type = 'BASE TABLE'");
        for (Object t : tables) {
            boolean ignore = false;
            if (!Sync.isActive()
                    && (!cu.s(t).equalsIgnoreCase("clie_forn")
                    && !cu.s(t).equalsIgnoreCase("clie_forn_contatti")
                    && !cu.s(t).equalsIgnoreCase("articoli")
                    && !cu.s(t).equalsIgnoreCase("articoli_prezzi")
                    && !cu.s(t).equalsIgnoreCase("pacchetti_articoli")
                    && !cu.s(t).startsWith("righ_")
                    && !cu.s(t).startsWith("test_")
                    && !cu.s(t).startsWith("doc_ricorrenti"))) {
                ignore = true;
            }

            if (!ignore) {
                List<Map> fields = dbu.getListMap(conn, "SHOW FIELDS FROM " + cu.s(t));
                Map mt = new HashMap();
                mt.put("fields", fields);
                for (Map f : fields) {
                    if (cu.s(f.get("Extra")).equalsIgnoreCase("auto_increment")) {
                        mt.put("auto_inc", true);
                        break;
                    }
                }
                if (!mt.containsKey("auto_inc")) {
                    mt.put("auto_inc", false);
                }
                tablesinfo.put(t, mt);

                List<Map> keys = dbu.getListMap(conn, "SHOW KEYS FROM " + cu.s(t) + " WHERE Key_name = 'PRIMARY'");
                if (keys.size() == 1) {
                    tablespk.put(cu.s(t), keys.get(0).get("Column_name"));
                } else {
                    tablespk.put(cu.s(t), keys);
                }
            }
        }

    }

    public static String getPk(Connection conn, String tab) throws Exception {
        if (tablespk == null) {
            inittabpk(conn);
        }
        Object ret = tablespk.get(tab);
        if (ret instanceof String) {
            return cu.s(ret);
        } else {
            throw new Exception("InvoicexUtil.getPk pk non singola (tab=" + tab + ")");
        }
    }

    public static boolean isAutoInc(Connection conn, String tab) throws Exception {
        if (tablespk == null) {
            inittabpk(conn);
        }
        Object ret = ((Map) tablesinfo.get(tab)).get("auto_inc");
        if (ret instanceof Boolean) {
            return (Boolean) ret;
        } else {
            throw new Exception("InvoicexUtil.isAutoInc errore (tab=" + tab + ")");
        }
    }

    public static class HttpClientSync {

        HttpClient httpclient;
        HttpMethodBase get;
        List<NameValuePair> q;

        public void addParam(String name, String value) {
            q.add(new NameValuePair(name, value));
        }

        public int execGet() throws IOException {
            get.setQueryString(q.toArray(new NameValuePair[q.size()]));
            return httpclient.executeMethod(get);
        }

        public PostMethod getPost() {
            return (PostMethod) get;
        }
    }

    public static enum HttpClientMethod {
        GET, POST
    }

    private static HttpClientSync getHttpClientSync() {
        return getHttpClientSync(HttpClientMethod.GET);
    }

    private static HttpClientSync getHttpClientSync(HttpClientMethod method) {
        HttpClientSync ret = new HttpClientSync();

        HttpClient httpclient = getHttpClient();
        ret.httpclient = httpclient;

        HttpMethodBase get = null;

        if (method == HttpClientMethod.GET) {
            get = new GetMethod(geturl());
        } else {
            get = new PostMethod(geturl());
        }
        get.addRequestHeader("User-Agent", "Invoicex/" + main.version + "(" + main.build + ")");
        get.addRequestHeader("Invoicex", password);    //<--- password del database
//        get.addRequestHeader("Content-Type", "text/html; charset=UTF-8");

        ret.get = get;

        List<NameValuePair> q = new ArrayList();
        q.add(new NameValuePair("db", nomedb));
        q.add(new NameValuePair("dbuser", username));
        ret.q = q;

        return ret;
    }

    public static boolean checkLogin(String server, String nomedb, String username, String password) {
        Sync.server = server;
        Sync.nomedb = nomedb;
        Sync.username = username;
        Sync.password = password;

        String url = geturl();
        HttpClientSync h = getHttpClientSync();
        h.q.add(new NameValuePair("c", "Dump"));
        h.q.add(new NameValuePair("m", "checkLogin"));
        h.q.add(new NameValuePair("clientId", cu.s(client_id)));
        h.get.setQueryString(h.q.toArray(new NameValuePair[h.q.size()]));
        int retcode = 0;
        String message = null;
        try {
            System.out.println("get = " + h.get);
            System.out.println("url = " + url + "?" + h.get.getQueryString());
            retcode = h.httpclient.executeMethod(h.get);
            if (debug) {
                System.out.println("httpClient getURL: status: " + h.get.getStatusLine());
                for (Header hd : h.get.getResponseHeaders()) {
                    System.out.print("httpClient getURL: header: " + hd);
                }
            }
            retcode = h.get.getStatusLine().getStatusCode();
            message = h.get.getStatusLine().getReasonPhrase();
            String out = h.get.getResponseBodyAsString();
            System.out.println("out = " + out);

            if (retcode == 200 && out.equalsIgnoreCase("true")) {
                //memorizzo dati di accesso
                return true;
            } else {
                SwingUtils.showErrorMessage(main.getPadreFrame(), out, "Sync", true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            h.get.releaseConnection();
        }
        return false;
    }

    public static String registraClient(String clientName) throws IOException {
        String url = geturl();

        String out = "";
        HttpClientSync h = getHttpClientSync();
        h.addParam("c", "dump");
        h.addParam("m", "registraClient");
        h.addParam("client_name", clientName);
        h.addParam("os", System.getProperty("os.name"));
        h.addParam("os_version", System.getProperty("os.version"));
        h.addParam("model", SystemUtils.getHostname());
        h.addParam("os_username", System.getProperty("user.name"));
        h.addParam("invoicex_version", main.version + " " + main.build);
        h.addParam("invoicex_licenza", main.versione);

        int retcode = 0;
        String message = null;

        System.out.println("get = " + h.get);

        retcode = h.execGet();
        if (debug) {
            System.out.println("httpClient getURL: status: " + h.get.getStatusLine());
            for (Header hd : h.get.getResponseHeaders()) {
                System.out.print("httpClient getURL: header: " + hd);
            }
        }
        retcode = h.get.getStatusLine().getStatusCode();
        message = h.get.getStatusLine().getReasonPhrase();
        out = h.get.getResponseBodyAsString();

        if (Sync.verbose) {
            System.out.println("registra client out = " + out);
        }

        return out;
    }

    public static String scaricaDump() {
        //http://www.demo.tnx.it/invoicex_rest/ ? db=inv_test_sync_master & c=dump & type=mysql

        HttpClientSync h = getHttpClientSync();
        h.addParam("c", "Dump");
        h.addParam("type", "mysql");

        int retcode = 0;
        String message = null;
        try {
            retcode = h.execGet();
            System.out.println("url = " + h.get.getURI() + "?" + h.get.getQueryString());
            if (debug) {
                System.out.println("httpClient getURL: status: " + h.get.getStatusLine());
                for (Header hd : h.get.getResponseHeaders()) {
                    System.out.print("httpClient getURL: header: " + hd);
                }
            }
            retcode = h.get.getStatusLine().getStatusCode();
            message = h.get.getStatusLine().getReasonPhrase();
            System.out.println("retcode = " + retcode);
            System.out.println("message = " + message);

            if (retcode == 200) {
                InputStream ins = h.get.getResponseBodyAsStream();
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd_HHmmss");
                Date d = new Date();
                File ftmp = new File(System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "tmp");
                try {
                    ftmp.mkdir();
                } catch (Exception e) {
                }
                String filenamedump = System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "tmp" + File.separator + "dump_sync_master_" + sdf1.format(d) + ".txt";
                System.out.println("filenamedump = " + filenamedump);
                OutputStream outs = new FileOutputStream(filenamedump);
                IOUtils.copyLarge(ins, outs);
                ins.close();
                outs.close();
                return filenamedump;
            } else {
                throw new Exception("Errore nel recupero del dump dal sync master\n" + "Retcode:" + retcode);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            h.get.releaseConnection();
        }
        return null;
    }

    private static List<Map> masterGetListMap(String sql) {
        String url = geturl();
        HttpClientSync h = null;
        try {
            String out = "";

            h = getHttpClientSync();
            h.q.add(new NameValuePair("c", "Sync"));
            h.q.add(new NameValuePair("m", "syncSql"));
            h.q.add(new NameValuePair("sql", sql));
            h.q.add(new NameValuePair("clientId", cu.s(client_id)));

            h.get.setQueryString(h.q.toArray(new NameValuePair[h.q.size()]));

            int retcode = 0;
            String message = null;
            try {
                System.out.println("url = " + url + "?" + h.get.getQueryString());
                retcode = h.httpclient.executeMethod(h.get);
                if (debug) {
                    System.out.println("httpClient getURL: status: " + h.get.getStatusLine());
                    for (Header hd : h.get.getResponseHeaders()) {
                        System.out.print("httpClient getURL: header: " + hd);
                    }
                }
                retcode = h.get.getStatusLine().getStatusCode();
                message = h.get.getStatusLine().getReasonPhrase();
                out = h.get.getResponseBodyAsString();
                System.out.println("out = " + out);
            } catch (Exception ex) {
                throw ex;
            } finally {
                h.get.releaseConnection();
            }

            String xinvoicex = cu.s(h.get.getResponseHeader("X-Invoicex"));
            System.out.println("xinvoicex = " + xinvoicex);

            if (retcode != 200) {
                System.out.println("getURL: errore retcode:" + retcode);
                return null;
            }

            //analizzo risposta ed inserisco in sync_slave
            if (out != null && out.trim().length() > 0) {
                //provo a json decodare
                JSONParser parser = new JSONParser();
                Object ret = parser.parse(out);
                System.out.println("ret = " + ret);
                JSONArray retjsa = (JSONArray) ret;
                return retjsa;
            }
        } catch (Exception ex) {
            SwingUtils.showExceptionMessage(main.getPadreFrame(), ex);
        }
        return null;
    }

    private static Object masterGetObject(String sql) {
        String url = geturl();
        HttpClientSync h = null;
        try {
            String out = "";

            h = getHttpClientSync();
            h.q.add(new NameValuePair("c", "Sync"));
            h.q.add(new NameValuePair("m", "syncSql"));
            h.q.add(new NameValuePair("sql", sql));
            h.q.add(new NameValuePair("clientId", cu.s(client_id)));
            h.get.setQueryString(h.q.toArray(new NameValuePair[h.q.size()]));

            int retcode = 0;
            String message = null;
            try {
                System.out.println("url = " + url + "?" + h.get.getQueryString());
                retcode = h.httpclient.executeMethod(h.get);
                if (debug) {
                    System.out.println("httpClient getURL: status: " + h.get.getStatusLine());
                    for (Header hd : h.get.getResponseHeaders()) {
                        System.out.print("httpClient getURL: header: " + hd);
                    }
                }
                retcode = h.get.getStatusLine().getStatusCode();
                message = h.get.getStatusLine().getReasonPhrase();
                out = h.get.getResponseBodyAsString();
                System.out.println("out = " + out);
            } catch (Exception ex) {
                throw ex;
            } finally {
                h.get.releaseConnection();
            }

            String xinvoicex = cu.s(h.get.getResponseHeader("X-Invoicex"));
            System.out.println("xinvoicex = " + xinvoicex);

            if (retcode != 200) {
                System.out.println("getURL: errore retcode:" + retcode);
                return null;
            }

            //analizzo risposta ed inserisco in sync_slave
            if (out != null && out.trim().length() > 0) {
                //provo a json decodare
                JSONParser parser = new JSONParser();
                Object ret = parser.parse(out);
                System.out.println("ret = " + ret);
                JSONArray retjsa = (JSONArray) ret;
                for (Object objn : retjsa) {
                    System.out.println("objn = " + objn);
                    Map m = (Map) objn;
                    System.out.println("m = " + m);

                }
            }
        } catch (Exception ex) {
            SwingUtils.showExceptionMessage(main.getPadreFrame(), ex);
        }
        return null;
    }

    public static boolean isQueryToSync(String sql) {
        boolean ignoreSync = false;
        if (Sync.isActive()) {
            StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
            for (StackTraceElement e : stacks) {
                if (e.getClassName().equals("gestioneFatture.DbChanges2")
                        || e.getClassName().equals("gestioneFatture.DbChanges")
                        || e.getClassName().equals("it.tnx.invoicex.gui.JFrameWizardDb")
                        || e.getClassName().equals("invoicexplugininvoicex.DbChanges2PluginInvoicex")) {
                    ignoreSync = true;
                    break;
                }
            }

            //controllo tabelle da ignorare
            if (!ignoreSync) {
                String tab = "";
                if (sql.toLowerCase().startsWith("insert")) {
                    tab = StringUtils.substringBefore(StringUtils.substringAfter(sql.toLowerCase(), " into "), " ");
                } else if (sql.toLowerCase().startsWith("update")) {
                    tab = StringUtils.substringBefore(StringUtils.substringAfter(sql.toLowerCase(), "update "), " ");
                } else if (sql.toLowerCase().startsWith("replace")) {
                    tab = StringUtils.substringBefore(StringUtils.substringAfter(sql.toLowerCase(), "replace "), " ");
                } else if (sql.toLowerCase().startsWith("drop table if exists")) {
                    tab = StringUtils.substringBefore(StringUtils.substringAfter(sql.toLowerCase(), "drop table if exists "), " ");
                } else if (sql.toLowerCase().startsWith("drop table")) {
                    tab = StringUtils.substringBefore(StringUtils.substringAfter(sql.toLowerCase(), "drop table "), " ");
                } else {
                    tab = StringUtils.substringBefore(StringUtils.substringAfter(sql.toLowerCase(), "from "), " ");
                }
                tab = tab.toLowerCase();
                if (tab.equalsIgnoreCase("storico")
                        || tab.equalsIgnoreCase("stampa_iva_semplice")
                        || tab.equalsIgnoreCase("movimenti_magazzino_eliminati")
                        || tab.equalsIgnoreCase("pn_temp_bilancio")
                        || tab.equalsIgnoreCase("scadenze_sel")
                        || tab.equalsIgnoreCase("temp_stampa_stat_ord_bol_fat")
                        || tab.equalsIgnoreCase("certificati")
                        || tab.equalsIgnoreCase("certificati_acq")
                        || tab.startsWith("sync_")
                        || tab.startsWith("log")
                        || tab.endsWith("_temp")
                        || tab.startsWith("temp_")
                        || tab.indexOf("_temp_") >= 0
                        || tab.equalsIgnoreCase("accessi_utenti_online")) {
                    ignoreSync = true;
                }
            }
        }

        if (Sync.isActive() && !ignoreSync && sql != null && (sql.toLowerCase().startsWith("delete")
                || sql.toLowerCase().startsWith("update")
                || sql.toLowerCase().startsWith("insert")
                || sql.toLowerCase().startsWith("replace")
                || sql.toLowerCase().startsWith("drop")
                || sql.toLowerCase().startsWith("truncate")
                || sql.toLowerCase().startsWith("alter"))) {
            return true;
        }

        return false;
    }

    public static class EsitoExecQuery {

        public boolean esito;
        public Integer last_id;
        public Integer update_count;
    }

    public static EsitoExecQuery execQueryToSync(Connection conn, String sql, boolean showexc, boolean throwsexc) throws Exception {
        Statement stat = null;
        EsitoExecQuery esito = new EsitoExecQuery();

        boolean trans_esterna = false;  //se true la transazione è gestita esternnamente, vedi movimenti
        if (!conn.getAutoCommit()) {
            trans_esterna = true;
        }

        boolean ok = true;
        try {
            if (sql.toLowerCase().startsWith("delete")) {
                //faccio la select e poi delete per id
                if (sql.indexOf(" join ") >= 0) {
                    ok = false;
                }

                if (ok) {
                    //delete from articoli_prezzi where articolo = '' or articolo is null   
                    String tab = StringUtils.substringBefore(StringUtils.substringAfter(sql.toLowerCase(), "from "), " ");
                    String dopotab = StringUtils.substringAfter(StringUtils.substringAfter(sql.toLowerCase(), "from "), " ");
                    String pkfield = Sync.getPk(conn, tab);
                    String sqlsel = "select " + pkfield + " from " + tab + " " + dopotab;
                    //fare la select ed eliminare per id
                    ArrayList list = DbUtils.getList(conn, sqlsel);
                    if (!trans_esterna) {
                        conn.setAutoCommit(false);
                    }
                    for (Object id : list) {
                        if (Sync.add(conn, Sync.DELETE, tab, null, id, null, null)) {
                            String sqldel = "delete from " + tab + " where " + pkfield + " = " + dbu.sql(id);
                            dbu.tryExecQuery(conn, sql, false, true);
                        } else {
                            if (!trans_esterna) {
                                conn.rollback();
                            }
                            throw new Exception("Errore sync");
                        }
                    }
                    if (!trans_esterna) {
                        conn.commit();
                    }
                }
            } else if (sql.toLowerCase().startsWith("insert")) {
                if (sql.indexOf(" from ") >= 0) {
                    ok = false;
                }
                if (ok) {
                    //fare insert, prendere id nuovo e mettere nella sync
                    String tab = StringUtils.substringBefore(StringUtils.substringAfter(sql.toLowerCase(), " into "), " ");
                    String pkfield = Sync.getPk(conn, tab);

                    if (!trans_esterna) {
                        conn.setAutoCommit(false);
                    }

                    dbu.tryExecQuery(conn, sql, false, true);
                    String pkvalue = null;
                    if (isAutoInc(conn, tab)) {
                        esito.last_id = cu.i(dbu.getObject(conn, "SELECT LAST_INSERT_ID()", true));
                        pkvalue = dbu.sql(esito.last_id);
                    } else {
                        pkvalue = getFieldValueFromSql(sql, pkfield);
                    }
                    Map newmap = null;
                    try {
                        newmap = dbu.getListMap(conn, "select * from " + tab + " where " + pkfield + " = " + pkvalue).get(0);
                    } catch (Exception e) {
                        if (!trans_esterna) {
                            conn.rollback();
                        }
                        throw new Exception("Errore sync (non trovato record inserito)");
                    }
                    if (pkvalue.startsWith("'") && pkvalue.endsWith("'") || pkvalue.startsWith("(") && pkvalue.endsWith(")")) {
                        pkvalue = pkvalue.substring(1, pkvalue.length() - 1);
                    }
                    if (!Sync.add(conn, Sync.INSERT, tab, null, pkvalue, null, newmap)) {
                        if (!trans_esterna) {
                            conn.rollback();
                        }
                        throw new Exception("Errore sync (errore in Sync.add)");
                    }
                    if (!trans_esterna) {
                        conn.commit();
                    }
                }
            } else if (sql.toLowerCase().startsWith("update")) {
                if (sql.indexOf(" join ") >= 0) {
                    ok = false;
                }
                if (ok) {
                    //fare update prendere id e mettere nella sync
                    String tab = StringUtils.substringBefore(StringUtils.substringAfter(sql, " ").toLowerCase(), " ");

                    String alias = StringUtils.substringAfter(StringUtils.substringBefore(sql.toLowerCase(), " set").toLowerCase(), "update ").trim();
                    if (alias.indexOf(" ") > 0) {
                        alias = StringUtils.substringAfter(alias, " ");
                    } else {
                        alias = "";
                    }

                    String pkfield = Sync.getPk(conn, tab);

                    String pkvalue = null;
                    String pkvaluepart = StringUtils.substringAfterLast(sql.toLowerCase(), "where " + pkfield).trim();
                    String sql2 = "";
                    //TODO migliorare analisi della where
                    if (pkvaluepart.startsWith("=") && (pkvaluepart.indexOf(" and ") < 0 && pkvaluepart.indexOf(" or ") < 0)) {
                        pkvalue = StringUtils.substringAfterLast(pkvaluepart, "=").trim();
                        if (tab.equalsIgnoreCase("dati_azienda")) {
                            sql2 = "select * from " + tab;
                        } else {
                            sql2 = "select * from " + tab + " where " + pkfield + " = " + pkvalue;
                        }
                        if (verbose) {
                            System.out.println("SYNC query di select per update: " + sql2);
                        }
                        Map oldmap = dbu.getListMap(conn, sql2).get(0);
                        if (!trans_esterna) {
                            conn.setAutoCommit(false);
                        }
                        esito.update_count = dbu.tryExecQueryWithResult(conn, sql, true);
                        Map newmap = dbu.getListMap(conn, sql2).get(0);
                        if (pkvalue.startsWith("'") && pkvalue.endsWith("'") || pkvalue.startsWith("(") && pkvalue.endsWith(")")) {
                            pkvalue = pkvalue.substring(1, pkvalue.length() - 1);
                        }
                        if (!Sync.add(conn, Sync.UPDATE, tab, null, pkvalue, oldmap, newmap)) {
                            if (!trans_esterna) {
                                conn.rollback();
                            }
                            throw new Exception("Errore sync");
                        }
                        if (!trans_esterna) {
                            conn.commit();
                        }
                    } else {
                        //prendere la where, selezionare gli id e fare update con ciclo
                        String sqlwhere = null;
                        String sqlset = null;
                        try {
                            CCJSqlParserManager pm = new CCJSqlParserManager();
                            net.sf.jsqlparser.statement.update.Update update = (net.sf.jsqlparser.statement.update.Update) pm.parse(new StringReader(sql));
                            sqlwhere = update.getWhere().toString();
                            sqlset = sql.substring(sql.toLowerCase().indexOf("set ") + 4);
                            sqlset = sqlset.substring(0, sqlset.toLowerCase().indexOf("where " + sqlwhere.toLowerCase()));
                        } catch (Exception e) {
                            sqlwhere = sql.substring(sql.toLowerCase().lastIndexOf("where ") + 6);
                            sqlset = sql.substring(sql.toLowerCase().indexOf("set ") + 4);
                            sqlset = sqlset.substring(0, sqlset.toLowerCase().lastIndexOf("where "));
                        }

                        String sqlsel = "select " + pkfield + " from " + tab + " " + alias;
                        if (StringUtils.isNotBlank(sqlwhere)) {
                            sqlsel += " where " + sqlwhere;
                        }
                        if (verbose) {
                            System.out.println("SYNC query di select per update: " + sqlsel);
                        }
                        //fare la select ed eseguire le update
                        ArrayList list = DbUtils.getList(conn, sqlsel);
                        if (!trans_esterna) {
                            conn.setAutoCommit(false);
                        }
                        for (Object id : list) {
                            String sqlold = "select * from " + tab + " where " + pkfield + " = " + dbu.sql(id);
                            Map oldmap = dbu.getListMap(conn, sqlold).get(0);
                            String sqlupdate = "update " + tab + " set " + sqlset + " where " + pkfield + " = " + dbu.sql(id);
                            Integer ucount = dbu.tryExecQueryWithResult(conn, sqlupdate, true);
                            Map newmap = dbu.getListMap(conn, sqlold).get(0);
                            if (ucount != null) {
                                if (Sync.add(conn, Sync.UPDATE, tab, null, id, oldmap, newmap)) {
                                    if (esito.update_count == null) {
                                        esito.update_count = 0;
                                    }
                                    esito.update_count += ucount;
                                } else {
                                    if (!trans_esterna) {
                                        conn.rollback();
                                    }
                                    throw new Exception("Errore sync");
                                }
                            }
                        }
                        if (!trans_esterna) {
                            conn.commit();
                        }
                    }

                }
            } else {
                ok = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (showexc) {
                SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
            }
            ok = false;
            try {
                if (!trans_esterna) {
                    if (!conn.getAutoCommit()) {
                        conn.rollback();
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            //se è un errore di sintassi faccio vedere l'errore
            if (throwsexc && !e.getClass().getName().endsWith("MySQLSyntaxErrorException")) {
                throw e;
            }
        } finally {
            if (!trans_esterna) {
                conn.setAutoCommit(true);
            }
        }

        if (!ok) {
            System.err.println("ERR SYNC: query non gestita\n" + sql);
            Thread.dumpStack();
            SwingUtils.showErrorMessage(main.getPadreFrame(), "Sync: query non gestita\n" + StringUtils.abbreviate(sql, 100), "TODO Sync", true);
            if (throwsexc) {
                throw new Exception("Sync: query non gestita\n" + StringUtils.abbreviate(sql, 100));
            } else {
                esito.esito = false;
                return esito;
            }
        } else {
            esito.esito = true;
            return esito;
        }
    }

    private static String getFieldValueFromSql(String sql, String field) {
        if (sql.toLowerCase().startsWith("insert ") && sql.toLowerCase().indexOf(" set ") < 0) {
            //campi e valori separati
            Thread.dumpStack();
            SwingUtils.showErrorMessage(main.getPadreFrame(), "TODO getfieldvaluefromsql", true);
        } else {
            String[] splits = splitSql(sql);
            System.out.println("splits:" + splits);
            for (String split : splits) {
                split = StringUtils.replace(split, "`", "");
                String pre = StringUtils.substringBefore(split, "=").trim();
                if (pre.indexOf(" ") >= 0) {
                    pre = StringUtils.substringAfterLast(pre, " ").trim();
                }
                System.out.println("pre = " + pre);
                String post = StringUtils.substringAfter(split, "=").trim();
                System.out.println("post = " + post);
                if (pre.equalsIgnoreCase(field)) {
                    return post;
                }
            }
        }
        return null;
    }

    public static String[] splitSql(String sql) {
        String otherThanQuote = " [^\"] ";
        String quotedString = String.format(" \" %s* \" ", otherThanQuote);
        String regex = String.format("(?x) "
                + // enable comments, ignore white spaces
                ",                         "
                + // match a comma
                "(?=                       "
                + // start positive look ahead
                "  (                       "
                + //   start group 1
                "    %s*                   "
                + //     match 'otherThanQuote' zero or more times
                "    %s                    "
                + //     match 'quotedString'
                "  )*                      "
                + //   end group 1 and repeat it zero or more times
                "  %s*                     "
                + //   match 'otherThanQuote'
                "  $                       "
                + // match the end of the string
                ")                         ", // stop positive look ahead
                otherThanQuote, quotedString, otherThanQuote);

        String[] tokens = sql.split(regex, -1);
        for (String t : tokens) {
            System.out.println("> " + t);
        }
        return tokens;
    }

    public static boolean saveDoc(String suff, String serie, String numero, String anno, Integer id,
            JInternalFrame parent, tnxDbPanel dati, String table_righe_temp, String table_righe_lotti_temp, String table_righe_matricole_temp) {
        String sql;

        Connection conn = Db.getConn();

        String table_righe = StringUtils.substringBeforeLast(table_righe_temp, "_temp_");
        String table_righe_lotti = StringUtils.substringBeforeLast(table_righe_lotti_temp, "_temp_");
        String table_righe_matricole = null;
        if (table_righe_matricole_temp != null) {
            table_righe_matricole = StringUtils.substringBeforeLast(table_righe_matricole_temp, "_temp_");
        }

        //controllo che non sia già inserito numero e anno
        sql = "select numero from " + dati.dbNomeTabella;
        sql += " where serie = " + dbu.sql(serie);
        sql += " and numero = " + dbu.sql(numero);
        sql += " and anno = " + dbu.sql(anno);
        sql += " and id != " + dbu.sql(id);
        System.out.println("sql per controllo numero/serie/anno:" + sql);
        try {
            Object numerodb = dbu.getObject(conn, sql, false);
            if (numerodb != null) {
                SwingUtils.showErrorMessage(parent, "Il documento " + serie + "" + numero + " del " + anno + " è già presente, impossibile continuare !");
                return false;
            }
            boolean retsave = dati.dbSave();
            if (id == -1) {
                id = dati.id;
                //aggiorno id_padre in temp matricole
                if (table_righe_matricole_temp != null) {
                    dbu.tryExecQuery(conn, "update " + table_righe_matricole_temp + " set id_padre = " + id, false, true);
                }
            }
            if (retsave) {
                //forzo serie numero e anno su quelle temp prima di portarle nel definitivo
                sql = "update " + table_righe_temp + " "
                        + " set serie = " + dbu.sql(serie) + ""
                        + ", numero = " + dbu.sql(numero)
                        + ", anno = " + dbu.sql(anno)
                        + ", id_padre = " + dbu.sql(id);
                System.out.println("sql per forzare righ temp = " + sql);
                dbu.tryExecQuery(conn, sql);

                //memorizzo righe precedenti
                HashMap righe_pre = null;
                HashMap righe_pre_lotti = null;
                HashMap righe_pre_matricole = null;
                if (Sync.isActive()) {
                    righe_pre = dbu.getListMapMap(conn, "select * from " + table_righe + " where id_padre = " + id, "id");
                    righe_pre_lotti = dbu.getListMapMap(conn, "select * from " + table_righe_lotti + " where id_padre in (select id from " + table_righe + " where id_padre = " + id + ")", "id");
                    if (table_righe_matricole != null) {
                        righe_pre_matricole = dbu.getListMapMap(conn, "select * from " + table_righe_matricole + " where id_padre = " + id, "id");
                    }
                }

                //memorizzo post
                //righe
                HashMap righe_post = dbu.getListMapMap(conn, "select * from " + table_righe_temp + " where id_padre = " + id, "id");
                List<Map> diffs = Sync.getDifferences(righe_pre, righe_post);
                System.out.println("diffs = " + diffs);
                Sync.execDifferences(it.tnx.Db.getConn(), diffs, table_righe, true);

                //righe lotti (id_padre dentro lotti si riferisce alla riga)
                HashMap righe_post_lotti = dbu.getListMapMap(conn, "select * from " + table_righe_lotti_temp, "id");
                List<Map> diffs_lotti = Sync.getDifferences(righe_pre_lotti, righe_post_lotti);
                //correggo gli id assegnati alle insert di righ da cui dipendono
                //preparo mappa id_padre_vecchio -> nuovo id
                Map map_id = new HashMap();
                for (Map ml : diffs) {
                    if (ml.get("k_old_id") != null) {
                        map_id.put(ml.get("k_old_id"), ml.get("k_new_id"));
                    }
                }
                //vado a cambiarlo su lotti
                System.out.println("diff lotti prima");
                DebugUtils.dump(diffs_lotti);
                for (Map ml : diffs_lotti) {
                    Map postl = (Map) ml.get("post");
                    if (postl != null && postl.get("id_padre") != null) {
                        Integer id_padre_l = (Integer) postl.get("id_padre");
                        ml.put("id_padre_old", id_padre_l);
                        if (map_id.get(id_padre_l) != null) {
                            ml.put("id_padre_new", map_id.get(id_padre_l));
                            postl.put("id_padre", ml.get("id_padre_new"));
                        }
                    }
                }
                System.out.println("diff lotti dopo");
                DebugUtils.dump(diffs_lotti);
                Sync.execDifferences(it.tnx.Db.getConn(), diffs_lotti, table_righe_lotti, true);

                //righe matricole (id_padre dentro matricole si riferisce a testata)
                if (table_righe_matricole != null) {
                    HashMap righe_post_matricole = dbu.getListMapMap(conn, "select * from " + table_righe_matricole_temp + " where id_padre = " + id, "id");
                    List<Map> diffs_matricole = Sync.getDifferences(righe_pre_matricole, righe_post_matricole);
                    System.out.println("diffs_matricole = " + diffs_matricole);
                    Sync.execDifferences(it.tnx.Db.getConn(), diffs_matricole, table_righe_matricole, true);
                }

                return true;
            } else {
                Thread.dumpStack();
                SwingUtils.showErrorMessage(parent, "Errore nel salvataggio dei dati di testa");
            }
        } catch (Exception e) {
            SwingUtils.showExceptionMessage(parent, e);
        }

        return false;

    }

    static public String aggiornaDebug(boolean updateTable) {
        String msg = "\nSync attivo !";
        try {
            Map m = dbu.getListMap(it.tnx.Db.getConn(), "select * from sync_config").get(0);
            msg += "\n" + DebugFastUtils.dumpAsString(m);
        } catch (Exception e) {
        }
        if (updateTable) {
            SwingUtils.inEdt(new Runnable() {
                public void run() {
                    Rectangle rect = main.getPadrePanel().tabsync.getVisibleRect();
                    try {
                        dbu.toTable(it.tnx.Db.getConn(), "select id, id_master, sync_date, client_id, type, table_name, field_name, primary_value, old_value, new_value from sync_slave order by id desc limit 300", main.getPadrePanel().tabsync);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    main.getPadrePanel().tabsync.scrollRectToVisible(rect);
                }
            });
        }
        return msg;
    }
}
