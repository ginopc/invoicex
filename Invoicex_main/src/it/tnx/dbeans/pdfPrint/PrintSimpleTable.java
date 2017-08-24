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
package it.tnx.dbeans.pdfPrint;

import com.Ostermiller.util.CSVPrinter;
import java.awt.*;
import javax.swing.*;

import com.lowagie.text.*;
import com.lowagie.text.html.HtmlWriter;
import com.lowagie.text.pdf.*;
import it.tnx.commons.CastUtils;
import it.tnx.commons.RunnableWithArgs;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.dbeans.ResultSet.LazyResultSetModel;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 *
 * @author root
 */
public class PrintSimpleTable {

    public JTable table;
    public ResultSet rs;
    public NumberFormat f1 = new DecimalFormat(",##0.00###");
    public NumberFormat f2 = new DecimalFormat(",##0");
    public SimpleDateFormat f3 = new SimpleDateFormat("dd/MM/yy");
    public boolean totali = false;

    public boolean non_stampare_valori_ripetuti = false;
    public Integer non_stampare_valori_ripetuti_colonna = null;

    //stili
    public HSSFWorkbook wb = null;
    public HSSFDataFormat format = null;
    public HSSFCellStyle style_double = null;
    public HSSFCellStyle style_int = null;
    public HSSFCellStyle style_date = null;

    /**
     * Creates a new instance of PrintSimpleTable
     */
    public PrintSimpleTable(JTable table) {
        this.table = table;
        init();
    }

    public PrintSimpleTable(ResultSet rs) {
        this.rs = rs;
        init();
    }

    private void init() {
        wb = new HSSFWorkbook();

        style_double = wb.createCellStyle();
        style_int = wb.createCellStyle();
        style_date = wb.createCellStyle();

        format = wb.createDataFormat();

        style_double.setDataFormat(format.getFormat("#,##0.00###"));
        style_int.setDataFormat(format.getFormat("#,##0"));
        style_date.setDataFormat(format.getFormat("dd/MM/yy"));
    }

    public String print(String title, int[] headerWidth) {
        return print(title, headerWidth, "pdf", "", "");
    }

    //stampa generica
    public String print(String title, int[] headerWidth, String formato, String note_testa, String note_piede) {
        return print(title, headerWidth, formato, note_testa, note_piede, 0);
    }

    public String print(String title, int[] headerWidth, String formato, String note_testa, String note_piede, int font_adjust) {
        f1.setGroupingUsed(true);
        f2.setGroupingUsed(true);
        Connection connection;
        java.sql.Statement stat;
        ResultSet resu;
        String nomeFilePdf = "tempStampa.pdf";
        String nomeFileHtml = "tempStampa.html";
        String nomeFileHtmlXls = "tempStampa.html.xls";

        com.lowagie.text.Font bf;
        com.lowagie.text.Font bfb;
        BaseFont bfCour;
        BaseFont bf_italic;
        BaseFont bf_bold;
        BaseFont bf_times;
        Document document;
        PdfWriter writer;
        HtmlWriter writerhtml;

        try {
            //creazione del pdf
            document = new Document(PageSize.A4);
            if (formato.equalsIgnoreCase("pdf")) {
                writer = PdfWriter.getInstance(document, new java.io.FileOutputStream(nomeFilePdf));
            } else if (formato.equalsIgnoreCase("xls")) {
                writerhtml = HtmlWriter.getInstance(document, new java.io.FileOutputStream(nomeFileHtmlXls));
            } else {
                writerhtml = HtmlWriter.getInstance(document, new java.io.FileOutputStream(nomeFileHtml));
            }
            document.addTitle("stampa tabella");
            document.addSubject("stampa tabella");
            document.addKeywords("stampa tabella");
            document.addAuthor("Invoicex");
            document.addHeader("Expires", "0");
            document.open();
            //------------------------------------
            bf = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8 + font_adjust, com.lowagie.text.Font.NORMAL);
            bfb = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8 + font_adjust, com.lowagie.text.Font.BOLD);
            bfCour = BaseFont.createFont("Courier", "winansi", false);
            bf_italic = BaseFont.createFont("Helvetica-Oblique", "winansi", false);
            bf_bold = BaseFont.createFont("Helvetica-Bold", "winansi", false);
            bf_times = BaseFont.createFont("Times-Roman", "winansi", false);
            //BaseFont bf = BaseFont.createFont("Helvetia", "winansi", false);

