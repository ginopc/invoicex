/**
 * Invoicex Copyright (c) 2005-2016 Marco Ceccarelli, Tnx srl
 *
 * Questo software è soggetto, e deve essere distribuito con la licenza GNU
 * General Public License, Version 2. La licenza accompagna il software o potete
 * trovarne una copia alla Free Software Foundation http://www.fsf.org .
 *
 * This software is subject to, and may be distributed under, the GNU General
 * Public License, Version 2. The license should have accompanied the software
 * or you may obtain a copy of the license from the Free Software Foundation at
 * http://www.fsf.org .
 *
 * -- Marco Ceccarelli (m.ceccarelli@tnx.it) Tnx snc (http://www.tnx.it)
 *
 */
package gestioneFatture.logic.provvigioni;

import it.tnx.Db;
import gestioneFatture.*;
import gestioneFatture.logic.documenti.Documento;
import it.tnx.commons.CastUtils;

import it.tnx.commons.DbUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.invoicex.InvoicexUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.commons.lang.StringUtils;

/**
 *
 *
 *
 * @author marco
 *
 */
public class ProvvigioniFattura implements JRDataSource {

    public Integer documento_id;
    
    private String pagamento_tipo;
    private boolean flag_pagata;
    private double provvigione;
    private int codiceAgente;
    private Agente agente;
    private int numero; //per calcolare il progressivo delle scadenze
    public String ret = "";
    
    private int tipoFattura;
    
    Connection conn = null;
    Integer p_numero = null;
    Integer p_id = null;

    Double ds_importo_provvigione_totale = null;
    Double ds_importo_provvigione_scadenza = null;

    public static final int TIPO_CALCOLO_IMPONIBILE_MENO_SPESE = 0;
    public static final int TIPO_CALCOLO_IMPONIBILE = 1;
    public static final int TIPO_CALCOLO_IVATO = 2;

    public ProvvigioniFattura(Integer documento_id, int codiceAgente, double provvigioni) {
        this(null, documento_id, codiceAgente, provvigioni);
    }

    public ProvvigioniFattura(Connection conn, Integer documento_id, int codiceAgente, double provvigioni) {        
        this.conn = conn;
        this.documento_id = documento_id;
        this.codiceAgente = codiceAgente;
        this.provvigione = provvigioni;
        try {
            Map fattura = dbu.getListMap(Db.getConn(), "select tipo_fattura from test_fatt where id = " + documento_id).get(0);
            this.tipoFattura = cu.i(fattura.get("tipo_fattura"));
        } catch (Exception ex) {
            SwingUtils.showExceptionMessage(main.getPadreFrame(), ex);
        }
    }

    public ProvvigioniFattura(Integer documento_id) throws Exception {
        this.documento_id = documento_id;
        Map fattura = dbu.getListMap(Db.getConn(), "select * from test_fatt where id = " + documento_id).get(0);
        this.codiceAgente = cu.i(fattura.get("agente_codice"));
        this.provvigione = cu.d(fattura.get("agente_percentuale"));
        this.tipoFattura = cu.i(fattura.get("tipo_fattura"));
        getTotaleProvvigioni(true);
    }

    public boolean generaProvvigioni() {
        return generaProvvigioni(null, null);
    }

