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
package gestioneFatture;

import gestioneFatture.logic.documenti.Documento;
import it.tnx.accessoUtenti.Permesso;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.FxUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.commons.ju;
import it.tnx.gui.MyBasicArrowButton;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.MyAbstractListIntelliHints;
import it.tnx.invoicex.iu;
import it.tnx.invoicex.sync.Sync;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.sql.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicArrowButton;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import tnxbeans.LimitedTextPlainDocument;
import tnxbeans.tnxComboField;
import tnxbeans.tnxDbPanel;
import tnxbeans.tnxTextField;

/**
 *
 *
 *
 *
 *
 * @author marco
 *
 *
 */
public class frmTestFattAcquisto
        extends javax.swing.JInternalFrame
        implements GenericFrmTest {

    public dbFatturaRicevuta dbdoc = new dbFatturaRicevuta();
    public Documento doc = new Documento();
    public frmElenFattAcquisto from;
    private Db db = Db.INSTANCE;
    int pWidth;
    int pHeight;
    public String dbStato = "L";
    public static String DB_INSERIMENTO = "I";
    public static String DB_MODIFICA = "M";
    public static String DB_LETTURA = "L";
    private String sql = "";
    private double totaleIniziale;
    private String pagamentoIniziale;
    private String pagamentoInizialeGiorno;    //private int tempTipoFatt = 0;
    //per controllare le provvigioni
    private double provvigioniIniziale;
    private int codiceAgenteIniziale;
    private String old_id = "";
    private boolean id_modificato = false;
    private String old_anno = "";
    private String old_data = "";
    private boolean anno_modificato = false;
    private int comClieSel_old = -1;
    public Integer id = null;
    public boolean in_apertura = false;
    private String serie_originale = null;
    private Integer numero_originale = null;
    private Integer anno_originale = null;
    private String data_originale = null;

    MyAbstractListIntelliHints al_clifor = null;
    AtomicReference<ClienteHint> clifor_selezionato_ref = new AtomicReference(null);

    public boolean loading = true;

    public tnxbeans.tnxComboField deposito;
    public javax.swing.JLabel labDeposito;
    public tnxDbPanel pan_deposito = null;

    boolean chiudere = true;

    String suff = "_acquisto";   //qui sempre di acquisto
    
    public String table_righe_temp = null;
    public String table_righe_lotti_temp = null;
    public String table_righe_matricole_temp = null;
    

    /**
     * Creates new form frmElenPrev
     */
    public frmTestFattAcquisto(String dbStato, String dbSerie, int dbNumero, String prevStato, int dbAnno, int tipoFattura, int dbIdFatt) {

        in_apertura = true;
        
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        this.dbStato = dbStato;
        this.id = dbIdFatt;

        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setDismissDelay(30000);

        //this.tempTipoFatt = tipoFattura;
        initComponents();
        
        if (main.versione.equalsIgnoreCase("base")) {
            menColAggNote.setEnabled(false);
        } else {
            menColAggNote.setIcon(null);
            menColAggNote.setEnabled(true);
            menColAggNote.setToolTipText(null);
        }        

        //campi liberi
        InvoicexUtil.initCampiLiberiTestate(this);

        LimitedTextPlainDocument limit = new LimitedTextPlainDocument(1, true);
        texSeri.setDocument(limit);

        if (!main.getPersonalContain("cirri")) {
            butImportXlsCirri.setVisible(false);
        }

//        if(!main.utente.getPermesso(Permesso.PERMESSO_FATTURE_VENDITA, Permesso.PERMESSO_TIPO_SCRITTURA)){
//            SwingUtils.showErrorMessage(main.getPadrePanel(), "Non hai i permessi per accedere a questa funzionalità", "Impossibile accedere", true);
//            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//            this.setVisible(false);
//            this.dispose();
//            return;
//        }
        prezzi_ivati.setVisible(false);

        comClie.putClientProperty("JComponent.sizeVariant", "small");
        comPaga.putClientProperty("JComponent.sizeVariant", "small");

        if (SystemUtils.IS_OS_MAC_OSX) {
//            tutto.setBorder(new MyOsxFrameBorder());

            Border b1 = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.lightGray), BorderFactory.createEmptyBorder(2, 2, 2, 2));
            ArrayList<JComponent> compsb = new ArrayList();
            compsb.add(texSeri);
            compsb.add(texNume);
            compsb.add(texData);
            compsb.add(texCliente);
            compsb.add(texScon1);
            compsb.add(texScon2);
            compsb.add(texScon3);
            compsb.add(texSpeseTrasporto);
            compsb.add(texSpeseIncasso);
            compsb.add(texNote);
            
            compsb.add((JComponent)comPaga.getEditor().getEditorComponent());
            compsb.add(texGiornoPagamento);
            
            compsb.add(texSeri1);
            compsb.add(texNumePrev2);
            compsb.add(texData1);
            compsb.add(texNote1);
            
            for (JComponent c : compsb) {
                c.setBorder(b1);
            }
            
        }        
        
        texNote.setFont(texSeri.getFont());
        texNote1.setFont(texSeri.getFont());

        texCliente.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                texCliente.selectAll();
            }
        });

        boolean filtraclifor = main.fileIni.getValueBoolean("pref", "filtraCliFor", false);
        InvoicexUtil.CliforTipo tipo = InvoicexUtil.CliforTipo.Tutti;
        if (filtraclifor) {        
            tipo = InvoicexUtil.CliforTipo.Solo_Fornitori_Entrambi_Provvisori;
        }
        al_clifor = InvoicexUtil.getCliforIntelliHints(texCliente, this, clifor_selezionato_ref, null, texScon1, tipo);
        al_clifor.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("selezionato")) {
                    ClienteHint hint = (ClienteHint) clifor_selezionato_ref.get();
                    if (hint != null) {
                        texClie.setText(hint.codice);
                    } else {
                        texClie.setText("");
                    }
                    comClie.dbTrovaKey(texClie.getText());
                    selezionaCliente();
                }
            }
        });

        if (main.getPersonalContain("carburante")) {
            jLabel114.setText("spese carburante");
        }

