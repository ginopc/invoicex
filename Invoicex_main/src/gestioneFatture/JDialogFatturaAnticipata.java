/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JDialogChooseListino.java
 *
 * Created on 4-feb-2010, 9.48.01
 */
package gestioneFatture;

import it.tnx.Db;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.StringUtilsTnx;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.gui.MyBasicArrowButton;
import it.tnx.invoicex.MyAbstractListIntelliHints;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 *
 * @author Toce Alessio
 */
public class JDialogFatturaAnticipata extends JDialog {

    JFrame padre;
    JInternalFrame elenco;
    Integer idFattura;
    
    public static class ContoHint extends HashMap {
        
        @Override
        public String toString() {
            return cu.s(get("cc")) + " - " + cu.s(get("nome")) + " - " + cu.s(get("indirizzo")) + " " + cu.s(get("comune"));
        }
    }
    
    MyAbstractListIntelliHints conto_hints = null;
    AtomicReference<ContoHint> conto_selezionato_ref = new AtomicReference(null);

    /**
     * Creates new form JDialogChooseListino
     */
    public JDialogFatturaAnticipata(Frame parent, boolean modal, JInternalFrame elenco, Integer idFattura) {
        super(parent, modal);

        this.elenco = elenco;
        this.idFattura = idFattura;

        initComponents();

        //selezione conti correnti
        conto_hints = new MyAbstractListIntelliHints(conto) {
            String current_search = "";

            @Override
            protected JList createList() {
                final JList list = super.createList();
                list.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        String img, tipo;
                        tipo = ((ContoHint) value).toString();
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

                        lab.setText("<html>" + content + "</html>");
                        System.out.println(index + ":" + content);
                        return lab;
                    }
                });
                return list;
            }

            public boolean updateHints(Object arg0) {
                if (arg0 != null && arg0.toString().trim().length() <= 0) {
//                    conto_selezionato_ref.set(null);
//                    return false;
                }

                SwingUtils.mouse_wait();
                current_search = arg0 != null ? arg0.toString() : "";
                Connection conn;
                try {
                    conn = gestioneFatture.Db.getConn();

                    String sql = "SELECT "
                            + " CONCAT('CC ', IFNULL(ban.cc,''), ' - ' , IFNULL(abi.nome,''), ' Ag. ', IFNULL(com.comune,''), ', ', IFNULL(cab.indirizzo,'')) as descrizione, "
                            + " ban.id "
                            + " , ban.cc "
                            + " , abi.nome "
                            + " , abi.abi "
                            + " , cab.indirizzo "
                            + " , com.comune "
                            + " FROM dati_azienda_banche ban "
                            + " LEFT JOIN banche_abi abi ON ban.abi = abi.abi "
                            + " LEFT JOIN banche_cab cab ON ban.cab = cab.cab AND ban.abi = cab.abi "
                            + " LEFT JOIN comuni com ON cab.codice_comune = com.codice"
                            + " where cc like '%" + gestioneFatture.Db.escw(current_search) + "%'"
                            + " or IFNULL(abi.nome,'') like '%" + gestioneFatture.Db.escw(current_search) + "%'"
                            + " or IFNULL(abi.abi,'') like '%" + gestioneFatture.Db.escw(current_search) + "%'"
                            + " or IFNULL(cab.indirizzo,'') like '%" + gestioneFatture.Db.escw(current_search) + "%'"
                            + " or IFNULL(com.comune,'') like '%" + gestioneFatture.Db.escw(current_search) + "%'"
                            + " order by IFNULL(abi.nome, '')";

                    System.out.println("sql ricerca cc:" + sql);
                    ResultSet rs = DbUtils.tryOpenResultSet(conn, sql);
                    Vector v = new Vector();

                    while (rs.next()) {
                        ContoHint conto = new ContoHint();
                        Map m = dbu.getRowMap(rs);
                        conto.putAll(m);
                        v.add(conto);
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
                    conto.setText(arg0.toString());
                    conto_selezionato_ref.set((ContoHint)arg0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    JDialogFatturaAnticipata() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        butAnnulla = new javax.swing.JButton();
        conto = new javax.swing.JTextField();
        apriconti = new MyBasicArrowButton(BasicArrowButton.SOUTH, UIManager.getColor("ComboBox.buttonBackground"), UIManager.getColor("ComboBox.buttonShadow"), UIManager.getColor("ComboBox.buttonDarkShadow"), UIManager.getColor("ComboBox.buttonHighlight"));

        setTitle("Anticipa Fattura");
        setBackground(new java.awt.Color(224, 223, 227));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jLabel1.setText("Conto Corrente:");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/appointment-new.png"))); // NOI18N
        jButton1.setText("Marca come anticpata");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        butAnnulla.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-undo.png"))); // NOI18N
        butAnnulla.setText("Annulla");
        butAnnulla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAnnullaActionPerformed(evt);
            }
        });

        conto.setColumns(40);
        conto.setName("cliente"); // NOI18N

        apriconti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apricontiActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(0, 274, Short.MAX_VALUE)
                        .add(butAnnulla)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton1))
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(conto)
                        .add(0, 0, 0)
                        .add(apriconti, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(apriconti, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel1)
                        .add(conto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 63, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(butAnnulla))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    private void butAnnullaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAnnullaActionPerformed
        dispose();
    }//GEN-LAST:event_butAnnullaActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        if (conto_selezionato_ref.get() != null) {
            Integer idConto = CastUtils.toInteger(conto_selezionato_ref.get().get("id") );

            String sql = "UPDATE test_fatt SET anticipata = 'S',"
                    + " banca_di_anticipazione = " + Db.pc(idConto, Types.INTEGER)
                    + " WHERE id = " + Db.pc(this.idFattura, Types.INTEGER);

            Db.executeSql(sql);

            System.out.println("SQL: " + sql);

            frmElenFatt frm = (frmElenFatt) this.elenco;

            if (main.fileIni.getValueBoolean("pref", "ColAgg_Anticipata", false) == false) {
                main.fileIni.setValue("pref", "ColAgg_Anticipata", true);
            }
            
            frm.dbRefresh();
            this.dispose();

//            SwingUtils.showInfoMessage(frm, "Fattura marcata correttamente come anticipata!", "Fattura Anticipata");

        } else {
            SwingUtils.showErrorMessage(this, "Seleziona un conto corrente!");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void apricontiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apricontiActionPerformed
        if (conto.getText().trim().length() == 0) {
            conto_hints.showHints();
            conto_hints.updateHints(null);
            conto_hints.showHints();
        } else {
            conto_hints.showHints();
        }
    }//GEN-LAST:event_apricontiActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton apriconti;
    private javax.swing.JButton butAnnulla;
    public javax.swing.JTextField conto;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

}
