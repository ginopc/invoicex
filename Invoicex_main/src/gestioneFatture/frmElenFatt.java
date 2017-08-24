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

import it.tnx.Db;
import gestioneFatture.logic.clienti.Cliente;
import gestioneFatture.logic.provvigioni.ProvvigioniFattura;

import it.tnx.accessoUtenti.Permesso;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.FileUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.KeyValuePair;
import it.tnx.commons.MicroBench;
import it.tnx.commons.RunnableWithArgs;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.commons.swing.DelayedExecutor;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.ItextUtil;
import it.tnx.invoicex.gui.JDialogExc;
import it.tnx.invoicex.gui.JDialogJasperViewer;
import it.tnx.proto.LockableCircularBusyPainterUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JDialog;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

//jasper
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignFrame;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.*;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.jxlayer_old.JXLayer;
import org.jdesktop.swingworker.SwingWorker;
import reports.JRDSInvoice;
import reports.JRDSInvoice_lux;
import tnxbeans.tnxDbGrid;

public class frmElenFatt
        extends javax.swing.JInternalFrame {

    private Point mouse_point;

    private String getNomeTabr() {
        return "righ_fatt";
    }

    private void setColsWidth() {
        java.util.Hashtable colsWidthPerc = new java.util.Hashtable();

        colsWidthPerc.put("Tipo", new Double(4));
        colsWidthPerc.put("Serie", new Double(4));
        colsWidthPerc.put("Numero", new Double(4));
        colsWidthPerc.put("Anno", new Double(0));
        colsWidthPerc.put("Data", new Double(9));
        colsWidthPerc.put("Cliente", new Double(19));
        if (main.fileIni.getValueBoolean("pref", "ColAgg_CatCli", false)) {
            colsWidthPerc.put("Categoria Cliente", new Double(10));
        }
        if (main.fileIni.getValueBoolean("pref", "ColAgg_Agente", false)) {
            colsWidthPerc.put("Agente", new Double(10));
        }

        colsWidthPerc.put("Note", new Double(15));
        colsWidthPerc.put("Totale", new Double(15));
        if (main.pluginEmail) {
            colsWidthPerc.put("Mail Inviata", new Double(5));
        }
        if (main.versione.equalsIgnoreCase("base")) {
            colsWidthPerc.put("Allegati", new Double(0));
        } else {
            colsWidthPerc.put("Allegati", new Double(5));
        }

        if (main.fileIni.getValueBoolean("pref", "ColAgg_Anticipata", false)) {
            colsWidthPerc.put("Anticipata", new Double(5));
        }
        if (main.fileIni.getValueBoolean("pref", "ColAgg_AnticipataBanca", false)) {
            colsWidthPerc.put("Banca", new Double(12));
        }

        colsWidthPerc.put("totale_imponibile", new Double(0));
        colsWidthPerc.put("totale_iva", new Double(0));
        colsWidthPerc.put("id", new Double(0));

        if (main.fileIni.getValueBoolean("pref", "ColAgg_RiferimentoCliente", false)) {
            colsWidthPerc.put("Riferimento Cliente", new Double(20));
        }
        if (main.fileIni.getValueBoolean("pref", "ColAgg_Rif", false)) {
            colsWidthPerc.put("Riferimento", new Double(20));
        }
        colsWidthPerc.put("color", new Double(0));

        griglia.columnsSizePerc = colsWidthPerc;
    }

    private void creaPdfEmail(JDialogWait dialog) {
        if (griglia.getSelectedRowCount() <= 0) {
            SwingUtils.showErrorMessage(this, "Seleziona un documento prima!");
            return;
        }
        Integer[] id = new Integer[griglia.getSelectedRowCount()];
        for (int i = 0; i < griglia.getSelectedRowCount(); i++) {
            id[i] = cu.toInteger(griglia.getValueAt(griglia.getSelectedRows()[i], griglia.getColumnByName("id")));
        }
        try {
            InvoicexUtil.creaPdf(Db.TIPO_DOCUMENTO_FATTURA, id, apriDirDopoStampa, false, dialog);
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showExceptionMessage(this, e);
        }
    }

    public static class EmailCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setHorizontalAlignment(SwingConstants.CENTER);
            if (CastUtils.toInteger0(value) > 0) {
                c.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/mail-forward.png")));
            } else {
                c.setIcon(null);
            }
            c.setText("");
            return c;
        }
    }

    public static class AnticipataCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setHorizontalAlignment(SwingConstants.CENTER);
            if (cu.s(value).equalsIgnoreCase("S")) {
                c.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/appointment-new.png")));
            } else {
                c.setIcon(null);
            }
            c.setText("");
            return c;
        }
    }

    public String sqlWhereLimit = "";
    public String sqlWhereDaData = "";
    public String sqlWhereAData = "";
    public String sqlWhereCliente = "";
    private boolean visualizzaTotali = true;
    private boolean apriDirDopoStampa = true;
    long ultimopress = 0;
    boolean scontrini = false;
