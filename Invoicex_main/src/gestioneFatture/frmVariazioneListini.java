/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gestioneFatture;

import com.jidesoft.hints.AbstractListIntelliHints;
import it.tnx.SwingWorker;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.StringUtilsTnx;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.dbu;
import it.tnx.gui.MyBasicArrowButton;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Vector;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author atoce
 */
public class frmVariazioneListini extends javax.swing.JInternalFrame {

    private AbstractListIntelliHints alRicercaArticolo = null;
    private AbstractListIntelliHints dalRicercaArticolo = null;
    private frmArtiConListino padre = null;
    /**
     * Creates new form frmVariazioneListini
     */
    public frmVariazioneListini(frmArtiConListino padre) {
        initComponents();

        this.padre = padre;
        comListino.dbOpenList(it.tnx.Db.getConn(), "select descrizione, codice from tipi_listino where ricarico_flag is null or ricarico_flag != 'S'");

        comCategoria.dbAddElement("", null);
        comCategoria.dbOpenList(it.tnx.Db.getConn(), "select substr(categoria,1,60) as categoria, id from categorie_articoli order by categoria", null, false);
        comSottocategoria.dbClearList();

        comFornitoreAbituale.dbOpenList(it.tnx.Db.getConn(), "SELECT substr(ragione_sociale,1,60) as ragione_sociale, codice FROM clie_forn where (tipo is null or tipo = 'F' or tipo = 'E') order by ragione_sociale");

        alRicercaArticolo = new AbstractListIntelliHints(texAArticolo) {
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
                        if (StringUtils.isBlank(((ClienteHint) value).codice)) {
                            content = "";
                        }
                        Color c = null;
                        if (!isSelected) {
                            c = new Color(240, 240, 100);
                        } else {
                            c = new Color(100, 100, 40);
                        }
                        String rgb = Integer.toHexString(c.getRGB());
                        rgb = rgb.substring(2, rgb.length());

                        content = StringUtilsTnx.highlightWord(content, word, "<span style='background-color: " + rgb + "'>", "</span>");

                        lab.setText("<html>" + content + "</html>");
                        System.out.println(index + ":" + content);
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
                    conn = it.tnx.Db.getConn();

                    String sql = ""
                            + "SELECT codice, descrizione FROM articoli"
                            + " where (codice like '%" + it.tnx.Db.aa(current_search) + "%'"
                            + " or descrizione like '%" + it.tnx.Db.aa(current_search) + "%'"
                            + " ) and descrizione != ''"
                            + " order by codice limit 50";

                    System.out.println("sql ricerca:" + sql);
                    ResultSet rs = DbUtils.tryOpenResultSet(conn, sql);
                    Vector v = new Vector();

                    ClienteHint clientevuoto = new ClienteHint();
                    clientevuoto.codice = "";
                    clientevuoto.ragione_sociale = "";
                    clientevuoto.obsoleto = false;
                    v.add(clientevuoto);

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
                super.acceptHint(arg0);
                try {
                    if (((ClienteHint) arg0).codice.equals("")) {
                        texAArticolo.setText("");
                        texAArticolo.setText("");
                    } else {
                        texAArticolo.setText(((ClienteHint) arg0).codice);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        dalRicercaArticolo = new AbstractListIntelliHints(texDaArticolo) {
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
                        if (StringUtils.isBlank(((ClienteHint) value).codice)) {
                            content = "";
                        }
                        Color c = null;
                        if (!isSelected) {
                            c = new Color(240, 240, 100);
                        } else {
                            c = new Color(100, 100, 40);
                        }
                        String rgb = Integer.toHexString(c.getRGB());
                        rgb = rgb.substring(2, rgb.length());

                        content = StringUtilsTnx.highlightWord(content, word, "<span style='background-color: " + rgb + "'>", "</span>");

                        lab.setText("<html>" + content + "</html>");
                        System.out.println(index + ":" + content);
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
                    conn = it.tnx.Db.getConn();

                    String sql = ""
                            + "SELECT codice, descrizione FROM articoli"
                            + " where (codice like '%" + it.tnx.Db.aa(current_search) + "%'"
                            + " or descrizione like '%" + it.tnx.Db.aa(current_search) + "%'"
                            + " ) and descrizione != ''"
                            + " order by codice limit 50";

                    System.out.println("sql ricerca:" + sql);
                    ResultSet rs = DbUtils.tryOpenResultSet(conn, sql);
                    Vector v = new Vector();

                    ClienteHint clientevuoto = new ClienteHint();
                    clientevuoto.codice = "";
                    clientevuoto.ragione_sociale = "";
                    clientevuoto.obsoleto = false;
                    v.add(clientevuoto);

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
                super.acceptHint(arg0);
                try {
                    if (((ClienteHint) arg0).codice.equals("")) {
                        texDaArticolo.setText("");
                        texDaArticolo.setText("");
                    } else {
                        texDaArticolo.setText(((ClienteHint) arg0).codice);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void aggiornaSottocategoria() {
        comSottocategoria.dbClearList();
        comSottocategoria.dbAddElement("", null);

        System.out.println("SOTTOCATEGORIA refreshing = trova key " + comCategoria.getSelectedKey());
        comSottocategoria.dbOpenList(it.tnx.Db.getConn(), "select sottocategoria, id from sottocategorie_articoli where id_padre = " + it.tnx.Db.pc(comCategoria.getSelectedKey(), Types.INTEGER) + " order by sottocategoria", null, false);
        comSottocategoria.dbTrovaKey(comCategoria.getSelectedKey());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        arrotondamentoGroup = new javax.swing.ButtonGroup();
        comListino = new tnxbeans.tnxComboField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        texDaArticolo = new javax.swing.JTextField();
        texAArticolo = new javax.swing.JTextField();
        comCategoria = new tnxbeans.tnxComboField();
        comSottocategoria = new tnxbeans.tnxComboField();
        comFornitoreAbituale = new tnxbeans.tnxComboField();
        comDecimali = new javax.swing.JComboBox();
        radArrMatematico = new javax.swing.JRadioButton();
        radArrInf = new javax.swing.JRadioButton();
        radArrSup = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        apriDaArticolo = new MyBasicArrowButton(BasicArrowButton.SOUTH, UIManager.getColor("ComboBox.buttonBackground"), UIManager.getColor("ComboBox.buttonShadow"), UIManager.getColor("ComboBox.buttonDarkShadow"), UIManager.getColor("ComboBox.buttonHighlight"));
        apriAArticolo = new MyBasicArrowButton(BasicArrowButton.SOUTH, UIManager.getColor("ComboBox.buttonBackground"), UIManager.getColor("ComboBox.buttonShadow"), UIManager.getColor("ComboBox.buttonDarkShadow"), UIManager.getColor("ComboBox.buttonHighlight"));
        jLabel8 = new javax.swing.JLabel();
        texPercentuale = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Variazione Listini");

        comListino.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setFont(jLabel1.getFont());
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Listino");

        jLabel2.setFont(jLabel2.getFont());
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Da articolo");

        jLabel3.setFont(jLabel3.getFont());
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("a articolo");

        jLabel4.setFont(jLabel4.getFont());
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Categoria");

        jLabel5.setFont(jLabel5.getFont());
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Sottocategoria");

        jLabel6.setFont(jLabel6.getFont());
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Fornitore abituale");

        jLabel7.setFont(jLabel7.getFont());
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Arrotonda al decimale");

        comCategoria.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comCategoria.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comCategoriaItemStateChanged(evt);
            }
        });

        comSottocategoria.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        comFornitoreAbituale.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        comDecimali.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5" }));
        comDecimali.setSelectedIndex(2);

        arrotondamentoGroup.add(radArrMatematico);
        radArrMatematico.setSelected(true);
        radArrMatematico.setText("Matematico");

        arrotondamentoGroup.add(radArrInf);
        radArrInf.setText("Per difetto");

        arrotondamentoGroup.add(radArrSup);
        radArrSup.setText("Per eccesso");

        apriDaArticolo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apriDaArticoloActionPerformed(evt);
            }
        });

        apriAArticolo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apriAArticoloActionPerformed(evt);
            }
        });

        jLabel8.setFont(jLabel8.getFont().deriveFont(jLabel8.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Variazione percentuale");

        texPercentuale.setColumns(6);

        jLabel9.setFont(jLabel9.getFont());
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Arrotondamento");

        jButton1.setFont(jButton1.getFont());
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/22x22/actions/document-save-as.png"))); // NOI18N
        jButton1.setText("Avvia Variazione");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comListino, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comCategoria, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comSottocategoria, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texAArticolo))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texDaArticolo)))
                        .add(0, 0, 0)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(apriDaArticolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, apriAArticolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(comFornitoreAbituale, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(radArrMatematico)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(radArrInf)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(radArrSup))
                                    .add(layout.createSequentialGroup()
                                        .add(texPercentuale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(18, 18, 18)
                                        .add(jLabel7)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(comDecimali, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(0, 147, Short.MAX_VALUE))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(jButton1)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jLabel1, jLabel2, jLabel3, jLabel4, jLabel5, jLabel6, jLabel7, jLabel8}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(comListino, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(apriDaArticolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel2)
                        .add(texDaArticolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel3)
                        .add(texAArticolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(apriAArticolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(comCategoria, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(comSottocategoria, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(comFornitoreAbituale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(texPercentuale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7)
                    .add(comDecimali, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radArrMatematico)
                    .add(radArrInf)
                    .add(radArrSup)
                    .add(jLabel9))
                .add(12, 12, 12)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void comCategoriaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comCategoriaItemStateChanged
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            System.out.println("SOTTOCATEGORIA  clear list");
            comSottocategoria.dbClearList();
        } else if (evt.getStateChange() == ItemEvent.SELECTED) {
            System.out.println("SOTTOCATEGORIA  carico ");
            comSottocategoria.dbClearList();
            comSottocategoria.dbAddElement("", null);
            comSottocategoria.dbOpenList(it.tnx.Db.getConn(), "select sottocategoria, id from sottocategorie_articoli where id_padre = " + it.tnx.Db.pc(comCategoria.getSelectedKey(), Types.INTEGER) + " order by sottocategoria", null, false);
        }
    }//GEN-LAST:event_comCategoriaItemStateChanged

    private void apriDaArticoloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apriDaArticoloActionPerformed
        dalRicercaArticolo.showHints();
    }//GEN-LAST:event_apriDaArticoloActionPerformed

    private void apriAArticoloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apriAArticoloActionPerformed
        alRicercaArticolo.showHints();
    }//GEN-LAST:event_apriAArticoloActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        final String listino = CastUtils.toString(comListino.getSelectedKey());
        if (listino.equals("-1")) {
            SwingUtils.showErrorMessage(this, "Seleziona almeno un listino per procedere con la variazione", "Impossibile procedere");
        } else {
            String percenutaleTxt = CastUtils.toString(texPercentuale.getText());
            percenutaleTxt = percenutaleTxt.replace(".", ",");
            final Double percentuale = CastUtils.toDouble(percenutaleTxt);
            final frmArtiConListino parent = this.padre;
            if(percentuale == null || percentuale == 0){
                SwingUtils.showErrorMessage(this, "Inserisci la percentuale di variazione", "Impossibile procedere");
            } else {
                if (SwingUtils.showYesNoMessage(this, "L'operazione non è reversibile, è consigliato effettuare una copia di sicurezza prima di procedere. Vuoi continuare con la variazione prezzi?", "Attenzione")) {

                    // Recupero il numero di articoli da correggere
                    String sql = "SELECT art.codice, pre.prezzo "
                            + "FROM articoli_prezzi pre LEFT JOIN articoli art ON pre.articolo = art.codice "
                            + "WHERE pre.listino = " + Db.pc(listino, Types.VARCHAR);

                    if (!texDaArticolo.getText().equals("")) {
                        sql += " AND art.codice >= " + Db.pc(texDaArticolo.getText(), Types.VARCHAR);
                    }

                    if (!texAArticolo.getText().equals("")) {
                        sql += " AND art.codice <= " + Db.pc(texAArticolo.getText(), Types.VARCHAR);
                    }

                    if (comCategoria.getSelectedKey() != null && !CastUtils.toString(comCategoria.getSelectedKey()).equals("-1")) {
                        sql += " AND art.categoria = " + Db.pc(CastUtils.toString(comCategoria.getSelectedKey()), Types.INTEGER);
                    }

                    if (comSottocategoria.getSelectedKey() != null && !CastUtils.toString(comSottocategoria.getSelectedKey()).equals("-1")) {
                        sql += " AND art.sottocategoria = " + Db.pc(CastUtils.toString(comSottocategoria.getSelectedKey()), Types.INTEGER);
                    }

                    if (!CastUtils.toString(comFornitoreAbituale.getSelectedKey()).equals("-1")) {
//                        sql += " AND art.fornitore <= " + Db.pc(comFornitoreAbituale.getText(), Types.VARCHAR);
                        sql += " AND art.fornitore = " + dbu.sql(comFornitoreAbituale.getSelectedKey());
                    }

                    sql += " ORDER BY art.codice ASC";
                    
                    System.out.println("sql aggiornamento listini = " + sql);

                    try {
                        final ResultSet articoli_variazione = Db.openResultSet(Db.getConn(), sql);
                        Integer articoli_totali = 0;

                        while (articoli_variazione.next()) {
                            articoli_totali++;
                        }

                        if (articoli_totali == 0) {
                            SwingUtils.showErrorMessage(this, "Il filtraggio non ha prodotto articoli da modificare", "Impossibile proseguire");
                        } else {
                            final JDialogInsert dialog = new JDialogInsert(main.getPadre(), false, "", articoli_totali);
                            final Integer decimali = CastUtils.toInteger(comDecimali.getSelectedItem());
                            final boolean arrotondaMatematico = radArrMatematico.isSelected();
                            final boolean arrotondaInferiore = radArrInf.isSelected();
                            final boolean arrotondaSuperiore = radArrSup.isSelected();
                            
                            dialog.setLocationRelativeTo(null);
                            dialog.setVisible(true);

                            SwingWorker sw = new SwingWorker() {

                                public Object construct() {
                                    try {
                                        articoli_variazione.beforeFirst();
                                        while(articoli_variazione.next()){
                                            dialog.updateValue();
                                            
                                            String codice = articoli_variazione.getString("codice");
                                            Double prezzo = articoli_variazione.getDouble("prezzo");
                                            
                                            if(prezzo > 0){
                                                Double variazione = prezzo * percentuale / 100d;
                                                Double newPrezzo = prezzo + variazione;
                                                
                                                if(arrotondaMatematico){
                                                    newPrezzo = it.tnx.Util.round(newPrezzo, decimali);
                                                } else if(arrotondaInferiore){
                                                    newPrezzo = it.tnx.Util.floor(newPrezzo, decimali);
                                                } else if(arrotondaSuperiore){
                                                    newPrezzo = it.tnx.Util.ceil(newPrezzo, decimali);
                                                }

                                                String sqlUpdate = "UPDATE articoli_prezzi SET prezzo = " + Db.pc(newPrezzo, Types.DECIMAL) + " WHERE articolo = " + Db.pc(codice, Types.VARCHAR) + " AND listino = " + Db.pc(listino, Types.VARCHAR);
                                                Db.executeSql(Db.getConn(), sqlUpdate);
                                                System.out.println("ARTICOLO: " + codice + ", PREZZO INIZIALE: " + prezzo + ", DA VARIARE: " + percentuale + "% (" + variazione + "€), NUOVO PREZZO: " + newPrezzo);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    parent.dati.dbRefresh();
                                    SwingUtils.showInfoMessage(dialog, "Variazione prezzi completata correttamente", "Variazione completata");
                                    dialog.dispose();
                                    
                                    return null;
                                }
                            };

                            sw.start();
                        }
                    } catch (Exception e) {
                        SwingUtils.showExceptionMessage(this, e);
                    }

                }
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton apriAArticolo;
    private javax.swing.JButton apriDaArticolo;
    private javax.swing.ButtonGroup arrotondamentoGroup;
    public tnxbeans.tnxComboField comCategoria;
    private javax.swing.JComboBox comDecimali;
    public tnxbeans.tnxComboField comFornitoreAbituale;
    public tnxbeans.tnxComboField comListino;
    public tnxbeans.tnxComboField comSottocategoria;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton radArrInf;
    private javax.swing.JRadioButton radArrMatematico;
    private javax.swing.JRadioButton radArrSup;
    private javax.swing.JTextField texAArticolo;
    private javax.swing.JTextField texDaArticolo;
    private javax.swing.JTextField texPercentuale;
    // End of variables declaration//GEN-END:variables
}
