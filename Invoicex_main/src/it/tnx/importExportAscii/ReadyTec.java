/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.importExportAscii;

import gestioneFatture.Db;
import gestioneFatture.Util;
import gestioneFatture.dbFattura;
import gestioneFatture.main;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.gui.JDialogExportAcquistiVendite;
import java.awt.Cursor;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Alessio
 */
public class ReadyTec extends Thread {

//    private ResultSet rs;
    private List<Map> rs;
    private ArrayList<ArrayList> listaRighe;
    JDialogExportAcquistiVendite frm;
    private boolean erroreCliente = false;
    private boolean erroreIva = false;
    private String clientiConErrori = "";
    private String codiciIvaNonCollegati = "";
    private String codiceIvaDefault = null;
    private String codiceContoDefault = null;
    private String codiceContoDefaultOrig = null;
    private String esigDiffIva = "0";
    public String nomeFile = null;

    public ReadyTec(List<Map> rs, JDialogExportAcquistiVendite frm) {
        this.rs = rs;
        this.frm = frm;

        try {
            Map dati_azienda = dbu.getListMap(Db.getConn(), "select export_fatture_estrai_scadenze"
                    + ", export_fatture_estrai_acconti"
                    + ", export_fatture_codice_iva"
                    + ", export_fatture_conto_ricavi"
                    + ", export_fatture_esig_diff_iva"
                    + " from dati_azienda").get(0);

            boolean estrai_scadenze = cu.toBoolean(dati_azienda.get("export_fatture_estrai_scadenze"));
            boolean estrai_acconti = cu.toBoolean(dati_azienda.get("export_fatture_estrai_acconti"));
            codiceIvaDefault = cu.toString(dati_azienda.get("export_fatture_codice_iva"));
            codiceContoDefault = cu.toString(dati_azienda.get("export_fatture_conto_ricavi"));
            codiceContoDefaultOrig = codiceContoDefault;
            esigDiffIva = cu.s(dati_azienda.get("export_fatture_esig_diff_iva"));

            listaRighe = new ArrayList<ArrayList>();

            //genero totali documenti
            InvoicexUtil.generaTotaliDocumentoDate(frm.dal.getDate(), frm.al.getDate(), null);

//            List<Map> iveriga = DbUtils.getListMap(Db.getConn(), "SELECT r.id_padre, r.iva, CAST(IFNULL(i.codice_readytec, percentuale) AS CHAR(20)) as iva_rea, sum(totale_ivato) as tivato, sum(totale_imponibile) as timpo, sum(totale_ivato)-sum(totale_imponibile) as tiva FROM righ_fatt r left join codici_iva i on r.iva = i.codice WHERE iva != '' GROUP BY id_padre, iva having sum(totale_imponibile) != 0");
            List<Map> iveriga = DbUtils.getListMap(Db.getConn(), "SELECT t.id_padre, t.codice_iva, CAST(IFNULL(i.codice_readytec, t.perc_iva) AS CHAR(20)) as iva_rea, t.totale as tivato, t.imponibile as timpo, t.iva as tiva FROM test_fatt_iva t left join codici_iva i on t.codice_iva = i.codice WHERE t.codice_iva != '' and t.totale != 0");
            HashMap<Integer, ArrayList> iveriga2 = new HashMap();
            System.out.println("inizio group");
            for (Map m : iveriga) {
                Integer id = cu.i(m.get("id_padre"));
                if (!iveriga2.containsKey(id)) {
                    iveriga2.put(id, new ArrayList());
                }
                ArrayList l = iveriga2.get(id);
                l.add(m);
            }

            String qScad = "SELECT fat.id, sum(par.importo) as pagato FROM test_fatt fat LEFT JOIN scadenze sca ON (sca.documento_tipo = 'FA' AND sca.id_doc = fat.id) LEFT JOIN scadenze_parziali par ON par.id_scadenza = sca.id GROUP BY fat.id";
            List<Map> pagamentiList = DbUtils.getListMap(Db.getConn(), qScad);
            HashMap<Integer, Double> pagamenti = new HashMap();
            System.out.println("inizio group");
            for (Map m : pagamentiList) {
                Integer id = (Integer) m.get("id");
                Double importo = CastUtils.toDouble0(m.get("pagato"));
                pagamenti.put(id, importo);
            }

            System.out.println("fine group");

            //prendo scadenze
            String sql = "SELECT t.id as fatt_id, s.* "
                    + " FROM test_fatt t "
                    + " LEFT JOIN scadenze s ON (s.documento_tipo = 'FA' AND s.id_doc = t.id)";
            sql += " where 1 = 1";
            if (frm.comCliente.getSelectedIndex() > 0) {
                sql += " and t.cliente = '" + frm.comCliente.getSelectedKey() + "'";
            }
            if (frm.comArticolo.getSelectedIndex() > 0) {
                sql += " and r.codice_articolo = " + it.tnx.Db.pc(frm.comArticolo.getSelectedKey(), 12);
            }
            if (frm.dal.getDate() != null) {
                sql += " and t.data >= " + DbUtils.pc2(frm.dal.getDate(), 91);
            }
            if (frm.al.getDate() != null) {
                sql += " and t.data <= " + DbUtils.pc2(frm.al.getDate(), 91);
            }
            if (frm.comFornitore.getSelectedIndex() > 0) {
                sql += " and t.fornitore = '" + frm.comCliente.getSelectedKey() + "'";
            }
            sql += " order by t.anno, t.serie, t.numero, s.numero";
            System.out.println("sql scadenze = " + sql);
            List<Map> scadenze = DbUtils.getListMap(Db.getConn(), sql);
            HashMap<Integer, List<Map>> scadenze_per_fat = new HashMap();
            if (estrai_scadenze) {
                for (Map m : scadenze) {
                    Integer id = cu.toInteger(m.get("fatt_id"));
                    List<Map> list_per_fat = null;
                    if (scadenze_per_fat.containsKey(id)) {
                        //aggiorno
                        list_per_fat = scadenze_per_fat.get(id);
                    } else {
                        //inserisco
                        list_per_fat = new ArrayList();
                    }
                    list_per_fat.add(m);
                    scadenze_per_fat.put(id, list_per_fat);
                }
            }

//            while (rs.next()) {
            for (Map m : rs) {
                if (StringUtils.isEmpty(cu.s(m.get("ragione_sociale"))) || StringUtils.isEmpty(cu.s(m.get("indirizzo")))
                        || StringUtils.isEmpty(cu.s(m.get("cap"))) || StringUtils.isEmpty(cu.s(m.get("localita")))) {
                    erroreCliente = true;
                    clientiConErrori += cu.s(m.get("codice")) + " - " + cu.s(m.get("ragione_sociale")) + "\n";
                }
            }

            if (!erroreCliente) {
                for (Map rsm : rs) {
                    ArrayList<CampoReadyTec> listaCampi = new ArrayList<CampoReadyTec>();
                    int tipoFattura = 1;
                    if (cu.i(rsm.get("tipo_fattura")) == dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO) {
                        tipoFattura = 2;
                    }

                    //controllo codici conto diversi
                    System.out.println("rsm = " + rsm);
                    sql = "select IFNULL(i.conto_readytec,'') as conto_readytec from test_fatt t "
                            + " left join righ_fatt r on t.id = r.id_padre"
                            + " left join codici_iva i on r.iva = i.codice"
                            + " where t.id = " + rsm.get("id")
                            + " group by IFNULL(i.conto_readytec,'')";
                    List<Map> list_conti_iva = dbu.getListMap(Db.getConn(), sql);
                    codiceContoDefault = codiceContoDefaultOrig;
                    if (list_conti_iva.size() > 1) {
                        SwingUtils.showErrorMessage(frm, "La fattura " + rsm.get("serie") + rsm.get("numero") + " del " + rsm.get("data") + " contiene più di un conto è non è possibile esportarla");
                        continue;
                    } else if (list_conti_iva.size() == 1) {
                        String conto_iva = cu.s(list_conti_iva.get(0).get("conto_readytec"));
                        if (StringUtils.isNotBlank(conto_iva)) {
                            System.out.println("impostato conto da codice iva = " + conto_iva + " invece di conto default = " + codiceContoDefault);
                            codiceContoDefault = conto_iva;
                        }
                    } else {
                        throw new Exception("Manca il codice/conto FATSEQ in anagrafica IVA");
                    }

                    /* Compilo i campi */
                    listaCampi.add(new CampoReadyTec("DITTANUM", null, CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, 0, CampoReadyTec.SEGNO_NO, "", 4, false));
                    listaCampi.add(new CampoReadyTec("TIPODOC", tipoFattura, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_NO, "", 1, true));

                    listaCampi.add(new CampoReadyTec("DATAFAT", cu.toDate(rsm.get("data")), CampoReadyTec.TIPO_DATA, CampoReadyTec.ALLINEAMENTO_DATA_ENG, 0, CampoReadyTec.DATA_NO_SECOLO, ",", 6, true));
                    String serie = "";
                    if (main.getPersonalContain("premioceleste") && cu.s(rsm.get("serie")).equals("P")) {
                        serie = "1";
                    } else if (tipoFattura != 2) {
                        serie = cu.s(rsm.get("serie"));
                        if (StringUtils.isNotBlank(serie)) {
                            try {
                                int asci = (int) serie.toUpperCase().charAt(0);
                                if (asci >= 65 && asci <= 90) {
                                    serie = String.valueOf((asci - 64));
                                } else {
                                    String msg = "";
                                    try {
                                        msg = cu.s(rsm.get("serie")) + "/" + cu.i(rsm.get("numero"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore, la fattura " + msg + " ha la serie non alfabetica e non è gestitibile");
                                    return;
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    } else {
                        serie = "";
                    }

                    if (serie.equals("") && cu.i(rsm.get("numero")) == 504) {
                        System.out.println("debug");
                    }

                    listaCampi.add(new CampoReadyTec("SERIE", serie, CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, 2, CampoReadyTec.SEGNO_NO, ",", 2, false));

                    listaCampi.add(new CampoReadyTec("NUMFAT", cu.i(rsm.get("numero")), CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_NO, "", 6, true));

                    if (cu.i(rsm.get("numero")) == 2022) {
                        System.out.println("stop");
                    }

                    listaCampi.add(new CampoReadyTec("CLFO", null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_NO, "", 5, false));
                    listaCampi.add(new CampoReadyTec("RAGSOC", cu.s(rsm.get("ragione_sociale")), CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, 0, CampoReadyTec.SEGNO_NO, "", 32, true));
                    listaCampi.add(new CampoReadyTec("INDIRIZZO", cu.s(rsm.get("indirizzo")), CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, 0, CampoReadyTec.SEGNO_NO, "", 32, true));
                    listaCampi.add(new CampoReadyTec("CAP", cu.s(rsm.get("cap")), CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, 0, CampoReadyTec.SEGNO_NO, "", 5, true));
                    listaCampi.add(new CampoReadyTec("LOCALITA", cu.s(rsm.get("localita")), CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, 0, CampoReadyTec.SEGNO_NO, "", 25, true));
                    listaCampi.add(new CampoReadyTec("PROV", cu.s(rsm.get("provincia")).toUpperCase(), CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, 0, CampoReadyTec.SEGNO_NO, "", 2, false));
                    listaCampi.add(new CampoReadyTec("PARTIVA", cu.s(rsm.get("piva_cfiscale")).toUpperCase(), CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, 0, CampoReadyTec.SEGNO_NO, "", 11, false));
                    listaCampi.add(new CampoReadyTec("CODFISC", cu.s(rsm.get("cfiscale")).toUpperCase(), CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, 0, CampoReadyTec.SEGNO_NO, "", 16, false));

                    /* IMPONIBILI */
                    System.out.println("rsm.get(\"id\") = " + rsm.get("id"));
                    ArrayList<Map> codiciIva = iveriga2.get(cu.i(rsm.get("id")));

                    if (codiciIva == null) {
                        System.out.println("stop");
                    }

                    for (int numCodiciIva = 1; numCodiciIva <= 8; numCodiciIva++) {
                        if (codiciIva != null && codiciIva.size() >= numCodiciIva) {
                            try {
                                Double timpo = Math.abs(CastUtils.toDouble0(codiciIva.get(numCodiciIva - 1).get("timpo")));

                                listaCampi.add(new CampoReadyTec("FAT-IMPONIBILE-" + numCodiciIva, (timpo != 0 ? timpo : null), CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 12, false));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                erroreIva = true;
                            }
                        } else {
                            listaCampi.add(new CampoReadyTec("FAT-IMPONIBILE-" + numCodiciIva, null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_NO, "", 12, false));
                        }
                    }

                    /* ALIQUOTE */
                    for (int numCodiciIva = 1; numCodiciIva <= 8; numCodiciIva++) {
                        if (codiciIva != null && codiciIva.size() >= numCodiciIva) {
                            try {
                                Integer codicei = CastUtils.toInteger0(codiciIva.get(numCodiciIva - 1).get("iva_rea"));

                                if (codicei == 0) {
                                    if (codiceIvaDefault.equals("")) {
                                        codiciIvaNonCollegati += "Impostare il codice di collegamento TeamSystem FATSEQ per il codice IVA " + codiciIva.get(numCodiciIva - 1).get("iva") + "\n";
                                    } else {
                                        codicei = CastUtils.toInteger0(codiceIvaDefault);
                                    }
                                }

                                listaCampi.add(new CampoReadyTec("FAT-ALIVA-" + numCodiciIva, codicei != 0 ? codicei : null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_NO, "", 3, false));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                erroreIva = true;
                            }
                        } else {
                            listaCampi.add(new CampoReadyTec("FAT-ALIVA-" + numCodiciIva, null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_NO, "", 3, false));
                        }
                    }


                    /* IMPORTI */
                    for (int numCodiciIva = 1; numCodiciIva <= 8; numCodiciIva++) {
                        if (codiciIva != null && codiciIva.size() >= numCodiciIva) {
                            try {
                                Double tiva = Math.abs(CastUtils.toDouble0(codiciIva.get(numCodiciIva - 1).get("tiva")));

                                listaCampi.add(new CampoReadyTec("FAT-IMPIVA-" + numCodiciIva, tiva != 0 ? tiva : null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 10, false));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                erroreIva = true;
                            }
                        } else {
                            listaCampi.add(new CampoReadyTec("FAT-IMPIVA-" + numCodiciIva, null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_NO, "", 10, false));
                        }
                    }

                    listaCampi.add(new CampoReadyTec("FAT-TIPOIVA", 0, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_NO, "", 3, true));

                    //AGGIUNGO CODICE DI PAGAMENTO
//                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-1", null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 15, false));
                    listaCampi.add(new CampoReadyTec("FAT-CODPAGIN", rsm.get("id_pagamento_teamsystem"), CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_NO, "", 3, false));
                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-1", null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 12, false));

                    if (estrai_acconti) {
                        listaCampi.add(new CampoReadyTec("FAT-ACCONTI", pagamenti.get(cu.i(rsm.get("id"))), CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 10, false));
                    } else {
                        listaCampi.add(new CampoReadyTec("FAT-ACCONTI", 0, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 10, false));
                    }
//                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-1", null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 48, false));
                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-1", null, CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, CampoReadyTec.SEGNO_NO, 0, "", 33, false));
                    listaCampi.add(new CampoReadyTec("FAT-ESIGIBILITA-DIFFERITA", esigDiffIva, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, CampoReadyTec.SEGNO_NO, 0, "", 1, false));
                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-1", null, CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_SINISTRA, CampoReadyTec.SEGNO_NO, 0, "", 14, false));
                    
                    listaCampi.add(new CampoReadyTec("FAT-IMPSPINC", cu.d0(rsm.get("spese_incasso")), CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 10, false));
                    listaCampi.add(new CampoReadyTec("FAT-IMPBOLLI", cu.d0(rsm.get("marca_da_bollo")), CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 10, false));

                    Double totaleConto = cu.d0(rsm.get("totale_imponibile")) - cu.d0(rsm.get("spese_incasso")) - cu.d0(rsm.get("spese_trasporto")) - cu.d0(rsm.get("marca_da_bollo"));

                    //aggiungo scadenze
//                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-2", null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_DOPO, "", 384, false));
                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-2", null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_DOPO, "", 36, false));
                    //ciclo da 1 a 12 per le scadenze
                    int id = cu.i(rsm.get("id"));
                    List<Map> lscadenze = null;
                    try {
                        lscadenze = scadenze_per_fat.get(id);
                    } catch (Exception e) {
                    }
                    int nscadenze = 0;
                    if (lscadenze != null) {
                        nscadenze = lscadenze.size();
                    }
                    for (int i = 1; i <= 12; i++) {
                        //FAT-TIPO-EFF-01           Tipo eff. 1=TR,2=RB,3=RD,6=Cont 01    F   N   480  480    1       N   0
                        Integer s = null;
                        if (i <= nscadenze) {
                            s = cu.toInteger(rsm.get("tipo_effetto_teamsystem"));
                        }
                        listaCampi.add(new CampoReadyTec("FAT-TIPO-EFF-0" + i, s, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_NO, "", 1, false));
                    }
                    for (int i = 1; i <= 12; i++) {
                        //FAT-DATA-SCAD-01          Data scadenza effetto AAAAMMGG  01    F   D   492  499    8   A       8
                        //listaCampi.add(new CampoReadyTec("DATAFAT", rs.getDate("data"), CampoReadyTec.TIPO_DATA, CampoReadyTec.ALLINEAMENTO_DATA_ENG, 0, CampoReadyTec.DATA_NO_SECOLO, ",", 6, true));                        
                        Date data = null;
                        if (i <= nscadenze) {
                            data = cu.toDate(lscadenze.get(i - 1).get("data_scadenza"));
                        }
                        listaCampi.add(new CampoReadyTec("FAT-DATA-SCAD-0" + i, data, CampoReadyTec.TIPO_DATA, CampoReadyTec.ALLINEAMENTO_DATA_ENG, 0, CampoReadyTec.DATA_CON_SECOLO, ",", 8, false));
                    }
                    for (int i = 1; i <= 12; i++) {
                        //FAT-IMPO-SCAD-01          Importo effetto 01                    F   N   588  599   12       D   0
                        //listaCampi.add(new CampoReadyTec("FAT-TOT-DOCUM", Math.abs(rs.getDouble("totale")), CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 12, false));
                        Double importo = null;
                        if (i <= nscadenze) {
                            importo = cu.toDouble(lscadenze.get(i - 1).get("importo"));
                        }
                        listaCampi.add(new CampoReadyTec("FAT-IMPO-SCAD-01" + i, importo, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 12, false));
                    }
                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-2", null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_DOPO, "", 96, false));
                    //fine change per scadenze

                    listaCampi.add(new CampoReadyTec("FAT-CONTO-1", codiceContoDefault, CampoReadyTec.TIPO_ALFANUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_NO, "", 7, false));
                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-3", null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_DOPO, "", 133, false));
                    listaCampi.add(new CampoReadyTec("FAT-TOT-CONTO-01", Math.abs(totaleConto), CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 12, false));
                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-4", null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_DOPO, "", 228, false));

                    //per le ritenute metto totale da pagare se inserito
//                    listaCampi.add(new CampoReadyTec("FAT-TOT-DOCUM", Math.abs(cu.d(rsm.get("totale"))), CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 12, false));
                    double totale = 0;
                    if (cu.d0(rsm.get("totale_da_pagare")) != 0) {
                        totale = cu.d0(rsm.get("totale_da_pagare"));
                    } else {
                        totale = cu.d0(rsm.get("totale"));
                    }
                    listaCampi.add(new CampoReadyTec("FAT-TOT-DOCUM", Math.abs(totale), CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 2, CampoReadyTec.SEGNO_DOPO, "", 12, false));

                    listaCampi.add(new CampoReadyTec("FAT-TIPOFAT", 3, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_NO, "", 1, false));
                    
//                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-5", null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_DOPO, "", 80, false));
                    listaCampi.add(new CampoReadyTec("RIEMPIMENTO-5", null, CampoReadyTec.TIPO_NUMERICO, CampoReadyTec.ALLINEAMENTO_DESTRA, 0, CampoReadyTec.SEGNO_DOPO, "", 78, false));
                    listaCampi.add(new CampoReadyTec("VERSIONE-1", "1 ", CampoReadyTec.TIPO_ALFANUMERICO_NOTRIM, CampoReadyTec.ALLINEAMENTO_SINISTRA, 0, CampoReadyTec.SEGNO_NO, "", 2, false));

                    listaRighe.add(listaCampi);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore nella lettura dei dati", "Errore Database");
        } catch (Exception ex) {
            ex.printStackTrace();
            SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore: " + ex.getMessage(), "Errore");
        }
    }

    @Override
    public void run() {
        try {
            frm.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (erroreCliente || erroreIva) {
                if (erroreCliente) {
                    SwingUtils.showErrorMessage(main.getPadre(), "Le anagrafiche di alcuni clienti non sono complete:\n" + clientiConErrori, "Errore anagrafica clienti", true);
                }
                if (!codiciIvaNonCollegati.equals("")) {
                    SwingUtils.showErrorMessage(main.getPadre(), codiciIvaNonCollegati, "Errore anagrafica clienti", true);
                }
                if (erroreIva) {
                    SwingUtils.showErrorMessage(main.getPadre(), "Si è verificato un errore nella lettura dei codici IVA", "Errore codici IVA", true);
                }

                SwingUtils.showErrorMessage(main.getPadre(), "Impossibile proseguire", "Errore");

            } else {
                String dir = System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "TeamSystemFATSEQExport" + File.separator;
                File startDir = new File(dir);
                if (!startDir.exists()) {
                    startDir.mkdir();
                }

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmm");
                nomeFile = dir + "export_TeamSystem_FATSEQ_" + sdf.format(new java.util.Date()) + ".txt";

                FileOutputStream fos = new FileOutputStream(nomeFile);

                for (ArrayList<CampoReadyTec> riga : listaRighe) {
                    String concat = "";
                    System.out.println("riga = " + riga);
                    for (CampoReadyTec campo : riga) {
                        try {
                            String rigaText = campo.formatCampo();
                            rigaText = rigaText.replace("à", "a");
                            rigaText = rigaText.replace("è", "e");
                            rigaText = rigaText.replace("é", "e");
                            rigaText = rigaText.replace("ì", "i");
                            rigaText = rigaText.replace("ò", "o");
                            rigaText = rigaText.replace("ù", "u");
                            concat += rigaText;
                        } catch (Exception e) {
                            e.printStackTrace();
                            SwingUtils.showErrorMessage(main.getPadreFrame(), e.getMessage(), "Errore Export");
                            frm.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            return;
                        }
                    }

//                    concat += System.getProperty("line.separator");
                    concat += "\r\n"; //forzo eol di windows

                    fos.write(concat.getBytes("ISO-8859-1"));
                }

                fos.close();
                Util.start2(dir);
            }
            frm.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class CampoReadyTec {
    // Variabili statiche
    // Tipo di campo

    public static char TIPO_ALFANUMERICO = 'A';
    public static char TIPO_ALFANUMERICO_NOTRIM = 'B';
    public static char TIPO_NUMERICO = 'N';
    public static char TIPO_DATA = 'D';
    // Allineamento o Formato data
    public static char ALLINEAMENTO_SINISTRA = 'S';
    public static char ALLINEAMENTO_DESTRA = 'D';
    public static char ALLINEAMENTO_DATA_ENG = 'A';
    public static char ALLINEAMENTO_DATA_ITA = 'G';
    // Formato segno
    public static int SEGNO_NO = 0;
    public static int SEGNO_PRIMA = 1;
    public static int SEGNO_DOPO = 2;
    // Formato data decimali
    public static int DATA_CON_SECOLO = 8;
    public static int DATA_NO_SECOLO = 6;
    // Variabili di istanza
    private String nomeCampo;
    private Object valore;
    private char tipo;
    private char allineamento_formato;
    private int segno;
    private int decimali_secolo;
    private String virgola;
    private int lunghezza;
    private boolean obbligatorio;

    public CampoReadyTec(String nomeCampo, Object value, char tipo, char allineamento, int segno, int decimali, String virgola, int lunghezza, boolean obbligatorio) {
        this.nomeCampo = nomeCampo;
        if (value instanceof String) {
            String ret = (String) value;
            ret = StringUtils.replace(ret, "\n", " ");
            ret = StringUtils.replace(ret, "\r", " ");
            ret = StringUtils.replace(ret, "\t", " ");
            this.valore = ret;
        } else {
            this.valore = value;
        }
        this.tipo = tipo;
        this.allineamento_formato = allineamento;
        this.segno = segno;
        this.decimali_secolo = decimali;
        this.virgola = virgola;
        this.lunghezza = lunghezza;
        this.obbligatorio = obbligatorio;
    }

    public String getNomeCampo() {
        return this.nomeCampo;
    }

    private boolean isEmpty() {
        if (valore instanceof String) {
            String ret = String.valueOf(valore);
            return ret.equals("");
        } else {
            return valore == null;
        }
    }

    public String formatCampo() throws Exception {
        String field = "";

        if (this.valore != null && !isEmpty()) {
            if (this.tipo == CampoReadyTec.TIPO_ALFANUMERICO) {
                field = String.valueOf(valore).trim().toUpperCase();
            } else if (this.tipo == CampoReadyTec.TIPO_ALFANUMERICO_NOTRIM) {
                field = String.valueOf(valore).toUpperCase();                
            } else if (this.tipo == CampoReadyTec.TIPO_NUMERICO) {
                String sign = "";
                if (this.decimali_secolo == 0) {
                    int value = -1;
                    try {
                        if (valore instanceof Double) {
                            double val = (Double) valore;
                            value = Integer.parseInt(FormatUtils.formatNumNoDec(val));
                        } else {
                            value = Integer.parseInt(String.valueOf(valore));
                        }
                    } catch (Exception e) {
                        throw new Exception("Impossibile usare il campo " + nomeCampo + " come numero");
                    }
                    if (value >= 0) {
                        sign = "+";
                    } else {
                        sign = "-";
                    }
                    field = String.valueOf(value);
                } else {
                    double value = 0d;
                    try {
                        value = Double.parseDouble(String.valueOf(valore));
                        field = FormatUtils.formatParametr(value, decimali_secolo, virgola);
                        field = field.replace(",", "");
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("Impossibile usare il campo " + nomeCampo + " come decimale");
                    }

                    if (value >= 0) {
                        sign = "+";
                    } else {
                        sign = "-";
                    }
//                    field = String.valueOf(value);
                }

                if (this.segno == CampoReadyTec.SEGNO_PRIMA) {
                    field = sign + field;
                } else if (this.segno == CampoReadyTec.SEGNO_DOPO) {
                    field = field + sign;
                }

            } else if (this.tipo == CampoReadyTec.TIPO_DATA) {
                String value = String.valueOf(valore);
                String[] dataSplit = value.split("-");

                String anno = dataSplit[0];
                String mese = dataSplit[1];
                String giorno = dataSplit[2];

                if (this.decimali_secolo == CampoReadyTec.DATA_NO_SECOLO) {
                    anno = anno.substring(2);
                }

                if (this.allineamento_formato == CampoReadyTec.ALLINEAMENTO_DATA_ENG) {
                    field = anno + mese + giorno;
                } else {
                    field = giorno + mese + anno;
                }
            }
        } else if (this.obbligatorio) {
            throw new Exception("Il campo " + this.nomeCampo + " è obbligatorio, non sono stati passati valori. Controllare l'anagrafica per procedere con l'export");
        } else {
            field = "";
        }

        if (field.length() > this.lunghezza) {
            field = field.substring(0, this.lunghezza);
//            if (this.allineamento_formato == CampoReadyTec.ALLINEAMENTO_DESTRA) {
//                // Allineamento a destra, quindi cancello a sinistra
//                field = field.substring(field.length() - this.lunghezza);
//            } else {
//                // Allineamento a sinistra, quindi cancello a destra
//                field = field.substring(0, this.lunghezza);
//            }
        } else if (field.length() < this.lunghezza) {
            while (field.length() < this.lunghezza) {
                String riempimento = " ";
                if (this.allineamento_formato == CampoReadyTec.ALLINEAMENTO_DESTRA) {
                    // Allineamento a destra, quindi aggiungo a sinistra
                    field = riempimento + field;
                } else {
                    // Allineamento a sinistra, quindi aggiungo a destra
                    field += riempimento;
                }
            }
        }

        return field;
    }
}
