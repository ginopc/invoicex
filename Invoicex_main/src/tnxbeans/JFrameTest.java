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

package tnxbeans;

/**
 *
 * @author  Lorenzo
 */
public class JFrameTest extends javax.swing.JFrame {
    
    /** Creates new form JFrameTest */
    public JFrameTest() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        tnxDbPanel1 = new tnxbeans.tnxDbPanel();
        tnxCheckBox1 = new tnxbeans.tnxCheckBox();
        tnxComboField1 = new tnxbeans.tnxComboField();
        jScrollPane1 = new javax.swing.JScrollPane();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        tnxDbPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tnxCheckBox1.setText("tnxCheckBox1");
        tnxDbPanel1.add(tnxCheckBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(83, 5, -1, -1));

        tnxDbPanel1.add(tnxComboField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(196, 5, -1, -1));

        tnxDbPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 70, 170, 160));

        getContentPane().add(tnxDbPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new JFrameTest().show();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private tnxbeans.tnxCheckBox tnxCheckBox1;
    private tnxbeans.tnxComboField tnxComboField1;
    private tnxbeans.tnxDbPanel tnxDbPanel1;
    // End of variables declaration//GEN-END:variables
    
}