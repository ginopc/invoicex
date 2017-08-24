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
package gestioneFatture;

import static gestioneFatture.InvoicexEvent.TYPE_GENERIC_ConvDocRiga;
import gestioneFatture.logic.documenti.DettaglioIva;
import gestioneFatture.logic.documenti.Documento;
import gestioneFatture.logic.provvigioni.ProvvigioniFattura;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbCacheResultSet;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugFastUtils;
import it.tnx.commons.KeyValuePair;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.Magazzino;
import it.tnx.invoicex.data.Giacenza;
import it.tnx.invoicex.gui.JDialogSceltaQuantita;
import it.tnx.invoicex.iu;
import it.tnx.invoicex.sync.Sync;
import java.math.BigDecimal;
import java.sql.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import tnxbeans.*;
import org.jdesktop.swingworker.SwingWorker;

public class dbDocumento {

    private Db dbUtil = Db.INSTANCE;
    public String serie;
    public int numero;
    public String stato;
    public int anno;
    private int id;
    public tnxTextField texTota;
    public tnxTextField texTotaIva;
    public tnxTextField texTotaImpo;
    public double sconto1 = 0;
    public double sconto2 = 0;
    public double sconto3 = 0;
    public double speseVarie = 0;
    public double speseTrasportoIva = 0;
    public double speseIncassoIva = 0;
    public double totale;
    public double totaleIva;
    public double totaleImponibile;
    public String dbStato = "L";
    public static String DB_INSERIMENTO = "I";
    public static String DB_MODIFICA = "M";
    public static String DB_LETTURA = "L";
    public String tipoDocumento = Db.TIPO_DOCUMENTO_FATTURA;
    public Integer tipoFattura = dbFattura.TIPO_FATTURA_IMMEDIATA;
    private gestioneFatture.logic.clienti.Cliente cliente;
    int contaRighe;
    SimpleDateFormat sdf;
    boolean acquisto = false;
    public Integer[] ids;
    Map cacheArticoli = null;
    Map cacheQtaLotti = null;
    Map cacheQtaMatricole = null;
    private boolean useSerie = true;
    public static Long articoli = null;
    private static boolean fareCacheArticoli = true;

    Set<String> articoli_movimentati = new HashSet();

    String table_righe_temp = null;

    public dbDocumento() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public boolean dbRicalcolaProgressivo(String stato, String data, JTextComponent texNumePrev, JTextComponent texAnno, String serie, Integer id) {

        if (stato.equals(frmTestDocu.DB_INSERIMENTO)) {

            //ricreo campo data
            DateFormat myFormat = new SimpleDateFormat("dd/MM/yy");
            java.util.GregorianCalendar myDate = new java.util.GregorianCalendar();
            myFormat.setLenient(false);

            try {
                myDate.setTime(myFormat.parse(data));

                //calcola il progressivo in base alla data e anno
                String sql = "select numero from ";

                if (this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT)) {
                    sql += "test_ddt";
                } else if (this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
                    sql += "test_ddt_acquisto";
                } else if (this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
                    sql += "test_fatt_acquisto";
                } else {
                    sql += "test_fatt";
                }
                int myanno = myDate.get(Calendar.YEAR);
                if ((this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA) || this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA))
                        && (InvoicexUtil.getTipoNumerazione() == InvoicexUtil.TIPO_NUMERAZIONE_ANNO_INFINITA && myanno >= 2013)) {
                    sql += " where anno >= 2013";
                } else {
                    sql += " where anno = " + myanno;
                }
                sql += " and serie = " + Db.pc(serie, Types.VARCHAR);
                if (id != null) {
                    sql += " and id != " + Db.pc(id, Types.INTEGER);
                }
                sql += " order by numero desc limit 1";

                ResultSet resu = Db.openResultSet(sql);

                if (resu.next() == true) {

                    if (texNumePrev.getText().length() == 0 || !texNumePrev.getText().equalsIgnoreCase(String.valueOf(resu.getInt(1) + 1))) {
                        texNumePrev.setText(String.valueOf(resu.getInt(1) + 1));
                    }
                } else {
                    texNumePrev.setText("1");
                }

                texAnno.setText(String.valueOf(myDate.get(Calendar.YEAR)));

