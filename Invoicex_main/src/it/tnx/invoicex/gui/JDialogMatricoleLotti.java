/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JDialogLotti.java
 *
 * Created on 5-gen-2010, 17.42.40
 */
package it.tnx.invoicex.gui;

import com.jidesoft.swing.AutoCompletionComboBox;
import com.lowagie.text.DocumentException;
import gestioneFatture.iniFileProp;
import gestioneFatture.main;
import it.tnx.Db;
import it.tnx.accessoUtenti.Utente;
import it.tnx.commons.DbUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.table.EditorUtils;
import it.tnx.commons.table.EditorUtils.ComboEditor;
import it.tnx.invoicex.Magazzino;
import it.tnx.invoicex.data.Giacenza;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author mceccarelli
 */
public class JDialogMatricoleLotti extends javax.swing.JDialog {

    public boolean ret = false;
    String tipo /* C carica, S scarica*/;
    Double qta;
    String articolo;
    String tabella;
    Integer id;
    Integer id_padre_doc;
    boolean inserimento = false;

    /** Creates new form JDialogLotti */
    public JDialogMatricoleLotti(java.awt.Frame parent, boolean modal, boolean inserimento) {
        super(parent, modal);
        this.inserimento = inserimento;
        initComponents();
        tab.setSurrendersFocusOnKeystroke(true);
        tab.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labmsg = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tab = new javax.swing.JTable();
        conferma = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Gestione Matricole e Lotti");

        labmsg.setText("...");

        tab.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Matricola", "Lotto"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tab.setRowHeight(20);
        jScrollPane1.setViewportView(tab);
        tab.getColumnModel().getColumn(0).setPreferredWidth(100);
        tab.getColumnModel().getColumn(1).setPreferredWidth(100);

        conferma.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Checkmark-16.png"))); // NOI18N
        conferma.setText("Conferma");
        conferma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confermaActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Delete Sign-16.png"))); // NOI18N
        jButton2.setText("Annulla");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jButton2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(conferma))
                    .add(labmsg, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(labmsg)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(conferma)
                    .add(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void confermaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confermaActionPerformed
        try {
            //controlli...
            //almeno un codice lotto
            boolean ok = true;

//            int contalotti = 0;
//            for (int i = 0; i < tab.getRowCount(); i++) {
//                Object lotto = tab.getValueAt(i, 1);
//                if (lotto != null && lotto.toString().trim().length() > 0) {
//                    contalotti++;
//                }
//            }
//            if (contalotti == 0) {
//                if (!SwingUtils.showYesNoMessage(this, "Sembra che non hai inserito nessun lotto, confermi comunque ?")) {
//                    ok = false;
//                }
//            }

            if (ok) {
                if (tabella.equals("movimenti_magazzino")) {
                    //riprendo movimento e lo splitto per i lotti
                    ArrayList<Map> list = DbUtils.getListMap(Db.getConn(), "select * from " + tabella + " where id = " + id);
                    //                DebugUtils.dump(list);
                    System.out.println("id da rimuovere = " + list.get(0).get("id"));
                    String sql = "delete from " + tabella + " where id = " + list.get(0).get("id");
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                    for (int i = 0; i < tab.getRowCount(); i++) {
                        String matricola = null;
                        String lotto = null;
                        if (tab.getColumnCount() == 1) {
                            System.out.println("tab.getValueAt(i, 0):" + tab.getValueAt(i, 0));
                            Giacenza g = (Giacenza) tab.getValueAt(i, 0);
                            matricola = g.getMatricola();
                            lotto = g.getLotto();
                        } else {
                            matricola = (String) tab.getValueAt(i, 0);
                            lotto = (String) tab.getValueAt(i, 1);
                        }
                        if (lotto != null && lotto.toString().trim().length() > 0 && matricola != null && !StringUtils.isEmpty(matricola)) {
                            System.out.println("aggiungere lotto:" + lotto + " e matricola:" + matricola);

                            HashMap m = (HashMap) list.get(0);
                            m.put("lotto", lotto);
                            m.put("matricola", matricola);
                            m.put("quantita", 1);
                            m.remove("id");
                            sql = "insert into " + tabella + " set " + DbUtils.prepareSqlFromMap(m);
                            System.out.println("sql:" + sql);
                            DbUtils.tryExecQuery(Db.getConn(), sql);
                        }
                    }
                } else {
                    //rimuovo eventuali precedenti
                    String sql = "delete from " + tabella + " where id_padre = " + id;
                    System.out.println("elimino i records precedenti da " + tabella + " sql:" + " esito:" + DbUtils.tryExecQuery(Db.getConn(), sql));
                    //inserisco
//                    List<HashMap> list = DbUtils.getListMap(Db.getConn(), "select * from " + tabella + " where id = " + id);
//                    System.out.println("recs:" + list.size());
//                    HashMap rectab = list.get(0);
                    for (int i = 0; i < tab.getRowCount(); i++) {
                        String matricola = null;
                        String lotto = null;
                        if (tab.getColumnCount() == 1) {
                            System.out.println("tab.getValueAt(i, 0):" + tab.getValueAt(i, 0));
                            Giacenza g = (Giacenza) tab.getValueAt(i, 0);
                            matricola = g.getMatricola();
                            lotto = g.getLotto();
                        } else {
                            matricola = (String) tab.getValueAt(i, 0);
                            lotto = (String) tab.getValueAt(i, 1);
                        }
                        if (lotto != null && lotto.toString().trim().length() > 0 && matricola != null && !StringUtils.isEmpty(matricola)) {
                            System.out.println("aggiungere lotto:" + lotto);

                            HashMap r = new HashMap();
                            r.put("id_padre", id);
                            r.put("codice_articolo", articolo);
                            r.put("lotto", lotto);
                            r.put("matricola", matricola);
                            r.put("qta", 1);
                            sql = "insert into " + tabella + " set " + DbUtils.prepareSqlFromMap(r);
                            System.out.println("aggiungo i lotti:" + sql);
                            DbUtils.tryExecQuery(Db.getConn(), sql);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ret = true;
        dispose();
    }//GEN-LAST:event_confermaActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton conferma;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JLabel labmsg;
    private javax.swing.JTable tab;
    // End of variables declaration//GEN-END:variables

    public void init(String tipo /* C carica, S scarica*/, Double qta, String articolo, String tabella, Integer id, String lotto_iniziale) {
        this.tipo = tipo;
        this.qta = qta;
        this.articolo = articolo;
        this.tabella = tabella;
        this.id = id;

        if (tipo.equals("S")) {
            labmsg.setText("Indica le matricole con il loro lotto da scaricare");
        } else {
            labmsg.setText("Indica le matricole con il loro lotto da caricare");
        }
        labmsg.setText(labmsg.getText() + " dell'articolo " + articolo);

        if (tipo.equals("C")) {
            if (lotto_iniziale != null) {
                tab.setValueAt(lotto_iniziale, 0, 1);
            }
            boolean success = tab.editCellAt(0, 0);
            if (success) {
                tab.changeSelection(0, 0, false, false);
            }
        } else {
            tab.setModel(new javax.swing.table.DefaultTableModel(
                    new Object[][]{
                        {}
                    },
                    new String[]{
                        "Matricola / Lotto"
                    }) {

                Class[] types = new Class[]{
                    java.lang.String.class
                };

                public Class getColumnClass(int columnIndex) {
                    return types[columnIndex];
                }
            });

            ((DefaultTableModel) tab.getModel()).setRowCount(qta.intValue());
            ArrayList<Map> lista;
            ArrayList<Giacenza> beans = null;
            try {
                beans = main.magazzino.getGiacenza(true, articolo, null);
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(this, e);
                return;
            }
            
            try {
                lista = DbUtils.getListMap(Db.getConn(), "select * from " + tabella + " where id_padre = " + id);
                int row = 0;
                for (Map rec : lista) {
                    Giacenza g = new Giacenza();
                    g.setLotto((String) rec.get("lotto"));
                    g.setMatricola((String) rec.get("matricola"));
                    tab.setValueAt(g, row, 0);
                    beans.add(g);
                    row++;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Set the combobox editor on the 1st visible column
            TableColumn col = tab.getColumnModel().getColumn(0);
            AutoCompletionComboBox combo2b = new AutoCompletionComboBox(new Vector(beans));
            combo2b.setBorder(BorderFactory.createEmptyBorder());
            ComboEditor editor2b = new EditorUtils.ComboEditor(combo2b);
            col.setCellEditor(editor2b);

        }
    }

    public ArrayList<HashMap<String, String>> getMatricoleLotti() {
        ArrayList ret = new ArrayList();
        for (int row = 0; row < tab.getRowCount(); row++) {
            if (tab.getColumnCount() == 1) {
                if (tab.getValueAt(row, 0) != null) {
                    Giacenza g = (Giacenza) tab.getValueAt(row, 0);
                    HashMap m = new HashMap();
                    m.put("matricola", g.getMatricola());
                    m.put("lotto", g.getLotto());
                    ret.add(m);
                }
            } else {
                if (tab.getValueAt(row, 0) != null && tab.getValueAt(row, 1) != null) {
                    HashMap m = new HashMap();
                    m.put("matricola", (String) tab.getValueAt(row, 0));
                    m.put("lotto", (String) tab.getValueAt(row, 1));
                    ret.add(m);
                }
            }

        }
        return ret;
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