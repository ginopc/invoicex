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
package gestioneFatture.stats;

import com.caucho.quercus.QuercusEngine;
import com.caucho.quercus.QuercusErrorException;
import com.caucho.quercus.env.QuercusLanguageException;
import java.text.*;

import gestioneFatture.*;
import it.tnx.Db;
import it.tnx.commons.BareBonesBrowserLaunch;
import it.tnx.commons.DebugFastUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.gui.DateDocument;
import it.tnx.invoicex.InvoicexUtil;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

//jasper
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.UIManager;
import net.sf.jasperreports.engine.JasperManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;

//jfreechart
import org.jdesktop.swingworker.SwingWorker;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;

public class frmStatOrdiniBolleFatture extends javax.swing.JInternalFrame {

    String tempdata1 = "";
    String tempdata2 = "";
    String dataPartenza = "";
    String dataArrivo = "";

    public static java.awt.Image createOrdiniBolleFattureGraph(String tipoDoc, String serie) {

        String sql = "";

        java.sql.ResultSet res = null;

        DefaultCategoryDataset cd = new DefaultCategoryDataset();

        //raggruppati per mese
        sql = "select anno, mese, sum(totale_imponibile) as totaleMese";
        sql += " from temp_stampa_stat_ord_bol_fat";
        sql += " where hostname = USER()";
        if (tipoDoc != null) {
            sql += " and tipo_doc = " + Db.pc(tipoDoc, Types.VARCHAR);
        }
        if (serie != null) {
            sql += " and serie = " + Db.pc(serie, Types.VARCHAR);
        }
        sql += " group by anno, mese";
        try {
            res = it.tnx.Db.openResultSet(sql);
            while (res.next()) {
                if (tipoDoc != null) {
                    cd.addValue(res.getDouble("totaleMese"), tipoDoc, res.getString("mese"));
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        //scorro per i tipi di documento
        if (tipoDoc == null) {
            try {
                sql = "select anno, mese, sum(totale_imponibile) as totaleMese";
                sql += " from temp_stampa_stat_ord_bol_fat";
                sql += " where hostname = USER()";
                sql += " and tipo_doc = " + Db.pc("Ordini", Types.VARCHAR);
                sql += " group by anno, mese";
                sql += " order by anno, mese";
                res = it.tnx.Db.openResultSet(sql);
                while (res.next()) {
                    cd.addValue(res.getDouble("totaleMese"), "Ordini", res.getString("mese"));
                }
                sql = "select anno, mese, sum(totale_imponibile) as totaleMese";
                sql += " from temp_stampa_stat_ord_bol_fat";
                sql += " where hostname = USER()";
                sql += " and tipo_doc = " + Db.pc("Bolle", Types.VARCHAR);
                sql += " group by anno, mese";
                sql += " order by anno, mese";
                res = it.tnx.Db.openResultSet(sql);
                while (res.next()) {
                    cd.addValue(res.getDouble("totaleMese"), "Bolle", res.getString("mese"));
                }
                sql = "select anno, mese, sum(totale_imponibile) as totaleMese";
                sql += " from temp_stampa_stat_ord_bol_fat";
                sql += " where hostname = USER()";
                sql += " and tipo_doc = " + Db.pc("Fatture", Types.VARCHAR);
                sql += " group by anno, mese";
                sql += " order by anno, mese";
                res = it.tnx.Db.openResultSet(sql);
                while (res.next()) {
                    cd.addValue(res.getDouble("totaleMese"), "Fatture", res.getString("mese"));
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(null, "Mese", "Totale", cd, org.jfree.chart.plot.PlotOrientation.VERTICAL, true, true, true);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        chart.setBackgroundPaint(new Color(255, 255, 255));
        chart.getCategoryPlot().setBackgroundPaint(new Color(255, 255, 255));
        chart.getCategoryPlot().getDomainAxis().setTickMarksVisible(true);
        return chart.createBufferedImage(800, 400);
//        return chart.createBufferedImage(800, 200);
    }

    public static java.awt.Image createGraphAgenti(String tipoDoc, Integer codice_agente) throws Exception {

        String sql = "";

        java.sql.ResultSet res = null;

        DefaultCategoryDataset cd = new DefaultCategoryDataset();
        TimePeriodValuesCollection timedata = new TimePeriodValuesCollection();

//        for (int i = 0; i < 6; i++) {
//            series.add(new Year(2005 + i), Math.pow(2, i) * scale);
//        }
        Map minmax = dbu.getListMap(Db.getConn(), "select min(data) as mindata, max(data) as maxdata from temp_stampa_stat_ord_bol_fat where hostname = USER()").get(0);
        DebugFastUtils.dump(minmax);
        Date dmin = cu.toDate(minmax.get("mindata"));
        Date dmax = cu.toDate(minmax.get("maxdata"));
        DateUtils.addMonths(dmax, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dmin);

        if (codice_agente != null) {
            //ciclo per i mesi da min a max
            TimePeriodValues series = new TimePeriodValues(cu.s(codice_agente));
            while (cal.getTime().before(dmax)) {
                sql = "select sum(totale_imponibile) as totaleMese";
                sql += " from temp_stampa_stat_ord_bol_fat";
                sql += " where hostname = USER()";
                if (tipoDoc != null) {
                    sql += " and tipo_doc = " + Db.pc(tipoDoc, Types.VARCHAR);
                }
                sql += " and agente_codice = " + Db.pc(codice_agente, Types.INTEGER);
                sql += " and anno = " + cal.get(Calendar.YEAR) + " and mese = " + (cal.get(Calendar.MONTH) + 1);
                try {
                    res = it.tnx.Db.openResultSet(sql);
                    Day day = new Day(1, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
                    if (res.next()) {
                        series.add(day, res.getDouble("totaleMese"));
                    } else {
                        series.add(day, 0);
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                }
                cal.add(Calendar.MONTH, 1);
            }
            if (!series.isEmpty()) {
                timedata.addSeries(series);
            }
        } else {
            //ciclo per tutti gli agenti
            List<Map> listagenti = dbu.getListMap(Db.getConn(), "select agente_codice from temp_stampa_stat_ord_bol_fat group by agente_codice");
            for (Map m : listagenti) {
                try {
                    sql = "select anno, mese, sum(totale_imponibile) as totaleMese";
                    sql += " from temp_stampa_stat_ord_bol_fat";
                    sql += " where hostname = USER()";
                    sql += " and agente_codice = " + Db.pc(m.get("agente_codice"), Types.INTEGER);
                    sql += " group by anno, mese";
                    sql += " order by anno, mese";
                    res = it.tnx.Db.openResultSet(sql);
                    TimePeriodValues series = new TimePeriodValues(cu.s(m.get("codice_agente")));
                    while (res.next()) {
                        //cd.addValue(res.getDouble("totaleMese"), cu.s(m.get("agente_codice")), res.getString("mese"));
                        Day day = new Day(1, res.getInt("mese"), res.getInt("anno"));
                        series.add(day, res.getDouble("totaleMese"));
                    }
                    if (!series.isEmpty()) {
                        timedata.addSeries(series);
                    }
                } catch (Exception err) {
                    SwingUtils.showExceptionMessage(main.getPadreFrame(), err);
                    break;
                }
            }
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(tipoDoc, "Date", "Value", timedata, true, true, false);

        applyChartTheme(chart);

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesVisible(true);
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        currency.setMaximumFractionDigits(0);
        ValueAxis rangeAxis = (ValueAxis) plot.getRangeAxis();
//        rangeAxis.setNumberFormatOverride(currency);        

        DateFormat formatter = new SimpleDateFormat("MMM-yyyy");
        DateTickUnit unit = new DateTickUnit(DateTickUnit.MONTH, 1, formatter);
//        plot.getDomainAxis().setTickUnit(unit);
        DateAxis range = (DateAxis) plot.getDomainAxis();
        range.setTickUnit(unit);

//        JFreeChart chart = ChartFactory.createLineChart(null, "Mese", "Totale", cd, org.jfree.chart.plot.PlotOrientation.VERTICAL, true, true, true);
//        chart.setAntiAlias(true);
//        chart.setTextAntiAlias(true);
//        chart.setBackgroundPaint(new Color(255, 255, 255));
//        chart.getCategoryPlot().setBackgroundPaint(new Color(255, 255, 255));
//        chart.getCategoryPlot().setRenderer(new LineAndShapeRenderer(true, true));
//        applyChartTheme(chart);
////        chart.getCategoryPlot().getDomainAxis().setTickMarksVisible(true);
//        chart.getCategoryPlot().setDomainGridlinesVisible(true);
        if (codice_agente == null) {
            return chart.createBufferedImage((int) (521d * 1.75d), (int) (250d * 1.75d));
        } else {
            return chart.createBufferedImage((int) (521d * 1.75d), (int) (150d * 1.75d));
        }

    }

    public static void applyChartTheme(JFreeChart chart) {
        final StandardChartTheme ct = (StandardChartTheme) org.jfree.chart.StandardChartTheme.createJFreeTheme();

        // The default font used by JFreeChart unable to render Chinese properly.
        // We need to provide font which is able to support Chinese rendering.
        final Font oldExtraLargeFont = ct.getExtraLargeFont();
        final Font oldLargeFont = ct.getLargeFont();
        final Font oldRegularFont = ct.getRegularFont();
//        final Font oldSmallFont = chartTheme.getSmallFont();

        Font labFont = UIManager.getFont("Label.font");

        final Font extraLargeFont = new Font(labFont.getFamily(), oldExtraLargeFont.getStyle(), oldExtraLargeFont.getSize());
        final Font largeFont = new Font(labFont.getFamily(), oldLargeFont.getStyle(), oldLargeFont.getSize());
        final Font regularFont = new Font(labFont.getFamily(), oldRegularFont.getStyle(), oldRegularFont.getSize());
//        final Font smallFont = new Font("Sans-serif", oldSmallFont.getStyle(), oldSmallFont.getSize());

        ct.setExtraLargeFont(extraLargeFont);
        ct.setLargeFont(largeFont);
        ct.setRegularFont(regularFont);
//        chartTheme.setSmallFont(smallFont);

//        chartTheme.setChartBackgroundPaint(new Color(240, 240, 240));
        ct.setPlotBackgroundPaint(new Color(250, 250, 250));
        ct.setGridBandPaint(new Color(200, 0, 0));
//        xyPlot.setRangeGridlinePaint(Color.black);
//        xyPlot.setDomainGridlinePaint(Color.black)

        ct.setRangeGridlinePaint(new Color(200, 200, 200));
        ct.setDomainGridlinePaint(new Color(200, 200, 200));

        ct.apply(chart);
    }

    public static String testReturnString(String i) {
        return "ciao " + i + " ciccio";
    }

    public static String getDescMese(String mese) {
        if (mese.equals("1")) {
            return "Gennaio";
        } else if (mese.equals("2")) {
            return "Febbraio";
        } else if (mese.equals("3")) {
            return "Marzo";
        } else if (mese.equals("4")) {
            return "Aprile";
        } else if (mese.equals("5")) {
            return "Maggio";
        } else if (mese.equals("6")) {
            return "Giugno";
        } else if (mese.equals("7")) {
            return "Luglio";
        } else if (mese.equals("8")) {
            return "Agosto";
        } else if (mese.equals("9")) {
            return "Settembre";
        } else if (mese.equals("10")) {
            return "Ottobre";
        } else if (mese.equals("11")) {
            return "Novembre";
        } else if (mese.equals("12")) {
            return "Dicembre";
        }
        return "";
    }

    public static java.awt.Image testReturnImage(String i) {
        // create a dataset...
        DefaultPieDataset data = new DefaultPieDataset();

        // fill dataset with employeeData
        data.setValue(i, 15);
        data.setValue(i, 20);
        data.setValue(i, 38);

        // create a chart with the dataset
        JFreeChart chart = ChartFactory.createPieChart(i, data, true, true, true);

        // create and return the image
        return chart.createBufferedImage(500, 220);
    }

    /**
     * Creates new form frmStatAgenti
     */
    public frmStatOrdiniBolleFatture() {
        initComponents();

        comRaggr.setVisible(false);

        DateDocument.installDateDocument(dpDal.getEditor());
        DateDocument.installDateDocument(dpAl.getEditor());

        java.util.Date dataInizioAnno = it.tnx.Util.getDateTime("01/01/" + it.tnx.Util.getCurrenteYear());
        dpDal.setDate(dataInizioAnno);
        dpAl.setDate(new Date());

        comCliente.dbAddElement("<tutti i clienti>", "*");
        comCliente.dbOpenList(Db.getConn(), "select ragione_sociale, codice from clie_forn order by ragione_sociale", "*", false);

        comArticolo.dbAddElement("<tutti gli articoli>", "*");
        comArticolo.dbOpenList(Db.getConn(), "select descrizione, codice from articoli order by descrizione", "*", false);

        comAgente.dbAddElement("<tutti gli agenti>", "*");
        comAgente.dbOpenList(Db.getConn(), "select nome, id from agenti order by nome", "*", false);

        //ricarico impo
        try {
            cheDettagli.setSelected(main.fileIni.getValueBoolean("pref", "frmStats_cheDettagli", false));
            comCliente.setSelectedItem(main.fileIni.getValue("pref", "frmStats_comCliente", "<tutti i clienti>"));
            comAgente.setSelectedItem(main.fileIni.getValue("pref", "frmStats_comAgente", "<tutti gli agenti>"));
            comArticolo.setSelectedItem(main.fileIni.getValue("pref", "frmStats_comArticolo", "<tutti gli articoli>"));
            cheQta.setSelected(main.fileIni.getValueBoolean("pref", "frmStats_cheQta", false));
            cheSoloOrdini.setSelected(main.fileIni.getValueBoolean("pref", "frmStats_cheSoloOrdini", true));
            cheOrdini.setSelected(main.fileIni.getValueBoolean("pref", "frmStats_cheOrdini", true));
            cheBolle.setSelected(main.fileIni.getValueBoolean("pref", "frmStats_cheBolle", true));
            cheFatture.setSelected(main.fileIni.getValueBoolean("pref", "frmStats_cheFatture", true));
            cheFattureAcq.setSelected(main.fileIni.getValueBoolean("pref", "frmStats_cheFattureAcq", true));
            comRaggr.setSelectedItem(main.fileIni.getValue("pref", "frmStats_comRaggr", "Nessun raggruppamento"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        butConferma = new javax.swing.JButton();
        cheDettagli = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        comCliente = new tnxbeans.tnxComboField();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        comArticolo = new tnxbeans.tnxComboField();
        cheQta = new javax.swing.JCheckBox();
        cheSoloOrdini = new javax.swing.JCheckBox();
        cheOrdini = new javax.swing.JCheckBox();
        cheBolle = new javax.swing.JCheckBox();
        cheFatture = new javax.swing.JCheckBox();
        cheFattureAcq = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        comRaggr = new javax.swing.JComboBox();
        dpDal = new org.jdesktop.swingx.JXDatePicker();
        dpAl = new org.jdesktop.swingx.JXDatePicker();
        jLabel5 = new javax.swing.JLabel();
        comAgente = new tnxbeans.tnxComboField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Report su Ordini / Bolle / Fatture");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("al");

        butConferma.setText("Anteprima");
        butConferma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butConfermaActionPerformed(evt);
            }
        });

        cheDettagli.setSelected(true);
        cheDettagli.setText("stampa dettaglio singoli documenti");
        cheDettagli.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cheDettagli.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Dal");

        comCliente.setDbDescCampo("");
        comCliente.setDbNomeCampo("");
        comCliente.setDbTipoCampo("");
        comCliente.setDbTrovaMentreScrive(true);
        comCliente.setPreferredSize(new java.awt.Dimension(137, 18));
        comCliente.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comClienteItemStateChanged(evt);
            }
        });
        comCliente.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                comClienteFocusLost(evt);
            }
        });

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Filtra per Cliente/Forn.");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Filtra per Articolo");

        comArticolo.setDbTrovaMentreScrive(true);

        cheQta.setSelected(true);
        cheQta.setText("stampa le quantità");
        cheQta.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cheQta.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        cheSoloOrdini.setSelected(true);
        cheSoloOrdini.setText("conteggia solo gli ordini e ignora i preventivi");
        cheSoloOrdini.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cheSoloOrdini.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        cheOrdini.setText("Ordini");
        cheOrdini.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cheOrdiniActionPerformed(evt);
            }
        });

