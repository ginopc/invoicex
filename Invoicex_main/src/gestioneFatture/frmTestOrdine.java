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
import it.tnx.commons.AutoCompletionEditable;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugFastUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.FxUtils;
import it.tnx.commons.JUtil;
import it.tnx.commons.MicroBench;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.commons.fu;
import it.tnx.commons.ju;
import it.tnx.commons.table.EditorUtils;
import it.tnx.commons.table.RendererUtils;
import it.tnx.gui.DateDocument;
import it.tnx.gui.JTableSs;
import it.tnx.gui.MyBasicArrowButton;
import it.tnx.gui.StyledComboBoxUI;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.MyAbstractListIntelliHints;
import it.tnx.invoicex.gui.utils.CellEditorFoglioOrdine;
import it.tnx.invoicex.gui.utils.ComboBoxRenderer2;
import it.tnx.invoicex.gui.utils.DataModelFoglioA;
import it.tnx.invoicex.gui.utils.DataModelFoglioB;
import it.tnx.invoicex.iu;
import it.tnx.invoicex.sync.Sync;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.*;

import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.EdgedBalloonStyle;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.jdesktop.swingworker.SwingWorker;
import tnxbeans.LimitedTextPlainDocument;
import tnxbeans.SeparatorComboBoxRenderer;
import tnxbeans.tnxComboField;
import tnxbeans.tnxDbGrid;
import tnxbeans.tnxDbPanel;
import tnxbeans.tnxTextField;

public class frmTestOrdine
        extends javax.swing.JInternalFrame
        implements GenericFrmTest {

    public dbOrdine dbdoc = new dbOrdine();
    private Documento doc = new Documento();
    public frmElenOrdini from;
    private Db db = Db.INSTANCE;
    int pWidth;
    int pHeight;
    public String dbStato = "L";
    public static String DB_INSERIMENTO = "I";
    public static String DB_MODIFICA = "M";
    public static String DB_LETTURA = "L";
    private String sql = "";
    private double totaleDaPagareFinaleIniziale;
    private String pagamentoIniziale;
    //private int tempTipoFatt = 0;
    //per controllare le provvigioni
    private double provvigioniIniziale;
    private int codiceAgenteIniziale;
    private String pagamentoInizialeGiorno;
    java.util.Timer tim;
    private String old_id = "";
    private boolean id_modificato = false;
    private String old_anno = "";
    private String old_data = "";
    private boolean anno_modificato = false;
    private int comClieSel_old = -1;
    private int comClieDest_old = -1;
    private String serie_originale = null;
    private Integer numero_originale = null;
    private Integer anno_originale = null;
    private String data_originale = null;

    javax.swing.JInternalFrame zoomA;
    javax.swing.JInternalFrame zoomB;
    FoglioSelectionListener foglioSelList;
    public boolean acquisto = false;
    public String suff = "";
    private String ccliente = "cliente";
    private boolean loading = true;
    public String tipoSNJ = "";
    public boolean loadingFoglio = false;
    private DataModelFoglioA foglioDataA;
    private DataModelFoglioB foglioDataB;
    private String sqlGrigliaA;
    private String sqlGrigliaB;
    public Integer id = null;
    private boolean block_aggiornareProvvigioni;

    MyAbstractListIntelliHints al_clifor = null;
    AtomicReference<ClienteHint> clifor_selezionato_ref = new AtomicReference(null);

    public tnxbeans.tnxComboField deposito;
    public javax.swing.JLabel labDeposito;
    public tnxDbPanel pan_deposito = null;

    private JCheckBox consegnatocheckbox = new JCheckBox();
    private MyCheckBoxEditor consegnatoeditor = new MyCheckBoxEditor(consegnatocheckbox);

    boolean chiudere = true;

    public String table_righe_temp = null;
    public String table_righe_lotti_temp = null;

    public boolean forza_vis_fido = false;

    public String prevStato;

    MyAbstractListIntelliHints al_for = null;
    AtomicReference<ClienteHint> for_selezionato_ref = new AtomicReference(null);

    /**
     * Creates new form frmElenPrev
     */
    public frmTestOrdine(String dbStato, String dbSerie, int dbNumero, String prevStato, int dbAnno, int tipoOrdine) {
        this(dbStato, dbSerie, dbNumero, prevStato, dbAnno, tipoOrdine, -1);
    }

    public frmTestOrdine(String dbStato, String dbSerie, int dbNumero, String prevStato, int dbAnno, int tipoOrdine, int dbIdOrdine) {
        this(dbStato, dbSerie, dbNumero, prevStato, dbAnno, tipoOrdine, dbIdOrdine, false);
    }

    public frmTestOrdine(String dbStato, String dbSerie, int dbNumero, String prevStato, int dbAnno, int tipoOrdine, int dbIdOrdine, boolean acquisto) {
        this(dbStato, dbSerie, dbNumero, prevStato, dbAnno, tipoOrdine, dbIdOrdine, acquisto, "");
    }

    public frmTestOrdine(String dbStato, String dbSerie, int dbNumero, String prevStato, int dbAnno, int tipoOrdine, int dbIdOrdine, boolean acquisto, String tipoSNJ) {
        this.prevStato = prevStato;

        if (Sync.isActive() && StringUtils.isNotEmpty(tipoSNJ)) {
            SwingUtils.showErrorMessage(InvoicexUtil.getActiveJInternalFrame(), "SYNC: form ordine non aggiornata per tipoSNJ");
            return;
        }

        loading = true;
        this.id = dbIdOrdine;
        if (!dbStato.equals(tnxDbPanel.DB_INSERIMENTO)) {
            dbdoc.id = dbIdOrdine;
        } else {
            dbdoc.id = -1;
        }
        this.acquisto = acquisto;
        this.tipoSNJ = tipoSNJ;

//        int permesso = Permesso.PERMESSO_ORDINI_VENDITA;
        if (acquisto) {
            suff = "_acquisto";
            ccliente = "fornitore";
            dbdoc.acquisto = true;
//            permesso = Permesso.PERMESSO_ORDINI_ACQUISTO;
        }

        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        this.dbStato = dbStato;

        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setDismissDelay(30000);

        //this.tempTipoFatt = tipoOrdine;
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

        ((JTextField) comStatoOrdine.getEditor().getEditorComponent()).addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popStatoOrdineDef.show(comStatoOrdine, evt.getX(), evt.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popStatoOrdineDef.show(comStatoOrdine, evt.getX(), evt.getY());
                }
            }
        });

        String acqven = acquisto ? " di acquisto" : " di vendita";
        popStatoOrdineDefRendiDef.setText("Imposta come standard per " + (cu.s(prevStato).equalsIgnoreCase("P") ? "Preventivi" : "Ordini") + acqven);

        LimitedTextPlainDocument limit = new LimitedTextPlainDocument(1, true);
        texSeri.setDocument(limit);

        if (acquisto) {
            menClienteControllaFido2.setVisible(false);
        }

        if (!main.getPersonalContain("cirri")) {
            butImportXlsCirri.setVisible(false);
        }

//        if(!main.utente.getPermesso(permesso, Permesso.PERMESSO_TIPO_SCRITTURA)){
//            SwingUtils.showErrorMessage(main.getPadrePanel(), "Non hai i permessi per accedere a questa funzionalità", "Impossibile accedere", true);
//            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//            this.setVisible(false);
//            this.dispose();
//            return;
//        }
        if (main.getPersonalContain("consegna_e_scarico")) {
//            split.setDividerLocation(330);
        } else {
//            split.setDividerLocation(305);
            labModConsegna.setVisible(false);
            labModScarico.setVisible(false);
            comConsegna.setVisible(false);
            comScarico.setVisible(false);

            labNoteConsegna.setVisible(false);
            texNoteConsegna.setVisible(false);
        }

        InvoicexUtil.macButtonSmall(butPrezziPrec);

        AutoCompletionEditable.enable(comCausaleTrasporto);
        AutoCompletionEditable.enable(comAspettoEsterioreBeni);
        AutoCompletionEditable.enable(comVettori);
        AutoCompletionEditable.enable(comMezzoTrasporto);
        AutoCompletionEditable.enable(comPorto);

        DateDocument.installDateDocument(consegna_prevista.getEditor());

        prezzi_ivati.setVisible(false);

        comClie.putClientProperty("JComponent.sizeVariant", "small");
        comClieDest.putClientProperty("JComponent.sizeVariant", "small");
        comAgente.putClientProperty("JComponent.sizeVariant", "small");
        comPaga.putClientProperty("JComponent.sizeVariant", "small");
        comStatoOrdine.putClientProperty("JComponent.sizeVariant", "small");
        stato_evasione.putClientProperty("JComponent.sizeVariant", "small");

        comCausaleTrasporto.putClientProperty("JComponent.sizeVariant", "mini");
        comAspettoEsterioreBeni.putClientProperty("JComponent.sizeVariant", "mini");
        comVettori.putClientProperty("JComponent.sizeVariant", "mini");
        comMezzoTrasporto.putClientProperty("JComponent.sizeVariant", "mini");
        comMezzoTrasporto.putClientProperty("JComponent.sizeVariant", "mini");
        comPorto.putClientProperty("JComponent.sizeVariant", "mini");
        comPaese.putClientProperty("JComponent.sizeVariant", "mini");

        butPrezziPrec.putClientProperty("JComponent.sizeVariant", "mini");

        if (SystemUtils.IS_OS_MAC_OSX) {
//            tutto.setBorder(new MyOsxFrameBorder());

            Border b1 = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.lightGray), BorderFactory.createEmptyBorder(2, 2, 2, 2));
            ArrayList<JComponent> compsb = new ArrayList();
            compsb.add(texSeri);
            compsb.add(texNumeOrdine);
            compsb.add(texData);
            compsb.add(texCliente);
            compsb.add(texScon1);
            compsb.add(texScon2);
            compsb.add(texScon3);
            compsb.add(texSpeseTrasporto);
            compsb.add(texSpeseIncasso);
            compsb.add(texNote);
            compsb.add(texPaga2);
            compsb.add(texConsegna);
            compsb.add((JComponent) comPaga.getEditor().getEditorComponent());
            compsb.add(texGiornoPagamento);
            compsb.add(texNotePagamento);
            compsb.add(texBancAbi);
            compsb.add(texBancCab);
            compsb.add(texBancIban);
            compsb.add((JComponent) comAgente.getEditor().getEditorComponent());
            compsb.add(texProvvigione);
            compsb.add((JComponent) comConsegna.getEditor().getEditorComponent());
            compsb.add((JComponent) comScarico.getEditor().getEditorComponent());

            compsb.add(texFornitoreIntelli);

            compsb.add((JComponent) comCausaleTrasporto.getEditor().getEditorComponent());
            compsb.add((JComponent) comAspettoEsterioreBeni.getEditor().getEditorComponent());
            compsb.add(texNumeroColli);
            compsb.add((JComponent) comVettori.getEditor().getEditorComponent());
            compsb.add((JComponent) comMezzoTrasporto.getEditor().getEditorComponent());
            compsb.add((JComponent) comPorto.getEditor().getEditorComponent());

            for (JComponent c : compsb) {
                c.setBorder(b1);
            }

        }

        try {
            InvoicexEvent event = new InvoicexEvent(this);
            event.type = InvoicexEvent.TYPE_FRMTESTORDI_CONSTR_POST_INIT_COMPS;
            main.events.fireInvoicexEvent(event);
        } catch (Exception err) {
            err.printStackTrace();
        }

        if (StringUtils.isEmpty(tipoSNJ) || acquisto) {
            this.panTab.remove(this.panFoglioRigheSNJA);
            this.panTab.remove(this.panFoglioRigheSNJB);
            dati.remove(this.textTipoSnj);
        } else {
            butNuovArti.setVisible(false);
            this.popGrig.remove(0);
            if (tipoSNJ.equals("A")) {
                this.panTab.remove(this.panFoglioRigheSNJB);
                foglioDataA = new DataModelFoglioA(1000, 11, this);
                foglioTipoA.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
                foglioTipoA.setModel(foglioDataA);

                javax.swing.table.TableColumnModel columns = foglioTipoA.getColumnModel();
                javax.swing.table.TableColumn col = columns.getColumn(0);
                col.setHeaderValue("riga");
                col.setPreferredWidth(0);
                col.setMinWidth(0);
                col.setMaxWidth(0);
                col.setWidth(0);

                col = columns.getColumn(1);
                col.setHeaderValue("Art.");
                col.setPreferredWidth(80);

                col = columns.getColumn(2);
                col.setHeaderValue("Descrizione");
                col.setPreferredWidth(150);

                col = columns.getColumn(3);
                col.setHeaderValue("qta");
                col.setPreferredWidth(40);
                JTextField textfieldeditor = new JTextField();
                EditorUtils.NumberEditor editor1 = new EditorUtils.NumberEditor(textfieldeditor);
                editor1.returnNull = true;
                col.setCellEditor(editor1);
                col.setCellRenderer(new RendererUtils.NumberRenderer(0, 5));

                col = columns.getColumn(4);
                col.setHeaderValue("um");
                col.setPreferredWidth(40);
                tnxbeans.tnxComboField comboUm = new tnxbeans.tnxComboField();
                comboUm.dbAddElement("", "");
                comboUm.dbAddElement("pz", "pz");
                comboUm.dbAddElement("gg", "gg");
                comboUm.dbAddElement("mesi", "mesi");
                col.setCellEditor(new CellEditorFoglioOrdine(comboUm));
                col.setCellRenderer(new ComboBoxRenderer2());

                col = columns.getColumn(5);
                col.setHeaderValue("Importo Unitario");
                col.setPreferredWidth(50);

                EditorUtils.CurrencyEditor editor2 = new EditorUtils.CurrencyEditor(textfieldeditor);
                editor2.returnNull = true;
                col.setCellEditor(editor2);
                col.setCellRenderer(new RendererUtils.CurrencyRenderer(2, 5));

                col = columns.getColumn(6);
                col.setHeaderValue("%");
                col.setPreferredWidth(20);
                tnxbeans.tnxComboField comboPercentuale = new tnxbeans.tnxComboField();
                comboPercentuale.dbAddElement("0", 0);
                comboPercentuale.dbAddElement("10", 10);
                comboPercentuale.dbAddElement("20", 20);
                comboPercentuale.dbAddElement("30", 30);
                comboPercentuale.dbAddElement("40", 40);
                comboPercentuale.dbAddElement("50", 50);
                comboPercentuale.dbAddElement("60", 60);
                comboPercentuale.dbAddElement("70", 70);
                comboPercentuale.dbAddElement("80", 80);
                comboPercentuale.dbAddElement("90", 90);
                comboPercentuale.dbAddElement("100", 100);
                col.setCellEditor(new CellEditorFoglioOrdine(comboPercentuale));
                col.setCellRenderer(new ComboBoxRenderer2());

                col = columns.getColumn(7);
                col.setHeaderValue("Emissione Fattura");
                col.setPreferredWidth(80);
                tnxbeans.tnxComboField comboEmissioneFattura = new tnxbeans.tnxComboField() {
                    {
                        setUI(new StyledComboBoxUI());
                    }
                };
                comboEmissioneFattura.dbOpenList(Db.getConn(), "SELECT descrizione, id FROM tipi_emissione_fattura");
                comboEmissioneFattura.dbAddElement("", "", 0);
                col.setCellEditor(new CellEditorFoglioOrdine(comboEmissioneFattura));
                col.setCellRenderer(new ComboBoxRenderer2());

                col = columns.getColumn(8);
                col.setHeaderValue("Termini di Pagamento");
                col.setPreferredWidth(80);
                tnxbeans.tnxComboField comboPagamenti = new tnxbeans.tnxComboField() {
                    {
                        setUI(new StyledComboBoxUI());
                    }
                };
                comboPagamenti.dbOpenList(Db.getConn(), "SELECT codice, codice FROM pagamenti");
                comboPagamenti.dbAddElement("", "", 0);

                col.setCellEditor(new CellEditorFoglioOrdine(comboPagamenti));
                col.setCellRenderer(new ComboBoxRenderer2());

                col = columns.getColumn(9);
                col.setHeaderValue("Imponibile");
                col.setCellRenderer(new RendererUtils.CurrencyRenderer());

                JTextField textEdit = new javax.swing.JTextField() {
                };

                CellEditorFoglioOrdine edit = new CellEditorFoglioOrdine(textEdit);

                //it.tnx.gui.KeyableCellEditor edit = new it.tnx.gui.KeyableCellEditor();
                //edit.setClickCountToStart(0);
                edit.setClickCountToStart(2);
                foglioTipoA.setDefaultEditor(Object.class, edit);

                for (int i = 0; i < foglioDataA.getRowCount(); i++) {
                    foglioDataA.setValueAt(new Integer((i + 1) * 10), i, 0);
                }

                FoglioSelectionListener foglioSelList = new FoglioSelectionListener(foglioTipoA);
                foglioTipoA.getSelectionModel().addListSelectionListener(foglioSelList);

                foglioTipoA.setColumnModel(columns);

                //rimuovo colonna id riga
                columns.removeColumn(columns.getColumn(10));

                //--- fine foglio ---------
            } else {
                this.panTab.remove(this.panFoglioRigheSNJA);
                foglioDataB = new DataModelFoglioB(1000, 11, this);
                foglioTipoB.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
                foglioTipoB.setModel(foglioDataB);

                javax.swing.table.TableColumnModel columns = foglioTipoB.getColumnModel();
                javax.swing.table.TableColumn col = columns.getColumn(0);

                JTextField textfieldeditor = new JTextField();
                EditorUtils.NumberEditor editorNum = new EditorUtils.NumberEditor(textfieldeditor);
                editorNum.returnNull = true;
                EditorUtils.CurrencyEditor editorCur = new EditorUtils.CurrencyEditor();
                editorCur.returnNull = true;
                RendererUtils.CurrencyRenderer rendCur25 = new RendererUtils.CurrencyRenderer(2, 5);
                RendererUtils.CurrencyRenderer rendCur = new RendererUtils.CurrencyRenderer();
                RendererUtils.NumberRenderer rendNum05 = new RendererUtils.NumberRenderer(0, 5);

                col.setHeaderValue("riga");
                col.setPreferredWidth(0);
                col.setMinWidth(0);
                col.setMaxWidth(0);
                col.setWidth(0);

                col = columns.getColumn(1);
                col.setHeaderValue("Art.");
                col.setPreferredWidth(80);

                col = columns.getColumn(2);
                col.setHeaderValue("Descrizione");
                col.setPreferredWidth(150);

                col = columns.getColumn(3);
                col.setHeaderValue("Costo Giornaliero");
                col.setPreferredWidth(50);
                col.setCellRenderer(rendCur25);
                col.setCellEditor(editorCur);

                col = columns.getColumn(4);
                col.setHeaderValue("Costo Mensile");
                col.setPreferredWidth(50);
                col.setCellRenderer(rendCur25);
                col.setCellEditor(editorCur);

                col = columns.getColumn(5);
                col.setHeaderValue("Durata Consulenza");
                col.setPreferredWidth(80);
                tnxbeans.tnxComboField comboDurataConsulenza = new tnxbeans.tnxComboField();
                comboDurataConsulenza.dbOpenList(Db.getConn(), "SELECT descrizione, id FROM tipi_durata_consulenza");
                col.setCellEditor(new CellEditorFoglioOrdine(comboDurataConsulenza));
                col.setCellRenderer(new ComboBoxRenderer2());

                col = columns.getColumn(6);
                col.setHeaderValue("Durata Contratto");
                col.setPreferredWidth(80);
                tnxbeans.tnxComboField comboDurataContratto = new tnxbeans.tnxComboField();
                comboDurataContratto.dbOpenList(Db.getConn(), "SELECT descrizione, id FROM tipi_durata_contratto");
                col.setCellEditor(new CellEditorFoglioOrdine(comboDurataContratto));
                col.setCellRenderer(new ComboBoxRenderer2());

                col = columns.getColumn(7);
                col.setHeaderValue("Emissione Fattura");
                col.setPreferredWidth(80);
                tnxbeans.tnxComboField comboEmissioneFattura = new tnxbeans.tnxComboField();
                comboEmissioneFattura.dbOpenList(Db.getConn(), "SELECT descrizione, id FROM tipi_emissione_fattura");
                col.setCellEditor(new CellEditorFoglioOrdine(comboEmissioneFattura));
                col.setCellRenderer(new ComboBoxRenderer2());

                col = columns.getColumn(8);
                col.setHeaderValue("Termini di Pagamento");
                col.setPreferredWidth(80);
                tnxbeans.tnxComboField comboPagamenti = new tnxbeans.tnxComboField();
                comboPagamenti.dbOpenList(Db.getConn(), "SELECT codice, codice FROM pagamenti");
                col.setCellEditor(new CellEditorFoglioOrdine(comboPagamenti));
                col.setCellRenderer(new ComboBoxRenderer2());

                col = columns.getColumn(9);
                col.setHeaderValue("Imponibile");
                col.setPreferredWidth(50);
                col.setCellRenderer(rendCur);

                //rimuovo colonna id riga
                columns.removeColumn(columns.getColumn(10));

                JTextField textEdit = new javax.swing.JTextField() {
                };

                CellEditorFoglioOrdine edit = new CellEditorFoglioOrdine(textEdit);

                edit.setClickCountToStart(2);
                foglioTipoB.setDefaultEditor(Object.class, edit);

                for (int i = 0; i < foglioDataB.getRowCount(); i++) {
                    foglioDataB.setValueAt(new Integer((i + 1) * 10), i, 0);
                }

                FoglioSelectionListener foglioSelList = new FoglioSelectionListener(foglioTipoB);
                foglioTipoB.getSelectionModel().addListSelectionListener(foglioSelList);

                foglioTipoB.setColumnModel(columns);
            }
        }

        if (main.getPersonalContain("litri")) {
            butInserisciPeso.setText("Inserisci Tot. Litri");
        }
        texNote.setFont(texSeri.getFont());

        texCliente.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                texCliente.selectAll();
            }
        });

        boolean filtraclifor = main.fileIni.getValueBoolean("pref", "filtraCliFor", false);
        InvoicexUtil.CliforTipo tipo = InvoicexUtil.CliforTipo.Tutti;
        if (filtraclifor) {
            tipo = InvoicexUtil.CliforTipo.Solo_Clienti_Entrambi_Provvisori;
            if (acquisto) {
                tipo = InvoicexUtil.CliforTipo.Solo_Fornitori_Entrambi_Provvisori;
            }
        }
        al_clifor = InvoicexUtil.getCliforIntelliHints(texCliente, this, clifor_selezionato_ref, null, comClieDest, tipo);
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

        if (main.getPersonalContain("proskin")) {
            butImportRigheProskin.setVisible(true);
        } else {
            butImportRigheProskin.setVisible(false);
        }

        if (main.getPersonalContain("intertelecom") && !this.acquisto) {
            this.labRiferimento.setText("Giorni Validità");
        } else {
            this.labRiferimento.setText("Consegna");
        }

//        griglia.setNoTnxResize(true);
        griglia.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        if (acquisto) {
            stato_evasione.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Ricevuto", "Ricevuto Parziale", "Non Ricevuto"}));
        } else {
            stato_evasione.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Evaso", "Evaso Parziale", "Non Evaso"}));
        }
        stato_evasione.setSelectedIndex(2);

        dati.aggiungiDbPanelCollegato(dati_altri1);
        dati.aggiungiDbPanelCollegato(dati_altri2);

        if (acquisto) {
            setTitle("Preventivo/Ordine di Acquisto");
            jLabel151.setText("fornitore");
            texClie.setDbNomeCampo(ccliente);
            texClieDest.setDbNomeCampo("id_" + ccliente + "_destinazione");

            //TODO ***********************************************************************            
            dati.remove(texForni);
            dati.addExcludeComp(texForni);

            dati_altri1.setVisible(false);

            if (main.getPersonalContain("dest_diversa_ordini_fornitori")) {
                dati_altri2.setVisible(true);
//                togliAltriCampiEccDestDiversaForn();
            } else {
                dati_altri2.setVisible(false);
            }

            jLabel17.setText("Rif. Forn.");
            jLabel17.setToolTipText("Inserire il numero dell'ordine assegnato dal fornitore");

            jLabel151.setPreferredSize(new Dimension((int) jLabel151.getPreferredSize().getWidth() + 150, (int) jLabel151.getPreferredSize().getHeight()));

//            split.setDividerLocation(170);
        } else {
            setTitle("Preventivo/Ordine di Vendita");
        }

        //imposto campi piccolini
        texDestRagioneSociale.setMargin(new Insets(0, 0, 0, 0));
        texDestIndirizzo.setMargin(new Insets(0, 0, 0, 0));
        texDestCap.setMargin(new Insets(0, 0, 0, 0));
        texDestLocalita.setMargin(new Insets(0, 0, 0, 0));
        texDestProvincia.setMargin(new Insets(0, 0, 0, 0));
        texDestTelefono.setMargin(new Insets(0, 0, 0, 0));
        texDestCellulare.setMargin(new Insets(0, 0, 0, 0));
        texForni.setMargin(new Insets(0, 0, 0, 0));
        texNumeroColli.setMargin(new Insets(0, 0, 0, 0));
        ((JTextComponent) comPaese.getEditor().getEditorComponent()).setMargin(new Insets(0, 0, 0, 0));
        ((JTextComponent) comCausaleTrasporto.getEditor().getEditorComponent()).setMargin(new Insets(0, 0, 0, 0));
        ((JTextComponent) comAspettoEsterioreBeni.getEditor().getEditorComponent()).setMargin(new Insets(0, 0, 0, 0));
        ((JTextComponent) comMezzoTrasporto.getEditor().getEditorComponent()).setMargin(new Insets(0, 0, 0, 0));
        ((JTextComponent) comPorto.getEditor().getEditorComponent()).setMargin(new Insets(0, 0, 0, 0));
        ((JTextComponent) comVettori.getEditor().getEditorComponent()).setMargin(new Insets(0, 0, 0, 0));

        //init campi particolari
        this.texData.setDbDefault(texData.DEFAULT_CURRENT);

        //oggetto preventivo
        this.dbdoc.dbStato = dbStato;
        this.dbdoc.serie = dbSerie;
        this.dbdoc.numero = dbNumero;
        this.dbdoc.stato = prevStato;
        this.dbdoc.anno = dbAnno;

        //105
        this.dbdoc.tipoOrdine = tipoOrdine;
        this.dbdoc.texTota = this.texTota;
        this.dbdoc.texTotaImpo = this.texTotaImpo;
        this.dbdoc.texTotaIva = this.texTotaIva;
