/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui;

import com.jidesoft.hints.AbstractListIntelliHints;
import com.lowagie.text.Cell;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfPCell;
import gestioneFatture.ClienteHint;
import gestioneFatture.CoordinateBancarie;
import gestioneFatture.InvoicexEvent;
import gestioneFatture.Menu;
import gestioneFatture.Scadenze;
import gestioneFatture.Util;
import gestioneFatture.dbFattura;
import gestioneFatture.dbOrdine;
import gestioneFatture.diaDistRiba;
import gestioneFatture.frmElenFatt;
import gestioneFatture.frmPagaPart;
import gestioneFatture.frmTestDocu;
import gestioneFatture.frmTestFatt;
import gestioneFatture.frmTestFattAcquisto;
import gestioneFatture.frmTestOrdine;
import gestioneFatture.main;
import gestioneFatture.prnDistRb;
import it.tnx.Db;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.KeyValuePair;
import it.tnx.commons.MicroBench;
import it.tnx.commons.RunnableWithArgs;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.commons.swing.DelayedExecutor;
import it.tnx.dbeans.pdfPrint.PrintSimpleTable;
import it.tnx.invoicex.InvoicexUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.jdesktop.swingworker.SwingWorker;
import tnxbeans.tnxDbGrid;

/**
 *
 * @author Marco
 */
public class JInternalFrameScadenzario extends javax.swing.JInternalFrame {

    int order;
    String orderClause = "";
    int rigaAttuale = 0;
    AbstractListIntelliHints alRicercaCliente = null;
    boolean tutti_i_clienti = false;
    Integer cliente_selezionato = null;

    boolean opening = true;

    boolean situazioneClienti = true;
    boolean situazioneFornitori = false;
    boolean situazioneOrdini = false;
    boolean fatturati = true;
    String sql;

    /**
     * Creates new form JInternalFrameSituazioneClientiFornitori
     */
    public JInternalFrameScadenzario() {
        initComponents();
        
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/scadenzario2.png")));

        if (!(main.fileIni.getValueBoolean("pref", "scadenzeOrdini", false))) {
            radOrdini.setVisible(false);
        }
        if (!main.pluginRiba) {
            segnaposto_riba.setVisible(false);
        }

        if (main.fileIni.getValueBoolean("pref", "ColAgg_Anticipata", false) == false) {
            fattureAnticipate.setVisible(false);
            conto_anticipazione.setVisible(false);
            comContoAnticipazione.setVisible(false);
        }

        Vector v = new Vector();
        KeyValuePair kv1 = new KeyValuePair("", "<Non Ordinare>");
        KeyValuePair kv2 = new KeyValuePair("clie_forn.ragione_sociale, scadenze.data_scadenza", "Cliente, Data Scadenza");
        KeyValuePair kv3 = new KeyValuePair("clie_forn.ragione_sociale, t.data", "Cliente, Data Documento");
        KeyValuePair kv4 = new KeyValuePair("scadenze.data_scadenza", "Data Scadenza");
        KeyValuePair kv5 = new KeyValuePair("t.data", "Data Documento");
        KeyValuePair kv6 = new KeyValuePair("t.anno, t.serie, t.numero, scadenze.numero", "Numero documento");
        v.add(kv1);
        v.add(kv2);
        v.add(kv3);
        v.add(kv4);
        v.add(kv5);
        v.add(kv6);

        SwingUtils.initJComboFromKVList(ordine, v);
        ordine.setSelectedIndex(1);

        try {
            InvoicexEvent event = new InvoicexEvent(this);
            event.type = InvoicexEvent.TYPE_SITUAZIONECLIENTI_CONSTR_PRE_INIT_COMPS;
            main.events.fireInvoicexEvent(event);
        } catch (Exception err) {
            err.printStackTrace();
        }
        texClie.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                alRicercaCliente.showHints();
                texClie.selectAll();
            }
        });

        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setDismissDelay(10000);

        if (main.fileIni.getValueBoolean("pref", "scadenzeOrdini", false)) {
            groTipoSituazione.add(radOrdini);
        }
        radClienti.setSelected(true);

        numInte.setSelected(true);

        if (main.fileIni.getValueBoolean("pref", "ColAgg_RiferimentoCliente", false)) {
            menColAggRiferimentoCliente.setSelected(true);
        }

        controllaPanPrefExtra();

        griglia.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                order = griglia.getTableHeader().columnAtPoint(new Point(x, y));
            }
        });
//        griglia.setFont(griglia.getFont().deriveFont(griglia.getFont().getSize2D() + 1f));
        griglia.setRowHeight(griglia.getRowHeight() + 4);
