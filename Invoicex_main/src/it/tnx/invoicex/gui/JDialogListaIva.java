/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui;

import gestioneFatture.frmNuovRigaDescrizioneMultiRigaNew;
import gestioneFatture.main;
import it.tnx.Db;
import java.awt.Dialog;
import java.awt.Frame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author Marco
 */
public class JDialogListaIva extends javax.swing.JDialog {

    private JTextField texCodiceIva = null;
    private JLabel labPercentualeIva = null;
    private JInternalFrame parentFrame = null;

    /**
     * Creates new form JDialogListaIva
     */
    public JDialogListaIva(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }
    
    public JDialogListaIva(Dialog parent, boolean modal, JTextField texCodiceIva, JLabel labPercentualeIva, JInternalFrame parentFrame) {
        super(parent, modal);
        
        this.texCodiceIva = texCodiceIva;
        this.labPercentualeIva = labPercentualeIva;
        this.parentFrame = parentFrame;
        
        initComponents();
        
        //azzero la griglia
        griglia.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {
            { null }
        }, new String[] { "" }));

        //setto le larghezze delle colonne
        java.util.Hashtable colsWidthPerc = new java.util.Hashtable();
        colsWidthPerc.put("codice", new Double(15));
        colsWidthPerc.put("percentuale", new Double(15));
        colsWidthPerc.put("descrizione", new Double(70));
        this.griglia.columnsSizePerc = colsWidthPerc;
        griglia.flagUsaOrdinamento = true;

        String sql = "select codice, percentuale, descrizione";
        sql += " from codici_iva";
        sql += " order by codice";
        griglia.dbOpen(Db.getConn(), sql);
        
        griglia.getSelectionModel().clearSelection();
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
        griglia = new tnxbeans.tnxDbGrid();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Lista codici IVA");

        griglia.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        griglia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grigliaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(griglia);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void grigliaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseClicked

        if (evt.getClickCount() == 2) {
            this.texCodiceIva.setText(this.griglia.getValueAt(griglia.getSelectedRow(), 0).toString());

            String tempPercentuale = this.griglia.getValueAt(griglia.getSelectedRow(), 1)
                    .toString();
            tempPercentuale = tempPercentuale.substring(0, tempPercentuale.length() - 2);

            java.text.DecimalFormat dec = new java.text.DecimalFormat();

            if (labPercentualeIva != null) {
                this.labPercentualeIva.setText(tempPercentuale + "%");
            }

            this.dispose();

            System.err.println("parentFrame = " + parentFrame);
            if (parentFrame != null) {
                parentFrame.toFront();
                main.getPadre().getDesktopPane().getDesktopManager().activateFrame(parentFrame);
                try {
                    ((frmNuovRigaDescrizioneMultiRigaNew) parentFrame).aggiorna_iva();
                    ((frmNuovRigaDescrizioneMultiRigaNew) parentFrame).aggiornaTotale();
                } catch (Exception ex) {
                }
            }
        }
    }//GEN-LAST:event_grigliaMouseClicked

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
            java.util.logging.Logger.getLogger(JDialogListaIva.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JDialogListaIva.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JDialogListaIva.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JDialogListaIva.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JDialogListaIva dialog = new JDialogListaIva(new javax.swing.JFrame(), true);
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
    private tnxbeans.tnxDbGrid griglia;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}