//        this.setClosable(false);

        comPorto.dbAddElement("", "");
        comPorto.dbOpenList(Db.getConn(), "select porto, id from tipi_porto group by porto");
        comMezzoTrasporto.dbAddElement("");
        comMezzoTrasporto.dbOpenList(Db.getConn(), "select nome from tipi_consegna group by nome");
        comCausaleTrasporto.dbAddElement("");
        comCausaleTrasporto.dbOpenList(Db.getConn(), "select nome, id from tipi_causali_trasporto group by nome");

//        comAspettoEsterioreBeni.dbAddElement("");
//        comAspettoEsterioreBeni.dbAddElement("SCATOLA");
//        comAspettoEsterioreBeni.dbAddElement("A VISTA");
//        comAspettoEsterioreBeni.dbAddElement("SCATOLA IN PANCALE");
        comAspettoEsterioreBeni.dbAddElement("");
        comAspettoEsterioreBeni.dbOpenList(Db.getConn(), "select nome, id from tipi_aspetto_esteriore_beni group by nome", null, false);

        comVettori.dbAddElement("");
        comVettori.dbOpenList(Db.getConn(), "select nome,nome from vettori order by nome", null, false);

        //faccio copia in caso di annulla deve rimettere le righe giuste
        if (!main.edit_doc_in_temp) {
            if (dbStato == this.DB_MODIFICA) {
                porto_in_temp();
            }
        } else {
            //porto righe in tabella temporanea e modifica quella temporanea, se poi si conferma le porto nelle righe definitive
            table_righe_temp = InvoicexUtil.getTempTableName("righ_ordi" + suff);
            table_righe_lotti_temp = InvoicexUtil.getTempTableName("righ_ordi_lotti" + suff);
            try {
                InvoicexUtil.createTempTable(dbStato, table_righe_temp, "righ_ordi" + suff, id);
                InvoicexUtil.createTempTable(dbStato, table_righe_lotti_temp, "righ_ordi_lotti" + suff, id);
                dbdoc.table_righe_temp = table_righe_temp;
                if (dbStato.equalsIgnoreCase(this.DB_INSERIMENTO)) {
                    dbdoc.id = -1;
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
            }
        }

        //memorizzo il numero doc originale
        serie_originale = dbSerie;
        numero_originale = dbNumero;
        anno_originale = dbAnno;

        //this.texSeri.setVisible(false);
        //associo il panel ai dati        
        this.dati.dbNomeTabella = "test_ordi" + suff;

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

        //apro la combo pagamenti
        this.comPaga.dbOpenList(db.getConn(), "select codice, codice from pagamenti order by codice", null, false);

        comConsegna.dbOpenList(db.getConn(), "select nome, id from tipi_consegna", null, false);
        comScarico.dbOpenList(db.getConn(), "select nome, id from tipi_scarico", null, false);

        comPaese.dbAddElement("", "");
        comPaese.dbOpenList(Db.getConn(), "select nome, codice1 from stati", null, false);

        //apro combo agenti
        if (main.getPersonalContain("medcomp")) {
            //selezionare gli agenti in base a quelli collegati al cliente fornitore
            comAgente.setRenderer(new SeparatorComboBoxRenderer());
            Integer cod_cliente = null;
            try {
                if (dbIdOrdine != -1) {
                    cod_cliente = cu.toInteger(dbu.getObject(Db.getConn(), "select " + ccliente + " from test_ordi" + suff + " where id = " + dbIdOrdine));
                }
            } catch (Exception e) {
            }
            InvoicexUtil.caricaComboAgentiCliFor(comAgente, cod_cliente);
        } else {
            comAgente.dbOpenList(Db.getConn(), "select nome, id from agenti where id != 0 and IFNULL(nome,'') != '' order by nome", null, false);
        }

        //this.dati.butSave = this.butSave;
        //this.dati.butUndo = this.butUndo;
        //controllo se inserimento o modifica
        if (dbStato.equalsIgnoreCase(this.DB_INSERIMENTO)) {
            this.dati.dbOpen(db.getConn(), "select * from test_ordi" + suff + " limit 0");
        } else {
            sql = "select * from test_ordi" + suff;
//            sql += " where serie = " + db.pc(dbSerie, "VARCHAR");
//            sql += " and numero = " + dbNumero;
//            //sql += " and stato = " + db.pc(prevStato, "VARCHAR");
//            sql += " and anno = " + dbAnno;
            sql += " where id = " + id;
            this.dati.dbOpen(db.getConn(), sql);

            consegna_prevista.setDate(cu.toDate(dati.dbGetField("data_consegna_prevista")));
        }

        this.dati.dbRefresh();
        this.dbdoc.dbRefresh();

        //apro la combo clienti
        this.comClie.setDbTextAbbinato(this.texClie);
        this.texClie.setDbComboAbbinata(this.comClie);
        this.comClie.dbOpenList(db.getConn(), "select ragione_sociale,codice from clie_forn where ragione_sociale != '' order by ragione_sociale", this.texClie.getText());

        //apro combo destinazione cliente
        comClieDest.dbTrovaMentreScrive = false;
        sql = "select ragione_sociale, id from clie_forn_dest";
        sql += " where codice_cliente = " + Db.pc(this.texClie.getText(), "NUMERIC");
        sql += " order by ragione_sociale";
        riempiDestDiversa(sql);

//        comForni.dbOpenList(Db.getConn(), "select ragione_sociale,codice from clie_forn order by ragione_sociale", this.texForni.getText());
//        comForni.setSelectedIndex(-1);
        texFornitoreIntelli.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                texFornitoreIntelli.selectAll();
            }
        });
        al_for = InvoicexUtil.getCliforIntelliHints(texFornitoreIntelli, this, for_selezionato_ref, null, texScon1);
        al_for.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("selezionato")) {
                    ClienteHint hint = (ClienteHint) for_selezionato_ref.get();
                    if (hint != null) {
                        texFornitoreIntelli.setText(hint.ragione_sociale);
                        texForni.setText(hint.codice);
                    } else {
                        texFornitoreIntelli.setText("");
                        texForni.setText("");
                    }
                }
            }
        });

        //righe
        //apro la griglia
        this.griglia.dbNomeTabella = getNomeTabRighe();

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
        if (dbStato.equalsIgnoreCase(this.DB_INSERIMENTO)) {
            inserimento();
            texData.setEditable(true);
            texSconto.setText("0");
            texAcconto.setText("0");
            InvoicexUtil.fireEvent(this, InvoicexEvent.TYPE_FRMTESTORDI_INIT_INSERIMENTO);
        } else {
            //disabilito la data perch??? non tornerebbe pi??? la chiave per numero e anno
            this.texData.setEditable(false);
            this.dbdoc.sconto1 = Db.getDouble(this.texScon1.getText());
            this.dbdoc.sconto2 = Db.getDouble(this.texScon2.getText());
            this.dbdoc.sconto3 = Db.getDouble(this.texScon3.getText());

            //this.ordine.speseVarie = Db.getDouble(this.texSpesVari.getText());
            this.dbdoc.speseTrasportoIva = Db.getDouble(this.texSpeseTrasporto.getText());
            this.dbdoc.speseIncassoIva = Db.getDouble(this.texSpeseIncasso.getText());

            texSconto.setText(FormatUtils.formatEuroIta(CastUtils.toDouble0(dati.dbGetField("sconto"))));
            texAcconto.setText(FormatUtils.formatEuroIta(CastUtils.toDouble0(dati.dbGetField("acconto"))));

            dopoInserimento();

            try {
                String fornitore = (String) DbUtils.getObject(Db.getConn(), "select ragione_sociale from clie_forn where codice = " + Db.pc(texForni.getText(), Types.INTEGER));
                texFornitoreIntelli.setText(fornitore);
            } catch (Exception e) {
                if (!e.getMessage().startsWith("record non trovato")) {
                    e.printStackTrace();
                }
            }

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    comClie.setLocked(false);
                    comClie.setEditable(true);
                    comClie.setEnabled(true);
                    comClie.grabFocus();
                }
            });
        }

        this.textTipoSnj.setText(tipoSNJ);
        this.textTipoSnj.setVisible(false);