//        griglia.setIntercellSpacing(new Dimension(4, 4));

        if (main.pluginEmail) {
            griglia.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int r = griglia.rowAtPoint(e.getPoint());
                    int c = griglia.columnAtPoint(e.getPoint());
                    try {
                        if (c == griglia.getColumn("Email Inviata").getModelIndex() && CastUtils.toInteger0(griglia.getValueAt(r, c)) >= 1) {
                            griglia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        } else {
                            griglia.setCursor(Cursor.getDefaultCursor());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        }

        try {
            InvoicexEvent event = new InvoicexEvent(this);
            event.type = InvoicexEvent.TYPE_SITUAZIONECLIENTI_CONSTR_POST_INIT_COMPS;
            main.events.fireInvoicexEvent(event);
        } catch (Exception err) {
            err.printStackTrace();
        }

        //
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                init();
            }
        });

        alRicercaCliente = new AbstractListIntelliHints(texClie) {
            String current_search = "";

            @Override
            protected JList createList() {
                final JList list = super.createList();
                list.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        String img, tipo;
                        tipo = ((ClienteHint) value).toString();
                        JLabel lab = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (index == 0) {
                            return lab;
                        }
                        String word = current_search.toLowerCase();
                        String content = tipo.toLowerCase();
                        Color c = lab.getBackground();
                        c = c.darker();
                        String rgb = Integer.toHexString(c.getRGB());
                        rgb = rgb.substring(2, rgb.length());
                        content = StringUtils.replace(content, word, "<span style='background-color: " + rgb + "'>" + word + "</span>");
                        lab.setText("<html>" + content + "</html>");
                        System.out.println("lab " + index + " text:" + content);
                        return lab;
                    }
                });
                return list;
            }

            public boolean updateHints(Object arg0) {
//                if (arg0.toString().trim().length() <= 1) return false;
                SwingUtils.mouse_wait();
                current_search = arg0.toString();
                Connection conn;
                try {
                    conn = Db.getConn();

                    String sql = ""
                            + "SELECT codice, ragione_sociale FROM clie_forn"
                            + " where codice like '%" + Db.aa(current_search) + "%'"
                            + " or ragione_sociale like '%" + Db.aa(current_search) + "%'"
                            + " order by ragione_sociale, codice limit 50";

                    System.out.println("sql ricerca:" + sql);
                    ResultSet rs = DbUtils.tryOpenResultSet(conn, sql);
                    Vector v = new Vector();

                    ClienteHint cliente_tutti = new ClienteHint();
                    cliente_tutti.codice = "*";
                    cliente_tutti.ragione_sociale = "<tutti>";
                    v.add(cliente_tutti);

                    while (rs.next()) {
                        ClienteHint cliente = new ClienteHint();
                        cliente.codice = rs.getString(1);
                        cliente.ragione_sociale = rs.getString(2);
                        v.add(cliente);
                    }
                    setListData(v);
                    rs.getStatement().close();
                    rs.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                SwingUtils.mouse_def();
                return true;
            }

            @Override
            public void acceptHint(Object arg0) {
//                super.acceptHint(arg0);
                try {
                    if (((ClienteHint) arg0).codice.equals("*")) {
                        tutti_i_clienti = true;
                        cliente_selezionato = null;
                    } else {
                        tutti_i_clienti = false;
                        cliente_selezionato = CastUtils.toInteger(((ClienteHint) arg0).codice);
                    }
                    texClie.setText(((ClienteHint) arg0).toString());
                    comPagamento.requestFocus();
                    selezionaSituazione();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        aggiornaOrdine();

        texDal.getEditor().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                selezionaSituazione();
            }
        });
        texAl.getEditor().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                selezionaSituazione();
            }
        });
        texDalDoc.getEditor().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                selezionaSituazione();
            }
        });
        texAlDoc.getEditor().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                selezionaSituazione();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpOrdine = new javax.swing.ButtonGroup();
        groTipoSituazione = new javax.swing.ButtonGroup();
        groNumeroInteEste = new javax.swing.ButtonGroup();
        pop = new javax.swing.JPopupMenu();
        menApriScadenza = new javax.swing.JMenuItem();
        menApriDocumento = new javax.swing.JMenuItem();
        menColAgg = new javax.swing.JMenu();
        menColAggRiferimentoCliente = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        menExp = new javax.swing.JMenuItem();
        butPrin = new javax.swing.JButton();
        butPrinDistinta = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        radClienti = new javax.swing.JRadioButton();
        radFornitori = new javax.swing.JRadioButton();
        radOrdini = new javax.swing.JRadioButton();
        jSeparator2 = new javax.swing.JSeparator();
        numInte = new javax.swing.JRadioButton();
        numEste = new javax.swing.JRadioButton();
        numEntrambi = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        segnaposto_riba = new javax.swing.JButton();
        panGriglia = new javax.swing.JPanel();
        labTotale = new javax.swing.JLabel();
        panHeaderGriglia = new javax.swing.JPanel();
        labScadenza = new javax.swing.JLabel();
        labDocumento = new javax.swing.JLabel();
        scrollGriglia = new javax.swing.JScrollPane();
        griglia = getGriglia();
        panelFiltri = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        labCliente = new javax.swing.JLabel();
        labCategoriaCliente = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        ordine = new javax.swing.JComboBox();
        radCresc = new javax.swing.JRadioButton();
        radDescr = new javax.swing.JRadioButton();
        butSeleTutt = new javax.swing.JButton();
        butDeseTutt = new javax.swing.JButton();
        butRefresh = new javax.swing.JButton();
        texClie = new javax.swing.JTextField();
        comCategoriaClifor = new tnxbeans.tnxComboField();
        comPagamento = new tnxbeans.tnxComboField();
        texDalDoc = new org.jdesktop.swingx.JXDatePicker();
        texAlDoc = new org.jdesktop.swingx.JXDatePicker();
        comBanca = new tnxbeans.tnxComboField();
        riferimento = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        texDal = new org.jdesktop.swingx.JXDatePicker();
        jLabel13 = new javax.swing.JLabel();
        texAl = new org.jdesktop.swingx.JXDatePicker();
        conto_anticipazione = new javax.swing.JLabel();
        comContoAnticipazione = new tnxbeans.tnxComboField();
        jPanel1 = new javax.swing.JPanel();
        fattureAnticipate = new javax.swing.JCheckBox();
        nonFatturati = new javax.swing.JCheckBox();
        chePaga = new javax.swing.JCheckBox();
        cheStam = new javax.swing.JCheckBox();
        cheVisNoteDiCredito = new javax.swing.JCheckBox();
        cheVisProforma = new javax.swing.JCheckBox();
        cheVisDaInviare = new javax.swing.JCheckBox();
        segna_posto_invia_sollecito = new javax.swing.JLabel();
        labAgente = new javax.swing.JLabel();
        comAgente = new tnxbeans.tnxComboField();

        menApriScadenza.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/scadenzario2.png"))); // NOI18N
        menApriScadenza.setText("Apri Scadenza");
        menApriScadenza.setToolTipText("");
        menApriScadenza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menApriScadenzaActionPerformed(evt);
            }
        });
        pop.add(menApriScadenza);

        menApriDocumento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/apps/accessories-text-editor.png"))); // NOI18N
        menApriDocumento.setText("Apri Documento");
        menApriDocumento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menApriDocumentoActionPerformed(evt);
            }
        });
        pop.add(menApriDocumento);

        menColAgg.setText("Colonne Aggiuntive");

        menColAggRiferimentoCliente.setText("Riferimento Cliente/Fornitore");
        menColAggRiferimentoCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColAggRiferimentoClienteActionPerformed(evt);
            }
        });
        menColAgg.add(menColAggRiferimentoCliente);

        pop.add(menColAgg);
        pop.add(jSeparator4);

        menExp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/mimetypes/x-office-spreadsheet.png"))); // NOI18N
        menExp.setText("Esporta tabella su Excel con dettaglio pagamenti");
        menExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menExpActionPerformed(evt);
            }
        });
        pop.add(menExp);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Scadenzario");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
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

        butPrin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-print.png"))); // NOI18N
        butPrin.setText("Stampa");
        butPrin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPrinActionPerformed(evt);
            }
        });

        butPrinDistinta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-print.png"))); // NOI18N
        butPrinDistinta.setText("Distinta Ri.Ba. per banca");
        butPrinDistinta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPrinDistintaActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        groTipoSituazione.add(radClienti);
        radClienti.setText("Clienti / Riscossioni");
        radClienti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radClientiActionPerformed(evt);
            }
        });

        groTipoSituazione.add(radFornitori);
        radFornitori.setText("Fornitori / Pagamenti");
        radFornitori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radFornitoriActionPerformed(evt);
            }
        });

        groTipoSituazione.add(radOrdini);
        radOrdini.setText("da Ordini e Preventivi di vendita");
        radOrdini.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radOrdiniActionPerformed(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        groNumeroInteEste.add(numInte);
        numInte.setText("Solo numerazione interna");
        numInte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numInteActionPerformed(evt);
            }
        });

        groNumeroInteEste.add(numEste);
        numEste.setText("Solo numerazione esterna");
        numEste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numEsteActionPerformed(evt);
            }
        });

        groNumeroInteEste.add(numEntrambi);
        numEntrambi.setText("Numerazione interna ed esterna");
        numEntrambi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numEntrambiActionPerformed(evt);
            }
        });

        jLabel1.setText("Riferimenti per le fatture di acquisto");

        jLabel2.setText("Scadenze da:");

        segnaposto_riba.setText("segnaposto");
        segnaposto_riba.setName("segnaposto_riba"); // NOI18N

        panGriglia.setLayout(new java.awt.BorderLayout());

        labTotale.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labTotale.setText("...");
        labTotale.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 1, 1, 1));
        panGriglia.add(labTotale, java.awt.BorderLayout.SOUTH);

        panHeaderGriglia.setPreferredSize(new java.awt.Dimension(0, 24));
        panHeaderGriglia.setLayout(null);

        labScadenza.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labScadenza.setText("Scadenza");
        labScadenza.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panHeaderGriglia.add(labScadenza);
        labScadenza.setBounds(30, 0, 55, 25);

        labDocumento.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labDocumento.setText("Documento");
        labDocumento.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panHeaderGriglia.add(labDocumento);
        labDocumento.setBounds(545, 0, 60, 25);

        panGriglia.add(panHeaderGriglia, java.awt.BorderLayout.NORTH);

        griglia.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
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
        griglia.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                grigliaComponentResized(evt);
            }
        });
        scrollGriglia.setViewportView(griglia);

        panGriglia.add(scrollGriglia, java.awt.BorderLayout.CENTER);

        jLabel3.setText("Filtra per:");

        labCliente.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labCliente.setText("Cliente");

        labCategoriaCliente.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labCategoriaCliente.setText("Categoria cliente");

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Pagamento");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Data documento dal");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Banca");

        jLabel8.setText("Al");

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Riferimento");

        jLabel11.setText("Ordina per ");

        ordine.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ordine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ordineActionPerformed(evt);
            }
        });

        grpOrdine.add(radCresc);
        radCresc.setText("Crescente");
        radCresc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radCrescActionPerformed(evt);
            }
        });

        grpOrdine.add(radDescr);
        radDescr.setText("Decrescente");
        radDescr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radDescrActionPerformed(evt);
            }
        });

        butSeleTutt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-select-all.png"))); // NOI18N
        butSeleTutt.setText("Seleziona tutte");
        butSeleTutt.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butSeleTutt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSeleTuttActionPerformed(evt);
            }
        });

        butDeseTutt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-clear.png"))); // NOI18N
        butDeseTutt.setText("Deseleziona tutte");
        butDeseTutt.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butDeseTutt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDeseTuttActionPerformed(evt);
            }
        });

        butRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/view-refresh.png"))); // NOI18N
        butRefresh.setText("Aggiorna");
        butRefresh.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butRefreshActionPerformed(evt);
            }
        });

        texClie.setColumns(30);

        comCategoriaClifor.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comCategoriaCliforItemStateChanged(evt);
            }
        });

        comPagamento.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comPagamentoItemStateChanged(evt);
            }
        });

        texDalDoc.setName("texDalDoc"); // NOI18N
        texDalDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texDalDocActionPerformed(evt);
            }
        });
        texDalDoc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texDalDocFocusLost(evt);
            }
        });
        texDalDoc.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                texDalDocPropertyChange(evt);
            }
        });

        texAlDoc.setName("data"); // NOI18N
        texAlDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texAlDocActionPerformed(evt);
            }
        });
        texAlDoc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texAlDocFocusLost(evt);
            }
        });
        texAlDoc.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                texAlDocPropertyChange(evt);
            }
        });

        comBanca.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comBancaItemStateChanged(evt);
            }
        });

        riferimento.setColumns(30);
        riferimento.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                riferimentoKeyReleased(evt);
            }
        });

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Data scadenza dal");

        texDal.setName("data"); // NOI18N
        texDal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texDalActionPerformed(evt);
            }
        });
        texDal.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texDalFocusLost(evt);
            }
        });
        texDal.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                texDalPropertyChange(evt);
            }
        });

        jLabel13.setText("Al");

        texAl.setName("data"); // NOI18N
        texAl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texAlActionPerformed(evt);
            }
        });
        texAl.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texAlFocusLost(evt);
            }
        });
        texAl.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                texAlPropertyChange(evt);
            }
        });

        conto_anticipazione.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        conto_anticipazione.setText("Conto Anticipazione");

        comContoAnticipazione.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comContoAnticipazioneItemStateChanged(evt);
            }
        });

        fattureAnticipate.setText("Solo Fatture Anticipate");
        fattureAnticipate.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        fattureAnticipate.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        fattureAnticipate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fattureAnticipateItemStateChanged(evt);
            }
        });

        nonFatturati.setText("Solo non fatturati");
        nonFatturati.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        nonFatturati.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        nonFatturati.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                nonFatturatiItemStateChanged(evt);
            }
        });

        chePaga.setText("Solo da pagare");
        chePaga.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        chePaga.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        chePaga.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chePagaItemStateChanged(evt);
            }
        });
        chePaga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chePagaActionPerformed(evt);
            }
        });

        cheStam.setText("Solo da stampare");
        cheStam.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cheStam.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        cheStam.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cheStamItemStateChanged(evt);
            }
        });

        cheVisNoteDiCredito.setText("Vis. da note di credito");
        cheVisNoteDiCredito.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cheVisNoteDiCredito.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        cheVisNoteDiCredito.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cheVisNoteDiCreditoItemStateChanged(evt);
            }
        });

        cheVisProforma.setText("Vis. da proforma");
        cheVisProforma.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cheVisProforma.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        cheVisProforma.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cheVisProformaItemStateChanged(evt);
            }
        });

        cheVisDaInviare.setText("Email non inviata");
        cheVisDaInviare.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cheVisDaInviare.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        cheVisDaInviare.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cheVisDaInviareItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 276, Short.MAX_VALUE)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cheStam)
                            .add(chePaga)
                            .add(nonFatturati))
                        .add(fattureAnticipate))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(cheVisDaInviare)
                        .add(cheVisProforma)
                        .add(cheVisNoteDiCredito))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {chePaga, cheStam, nonFatturati}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.linkSize(new java.awt.Component[] {cheVisDaInviare, cheVisNoteDiCredito, cheVisProforma}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 104, Short.MAX_VALUE)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel1Layout.createSequentialGroup()
                    .add(2, 2, 2)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(cheStam)
                        .add(cheVisNoteDiCredito))
                    .add(1, 1, 1)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(chePaga)
                        .add(cheVisProforma))
                    .add(1, 1, 1)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(nonFatturati)
                        .add(cheVisDaInviare))
                    .add(1, 1, 1)
                    .add(fattureAnticipate)
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        segna_posto_invia_sollecito.setText(" ");

        labAgente.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labAgente.setText("Agente");

        comAgente.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comAgenteItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout panelFiltriLayout = new org.jdesktop.layout.GroupLayout(panelFiltri);
        panelFiltri.setLayout(panelFiltriLayout);
        panelFiltriLayout.setHorizontalGroup(
            panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelFiltriLayout.createSequentialGroup()
                .addContainerGap()
                .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panelFiltriLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(panelFiltriLayout.createSequentialGroup()
                                .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(panelFiltriLayout.createSequentialGroup()
                                        .add(jLabel12)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texDal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel13)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texAl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(panelFiltriLayout.createSequentialGroup()
                                        .add(labCliente)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(panelFiltriLayout.createSequentialGroup()
                                        .add(jLabel6)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(comPagamento, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .add(panelFiltriLayout.createSequentialGroup()
                                        .add(jLabel7)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texDalDoc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel8)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texAlDoc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(panelFiltriLayout.createSequentialGroup()
                                        .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(labAgente)
                                            .add(labCategoriaCliente))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(comCategoriaClifor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(comAgente, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .add(18, 18, 18)
                                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(panelFiltriLayout.createSequentialGroup()
                                    .add(conto_anticipazione, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(comContoAnticipazione, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 246, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, panelFiltriLayout.createSequentialGroup()
                                        .add(jLabel9)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(comBanca, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, panelFiltriLayout.createSequentialGroup()
                                        .add(jLabel10)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(riferimento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap(200, Short.MAX_VALUE))
                    .add(panelFiltriLayout.createSequentialGroup()
                        .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, panelFiltriLayout.createSequentialGroup()
                                .add(jLabel11)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(ordine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(radCresc)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(radDescr)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 270, Short.MAX_VALUE)
                                .add(butSeleTutt)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(butDeseTutt)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(butRefresh)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(segna_posto_invia_sollecito))
                            .add(panelFiltriLayout.createSequentialGroup()
                                .add(jLabel3)
                                .add(0, 744, Short.MAX_VALUE)))
                        .addContainerGap())))
        );

        panelFiltriLayout.linkSize(new java.awt.Component[] {conto_anticipazione, jLabel10, jLabel12, jLabel3, jLabel6, jLabel7, jLabel9, labCategoriaCliente, labCliente}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        panelFiltriLayout.linkSize(new java.awt.Component[] {texDal, texDalDoc}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        panelFiltriLayout.linkSize(new java.awt.Component[] {texAl, texAlDoc}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        panelFiltriLayout.linkSize(new java.awt.Component[] {comBanca, comCategoriaClifor, comContoAnticipazione, comPagamento, riferimento, texClie}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        panelFiltriLayout.setVerticalGroup(
            panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, panelFiltriLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(jLabel3)
                .add(1, 1, 1)
                .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panelFiltriLayout.createSequentialGroup()
                        .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(labCliente)
                            .add(texClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(1, 1, 1)
                        .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(labCategoriaCliente)
                            .add(comCategoriaClifor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(1, 1, 1)
                        .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(labAgente)
                            .add(comAgente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(1, 1, 1)
                        .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel6)
                            .add(comPagamento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(1, 1, 1)
                        .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel7)
                            .add(jLabel8)
                            .add(texDalDoc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(texAlDoc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(2, 2, 2)
                        .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel12)
                            .add(jLabel13)
                            .add(texDal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(texAl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(1, 1, 1)
                .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(comBanca, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(1, 1, 1)
                .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(riferimento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(1, 1, 1)
                .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(conto_anticipazione)
                    .add(comContoAnticipazione, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(panelFiltriLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(butRefresh)
                    .add(butDeseTutt)
                    .add(butSeleTutt)
                    .add(jLabel11)
                    .add(ordine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(radCresc)
                    .add(radDescr)
                    .add(segna_posto_invia_sollecito))
                .add(0, 0, 0))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panGriglia, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jSeparator3)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(butPrin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(butPrinDistinta, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(segnaposto_riba, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(7, 7, 7)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(radClienti)
                            .add(radFornitori)
                            .add(radOrdini))
                        .add(6, 6, 6)
                        .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(numInte)
                            .add(jLabel1)
                            .add(numEste)
                            .add(numEntrambi))
                        .add(0, 0, Short.MAX_VALUE))
                    .add(panelFiltri, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {butPrin, butPrinDistinta, segnaposto_riba}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jSeparator1)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(butPrin)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(butPrinDistinta))
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(numInte)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(numEste)))
                        .add(0, 0, 0)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(numEntrambi)
                            .add(segnaposto_riba)))
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(radClienti)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(radFornitori)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(radOrdini))
                    .add(jSeparator2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelFiltri, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panGriglia, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void texDalDocPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_texDalDocPropertyChange
        System.out.println("evt = " + evt);
    }//GEN-LAST:event_texDalDocPropertyChange

    private void texAlDocPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_texAlDocPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_texAlDocPropertyChange

    private void texDalPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_texDalPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_texDalPropertyChange

    private void texAlPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_texAlPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_texAlPropertyChange

    private void radClientiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radClientiActionPerformed
        numInte.setSelected(true);
        selezionaSituazione();
    }//GEN-LAST:event_radClientiActionPerformed

    private void radFornitoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radFornitoriActionPerformed
        numEntrambi.setSelected(true);
        selezionaSituazione();
    }//GEN-LAST:event_radFornitoriActionPerformed

    private void radOrdiniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radOrdiniActionPerformed
        numInte.setSelected(true);
        selezionaSituazione();
    }//GEN-LAST:event_radOrdiniActionPerformed

    private void numEntrambiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numEntrambiActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_numEntrambiActionPerformed

    private void numInteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numInteActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_numInteActionPerformed

    private void numEsteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numEsteActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_numEsteActionPerformed

    private void butRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butRefreshActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_butRefreshActionPerformed

    private void menApriDocumentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menApriDocumentoActionPerformed
        if (griglia.getSelectedRowCount() <= 0) {
            SwingUtils.showErrorMessage(getTopLevelAncestor(), "Seleziona un documento prima!");
            return;
        }

        getTopLevelAncestor().setCursor(new Cursor(Cursor.WAIT_CURSOR));

//        controllo se sono aperti altri frmTestFatt
        String dbSerie = "";
        int dbNumero = 0;
        int dbAnno = 0;
        Integer dbId = null;
        DebugUtils.dumpJTable(griglia, griglia.getSelectedRow());
        if (radFornitori.isSelected()) {
            dbSerie = String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("serie2")));
            dbNumero = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("numero2"))));
            dbAnno = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("anno2"))));
        } else {
            dbSerie = String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("serie")));
            dbNumero = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("numero"))));
            dbAnno = Integer.parseInt(String.valueOf(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("anno"))));
        }

        JInternalFrame frm = null;

        if (radClienti.isSelected()) {
            dbId = InvoicexUtil.getIdFattura(dbSerie, dbNumero, dbAnno);
            frm = new frmTestFatt();
            ((frmTestFatt) frm).init(frmTestDocu.DB_MODIFICA, dbSerie, dbNumero, "P", dbAnno, dbFattura.TIPO_FATTURA_NON_IDENTIFICATA, dbId);
        } else if (radOrdini.isSelected()) {
            dbId = InvoicexUtil.getIdOrdine(dbSerie, dbNumero, dbAnno);
            frm = new frmTestOrdine(frmTestOrdine.DB_MODIFICA, dbSerie, dbNumero, "P", dbAnno, dbOrdine.TIPO_ORDINE, dbId);
        } else if (radFornitori.isSelected()) {
            dbId = InvoicexUtil.getIdFatturaAcquisto(dbSerie, dbNumero, dbAnno);
            frm = new frmTestFattAcquisto(frmTestFatt.DB_MODIFICA, dbSerie, dbNumero, "P", dbAnno, dbFattura.TIPO_FATTURA_ACQUISTO, dbId);
        }

        Menu m = (Menu) main.getPadre();
        m.openFrame(frm, 740, InvoicexUtil.getHeightIntFrame(750));

        getTopLevelAncestor().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menApriDocumentoActionPerformed

    private void menColAggRiferimentoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColAggRiferimentoClienteActionPerformed
        System.out.println("ColAgg_RiferimentoCliente = " + menColAggRiferimentoCliente.isSelected());
        main.fileIni.setValue("pref", "ColAgg_RiferimentoCliente", menColAggRiferimentoCliente.isSelected());
        controllaPanPrefExtra();
        selezionaSituazione();
    }//GEN-LAST:event_menColAggRiferimentoClienteActionPerformed

    private void riferimentoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_riferimentoKeyReleased
        delay_rif.update();
    }//GEN-LAST:event_riferimentoKeyReleased

    private void butSeleTuttActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSeleTuttActionPerformed
        ListSelectionModel sele = this.griglia.getSelectionModel();
        sele.setSelectionInterval(0, this.griglia.getRowCount() - 1);
        this.griglia.setSelectionModel(sele);
    }//GEN-LAST:event_butSeleTuttActionPerformed

    private void butDeseTuttActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDeseTuttActionPerformed
        ListSelectionModel sele = this.griglia.getSelectionModel();
        sele.clearSelection();
        this.griglia.setSelectionModel(sele);
    }//GEN-LAST:event_butDeseTuttActionPerformed

    private void butPrinDistintaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPrinDistintaActionPerformed
        stampaDistinta();
    }//GEN-LAST:event_butPrinDistintaActionPerformed

    private void cheStamItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cheStamItemStateChanged
        selezionaSituazione();
    }//GEN-LAST:event_cheStamItemStateChanged

    private void chePagaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chePagaItemStateChanged
        selezionaSituazione();
    }//GEN-LAST:event_chePagaItemStateChanged

    private void nonFatturatiItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_nonFatturatiItemStateChanged
        if (nonFatturati.isSelected()) {
            fatturati = false;
        } else {
            fatturati = true;
        }
        selezionaSituazione();
    }//GEN-LAST:event_nonFatturatiItemStateChanged


    private void comBancaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comBancaItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            selezionaSituazione();
        }
    }//GEN-LAST:event_comBancaItemStateChanged

    private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameOpened
        griglia.resizeColumnsPerc(true);
    }//GEN-LAST:event_formInternalFrameOpened

    private void butPrinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPrinActionPerformed
        String nomeStampa = "scadenzario";

        //chiedere parametri stampa        
        JDialogParamStampa dParam = new JDialogParamStampa(main.getPadreFrame(), true);
        dParam.init(nomeStampa);
        dParam.setLocationRelativeTo(null);
        dParam.setVisible(true);
        if (!dParam.confermato) {
            return;
        }
        final Integer carattere = cu.i0(main.fileIni.getValue("pref", "param_stampa_" + nomeStampa + "_carattere", "0"));

        int[] hw = new int[griglia.getColumnCount()];
        for (int i = 0; i < griglia.getColumnCount(); i++) {
            try {
                hw[i] = ((Double) griglia.columnsSizePerc.get(griglia.getColumnName(i))).intValue();
            } catch (Exception e) {
                System.out.println("e:" + e + " i:" + i);
                hw[i] = 10;
            }
        }

        String header = "";
        if (situazioneClienti || situazioneOrdini) {
            header = "Situazione cliente : " + this.texClie.getText();
        } else {
            header = "Situazione fornitore : " + this.texClie.getText();
        }
        if (comCategoriaClifor.getSelectedIndex() > 0) {
            if (situazioneClienti || situazioneOrdini) {
                header += "\nCategoria cliente : " + comCategoriaClifor.getSelectedItem();
            } else {
                header += "\nCategoria fornitore : " + comCategoriaClifor.getSelectedItem();
            }
        }
        if (comAgente.getSelectedIndex() > 0) {
            header += "\nAgente : " + comAgente.getSelectedItem();
        }        
        if (texDal.getDate() != null || texAl.getDate() != null) {
            header += "\nPeriodo scadenze dal : " + DateUtils.formatDateIta(texDal.getDate()) + "  al : " + DateUtils.formatDateIta(texAl.getDate());
        }
        if (texDalDoc.getDate() != null || texAlDoc.getDate() != null) {
            header += "\nPeriodo documenti dal : " + DateUtils.formatDateIta(texDalDoc.getDate()) + "  al : " + DateUtils.formatDateIta(texAlDoc.getDate());
        }

        tnxDbGrid.MyCallable intestazione = new tnxDbGrid.MyCallable() {

            @Override
            public Object call(Table datatable) throws Exception {
                com.lowagie.text.Font bf = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8 + carattere, com.lowagie.text.Font.BOLD);

                Cell tempPdfCell = new Cell(new Phrase("Scadenza", bf));
                tempPdfCell.setColspan(6);
                tnxDbGrid.set1(tempPdfCell);
                tempPdfCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                datatable.addCell(tempPdfCell);

                Cell tempPdfCell2 = new Cell(new Phrase("Fattura", bf));
                tempPdfCell2.setColspan(datatable.getColumns() - 6);
                tnxDbGrid.set1(tempPdfCell2);
                tempPdfCell2.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                datatable.addCell(tempPdfCell2);

                return null;
            }

        };

        tnxDbGrid.MyCallableProcessValue procvalue = null;
        if (main.pluginEmail) {
            procvalue = new tnxDbGrid.MyCallableProcessValue() {
                Integer colemail = null;

                @Override
                public Object call(Table datatable, int row, int col, Object value) throws Exception {
                    if (colemail == null) {
                        colemail = griglia.getTableHeader().getColumnModel().getColumnIndex("Email Inviata");
                    }
                    if (col == colemail) {
                        if (cu.s(value).equals("1")) {
                            return "sì";
                        }
                        return "";
                    }
                    return value;
                }
            };
        }

        String file = griglia.stampaTabella(header, hw, this.labTotale.getText(), "Stampato in data " + DateUtils.formatDateTime(new Date()), intestazione, nomeStampa, procvalue);

        System.out.println("apro:" + file);
