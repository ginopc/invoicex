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
package gestioneFatture.logic.provvigioni;

import it.tnx.Db;
import gestioneFatture.Reports;
import gestioneFatture.main;
import it.tnx.accessoUtenti.Permesso;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;

import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import java.awt.Cursor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import java.sql.Types;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class frmSituazioneAgenti
        extends javax.swing.JInternalFrame {

    String sql;
    String sqlCorrente;
//    String sqlIniziale = null;
    String sqlInizialeScadenze = null;
    String sqlInizialeFatture = null;
    boolean per_scadenze = true;
    
    boolean is_loading = true;
    private String note_filtro_stampa;
    
    String oldsql = null;

    /**
     * Creates new form frmElenPrev
     */
    public frmSituazioneAgenti() {

        try {
            String provvigioni_tipo_data = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select provvigioni_tipo_data from dati_azienda"));
            if (provvigioni_tipo_data.equalsIgnoreCase("data_fattura")) {
                per_scadenze = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        if (per_scadenze) {
            sqlInizialeScadenze = " select test_fatt.pagamento, test_fatt.serie, test_fatt.numero, test_fatt.anno, test_fatt.data, test_fatt.totale, scadenze.data_scadenza as 'S data scadenza', scadenze.importo as 'S importo', scadenze.pagata as 'S pagata', provvigioni.numero as 'P num', provvigioni.importo as 'P importo', test_fatt.agente_percentuale as 'P %', provvigioni.importo_provvigione as 'P provvigione', provvigioni.pagata as 'P pagata', clie_forn.ragione_sociale as cliente, provvigioni.id as p_id, agenti.nome as agente, test_fatt.id as tid \n"
                    + " , (select count(*) from scadenze s2 where s2.id_doc = test_fatt.id and s2.documento_tipo = 'FA') as numero_scadenze, test_fatt.sconto \n"
                    + " from test_fatt left join scadenze on test_fatt.id = scadenze.id_doc \n"
                    + " left join provvigioni on test_fatt.id = provvigioni.id_doc and scadenze.id = provvigioni.id_scadenza \n"
                    + " left join clie_forn on test_fatt.cliente = clie_forn.codice \n"
                    + " left join pagamenti on test_fatt.pagamento = pagamenti.codice \n"
                    + " left join agenti on test_fatt.agente_codice = agenti.id \n"
                    + " where provvigioni.importo is not null and agenti.id is not null and id_scadenza is not null";
//        } else {
            sqlInizialeFatture = " select test_fatt.pagamento, test_fatt.serie, test_fatt.numero, test_fatt.anno, test_fatt.data, test_fatt.totale, provvigioni.data_scadenza as 'S data scadenza', test_fatt.totale as 'S importo', '' as 'S pagata', provvigioni.numero as 'P num', provvigioni.importo as 'P importo', test_fatt.agente_percentuale as 'P %', provvigioni.importo_provvigione as 'P provvigione', provvigioni.pagata as 'P pagata', clie_forn.ragione_sociale as cliente, provvigioni.id as p_id, agenti.nome as agente, test_fatt.id as tid "
                    + " , 1 as numero_scadenze, test_fatt.sconto \n"
                    + " from test_fatt left join provvigioni on test_fatt.id = provvigioni.id_doc \n"
                    + "   left join clie_forn on test_fatt.cliente = clie_forn.codice \n"
                    + "   left join pagamenti on test_fatt.pagamento = pagamenti.codice \n"
                    + "   left join agenti on test_fatt.agente_codice = agenti.id \n"
                    + " where provvigioni.importo is not null and agenti.id is not null and id_scadenza is null";
//        }

        initComponents();
        
        if (!main.utente.getPermesso(Permesso.PERMESSO_AGENTI, Permesso.PERMESSO_TIPO_SCRITTURA)) {
            this.comMarca.setEnabled(false);
            this.comMarca1.setEnabled(false);
        }
        //apro la griglia
        this.griglia.dbNomeTabella = "";
        this.griglia.flagUsaThread = false;

        java.util.Hashtable colsWidthPerc = new java.util.Hashtable();
        colsWidthPerc.put("pagamento", new Double(10));
        colsWidthPerc.put("serie", new Double(3));
        colsWidthPerc.put("numero", new Double(5));
        colsWidthPerc.put("anno", new Double(0));
        colsWidthPerc.put("data", new Double(7));
        colsWidthPerc.put("totale", new Double(8));
        colsWidthPerc.put("S data scadenza", new Double(7));
        colsWidthPerc.put("S importo", new Double(7));
        colsWidthPerc.put("S pagata", new Double(6));
        colsWidthPerc.put("P num", new Double(5));
        colsWidthPerc.put("P importo", new Double(8));
        colsWidthPerc.put("P %", new Double(6));
        colsWidthPerc.put("P provvigione", new Double(7));
        colsWidthPerc.put("P pagata", new Double(6));
        colsWidthPerc.put("cliente", new Double(10));
        colsWidthPerc.put("p_id", new Double(0));
        colsWidthPerc.put("agente", new Double(10));
        colsWidthPerc.put("tid", new Double(0));
        colsWidthPerc.put("numero_scadenze", new Double(0));
        colsWidthPerc.put("sconto", new Double(0));
        this.griglia.columnsSizePerc = colsWidthPerc;

        java.util.Hashtable colsAlign = new java.util.Hashtable();
        colsAlign.put("totale", "RIGHT_CURRENCY");
        colsAlign.put("S importo", "RIGHT_CURRENCY");
        colsAlign.put("P provvigione", "RIGHT_CURRENCY");
        this.griglia.columnsAlign = colsAlign;
        this.comAgente.dbAddElement("<selezionare un agente>", "-1");
        this.comAgente.dbAddElement("<tutti gli agenti>", "*");
        this.comAgente.dbOpenList(Db.getConn(), "select nome, id from agenti order by nome", "*", false);
        this.comMese.addItem("Gennaio");
        this.comMese.addItem("Febbraio");
        this.comMese.addItem("Marzo");
        this.comMese.addItem("Aprile");
        this.comMese.addItem("Maggio");
        this.comMese.addItem("Giugno");
        this.comMese.addItem("Luglio");
        this.comMese.addItem("Agosto");
        this.comMese.addItem("Settembre");
        this.comMese.addItem("Ottobre");
        this.comMese.addItem("Novembre");
        this.comMese.addItem("Dicembre");
        this.comMese.addItem("Gennaio/Febbraio");
        this.comMese.addItem("Marzo/Aprile");
        this.comMese.addItem("Maggio/Giugno");
        this.comMese.addItem("Luglio/Agosto");
        this.comMese.addItem("Settembre/Ottobre");
        this.comMese.addItem("Novembre/Dicembre");
        this.comMese.addItem("1° Trimestre");
        this.comMese.addItem("2° Trimestre");
        this.comMese.addItem("3° Trimestre");
        this.comMese.addItem("4° Trimestre");
        this.comMese.addItem("<tutto l'anno>");
        this.comMese.addItem("da data / a data");
        //this.comMese.setSelectedIndex(it.tnx.Util.getCurrenteMonth() - 1);
        comMese.setSelectedIndex(comMese.getItemCount() - 2);
        this.texAnno.setText(String.valueOf(it.tnx.Util.getCurrenteYear()));

        is_loading = false;
        
        visualizzaTutte();

        texDal.addPropertyChangeListener("date", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                selezionaSituazione();
            }
        });
        texAl.addPropertyChangeListener("date", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                selezionaSituazione();
            }
        });
        texDalDoc.addPropertyChangeListener("date", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                selezionaSituazione();
            }
        });
        texAlDoc.addPropertyChangeListener("date", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                selezionaSituazione();
            }
        });
        
    }

    private Calendar getFine() {
        Calendar fine = Calendar.getInstance();
        if (comMese.getSelectedIndex() == comMese.getItemCount() - 1) {
            if (texAl.getDate() == null) {
                return null;
            }
            fine.setTime(texAl.getDate());
        } else if (comMese.getSelectedIndex() == comMese.getItemCount() - 2) {
            fine.set(it.tnx.Util.getInt(this.texAnno.getText()), 11, 31);
        } else if (comMese.getSelectedIndex() >= 18) {
            Calendar inizio = getInizio();
            inizio.add(Calendar.MONTH, 2);
            fine.set(it.tnx.Util.getInt(this.texAnno.getText()), inizio.get(Calendar.MONTH), inizio.getActualMaximum(fine.DAY_OF_MONTH));
        } else if (comMese.getSelectedIndex() > 11) {
            Calendar inizio = getInizio();
            inizio.add(Calendar.MONTH, 1);
            fine.set(it.tnx.Util.getInt(this.texAnno.getText()), inizio.get(Calendar.MONTH), inizio.getActualMaximum(fine.DAY_OF_MONTH));
        } else {
            fine.set(it.tnx.Util.getInt(this.texAnno.getText()), this.comMese.getSelectedIndex(), getInizio().getActualMaximum(fine.DAY_OF_MONTH));
        }
        System.out.println("fine:" + fine.getTime());
        return fine;
    }

    private Calendar getInizio() {
        Calendar inizio = Calendar.getInstance();
        if (comMese.getSelectedIndex() == comMese.getItemCount() - 1) {
            if (texDal.getDate() == null) {
                return null;
            }
            inizio.setTime(texDal.getDate());
        } else if (comMese.getSelectedIndex() == comMese.getItemCount() - 2) {
            inizio.set(it.tnx.Util.getInt(this.texAnno.getText()), 0, 1);
        } else if (comMese.getSelectedIndex() >= 18) {
            inizio.set(it.tnx.Util.getInt(this.texAnno.getText()), ((this.comMese.getSelectedIndex() - 18) * 3), 1);
        } else if (comMese.getSelectedIndex() > 11) {
            inizio.set(it.tnx.Util.getInt(this.texAnno.getText()), ((this.comMese.getSelectedIndex() - 12) * 2), 1);
        } else {
            inizio.set(it.tnx.Util.getInt(this.texAnno.getText()), this.comMese.getSelectedIndex(), 1);
        }
        System.out.println("inizio:" + inizio.getTime());
        return inizio;
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        tooMenu = new javax.swing.JToolBar();
        comStampa = new javax.swing.JButton();
        jLabel112 = new javax.swing.JLabel();
        comStampa1 = new javax.swing.JButton();
        comStampa2 = new javax.swing.JButton();
        jLabel113 = new javax.swing.JLabel();
        comMarca = new javax.swing.JButton();
        comMarca1 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        panDati = new javax.swing.JPanel();
        panClie = new tnxbeans.tnxDbPanel();
        jLabel2 = new javax.swing.JLabel();
        comAgente = new tnxbeans.tnxComboField();
        jLabel3 = new javax.swing.JLabel();
        labAnno = new javax.swing.JLabel();
        comMese = new javax.swing.JComboBox();
        texAnno = new javax.swing.JTextField();
        comAggiorna = new javax.swing.JButton();
        comAggiornaSoloPagate = new javax.swing.JButton();
        comAggiornaSoloPagate1 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        texDalDoc = new org.jdesktop.swingx.JXDatePicker();
        jLabel8 = new javax.swing.JLabel();
        texAlDoc = new org.jdesktop.swingx.JXDatePicker();
        labDal = new javax.swing.JLabel();
        texDal = new org.jdesktop.swingx.JXDatePicker();
        labAl = new javax.swing.JLabel();
        texAl = new org.jdesktop.swingx.JXDatePicker();
        jScrollPane1 = new javax.swing.JScrollPane();
        griglia = new tnxbeans.tnxDbGrid();
        jPanel2 = new javax.swing.JPanel();
        labTotale = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Situazione Agenti");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosed(evt);
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
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

        tooMenu.setRollover(true);

        comStampa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-print.png"))); // NOI18N
        comStampa.setText("Stampa");
        comStampa.setToolTipText("");
        comStampa.setBorderPainted(false);
        comStampa.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        comStampa.setMargin(new java.awt.Insets(2, 2, 2, 2));
        comStampa.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        comStampa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comStampaActionPerformed(evt);
            }
        });
        tooMenu.add(comStampa);

        jLabel112.setText("  ");
        tooMenu.add(jLabel112);

        comStampa1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-print.png"))); // NOI18N
        comStampa1.setText("Report per agente");
        comStampa1.setToolTipText("");
        comStampa1.setBorderPainted(false);
        comStampa1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        comStampa1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        comStampa1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        comStampa1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comStampa1ActionPerformed(evt);
            }
        });
        tooMenu.add(comStampa1);

        comStampa2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-print.png"))); // NOI18N
        comStampa2.setText("Report dettagliato");
        comStampa2.setToolTipText("");
        comStampa2.setBorderPainted(false);
        comStampa2.setFocusable(false);
        comStampa2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        comStampa2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        comStampa2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        comStampa2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comStampa2ActionPerformed(evt);
            }
        });
        tooMenu.add(comStampa2);

        jLabel113.setText("  ");
        tooMenu.add(jLabel113);

        comMarca.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-redo.png"))); // NOI18N
        comMarca.setText("Marca come pagate");
        comMarca.setToolTipText("");
        comMarca.setBorderPainted(false);
        comMarca.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        comMarca.setMargin(new java.awt.Insets(2, 2, 2, 2));
        comMarca.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        comMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comMarcaActionPerformed(evt);
            }
        });
        tooMenu.add(comMarca);

        comMarca1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Delete Sign-16.png"))); // NOI18N
        comMarca1.setText("Marca come NON pagate");
        comMarca1.setToolTipText("");
        comMarca1.setBorderPainted(false);
        comMarca1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        comMarca1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        comMarca1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        comMarca1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comMarca1ActionPerformed(evt);
            }
        });
        tooMenu.add(comMarca1);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-select-all.png"))); // NOI18N
        jButton1.setText("Seleziona tutte");
        jButton1.setBorderPainted(false);
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        tooMenu.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-clear.png"))); // NOI18N
        jButton2.setText("Deseleziona tutte");
        jButton2.setBorderPainted(false);
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        tooMenu.add(jButton2);

        getContentPane().add(tooMenu, java.awt.BorderLayout.NORTH);

        panDati.setLayout(new java.awt.BorderLayout());

        jLabel2.setText("Agente");

        comAgente.setPreferredSize(new java.awt.Dimension(180, 20));
        comAgente.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comAgenteItemStateChanged(evt);
            }
        });

        jLabel3.setText("Periodo");
        jLabel3.setToolTipText("");
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        labAnno.setText("anno");

        comMese.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comMeseItemStateChanged(evt);
            }
        });

        texAnno.setColumns(4);
        texAnno.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texAnnoFocusLost(evt);
            }
        });

        comAggiorna.setText("Visualizza Tutte");
        comAggiorna.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comAggiornaActionPerformed(evt);
            }
        });

        comAggiornaSoloPagate.setFont(comAggiornaSoloPagate.getFont().deriveFont(comAggiornaSoloPagate.getFont().getSize()-2f));
        comAggiornaSoloPagate.setText("Visualizza provvigioni NON pagate");
        comAggiornaSoloPagate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comAggiornaSoloPagateActionPerformed(evt);
            }
        });

        comAggiornaSoloPagate1.setFont(comAggiornaSoloPagate1.getFont().deriveFont(comAggiornaSoloPagate1.getFont().getSize()-2f));
        comAggiornaSoloPagate1.setText("Visualizza provvigioni pagate");
        comAggiornaSoloPagate1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comAggiornaSoloPagate1ActionPerformed(evt);
            }
        });

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Data documento dal");

        texDalDoc.setName("texDalDoc"); // NOI18N
        texDalDoc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texDalDocFocusLost(evt);
            }
        });
        texDalDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texDalDocActionPerformed(evt);
            }
        });
        texDalDoc.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                texDalDocPropertyChange(evt);
            }
        });

        jLabel8.setText("Al");

        texAlDoc.setName("data"); // NOI18N
        texAlDoc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texAlDocFocusLost(evt);
            }
        });
        texAlDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texAlDocActionPerformed(evt);
            }
        });
        texAlDoc.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                texAlDocPropertyChange(evt);
            }
        });

        labDal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labDal.setText("dal");

        texDal.setName("data"); // NOI18N
        texDal.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texDalFocusLost(evt);
            }
        });
        texDal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texDalActionPerformed(evt);
            }
        });
        texDal.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                texDalPropertyChange(evt);
            }
        });

        labAl.setText("al");

        texAl.setName("data"); // NOI18N
        texAl.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texAlFocusLost(evt);
            }
        });
        texAl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texAlActionPerformed(evt);
            }
        });
        texAl.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                texAlPropertyChange(evt);
            }
        });

        org.jdesktop.layout.GroupLayout panClieLayout = new org.jdesktop.layout.GroupLayout(panClie);
        panClie.setLayout(panClieLayout);
        panClieLayout.setHorizontalGroup(
            panClieLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panClieLayout.createSequentialGroup()
                .addContainerGap()
                .add(panClieLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panClieLayout.createSequentialGroup()
                        .add(panClieLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(panClieLayout.createSequentialGroup()
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comAgente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(panClieLayout.createSequentialGroup()
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comMese, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(labAnno)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texAnno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(labDal)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texDal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(labAl)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texAl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(comAggiorna)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(panClieLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(comAggiornaSoloPagate1)
                            .add(comAggiornaSoloPagate)))
                    .add(panClieLayout.createSequentialGroup()
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texDalDoc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texAlDoc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        panClieLayout.linkSize(new java.awt.Component[] {comAggiornaSoloPagate, comAggiornaSoloPagate1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        panClieLayout.setVerticalGroup(
            panClieLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panClieLayout.createSequentialGroup()
                .add(panClieLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panClieLayout.createSequentialGroup()
                        .add(7, 7, 7)
                        .add(panClieLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2)
                            .add(comAgente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(panClieLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel3)
                            .add(comMese, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(labAnno)
                            .add(texAnno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(labDal)
                            .add(labAl)
                            .add(texDal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(texAl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(panClieLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(panClieLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(comAggiorna, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(panClieLayout.createSequentialGroup()
                                .add(comAggiornaSoloPagate1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comAggiornaSoloPagate)))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(panClieLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jLabel8)
                    .add(texDalDoc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texAlDoc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panClieLayout.linkSize(new java.awt.Component[] {comMese, labAl, labAnno, labDal, texAl, texAnno, texDal}, org.jdesktop.layout.GroupLayout.VERTICAL);

        panDati.add(panClie, java.awt.BorderLayout.NORTH);

        jScrollPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jScrollPane1MouseClicked(evt);
            }
        });

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

        panDati.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(panDati, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        labTotale.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labTotale.setText("...");
        jPanel2.add(labTotale);

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void texAnnoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texAnnoFocusLost
        selezionaSituazione();
    }//GEN-LAST:event_texAnnoFocusLost

    private void comMeseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comMeseItemStateChanged
        if (evt.getStateChange() == evt.SELECTED) {
            if (comMese.getSelectedItem().equals("da data / a data")) {
                labAnno.setVisible(false);
                texAnno.setVisible(false);
                labDal.setVisible(true);
                texDal.setVisible(true);
                labAl.setVisible(true);
                texAl.setVisible(true);
            } else {
                labAnno.setVisible(true);
                texAnno.setVisible(true);
                labDal.setVisible(false);
                texDal.setVisible(false);
                labAl.setVisible(false);
                texAl.setVisible(false);
            }

            selezionaSituazione();
        }
    }//GEN-LAST:event_comMeseItemStateChanged

    private void comMarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comMarcaActionPerformed

        String sql;
        int id;

        if (griglia.getSelectedRowCount() == 0) {
            SwingUtils.showInfoMessage(this, "Seleziona almeno una riga prima!");
            return;
        }

        if (javax.swing.JOptionPane.showConfirmDialog(this, "Sicuro di marcare le provvigioni SELEZIONATE come Pagate ?", "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION) {

            for (int i = 0; i < this.griglia.getSelectedRowCount(); i++) {
                id = Integer.parseInt(String.valueOf(this.griglia.getValueAt(griglia.getSelectedRows()[i], griglia.getColumnByName("p_id"))));
                sql = "update provvigioni set pagata = 'S' where id = " + id;
                Db.executeSql(sql);
            }

            comAggiornaActionPerformed(null);
        }

    }//GEN-LAST:event_comMarcaActionPerformed

    private void comAggiornaSoloPagateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comAggiornaSoloPagateActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//        String sql = sqlIniziale + " and provvigioni.pagata = 'N'";
        String sql = sqlInizialeScadenze + " and provvigioni.pagata = 'N'" + getWhere(true)
                + "\n union all \n"
                + sqlInizialeFatture + " and provvigioni.pagata = 'N'" + getWhere(false);
        aggiorna(sql);
    }//GEN-LAST:event_comAggiornaSoloPagateActionPerformed

    private void comAggiornaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comAggiornaActionPerformed
        oldsql = null;
        visualizzaTutte();
    }//GEN-LAST:event_comAggiornaActionPerformed

    public void visualizzaTutte() {
        if (is_loading) {
            return;
        }

        String sql = sqlInizialeScadenze + getWhere(true) + " "
                + "\n union all \n"
                + sqlInizialeFatture + getWhere(false);

        if (!sql.equals(cu.s(oldsql))) {
            oldsql = sql;
            aggiorna(sql);
        }    
    }
    
    
    private void comAgenteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comAgenteItemStateChanged

        if (evt.getStateChange() == evt.SELECTED) {
            selezionaSituazione();
        }
    }//GEN-LAST:event_comAgenteItemStateChanged

    private void grigliaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseClicked

        // Add your handling code here:
        String serie;
        int numero;
        int anno;
        Integer p_numero = null;
        Integer p_id = null;
        Integer t_id = null;
        String pagamento;

        if (evt.getClickCount() == 2) {
            serie = String.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("serie")));
            numero = Integer.parseInt(String.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("numero"))));
            anno = Integer.parseInt(String.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("anno"))));
            p_numero = cu.toInteger(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("p_numero")));
            p_id = cu.toInteger(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("p_id")));
            t_id = cu.toInteger(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("tid")));

            ProvvigioniFattura tempProvvigioni = new ProvvigioniFattura(t_id, -1, -1);
            tempProvvigioni.p_numero = p_numero;
            tempProvvigioni.p_id = p_id;

            //frmPagaPart frm = new frmPagaPart(tempScad);
            frmDettaglioProvvigione frm = new frmDettaglioProvvigione(tempProvvigioni);

            main.getPadre().openFrame(frm, 550, 500);
        }
    }//GEN-LAST:event_grigliaMouseClicked

    private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameOpened

        // Add your handling code here:
        this.griglia.resizeColumnsPerc(true);
    }//GEN-LAST:event_formInternalFrameOpened

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosed

        // Add your handling code here:
        main.getPadre().closeFrame(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void jScrollPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jScrollPane1MouseClicked
    }//GEN-LAST:event_jScrollPane1MouseClicked

    private void comStampaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comStampaActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        String sql = sqlCorrente;
        this.griglia.dbOpen(Db.getConn(), sqlCorrente);

        prnStampaProvvigioniMensili stampa = new prnStampaProvvigioniMensili(sql, this.comAgente.getSelectedItem().toString(), this.comMese
                .getSelectedItem().toString() + " " + this.texAnno.getText());
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_comStampaActionPerformed

    private void comStampa1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comStampa1ActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            System.out.println("sqlCorrente = " + sqlCorrente);