//        java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);
//        boolean azioniPericolose = preferences.getBoolean("azioniPericolose", false);
        boolean azioniPericolose = main.fileIni.getValueBoolean("pref", "azioniPericolose", true);

        if (azioniPericolose) {
            texNumeOrdine.setEditable(true);
            texData.setEditable(true);
        }

        data_originale = texData.getText();

        zoomA = new frmZoomDesc();
        frmZoomDesc frmZoomA = (frmZoomDesc) zoomA;
        frmZoomA.selectList = frmTestOrdine.this.foglioSelList;
        frmZoomA.setGriglia(foglioTipoA);
        zoomA.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        zoomA.setResizable(true);
        zoomA.setIconifiable(true);
        zoomA.setClosable(true);
        zoomA.setBounds((int) frmTestOrdine.this.getLocation().getX() + 430, (int) frmTestOrdine.this.getLocation().getY() + 350, 300, 150);

        zoomB = new frmZoomDesc();
        frmZoomDesc frmZoomB = (frmZoomDesc) zoomB;
        frmZoomB.selectList = frmTestOrdine.this.foglioSelList;
        frmZoomB.setGriglia(foglioTipoB);
        zoomB.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        zoomB.setResizable(true);
        zoomB.setIconifiable(true);
        zoomB.setClosable(true);
        zoomB.setBounds((int) frmTestOrdine.this.getLocation().getX() + 430, (int) frmTestOrdine.this.getLocation().getY() + 350, 300, 150);

        comStatoOrdine.dbOpenList(Db.getConn(), "SELECT descrizione, id FROM stati_preventivo_ordine");

        if (!dbStato.equalsIgnoreCase(DB_INSERIMENTO)) {
            comStatoOrdine.setSelectedItem(dati.dbGetField("stato_ordine"));

            System.out.println("!!! evasione: " + dati.dbGetField("evaso"));
            if ("S".equalsIgnoreCase(String.valueOf(dati.dbGetField("evaso")))) {
                stato_evasione.setSelectedIndex(0);
            } else if ("P".equalsIgnoreCase(String.valueOf(dati.dbGetField("evaso")))) {
                stato_evasione.setSelectedIndex(1);
            } else {
                stato_evasione.setSelectedIndex(2);
            }
        } else {
            if (main.getPersonalContain("canicom")) {
                comStatoOrdine.setSelectedItem("Ordine");
            } else {
                try {
                    comStatoOrdine.setSelectedItem(dbu.getObject(Db.getConn(), "select " + getNomeCampoStatoDef() + " from dati_azienda"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            stato_evasione.setSelectedIndex(2);
        }

        prezzi_ivati_virtual.setSelected(prezzi_ivati.isSelected());

        //allegati
        InvoicexEvent evt = new InvoicexEvent(frmTestOrdine.this, InvoicexEvent.TYPE_AllegatiInit);
        evt.args = new Object[]{panTab};
        main.events.fireInvoicexEvent(evt);
        InvoicexUtil.fireEvent(frmTestOrdine.this, InvoicexEvent.TYPE_AllegatiCarica, dati.dbNomeTabella, id);

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        loading = false;
    }

    private void inserimento() {
        this.dati.dbNew();

        //prendo base da impostazioni
        boolean prezzi_ivati_b = false;
        try {
            String prezzi_ivati_s = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select l.prezzi_ivati from dati_azienda a join tipi_listino l on a.listino_base = l.codice", false));
            if (prezzi_ivati_s.equalsIgnoreCase("S")) {
                prezzi_ivati_b = true;
            }
            prezzi_ivati.setSelected(prezzi_ivati_b);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (main.iniSerie == false) {
            assegnaNumero();
            dopoInserimento();
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    texCliente.grabFocus();
                }
            });
        } else {

            stato_evasione.setSelectedIndex(2);

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
            cs = dati_altri1.getComponents();
            for (int i = 0; i < cs.length; i++) {
                cs[i].setEnabled(false);
                if (cs[i] instanceof tnxbeans.tnxComboField) {
                    tnxbeans.tnxComboField combo = (tnxbeans.tnxComboField) cs[i];
                    combo.setEditable(false);
                    combo.setLocked(true);
                }
            }
            cs = dati_altri2.getComponents();
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
        cs = this.dati_altri1.getComponents();
        for (int i = 0; i < cs.length; i++) {
            cs[i].setEnabled(true);
            if (cs[i] instanceof tnxbeans.tnxComboField) {
                tnxbeans.tnxComboField combo = (tnxbeans.tnxComboField) cs[i];
                combo.setEditable(true);
                combo.setLocked(false);
            }
        }
        cs = this.dati_altri2.getComponents();
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

            String sql = "select numero from test_ordi" + suff;
            sql += " where anno = " + java.util.Calendar.getInstance().get(Calendar.YEAR);
            sql += " and serie = " + Db.pc(texSeri.getText(), Types.VARCHAR);
            sql += " order by numero desc limit 1";
            resu = stat.executeQuery(sql);

            if (resu.next() == true) {
                this.texNumeOrdine.setText(String.valueOf(resu.getInt(1) + 1));
            } else {
                this.texNumeOrdine.setText("1");
            }

            dati.setCampiAggiuntivi(new Hashtable());
            dati.getCampiAggiuntivi().put("evaso", Db.pc(getStatoEvaso(), Types.VARCHAR));
            dati.getCampiAggiuntivi().put("sconto", Db.pc(doc.getSconto(), Types.DOUBLE));
            dati.getCampiAggiuntivi().put("totale_imponibile_pre_sconto", Db.pc(doc.totaleImponibilePreSconto, Types.DOUBLE));
            dati.getCampiAggiuntivi().put("totale_ivato_pre_sconto", Db.pc(doc.totaleIvatoPreSconto, Types.DOUBLE));
            dati.getCampiAggiuntivi().put("acconto", Db.pc(Db.getDouble(texAcconto.getText()), Types.DOUBLE));

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
                    Integer numero = Integer.parseInt(texNumeOrdine.getText());
                    Integer anno = Integer.parseInt(texAnno.getText());

                    String tmpSql = "select * from test_ordi" + suff + " where serie = '" + tmpSerie + "' and anno = " + anno + " and numero = " + numero;
                    ResultSet tmpControl = Db.openResultSet(tmpSql);

                    if (tmpControl.next()) {
                        JOptionPane.showMessageDialog(this, "Un' altro documento con lo stesso gruppo numero - serie - anno è già stato inserito!", "Impossibile inserire dati", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!main.edit_doc_in_temp) {
                dati.dbSave();
                //richiamo il refresh della maschera che lo ha lanciato
                if (from != null) {
                    frmElenOrdini temp = (frmElenOrdini) from;
                    temp.dbRefresh();
                }

                this.id = (Integer) dati.last_inserted_id;
                System.out.println("*** id new : " + this.id);
                this.dbdoc.id = id;

                if (dbStato.equalsIgnoreCase(tnxDbPanel.DB_INSERIMENTO)) {
                    InvoicexUtil.checkLock(dati.dbNomeTabella, id, false, null);
                }
                InvoicexUtil.checkLockAddFrame(this, dati.dbNomeTabella, id);
            }

            this.dbdoc.serie = this.texSeri.getText();
            this.dbdoc.stato = "P";
            this.dbdoc.numero = new Integer(this.texNumeOrdine.getText()).intValue();
            this.dbdoc.anno = java.util.Calendar.getInstance().get(Calendar.YEAR);

            if (!main.edit_doc_in_temp) {
                this.dati.dbCambiaStato(this.dati.DB_LETTURA);
            }

            this.texSeri.setEditable(false);
            this.texSeri.setBackground(this.texNumeOrdine.getBackground());

            //Fine
        } catch (Exception err) {
            err.printStackTrace();
            SwingUtils.showExceptionMessage(main.getPadreFrame(), err);
        }
        dati.dbCheckModificatiReset();
    }

    private void dopoInserimento() {

        controllaPermessiAnagCliFor();

        dbAssociaGrigliaRighe();
        doc.table_righe_temp = table_righe_temp;
        doc.load(Db.INSTANCE, this.dbdoc.numero, this.dbdoc.serie, this.dbdoc.anno, acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE, id);
        dbdoc.id = id;

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

        //provo a fare timer per aggiornare prezzo totale
        tim = new java.util.Timer();

        ricalcolaTotali();

        //rinfresco il discorso extra cee
        try {
            if (this.texClie.getText().length() > 0) {
                this.dbdoc.forceCliente(Long.parseLong(this.texClie.getText()));
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        comPagaItemStateChanged(null);

        //memorizzo totale iniziale, se cambia rigenreo le scadenze
        dbdoc.dbRefresh();
        totaleDaPagareFinaleIniziale = doc.getTotale_da_pagare_finale();
        pagamentoIniziale = this.comPaga.getText();
        pagamentoInizialeGiorno = this.texGiornoPagamento.getText();
        provvigioniIniziale = Db.getDouble(this.texProvvigione.getText());
        codiceAgenteIniziale = it.tnx.Util.getInt(this.comAgente.getSelectedKey().toString());

        //debug
        System.out.println("provvigioni iniziale = " + provvigioniIniziale);
        System.out.println("codice agente iniziale = " + codiceAgenteIniziale);

    }

    private boolean saveDocumento() {

        //sposto i totali di modo che li salvi
        this.texTota1.setText(this.texTota.getText());
        this.texTotaImpo1.setText(this.texTotaImpo.getText());
        this.texTotaIva1.setText(this.texTotaIva.getText());

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
        } catch (Exception err) {
            err.printStackTrace();
        }

        //storico
        Storico.scrivi("Salva Documento", "Documento = " + (acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE) + "/" + this.texSeri.getText() + "/" + this.dbdoc.numero + "/" + this.dbdoc.anno + ", Pagamento = " + this.comPaga.getText() + ", Importo documento = " + this.texTota1.getText());

        dati.setCampiAggiuntivi(new Hashtable());
        dati.getCampiAggiuntivi().put("evaso", Db.pc(getStatoEvaso(), Types.VARCHAR));
        dati.getCampiAggiuntivi().put("sconto", Db.pc(doc.getSconto(), Types.DOUBLE));
        dati.getCampiAggiuntivi().put("totale_imponibile_pre_sconto", Db.pc(doc.totaleImponibilePreSconto, Types.DOUBLE));
        dati.getCampiAggiuntivi().put("totale_ivato_pre_sconto", Db.pc(doc.totaleIvatoPreSconto, Types.DOUBLE));
        dati.getCampiAggiuntivi().put("data_consegna_prevista", Db.pc(consegna_prevista.getDate(), Types.DATE));
        dati.getCampiAggiuntivi().put("acconto", Db.pc(Db.getDouble(texAcconto.getText()), Types.DOUBLE));
        dati.getCampiAggiuntivi().put("stato_ordine", dbu.sql(cu.s(comStatoOrdine.getSelectedItem())));

        //salvo altrimenti genera le scadenze sull'importo vuoto
        if (!main.edit_doc_in_temp) {
            this.dati.dbSave();
        } else {
            Sync.saveDoc(suff, texSeri.getText(), texNumeOrdine.getText(), texAnno.getText(), id, this, dati, table_righe_temp, table_righe_lotti_temp, null);
            if (dati.id != null) {  //in inserimento viene avvalorato con il nuovo id di testata
                id = dati.id;
            }
        }

        //forzo anno in base alla data
        if (!main.edit_doc_in_temp) {
            InvoicexUtil.aggiornaAnnoDaData(acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE, id);
        } else {
            System.err.println("!!! TODO fare test per anno su testata e righe documento");
        }

        //genero le scadenze
        if (!acquisto) {
            if (main.fileIni.getValueBoolean("pref", "scadenzeOrdini", false)) {
                Scadenze tempScad = new Scadenze(acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE, id, this.comPaga.getText());
                boolean scadenzeRigenerate = false;
//                if (doc.getTotale() != this.totaleIniziale || !this.pagamentoInizialeGiorno.equals(this.texGiornoPagamento.getText()) || !this.pagamentoIniziale.equals(this.comPaga.getText()) || doc.getTotale_da_pagare() != this.totaleDaPagareIniziale) {
                if (tempScad.totaliDiversi()
                        || !this.pagamentoInizialeGiorno.equals(this.texGiornoPagamento.getText())
                        || !this.pagamentoIniziale.equals(this.comPaga.getText())
                        || doc.getTotale_da_pagare_finale() != this.totaleDaPagareFinaleIniziale
                        || !data_originale.equalsIgnoreCase(texData.getText())) {
                    tempScad.generaScadenze();
                    scadenzeRigenerate = true;
                    if (!dbStato.equals(this.DB_INSERIMENTO)) {
                        javax.swing.JOptionPane.showMessageDialog(this, "Sono state rigenerate le scadenze perche' il totale od il pagamento e' stato variato", "Attenzione", javax.swing.JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }

        InvoicexEvent event = new InvoicexEvent(this);
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

        String sql = "select * from clie_forn_dest";
        sql += " where codice_cliente = " + Db.pc(this.texClie.getText(), Types.INTEGER);
        sql += " and id = " + Db.pc(this.texClieDest.getText(), Types.INTEGER);

        ResultSet dest = Db.openResultSet(sql);

        try {

            if (dest.next()) {
                texDestRagioneSociale.setText(dest.getString("ragione_sociale"));
                texDestIndirizzo.setText(dest.getString("indirizzo"));
                texDestCap.setText(dest.getString("cap"));
                texDestLocalita.setText(dest.getString("localita"));
                texDestProvincia.setText(dest.getString("provincia"));
                texDestTelefono.setText(dest.getString("telefono"));
                texDestCellulare.setText(dest.getString("cellulare"));
                comPaese.dbTrovaKey(dest.getString("paese"));
            } else {
                texDestRagioneSociale.setText("");
                texDestIndirizzo.setText("");
                texDestCap.setText("");
                texDestLocalita.setText("");
                texDestProvincia.setText("");
                texDestTelefono.setText("");
                texDestCellulare.setText("");
                comPaese.setSelectedIndex(-1);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to
     *
     *
     * initialize the form.
     *
     *
     * WARNING: Do NOT modify this code. The content of this method is
     *
     *
     * always regenerated by the Form Editor.
     *
     *
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popGrig = new javax.swing.JPopupMenu();
        popGrigModi = new javax.swing.JMenuItem();
        popGrigElim = new javax.swing.JMenuItem();
        popGrigAdd = new javax.swing.JMenuItem();
        popGrigAddSub = new javax.swing.JMenuItem();
        popDuplicaRighe = new javax.swing.JMenuItem();
        menColAgg = new javax.swing.JMenu();
        menColAggNote = new javax.swing.JCheckBoxMenuItem();
        popFoglio = new javax.swing.JPopupMenu();
        popFoglioElimina = new javax.swing.JMenuItem();
        menClientePopup = new javax.swing.JPopupMenu();
        menClienteNuovo = new javax.swing.JMenuItem();
        menClienteModifica = new javax.swing.JMenuItem();
        menClienteControllaFido2 = new javax.swing.JCheckBoxMenuItem();
        popAltriDati = new javax.swing.JPopupMenu();
        popAltriDatiVisualizza = new javax.swing.JCheckBoxMenuItem();
        popStatoOrdineDef = new javax.swing.JPopupMenu();
        popStatoOrdineDefRendiDef = new javax.swing.JMenuItem();
        panTab = new javax.swing.JTabbedPane();
        panDati = new javax.swing.JPanel();
        split = new javax.swing.JSplitPane();
        dati = new tnxbeans.tnxDbPanel();
        texNumeOrdine = new tnxbeans.tnxTextField();
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
        texStat = new tnxbeans.tnxTextField();
        texStat.setVisible(false);
        texScon3 = new tnxbeans.tnxTextField();
        labScon21 = new javax.swing.JLabel();
        jLabel151 = new javax.swing.JLabel();
        texSeri = new tnxbeans.tnxTextField();
        texAnno = new tnxbeans.tnxTextField();
        texAnno.setVisible(false);
        texClieDest = new tnxbeans.tnxTextField();
        texClieDest.setVisible(false);
        jLabel17 = new javax.swing.JLabel();
        texPaga2 = new tnxbeans.tnxTextField();
        texSpeseTrasporto = new tnxbeans.tnxTextField();
        jLabel114 = new javax.swing.JLabel();
        butPrezziPrec = new javax.swing.JButton();
        comPaga = new tnxbeans.tnxComboField();
        jLabel19 = new javax.swing.JLabel();
        butAddClie = new javax.swing.JButton();
        labGiornoPagamento = new javax.swing.JLabel();
        texGiornoPagamento = new tnxbeans.tnxTextField();
        texTipoOrdine = new tnxbeans.tnxTextField();
        texTipoOrdine.setVisible(false);
        dati_altri2 = new tnxbeans.tnxDbPanel();
        labDestDiversa = new javax.swing.JLabel();
        comClieDest = new tnxbeans.tnxComboField();
        labDDRagSoc = new javax.swing.JLabel();
        labDDIndirizzo = new javax.swing.JLabel();
        texDestIndirizzo = new tnxbeans.tnxTextField();
        labDDCap = new javax.swing.JLabel();
        texDestCap = new tnxbeans.tnxTextField();
        labDDLoc = new javax.swing.JLabel();
        texDestLocalita = new tnxbeans.tnxTextField();
        labDDProv = new javax.swing.JLabel();
        texDestProvincia = new tnxbeans.tnxTextField();
        labDDTel = new javax.swing.JLabel();
        texDestTelefono = new tnxbeans.tnxTextField();
        labDDCel = new javax.swing.JLabel();
        texDestCellulare = new tnxbeans.tnxTextField();
        labDDPaese = new javax.swing.JLabel();
        comPaese = new tnxbeans.tnxComboField();
        labFaTitolo = new javax.swing.JLabel();
        labFa1 = new javax.swing.JLabel();
        comCausaleTrasporto = new tnxbeans.tnxComboField();
        labFa2 = new javax.swing.JLabel();
        comAspettoEsterioreBeni = new tnxbeans.tnxComboField();
        labFa3 = new javax.swing.JLabel();
        texNumeroColli = new tnxbeans.tnxTextField();
        labFa4 = new javax.swing.JLabel();
        comVettori = new tnxbeans.tnxComboField();
        labFa5 = new javax.swing.JLabel();
        comMezzoTrasporto = new tnxbeans.tnxComboField();
        labFa6 = new javax.swing.JLabel();
        comPorto = new tnxbeans.tnxComboField();
        texDestRagioneSociale = new tnxbeans.tnxTextField();
        sepFattAcc = new javax.swing.JSeparator();
        sepDestMerce = new javax.swing.JSeparator();
        texFornitoreIntelli = new javax.swing.JTextField();
        apriFornitori = new MyBasicArrowButton(BasicArrowButton.SOUTH, UIManager.getColor("ComboBox.buttonBackground"), UIManager.getColor("ComboBox.buttonShadow"), UIManager.getColor("ComboBox.buttonDarkShadow"), UIManager.getColor("ComboBox.buttonHighlight"));
        jLabel6 = new javax.swing.JLabel();
        dati_altri1 = new tnxbeans.tnxDbPanel();
        jLabel24 = new javax.swing.JLabel();
        texNotePagamento = new tnxbeans.tnxTextField();
        cheOpzioneRibaDestDiversa = new tnxbeans.tnxCheckBox();
        jLabel18 = new javax.swing.JLabel();
        texBancAbi = new tnxbeans.tnxTextField();
        butCoor = new javax.swing.JButton();
        labBancAbi = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        texBancCab = new tnxbeans.tnxTextField();
        labBancCab = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        texBancIban = new tnxbeans.tnxTextField();
        labAgente = new javax.swing.JLabel();
        comAgente = new tnxbeans.tnxComboField();
        labProvvigione = new javax.swing.JLabel();
        texProvvigione = new tnxbeans.tnxTextField();
        labPercentoProvvigione = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        labRiferimento = new javax.swing.JLabel();
        texConsegna = new tnxbeans.tnxTextField();
        textTipoSnj = new tnxbeans.tnxTextField();
        prezzi_ivati = new tnxbeans.tnxCheckBox();
        consegna_prevista = new org.jdesktop.swingx.JXDatePicker();
        jLabel115 = new javax.swing.JLabel();
        labModConsegna = new javax.swing.JLabel();
        comConsegna = new tnxbeans.tnxComboField();
        comScarico = new tnxbeans.tnxComboField();
        labModScarico = new javax.swing.JLabel();
        texCliente = new javax.swing.JTextField();
        apriclienti = new MyBasicArrowButton(BasicArrowButton.SOUTH, UIManager.getColor("ComboBox.buttonBackground"), UIManager.getColor("ComboBox.buttonShadow"), UIManager.getColor("ComboBox.buttonDarkShadow"), UIManager.getColor("ComboBox.buttonHighlight"));
        jSeparator3 = new javax.swing.JSeparator();
        texForni = new tnxbeans.tnxTextField();
        texForni.setVisible(false);
        jLabel4 = new javax.swing.JLabel();
        datiRighe = new tnxbeans.tnxDbPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        griglia = getGrigliaInitComp();
        jPanel1 = new javax.swing.JPanel();
        prezzi_ivati_virtual = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        butNuovArti = new javax.swing.JButton();
        butInserisciSubTotale = new javax.swing.JButton();
        butInserisciPeso = new javax.swing.JButton();
        butImportRighe = new javax.swing.JButton();
        butImportRigheProskin = new javax.swing.JButton();
        butImportXlsCirri = new javax.swing.JButton();
        panFoglioRigheSNJA = new javax.swing.JPanel();
        panGriglia = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        foglioTipoA = new JTableSs(){
            @Override
            public void setValueAt(Object value,int row,int column){
                super.setValueAt(value, row, column);
                ricalcolaTotali();
            }
        };
        panTotale = new javax.swing.JPanel();
        labStatus = new javax.swing.JLabel();
        panFoglioRigheSNJB = new javax.swing.JPanel();
        panGriglia1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        foglioTipoB = new JTableSs(){
            @Override
            public void setValueAt(Object value,int row,int column){
                super.setValueAt(value, row, column);
                ricalcolaTotali();
            }
        };
        panTotale1 = new javax.swing.JPanel();
        labStatus1 = new javax.swing.JLabel();
        datiAltro = new tnxbeans.tnxDbPanel();
        pan_segnaposto_deposito = new javax.swing.JPanel();
        labNoteConsegna = new javax.swing.JLabel();
        texNoteConsegna = new tnxbeans.tnxMemoField();
        jLabel5 = new javax.swing.JLabel();
        comContattoRiferimento = new tnxbeans.tnxComboField();
        labCampoLibero1 = new javax.swing.JLabel();
        comCampoLibero1 = new tnxbeans.tnxComboField();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        butPdf = new javax.swing.JButton();
        butStampa = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        butUndo = new javax.swing.JButton();
        butSave = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        comStatoOrdine = new tnxbeans.tnxComboField();
        stato_evasione = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        texTotaImpo = new tnxbeans.tnxTextField();
        texTotaIva = new tnxbeans.tnxTextField();
        texTota = new tnxbeans.tnxTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        texSconto = new tnxbeans.tnxTextField();
        jLabel27 = new javax.swing.JLabel();
        texAcconto = new tnxbeans.tnxTextField();
        jLabel3 = new javax.swing.JLabel();
        texTotaDaPagareFinale = new tnxbeans.tnxTextField();

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

        popGrigAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/list-add.png"))); // NOI18N
        popGrigAdd.setLabel("Aggiungi Riga");
        popGrigAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popGrigAddActionPerformed(evt);
            }
        });
        popGrig.add(popGrigAdd);

        popGrigAddSub.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/list-add.png"))); // NOI18N
        popGrigAddSub.setLabel("Aggiungi Riga");
        popGrigAddSub.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popGrigAddSubActionPerformed(evt);
            }
        });
        popGrig.add(popGrigAddSub);

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

        menClienteControllaFido2.setText("Visualizza controllo Fido");
        menClienteControllaFido2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Coins-16.png"))); // NOI18N
        menClienteControllaFido2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menClienteControllaFido2ActionPerformed(evt);
            }
        });
        menClientePopup.add(menClienteControllaFido2);

        popAltriDatiVisualizza.setText("Visualizza dati trasporto");
        popAltriDatiVisualizza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popAltriDatiVisualizzaActionPerformed(evt);
            }
        });
        popAltriDati.add(popAltriDatiVisualizza);

        popStatoOrdineDefRendiDef.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Star-16.png"))); // NOI18N
        popStatoOrdineDefRendiDef.setText("Imposta come standard per ...");
        popStatoOrdineDefRendiDef.setToolTipText("");
        popStatoOrdineDefRendiDef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popStatoOrdineDefRendiDefActionPerformed(evt);
            }
        });
        popStatoOrdineDef.add(popStatoOrdineDefRendiDef);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Preventivo/Ordine");
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
        addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                formVetoableChange(evt);
            }
        });

        panTab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                panTabStateChanged(evt);
            }
        });

        panDati.setLayout(new java.awt.BorderLayout());

        split.setBorder(null);
        split.setDividerLocation(330);
        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        split.setMinimumSize(new java.awt.Dimension(508, 200));

        dati.setMinimumSize(new java.awt.Dimension(0, 50));
        dati.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                datiMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                datiMouseReleased(evt);
            }
        });

        texNumeOrdine.setEditable(false);
        texNumeOrdine.setColumns(4);
        texNumeOrdine.setText("numero");
        texNumeOrdine.setDbDescCampo("");
        texNumeOrdine.setDbNomeCampo("numero");
        texNumeOrdine.setDbTipoCampo("testo");
        texNumeOrdine.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                texNumeOrdineFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                texNumeOrdineFocusLost(evt);
            }
        });

        texClie.setText("cliente");
        texClie.setToolTipText("");
        texClie.setDbComboAbbinata(comClie);
        texClie.setDbDefault("vuoto");
        texClie.setDbDescCampo("");
        texClie.setDbNomeCampo("cliente");
        texClie.setDbNullSeVuoto(true);
        texClie.setDbTipoCampo("");
        texClie.setName("textfield clifor nascosta"); // NOI18N
        texClie.addFocusListener(new java.awt.event.FocusAdapter() {
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
        comClie.setName("combo clifor nascosta"); // NOI18N
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
        texTotaImpo1.setText("0");
        texTotaImpo1.setDbDescCampo("");
        texTotaImpo1.setDbNomeCampo("totale_imponibile");
        texTotaImpo1.setDbTipoCampo("valuta");

        texTotaIva1.setBackground(new java.awt.Color(255, 200, 200));
        texTotaIva1.setText("0");
        texTotaIva1.setDbDescCampo("");
        texTotaIva1.setDbNomeCampo("totale_iva");
        texTotaIva1.setDbTipoCampo("valuta");

        texTota1.setBackground(new java.awt.Color(255, 200, 200));
        texTota1.setText("0");
        texTota1.setDbDescCampo("");
        texTota1.setDbNomeCampo("totale");
        texTota1.setDbTipoCampo("valuta");

        texNote.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        texNote.setDbNomeCampo("note");
        texNote.setFont(texNote.getFont());
        texNote.setText("note");

        jLabel13.setText("Numero");

        jLabel14.setText("Serie");

        jLabel16.setText("Data");

        labScon1.setText("Sc. 1");
        labScon1.setToolTipText("primo sconto");

        labScon2.setText("Sc. 3");
        labScon2.setToolTipText("sconto3");

        jLabel113.setText("Consegna prevista");

        texData.setColumns(9);
        texData.setText("data");
        texData.setDbDescCampo("");
        texData.setDbNomeCampo("data");
        texData.setDbTipoCampo("data");
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

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Note");

        texStat.setBackground(new java.awt.Color(255, 200, 200));
        texStat.setText("P");
        texStat.setDbDescCampo("");
        texStat.setDbNomeCampo("stato");
        texStat.setDbRiempire(false);
        texStat.setDbTipoCampo("");

        texScon3.setColumns(4);
        texScon3.setText("sconto3");
        texScon3.setToolTipText("terzo sconto");
        texScon3.setDbDescCampo("");
        texScon3.setDbNomeCampo("sconto3");
        texScon3.setDbTipoCampo("numerico");
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

        labScon21.setText("Sc. 2");
        labScon21.setToolTipText("secondo sconto");

        jLabel151.setText("Cliente");

        texSeri.setEditable(false);
        texSeri.setBackground(new java.awt.Color(204, 204, 204));
        texSeri.setColumns(2);
        texSeri.setText("serie");
        texSeri.setDbDescCampo("");
        texSeri.setDbNomeCampo("serie");
        texSeri.setDbTipoCampo("");
        texSeri.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texSeriFocusLost(evt);
            }
        });
        texSeri.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texSeriKeyPressed(evt);
            }
        });

        texAnno.setBackground(new java.awt.Color(255, 200, 200));
        texAnno.setText("anno");
        texAnno.setDbDescCampo("");
        texAnno.setDbNomeCampo("anno");
        texAnno.setDbTipoCampo("");

        texClieDest.setBackground(new java.awt.Color(255, 200, 200));
        texClieDest.setText("id_cliente_destinazione");
        texClieDest.setDbComboAbbinata(comClieDest);
        texClieDest.setDbDescCampo("");
        texClieDest.setDbNomeCampo("id_cliente_destinazione");
        texClieDest.setDbTipoCampo("numerico");

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("Riferimento");
        jLabel17.setToolTipText("Riferimento vostro ordine o altro documento");

        texPaga2.setColumns(15);
        texPaga2.setText("riferimento");
        texPaga2.setDbDescCampo("");
        texPaga2.setDbNomeCampo("riferimento");
        texPaga2.setDbTipoCampo("");
        texPaga2.setmaxChars(255);

        texSpeseTrasporto.setColumns(6);
        texSpeseTrasporto.setText("spese_trasporto");
        texSpeseTrasporto.setDbDescCampo("");
        texSpeseTrasporto.setDbNomeCampo("spese_trasporto");
        texSpeseTrasporto.setDbTipoCampo("valuta");
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

        jLabel114.setText("Sp. trasporto");

        butPrezziPrec.setFont(butPrezziPrec.getFont().deriveFont(butPrezziPrec.getFont().getSize()-1f));
        butPrezziPrec.setText("Prezzi precedenti");
        butPrezziPrec.setMargin(new java.awt.Insets(1, 4, 1, 4));
        butPrezziPrec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPrezziPrecActionPerformed(evt);
            }
        });

        comPaga.setDbDescCampo("");
        comPaga.setDbNomeCampo("pagamento");
        comPaga.setDbTextAbbinato(null);
        comPaga.setDbTipoCampo("VARCHAR");
        comPaga.setDbTrovaMentreScrive(true);
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

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText("Pagamento");

        butAddClie.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/Actions-contact-new-icon-16.png"))); // NOI18N
        butAddClie.setToolTipText("Crea una nuova anagrafica");
        butAddClie.setMargin(new java.awt.Insets(1, 1, 1, 1));
        butAddClie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAddClieActionPerformed(evt);
            }
        });

        labGiornoPagamento.setText("   giorno");

        texGiornoPagamento.setColumns(2);
        texGiornoPagamento.setToolTipText("Giorno del mese per le scadenze");
        texGiornoPagamento.setDbDescCampo("");
        texGiornoPagamento.setDbNomeCampo("giorno_pagamento");
        texGiornoPagamento.setDbTipoCampo("numerico");
        texGiornoPagamento.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texGiornoPagamentoFocusLost(evt);
            }
        });

        texTipoOrdine.setBackground(new java.awt.Color(255, 200, 200));
        texTipoOrdine.setText("tipoOrdine");
        texTipoOrdine.setDbDescCampo("");
        texTipoOrdine.setDbNomeCampo("tipo_fattura");
        texTipoOrdine.setDbTipoCampo("numerico");
        texTipoOrdine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texTipoOrdineActionPerformed(evt);
            }
        });

        dati_altri2.setName("dati_altri2"); // NOI18N

        labDestDiversa.setText("Destinazione merce");

        comClieDest.setDbNomeCampo("");
        comClieDest.setDbRiempire(false);
        comClieDest.setDbSalvare(false);
        comClieDest.setDbTextAbbinato(texClieDest);
        comClieDest.setDbTipoCampo("numerico");
        comClieDest.setDbTrovaMentreScrive(true);
        comClieDest.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comClieDestFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                comClieDestFocusLost(evt);
            }
        });
        comClieDest.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                comClieDestKeyPressed(evt);
            }
        });

        labDDRagSoc.setFont(labDDRagSoc.getFont().deriveFont(labDDRagSoc.getFont().getSize()-1f));
        labDDRagSoc.setText("ragione sociale");
        labDDRagSoc.setToolTipText("");

        labDDIndirizzo.setFont(labDDIndirizzo.getFont().deriveFont(labDDIndirizzo.getFont().getSize()-1f));
        labDDIndirizzo.setText("indirizzo");
        labDDIndirizzo.setToolTipText("");

        texDestIndirizzo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        texDestIndirizzo.setColumns(20);
        texDestIndirizzo.setToolTipText("");
        texDestIndirizzo.setDbDescCampo("");
        texDestIndirizzo.setDbNomeCampo("dest_indirizzo");
        texDestIndirizzo.setDbTipoCampo("");
        texDestIndirizzo.setFont(texDestIndirizzo.getFont().deriveFont(texDestIndirizzo.getFont().getSize()-1f));

        labDDCap.setFont(labDDCap.getFont().deriveFont(labDDCap.getFont().getSize()-1f));
        labDDCap.setText("cap");
        labDDCap.setToolTipText("");

        texDestCap.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        texDestCap.setColumns(5);
        texDestCap.setToolTipText("");
        texDestCap.setDbDescCampo("");
        texDestCap.setDbNomeCampo("dest_cap");
        texDestCap.setDbTipoCampo("");
        texDestCap.setFont(texDestCap.getFont().deriveFont(texDestCap.getFont().getSize()-1f));

        labDDLoc.setFont(labDDLoc.getFont().deriveFont(labDDLoc.getFont().getSize()-1f));
        labDDLoc.setText("loc.");
        labDDLoc.setToolTipText("");

        texDestLocalita.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        texDestLocalita.setColumns(10);
        texDestLocalita.setToolTipText("");
        texDestLocalita.setDbDescCampo("");
        texDestLocalita.setDbNomeCampo("dest_localita");
        texDestLocalita.setDbTipoCampo("");
        texDestLocalita.setFont(texDestLocalita.getFont().deriveFont(texDestLocalita.getFont().getSize()-1f));

        labDDProv.setFont(labDDProv.getFont().deriveFont(labDDProv.getFont().getSize()-1f));
        labDDProv.setText("prov.");
        labDDProv.setToolTipText("");

        texDestProvincia.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        texDestProvincia.setColumns(3);
        texDestProvincia.setToolTipText("");
        texDestProvincia.setDbDescCampo("");
        texDestProvincia.setDbNomeCampo("dest_provincia");
        texDestProvincia.setDbTipoCampo("");
        texDestProvincia.setFont(texDestProvincia.getFont().deriveFont(texDestProvincia.getFont().getSize()-1f));

        labDDTel.setFont(labDDTel.getFont().deriveFont(labDDTel.getFont().getSize()-1f));
        labDDTel.setText("telefono");
        labDDTel.setToolTipText("");

        texDestTelefono.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        texDestTelefono.setColumns(10);
        texDestTelefono.setToolTipText("");
        texDestTelefono.setDbDescCampo("");
        texDestTelefono.setDbNomeCampo("dest_telefono");
        texDestTelefono.setDbTipoCampo("");
        texDestTelefono.setFont(texDestTelefono.getFont().deriveFont(texDestTelefono.getFont().getSize()-1f));

        labDDCel.setFont(labDDCel.getFont().deriveFont(labDDCel.getFont().getSize()-1f));
        labDDCel.setText("cellulare");
        labDDCel.setToolTipText("");

        texDestCellulare.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        texDestCellulare.setColumns(10);
        texDestCellulare.setToolTipText("");
        texDestCellulare.setDbDescCampo("");
        texDestCellulare.setDbNomeCampo("dest_cellulare");
        texDestCellulare.setDbTipoCampo("");
        texDestCellulare.setFont(texDestCellulare.getFont().deriveFont(texDestCellulare.getFont().getSize()-1f));

        labDDPaese.setFont(labDDPaese.getFont().deriveFont(labDDPaese.getFont().getSize()-1f));
        labDDPaese.setText("paese");
        labDDPaese.setToolTipText("");

        comPaese.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        comPaese.setDbNomeCampo("dest_paese");
        comPaese.setDbTipoCampo("");
        comPaese.setDbTrovaMentreScrive(true);
        comPaese.setFont(comPaese.getFont().deriveFont(comPaese.getFont().getSize()-1f));

        labFaTitolo.setFont(labFaTitolo.getFont());
        labFaTitolo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labFaTitolo.setText("Dati fattura accompagnatoria");

        labFa1.setFont(labFa1.getFont().deriveFont(labFa1.getFont().getSize()-1f));
        labFa1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labFa1.setText("Causale del trasporto");

        comCausaleTrasporto.setDbNomeCampo("causale_trasporto");
        comCausaleTrasporto.setDbRiempireForceText(true);
        comCausaleTrasporto.setDbSalvaKey(false);
        comCausaleTrasporto.setFont(comCausaleTrasporto.getFont().deriveFont(comCausaleTrasporto.getFont().getSize()-1f));

        labFa2.setFont(labFa2.getFont().deriveFont(labFa2.getFont().getSize()-1f));
        labFa2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labFa2.setText("Aspetto esteriore beni");

        comAspettoEsterioreBeni.setDbNomeCampo("aspetto_esteriore_beni");
        comAspettoEsterioreBeni.setDbRiempireForceText(true);
        comAspettoEsterioreBeni.setDbSalvaKey(false);
        comAspettoEsterioreBeni.setFont(comAspettoEsterioreBeni.getFont().deriveFont(comAspettoEsterioreBeni.getFont().getSize()-1f));

        labFa3.setFont(labFa3.getFont().deriveFont(labFa3.getFont().getSize()-1f));
        labFa3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labFa3.setText("Num. colli");

        texNumeroColli.setColumns(5);
        texNumeroColli.setText("numero_colli");
        texNumeroColli.setDbDescCampo("");
        texNumeroColli.setDbNomeCampo("numero_colli");
        texNumeroColli.setDbTipoCampo("");
        texNumeroColli.setFont(texNumeroColli.getFont().deriveFont(texNumeroColli.getFont().getSize()-1f));
        texNumeroColli.setmaxChars(255);

        labFa4.setFont(labFa4.getFont().deriveFont(labFa4.getFont().getSize()-1f));
        labFa4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labFa4.setText("1° Vettore");

        comVettori.setDbNomeCampo("vettore1");
        comVettori.setDbRiempireForceText(true);
        comVettori.setDbSalvaKey(false);
        comVettori.setFont(comVettori.getFont().deriveFont(comVettori.getFont().getSize()-1f));

        labFa5.setFont(labFa5.getFont().deriveFont(labFa5.getFont().getSize()-1f));
        labFa5.setText("Cons. o inizio trasp. a mezzo");

        comMezzoTrasporto.setDbNomeCampo("mezzo_consegna");
        comMezzoTrasporto.setDbRiempireForceText(true);
        comMezzoTrasporto.setDbSalvaKey(false);
        comMezzoTrasporto.setFont(comMezzoTrasporto.getFont().deriveFont(comMezzoTrasporto.getFont().getSize()-1f));

        labFa6.setFont(labFa6.getFont().deriveFont(labFa6.getFont().getSize()-1f));
        labFa6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labFa6.setText("Porto");

        comPorto.setDbNomeCampo("porto");
        comPorto.setDbRiempireForceText(true);
        comPorto.setDbSalvaKey(false);
        comPorto.setFont(comPorto.getFont().deriveFont(comPorto.getFont().getSize()-1f));
        comPorto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comPortoActionPerformed(evt);
            }
        });

        texDestRagioneSociale.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        texDestRagioneSociale.setColumns(20);
        texDestRagioneSociale.setToolTipText("");
        texDestRagioneSociale.setDbDescCampo("");
        texDestRagioneSociale.setDbNomeCampo("dest_ragione_sociale");
        texDestRagioneSociale.setDbTipoCampo("");
        texDestRagioneSociale.setFont(texDestRagioneSociale.getFont().deriveFont(texDestRagioneSociale.getFont().getSize()-1f));

        texFornitoreIntelli.setColumns(25);
        texFornitoreIntelli.setFont(texFornitoreIntelli.getFont().deriveFont(texFornitoreIntelli.getFont().getSize()-1f));
        texFornitoreIntelli.setToolTipText("Da utilizzare per impostare come intestazione in stampa");
        texFornitoreIntelli.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                texFornitoreIntelliMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                texFornitoreIntelliMouseReleased(evt);
            }
        });

        apriFornitori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apriFornitoriActionPerformed(evt);
            }
        });

        jLabel6.setFont(jLabel6.getFont().deriveFont(jLabel6.getFont().getSize()-1f));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Fornitore");
        jLabel6.setToolTipText("Da utilizzare per impostare come intestazione in stampa");

        org.jdesktop.layout.GroupLayout dati_altri2Layout = new org.jdesktop.layout.GroupLayout(dati_altri2);
        dati_altri2.setLayout(dati_altri2Layout);
        dati_altri2Layout.setHorizontalGroup(
            dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dati_altri2Layout.createSequentialGroup()
                .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, comClieDest, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labFaTitolo)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sepFattAcc))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labFa1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comCausaleTrasporto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labFa2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comAspettoEsterioreBeni, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labFa4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comVettori, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labFa5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comMezzoTrasporto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labFa6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comPorto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labDDPaese)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comPaese, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labDestDiversa)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sepDestMerce))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labDDRagSoc)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texDestRagioneSociale, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labDDIndirizzo)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texDestIndirizzo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labDDCap)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texDestCap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(labDDLoc)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texDestLocalita, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(labDDProv)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texDestProvincia, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labDDTel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texDestTelefono, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(labDDCel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(texDestCellulare, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(labFa3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texNumeroColli, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texFornitoreIntelli)
                        .add(0, 0, 0)
                        .add(apriFornitori, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(15, 15, 15))
        );

        dati_altri2Layout.linkSize(new java.awt.Component[] {labFa1, labFa2}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        dati_altri2Layout.linkSize(new java.awt.Component[] {jLabel6, labFa3, labFa4, labFa6}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        dati_altri2Layout.setVerticalGroup(
            dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dati_altri2Layout.createSequentialGroup()
                .addContainerGap()
                .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(labDestDiversa)
                    .add(sepDestMerce, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(1, 1, 1)
                .add(comClieDest, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(dati_altri2Layout.createSequentialGroup()
                        .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(labDDRagSoc)
                            .add(texDestRagioneSociale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(2, 2, 2)
                        .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(labDDIndirizzo)
                            .add(texDestIndirizzo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(2, 2, 2)
                        .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(labDDCap)
                            .add(texDestCap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(labDDLoc)
                            .add(texDestLocalita, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(labDDProv)
                            .add(texDestProvincia, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(2, 2, 2)
                        .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(labDDTel)
                            .add(texDestTelefono, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(labDDCel)
                            .add(texDestCellulare, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(2, 2, 2)
                        .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(labDDPaese)
                            .add(comPaese, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(labFaTitolo))
                    .add(sepFattAcc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labFa1)
                    .add(comCausaleTrasporto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(2, 2, 2)
                .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labFa2)
                    .add(comAspettoEsterioreBeni, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(2, 2, 2)
                .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labFa3)
                    .add(texNumeroColli, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(2, 2, 2)
                .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labFa4)
                    .add(comVettori, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(2, 2, 2)
                .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labFa5)
                    .add(comMezzoTrasporto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(2, 2, 2)
                .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labFa6)
                    .add(comPorto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(2, 2, 2)
                .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dati_altri2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel6)
                        .add(texFornitoreIntelli, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(apriFornitori, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dati_altri2Layout.linkSize(new java.awt.Component[] {comCausaleTrasporto, labFa1}, org.jdesktop.layout.GroupLayout.VERTICAL);

        dati_altri2Layout.linkSize(new java.awt.Component[] {comAspettoEsterioreBeni, labFa2}, org.jdesktop.layout.GroupLayout.VERTICAL);

        dati_altri2Layout.linkSize(new java.awt.Component[] {labFa3, texNumeroColli}, org.jdesktop.layout.GroupLayout.VERTICAL);

        dati_altri2Layout.linkSize(new java.awt.Component[] {comVettori, labFa4}, org.jdesktop.layout.GroupLayout.VERTICAL);

        dati_altri2Layout.linkSize(new java.awt.Component[] {comMezzoTrasporto, labFa5}, org.jdesktop.layout.GroupLayout.VERTICAL);

        dati_altri2Layout.linkSize(new java.awt.Component[] {comPorto, labFa6}, org.jdesktop.layout.GroupLayout.VERTICAL);

        dati_altri2Layout.linkSize(new java.awt.Component[] {apriFornitori, texFornitoreIntelli}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("Note pagamento");

        texNotePagamento.setColumns(40);
        texNotePagamento.setText("note pagamento");
        texNotePagamento.setDbDescCampo("");
        texNotePagamento.setDbNomeCampo("note_pagamento");
        texNotePagamento.setDbTipoCampo("");
        texNotePagamento.setmaxChars(255);

        cheOpzioneRibaDestDiversa.setText("stampa Dest. Diversa su distinta");
        cheOpzioneRibaDestDiversa.setToolTipText("Selezionando questa opzione stampa la Destinazione diversa nella Distinta delle RIBA");
        cheOpzioneRibaDestDiversa.setDbDescCampo("Opzione Dest. Diversa Riba");
        cheOpzioneRibaDestDiversa.setDbNomeCampo("opzione_riba_dest_diversa");
        cheOpzioneRibaDestDiversa.setDbTipoCampo("");
        cheOpzioneRibaDestDiversa.setFont(cheOpzioneRibaDestDiversa.getFont().deriveFont(cheOpzioneRibaDestDiversa.getFont().getSize()-1f));

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("Banca ABI");

        texBancAbi.setColumns(6);
        texBancAbi.setToolTipText("");
        texBancAbi.setDbDescCampo("");
        texBancAbi.setDbNomeCampo("banca_abi");
        texBancAbi.setDbTipoCampo("");
        texBancAbi.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texBancAbiFocusLost(evt);
            }
        });
        texBancAbi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texBancAbiActionPerformed(evt);
            }
        });

        butCoor.setFont(butCoor.getFont().deriveFont(butCoor.getFont().getSize()-1f));
        butCoor.setText("cerca");
        butCoor.setMargin(new java.awt.Insets(1, 2, 1, 2));
        butCoor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCoorActionPerformed(evt);
            }
        });

        labBancAbi.setFont(labBancAbi.getFont().deriveFont(labBancAbi.getFont().getSize()-1f));
        labBancAbi.setText("...");

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText("Banca CAB");

        texBancCab.setColumns(6);
        texBancCab.setToolTipText("");
        texBancCab.setDbDescCampo("");
        texBancCab.setDbNomeCampo("banca_cab");
        texBancCab.setDbTipoCampo("");
        texBancCab.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texBancCabFocusLost(evt);
            }
        });
        texBancCab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texBancCabActionPerformed(evt);
            }
        });

        labBancCab.setFont(labBancCab.getFont().deriveFont(labBancCab.getFont().getSize()-1f));
        labBancCab.setText("...");

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("IBAN");

        texBancIban.setColumns(20);
        texBancIban.setToolTipText("");
        texBancIban.setDbDescCampo("");
        texBancIban.setDbNomeCampo("banca_iban");
        texBancIban.setDbTipoCampo("");
        texBancIban.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texBancIbanFocusLost(evt);
            }
        });
        texBancIban.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texBancIbanActionPerformed(evt);
            }
        });

        labAgente.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labAgente.setText("Agente");

        comAgente.setToolTipText("");
        comAgente.setDbDescCampo("");
        comAgente.setDbNomeCampo("agente_codice");
        comAgente.setDbTipoCampo("numerico");
        comAgente.setDbTrovaMentreScrive(true);
        comAgente.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                comAgenteFocusLost(evt);
            }
        });
        comAgente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comAgenteActionPerformed(evt);
            }
        });

        labProvvigione.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labProvvigione.setText("Provvigione");

        texProvvigione.setColumns(5);
        texProvvigione.setToolTipText("");
        texProvvigione.setDbDescCampo("");
        texProvvigione.setDbNomeCampo("agente_percentuale");
        texProvvigione.setDbTipoCampo("numerico");
        texProvvigione.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                texProvvigioneFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                texProvvigioneFocusLost(evt);
            }
        });

        labPercentoProvvigione.setText("%");

        jLabel54.setFont(jLabel54.getFont().deriveFont((jLabel54.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel54.setText("Coordinate bancarie del Cliente (RIBA) ");

        org.jdesktop.layout.GroupLayout dati_altri1Layout = new org.jdesktop.layout.GroupLayout(dati_altri1);
        dati_altri1.setLayout(dati_altri1Layout);
        dati_altri1Layout.setHorizontalGroup(
            dati_altri1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dati_altri1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel24)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(texNotePagamento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
            .add(dati_altri1Layout.createSequentialGroup()
                .add(2, 2, 2)
                .add(labAgente)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(comAgente, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(labProvvigione)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(texProvvigione, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labPercentoProvvigione))
            .add(dati_altri1Layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(jLabel54)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cheOpzioneRibaDestDiversa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, Short.MAX_VALUE))
            .add(dati_altri1Layout.createSequentialGroup()
                .add(15, 15, 15)
                .add(jLabel20)
                .add(5, 5, 5)
                .add(texBancCab, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labBancCab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(dati_altri1Layout.createSequentialGroup()
                .add(15, 15, 15)
                .add(jLabel23)
                .add(5, 5, 5)
                .add(texBancIban, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(dati_altri1Layout.createSequentialGroup()
                .add(15, 15, 15)
                .add(jLabel18)
                .add(5, 5, 5)
                .add(texBancAbi, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(butCoor)
                .add(5, 5, 5)
                .add(labBancAbi, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dati_altri1Layout.linkSize(new java.awt.Component[] {jLabel18, jLabel20, jLabel23}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        dati_altri1Layout.setVerticalGroup(
            dati_altri1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dati_altri1Layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(dati_altri1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texNotePagamento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel24))
                .add(2, 2, 2)
                .add(dati_altri1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel54)
                    .add(cheOpzioneRibaDestDiversa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(2, 2, 2)
                .add(dati_altri1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel18)
                    .add(texBancAbi, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(butCoor)
                    .add(labBancAbi))
                .add(dati_altri1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel20)
                    .add(dati_altri1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(texBancCab, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(labBancCab)))
                .add(dati_altri1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel23)
                    .add(texBancIban, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(2, 2, 2)
                .add(dati_altri1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(comAgente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labAgente)
                    .add(labProvvigione)
                    .add(texProvvigione, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labPercentoProvvigione)))
        );

        dati_altri1Layout.linkSize(new java.awt.Component[] {comAgente, labAgente, labPercentoProvvigione, labProvvigione, texProvvigione}, org.jdesktop.layout.GroupLayout.VERTICAL);

        dati_altri1Layout.linkSize(new java.awt.Component[] {butCoor, jLabel18, labBancAbi, texBancAbi}, org.jdesktop.layout.GroupLayout.VERTICAL);

        dati_altri1Layout.linkSize(new java.awt.Component[] {jLabel20, labBancCab, texBancCab}, org.jdesktop.layout.GroupLayout.VERTICAL);

        dati_altri1Layout.linkSize(new java.awt.Component[] {jLabel23, texBancIban}, org.jdesktop.layout.GroupLayout.VERTICAL);

        labRiferimento.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labRiferimento.setText("Consegna");

        texConsegna.setColumns(15);
        texConsegna.setText("consegna");
        texConsegna.setDbDescCampo("");
        texConsegna.setDbNomeCampo("data_consegna");
        texConsegna.setDbTipoCampo("");
        texConsegna.setmaxChars(255);

        textTipoSnj.setBackground(new java.awt.Color(255, 200, 200));
        textTipoSnj.setText("tipo_snj");
        textTipoSnj.setDbDescCampo("");
        textTipoSnj.setDbNomeCampo("tipo_snj");
        textTipoSnj.setDbTipoCampo("");

        prezzi_ivati.setBackground(new java.awt.Color(255, 204, 204));
        prezzi_ivati.setText("prezzi ivati");
        prezzi_ivati.setToolTipText("Selezionando questa opzione stampa la Destinazione diversa nella Distinta delle RIBA");
        prezzi_ivati.setDbDescCampo("Prezzi Ivati");
        prezzi_ivati.setDbNomeCampo("prezzi_ivati");
        prezzi_ivati.setDbTipoCampo("");
        prezzi_ivati.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        prezzi_ivati.setIconTextGap(1);

        consegna_prevista.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                consegna_previstaPropertyChange(evt);
            }
        });

        jLabel115.setText("Sp. incasso");

        labModConsegna.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labModConsegna.setText("Modalità di consegna");

        comConsegna.setDbDescCampo("Modalità di consegna");
        comConsegna.setDbNomeCampo("modalita_consegna");
        comConsegna.setDbTipoCampo("");
        comConsegna.setDbTrovaMentreScrive(true);
        comConsegna.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comConsegnaActionPerformed(evt);
            }
        });

        comScarico.setDbDescCampo("Modalità di scarico");
        comScarico.setDbNomeCampo("modalita_scarico");
        comScarico.setDbTipoCampo("");
        comScarico.setDbTrovaMentreScrive(true);
        comScarico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comScaricoActionPerformed(evt);
            }
        });

        labModScarico.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labModScarico.setText("Scarico");

        texCliente.setColumns(24);
        texCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                texClienteMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                texClienteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                texClienteMouseExited(evt);
            }
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

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        texForni.setColumns(3);
        texForni.setDbNomeCampo("fornitore");
        texForni.setFont(texForni.getFont().deriveFont(texForni.getFont().getSize()-1f));
        texForni.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texForniFocusLost(evt);
            }
        });

        jLabel4.setToolTipText("");

        org.jdesktop.layout.GroupLayout datiLayout = new org.jdesktop.layout.GroupLayout(dati);
        dati.setLayout(datiLayout);
        datiLayout.setHorizontalGroup(
            datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, datiLayout.createSequentialGroup()
                .addContainerGap()
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(datiLayout.createSequentialGroup()
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(datiLayout.createSequentialGroup()
                                .add(labScon1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(labScon21)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(labScon2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel114))
                            .add(datiLayout.createSequentialGroup()
                                .add(texScon1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texScon2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texScon3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texSpeseTrasporto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel115)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel113))
                            .add(datiLayout.createSequentialGroup()
                                .add(texSpeseIncasso, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(consegna_prevista, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(datiLayout.createSequentialGroup()
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(datiLayout.createSequentialGroup()
                                .add(texSeri, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texNumeOrdine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel14)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel13)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel16)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, datiLayout.createSequentialGroup()
                                .add(jLabel151)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(butPrezziPrec))
                            .add(texCliente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 202, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(0, 0, 0)
                        .add(apriclienti, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(butAddClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, datiLayout.createSequentialGroup()
                            .add(labModConsegna)
                            .add(5, 5, 5)
                            .add(comConsegna, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                            .add(labModScarico)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(comScarico, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jLabel4))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, datiLayout.createSequentialGroup()
                            .add(jLabel19)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(comPaga, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(0, 0, 0)
                            .add(labGiornoPagamento)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(texGiornoPagamento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel17)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texPaga2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 173, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(labRiferimento)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texConsegna, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel11)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texNote, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 381, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(dati_altri1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dati_altri2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(datiLayout.createSequentialGroup()
                        .add(texAnno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(textTipoSnj, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(datiLayout.createSequentialGroup()
                        .add(texClieDest, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texTipoOrdine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(texTotaImpo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texTotaIva1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texTota1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(texStat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(prezzi_ivati, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(datiLayout.createSequentialGroup()
                        .add(texClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(texForni, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(31, 31, 31))
        );

        datiLayout.linkSize(new java.awt.Component[] {jLabel14, texSeri}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {jLabel13, texNumeOrdine}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {jLabel16, texData}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {labScon1, texScon1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {labScon21, texScon2}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {labScon2, texScon3}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {jLabel114, texSpeseTrasporto}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {jLabel115, texSpeseIncasso}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.linkSize(new java.awt.Component[] {jLabel17, jLabel19}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiLayout.setVerticalGroup(
            datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiLayout.createSequentialGroup()
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator3)
                    .add(datiLayout.createSequentialGroup()
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(datiLayout.createSequentialGroup()
                                .add(2, 2, 2)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel14)
                                    .add(jLabel13)
                                    .add(jLabel16)
                                    .add(jLabel151)
                                    .add(butPrezziPrec))
                                .add(0, 0, 0)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(texSeri, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(texNumeOrdine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(texData, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(texCliente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(apriclienti, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(butAddClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(1, 1, 1)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(labScon1)
                                    .add(labScon21)
                                    .add(labScon2)
                                    .add(jLabel114)
                                    .add(jLabel115)
                                    .add(jLabel113))
                                .add(1, 1, 1)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(texScon1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texScon2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texScon3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texSpeseTrasporto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texSpeseIncasso, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(consegna_prevista, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(2, 2, 2)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel11)
                                    .add(texNote, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(3, 3, 3)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel17)
                                    .add(texPaga2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(labRiferimento)
                                    .add(texConsegna, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(3, 3, 3)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(jLabel19)
                                    .add(comPaga, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(labGiornoPagamento)
                                    .add(texGiornoPagamento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(dati_altri1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(2, 2, 2)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(labModConsegna)
                                        .add(comConsegna, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(datiLayout.createSequentialGroup()
                                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                            .add(labModScarico)
                                            .add(comScarico, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jLabel4))
                                        .add(1, 1, 1))))
                            .add(datiLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(texClieDest, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texTipoOrdine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(texAnno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(textTipoSnj, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texTotaImpo1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texTotaIva1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texTota1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texStat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(prezzi_ivati, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(texClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(comClie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texForni, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(dati_altri2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        datiLayout.linkSize(new java.awt.Component[] {apriclienti, butAddClie, texCliente}, org.jdesktop.layout.GroupLayout.VERTICAL);

        datiLayout.linkSize(new java.awt.Component[] {comConsegna, comScarico, labModConsegna, labModScarico}, org.jdesktop.layout.GroupLayout.VERTICAL);

        split.setLeftComponent(dati);

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

        prezzi_ivati_virtual.setFont(prezzi_ivati_virtual.getFont().deriveFont(prezzi_ivati_virtual.getFont().getSize()-1f));
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
        butNuovArti.setText("Nuova riga");
        butNuovArti.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butNuovArti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNuovArtiActionPerformed(evt);
            }
        });
        jPanel1.add(butNuovArti);

        butInserisciSubTotale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butInserisciSubTotale.setText("Sub-totale");
        butInserisciSubTotale.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butInserisciSubTotale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butInserisciSubTotaleActionPerformed(evt);
            }
        });
        jPanel1.add(butInserisciSubTotale);

        butInserisciPeso.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butInserisciPeso.setText("Peso");
        butInserisciPeso.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butInserisciPeso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butInserisciPesoActionPerformed(evt);
            }
        });
        jPanel1.add(butInserisciPeso);

        butImportRighe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butImportRighe.setText("Importa Righe Da CSV");
        butImportRighe.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butImportRighe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butImportRigheActionPerformed(evt);
            }
        });
        jPanel1.add(butImportRighe);

        butImportRigheProskin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butImportRigheProskin.setText("Righe da CC Xls");
        butImportRigheProskin.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butImportRigheProskin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butImportRigheProskinActionPerformed(evt);
            }
        });
        jPanel1.add(butImportRigheProskin);

        butImportXlsCirri.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butImportXlsCirri.setText("Import xls");
        butImportXlsCirri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butImportXlsCirriActionPerformed(evt);
            }
        });
        jPanel1.add(butImportXlsCirri);

        org.jdesktop.layout.GroupLayout datiRigheLayout = new org.jdesktop.layout.GroupLayout(datiRighe);
        datiRighe.setLayout(datiRigheLayout);
        datiRigheLayout.setHorizontalGroup(
            datiRigheLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 967, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 967, Short.MAX_VALUE)
        );
        datiRigheLayout.setVerticalGroup(
            datiRigheLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiRigheLayout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                .add(0, 0, 0))
        );

        split.setRightComponent(datiRighe);

        panDati.add(split, java.awt.BorderLayout.CENTER);

        panTab.addTab("Dati", panDati);

        panFoglioRigheSNJA.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                panFoglioRigheSNJAFocusGained(evt);
            }
        });
        panFoglioRigheSNJA.setLayout(new java.awt.BorderLayout());

        panGriglia.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                panGrigliaFocusGained(evt);
            }
        });
        panGriglia.setLayout(new java.awt.BorderLayout());

        foglioTipoA.setModel(new javax.swing.table.DefaultTableModel(
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
        foglioTipoA.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                foglioTipoAMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                foglioTipoAMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                foglioTipoAMouseReleased(evt);
            }
        });
        jScrollPane2.setViewportView(foglioTipoA);

        panGriglia.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        panFoglioRigheSNJA.add(panGriglia, java.awt.BorderLayout.CENTER);

        labStatus.setText("...");
        panTotale.add(labStatus);

        panFoglioRigheSNJA.add(panTotale, java.awt.BorderLayout.SOUTH);

        panTab.addTab("Foglio Righe Tipo A", panFoglioRigheSNJA);

        panFoglioRigheSNJB.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                panFoglioRigheSNJBFocusGained(evt);
            }
        });
        panFoglioRigheSNJB.setLayout(new java.awt.BorderLayout());

        panGriglia1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                panGriglia1FocusGained(evt);
            }
        });
        panGriglia1.setLayout(new java.awt.BorderLayout());

        foglioTipoB.setModel(new javax.swing.table.DefaultTableModel(
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
        foglioTipoB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                foglioTipoBMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                foglioTipoBMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                foglioTipoBMouseReleased(evt);
            }
        });
        jScrollPane3.setViewportView(foglioTipoB);

        panGriglia1.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        panFoglioRigheSNJB.add(panGriglia1, java.awt.BorderLayout.CENTER);

        labStatus1.setText("...");
        panTotale1.add(labStatus1);

        panFoglioRigheSNJB.add(panTotale1, java.awt.BorderLayout.SOUTH);

        panTab.addTab("Foglio Righe Tipo B", panFoglioRigheSNJB);

        org.jdesktop.layout.GroupLayout pan_segnaposto_depositoLayout = new org.jdesktop.layout.GroupLayout(pan_segnaposto_deposito);
        pan_segnaposto_deposito.setLayout(pan_segnaposto_depositoLayout);
        pan_segnaposto_depositoLayout.setHorizontalGroup(
            pan_segnaposto_depositoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 10, Short.MAX_VALUE)
        );
        pan_segnaposto_depositoLayout.setVerticalGroup(
            pan_segnaposto_depositoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 10, Short.MAX_VALUE)
        );

        labNoteConsegna.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labNoteConsegna.setText("Note consegna");

        texNoteConsegna.setDbNomeCampo("note_consegna");
        texNoteConsegna.setRows(5);

        jLabel5.setText("Contatto di riferimento");
        jLabel5.setToolTipText("Verrà stampato sotto ai dati del cliente");

        comContattoRiferimento.setDbNomeCampo("nome_contatto_riferimento");
        comContattoRiferimento.setDbRiempireForceText(true);
        comContattoRiferimento.setDbSalvaKey(false);
        comContattoRiferimento.setDbTrovaMentreScrive(true);
        comContattoRiferimento.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                comContattoRiferimentoFocusGained(evt);
            }
        });
        comContattoRiferimento.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                comContattoRiferimentoPopupMenuWillBecomeVisible(evt);
            }
        });

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
                    .add(datiAltroLayout.createSequentialGroup()
                        .add(labNoteConsegna)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texNoteConsegna, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .add(datiAltroLayout.createSequentialGroup()
                        .add(datiAltroLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(datiAltroLayout.createSequentialGroup()
                                .add(labCampoLibero1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comCampoLibero1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(pan_segnaposto_deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(datiAltroLayout.createSequentialGroup()
                                .add(jLabel5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comContattoRiferimento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        datiAltroLayout.linkSize(new java.awt.Component[] {jLabel5, labNoteConsegna}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        datiAltroLayout.setVerticalGroup(
            datiAltroLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiAltroLayout.createSequentialGroup()
                .addContainerGap()
                .add(pan_segnaposto_deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiAltroLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(labNoteConsegna)
                    .add(texNoteConsegna, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(datiAltroLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(comContattoRiferimento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiAltroLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labCampoLibero1)
                    .add(comCampoLibero1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(311, Short.MAX_VALUE))
        );

        panTab.addTab("Altro", datiAltro);

        getContentPane().add(panTab, java.awt.BorderLayout.CENTER);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        butPdf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/pdf-icon-16.png"))); // NOI18N
        butPdf.setText("Crea PDF");
        butPdf.setMargin(new java.awt.Insets(2, 4, 2, 4));
        butPdf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPdfActionPerformed(evt);
            }
        });

        butStampa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-print.png"))); // NOI18N
        butStampa.setText("Stampa");
        butStampa.setMargin(new java.awt.Insets(2, 4, 2, 4));
        butStampa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butStampaActionPerformed(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setPreferredSize(new java.awt.Dimension(4, 20));

        butUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-undo.png"))); // NOI18N
        butUndo.setText("Annulla");
        butUndo.setMargin(new java.awt.Insets(2, 4, 2, 4));
        butUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butUndoActionPerformed(evt);
            }
        });

        butSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/devices/media-floppy.png"))); // NOI18N
        butSave.setText("Salva");
        butSave.setMargin(new java.awt.Insets(2, 4, 2, 4));
        butSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSaveActionPerformed(evt);
            }
        });

        jLabel1.setText("Stato");

        comStatoOrdine.setDbNomeCampo("vettore1");
        comStatoOrdine.setDbRiempireForceText(true);
        comStatoOrdine.setDbSalvaKey(false);
        comStatoOrdine.setDbSalvare(false);
        comStatoOrdine.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comStatoOrdineItemStateChanged(evt);
            }
        });
        comStatoOrdine.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                comStatoOrdineMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                comStatoOrdineMouseReleased(evt);
            }
        });

        stato_evasione.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Evaso", "Evaso Parziale", "Non Evaso" }));
        stato_evasione.setSelectedIndex(2);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, butStampa, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3Layout.createSequentialGroup()
                        .add(butUndo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(butSave, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(butPdf, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(stato_evasione, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(comStatoOrdine, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(butSave, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(butUndo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(butStampa))
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(stato_evasione, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comStatoOrdine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(butPdf)
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        texTotaImpo.setEditable(false);
        texTotaImpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        texTotaImpo.setColumns(8);
        texTotaImpo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texTotaImpo.setText("0");
        texTotaImpo.setDbTipoCampo("valuta");

        texTotaIva.setEditable(false);
        texTotaIva.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        texTotaIva.setColumns(8);
        texTotaIva.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texTotaIva.setText("0");
        texTotaIva.setDbTipoCampo("valuta");

        texTota.setEditable(false);
        texTota.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        texTota.setColumns(8);
        texTota.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texTota.setText("0");
        texTota.setDbTipoCampo("valuta");
        texTota.setFont(texTota.getFont().deriveFont(texTota.getFont().getStyle() | java.awt.Font.BOLD, texTota.getFont().getSize()+1));

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() | java.awt.Font.BOLD, jLabel2.getFont().getSize()+1));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Totale");

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("Totale Iva");

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText("Totale Imponibile");

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("Sconto");

        texSconto.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        texSconto.setColumns(8);
        texSconto.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texSconto.setText("0");
        texSconto.setDbTipoCampo("valuta");
        texSconto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texScontoKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texScontoKeyReleased(evt);
            }
        });

        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel27.setText("Acconto");

        texAcconto.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        texAcconto.setColumns(8);
        texAcconto.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texAcconto.setText("0");
        texAcconto.setDbTipoCampo("valuta");
        texAcconto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texAccontoKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texAccontoKeyReleased(evt);
            }
        });

        jLabel3.setFont(jLabel3.getFont().deriveFont(jLabel3.getFont().getStyle() | java.awt.Font.BOLD, jLabel3.getFont().getSize()+1));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Tot. da Pagare");

        texTotaDaPagareFinale.setEditable(false);
        texTotaDaPagareFinale.setBorder(null);
        texTotaDaPagareFinale.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texTotaDaPagareFinale.setText("0");
        texTotaDaPagareFinale.setDbTipoCampo("valuta");
        texTotaDaPagareFinale.setFont(texTotaDaPagareFinale.getFont().deriveFont(texTotaDaPagareFinale.getFont().getStyle() | java.awt.Font.BOLD, texTotaDaPagareFinale.getFont().getSize()+1));

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap(79, Short.MAX_VALUE)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .add(jLabel27)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texAcconto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texTotaDaPagareFinale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .add(jLabel21)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texTotaIva, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel25)
                            .add(jLabel22))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(texSconto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(texTotaImpo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texTota, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(new java.awt.Component[] {texAcconto, texSconto, texTota, texTotaDaPagareFinale, texTotaImpo, texTotaIva}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(3, 3, 3)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texSconto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel25))
                .add(2, 2, 2)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texTotaImpo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel22))
                .add(2, 2, 2)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texTotaIva, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel21))
                .add(2, 2, 2)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texTota, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .add(2, 2, 2)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel27)
                    .add(texAcconto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texTotaDaPagareFinale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(2, 2, 2))
        );

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void comPagaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comPagaItemStateChanged
        texGiornoPagamento.setEnabled(false);
        labGiornoPagamento.setEnabled(false);

        try {

            ResultSet r = Db.lookUp(String.valueOf(comPaga.getSelectedKey()), "codice", "pagamenti");
            if (r != null && Db.nz(r.getString("flag_richiedi_giorno"), "N").equalsIgnoreCase("S")) {
                texGiornoPagamento.setEnabled(true);
                labGiornoPagamento.setEnabled(true);

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
            } else {
                texGiornoPagamento.setEnabled(false);
                labGiornoPagamento.setEnabled(false);
                texGiornoPagamento.setText("");
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }//GEN-LAST:event_comPagaItemStateChanged

    private void texSeriFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texSeriFocusLost
    }//GEN-LAST:event_texSeriFocusLost

    private void texSeriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texSeriKeyPressed

        if (evt.getKeyCode() == evt.VK_TAB || evt.getKeyCode() == evt.VK_ENTER) {
            assegnaSerie();
        }
    }//GEN-LAST:event_texSeriKeyPressed

    private void comAgenteFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comAgenteFocusLost
        InvoicexUtil.controlloProvvigioniAutomatiche(comAgente, texProvvigione, texScon1, this, acquisto ? null : cu.toInteger(texClie.getText()));
    }//GEN-LAST:event_comAgenteFocusLost

    private void comAgenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comAgenteActionPerformed
    }//GEN-LAST:event_comAgenteActionPerformed

    private void texClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texClieActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_texClieActionPerformed

    private void comPagaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comPagaFocusLost
        if (!acquisto) {
            //carico note su pagamento
            try {
                ResultSet p = Db.openResultSet("select * from pagamenti where codice = " + Db.pc(this.comPaga.getSelectedKey(), Types.VARCHAR));
                if (p.next()) {
                    this.texNotePagamento.setText(p.getString("note_su_documenti"));
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }//GEN-LAST:event_comPagaFocusLost

    private void comClieDestKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comClieDestKeyPressed

        if (evt.getKeyCode() == evt.VK_ENTER) {
            caricaDestinazioneDiversa();
        }
    }//GEN-LAST:event_comClieDestKeyPressed

    private void comClieDestFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comClieDestFocusLost
        if (comClieDest.getSelectedIndex() != comClieDest_old) {
            caricaDestinazioneDiversa();
        }
    }//GEN-LAST:event_comClieDestFocusLost

    private void comClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comClieActionPerformed
        //apro combo destinazione cliente
        if (!loading) {
            sql = "select obsoleto from clie_forn";
            sql += " where codice = " + Db.pc(this.texClie.getText(), "NUMERIC");

            ResultSet rs = Db.openResultSet(sql);
            try {
                if (rs.next()) {
                    int obsoleto = rs.getInt("obsoleto");
                    if (obsoleto == 1) {
                        JOptionPane.showMessageDialog(this, "Attenzione, il cliente selezionato è segnato come obsoleto.", "Cliente obsoleto", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        sql = "select ragione_sociale, id from clie_forn_dest";
        sql += " where codice_cliente = " + Db.pc(this.texClie.getText(), "NUMERIC");
        sql += " order by ragione_sociale";

        riempiDestDiversa(sql);

    }//GEN-LAST:event_comClieActionPerformed

    private void texDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texDataActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_texDataActionPerformed

    private void butAddClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAddClieActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        InvoicexUtil.genericFormAddCliente(this);
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_butAddClieActionPerformed

    private void butStampaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butStampaActionPerformed
        if (block_aggiornareProvvigioni) {
            return;
        }

        if (SwingUtils.showYesNoMessage(this, "Prima di stampare è necessario salvare il documento, proseguire ?")) {
            if (controlloCampi() == true) {
                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                String dbSerie = this.dbdoc.serie;
                int dbNumero = this.dbdoc.numero;
                int dbAnno = this.dbdoc.anno;

                if (saveDocumento()) {
                    main.events.fireInvoicexEvent(new InvoicexEvent(this, InvoicexEvent.TYPE_SAVE));

                    if (evt.getActionCommand().equalsIgnoreCase("pdf")) {
                        try {
                            InvoicexUtil.creaPdf(getTipoDoc(), new Integer[]{id}, true, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                            SwingUtils.showExceptionMessage(this, e);
                        }
                    } else {
                        frmElenOrdini.stampa("", dbSerie, dbNumero, dbAnno, acquisto, id);
                    }

                    String nuova_serie = texSeri.getText();
                    Integer nuovo_numero = cu.toInteger(texNumeOrdine.getText());
                    Integer nuovo_anno = cu.toInteger(texAnno.getText());

                    //aggiorno le righe temp
                    if (!main.edit_doc_in_temp) {
                        sql = "update " + getNomeTabRighe();
                        sql += " set serie = '" + nuova_serie + "'";
                        sql += " , numero = " + nuovo_numero + "";
                        sql += " , anno = " + nuovo_anno + "";
                        sql += " where serie = " + db.pc(serie_originale, "VARCHAR");
                        sql += " and numero = " + numero_originale;
                        sql += " and anno = " + anno_originale;
                        Db.executeSqlDialogExc(sql, true);
                    }

                    serie_originale = texSeri.getText();
                    numero_originale = cu.toInteger(texNumeOrdine.getText());
                    anno_originale = cu.toInteger(texAnno.getText());

                    totaleDaPagareFinaleIniziale = doc.getTotale_da_pagare_finale();
                    pagamentoIniziale = comPaga.getText();
                    pagamentoInizialeGiorno = texGiornoPagamento.getText();
                    provvigioniIniziale = Db.getDouble(texProvvigione.getText());
                    codiceAgenteIniziale = it.tnx.Util.getInt(comAgente.getSelectedKey().toString());

                    //una volta salvatao e stampato entro in modalitaà modifica se ero in inserimento
                    if (!main.edit_doc_in_temp) {
                        if (dbStato.equals(frmTestOrdine.DB_INSERIMENTO)) {
                            dbStato = frmTestOrdine.DB_MODIFICA;
                            //e riporto le righe in _temp
                            sql = "insert into righ_ordi" + suff + "_temp";
                            sql += " select *, '" + main.login + "' as username";
                            sql += " from righ_ordi" + suff;
                            sql += " where serie = " + db.pc(nuova_serie, "VARCHAR");
                            sql += " and numero = " + nuovo_numero;
                            sql += " and anno = " + nuovo_anno;
                            try {
                                DbUtils.tryExecQuery(Db.getConn(), sql);
                                System.out.println("sql ok:" + sql);
                            } catch (Exception e) {
                                System.err.println("sql errore:" + sql);
                                e.printStackTrace();
                            }
                        } else {
                            porto_in_temp();
                        }
                    }
                    dati.dbCheckModificatiReset();
                }
            }
        }

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_butStampaActionPerformed

    private void texSpeseTrasportoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texSpeseTrasportoActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_texSpeseTrasportoActionPerformed

    private void butPrezziPrecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPrezziPrecActionPerformed
        showPrezziFatture();
    }//GEN-LAST:event_butPrezziPrecActionPerformed

    private void texSpeseTrasportoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texSpeseTrasportoKeyReleased

        try {
            dbdoc.speseTrasportoIva = Db.getDouble(this.texSpeseTrasporto.getText());
        } catch (Exception err) {
            dbdoc.speseTrasportoIva = 0;
        }

        dbdoc.dbRefresh();

        ricalcolaTotali();
    }//GEN-LAST:event_texSpeseTrasportoKeyReleased

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
    }//GEN-LAST:event_formInternalFrameClosing

    private void texBancAbiFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texBancAbiFocusLost
        texBancAbiActionPerformed(null);
    }//GEN-LAST:event_texBancAbiFocusLost

    private void texBancCabFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texBancCabFocusLost
        trovaCab();
    }//GEN-LAST:event_texBancCabFocusLost

    private void texBancCCFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texBancCCFocusLost
    }//GEN-LAST:event_texBancCCFocusLost

    private void texBancCabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texBancCabActionPerformed
        trovaCab();
    }//GEN-LAST:event_texBancCabActionPerformed

    private void texBancAbiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texBancAbiActionPerformed
        caricaBanca();
    }//GEN-LAST:event_texBancAbiActionPerformed

    public void caricaBanca() {
        try {
            String descAbi = Db.lookUp(texBancAbi.getText(), "abi", "banche_abi").getString(2);
            descAbi = StringUtils.abbreviate(descAbi, CoordinateBancarie.maxAbi);
            labBancAbi.setText(descAbi);
        } catch (Exception err) {
            labBancAbi.setText("");
        }
    }

    private void butCoorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCoorActionPerformed

        CoordinateBancarie coords = new CoordinateBancarie();
        coords.setField_texBancAbi(this.texBancAbi);
        coords.setField_labBancAbi(this.labBancAbi);
        coords.setField_texBancCab(this.texBancCab);
        coords.setField_labBancCab(this.labBancCab);

        frmListCoorBanc frm = new frmListCoorBanc(coords);

        main.getPadre().openFrame(frm, 700, 500, 150, 50);
    }//GEN-LAST:event_butCoorActionPerformed

    private void comClieItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comClieItemStateChanged
