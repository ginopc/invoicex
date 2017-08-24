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
package gestioneFatture.magazzino;

import it.tnx.Db;
import gestioneFatture.*;
import it.tnx.accessoUtenti.Permesso;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.MicroBench;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.commons.swing.DelayedExecutor;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.data.Giacenza;
import it.tnx.invoicex.gui.JDialogLotti;
import it.tnx.invoicex.gui.JDialogMatricoleLotti;
import java.awt.Component;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.lang.StringUtils;
import tnxbeans.DbEvent;
import tnxbeans.DbListener;
import tnxbeans.tnxComboField;
import tnxbeans.tnxDbPanel;
import tnxbeans.tnxTextField;

public class frmMovimenti
        extends javax.swing.JInternalFrame {

    AtomicReference<ArticoloHint> articolo_ref = new AtomicReference(null);
    AtomicReference<ArticoloHint> articolo_selezionato_filtro_ref = new AtomicReference(null);

    DelayedExecutor delay_filtro = new DelayedExecutor(new Runnable() {
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SwingUtils.mouse_wait();
                    System.out.println("*** dbrefresh articolo");
                    refresh();
                    SwingUtils.mouse_def();
                }
            });
        }
    }, 250);

    /**
     * Creates new form frmDati_blank
     */
    public frmMovimenti() {
        initComponents();

        MicroBench mb = new MicroBench();
        mb.start();

//        griglia.setNoTnxResize(true);
        //apro la combo pagamenti
        this.comCausale.dbOpenList(Db.getConn(), "select descrizione, codice from tipi_causali_magazzino order by codice", null, false);
        mb.out("frmMovimenti mb due openlist piccoli 1");

        deposito.dbOpenList(Db.getConn(), "select CONCAT(nome, ' [', id, ']'), id from depositi order by nome");
        filtro_deposito.dbOpenList(Db.getConn(), "select CONCAT(nome, ' [', id, ']'), id from depositi order by nome");

        mb.out("frmMovimenti mb due openlist piccoli 2");

        //associo il panel ai dati
        this.dati.dbNomeTabella = "movimenti_magazzino";
        this.dati.dbNomeTabellaAlias = "m";

        Vector chiave = new Vector();
        chiave.add("id");
        this.dati.dbChiave = chiave;
        this.dati.butSave = this.butSave;
        this.dati.butUndo = this.butUndo;
        this.dati.butFind = this.butFind;
        this.dati.butNew = this.butNew;
        this.dati.butDele = this.butDele;
        this.dati.tipo_permesso = Permesso.PERMESSO_MAGAZZINO;

        this.dati.dbChiaveAutoInc = true;
        this.dati.dbOpen(Db.getConn(), "select * from movimenti_magazzino order by data desc, id desc limit 1");
        this.dati.dbRefresh();

        mb.out("frmMovimenti mb apertura dati");

        //apro la griglia
        //this.griglia.dbEditabile = true;
        this.griglia.dbChiave = chiave;
        this.griglia.flagUsaThread = false;

        java.util.Hashtable colsWidthPerc = new java.util.Hashtable();

//        griglia.dbOpen(Db.getConn(), "select m.*, a.codice_fornitore, a.codice_a_barre from movimenti_magazzino m left join articoli a on m.articolo = a.codice order by id desc", Db.INSTANCE, true);
        refresh();

        griglia.dbPanel = this.dati;

        mb.out("frmMovimenti mb apertura griglia");

        DelayedExecutor articolo_select = new DelayedExecutor(new Runnable() {
            public void run() {
                System.out.println("articolo_select this = " + this);
                texCodiArti.setText("");
                try {
                    texCodiArti.setText(articolo_ref.get().codice);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dati.dbForzaModificati();
            }
        }, 100);
        InvoicexUtil.getArticoloIntelliHints(articolo, this, articolo_ref, articolo_select, null);

        dati.addDbListener(new DbListener() {

            public void statusFired(DbEvent event) {
                if (event.getStatus() == tnxDbPanel.STATUS_REFRESHING || event.getStatus() == tnxDbPanel.STATUS_ADDING) {
                    //carico dati articolo
                    labDatiArticolo.setText("");
                    articolo.setText("");
                    List<Map> l;
                    try {
                        l = DbUtils.getListMap(Db.getConn(), "select * from articoli where codice = '" + Db.aa(texCodiArti.getText()) + "'");
                        String da = "Cod. fornitore " + l.get(0).get("codice_fornitore") + " / Cod. a barre " + l.get(0).get("codice_a_barre");
                        labDatiArticolo.setText(da);
                        articolo.setText(cu.s(l.get(0).get("descrizione")));
                    } catch (Exception ex) {
                    }

                    //controllo provenienza
                    String tab = cu.s(dati.dbGetField("da_tabella"));
                    String da_id = cu.s(dati.dbGetField("da_id"));
                    String da_id_riga = cu.s(dati.dbGetField("da_id_riga"));
                    if (StringUtils.isBlank(tab) || event.getStatus() == tnxDbPanel.STATUS_ADDING) {
//                        dati.setDbReadOnly(false);
                        setReadonly(false);

                        labGenerato.setVisible(false);
                        labProv.setVisible(false);
                    } else {
//                        dati.setDbReadOnly(true);
                        setReadonly(true);

                        labGenerato.setVisible(true);
                        String tipodoc = Db.getTipoDocDaNomeTabT(tab);
                        Map rec = null;
                        String numero = "";
                        try {
                            rec = dbu.getListMap(Db.getConn(), "select serie, numero, data from " + tab + " where id = " + da_id).get(0);
                            numero = cu.s(rec.get("serie")) + cu.s(rec.get("numero")) + " del " + DateUtils.formatDateIta(cu.toDate(rec.get("data")));
                        } catch (Exception e) {
                        }
                        labProv.setText(Db.getDescTipoDocBreve(tipodoc) + " " + numero);
                        labProv.setVisible(true);
                    }
                }
            }

            private void setReadonly(boolean come) {
                Component[] comps = dati.getComponents();
                for (Component comp : comps) {
                    if (comp instanceof tnxTextField || comp instanceof tnxComboField) {
                        if (!cu.s(comp.getName()).equals("lotto")
                                && !cu.s(comp.getName()).equals("matricola")
                                && !cu.s(comp.getName()).equals("note")) {
                            comp.setEnabled(!come);
                        }
                    }
                }
                articolo.setEnabled(!come);
            }
        });

        dati.griglia = griglia;
        dati.dbRefresh();

        InvoicexUtil.getArticoloIntelliHints(filtro_articolo, this, articolo_selezionato_filtro_ref, delay_filtro, null);

        mb.out("frmMovimenti mb refresh dati");

    }

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
        jButton2 = new javax.swing.JButton();
        butRefresh = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        tabCent = new javax.swing.JTabbedPane();
        dati = new tnxbeans.tnxDbPanel();
        texQuantita = new tnxbeans.tnxTextField();
        texNote = new tnxbeans.tnxTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel2111 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel224 = new javax.swing.JLabel();
        texData = new tnxbeans.tnxTextField();
        comCausale = new tnxbeans.tnxComboField();
        texCodiArti = new tnxbeans.tnxTextField();
        texCausale = new tnxbeans.tnxTextField();
        texId = new tnxbeans.tnxTextField();
        jLabel3 = new javax.swing.JLabel();
        texMatricola = new tnxbeans.tnxTextField();
        jLabel4 = new javax.swing.JLabel();
        texLotto = new tnxbeans.tnxTextField();
        jLabel2112 = new javax.swing.JLabel();
        tnxTextField1 = new tnxbeans.tnxTextField();
        jLabel27 = new javax.swing.JLabel();
        labDatiArticolo = new javax.swing.JLabel();
        labDepositoPartenza = new javax.swing.JLabel();
        deposito = new tnxbeans.tnxComboField();
        articolo = new javax.swing.JTextField();
        labGenerato = new javax.swing.JLabel();
        labProv = new javax.swing.JLabel();
        panElen = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        filtro_articolo = new javax.swing.JTextField();
        filtro_barre = new javax.swing.JTextField();
        filtro_fornitore = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        filtro_deposito = new tnxbeans.tnxComboField();
        labDepositoPartenza1 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        filtro_lotto = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        filtro_matricola = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        griglia = new tnxbeans.tnxDbGrid();
        jPanel2 = new javax.swing.JPanel();
        butUndo = new javax.swing.JButton();
        butSave = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Movimenti magazzino");
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
            }
        });

        panAlto.setLayout(new java.awt.BorderLayout());

        jToolBar1.setRollover(true);

        butNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butNew.setText("Nuovo");
        butNew.setBorderPainted(false);
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
        butDele.setBorderPainted(false);
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
        butFind.setBorderPainted(false);
        butFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butFindActionPerformed(evt);
            }
        });
        jToolBar1.add(butFind);

        jLabel131.setText(" ");
        jToolBar1.add(jLabel131);

        butFirs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/go-first.png"))); // NOI18N
        butFirs.setBorderPainted(false);
        butFirs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butFirsActionPerformed(evt);
            }
        });
        jToolBar1.add(butFirs);

        butPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/go-previous.png"))); // NOI18N
        butPrev.setBorderPainted(false);
        butPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPrevActionPerformed(evt);
            }
        });
        jToolBar1.add(butPrev);

        butNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/go-next.png"))); // NOI18N
        butNext.setBorderPainted(false);
        butNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNextActionPerformed(evt);
            }
        });
        jToolBar1.add(butNext);

        butLast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/go-last.png"))); // NOI18N
        butLast.setBorderPainted(false);
        butLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butLastActionPerformed(evt);
            }
        });
        jToolBar1.add(butLast);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-print.png"))); // NOI18N
        jButton2.setText("Stampa");
        jButton2.setBorderPainted(false);
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jButton2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        butRefresh.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        butRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/view-refresh.png"))); // NOI18N
        butRefresh.setToolTipText("Aggiorna l'elenco dei documenti");
        butRefresh.setBorderPainted(false);
        butRefresh.setFocusable(false);
        butRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butRefresh.setIconTextGap(2);
        butRefresh.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butRefreshActionPerformed(evt);
            }
        });
        jToolBar1.add(butRefresh);

        panAlto.add(jToolBar1, java.awt.BorderLayout.NORTH);

        jLabel9.setText("<html><font color=red>Nota Bene:</font> Per inserire l'esistenza iniziale o rettificare una giacenza, prima di tutto cliccare su <b>'Nuovo'</b><br> per inserire un nuovo movimento e non modificare il movimento visualizzato !</html>");
        jLabel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 601, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel9)
        );

        panAlto.add(jPanel1, java.awt.BorderLayout.CENTER);

        getContentPane().add(panAlto, java.awt.BorderLayout.NORTH);

        tabCent.setName("dati"); // NOI18N

        texQuantita.setColumns(10);
        texQuantita.setText("quantita");
        texQuantita.setDbNomeCampo("quantita");
        texQuantita.setDbTipoCampo("numerico");

        texNote.setText("note");
        texNote.setDbNomeCampo("note");
        texNote.setName("note"); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("quantita");

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("causale");

        jLabel2111.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2111.setText("data");

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("articolo");

        jLabel224.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel224.setText("note");

        texData.setColumns(10);
        texData.setText("data");
        texData.setDbDefault("CURRENT");
        texData.setDbNomeCampo("data");
        texData.setDbTipoCampo("data");

        comCausale.setEditable(false);
        comCausale.setDbNomeCampo("causale");
        comCausale.setDbRiempire(false);
        comCausale.setDbSalvare(false);
        comCausale.setDbTextAbbinato(texCausale);

        texCodiArti.setColumns(10);
        texCodiArti.setText("codice_articolo");
        texCodiArti.setToolTipText("");
        texCodiArti.setDbDescCampo("");
        texCodiArti.setDbNomeCampo("articolo");
        texCodiArti.setDbTipoCampo("");
        texCodiArti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texCodiArtiActionPerformed(evt);
            }
        });
        texCodiArti.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texCodiArtiFocusLost(evt);
            }
        });

        texCausale.setBackground(new java.awt.Color(204, 102, 255));
        texCausale.setText("0");
        texCausale.setDbComboAbbinata(comCausale);
        texCausale.setDbNomeCampo("causale");
        texCausale.setDbTipoCampo("NUMERIC");
        texCausale.setVisible(false);

        texId.setBackground(new java.awt.Color(204, 102, 255));
        texId.setText("0");
        texId.setDbNomeCampo("id");
        texId.setDbSalvare(false);
        texId.setDbTipoCampo("INTEGER");
        texId.setVisible(false);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("matricola");

        texMatricola.setColumns(10);
        texMatricola.setText("matricola");
        texMatricola.setDbNomeCampo("matricola");
        texMatricola.setDbTipoCampo("");
        texMatricola.setName("matricola"); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("lotto");

        texLotto.setColumns(10);
        texLotto.setText("lotto");
        texLotto.setDbNomeCampo("lotto");
        texLotto.setDbTipoCampo("");
        texLotto.setName("lotto"); // NOI18N

        jLabel2112.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2112.setText("id");

        tnxTextField1.setEditable(false);
        tnxTextField1.setColumns(5);
        tnxTextField1.setDbNomeCampo("id");

        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel27.setText("dati articolo");

        labDatiArticolo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labDatiArticolo.setText("...");

        labDepositoPartenza.setText("deposito");

        deposito.setEditable(false);
        deposito.setDbNomeCampo("deposito");
        deposito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                depositoActionPerformed(evt);
            }
        });

        labGenerato.setFont(labGenerato.getFont().deriveFont(labGenerato.getFont().getStyle() | java.awt.Font.BOLD, labGenerato.getFont().getSize()+1));
        labGenerato.setForeground(new java.awt.Color(204, 0, 51));
        labGenerato.setText("Movimento generato dal documento");

        labProv.setText("...");

        org.jdesktop.layout.GroupLayout datiLayout = new org.jdesktop.layout.GroupLayout(dati);
        dati.setLayout(datiLayout);
        datiLayout.setHorizontalGroup(
            datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiLayout.createSequentialGroup()
                .addContainerGap()
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(datiLayout.createSequentialGroup()
                        .add(jLabel224, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texNote, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(datiLayout.createSequentialGroup()
                        .add(jLabel27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(labDatiArticolo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(datiLayout.createSequentialGroup()
                        .add(jLabel2111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(labDepositoPartenza)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 295, Short.MAX_VALUE)
                        .add(jLabel2112)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tnxTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(datiLayout.createSequentialGroup()
                        .add(jLabel26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texCodiArti, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(articolo))
                    .add(datiLayout.createSequentialGroup()
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(datiLayout.createSequentialGroup()
                                        .add(texId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texCausale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(texLotto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texMatricola, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texQuantita, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comCausale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(datiLayout.createSequentialGroup()
                                .add(labGenerato)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(labProv)))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        datiLayout.setVerticalGroup(
            datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiLayout.createSequentialGroup()
                .addContainerGap()
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2112)
                    .add(tnxTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labDepositoPartenza)
                    .add(deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texCodiArti, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(articolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labDatiArticolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texQuantita, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(comCausale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel224, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texNote, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texMatricola, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texLotto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texCausale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labGenerato)
                    .add(labProv))
                .addContainerGap(127, Short.MAX_VALUE))
        );

        tabCent.addTab("dati", dati);

        panElen.setName("elenco"); // NOI18N
        panElen.setLayout(new java.awt.BorderLayout());

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Cod. Fornitore ");
        jLabel16.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Cod. a barre ");
        jLabel15.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Cod. Articolo ");
        jLabel14.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        jLabel10.setText("Filtra elenco movimenti:");

        filtro_articolo.setColumns(20);
        filtro_articolo.setMaximumSize(new java.awt.Dimension(80, 2147483647));
        filtro_articolo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                filtro_articoloKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtro_articoloKeyReleased(evt);
            }
        });

        filtro_barre.setColumns(20);
        filtro_barre.setMaximumSize(new java.awt.Dimension(150, 2147483647));
        filtro_barre.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtro_barreKeyReleased(evt);
            }
        });

        filtro_fornitore.setColumns(20);
        filtro_fornitore.setMaximumSize(new java.awt.Dimension(80, 2147483647));
        filtro_fornitore.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtro_fornitoreKeyReleased(evt);
            }
        });

        jButton1.setText("Azzera filtri");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        filtro_deposito.setEditable(false);
        filtro_deposito.setDbNomeCampo("");
        filtro_deposito.setDbRiempire(false);
        filtro_deposito.setDbSalvaKey(false);
        filtro_deposito.setDbSalvare(false);
        filtro_deposito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtro_depositoActionPerformed(evt);
            }
        });

        labDepositoPartenza1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labDepositoPartenza1.setText("Deposito");

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("Lotto");
        jLabel17.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        filtro_lotto.setColumns(20);
        filtro_lotto.setMaximumSize(new java.awt.Dimension(150, 2147483647));
        filtro_lotto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtro_lottoKeyReleased(evt);
            }
        });

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("Matricola");
        jLabel18.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        filtro_matricola.setColumns(20);
        filtro_matricola.setMaximumSize(new java.awt.Dimension(150, 2147483647));
        filtro_matricola.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtro_matricolaKeyReleased(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jButton1)
                .addContainerGap())
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                        .add(jLabel10)
                        .add(180, 180, 180))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jLabel17)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(filtro_lotto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jLabel14)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(filtro_articolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jLabel16)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(filtro_fornitore, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(40, 40, 40)))
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(labDepositoPartenza1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filtro_deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel15)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filtro_barre, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel18)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filtro_matricola, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(70, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(new java.awt.Component[] {jLabel14, jLabel15, jLabel16, jLabel17, jLabel18, labDepositoPartenza1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel10)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel14)
                    .add(filtro_articolo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labDepositoPartenza1)
                    .add(filtro_deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel16)
                    .add(filtro_fornitore, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(filtro_barre, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel15))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(filtro_lotto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel17)
                    .add(jLabel18)
                    .add(filtro_matricola, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jButton1)
                .addContainerGap())
        );

        panElen.add(jPanel3, java.awt.BorderLayout.NORTH);

        griglia.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        griglia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grigliaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(griglia);

        panElen.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        tabCent.addTab("elenco", panElen);

        getContentPane().add(tabCent, java.awt.BorderLayout.CENTER);
        tabCent.getAccessibleContext().setAccessibleName("Dati");

        butUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-undo.png"))); // NOI18N
        butUndo.setText("Annulla");
        butUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butUndoActionPerformed(evt);
            }
        });
        jPanel2.add(butUndo);

        butSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/devices/media-floppy.png"))); // NOI18N
        butSave.setText("Salva");
        butSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSaveActionPerformed(evt);
            }
        });
        jPanel2.add(butSave);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        main.getPadre().closeFrame(this);
    }//GEN-LAST:event_formInternalFrameClosing

    private void butDeleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDeleActionPerformed

        int ret = JOptionPane.showConfirmDialog(this, "Sicuro di eliminare ?", "Attenzione", JOptionPane.YES_NO_OPTION);

        if (ret == JOptionPane.YES_OPTION) {
            final Object inizio_mysql = Db.getCurrentTimestamp();

            final int[] rows = griglia.getSelectedRows();
            Thread t = new Thread() {
                @Override
                public void run() {
                    for (int i = rows.length - 1; i >= 0; i--) {
                        final int final_i = i;
                        griglia.getSelectionModel().setSelectionInterval(rows[final_i], rows[final_i]);
                        griglia.dbSelezionaRiga();
                        main.magazzino.preDelete("where id = " + texId.getText());
                        System.out.println("sel:" + rows[final_i]);
                        dati.dbDelete();
                    }
                    griglia.dbRefresh();
                    griglia.dbSelezionaRiga();
                    main.events.fireInvoicexEventMagazzino(this, inizio_mysql);
                }
            };
            t.start();
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
        Object inizio_mysql = Db.getCurrentTimestamp();

        this.dati.dbSave();

        String codice = texCodiArti.getText();

        try {
            if (main.GLOB.get("movimenti_disattiva_update_disponibilita_reale") == null) {
                List<Giacenza> retg = null;
                if (main.disp_articoli_da_deposito == null) {
                    retg = main.magazzino.getGiacenza(false, codice, null);
                } else {
                    retg = main.magazzino.getGiacenza(false, codice, null, null, false, true, main.disp_articoli_da_deposito);
                }
                System.out.println("retg = " + retg);
                if (retg != null) {
                    double qta = 0d;
                    if (retg.size() == 1) qta = retg.get(0).getGiacenza();
                    String sql = "update articoli set disponibilita_reale = " + dbu.sql(qta) + ", disponibilita_reale_ts = CURRENT_TIMESTAMP where codice = " + dbu.sql(codice);
                    System.out.println("sql aggiorna giacenza = " + sql);
                    dbu.tryExecQuery(gestioneFatture.Db.getConn(), sql);
                } else {
                    System.out.println("errore sul calcolo giacenze dell'articolo:" + codice + " esito:" + retg);
                }
            }
            //evento per far calcolare altri eventuali tipi di giacenze
            main.events.fireInvoicexEvent(new InvoicexEvent(codice, InvoicexEvent.TYPE_UPDATE_DISP_DA_GENERA_MOVIMENTI));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //chiedo lotti ?        
        try {
            String lotti = (String) DbUtils.getObject(Db.getConn(), "select gestione_lotti from articoli where codice = '" + Db.aa(codice) + "'");
            String matricole = (String) DbUtils.getObject(Db.getConn(), "select gestione_matricola from articoli where codice = '" + Db.aa(codice) + "'");
            if (lotti.equalsIgnoreCase("S") && matricole.equalsIgnoreCase("S")) {
                JDialogMatricoleLotti dialog = new JDialogMatricoleLotti(main.getPadre(), true, false);
                dialog.setLocationRelativeTo(null);
                String tipo = "C";
                if (comCausale.getSelectedItem().toString().toLowerCase().startsWith("scaric")) {
                    tipo = "S";
                }
                dialog.init(tipo, CastUtils.toDouble0(texQuantita.getText()), codice, "movimenti_magazzino", CastUtils.toInteger0(texId.getText()), texLotto.getText());
                dialog.setVisible(true);
                System.out.println("lotti ok");
            } else if (lotti.equalsIgnoreCase("S")) {
                JDialogLotti dialog = new JDialogLotti(main.getPadre(), true, false);
                dialog.setLocationRelativeTo(null);
                String tipo = "C";
                if (comCausale.getSelectedItem().toString().toLowerCase().startsWith("scaric")) {
                    tipo = "S";
                }
                dialog.init(tipo, CastUtils.toDouble0(texQuantita.getText()), codice, "movimenti_magazzino", CastUtils.toInteger0(texId.getText()), texLotto.getText(), cu.i(deposito.getSelectedKey()), false);
                dialog.setVisible(true);
                griglia.dbRefresh();
                griglia.dbSelezionaRiga();
                //dati.dbRefresh();
                System.out.println("lotti ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        main.events.fireInvoicexEventMagazzino(this, inizio_mysql);

//        griglia.dbRefresh();
//        griglia.dbSelezionaRiga();
//        dati.dbRefresh();

    }//GEN-LAST:event_butSaveActionPerformed

    private void butNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNewActionPerformed
        tabCent.setSelectedIndex(0);
        dati.dbNew();
        comCausale.setSelectedIndex(1);
        deposito.dbTrovaKey(main.fileIni.getValue("depositi", "predefinito", "0"));
        texCodiArti.requestFocus();
        articolo.setText("");
        labDatiArticolo.setText("");
    }//GEN-LAST:event_butNewActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        it.tnx.PrintUtilities.printComponent(this.dati);
}//GEN-LAST:event_jButton2ActionPerformed

    private void butRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butRefreshActionPerformed
        griglia.dbRefresh();
}//GEN-LAST:event_butRefreshActionPerformed

    private void filtro_articoloKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filtro_articoloKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_filtro_articoloKeyPressed

    private void filtro_articoloKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filtro_articoloKeyReleased
//        delay_filtro.update();
    }//GEN-LAST:event_filtro_articoloKeyReleased

    private void filtro_barreKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filtro_barreKeyReleased
        delay_filtro.update();
    }//GEN-LAST:event_filtro_barreKeyReleased

    private void filtro_fornitoreKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filtro_fornitoreKeyReleased
        delay_filtro.update();
    }//GEN-LAST:event_filtro_fornitoreKeyReleased

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        filtro_articolo.setText("");
        filtro_barre.setText("");
        filtro_fornitore.setText("");
        filtro_deposito.setSelectedIndex(-1);
        delay_filtro.update();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void grigliaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_grigliaMouseClicked

    private void depositoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_depositoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_depositoActionPerformed

    private void filtro_depositoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filtro_depositoActionPerformed
        delay_filtro.update();
    }//GEN-LAST:event_filtro_depositoActionPerformed

    private void texCodiArtiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texCodiArtiActionPerformed
        caricaDescArticolo();
    }//GEN-LAST:event_texCodiArtiActionPerformed

    private void texCodiArtiFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texCodiArtiFocusLost
        caricaDescArticolo();
    }//GEN-LAST:event_texCodiArtiFocusLost

    private void filtro_lottoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filtro_lottoKeyReleased
        delay_filtro.update();
    }//GEN-LAST:event_filtro_lottoKeyReleased

    private void filtro_matricolaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filtro_matricolaKeyReleased
        delay_filtro.update();
    }//GEN-LAST:event_filtro_matricolaKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField articolo;
    private javax.swing.JButton butDele;
    private javax.swing.JButton butFind;
    private javax.swing.JButton butFirs;
    private javax.swing.JButton butLast;
    private javax.swing.JButton butNew;
    private javax.swing.JButton butNext;
    private javax.swing.JButton butPrev;
    private javax.swing.JButton butRefresh;
    private javax.swing.JButton butSave;
    private javax.swing.JButton butUndo;
    private tnxbeans.tnxComboField comCausale;
    private tnxbeans.tnxDbPanel dati;
    private tnxbeans.tnxComboField deposito;
    private javax.swing.JTextField filtro_articolo;
    private javax.swing.JTextField filtro_barre;
    private tnxbeans.tnxComboField filtro_deposito;
    private javax.swing.JTextField filtro_fornitore;
    private javax.swing.JTextField filtro_lotto;
    private javax.swing.JTextField filtro_matricola;
    private tnxbeans.tnxDbGrid griglia;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel131;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel2111;
    private javax.swing.JLabel jLabel2112;
    private javax.swing.JLabel jLabel224;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel labDatiArticolo;
    private javax.swing.JLabel labDepositoPartenza;
    private javax.swing.JLabel labDepositoPartenza1;
    private javax.swing.JLabel labGenerato;
    private javax.swing.JLabel labProv;
    private javax.swing.JPanel panAlto;
    private javax.swing.JPanel panElen;
    private javax.swing.JTabbedPane tabCent;
    private tnxbeans.tnxTextField texCausale;
    private tnxbeans.tnxTextField texCodiArti;
    private tnxbeans.tnxTextField texData;
    private tnxbeans.tnxTextField texId;
    private tnxbeans.tnxTextField texLotto;
    private tnxbeans.tnxTextField texMatricola;
    private tnxbeans.tnxTextField texNote;
    private tnxbeans.tnxTextField texQuantita;
    private tnxbeans.tnxTextField tnxTextField1;
    // End of variables declaration//GEN-END:variables

    public void refresh() {
        String sql = "";
        sql += "select m.id"
                + ", m.data"
                + ", CAST(CONCAT(dep.nome, ' [', dep.id, ']') AS CHAR(50)) as deposito"
                + ", cau.descrizione as causale"
                + ", m.articolo"
                + ", if (cau.segno = -1 , -m.quantita, m.quantita) as quantita"
                + ", m.note"
                + ", cast(concat(IF(m.da_tipo_fattura = 7, 'Scontr.', "
                + "         IF(m.da_tabella = 'test_fatt', 'Fatt. Vend.', "
                + "         IF(m.da_tabella = 'test_ddt', 'DDT Vend.',"
                + "         IF(m.da_tabella = 'test_fatt_acquisto', 'Fatt. Acq.',"
                + "         IF(m.da_tabella = 'test_ddt_acquisto', 'DDT Acq.', m.da_tabella)))))"
                + "        , da_serie, ' ', da_numero, '/', da_anno, ' - [ID ', da_id, ']') as CHAR)as origine"
                //                + ", m.da_tabella"
                //                + ", m.da_anno"
                //                + ", m.da_serie"
                //                + ", m.da_numero"
                //                + ", m.da_id"
                + ", m.matricola"
                + ", m.lotto"
                + ", a.codice_fornitore, a.codice_a_barre from movimenti_magazzino m left join articoli a on m.articolo = a.codice"
                + " left join tipi_causali_magazzino cau on m.causale = cau.codice"
                + " left join depositi dep ON m.deposito = dep.id";
        sql += " where 1 = 1";
        System.out.println("articolo_selezionato_filtro_ref = " + articolo_selezionato_filtro_ref);
        System.out.println("articolo_selezionato_filtro_ref get = " + articolo_selezionato_filtro_ref.get());
        if (articolo_selezionato_filtro_ref.get() != null && StringUtils.isNotBlank(articolo_selezionato_filtro_ref.get().codice)) {
            sql += " and m.articolo like '" + Db.aa(articolo_selezionato_filtro_ref.get().codice) + "'";
        }
        if (filtro_barre.getText().length() > 0) {
            sql += " and a.codice_a_barre like '%" + Db.aa(filtro_barre.getText()) + "%'";
        }
        if (filtro_lotto.getText().length() > 0) {
            sql += " and m.lotto like '%" + Db.aa(filtro_lotto.getText()) + "%'";
        }
        if (filtro_matricola.getText().length() > 0) {
            sql += " and m.matricola like '%" + Db.aa(filtro_matricola.getText()) + "%'";
        }
        if (filtro_fornitore.getText().length() > 0) {
            sql += " and a.codice_fornitore like '%" + Db.aa(filtro_fornitore.getText()) + "%'";
        }
        if (filtro_deposito.getSelectedIndex() >= 0) {
            sql += " and m.deposito = " + cu.s(filtro_deposito.getSelectedKey());
        }
        sql += " order by m.data desc, id desc";
        System.out.println("sql movimenti: " + sql);
        griglia.dbOpen(Db.getConn(), sql, Db.INSTANCE, true);
        griglia.dbSelezionaRiga();
    }

    private void caricaDescArticolo() {
        try {
            String sql = "select codice, descrizione from articoli where codice = " + dbu.sql(texCodiArti.getText());
            List<Map> ret = dbu.getListMap(Db.getConn(), sql);
            if (ret != null && ret.size() > 0) {
                Map m = ret.get(0);
                articolo.setText(cu.s(m.get("descrizione")));
                texCodiArti.setText(cu.s(m.get("codice")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            articolo.setText("");
        }        
    }

}