//            JasperReport r = (JasperReport)JRLoader.loadObject("reports/provvigioni.jasper");
            File freport = new File(main.wd + "reports/provvigioni.jrxml");
            JasperReport r = Reports.getReport(freport);

            HashMap params = new HashMap();
            params.put("query", sqlCorrente);
            String note1 = "";
            note1 = main.attivazione.getDatiAzienda().getRagione_sociale();
            
//            note1 += "\n" + "Periodo: " + comMese.getSelectedItem() + " anno:" + texAnno.getText();
            note1 += "\n " + note_filtro_stampa;
            
            
            params.put("note1", note1);
            JasperPrint p = JasperFillManager.fillReport(r, params, Db.getConn());
            JasperViewer viewer = new JasperViewer(p, false);
            viewer.setVisible(true);            
        } catch (JRException ex) {
            Logger.getLogger(frmSituazioneAgenti.class.getName()).log(Level.SEVERE, null, ex);
            SwingUtils.showExceptionMessage(this, ex);
        }
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_comStampa1ActionPerformed

    private void comAggiornaSoloPagate1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comAggiornaSoloPagate1ActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//        String sql = sqlIniziale + " and provvigioni.pagata = 'S'";
//                sql += getWhere();
        String sql = sqlInizialeScadenze + " and provvigioni.pagata = 'S'" + getWhere(true)
                + "\n union all \n"
                + sqlInizialeFatture + " and provvigioni.pagata = 'S'" + getWhere(false);
        aggiorna(sql);
    }//GEN-LAST:event_comAggiornaSoloPagate1ActionPerformed

    private void comMarca1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comMarca1ActionPerformed

        String sql;
        int id;

        if (griglia.getSelectedRowCount() == 0) {
            SwingUtils.showInfoMessage(this, "Seleziona almeno una riga prima!");
            return;
        }

        if (javax.swing.JOptionPane.showConfirmDialog(this, "Sicuro di marcare le provvigioni SELEZIONATE come NON Pagate ?", "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION) {

            System.out.println("griglia    rowcount " + griglia.getRowCount());
            System.out.println("griglia selrowcount " + griglia.getSelectedRowCount());
            for (int i = 0; i < griglia.getSelectedRowCount(); i++) {
                Object value = griglia.getValueAt(griglia.getSelectedRows()[i], griglia.getColumnByName("p_id"));
                System.out.println("riga " + griglia.getSelectedRows()[i] + " col " + griglia.getColumnByName("p_id") + " value: " + value);
                id = Integer.parseInt(String.valueOf(value));
                sql = "update provvigioni set pagata = 'N' where id = " + id;
                Db.executeSql(sql);
            }

            this.comAggiornaActionPerformed(null);
        }

    }//GEN-LAST:event_comMarca1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        griglia.getSelectionModel().setSelectionInterval(0, griglia.getRowCount()-1);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        griglia.getSelectionModel().clearSelection();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void comStampa2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comStampa2ActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            System.out.println("sqlCorrente = " + sqlCorrente);