//        if (evt.getStateChange() == ItemEvent.SELECTED && !loading) {
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

    private void texDataFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texDataFocusLost
        if (!ju.isValidDateIta(texData.getText())) {
            SwingUtils.showFlashMessage2Comp("Data non valida", 3, texData, Color.red);
            return;
        }

        if (!old_anno.equals(getAnno())) {
            if (dbStato == DB_INSERIMENTO) {
                dbdoc.dbRicalcolaProgressivo(dbStato, this.texData.getText(), this.texNumeOrdine, texAnno, texSeri.getText(), id);
                dbdoc.numero = new Integer(this.texNumeOrdine.getText()).intValue();
                id_modificato = true;
            } else {
                //controllo che se è un numero già presente non glielo faccio fare percè altrimenti sovrascrive una altra fattura
                sql = "select numero from test_ordi" + suff;
                sql += " where serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
                sql += " and numero " + Db.pcW(texNumeOrdine.getText(), "NUMBER");
                sql += " and anno " + Db.pcW(getAnno(), "VARCHAR");
                ResultSet r = Db.openResultSet(sql);
                try {
                    if (r.next()) {
                        texData.setText(old_data);
                        JOptionPane.showMessageDialog(this, "Non puoi mettere questo numero e data, si sovrappongono ad un documento già presente !", "Attenzione", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            try {
                texAnno.setText(getAnno());
                dbdoc.anno = Integer.parseInt(getAnno());
                dbdoc.numero = Integer.parseInt(texNumeOrdine.getText());

                if (!main.edit_doc_in_temp) {
                    sql = "update righ_ordi" + suff;
                    sql += " set anno = " + Db.pc(dbdoc.anno, "NUMBER");
                    sql += " , numero = " + Db.pc(dbdoc.numero, "NUMBER");
                    sql += " where serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
                    sql += " and numero " + Db.pcW(old_id, "NUMBER");
                    sql += " and anno " + Db.pcW(old_anno, "VARCHAR");
                    Db.executeSql(sql);

                    sql = "update test_ordi" + suff;
                    sql += " set anno = " + Db.pc(dbdoc.anno, "NUMBER");
                    sql += " , numero = " + Db.pc(dbdoc.numero, "NUMBER");
                    sql += " where serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
                    sql += " and numero " + Db.pcW(old_id, "NUMBER");
                    sql += " and anno " + Db.pcW(old_anno, "VARCHAR");
                    Db.executeSql(sql);

                    //                dati.dbChiaveValori.clear();
                    //                dati.dbChiaveValori.put("serie", ordine.serie);
                    //                dati.dbChiaveValori.put("numero", ordine.numero);
                    //                dati.dbChiaveValori.put("anno", ordine.anno);
                    //riassocio
                    dbAssociaGrigliaRighe();

                    doc.load(Db.INSTANCE, dbdoc.numero, dbdoc.serie, dbdoc.anno, acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE, id);
                    ricalcolaTotali();

                    anno_modificato = true;
                }

            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }//GEN-LAST:event_texDataFocusLost

    private void texScon3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon3KeyReleased

        try {
            dbdoc.sconto3 = Db.getDouble(this.texScon3.getText());
        } catch (Exception err) {

            //err.printStackTrace();
            dbdoc.sconto3 = 0;
        }

        dbdoc.dbRefresh();

        ricalcolaTotali();
    }//GEN-LAST:event_texScon3KeyReleased

    private void comClieKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comClieKeyPressed

        // Add your handling code here:
        if (evt.getKeyCode() == evt.VK_ENTER) {
            this.recuperaDatiCliente();
        }

        //ricerca con F4
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
                    javax.swing.JOptionPane.showMessageDialog(this, "Nessun cliente trovato");
                }
            } catch (Exception err) {
                err.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this, "Errore nella ricerca cliente: " + err.toString());
            }
        }
    }//GEN-LAST:event_comClieKeyPressed

    private void texClieKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texClieKeyPressed

        // Add your handling code here:
        if (evt.getKeyCode() == evt.VK_ENTER) {
            this.recuperaDatiCliente();
        }
    }//GEN-LAST:event_texClieKeyPressed

    private void comClieFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comClieFocusLost
        if (comClie.getSelectedIndex() != comClieSel_old) {
            if (comClie.getSelectedIndex() == -1 && !StringUtils.isEmpty(comClie.getText())) {
                int ret = JOptionPane.showConfirmDialog(this, "Non hai selezionato un cliente esistente, vuoi creare '" + comClie.getText() + "' come cliente provvisorio ?", "Attenzione", JOptionPane.YES_NO_OPTION);
                if (ret == JOptionPane.YES_OPTION) {
                    int codice = 1;
                    ResultSet r = null;
                    try {
                        r = DbUtils.tryOpenResultSet(Db.getConn(), "select max(codice) from clie_forn");
                        if (r.next()) {
                            codice = r.getInt(1) + 1;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        try {
                            r.getStatement().close();
                        } catch (Exception ex) {
                        }
                    }
                    String sql = "insert into clie_forn (codice, tipo, ragione_sociale) values (" + codice + ", 'P', " + Db.pc(comClie.getText(), Types.VARCHAR) + ")";
                    try {
                        DbUtils.tryExecQuery(Db.getConn(), sql);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    comClie.dbRefreshItems();
                    comClie.dbTrovaKey(String.valueOf(codice));
                    comClie.syncToText();
                }
            } else {
                this.recuperaDatiCliente();
            }
        }
    }//GEN-LAST:event_comClieFocusLost

    private void texClieFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texClieFocusLost

        try {

            if (this.texClie.getText().length() > 0) {
                this.dbdoc.forceCliente(Long.parseLong(this.texClie.getText()));
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }//GEN-LAST:event_texClieFocusLost

    private void texSpeseIncassoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texSpeseIncassoKeyReleased

        try {
            dbdoc.speseIncassoIva = Db.getDouble(this.texSpeseIncasso.getText());
        } catch (Exception err) {
            dbdoc.speseIncassoIva = 0;
        }

        dbdoc.dbRefresh();

        ricalcolaTotali();
    }//GEN-LAST:event_texSpeseIncassoKeyReleased

    private void texScon2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon2KeyReleased

        try {
            dbdoc.sconto2 = Db.getDouble(this.texScon2.getText());
        } catch (Exception err) {

            //err.printStackTrace();
            dbdoc.sconto2 = 0;
        }

        dbdoc.dbRefresh();

        ricalcolaTotali();
    }//GEN-LAST:event_texScon2KeyReleased

    private void texScon1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon1KeyReleased

        try {
            dbdoc.sconto1 = Db.getDouble(this.texScon1.getText());
        } catch (Exception err) {
            dbdoc.sconto1 = 0;
        }

        dbdoc.dbRefresh();

        ricalcolaTotali();
    }//GEN-LAST:event_texScon1KeyReleased

    private void texScon2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon2KeyPressed
    }//GEN-LAST:event_texScon2KeyPressed

    private void texScon1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon1KeyPressed
    }//GEN-LAST:event_texScon1KeyPressed

    private void texScon1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon1KeyTyped
    }//GEN-LAST:event_texScon1KeyTyped

    private void texScon2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texScon2FocusLost
        ricalcolaTotali();
    }//GEN-LAST:event_texScon2FocusLost

    private void texScon1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texScon1FocusLost
        ricalcolaTotali();
        if (main.fileIni.getValueBoolean("pref", "provvigioniAutomatiche", false)) {
            this.comAgenteFocusLost(null);
        }
    }//GEN-LAST:event_texScon1FocusLost

    private void texScon1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texScon1ActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_texScon1ActionPerformed

    private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameOpened
        griglia.resizeColumnsPerc(true);

        if (main.fileIni.getValueBoolean("pref", "ordine_acq_dati_trasporto", false)) {
            popAltriDatiVisualizza.setSelected(true);
        }
        popAltriDatiVisualizzaActionPerformed(null);

        InvoicexUtil.aggiornaSplit(dati, split);
    }//GEN-LAST:event_formInternalFrameOpened

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosed
        InvoicexUtil.removeLock(dati.dbNomeTabella, id, this);

        // Add your handling code here:
        try {
            tim.cancel();
        } catch (Exception e) {
        }
        main.getPadre().closeFrame(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void grigliaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseClicked

        // Add your handling code here:
        try {

            if (evt.getClickCount() == 2) {

                //modifico o la riga o la finestra
                if (main.getPersonalContain("snj") && tipoSNJ != null && (tipoSNJ.equals("A") || tipoSNJ.equals("B"))) {
                    SwingUtils.showInfoMessage(this, "La tua personalizzazione permette l'inserimento solo tramite il foglio righe", "Modifica con foglio righe");
                } else {
                    popGrigModiActionPerformed(null);
                }
            }
        } catch (Exception err) {
            javax.swing.JOptionPane.showMessageDialog(null, err.toString());
            err.printStackTrace();
        }


    }//GEN-LAST:event_grigliaMouseClicked

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

        ricalcolaSubTotaliOrdine();

        griglia.dbRefresh();
        dbdoc.dbRefresh();
        ricalcolaTotali();
        dbAssociaGrigliaRighe();

    }//GEN-LAST:event_popGrigElimActionPerformed
    public void ricalcolaSubTotaliOrdine() {
        try {
            String dbSerie = this.texSeri.getText();
            String dbNumero = this.texNumeOrdine.getText();
            String dbAnno = this.texAnno.getText();

            String query = "SELECT * FROM " + getNomeTabRighe() + " WHERE id_padre = " + id + " ORDER BY riga";
            ResultSet rs = Db.openResultSet(query);

            double subTotale = 0d;
            double subTotaleIvato = 0d;
            while (rs.next()) {
                if (rs.getString("codice_articolo").equals(dbOrdine.CODICE_SUBTOTALE)) {
                    String subTotaleDesc = "";
                    if (!prezzi_ivati.isSelected()) {
                        subTotaleDesc = dbOrdine.DESCRIZIONE_SUBTOTALE + " " + FormatUtils.formatEuroIta(subTotale);
                    } else {
                        subTotaleDesc = dbOrdine.DESCRIZIONE_SUBTOTALE + " " + FormatUtils.formatEuroIta(subTotaleIvato);
                    }
                    String updateQuery = "UPDATE " + getNomeTabRighe() + " SET descrizione = " + Db.pc(subTotaleDesc, Types.VARCHAR);
                    updateQuery += " WHERE id_padre = " + id;
                    updateQuery += " AND riga = " + Db.pc(rs.getInt("riga"), Types.INTEGER);

                    Db.executeSql(updateQuery);
                    subTotale = 0d;
                    subTotaleIvato = 0d;
                } else {
                    double totaleRiga = rs.getDouble("totale_imponibile");
                    subTotale += totaleRiga;
                    double totaleRigaIvato = rs.getDouble("totale_ivato");
                    subTotaleIvato += totaleRigaIvato;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void popGrigModiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popGrigModiActionPerformed
        Integer id_riga = cu.i(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id")));
        Integer id_padre = cu.i(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id_padre")));

        //modifico la riga
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
//            frmNuovRiga frm = new frmNuovRiga(this, this.dati.DB_MODIFICA, this.texSeri.getText(), Integer.valueOf(this.texNumeOrdine.getText()).intValue(), "P", Integer.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), 3).toString()).intValue(), Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente);
//            temp.openFrame(frm, 600, 350, 100, 100);
//            frm.setStato();
//        } else {
        try {
            frmNuovRigaDescrizioneMultiRigaNew frm = new frmNuovRigaDescrizioneMultiRigaNew(this, this.dati.DB_MODIFICA, this.texSeri.getText(), Integer.valueOf(this.texNumeOrdine.getText()).intValue(), "P", Integer.valueOf(this.griglia.getValueAt(griglia.getSelectedRow(), 3).toString()).intValue(), Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente, id_riga, id_padre, getNomeTabRighe());
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

    private void butNuovArtiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNuovArtiActionPerformed

        SwingUtils.mouse_wait(this);

        MicroBench mb = new MicroBench();
        mb.start();

        String codiceListino = "1";

        if (texClie.getText().length() > 0) {
            try {
                codiceListino = Db.lookUp(texClie.getText(), "codice", "clie_forn").getString("codice_listino");
            } catch (Exception err) {
                err.printStackTrace();
            }
        }

        mb.out("listino");

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

        mb.out("multirigao");

//        if (multiriga == false) {
//
//            frmNuovRiga frm = new frmNuovRiga(this, this.dati.DB_INSERIMENTO, this.texSeri.getText(), Integer.valueOf(this.texNumeOrdine.getText()).intValue(), "P", 0, Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente);
//            temp.openFrame(frm, 600, 350, 100, 100);
//            frm.setStato();
//        } else {
        try {
            mb.out("pre");
            frmNuovRigaDescrizioneMultiRigaNew frm = new frmNuovRigaDescrizioneMultiRigaNew(this, this.dati.DB_INSERIMENTO, this.texSeri.getText(), Integer.valueOf(this.texNumeOrdine.getText()).intValue(), "P", 0, Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente, null, this.id, getNomeTabRighe());
            mb.out("post");
            int w = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_w", "980"));
            int h = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_h", "460"));
            int top = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_top", "100"));
            int left = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_left", "100"));
            main.getPadre().openFrame(frm, w, h, top, left);
            mb.out("post open");
            frm.setStato();
            mb.out("post setstato");
            frm.texProvvigione.setText(texProvvigione.getText());
            frm.consegna_prevista.setDate(consegna_prevista.getDate());
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }

        SwingUtils.mouse_def(this);

    }//GEN-LAST:event_butNuovArtiActionPerformed

    private void butUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butUndoActionPerformed
        if (block_aggiornareProvvigioni) {
            return;
        }

        if (evt != null) {
            if (!SwingUtils.showYesNoMessage(main.getPadreWindow(), "Sicuro di annullare le modifiche ?")) {
                return;
            }
        }

        if (!main.edit_doc_in_temp) {
            if (dbStato == this.DB_INSERIMENTO) {
                //elimino la testata inserita e poi annullata
                String sql = "delete from test_ordi" + suff;
                sql += " where serie = " + Db.pc(String.valueOf(this.dbdoc.serie), "VARCHAR");
                sql += " and numero = " + Db.pc(String.valueOf(this.dbdoc.numero), "NUMBER");
                //sql += " and stato = " + Db.pc(String.valueOf(this.ordine.stato), "VARCHAR");
                sql += " and anno = " + Db.pc(String.valueOf(this.dbdoc.anno), "INTEGER");
                Db.executeSql(sql);
                sql = "delete from righ_ordi" + suff;
                sql += " where serie = " + Db.pc(String.valueOf(this.dbdoc.serie), "VARCHAR");
                sql += " and numero = " + Db.pc(String.valueOf(this.dbdoc.numero), "NUMBER");
                //sql += " and stato = " + Db.pc(String.valueOf(this.ordine.stato), "VARCHAR");
                sql += " and anno = " + Db.pc(String.valueOf(this.dbdoc.anno), "VARCHAR");
                Db.executeSql(sql);
            } else if (dbStato == this.DB_MODIFICA) {
                System.out.println("annulla da modifica, elimino " + dbdoc.serie + "/" + dbdoc.numero + "/" + dbdoc.anno + " e rimetto da temp " + serie_originale + "/" + numero_originale + "/" + anno_originale);

                //rimetto numero originale
                sql = "update test_ordi" + suff;
                sql += " set numero = " + Db.pc(numero_originale, "NUMBER");
                sql += " , anno = " + Db.pc(anno_originale, "NUMBER");
                sql += " where id = " + this.id;
                Db.executeSql(sql);

                //elimino le righe inserite per id_padre
                sql = "delete from righ_ordi" + suff;
                sql += " where id_padre = " + this.id;
                Db.executeSql(sql);

                //e rimetto quelle da temp
                /* ATTENZIONE, NON RIMETTERE COME QUI SOTTO, OVVERO SENZA GLI ID ALTRIMENTI SI PERDE IL COLLEGAMENTO CON LE INFO SU LOTTI E MATRICOLE */
                //            sql = "insert into righ_ordi" + suff + " (" + Db.getFieldList("righ_ordi" + suff, false, Arrays.asList("id")) + ")";
                //            sql += " select " + Db.getFieldList("righ_ordi" + suff + "_temp", true, Arrays.asList("id"));
                sql = "insert into righ_ordi" + suff + " (" + Db.getFieldList("righ_ordi" + suff, false) + ")";
                sql += " select " + Db.getFieldList("righ_ordi" + suff + "_temp", true);
                sql += " from righ_ordi" + suff + "_temp";
                sql += " where id_padre = " + this.id;
                sql += " and username = '" + main.login + "'";
                Db.executeSqlDialogExc(sql, true);

                //aggiorno scadenze
                //non serve più si va per id_doc
            }
        } else {
            //non faccio niente, se si annulla rimane tutto come prima
        }

        if (from != null) {
            this.from.dbRefresh();
        }
        this.dispose();
    }//GEN-LAST:event_butUndoActionPerformed

    public void annulla() {
        butUndoActionPerformed(null);
    }

    private void butSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSaveActionPerformed
        if (block_aggiornareProvvigioni) {
            return;
        }

        if (controlloCampi() == true) {
            if (saveDocumento()) {
                main.events.fireInvoicexEvent(new InvoicexEvent(this, InvoicexEvent.TYPE_SAVE));

                if (from != null) {
                    this.from.dbRefresh();
                }

                if (chiudere) {
                    SwingUtils.mouse_def(this);
                    this.dispose();
                } else {
                    //ci pensa allegati util a chiudere
                    System.out.println("aspetto il salvataggio degli allegati");
                }
            }
        }
    }//GEN-LAST:event_butSaveActionPerformed

private void butInserisciPesoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butInserisciPesoActionPerformed
    doc.setPrezziIvati(prezzi_ivati.isSelected());
    doc.setSconto(Db.getDouble(texSconto.getText()));
    doc.calcolaTotali();
    System.out.println("peso:" + doc.totalePeso);

    String dbSerie = this.dbdoc.serie;
    int dbNumero = this.dbdoc.numero;
    int dbAnno = this.dbdoc.anno;
    int riga = 0;

    //apre il resultset per ultimo +1
    Statement stat = null;
    ResultSet resu = null;
    try {
        stat = Db.getConn().createStatement();
        String sql = "select riga from " + getNomeTabRighe();
        sql += " where id_padre = " + id;
        sql += " order by riga desc limit 1";
        resu = stat.executeQuery(sql);
        if (resu.next() == true) {
            riga = resu.getInt(1) + iu.getRigaInc();
        } else {
            riga = 1;
        }
    } catch (Exception err) {
        err.printStackTrace();
    } finally {
        try {
            stat.close();
        } catch (Exception ex1) {
        }
    }

    sql = "insert into " + getNomeTabRighe() + " (serie, numero, anno, riga, codice_articolo, descrizione, id_padre) values (";
    sql += db.pc(dbSerie, "VARCHAR");
    sql += ", " + db.pc(dbNumero, "NUMBER");
    sql += ", " + db.pc(dbAnno, "NUMBER");
    sql += ", " + db.pc(riga, "NUMBER");
    sql += ", ''";
    if (main.getPersonalContain("litri")) {
        sql += ", '" + it.tnx.Util.format2Decimali(doc.totalePeso) + " Litri Totali'";
    } else {
        sql += ", 'Peso totale Kg. " + it.tnx.Util.format2Decimali(doc.totalePeso) + "'";
    }
    sql += ", " + Db.pc(id, Types.INTEGER);
    sql += ")";
    Db.executeSql(sql);

    griglia.dbRefresh();
}//GEN-LAST:event_butInserisciPesoActionPerformed

private void texTipoOrdineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texTipoOrdineActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_texTipoOrdineActionPerformed

private void texNumeOrdineFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texNumeOrdineFocusGained

    old_id = texNumeOrdine.getText();
    id_modificato = false;

}//GEN-LAST:event_texNumeOrdineFocusGained

private void texNumeOrdineFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texNumeOrdineFocusLost
    texNumeOrdine.setText(texNumeOrdine.getText().replaceAll("[^\\d.]", ""));
    if (!old_id.equals(texNumeOrdine.getText())) {
        //controllo che se è un numero già presente non glielo facci ofare percè altrimenti sovrascrive una altra fattura
        sql = "select numero from test_ordi" + suff;
        sql += " where serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
        sql += " and numero " + Db.pcW(texNumeOrdine.getText(), "NUMBER");
        sql += " and anno " + Db.pcW(String.valueOf(this.dbdoc.anno), "VARCHAR");
        ResultSet r = Db.openResultSet(sql);
        try {
            if (r.next()) {
                texNumeOrdine.setText(old_id);
                JOptionPane.showMessageDialog(this, "Non puoi mettere il numero di un documento già presente !", "Attenzione", JOptionPane.INFORMATION_MESSAGE);
                return;
            } else {
                //associo al nuovo numero
                dbdoc.numero = new Integer(this.texNumeOrdine.getText()).intValue();

                if (!main.edit_doc_in_temp) {
                    sql = "update righ_ordi" + suff;
                    sql += " set numero = " + Db.pc(dbdoc.numero, "NUMBER");
                    sql += " where serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
                    sql += " and numero " + Db.pcW(old_id, "NUMBER");
                    sql += " and anno " + Db.pcW(String.valueOf(this.dbdoc.anno), "VARCHAR");
                    Db.executeSql(sql);

                    sql = "update test_ordi" + suff;
                    sql += " set numero = " + Db.pc(dbdoc.numero, "NUMBER");
                    sql += " where serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
                    sql += " and numero " + Db.pcW(old_id, "NUMBER");
                    sql += " and anno " + Db.pcW(String.valueOf(this.dbdoc.anno), "VARCHAR");
                    Db.executeSql(sql);

                    //aggiorno scadenze
                    //non serve più si va per id_doc
                    //riassocio
                    dbAssociaGrigliaRighe();
                    id_modificato = true;

                    dbdoc.numero = Integer.parseInt(texNumeOrdine.getText());
                    doc.load(Db.INSTANCE, this.dbdoc.numero, this.dbdoc.serie, this.dbdoc.anno, acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE, id);
                    ricalcolaTotali();
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

}//GEN-LAST:event_texNumeOrdineFocusLost

private void texDataFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texDataFocusGained

    old_anno = getAnno();
    old_data = texData.getText();
    old_id = texNumeOrdine.getText();
    anno_modificato = false;

}//GEN-LAST:event_texDataFocusGained

private void texBancIbanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texBancIbanActionPerformed
}//GEN-LAST:event_texBancIbanActionPerformed

private void texBancIbanFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texBancIbanFocusLost
}//GEN-LAST:event_texBancIbanFocusLost

private void comClieFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comClieFocusGained
    comClieSel_old = comClie.getSelectedIndex();
}//GEN-LAST:event_comClieFocusGained

private void comClieDestFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comClieDestFocusGained
    comClieDest_old = comClieDest.getSelectedIndex();
}//GEN-LAST:event_comClieDestFocusGained

