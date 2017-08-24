/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * JDialogSceltaQuantita.java
 *
 * Created on 13-set-2010, 17.47.18
 */
package it.tnx.invoicex.gui;

import gestioneFatture.main;
import it.tnx.Db;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.KeyValuePair;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.commons.table.EditorUtils;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.Magazzino;
import it.tnx.invoicex.data.Giacenza;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author mceccarelli
 */
public class JDialogSceltaQuantita extends javax.swing.JDialog {

    String tipodoc_da = null;
    String tipodoc_a = null;
    Integer[] ids = null;
    public boolean ok;
    public boolean loading = false;
    public boolean check_giacenze = false;

    KeyValuePair kvd1 = new KeyValuePair(Magazzino.Depositi.TUTTI_DETTAGLIO, "<tutti i depositi>");

    /**
     * Creates new form JDialogSceltaQuantita
     */
    public JDialogSceltaQuantita(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

//if (worker != null) {
//    worker.publish("init qta");
//}
        if (main.fileIni.getValueBoolean("pref", "tutterigheconfermate", false)) {
            tutterigheconfermate.setSelected(true);
        }

//        tab.setRowHeight(25);
        tab.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        MultiLineHeaderRenderer renderer = new MultiLineHeaderRenderer();
        Enumeration enum1 = tab.getColumnModel().getColumns();
        while (enum1.hasMoreElements()) {
            ((TableColumn) enum1.nextElement()).setHeaderRenderer(renderer);
        }

        final JTextField tf = new JTextField();
        tf.setFont(tab.getFont());
        tf.setBorder(BorderFactory.createEmptyBorder());
        tf.setHorizontalAlignment(JTextField.RIGHT);
        tf.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                tf.selectAll();
            }
        });
        DefaultCellEditor editor = new DefaultCellEditor(tf) {
            boolean init = false;
            JTable table = null;
            int row = -1;
            int col = -1;

            @Override
            public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
                JTextField tf = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
                if (!init) {
                    init = true;
                    this.table = table;
                    tf.getDocument().addDocumentListener(new DocumentListener() {
                        public void insertUpdate(DocumentEvent e) {
                            textchange(table.getSelectedRow());
                        }

                        public void removeUpdate(DocumentEvent e) {
                            textchange(table.getSelectedRow());
                        }

                        public void changedUpdate(DocumentEvent e) {
                            textchange(table.getSelectedRow());
                        }
                    });
                }
                this.row = row;
                this.col = column;
                tf.setText(FormatUtils.formatPerc(CastUtils.toDouble0(value)));
//                tf.selectAll();

                //se articolo con gestione lotti non rendere modificabile la quantità da evadre, o tutto o niente.
                int col_lotti = table.getColumn("gestione_lotti").getModelIndex();
                col_lotti = table.convertColumnIndexToView(col_lotti);
//                if (CastUtils.toString(table.getValueAt(row, col_lotti)).equalsIgnoreCase("S")) {
//                    tf.setEditable(false);
//                    tf.setToolTipText("Per articoli con gestione lotti non è possibile l'evasione parziale");
//                    if (isVisible()) {
//                        Point p = MouseInfo.getPointerInfo().getLocation();
//                        SwingUtils.showFlashMessage2("Per articoli con gestione lotti non è possibile l'evasione parziale", 3, p, Color.BLACK.YELLOW);
//                    }
//                } else {
                tf.setEditable(true);
                tf.setToolTipText("Indicare la quantità da confermare per la conversione del documento");
//                }

                return tf;
            }

            private void textchange(int row) {
                //if (1==1) return;
                if (loading) {
                    return;
                }
                if (row < 0) {
                    return;
                }
                if (row >= table.getRowCount()) {
                    return;
                }
                if (table != null) {
                    double qtaconf = CastUtils.toDouble0(tf.getText());
                    double qta = CastUtils.toDouble0(table.getValueAt(row, table.getColumn("quantità").getModelIndex()));
                    double prezzo = CastUtils.toDouble0(table.getValueAt(row, table.getColumn("prezzo").getModelIndex()));
                    loading = true;
                    if (qtaconf > 0) {
                        table.setValueAt(true, row, table.getColumn("riga confermata").getModelIndex());
                    } else if ((prezzo != 0 || qta > 0)) {
                        table.setValueAt(false, row, table.getColumn("riga confermata").getModelIndex());
                    }
                    loading = false;
                }
            }

            @Override
            public Object getCellEditorValue() {
                return CastUtils.toDouble(super.getCellEditorValue());
            }

            @Override
            public int getClickCountToStart() {
                return 1;
            }
        };
        tab.getColumn("quantità confermata").setCellEditor(editor);

