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
import it.tnx.commons.DebugFastUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.MicroBench;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.invoicex.InvoicexUtil;
import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

public class Documento2 {

    public Vector dettagliIva = new Vector();
    public Map<IvaDed, DettaglioIva> dettagliIvaDedMap = new LinkedHashMap();
//    public Map<ContoIvaDed, DettaglioIva> dettagliContoIvaDedMap = new LinkedHashMap();
    private Vector dettagliDocumento = new Vector();
    private Map<IvaDed, Double> proporzioniIva = new HashMap<IvaDed, Double>(); //percentuale iva, proporzione
    private Map<IvaDed, Double> proporzioniIvaSconto = new HashMap<IvaDed, Double>(); //percentuale iva, sconto
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

    private double totale;
    private double totaleIva;
    private double totaleImponibileParziale;
    private double totaleImponibilePerRivalsa;
    private double totaleImponibile;
    private double totaleImponibilePerRitenuta;
    private double totaleIvatoParziale;
    private double totaleIvato;
    private double totaleIvatoPerRitenuta;

//    private double speseTrasporto; //VENGONO INCLUSE NEL CALCOLO DELL'IVA RIPARTENDO SUGLI SCAGLIONI //NON VENGONO SCONTATE
//    private double speseVarieImponibili; //VENGONO INCLUSE NEL CALCOLO DELL'IVA RIPARTENDO SUGLI SCAGLIONI //NON VENGONO SCONTATE
//    private double speseIncasso; //VENGONO INCLUSE NEL CALCOLO DELL'IVA RIPARTENDO SUGLI SCAGLIONI //NON VENGONO SCONTATE
//    public Double speseBollo = null;
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

    Integer numero = null;
    String serie = null;
    Integer anno = null;
    Integer id = null;

    Map testa = null;
    List<Map> righe = null;
    public Map cliente = null;
    public Map<Object, Map> iva = null;

    public Documento2(Map testa, List<Map> righe) {
        this.testa = testa;
        this.righe = righe;
    }

    synchronized public void calcolaTotali() throws Exception {
        calcolaTotali(null);
        visualizzaCastellettoIva();
    }