private void popGrigAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popGrigAddActionPerformed
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
//
//        frmNuovRiga frm = new frmNuovRiga(this, this.dati.DB_INSERIMENTO, this.texSeri.getText(), Integer.valueOf(this.texNumeOrdine.getText()).intValue(), "P", value, Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente);
//        temp.openFrame(frm, 600, 350, 100, 100);
//        frm.setStato();
//    } else {
    try {
        frmNuovRigaDescrizioneMultiRigaNew frm = new frmNuovRigaDescrizioneMultiRigaNew(this, this.dati.DB_INSERIMENTO, this.texSeri.getText(), Integer.valueOf(this.texNumeOrdine.getText()).intValue(), "P", value, Integer.valueOf(this.texAnno.getText()).intValue(), codiceListino, codiceCliente, id_riga, id_padre, getNomeTabRighe());
        int w = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_w", "980"));
        int h = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_h", "460"));
        int top = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_top", "100"));
        int left = Integer.parseInt(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_left", "100"));
        main.getPadre().openFrame(frm, w, h, top, left);
        frm.setStato();
        frm.texProvvigione.setText(texProvvigione.getText());
    } catch (Exception e) {
        e.printStackTrace();
    }
//    }
}//GEN-LAST:event_popGrigAddActionPerformed

private void formVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_formVetoableChange

    boolean tr = false;
    if (evt.getPropertyName().equals(IS_CLOSED_PROPERTY)) {
        boolean changed = ((Boolean) evt.getNewValue()).booleanValue();
        if (changed) {
            try {
                if (dati.dbCheckModificati()
                        || (doc.getTotale_da_pagare_finale() != totaleDaPagareFinaleIniziale
                        || !this.pagamentoInizialeGiorno.equals(this.texGiornoPagamento.getText())
                        || !this.pagamentoIniziale.equals(this.comPaga.getText())
                        || !data_originale.equalsIgnoreCase(texData.getText()))) {
                    FxUtils.fadeBackground(butSave, Color.RED);
                    int confirm = JOptionPane.showOptionDialog(this,
                            "<html><b>Chiudi " + getTitle() + "?</b><br>Se hai fatto delle modifiche e verranno <b>perse</b> !<br>Per salvarle devi cliccare sul pulsante <b>Salva</b><br>",
                            "Conferma chiusura",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, null, null);
                    if (confirm == 0) {
                    } else {
                        tr = true;
                    }
                }
            } catch (Exception e) {
            }

            if (tr) {
                throw new PropertyVetoException("Cancelled", null);
            } else {
//                if (dbStato.equalsIgnoreCase(this.DB_INSERIMENTO)) {
                butUndoActionPerformed(null);
//                }
            }
        }

    }
}//GEN-LAST:event_formVetoableChange