//        TableColumn col = tab.getColumn("quantità confermata");
//        col.setCellEditor(new SpinnerEditor());
        non_evadere_ordine.setVisible(false);

    }

    public void load(String da_tipo_doc, String a_tipo_doc, Integer[] ids, Boolean dest_fatt_proforma) {
        String sql = "";
        this.tipodoc_da = da_tipo_doc;
        this.tipodoc_a = a_tipo_doc;
        this.ids = ids;

        if (dest_fatt_proforma == null) {
            dest_fatt_proforma = false;
        }
        if (main.getPersonalContain("consegna_e_scarico") && da_tipo_doc.equals(Db.TIPO_DOCUMENTO_ORDINE) && a_tipo_doc.equals(Db.TIPO_DOCUMENTO_FATTURA) && dest_fatt_proforma) {
            non_evadere_ordine.setVisible(true);
            non_evadere_ordine.setSelected(true);
        }

        if (da_tipo_doc.equals(Db.TIPO_DOCUMENTO_DDT) || da_tipo_doc.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
            cheMovimentaDaDeposito.setVisible(false);
        }

        setIntestazione();

        try {
            if (Magazzino.isMultiDeposito()) {
                SwingUtils.initJComboFromDb(deposito, Db.getConn(), "select id, nome from depositi order by nome", "id", "nome", kvd1);
                if (!main.fileIni.existKey("pref", "depositoSceltaLottiDepositoStandard")) {
                    //allora sceglo il deposito dal documento di origine, altrimenti scelgo tutti i depositi
                    Integer id = ids[0];
                    sql = "select deposito";
                    sql += " from " + Db.getNomeTabT(da_tipo_doc) + " t ";
                    sql += " where id = " + id;

                    Integer codice_deposito = cu.i(dbu.getObject(Db.getConn(), sql));
                    if (codice_deposito != null) {
                        SwingUtils.findJComboKV(deposito, codice_deposito);
                        if (main.fileIni.getValueBoolean("pref", "cheMovimentaDaDeposito", true)) {
                            cheMovimentaDaDeposito.setSelected(true);
                        }
                    }
                }
            } else {
                giacenze_da.setVisible(false);
                deposito.setVisible(false);
                cheMovimentaDaDeposito.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!main.getPersonalContain("snj") || !da_tipo_doc.contains(Db.TIPO_DOCUMENTO_ORDINE)) {
            tab.removeColumn(tab.getColumn("emissione fattura"));
            tab.removeColumn(tab.getColumn("percentuale"));
            tab.removeColumn(tab.getColumn("termini_pagamento"));
        }

        DefaultTableModel m = getModel();
        int riga = 0;
        for (int i = 0; i < ids.length; i++) {
            int id = ids[i];
            try {
                sql = "select t.id as tid, t.serie, t.numero, t.data, t.anno, r.id as rid, r.riga, r.codice_articolo, r.descrizione, r.quantita, r.quantita_evasa, r." + getCampoin(a_tipo_doc) + " as dest_id, r." + getCampoin(a_tipo_doc) + "_riga as dest_id_riga, r.prezzo ";
                if (main.getPersonalContain("snj") && da_tipo_doc.equals(Db.TIPO_DOCUMENTO_ORDINE)) {
                    sql += ", em.descrizione as emissione_fattura, r.percentuale, r.termini_pagamento ";
                }
                sql += ", a.gestione_lotti ";
                sql += " from " + Db.getNomeTabT(da_tipo_doc) + " t left join " + Db.getNomeTabR(da_tipo_doc) + " r on t.id = r.id_padre";
                sql += " left join articoli a ON r.codice_articolo = a.codice ";
                if (main.getPersonalContain("snj") && da_tipo_doc.equals(Db.TIPO_DOCUMENTO_ORDINE)) {
                    sql += " left join tipi_emissione_fattura em ON r.emissione_fattura = em.id ";
                }
                sql += " where t.id = " + id + " order by r.riga";
                System.out.println("sql = " + sql);
                List<Map> righe = DbUtils.getListMap(Db.conn, sql);
//---------------------                
                if (righe != null && righe.size() > 0) {
                    Map map = righe.get(0);
                    Object[] row = new Object[m.getColumnCount()];
                    row[4] = "--- " + Db.getDescTipoDocNomeFile(da_tipo_doc) + " " + map.get("serie") + map.get("numero") + " del " + DateUtils.formatDateIta(cu.toDate(map.get("data"))) + " ---";
                    getModel().addRow(row);
                    riga++;
                }
//---------------------                
                for (Map map : righe) {
                    Object[] row = new Object[m.getColumnCount()];
                    row[0] = Db.getDescTipoDocBreve(da_tipo_doc) + " " + map.get("id") + " " + map.get("serie") + "/" + map.get("numero") + "/" + map.get("anno");
                    row[1] = Db.getDescTipoDocBreve(a_tipo_doc);
                    row[2] = map.get("riga");
                    row[3] = map.get("codice_articolo");
                    row[4] = map.get("descrizione");
                    row[5] = map.get("quantita");
                    row[6] = 0;
                    row[7] = true;
                    row[8] = map.get("tid");
                    //prov_id_riga
                    row[9] = map.get("rid");
                    row[10] = map.get("dest_id");
                    row[11] = map.get("dest_id_riga");
                    row[12] = cu.d0(map.get("quantita_evasa")) < 0 ? 0 : cu.d0(map.get("quantita_evasa"));
                    row[13] = map.get("prezzo");
                    if (main.getPersonalContain("snj") && da_tipo_doc.contains(Db.TIPO_DOCUMENTO_ORDINE)) {
                        row[14] = map.get("emissione_fattura");
                        row[15] = map.get("percentuale");
                        row[16] = map.get("termini_pagamento");
                    }
                    row[17] = map.get("gestione_lotti");
                    row[18] = getGiacenza(cu.s(map.get("codice_articolo")), riga);
                    getModel().addRow(row);
                    riga++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tab.getColumn("descrizione").setCellRenderer(new LineBreakPanelRenderer(tab.getFont()));

        try {
            tab.getColumn("quantità").setCellRenderer(InvoicexUtil.getNumber0_5Renderer());
            tab.getColumn("quantità confermata").setCellRenderer(InvoicexUtil.getNumber0_5Renderer());
            DecimalFormat df1 = new DecimalFormat("0.#####");
            tab.getColumn("quantità confermata").setCellEditor(new EditorUtils.NumberEditor(new JTextField(), df1) {
                public Object getCellEditorValue() {
                    String text = ((JTextField) editorComponent).getText();
                    Double qta_evasa = CastUtils.toDouble0All(text);
                    return qta_evasa;
                }
            });
            tab.getColumn("giacenza").setCellRenderer(InvoicexUtil.getNumber0_5Renderer());
            tab.getColumn("riga confermata").setCellRenderer(new MyBooleanRenderer());
        } catch (Exception e) {
            e.printStackTrace();
        }

        seltuttoActionPerformed(null);

        tab.requestFocus();
        tab.editCellAt(0, tab.getColumn("quantità confermata").getModelIndex());
        tab.changeSelection(0, tab.getColumn("quantità confermata").getModelIndex(), false, false);

        if (deposito.getSelectedIndex() == 0) {
            cheMovimentaDaDeposito.setSelected(false);
            cheMovimentaDaDeposito.setEnabled(false);
        } else {
            cheMovimentaDaDeposito.setEnabled(true);
        }

    }

    public static class MyBooleanRenderer extends JCheckBox implements TableCellRenderer, UIResource {

        private static final long serialVersionUID = 1L;
        private final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        JLabel lab = new JLabel("");

        MyBooleanRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);

            lab.setOpaque(true);
            lab.setBackground(new Color(250, 250, 250));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (table.getValueAt(row, 0) == null) {
                return lab;
            } else {
                if (isSelected) {
                    setForeground(table.getSelectionForeground());
                    super.setBackground(table.getSelectionBackground());
                } else {
                    setForeground(table.getForeground());
                    setBackground(table.getBackground());
                }
                setSelected(value != null && ((Boolean) value).booleanValue());
                if (hasFocus) {
                    setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                } else {
                    setBorder(noFocusBorder);
                }
                return this;
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tab = getMyJTable();
        conferma = new javax.swing.JButton();
        int1 = new javax.swing.JLabel();
        seltutto = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        seltutto1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        tutterigheconfermate = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        selevadibile = new javax.swing.JButton();
        non_evadere_ordine = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        deposito = new javax.swing.JComboBox();
        giacenze_da = new javax.swing.JLabel();
        cheMovimentaDaDeposito = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tab.setFont(tab.getFont().deriveFont(tab.getFont().getSize()+2f));
        tab.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "provenienza", "destinazione", "riga", "articolo", "descrizione", "quantità", "quantità confermata", "riga confermata", "prov_id", "prov_id_riga", "dest_id", "dest_id_riga", "quantità già confermata", "prezzo", "emissione fattura", "percentuale", "termini_pagamento", "gestione_lotti", "giacenza"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Object.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tab);
        if (tab.getColumnModel().getColumnCount() > 0) {
            tab.getColumnModel().getColumn(0).setMinWidth(0);
            tab.getColumnModel().getColumn(0).setPreferredWidth(0);
            tab.getColumnModel().getColumn(0).setMaxWidth(0);
            tab.getColumnModel().getColumn(1).setMinWidth(0);
            tab.getColumnModel().getColumn(1).setPreferredWidth(0);
            tab.getColumnModel().getColumn(1).setMaxWidth(0);
            tab.getColumnModel().getColumn(2).setPreferredWidth(20);
            tab.getColumnModel().getColumn(3).setPreferredWidth(50);
            tab.getColumnModel().getColumn(4).setPreferredWidth(200);
            tab.getColumnModel().getColumn(5).setPreferredWidth(30);
            tab.getColumnModel().getColumn(6).setPreferredWidth(30);
            tab.getColumnModel().getColumn(7).setPreferredWidth(30);
            tab.getColumnModel().getColumn(8).setMinWidth(0);
            tab.getColumnModel().getColumn(8).setPreferredWidth(0);
            tab.getColumnModel().getColumn(8).setMaxWidth(0);
            tab.getColumnModel().getColumn(9).setMinWidth(0);
            tab.getColumnModel().getColumn(9).setPreferredWidth(0);
            tab.getColumnModel().getColumn(9).setMaxWidth(0);
            tab.getColumnModel().getColumn(10).setMinWidth(0);
            tab.getColumnModel().getColumn(10).setPreferredWidth(0);
            tab.getColumnModel().getColumn(10).setMaxWidth(0);
            tab.getColumnModel().getColumn(11).setMinWidth(0);
            tab.getColumnModel().getColumn(11).setPreferredWidth(0);
            tab.getColumnModel().getColumn(11).setMaxWidth(0);
            tab.getColumnModel().getColumn(12).setPreferredWidth(30);
            tab.getColumnModel().getColumn(13).setMinWidth(0);
            tab.getColumnModel().getColumn(13).setPreferredWidth(0);
            tab.getColumnModel().getColumn(13).setMaxWidth(0);
            tab.getColumnModel().getColumn(17).setMinWidth(0);
            tab.getColumnModel().getColumn(17).setPreferredWidth(0);
            tab.getColumnModel().getColumn(17).setMaxWidth(0);
            tab.getColumnModel().getColumn(18).setPreferredWidth(20);
        }

        conferma.setText("Conferma");
        conferma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confermaActionPerformed(evt);
            }
        });

        int1.setText("...");

        seltutto.setText("Seleziona solo da confermare");
        seltutto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seltuttoActionPerformed(evt);
            }
        });

        jButton2.setText("Annulla tutto");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Annulla");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        seltutto1.setText("Seleziona tutto");
        seltutto1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seltutto1ActionPerformed(evt);
            }
        });

        jLabel1.setText("<html>Da questa schermata devi scegliere sia le quantità da confermare sia le righe stesse (colonna 'riga confermata'),<br />puoi scegliere ad esempio di impostare la quantità a 0 ma di esportare comunque la riga.<br>Le righe 'verdi' sono quelle che verranno portate nel nuovo documento</html>");

        tutterigheconfermate.setText("Preferisco come standard avere tutte le righe confermate ");
        tutterigheconfermate.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        tutterigheconfermate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tutterigheconfermateActionPerformed(evt);
            }
        });

        jSeparator1.setMinimumSize(new java.awt.Dimension(50, 4));
        jSeparator1.setPreferredSize(new java.awt.Dimension(50, 4));
        jSeparator1.setRequestFocusEnabled(false);

        selevadibile.setText("Seleziona solo evadibile");
        selevadibile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selevadibileActionPerformed(evt);
            }
        });

        non_evadere_ordine.setText("Non evadere l'ordine");
        non_evadere_ordine.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        deposito.setToolTipText("Da qui puoi scegliere da quale deposito controllare le giacenze ma non cambia il deposito da movimentare");
        deposito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                depositoActionPerformed(evt);
            }
        });

        giacenze_da.setText("Giacenze da");
        giacenze_da.setToolTipText("Da qui puoi scegliere da quale deposito controllare le giacenze ma non cambia il deposito da movimentare");

        cheMovimentaDaDeposito.setText("movimenta questo deposito");
        cheMovimentaDaDeposito.setToolTipText("Verrà utilizzato come deposito da movimentare");
        cheMovimentaDaDeposito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cheMovimentaDaDepositoActionPerformed(evt);
            }
        });

        jLabel3.setText(" ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(tutterigheconfermate)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(non_evadere_ordine)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel2))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 41, Short.MAX_VALUE)
                        .add(jButton3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(conferma))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(int1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jButton2)
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(giacenze_da)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cheMovimentaDaDeposito)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel3))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(seltutto)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(selevadibile)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(seltutto1)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(int1)
                    .add(seltutto1)
                    .add(seltutto)
                    .add(jButton2)
                    .add(selevadibile))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(giacenze_da)
                    .add(cheMovimentaDaDeposito)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(conferma)
                    .add(jButton3)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tutterigheconfermate)
                    .add(non_evadere_ordine)
                    .add(jLabel2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        SwingUtils.mouse_wait(this);
        for (int i = 0; i < tab.getRowCount(); i++) {
            if (tab.getValueAt(i, 0) != null) {
                tab.setValueAt(0, i, tab.getColumn("quantità confermata").getModelIndex());
            }
        }
        SwingUtils.mouse_def(this);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void seltuttoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seltuttoActionPerformed
//        loading = true;
        SwingUtils.mouse_wait(this);
        for (int i = 0; i < tab.getRowCount(); i++) {
            if (tab.getValueAt(i, 0) == null) {
                continue;
            }
            double qta = CastUtils.toDouble0(tab.getValueAt(i, tab.getColumn("quantità").getModelIndex()));
            double qta_evasa = CastUtils.toDouble0(tab.getValueAt(i, tab.getColumn("quantità già confermata").getModelIndex()));
            double qta_da_evadere = qta - qta_evasa;
            if (qta_da_evadere < 0) {
                qta_da_evadere = 0;
            }
            tab.setValueAt(qta_da_evadere, i, tab.getColumn("quantità confermata").getModelIndex());
            if (!tutterigheconfermate.isSelected()) {
                if (qta_evasa > 0 && qta_da_evadere <= 0) {
                    tab.setValueAt(false, i, tab.getColumn("riga confermata").getModelIndex());
                } else if (qta_da_evadere > 0) {
                    tab.setValueAt(true, i, tab.getColumn("riga confermata").getModelIndex());
                } else {
                    tab.setValueAt(false, i, tab.getColumn("riga confermata").getModelIndex());
                }
            } else {
                tab.setValueAt(true, i, tab.getColumn("riga confermata").getModelIndex());
            }
        }
        SwingUtils.mouse_def(this);
//        loading = false;
    }//GEN-LAST:event_seltuttoActionPerformed

    private void confermaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confermaActionPerformed
        ok = true;

        //avvertimeno giacenza
        if (check_giacenze) {
            boolean giac_ok = true;
            for (int i = 0; i < tab.getRowCount(); i++) {
                double qta_evasa = CastUtils.toDouble0(tab.getValueAt(i, tab.getColumn("quantità confermata").getModelIndex()));
                double giacenza = CastUtils.toDouble0(tab.getValueAt(i, tab.getColumnModel().getColumnIndex("giacenza")));
                if (qta_evasa > giacenza) {
                    giac_ok = false;
                    break;
                }
            }
            if (!giac_ok) {
                if (!SwingUtils.showYesNoMessage(this, "Ci sono uno o più articoli con giacenza insufficiente per l'evasione, sicuro di continuare ?", "Attenzione, giacenza insufficiente")) {
                    return;
                }
            }
        }

        if (main.getPersonalContain("snj")) {
            String terminiPagamento = "";
            for (int i = 0; i < tab.getRowCount(); i++) {
                double qta_evasa = CastUtils.toDouble0(tab.getValueAt(i, tab.getColumn("quantità confermata").getModelIndex()));
                boolean convertire = CastUtils.toBoolean(tab.getValueAt(i, tab.getColumn("riga confermata").getModelIndex()));
                if (convertire) {
                    try {
                        String newTerminiPagamento = CastUtils.toString(tab.getValueAt(i, tab.getColumn("termini_pagamento").getModelIndex()));
                        if (terminiPagamento.equals("")) {
                            terminiPagamento = newTerminiPagamento;
                        } else if (!terminiPagamento.equals(newTerminiPagamento)) {
                            SwingUtils.showErrorMessage(this, "Non è possibile convertire righe con termini di pagamento diversi", "Errore Termini di Pagamento", true);
                            return;
                        }
                    } catch (java.lang.IllegalArgumentException e) {
                        //ignoro il fatto che non eissta la colonna termini_pagamento
                    }
                }
            }
        }

        setVisible(false);
    }//GEN-LAST:event_confermaActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        ok = false;
        setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void seltutto1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seltutto1ActionPerformed
        SwingUtils.mouse_wait(this);
        for (int i = 0; i < tab.getRowCount(); i++) {
            if (tab.getValueAt(i, 0) == null) {
                continue;
            }
            double qta = CastUtils.toDouble0(tab.getValueAt(i, tab.getColumn("quantità").getModelIndex()));
            double qta_evasa = CastUtils.toDouble0(tab.getValueAt(i, tab.getColumn("quantità già confermata").getModelIndex()));
            double qta_da_evadere = qta;
            tab.setValueAt(qta_da_evadere, i, tab.getColumn("quantità confermata").getModelIndex());
            tab.setValueAt(true, i, tab.getColumn("riga confermata").getModelIndex());
        }
        SwingUtils.mouse_def(this);
    }//GEN-LAST:event_seltutto1ActionPerformed

    private void tabMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabMouseClicked
        if (evt.getClickCount() >= 2) {
            if (tab.getValueAt(tab.getSelectedRow(), 0) != null) {
                changeRigaConfermata();
            }
        }
    }//GEN-LAST:event_tabMouseClicked

    private void tutterigheconfermateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tutterigheconfermateActionPerformed
        main.fileIni.setValue("pref", "tutterigheconfermate", tutterigheconfermate.isSelected());
    }//GEN-LAST:event_tutterigheconfermateActionPerformed

    private void selevadibileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selevadibileActionPerformed
        SwingUtils.mouse_wait(this);
        for (int i = 0; i < tab.getRowCount(); i++) {
            if (tab.getValueAt(i, 0) == null) {
                continue;
            }
            double qta = CastUtils.toDouble0(tab.getValueAt(i, tab.getColumn("quantità").getModelIndex()));
            double qta_evasa = CastUtils.toDouble0(tab.getValueAt(i, tab.getColumn("quantità già confermata").getModelIndex()));
            double giacenza = CastUtils.toDouble0(tab.getValueAt(i, tab.getColumnModel().getColumnIndex("giacenza")));
            double qta_da_evadere = qta - qta_evasa;
            if (qta_da_evadere < 0) {
                qta_da_evadere = 0;
            }
            if (qta_da_evadere > giacenza) {
                qta_da_evadere = giacenza;
            }
            tab.setValueAt(qta_da_evadere, i, tab.getColumn("quantità confermata").getModelIndex());
            if (!tutterigheconfermate.isSelected()) {
                if (qta_evasa > 0 && qta_da_evadere <= 0) {
                    tab.setValueAt(false, i, tab.getColumn("riga confermata").getModelIndex());
                } else if (qta_da_evadere > 0) {
                    tab.setValueAt(true, i, tab.getColumn("riga confermata").getModelIndex());
                } else {
                    tab.setValueAt(false, i, tab.getColumn("riga confermata").getModelIndex());
                }
            } else {
                tab.setValueAt(true, i, tab.getColumn("riga confermata").getModelIndex());
            }
        }
        SwingUtils.mouse_def(this);
    }//GEN-LAST:event_selevadibileActionPerformed

    private void depositoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_depositoActionPerformed
        //aggiorno giacenze in base al deposito
        loading = true;
        for (int i = 0; i < tab.getRowCount(); i++) {
            String artriga = cu.s(tab.getValueAt(i, tab.getColumn("articolo").getModelIndex()));
            if (StringUtils.isNotBlank(artriga)) {
                double giac = getGiacenza(artriga, i);
                tab.setValueAt(giac, i, tab.getColumnModel().getColumnIndex("giacenza"));
            }
        }
        if (deposito.getSelectedIndex() == 0) {
            cheMovimentaDaDeposito.setSelected(false);
            cheMovimentaDaDeposito.setEnabled(false);
        } else {
            cheMovimentaDaDeposito.setEnabled(true);
        }
        loading = false;
    }//GEN-LAST:event_depositoActionPerformed

    private void cheMovimentaDaDepositoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cheMovimentaDaDepositoActionPerformed
        main.fileIni.setValue("pref", "cheMovimentaDaDeposito", cheMovimentaDaDeposito.isSelected());
    }//GEN-LAST:event_cheMovimentaDaDepositoActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                JDialogSceltaQuantita dialog = new JDialogSceltaQuantita(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBox cheMovimentaDaDeposito;
    private javax.swing.JButton conferma;
    public javax.swing.JComboBox deposito;
    private javax.swing.JLabel giacenze_da;
    public javax.swing.JLabel int1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    public javax.swing.JCheckBox non_evadere_ordine;
    public javax.swing.JButton selevadibile;
    private javax.swing.JButton seltutto;
    private javax.swing.JButton seltutto1;
    public javax.swing.JTable tab;
    private javax.swing.JCheckBox tutterigheconfermate;
    // End of variables declaration//GEN-END:variables

    public DefaultTableModel getModel() {
        return (DefaultTableModel) tab.getModel();
    }

    public JTable getTable() {
        return tab;
    }

    private void setIntestazione() {
        int1.setText("Selezionare le quantità per il passaggio da " + Db.getDescTipoDoc(tipodoc_da) + " a " + Db.getDescTipoDoc(tipodoc_a));

        if (tipodoc_da == Db.TIPO_DOCUMENTO_ORDINE && (tipodoc_a == Db.TIPO_DOCUMENTO_DDT || tipodoc_a == Db.TIPO_DOCUMENTO_FATTURA)) {
            check_giacenze = true;
        } else {
            selevadibile.setVisible(false);
        }
    }

    private String getCampoin(String tipoDoc) {
        HashMap val = new HashMap();
        val.put(Db.TIPO_DOCUMENTO_DDT, "in_ddt");
        val.put(Db.TIPO_DOCUMENTO_DDT_ACQUISTO, "in_ddt");
        val.put(Db.TIPO_DOCUMENTO_FATTURA, "in_fatt");
        val.put(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, "in_fatt");
        val.put(Db.TIPO_DOCUMENTO_SCONTRINO, "in_fatt");
        try {
            return (String) val.get(tipoDoc);
        } catch (Exception e) {
            e.printStackTrace();
            return tipoDoc;
        }
    }

    private void changeRigaConfermata() {
        boolean val = CastUtils.toBoolean(tab.getValueAt(tab.getSelectedRow(), tab.getColumn("riga confermata").getModelIndex()));
        if (val) {
            tab.setValueAt(false, tab.getSelectedRow(), tab.getColumn("riga confermata").getModelIndex());
        } else {
            tab.setValueAt(true, tab.getSelectedRow(), tab.getColumn("riga confermata").getModelIndex());
        }
    }

    private void ricalcolareGiacenze(String codart) {
        if (StringUtils.isNotBlank(codart)) {
            for (int i = 1; i < tab.getRowCount(); i++) {
                String artriga = cu.s(tab.getValueAt(i, tab.getColumn("articolo").getModelIndex()));
                if (tab.getValueAt(i, 0) != null && artriga.equals(codart)) {
                    double giac = getGiacenza(artriga, i);
                    tab.setValueAt(giac, i, tab.getColumnModel().getColumnIndex("giacenza"));
                }
            }
        }
    }

    Map<Object, Map<String, Giacenza>> cache_giac = new HashMap(); // Map<Deposito, Map<cod articolo, Giacenza>>

    private double getGiacenza(String articolo, int riga) {
        //calcolo gaicenza di tutti gli articoli
        Object depkey = null;
        ArrayList<Giacenza> list = null;
        Map<String, Giacenza> giacmap = null;
        try {
            if (deposito.isVisible()) {
                depkey = deposito.getSelectedItem();
            }
            if (cache_giac.get(depkey) == null) {
                if (!deposito.isVisible() || deposito.getSelectedItem() == kvd1) {
                    list = main.magazzino.getGiacenza(false, null, null, null, false, true, true, null);
                } else {
                    list = main.magazzino.getGiacenza(false, null, null, null, false, true, true, ((KeyValuePair) deposito.getSelectedItem()).getKey());
                }
                //converto a map
                giacmap = new HashMap<String, Giacenza>();
                for (Giacenza g : list) {
                    giacmap.put(g.getCodice_articolo(), g);
                }
                cache_giac.put(depkey, giacmap);
                list = null;
            }

            giacmap = cache_giac.get(depkey);
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showExceptionMessage(this, e);
        }

        double giacenza = 0;
        if (giacmap != null && giacmap.size() > 0) {
            try {
                Giacenza g = (Giacenza) giacmap.get(articolo);
                if (g != null) {
                    giacenza = g.getGiacenza();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //vado a cerca a ritroso nella tabella se ci sono altre righe con stesso articolo e sottraggo (se riga confermata) la quantta confermata
        if (riga > 0) {
            for (int i = riga - 1; i >= 0; i--) {
                String art_riga = cu.s(tab.getValueAt(i, tab.getColumn("articolo").getModelIndex()));
                Double qta_conf = cu.d(tab.getValueAt(i, tab.getColumn("quantità confermata").getModelIndex()));
                Boolean riga_conf = cu.toBoolean(tab.getValueAt(i, tab.getColumn("riga confermata").getModelIndex()));
                if (art_riga != null && art_riga.equalsIgnoreCase(articolo)) {
                    if (riga_conf != null && riga_conf == true) {
                        if (qta_conf != null) {
                            giacenza -= qta_conf;
                        }
                    }
                }
            }
        }

        return giacenza;
    }

    static class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {

        final JSpinner spinner = new JSpinner();

        public SpinnerEditor() {
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                int row, int column) {
            try {
                if (value instanceof Double || value instanceof BigDecimal) {
                    double valMax = CastUtils.toDouble0(table.getValueAt(row, column - 1));
                    double val = CastUtils.toDouble0(value);
                    spinner.setModel(new SpinnerNumberModel(val, 0d, valMax, 0.1d));
                } else {
                    int valMax = CastUtils.toInteger0(table.getValueAt(row, column - 1));
                    int val = CastUtils.toInteger0(value);
                    spinner.setModel(new SpinnerNumberModel(val, 0, valMax, 1));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            spinner.setValue(value);
            return spinner;
        }

        public boolean isCellEditable(EventObject evt) {
            if (evt instanceof MouseEvent) {
                return ((MouseEvent) evt).getClickCount() >= 2;
            }
            return true;
        }

        public Object getCellEditorValue() {
            return spinner.getValue();
        }
    }

    public JTable getMyJTable() {
        return new JTable() {
            Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

            public boolean isCellEditable(int row, int column) {
                boolean ret = super.isCellEditable(row, column);
                if (getValueAt(row, 0) == null) {
                    return false;
                }
                return ret;
            }

            public void changeSelection(final int row, final int column, boolean toggle, boolean extend) {
                super.changeSelection(row, column, toggle, extend);
                System.out.println("changeSel:" + row + " " + column);
                if (editCellAt(row, column)) {
                    Component comp = getEditorComponent();
                    comp.requestFocusInWindow();
                    if (comp instanceof JTextField) {
                        JTextField textComp = (JTextField) comp;
                        textComp.selectAll();
                    }
                }
            }

            @Override
            public void setValueAt(Object val, int row, int column) {
                super.setValueAt(val, row, column);

                if (!loading && getValueAt(row, 0) != null) {
                    if (column == getColumn("quantità confermata").getModelIndex()) {
                        Double dval = CastUtils.toDouble0(val);
                        double qtaconf = CastUtils.toDouble0(getValueAt(row, getColumn("quantità confermata").getModelIndex()));
                        double qta = CastUtils.toDouble0(getValueAt(row, getColumn("quantità").getModelIndex()));
                        double prezzo = CastUtils.toDouble0(getValueAt(row, getColumn("prezzo").getModelIndex()));
                        String codart = CastUtils.toString(getValueAt(row, getColumn("articolo").getModelIndex()));
                        String descr = CastUtils.toString(getValueAt(row, getColumn("descrizione").getModelIndex()));
                        loading = true;
                        if (qtaconf > 0) {
                            setValueAt(true, row, getColumn("riga confermata").getModelIndex());
                        } else //                        if ((prezzo != 0 || qta > 0) && !main.getPersonalContain("proskin")) {
                         if ((prezzo != 0 || qta > 0)) {
                                setValueAt(false, row, getColumn("riga confermata").getModelIndex());
                            }
                        loading = false;
                    } else if (column == getColumn("riga confermata").getModelIndex()) {
                        Boolean confermata = CastUtils.toBoolean(getValueAt(row, getColumn("riga confermata").getModelIndex()));
                        loading = true;
                        if (!confermata) {
                            setValueAt(0, row, getColumn("quantità confermata").getModelIndex());
                        }
                        loading = false;
                    }
                    //ricalcolare le giaceze
                    if (column == getColumn("quantità confermata").getModelIndex() || column == getColumn("riga confermata").getModelIndex()) {
                        ricalcolareGiacenze(cu.s(getValueAt(row, getColumn("articolo").getModelIndex())));
                    }
                }
            }

            @Override
            public Component prepareEditor(TableCellEditor editor, int row, int column) {
                Component comp = super.prepareEditor(editor, row, column);
                Color back = Color.WHITE;
                if (CastUtils.toBoolean(getValueAt(row, getColumn("riga confermata").getModelIndex()))) {
                    if (isCellSelected(row, column)) {
                        if (column == getColumn("quantità confermata").getModelIndex()) {
                            back = new Color(240, 255, 240);
                        } else {
                            back = new Color(200, 255, 200);
                        }
                    } else if (column == getColumn("quantità confermata").getModelIndex()) {
                        back = new Color(240, 255, 240);
                    } else {
                        back = new Color(220, 255, 220);
                    }
                } else if (isCellSelected(row, column)) {
                    back = new Color(245, 245, 245);
                }

                //evidenzio giacenza insufficiente
                if (column == getColumnModel().getColumnIndex("quantità confermata")
                        || column == getColumnModel().getColumnIndex("giacenza")) {
                    double qta_confermata = cu.d0(getValueAt(row, getColumnModel().getColumnIndex("quantità confermata")));
                    double giacenza = cu.d0(getValueAt(row, getColumnModel().getColumnIndex("giacenza")));
                    if (qta_confermata > giacenza) {
                        back = new Color(245, 235, 225);
                    }
                }

                if (column == getColumn("quantità confermata").getModelIndex()) {
                    comp.setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    comp.setFont(getFont().deriveFont(Font.PLAIN));
                }
                comp.setBackground(back);
                comp.setForeground(Color.BLACK);
                return comp;
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                Color back = Color.WHITE;
                if (CastUtils.toBoolean(getValueAt(row, getColumn("riga confermata").getModelIndex()))) {
                    if (isCellSelected(row, column)) {
                        if (column == getColumn("quantità confermata").getModelIndex()) {
                            back = new Color(240, 255, 240);
                        } else {
                            back = new Color(200, 255, 200);
                        }
                    } else if (column == getColumn("quantità confermata").getModelIndex()) {
                        back = new Color(240, 255, 240);
                    } else {
                        back = new Color(220, 255, 220);
                    }
                } else if (isCellSelected(row, column)) {
                    back = new Color(245, 245, 245);
                }

                //evidenzio giacenza insufficiente
                if (column == getColumnModel().getColumnIndex("quantità confermata")
                        || column == getColumnModel().getColumnIndex("giacenza")) {
                    double qta_confermata = cu.d0(getValueAt(row, getColumnModel().getColumnIndex("quantità confermata")));
                    double giacenza = cu.d0(getValueAt(row, getColumnModel().getColumnIndex("giacenza")));
                    if (qta_confermata > giacenza) {
                        back = new Color(245, 235, 225);
                    }
                }

                if (getValueAt(row, 0) == null) {
                    comp.setFont(getFont().deriveFont(Font.ITALIC));
                    back = new Color(250, 250, 250);
                    //tab.setRowHeight(row, tab.getRowHeight(row) - 4);
                } else {
                    comp.setFont(getFont().deriveFont(Font.PLAIN));
                }
                comp.setForeground(Color.BLACK);
                comp.setBackground(back);
                if (column == getColumn("quantità confermata").getModelIndex()) {
                    comp.setFont(getFont().deriveFont(Font.BOLD));
                } else if (column == getColumnModel().getColumnIndex("giacenza")) {
                    Color c = comp.getForeground();
                    double qta = CastUtils.toDouble0All(getValueAt(row, getColumnModel().getColumnIndex("giacenza")));
                    if (qta <= 0) {
                        if ((c.getRed() + c.getGreen() + c.getBlue()) / 3 > 125) {
                            comp.setForeground(new Color(255, 180, 180));
                        } else {
                            comp.setForeground(Color.RED);
                        }
                    }
                } else {
                }

                if (comp instanceof JLabel) {
                    ((JLabel) comp).setBorder(noFocusBorder);
                }
                return comp;
            }

        };
    }
}

class LineWrapCellRenderer extends JTextArea implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        this.setText((String) value);
        this.setWrapStyleWord(true);
        this.setLineWrap(true);

        this.validate();
        this.updateUI();

        try {
//            System.out.println("row:" + row);
            int fontHeight = this.getFontMetrics(this.getFont()).getHeight();
            int textLength = this.getText().length();
//            System.out.println("textLength = " + textLength);
//            System.out.println("getRows = " + getRows());
//            System.out.println("getColumns = " + getColumns());
            int lines = textLength / this.getRows() + 1;//+1, cause we need at least 1 row.
//            System.out.println("lines = " + lines);
            int height = fontHeight * lines;
//            System.out.println("height = " + height);
            table.setRowHeight(row, height);
        } catch (Exception e) {

        }

        return this;
    }
}

class LineBreakPanelRenderer extends JPanel implements TableCellRenderer {

    // The LineBreakMeasurer used to line-break the paragraph.
    private LineBreakMeasurer lineMeasurer;
    // index of the first character in the paragraph.
    private int paragraphStart;
    // index of the first character after the end of the paragraph.
    private int paragraphEnd;
    private static final Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();

//    static {
//        map.put(TextAttribute.FAMILY, "Serif");
//        map.put(TextAttribute.SIZE, new Float(18.0));
//    }
    private AttributedString text = null;
    private String textRaw = null;
    JTable table = null;
    Integer currentRow = null;
    Integer currentCol = null;
    Font f = null;
    boolean isSelected = false;
    private Color unselectedForeground;
    private Color unselectedBackground;
    boolean hasFocus = false;

    LineBreakPanelRenderer(Font f) {
        this.f = f;
        map.put(TextAttribute.FAMILY, f.getFamily());
        map.put(TextAttribute.SIZE, new Float(f.getSize()));

        lab.setOpaque(true);
        lab.setBackground(new Color(250, 250, 250));
    }

    public void setText(String text) {
        textRaw = text;
        if (text != null && text.length() > 0) {
            this.text = new AttributedString(text, map);
        } else {
            this.text = null;
        }
        //repaint();
    }

    public void paintComponent(Graphics g) {
//        super.paintComponent(g);

        //---- default cell renderer
        Color fg = null;
        Color bg = null;
//        JTable.DropLocation dropLocation = table.getDropLocation();
//        if (dropLocation != null
//                && !dropLocation.isInsertRow()
//                && !dropLocation.isInsertColumn()
//                && dropLocation.getRow() == currentRow
//                && dropLocation.getColumn() == currentCol) {
//
//            fg = DefaultLookup.getColor(this, ui, "Table.dropCellForeground");
//            bg = DefaultLookup.getColor(this, ui, "Table.dropCellBackground");
//
//            isSelected = true;
//        }

//        if (isSelected) {
//            super.setForeground(fg == null ? table.getSelectionForeground()
//                                           : fg);
//            super.setBackground(bg == null ? table.getSelectionBackground()
//                                           : bg);
//	} else {
//            Color background = unselectedBackground != null
//                                    ? unselectedBackground
//                                    : table.getBackground();
//            super.setForeground(unselectedForeground != null
//                                    ? unselectedForeground
//                                    : table.getForeground());
//            super.setBackground(background);
//	}
//        if (CastUtils.toBoolean(getValueAt(row, getColumn("riga confermata").getModelIndex()))) {
//            comp.setFont(comp.getFont().deriveFont(Font.BOLD));
//            comp.setBackground(new Color(200,255,200));
//        } else {
//            comp.setFont(comp.getFont().deriveFont(Font.PLAIN));
//            comp.setBackground(Color.WHITE);
//        }
        if (hasFocus) {
            if (!isSelected && table.isCellEditable(currentRow, currentCol)) {
                Color col;
//                col = DefaultLookup.getColor(table, ui, "Table.focusCellForeground");
                col = UIManager.getColor("Table.focusCellForeground");
                if (col != null) {
                    super.setForeground(col);
                }
//                col = DefaultLookup.getColor(table, ui, "Table.focusCellBackground");
                col = UIManager.getColor("Table.focusCellBackground");
                if (col != null) {
                    super.setBackground(col);
                }
            }
        }
//        System.out.println("row = " + currentRow + " foreground " + getForeground() + " background " + getBackground());
        super.paintComponent(g);
        g.setColor(getForeground());
        //---------- fine default

        if (text == null) {
            int h = 25 + 3;
            if (table.getRowHeight(currentRow) != h) {
                table.setRowHeight(currentRow, h);
            }
            return;
        }

        setBackground(Color.white);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create a new LineBreakMeasurer from the paragraph.
        // It will be cached and re-used.
//        if (lineMeasurer == null) {
        AttributedCharacterIterator paragraph = text.getIterator();
        paragraphStart = paragraph.getBeginIndex();
        paragraphEnd = paragraph.getEndIndex();
        FontRenderContext frc = g2d.getFontRenderContext();
        lineMeasurer = new LineBreakMeasurer(paragraph, frc);
//        }

        // Set break width to width of Component.
//        float breakWidth = (float) getSize().width;
        float breakWidth = (float) table.getColumnModel().getColumn(currentCol).getWidth();

        float drawPosY = 0;
        // Set position to the index of the first character in the paragraph.
        lineMeasurer.setPosition(paragraphStart);

        int lines = 0;
        // Get lines until the entire paragraph has been displayed.
        TextLayout layout = null;
        while (lineMeasurer.getPosition() < paragraphEnd) {
            lines++;

            // Retrieve next layout. A cleverer program would also cache
            // these layouts until the component is re-sized.
//            layout = lineMeasurer.nextLayout(breakWidth);
            int next = lineMeasurer.nextOffset(breakWidth);
            int limit = next;
            try {
                if (limit <= paragraphEnd) {
                    for (int i = lineMeasurer.getPosition(); i < next; ++i) {
                        char c = textRaw.charAt(i);
                        if (c == '\n') {
                            limit = i;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            layout = lineMeasurer.nextLayout(breakWidth - 3, limit + 1, false);

            // Compute pen x position. If the paragraph is right-to-left we
            // will align the TextLayouts to the right edge of the panel.
            // Note: this won't occur for the English text in this sample.
            // Note: drawPosX is always where the LEFT of the text is placed.
            float drawPosX = layout.isLeftToRight()
                    ? 0 : breakWidth - layout.getAdvance();
            drawPosX += 3;

            // Move y-coordinate by the ascent of the layout.
            drawPosY += layout.getAscent();

            // Draw the TextLayout at (drawPosX, drawPosY).
            if (lineMeasurer.getPosition() >= paragraphEnd && lines == 1) {
                int h = (int) (lines * (layout.getAscent() + layout.getDescent() + layout.getLeading()));
                if (h < 25) {
                    h = 25;
                }
                h += 3;
                float hdraw = (h / 2f) + (f.getSize() / 2f) - 3;
//                System.out.println("row = " + currentRow + " hdraw = " + hdraw);
                layout.draw(g2d, drawPosX, hdraw);
            } else {
                layout.draw(g2d, drawPosX, drawPosY);
            }
//            g2d.drawString(String.valueOf(lines), drawPosX, drawPosY);

            // Move y-coordinate in preparation for next layout.
            drawPosY += layout.getDescent() + layout.getLeading();
        }
//        System.out.println("lines = " + lines);

        if (table != null && currentRow != null) {
            int h = (int) (lines * (layout.getAscent() + layout.getDescent() + layout.getLeading()));
//            System.out.println("row = " + currentRow + " / h = " + h);
            if (h < 25) {
                h = 25;
            }
            h += 3;
            if (table.getRowHeight(currentRow) != h) {
                table.setRowHeight(currentRow, h);
            }
        }
    }

    JLabel lab = new JLabel("");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        currentRow = row;
        currentCol = column;
        this.table = table;
        this.isSelected = isSelected;
        this.hasFocus = hasFocus;
        setText((String) value);

        if (table.getValueAt(row, 0) == null) {
            lab.setText(cu.s(value));
            lab.setFont(f.deriveFont(Font.ITALIC));
            lab.setForeground(Color.GRAY);
            return lab;
        }

        return this;
    }

}

class MultiLineHeaderRenderer extends JList implements TableCellRenderer {

    public MultiLineHeaderRenderer() {
        setOpaque(true);
        setForeground(UIManager.getColor("TableHeader.foreground"));
        setBackground(UIManager.getColor("TableHeader.background"));
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));

        ListCellRenderer renderer = getCellRenderer();
        ((JLabel) renderer).setHorizontalAlignment(JLabel.CENTER);
        setCellRenderer(renderer);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        String str = (value == null) ? "" : value.toString();
        if (str.equalsIgnoreCase("quantità confermata")) {
            setFont(table.getFont().deriveFont(Font.BOLD));
        } else {
            setFont(table.getFont().deriveFont((float) (table.getFont().getSize() - 1)));
        }
        if (str.equalsIgnoreCase("quantità già confermata")) {
            str = "quantità\ngià confermata";
        } else {
            str = StringUtils.replace(str, " ", "\n");
        }

        BufferedReader br = new BufferedReader(new StringReader(str));
        String line;
        Vector v = new Vector();
        try {
            while ((line = br.readLine()) != null) {
                v.addElement(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setListData(v);
        return this;
    }
}
