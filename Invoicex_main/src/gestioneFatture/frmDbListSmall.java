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
import it.tnx.commons.cu;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JTextField;

public class frmDbListSmall
    extends javax.swing.JDialog {

    private String sql;
    private Object textOut;
    private int colonna;

    /** Creates new form frmDbListSmall */
    public frmDbListSmall(java.awt.Frame parent, boolean modal, String sql, Object textOut, int colonna, java.util.Hashtable columns, int left, int top, int width, int height) {
        super(parent, modal);
        initComponents();
        this.setBounds(left, top, width, height);
        this.sql = sql;
        this.griglia.columnsSizePerc = columns;
        this.griglia.dbOpen(Db.getConn(), sql, Db.INSTANCE, true);
        this.textOut = textOut;
        this.colonna = colonna;
        this.show();
    }

    /** This method is called from within the constructor to

   * initialize the form.

   * WARNING: Do NOT modify this code. The content of this method is

   * always regenerated by the Form Editor.

   */
    private void initComponents() {//GEN-BEGIN:initComponents
        jScrollPane1 = new javax.swing.JScrollPane();
        griglia = new tnxbeans.tnxDbGrid();
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        griglia.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {
            { null, null, null, null }, { null, null, null, null }, 
            { null, null, null, null }, { null, null, null, null }
        }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        griglia.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                grigliaKeyPressed(evt);
            }
        });
        griglia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grigliaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(griglia);
        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
        pack();
    }//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
    }//GEN-LAST:event_formKeyPressed

    private void grigliaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_grigliaKeyPressed

        if (evt.getKeyCode() == evt.VK_ENTER) {
            select();
        } else if (evt.getKeyCode() == evt.VK_ESCAPE) {
            if (textOut instanceof JTextField) {
                ((JTextField)textOut).setText("");
            } else if (textOut instanceof StringBuffer) {
                ((StringBuffer)textOut).setLength(0);
            } else if (textOut instanceof AtomicReference) {
                ((AtomicReference<ArticoloHint>)textOut).set(null);
            }
            exitForm(null);
        }
    }//GEN-LAST:event_grigliaKeyPressed

    private void grigliaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseClicked

        if (evt.getClickCount() == 2) {
            select();
        }
    }//GEN-LAST:event_grigliaMouseClicked

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm

        //setVisible(false);
        dispose();
    }//GEN-LAST:event_exitForm

    /**

   * @param args the command line arguments

   */
    public static void main(String[] args) {

        //new frmDbListSmall(null, null, 0).show();
    }

    private void select() {
        String value = cu.s(griglia.getValueAt(this.griglia.getSelectedRow(), this.colonna));
        if (textOut instanceof JTextField) {
            ((JTextField)textOut).setText(value);
        } else if (textOut instanceof StringBuffer) {
            ((StringBuffer)textOut).setLength(0);
            ((StringBuffer)textOut).append(value);
        } else if (textOut instanceof AtomicReference) {
            ((AtomicReference<ArticoloHint>)textOut).set(new ArticoloHint(value, ""));
        }        
        exitForm(null);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private tnxbeans.tnxDbGrid griglia;

    // End of variables declaration//GEN-END:variables
}