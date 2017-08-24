/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JDialogImportaReport2.java
 *
 * Created on 18-dic-2009, 10.35.20
 */
package gestioneFatture;

import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.invoicex.InvoicexUtil;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.sql.Types;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Toce Alessio
 */
public class JDialogCambiaPassword extends javax.swing.JDialog {

    /** Creates new form JDialogImportaReport2 */
    String dbPassword = "";

    public JDialogCambiaPassword() {
        super(main.getPadreWindow(), true);
        initComponents();

        this.btnSalvaPassword.setEnabled(false);
        try {
            this.dbPassword = CastUtils.toString(DbUtils.getObject(Db.getConn(), "SELECT password FROM accessi_utenti WHERE id = " + Db.pc(main.utente.getIdUtente(), Types.INTEGER)));
        } catch (Exception e) {
            SwingUtils.showErrorMessage(this, "Errore nella ricerca della password", "Errore");
            dispose();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        btnAnnulla = new javax.swing.JButton();
        btnSalvaPassword = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        texPasswordOld = new javax.swing.JPasswordField();
        texPasswordNew = new javax.swing.JPasswordField();
        texPasswordRepeat = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cambia Password");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Vecchia Password:");

        btnAnnulla.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Delete Sign-16.png"))); // NOI18N
        btnAnnulla.setText("Annulla");
        btnAnnulla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnnullaActionPerformed(evt);
            }
        });

        btnSalvaPassword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Checkmark-16.png"))); // NOI18N
        btnSalvaPassword.setText("Salva");
        btnSalvaPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvaPasswordActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Nuova Password:");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Ripeti Password:");

        texPasswordOld.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                texPasswordOldFocusGained(evt);
            }
        });
        texPasswordOld.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texPasswordOldKeyReleased(evt);
            }
        });

        texPasswordNew.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                texPasswordNewFocusGained(evt);
            }
        });
        texPasswordNew.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texPasswordNewKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texPasswordNewKeyReleased(evt);
            }
        });

        texPasswordRepeat.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                texPasswordRepeatFocusGained(evt);
            }
        });
        texPasswordRepeat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texPasswordRepeatKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texPasswordRepeatKeyReleased(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texPasswordOld, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texPasswordNew, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texPasswordRepeat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)))
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(153, Short.MAX_VALUE)
                .add(btnAnnulla)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnSalvaPassword)
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jLabel1, jLabel2, jLabel3}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(texPasswordOld, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(texPasswordNew, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(texPasswordRepeat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnSalvaPassword)
                    .add(btnAnnulla))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalvaPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvaPasswordActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String password = InvoicexUtil.md5(new String(texPasswordNew.getPassword()));
        Integer id = main.utente.getIdUtente();

        String sql = "UPDATE accessi_utenti SET password = " + Db.pc(password, Types.VARCHAR) + " WHERE id = " + Db.pc(id, Types.INTEGER);

        try {
            Db.executeSql(sql);
            SwingUtils.showInfoMessage(this, "Password cambiata", "Cambio Password");
            dispose();
        } catch (Exception e) {
            SwingUtils.showErrorMessage(this, "Impossibile cambiare la password", "Cambio Password");
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_btnSalvaPasswordActionPerformed

    private void btnAnnullaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnnullaActionPerformed
        dispose();
    }//GEN-LAST:event_btnAnnullaActionPerformed

    private void texPasswordRepeatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texPasswordRepeatKeyReleased
        abilitaSalva();
        if(btnSalvaPassword.isEnabled() && evt.getKeyCode() == KeyEvent.VK_ENTER) this.btnSalvaPasswordActionPerformed(null);
    }//GEN-LAST:event_texPasswordRepeatKeyReleased

    private void texPasswordNewKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texPasswordNewKeyReleased
        abilitaSalva();
        if(btnSalvaPassword.isEnabled() && evt.getKeyCode() == KeyEvent.VK_ENTER) this.btnSalvaPasswordActionPerformed(null);
    }//GEN-LAST:event_texPasswordNewKeyReleased

    private void texPasswordOldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texPasswordOldKeyReleased
        abilitaSalva();
        if(btnSalvaPassword.isEnabled() && evt.getKeyCode() == KeyEvent.VK_ENTER) this.btnSalvaPasswordActionPerformed(null);
    }//GEN-LAST:event_texPasswordOldKeyReleased

    private void texPasswordOldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texPasswordOldFocusGained
        ((JTextComponent) this.texPasswordOld).selectAll();
    }//GEN-LAST:event_texPasswordOldFocusGained

    private void texPasswordNewFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texPasswordNewFocusGained
        ((JTextComponent) this.texPasswordNew).selectAll();
    }//GEN-LAST:event_texPasswordNewFocusGained

    private void texPasswordRepeatFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texPasswordRepeatFocusGained
        ((JTextComponent) this.texPasswordRepeat).selectAll();
    }//GEN-LAST:event_texPasswordRepeatFocusGained

    private void texPasswordRepeatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texPasswordRepeatKeyPressed

    }//GEN-LAST:event_texPasswordRepeatKeyPressed

    private void texPasswordNewKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texPasswordNewKeyPressed
        
    }//GEN-LAST:event_texPasswordNewKeyPressed

    private void abilitaSalva() {
        String oldPassword = InvoicexUtil.md5(new String(this.texPasswordOld.getPassword()));
        String newPassword = new String(this.texPasswordNew.getPassword());
        String repPassword = new String(this.texPasswordRepeat.getPassword());

        this.btnSalvaPassword.setEnabled(oldPassword.equals(this.dbPassword) && newPassword.equals(repPassword));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAnnulla;
    private javax.swing.JButton btnSalvaPassword;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPasswordField texPasswordNew;
    private javax.swing.JPasswordField texPasswordOld;
    private javax.swing.JPasswordField texPasswordRepeat;
    // End of variables declaration//GEN-END:variables
}