//        SwingUtils.open(new File(file));
        Util.start(file);
    }//GEN-LAST:event_butPrinActionPerformed

    private void ordineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ordineActionPerformed
        aggiornaOrdine();
    }//GEN-LAST:event_ordineActionPerformed

    private void radCrescActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radCrescActionPerformed
        aggiornaOrdine();
    }//GEN-LAST:event_radCrescActionPerformed

    private void radDescrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDescrActionPerformed
        aggiornaOrdine();
    }//GEN-LAST:event_radDescrActionPerformed

    private void chePagaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chePagaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chePagaActionPerformed

    private void cheVisNoteDiCreditoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cheVisNoteDiCreditoItemStateChanged
        selezionaSituazione();
    }//GEN-LAST:event_cheVisNoteDiCreditoItemStateChanged

    private void cheVisProformaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cheVisProformaItemStateChanged
        selezionaSituazione();
    }//GEN-LAST:event_cheVisProformaItemStateChanged

    private void cheVisDaInviareItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cheVisDaInviareItemStateChanged
        selezionaSituazione();
    }//GEN-LAST:event_cheVisDaInviareItemStateChanged

    private void comPagamentoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comPagamentoItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            selezionaSituazione();
        }
    }//GEN-LAST:event_comPagamentoItemStateChanged

    private void texDalDocFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texDalDocFocusLost
        selezionaSituazione();
    }//GEN-LAST:event_texDalDocFocusLost

    private void texAlDocFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texAlDocFocusLost
        selezionaSituazione();
    }//GEN-LAST:event_texAlDocFocusLost

    private void texDalFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texDalFocusLost
        selezionaSituazione();
    }//GEN-LAST:event_texDalFocusLost

    private void texAlFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texAlFocusLost
        selezionaSituazione();
    }//GEN-LAST:event_texAlFocusLost

    private void grigliaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseClicked
        try {
            System.out.println("situazione clienti fornitori click");

            int r = griglia.rowAtPoint(evt.getPoint());
            int c = griglia.columnAtPoint(evt.getPoint());
            if (griglia.hasColumn("Email Inviata") && main.pluginEmail && c == griglia.getColumn("Email Inviata").getModelIndex() && CastUtils.toInteger0(griglia.getValueAt(r, c)) >= 1) {
                HashMap params = new HashMap();
                params.put("source", this);
                params.put("tipo", "Sollecito");
                params.put("id", CastUtils.toInteger(griglia.getValueAt(r, griglia.getColumnByName("id"))));
                InvoicexEvent event = new InvoicexEvent(params);
                event.type = InvoicexEvent.TYPE_EMAIL_STORICO;
                main.events.fireInvoicexEvent(event);
                return;
            }

            if (evt.getClickCount() == 2) {
                apriScadenza();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_grigliaMouseClicked

    private void grigliaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMousePressed
        if (evt.isPopupTrigger()) {
            System.out.println("popup");
            pop.show(griglia, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_grigliaMousePressed

    private void grigliaMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseReleased
        if (evt.isPopupTrigger()) {
            System.out.println("popup");
            pop.show(griglia, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_grigliaMouseReleased

    private void grigliaComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_grigliaComponentResized
        moveLabDocumento(null);
    }//GEN-LAST:event_grigliaComponentResized

    private void comCategoriaCliforItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comCategoriaCliforItemStateChanged
        selezionaSituazione();
    }//GEN-LAST:event_comCategoriaCliforItemStateChanged

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        salvaImpo();
    }//GEN-LAST:event_formInternalFrameClosing

    private void texDalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texDalActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_texDalActionPerformed

    private void texAlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texAlActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_texAlActionPerformed

    private void texDalDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texDalDocActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_texDalDocActionPerformed

    private void texAlDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texAlDocActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_texAlDocActionPerformed

    private void menApriScadenzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menApriScadenzaActionPerformed
        apriScadenza();
    }//GEN-LAST:event_menApriScadenzaActionPerformed

    private void fattureAnticipateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fattureAnticipateItemStateChanged
        selezionaSituazione();
    }//GEN-LAST:event_fattureAnticipateItemStateChanged

    private void comContoAnticipazioneItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comContoAnticipazioneItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            selezionaSituazione();
        }
    }//GEN-LAST:event_comContoAnticipazioneItemStateChanged

    private void menExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menExpActionPerformed
        SwingUtils.mouse_wait(this);
        System.out.println("esporta");

        final String sql = griglia.oldSql;
        System.out.println("sql = " + sql);
        final JInternalFrame internalframe = this;

        SwingWorker work = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                ResultSet rs = Db.openResultSet(sql);
                PrintSimpleTable print = new PrintSimpleTable(griglia) {
                    public String printExcel(String title, int[] headerWidth, String note_testa, String note_piede) {
                        Connection connection;
                        java.sql.Statement stat;
                        ResultSet resu;
                        String nomeFileXls = "tempStampa_" + (new java.util.Date()).getTime() + ".xls";

                        try {

                            HSSFSheet sheet = wb.createSheet("invoicex");
                            sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);

                            short contarows = 0;
                            HSSFRow row = sheet.createRow((short) contarows);
                            contarows++;
                            row.createCell((short) 0).setCellValue(title);

                            if (note_testa != null && note_testa.length() > 0) {
                                row = sheet.createRow((short) contarows);
                                contarows++;
                                row = sheet.createRow((short) contarows);
                                contarows++;
                                row.createCell((short) 0).setCellValue(note_testa);
                            }

                            row = sheet.createRow((short) contarows);
                            contarows++;
                            //colonne
                            row = sheet.createRow((short) contarows);
                            contarows++;
                            int columns = 0;
                            if (table != null) {
                                columns = table.getColumnCount();
                            } else {
                                columns = rs.getMetaData().getColumnCount();
                            }
                            columns += 3;

                            if (headerWidth == null && table != null) {
                                headerWidth = new int[columns];
                                int i = 0;
                                for (i = 0; i < columns - 3; i++) {
                                    headerWidth[i] = table.getColumnModel().getColumn(i).getWidth() / 7;
                                }
                                headerWidth[i + 0] = 10;
                                headerWidth[i + 1] = 10;
                                headerWidth[i + 2] = 10;
                            }

                            for (int i = 0; i < columns; i++) {
                                String col = "";
                                if (i < (columns - 3)) {
                                    if (table != null) {
                                        col = table.getColumnName(i);
                                    } else {
                                        col = rs.getMetaData().getColumnLabel(i + 1);
                                    }
                                } else {
                                    if (i == columns - 3) {
                                        col = "Data pagamento";
                                    } else if (i == columns - 2) {
                                        col = "Importo pagato";
                                    } else if (i == columns - 1) {
                                        col = "";
                                    }
                                }
                                row.createCell((short) i).setCellValue(col);

                                if (headerWidth != null) {
                                    sheet.setColumnWidth((short) i, (short) (headerWidth[i] * 300));
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
                                row = sheet.createRow((short) contarows);
                                contarows++;
                                //colonne
                                for (int i = 0; i < columns - 3; i++) {
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
                                        row.createCell((short) i).setCellValue("");
                                    } else {
                                        setCell(o, row, i);
                                    }
                                    oldrow.put(i, o);
                                }
                                //altre tre colonne
                                //recuperare i dati dei pagamenti da scadenze_parziali
                                String sql = "select * from scadenze_parziali where id_scadenza = " + dbu.sql(oldrow.get(0));
                                List<Map> pags = dbu.getListMap(Db.getConn(), sql);
                                if (pags != null && pags.size() > 0) {
                                    for (int i = 0; i < pags.size(); i++) {
                                        if (i > 0) {
                                            row = sheet.createRow((short) contarows);
                                            contarows++;
                                        }
                                        Map m = pags.get(i);
                                        setCell(m.get("data"), row, (short) columns - 3);
                                        setCell(m.get("importo"), row, (short) columns - 2);
                                        setCell(m.get("note"), row, (short) columns - 1);
                                    }
                                } else {
                                    if (cu.s(oldrow.get(3)).equalsIgnoreCase("S")) {
                                        setCell(oldrow.get(2), row, (short) columns - 3);
                                        setCell(oldrow.get(4), row, (short) columns - 2);
                                    }
                                }
                            }

                            if (note_piede != null && note_piede.length() > 0) {
                                row = sheet.createRow((short) contarows);
                                contarows++;
                                row = sheet.createRow((short) contarows);
                                contarows++;
                                row.createCell((short) 0).setCellValue(note_piede);
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

                };
                String file = print.printExcel("Export", null, "", "");
                return file;
            }

            @Override
            protected void done() {
                SwingUtils.mouse_def(internalframe);
                try {
                    Util.start2(cu.s(get()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        };
        work.execute();

    }//GEN-LAST:event_menExpActionPerformed

    private void comAgenteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comAgenteItemStateChanged
        selezionaSituazione();
    }//GEN-LAST:event_comAgenteItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butDeseTutt;
    private javax.swing.JButton butPrin;
    private javax.swing.JButton butPrinDistinta;
    private javax.swing.JButton butRefresh;
    private javax.swing.JButton butSeleTutt;
    private javax.swing.JCheckBox chePaga;
    public javax.swing.JCheckBox cheStam;
    private javax.swing.JCheckBox cheVisDaInviare;
    private javax.swing.JCheckBox cheVisNoteDiCredito;
    private javax.swing.JCheckBox cheVisProforma;
    private tnxbeans.tnxComboField comAgente;
    private tnxbeans.tnxComboField comBanca;
    private tnxbeans.tnxComboField comCategoriaClifor;
    private tnxbeans.tnxComboField comContoAnticipazione;
    private tnxbeans.tnxComboField comPagamento;
    private javax.swing.JLabel conto_anticipazione;
    private javax.swing.JCheckBox fattureAnticipate;
    public tnxbeans.tnxDbGrid griglia;
    private javax.swing.ButtonGroup groNumeroInteEste;
    private javax.swing.ButtonGroup groTipoSituazione;
    private javax.swing.ButtonGroup grpOrdine;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JLabel labAgente;
    private javax.swing.JLabel labCategoriaCliente;
    private javax.swing.JLabel labCliente;
    private javax.swing.JLabel labDocumento;
    private javax.swing.JLabel labScadenza;
    private javax.swing.JLabel labTotale;
    private javax.swing.JMenuItem menApriDocumento;
    private javax.swing.JMenuItem menApriScadenza;
    private javax.swing.JMenu menColAgg;
    private javax.swing.JCheckBoxMenuItem menColAggRiferimentoCliente;
    private javax.swing.JMenuItem menExp;
    private javax.swing.JCheckBox nonFatturati;
    private javax.swing.JRadioButton numEntrambi;
    private javax.swing.JRadioButton numEste;
    private javax.swing.JRadioButton numInte;
    public javax.swing.JComboBox ordine;
    private javax.swing.JPanel panGriglia;
    private javax.swing.JPanel panHeaderGriglia;
    public javax.swing.JPanel panelFiltri;
    public javax.swing.JPopupMenu pop;
    private javax.swing.JRadioButton radClienti;
    private javax.swing.JRadioButton radCresc;
    private javax.swing.JRadioButton radDescr;
    private javax.swing.JRadioButton radFornitori;
    private javax.swing.JRadioButton radOrdini;
    private javax.swing.JTextField riferimento;
    private javax.swing.JScrollPane scrollGriglia;
    public javax.swing.JLabel segna_posto_invia_sollecito;
    public javax.swing.JButton segnaposto_riba;
    private org.jdesktop.swingx.JXDatePicker texAl;
    private org.jdesktop.swingx.JXDatePicker texAlDoc;
    private javax.swing.JTextField texClie;
    private org.jdesktop.swingx.JXDatePicker texDal;
    private org.jdesktop.swingx.JXDatePicker texDalDoc;
    // End of variables declaration//GEN-END:variables

    public final String SOLO_RIBA = "Solo RIBA";
    public final String SOLO_RID = "Solo RID";
    public final String SOLO_ALTRI = "Solo BONIFICI/RIMESSA DIRETTA/ALTRI";
    public final String TUTTI = "Tutti i tipi di pagamento";
    
    private void init() {
        //apro la griglia
        this.griglia.dbNomeTabella = "";
        this.griglia.flagUsaThread = false;

        java.util.Hashtable colsAlign = new java.util.Hashtable();
        colsAlign.put("totale", "RIGHT_CURRENCY");
        this.griglia.columnsAlign = colsAlign;
//        this.comClie.dbAddElement("<selezionare un cliente/fornitore>", "-1");
//        this.comClie.dbAddElement("<tutti>", "*");
//        this.comClie.dbOpenList(Db.getConn(), "select ragione_sociale, codice from clie_forn order by ragione_sociale", "*", false);
//        this.comClie.dbTrovaMentreScrive = true;
        //105
        comPagamento.addItem(SOLO_RIBA);
        comPagamento.addItem(SOLO_RID);
        comPagamento.addItem(SOLO_ALTRI);
        comPagamento.addItem(TUTTI);

        comBanca.dbAddElement("<tutte le banche>", "*");
        comBanca.dbOpenList(Db.getConn(), "select CONCAT('CC ', cc, ' - ', desc_sint_banca), cc from distinte_riba group by cc, desc_sint_banca", "*", false);
        comBanca.dbTrovaMentreScrive = true;

        comCategoriaClifor.dbAddElement("<tutte le categorie>", "*");
        comCategoriaClifor.dbOpenList(Db.getConn(), "select descrizione,id from tipi_clie_forn", "*", false);
        comCategoriaClifor.dbTrovaMentreScrive = true;

        comAgente.dbAddElement("<tutti gli agenti>", "*");
        comAgente.dbOpenList(Db.getConn(), "select nome,id from agenti order by nome", "*", false);
        comAgente.dbTrovaMentreScrive = true;

        String sqlContiAntic = "SELECT "
                + "CONCAT('CC ', IFNULL(ban.cc,''), ' - ' , IFNULL(abi.nome,''), ' Ag. ', IFNULL(com.comune,''), ', ', IFNULL(cab.indirizzo,'')) as descrizione, "
                + "ban.id as codice "
                + "FROM dati_azienda_banche ban "
                + "LEFT JOIN banche_abi abi ON ban.abi = abi.abi "
                + "LEFT JOIN banche_cab cab ON ban.cab = cab.cab AND ban.abi = cab.abi "
                + "LEFT JOIN comuni com ON cab.codice_comune = com.codice "
                + "INNER JOIN test_fatt fat ON fat.banca_di_anticipazione = ban.id "
                + "GROUP BY ban.id";

        comContoAnticipazione.dbAddElement("<tutti i conti>", "*");
        comContoAnticipazione.dbOpenList(Db.getConn(), sqlContiAntic, "*", false);

        caricaImpo();

        opening = false;

        selezionaSituazione();

//        AutoCompletion.enable(comClie);
    }

    public void selezionaSituazione() {
        selezionaSituazione(false);
    }

    public void selezionaSituazione(boolean seltutte) {
        //Aggiorna        
        if (opening) {
            return;
        }

        if (StringUtils.isBlank(texDal.getEditor().getText())) {
            texDal.setDate(null);
        }
        if (StringUtils.isBlank(texAl.getEditor().getText())) {
            texAl.setDate(null);
        }
        if (StringUtils.isBlank(texDalDoc.getEditor().getText())) {
            texDalDoc.setDate(null);
        }
        if (StringUtils.isBlank(texAlDoc.getEditor().getText())) {
            texAlDoc.setDate(null);
        }

        griglia.getColumnsTitlesDisplayed().put("ndin", "n/n");
        griglia.getColumnsTitlesDisplayed().put("data scadenza", "data");
        griglia.getColumnsTitlesDisplayed().put("numero_interno_display", "num. interno");
        griglia.getColumnsTitlesDisplayed().put("numero_esterno_display", "num. esterno");
        griglia.getColumnsTitlesDisplayed().put("banca", "banca presentazione riba");
        griglia.getColumnsTitlesDisplayed().put("note_pagamento", "note pagamento");
        griglia.getColumnsTitlesDisplayed().put("data_interno", "data");
        griglia.getColumnsTitlesDisplayed().put("data_esterno", "data esterno");
        griglia.getColumnsTitlesDisplayed().put("persona_riferimento", "riferimento");
//        griglia.getColumnsTitlesDisplayed().put("s_pagato", "pagato");
        griglia.getColumnsTitlesDisplayed().put("s_da_pagare", "da pagare");

        java.util.Hashtable colsWidthPerc = new java.util.Hashtable();
        colsWidthPerc.put("id", new Double(0));
        colsWidthPerc.put("ndin", new Double(4));
        colsWidthPerc.put("pagamento", new Double(12));
        colsWidthPerc.put("riba", new Double(0));
        colsWidthPerc.put("abi", new Double(0));
        colsWidthPerc.put("cab", new Double(0));
        colsWidthPerc.put("distinta", new Double(6));
        colsWidthPerc.put("serie", new Double(0));
        colsWidthPerc.put("serie2", new Double(0));
        colsWidthPerc.put("serie_interna", new Double(0));
        colsWidthPerc.put("serie_esterna", new Double(0));
        colsWidthPerc.put("numero", new Double(0));
        colsWidthPerc.put("numero2", new Double(0));
        colsWidthPerc.put("numero_interno", new Double(0));
        colsWidthPerc.put("numero_esterno", new Double(0));
        colsWidthPerc.put("numero_interno_display", new Double(6));
        colsWidthPerc.put("numero_esterno_display", new Double(6));
        colsWidthPerc.put("anno", new Double(0));
        colsWidthPerc.put("anno2", new Double(0));
        colsWidthPerc.put("data", new Double(9));
        colsWidthPerc.put("data_interno", new Double(9));
        colsWidthPerc.put("data_esterno", new Double(9));
        colsWidthPerc.put("totale", new Double(9));
        colsWidthPerc.put("data scadenza", new Double(9));
        colsWidthPerc.put("pagata", new Double(5));
        colsWidthPerc.put("importo", new Double(10));
        colsWidthPerc.put("cliente", new Double(13));
        colsWidthPerc.put("agente", new Double(10));
        colsWidthPerc.put("fornitore", new Double(13));
        colsWidthPerc.put("codice", new Double(0));
        colsWidthPerc.put("banca", new Double(12));
        colsWidthPerc.put("dserie", new Double(0));
        colsWidthPerc.put("dnumero", new Double(0));
        colsWidthPerc.put("danno", new Double(0));
        colsWidthPerc.put("importo_pagato", new Double(0));
        colsWidthPerc.put("riba", new Double(0));
        colsWidthPerc.put("abi", new Double(0));
        colsWidthPerc.put("cab", new Double(0));
        colsWidthPerc.put("iban", new Double(0));
        colsWidthPerc.put("documento_tipo", new Double(0));
        colsWidthPerc.put("note_pagamento", new Double(12));
        colsWidthPerc.put("persona_riferimento", new Double(8));
        colsWidthPerc.put("Email Inviata", new Double(5));
//        colsWidthPerc.put("s_pagato", new Double(8));
        colsWidthPerc.put("s_da_pagare", new Double(8));

        colsWidthPerc.put("t_id", new Double(0));

        if (radClienti.isSelected()) {
            numInte.setEnabled(false);
            numEste.setEnabled(false);
            numEntrambi.setEnabled(false);
            nonFatturati.setEnabled(false);
            fattureAnticipate.setEnabled(true);
            comContoAnticipazione.setEnabled(true);
            situazioneClienti = true;
            situazioneFornitori = false;
            situazioneOrdini = false;
            butPrinDistinta.setEnabled(true);
            comBanca.setEnabled(true);
            labCliente.setText("Cliente");
            labCategoriaCliente.setText("Categoria Cliente");
            
            comAgente.setEnabled(true);

            if (main.pluginEmail) {
                colsWidthPerc.put("Email Inviata", new Double(6));
            }

            try {
                InvoicexEvent event = new InvoicexEvent(this);
                event.type = InvoicexEvent.TYPE_SITUAZIONECLIENTI_ACTIVATE_BTNRIBA;
                main.events.fireInvoicexEvent(event);
            } catch (Exception err) {
                err.printStackTrace();
            }
        } else if (radFornitori.isSelected()) {
            numInte.setEnabled(true);
            numEste.setEnabled(true);
            numEntrambi.setEnabled(true);
            nonFatturati.setEnabled(false);
            fattureAnticipate.setEnabled(false);
            comContoAnticipazione.setEnabled(false);
            situazioneClienti = false;
            situazioneFornitori = true;
            situazioneOrdini = false;
            butPrinDistinta.setEnabled(false);
            comBanca.setEnabled(false);
            comBanca.setSelectedIndex(0);
            labCliente.setText("Fornitore");
            labCategoriaCliente.setText("Categoria Fornitore");
            comAgente.setEnabled(false);

            try {
                InvoicexEvent event = new InvoicexEvent(this);
                event.type = InvoicexEvent.TYPE_SITUAZIONECLIENTI_DEACTIVATE_BTNRIBA;
                main.events.fireInvoicexEvent(event);
            } catch (Exception err) {
                err.printStackTrace();
            }
        } else {
            numInte.setEnabled(false);
            numEste.setEnabled(false);
            numEntrambi.setEnabled(false);
            nonFatturati.setEnabled(true);
            fattureAnticipate.setEnabled(false);
            comContoAnticipazione.setEnabled(false);
            situazioneClienti = false;
            situazioneFornitori = false;
            situazioneOrdini = true;
            butPrinDistinta.setEnabled(false);
            comBanca.setEnabled(false);
            comBanca.setSelectedIndex(0);
            labCliente.setText("Cliente");
            labCategoriaCliente.setText("Categoria Cliente");
            fatturati = nonFatturati.isSelected();
            comAgente.setEnabled(false);

            try {
                InvoicexEvent event = new InvoicexEvent(this);
                event.type = InvoicexEvent.TYPE_SITUAZIONECLIENTI_DEACTIVATE_BTNRIBA;
                main.events.fireInvoicexEvent(event);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }

        griglia.columnsSizePerc = colsWidthPerc;

        //format per data in italiano
        String tab = "test_fatt";
        if (situazioneFornitori) {
            tab = "test_fatt_acquisto";
        }
        if (situazioneOrdini) {
            tab = "test_ordi";
        }

        String campo_totale = "t.totale";
        if (tab.equals("test_fatt")) {
            campo_totale = "if(IFNULL(t.totale_da_pagare,0)>0, t.totale_da_pagare, t.totale) ";
        } else if (situazioneFornitori) {
            campo_totale = "if(IFNULL(t.totale_da_pagare,0)>0, t.totale_da_pagare, t.importo) ";
        }

        String campo_cliente = "cliente";
        if (situazioneFornitori) {
            campo_cliente = "fornitore";
        }
        
        String campo_agente = "";
        if (comAgente.isEnabled()) {
            try {
                int count_agenti = cu.i0(dbu.getObject(Db.getConn(), "select count(*) from agenti"));
                if (count_agenti > 0) {
                    campo_agente = " agenti.nome as agente , ";
                }
            } catch (Exception e) {
            }
        }
        
        String campo_numero = "numero";
        String campo_numero1 = "";
        String campo_numero_display_interno = "cast(concat(CONVERT(t.serie USING utf8), IF(LENGTH(t.serie) > 0, '/', ''), t.numero) as char(50)) as numero_interno_display";
        String campo_numero_display_esterno = "";
        if (situazioneFornitori) {
            campo_numero = "numero_doc as numero_esterno";
            campo_numero1 = "numero as numero_interno";
            campo_numero_display_esterno = "cast(concat(CONVERT(t.serie_doc USING utf8), IF(LENGTH(t.serie_doc) > 0, '/', ''), t.numero_doc) as char(50)) as numero_esterno_display";
        }
        String campo_serie = "serie";
        String campo_serie1 = "";
        if (situazioneFornitori) {
            campo_serie = "serie_doc as serie_esterna";
            campo_serie1 = "serie as serie_interna";
        }
        String agg = "";
        if (situazioneFornitori) {
            agg = ", t.serie as serie2, t.numero as numero2, t.anno as anno2";
        }

        String sql = "select scadenze.id, CAST(CONCAT(scadenze.numero,'/',scadenze.numero_totale) as char(10)) as ndin ";
        sql += ", scadenze.data_scadenza as 'data scadenza', scadenze.pagata";
        if (situazioneClienti) {
            sql += ", scadenze.importo * (IF(t.tipo_fattura = " + dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO + ",-1,1)) as importo";
        } else {
            sql += ", scadenze.importo * (IF(t.tipo_fattura = " + dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO + ",-1,1)) as importo";
        }
//        sql += ", scadenze.importo_pagato as s_pagato, scadenze.importo_da_pagare as s_da_pagare ";
        sql += ", scadenze.importo_da_pagare as s_da_pagare ";
        sql += ", scadenze.distinta";

        if (situazioneFornitori) {
            if (this.numInte.isSelected()) {
                sql += ", t." + campo_serie + ", ";
                sql += "t." + campo_numero1 + ", ";
            } else if (this.numEntrambi.isSelected()) {
                sql += ", t." + campo_serie + ", t." + campo_serie1 + ", ";
                sql += "t." + campo_numero + ", t." + campo_numero1 + ", ";
            } else {
                sql += ", t." + campo_serie + ", ";
                sql += "t." + campo_numero + ", ";
            }
        } else {
            sql += ", t." + campo_serie + ", ";
            sql += "t." + campo_numero + ", ";
        }
        if (situazioneFornitori) {
            if (this.numInte.isSelected()) {
                sql += campo_numero_display_interno;
                sql += ", t.data as data_interno";
            } else if (this.numEntrambi.isSelected()) {
                sql += campo_numero_display_interno;
                sql += ", t.data as data_interno";
                sql += ", " + campo_numero_display_esterno;
                sql += ", t.data_doc as data_esterno";
            } else {
                sql += campo_numero_display_esterno;
                sql += ", t.data_doc as data_esterno";
            }
        } else {
            sql += campo_numero_display_interno;
            sql += ", t.data as data_interno";
        }
        sql += ", t.anno";
        sql += ", t.pagamento ";
        if (situazioneClienti) {
            sql += ", " + campo_totale + " * (IF(t.tipo_fattura = " + dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO + ",-1,1)) as totale";
        } else {
            sql += ", " + campo_totale + " * (IF(t.tipo_fattura = " + dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO + ",-1,1)) as totale";
        }
        sql += ", clie_forn.ragione_sociale as " + campo_cliente + ", " + campo_agente + "clie_forn.codice, distinte_riba.desc_sint_banca as banca" + agg;
        sql += ", t.serie as dserie, t.numero as dnumero, t.anno as danno";
        sql += ", sum(scadenze_parziali.importo) as importo_pagato";
        if (situazioneClienti) {
            sql += ", pagamenti.riba, t.banca_abi as abi, t.banca_cab as cab, t.banca_iban as iban";
        }
        sql += ", scadenze.documento_tipo, scadenze.note_pagamento";

        if (main.fileIni.getValueBoolean("pref", "ColAgg_RiferimentoCliente", false)) {
            sql += " , clie_forn.persona_riferimento";
        }

        if (main.pluginEmail) {
            sql += ", scadenze.mail_inviata as 'Email Inviata'";
        }

        sql += ", t.id as t_id";

        sql += " from " + tab + " t left join scadenze on t.id = scadenze.id_doc \n";
        sql += " left join clie_forn on t." + campo_cliente + " = clie_forn.codice \n";
        if (StringUtils.isNotBlank(campo_agente)) {
            sql += " left join agenti on t.agente_codice = agenti.id \n";
        }
        sql += " left join pagamenti on t.pagamento = pagamenti.codice \n";
        sql += " left join distinte_riba on scadenze.distinta = distinte_riba.id \n";
        sql += " left join scadenze_parziali on scadenze.id = scadenze_parziali.id_scadenza \n";
        if (situazioneClienti) {
            sql += " where scadenze.documento_tipo = 'FA'";
            sql += " and t.tipo_fattura != " + dbFattura.TIPO_FATTURA_SCONTRINO;
            if (!cheVisNoteDiCredito.isSelected()) {
                sql += " and t.tipo_fattura != " + dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO;
            }
            if (!cheVisProforma.isSelected()) {
                sql += " and t.tipo_fattura != " + dbFattura.TIPO_FATTURA_PROFORMA;
            }
            if (main.pluginEmail && cheVisDaInviare.isSelected()) {
                sql += " and scadenze.mail_inviata = 0";
            }
            if (main.fileIni.getValueBoolean("pref", "ColAgg_RiferimentoCliente", false)) {
                sql += " and IFNULL(clie_forn.persona_riferimento,'') like '%" + Db.aa(riferimento.getText()) + "%'";
            }

            // Solo fatture anticipate
            if (main.fileIni.getValueBoolean("pref", "ColAgg_Anticipata", false)) {
                if (this.fattureAnticipate.isSelected() == true) {
                    sql += " and IFNULL(t.anticipata,'N') = 'S'";
                }
                if (this.comContoAnticipazione.getSelectedKey() != "*") {
                    sql += " and t.banca_di_anticipazione = " + Db.pc(this.comContoAnticipazione.getSelectedKey(), Types.INTEGER);
                }
            }
        } else if (situazioneFornitori) {
            sql += " where scadenze.documento_tipo = 'FR'";
            if (main.fileIni.getValueBoolean("pref", "ColAgg_RiferimentoCliente", false)) {
                sql += " and IFNULL(clie_forn.persona_riferimento,'') like '%" + Db.aa(riferimento.getText()) + "%'";
            }
            if (!cheVisNoteDiCredito.isSelected()) {
                sql += " and t.tipo_fattura != " + dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO;
            }
        } else {
            sql += " where scadenze.documento_tipo = 'OR'";
            if (main.fileIni.getValueBoolean("pref", "ColAgg_RiferimentoCliente", false)) {
                sql += " and IFNULL(clie_forn.persona_riferimento,'') like '%" + Db.aa(riferimento.getText()) + "%'";
            }
        }

        if (radOrdini.isSelected()) {
            if (fatturati) {
//                sql += " and isNull(t.doc_tipo)";
//                sql += " and t.convertito not like '%Fatt.%'";
                sql += " and IFNULL(t.convertito,'') = ''";
            }
        }
        //prendo cliente
//        if (!this.comClie.getSelectedKey().toString().equalsIgnoreCase("*")) {
        if (!tutti_i_clienti && cliente_selezionato != null) {
            sql += " and t." + campo_cliente + " = " + cliente_selezionato;
        }

        if (comCategoriaClifor.getSelectedIndex() > 0) {
            sql += " and clie_forn.tipo2 = " + Db.pc(comCategoriaClifor.getSelectedKey(), Types.VARCHAR);
        }

        if (situazioneClienti) {
            if (comAgente.getSelectedIndex() > 0) {
                sql += " and t.agente_codice = " + Db.pc(comAgente.getSelectedKey(), Types.VARCHAR);
            }
        }

        //lo se vederle tutte o solo da pagare
        if (this.chePaga.isSelected() == true) {
            sql += " and (scadenze.pagata = 'N' or scadenze.pagata = 'P')";
        }

        if (texDal.getDate() != null) {
            sql += " and scadenze.data_scadenza >= " + Db.pc(texDal.getDate(), java.sql.Types.DATE);
        }
        if (texAl.getDate() != null) {
            sql += " and scadenze.data_scadenza <= " + Db.pc(texAl.getDate(), java.sql.Types.DATE);
        }
        if (texDalDoc.getDate() != null) {
            sql += " and t.data >= " + Db.pc(texDalDoc.getDate(), java.sql.Types.DATE);
        }
        if (texAlDoc.getDate() != null) {
            sql += " and t.data <= " + Db.pc(texAlDoc.getDate(), java.sql.Types.DATE);
        }

        if (cu.s(comPagamento.getSelectedItem()).equals(SOLO_RIBA)) {
            sql += " and pagamenti.riba = 'S' and IFNULL(scadenze.flag_acconto,'N') != 'S' ";
        } else if (cu.s(comPagamento.getSelectedItem()).equals(SOLO_RID)) {
            sql += " and pagamenti.rid = 'S' and IFNULL(scadenze.flag_acconto,'N') != 'S' ";
        } else if (cu.s(comPagamento.getSelectedItem()).equals(SOLO_ALTRI)) {
            sql += " and pagamenti.riba = 'N' and pagamenti.rid = 'N'";
        }
        if (this.cheStam.isSelected() == true) {
            sql += " and scadenze.distinta is null";
        }
        if (!this.comBanca.getSelectedKey().toString().equalsIgnoreCase("*")) {
            if (comBanca.getSelectedKey().toString().equalsIgnoreCase("-1")) {
                sql += " and distinte_riba.cc is null";
            } else {
                sql += " and distinte_riba.cc = " + Db.pc(comBanca.getSelectedKey(), Types.VARCHAR);
            }
        }

        SwingUtils.mouse_wait(this);

        sql += " group by scadenze.id";
        if (!orderClause.equals("")) {
            sql += " order by " + orderClause;
        }

        System.out.println("sql situazione clifor: " + sql);
        this.griglia.flagUsaThread = false;
        MicroBench mb = new MicroBench();
        mb.start();
        this.griglia.dbOpen(Db.getConn(), sql);

        if (main.pluginEmail) {
            try {
                griglia.getColumn("Email Inviata").setCellRenderer(new frmElenFatt.EmailCellRenderer());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            griglia.getColumn("pagata").setCellRenderer(new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel lab = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    lab.setHorizontalAlignment(SwingConstants.CENTER);
                    return lab;
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        griglia.getColumn("totale").addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("width")) {
                    moveLabDocumento((Integer) evt.getNewValue());
                }
            }
        });
        scrollGriglia.getViewport().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                moveLabDocumento(null);
            }
        });

        System.out.println(mb.getDiff("selezione situazione"));
        this.sql = sql;
        //setto header con Jlabel
        /*JLabelHeaderRenderer renderer = new JLabelHeaderRenderer();
         TableColumnModel model = this.griglia.getColumnModel();
         int n = model.getColumnCount();
         for (int i=0;i<n;i++) {
         model.getColumn(i).setHeaderRenderer(renderer);
         }*/
        //deseleziono tutte lengthscadenze
        if (seltutte) {
            butSeleTuttActionPerformed(null);
        } else {
            butDeseTuttActionPerformed(null);
        }

        //aggiorno il totale
        double totale = 0;
        double totalePagate = 0;
        double totaleDaPagare = 0;
        for (int i = 0; i < this.griglia.getRowCount(); i++) {
            Double d = (Double) this.griglia.getValueAt(i, griglia.getColumnByName("importo"));
            Double dpagato = (Double) this.griglia.getValueAt(i, griglia.getColumnByName("importo_pagato"));
            if (String.valueOf(this.griglia.getValueAt(i, griglia.getColumnByName("pagata"))).equals("S")) {
                totalePagate += d;
            } else if (String.valueOf(this.griglia.getValueAt(i, griglia.getColumnByName("pagata"))).equals("P")) {
                totalePagate += dpagato;
                totaleDaPagare += (d - dpagato);
            } else {
                totaleDaPagare += d.doubleValue();
            }
            totale += d.doubleValue();
            //System.out.println("sommo [" + i + "]=" + d.doubleValue());
        }

        this.labTotale.setText("Totale " + it.tnx.Util.EURO + " " + it.tnx.Util.formatValutaEuro(totale)
                + " / Gia' Pagate " + it.tnx.Util.EURO + " " + it.tnx.Util.formatValutaEuro(totalePagate)
                + " / Da Pagare " + it.tnx.Util.EURO + " " + it.tnx.Util.formatValutaEuro(totaleDaPagare));

        SwingUtils.mouse_def(this);
    }

    private void moveLabDocumento(Integer neww) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String campo2 = "serie";
                if (radFornitori.isSelected()) {
                    campo2 = "serie_esterna";
                }
                int x1 = SwingUtils.getColumnLocationX(griglia, griglia.getColumnModel().getColumnIndex("ndin")) - (int) scrollGriglia.getViewport().getViewPosition().getX();
                int x2 = SwingUtils.getColumnLocationX(griglia, griglia.getColumnModel().getColumnIndex(campo2)) - (int) scrollGriglia.getViewport().getViewPosition().getX();
                int neww2 = x2 - x1;
                labScadenza.setSize(neww2 + 1, labDocumento.getHeight());
                labScadenza.setLocation(x1, labDocumento.getLocation().y);

                x1 = SwingUtils.getColumnLocationX(griglia, griglia.getColumnModel().getColumnIndex(campo2)) - (int) scrollGriglia.getViewport().getViewPosition().getX();