//            JasperReport r = (JasperReport)JRLoader.loadObject("reports/provvigioni_det.jasper");
//            File freport = new File(main.wd + "reports/provvigioni_det.jrxml");
            File freport = new File(main.wd + "reports/provvigioni_det2.jrxml");
            JasperReport r = Reports.getReport(freport);

            HashMap params = new HashMap();
            params.put("query", sqlCorrente);
            params.put("SUBREPORT_DIR", main.wd + "reports/");
            String note1 = "";
            note1 = main.attivazione.getDatiAzienda().getRagione_sociale();
            
//            note1 += "\n" + "Periodo: " + comMese.getSelectedItem() + " anno:" + texAnno.getText();
            note1 += "\n " + note_filtro_stampa;
            
            params.put("note1", note1);
//            JasperPrint p = JasperFillManager.fillReport(r, params,Db.getConn());
            System.out.println("params = " + params);
            JasperPrint p = JasperFillManager.fillReport(r, params, Db.getConn());
            JasperViewer viewer = new JasperViewer(p, false);
            viewer.setVisible(true);
        } catch (JRException ex) {
            Logger.getLogger(frmSituazioneAgenti.class.getName()).log(Level.SEVERE, null, ex);
            SwingUtils.showExceptionMessage(this, ex);
        }
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_comStampa2ActionPerformed

    private void texDalDocFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texDalDocFocusLost
        selezionaSituazione();
    }//GEN-LAST:event_texDalDocFocusLost

    private void texDalDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texDalDocActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_texDalDocActionPerformed

    private void texDalDocPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_texDalDocPropertyChange
        System.out.println("evt = " + evt);
    }//GEN-LAST:event_texDalDocPropertyChange

    private void texAlDocFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texAlDocFocusLost
        selezionaSituazione();
    }//GEN-LAST:event_texAlDocFocusLost

    private void texAlDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texAlDocActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_texAlDocActionPerformed

    private void texAlDocPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_texAlDocPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_texAlDocPropertyChange

    private void texDalFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texDalFocusLost
        selezionaSituazione();
    }//GEN-LAST:event_texDalFocusLost

    private void texDalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texDalActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_texDalActionPerformed

    private void texDalPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_texDalPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_texDalPropertyChange

    private void texAlFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texAlFocusLost
        selezionaSituazione();
    }//GEN-LAST:event_texAlFocusLost

    private void texAlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texAlActionPerformed
        selezionaSituazione();
    }//GEN-LAST:event_texAlActionPerformed

    private void texAlPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_texAlPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_texAlPropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private tnxbeans.tnxComboField comAgente;
    private javax.swing.JButton comAggiorna;
    private javax.swing.JButton comAggiornaSoloPagate;
    private javax.swing.JButton comAggiornaSoloPagate1;
    private javax.swing.JButton comMarca;
    private javax.swing.JButton comMarca1;
    private javax.swing.JComboBox comMese;
    private javax.swing.JButton comStampa;
    private javax.swing.JButton comStampa1;
    private javax.swing.JButton comStampa2;
    private tnxbeans.tnxDbGrid griglia;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel112;
    private javax.swing.JLabel jLabel113;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labAl;
    private javax.swing.JLabel labAnno;
    private javax.swing.JLabel labDal;
    private javax.swing.JLabel labTotale;
    private tnxbeans.tnxDbPanel panClie;
    private javax.swing.JPanel panDati;
    private org.jdesktop.swingx.JXDatePicker texAl;
    private org.jdesktop.swingx.JXDatePicker texAlDoc;
    private javax.swing.JTextField texAnno;
    private org.jdesktop.swingx.JXDatePicker texDal;
    private org.jdesktop.swingx.JXDatePicker texDalDoc;
    private javax.swing.JToolBar tooMenu;
    // End of variables declaration//GEN-END:variables
    public void dbRefresh() {
        griglia.dbRefresh();
    }

    public void selezionaSituazione() {        
        visualizzaTutte();
    }

