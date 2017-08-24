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
package gestioneFatture.logic.documenti;

import gestioneFatture.Db;
import gestioneFatture.logic.Iva;
import gestioneFatture.logic.clienti.Cliente;
import gestioneFatture.main;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.invoicex.InvoicexUtil;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

public class DocumentoOLD {

    private String sqlPerSconti = null;
    private String sqlPerDettagli = null;
    private String sqlPerDettagliIva = null;
    public Vector dettagliIva = new Vector();
    public Map<String, DettaglioIva> dettagliIvaMap = new LinkedHashMap();
    public Map<IvaDed, DettaglioIva> dettagliIvaDedMap = new LinkedHashMap();
    private Vector dettagliDocumento = new Vector();
    private Map<String, Double> proporzioniIva = new HashMap<String, Double>(); //percentuale iva, proporzione
    private Map<String, Double> proporzioniIvaSconto = new HashMap<String, Double>(); //percentuale iva, sconto
    private Db db = null;
    //variabili d'appoggio
    private double totaImpo = 0;
    private double totaIva = 0;
    private double tota = 0;
    private double totaleQuantita = 0;
    public double totalePeso = 0;
    private double tempSconto1 = 0;
    private double tempSconto2 = 0;
    private double tempSconto3 = 0;
    private double tempSpeseVarie = 0;
    private double tempSpeseTrasportoIva = 0;
    private double tempSpeseIncassoIva = 0;
    private String rivalsa_inps_descrizione;
    private Double rivalsa_inps_perc;
    private gestioneFatture.logic.clienti.Cliente cliente;
    private double scontoTestata1;
    private double scontoTestata2;
    private double scontoTestata3;
    private double totale;
    private double totaleIva;
    private double totaleImponibileParziale;
    private double totaleImponibilePerRivalsa;
    private double totaleImponibile;
    private double totaleImponibilePerRitenuta;
    private double totaleIvatoParziale;
    private double totaleIvato;
    private double totaleIvatoPerRitenuta;
    private double speseTrasporto; //VENGONO INCLUSE NEL CALCOLO DELL'IVA RIPARTENDO SUGLI SCAGLIONI //NON VENGONO SCONTATE
    private double speseVarieImponibili; //VENGONO INCLUSE NEL CALCOLO DELL'IVA RIPARTENDO SUGLI SCAGLIONI //NON VENGONO SCONTATE
    private double speseIncasso; //VENGONO INCLUSE NEL CALCOLO DELL'IVA RIPARTENDO SUGLI SCAGLIONI //NON VENGONO SCONTATE
    public Double speseBollo = null;
    public boolean speseBolloSiNo = false;
    private long codiceCliente;
    private long codiceFornitore;
    private int ritenuta;
    private String ritenuta_descrizione;
    private Integer rivalsa_codice;
    private double ritenuta_perc;
    private double totale_ritenuta;
    private double acconto;
    private double totale_da_pagare;
    private double totale_da_pagare_finale; //al netto degli acconti
    private boolean rivalsa_inps;
    private double totale_rivalsa;
    private double totale_imponibile2;
    private double totaleSconti;
    private Date data;
    private double parametroArrotondamento = 0d;
    private boolean perDifetto = true;
    private boolean prezziIvati = false;
    private String tipoDocumento = "";
    private double sconto;
    public double totaleImponibilePreSconto = 0;
    public double totaleIvatoPreSconto = 0;
    double imponibile = 0;
    double imposta = 0;
    double imposta_noarr = 0;
    double ivato = 0;
//        double totaleImponibileParziale = 0;
//        double totaleImponibile = 0;
//        double totaleImponibilePerRitenuta = 0;
    double totaleImponibilePositivo = 0;
//        double totaleIvatoParziale = 0;
//        double totaleIvato = 0;
//        double totaleIvatoPerRitenuta = 0;
    double totaleIvatoPositivo = 0;
    double totaleImposta = 0;
    double speseImponibili = 0;
    double speseIvate = 0;
//        double totaleSconti = 0;
    public List<DettaglioIva> speseTrasportoDettagli = null;
    public List<DettaglioIva> speseIncassoDettagli = null;

    public Integer id_documento = null;
    public String table_righe_temp;

    public DocumentoOLD() {
        System.out.println("new Documento " + this);
    }

    public boolean load(Db db, int numero, String serie, int anno, String tipoDocumento) {
        return load(db, numero, serie, anno, tipoDocumento, null);
    }

    public boolean load(Db db, String tipoDocumento, Integer id) {
        return load(db, -1, null, -1, tipoDocumento, id);
    }

    public boolean load(Db db, int numero, String serie, int anno, String tipoDocumento, Integer id) {
        this.db = db;
        this.tipoDocumento = tipoDocumento;

        String sql = "select sconto1,sconto2,sconto3, spese_varie, spese_trasporto, spese_incasso, prezzi_ivati, sconto, acconto";
        if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT)) {
            sql += ", data, cliente from test_ddt";
        } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
            sql += ", data, fornitore from test_ddt_acquisto";
        } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA)) {
            sql += ", data, cliente from test_fatt";
            if (id == null) {
                System.err.println("id null");
                String sqlid = "select id from test_fatt";
                sqlid += " where serie = " + db.pc(serie, "VARHCAR");
                sqlid += " and numero = " + db.pc(numero, "LONG");
                sqlid += " and anno = " + db.pc(anno, "INTEGER");
                sqlid += " and tipo_fattura != 7";
                try {
                    ResultSet rid;
                    rid = DbUtils.tryOpenResultSet(Db.getConn(), sqlid);
                    if (rid.next()) {
                        id = rid.getInt("id");
                    }
                    rid.close();
                } catch (Exception ex) {
                    Logger.getLogger(DocumentoOLD.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_SCONTRINO)) {
            sql += ", data, cliente from test_fatt";
            if (id == null) {
                System.err.println("Documento load Scontrino senza id");
            }
        } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_ORDINE)) {
            sql += ", data, cliente from test_ordi";
        } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
            sql += ", data, fornitore from test_ordi_acquisto";
        } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            sql += ", data_doc as data, fornitore as cliente from test_fatt_acquisto";
        } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_PAGAMENTO_RICORRENTE)) {
            sql += ", data, cliente as cliente from test_pagamenti_ricorrenti";
        } else {
            javax.swing.JOptionPane.showMessageDialog(null, "errore in calcolo totale, tipo documento errato", "Errore", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        if (id == null) {
            if (!tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_SCONTRINO)) {
                System.err.println("id null");
                id = InvoicexUtil.getIdDaNumero(tipoDocumento, serie, numero, anno);
//                sql += " where serie = " + db.pc(serie, "VARHCAR");
//                sql += " and numero = " + db.pc(numero, "LONG");
//                sql += " and anno = " + db.pc(anno, "INTEGER");
                if (id == null) {
                    return false;
                }
            }
        }
        sql += " where id = " + id;

        this.id_documento = id;

        sqlPerSconti = sql;

//        sql = "select sum(round((ifnull(prezzo,0) * ifnull(quantita,0)) * (1-ifnull(sconto1,0)/100) * (1-ifnull(sconto2,0)/100),2)) as totaImpo, prezzo, quantita, sconto1, sconto2, iva, percentuale, codici_iva.descrizione,codici_iva.descrizione_breve";
        sql = "select sum(totale_imponibile) as totaImpo, sum(totale_ivato) as totaIvato, prezzo, quantita, sconto1, sconto2, iva, codici_iva.percentuale, codici_iva.descrizione,codici_iva.descrizione_breve";
        sql += ", sum(ifnull(quantita,0)) as quantita";
        sql += ", sum((ifnull(prezzo,0) * ifnull(quantita,0))) as lordo";
        sql += ", sum((ifnull(prezzo,0) * ifnull(quantita,0) * (1 + (codici_iva.percentuale / 100)))) as lordoIvato";

        if (main.fileIni.getValueBoolean("pref", "attivaArrotondamento", false)) {
            sql += ", arrotondamento_parametro, arrotondamento_tipo";
        }

        sql += ", iva_deducibile";

        sql += " from ";

        if (table_righe_temp != null) {
            sql += table_righe_temp;
        } else {
            if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT)) {
                sql += "righ_ddt";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
                sql += "righ_ddt_acquisto";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA) || tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_SCONTRINO)) {
                sql += "righ_fatt";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_ORDINE)) {
                sql += "righ_ordi";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
                sql += "righ_ordi_acquisto";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
                sql += "righ_fatt_acquisto";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_PAGAMENTO_RICORRENTE)) {
                sql += "righ_pagamenti_ricorrenti";
            }
        }

        sql += " left join codici_iva on iva = codice";
        if (id == null) {
//            sql += " where serie = " + db.pc(serie, "VARHCAR");
//            sql += " and numero = " + db.pc(numero, "LONG");
//            sql += " and anno = " + Db.pc(anno, "INTEGER");
        } else {
            sql += " where id_padre = " + id;
        }
//        sql += " group by iva";
        sql += " group by id";
        sqlPerDettagliIva = sql;

        //select perdettagli righe
        sql = "select ifnull(quantita,0) as quantita, a.peso_kg as peso ";
