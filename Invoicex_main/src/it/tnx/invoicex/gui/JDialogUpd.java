/**
 * Invoicex
 * Copyright (c) 2005,2006,2007,2008,2009 Marco Ceccarelli, Tnx snc
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

package it.tnx.invoicex.gui;

import gestioneFatture.main;
import it.tnx.commons.SwingUtils;
import java.net.URL;
import org.jdesktop.jdic.desktop.Desktop;

/**
 *
 * @author mceccarelli
 */
public class JDialogUpd extends javax.swing.JDialog {
    String v = null;

    /** Creates new form JDialogUpd */
    public JDialogUpd(java.awt.Frame parent, boolean modal, String v) {
        super(parent, modal);
        this.v = v;
        initComponents();
        try {
            String vnr = main.getURL(main.baseurlserver + "/vnr.php");
            String msg = "<html>E' disponibile l'aggiornamento <b>" + v + "</b> contenente le seguenti modifiche:<br>" + vnr +
                    "<br><b>Clicca su 'Vai al sito' per acquistare Invoicex con gli aggiornamenti automatici</b><br>oppure scarica manualmente il nuovo Setup della versione Base</html>";
            jLabel2.setText(msg);
        } catch (Exception ex) {
            ex.printStackTrace();
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

        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Aggiornamenti");
        addWindowListener(formListener);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setText("...");
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        getContentPane().add(jLabel2, java.awt.BorderLayout.CENTER);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/apps/internet-web-browser.png"))); // NOI18N
        jButton2.setText("Vai al sito");
        jButton2.addActionListener(formListener);

        jButton3.setText("Chiudi messaggio");
        jButton3.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jButton3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton2)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(1, 1, 1)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton2)
                    .add(jButton3))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.WindowListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == jButton2) {
                JDialogUpd.this.jButton2ActionPerformed(evt);
            }
            else if (evt.getSource() == jButton3) {
                JDialogUpd.this.jButton3ActionPerformed(evt);
            }
        }

        public void windowActivated(java.awt.event.WindowEvent evt) {
        }

        public void windowClosed(java.awt.event.WindowEvent evt) {
            if (evt.getSource() == JDialogUpd.this) {
                JDialogUpd.this.formWindowClosed(evt);
            }
        }

        public void windowClosing(java.awt.event.WindowEvent evt) {
        }

        public void windowDeactivated(java.awt.event.WindowEvent evt) {
        }

        public void windowDeiconified(java.awt.event.WindowEvent evt) {
        }

        public void windowIconified(java.awt.event.WindowEvent evt) {
        }

        public void windowOpened(java.awt.event.WindowEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
//            SwingUtils.openUrl(new URL("http://www.tnx.it/index.php?action=acquista&ref=invoicex&ref2=3&pluginAutoUpdate=1&i=" + main.attivazione.getIdRegistrazione()));
            SwingUtils.openUrl(new URL("http://www.invoicex.it/Acquista-il-programma/?ref2=3&i=" + main.attivazione.getIdRegistrazione()));
        } catch (Exception ex) {
            SwingUtils.showErrorMessage(this, ex.toString());
        }
        dispose();
}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
//        if (jCheckBox1.isSelected()) {
//            main.fileIni.setValue("pref", "msg_plugins_upd", false);
//        }
        dispose();
}//GEN-LAST:event_jButton3ActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        System.out.println("window closed");
        main.fileIni.setValue("pref", "msg_plugins_upd_v_" + v, true);
    }//GEN-LAST:event_formWindowClosed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton jButton2;
    public javax.swing.JButton jButton3;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables

}