    public boolean generaProvvigioni(Date oldDataScadenza, Date nuovaDataScadenza) {
        Connection lconn = null;
        if (conn != null) {
            lconn = conn;
        } else {
            lconn = Db.getConn();
        }

        String sql;
        this.numero = 0;

        //le provvigioni vengono generate insieme alle scadenze di pagamento nel senso che le date di pagamento delle provvigioni
        //vanno insieme alle date di scadenza delle scadenze di pagamento
        //oppure se impostato nelle impostazioni si possono generare alla data del documento
        boolean per_scadenze = true;
        try {
            String provvigioni_tipo_data = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select provvigioni_tipo_data from dati_azienda"));
            if (provvigioni_tipo_data.equalsIgnoreCase("data_fattura")) {
                per_scadenze = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //seleziono numero ultimo +1;
        java.sql.Statement stat;
        ResultSet resu;
        Date data_fattura = null;

        try {

            //elimino le pecedenti provvigioni
            sql = "select * from provvigioni";
            sql += " where id_doc = " + Db.pc(this.documento_id, Types.INTEGER);
            sql += " and pagata = 'S'";
            if (DbUtils.containRows(lconn, sql)) {
                SwingUtils.showErrorMessage(main.getPadre(), "Attenzione sono state rigenerate le provvigioni ma alcune erano già pagate e sono state reinserite come da pagare !");
//                ret = "non generate perchè ci sono già pagate";
//                return false;
            }

            //elimino le pecedenti provvigioni
            sql = "delete from provvigioni";
            sql += " where id_doc = " + Db.pc(this.documento_id, Types.INTEGER);
            Db.executeSql(lconn, sql);

            //controllo che ci sia il codice agente
            stat = lconn.createStatement();
            sql = "select agente_codice from test_fatt";
            sql += " where id = " + Db.pc(this.documento_id, Types.INTEGER);
            resu = stat.executeQuery(sql);
            if (resu.next()) {
                if (StringUtils.isEmpty(resu.getString(1))) {
                    ret = "non generate codice agente vuoto";
                    stat.close();
                    return false;
                }
            }

            //apre il resultset per ultimo +1
            try {
                stat = lconn.createStatement();
                sql = "select numero from provvigioni";
                sql += " where id_doc = " + Db.pc(this.documento_id, Types.INTEGER);
                sql += " order by numero desc limit 1";
                resu = stat.executeQuery(sql);

                if (resu.next() == true) {
                    this.numero = (resu.getInt(1) + 1);
                } else {
                    this.numero = 1;
                }
            } catch (Exception err) {
                if (!main.isBatch) {
                    javax.swing.JOptionPane.showMessageDialog(null, err.toString());
                }
            }

            //prima ricalcolo l'importo delle scadenze al netto delle spese accessorie
            int numeroScadenze = 0;

            try {
                if (per_scadenze) {
                    stat = lconn.createStatement();
                    sql = "select id from scadenze";
                    sql += " where id_doc = " + Db.pc(this.documento_id, Types.INTEGER);
                    resu = stat.executeQuery(sql);
                    while (resu.next()) {
                        numeroScadenze++;
                    }
                } else {
                    numeroScadenze = 1;
                }

                //controllo il tipo di calcolo da fare
                int tipo_calcolo = cu.i0(dbu.getObject(Db.getConn(), "select provvigioni_tipo_calcolo from dati_azienda"));
                Totali tot = calcolaTotali(lconn, documento_id, numeroScadenze, tipo_calcolo);

                //seleziono le scadenze e rigenero le provvigioni in base a quelle
                if (per_scadenze) {
                    try {
                        stat = lconn.createStatement();
                        sql = "select scadenze.*, test_fatt.agente_percentuale, test_fatt.id as t_id, test_fatt.totale_da_pagare_finale";
                        sql += " from scadenze ";
                        sql += " left join test_fatt on test_fatt.id = scadenze.id_doc and scadenze.documento_tipo = '" + Db.TIPO_DOCUMENTO_FATTURA + "'";
                        sql += " where documento_tipo = '" + Db.TIPO_DOCUMENTO_FATTURA + "'";
                        sql += " and id_doc = " + documento_id;
                        sql += " order by numero";
                        List<Map> list = dbu.getListMap(lconn, sql);

                        //calcolo totale scadenza
                        double totale_scadenze = 0;
                        for (Map m : list) {       
                            totale_scadenze += cu.d(m.get("importo"));
                        }                 
                        
                        double somma_base = 0;
                        double somma_prov = 0;
                        int conta_scadenze = 0;
                        for (Map m : list) {     
                            conta_scadenze++;
                            
                            //inserisciProvvigione(lconn, tot.base_provvigione_per_scadenza, tot.importo_provvigione_per_scadenza, resu.getDate("data_scadenza"));                            
                            //calcolo importo provvigione in proporzione all'importo della scadenza
                            double id_scadenza = cu.i(m.get("id"));
                            double perc = cu.d(m.get("importo")) / totale_scadenze;
                            System.out.println("perc scadenza " + perc);
                            
                            double base = tot.base_provvigione_totale * perc;
                            double prov = tot.importo_provvigione_totale * perc;
                            if (conta_scadenze == list.size()) {
                                base = tot.base_provvigione_totale - somma_base;
                                prov = tot.importo_provvigione_totale - somma_prov;
                            } else {
                                somma_base += it.tnx.Util.round(base, 2);
                                somma_prov += it.tnx.Util.round(prov, 2);
                            }
                            
                            inserisciProvvigione(lconn, it.tnx.Util.round(base, 2), it.tnx.Util.round(prov, 2), cu.toDate(m.get("data_scadenza")), cu.i(m.get("id")));
                        }
                    } catch (Exception err) {
                        err.printStackTrace();
                        SwingUtils.showExceptionMessage(main.getPadreFrame(), err);
                    }
                } else {
                    data_fattura = (Date) cu.toDate(dbu.getObject(lconn, "select data from test_fatt where id = " + documento_id));
                    try {
                        inserisciProvvigione(lconn, tot.base_provvigione_totale, tot.importo_provvigione_totale, data_fattura, null);
                    } catch (Exception err) {
                        err.printStackTrace();
                        SwingUtils.showExceptionMessage(main.getPadreFrame(), err);
                    }
                }

                System.out.println("--- totali provvigioni ---");
                System.out.println("base totale        = " + tot.base_provvigione_totale);
                System.out.println("provvigione totale = " + tot.importo_provvigione_totale);

            } catch (Exception err) {
                err.printStackTrace();
                if (!main.isBatch) {
                    SwingUtils.showExceptionMessage(main.getPadreFrame(), err);
                }
            }

            //Storico.scrivi("Genera Provvigioni Errore", "Documento = " + documento_serie + "/" + documento_numero + "/" + documento_anno + ", Pagamento = " + pagamento_tipo + ", Importo documento = " + documento_importo);
        } catch (Exception err) {
            err.printStackTrace();

            return false;
        }

        return true;
    }

    static public class Totali {
        double base_provvigione_totale;
        double importo_provvigione_totale;
    }

    public Totali calcolaTotali(Connection conn, int id_fattura, int numeroScadenze, int tipo_calcolo) throws Exception {
        Totali tot = new Totali();

        //elimino dettagli precedenti
        String sql = "delete from provvigioni_dettagli where id_test_fatt = " + id_fattura;
        try {
            dbu.tryExecQuery(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
            if (!main.isBatch) {
                SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
            }
        }

        List<Map> list_dettagli = new ArrayList();
        if (tipo_calcolo == ProvvigioniFattura.TIPO_CALCOLO_IVATO || tipo_calcolo == ProvvigioniFattura.TIPO_CALCOLO_IMPONIBILE) {
            //base su totale ivato documento
            String campo_testata = "totale";
            if (tipo_calcolo == ProvvigioniFattura.TIPO_CALCOLO_IMPONIBILE) {
                campo_testata = "totale_imponibile";
            }
            
            tot.base_provvigione_totale = cu.d0(dbu.getObject(Db.getConn(), "select " + campo_testata + " from test_fatt where id = " + id_fattura));
            tot.base_provvigione_totale = it.tnx.Util.round(tot.base_provvigione_totale, 2);
            
            if (tot.base_provvigione_totale != 0) {

                String campo = "totale_ivato";
                if (tipo_calcolo == ProvvigioniFattura.TIPO_CALCOLO_IMPONIBILE) {
                    campo = "totale_imponibile";
                }

                InvoicexUtil.aggiornaPrezziNettiUnitari("righ_fatt", "test_fatt", id_fattura);

                //calcolare proporzione delle provvigioni
                sql = "select provvigione, sum(prezzo_netto_totale) as totale_imponibile, sum(prezzo_ivato_netto_totale) as totale_ivato \n"
                        + "from righ_fatt where id_padre = " + id_fattura + "\n"
                        + "group by provvigione";
                List<Map> rows = DbUtils.getListMap(conn, sql);
                double totale_righe = 0;
                for (Map row : rows) {
                    totale_righe += cu.d0(row.get(campo));
                }
                double prop = tot.base_provvigione_totale / totale_righe;
                sql = "select id, provvigione, prezzo_netto_totale as totale_imponibile, prezzo_ivato_netto_totale as totale_ivato \n"
                        + "from righ_fatt where id_padre = " + id_fattura;
                rows = DbUtils.getListMap(conn, sql);
                for (Map row : rows) {
                    double base_per_provv = cu.d0(row.get(campo)) * prop;
                    double importo_provvigione = base_per_provv / 100d * cu.d0(row.get("provvigione"));
                    tot.importo_provvigione_totale += importo_provvigione;
                    //preparo dettagli per inserire in provvigioni_dettagli
                    Map r = new HashMap();
                    r.put("id_test_fatt", id_fattura);
                    r.put("tipo_calcolo", tipo_calcolo);
                    r.put("id_riga", row.get("id"));
                    r.put("base_provvigione", base_per_provv);
                    r.put("perc_provvigione", cu.d0(row.get("provvigione")));
                    r.put("importo_provvigione", importo_provvigione);
                    String sqldett = "insert into provvigioni_dettagli set " + dbu.prepareSqlFromMap(r);
                    try {
                        dbu.tryExecQuery(Db.getConn(), sqldett);
                    } catch (Exception e) {
                        if (!main.isBatch) {
                            SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
                        }
                        e.printStackTrace();
                        return null;
                    }
                    list_dettagli.add(r);
                }
                System.out.println("list_dettagli = " + list_dettagli);
                tot.importo_provvigione_totale = it.tnx.Util.round(tot.importo_provvigione_totale, 2);
            } else {
                tot.importo_provvigione_totale = 0;
            }
        } else {
            //ProvvigioniFattura.TIPO_CALCOLO_IMPONIBILE_MENO_SPESE
            //Nuovo Calcolo per RIGA
            //calcolo in base alle righe
            sql = "select serie, numero, anno, cliente, data, sconto1 as sc1t, sconto2 as sc2t, sconto3 as sc3t,";
            sql += " sconto, totale_imponibile, spese_varie, spese_trasporto, spese_incasso";
            sql += " from test_fatt where id = " + id_fattura;
            Map rowtesta = DbUtils.getListMap(Db.getConn(), sql).get(0);
            double base_totale = cu.d0(rowtesta.get("totale_imponibile")) - cu.d0(rowtesta.get("spese_varie")) - cu.d0(rowtesta.get("spese_trasporto")) - cu.d0(rowtesta.get("spese_incasso"));
            
            if (base_totale != 0) {
                sql = "select id, quantita, prezzo, sconto1, sconto2, provvigione from righ_fatt";
                sql += " where id_padre = " + id_fattura;
                List<Map> rows = DbUtils.getListMap(conn, sql);
                for (Map row : rows) {
                    double tot_riga = CastUtils.toDouble0(row.get("quantita")) * CastUtils.toDouble0(row.get("prezzo"));
                    tot_riga = tot_riga - (tot_riga / 100d * CastUtils.toDouble0(row.get("sconto1")));
                    tot_riga = tot_riga - (tot_riga / 100d * CastUtils.toDouble0(row.get("sconto2")));

                    tot_riga = tot_riga - (tot_riga / 100d * cu.d0(rowtesta.get("sc1t")));
                    tot_riga = tot_riga - (tot_riga / 100d * cu.d0(rowtesta.get("sc2t")));
                    tot_riga = tot_riga - (tot_riga / 100d * cu.d0(rowtesta.get("sc3t")));

                    System.out.println("tot_riga = " + tot_riga);

                    //tolgo una proporzione in base all'importo riga con il totale(al lordo sconto) dello sconto totale se presente
                    double sconto = cu.d0(rowtesta.get("sconto"));
                    if (sconto > 0) {
                        //importo_riga : totale_lordo = x : sconto_a_importo -> x = importo_riga * sconto_a_importo / totale_lordo
                        double sconto_proporzionato = tot_riga * sconto / (base_totale + sconto);
                        tot_riga -= sconto_proporzionato;
                    }

                    //preparo dettagli per inserire in provvigioni_dettagli
                    Map r = new HashMap();
                    r.put("id_test_fatt", id_fattura);
                    r.put("tipo_calcolo", tipo_calcolo);
                    r.put("id_riga", row.get("id"));
                    r.put("base_provvigione", tot_riga);
                    r.put("perc_provvigione", cu.i0(row.get("provvigione")));
                    r.put("importo_provvigione", tot_riga / 100d * CastUtils.toDouble0(row.get("provvigione")));
                    String sqldett = "insert into provvigioni_dettagli set " + dbu.prepareSqlFromMap(r);
                    try {
                        dbu.tryExecQuery(Db.getConn(), sqldett);
                    } catch (Exception e) {
                        if (!main.isBatch) {
                            SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
                        }
                        e.printStackTrace();
                        return null;
                    }
                    list_dettagli.add(r);

                    tot.base_provvigione_totale += tot_riga;
                    tot.importo_provvigione_totale += tot_riga / 100d * CastUtils.toDouble0(row.get("provvigione"));
                }
                tot.base_provvigione_totale = it.tnx.Util.round(tot.base_provvigione_totale, 2);
                tot.importo_provvigione_totale = it.tnx.Util.round(tot.importo_provvigione_totale, 2);
            } else {
                tot.base_provvigione_totale = 0;
                tot.importo_provvigione_totale = 0;
            }
        }

        return tot;
    }

    private void inserisciProvvigione(double importo, double importoProvvigione, java.util.Date scadenza, Integer id_scadenza) {
        inserisciProvvigione(null, importo, importoProvvigione, scadenza, id_scadenza);
    }

    private void inserisciProvvigione(Connection conn, double importo, double importoProvvigione, java.util.Date scadenza, Integer id_scadenza) {
        Connection lconn = null;
        if (conn != null) {
            lconn = conn;
        } else {
            lconn = Db.getConn();
        }

        if (tipoFattura == dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO) {
            importo = -importo;
            importoProvvigione = -importoProvvigione;
        }

        String sql = "";

        try {
            sql = "insert into provvigioni (";
            sql += "id_doc";
            sql += ",id_scadenza";
            sql += ",data_scadenza";
            sql += ",pagata";
            sql += ",importo";
            sql += ",importo_provvigione";
            sql += ",numero) values (";

            //valori
            sql += Db.pc(this.documento_id, Types.INTEGER);
            sql += "," + Db.pc(id_scadenza, Types.INTEGER);
            sql += "," + Db.pc(Db.formatDataMysql(scadenza), Types.DATE);
            sql += "," + Db.pc("N", Types.VARCHAR);
            sql += "," + Db.pc(importo, Types.DOUBLE);
            sql += "," + Db.pc(importoProvvigione, Types.DOUBLE);
            sql += "," + Db.pc(this.numero, Types.INTEGER);
            sql += ")";

            Db.executeSql(lconn, sql);

            //storico
            try {
                Map map = InvoicexUtil.getSerieNumeroAnno(Db.TIPO_DOCUMENTO_FATTURA, documento_id);
                Storico.scrivi(conn, "Genera Provvigioni Inserisci", "Documento = " + map.get("serie") + "/" + map.get("numero") + "/" + map.get("anno") + ", Importo = " + importo + ", Numero = " + numero + ", Data = " + scadenza + ", id_scadenza=" + id_scadenza + ", Importo Provvigione= " + importoProvvigione);
            } catch (Exception e) {
                System.err.println("errore in storico: " + e.getMessage());
            }

            this.numero++;
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    List<Map> rows = null;
    
    public double getTotaleProvvigioni() throws Exception {
        return getTotaleProvvigioni(false);
    }

    public double getTotaleProvvigioni(boolean ds) throws Exception {
        String sql;
        java.sql.Statement stat;
        ResultSet resu;
        double importoTotale2 = 0;

        double sc1t = 0;
        double sc2t = 0;
        double sc3t = 0;

        boolean per_scadenze = true;
        try {
            String provvigioni_tipo_data = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select provvigioni_tipo_data from dati_azienda"));
            if (provvigioni_tipo_data.equalsIgnoreCase("data_fattura")) {
                per_scadenze = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int numeroScadenze = 1;
        if (per_scadenze) {
            numeroScadenze = InvoicexUtil.getNumeroScadenze(documento_id);
        }

        //controllo il tipo di calcolo da fare        
        Integer tipo_calcolo = cu.i(dbu.getObject(Db.getConn(), "select tipo_calcolo from provvigioni_dettagli where id_test_fatt = " + documento_id, false));
        if (tipo_calcolo == null) {
            tipo_calcolo = TIPO_CALCOLO_IMPONIBILE_MENO_SPESE;
        }
        Totali tot = calcolaTotali(Db.getConn(), documento_id, numeroScadenze, tipo_calcolo);
        ds_importo_provvigione_totale = tot.importo_provvigione_totale;
//        ds_importo_provvigione_scadenza = tot.importo_provvigione_per_scadenza;

        try {
            //prendo sconti di testata
            //calcolo in base alle righe
            sql = "select t.serie, t.numero, t.anno, t.cliente, t.data, t.sconto1 as sc1t, t.sconto2 as sc2t, t.sconto3 as sc3t,";
            sql += " r.id as rigaid, r.riga, r.quantita, r.prezzo, r.provvigione, r.sconto1 as sc1r, r.sconto2 as sc2r ,";
            sql += " r.codice_articolo, r.descrizione, t.sconto, t.totale_imponibile, t.spese_varie, t.spese_trasporto, t.spese_incasso";
            sql += " , pd.base_provvigione as imp_riga, pd.importo_provvigione as imp_prov";
            sql += " from test_fatt t left join righ_fatt r on t.id = r.id_padre";
            sql += " left join provvigioni_dettagli pd on t.id = pd.id_test_fatt and r.id = pd.id_riga";
            sql += " where t.id = " + documento_id;
            sql += " order by r.riga";

            //sconti di testata
            rows = DbUtils.getListMap(Db.getConn(), sql);

            return tot.importo_provvigione_totale;
        } catch (Exception err) {
            err.printStackTrace();
            if (!main.isBatch) {
                SwingUtils.showExceptionMessage(main.getPadreFrame(), err);
            }
        }
        return 0;
    }

    private int current = 0;

    public boolean next() throws JRException {
        current++;
        if (current > rows.size()) {
            return false;
        }
        return true;
    }

    public Object getFieldValue(JRField jrf) throws JRException {
        try {
            if (jrf.getName().equals("importoProvvigione2")) {
                return ds_importo_provvigione_totale;
            }
            if (jrf.getName().equals("importoProvvigione2Scadenza")) {
                return ds_importo_provvigione_scadenza;
            }
            return rows.get(current - 1).get(jrf.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static public ProvvigioniFattura getDs(Object id_fattura) throws Exception {
        return new ProvvigioniFattura(((Long) id_fattura).intValue());
    }
}