                return (true);
            } catch (Exception err) {
                err.printStackTrace();

                return (false);
            }
        }

        return (true);
    }

    public void setStampato(String tipoDocumento) {

        //memorizzo data di stampa
        String sql = "";

        if (tipoDocumento.equals(Db.TIPO_DOCUMENTO_DDT)) {
            sql = "update test_ddt set stampato = '" + Db.getCurrDateTimeMysql() + "'";
        } else if (tipoDocumento.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
            sql = "update test_ddt_acquisto set stampato = '" + Db.getCurrDateTimeMysql() + "'";
        } else if (tipoDocumento.equals(Db.TIPO_DOCUMENTO_FATTURA)) {
            sql = "update test_fatt set stampato = '" + Db.getCurrDateTimeMysql() + "'";
        }

        sql += " where serie = " + dbUtil.pc(this.serie, "VARHCAR");
        sql += " and numero = " + dbUtil.pc(String.valueOf(this.numero), "LONG");
        //sql += " and stato = " + dbUtil.pc(String.valueOf(this.stato), "VARCHAR");
        sql += " and anno = " + Db.pc(Calendar.getInstance().get(Calendar.YEAR), "INTEGER");
        Db.executeSql(sql);
    }

    public void dbRefresh() {
        //debug
        System.out.println("!!! richiamata la routine dbPreventivo.dbRefresh()");

        //calcolo del totale e castelletto iva secondo nuove classi
        gestioneFatture.logic.documenti.Documento doc = new gestioneFatture.logic.documenti.Documento();
        doc.table_righe_temp = table_righe_temp;
        doc.load(Db.INSTANCE, numero, serie, anno, this.tipoDocumento, id);
        doc.calcolaTotali();
        doc.visualizzaCastellettoIva();
        this.totale = doc.getTotale();
        this.totaleImponibile = doc.getTotaleImponibile();
        this.totaleIva = doc.getTotaleIva();
        this.speseVarie = doc.getSpeseVarieImponibili();
        this.speseTrasportoIva = doc.getSpeseTrasporto();
        this.speseIncassoIva = doc.getSpeseIncasso();
    }

    public String convertiInfattura(boolean raggr, boolean raggr_riepilogo) {
        return convertiInFattura(raggr, raggr_riepilogo, true);
    }

    public String convertiInFattura(boolean raggr, boolean raggr_riepilogo, boolean use_serie) {
        return convertiInFattura(raggr, raggr_riepilogo, use_serie, false);
    }

    public String convertiInFattura(boolean raggr, boolean raggr_riepilogo, boolean use_serie, boolean notaDiCredito) {
        return convertiInFattura(raggr, raggr_riepilogo, use_serie, notaDiCredito, Calendar.getInstance(), null);
    }

    public String convertiInFattura(boolean raggr, boolean raggr_riepilogo, boolean use_serie, boolean notaDiCredito, Calendar calendarDataFattura, SwingWorker worker) {
        //!!! provare con wds per chi ha invoicex online (velocizzerebbe)

        Connection conn = Db.getConn();

        //converte in Fattura...
        //cerco ultimo numero ordine
        String newSerie = "";
        Integer newNumero = null;
        String newStato;
        String sql;
        String sqlC = "";
        String sqlV = "";
        double speseIncasso = 0;
        double speseTrasporto = 0;
        double scontoTotale = 0;
        double accontoTotale = 0;
        String codiceCliente = "";
        String pagamento = "";
        Integer giorno_pagamento = null;
        String banca_abi = "";
        String banca_cab = "";
        String banca_cc_iban = "";
        Integer agente_codice = null;
        Double agente_percentuale = null;

        this.useSerie = use_serie;

        String campo_in = "in_fatt";

        sdf = new SimpleDateFormat("dd/MM/yyyy");

        String test_fatt = acquisto ? "test_fatt_acquisto" : "test_fatt";
        String test_ddt = acquisto ? "test_ddt_acquisto" : "test_ddt";
        String righ_ddt = acquisto ? "righ_ddt_acquisto" : "righ_ddt";
        String tipo_doc = acquisto ? Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA : Db.TIPO_DOCUMENTO_FATTURA;
        String righ_fatt = acquisto ? "righ_fatt_acquisto" : "righ_fatt";

        //ordino ids secondo preferenze (in ordine crescente di data)
        sql = "select id from " + test_ddt + " t where id IN (" + StringUtils.join(ids, ",") + ") order by data, serie, numero";
        try {
            List ids2 = dbu.getList(conn, sql);
            ids = new Integer[ids2.size()];
            for (int i = 0; i < ids2.size(); i++) {
                ids[i] = cu.i(ids2.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!notaDiCredito && main.fileIni.getValue("pref", "riporta_serie", "0").equals("3")) {
            JDialogSelezionaSerie diaSerie = new JDialogSelezionaSerie(main.getPadre(), true);
            diaSerie.pack();
            diaSerie.setLocationRelativeTo(null);
            diaSerie.setVisible(true);

            newSerie = diaSerie.serie;
        }

        if (notaDiCredito || !main.fileIni.getValue("pref", "riporta_serie", "0").equals("3")) {
            newSerie = Db.nz(this.useSerie ? this.serie : (notaDiCredito ? "#" : ""), "");
        }

        int anno_fattura = calendarDataFattura.get(Calendar.YEAR);
        sql = "select numero from " + test_fatt;
        if (InvoicexUtil.getTipoNumerazione() == InvoicexUtil.TIPO_NUMERAZIONE_ANNO_INFINITA && anno_fattura >= 2013) {
            sql += " where anno >= 2013";
        } else {
            sql += " where anno = " + anno_fattura;
        }
        sql += " and serie = " + Db.pc(Db.nz(newSerie, ""), "VARCHAR");
        if (!acquisto) {
            sql += " and tipo_fattura != 7";
        }
        sql += " order by numero desc limit 1";
        try {
            newNumero = cu.i(DbUtils.getObject(conn, sql));
        } catch (Exception e) {
        }
        if (newNumero != null) {
            newNumero++;
        } else {
            newNumero = 1;
        }

        newStato = "P";

        //inserisco nuova fattura salvandomi i dati su hashtable
        //e selzionando dai ddt
        contaRighe = 0;
        sql = "select * from " + test_ddt + " t ";

        //selezion da elenco vettore
        /*
         sql += " where serie = " + Db.pc(serie,"VARCHAR");
         sql += " and numero = " + Db.pc(String.valueOf(numero),"NUMERIC");
         sql += " and stato = 'P'";*/
        sql += " where t.id = " + ids[0];

        sql += " order by serie, anno, numero";

        //***
        System.out.println("sql origine conversione:\n" + sql);
        ResultSet tempPrev = Db.openResultSet(sql);
        ResultSet ddtPrev = tempPrev;
        pagamento = null;

        String campoc = "cliente";
        if (acquisto) {
            campoc = "fornitore";
        }

        Integer id = null;

        try {
            ResultSetMetaData metaPrev = tempPrev.getMetaData();
            boolean flag = true;

            if (tempPrev.next() == true) {
                codiceCliente = tempPrev.getString(campoc);
                try {
                    giorno_pagamento = CastUtils.toInteger0(DbUtils.getObject(conn, "select giorno_pagamento from clie_forn where codice = " + Db.pc(codiceCliente, Types.VARCHAR)));
                } catch (Exception e) {
                }
                speseIncasso = tempPrev.getDouble("spese_incasso");
                speseTrasporto = tempPrev.getDouble("spese_trasporto");
                //calcolo spese di trasporto per somma
                sql = "select sum(IFNULL(spese_incasso, 0)) as tot_spese_incasso"
                        + ", sum(IFNULL(spese_trasporto,0)) as tot_spese_trasporto"
                        + ", sum(IFNULL(sconto,0)) as tot_sconto"
                        + ", sum(IFNULL(acconto, 0)) as tot_acconto"
                        + " from " + test_ddt + " t ";
                sql += " where t.id in (" + StringUtils.join(ids, ",") + ")";

                System.err.println("sql totale spese:" + sql);
                try {
                    List<Map> list = DbUtils.getListMap(conn, sql);
                    System.out.println("list = " + list);
                    speseTrasporto = CastUtils.toDouble0(list.get(0).get("tot_spese_trasporto"));
                    speseIncasso = CastUtils.toDouble0(list.get(0).get("tot_spese_incasso"));
                    scontoTotale = CastUtils.toDouble0(list.get(0).get("tot_sconto"));
                    accontoTotale = CastUtils.toDouble0(list.get(0).get("tot_acconto"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ResultSet colonne_dest = Db.openResultSet("select * from " + test_fatt + " limit 0");

                for (int i = 1; i <= metaPrev.getColumnCount(); i++) {
                    flag = true;

                    if (metaPrev.getColumnName(i).equalsIgnoreCase("numero")) {
                        sqlC += "numero";
                        sqlV += Db.pc(String.valueOf(newNumero), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("serie")) {
                        sqlC += "serie";
                        sqlV += Db.pc(newSerie, metaPrev.getColumnType(i));
                    } else if (!acquisto && metaPrev.getColumnName(i).equalsIgnoreCase("stato")) {
                        sqlC += "stato";
                        sqlV += Db.pc(newStato, metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("anno")) {
                        sqlC += "anno";
                        sqlV += Db.pc(anno_fattura, "INTEGER");
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("spese_trasporto")) {
                        sqlC += "spese_trasporto";
                        sqlV += Db.pc(speseTrasporto, Types.DOUBLE);
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("spese_incasso")) {
                        sqlC += "spese_incasso";
                        sqlV += Db.pc(speseIncasso, Types.DOUBLE);
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("sconto")) {
                        sqlC += "sconto";
                        sqlV += Db.pc(scontoTotale, Types.DOUBLE);
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("acconto")) {
                        sqlC += "acconto";
                        sqlV += Db.pc(accontoTotale, Types.DOUBLE);
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("data")) {
                        DateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
                        sqlC += "data";
                        sqlV += Db.pc(myFormat.format(calendarDataFattura.getTime()), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase(campoc)
                            || metaPrev.getColumnName(i).equalsIgnoreCase(campoc + "_destinazione")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("id_" + campoc + "_destinazione")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("data_consegna")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("spese_varie")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("note")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("note_testa")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("note_corpo")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("note_piede")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("totale_imponibile")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("totale_iva")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("totale")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("pagamento")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("sconto1")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("sconto2")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("riferimento")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("sconto3")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("codice_listino")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("dest_ragione_sociale")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("dest_indirizzo")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("dest_cap")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("dest_localita")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("dest_provincia")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("dest_telefono")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("dest_cellulare")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("dest_paese")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("banca_abi")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("banca_cab")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("banca_cc")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("banca_iban")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("agente_codice")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("agente_percentuale")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("note_pagamento")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("prezzi_ivati")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("sconto")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("giorno_pagamento")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("conto")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("deposito")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("modalita_consegna")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("modalita_scarico")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("note_consegna")) {
                        if (metaPrev.getColumnName(i).equalsIgnoreCase("pagamento")) {
                            pagamento = tempPrev.getString(i);
                        }
                        if (metaPrev.getColumnName(i).equalsIgnoreCase("agente_codice")) {
                            agente_codice = tempPrev.getInt(i);
                        }
                        if (metaPrev.getColumnName(i).equalsIgnoreCase("agente_percentuale")) {
                            agente_percentuale = tempPrev.getDouble(i);
                        }

                        if (metaPrev.getColumnName(i).equalsIgnoreCase("riferimento")) {
                            sqlC += metaPrev.getColumnName(i);
                            String optionVostroOrdine = main.fileIni.getValue("pref", "stato_vs_ordine", "0");
                            if (optionVostroOrdine.equals("0")) {
                                sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                            } else {
                                String concat = "DDT " + String.valueOf(tempPrev.getObject("serie")) + String.valueOf(tempPrev.getObject("numero")) + " " + String.valueOf(tempPrev.getObject("anno"));
                                if (optionVostroOrdine.equals("1")) {
                                    sqlV += Db.pc(concat, metaPrev.getColumnType(i));
                                } else if (optionVostroOrdine.equals("2")) {
                                    if (String.valueOf(tempPrev.getObject(i)).equals("")) {
                                        sqlV += Db.pc(concat, metaPrev.getColumnType(i));
                                    } else {
                                        sqlV += Db.pc(tempPrev.getObject(i) + ", " + concat, metaPrev.getColumnType(i));
                                    }
                                }
                            }
//                        } else if (tempPrev.getObject(i) != null) {
                        } else //ignoro per acquisto
                        {
                            if (!DbUtils.existColumn(colonne_dest, metaPrev.getColumnName(i))) {
                                flag = false;
                            } else if (acquisto && metaPrev.getColumnName(i).equalsIgnoreCase("totale_imponibile")) {
                                sqlC += "imponibile";
                                sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                            } else if (acquisto && metaPrev.getColumnName(i).equalsIgnoreCase("totale")) {
                                sqlC += "importo";
                                sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                            } else if (acquisto && metaPrev.getColumnName(i).equalsIgnoreCase("totale_iva")) {
                                sqlC += "iva";
                                sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                            } else if (metaPrev.getColumnName(i).equalsIgnoreCase("giorno_pagamento")) {
                                sqlC += "giorno_pagamento";
                                Integer giorno_su_doc = CastUtils.toInteger0(tempPrev.getObject(i));
                                if (giorno_su_doc > 0) {
                                    giorno_pagamento = giorno_su_doc;
                                }
                                sqlV += Db.pc(giorno_pagamento, metaPrev.getColumnType(i));
                            } else {
                                sqlC += metaPrev.getColumnName(i);
                                sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                            }
                        }
                    } else {
                        //non li prendo
                        flag = false;
                    }

                    if (flag == true) {

//non capisco...
//                        if (tempPrev.getObject(i) != null) {
                        sqlC += ",";
                        sqlV += ",";
//                        }
                    }
                }

                //aggiungo tipo fattura fattura immediata = 1
                sqlC = "tipo_fattura," + sqlC;
                if (!acquisto) {
                    if (notaDiCredito) {
                        sqlV = Db.pc(dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO, Types.INTEGER) + "," + sqlV;
                    } else {
                        sqlV = Db.pc(dbFattura.TIPO_FATTURA_IMMEDIATA, Types.INTEGER) + "," + sqlV;
                    }
                } else if (notaDiCredito) {
                    sqlV = Db.pc(dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO, Types.INTEGER) + "," + sqlV;
                } else {
                    sqlV = Db.pc(dbFattura.TIPO_FATTURA_ACQUISTO, Types.INTEGER) + "," + sqlV;
                }

                //creo la insetrt
                sql = "insert into " + test_fatt;
                sql += "(" + sqlC.substring(0, sqlC.length() - 1) + ") values (" + sqlV.substring(0, sqlV.length() - 1) + ")";
                id = Db.executeSqlRetIdDialogExc(conn, sql, false, true);
            }
        } catch (Exception err) {
            System.out.println("sqlerr:" + sql);
            err.printStackTrace();
            SwingUtils.showErrorMessage(main.getPadre(), "Impossibile completare l'operazione.\n" + err.getMessage(), "Errore");
            return null;
        }

        //split payment dal cliente
        if (!acquisto) {
            try {
                String split_payment = cu.s(dbu.getObject(Db.getConn(), "select split_payment from clie_forn where codice = " + codiceCliente));
                sql = "insert into test_fatt_xmlpa set id_fattura = " + id + ", split_payment = " + dbu.sql(split_payment);
                System.out.println("sql = " + sql);
                DbUtils.tryExecQuery(Db.getConn(), sql);
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtils.showErrorMessage(main.getPadre(), "Impossibile completare l'operazione.\n" + e.getMessage(), "Errore");
            }
        }

        String tabellaDest = acquisto ? "fatt_acquisto" : "fatt";
        String tipoDocDest = acquisto ? Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA : Db.TIPO_DOCUMENTO_FATTURA;

        //recupero ritenuta/rivalsa su conversione
        try {
            if (tabellaDest.equals("fatt") || tabellaDest.equals("fatt_acquisto")) {
                sql = "select ritenuta from clie_forn where codice = " + Db.pc(codiceCliente, Types.VARCHAR);
                String ritenuta = cu.s(DbUtils.getObject(conn, sql));
                if (StringUtils.isNotBlank(ritenuta)) {
                    sql = "update test_" + tabellaDest + " set ritenuta = " + Db.pc(ritenuta, Types.VARCHAR) + " where id = " + id;
                    DbUtils.tryExecQuery(conn, sql);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (raggr) {
            //inserisco righe
            sql = "select t.*, t.data as dataddt from " + test_ddt + " t ";
            sql += " where t.id in (" + StringUtils.join(ids, ",") + ")";
            sql += " order by t.data, t.serie, t.anno, t.numero ";
            System.err.println("sql ddt:" + sql);
            ResultSet rdaddt = Db.openResultSet(sql);
//            String daddt = "Da DDT ";
            String daddt = main.fileIni.getValue("altro", "da_ddt", "*** Da DDT");
            try {
                while (rdaddt.next()) {
                    if (rdaddt.isFirst()) {
                    } else {
                        daddt += ", ";
                    }
                    daddt += rdaddt.getString("serie") + "" + rdaddt.getString("numero") + " del " + sdf.format(rdaddt.getDate("dataddt"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(dbDocumento.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("daddt:" + daddt);
            sql = "select r.*, t.data as dataddt, sum(quantita) as sqta, sum(r.totale_imponibile) as stotale_imponibile, sum(r.totale_ivato) as stotale_ivato from " + test_ddt + " t ";
            sql += "left join " + righ_ddt + " r on t.id = r.id_padre";
            sql += " where t.id in (" + StringUtils.join(ids, ",") + ")";
            sql += " group by codice_articolo, um, prezzo, iva, sconto1, sconto2";
            sql += " order by t.serie, t.anno, t.numero, r.riga";
            tempPrev = Db.openResultSet(sql);
            inserisciRighe(raggr, 0, daddt, tempPrev, newNumero, newStato, newSerie, calendarDataFattura);

            //aggiungo da_ddt_raggr per sapere da quale ddt è stata creata la fattura
            try {
                sql = "update test_" + tabellaDest + " set da_ddt_raggr = " + dbu.sql(StringUtils.join(ids, ",")) + " where id = " + id;
                dbu.tryExecQuery(conn, sql);
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
            }

            //ricalcolare lo stato evasa , parziale , no sul documento di origine
            try {
                for (int i = 0; i < ids.length; i++) {
                    sql = "select id from " + test_ddt + " t ";
                    sql += " where t.id = " + ids[i];
                    ResultSet doc_origine = Db.openResultSet(sql);
                    if (doc_origine.next()) {
                        //forzo tutte le righe evase sul ddt di orgine
                        sql = "UPDATE " + righ_ddt + " AS r JOIN " + test_ddt + " as t ON r.id_padre = t.id SET r.quantita_evasa = r.quantita";
                        sql += " where t.id = " + ids[i];
                        DbUtils.tryExecQuery(conn, sql);
                        //calcolo evasione
                        //InvoicexUtil.aggiornaStatoEvasione(acquisto ? Db.TIPO_DOCUMENTO_DDT_ACQUISTO : Db.TIPO_DOCUMENTO_DDT, doc_origine.getInt("id"));
                        //forzo evase
                        sql = "update " + test_ddt + " t set evaso = 'S' where t.id = " + ids[i];
                        DbUtils.tryExecQuery(conn, sql);
                    }
                    //metto flag che sono gia' state fatturate
                    try {
                        String convertito = CastUtils.toString(DbUtils.getObject(conn, "select convertito from " + test_ddt + " t where t.id = " + ids[i]));
                        convertito += (convertito.length() > 0 ? "\n" : "") + InvoicexUtil.getNumeroDaId(tipoDocDest, id, false);
                        sql = "update " + test_ddt + " t";
                        sql += " set convertito = " + Db.pc(convertito, "VARCHAR");
                        sql += " where t.id = " + ids[i];
                        DbUtils.tryExecQuery(conn, sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (raggr_riepilogo) {
            for (int i2 = 0; i2 < ids.length; i2++) {
                //inserisco righe
                sql = "select r.*, t.data from " + test_ddt + " t ";
                sql += "left join " + righ_ddt + " r on t.id = r.id_padre";
                sql += " where t.id = " + ids[i2];
                sql += " order by t.serie, t.anno, t.numero, r.riga";
                tempPrev = Db.openResultSet(sql);
                inserisciRigheRiepilogative(i2, null, tempPrev, newNumero, newStato, newSerie, calendarDataFattura);
            }

            //aggiungo da_ddt_raggr per sapere da quale ddt è stata creata la fattura
            try {
                sql = "update test_" + tabellaDest + " set da_ddt_raggr = " + dbu.sql(StringUtils.join(ids, ",")) + " where id = " + id;
                dbu.tryExecQuery(conn, sql);
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
            }

            //ricalcolare lo stato evasa , parziale , no sul documento di origine
            try {
                for (int i = 0; i < ids.length; i++) {
                    sql = "select id from " + test_ddt + " t ";
                    sql += " where t.id = " + ids[i];
                    ResultSet doc_origine = Db.openResultSet(sql);
                    if (doc_origine.next()) {
                        //forzo tutte le righe evase sul ddt di orgine
                        sql = "UPDATE " + righ_ddt + " AS r JOIN " + test_ddt + " as t ON r.id_padre = t.id SET r.quantita_evasa = r.quantita";
                        sql += " where t.id = " + ids[i];
                        DbUtils.tryExecQuery(conn, sql);
                        //calcolo evasione
                        //InvoicexUtil.aggiornaStatoEvasione(acquisto ? Db.TIPO_DOCUMENTO_DDT_ACQUISTO : Db.TIPO_DOCUMENTO_DDT, doc_origine.getInt("id"));
                        //forzo evase
                        sql = "update " + test_ddt + " t set evaso = 'S' where t.id = " + ids[i];
                        DbUtils.tryExecQuery(conn, sql);
                    }
                    //metto flag che sono gia' state fatturate
                    try {
                        String convertito = CastUtils.toString(DbUtils.getObject(conn, "select convertito from " + test_ddt + " t where t.id = " + ids[i]));
                        convertito += (convertito.length() > 0 ? "\n" : "") + InvoicexUtil.getNumeroDaId(tipoDocDest, id, false);
                        sql = "update " + test_ddt + " t";
                        sql += " set convertito = " + Db.pc(convertito, "VARCHAR");
                        sql += " where t.id = " + ids[i];
                        System.out.println("sql = " + sql);
                        DbUtils.tryExecQuery(conn, sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            for (int i2 = 0; i2 < ids.length; i2++) {
                //inserisco righe
                sql = "select r.*, t.data from " + test_ddt + " t ";
                sql += "left join " + righ_ddt + " r on t.id = r.id_padre";
                sql += " where t.id = " + ids[i2];
                sql += " order by t.serie, t.anno, t.numero, r.riga";
                tempPrev = Db.openResultSet(sql);
                inserisciRighe(raggr, i2, null, tempPrev, newNumero, newStato, newSerie, id, calendarDataFattura);

                if (worker != null) {
                    int perc = i2 * 100 / ids.length;
                    worker.publish(perc);
                }

            }
        }

        if (worker != null) {
            worker.publish("scelta_qta");
        }

        if (!raggr && !raggr_riepilogo) {
            //presentare la tabella dei codici art./descr/quantità di origine/quantita evasa o arrivata
            JDialogSceltaQuantita dialog = new JDialogSceltaQuantita(main.getPadreFrame(), true);
            dialog.load(acquisto ? Db.TIPO_DOCUMENTO_DDT_ACQUISTO : Db.TIPO_DOCUMENTO_DDT, acquisto ? Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA : Db.TIPO_DOCUMENTO_FATTURA, ids, null);
            dialog.setTitle("Selezione quantità");
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            //controllare esito
            boolean annullato = false;
            if (dialog.ok) {
                JTable table = dialog.getTable();

                if (worker != null) {
                    worker.publish("scelta_qta_ok");
                }

                for (int i = 0; i < table.getRowCount(); i++) {
                    //modificare le quantita sul documento generato
                    double qtaconf = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("quantità confermata").getModelIndex()));
                    boolean convertire = CastUtils.toBoolean(table.getValueAt(i, table.getColumn("riga confermata").getModelIndex()));
                    double qta = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("quantità").getModelIndex()));
                    double prezzo = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("prezzo").getModelIndex()));
                    String codart = CastUtils.toString(table.getValueAt(i, table.getColumn("articolo").getModelIndex()));
                    String descr = CastUtils.toString(table.getValueAt(i, table.getColumn("descrizione").getModelIndex()));

                    sql = null;
//                    if (qtaconf > 0) {
                    if (convertire) {
                        sql = "update righ_" + tabellaDest + " r ";
                        sql += " set quantita = " + Db.pc(qtaconf, Types.DOUBLE);
                        sql += " where id = " + table.getValueAt(i, table.getColumn("dest_id_riga").getModelIndex());
                    } else {
//                        if ((prezzo != 0 || qta > 0) && !main.getPersonalContain("proskin")) {
                        sql = "delete from righ_" + tabellaDest + " where id = " + table.getValueAt(i, table.getColumn("dest_id_riga").getModelIndex());
//                        }
                    }
                    if (sql != null) {
                        System.out.println("i = " + i + " sql = " + sql);
                        Db.executeSql(sql);
                    }
                    //andare sulle righe di origine a mettere la quantita evasa o arrivata
                    sql = "update " + righ_ddt + " r ";
                    double qta_evasa = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("quantità confermata").getModelIndex()));
                    double qta_gia_evasa = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("quantità già confermata").getModelIndex()));
                    sql += " set quantita_evasa = " + Db.pc(qta_evasa + qta_gia_evasa, Types.DOUBLE);
                    Object rigaid = table.getValueAt(i, table.getColumn("prov_id_riga").getModelIndex());
                    sql += " where id = " + table.getValueAt(i, table.getColumn("prov_id_riga").getModelIndex());
                    System.out.println("i = " + i + " sql = " + sql);
                    //Db.executeSql(sql);
                    try {
                        DbUtils.tryExecQuery(conn, sql);
                    } catch (Exception e) {
                        SwingUtils.showErrorMessage(main.getPadreFrame(), "Problema nel salvataggio della quantità evasa alla riga id:" + cu.toString(rigaid) + " (i:" + i + "\n" + e.getMessage());
                    }

                    if (worker != null) {
                        int perc = i * 100 / table.getRowCount();
                        worker.publish(perc);
                    }

                }

                //controllo presenza di righe "Da prevemtivo..." che però sono superfluee perchè di quel preventivo non c'è niente
                //per farlo controllo le righe ragguppate per da_ordi, se 1 riga vuol dire che è rimasta solo quella, per ulteriore verifica controllo da_ordi_riga che sia nullo
                sql = "select da_ddt, count(*) as conta from " + righ_fatt + " where id_padre = " + id + " group by da_ddt";
                try {
                    List<Map> listgroup = dbu.getListMap(conn, sql);
                    if (listgroup != null && listgroup.size() > 0) {
                        for (Map m : listgroup) {
                            if (cu.i0(m.get("conta")) == 1 && m.get("da_ordi_riga") == null) {
                                sql = "delete from " + righ_fatt + " where id_padre = " + id + " and da_ddt = " + m.get("da_ddt");
                                dbu.tryExecQuery(conn, sql);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (worker != null) {
                    worker.publish("ricalcolo_stato");
                }

                //ricalcolare lo stato evasa , parziale , no sul documento di origine
                try {
                    for (int i = 0; i < ids.length; i++) {
                        sql = "select id from " + test_ddt + " t ";
                        sql += " where t.id = " + ids[i];
                        ResultSet doc_origine = Db.openResultSet(sql);
                        if (doc_origine.next()) {
                            InvoicexUtil.aggiornaStatoEvasione(acquisto ? Db.TIPO_DOCUMENTO_DDT_ACQUISTO : Db.TIPO_DOCUMENTO_DDT, doc_origine.getInt("id"));
                        }

                        //metto flag che sono gia' state fatturate (solo se passata almeno una riga sul nuovo documento)
                        try {
                            Integer idn = ids[i];
                            //se di un ordine si sono escluse tutte le righe si deve togliere il 'convertito' e le quantità evase
                            sql = "select id from righ_" + tabellaDest + " where da_ddt = " + idn + " and id_padre = " + id;
                            if (dbu.containRows(Db.getConn(), sql)) {
                                String convertito = CastUtils.toString(DbUtils.getObject(conn, "select convertito from " + test_ddt + " t where t.id = " + ids[i]));
                                convertito += (convertito.length() > 0 ? "\n" : "") + InvoicexUtil.getNumeroDaId(tipoDocDest, id, false);
                                sql = "update " + test_ddt + " t";
                                sql += " set convertito = " + Db.pc(convertito, "VARCHAR");
                                sql += " where t.id = " + ids[i];
                                System.out.println("sql = " + sql);
                                DbUtils.tryExecQuery(conn, sql);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                //portare i dati dei lotti/matricole
                Integer codice_deposito = null;
                if (dialog.deposito.getSelectedIndex() > 0) {
                    try {
                        codice_deposito = cu.i(((KeyValuePair) dialog.deposito.getSelectedItem()).getKey());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                InvoicexUtil.riportaLotti(table, tabellaDest, righ_ddt, tipoDocDest, main.getPadreFrame(), codice_deposito);
            } else {

                if (worker != null) {
                    worker.publish("scelta_qta_ko");
                }

                annullato = true;
                JTable table = dialog.getTable();
                Integer old_id = null;
                for (int i = 0; i < table.getRowCount(); i++) {
                    //Integer t_id = CastUtils.toInteger(table.getValueAt(i, table.getColumn("dest_id").getModelIndex()));
                    Integer t_id = id;  //id della fattura creata
                    if (old_id == null || old_id != t_id) {
                        //annullare l'inserimento nuovo
                        sql = "delete from righ_" + tabellaDest;
                        sql += " where id_padre = " + t_id;
                        System.out.println("i = " + i + " sql = " + sql);
                        Db.executeSql(sql);
                        sql = "delete from test_" + tabellaDest;
                        sql += " where id = " + t_id;
                        System.out.println("i = " + i + " sql = " + sql);
                        Db.executeSql(sql);
                        old_id = t_id;
                    }
                    //azzerare il campo_in nella tab. di origine
                    sql = "update " + righ_ddt + " r ";
                    sql += " set " + campo_in + " = null";
                    sql += " , " + campo_in + "_riga = null";
                    sql += " where id = " + table.getValueAt(i, table.getColumn("prov_id_riga").getModelIndex());
                    System.out.println("i = " + i + " sql = " + sql);
                    Db.executeSql(sql);

                    if (worker != null) {
                        int perc = i * 100 / table.getRowCount();
                        worker.publish(perc);
                    }

                }
//                try {
//                    ddtPrev.beforeFirst();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                for (Integer idn : ids) {
//                    System.out.println("idn:" + idn);
//                    // Ripristino a valore precedente riferimenti a fattura
//                    try {
//                        sql = "update " + test_ddt + " set ";
//                        sql += "fattura_serie = " + Db.pc(ddtPrev.getString("fattura_serie"), Types.VARCHAR) + ", ";
//                        sql += "fattura_numero = " + Db.pc(ddtPrev.getString("fattura_numero"), Types.INTEGER) + ", ";
//                        sql += "fattura_anno = " + Db.pc(ddtPrev.getString("fattura_anno"), Types.INTEGER) + ", ";
//                        sql += "convertito = " + Db.pc(ddtPrev.getString("convertito"), Types.VARCHAR) + ", ";
//                        sql += "evaso = " + Db.pc(ddtPrev.getString("evaso"), Types.VARCHAR) + " ";
//                        sql += "WHERE id = " + Db.pc(ddtPrev.getString("id"), Types.INTEGER);
//                        Db.executeSql(sql);
//                        ddtPrev.next();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
            }

            if (annullato) {
                if (worker != null) {
                    worker.publish("annullamento_ok");
                }
                return null;
            }

        }

        if (worker != null) {
            worker.publish("ricalcolo_totali");
        }

        //prima di ricalcolare i totali aggiorno le righe
        if (id != null) {
            InvoicexUtil.aggiornaTotaliRighe(acquisto ? Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA : Db.TIPO_DOCUMENTO_FATTURA, id);
        }

        //aggiorno abi, cab e pagamento prendendoli dal cliente
//        if (pagamento.length() == 0) {
        sql = "select pagamento, banca_abi, banca_cab, banca_cc_iban from clie_forn";
        sql += " where codice = " + Db.pc(codiceCliente, "INTEGER");

        try {
            ResultSet cliente = Db.openResultSet(sql);
            cliente.next();
            if (StringUtils.isEmpty(pagamento)) {
                pagamento = cliente.getString("pagamento");
            }
            banca_abi = cliente.getString("banca_abi");
            banca_cab = cliente.getString("banca_cab");
            banca_cc_iban = cliente.getString("banca_cc_iban");
        } catch (Exception err2) {
            System.out.println("convertiInFatture: impossibile trovare il cliente per banca abi e cab");
        }
//        }

        Integer agente = null;
        BigDecimal perc = null;
        try {
            agente = (Integer) DbUtils.getObject(conn, "select agente from clie_forn where codice = " + Db.pc(codiceCliente, Types.VARCHAR));
            if (agente != null && agente > 0) {
                perc = (BigDecimal) DbUtils.getObject(conn, "select percentuale from agenti where id = " + Db.pc(agente, Types.INTEGER));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //ricaloclo totali
        dbFattura tempFattura = null;
        if (acquisto) {
            tempFattura = new dbFatturaRicevuta();
            tempFattura.serie = newSerie;
            tempFattura.anno = calendarDataFattura.get(Calendar.YEAR);
            tempFattura.numero = newNumero;
            tempFattura.setId(id);
            tempFattura.speseIncassoIva = speseIncasso;
            tempFattura.speseTrasportoIva = speseTrasporto;
            tempFattura.dbRefresh();
            sql = "update " + test_fatt + " set ";
            sql += " imponibile = " + Db.pc(tempFattura.totaleImponibile, "DOUBLE");
            sql += " , iva = " + Db.pc(tempFattura.totaleIva, "DOUBLE");
            sql += " , importo = " + Db.pc(tempFattura.totale, "DOUBLE");
        } else {
            tempFattura = new dbFattura();
            tempFattura.acquisto = acquisto;
            tempFattura.serie = newSerie;
            tempFattura.anno = calendarDataFattura.get(Calendar.YEAR);
            tempFattura.numero = newNumero;
            tempFattura.setId(id);
            tempFattura.speseIncassoIva = speseIncasso;
            tempFattura.speseTrasportoIva = speseTrasporto;
            tempFattura.dbRefresh();
            sql = "update " + test_fatt + " set ";
            sql += " totale_imponibile = " + Db.pc(tempFattura.totaleImponibile, "DOUBLE");
            sql += " , totale_iva = " + Db.pc(tempFattura.totaleIva, "DOUBLE");
            sql += " , totale = " + Db.pc(tempFattura.totale, "DOUBLE");
        }
        sql += " , totale_da_pagare = " + Db.pc(tempFattura.doc.getTotale_da_pagare(), "DOUBLE");
        sql += " , totale_da_pagare_finale = " + Db.pc(tempFattura.doc.getTotale_da_pagare_finale(), "DOUBLE");
        sql += " , totale_imponibile_pre_sconto = " + Db.pc(tempFattura.totaleImponibilePreSconto, "DOUBLE");
        sql += " , totale_ivato_pre_sconto = " + Db.pc(tempFattura.totaleIvatoPreSconto, "DOUBLE");

        //dati cliente
        sql += " , pagamento = " + Db.pc(pagamento, "VARCHAR");
        try {
            sql += " , note_pagamento = " + Db.pc(CastUtils.toString(DbUtils.getObject(conn, "select note_su_documenti from pagamenti where codice = " + Db.pc(pagamento, Types.VARCHAR))), "VARCHAR");
        } catch (Exception e) {
        }
        sql += " , banca_abi = " + Db.pc(banca_abi, "VARCHAR");
        sql += " , banca_cab = " + Db.pc(banca_cab, "VARCHAR");
        sql += " , banca_iban = " + Db.pc(banca_cc_iban, "VARCHAR");
        if (!acquisto) {
            if (agente_codice == null || agente_codice == 0) {
                if (agente != null && agente > 0) {
                    sql += " , agente_codice = " + Db.pc(agente, "VARCHAR");
                }
                if (perc != null) {
                    sql += " , agente_percentuale = " + Db.pc(perc.doubleValue(), "VARCHAR");
                }
            }
        }

        //***
        sql += " where serie = " + Db.pc(newSerie, "VARCHAR");
        sql += " and anno = " + Db.pc(tempFattura.anno, "INTEGER");
        sql += " and numero = " + Db.pc(newNumero, "LONG");

        Db.executeSql(sql);

        //debug
        System.out.println("sql totale fatt:" + sql);

        //***
        //genero le scadenze in automatico
        if (Db.contain("pagamenti", "codice", Types.VARCHAR, pagamento)) {
            Scadenze tempScad = new Scadenze(tipo_doc, id, pagamento);
            tempScad.generaScadenze();
        } else {
            Storico.scrivi("DDT a Fattura", "non trovato pagamento");
            if (StringUtils.isNotBlank(pagamento)) {
                SwingUtils.showErrorMessage(main.getPadre(), "Il pagamento '" + pagamento + "' non esiste, impossibile generare le eventuali scadenze di pagamento !");
            }
        }

        //genero le provvigioni
        //rigenero le provvigioni se ancora non sono state pagate
        //Scadenze tempScad = new Scadenze(Db.TIPO_DOCUMENTO_FATTURA, this.texSeri.getText(), this.prev.numero, this.prev.anno, this.comPaga.getText());
        //cercare agente da cliente
        if (agente_codice != null && !acquisto) {
            ProvvigioniFattura provvigioni = new ProvvigioniFattura(id, agente_codice, agente_percentuale);
            boolean ret = provvigioni.generaProvvigioni();
            System.out.println("esito genera provvigioni:" + ret + " : " + provvigioni.ret);
        } else {
            try {
                if (perc != null && perc.doubleValue() > 0d && !acquisto) {
                    ProvvigioniFattura provvigioni = new ProvvigioniFattura(id, agente, perc.doubleValue());
                    boolean ret = provvigioni.generaProvvigioni();
                    System.out.println("esito genera provvigioni:" + ret + " : " + provvigioni.ret);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return (Db.nz(newSerie, "") + " " + newNumero);
    }
    String sqls = null;     //per fare una query sola
    //105

    public boolean azzeraMovimentiMagazzino() {
        return generaMovimentiMagazzino(true);
    }

    public boolean generaMovimentiMagazzino() {
        return generaMovimentiMagazzino(false);
    }

    public boolean generaMovimentiMagazzino(boolean soloAzzeramento) {
        String sql;
        String sqlC;
        String sqlV;
        boolean inseribile;
        ResultSet righe;
        ResultSet testa;
        int deposito = 0;
        Integer deposito_arrivo = null;

        Object inizio_mysql = Db.getCurrentTimestamp();

        articoli_movimentati.clear();

        System.out.println(this.numero + " / " + this.anno + " " + numero + " / " + anno + " / " + tipoDocumento);

        //elimino eventuali movimenti precedenti derivanti dallo stesso documento        
        sql = "";
//        if (this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
//            sql += " where da_tabella = " + dbUtil.pc("test_" + this.getNomeTabellaFinale(tipoDocumento), "VARHCAR");
//            sql += " and da_serie = " + dbUtil.pc(serie, "VARHCAR");
//            sql += " and da_numero = " + dbUtil.pc(String.valueOf(numero), "LONG");
//            sql += " and da_anno = " + Db.pc(anno, "INTEGER");
//        } else if (this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA) || this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_SCONTRINO)) {
        if (id == 0) {
            System.out.println("Id 0 su generazione movimenti per " + serie + "/" + numero + "/" + anno);
            if (!main.isBatch) {
                SwingUtils.showErrorMessage(main.getPadre(), "Id 0 su generazione movimenti per " + serie + "/" + numero + "/" + anno);
            }
        }
        sql += " where da_tabella = " + dbUtil.pc("test_" + this.getNomeTabellaFinale(tipoDocumento), "VARHCAR");
        sql += " and da_id = " + id;
//        } else {
//            sql += " where da_tabella = " + dbUtil.pc("test_" + this.getNomeTabellaFinale(tipoDocumento), "VARHCAR");
//            sql += " and da_serie = " + dbUtil.pc(serie, "VARHCAR");
//            sql += " and da_numero = " + dbUtil.pc(String.valueOf(numero), "LONG");
//            sql += " and da_anno = " + Db.pc(anno, "INTEGER");
//        }
        //memorizzo gli eliminati
        main.magazzino.preDelete(sql);
        //elimino
        sql = "delete from movimenti_magazzino" + sql;
        Db.executeSql(sql);

        if (soloAzzeramento) {
            main.events.fireInvoicexEventMagazzino(this, inizio_mysql);
            return true;
        }

        //seleziono la testata del documento, per prendere la data..
//        if (this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
//            sql = "select * from test_fatt_acquisto";
//            sql += " where serie = " + dbUtil.pc(serie, "VARHCAR");
//            sql += " and numero = " + dbUtil.pc(String.valueOf(numero), "LONG");
//            sql += " and anno = " + Db.pc(anno, "INTEGER");
//        } else if (this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA) || this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_SCONTRINO)) {
        sql = "select * from test_" + getNomeTabellaFinale(tipoDocumento);
        sql += " where id = " + id;
//        } else {
//            sql = "select * from test_" + getNomeTabellaFinale(tipoDocumento);
//            sql += " where serie = " + dbUtil.pc(serie, "VARHCAR");
//            sql += " and numero = " + dbUtil.pc(String.valueOf(numero), "LONG");
//            sql += " and anno = " + Db.pc(anno, "INTEGER");
//        }
        System.out.println("sql:" + sql);
        testa = Db.openResultSet(sql);

        //per costo medio ponderato
        InvoicexUtil.aggiornaPrezziNettiUnitari(Db.getConn(), "righ_" + getNomeTabellaFinale(this.tipoDocumento), "test_" + getNomeTabellaFinale(this.tipoDocumento), id);

        //seleziono le righe del documento
//        if (this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA) || this.tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_SCONTRINO)) {
        sql = "select r.codice_articolo, r.quantita, art.servizio, r.id, r.riga, r.prezzo, r.prezzo_netto_unitario from righ_" + getNomeTabellaFinale(this.tipoDocumento) + " r";
        sql += " left join articoli art on r.codice_articolo = art.codice";
        sql += " where r.id_padre = " + id;
        sql += " order by r.riga";
        System.out.println("sql:" + sql);
//        } else {
//            sql = "select r.codice_articolo, r.quantita, art.servizio, r.id, r.riga from righ_" + getNomeTabellaFinale(this.tipoDocumento) + " r";
//            sql += " left join articoli art on r.codice_articolo = art.codice";
//            sql += " where r.serie = " + dbUtil.pc(this.serie, "VARHCAR");
//            sql += " and r.numero = " + dbUtil.pc(String.valueOf(this.numero), "LONG");
//            sql += " and r.anno = " + Db.pc(this.anno, "INTEGER");
//            sql += " order by r.riga";
//            System.out.println("sql:" + sql);
//        }

        try {
            if (testa.next()) {
                //controllo che non sia nota di credito o fattura pro-forma
                int tipo_fattura = 0;
                try {
                    if (DbUtils.existColumn(testa, "tipo_fattura")) {
                        tipo_fattura = testa.getInt("tipo_fattura");
                        if (tipo_fattura == dbFattura.TIPO_FATTURA_PROFORMA) {
                            //ignoro le fatture pro forma
                            if (!main.getPersonalContain("movimenti_su_proforma")) {
                                return true;
                            }
                        }
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(dbDocumento.class.getName()).log(Level.SEVERE, null, ex);
                }

                righe = Db.openResultSet(sql);

                //inserisco le righe nei movimenti di magazzino
                //se presente il codice articolo e la qta
                sqls = "";
                sceltaRigaKit(righe, testa, null, "righ_" + getNomeTabellaFinale(tipoDocumento), "");
                if (StringUtils.isNotBlank(sqls)) {
                    if (!Sync.isActive()) {
                        try {
                            Db.executeSqlDialogExc(sqls, false, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (!main.isBatch) {
                                SwingUtils.showErrorMessage(InvoicexUtil.getActiveJInternalFrame(), "Errore nella generazione dei movimenti\n" + e.toString());
                            }
                        }
                    } else {
                        //ciclo le query di inserimento
                        String[] queries = StringUtils.split(sqls, ";\n");
                        Connection conn = Db.getConnection();
                        conn.setAutoCommit(false);
                        try {
                            for (String sqlq : queries) {
                                Db.executeSqlDialogExc(conn, sqlq, false, true);
                            }
                            conn.commit();
                        } catch (Exception e) {
                            conn.rollback();
                            if (!main.isBatch) {
                                SwingUtils.showErrorMessage(InvoicexUtil.getActiveJInternalFrame(), "Errore nella generazione dei movimenti\n" + e.toString());
                            }
                        } finally {
                            try {
                                conn.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }

                //aggiorno le disopnibilita degli articoli
                if (articoli_movimentati.size() > 0) {
                    for (String articolo : articoli_movimentati) {
                        
                        if (main.GLOB.get("movimenti_disattiva_update_disponibilita_reale") == null) {
                            List<Giacenza> retg = null;
                            if (main.disp_articoli_da_deposito == null) {
                                retg = main.magazzino.getGiacenza(false, articolo, null);
                            } else {
                                retg = main.magazzino.getGiacenza(false, articolo, null, null, false, true, main.disp_articoli_da_deposito);
                            }
                            System.out.println("retg = " + retg);
                            if (retg != null) {
                                double qta = 0d;
                                if (retg.size() == 1) qta = retg.get(0).getGiacenza();
                                sql = "update articoli set disponibilita_reale = " + dbu.sql(qta) + ", disponibilita_reale_ts = CURRENT_TIMESTAMP where codice = " + dbu.sql(articolo);
                                System.out.println("sql aggiorna gaicenza = " + sql);
                                dbu.tryExecQuery(Db.getConn(), sql);
                            } else {
                                System.out.println("errore sul calcolo giacenze dell'articolo:" + articolo + " esito:" + retg);
                            }
                        }

                        //evento per far calcolare altri eventuali tipi di giacenze
                        main.events.fireInvoicexEvent(new InvoicexEvent(articolo, InvoicexEvent.TYPE_UPDATE_DISP_DA_GENERA_MOVIMENTI));
                    }
                } else {
                    System.out.println("non aggiorno giacenze su articoli perchè articoli_movimentati.size <= 0");
                }

                main.events.fireInvoicexEventMagazzino(this, inizio_mysql);

                return true;
            } else {
                return false;
            }
        } catch (Exception err) {
            err.printStackTrace();

            return false;
        }
    }

    public boolean generaMovimentiScontrino() {
        String sql;
        String sqlC;
        String sqlV;
        boolean inseribile;
        ResultSet righe;
        ResultSet testa;

        Object inizio_mysql = Db.getCurrentTimestamp();

        System.out.println(this.numero + " / " + this.anno + " " + numero + " / " + anno + " / " + tipoDocumento);

        //memorizzo gli eliminati
        sql = " where da_tabella = 'test_fatt'";
        sql += " and da_id = " + Db.pc(id, "INTEGER");
        main.magazzino.preDelete(sql);
        //elimino eventuali movimenti precedenti derivanti dallo stesso documento        
        sql = "delete from movimenti_magazzino " + sql;
        Db.executeSql(sql);

        //seleziono la testata del documento, per prendere la data..
        sql = "select * from test_fatt";
        sql += " where id = " + Db.pc(id, "INTEGER");
        System.out.println("sql:" + sql);
        testa = Db.openResultSet(sql);

        //seleziono le righe del documento
        sql = "select r.codice_articolo, r.quantita, art.servizio, r.id, r.riga from righ_" + getNomeTabellaFinale(this.tipoDocumento) + " r";
        sql += " left join articoli art on r.codice_articolo = art.codice";
        sql += " where r.id_padre = " + Db.pc(id, Types.INTEGER);
        sql += " order by r.riga";
        System.out.println("sql:" + sql);

        try {
            testa.next();
            //controllo che non sia nota di credito o fattura pro-forma
            int tipo_fattura = 0;
            try {
                if (DbUtils.existColumn(testa, "tipo_fattura")) {
                    tipo_fattura = testa.getInt("tipo_fattura");
                    if (tipo_fattura == dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO || tipo_fattura == dbFattura.TIPO_FATTURA_PROFORMA) {

                        main.events.fireInvoicexEventMagazzino(this, inizio_mysql);

                        //ignoro le fatture pro forma e note di credito
                        return true;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(dbDocumento.class.getName()).log(Level.SEVERE, null, ex);
            }

            righe = Db.openResultSet(sql);

            //inserisco le righe nei movimenti di magazzino
            //se presente il codice articolo e la qta
            sqls = "";
            sceltaRigaKit(righe, testa, null, "righ_" + getNomeTabellaFinale(tipoDocumento), "");
            if (StringUtils.isNotBlank(sqls)) {
                Db.executeSql(sqls);
            }

            main.events.fireInvoicexEventMagazzino(this, inizio_mysql);

            return true;
        } catch (Exception err) {
            err.printStackTrace();

            return false;
        }
    }

    synchronized private void sceltaRigaKit(ResultSet righe, ResultSet testa, Double quant, String tabella, String pacchetto) throws Exception {
        try {
//            DebugUtils.dumpMem();
            if (articoli == null) {
                articoli = CastUtils.toLong(DbUtils.getObject(Db.getConn(), "select count(*) from articoli"));
                if (articoli >= 25000) {
                    fareCacheArticoli = false;
                }
            }
            if (fareCacheArticoli && cacheArticoli == null) {
                cacheArticoli = DbUtils.getListMapKV(Db.getConn(), "select codice, flag_kit from articoli");
            }
//            DebugUtils.dumpMem();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (cacheQtaLotti == null) {
                String sql = "select id_padre as riga, count(*) from " + getNomeTabellaFinaleLotti(this.tipoDocumento) + " where id_padre in (select id from righ_" + getNomeTabellaFinale(tipoDocumento) + " where id_padre = " + testa.getInt("id") + ") group by id_padre";
                System.err.println("sql:" + sql);
                cacheQtaLotti = DbUtils.getListMapKV(Db.getConn(), sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (cacheQtaMatricole == null) {
//                String sql = "select riga, count(*) from " + getNomeTabellaFinaleMatricole(tipoDocumento) + " where id_padre = " + testa.getInt("id") + " group by riga";
                String sql = "select id_padre_righe, count(*) from " + getNomeTabellaFinaleMatricole(tipoDocumento) + " where id_padre_righe in (select id from righ_" + getNomeTabellaFinale(tipoDocumento) + " where id_padre = " + testa.getInt("id") + ") group by id_padre_righe";
                System.err.println("sql:" + sql);
                cacheQtaMatricole = DbUtils.getListMapKV(Db.getConn(), sql);
            }
        } catch (Exception e) {
            SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
        }

        while (righe.next()) {
            boolean inseribile = true;

            if (Db.nz(righe.getString("codice_articolo"), "").trim().length() == 0) {
                inseribile = false;
            } else if (righe.getDouble("quantita") == 0) {
                inseribile = false;
            }
            //non inserisco gli articoli di tipo servizio
            boolean servizio = CastUtils.toBoolean(righe.getString("servizio"));
            if (servizio == true) {
                inseribile = false;
            }

            String codart = righe.getString("codice_articolo");
            String sql = null;
            boolean kit = false;
            if (cacheArticoli != null && cacheArticoli.containsKey(codart)) {
                if (((String) cacheArticoli.get(codart)).equals("S")) {
                    kit = true;
                } else {
                    kit = false;
                }
            } else {
                sql = "select flag_kit from articoli where codice = " + Db.pc(righe.getString("codice_articolo"), Types.VARCHAR);
                ResultSet tmp = Db.openResultSet(sql);
                if (tmp.next()) {
                    if (tmp.getString("flag_kit").equals("S")) {
                        kit = true;
                    }
                }
            }

            if (inseribile || kit) {
                if (!kit) {
                    insertRiga(testa, righe, quant);
                } else {
                    List<Map> listkit = esplodiKit(righe.getString("codice_articolo"), cu.d0(righe.getDouble("quantita")), true, servizio, null);
                    DebugFastUtils.dump(listkit);
                    int iriga = 0;
                    for (Map r : listkit) {
                        iriga++;
                        List<Map> listcache = new ArrayList<Map>();
                        Map rcache = new HashMap();
                        rcache.put("codice_articolo", r.get("articolo"));
                        rcache.put("quantita", r.get("quantita"));
                        rcache.put("id", righe.getInt("id"));
                        rcache.put("riga", righe.getInt("riga"));
                        listcache.add(rcache);
                        DbCacheResultSet tmprighe = new DbCacheResultSet(listcache);
                        tmprighe.next();
                        if (iriga == 1 && !servizio) {
                            //nel caso sia la prima riga che inserisco (se fosse servizio la prima riga sarebbe già un componente del kit)
                            //inserisco gli eventuali lotti o matricole
                            insertRiga(testa, tmprighe, null);
                        } else {
                            insertRiga2(testa, tmprighe, null, null, null);
                        }
                    }

                    //vecchi esplosione kit
//                    //comunque inserisco la riga del kit
//                    insertRiga(testa, righe, quant);
//                    if (tabella.equals("pacchetti_articoli")) {
//                        sql = "select quantita from pacchetti_articoli where ";
//                        sql += "articolo = " + Db.pc(righe.getString("codice_articolo"), Types.VARCHAR) + " and pacchetto = " + Db.pc(pacchetto, Types.VARCHAR);
////                        System.out.println("sql: " + sql);
//                        ResultSet rs = Db.openResultSet(sql);
//                        rs.next();
//                        quant = quant * rs.getDouble("quantita");
//                    } else {
//                        quant = righe.getDouble("quantita");
//                    }
//                    sql = "select articolo as codice_articolo";
//                    sql += ", quantita * " + quant + " as quantita";
//                    sql += " , servizio";
//                    sql += " ," + righe.getInt("id") + " as id";
//                    sql += " from pacchetti_articoli p left join articoli a on p.articolo = a.codice";
//                    sql += " where pacchetto = " + Db.pc(righe.getString("codice_articolo"), Types.VARCHAR);
//                    ResultSet temp = Db.openResultSet(sql);
////                    System.out.println("sql: " + sql);
////                    sceltaRigaKit(temp, testa, quant, "pacchetti_articoli", Db.nz(righe.getString("codice_articolo"), ""));
//                    sceltaRigaKit(temp, testa, !tabella.equals("pacchetti_articoli") ? quant : null, "pacchetti_articoli", Db.nz(righe.getString("codice_articolo"), ""));
                }
            }
        }
        cacheArticoli = null;
        cacheQtaLotti = null;
        cacheQtaMatricole = null;
    }

    private void insertRiga(ResultSet testa, ResultSet righe, Double quant) throws SQLException {

//        if (!(righe instanceof DbCacheResultSet) && !DbUtils.existColumn(righe, "id")) {
//            insertRiga2(testa, righe, quant, null, null);
//            return;
//        }
        //lotti
        String sqllotti = "select * from " + getNomeTabellaFinaleLotti(this.tipoDocumento) + " where id_padre = " + righe.getInt("id");
        try {
            int contalotti = 0;
            if (cacheQtaLotti != null) {
                contalotti = CastUtils.toInteger0(cacheQtaLotti.get(righe.getInt("id")));
            }
            if (cacheQtaLotti == null || contalotti > 0) {
                ResultSet rlotti = DbUtils.tryOpenResultSet(Db.getConn(), sqllotti);
                contalotti = 0;
                while (rlotti.next()) {
                    contalotti++;
//                    DebugUtils.dump(rlotti);
                    insertRiga2(testa, righe, rlotti.getDouble("qta"), rlotti.getString("lotto"), rlotti.getString("matricola"));
                }
            }
            if (contalotti == 0) {
                //testo matricole
                String sqlmatricole = null;
                int contaMatricole = 0;
                try {
//                    sqlmatricole = "select * from " + getNomeTabellaFinaleMatricole(this.tipoDocumento) + " where id_padre = " + testa.getInt("id") + " and riga = " + righe.getInt("riga");
                    sqlmatricole = "select * from " + getNomeTabellaFinaleMatricole(this.tipoDocumento) + " where id_padre_righe = " + righe.getInt("id");

                    if (sqlmatricole != null) {
                        if (cacheQtaMatricole != null) {
                            contaMatricole = CastUtils.toInteger0(cacheQtaMatricole.get(righe.getInt("id")));
                        }
                        if (cacheQtaMatricole == null || contaMatricole > 0) {
                            contaMatricole = 0;
                            ResultSet rMatricole = DbUtils.tryOpenResultSet(Db.getConn(), sqlmatricole);
                            while (rMatricole.next()) {
                                contaMatricole++;
//                                DebugUtils.dump(rMatricole);
                                insertRiga2(testa, righe, 1d, null, rMatricole.getString("matricola"));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (contaMatricole == 0) {
                    insertRiga2(testa, righe, quant, null, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void insertRiga2(ResultSet testa, ResultSet righe, Double quant, String lotto, String matricola) throws SQLException {
        Integer deposito = cu.i(testa.getObject("deposito"));
        if (deposito == null) {
            deposito = cu.i0(main.fileIni.getValue("depositi", "predefinito", "0"));
        }
        Integer deposito_arrivo = cu.i(testa.getObject("deposito_arrivo"));

        String sqltmp = "select ";

        Map m = new HashMap();

        String sql = "insert into movimenti_magazzino (";

        String sqlC = "data";
        String sqlV = "'" + testa.getString("data") + "'";
        m.put("data", testa.getObject("data"));

        sqlC += ", causale";
        int tipo_fattura = 0;
        try {
            if (DbUtils.existColumn(testa, "tipo_fattura")) {
                tipo_fattura = testa.getInt("tipo_fattura");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((tipoDocumento.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA) && tipo_fattura != dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO)
                || tipo_fattura == dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO
                || tipoDocumento == Db.TIPO_DOCUMENTO_DDT_ACQUISTO) {
            sqlV += ", 2"; //causale per il carico
            m.put("causale", 2);
        } else {
            sqlV += ", 3"; //causale che inserisco io da programma per lo scarico di magazzino
            m.put("causale", 3);
        }

        sqlC += ", deposito";
        sqlV += ", " + deposito;
        m.put("deposito", deposito);

        sqlC += ", articolo";
        sqlV += ", " + Db.pc(righe.getString("codice_articolo"), java.sql.Types.VARCHAR);
        m.put("articolo", righe.getString("codice_articolo"));

        articoli_movimentati.add(righe.getString("codice_articolo"));

        Double quantitamov = null;
        if (StringUtils.isBlank(matricola)) {
            if (StringUtils.isBlank(lotto) && quant == null) {
                //se vuoto anche il lotto prendo quantità riga
                quantitamov = righe.getDouble("quantita");
            } else {
                //se lotto presente prendo la qta per lotto passata
                quantitamov = quant;
            }
        } else {
            //se presente matricola imposto qta a 1 
            quantitamov = 1d;
            sqlC += ", matricola";
            sqlV += ", " + Db.pc(matricola, java.sql.Types.VARCHAR);
            m.put("matricola", matricola);
        }
        sqlC += ", quantita";
        sqlV += ", " + Db.pc(quantitamov, java.sql.Types.DECIMAL);
        m.put("quantita", quantitamov);

        sqlC += ", da_tabella";
        sqlV += ", 'test_" + getNomeTabellaFinale(this.tipoDocumento) + "'";
        m.put("da_tabella", "test_" + getNomeTabellaFinale(this.tipoDocumento));

        sqlC += ", da_serie";
        sqlV += ", '" + this.serie + "'";
        m.put("da_serie", serie);

        sqlC += ", da_numero";
        sqlV += ", " + this.numero;
        m.put("da_numero", numero);

        sqlC += ", da_anno";
        sqlV += ", " + this.anno;
        m.put("da_anno", anno);

        sqlC += ", da_tipo_fattura";
        sqlV += ", " + ((tipoFattura == null) ? "null" : tipoFattura);
        m.put("da_tipo_fattura", tipoFattura);

        sqlC += ", da_id";
        try {
            sqlV += ", " + testa.getInt("id");
            m.put("da_id", testa.getInt("id"));
        } catch (Exception e) {
            sqlV += ", " + this.id;
            m.put("da_id", id);
        }

        sqlC += ", da_id_riga";
        try {
            sqlV += ", " + righe.getInt("id");
            m.put("da_id_riga", righe.getInt("id"));
        } catch (Exception e) {
            sqlV += ", null";
            m.put("da_id_riga", null);
        }

        sqlC += ", lotto";
        sqlV += ", " + (lotto == null ? "null" : Db.pc(lotto, Types.VARCHAR));
        m.put("lotto", lotto);

        //costo medio ponderato
        /*
        try {
            sqlC += ", prezzo_unitario";
            sqlV += ", " + dbu.sql(righe.getObject("prezzo"));            
        } catch (Exception e) {
            e.printStackTrace();
            sqlV += ", null";
        }
        try {
            sqlC += ", prezzo_unitario_netto";
            sqlV += ", " + dbu.sql(righe.getObject("prezzo_netto_unitario"));            
        } catch (Exception e) {
            e.printStackTrace();
            sqlV += ", null";
        }
         */
        sql = sql + sqlC + ") values (" + sqlV + ")";

//        System.out.println("sql mov:" + sql);        
        System.out.println("ins mov id_riga:" + righe.getInt("id") + " art:" + righe.getString("codice_articolo") + " \t\t qta:" + quantitamov + " \t\t lotto:" + lotto + " matr:" + matricola);

//        sqls += sql + ";\n";
        sqls += "insert into movimenti_magazzino set " + dbu.prepareSqlFromMap(m) + ";\n";

        //inserisco eventuale movimento su deposito_arrivo
        if (deposito_arrivo != null) {

            Map row = new HashMap();
            row.put("codice_articolo", righe.getString("codice_articolo"));
            row.put("tipo_documento", tipoDocumento);
            row.put("deposito_partenza", deposito);
            row.put("deposito_arrivo", deposito_arrivo);
            InvoicexEvent evt = new InvoicexEvent(row, InvoicexEvent.TYPE_NUOVI_MOVIMENTI_MAGAZZINO_INS_RIGA_DEP_ARRIVO);
            try {
                Object row_evt = main.events.fireInvoicexEventWResult(evt);
                if (row_evt != null) {
                    row = (Map) row_evt;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            sql = "insert into movimenti_magazzino (";
            m.clear();

            sqlC = "data";
            sqlV = "'" + testa.getString("data") + "'";
            m.put("data", testa.getString("data"));

            sqlC += ", causale";
            if ((tipoDocumento.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA) && tipo_fattura != dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO)
                    || tipo_fattura == dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO
                    || tipoDocumento == Db.TIPO_DOCUMENTO_DDT_ACQUISTO) {
                sqlV += ", 3";//inverto per deposito di arrivo
                m.put("causale", 3);
            } else {
                sqlV += ", 2";//inverto per deposito di arrivo
                m.put("causale", 2);
            }

            sqlC += ", deposito";
            sqlV += ", " + deposito_arrivo;
            m.put("deposito", deposito_arrivo);

            sqlC += ", articolo";
            sqlV += ", " + dbu.sql(row.get("codice_articolo"));
            m.put("articolo", row.get("codice_articolo"));

            articoli_movimentati.add(cu.s(row.get("codice_articolo")));

            if (StringUtils.isNotBlank(cu.s(row.get("descrizione")))) {
                sqlC += ", note";
                sqlV += ", " + dbu.sql(cu.s(row.get("descrizione")).trim());
                m.put("note", cu.s(row.get("descrizione")).trim());
            }

            if (StringUtils.isBlank(matricola)) {
                sqlC += ", quantita";
                if (StringUtils.isBlank(lotto)) {
                    //se vuoto anche il lotto prendo quantità riga
                    Double tot = righe.getDouble("quantita");
                    sqlV += ", " + tot;
                    m.put("quantita", tot);
                } else {
                    //se lotto presente prendo la qta per lotto passata
                    sqlV += ", " + quant;
                    m.put("quantita", quant);
                }
            } else {
                //se presente matricola imposto qta a 1 
                sqlC += ", quantita";
                sqlV += ", " + Db.pc(1, java.sql.Types.DECIMAL);
                m.put("quantita", 1);

                sqlC += ", matricola";
                sqlV += ", " + Db.pc(matricola, java.sql.Types.VARCHAR);
                m.put("matricola", matricola);
            }
            sqlC += ", da_tabella";
            sqlV += ", 'test_" + getNomeTabellaFinale(this.tipoDocumento) + "'";
            m.put("da_tabella", "test_" + getNomeTabellaFinale(this.tipoDocumento));

            sqlC += ", da_serie";
            sqlV += ", '" + this.serie + "'";
            m.put("da_serie", serie);

            sqlC += ", da_numero";
            sqlV += ", " + this.numero;
            m.put("da_numero", numero);

            sqlC += ", da_anno";
            sqlV += ", " + this.anno;
            m.put("da_anno", anno);

            sqlC += ", da_tipo_fattura";
            sqlV += ", " + ((tipoFattura == null) ? "null" : tipoFattura);
            m.put("da_tipo_fattura", tipoFattura);

            sqlC += ", da_id";
            try {
                sqlV += ", " + testa.getInt("id");
                m.put("da_id", testa.getInt("id"));
            } catch (Exception e) {
                sqlV += ", " + this.id;
                m.put("da_id", id);
            }

            sqlC += ", da_id_riga";
            try {
                sqlV += ", " + righe.getInt("id");
                m.put("da_id_riga", righe.getInt("id"));
            } catch (Exception e) {
                sqlV += ", null";
                m.put("da_id_riga", null);
            }

            sqlC += ", lotto";
            sqlV += ", " + (lotto == null ? "null" : Db.pc(lotto, Types.VARCHAR));
            m.put("lotto", lotto);

            //costo medio ponderato
            /*
            try {
                sqlC += ", prezzo_unitario";
                sqlV += ", " + dbu.sql(righe.getObject("prezzo"));            
            } catch (Exception e) {
                e.printStackTrace();
                sqlV += ", null";
            }
            try {
                sqlC += ", prezzo_unitario_netto";
                sqlV += ", " + dbu.sql(righe.getObject("prezzo_netto_unitario"));            
            } catch (Exception e) {
                e.printStackTrace();
                sqlV += ", null";
            }
             */
            sql = sql + sqlC + ") values (" + sqlV + ")";

            System.out.println("sql mov arrivo:" + sql);

//            sqls += sql + ";\n";
            sqls += "insert into movimenti_magazzino set " + dbu.prepareSqlFromMap(m) + ";\n";
        }

        //controllo se fare anche movimento di carico in automatico (personal autocarico -> dream flower)
        Integer id_riga = righe.getInt("id");
        if (main.getPersonalContain("autocarico") && id_riga != null && (tipoDocumento.equals(Db.TIPO_DOCUMENTO_DDT) || tipoDocumento.equals(Db.TIPO_DOCUMENTO_FATTURA))) {
            //controllo se lo stato della riga è A allora creo movimento di carico
            try {
                String tab = getNomeTabellaFinale(this.tipoDocumento);
                String stato = cu.s(DbUtils.getObject(Db.getConn(), "select stato from righ_" + tab + " where id = " + id_riga));
                if (stato.equals("A")) {
                    m.clear();

                    sql = "insert into movimenti_magazzino (";
                    sqlC = "data";
                    sqlV = "'" + testa.getString("data") + "'";
                    m.put("data", testa.getString("data"));

                    sqlC += ", causale";
                    sqlV += ", " + Magazzino.CARICO;
                    m.put("causale", Magazzino.CARICO);

                    sqlC += ", deposito";
                    sqlV += ", " + deposito;
                    m.put("deposito", deposito);

                    sqlC += ", articolo";
                    sqlV += ", " + Db.pc(righe.getString("codice_articolo"), java.sql.Types.VARCHAR);
                    m.put("articolo", righe.getString("codice_articolo"));

                    articoli_movimentati.add(righe.getString("codice_articolo"));

                    if (StringUtils.isBlank(matricola)) {
                        sqlC += ", quantita";
                        if (StringUtils.isBlank(lotto)) {
                            Double tot = righe.getDouble("quantita");
                            sqlV += ", " + tot;
                            m.put("quantita", tot);
                        } else {
                            sqlV += ", " + quant;
                            m.put("quantita", quant);
                        }
                    } else {
                        sqlC += ", quantita";
                        sqlV += ", " + Db.pc(1, java.sql.Types.DECIMAL);
                        m.put("quantita", 1);

                        sqlC += ", matricola";
                        sqlV += ", " + Db.pc(matricola, java.sql.Types.VARCHAR);
                        m.put("matricola", matricola);
                    }
                    sqlC += ", da_tabella";
                    sqlV += ", 'test_" + getNomeTabellaFinale(this.tipoDocumento) + "'";
                    m.put("da_tabella", "test_" + getNomeTabellaFinale(this.tipoDocumento));

                    sqlC += ", da_serie";
                    sqlV += ", '" + this.serie + "'";
                    m.put("da_serie", serie);

                    sqlC += ", da_numero";
                    sqlV += ", " + this.numero;
                    m.put("da_numero", numero);

                    sqlC += ", da_anno";
                    sqlV += ", " + this.anno;
                    m.put("da_anno", anno);

                    sqlC += ", da_tipo_fattura";
                    sqlV += ", " + ((tipoFattura == null) ? "null" : tipoFattura);
                    m.put("da_tipo_fattura", tipoFattura);

                    sqlC += ", da_id";
                    try {
                        sqlV += ", " + testa.getInt("id");
                        m.put("da_id", testa.getInt("id"));
                    } catch (Exception e) {
                        sqlV += ", " + this.id;
                        m.put("da_id", id);
                    }
                    sqlC += ", da_id_riga";
                    sqlV += ", " + id_riga;
                    m.put("da_id_riga", id_riga);

                    sqlC += ", lotto";
                    sqlV += ", " + (lotto == null ? "null" : Db.pc(lotto, Types.VARCHAR));
                    m.put("lotto", lotto);

                    sqlC += ", note";
                    sqlV += ", 'carico automatico'";
                    m.put("note", "carico automatico");

                    //costo medio ponderato
                    /*
                    try {
                        sqlC += ", prezzo_unitario";
                        sqlV += ", " + dbu.sql(righe.getObject("prezzo"));            
                    } catch (Exception e) {
                        e.printStackTrace();
                        sqlV += ", null";
                    }
                    try {
                        sqlC += ", prezzo_unitario_netto";
                        sqlV += ", " + dbu.sql(righe.getObject("prezzo_netto_unitario"));            
                    } catch (Exception e) {
                        e.printStackTrace();
                        sqlV += ", null";
                    }
                     */
                    sql = sql + sqlC + ") values (" + sqlV + ")";

                    System.out.println("sql mov autocarico:" + sql);

//                    sqls += sql + ";\n";
                    sqls += "insert into movimenti_magazzino set " + dbu.prepareSqlFromMap(m) + ";\n";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private String getNomeTabellaFinale(String tipo) {
        if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT)) {
            return ("ddt");
        } else if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
            return ("ddt_acquisto");
        } else if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA) || tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_SCONTRINO)) {
            return ("fatt");
        } else if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            return ("fatt_acquisto");
        } else {
            javax.swing.JOptionPane.showMessageDialog(null, "Tipo di documento " + tipo + " inesistente", "Errore", javax.swing.JOptionPane.ERROR_MESSAGE);
            return (null);
        }
    }

    private String getNomeTabellaFinaleLotti(String tipo) {
        if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT)) {
            return ("righ_ddt_lotti");
        } else if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
//            SwingUtils.showErrorMessage(null, "Errore tabella ddt acquisto LOTTI");
            return ("righ_ddt_acquisto_lotti");
        } else if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_ORDINE)) {
            return ("righ_ordi_lotti");
        } else if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
            return ("righ_ordi_acquisto_lotti");
        } else if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA) || tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_SCONTRINO)) {
            return ("righ_fatt_lotti");
        } else if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            return ("righ_fatt_acquisto_lotti");
        } else {
            javax.swing.JOptionPane.showMessageDialog(null, "Tipo di documento " + tipo + " inesistente", "Errore", javax.swing.JOptionPane.ERROR_MESSAGE);
            return (null);
        }
    }

    private String getNomeTabellaFinaleMatricole(String tipo) {
        if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT)) {
            return ("righ_ddt_matricole");
        } else if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
//            SwingUtils.showErrorMessage(null, "Errore tabella ddt acquisto MATRICOLE");
            return ("righ_ddt_acquisto_matricole");
        } else if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA) || tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_SCONTRINO)) {
            return ("righ_fatt_matricole");
        } else if (tipo.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            return ("righ_fatt_acquisto_matricole");
        } else {
            javax.swing.JOptionPane.showMessageDialog(null, "Tipo di documento " + tipo + " inesistente", "Errore", javax.swing.JOptionPane.ERROR_MESSAGE);
            return (null);
        }
    }

    private void inserisciRighe(boolean raggr, int i2, String daddt, ResultSet tempPrev, int newNumero, String newStato, String newSerie, Calendar calendarDataFattura) {
        inserisciRighe(raggr, i2, daddt, tempPrev, newNumero, newStato, newSerie, null, calendarDataFattura);
    }

    private void inserisciRighe(boolean raggr, int i2, String daddt, ResultSet tempPrev, int newNumero, String newStato, String newSerie, Integer id_padre_originale, Calendar calendarDataFattura) {
        Connection conn = Db.getConn();

        String test_fatt = acquisto ? "test_fatt_acquisto" : "test_fatt";
        String test_ddt = acquisto ? "test_ddt_acquisto" : "test_ddt";
        String righ_ddt = acquisto ? "righ_ddt_acquisto" : "righ_ddt";
        String righ_fatt = acquisto ? "righ_fatt_acquisto" : "righ_fatt";
        String tipo_doc = acquisto ? Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA : Db.TIPO_DOCUMENTO_FATTURA;

        int id_padre = -1;
        if (id_padre_originale != null) {
            id_padre = id_padre_originale;
        } else {
            try {
                String sql = "select id from " + test_fatt + " where serie = " + Db.pc(newSerie, Types.VARCHAR);
                sql += " and numero = " + Db.pc(newNumero, Types.INTEGER);
                sql += " and anno = " + Db.pc(calendarDataFattura.get(Calendar.YEAR), "INTEGER");
                if (test_fatt.equalsIgnoreCase("test_fatt")) {
                    sql += " and IFNULL(tipo_fattura,0) != 7";
                }
                id_padre = CastUtils.toInteger(DbUtils.getObject(conn, sql));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            ResultSetMetaData metaPrev = tempPrev.getMetaData();
            //inserisco riga di provenienza
            String sqlC = "";
            String sqlV = "";
            String sqlbatch = "";
            contaRighe += iu.getRigaInc();
            
            //prendo dati di provenienza
            ResultSet tempDdtProv = null;
            String sql = "";
            if (!raggr) {
                sql = "select serie,numero,data,id from " + test_ddt + " t ";
                sql += " where t.id = " + ids[i2];
                sql += " order by serie, anno, numero";
                System.out.println("DDT_FATT_SQL>" + sql);
                tempDdtProv = Db.openResultSet(sql);
                tempDdtProv.next();
            }

            ResultSet colonne = Db.openResultSet("select * from " + righ_fatt + " limit 0");

            //inserisco riga di descrzione
            /*
            for (int i = 1; i <= metaPrev.getColumnCount(); i++) {
                if (!DbUtils.existColumn(colonne, metaPrev.getColumnName(i))) {
                    continue;
                }
                if (metaPrev.getColumnName(i).equalsIgnoreCase("id")) {
                    continue;
                }
                if (metaPrev.getColumnName(i).equalsIgnoreCase("data")) {
                    continue;
                }
                if (metaPrev.getColumnName(i).equalsIgnoreCase("dataddt")) {
                    continue;
                }
                if (metaPrev.getColumnName(i).equalsIgnoreCase("sqta")) {
                    continue;
                }
                if (metaPrev.getColumnName(i).equalsIgnoreCase("iva_deducibile")) {
                    continue;
                }
                if (metaPrev.getColumnName(i).equalsIgnoreCase("numero")) {
                    sqlC += "numero";
                    sqlV += Db.pc(String.valueOf(newNumero), metaPrev.getColumnType(i));
                } else if (metaPrev.getColumnName(i).equalsIgnoreCase("stato")) {
                    sqlC += "stato";
                    sqlV += Db.pc(newStato, metaPrev.getColumnType(i));
                } else if (metaPrev.getColumnName(i).equalsIgnoreCase("serie")) {
                    sqlC += "serie";
                    sqlV += Db.pc(Db.nz(serie, ""), metaPrev.getColumnType(i));
                } else if (metaPrev.getColumnName(i).equalsIgnoreCase("anno")) {
                    sqlC += "anno";
                    sqlV += Db.pc(calendarDataFattura.get(Calendar.YEAR), "INTEGER");
                } else if (metaPrev.getColumnName(i).equalsIgnoreCase("id_padre")) {
                    sqlC += "id_padre";
                    sqlV += id_padre;
                } else if (metaPrev.getColumnName(i).equalsIgnoreCase("riga")) {
                    sqlC += "riga";
                    sqlV += Db.pc(String.valueOf(contaRighe), metaPrev.getColumnType(i));
                } else if (metaPrev.getColumnName(i).equalsIgnoreCase("prezzo")) {
                    sqlC += "prezzo";
                    sqlV += Db.pc("0", metaPrev.getColumnType(i));
                } else if (metaPrev.getColumnName(i).equalsIgnoreCase("prezzo_ivato")) {
                    sqlC += "prezzo_ivato";
                    sqlV += Db.pc("0", metaPrev.getColumnType(i));
                } else if (metaPrev.getColumnName(i).equalsIgnoreCase("totale_ivato")) {
                    sqlC += "totale_ivato";
                    sqlV += "0";
                } else if (metaPrev.getColumnName(i).equalsIgnoreCase("totale_imponibile")) {
                    sqlC += "totale_imponibile";
                    sqlV += "0";
                } else if (metaPrev.getColumnName(i).equalsIgnoreCase("descrizione")) {
                    sqlC += "descrizione";
                    if (raggr) {
                        sqlV += Db.pc(daddt, metaPrev.getColumnType(i));
                    } else {
                        sqlV += Db.pc(main.fileIni.getValue("altro", "da_ddt", "*** Da DDT") + " numero " + Db.nz(tempDdtProv.getString("serie"), "") + tempDdtProv.getString("numero") + " del " + sdf.format(tempDdtProv.getDate("data")), metaPrev.getColumnType(i));
                    }
                } else {
                    sqlC += metaPrev.getColumnName(i);
                    sqlV += Db.pc("", metaPrev.getColumnType(i));
                }

                if (i < metaPrev.getColumnCount()) {
                    sqlC += ",";
                    sqlV += ",";
                }
            }

            if (sqlC.endsWith(",")) {
                sqlC = sqlC.substring(0, sqlC.length() - 1);
            }
            if (sqlV.endsWith(",")) {
                sqlV = sqlV.substring(0, sqlV.length() - 1);
            }

            //inserisco ordine di provenienza
            if (!raggr) {
                sqlC += ", da_ddt";
                sqlV += "," + tempDdtProv.getString("id");
            }

            sql = "insert into " + righ_fatt + " ";
            sql += "(" + sqlC + ") values (" + sqlV + ")";
             */
            
            Map m = new HashMap();
            m.put("numero", newNumero);
            m.put("stato", newStato);
            m.put("serie", serie);
            m.put("anno", calendarDataFattura.get(Calendar.YEAR));
            m.put("id_padre", id_padre);
            m.put("riga", contaRighe);
            m.put("prezzo", 0);
            m.put("prezzo_ivato", 0);
            m.put("totale_ivato", 0);
            m.put("totale_imponibile", 0);
            if (raggr) {
                m.put("descrizione", daddt);
            } else {
                m.put("descrizione", main.fileIni.getValue("altro", "da_ddt", "*** Da DDT") + " numero " + Db.nz(tempDdtProv.getString("serie"), "") + tempDdtProv.getString("numero") + " del " + sdf.format(tempDdtProv.getDate("data")));
            }
            if (!raggr) {
                m.put("da_ddt", tempDdtProv.getString("id"));
            }
            
            sql = "insert into " + righ_fatt + " set " + dbu.prepareSqlFromMap(m);

            System.out.println("sql:" + sql);
            Db.executeSql(sql);

            //inserisco le righe sorgenti
            while (tempPrev.next() == true) {
                sqlC = "";
                sqlV = "";
                contaRighe += iu.getRigaInc();
                
                /*
                String virgola = ",";
                for (int i = 1; i <= metaPrev.getColumnCount(); i++) {
                    virgola = ",";

                    if (!DbUtils.existColumn(colonne, metaPrev.getColumnName(i))) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("id")) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("data")) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("dataddt")) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("sqta")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("stotale_imponibile")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("stotale_ivato")) {
                        continue;
                    }

                    if (metaPrev.getColumnName(i).equalsIgnoreCase("numero")) {
                        sqlC += "numero";
                        sqlV += Db.pc(String.valueOf(newNumero), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("serie")) {
                        sqlC += "serie";
                        sqlV += Db.pc(newSerie, metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("stato")) {
                        sqlC += "stato";
                        sqlV += Db.pc(newStato, metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("riga")) {
                        sqlC += "riga";
                        sqlV += Db.pc(String.valueOf(contaRighe), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("anno")) {
                        sqlC += "anno";
                        sqlV += Db.pc(calendarDataFattura.get(Calendar.YEAR), "INTEGER");
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("id_padre")) {
                        sqlC += "id_padre";
                        sqlV += id_padre;
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("prezzo")) {
                        sqlC += "prezzo";
                        sqlV += Db.pc(Db.nz(tempPrev.getObject(i), "0"), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("prezzo_ivato")) {
                        sqlC += "prezzo_ivato";
                        sqlV += Db.pc(Db.nz(tempPrev.getObject(i), "0"), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("quantita")) {
                        sqlC += "quantita";
                        if (raggr) {
                            sqlV += Db.pc(Db.nz(tempPrev.getObject("sqta"), "0"), metaPrev.getColumnType(i));
                        } else {
                            sqlV += Db.pc(Db.nz(tempPrev.getObject("quantita"), "0"), metaPrev.getColumnType(i));
                        }
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("totale_imponibile")) {
                        sqlC += "totale_imponibile";
                        if (raggr) {
                            sqlV += Db.pc(Db.nz(tempPrev.getObject("stotale_imponibile"), "0"), metaPrev.getColumnType(i));
                        } else {
                            sqlV += Db.pc(Db.nz(tempPrev.getObject("totale_imponibile"), "0"), metaPrev.getColumnType(i));
                        }
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("totale_ivato")) {
                        sqlC += "totale_ivato";
                        if (raggr) {
                            sqlV += Db.pc(Db.nz(tempPrev.getObject("stotale_ivato"), "0"), metaPrev.getColumnType(i));
                        } else {
                            sqlV += Db.pc(Db.nz(tempPrev.getObject("totale_ivato"), "0"), metaPrev.getColumnType(i));
                        }
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("quantita_evasa")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("in_ddt")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("in_fatt")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("da_ordi")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("da_ddt")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("in_ddt_riga")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("in_fatt_riga")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("da_ordi_riga")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("da_ddt_riga")) {
                        virgola = "";
                    } else {
                        sqlC += metaPrev.getColumnName(i);
                        sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                    }

                    if (i != metaPrev.getColumnCount()) {
                        sqlC += virgola;
                        sqlV += virgola;
                    }
                }
                if (sqlC.endsWith(",")) {
                    sqlC = sqlC.substring(0, sqlC.length() - 1);
                }
                if (sqlV.endsWith(",")) {
                    sqlV = sqlV.substring(0, sqlV.length() - 1);
                }

                //aggiungo id di provenienza di
                if (!raggr) {
                    sqlC += ", da_ddt, da_ddt_riga";
                    sqlV += ", " + tempDdtProv.getInt("id") + ", " + tempPrev.getInt("id");
                }

                sql = "insert into " + righ_fatt + " ";
                sql += "(" + sqlC + ") values (" + sqlV + ")";
                */
                
                m = dbu.getRowMap(tempPrev);
                Set toremove = new HashSet();
                for (Object o : m.entrySet()) {
                    Map.Entry entry = (Map.Entry)o;
                    if (!dbu.existColumn(colonne, cu.s(entry.getKey()))) {
                        toremove.add(entry.getKey());
                    }
                }
                m.keySet().removeAll(toremove);
                m.remove("id");
                m.remove("data");
                m.remove("dataddt");
                m.remove("sqta");
                m.remove("stotale_imponibile");
                m.remove("stotale_ivato");
                m.remove("stotale_imponibile");
                m.remove("stotale_imponibile");
                m.remove("stotale_imponibile");
                m.put("numero", newNumero);
                m.put("serie", newSerie);
                m.put("stato", newStato);
                m.put("riga", contaRighe);
                m.put("anno", calendarDataFattura.get(Calendar.YEAR));
                m.put("id_padre", id_padre);
                m.put("prezzo", Db.nz(tempPrev.getObject("prezzo"), "0"));
                m.put("prezzo_ivato", Db.nz(tempPrev.getObject("prezzo_ivato"), "0"));
                if (raggr) {
                    m.put("quantita", Db.nz(tempPrev.getObject("sqta"), "0"));
                } else {
                    m.put("quantita", Db.nz(tempPrev.getObject("quantita"), "0"));
                }                
                if (raggr) {
                    m.put("totale_imponibile", Db.nz(tempPrev.getObject("stotale_imponibile"), "0"));
                } else {
                    m.put("totale_imponibile", Db.nz(tempPrev.getObject("totale_imponibile"), "0"));
                }
                if (raggr) {
                    m.put("totale_ivato", Db.nz(tempPrev.getObject("stotale_ivato"), "0"));
                } else {
                    m.put("totale_ivato", Db.nz(tempPrev.getObject("totale_ivato"), "0"));
                }
                m.remove("quantita_evasa");
                m.remove("in_ddt");
                m.remove("in_fatt");
                m.remove("da_ordi");
                m.remove("da_ddt");
                m.remove("in_ddt_riga");
                m.remove("in_fatt_riga");
                m.remove("da_ordi_riga");
                m.remove("da_ddt_riga");
                
                if (!raggr) {
                    m.put("da_ddt", tempDdtProv.getInt("id"));
                    m.put("da_ddt_riga", tempPrev.getInt("id"));
                }

                sql = "insert into " + righ_fatt + " set " + dbu.prepareSqlFromMap(m);
                
                //debug
                System.out.println("DEBUG:" + sql);
                Integer id_riga = Db.executeSqlRetIdDialogExc(conn, sql, false, false);

                //riporto su riga di provenienza l'id della riga generata e l'id di testata
                String campo_in = "in_fatt";

                sql = "update " + righ_ddt + " set " + campo_in + "_riga = " + id_riga;
                sql += " , " + campo_in + " = " + id_padre;
                sql += " where id = " + tempPrev.getInt("id");
                System.out.println("sql = " + sql);
                Db.executeSql(sql);
                
                //eventuali personalizzazioni
                Map params = new HashMap();
                params.put("tab_sorg", righ_ddt);
                params.put("tab_dest", "righ_" + righ_fatt);
                params.put("id_riga_sorg", tempPrev.getInt("id"));
                params.put("id_riga_dest", id_riga);
                InvoicexEvent e = new InvoicexEvent(this, TYPE_GENERIC_ConvDocRiga);
                e.params = params;
                try {
                    main.events.fireInvoicexEventExc(e);
                } catch (Exception ex) {
                    SwingUtils.showExceptionMessage(main.getPadreFrame(), ex);
                }                
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

    }

//    private void inserisciRighePostConferma(boolean raggr, int i2, String daddt, ResultSet tempPrev, int newNumero, String newStato, String newSerie) {
//        String sql;
//        String test_fatt = acquisto ? "test_fatt_acquisto" : "test_fatt";
//        String test_ddt = acquisto ? "test_ddt_acquisto" : "test_ddt";
//        String righ_ddt = acquisto ? "righ_ddt_acquisto" : "righ_ddt";
//        String righ_fatt = acquisto ? "righ_fatt_acquisto" : "righ_fatt";
//        String tipo_doc = acquisto ? Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA : Db.TIPO_DOCUMENTO_FATTURA;
//
//        //metto flag che sono gia' state fatturate
//        if (raggr) {
//            sql = "update " + test_ddt + " t ";
//            sql += " set fattura_serie = " + Db.pc(Db.nz(serie, ""), "VARCHAR");
//            sql += " , fattura_anno = " + Db.pc(Calendar.getInstance().get(Calendar.YEAR), "INTEGER");
//            sql += " , fattura_numero = " + Db.pc(String.valueOf(newNumero), "INTEGER");
//            for (int i = 0; i < elencoDdtR.size(); i++) {
//                if (i == 0) {
//                    sql += " where " + elencoDdtR.get(i);
//                } else {
//                    sql += " or " + elencoDdtR.get(i);
//                }
//            }
//            Db.executeSql(sql);
//        } else {
//            sql = "update " + test_ddt + " t ";
//            sql += " set fattura_serie = " + Db.pc(Db.nz(serie, ""), "VARCHAR");
//            sql += " , fattura_anno = " + Db.pc(Calendar.getInstance().get(Calendar.YEAR), "INTEGER");
//            sql += " , fattura_numero = " + Db.pc(String.valueOf(newNumero), "INTEGER");
//            sql += elencoDdt.get(i2);
//            Db.executeSql(sql);
//        }
//    }
    private void inserisciRigheRiepilogative(int i2, String daddt, ResultSet tempPrev, int newNumero, String newStato, String newSerie, Calendar calendarDataFattura) {
        String test_fatt = acquisto ? "test_fatt_acquisto" : "test_fatt";
        String test_ddt = acquisto ? "test_ddt_acquisto" : "test_ddt";
        String righ_ddt = acquisto ? "righ_ddt_acquisto" : "righ_ddt";
        String righ_fatt = acquisto ? "righ_fatt_acquisto" : "righ_fatt";
        String tipo_doc = acquisto ? Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA : Db.TIPO_DOCUMENTO_FATTURA;

        System.out.println("!!! inseriscirigheriepilogative i2:" + i2 + " daddt:" + daddt + " elencoddt:" + ids[i2]);
        int id_padre = -1;
        try {
            String sql = "select id from " + test_fatt + " where serie = " + Db.pc(newSerie, Types.VARCHAR);
            sql += " and numero = " + Db.pc(newNumero, Types.INTEGER);
            sql += " and anno = " + Db.pc(calendarDataFattura.get(Calendar.YEAR), "INTEGER");
            id_padre = CastUtils.toInteger(DbUtils.getObject(Db.getConn(), sql));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ResultSetMetaData metaPrev = tempPrev.getMetaData();
            //inserisco riga di provenienza
            contaRighe += iu.getRigaInc();
            
            //prendo dati di provenienza
            ResultSet tempDdtProv = null;
            String sql = "";

            sql = "select serie,numero,data,anno, id from " + test_ddt + " t ";
            sql += " where t.id = " + ids[i2];
            sql += " order by serie, anno, numero";
            System.out.println("DDT_FATT_SQL>" + sql);
            tempDdtProv = Db.openResultSet(sql);
            tempDdtProv.next();

            //prendo il totale del ddt
            Documento doc = new Documento();
            String tipodoc = Db.TIPO_DOCUMENTO_DDT;
            doc.load(Db.INSTANCE, tempDdtProv.getInt("numero"), tempDdtProv.getString("serie"), tempDdtProv.getInt("anno"), tipodoc, tempDdtProv.getInt("id"));
            doc.calcolaTotali();
            doc.visualizzaCastellettoIva();

            ResultSet colonne = Db.openResultSet("select * from " + righ_fatt + " limit 0");

            Vector<DettaglioIva> ive = InvoicexUtil.getIveDedVector(doc.dettagliIvaDedMap);

            for (DettaglioIva diva : ive) {
                String sqlC = "";
                String sqlV = "";

                for (int i = 1; i <= metaPrev.getColumnCount(); i++) {
                    if (!DbUtils.existColumn(colonne, metaPrev.getColumnName(i))) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("id")) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("data")) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("dataddt")) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("sqta")) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("iva_deducibile")) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("numero")) {
                        sqlC += "numero";
                        sqlV += Db.pc(String.valueOf(newNumero), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("stato")) {
                        sqlC += "stato";
                        sqlV += Db.pc(newStato, metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("serie")) {
                        sqlC += "serie";
                        sqlV += Db.pc(Db.nz(serie, ""), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("anno")) {
                        sqlC += "anno";
                        sqlV += Db.pc(calendarDataFattura.get(Calendar.YEAR), "INTEGER");
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("id_padre")) {
                        sqlC += "id_padre";
                        sqlV += id_padre;
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("riga")) {
                        sqlC += "riga";
                        sqlV += Db.pc(String.valueOf(contaRighe), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("prezzo")) {
                        sqlC += "prezzo";
                        sqlV += Db.pc(diva.getImponibile(), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("prezzo_ivato")) {
                        sqlC += "prezzo_ivato";
                        sqlV += Db.pc(diva.getIvato(), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("descrizione")) {
                        sqlC += "descrizione";
                        sqlV += Db.pc(main.fileIni.getValue("altro", "da_ddt", "*** Da DDT") + " numero " + Db.nz(tempDdtProv.getString("serie"), "") + tempDdtProv.getString("numero") + " del " + sdf.format(tempDdtProv.getDate("data")), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("iva")) {
                        sqlC += "iva";
                        sqlV += Db.pc(diva.getCodice(), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("quantita")) {
                        sqlC += "quantita";
                        sqlV += Db.pc(1, metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("totale_ivato")) {
                        sqlC += "totale_ivato";
                        sqlV += Db.pc(diva.getImponibile() + diva.getImposta(), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("totale_imponibile")) {
                        sqlC += "totale_imponibile";
                        sqlV += Db.pc(diva.getImponibile(), metaPrev.getColumnType(i));
                    } else {
                        sqlC += metaPrev.getColumnName(i);
//                        sqlV += Db.pc("", metaPrev.getColumnType(i));
                        sqlV += Db.pc(null, metaPrev.getColumnType(i));
                    }

                    if (i < metaPrev.getColumnCount()) {
                        sqlC += ",";
                        sqlV += ",";
                    }
                }

                if (sqlC.endsWith(",")) {
                    sqlC = sqlC.substring(0, sqlC.length() - 1);
                }
                if (sqlV.endsWith(",")) {
                    sqlV = sqlV.substring(0, sqlV.length() - 1);
                }
                sql = "insert into " + righ_fatt + " ";
                sql += "(" + sqlC + ") values (" + sqlV + ")";

                System.out.println("sql:" + sql);
                Db.executeSql(sql);

            }
        } catch (Exception err) {
            err.printStackTrace();
        }

    }
//    private void inserisciRigheRiepilogativePostConferma(int i2, String daddt, ResultSet tempPrev, int newNumero, String newStato, String newSerie) {
//        String sql = "";
//        String test_fatt = acquisto ? "test_fatt_acquisto" : "test_fatt";
//        String test_ddt = acquisto ? "test_ddt_acquisto" : "test_ddt";
//        String righ_ddt = acquisto ? "righ_ddt_acquisto" : "righ_ddt";
//        String righ_fatt = acquisto ? "righ_fatt_acquisto" : "righ_fatt";
//        String tipo_doc = acquisto ? Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA : Db.TIPO_DOCUMENTO_FATTURA;
//        //metto flag che sono gia' state fatturate
//        sql = "update " + test_ddt + " t ";
//        sql += " set fattura_serie = " + Db.pc(Db.nz(serie, ""), "VARCHAR");
//        sql += " , fattura_anno = " + Db.pc(Calendar.getInstance().get(Calendar.YEAR), "INTEGER");
//        sql += " , fattura_numero = " + Db.pc(String.valueOf(newNumero), "INTEGER");
//        sql += elencoDdt.get(i2);
//        Db.executeSql(sql);
//    }

    private List<Map> esplodiKit(String articolo_kit, Double qta, boolean kit, boolean servizio, List<Map> ret) throws Exception {
        if (ret == null) {
            ret = new ArrayList();
        }
        if (kit) {
            String sql = "select p.articolo, p.quantita, af.servizio, af.flag_kit from pacchetti_articoli p "
                    + " join articoli ap on p.pacchetto = ap.codice "
                    + " join articoli af on p.articolo = af.codice "
                    + " where pacchetto = " + Db.pcs(articolo_kit);
            List<Map> tmp = DbUtils.getListMap(Db.getConn(), sql);
            //aggiungo il kit stesso, se non flaggato come servizio
            if (!servizio) {
                Map r = new HashMap();
                r.put("articolo", articolo_kit);
                r.put("quantita", qta);
                r.put("kit", true);
                ret.add(r);
            }
            for (Map m : tmp) {
                esplodiKit(cu.s(m.get("articolo")), cu.d0(m.get("quantita")) * qta, cu.toBoolean(m.get("flag_kit")), cu.toBoolean(m.get("servizio")), ret);
            }
        } else if (!servizio) {
            //aggiungo il figlio del kit
            Map r = new HashMap();
            r.put("articolo", articolo_kit);
            r.put("quantita", qta);
            r.put("servizio", servizio);
            ret.add(r);
        }

        return ret;
    }
}