//    LockableUI lockableUI = new LockableBusyPainterUI();
    LockableCircularBusyPainterUI lockableUI = new LockableCircularBusyPainterUI();
    Lock caricamento = new ReentrantLock();
    DelayedExecutor delay_cliente = new DelayedExecutor(new Runnable() {
        int contatore = 0;

        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    contatore++;
                    System.out.println("*** dbrefresh texCliente.getText(): " + texCliente.getText() + " contatore:" + contatore);
                    SwingUtils.mouse_wait();
                    delay_cliente_update();
                    SwingUtils.mouse_def();
                }

            });
        }
    }, 450);

    List<Map> filters = null;
    RunnableWithArgs filtriActionModifica;
    RunnableWithArgs filtriActionRimuovi;

    private void delay_cliente_update() {
        if (texCliente.getText().trim().length() == 0) {
            sqlWhereCliente = "";
            System.out.println("texCliente len 0 sqlcliente:" + sqlWhereCliente);
        } else {
            sqlWhereCliente = " and clie_forn.ragione_sociale like '%" + Db.aa(texCliente.getText()) + "%'";
            if (main.fileIni.getValueBoolean("pref", "ColAgg_RiferimentoCliente", false)) {
                sqlWhereCliente = " and (clie_forn.ragione_sociale like '%" + Db.aa(texCliente.getText()) + "%'";
                sqlWhereCliente += " or clie_forn.persona_riferimento like '%" + Db.aa(texCliente.getText()) + "%'";
                sqlWhereCliente += ")";
            }
            System.out.println("texCliente len > 0 sqlcliente:" + sqlWhereCliente);
        }
        dbRefresh(false);
    }

    public void frmElenFatt_init(boolean scontrini, final String filtra_cliente) {
        this.scontrini = scontrini;

        initComponents();

        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/document-new-vendita.png")));

        InvoicexUtil.resizePanelFlow(panFiltri);
        filtriActionRimuovi = new RunnableWithArgs() {
            public void run() {
                Object[] objs = getArgs();
                if (objs != null && objs[1] != null) {
                    //rimuovo da filtri
                    if (objs != null && objs[0] != null) {
                        Map m = (Map) objs[0];
                        Object campo = m.get("campo");
                        Iterator<Map> iter = filters.iterator();
                        while (iter.hasNext()) {
                            Map mf = iter.next();
                            if (mf.get("campo").equals(campo)) {
                                filters.remove(mf);
                                break;
                            }
                        }
                    }

                    try {
                        InvoicexUtil.salvaFiltri(filters, frmElenFatt.this.getClass().getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ActionEvent ae = (ActionEvent) objs[1];
                    JComponent source = (JComponent) ae.getSource();
                    panFiltri.remove(source.getParent());
                    panFiltri.getTopLevelAncestor().validate();
                    panFiltri.getTopLevelAncestor().repaint();
                    dbRefresh();
                }
            }
        };
        filtriActionModifica = new RunnableWithArgs() {
            public void run() {
                try {
                    JDialog dialog = InvoicexUtil.getDialogFiltri(frmElenFatt.this, true, false, filters);
                    Object[] objs = getArgs();
                    if (objs != null && objs[0] != null) {
                        dialog.getClass().getDeclaredMethod("posiziona", Object.class).invoke(dialog, objs[0]);
                    }
                    if (objs != null && objs[1] != null) {
                        ActionEvent ae = (ActionEvent) objs[1];
                        JComponent source = (JComponent) ae.getSource();
                        InvoicexUtil.mettiSotto(dialog, source);
                    }
                    dialog.setVisible(true);
                    boolean conferma = dialog.getClass().getField("conferma").getBoolean(dialog);
                    if (conferma) {
                        filters = (List) dialog.getClass().getDeclaredMethod("getFilters").invoke(dialog);
                        InvoicexUtil.aggiornaFiltri(panFiltri, filters, linkAggiungiFiltro, this, filtriActionRimuovi);
                        try {
                            InvoicexUtil.salvaFiltri(filters, frmElenFatt.this.getClass().getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dbRefresh();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        };

        if (main.fileIni.getValueBoolean("pref", "ColAgg_RiferimentoCliente", false)) {
            menColAggRiferimentoCliente.setSelected(true);
        }
        if (main.fileIni.getValueBoolean("pref", "ColAgg_Rif", false)) {
            menColAggRif.setSelected(true);
        }
        if (main.fileIni.getValueBoolean("pref", "ColAgg_CatCli", false)) {
            menColAggCatCli.setSelected(true);
        }
        if (main.fileIni.getValueBoolean("pref", "ColAgg_Agente", false)) {
            menColAggAgente.setSelected(true);
        }
        if (main.fileIni.getValueBoolean("pref", "ColAgg_Anticipata", false)) {
            menColAggAnticipata.setSelected(true);
        }
        if (main.fileIni.getValueBoolean("pref", "ColAgg_AnticipataBanca", false)) {
            menColAggAnticipataBanca.setSelected(true);
        }

        if (!InvoicexUtil.isFunzioniManutenzione()) {
            storico.setVisible(false);
        }

//        griglia.setNoTnxResize(true);
        //ricarico filtri se salvati
        if (filtra_cliente == null) {
            try {
                filters = InvoicexUtil.caricaFiltri(this.getClass().getName());
                InvoicexUtil.aggiornaFiltri(panFiltri, filters, linkAggiungiFiltro, filtriActionModifica, filtriActionRimuovi);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        remove(jPanel8);

        add(jPanel2, BorderLayout.NORTH);
        add(jPanel5, BorderLayout.SOUTH);

//        JXLayer<JComponent> l = new JXLayer<JComponent>(jPanel8, lockableUI);
        JXLayer<JComponent> jxlayer = new JXLayer<JComponent>(panDati, lockableUI);
        jxlayer.setRequestFocusEnabled(false);
        jxlayer.setFocusable(false);
        lockableUI.setLocked(false);

        add(jxlayer, BorderLayout.CENTER);

        if (scontrini) {
            setTitle("Elenco Scontrini");
            butNew.setVisible(false);
            butNew1.setVisible(false);
            butNuovaFatturaAccompagnatoria.setVisible(false);
            butNew2.setVisible(false);
            butEmail.setVisible(false);
            butCreaFatturaDaProforma.setVisible(false);
            butDuplica.setVisible(false);
        }

        //fine
        //apro la griglia
        this.griglia.dbNomeTabella = "test_fatt";
        this.griglia.flagUsaThread = false;

        setColsWidth();

        java.util.Hashtable colsAlign = new java.util.Hashtable();
        colsAlign.put("Totale", "RIGHT_CURRENCY");
        this.griglia.columnsAlign = colsAlign;

//        Vector chiave = new Vector();
//        chiave.add("Serie");
//        chiave.add("Numero");
//        chiave.add("Anno");
        Vector chiave = new Vector();
        chiave.add("id");
        this.griglia.dbChiave = chiave;

        //carico le prefereences utente
        try {
//            java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);
//            this.visualizzaTotali = preferences.getBoolean("visualizzaTotali", true);
            visualizzaTotali = main.fileIni.getValueBoolean("pref", "visualizzaTotali", true);

//            String limit = preferences.get("limit", "50");
            String limit = main.fileIni.getValue("pref", "limit", "50");

            this.texLimit.setText(limit);

            if (main.fileIni.getValueBoolean("pref", "visualizzaAnnoInCorso", false)) {
                texDal.setText(DateUtils.getDateStartYear());
                if (filtra_cliente == null) {
                    texDalFocusLost(null);
                }
            } else if (filtra_cliente == null) {
                texLimitFocusLost(null);
            }

            try {
                InvoicexEvent event = new InvoicexEvent(this);
                event.type = InvoicexEvent.TYPE_FRMELENFATT_CONSTR_POST_INIT_COMPS;
                main.events.fireInvoicexEvent(event);
            } catch (Exception err) {
                err.printStackTrace();
            }

            //dbRefresh();
        } catch (Exception err) {
            err.printStackTrace();
        }

//        this.comCliente.dbAddElement("<tutti i clienti>", "*");
//        this.comCliente.dbOpenList(Db.getConn(), "select ragione_sociale, codice from clie_forn where tipo = 'C' or tipo = 'E' order by ragione_sociale", "*", false);
//        griglia.setRowHeight(25);
//        griglia.getTableHeader().setPreferredSize(new Dimension(griglia.getTableHeader().getWidth(), 30));
        if (main.pluginEmail) {
            griglia.addMouseMotionListener(new MouseMotionAdapter() {

                @Override
                public void mouseMoved(MouseEvent e) {
                    int r = griglia.rowAtPoint(e.getPoint());
                    int c = griglia.columnAtPoint(e.getPoint());
//                    System.out.println("cella a " + r + " " + c);
                    try {
                        if (c == griglia.getColumn("Mail Inviata").getModelIndex() && CastUtils.toInteger0(griglia.getValueAt(r, c)) >= 1) {
                            griglia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        } else {
                            griglia.setCursor(Cursor.getDefaultCursor());
                        }
                    } catch (Exception ex) {
                    }
                }
            });
        }

//        //strano che con la clessidra si perde il focus dalla text
//        KeyboardFocusManager.getCurrentKeyboardFocusManager().addVetoableChangeListener(new VetoableChangeListener() {
//            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
//                if (evt.getPropertyName().equals("focusOwner")) {
//                    System.err.println("evt: " + evt.getPropertyName() + " old:" + (evt.getOldValue() == null ? "-" : evt.getOldValue().getClass()) + " new:" + (evt.getNewValue() == null ? "-" : evt.getNewValue().getClass()) + " source:" + evt.getSource());
//                    //Thread.currentThread().dumpStack();
//                    if (evt.getNewValue() != null) {
//                        try {
//                            JComponent c = (JComponent) evt.getNewValue();
//                            FxUtils.fadeBackground(c, Color.red);
//                            FxUtils.fadeForeground(c, Color.red);
//                        } catch (Exception e) {
//                            //e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        });
        if (!main.utente.getPermesso(Permesso.PERMESSO_FATTURE_VENDITA, Permesso.PERMESSO_TIPO_SCRITTURA)) {
            this.butCreaFatturaDaProforma.setEnabled(false);
            this.butDuplica.setEnabled(false);
            this.butModi.setEnabled(false);
            this.butNew.setEnabled(false);
            this.butNew1.setEnabled(false);
            this.butNew2.setEnabled(false);
            this.butNuovaFatturaAccompagnatoria.setEnabled(false);

            this.menModifica.setEnabled(false);
            this.menDuplica.setEnabled(false);
            this.menConvertiFattProforma.setEnabled(false);
            this.storico.setEnabled(false);
            this.rigeneraProvvigioni.setEnabled(false);
            this.riprendiDaStorico.setEnabled(false);
            this.menColoraRiga.setEnabled(false);
        }

        if (!main.utente.getPermesso(Permesso.PERMESSO_FATTURE_VENDITA, Permesso.PERMESSO_TIPO_CANCELLA)) {
            this.butDele.setEnabled(false);
            this.menElimina.setEnabled(false);
        }

        if (filtra_cliente != null) {
            texCliente.setText(filtra_cliente);
            delay_cliente_update();
        }

    }

    public frmElenFatt() {
        this(false, null);
    }

    public frmElenFatt(boolean scontrini) {
        this(scontrini, null);
    }

    public frmElenFatt(boolean scontrini, String filtra_cliente) {
        try {
            InvoicexEvent event = new InvoicexEvent(this);
            event.type = InvoicexEvent.TYPE_FRMELENFATT_CONSTR_PRE_INIT_COMPS;
            main.events.fireInvoicexEvent(event);
        } catch (Exception err) {
            err.printStackTrace();
        }

        frmElenFatt_init(scontrini, filtra_cliente);
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pop = new javax.swing.JPopupMenu();
        menModifica = new javax.swing.JMenuItem();
        menElimina = new javax.swing.JMenuItem();
        menStampa = new javax.swing.JMenuItem();
        menDuplica = new javax.swing.JMenuItem();
        menPdfEmail = new javax.swing.JMenuItem();
        menExportXmlPa = new javax.swing.JMenuItem();
        menConvertiFattProforma = new javax.swing.JMenuItem();
        menExportCSV = new javax.swing.JMenuItem();
        menSegnalaAnticipata = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        provvigioni = new javax.swing.JMenuItem();
        storico = new javax.swing.JMenuItem();
        rigeneraProvvigioni = new javax.swing.JMenuItem();
        riprendiDaStorico = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        menColAgg = new javax.swing.JMenu();
        menColAggRif = new javax.swing.JCheckBoxMenuItem();
        menColAggRiferimentoCliente = new javax.swing.JCheckBoxMenuItem();
        menColAggCatCli = new javax.swing.JCheckBoxMenuItem();
        menColAggAgente = new javax.swing.JCheckBoxMenuItem();
        menColAggAnticipata = new javax.swing.JCheckBoxMenuItem();
        menColAggAnticipataBanca = new javax.swing.JCheckBoxMenuItem();
        menColoraRiga = new javax.swing.JMenu();
        menColoraBlu = new javax.swing.JMenuItem();
        menColoraBlu2 = new javax.swing.JMenuItem();
        menColoreCeleste = new javax.swing.JMenuItem();
        menColoreCeleste2 = new javax.swing.JMenuItem();
        menColoraVerde = new javax.swing.JMenuItem();
        menColoraVerde2 = new javax.swing.JMenuItem();
        menColoraGiallo = new javax.swing.JMenuItem();
        menColoraGiallo2 = new javax.swing.JMenuItem();
        menColoraArancione = new javax.swing.JMenuItem();
        menColoreArancione2 = new javax.swing.JMenuItem();
        menColoraRosso = new javax.swing.JMenuItem();
        menColoraRosso2 = new javax.swing.JMenuItem();
        menTogliColore = new javax.swing.JMenuItem();
        jPanel8 = new javax.swing.JPanel();
        panDati = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        griglia = new tnxDbGrid() {
            Color color_hover = new Color(200,200,220);
            Color color_sel = new Color(155,155,175);
            Color color_fatt = new Color(255,255,255);
            Color color_pro = new Color(255,255,220);
            Color color_nc = new Color(255,230,230);

            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                Color back = colorForRow(row, c);
                c.setForeground(Color.BLACK);
                if (isRowSelected(row)) {
                    c.setBackground(SwingUtils.mixColours(back, color_sel));
                } else if (row == rollOverRowIndex) {
                    c.setBackground(SwingUtils.mixColours(back, color_hover));
                } else {
                    c.setBackground(back);
                }
                return c;
            }

            protected Color colorForRow(int row, Component c) {
                try {
                    Color cc = InvoicexUtil.getColorePerMarcatura(cu.s(getValueAt(row, getColumnByName("color"))));
                    if(cc != null){
                        return cc;
                    } else if (getValueAt(row, 0).toString().equalsIgnoreCase("NC")) {
                        return color_nc;
                    } else if (getValueAt(row, 0).toString().equalsIgnoreCase("FP")) {
                        return color_pro;
                    } else {
                        return color_fatt;
                    }
                } catch (Exception e) {
                }
                return c.getBackground();
            }
        };
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        butNew = new javax.swing.JButton();
        butNew1 = new javax.swing.JButton();
        butNuovaFatturaAccompagnatoria = new javax.swing.JButton();
        butNew2 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        butModi = new javax.swing.JButton();
        butDele = new javax.swing.JButton();
        butDuplica = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        butPrin = new javax.swing.JButton();
        butEmail = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        butCreaFatturaDaProforma = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        texLimit = new javax.swing.JTextField();
        filtroTipo = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        texDal = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        texAl = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        texCliente = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        butRefresh = new javax.swing.JButton();
        panFiltri = new javax.swing.JPanel();
        linkAggiungiFiltro = new org.jdesktop.swingx.JXHyperlink();
        jPanel5 = new javax.swing.JPanel();
        labTotale = new javax.swing.JLabel();

        pop.setName("popup"); // NOI18N

        menModifica.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/apps/accessories-text-editor.png"))); // NOI18N
        menModifica.setText("Modifica");
        menModifica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menModificaActionPerformed(evt);
            }
        });
        pop.add(menModifica);

        menElimina.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/places/user-trash.png"))); // NOI18N
        menElimina.setText("Elimina");
        menElimina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menEliminaActionPerformed(evt);
            }
        });
        pop.add(menElimina);

        menStampa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-print.png"))); // NOI18N
        menStampa.setText("Stampa");
        menStampa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menStampaActionPerformed(evt);
            }
        });
        pop.add(menStampa);

        menDuplica.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-copy.png"))); // NOI18N
        menDuplica.setText("Duplica");
        menDuplica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menDuplicaActionPerformed(evt);
            }
        });
        pop.add(menDuplica);

        menPdfEmail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/pdf-icon-16.png"))); // NOI18N
        menPdfEmail.setText("Crea Pdf per Email");
        menPdfEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menPdfEmailActionPerformed(evt);
            }
        });
        pop.add(menPdfEmail);

        menExportXmlPa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/logo-ministero16.png"))); // NOI18N
        menExportXmlPa.setText("Export Fattura Elettronica P.A.");
        menExportXmlPa.setToolTipText("Export in Xml per la fatturazione elettronica della Pubblica Amministrazione");
        menExportXmlPa.setName("menu-xml-pa"); // NOI18N
        menExportXmlPa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menExportXmlPaActionPerformed(evt);
            }
        });
        pop.add(menExportXmlPa);

        menConvertiFattProforma.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/emblems/emblem-system.png"))); // NOI18N
        menConvertiFattProforma.setText("Converti da Pro-forma a Fattura");
        menConvertiFattProforma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menConvertiFattProformaActionPerformed(evt);
            }
        });
        pop.add(menConvertiFattProforma);

        menExportCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/media-skip-forward.png"))); // NOI18N
        menExportCSV.setText("Export righe in CSV");
        menExportCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menExportCSVActionPerformed(evt);
            }
        });
        pop.add(menExportCSV);

        menSegnalaAnticipata.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/appointment-new.png"))); // NOI18N
        menSegnalaAnticipata.setText("Segna come anticipata");
        menSegnalaAnticipata.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menSegnalaAnticipataActionPerformed(evt);
            }
        });
        pop.add(menSegnalaAnticipata);
        pop.add(jSeparator3);

        provvigioni.setText("Controlla Provvigioni");
        provvigioni.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                provvigioniActionPerformed(evt);
            }
        });
        pop.add(provvigioni);

        storico.setText("Controlla Storico");
        storico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storicoActionPerformed(evt);
            }
        });
        pop.add(storico);

        rigeneraProvvigioni.setText("Rigenera Provvigioni");
        rigeneraProvvigioni.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rigeneraProvvigioniActionPerformed(evt);
            }
        });
        pop.add(rigeneraProvvigioni);

        riprendiDaStorico.setText("Torna a versione precedente");
        riprendiDaStorico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                riprendiDaStoricoActionPerformed(evt);
            }
        });
        pop.add(riprendiDaStorico);
        pop.add(jSeparator4);

        menColAgg.setText("Colonne Aggiuntive");

        menColAggRif.setText("Riferimento");
        menColAggRif.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColAggRifActionPerformed(evt);
            }
        });
        menColAgg.add(menColAggRif);

        menColAggRiferimentoCliente.setText("Riferimento Cliente");
        menColAggRiferimentoCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColAggRiferimentoClienteActionPerformed(evt);
            }
        });
        menColAgg.add(menColAggRiferimentoCliente);

        menColAggCatCli.setText("Categoria Cliente");
        menColAggCatCli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColAggCatCliActionPerformed(evt);
            }
        });
        menColAgg.add(menColAggCatCli);

        menColAggAgente.setText("Agente");
        menColAggAgente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColAggAgenteActionPerformed(evt);
            }
        });
        menColAgg.add(menColAggAgente);

        menColAggAnticipata.setText("Fattura Anticipata");
        menColAggAnticipata.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColAggAnticipataActionPerformed(evt);
            }
        });
        menColAgg.add(menColAggAnticipata);

        menColAggAnticipataBanca.setText("Banca di anticipazione");
        menColAggAnticipataBanca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColAggAnticipataBancaActionPerformed(evt);
            }
        });
        menColAgg.add(menColAggAnticipataBanca);

        pop.add(menColAgg);

        menColoraRiga.setText("Marca Fattura");

        menColoraBlu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-blu.png"))); // NOI18N
        menColoraBlu.setText("Blu");
        menColoraBlu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoraBluActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoraBlu);

        menColoraBlu2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-blu2.png"))); // NOI18N
        menColoraBlu2.setText("Blu chiaro");
        menColoraBlu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoraBlu2ActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoraBlu2);

        menColoreCeleste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-celeste.png"))); // NOI18N
        menColoreCeleste.setText("Celeste");
        menColoreCeleste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoreCelesteActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoreCeleste);

        menColoreCeleste2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-celeste2.png"))); // NOI18N
        menColoreCeleste2.setText("Celeste chiaro");
        menColoreCeleste2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoreCeleste2ActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoreCeleste2);

        menColoraVerde.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-verde.png"))); // NOI18N
        menColoraVerde.setText("Verde");
        menColoraVerde.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoraVerdeActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoraVerde);

        menColoraVerde2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-verde2.png"))); // NOI18N
        menColoraVerde2.setText("Verde chiaro");
        menColoraVerde2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoraVerde2ActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoraVerde2);

        menColoraGiallo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-giallo.png"))); // NOI18N
        menColoraGiallo.setText("Giallo");
        menColoraGiallo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoraGialloActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoraGiallo);

        menColoraGiallo2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-giallo2.png"))); // NOI18N
        menColoraGiallo2.setText("Giallo chiaro");
        menColoraGiallo2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoraGiallo2ActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoraGiallo2);

        menColoraArancione.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-arancione.png"))); // NOI18N
        menColoraArancione.setText("Arancione");
        menColoraArancione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoraArancioneActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoraArancione);

        menColoreArancione2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-arancione2.png"))); // NOI18N
        menColoreArancione2.setText("Arancione chiaro");
        menColoreArancione2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoreArancione2ActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoreArancione2);

        menColoraRosso.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-rosso.png"))); // NOI18N
        menColoraRosso.setText("Rosso");
        menColoraRosso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoraRossoActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoraRosso);

        menColoraRosso2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/status/color-rosso2.png"))); // NOI18N
        menColoraRosso2.setText("Rosa");
        menColoraRosso2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColoraRosso2ActionPerformed(evt);
            }
        });
        menColoraRiga.add(menColoraRosso2);

        menTogliColore.setText("Togli Colore");
        menTogliColore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menTogliColoreActionPerformed(evt);
            }
        });
        menColoraRiga.add(menTogliColore);

        pop.add(menColoraRiga);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Gestione Fatture di Vendita");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosed(evt);
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameOpened(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        jPanel8.setLayout(new java.awt.BorderLayout());

        panDati.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panDati.setLayout(new java.awt.BorderLayout());

        jScrollPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jScrollPane1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jScrollPane1MouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jScrollPane1MousePressed(evt);
            }
        });

        griglia.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        griglia.setFont(griglia.getFont().deriveFont(griglia.getFont().getSize()+1f));
        griglia.setRowHeight(20);
        griglia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grigliaMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                grigliaMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                grigliaMouseReleased(evt);
            }
        });
        griglia.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                grigliaKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(griglia);

        panDati.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel8.add(panDati, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 2));

        butNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butNew.setText("Nuova Fattura");
        butNew.setName("nuova_fattura"); // NOI18N
        butNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNewActionPerformed(evt);
            }
        });
        jPanel1.add(butNew);

        butNew1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butNew1.setText("Nota di Credito");
        butNew1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNew1ActionPerformed(evt);
            }
        });
        jPanel1.add(butNew1);

        butNuovaFatturaAccompagnatoria.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butNuovaFatturaAccompagnatoria.setText("Fattura Accompagnatoria");
        butNuovaFatturaAccompagnatoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNuovaFatturaAccompagnatoriaActionPerformed(evt);
            }
        });
        jPanel1.add(butNuovaFatturaAccompagnatoria);

        butNew2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butNew2.setText("Fattura Pro-forma");
        butNew2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNew2ActionPerformed(evt);
            }
        });
        jPanel1.add(butNew2);

        jPanel6.add(jPanel1);

        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 2));

        butModi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/apps/accessories-text-editor.png"))); // NOI18N
        butModi.setText("Modifica");
        butModi.setMargin(new java.awt.Insets(2, 4, 2, 4));
        butModi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butModiActionPerformed(evt);
            }
        });
        jPanel7.add(butModi);

        butDele.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/places/user-trash.png"))); // NOI18N
        butDele.setText("Elimina");
        butDele.setToolTipText("Elimina");
        butDele.setMargin(new java.awt.Insets(2, 4, 2, 4));
        butDele.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDeleActionPerformed(evt);
            }
        });
        jPanel7.add(butDele);

        butDuplica.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-copy.png"))); // NOI18N
        butDuplica.setText("Duplica");
        butDuplica.setMargin(new java.awt.Insets(2, 4, 2, 4));
        butDuplica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDuplicaActionPerformed(evt);
            }
        });
        jPanel7.add(butDuplica);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(4, 20));
        jPanel7.add(jSeparator1);

        butPrin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-print.png"))); // NOI18N
        butPrin.setText("Stampa");
        butPrin.setToolTipText("");
        butPrin.setMargin(new java.awt.Insets(2, 4, 2, 4));
        butPrin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPrinActionPerformed(evt);
            }
        });
        jPanel7.add(butPrin);

        butEmail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/pdf-icon-16.png"))); // NOI18N
        butEmail.setText("Crea PDF");
        butEmail.setToolTipText("");
        butEmail.setMargin(new java.awt.Insets(2, 4, 2, 4));
        butEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butEmailActionPerformed(evt);
            }
        });
        jPanel7.add(butEmail);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setPreferredSize(new java.awt.Dimension(4, 20));
        jPanel7.add(jSeparator2);

        butCreaFatturaDaProforma.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/emblems/emblem-system.png"))); // NOI18N
        butCreaFatturaDaProforma.setText("Da Proforma a Fattura");
        butCreaFatturaDaProforma.setMargin(new java.awt.Insets(2, 4, 2, 4));
        butCreaFatturaDaProforma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCreaFatturaDaProformaActionPerformed(evt);
            }
        });
        jPanel7.add(butCreaFatturaDaProforma);

        jPanel6.add(jPanel7);

        jPanel2.add(jPanel6);

        jPanel4.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jPanel4ComponentResized(evt);
            }
        });
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 2));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel1.setText("vis.");
        jPanel4.add(jLabel1);

        texLimit.setColumns(3);
        texLimit.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        texLimit.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texLimitFocusLost(evt);
            }
        });
        texLimit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texLimitKeyPressed(evt);
            }
        });
        jPanel4.add(texLimit);

        filtroTipo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tutti i documenti", "Fatture", "Fatture accompag.", "Fatture pro-forma", "Note di credito" }));
        filtroTipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtroTipoActionPerformed(evt);
            }
        });
        jPanel4.add(filtroTipo);

        jLabel3.setText("|");
        jPanel4.add(jLabel3);

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel4.setText("dal");
        jPanel4.add(jLabel4);

        texDal.setColumns(6);
        texDal.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        texDal.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texDalFocusLost(evt);
            }
        });
        texDal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texDalKeyPressed(evt);
            }
        });
        jPanel4.add(texDal);

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel5.setText("al");
        jPanel4.add(jLabel5);

        texAl.setColumns(6);
        texAl.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        texAl.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texAlFocusLost(evt);
            }
        });
        texAl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texAlKeyPressed(evt);
            }
        });
        jPanel4.add(texAl);

        jLabel6.setText("|");
        jPanel4.add(jLabel6);

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel7.setText("cliente");
        jPanel4.add(jLabel7);

        texCliente.setColumns(10);
        texCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texClienteActionPerformed(evt);
            }
        });
        texCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texClienteKeyReleased(evt);
            }
        });
        jPanel4.add(texCliente);

        jLabel9.setText("|");
        jLabel9.setIconTextGap(2);
        jPanel4.add(jLabel9);

        butRefresh.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        butRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/view-refresh.png"))); // NOI18N
        butRefresh.setToolTipText("Aggiorna l'elenco dei documenti");
        butRefresh.setIconTextGap(2);
        butRefresh.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butRefreshActionPerformed(evt);
            }
        });
        jPanel4.add(butRefresh);

        jPanel2.add(jPanel4);

        panFiltri.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 2));

        linkAggiungiFiltro.setText("aggiungi filtro");
        linkAggiungiFiltro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkAggiungiFiltroActionPerformed(evt);
            }
        });
        panFiltri.add(linkAggiungiFiltro);

        jPanel2.add(panFiltri);

        jPanel8.add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel5.setLayout(new java.awt.BorderLayout(2, 2));

        labTotale.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labTotale.setText("totale documenti visualizzati ");
        labTotale.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jPanel5.add(labTotale, java.awt.BorderLayout.CENTER);

        jPanel8.add(jPanel5, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel8, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void texAlKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texAlKeyPressed

        if (evt.getKeyCode() == evt.VK_ENTER) {
            texAlFocusLost(null);
        }
    }//GEN-LAST:event_texAlKeyPressed

    private void texDalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texDalKeyPressed

        if (evt.getKeyCode() == evt.VK_ENTER) {
            texDalFocusLost(null);
        }
    }//GEN-LAST:event_texDalKeyPressed

    private void texLimitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texLimitKeyPressed

        if (evt.getKeyCode() == evt.VK_ENTER) {
            texLimitFocusLost(null);
        }
    }//GEN-LAST:event_texLimitKeyPressed

    private void butRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butRefreshActionPerformed
        dbRefresh();
    }//GEN-LAST:event_butRefreshActionPerformed

    public void filtraPerCliente() {
        delay_cliente_update();
    }

    private void texAlFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texAlFocusLost

        if (this.texAl.getText().length() == 0) {
            sqlWhereAData = "";
            dbRefresh(false);
        } else if (it.tnx.Checks.isDate(this.texAl.getText())) {
            sqlWhereAData = " and data <= " + Db.pc2(this.texAl.getText(), Types.DATE);
            dbRefresh(false);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Il parametro richiesto deve essere una data in formato (gg/mm/aaaa)", "Attenzione", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_texAlFocusLost

    private void texDalFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texDalFocusLost

        if (this.texDal.getText().length() == 0) {
            sqlWhereDaData = "";
            dbRefresh(false);
        } else if (it.tnx.Checks.isDate(this.texDal.getText())) {
            sqlWhereDaData = " and data >= " + Db.pc2(this.texDal.getText(), Types.DATE);
            dbRefresh(false);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Il parametro richiesto deve essere una data in formato (gg/mm/aaaa)", "Attenzione", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_texDalFocusLost

    private void texLimitFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texLimitFocusLost

        if (this.texLimit.getText().length() == 0) {
            sqlWhereLimit = "";
            dbRefresh(false);
        } else if (it.tnx.Checks.isInteger(this.texLimit.getText())) {
            sqlWhereLimit = " limit " + this.texLimit.getText();
            dbRefresh(false);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Il parametro richiesto deve essere numerico", "Attenzione", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_texLimitFocusLost

    private void grigliaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_grigliaKeyPressed
            }//GEN-LAST:event_grigliaKeyPressed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
            }//GEN-LAST:event_formKeyPressed

    public void CreaPdfPerEmail() {
        this.apriDirDopoStampa = false;
        creaPdfEmail(null);
        this.apriDirDopoStampa = true;
    }

    public void CreaPdfPerEmail(JDialogWait dialog) {
        this.apriDirDopoStampa = false;
        creaPdfEmail(dialog);
        this.apriDirDopoStampa = true;
    }

    private void butEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butEmailActionPerformed
        creaPdfEmail(null);
    }//GEN-LAST:event_butEmailActionPerformed

    private void butNuovaFatturaAccompagnatoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNuovaFatturaAccompagnatoriaActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        //controllo se sono aperti altri frmTestFatt
        if (this.controllaAperFatt() == false) {

            //inserito da lorenzo
            JInternalFrame frm = null;
            frmTestFatt frmTemp = null;
            frmTemp = new frmTestFatt();
            frmTemp.init(frmTestFatt.DB_INSERIMENTO, "", 0, "P", 0, dbFattura.TIPO_FATTURA_ACCOMPAGNATORIA, -1);
            frmTemp.from = this;
            frm = (JInternalFrame) frmTemp;
            if (frmTemp != null && frmTemp.trow != null) {
                SwingUtils.showErrorMessage(main.getPadreWindow(), frmTemp.trow.getMessage());
            } else {
                MenuPanel m = (MenuPanel) main.getPadrePanel();
                m.openFrame(frm, 740, InvoicexUtil.getHeightIntFrame(750));
            }
        }

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_butNuovaFatturaAccompagnatoriaActionPerformed

    private void butNew1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNew1ActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        //controllo se sono aperti altri frmTestFatt
        if (this.controllaAperFatt() == false) {

            frmTestFatt frm = null;
            String serie = "";
            System.out.println("main.fileIni:" + main.fileIni);
            if (main.fileIni.getValueBoolean("pref", "numerazioneNoteCredito", true)) {
                serie = "#";
            } else {
                serie = "";
            }
            frm = new frmTestFatt();
            frm.init(frmTestFatt.DB_INSERIMENTO, serie, 0, "P", 0, dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO, -1);
            frm.from = this;

            MenuPanel m = (MenuPanel) main.getPadrePanel();
            m.openFrame(frm, 740, InvoicexUtil.getHeightIntFrame(750));
        }

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_butNew1ActionPerformed

    private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameOpened

        // Add your handling code here:
        this.griglia.resizeColumnsPerc(true);
    }//GEN-LAST:event_formInternalFrameOpened

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosed

        // Add your handling code here:
        main.getPadre().closeFrame(this);