//        griglia.setNoTnxResize(true);

        try {
            InvoicexEvent event = new InvoicexEvent(this);
            event.type = InvoicexEvent.TYPE_FRMTESTFATTACQUISTO_CONSTR_POST_INIT_COMPS;
            main.events.fireInvoicexEvent(event);
            if (deposito == null) {
                tabDocumento.remove(datiAltro);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        this.griglia.dbEditabile = false;

        //init campi particolari
        this.texData.setDbDefault(texData.DEFAULT_CURRENT);

        //oggetto preventivo
        this.dbdoc.dbStato = dbStato;
        this.dbdoc.serie = dbSerie;
        this.dbdoc.numero = dbNumero;
        this.dbdoc.stato = prevStato;
        this.dbdoc.anno = dbAnno;
        this.dbdoc.setId(id);

        //105
        this.dbdoc.tipoFattura = tipoFattura;
        this.dbdoc.texTota = this.texTota;
        this.dbdoc.texTotaImpo = this.texTotaImpo;
        this.dbdoc.texTotaIva = this.texTotaIva;
        dbdoc.tipoDocumento = Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA;
//        this.setClosable(false);

        this.comPaga.dbOpenList(db.getConn(), "select codice, codice from pagamenti order by codice", null, false);

        //faccio copia in caso di annulla deve rimettere le righe giuste
        if (!main.edit_doc_in_temp) {
            if (dbStato.equals(frmTestFatt.DB_MODIFICA)) {
                porto_in_temp();
                //memorizzo il numero doc originale
                serie_originale = dbSerie;
                numero_originale = dbNumero;
                anno_originale = dbAnno;
            }
        } else {
            //porto righe in tabella temporanea e modifica quella temporanea, se poi si conferma le porto nelle righe definitive
            table_righe_temp = InvoicexUtil.getTempTableName("righ_fatt" + suff);
            table_righe_lotti_temp = InvoicexUtil.getTempTableName("righ_fatt" + suff + "_lotti");
            table_righe_matricole_temp = InvoicexUtil.getTempTableName("righ_fatt" + suff + "_matricole");
            try {
                InvoicexUtil.createTempTable(dbStato, table_righe_temp, "righ_fatt" + suff, id);
                InvoicexUtil.createTempTable(dbStato, table_righe_lotti_temp, "righ_fatt" + suff + "_lotti", id);
                InvoicexUtil.createTempTable(dbStato, table_righe_matricole_temp, "righ_fatt" + suff + "_matricole", id);
                dbdoc.table_righe_temp = table_righe_temp;
                doc.table_righe_temp = table_righe_temp;
                if (dbStato.equalsIgnoreCase(this.DB_INSERIMENTO)) {
                    dbdoc.setId(-1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
            }
        }        
        
        
        
        
        
        
        

        //this.texSeri.setVisible(false);
        //associo il panel ai dati
        this.dati.dbNomeTabella = "test_fatt_acquisto";

        dati.dbChiaveAutoInc = true;

        dati.messaggio_nuovo_manuale = true;

//        Vector chiave = new Vector();
//        chiave.add("serie");
//        chiave.add("numero");
//        chiave.add("anno");
//        this.dati.dbChiave = chiave;
        Vector chiave = new Vector();
        chiave.add("id");
        dati.dbChiave = chiave;

        dati.aggiungiDbPanelCollegato(datiAltro);

        //this.dati.butUndo = this.butUndo;
        //controllo se inserimento o modifica
        if (dbStato.equalsIgnoreCase(this.DB_INSERIMENTO)) {
            this.dati.dbOpen(db.getConn(), "select * from test_fatt_acquisto limit 0");
        } else {
            sql = "select * from test_fatt_acquisto";
//            sql += " where serie = " + db.pc(dbSerie, "VARCHAR");
//            sql += " and numero = " + dbNumero;
//            sql += " and anno = " + dbAnno;
            sql += " where id = " + id;
            this.dati.dbOpen(db.getConn(), sql);
        }

        //105 metto titolo finestra per sapere se fattura o altro
        if (dbdoc.tipoFattura == dbFattura.TIPO_FATTURA_NON_IDENTIFICATA) {
            sql = "select tipo_fattura from test_fatt_acquisto";
            sql += " where id = " + this.id;
            System.err.println("dbopen tipo_fattura:" + sql);
            try {
                dbdoc.tipoFattura = CastUtils.toInteger(DbUtils.getObject(Db.getConn(), sql));
            } catch (Exception e) {
            }
        }

        setTipoFattura(dbdoc.tipoFattura);

        //apro la combo clienti
        this.comClie.setDbTextAbbinato(this.texClie);
        this.texClie.setDbComboAbbinata(this.comClie);
        this.comClie.dbOpenList(db.getConn(), "select ragione_sociale,codice from clie_forn where ragione_sociale != '' order by ragione_sociale", this.texClie.getText());

        this.dati.dbRefresh();
        this.dbdoc.dbRefresh();

        //righe
        //apro la griglia
        griglia.dbNomeTabella = getNomeTabRighe();

        java.util.Hashtable colsWidthPerc = new java.util.Hashtable();
        colsWidthPerc.put("serie", new Double(0));
        colsWidthPerc.put("numero", new Double(0));
        colsWidthPerc.put("anno", new Double(0));
        colsWidthPerc.put("riga", new Double(5));
        colsWidthPerc.put("articolo", new Double(15));
        colsWidthPerc.put("descrizione", new Double(45));
        colsWidthPerc.put("um", new Double(5));
        colsWidthPerc.put("quantita", new Double(10));
        colsWidthPerc.put("prezzo", new Double(12));
        colsWidthPerc.put("Totale", new Double(10));
        colsWidthPerc.put("Ivato", new Double(10));
        colsWidthPerc.put("sconto1", new Double(0));
        colsWidthPerc.put("sconto2", new Double(0));
        colsWidthPerc.put("iva", new Double(0));
        colsWidthPerc.put("id", new Double(0));
        if (main.isPluginContabilitaAttivo()) {
            colsWidthPerc.put("conto", new Double(10));
        }
        if (main.fileIni.getValueBoolean("pref", "ColAgg_righe_note", false)) {
            colsWidthPerc.put("note", 15d);
        }                

        this.griglia.columnsSizePerc = colsWidthPerc;

        java.util.Hashtable colsAlign = new java.util.Hashtable();
        colsAlign.put("quantita", "RIGHT_CURRENCY");
        colsAlign.put("prezzo", "RIGHT_CURRENCY");
        this.griglia.columnsAlign = colsAlign;
        this.griglia.flagUsaOrdinamento = false;

        //        Vector chiave2 = new Vector();
        //        chiave2.add("serie");
        //        chiave2.add("numero");
        //        chiave2.add("anno");
        //        chiave2.add("riga");
        Vector chiave2 = new Vector();
        chiave2.add("id");
        this.griglia.dbChiave = chiave2;

        //this.griglia.dbPanel=this.dati;
        //controllo come devo aprire
        if (dbStato.equalsIgnoreCase(DB_INSERIMENTO)) {
            inserimento();
            InvoicexUtil.fireEvent(this, InvoicexEvent.TYPE_FRMTESTFATT_ACQUISTO_INIT_INSERIMENTO);
        } else {

            //disabilito la data perch??? non tornerebbe pi??? la chiave per numero e anno
            //siccome tutti vogliono modificarsi la data che se la modifichino...
            //this.texData.setEditable(false);
            this.dbdoc.sconto1 = Db.getDouble(this.texScon1.getText());
            this.dbdoc.sconto2 = Db.getDouble(this.texScon2.getText());
            this.dbdoc.sconto3 = Db.getDouble(this.texScon3.getText());

            //this.prev.speseVarie = Db.getDouble(this.texSpesVari.getText());
            this.dbdoc.speseTrasportoIva = Db.getDouble(this.texSpeseTrasporto.getText());
            this.dbdoc.speseIncassoIva = Db.getDouble(this.texSpeseIncasso.getText());
            dopoInserimento();

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    texCliente.requestFocus();
                }
            });
        }

        if (dbStato.equalsIgnoreCase(DB_INSERIMENTO)) {
            texSconto.setText("0");
        } else {
            texSconto.setText(FormatUtils.formatEuroIta(CastUtils.toDouble0(dati.dbGetField("sconto"))));
            ricalcolaTotali();
        }

//        java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);
//        boolean azioniPericolose = preferences.getBoolean("azioniPericolose", false);
        boolean azioniPericolose = main.fileIni.getValueBoolean("pref", "azioniPericolose", true);

        if (azioniPericolose) {
            texNume.setEditable(true);
            texData.setEditable(true);
        }

        prezzi_ivati_virtual.setSelected(prezzi_ivati.isSelected());

        Menu m = (Menu) main.getPadre();

        data_originale = texData1.getText();
        
        loading = false;
        ricalcolaTotali();

        //allegati 
        InvoicexEvent evt = new InvoicexEvent(frmTestFattAcquisto.this, InvoicexEvent.TYPE_AllegatiInit);
        evt.args = new Object[]{tabDocumento};
        main.events.fireInvoicexEvent(evt);

        InvoicexUtil.fireEvent(frmTestFattAcquisto.this, InvoicexEvent.TYPE_AllegatiCarica, dati.dbNomeTabella, id);
        
        in_apertura = false;

        //        org.jscroll.JScrollDesktopPane desk = (org.jscroll.JScrollDesktopPane)m.getDesktopPane();
        //        desk.add(zoom, JDesktopPane.PALETTE_LAYER);
        //        zoom.setVisible(false);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

    }

    private void inserimento() {
        this.dati.dbNew();

        //prendo base da impostazioni
        boolean prezzi_ivati_b = false;
        try {
            String prezzi_ivati_s = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select l.prezzi_ivati from dati_azienda a join tipi_listino l on a.listino_base = l.codice"));
            if (prezzi_ivati_s.equalsIgnoreCase("S")) {
                prezzi_ivati_b = true;
            }
            prezzi_ivati.setSelected(prezzi_ivati_b);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //controllo serie default
        if (Db.getSerieDefault().length() > 0) {
            texSeri.setText(Db.getSerieDefault());
        } else {
            texSeri.setText(dbdoc.serie);
        }

        if (main.iniSerie == false) {
            assegnaNumero();
            dopoInserimento();
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    texCliente.requestFocus();
                }
            });
        } else {

            //disabilitare tutto prima
            Component[] cs = this.dati.getComponents();

            for (int i = 0; i < cs.length; i++) {
                cs[i].setEnabled(false);

                if (cs[i] instanceof tnxbeans.tnxComboField) {

                    tnxbeans.tnxComboField combo = (tnxbeans.tnxComboField) cs[i];
                    combo.setEditable(false);
                    combo.setLocked(true);
                }
            }

            this.texSeri.setToolTipText("Inserisci la serie e premi Invio per confermarla ed assegnare un numero al documento");
            this.texSeri.setEnabled(true);
            this.texSeri.setEditable(true);
            this.texSeri.setBackground(java.awt.Color.RED);
            javax.swing.SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    texSeri.requestFocus();
                }
            });
        }
    }

    private void assegnaSerie() {
        this.texSeri.setText(texSeri.getText().toUpperCase());
        assegnaNumero();

        //riabilito
        Component[] cs = this.dati.getComponents();

        for (int i = 0; i < cs.length; i++) {
            cs[i].setEnabled(true);

            if (cs[i] instanceof tnxbeans.tnxComboField) {

                tnxbeans.tnxComboField combo = (tnxbeans.tnxComboField) cs[i];
                combo.setEditable(true);
                combo.setLocked(false);
            }
        }

        dopoInserimento();
        texCliente.requestFocus();
    }

    private void assegnaNumero() {

        //metto ultimo numero preventivo + 1
        //apre il resultset per ultimo +1
        java.sql.Statement stat;
        ResultSet resu;

        try {
            stat = db.getConn().createStatement();

            int myanno = java.util.Calendar.getInstance().get(Calendar.YEAR);
            String sql = "select numero from test_fatt_acquisto";
            if (InvoicexUtil.getTipoNumerazione() == InvoicexUtil.TIPO_NUMERAZIONE_ANNO_INFINITA && myanno >= 2013) {
                sql += " where anno >= 2013";
            } else {
                sql += " where anno = " + myanno;
            }
            sql += " and serie = " + Db.pc(texSeri.getText(), Types.VARCHAR);
            sql += " order by numero desc limit 1";
            resu = stat.executeQuery(sql);

            if (resu.next() == true) {
                this.texNume.setText(String.valueOf(resu.getInt(1) + 1));
            } else {
                this.texNume.setText("1");
            }

            this.texTipoFattura.setText(String.valueOf(this.dbdoc.tipoFattura));
            this.texAnno.setText(String.valueOf(java.util.Calendar.getInstance().get(Calendar.YEAR)));

            //-----------------------------------------------------------------
            //se apre in inserimento gli faccio subito salvare la testa
            //se poi la annulla vado ad eliminare
            //appoggio totali
            this.texTota1.setText(this.texTota.getText());
            this.texTotaIva1.setText(this.texTotaIva.getText());
            this.texTotaImpo1.setText(this.texTotaImpo.getText());

//            texClie.setText("0");
            if (this.dati.dbStato.equals(DB_INSERIMENTO)) {
                try {
                    String tmpSerie = this.texSeri.getText();
                    Integer numero = Integer.parseInt(texNume.getText());
                    Integer anno = Integer.parseInt(texAnno.getText());

                    String tmpSql = "select * from test_fatt_acquisto where serie = '" + tmpSerie + "' and anno = " + anno + " and numero = " + numero;
                    ResultSet tmpControl = Db.openResultSet(tmpSql);

                    if (tmpControl.next()) {
                        JOptionPane.showMessageDialog(this, "Un' altra fattura con lo stesso gruppo numero - serie - anno è già stata inserita!", "Impossibile inserire dati", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            

            if (!main.edit_doc_in_temp) {            
                if (this.dati.dbSave() == true) {
                    //richiamo il refresh della maschera che lo ha lanciato
                    if (from != null) {
                        frmElenFattAcquisto temp = (frmElenFattAcquisto) from;
                        temp.dbRefresh();
                    }
                }
                texClie.setText("");
                
                this.id = (Integer) dati.last_inserted_id;
                this.dbdoc.setId(id);
                if (dbStato.equalsIgnoreCase(tnxDbPanel.DB_INSERIMENTO)) {
                    InvoicexUtil.checkLock(dati.dbNomeTabella, id, false, null);
                }
                InvoicexUtil.checkLockAddFrame(frmTestFattAcquisto.this, dati.dbNomeTabella, id);
            }

            this.dbdoc.serie = this.texSeri.getText();
            this.dbdoc.stato = "P";
            this.dbdoc.numero = new Integer(this.texNume.getText()).intValue();
            this.dbdoc.anno = java.util.Calendar.getInstance().get(Calendar.YEAR);

            if (!main.edit_doc_in_temp) {
                this.dati.dbCambiaStato(this.dati.DB_LETTURA);
            }

            this.texSeri.setEditable(false);
            this.texSeri.setBackground(this.texNume.getBackground());

            try {
                texNote.setText(main.fileIni.getValue("pref", "noteStandardFattAcquisto"));
            } catch (Exception err) {
                err.printStackTrace();
            }
            //Fine
        } catch (Exception err) {
            err.printStackTrace();
        }

        dati.dbCheckModificatiReset();
    }

    private void dopoInserimento() {
        
        controllaPermessiAnagCliFor();
        
        dbAssociaGrigliaRighe();
        doc.load(Db.INSTANCE, this.dbdoc.numero, this.dbdoc.serie, this.dbdoc.anno, Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, id);
        dbdoc.setId(id);

        if (comClie.getText().trim().length() == 0) {
            try {
                String cliente = (String) DbUtils.getObject(Db.getConn(), "select ragione_sociale from clie_forn where codice = " + Db.pc(texClie.getText(), Types.INTEGER));
                texCliente.setText(cliente);
            } catch (Exception e) {
            }
        } else {
            texCliente.setText(comClie.getText());
        }

        //apro combo banche
        trovaAbi();
        trovaCab();

//        timerRefreshFattura timTest = new timerRefreshFattura(this, doc);
//        tim.schedule(timTest, 1000, 500);
        //rinfresco il discorso extra cee
        try {

            if (this.texClie.getText().length() > 0) {
                this.dbdoc.forceCliente(Long.parseLong(this.texClie.getText()));
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        comPagaItemStateChanged(null);
        
        //memorizzo totale iniziale, se cambia rigenreo lengthscadenze
        dbdoc.dbRefresh();

        this.totaleIniziale = this.dbdoc.totale;
        this.pagamentoIniziale = this.comPaga.getText();
        this.pagamentoInizialeGiorno = this.texGiornoPagamento.getText();

        try {
            InvoicexEvent event = new InvoicexEvent(this);
            event.type = InvoicexEvent.TYPE_FRMTESTFATT_ACQUISTO_DOPO_INSERIMENTO;
            main.events.fireInvoicexEvent(event);
        } catch (Exception err) {
            err.printStackTrace();
        }

        ricalcolaTotali();
    }

    private void segnalaDuplicato() {
        if (!checkDuplicato()) {
            //SwingUtils.showErrorMessage(this, "Esiste già un documento esterno che corrisponde ai dati indicati!", "Controllo inserimento dati");
            Point p = texNumePrev2.getLocationOnScreen();
            p.translate(0, texNumePrev2.getHeight() + 2);
            SwingUtils.showFlashMessage2("Esiste già un documento esterno che corrisponde ai dati indicati!", 5, p, Color.red);
        }
    }

    private boolean checkDuplicato() {
        String fornitore = this.texClie.getText();
        String numero = this.texNumePrev2.getText();
        String data = this.texData1.getText();
        String serie = this.texSeri1.getText();

        System.out.println("Fornitore: " + fornitore);
        System.out.println("Numero: " + numero);
        System.out.println("Data: " + data);
        System.out.println("Serie: " + serie);

        if (!fornitore.equals("") && !numero.equals("") && !data.equals("")) {
            String anno = getAnnoFornitoreDaForm();
            System.out.println("Anno: " + anno);

            // Controllo se esistono fatture di acquisto già inserite per questo fornitore
            String sql = "SELECT id FROM test_fatt_acquisto WHERE"
                    + " fornitore = " + db.pc(fornitore, Types.INTEGER) + " AND"
                    + " numero_doc = " + db.pc(numero, Types.INTEGER) + " AND"
                    + " serie_doc = " + db.pc(serie, Types.CHAR) + " AND"
                    + " YEAR(data_doc) = " + db.pc(anno, Types.INTEGER) + " AND"
                    + " id != " + db.pc(this.id, Types.INTEGER) + " AND"
                    + " tipo_fattura = " + db.pc(dbdoc.tipoFattura, Types.INTEGER);

            try {
                Integer id_duplicato = CastUtils.toInteger(DbUtils.getObject(Db.getConn(), sql));
                if (id_duplicato > 0) {
                    return false;
                }
            } catch (Exception e) {
                return true;
            }
        }

        return true;
    }

    private void setTipoFattura(int tipoFattura) {

        //imposto il titolo
        if (tipoFattura == dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO) {
            this.setTitle("NOTA DI CREDITO");
        } else {
            this.setTitle("FATTURA");
        }

    }

    private void listiniTicket() {
    }

    private boolean saveDocumento() {

        //aggiorno totali
        try {

            if (texClie.getText() != null && texClie.getText().length() > 0) {
                doc.setCodiceCliente(Long.parseLong(texClie.getText()));
            }

            doc.setScontoTestata1(Db.getDouble(texScon1.getText()));
            doc.setScontoTestata2(Db.getDouble(texScon2.getText()));
            doc.setScontoTestata3(Db.getDouble(texScon3.getText()));
            doc.setSpeseIncasso(Db.getDouble(texSpeseIncasso.getText()));
            doc.setSpeseTrasporto(Db.getDouble(texSpeseTrasporto.getText()));
            doc.setPrezziIvati(prezzi_ivati.isSelected());
            doc.setSconto(Db.getDouble(texSconto.getText()));
            doc.calcolaTotali();

            doc.visualizzaCastellettoIva();

            texTota.setText(it.tnx.Util.formatValutaEuro(doc.getTotale()));
            texTotaImpo.setText(it.tnx.Util.formatValutaEuro(doc.getTotaleImponibile()));
            texTotaIva.setText(it.tnx.Util.formatValutaEuro(doc.getTotaleIva()));

        } catch (Exception err) {
            err.printStackTrace();
        }

        //sposto i totali di modo che li salvi
        this.texTota1.setText(this.texTota.getText());
        this.texTotaImpo1.setText(this.texTotaImpo.getText());
        this.texTotaIva1.setText(this.texTotaIva.getText());

        //salvo altrimenti genera le scadenze sull'importo vuoto
        try {
            InvoicexEvent event = new InvoicexEvent(this);
            event.type = InvoicexEvent.TYPE_FRMTESTFATT_ACQUISTO_PRIMA_DI_SAVE;
            main.events.fireInvoicexEvent(event);
        } catch (Exception err) {
            err.printStackTrace();
        }

        //se non c'è plugin ritenute azzero totale_da_pagare
        Hashtable datiagg = new Hashtable();
        if (dati.getCampiAggiuntivi() != null) {
            datiagg = dati.getCampiAggiuntivi();
        }
        if (!main.pluginRitenute) {
            datiagg.put("totale_ritenuta", "null");
            datiagg.put("totale_da_pagare", "null");
            datiagg.put("totaleRivalsa", "null");
        }

        datiagg.put("sconto", Db.pc(doc.getSconto(), Types.DOUBLE));
        datiagg.put("totale_imponibile_pre_sconto", Db.pc(doc.totaleImponibilePreSconto, Types.DOUBLE));
        datiagg.put("totale_ivato_pre_sconto", Db.pc(doc.totaleIvatoPreSconto, Types.DOUBLE));
        datiagg.put("totale_da_pagare_finale", Db.pc(doc.getTotale_da_pagare_finale(), Types.DOUBLE));

        dati.setCampiAggiuntivi(datiagg);

        //salvo altrimenti genera le scadenze sull'importo vuoto
        if (!main.edit_doc_in_temp) {
            this.dati.dbSave();
            InvoicexUtil.aggiornaAnnoDaData(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, id);
        } else {
            Sync.saveDoc(suff, texSeri.getText(), texNume.getText(), texAnno.getText(), id, this, dati, table_righe_temp, table_righe_lotti_temp, table_righe_matricole_temp);
            if (dati.id != null) {  //in inserimento viene avvalorato con il nuovo id di testata
                id = dati.id;
            }
        }
        
        dbdoc.setId(id);

        //genero le scadenze
        Scadenze tempScad = new Scadenze(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, id, this.comPaga.getText(), CastUtils.toDate(texData1.getText()));
        boolean scadenzeRigenerate = false;
        if (doc.getTotale() != this.totaleIniziale
                || !this.pagamentoInizialeGiorno.equals(this.texGiornoPagamento.getText())
                || !this.pagamentoIniziale.equals(this.comPaga.getText())
                || !data_originale.equalsIgnoreCase(texData1.getText())) {
            tempScad.generaScadenze();
            scadenzeRigenerate = true;
            if (!dbStato.equals(this.DB_INSERIMENTO)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Sono state rigenerate le scadenze perche' il totale od il pagamento e' stato variato", "Attenzione", javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        }

        //109 se fattura accompagnatoria devo generare i movimenti
        //        dbPreventivo tempPrev = new dbPreventivo();
        //        tempPrev.serie = prev.serie;
        //        tempPrev.numero = prev.numero;
        //        tempPrev.stato = prev.stato;
        //        tempPrev.anno = prev.anno;
        //        tempPrev.tipoDocumento = Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA;
        //cerco se fattura da conversione da ddt
        int conta = 0;
        if (id != null) {
            dbdoc.setId(id);
            try {
                String sql = "select count(*) from righ_ddt_acquisto "
                        + " where in_fatt = " + id;
                ResultSet r = Db.openResultSet(sql);
                if (r.next()) {
                    conta = r.getInt(1);
                }
            } catch (SQLException sqlerr) {
                sqlerr.printStackTrace();
            }
        }

        boolean azzerare = false;
        boolean generare = false;

        System.out.println(dbdoc + " " + dbdoc.numero + " / " + dbdoc.anno);
        int tipo_fattura = Integer.parseInt(texTipoFattura.getText());

        if (tipo_fattura == dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO) {
            if (SwingUtils.showYesNoMessage(this, "Vuoi generare i movimenti di scarico magazzino per questa nota di credito ?")) {
                generare = true;
            } else {
                azzerare = true;
            }
        } else if (conta == 0) {
            if (SwingUtils.showYesNoMessage(this, "Vuoi generare i movimenti di carico magazzino per questa fattura ?\nNel caso sia stato gia' inserito il DDT di Acquisto non devono essere generati.")) {
                generare = true;
            } else {
                azzerare = true;
            }
        } else {
            azzerare = true;
        }
        if (generare) {
            if (dbdoc.generaMovimentiMagazzino() == false) {
                SwingUtils.showErrorMessage(main.getPadreWindow(), "Errore nella generazione dei movimenti di magazzino");
            }
        }
        if (azzerare) {
            if (dbdoc.azzeraMovimentiMagazzino() == true) {
            } else {
                SwingUtils.showErrorMessage(main.getPadreWindow(), "Errore nell'azzeramento dei movimenti di magazzino");
            }
        }

        //aggiorno eventuali documenti collegati (ordini, ddt)
        InvoicexUtil.aggiornaRiferimentoDocumenti(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, id);

        if (from != null) {
            from.dbRefresh();
        }

        InvoicexEvent event = new InvoicexEvent(this);
        event.type = InvoicexEvent.TYPE_FRMTESTFATT_ACQUISTO_DOPO_SAVE;
        main.events.fireInvoicexEvent(event);

        event = new InvoicexEvent(this);
        event.type = InvoicexEvent.TYPE_AllegatiSalva;
        event.args = new Object[]{dati.dbNomeTabella, id};
        try {
            Object ret = main.events.fireInvoicexEventWResult(event);
            if (ret != null && ret instanceof Boolean && ((Boolean) ret)) {
                chiudere = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private void caricaDestinazioneDiversa() {
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popGrig = new javax.swing.JPopupMenu();
        popGrigModi = new javax.swing.JMenuItem();
        popGrigElim = new javax.swing.JMenuItem();
        popGridAdd = new javax.swing.JMenuItem();
        popDuplicaRighe = new javax.swing.JMenuItem();
        menColAgg = new javax.swing.JMenu();
        menColAggNote = new javax.swing.JCheckBoxMenuItem();
        popFoglio = new javax.swing.JPopupMenu();
        popFoglioElimina = new javax.swing.JMenuItem();
        foglio3 = new javax.swing.JTable();
        menClientePopup = new javax.swing.JPopupMenu();
        menClienteNuovo = new javax.swing.JMenuItem();
        menClienteModifica = new javax.swing.JMenuItem();
        tabDocumento = new javax.swing.JTabbedPane();
        panDati = new javax.swing.JPanel();
        split = new javax.swing.JSplitPane();
        dati = new tnxbeans.tnxDbPanel();
        texNume = new tnxbeans.tnxTextField();
        texClie = new tnxbeans.tnxTextField();
        texClie.setVisible(false);
        texSpeseIncasso = new tnxbeans.tnxTextField();
        texScon2 = new tnxbeans.tnxTextField();
        texScon1 = new tnxbeans.tnxTextField();
        comClie = new tnxbeans.tnxComboField();
        comClie.setVisible(false);
        texTotaImpo1 = new tnxbeans.tnxTextField();
        texTotaImpo1.setVisible(false);
        texTotaIva1 = new tnxbeans.tnxTextField();
        texTotaIva1.setVisible(false);
        texTota1 = new tnxbeans.tnxTextField();
        texTota1.setVisible(false);
        texNote = new tnxbeans.tnxMemoField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        labScon1 = new javax.swing.JLabel();
        labScon2 = new javax.swing.JLabel();
        jLabel113 = new javax.swing.JLabel();
        texData = new tnxbeans.tnxTextField();
        jLabel11 = new javax.swing.JLabel();
        texScon3 = new tnxbeans.tnxTextField();
        labScon21 = new javax.swing.JLabel();
        jLabel151 = new javax.swing.JLabel();
        texSeri = new tnxbeans.tnxTextField();
        texAnno = new tnxbeans.tnxTextField();
        texAnno.setVisible(false);
        texSpeseTrasporto = new tnxbeans.tnxTextField();
        jLabel114 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        texSeri1 = new tnxbeans.tnxTextField();
        texNumePrev2 = new tnxbeans.tnxTextField();
        texData1 = new tnxbeans.tnxTextField();
        jLabel12 = new javax.swing.JLabel();
        texNote1 = new tnxbeans.tnxMemoField();
        jLabel20 = new javax.swing.JLabel();
        comPaga = new tnxbeans.tnxComboField();
        butScad = new javax.swing.JButton();
        labGiornoPagamento = new javax.swing.JLabel();
        texGiornoPagamento = new tnxbeans.tnxTextField();
        prezzi_ivati = new tnxbeans.tnxCheckBox();
        texCliente = new javax.swing.JTextField();
        apriclienti = new MyBasicArrowButton(BasicArrowButton.SOUTH, UIManager.getColor("ComboBox.buttonBackground"), UIManager.getColor("ComboBox.buttonShadow"), UIManager.getColor("ComboBox.buttonDarkShadow"), UIManager.getColor("ComboBox.buttonHighlight"));
        texTipoFattura = new tnxbeans.tnxTextField();
        texTipoFattura.setVisible(false);
        butAddClie = new javax.swing.JButton();
        datiRighe = new tnxbeans.tnxDbPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        griglia = new tnxbeans.tnxDbGrid();
        jPanel1 = new javax.swing.JPanel();
        prezzi_ivati_virtual = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        butNuovArti = new javax.swing.JButton();
        butImportRighe = new javax.swing.JButton();
        butImportXlsCirri = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        datiAltro = new tnxbeans.tnxDbPanel();
        pan_segnaposto_deposito = new javax.swing.JPanel();
        labCampoLibero1 = new javax.swing.JLabel();
        comCampoLibero1 = new tnxbeans.tnxComboField();
        jPanel5 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        butUndo = new javax.swing.JButton();
        butSave = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        panRitenute = new javax.swing.JPanel();
        texTotaImpo = new tnxbeans.tnxTextField();
        texTotaIva = new tnxbeans.tnxTextField();
        texTota = new tnxbeans.tnxTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        texSconto = new tnxbeans.tnxTextField();

        popGrigModi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/apps/accessories-text-editor.png"))); // NOI18N
        popGrigModi.setText("modifica riga");
        popGrigModi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popGrigModiActionPerformed(evt);
            }
        });
        popGrig.add(popGrigModi);

        popGrigElim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/places/user-trash.png"))); // NOI18N
        popGrigElim.setText("elimina");
        popGrigElim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popGrigElimActionPerformed(evt);
            }
        });
        popGrig.add(popGrigElim);

        popGridAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/list-add.png"))); // NOI18N
        popGridAdd.setLabel("Aggiungi Riga");
        popGridAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popGridAddActionPerformed(evt);
            }
        });
        popGrig.add(popGridAdd);

        popDuplicaRighe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-copy.png"))); // NOI18N
        popDuplicaRighe.setText("Duplica");
        popDuplicaRighe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popDuplicaRigheActionPerformed(evt);
            }
        });
        popGrig.add(popDuplicaRighe);

        menColAgg.setText("Colonne Aggiuntive");

        menColAggNote.setText("Note");
        menColAggNote.setToolTipText("Abilitato solo per versioni PRO o superiore");
        menColAggNote.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/ico_pro.png"))); // NOI18N
        menColAggNote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menColAggNoteActionPerformed(evt);
            }
        });
        menColAgg.add(menColAggNote);

        popGrig.add(menColAgg);

        popFoglioElimina.setText("elimina");
        popFoglioElimina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popFoglioEliminaActionPerformed(evt);
            }
        });
        popFoglio.add(popFoglioElimina);

        foglio3.setModel(new javax.swing.table.DefaultTableModel(
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
        foglio3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                foglio3MouseClicked(evt);
            }
        });

        menClienteNuovo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/contact-new.png"))); // NOI18N
        menClienteNuovo.setText("Nuova Anagrafica");
        menClienteNuovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menClienteNuovoActionPerformed(evt);
            }
        });
        menClientePopup.add(menClienteNuovo);

        menClienteModifica.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/apps/accessories-text-editor.png"))); // NOI18N
        menClienteModifica.setText("Modifica Anagrafica");
        menClienteModifica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menClienteModificaActionPerformed(evt);
            }
        });
        menClientePopup.add(menClienteModifica);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Fattura d'acquisto");
        setPreferredSize(new java.awt.Dimension(890, 691));
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosed(evt);
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
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                formVetoableChange(evt);
            }
        });

        tabDocumento.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabDocumentoStateChanged(evt);
            }
        });

        panDati.setLayout(new java.awt.BorderLayout());

        split.setDividerLocation(250);
        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        split.setMinimumSize(new java.awt.Dimension(508, 200));

        dati.setMinimumSize(new java.awt.Dimension(0, 50));
        dati.setPreferredSize(new java.awt.Dimension(50, 50));
        dati.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                datiComponentResized(evt);
            }
        });

        texNume.setEditable(false);
        texNume.setColumns(4);
        texNume.setText("numero");
        texNume.setDbDescCampo("");
        texNume.setDbNomeCampo("numero");
        texNume.setDbTipoCampo("");
        texNume.setFont(texNume.getFont());
        texNume.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                texNumeFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                texNumeFocusLost(evt);
            }
        });

        texClie.setColumns(2);
        texClie.setDbComboAbbinata(comClie);
        texClie.setDbDefault("vuoto");
        texClie.setDbDescCampo("");
        texClie.setDbNomeCampo("fornitore");
        texClie.setDbNullSeVuoto(true);
        texClie.setDbTipoCampo("");
        texClie.setName("texClie"); // NOI18N
        texClie.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                texClieFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                texClieFocusLost(evt);
            }
        });
        texClie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texClieActionPerformed(evt);
            }
        });
        texClie.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texClieKeyPressed(evt);
            }
        });

        texSpeseIncasso.setColumns(6);
        texSpeseIncasso.setText("spese_incasso");
        texSpeseIncasso.setDbDescCampo("");
        texSpeseIncasso.setDbNomeCampo("spese_incasso");
        texSpeseIncasso.setDbTipoCampo("valuta");
        texSpeseIncasso.setFont(texSpeseIncasso.getFont());
        texSpeseIncasso.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texSpeseIncassoFocusLost(evt);
            }
        });
        texSpeseIncasso.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texSpeseIncassoKeyReleased(evt);
            }
        });

        texScon2.setColumns(4);
        texScon2.setText("sconto2");
        texScon2.setToolTipText("secondo sconto");
        texScon2.setDbDescCampo("");
        texScon2.setDbNomeCampo("sconto2");
        texScon2.setDbTipoCampo("numerico");
        texScon2.setFont(texScon2.getFont());
        texScon2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texScon2FocusLost(evt);
            }
        });
        texScon2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texScon2KeyReleased(evt);
            }
        });

        texScon1.setColumns(4);
        texScon1.setText("sconto1");
        texScon1.setToolTipText("primo sconto");
        texScon1.setDbDescCampo("");
        texScon1.setDbNomeCampo("sconto1");
        texScon1.setDbTipoCampo("numerico");
        texScon1.setFont(texScon1.getFont());
        texScon1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texScon1FocusLost(evt);
            }
        });
        texScon1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texScon1ActionPerformed(evt);
            }
        });
        texScon1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texScon1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texScon1KeyReleased(evt);
            }
        });

        comClie.setDbNomeCampo("");
        comClie.setDbRiempire(false);
        comClie.setDbSalvare(false);
        comClie.setDbTextAbbinato(texClie);
        comClie.setDbTipoCampo("");
        comClie.setDbTrovaMentreScrive(true);
        comClie.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comClieItemStateChanged(evt);
            }
        });
        comClie.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comClieFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                comClieFocusLost(evt);
            }
        });
        comClie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comClieActionPerformed(evt);
            }
        });
        comClie.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                comClieKeyPressed(evt);
            }
        });

        texTotaImpo1.setBackground(new java.awt.Color(255, 200, 200));
        texTotaImpo1.setColumns(2);
        texTotaImpo1.setText("0");
        texTotaImpo1.setDbDescCampo("");
        texTotaImpo1.setDbNomeCampo("imponibile");
        texTotaImpo1.setDbTipoCampo("valuta");

        texTotaIva1.setBackground(new java.awt.Color(255, 200, 200));
        texTotaIva1.setColumns(2);
        texTotaIva1.setText("0");
        texTotaIva1.setDbDescCampo("");
        texTotaIva1.setDbNomeCampo("iva");
        texTotaIva1.setDbTipoCampo("valuta");

        texTota1.setBackground(new java.awt.Color(255, 200, 200));
        texTota1.setColumns(2);
        texTota1.setText("0");
        texTota1.setDbDescCampo("");
        texTota1.setDbNomeCampo("importo");
        texTota1.setDbTipoCampo("valuta");

        texNote.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        texNote.setDbNomeCampo("note");
        texNote.setFont(texNote.getFont());
        texNote.setText("note");

        jLabel13.setFont(jLabel13.getFont());
        jLabel13.setText("Numero");

        jLabel14.setFont(jLabel14.getFont());
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("Serie");

        jLabel16.setFont(jLabel16.getFont());
        jLabel16.setText("Data");

        labScon1.setFont(labScon1.getFont());
        labScon1.setText("Sc. 1");
        labScon1.setToolTipText("primo sconto");

        labScon2.setFont(labScon2.getFont());
        labScon2.setText("Sc. 3");
        labScon2.setToolTipText("sconto3");

        jLabel113.setFont(jLabel113.getFont());
        jLabel113.setText("Sp. incasso");

        texData.setColumns(9);
        texData.setText("data");
        texData.setDbDescCampo("");
        texData.setDbNomeCampo("data");
        texData.setDbTipoCampo("data");
        texData.setFont(texData.getFont());
        texData.setmaxChars(10);
        texData.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                texDataFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                texDataFocusLost(evt);
            }
        });
        texData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texDataActionPerformed(evt);
            }
        });

        jLabel11.setFont(jLabel11.getFont());
        jLabel11.setText("Tipo Pagamento");

        texScon3.setColumns(4);
        texScon3.setText("sconto3");
        texScon3.setToolTipText("terzo sconto");
        texScon3.setDbDescCampo("");
        texScon3.setDbNomeCampo("sconto3");
        texScon3.setDbTipoCampo("numerico");
        texScon3.setFont(texScon3.getFont());
        texScon3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texScon3FocusLost(evt);
            }
        });
        texScon3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texScon3KeyReleased(evt);
            }
        });

        labScon21.setFont(labScon21.getFont());
        labScon21.setText("Sc. 2");
        labScon21.setToolTipText("secondo sconto");

        jLabel151.setFont(jLabel151.getFont());
        jLabel151.setText("Fornitore");

        texSeri.setEditable(false);
        texSeri.setBackground(new java.awt.Color(204, 204, 204));
        texSeri.setColumns(2);
        texSeri.setText("serie");
        texSeri.setDbDescCampo("");
        texSeri.setDbNomeCampo("serie");
        texSeri.setDbTipoCampo("");
        texSeri.setFont(texSeri.getFont());
        texSeri.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texSeriKeyPressed(evt);
            }
        });

        texAnno.setBackground(new java.awt.Color(255, 200, 200));
        texAnno.setColumns(2);
        texAnno.setText("anno");
        texAnno.setDbDescCampo("");
        texAnno.setDbNomeCampo("anno");
        texAnno.setDbTipoCampo("");

        texSpeseTrasporto.setColumns(6);
        texSpeseTrasporto.setText("spese_trasporto");
        texSpeseTrasporto.setDbDescCampo("");
        texSpeseTrasporto.setDbNomeCampo("spese_trasporto");
        texSpeseTrasporto.setDbTipoCampo("valuta");
        texSpeseTrasporto.setFont(texSpeseTrasporto.getFont());
        texSpeseTrasporto.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texSpeseTrasportoFocusLost(evt);
            }
        });
        texSpeseTrasporto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texSpeseTrasportoActionPerformed(evt);
            }
        });
        texSpeseTrasporto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texSpeseTrasportoKeyReleased(evt);
            }
        });

        jLabel114.setFont(jLabel114.getFont());
        jLabel114.setText("Sp. trasporto");

        jLabel15.setFont(jLabel15.getFont().deriveFont((jLabel15.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel15.setText("Dati del documento esterno");

        jLabel17.setFont(jLabel17.getFont());
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setText("Serie");

        jLabel18.setFont(jLabel18.getFont());
        jLabel18.setText("Numero");

        jLabel19.setFont(jLabel19.getFont());
        jLabel19.setText("Data");

        texSeri1.setColumns(4);
        texSeri1.setText("serie");
        texSeri1.setDbDescCampo("Serie Documento");
        texSeri1.setDbNomeCampo("serie_doc");
        texSeri1.setDbTipoCampo("");
        texSeri1.setFont(texSeri1.getFont());
        texSeri1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texSeri1FocusLost(evt);
            }
        });

        texNumePrev2.setColumns(10);
        texNumePrev2.setText("numero");
        texNumePrev2.setDbDescCampo("Numero Documento");
        texNumePrev2.setDbNomeCampo("numero_doc");
        texNumePrev2.setDbTipoCampo("");
        texNumePrev2.setFont(texNumePrev2.getFont());
        texNumePrev2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texNumePrev2FocusLost(evt);
            }
        });

        texData1.setColumns(9);
        texData1.setText("data");
        texData1.setDbDescCampo("Data Documento");
        texData1.setDbNomeCampo("data_doc");
        texData1.setDbTipoCampo("data");
        texData1.setFont(texData1.getFont());
        texData1.setmaxChars(10);
        texData1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texData1FocusLost(evt);
            }
        });

        jLabel12.setFont(jLabel12.getFont());
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("Note");

        texNote1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        texNote1.setDbNomeCampo("descrizione");
        texNote1.setFont(texNote1.getFont());
        texNote1.setText("note");

        jLabel20.setFont(jLabel20.getFont());
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText("Note");

        comPaga.setDbDescCampo("");
        comPaga.setDbNomeCampo("pagamento");
        comPaga.setDbTipoCampo("VARCHAR");
        comPaga.setDbTrovaMentreScrive(true);
        comPaga.setFont(comPaga.getFont());
        comPaga.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comPagaItemStateChanged(evt);
            }
        });
        comPaga.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                comPagaFocusLost(evt);
            }
        });

        butScad.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        butScad.setText("...");
        butScad.setToolTipText("Gestione Scadenze");
        butScad.setMargin(new java.awt.Insets(1, 1, 1, 1));
        butScad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butScadActionPerformed(evt);
            }
        });

        labGiornoPagamento.setFont(labGiornoPagamento.getFont());
        labGiornoPagamento.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labGiornoPagamento.setText("giorno");

        texGiornoPagamento.setColumns(3);
        texGiornoPagamento.setToolTipText("Giorno del mese per le scadenze");
        texGiornoPagamento.setDbDescCampo("");
        texGiornoPagamento.setDbNomeCampo("giorno_pagamento");
        texGiornoPagamento.setDbTipoCampo("numerico");
        texGiornoPagamento.setFont(texGiornoPagamento.getFont());

        prezzi_ivati.setBackground(new java.awt.Color(255, 204, 204));
        prezzi_ivati.setText("prezzi ivati");
        prezzi_ivati.setToolTipText("Selezionando questa opzione stampa la Destinazione diversa nella Distinta delle RIBA");
        prezzi_ivati.setDbDescCampo("Prezzi Ivati");
        prezzi_ivati.setDbNomeCampo("prezzi_ivati");
        prezzi_ivati.setDbTipoCampo("");
        prezzi_ivati.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        prezzi_ivati.setIconTextGap(1);

        texCliente.setColumns(25);
        texCliente.setFont(texCliente.getFont());
        texCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                texClienteMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                texClienteMouseReleased(evt);
            }
        });

        apriclienti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apriclientiActionPerformed(evt);
            }
        });

        texTipoFattura.setBackground(new java.awt.Color(255, 200, 200));
        texTipoFattura.setColumns(2);
        texTipoFattura.setText("tipoFattura");
        texTipoFattura.setDbDescCampo("");
        texTipoFattura.setDbNomeCampo("tipo_fattura");
        texTipoFattura.setDbTipoCampo("numerico");

        butAddClie.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        butAddClie.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/Actions-contact-new-icon-16.png"))); // NOI18N
        butAddClie.setToolTipText("Crea una nuova anagrafica");
        butAddClie.setMargin(new java.awt.Insets(1, 1, 1, 1));
        butAddClie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAddClieActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout datiLayout = new org.jdesktop.layout.GroupLayout(dati);
        dati.setLayout(datiLayout);
        datiLayout.setHorizontalGroup(
            datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiLayout.createSequentialGroup()
                .addContainerGap()
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(datiLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(datiLayout.createSequentialGroup()
                                .add(texSeri1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texNumePrev2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel17)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(jLabel18)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(texData1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel19))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, datiLayout.createSequentialGroup()
                                .add(texNote1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                            .add(datiLayout.createSequentialGroup()
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(datiLayout.createSequentialGroup()
                                        .add(texTotaImpo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texAnno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texTota1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texTotaIva1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(comClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texTipoFattura, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(prezzi_ivati, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(jLabel12))
                                .add(0, 0, Short.MAX_VALUE))))
                    .add(datiLayout.createSequentialGroup()
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(datiLayout.createSequentialGroup()
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(datiLayout.createSequentialGroup()
                                        .add(jLabel14)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel13)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel16)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel151))
                                    .add(datiLayout.createSequentialGroup()
                                        .add(texSeri, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texNume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texCliente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(0, 0, 0)
                                        .add(apriclienti, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(butAddClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(datiLayout.createSequentialGroup()
                                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, datiLayout.createSequentialGroup()
                                                .add(labScon1)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(labScon21)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(labScon2)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(jLabel114)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(jLabel113)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(jLabel11))
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, datiLayout.createSequentialGroup()
                                                .add(texScon1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(texScon2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(texScon3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(texSpeseTrasporto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(texSpeseIncasso, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(comPaga, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 195, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(butScad, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(labGiornoPagamento)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(texGiornoPagamento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(jLabel15))
                                .add(0, 0, Short.MAX_VALUE))
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel20)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texNote, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())))
        );

        datiLayout.linkSize(new java.awt.Component[] {jLabel14, texSeri}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {jLabel13, texNume}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {jLabel16, texData}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {labScon1, texScon1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {labScon21, texScon2}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {labScon2, texScon3}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {jLabel114, texSpeseTrasporto}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {jLabel113, texSpeseIncasso}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {jLabel17, texSeri1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.setVerticalGroup(
            datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiLayout.createSequentialGroup()
                .add(2, 2, 2)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel14)
                    .add(jLabel13)
                    .add(jLabel16)
                    .add(jLabel151))
                .add(1, 1, 1)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(texSeri, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(texNume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(texData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(texCliente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(butAddClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(apriclienti, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labScon1)
                    .add(labScon21)
                    .add(labScon2)
                    .add(jLabel114)
                    .add(jLabel113)
                    .add(jLabel11))
                .add(1, 1, 1)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(texGiornoPagamento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(labGiornoPagamento))
                    .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(texScon1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(texScon2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(texScon3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(texSpeseTrasporto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(texSpeseIncasso, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(comPaga, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(butScad, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel20)
                    .add(texNote, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel15)
                .add(2, 2, 2)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel17)
                    .add(jLabel18)
                    .add(jLabel19)
                    .add(jLabel12))
                .add(1, 1, 1)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(texSeri1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(texNumePrev2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(texData1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(texNote1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texTotaImpo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texAnno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texTota1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texTotaIva1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(comClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texTipoFattura, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(prezzi_ivati, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(1, Short.MAX_VALUE))
        );

        datiLayout.linkSize(new java.awt.Component[] {apriclienti, butAddClie, texCliente}, org.jdesktop.layout.GroupLayout.VERTICAL);

        datiLayout.linkSize(new java.awt.Component[] {butScad, comPaga}, org.jdesktop.layout.GroupLayout.VERTICAL);

        split.setLeftComponent(dati);

        datiRighe.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);

        griglia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                grigliaMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                grigliaMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                grigliaMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(griglia);

        datiRighe.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        prezzi_ivati_virtual.setText("Prezzi IVA inclusa");
        prezzi_ivati_virtual.setToolTipText("<html>\nSelezionando questa opzione verrà effettuato lo scorporo IVA soltanto a fine documento e non riga per riga, inoltre<br>\nverranno presentati in stampa gli importi di riga già ivati invece che gli imponibili.<br>\n<br>\nL'esempio più lampante è questo:<br>\n<br>\nArticolo di prezzo <b>10,00</b> € (iva inclusa del 21%)<br>\n- Senza la scelta 'Prezzi IVA inclusa' il totale fattura verrà <b>9,99</b> € perchè:<br>\nlo scorporo di 10,00 € genera un imponibile di 8,26 il quale applicando l'iva 21% (1,73 €) genererà un totale di 9,99 €<br>\n- Con la scelta 'Prezzi IVA inclusa' il totale fattura verrà direttamente <b>10,00</b> € e verrà calcolato l'imponibile facendo la<br>\nsottrazione tra il totale e l'iva derivante dallo scorporo del totale già ivato.<br>\n</html>");
        prezzi_ivati_virtual.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        prezzi_ivati_virtual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prezzi_ivati_virtualActionPerformed(evt);
            }
        });
        jPanel1.add(prezzi_ivati_virtual);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(6, 20));
        jPanel1.add(jSeparator1);

        butNuovArti.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butNuovArti.setText("Inserisci nuova riga");
        butNuovArti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNuovArtiActionPerformed(evt);
            }
        });
        jPanel1.add(butNuovArti);

        butImportRighe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butImportRighe.setText("Importa Righe Da CSV");
        butImportRighe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butImportRigheActionPerformed(evt);
            }
        });
        jPanel1.add(butImportRighe);

        butImportXlsCirri.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butImportXlsCirri.setText("Import xls");
        butImportXlsCirri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butImportXlsCirriActionPerformed(evt);
            }
        });
        jPanel1.add(butImportXlsCirri);

        datiRighe.add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));
        datiRighe.add(jPanel2, java.awt.BorderLayout.SOUTH);

        split.setRightComponent(datiRighe);

        panDati.add(split, java.awt.BorderLayout.CENTER);

        tabDocumento.addTab("Dati Fattura", panDati);

        org.jdesktop.layout.GroupLayout pan_segnaposto_depositoLayout = new org.jdesktop.layout.GroupLayout(pan_segnaposto_deposito);
        pan_segnaposto_deposito.setLayout(pan_segnaposto_depositoLayout);
        pan_segnaposto_depositoLayout.setHorizontalGroup(
            pan_segnaposto_depositoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        pan_segnaposto_depositoLayout.setVerticalGroup(
            pan_segnaposto_depositoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        labCampoLibero1.setText("campo libero 1");
        labCampoLibero1.setToolTipText("");

        comCampoLibero1.setDbNomeCampo("campo_libero_1");
        comCampoLibero1.setDbRiempireForceText(true);
        comCampoLibero1.setDbSalvaKey(false);
        comCampoLibero1.setDbTrovaMentreScrive(true);
        comCampoLibero1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comCampoLibero1FocusGained(evt);
            }
        });
        comCampoLibero1.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                comCampoLibero1PopupMenuWillBecomeVisible(evt);
            }
        });

        org.jdesktop.layout.GroupLayout datiAltroLayout = new org.jdesktop.layout.GroupLayout(datiAltro);
        datiAltro.setLayout(datiAltroLayout);
        datiAltroLayout.setHorizontalGroup(
            datiAltroLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiAltroLayout.createSequentialGroup()
                .addContainerGap()
                .add(datiAltroLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pan_segnaposto_deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(datiAltroLayout.createSequentialGroup()
                        .add(labCampoLibero1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comCampoLibero1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(542, Short.MAX_VALUE))
        );
        datiAltroLayout.setVerticalGroup(
            datiAltroLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiAltroLayout.createSequentialGroup()
                .addContainerGap()
                .add(pan_segnaposto_deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(datiAltroLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labCampoLibero1)
                    .add(comCampoLibero1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(537, Short.MAX_VALUE))
        );

        tabDocumento.addTab("Altro", datiAltro);

        getContentPane().add(tabDocumento, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel3.setPreferredSize(new java.awt.Dimension(194, 140));

        jLabel3.setForeground(javax.swing.UIManager.getDefaults().getColor("Label.background"));

        butUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-undo.png"))); // NOI18N
        butUndo.setText("Annulla");
        butUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butUndoActionPerformed(evt);
            }
        });

        butSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/devices/media-floppy.png"))); // NOI18N
        butSave.setText("Salva");
        butSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSaveActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(butUndo)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(butSave)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(jLabel3))
                    .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(butSave, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(butUndo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.add(jPanel3, java.awt.BorderLayout.WEST);

        jPanel4.setPreferredSize(new java.awt.Dimension(500, 40));

        panRitenute.setName("panRitenute"); // NOI18N

        org.jdesktop.layout.GroupLayout panRitenuteLayout = new org.jdesktop.layout.GroupLayout(panRitenute);
        panRitenute.setLayout(panRitenuteLayout);
        panRitenuteLayout.setHorizontalGroup(
            panRitenuteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 277, Short.MAX_VALUE)
        );
        panRitenuteLayout.setVerticalGroup(
            panRitenuteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        texTotaImpo.setEditable(false);
        texTotaImpo.setBorder(null);
        texTotaImpo.setColumns(10);
        texTotaImpo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texTotaImpo.setText("0");
        texTotaImpo.setDbTipoCampo("valuta");
        texTotaImpo.setFont(texTotaImpo.getFont());

        texTotaIva.setEditable(false);
        texTotaIva.setBorder(null);
        texTotaIva.setColumns(10);
        texTotaIva.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texTotaIva.setText("0");
        texTotaIva.setDbTipoCampo("valuta");
        texTotaIva.setFont(texTotaIva.getFont());

        texTota.setEditable(false);
        texTota.setBorder(null);
        texTota.setColumns(10);
        texTota.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texTota.setText("0");
        texTota.setDbTipoCampo("valuta");
        texTota.setFont(texTota.getFont().deriveFont(texTota.getFont().getStyle() | java.awt.Font.BOLD, texTota.getFont().getSize()+2));

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() | java.awt.Font.BOLD, jLabel2.getFont().getSize()+1));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Totale");

        jLabel21.setFont(jLabel21.getFont());
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("Totale Iva");

        jLabel22.setFont(jLabel22.getFont());
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText("Totale Imponibile");

        jLabel26.setFont(jLabel26.getFont());
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("Sconto");

        texSconto.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        texSconto.setColumns(10);
        texSconto.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texSconto.setText("0");
        texSconto.setDbTipoCampo("valuta");
        texSconto.setFont(texSconto.getFont());
        texSconto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texScontoKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texScontoKeyReleased(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(panRitenute, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .add(jLabel26)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texSconto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texTota, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .add(jLabel21)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texTotaIva, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .add(jLabel22)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texTotaImpo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(new java.awt.Component[] {texSconto, texTota, texTotaImpo, texTotaIva}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(2, 2, 2)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texSconto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel26))
                .add(2, 2, 2)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texTotaImpo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel22))
                .add(2, 2, 2)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texTotaIva, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel21))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texTota, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addContainerGap(68, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, panRitenute, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel5.add(jPanel4, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel5, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void texClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texClieActionPerformed
    }//GEN-LAST:event_texClieActionPerformed

    private void texClieFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texClieFocusLost
        try {
            if (this.texClie.getText().length() > 0) {
                this.dbdoc.forceCliente(Long.parseLong(this.texClie.getText()));
                segnalaDuplicato();

            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        ricalcolaTotali();
    }//GEN-LAST:event_texClieFocusLost

    private void texClieKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texClieKeyPressed
        if (evt.getKeyCode() == evt.VK_ENTER) {
            this.recuperaDatiCliente();
        }
    }//GEN-LAST:event_texClieKeyPressed

    private void texSpeseIncassoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texSpeseIncassoFocusLost
        ricalcolaTotali();
    }//GEN-LAST:event_texSpeseIncassoFocusLost

    private void texSpeseIncassoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texSpeseIncassoKeyReleased
        try {
            dbdoc.speseIncassoIva = Db.getDouble(this.texSpeseIncasso.getText());
        } catch (Exception err) {
            dbdoc.speseIncassoIva = 0;
        }

        dbdoc.dbRefresh();
    }//GEN-LAST:event_texSpeseIncassoKeyReleased

    private void texScon2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texScon2FocusLost
        ricalcolaTotali();
    }//GEN-LAST:event_texScon2FocusLost

    private void texScon2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon2KeyReleased
        try {
            dbdoc.sconto2 = Db.getDouble(this.texScon2.getText());
        } catch (Exception err) {
            dbdoc.sconto2 = 0;
        }

        dbdoc.dbRefresh();
    }//GEN-LAST:event_texScon2KeyReleased

    private void texScon1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texScon1ActionPerformed
    }//GEN-LAST:event_texScon1ActionPerformed

    private void texScon1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texScon1FocusLost
        ricalcolaTotali();
    }//GEN-LAST:event_texScon1FocusLost

    private void texScon1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon1KeyPressed
    }//GEN-LAST:event_texScon1KeyPressed

    private void texScon1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon1KeyReleased
        try {
            dbdoc.sconto1 = Db.getDouble(this.texScon1.getText());
        } catch (Exception err) {
            dbdoc.sconto1 = 0;
        }

        dbdoc.dbRefresh();
    }//GEN-LAST:event_texScon1KeyReleased

    private void comClieItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comClieItemStateChanged
