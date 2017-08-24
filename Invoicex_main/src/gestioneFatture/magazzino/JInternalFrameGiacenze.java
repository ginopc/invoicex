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
/*
 * JInternalFrameGiacenze.java
 *
 * Created on 23 maggio 2007, 12.06
 */
package gestioneFatture.magazzino;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.CustomExpression;
import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DJGroupLabel;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.builders.GroupBuilder;
import ar.com.fdvs.dj.domain.builders.StyleBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.GroupLayout;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.entities.DJGroup;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn;
import gestioneFatture.ArticoloHint;
import gestioneFatture.ClienteHint;
import gestioneFatture.InvoicexEvent;
import it.tnx.Db;
import gestioneFatture.Util;
import gestioneFatture.main;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.KeyValuePair;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.swing.DelayedExecutor;
import it.tnx.commons.table.RendererUtils;
import it.tnx.dbeans.pdfPrint.PrintSimpleTable;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.Magazzino;
import it.tnx.invoicex.data.Giacenza;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Filter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.PatternFilter;
import util.BeanAdapterTableModel;

/**
 *
 * @author mceccarelli
 */
public class JInternalFrameGiacenze extends javax.swing.JInternalFrame {

    private boolean matricola;
    private boolean lotti;
    public Integer fornitore = null;
    boolean flagPrimo = true;
    private Object listino;

