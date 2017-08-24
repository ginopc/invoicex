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
package gestioneFatture.primaNota;

import gestioneFatture.*;

import gestioneFatture.logic.documenti.*;
import it.tnx.Util;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.invoicex.InvoicexUtil;
import java.sql.ResultSet;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import org.apache.commons.lang.StringUtils;

public class PrimaNotaUtils {

    public static final int TIPO_PERIODO_TRIMESTRALE = 1;
    public static final int TIPO_PERIODO_MENSILE = 2;
    public static final int TIPO_PERIODO_ANNUALE = 3;
    public static final int TIPO_PERIODO_DAL_AL = 4;
    public static int progressivo = 0;
    int tipoLiquidazione;
    int periodo;
    int anno;
    int meseDal = 0;
    int meseAl = 0;
    public TotaliIva totali = new TotaliIva();
    public JProgressBar progressBar = null;
    public JLabel messaggio = null;
    JDialogCompilazioneReport dialog = null;
    
    public List<Map> riva = null;
    
    String dal = "2015-02-01";
    String al = "2015-02-07";

    /**
     * Creates a new instance of PrimaNotaUtils
     */
    public PrimaNotaUtils() {
    }

    public PrimaNotaUtils(JDialogCompilazioneReport dialog) {
        this.dialog = dialog;
        this.progressBar = dialog.jProgressBar1;
        this.messaggio = dialog.jLabel1;
    }

    
    public void generaPrimaNota(int tipoPeriodo, int periodo, int anno, boolean data_reg, boolean scontrini, Date data_dal, Date data_al) {
        this.generaPrimaNota(tipoPeriodo, periodo, anno, data_reg, scontrini, data_dal, data_al, false);
    }
    
    
    public void generaPrimaNota(int tipoPeriodo, int periodo, int anno, boolean data_reg, boolean scontrini, Date data_dal, Date data_al, boolean definitiva) {
        List anomalie = new ArrayList();

        stato(-1, -1, "inizializzazione");

        String sql = "";
        progressivo = 0;
        this.tipoLiquidazione = tipoPeriodo;
        this.periodo = periodo;
        this.anno = anno;

        if (tipoPeriodo == TIPO_PERIODO_TRIMESTRALE) {
            meseDal = periodo * 3 - 2;
            meseAl = periodo * 3;
        }
        
        //elimino la precedente stampa
        sql = "delete from stampa_iva_semplice";
        Db.executeSql(sql);

        //inserisco dalle fatture ricevute
        String campodata = data_reg ? "data_doc" : "data";
        
        String dal = FormatUtils.formatMysqlDate(data_dal);
        String al = FormatUtils.formatMysqlDate(data_al);

        //sql = "select SQL_CALC_FOUND_ROWS t." + campodata + ", t.numero, t.serie, t.anno, t.serie_doc, t.numero_doc, t.importo, c.ragione_sociale, id from test_fatt_acquisto t";
        sql = "select SQL_CALC_FOUND_ROWS t.data_doc, t.data, t.numero, t.serie, t.anno, t.serie_doc, t.numero_doc, t.importo, c.ragione_sociale, t.id, t.imponibile, t.iva, c.codice, c.piva_cfiscale, c.cfiscale, tipo_fattura from test_fatt_acquisto t";
        sql += " left join clie_forn c on t.fornitore = c.codice";

//        if (data_reg) {
//            sql += " where YEAR(data_doc) = " + anno;
//        } else {
//            sql += " where anno = " + anno;
//        }
//        if (tipoPeriodo == TIPO_PERIODO_TRIMESTRALE) {
//            sql += " and month(" + campodata + ") >= " + meseDal + " and month(" + campodata + ") <= " + meseAl;
//        } else if (tipoPeriodo == TIPO_PERIODO_MENSILE) {
//            sql += " and month(" + campodata + ") = " + periodo;
//        } else if (tipoPeriodo == TIPO_PERIODO_DAL_AL) {
//            sql += " and " + campodata + " >= '" + dal + "' and " + campodata + " <= '" + al + "'";
//        }
        sql += " where " + campodata + " >= '" + dal + "' and " + campodata + " <= '" + al + "'";

        if (data_reg) {
            sql += " order by data_doc";
        } else {
            sql += " order by data, serie, numero";
        }
        
        //preparo elenco codici iva
        String sqlwa = "", sqlwv = "";
//        sqlwv += " and t.anno = " + anno;
//        if (data_reg) {
//            sqlwa += " and YEAR(t.data_doc) = " + anno;
//        } else {
//            sqlwa += " and t.anno = " + anno;
//        }        
//        if (tipoPeriodo == TIPO_PERIODO_TRIMESTRALE) {
//            sqlwv += " and month(t.data) >= " + meseDal + " and month(t.data) <= " + meseAl;
//            sqlwa += " and month(t." + campodata + ") >= " + meseDal + " and month(t." + campodata + ") <= " + meseAl;
//        } else if (tipoPeriodo == TIPO_PERIODO_MENSILE) {
//            sqlwv += " and month(t.data) = " + periodo;
//            sqlwa += " and month(t." + campodata + ") = " + periodo;
//        } else if (tipoPeriodo == TIPO_PERIODO_DAL_AL) {
//            sqlwv += " and t.data >= '2015-02-01' and t.data <= '2015-02-07'";
//            sqlwa += " and " + campodata + " >= '" + dal + "' and " + campodata + " <= '" + al + "'";
//        }
        sqlwv += " and t.data >= '" + dal + "' and t.data <= '" + al + "'";
        sqlwa += " and " + campodata + " >= '" + dal + "' and " + campodata + " <= '" + al + "'";
        
        
        String sqlriva = "select distinct codice, percentuale from (select i.codice, i.percentuale from codici_iva i\n"
                + " join righ_fatt r on r.iva = i.codice\n"
                + " join test_fatt t on r.id_padre = t.id\n"
                + " where percentuale > 0 \n"
                + " " + sqlwv
                + " \n"
                + " union all\n"
                + " \n"
                + " select i.codice, i.percentuale from codici_iva i\n"
                + " join righ_fatt_acquisto r on r.iva = i.codice\n"
                + " join test_fatt_acquisto t on r.id_padre = t.id\n"
                + " where percentuale > 0 \n"
                + " " + sqlwa
                + " ) tab \n"
                + " order by percentuale desc";
        try {
            System.out.println("sqlriva = " + sqlriva);
            riva = DbUtils.getListMap(Db.getConn(), sqlriva);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        //aggiungo i codici iva delle spese inseriti nelle impostazioni
        try {
            Map mivaspese = dbu.getListMap(Db.getConn(), "select codiceIvaSpese,codiceIvaBollo from dati_azienda").get(0);
            String codiceIvaSpese = cu.s(mivaspese.get("codiceIvaSpese"));
            if (StringUtils.isNotBlank(codiceIvaSpese)) {
                boolean ivaspesetrovato = false;
                for (Map m : riva) {
                    if (m.get("codice").equals(codiceIvaSpese)) {
                        ivaspesetrovato = true;
                    }
                }
                if (!ivaspesetrovato) {
                    Map mivatoadd = new HashMap();
                    mivatoadd.put("codice", codiceIvaSpese);
                    double perc = cu.d(dbu.getObject(Db.getConn(), "select percentuale from codici_iva where codice = " + dbu.sql(codiceIvaSpese)));
                    if (perc > 0) {
                        mivatoadd.put("percentuale", perc);
                        riva.add(mivatoadd);
                    }
                }
            }

            //test per bollo
            boolean bollipresenti = false;
            String sqltestbollov = "select bollo_presente from test_fatt t \n"
                    + " where IFNULL(bollo_presente,'N') = 'S' \n"
                    + " " + sqlwv + " limit 1";
            try {
                System.out.println("sqltestbollov = " + sqltestbollov);
                if (dbu.containRows(Db.getConn(), sqltestbollov)) {
                    bollipresenti = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (bollipresenti) {
                String codiceIvaBolli = cu.s(mivaspese.get("codiceIvaBollo"));
                if (StringUtils.isNotBlank(codiceIvaBolli)) {
                    boolean ivabollotrovato = false;
                    for (Map m : riva) {
                        if (m.get("codice").equals(codiceIvaBolli)) {
                            ivabollotrovato = true;
                        }
                    }
                    if (!ivabollotrovato) {
                        Map mivatoadd = new HashMap();
                        mivatoadd.put("codice", codiceIvaBolli);
                        double perc = cu.d(dbu.getObject(Db.getConn(), "select percentuale from codici_iva where codice = " + dbu.sql(codiceIvaBolli)));
                        if (perc > 0) {
                            mivatoadd.put("percentuale", perc);
                            riva.add(mivatoadd);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("riva = " + riva);

//        ResultSet r = Db.openResultSet(sql);
        Integer rconta = 0;
        int iconta = 0;
        ResultSet r = null;
        try {
            r = DbUtils.tryOpenResultSet(Db.getConn(), sql);
            try {
                rconta = CastUtils.toInteger0(DbUtils.getObject(Db.getConn(), "select FOUND_ROWS()"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("fatture acquisto:" + sql + " conta:" + rconta);
            stato(0, rconta, "Fatture di acquisto...");
            while (r.next()) {
                iconta++;

                double totale_imponibile = 0;
                double totale_iva = 0;
                double totale_totale = 0;

                //preparo sql di inserimento nella stampa
                String sqli = "insert into stampa_iva_semplice (";
                String sqlic = "tipo";
                String sqliv = "'A'";
                sqlic += ", data";
                //sqliv += ", " + Db.pc(r.getDate(campodata), Types.DATE);
                sqliv += ", " + Db.pc(r.getDate("data"), Types.DATE);
                sqlic += ", data_doc";
                sqliv += ", " + Db.pc(r.getDate("data_doc"), Types.DATE);
                sqlic += ", numero_prog";

                String serie = r.getString("serie");
                String numeroprog = (StringUtils.isBlank(serie) ? "" : serie + "/") + r.getString("numero");
                try {
                    if (r.getInt("anno") >= 2013) {
                        if (InvoicexUtil.getTipoNumerazione() == InvoicexUtil.TIPO_NUMERAZIONE_ANNO) {
                            numeroprog += "/" + r.getString("anno");
                        } else if (InvoicexUtil.getTipoNumerazione() == InvoicexUtil.TIPO_NUMERAZIONE_ANNO_2CIFRE) {
                            numeroprog += "/" + StringUtils.right(r.getString("anno"), 2);
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                sqliv += ", " + Db.pc(numeroprog, Types.VARCHAR);

                sqlic += ", numero_doc";
                sqliv += ", " + Db.pc(r.getString("serie_doc") + " " + r.getString("numero_doc"), Types.VARCHAR);
                sqlic += ", ragione_sociale";
                sqliv += ", " + Db.pc(r.getString("ragione_sociale"), Types.VARCHAR);
                sqlic += ", piva_cfiscale";
                if (!StringUtils.isBlank(r.getString("piva_cfiscale"))) {
                    sqliv += ", " + Db.pc(r.getString("piva_cfiscale"), Types.VARCHAR);
                } else {
                    sqliv += ", " + Db.pc(r.getString("cfiscale"), Types.VARCHAR);
                }
                sqlic += ", id_fattura";
                sqliv += ", " + Db.pc(r.getInt("id"), Types.VARCHAR);
                sqlic += ", id_clifor";
                sqliv += ", " + Db.pc(r.getString("codice"), Types.VARCHAR);

                if (r.getInt("numero") == 16) {
                    System.out.println("stop");
                }

                //calcolo del totale e castelletto iva secondo nuove classi
                Documento doc;
                doc = new Documento();
                doc.load(Db.INSTANCE, r.getInt("numero"), r.getString("serie"), r.getInt("anno"), Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, r.getInt("id"));
                doc.calcolaTotali();

                //if ((Util.round(r.getDouble("importo"), 2) != Util.round(doc.getTotale(), 2)) && (Math.abs(Util.round(r.getDouble("importo"), 2) - Util.round(doc.getTotale(), 2)) >= 1.0D)) {
                if ((Util.round(r.getDouble("importo"), 2) != Util.round(doc.getTotale(), 2)) && (Math.abs(Util.round(r.getDouble("importo"), 2) - Util.round(doc.getTotale(), 2)) >= 0.01D)) {
                    anomalie.add(new Object[]{"Acquisto", Integer.valueOf(r.getInt("id")), r.getString("serie"), Integer.valueOf(r.getInt("numero")), Integer.valueOf(r.getInt("anno")), Double.valueOf(Util.round(r.getDouble("importo"), 2)), Double.valueOf(Util.round(doc.getTotale(), 2))});
                }

                int segno = 1;
                if (r.getInt("tipo_fattura") == dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO) {
                    segno = -1;
                }

                sqlic += ", totale";
                sqliv += ", " + Db.pc(doc.getTotale() * segno, Types.DOUBLE);

                //ciclo per i codici iva descendig senza quelle a zero
                //sql = "select codice from codici_iva where percentuale > 0 order by percentuale desc";

//                Statement stat_riva = Db.getConn().createStatement();
//                ResultSet riva = stat_riva.executeQuery(sql);
                int codiceIva = 0;

                Double impIndeducibile = doc.getImpNonDeducibile() * segno;
                impIndeducibile = Util.round(impIndeducibile, 2);
                String impIndeducibileStr = Db.pc(impIndeducibile, Types.DOUBLE);
                totale_imponibile += impIndeducibile;

                Double ivaIndeducibile = doc.getImpIvaNonDeducibile() * segno;
                ivaIndeducibile = Util.round(ivaIndeducibile, 2);
                String ivaIndeducibileStr = Db.pc(ivaIndeducibile, Types.DOUBLE);
                totale_iva += ivaIndeducibile;

//                while (riva.next()) {
                for (Map miva : riva) {
                
                    codiceIva++;

                    //seleziono i dettagli iva per ogni codice
                    double imponibile = doc.getImpIvaDeducibile(cu.s(miva.get("codice")));

                    if (imponibile != 0) {
                        sqlic += ", imp" + codiceIva;
                        sqlic += ", iva" + codiceIva;

//                        if (Db.nz(r.getString("tipofattura"), "").equals("NC")) {
//                            sqliv += ", " + Db.pc(doc.getImpIva(riva.getString("codice")) * -1, Types.DOUBLE);
//                            sqliv += ", " + Db.pc(doc.getIva(riva.getString("codice")) * -1, Types.DOUBLE);
//                            totali.totaleVendite += doc.getIva(riva.getString("codice")) * -1;
//                        } else {
                        Double imp = doc.getImpIvaDeducibile(cu.s(miva.get("codice"))) * segno;
                        imp = Util.round(imp, 2);
                        String impStr = Db.pc(imp, Types.DOUBLE);
                        totale_imponibile += imp;

                        Double iva = doc.getIvaDeducibile(cu.s(miva.get("codice"))) * segno;
                        iva = Util.round(iva, 2);
                        String ivaStr = Db.pc(iva, Types.DOUBLE);
                        totale_iva += iva;

                        sqliv += ", " + impStr;
                        sqliv += ", " + ivaStr;
                        System.out.println("totaleAcquisti += " + CastUtils.toDouble0(doc.getIvaDeducibile(cu.s(miva.get("codice")))) + " numero:" + r.getInt("numero") + " tot: " + totali.totaleAcquisti);
//                        totali.totaleAcquisti += CastUtils.toDouble0(doc.getIvaDeducibile(riva.getString("codice")));
                        totali.totaleAcquisti += iva;
//                        }
                    }

                }

                sqlic += ", imp_deducibile";
                sqlic += ", iva_deducibile";
                sqliv += ", " + impIndeducibileStr;
                sqliv += ", " + ivaIndeducibileStr;
//                stat_riva.close();

                //ciclo per i codici iva descendig senza quelle a zero
                sql = "select codice from codici_iva where percentuale = 0";

                ResultSet rivaNoIva = Db.openResultSet(sql);
                codiceIva = 0;

                double totImpNoIva = 0;

                while (rivaNoIva.next()) {
                    codiceIva++;

                    //seleziono i dettagli iva per ogni codice
                    double imponibile = 0;

//                    if (Db.nz(r.getString("tipofattura"), "").equals("NC")) {
//                        imponibile = doc.getImpIva(rivaNoIva.getString("codice")) * -1;
//                    } else {
                    imponibile = doc.getImpIva(rivaNoIva.getString("codice")) * segno;
                    totale_imponibile += imponibile;
//                    }

                    if (imponibile != 0) {
                        totImpNoIva += imponibile;
                    }

                }

                sqlic += ", altre_imp";
                sqliv += ", " + Db.pc(totImpNoIva, Types.DOUBLE);
                sqli = sqli + sqlic + ") values (" + sqliv + ")";
                System.out.println(sqli);
//                Db.executeSql(sqli);
                
                Integer lastid = null;
                try {
                    lastid = Db.executeSqlRetIdDialogExc(Db.getConn(), sqli, false, true);
                } catch (Exception e) {
                    SwingUtils.showExceptionMessage(dialog, e);
                }

                //controllo totali registrati in fattura se uguali a totali ricalcolati
                totale_totale = Util.round(totale_imponibile + totale_iva, 2);
                if (Util.round(totale_iva * segno, 2) != r.getDouble("iva") || Util.round(totale_imponibile * segno, 2) != r.getDouble("imponibile") || Util.round(totale_totale * segno, 2) != r.getDouble("importo")) {
                    System.out.println("!!! totale_totale: " + totale_totale + " : " + r.getDouble("importo"));
                    System.out.println("!!! totale_imponibile: " + totale_imponibile + " : " + r.getDouble("imponibile"));
                    System.out.println("!!! totale_iva: " + totale_iva + " : " + r.getDouble("iva"));

                    //rettifica imponibile
                    List<Map> list = DbUtils.getListMap(Db.getConn(), "select * from stampa_iva_semplice where id = " + lastid);
                    Map m = list.get(0);
                    double differenza = (totale_totale * segno) - r.getDouble("importo");
                    String sqldiff = "update stampa_iva_semplice set ";
                    String campo = null;
                    if (cu.i0(m.get("imp_deducibile")) != 0) {
                        campo = "imp_deducibile";
                    } else if (cu.i0(m.get("imp5")) != 0) {
                        campo = "imp5";
                    } else if (cu.i0(m.get("imp4")) != 0) {
                        campo = "imp4";
                    } else if (cu.i0(m.get("imp3")) != 0) {
                        campo = "imp3";
                    } else if (cu.i0(m.get("imp2")) != 0) {
                        campo = "imp2";
                    } else if (cu.i0(m.get("imp1")) != 0) {
                        campo = "imp1";
                    }
                    if (campo != null) {
                        sqldiff += campo + " = " + campo + " - " + differenza;
                        sqldiff += " where id = " + lastid;
                        try {
                            System.out.println("sqldiff = " + sqldiff);
                            DbUtils.tryExecQuery(Db.getConn(), sqldiff);
                        } catch (Exception e) {
                            SwingUtils.showExceptionMessage(dialog, e);
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("!!! !!! !!! Errore durante la rettifica decimali iva !!! !!! !!!");
                    }
                }

                //insPnTesta(r.getDate("data"), "FA", r.getString("numero_doc"), r.getString("serie_doc"), r.getDate("data_doc"), r.getInt("numero"), null);
//                System.out.println(r.getString(1));
                try {
                    // Aggiorno la testa della fattura coinvolta per impostare il blocco se la stampa è in definitivo
                    if(definitiva){
                        if (dbFattura.bloccaFattura(r.getInt("id"), dbFattura.TIPO_FATTURA_ACQUISTO)) {
                            System.out.println("Fattura di acquisto id " + r.getInt("id") + " bloccata");
                        } else {
                            System.err.println("!!! !!! !!! Errore durante il blocco della fattura di acquisto " + r.getInt("id") + " !!! !!! !!!");
                        }
                    }  
                    stato(iconta, rconta, "Fatture di acquisto... " + r.getString("serie") + " " + r.getInt("numero") + "/" + r.getInt("anno"));
                } catch (Exception e) {
                }
            }

        } catch (Exception err) {
            err.printStackTrace();
        }
        try {
            r.getStatement().close();
            r.close();
        } catch (Exception e) {
        }

        stato(-1, -1, "Fatture di vendita... ");

        //inserisco dalle fatture di vendita
        sql = "select SQL_CALC_FOUND_ROWS t.serie, t.numero, t.data, t.totale, t.anno, IF(t.tipo_fattura = 7,'* scontrino *', c.ragione_sociale) as ragione_sociale";
        sql += ", tf.descrizione_breve as tipofattura, t.id, t.tipo_fattura, c.codice, c.piva_cfiscale, c.cfiscale";
        sql += ", tx.split_payment";
        sql += " from test_fatt t";
        sql += " left join test_fatt_xmlpa tx on t.id = tx.id_fattura";
        sql += " left join clie_forn c on t.cliente = c.codice";
        sql += " left join tipi_fatture tf on t.tipo_fattura = tf.tipo";

//        sql += " where anno = " + anno;
//        if (tipoPeriodo == TIPO_PERIODO_TRIMESTRALE) {
//            sql += " and month(data) >= " + meseDal + " and month(data) <= " + meseAl;
//        } else if (tipoPeriodo == TIPO_PERIODO_MENSILE) {
//            sql += " and month(data) = " + periodo;
//        } else if (tipoPeriodo == TIPO_PERIODO_DAL_AL) {
//            sql += " and data >= '" + dal + "' and data <= '" + al + "'";
//        }
        sql += " where data >= '" + dal + "' and data <= '" + al + "'";
        
        sql += " and tf.descrizione_breve != 'FP'";
        
        if (!scontrini) {
            sql += " and t.tipo_fattura != 7";
        }
        sql += " order by data, serie, numero";
//        r = Db.openResultSet(sql);
        System.out.println("sql = " + sql);

        try {
            r = DbUtils.tryOpenResultSet(Db.getConn(), sql);

            rconta = 0;
            try {
                rconta = CastUtils.toInteger0(DbUtils.getObject(Db.getConn(), "select FOUND_ROWS()"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("fatture vendita:" + sql + " conta:" + rconta);

            stato(0, rconta, "Fatture di vendita...");
            iconta = 0;

            while (r.next()) {
                iconta++;

                if (r.getString("numero").equals("1082")) {
                    System.out.println("sss");
                }

                //preparo sql di inserimento nella stampa
                String sqli = "insert into stampa_iva_semplice (";
                String sqlic = "tipo";
                String sqliv = "'V'";
                sqlic += ", data";
                sqliv += ", " + Db.pc(r.getDate("data"), Types.DATE);
                sqlic += ", numero_doc";
                String serie = r.getString("serie");
                String numerodoc = (StringUtils.isBlank(serie) ? "" : serie + "/") + r.getString("numero");
                try {
                    if (r.getInt("anno") >= 2013) {
                        if (InvoicexUtil.getTipoNumerazione() == InvoicexUtil.TIPO_NUMERAZIONE_ANNO) {
                            numerodoc += "/" + r.getString("anno");
                        } else if (InvoicexUtil.getTipoNumerazione() == InvoicexUtil.TIPO_NUMERAZIONE_ANNO_2CIFRE) {
                            numerodoc += "/" + StringUtils.right(r.getString("anno"), 2);
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                sqliv += ", " + Db.pc(numerodoc, Types.VARCHAR);
                sqlic += ", ragione_sociale";
                sqliv += ", " + Db.pc(r.getString("ragione_sociale"), Types.VARCHAR);

                sqlic += ", piva_cfiscale";
                if (!StringUtils.isBlank(r.getString("piva_cfiscale"))) {
                    sqliv += ", " + Db.pc(r.getString("piva_cfiscale"), Types.VARCHAR);
                } else {
                    sqliv += ", " + Db.pc(r.getString("cfiscale"), Types.VARCHAR);
                }
                sqlic += ", id_fattura";
                sqliv += ", " + Db.pc(r.getInt("id"), Types.VARCHAR);
                sqlic += ", id_clifor";
                sqliv += ", " + Db.pc(r.getString("codice"), Types.VARCHAR);

                sqlic += ", totale";
                //calcolo del totale e castelletto iva secondo nuove classi
                Documento doc;
                doc = new Documento();
                Integer tipo_fattura = cu.toInteger0(r.getObject("tipo_fattura"));
                if (tipo_fattura == dbFattura.TIPO_FATTURA_SCONTRINO) {
                    doc.load(Db.INSTANCE, r.getInt("numero"), r.getString("serie"), r.getInt("anno"), Db.TIPO_DOCUMENTO_SCONTRINO, r.getInt("id"));
                } else {
                    doc.load(Db.INSTANCE, r.getInt("numero"), r.getString("serie"), r.getInt("anno"), Db.TIPO_DOCUMENTO_FATTURA, r.getInt("id"));
                }
                doc.calcolaTotali();

                if (Db.nz(r.getString("tipofattura"), "").equals("NC")) {
                    sqliv += ", " + Db.pc(doc.getTotale() * -1, Types.DOUBLE);
                } else {
                    sqliv += ", " + Db.pc(doc.getTotale(), Types.DOUBLE);
                }

//                if ((Util.round(r.getDouble("totale"), 2) != Util.round(doc.getTotale(), 2)) && (Math.abs(Util.round(r.getDouble("totale"), 2) - Util.round(doc.getTotale(), 2)) >= 1.0D)) {
                if ((Util.round(r.getDouble("totale"), 2) != Util.round(doc.getTotale(), 2)) && (Math.abs(Util.round(r.getDouble("totale"), 2) - Util.round(doc.getTotale(), 2)) >= 0.01D)) {
                    anomalie.add(new Object[]{"Vendita", Integer.valueOf(r.getInt("id")), r.getString("serie"), Integer.valueOf(r.getInt("numero")), Integer.valueOf(r.getInt("anno")), Double.valueOf(Util.round(r.getDouble("totale"), 2)), Double.valueOf(Util.round(doc.getTotale(), 2))});
                }

                //ciclo per i codici iva descendig senza quelle a zero
//                sql = "select codice from codici_iva where percentuale > 0 order by percentuale desc";

//                ResultSet riva = Db.openResultSet(sql);
                int codiceIva = 0;

                
                double imp_split = 0;
                double iva_split = 0;
                int segno = 1;
                if (Db.nz(r.getString("tipofattura"), "").equals("NC")) segno = -1;
                
//                while (riva.next()) {
                for (Map miva : riva) {
                    codiceIva++;

                    //seleziono i dettagli iva per ogni codice
                    double imponibile = doc.getImpIva(cu.s(miva.get("codice")));

                    //split payment
                    if (cu.toBoolean(r.getString("split_payment"))) {
                        if (imponibile != 0) {
                            imp_split += doc.getImpIva(cu.s(miva.get("codice"))) * segno;
                            iva_split += doc.getIva(cu.s(miva.get("codice"))) * segno;
                        }                        
                    } else {
                        if (imponibile != 0) {
                            sqlic += ", imp" + codiceIva;
                            sqlic += ", iva" + codiceIva;

                            if (Db.nz(r.getString("tipofattura"), "").equals("NC")) {
                                sqliv += ", " + Db.pc(doc.getImpIva(cu.s(miva.get("codice"))) * -1, Types.DOUBLE);
                                sqliv += ", " + Db.pc(doc.getIva(cu.s(miva.get("codice"))) * -1, Types.DOUBLE);

                                System.out.println("totaleVendite += " + CastUtils.toDouble0(doc.getIva(cu.s(miva.get("codice"))) * -1) + " numero:" + r.getInt("numero") + " totale: " + totali.totaleVendite);
                                totali.totaleVendite += CastUtils.toDouble0(doc.getIva(cu.s(miva.get("codice"))) * -1);
                            } else {
                                sqliv += ", " + Db.pc(doc.getImpIva(cu.s(miva.get("codice"))), Types.DOUBLE);
                                sqliv += ", " + Db.pc(doc.getIva(cu.s(miva.get("codice"))), Types.DOUBLE);

                                System.out.println("totaleVendite += " + CastUtils.toDouble0(doc.getIva(cu.s(miva.get("codice")))) + " numero:" + r.getInt("numero") + " totale: " + totali.totaleVendite);
                                totali.totaleVendite += doc.getIva(cu.s(miva.get("codice")));
                            }
                        }
                    }
                }

                if (cu.toBoolean(r.getString("split_payment"))) {
                    sqlic += ", imp_deducibile";
                    sqliv += ", " + Db.pc(imp_split, Types.DOUBLE);
                    sqlic += ", iva_deducibile";
                    sqliv += ", " + Db.pc(iva_split, Types.DOUBLE);
                }
                

                //ciclo per i codici iva descendig senza quelle a zero
                sql = "select codice from codici_iva where percentuale = 0";

                ResultSet rivaNoIva = Db.openResultSet(sql);
                codiceIva = 0;

                double totImpNoIva = 0;

                while (rivaNoIva.next()) {
                    codiceIva++;

                    //seleziono i dettagli iva per ogni codice
                    double imponibile = 0;

                    if (Db.nz(r.getString("tipofattura"), "").equals("NC")) {
                        imponibile = doc.getImpIva(rivaNoIva.getString("codice")) * -1;
                    } else {
                        imponibile = doc.getImpIva(rivaNoIva.getString("codice"));
                    }

                    if (imponibile != 0) {
                        totImpNoIva += imponibile;
                    }
                }

                sqlic += ", altre_imp";
                sqliv += ", " + Db.pc(totImpNoIva, Types.DOUBLE);
                sqli = sqli + sqlic + ") values (" + sqliv + ")";
                System.out.println(sqli);
//                Db.executeSql(sqli);
                try {
                    DbUtils.tryExecQuery(Db.getConn(), sqli);
                } catch (Exception e) {
                    SwingUtils.showExceptionMessage(dialog, e);
                }

                //insPnTesta(r.getDate("data"), "FA", r.getString("numero_doc"), r.getString("serie_doc"), r.getDate("data_doc"), r.getInt("numero"), null);
//                System.out.println(r.getString(1));
                try {
                    
                    if(definitiva){
                        if (dbFattura.bloccaFattura(r.getInt("id"), dbFattura.TIPO_FATTURA_IMMEDIATA)) {
                            System.out.println("Fattura di vendita id " + r.getInt("id") + " bloccata");
                        } else {
                            System.err.println("!!! !!! !!! Errore durante il blocco della fattura di vendita" + r.getInt("id") + " !!! !!! !!!");
                        }
                    }                    
                    stato(iconta, rconta, "Fatture di vendita... " + r.getString("serie") + " " + r.getInt("numero") + "/" + r.getInt("anno"));
                } catch (Exception e) {
                }

            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        try {
            r.getStatement().close();
            r.close();
        } catch (Exception e) {
        }

        if (anomalie.size() > 0) {
            SwingUtils.showErrorMessage(main.getPadreFrame(), "Riscontrate anomalie su una o più fatture, visualizza il dettaglio dalla prossima videata", "Attenzione", true);

            JDialogAnomalie da = new JDialogAnomalie(main.getPadreFrame(), true);
            da.anomalie = anomalie;
            da.text.setContentType("text/html");
            String bodyRule = "body { font-family: Monospaced; font-size: 14pt; }";

            ((HTMLDocument) da.text.getDocument()).getStyleSheet().addRule(bodyRule);
            String testo = "<html><pre><table>";
            testo = testo + "<tr>";
            testo = testo + "<td>Tipo</td>";
            testo = testo + "<td>ID</td>";
            testo = testo + "<td>Serie</td>";
            testo = testo + "<td>Numero</td>";
            testo = testo + "<td>Anno</td>";
            testo = testo + "<td>Totale registrato</td>";
            testo = testo + "<td>Totale ricalcolato</td>";
            testo = testo + "</tr>";
            for (int i = 0; i < anomalie.size(); i++) {
                Object[] row = (Object[]) (Object[]) anomalie.get(i);
                testo = testo + "<tr>";
                testo = testo + "<td>" + row[0] + "</td>";
                testo = testo + "<td>" + row[1] + "</td>";
                testo = testo + "<td>" + row[2] + "</td>";
                testo = testo + "<td>" + row[3] + "</td>";
                testo = testo + "<td>" + row[4] + "</td>";
                testo = testo + "<td>" + row[5] + "</td>";
                testo = testo + "<td>" + row[6] + "</td>";
                testo = testo + "</tr>";
            }
            testo = testo + "</table></pre></html>";
            da.text.setText(testo);
            da.text.setEditable(false);
            da.setLocationRelativeTo(null);
            da.setVisible(true);
        }
    }

    private void stato(final int progress, final int maxprogress, final String msg) {
        if (progressBar != null && messaggio != null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    try {
                        if (progress == -1) {
                            progressBar.setIndeterminate(true);
                        } else {
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(progress);
                            progressBar.setMaximum(maxprogress);
                        }
                        messaggio.setText(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
