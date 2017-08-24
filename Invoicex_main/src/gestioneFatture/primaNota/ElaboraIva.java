/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gestioneFatture.primaNota;

import gestioneFatture.JDialogCompilazioneReport;
import gestioneFatture.Reports;
import gestioneFatture.Util;
import gestioneFatture.main;
import it.tnx.Db;
import it.tnx.SwingWorker;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.SystemUtils;
import it.tnx.commons.cu;
import it.tnx.invoicex.InvoicexUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

/**
 *
 * @author Marco
 */
public class ElaboraIva extends SwingWorker {

    public frmStampaRegistroIva padre = null;
    public boolean xls = false;
    public JasperPrint jasperPrint;

    public Object construct() {
        final JDialogCompilazioneReport dialog = new JDialogCompilazioneReport();

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        main.fileIni.setValue("frm_stampa_registro_iva", "data", padre.comData.getSelectedItem());

        String sql = "";

        //prima rigenero la prima nota del periodo
        int anno = 0;

        try {
            anno = Integer.parseInt(padre.texAnno.getText());
        } catch (Exception err) {
            err.printStackTrace();
        }

        gestioneFatture.primaNota.PrimaNotaUtils pn = new gestioneFatture.primaNota.PrimaNotaUtils(dialog);
        pn.generaPrimaNota(padre.tipoLiquidazione, padre.comPeriodo.getSelectedIndex() + 1, anno, (padre.comData.getSelectedIndex() == 0 ? false : true), padre.scontrini.isSelected(), padre.dal.getDate(), padre.al.getDate(), padre.stampa_definitiva.isSelected());

        try {

            //con compilazione
            File frep = new File("reports/iva.jrxml");
            JasperReport jasperReport = Reports.getReport(frep);

                //System.out.println("load jrxml");
            //JasperDesign jasperDesign = JasperManager.loadXmlDesign("reports/iva.jrxml");
            //System.out.print("compilazione...");
            //JasperReport jasperReport = JasperManager.compileReport(jasperDesign);
            //System.out.println("...ok");
            //senza compilazione
            //System.out.println("load jasper");
            //JasperReport jasperReport = JasperManager.loadReport("reports/iva.jasper");
//            JasperReport jasperReport = JasperManager.loadReport(getClass().getResourceAsStream("/reports/iva.jasper"));
            // Second, create a map of parameters to pass to the report.
            java.util.Map parameters = new java.util.HashMap();
            if (padre.radAnnuale.isSelected()) {
                parameters.put("periodo", "Anno " + padre.texAnno.getText());
            } else if (padre.radData.isSelected()) {
                parameters.put("periodo", "Dal " + DateUtils.formatDateIta(padre.dal.getDate()) + " al " + DateUtils.formatDateIta(padre.al.getDate()));
            } else {
                parameters.put("periodo", padre.comPeriodo.getSelectedItem() + " " + padre.texAnno.getText());
            }
            parameters.put("anno", padre.texAnno.getText());
            parameters.put("tipo_numerazione_pagine", padre.tipo_numero_pagina.getSelectedIndex());
            int pro = CastUtils.toInteger0(padre.progressivo.getText());
            if (pro > 0) {
                pro--;
            }

            parameters.put("progressivo_partenza", pro);
            String int1 = "";
            try {
                //dati azienda
                sql = "select ragione_sociale, indirizzo, localita, cap, provincia, cfiscale, piva from dati_azienda";
                Map m = DbUtils.getListMap(Db.getConn(), sql).get(0);
                int1 += m.get("ragione_sociale") + ", " + m.get("indirizzo") + ", " + m.get("cap") + " " + m.get("localita") + " (" + m.get("provincia") + "), " + "Partita IVA " + m.get("piva") + ", Codice Fiscale " + m.get("cfiscale");
            } catch (Exception e) {
                e.printStackTrace();
            }
            parameters.put("intestazione1", int1);

            //ciclo per i codici iva descendig senza quelle a zero
//                sql = "select * from codici_iva where percentuale > 0 order by percentuale desc";
//                ResultSet riva = Db.openResultSet(sql);
            int codiceIva = 0;

            parameters.put("iva1", "");
            parameters.put("iva2", "");
            parameters.put("iva3", "");
            parameters.put("iva4", "");
            parameters.put("iva5", "");

//                while (riva.next() && codiceIva <= 5) {
            for (Map miva : pn.riva) {
                codiceIva++;
                if (codiceIva > 5) {
                    break;
                }
                try {
                    parameters.put("iva" + codiceIva, "Aliquota " + it.tnx.Util.formatNumero0Decimali(cu.d(miva.get("percentuale"))) + " %");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            double saldo1 = 0;
            double saldo2 = 0;
            double creditoPeriodoPrec = 0;

            //calcolo totali
            parameters.put("totaleIvaAcquisti", new Double(pn.totali.totaleAcquisti));
            parameters.put("totaleIvaVendite", new Double(pn.totali.totaleVendite));

            DebugUtils.dump(parameters);

            saldo1 = pn.totali.totaleAcquisti - pn.totali.totaleVendite;

            System.out.println("saldo1: " + saldo1);

            parameters.put("ivaSaldo1", new Double(Math.abs(saldo1)));
            creditoPeriodoPrec = it.tnx.Util.getDouble(padre.texIvaPrecedente.getText());
            parameters.put("ivaACreditoPeriodoPrec", new Double(creditoPeriodoPrec));
            saldo2 = saldo1 + creditoPeriodoPrec;
            parameters.put("ivaSaldo2", new Double(Math.abs(saldo2)));

            if (saldo1 < 0) {
                parameters.put("scrittaDebitoCredito1", "Debito");
            } else {
                parameters.put("scrittaDebitoCredito1", "Credito");
            }

            if (saldo2 < 0) {
                parameters.put("scrittaDebitoCredito2", "Debito");
            } else {
                parameters.put("scrittaDebitoCredito2", "Credito");
            }

            if (!xls) {
                Connection conn = it.tnx.Db.getConn();
                jasperPrint = JasperManager.fillReport(jasperReport, parameters, conn);
                InvoicexUtil.apriStampa(jasperPrint);
            } else {
                    //esporto xls
                //stampa_iva_semplice
                Map<String, String> colonne = Collections.synchronizedMap(new LinkedHashMap<String, String>());
                colonne.put("id", "");
                colonne.put("tipo", "");
                colonne.put("data", "data doc. interno");
                colonne.put("numero_prog", "num. doc. interno");
                colonne.put("numero_doc", "num. doc. esterno");
                colonne.put("data_doc", "data doc. esterno");
                colonne.put("ragione_sociale", "");
                colonne.put("piva_cfiscale", "partita iva");
                colonne.put("totale", "");
                colonne.put("imp1", "Imponibile " + parameters.get("iva1"));
                colonne.put("iva1", "Iva " + parameters.get("iva1"));
                colonne.put("imp2", "Imponibile " + parameters.get("iva2"));
                colonne.put("iva2", "Iva " + parameters.get("iva2"));
                colonne.put("imp3", "Imponibile " + parameters.get("iva3"));
                colonne.put("iva3", "Iva " + parameters.get("iva3"));
                colonne.put("imp4", "Imponibile " + parameters.get("iva4"));
                colonne.put("iva4", "Iva " + parameters.get("iva4"));
                colonne.put("imp5", "Imponibile " + parameters.get("iva5"));
                colonne.put("iva5", "Iva " + parameters.get("iva5"));
                colonne.put("altre_imp", "Esenti/Non Imponibili/Fuori campo");
                colonne.put("imp_deducibile", "imponibile indeducibile");
                colonne.put("iva_deducibile", "iva indeducibile");

                File exportDir = new File(SystemUtils.getUserDocumentsFolder() + File.separator + "Invoicex" + File.separator + "export");
                exportDir.mkdirs();
                File nomeFile = new File(SystemUtils.getUserDocumentsFolder() + File.separator + "Invoicex" + File.separator + "export" + File.separator + "iva.xls");
                    //boolean ret = InvoicexUtil.esportaInExcel(r, nomeFile.getAbsolutePath(), "Registro IVA", int1, "", colonne, true);

                try {
                    HSSFWorkbook wb = new HSSFWorkbook();
                    HSSFSheet sheet = wb.createSheet("Fatture di acquisto");
                    sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
                    ResultSet racq = DbUtils.tryOpenResultSet(Db.getConn(), "select * from stampa_iva_semplice where tipo = 'A'");
                    short row_tot_acq = accodaXls(racq, sheet, colonne, wb);
                    sheet = wb.createSheet("Fatture di vendita");
                    sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
                    ResultSet rven = DbUtils.tryOpenResultSet(Db.getConn(), "select * from stampa_iva_semplice where tipo = 'V'");
                    short row_tot_ven = accodaXls(rven, sheet, colonne, wb);
                    sheet = wb.createSheet("Totali");
                    sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
                    accodaXlsTotali(creditoPeriodoPrec, sheet, wb, row_tot_acq, row_tot_ven, parameters);
                    FileOutputStream fileOut = new FileOutputStream(nomeFile);
                    wb.write(fileOut);
                    fileOut.close();
                    Util.start2(nomeFile.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtils.showErrorMessage(padre, "Errore: " + ex.getMessage());
                }
            }

        } catch (Exception err) {
            err.printStackTrace();
        } finally {
            dialog.setVisible(false);
            return null;
        }
    }

    @Override
    public void finished() {
        padre.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    private Short accodaXls(ResultSet rs, HSSFSheet sheet, Map colonne, HSSFWorkbook wb) {
        try {
            short contarows = 0;
            HSSFDataFormat format = wb.createDataFormat();

            HSSFRow row = sheet.createRow(contarows);
            contarows++;
            row.createCell(0).setCellValue(padre.getTitle());

            row = sheet.createRow(contarows);
            contarows++;
            row = sheet.createRow(contarows);
            contarows++;
            row.createCell(0).setCellValue(sheet.getSheetName());

            row = sheet.createRow(contarows);
            contarows++;
            //colonne
            row = sheet.createRow(contarows);
            contarows++;
            int columns = 0;
            columns = rs.getMetaData().getColumnCount();

            Iterator iter = colonne.keySet().iterator();
            short i = 0;
            while (iter.hasNext()) {
                Object key = iter.next();
                Object value = colonne.get(key);
                String col = "";
                //col = rs.getMetaData().getColumnLabel(i+1);
                col = (String) value;
                if (col == null || col.length() == 0) {
                    col = (String) key;
                }
                row.createCell(i).setCellValue(col);
                i++;
                //sheet.setColumnWidth(i, (headerWidth[i] * 300));
            }

            //stili
            HSSFCellStyle styledouble = wb.createCellStyle();
            styledouble.setDataFormat(format.getFormat("#,##0.00###"));

            HSSFCellStyle styleint = wb.createCellStyle();
            styleint.setDataFormat(format.getFormat("#,##0"));

            HSSFCellStyle styledata = wb.createCellStyle();
            styledata.setDataFormat(format.getFormat("dd/MM/yy"));

            //righe
            int rowcount = 0;
            rs.last();
            rowcount = rs.getRow();
            rs.beforeFirst();
            for (int j = 0; j < rowcount; j++) {
                row = sheet.createRow(contarows);
                contarows++;
                //colonne
                iter = colonne.keySet().iterator();
                i = 0;
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String value = (String) colonne.get(key);

                    //controllo tipo di campo
                    Object o = null;
                    rs.absolute(j + 1);
                    o = rs.getObject(key);
                    if (o instanceof Double) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue((Double) o);
                        cell.setCellStyle(styledouble);
                    } else if (o instanceof BigDecimal) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue(((BigDecimal) o).doubleValue());
                        cell.setCellStyle(styledouble);
                    } else if (o instanceof Integer) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue(((Integer) o).intValue());
                        cell.setCellStyle(styleint);
                    } else if (o instanceof java.sql.Date) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue(((java.sql.Date) o));
                        cell.setCellStyle(styledata);
                    } else if (o instanceof byte[]) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue(new String((byte[]) o));
                        cell.setCellStyle(styleint);
                        row.createCell(i).setCellValue(new String((byte[]) o));
                    } else if (o instanceof Long) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue(((Long) o).longValue());
                        cell.setCellStyle(styleint);
                    } else {
                        if (!(o instanceof String)) {
                            if (o != null) {
                                System.out.println(o.getClass());
                            }
                        }
                        row.createCell(i).setCellValue(CastUtils.toString(o));
                    }
                    i++;
                }
            }

            //formule totali
            row = sheet.createRow(contarows);
            contarows++;
            row = sheet.createRow(contarows);
            row.createCell(7).setCellValue("Totali");
            row.createCell(8).setCellFormula("SUM(I1:I" + contarows + ")");
            row.getCell(8).setCellStyle(styledouble);
            row.createCell(9).setCellFormula("SUM(J1:J" + contarows + ")");
            row.getCell(9).setCellStyle(styledouble);
            row.createCell(10).setCellFormula("SUM(K1:K" + contarows + ")");
            row.getCell(10).setCellStyle(styledouble);
            row.createCell(11).setCellFormula("SUM(L1:L" + contarows + ")");
            row.getCell(11).setCellStyle(styledouble);
            row.createCell(12).setCellFormula("SUM(M1:M" + contarows + ")");
            row.getCell(12).setCellStyle(styledouble);
            row.createCell(13).setCellFormula("SUM(N1:N" + contarows + ")");
            row.getCell(13).setCellStyle(styledouble);
            row.createCell(14).setCellFormula("SUM(O1:O" + contarows + ")");
            row.getCell(14).setCellStyle(styledouble);
            row.createCell(15).setCellFormula("SUM(P1:P" + contarows + ")");
            row.getCell(15).setCellStyle(styledouble);
            row.createCell(16).setCellFormula("SUM(Q1:Q" + contarows + ")");
            row.getCell(16).setCellStyle(styledouble);
            row.createCell(17).setCellFormula("SUM(R1:R" + contarows + ")");
            row.getCell(17).setCellStyle(styledouble);
            row.createCell(18).setCellFormula("SUM(S1:S" + contarows + ")");
            row.getCell(18).setCellStyle(styledouble);
            row.createCell(19).setCellFormula("SUM(T1:T" + contarows + ")");
            row.getCell(19).setCellStyle(styledouble);
            row.createCell(20).setCellFormula("SUM(U1:U" + contarows + ")");
            row.getCell(20).setCellStyle(styledouble);
            row.createCell(21).setCellFormula("SUM(V1:V" + contarows + ")");
            row.getCell(21).setCellStyle(styledouble);

            contarows++;

