/**
 * Invoicex
 * Copyright (c) 2005-2016 Marco Ceccarelli, Tnx srl
 *
 * Questo software è soggetto, e deve essere distribuito con la licenza  
 * GNU General Public License, Version 2. La licenza accompagna il software
 * o potete trovarne una copia alla Free Software Foundation http://www.fsf.org .
 *
 * This software is subject to, and may be distributed under, the
 * GNU General Public License, Version 2. The license should have
 * accompanied the software or you may obtain a copy of the license
 * from the Free Software Foundation at http://www.fsf.org .
 * 
 * --
 * Marco Ceccarelli (m.ceccarelli@tnx.it)
 * Tnx snc (http://www.tnx.it)
 *
 */
package gestioneFatture;

import static gestioneFatture.InvoicexEvent.TYPE_GENERIC_ConvDocRiga;
import gestioneFatture.logic.provvigioni.ProvvigioniFattura;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugFastUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.KeyValuePair;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.gui.JDialogSceltaQuantita;
import it.tnx.invoicex.iu;
import java.math.BigDecimal;
import java.sql.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;
import org.apache.commons.lang.StringUtils;
import tnxbeans.*;

public class dbOrdine {

    private Db dbUtil = Db.INSTANCE;
    public String serie;
    public int numero;
    public String stato;
    public int anno;
    public int id;
    public tnxTextField texTota;
    public tnxTextField texTotaIva;
    public tnxTextField texTotaImpo;
    //105
    public int tipoOrdine;
    public static final int TIPO_ORDINE = 0;
    public double sconto1 = 0;
    public double sconto2 = 0;
    public double sconto3 = 0;
    public double speseVarie = 0;
    public double speseTrasportoIva = 0;
    public double speseIncassoIva = 0;
    public double totale;
    public double totaleIva;
    public double totaleImponibile;
    public double totaleDaPagare;
    public String dbStato = "L";
    public static String DB_INSERIMENTO = "I";
    public static String DB_MODIFICA = "M";
    public static String DB_LETTURA = "L";
    public static String CODICE_SUBTOTALE = "##SUBTOTALE##";
    public static String DESCRIZIONE_SUBTOTALE = "Sub-Totale";
    private gestioneFatture.logic.clienti.Cliente cliente;
    private boolean refreshCliente = true;
//    public Vector elenco;
    public boolean acquisto = false;
    public String scontrino = "";
    public Integer[] ids;
    private int useSerie = 1;
    String table_righe_temp = null;

    public dbOrdine() {
    }

    public String converti(String tabellaDest, int useSerie) {
        this.useSerie = useSerie;
        return converti(tabellaDest, false);
    }

    public String converti(String tabellaDest) {
        return converti(tabellaDest, false);
    }

    public String converti(String tabellaDest, boolean proforma) {
        return converti(tabellaDest, proforma, false);
    }

    public String converti(String tabellaDest, String scontrino) {
        this.scontrino = scontrino;
        return converti(tabellaDest, false);
    }

    public String converti(String tabellaDest, boolean proforma, boolean acc) {
        return converti(tabellaDest, useSerie, proforma, acc);
    }
    
    public String converti(String tabellaDest, int useSerie, boolean proforma, boolean acc) {
        return converti(tabellaDest, useSerie, proforma, acc, null);
    }
    
