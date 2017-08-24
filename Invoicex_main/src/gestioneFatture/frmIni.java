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

public class frmIni
    extends javax.swing.JFrame {

    /** Creates new form frmIni */
    public frmIni(iniFile ini) {
        initComponents();

        if (ini != null) {
            this.treIni.setModel(ini.getTreeModel());
        }
    }

    /** This method is called from within the constructor to

     * initialize the form.

     * WARNING: Do NOT modify this code. The content of this method is

     * always regenerated by the Form Editor.

     */
    private void initComponents() { //GEN-BEGIN:initComponents
        jScrollPane1 = new javax.swing.JScrollPane();
        treIni = new javax.swing.JTree();
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        jScrollPane1.setViewportView(treIni);
        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
        pack();
    } //GEN-END:initComponents

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) { //GEN-FIRST:event_exitForm
        System.exit(0);
    } //GEN-LAST:event_exitForm

    /**

    * @param args the command line arguments

    */
    public static void main(String[] args) {
        new frmIni(null).show();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree treIni;

    // End of variables declaration//GEN-END:variables
}