private void texForniFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texForniFocusLost
    //this.texForni1.setText(texForni.getText());
}//GEN-LAST:event_texForniFocusLost

private void grigliaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMousePressed
    if (evt.isPopupTrigger()) {
        iu.impostaRigaSopraSotto(griglia, popGrigAdd, popGrig, evt, popGrigAddSub);
    }
}//GEN-LAST:event_grigliaMousePressed

private void grigliaMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_grigliaMouseReleased
    if (evt.isPopupTrigger()) {
        iu.impostaRigaSopraSotto(griglia, popGrigAdd, popGrig, evt, popGrigAddSub);
    }
}//GEN-LAST:event_grigliaMouseReleased

private void butImportRigheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butImportRigheActionPerformed
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    JFileChooser fileChoose = new JFileChooser(new File(System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "Export" + File.separator));
    FileFilter filter1 = new FileFilter() {

        @Override
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
            int numero = Integer.valueOf(this.texNumeOrdine.getText()).intValue();
            int anno = Integer.valueOf(this.texAnno.getText()).intValue();
            int idPadre = this.id;
            InvoicexUtil.importCSV(acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE, f, serie, numero, anno, idPadre, nomeListino);
            InvoicexUtil.aggiornaTotaliRighe(acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE, idPadre, prezzi_ivati_virtual.isSelected());
            griglia.dbRefresh();
            ricalcolaTotali();
            JOptionPane.showMessageDialog(this, "Righe caricate correttamente", "Esecuzione terminata", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_butImportRigheActionPerformed

private void texScon3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texScon3FocusLost
    ricalcolaTotali();
}//GEN-LAST:event_texScon3FocusLost

private void texSpeseTrasportoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texSpeseTrasportoFocusLost
    ricalcolaTotali();
}//GEN-LAST:event_texSpeseTrasportoFocusLost

private void texSpeseIncassoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texSpeseIncassoFocusLost
    ricalcolaTotali();
}//GEN-LAST:event_texSpeseIncassoFocusLost

private void texProvvigioneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texProvvigioneFocusGained
    texProvvigione.setName(texProvvigione.getText());
}//GEN-LAST:event_texProvvigioneFocusGained

private void texProvvigioneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texProvvigioneFocusLost
    if (!StringUtils.equals(texProvvigione.getName(), texProvvigione.getText()) && griglia.getRowCount() > 0 && !acquisto) {
        aggiornareProvvigioni();
    }
}//GEN-LAST:event_texProvvigioneFocusLost

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
                sql = "SELECT MAX(riga) as maxnum FROM " + getNomeTabRighe() + " WHERE id_padre = " + id;

                ResultSet tempUltimo = Db.openResultSet(sql);
                if (tempUltimo.next() == true) {
                    newNumero = tempUltimo.getInt("maxnum") + InvoicexUtil.getRigaInc();
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

            sql = "select * from " + getNomeTabRighe() + " where id = " + Db.pc(dbId, Types.INTEGER);
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
                    sql = "insert into " + getNomeTabRighe() + " ";
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

private void foglioTipoAMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_foglioTipoAMouseClicked
}//GEN-LAST:event_foglioTipoAMouseClicked

private void foglioTipoAMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_foglioTipoAMousePressed
    if (evt.isPopupTrigger()) {
        popFoglio.show(foglioTipoA, evt.getX(), evt.getY());
    }
}//GEN-LAST:event_foglioTipoAMousePressed

private void foglioTipoAMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_foglioTipoAMouseReleased
    if (evt.isPopupTrigger()) {
        popFoglio.show(foglioTipoA, evt.getX(), evt.getY());
    }
}//GEN-LAST:event_foglioTipoAMouseReleased

private void panGrigliaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_panGrigliaFocusGained
    // TODO add your handling code here:
}//GEN-LAST:event_panGrigliaFocusGained

private void panFoglioRigheSNJAFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_panFoglioRigheSNJAFocusGained
}//GEN-LAST:event_panFoglioRigheSNJAFocusGained

private void popFoglioEliminaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popFoglioEliminaActionPerformed

    DefaultTableModel tableModel = (DefaultTableModel) foglioTipoA.getModel();
    tableModel.removeRow(foglioTipoA.getSelectedRow());
}//GEN-LAST:event_popFoglioEliminaActionPerformed

private void foglioTipoBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_foglioTipoBMouseClicked
    // TODO add your handling code here:
}//GEN-LAST:event_foglioTipoBMouseClicked

private void foglioTipoBMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_foglioTipoBMousePressed
    // TODO add your handling code here:
}//GEN-LAST:event_foglioTipoBMousePressed

private void foglioTipoBMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_foglioTipoBMouseReleased
    // TODO add your handling code here:
}//GEN-LAST:event_foglioTipoBMouseReleased

private void panGriglia1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_panGriglia1FocusGained
    // TODO add your handling code here:
}//GEN-LAST:event_panGriglia1FocusGained

private void panFoglioRigheSNJBFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_panFoglioRigheSNJBFocusGained
    // TODO add your handling code here:
}//GEN-LAST:event_panFoglioRigheSNJBFocusGained

private void panTabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_panTabStateChanged
    if (StringUtils.isEmpty(tipoSNJ) || acquisto) {
        return;
    }

    if (panTab.getSelectedIndex() == 1) {

        if (tipoSNJ != null && tipoSNJ.equals("A")) {
            //visualizzo il pannellino per lengthdescrizioni multiriga
            zoomA.setVisible(true);

            //associo il nuovo foglio
            ResultSet resu = Db.openResultSet(sqlGrigliaA);
            loadingFoglio = true;

            int rowCount = 0;

            try {
                while (resu.next()) {
                    foglioDataA.setValueAt(resu.getString(1), rowCount, 0);
                    foglioDataA.setValueAt(resu.getString(2), rowCount, 1);

                    foglioDataA.setValueAt(resu.getString(3), rowCount, 2); //descrizione

//                    foglioDataA.setValueAt(FormatUtils.formatNum0_5Dec(resu.getDouble(4)), rowCount, 3); //qta
//                    foglioDataA.setValueAt(Db.formatValuta(resu.getDouble(6)), rowCount, 5); //prezzo
                    foglioDataA.setValueAt(resu.getDouble(4), rowCount, 3); //qta
                    foglioDataA.setValueAt(resu.getDouble(6), rowCount, 5); //prezzo

                    HashMap val = new HashMap();
                    val.put("k", resu.getString(5));
                    val.put("d", resu.getString(5));
                    foglioDataA.setValueAt(val, rowCount, 4);

                    val = new HashMap();
                    val.put("k", resu.getString(7));
                    val.put("d", resu.getString(7));
                    foglioDataA.setValueAt(val, rowCount, 6);
                    val = new HashMap();
                    val.put("k", resu.getString(8));
                    val.put("d", resu.getString(9));
                    foglioDataA.setValueAt(val, rowCount, 7);
                    val = new HashMap();
                    val.put("k", resu.getString(10));
                    val.put("d", resu.getString(11));
                    foglioDataA.setValueAt(val, rowCount, 8);
                    foglioDataA.setValueAt(Db.formatValuta(resu.getDouble(12)), rowCount, 9);
                    //id riga
                    foglioDataA.setValueAt(resu.getInt("id"), rowCount, 10);
                    rowCount++;
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

            loadingFoglio = false;
            ricalcolaTotali();
        } else {
            //visualizzo il pannellino per lengthdescrizioni multiriga
            zoomB.setVisible(true);
            //associo il nuovo foglio
            ResultSet resu = Db.openResultSet(sqlGrigliaB);
            loadingFoglio = true;
            int rowCount = 0;

            try {
                while (resu.next()) {
                    foglioDataB.setValueAt(resu.getString(1), rowCount, 0);
                    foglioDataB.setValueAt(resu.getString(2), rowCount, 1);
                    foglioDataB.setValueAt(resu.getString(3), rowCount, 2);

                    foglioDataB.setValueAt(resu.getObject(4), rowCount, 3);
                    foglioDataB.setValueAt(resu.getObject(5), rowCount, 4);

                    HashMap val = new HashMap();
                    val.put("k", resu.getString(6));
                    val.put("d", resu.getString(7));
                    foglioDataB.setValueAt(val, rowCount, 5);
                    val = new HashMap();
                    val.put("k", resu.getString(8));
                    val.put("d", resu.getString(9));
                    foglioDataB.setValueAt(val, rowCount, 6);
                    val = new HashMap();
                    val.put("k", resu.getString(10));
                    val.put("d", resu.getString(11));
                    foglioDataB.setValueAt(val, rowCount, 7);
                    val = new HashMap();
                    val.put("k", resu.getString(12));
                    val.put("d", resu.getString(13));
                    foglioDataB.setValueAt(val, rowCount, 8);
                    foglioDataB.setValueAt(Db.formatValuta(resu.getDouble(14)), rowCount, 9);

                    //id riga
                    foglioDataB.setValueAt(resu.getInt("id"), rowCount, 10);

                    rowCount++;
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

            loadingFoglio = false;
            ricalcolaTotali();
        }
    }
}//GEN-LAST:event_panTabStateChanged

private void prezzi_ivati_virtualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prezzi_ivati_virtualActionPerformed
    prezzi_ivati.setSelected(prezzi_ivati_virtual.isSelected());
    InvoicexUtil.aggiornaTotaliRighe(Db.TIPO_DOCUMENTO_FATTURA, this.id, prezzi_ivati_virtual.isSelected());
    ricalcolaTotali();
    ricalcolaSubTotaliOrdine();
    dbAssociaGrigliaRighe();
}//GEN-LAST:event_prezzi_ivati_virtualActionPerformed

private void butInserisciSubTotaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butInserisciSubTotaleActionPerformed
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    int id_padre = 0;
    try {
        id_padre = (Integer) griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("id_padre"));
    } catch (Exception e) {
        return;
    }

    int riga = 1;
    double subTotale = 0d;
    try {
        riga = CastUtils.toInteger(DbUtils.getObject(Db.getConn(), "SELECT MAX(riga) + " + iu.getRigaInc() + " FROM " + getNomeTabRighe() + " WHERE id_padre = " + id_padre));
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    try {
        String query = "INSERT INTO " + getNomeTabRighe() + " SET ";
        query += "serie = " + Db.pc(texSeri.getText(), Types.VARCHAR) + ", ";
        query += "numero = " + Db.pc(texNumeOrdine.getText(), Types.INTEGER) + ", ";
        query += "anno = " + Db.pc(texAnno.getText(), Types.INTEGER) + ", ";
        query += "riga = " + Db.pc(riga, Types.INTEGER) + ", ";
        query += "codice_articolo = " + Db.pc(dbOrdine.CODICE_SUBTOTALE, Types.VARCHAR) + ", ";
        query += "descrizione = " + Db.pc(dbOrdine.DESCRIZIONE_SUBTOTALE + " " + FormatUtils.formatEuroIta(subTotale), Types.VARCHAR) + ", ";
        query += "quantita = " + Db.pc(1, Types.DECIMAL) + ", ";
        query += "prezzo = " + Db.pc(0, Types.DECIMAL) + ", ";
        query += "iva = " + Db.pc("", Types.VARCHAR) + ", ";
        query += "sconto1 = " + Db.pc(0, Types.DECIMAL) + ", ";
        query += "sconto2 = " + Db.pc(0, Types.DECIMAL) + ", ";
        query += "is_descrizione = " + Db.pc("S", Types.VARCHAR) + ", ";
        query += "id_padre = " + Db.pc(id_padre, Types.INTEGER) + ", ";
        query += "totale_ivato = " + Db.pc(0, Types.DECIMAL) + ", ";
        query += "totale_imponibile = " + Db.pc(0, Types.DECIMAL);
        Db.executeSql(query);
    } catch (Exception e) {
        e.printStackTrace();
    }

    ricalcolaTotali();
    ricalcolaSubTotaliOrdine();
    dbAssociaGrigliaRighe();
    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

}//GEN-LAST:event_butInserisciSubTotaleActionPerformed

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

private void popGrigAddSubActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popGrigAddSubActionPerformed
    int numCol = griglia.getColumnByName("riga");
    int numRiga = griglia.getSelectedRow();
    int riga = (Integer) griglia.getValueAt(numRiga, numCol);
    int id_padre = (Integer) griglia.getValueAt(numRiga, griglia.getColumnByName("id_padre"));

    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    //sposto le righe sotto
    try {
        String sql = "update " + getNomeTabRighe() + " set riga = riga+1 where riga >= " + riga;
        sql += " and id_padre = " + id_padre;
        sql += " order by riga DESC";
        Db.executeSql(sql);

        //inserisco la riga
        String query = "INSERT INTO " + getNomeTabRighe() + " SET ";
        query += "serie = " + Db.pc(texSeri.getText(), Types.VARCHAR) + ", ";
        query += "numero = " + Db.pc(texNumeOrdine.getText(), Types.INTEGER) + ", ";
        query += "anno = " + Db.pc(texAnno.getText(), Types.INTEGER) + ", ";
        query += "riga = " + Db.pc(riga, Types.INTEGER) + ", ";
        query += "codice_articolo = " + Db.pc(dbOrdine.CODICE_SUBTOTALE, Types.VARCHAR) + ", ";
        query += "descrizione = '...',";
        query += "quantita = " + Db.pc(1, Types.DECIMAL) + ", ";
        query += "prezzo = " + Db.pc(0, Types.DECIMAL) + ", ";
        query += "iva = " + Db.pc("", Types.VARCHAR) + ", ";
        query += "sconto1 = " + Db.pc(0, Types.DECIMAL) + ", ";
        query += "sconto2 = " + Db.pc(0, Types.DECIMAL) + ", ";
        query += "is_descrizione = " + Db.pc("S", Types.VARCHAR) + ", ";
        query += "id_padre = " + Db.pc(id_padre, Types.INTEGER) + ", ";
        query += "totale_ivato = " + Db.pc(0, Types.DECIMAL) + ", ";
        query += "totale_imponibile = " + Db.pc(0, Types.DECIMAL);

        Db.executeSql(query);
    } catch (Exception e) {
        e.printStackTrace();
    }

    ricalcolaTotali();
    ricalcolaSubTotaliOrdine();
    dbAssociaGrigliaRighe();

    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

}//GEN-LAST:event_popGrigAddSubActionPerformed

