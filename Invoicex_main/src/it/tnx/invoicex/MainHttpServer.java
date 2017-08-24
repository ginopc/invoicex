/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex;

import gestioneFatture.Storico;
import gestioneFatture.dbOrdine;
import gestioneFatture.frmElenDDT;
import gestioneFatture.frmElenFatt;
import gestioneFatture.frmElenFattAcquisto;
import gestioneFatture.frmElenOrdini;
import gestioneFatture.iniFileProp;
import gestioneFatture.logic.documenti.Documento;
import gestioneFatture.main;
import it.tnx.Db;
import it.tnx.accessoUtenti.Utente;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.SystemUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import static it.tnx.invoicex.MainBatch.tipi_doc_inv;
import static it.tnx.invoicex.MainBatch.tipo_documento_ddt_acquisto;
import static it.tnx.invoicex.MainBatch.tipo_documento_ddt_vendita;
import static it.tnx.invoicex.MainBatch.tipo_documento_fattura_acquisto;
import static it.tnx.invoicex.MainBatch.tipo_documento_fattura_vendita;
import static it.tnx.invoicex.MainBatch.tipo_documento_ordine_acquisto;
import static it.tnx.invoicex.MainBatch.tipo_documento_ordine_vendita;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.simple.JSONObject;

/**
 *
 * @author Marco
 */
public class MainHttpServer {

    PrintStream origerr = null;
    PrintStream origout = null;
    ByteArrayOutputStream baoserr = new ByteArrayOutputStream();
    ByteArrayOutputStream baosout = new ByteArrayOutputStream();

    public MainHttpServer() {
        System.setProperty("logfile.name", "invoicex.log");

        origerr = new PrintStream(System.err);
        origout = new PrintStream(System.out);
        //per log4j
        System.setErr(new PrintStream(new LoggingOutputStream(Category.getRoot(), Priority.WARN), true));
        System.setOut(new PrintStream(new LoggingOutputStream(Category.getRoot(), Priority.INFO), true));

//        System.setErr(new PrintStream(baoserr));
//        System.setOut(new PrintStream(baosout));        
    }

    public static void main(String[] args) throws Exception {
        MainHttpServer server = new MainHttpServer();
        server.start();
    }