            //stampa
            //cliente
            double y;
            double iy;
            double x;
            double ix;

            int riga = 0;
            iy = 1.25;
            y = 4;
            //tabella dati testata
            Table datatable;
            //datatable = new Table(meta.getColumnCount());
            if (table != null) {
                datatable = new Table(this.table.getColumnCount());
            } else {
                datatable = new Table(rs.getMetaData().getColumnCount());
            }
            datatable.setBorder(0);
            //datatable.setCellpadding(0);
            datatable.setPadding(2);
            //int headerwidths2[] = {20, 80}; // percentage
            //datatable.setWidths(headerwidths2);
            if (headerWidth != null) {
                datatable.setWidths(headerWidth);
            }
            datatable.setWidth(100); // percentage
            datatable.setCellsFitPage(true);

            Phrase tempFrase;
            Cell tempPdfCell;

            //intestazione
            Phrase intestazione = new Phrase();
            intestazione.add(new Chunk(title, new com.lowagie.text.Font(com.lowagie.text.Font.TIMES_ROMAN, 10 + font_adjust, com.lowagie.text.Font.BOLD)));
            document.add(intestazione);

            if (note_testa != null && note_testa.length() > 0) {
                intestazione = new Phrase();
                System.out.println("leading:" + intestazione.getLeading());
                intestazione.setLeading(10f);
                intestazione.add(new Chunk("\n" + note_testa, new com.lowagie.text.Font(com.lowagie.text.Font.COURIER, 8 + font_adjust, com.lowagie.text.Font.NORMAL)));
                document.add(intestazione);
            }

            //tempPdfCell = new Cell(new Phrase(this. , new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.NORMAL)));
            //colonne
            int columns = 0;
            if (table != null) {
                columns = table.getColumnCount();
            } else {
                columns = rs.getMetaData().getColumnCount();
            }

            for (int i = 0; i < columns; i++) {
                if (table != null) {
                    tempPdfCell = new Cell(new Phrase(this.table.getColumnName(i), bf));
                } else {
                    tempPdfCell = new Cell(new Phrase(rs.getMetaData().getColumnLabel(i + 1), bf));
                }
                set1(tempPdfCell);
                datatable.addCell(tempPdfCell);
            }
            //righe
            int rowcount = 0;
            if (table != null) {
                rowcount = table.getRowCount();
            } else {
                rs.last();
                rowcount = rs.getRow();
                rs.beforeFirst();
            }
            Object[] tots = new Object[columns];
            for (int j = 0; j < rowcount; j++) {
                //colonne
                for (int i = 0; i < columns; i++) {
                    //controllo tipo di campo
                    //tempPdfCell = new Cell(new Phrase(nz(String.valueOf(this.getValueAt(j,i)),"") , bf));
                    Object o = null;
                    if (table != null) {
                        o = this.table.getValueAt(j, i);
                    } else {
                        rs.absolute(j + 1);
                        o = rs.getObject(i + 1);
                    }
                    //System.out.println("o: " + o + " class:" + o.getClass());
                    if (o instanceof Double) {
                        tempPdfCell = new Cell(new Phrase(f1.format((Double) o), bf));
                        set2r(tempPdfCell);
                        tots[i] = CastUtils.toDouble0(tots[i]) + CastUtils.toDouble0(o);
                    } else if (o instanceof BigDecimal) {
                        tempPdfCell = new Cell(new Phrase(f1.format((BigDecimal) o), bf));
                        set2r(tempPdfCell);
                        tots[i] = CastUtils.toDouble0(tots[i]) + CastUtils.toDouble0(o);
                    } else if (o instanceof Integer) {
                        tempPdfCell = new Cell(new Phrase(f2.format((Integer) o), bf));
                        set2r(tempPdfCell);
                        tots[i] = CastUtils.toInteger0(tots[i]) + CastUtils.toInteger0(o);
                    } else if (o instanceof java.sql.Date) {
                        tempPdfCell = new Cell(new Phrase(f3.format((java.sql.Date) o), bf));
                        set2r(tempPdfCell);
                    } else if (o instanceof byte[]) {
                        tempPdfCell = new Cell(new Phrase(new String((byte[]) o), bf));
                        set2r(tempPdfCell);
                    } else if (o instanceof Long) {
                        tempPdfCell = new Cell(new Phrase(f2.format((Long) o), bf));
                        set2r(tempPdfCell);
                        tots[i] = CastUtils.toDouble0(tots[i]) + CastUtils.toDouble0(o);
                    } else {
                        if (!(o instanceof String)) {
                            if (o != null) {
                                System.out.println(o.getClass().getName());
                            }
                        }
                        tempPdfCell = new Cell(new Phrase(nz(o, ""), bf));
                        set2(tempPdfCell);
                    }

                    datatable.addCell(tempPdfCell);
                }
            }