    AtomicReference<ArticoloHint> articolo_selezionato_filtro_ref = new AtomicReference(null);
    AtomicReference<ClienteHint> fornitore_selezionato_filtro_ref = new AtomicReference(null);
    DelayedExecutor delay_filtro = new DelayedExecutor(new Runnable() {
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    aggiorna();
                }
            });
        }
    }, 250);
    private boolean loading = true;

    /**
     * Creates new form JInternalFrameGiacenze
     */
    public JInternalFrameGiacenze(boolean matricola) {
        this(matricola, false);
    }

    public JInternalFrameGiacenze(boolean matricola, boolean lotti) {
        initComponents();

        tabGiacenze.setShowGrid(false);
        tabGiacenze.setShowHorizontalLines(true);
//        tabGiacenze.setIntercellSpacing(new Dimension(3, 3));

        texArticolo.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                filtra();
            }

            public void removeUpdate(DocumentEvent e) {
                filtra();
            }

            public void changedUpdate(DocumentEvent e) {
                filtra();
            }
        });
        InvoicexUtil.getArticoloIntelliHints(texArticolo, this, articolo_selezionato_filtro_ref, delay_filtro, null);
        InvoicexUtil.getFornitoriIntelliHints(texFornitore, this, fornitore_selezionato_filtro_ref, delay_filtro, null);

        jXDatePicker1.setFormats(new SimpleDateFormat("dd/MM/yyyy"));
        this.matricola = matricola;
        this.lotti = lotti;

        categoria.dbAddElement("", null);
        categoria.dbOpenList(Db.getConn(), "select categoria, id from categorie_articoli order by categoria", null, false);
        sottocategoria.dbClearList();

        //carico impo
        ordine.setSelectedItem(main.fileIni.getValue("pref", "stampa_giacenze_ordine"));
        categoria.setSelectedItem(main.fileIni.getValue("pref", "stampa_giacenze_filtro_cat"));
        sottocategoria.setSelectedItem(main.fileIni.getValue("pref", "stampa_giacenze_filtro_sottocat"));
        

        loading = false;

        InvoicexUtil.fireEvent(this, InvoicexEvent.TYPE_FRMGIACENZE_CONSTR_POST_INIT_COMPS);
    }

    public void initFilters() {
//        comArticoli_old.addItem(null);
//        TreeSet articoli = new TreeSet();
//        BeanAdapterTableModel model = (BeanAdapterTableModel) tabGiacenze.getModel();
//        for (Object o : model.getBeans()) {
//            articoli.add(((Giacenza) o).getCodice_articolo());
//        }
//        Iterator iter = articoli.iterator();
//        for (Object o : articoli) {
//            comArticoli_old.addItem(iter.next());
//        }

        KeyValuePair kv = new KeyValuePair("null", "<non stampare prezzi>");
        KeyValuePair kv1 = new KeyValuePair("***EL1***", "<ultimo prezzo lordo vendita>");
        KeyValuePair kv2 = new KeyValuePair("***EL2***", "<ultimo prezzo lordo acquisto>");
        KeyValuePair kv3 = new KeyValuePair("***EL3***", "<ultimo prezzo netto vendita>");
        KeyValuePair kv4 = new KeyValuePair("***EL4***", "<ultimo prezzo netto acquisto>");
        KeyValuePair kv5 = new KeyValuePair("***EL5***", "<costo netto medio>");
        SwingUtils.initJComboFromDb(comPrezzi, Db.getConn(), "select codice, descrizione from tipi_listino order by codice", "codice", "descrizione", kv, kv1, kv3, kv2, kv4, kv5);

        KeyValuePair kvd1 = new KeyValuePair(Magazzino.Depositi.TUTTI_RIEPILOGATIVO, "<tutti i depositi riepilogativo>");
        KeyValuePair kvd2 = new KeyValuePair(Magazzino.Depositi.TUTTI_DETTAGLIO, "<tutti i depositi con dettaglio>");
        SwingUtils.initJComboFromDb(deposito, Db.getConn(), "select id, nome from depositi order by nome", "id", "nome", kvd1, kvd2);
        
        try {
            deposito.setSelectedIndex( cu.i(main.fileIni.getValue("pref", "stampa_giacenze_deposito")) );
        } catch (Exception e) {
        }
        try {
            comPrezzi.setSelectedIndex( cu.i(main.fileIni.getValue("pref", "stampa_giacenze_prezzi")) );
        } catch (Exception e) {
        }
        giacenza_zero.setSelected(main.fileIni.getValueBoolean("pref", "stampa_giacenze_zero", true));
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tabGiacenze = new JXTable();
        stampa = new javax.swing.JButton();
        lab_sottocategoria = new javax.swing.JLabel();
        sottocategoria = new tnxbeans.tnxComboField();
        esporta = new javax.swing.JButton();
        labTotali = new javax.swing.JLabel();
        segna_posto_fornitore = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jXDatePicker1 = new org.jdesktop.swingx.JXDatePicker();
        jLabel4 = new javax.swing.JLabel();
        butRefresh = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        deposito = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        comPrezzi = new javax.swing.JComboBox();
        giacenza_zero = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        texArticolo = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        texFornitore = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        ordine = new javax.swing.JComboBox();
        lab_categoria = new javax.swing.JLabel();
        categoria = new tnxbeans.tnxComboField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Giacenze");

        tabGiacenze.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tabGiacenze);

        stampa.setText("Stampa");
        stampa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stampaActionPerformed(evt);
            }
        });

        lab_sottocategoria.setText("Sotto categoria");

        sottocategoria.setEditable(false);
        sottocategoria.setDbDescCampo("");
        sottocategoria.setDbNomeCampo("sottocategoria");
        sottocategoria.setDbNullSeVuoto(true);
        sottocategoria.setDbRiempire(false);
        sottocategoria.setDbTipoCampo("");
        sottocategoria.setDbTrovaMentreScrive(true);
        sottocategoria.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sottocategoriaItemStateChanged(evt);
            }
        });
        sottocategoria.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                sottocategoriaPopupMenuWillBecomeVisible(evt);
            }
        });

        esporta.setText("Esporta in Excel");
        esporta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                esportaActionPerformed(evt);
            }
        });

        labTotali.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labTotali.setText("...");
        labTotali.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Data");

        jXDatePicker1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jXDatePicker1ActionPerformed(evt);
            }
        });

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getSize()-1f));
        jLabel4.setText("la giacenza verrà calcolata alla data specificata");

        butRefresh.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        butRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/view-refresh.png"))); // NOI18N
        butRefresh.setToolTipText("Aggiorna l'elenco");
        butRefresh.setIconTextGap(2);
        butRefresh.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butRefreshActionPerformed(evt);
            }
        });

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Deposito");

        deposito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                depositoActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Prezzo");

        comPrezzi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comPrezziActionPerformed(evt);
            }
        });

        giacenza_zero.setSelected(true);
        giacenza_zero.setText("Comprendi giacenza a zero");
        giacenza_zero.setToolTipText("Selezionandolo verranno visualizzati anche gli articoli con giacenza uguale a zero");
        giacenza_zero.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                giacenza_zeroActionPerformed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Filtra per articolo");

        texArticolo.setColumns(30);
        texArticolo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texArticoloActionPerformed(evt);
            }
        });

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Fornitore abituale");

        texFornitore.setColumns(30);
        texFornitore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texFornitoreActionPerformed(evt);
            }
        });

        jLabel7.setText("Ordina per");

        ordine.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Codice articolo", "Descrizione articolo", "Categoria, Sottocategoria Articolo, Codice articolo", "Categoria, Sottocategoria Articolo, Descrizione", "Giacenza crescente", "Giacenza decrescente" }));
        ordine.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ordineItemStateChanged(evt);
            }
        });

        lab_categoria.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lab_categoria.setText("Categoria articoli");

        categoria.setEditable(false);
        categoria.setDbDescCampo("");
        categoria.setDbNomeCampo("categoria");
        categoria.setDbNullSeVuoto(true);
        categoria.setDbTipoCampo("");
        categoria.setDbTrovaMentreScrive(true);
        categoria.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                categoriaItemStateChanged(evt);
            }
        });
        categoria.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                categoriaPopupMenuWillBecomeVisible(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .add(14, 14, 14)
                        .add(jXDatePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(butRefresh))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(0, 0, Short.MAX_VALUE)
                                .add(esporta)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(stampa))
                            .add(labTotali, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(segna_posto_fornitore, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comPrezzi, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texArticolo))
                            .add(layout.createSequentialGroup()
                                .add(jLabel5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jLabel6)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texFornitore))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(giacenza_zero)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel7)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(ordine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(lab_categoria)
                                            .add(lab_sottocategoria))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(sottocategoria, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(categoria, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                                .add(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jLabel2, jLabel3, jLabel5}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {jLabel1, jLabel6}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {comPrezzi, deposito}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jXDatePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel3)
                        .add(jLabel4))
                    .add(butRefresh))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texFornitore, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(comPrezzi, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texArticolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lab_categoria)
                    .add(categoria, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sottocategoria, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lab_sottocategoria))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(ordine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(giacenza_zero)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(segna_posto_fornitore)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labTotali)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(esporta)
                    .add(stampa))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void filtra() {
        System.out.println("articolo_selezionato_filtro_ref.get(): " + articolo_selezionato_filtro_ref.get());
        if (texArticolo.getText().length() > 0 && articolo_selezionato_filtro_ref.get() == null) {
            System.out.println("Pattern.quote(texArticolo.getText()): " + Pattern.quote(texArticolo.getText()));
            Filter[] filterArray = {new PatternFilter("(.*" + Pattern.quote(texArticolo.getText()) + ".*)", Pattern.CASE_INSENSITIVE, 0)};
            FilterPipeline filters = new FilterPipeline(filterArray);
            ((JXTable) tabGiacenze).setFilters(filters);
        } else {
            ((JXTable) tabGiacenze).setFilters(null);
        }
        aggiornaTotali();
    }

    private void stampaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stampaActionPerformed

        SwingUtils.mouse_wait(this);
        salva();
        boolean group = true;
        if (!cu.s(ordine.getSelectedItem()).startsWith("Categoria")) {
            group = false;
        }

        try {
            BeanAdapterTableModel model = (BeanAdapterTableModel) tabGiacenze.getModel();
            ArrayList newbeans = new ArrayList();
            for (int i = 0; i < tabGiacenze.getRowCount(); i++) {
                int mi = ((JXTable) tabGiacenze).convertRowIndexToModel(i);
                Object o = model.getBeans().get(mi);
                if (o.getClass().getName().equalsIgnoreCase("it.tnx.invoicex.data.Giacenza")) {
                    newbeans.add(o);
                }
            }

            FastReportBuilder drb = new FastReportBuilder();
            //column.setPattern("$ #.00");

            Style giacStyle = new Style();
            giacStyle.setHorizontalAlign(HorizontalAlign.RIGHT);

            Style groupVariables = new Style("groupVariables");
            groupVariables.setFont(Font.ARIAL_BIG_BOLD);
            groupVariables.setBorderTop(Border.THIN);
            groupVariables.setHorizontalAlign(HorizontalAlign.LEFT);
            groupVariables.setBackgroundColor(Color.LIGHT_GRAY);

            Style groupVariablesSottocat = new Style("groupVariablesSottocat");
            groupVariablesSottocat.setFont(Font.ARIAL_MEDIUM_BOLD);
            groupVariablesSottocat.setPaddingLeft(10);
            groupVariablesSottocat.setHorizontalAlign(HorizontalAlign.LEFT);
            groupVariablesSottocat.setBackgroundColor(Color.LIGHT_GRAY);

            AbstractColumn colCategoria = ColumnBuilder.getNew()
                    .setColumnProperty("categoria", String.class.getName())
                    .setTitle("Categoria")
                    .setStyle(groupVariables)
                    .build();

            AbstractColumn colSottocategoria = ColumnBuilder.getNew()
                    .setColumnProperty("sottocategoria", String.class.getName())
                    .setTitle("Sottocategoria")
                    .setStyle(groupVariablesSottocat)
                    .build();

            AbstractColumn colArticolo = ColumnBuilder.getNew()
                    .setColumnProperty("codice_articolo", String.class.getName())
                    .setTitle("Articolo")
                    .setWidth(18)
                    .build();

            AbstractColumn colGiacenza = ColumnBuilder.getNew()
                    .setColumnProperty("giacenza", Double.class.getName())
                    .setTitle("Giacenza")
                    .setWidth(18)
                    .setPattern("0.###")
                    .setStyle(giacStyle)
                    .build();

            AbstractColumn colValore = ColumnBuilder.getNew()
                    .setColumnProperty("prezzo", Double.class.getName())
                    .setTitle("Valore")
                    .setWidth(20)
                    .setPattern("€ #,##0.00###")
                    .setStyle(giacStyle)
                    .build();

            AbstractColumn colValoreTot = ColumnBuilder.getNew()
                    .setTitle("Valore totale")
                    .setWidth(20)
                    .setPattern("€ #,##0.00")
                    .setStyle(giacStyle)
                    .setCustomExpression(new CustomExpression() {
                        public Object evaluate(Map fields, Map variables, Map parameters) {
                            double prezzo = cu.d0(fields.get("prezzo"));
                            double giac = cu.d0(fields.get("giacenza"));
                            return prezzo * giac;
                        }

                        public String getClassName() {
                            return Double.class.getName();
                        }
                    })
                    .build();

            DJGroup g1 = null;
            DJGroup g2 = null;
            if (group) {
                GroupBuilder gb1 = new GroupBuilder();

                g1 = gb1.setCriteriaColumn((PropertyColumn) colCategoria)
                        .setGroupLayout(GroupLayout.VALUE_IN_HEADER) // tells the group how to be shown, there are manyposibilities, see the GroupLayout for more.
                        .setDefaultHeaderVariableStyle(groupVariables)
                        .build();

                GroupBuilder gb2 = new GroupBuilder();
                g2 = gb2.setCriteriaColumn((PropertyColumn) colSottocategoria)
                        .setGroupLayout(GroupLayout.VALUE_IN_HEADER) // tells the group how to be shown, there are manyposibilities, see the GroupLayout for more.
                        .setDefaultHeaderVariableStyle(groupVariablesSottocat)
                        .build();
            }

            Style totalStyle = new Style();
            totalStyle.setBorderTop(Border.PEN_1_POINT);
            totalStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
            totalStyle.setBackgroundColor(new Color(230, 230, 230));
            totalStyle.setTransparency(Transparency.OPAQUE);

            Style atStyle2 = new StyleBuilder(true).setFont(Font.ARIAL_SMALL).build();

            if (group) {
                drb.addGroup(g1);
                drb.addGroup(g2);

                drb.addColumn(colCategoria);
                drb.addColumn(colSottocategoria);
            }

            drb.addColumn(colArticolo);
            drb.addColumn("Descrizione", "descrizione_articolo", String.class.getName(), 50);
            drb.addColumn(colGiacenza);

            Object deposito_key = null;
            try {
                deposito_key = ((KeyValuePair) deposito.getSelectedItem()).key;
            } catch (Exception e) {
            }
            if (deposito_key == Magazzino.Depositi.TUTTI_DETTAGLIO) {
                drb.addColumn("Deposito", "deposito", String.class.getName(), 40);
            }

            if (matricola) {
                drb.addColumn("Matricola", "matricola", String.class.getName(), 20);
            }
            if (lotti) {
                drb.addColumn("Lotto", "lotto", String.class.getName(), 20);
            }
            if (comPrezzi.getSelectedIndex() >= 1) {
                drb.addColumn(colValore);
                drb.addColumn(colValoreTot);
            }

            drb.addGlobalFooterVariable(colGiacenza, DJCalculation.SUM, totalStyle);
            if (comPrezzi.getSelectedIndex() >= 1) {
                drb.addGlobalFooterVariable(colValoreTot, DJCalculation.SUM, totalStyle);
            }

            String sottotitolo = "Giacenza al " + DateUtils.formatDate(jXDatePicker1.getDate() == null ? new Date() : jXDatePicker1.getDate());
            if (deposito_key == Magazzino.Depositi.TUTTI_RIEPILOGATIVO) {
                sottotitolo += ", Deposito: Tutti riepilogativo";
            } else if (deposito_key == Magazzino.Depositi.TUTTI_DETTAGLIO) {
                sottotitolo += ", Deposito: Tutti con dettaglio";
            } else {
                sottotitolo += ", Deposito: " + deposito.getSelectedItem();
            }
            if (comPrezzi.getSelectedIndex() >= 1) {
                sottotitolo += ", Valori: " + comPrezzi.getSelectedItem();
            }
            if (fornitore_selezionato_filtro_ref.get() != null) {
                sottotitolo += "\\nFornitore abituale: " + fornitore_selezionato_filtro_ref.get().ragione_sociale + " [" + fornitore_selezionato_filtro_ref.get().codice + "]";
            }

            DynamicReport dr = drb.setTitle("Giacenze")
                    .setColumnSpace(20)
                    .setSubtitle(sottotitolo)
                    .setPrintBackgroundOnOddRows(true)
                    .setUseFullPageWidth(true)
                    .addAutoText(AutoText.AUTOTEXT_PAGE_X_SLASH_Y, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_RIGHT, 30, 30, atStyle2)
                    .addAutoText(AutoText.AUTOTEXT_CREATED_ON, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_LEFT, AutoText.PATTERN_DATE_DATE_TIME, 200, 200, atStyle2)
                    .setReportLocale(new Locale("it", "IT"))
                    .build();

            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(newbeans);
            JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);
            SwingUtils.mouse_def(this);
            InvoicexUtil.apriStampa(jp);
        } catch (Exception e) {
            SwingUtils.mouse_def(this);
            e.printStackTrace();
            SwingUtils.showExceptionMessage(this, e);
        }


    }//GEN-LAST:event_stampaActionPerformed

    private void esportaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_esportaActionPerformed
        SwingUtils.mouse_wait(main.getPadre());
        salva();
        PrintSimpleTable print = new PrintSimpleTable(tabGiacenze);
        int[] hw = null;
        if (matricola || lotti) {
            if (String.valueOf(comPrezzi.getSelectedItem()).equals("<non stampare prezzi>")) {
                hw = new int[]{10, 50, 10, 10};
            } else {
                hw = new int[]{10, 50, 10, 10, 10};
            }
        } else if (String.valueOf(comPrezzi.getSelectedItem()).equals("<non stampare prezzi>")) {
            hw = new int[]{10, 50, 10};
        } else {
            hw = new int[]{10, 50, 10, 10};
        }

        Object deposito_key = null;
        try {
            deposito_key = ((KeyValuePair) deposito.getSelectedItem()).key;
        } catch (Exception e) {
        }
        int[] hw2 = hw;
        if (deposito_key == Magazzino.Depositi.TUTTI_DETTAGLIO) {
            hw2 = new int[hw.length + 1];
            for (int i = 0; i < hw.length; i++) {
                hw2[i] = hw[i];
            }
            hw2[hw.length] = 20;
        }

        String nomeFile = print.printExcel("Giacenze al " + DateUtils.formatDate(jXDatePicker1.getDate()), hw2, "", "");
