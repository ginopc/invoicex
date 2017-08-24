/**
 * Invoicex Copyright (c) 2005-2016 Marco Ceccarelli, Tnx srl
 *
 * Questo software è soggetto, e deve essere distribuito con la licenza GNU
 * General Public License, Version 2. La licenza accompagna il software o potete
 * trovarne una copia alla Free Software Foundation http://www.fsf.org .
 *
 * This software is subject to, and may be distributed under, the GNU General
 * Public License, Version 2. The license should have accompanied the software
 * or you may obtain a copy of the license from the Free Software Foundation at
 * http://www.fsf.org .
 *
 * -- Marco Ceccarelli (m.ceccarelli@tnx.it) Tnx snc (http://www.tnx.it)
 *
 */
/*
 * frmStatAgenti.java
 *
 * Created on 16 aprile 2003, 17.33
 */
package gestioneFatture.primaNota;

import it.tnx.Db;
import gestioneFatture.*;

import it.tnx.SwingWorker;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.SystemUtils;
import it.tnx.commons.cu;
import it.tnx.invoicex.InvoicexUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;

import java.sql.*;
import java.sql.ResultSet;


import java.util.*;
import java.util.Date;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;



//jasper
//import dori.jasper.engine.design.*;
//import dori.jasper.engine.*;
//import dori.jasper.view.*;
import net.sf.jasperreports.engine.*;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

//jfreechart
/**
 *
 * @author Lorenzo
 */
