/**
 * Invoicex
 * Copyright (c) 2005-2016 Marco Ceccarelli, Tnx srl
 *
 * Questo software è soggetto, e deve essere distribuito con la licenza
 * GNU General Public License, Version 2. La licenza accompagna il software
 * o potete trovarne una copia alla Free Software Foundation http://www.fsf.org .
 *
 * This software is subject to, and may be distributed under, the
 * GNU General Public License, Version 2. The license should have
 * accompanied the software or you may obtain a copy of the license
 * from the Free Software Foundation at http://www.fsf.org .
 *
 * --
 * Marco Ceccarelli (m.ceccarelli@tnx.it)
 * Tnx snc (http://www.tnx.it)
 *
 */
/*
 * JDialogMatricole.java
 *
 * Created on 21 maggio 2007, 12.03
 */
package it.tnx.invoicex.gui;

import com.jidesoft.hints.AbstractListIntelliHints;
import com.lowagie.text.DocumentException;
import gestioneFatture.iniFileProp;
import gestioneFatture.main;
import it.tnx.Db;
import it.tnx.accessoUtenti.Utente;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.table.EditorUtils;
import it.tnx.commons.table.EditorUtils.ComboEditor;
import it.tnx.invoicex.Magazzino;
import it.tnx.invoicex.MyAbstractListIntelliHints;
import it.tnx.invoicex.data.Giacenza;
import it.tnx.invoicex.gui.utils.StringKeyValueHint;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicListUI;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import net.sf.jasperreports.engine.JRException;

/**
 *
 * @author mceccarelli
 */
public class JDialogMatricole extends javax.swing.JDialog {

    public Object[][] values = null;
    public TableModel model = null;
    private int riga;
    private Integer id_riga;
    private Integer id_padre_doc;
    private String articolo;
    public boolean ok = true;
    public boolean matricoleDaInserire = false;
    String nomeTabMatricole;

