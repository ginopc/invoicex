/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui;

/**
 *
 * @author Marco
 */
public class JDialogInfoLicenza extends javax.swing.JDialog {

    /**
     * Creates new form JDialogInfoLicenza
     */
    public JDialogInfoLicenza(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scadenza = new javax.swing.JLabel();
        icona = new javax.swing.JLabel();
        versione = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Informazioni sulla licenza");
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        scadenza.setFont(scadenza.getFont().deriveFont(scadenza.getFont().getSize()+3f));
        scadenza.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        scadenza.setText("...");
        getContentPane().add(scadenza, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, 535, 50));

        icona.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/base.png"))); // NOI18N
        getContentPane().add(icona, new org.netbeans.lib.awtextra.AbsoluteConstraints(255, 30, -1, -1));

        versione.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        versione.setText("...");
        getContentPane().add(versione, new org.netbeans.lib.awtextra.AbsoluteConstraints(305, 30, 250, 50));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("La tua licenza di Invoicex:");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(4, 30, 240, 50));

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(JDialogInfoLicenza.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JDialogInfoLicenza.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JDialogInfoLicenza.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JDialogInfoLicenza.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JDialogInfoLicenza dialog = new JDialogInfoLicenza(new javax.swing.JFrame(), true);
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
    public javax.swing.JLabel icona;
    private javax.swing.JLabel jLabel2;
    public javax.swing.JLabel scadenza;
    public javax.swing.JLabel versione;
    // End of variables declaration//GEN-END:variables
}