public class frmStampaRegistroIva
        extends javax.swing.JInternalFrame {

    int tipoLiquidazione = 0;

    /**
     * Creates new form frmStatAgenti
     */
    public frmStampaRegistroIva() {
        initComponents();

        tipo_numero_paginaActionPerformed(null);

        //jLabel2.setVisible(false);
        //this.texIvaPrecedente.setVisible(false);

        this.cambiaTipoIva(true);

        texAnno.setText(String.valueOf(it.tnx.Util.getCurrenteYear()));
        texIvaPrecedente.setText("0");

        try {
            comData.setSelectedItem(main.fileIni.getValue("frm_stampa_registro_iva", "data"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (main.pluginScontrini) {
            scontrini.setSelected(true);
            scontrini.setVisible(true);
        } else {
            scontrini.setSelected(false);
            scontrini.setVisible(false);
        }

        aggiornaDalAl();
        
        texAnno.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                aggiornaDalAl();
            }

            public void removeUpdate(DocumentEvent e) {
                aggiornaDalAl();
            }

            public void changedUpdate(DocumentEvent e) {
                aggiornaDalAl();
            }
        });
    }

    public void cambiaTipoIva(boolean start) {
        try {
            if (start == true) {
                ResultSet temp = Db.openResultSet("select tipo_liquidazione_iva from dati_azienda");
                temp.next();

                if (temp.getString("tipo_liquidazione_iva").equalsIgnoreCase("mensile")) {
                    this.radMensile.setSelected(true);
                    tipoLiquidazione = gestioneFatture.primaNota.PrimaNotaUtils.TIPO_PERIODO_MENSILE;
                } else if (temp.getString("tipo_liquidazione_iva").equalsIgnoreCase("trimestrale")) {
                    this.radTrimestrale.setSelected(true);
                    tipoLiquidazione = gestioneFatture.primaNota.PrimaNotaUtils.TIPO_PERIODO_TRIMESTRALE;
                } else if (temp.getString("tipo_liquidazione_iva").equalsIgnoreCase("annuale")) {
                    this.radAnnuale.setSelected(true);
                    tipoLiquidazione = gestioneFatture.primaNota.PrimaNotaUtils.TIPO_PERIODO_ANNUALE;
                } else {
                    this.radData.setSelected(true);
                    tipoLiquidazione = gestioneFatture.primaNota.PrimaNotaUtils.TIPO_PERIODO_DAL_AL;
                }
            } else {
                if (this.radMensile.isSelected()) {
                    tipoLiquidazione = gestioneFatture.primaNota.PrimaNotaUtils.TIPO_PERIODO_MENSILE;
                } else if (this.radTrimestrale.isSelected()) {
                    tipoLiquidazione = gestioneFatture.primaNota.PrimaNotaUtils.TIPO_PERIODO_TRIMESTRALE;
                } else if (this.radAnnuale.isSelected()) {
                    tipoLiquidazione = gestioneFatture.primaNota.PrimaNotaUtils.TIPO_PERIODO_ANNUALE;
                } else {
                    tipoLiquidazione = gestioneFatture.primaNota.PrimaNotaUtils.TIPO_PERIODO_DAL_AL;
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        comPeriodo.removeAllItems();
        comPeriodo.setEnabled(true);
        dal.setEnabled(false);
        al.setEnabled(false);

        int month = Calendar.getInstance().get(Calendar.MONTH);
        if (tipoLiquidazione == gestioneFatture.primaNota.PrimaNotaUtils.TIPO_PERIODO_MENSILE) {
            comPeriodo.addItem("Gennaio");
            comPeriodo.addItem("Febbraio");
            comPeriodo.addItem("Marzo");
            comPeriodo.addItem("Aprile");
            comPeriodo.addItem("Maggio");
            comPeriodo.addItem("Giugno");
            comPeriodo.addItem("Luglio");
            comPeriodo.addItem("Agosto");
            comPeriodo.addItem("Settembre");
            comPeriodo.addItem("Ottobre");
            comPeriodo.addItem("Novembre");
            comPeriodo.addItem("Dicembre");
            try {
                comPeriodo.setSelectedIndex(month);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (tipoLiquidazione == gestioneFatture.primaNota.PrimaNotaUtils.TIPO_PERIODO_TRIMESTRALE) {
            comPeriodo.addItem("Gennaio/Febbraio/Marzo");
            comPeriodo.addItem("Aprile/Maggio/Giugno");
            comPeriodo.addItem("Luglio/Agosto/Settembre");
            comPeriodo.addItem("Ottobre/Novembre/Dicembre");
            try {
                comPeriodo.setSelectedIndex(((month) / 3));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (tipoLiquidazione == gestioneFatture.primaNota.PrimaNotaUtils.TIPO_PERIODO_ANNUALE) {
            comPeriodo.setEnabled(false);
        } else {
            comPeriodo.setEnabled(false);
            dal.setEnabled(true);
            al.setEnabled(true);
        }
    }

    public static String getDescrizioneRegistro(String tipo) {

        if (tipo.equalsIgnoreCase("A")) {

            return "Registro IVA Fatture di Acquisto";
        } else {

            return "Registro IVA Fatture di Vendita";
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        butConferma = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        comPeriodo = new javax.swing.JComboBox();
        texAnno = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        texIvaPrecedente = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        comData = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        radTrimestrale = new javax.swing.JRadioButton();
        radMensile = new javax.swing.JRadioButton();
        radAnnuale = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        tipo_numero_pagina = new javax.swing.JComboBox();
        progressivo = new javax.swing.JTextField();
        labprogressivo = new javax.swing.JLabel();
        scontrini = new javax.swing.JCheckBox();
        formato = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        radData = new javax.swing.JRadioButton();
        dal = new org.jdesktop.swingx.JXDatePicker();
        al = new org.jdesktop.swingx.JXDatePicker();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        stampa_definitiva = new javax.swing.JCheckBox();

        FormListener formListener = new FormListener();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Stampa Registro Iva");

        butConferma.setText("Visualizza il Report");
        butConferma.addActionListener(formListener);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Seleziona il periodo");

        comPeriodo.addActionListener(formListener);

        texAnno.setColumns(6);
        texAnno.addKeyListener(formListener);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Anno");

        texIvaPrecedente.setColumns(10);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Data da usare per ordinare Fatt. Acquisto");

        comData.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Data di registrazione", "Data del doc. esterno" }));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Iva a Credito dal periodo prec.");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Tipo Liquidazione");

        buttonGroup1.add(radTrimestrale);
        radTrimestrale.setText("Trimestrale");
        radTrimestrale.addActionListener(formListener);

        buttonGroup1.add(radMensile);
        radMensile.setText("Mensile");
        radMensile.addActionListener(formListener);

        buttonGroup1.add(radAnnuale);
        radAnnuale.setText("Annuale");
        radAnnuale.addActionListener(formListener);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Come stampare i numeri di pagina");

        tipo_numero_pagina.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Stampa numeratore pagine semplice (Pagina # di #)", "Stampa progressivo con anno" }));
        tipo_numero_pagina.addActionListener(formListener);

        progressivo.setColumns(6);
        progressivo.setText("1");

        labprogressivo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labprogressivo.setText("progressivo di partenza");

        scontrini.setText("Includi scontrini");

        formato.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Anteprima a video", "Esportazione in Excel" }));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Formato");

        buttonGroup1.add(radData);
        radData.setText("Da data a data");
        radData.addActionListener(formListener);

        dal.setName("dal"); // NOI18N
        dal.addFocusListener(formListener);
        dal.addActionListener(formListener);
        dal.addPropertyChangeListener(formListener);

        al.setName("data"); // NOI18N
        al.addFocusListener(formListener);
        al.addActionListener(formListener);
        al.addPropertyChangeListener(formListener);

        jLabel9.setText("Al");

        jLabel10.setText("Dal");

        stampa_definitiva.setText("Stampa definitva (verranno bloccati i documenti)");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(butConferma, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabel7)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(radAnnuale)
                                    .add(radMensile)
                                    .add(radTrimestrale)
                                    .add(radData)))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(jLabel6)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texIvaPrecedente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(10, 10, 10)
                                        .add(jLabel2))
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .addContainerGap()
                                        .add(jLabel3))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel8))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(tipo_numero_pagina, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(labprogressivo)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(progressivo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(scontrini)
                                    .add(formato, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(comData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(stampa_definitiva)))
                            .add(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(jLabel5)
                                    .add(jLabel10))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(comPeriodo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(18, 18, 18)
                                        .add(jLabel1)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texAnno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(dal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel9)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(al, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                        .add(0, 45, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {jLabel2, jLabel3, jLabel5, jLabel6, jLabel7}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(11, 11, 11)
                .add(radMensile)
                .add(2, 2, 2)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radTrimestrale)
                    .add(jLabel7))
                .add(2, 2, 2)
                .add(radAnnuale)
                .add(2, 2, 2)
                .add(radData)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(comPeriodo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5)
                    .add(texAnno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(al, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9)
                    .add(jLabel10))
                .add(9, 9, 9)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(texIvaPrecedente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(comData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(tipo_numero_pagina, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(progressivo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labprogressivo))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scontrini)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(formato, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(stampa_definitiva)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 7, Short.MAX_VALUE)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(butConferma, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.FocusListener, java.awt.event.KeyListener, java.beans.PropertyChangeListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == butConferma) {
                frmStampaRegistroIva.this.butConfermaActionPerformed(evt);
            }
            else if (evt.getSource() == comPeriodo) {
                frmStampaRegistroIva.this.comPeriodoActionPerformed(evt);
            }
            else if (evt.getSource() == radTrimestrale) {
                frmStampaRegistroIva.this.radTrimestraleActionPerformed(evt);
            }
            else if (evt.getSource() == radMensile) {
                frmStampaRegistroIva.this.radMensileActionPerformed(evt);
            }
            else if (evt.getSource() == radAnnuale) {
                frmStampaRegistroIva.this.radAnnualeActionPerformed(evt);
            }
            else if (evt.getSource() == tipo_numero_pagina) {
                frmStampaRegistroIva.this.tipo_numero_paginaActionPerformed(evt);
            }
            else if (evt.getSource() == radData) {
                frmStampaRegistroIva.this.radDataActionPerformed(evt);
            }
            else if (evt.getSource() == dal) {
                frmStampaRegistroIva.this.dalActionPerformed(evt);
            }
            else if (evt.getSource() == al) {
                frmStampaRegistroIva.this.alActionPerformed(evt);
            }
        }

        public void focusGained(java.awt.event.FocusEvent evt) {
        }

        public void focusLost(java.awt.event.FocusEvent evt) {
            if (evt.getSource() == dal) {
                frmStampaRegistroIva.this.dalFocusLost(evt);
            }
            else if (evt.getSource() == al) {
                frmStampaRegistroIva.this.alFocusLost(evt);
            }
        }

        public void keyPressed(java.awt.event.KeyEvent evt) {
        }

        public void keyReleased(java.awt.event.KeyEvent evt) {
        }

        public void keyTyped(java.awt.event.KeyEvent evt) {
            if (evt.getSource() == texAnno) {
                frmStampaRegistroIva.this.texAnnoKeyTyped(evt);
            }
        }

        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (evt.getSource() == dal) {
                frmStampaRegistroIva.this.dalPropertyChange(evt);
            }
            else if (evt.getSource() == al) {
                frmStampaRegistroIva.this.alPropertyChange(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void butConfermaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butConfermaActionPerformed
        if (dal.getDate() == null) {
            SwingUtils.showInfoMessage(this, "Manca l'inizio del periodo");
            return;
        }
        if (dal.getDate() == null) {
            SwingUtils.showInfoMessage(this, "Manca la fine del periodo");
            return;
        }
        
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        
        ElaboraIva work = new ElaboraIva();
        work.padre = this;
        if (cu.toString(formato.getSelectedItem()).toLowerCase().indexOf("excel") >= 0) {
            work.xls = true;
        } else {
            work.xls = false;
        }
        
        work.start();

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_butConfermaActionPerformed

    private void radMensileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMensileActionPerformed
        cambiaTipoIva(false);
    }//GEN-LAST:event_radMensileActionPerformed

    private void radTrimestraleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radTrimestraleActionPerformed
        cambiaTipoIva(false);
    }//GEN-LAST:event_radTrimestraleActionPerformed

    private void radAnnualeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radAnnualeActionPerformed
        cambiaTipoIva(false);
    }//GEN-LAST:event_radAnnualeActionPerformed

    private void tipo_numero_paginaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipo_numero_paginaActionPerformed
        if (tipo_numero_pagina.getSelectedIndex() == 0) {
            labprogressivo.setEnabled(false);
            progressivo.setText("---");
            progressivo.setEnabled(false);
        } else {
            labprogressivo.setEnabled(true);
            progressivo.setText("1");
            progressivo.setEnabled(true);
        }
    }//GEN-LAST:event_tipo_numero_paginaActionPerformed

    private void radDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDataActionPerformed
        cambiaTipoIva(false);        
    }//GEN-LAST:event_radDataActionPerformed

    private void dalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dalActionPerformed
        
    }//GEN-LAST:event_dalActionPerformed

    private void dalFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dalFocusLost
        
    }//GEN-LAST:event_dalFocusLost

    private void dalPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dalPropertyChange
        System.out.println("evt = " + evt);
    }//GEN-LAST:event_dalPropertyChange

    private void alActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alActionPerformed
        
    }//GEN-LAST:event_alActionPerformed

    private void alFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_alFocusLost
        
    }//GEN-LAST:event_alFocusLost

    private void alPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_alPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_alPropertyChange

    private void comPeriodoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comPeriodoActionPerformed
        aggiornaDalAl();
    }//GEN-LAST:event_comPeriodoActionPerformed

    private void texAnnoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texAnnoKeyTyped
        
    }//GEN-LAST:event_texAnnoKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public org.jdesktop.swingx.JXDatePicker al;
    public javax.swing.JButton butConferma;
    public javax.swing.ButtonGroup buttonGroup1;
    public javax.swing.JComboBox comData;
    public javax.swing.JComboBox comPeriodo;
    public org.jdesktop.swingx.JXDatePicker dal;
    public javax.swing.JComboBox formato;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel10;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JLabel jLabel5;
    public javax.swing.JLabel jLabel6;
    public javax.swing.JLabel jLabel7;
    public javax.swing.JLabel jLabel8;
    public javax.swing.JLabel jLabel9;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.JLabel labprogressivo;
    public javax.swing.JTextField progressivo;
    public javax.swing.JRadioButton radAnnuale;
    public javax.swing.JRadioButton radData;
    public javax.swing.JRadioButton radMensile;
    public javax.swing.JRadioButton radTrimestrale;
    public javax.swing.JCheckBox scontrini;
    public javax.swing.JCheckBox stampa_definitiva;
    public javax.swing.JTextField texAnno;
    public javax.swing.JTextField texIvaPrecedente;
    public javax.swing.JComboBox tipo_numero_pagina;
    // End of variables declaration//GEN-END:variables

    private void aggiornaDalAl() {
        if (!radData.isSelected()) {
            dal.setDate(null);
            al.setDate(null);
        }
        if (radMensile.isSelected()) {
            if (comPeriodo.getSelectedIndex() >= 0 && cu.i0(texAnno.getText()) > 2000) {
                Date iniziomese = DateUtils.getDate(cu.i(texAnno.getText()), comPeriodo.getSelectedIndex() + 1, 1);
                Calendar cal = Calendar.getInstance();
                cal.setTime(iniziomese);
                cal.add(Calendar.MONTH, 1);
                cal.add(Calendar.DAY_OF_YEAR, -1);
                Date finemese = cal.getTime();
                
                dal.setDate(iniziomese);
                al.setDate(finemese);
            }
        } else if (radTrimestrale.isSelected()) {
            if (comPeriodo.getSelectedIndex() >= 0 && cu.i0(texAnno.getText()) > 2000) {
                Date iniziomese = DateUtils.getDate(cu.i(texAnno.getText()), (comPeriodo.getSelectedIndex() * 3) + 1, 1);
                Calendar cal = Calendar.getInstance();
                cal.setTime(iniziomese);
                cal.add(Calendar.MONTH, 3);
                cal.add(Calendar.DAY_OF_YEAR, -1);
                Date finemese = cal.getTime();
                
                dal.setDate(iniziomese);
                al.setDate(finemese);
            }            
        } else if (radAnnuale.isSelected()) {
            if (cu.i0(texAnno.getText()) > 2000) {
                Date iniziomese = DateUtils.getDate(cu.i0(texAnno.getText()), 1, 1);
                Calendar cal = Calendar.getInstance();
                cal.setTime(iniziomese);
                cal.add(Calendar.YEAR, 1);
                cal.add(Calendar.DAY_OF_YEAR, -1);
                Date finemese = cal.getTime();
                
                dal.setDate(iniziomese);
                al.setDate(finemese);
            }        
        }
    }

    final public class ElaboraIva extends SwingWorker {

        public frmStampaRegistroIva padre = null;
        public boolean xls = false;

        public Object construct() {
            final JDialogCompilazioneReport dialog = new JDialogCompilazioneReport();

            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            main.fileIni.setValue("frm_stampa_registro_iva", "data", comData.getSelectedItem());

            String sql = "";

            //prima rigenero la prima nota del periodo
            int anno = 0;

            try {
                anno = Integer.parseInt(texAnno.getText());
            } catch (Exception err) {
                err.printStackTrace();
            }

            gestioneFatture.primaNota.PrimaNotaUtils pn = new gestioneFatture.primaNota.PrimaNotaUtils(dialog);
            pn.generaPrimaNota(tipoLiquidazione, comPeriodo.getSelectedIndex() + 1, anno, (comData.getSelectedIndex() == 0 ? false : true), scontrini.isSelected(), dal.getDate(), al.getDate(), stampa_definitiva.isSelected());

            try {

                //con compilazione
                File frep = new File("reports/iva.jrxml");
                JasperReport jasperReport = Reports.getReport(frep);

                //System.out.println("load jrxml");
                //JasperDesign jasperDesign = JasperManager.loadXmlDesign("reports/iva.jrxml");
                //System.out.print("compilazione...");
                //JasperReport jasperReport = JasperManager.compileReport(jasperDesign);
                //System.out.println("...ok");
                //senza compilazione
                //System.out.println("load jasper");
                //JasperReport jasperReport = JasperManager.loadReport("reports/iva.jasper");

//            JasperReport jasperReport = JasperManager.loadReport(getClass().getResourceAsStream("/reports/iva.jasper"));

                // Second, create a map of parameters to pass to the report.
                java.util.Map parameters = new java.util.HashMap();
                if (radAnnuale.isSelected()) {
                    parameters.put("periodo", "Anno " + texAnno.getText());
                } else if (radData.isSelected()) {
                    parameters.put("periodo", "Dal " + DateUtils.formatDateIta(dal.getDate()) + " al " + DateUtils.formatDateIta(al.getDate()));
                } else {
                    parameters.put("periodo", padre.comPeriodo.getSelectedItem() + " " + texAnno.getText());
                }
                parameters.put("anno", texAnno.getText());
                parameters.put("tipo_numerazione_pagine", tipo_numero_pagina.getSelectedIndex());
                int pro = CastUtils.toInteger0(progressivo.getText());
                if (pro > 0) {
                    pro--;
                }

                parameters.put("progressivo_partenza", pro);
                String int1 = "";
                try {
                    //dati azienda
                    sql = "select ragione_sociale, indirizzo, localita, cap, provincia, cfiscale, piva from dati_azienda";
                    Map m = DbUtils.getListMap(Db.getConn(), sql).get(0);
                    int1 += m.get("ragione_sociale") + ", " + m.get("indirizzo") + ", " + m.get("cap") + " " + m.get("localita") + " (" + m.get("provincia") + "), " + "Partita IVA " + m.get("piva") + ", Codice Fiscale " + m.get("cfiscale");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                parameters.put("intestazione1", int1);

                //ciclo per i codici iva descendig senza quelle a zero
//                sql = "select * from codici_iva where percentuale > 0 order by percentuale desc";
//                ResultSet riva = Db.openResultSet(sql);
                int codiceIva = 0;

                parameters.put("iva1", "");
                parameters.put("iva2", "");
                parameters.put("iva3", "");
                parameters.put("iva4", "");
                parameters.put("iva5", "");
                
//                while (riva.next() && codiceIva <= 5) {
                for (Map miva : pn.riva) {
                    codiceIva++;
                    if (codiceIva > 5) break;
                    try {
                        parameters.put("iva" + codiceIva, "Aliquota " + it.tnx.Util.formatNumero0Decimali(cu.d(miva.get("percentuale"))) + " %");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                double saldo1 = 0;
                double saldo2 = 0;
                double creditoPeriodoPrec = 0;

                //calcolo totali
                parameters.put("totaleIvaAcquisti", new Double(pn.totali.totaleAcquisti));
                parameters.put("totaleIvaVendite", new Double(pn.totali.totaleVendite));

                DebugUtils.dump(parameters);

                saldo1 = pn.totali.totaleAcquisti - pn.totali.totaleVendite;

                System.out.println("saldo1: " + saldo1);

                parameters.put("ivaSaldo1", new Double(Math.abs(saldo1)));
                creditoPeriodoPrec = it.tnx.Util.getDouble(padre.texIvaPrecedente.getText());
                parameters.put("ivaACreditoPeriodoPrec", new Double(creditoPeriodoPrec));
                saldo2 = saldo1 + creditoPeriodoPrec;
                parameters.put("ivaSaldo2", new Double(Math.abs(saldo2)));

                if (saldo1 < 0) {
                    parameters.put("scrittaDebitoCredito1", "Debito");
                } else {
                    parameters.put("scrittaDebitoCredito1", "Credito");
                }

                if (saldo2 < 0) {
                    parameters.put("scrittaDebitoCredito2", "Debito");
                } else {
                    parameters.put("scrittaDebitoCredito2", "Credito");
                }

                if (!xls) {
                    Connection conn = it.tnx.Db.getConn();
                    JasperPrint jasperPrint = JasperManager.fillReport(jasperReport, parameters, conn);
                    InvoicexUtil.apriStampa(jasperPrint);
                } else {
                    //esporto xls
                    //stampa_iva_semplice
                    Map<String, String> colonne = Collections.synchronizedMap(new LinkedHashMap<String, String>());
                    colonne.put("id", "");
                    colonne.put("tipo", "");
                    colonne.put("data", "data doc. interno");
                    colonne.put("numero_prog", "num. doc. interno");
                    colonne.put("numero_doc", "num. doc. esterno");
                    colonne.put("data_doc", "data doc. esterno");
                    colonne.put("ragione_sociale", "");
                    colonne.put("piva_cfiscale", "partita iva");
                    colonne.put("totale", "");
                    colonne.put("imp1", "Imponibile " + parameters.get("iva1"));
                    colonne.put("iva1", "Iva " + parameters.get("iva1"));
                    colonne.put("imp2", "Imponibile " + parameters.get("iva2"));
                    colonne.put("iva2", "Iva " + parameters.get("iva2"));
                    colonne.put("imp3", "Imponibile " + parameters.get("iva3"));
                    colonne.put("iva3", "Iva " + parameters.get("iva3"));
                    colonne.put("imp4", "Imponibile " + parameters.get("iva4"));
                    colonne.put("iva4", "Iva " + parameters.get("iva4"));
                    colonne.put("imp5", "Imponibile " + parameters.get("iva5"));
                    colonne.put("iva5", "Iva " + parameters.get("iva5"));
                    colonne.put("altre_imp", "Esenti/Non Imponibili/Fuori campo");
                    colonne.put("imp_deducibile", "imponibile indeducibile");
                    colonne.put("iva_deducibile", "iva indeducibile");
                    
                    File exportDir = new File(SystemUtils.getUserDocumentsFolder() + File.separator + "Invoicex" + File.separator + "export");
                    exportDir.mkdirs();
                    File nomeFile = new File(SystemUtils.getUserDocumentsFolder() + File.separator + "Invoicex" + File.separator + "export" + File.separator + "iva.xls");
                    //boolean ret = InvoicexUtil.esportaInExcel(r, nomeFile.getAbsolutePath(), "Registro IVA", int1, "", colonne, true);
                    
                    try {
                        HSSFWorkbook wb = new HSSFWorkbook();
                        HSSFSheet sheet = wb.createSheet("Fatture di acquisto");
                        sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE); 
                        ResultSet racq = DbUtils.tryOpenResultSet(Db.getConn(), "select * from stampa_iva_semplice where tipo = 'A'");
                        short row_tot_acq = accodaXls(racq, sheet, colonne, wb);
                        sheet = wb.createSheet("Fatture di vendita");
                        sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE); 
                        ResultSet rven = DbUtils.tryOpenResultSet(Db.getConn(), "select * from stampa_iva_semplice where tipo = 'V'");
                        short row_tot_ven = accodaXls(rven, sheet, colonne, wb);
                        sheet = wb.createSheet("Totali");
                        sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE); 
                        accodaXlsTotali(creditoPeriodoPrec, sheet, wb, row_tot_acq, row_tot_ven, parameters);
                        FileOutputStream fileOut = new FileOutputStream(nomeFile);
                        wb.write(fileOut);
                        fileOut.close();
                        Util.start2(nomeFile.getAbsolutePath());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtils.showErrorMessage(padre, "Errore: " + ex.getMessage());
                    }
                }

            } catch (Exception err) {
                err.printStackTrace();
            } finally {
                dialog.setVisible(false);
                return null;
            }
        }

        @Override
        public void finished() {
            padre.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }

        private Short accodaXls(ResultSet rs, HSSFSheet sheet, Map colonne, HSSFWorkbook wb) {
            try {
                short contarows = 0;
                HSSFDataFormat format = wb.createDataFormat();
                
                HSSFRow row = sheet.createRow(contarows);
                contarows++;
                row.createCell(0).setCellValue(title);

                row = sheet.createRow(contarows);
                contarows++;
                row = sheet.createRow(contarows);
                contarows++;
                row.createCell(0).setCellValue(sheet.getSheetName());

                
                row = sheet.createRow(contarows);
                contarows++;
                //colonne
                row = sheet.createRow(contarows);
                contarows++;
                int columns = 0;
                columns = rs.getMetaData().getColumnCount();

                Iterator iter = colonne.keySet().iterator();
                short i = 0;
                while (iter.hasNext()) {
                    Object key = iter.next();
                    Object value = colonne.get(key);
                    String col = "";
                    //col = rs.getMetaData().getColumnLabel(i+1);
                    col = (String) value;
                    if (col == null || col.length() == 0) {
                        col = (String) key;
                    }
                    row.createCell(i).setCellValue(col);
                    i++;
                    //sheet.setColumnWidth(i, (headerWidth[i] * 300));
                }

                //stili
                HSSFCellStyle styledouble = wb.createCellStyle();
                styledouble.setDataFormat(format.getFormat("#,##0.00###"));

                HSSFCellStyle styleint = wb.createCellStyle();
                styleint.setDataFormat(format.getFormat("#,##0"));

                HSSFCellStyle styledata = wb.createCellStyle();
                styledata.setDataFormat(format.getFormat("dd/MM/yy"));

                //righe
                int rowcount = 0;
                rs.last();
                rowcount = rs.getRow();
                rs.beforeFirst();
                for (int j = 0; j < rowcount; j++) {
                    row = sheet.createRow(contarows);
                    contarows++;
                    //colonne
                    iter = colonne.keySet().iterator();
                    i = 0;
                    while (iter.hasNext()) {
                        String key = (String) iter.next();
                        String value = (String) colonne.get(key);

                        //controllo tipo di campo
                        Object o = null;
                        rs.absolute(j + 1);
                        o = rs.getObject(key);
                        if (o instanceof Double) {
                            HSSFCell cell = row.createCell(i);
                            cell.setCellValue((Double) o);
                            cell.setCellStyle(styledouble);
                        } else if (o instanceof BigDecimal) {
                            HSSFCell cell = row.createCell(i);
                            cell.setCellValue(((BigDecimal) o).doubleValue());
                            cell.setCellStyle(styledouble);
                        } else if (o instanceof Integer) {
                            HSSFCell cell = row.createCell(i);
                            cell.setCellValue(((Integer) o).intValue());
                            cell.setCellStyle(styleint);
                        } else if (o instanceof java.sql.Date) {
                            HSSFCell cell = row.createCell(i);
                            cell.setCellValue(((java.sql.Date) o));
                            cell.setCellStyle(styledata);
                        } else if (o instanceof byte[]) {
                            HSSFCell cell = row.createCell(i);
                            cell.setCellValue(new String((byte[]) o));
                            cell.setCellStyle(styleint);
                            row.createCell(i).setCellValue(new String((byte[]) o));
                        } else if (o instanceof Long) {
                            HSSFCell cell = row.createCell(i);
                            cell.setCellValue(((Long) o).longValue());
                            cell.setCellStyle(styleint);
                        } else {
                            if (!(o instanceof String)) {
                                if (o != null) {
                                    System.out.println(o.getClass());
                                }
                            }
                            row.createCell(i).setCellValue(CastUtils.toString(o));
                        }
                        i++;
                    }
                }

                //formule totali
                row = sheet.createRow(contarows);
                contarows++;
                row = sheet.createRow(contarows);
                row.createCell(7).setCellValue("Totali");                                
                row.createCell(8).setCellFormula("SUM(I1:I" + contarows + ")");
                row.getCell(8).setCellStyle(styledouble);
                row.createCell(9).setCellFormula("SUM(J1:J" + contarows + ")");
                row.getCell(9).setCellStyle(styledouble);
                row.createCell(10).setCellFormula("SUM(K1:K" + contarows + ")");
                row.getCell(10).setCellStyle(styledouble);
                row.createCell(11).setCellFormula("SUM(L1:L" + contarows + ")");
                row.getCell(11).setCellStyle(styledouble);
                row.createCell(12).setCellFormula("SUM(M1:M" + contarows + ")");
                row.getCell(12).setCellStyle(styledouble);
                row.createCell(13).setCellFormula("SUM(N1:N" + contarows + ")");
                row.getCell(13).setCellStyle(styledouble);
                row.createCell(14).setCellFormula("SUM(O1:O" + contarows + ")");
                row.getCell(14).setCellStyle(styledouble);
                row.createCell(15).setCellFormula("SUM(P1:P" + contarows + ")");
                row.getCell(15).setCellStyle(styledouble);
                row.createCell(16).setCellFormula("SUM(Q1:Q" + contarows + ")");
                row.getCell(16).setCellStyle(styledouble);
                row.createCell(17).setCellFormula("SUM(R1:R" + contarows + ")");
                row.getCell(17).setCellStyle(styledouble);
                row.createCell(18).setCellFormula("SUM(S1:S" + contarows + ")");
                row.getCell(18).setCellStyle(styledouble);
                row.createCell(19).setCellFormula("SUM(T1:T" + contarows + ")");
                row.getCell(19).setCellStyle(styledouble);
                row.createCell(20).setCellFormula("SUM(U1:U" + contarows + ")");
                row.getCell(20).setCellStyle(styledouble);
                row.createCell(21).setCellFormula("SUM(V1:V" + contarows + ")");
                row.getCell(21).setCellStyle(styledouble);
                
                contarows++;

//                if (note_piede != null && note_piede.length() > 0) {
//                    row = sheet.createRow(contarows);
//                    contarows++;
//                    row = sheet.createRow(contarows);
//                    contarows++;
//                    row.createCell(0).setCellValue(note_piede);
//                }

                return contarows;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        
        private boolean accodaXlsTotali(double creditoPeriodoPrec, HSSFSheet sheet, HSSFWorkbook wb, short row_tot_acq, short row_tot_ven, Map params) {
            try {
                short contarows = 0;
                HSSFDataFormat format = wb.createDataFormat();
                HSSFCellStyle styledouble = wb.createCellStyle();
                styledouble.setDataFormat(format.getFormat("#,##0.00###"));
                
                FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
                
                HSSFRow row = sheet.createRow(contarows);
                contarows++;
                row = sheet.createRow(contarows);
                contarows++;
                row = sheet.createRow(contarows);
                contarows++;
                row.createCell(1).setCellValue("Iva per Fatture di vendita");
                row.createCell(2).setCellFormula("'Fatture di vendita'!K" + row_tot_ven + " + " +
                        "'Fatture di vendita'!M" + row_tot_ven + " + " +
                        "'Fatture di vendita'!O" + row_tot_ven + " + " +
                        "'Fatture di vendita'!Q" + row_tot_ven + " + " +
                        "'Fatture di vendita'!S" + row_tot_ven);
                evaluator.evaluateFormulaCell(row.getCell(2));
                row.getCell(2).setCellStyle(styledouble);

                row = sheet.createRow(contarows);
                contarows++;
                row.createCell(1).setCellValue("Iva per Fatture di acquisto");
                row.createCell(2).setCellFormula("'Fatture di acquisto'!K" + row_tot_acq + " + " +
                        "'Fatture di acquisto'!M" + row_tot_acq + " + " +
                        "'Fatture di acquisto'!O" + row_tot_acq + " + " +
                        "'Fatture di acquisto'!Q" + row_tot_acq + " + " +
                        "'Fatture di acquisto'!S" + row_tot_acq);
                evaluator.evaluateFormulaCell(row.getCell(2));
                row.getCell(2).setCellStyle(styledouble);
                
                row = sheet.createRow(contarows);
                contarows++;
                row.createCell(1).setCellFormula("CONCATENATE(\"Iva a \",IF((C3-C4) >= 0, \"debito\", \"credito\"))");
                row.createCell(2).setCellFormula("ABS(C3-C4)");
                evaluator.evaluateFormulaCell(row.getCell(2));
                row.getCell(2).setCellStyle(styledouble);
                
                row = sheet.createRow(contarows);
                contarows++;
                row.createCell(1).setCellValue("Iva a credito da periodo precedente");
                row.createCell(2).setCellValue((Double)params.get("ivaACreditoPeriodoPrec"));
                row.getCell(2).setCellStyle(styledouble);
                
                row = sheet.createRow(contarows);
                contarows++;
                //row.createCell(1).setCellValue("Iva a " + params.get("scrittaDebitoCredito2"));
                row.createCell(1).setCellFormula("CONCATENATE(\"Iva a \",IF((C5-C6) >= 0, \"debito\", \"credito\"))");
                row.createCell(2).setCellFormula("ABS(C5-C6)");
                evaluator.evaluateFormulaCell(row.getCell(2));
                row.getCell(2).setCellStyle(styledouble);
                
                sheet.autoSizeColumn(1);
                sheet.autoSizeColumn(2);
                
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }        
        }
    }
}