//        lockableUI.stop();

    }//GEN-LAST:event_formInternalFrameClosed

    private void grigliaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseClicked
        try {
            int r = griglia.rowAtPoint(evt.getPoint());
            int c = griglia.columnAtPoint(evt.getPoint());
            if (griglia.hasColumn("Mail Inviata") && main.pluginEmail && c == griglia.getColumn("Mail Inviata").getModelIndex() && CastUtils.toInteger0(griglia.getValueAt(r, c)) >= 1) {
                HashMap params = new HashMap();
                params.put("source", this);
                params.put("tipo", "Fattura di Vendita");
                params.put("id", griglia.getValueAt(r, griglia.getColumnByName("id")));
                InvoicexEvent event = new InvoicexEvent(params);
                event.type = InvoicexEvent.TYPE_EMAIL_STORICO;
                main.events.fireInvoicexEvent(event);
            } else if (evt.getClickCount() == 2) {
                butModiActionPerformed(null);
            } else {
                //tasto destro
                //if (e.getModifiers()==InputEvent.BUTTON3_MASK) popGrig.show(tabNomi,e.getX(),e.getY());
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }//GEN-LAST:event_grigliaMouseClicked

    private void jScrollPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jScrollPane1MouseClicked
            }//GEN-LAST:event_jScrollPane1MouseClicked

    private void butDeleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDeleActionPerformed
        if (griglia.getSelectedRowCount() <= 0) {
            SwingUtils.showErrorMessage(this, "Seleziona un documento prima!");
            return;
        }
        if (griglia.getSelectedRowCount() > 1) {
            SwingUtils.showErrorMessage(this, "Seleziona un documento per volta");
            return;
        }
        if (griglia.getSelectedRow() < 0) {
            return;
        }

        final Object inizio_mysql = Db.getCurrentTimestamp();

        String sql;
        int dbAnno = 0;

        int dbIdFattura = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"))));
        try {
            if (dbFattura.fatturaBloccata(dbIdFattura, (Integer) dbu.getObject(Db.getConn(), "SELECT tipo_fattura FROM test_fatt WHERE id = " + Db.pc(dbIdFattura, Types.INTEGER), true))) {
                SwingUtils.showErrorMessage(frmElenFatt.this, "Impossibile procedere, la fattura risulta bloccata perchè inclusa in una stampa definitiva del registro iva!\nPuoi vederne il contenuto con il pulsante di Stampa");
                return;
            }
        } catch (Exception ex) {
            Logger.getLogger(frmElenFatt.class.getName()).log(Level.SEVERE, null, ex);
        }

        //controllo se il documento puo' essere eliminato
        //lo puo' solo se ?? l'ultimo, altrimenti cambia la numerazione!
        if (!scontrini) {
            try {
                dbAnno = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), 3)));

                int numeroSelezionato = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("numero"))));
                int numeroInDb = 0;

                int id = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("numero"))));

                sql = "select numero from test_fatt";
                sql += " where serie = '" + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("serie")) + "'";
                sql += " and anno = " + dbAnno;
                sql += " and tipo_fattura != 7"; //scontini
                sql += " order by numero desc";
                ResultSet tempNumero = Db.openResultSet(sql);
                tempNumero.next();
                numeroInDb = tempNumero.getInt(1);

                if (numeroInDb != numeroSelezionato) {
                    java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);
                    //                boolean azioniPericolose = preferences.getBoolean("azioniPericolose", false);
                    boolean azioniPericolose = main.fileIni.getValueBoolean("pref", "azioniPericolose", true);
                    if (!azioniPericolose) {
                        javax.swing.JOptionPane.showMessageDialog(this, "Il documento non puo' essere eliminato", "Attenzione", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        return;
                    } else {
                        int ret = JOptionPane.showConfirmDialog(this, "Il documento non andrebbe eliminato per la progressione della numerazione, sei sicuro di eliminarlo ?", "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION);
                        if (ret == JOptionPane.NO_OPTION) {
                            return;
                        }
                    }
                }

                String sqlDdt = "SELECT id FROM test_ddt WHERE ";
                sqlDdt += "";
                sqlDdt += "fattura_anno = " + Db.pc(dbAnno, Types.INTEGER);

            } catch (Exception err) {
                err.printStackTrace();
            }
        }

        String grigliaSerie = String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("serie")));
        int numSel = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("numero"))));
        String grigliaAnno = String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("anno")));
        String check = "";
        if (!grigliaSerie.equals("")) {
            check += grigliaSerie + "/" + numSel;
        } else {
            check += "" + numSel;
        }
        check = StringUtils.replace(check, "*", "\\*");

        // Controllo provenienza da Ordine
        sql = "select da_ordi from righ_fatt where IFNULL(da_ordi, 0) != 0 and id_padre = " + dbIdFattura + " group by da_ordi";
        boolean fromOtherDocOrdi = false;
        List<Map> da_ordi = null;
        try {
            da_ordi = DbUtils.getListMap(Db.getConn(), sql);
            if (da_ordi.size() > 0) {
                if (!SwingUtils.showYesNoMessage(this, "Questo documento proviene da un Ordine.\nContinuare con la cancellazione ?")) {
                    return;
                } else {
                    fromOtherDocOrdi = true;
                }
            } else {
                //no da ordi
            }
        } catch (Exception e) {
        }

        // Controllo provenienza da DDT
        boolean da_ddt_check = false;
        sql = "select da_ddt from righ_fatt where IFNULL(da_ddt, 0) != 0 and id_padre = " + dbIdFattura + " group by da_ddt";
        boolean fromOtherDocDdt = false;
        List<Map> da_ddt = null;
        try {
            da_ddt = DbUtils.getListMap(Db.getConn(), sql);
            if (da_ddt.size() > 0) {
                da_ddt_check = true;
            } else {
                //no da ddt
            }
        } catch (Exception e) {
        }
        List<String> da_ddt_raggr = null;
        if (!da_ddt_check) {
            //altro controllo per ddt raggruppati
            try {
                String da_ddt_raggr_s = cu.s(dbu.getObject(Db.getConn(), "select da_ddt_raggr from test_fatt where id = " + dbu.sql(dbIdFattura)));
                if (StringUtils.isNotBlank(da_ddt_raggr_s)) {
                    da_ddt_raggr = new ArrayList();
                    da_ddt_raggr.addAll(Arrays.asList(StringUtils.split(da_ddt_raggr_s, ",")));
                    da_ddt_check = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (da_ddt_check) {
            if (!SwingUtils.showYesNoMessage(this, "Questo documento proviene da un DDT.\nContinuare con la cancellazione ?")) {
                return;
            } else {
                fromOtherDocDdt = true;
            }
        }

        //chiedo conferma per eliminare il documento
        String msg = null;
        if (scontrini) {
            if (griglia.getSelectedRowCount() > 1) {
                msg = "Sicuro di eliminare gli scontrini ?";
            } else {
                msg = "Sicuro di eliminare lo scontrino ?";
            }
        } else {
            msg = "Sicuro di eliminare la fattura ?";
        }

        dbAnno = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), 3)));

        int res = JOptionPane.showConfirmDialog(this, msg);

        if (res == JOptionPane.OK_OPTION) {
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            if (!scontrini) {
                try {
                    String serie = CastUtils.toString(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("serie")));
                    String numero = CastUtils.toString(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("numero")));
                    String anno = CastUtils.toString(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("anno")));
                    String numerocompleto = serie + "/" + numero + "/" + anno;
                    InvoicexUtil.storicizza("elimina fattura id:" + dbIdFattura + " numero:" + numerocompleto, "fattura", CastUtils.toInteger(griglia.getColumnByName("id")), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Ripristino situazione ordini
                if (fromOtherDocOrdi) {
                    try {
                        for (Map rec : da_ordi) {
                            sql = "select id, convertito from test_ordi where id = " + rec.get("da_ordi");
                            List<Map> ordi = DbUtils.getListMap(Db.getConn(), sql);
                            if (ordi.size() > 0) {
                                Map recordi = ordi.get(0);
                                String convertito = CastUtils.toString(recordi.get("convertito"));
                                convertito = convertito.replaceAll("Fatt. " + check + "\n", "");
                                convertito = convertito.replaceAll("\nFatt. " + check + "$", "");
                                convertito = convertito.replaceAll("Fatt. " + check + "$", "");

                                sql = "UPDATE test_ordi SET ";
                                if (convertito.equals("")) {
                                    sql += "convertito = NULL";
                                    sql += ", evaso = '', doc_tipo = NULL, doc_serie = NULL, doc_numero = NULL, doc_anno = NULL";
                                } else {
                                    sql += "convertito = '" + convertito + "'";
                                    sql += ", evaso = 'P', doc_numero = '" + convertito.substring(convertito.lastIndexOf(" ")) + "'";
                                }
                                sql += " WHERE id = '" + CastUtils.toString(recordi.get("id")) + "' ";
                                // TOLGO I RIFERIMENTI DALL'ORDINE
                                Db.executeSql(sql);

                                sql = "UPDATE righ_ordi rord LEFT JOIN righ_fatt rfat ON rfat.da_ordi_riga = rord.id ";
                                sql += "SET rord.quantita_evasa = rord.quantita_evasa - rfat.quantita ";
                                sql += "WHERE rfat.da_ordi = " + Db.pc(CastUtils.toString(recordi.get("id")), Types.INTEGER);
                                sql += " AND rfat.id_padre = '" + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id")) + "'";
                                // TOLGO I RIFERIMENTI DALLE RIGHE DELL'ORDINE
                                Db.executeSql(sql);
                            }
                        }
                    } catch (SQLException ex) {
                        JDialogExc dialog = new JDialogExc(main.getPadreWindow(), true, ex);
                        dialog.setLocationRelativeTo(null);
                        dialog.setVisible(true);
                        return;
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                }

                // Riprsitno situazione DDT
                if (fromOtherDocDdt) {
                    try {
                        for (Map rec : da_ddt) {
                            sql = "select id, convertito from test_ddt where id = " + rec.get("da_ddt");
                            List<Map> ddt = DbUtils.getListMap(Db.getConn(), sql);
                            if (ddt.size() > 0) {
                                Map recddt = ddt.get(0);
                                String convertito = CastUtils.toString(recddt.get("convertito"));
                                InvoicexUtil.aggiornaConvertito(check, "test_ddt", convertito, cu.i(recddt.get("id")));

                                sql = "UPDATE righ_fatt rfat LEFT JOIN righ_ddt rddt ON rfat.da_ddt_riga = rddt.id ";
                                sql += "SET rddt.quantita_evasa = rddt.quantita_evasa - rfat.quantita ";
                                sql += "WHERE rfat.da_ddt = " + Db.pc(CastUtils.toString(recddt.get("id")), Types.INTEGER);
                                sql += " AND rfat.id_padre = '" + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id")) + "'";
                                // TOLGO I RIFERIMENTI DALLE RIGHE DELL'ORDINE
                                Db.executeSql(sql);
                            }
                        }
                        //per i ddt raggruppati
                        if (da_ddt_raggr != null) {
                            for (String id_da_ddt : da_ddt_raggr) {
                                sql = "select id, convertito from test_ddt where id = " + id_da_ddt;
                                List<Map> ddt = DbUtils.getListMap(Db.getConn(), sql);
                                if (ddt.size() > 0) {
                                    Map recddt = ddt.get(0);
                                    String convertito = CastUtils.toString(recddt.get("convertito"));
                                    InvoicexUtil.aggiornaConvertito(check, "test_ddt", convertito, cu.i(recddt.get("id")));

                                    //azzero quantità evasa da ddt perchè convertendo con raggruppamento l'evasione è completa
                                    sql = "UPDATE righ_ddt ";
                                    sql += " SET quantita_evasa = 0";
                                    sql += " WHERE id_padre = " + id_da_ddt;
                                    Db.executeSql(sql);
                                }
                            }
                        }
                    } catch (SQLException ex) {
                        JDialogExc dialog = new JDialogExc(main.getPadreWindow(), true, ex);
                        dialog.setLocationRelativeTo(null);
                        dialog.setVisible(true);
                        return;
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                }

                //elimino matricole (id_padre fa riferimento alla testa del ddt
                sql = "delete from " + getNomeTabr() + "_matricole";
                sql += " where id_padre = '" + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id")) + "'";
                System.out.println("sql = " + sql);
                Db.executeSql(sql);
                //elimina anche dai lotti (id_padre fa riferimento alla riga del ddt)
                sql = "delete rl.*";
                sql += " from " + getNomeTabr() + "_lotti rl INNER JOIN " + getNomeTabr() + " r ON rl.id_padre = r.id";
                sql += " where r.id_padre = '" + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id")) + "'";
                System.out.println("sql = " + sql);
                Db.executeSql(sql);

                //elimino le righe per vecchia chiave
                sql = "delete from " + getNomeTabr();
                sql += " where serie = '" + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("serie")) + "'";
                sql += " and numero = '" + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("numero")) + "'";
                sql += " and anno = " + dbAnno;
                Db.executeSql(sql);
                //elimino anche con id_padre
                sql = "delete from " + getNomeTabr();
                sql += " where id_padre = '" + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id")) + "'";
                Db.executeSql(sql);

                //elimino provvigioni
                sql = "delete from provvigioni where id_doc = " + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"));
                System.out.println("elimino provvigioni = " + sql);
                try {
                    dbu.tryExecQuery(Db.getConn(), sql);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //elimino scadenze
                //prima i pgamenti parziali eventuali
                sql = "delete sp.* from test_fatt tf \n"
                        + " join scadenze s on s.documento_tipo = '" + Db.TIPO_DOCUMENTO_FATTURA + "' \n"
                        + " and s.id_doc = tf.id \n"
                        + " join scadenze_parziali sp on sp.id_scadenza = s.id \n"
                        + " where tf.id = " + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"));
                System.out.println("elimino scadenze parziali = " + sql);
                try {
                    dbu.tryExecQuery(Db.getConn(), sql);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //ed elimino scadenze
                sql = "delete s.* from test_fatt tf \n"
                        + " join scadenze s on s.documento_tipo = '" + Db.TIPO_DOCUMENTO_FATTURA + "' \n"
                        + " and s.id_doc = tf.id \n"
                        + " where tf.id = " + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"));
                System.out.println("elimino scadenze parziali = " + sql);
                try {
                    dbu.tryExecQuery(Db.getConn(), sql);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //memorizzo gli eliminati
                sql = " where da_tabella = 'test_fatt'";
                sql += " and da_id = '" + griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id")) + "'";
                main.magazzino.preDelete(sql);
                //elimino eventuali movimenti precedenti derivanti dallo stesso documento
                sql = "delete from movimenti_magazzino " + sql;
                Db.executeSql(sql);

                //debug
                //JOptionPane.showMessageDialog(this,sql);
                //debug
                Db.executeSql(sql);

                main.events.fireInvoicexEventMagazzino(this, inizio_mysql);

                try {
                    InvoicexEvent event = new InvoicexEvent(frmElenFatt.this);
                    event.type = InvoicexEvent.TYPE_FRMELENFATT_POST_DELETE;
                    event.id = cu.i(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id")));
                    main.events.fireInvoicexEvent(event);
                    frmElenFatt.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    return;
                } catch (Exception err) {
                    err.printStackTrace();
                } finally {
                    this.griglia.dbDelete();
                }
            } else {
                //scontrini
                final int[] rows = griglia.getSelectedRows();

                Thread t = new Thread() {

                    @Override
                    public void run() {
                        for (int i = rows.length - 1; i >= 0; i--) {
                            final int final_i = i;
                            griglia.getSelectionModel().setSelectionInterval(rows[final_i], rows[final_i]);

                            int dbIdFattura = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"))));
                            int dbNumFattura = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("numero"))));

                            // INSERIRE QUI QUERY PER IL RIPRISTINO DEI VALORI DA FATTURARE O GIA' FATTURATI
                            // IN DOCUMENTO DI PARTENZA
                            String sql = "delete from righ_fatt";
                            sql += " where id_padre = " + dbIdFattura;
                            Db.executeSql(sql);

                            sql = "delete from test_fatt";
                            sql += " where id = " + dbIdFattura;
                            Db.executeSql(sql);

                            sql = " where da_tabella = 'test_fatt'";
                            sql += " and da_id = " + dbIdFattura;
                            main.magazzino.preDelete(sql);
                            sql = "delete from movimenti_magazzino " + sql;
                            Db.executeSql(sql);
                        }

                        main.events.fireInvoicexEventMagazzino(this, inizio_mysql);

                        dbRefresh();
                    }
                };
                t.start();

            }

            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_butDeleActionPerformed

    private void butPrinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPrinActionPerformed

        System.out.println("fileIni param prop:" + main.fileIni.realFileName);

        if (griglia.getSelectedRowCount() <= 0) {
            SwingUtils.showErrorMessage(this, "Seleziona un documento prima!");
            return;
        }

        main.loadIni();

        //controllo scontrini
        System.out.println(griglia.getValueAt(griglia.getSelectedRow(), 0));
        if (griglia.getValueAt(griglia.getSelectedRow(), 0).equals("SC")) {
            butModiActionPerformed(null);
            return;
        }

        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        if (griglia.getSelectedRowCount() > 1) {
            SwingWorker work = new SwingWorker() {

                @Override
                protected Object doInBackground() throws Exception {
                    ArrayList files = new ArrayList();
                    for (int i : griglia.getSelectedRows()) {
                        System.out.println("stampa:" + i);
                        final String tipoFattura = String.valueOf(griglia.getValueAt(i, 0));
                        final String dbSerie = String.valueOf(griglia.getValueAt(i, 1));
                        final int dbNumero = Integer.parseInt(String.valueOf(griglia.getValueAt(i, 2)));
                        final int dbAnno = Integer.parseInt(String.valueOf(griglia.getValueAt(i, 3)));
                        final int dbId = Integer.parseInt(String.valueOf(griglia.getValueAt(i, griglia.getColumnByName("id"))));

                        try {
                            InvoicexUtil.aggiornaTotaliRighe(Db.TIPO_DOCUMENTO_FATTURA, dbId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Object ret = stampa(tipoFattura, dbSerie, dbNumero, dbAnno, true, true, false, dbId);
                        files.add(ret);
                    }
                    //concateno i pdf e li visualizzo
                    String out = System.getProperty("user.home") + "/stampa.pdf";
                    ItextUtil.concatenate(out, (String[]) files.toArray(new String[files.size()]));

//test a5 in a4                    
/*
                     try {
                     Document document = new Document(PageSize.A4.rotate());
                     PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("c:\\temp\\test_a5.pdf"));
                     document.open();

                     PdfReader reader = new PdfReader(out);
                     int pagecount = reader.getNumberOfPages();
                     System.out.println("pagecount = " + pagecount);

                     PdfTemplate page1 = writer.getImportedPage(reader, 1);
                     PdfTemplate page2 = writer.getImportedPage(reader, 2);
                        
                     PdfContentByte content = writer.getDirectContent();
                        
                     //// in PDF, "0, 0" is the lower left corner. 
                     //                        document.newPage();
                        
                     content.addTemplate(page1, 0, 0);
                     content.addTemplate(page2, page2.getWidth(), 0);

                     document.close();

                     } catch (Exception e) {
                     e.printStackTrace();
                     }
                     */
                    Util.start(out);
                    return null;
                }
            };
            work.execute();
        } else {
            int i = griglia.getSelectedRow();
            final String tipoFattura = String.valueOf(griglia.getValueAt(i, 0));
            final String dbSerie = String.valueOf(griglia.getValueAt(i, 1));
            final int dbNumero = Integer.parseInt(String.valueOf(griglia.getValueAt(i, 2)));
            final int dbAnno = Integer.parseInt(String.valueOf(griglia.getValueAt(i, 3)));
            final int dbId = Integer.parseInt(String.valueOf(griglia.getValueAt(i, griglia.getColumnByName("id"))));

            try {
                InvoicexUtil.aggiornaTotaliRighe(Db.TIPO_DOCUMENTO_FATTURA, dbId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            stampa(tipoFattura, dbSerie, dbNumero, dbAnno, false, false, false, dbId);
        }

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_butPrinActionPerformed

    static public void stampa(final String tipoFattura, final String dbSerie, final int dbNumero, final int dbAnno, final Integer id) {
        stampa(tipoFattura, dbSerie, dbNumero, dbAnno, false, false, false, id);
    }

    static public Object stampa(final String tipoFattura, final String dbSerie, final int dbNumero, final int dbAnno, final boolean generazionePdfDaJasper, final boolean attendi, final boolean booleanPerEmail, final Integer id) {
        Object ret = null;

        if (main.getPersonalContain(main.PERSONAL_LUXURY)) {
            int ret1 = JOptionPane.showConfirmDialog(main.getPadreWindow(), "Vuoi stampare con lo sfondo nero ?", "Attenzione", 0);
            if (ret1 == 0) {
                main.luxStampaNera = true;
            } else {
                main.luxStampaNera = false;
            }

            int ret2 = JOptionPane.showConfirmDialog(main.getPadreWindow(), "Vuoi stampare in Dollari ?", "Attenzione", 0);
            if (ret2 == 0) {
                main.luxStampaValuta = "$";
            } else {
//                main.luxStampaValuta = "?";
                main.luxStampaValuta = "\u20ac";
            }

            main.luxProforma = false;
            ret2 = JOptionPane.showConfirmDialog(main.getPadreWindow(), "Vuoi stampare come PRO-FORMA ?", "Attenzione", 0);
            if (ret2 == 0) {
                main.luxProforma = true;
            }

        }

        //tipoFattura
        String paramTipoStampa = "tipoStampa";
        if (tipoFattura.equalsIgnoreCase("FA")) {
            paramTipoStampa = "tipoStampaFA";
        }
        String tempts = "";
        if (!StringUtils.isBlank(main.fileIni.getValue("stampe", paramTipoStampa))) {
            tempts = main.fileIni.getValue("stampe", paramTipoStampa);
        } else {
            tempts = main.fileIni.getValue("pref", paramTipoStampa, "");
        }
        final String prefTipoStampa = tempts;

        //controllo bolli e numero report
        final File freport = new File(main.wd + Reports.DIR_REPORTS + Reports.DIR_FATTURE + prefTipoStampa);
        try {
            if (!main.isBatch) {
                boolean bollo_presente = cu.toBoolean(dbu.getObject(Db.getConn(), "select bollo_presente from test_fatt where id = " + id, false));
                String nomefile = freport.getName();
                int ver_report = cu.i(StringUtils.substringBefore(StringUtils.substringAfterLast(nomefile, "_mod"), "_"));
                if (bollo_presente && (ver_report < 7)) {
                    SwingUtils.showErrorMessage(main.getPadreFrame(), "Attenzione, in fattura è presente il bollo ma hai un report che non lo supporta\nVai in Utilità->Impostazioni->Avanzate e seleziona il report fattura_mod7_default.jrxml o superiore", "Attenzione");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        //salvo img logo in db percè le stampe la caricono da db invece che da file per integrazione con client manager
//        InvoicexUtil.salvaLogoInDb(main.fileIni.getValue("varie", "percorso_logo_stampe"));
        //nuovo tipo di stampa
        System.out.println("prefTipoStampa:" + prefTipoStampa);
        if (prefTipoStampa.endsWith(".jrxml")) {
            SwingWorker work = new SwingWorker() {

                protected Object doInBackground() throws Exception {
                    JDialog dialog = null;
                    Object ret = null;
                    try {
                        if (!main.isBatch) {
                            dialog = new JDialogCompilazioneReport();
                            dialog.setLocationRelativeTo(null);
                            dialog.setVisible(true);
                        }

                        System.out.println("stampa worker 1");
                        JasperReport rep = Reports.getReport(freport);
                        System.out.println("stampa worker 2");

                        //controllo modifiche al report
                        String suffisso = "";
                        try {
                            if (!main.getPersonalContain("noCheckScontiStampa")) {
                                if (!controllaScontoPresente(dbSerie, dbNumero, dbAnno)) {
                                    suffisso += "_nosconto";
                                }
                            }
                        } catch (Exception e) {
                        }
                        //logo
                        if (InvoicexUtil.controllaPosizioneLogoSuffisso().length() > 0) {
                            suffisso += InvoicexUtil.controllaPosizioneLogoSuffisso();
                        }
                        //luxury
                        if ((main.getPersonalContain(main.PERSONAL_LUXURY)) && (main.luxStampaNera)) {
                            suffisso += "_nera";
                        }
                        if (suffisso.length() == 0) {
                            //no elaborazioni
                            suffisso += ".jasper";
                        } else {
                            suffisso += "_gen_invoicex.jasper";
                        }

                        //controllo se già presente
                        String newFile = freport.getAbsolutePath() + suffisso;
                        File newFileFile = new File(newFile);
                        boolean ricompilare = true;
                        if (newFileFile.exists() && newFileFile.lastModified() >= freport.lastModified()) {
                            ricompilare = false;
                            try {
                                rep = JasperManager.loadReport(newFile);
                            } catch (JRException jrerr) {
                                jrerr.printStackTrace();
                                ricompilare = true;
                            }
                        }

                        System.out.println("stampa worker 3");
                        JasperDesign repdes = JRXmlLoader.load(freport);
                        System.out.println("stampa worker 4");

                        if (ricompilare) {
                            //sconto...
                            try {
                                if (!main.getPersonalContain("noCheckScontiStampa")) {
                                    if (!controllaScontoPresente(dbSerie, dbNumero, dbAnno)) {
                                        repdes = controllaSconto(freport, rep, repdes);    //rimuove la colonna sconti
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //logo
                            try {
                                repdes = InvoicexUtil.controllaLogo(freport, rep, repdes);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if ((main.getPersonalContain(main.PERSONAL_LUXURY)) && (main.luxStampaNera)) {
                                try {
                                    repdes = controllaStampaLuxury(freport, rep, repdes);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            //salvo per debug report design generato
                            System.out.println("newFile = " + newFile);
                            
//                            JasperDesignViewer.viewReportDesign(repdes);
//                            JasperCompileManager.writeReportToXmlFile(repdes, "c:\\temp\\test.jrxml");

                            System.out.println("stampa worker 5");
                            JasperCompileManager.compileReportToFile(repdes, newFile);
                            rep = JasperManager.loadReport(newFile);
                        }

                        System.out.println("stampa worker 6");
                        //eventuali personalizzazioni
                        try {
                            HashMap params = new HashMap();
                            params.put("source", this);
                            params.put("freport", freport);
                            params.put("rep", rep);
                            params.put("repdes", repdes);
                            InvoicexEvent event = new InvoicexEvent(params);
                            event.type = InvoicexEvent.TYPE_PREPARA_JASPER;
                            HashMap reth = (HashMap) main.events.fireInvoicexEventWResult(event);
                            if (reth != null) {
                                rep = (JasperReport) reth.get("rep");
                            }
                        } catch (Exception err) {
                            err.printStackTrace();
                        }

                        java.util.Map params = new java.util.HashMap();
                        JRDSInvoice jrInvoice = null;
                        System.out.println("stampa worker 7");

                        MicroBench mb0 = new MicroBench();
                        mb0.start();
                        if (main.getPersonalContain(main.PERSONAL_LUXURY)) {
                            jrInvoice = new JRDSInvoice_lux(Db.getConn(), dbSerie, dbNumero, dbAnno, booleanPerEmail);
                        } else {
                            jrInvoice = new JRDSInvoice(Db.getConn(), dbSerie, dbNumero, dbAnno, booleanPerEmail, id, freport);
                        }
                        mb0.out("init jrinvoice");

                        //etichette lingua
                        boolean italian = false;
//                        Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);
//                        if (preferences.getBoolean("soloItaliano", true)) {
                        if (main.fileIni.getValueBoolean("pref", "soloItaliano", true)) {
                            italian = true;
                        } else if (jrInvoice.codiceCliente != null) {
                            Cliente cliente = new Cliente(jrInvoice.codiceCliente);
                            italian = cliente.isItalian();
                        }

                        ResourceBundle rb = null;
                        if (italian) {
                            rb = ResourceBundle.getBundle("gestioneFatture/print/labels");
                            params.put("lang", "it");
                        } else {
                            rb = ResourceBundle.getBundle("gestioneFatture/print/labels", java.util.Locale.UK);
                            params.put("lang", "en");
                        }
                        for (Enumeration e = rb.getKeys(); e.hasMoreElements();) {
                            String k = (String) e.nextElement();
                            params.put("e_" + k, rb.getString(k));
                        }

                        params.put("myds", jrInvoice);
                        if (tipoFattura.equalsIgnoreCase("NC")) {
                            params.put("FATTURA_NOTA", "Nota di Credito");
                        }

                        params.put("stampaPivaSotto", main.fileIni.getValueBoolean("pref", "stampaPivaSotto", false));

                        System.out.println("stampa worker 8");
                        MicroBench mb = new MicroBench();
                        mb.start();
                        JasperPrint print = JasperManager.fillReport(rep, params, jrInvoice);
                        mb.out("fill report fatture");

                        if (generazionePdfDaJasper) {
                            System.out.println("stampa worker 9");
                            //File fd = new File("tempEmail/documento_" + tipoFattura + "_" + dbSerie + String.valueOf(dbNumero) + "_" + jrInvoice.nomeClienteFile + ".pdf");
//                            String nomeFile = "documento_" + tipoFattura + "_" + dbSerie + String.valueOf(dbNumero) + "_" + jrInvoice.nomeClienteFile + ".pdf";
//                            nomeFile = "tempEmail/" + FileUtils.normalizeFileName(nomeFile);
                            String nomeFile = "tempEmail/" + InvoicexUtil.getNomeFileDoc(Db.TIPO_DOCUMENTO_FATTURA, dbSerie, cu.s(dbNumero), jrInvoice.nomeClienteFile, !italian, tipoFattura) + ".pdf";

                            File fd = new File(main.wd + nomeFile);
                            try {
                                fd.getParentFile().mkdirs();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String nomeFilePdf = fd.getAbsolutePath();
                            try {
                                if (!fd.createNewFile()) {
//                                    nomeFile = "documento_" + tipoFattura + "_" + dbSerie + String.valueOf(dbNumero) + "_" + jrInvoice.nomeClienteFile + "_" + FormatUtils.zeroFill(CastUtils.toInteger0(Math.random() * 10000), 5) + ".pdf";
//                                    nomeFile = "tempEmail/" + FileUtils.normalizeFileName(nomeFile);
                                    nomeFile = "tempEmail/" + InvoicexUtil.getNomeFileDoc(Db.TIPO_DOCUMENTO_FATTURA, dbSerie, cu.s(dbNumero), jrInvoice.nomeClienteFile + "_" + FormatUtils.zeroFill(CastUtils.toInteger0(Math.random() * 10000), 5), !italian, tipoFattura) + ".pdf";
                                    fd = new File(main.wd + nomeFile);
                                    nomeFilePdf = fd.getAbsolutePath();
                                }
                            } catch (Exception ex) {
                            }
                            System.out.println("stampa worker 10");

                            JasperExportManager.exportReportToPdfFile(print, nomeFilePdf);
                            System.out.println("stampa worker 11");
                            ret = nomeFilePdf;
                        } else {
                            System.out.println("stampa worker 12");
//                            if (preferences.getBoolean("stampaPdf", false)) {
                            if (main.fileIni.getValueBoolean("pref", "stampaPdf", false)) {
                                String nomeFilePdf = main.wd + "tempPrnFatt.pdf";
                                File fd = new File(nomeFilePdf);
                                try {
                                    if (!fd.createNewFile()) {
                                        nomeFilePdf = main.wd + "tempPrnFatt_" + FormatUtils.zeroFill(CastUtils.toInteger0(Math.random() * 10000), 5) + ".pdf";
                                    }
                                } catch (Exception ex) {
                                }
                                JasperExportManager.exportReportToPdfFile(print, nomeFilePdf);

//provo compressione itext ma diventa più grande !
//                                PdfReader reader = new PdfReader(new FileInputStream(nomeFilePdf));
//                                PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(nomeFilePdf + "2"));
//                                int total = reader.getNumberOfPages() + 1;
//                                for ( int i=1; i<total; i++) {
//                                   reader.setPageContent(i + 1, reader.getPageContent(i + 1));
//                                }
//                                stamper.setFullCompression();
//                                stamper.close();                                
//provato compressed ma cambia di pochissimo                                
//                                JRPdfExporter exporter = new JRPdfExporter();
//                                exporter.setParameter(JRPdfExporterParameter.IS_COMPRESSED, true);
//                                exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
//                                exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, nomeFilePdf);
//                                exporter.exportReport();
//                                SwingUtils.open(new File(nomeFilePdf));
                                Util.start2(nomeFilePdf);
                            } else {
//                                JasperViewer.viewReport(print, false);
                                final JasperPrint printer = print;
                                Thread t = new Thread(new Runnable() {

                                    public void run() {
                                        JDialogJasperViewer viewr = new JDialogJasperViewer(main.getPadreWindow(), true, printer);
                                        viewr.setTitle("Anteprima di stampa");
                                        viewr.setLocationRelativeTo(null);
                                        viewr.setVisible(true);
                                    }
                                });

                                t.start();
                            }
                        }

                        if (main.fileIni.getValueBoolean("pref", "stampaCedoliniBonifici", false)) {

                            //controllo che se non c'è abbinato iban beneficario non stampo cedolino
                            //dati_azienda_banche - id_conto da paga
                            String sqltestiban = "select dab.cc_iban\n"
                                    + "from test_fatt tf \n"
                                    + "left join pagamenti p on tf.pagamento = p.codice\n"
                                    + "left join dati_azienda_banche dab on p.id_conto = dab.id\n"
                                    + "where tf.id = " + id;
                            try {
                                String ibancc = cu.s(dbu.getObject(Db.getConn(), sqltestiban));
                                if (StringUtils.isNotBlank(ibancc)) {
                                    File freportced = new File(main.wd + Reports.DIR_REPORTS + "cedolino_bonifico.jrxml");

                                    JasperReport repced = Reports.getReport(freportced);
                                    HashMap paramsced = new HashMap();
                                    paramsced.put("serie", dbSerie);
                                    paramsced.put("numero", dbNumero);
                                    paramsced.put("anno", dbAnno);
                                    paramsced.put("stampaPivaSotto", main.fileIni.getValueBoolean("pref", "stampaPivaSotto", false));
                                    JasperPrint printced = JasperManager.fillReport(repced, paramsced, Db.getConn());

                                    if (generazionePdfDaJasper) {
                                        //File fd = new File("tempEmail/documento_" + tipoFattura + "_" + dbSerie + String.valueOf(dbNumero) + "_" + jrInvoice.nomeClienteFile + ".pdf");
                                        String nomeFile = "cedolino_bonifico_" + dbSerie + String.valueOf(dbNumero) + "_" + jrInvoice.nomeClienteFile + ".pdf";
                                        nomeFile = "tempEmail/" + FileUtils.normalizeFileName(nomeFile);
                                        File fd = new File(main.wd + nomeFile);
                                        String nomeFilePdf = fd.getAbsolutePath();
                                        JasperExportManager.exportReportToPdfFile(printced, nomeFilePdf);
                                        ret = nomeFilePdf;
                                    } else if (main.fileIni.getValueBoolean("pref", "stampaPdf", false)) {
                                        String nomeFilePdf = main.wd + "tempPrnFatt_cedolino.pdf";
                                        JasperExportManager.exportReportToPdfFile(printced, nomeFilePdf);
                                        Util.start2(nomeFilePdf);
                                    } else {
                                        JDialogJasperViewer viewr = new JDialogJasperViewer(main.getPadreWindow(), true, printced);
                                        viewr.setTitle("Anteprima di stampa");
                                        viewr.setLocationRelativeTo(null);
                                        viewr.setVisible(true);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    } catch (Exception exc) {
                        return exc;
                    } finally {
                        if (!main.isBatch) {
                            dialog.setVisible(false);
                        }
                    }

//                    main.getPadre().toFront();
                    return ret;
                }

                @Override
                protected void done() {
                    try {
                        if (get() instanceof Exception) {
                            if (!main.isBatch) {
                                SwingUtils.showExceptionMessage(null, (Exception)get());
                            } else {
                                ((Exception)get()).printStackTrace();
                            }
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (ExecutionException ex) {
                        ex.printStackTrace();
                    }
                }

                private JasperDesign controllaSconto(File freport, JasperReport rep, JasperDesign repdes) throws JRException {
                    JRDesignBand details = (JRDesignBand) repdes.getDetail();

//                        DebugUtils.dump(details.getElements());
                    int xsconti = 0;
                    int xdescrizione = 0;
                    int wsconti = 0;

                    //rimuovo campo sconti
                    for (JRElement el : details.getElements()) {
                        if (el instanceof JRTextField) {
                            JRTextField tf = (JRTextField) el;
                            if (tf.getExpression().getText().equalsIgnoreCase("$F{sconti}")) {
                                xsconti = tf.getX();
                                wsconti = tf.getWidth();
                                details.removeElement((JRDesignElement) tf);
                                break;
                            }
                        }

                        if (el instanceof JRDesignFrame) {
                            JRDesignFrame frame = (JRDesignFrame) el;
                            for (JRElement el2 : frame.getElements()) {
                                if (el2 instanceof JRTextField) {
                                    JRTextField tf = (JRTextField) el2;
                                    if (tf.getExpression().getText().equalsIgnoreCase("$F{sconti}")) {
                                        xsconti = tf.getX() + el.getX();
                                        wsconti = tf.getWidth();
                                        details.removeElement((JRDesignElement) tf);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    //rimuovo linea separazione sconti
                    for (JRElement el : details.getElements()) {
                        if (el instanceof JRLine) {
                            JRLine l = (JRLine) el;
                            if (l.getX() > xsconti) {
                                details.removeElement((JRDesignElement) l);
                                break;
                            }
                        } else if (el instanceof JRDesignFrame) {
                            JRDesignFrame frame = (JRDesignFrame) el;
                            for (JRElement el2 : frame.getElements()) {
                                if (el2 instanceof JRLine) {
                                    JRLine l = (JRLine) el2;
                                    if ((l.getX() + el.getX()) > xsconti) {
                                        frame.removeElement((JRDesignElement) l);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    //allargo descrizione
                    for (JRElement el : details.getElements()) {
                        if (el instanceof JRTextField) {
                            JRTextField tf = (JRTextField) el;
                            if (tf.getExpression().getText().equalsIgnoreCase("$F{descrizione}")) {
                                xdescrizione = tf.getX();
                                tf.setWidth(tf.getWidth() + wsconti);
                                break;
                            }
                        } else if (el instanceof JRDesignFrame) {
                            JRDesignFrame frame = (JRDesignFrame) el;
                            if (frame.getKey().equals("frameStandard")) {
                                for (JRElement el2 : frame.getElements()) {
                                    if (el2 instanceof JRTextField) {
                                        JRTextField tf = (JRTextField) el2;
                                        if (tf.getExpression().getText().equalsIgnoreCase("$F{descrizione}")) {
                                            xdescrizione = tf.getX() + el.getX();
                                            tf.setWidth(tf.getWidth() + wsconti);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //sposto campi e linee dopo descrizione
                    for (JRElement el : details.getElements()) {
                        if (el instanceof JRDesignElement) {
                            JRDesignElement de = (JRDesignElement) el;
                            if (de.getX() > xdescrizione && de.getX() < xsconti) {
                                System.out.println("sposto de: " + de.getKey() + " a nuova x:" + (de.getX() + wsconti));
                                de.setX(de.getX() + wsconti);
                            }
                        }
                        if (el instanceof JRDesignFrame) {
                            JRDesignFrame frame = (JRDesignFrame) el;
                            System.out.println("frame:" + frame.getKey());
                            for (JRElement el2 : frame.getElements()) {
                                if (el2 instanceof JRDesignElement) {
                                    JRDesignElement de = (JRDesignElement) el2;
                                    if ((de.getX() + el.getX()) > xdescrizione && (de.getX() + el.getX()) < xsconti) {
                                        System.out.println("sposto de: " + de);
                                        de.setX(de.getX() + wsconti);
                                    }
                                }
                            }
                        }
                    }
                    //intestazione
                    JRDesignBand header = (JRDesignBand) repdes.getPageHeader();
                    //rimuovo campo sconti
                    for (JRElement el : header.getElements()) {
                        if (el instanceof JRDesignStaticText) {
                            JRDesignStaticText st = (JRDesignStaticText) el;
                            if (st.getText().equalsIgnoreCase("Sconti") || st.getText().equalsIgnoreCase("Sconto") || st.getText().equalsIgnoreCase("Sc. %")) {
                                xsconti = st.getX();
                                header.removeElement((JRDesignElement) st);
                            }
                        }
                        if (el instanceof JRTextField) {
                            JRTextField tf = (JRTextField) el;
                            if (tf.getExpression().getText().equalsIgnoreCase("$F{descrizione}")) {
                                xdescrizione = tf.getX();
                                tf.setWidth(tf.getWidth() + wsconti);
                                break;
                            }
                        } else if (el instanceof JRTextField) {
                            JRTextField tf = (JRTextField) el;
                            System.out.println("campo: " + tf.getExpression().getText());
                            if (tf.getExpression().getText().equalsIgnoreCase("$P{e_Sconti}")) {
                                xsconti = tf.getX();
                                header.removeElement((JRDesignElement) tf);
                            }
                        }
                        if (el instanceof JRDesignFrame) {
                            JRDesignFrame frame = (JRDesignFrame) el;
                            for (JRElement el2 : frame.getElements()) {
                                if (el2 instanceof JRDesignStaticText) {
                                    JRDesignStaticText st = (JRDesignStaticText) el2;
                                    if (st.getText().equalsIgnoreCase("Sconti") || st.getText().equalsIgnoreCase("Sconto") || st.getText().equalsIgnoreCase("Sc. %")) {
                                        xsconti = st.getX() + el.getX();
                                        frame.removeElement((JRDesignElement) st);
                                        break;
                                    }
                                } else if (el2 instanceof JRTextField) {
                                    JRTextField tf = (JRTextField) el2;
                                    System.out.println("campo: " + tf.getExpression().getText());
                                    if (tf.getExpression().getText().equalsIgnoreCase("$P{e_Sconti}")) {
                                        xsconti = tf.getX() + el.getX();
                                        frame.removeElement((JRDesignElement) tf);
                                        break;
                                    }
                                }
                            }
                        }

                    }
                    //sposto campi e linee dopo descrizione
                    for (JRElement el : header.getElements()) {
                        if (el instanceof JRDesignStaticText) {
                            JRDesignStaticText de = (JRDesignStaticText) el;
                            System.out.println(de.getText() + " x:" + de.getX() + "> xdescrizione:" + xdescrizione + " < xsconti:" + xsconti + " y:" + de.getY() + " > " + (header.getHeight() - 30));
                            if (de.getX() > xdescrizione && de.getX() < xsconti && de.getY() > header.getHeight() - 30) {
                                de.setX(de.getX() + wsconti);
//                                de.setForecolor(Color.RED);
                            }
                        }
                        if (el instanceof JRDesignFrame) {
                            JRDesignFrame frame = (JRDesignFrame) el;
                            System.out.println("frame:" + frame.getKey());
                            for (JRElement el2 : frame.getElements()) {
                                if (el2 instanceof JRDesignStaticText) {
                                    JRDesignStaticText de = (JRDesignStaticText) el2;
                                    if ((de.getX() + el.getX()) > xdescrizione && (de.getX() + el.getX()) < xsconti && frame.getY() > header.getHeight() - 30) {
                                        System.out.println("sposto de: " + de.getKey() + " a nuova x:" + (de.getX() + wsconti));
                                        de.setX(de.getX() + wsconti);
//                                        de.setForecolor(Color.RED);
                                    }
                                } else if (el2 instanceof JRTextField) {
                                    JRTextField de = (JRTextField) el2;
                                    if (!de.getExpression().getText().equals("$F{note}")) {
                                        if ((de.getX() + el.getX()) > xdescrizione && (de.getX() + el.getX()) < xsconti && frame.getY() >= header.getHeight() - frame.getHeight()) {
                                            System.out.println("sposto de: " + de.getKey() + " a nuova x:" + (de.getX() + wsconti));
                                            de.setX(de.getX() + wsconti);
//                                            de.setForecolor(Color.RED);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return repdes;
                }

                private boolean controllaScontoPresente(String dbSerie, int dbNumero, int dbAnno) throws SQLException {
                    String sql = "";
                    sql += "select count(*) from righ_fatt";
                    Integer lid = id;
                    if (lid == null) {
                        lid = InvoicexUtil.getIdFattura(dbSerie, dbNumero, dbAnno);
                    }
                    if (lid != null) {
                        sql += " where id_padre = " + lid;
                    } else {
                        System.out.println("non trovato id per " + dbSerie + "/" + dbNumero + "/" + dbAnno);
                    }
                    sql += " and ((sconto1 is not null and sconto1 != 0)";
                    sql += " or (sconto2 is not null and sconto2 != 0))";
                    ResultSet r = Db.openResultSet(sql);
                    if (r.next()) {
                        if (r.getInt(1) > 0) {
                            return true;
                        }
                    }
                    return false;
                }

                private JasperDesign controllaStampaLuxury(File freport, JasperReport rep, JasperDesign repdes)
                        throws JRException {

                    ArrayList<JRElement> listel = scanJR(repdes);

                    for (JRElement el : listel) {
                        if (el instanceof JRLine) {
                            JRLine ce = (JRLine) el;
                            ce.setForecolor(new Color(200, 200, 140));
                        }
                        if (el instanceof JRRectangle) {
                            JRRectangle ce = (JRRectangle) el;
                            ce.setForecolor(new Color(200, 200, 140));
                        }
                        if (el instanceof JRTextElement) {
                            JRTextElement ce = (JRTextElement) el;
                            ce.setForecolor(new Color(220, 220, 220));
                        }
                    }

                    return repdes;
                }

                private ArrayList<JRElement> scanJR(JasperDesign repdes) {
                    ArrayList l = new ArrayList();
                    l.addAll(scanJR2(repdes.getPageHeader()));
                    l.addAll(scanJR2(repdes.getDetail()));
                    l.addAll(scanJR2(repdes.getPageFooter()));
                    l.addAll(scanJR2(repdes.getLastPageFooter()));
                    l.addAll(scanJR2(repdes.getBackground()));
                    return l;
                }

                private ArrayList scanJR2(JRBand band) {
                    ArrayList l = new ArrayList();
                    for (JRElement el : band.getElements()) {
                        if (el instanceof JRFrame) {
                            JRFrame frame = (JRFrame) el;
                            for (JRElement el2 : frame.getElements()) {
                                l.add(el2);
                            }
                        } else {
                            l.add(el);
                        }
                    }
                    return l;
                }
                
            };

            work.execute();
            if (attendi) {
                try {
                    ret = work.get();
                } catch (Exception e) {
                }
                System.out.println("get " + work + " : " + ret);
            }
        }

        return ret;
    }

    public void publicbutModiActionPerformed() {
        butModiActionPerformed(null);
    }

    private void butModiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butModiActionPerformed
        if (main.utente.getPermesso(Permesso.PERMESSO_FATTURE_VENDITA, Permesso.PERMESSO_TIPO_SCRITTURA)) {
            InvoicexUtil.attendiCaricamentoPluginRitenute(new Runnable() {

                public void run() {
                    if (griglia.getSelectedRowCount() <= 0) {
                        SwingUtils.showErrorMessage(frmElenFatt.this, "Seleziona un documento prima!");
                        return;
                    }

                    //controllo se sono aperti altri frmTestFatt
                    //if (this.controllaAperFatt() == false) {
                    final String dbSerie = String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), 1));
                    final int dbNumero = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), 2)));
                    final int dbAnno = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), 3)));
                    final int dbIdFattura = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"))));
                    final String numerocompleto = dbSerie + "/" + dbNumero + "/" + dbAnno;

                    try {
                        if (dbFattura.fatturaBloccata(dbIdFattura, (Integer) dbu.getObject(Db.getConn(), "SELECT tipo_fattura FROM test_fatt WHERE id = " + Db.pc(dbIdFattura, Types.INTEGER), true))) {
                            SwingUtils.showErrorMessage(frmElenFatt.this, "Impossibile procedere, la fattura risulta bloccata perchè inclusa in una stampa definitiva del registro iva!\nPuoi vederne il contenuto con il pulsante di Stampa");
                            return;
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(frmElenFatt.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    //controllo se è già aperta andare su quella
                    List<JInternalFrame> frames = InvoicexUtil.getFrames(frmTestFatt.class);
                    for (JInternalFrame iframe : frames) {
                        frmTestFatt f = (frmTestFatt) iframe;
                        if (f.id != null && f.id.equals(dbIdFattura)) {
                            System.out.println("trovata form già aperta");
                            main.getPadre().getDesktopPane().getDesktopManager().activateFrame(f);
                            return;
                        }
                    }

                    if (!InvoicexUtil.checkLock(
                            "test_fatt", dbIdFattura, true, frmElenFatt.this)) {
                        return;
                    }

                    System.out.println("wait cursor");
                    frmElenFatt.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    frmElenFatt.this.griglia.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                    SwingWorker worker1 = new SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            frmTestFatt testFatt = null;

                            InvoicexUtil.storicizza("modifica fattura id:" + dbIdFattura + " numero:" + numerocompleto, "fattura", dbIdFattura, false);

                            try {
                                InvoicexUtil.aggiornaTotaliRighe(Db.TIPO_DOCUMENTO_FATTURA, dbIdFattura);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            return null;
                        }

                        @Override
                        protected void done() {
                            frmTestFatt testFatt = null;

                            //controllo che non sia gia' stata stampata o che non ci siano scadenze gi? in distinta
                            String sql = "";

                            try {

                                Statement stat = Db.getConn().createStatement();
                                sql = "select stampato from test_fatt";
                                //            sql += " where serie = " + Db.pc(dbSerie, Types.VARCHAR);
                                //            sql += " and numero = " + Db.pc(dbNumero, Types.INTEGER);
                                //            sql += " and anno = " + Db.pc(dbAnno, Types.INTEGER);
                                sql += " where id = " + dbIdFattura;

                                ResultSet resu = stat.executeQuery(sql);

                                if (resu.next() == true) {

                                    java.util.GregorianCalendar cal = new java.util.GregorianCalendar();

                                    if (resu.getDate("stampato") != null) {
                                        cal.setTime(resu.getDate("stampato"));

                                        if (cal.get(cal.YEAR) >= 2000) {

                                            String msg = "La fattura e' gia' stata stampata, Sicuro di proseguire ?";
                                            int ret = javax.swing.JOptionPane.showConfirmDialog(null, msg, "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE);

                                            if (ret == javax.swing.JOptionPane.NO_OPTION) {
                                                frmElenFatt.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                                frmElenFatt.this.griglia.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                                return;
                                            }
                                        }
                                    }
                                }

                                sql = "select id, data_scadenza, importo, distinta from scadenze";
                                sql += " where documento_tipo = " + Db.pc(Db.TIPO_DOCUMENTO_FATTURA, Types.VARCHAR);
                                sql += " and id_doc = " + dbIdFattura;
                                sql += " and distinta is not null";
                                resu = stat.executeQuery(sql);

                                if (resu.next() == true) {
                                    //storico
                                    String msg = "La fattura e' legata ad una o piu' scadenze gia' stampate in distinta\nSicuro di proseguire ?";
                                    int ret = javax.swing.JOptionPane.showConfirmDialog(null, msg, "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION);

                                    if (ret == javax.swing.JOptionPane.NO_OPTION) {
                                        frmElenFatt.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                        frmElenFatt.this.griglia.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                        return;
                                    }
                                }
                            } catch (Exception err) {
                                err.printStackTrace();
                                javax.swing.JOptionPane.showMessageDialog(frmElenFatt.this, "Errore:" + err.toString(), "Attenzione", javax.swing.JOptionPane.ERROR_MESSAGE);
                            }

                            //inserito da lorenzo
                            javax.swing.JInternalFrame frm = null;

                            try {
                                sql = "select tipo_fattura from test_fatt";
                                sql += "  where id = " + dbIdFattura;

                                ResultSet resSceltaTipo = Db.openResultSet(sql);
                                if (!resSceltaTipo.next()) {
                                    SwingUtils.showInfoMessage(frmElenFatt.this, "Impossibile trovare il documento");
                                    frmElenFatt.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                    return;
                                }

                                String tempSceltaTipo = Db.nz(resSceltaTipo.getString("tipo_fattura"), String.valueOf(dbFattura.TIPO_FATTURA_IMMEDIATA));

                                String tipo = String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("tipo")));

                                if (!tipo.equals("SC")) {
                                    testFatt = new frmTestFatt();
                                    testFatt.init(frmTestDocu.DB_MODIFICA, dbSerie, dbNumero, "P", dbAnno, dbFattura.TIPO_FATTURA_NON_IDENTIFICATA, dbIdFattura);
                                    testFatt.from = frmElenFatt.this;
                                    frm = (JInternalFrame) testFatt;
                                } else {
                                    try {
                                        InvoicexEvent event = new InvoicexEvent(frmElenFatt.this);
                                        event.type = InvoicexEvent.TYPE_FRMELENFATT_OPEN_FORM_SCONTRINO;
                                        main.events.fireInvoicexEvent(event);
                                        frmElenFatt.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                        frmElenFatt.this.griglia.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                        return;
                                    } catch (Exception err) {
                                        err.printStackTrace();
                                    }
                                }

                            } catch (Exception err) {
                                err.printStackTrace();
                            }

                            if (testFatt != null && testFatt.trow != null) {
                                SwingUtils.showErrorMessage(main.getPadreWindow(), testFatt.trow.getMessage());
                            } else {
                                MenuPanel m = (MenuPanel) main.getPadrePanel();
                                m.openFrame(frm, 740, InvoicexUtil.getHeightIntFrame(750));
                            }

                            System.out.println("def cursor");
                            frmElenFatt.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                            frmElenFatt.this.griglia.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                        }

                    };

                    worker1.execute();

                }
            });
        }
    }//GEN-LAST:event_butModiActionPerformed

    private void butNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNewActionPerformed
        InvoicexUtil.attendiCaricamentoPluginRitenute(new Runnable() {

            public void run() {
                frmElenFatt.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                MicroBench mb = new MicroBench();
                mb.start();

                //controllo se sono aperti altri frmTestFatt
                if (frmElenFatt.this.controllaAperFatt() == false) {

                    frmTestFatt frmTemp = new frmTestFatt();
                    frmTemp.init(frmTestFatt.DB_INSERIMENTO, "", 0, "P", 0, dbFattura.TIPO_FATTURA_IMMEDIATA, -1);

                    frmTemp.from = frmElenFatt.this;
                    JInternalFrame frm = (JInternalFrame) frmTemp;

                    //fine, sotto 1 riga cancellata da lorenzo
                    //frmTestFatt frm = new frmTestFatt(frmTestFatt.DB_INSERIMENTO,"",0,"P",0, dbFattura.TIPO_FATTURA_IMMEDIATA);
                    MenuPanel m = (MenuPanel) main.getPadrePanel();
                    m.openFrame(frm, 740, InvoicexUtil.getHeightIntFrame(750));

                }

                mb.out("butNewActionPerformed");

                frmElenFatt.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

    }//GEN-LAST:event_butNewActionPerformed

private void butNew2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNew2ActionPerformed
    //creo una fattura pro-forma
    this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

    //controllo se sono aperti altri frmTestFatt
    if (this.controllaAperFatt() == false) {

        //inserito da lorenzo
        javax.swing.JInternalFrame frm = null;

        frmTestFatt frmTemp = new frmTestFatt();
        frmTemp.init(frmTestFatt.DB_INSERIMENTO, "*", 0, "P", 0, dbFattura.TIPO_FATTURA_PROFORMA, -1);
        frmTemp.from = this;
        frm = (JInternalFrame) frmTemp;

        MenuPanel m = (MenuPanel) main.getPadrePanel();
        m.openFrame(frm, 740, InvoicexUtil.getHeightIntFrame(750));
    }

    this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_butNew2ActionPerformed

private void butCreaFatturaDaProformaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCreaFatturaDaProformaActionPerformed
    if (griglia.getSelectedRowCount() <= 0) {
        SwingUtils.showErrorMessage(this, "Seleziona un documento prima !");
        return;
    }
    if (griglia.getSelectedRowCount() > 1) {
        SwingUtils.showErrorMessage(this, "Seleziona un solo documento !");
        return;
    }
    String dbSerie = String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), 1));
    int dbNumero = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), 2)));
    int dbAnno = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), 3)));
    int id = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"))));

    try {
        InvoicexUtil.aggiornaTotaliRighe(Db.TIPO_DOCUMENTO_FATTURA, id);
    } catch (Exception e) {
        e.printStackTrace();
    }

    String sql = "";
    Statement stat = null;
    try {
        stat = Db.getConn().createStatement();
        sql = "select * from test_fatt";
        sql += " where id = " + id;
        ResultSet resu = stat.executeQuery(sql);
        boolean isProforma = false;
        if (resu.next()) {
            if (resu.getInt("tipo_fattura") == dbFattura.TIPO_FATTURA_PROFORMA) {
                isProforma = true;
            }
        }

        if (!isProforma) {
            JOptionPane.showMessageDialog(this, "Il documento selezionato non e' una fattura pro-forma", "Attenzione", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //chiedo se vuole una fattora accompgnatoria
        Object[] sceltatipo = {"Fattura", "Fattura accompagnatoria"};
        Object sceltaret = JOptionPane.showInputDialog(null, "Seleziona il tipo di fattura", "Richiesta", JOptionPane.INFORMATION_MESSAGE, null, sceltatipo, sceltatipo[0]);
        System.out.println("selectedValue = " + sceltaret);
        int tipofatt_scelto = dbFattura.TIPO_FATTURA_IMMEDIATA;
        if (sceltaret.equals(sceltatipo[1])) {
            tipofatt_scelto = dbFattura.TIPO_FATTURA_ACCOMPAGNATORIA;
        }
        
        String newSerie = "";
        if (main.fileIni.getValue("varie", "campoSerie", "S").equalsIgnoreCase("S")) {
            JDialogSelezionaSerie diaSerie = new JDialogSelezionaSerie(main.getPadre(), true);
            diaSerie.pack();
            diaSerie.setLocationRelativeTo(null);
            diaSerie.setVisible(true);
            newSerie = diaSerie.serie;
        }        

        //nuovo anno
        int nuovoanno = DateUtils.getCurrentYear();

        //trovo nuovo numero
        sql = "select numero from test_fatt";
        sql += " where anno = " + nuovoanno;
        sql += " and serie = " + dbu.sql(newSerie);
        sql += " and tipo_fattura != 7";    //scontrini
        sql += " order by numero desc limit 1";
        resu = stat.executeQuery(sql);
        int nuovonumero = 0;
        if (resu.next() == true) {
            nuovonumero = resu.getInt(1) + 1;
        } else {
            nuovonumero = 1;
        }

        //aggiorno il documento test, righ
        sql = "update test_fatt set numero = " + nuovonumero;
        sql += " , serie = " + dbu.sql(newSerie) + ", tipo_fattura = " + tipofatt_scelto;
        sql += " , anno = " + nuovoanno;
        sql += " , data = " + Db.pc(new java.util.Date(), Types.DATE);
        sql += " where id = '" + id + "'";
        Db.executeSql(sql);

        sql = "update righ_fatt set numero = " + nuovonumero;
        sql += " , serie = " + dbu.sql(newSerie);
        sql += " , anno = " + nuovoanno;
        sql += " where id_padre = " + id;
        Db.executeSql(sql);

        //prima cambiavo i riferimenti su scadenze e provvigioni ma adesso con id_doc non serve più
        dbDocumento tempPrev = new dbDocumento();
        tempPrev.serie = "";
        tempPrev.numero = nuovonumero;
        tempPrev.anno = dbAnno;
        tempPrev.tipoDocumento = Db.TIPO_DOCUMENTO_FATTURA;
        tempPrev.setId(id);
        int tipo_fattura = tipofatt_scelto;
        boolean genera = InvoicexUtil.generareMovimenti(tipo_fattura, this);

        //elimino i movimenti eventuali
        Object inizio_mysql = Db.getCurrentTimestamp();
        try {
            main.magazzino.preDelete("where da_tabella = 'test_fatt' and da_id = " + id);
            sql = "delete from movimenti_magazzino where da_tabella = 'test_fatt' and da_id = " + id;
            DbUtils.tryExecQuery(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        main.events.fireInvoicexEventMagazzino(this, inizio_mysql);

        if (genera) {
            if (tempPrev.generaMovimentiMagazzino() == false) {
                javax.swing.JOptionPane.showMessageDialog(this, "Errore nella generazione dei movimenti di magazzino", "Errore", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }

        //aggiorno documenti collegati
        InvoicexUtil.aggiornaRiferimentoDocumenti(Db.TIPO_DOCUMENTO_FATTURA, id);

        butRefreshActionPerformed(null);
    } catch (Exception ex0) {
        ex0.printStackTrace();
    } finally {
        try {
            stat.close();
        } catch (Exception ex0) {
        }
    }
}//GEN-LAST:event_butCreaFatturaDaProformaActionPerformed

private void menConvertiFattProformaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menConvertiFattProformaActionPerformed
    butCreaFatturaDaProformaActionPerformed(null);
}//GEN-LAST:event_menConvertiFattProformaActionPerformed

private void menModificaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menModificaActionPerformed
    butModiActionPerformed(null);
}//GEN-LAST:event_menModificaActionPerformed

private void menEliminaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menEliminaActionPerformed
    butDeleActionPerformed(null);
}//GEN-LAST:event_menEliminaActionPerformed

private void menStampaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menStampaActionPerformed
    butPrinActionPerformed(null);
}//GEN-LAST:event_menStampaActionPerformed

private void menPdfEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menPdfEmailActionPerformed
    butEmailActionPerformed(null);
}//GEN-LAST:event_menPdfEmailActionPerformed

private void jScrollPane1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jScrollPane1MousePressed
}//GEN-LAST:event_jScrollPane1MousePressed

private void grigliaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMousePressed
    if (evt.isPopupTrigger()) {
        mouse_point = evt.getPoint();
        cambiaEtichettaAnticipazione();
        pop.show(evt.getComponent(), evt.getX(), evt.getY());
    }
}//GEN-LAST:event_grigliaMousePressed

private void grigliaMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseReleased
    if (evt.isPopupTrigger()) {
        mouse_point = evt.getPoint();
        cambiaEtichettaAnticipazione();
        pop.show(evt.getComponent(), evt.getX(), evt.getY());
    }
}//GEN-LAST:event_grigliaMouseReleased

    public void cambiaEtichettaAnticipazione() {
        try {
            menSegnalaAnticipata.setText("Segna come anticipata");

            if (griglia.getSelectedRowCount() <= 0) {
                menSegnalaAnticipata.setEnabled(false);
                return;
            }

            if (griglia.getSelectedRowCount() > 1) {
                menSegnalaAnticipata.setEnabled(false);
                return;
            }

            if (griglia.getSelectedRowCount() == 1) {
                System.out.println("griglia.rowAtPoint(mouse_point): " + griglia.rowAtPoint(mouse_point));
                System.out.println("colonna id click : " + griglia.getColumnByName("id"));
                System.out.println("griglia get value : " + griglia.getValueAt(griglia.rowAtPoint(mouse_point), griglia.getColumnByName("id")));
                int dbIdFattura = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.rowAtPoint(mouse_point), griglia.getColumnByName("id"))));
                // Verifico che la fattura sia già anticipata
                if (dbFattura.isFatturaAnticipata(dbIdFattura)) {
                    menSegnalaAnticipata.setText("Riporta a fattura non anticipata");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recuperaPagamentoCliente(int idDuplicata) {
        String sql = "SELECT * FROM test_fatt WHERE id = '" + idDuplicata + "'";
        ResultSet fattura = Db.openResultSet(sql);
        int idCliente = 0;
        try {
            if (fattura.next()) {
                idCliente = fattura.getInt("cliente");

                if (idCliente > 0) {
                    sql = "SELECT * FROM clie_forn WHERE codice = '" + idCliente + "'";
                    ResultSet datiCliente = Db.openResultSet(sql);

                    if (datiCliente.next()) {
                        String pagamento = datiCliente.getString("pagamento");
                        String abi = datiCliente.getString("banca_abi");
                        String cab = datiCliente.getString("banca_cab");
                        String cc = datiCliente.getString("banca_cc");
                        String iban = datiCliente.getString("banca_cc_iban");
                        String note = "";
                        sql = "SELECT note_su_documenti as note FROM pagamenti";
                        sql += " WHERE codice = '" + pagamento + "'";
                        ResultSet rsNote = Db.openResultSet(sql);

                        if (rsNote.next()) {
                            note = rsNote.getString("note");
                        }

                        sql = "UPDATE test_fatt set ";
                        sql += " pagamento = '" + pagamento + "', ";
                        sql += " banca_abi = '" + abi + "', ";
                        sql += " banca_cab = '" + cab + "', ";
                        sql += " banca_cc = '" + cc + "', ";
                        sql += " banca_iban = '" + iban + "', ";
                        sql += " note_pagamento = '" + note + "' ";
                        sql += " WHERE id = '" + idDuplicata + "'";

                        System.out.println("DEBUG -- insert SQL: " + sql);
                        Db.executeSql(sql);
                    }
                }
            }
        } catch (Exception e) {
        }

    }

private void butDuplicaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDuplicaActionPerformed

    if (griglia.getSelectedRowCount() <= 0) {
        SwingUtils.showErrorMessage(this, "Seleziona un documento prima!");
        return;
    }

    String sql;
    String sqlC = "";
    String sqlV = "";
    int newAnno = java.util.Calendar.getInstance().get(Calendar.YEAR);
    int newNumero;
    int idDuplicata = 0;

    int numDup = griglia.getSelectedRows().length;
    int res = -1;
    //chiedo conferma per eliminare il documento
    if (numDup > 1) {
//        String msg = "Sicuro di voler duplicare " + numDup + " documenti ?";
//        res = JOptionPane.showConfirmDialog(this, msg);
        SwingUtils.showInfoMessage(main.getPadre(), "Non è possibile duplicare più documenti contemporaneamente,\nselezionarne uno per volta");
    } else {
        res = JOptionPane.OK_OPTION;
    }

    if (res == JOptionPane.OK_OPTION) {
        res = JOptionPane.showConfirmDialog(this, "Riprendere i dati di pagamento dall'anagrafica Cliente ?");
        boolean riprendi = false;
        if (res == JOptionPane.OK_OPTION) {
            riprendi = true;
        }
        if (res == JOptionPane.CANCEL_OPTION) {
            return;
        }

        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        for (int sel : griglia.getSelectedRows()) {

            String dbSerie = String.valueOf(griglia.getValueAt(sel, 1));
            int dbNumero = Integer.parseInt(String.valueOf(griglia.getValueAt(sel, 2)));
            int dbAnno = Integer.parseInt(String.valueOf(griglia.getValueAt(sel, 3)));
            int dbId = Integer.parseInt(String.valueOf(griglia.getValueAt(sel, griglia.getColumnByName("id"))));

            try {
                InvoicexUtil.aggiornaTotaliRighe(Db.TIPO_DOCUMENTO_FATTURA, dbId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //cerco ultimo numero
            newNumero = 1;
            sqlC = "";
            sqlV = "";
            sql = "SELECT MAX(numero) as maxnum FROM test_fatt WHERE anno = '" + newAnno + "' and tipo_fattura != 7";   //scontrini
            sql += " and serie = '" + dbSerie + "'";
            try {
                ResultSet tempUltimo = Db.openResultSet(sql);
                if (tempUltimo.next() == true) {
                    newNumero = tempUltimo.getInt("maxnum") + 1;
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

            //    SwingUtils.showInfoMessage(this, "newnumero:" + newNumero);
            //inserisco nuovo salvandomi i dati su hashtable
            sql = "select * from test_fatt";
            sql += " where id = " + dbId;
            ResultSet tempPrev = Db.openResultSet(sql);

            try {
                ResultSetMetaData metaPrev = tempPrev.getMetaData();

                List colonne_da_ignorare = new ArrayList();
                colonne_da_ignorare.add("mail_inviata");
                colonne_da_ignorare.add("ts");
                colonne_da_ignorare.add("ts_gen_totali");
                colonne_da_ignorare.add("anticipata");
                colonne_da_ignorare.add("banca_di_anticipazione");
                colonne_da_ignorare.add("bloccata");

                if (tempPrev.next() == true) {
                    for (int i = 1; i <= metaPrev.getColumnCount(); i++) {
                        if (!metaPrev.getColumnName(i).equalsIgnoreCase("id")) {
                            if (metaPrev.getColumnName(i).equalsIgnoreCase("numero")) {
                                sqlC += "numero";
                                sqlV += Db.pc(newNumero, metaPrev.getColumnType(i));
                            } else if (metaPrev.getColumnName(i).equalsIgnoreCase("anno")) {
                                sqlC += "anno";
                                sqlV += Db.pc(java.util.Calendar.getInstance().get(Calendar.YEAR), "LONG");
                            } else if (metaPrev.getColumnName(i).equalsIgnoreCase("data")) {
                                DateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Calendar myCalendar = GregorianCalendar.getInstance();
                                sqlC += "data";
                                sqlV += Db.pc(myFormat.format(myCalendar.getTime()), metaPrev.getColumnType(i));
                            } else if (!colonne_da_ignorare.contains(metaPrev.getColumnName(i))) {
                                sqlC += metaPrev.getColumnName(i);
                                sqlV += Db.pc(tempPrev.getObject(i), metaPrev.getColumnType(i));
                            }
                            if (!colonne_da_ignorare.contains(metaPrev.getColumnName(i))) {
                                sqlC += ",";
                                sqlV += ",";
                            }
                        }
                    }
                    sqlC = StringUtils.chop(sqlC);
                    sqlV = StringUtils.chop(sqlV);
                    if (sqlC.endsWith(",")) {
                        sqlC = StringUtils.chop(sqlC);
                    }
                    if (sqlV.endsWith(",")) {
                        sqlV = StringUtils.chop(sqlV);
                    }
                    sql = "insert into test_fatt ";
                    sql += "(" + sqlC + ") values (" + sqlV + ")";
                    System.out.println("duplica:" + sql);
                    Db.executeSql(sql);

                    sql = "select * from test_fatt";
                    sql += " where serie = '" + dbSerie + "'";
                    sql += " and numero = '" + newNumero + "'";
                    sql += " and anno = '" + newAnno + "'";
                    sql += " and tipo_fattura != 7";
                    ResultSet lastIns = Db.openResultSet(sql);
                    try {
                        if (lastIns.next()) {
                            idDuplicata = lastIns.getInt("id");
                            System.out.println("idDuplicata:" + idDuplicata);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

            //inserisco nuovo ordine salvandomi i dati su hashtable
            sql = "select * from righ_fatt";
            sql += " where id_padre = " + dbId;
            ResultSet tempPrev2 = Db.openResultSet(sql);
            try {
                ResultSetMetaData metaPrev2 = tempPrev2.getMetaData();

                List colonne_da_ignorare = new ArrayList();
                colonne_da_ignorare.add("id");
                colonne_da_ignorare.add("da_ordi_riga");
                colonne_da_ignorare.add("da_ddt_riga");
                colonne_da_ignorare.add("da_ordi");
                colonne_da_ignorare.add("da_ddt");
                colonne_da_ignorare.add("ts");
                colonne_da_ignorare.add("ts_gen_totali");

                while (tempPrev2.next() == true) {
                    sqlC = "";
                    sqlV = "";
                    for (int i = 1; i <= metaPrev2.getColumnCount(); i++) {
                        if (!metaPrev2.getColumnName(i).equalsIgnoreCase("id")) {
                            if (metaPrev2.getColumnName(i).equalsIgnoreCase("numero")) {
                                sqlC += "numero";
                                sqlV += Db.pc(newNumero, metaPrev2.getColumnType(i));
                            } else if (metaPrev2.getColumnName(i).equalsIgnoreCase("anno")) {
                                sqlC += "anno";
                                sqlV += Db.pc(java.util.Calendar.getInstance().get(Calendar.YEAR), "LONG");
                            } else if (metaPrev2.getColumnName(i).equalsIgnoreCase("id_padre")) {
                                sqlC += "id_padre";
                                sqlV += idDuplicata;
                            } else {
                                if (colonne_da_ignorare.contains(metaPrev2.getColumnName(i))) {
                                    continue;
                                }
                                sqlC += metaPrev2.getColumnName(i);
                                sqlV += Db.pc(tempPrev2.getObject(i), metaPrev2.getColumnType(i));
                            }
                            if (i != metaPrev2.getColumnCount()) {
                                sqlC += ",";
                                sqlV += ",";
                            }
                        }
                    }
                    sql = "insert into righ_fatt ";
                    if (sqlC.endsWith(",")) {
                        sqlC = StringUtils.chop(sqlC);
                    }
                    if (sqlV.endsWith(",")) {
                        sqlV = StringUtils.chop(sqlV);
                    }
                    sql += "(" + sqlC + ") values (" + sqlV + ")";
                    System.out.println("duplica righe:" + sql);
                    Db.executeSql(sql);
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
            if (riprendi) {
                recuperaPagamentoCliente(idDuplicata);
            }
        }

        final Integer idDuplicata_f = idDuplicata;
        Runnable run = new Runnable() {

            public void run() {
                //aprire il nuovo
                //cerco il dbId
                for (int row = 0; row < griglia.getRowCount(); row++) {
                    if (CastUtils.toInteger0(griglia.getValueAt(row, griglia.getColumnByName("id"))) == idDuplicata_f.intValue()) {
                        griglia.getSelectionModel().setSelectionInterval(row, row);
                        griglia.scrollToRow(row);
                        butModiActionPerformed(null);
                        break;
                    }
                }
            }
        };
        dbRefresh(true, run);
    }
    this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_butDuplicaActionPerformed

private void texClienteKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texClienteKeyReleased
    System.out.println("texClienteKeyReleased evt:" + evt.getKeyChar() + " text:" + texCliente.getText());
    filtraPerCliente();
}//GEN-LAST:event_texClienteKeyReleased

private void menExportCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menExportCSVActionPerformed
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    if (griglia.getSelectedRowCount() < 0) {
        JOptionPane.showMessageDialog(this, "Seleziona almeno una riga da esportare", "Errore Selezione", JOptionPane.INFORMATION_MESSAGE);
        return;
    } else {
        int[] ids = new int[griglia.getSelectedRowCount()];
        int i = 0;

        int first = griglia.getSelectedRow();
        String serie = String.valueOf(griglia.getValueAt(first, griglia.getColumnByName("serie")));
        String numero = String.valueOf(griglia.getValueAt(first, griglia.getColumnByName("numero")));
        String nomeCliente = String.valueOf(griglia.getValueAt(first, griglia.getColumnByName("Cliente")));
//        String nomeFile = "documento_" + Db.TIPO_DOCUMENTO_FATTURA + "_" + serie + numero + "_" + nomeCliente;        
//        nomeFile = FileUtils.normalizeFileName(nomeFile);
        String nomeFile = InvoicexUtil.getNomeFileDoc(Db.TIPO_DOCUMENTO_FATTURA, serie, numero, nomeCliente, false);

        String input = JOptionPane.showInputDialog(this, "Inserisci il nome con cui vuoi salvare il file: ", nomeFile);

        if (input != null) {
            if (!input.equals("")) {
                nomeFile = FileUtils.normalizeFileNameDir(input);

                for (int rigaSel : griglia.getSelectedRows()) {
                    int id = Integer.parseInt(String.valueOf(griglia.getValueAt(rigaSel, griglia.getColumnByName("id"))));
                    ids[i] = id;
                    i++;
                }
            } else {
                int res = JOptionPane.showConfirmDialog(this, "Non puoi inserire un nome vuoto per il file. Continuare con il nome standard?", "Errore inserimento", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.NO_OPTION) {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return;
                }
            }
            InvoicexUtil.exportCSV(Db.TIPO_DOCUMENTO_FATTURA, ids, nomeFile);
        }
    }
    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_menExportCSVActionPerformed

private void menDuplicaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menDuplicaActionPerformed
    butDuplicaActionPerformed(null);
}//GEN-LAST:event_menDuplicaActionPerformed

private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
    System.out.println("****** RESIZE");
}//GEN-LAST:event_formComponentResized

private void storicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storicoActionPerformed
    int id = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"))));
    InvoicexUtil.leggiDaStorico("fattura", id);
}//GEN-LAST:event_storicoActionPerformed

private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
    if (evt.getClickCount() == 3) {
        storico.setVisible(true);
    }
}//GEN-LAST:event_jPanel1MouseClicked

private void provvigioniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_provvigioniActionPerformed
    try {
        String sqlCorrente;

        boolean per_scadenze = true;
        try {
            String provvigioni_tipo_data = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select provvigioni_tipo_data from dati_azienda"));
            if (provvigioni_tipo_data.equalsIgnoreCase("data_fattura")) {
                per_scadenze = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sqlIniziale = null;
        if (per_scadenze) {
            sqlIniziale = " select test_fatt.pagamento, test_fatt.serie, test_fatt.numero, test_fatt.anno, test_fatt.data, test_fatt.totale, scadenze.data_scadenza as 'S data scadenza', scadenze.importo as 'S importo', scadenze.pagata as 'S pagata', provvigioni.importo as 'P importo', test_fatt.agente_percentuale as 'P %', provvigioni.importo_provvigione as 'P provvigione', provvigioni.pagata as 'P pagata', clie_forn.ragione_sociale as cliente, provvigioni.id, agenti.nome as agente, test_fatt.id as tid "
                    + " , (select count(*) from scadenze s2 where s2.id_doc = test_fatt.id and s2.documento_tipo = 'FA') as numero_scadenze, test_fatt.sconto"
                    + " from test_fatt \n"
                    + " left join scadenze on test_fatt.id = scadenze.id_doc \n"
                    + " left join provvigioni on test_fatt.id = provvigioni.id_doc and scadenze.id = provvigioni.id_scadenza \n"
                    + " left join clie_forn on test_fatt.cliente = clie_forn.codice" + "   left join pagamenti on test_fatt.pagamento = pagamenti.codice \n"
                    + " left join agenti on test_fatt.agente_codice = agenti.id \n"
                    + " where scadenze.documento_tipo = 'FA'" + " and provvigioni.importo is not null and agenti.id is not null";
        } else {
            sqlIniziale = " select test_fatt.pagamento, test_fatt.serie, test_fatt.numero, test_fatt.anno, test_fatt.data as 'S data scadenza', test_fatt.totale as 'S importo', '' as 'S pagata', provvigioni.importo as 'P importo', test_fatt.agente_percentuale as 'P %', provvigioni.importo_provvigione as 'P provvigione', provvigioni.pagata as 'P pagata', clie_forn.ragione_sociale as cliente, provvigioni.id, agenti.nome as agente, test_fatt.id as tid "
                    + " , 1 as numero_scadenze, test_fatt.sconto"
                    + " from test_fatt \n"
                    + "   left join provvigioni on test_fatt.id = provvigioni.id_doc \n"
                    + "   left join clie_forn on test_fatt.cliente = clie_forn.codice \n"
                    + "   left join pagamenti on test_fatt.pagamento = pagamenti.codice \n"
                    + "   left join agenti on test_fatt.agente_codice = agenti.id \n"
                    + " where provvigioni.importo is not null and agenti.id is not null \n";
        }
        final int dbId = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"))));
        sqlCorrente = sqlIniziale + " and test_fatt.id = " + dbId;
        sqlCorrente += " order by test_fatt.data, test_fatt.serie, test_fatt.numero, test_fatt.id";
        System.out.println("sqlCorrente = " + sqlCorrente);
//        JasperReport r = (JasperReport) JRLoader.loadObject("reports/provvigioni_det.jrxml.jasper");

        File freport = new File(main.wd + "reports/provvigioni_det2.jrxml");
        JasperReport r = Reports.getReport(freport);
        HashMap params = new HashMap();
        params.put("query", sqlCorrente);
        params.put("SUBREPORT_DIR", main.wd + "reports/");
        String note1 = "";
        note1 = main.attivazione.getDatiAzienda().getRagione_sociale();
        params.put("note1", note1);
        JasperPrint p = JasperFillManager.fillReport(r, params, Db.getConn());
        JasperViewer viewer = new JasperViewer(p, false);
        viewer.setVisible(true);
    } catch (JRException ex) {
        SwingUtils.showExceptionMessage(this, ex);
        ex.printStackTrace();
    }
}//GEN-LAST:event_provvigioniActionPerformed

private void rigeneraProvvigioniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rigeneraProvvigioniActionPerformed
    try {
        String dbSerie = String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), 1));
        int dbNumero = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), 2)));
        int dbAnno = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), 3)));
        int dbIdFattura = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"))));

        int agente_codice = CastUtils.toInteger(DbUtils.getObject(Db.getConn(), "select agente_codice from test_fatt where id = " + dbIdFattura));
        double agente_perc = CastUtils.toDouble(DbUtils.getObject(Db.getConn(), "select agente_percentuale from test_fatt where id = " + dbIdFattura));

//        ProvvigioniFattura provvigioni = new ProvvigioniFattura(Db.TIPO_DOCUMENTO_FATTURA, dbSerie, dbNumero, dbAnno, agente_codice, agente_perc);
        ProvvigioniFattura provvigioni = new ProvvigioniFattura(dbIdFattura, agente_codice, agente_perc);
        boolean ret = provvigioni.generaProvvigioni();
        System.out.println("esito genera provvigioni:" + ret + " : " + provvigioni.ret);
        SwingUtils.showInfoMessage(main.getPadreWindow(), "provvigioni generate");
    } catch (Exception e) {
        SwingUtils.showExceptionMessage(main.getPadreWindow(), e);
    }
}//GEN-LAST:event_rigeneraProvvigioniActionPerformed

private void menColAggRiferimentoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColAggRiferimentoClienteActionPerformed
    System.out.println("ColAgg_RiferimentoCliente = " + menColAggRiferimentoCliente.isSelected());
    main.fileIni.setValue("pref", "ColAgg_RiferimentoCliente", menColAggRiferimentoCliente.isSelected());
    dbRefresh(true);
}//GEN-LAST:event_menColAggRiferimentoClienteActionPerformed
private void riprendiDaStoricoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_riprendiDaStoricoActionPerformed
    try {
        int dbIdFattura = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"))));
        if (dbFattura.fatturaBloccata(dbIdFattura, (Integer) dbu.getObject(Db.getConn(), "SELECT tipo_fattura FROM test_fatt WHERE id = " + Db.pc(dbIdFattura, Types.INTEGER), true))) {
            SwingUtils.showErrorMessage(frmElenFatt.this, "Impossibile procedere, la fattura risulta bloccata perchè inclusa in una stampa definitiva del registro iva!\nPuoi vederne il contenuto con il pulsante di Stampa");
            return;
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    boolean presente = false;
    JInternalFrame[] iframes = main.getPadre().getDesktopPane().getAllFrames();
    for (JInternalFrame f : iframes) {
        if (f instanceof frmTestFatt) {
            presente = true;
            break;
        }
    }
    if (presente) {
        SwingUtils.showErrorMessage(this, "Per usare questa funzione prima chiudi tutte le fatture aperte");
        return;
    }

    JDialogStorico dialog = new JDialogStorico(main.getPadreWindow(), true, this);
    int id = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"))));
    dialog.leggiDaStorico("fattura", id);
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
}//GEN-LAST:event_riprendiDaStoricoActionPerformed

private void jScrollPane1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jScrollPane1MouseEntered
//    ToolTipManager.sharedInstance().setInitialDelay(0);
//    Action action = griglia.getActionMap().get("postTip");
//    System.out.println("action: " + action);
//    if (action != null) {
//        action.actionPerformed(new ActionEvent(griglia, ActionEvent.ACTION_PERFORMED, "postTip"));
//    }
}//GEN-LAST:event_jScrollPane1MouseEntered

private void menColAggRifActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColAggRifActionPerformed
    System.out.println("ColAgg_Rif = " + menColAggRif.isSelected());
    main.fileIni.setValue("pref", "ColAgg_Rif", menColAggRif.isSelected());
    dbRefresh(true);
}//GEN-LAST:event_menColAggRifActionPerformed

private void menColoraRossoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoraRossoActionPerformed
    InvoicexUtil.salvaColoreRiga("rosso", "test_fatt", griglia);
}//GEN-LAST:event_menColoraRossoActionPerformed

private void menColoraBluActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoraBluActionPerformed
    InvoicexUtil.salvaColoreRiga("blu", "test_fatt", griglia);
}//GEN-LAST:event_menColoraBluActionPerformed

private void menColoraGialloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoraGialloActionPerformed
    InvoicexUtil.salvaColoreRiga("giallo", "test_fatt", griglia);
}//GEN-LAST:event_menColoraGialloActionPerformed

private void menTogliColoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menTogliColoreActionPerformed
    InvoicexUtil.salvaColoreRiga("", "test_fatt", griglia);
}//GEN-LAST:event_menTogliColoreActionPerformed

private void texClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texClienteActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_texClienteActionPerformed

    private void menColAggCatCliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColAggCatCliActionPerformed
        main.fileIni.setValue("pref", "ColAgg_CatCli", menColAggCatCli.isSelected());
        dbRefresh(true);
    }//GEN-LAST:event_menColAggCatCliActionPerformed

    private void filtroTipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filtroTipoActionPerformed
        if (!cu.s(filtroTipo.getName()).equalsIgnoreCase("init")) {
            filtroTipo.setName("action");
            dbRefresh(false);
        }
    }//GEN-LAST:event_filtroTipoActionPerformed

    private void menColoraRosso2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoraRosso2ActionPerformed
        InvoicexUtil.salvaColoreRiga("rosso2", "test_fatt", griglia);
    }//GEN-LAST:event_menColoraRosso2ActionPerformed

    private void menColoraBlu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoraBlu2ActionPerformed
        InvoicexUtil.salvaColoreRiga("blu2", "test_fatt", griglia);
    }//GEN-LAST:event_menColoraBlu2ActionPerformed

    private void menColoraGiallo2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoraGiallo2ActionPerformed
        InvoicexUtil.salvaColoreRiga("giallo2", "test_fatt", griglia);
    }//GEN-LAST:event_menColoraGiallo2ActionPerformed

    private void menColoraVerdeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoraVerdeActionPerformed
        InvoicexUtil.salvaColoreRiga("verde", "test_fatt", griglia);
    }//GEN-LAST:event_menColoraVerdeActionPerformed

    private void menColoraVerde2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoraVerde2ActionPerformed
        InvoicexUtil.salvaColoreRiga("verde2", "test_fatt", griglia);
    }//GEN-LAST:event_menColoraVerde2ActionPerformed

    private void menColoraArancioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoraArancioneActionPerformed
        InvoicexUtil.salvaColoreRiga("arancione", "test_fatt", griglia);
    }//GEN-LAST:event_menColoraArancioneActionPerformed

    private void menColoreArancione2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoreArancione2ActionPerformed
        InvoicexUtil.salvaColoreRiga("arancione2", "test_fatt", griglia);
    }//GEN-LAST:event_menColoreArancione2ActionPerformed

    private void menColoreCelesteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoreCelesteActionPerformed
        InvoicexUtil.salvaColoreRiga("celeste", "test_fatt", griglia);
    }//GEN-LAST:event_menColoreCelesteActionPerformed

    private void menColoreCeleste2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColoreCeleste2ActionPerformed
        InvoicexUtil.salvaColoreRiga("celeste2", "test_fatt", griglia);
    }//GEN-LAST:event_menColoreCeleste2ActionPerformed

    private void menExportXmlPaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menExportXmlPaActionPerformed
        //SwingUtils.showInfoMessage(this, "aaaa");
    }//GEN-LAST:event_menExportXmlPaActionPerformed

    private void menSegnalaAnticipataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menSegnalaAnticipataActionPerformed
        if (griglia.getSelectedRowCount() <= 0) {
            SwingUtils.showErrorMessage(this, "Seleziona un documento prima!");
            return;
        }
        if (griglia.getSelectedRowCount() > 1) {
            SwingUtils.showErrorMessage(this, "Seleziona un documento per volta");
            return;
        }
        if (griglia.getSelectedRow() < 0) {
            return;
        }

        int dbIdFattura = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id"))));
        if (dbFattura.isFatturaAnticipata(dbIdFattura)) {
            smarcaFatturaAnticipata(dbIdFattura);
        } else {
            segnalaFatturaAnticipata(dbIdFattura);
        }
    }//GEN-LAST:event_menSegnalaAnticipataActionPerformed

    private void smarcaFatturaAnticipata(Integer idFattura) {
        int ret = javax.swing.JOptionPane.showConfirmDialog(main.getPadreWindow(), "Proseguendo la fattura tornerà allo stato non anticipata e verrà deselezionata dal conto di anticipazione. Procedere?", "Fattura Anticipata", javax.swing.JOptionPane.YES_NO_OPTION);
        if (ret == JOptionPane.NO_OPTION) {
            return;
        } else {

            String sql = "UPDATE test_fatt SET anticipata = 'N', banca_di_anticipazione = null WHERE id = " + Db.pc(idFattura, Types.INTEGER);
            Db.executeSql(sql);

            dbRefresh();
        }
    }

    private void segnalaFatturaAnticipata(Integer idFattura) {
        try {
            // Controllo se esistono conti correnti aziendali inseriti per procedere:
            Integer id = CastUtils.toInteger(Db.nz(DbUtils.getObject(Db.getConn(), "SELECT id FROM dati_azienda_banche", false), null));
            if (id != null) {
                JDialogFatturaAnticipata dialog = new JDialogFatturaAnticipata(main.getPadreWindow(), false, this, idFattura);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            } else {
                int ret = javax.swing.JOptionPane.showConfirmDialog(main.getPadreWindow(), "Non hai ancora inserito nessun conto corrente aziendale su cui anticipare la fattura. Vuoi inserirne uno per procedere?", "Errore conti correnti aziendali", javax.swing.JOptionPane.YES_NO_OPTION);
                if (ret == JOptionPane.NO_OPTION) {
                    return;
                } else {
                    frmDatiAzieBanc frm = new frmDatiAzieBanc();
                    main.getPadre().openFrame(frm, 700, InvoicexUtil.getHeightIntFrame(500));
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(frmElenFatt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void menColAggAnticipataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColAggAnticipataActionPerformed
        main.fileIni.setValue("pref", "ColAgg_Anticipata", menColAggAnticipata.isSelected());
        dbRefresh(true);
    }//GEN-LAST:event_menColAggAnticipataActionPerformed

    private void menColAggAnticipataBancaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColAggAnticipataBancaActionPerformed
        main.fileIni.setValue("pref", "ColAgg_AnticipataBanca", menColAggAnticipataBanca.isSelected());
        dbRefresh(true);
    }//GEN-LAST:event_menColAggAnticipataBancaActionPerformed

    private void jPanel4ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel4ComponentResized
        if ((butRefresh.getLocation().y + butRefresh.getSize().height) > jPanel4.getSize().height) {
            int newh = butRefresh.getLocation().y + butRefresh.getSize().height + 5;
            jPanel4.setMinimumSize(new Dimension(100, newh));
            jPanel4.setPreferredSize(new Dimension(100, newh));
        } else if (jPanel4.getSize().height > (butRefresh.getLocation().y + butRefresh.getSize().height) + 5) {
            int newh = butRefresh.getLocation().y + butRefresh.getSize().height + 5;
            jPanel4.setMinimumSize(new Dimension(100, newh));
            jPanel4.setPreferredSize(new Dimension(100, newh));
        }
    }//GEN-LAST:event_jPanel4ComponentResized

    private void linkAggiungiFiltroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkAggiungiFiltroActionPerformed
        try {
            JDialog dialog = InvoicexUtil.getDialogFiltri(this, true, false, filters);
            dialog.pack();
            InvoicexUtil.mettiSotto(dialog, linkAggiungiFiltro);
            dialog.setVisible(true);
            boolean conferma = dialog.getClass().getField("conferma").getBoolean(dialog);
            if (conferma) {
                filters = (List) dialog.getClass().getDeclaredMethod("getFilters").invoke(dialog);
                InvoicexUtil.aggiornaFiltri(panFiltri, filters, linkAggiungiFiltro, filtriActionModifica, filtriActionRimuovi);
                try {
                    InvoicexUtil.salvaFiltri(filters, this.getClass().getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dbRefresh();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }//GEN-LAST:event_linkAggiungiFiltroActionPerformed

    private void menColAggAgenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColAggAgenteActionPerformed
        main.fileIni.setValue("pref", "ColAgg_Agente", menColAggAgente.isSelected());
        dbRefresh(true);
    }//GEN-LAST:event_menColAggAgenteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCreaFatturaDaProforma;
    private javax.swing.JButton butDele;
    private javax.swing.JButton butDuplica;
    private javax.swing.JButton butEmail;
    public javax.swing.JButton butModi;
    public javax.swing.JButton butNew;
    private javax.swing.JButton butNew1;
    private javax.swing.JButton butNew2;
    private javax.swing.JButton butNuovaFatturaAccompagnatoria;
    public javax.swing.JButton butPrin;
    private javax.swing.JButton butRefresh;
    public javax.swing.JComboBox filtroTipo;
    public tnxbeans.tnxDbGrid griglia;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    public javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    public javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JLabel labTotale;
    public org.jdesktop.swingx.JXHyperlink linkAggiungiFiltro;
    private javax.swing.JMenu menColAgg;
    private javax.swing.JCheckBoxMenuItem menColAggAgente;
    private javax.swing.JCheckBoxMenuItem menColAggAnticipata;
    private javax.swing.JCheckBoxMenuItem menColAggAnticipataBanca;
    private javax.swing.JCheckBoxMenuItem menColAggCatCli;
    private javax.swing.JCheckBoxMenuItem menColAggRif;
    private javax.swing.JCheckBoxMenuItem menColAggRiferimentoCliente;
    private javax.swing.JMenuItem menColoraArancione;
    private javax.swing.JMenuItem menColoraBlu;
    private javax.swing.JMenuItem menColoraBlu2;
    private javax.swing.JMenuItem menColoraGiallo;
    private javax.swing.JMenuItem menColoraGiallo2;
    private javax.swing.JMenu menColoraRiga;
    private javax.swing.JMenuItem menColoraRosso;
    private javax.swing.JMenuItem menColoraRosso2;
    private javax.swing.JMenuItem menColoraVerde;
    private javax.swing.JMenuItem menColoraVerde2;
    private javax.swing.JMenuItem menColoreArancione2;
    private javax.swing.JMenuItem menColoreCeleste;
    private javax.swing.JMenuItem menColoreCeleste2;
    private javax.swing.JMenuItem menConvertiFattProforma;
    private javax.swing.JMenuItem menDuplica;
    private javax.swing.JMenuItem menElimina;
    private javax.swing.JMenuItem menExportCSV;
    public javax.swing.JMenuItem menExportXmlPa;
    private javax.swing.JMenuItem menModifica;
    private javax.swing.JMenuItem menPdfEmail;
    private javax.swing.JMenuItem menSegnalaAnticipata;
    private javax.swing.JMenuItem menStampa;
    private javax.swing.JMenuItem menTogliColore;
    private javax.swing.JPanel panDati;
    public javax.swing.JPanel panFiltri;
    public javax.swing.JPopupMenu pop;
    private javax.swing.JMenuItem provvigioni;
    private javax.swing.JMenuItem rigeneraProvvigioni;
    private javax.swing.JMenuItem riprendiDaStorico;
    private javax.swing.JMenuItem storico;
    private javax.swing.JTextField texAl;
    public javax.swing.JTextField texCliente;
    private javax.swing.JTextField texDal;
    private javax.swing.JTextField texLimit;
    // End of variables declaration//GEN-END:variables

    public void dbRefresh() {
        dbRefresh(true, null);
    }

    public void dbRefresh(final boolean locca) {
        dbRefresh(locca, null);
    }

    public void dbRefresh(final boolean locca, final Runnable runAfter) {
//        griglia.setEnabled(false);
//        SwingUtils.mouse_wait(this);
//        if (locca) {
        lockableUI.setLocked(true);

//        }
        final frmElenFatt elen = this;
        SwingWorker worker = new SwingWorker() {

            int oldsel = -1;
            int oldselid = -1;
            Rectangle old_visiblerect = null;

            @Override
            protected Object doInBackground() throws Exception {
                caricamento.lock();

                MicroBench mb = new MicroBench();
                mb.start();

//                try {
//                    Thread.sleep(3000);
//                } catch (Exception e) {
//                }
                old_visiblerect = griglia.getVisibleRect();
                oldsel = griglia.getSelectedRow();
                if (oldsel >= 0) {
                    try {
                        oldselid = CastUtils.toInteger(griglia.getValueAt(oldsel, griglia.getColumnByName("id")));
                    } catch (Exception e) {
                    }
                }

                String sql;
                sql = "select ";
                sql += " tipi_fatture.descrizione_breve AS Tipo, ";
                sql += " t.serie AS Serie, ";
                sql += " t.numero AS Numero, ";
                sql += " t.anno AS Anno, ";

                //format per data in italiano
                //sql += " DATE_FORMAT(test_fatt.data,'%d/%m/%y') AS Data ,";
                sql += " t.data AS Data ,";
                sql += " clie_forn.ragione_sociale As Cliente, ";
                if (main.fileIni.getValueBoolean("pref", "ColAgg_CatCli", false)) {
                    sql += " tcf.descrizione 'Categoria Cliente',";
                }

                sql += " t.note AS Note, ";

                //format per valuta in italianot
                //sql += " REPLACE(REPLACE(REPLACE(FORMAT(test_fatt.totale_imponibile,2),'.','X'),',','.'),'X',',') AS Totale ";
                if (main.pluginRitenute && main.fileIni.getValueBoolean("pluginRitenute", "totale_fatture_in_elenco", false)) {
                    sql += " t.totale_da_pagare * tipi_fatture.segno AS Totale, ";
                } else {
                    sql += " t.totale * tipi_fatture.segno AS Totale, ";
                }
                sql += " t.totale_imponibile * tipi_fatture.segno as totale_imponibile, ";
                sql += " t.id AS id,";
                sql += " t.totale_iva * tipi_fatture.segno as totale_iva";

                if (main.fileIni.getValueBoolean("pref", "ColAgg_Agente", false)) {
                    sql += " , agenti.nome as 'Agente' ";
                }

                if (main.fileIni.getValueBoolean("pref", "ColAgg_RiferimentoCliente", false)) {
                    sql += " , clie_forn.persona_riferimento as 'Riferimento Cliente'";
                }
                if (main.fileIni.getValueBoolean("pref", "ColAgg_Rif", false)) {
                    sql += " , t.riferimento as Riferimento";
                }

                if (main.pluginEmail) {
                    sql += " , t.mail_inviata as 'Mail Inviata'";
                }

                sql += ", t.color as color ";

                sql += " , count(fd.id) as Allegati";
                if (main.fileIni.getValueBoolean("pref", "ColAgg_Anticipata", false)) {
                    sql += ", t.anticipata as 'Anticipata'";
                }
                if (main.fileIni.getValueBoolean("pref", "ColAgg_AnticipataBanca", false)) {
                    sql += ", abi.nome as 'Banca'";
                }

                sql += " from test_fatt t left join clie_forn on t.cliente = clie_forn.codice";
                sql += "    left join tipi_clie_forn tcf on clie_forn.tipo2 = tcf.id";
                sql += "    left join agenti on t.agente_codice = agenti.id ";
                sql += "    left join tipi_fatture on t.tipo_fattura = tipi_fatture.tipo";
                sql += "    left join files_documenti fd on fd.id_doc = t.id and fd.tabella_doc = 'test_fatt'";
                if (main.fileIni.getValueBoolean("pref", "ColAgg_AnticipataBanca", false)) {
                    sql += "    left join dati_azienda_banche dab on t.banca_di_anticipazione = dab.id";
                    sql += "    left join banche_abi abi on dab.abi = abi.abi";
                }

                sql += " where 1 = 1\n";
                if (scontrini) {
                    sql += " and tipo_fattura = 7 ";
                } else {
                    sql += " and tipo_fattura != 7 ";
                }
                sql += sqlWhereDaData;
                sql += sqlWhereAData;
                sql += sqlWhereCliente;
                sql += InvoicexUtil.getWhereFiltri(filters, Db.TIPO_DOCUMENTO_FATTURA);

                if (elen.filtroTipo.getSelectedIndex() != 0) {
                    if (cu.s(elen.filtroTipo.getSelectedItem()).equals("Fatture")) {
                        sql += " and tipo_fattura = " + dbFattura.TIPO_FATTURA_IMMEDIATA;
                    } else if (cu.s(elen.filtroTipo.getSelectedItem()).equals("Fatture accompag.")) {
                        sql += " and tipo_fattura = " + dbFattura.TIPO_FATTURA_ACCOMPAGNATORIA;
                    } else if (cu.s(elen.filtroTipo.getSelectedItem()).equals("Fatture pro-forma")) {
                        sql += " and tipo_fattura = " + dbFattura.TIPO_FATTURA_PROFORMA;
                    } else if (cu.s(elen.filtroTipo.getSelectedItem()).equals("Note di credito")) {
                        sql += " and tipo_fattura = " + dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO;
                    } else if (cu.s(elen.filtroTipo.getSelectedItem()).startsWith("Solo serie ")) {
                        sql += " and t.serie = " + dbu.sql(StringUtils.substringAfter(cu.s(elen.filtroTipo.getSelectedItem()), "Solo serie "));
                    }
                }

                sql += " group by t.id ";
                sql += "\n order by t.data desc,t.numero desc";
                sql += sqlWhereLimit;
                System.out.println("sql elenco fatture:" + sql);

                setColsWidth();

                final String f_sql = sql;
                elen.griglia.dbOpen(Db.getConn(), f_sql, Db.INSTANCE);

                //calcolo il totale
                if (elen.visualizzaTotali == true) {

                    double totale = 0;
                    double totaleIva = 0;
                    double totaleImp = 0;
                    int totaleDocumenti = 0;
                    int contaDocumenti = 0;
                    ResultSet somma = Db.openResultSet(sql);
                    sql = "select count(*) from test_fatt";
                    sql += " where 1 = 1\n";
                    if (scontrini) {
                        sql += " and tipo_fattura = 7 ";
                    } else {
                        sql += " and tipo_fattura != 7 ";
                    }
                    ResultSet rtota = Db.openResultSet(sql);

                    try {

                        while (somma.next()) {

                            if (Db.nz(somma.getString("Tipo"), "").equals("NC")) {
                                totale -= Math.abs(somma.getDouble("Totale"));
                                totaleImp -= Math.abs(somma.getDouble("totale_imponibile"));
                                totaleIva -= Math.abs(somma.getDouble("totale_iva"));
                            } else {
                                totale += somma.getDouble("Totale");
                                totaleImp += somma.getDouble("totale_imponibile");
                                totaleIva += somma.getDouble("totale_iva");
                            }

                            contaDocumenti++;
                        }

                        if (rtota.next()) {
                            totaleDocumenti = rtota.getInt(1);
                        }
                        final int _contaDocumenti = contaDocumenti;
                        final int _totaleDocumenti = totaleDocumenti;
                        final double _totale = totale;
                        final double _totaleImp = totaleImp;
                        final double _totaleIva = totaleIva;
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                elen.labTotale.setText("documenti visualizzati " + _contaDocumenti + " di " + _totaleDocumenti + " / totale documenti visualizzati Imponib.: \u20ac " + it.tnx.Util.formatValutaEuro(_totaleImp) + " " + " IVA: \u20ac " + it.tnx.Util.formatValutaEuro(_totaleIva) + " " + "Totale: \u20ac " + it.tnx.Util.formatValutaEuro(_totale));
                            }
                        });
                    } catch (Exception err) {
                        err.printStackTrace();
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                elen.labTotale.setText("");
                            }
                        });
                    }
                } else {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            elen.labTotale.setText("");
                        }
                    });
                }

                mb.out("dbrefresh tempo");

                caricamento.unlock();
                return null;
            }

            @Override
            public void done() {
                super.done();
//                SwingUtils.mouse_def(_this);
//                griglia.setEnabled(true);

                if (main.pluginEmail) {
                    try {
                        griglia.getColumn("Mail Inviata").setCellRenderer(new EmailCellRenderer());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    griglia.getColumn("Allegati").setCellRenderer(new InvoicexUtil.AllegatiCellRenderer());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (griglia.hasColumn("Anticipata")) {
                        griglia.getColumn("Anticipata").setCellRenderer(new AnticipataCellRenderer());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (oldsel != -1) {
                        //riseleziono
                        int colid = elen.griglia.getColumnByName("id");
                        for (int i = 0; i < elen.griglia.getRowCount(); i++) {
                            if (CastUtils.toInteger(elen.griglia.getValueAt(i, colid)) == oldselid) {
                                elen.griglia.getSelectionModel().setSelectionInterval(i, i);
                                elen.griglia.scrollRectToVisible(old_visiblerect);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!cu.s(filtroTipo.getName()).equals("action")) {
                    InvoicexUtil.aggiungiFiltroSerie(elen.filtroTipo, "test_fatt");
                } else {
                    filtroTipo.setName("");
                }

//                if (locca) {
                lockableUI.setLocked(false);
//                }

                if (runAfter != null) {
                    try {
                        runAfter.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        worker.execute();
    }

    private boolean controllaAperFatt() {

//        java.awt.Component[] arrComp = this.getParent().getComponents();
//        int flag_aperta = 0;
//        
//        for (int i = 0; i < arrComp.length; i++) {
//            
//            if (arrComp[i].getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFatt")) {
//                flag_aperta = 1;
//            }
//        }
//        
//        if (flag_aperta == 1) {
//            javax.swing.JOptionPane.showMessageDialog(this, "Prima di aprire una fattura chiudere quelle gia' aperte", "Attenzione", javax.swing.JOptionPane.WARNING_MESSAGE);
//            
//            return true;
//        }
//        
        return false;
    }
}