//        SwingUtils.open(new File(nomeFile));
        Util.start2(nomeFile);

        SwingUtils.mouse_def(main.getPadre());
    }//GEN-LAST:event_esportaActionPerformed

    private void texArticoloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texArticoloActionPerformed
        aggiorna();
    }//GEN-LAST:event_texArticoloActionPerformed

    private void giacenza_zeroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_giacenza_zeroActionPerformed
        aggiorna();
    }//GEN-LAST:event_giacenza_zeroActionPerformed

    private void comPrezziActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comPrezziActionPerformed
        aggiorna();
    }//GEN-LAST:event_comPrezziActionPerformed

    private void depositoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_depositoActionPerformed
        aggiorna();
    }//GEN-LAST:event_depositoActionPerformed

    private void butRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butRefreshActionPerformed
        aggiorna();
    }//GEN-LAST:event_butRefreshActionPerformed

    private void jXDatePicker1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jXDatePicker1ActionPerformed
        comPrezziActionPerformed(null);
    }//GEN-LAST:event_jXDatePicker1ActionPerformed

    private void texFornitoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texFornitoreActionPerformed
        aggiorna();
    }//GEN-LAST:event_texFornitoreActionPerformed

    private void categoriaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_categoriaItemStateChanged
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            System.out.println("SOTTOCATEGORIA  clear list");
            sottocategoria.dbClearList();
        } else if (evt.getStateChange() == ItemEvent.SELECTED) {
            System.out.println("SOTTOCATEGORIA  carico ");
            sottocategoria.dbClearList();
            sottocategoria.dbAddElement("", null);
            sottocategoria.dbOpenList(Db.getConn(), "select sottocategoria, id from sottocategorie_articoli where id_padre = " + Db.pc(categoria.getSelectedKey(), Types.INTEGER) + " order by sottocategoria", null, false);
            if (!loading) {
                aggiorna();
            }
        }
    }//GEN-LAST:event_categoriaItemStateChanged

    private void categoriaPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_categoriaPopupMenuWillBecomeVisible
        // TODO add your handling code here:
    }//GEN-LAST:event_categoriaPopupMenuWillBecomeVisible

    private void sottocategoriaPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_sottocategoriaPopupMenuWillBecomeVisible
        // TODO add your handling code here:
    }//GEN-LAST:event_sottocategoriaPopupMenuWillBecomeVisible

    private void sottocategoriaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sottocategoriaItemStateChanged
        if (!loading) {
            aggiorna();
        }
    }//GEN-LAST:event_sottocategoriaItemStateChanged

    private void ordineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ordineItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED && !loading) {
            aggiorna();
        }
    }//GEN-LAST:event_ordineItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butRefresh;
    public tnxbeans.tnxComboField categoria;
    private javax.swing.JComboBox comPrezzi;
    private javax.swing.JComboBox deposito;
    private javax.swing.JButton esporta;
    private javax.swing.JCheckBox giacenza_zero;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXDatePicker jXDatePicker1;
    private javax.swing.JLabel labTotali;
    public javax.swing.JLabel lab_categoria;
    public javax.swing.JLabel lab_sottocategoria;
    private javax.swing.JComboBox ordine;
    public javax.swing.JLabel segna_posto_fornitore;
    public tnxbeans.tnxComboField sottocategoria;
    private javax.swing.JButton stampa;
    public javax.swing.JTable tabGiacenze;
    private javax.swing.JTextField texArticolo;
    private javax.swing.JTextField texFornitore;
    // End of variables declaration//GEN-END:variables

    public void aggiornaTotali() {
        //totali
        double tot_qta = 0;
        double tot_valore = 0;
        labTotali.setText("");
        try {
            int col_giac = tabGiacenze.getColumnModel().getColumnIndex("giacenza");
            int col_valore = -1;
            try {
                col_valore = tabGiacenze.getColumnModel().getColumnIndex("prezzo");
            } catch (Exception e) {
            }
            for (int row = 0; row < tabGiacenze.getRowCount(); row++) {
                double qta = CastUtils.toDouble0(tabGiacenze.getValueAt(row, col_giac));
                double valore = 0;
                if (col_valore >= 0) {
                    valore = CastUtils.toDouble0(tabGiacenze.getValueAt(row, col_valore));
                }
                tot_qta += qta;
                tot_valore += (qta * valore);
            }
            labTotali.setText("<html>Totale Quantita' <b>" + FormatUtils.formatPerc(tot_qta) + "</b> / Totale Valore <b>" + FormatUtils.formatEuroIta(tot_valore) + "</b></html>");
        } catch (Exception e) {
        }

    }

    public void aggiorna() {
        if (loading) {
            return;
        }

        SwingUtils.mouse_wait(this);

        KeyValuePair item = (KeyValuePair) comPrezzi.getSelectedItem();
        if (item == null) {
            listino = new Integer(0);
        } else {
            listino = item.key;
            if (listino.equals("null")) {
                listino = new Integer(0);
            } else if (listino.equals("***EL1***")) {
                listino = new Integer(1);
            } else if (listino.equals("***EL2***")) {
                listino = new Integer(2);
            } else if (listino.equals("***EL3***")) {
                listino = new Integer(3);
            } else if (listino.equals("***EL4***")) {
                listino = new Integer(4);
            } else if (listino.equals("***EL5***")) {
                listino = new Integer(5);
            }
        }

        SwingWorker w = new SwingWorker() {
            BeanAdapterTableModel tableModel;

            @Override
            protected Object doInBackground() throws Exception {
                Magazzino m = new Magazzino();
                ArrayList beans;
                ArrayList<String> colonne_int = new ArrayList();
                ArrayList<String> colonne_campi = new ArrayList();
                String[][] cols;

                colonne_int.add("codice_articolo");
                colonne_campi.add("articolo");
                colonne_int.add("descrizione_articolo");
                colonne_campi.add("descrizione");
                colonne_int.add("giacenza");
                colonne_campi.add("giacenza");

                Object deposito_key = null;
                try {
                    deposito_key = ((KeyValuePair) deposito.getSelectedItem()).key;
                } catch (Exception e) {
                }

                if (deposito_key == Magazzino.Depositi.TUTTI_DETTAGLIO) {
                    colonne_int.add("deposito");
                    colonne_campi.add("deposito");
                }

                Magazzino magazzino = new Magazzino();
                String articolo = null;
                if (articolo_selezionato_filtro_ref != null && articolo_selezionato_filtro_ref.get() != null) {
                    articolo = articolo_selezionato_filtro_ref.get().codice;
                }
                if (fornitore_selezionato_filtro_ref != null && fornitore_selezionato_filtro_ref.get() != null) {
                    magazzino.filtro_fornitore_abituale = cu.i(fornitore_selezionato_filtro_ref.get().codice);
                }
                if (categoria.getSelectedIndex() != -1) {
                    magazzino.filtro_categoria = cu.i(categoria.getSelectedKey());
                }
                if (sottocategoria.getSelectedIndex() != -1) {
                    magazzino.filtro_sottocategoria = cu.i(sottocategoria.getSelectedKey());
                }
                if (matricola) {
                    beans = magazzino.getGiacenza(true, articolo, listino, jXDatePicker1.getDate(), false, giacenza_zero.isSelected(), false, deposito_key, fornitore);
                    colonne_int.add("matricola");
                    colonne_campi.add("matricola");
                } else if (lotti) {
                    beans = magazzino.getGiacenzaPerLotti(articolo, listino, jXDatePicker1.getDate(), giacenza_zero.isSelected(), deposito_key);
                    colonne_int.add("lotto");
                    colonne_campi.add("lotto");
                } else {
                    beans = magazzino.getGiacenza(false, articolo, listino, jXDatePicker1.getDate(), false, giacenza_zero.isSelected(), true, deposito_key, fornitore, true);
                }

                if (!listino.equals(new Integer(0))) {
                    colonne_int.add("prezzo");
                    colonne_campi.add("prezzo");
                }

                cols = new String[colonne_int.size()][2];
                for (int i = 0; i < colonne_int.size(); i++) {
                    cols[i][0] = colonne_int.get(i);
                    cols[i][1] = colonne_campi.get(i);
                }

                ordina(beans);
                //aggiungo categorie e sottocategorie
                if (cu.s(ordine.getSelectedItem()).startsWith("Categoria")) {
                    aggiungiCategorie(beans);
                }

                tableModel = new BeanAdapterTableModel(beans, cols);
                return null;
            }

            @Override
            protected void done() {
                super.done();

                try {
                    tabGiacenze.setModel(tableModel);

                    tabGiacenze.getColumn("articolo").setCellRenderer(new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            JLabel lab = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            if (!cu.s(ordine.getSelectedItem()).startsWith("Categoria")) {
                                lab.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 4, 0, 0), BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY)));
                                return lab;
                            }
                            Object b = ((BeanAdapterTableModel) table.getModel()).getBeans().get(row);
                            if (b.getClass().getName().equalsIgnoreCase("it.tnx.invoicex.data.Giacenza")) {
                                lab.setText("      " + lab.getText());
                                lab.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
                            } else {
                                lab.setFont(lab.getFont().deriveFont(java.awt.Font.BOLD));
                                lab.setBorder(BorderFactory.createEmptyBorder());
                            }
                            return lab;
                        }

                    });

                    tabGiacenze.getColumn("descrizione").setCellRenderer(new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            JLabel lab = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            Object b = ((BeanAdapterTableModel) table.getModel()).getBeans().get(row);
                            if (b.getClass().getName().equalsIgnoreCase("it.tnx.invoicex.data.Giacenza")) {
                                lab.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 4, 0, 0), BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY)));
                            } else {
                                lab.setBorder(BorderFactory.createEmptyBorder());
                            }
                            return lab;
                        }

                    });

                    tabGiacenze.getColumn("giacenza").setCellRenderer(new RendererUtils.NumberRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            JLabel lab = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            Object b = ((BeanAdapterTableModel) table.getModel()).getBeans().get(row);
                            if (b.getClass().getName().equalsIgnoreCase("it.tnx.invoicex.data.Giacenza") && comPrezzi.getSelectedIndex() > 0) {
                                lab.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
                            } else {
                                lab.setBorder(BorderFactory.createEmptyBorder());
                            }
                            if (b.getClass().getName().equalsIgnoreCase("it.tnx.invoicex.data.Giacenza")) {
                                lab.setText(lab.getText() + " ");
                            } else {
                                lab.setText("");
                            }
                            return lab;
                        }
                    });
                    try {
                        tabGiacenze.getColumn("prezzo").setCellRenderer(new RendererUtils.CurrencyRenderer() {
                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                                JLabel lab = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                Object b = ((BeanAdapterTableModel) table.getModel()).getBeans().get(row);
                                if (!b.getClass().getName().equalsIgnoreCase("it.tnx.invoicex.data.Giacenza")) {
                                    lab.setText("");
                                }
                                return lab;
                            }
                        });
                    } catch (Exception e) {
                    }

                    if (articolo_selezionato_filtro_ref != null && articolo_selezionato_filtro_ref.get() != null) {
                        ((JXTable) tabGiacenze).setFilters(null);
                    }

                    aggiornaTotali();
                } catch (Exception e) {
                    SwingUtils.showExceptionMessage(JInternalFrameGiacenze.this, e);
                }

                SwingUtils.mouse_def(JInternalFrameGiacenze.this);
            }

            private void aggiungiCategorie(ArrayList beans) {
                Integer old_cat_id = null;
                Integer old_scat_id = null;
                Iterator iter = beans.iterator();
                for (int i = 0; i < beans.size(); i++) {
                    Giacenza g = (Giacenza) beans.get(i);
                    if (g.getCategoria_id() != old_cat_id) {
                        GiacenzaCategoria gcat = new GiacenzaCategoria();
                        gcat.setCodice_articolo(cu.s(g.getCategoria()) + " [" + g.getCategoria_id() + "]");
                        beans.add(i, gcat);
                        i++;
                    }
                    if (g.getSottocategoria_id() != old_scat_id) {
                        GiacenzaSottocategoria gscat = new GiacenzaSottocategoria();
                        gscat.setCodice_articolo("   " + cu.s(g.getSottocategoria()) + " [" + g.getSottocategoria_id() + "]");
                        beans.add(i, gscat);
                        i++;
                    }
                    if (g.getCategoria_id() != old_cat_id) {
                        old_scat_id = null;
                    } else {
                        old_scat_id = g.getSottocategoria_id();
                    }
                    old_cat_id = g.getCategoria_id();
                }
            }

        };
        w.execute();
    }

    static public class GiacenzaCategoria extends Giacenza {

    }

    static public class GiacenzaSottocategoria extends Giacenza {

    }

    private void ordina(ArrayList beans) {
        if (cu.s(ordine.getSelectedItem()).equals("Codice articolo")) {
            return;
        }

        Collections.sort(beans, new Comparator() {

            public int compare(Object o1, Object o2) {
                String s1 = null;
                String s2 = null;
                //Codice articolo, Descrizione articolo, Categoria, Sottocategoria Articolo, Codice articolo, Categoria, Sottocategoria Articolo, Descrizione, Giacenza crescente, Giacenza decrescente
                if (cu.s(ordine.getSelectedItem()).equals("Descrizione articolo")) {
                    s1 = ((Giacenza) o1).getDescrizione_articolo();
                    s2 = ((Giacenza) o2).getDescrizione_articolo();
                } else if (cu.s(ordine.getSelectedItem()).equals("Categoria, Sottocategoria Articolo, Codice articolo")) {
                    s1 = StringUtils.rightPad(cu.s(((Giacenza) o1).getCategoria()), 50, " ") + StringUtils.rightPad(cu.s(((Giacenza) o1).getSottocategoria()), 50, " ") + ((Giacenza) o1).getCodice_articolo();
                    s2 = StringUtils.rightPad(cu.s(((Giacenza) o2).getCategoria()), 50, " ") + StringUtils.rightPad(cu.s(((Giacenza) o2).getSottocategoria()), 50, " ") + ((Giacenza) o2).getCodice_articolo();
                } else if (cu.s(ordine.getSelectedItem()).equals("Categoria, Sottocategoria Articolo, Descrizione")) {
                    s1 = StringUtils.rightPad(cu.s(((Giacenza) o1).getCategoria()), 50, " ") + StringUtils.rightPad(cu.s(((Giacenza) o1).getSottocategoria()), 50, " ") + ((Giacenza) o1).getDescrizione_articolo();
                    s2 = StringUtils.rightPad(cu.s(((Giacenza) o2).getCategoria()), 50, " ") + StringUtils.rightPad(cu.s(((Giacenza) o2).getSottocategoria()), 50, " ") + ((Giacenza) o2).getDescrizione_articolo();
                } else if (cu.s(ordine.getSelectedItem()).equals("Giacenza crescente")) {
                    return cu.d0(((Giacenza) o1).getGiacenza()).compareTo(cu.d0(((Giacenza) o2).getGiacenza()));
                } else if (cu.s(ordine.getSelectedItem()).equals("Giacenza decrescente")) {
                    return cu.d0(((Giacenza) o2).getGiacenza()).compareTo(cu.d0(((Giacenza) o1).getGiacenza()));
                }

                return s1.compareTo(s2);
            }
        });
    }

    private void salva() {
        main.fileIni.setValue("pref", "stampa_giacenze_ordine", ordine.getSelectedItem());
        main.fileIni.setValue("pref", "stampa_giacenze_filtro_cat", categoria.getSelectedItem());
        main.fileIni.setValue("pref", "stampa_giacenze_filtro_sottocat", sottocategoria.getSelectedItem());
        main.fileIni.setValue("pref", "stampa_giacenze_deposito", deposito.getSelectedIndex());
        main.fileIni.setValue("pref", "stampa_giacenze_prezzi", comPrezzi.getSelectedIndex());
        //main.fileIni.setValue("pref", "stampa_giacenze_fornitore", texFornitore.getText());
        main.fileIni.setValue("pref", "stampa_giacenze_zero", giacenza_zero.isSelected());
    }
}