//                if (note_piede != null && note_piede.length() > 0) {
//                    row = sheet.createRow(contarows);
//                    contarows++;
//                    row = sheet.createRow(contarows);
//                    contarows++;
//                    row.createCell(0).setCellValue(note_piede);
//                }
            return contarows;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private boolean accodaXlsTotali(double creditoPeriodoPrec, HSSFSheet sheet, HSSFWorkbook wb, short row_tot_acq, short row_tot_ven, Map params) {
        try {
            short contarows = 0;
            HSSFDataFormat format = wb.createDataFormat();
            HSSFCellStyle styledouble = wb.createCellStyle();
            styledouble.setDataFormat(format.getFormat("#,##0.00###"));

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            HSSFRow row = sheet.createRow(contarows);
            contarows++;
            row = sheet.createRow(contarows);
            contarows++;
            row = sheet.createRow(contarows);
            contarows++;
            row.createCell(1).setCellValue("Iva per Fatture di vendita");
            row.createCell(2).setCellFormula("'Fatture di vendita'!K" + row_tot_ven + " + "
                    + "'Fatture di vendita'!M" + row_tot_ven + " + "
                    + "'Fatture di vendita'!O" + row_tot_ven + " + "
                    + "'Fatture di vendita'!Q" + row_tot_ven + " + "
                    + "'Fatture di vendita'!S" + row_tot_ven);
            evaluator.evaluateFormulaCell(row.getCell(2));
            row.getCell(2).setCellStyle(styledouble);

            row = sheet.createRow(contarows);
            contarows++;
            row.createCell(1).setCellValue("Iva per Fatture di acquisto");
            row.createCell(2).setCellFormula("'Fatture di acquisto'!K" + row_tot_acq + " + "
                    + "'Fatture di acquisto'!M" + row_tot_acq + " + "
                    + "'Fatture di acquisto'!O" + row_tot_acq + " + "
                    + "'Fatture di acquisto'!Q" + row_tot_acq + " + "
                    + "'Fatture di acquisto'!S" + row_tot_acq);
            evaluator.evaluateFormulaCell(row.getCell(2));
            row.getCell(2).setCellStyle(styledouble);

            row = sheet.createRow(contarows);
            contarows++;
            row.createCell(1).setCellFormula("CONCATENATE(\"Iva a \",IF((C3-C4) >= 0, \"debito\", \"credito\"))");
            row.createCell(2).setCellFormula("ABS(C3-C4)");
            evaluator.evaluateFormulaCell(row.getCell(2));
            row.getCell(2).setCellStyle(styledouble);

            row = sheet.createRow(contarows);
            contarows++;
            row.createCell(1).setCellValue("Iva a credito da periodo precedente");
            row.createCell(2).setCellValue((Double) params.get("ivaACreditoPeriodoPrec"));
            row.getCell(2).setCellStyle(styledouble);

            row = sheet.createRow(contarows);
            contarows++;
            //row.createCell(1).setCellValue("Iva a " + params.get("scrittaDebitoCredito2"));
            row.createCell(1).setCellFormula("CONCATENATE(\"Iva a \",IF((C5-C6) >= 0, \"debito\", \"credito\"))");
            row.createCell(2).setCellFormula("ABS(C5-C6)");
            evaluator.evaluateFormulaCell(row.getCell(2));
            row.getCell(2).setCellStyle(styledouble);

            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}