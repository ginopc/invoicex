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
package gestioneFatture.magazzino;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.CustomExpression;
import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.builders.StyleBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import com.jidesoft.hints.AbstractListIntelliHints;
import it.tnx.Db;
import java.text.*;
import javax.swing.text.*;
import java.sql.*;
import java.io.*;

import gestioneFatture.*;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugFastUtils;
import it.tnx.commons.KeyValuePair;
import it.tnx.commons.StringUtilsTnx;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.gui.DateDocument;
import it.tnx.gui.MyBasicArrowButton;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.Magazzino;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.commons.lang.StringUtils;

public class JInternalFrameStampaMovimenti extends javax.swing.JInternalFrame {

    MaskFormatter formatter = null;
    AtomicReference<ArticoloHint> articolo_selezionato_ref = new AtomicReference(null);

    AbstractListIntelliHints alRicercaCliFor = null;
    Integer codiceCliFor = null;

    public JInternalFrameStampaMovimenti() {

        try {
            formatter = new MaskFormatter("##/##/##");
            formatter.setPlaceholderCharacter('_');
        } catch (Exception err) {
            err.printStackTrace();
        }

        initComponents();

        prezzi.setSelected(main.fileIni.getValueBoolean("pref", "stampa_movimenti_includi_prezzi", true));

        cheGiacenze.setVisible(false);

        this.comCausale.dbClearList();
        this.comCausale.dbAddElement("<tutte le causali>", "T");
        this.comCausale.dbOpenList(Db.getConn(), "select descrizione, codice from tipi_causali_magazzino");
//        this.forDaData.setText("01/01/" + it.tnx.Util.getYearString());

        KeyValuePair kvd1 = new KeyValuePair(Magazzino.Depositi.TUTTI, "<tutti i depositi>");
        SwingUtils.initJComboFromDb(deposito, Db.getConn(), "select id, nome from depositi order by nome", "id", "nome", kvd1);

        InvoicexUtil.getArticoloIntelliHints(articolo, this, articolo_selezionato_ref, null, null);

        DateDocument.installDateDocument(daData.getEditor());
        DateDocument.installDateDocument(aData.getEditor());

        categoria.dbAddElement("", null);
        categoria.dbOpenList(Db.getConn(), "select categoria, id from categorie_articoli order by categoria", null, false);
        sottocategoria.dbClearList();

        comCatCliFor.dbOpenList(Db.getConn(), "select descrizione, id from tipi_clie_forn order by descrizione", null, false);

        alRicercaCliFor = new AbstractListIntelliHints(texCliFor) {
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
                        String word = current_search;
                        String content = tipo;
                        Color c = null;
                        if (!isSelected) {
                            c = new Color(240, 240, 100);
                        } else {
                            c = new Color(100, 100, 40);
                        }
                        String rgb = Integer.toHexString(c.getRGB());
                        rgb = rgb.substring(2, rgb.length());

                        content = StringUtilsTnx.highlightWord(content, word, "<span style='background-color: " + rgb + "'>", "</span>");

                        if (((ClienteHint) value).obsoleto) {
                            content = "<span style='color: FF0000'>" + content + " (Obsoleto)</span>";
                        }
                        lab.setText("<html>" + content + "</html>");
                        System.out.println(index + ":" + content);
                        return lab;
                    }
                });
                return list;
            }

            public boolean updateHints(Object arg0) {
                SwingUtils.mouse_wait();
                current_search = arg0.toString();
                Connection conn;
                try {
                    conn = Db.getConn();

                    String sql = ""
                            + "SELECT codice, ragione_sociale, obsoleto FROM clie_forn"
                            + " where (codice like '%" + Db.aa(current_search) + "%'"
                            + " or ragione_sociale like '%" + Db.aa(current_search) + "%'"
                            + " ) and ragione_sociale != ''"
                            + " order by ragione_sociale, codice limit 50";

                    System.out.println("sql ricerca:" + sql);
                    ResultSet rs = DbUtils.tryOpenResultSet(conn, sql);
                    Vector v = new Vector();

                    while (rs.next()) {
                        ClienteHint cliente = new ClienteHint();
                        cliente.codice = rs.getString(1);
                        cliente.ragione_sociale = rs.getString(2);
                        cliente.obsoleto = rs.getBoolean(3);
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
                super.acceptHint(arg0);
                try {
                    if (((ClienteHint) arg0).codice.equals("*")) {
                        codiceCliFor = null;
                    } else {
                        codiceCliFor = cu.i(((ClienteHint) arg0).codice);
                    }
                    texCliFor.setText(((ClienteHint) arg0).ragione_sociale);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

    }

    private String getDateToMysql(String value) {
        String ret = "";
        DateFormat myFormat = new SimpleDateFormat("dd/MM/yy");
        DateFormat myFormatSql = new SimpleDateFormat("yyyy-MM-dd");
        myFormat.setLenient(false);
        try {
            java.util.Date myDate = myFormat.parse(value);
            ret = myFormatSql.format(myDate);
            return ret;
        } catch (Exception err) {
            return "";
        }
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        comCausale = new tnxbeans.tnxComboField();
        jLabel3 = new javax.swing.JLabel();
        cheGiacenze = new javax.swing.JCheckBox();
        deposito = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        articolo = new javax.swing.JTextField();
        daData = new org.jdesktop.swingx.JXDatePicker();
        aData = new org.jdesktop.swingx.JXDatePicker();
        lab_categoria = new javax.swing.JLabel();
        categoria = new tnxbeans.tnxComboField();
        lab_sottocategoria = new javax.swing.JLabel();
        sottocategoria = new tnxbeans.tnxComboField();
        jLabel11 = new javax.swing.JLabel();
        texCliFor = new javax.swing.JTextField();
        apriCliFor = new MyBasicArrowButton(BasicArrowButton.SOUTH, UIManager.getColor("ComboBox.buttonBackground"), UIManager.getColor("ComboBox.buttonShadow"), UIManager.getColor("ComboBox.buttonDarkShadow"), UIManager.getColor("ComboBox.buttonHighlight"));
        comCatCliFor = new tnxbeans.tnxComboField();
        jLabel227 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        butStampa2 = new javax.swing.JButton();
        butStampa = new javax.swing.JButton();
        butStampa1 = new javax.swing.JButton();
        prezzi = new javax.swing.JCheckBox();
        jLabel17 = new javax.swing.JLabel();
        lotto = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        matricola = new javax.swing.JTextField();
        lotto_come = new javax.swing.JComboBox();
        matricola_come = new javax.swing.JComboBox();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Stampa movimenti di magazzino");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Da data");

        jLabel2.setText("A data");

        comCausale.setDbNomeCampo("causale");
        comCausale.setDbRiempire(false);
        comCausale.setDbSalvare(false);
        comCausale.setDbTrovaMentreScrive(true);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Causale");

        cheGiacenze.setText("Giacenza");
        cheGiacenze.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        cheGiacenze.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cheGiacenzeActionPerformed(evt);
            }
        });

        deposito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                depositoActionPerformed(evt);
            }
        });

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Deposito");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Articolo");

        articolo.setColumns(30);

        daData.setName("daData"); // NOI18N
        daData.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                daDataPropertyChange(evt);
            }
        });

        aData.setName("data"); // NOI18N
        aData.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                aDataPropertyChange(evt);
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

        lab_sottocategoria.setText("sotto categoria");

        sottocategoria.setEditable(false);
        sottocategoria.setDbDescCampo("");
        sottocategoria.setDbNomeCampo("sottocategoria");
        sottocategoria.setDbNullSeVuoto(true);
        sottocategoria.setDbRiempire(false);
        sottocategoria.setDbTipoCampo("");
        sottocategoria.setDbTrovaMentreScrive(true);
        sottocategoria.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                sottocategoriaPopupMenuWillBecomeVisible(evt);
            }
        });

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Cliente/Fornitore");

        apriCliFor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apriCliForActionPerformed(evt);
            }
        });

        comCatCliFor.setDbDescCampo("Categoria");
        comCatCliFor.setDbNomeCampo("tipo2");
        comCatCliFor.setDbTrovaMentreScrive(true);

        jLabel227.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel227.setText("Categoria Cliente/Fornitore");
        jLabel227.setToolTipText("Categoria");

        butStampa2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/pdf-icon-16.png"))); // NOI18N
        butStampa2.setText("PDF");
        butStampa2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butStampa2ActionPerformed(evt);
            }
        });

        butStampa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-print.png"))); // NOI18N
        butStampa.setText("Stampa");
        butStampa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butStampaActionPerformed(evt);
            }
        });

        butStampa1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-undo.png"))); // NOI18N
        butStampa1.setText("Annulla");
        butStampa1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butStampa1ActionPerformed(evt);
            }
        });

        prezzi.setText("Includi prezzi");

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("Lotto");
        jLabel17.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        lotto.setColumns(20);
        lotto.setMaximumSize(new java.awt.Dimension(150, 2147483647));
        lotto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lottoKeyReleased(evt);
            }
        });

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("Matricola");
        jLabel18.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        matricola.setColumns(20);
        matricola.setMaximumSize(new java.awt.Dimension(150, 2147483647));
        matricola.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                matricolaKeyReleased(evt);
            }
        });

        lotto_come.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Uguale a", "Contiene", "Inizia con", "Finisce con" }));

        matricola_come.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Uguale a", "Contiene", "Inizia con", "Finisce con" }));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSeparator1)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(daData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(aData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel3)
                                    .add(jLabel5))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(comCausale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(cheGiacenze))))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(lab_categoria)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(categoria, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(lab_sottocategoria)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(sottocategoria, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jLabel227)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comCatCliFor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(0, 38, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(butStampa1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(butStampa)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(butStampa2))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel11)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(prezzi)
                                .add(0, 0, Short.MAX_VALUE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(texCliFor)
                                .add(1, 1, 1)
                                .add(apriCliFor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel17)
                            .add(jLabel4)
                            .add(jLabel18))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(articolo)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(lotto_come, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(lotto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(matricola_come, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(matricola, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {jLabel1, jLabel11, jLabel227, jLabel3, jLabel4, jLabel5, lab_categoria}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(daData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(aData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(comCausale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cheGiacenze))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lab_categoria)
                    .add(lab_sottocategoria)
                    .add(categoria, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sottocategoria, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(articolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel17)
                    .add(lotto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lotto_come, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel18)
                    .add(matricola, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(matricola_come, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel227)
                    .add(comCatCliFor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(texCliFor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel11))
                    .add(apriCliFor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(prezzi)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 109, Short.MAX_VALUE)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(butStampa2)
                        .add(butStampa))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, butStampa1))
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


  private void butStampa2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butStampa2ActionPerformed
      main.fileIni.setValue("pref", "stampa_movimenti_includi_prezzi", prezzi.isSelected());
      stampa(true);
  }//GEN-LAST:event_butStampa2ActionPerformed

  private void cheGiacenzeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cheGiacenzeActionPerformed
      if (cheGiacenze.isSelected() == true) {
          this.comCausale.setEnabled(false);
      } else {
          this.comCausale.setEnabled(true);
      }
  }//GEN-LAST:event_cheGiacenzeActionPerformed

  private void butStampa1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butStampa1ActionPerformed
      this.dispose();
  }//GEN-LAST:event_butStampa1ActionPerformed

  private void butStampaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butStampaActionPerformed
      main.fileIni.setValue("pref", "stampa_movimenti_includi_prezzi", prezzi.isSelected());
      stampa(false);
  }//GEN-LAST:event_butStampaActionPerformed

    private void depositoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_depositoActionPerformed

    }//GEN-LAST:event_depositoActionPerformed

    private void daDataPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_daDataPropertyChange

    }//GEN-LAST:event_daDataPropertyChange

    private void aDataPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_aDataPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_aDataPropertyChange

    private void categoriaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_categoriaItemStateChanged
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            System.out.println("SOTTOCATEGORIA  clear list");
            sottocategoria.dbClearList();
        } else if (evt.getStateChange() == ItemEvent.SELECTED) {
            System.out.println("SOTTOCATEGORIA  carico ");
            sottocategoria.dbClearList();
            sottocategoria.dbAddElement("", null);
            sottocategoria.dbOpenList(Db.getConn(), "select sottocategoria, id from sottocategorie_articoli where id_padre = " + Db.pc(categoria.getSelectedKey(), Types.INTEGER) + " order by sottocategoria", null, false);
        }
    }//GEN-LAST:event_categoriaItemStateChanged

    private void categoriaPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_categoriaPopupMenuWillBecomeVisible
        // TODO add your handling code here:
    }//GEN-LAST:event_categoriaPopupMenuWillBecomeVisible

    private void sottocategoriaPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_sottocategoriaPopupMenuWillBecomeVisible
        // TODO add your handling code here:
    }//GEN-LAST:event_sottocategoriaPopupMenuWillBecomeVisible

    private void apriCliForActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apriCliForActionPerformed
        alRicercaCliFor.showHints();
    }//GEN-LAST:event_apriCliForActionPerformed

    private void lottoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lottoKeyReleased

    }//GEN-LAST:event_lottoKeyReleased

    private void matricolaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_matricolaKeyReleased

    }//GEN-LAST:event_matricolaKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXDatePicker aData;
    private javax.swing.JButton apriCliFor;
    private javax.swing.JTextField articolo;
    private javax.swing.JButton butStampa;
    private javax.swing.JButton butStampa1;
    private javax.swing.JButton butStampa2;
    public tnxbeans.tnxComboField categoria;
    private javax.swing.JCheckBox cheGiacenze;
    private tnxbeans.tnxComboField comCatCliFor;
    private tnxbeans.tnxComboField comCausale;
    private org.jdesktop.swingx.JXDatePicker daData;
    private javax.swing.JComboBox deposito;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel227;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    public javax.swing.JLabel lab_categoria;
    public javax.swing.JLabel lab_sottocategoria;
    private javax.swing.JTextField lotto;
    private javax.swing.JComboBox lotto_come;
    private javax.swing.JTextField matricola;
    private javax.swing.JComboBox matricola_come;
    private javax.swing.JCheckBox prezzi;
    public tnxbeans.tnxComboField sottocategoria;
    public javax.swing.JTextField texCliFor;
    // End of variables declaration//GEN-END:variables

    private void stampa(boolean pdf) {
        Object deposito_key = null;
        try {
            deposito_key = ((KeyValuePair) deposito.getSelectedItem()).key;
        } catch (Exception e) {
        }

        String sql = "";
        ResultSet resu;
        //select per stampa movimenti semplice
        sql += "select DATE_FORMAT(m.data, '%d/%m/%y') as data, tipi_causali_magazzino.descrizione as causale, articolo, m.lotto, m.matricola, articoli.descrizione as desc_art \n"
                + ", m.quantita * tipi_causali_magazzino.segno as quantita \n";
        sql += ", CONCAT(dep.nome, ' [', dep.id, ']') as deposito \n";

        sql += ", IFNULL(tdcf.codice,IFNULL(tfcf.codice,IFNULL(tdacf.codice,tfacf.codice))) as clifor \n";
        sql += ", IFNULL(tdcf.ragione_sociale,IFNULL(tfcf.ragione_sociale,IFNULL(tdacf.ragione_sociale,tfacf.ragione_sociale))) as clifor_rs \n";
        sql += ", IF( IFNULL(rd.codice_articolo,IFNULL(rf.codice_articolo,IFNULL(rda.codice_articolo,rfa.codice_articolo))) = articolo, IFNULL(rd.prezzo_netto_unitario,IFNULL(rf.prezzo_netto_unitario,IFNULL(rda.prezzo_netto_unitario,rfa.prezzo_netto_unitario))), 0) as prezzo_netto_unitario \n";

        sql += ", cast(concat(IF(m.da_tipo_fattura = 7, 'Scontr.', "
                + "         IF(m.da_tabella = 'test_fatt', 'Fatt. Vend.', "
                + "         IF(m.da_tabella = 'test_ddt', 'DDT Vend.',"
                + "         IF(m.da_tabella = 'test_fatt_acquisto', 'Fatt. Acq.',"
                + "         IF(m.da_tabella = 'test_ddt_acquisto', 'DDT Acq.', m.da_tabella)))))"
                + "        , da_serie, ' ', da_numero, '/', da_anno) as CHAR) as origine";

        String sqlf = "";
        sqlf += " from movimenti_magazzino m left join articoli on m.articolo = articoli.codice \n";
        sqlf += " left join tipi_causali_magazzino on m.causale = tipi_causali_magazzino.codice \n";
        sqlf += " left join depositi dep ON m.deposito = dep.id \n";

        sqlf += " left join test_ddt td on m.da_tabella = 'test_ddt' and m.da_id = td.id \n";
        sqlf += " left join righ_ddt rd on m.da_tabella = 'test_ddt' and m.da_id_riga = rd.id \n";
        sqlf += " left join clie_forn tdcf on td.cliente = tdcf.codice \n";

        sqlf += " left join test_fatt tf on m.da_tabella = 'test_fatt' and m.da_id = tf.id \n";
        sqlf += " left join righ_fatt rf on m.da_tabella = 'test_fatt' and m.da_id_riga = rf.id \n";
        sqlf += " left join clie_forn tfcf on tf.cliente = tfcf.codice \n";

        sqlf += " left join test_ddt_acquisto tda on m.da_tabella = 'test_ddt_acquisto' and m.da_id = tda.id \n";
        sqlf += " left join righ_ddt_acquisto rda on m.da_tabella = 'test_ddt_acquisto' and m.da_id_riga = rda.id \n";
        sqlf += " left join clie_forn tdacf on tda.fornitore = tdacf.codice \n";

        sqlf += " left join test_fatt_acquisto tfa on m.da_tabella = 'test_fatt_acquisto' and m.da_id = tfa.id \n";
        sqlf += " left join righ_fatt_acquisto rfa on m.da_tabella = 'test_fatt_acquisto' and m.da_id_riga = rfa.id \n";
        sqlf += " left join clie_forn tfacf on tfa.fornitore = tfacf.codice \n";

        String sqlw = " where 1 = 1 \n";

        String sottotitolo = "";

        //controllo le date    
        if (this.daData.getDate() != null) {
            sqlw += "\n and m.data >= " + Db.pc(daData.getDate(), Types.DATE);
            sottotitolo += "\\n dal " + DateUtils.formatDateIta(daData.getDate());
        }

        //data di fine
        if (this.aData.getDate() != null) {
            sqlw += "\n  and m.data <= " + Db.pc(aData.getDate(), Types.DATE);
            sottotitolo += "\\n al " + DateUtils.formatDateIta(aData.getDate());
        }

        //controllo causale
        if (this.comCausale.getSelectedIndex() > 0) {
            sqlw += " and causale = " + this.comCausale.getSelectedKey();
            sottotitolo += "\\n causale: " + this.comCausale.getSelectedKey() + " " + this.comCausale.getSelectedItem();
        }

        if (deposito_key instanceof Integer) {
            sqlw += "\n  and m.deposito = " + deposito_key;
            sottotitolo += "\\n deposito: " + deposito_key + " " + ((KeyValuePair) deposito.getSelectedItem()).value;
        }

        if (articolo_selezionato_ref.get() != null) {
            try {
                sqlw += "\n  and articolo = " + Db.pc(articolo_selezionato_ref.get().codice, Types.VARCHAR);
                sottotitolo += "\\n articolo: " + articolo_selezionato_ref.get().codice + " " + articolo_selezionato_ref.get().descrizione;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (categoria.getSelectedItem() != null && categoria.getSelectedKey() != null) {
            sqlw += "\n  and articoli.categoria = " + dbu.sql(categoria.getSelectedKey());
            sottotitolo += "\\n categoria articoli: " + categoria.getSelectedKey() + " " + categoria.getSelectedItem();
        }

        if (sottocategoria.getSelectedItem() != null) {
            sqlw += "\n  and articoli.sottocategoria = " + dbu.sql(sottocategoria.getSelectedKey());
            sottotitolo += "\\n sottocategoria articoli: " + sottocategoria.getSelectedKey() + " " + sottocategoria.getSelectedItem();
        }

        if (lotto.getText().length() > 0) {
            sottotitolo += "\\n lotto " + lotto_come.getSelectedItem() + " " + lotto.getText();
            sqlw += "\n and m.lotto " + getCond(lotto_come, lotto.getText());
        }
        if (matricola.getText().length() > 0) {
            sottotitolo += "\\n matricola " + matricola_come.getSelectedItem() + " " + matricola.getText();
            sqlw += " and m.matricola " + getCond(matricola_come, matricola.getText());
        }

        if (StringUtils.isNotBlank(cu.s(comCatCliFor.getSelectedItem()))) {
            sqlw += "\n and IFNULL(tdcf.tipo2,IFNULL(tfcf.tipo2,IFNULL(tdacf.tipo2,tfacf.tipo2))) = " + dbu.sql(comCatCliFor.getSelectedKey());
            sottotitolo += "\\n categoria clienti/fornitori: " + comCatCliFor.getSelectedKey() + " " + comCatCliFor.getSelectedItem();
        }

        if (codiceCliFor != null) {
            sqlw += "\n and IFNULL(tdcf.codice,IFNULL(tfcf.codice,IFNULL(tdacf.codice,tfacf.codice))) = " + dbu.sql(codiceCliFor);
            sottotitolo += "\\n cliente/fornitore: " + codiceCliFor + " " + texCliFor.getText();
        }

        sql += sqlf + sqlw;
        sql += " order by m.data, m.id";

        System.out.println("sql = " + sql);

        //test matricole e lotti
        boolean visualizzareLottiMatricole = false;
        String sqltest = "select m.lotto, m.matricola " + sqlf + sqlw + " group by m.lotto, m.matricola";
        try {
            List<Map> listtest = dbu.getListMap(Db.getConn(), sqltest);
            DebugFastUtils.dump(listtest);
            if (listtest.size() > 0) {
                for (Map m : listtest) {
                    if (StringUtils.isNotBlank(cu.s(m.get("lotto")))
                            || StringUtils.isNotBlank(cu.s(m.get("matricola")))) {
                        visualizzareLottiMatricole = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         if (this.cheGiacenze.isSelected() == true) {
         //faccio stampa giacenze
         //debug
         sql += " group by articolo order by articolo";
         System.out.println("print_magazzino_giacenze:sql:" + sql);
         resu = Db.openResultSet(sql);
         if (resu != null) {
         String titolo = "Stampa giacenze magazzino - stampata " + it.tnx.Util.getDateTimeStringITALIAN(java.text.DateFormat.FULL, java.text.DateFormat.SHORT);
         titolo += "\ndal: " + this.forDaData.getEditor().getText() + " al: " + this.forAData.getEditor().getText();
         PrintGiacenze print = new PrintGiacenze(resu, titolo);
         }
         } else {
         //faccio stampa a elenco normale
         //controllo causale
         if (this.comCausale.getSelectedIndex() > 0) {
         sql += " and causale = " + this.comCausale.getSelectedKey();
         }
         sql += " order by m.data, m.id";
         //debug
         System.out.println("print_magazzino:sql:" + sql);
         resu = Db.openResultSet(sql);
         if (resu != null) {
         PrintMovimenti print = new PrintMovimenti(resu);
         }
         }*/
        SwingUtils.mouse_wait(this);
        try {

            InvoicexUtil.aggiornaPrezziNettiUnitari(Db.getConn(), "righ_ddt", "test_ddt");
            InvoicexUtil.aggiornaPrezziNettiUnitari(Db.getConn(), "righ_fatt", "test_fatt");
            InvoicexUtil.aggiornaPrezziNettiUnitari(Db.getConn(), "righ_ddt_acquisto", "test_ddt_acquisto");
            InvoicexUtil.aggiornaPrezziNettiUnitari(Db.getConn(), "righ_fatt_acquisto", "test_fatt_acquisto");

            FastReportBuilder drb = new FastReportBuilder();
            //column.setPattern("$ #.00");

            ar.com.fdvs.dj.domain.Style defStyle = new StyleBuilder(true).setFont(new Font(7, Font._FONT_ARIAL, false, false, false)).build();
            defStyle.setPadding(1);
            defStyle.setPaddingBottom(2);

            ar.com.fdvs.dj.domain.Style defStyleTot = new StyleBuilder(true).setFont(new Font(7, Font._FONT_ARIAL, true, false, false)).build();
            defStyleTot.setPadding(1);
            defStyleTot.setHorizontalAlign(HorizontalAlign.RIGHT);

            ar.com.fdvs.dj.domain.Style defStyleHeader = new StyleBuilder(true).setFont(new Font(7, Font._FONT_ARIAL, true, true, true)).build();
            defStyleHeader.setPadding(1);

            ar.com.fdvs.dj.domain.Style rStyle = (ar.com.fdvs.dj.domain.Style) defStyle.clone();
            rStyle.setHorizontalAlign(HorizontalAlign.RIGHT);

            AbstractColumn colData = ColumnBuilder.getNew()
                    .setColumnProperty("data", String.class.getName())
                    .setTitle("Data")
                    .setWidth(8)
                    .build();

            AbstractColumn colQuantita = ColumnBuilder.getNew()
                    .setColumnProperty("quantita", Double.class.getName())
                    .setTitle("Quantità")
                    .setWidth(8)
                    .setPattern("0.###")
                    .setStyle(rStyle)
                    .build();

            AbstractColumn colPrezzo = ColumnBuilder.getNew()
                    .setColumnProperty("prezzo_netto_unitario", Double.class.getName())
                    .setTitle("Prezzo netto")
                    .setWidth(10)
                    .setPattern("€ #,##0.00###")
                    .setStyle(rStyle)
                    .build();

            AbstractColumn colPrezzoTotale = ColumnBuilder.getNew()
                    .setTitle("Totale")
                    .setWidth(10)
                    .setPattern("€ #,##0.00")
                    .setStyle(rStyle)
                    .setCustomExpression(new CustomExpression() {
                        public Object evaluate(Map fields, Map variables, Map parameters) {
                            double prezzo = cu.d0(fields.get("prezzo_netto_unitario"));
                            double giac = cu.d0(fields.get("quantita"));
                            return prezzo * giac;
                        }

                        public String getClassName() {
                            return Double.class.getName();
                        }
                    })
                    .build();

//            AbstractColumn colValoreTot = ColumnBuilder.getNew()
//                    .setTitle("Valore totale")
//                    .setWidth(20)
//                    .setPattern("€ #,##0.00")
//                    .setStyle(rStyle)
//                    .setCustomExpression(new CustomExpression() {
//                        public Object evaluate(Map fields, Map variables, Map parameters) {
//                            double prezzo = cu.d0(fields.get("prezzo"));
//                            double giac = cu.d0(fields.get("giacenza"));
//                            return prezzo * giac;
//                        }
//
//                        public String getClassName() {
//                            return Double.class.getName();
//                        }
//                    })
//                    .build();
            ar.com.fdvs.dj.domain.Style totalStyle = new ar.com.fdvs.dj.domain.Style();
            totalStyle.setBorderTop(Border.PEN_1_POINT);
            totalStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
            totalStyle.setBackgroundColor(new Color(230, 230, 230));
            totalStyle.setTransparency(Transparency.OPAQUE);

            drb.addColumn(colData);
            drb.addColumn("Origine", "origine", String.class.getName(), 15);
//            drb.addColumn("Causale", "causale", String.class.getName(), 15);
            

            if (visualizzareLottiMatricole) {
                drb.addColumn("Codice articolo", "articolo", String.class.getName(), 10);
                drb.addColumn("Lotto", "lotto", String.class.getName(), 10);
                drb.addColumn("Matricola", "matricola", String.class.getName(), 10);
                drb.addColumn("Articolo", "desc_art", String.class.getName(), 20);
            } else {
                drb.addColumn("Codice articolo", "articolo", String.class.getName(), 20);
                drb.addColumn("Articolo", "desc_art", String.class.getName(), 30);
            }
            
            drb.addColumn(colQuantita);

            if (Magazzino.isMultiDeposito()) {
                drb.addColumn("Deposito", "deposito", String.class.getName(), 15);
            }
            drb.addColumn("Cliente/Fornitore", "clifor_rs", String.class.getName(), 20);

            if (prezzi.isSelected()) {
                drb.addColumn(colPrezzo);
                drb.addColumn(colPrezzoTotale);
            }

//          if (comPrezzi.getSelectedIndex() >= 1) {
//              drb.addColumn(colValore);
//              drb.addColumn(colValoreTot);
//          }
            drb.addGlobalFooterVariable(colQuantita, DJCalculation.SUM, defStyleTot);

            if (prezzi.isSelected()) {
                drb.addGlobalFooterVariable(colPrezzoTotale, DJCalculation.SUM, defStyleTot);
            }

//          if (comPrezzi.getSelectedIndex() >= 1) {
//              drb.addGlobalFooterVariable(colValoreTot, DJCalculation.SUM, totalStyle);
//          }
            if (sottotitolo.length() > 0) {
                sottotitolo = StringUtils.replace(sottotitolo, "\n", " ");
                sottotitolo = StringUtils.replace(sottotitolo, "\"", "\\\"");
                sottotitolo = "Filtri\\n" + sottotitolo;
                System.out.println("sottotitolo = " + sottotitolo);
            }

            drb.setHeaderHeight(5);
            drb.setDetailHeight(10);

            drb.setGrandTotalLegendStyle(defStyleHeader);
            //drb.getGroup(0).setDefaulFooterVariableStyle(defStyleHeader);

            DynamicReport dr = drb.setTitle("Movimenti di magazzino")
                    .setDefaultStyles(null, null, defStyleHeader, defStyle)
                    .setColumnSpace(3)
                    .setSubtitle(sottotitolo)
                    .setPrintBackgroundOnOddRows(true)
                    .setUseFullPageWidth(true)
                    .addAutoText(AutoText.AUTOTEXT_PAGE_X_SLASH_Y, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_RIGHT, 30, 30, defStyle)
                    .addAutoText(AutoText.AUTOTEXT_CREATED_ON, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_LEFT, AutoText.PATTERN_DATE_DATE_TIME, 200, 200, defStyle)
                    .setReportLocale(new Locale("it", "IT"))
                    .build();

            //JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(newbeans);
            ResultSet rs = dbu.tryOpenResultSet(Db.getConn(), sql);
            JRResultSetDataSource ds = new JRResultSetDataSource(rs);
            JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);
            SwingUtils.mouse_def(this);
            if (!pdf) {
                InvoicexUtil.apriStampa(jp);
            } else {
                File dirtmp = new File(System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "tmp" + File.separator);
                System.out.println("dirtmp = " + dirtmp);
                dirtmp.mkdirs();
                File file = File.createTempFile("print_", ".pdf", dirtmp);
                System.out.println("file = " + file);
                JasperExportManager.exportReportToPdfFile(jp, file.getAbsolutePath());
                Util.start2(file.getAbsolutePath());
            }
            dbu.close(rs);
        } catch (Exception e) {
            SwingUtils.mouse_def(this);
            e.printStackTrace();
            SwingUtils.showExceptionMessage(this, e);
        }

    }

    private String getCond(JComboBox come, String text) {
        if (come.getSelectedItem().equals("Uguale a")) {
            return " = '" + Db.aa(text) + "'";
        } else if (come.getSelectedItem().equals("Contiene")) {
            return " like '%" + Db.aa(text) + "%'";
        } else if (come.getSelectedItem().equals("Inizia con")) {
            return " like '" + Db.aa(text) + "%'";
        } else if (come.getSelectedItem().equals("Finisce con")) {
            return " like '%" + Db.aa(text) + "'";
        }
        return null;
    }
}