//        if (evt.getStateChange() == ItemEvent.SELECTED) {
//            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//            String sqlTmp = "SELECT note, note_automatiche as auto FROM clie_forn where codice = " + Db.pc(String.valueOf(comClie.getSelectedKey()), "NUMERIC");
//            ResultSet noteauto = Db.openResultSet(sqlTmp);
//            try {
//                if (noteauto.next()) {
//                    String auto = noteauto.getString("auto");
//                    String nota = noteauto.getString("note");
//                    if (auto != null && auto.equals("S")) {
//                        if (!texNote.getText().equals("") && !texNote.getText().equals(nota)) {
//                            this.texNote.setText(nota);
//                        } else {
//                            this.texNote.setText(noteauto.getString("note"));
//                        }
//                    }
//                }
//            } catch (SQLException ex) {
//                ex.printStackTrace();
//            }
//            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//        }
    }//GEN-LAST:event_comClieItemStateChanged

    private void comClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comClieActionPerformed
        //apro combo destinazione cliente
        sql = "select obsoleto from clie_forn";
        sql += " where codice = " + Db.pc(this.texClie.getText(), "NUMERIC");

        ResultSet rs = Db.openResultSet(sql);
        try {
            if (rs.next()) {
                int obsoleto = rs.getInt("obsoleto");
                if (obsoleto == 1) {
                    JOptionPane.showMessageDialog(this, "Attenzione, il fornitore selezionato è segnato come obsoleto.", "Fornitore obsoleto", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_comClieActionPerformed

    private void comClieFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comClieFocusLost
        if (comClie.getSelectedIndex() != comClieSel_old) {
            this.recuperaDatiCliente();
            ricalcolaTotali();
        }
    }//GEN-LAST:event_comClieFocusLost

    private void comClieKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comClieKeyPressed

        if (evt.getKeyCode() == evt.VK_ENTER) {
            this.recuperaDatiCliente();        //ricerca con F4
        }
        if (evt.getKeyCode() == evt.VK_F4) {

            java.util.Hashtable colsWidthPerc = new java.util.Hashtable();
            colsWidthPerc.put("id", new Double(20));
            colsWidthPerc.put("ragione", new Double(40));
            colsWidthPerc.put("indi", new Double(40));

            String sql
                    = "select clie_forn.codice as id, clie_forn.ragione_sociale as ragione, clie_forn.indirizzo as indi from clie_forn " + "where clie_forn.ragione_sociale like '%" + Db.aa(this.comClie.getText()) + "%'" + " order by clie_forn.ragione_sociale";
            ResultSet resTemp = db.openResultSet(sql);

            try {

                if (resTemp.next() == true) {

                    frmDbListSmall temp = new frmDbListSmall(main.getPadre(), true, sql, this.texClie, 0, colsWidthPerc, 50, 50, 400, 300);
                    this.recuperaDatiCliente();
                    this.comClie.dbTrovaKey(texClie.getText());
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Nessun fornitore trovato");
                }
            } catch (Exception err) {
                err.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this, "Errore nella ricerca fornitore: " + err.toString());
            }
        }
    }//GEN-LAST:event_comClieKeyPressed

    private void texDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texDataActionPerformed
    }//GEN-LAST:event_texDataActionPerformed

    private void texDataFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texDataFocusLost

        if (!ju.isValidDateIta(texData.getText())) {
            SwingUtils.showFlashMessage2Comp("Data non valida", 3, texData, Color.red);
            return;
        }

        if (!old_anno.equals(getAnnoDaForm())) {
            if (dbStato == DB_INSERIMENTO) {
                dbdoc.dbRicalcolaProgressivo(dbStato, this.texData.getText(), this.texNume, texAnno, texSeri.getText(), id);
                dbdoc.numero = new Integer(this.texNume.getText()).intValue();
                id_modificato = true;
            } else {
                //controllo che se è un numero già presente non glielo faccio fare percè altrimenti sovrascrive una altra fattura
                sql = "select numero from test_fatt_acquisto";
                sql += " where serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
                sql += " and numero " + Db.pcW(texNume.getText(), "NUMBER");
                sql += " and anno " + Db.pcW(getAnnoDaForm(), "VARCHAR");
                ResultSet r = Db.openResultSet(sql);
                try {
                    if (r.next()) {
                        texData.setText(old_data);
                        JOptionPane.showMessageDialog(this, "Non puoi mettere questo numero e data, si sovrappongono ad una fattura già presente !", "Attenzione", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            try {
                texAnno.setText(getAnnoDaForm());
                dbdoc.anno = Integer.parseInt(getAnnoDaForm());
                dbdoc.numero = Integer.parseInt(texNume.getText());

                if (!main.edit_doc_in_temp) {
                
                    sql = "update righ_fatt_acquisto";
                    sql += " set anno = " + Db.pc(dbdoc.anno, "NUMBER");
                    sql += " , numero = " + Db.pc(dbdoc.numero, "NUMBER");
                    sql += " where id_padre = " + this.id;
                    Db.executeSql(sql);

                    sql = "update test_fatt_acquisto";
                    sql += " set anno = " + Db.pc(dbdoc.anno, "NUMBER");
                    sql += " , numero = " + Db.pc(dbdoc.numero, "NUMBER");
                    sql += " where id = " + this.id;
                    Db.executeSql(sql);

                    //riassocio
                    dbAssociaGrigliaRighe();

                    doc.load(Db.INSTANCE, dbdoc.numero, dbdoc.serie, dbdoc.anno, Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, id);
                    ricalcolaTotali();

                    anno_modificato = true;

                    //vado ad aggiornare eventuali movimenti generati
                    sql = "update movimenti_magazzino";
                    sql += " set da_numero = " + Db.pc(dbdoc.numero, "NUMBER");
                    sql += ", da_anno = " + Db.pc(dbdoc.anno, "NUMBER");
                    sql += " where da_serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
                    sql += " and da_numero " + Db.pcW(old_id, "NUMBER");
                    sql += " and da_anno " + Db.pcW(String.valueOf(old_anno), "VARCHAR");
                    sql += " and da_tabella = 'test_fatt_acquisto'";
                    Db.executeSql(sql);
                }

            } catch (Exception err) {
                err.printStackTrace();
            }
        } else {
            ricalcolaTotali();
        }
    }//GEN-LAST:event_texDataFocusLost

    private void texScon3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texScon3FocusLost
        ricalcolaTotali();
    }//GEN-LAST:event_texScon3FocusLost

    private void texScon3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon3KeyReleased
        try {
            dbdoc.sconto3 = Db.getDouble(this.texScon3.getText());
        } catch (Exception err) {

            //err.printStackTrace();
            dbdoc.sconto3 = 0;
        }

        dbdoc.dbRefresh();
    }//GEN-LAST:event_texScon3KeyReleased

    private void texSeriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texSeriKeyPressed
        if (evt.getKeyCode() == evt.VK_TAB || evt.getKeyCode() == evt.VK_ENTER) {
            assegnaSerie();
        }
    }//GEN-LAST:event_texSeriKeyPressed

    private void texSpeseTrasportoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texSpeseTrasportoActionPerformed
