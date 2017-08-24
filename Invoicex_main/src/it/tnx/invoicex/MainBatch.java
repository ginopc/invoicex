/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex;

import gestioneFatture.DumpThread;
import gestioneFatture.Scadenze;
import gestioneFatture.Storico;
import gestioneFatture.dbDocumento;
import gestioneFatture.dbFattura;
import gestioneFatture.dbFatturaRicevuta;
import gestioneFatture.dbOrdine;
import gestioneFatture.frmElenDDT;
import gestioneFatture.frmElenFatt;
import gestioneFatture.frmElenFattAcquisto;
import gestioneFatture.frmElenOrdini;
import gestioneFatture.iniFileProp;
import gestioneFatture.logic.documenti.Documento;
import gestioneFatture.logic.provvigioni.ProvvigioniFattura;
import gestioneFatture.main;
import static gestioneFatture.main.getPadreFrame;
import static gestioneFatture.main.pf;
import static gestioneFatture.main.pluginPresenti;
import static gestioneFatture.main.plugins;
import static gestioneFatture.main.pluginsAvviati;
import static gestioneFatture.main.plugins_path;
import it.tnx.Db;
import it.tnx.JFrameMessage;
import it.tnx.accessoUtenti.Utente;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugFastUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.FileUtils;
import it.tnx.commons.HttpUtils;
import it.tnx.commons.MicroBench;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import mjpf.EntryDescriptor;
import mjpf.PluginEntry;
import mjpf.PluginFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Marco
 */
public class MainBatch {

    String file_param = null;
    String param_prop = null;
    JSONObject json_param = null;
    PrintStream origerr = null;
    PrintStream origout = null;
    ByteArrayOutputStream baoserr = new ByteArrayOutputStream();
    ByteArrayOutputStream baosout = new ByteArrayOutputStream();
    
    private String working_dir = null;

    static public final String tipo_documento_fattura_vendita = "fattura_vendita";
    static public final String tipo_documento_fattura_acquisto = "fattura_acquisto";
    static public final String tipo_documento_ordine_vendita = "ordine_vendita";
    static public final String tipo_documento_ordine_acquisto = "ordine_acquisto";
    static public final String tipo_documento_ddt_vendita = "ddt_vendita";
    static public final String tipo_documento_ddt_acquisto = "ddt_acquisto";

    static public Map<String, String> tipi_doc_inv = new HashMap();

    static {
        tipi_doc_inv.put(tipo_documento_fattura_vendita, Db.TIPO_DOCUMENTO_FATTURA);
        tipi_doc_inv.put(tipo_documento_fattura_acquisto, Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA);
        tipi_doc_inv.put(tipo_documento_ordine_vendita, Db.TIPO_DOCUMENTO_ORDINE);
        tipi_doc_inv.put(tipo_documento_ordine_acquisto, Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO);
        tipi_doc_inv.put(tipo_documento_ddt_vendita, Db.TIPO_DOCUMENTO_DDT);
        tipi_doc_inv.put(tipo_documento_ddt_acquisto, Db.TIPO_DOCUMENTO_DDT_ACQUISTO);
    }

    public MainBatch() {
        System.setProperty("logfile.name", "invoicex.log");

        origerr = new PrintStream(System.err);
        origout = new PrintStream(System.out);

        //per log4j
//        System.setErr(new PrintStream(new LoggingOutputStream(Category.getRoot(), Priority.WARN), true));
//        System.setOut(new PrintStream(new LoggingOutputStream(Category.getRoot(), Priority.INFO), true));

        System.setErr(new PrintStream(baoserr));
        System.setOut(new PrintStream(baosout));
    }