    /**
     * Creates new form JDialogMatricole
     */
    public JDialogMatricole(java.awt.Frame parent, boolean modal, int righe, int riga, String articolo, String serie, String numero, String anno, String nomeTabMatricole, Integer id_riga, Integer id_padre_doc) {
        super(parent, modal);
        this.riga = riga;
        this.id_riga = id_riga;
        this.articolo = articolo;
        this.nomeTabMatricole = nomeTabMatricole;

        ArrayList lista_prima = new ArrayList();
        String sql = "";
        values = new Object[righe][1];

        initComponents();

        jTable1.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        try {
            //prima vado a togliere le matricole precedenti se presenti
            sql = "select * from " + nomeTabMatricole;
//            sql += " where numero = '" + numero + "'";
//            sql += " and anno = '" + anno + "'";
//            sql += " and riga = '" + riga + "'";
            sql += " where id_padre_righe = " + id_riga;

            System.out.println("seleziono le eventuali matricole abbinate:" + sql);
            ResultSet r = Db.openResultSet(sql);
            int i = 0;
            while (r.next()) {
                System.out.println("memorizzo:" + r.getString("matricola"));
                lista_prima.add(r.getString("matricola"));
                try {
                    values[i][0] = r.getString("matricola");
                } catch (ArrayIndexOutOfBoundsException e1) {
                }
                i++;
            }

            sql = "delete from " + nomeTabMatricole;
//            sql += " where numero = '" + numero + "'";
//            sql += " and anno = '" + anno + "'";
//            sql += " and riga = '" + riga + "'";
            sql += " where id_padre_righe = " + id_riga;
            System.out.println("elimino le matricole:" + sql);
            Db.executeSql(sql);
        } catch (Exception err) {
            err.printStackTrace();
        }

        model = new javax.swing.table.DefaultTableModel(values, new String[]{"Matricola"}) {
            Class[] types = new Class[]{
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

        };
        jTable1.setModel(model);

        ArrayList<Giacenza> beans = null;
        try {
            beans = main.magazzino.getGiacenza(true, articolo, null);
        } catch (Exception e) {
            SwingUtils.showExceptionMessage(this, e);
            return;
        }

        Vector beans2 = new Vector();
        for (Giacenza g : beans) {
            beans2.add(new StringKeyValueHint(g.getMatricola()));
        }
        JPanelCellMatricola cell = new JPanelCellMatricola(beans2);
        TableCellEditorMatricola editor = new TableCellEditorMatricola(cell);

        int vColIndex = 0;
        TableColumn col = jTable1.getColumnModel().getColumn(vColIndex);
        col.setCellEditor(editor);

        jTable1.setRowHeight(20);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new JTable() {
            public void changeSelection(final int row, final int column, boolean toggle, boolean extend) {
                super.changeSelection(row, column, toggle, extend);
                /*System.out.println("change sel " + row);
                if (editCellAt(row, column)) {
                    //transferFocus();
                    Component editor = getEditorComponent();
                    editor.requestFocusInWindow();
                }*/
            }
        }
        ;
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Gestione matricole");
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Matricola"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTable1PropertyChange(evt);
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable1KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTable1KeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/22x22/devices/media-floppy.png"))); // NOI18N
        jButton1.setText("Conferma");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/22x22/actions/edit-undo.png"))); // NOI18N
        jButton2.setText("Annulla");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setText("Inserire i codici matricola");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jButton2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton1))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 231, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        model = null;
        ok = false;
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //prima controlla che si siano scelte tutte le matricole richieste
        for (int i = 0; i < jTable1.getRowCount(); i++) {
            Object value = jTable1.getValueAt(i, 0);
            if (value == null) {
                JOptionPane.showMessageDialog(this, "Ci sono una o più matricole da inserire", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        //prima controlla che non si sia scelto due volte la solita matricola
        for (int i = 0; i < jTable1.getRowCount(); i++) {
            Object value = jTable1.getValueAt(i, 0);
            int conta = 0;
            for (int i2 = 0; i2 < jTable1.getRowCount(); i2++) {
                Object value2 = jTable1.getValueAt(i2, 0);
                if (value2.equals(value)) {
                    conta++;
                }
            }
            if (conta >= 2) {
                JOptionPane.showMessageDialog(this, "Ci sono una o più matricole inserite più di una volta", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        jTable1.changeSelection(0, 0, false, false);
        jTable1.requestFocus();
    }//GEN-LAST:event_formWindowGainedFocus

    private void jTable1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTable1PropertyChange
//        System.out.println("jTable1.property " + evt.getPropertyName() + " da:" + evt.getOldValue() + " a:" + evt.getNewValue());
    }//GEN-LAST:event_jTable1PropertyChange

    private void jTable1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyTyped
//        System.out.println("evt = " + evt);
    }//GEN-LAST:event_jTable1KeyTyped

    private void jTable1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyPressed
//        System.out.println("evt = " + evt);
    }//GEN-LAST:event_jTable1KeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    public void setMatricoleDaInserire(boolean come) {
        if (come) {
            matricoleDaInserire = true;
            // Set the combobox editor on the 1st visible column
            int vColIndex = 0;
            TableColumn col = jTable1.getColumnModel().getColumn(vColIndex);
            col.setCellEditor(new DefaultCellEditor(new JTextField()));
        }
    }

    public static void main(String[] args) throws FileNotFoundException, DocumentException, IOException, JRException, Exception {
        try {
            File fwd = new File("./");
            try {
                main.wd = fwd.getCanonicalPath() + File.separator;
            } catch (Exception e) {
                e.printStackTrace();
            }
            main.paramProp = "param_prop_blank_prima_nota.txt";
            main.fileIni = new iniFileProp();
            main.fileIni.realFileName = main.wd + main.paramProp;
            main.loadIni();
            main.utente = new Utente(1);
            main.pluginRitenute = true;

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            JFrame main = new JFrame();
            main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JDesktopPane desk = new JDesktopPane();
            main.getContentPane().add(desk);

            JDialogMatricole d = new JDialogMatricole(null, true, 5, 0, "test-matricole", "", "0", "0", "righ_fatt_acquisto_matricole", 0, 0);
            d.setLocationRelativeTo(null);
            d.setMatricoleDaInserire(true);

//            final JInternalFrameGenerazioneRegistrazioni frame = new JInternalFrameGenerazioneRegistrazioni();
//            frame.setBounds(10, 10, 700, 500);
//            frame.setVisible(true);
//            desk.add(frame);
            main.setSize(1000, 800);
            main.setLocationRelativeTo(null);
            main.setVisible(true);

            d.setVisible(true);

//            frame.dal.setDate(DateUtils.getDate(2013, 10, 28));
//            frame.vendita.setSelected(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {

    public MyComboBoxRenderer(String[] items) {
        super(items);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        // Select the current value
        setSelectedItem(value);
        return this;
    }
}