// Add your handling code here:
    }//GEN-LAST:event_texSpeseTrasportoActionPerformed

    private void texSpeseTrasportoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texSpeseTrasportoFocusLost
        ricalcolaTotali();
    }//GEN-LAST:event_texSpeseTrasportoFocusLost

    private void texSpeseTrasportoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texSpeseTrasportoKeyReleased
        try {
            dbdoc.speseTrasportoIva = Db.getDouble(this.texSpeseTrasporto.getText());
        } catch (Exception err) {
            dbdoc.speseTrasportoIva = 0;
        }

        dbdoc.dbRefresh();
    }//GEN-LAST:event_texSpeseTrasportoKeyReleased

    private void grigliaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseClicked

        try {
            if (evt.getClickCount() == 2) {
                //modifico o la riga o la finestra
                popGrigModiActionPerformed(null);
            }
        } catch (Exception err) {
            javax.swing.JOptionPane.showMessageDialog(null, err.toString());
            err.printStackTrace();
        }

    }//GEN-LAST:event_grigliaMouseClicked

    private void butNuovArtiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNuovArtiActionPerformed
        String codiceListino = "1";

        try {
            codiceListino = Db.lookUp(this.texClie.getText(), "codice", "clie_forn").getString("codice_listino");
        } catch (Exception err) {
            err.printStackTrace();
        }

        int codiceCliente = -1;

        if (this.texClie.getText().length() > 0) {

            try {
                codiceCliente = Integer.parseInt(texClie.getText());
            } catch (Exception err) {
                err.printStackTrace();
            }
        }