            if (totali) {
                tempPdfCell = new Cell(new Phrase("Totale", bfb));
                set2r(tempPdfCell);
                datatable.addCell(tempPdfCell);
                for (int i = 1; i < columns; i++) {
                    Object o = tots[i];
                    if (o instanceof Double) {
                        tempPdfCell = new Cell(new Phrase(f1.format((Double) o), bfb));
                        set2r(tempPdfCell);
                    } else if (o instanceof BigDecimal) {
                        tempPdfCell = new Cell(new Phrase(f1.format((BigDecimal) o), bfb));
                        set2r(tempPdfCell);
                    } else if (o instanceof Integer) {
                        tempPdfCell = new Cell(new Phrase(f2.format((Integer) o), bfb));
                        set2r(tempPdfCell);
                    } else if (o instanceof Long) {
                        tempPdfCell = new Cell(new Phrase(f2.format((Long) o), bfb));
                        set2r(tempPdfCell);
                    } else {
                        tempPdfCell = new Cell(new Phrase(nz(o, ""), bfb));
                        set2(tempPdfCell);
                    }
                    datatable.addCell(tempPdfCell);
                }
            }

            document.add(datatable);

            if (note_piede != null && note_piede.length() > 0) {
                intestazione = new Phrase();
                intestazione.add(new Chunk(note_piede, new com.lowagie.text.Font(com.lowagie.text.Font.TIMES_ROMAN, 9 + font_adjust, com.lowagie.text.Font.NORMAL)));
                document.add(intestazione);
            }

            //chiudo
            document.close();