    void start() {
        System.out.println("Invoicex (Server) ver. " + main.version + " " + main.build);
        System.out.flush();

        try {
//            if (file_param != null && this.file_param == null) {
//                this.file_param = file_param;
//            }

            Server server = new Server(9090);

            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath("keystore");
            sslContextFactory.setCertAlias("jetty");
            sslContextFactory.setTrustAll(true);
            sslContextFactory.setValidateCerts(false);
            sslContextFactory.setKeyStorePassword("OBF:1ku51mt71kfv1ku11san1kqt1kcj1mpv1kqp");
            sslContextFactory.setKeyManagerPassword("OBF:1ku51mt71kfv1ku11san1kqt1kcj1mpv1kqp");
            sslContextFactory.setTrustStorePassword("OBF:1ku51mt71kfv1ku11san1kqt1kcj1mpv1kqp");

            SslSelectChannelConnector sslconn = new SslSelectChannelConnector(sslContextFactory);
            sslconn.setPort(9443);
            sslconn.setMaxIdleTime(30000);
            server.addConnector(sslconn);

            /*            
             SslSocketConnector sslConnector =  new SslSocketConnector();
             sslConnector.setPort(9443);
             sslConnector.setKey
             sslConnector.setHost("localhost");
             //            ((AbstractConnector) connector).setThreadPool(new QueuedThreadPool(20));
             server.addConnector (sslConnector);
             */

            /*
             // Create the ResourceHandler. It is the object that will actually handle the request for a given file. It is
             // a Jetty Handler object so it is suitable for chaining with other handlers as you will see in other examples.
             ResourceHandler resource_handler = new ResourceHandler();
             // Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
             // In this example it is the current directory but it can be configured to anything that the jvm has access to.
             resource_handler.setDirectoriesListed(true);
             resource_handler.setWelcomeFiles(new String[]{ "index.html" });
             resource_handler.setResourceBase(".");

             // Add the ResourceHandler to the server.
             HandlerList handlers = new HandlerList();
             handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
             server.setHandler(handlers);
             */
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

//            context.addServlet(new ServletHolder(new HelloServlet()), "/*");
//            context.addServlet(new ServletHolder(new HelloServlet("Buongiorno Mondo")), "/it/*");
//            context.addServlet(new ServletHolder(new HelloServlet("Bonjour le Monde")), "/fr/*");
            context.addServlet(new ServletHolder(new ApiServlet()), "/api/*");

            server.start();
            server.join();

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    static public class HelloServlet extends HttpServlet {

        private String greeting = "Hello World";

        public HelloServlet() {
        }

        public HelloServlet(String greeting) {
            this.greeting = greeting;
        }

        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>" + greeting + "</h1>");
            response.getWriter().println("session=" + request.getSession(true).getId());
        }
    }

    static public class ApiServlet extends HttpServlet {
        // /ordine_vendita/N/calcolaTotali

//http://localhost:9090/api/util/calcolaTotali/ordine_vendita/30        
//http://localhost:9090/api/util/converti/ordine_vendita/30/fattura_vendita
//http://localhost:9090/api/pdf/ordine_vendita/30
        public ApiServlet() {
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            doGet(req, resp);
        }
        
        
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String qs = request.getQueryString();
            System.out.println("query string = " + qs);
            
//            Map dati = request.getParameterMap();
//            System.out.println("getParameterMap:\n");
//            DebugUtils.dump(dati);

            String path_info = request.getPathInfo();
            System.out.println("path_info = " + path_info);
            DebugUtils.dump(path_info);            

            String servlet_path = request.getServletPath();
            System.out.println("servlet_path = " + servlet_path);

            String uri = request.getRequestURL().toString();
            System.out.println("uri = " + uri);

            String[] parts = StringUtils.split(path_info, "/");
            if ("util".equals(parts[0]) && "calcolaTotali".equals(parts[1])) {

                String tipo_documento = parts[2];
                Integer id = cu.i(parts[3]);

                System.out.println("calcola totali " + tipo_documento + " = " + id);

                try {
                    main inv = getMain(request, response);

                    Object ret = calcolaTotali(inv, tipo_documento, id);
                    if (ret instanceof Boolean && (Boolean) ret == true) {
                        JSONObject retj = new JSONObject();
                        retj.put("esito", "ok");
                        ok(retj.toJSONString(), request, response);
                        return;
                    }
                    error(ret == null ? "ret = null" : ret.toString(), request, response);
                } catch (Exception e) {
                    error(e.toString(), request, response);
                }
                return;
            } else if ("pdf".equals(parts[0])) {
                String tipo_documento = parts[1];
                Integer id = cu.i(parts[2]);

                System.out.println("pdf " + tipo_documento + " = " + id);

                try {
                    main inv = getMain(request, response);

                    Object ret = getPdf(inv, tipo_documento, id);
                    if (ret instanceof String) {
                        JSONObject retj = new JSONObject();
                        retj.put("esito", "ok");
                        retj.put("file", ret);
                        ok(retj.toJSONString(), request, response);
                        return;
                    }
                    error(ret == null ? "ret = null" : ret.toString(), request, response);
                } catch (Exception e) {
                    error(e.toString(), request, response);
                }
                return;
            } else if ("util".equals(parts[0]) && "converti".equals(parts[1])) {

                String da_tipo_documento = parts[2];
                Integer da_id = cu.i(parts[3]);
                String a_tipo_documento = parts[4];

                System.out.println("converti " + da_tipo_documento + " " + da_id + " a " + a_tipo_documento);

                try {
                    main inv = getMain(request, response);

                    Object ret = convertiDoc(da_tipo_documento, a_tipo_documento, da_id, null);
                    System.out.println("dump ret convertiDoc");
                    DebugUtils.dump(ret);
                    if (ret instanceof Integer) {
                        JSONObject retj = new JSONObject();
                        retj.put("esito", "ok");
                        retj.put("id", ret);
                        ok(retj.toJSONString(), request, response);
                        return;
                    }
                    error(ret == null ? "ret = null" : ret.toString(), request, response);
                } catch (Exception e) {
                    error(e.toString(), request, response, e);
                }
                return;
            }
        }

        private void error(String errore, HttpServletRequest request, HttpServletResponse response) throws IOException {
            error(errore, request, response, null);
        }

        private void error(String errore, HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
            if (ex != null) {
                ex.printStackTrace();
            }
            System.out.println("error = " + errore);
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(errore);
        }

        private void ok(String json, HttpServletRequest request, HttpServletResponse response) throws IOException {
            System.out.println("json:" + json);
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(json);
        }

        //funzioni
        public Object convertiDoc(String da_tipo_documento, String a_tipo_documento, Integer id, List<Integer> id_righe_da_includere) throws Exception {
            Storico.scriviSeparati("http convertiDoc inizio " + da_tipo_documento + " a " + a_tipo_documento + " id " + id + " id_righe " + id_righe_da_includere, null);

            String da_tipo_doc_inv = tipi_doc_inv.get(da_tipo_documento);
            String a_tipo_doc_inv = tipi_doc_inv.get(a_tipo_documento);

//            JSONObject jp = json_param;
            //da ordine a fattura
            dbOrdine doc = new dbOrdine();
            String sql = "select id, serie, numero, anno from test_ordi where id = " + id;
            System.out.println("sql = " + sql);
            List<Map> list = dbu.getListMap(Db.getConn(), sql);
            DebugUtils.dump(list);
            if (list.isEmpty()) {
                return new Exception("Non trovato documento di origine " + da_tipo_documento + " id " + id);
            }
            Map rec = list.get(0);

            InvoicexUtil.aggiornaTotaliRighe(Db.TIPO_DOCUMENTO_ORDINE, id);

            doc.serie = cu.s(rec.get("serie"));
            
            doc.ids = new Integer[]{id};
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

        public Object getPdf(main inv, String tipo_documento, Integer id) throws Exception {
            String tipo_doc_inv = tipi_doc_inv.get(tipo_documento);

            System.out.println("tipo_doc_inv = " + tipo_doc_inv);
            
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

        public Object calcolaTotali(main inv, String tipo_documento, Integer id) throws Exception {
            Storico.scriviSeparati("calcolaTotali inizio " + tipo_documento + " id " + id, null);

            String tipo_doc = tipi_doc_inv.get(tipo_documento);

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

        private main getMain(HttpServletRequest request, HttpServletResponse response) throws IOException {
            main inv = new main();

            final Map params = new HashMap();
            params.put("pref_tipoStampa", "fattura_mod7_default.jrxml");
            params.put("pref_tipoStampaFA", "fattura_acc_mod7_default.jrxml");
            params.put("pref_tipoStampaDDT", "ddt_mod6_default.jrxml");
            params.put("pref_tipoStampaOrdine", "ordine_mod7_default.jrxml");

            inv.isBatch = true;
            inv.isServer = true;

            File fwd = new File("./");
            try {
                inv.wd = fwd.getCanonicalPath() + File.separator;
                System.out.println("wd = " + inv.wd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //carica da json
            inv.fileIni = new iniFileProp() {

                @Override
                public String getValue(String subject, String variable) {
                    String key = subject + "_" + variable;
                    if (params.containsKey(key)) {
                        return cu.s(params.get(key));
                    } else {
                        System.out.println("!!! getValue : " + subject + " : " + variable);
                    }
                    return cu.s("");
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
                    String key = subject + "_" + variable;
                    if (params.containsKey(key)) {
                        return true;
                    } else {
                        System.out.println("!! existKey : " + subject + " : " + variable);
                    }
                    return false;
                }

            };

            //controllo se forzare i dati di connessione
//                Db.dbServ = (String) db.get("server");
//                Db.dbPort = 3306;
//                int ip = Db.dbServ.indexOf(":");
//                if (ip > 0) {
//                    String server = Db.dbServ.substring(0, ip);
//                    String porta = Db.dbServ.substring(ip + 1, Db.dbServ.length());
//                    try {
//                        Db.dbPort = Integer.parseInt(porta);
//                    } catch (NumberFormatException err) {
//                    }
//                }
//                Db.dbNameDB = (String) db.get("database");
//                Db.dbName = (String) db.get("user");
//                Db.dbPass = (String) db.get("pass");
            //i dati li potrei far passare da headers tipo x-invoicex-server = localhost
                      Db.dbPort = 3306;
//            Db.dbServ = "localhost";
//            Db.dbNameDB = "base_ecommerce_new";
//            Db.dbName = "root";
//            Db.dbPass = "";
            
System.out.println("Imposto server: = " + request.getParameter("param[db][server]"));
            Db.dbServ = request.getParameter("param[db][server]");
            
System.out.println("Imposto db: = " + request.getParameter("param[db][database]"));            
            Db.dbNameDB = request.getParameter("param[db][database]");
            
System.out.println("Imposto user: = " + request.getParameter("param[db][user]"));
            Db.dbName = request.getParameter("param[db][user]");
            
System.out.println("Imposto pass: = " + StringUtils.substring(request.getParameter("param[db][pass]"), 0, 3) + "***");
            Db.dbPass = request.getParameter("param[db][pass]");

            if (SystemUtils.getHostname().equalsIgnoreCase("mceccarelli")) {
                Db.dbServ = "linux";
                Db.dbName = "root";
                Db.dbPass = "osmmc07013010";
            }
            
            if (Db.dbServ.equalsIgnoreCase("localhost")) {
                Db.localSocketAddress = "127.0.0.1";
            }

            //testo connessione
            try {
                Connection testconn = Db.getConn();
                if (testconn == null) {
                    error("errore in connessione mysql (test=null) a:" + Db.dbServ + " db:" + Db.dbNameDB + " user:" + Db.dbName, request, response);
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                error("errore in connessione mysql (exc:" + e.toString() + ") a: " + Db.dbServ + " db:" + Db.dbNameDB + " user:" + Db.dbName, request, response);
                return null;
            }

            inv.utente = new Utente(1);

            //caricamento plugins ?
//            initPlugins();
            return inv;
        }

    }

}