//        java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);
//        boolean multiriga = preferences.getBoolean("multiriga", false);
        boolean multiriga = main.fileIni.getValueBoolean("pref", "multiriga", false);

//        if (multiriga == false) {
//            frmNuovRiga frm = new frmNuovRiga(this, this.dati.DB_INSERIMENTO, this.texSeri.getText(), Integer.valueOf(this.texNumePrev.getText()).intValue(), "P", 0, Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente);
//            temp.openFrame(frm, 600, 350, 100, 100);
//            frm.setStato();
//        } else {
        try {
            frmNuovRigaDescrizioneMultiRigaNew frm = new frmNuovRigaDescrizioneMultiRigaNew(this, this.dati.DB_INSERIMENTO, this.texSeri.getText(), Integer.valueOf(this.texNume.getText()).intValue(), "P", 0, Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente, null, this.id, getNomeTabRighe());
            int w = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_w", "980"));
            int h = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_h", "460"));
            int top = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_top", "100"));
            int left = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_left", "100"));
            main.getPadre().openFrame(frm, w, h, top, left);
            frm.setStato();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        }
    }//GEN-LAST:event_butNuovArtiActionPerformed

    private void tabDocumentoStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabDocumentoStateChanged
    }//GEN-LAST:event_tabDocumentoStateChanged

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
    }//GEN-LAST:event_formKeyPressed

    private void popFoglioEliminaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popFoglioEliminaActionPerformed
    }//GEN-LAST:event_popFoglioEliminaActionPerformed

    private void foglio3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_foglio3MouseClicked
    }//GEN-LAST:event_foglio3MouseClicked

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
                    }//GEN-LAST:event_formInternalFrameClosing

    private void texBancCCFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texBancCCFocusLost
    }//GEN-LAST:event_texBancCCFocusLost

    private void butUndo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butUndo1ActionPerformed

    }//GEN-LAST:event_butUndo1ActionPerformed

    private void texScon2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon2KeyPressed
                    }//GEN-LAST:event_texScon2KeyPressed

    private void texScon1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon1KeyTyped
                    }//GEN-LAST:event_texScon1KeyTyped

    private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameOpened
        griglia.resizeColumnsPerc(true);
        InvoicexUtil.aggiornaSplit(dati, split);        
    }//GEN-LAST:event_formInternalFrameOpened

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosed
        InvoicexUtil.removeLock("test_fatt_acquisto", id, this);
        main.getPadre().closeFrame(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void popGrigElimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popGrigElimActionPerformed

        //elimino la/le righe
        try {
            if (griglia.getSelectedRowCount() >= 1) {
                //elimino gli le righe per id
                for (int i = 0; i < griglia.getSelectedRowCount(); i++) {
                    sql = "delete from " + griglia.dbNomeTabella + " where id = " + griglia.getValueAt(griglia.getSelectedRows()[i], griglia.getColumnByName("id"));
                    System.out.println("i = " + i);
                    dbu.tryExecQuery(Db.getConn(), sql);
                }
            }
        } catch (Exception e) {
            SwingUtils.showExceptionMessage(this, e);
        }

        griglia.dbRefresh();
        dbdoc.dbRefresh();
        ricalcolaTotali();
    }//GEN-LAST:event_popGrigElimActionPerformed

    private void popGrigModiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popGrigModiActionPerformed
        String codiceListino = "1";

        Integer id_riga = cu.i(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id")));
        Integer id_padre = cu.i(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id_padre")));

        try {
            codiceListino = Db.lookUp(this.texClie.getText(), "codice", "clie_forn").getString("codice_listino");
        } catch (Exception err) {
            System.out.println("err:" + err);
        }

        int codiceCliente = -1;

        if (this.texClie.getText().length() > 0) {

            try {
                codiceCliente = Integer.parseInt(texClie.getText());
            } catch (Exception err) {
                err.printStackTrace();
            }
        }

//        java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);
//        boolean multiriga = preferences.getBoolean("multiriga", false);
        boolean multiriga = main.fileIni.getValueBoolean("pref", "multiriga", false);

//        if (multiriga == false) {
//            frmNuovRiga frm = new frmNuovRiga(this, this.dati.DB_MODIFICA, this.texSeri.getText(), Integer.valueOf(this.texNumePrev.getText()).intValue(), "P", Integer.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), 3).toString()).intValue(), Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente);
//            temp.openFrame(frm, 600, 350, 100, 100);
//            frm.setStato();
//        } else {
        try {
            frmNuovRigaDescrizioneMultiRigaNew frm = new frmNuovRigaDescrizioneMultiRigaNew(this, this.dati.DB_MODIFICA, this.texSeri.getText(), Integer.valueOf(this.texNume.getText()).intValue(), "P", Integer.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), 3).toString()).intValue(), Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente, id_riga, id_padre, getNomeTabRighe());
            int w = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_w", "980"));
            int h = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_h", "460"));
            int top = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_top", "100"));
            int left = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_left", "100"));
            main.getPadre().openFrame(frm, w, h, top, left);
            frm.setStato();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        }
    }//GEN-LAST:event_popGrigModiActionPerformed

    private void butUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butUndoActionPerformed
        if (evt != null) {
            if (!SwingUtils.showYesNoMessage(main.getPadreWindow(), "Sicuro di annullare le modifiche ?")) {
                return;
            }
        }

        if (!main.edit_doc_in_temp) {
            SwingUtils.mouse_wait(main.getPadreFrame());
            // Add your handling code here:
            if (dbStato == this.DB_INSERIMENTO) {

                //elimino la testata inserita e poi annullata
                String sql = "delete from test_fatt_acquisto";
                sql += " where id = " + this.id;
                Db.executeSql(sql);
                sql = "delete from righ_fatt_acquisto";
                sql += " where id_padre = " + this.id;
                Db.executeSql(sql);
            } else if (dbStato == this.DB_MODIFICA) {
                //rimetto numero originale
                sql = "update test_fatt_acquisto";
                sql += " set numero = " + Db.pc(numero_originale, "NUMBER");
                sql += " , anno = " + Db.pc(anno_originale, "NUMBER");
                sql += " where id = " + this.id;
                Db.executeSql(sql);

                //elimino le righe inserite
                sql = "delete from righ_fatt_acquisto";
                sql += " where id_padre = " + this.id;
                Db.executeSqlDialogExc(sql, true);

                //e rimetto quelle da temp
                /* ATTENZIONE, NON RIMETTERE COME QUI SOTTO, OVVERO SENZA GLI ID ALTRIMENTI SI PERDE IL COLLEGAMENTO CON LE INFO SU LOTTI E MATRICOLE */
    //            sql = "insert into righ_fatt_acquisto (" + Db.getFieldList("righ_fatt_acquisto", false, Arrays.asList("id")) + ")";
    //            sql += " select " + Db.getFieldList("righ_fatt_acquisto_temp", true, Arrays.asList("id"));
                sql = "insert into righ_fatt_acquisto (" + Db.getFieldList("righ_fatt_acquisto", false) + ")";
                sql += " select " + Db.getFieldList("righ_fatt_acquisto_temp", true);
                sql += " from righ_fatt_acquisto_temp";
                sql += " where id_padre = " + this.id;
                sql += " and username = '" + main.login + "'";
                Db.executeSqlDialogExc(sql, true);

                //rimetto numero originale su eventuali movimenti
                sql = "update movimenti_magazzino";
                sql += " set da_numero = " + Db.pc(numero_originale, "NUMBER");
                sql += ", da_anno = " + Db.pc(anno_originale, "NUMBER");
                sql += " where da_serie " + Db.pcW(dbdoc.serie, "VARCHAR");
                sql += " and da_numero " + Db.pcW(dbdoc.numero, "NUMBER");
                sql += " and da_anno " + Db.pcW(dbdoc.anno, "VARCHAR");
                sql += " and da_tabella = 'test_fatt_acquisto'";
                Db.executeSql(sql);

            }
        }
        
        try {
            from.dbRefresh();
        } catch (Exception e) {
        }

        SwingUtils.mouse_def(main.getPadreFrame());
        this.dispose();
    }//GEN-LAST:event_butUndoActionPerformed

    public void annulla() {
        butUndoActionPerformed(null);
    }

    private void butSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSaveActionPerformed
        if (controlloCampi() == true) {
            SwingUtils.mouse_wait(this);
            griglia.setEnabled(false);
            butSave.setEnabled(false);
            butUndo.setEnabled(false);
            Thread t = new Thread("frmTestFattAcquisto_butSave") {
                @Override
                public void run() {
                    if (saveDocumento()) {
                        SwingUtils.inEdt(new Runnable() {
                            public void run() {

                                SwingUtils.mouse_def(main.getPadreFrame());
                                if (from != null) {
                                    from.dbRefresh();
                                }

                                if (chiudere) {
                                    SwingUtils.mouse_def(frmTestFattAcquisto.this);
                                    frmTestFattAcquisto.this.dispose();
                                } else {
                                    //ci pensa allegati util a chiudere
                                    System.out.println("aspetto il salvataggio degli allegati");
                                }
                            }
                        });
                    } else {
                        griglia.setEnabled(true);
                        butSave.setEnabled(true);
                        butUndo.setEnabled(true);
                    }
                }
            };
            t.start();
        }
    }//GEN-LAST:event_butSaveActionPerformed

