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



/*

 * frmDati_blank.java

 *

 * Created on 31 dicembre 2001, 16.43

 */
package gestioneFatture.logic.provvigioni;

import it.tnx.Db;
import gestioneFatture.*;
import it.tnx.accessoUtenti.Permesso;
import it.tnx.commons.cu;
import it.tnx.invoicex.InvoicexUtil;

import java.sql.*;
import java.sql.ResultSet;
import java.util.Map;


import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.text.*;

/**

 *

 * @author  Administrator

 */
public class frmDettaglioProvvigione
    extends javax.swing.JInternalFrame {

    String sql;
    ProvvigioniFattura provvigioni;
    java.text.DecimalFormat df = new java.text.DecimalFormat(".00");
    double totaleDocumento;
    java.util.Timer timTotali;

    /** Creates new form frmDati_blank */
    public frmDettaglioProvvigione(ProvvigioniFattura provvigioni) {
        this.provvigioni = provvigioni;

        try {
            mask = new MaskFormatter("##/##/##");
        } catch (Exception err) {
            err.printStackTrace();
        }

        initComponents();

        //associo il panel ai dati
        this.dati.dbNomeTabella = "provvigioni";

        Vector chiave = new Vector();
        chiave.add("id");

        //chiave.add("documento_tipo");
        //chiave.add("documento_serie");
        //chiave.add("documento_numero");
        //chiave.add("documento_anno");
        //chiave.add("data_scadenza");
        this.dati.dbChiave = chiave;
        this.dati.butSave = this.butSave;
        this.dati.butSaveClose = this.butSave1;
        this.dati.butUndo = this.butUndo;
        this.dati.butFind = this.butFind;
        this.dati.butNew  = this.butNew;
        this.dati.butDele = this.butDele;
        this.dati.tipo_permesso = Permesso.PERMESSO_AGENTI;
        
        sql = "select * from provvigioni";
        sql += " where id_doc = " + Db.pc(provvigioni.documento_id, Types.VARCHAR);
        sql += " order by id";
        this.dati.dbOpen(Db.getConn(), sql);
        this.dati.dbRefresh();

        //apro la griglia
        //this.griglia.dbEditabile = true;
        java.util.Hashtable colsWidthPerc = new java.util.Hashtable();
        colsWidthPerc.put("documento_serie", new Double(0));
        colsWidthPerc.put("documento_anno", new Double(0));
        colsWidthPerc.put("documento_numero", new Double(0));
        colsWidthPerc.put("documento_tipo", new Double(0));
        colsWidthPerc.put("data_scadenza", new Double(0));
        colsWidthPerc.put("id", new Double(5));
        colsWidthPerc.put("numero", new Double(5));
        colsWidthPerc.put("Data", new Double(20));
        colsWidthPerc.put("Importo", new Double(20));
        colsWidthPerc.put("Pagata", new Double(7));
        colsWidthPerc.put("id_doc", new Double(0));
        this.griglia.columnsSizePerc = colsWidthPerc;

        java.util.Hashtable colsAlign = new java.util.Hashtable();
        colsAlign.put("Importo", "RIGHT_CURRENCY");
        this.griglia.columnsAlign = colsAlign;
        this.griglia.dbChiave = chiave;
        this.griglia.flagUsaThread = false;
        sql = "select documento_serie,documento_anno,documento_numero,documento_tipo,data_scadenza,id,numero, data_scadenza as Data, importo_provvigione as Importo, pagata as Pagata, id_doc from provvigioni";
        sql += " where id_doc = " + provvigioni.documento_id;
        sql += " order by id";
        this.griglia.dbOpen(Db.getConn(), sql);
        this.griglia.dbPanel = this.dati;
        
        //se mi arriva il p_id cerco la provvigione selezionata
        if (provvigioni != null && provvigioni.p_id != null) {
            try {
                boolean trovato = false;
                for (int i = 0; i < griglia.getRowCount(); i++) {
                    Integer p_id_griglia = cu.toInteger(griglia.getValueAt(i, griglia.getColumnByName("id")));
                    if (p_id_griglia == provvigioni.p_id) {
                        griglia.getSelectionModel().setSelectionInterval(i, i);
                        griglia.dbSelezionaRiga();
                        trovato = true;
                    }
                }
                if (!trovato) {
                    griglia.getSelectionModel().clearSelection();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        //sistemo alcune cose
        //this.comTipo.addItem("CL");
        //this.comTipo.addItem("FO");
        this.dati.dbRefresh();

        if (dati.isOnSomeRecord == false) {
            javax.swing.JOptionPane.showMessageDialog(this, "Non ci sono provvigioni caricate per questo documento, prosegui inserendone una", "Attenzione", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            butNewActionPerformed(null);
        }

        Map m = null;
        try {
            m = InvoicexUtil.getSerieNumeroAnno(Db.TIPO_DOCUMENTO_FATTURA, provvigioni.documento_id);
        } catch (Exception ex) {
            Logger.getLogger(frmDettaglioProvvigione.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.labDocu.setText("Provvigioni del documento: " + m.get("serie") + m.get("numero"));

        //creo timer per aggiornare totale provvigioni
        timTotali = new java.util.Timer();

        timRefreshTotali timRefresh = new timRefreshTotali(this, this.griglia, this.labTotaleProvvigioni, totaleDocumento);
        timTotali.schedule(timRefresh, 1000, 500);
        
        texDataScad.setEditable(false);
        texDataScad.setEnabled(false);

    }

    /** This method is called from within the constructor to

   * initialize the form.

   * WARNING: Do NOT modify this code. The content of this method is

   * always regenerated by the Form Editor.

   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panAlto = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        butNew = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        butDele = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        butFind = new javax.swing.JButton();
        jLabel131 = new javax.swing.JLabel();
        butFirs = new javax.swing.JButton();
        butPrev = new javax.swing.JButton();
        butNext = new javax.swing.JButton();
        butLast = new javax.swing.JButton();
        tabCent = new javax.swing.JTabbedPane();
        panDati = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dati = new tnxbeans.tnxDbPanel();
        texId = new tnxbeans.tnxTextField();
        jLabel2211 = new javax.swing.JLabel();
        jLabel21111 = new javax.swing.JLabel();
        labDocu = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        griglia = new tnxbeans.tnxDbGrid();
        jLabel3 = new javax.swing.JLabel();
        jLabel22111 = new javax.swing.JLabel();
        texNume = new tnxbeans.tnxTextField();
        tnxCheckBox1 = new tnxbeans.tnxCheckBox();
        texDataScad = new tnxbeans.tnxTextFieldFormatted(mask);
        texDocuId = new tnxbeans.tnxTextField();
        texDocuId.setVisible(false);
        jLabel22112 = new javax.swing.JLabel();
        texImpo = new tnxbeans.tnxTextField();
        labTotaleProvvigioni = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        butUndo = new javax.swing.JButton();
        butSave = new javax.swing.JButton();
        butSave1 = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Provvigioni");
        setMinimumSize(new java.awt.Dimension(550, 34));
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameOpened(evt);
            }
        });

        panAlto.setLayout(new java.awt.BorderLayout());

        butNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butNew.setText("Nuovo");
        butNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNewActionPerformed(evt);
            }
        });
        jToolBar1.add(butNew);

        jLabel1.setText(" ");
        jToolBar1.add(jLabel1);

        butDele.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/places/user-trash.png"))); // NOI18N
        butDele.setText("Elimina");
        butDele.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDeleActionPerformed(evt);
            }
        });
        jToolBar1.add(butDele);

        jLabel11.setText(" ");
        jToolBar1.add(jLabel11);

        jLabel12.setText(" ");
        jToolBar1.add(jLabel12);

        jLabel13.setText(" ");
        jToolBar1.add(jLabel13);

        butFind.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-find.png"))); // NOI18N
        butFind.setText("Trova");
        butFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butFindActionPerformed(evt);
            }
        });
        jToolBar1.add(butFind);

        jLabel131.setText(" ");
        jToolBar1.add(jLabel131);

        butFirs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/go-first.png"))); // NOI18N
        butFirs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butFirsActionPerformed(evt);
            }
        });
        jToolBar1.add(butFirs);

        butPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/go-previous.png"))); // NOI18N
        butPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPrevActionPerformed(evt);
            }
        });
        jToolBar1.add(butPrev);

        butNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/go-next.png"))); // NOI18N
        butNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNextActionPerformed(evt);
            }
        });
        jToolBar1.add(butNext);

        butLast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/go-last.png"))); // NOI18N
        butLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butLastActionPerformed(evt);
            }
        });
        jToolBar1.add(butLast);

        panAlto.add(jToolBar1, java.awt.BorderLayout.CENTER);

        getContentPane().add(panAlto, java.awt.BorderLayout.NORTH);

        tabCent.setName("dati"); // NOI18N

        panDati.setName("dati"); // NOI18N
        panDati.setLayout(new java.awt.BorderLayout());

        texId.setEditable(false);
        texId.setText("id");
        texId.setDbNomeCampo("id");

        jLabel2211.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2211.setText("data scadenza");

        jLabel21111.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21111.setText("id");

        labDocu.setForeground(new java.awt.Color(0, 0, 255));
        labDocu.setText("...");

        jScrollPane1.setToolTipText("cliccando su una riga si ceglie la destinazione da moficare");

        griglia.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                grigliaComponentResized(evt);
            }
        });
        jScrollPane1.setViewportView(griglia);

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel3.setText("Elenco provvigioni");

        jLabel22111.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22111.setText("numero");

        texNume.setColumns(5);
        texNume.setDbNomeCampo("numero");
        texNume.setDbTipoCampo("NUMERICO");

        tnxCheckBox1.setText("Pagata ?");
        tnxCheckBox1.setDbDescCampo("");
        tnxCheckBox1.setDbNomeCampo("pagata");
        tnxCheckBox1.setDbTipoCampo("");
        tnxCheckBox1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        texDataScad.setColumns(10);
        texDataScad.setDbNomeCampo("data_scadenza");
        texDataScad.setDbTipoCampo("data");

        texDocuId.setBackground(new java.awt.Color(255, 204, 204));
        texDocuId.setDbNomeCampo("id_doc");
        texDocuId.setDbTipoCampo("LONG");

        jLabel22112.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22112.setText("importo");

        texImpo.setColumns(10);
        texImpo.setDbNomeCampo("importo_provvigione");
        texImpo.setDbTipoCampo("VALUTA");

        labTotaleProvvigioni.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labTotaleProvvigioni.setText("Totale provvigioni");

        org.jdesktop.layout.GroupLayout datiLayout = new org.jdesktop.layout.GroupLayout(dati);
        dati.setLayout(datiLayout);
        datiLayout.setHorizontalGroup(
            datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiLayout.createSequentialGroup()
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, datiLayout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(labTotaleProvvigioni, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 280, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(datiLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1)
                            .add(datiLayout.createSequentialGroup()
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(datiLayout.createSequentialGroup()
                                        .add(jLabel22111)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texNume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(datiLayout.createSequentialGroup()
                                        .add(jLabel2211)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texDataScad, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, datiLayout.createSequentialGroup()
                                        .add(jLabel21111)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, texDocuId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                    .add(datiLayout.createSequentialGroup()
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(datiLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabel3))
                            .add(datiLayout.createSequentialGroup()
                                .add(5, 5, 5)
                                .add(labDocu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 375, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .add(datiLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel22112)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(texImpo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(tnxCheckBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        datiLayout.linkSize(new java.awt.Component[] {jLabel2211, jLabel22111, jLabel22112}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.setVerticalGroup(
            datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiLayout.createSequentialGroup()
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(datiLayout.createSequentialGroup()
                        .add(5, 5, 5)
                        .add(labDocu)
                        .add(6, 6, 6)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(texNume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel22111))
                        .add(5, 5, 5)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(texDataScad, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2211))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(texImpo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel22112)
                            .add(tnxCheckBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(datiLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(texId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel21111))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texDocuId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(5, 5, 5)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labTotaleProvvigioni, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jScrollPane2.setViewportView(dati);

        panDati.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        tabCent.addTab("dati", panDati);

        getContentPane().add(tabCent, java.awt.BorderLayout.CENTER);

        butUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Delete Sign-16.png"))); // NOI18N
        butUndo.setText("Annulla");
        butUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butUndoActionPerformed(evt);
            }
        });
        jPanel2.add(butUndo);

        butSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Checkmark-16.png"))); // NOI18N
        butSave.setText("Salva");
        butSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSaveActionPerformed(evt);
            }
        });
        jPanel2.add(butSave);

        butSave1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Checkmark-16.png"))); // NOI18N
        butSave1.setText("Salva e Chiudi");
        butSave1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSave1ActionPerformed(evt);
            }
        });
        jPanel2.add(butSave1);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void grigliaComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_grigliaComponentResized

        // Add your handling code here:
        this.griglia.resizeColumnsPerc(true);
    }//GEN-LAST:event_grigliaComponentResized

    private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameOpened

        // Add your handling code here:
        this.griglia.resizeColumnsPerc(true);
    }//GEN-LAST:event_formInternalFrameOpened

    private void butLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butLastActionPerformed

        // Add your handling code here:
        this.griglia.dbGoLast();
    }//GEN-LAST:event_butLastActionPerformed

    private void butNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNextActionPerformed

        // Add your handling code here:
        this.griglia.dbGoNext();
    }//GEN-LAST:event_butNextActionPerformed

    private void butPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPrevActionPerformed

        // Add your handling code here:
        this.griglia.dbGoPrevious();
    }//GEN-LAST:event_butPrevActionPerformed

    private void butFirsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butFirsActionPerformed

        // Add your handling code here:
        this.griglia.dbGoFirst();
    }//GEN-LAST:event_butFirsActionPerformed

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        timTotali.cancel();
        main.getPadre().closeFrame(this);
        this.dispose();
    }//GEN-LAST:event_formInternalFrameClosing

    private void butDeleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDeleActionPerformed

        int ret = JOptionPane.showConfirmDialog(this, "Sicuro di eliminare ?", "Attenzione", JOptionPane.YES_NO_OPTION);

        if (ret == JOptionPane.YES_OPTION) {
            this.dati.dbDelete();
            this.griglia.dbRefresh();
        }
    }//GEN-LAST:event_butDeleActionPerformed

    private void butFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butFindActionPerformed

        boolean ret = this.griglia.dbFindNext();

        if (ret == false) {

            int ret2 = JOptionPane.showConfirmDialog(this, "Posizione non trovata\nVuoi riprovare dall'inizio ?", "Attenzione", JOptionPane.YES_NO_OPTION);

            //JOptionPane.showMessageDialog(this,"?-:"+String.valueOf(i));
            if (ret2 == JOptionPane.OK_OPTION) {

                boolean ret3 = this.griglia.dbFindFirst();
            }
        }
    }//GEN-LAST:event_butFindActionPerformed

    private void butUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butUndoActionPerformed
        dati.dbUndo();
    }//GEN-LAST:event_butUndoActionPerformed

    private void butSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSaveActionPerformed
        this.dati.dbSave();
        this.griglia.dbRefresh();
        //cerco le finestre di situazione agenti per aggiornarle
        aggiornaSituazioni();
    }//GEN-LAST:event_butSaveActionPerformed

    private void aggiornaSituazioni() {
        //throw new UnsupportedOperationException("Not yet implemented");
        JInternalFrame[] frames = main.getPadre().getDesktopPane().getAllFrames();
        for (JInternalFrame frame : frames) {
            if (frame instanceof frmSituazioneAgenti) {
                ((frmSituazioneAgenti) frame).aggiornagriglia();
            }
        }
    }    
    
    private void butNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNewActionPerformed
        this.dati.dbNew();

        //setto defualt
        java.sql.Statement stat;
        ResultSet resu;

        //apre il resultset per ultimo +1
        try {
            stat = Db.getConn().createStatement();

            String sql = "select numero from provvigioni";
            sql += " where id_doc = " + provvigioni.documento_id;
            sql += " order by numero desc limit 1";
            resu = stat.executeQuery(sql);

            if (resu.next() == true) {
                this.texNume.setText(String.valueOf(resu.getInt(1) + 1));
            } else {
                this.texNume.setText("1");
            }
        } catch (Exception err) {
            javax.swing.JOptionPane.showMessageDialog(null, err.toString());
        }

        this.texDocuId.setText(cu.s(provvigioni.documento_id));
        
    }//GEN-LAST:event_butNewActionPerformed

    private void butSave1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSave1ActionPerformed
        butSaveActionPerformed(evt);
        dispose();
    }//GEN-LAST:event_butSave1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butDele;
    private javax.swing.JButton butFind;
    private javax.swing.JButton butFirs;
    private javax.swing.JButton butLast;
    private javax.swing.JButton butNew;
    private javax.swing.JButton butNext;
    private javax.swing.JButton butPrev;
    private javax.swing.JButton butSave;
    private javax.swing.JButton butSave1;
    private javax.swing.JButton butUndo;
    private tnxbeans.tnxDbPanel dati;
    private tnxbeans.tnxDbGrid griglia;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel131;
    private javax.swing.JLabel jLabel21111;
    private javax.swing.JLabel jLabel2211;
    private javax.swing.JLabel jLabel22111;
    private javax.swing.JLabel jLabel22112;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel labDocu;
    private javax.swing.JLabel labTotaleProvvigioni;
    private javax.swing.JPanel panAlto;
    private javax.swing.JPanel panDati;
    private javax.swing.JTabbedPane tabCent;
    private tnxbeans.tnxTextFieldFormatted texDataScad;
    private tnxbeans.tnxTextField texDocuId;
    private tnxbeans.tnxTextField texId;
    private tnxbeans.tnxTextField texImpo;
    private tnxbeans.tnxTextField texNume;
    private tnxbeans.tnxCheckBox tnxCheckBox1;
    // End of variables declaration//GEN-END:variables
    MaskFormatter mask;
}

class timRefreshTotali
    extends java.util.TimerTask {

    JInternalFrame frame;
    JLabel labTotaleProvvigioni;
    tnxbeans.tnxDbGrid griglia;
    double tempTotale;
    int INDEX_COL_IMPORTO = 8;
    private double totaleDocumento;

    public timRefreshTotali(JInternalFrame frame, tnxbeans.tnxDbGrid griglia, JLabel labTotaleProvvigioni, double totaleDocumento) {
        this.frame = frame;
        this.labTotaleProvvigioni = labTotaleProvvigioni;
        this.griglia = griglia;
        this.totaleDocumento = totaleDocumento;
    }

    public void run() {

        try {

            //calcola totale
            tempTotale = 0;

            for (int i = 0; i < griglia.getRowCount(); i++) {

                try {
                    tempTotale += Double.parseDouble(griglia.getValueAt(i, INDEX_COL_IMPORTO).toString());
                } catch (Exception err) {
                }
            }

            this.labTotaleProvvigioni.setText("Totale Provvigioni " + it.tnx.Util.EURO + " " + it.tnx.Util.formatValutaEuro(tempTotale));
            this.labTotaleProvvigioni.setForeground(java.awt.Color.black);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}