            //Runtime.getRuntime().exec("start " + nomeFilePdf);
            //Util.start(nomeFilePdf);
            return (nomeFilePdf);

        } catch (Exception err) {
            err.printStackTrace();
            SwingUtils.showErrorMessage(null, err.getMessage());
            return (null);
        }
    }

    void set1(Cell tempPdfCell) {
        tempPdfCell.setBackgroundColor(new Color(255, 255, 240));
        tempPdfCell.setBorderColor(new Color(200, 200, 200));
        tempPdfCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
    }

    void set2(Cell tempPdfCell) {
        tempPdfCell.setBackgroundColor(new Color(255, 255, 255));
        tempPdfCell.setBorderColor(new Color(200, 200, 200));
        tempPdfCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
    }

    void set2r(Cell tempPdfCell) {
        tempPdfCell.setBackgroundColor(new Color(255, 255, 255));
        tempPdfCell.setBorderColor(new Color(200, 200, 200));
        tempPdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
    }

    public static String nz(String valore, String seNullo) {
        if (valore == null) {
            return (seNullo);
        }
        return (valore);
    }

    public static String nz(Object valore, String seNullo) {
        if (valore == null) {
            return (seNullo);
        }
        return (valore.toString());
    }

    public String printExcel(String title, int[] headerWidth, String note_testa, String note_piede) {
        Connection connection;
        java.sql.Statement stat;
        ResultSet resu;
        String nomeFileXls = "tempStampa_" + (new java.util.Date()).getTime() + ".xls";

        try {

            HSSFSheet sheet = wb.createSheet("invoicex");
            sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);

            int contarows = 0;
            HSSFRow row = sheet.createRow(contarows);
            contarows++;
            row.createCell(0).setCellValue(title);

            if (note_testa != null && note_testa.length() > 0) {
                row = sheet.createRow(contarows);
                contarows++;
                row = sheet.createRow(contarows);
                contarows++;
                row.createCell(0).setCellValue(note_testa);
            }

            row = sheet.createRow(contarows);
            contarows++;
            //colonne
            row = sheet.createRow(contarows);
            contarows++;
            int columns = 0;
            if (table != null) {
                columns = table.getColumnCount();
            } else {
                columns = rs.getMetaData().getColumnCount();
            }

            if (headerWidth == null && table != null) {
                headerWidth = new int[columns];
                for (int i = 0; i < columns; i++) {
                    headerWidth[i] = table.getColumnModel().getColumn(i).getWidth() / 7;
                }
            }

            for (int i = 0; i < columns; i++) {
                String col = "";
                if (table != null) {
                    col = table.getColumnName(i);
                } else {
                    col = rs.getMetaData().getColumnLabel(i + 1);
                }
                row.createCell(i).setCellValue(col);

                if (headerWidth != null) {
                    sheet.setColumnWidth(i, (headerWidth[i] * 300));
                }
            }

            //righe
            int rowcount = 0;
            if (table != null) {
                rowcount = table.getRowCount();
            } else {
                rs.last();
                rowcount = rs.getRow();
                rs.beforeFirst();
            }
            Map oldrow = new HashMap();
            for (int j = 0; j < rowcount; j++) {

                row = sheet.createRow(contarows);
                contarows++;
                //colonne
                for (int i = 0; i < columns; i++) {
                    //controllo tipo di campo
                    Object o = null;
                    if (table != null) {
                        o = this.table.getValueAt(j, i);
                    } else {
                        rs.absolute(j + 1);
                        o = rs.getObject(i + 1);
                    }
                    boolean non_stampare = false;
                    if (non_stampare_valori_ripetuti && non_stampare_valori_ripetuti_colonna != null) {
                        Object ocol = null;
                        if (table != null) {
                            ocol = this.table.getValueAt(j, non_stampare_valori_ripetuti_colonna);
                        } else {
                            rs.absolute(j + 1);
                            ocol = rs.getObject(non_stampare_valori_ripetuti_colonna + 1);
                        }
                        if (cu.s(ocol).equals(cu.s(oldrow.get(non_stampare_valori_ripetuti_colonna))) && cu.s(o).equals(cu.s(oldrow.get(i)))) {
                            non_stampare = true;
                        }
                    } else if (non_stampare_valori_ripetuti && non_stampare_valori_ripetuti_colonna == null) {
                        if (cu.s(o).equals(cu.s(oldrow.get(i)))) {
                            non_stampare = true;
                        }
                    }

                    if (non_stampare) {
                        row.createCell(i).setCellValue("");
                    } else {
                        setCell(o, row, i);
                    }
                    oldrow.put(i, o);
                }
            }

            if (note_piede != null && note_piede.length() > 0) {
                row = sheet.createRow(contarows);
                contarows++;
                row = sheet.createRow(contarows);
                contarows++;
                row.createCell(0).setCellValue(note_piede);
            }

            FileOutputStream fileOut = new FileOutputStream(nomeFileXls);
            wb.write(fileOut);
            fileOut.close();

            return (nomeFileXls);
        } catch (Exception err) {
            javax.swing.JOptionPane.showMessageDialog(null, err.toString());
            err.printStackTrace();
            return (null);
        }
    }

    public String printCsv(String title, int[] headerWidth, String note_testa, String note_piede) {
        Connection connection;
        java.sql.Statement stat;
        ResultSet resu;

        File dir = new File(System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "Export");
        dir.mkdirs();

        String nomeFileCsv = System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "Export" + File.separator + "export_" + (new java.util.Date()).getTime() + ".csv";

        try {

            CSVPrinter csvp = new CSVPrinter(new FileOutputStream(nomeFileCsv));

            short contarows = 0;
            contarows++;

            int columns = 0;
            if (table != null) {
                columns = table.getColumnCount();
            } else {
                columns = rs.getMetaData().getColumnCount();
            }
            for (int i = 0; i < columns; i++) {
                String col = "";
                if (table != null) {
                    col = table.getColumnName(i);
                } else {
                    col = rs.getMetaData().getColumnLabel(i + 1);
                }
                csvp.print(col);
            }
            csvp.println();
            //righe
            int rowcount = 0;
            if (table != null) {
                rowcount = table.getRowCount();
            } else {
                rs.last();
                rowcount = rs.getRow();
                rs.beforeFirst();
            }
            for (int j = 0; j < rowcount; j++) {
                contarows++;
                //colonne
                for (int i = 0; i < columns; i++) {
                    //controllo tipo di campo
                    Object o = null;
                    if (table != null) {
                        o = this.table.getValueAt(j, i);
                    } else {
                        rs.absolute(j + 1);
                        o = rs.getObject(i + 1);
                    }
                    if (o instanceof Double) {
                        csvp.print(CastUtils.toString(o));
                    } else if (o instanceof BigDecimal) {
                        csvp.print(CastUtils.toString(o));
                    } else if (o instanceof Integer) {
                        csvp.print(CastUtils.toString(o));
                    } else if (o instanceof java.sql.Date) {
                        csvp.print(CastUtils.toString(o));
                    } else if (o instanceof byte[]) {
                        csvp.print(new String((byte[]) o));
                    } else if (o instanceof Long) {
                        csvp.print(CastUtils.toString(o));
                    } else {
                        if (!(o instanceof String)) {
                            if (o != null) {
                                System.out.println(o.getClass());
                            }
                        }
                        csvp.print(nz(o, ""));
                    }
                }
                csvp.println();
            }

            csvp.close();

            return (nomeFileCsv);
        } catch (Exception err) {
            javax.swing.JOptionPane.showMessageDialog(null, err.toString());
            err.printStackTrace();
            return (null);
        }
    }

    public void setCell(Object o, HSSFRow row, int i) {
        if (o instanceof Double) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue((Double) o);
            cell.setCellStyle(style_double);
        } else if (o instanceof BigDecimal) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(((BigDecimal) o).doubleValue());
            cell.setCellStyle(style_double);
        } else if (o instanceof Integer) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(((Integer) o).intValue());
            cell.setCellStyle(style_int);
        } else if (o instanceof java.sql.Date) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(((java.sql.Date) o));
            cell.setCellStyle(style_date);
        } else if (o instanceof byte[]) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(new String((byte[]) o));
            cell.setCellStyle(style_int);
            row.createCell(i).setCellValue(new String((byte[]) o));
        } else if (o instanceof Long) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(((Long) o).longValue());
            cell.setCellStyle(style_int);
        } else {
            if (!(o instanceof String)) {
                if (o != null) {
                    System.out.println(o.getClass());
                }
            }
            row.createCell(i).setCellValue(nz(o, ""));
        }
    }

}