//    public String getWhere() {
    public String getWhere(boolean per_scadenze) {
        String sql = "";
        Calendar inizio = getInizio();
        Calendar fine = getFine();
        
        note_filtro_stampa = "";
        
        if (per_scadenze) {
            if (inizio != null || fine != null) {
                note_filtro_stampa += "Per scadenze";
            }
            if (inizio != null) {
                note_filtro_stampa += " dal " + DateUtils.formatDateIta(inizio.getTime());
                sql += " and scadenze.data_scadenza >= " + it.tnx.Db2.pc2(inizio.getTime(), Types.DATE);
            }
            if (fine != null) {
                note_filtro_stampa += " al " + DateUtils.formatDateIta(fine.getTime()) + "\n";
                sql += " and scadenze.data_scadenza <= " + it.tnx.Db2.pc2(fine.getTime(), Types.DATE);
            }
        } else {
            if (inizio != null || fine != null) {
                note_filtro_stampa += "Per provvigioni";
            }            
            if (inizio != null) {
                note_filtro_stampa += " dal " + DateUtils.formatDateIta(inizio.getTime());
                sql += " and provvigioni.data_scadenza >= " + it.tnx.Db2.pc2(inizio.getTime(), Types.DATE);
            }
            if (fine != null) {
                note_filtro_stampa += " al " + DateUtils.formatDateIta(fine.getTime()) + "\n";
                sql += " and provvigioni.data_scadenza <= " + it.tnx.Db2.pc2(fine.getTime(), Types.DATE);
            }
        }
        if (inizio != null || fine != null) {
            note_filtro_stampa += "\n";
        }            
        

        if (texDalDoc.getDate() != null || texAlDoc.getDate() != null) {
            note_filtro_stampa += "Per fatture";
        }
        if (texDalDoc.getDate() != null) {
            note_filtro_stampa += " dal " + DateUtils.formatDateIta(texDalDoc.getDate());
            sql += " and test_fatt.data >= " + it.tnx.Db2.pc2(texDalDoc.getDate(), Types.DATE);
        }
        if (texAlDoc.getDate() != null) {
            note_filtro_stampa += " al " + DateUtils.formatDateIta(texAlDoc.getDate());
            sql += " and test_fatt.data <= " + it.tnx.Db2.pc2(texAlDoc.getDate(), Types.DATE);
        }
        if (texDalDoc.getDate() != null || texAlDoc.getDate() != null) {
            note_filtro_stampa += "\n";
        }

        if (!comAgente.getSelectedKey().toString().equalsIgnoreCase("*")) {
            sql += " and test_fatt.agente_codice = " + comAgente.getSelectedKey();
        }

        return sql;
    }
    
    public String getOrder() {
//        return " order by test_fatt.data, test_fatt.serie, test_fatt.numero, test_fatt.id";
        return " order by data, serie, numero, tid, `S data scadenza`";
    }

    private void aggiorna(String sql) {
//        sql += getWhere() + getOrder();
        sql += getOrder();

        System.out.println("select provvigioni:" + sql);
        sqlCorrente = sql;
        
        aggiornagriglia();
    }
    
    public void aggiornagriglia() {
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        int oldSelectedRow = griglia.getSelectedRow();
        this.griglia.dbOpen(Db.getConn(), sqlCorrente);
        
        if (griglia.getRowCount() > oldSelectedRow && oldSelectedRow >= 0 && griglia.getRowCount() > 0) {
            griglia.setRowSelectionInterval(oldSelectedRow, oldSelectedRow);
            if (!getVisibleRect().contains(griglia.getCellRect(griglia.getSelectedRow(), 1, true))) {
                griglia.scrollToRow(griglia.getSelectedRow());
            }        
        }
                
        //aggiorno il totale
        double totale = 0;
        double totalePagate = 0;
        double totaleDaPagare = 0;
        for (int i = 0; i < this.griglia.getRowCount(); i++) {
            Double d = (Double) this.griglia.getValueAt(i, griglia.getColumnByName("P provvigione"));
            if (String.valueOf(this.griglia.getValueAt(i, griglia.getColumnByName("P pagata"))).equals("S")) {
                totalePagate += d.doubleValue();
            } else {
                totaleDaPagare += d.doubleValue();
            }
            totale += d.doubleValue();
        }
        this.labTotale.setText("Totale " + it.tnx.Util.EURO + " " + it.tnx.Util.formatValutaEuro(totale)
                + " / Gia' Pagate " + it.tnx.Util.EURO + " " + it.tnx.Util.formatValutaEuro(totalePagate)
                + " / Da Pagare " + it.tnx.Util.EURO + " " + it.tnx.Util.formatValutaEuro(totaleDaPagare));

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));    
    }
}