//        sql += ", round((ifnull(prezzo,0) * ifnull(quantita,0)) * (1-ifnull(sconto1,0)/100) * (1-ifnull(sconto2,0)/100),2) as impo";
        sql += ", totale_imponibile as impo";
        sql += ", totale_ivato as ivato";
        sql += ", flag_ritenuta";
        sql += ", flag_rivalsa";
        sql += ", round((ifnull(prezzo,0) * ifnull(quantita,0)),2) as lordo";
        sql += ", round((ifnull(prezzo,0) * ifnull(quantita,0) * (1 + codici_iva.percentuale / 100)), 2) as lordoIvato";

        if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_ORDINE)) {
            sql += ", codice_articolo";
        }
        if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            sql += ", iva_deducibile, r.iva";
        }
        sql += " from ";

        if (table_righe_temp != null) {
            sql += table_righe_temp;
        } else {
            if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT)) {
                sql += "righ_ddt";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
                sql += "righ_ddt_acquisto";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA) || tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_SCONTRINO)) {
                sql += "righ_fatt";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_ORDINE)) {
                sql += "righ_ordi";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
                sql += "righ_ordi_acquisto";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
                sql += "righ_fatt_acquisto";
            } else if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_PAGAMENTO_RICORRENTE)) {
                sql += "righ_pagamenti_ricorrenti";
            }
        }

        sql += " r left join articoli a on r.codice_articolo = a.codice";
        sql += " left join codici_iva on r.iva = codici_iva.codice";
        if (id == null) {
            SwingUtils.showErrorMessage(main.getPadreFrame(), "Problema nel calcolo del totale documento (id = null)", "Errore");
        } else {
            if (cu.i0(id) == 0) {
                SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore in Documento.load id = 0");
            }
            sql += " where id_padre = " + id;
        }
        sqlPerDettagli = sql;
        System.err.println("sql calcolo documento = " + sql);

        //ritenuta
        if ((tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA) || tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) && main.pluginRitenute) {
            try {
                String sqlrit = "select ritenuta";
                sqlrit += " from " + InvoicexUtil.getTabTestateFromTipoDoc(tipoDocumento);
                sqlrit += " where id = " + id;
                ResultSet rrit = DbUtils.tryOpenResultSet(Db.getConn(), sqlrit);
                if (rrit.next()) {
                    if (rrit.getObject("ritenuta") == null || rrit.getInt("ritenuta") <= 0) {
                        ritenuta = 0;
                    } else {
                        ritenuta = rrit.getInt("ritenuta");
                    }
                } else {
                    ritenuta = 0;
                }
                DbUtils.close(rrit);
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
        //rivalsa
        if ((tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA) || tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) && main.pluginRitenute) {
            try {
                String sqlrit = "select rivalsaInpsPerc, rivalsaInpsTesto, rivalsa";
                sqlrit += " from " + InvoicexUtil.getTabTestateFromTipoDoc(tipoDocumento);
                sqlrit += " where id = " + id;
                ResultSet rriv = DbUtils.tryOpenResultSet(Db.getConn(), sqlrit);
                setRivalsa_inps_perc(null);
                setRivalsa_inps_descrizione(null);
                if (rriv.next()) {
                    if (rriv.getObject("rivalsaInpsPerc") == null || rriv.getDouble("rivalsaInpsPerc") <= 0) {
                    } else {
                        setRivalsaCodice(rriv.getInt("rivalsa"));
                        setRivalsa_inps_perc(rriv.getDouble("rivalsaInpsPerc"));
                        setRivalsa_inps_descrizione(rriv.getString("rivalsaInpsTesto"));
                    }
                }
                DbUtils.close(rriv);
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }

        //bollo
        if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA)) {
            try {
                String sqlbollo = "select marca_da_bollo, bollo_presente ";
                sqlbollo += " from " + InvoicexUtil.getTabTestateFromTipoDoc(tipoDocumento);
                sqlbollo += " where id = " + id;
                List<Map> list = dbu.getListMap(Db.getConn(), sqlbollo);
                if (list.size() > 0) {
                    speseBolloSiNo = cu.toBoolean(list.get(0).get("bollo_presente"));
                    if (speseBolloSiNo) {
                        speseBollo = cu.toDouble0(list.get(0).get("marca_da_bollo"));
                    }
                }
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }

        try {
            ResultSet resuSconti = DbUtils.tryOpenResultSet(Db.getConn(), this.sqlPerSconti);
            if (resuSconti.next()) {
                setScontoTestata1(resuSconti.getObject("sconto1") == null ? 0 : resuSconti.getDouble("sconto1"));
                setScontoTestata2(resuSconti.getObject("sconto2") == null ? 0 : resuSconti.getDouble("sconto2"));
                setScontoTestata3(resuSconti.getObject("sconto3") == null ? 0 : resuSconti.getDouble("sconto3"));
                setSpeseTrasporto(resuSconti.getObject("spese_trasporto") == null ? 0 : resuSconti.getDouble("spese_trasporto"));
                setSpeseVarieImponibili(resuSconti.getObject("spese_varie") == null ? 0 : resuSconti.getDouble("spese_varie"));
                setSpeseIncasso(resuSconti.getObject("spese_incasso") == null ? 0 : resuSconti.getDouble("spese_incasso"));
                setData(resuSconti.getDate("data"));
                if (tipoDocumento.equals(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO) || tipoDocumento.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
                    setCodiceCliente(resuSconti.getObject("fornitore") == null ? 0 : resuSconti.getLong("fornitore"));
                } else {
                    setCodiceCliente(resuSconti.getObject("cliente") == null ? 0 : resuSconti.getLong("cliente"));
                }
                if (!resuSconti.getString("prezzi_ivati").equalsIgnoreCase("N")) {
                    setPrezziIvati(true);
                }
                setSconto(resuSconti.getObject("sconto") == null ? 0 : resuSconti.getDouble("sconto"));
                setAcconto(cu.d0(resuSconti.getDouble("acconto")));
            } else {
                DbUtils.close(resuSconti);
                return false;
            }
            DbUtils.close(resuSconti);
        } catch (Exception err) {
            err.printStackTrace();

            return false;
        }

        return true;
    }

    public boolean loadScontrini(Db db, String tipoDocumento, Integer id) {
        this.db = db;

        String sql = "select sconto1,sconto2,sconto3, spese_varie, spese_trasporto, spese_incasso";
        sql += ", cliente from test_fatt";
        sql += " where id = " + id;

        sqlPerSconti = sql;
        sql = "select sum(round((ifnull(prezzo,0) * ifnull(quantita,0)) * (1-ifnull(sconto1,0)/100) * (1-ifnull(sconto2,0)/100),2)) as totaImpo, prezzo, quantita, sconto1, sconto2, iva, percentuale, codici_iva.descrizione,codici_iva.descrizione_breve";
        sql += ", sum(ifnull(quantita,0)) as quantita";
        sql += ", sum(round((ifnull(prezzo,0) * ifnull(quantita,0)),2)) as lordo";
        sql += ", iva_deducibile";

        sql += " from ";

        sql += "righ_fatt";

        sql += " left join codici_iva on iva = codice";
        sql += " where id_padre = " + id;
//        sql += " group by iva";
        sql += " group by id";
        sqlPerDettagliIva = sql;

        //select perdettagli righe
        sql = "select ifnull(quantita,0) as quantita, a.peso_kg as peso ";
        sql += ", round((ifnull(prezzo,0) * ifnull(quantita,0)) * (1-ifnull(sconto1,0)/100) * (1-ifnull(sconto2,0)/100),2) as impo";
        sql += ", flag_ritenuta";
        sql += ", flag_rivalsa";
        sql += ", round((ifnull(prezzo,0) * ifnull(quantita,0)),2) as lordo";
        sql += " from ";

        sql += "righ_fatt";

        sql += " r left join articoli a on r.codice_articolo = a.codice";
        sql += " where id_padre = " + id;
        sqlPerDettagli = sql;

        try {

            ResultSet resuSconti = DbUtils.tryOpenResultSet(Db.getConn(), this.sqlPerSconti);
            if (resuSconti.next()) {
                setScontoTestata1(resuSconti.getObject("sconto1") == null ? 0 : resuSconti.getDouble("sconto1"));
                setScontoTestata2(resuSconti.getObject("sconto2") == null ? 0 : resuSconti.getDouble("sconto2"));
                setScontoTestata3(resuSconti.getObject("sconto3") == null ? 0 : resuSconti.getDouble("sconto3"));
                setSpeseTrasporto(resuSconti.getObject("spese_trasporto") == null ? 0 : resuSconti.getDouble("spese_trasporto"));
                setSpeseVarieImponibili(resuSconti.getObject("spese_varie") == null ? 0 : resuSconti.getDouble("spese_varie"));
                setSpeseIncasso(resuSconti.getObject("spese_incasso") == null ? 0 : resuSconti.getDouble("spese_incasso"));
                setCodiceCliente(resuSconti.getObject("cliente") == null ? 0 : resuSconti.getLong("cliente"));
            } else {
                DbUtils.close(resuSconti);
                return false;
            }
            DbUtils.close(resuSconti);
        } catch (Exception err) {
            err.printStackTrace();

            return false;
        }

        return true;
    }

    public void caricaCliente(long codice) {
        cliente = new Cliente(codice);
    }

    synchronized public void calcolaTotali() {
        calcolaTotali(null);
    }

    
    synchronized public void calcolaTotali(String iva_spese) {

//        double imponibile = 0;
//        double imposta = 0;
//        double imposta_noarr = 0;
//        double ivato = 0;
//        double totaleImponibileParziale = 0;
//        double totaleImponibile = 0;
//        double totaleImponibilePerRitenuta = 0;
//        double totaleImponibilePositivo = 0;
//        double totaleIvatoParziale = 0;
//        double totaleIvato = 0;
//        double totaleIvatoPerRitenuta = 0;
//        double totaleIvatoPositivo = 0;
//
//        double totaleImposta = 0;
//
//        double speseImponibili = 0;
//        double speseIvate = 0;
//
//        double totaleSconti = 0;
        setTotale_rivalsa(0);

        calcolaTotaliSub(iva_spese, 1);

        //calcolo rivalsa e ripeto i calcoli
        //if (getRivalsa_inps_perc() != null && getRivalsa_inps_perc() != 0) {
        if (getRivalsa_inps_perc() != null || ritenuta > 0) {
//            //calcolo il totale imponibile parziale
//            try {
//                ResultSet resuTota = DbUtils.tryOpenResultSet(Db.getConn(), this.sqlPerDettagli);
//                while (resuTota.next()) {
//                    if (!StringUtils.equalsIgnoreCase(resuTota.getString("flag_ritenuta"), "N")) {
//                        if (!prezziIvati) {
//                            //senza iva
//                            if (main.fileIni.getValueBoolean("pref", "attivaArrotondamento", false)) {
//                                imponibile = resuTota.getDouble("impo");
//                                imponibile = InvoicexUtil.calcolaPrezzoArrotondato(imponibile, parametroArrotondamento, perDifetto);
//                                imponibile = imponibile - (imponibile / 100 * scontoTestata1);
//                            } else {
//                                imponibile = resuTota.getDouble("impo") - (resuTota.getDouble("impo") / 100 * scontoTestata1);
//                            }
//                            imponibile = imponibile - (imponibile / 100 * scontoTestata2);
//                            imponibile = imponibile - (imponibile / 100 * scontoTestata3);
//                            imponibile = it.tnx.Util.round(imponibile, 2);
//                            totaleImponibile += imponibile;
//                            totaleImponibilePositivo += imponibile;
//                        } else {
//                            //ivato
//                            ivato = resuTota.getDouble("ivato") - (resuTota.getDouble("ivato") / 100 * scontoTestata1);
//                            ivato = ivato - (ivato / 100 * scontoTestata2);
//                            ivato = ivato - (ivato / 100 * scontoTestata3);
//                            ivato = it.tnx.Util.round(ivato, 2);
//                            totaleIvato += ivato;
//                            totaleIvatoPositivo += ivato;
//
//                            imponibile = imponibile - (imponibile / 100 * scontoTestata2);
//                            imponibile = imponibile - (imponibile / 100 * scontoTestata3);
//                            imponibile = it.tnx.Util.round(imponibile, 2);
//                            totaleImponibile += imponibile;
//                            totaleImponibilePositivo += imponibile;
//                        }
//                    }
//                }
//                DbUtils.close(resuTota);
//                //senza iva
//                speseImponibili = this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso;
//                totaleImponibile -= sconto;
//                totaleImponibile = it.tnx.Util.round(totaleImponibile, 2);
//                totaleImponibilePositivo -= sconto;
//                totaleImponibilePositivo = it.tnx.Util.round(totaleImponibilePositivo, 2);
//                //ivato
//                speseIvate = this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso;
//                totaleIvato -= sconto;
//                totaleIvato = it.tnx.Util.round(totaleIvato, 2);
//                totaleIvatoPositivo -= sconto;
//                totaleIvatoPositivo = it.tnx.Util.round(totaleIvato, 2);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            totaleImponibile = 0;
            totaleImponibilePositivo = 0;
            totaleImponibilePerRitenuta = 0;
            totaleIvato = 0;
            totaleIvatoPositivo = 0;
            totaleIvatoPerRitenuta = 0;
            totaleImposta = 0;
            Iterator<String> ivaiter = dettagliIvaMap.keySet().iterator();
            while (ivaiter.hasNext()) {
                String ivakey = ivaiter.next();
                DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
                totaleImponibile += d.getImponibile();
                totaleImponibilePositivo += d.getImponibile();
                totaleImponibilePerRitenuta += d.getImponibile();
                totaleIvato += d.getIvato();
                totaleIvatoPositivo += d.getIvato();
                totaleIvatoPerRitenuta += d.getIvato();
                totaleImposta += d.getImposta();
            }

            if (getRivalsa_inps_perc() != null) {
                double sommeNonSoggetteRivalsa = 0;
                //tolgo importi non soggetti a rivalsa
                try {
                    ResultSet resuDett = DbUtils.tryOpenResultSet(Db.getConn(), this.sqlPerDettagli);
                    if (resuDett != null) {
                        while (resuDett.next()) {
                            if (StringUtils.equalsIgnoreCase(resuDett.getString("flag_rivalsa"), "N")) {
                                sommeNonSoggetteRivalsa += resuDett.getDouble("impo");
                            }
                        }
                        DbUtils.close(resuDett);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                System.out.println("totale imponibile per rivalsa: " + totaleImponibile);
//                System.out.println("somme non soggette a rivalsa: " + sommeNonSoggetteRivalsa);
                setTotaleImponibilePerRivalsa(totaleImponibile - sommeNonSoggetteRivalsa);
                setTotale_rivalsa(it.tnx.Util.round(getTotaleImponibilePerRivalsa() / 100d * getRivalsa_inps_perc(), 2));
                if (main.fileIni.getValueBoolean("pluginRitenute", "includi_rivalsa", true)) {
                    totaleImponibilePerRitenuta += getTotale_rivalsa();
                }
            }

            //togliere dal totale per ritenuta le righe con flag da non applicare la ritenuta
            try {
                ResultSet resuDett = DbUtils.tryOpenResultSet(Db.getConn(), this.sqlPerDettagli);
                while (resuDett.next()) {
                    if (StringUtils.equalsIgnoreCase(resuDett.getString("flag_ritenuta"), "N")) {
                        //senza iva
                        imponibile = resuDett.getDouble("impo") - (resuDett.getDouble("impo") / 100 * scontoTestata1);
                        imponibile = imponibile - (imponibile / 100 * scontoTestata2);
                        imponibile = imponibile - (imponibile / 100 * scontoTestata3);
                        imponibile = it.tnx.Util.round(imponibile, 2);
                        totaleImponibilePerRitenuta -= imponibile;

                        //ivato
                        ivato = resuDett.getDouble("ivato") - (resuDett.getDouble("ivato") / 100 * scontoTestata1);
                        ivato = ivato - (ivato / 100 * scontoTestata2);
                        ivato = ivato - (ivato / 100 * scontoTestata3);
                        ivato = it.tnx.Util.round(ivato, 2);
                        totaleIvatoPerRitenuta -= ivato;
                    }
                }
                DbUtils.close(resuDett);
            } catch (Exception e) {
                e.printStackTrace();
            }

//        totaleImponibilePerRitenuta -= sconto;
//        totaleImponibilePerRitenuta = it.tnx.Util.round(totaleImponibilePerRitenuta, 2);
//        totaleIvatoPerRitenuta -= sconto;
//        totaleIvatoPerRitenuta = it.tnx.Util.round(totaleIvatoPerRitenuta, 2);
            //calcolo rivalsa spostato in funzione precedente ....
            totaleImponibileParziale = totaleImponibile;
            totaleIvatoParziale = totaleIvato;
        }

        //calcolare totali per ritenuta
//        totaleImponibilePerRitenuta -= sconto;
//        totaleImponibilePerRitenuta = it.tnx.Util.round(totaleImponibilePerRitenuta, 2);
//        totaleIvatoPerRitenuta -= sconto;
//        totaleIvatoPerRitenuta = it.tnx.Util.round(totaleIvatoPerRitenuta, 2);
        calcolaTotaliSub(iva_spese, 2);

        dettagliIva = InvoicexUtil.getIveVector(dettagliIvaMap);
//        DebugFastUtils.dump(dettagliIva);
    }

    
    
    
    
    
    
    
    synchronized public void calcolaTotaliSub(String iva_spese, int passaggio) {

        try {
            caricaCliente(getCodiceCliente());

            totaleQuantita = 0;
            totalePeso = 0;
            totaleSconti = 0;
//            totaleIvatoPerRitenuta = 0;
//            totaleImponibilePerRitenuta = 0;

//            System.out.println("sqlPerDettagli:" + sqlPerDettagli);
//            ResultSet resuDett = this.db.openResultSet(this.sqlPerDettagli);
            if (sqlPerDettagli == null) {
                return;
            }

            System.out.println("sqlPerDettagli = " + sqlPerDettagli);
            ResultSet resuDett = DbUtils.tryOpenResultSet(Db.getConn(), this.sqlPerDettagli);

            if (resuDett == null) {
                return;
            }

            while (resuDett.next()) {
//                if (!this.tipoDocumento.equals(Db.TIPO_DOCUMENTO_ORDINE) || !resuDett.getString("codice_articolo").equals(dbOrdine.CODICE_SUBTOTALE)) {
                totaleQuantita += resuDett.getDouble("quantita");
                if (!StringUtils.equalsIgnoreCase(resuDett.getString("flag_ritenuta"), "N")) {
//                        //senza iva
//                        imponibile = resuDett.getDouble("impo") - (resuDett.getDouble("impo") / 100 * scontoTestata1);
//                        imponibile = imponibile - (imponibile / 100 * scontoTestata2);
//                        imponibile = imponibile - (imponibile / 100 * scontoTestata3);
//                        imponibile = it.tnx.Util.round(imponibile, 2);
//                        totaleImponibilePerRitenuta += imponibile;

                    totaleSconti += resuDett.getDouble("lordo") - imponibile;

                    //ivato
//                        ivato = resuDett.getDouble("ivato") - (resuDett.getDouble("ivato") / 100 * scontoTestata1);
//                        ivato = ivato - (ivato / 100 * scontoTestata2);
//                        ivato = ivato - (ivato / 100 * scontoTestata3);
//                        ivato = it.tnx.Util.round(ivato, 2);
//                        totaleIvatoPerRitenuta += ivato;
                    totaleSconti += resuDett.getDouble("lordoIvato") - ivato;
                }
                try {
                    double pesoriga = resuDett.getDouble("quantita") * resuDett.getDouble("peso");
                    totalePeso += pesoriga;
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
//                }
            }
            DbUtils.close(resuDett);
        } catch (Exception err) {
            err.printStackTrace();
            totaleQuantita = 0;
        }
//        totaleImponibilePerRitenuta -= sconto;
//        totaleImponibilePerRitenuta = it.tnx.Util.round(totaleImponibilePerRitenuta, 2);

//        totaleIvatoPerRitenuta -= sconto;
//        totaleIvatoPerRitenuta = it.tnx.Util.round(totaleIvatoPerRitenuta, 2);
        //calcolo rivalsa spostato in funzione precedente ....
        totaleImponibile = 0;
        totaleImponibilePositivo = 0;
        totaleIvato = 0;
        totaleIvatoPositivo = 0;
        double totaleImponibile_deducibile = 0;
        double totaleImponibile_indeducibile = 0;
        double totaleIvato_deducibile = 0;
        double totaleIvato_indeducibile = 0;

        boolean split_payment = false;
        if (tipoDocumento.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA)) {
            try {
                split_payment = cu.toBoolean(dbu.getObject(Db.getConn(), "select split_payment from test_fatt_xmlpa where id_fattura = " + id_documento));
            } catch (Exception e) {
            }
        }

        try {
            ResultSet resuTota = DbUtils.tryOpenResultSet(Db.getConn(), this.sqlPerDettagliIva);
            dettagliIvaMap.clear();

            while (resuTota.next()) {
                if (main.fileIni.getValueBoolean("pref", "attivaArrotondamento", false)) {
                    setParametriArrotondamento(resuTota.getString("arrotondamento_parametro"), resuTota.getString("arrotondamento_tipo"));
                }
                String kiva = resuTota.getString("iva");
                if (StringUtils.isNotEmpty(kiva)) {
                    //senza iva
                    imponibile = resuTota.getDouble("totaImpo");
                    imponibile = InvoicexUtil.calcolaPrezzoArrotondato(imponibile, parametroArrotondamento, perDifetto);
                    imponibile = imponibile - (imponibile / 100 * scontoTestata1);
                    imponibile = imponibile - (imponibile / 100 * scontoTestata2);
                    imponibile = imponibile - (imponibile / 100 * scontoTestata3);
//                    imponibile = it.tnx.Util.round(imponibile, 2);
                    double perc_deducibilita = resuTota.getObject("iva_deducibile") == null ? 100d : cu.d0(resuTota.getObject("iva_deducibile"));
//                    double imponibile_deducibile = it.tnx.Util.round(imponibile / 100d * perc_deducibilita, 2);
//                    double imponibile_indeducibile = it.tnx.Util.round(imponibile - imponibile_deducibile, 2);
                    double imponibile_deducibile = imponibile / 100d * perc_deducibilita;
                    double imponibile_indeducibile = imponibile - imponibile_deducibile;

                    totaleImponibile += imponibile;
                    totaleImponibilePositivo += imponibile;
                    totaleImponibile_deducibile += imponibile_deducibile;
                    totaleImponibile_indeducibile += imponibile_indeducibile;

                    //con iva
                    ivato = resuTota.getDouble("totaIvato");
                    ivato = InvoicexUtil.calcolaPrezzoArrotondato(ivato, parametroArrotondamento, perDifetto);
                    ivato = ivato - (ivato / 100 * scontoTestata1);
                    ivato = ivato - (ivato / 100 * scontoTestata2);
                    ivato = ivato - (ivato / 100 * scontoTestata3);
//                    ivato = it.tnx.Util.round(ivato, 2);
//                    double ivato_deducibile = it.tnx.Util.round(ivato / 100d * perc_deducibilita, 2);
//                    double ivato_indeducibile = it.tnx.Util.round(ivato - ivato_deducibile, 2);
                    double ivato_deducibile = ivato / 100d * perc_deducibilita;
                    double ivato_indeducibile = ivato - ivato_deducibile;

                    totaleIvato += ivato;
                    totaleIvatoPositivo += ivato;
                    totaleIvato_deducibile += ivato_deducibile;
                    totaleIvato_indeducibile += ivato_indeducibile;

                    //inserisco nei dettagli iva questo gruppo
                    DettaglioIva diva = dettagliIvaMap.get(kiva);
                    if (diva == null) {
                        diva = new DettaglioIva();
                        diva.setCodice(kiva);
                        diva.setDescrizione(resuTota.getString("descrizione"));
                        diva.setDescrizioneBreve(resuTota.getString("descrizione_breve"));
                        diva.setPercentuale(resuTota.getDouble("percentuale"));
                    }
                    //diva.setCodice(resuTota.getString("iva").substring(0, resuTota.getString("iva").length() - 3));

                    diva.setImponibile(cu.d0(diva.getImponibile()) + imponibile);
//                    diva.setImponibile(it.tnx.Util.round(diva.getImponibile(), 2));
                    diva.setImponibile_deducibile(cu.d0(diva.getImponibile_deducibile()) + imponibile_deducibile);
                    diva.setImponibile_indeducibile(cu.d0(diva.getImponibile_indeducibile()) + imponibile_indeducibile);
                    diva.imponibile_noarr = cu.d0(diva.getImponibile());

                    diva.setIvato(cu.d0(diva.getIvato()) + ivato);
//                    diva.setIvato(it.tnx.Util.round(diva.getIvato(), 2));
                    diva.setIvato_deducibile(cu.d0(diva.getIvato_deducibile()) + ivato_deducibile);
                    diva.setIvato_indeducibile(cu.d0(diva.getIvato_indeducibile()) + ivato_indeducibile);
                    diva.ivato_noarr = cu.d0(diva.getIvato());

//                    dettagliIva.add(diva);
                    dettagliIvaMap.put(kiva, diva);
                }
            }
            DbUtils.close(resuTota);

//TODO rileggo per arrotondare ???
//            System.out.println("iva prima");
//            DebugFastUtils.dump(dettagliIvaMap);
            Iterator<String> ivaiter = dettagliIvaMap.keySet().iterator();
            while (ivaiter.hasNext()) {
                String ivakey = ivaiter.next();
                DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
                d.setImponibile(it.tnx.Util.round(d.getImponibile(), 2));
                d.setIvato(it.tnx.Util.round(d.getIvato(), 2));
                d.setImponibile_deducibile(it.tnx.Util.round(d.getImponibile_deducibile(), 2));
                d.setImponibile_indeducibile(it.tnx.Util.round(d.getImponibile_indeducibile(), 2));
                d.setIvato_deducibile(it.tnx.Util.round(d.getIvato_deducibile(), 2));
                d.setIvato_indeducibile(it.tnx.Util.round(d.getIvato_indeducibile(), 2));
            }
//            System.out.println("iva dopo");
//            DebugFastUtils.dump(dettagliIvaMap);

            //controllo che se e' stato inserito il codice iva spese nei parametri allora non ripartiziono ma applico quella indicata
            String codiceIvaSpese = "";
            if (iva_spese == null) {
//                codiceIvaSpese = InvoicexUtil.getIvaSpese();
                codiceIvaSpese = InvoicexUtil.getIvaSpesePerData(data);
            } else {
                codiceIvaSpese = iva_spese;
            }

            gestioneFatture.logic.Iva ivaSpese = new gestioneFatture.logic.Iva();

            if (ivaSpese.load(Db.INSTANCE, codiceIvaSpese)) {

                //calcolo con codice iva spese fissato
                boolean codiceIvaSpeseTrovato = false;
                speseImponibili = this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso;
                speseIvate = this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso;
                proporzioniIva.put(ivaSpese.getCodice(), 100d);
                if (getRivalsa_inps_perc() != null && getRivalsa_inps_perc() != 0) {
                    if (main.fileIni.getValueBoolean("pluginRitenute", "includi_rivalsa_in_iva", true)) {
                        speseImponibili += getTotale_rivalsa();
                        speseIvate += getTotale_rivalsa();
                    }
                }

                ivaiter = dettagliIvaMap.keySet().iterator();
                //for (int i = 0; i < dettagliIva.size(); i++) {
                while (ivaiter.hasNext()) {
                    String ivakey = ivaiter.next();
                    DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
                    if (d.getCodice().equalsIgnoreCase(codiceIvaSpese)) {
                        codiceIvaSpeseTrovato = true;
                        d.setImponibile(d.getImponibile() + this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso);
                        d.setImponibile(it.tnx.Util.round(d.getImponibile(), 2));
                        //metto spese varie sempre deducibili al 100% (TODO: parametrizzare nel documento con percentuale deducibilita per trasporto e icnasso ?)
                        d.setImponibile_deducibile(d.getImponibile_deducibile() + (this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso));
                        d.setIvato(d.getIvato() + this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso);
                        d.setIvato(it.tnx.Util.round(d.getIvato(), 2));
                        d.setIvato_deducibile(d.getIvato_deducibile() + this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso);
                        if (getRivalsa_inps_perc() != null && getRivalsa_inps_perc() != 0) {
                            if (main.fileIni.getValueBoolean("pluginRitenute", "includi_rivalsa_in_iva", true)) {
                                //metto rivalsa sempre deducibile al 100% (TODO: parametrizzare nel documento con percentuale deducibilita per trasporto e icnasso ?)
                                d.setImponibile(d.getImponibile() + getTotale_rivalsa());
                                d.setImponibile(it.tnx.Util.round(d.getImponibile(), 2));
                                d.setImponibile_deducibile(d.getImponibile_deducibile() + getTotale_rivalsa());
                                d.setIvato(d.getIvato() + (getTotale_rivalsa() + it.tnx.Util.round(getTotale_rivalsa() / 100d * d.getPercentuale(), 2)));
                                d.setIvato(it.tnx.Util.round(d.getIvato(), 2));
                                d.setIvato_deducibile(d.getIvato_deducibile() + (getTotale_rivalsa() + it.tnx.Util.round(getTotale_rivalsa() / 100d * d.getPercentuale(), 2)));
                            }
                        }
                    }

                    double imposta_deducibile = 0;
                    double imposta_indeducibile = 0;
                    if (!isPrezziIvati()) {
                        imposta = d.getImponibile() / 100d * d.getPercentuale();
                        imposta_noarr = imposta;
                        imposta = it.tnx.Util.round(imposta, 2);
                        imposta_deducibile = it.tnx.Util.round(d.getImponibile_deducibile() / 100d * d.getPercentuale(), 2);
                        imposta_indeducibile = it.tnx.Util.round(d.getImponibile_indeducibile() / 100d * d.getPercentuale(), 2);
                    } else {
                        imposta = d.getIvato() * d.getPercentuale() / (100d + d.getPercentuale());
                        imposta_noarr = imposta;
                        imposta = it.tnx.Util.round(imposta, 2);
                        imposta_deducibile = it.tnx.Util.round(d.getIvato_deducibile() * d.getPercentuale() / (100d + d.getPercentuale()), 2);
                        imposta_indeducibile = it.tnx.Util.round(d.getIvato_indeducibile() * d.getPercentuale() / (100d + d.getPercentuale()), 2);
                    }

                    //se cliente italiano ok altrimenti niente iva
//                    caricaCliente(getCodiceCliente());
//                    aggiunto codice iva 41
//                    if (cliente != null) {
//                        if (!cliente.getTipoIva().equals(cliente.TIPO_IVA_ITALIA)) {
//                            imposta = 0;
//                        }
//                    }
                    d.setImposta(imposta);
                    d.imposta_noarr = imposta_noarr;
                    d.setImposta_deducibile(imposta_deducibile);
                    d.setImposta_indeducibile(imposta_indeducibile);

                    if (isPrezziIvati()) {
                        d.setImponibile(d.getIvato() - d.getImposta());
                        d.setImponibile_deducibile(d.getIvato_deducibile() - d.getImposta_deducibile());
                        d.setImponibile_indeducibile(d.getIvato_indeducibile() - d.getImposta_indeducibile());
                    }
                }

                if (codiceIvaSpeseTrovato == false) {

                    DettaglioIva ds = new DettaglioIva();
                    ds.setCodice(codiceIvaSpese);
                    ds.setDescrizione(ivaSpese.getDescrizione());
                    ds.setDescrizioneBreve(ivaSpese.getDescrizioneBreve());
                    ds.setImponibile(this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso);
                    //metto spese varie sempre deducibili al 100% (TODO: parametrizzare nel documento con percentuale deducibilita per trasporto e icnasso ?)
                    ds.setImponibile_deducibile(this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso);
                    ds.setIvato(this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso);
                    ds.setIvato_deducibile(this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso);
                    if (getRivalsa_inps_perc() != null && getRivalsa_inps_perc() != 0) {
                        if (main.fileIni.getValueBoolean("pluginRitenute", "includi_rivalsa_in_iva", true)) {
                            ds.setImponibile(ds.getImponibile() + getTotale_rivalsa());
                            ds.setImponibile(it.tnx.Util.round(ds.getImponibile(), 2));
                            ds.setImponibile_deducibile(ds.getImponibile_deducibile() + getTotale_rivalsa());
                        }
                    }

                    double imposta_deducibile = 0;
                    double imposta_indeducibile = 0;
                    if (!isPrezziIvati()) {
                        imposta = ds.getImponibile() / 100d * ivaSpese.getPercentuale();
                        imposta_noarr = imposta;
                        imposta = it.tnx.Util.round(imposta, 2);
                        imposta_deducibile = it.tnx.Util.round(ds.getImponibile_deducibile() / 100d * ivaSpese.getPercentuale(), 2);
                    } else {
                        imposta = ds.getIvato() * ivaSpese.getPercentuale() / (100d + ivaSpese.getPercentuale());
                        imposta_noarr = imposta;
                        imposta = it.tnx.Util.round(imposta, 2);
                        imposta_deducibile = it.tnx.Util.round(ds.getIvato_deducibile() * ivaSpese.getPercentuale() / (100d + ivaSpese.getPercentuale()), 2);
                    }

                    ds.setImposta(imposta);
                    ds.setImposta_deducibile(imposta_deducibile);
                    ds.imposta_noarr = imposta_noarr;
                    ds.setPercentuale(ivaSpese.getPercentuale());
                    if (isPrezziIvati()) {
                        ds.setImponibile(ds.getIvato() - ds.getImposta());
                        ds.setImponibile_deducibile(ds.getIvato_deducibile() - ds.getImposta_deducibile());
                    }
                    if (ds.getImponibile() != 0) {
                        dettagliIvaMap.put(codiceIvaSpese, ds);
                    }
                }

                //ciclo i dettagli per ricaloclare il nuovo toale imponibile e toale imposta
                totaleImponibile = 0;
                totaleImponibilePositivo = 0;
                totaleIvato = 0;
                totaleIvatoPositivo = 0;
                totaleImposta = 0;

                ivaiter = dettagliIvaMap.keySet().iterator();
                //for (int i = 0; i < dettagliIva.size(); i++) {
                while (ivaiter.hasNext()) {
                    String ivakey = ivaiter.next();
                    DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
                    totaleImponibile += d.getImponibile();
                    totaleImponibilePositivo += d.getImponibile();
                    totaleIvato += d.getIvato();
                    totaleIvatoPositivo += d.getIvato();
                    totaleImposta += d.getImposta();
                }
            } else {
                //ciclo per calcolare la proporzione degli imponibili iva e ripartizionarie le spese imponibili
                speseImponibili = this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso;
                speseIvate = this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso;
                if (getRivalsa_inps_perc() != null && getRivalsa_inps_perc() != 0) {
                    if (main.fileIni.getValueBoolean("pluginRitenute", "includi_rivalsa_in_iva", true)) {
//                        speseImponibili += getTotale_rivalsa();
                        //aggiungo la rivalsa sempre sul codice 20/21, se non trovo aggiungo a totale da riproporzionare
                        boolean trovata_iva_std = false;
                        double iva_std = 22d;
                        if (data != null && data.before(DateUtils.getOnlyDate(2011, 9, 17))) {
                            iva_std = 20d;
                        } else if (data != null && data.before(DateUtils.getOnlyDate(2013, 10, 1))) {
                            iva_std = 21d;
                        }
                        ivaiter = dettagliIvaMap.keySet().iterator();
                        //for (int i = 0; i < dettagliIva.size(); i++) {
                        while (ivaiter.hasNext()) {
                            String ivakey = ivaiter.next();
                            DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
                            if (d.getPercentuale() == iva_std) {
                                trovata_iva_std = true;
                                break;
                            }
                        }
                        if (trovata_iva_std) {
                            ivaiter = dettagliIvaMap.keySet().iterator();
                            //for (int i = 0; i < dettagliIva.size(); i++) {
                            while (ivaiter.hasNext()) {
                                String ivakey = ivaiter.next();
                                DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
                                if (d.getPercentuale() == iva_std) {
                                    d.setImponibile(d.getImponibile() + getTotale_rivalsa());
                                    //metto spese varie sempre deducibili al 100% (TODO: parametrizzare nel documento con percentuale deducibilita per trasporto e icnasso ?)
                                    imposta = d.getImponibile() / 100d * d.getPercentuale();
                                    imposta_noarr = imposta;
                                    imposta = it.tnx.Util.round(imposta, 2);
                                    double imposta_deducibile = it.tnx.Util.round(d.getImponibile_deducibile() / 100d * d.getPercentuale(), 2);
                                    d.setImposta(imposta);
                                    d.setImponibile_deducibile(imposta_deducibile);
                                    d.setIvato(d.getImponibile() + imposta);
                                    d.setIvato_deducibile(d.getImponibile_deducibile() + imposta_deducibile);
                                    break;
                                }
                            }
                        } else {
                            if (!isPrezziIvati()) {
                                speseImponibili += getTotale_rivalsa();
                            } else {
                                speseIvate += getTotale_rivalsa();
                            }
                        }
                    }
                }

//ciclo i dettagli per ricaloclare il nuovo toale imponibile e toale imposta
                totaleImponibile = 0;
                totaleImponibilePositivo = 0;
                totaleIvato = 0;
                totaleIvatoPositivo = 0;
                totaleImposta = 0;

                ivaiter = dettagliIvaMap.keySet().iterator();
                //for (int i = 0; i < dettagliIva.size(); i++) {
                while (ivaiter.hasNext()) {
                    String ivakey = ivaiter.next();
                    DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
                    totaleImponibile += d.getImponibile();
                    totaleImponibilePositivo += d.getImponibile();
                    totaleIvato += d.getIvato();
                    totaleIvatoPositivo += d.getIvato();
                    totaleImposta += d.getImposta();
                }

                ivaiter = dettagliIvaMap.keySet().iterator();
                //for (int i = 0; i < dettagliIva.size(); i++) {
                while (ivaiter.hasNext()) {
                    String ivakey = ivaiter.next();
                    DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
                    double importoSpeseRipartito = 0;
                    double nuovoImponibile = 0;
                    double nuovoImponibile_deducibile = 0;
                    double nuovoIvato = 0;
                    double nuovoIvato_deducibile = 0;

                    if (!isPrezziIvati()) {
                        importoSpeseRipartito = speseImponibili * d.getImponibile() / totaleImponibilePositivo;
                        if (totaleImponibilePositivo != 0) {
                            proporzioniIva.put(d.getCodice(), 100d * d.getImponibile() / totaleImponibilePositivo);
                        } else {
                            proporzioniIva.put(d.getCodice(), 0d);
                        }

                        if (!Double.isNaN(importoSpeseRipartito)) {
                            nuovoImponibile = d.getImponibile() + importoSpeseRipartito;
                            //metto spese varie sempre deducibili al 100% (TODO: parametrizzare nel documento con percentuale deducibilita per trasporto e icnasso ?)
                            nuovoImponibile_deducibile = d.getImponibile_deducibile() + importoSpeseRipartito;
                        } else {
                            nuovoImponibile = d.getImponibile();
                            nuovoImponibile_deducibile = d.getImponibile_deducibile();
                        }
                        imposta = nuovoImponibile / 100d * d.getPercentuale();
                        imposta_noarr = imposta;
                        imposta = it.tnx.Util.round(imposta, 2);
                        double imposta_deducibile = it.tnx.Util.round(nuovoImponibile_deducibile / 100d * d.getPercentuale(), 2);
                        d.imponibile_noarr = nuovoImponibile;
                        nuovoImponibile = it.tnx.Util.round(nuovoImponibile, 2);
                        d.setImponibile(nuovoImponibile);
                        d.setImponibile_deducibile(it.tnx.Util.round(nuovoImponibile_deducibile, 2));

                        //se cliente italiano ok altrimenti niente iva
                        //                    caricaCliente(getCodiceCliente());
                        //                  aggiunto codice iva 41
                        //                    if (cliente != null) {
                        //                        if (!cliente.getTipoIva().equals(cliente.TIPO_IVA_ITALIA)) {
                        //                            imposta = 0;
                        //                        }
                        //                    }
                        d.setImposta(imposta);
                        d.setImposta_deducibile(imposta_deducibile);
                        d.imposta_noarr = imposta_noarr;
                    } else {
                        importoSpeseRipartito = speseIvate * d.getIvato() / totaleIvatoPositivo;
                        if (totaleIvatoPositivo != 0) {
                            proporzioniIva.put(d.getCodice(), 100d * d.getIvato() / totaleIvatoPositivo);
                        } else {
                            proporzioniIva.put(d.getCodice(), 0d);
                        }
                        if (!Double.isNaN(importoSpeseRipartito)) {
                            nuovoIvato = d.getIvato() + importoSpeseRipartito;
                            nuovoIvato_deducibile = d.getIvato_deducibile() + importoSpeseRipartito;
                        } else {
                            nuovoIvato = d.getIvato();
                            nuovoIvato_deducibile = d.getIvato_deducibile();
                        }
                        imposta = nuovoIvato * d.getPercentuale() / (100d + d.getPercentuale());
                        imposta_noarr = imposta;
                        imposta = it.tnx.Util.round(imposta, 2);
                        double imposta_deducibile = it.tnx.Util.round(nuovoIvato_deducibile * d.getPercentuale() / (100d + d.getPercentuale()), 2);
                        d.ivato_noarr = nuovoIvato;
                        nuovoIvato = it.tnx.Util.round(nuovoIvato, 2);
                        d.setIvato(nuovoIvato);
                        d.setIvato_deducibile(nuovoIvato_deducibile);
                        d.setImposta(imposta);
                        d.setImposta_deducibile(imposta_deducibile);
                        d.setImponibile(nuovoIvato - imposta);
                        d.setImponibile_deducibile(nuovoIvato_deducibile - imposta_deducibile);
                        d.imposta_noarr = imposta_noarr;
                    }
                }

                //ciclo i dettagli per ricaloclare il nuovo toale imponibile e toale imposta
                totaleImponibile = 0;
                totaleImponibilePositivo = 0;
                totaleIvato = 0;
                totaleIvatoPositivo = 0;
                totaleImposta = 0;

                ivaiter = dettagliIvaMap.keySet().iterator();
                //for (int i = 0; i < dettagliIva.size(); i++) {
                while (ivaiter.hasNext()) {
                    String ivakey = ivaiter.next();
                    DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
                    totaleImponibile += d.getImponibile();
                    totaleImponibilePositivo += d.getImponibile();
                    totaleIvato += d.getIvato();
                    totaleIvatoPositivo += d.getIvato();
                    totaleImposta += d.getImposta();
                }
            }

            //ripartizione sconto a importo
            totaleImponibilePreSconto = totaleImponibile;
            totaleIvatoPreSconto = totaleIvato;
            ivaiter = dettagliIvaMap.keySet().iterator();
            //for (int i = 0; i < dettagliIva.size(); i++) {
            while (ivaiter.hasNext()) {
                String ivakey = ivaiter.next();
                DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
                double scontoRipartito = 0;
                double nuovoImponibile = 0;
                double nuovoIvato = 0;
                if (!isPrezziIvati()) {
                    if (passaggio == 1) {
                        scontoRipartito = sconto * d.getImponibile() / totaleImponibilePositivo;
                        proporzioniIvaSconto.put(d.getCodice(), scontoRipartito);
                        if (totaleImponibilePositivo != 0) {
                            proporzioniIva.put(d.getCodice(), 100d * d.getImponibile() / totaleImponibilePositivo);
                        } else {
                            proporzioniIva.put(d.getCodice(), 0d);
                        }
                    } else {
                        if (proporzioniIvaSconto != null && d != null) {
                            scontoRipartito = CastUtils.toDouble0(proporzioniIvaSconto.get(d.getCodice()));
                        } else {
                            scontoRipartito = 0;
                        }
                    }
                    if (!Double.isNaN(scontoRipartito)) {
                        nuovoImponibile = d.getImponibile() - scontoRipartito;
                    } else {
                        nuovoImponibile = d.getImponibile();
                    }
                    double perc_sconto = nuovoImponibile * 100d / d.getImponibile();
                    if (perc_sconto == Double.NaN) {
                        perc_sconto = 0;
                    }
                    double nuovoImponibile_deducibile = d.getImponibile_deducibile() * (perc_sconto / 100d);
                    double nuovoImponibile_indeducibile = d.getImponibile_indeducibile() * (perc_sconto / 100d);
                    imposta = nuovoImponibile / 100d * d.getPercentuale();
                    double imposta_deducibile = nuovoImponibile_deducibile / 100d * d.getPercentuale();
                    double imposta_indeducibile = nuovoImponibile_indeducibile / 100d * d.getPercentuale();
                    imposta_noarr = imposta;
                    imposta = it.tnx.Util.round(imposta, 2);
                    d.imponibile_noarr = nuovoImponibile;
                    nuovoImponibile = it.tnx.Util.round(nuovoImponibile, 2);
                    d.setImponibile(nuovoImponibile);
                    d.setImponibile_deducibile(it.tnx.Util.round(nuovoImponibile_deducibile, 2));
                    d.setImponibile_indeducibile(it.tnx.Util.round(nuovoImponibile_indeducibile, 2));
                    d.setImposta(imposta);
                    d.setImposta_deducibile(it.tnx.Util.round(imposta_deducibile, 2));
                    d.setImposta_indeducibile(it.tnx.Util.round(imposta_indeducibile, 2));
                    d.imposta_noarr = imposta_noarr;
                } else {
                    if (passaggio == 1) {
                        scontoRipartito = sconto * d.getIvato() / totaleIvatoPositivo;
                        proporzioniIvaSconto.put(d.getCodice(), scontoRipartito);
                        if (totaleIvatoPositivo != 0) {
                            proporzioniIva.put(d.getCodice(), 100d * d.getIvato() / totaleIvatoPositivo);
                        } else {
                            proporzioniIva.put(d.getCodice(), 0d);
                        }
                    } else {
                        scontoRipartito = proporzioniIvaSconto.get(d.getCodice());
                    }
                    if (!Double.isNaN(scontoRipartito)) {
                        nuovoIvato = d.getIvato() - scontoRipartito;
                    } else {
                        nuovoIvato = d.getIvato();
                    }
                    double perc_sconto = nuovoIvato * 100d / d.getIvato();
                    double nuovoIvato_deducibile = d.getIvato_deducibile() * (perc_sconto / 100d);
                    double nuovoIvato_indeducibile = d.getIvato_indeducibile() * (perc_sconto / 100d);
                    imposta = nuovoIvato * d.getPercentuale() / (100d + d.getPercentuale());
                    double imposta_deducibile = nuovoIvato_deducibile * d.getPercentuale() / (100d + d.getPercentuale());
                    double imposta_indeducibile = nuovoIvato_indeducibile * d.getPercentuale() / (100d + d.getPercentuale());
                    imposta_noarr = imposta;
                    imposta = it.tnx.Util.round(imposta, 2);
                    d.ivato_noarr = nuovoIvato;
                    nuovoIvato = it.tnx.Util.round(nuovoIvato, 2);
                    d.setIvato(nuovoIvato);
                    d.setIvato_deducibile(it.tnx.Util.round(nuovoIvato_deducibile, 2));
                    d.setIvato_indeducibile(it.tnx.Util.round(nuovoIvato_indeducibile, 2));
                    d.setImposta(imposta);
                    d.setImposta_deducibile(it.tnx.Util.round(imposta_deducibile, 2));
                    d.setImposta_indeducibile(it.tnx.Util.round(imposta_indeducibile, 2));
                    d.setImponibile(nuovoIvato - imposta);
                    d.setImponibile_deducibile(nuovoIvato_deducibile - imposta_deducibile);
                    d.setImponibile_indeducibile(nuovoIvato_indeducibile - imposta_indeducibile);
                    d.imposta_noarr = imposta_noarr;
                }
            }

            //ciclo i dettagli per ricaloclare il nuovo toale imponibile e toale imposta
            totaleImponibile = 0;
            totaleImponibilePositivo = 0;
            totaleIvato = 0;
            totaleIvatoPositivo = 0;
            totaleImposta = 0;

            ivaiter = dettagliIvaMap.keySet().iterator();
            //for (int i = 0; i < dettagliIva.size(); i++) {
            while (ivaiter.hasNext()) {
                String ivakey = ivaiter.next();
                DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
                totaleImponibile += d.getImponibile();
                totaleImponibilePositivo += d.getImponibile();
                totaleIvato += d.getIvato();
                totaleIvatoPositivo += d.getIvato();
                totaleImposta += d.getImposta();
            }

            //todo controllare con prove
            totaleImponibile = it.tnx.Util.round(totaleImponibile, 2);
            totaleImponibilePositivo = it.tnx.Util.round(totaleImponibilePositivo, 2);
            totaleIvato = it.tnx.Util.round(totaleIvato, 2);
            totaleIvatoPositivo = it.tnx.Util.round(totaleIvatoPositivo, 2);
            totaleImposta = it.tnx.Util.round(totaleImposta, 2);

//            this.totaleImponibileParziale = totaleImponibileParziale;
//            this.totaleImponibile = totaleImponibile;
//            this.totaleImponibilePerRitenuta = totaleImponibilePerRitenuta;
//            this.totaleIvatoParziale = totaleIvatoParziale;
//            this.totaleIvato = totaleIvato;
//            this.totaleIvatoPerRitenuta = totaleIvatoPerRitenuta;
            this.totaleIva = totaleImposta;

            if (!isPrezziIvati()) {
                this.totale = totaleImponibile + totaleImposta;
            } else {
                this.totale = totaleIvato;
            }
            this.totaleSconti = totaleSconti;

            ritenuta_perc = 0;
            ritenuta_descrizione = "";
            totale_ritenuta = 0;
            totale_da_pagare = 0;
            totale_da_pagare_finale = 0;
            if (ritenuta > 0) {
                try {
//                    ResultSet r = Db.openResultSet("select * from tipi_ritenuta where id = " + ritenuta);
                    ResultSet r = DbUtils.tryOpenResultSet(Db.getConn(), "select * from tipi_ritenuta where id = " + ritenuta);
                    if (r.next()) {
                        ritenuta_perc = r.getDouble("percentuale");
                        ritenuta_descrizione = r.getString("descrizione");
//                        totale_ritenuta = it.tnx.Util.round(this.totaleImponibile / 100 * ritenuta_perc, 2);
                        totale_ritenuta = it.tnx.Util.round(this.totaleImponibilePerRitenuta / 100d * ritenuta_perc, 2);
                        totale_da_pagare = this.totale - totale_ritenuta;
                        if (main.fileIni.getValueBoolean("pluginRitenute", "sottrai_rivalsa", false)) {
                            totale_da_pagare = totale_da_pagare - totale_rivalsa;
                        }
                    } else {
                        System.err.println("problema nel calcolo totale, manca ritenuta (" + ritenuta + ") in tabella");
                        totale_da_pagare = this.totale - totale_ritenuta;
                        if (main.fileIni.getValueBoolean("pluginRitenute", "sottrai_rivalsa", false)) {
                            totale_da_pagare = totale_da_pagare - totale_rivalsa;
                        }
                    }
                    DbUtils.close(r);
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            } else {
                totale_da_pagare = this.totale;
                if (main.fileIni.getValueBoolean("pluginRitenute", "sottrai_rivalsa", false)) {
                    totale_da_pagare = totale_da_pagare - totale_rivalsa;
                }
            }

            //BOLLO
            if (passaggio > 1) {
                //bollo
                if (speseBolloSiNo) {
                    speseBollo = cu.d0(speseBollo);
                    String codiceIvaBollo = "15";
                    Iva ivaBollo = new Iva();
                    if (!ivaBollo.load(Db.INSTANCE, codiceIvaBollo)) {
                        ivaBollo.setCodice(codiceIvaBollo);
                        ivaBollo.setPercentuale(0d);
                        ivaBollo.setDescrizione("Escluso art. 15");
                        ivaBollo.setDescrizioneBreve("Escluso art. 15");
                    }
                    DettaglioIva divabollo = null;
                    if (dettagliIvaMap.containsKey(codiceIvaBollo)) {
                        divabollo = (DettaglioIva) dettagliIvaMap.get(codiceIvaBollo);
                    } else {
                        divabollo = new DettaglioIva();
                        divabollo.setCodice(codiceIvaBollo);
                        divabollo.setDescrizione(ivaBollo.getDescrizione());
                        divabollo.setDescrizioneBreve(ivaBollo.getDescrizioneBreve());
                        divabollo.setPercentuale(ivaBollo.getPercentuale());
                    }
                    double imposta = speseBollo / 100d * ivaBollo.getPercentuale();
                    imposta = it.tnx.Util.round(imposta, 2);
                    divabollo.setImponibile(cu.d0(divabollo.getImponibile()) + speseBollo);
                    divabollo.setImponibile_deducibile(cu.d0(divabollo.getImponibile_deducibile()) + speseBollo);
                    divabollo.setImposta(imposta);
                    divabollo.setImposta_deducibile(cu.d0(divabollo.getImposta_deducibile()) + imposta);
                    divabollo.setIvato(divabollo.getIvato() + speseBollo + imposta);
                    dettagliIvaMap.put(codiceIvaBollo, divabollo);

                    totaleImponibile += speseBollo;
                    totale += speseBollo + imposta;
                    totaleIvato += speseBollo + imposta;
                    totaleIva += imposta;
                    totaleImposta += imposta;
                    totale_da_pagare += speseBollo + imposta;
                    totale_da_pagare_finale += speseBollo + imposta;

                }
            }

            //ripasso dettagli iva per calcolare imponibili deducibilità per sottrazione lasciando invariata l'iva
            System.out.println("calcolo deds");
            ivaiter = dettagliIvaMap.keySet().iterator();
            //for (int i = 0; i < dettagliIva.size(); i++) {
            while (ivaiter.hasNext()) {
                String ivakey = ivaiter.next();
                DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
//                d.setImponibile_deducibile(it.tnx.Util.round(d.getImponibile() - d.getImponibile_indeducibile(), 2));
//                d.setImposta_deducibile(it.tnx.Util.round(d.getImposta() - d.getImposta_indeducibile(), 2));
                d.setImponibile_indeducibile(it.tnx.Util.round(d.getImponibile() - d.getImponibile_deducibile(), 2));
                d.setImposta_indeducibile(it.tnx.Util.round(d.getImposta() - d.getImposta_deducibile(), 2));
            }

            //acconto
            totale_da_pagare_finale = totale_da_pagare - acconto;

            //split payment, si toglier l'imposta
            if (split_payment) {
                totale_da_pagare_finale -= totaleImposta;
            }

            //calcolo peso
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public double getImpIvaNonDeducibile() {
        double ret = 0;
        Iterator<String> ivaiter = dettagliIvaMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            String ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
            ret += d.getImposta_indeducibile();
        }
        return ret;
    }

    public double getImpIvaNonDeducibileOld() throws SQLException, Exception {
//        ResultSet dettagli = Db.openResultSet(sqlPerDettagli);
        ResultSet dettagli = DbUtils.tryOpenResultSet(Db.getConn(), sqlPerDettagli);
        double impDeducibile = 0d;
        while (dettagli.next()) {
            String iva = CastUtils.toString(dettagli.getString("iva"));
            Double ivaPerc = 0d;
//            ResultSet dettaglioIva = Db.openResultSet("SELECT percentuale FROM codici_iva WHERE codice = '" + iva + "'");
            ResultSet dettaglioIva = DbUtils.tryOpenResultSet(Db.getConn(), "SELECT percentuale FROM codici_iva WHERE codice = '" + iva + "'");
            if (dettaglioIva.next()) {
                ivaPerc = dettaglioIva.getDouble("percentuale");
            }
            DbUtils.close(dettaglioIva);

            double imponibile = CastUtils.toDouble0(dettagli.getDouble("impo"));
            //applico li sconti di testata
            if (scontoTestata1 != 0) {
                imponibile = imponibile - (imponibile / 100d * scontoTestata1);
            }
            if (scontoTestata2 != 0) {
                imponibile = imponibile - (imponibile / 100d * scontoTestata2);
            }
            if (scontoTestata3 != 0) {
                imponibile = imponibile - (imponibile / 100d * scontoTestata3);
            }

            if (sconto > 0) {
                //importo_riga : totale_lordo = x : sconto_a_importo -> x = importo_riga * sconto_a_importo / totale_lordo
                double sconto_proporzionato = 0;
                if (!prezziIvati) {
                    sconto_proporzionato = imponibile * sconto / (totaleImponibilePreSconto);
                } else {
                    sconto_proporzionato = imponibile * sconto / (totaleIvatoPreSconto);
                }
                imponibile -= sconto_proporzionato;
            }

            Double imposta = CastUtils.toDouble0(imponibile) * ivaPerc / 100;
            Double percDedu = 100d;
            if (dettagli.getObject("iva_deducibile") != null) {
                percDedu = CastUtils.toDouble0(dettagli.getDouble("iva_deducibile"));
            }
            Double deducibile = imposta - (imposta * (percDedu / 100));
            impDeducibile += deducibile;
            System.out.println("dettagli: " + dettagli.getDouble("iva_deducibile"));
        }
        DbUtils.close(dettagli);
        return impDeducibile;
    }

    public double getImpNonDeducibile() throws SQLException, Exception {
        double ret = 0;
        Iterator<String> ivaiter = dettagliIvaMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            String ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
            ret += d.getImponibile_indeducibile();
        }
        return ret;
    }

    public double getImpNonDeducibileOld() throws SQLException, Exception {
//        ResultSet dettagli = Db.openResultSet(sqlPerDettagli);
        ResultSet dettagli = DbUtils.tryOpenResultSet(Db.getConn(), sqlPerDettagli);
        double impDeducibile = 0d;
        while (dettagli.next()) {
            Double imponibile = CastUtils.toDouble0(dettagli.getDouble("impo"));

            //applico li sconti di testata
            if (scontoTestata1 != 0) {
                imponibile = imponibile - (imponibile / 100d * scontoTestata1);
            }
            if (scontoTestata2 != 0) {
                imponibile = imponibile - (imponibile / 100d * scontoTestata2);
            }
            if (scontoTestata3 != 0) {
                imponibile = imponibile - (imponibile / 100d * scontoTestata3);
            }

            if (sconto > 0) {
                //importo_riga : totale_lordo = x : sconto_a_importo -> x = importo_riga * sconto_a_importo / totale_lordo
                double sconto_proporzionato = 0;
                if (!prezziIvati) {
                    sconto_proporzionato = imponibile * sconto / (totaleImponibilePreSconto);
                } else {
                    sconto_proporzionato = imponibile * sconto / (totaleIvatoPreSconto);
                }
                imponibile -= sconto_proporzionato;
            }

            Double percDedu = 100d;
            if (dettagli.getObject("iva_deducibile") != null) {
                percDedu = CastUtils.toDouble0(dettagli.getDouble("iva_deducibile"));
            }
            Double deducibile = imponibile - (imponibile * (percDedu / 100));
            impDeducibile += deducibile;
            System.out.println("dettagli: " + dettagli.getDouble("iva_deducibile"));
        }
        DbUtils.close(dettagli);
        return impDeducibile;
    }

    public double getImpIvaDeducibile(String codiceIva) {
        DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(codiceIva);
        if (d != null) {
            return d.getImponibile_deducibile();
        }
        return 0;
    }

    public double getImpIvaDeducibileOld(String codiceIva) {
        double impDeducibile = 0d;
        try {
//            ResultSet dettagli = Db.openResultSet(sqlPerDettagli);
            ResultSet dettagli = DbUtils.tryOpenResultSet(Db.getConn(), sqlPerDettagli);
            while (dettagli.next()) {
                if (CastUtils.toString(dettagli.getString("iva")).equals(codiceIva)) {
                    Double imponibile = CastUtils.toDouble0(dettagli.getDouble("impo"));
                    //applico li sconti di testata
                    if (scontoTestata1 != 0) {
                        imponibile = imponibile - (imponibile / 100d * scontoTestata1);
                    }
                    if (scontoTestata2 != 0) {
                        imponibile = imponibile - (imponibile / 100d * scontoTestata2);
                    }
                    if (scontoTestata3 != 0) {
                        imponibile = imponibile - (imponibile / 100d * scontoTestata3);
                    }

                    if (sconto > 0) {
                        //importo_riga : totale_lordo = x : sconto_a_importo -> x = importo_riga * sconto_a_importo / totale_lordo
                        double sconto_proporzionato = 0;
                        if (!prezziIvati) {
                            sconto_proporzionato = imponibile * sconto / (totaleImponibilePreSconto);
                        } else {
                            sconto_proporzionato = imponibile * sconto / (totaleIvatoPreSconto);
                        }
                        imponibile -= sconto_proporzionato;
                    }

                    Double percDedu = 100d;
                    if (dettagli.getObject("iva_deducibile") != null) {
                        percDedu = CastUtils.toDouble0(dettagli.getDouble("iva_deducibile"));
                    }
                    Double deducibile = imponibile * (percDedu / 100);
                    impDeducibile += deducibile;
                    System.out.println("dettagli: " + dettagli.getDouble("iva_deducibile"));
                }
            }
            DbUtils.close(dettagli);

            //aggiungo le spese per le proporzioni iva
            double altreSpese = this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso;
            if (getRivalsa_inps_perc() != null && getRivalsa_inps_perc() != 0) {
                if (main.fileIni.getValueBoolean("pluginRitenute", "includi_rivalsa_in_iva", true)) {
                    altreSpese += getTotale_rivalsa();
                }
            }
            for (Map.Entry<String, Double> entry : proporzioniIva.entrySet()) {
                System.out.println("entry: " + entry);
                if (entry.getKey().equals(codiceIva)) {
                    impDeducibile += (altreSpese / 100d * entry.getValue());
                }
            }

            return impDeducibile;
        } catch (Exception e) {
        }
        return impDeducibile;
    }

    public double getIvaDeducibile(String codiceIva) {
        DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(codiceIva);
        if (d != null) {
            return d.getImposta_deducibile();
        }
        return 0;
    }

    public double getIvaDeducibileOld(String codiceIva) {
        double impDeducibile = 0d;
        try {
//            ResultSet dettagli = Db.openResultSet(sqlPerDettagli);
            ResultSet dettagli = DbUtils.tryOpenResultSet(Db.getConn(), sqlPerDettagli);
            while (dettagli.next()) {
                String iva = CastUtils.toString(dettagli.getString("iva"));
                if (iva.equals(codiceIva)) {
                    Double ivaPerc = 0d;
//                    ResultSet dettaglioIva = Db.openResultSet("SELECT percentuale FROM codici_iva WHERE codice = '" + iva + "'");
                    ResultSet dettaglioIva = DbUtils.tryOpenResultSet(Db.getConn(), "SELECT percentuale FROM codici_iva WHERE codice = '" + iva + "'");
                    if (dettaglioIva.next()) {
                        ivaPerc = dettaglioIva.getDouble("percentuale");
                    }
                    DbUtils.close(dettaglioIva);

                    double imponibile = CastUtils.toDouble0(dettagli.getDouble("impo"));
                    //applico li sconti di testata
                    if (scontoTestata1 != 0) {
                        imponibile = imponibile - (imponibile / 100d * scontoTestata1);
                    }
                    if (scontoTestata2 != 0) {
                        imponibile = imponibile - (imponibile / 100d * scontoTestata2);
                    }
                    if (scontoTestata3 != 0) {
                        imponibile = imponibile - (imponibile / 100d * scontoTestata3);
                    }

                    if (sconto > 0) {
                        //importo_riga : totale_lordo = x : sconto_a_importo -> x = importo_riga * sconto_a_importo / totale_lordo
                        double sconto_proporzionato = 0;
                        if (!prezziIvati) {
                            sconto_proporzionato = imponibile * sconto / (totaleImponibilePreSconto);
                        } else {
                            sconto_proporzionato = imponibile * sconto / (totaleIvatoPreSconto);
                        }
                        imponibile -= sconto_proporzionato;
                    }

                    Double imposta = CastUtils.toDouble0(imponibile) * ivaPerc / 100d;
                    Double percDedu = 100d;
                    if (dettagli.getObject("iva_deducibile") != null) {
                        percDedu = CastUtils.toDouble0(dettagli.getDouble("iva_deducibile"));
                    }
                    Double deducibile = imposta * (percDedu / 100);
                    impDeducibile += deducibile;
                    System.out.println("dettagli: " + dettagli.getDouble("iva_deducibile"));
                }
            }
            DbUtils.close(dettagli);

            //aggiungo le spese per le proporzioni iva
            double altreSpese = this.speseTrasporto + this.speseVarieImponibili + this.speseIncasso;
            if (getRivalsa_inps_perc() != null && getRivalsa_inps_perc() != 0) {
                if (main.fileIni.getValueBoolean("pluginRitenute", "includi_rivalsa_in_iva", true)) {
                    altreSpese += getTotale_rivalsa();
                }
            }
            for (Map.Entry<String, Double> entry : proporzioniIva.entrySet()) {
                System.out.println("entry: " + entry);
                if (entry.getKey().equals(codiceIva)) {
                    Double ivaPerc = 0d;
//                    ResultSet dettaglioIva = Db.openResultSet("SELECT percentuale FROM codici_iva WHERE codice = '" + codiceIva + "'");
                    ResultSet dettaglioIva = DbUtils.tryOpenResultSet(Db.getConn(), "SELECT percentuale FROM codici_iva WHERE codice = '" + codiceIva + "'");
                    if (dettaglioIva.next()) {
                        ivaPerc = dettaglioIva.getDouble("percentuale");
                    }
                    DbUtils.close(dettaglioIva);
                    impDeducibile += (altreSpese * ivaPerc / 100d) / 100d * entry.getValue();
                }
            }

        } catch (Exception e) {
        }

        return impDeducibile;
    }

    public double getImpIva(String codiceIva) {
        DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(codiceIva);
        if (d != null) {
            return d.getImponibile();
        }
        return 0;
    }

    public double getIva(String codiceIva) {
        DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(codiceIva);
        if (d != null) {
            return d.getImposta();
        }
        return 0;
    }

    public void visualizzaCastellettoIva() {

        String temp = "Castelletto Iva\n";
        temp += "codice\tdescrizione\tpercentuale\timponibile\timposta\t\t\timponibile_noarr\timposta_noarr\n";

        Iterator<String> ivaiter = dettagliIvaMap.keySet().iterator();
        //for (int i = 0; i < dettagliIva.size(); i++) {
        while (ivaiter.hasNext()) {
            String ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
            temp += d.getCodice();
            temp += "\t" + d.getDescrizione();
            temp += "\t" + d.getPercentuale();
            temp += "\t" + d.getImponibile();
            temp += "\t" + d.getImposta();
            temp += "\t\t\t" + d.imponibile_noarr;
            temp += "\t" + d.imposta_noarr;
            temp += "\n";
        }

        temp += "totaleiva " + totaleIva;
        temp += "\t totaleimp " + totaleImponibile;
        temp += "\t totale    " + totale;
        temp += "\n";

        //javax.swing.JOptionPane.showMessageDialog(null, temp);
        System.out.println(temp);
    }

    public String dumpCastellettoIva() {

        String temp = "Castelletto Iva\n";
        temp += "codice\tdescrizione\tpercentuale\timponibile\timposta\t\t\timponibile_noarr\timposta_noarr\n";

        Iterator<String> ivaiter = dettagliIvaMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            String ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakey);
            temp += d.getCodice();
            temp += "\t" + d.getDescrizione();
            temp += "\t" + d.getPercentuale();
            temp += "\t" + d.getImponibile();
            temp += "\t" + d.getImposta();
            temp += "\t\t\t" + d.imponibile_noarr;
            temp += "\t" + d.imposta_noarr;
            temp += "\n";
        }

        temp += "totaleiva " + totaleIva;
        temp += "\t totaleimp " + totaleImponibile;
        temp += "\t totale    " + totale;
        temp += "\n";

        return temp;
    }

    /**
     * Getter for property cliente.
     *
     * @return Value of property cliente.
     *
     *
     *
     */
    public gestioneFatture.logic.clienti.Cliente getCliente() {

        return this.cliente;
    }

    /**
     * Setter for property cliente.
     *
     * @param cliente New value of property cliente.
     *
     *
     *
     */
    public void setCliente(gestioneFatture.logic.clienti.Cliente cliente) {
        this.cliente = cliente;
    }

    /**
     * Getter for property scontoTestata1.
     *
     * @return Value of property scontoTestata1.
     *
     *
     *
     */
    public double getScontoTestata1() {

        return this.scontoTestata1;
    }

    /**
     * Setter for property scontoTestata1.
     *
     * @param scontoTestata1 New value of property scontoTestata1.
     *
     *
     *
     */
    public void setScontoTestata1(double scontoTestata1) {
        this.scontoTestata1 = scontoTestata1;
    }

    /**
     * Getter for property scontoTestata2.
     *
     * @return Value of property scontoTestata2.
     *
     *
     *
     */
    public double getScontoTestata2() {

        return this.scontoTestata2;
    }

    public void setParametriArrotondamento(String arrotondamentoParametro, String arrotondamentoTipo) {
        this.parametroArrotondamento = Double.parseDouble(arrotondamentoParametro);
        this.perDifetto = arrotondamentoTipo.equals("Inf.");
    }

    /**
     * Setter for property scontoTestata2.
     *
     * @param scontoTestata2 New value of property scontoTestata2.
     *
     *
     *
     */
    public void setScontoTestata2(double scontoTestata2) {
        this.scontoTestata2 = scontoTestata2;
    }

    /**
     * Getter for property scontoTestata3.
     *
     * @return Value of property scontoTestata3.
     *
     *
     *
     */
    public double getScontoTestata3() {

        return this.scontoTestata3;
    }

    /**
     * Setter for property scontoTestata3.
     *
     * @param scontoTestata3 New value of property scontoTestata3.
     *
     *
     *
     */
    public void setScontoTestata3(double scontoTestata3) {
        this.scontoTestata3 = scontoTestata3;
    }

    /**
     * Getter for property totaleSconti.
     *
     * @return Value of property totaleSconti.
     *
     *
     *
     */
    public double getTotaleSconti() {

        return this.totaleSconti;
    }

    /**
     * Setter for property totaleSconti.
     *
     * @param totaleSconti New value of property totaleSconti.
     *
     *
     *
     */
    public void setTotaleSconti(double totaleSconti) {
        this.totaleSconti = totaleSconti;
    }

    /**
     * Getter for property totale.
     *
     * @return Value of property totale.
     *
     *
     *
     */
    public double getTotale() {

        return this.totale;
    }

    /**
     * Setter for property totale.
     *
     * @param totale New value of property totale.
     *
     *
     *
     */
    public void setTotale(double totale) {
        this.totale = totale;
    }

    /**
     * Getter for property totale.
     *
     * @return Value of property totale.
     *
     *
     *
     */
    public double getTotaleQuantita() {

        return this.totaleQuantita;
    }

    /**
     * Getter for property totaleIva.
     *
     * @return Value of property totaleIva.
     *
     *
     *
     */
    public double getTotaleIva() {

        return this.totaleIva;
    }

    /**
     * Setter for property totaleIva.
     *
     * @param totaleIva New value of property totaleIva.
     *
     *
     *
     */
    public void setTotaleIva(double totaleIva) {
        this.totaleIva = totaleIva;
    }

    /**
     * Getter for property totaleImponibile.
     *
     * @return Value of property totaleImponibile.
     *
     *
     *
     */
    public double getTotaleImponibile() {

        return this.totaleImponibile;
    }

    /**
     * Setter for property totaleImponibile.
     *
     * @param totaleImponibile New value of property totaleImponibile.
     *
     *
     *
     */
    public void setTotaleImponibile(double totaleImponibile) {
        this.totaleImponibile = totaleImponibile;
    }

    /**
     * Getter for property speseTrasporto.
     *
     * @return Value of property speseTrasporto.
     *
     *
     *
     */
    public double getSpeseTrasporto() {

        return this.speseTrasporto;
    }

    /**
     * Setter for property speseTrasporto.
     *
     * @param speseTrasporto New value of property speseTrasporto.
     *
     *
     *
     */
    public void setSpeseTrasporto(double speseTrasporto) {
        this.speseTrasporto = speseTrasporto;
    }

    /**
     * Getter for property speseVarieImponibili.
     *
     * @return Value of property speseVarieImponibili.
     *
     *
     *
     */
    public double getSpeseVarieImponibili() {

        return this.speseVarieImponibili;
    }

    /**
     * Setter for property speseVarieImponibili.
     *
     * @param speseVarieImponibili New value of property speseVarieImponibili.
     *
     *
     *
     */
    public void setSpeseVarieImponibili(double speseVarieImponibili) {
        this.speseVarieImponibili = speseVarieImponibili;
    }

    /**
     * Getter for property speseIncasso.
     *
     * @return Value of property speseIncasso.
     *
     *
     *
     */
    public double getSpeseIncasso() {

        return this.speseIncasso;
    }

    /**
     * Setter for property speseIncasso.
     *
     * @param speseIncasso New value of property speseIncasso.
     *
     *
     *
     */
    public void setSpeseIncasso(double speseIncasso) {
        this.speseIncasso = speseIncasso;
    }

    /**
     * Getter for property codiceCliente.
     *
     * @return Value of property codiceCliente.
     *
     *
     *
     */
    public long getCodiceCliente() {

        return this.codiceCliente;
    }

    /**
     * Setter for property codiceCliente.
     *
     * @param codiceCliente New value of property codiceCliente.
     *
     *
     *
     */
    public void setCodiceCliente(long codiceCliente) {
        this.codiceCliente = codiceCliente;
    }

    public void setCodiceFornitore(long codiceFornitore) {
        this.codiceFornitore = codiceFornitore;
    }

    public int getRitenuta() {
        return ritenuta;
    }

    public void setRitenuta(int ritenuta) {
        this.ritenuta = ritenuta;
    }

    public double getTotale_ritenuta() {
        return totale_ritenuta;
    }

    public void setTotale_ritenuta(double totale_ritenuta) {
        this.totale_ritenuta = totale_ritenuta;
    }

    public double getTotale_da_pagare() {
        return totale_da_pagare;
    }

    public void setTotale_da_pagare(double totale_da_pagare) {
        this.totale_da_pagare = totale_da_pagare;
    }

    public double getTotale_da_pagare_finale() {    //è al netto dell'acconto
        return totale_da_pagare_finale;
    }

    public void setTotale_da_pagare_finale(double totale_da_pagare_finale) {
        this.totale_da_pagare_finale = totale_da_pagare_finale;
    }

    public double getAcconto() {
        return acconto;
    }

    public void setAcconto(double acconto) {
        this.acconto = acconto;
    }

    public double getRitenuta_perc() {
        return ritenuta_perc;
    }

    public void setRitenuta_perc(double ritenuta_perc) {
        this.ritenuta_perc = ritenuta_perc;
    }

    public String getRitenuta_descrizione() {
        return ritenuta_descrizione;
    }

    public void setRitenuta_descrizione(String ritenuta_descrizione) {
        this.ritenuta_descrizione = ritenuta_descrizione;
    }

    public boolean isRivalsa_inps() {
        return rivalsa_inps;
    }

    public void setRivalsa_inps(boolean rivalsa_inps) {
        this.rivalsa_inps = rivalsa_inps;
    }

    public void setRivalsaCodice(Integer codRivalsa) {
        if (!codRivalsa.equals("")) {
            this.rivalsa_codice = codRivalsa;
        }
    }

    public double getTotale_rivalsa() {
        double massimale = 0d;
        if (rivalsa_codice != null && rivalsa_codice != -1) {
            try {
                massimale = CastUtils.toDouble0(DbUtils.getObject(Db.getConn(), "SELECT massimale FROM tipi_rivalsa WHERE id = " + Db.pc(rivalsa_codice, Types.INTEGER)));
            } catch (Exception ex) {
                massimale = 0d;
            }
            if (massimale != 0d && massimale < totale_rivalsa) {
                return massimale;
            }
        }

        return totale_rivalsa;
    }

    public void setTotale_rivalsa(double totale_rivalsa) {
        this.totale_rivalsa = totale_rivalsa;

        double massimale = 0d;
        if (rivalsa_codice != null && rivalsa_codice != -1) {
            try {
                massimale = CastUtils.toDouble0(DbUtils.getObject(Db.getConn(), "SELECT massimale FROM tipi_rivalsa WHERE id = " + Db.pc(rivalsa_codice, Types.INTEGER)));
            } catch (Exception ex) {
                massimale = 0d;
            }
            if (massimale != 0d && massimale < totale_rivalsa) {
                this.totale_rivalsa = massimale;
            }
        }
    }

    public double getTotale_imponibile2() {
        return totale_imponibile2;
    }

    public void setTotale_imponibile2(double totale_imponibile2) {
        this.totale_imponibile2 = totale_imponibile2;
    }

    public double getTotaleImponibileParziale() {
        return totaleImponibileParziale;
    }

    public void setTotaleImponibileParziale(double totaleImponibileParziale) {
        this.totaleImponibileParziale = totaleImponibileParziale;
    }

    public Double getRivalsa_inps_perc() {
        return rivalsa_inps_perc;
    }

    public void setRivalsa_inps_perc(Double rivalsa_inps_perc) {
        this.rivalsa_inps_perc = rivalsa_inps_perc;
    }

    public String getRivalsa_inps_descrizione() {
        return rivalsa_inps_descrizione;
    }

    public void setRivalsa_inps_descrizione(String rivalsa_inps_descrizione) {
        this.rivalsa_inps_descrizione = rivalsa_inps_descrizione;
    }

    public void setData(Date date) {
        this.data = date;
    }

    public boolean isPrezziIvati() {
        return prezziIvati;
    }

    public void setPrezziIvati(boolean prezziIvati) {
        this.prezziIvati = prezziIvati;
    }

    public double getTotaleIvatoParziale() {
        return totaleIvatoParziale;
    }

    public void setTotaleIvatoParziale(double totaleIvatoParziale) {
        this.totaleIvatoParziale = totaleIvatoParziale;
    }

    public double getTotaleIvato() {
        return totaleIvato;
    }

    public void setTotaleIvato(double totaleIvato) {
        this.totaleIvato = totaleIvato;
    }

    public double getSconto() {
        return sconto;
    }

    public void setSconto(double sconto) {
        this.sconto = sconto;
    }

    public double getTotaleImponibilePerRivalsa() {
        return totaleImponibilePerRivalsa;
    }

    public void setTotaleImponibilePerRivalsa(double totaleImponibilePerRivalsa) {
        this.totaleImponibilePerRivalsa = totaleImponibilePerRivalsa;
    }

    public static class IvaDed {
        String codice_iva;
        Double perc_deducibilita;
        
        public IvaDed(String codice_iva, Double perc_deducibilita) {
            if (perc_deducibilita == null) perc_deducibilita = 100d;
            this.perc_deducibilita = perc_deducibilita;
            this.codice_iva = codice_iva;
        }

        @Override
        public String toString() {
            return "IvaDed codice iva = " + cu.s(codice_iva) + " perc. dedducibilita = " + cu.s(perc_deducibilita);
        }
        
        
    }
}
