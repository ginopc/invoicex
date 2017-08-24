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



package gestioneFatture;

import it.tnx.Db;
import java.awt.*;

import javax.swing.*;

public class frmListIva
    extends javax.swing.JInternalFrame {

    private JTextField texCodiceIva = null;
    private JLabel labPercentualeIva = null;
    private JInternalFrame parentFrame = null;

    /** Creates new form frmListCoorBanc */
    public frmListIva(JTextField texCodiceIva, JLabel labPercentualeIva, JInternalFrame parentFrame) {
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

        
    }

    /** This method is called from within the constructor to

   * initialize the form.

   * WARNING: Do NOT modify this code. The content of this method is

   * always regenerated by the Form Editor.

   */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        griglia = new tnxbeans.tnxDbGrid();
        jPanel3 = new javax.swing.JPanel();
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Lista codici IVA");
        jPanel1.setLayout(new java.awt.BorderLayout());
        griglia.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {
            { null, null, null, null }, { null, null, null, null }, 
            { null, null, null, null }, { null, null, null, null }
        }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        griglia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grigliaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(griglia);
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jPanel1.add(jPanel3, java.awt.BorderLayout.SOUTH);
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
        pack();
    }//GEN-END:initComponents

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
                    ((frmNuovRigaDescrizioneMultiRigaNew)parentFrame).aggiorna_iva();
                    ((frmNuovRigaDescrizioneMultiRigaNew)parentFrame).aggiornaTotale();
                } catch (Exception ex) {
                }
            }
        }
    }//GEN-LAST:event_grigliaMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private tnxbeans.tnxDbGrid griglia;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;

    // End of variables declaration//GEN-END:variables
}