    synchronized public void calcolaTotali(String iva_spese) throws Exception {
        Double scontoTestata1 = cu.d(testa.get("sconto1"));
        Double scontoTestata2 = cu.d(testa.get("sconto2"));
        Double scontoTestata3 = cu.d(testa.get("sconto3"));
        setAcconto(cu.d0(testa.get("acconto")));

        setTotale_rivalsa(0);

        calcolaTotaliSub(iva_spese, 1);

        if (getRivalsa_inps_perc() != null || ritenuta > 0) {
            totaleImponibile = 0;
            totaleImponibilePositivo = 0;
            totaleImponibilePerRitenuta = 0;
            totaleIvato = 0;
            totaleIvatoPositivo = 0;
            totaleIvatoPerRitenuta = 0;
            totaleImposta = 0;

            Iterator<IvaDed> ivaiter = dettagliIvaDedMap.keySet().iterator();
            while (ivaiter.hasNext()) {
                IvaDed ivakey = ivaiter.next();
                DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
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
                    for (Map r : righe) {
                        if (StringUtils.equalsIgnoreCase(cu.s(r.get("flag_rivalsa")), "N")) {
                            sommeNonSoggetteRivalsa += cu.d0(r.get("impo"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setTotaleImponibilePerRivalsa(totaleImponibile - sommeNonSoggetteRivalsa);
                setTotale_rivalsa(it.tnx.Util.round(getTotaleImponibilePerRivalsa() / 100d * getRivalsa_inps_perc(), 2));
                if (main.fileIni.getValueBoolean("pluginRitenute", "includi_rivalsa", true)) {
                    totaleImponibilePerRitenuta += getTotale_rivalsa();
                }
            }

            //togliere dal totale per ritenuta le righe con flag da non applicare la ritenuta
            try {
                for (Map r : righe) {
                    if (StringUtils.equalsIgnoreCase(cu.s(r.get("flag_ritenuta")), "N")) {
                        //senza iva
                        imponibile = cu.d0(r.get("impo")) - (cu.d0(r.get("impo")) / 100 * scontoTestata1);
                        imponibile = imponibile - (imponibile / 100 * scontoTestata2);
                        imponibile = imponibile - (imponibile / 100 * scontoTestata3);
                        imponibile = it.tnx.Util.round(imponibile, 2);
                        totaleImponibilePerRitenuta -= imponibile;

                        //ivato
                        ivato = cu.d0(r.get("ivato")) - (cu.d0(r.get("ivato")) / 100 * scontoTestata1);
                        ivato = ivato - (ivato / 100 * scontoTestata2);
                        ivato = ivato - (ivato / 100 * scontoTestata3);
                        ivato = it.tnx.Util.round(ivato, 2);
                        totaleIvatoPerRitenuta -= ivato;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //calcolo rivalsa spostato in funzione precedente ....
            totaleImponibileParziale = totaleImponibile;
            totaleIvatoParziale = totaleIvato;
        }

        //calcolare totali per ritenuta
        calcolaTotaliSub(iva_spese, 2);

        dettagliIva = InvoicexUtil.getIveVector(getDettagliIvaMap(dettagliIvaDedMap));
    }

    synchronized public void calcolaTotaliSub(String iva_spese, int passaggio) throws Exception {
        MicroBench mb = new MicroBench(true);

        Double scontoTestata1 = cu.d0(testa.get("sconto1"));
        Double scontoTestata2 = cu.d0(testa.get("sconto2"));
        Double scontoTestata3 = cu.d0(testa.get("sconto3"));

        Double speseTrasporto = cu.d0(testa.get("spese_trasporto"));
        Double speseIncasso = cu.d0(testa.get("spese_incasso"));
        Double speseVarieImponibili = 0d;
        
        setAcconto(cu.d0(testa.get("acconto")));

        //raggruppo per perc. deducibilita
        totaleQuantita = 0;
        totalePeso = 0;
        totaleSconti = 0;

        for (Map r : righe) {
            totaleQuantita += cu.d0(r.get("quantita"));
            if (!StringUtils.equalsIgnoreCase(cu.s(r.get("flag_ritenuta")), "N")) {
                totaleSconti += cu.d0(r.get("lordo")) - imponibile;
                totaleSconti += cu.d0(r.get("lordoIvato")) - ivato;
            }
            try {
                double pesoriga = cu.d0(r.get("quantita")) * cu.d0(r.get("peso"));
                totalePeso += pesoriga;
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }

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
                split_payment = cu.toBoolean(testa.get("split_payment"));
            } catch (Exception e) {
            }
        }

        mb.out("calcolaTotaliSub 1");

        if (iva == null) {
            iva = dbu.getListMapMap(it.tnx.Db.getConn(), "select * from codici_iva order by codice", "codice");
        }

        //preparo totali di riga
        //String sql1 = "UPDATE " + tabr + " set totale_imponibile = ROUND(CAST(  IF(arrotondamento_parametro = '0',  (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.',   FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2)),2)";        
        int i = 0;
        for (Map r : righe) {
            i++;
            if (iva.get(r.get("iva")) == null && (cu.d0(r.get("prezzo")) != 0 || cu.d0(r.get("prezzo_ivato")) != 0)) {
                DebugFastUtils.dump(iva);
                throw new Exception("Iva non trovata, codice '" + r.get("iva") + "' alla riga " + i);
            }
            if (iva.get(r.get("iva")) != null) {
                double qta = cu.d0(r.get("quantita"));
                double importo_senza_iva = cu.d0(r.get("prezzo"));
                double importo_con_iva = cu.d0(r.get("prezzo_ivato"));
                double sconto1 = cu.d0(r.get("sconto1"));
                double sconto2 = cu.d0(r.get("sconto2"));
                double tot_senza_iva = 0;
                double tot_con_iva = 0;
                double totale_imponibile = 0;
                double totale_ivato = 0;
                double iva_prezz = 100d;
                iva_prezz = 100d + cu.d0(iva.get(r.get("iva")).get("percentuale"));
                if (!isPrezziIvati()) {
                    tot_senza_iva = InvoicexUtil.getTotaleRigaSenzaIva(qta, importo_senza_iva, sconto1, sconto2);
                    totale_imponibile = tot_senza_iva;
                    tot_con_iva = FormatUtils.round((tot_senza_iva / 100d) * iva_prezz, 2);
                    totale_ivato = tot_con_iva;
                } else {
                    tot_con_iva = InvoicexUtil.getTotaleRigaConIva(qta, importo_con_iva, sconto1, sconto2);
                    totale_ivato = tot_con_iva;
                    tot_senza_iva = FormatUtils.round((tot_con_iva / iva_prezz) * 100d, 2);
                    totale_imponibile = tot_senza_iva;
                }
                r.put("totale_imponibile", totale_imponibile);
                r.put("totale_ivato", totale_ivato);
            }
        }

        mb.out("calcolaTotaliSub 2");

        List<Map> righe_iva = new ArrayList<Map>();
        //preparo totali di riga per iva
        i = 0;
        for (Map r : righe) {
            i++;
            Map riva = new HashMap();
            for (Object key : r.keySet()) {
                riva.put(key, r.get(key));
            }
            riva.put("totaImpo", r.get("totale_imponibile"));
            riva.put("totaIvato", r.get("totale_ivato"));
            if (iva.get(r.get("iva")) == null && (cu.d0(r.get("prezzo")) != 0 || cu.d0(r.get("prezzo_ivato")) != 0)) {
                DebugFastUtils.dump(iva);
                throw new Exception("Iva non trovata, codice '" + r.get("iva") + "' alla riga " + i);
            }
            if (iva.get(r.get("iva")) != null) {
                riva.put("percentuale", iva.get(r.get("iva")).get("percentuale"));
                riva.put("descrizione", iva.get(r.get("iva")).get("descrizione"));
                riva.put("descrizione_breve", iva.get(r.get("iva")).get("descrizione_breve"));
                riva.put("quantita", cu.d0(r.get("quantita")));
                riva.put("lordo", cu.d0(r.get("prezzo")) * cu.d0(r.get("quantita")));
                riva.put("lordoIvato", cu.d0(r.get("prezzo")) * cu.d0(r.get("quantita")) * (1 + (cu.d0(riva.get("percentuale")) / 100)));
                righe_iva.add(riva);
            }
        }

        dettagliIvaDedMap.clear();
//        dettagliContoIvaDedMap.clear();

        mb.out("calcolaTotaliSub 3");

        for (Map r : righe_iva) {
            if (main.fileIni.getValueBoolean("pref", "attivaArrotondamento", false)) {
                setParametriArrotondamento(cu.s(r.get("arrotondamento_parametro")), cu.s(r.get("arrotondamento_tipo")));
            }
            String kiva = cu.s(r.get("iva"));
            Double ded = cu.d(r.get("iva_deducibile"));
            if (ded == null) {
                ded = 100d;
            }
            IvaDed kivaded = new IvaDed(kiva, ded);

            if (StringUtils.isNotEmpty(kiva)) {
                //senza iva
                imponibile = cu.d0(r.get("totaImpo"));
                imponibile = InvoicexUtil.calcolaPrezzoArrotondato(imponibile, parametroArrotondamento, perDifetto);
                imponibile = imponibile - (imponibile / 100 * scontoTestata1);
                imponibile = imponibile - (imponibile / 100 * scontoTestata2);
                imponibile = imponibile - (imponibile / 100 * scontoTestata3);

                double perc_deducibilita = r.get("iva_deducibile") == null ? 100d : cu.d0(r.get("iva_deducibile"));
                double imponibile_deducibile = imponibile / 100d * perc_deducibilita;
                double imponibile_indeducibile = imponibile - imponibile_deducibile;

                totaleImponibile += imponibile;
                totaleImponibilePositivo += imponibile;
                totaleImponibile_deducibile += imponibile_deducibile;
                totaleImponibile_indeducibile += imponibile_indeducibile;

                //con iva
                ivato = cu.d0(r.get("totaIvato"));
                ivato = InvoicexUtil.calcolaPrezzoArrotondato(ivato, parametroArrotondamento, perDifetto);
                ivato = ivato - (ivato / 100 * scontoTestata1);
                ivato = ivato - (ivato / 100 * scontoTestata2);
                ivato = ivato - (ivato / 100 * scontoTestata3);
                double ivato_deducibile = ivato / 100d * perc_deducibilita;
                double ivato_indeducibile = ivato - ivato_deducibile;

                totaleIvato += ivato;
                totaleIvatoPositivo += ivato;
                totaleIvato_deducibile += ivato_deducibile;
                totaleIvato_indeducibile += ivato_indeducibile;

                //inserisco nei dettagli iva questo gruppo
                DettaglioIva diva = dettagliIvaDedMap.get(kivaded);
                if (diva == null) {
                    diva = new DettaglioIva();
                    diva.setCodice(kiva);
                    diva.setPerc_deducibilita(ded);
                    diva.setDescrizione(cu.s(r.get("descrizione")));
                    diva.setDescrizioneBreve(cu.s(r.get("descrizione_breve")));
                    diva.setPercentuale(cu.d0(r.get("percentuale")));
                }
                //diva.setCodice(resuTota.getString("iva").substring(0, resuTota.getString("iva").length() - 3));

                diva.setImponibile(cu.d0(diva.getImponibile()) + imponibile);
                diva.setImponibile_deducibile(cu.d0(diva.getImponibile_deducibile()) + imponibile_deducibile);
                diva.setImponibile_indeducibile(cu.d0(diva.getImponibile_indeducibile()) + imponibile_indeducibile);
                diva.imponibile_noarr = cu.d0(diva.getImponibile());

                diva.setIvato(cu.d0(diva.getIvato()) + ivato);
                diva.setIvato_deducibile(cu.d0(diva.getIvato_deducibile()) + ivato_deducibile);
                diva.setIvato_indeducibile(cu.d0(diva.getIvato_indeducibile()) + ivato_indeducibile);
                diva.ivato_noarr = cu.d0(diva.getIvato());

                dettagliIvaDedMap.put(kivaded, diva);
            }
        }

        mb.out("calcolaTotaliSub 4");

        //rileggo per arrotondare
        Iterator<IvaDed> ivaiter = dettagliIvaDedMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            IvaDed ivadedkey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivadedkey);
            d.setImponibile(it.tnx.Util.round(d.getImponibile(), 2));
            d.setIvato(it.tnx.Util.round(d.getIvato(), 2));
            d.setImponibile_deducibile(it.tnx.Util.round(d.getImponibile_deducibile(), 2));
            d.setImponibile_indeducibile(it.tnx.Util.round(d.getImponibile_indeducibile(), 2));
            d.setIvato_deducibile(it.tnx.Util.round(d.getIvato_deducibile(), 2));
            d.setIvato_indeducibile(it.tnx.Util.round(d.getIvato_indeducibile(), 2));
        }

        //controllo che se e' stato inserito il codice iva spese nei parametri allora non ripartiziono ma applico quella indicata
        String codiceIvaSpese = "";
        if (iva_spese == null) {
            codiceIvaSpese = InvoicexUtil.getIvaSpesePerData(data);
        } else {
            codiceIvaSpese = iva_spese;
        }

        gestioneFatture.logic.Iva ivaSpese = new gestioneFatture.logic.Iva();

        if (ivaSpese.load(Db.INSTANCE, codiceIvaSpese)) {

            //calcolo con codice iva spese fissato
            boolean codiceIvaSpeseTrovato = false;
            speseImponibili = speseTrasporto + speseVarieImponibili + speseIncasso;
            speseIvate = speseTrasporto + speseVarieImponibili + speseIncasso;
            proporzioniIva.put(new IvaDed(ivaSpese.getCodice(), 100d), 100d);
            if (getRivalsa_inps_perc() != null && getRivalsa_inps_perc() != 0) {
                if (main.fileIni.getValueBoolean("pluginRitenute", "includi_rivalsa_in_iva", true)) {
                    speseImponibili += getTotale_rivalsa();
                    speseIvate += getTotale_rivalsa();
                }
            }

            ivaiter = dettagliIvaDedMap.keySet().iterator();
            //for (int i = 0; i < dettagliIva.size(); i++) {
            while (ivaiter.hasNext()) {
                IvaDed ivakey = ivaiter.next();
                DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
                if (d.getCodice().equalsIgnoreCase(codiceIvaSpese) && d.getPerc_deducibilita() == 100d) {
                    codiceIvaSpeseTrovato = true;
                    d.setImponibile(d.getImponibile() + speseTrasporto + speseVarieImponibili + speseIncasso);
                    d.setImponibile(it.tnx.Util.round(d.getImponibile(), 2));
                    //metto spese varie sempre deducibili al 100% (TODO: parametrizzare nel documento con percentuale deducibilita per trasporto e icnasso ?)
                    d.setImponibile_deducibile(d.getImponibile_deducibile() + (speseTrasporto + speseVarieImponibili + speseIncasso));
                    d.setIvato(d.getIvato() + speseTrasporto + speseVarieImponibili + speseIncasso);
                    d.setIvato(it.tnx.Util.round(d.getIvato(), 2));
                    d.setIvato_deducibile(d.getIvato_deducibile() + speseTrasporto + speseVarieImponibili + speseIncasso);
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
                } else {
                    imposta = d.getIvato() * d.getPercentuale() / (100d + d.getPercentuale());
                    imposta_noarr = imposta;
                    imposta = it.tnx.Util.round(imposta, 2);
                }
                imposta_deducibile = it.tnx.Util.round(imposta / 100d * d.getPerc_deducibilita(), 2);
                imposta_indeducibile = it.tnx.Util.round(imposta - imposta_deducibile, 2);

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
                ds.setImponibile(speseTrasporto + speseVarieImponibili + speseIncasso);
                //metto spese varie sempre deducibili al 100% (TODO: parametrizzare nel documento con percentuale deducibilita per trasporto e icnasso ?)
                ds.setImponibile_deducibile(speseTrasporto + speseVarieImponibili + speseIncasso);
                ds.setIvato(speseTrasporto + speseVarieImponibili + speseIncasso);
                ds.setIvato_deducibile(speseTrasporto + speseVarieImponibili + speseIncasso);
                if (getRivalsa_inps_perc() != null && getRivalsa_inps_perc() != 0) {
                    if (main.fileIni.getValueBoolean("pluginRitenute", "includi_rivalsa_in_iva", true)) {
                        ds.setImponibile(ds.getImponibile() + getTotale_rivalsa());
                        ds.setImponibile(it.tnx.Util.round(ds.getImponibile(), 2));
                        ds.setImponibile_deducibile(ds.getImponibile_deducibile() + getTotale_rivalsa());

                        ds.setIvato(ds.getIvato() + (getTotale_rivalsa() + it.tnx.Util.round(getTotale_rivalsa() / 100d * ds.getPercentuale(), 2)));
                        ds.setIvato(it.tnx.Util.round(ds.getIvato(), 2));
                        ds.setIvato_deducibile(ds.getIvato_deducibile() + (getTotale_rivalsa() + it.tnx.Util.round(getTotale_rivalsa() / 100d * ds.getPercentuale(), 2)));
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
                    ds.setPerc_deducibilita(100d);
                    dettagliIvaDedMap.put(new IvaDed(codiceIvaSpese, 100d), ds);
                }
            }

            //ciclo i dettagli per ricaloclare il nuovo toale imponibile e toale imposta
            totaleImponibile = 0;
            totaleImponibilePositivo = 0;
            totaleIvato = 0;
            totaleIvatoPositivo = 0;
            totaleImposta = 0;

            ivaiter = dettagliIvaDedMap.keySet().iterator();
            //for (int i = 0; i < dettagliIva.size(); i++) {
            while (ivaiter.hasNext()) {
                IvaDed ivakey = ivaiter.next();
                DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
                totaleImponibile += d.getImponibile();
                totaleImponibilePositivo += d.getImponibile();
                totaleIvato += d.getIvato();
                totaleIvatoPositivo += d.getIvato();
                totaleImposta += d.getImposta();
            }
        } else {
            //ciclo per calcolare la proporzione degli imponibili iva e ripartizionarie le spese imponibili
            speseImponibili = speseTrasporto + speseVarieImponibili + speseIncasso;
            speseIvate = speseTrasporto + speseVarieImponibili + speseIncasso;
            if (getRivalsa_inps_perc() != null && getRivalsa_inps_perc() != 0) {
                if (main.fileIni.getValueBoolean("pluginRitenute", "includi_rivalsa_in_iva", true)) {
                    //aggiungo la rivalsa sempre sul codice 20/21, se non trovo aggiungo a totale da riproporzionare
                    boolean trovata_iva_std = false;
                    double iva_std = 22d;
                    if (data != null && data.before(DateUtils.getOnlyDate(2011, 9, 17))) {
                        iva_std = 20d;
                    } else if (data != null && data.before(DateUtils.getOnlyDate(2013, 10, 1))) {
                        iva_std = 21d;
                    }
                    ivaiter = dettagliIvaDedMap.keySet().iterator();
                    while (ivaiter.hasNext()) {
                        IvaDed ivakey = ivaiter.next();
                        DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
                        if (d.getPercentuale() == iva_std) {
                            trovata_iva_std = true;
                            break;
                        }
                    }
                    if (trovata_iva_std) {
                        ivaiter = dettagliIvaDedMap.keySet().iterator();
                        while (ivaiter.hasNext()) {
                            IvaDed ivakey = ivaiter.next();
                            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
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
                    } else if (!isPrezziIvati()) {
                        speseImponibili += getTotale_rivalsa();
                    } else {
                        speseIvate += getTotale_rivalsa();
                    }
                }
            }

            //ciclo i dettagli per ricaloclare il nuovo toale imponibile e toale imposta
            totaleImponibile = 0;
            totaleImponibilePositivo = 0;
            totaleIvato = 0;
            totaleIvatoPositivo = 0;
            totaleImposta = 0;

            ivaiter = dettagliIvaDedMap.keySet().iterator();
            //for (int i = 0; i < dettagliIva.size(); i++) {
            while (ivaiter.hasNext()) {
                IvaDed ivakey = ivaiter.next();
                DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
                totaleImponibile += d.getImponibile();
                totaleImponibilePositivo += d.getImponibile();
                totaleIvato += d.getIvato();
                totaleIvatoPositivo += d.getIvato();
                totaleImposta += d.getImposta();
            }

            Map<String, DettaglioIva> dettagliIvaMap = getDettagliIvaMap(dettagliIvaDedMap);
            Iterator<String> ivaitercodice = getDettagliIvaMap(dettagliIvaDedMap).keySet().iterator();
            while (ivaitercodice.hasNext()) {
                String ivakeycodice = ivaitercodice.next();
                DettaglioIva d = (DettaglioIva) dettagliIvaMap.get(ivakeycodice);
                double importoSpeseRipartito = 0;
                double nuovoImponibile = 0;
                double nuovoIvato = 0;

                if (!isPrezziIvati()) {
                    importoSpeseRipartito = speseImponibili * d.getImponibile() / totaleImponibilePositivo;

                    //vado ad aggiungerlo allo stesso codice iva ma con deducibilità 100%
                    DettaglioIva d2 = getDetIva(ivakeycodice, 100d);
                    if (d2 == null) {
                        d2 = new DettaglioIva();
                        d2.setCodice(ivakeycodice);
                        d2.setDescrizione(d.getDescrizione());
                        d2.setDescrizioneBreve(d.getDescrizioneBreve());
                        d2.setPercentuale(d.getPercentuale());
                        d2.setPerc_deducibilita(100d);
                    }

                    if (!Double.isNaN(importoSpeseRipartito)) {
                        nuovoImponibile = cu.d0(d2.getImponibile()) + importoSpeseRipartito;
                    } else {
                        nuovoImponibile = cu.d0(d2.getImponibile());
                    }
                    imposta = nuovoImponibile / 100d * d.getPercentuale();
                    imposta_noarr = imposta;
                    imposta = it.tnx.Util.round(imposta, 2);
                    d2.imponibile_noarr = nuovoImponibile;
                    nuovoImponibile = it.tnx.Util.round(nuovoImponibile, 2);
                    d2.setImponibile(nuovoImponibile);
                    d2.setImposta(imposta);
                    d2.imposta_noarr = imposta_noarr;
                    dettagliIvaDedMap.put(new IvaDed(ivakeycodice, 100d), d2);
                } else {
                    importoSpeseRipartito = speseIvate * d.getIvato() / totaleIvatoPositivo;

                    //vado ad aggiungerlo allo stesso codice iva ma con deducibilità 100%
                    DettaglioIva d2 = getDetIva(ivakeycodice, 100d);
                    if (d2 == null) {
                        d2 = new DettaglioIva();
                        d2.setCodice(ivakeycodice);
                        d2.setDescrizione(d.getDescrizione());
                        d2.setDescrizioneBreve(d.getDescrizioneBreve());
                        d2.setPercentuale(d.getPercentuale());
                        d2.setPerc_deducibilita(100d);
                    }

                    if (!Double.isNaN(importoSpeseRipartito)) {
                        nuovoIvato = d2.getIvato() + importoSpeseRipartito;
                    } else {
                        nuovoIvato = d2.getIvato();
                    }
                    imposta = nuovoIvato * d.getPercentuale() / (100d + d.getPercentuale());
                    imposta_noarr = imposta;
                    imposta = it.tnx.Util.round(imposta, 2);
                    d2.ivato_noarr = nuovoIvato;
                    nuovoIvato = it.tnx.Util.round(nuovoIvato, 2);
                    d2.setIvato(nuovoIvato);
                    d2.setImposta(imposta);
                    d2.setImponibile(nuovoIvato - imposta);
                    d2.imposta_noarr = imposta_noarr;
                    dettagliIvaDedMap.put(new IvaDed(ivakeycodice, 100d), d2);
                }
            }

            //ciclo i dettagli per ricaloclare il nuovo toale imponibile e toale imposta
            totaleImponibile = 0;
            totaleImponibilePositivo = 0;
            totaleIvato = 0;
            totaleIvatoPositivo = 0;
            totaleImposta = 0;

            ivaiter = dettagliIvaDedMap.keySet().iterator();
            //for (int i = 0; i < dettagliIva.size(); i++) {
            while (ivaiter.hasNext()) {
                IvaDed ivakey = ivaiter.next();
                DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
                totaleImponibile += d.getImponibile();
                totaleImponibilePositivo += d.getImponibile();
                totaleIvato += d.getIvato();
                totaleIvatoPositivo += d.getIvato();
                totaleImposta += d.getImposta();
            }
        }

        mb.out("calcolaTotaliSub 5");

        //ripartizione sconto a importo e ricalcolo deducibile indeducibile
        totaleImponibilePreSconto = totaleImponibile;
        totaleIvatoPreSconto = totaleIvato;
        ivaiter = dettagliIvaDedMap.keySet().iterator();
        //for (int i = 0; i < dettagliIva.size(); i++) {
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
            double scontoRipartito = 0;
            double nuovoImponibile = 0;
            double nuovoIvato = 0;
            if (!isPrezziIvati()) {
                if (passaggio == 1) {
                    scontoRipartito = sconto * d.getImponibile() / totaleImponibilePositivo;
                    proporzioniIvaSconto.put(ivakey, scontoRipartito);
                    if (totaleImponibilePositivo != 0) {
                        proporzioniIva.put(ivakey, 100d * d.getImponibile() / totaleImponibilePositivo);
                    } else {
                        proporzioniIva.put(ivakey, 0d);
                    }
                } else if (proporzioniIvaSconto != null && d != null) {
                    scontoRipartito = CastUtils.toDouble0(proporzioniIvaSconto.get(ivakey));
                } else {
                    scontoRipartito = 0;
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
                imposta = nuovoImponibile / 100d * d.getPercentuale();
                imposta_noarr = imposta;
                imposta = it.tnx.Util.round(imposta, 2);
                d.imponibile_noarr = nuovoImponibile;
                nuovoImponibile = it.tnx.Util.round(nuovoImponibile, 2);

                double imposta_deducibile = it.tnx.Util.round(imposta / 100d * d.getPerc_deducibilita(), 2);
                double imposta_indeducibile = it.tnx.Util.round(imposta - imposta_deducibile, 2);
                double nuovoImponibile_deducibile = it.tnx.Util.round(nuovoImponibile / 100d * d.getPerc_deducibilita(), 2);
                double nuovoImponibile_indeducibile = it.tnx.Util.round(nuovoImponibile + imposta - nuovoImponibile_deducibile - imposta_deducibile - imposta_indeducibile, 2);

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
                    proporzioniIvaSconto.put(ivakey, scontoRipartito);
                    if (totaleIvatoPositivo != 0) {
                        proporzioniIva.put(ivakey, 100d * d.getIvato() / totaleIvatoPositivo);
                    } else {
                        proporzioniIva.put(ivakey, 0d);
                    }
                } else if (proporzioniIvaSconto != null && d != null) {
                    scontoRipartito = CastUtils.toDouble0(proporzioniIvaSconto.get(ivakey));
                } else {
                    scontoRipartito = 0;
                }
                if (!Double.isNaN(scontoRipartito)) {
                    nuovoIvato = d.getIvato() - scontoRipartito;
                } else {
                    nuovoIvato = d.getIvato();
                }
                double perc_sconto = nuovoIvato * 100d / d.getIvato();
                imposta = nuovoIvato * d.getPercentuale() / (100d + d.getPercentuale());
                imposta_noarr = imposta;
                imposta = it.tnx.Util.round(imposta, 2);
                d.ivato_noarr = nuovoIvato;
                nuovoIvato = it.tnx.Util.round(nuovoIvato, 2);

                d.setImponibile(nuovoIvato - imposta);

                nuovoImponibile = nuovoIvato - imposta;
                double imposta_deducibile = it.tnx.Util.round(imposta / 100d * d.getPerc_deducibilita(), 2);
                double imposta_indeducibile = it.tnx.Util.round(imposta - imposta_deducibile, 2);
                double nuovoIvato_deducibile = it.tnx.Util.round(nuovoIvato / 100d * d.getPerc_deducibilita(), 2);
                double nuovoIvato_indeducibile = it.tnx.Util.round(nuovoIvato - nuovoIvato_deducibile, 2);
                double nuovoImponibile_deducibile = it.tnx.Util.round(nuovoImponibile / 100d * d.getPerc_deducibilita(), 2);
                double nuovoImponibile_indeducibile = it.tnx.Util.round(nuovoImponibile + imposta - nuovoImponibile_deducibile - imposta_deducibile - imposta_indeducibile, 2);

                d.setIvato(nuovoIvato);
                d.setImposta(imposta);
                d.setImposta_deducibile(it.tnx.Util.round(imposta_deducibile, 2));
                d.setImposta_indeducibile(it.tnx.Util.round(imposta_indeducibile, 2));

                d.setImponibile_deducibile(nuovoImponibile_deducibile);
                d.setImponibile_indeducibile(nuovoImponibile_indeducibile);

                d.setIvato_deducibile(nuovoIvato_deducibile);
                d.setIvato_indeducibile(nuovoIvato_indeducibile);

                d.imposta_noarr = imposta_noarr;
            }
        }

        mb.out("calcolaTotaliSub 6");

        //ciclo i dettagli per ricaloclare il nuovo toale imponibile e toale imposta
        totaleImponibile = 0;
        totaleImponibilePositivo = 0;
        totaleIvato = 0;
        totaleIvatoPositivo = 0;
        totaleImposta = 0;

        ivaiter = dettagliIvaDedMap.keySet().iterator();
        //for (int i = 0; i < dettagliIva.size(); i++) {
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
            totaleImponibile += d.getImponibile();
            totaleImponibilePositivo += d.getImponibile();
            totaleIvato += d.getIvato();
            totaleIvatoPositivo += d.getIvato();
            totaleImposta += d.getImposta();
        }

        totaleImponibile = it.tnx.Util.round(totaleImponibile, 2);
        totaleImponibilePositivo = it.tnx.Util.round(totaleImponibilePositivo, 2);
        totaleIvato = it.tnx.Util.round(totaleIvato, 2);
        totaleIvatoPositivo = it.tnx.Util.round(totaleIvatoPositivo, 2);
        totaleImposta = it.tnx.Util.round(totaleImposta, 2);

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
                ResultSet r = DbUtils.tryOpenResultSet(Db.getConn(), "select * from tipi_ritenuta where id = " + ritenuta);
                if (r.next()) {
                    ritenuta_perc = r.getDouble("percentuale");
                    ritenuta_descrizione = r.getString("descrizione");
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

        mb.out("calcolaTotaliSub 7");

        //BOLLO
        Double speseBollo = cu.d(testa.get("marca_da_bollo"));
        if (passaggio > 1) {
            //bollo
            if (speseBolloSiNo) {
                speseBollo = cu.d0(speseBollo);
                IvaDed codiceIvaBollo = new IvaDed("15", 100d);
                Iva ivaBollo = new Iva();
                if (!ivaBollo.load(Db.INSTANCE, codiceIvaBollo.codice_iva)) {
                    ivaBollo.setCodice(codiceIvaBollo.codice_iva);
                    ivaBollo.setPercentuale(0d);
                    ivaBollo.setDescrizione("Escluso art. 15");
                    ivaBollo.setDescrizioneBreve("Escluso art. 15");
                }
                DettaglioIva divabollo = null;
                if (dettagliIvaDedMap.containsKey(codiceIvaBollo)) {
                    divabollo = (DettaglioIva) dettagliIvaDedMap.get(codiceIvaBollo);
                } else {
                    divabollo = new DettaglioIva();
                    divabollo.setCodice(codiceIvaBollo.codice_iva);
                    divabollo.setPerc_deducibilita(100d);
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
                dettagliIvaDedMap.put(codiceIvaBollo, divabollo);

                totaleImponibile += speseBollo;
                totale += speseBollo + imposta;
                totaleIvato += speseBollo + imposta;
                totaleIva += imposta;
                totaleImposta += imposta;
                totale_da_pagare += speseBollo + imposta;
                totale_da_pagare_finale += speseBollo + imposta;

            }
        }

        mb.out("calcolaTotaliSub 8");

        //ripasso dettagli iva per calcolare imponibili deducibilità per sottrazione lasciando invariata l'iva
        System.out.println("calcolo deds");
        ivaiter = dettagliIvaDedMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
            d.setImponibile_deducibile(it.tnx.Util.round(d.getImponibile() - d.getImponibile_indeducibile(), 2));
            d.setImposta_deducibile(it.tnx.Util.round(d.getImposta() - d.getImposta_indeducibile(), 2));
//                d.setImponibile_indeducibile(it.tnx.Util.round(d.getImponibile() - d.getImponibile_deducibile(), 2));
//                d.setImposta_indeducibile(it.tnx.Util.round(d.getImposta() - d.getImposta_deducibile(), 2));
        }

        mb.out("calcolaTotaliSub 9");

        //acconto
        totale_da_pagare_finale = totale_da_pagare - acconto;

        //split payment, si toglier l'imposta
        if (split_payment) {
            totale_da_pagare_finale -= totaleImposta;
        }
    }

    public double getImpIvaNonDeducibile() {
        double ret = 0;
        Iterator<IvaDed> ivaiter = dettagliIvaDedMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
            ret += d.getImposta_indeducibile();
        }
        return ret;
    }

    public double getImpNonDeducibile() throws SQLException, Exception {
        double ret = 0;
        Iterator<IvaDed> ivaiter = dettagliIvaDedMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
            ret += d.getImponibile_indeducibile();
        }
        return ret;
    }

    public double getImpIvaDeducibile(String codiceIva) {
        double ret = 0;
        Iterator<IvaDed> ivaiter = dettagliIvaDedMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
            if (d.getCodice().equalsIgnoreCase(codiceIva)) {
                ret += d.getImponibile_deducibile();
            }
        }
        return ret;
    }

    public double getIvaDeducibile(String codiceIva) {
        double ret = 0;
        Iterator<IvaDed> ivaiter = dettagliIvaDedMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
            if (d.getCodice().equalsIgnoreCase(codiceIva)) {
                ret += d.getImposta_deducibile();
            }
        }
        return ret;
    }

    public double getImpIva(String codiceIva) {
        double ret = 0;
        Iterator<IvaDed> ivaiter = dettagliIvaDedMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
            if (d.getCodice().equalsIgnoreCase(codiceIva)) {
                ret += d.getImponibile();
            }
        }
        return ret;
    }