    public void exec(String funzione, String file_param) {
        System.out.println("funzione = " + funzione + " file_param:" + file_param);
        System.out.flush();
        
        main.isBatch = true;
        MicroBench mb = new MicroBench(true);        
        try {
            if (file_param != null && this.file_param == null) {
                this.file_param = file_param;
            }
            init();
            if (funzione.equalsIgnoreCase("getPdf")) {
                Object ret = getPdf(cu.s(json_param.get("tipo_doc")), cu.i(json_param.get("id")));
                mb.out("fine");
                if (ret instanceof String) {
                    JSONObject retj = new JSONObject();
                    retj.put("esito", "ok");
                    retj.put("file", ret);
                    ok(retj.toJSONString());
                }
                error(ret == null ? "ret = null" : ret.toString());
            } else if (funzione.equalsIgnoreCase("convertiDoc")) {
                List<Integer> righe_da_includere = null;
                JSONArray jrighe_includere = (JSONArray)json_param.get("righe_da_includere");
                System.out.println("jrighe_includere = " + jrighe_includere);
                if (jrighe_includere != null) {
                    righe_da_includere = new ArrayList<Integer>();
                    for (Object origa : jrighe_includere) {
                        righe_da_includere.add(cu.i(((JSONObject)origa).get("id")));
                    }
                }
                Object ret = convertiDoc(
                        cu.s(json_param.get("da_tipo_doc"))
                        ,cu.s(json_param.get("a_tipo_doc"))
                        ,cu.i(json_param.get("id"))
                        ,righe_da_includere
                );
                mb.out("fine");
                if (ret instanceof Integer) {
                    JSONObject retj = new JSONObject();
                    retj.put("esito", "ok");
                    retj.put("id", ret);
                    ok(retj.toJSONString());
                } else if (ret instanceof Exception) {
                    error(ret == null ? "ret = null" : ((Exception)ret).getMessage());
                }
                error(ret == null ? "ret = null" : ret.toString());                
            } else if (funzione.equalsIgnoreCase("saveDocument")) {
                Object ret = saveDocument(cu.s(json_param.get("tipo_doc")));
                mb.out("fine");
                if (ret instanceof Integer) {
                    JSONObject retj = new JSONObject();
                    retj.put("esito", "ok");
                    retj.put("id", ret);
                    ok(retj.toJSONString());
                }
                error(ret == null ? "ret = null" : ret.toString());
//            } else if (funzione.equalsIgnoreCase("testxmlpa")) {
//                Integer id = 244;
//                InvoicexUtil.EsitoGeneraTotali esito = InvoicexUtil.generaTotaliDocumento("test_fatt", id);
//                if (!esito.esito) {
//                    SwingUtils.showErrorMessage(null, esito.anomalia);
//                }
//                String sql = "select t.*, p.codice_xmlpa "
//                        + " from test_fatt t left join pagamenti p on t.pagamento = p.codice"
//                        + " where id = " + id;
//                List<Map> fatture = DbUtils.getListMap(Db.getConn(), sql);
//                sql = "select * from clie_forn where codice = " + fatture.get(0).get("cliente");
//                Map destinatario = DbUtils.getListMap(Db.getConn(), sql).get(0);
//                for (Map m : fatture) {
//                    sql = "select r.*, i.percentuale as perc_iva, i.codice_natura_xmlpa from righ_fatt r left join codici_iva i on r.iva = i.codice where id_padre = " + m.get("id") + " order by riga";
//                    List<Map> righe = DbUtils.getListMap(Db.getConn(), sql);
//                    m.put("righe", righe);
//                    sql = "select t.*, i.codice_natura_xmlpa from test_fatt_iva t left join codici_iva i on t.codice_iva = i.codice where id_padre = " + m.get("id");
//                    List<Map> totali_iva = DbUtils.getListMap(Db.getConn(), sql);
//                    m.put("totali_iva", totali_iva);
//                    sql = "select * from scadenze"
//                            + " where documento_tipo = 'FA' and documento_serie = '" + m.get("serie") + "' and documento_numero = " + m.get("numero") + " and documento_anno = " + m.get("anno") + " order by numero";
//                    List<Map> scadenze = DbUtils.getListMap(Db.getConn(), sql);
//                    m.put("scadenze", scadenze);
//                    
//                }
//                ExportXmlPa x = new ExportXmlPa(fatture, destinatario);
//                x.testxmlpa();
                
            } else if (funzione.equalsIgnoreCase("generaProvvigioni")) {
                Object ret = generaProvvigioni(cu.i(json_param.get("id_fattura")));
                mb.out("fine");
                if (ret instanceof Boolean && (Boolean)ret == true) {
                    JSONObject retj = new JSONObject();
                    retj.put("esito", "ok");
                    ok(retj.toJSONString());
                }
                error(ret == null ? "ret = null" : ret.toString());                
            } else if (funzione.equalsIgnoreCase("calcolaTotali")) {
                Object ret = calcolaTotali(cu.s(json_param.get("tipo_doc")), cu.i(json_param.get("id")));
                mb.out("fine");
                if (ret instanceof Boolean && (Boolean)ret == true) {
                    JSONObject retj = new JSONObject();
                    retj.put("esito", "ok");
                    ok(retj.toJSONString());
                }
                error(ret == null ? "ret = null" : ret.toString());                                
            } else if (funzione.equalsIgnoreCase("backup")) {
                if (main.pluginBackupTnx) {
                    URL[] url = new URL[1];
                    url[0] = new URL("file:plugins/InvoicexPluginBackupTnx.jar");
                    URLClassLoader classloader = new URLClassLoader(url);                    
                    Class cl = classloader.loadClass("invoicexpluginbackuptnx.DumpThreadOnline");
                    Constructor<?> cons = cl.getConstructor(JFrameMessage.class);
                    Object odump = cons.newInstance(new JFrameMessage());
                    Method mstart = cl.getMethod("start");
                    mstart.invoke(odump);
                    Method mjoin = cl.getMethod("join");
                    mjoin.invoke(odump);
                } else {
                    DumpThread dump = new DumpThread(new JFrameMessage());
                    dump.start();
                    dump.join();
                }
                System.out.println("backup completato");                
            } else {
                error("funzione '" + funzione + "' non definita");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            error(t.toString());
            mb.out("fine con errore");
            System.exit(1);
        }
        mb.out("fine ok");
        output();
        System.exit(0);
    }

    public void init() throws ParseException, IOException {
        System.out.println("Invoicex (Batch) ver. " + main.version + " " + main.build);
        System.out.flush();
        
        JSONObject db = null;
        if (file_param != null) {
            System.out.println("file_param = " + file_param);
            String filecontent = FileUtils.readContent(new File(file_param));
            JSONParser parser = new JSONParser();
            json_param = (JSONObject) parser.parse(filecontent);
            db = (JSONObject) json_param.get("db");
            param_prop = (String) json_param.get("param_prop");
            if (db == null && param_prop == null) {
                error("parametri del db vuoti e nessun param_prop passato");
            }
        }

//        JSONArray lista_news = (JSONArray) jo.get("n");
        File fwd = new File("./");
        try {
            main.wd = fwd.getCanonicalPath() + File.separator;
        } catch (Exception e) {
            e.printStackTrace();
        }
        main.paramProp = "param_prop.txt";
        if (param_prop != null) {
            main.paramProp = (String) param_prop;
        }
        if (!main.paramProp.equals("json")) {
            File test = new File(main.paramProp);
            if (!test.exists()) {
                error("il file di parametri " + test.getAbsolutePath() + " non esiste");
            }
            main.fileIni = new iniFileProp();
            main.fileIni.realFileName = main.wd + main.paramProp;
            main.loadIni();
        } else {
            //carica da json
            main.fileIni = new iniFileProp() {

                @Override
                public String getValue(String subject, String variable) {
                    return cu.s(json.get("param_" + subject + "_" + variable));
                }

                @Override
                public synchronized boolean setValue(String subject, String variable, String value) {
                    return true;
                }

                @Override
                public void parseLines() {
                    //ignora
                }

                @Override
                public void loadFile() {
                    //ignora
                }

                @Override
                public synchronized boolean existKey(String subject, String variable) {
                    return json.containsKey("param_" + subject + "_" + variable);
                }

            };
            //main.fileIni.realFileName = "json";
            main.fileIni.json = json_param;
//            fileIni.loadFile();
//            fileIni.parseLines();

        }

        //controllo se forzare i dati di connessione
        if (db != null) {
            Db.dbServ = (String) db.get("server");
            Db.dbPort = 3306;
            int ip = Db.dbServ.indexOf(":");
            if (ip > 0) {
                String server = Db.dbServ.substring(0, ip);
                String porta = Db.dbServ.substring(ip + 1, Db.dbServ.length());
                try {
                    Db.dbPort = Integer.parseInt(porta);
                } catch (NumberFormatException err) {
                }
            }
            Db.dbNameDB = (String) db.get("database");
            Db.dbName = (String) db.get("user");
            Db.dbPass = (String) db.get("pass");
        }
        
        if (Db.dbServ.equalsIgnoreCase("localhost")) {
            Db.localSocketAddress = "127.0.0.1";
        }

        //testo connessione
        try {
            Connection testconn = Db.getConn();
            if (testconn == null) {
                error("errore in connessione mysql (test=null) a:" + Db.dbServ + " db:" + Db.dbNameDB + " user:" + Db.dbName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            error("errore in connessione mysql (exc:" + e.toString() + ") a: " + Db.dbServ + " db:" + Db.dbNameDB + " user:" + Db.dbName);
        }

        main.utente = new Utente(1);

        //caricamento plugins ?
        initPlugins();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    

    //funzioni
    public Object getPdf(String tipo_documento, Integer id) throws Exception {
        String tipo_doc_inv = tipi_doc_inv.get(tipo_documento);

        try {
            InvoicexUtil.aggiornaTotaliRighe(tipo_doc_inv, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map retnum = InvoicexUtil.getSerieNumeroAnno(tipo_doc_inv, id);
        String dbSerie = cu.s(retnum.get("serie"));
        int dbNumero = cu.i0(retnum.get("numero"));
        int dbAnno = cu.i0(retnum.get("anno"));

        //TODO param nome tab e != 7
        Object ret = null;
        if (tipo_documento.equals(tipo_documento_fattura_vendita)) {
            String sql = "select tipo_fattura, descrizione_breve from test_fatt t join tipi_fatture tf on t.tipo_fattura = tf.tipo where id = " + id;
            sql += " and tipo_fattura != 7";
            List<Map> list = DbUtils.getListMap(Db.getConn(), sql);
            String tipofatt = cu.s(list.get(0).get("descrizione_breve"));
            ret = frmElenFatt.stampa(tipofatt, dbSerie, dbNumero, dbAnno, true, true, true, id);
        } else if (tipo_documento.equals(tipo_documento_fattura_acquisto)) {
            String sql = "select tipo_fattura, descrizione_breve from test_fatt_acquisto t join tipi_fatture_acquisto tf on t.tipo_fattura = tf.tipo where id = " + id;
            List<Map> list = DbUtils.getListMap(Db.getConn(), sql);
            String tipofatt = cu.s(list.get(0).get("descrizione_breve"));
            ret = frmElenFattAcquisto.stampa(tipofatt, dbSerie, dbNumero, dbAnno, true, true, true, id);
        } else if (tipo_documento.equals(tipo_documento_ddt_vendita) || tipo_documento.equals(tipo_documento_ddt_acquisto)) {
            boolean acquisto = tipo_documento.equals(tipo_documento_ddt_vendita) ? false : true;
            ret = frmElenDDT.stampa(tipo_documento, dbSerie, dbNumero, dbAnno, true, true, true, acquisto, id);
        } else if (tipo_documento.equals(tipo_documento_ordine_vendita) || tipo_documento.equals(tipo_documento_ordine_acquisto)) {
            boolean acquisto = tipo_documento.equals(tipo_documento_ordine_vendita) ? false : true;
            ret = frmElenOrdini.stampa(tipo_documento, dbSerie, dbNumero, dbAnno, true, true, true, acquisto, id);
        }

        System.out.println("ret = " + ret);
        if (ret instanceof String) {
            return (String) ret;
        }

        return ret;
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public Object convertiDoc(String da_tipo_documento, String a_tipo_documento, Integer id, List<Integer> id_righe_da_includere) throws Exception {
        Storico.scriviSeparati("batch convertiDoc inizio " + da_tipo_documento + " a " + a_tipo_documento + " id " + id + " id_righe " + id_righe_da_includere, null);

        String da_tipo_doc_inv = tipi_doc_inv.get(da_tipo_documento);
        String a_tipo_doc_inv = tipi_doc_inv.get(a_tipo_documento);
        
        JSONObject jp = json_param;
        
        //da ordine a fattura
        dbOrdine doc = new dbOrdine();
        String sql = "select id, serie, numero, anno from test_ordi where id = " + id;
        List<Map> list = dbu.getListMap(Db.getConn(), sql);
        if (list.size() == 0) {
            return new Exception("Non trovato documento di origine " + da_tipo_documento + " id " + id);
        }
        Map rec = list.get(0);

        InvoicexUtil.aggiornaTotaliRighe(Db.TIPO_DOCUMENTO_ORDINE, id);
        
        doc.serie = cu.s(rec.get("serie"));
        
        doc.ids = new Integer[] {id};
        doc.acquisto = false;

        String ret = doc.converti("fatt", false);
        if (ret != null) {
            //fattura creata -> ret
            System.out.println("ret: " + ret);
            Integer id_new = cu.i(ret);
            return id_new;
        } else {
            //problema creazione fattura...
            return "Errore in conversione documento, consultare il log";
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public Object saveDocument(String tipo_documento) throws Exception {
        Storico.scriviSeparati("batch saveDocument inizio " + tipo_documento, null);

        String tipo_doc_inv = tipi_doc_inv.get(tipo_documento);
        JSONObject jp = json_param;
        JSONObject dati = (JSONObject) jp.get("dati_doc");
        JSONObject righe = (JSONObject) dati.get("righe");
        JSONObject altro = (JSONObject) dati.get("altro");
        
        String sql = null;
        String tabt = InvoicexUtil.getTabTestateFromTipoDoc(tipo_doc_inv);
        String tabr = InvoicexUtil.getTabRigheFromTipoDoc(tipo_doc_inv);
        boolean acquisto = false;
        if (tabt.endsWith("acquisto")) {
            acquisto = true;
        }
        String campoclifor = acquisto ? "fornitore" : "cliente";

        //creo testata
        //prendo ultimo numero + uno
        String serie = "";
        Integer numero = null;
        Integer anno = null;
        Date data = new Date();
        if (cu.toDate(jp.get("data")) != null) {
            data = cu.toDate(jp.get("data"));
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        if (cu.s(jp.get("serie")) != null) {
            serie = cu.s(jp.get("serie"));
        }
        anno = cal.get(Calendar.YEAR);

        sql = "select numero from " + tabt;
        sql += " where anno = " + anno;
        sql += " and serie = " + Db.pcs(serie);
        if (tipo_documento.equals(tipo_documento_fattura_vendita)) {
            sql += " and tipo_fattura != " + dbFattura.TIPO_FATTURA_SCONTRINO;
        }
        sql += " order by numero desc limit 1";
        Object obj = DbUtils.getObject(Db.getConn(), sql, false);
        numero = cu.i0(obj) + 1;

        Map m = new HashMap();
        m.put("serie", serie);
        m.put("numero", numero);
        m.put("anno", anno);
        Integer tipoFattura = null;
        if (tipo_documento.equals(tipo_documento_fattura_vendita)) {
            tipoFattura = dbFattura.TIPO_FATTURA_IMMEDIATA;
            if (cu.s(jp.get("tipo_doc2")).equalsIgnoreCase("accompagnatoria")) {
                tipoFattura = dbFattura.TIPO_FATTURA_ACCOMPAGNATORIA;
            }
            m.put("tipo_fattura", tipoFattura);
        }

        if (dati.get("deposito") != null) {
            m.put("deposito", dati.get("deposito"));
        }
        
        m.put("data", data);
        m.put("pagamento", dati.get("tipo_pagamento"));

        m.put("sconto1", 0);
        m.put("sconto2", 0);
        m.put("sconto3", 0);
        if (cu.d0a(altro.get("sconto_livello")) != 0) {
            m.put("sconto1", cu.d0a(altro.get("sconto_livello")));
        }

        Map mcf = null;
        Map mdd = null;
        Map mtp = null;

        try {
            mcf = DbUtils.getListMap(Db.getConn(), "select * from clie_forn where codice = '" + dati.get("clifor") + "'").get(0);
        } catch (Exception e) {
            System.err.println("non trovato cliente");
        }
        try {
//            mdd = DbUtils.getListMap(Db.getConn(), "select * from clie_forn_dest where codice_cliente = '" + dati.get("clifor") + "' and codice = '" + dati.get("clifor_dest") + "'").get(0);
            mdd = DbUtils.getListMap(Db.getConn(), "select * from clie_forn_dest where id = '" + dati.get("clifor_dest") + "'").get(0);
        } catch (Exception e) {
            System.err.println("non trovata dest diversa");
        }
        String codicePagamento = cu.s(dati.get("tipo_pagamento"));
        try {
            mtp = DbUtils.getListMap(Db.getConn(), "select * from pagamenti where codice = " + Db.pcs(codicePagamento)).get(0);
        } catch (Exception e) {
            System.err.println("non trovato tipo pagamentos");
        }

        m.put("banca_abi", mcf.get("banca_abi"));
        m.put("banca_cab", mcf.get("banca_cab"));
        m.put("banca_cc", mcf.get("banca_cc"));
        m.put("banca_iban", mcf.get("banca_iban"));

        String note = "";
        if (cu.s(mcf.get("note_automatiche")).equalsIgnoreCase("S")) {
            note = cu.s(mcf.get("note"));
        }
        m.put("note", note);

        if (mdd != null) {
//            m.put("cliente_destinazione", dati.get("clifor_dest"));
            m.put("id_cliente_destinazione", dati.get("clifor_dest"));
            m.put("dest_ragione_sociale", mdd.get("ragione_sociale"));
            m.put("dest_indirizzo", mdd.get("indirizzo"));
            m.put("dest_cap", mdd.get("cap"));
            m.put("dest_localita", mdd.get("localita"));
            m.put("dest_provincia", mdd.get("provincia"));
            m.put("dest_telefono", mdd.get("telefono"));
            m.put("dest_cellulare", mdd.get("cellulare"));
            m.put("dest_paese", mdd.get("paese"));
        }

        m.put("spese_trasporto", cu.d0a(dati.get("spese_spedizione")));
        m.put("spese_incasso", cu.d0a(dati.get("spese_incasso")));

        m.put("riferimento", "");
        m.put("note_pagamento", mtp.get("note_su_documenti"));

        Integer codice_agente = cu.i(mcf.get("agente"));
        m.put("agente_codice", codice_agente);

        //trovo perc provv
        Double perc_provvigione = null;
        Double prov_agente = null;
        Double prov_clie = null;
        //per cliente
        sql = "select provvigione_predefinita_cliente\n"
                + " from clie_forn where codice = " + dati.get("clifor");
        prov_clie = cu.d(DbUtils.getObject(Db.getConn(), sql, false));
        if (prov_clie != null) {
            perc_provvigione = prov_clie;
        } else {
            //per agente
            sql = "select percentuale \n"
                    + " from agenti where id = " + codice_agente;
            prov_agente = cu.d(DbUtils.getObject(Db.getConn(), sql, false));
            if (prov_agente != null) {
                perc_provvigione = prov_agente;
            }
        }
        m.put("agente_percentuale", perc_provvigione);
        Double perc_provvigione_testata = perc_provvigione;

        m.put("giorno_pagamento", mcf.get("giorno_pagamento"));

        if (tipo_documento.equals(tipo_documento_ordine_vendita) || tipo_documento.equals(tipo_documento_ordine_acquisto)) {
            m.put("stato_ordine", "Ordine");
        }

        //prezzi_ivati
        m.put(campoclifor, dati.get("clifor"));

        sql = "insert into " + tabt + " set " + DbUtils.prepareSqlFromMap(m);
        Integer id = Db.executeSqlRetIdDialogExc(Db.getConn(), sql, false, true);

        System.out.println("generato " + tabt + " = " + id);

        if (tipo_documento.equals(tipo_documento_ordine_vendita) || tipo_documento.equals(tipo_documento_ordine_acquisto)) {
            dbOrdine ordine = new dbOrdine();
            ordine.serie = serie;
            ordine.numero = numero;
            ordine.anno = anno;
            ordine.id = id;
        }

        Integer old_da_ordine = null;

        //inserisco righe
        System.out.println("righe:");
        DebugUtils.dump(righe);        
        SortedSet<String> keys = new TreeSet<String>(righe.keySet());
        System.out.println("keys:");
        DebugUtils.dump(keys);
        
        int riga = 0;
        List<Integer> id_ordini_da_aggiornare_stato = new ArrayList();
        for (String key : keys) {
            JSONObject jr = (JSONObject) righe.get(key);
            riga++;

            Integer da_ordine = null;
            Integer da_riga = null;
            if (jr.get("da_ordine") != null && jr.get("da_riga") != null) {
                da_ordine = cu.i(jr.get("da_ordine"));
                da_riga = cu.i(jr.get("da_riga"));
                System.out.println("riga = " + riga);
                System.out.println("da_ordine = " + da_ordine);
                System.out.println("old_da_ordine = " + old_da_ordine);
                if (riga == 1 || !da_ordine.equals(old_da_ordine)) {
                    //da ordine...                    
                    try {
                        Map ordi_prov = DbUtils.getListMap(Db.getConn(), "select id, serie, numero, data, riferimento from test_ordi" + (acquisto ? "_acquisto" : "") + " where id = " + da_ordine).get(0);
                        id_ordini_da_aggiornare_stato.add(cu.i(ordi_prov.get("id")));
                        Map r = new HashMap();
                        r.put("id_padre", id);
                        r.put("serie", serie);
                        r.put("numero", numero);
                        r.put("anno", anno);
                        r.put("riga", riga);
                        r.put("codice_articolo", "");
                        r.put("quantita", 0);
                        r.put("prezzo", 0);
                        r.put("prezzo_ivato", 0);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        String descrizione = main.fileIni.getValue("altro", "da_ordine", "*** Da Ordine") + " numero " + cu.s(ordi_prov.get("serie")) + cu.s(ordi_prov.get("numero")) + " del " + sdf.format(cu.toDate(ordi_prov.get("data")));
                        if (!CastUtils.toString(ordi_prov.get("riferimento")).equals("")) {
                            descrizione += " / Vostro rif. " + ordi_prov.get("riferimento");
                        }
                        r.put("descrizione", descrizione);

                        sql = "insert into " + tabr + " set " + DbUtils.prepareSqlFromMap(r);
                        DbUtils.tryExecQuery(Db.getConn(), sql);
                        riga++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            Map r = new HashMap();
            r.put("id_padre", id);
            r.put("serie", serie);
            r.put("numero", numero);
            r.put("anno", anno);
            r.put("riga", riga);
            r.put("codice_articolo", jr.get("codice"));
            r.put("descrizione", jr.get("nome"));
            r.put("um", "pz");
            double qta = cu.d0a(jr.get("quantita"));
            r.put("quantita", qta);

            r.put("sconto1", 0);
            r.put("sconto2", 0);

            double prezzo_imponibile = 0;
            double prezzo_ivato = 0;
            if (key.startsWith("invx_")) {
                //righe da ordini di sinvoicex
                if (cu.d0a(jr.get("sconto1")) != 0) {
                    r.put("sconto1", cu.d0a(jr.get("sconto1")));
                }
                if (cu.d0a(jr.get("sconto2")) != 0) {
                    r.put("sconto2", cu.d0a(jr.get("sconto2")));
                }
                //prezzo_imponibile = cu.d0a(jr.get("prezzo"));
                prezzo_imponibile = cu.d0a(jr.get("prezzo_pieno"));
                r.put("prezzo", prezzo_imponibile);
            } else {
                //righe del carrello
                //priorità sconti : 1° sconto cliente/marca, 2° sconto articolo, 3° sconto coupon                            
                List sconti = new ArrayList();
                if (cu.d0a(jr.get("sconto_produttore")) != 0) {
                    sconti.add(cu.d0a(jr.get("sconto_produttore")));
                }
                if (cu.d0a(jr.get("sconto_sconto1")) != 0) {
                    sconti.add(cu.d0a(jr.get("sconto_sconto1")));
                }
                if (cu.d0a(jr.get("sconto_sconto2")) != 0) {
                    sconti.add(cu.d0a(jr.get("sconto_sconto2")));
                }
                if (cu.d0a(jr.get("sconto_coupon")) != 0) {
                    sconti.add(cu.d0a(jr.get("sconto_coupon")));
                }
                if (sconti.size() > 2) {
                    //calcolo sconto medio
                    double prezzo_netto = cu.d0a(jr.get("prezzo"));
                    double prezzo_lordo = cu.d0a(jr.get("prezzo_pieno"));
                    double sconto = 100d - (prezzo_netto * 100d / prezzo_lordo);
                    r.put("sconto1", sconto);
                } else {
                    if (sconti.size() == 2) {
                        r.put("sconto1", sconti.get(0));
                        r.put("sconto2", sconti.get(1));
                    } else if (sconti.size() == 1) {
                        r.put("sconto1", sconti.get(0));
                    }
                }
                prezzo_imponibile = cu.d0a(jr.get("prezzo_pieno"));
                r.put("prezzo", prezzo_imponibile);
            }
            String codiceIva = InvoicexUtil.getIvaRigaArticolo(cu.i(dati.get("clifor")), cu.s(mcf.get("paese")), cu.s(jr.get("codice")));
            r.put("iva", codiceIva);
            boolean prezziIvati = false;
            r.put("flag_ritenuta", "");
            r.put("flag_rivalsa", "");

            //per toys priorità percentuale = agente, cliente, fornitore articolo
            perc_provvigione = perc_provvigione_testata;
            sql = "select c.provvigione_predefinita_fornitore\n"
                    + " from clie_forn c join articoli a on c.codice = a.fornitore and a.codice = " + Db.pcs(cu.s(jr.get("codice")));
            Double prov_forn = cu.d(DbUtils.getObject(Db.getConn(), sql, false));
            if (prov_forn != null) {
                perc_provvigione = prov_forn;
            } else {
                //per cliente
                if (prov_clie != null) {
                    perc_provvigione = prov_clie;
                } else {
                    //per agente
                    if (prov_agente != null) {
                        perc_provvigione = prov_agente;
                    }
                }
            }
            r.put("provvigione", perc_provvigione);

            if (!tipo_documento.equals(tipo_documento_fattura_vendita) && !tipo_documento.equals(tipo_documento_fattura_acquisto)) {
                r.put("quantita_evasa", 0);
            }
            r.put("arrotondamento_parametro", 0);
            r.put("arrotondamento_tipo", "Inf.");
            double iva_prezz = 100d;
            try {
                iva_prezz = 100d + ((BigDecimal) DbUtils.getObject(Db.getConn(), "select percentuale from codici_iva where codice = " + Db.pc(codiceIva, Types.VARCHAR))).doubleValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!prezziIvati) {
                double new_prezz_lordo = 0;
                double prezz_netto = prezzo_imponibile;
                new_prezz_lordo = (double) (prezz_netto / 100d) * iva_prezz;
                prezzo_ivato = new_prezz_lordo;
            } else {
                double new_prezz_netto = 0;
                double prezz_lordo = prezzo_ivato;
                new_prezz_netto = (double) (prezz_lordo * 100d) / iva_prezz;
                prezzo_imponibile = new_prezz_netto;
            }
            Map totali = InvoicexUtil.getTotaliRiga(qta, prezzo_imponibile, prezzo_ivato, cu.d0a(r.get("sconto1")), cu.d0a(r.get("sconto2")), codiceIva, prezziIvati);
            r.put("totale_ivato", totali.get("tot_con_iva"));
            r.put("totale_imponibile", totali.get("tot_senza_iva"));
            r.put("is_descrizione", "N");
            r.put("prezzo_ivato", prezzo_ivato);

            //evasione
            if (!tipo_documento.equals(tipo_documento_ordine_vendita) && !tipo_documento.equals(tipo_documento_ordine_acquisto)) {
                if (jr.get("da_ordine") != null && jr.get("da_riga") != null) {
                    //andare ad evadere la riga dell'ordine
                    sql = "update righ_ordi" + (acquisto ? "_acquisto" : "") + " set "
                            + " quantita_evasa = IFNULL(quantita_evasa,0) + " + Db.pc(qta, Types.DOUBLE) + ""
                            + " where id = " + da_riga;
                    System.out.println("evasione: " + sql);
                    DbUtils.tryExecQuery(Db.getConn(), sql);

                    r.put("da_ordi", da_ordine);
                    r.put("da_ordi_riga", da_riga);
                }
            }

            sql = "insert into " + tabr + " set " + DbUtils.prepareSqlFromMap(r);
            DbUtils.tryExecQuery(Db.getConn(), sql);

            old_da_ordine = da_ordine;
        }

        //salvo totali
        Documento doc = new Documento();
        doc.load(gestioneFatture.Db.INSTANCE, numero, serie, anno, tipo_doc_inv, id);
        doc.calcolaTotali();

        Map t = new HashMap();
        if (!tipo_documento.equals(tipo_documento_fattura_vendita) && !tipo_documento.equals(tipo_documento_fattura_acquisto)) {
            t.put("evaso", "");
        }
        t.put("sconto", 0);
        t.put("totale_imponibile", doc.getTotaleImponibile());
        t.put("totale_iva", doc.getTotaleIva());
        t.put("totale", doc.getTotale());

        t.put("totale_imponibile_pre_sconto", doc.totaleImponibilePreSconto);
        t.put("totale_ivato_pre_sconto", doc.totaleIvatoPreSconto);
        sql = "update " + tabt + " set " + DbUtils.prepareSqlFromMap(t) + " where id = " + id;
        DbUtils.tryExecQuery(Db.getConn(), sql);

        InvoicexUtil.aggiornaPrezziNettiUnitari(Db.getConn(), tabr, tabt, id);

        //stato dei documenti di provenienza
        try {
            for (Integer id_ordine : id_ordini_da_aggiornare_stato) {
                try {
                    InvoicexUtil.aggiornaStatoEvasione(acquisto ? gestioneFatture.Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : gestioneFatture.Db.TIPO_DOCUMENTO_ORDINE, id_ordine);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //metto flag che sono gia' state fatturate
                String test_ordi = acquisto ? "test_ordi_acquisto" : "test_ordi";
                try {
                    String convertito = CastUtils.toString(DbUtils.getObject(gestioneFatture.Db.getConn(), "select convertito from test_ordi t where id = " + id_ordine));
                    convertito += (convertito.length() > 0 ? "\n" : "") + InvoicexUtil.getNumeroDaId(tipo_doc_inv, id);
                    sql = "update " + test_ordi + " t";
                    sql += " set convertito = " + gestioneFatture.Db.pc(convertito, "VARCHAR");
                    sql += " where id = " + id_ordine;
                    System.out.println("sql = " + sql);
                    DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), sql);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //scadenze
        boolean generaScadenze = false;
        if (tipo_documento.equals(tipo_documento_ordine_vendita)) {
            if (main.fileIni.getValueBoolean("pref", "scadenzeOrdini", false)) {
                generaScadenze = true;
            }
        } else if (tipo_documento.equals(tipo_documento_fattura_vendita) || tipo_documento.equals(tipo_documento_fattura_acquisto)) {
            generaScadenze = true;
        }
        if (generaScadenze) {
            Scadenze tempScad = new Scadenze(tipo_doc_inv, id, codicePagamento);
            boolean scadenzeRigenerate = false;
            tempScad.generaScadenze();
        }

        //Provvigioni
        if (tipo_documento.equals(tipo_documento_fattura_vendita) && codice_agente != null) {
            //trovare codice agente e perc provvigioni
            Double perc_agente = 0d;
            ProvvigioniFattura provvigioni = new ProvvigioniFattura(id, codice_agente, perc_agente);
            double nuovoImportoTeoricoProvvigioni = provvigioni.getTotaleProvvigioni();
            boolean ret = provvigioni.generaProvvigioni();
            try {
                Storico.scrivi("Genera provvigioni", tipo_doc_inv + " " + serie + " " + numero + " " + anno + " " + codice_agente + " " + perc_agente);
            } catch (Exception e) {
            }
            System.out.println("esito genera provvigioni:" + ret + " : " + provvigioni.ret);
        }

        //movimenti
        dbDocumento dbdoc = null;
        boolean generaMovimenti = false;
        if (tipo_documento.equals(tipo_documento_ddt_vendita) || tipo_documento.equals(tipo_documento_ddt_acquisto)) {
            generaMovimenti = true;
            dbdoc = new dbDocumento();
        } else if (tipo_documento.equals(tipo_documento_fattura_vendita) || tipo_documento.equals(tipo_documento_fattura_acquisto)) {
            dbdoc = new dbFattura();
            if (acquisto) {
                dbdoc = new dbFatturaRicevuta();
            }
        }
        int conta = 0;
        if (dbdoc != null && id != null) {
            dbdoc.serie = serie;
            dbdoc.numero = numero;
            dbdoc.anno = anno;
            dbdoc.setId(id);
            if (tipo_documento.equals(tipo_documento_fattura_vendita) || tipo_documento.equals(tipo_documento_fattura_acquisto)) {
                dbdoc.tipoFattura = tipoFattura;
                try {
                    sql = "select count(*) from righ_ddt" + (acquisto ? "_acquisto" : "") + " "
                            + " where in_fatt = " + id;
                    conta = cu.i0(DbUtils.getObject(Db.getConn(), sql));
                } catch (SQLException sqlerr) {
                    sqlerr.printStackTrace();
                }
            } else if (tipo_documento.equals(tipo_documento_ddt_vendita) || tipo_documento.equals(tipo_documento_ddt_acquisto)) {
                dbdoc.tipoDocumento = tipo_doc_inv;
            }
            boolean azzerare = false;
            boolean generare = false;
            if (conta == 0) {
                generare = true;
            } else {
                azzerare = true;
            }
            if (generare) {
                Storico.scriviSeparati("batch saveDocument generaMovimentiMagazzino id doc:" + id, "");
                if (dbdoc.generaMovimentiMagazzino() == false) {
                    return new Exception("Errore nella generazione dei movimenti di magazzino");
                }
            }
            if (azzerare) {
                Storico.scriviSeparati("batch saveDocument azzeraMovimentiMagazzino id doc:" + id, "");
                if (dbdoc.azzeraMovimentiMagazzino() == true) {
                } else {
                    return new Exception("Errore nell'azzeramento dei movimenti di magazzino");
                }
            }
        }

        Storico.scriviSeparati("batch saveDocument fine " + tipo_documento + " id:" + id,
                "jp:\n" + MyDebugUtils.dumpAsString(jp));

        return id;
    }
    
    
    public Object calcolaTotali(String tipo_documento, Integer id) throws Exception {
        Storico.scriviSeparati("calcolaTotali inizio " + tipo_documento + " id " + id, null);

        String tipo_doc = tipi_doc_inv.get(tipo_documento);
        
        JSONObject jp = json_param;

        try {
            String tabt = Db.getNomeTabT(tipo_doc);
            String tabr = Db.getNomeTabR(tipo_doc);
            InvoicexUtil.aggiornaTotaliRighe(tipo_doc, id);
            InvoicexUtil.aggiornaPrezziNettiUnitari(tabr, tabt, id);            
            
            Documento doc = new Documento();
            doc.load(gestioneFatture.Db.INSTANCE, tipo_doc, id);
            doc.calcolaTotali();
            
            //salvarli
            Map m = new HashMap();
            m.put("totale", doc.getTotale());
            m.put("totale_iva", doc.getTotaleIva());
            m.put("totale_imponibile", doc.getTotaleImponibile());
            
            m.put("totale_imponibile_pre_sconto", doc.totaleImponibilePreSconto);
            m.put("totale_ivato_pre_sconto", doc.totaleIvatoPreSconto);
            
            System.out.println("tipo_doc:" + tipo_doc);
            if (tipo_doc.equals(Db.TIPO_DOCUMENTO_FATTURA) || tipo_doc.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
                m.put("totale_ritenuta", doc.getTotale_ritenuta());
                m.put("totaleRivalsa", doc.getTotale_rivalsa());
                m.put("totale_da_pagare", doc.getTotale_da_pagare());
            }
            
            m.put("totale_da_pagare_finale", doc.getTotale_da_pagare_finale());
            
            String sql = "update " + Db.getNomeTabT(tipo_doc) + " set " + dbu.prepareSqlFromMap(m) + " where id = " + id;
            System.out.println("sql totali = " + sql);
            dbu.tryExecQuery(Db.getConn(), sql);


        } catch (Exception e) {
            e.printStackTrace();
            return "Errore in calcola totali: " + e.toString();
        }
        return true;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    private void output() {
        origerr.println(baoserr.toString());
        origout.println(baosout.toString());
    }

    private void error(String error) {
        output();
        origerr.println("errore:" + error);
        System.exit(2);
    }

    private void ok(String json) {
        output();
        origerr.println(json);
        System.exit(0);
    }

    private void initPlugins() {
        //imposto no verifica cert ssl
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

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                System.out.println("verify = " + hostname + " sessione:" + session);
                return true;
            }
        });

        
        
        
        //controllo il plugin generale
        pf = new PluginFactory();
        pf.loadPlugins(plugins_path);
        boolean pi = false;
        Collection plugcol = pf.getAllEntryDescriptor();
        if (plugcol != null) {
            Iterator plugiter = plugcol.iterator();
            while (plugiter.hasNext()) {
                EntryDescriptor pd = (EntryDescriptor) plugiter.next();
                if (pd.getName().equalsIgnoreCase("pluginInvoicex")) {
                    pi = true;
                }
            }
        }
        if (!pi) {
            try {
                HttpUtils.saveBigFile(main.baseurlserver + "/" + InvoicexUtil.getDownloadDir() + "/plugins/InvoicexPluginInvoicex.jar", "plugins/InvoicexPluginInvoicex.jar");
            } catch (Exception e) {
                e.printStackTrace();
            }
            pf.loadPlugins(plugins_path);
            plugcol = pf.getAllEntryDescriptor();
        }        
        
        
        
        //carico i plugins
        pf = new PluginFactory();
        pf.loadPlugins(plugins_path);
        plugcol = pf.getAllEntryDescriptor();
        if (plugcol != null) {
            Iterator plugiter = plugcol.iterator();
            while (plugiter.hasNext()) {
                EntryDescriptor pd = (EntryDescriptor) plugiter.next();
//                if (!pd.getName().equalsIgnoreCase("pluginInvoicex")) {
                
//per batch carico solo acluni plugins
                if (pd.getName().equalsIgnoreCase("pluginAutoUpdate") 
                        || pd.getName().equalsIgnoreCase("pluginBackupTnx")
                        || pd.getName().equalsIgnoreCase("pluginToyforyou")) {
                    pluginPresenti.add(pd.getName());
                    plugins.put(pd.getName(), pd);

                    //avvio il plugin
                    try {
                        MicroBench mb = new MicroBench();
                        mb.start();
                        if (!main.isBatch) {
                            main.getPadrePanel().lblInfoLoading2.setText("Caricamento plugin " + pd.getName() + "...");
                        }
                        PluginEntry pl = (PluginEntry) pf.getPluginEntry(pd.getId());
                        pl.initPluginEntry(null);
                        System.out.println(pd.getName() + " Init -> tempo: " + mb.getDiff("init"));
                        pl.startPluginEntry();
                        pluginsAvviati.put(pd.getName(), pl);
                        if (!main.isBatch) {
                            main.getPadrePanel().lblInfoLoading2.setText(pd.getName() + " Caricato");
                        }
                        System.out.println(pd.getName() + " Caricato -> tempo: " + mb.getDiff("caricamento"));
                    } catch (NoSuchFieldError nofield) {
                        nofield.printStackTrace();
                        if (!main.isBatch) {
                            SwingUtils.showErrorMessage(getPadreFrame(), "<html>Errore durante l'avvio del <b>" + pd.getName() + "</b><br>Manca il campo <b>" + nofield.getMessage() + "</b><br>Probabilmente devi aggiornare Invoicex Base all'ultima release per utilizzare questo plugin</html>");
                        }
                    } catch (NoClassDefFoundError noclass) {
                        noclass.printStackTrace();
                        if (!main.isBatch) {
                            SwingUtils.showErrorMessage(getPadreFrame(), "<html>Errore durante l'avvio del <b>" + pd.getName() + "</b><br>Manca la classe <b>" + noclass.getMessage() + "</b><br>Probabilmente devi aggiornare Invoicex Base all'ultima release per utilizzare questo plugin</html>");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        if (!main.isBatch) {
                            SwingUtils.showErrorMessage(getPadreFrame(), "<html>Errore durante l'avvio del <b>" + pd.getName() + "</b><br>" + ex.toString() + "</b><br>Probabilmente devi aggiornare Invoicex Base all'ultima release per utilizzare questo plugin</html>");
                        }
                    } catch (Throwable tr) {
                        tr.printStackTrace();
                        if (!main.isBatch) {
                            SwingUtils.showErrorMessage(getPadreFrame(), "<html>Errore durante l'avvio del <b>" + pd.getName() + "</b><br>" + tr.toString() + "</b><br>Probabilmente devi aggiornare Invoicex Base all'ultima release per utilizzare questo plugin</html>");
                        }
                    }

                    main.controllaFlagPlugin(pd.getName());
                } else {
                    System.err.println("!!! ATTENZIONE !!! il " + pd.getName() + " non lo carico perchè in modalità batch");
                }
            }

        }

    }


    
    public Object generaProvvigioni(Integer id_fattura) {
        //Provvigioni
        Map fattura;
        try {
            fattura = dbu.getListMap(Db.getConn(), "select tipo_fattura from test_fatt where id = " + id_fattura).get(0);
            ProvvigioniFattura provvigioni = new ProvvigioniFattura(id_fattura);
            double nuovoImportoTeoricoProvvigioni = provvigioni.getTotaleProvvigioni();
            boolean ret = provvigioni.generaProvvigioni();
            try {
                Storico.scrivi("Genera provvigioni", Db.TIPO_DOCUMENTO_FATTURA + " " + fattura.get("serie") + " " + fattura.get("numero") + " " + fattura.get("anno") + " " + fattura.get("codice_agente") + " " + fattura.get("perc_agente") + " ret:" + ret);
            } catch (Exception e) {
            }            
            System.out.println("esito genera provvigioni:" + ret + " : " + provvigioni.ret);
            if (ret) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex;
        }
    }
    
}