private void butImportRigheProskinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butImportRigheProskinActionPerformed
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    //new File(System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "Export" + File.separator)
    String pathprec = main.fileIni.getValue("proskin", "path_prec", null);
    JFileChooser fileChoose = new JFileChooser(pathprec);
    FileFilter filter1 = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            if (pathname.getAbsolutePath().endsWith(".xls")) {
                return true;
            } else if (pathname.isDirectory()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return "File Excel (*.xls)";
        }
    };

    fileChoose.addChoosableFileFilter(filter1);
    fileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);

    int ret = fileChoose.showOpenDialog(this);

    if (ret == javax.swing.JFileChooser.APPROVE_OPTION) {
        try {
            //apro il file
            File f = fileChoose.getSelectedFile();
            try {
                main.fileIni.setValue("proskin", "path_prec", f.getParent());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            String serie = texSeri.getText();
            int numero = Integer.valueOf(this.texNumeOrdine.getText()).intValue();
            int anno = Integer.valueOf(this.texAnno.getText()).intValue();
            int idPadre = 0;
            String sql = "SELECT id FROM test_ordi" + suff + " WHERE serie = '" + serie + "' AND numero = (" + numero + ") AND anno = (" + anno + ")";
            ResultSet testa = Db.openResultSet(sql);
            if (testa.next()) {
                idPadre = testa.getInt("id");
            } else {
                idPadre = 1;
            }
            InvoicexUtil.importXls(acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE, f, serie, numero, anno, idPadre);
            griglia.dbRefresh();
            ricalcolaTotali();
            JOptionPane.showMessageDialog(this, "Righe caricate correttamente", "Esecuzione terminata", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_butImportRigheProskinActionPerformed

    private void consegna_previstaPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_consegna_previstaPropertyChange
        if (!loading && evt.getPropertyName().equals("date")) {
            if (griglia.getRowCount() > 0) {
                int ret = JOptionPane.showInternalConfirmDialog(this, "Vuoi impostare la nuova scadenza del '" + DateUtils.formatDateIta(cu.toDate(evt.getNewValue())) + "' in tutte le righe del documento ?", "Attenzione", JOptionPane.YES_NO_OPTION);
                if (ret == JOptionPane.YES_OPTION) {
                    System.out.println("imposto scadenza");
                    int id = acquisto ? InvoicexUtil.getIdOrdineAcquisto(dbdoc.serie, dbdoc.numero, dbdoc.anno) : InvoicexUtil.getIdOrdine(dbdoc.serie, dbdoc.numero, dbdoc.anno);
                    String sql = "update " + getNomeTabRighe() + " set data_consegna_prevista = " + Db.pc(cu.toDate(evt.getNewValue()), Types.DATE) + " where id_padre = " + id;
                    try {
                        DbUtils.tryExecQuery(Db.getConn(), sql);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            dbAssociaGrigliaRighe();
        }
    }//GEN-LAST:event_consegna_previstaPropertyChange

    private void comConsegnaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comConsegnaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comConsegnaActionPerformed

    private void comScaricoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comScaricoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comScaricoActionPerformed

    private void apriclientiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apriclientiActionPerformed
        //    alRicercaCliente.showHints();
        if (texCliente.getText().trim().length() == 0) {
            al_clifor.showHints2();
            al_clifor.updateHints(null);
            al_clifor.showHints2();
        } else {
            al_clifor.showHints();
        }
        //    al_clifor.showHints();
    }//GEN-LAST:event_apriclientiActionPerformed

    private void butPdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPdfActionPerformed
        butStampaActionPerformed(new ActionEvent(this, 0, "pdf"));
    }//GEN-LAST:event_butPdfActionPerformed

    private void butImportXlsCirriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butImportXlsCirriActionPerformed
        InvoicexUtil.importRigheXlsCirri(this);
    }//GEN-LAST:event_butImportXlsCirriActionPerformed

    private void texGiornoPagamentoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texGiornoPagamentoFocusLost

    }//GEN-LAST:event_texGiornoPagamentoFocusLost

    private void texAccontoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texAccontoKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_texAccontoKeyPressed

    private void texAccontoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texAccontoKeyReleased
        double valore = CastUtils.toDouble0(texAcconto.getText());
        if (valore < 0) {
            valore = Math.abs(valore);
            texAcconto.setText(FormatUtils.formatEuroIta(valore));
        }
        ricalcolaTotali();
    }//GEN-LAST:event_texAccontoKeyReleased

    private void texClienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_texClienteMouseClicked


    }//GEN-LAST:event_texClienteMouseClicked

    private void texClienteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_texClienteMouseEntered

    }//GEN-LAST:event_texClienteMouseEntered

    private void texClienteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_texClienteMouseExited

    }//GEN-LAST:event_texClienteMouseExited

    private void comStatoOrdineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comStatoOrdineItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            aggiornaNote();
        }
    }//GEN-LAST:event_comStatoOrdineItemStateChanged

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

    private void menClienteControllaFido2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menClienteControllaFido2ActionPerformed
        forza_vis_fido = menClienteControllaFido2.isSelected();
        aggiornaFido();
    }//GEN-LAST:event_menClienteControllaFido2ActionPerformed

    private void comContattoRiferimentoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comContattoRiferimentoFocusGained

    }//GEN-LAST:event_comContattoRiferimentoFocusGained

    private void comContattoRiferimentoPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_comContattoRiferimentoPopupMenuWillBecomeVisible
        if (comContattoRiferimento.getItemCount() == 0) {
            caricaComboContatti();
        }
    }//GEN-LAST:event_comContattoRiferimentoPopupMenuWillBecomeVisible

    private void popAltriDatiVisualizzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popAltriDatiVisualizzaActionPerformed
        if (popAltriDatiVisualizza.isSelected() || main.getPersonalContain("dest_diversa_ordini_fornitori")) {
            dati_altri2.setVisible(true);
//            split.setDividerLocation(dati_altri2.getHeight() + dati_altri2.getY() + 4);
            InvoicexUtil.aggiornaSplit(dati, split);
            main.fileIni.setValue("pref", "ordine_acq_dati_trasporto", true);
        } else if (acquisto) {
            dati_altri2.setVisible(false);
//                split.setDividerLocation(170);
            InvoicexUtil.aggiornaSplit(dati, split);
            main.fileIni.setValue("pref", "ordine_acq_dati_trasporto", false);
        }
    }//GEN-LAST:event_popAltriDatiVisualizzaActionPerformed

    private void datiMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_datiMousePressed
        if (evt.isPopupTrigger()) {
            if (acquisto) {
                popAltriDati.show(dati, evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_datiMousePressed

    private void datiMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_datiMouseReleased
        if (evt.isPopupTrigger()) {
            if (acquisto) {
                popAltriDati.show(dati, evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_datiMouseReleased

    private void popStatoOrdineDefRendiDefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popStatoOrdineDefRendiDefActionPerformed
        String sql = "update dati_azienda set " + getNomeCampoStatoDef() + " = " + dbu.sql(cu.s(comStatoOrdine.getText()));
        try {
            dbu.tryExecQuery(Db.getConn(), sql, false);
        } catch (Exception ex) {
            SwingUtils.showExceptionMessage(this, ex);
        }
    }//GEN-LAST:event_popStatoOrdineDefRendiDefActionPerformed

    private void comStatoOrdineMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_comStatoOrdineMousePressed
        //vedi init comp
    }//GEN-LAST:event_comStatoOrdineMousePressed

    private void comStatoOrdineMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_comStatoOrdineMouseReleased
        //vedi init comps
    }//GEN-LAST:event_comStatoOrdineMouseReleased

    private void comCampoLibero1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_comCampoLibero1FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_comCampoLibero1FocusGained

    private void comCampoLibero1PopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_comCampoLibero1PopupMenuWillBecomeVisible
        if (comCampoLibero1.getItemCount() == 0) {
            InvoicexUtil.caricaComboTestateCampoLibero1(comCampoLibero1);
        }
    }//GEN-LAST:event_comCampoLibero1PopupMenuWillBecomeVisible

    private void comPortoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comPortoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comPortoActionPerformed

    private void texFornitoreIntelliMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_texFornitoreIntelliMousePressed
        if (evt.isPopupTrigger()) {
            menClientePopup.show(texCliente, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_texFornitoreIntelliMousePressed

    private void texFornitoreIntelliMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_texFornitoreIntelliMouseReleased
        if (evt.isPopupTrigger()) {
            menClientePopup.show(texCliente, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_texFornitoreIntelliMouseReleased

    private void apriFornitoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apriFornitoriActionPerformed
        if (texFornitoreIntelli.getText().trim().length() == 0) {
            al_for.showHints2();
            al_for.updateHints(null);
            al_for.showHints2();
        } else {
            al_for.showHints();
        }
    }//GEN-LAST:event_apriFornitoriActionPerformed

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
    private javax.swing.JButton apriFornitori;
    private javax.swing.JButton apriclienti;
    private javax.swing.JButton butAddClie;
    private javax.swing.JButton butCoor;
    private javax.swing.JButton butImportRighe;
    private javax.swing.JButton butImportRigheProskin;
    private javax.swing.JButton butImportXlsCirri;
    private javax.swing.JButton butInserisciPeso;
    private javax.swing.JButton butInserisciSubTotale;
    public javax.swing.JButton butNuovArti;
    private javax.swing.JButton butPdf;
    private javax.swing.JButton butPrezziPrec;
    private javax.swing.JButton butSave;
    private javax.swing.JButton butStampa;
    public javax.swing.JButton butUndo;
    private tnxbeans.tnxCheckBox cheOpzioneRibaDestDiversa;
    private tnxbeans.tnxComboField comAgente;
    private tnxbeans.tnxComboField comAspettoEsterioreBeni;
    private tnxbeans.tnxComboField comCampoLibero1;
    private tnxbeans.tnxComboField comCausaleTrasporto;
    public tnxbeans.tnxComboField comClie;
    private tnxbeans.tnxComboField comClieDest;
    private tnxbeans.tnxComboField comConsegna;
    private tnxbeans.tnxComboField comContattoRiferimento;
    private tnxbeans.tnxComboField comMezzoTrasporto;
    private tnxbeans.tnxComboField comPaese;
    private tnxbeans.tnxComboField comPaga;
    private tnxbeans.tnxComboField comPorto;
    private tnxbeans.tnxComboField comScarico;
    public tnxbeans.tnxComboField comStatoOrdine;
    private tnxbeans.tnxComboField comVettori;
    private org.jdesktop.swingx.JXDatePicker consegna_prevista;
    public tnxbeans.tnxDbPanel dati;
    public tnxbeans.tnxDbPanel datiAltro;
    private tnxbeans.tnxDbPanel datiRighe;
    private tnxbeans.tnxDbPanel dati_altri1;
    private tnxbeans.tnxDbPanel dati_altri2;
    public it.tnx.gui.JTableSs foglioTipoA;
    private it.tnx.gui.JTableSs foglioTipoB;
    public tnxbeans.tnxDbGrid griglia;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel113;
    private javax.swing.JLabel jLabel114;
    private javax.swing.JLabel jLabel115;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel151;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labAgente;
    private javax.swing.JLabel labBancAbi;
    private javax.swing.JLabel labBancCab;
    private javax.swing.JLabel labCampoLibero1;
    private javax.swing.JLabel labDDCap;
    private javax.swing.JLabel labDDCel;
    private javax.swing.JLabel labDDIndirizzo;
    private javax.swing.JLabel labDDLoc;
    private javax.swing.JLabel labDDPaese;
    private javax.swing.JLabel labDDProv;
    private javax.swing.JLabel labDDRagSoc;
    private javax.swing.JLabel labDDTel;
    private javax.swing.JLabel labDestDiversa;
    private javax.swing.JLabel labFa1;
    private javax.swing.JLabel labFa2;
    private javax.swing.JLabel labFa3;
    private javax.swing.JLabel labFa4;
    private javax.swing.JLabel labFa5;
    private javax.swing.JLabel labFa6;
    private javax.swing.JLabel labFaTitolo;
    private javax.swing.JLabel labGiornoPagamento;
    private javax.swing.JLabel labModConsegna;
    private javax.swing.JLabel labModScarico;
    private javax.swing.JLabel labNoteConsegna;
    private javax.swing.JLabel labPercentoProvvigione;
    private javax.swing.JLabel labProvvigione;
    private javax.swing.JLabel labRiferimento;
    private javax.swing.JLabel labScon1;
    private javax.swing.JLabel labScon2;
    private javax.swing.JLabel labScon21;
    public javax.swing.JLabel labStatus;
    public javax.swing.JLabel labStatus1;
    private javax.swing.JCheckBoxMenuItem menClienteControllaFido2;
    private javax.swing.JMenuItem menClienteModifica;
    private javax.swing.JMenuItem menClienteNuovo;
    public javax.swing.JPopupMenu menClientePopup;
    private javax.swing.JMenu menColAgg;
    private javax.swing.JCheckBoxMenuItem menColAggNote;
    private javax.swing.JPanel panDati;
    private javax.swing.JPanel panFoglioRigheSNJA;
    private javax.swing.JPanel panFoglioRigheSNJB;
    private javax.swing.JPanel panGriglia;
    private javax.swing.JPanel panGriglia1;
    public javax.swing.JTabbedPane panTab;
    private javax.swing.JPanel panTotale;
    private javax.swing.JPanel panTotale1;
    public javax.swing.JPanel pan_segnaposto_deposito;
    private javax.swing.JPopupMenu popAltriDati;
    private javax.swing.JCheckBoxMenuItem popAltriDatiVisualizza;
    private javax.swing.JMenuItem popDuplicaRighe;
    private javax.swing.JPopupMenu popFoglio;
    private javax.swing.JMenuItem popFoglioElimina;
    private javax.swing.JPopupMenu popGrig;
    private javax.swing.JMenuItem popGrigAdd;
    private javax.swing.JMenuItem popGrigAddSub;
    private javax.swing.JMenuItem popGrigElim;
    private javax.swing.JMenuItem popGrigModi;
    private javax.swing.JPopupMenu popStatoOrdineDef;
    private javax.swing.JMenuItem popStatoOrdineDefRendiDef;
    public tnxbeans.tnxCheckBox prezzi_ivati;
    private javax.swing.JCheckBox prezzi_ivati_virtual;
    private javax.swing.JSeparator sepDestMerce;
    private javax.swing.JSeparator sepFattAcc;
    private javax.swing.JSplitPane split;
    private javax.swing.JComboBox stato_evasione;
    public tnxbeans.tnxTextField texAcconto;
    public tnxbeans.tnxTextField texAnno;
    private tnxbeans.tnxTextField texBancAbi;
    private tnxbeans.tnxTextField texBancCab;
    private tnxbeans.tnxTextField texBancIban;
    public tnxbeans.tnxTextField texClie;
    private tnxbeans.tnxTextField texClieDest;
    public javax.swing.JTextField texCliente;
    private tnxbeans.tnxTextField texConsegna;
    private tnxbeans.tnxTextField texData;
    private tnxbeans.tnxTextField texDestCap;
    private tnxbeans.tnxTextField texDestCellulare;
    private tnxbeans.tnxTextField texDestIndirizzo;
    private tnxbeans.tnxTextField texDestLocalita;
    private tnxbeans.tnxTextField texDestProvincia;
    private tnxbeans.tnxTextField texDestRagioneSociale;
    private tnxbeans.tnxTextField texDestTelefono;
    private tnxbeans.tnxTextField texForni;
    public javax.swing.JTextField texFornitoreIntelli;
    public tnxbeans.tnxTextField texGiornoPagamento;
    public tnxbeans.tnxMemoField texNote;
    private tnxbeans.tnxMemoField texNoteConsegna;
    private tnxbeans.tnxTextField texNotePagamento;
    public tnxbeans.tnxTextField texNumeOrdine;
    private tnxbeans.tnxTextField texNumeroColli;
    private tnxbeans.tnxTextField texPaga2;
    private tnxbeans.tnxTextField texProvvigione;
    public tnxbeans.tnxTextField texScon1;
    public tnxbeans.tnxTextField texScon2;
    public tnxbeans.tnxTextField texScon3;
    public tnxbeans.tnxTextField texSconto;
    public tnxbeans.tnxTextField texSeri;
    public tnxbeans.tnxTextField texSpeseIncasso;
    public tnxbeans.tnxTextField texSpeseTrasporto;
    private tnxbeans.tnxTextField texStat;
    private tnxbeans.tnxTextField texTipoOrdine;
    public tnxbeans.tnxTextField texTota;
    private tnxbeans.tnxTextField texTota1;
    public tnxbeans.tnxTextField texTotaDaPagareFinale;
    public tnxbeans.tnxTextField texTotaImpo;
    private tnxbeans.tnxTextField texTotaImpo1;
    public tnxbeans.tnxTextField texTotaIva;
    private tnxbeans.tnxTextField texTotaIva1;
    private tnxbeans.tnxTextField textTipoSnj;
    // End of variables declaration//GEN-END:variables

    public void dbAssociaGrigliaRighe() {

        if (main.fileIni.getValueBoolean("pref", "ColAgg_righe_note", false)) {
            menColAggNote.setSelected(true);
        }

        String campi = "serie,";
        campi += "numero,";
        campi += "anno,";
        campi += "riga,";
        campi += "stato,";
        campi += "codice_articolo as articolo,";
        campi += "descrizione,";
        campi += "um,";

        campi += "quantita,";
        campi += "quantita_evasa AS '" + getCampoQtaEvasa() + "',";

        campi += "prezzo, ";
        campi += "sconto1 as Sconti, ";
        campi += "sconto2, ";

        campi += " (totale_imponibile) as Totale ";
        campi += ", iva ";
        campi += ", (totale_ivato) as Ivato ";
//        campi += "(prezzo*quantita) - ((prezzo*quantita)*sconto1/100) - ( ((prezzo*quantita) - ((prezzo*quantita)*sconto1/100)) * sconto2 / 100) as Totale ";
        campi += ", r.id";
        campi += ", r.id_padre";
        if (visConsegnaPrevista()) {
            campi += ", data_consegna_prevista as consegna";
            campi += ", flag_consegnato as consegnato";
        }
        if (main.isPluginContabilitaAttivo()) {
            campi += ", conto";
        }

        if (main.fileIni.getValueBoolean("pref", "ColAgg_righe_note", false)) {
            campi += ", n.note";
        }

        String sql = "select " + campi + " from " + getNomeTabRighe() + " r ";
        if (main.fileIni.getValueBoolean("pref", "ColAgg_righe_note", false)) {
            sql += " left join note n on n.tabella = '" + getNomeTabRighe() + "' and n.id_tab = r.id";
        }
        sql += " where r.id_padre = " + id + " order by riga";

        //debug
        //javax.swing.JOptionPane.showMessageDialog(null,sql);
        System.out.println("sql associa griglia:" + sql);

        this.sqlGrigliaA = "SELECT riga, codice_articolo, rig.descrizione, quantita, um, prezzo, percentuale, tef.id as tef_id, tef.descrizione as emissione_fattura, pag.codice as id_pag, pag.descrizione as termini_pagamento, totale_imponibile, rig.id ";
        this.sqlGrigliaA += "FROM " + getNomeTabRighe() + " rig LEFT JOIN tipi_emissione_fattura tef ON rig.emissione_fattura = tef.id LEFT JOIN pagamenti pag ON rig.termini_pagamento = pag.codice";
        this.sqlGrigliaA += " where serie = " + db.pc(this.dbdoc.serie, "VARCHAR") + " and numero = " + this.dbdoc.numero + " and anno = " + db.pc(this.dbdoc.anno, "INTEGER") + " order by riga";

        this.sqlGrigliaB = "SELECT riga, codice_articolo, rig.descrizione, costo_giornaliero, costo_mensile, con.id as con_id, con.descrizione as durata_consulenza, cnt.id as cnt_id, cnt.descrizione as durata_contratto, tef.id as tef_id, tef.descrizione as emissione_fattura, pag.codice as id_pag, pag.descrizione as termini_pagamento, totale_imponibile, rig.id ";
        this.sqlGrigliaB += "FROM " + getNomeTabRighe() + " rig LEFT JOIN tipi_emissione_fattura tef ON rig.emissione_fattura = tef.id LEFT JOIN pagamenti pag ON rig.termini_pagamento = pag.codice LEFT JOIN tipi_durata_consulenza con ON rig.durata_consulenza = con.id LEFT JOIN tipi_durata_contratto cnt ON rig.durata_contratto = cnt.id";
        this.sqlGrigliaB += " where serie = " + db.pc(this.dbdoc.serie, "VARCHAR") + " and numero = " + this.dbdoc.numero + " and anno = " + db.pc(this.dbdoc.anno, "INTEGER") + " order by riga";

        if (visConsegnaPrevista()) {
            griglia.colonneEditabiliByName = new String[]{getCampoQtaEvasa(), "consegnato"};
        } else {
            griglia.colonneEditabiliByName = new String[]{getCampoQtaEvasa()};
        }
        griglia.dbEditabile = true;
        griglia.dbConsentiAggiunte = false;

        //dimensioni colonne
        java.util.Hashtable colsWidthPerc = new java.util.Hashtable();
        colsWidthPerc.put("serie", new Double(0));
        colsWidthPerc.put("numero", new Double(0));
        colsWidthPerc.put("anno", new Double(0));
        colsWidthPerc.put("stato", new Double(0));
        colsWidthPerc.put("riga", new Double(5));
        colsWidthPerc.put("articolo", new Double(15));
        colsWidthPerc.put("descrizione", new Double(40));
        colsWidthPerc.put("um", new Double(5));
        colsWidthPerc.put("quantita", new Double(10));
        colsWidthPerc.put(getCampoQtaEvasa(), new Double(10));
        colsWidthPerc.put("prezzo", new Double(12));
        //colsWidthPerc.put("sconto1", new Double(10));
        colsWidthPerc.put("sconto2", new Double(0));
        colsWidthPerc.put("iva", new Double(5));
        colsWidthPerc.put("Totale", new Double(10));
        colsWidthPerc.put("Ivato", new Double(10));
        colsWidthPerc.put("Sconti", new Double(10));
        colsWidthPerc.put("id", 0d);
        colsWidthPerc.put("id_padre", 0d);
        if (visConsegnaPrevista()) {
            colsWidthPerc.put("consegna", new Double(10));
            colsWidthPerc.put("consegnato", new Double(10));
        }
        if (main.isPluginContabilitaAttivo()) {
            colsWidthPerc.put("conto", new Double(10));
        }
        if (main.fileIni.getValueBoolean("pref", "ColAgg_righe_note", false)) {
            colsWidthPerc.put("note", 15d);
        }

        griglia.columnsSizePerc = colsWidthPerc;

        griglia.dbOpen(db.getConn(), sql);
        griglia.getColumn("quantita").setCellRenderer(InvoicexUtil.getNumber0_5Renderer());
        griglia.getColumn(getCampoQtaEvasa()).setCellRenderer(InvoicexUtil.getNumber0_5Renderer());
        griglia.getColumn("Sconti").setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lab = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Double sconto1 = cu.d(table.getValueAt(row, column));
                if (sconto1 != null) {
                    String ssconto1 = fu.formatNum0_5Dec(sconto1);
                    Double sconto2 = cu.d(table.getValueAt(row, column + 1));
                    String ssconto2 = fu.formatNum0_5Dec(sconto2);
                    lab.setText("");
                    lab.setHorizontalAlignment(SwingConstants.CENTER);
                    if (sconto1 != null && sconto1 != 0) {
                        lab.setText(ssconto1);
                    }
                    if (sconto2 != null && sconto2 != 0) {
                        lab.setText(lab.getText() + " + " + ssconto2);
                    }
                } else {
                    lab.setText("");
                }
                return lab;
            }

        });

        if (visConsegnaPrevista()) {
            griglia.getColumn("consegna").setCellRenderer(new RendererUtils.DateRenderer(new SimpleDateFormat("dd/MM/yy")));
            griglia.getColumn("consegnato").setCellRenderer(new MyCheckBoxRenderer());
//            consegnatocheckbox.setFocusable(false);
            consegnatoeditor.setClickCountToStart(1);
            griglia.getColumn("consegnato").setCellEditor(consegnatoeditor);
        }

        DecimalFormat df1 = new DecimalFormat("0.#####");
        griglia.getColumn(getCampoQtaEvasa()).setCellEditor(new EditorUtils.NumberEditor(new JTextField(), df1) {
            public Object getCellEditorValue() {
                String text = ((JTextField) editorComponent).getText();
                Double qta_evasa = CastUtils.toDouble0All(text);
                System.out.println("text:" + text + " qta_evasa:" + qta_evasa);
                return qta_evasa;
            }
        });
    }

    private void togliAltriCampiEccDestDiversaForn() {
        List<JComponent> i = new ArrayList();
        i.add(labDestDiversa);
        i.add(comClieDest);
//        i.add(panDestManuale);

        i.add(labDDRagSoc);
        i.add(texDestRagioneSociale);
        i.add(labDDIndirizzo);
        i.add(texDestIndirizzo);
        i.add(labDDCap);
        i.add(texDestCap);
        i.add(labDDLoc);
        i.add(texDestLocalita);
        i.add(labDDProv);
        i.add(texDestProvincia);
        i.add(labDDTel);
        i.add(texDestTelefono);
        i.add(labDDCel);
        i.add(texDestCellulare);

        for (Component c : dati_altri2.getComponents()) {
            if (i.indexOf(c) < 0) {
                c.setVisible(false);
            }
        }

    }

    private String getNomeTabRighe() {
        if (table_righe_temp != null) {
            return table_righe_temp;
        }
        return "righ_ordi" + suff;
    }

    public String getNomeTabRigheLotti() {
        if (table_righe_lotti_temp != null) {
            return table_righe_lotti_temp;
        }
        return "righ_ordi" + suff + "_lotti";
    }

    public String getNomeTabRigheMatricole() {
        return null;
    }

    public void aggiornaFido() {
        if (workerFido != null && !workerFido.isDone()) {
            workerFido.cancel(true);
        }
        Object ret = null;
        try {
//            Class fidoclass = Class.forName("invoicexplugininvoicex.fido.WorkerFido");
            Class fidoclass = main.pf.classloader.loadClass("invoicexplugininvoicex.fido.WorkerFido");
            Constructor fidoconstr = fidoclass.getConstructor(Integer.class, String.class, Integer.class, boolean.class);
            ret = fidoconstr.newInstance(cu.i(texClie.getText()), dbStato, id, forza_vis_fido);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ret != null) {
            workerFido = (SwingWorker) ret;
            workerFido.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    // propertyName=state; oldValue=STARTED; newValue=DON
                    if (evt.getNewValue() == SwingWorker.StateValue.DONE) {

                        try {
                            Object ret = workerFido.get();
                            if (ret != null) {
                                Map m = (Map) ret;

                                double esposizione = cu.d0(m.get("esposizione"));
                                double importo_fido = cu.d0(m.get("importo"));
                                String msg = "<html>"
                                        + "Fido cliente € " + FormatUtils.formatEuroIta(importo_fido) + "<br>"
                                        + "Esposizione attuale € " + FormatUtils.formatEuroIta(esposizione) + "<br>";
                                double totale = cu.d0(texTotaDaPagareFinale.getText());
                                msg += "Totale di questo documento € " + FormatUtils.formatEuroIta(totale) + "<br>";
                                if (esposizione + totale > importo_fido) {
                                    msg += "<b><font color=\"red\">Totale esposizione oltre il fido ! € " + FormatUtils.formatEuroIta(totale + esposizione) + "</font></b><br>";
                                } else {
                                    msg += "Totale esposizione entro il fido <font color=\"green\">€ " + FormatUtils.formatEuroIta(totale + esposizione) + "</font><br>";
                                }

                                List<Map> scalare = (List<Map>) m.get("scalare");
                                double esposizione_scalare = esposizione + totale;
                                if (scalare != null && scalare.size() > 0) {
                                    msg += "<br>";
                                    for (Map msca : scalare) {
                                        if (cu.d0(msca.get("somma_da_pagare")) != 0) {
                                            esposizione_scalare -= cu.d0(msca.get("somma_da_pagare"));
                                            String s_espo = "<font color=\"" + (esposizione_scalare < importo_fido ? "green" : "red") + "\">€ " + FormatUtils.formatEuroIta(esposizione_scalare) + "</font>";
                                            msg += "Esposizione al " + DateUtils.formatDateIta(cu.toDate(msca.get("data_scadenza"))) + " " + s_espo + "<br>";
                                        }
                                    }
                                }
                                msg += "</html>";

                                if (esposizione + totale > importo_fido || forza_vis_fido || (balloon_fido != null && balloon_fido.isVisible())) {
                                    if (balloon_fido == null || !balloon_fido.isVisible()) {
                                        balloon_fido = new BalloonTip(texCliente, msg, edgedLook, true);
                                    } else {
                                        balloon_fido.setTextContents(msg);
                                    }

                                    balloon_fido.setVisible(true);
                                }

                                //ToolTipUtils.balloonToToolTip(balloon_bollo, 500, 5000);                            
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                }
            });
            workerFido.execute();
        }

    }

    private void caricaComboContatti() {
        //carico combo contatti di riferimento
        String oldtext = comContattoRiferimento.getText();
        comContattoRiferimento.dbClearList();
        comContattoRiferimento.dbAddElement("", null);
        comContattoRiferimento.dbOpenList(Db.getConn(), "select nome, id from clie_forn_contatti where id_clifor = " + dbu.sql(cu.i(texClie.getText())));
        comContattoRiferimento.setText(oldtext);
    }

    private String getNomeCampoStatoDef() {
        return "stato_def_" + (cu.s(prevStato).equalsIgnoreCase("P") ? "pre" : "ord") + "_" + (acquisto ? "acq" : "ven");
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

    static public class MyCheckBoxRenderer extends JCheckBox implements TableCellRenderer {

        MyCheckBoxRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                //super.setBackground(table.getSelectionBackground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            boolean sel = CastUtils.toBoolean(value);
            setSelected(sel);
            return this;
        }
    }

    static public class MyCheckBoxEditor extends DefaultCellEditor {

        public MyCheckBoxEditor(final JCheckBox checkbox) {
            super(checkbox);
            editorComponent = checkbox;
            checkbox.setHorizontalAlignment(JLabel.CENTER);
            delegate = new EditorDelegate() {
                public void setValue(Object value) {
                    boolean selected = false;
                    if (value instanceof Boolean) {
                        selected = ((Boolean) value).booleanValue();
                    } else if (value instanceof String) {
                        //selected = value.equals("true");
                        selected = CastUtils.toBoolean(value);
                    }
                    checkbox.setSelected(selected);
                }

                public Object getCellEditorValue() {
                    return checkbox.isSelected() ? "S" : "N";
                }
            };
            checkbox.addActionListener(delegate);
            checkbox.setRequestFocusEnabled(false);
        }
    }

    SwingWorker workerFido = null;
    BalloonTipStyle edgedLook = new EdgedBalloonStyle((Color) UIManager.get("ToolTip.background"), Color.RED);
    BalloonTip balloon_fido;

    public void recuperaDatiCliente() {

        try {
            if (this.texClie.getText().length() > 0) {
                this.dbdoc.forceCliente(Long.parseLong(this.texClie.getText()));
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        if (!acquisto) {
            aggiornaFido();
        }

        //li recupero dal cliente
        ResultSet tempClie;
        String sql = "select * from clie_forn";
        sql += " where codice = " + Db.pc(this.texClie.getText(), "NUMERIC");
        tempClie = Db.openResultSet(sql);

        try {
            if (tempClie.next() == true) {

                //tecnospurghi preferisce che rimanga con il codice fra parentesi quadre dopo la selezione
//                texCliente.setText(cu.s(tempClie.getString("ragione_sociale")));
                //this.texPaga.setText(Db.nz(tempClie.getString("pagamento"),""));
                //if (Db.nz(tempClie.getString("pagamento"),"").length() > 0) this.comPaga.setText(Db.nz(tempClie.getString("pagamento"),""));
                if (Db.nz(tempClie.getString("pagamento"), "").length() > 0) {
                    this.comPaga.dbTrovaRiga(Db.nz(tempClie.getString("pagamento"), ""));
                }

                comPagaFocusLost(null);

                if (Db.nz(tempClie.getString("banca_abi"), "").length() > 0) {
                    this.texBancAbi.setText(Db.nz(tempClie.getString("banca_abi"), ""));
                }

                if (Db.nz(tempClie.getString("banca_cab"), "").length() > 0) {
                    this.texBancCab.setText(Db.nz(tempClie.getString("banca_cab"), ""));
                }

                if (Db.nz(tempClie.getString("banca_cc_iban"), "").length() > 0) {
                    this.texBancIban.setText(Db.nz(tempClie.getString("banca_cc_iban"), ""));
                }

                //if (Db.nz(tempClie.getString("banca_cc"),"").length() > 0) this.texBancCC.setText(Db.nz(tempClie.getString("banca_cc"),""));
                //cerca lengthdescrizioni
                texBancAbiActionPerformed(null);
                trovaCab();

                //opzione dest diversa riba
                if (tempClie.getString("opzione_riba_dest_diversa") != null && tempClie.getString("opzione_riba_dest_diversa").equalsIgnoreCase("S")) {
                    this.cheOpzioneRibaDestDiversa.setSelected(true);
                } else {
                    this.cheOpzioneRibaDestDiversa.setSelected(false);
                }

                if (tempClie.getInt("agente") >= 0) {
                    if (main.getPersonalContain("medcomp")) {
                        //selezionare gli agenti in base a quelli collegati al cliente fornitore
                        Integer cod_cliente = null;
                        try {
                            cod_cliente = cu.toInteger(texClie.getText());
                        } catch (Exception e) {
                        }
                        InvoicexUtil.caricaComboAgentiCliFor(comAgente, cod_cliente);
                    } else {
                        comAgente.dbTrovaKey(tempClie.getString("agente"));
                    }
                    comAgenteFocusLost(null);
                }

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
                    try {
                        String prezzi_ivati_s = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select l.prezzi_ivati from dati_azienda a join tipi_listino l on a.listino_base = l.codice"));
                        if (prezzi_ivati_s.equalsIgnoreCase("S")) {
                            prezzi_ivati_b = true;
                        }
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }
                }
                if (prezzi_ivati_virtual.isSelected() != prezzi_ivati_b) {
                    prezzi_ivati_virtual.setSelected(prezzi_ivati_b);
                    prezzi_ivati.setSelected(prezzi_ivati_b);
                    prezzi_ivati_virtualActionPerformed(null);
                }

                aggiornaNote();

                caricaComboContatti();

                InvoicexUtil.fireEvent(this, InvoicexEvent.TYPE_FRMTESTORDI_CARICA_DATI_CLIENTE, tempClie);
            } else {
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        ricalcolaTotali();
    }

    private void aggiornaNote() {
        if (loading) {
            return;
        }
        //note automatiche e altro
        String sqlTmp = "SELECT note_prev, note_ordi, note_prev_acq, note_ordi_acq, modalita_consegna, modalita_scarico, note_consegna FROM clie_forn where codice = " + Db.pc(String.valueOf(comClie.getSelectedKey()), "NUMERIC");
        ResultSet res = Db.openResultSet(sqlTmp);
        try {
            if (res.next()) {
                String tipo = cu.s(comStatoOrdine.getSelectedItem());
                boolean is_ordine = tipo.indexOf("Ordine") >= 0;

                String key_prev_standard = "";
                String key_ordi_standard = "";
                String key_prev_cliente = "";
                String key_ordi_cliente = "";

                if (acquisto) {
                    key_prev_cliente = "note_prev_acq";
                    key_ordi_cliente = "note_ordi_acq";
                    key_prev_standard = "noteStandardPrevAcquisto";
                    key_ordi_standard = "noteStandardOrdiAcquisto";
                } else {
                    key_prev_cliente = "note_prev";
                    key_ordi_cliente = "note_ordi";
                    key_prev_standard = "noteStandardPrev";
                    key_ordi_standard = "noteStandardOrdi";
                }

                String key_ini = is_ordine ? key_ordi_standard : key_prev_standard;
                String key_campo = is_ordine ? key_ordi_cliente : key_prev_cliente;

                String note_standard = main.fileIni.getValue("pref", key_ini, "");
                String note_ordi_standard = main.fileIni.getValue("pref", key_ordi_standard, "");
                String note_prev_standard = main.fileIni.getValue("pref", key_prev_standard, "");
                String note_ordi_cliente = res.getString(key_ordi_cliente);
                String note_prev_cliente = res.getString(key_prev_cliente);

                String note_cliente = cu.s(res.getString(key_campo));

                String nuove_note = note_standard + (StringUtils.isNotBlank(texNote.getText()) ? "\n" : "") + note_cliente;

                Map map_diversi = new HashMap();
                if (!texNote.getText().equalsIgnoreCase(nuove_note)) {
                    map_diversi.put("Note", nuove_note);
                }
                if (!comConsegna.getText().equalsIgnoreCase(cu.s(res.getObject("modalita_consegna")))) {
                    map_diversi.put("Modalità di consegna", cu.s(res.getObject("modalita_consegna")));
                }
                if (!comScarico.getText().equalsIgnoreCase(cu.s(res.getObject("modalita_scarico")))) {
                    map_diversi.put("Modalità di scarico", cu.s(res.getObject("modalita_scarico")));
                }
                if (texNoteConsegna.isVisible() && !texNoteConsegna.getText().equalsIgnoreCase(cu.s(res.getObject("note_consegna")))) {
                    map_diversi.put("Note di consegna", res.getString("note_consegna"));
                }
                boolean sovrascrivere = true;
                if (map_diversi.size() > 0 && dbStato.equals(tnxDbPanel.DB_MODIFICA)) {
                    if (!SwingUtils.showYesNoMessage(this, "Sovrascrivere i seguenti dati:" + map_diversi.keySet() + "\nCon i dati dall'anagrafica cliente/fornitore o dalle note predefinite ?", "Attenzione")) {
                        sovrascrivere = false;
                    }
                }
                if (sovrascrivere) {
                    texNote.setText(nuove_note);
                    //consegna e scarico
                    try {
                        comConsegna.dbTrovaKey(res.getObject("modalita_consegna"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        comScarico.dbTrovaKey(res.getObject("modalita_scarico"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        texNoteConsegna.setText(res.getString("note_consegna"));
                    } catch (Exception e) {
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void trovaAbi() {
        caricaBanca();
    }

    private void trovaCab() {
        try {
            String sql = "";
            sql += "select banche_cab.cap,";
            sql += " banche_cab.indirizzo,";
            sql += " comuni.comune,";
            sql += " comuni.provincia";
            sql += " from banche_cab left join comuni on banche_cab.codice_comune = comuni.codice";
            sql += " where banche_cab.abi = " + Db.pc(this.texBancAbi.getText(), "VARCHAR");
            sql += " and banche_cab.cab = " + Db.pc(this.texBancCab.getText(), "VARCHAR");

            ResultSet temp = Db.openResultSet(sql);

            if (temp.next()) {
                String descCab = Db.nz(temp.getString(1), "") + " " + Db.nz(temp.getString(2), "") + ", " + Db.nz(temp.getString(3), "") + " (" + Db.nz(temp.getString(4), "") + ")";
                descCab = StringUtils.abbreviate(descCab, CoordinateBancarie.maxCab);
                this.labBancCab.setText(descCab);
            } else {
                this.labBancCab.setText("");
            }
        } catch (Exception err) {
            this.labBancCab.setText("");
        }
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
                texCliente.requestFocus();
                javax.swing.JOptionPane.showMessageDialog(this, "Il codice " + ccliente + " specificato non esiste in anagrafica !");
                return false;
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        //controllo fido
        try {
            if (!acquisto) {
                aggiornaFido();
                Object retfido = workerFido.get();
                if (retfido != null && ((Map) retfido).get("importo") != null) {
                    Map m = (Map) retfido;
                    double esposizione = cu.d0(m.get("esposizione"));
                    double importo_fido = cu.d0(m.get("importo"));
                    double totale = cu.d0(texTotaDaPagareFinale.getText());
                    boolean obbl = cu.toBoolean(m.get("obbligatorio"));
                    if (esposizione + totale > importo_fido && obbl) {
                        SwingUtils.showInfoMessage(this, "Non puoi savare il documento perchè l'esposizione del cliente è oltre il fido");
                        return false;
                    } else if (esposizione + totale > importo_fido && !obbl) {
                        if (!SwingUtils.showYesNoMessage(this, "Attenzione, l'esposizione del cliente è oltre il fido\nSicuro di continuare ?")) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //controllo pagamento
        ResultSet temp;
        boolean ok = true;
        boolean flagCoordinate = false;
        sql = "select * from pagamenti where codice = " + Db.pc(this.comPaga.getSelectedKey().toString(), "VARCHAR");
        temp = Db.openResultSet(sql);

        try {
            if (temp.next() == true && comPaga.getSelectedKey().toString().length() > 0) {
                if (temp.getString("coordinate_necessarie").equalsIgnoreCase("S")) {
                    //servono lengthcoordinate, cotnrollare che ci siano i 3 ccampi della banca
                    if (this.texBancAbi.getText().length() == 0 || this.texBancCab.getText().length() == 0) {
                        flagCoordinate = true;
                        ok = false;
                    }
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Manca il tipo di pagamento (e' obbligatorio)", "Attenzione", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                ok = false;
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        if (ok == false && flagCoordinate == true) {
            //uscire per mancanza coordinate
            int ret = javax.swing.JOptionPane.showConfirmDialog(this, "Mancano le coordinate bancarie per il tipo di pagamento scelto\nContinuare ugualmente?", "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION);
            if (ret != javax.swing.JOptionPane.YES_OPTION) {
                return false;
            }
        } else if (ok == false) {
            return false;
        }

        //controllo tipo pagamento
        if (!InvoicexUtil.controlloGiornoPagamento(String.valueOf(comPaga.getSelectedKey()), texGiornoPagamento.getText(), this)) {
            return false;
        }

        return true;
    }

    private void showPrezziFatture() {
        frmPrezziFatturePrecedenti form = new frmPrezziFatturePrecedenti(Integer.parseInt(this.texClie.getText().toString()), null, getTipoDoc());
        main.getPadre().openFrame(form, 450, 500, this.getY() + 50, this.getX() + this.getWidth() - 200);
    }

    public void ricalcolaTotali() {
        try {

            //this.parent.ordine.dbRefresh();
            //provo con nuova classe Documento
            if (texClie.getText() != null && texClie.getText().length() > 0) {
                doc.setCodiceCliente(Long.parseLong(texClie.getText()));
            }

            doc.setAcconto(Db.getDouble(texAcconto.getText()));
            doc.setScontoTestata1(Db.getDouble(texScon1.getText()));
            doc.setScontoTestata2(Db.getDouble(texScon2.getText()));
            doc.setScontoTestata3(Db.getDouble(texScon3.getText()));
            doc.setSpeseIncasso(Db.getDouble(texSpeseIncasso.getText()));
            doc.setSpeseTrasporto(Db.getDouble(texSpeseTrasporto.getText()));
            doc.setPrezziIvati(prezzi_ivati.isSelected());
            doc.setSconto(Db.getDouble(texSconto.getText()));
            doc.calcolaTotali();
            texTota.setText(it.tnx.Util.formatValutaEuro(doc.getTotale()));
            texTotaImpo.setText(it.tnx.Util.formatValutaEuro(doc.getTotaleImponibile()));
            texTotaIva.setText(it.tnx.Util.formatValutaEuro(doc.getTotaleIva()));
            texTotaDaPagareFinale.setText(it.tnx.Util.formatValutaEuro(doc.getTotale_da_pagare_finale()));

            aggiornaFido();
        } catch (Exception err) {
            err.printStackTrace();
        }

        //calcolo totale numero colli 
        if (!loading) {
            InvoicexUtil.calcolaColli(griglia, texNumeroColli);
        }

    }

    private String getAnno() {
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

    synchronized private void riempiDestDiversa(String sql) {
        boolean oldrefresh = dati.isRefreshing;
        dati.isRefreshing = true;
        comClieDest.setRinominaDuplicati(true);
        comClieDest.dbClearList();
        comClieDest.dbAddElement("", "");
        comClieDest.dbOpenList(Db.getConn(), sql, this.texClieDest.getText(), false);
        dati.isRefreshing = oldrefresh;
    }

    public void aggiornareProvvigioni() {
        block_aggiornareProvvigioni = true;
        if (SwingUtils.showYesNoMessage(this, "Vuoi aggiornare le provvigioni delle righe già inserite alla nuova provvigione ?")) {
            int id = InvoicexUtil.getIdOrdine(dbdoc.serie, dbdoc.numero, dbdoc.anno);
            String sql = "update " + getNomeTabRighe() + " set provvigione = " + Db.pc(cu.toDouble0(texProvvigione.getText()), Types.DOUBLE) + " where id_padre = " + id;
            try {
                DbUtils.tryExecQuery(Db.getConn(), sql);
                griglia.dbRefresh();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        block_aggiornareProvvigioni = false;
    }

    public JTable getGrid() {
        return griglia;
    }

    private String getStatoEvaso() {
        System.out.println("!!! getStatoEvaso: " + stato_evasione.getSelectedIndex());
        if (stato_evasione.getSelectedIndex() == 0) {
            return "S";
        }
        if (stato_evasione.getSelectedIndex() == 1) {
            return "P";
        } else {
            return "";
        }
    }

    public tnxDbGrid getGrigliaInitComp() {
        return new tnxDbGrid() {
            //ovveride del save

            @Override
            public void saveDataEntry(int row) {
                //non faccio niente e salvo solo cosa voglio io
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                if (column == getColumnByName(getCampoQtaEvasa())) {
                    double qta_evasa = CastUtils.toDouble0(aValue);
                    qta_evasa = FormatUtils.round(qta_evasa, 5);
                    aValue = qta_evasa;
                    //salvo in tabella riga ddt la qta evasa
                    String tabr = getNomeTabRighe();
                    try {
                        Integer idriga = CastUtils.toInteger(getValueAt(row, getColumnByName("id")));
                        String sql = "update " + tabr + " set quantita_evasa = " + Db.pc(CastUtils.toDouble(aValue), Types.DOUBLE) + " where id = " + idriga;
                        System.err.println("sql = " + sql);
                        DbUtils.tryExecQuery(Db.getConn(), sql);
                        aggiornaStatoEvasione();
                    } catch (Exception e) {
                        SwingUtils.showExceptionMessage(this, e);
                    }
                } else if (column == getColumnByName("consegnato")) {
                    String cons = cu.s(aValue);
                    //salvo in tabella riga ddt la qta evasa
                    String tabr = getNomeTabRighe();
                    try {
                        Integer idriga = CastUtils.toInteger(getValueAt(row, getColumnByName("id")));
                        String sql = "update " + tabr + " set flag_consegnato = " + Db.pcs(cons) + " where id = " + idriga;
                        System.err.println("sql = " + sql);
                        DbUtils.tryExecQuery(Db.getConn(), sql);
                    } catch (Exception e) {
                        SwingUtils.showExceptionMessage(this, e);
                    }
                } else {
                    SwingUtils.showErrorMessage(this, "Colonna non modificabile (column:" + column + ")");
                }
            }

            public void changeSelection(final int row, final int column, boolean toggle, boolean extend) {
                super.changeSelection(row, column, toggle, extend);
                System.out.println("changeSel:" + row + " " + column);
                if (editCellAt(row, column)) {
                    Component comp = getEditorComponent();
                    comp.requestFocusInWindow();
                    if (comp instanceof JTextField) {
                        JTextField textComp = (JTextField) comp;
                        textComp.selectAll();
                    }
                }
            }
        };
    }

    private void aggiornaStatoEvasione() {
        String evaso = InvoicexUtil.getStatoEvasione(griglia, "quantita", getCampoQtaEvasa());
        if ("S".equalsIgnoreCase(evaso)) {
            stato_evasione.setSelectedIndex(0);
        } else if ("P".equalsIgnoreCase(evaso)) {
            stato_evasione.setSelectedIndex(1);
        } else {
            stato_evasione.setSelectedIndex(2);
        }
    }

    private String getCampoQtaEvasa() {
        return acquisto ? "qta arrivata" : "qta evasa";
    }

    private String getTipoDoc() {
        return acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE;
    }

    private boolean visConsegnaPrevista() {
        if (consegna_prevista.getDate() != null) {
            return true;
        }

        String sql = "SELECT data_consegna_prevista ";
        sql += " FROM " + getNomeTabRighe() + " rig";
        sql += " where data_consegna_prevista is not null and id_padre = " + id;
        sql += " limit 1";
        try {
            return DbUtils.containRows(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public tnxDbPanel getDatiPanel() {
        return dati;
    }

    public JTabbedPane getTab() {
        return panTab;
    }

    public boolean isAcquisto() {
        return acquisto;
    }

    public void selezionaCliente() {
        //apro combo destinazione cliente
        sql = "select ragione_sociale, id from clie_forn_dest";
        sql += " where codice_cliente = " + Db.pc(this.texClie.getText(), "NUMERIC");
        sql += " order by ragione_sociale";
        riempiDestDiversa(sql);
        recuperaDatiCliente();

        //commento perchè lo fa già in recuperaDatiCliente
        //texNote.setText("");
        //aggiornaNote();
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
            //controllo tabella temp
            String sql = "check table righ_ordi" + suff + "_temp";
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
            sql = "delete from righ_ordi" + suff + "_temp";
            sql += " where id_padre = " + frmTestOrdine.this.id;
            sql += " and username = '" + main.login + "'";
            Db.executeSqlDialogExc(sql, true);

            sql = "delete te.* from righ_ordi" + suff + "_temp te join righ_ordi" + suff + " ri on te.id = ri.id";
            sql += " and ri.serie " + Db.pcW(this.dbdoc.serie, "VARCHAR");
            sql += " and ri.numero " + Db.pcW(String.valueOf(this.dbdoc.numero), "NUMBER");
            sql += " and ri.anno " + Db.pcW(String.valueOf(this.dbdoc.anno), "VARCHAR");
            sql += " and te.username = '" + main.login + "'";
            Db.executeSqlDialogExc(sql, true);

            //e inserisco
            sql = "insert into righ_ordi" + suff + "_temp";
            sql += " select *, '" + main.login + "' as username";
            sql += " from righ_ordi" + suff;
            sql += " where id_padre = " + frmTestOrdine.this.id;
            Db.executeSqlDialogExc(sql, true);
        } else {
            System.out.println("porto_in_temp, NO per edit in mem");
        }
    }
}
