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

import it.tnx.commons.DbUtils;
import it.tnx.commons.dbu;
import it.tnx.invoicex.InvoicexUtil;
import java.sql.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.JTextComponent;

public class dbFattura extends dbDocumento {

    //105
    public int tipoFattura;
    public static final int TIPO_FATTURA_NON_IDENTIFICATA = 0;
    public static final int TIPO_FATTURA_IMMEDIATA = 1;
    public static final int TIPO_FATTURA_ACCOMPAGNATORIA = 2;
    public static final int TIPO_FATTURA_NOTA_DI_CREDITO = 3;
    public static final int TIPO_FATTURA_CUCINAIN_SEMPLICE = 4;
    public static final int TIPO_FATTURA_CUCINAIN_TICKET = 5;
    public static final int TIPO_FATTURA_PROFORMA = 6;
    public static final int TIPO_FATTURA_ACQUISTO = 7;
    public static final int TIPO_FATTURA_SCONTRINO = 7;
    public static final int TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO = 9;
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
    public double totaleSconti;
    public String dbStato = "L";
    public static String DB_INSERIMENTO = "I";
    public static String DB_MODIFICA = "M";
    public static String DB_LETTURA = "L";
    private gestioneFatture.logic.clienti.Cliente cliente;
    private boolean refreshCliente = true;

    public double totaleImponibilePreSconto = 0;
    public double totaleIvatoPreSconto = 0;

    public gestioneFatture.logic.documenti.Documento doc = null;

    public dbFattura() {
    }

    public static boolean isFatturaAnticipata(int id){
        String sql = "SELECT anticipata FROM test_fatt WHERE id = " + Db.pc(id, Types.INTEGER);
        String anticipata = DbUtils.getString(Db.getConn(), sql);
        
        return anticipata.equals("S");
    }
    
    public static boolean bloccaFattura(int id, int tipo_fattura) {

        String table = "";
        if (tipo_fattura == TIPO_FATTURA_ACQUISTO) {
            table = "test_fatt_acquisto";
        } else if (tipo_fattura == TIPO_FATTURA_IMMEDIATA) {
            table = "test_fatt";
        } else {
            return false;
        }

        // Recupero informazioni sulla fattura (id e eventuale data di blocco già impostata)
        String sql = "SELECT bloccata FROM " + table + " WHERE id = " + Db.pc(id, Types.INTEGER);
        System.out.println("SQL Ricerca: " + sql);

        try {
            Object data = dbu.getObject(Db.getConn(), sql, true);
            if (data == null) {
                sql = "UPDATE " + table + " SET bloccata = CURRENT_TIMESTAMP WHERE id = " + Db.pc(id, Types.INTEGER);
                Db.executeSql(sql);
            }
        } catch (Exception ex) {
            Logger.getLogger(dbFattura.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    public static boolean fatturaBloccata(int id, int tipo_fattura) {
        String table = "";
        if (tipo_fattura == TIPO_FATTURA_ACQUISTO || tipo_fattura == TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO) {
            table = "test_fatt_acquisto";
        } else if (tipo_fattura == TIPO_FATTURA_IMMEDIATA || tipo_fattura == TIPO_FATTURA_ACCOMPAGNATORIA || tipo_fattura == TIPO_FATTURA_NOTA_DI_CREDITO || tipo_fattura == TIPO_FATTURA_SCONTRINO) {
            table = "test_fatt";
        } else {
            return false;
        }

        // Recupero informazioni sulla fattura (id e eventuale data di blocco già impostata)
        String sql = "SELECT bloccata FROM " + table + " WHERE id = " + Db.pc(id, Types.INTEGER);
        System.out.println("SQL Ricerca: " + sql);

        try {
            Object data = dbu.getObject(Db.getConn(), sql, true);
            if (data == null) {
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            //Logger.getLogger(dbFattura.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean dbRicalcolaProgressivo(String stato, String data, JTextComponent texNumePrev, JTextComponent texAnno, String serie, Integer id) {

        if (stato.equals(frmTestDocu.DB_INSERIMENTO)) {

            //ricreo campo data
            DateFormat myFormat = new SimpleDateFormat("dd/MM/yy");
            java.util.GregorianCalendar myDate = new java.util.GregorianCalendar();
            myFormat.setLenient(false);

            try {
                myDate.setTime(myFormat.parse(data));
                int myanno = myDate.get(Calendar.YEAR);

                //calcola il progressivo in base alla data e anno
                String sql = "select numero from test_fatt";
                if (InvoicexUtil.getTipoNumerazione() == InvoicexUtil.TIPO_NUMERAZIONE_ANNO_INFINITA && myanno >= 2013) {
                    sql += " where anno >= 2013";
                } else {
                    sql += " where anno = " + myanno;
                }
                sql += " and serie = " + Db.pc(serie, Types.VARCHAR);
                sql += " and tipo_fattura != 7";
                if (id != null) {
                    sql += " and id != " + Db.pc(id, Types.INTEGER);
                }
                sql += " order by numero desc limit 1";

                ResultSet resu = Db.openResultSet(sql);

                if (resu.next() == true) {

                    if (texNumePrev.getText().length() == 0 || !texNumePrev.getText()
                            .equalsIgnoreCase(String.valueOf(resu.getInt(1) + 1))) {
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
        doc = new gestioneFatture.logic.documenti.Documento();
        doc.table_righe_temp = table_righe_temp;
        doc.load(Db.INSTANCE, numero, serie, anno, Db.TIPO_DOCUMENTO_FATTURA, getId());
        doc.calcolaTotali();
        doc.visualizzaCastellettoIva();

        this.totale = doc.getTotale();
        this.totaleImponibile = doc.getTotaleImponibile();
        this.totaleIva = doc.getTotaleIva();
        this.speseVarie = doc.getSpeseVarieImponibili();
        this.speseTrasportoIva = doc.getSpeseTrasporto();
        this.speseIncassoIva = doc.getSpeseIncasso();
        this.totaleDaPagare = doc.getTotale_da_pagare();
        this.totaleImponibilePreSconto = doc.totaleImponibilePreSconto;
        this.totaleIvatoPreSconto = doc.totaleIvatoPreSconto;
    }

    public void setRefreshCliente() {
        this.refreshCliente = true;
    }

    public void forceCliente(long cliente) {
        this.cliente = new gestioneFatture.logic.clienti.Cliente(cliente);
    }

    public static String getDescTipoFattura(Integer tipo) {
        if (tipo == TIPO_FATTURA_ACCOMPAGNATORIA) {
            return "Fattura accompagnatoria";
        }
        if (tipo == TIPO_FATTURA_IMMEDIATA) {
            return "Fattura";
        }
        if (tipo == TIPO_FATTURA_ACQUISTO) {
            return "Fattura";
        }
        if (tipo == TIPO_FATTURA_NOTA_DI_CREDITO) {
            return "Nota di credito";
        }
        if (tipo == TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO) {
            return "Nota di credito";
        }
        return "??? FATTURA ???";
    }

}
