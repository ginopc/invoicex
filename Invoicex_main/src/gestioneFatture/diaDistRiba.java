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
import java.text.*;
import java.util.*;

public class diaDistRiba
    extends javax.swing.JDialog {

    public boolean cancel = false;
    public CoordinateBancarie coord;
    public boolean prova = true;
    public String data_distinta = Db.getCurrDateTimeMysql();

    /** Creates new form diaDistRiba */
    public diaDistRiba(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        //metto la data di oggi come default
        java.text.SimpleDateFormat formattazione_init = new java.text.SimpleDateFormat("dd/MM/yy");
        this.texDataDistinta.setText(formattazione_init.format(new java.util.Date()));

        //carico la griglia dei conti
        griBanc.dbNomeTabella = "";
        griBanc.flagUsaThread = false;

        java.util.Hashtable colsWidthPerc = new java.util.Hashtable();
        colsWidthPerc.put("abi", new Double(8));
        colsWidthPerc.put("nome", new Double(15));
        colsWidthPerc.put("cab", new Double(8));
        colsWidthPerc.put("comune", new Double(15));
        colsWidthPerc.put("indirizzo", new Double(20));
        colsWidthPerc.put("cc", new Double(15));
        colsWidthPerc.put("note", new Double(15));
        griBanc.columnsSizePerc = colsWidthPerc;

        String sql = "select";
        sql += " dati_azienda_banche.abi";
        sql += " ,banche_abi.nome";
        sql += " ,dati_azienda_banche.cab";
        sql += " ,comuni.comune";
        sql += " ,banche_cab.indirizzo";
        sql += " ,dati_azienda_banche.cc";
        sql += " ,dati_azienda_banche.note";
        sql += " from dati_azienda_banche left join banche_abi on dati_azienda_banche.abi = banche_abi.abi";
        sql += " left join banche_cab on dati_azienda_banche.cab = banche_cab.cab and dati_azienda_banche.abi = banche_cab.abi";
        sql += " left join comuni on banche_cab.codice_comune = comuni.codice";
        this.griBanc.dbOpen(Db.getConn(), sql);
    }

    /** This method is called from within the constructor to

   * initialize the form.

   * WARNING: Do NOT modify this code. The content of this method is

   * always regenerated by the Form Editor.

   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        radDefi = new javax.swing.JRadioButton();
        radProv = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        butStam = new javax.swing.JButton();
        butAnnu = new javax.swing.JButton();
        texDataDistinta = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        griBanc = new tnxbeans.tnxDbGrid();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        buttonGroup1.add(radDefi);
        radDefi.setText("Stampa DEFINITIVA");
        radDefi.setToolTipText("Stampa le scadenze e le marca come già elaborate (non vengono più ristampate)");

        buttonGroup1.add(radProv);
        radProv.setSelected(true);
        radProv.setText("Stampa in prova");
        radProv.setToolTipText("Stampa le scadenze ma non le marca e possono essere ristampate");

        jLabel1.setText("Seleziona il conto di accredito");

        jLabel2.setText("Seleziona la modalità di stampa");

        butStam.setText("Stampa");
        butStam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butStamActionPerformed(evt);
            }
        });

        butAnnu.setText("Annulla");
        butAnnu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAnnuActionPerformed(evt);
            }
        });

        texDataDistinta.setMinimumSize(new java.awt.Dimension(100, 20));
        texDataDistinta.setPreferredSize(new java.awt.Dimension(100, 20));

        jLabel3.setText("Data Distinta");

        griBanc.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(griBanc);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(butAnnu)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(butStam))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texDataDistinta, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(radProv)
                            .add(radDefi))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .add(8, 8, 8)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(radDefi)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(radProv))
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(texDataDistinta, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(butStam)
                    .add(butAnnu))
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void butAnnuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAnnuActionPerformed
        this.cancel = true;
        this.hide();
    }//GEN-LAST:event_butAnnuActionPerformed

    private void butStamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butStamActionPerformed

        //preparo le coords
        this.coord = new CoordinateBancarie();

        String temp;
        temp = String.valueOf(griBanc.getValueAt(griBanc.getSelectedRow(), 0));
        this.coord.setAbi(temp);
        temp = String.valueOf(griBanc.getValueAt(griBanc.getSelectedRow(), 2));
        this.coord.setCab(temp);
        temp = String.valueOf(griBanc.getValueAt(griBanc.getSelectedRow(), 5));
        this.coord.setCc(temp);

        //prova o definitiva
        if (this.radDefi.isSelected() == true) {
            this.prova = false;
        }

        try {

            DateFormat parsaData = new SimpleDateFormat("dd/MM/yy");
            Date data_inserita = parsaData.parse(this.texDataDistinta.getText());
            java.text.SimpleDateFormat riformattazione = new java.text.SimpleDateFormat("yyyy-MM-dd");
            this.data_distinta = riformattazione.format(data_inserita);
        } catch (Exception err) {
            err.printStackTrace();
        }

        this.hide();
    }//GEN-LAST:event_butStamActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    /**

   * @param args the command line arguments

   */
    public static void main(String[] args) {
        new diaDistRiba(new javax.swing.JFrame(), true).show();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butAnnu;
    private javax.swing.JButton butStam;
    private javax.swing.ButtonGroup buttonGroup1;
    private tnxbeans.tnxDbGrid griBanc;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton radDefi;
    private javax.swing.JRadioButton radProv;
    private javax.swing.JTextField texDataDistinta;
    // End of variables declaration//GEN-END:variables
}