private void comPagaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comPagaItemStateChanged
    if (evt != null && evt.getStateChange() == ItemEvent.DESELECTED) {
        return;
    }

    try {
        texGiornoPagamento.setEnabled(false);
        labGiornoPagamento.setEnabled(false);

        if (comPaga.getSelectedIndex() >= 0) {
            ResultSet r = Db.lookUp(String.valueOf(comPaga.getSelectedKey()), "codice", "pagamenti");
            if (Db.nz(r.getString("flag_richiedi_giorno"), "N").equalsIgnoreCase("S")) {
                texGiornoPagamento.setEnabled(true);
                labGiornoPagamento.setEnabled(true);
                if (!in_apertura) {
                    //carico il giorno dal cliente
                    texGiornoPagamento.setText("");
                    //li recupero dal cliente
                    ResultSet tempClie;
                    String sql = "select giorno_pagamento from clie_forn";
                    sql += " where codice = " + Db.pc(this.texClie.getText(), "NUMERIC");
                    tempClie = Db.openResultSet(sql);
                    try {
                        if (tempClie.next() == true) {
                            texGiornoPagamento.setText(tempClie.getString("giorno_pagamento"));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (texGiornoPagamento.getText().equals("0")) {
                        texGiornoPagamento.setText("");
                    }
                }
            }
        }
    } catch (Exception err) {
        err.printStackTrace();
    }    
    
}//GEN-LAST:event_comPagaItemStateChanged

private void comPagaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comPagaFocusLost

    try {
        ResultSet p = Db.openResultSet("select * from pagamenti where codice = " + Db.pc(this.comPaga.getSelectedKey(), Types.VARCHAR));
    } catch (Exception err) {
        err.printStackTrace();
    }

}//GEN-LAST:event_comPagaFocusLost

private void butScadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butScadActionPerformed
    if (!DateUtils.isDate(texData1.getText())) {
        SwingUtils.showInfoMessage(this, "Inserire una data valida della data del documento");
    } else {
        saveDocumento();
        Scadenze tempScad = new Scadenze(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, id, this.comPaga.getText(), CastUtils.toDate(texData1.getText()));
        frmPagaPart frm = new frmPagaPart(tempScad, null);

        main.getPadre().openFrame(frm, 650, 550, 300, 100);
    }
}//GEN-LAST:event_butScadActionPerformed

