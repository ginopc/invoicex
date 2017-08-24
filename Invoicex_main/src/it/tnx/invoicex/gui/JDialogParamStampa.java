/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui;

import gestioneFatture.main;
import it.tnx.commons.cu;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

/**
 *
 * @author Marco
 */
public class JDialogParamStampa extends javax.swing.JDialog {
    public boolean confermato = false;
    String nomeStampa = null;
    
    /**
     * Creates new form JDialogParamStampa
     */
    public JDialogParamStampa(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        ((JSpinner.NumberEditor) carattere.getEditor()).getTextField().setFont(carattere.getFont().deriveFont(carattere.getFont().getSize()+2f));
        JRootPane rootPane = SwingUtilities.getRootPane(conferma); 
        rootPane.setDefaultButton(conferma);
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        verticale = new javax.swing.JToggleButton();
        orizzontale = new javax.swing.JToggleButton();
        carattere = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        conferma = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Formato");

        buttonGroup1.add(verticale);
        verticale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/print-vertical-22.png"))); // NOI18N
        verticale.setText("Verticale");
        verticale.setToolTipText("");
        verticale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verticaleActionPerformed(evt);
            }
        });

        buttonGroup1.add(orizzontale);
        orizzontale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/print-landscape-22.png"))); // NOI18N
        orizzontale.setText("Orizzontale");
        orizzontale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orizzontaleActionPerformed(evt);
            }
        });

        carattere.setFont(carattere.getFont().deriveFont(carattere.getFont().getSize()+4f));
        carattere.setModel(new javax.swing.SpinnerNumberModel(0, -5, 10, 1));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Dimensione carattere");

        conferma.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Checkmark-16.png"))); // NOI18N
        conferma.setText("Prosegui");
        conferma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confermaActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Delete Sign-16.png"))); // NOI18N
        jButton2.setText("Annulla");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(carattere, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(verticale)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(orizzontale)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jButton2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(conferma)
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jLabel1, jLabel2}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {orizzontale, verticale}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(verticale)
                    .add(orizzontale))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(carattere, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 30, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(conferma)
                    .add(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void orizzontaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orizzontaleActionPerformed
        
    }//GEN-LAST:event_orizzontaleActionPerformed

    private void verticaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verticaleActionPerformed
        
    }//GEN-LAST:event_verticaleActionPerformed

    private void confermaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confermaActionPerformed
        salvaParam();
        confermato = true;
        setVisible(false);
    }//GEN-LAST:event_confermaActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JDialogParamStampa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JDialogParamStampa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JDialogParamStampa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JDialogParamStampa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JDialogParamStampa dialog = new JDialogParamStampa(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JSpinner carattere;
    private javax.swing.JButton conferma;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JToggleButton orizzontale;
    private javax.swing.JToggleButton verticale;
    // End of variables declaration//GEN-END:variables

    public void init(String nomeStampa) {
        this.nomeStampa = nomeStampa;
        String formato = main.fileIni.getValue("pref", "param_stampa_" + nomeStampa + "_formato", "verticale");
        if (cu.s(formato).equals("verticale")) {
            verticale.setSelected(true);
            verticale.requestFocus();
        } else {
            orizzontale.setSelected(true);
            orizzontale.requestFocus();
        }
        Integer ncar = cu.i0(main.fileIni.getValue("pref", "param_stampa_" + nomeStampa + "_carattere", "0"));
        carattere.setValue(ncar);
    }

    private void salvaParam() {
        if (verticale.isSelected()) {
            main.fileIni.setValue("pref",  "param_stampa_" + nomeStampa + "_formato", "verticale");
        }else {
            main.fileIni.setValue("pref",  "param_stampa_" + nomeStampa + "_formato", "orizzontale");
        }
        main.fileIni.setValue("pref",  "param_stampa_" + nomeStampa + "_carattere", carattere.getValue());
    }
    
}