//                x2 = SwingUtils.getColumnLocationX(griglia, griglia.getColumnModel().getColumnIndex("cliente")) - (int)scrollGriglia.getViewport().getViewPosition().getX();
                x2 = griglia.getWidth() - (int) scrollGriglia.getViewport().getViewPosition().getX();
                neww2 = x2 - x1;
                labDocumento.setSize(neww2 + 1, labScadenza.getHeight());
                labDocumento.setLocation(x1, labScadenza.getLocation().y);
            }
        });

    }

    private void controllaPanPrefExtra() {
        if (menColAggRiferimentoCliente.isSelected()) {

        } else {

        }
    }

    //stampa distinta RB per banca
    public void stampaDistinta() {
        String sql;
        String sqlSele;
        int id;
        /* faccio dialo gper cc
         CoordinateBancarie coord = new CoordinateBancarie();
         coord.setAbi("06160");
         coord.setCab("71940");
         coord.setCc("12345");
         */
        if (this.griglia.getSelectedRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Nessuna scadenza selezionata");
            return;
        }
        java.awt.Frame parent = (java.awt.Frame) this.getTopLevelAncestor();
        diaDistRiba dialog = new diaDistRiba(parent, true);
        dialog.setBounds(100, 100, 700, 400);
        dialog.show();
        String data_stampa = dialog.data_distinta;
        //chiedo conferma
        if (dialog.prova == false) {
            int ret = javax.swing.JOptionPane.showConfirmDialog(this, "Sicuro di stampare in definitivo?", "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION);
            if (ret == javax.swing.JOptionPane.NO_OPTION) {
                return;
            }
        }
        //
        if (dialog.cancel == false) {
            CoordinateBancarie coord = new CoordinateBancarie();
            coord = dialog.coord;
            //invece che prendere tutte le scadenze prendo quelle che seleziona a mano
            //---
            sql = "delete from scadenze_sel where username = " + Db.pc(main.login, "VARCHAR");
            Db.executeSql(sql);
            int[] righe = this.griglia.getSelectedRows();
            for (int i = 0; i < righe.length; i++) {
                if (righe[i] < this.griglia.getRowCount()) {
                    sql = "insert into scadenze_sel (id, username) values ";
                    sql += "(" + this.griglia.getValueAt(righe[i], 0);
                    sql += " ," + Db.pc(main.login, "VARCHAR");
                    sql += ")";
                    if (this.griglia.getValueAt(righe[i], 0) != null) {
                        Db.executeSql(sql);
                    }
                }
            }
            //prepasro sql per print
            sqlSele = "select scadenze_sel.id, ";
            sqlSele += " scadenze_sel.username, "
                    + "t.serie, "
                    + "t.numero, "
                    + "t.anno, "
                    + "t.data, "
                    + "t.totale, "
                    + "t.banca_abi as t_banca_abi, "
                    + "t.banca_cab as t_banca_cab, "
                    + "t.opzione_riba_dest_diversa, "
                    + "scadenze.data_scadenza, "
                    + "scadenze.pagata, "
                    + "scadenze.importo, "
                    + "scadenze.numero";
            sqlSele += ", dest_ragione_sociale, dest_indirizzo, dest_cap, dest_localita, dest_provincia, dest_telefono, dest_cellulare";

            sqlSele += ", clie_forn.codice as clie_forn_codice";
            sqlSele += ", clie_forn.ragione_sociale as clie_forn_ragione_sociale";
            sqlSele += ", clie_forn.indirizzo as clie_forn_indirizzo";
            sqlSele += ", clie_forn.cap as clie_forn_cap";
            sqlSele += ", clie_forn.localita as clie_forn_localita";
            sqlSele += ", clie_forn.provincia as clie_forn_provincia";
            sqlSele += ", clie_forn.piva_cfiscale as clie_forn_piva_cfiscale";
            sqlSele += ", banche_abi.nome as banche_abi_nome";
            sqlSele += ", banche_cab.indirizzo as banche_cab_indirizzo";
            sqlSele += ", comuni.comune as comuni_comune";
            sqlSele += ", t.totale_da_pagare";

            sqlSele += " from scadenze_sel inner join scadenze on scadenze_sel.id = scadenze.id \n";
            sqlSele += " left join test_fatt t on t.id = scadenze.id_doc \n";
            sqlSele += " left join clie_forn on t.cliente = clie_forn.codice \n";
            sqlSele += " left join banche_abi on t.banca_abi = banche_abi.abi \n";
            sqlSele += " left join banche_cab on t.banca_abi = banche_cab.abi and t.banca_cab = banche_cab.cab \n";
            sqlSele += " left join comuni on banche_cab.codice_comune = comuni.codice \n";
            sqlSele += " where scadenze_sel.username = " + Db.pc(main.login, "VARCHAR");
            //sqlSele += " order by t.cliente, t.numero, scadenze.data_scadenza";
            sqlSele += " order by clie_forn.ragione_sociale, t.numero, scadenze.data_scadenza";
            //104 aggiungo controllo totali fattura con totali scadenze
            //da finire
            //if (Scadenze.controllaTotali(sqlSele, this) == false) {
            //  return;
            //}
            //***
            if (dialog.prova == true) {
                prnDistRb print = new prnDistRb(sqlSele, coord, true, 0, Db.getCurrDateTimeMysqlIta());
            } else {
                //inserisco il record della distinta
                try {
                    String strDataDistinta = data_stampa;
                    //sql = "insert into distinte_riba (data) values ('" + strDataDistinta + "')";
                    sql = "insert into distinte_riba (";
                    sql += " data";
                    sql += " , abi";
                    sql += " , cab";
                    sql += " , cc";
                    sql += " , desc_sint_banca";
                    sql += " ) values ( ";
                    sql += "'" + strDataDistinta + "'";
                    sql += ", '" + coord.getAbi() + "'";
                    sql += ", '" + coord.getCab() + "'";
                    sql += ", '" + coord.getCc() + "'";
                    sql += ", " + Db.pc(coord.findSmallDescription(), Types.VARCHAR);
                    sql += ")";
                    Db.executeSql(sql);
                    //prendo ultimo id distinte_riba
                    sql = "select id from distinte_riba order by id desc limit 0,1";
                    ResultSet temp = Db.openResultSet(sql);
                    temp.next();
                    id = temp.getInt("id");
                    //riformatto la data
                    String nuova_data = Db.getCurrDateTimeMysqlIta();
                    try {
                        DateFormat parsaData = new SimpleDateFormat("yyyy-MM-dd");
                        Date data_inserita = parsaData.parse(strDataDistinta);
                        java.text.SimpleDateFormat riformattazione = new java.text.SimpleDateFormat("dd/MM/yy");
                        nuova_data = riformattazione.format(data_inserita);
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                    //lancio creazione pdf, dovrei controllare anche che nega creato...
                    prnDistRb print = new prnDistRb(sqlSele, coord, false, id, nuova_data);
                    //controllo esito stampa
                    if (print.rispostaConferma == print.RISPOSTA_CONTINUA) {
                        //marca lengthscadenze come gi??? stampate
                        //metto id distinta nelle scadenze ciclando id per id
                        ResultSet tempSel = Db.openResultSet("select * from scadenze_sel where scadenze_sel.username = " + Db.pc(main.login, "VARCHAR"));
                        while (tempSel.next()) {
                            sql = "update scadenze";
                            sql += " set distinta = " + id;
                            sql += " where documento_tipo = 'FA'";
                            sql += " and id = " + tempSel.getString("id");
                            Db.executeSql(sql);
                        }
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                    javax.swing.JOptionPane.showMessageDialog(this, "Errore nella marcatura delle scadenze\n" + err.toString());
                }
            }
            dialog.dispose();
        }
    }

    public boolean caricaImpo() {
        texClie.setText(main.fileIni.getValue("situazione_clienti_fornitori", "texClie", "<tutti> [*]"));
        comCategoriaClifor.dbTrovaRiga(main.fileIni.getValue("situazione_clienti_fornitori", "comCategoriaClifor", "<tutte le categorie>"));
        comAgente.dbTrovaRiga(main.fileIni.getValue("situazione_clienti_fornitori", "comAgente", "<tutti gli agenti>"));
        tutti_i_clienti = true;
        cliente_selezionato = null;
        if (main.fileIni.existKey("situazione_clienti_fornitori", "comClie") || main.fileIni.existKey("situazione_clienti_fornitori", "texClie")) {
//            comClie.dbTrovaRiga(main.fileIni.getValue("situazione_clienti_fornitori", "comClie"));
            texClie.setText(main.fileIni.getValue("situazione_clienti_fornitori", "texClie", "<tutti> [*]"));
            if (main.fileIni.getValueBoolean("situazione_clienti_fornitori", "tutti_i_clienti", true)) {
                tutti_i_clienti = true;
                cliente_selezionato = null;
            } else {
                tutti_i_clienti = false;
                cliente_selezionato = CastUtils.toInteger(main.fileIni.getValue("situazione_clienti_fornitori", "cliente_selezionato"));
            }
            comPagamento.dbTrovaRiga(main.fileIni.getValue("situazione_clienti_fornitori", "comPagamento", "Tutti i tipi di pagamento"));
            if (comPagamento.getSelectedIndex() == -1) {
                comPagamento.dbTrovaRiga(TUTTI);
            }

            texDal.setDate(cu.toDateIta(main.fileIni.getValue("situazione_clienti_fornitori", "texDal")));
            texAl.setDate(cu.toDateIta(main.fileIni.getValue("situazione_clienti_fornitori", "texAl")));
            texDalDoc.setDate(cu.toDateIta(main.fileIni.getValue("situazione_clienti_fornitori", "texDalDoc")));
            texAlDoc.setDate(cu.toDateIta(main.fileIni.getValue("situazione_clienti_fornitori", "texAlDoc")));

            cheStam.setSelected((main.fileIni.getValue("situazione_clienti_fornitori", "cheStam").equals("true") ? true : false));
            chePaga.setSelected((main.fileIni.getValue("situazione_clienti_fornitori", "chePaga").equals("true") ? true : false));

            if (main.fileIni.getValueBoolean("pref", "ColAgg_Anticipata", false) != false) {
                fattureAnticipate.setSelected((main.fileIni.getValue("situazione_clienti_fornitori", "fattureAnticipate").equals("true") ? true : false));
            } else {
                fattureAnticipate.setSelected(false);
            }
            cheVisNoteDiCredito.setSelected((main.fileIni.getValue("situazione_clienti_fornitori", "cheVisNoteDiCredito").equals("true") ? true : false));
            cheVisProforma.setSelected((main.fileIni.getValue("situazione_clienti_fornitori", "cheVisProforma").equals("true") ? true : false));

            if (main.pluginEmail) {
                cheVisDaInviare.setSelected((main.fileIni.getValue("situazione_clienti_fornitori", "cheVisDaInviare").equals("true") ? true : false));
            }

            comBanca.dbTrovaRiga(main.fileIni.getValue("situazione_clienti_fornitori", "comBanca"));
            comContoAnticipazione.dbTrovaKey(main.fileIni.getValue("situazione_clienti_fornitori", "contoCorrenteAnticipazione", "*"));
            radClienti.setSelected((main.fileIni.getValue("situazione_clienti_fornitori", "radClienti").equals("true") ? true : false));
            radFornitori.setSelected((main.fileIni.getValue("situazione_clienti_fornitori", "radFornitori").equals("true") ? true : false));

            numEntrambi.setSelected((main.fileIni.getValue("numero_interno_esterno", "numEntrambi").equals("true")));
            numInte.setSelected((main.fileIni.getValue("numero_interno_esterno", "numInte").equals("true")));
            numEste.setSelected((main.fileIni.getValue("numero_interno_esterno", "numEste").equals("true")));

            try {
                ordine.setSelectedIndex(cu.i0(main.fileIni.getValue("situazione_clienti_fornitori", "ordine", "1")));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
        return false;
    }

    public void salvaImpo() {
//        main.fileIni.setValue("situazione_clienti_fornitori", "comClie", comClie.getSelectedItem());
        System.out.println("getWidth = " + getWidth());
        main.fileIni.setValue("situazione_clienti_fornitori", "frmwidth", getWidth());
        main.fileIni.setValue("situazione_clienti_fornitori", "texClie", texClie.getText());
        main.fileIni.setValue("situazione_clienti_fornitori", "tutti_i_clienti", tutti_i_clienti);
        main.fileIni.setValue("situazione_clienti_fornitori", "cliente_selezionato", cliente_selezionato);
        main.fileIni.setValue("situazione_clienti_fornitori", "comCategoriaClifor", comCategoriaClifor.getSelectedItem());
        main.fileIni.setValue("situazione_clienti_fornitori", "comAgente", comAgente.getSelectedItem());
        main.fileIni.setValue("situazione_clienti_fornitori", "comPagamento", comPagamento.getSelectedItem());

        main.fileIni.setValue("situazione_clienti_fornitori", "texDal", DateUtils.formatDateIta(texDal.getDate()));
        main.fileIni.setValue("situazione_clienti_fornitori", "texAl", DateUtils.formatDateIta(texAl.getDate()));

        main.fileIni.setValue("situazione_clienti_fornitori", "texDalDoc", DateUtils.formatDateIta(texDalDoc.getDate()));
        main.fileIni.setValue("situazione_clienti_fornitori", "texAlDoc", DateUtils.formatDateIta(texAlDoc.getDate()));

        main.fileIni.setValue("situazione_clienti_fornitori", "cheStam", cheStam.isSelected());
        main.fileIni.setValue("situazione_clienti_fornitori", "chePaga", chePaga.isSelected());
        main.fileIni.setValue("situazione_clienti_fornitori", "fattureAnticipate", fattureAnticipate.isSelected());
        main.fileIni.setValue("situazione_clienti_fornitori", "contoCorrenteAnticipazione", comContoAnticipazione.getSelectedKey());
        main.fileIni.setValue("situazione_clienti_fornitori", "cheVisNoteDiCredito", cheVisNoteDiCredito.isSelected());
        main.fileIni.setValue("situazione_clienti_fornitori", "cheVisProforma", cheVisProforma.isSelected());
        if (main.pluginEmail) {
            main.fileIni.setValue("situazione_clienti_fornitori", "cheVisDaInviare", cheVisDaInviare.isSelected());
        }
        main.fileIni.setValue("situazione_clienti_fornitori", "comBanca", comBanca.getSelectedItem());
        main.fileIni.setValue("situazione_clienti_fornitori", "radClienti", radClienti.isSelected());
        main.fileIni.setValue("situazione_clienti_fornitori", "radFornitori", radFornitori.isSelected());

        main.fileIni.setValue("numero_interno_esterno", "numEntrambi", numEntrambi.isSelected());
        main.fileIni.setValue("numero_interno_esterno", "numInte", numInte.isSelected());
        main.fileIni.setValue("numero_interno_esterno", "numEste", numEste.isSelected());

        main.fileIni.setValue("situazione_clienti_fornitori", "ordine", ordine.getSelectedIndex());
    }

    private void aggiornaOrdine() {
        orderClause = String.valueOf(((KeyValuePair) ordine.getSelectedItem()).key);
        if (!orderClause.equals("")) {
            radDescr.setEnabled(true);
            radCresc.setEnabled(true);
            if (radDescr.isSelected()) {
                int index = orderClause.indexOf(",");
                if (index >= 0) {
                    String[] clausole = orderClause.split(",");
                    orderClause = "";
                    for (String s : clausole) {
                        orderClause += s + " DESC,";
                    }
                    orderClause = StringUtils.chop(orderClause);
                } else {
                    orderClause += " DESC";
                }
            }
        } else {
            radDescr.setEnabled(false);
            radCresc.setEnabled(false);
        }
        selezionaSituazione();
    }

    DelayedExecutor delay_rif = new DelayedExecutor(new Runnable() {
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    selezionaSituazione();
                }
            });
        }
    }, 250);

    private tnxDbGrid getGriglia() {
        return new tnxDbGrid() {
            String tooltipText;
            boolean sameRow;
//        Color color_pagata = new Color(230, 255, 230);
//        Color color_non_pagata = new Color(255, 230, 230);
//        Color color_parziale = new Color(255, 255, 230);
//        Color color_hover = new Color(200, 200, 200);
//        Color color_sel = new Color(155, 155, 155);
            Color color_pagata = new Color(200, 255, 200);
            Color color_non_pagata = new Color(255, 200, 200);
            Color color_parziale = new Color(255, 255, 200);
            Color color_hover = new Color(255, 255, 255);
            Color color_sel = new Color(160, 160, 160);

            private Border paddingBorder = BorderFactory.createEmptyBorder(3, 3, 3, 2);

            @Override
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

                try {
                    JLabel lab = (JLabel) c;
                    lab.setBorder(paddingBorder);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return c;
            }

            protected Color colorForRow(int row, Component c) {
                int col = getColumnByName("Pagata");
                try {
                    if (getValueAt(row, col).toString().equalsIgnoreCase("S")) {
                        return color_pagata;
                    } else if (getValueAt(row, col).toString().equalsIgnoreCase("N")) {
                        return color_non_pagata;
                    } else {
                        return color_parziale;
                    }
                } catch (Exception e) {
                }
                return c.getBackground();
            }

            public String convertValueToText(Object value, int row, int column) {
                if (value != null) {
                    String sValue = value.toString();
                    if (sValue != null) {
                        return sValue;
                    }
                }
                return "";
            }

            @Override
            public String getToolTipText(MouseEvent event) {
                if (sameRow) {
                    return tooltipText;
                } else {
                    String tip = null;
                    Point p = event.getPoint();
                    int hitRowIndex = rowAtPoint(p);
                    String campoUtente = "cliente";
                    String numero = "";
                    int anno = Integer.parseInt(String.valueOf(getValueAt(hitRowIndex, getColumnByName("anno"))));
                    String serie = String.valueOf(getValueAt(hitRowIndex, getColumnByName("serie")));
                    if (situazioneFornitori) {
                        campoUtente = "fornitore";
                    }

                    String campo_data = null;
                    String numero_esterno = null;
                    if (situazioneFornitori) {
                        if (numInte.isSelected()) {
                            campo_data = "data_interno";
                            numero_esterno = "";
                        } else if (numEntrambi.isSelected()) {
                            campo_data = "data_interno";
                            numero_esterno = String.valueOf(getValueAt(hitRowIndex, getColumnByName("numero_esterno_display")));
                        } else {
                            campo_data = "data_esterno";
                            numero_esterno = String.valueOf(getValueAt(hitRowIndex, getColumnByName("numero_esterno_display")));
                        }
                    } else {
                        campo_data = "data_interno";
                        numero_esterno = "";
                    }

                    numero = String.valueOf(getValueAt(hitRowIndex, getColumnByName("numero_interno_display")));
                    String cliente = String.valueOf(getValueAt(hitRowIndex, getColumnByName(campoUtente)));
                    String tipoDocumento = String.valueOf(getValueAt(hitRowIndex, getColumnByName("documento_tipo")));
                    String dataDocumento = String.valueOf(getValueAt(hitRowIndex, getColumnByName("data")));
                    Date dataDocumentod = (Date) getValueAt(hitRowIndex, getColumnByName(campo_data));
                    String pagamento = String.valueOf(getValueAt(hitRowIndex, getColumnByName("pagamento")));
                    String dataScadenza = String.valueOf(getValueAt(hitRowIndex, getColumnByName("data scadenza")));
                    String ndin = String.valueOf(getValueAt(hitRowIndex, getColumnByName("ndin")));
                    Date dataScadenzad = (Date) getValueAt(hitRowIndex, getColumnByName("data scadenza"));
                    Double totale = Double.parseDouble(String.valueOf(getValueAt(hitRowIndex, getColumnByName("totale"))));
                    Double importo = Double.parseDouble(String.valueOf(getValueAt(hitRowIndex, getColumnByName("importo"))));
                    Double daPagare = Double.parseDouble(String.valueOf(getValueAt(hitRowIndex, getColumnByName("s_da_pagare"))));
                    int id = Integer.parseInt(String.valueOf(getValueAt(hitRowIndex, getColumnByName("id"))));

                    if (tipoDocumento.equals("FA")) {
                        tipoDocumento = "Fattura";
                    } else if (tipoDocumento.equals("FR")) {
                        tipoDocumento = "Fattura di Acquisto";
                    } else {
                        tipoDocumento = "Ordine";
                    }
                    tip = "<html><b>Cliente: </b>" + cliente + "<br>";

                    tip += "<b>Documento: </b>" + tipoDocumento + " " + numero + " del " + DateUtils.formatDate(dataDocumentod);
                    if (situazioneFornitori) {
                        tip += " (Num. Esterno: " + numero_esterno + ")";
                    }
                    tip += "<br><b>Pagamento: </b>" + pagamento + "<br>";
                    tip += "<b>Totale documento: </b>" + FormatUtils.formatEuroIta(totale) + "<br>";
                    tip += "<br>Scadenza " + ndin + " del " + DateUtils.formatDate(dataScadenzad) + "<br>";
                    tip += "<b>Importo scadenza: </b>" + FormatUtils.formatEuroIta(importo) + "<br>";
                    tip += "<b>Ancora da pagare: </b>" + FormatUtils.formatEuroIta(daPagare);
                    tip += "</html>";

                    tooltipText = tip;
                    return tip;
                }
            }

            // makes the tooltip's location to match table cell location
            // also avoids showing empty tooltips
            public Point getToolTipLocation(MouseEvent event) {
                int row = rowAtPoint(event.getPoint());
                if (row == -1) {
                    return null;
                }
                int col = columnAtPoint(event.getPoint());
                if (col == -1) {
                    return null;
                }

                sameRow = (row == rigaAttuale);
                boolean hasTooltip = getToolTipText() == null ? getToolTipText(event) != null : true;
                rigaAttuale = row;
//            return hasTooltip ? getCellRect(row, 1, false).getLocation(): null;
                Point p = event.getPoint();
                p.translate(10, 10);
                return hasTooltip ? p : null;
            }
        };
    }

    private void apriScadenza() {
        String serie = null;
        int numero = 0;
        int anno = 0;
        String pagamento = null;

        Date data_doc = null;

        Integer id_doc = cu.i(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("t_id")));

        if (situazioneClienti || situazioneOrdini) {
            serie = String.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("serie")));
            System.err.println("serie = " + serie);
            numero = Integer.parseInt(String.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("numero"))));
            System.err.println("numero = " + numero);
            anno = Integer.parseInt(String.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("anno"))));
            System.err.println("anno = " + anno);
            data_doc = (Date) griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("data_interno"));
        } else if (situazioneFornitori) {
            serie = String.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("serie2")));
            System.err.println("serie = " + serie);
            numero = Integer.parseInt(String.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("numero2"))));
            System.err.println("numero = " + numero);
            anno = Integer.parseInt(String.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("anno2"))));
            System.err.println("anno = " + anno);
            //data_doc = (Date) griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("data_interno"));
            try {
                data_doc = (Date) DbUtils.getObject(Db.getConn(), "select data from test_fatt_acquisto where serie = " + Db.pc(serie, Types.VARCHAR) + " and numero = " + numero + " and anno = " + anno);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        pagamento = String.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), 1));
        System.err.println("pagamento = " + pagamento);
        //javax.swing.JOptionPane.showMessageDialog(this, "debug2:" + serie + ":" + numero + ":" + anno + ":" + pagamento);
        Scadenze tempScad = null;
        if (situazioneClienti) {
            tempScad = new Scadenze(Db.TIPO_DOCUMENTO_FATTURA, id_doc, null, data_doc);
            System.err.println("tempScad = " + tempScad);
        } else if (situazioneFornitori) {
            tempScad = new Scadenze(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, id_doc, null, data_doc);
            System.err.println("tempScad = " + tempScad);
        } else if (situazioneOrdini) {
            tempScad = new Scadenze(Db.TIPO_DOCUMENTO_ORDINE, id_doc, null, data_doc);
            System.err.println("tempScad = " + tempScad);
        }
        frmPagaPart frm = new frmPagaPart(tempScad, CastUtils.toInteger(griglia.getValueAt(griglia.getSelectedRow(), 0)));
        System.err.println("frm = " + frm);
        Point p = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(p, main.getPadrePanel().getDesktopPane());
        int x = p.x;
        int y = p.y;
        int w = 650;
        int h = 550;
        Dimension d = main.getPadrePanel().getDesktopPane().getSize();
        if (x + w > d.width) {
            x = x - w;
        }
        if (y + h > d.height) {
            y = y - h;
        }
        main.getPadre().openFrame(frm, w, h, y, x);
    }
}