private void texNumeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texNumeFocusLost
    texNume.setText(texNume.getText().replaceAll("[^\\d.]", ""));
    if (!old_id.equals(texNume.getText())) {
        //controllo che se è un numero già presente non glielo facci ofare percè altrimenti sovrascrive una altra fattura
        sql = "select numero from test_fatt_acquisto";
        sql += " where serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
        sql += " and numero " + Db.pcW(texNume.getText(), "NUMBER");
        sql += " and anno " + Db.pcW(String.valueOf(this.dbdoc.anno), "VARCHAR");
        ResultSet r = Db.openResultSet(sql);
        try {
            if (r.next()) {
                texNume.setText(old_id);
                JOptionPane.showMessageDialog(this, "Non puoi mettere il numero di una fattura già presente !", "Attenzione", JOptionPane.INFORMATION_MESSAGE);
                return;
            } else {
                //associo al nuovo numero
                dbdoc.numero = new Integer(this.texNume.getText()).intValue();

                if (!main.edit_doc_in_temp) {
                    sql = "update righ_fatt_acquisto";
                    sql += " set numero = " + Db.pc(dbdoc.numero, "NUMBER");
                    sql += " where id_padre = " + this.id;
                    Db.executeSql(sql);

                    sql = "update test_fatt_acquisto";
                    sql += " set numero = " + Db.pc(dbdoc.numero, "NUMBER");
                    sql += " where id = " + this.id;
                    Db.executeSql(sql);

    //                dati.dbChiaveValori.clear();
    //                dati.dbChiaveValori.put("serie", prev.serie);
    //                dati.dbChiaveValori.put("numero", prev.numero);
    //                dati.dbChiaveValori.put("anno", prev.anno);
                    //riassocio
                    dbAssociaGrigliaRighe();
                    id_modificato = true;

                    dbdoc.numero = Integer.parseInt(texNume.getText());
                    doc.load(Db.INSTANCE, this.dbdoc.numero, this.dbdoc.serie, this.dbdoc.anno, Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, id);
                    ricalcolaTotali();

                    //vado ad aggiornare eventuali movimenti generati
                    sql = "update movimenti_magazzino";
                    sql += " set da_numero = " + Db.pc(dbdoc.numero, "NUMBER");
                    sql += ", da_anno = " + Db.pc(dbdoc.anno, "NUMBER");
                    sql += " where da_serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
                    sql += " and da_numero " + Db.pcW(old_id, "NUMBER");
                    sql += " and da_anno " + Db.pcW(String.valueOf(dbdoc.anno), "VARCHAR");
                    sql += " and da_tabella = 'test_fatt_acquisto'";
                    Db.executeSql(sql);
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

}//GEN-LAST:event_texNumeFocusLost

private void texNumeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texNumeFocusGained
    old_id = texNume.getText();
    id_modificato = false;
}//GEN-LAST:event_texNumeFocusGained

private void texDataFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texDataFocusGained
    old_anno = getAnnoDaForm();
    old_data = texData.getText();
    old_id = texNume.getText();
    anno_modificato = false;
}//GEN-LAST:event_texDataFocusGained

private void popGridAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popGridAddActionPerformed
    int numCol = this.griglia.getColumnByName("riga");
    int numRiga = this.griglia.getSelectedRow();
    int value = (Integer) this.griglia.getValueAt(numRiga, numCol);
    Integer id_riga = cu.i(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id")));
    Integer id_padre = cu.i(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id_padre")));

    String codiceListino = "1";

    try {
        codiceListino = Db.lookUp(this.texClie.getText(), "codice", "clie_forn").getString("codice_listino");
    } catch (Exception err) {
        err.printStackTrace();
    }

    int codiceCliente = -1;

    if (this.texClie.getText().length() > 0) {

        try {
            codiceCliente = Integer.parseInt(texClie.getText());
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

//        java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);
//        boolean multiriga = preferences.getBoolean("multiriga", false);
    boolean multiriga = main.fileIni.getValueBoolean("pref", "multiriga", false);

//    if (multiriga == false) {
//        frmNuovRiga frm = new frmNuovRiga(this, this.dati.DB_INSERIMENTO, this.texSeri.getText(), Integer.valueOf(this.texNumePrev.getText()).intValue(), "P", value, Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente);
//        temp.openFrame(frm, 600, 350, 100, 100);
//        frm.setStato();
//    } else {
    try {
        frmNuovRigaDescrizioneMultiRigaNew frm = new frmNuovRigaDescrizioneMultiRigaNew(this, this.dati.DB_INSERIMENTO, this.texSeri.getText(), Integer.valueOf(this.texNume.getText()).intValue(), "P", value, Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente, id_riga, id_padre, getNomeTabRighe());
        int w = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_w", "980"));
        int h = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_h", "460"));
        int top = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_top", "100"));
        int left = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_left", "100"));
        main.getPadre().openFrame(frm, w, h, top, left);
        frm.setStato();
    } catch (Exception e) {
        e.printStackTrace();
    }

//    }
}//GEN-LAST:event_popGridAddActionPerformed

private void texClieFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texClieFocusGained
}//GEN-LAST:event_texClieFocusGained

private void comClieFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comClieFocusGained
    comClieSel_old = comClie.getSelectedIndex();
}//GEN-LAST:event_comClieFocusGained

private void formVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_formVetoableChange
    if (evt.getPropertyName().equals(IS_CLOSED_PROPERTY)) {
        boolean changed = ((Boolean) evt.getNewValue()).booleanValue();
        if (changed) {
            if (dati.dbCheckModificati() || (doc.getTotale() != this.totaleIniziale || !this.pagamentoInizialeGiorno.equals(this.texGiornoPagamento.getText()) || !this.pagamentoIniziale.equals(this.comPaga.getText()))) {
                FxUtils.fadeBackground(butSave, Color.RED);
                int confirm = JOptionPane.showOptionDialog(this,
                        "<html><b>Chiudi " + getTitle() + "?</b><br>Hai fatto delle modifiche e così verranno <b>perse</b> !<br>Per salvarle devi cliccare sul pulsante <b>Salva</b> in basso a sinistra<br>",
                        "Conferma chiusura",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, null, null);
                if (confirm == 0) {
                } else {
                    throw new PropertyVetoException("Cancelled", null);
                }
            }
//            if (dbStato.equalsIgnoreCase(this.DB_INSERIMENTO)) {
            butUndoActionPerformed(null);
//            }
        }

    }
}//GEN-LAST:event_formVetoableChange

private void butImportRigheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butImportRigheActionPerformed
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    JFileChooser fileChoose = new JFileChooser(new File(System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "Export" + File.separator));
    FileFilter filter1 = new FileFilter() {

        public boolean accept(File pathname) {
            if (pathname.getAbsolutePath().endsWith(".csv")) {
                return true;
            } else if (pathname.isDirectory()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return "File CSV (*.csv)";
        }
    };

    fileChoose.addChoosableFileFilter(filter1);
    fileChoose.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

    int ret = fileChoose.showOpenDialog(this);

    if (ret == javax.swing.JFileChooser.APPROVE_OPTION) {
        ret = JOptionPane.showConfirmDialog(this, "Vuoi selezionare un listino prezzi esistente?", "Import CSV", JOptionPane.YES_NO_CANCEL_OPTION);
        String nomeListino = "";
        if (ret == JOptionPane.CANCEL_OPTION) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
        } else if (ret == JOptionPane.YES_OPTION) {
            JDialogChooseListino dialog = new JDialogChooseListino(main.getPadre(), true);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            nomeListino = dialog.listinoChoose;

            if (nomeListino.equals("")) {
                nomeListino = "FromFile";
                JOptionPane.showMessageDialog(this, "Non hai scelto nessun listino. Il file verrà caricato con i prezzi interni al file stesso", "Errore Selezione", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            nomeListino = "FromFile";
        }
        try {
            //apro il file
            File f = fileChoose.getSelectedFile();
            String serie = texSeri.getText();
            int numero = Integer.valueOf(this.texNume.getText()).intValue();
            int anno = Integer.valueOf(this.texAnno.getText()).intValue();
            int idPadre = this.id;
            InvoicexUtil.importCSV(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, f, serie, numero, anno, idPadre, nomeListino);
            InvoicexUtil.aggiornaTotaliRighe(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA, idPadre, prezzi_ivati_virtual.isSelected());
            griglia.dbRefresh();
            ricalcolaTotali();
            JOptionPane.showMessageDialog(this, "Righe caricate correttamente", "Esecuzione terminata", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_butImportRigheActionPerformed

private void grigliaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMousePressed
    if (evt.isPopupTrigger()) {
        iu.impostaRigaSopraSotto(griglia, popGridAdd, popGrig, evt);
    }
}//GEN-LAST:event_grigliaMousePressed

private void grigliaMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseReleased
    if (evt.isPopupTrigger()) {
        iu.impostaRigaSopraSotto(griglia, popGridAdd, popGrig, evt);
    }
}//GEN-LAST:event_grigliaMouseReleased

private void datiComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_datiComponentResized
}//GEN-LAST:event_datiComponentResized

private void popDuplicaRigheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popDuplicaRigheActionPerformed
    if (griglia.getRowCount() <= 0) {
        SwingUtils.showErrorMessage(this, "Seleziona una riga prima!");
        return;
    }

    String sql;
    String sqlC = "";
    String sqlV = "";

    int numDup = griglia.getSelectedRows().length;
    int res;
    //chiedo conferma per eliminare il documento
    if (numDup > 1) {
        String msg = "Sicuro di voler duplicare " + numDup + " Righe ?";
        res = JOptionPane.showConfirmDialog(this, msg);
    } else {
        res = JOptionPane.OK_OPTION;
    }

    if (res == JOptionPane.OK_OPTION) {
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        for (int sel : griglia.getSelectedRows()) {

            int dbId = Integer.parseInt(String.valueOf(griglia.getValueAt(sel, griglia.getColumnByName("id"))));

            //cerco ultimo numero ordine
            int newNumero = 1;
            sqlC = "";
            sqlV = "";

            try {
                int dbIdPadre = (Integer) DbUtils.getObject(Db.getConn(), "SELECT id_padre FROM righ_fatt_acquisto WHERE id = " + Db.pc(dbId, Types.INTEGER));
                sql = "SELECT MAX(riga) as maxnum FROM righ_fatt_acquisto WHERE id_padre = " + Db.pc(dbIdPadre, Types.INTEGER);
                ResultSet tempUltimo = Db.openResultSet(sql);
                if (tempUltimo.next() == true) {
                    newNumero = tempUltimo.getInt("maxnum") + InvoicexUtil.getRigaInc();
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

            sql = "select * from righ_fatt_acquisto where id = " + Db.pc(dbId, Types.INTEGER);
            ResultSet tempPrev2 = Db.openResultSet(sql);
            try {
                ResultSetMetaData metaPrev2 = tempPrev2.getMetaData();

                while (tempPrev2.next() == true) {
                    sqlC = "";
                    sqlV = "";
                    for (int i = 1; i <= metaPrev2.getColumnCount(); i++) {
                        if (!metaPrev2.getColumnName(i).equalsIgnoreCase("id")) {
                            if (metaPrev2.getColumnName(i).equalsIgnoreCase("riga")) {
                                sqlC += "riga";
                                sqlV += Db.pc(newNumero, metaPrev2.getColumnType(i));
                            } else {
                                sqlC += metaPrev2.getColumnName(i);
                                sqlV += Db.pc(tempPrev2.getObject(i), metaPrev2.getColumnType(i));
                            }
                            if (i != metaPrev2.getColumnCount()) {
                                sqlC += ",";
                                sqlV += ",";
                            }
                        }
                    }
                    sql = "insert into righ_fatt_acquisto ";
                    sql += "(" + sqlC + ") values (" + sqlV + ")";
                    System.out.println("duplica righe:" + sql);
                    Db.executeSql(sql);
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

        }
        griglia.dbRefresh();
        this.ricalcolaTotali();

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}//GEN-LAST:event_popDuplicaRigheActionPerformed

private void prezzi_ivati_virtualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prezzi_ivati_virtualActionPerformed
    prezzi_ivati.setSelected(prezzi_ivati_virtual.isSelected());
    InvoicexUtil.aggiornaTotaliRighe(Db.TIPO_DOCUMENTO_FATTURA, this.id, prezzi_ivati_virtual.isSelected());
    dbAssociaGrigliaRighe();
    ricalcolaTotali();
}//GEN-LAST:event_prezzi_ivati_virtualActionPerformed

    private void apriclientiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apriclientiActionPerformed
        //    alRicercaCliente.showHints();
        if (texCliente.getText().trim().length() == 0) {
            al_clifor.showHints();
            al_clifor.updateHints(null);
            al_clifor.showHints();
        } else {
            al_clifor.showHints();
        }
        //    al_clifor.showHints();
    }//GEN-LAST:event_apriclientiActionPerformed

    private void texScontoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScontoKeyPressed

    }//GEN-LAST:event_texScontoKeyPressed

    private void texScontoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScontoKeyReleased
        double valore = CastUtils.toDouble0(texSconto.getText());
        if (valore < 0) {
            valore = Math.abs(valore);
            texSconto.setText(FormatUtils.formatEuroIta(valore));
        }
        ricalcolaTotali();
    }//GEN-LAST:event_texScontoKeyReleased

    private void butImportXlsCirriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butImportXlsCirriActionPerformed
        InvoicexUtil.importRigheXlsCirri(this);
    }//GEN-LAST:event_butImportXlsCirriActionPerformed

    private void texSeri1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texSeri1FocusLost
        segnalaDuplicato();
    }//GEN-LAST:event_texSeri1FocusLost

    private void texNumePrev2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texNumePrev2FocusLost
        segnalaDuplicato();
    }//GEN-LAST:event_texNumePrev2FocusLost

    private void texData1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texData1FocusLost
        segnalaDuplicato();
    }//GEN-LAST:event_texData1FocusLost

    private void butAddClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAddClieActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        InvoicexUtil.genericFormAddCliente(this);
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_butAddClieActionPerformed

    private void menClienteNuovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menClienteNuovoActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        InvoicexUtil.genericFormAddCliente(this);
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menClienteNuovoActionPerformed

    private void menClienteModificaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menClienteModificaActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        InvoicexUtil.genericFormEditCliente(this, texClie.getText());
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menClienteModificaActionPerformed

    private void texClienteMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_texClienteMousePressed
        if (evt.isPopupTrigger()) {
            menClientePopup.show(texCliente, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_texClienteMousePressed

    private void texClienteMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_texClienteMouseReleased
        if (evt.isPopupTrigger()) {
            menClientePopup.show(texCliente, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_texClienteMouseReleased

    private void comCampoLibero1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comCampoLibero1FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_comCampoLibero1FocusGained

    private void comCampoLibero1PopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_comCampoLibero1PopupMenuWillBecomeVisible
        if (comCampoLibero1.getItemCount() == 0) {
            InvoicexUtil.caricaComboTestateCampoLibero1(comCampoLibero1);
        }
    }//GEN-LAST:event_comCampoLibero1PopupMenuWillBecomeVisible

    private void menColAggNoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menColAggNoteActionPerformed
        main.fileIni.setValue("pref", "ColAgg_righe_note", menColAggNote.isSelected());
        java.util.Hashtable colsWidthPerc = griglia.columnsSizePerc;
        if (main.fileIni.getValueBoolean("pref", "ColAgg_righe_note", false)) {
            colsWidthPerc.put("note", 15d);
        }
        griglia.columnsSizePercOrig = null;
        dbAssociaGrigliaRighe();
    }//GEN-LAST:event_menColAggNoteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton apriclienti;
    private javax.swing.JButton butAddClie;
    private javax.swing.JButton butImportRighe;
    private javax.swing.JButton butImportXlsCirri;
    public javax.swing.JButton butNuovArti;
    private javax.swing.JButton butSave;
    private javax.swing.JButton butScad;
    public javax.swing.JButton butUndo;
    private tnxbeans.tnxComboField comCampoLibero1;
    private tnxbeans.tnxComboField comClie;
    private tnxbeans.tnxComboField comPaga;
    public tnxbeans.tnxDbPanel dati;
    public tnxbeans.tnxDbPanel datiAltro;
    private tnxbeans.tnxDbPanel datiRighe;
    private javax.swing.JTable foglio3;
    public tnxbeans.tnxDbGrid griglia;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel113;
    private javax.swing.JLabel jLabel114;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel151;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    public javax.swing.JPanel jPanel4;
    public javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labCampoLibero1;
    private javax.swing.JLabel labGiornoPagamento;
    private javax.swing.JLabel labScon1;
    private javax.swing.JLabel labScon2;
    private javax.swing.JLabel labScon21;
    private javax.swing.JMenuItem menClienteModifica;
    private javax.swing.JMenuItem menClienteNuovo;
    private javax.swing.JPopupMenu menClientePopup;
    private javax.swing.JMenu menColAgg;
    private javax.swing.JCheckBoxMenuItem menColAggNote;
    private javax.swing.JPanel panDati;
    public javax.swing.JPanel panRitenute;
    public javax.swing.JPanel pan_segnaposto_deposito;
    private javax.swing.JMenuItem popDuplicaRighe;
    private javax.swing.JPopupMenu popFoglio;
    private javax.swing.JMenuItem popFoglioElimina;
    private javax.swing.JMenuItem popGridAdd;
    private javax.swing.JPopupMenu popGrig;
    private javax.swing.JMenuItem popGrigElim;
    private javax.swing.JMenuItem popGrigModi;
    public tnxbeans.tnxCheckBox prezzi_ivati;
    private javax.swing.JCheckBox prezzi_ivati_virtual;
    private javax.swing.JSplitPane split;
    public javax.swing.JTabbedPane tabDocumento;
    private tnxbeans.tnxTextField texAnno;
    public tnxbeans.tnxTextField texClie;
    public javax.swing.JTextField texCliente;
    private tnxbeans.tnxTextField texData;
    private tnxbeans.tnxTextField texData1;
    public tnxbeans.tnxTextField texGiornoPagamento;
    private tnxbeans.tnxMemoField texNote;
    private tnxbeans.tnxMemoField texNote1;
    private tnxbeans.tnxTextField texNume;
    private tnxbeans.tnxTextField texNumePrev2;
    public tnxbeans.tnxTextField texScon1;
    public tnxbeans.tnxTextField texScon2;
    public tnxbeans.tnxTextField texScon3;
    public tnxbeans.tnxTextField texSconto;
    private tnxbeans.tnxTextField texSeri;
    private tnxbeans.tnxTextField texSeri1;
    public tnxbeans.tnxTextField texSpeseIncasso;
    public tnxbeans.tnxTextField texSpeseTrasporto;
    private tnxbeans.tnxTextField texTipoFattura;
    public tnxbeans.tnxTextField texTota;
    private tnxbeans.tnxTextField texTota1;
    public tnxbeans.tnxTextField texTotaImpo;
    private tnxbeans.tnxTextField texTotaImpo1;
    public tnxbeans.tnxTextField texTotaIva;
    private tnxbeans.tnxTextField texTotaIva1;
    // End of variables declaration//GEN-END:variables

    void dbAssociaGrigliaRighe() {

        if (main.fileIni.getValueBoolean("pref", "ColAgg_righe_note", false)) {
            menColAggNote.setSelected(true);
        }

        String campi = "serie,";
        campi += "numero,";
        campi += "anno,";
        campi += "riga,";
        campi += "codice_articolo as articolo,";
        campi += "descrizione,";
        campi += "r.id,";
        campi += "um,";

        //campi += "quantita,";
        campi += "REPLACE(REPLACE(REPLACE(FORMAT(quantita,2),'.','X'),',','.'),'X',',') AS quantita,";
        campi += "prezzo, ";
        campi += "sconto1, ";
        campi += "sconto2, ";
        campi += "(totale_imponibile) as Totale ";
        campi += ", (totale_ivato) as Ivato ";
        campi += ", iva ";
        if (main.isPluginContabilitaAttivo()) {
            campi += ", conto";
        }
        
        if (main.fileIni.getValueBoolean("pref", "ColAgg_righe_note", false)) {
            campi += ", n.note";
        }

//        String sql = "select " + campi + " from righ_fatt_acquisto" + " where serie = " + db.pc(this.prev.serie, "VARCHAR") + " and numero = " + this.prev.numero + " and anno = " + db.pc(this.prev.anno, "INTEGER") + " order by riga";
        String sql = "select " + campi + " from " + getNomeTabRighe() + " r ";
        if (main.fileIni.getValueBoolean("pref", "ColAgg_righe_note", false)) {
            sql += " left join note n on n.tabella = '" + getNomeTabRighe() + "' and n.id_tab = r.id";
        }                
        sql += " where r.id_padre = " + id;
        sql += " order by riga";

        //debug
        //javax.swing.JOptionPane.showMessageDialog(null,sql);
        System.out.println("sql associa griglia:" + sql);
        //this.sqlGriglia = sql;
        this.griglia.dbOpen(db.getConn(), sql);
    }
    
    public String getNomeTabRighe() {
        if (table_righe_temp != null) {
            return table_righe_temp;
        }
        return "righ_fatt" + suff;
    }
    
    public String getNomeTabRigheLotti() {
        if (table_righe_lotti_temp != null) {
            return table_righe_lotti_temp;
        }
        return "righ_fatt" + suff + "_lotti";
    }

    public String getNomeTabRigheMatricole() {
        if (table_righe_matricole_temp != null) {
            return table_righe_matricole_temp;
        }
        return "righ_fatt" + suff + "_matricole";
    }

    
    public void recuperaDatiCliente() {
        try {
            if (this.texClie.getText().length() > 0) {
                this.dbdoc.forceCliente(Long.parseLong(this.texClie.getText()));

                //li recupero dal cliente
                ResultSet tempClie;
                sql = "select * from clie_forn";
                sql += " where codice = " + Db.pc(this.texClie.getText(), "NUMERIC");
                tempClie = Db.openResultSet(sql);
                try {
                    if (tempClie.next() == true) {

                        //tecnospurghi preferisce che rimanga con il codice fra parentesi quadre dopo la selezione
//                        texCliente.setText(cu.s(tempClie.getString("ragione_sociale")));
                        if (Db.nz(tempClie.getString("pagamento"), "").length() > 0) {
                            this.comPaga.dbTrovaRiga(Db.nz(tempClie.getString("pagamento"), ""));
                        }
                        comPagaFocusLost(null);

                        //carico sconti
                        texScon1.setText(FormatUtils.formatPerc(tempClie.getObject("sconto1t"), true));
                        texScon2.setText(FormatUtils.formatPerc(tempClie.getObject("sconto2t"), true));
                        texScon3.setText(FormatUtils.formatPerc(tempClie.getObject("sconto3t"), true));

                        //leggere listino del cliente per prezzi_ivati o meno
                        boolean prezzi_ivati_b = false;
                        try {
                            String prezzi_ivati_s = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select l.prezzi_ivati from clie_forn c join tipi_listino l on c.codice_listino = l.codice where c.codice = " + Db.pc(this.texClie.getText(), "NUMERIC")));
                            if (prezzi_ivati_s.equalsIgnoreCase("S")) {
                                prezzi_ivati_b = true;
                            }
                        } catch (Exception e) {
                            //prendo base da impostazioni
                            String prezzi_ivati_s = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select l.prezzi_ivati from dati_azienda a join tipi_listino l on a.listino_base = l.codice"));
                            if (prezzi_ivati_s.equalsIgnoreCase("S")) {
                                prezzi_ivati_b = true;
                            }
                        }
                        if (prezzi_ivati_virtual.isSelected() != prezzi_ivati_b) {
                            prezzi_ivati_virtual.setSelected(prezzi_ivati_b);
                            prezzi_ivati.setSelected(prezzi_ivati_b);
                            prezzi_ivati_virtualActionPerformed(null);
                        }

                        aggiornaNote();

                        InvoicexUtil.fireEvent(this, InvoicexEvent.TYPE_FRMTESTFATTACQUISTO_CARICA_DATI_CLIENTE, tempClie);

                    }
                } catch (Exception err) {
                    err.printStackTrace();
                }

            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    private void aggiornaNote() {
        if (loading) {
            return;
        }
        //note automatiche
        String sqlTmp = "SELECT note_fatt_acq FROM clie_forn where codice = " + Db.pc(String.valueOf(comClie.getSelectedKey()), "NUMERIC");
        ResultSet res = Db.openResultSet(sqlTmp);
        try {
            if (res.next()) {
                String key_standard = "noteStandardAcquisto";
                String key_cliente = "note_fatt_acq";

                String note_standard = main.fileIni.getValue("pref", key_standard, "");
                String note_cliente = cu.s(res.getString(key_cliente));

                texNote.setText(note_standard);
                texNote.setText(texNote.getText() + (StringUtils.isNotBlank(texNote.getText()) ? "\n" : "") + note_cliente);

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void trovaAbi() {
    }

    private void trovaCab() {
    }

    private boolean controlloCampi() {
        //controllo data
        if (!ju.isValidDateIta(texData.getText())) {
            texData.requestFocus();
            javax.swing.JOptionPane.showMessageDialog(this, "Data del documento non valida");
            return false;
        }

        //controllo cliente
        //li recupero dal cliente
        ResultSet tempClie;
        String sql = "select * from clie_forn";
        sql += " where codice = " + Db.pc(this.texClie.getText(), "NUMERIC");
        tempClie = Db.openResultSet(sql);
        try {
            if (tempClie.next() != true) {
                javax.swing.JOptionPane.showMessageDialog(this, "Il codice cliente specificato non esiste in anagrafica !");
                return false;
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        if (StringUtils.isEmpty(texNumePrev2.getText()) || StringUtils.isEmpty(texData1.getText())) {
            SwingUtils.showErrorMessage(this, "Inserire obbligatoriamente il numero e la data del documento esterno");
            return false;
        }

        if (!ju.isValidDateIta(texData1.getText())) {
            SwingUtils.showInfoMessage(this, "Inserire una data valida della data del documento esterno");
            return false;
        }

        if (!checkDuplicato()) {
            SwingUtils.showInfoMessage(this, "Esiste già una fattura con gli stessi dati del documento esterno per questo fornitore");
            return false;
        }

        //controllo pagamento
        ResultSet temp;
        boolean ok = true;
        boolean flagCoordinate = false;
        sql = "select * from pagamenti where codice = " + Db.pc(this.comPaga.getSelectedKey().toString(), "VARCHAR");
        temp = Db.openResultSet(sql);
        try {
            if (temp.next() == true && comPaga.getSelectedKey().toString().length() > 0) {
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Manca il tipo di pagamento (e' obbligatorio)", "Attenzione", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        
        //controllo tipo pagamento
        if (!InvoicexUtil.controlloGiornoPagamento(String.valueOf(comPaga.getSelectedKey()), texGiornoPagamento.getText(), this)) {
            return false;
        }

        return true;
    }

    private void showPrezziFatture() {

        frmPrezziFatturePrecedenti form = new frmPrezziFatturePrecedenti(Integer.parseInt(this.texClie.getText().toString()), null, Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA);
        main.getPadre().openFrame(form, 450, 500, this.getX() + this.getWidth() - 200, this.getY() + 50);
    }

    public String getSerie() {

        return this.texSeri.getText();
    }

    public String getNumero() {

        return this.texNume.getText();
    }

    public String getAnno() {

        return this.texAnno.getText();
    }

    public void ricalcolaTotali() {
        if (loading) {
            return;
        }
        try {

            //this.parent.prev.dbRefresh();
            //provo con nuova classe Documento
            if (texClie.getText() != null && texClie.getText().length() > 0 && !texClie.getText().equals("fornitore")) {
                doc.setCodiceCliente(Long.parseLong(texClie.getText()));
            }

            doc.setScontoTestata1(Db.getDouble(texScon1.getText()));
            doc.setScontoTestata2(Db.getDouble(texScon2.getText()));
            doc.setScontoTestata3(Db.getDouble(texScon3.getText()));
            doc.setSpeseIncasso(Db.getDouble(texSpeseIncasso.getText()));
            doc.setSpeseTrasporto(Db.getDouble(texSpeseTrasporto.getText()));
            doc.setPrezziIvati(prezzi_ivati.isSelected());
            doc.setSconto(Db.getDouble(texSconto.getText()));

            doc.setData(null);
            try {
                SimpleDateFormat datef = null;
                if (texData.getText().length() == 8 || texData.getText().length() == 7) {
                    datef = new SimpleDateFormat("dd/MM/yy");
                } else if (texData.getText().length() == 10 || texData.getText().length() == 9) {
                    datef = new SimpleDateFormat("dd/MM/yyyy");
                }
                if (datef != null) {
                    Calendar cal = Calendar.getInstance();
                    datef.setLenient(true);
                    cal.setTime(datef.parse(texData.getText()));
                    doc.setData(new java.sql.Date(cal.getTime().getTime()));
                }
            } catch (Exception err) {
                System.out.println("err:" + err);
            }

            doc.calcolaTotali();
            try {
                InvoicexEvent event = new InvoicexEvent(this);
                event.type = InvoicexEvent.TYPE_FRMTESTFATT_RICALCOLA_TOTALI_4;
                main.events.fireInvoicexEvent(event);
            } catch (Exception err) {
                err.printStackTrace();
            }
            doc.calcolaTotali();
            texTota.setText(it.tnx.Util.formatValutaEuro(doc.getTotale()));
            texTotaImpo.setText(it.tnx.Util.formatValutaEuro(doc.getTotaleImponibile()));
            texTotaIva.setText(it.tnx.Util.formatValutaEuro(doc.getTotaleIva()));

            //test rivalsa inps
            Component comp = SwingUtils.getCompByName(jPanel4, "texRivalsaPerc");
            if (comp != null) {
//                labRivInps.setText("rivvvvvv");
                System.out.println("comp:" + comp);
            }

            try {
                InvoicexEvent event = new InvoicexEvent(this);
                event.type = InvoicexEvent.TYPE_FRMTESTFATT_RICALCOLA_TOTALI_3;
                main.events.fireInvoicexEvent(event);
            } catch (Exception err) {
                err.printStackTrace();
            }
        } catch (Exception err) {
            err.printStackTrace();
            System.out.println("texClie.getText():" + texClie.getText());
        }
    }

    private String getAnnoDaForm() {
        try {
            SimpleDateFormat datef = null;
            if (texData.getText().length() == 8 || texData.getText().length() == 7) {
                datef = new SimpleDateFormat("dd/MM/yy");
            } else if (texData.getText().length() == 10 || texData.getText().length() == 9) {
                datef = new SimpleDateFormat("dd/MM/yyyy");
            } else {
                return "";
            }
            Calendar cal = Calendar.getInstance();
            datef.setLenient(true);
            cal.setTime(datef.parse(texData.getText()));
            return String.valueOf(cal.get(Calendar.YEAR));
        } catch (Exception err) {
            return "";
        }
    }

    private String getAnnoFornitoreDaForm() {
        try {
            SimpleDateFormat datef = null;
            if (texData1.getText().length() == 8 || texData1.getText().length() == 7) {
                datef = new SimpleDateFormat("dd/MM/yy");
            } else if (texData1.getText().length() == 10 || texData1.getText().length() == 9) {
                datef = new SimpleDateFormat("dd/MM/yyyy");
            } else {
                return "";
            }
            Calendar cal = Calendar.getInstance();
            datef.setLenient(true);
            cal.setTime(datef.parse(texData1.getText()));
            return String.valueOf(cal.get(Calendar.YEAR));
        } catch (Exception err) {
            return "";
        }
    }

    public void aggiornareProvvigioni() {
        System.out.println("aggiornare provvigione, no per fatt. acquisto");
    }

    public JTable getGrid() {
        return griglia;
    }

    public tnxDbPanel getDatiPanel() {
        return dati;
    }

    public JTabbedPane getTab() {
        return tabDocumento;
    }

    public boolean isAcquisto() {
        return true;
    }

    public void selezionaCliente() {
        String sqlTmp = "SELECT note, note_automatiche as auto FROM clie_forn where codice = " + Db.pc(String.valueOf(comClie.getSelectedKey()), "NUMERIC");
        ResultSet noteauto = Db.openResultSet(sqlTmp);
        try {
            if (noteauto.next()) {
                String auto = noteauto.getString("auto");
                String nota = noteauto.getString("note");
                if (auto != null && auto.equals("S")) {
                    if (!texNote.getText().equals("") && !texNote.getText().equals(nota)) {
                        this.texNote.setText(nota);
                    } else {
                        this.texNote.setText(noteauto.getString("note"));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        recuperaDatiCliente();
        ricalcolaTotali();
    }

    public Integer getId() {
        return this.id;
    }

    public boolean isPrezziIvati() {
        return prezzi_ivati_virtual.isSelected();
    }

    private void porto_in_temp() {
        if (!main.edit_doc_in_temp) {
            String sql = "check table righ_fatt_acquisto_temp";
            try {
                DbUtils.dumpResultSet(Db.getConn(), sql);
                ResultSet r = Db.openResultSet(sql);
                if (r.next()) {
                    if (!r.getString("Msg_text").equalsIgnoreCase("OK")) {
                        SwingUtils.showErrorMessage(main.getPadre(), "Errore durante il controllo della tabella temporanea [1]");
                    }
                } else {
                    SwingUtils.showErrorMessage(main.getPadre(), "Errore durante il controllo della tabella temporanea [2]");
                }
            } catch (Exception e) {
                SwingUtils.showErrorMessage(main.getPadre(), "Errore durante il controllo della tabella temporanea\n" + e.toString());
            }

            //tolgo le righe da temp che tanto non serbono +
            sql = "delete from righ_fatt_acquisto_temp";
            sql += " where id_padre = " + frmTestFattAcquisto.this.id;
            sql += " and username = '" + main.login + "'";
            Db.executeSqlDialogExc(sql, true);

            sql = "delete te.* from righ_fatt_acquisto_temp te join righ_fatt_acquisto ri on te.id = ri.id";
            sql += " and ri.serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
            sql += " and ri.numero " + Db.pcW(String.valueOf(this.dbdoc.numero), "NUMBER");
            sql += " and ri.anno " + Db.pcW(String.valueOf(this.dbdoc.anno), "VARCHAR");
            sql += " and te.username = '" + main.login + "'";
            Db.executeSqlDialogExc(sql, true);

            //e inserisco
            sql = "insert into righ_fatt_acquisto_temp";
            sql += " select *, '" + main.login + "' as username";
            sql += " from righ_fatt_acquisto";
            sql += " where id_padre = " + frmTestFattAcquisto.this.id;
            Db.executeSqlDialogExc(sql, true);
        } else {
            System.out.println("porto_in_temp, NO per edit in mem");
        }
    }

    public JLabel getCampoLibero1Label() {
        return labCampoLibero1;
    }

    public tnxComboField getCampoLibero1Combo() {
        return comCampoLibero1;
    }

    private void controllaPermessiAnagCliFor() {
        butAddClie.setEnabled(false);
        menClienteNuovo.setEnabled(false);
        menClienteModifica.setEnabled(false);
        if (main.utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_CLIENTI, Permesso.PERMESSO_TIPO_SCRITTURA)) {
            butAddClie.setEnabled(true);
            menClienteNuovo.setEnabled(true);
            menClienteModifica.setEnabled(true);
        }
    }

    public void selezionaCliente(String codice) {
        texClie.setText(codice);
        try {
            String cliente = (String) DbUtils.getObject(Db.getConn(), "select ragione_sociale from clie_forn where codice = " + Db.pc(texClie.getText(), Types.INTEGER));
            texCliente.setText(cliente);
        } catch (Exception e) {
            e.printStackTrace();
        }        
        recuperaDatiCliente();
    }

}