        cheBolle.setText("Bolle");

        cheFatture.setText("Fatture");

        cheFattureAcq.setText("Fatture di acquisto");

        jLabel3.setText("Incudi");

        comRaggr.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "per Mese" }));

        dpDal.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                dpDalPropertyChange(evt);
            }
        });

        dpAl.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                dpAlPropertyChange(evt);
            }
        });

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Filtra per Agente");

        comAgente.setDbTrovaMentreScrive(true);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(comRaggr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(butConferma))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel1)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(comArticolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 310, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel3)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(cheOrdini)
                                        .add(1, 1, 1)
                                        .add(cheBolle)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(cheFatture)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(cheFattureAcq))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel6)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(dpDal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(jLabel2)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(dpAl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel7)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(comCliente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 310, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(0, 0, Short.MAX_VALUE)))
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(cheDettagli)
                            .add(layout.createSequentialGroup()
                                .add(jLabel5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comAgente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 310, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(cheQta)
                            .add(cheSoloOrdini))
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        layout.linkSize(new java.awt.Component[] {jLabel1, jLabel5, jLabel7}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {comArticolo, comCliente}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(cheOrdini)
                    .add(cheBolle)
                    .add(cheFatture)
                    .add(cheFattureAcq))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jLabel2)
                    .add(dpDal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(dpAl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(comCliente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(comArticolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(comAgente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cheDettagli)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cheQta)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cheSoloOrdini)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 79, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(butConferma)
                    .add(comRaggr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {comArticolo, comCliente}, org.jdesktop.layout.GroupLayout.VERTICAL);

        layout.linkSize(new java.awt.Component[] {jLabel2, jLabel6}, org.jdesktop.layout.GroupLayout.VERTICAL);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void butConfermaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butConfermaActionPerformed
      //salvo impostazioni
      main.fileIni.setValue("pref", "frmStats_cheDettagli", cheDettagli.isSelected());
      main.fileIni.setValue("pref", "frmStats_comCliente", cu.s(comCliente.getSelectedItem()));
      main.fileIni.setValue("pref", "frmStats_comArticolo", cu.s(comArticolo.getSelectedItem()));
      main.fileIni.setValue("pref", "frmStats_comAgente", cu.s(comAgente.getSelectedItem()));
      main.fileIni.setValue("pref", "frmStats_cheQta", cheQta.isSelected());
      main.fileIni.setValue("pref", "frmStats_cheSoloOrdini", cheSoloOrdini.isSelected());
      main.fileIni.setValue("pref", "frmStats_cheOrdini", cheOrdini.isSelected());
      main.fileIni.setValue("pref", "frmStats_cheBolle", cheBolle.isSelected());
      main.fileIni.setValue("pref", "frmStats_cheFatture", cheFatture.isSelected());
      main.fileIni.setValue("pref", "frmStats_cheFattureAcq", cheFattureAcq.isSelected());
      main.fileIni.setValue("pref", "frmStats_comRaggr", cu.s(comRaggr.getSelectedItem()));

      //controllo date
//      if (it.tnx.Checks.isDate(this.texDal.getText()) == false) {
//          javax.swing.JOptionPane.showInternalMessageDialog(this, "La data di partenza non e' valida !", "Attenzione", javax.swing.JOptionPane.WARNING_MESSAGE);
//          return;
//      }
//      if (it.tnx.Checks.isDate(this.texAl.getText()) == false) {
//          javax.swing.JOptionPane.showInternalMessageDialog(this, "La data di arrivo non e' valida !", "Attenzione", javax.swing.JOptionPane.WARNING_MESSAGE);
//          return;
//      }
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

      final frmStatOrdiniBolleFatture _this = this;
      SwingWorker worker = new SwingWorker() {

          @Override
          protected Object doInBackground() throws Exception {
              String sql = "";
              String sqli = "";

              sql = "delete from temp_stampa_stat_ord_bol_fat";
              sql += " where hostname = USER()";
              it.tnx.Db.executeSql(sql);

              java.text.SimpleDateFormat riformattazione = new java.text.SimpleDateFormat("yyyy-MM-dd");
              DateFormat parsaData = new SimpleDateFormat("dd/MM/yy");
              java.util.Date giorno1 = dpDal.getDate();
              dataPartenza = riformattazione.format(giorno1);
              java.util.Date giorno2 = dpAl.getDate();
              dataArrivo = riformattazione.format(giorno2);

              if (cheFatture.isSelected()) {
                  //per fatture
//                  sql = "select sum(round(calcola_importo_netto(r.prezzo * r.quantita, r.sconto1, r.sconto2, t.sconto1, t.sconto2, t.sconto3),2)) "
                  sql = "select sum(round(r.totale_imponibile,2)) "
                          + " + IFNULL(t.spese_trasporto,0) + IFNULL(t.spese_incasso,0) as totaleArticolo, c.ragione_sociale, r.codice_articolo, month(t.data) as mese, t.* "
                          + " , sum(r.quantita) as tot_qta "
                          + " , a.nome as agente_nome "
                          + " , a.id as agente_id "
                          + " from test_fatt t "
                          + " join righ_fatt r on t.id = r.id_padre  "
                          + " join clie_forn c on t.cliente = c.codice "
                          + " left join agenti a on t.agente_codice = a.id ";

                  try {
                      sql += "where t.data between '" + dataPartenza + "' and '" + dataArrivo + "'";

                      if (comCliente.getSelectedIndex() > 0) {
                          sql += " and cliente = '" + Integer.parseInt((String) _this.comCliente.getSelectedKey()) + "'";
                      }

                      if (comArticolo.getSelectedIndex() > 0) {
                          sql += " and r.codice_articolo = '" + comArticolo.getSelectedKey() + "'";
                      }

                      if (comAgente.getSelectedIndex() > 0) {
                          sql += " and t.agente_codice = '" + comAgente.getSelectedKey() + "'";
                      }

                      sql += " and t.tipo_fattura != 6";
                      sql += " and t.tipo_fattura != 7";
                      sql += " group by t.numero, t.serie, t.anno order by t.serie, t.anno, t.numero";
                  } catch (Exception err) {
                      err.printStackTrace();
                  }

                  System.out.println("query report:" + sql);

                  //preparo la tabella di appoggio
                  try {
                      insTabellaTemp(sql, 3, "Fatture");
                  } catch (Exception err) {
                      return err;
                  }
              }

              //scontrini
//                sql = "select sum(round(calcola_importo_netto(r.prezzo * r.quantita, r.sconto1, r.sconto2, t.sconto1, t.sconto2, t.sconto3),2)) "
              sql = "select sum(round(r.totale_imponibile,2)) "
                      + " + IFNULL(t.spese_trasporto,0) + IFNULL(t.spese_incasso,0) as totaleArticolo, c.ragione_sociale, r.codice_articolo, month(t.data) as mese, t.*  "
                      + ", sum(r.quantita) as tot_qta"
                      + " , a.nome as agente_nome "
                      + " , a.id as agente_id "
                      + " from test_fatt t"
                      + " join righ_fatt r on t.id = r.id_padre"
                      + " left join clie_forn c on t.cliente = c.codice "
                      + " left join agenti a on t.agente_codice = a.id ";
              try {
                  sql += "where t.data between '" + dataPartenza + "' and '" + dataArrivo + "'";
                  if (comCliente.getSelectedIndex() > 0) {
                      sql += " and cliente = '" + Integer.parseInt((String) _this.comCliente.getSelectedKey()) + "'";
                  }

                  if (comArticolo.getSelectedIndex() > 0) {
                      sql += " and r.codice_articolo = '" + comArticolo.getSelectedKey() + "'";
                  }
                  sql += " and t.tipo_fattura = 7";
                  sql += " group by t.id order by t.id";
              } catch (Exception err) {
                  err.printStackTrace();
              }
              System.out.println("query report:" + sql);
              //preparo la tabella di appoggio
              try {
                  insTabellaTemp(sql, 4, "Scontrini");
              } catch (Exception err) {
                  err.printStackTrace();
              }

              if (cheOrdini.isSelected()) {
                  //per ordini
//                  sql = "select sum(round(calcola_importo_netto(r.prezzo * r.quantita, r.sconto1, r.sconto2, t.sconto1, t.sconto2, t.sconto3),2)) "
                  sql = "select sum(round(r.totale_imponibile,2)) "
                          + " + IFNULL(t.spese_trasporto,0) + IFNULL(t.spese_incasso,0) as totaleArticolo"
                          + " , c.ragione_sociale, r.codice_articolo, month(t.data) as mese, t.*  "
                          + " , sum(r.quantita) as tot_qta"
                          + " , a.nome as agente_nome "
                          + " , a.id as agente_id "
                          + " from test_ordi t"
                          + " join righ_ordi r on r.id_padre = t.id "
                          + " join clie_forn c on t.cliente = c.codice "
                          + " left join agenti a on t.agente_codice = a.id ";

                  try {
                      sql += "where t.data between '" + dataPartenza + "' and '" + dataArrivo + "'";
                      if (comCliente.getSelectedIndex() > 0) {
                          sql += " and cliente = '" + Integer.parseInt((String) _this.comCliente.getSelectedKey()) + "'";
                      }
                      if (comArticolo.getSelectedIndex() > 0) {
                          sql += " and r.codice_articolo = '" + comArticolo.getSelectedKey() + "'";
                      }
                      if (comAgente.getSelectedIndex() > 0) {
                          sql += " and t.agente_codice = '" + comAgente.getSelectedKey() + "'";
                      }
                      if (cheSoloOrdini.isSelected()) {
                          sql += " and stato_ordine like '%ordine%'";
                      }
                      sql += " group by t.numero, t.serie, t.anno order by t.serie, t.anno, t.numero";
                      System.out.println("sql = " + sql);
                  } catch (Exception err) {
                      err.printStackTrace();
                  }

                  try {
                      insTabellaTemp(sql, 1, "Ordini");
                  } catch (Exception err) {
                      err.printStackTrace();
                  }
              }

              if (cheBolle.isSelected()) {
                  //per bolle
//                  sql = "select sum(round(calcola_importo_netto(r.prezzo * r.quantita, r.sconto1, r.sconto2, t.sconto1, t.sconto2, t.sconto3),2))"
                  sql = "select sum(round(r.totale_imponibile,2)) "
                          + " + IFNULL(t.spese_trasporto,0) + IFNULL(t.spese_incasso,0) as totaleArticolo"
                          + " , c.ragione_sociale, r.codice_articolo, month(t.data) as mese, t.*  "
                          + " , sum(r.quantita) as tot_qta"
                          + " , a.nome as agente_nome "
                          + " , a.id as agente_id "
                          + " from test_ddt t"
                          + " join righ_ddt r on r.id_padre = t.id "
                          + " join clie_forn c on t.cliente = c.codice "
                          + " left join agenti a on t.agente_codice = a.id ";

                  try {
                      sql += "where t.data between '" + dataPartenza + "' and '" + dataArrivo + "'";
                      if (comCliente.getSelectedIndex() > 0) {
                          sql += " and cliente = '" + Integer.parseInt((String) _this.comCliente.getSelectedKey()) + "'";
                      }
                      if (comArticolo.getSelectedIndex() > 0) {
                          sql += " and r.codice_articolo = '" + comArticolo.getSelectedKey() + "'";
                      }
                      if (comAgente.getSelectedIndex() > 0) {
                          sql += " and t.agente_codice = '" + comAgente.getSelectedKey() + "'";
                      }
                      sql += " group by t.numero, t.serie, t.anno order by t.serie, t.anno, t.numero";
                  } catch (Exception err) {
                      err.printStackTrace();
                  }

                  try {
                      insTabellaTemp(sql, 2, "Bolle");
                  } catch (Exception err) {
                      err.printStackTrace();
                  }
              }

              if (cheFattureAcq.isSelected()) {
                  //per fatture di acquisto
//                  sql = "select sum(round(calcola_importo_netto(r.prezzo * r.quantita, r.sconto1, r.sconto2, t.sconto1, t.sconto2, t.sconto3),2)) "
                  sql = "select sum(round(r.totale_imponibile,2)) "
                          + " + IFNULL(t.spese_trasporto,0) + IFNULL(t.spese_incasso,0) as totaleArticolo"
                          + " , c.ragione_sociale, r.codice_articolo, month(t.data) as mese, t.*  "
                          + " , sum(r.quantita) as tot_qta"
                          + " , null as agente_nome "
                          + " , null as agente_id "
                          + " from test_fatt_acquisto t"
                          + " join righ_fatt_acquisto r on r.id_padre = t.id "
                          + " join clie_forn c on t.fornitore = c.codice ";
                  try {
                      sql += "where t.data between '" + dataPartenza + "' and '" + dataArrivo + "'";
                      if (comCliente.getSelectedIndex() > 0) {
                          sql += " and t.fornitore = '" + Integer.parseInt((String) _this.comCliente.getSelectedKey()) + "'";
                      }
                      if (comArticolo.getSelectedIndex() > 0) {
                          sql += " and r.codice_articolo = '" + comArticolo.getSelectedKey() + "'";
                      }
                      sql += " group by t.numero, t.serie, t.anno order by t.serie, t.anno, t.numero";
                  } catch (Exception err) {
                      err.printStackTrace();
                  }

                  try {
                      insTabellaTemp(sql, 10, "Fatture Acquisto");
                  } catch (Exception err) {
                      err.printStackTrace();
                  }
              }

              if (comRaggr.getSelectedItem().equals("per Agente")) {
                  try {
                      QuercusEngine engine = new QuercusEngine();
                      engine.setIni("display_errors", "On");
                      engine.setIni("log_errors", "On");

                      //                engine.setIni("include_path", ".;./;./php/Mustache;./Mustache");
                      ByteArrayOutputStream ba = new ByteArrayOutputStream();

                      engine.setOutputStream(ba);
                      engine.getQuercus().setServerEnv("dal", it.tnx.commons.DateUtils.formatDateIta(dpDal.getDate()));
                      engine.getQuercus().setServerEnv("al", it.tnx.commons.DateUtils.formatDateIta(dpAl.getDate()));
                      engine.getQuercus().setServerEnv("stampa_singoli_documenti", cu.s(cheDettagli.isSelected()));
                      engine.getQuercus().setServerEnv("stampa_quantita", cu.s(cheQta.isSelected()));
                      engine.getQuercus().setServerEnv("tipo", cu.s(comRaggr.getSelectedItem()));

                      engine.executeFile("php/stats.php");

                      IOUtils.copy(new ByteArrayInputStream(ba.toByteArray()), System.out);
                      File fout = new File(System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "tmp" + File.separator + "stats.html");
                      IOUtils.copy(new ByteArrayInputStream(ba.toByteArray()), new FileOutputStream(fout));

                      BareBonesBrowserLaunch.openURL(fout.toURI().toURL().toString());
                  } catch (QuercusLanguageException qe) {
                      qe.printStackTrace();
                      SwingUtils.showExceptionMessage(frmStatOrdiniBolleFatture.this, qe);
                  } catch (QuercusErrorException qe) {
                      qe.printStackTrace();
                      SwingUtils.showExceptionMessage(frmStatOrdiniBolleFatture.this, qe);
                  } catch (Exception e) {
                      e.printStackTrace();
                      SwingUtils.showExceptionMessage(frmStatOrdiniBolleFatture.this, e);
                  }
              } else {
                  //this.gridAgenti.dbOpen(Db.conn, sqlAggiorna);
                  try {
                      //con compilazione
//                      System.out.println("load jrxml");
//                      JasperDesign jasperDesign = JasperManager.loadXmlDesign("reports/stats_monthly_per_agente.jrxml");
//                      //JasperDesign jasperDesign = JasperManager.loadXmlDesign("C:/cvs_tnx/tnx/Invoicex15/src/reports/stats_monthly.jrxml");
//                      System.out.print("compilazione...");
//                      JasperReport jasperReport = JasperManager.compileReport(jasperDesign);
//                      System.out.println("...ok");

                      //senza compilazione
                      System.out.println("load jasper");
                      JasperReport jasperReport = JasperManager.loadReport("reports/stats_monthly.jasper");

                      java.util.Map parameters = new java.util.HashMap();
                      parameters.put("periodo", "Dal " + it.tnx.commons.DateUtils.formatDateIta(dpDal.getDate()) + " al " + it.tnx.commons.DateUtils.formatDateIta(dpAl.getDate()));
                      parameters.put("stampaDettagli", new Boolean(cheDettagli.isSelected()));
                      parameters.put("stampaQta", new Boolean(cheQta.isSelected()));

                      java.sql.Connection conn = it.tnx.Db.getConn();
                      JasperPrint jasperPrint = JasperManager.fillReport(jasperReport, parameters, conn);

                      InvoicexUtil.apriStampa(jasperPrint);
                  } catch (Exception err) {
                      err.printStackTrace();
                  }

              }

              return null;
          }

          @Override
          protected void done() {
              try {
                  _this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                  if (get() instanceof Exception) {
                      SwingUtils.showExceptionMessage(frmStatOrdiniBolleFatture.this, (Exception) get());
                  }
              } catch (InterruptedException ex) {
                  ex.printStackTrace();
              } catch (ExecutionException ex) {
                  ex.printStackTrace();
              }
          }

          private String getSqli(int i, String tipo, ResultSet res) throws SQLException {
              String sqli = "insert into temp_stampa_stat_ord_bol_fat set hostname = USER(), ";
              Map rec = new HashMap();
              rec.put("tipo_doc_ordine", i);
              rec.put("tipo_doc", tipo);
              if (tipo.equalsIgnoreCase("Scontrini")) {
                  rec.put("serie", "");
                  int anno = 0;
                  try {
                      res.getDate("data");
                      Calendar cal = Calendar.getInstance();
                      cal.setTime(res.getDate("data"));
                      anno = cal.get(Calendar.YEAR);
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
                  rec.put("cliente", "scontrino - anonimo");
              } else {
                  rec.put("serie", res.getString("serie"));
                  rec.put("anno", res.getInt("anno"));
                  rec.put("cliente", res.getString("ragione_sociale"));
              }

              rec.put("numero", res.getInt("numero"));
              rec.put("mese", res.getInt("mese"));
              rec.put("data", res.getDate("data"));

              double imp = res.getDouble("totaleArticolo");
              if (tipo.equalsIgnoreCase("Fatture")) {
                  if (res.getInt("tipo_fattura") == dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO) {
                      imp = -(Math.abs(imp));
                  }
              }
              rec.put("totale_imponibile", imp);
              rec.put("qta", res.getDouble("tot_qta"));
              rec.put("agente_codice", res.getInt("agente_id"));
              rec.put("agente_nome", res.getString("agente_nome"));

              sqli = sqli + dbu.prepareSqlFromMap(rec);

              System.out.println("sqli = " + sqli);

              return sqli;
          }

          private void insTabellaTemp(String sql, int tipo, String tipos) throws Exception {
              java.sql.ResultSet res = it.tnx.Db.openResultSet(sql);
              int conta = 0;
              String sqlmulti = "";
              while (res.next()) {
                  conta++;
                  String sqli = getSqli(tipo, tipos, res);
                  sqlmulti += sqli + ";\n";
                  if (conta == 100) {
                      it.tnx.Db.executeSql(sqlmulti);
                      conta = 0;
                      sqlmulti = "";
                  }
              }
              if (conta != 0) {
                  System.out.println("sqlmulti = " + sqlmulti);
                  it.tnx.Db.executeSql(sqlmulti);
              }
          }
      };
      worker.execute();

  }//GEN-LAST:event_butConfermaActionPerformed

private void comClienteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comClienteItemStateChanged
}//GEN-LAST:event_comClienteItemStateChanged

private void comClienteFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comClienteFocusLost
}//GEN-LAST:event_comClienteFocusLost

    private void cheOrdiniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cheOrdiniActionPerformed
        if (cheOrdini.isSelected()) {
            cheSoloOrdini.setEnabled(true);
        } else {
            cheSoloOrdini.setEnabled(false);
        }
    }//GEN-LAST:event_cheOrdiniActionPerformed

    private void dpDalPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dpDalPropertyChange

    }//GEN-LAST:event_dpDalPropertyChange

    private void dpAlPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dpAlPropertyChange

    }//GEN-LAST:event_dpAlPropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butConferma;
    private javax.swing.JCheckBox cheBolle;
    private javax.swing.JCheckBox cheDettagli;
    private javax.swing.JCheckBox cheFatture;
    private javax.swing.JCheckBox cheFattureAcq;
    private javax.swing.JCheckBox cheOrdini;
    private javax.swing.JCheckBox cheQta;
    private javax.swing.JCheckBox cheSoloOrdini;
    private tnxbeans.tnxComboField comAgente;
    private tnxbeans.tnxComboField comArticolo;
    private tnxbeans.tnxComboField comCliente;
    private javax.swing.JComboBox comRaggr;
    private org.jdesktop.swingx.JXDatePicker dpAl;
    private org.jdesktop.swingx.JXDatePicker dpDal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    // End of variables declaration//GEN-END:variables
}