    public double getIva(String codiceIva) {
        double ret = 0;
        Iterator<IvaDed> ivaiter = dettagliIvaDedMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
            if (d.getCodice().equalsIgnoreCase(codiceIva)) {
                ret += d.getImposta();
            }
        }
        return ret;
    }

    public void visualizzaCastellettoIva() {

        String temp = "Castelletto Iva\n";
        temp += "codice\t percentuale\t imponibile\t imposta\t\t\t imponibile_noarr\t imposta_noarr\n";

        Iterator<IvaDed> ivaiter = dettagliIvaDedMap.keySet().iterator();
        //for (int i = 0; i < dettagliIva.size(); i++) {
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
            temp += d.getCodice();
            temp += "\t" + d.getPercentuale() + " (ded.:" + d.getPerc_deducibilita() + ")";
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

        Iterator<IvaDed> ivaiter = dettagliIvaDedMap.keySet().iterator();
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            DettaglioIva d = (DettaglioIva) dettagliIvaDedMap.get(ivakey);
            temp += d.getCodice();
            temp += "\t" + d.getDescrizione();
            temp += "\t" + d.getPercentuale() + " (ded.:" + d.getPerc_deducibilita() + ")";
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

    private Map<String, DettaglioIva> getDettagliIvaMap(Map<IvaDed, DettaglioIva> dettagliIvaDedMap) {
        //ritorna elenco dettagli iva raggruppati solo per codice iva
        //ad esempio per avere totale imponibile/ivato per codice iva per ripartire le spese trasp
        Map<String, DettaglioIva> dettagliIvaMap = new LinkedHashMap();

        for (IvaDed key : dettagliIvaDedMap.keySet()) {
            DettaglioIva dsorg = dettagliIvaDedMap.get(key);
            if (!dettagliIvaMap.containsKey(key.codice_iva.toUpperCase())) {
                dettagliIvaMap.put(key.codice_iva.toUpperCase(), new DettaglioIva());
            }
            DettaglioIva ddest = dettagliIvaMap.get(key.codice_iva.toUpperCase());
            ddest.setImponibile(cu.d0(ddest.getImponibile()) + cu.d0(dsorg.getImponibile()));
            ddest.setIvato(cu.d0(ddest.getIvato()) + cu.d0(dsorg.getIvato()));
            ddest.setImposta(cu.d0(ddest.getImposta()) + cu.d0(dsorg.getImposta()));
            ddest.setCodice(dsorg.getCodice());
            ddest.setDescrizione(dsorg.getDescrizione());
            ddest.setDescrizioneBreve(dsorg.getDescrizioneBreve());
            ddest.setPercentuale(dsorg.getPercentuale());
        }

        return dettagliIvaMap;
    }

    private DettaglioIva getDetIva(String codice_iva, double perc_ded) {
        for (IvaDed key : dettagliIvaDedMap.keySet()) {
            DettaglioIva dsorg = dettagliIvaDedMap.get(key);
            if (dsorg.getCodice().equalsIgnoreCase(codice_iva) && dsorg.getPerc_deducibilita() == perc_ded) {
                return dsorg;
            }
        }
        return null;
    }

    public void setParametriArrotondamento(String arrotondamentoParametro, String arrotondamentoTipo) {
        this.parametroArrotondamento = cu.d0(arrotondamentoParametro);
        this.perDifetto = arrotondamentoTipo.equals("Inf.");
    }

}