    public String converti(String tabellaDest, int useSerie, boolean proforma, boolean acc, Map batch_param) {
        Connection conn = Db.getConn();
        
        this.useSerie = useSerie;        
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
        String banca_abi = "";
        String banca_cab = "";
        Integer giorno_pagamento = null;
        Integer agente_codice = null;
        Double agente_percentuale = null;
        
        //ordino ids secondo preferenze
        sql = "select id from " + getNomeTab() + " t where id IN (" + StringUtils.join(ids, ",") + ") order by data, serie, numero";
        try {
            List ids2 = dbu.getList(conn, sql);
            ids = new Integer[ids2.size()];
            for (int i = 0; i < ids2.size(); i++) {
                ids[i] = cu.i(ids2.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!main.isBatch) {
            try {
                SwingUtils.inEdtWait(new Runnable() {
                    public void run() {
                        SwingUtils.mouse_wait(main.getPadrePanel().getDesktopPane().getSelectedFrame());
                    }
                });
            } catch (Exception e) {
            }
        }

        String campo_in = "in_ddt";
        String tipoDocDest = acquisto ? Db.TIPO_DOCUMENTO_DDT_ACQUISTO : Db.TIPO_DOCUMENTO_DDT;
        if (tabellaDest.indexOf("fatt") >= 0) {
            campo_in = "in_fatt";
            if (scontrino.equals("")) {
                tipoDocDest = acquisto ? Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA : Db.TIPO_DOCUMENTO_FATTURA;
            } else {
                tipoDocDest = Db.TIPO_DOCUMENTO_SCONTRINO;
            }
        }

        if (proforma) {
            newSerie = "*";
        } else {
            if (this.useSerie == 1) {
                newSerie = this.serie;
            } else if (!main.fileIni.getValue("pref", "riporta_serie", "0").equals("3")) {
                newSerie = "";
            }
        }

        if (!main.isBatch) {
            if (main.fileIni.getValue("pref", "riporta_serie", "0").equals("3")) {
                JDialogSelezionaSerie diaSerie = new JDialogSelezionaSerie(main.getPadreFrame(), true);
                diaSerie.pack();
                diaSerie.setLocationRelativeTo(null);
                diaSerie.setVisible(true);

                newSerie = diaSerie.serie;
            }
        }

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        if (tabellaDest.equals("fatt") || tabellaDest.equals("fatt_acquisto")) {
            int myanno = java.util.Calendar.getInstance().get(Calendar.YEAR);
            sql = "select numero from test_" + tabellaDest;
            if (InvoicexUtil.getTipoNumerazione() == InvoicexUtil.TIPO_NUMERAZIONE_ANNO_INFINITA && myanno >= 2013) {
                sql += " where anno >= 2013";
            } else {
                sql += " where anno = " + myanno;
            }
            sql += " and serie = " + Db.pc(Db.nz(newSerie, ""), "VARCHAR");
            if (!acquisto) {
                if (this.scontrino.equals("SC")) {
                    sql += " and data = " + Db.pc(new java.util.Date(), Types.DATE);
                    sql += " and tipo_fattura = 7";
                } else {
                    sql += " and tipo_fattura != 7";
                }
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
        } else {
            sql = "select numero from test_" + tabellaDest;
            if (scontrino.equals("")) {
                sql += " where serie = " + Db.pc(Db.nz(newSerie, ""), "VARCHAR");
                //sql += " and stato = " + Db.pc("P", "VARCHAR");
                sql += " and anno = " + Db.pc(Calendar.getInstance().get(Calendar.YEAR), "INTEGER");
                if (tabellaDest.equals("fatt")) {
                    sql += " and tipo_fattura != '7'";
                }
                sql += " order by numero desc";
            } else {
                //scontrino
                sql += " where data = " + Db.pc(new java.util.Date(), Types.DATE);
                sql += " and tipo_fattura = '7'";
                sql += " order by numero desc limit 1";
            }
            newNumero = 1;
            try {
                ResultSet tempUltimo = Db.openResultSet(sql);
                if (tempUltimo.next() == true) {
                    newNumero = tempUltimo.getInt("numero") + 1;
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
        

        newStato = "P";

        //inserisco nuova fattura salvandomi i dati su hashtable
        //e selzionando dai ddt
        int contaRighe = 0;
        sql = "select * from " + getNomeTab() + " t ";
        sql += " where t.id = " + ids[0];
        ResultSet tempPrev = Db.openResultSet(sql);
        ResultSet ordiPrev = tempPrev;

        String campoc = "cliente";
        if (acquisto) {
            campoc = "fornitore";
        }
        
        Integer id = null;

        try {

            ResultSetMetaData metaPrev = tempPrev.getMetaData();

            ResultSet resuDest = Db.openResultSet("select * from test_" + tabellaDest + " limit 0");
            ResultSetMetaData metaDest = resuDest.getMetaData();
            List<String> colsDest = new ArrayList<String>();
            for (int i = 1; i <= metaDest.getColumnCount(); i++) {
                colsDest.add(metaDest.getColumnName(i));
            }

            boolean flag = true;

            if (tempPrev.next() == true) {
                codiceCliente = tempPrev.getString(getCampoc());
                speseIncasso = tempPrev.getDouble("spese_incasso");
                speseTrasporto = tempPrev.getDouble("spese_trasporto");

                //calcolo spese di trasporto per somma
                sql = "select sum(IFNULL(spese_incasso, 0)) as tot_spese_incasso"
                        + ", sum(IFNULL(spese_trasporto,0)) as tot_spese_trasporto"
                        + ", sum(IFNULL(sconto,0)) as tot_sconto"
                        + ", sum(IFNULL(acconto,0)) as tot_acconto"
                        + " from " + getNomeTab() + " t ";
                sql += " where ";
                sql += " t.id in (" + StringUtils.join(ids, ",") + ")";
                
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

                for (int i = 1; i <= metaPrev.getColumnCount(); i++) {
                    flag = true;

                    if (metaPrev.getColumnName(i).equalsIgnoreCase("numero")) {
                        sqlC += "numero";
                        sqlV += Db.pc(String.valueOf(newNumero), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("serie")) {
                        sqlC += "serie";
                        sqlV += Db.pc(Db.nz(newSerie, ""), metaPrev.getColumnType(i));                        
                    } else if (!tipoDocDest.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA) && metaPrev.getColumnName(i).equalsIgnoreCase("stato")) {
                        sqlC += "stato";
                        sqlV += Db.pc(newStato, metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("anno")) {
                        sqlC += "anno";
                        sqlV += Db.pc(Calendar.getInstance().get(Calendar.YEAR), "INTEGER");
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
                        Calendar myCalendar = GregorianCalendar.getInstance();
                        sqlC += "data";
                        sqlV += Db.pc(myFormat.format(myCalendar.getTime()), metaPrev.getColumnType(i));
                    } else if (tipoDocDest.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA) && metaPrev.getColumnName(i).equalsIgnoreCase("totale_imponibile")) {
                        sqlC += "imponibile";
                        sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                    } else if (tipoDocDest.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA) && metaPrev.getColumnName(i).equalsIgnoreCase("totale_iva")) {
                        sqlC += "iva";
                        sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                    } else if (tipoDocDest.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA) && metaPrev.getColumnName(i).equalsIgnoreCase("totale")) {
                        sqlC += "importo";
                        sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase(campoc)                            //elenco dei campi che riporta nel documento di destinazione
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
                            || metaPrev.getColumnName(i).equalsIgnoreCase("spese_trasporto")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("spese_incasso")
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
                            || metaPrev.getColumnName(i).equalsIgnoreCase("causale_trasporto")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("aspetto_esteriore_beni")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("numero_colli")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("vettore1")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("mezzo_consegna")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("porto")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("giorno_pagamento")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("conto")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("deposito")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("modalita_consegna")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("modalita_scarico")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("note_consegna")
                            ) {
                        if (metaPrev.getColumnName(i).equalsIgnoreCase("pagamento")) {
                            pagamento = tempPrev.getString(i);
                            if (pagamento == null) {
                                pagamento = "";
                            }
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
                            if (optionVostroOrdine.equals("0") || optionVostroOrdine.equals("")) {
                                sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                            } else {
                                String concat = "ORD " + String.valueOf(tempPrev.getObject("serie")) + String.valueOf(tempPrev.getObject("numero")) + " " + String.valueOf(tempPrev.getObject("anno"));
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
//                        } else if (tempPrev.getObject(i) != null && colsDest.contains(metaPrev.getColumnName(i))) {
                        } else if (colsDest.contains(metaPrev.getColumnName(i))) {
                            sqlC += metaPrev.getColumnName(i);
                            sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                        } else {
                            flag = false;
                        }
                    } else {
                        //ignoro altri campi
                        flag = false;
                    }

                    if (flag == true) {
                        /* ipsoware creava problema, metteva riferimento ma era nullo e non metteva la virgola dopo in caso di riferimento */
//                        if (tempPrev.getObject(i) != null) {
                        sqlC += ",";
                        sqlV += ",";
//                        }
                    }
                }

                if (tabellaDest.equals("fatt")) {
                    //aggiungo tipo fattura fattura immediata = 1
                    sqlC = "tipo_fattura," + sqlC;
                    if (!scontrino.equals("")) {
                        System.out.println("TIPO: " + Db.pc(dbFattura.TIPO_FATTURA_SCONTRINO, Types.INTEGER));
                        sqlV = Db.pc(dbFattura.TIPO_FATTURA_SCONTRINO, Types.INTEGER) + "," + sqlV;
                    } else if (proforma) {
                        sqlV = Db.pc(dbFattura.TIPO_FATTURA_PROFORMA, Types.INTEGER) + "," + sqlV;
                    } else if (acc) {
                        sqlV = Db.pc(dbFattura.TIPO_FATTURA_ACCOMPAGNATORIA, Types.INTEGER) + "," + sqlV;
                    } else {
                        sqlV = Db.pc(dbFattura.TIPO_FATTURA_IMMEDIATA, Types.INTEGER) + "," + sqlV;
                    }
                } else if (tabellaDest.equals("fatt_acquisto")) {
                    //aggiungo tipo fattura fattura immediata = 1
                    sqlC = "tipo_fattura," + sqlC;
                    sqlV = Db.pc(dbFattura.TIPO_FATTURA_ACQUISTO, Types.INTEGER) + "," + sqlV;
                } else if (tabellaDest.equals("ddt")) {
                    String sql_clifor = "select opzione_prezzi_ddt from clie_forn where codice = " + tempPrev.getInt(campoc);
                    List<Map> ret_clifor = DbUtils.getListMap(conn, sql_clifor);
                    if (ret_clifor != null && ret_clifor.size() > 0) {
                        Map rec = ret_clifor.get(0);
                        //aggiungo stampa prezzi
                        if (cu.toString(rec.get("opzione_prezzi_ddt")).equalsIgnoreCase("S")) {
                            sqlC = "opzione_prezzi_ddt," + sqlC;
                            sqlV = "'S'," + sqlV;
                        }
                    }
                }

                sql = "insert into test_" + tabellaDest + " ";
                sql += "(" + sqlC.substring(0, sqlC.length() - 1) + ") values (" + sqlV.substring(0, sqlV.length() - 1) + ")";
                System.out.println("DEBUG:" + sql);
                id = Db.executeSqlRetIdDialogExc(conn, sql, false, false);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        
        //split payment dal cliente
        if (tabellaDest.equals("fatt")) {
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
        
        for (int i2 = 0; i2 < ids.length; i2++) {
            //inserisco righe
            sql = "select r.*, t.data from " + getNomeTab() + " t ";
            sql += "left join " + getNomeTabr() + " r on t.id = r.id_padre";
            sql += " where r.id_padre = " + ids[i2];
            sql += " order by r.riga";
            tempPrev = Db.openResultSet(sql);

            try {

                ResultSetMetaData metaPrev = tempPrev.getMetaData();

                //inserisco riga di provenienza
                sqlC = "";
                sqlV = "";
                contaRighe += iu.getRigaInc();

                //prendo dati di provenienza
                sql = "select serie,numero,data,id,riferimento,stato_ordine from " + getNomeTab() + " t ";
                sql += " where t.id = " + ids[i2];
                System.out.println("ORDI_DDT_SQL>" + sql);

                ResultSet tempDdtProv = Db.openResultSet(sql);
                tempDdtProv.next();

                String virgola;

                for (int i = 1; i <= metaPrev.getColumnCount() - 1; i++) {
                    virgola = ",";

                    if (metaPrev.getColumnName(i).equalsIgnoreCase("id")) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("iva_deducibile")) {
                        continue;
                    }
                    if (metaPrev.getColumnName(i).equalsIgnoreCase("id_padre")) {
                        sqlC += "id_padre";
                        sqlV += String.valueOf(id);
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("numero")) {
                        sqlC += "numero";
                        sqlV += Db.pc(String.valueOf(newNumero), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("stato")) {
                        sqlC += "stato";
                        sqlV += Db.pc(newStato, metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("serie")) {
                        sqlC += "serie";
                        sqlV += Db.pc(Db.nz(newSerie, ""), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("anno")) {
                        sqlC += "anno";
                        sqlV += Db.pc(Calendar.getInstance().get(Calendar.YEAR), "INTEGER");
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("riga")) {
                        sqlC += "riga";
                        sqlV += Db.pc(String.valueOf(contaRighe), metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("prezzo")) {
                        sqlC += "prezzo";
                        sqlV += Db.pc("0", metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("prezzo_ivato")) {
                        sqlC += "prezzo_ivato";
                        sqlV += Db.pc("0", metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase("descrizione")) {
                        sqlC += "descrizione";
                        String stato = cu.s(tempDdtProv.getString("stato_ordine"));
                        String descrizione = "";
                        if (main.fileIni.getValue("altro", "da_preventivo", "").equals("") && main.fileIni.getValue("altro", "da_ordine", "").equals("")) {
                            descrizione = "Da " + stato + " numero " + Db.nz(tempDdtProv.getString("serie"), "") + tempDdtProv.getString("numero") + " del " + sdf.format(tempDdtProv.getDate("data"));
                        } else {
                            if (stato.toLowerCase().indexOf("preventivo") >= 0) {
                                descrizione = main.fileIni.getValue("altro", "da_preventivo", "*** Da Preventivo") + " numero " + Db.nz(tempDdtProv.getString("serie"), "") + tempDdtProv.getString("numero") + " del " + sdf.format(tempDdtProv.getDate("data"));
                            } else {
                                descrizione = main.fileIni.getValue("altro", "da_ordine", "*** Da Ordine") + " numero " + Db.nz(tempDdtProv.getString("serie"), "") + tempDdtProv.getString("numero") + " del " + sdf.format(tempDdtProv.getDate("data"));
                            }
                        }
                        if (!CastUtils.toString(tempDdtProv.getString("riferimento")).equals("")) {
                            descrizione += " / Vostro rif. " + tempDdtProv.getString("riferimento");
                        }
                        sqlV += Db.pc(descrizione, metaPrev.getColumnType(i));
                    } else if (metaPrev.getColumnName(i).equalsIgnoreCase(campoc)
                            || metaPrev.getColumnName(i).equalsIgnoreCase(campoc + "_destinazione")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("id_" + campoc + "_destinazione")
                            || metaPrev.getColumnName(i).equalsIgnoreCase("data_consegna")) {
                        sqlC += metaPrev.getColumnName(i);
//                        sqlV += Db.pc("", metaPrev.getColumnType(i));
                        sqlV += Db.pc(null, metaPrev.getColumnType(i));
                    } else {
                        //ignoro gli altri campi
                        virgola = "";
                    }

                    sqlC += virgola;
                    sqlV += virgola;
                }

                sqlC = sqlC.substring(0, sqlC.length() - 1);
                sqlV = sqlV.substring(0, sqlV.length() - 1);
                //inserisco ordine di provenienza
                sqlC += ", da_ordi";
                sqlV += "," + tempDdtProv.getString("id");
                sql = "insert into righ_" + tabellaDest + " ";
                sql += "(" + sqlC + ") values (" + sqlV + ")";
                System.out.println("DEBUG:" + sql);
                Db.executeSql(sql);

                //controllo colonne di destinazione
                ResultSet resuDest = Db.openResultSet("select * from righ_" + tabellaDest + " limit 0");
                ResultSetMetaData metaDest = resuDest.getMetaData();
                List<String> colsDest = new ArrayList<String>();
                for (int i = 1; i <= metaDest.getColumnCount(); i++) {
                    colsDest.add(metaDest.getColumnName(i));
                }

                //inseisco le righe del doc.
                while (tempPrev.next() == true) {
                    sqlC = "";
                    sqlV = "";
                    contaRighe += iu.getRigaInc();
                    for (int i = 1; i <= metaPrev.getColumnCount() - 1; i++) {
                        virgola = ",";
                        if (metaPrev.getColumnName(i).equalsIgnoreCase("id")) {
                            continue;
                        }
                        if (metaPrev.getColumnName(i).equalsIgnoreCase("id_padre")) {
                            sqlC += "id_padre";
                            sqlV += String.valueOf(id);
                        } else if (metaPrev.getColumnName(i).equalsIgnoreCase("numero")) {
                            sqlC += "numero";
                            sqlV += Db.pc(String.valueOf(newNumero), metaPrev.getColumnType(i));
                        } else if (metaPrev.getColumnName(i).equalsIgnoreCase("serie")) {
                            sqlC += "serie";
                            sqlV += Db.pc(Db.nz(newSerie, ""), metaPrev.getColumnType(i));
                        } else if (metaPrev.getColumnName(i).equalsIgnoreCase("stato")) {
                            sqlC += "stato";
                            sqlV += Db.pc(newStato, metaPrev.getColumnType(i));
                        } else if (metaPrev.getColumnName(i).equalsIgnoreCase("riga")) {
                            sqlC += "riga";
                            sqlV += Db.pc(String.valueOf(contaRighe), metaPrev.getColumnType(i));
                        } else if (metaPrev.getColumnName(i).equalsIgnoreCase("anno")) {
                            sqlC += "anno";
                            sqlV += Db.pc(Calendar.getInstance().get(Calendar.YEAR), "INTEGER");
                        } else if (metaPrev.getColumnName(i).equalsIgnoreCase("prezzo")) {
                            sqlC += "prezzo";
                            sqlV += Db.pc(Db.nz(tempPrev.getObject(i), "0"), metaPrev.getColumnType(i));
                        } else if (metaPrev.getColumnName(i).equalsIgnoreCase("prezzo_ivato")) {
                            sqlC += "prezzo_ivato";
                            sqlV += Db.pc(Db.nz(tempPrev.getObject(i), "0"), metaPrev.getColumnType(i));
                        } else if (metaPrev.getColumnName(i).equalsIgnoreCase("doc_tipo")
                                || metaPrev.getColumnName(i).equalsIgnoreCase("doc_serie")
                                || metaPrev.getColumnName(i).equalsIgnoreCase("doc_numero")
                                || metaPrev.getColumnName(i).equalsIgnoreCase("doc_anno")
                                || metaPrev.getColumnName(i).equalsIgnoreCase("doc_riga")
                                || metaPrev.getColumnName(i).equalsIgnoreCase("quantita_evasa")
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
                            if (colsDest.contains(metaPrev.getColumnName(i))) {
                                sqlC += metaPrev.getColumnName(i);
                                sqlV += Db.pc(Db.nz(tempPrev.getObject(i), ""), metaPrev.getColumnType(i));
                            }
                        }

                        if (colsDest.contains(metaPrev.getColumnName(i))) {
                            sqlC += virgola;
                            sqlV += virgola;
                        }
                    }

                    sqlC = sqlC.substring(0, sqlC.length() - 1);
                    sqlV = sqlV.substring(0, sqlV.length() - 1);

                    //aggiungo id di provenienza di
                    sqlC += ", da_ordi, da_ordi_riga";
                    sqlV += ", " + tempDdtProv.getInt("id") + ", " + tempPrev.getInt("id");

                    sql = "insert into righ_" + tabellaDest + " ";
                    sql += "(" + sqlC + ") values (" + sqlV + ")";
                    System.out.println("DEBUG:" + sql);
                    
                    Integer id_riga = Db.executeSqlRetIdDialogExc(conn, sql, false, false);

                    //riporto su riga di provenienza l'id della riga generata e l'id di testata
                    sql = "update " + getNomeTabr() + " set " + campo_in + "_riga = " + id_riga;
                    sql += " , " + campo_in + " = " + id;
                    sql += " where id = " + tempPrev.getInt("id");
                    System.out.println("sql = " + sql);
                    Db.executeSql(sql);
                    
                    //eventuali personalizzazioni
                    Map params = new HashMap();
                    params.put("tab_sorg", getNomeTabr());
                    params.put("tab_dest", "righ_" + tabellaDest);
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

//adesso funziona con il camp oconvertito                
//                //metto flag che sono gia' state fatturate
//                sql = "update " + getNomeTab() + " t";
//                sql += " set doc_serie = " + Db.pc(Db.nz(serie, ""), "VARCHAR");
//                sql += " , doc_anno = " + Db.pc(Calendar.getInstance().get(Calendar.YEAR), "INTEGER");
//                sql += " , doc_numero = " + Db.pc(String.valueOf(newNumero), "INTEGER");
//
//                if (tabellaDest.equalsIgnoreCase("fatt")) {
//                    if (scontrino.equals("")) {
//                        sql += " , doc_tipo = " + Db.pc(Db.TIPO_DOCUMENTO_FATTURA, Types.VARCHAR);
//                    } else {
//                        sql += " , doc_tipo = " + Db.pc(Db.TIPO_DOCUMENTO_SCONTRINO, Types.VARCHAR);
//                    }
//                } else {
//                    sql += " , doc_tipo = " + Db.pc(Db.TIPO_DOCUMENTO_DDT, Types.VARCHAR);
//                }
//
//                sql += elenco.get(i2);
//                Db.executeSql(sql);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }

        //presentare la tabella dei codici art./descr/quantità di origine/quantita evasa o arrivata
        boolean annullato = false;
        JTable table = null;
        JDialogSceltaQuantita dialog = null;
        if (!main.isBatch) {
            dialog = new JDialogSceltaQuantita(main.getPadreFrame(), true);
            dialog.load(acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE, tipoDocDest, ids, proforma);
            dialog.setTitle("Selezione quantità");
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            if (dialog.ok) {
                table = dialog.getTable();
                String pagamentoSnj = "";
                for (int i = 0; i < table.getRowCount(); i++) {
                    //modificare le quantita sul documento generato
                    double qtaconf = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("quantità confermata").getModelIndex()));
                    boolean convertire = CastUtils.toBoolean(table.getValueAt(i, table.getColumn("riga confermata").getModelIndex()));
                    double qta = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("quantità").getModelIndex()));
                    double prezzo = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("prezzo").getModelIndex()));
                    String codart = CastUtils.toString(table.getValueAt(i, table.getColumn("articolo").getModelIndex()));
                    String descr = CastUtils.toString(table.getValueAt(i, table.getColumn("descrizione").getModelIndex()));

    //                if (qtaconf > 0) {
                    if (convertire) {
                        if (main.getPersonalContain("snj") && pagamentoSnj.equals("")) {
                            try {
                                pagamentoSnj = CastUtils.toString(table.getValueAt(i, table.getColumn("termini_pagamento").getModelIndex()));
                                String sqlSnj = "UPDATE test_" + tabellaDest + " set pagamento = " + Db.pc(pagamentoSnj, Types.VARCHAR) + " WHERE id = " + Db.pc(id, Types.INTEGER);
                                Db.executeSql(sqlSnj);
                            } catch (java.lang.IllegalArgumentException e) {
                                //ignoro colonna inesistente
                            }
                        }
                        sql = "update righ_" + tabellaDest + " r ";
                        sql += " set quantita = " + Db.pc(qtaconf, Types.DOUBLE);
                        sql += " where id = " + table.getValueAt(i, table.getColumn("dest_id_riga").getModelIndex());
                    } else {
    //                    if ((prezzo != 0 || qta > 0) && !main.getPersonalContain("proskin")) {
                            sql = "delete from righ_" + tabellaDest + " where id = " + table.getValueAt(i, table.getColumn("dest_id_riga").getModelIndex());
    //                    }
                    }
                    System.out.println("i = " + i + " sql = " + sql);
                    Db.executeSql(sql);
                    //andare sulle righe di origine a mettere la quantita evasa o arrivata
                    if (!dialog.non_evadere_ordine.isSelected()) {
                        Integer prov_id_riga = cu.i(table.getValueAt(i, table.getColumn("prov_id_riga").getModelIndex()));
                        if (prov_id_riga != null) {
                            sql = "update " + getNomeTabr() + " r ";
                            double qta_evasa = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("quantità confermata").getModelIndex()));
                            double qta_gia_evasa = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("quantità già confermata").getModelIndex()));
                            sql += " set quantita_evasa = " + Db.pc(qta_evasa + qta_gia_evasa, Types.DOUBLE);
                            sql += " where id = " + prov_id_riga;
                            System.out.println("i = " + i + " sql = " + sql);
                            Db.executeSql(sql);
                        }
                    }
                }
                
                //controllo presenza di righe "Da preventivo..." che però sono superfluee perchè di quel preventivo non c'è niente
                //per farlo controllo le righe ragguppate per da_ordi, se 1 riga vuol dire che è rimasta solo quella, per ulteriore verifica controllo da_ordi_riga che sia nullo
                sql = "select da_ordi, count(*) as conta from righ_" + tabellaDest + " where id_padre = " + id + " group by da_ordi";
                try {
                    List<Map> listgroup = dbu.getListMap(conn, sql);
                    if (listgroup != null && listgroup.size() > 0) {
                        for (Map m : listgroup) {
                            if (cu.i0(m.get("conta")) == 1 && m.get("da_ordi_riga") == null) {
                                sql = "delete from righ_" + tabellaDest + " where id_padre = " + id + " and da_ordi = " + m.get("da_ordi");
                                dbu.tryExecQuery(conn, sql);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (StringUtils.isNotBlank(pagamentoSnj)) {
                    pagamento = pagamentoSnj;
                }

                //ricalcolare lo stato evasa , parziale , no sul documento di origine
                if (dialog.non_evadere_ordine.isSelected()) {
                    //togliere i riferimenti delle righe altrimenti quando si elimina le va a togliere dall'ordine
                    sql = "update righ_fatt set da_ordi = null, da_ordi_riga = null where id_padre = " + id;
                    try {
                        dbu.tryExecQuery(conn, sql);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                annullato = true;
                table = dialog.getTable();
                Integer old_id = null;
                for (int i = 0; i < table.getRowCount(); i++) {
                    Integer t_id = CastUtils.toInteger(table.getValueAt(i, table.getColumn("dest_id").getModelIndex()));
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
                    sql = "update " + getNomeTabr() + " r ";
                    sql += " set " + campo_in + " = null";
                    sql += " , " + campo_in + "_riga = null";
                    sql += " where id = " + table.getValueAt(i, table.getColumn("prov_id_riga").getModelIndex());
                    System.out.println("i = " + i + " sql = " + sql);
                    Db.executeSql(sql);
                }
                try {
                    // Ripristino a valore precedente riferimenti a fattura
                    if (ordiPrev.first()) {
                        sql = "update " + getNomeTab() + " set ";
                        sql += " doc_tipo = " + Db.pc(ordiPrev.getString("doc_tipo"), Types.VARCHAR) + ", ";
                        sql += " doc_serie = " + Db.pc(ordiPrev.getString("doc_serie"), Types.VARCHAR) + ", ";
                        sql += " doc_numero = " + Db.pc(ordiPrev.getString("doc_numero"), Types.INTEGER) + ", ";
                        sql += " doc_anno = " + Db.pc(ordiPrev.getString("doc_anno"), Types.INTEGER) + ", ";
                        sql += " convertito = " + Db.pc(ordiPrev.getString("convertito"), Types.VARCHAR) + ", ";
                        sql += " evaso = " + Db.pc(ordiPrev.getString("evaso"), Types.VARCHAR) + " ";
                        sql += " WHERE id = " + Db.pc(ordiPrev.getString("id"), Types.INTEGER);

                        Db.executeSql(sql);
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(dbDocumento.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            //batch, controllare le righe da includere
            for (Integer id_testata_origine : ids) {
                if (batch_param != null && batch_param.get("righe_da_includere") != null) {
                    List<Integer> righe_da_includere = (List<Integer>)batch_param.get("righe_da_includere");
                    sql = "select id from " + getNomeTabr() + " where id_padre = " + id_testata_origine + " order by id";
                    try {
                        List list = dbu.getList(conn, sql);
                        for (Object ido : list) {
                            Integer idrigasorgente = cu.i(ido);
                            if (!righe_da_includere.contains(idrigasorgente)) {
                                sql = "delete from righ_" + tabellaDest + " where da_ordi_riga = " + idrigasorgente;
                                System.out.println("sql delete riga non convertita = " + sql);
                                Db.executeSql(sql);
                            } else {
                                sql = "update " + getNomeTabr() + " r ";
                                sql += " set quantita_evasa = quantita";
                                sql += " where id = " + idrigasorgente;
                                System.out.println("sql evasione quantita riga origine = " + sql);
                                Db.executeSql(sql);                        
                            }
                        }                    
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    sql = "update " + getNomeTabr() + " r ";
                    sql += " set quantita_evasa = quantita";
                    sql += " where id_padre = " + id_testata_origine;
                    System.out.println("sql evasione quantita riga origine = " + sql);
                    Db.executeSql(sql);                        
                }
            }
        }
        
        
        if (annullato) {
            return null;
        } else {
            try {
                for (int i = 0; i < ids.length; i++) {
                    sql = "select id from " + getNomeTab() + " t ";
                    sql += " where t.id = " + ids[i];
                    ResultSet doc_origine = Db.openResultSet(sql);
                    if (doc_origine.next()) {
                        InvoicexUtil.aggiornaStatoEvasione(acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE, doc_origine.getInt("id"));
                    }

                    //metto flag che sono gia' state fatturate (solo se passata almeno una riga sul nuovo documento)
                    try {
                        Integer idn = ids[i];
                        //se di un ordine si sono escluse tutte le righe si deve togliere il 'convertito' e le quantità evase
                        sql = "select id from righ_" + tabellaDest + " where da_ordi = " + idn + " and id_padre = " + id;
                        if (dbu.containRows(Db.getConn(), sql)) {
                            String convertito = CastUtils.toString(DbUtils.getObject(conn, "select convertito from " + getNomeTab() + " t where t.id = " + ids[i]));
                            convertito += (convertito.length() > 0 ? "\n" : "") + InvoicexUtil.getNumeroDaId(tipoDocDest, id);
                            sql = "update " + getNomeTab() + " t";
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
            if (!main.isBatch && dialog != null) {
                Integer codice_deposito = null;
                if (dialog.deposito.getSelectedIndex() > 0) {
                    try {
                        codice_deposito = cu.i(((KeyValuePair)dialog.deposito.getSelectedItem()).getKey());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                InvoicexUtil.riportaLotti(table, tabellaDest, getNomeTabr(), tipoDocDest, main.getPadreFrame(), codice_deposito);
            }
        
        }

        //prima di ricalcolare i totali aggiorno le righe
        if (id != null) {
            InvoicexUtil.aggiornaTotaliRighe(tipoDocDest, id);
        }

        //aggiorno abi, cab e pagamento prendendoli dal cliente
        if (pagamento != null && pagamento.length() == 0) {
            sql = "select pagamento, banca_abi, banca_cab, giorno_pagamento from clie_forn";
            sql += " where codice = " + Db.pc(codiceCliente, "INTEGER");

            try {
                ResultSet cliente = Db.openResultSet(sql);
                cliente.next();
                pagamento = cliente.getString("pagamento");
                banca_abi = cliente.getString("banca_abi");
                banca_cab = cliente.getString("banca_cab");
                giorno_pagamento = CastUtils.toInteger(cliente.getInt("giorno_pagamento"));
            } catch (Exception err2) {
                System.out.println("convertiInAltroDoc: impossibile trovare il cliente per banca abi e cab");
            }
        }

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
        gestioneFatture.logic.documenti.Documento doc;
        doc = new gestioneFatture.logic.documenti.Documento();
        if (tabellaDest.indexOf("fatt") >= 0) {
            if (scontrino != null && scontrino.equals("SC")) {
                doc.load(Db.INSTANCE, newNumero, newSerie, Calendar.getInstance().get(Calendar.YEAR), Db.TIPO_DOCUMENTO_SCONTRINO, id);
            } else {
                doc.load(Db.INSTANCE, newNumero, newSerie, Calendar.getInstance().get(Calendar.YEAR), acquisto ? Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA : Db.TIPO_DOCUMENTO_FATTURA, id);
            }
        } else {
            doc.load(Db.INSTANCE, newNumero, newSerie, Calendar.getInstance().get(Calendar.YEAR), acquisto ? Db.TIPO_DOCUMENTO_DDT_ACQUISTO : Db.TIPO_DOCUMENTO_DDT, id);
        }
        doc.calcolaTotali();

        String campo_totale_imponibile = "totale_imponibile";
        String campo_totale_iva = "totale_iva";
        String campo_totale = "totale";
        if (tipoDocDest.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            campo_totale_imponibile = "imponibile";
            campo_totale_iva = "iva";
            campo_totale = "importo";
        }

        sql = "update test_" + tabellaDest + " set ";
        sql += " " + campo_totale_imponibile + " = " + Db.pc(doc.getTotaleImponibile(), "DOUBLE");
        sql += " , " + campo_totale_iva + " = " + Db.pc(doc.getTotaleIva(), "DOUBLE");
        sql += " , " + campo_totale + " = " + Db.pc(doc.getTotale(), "DOUBLE");
        if (tabellaDest.indexOf("fatt") >= 0) {
            sql += " , totale_da_pagare = " + Db.pc(doc.getTotale_da_pagare(), "DOUBLE");
            sql += " , totale_da_pagare_finale = " + Db.pc(doc.getTotale_da_pagare_finale(), "DOUBLE");
        }
        sql += " , totale_imponibile_pre_sconto = " + Db.pc(doc.totaleImponibilePreSconto, "DOUBLE");
        sql += " , totale_ivato_pre_sconto = " + Db.pc(doc.totaleIvatoPreSconto, "DOUBLE");

        //dati cliente
        if (tabellaDest.equalsIgnoreCase("fatt") || tabellaDest.equalsIgnoreCase("ddt")) {
            if (StringUtils.isEmpty(pagamento)) {
                sql += " , pagamento = " + Db.pc(pagamento, "VARCHAR");
                sql += " , banca_abi = " + Db.pc(banca_abi, "VARCHAR");
                sql += " , banca_cab = " + Db.pc(banca_cab, "VARCHAR");
                sql += " , giorno_pagamento = " + Db.pc(giorno_pagamento, "INTEGER");
            }
        }

        if (!tipoDocDest.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            if (agente_codice == null || agente_codice == 0) {
                if (agente != null && agente > 0) {
                    sql += " , agente_codice = " + Db.pc(agente, "VARCHAR");
                }
                if (perc != null) {
                    sql += " , agente_percentuale = " + Db.pc(perc.doubleValue(), "VARCHAR");
                }
            }
        }
        
        //cambio deposito ?
        if (!main.isBatch) {
            if (dialog.cheMovimentaDaDeposito.isVisible() && dialog.cheMovimentaDaDeposito.isSelected() && dialog.deposito.getSelectedItem() instanceof KeyValuePair) {
                System.out.println("cambio deposito in:" + dialog.deposito.getSelectedItem() + " key:" +  ((KeyValuePair)dialog.deposito.getSelectedItem()).key );
                try {
                    sql += " , deposito = " + dbu.sql( ((KeyValuePair)dialog.deposito.getSelectedItem()).key );
                } catch (Exception e) {
                    SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
                }
            }
        }

        sql += " where id = " + id;
        Db.executeSql(sql);

//con gestione scontrini crea problemi!!!
//        //sistemo id_padre
//        if (tabellaDest.equals("fatt")) {
//            sql = "UPDATE righ_fatt r left join test_fatt t on r.serie = t.serie and r.anno = t.anno and r.numero = t.numero set r.id_padre = t.id";
//        } else if (tabellaDest.equals("ddt")) {
//            sql = "UPDATE righ_ddt r left join test_ddt t on r.serie = t.serie and r.anno = t.anno and r.numero = t.numero set r.id_padre = t.id";
//        }
//        sql += " where t.serie = " + Db.pc(Db.nz(newSerie, ""), "VARCHAR");
//        sql += " and t.anno = " + Db.pc(Calendar.getInstance().get(Calendar.YEAR), "INTEGER");
//        sql += " and t.numero = " + Db.pc(newNumero, "LONG");
//        Db.executeSql(sql);

        if (tabellaDest.equals("fatt") && scontrino.equals("")) {
            //genero le scadenze in automatico
            if (Db.contain("pagamenti", "codice", Types.VARCHAR, pagamento)) {
                Scadenze tempScad = new Scadenze(Db.TIPO_DOCUMENTO_FATTURA, id, pagamento);
                tempScad.generaScadenze();
            } else {
                if (!main.isBatch) {
                    SwingUtils.showErrorMessage(main.getPadreFrame(), "Il pagamento " + pagamento + " non esiste !");
                }
                Storico.scrivi("Ordine a doc " + tabellaDest, "non trovato pagamento");
            }
            //genero provvigioni
            if (agente_codice != null) {
                ProvvigioniFattura provvigioni = new ProvvigioniFattura(id, agente_codice, agente_percentuale);
                boolean ret = provvigioni.generaProvvigioni();
                System.out.println("esito genera provvigioni:" + ret + " : " + provvigioni.ret);
            } else {
                try {
                    if (perc != null && perc.doubleValue() > 0d) {
                        ProvvigioniFattura provvigioni = new ProvvigioniFattura(id, agente, perc.doubleValue());
                        boolean ret = provvigioni.generaProvvigioni();
                        System.out.println("esito genera provvigioni:" + ret + " : " + provvigioni.ret);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        boolean ok = true;
        if (!main.getPersonalContain("movimenti_su_proforma") && proforma) {
            ok = false;
        }

        //genero movimenti
        if (ok) {
            dbDocumento temp_doc = new dbDocumento();
            temp_doc.serie = newSerie;
            temp_doc.numero = newNumero;
            temp_doc.anno = Calendar.getInstance().get(Calendar.YEAR);
            if (tabellaDest.equalsIgnoreCase("ddt")) {
                temp_doc.tipoDocumento = Db.TIPO_DOCUMENTO_DDT;
            } else if (tabellaDest.equalsIgnoreCase("ddt_acquisto")) {
                temp_doc.tipoDocumento = Db.TIPO_DOCUMENTO_DDT_ACQUISTO;
            } else if (tabellaDest.equalsIgnoreCase("fatt_acquisto")) {
                temp_doc.tipoDocumento = Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA;
            } else {
                if (scontrino.equals("SC")) {
                    temp_doc.tipoDocumento = Db.TIPO_DOCUMENTO_SCONTRINO;
                    temp_doc.tipoFattura = dbFattura.TIPO_FATTURA_SCONTRINO;
                } else {
                    temp_doc.tipoDocumento = Db.TIPO_DOCUMENTO_FATTURA;
                }
            }
            temp_doc.setId(id);
            boolean genera = false;
            if (scontrino.equals("SC")) {
                genera = true;
            } else {
                if (tabellaDest.equalsIgnoreCase("fatt")) {
                    int tipo_fattura = dbFattura.TIPO_FATTURA_IMMEDIATA;
                    if (proforma) {
                        tipo_fattura = dbFattura.TIPO_FATTURA_PROFORMA;
                    }
                    if (acc) {
                        tipo_fattura = dbFattura.TIPO_FATTURA_ACCOMPAGNATORIA;
                    }
                    genera = InvoicexUtil.generareMovimenti(tipo_fattura, main.getPadreFrame());
                } else {
                    genera = true;
                }
            }
            if (genera) {
                if (temp_doc.generaMovimentiMagazzino() == false) {
                    if (!main.isBatch) {
                        javax.swing.JOptionPane.showMessageDialog(main.getPadreFrame(), "Errore nella generazione dei movimenti di magazzino", "Errore", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        if (!main.isBatch) {
            return (Db.nz(newSerie, "") + " " + newNumero);
        } else {
            return cu.s(id);
        }
    }

    public boolean dbRicalcolaProgressivo(String stato, String data, JTextComponent texNumePrev, JTextComponent texAnno, String serie, Integer id) {

        if (stato == frmTestDocu.DB_INSERIMENTO) {

            //ricreo campo data
            DateFormat myFormat = new SimpleDateFormat("dd/MM/yy");
            java.util.GregorianCalendar myDate = new java.util.GregorianCalendar();
            myFormat.setLenient(false);

            try {
                myDate.setTime(myFormat.parse(data));

                //calcola il progressivo in base alla data e anno
                String sql = "select numero from " + getNomeTab();
                sql += " where anno = " + myDate.get(Calendar.YEAR);
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

    public void dbRefresh() {
        //debug
        System.out.println("!!! richiamata la routine dbFattura.dbRefresh()");

        //calcolo del totale e castelletto iva secondo nuove classi
        gestioneFatture.logic.documenti.Documento doc = new gestioneFatture.logic.documenti.Documento();
        doc.table_righe_temp = table_righe_temp;
        doc.load(Db.INSTANCE, numero, serie, anno, acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE, id);
        doc.calcolaTotali();
        doc.visualizzaCastellettoIva();
        this.totale = doc.getTotale();
        this.totaleImponibile = doc.getTotaleImponibile();
        this.totaleDaPagare = doc.getTotale_da_pagare();
        this.totaleIva = doc.getTotaleIva();
        this.speseVarie = doc.getSpeseVarieImponibili();
        this.speseTrasportoIva = doc.getSpeseTrasporto();
        this.speseIncassoIva = doc.getSpeseIncasso();
    }

    public void setRefreshCliente() {
        this.refreshCliente = true;
    }

    public void forceCliente(long cliente) {
        this.cliente = new gestioneFatture.logic.clienti.Cliente(cliente);
    }

    private String getNomeTab() {
        if (acquisto) {
            return "test_ordi_acquisto";
        } else {
            return "test_ordi";
        }
    }

    private String getNomeTabr() {
        if (acquisto) {
            return "righ_ordi_acquisto";
        } else {
            return "righ_ordi";
        }
    }

    private String getCampoc() {
        if (acquisto) {
            return "fornitore";
        } else {
            return "cliente";
        }
    }
}
