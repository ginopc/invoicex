/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * MenuPanel.java
 *
 * Created on 25-giu-2010, 16.36.26
 */
package gestioneFatture;

import static gestioneFatture.WizardDb.progress;
import static gestioneFatture.WizardDb.wizard;
import gestioneFatture.logic.documenti.Documento;
import gestioneFatture.logic.documenti.Documento2;
import gestioneFatture.magazzino.JInternalFrameExportStampaGiacenze;
import gestioneFatture.magazzino.JInternalFrameGiacenze;
import static gestioneFatture.main.paramProp;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import it.tnx.ClassPathHacker;
import it.tnx.Db;
import it.tnx.accessoUtenti.Permesso;
import it.tnx.accessoUtenti.Utente;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.FileUtils;
import it.tnx.commons.HttpUtils;
import it.tnx.commons.MicroBench;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.commons.swing.DelayedExecutor;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.Main;
import it.tnx.invoicex.PlatformUtils;
import it.tnx.invoicex.sync.Sync;
import it.tnx.invoicex.gui.JDialogAbout;
import it.tnx.invoicex.gui.JDialogDatiAzienda;
import it.tnx.invoicex.gui.JDialogExc;
import it.tnx.invoicex.gui.JDialogExportAcquistiVendite;
import it.tnx.invoicex.gui.JDialogExportElencoDdt;
import it.tnx.invoicex.gui.JDialogExportVenditePeroni;
import it.tnx.invoicex.gui.JDialogHelpAss;
import it.tnx.invoicex.gui.JDialogInfoLicenza;
import it.tnx.invoicex.gui.JDialogNoteRilascio;
import it.tnx.invoicex.gui.JDialogPlugins;
import it.tnx.invoicex.gui.JDialogProgress;
import it.tnx.invoicex.gui.JDialogTotaliFatture;
import it.tnx.invoicex.gui.JDialogUpd;
import it.tnx.invoicex.gui.JFrameDb;
import it.tnx.invoicex.gui.JFrameWizardDb;
import it.tnx.invoicex.gui.JInternalFrameClientiFornitori;
import it.tnx.invoicex.gui.JInternalFrameReportImpegnato;
import it.tnx.invoicex.gui.JInternalFrameScadenzario;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javafx.application.Platform;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.table.TableColumnModel;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import net.sf.jasperreports.engine.JasperManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingworker.SwingWorker;
import org.jibble.logtailer.JLogTailerFrame;
import org.json.simple.JSONObject;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.TimelineState;
import org.pushingpixels.trident.callback.TimelineCallback;
import org.pushingpixels.trident.callback.TimelineCallbackAdapter;
import org.pushingpixels.trident.ease.Spline;
import tnxbeans.tnxDbPanel;

/**
 *
 * @author mceccarelli
 */
public class MenuPanel extends javax.swing.JPanel {

    public static MenuPanel INSTANCE = null;
    Timeline rolloverTimeline;
    Timeline timeline = null;
    private long lastshow;
    private boolean menuvis;
    Thread trmenu = null;
    float posBarr2 = 0f;
    boolean bcheckPanBarr2 = false;
    boolean nascondimenu = false;
    boolean initcomps = true;
    JLogTailerFrame tail = null;
    public static boolean sfuma = true;
    JLabel labSync = null;

    public static Map oldFramesWidth = new HashMap();

    private MouseAdapter mouse_adapter_dimensioni_finestra;

    private void checkSize(int left, int top, int larg, int alte) {
        left = 0;
        top = 0;
        larg = 100;
        alte = 100;
    }

    void addSyncIcon() {
        labSync = new JLabel();
        labSync.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Cloud-16.png")));
        labSync.setBounds(desktop.getWidth() - 16 - 2, 2, 16, 16);
        desktop.add(labSync);
    }

    private Map getDimensioniFrame(JInternalFrame frame, Map oldSize, Integer orig_width, Integer orig_height) {
        System.out.println("carica dimensioni frame");

        Properties p = new SortedProperties();
        String nomefile = System.getProperty("user.home") + "/.invoicex/finestre.txt";

        try {
            if (new File(nomefile).exists()) {
                p.load(new FileInputStream(nomefile));
            }
        } catch (IOException ex) {
            SwingUtils.showExceptionMessage(frame, ex);
        }

        String parent_name = SwingUtils.getFrameId(frame);

        if (oldSize == null) {
            Map save = new HashMap();
            save.put("width", orig_width);
            save.put("height", orig_height);
            oldFramesWidth.put(parent_name, save);
        }

        if (oldSize != null) {
            Map ret = new HashMap();
            ret.put("width", cu.i0(oldSize.get("width")));
            ret.put("height", cu.i0(oldSize.get("height")));
            return ret;
        }

        if (p.containsKey(parent_name + "_width")) {
            Map ret = new HashMap();
            ret.put("width", cu.i0(p.get(parent_name + "_width")));
            ret.put("height", cu.i0(p.get(parent_name + "_height")));
            return ret;
        }

        return null;
    }

    public enum TipoNews {

        SOLE, LAMPADINA
    };

    /**
     * Creates new form MenuPanel
     */
    public MenuPanel() {
        INSTANCE = this;

        try {
            initComponents();
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showExceptionMessage(main.splash, e);
        }

        lblInfoLoading2.setVisible(false);

        if (PlatformUtils.isWindows()) {
            System.out.println(desktop.getDesktopManager());
            desktop.setDesktopManager(new DefaultDesktopManager());
        }

        if (!main.getPersonalContain("consegna_e_scarico")) {
//            menAnagConsegna.setVisible(false);
            menAnagScarico.setVisible(false);
        }

        menContab.setVisible(false);

        labmess2.setVisible(false);

        if (!main.getPersonalContain("snj")) {
            menSnj.setVisible(false);
        }
        if (!main.getPersonalContain("peroni")) {
            menExpVendPeroni.setVisible(false);
        }
        if (main.pluginAttivi.contains("pluginToysforyou")) {
            menGestEvadibileCliente.setVisible(true);
            menGestImpegnato.setVisible(false);
            menGestImpegnatoProduttore.setVisible(true);
        } else {
            menGestEvadibileCliente.setVisible(false);
            menGestImpegnato.setVisible(false);
            menGestImpegnatoProduttore.setVisible(false);
        }

//        menMagazzinoUltimiPrezzi.setVisible(false);
        int utenti = 0;
        try {
            utenti = CastUtils.toInteger0(DbUtils.getObject(Db.getConn(true), "select gestione_utenti from dati_azienda limit 1"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (utenti == 0) {
            menGestUtenti.setVisible(false);
        }

        String data_scadenza = null;
        try {
            data_scadenza = main.fileIni.getValue("plugin_invoicex", "scadenza");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (data_scadenza == null || data_scadenza.length() == 0) {
            menAiutoInfoLicenza.setVisible(false);
        }

        //menù dinamico
        int menuPersonale = 1;
        boolean controllo = true;
        boolean addMenuToBar = false;
        JMenu menPersonale = new JMenu();
        menPersonale.setText("Comandi Personali");

        while (controllo) {
            String testo = main.fileIni.getValue("menupersonale", "menu" + menuPersonale, "");
            String cmd = main.fileIni.getValue("menupersonale", "comando" + menuPersonale, "");

            if (!testo.equals("") && !cmd.equals("")) {
                JMenuItem menPersonaleItem = new JMenuItem();
                menPersonaleItem.setText(testo);
                final String execCmd = cmd;
                menPersonaleItem.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        try {
                            System.out.println("execCmd = " + execCmd);
                            Process exec = Runtime.getRuntime().exec(execCmd);
                        } catch (IOException ex) {
                            SwingUtils.showErrorMessage(main.getPadrePanel(), "Errore nell'esecuzione del comando personalizzato", "Errore");
                        }
                    }
                });

                menPersonale.add(menPersonaleItem);
                addMenuToBar = true;
                menuPersonale++;
            } else {
                controllo = false;
            }
        }

        if (addMenuToBar) {
            menBar.add(menPersonale);
        }

        System.out.println(desktop.getDesktopManager().getClass());

//        desktop.setDesktopManager(new DefaultDesktopManager() {
//            public void dragFrame(JComponent f, int x, int y) {
//                System.out.println("f = " + f);
//                
//                if (f instanceof JInternalFrame) {  // Deal only w/internal frames
//                    JInternalFrame frame = (JInternalFrame) f;
//                    JDesktopPane desk = frame.getDesktopPane();
//                    Dimension d = desk.getSize();
//
//                    // Nothing all that fancy below, just figuring out how to adjust
//                    // to keep the frame on the desktop.
////                    if (x < 0) {              // too far left?
//                    if (x < -(frame.getWidth() - 100)) {              // too far left?
////                        x = 0;                  // flush against the left side
//                        x = -(frame.getWidth() - 100);
//                    } else {
////                        if (x + frame.getWidth() > d.width) {     // too far right?
////                            x = d.width - frame.getWidth();         // flush against right side
////                        }
//                        if (x + 100 > d.width) {     // too far right?
//                            x = d.width - 100;         // flush against right side
//                        }
//                    }
//                    if (y < 0) {              // too high?
//                        y = 0;                    // flush against the top
//                    } else {
////                        if (y + frame.getHeight() > d.height) {   // too low?
////                            y = d.height - frame.getHeight();       // flush against the bottom
////                        }
//                        if (y + 50 > d.height) {   // too low?
//                            y = d.height - 50;       // flush against the bottom
//                        }
//                    }
//                }
//
//                // Pass along the (possibly cropped) values to the normal drag handler.
//                super.dragFrame(f, x, y);
//            }
//        });
        panBarr = panBarr2;
        initcomps = false;

//        try {
//            rolloverTimeline = new Timeline(jPanel1);
//            rolloverTimeline.addPropertyToInterpolate("background", Color.blue, Color.red);
//            rolloverTimeline.setDuration(500);
//        } catch (Exception e) {
//            SwingUtils.showExceptionMessage(this, e);
//        }
//        butOrdini1.setVisible(false);
        if (!main.debug) {
            ricalcolo_totali_fatture.setVisible(false);
            jButton4.setVisible(false);
            jButton5.setVisible(false);
            jButton6.setVisible(false);
            jButton7.setVisible(false);
            jButton1.setVisible(false);
            sync.setVisible(false);
            sync_wiz.setVisible(false);
            frame_debug.setVisible(false);
//            imppn.setVisible(false);
        }

//        butRiscattaAcquisto.setVisible(false);
        scrollDesktop.setBackground(desktop.getBackground());

        desktop.addContainerListener(new ContainerListener() {
            public void componentAdded(ContainerEvent e) {
                if (e.getChild() instanceof JInternalFrame) {

                    JInternalFrame frame = (JInternalFrame) e.getChild();

                    if (mouse_adapter_dimensioni_finestra == null) {
                        mouse_adapter_dimensioni_finestra = new MouseAdapter() {
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                if (e.isPopupTrigger()) {
                                    popupMenuFrame(e);
                                }
                            }

                            @Override
                            public void mousePressed(MouseEvent e) {
                                if (e.isPopupTrigger()) {
                                    popupMenuFrame(e);
                                }
                            }

                            private void popupMenuFrame(MouseEvent e) {
                                popMenuFrame.show(e.getComponent(), e.getX(), e.getY());
                            }
                        };
                    }

                    frame.addMouseListener(mouse_adapter_dimensioni_finestra);

                    if (!(PlatformUtils.isMac() && UIManager.getLookAndFeel().getClass().getName().equals(UIManager.getSystemLookAndFeelClassName()))) {
                        //non c'è northpane su osx
                        try {
                            if (frame.getUI() instanceof BasicInternalFrameUI && ((BasicInternalFrameUI) frame.getUI()).getNorthPane() != null) {
                                ((BasicInternalFrameUI) frame.getUI()).getNorthPane().addMouseListener(mouse_adapter_dimensioni_finestra);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                MyEventQueue.abilitaUndo(e.getChild());
            }

            public void componentRemoved(ContainerEvent e) {
            }
        });

        if (main.getPersonalContain("improvvivo")) {
            menAnagListini.setVisible(false);
            menAnagVettori.setVisible(false);
            menAnagArti.setVisible(false);
            menGestDdt.setVisible(false);
            menGestOrdini.setVisible(false);
            menGestRistDistRiba.setVisible(false);
            menMagazzino.setVisible(false);
            menAgenti.setVisible(false);
            butOrdini.setVisible(false);
            butDdt.setVisible(false);
            butArticoli.setVisible(false);
        }

//        desktop.addMouseMotionListener(new MouseMotionAdapter() {
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                super.mouseMoved(e);
//                System.out.println(e);
//            }
//        });
//        if (!PlatformUtils.isWindows()) {
//            butVnc.setVisible(false);
//        }
//        menUtilAggi.setEnabled(false);
        if (main.iniFlagMagazzino == false) {
            this.menMagazzino.setVisible(false);
        }

        panBarr2.setSize(getWidth(), 0);
        //panBarr2.setSize(getWidth(), 40);
        //panBarr2.setSize(getWidth(), 400);

//        checkPanBarr2();
        panBarr2.setLocation(0, -panBarr2.getHeight());
        panBarr2.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                showmenu();
            }
        });

        timeline = new Timeline(this);
        timeline.setEase(new Spline(0.8f));
        timeline.setDuration(200);
        timeline.addPropertyToInterpolate("posBarr2", 0.0f, 1.0f);

        long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK;
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent e) {
                try {
                    int y = (int) MouseInfo.getPointerInfo().getLocation().getY();
                    y = (int) (y - jLayeredPane1.getLocationOnScreen().getY());
                    if (y >= 0 && y < 5) {
                        showmenu();
                    }
                } catch (Exception evt) {
                }
            }
        }, eventMask);

        trmenu = new Thread("trmenu") {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(250);
                        if (menuvis && System.currentTimeMillis() - lastshow > 1500) {
                            hidemenu();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        };
        trmenu.start();

        if (!main.fileIni.existKey("pref", "nascondi_menu")) {
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            if (dim.getWidth() <= 1200) {
                main.fileIni.setValue("pref", "nascondi_menu", true);
            } else {
                main.fileIni.setValue("pref", "nascondi_menu", false);
            }
        }

        initAzioni();

        checkPermessi();
        attivaPermessi();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nascondimenu = main.fileIni.getValueBoolean("pref", "nascondi_menu", false);
                menuvis = false;
                setNascondimenu(nascondimenu);
                popnascondi.setSelected(nascondimenu);
            }
        });

    }
    public Map<String, Action> azioni = new HashMap();

    private void checkPermessi() {
        if (main.utente == null) {
            return;
        }

        Integer idRuolo = main.utente.getIdRuolo();
        try {
            ResultSet rs = Db.openResultSet(Db.getConn(), "SELECT * FROM accessi_tipi_permessi");
            while (rs.next()) {
                Integer idPermesso = rs.getInt("id");
                String sql = "SELECT id FROM accessi_ruoli_permessi WHERE id_role = " + Db.pc(idRuolo, Types.INTEGER) + " AND id_privilegio = " + Db.pc(idPermesso, Types.INTEGER);
                Integer controllo = CastUtils.toInteger0(Db.nz(DbUtils.getObject(Db.getConn(), sql, false), "-1"));
                System.out.println("idPermesso = " + idPermesso + " controllo:" + controllo);
                if (controllo.equals(-1)) {
                    String sqlInsert = "INSERT INTO accessi_ruoli_permessi SET ";
                    sqlInsert += "id_role = " + Db.pc(idRuolo, Types.INTEGER) + ", ";
                    sqlInsert += " id_privilegio = " + Db.pc(idPermesso, Types.INTEGER) + ", ";
                    sqlInsert += " lettura = " + Db.pc(0, Types.INTEGER) + ", ";
                    sqlInsert += " scrittura = " + Db.pc(0, Types.INTEGER) + ", ";
                    sqlInsert += " cancella = " + Db.pc(0, Types.INTEGER);
                    Db.executeSql(sqlInsert);
                    Permesso perm = new Permesso(idPermesso, 0, 0, 0);
                    main.utente.getPermessi().put(idPermesso, perm);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void attivaPermessi() {

        Utente utente = main.utente;

        if (utente.ex != null) {
            System.out.println("!!! problema sui permessi: " + utente.ex.toString());
            SwingUtils.showErrorMessage(main.getPadreFrame(), "Problema nella gestione dei permessi", true);
            return;
        }

        // ANAGRAFICA CLIENTI
        this.butClieForn.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_CLIENTI, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagClie.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_CLIENTI, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagCatCli.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_CLIENTI, Permesso.PERMESSO_TIPO_LETTURA));

        // ANAGRAFICA ARTICOLI
        this.butArticoli.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ARTICOLI, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagArti.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ARTICOLI, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagListini.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ARTICOLI, Permesso.PERMESSO_TIPO_LETTURA));

        // ANAGRAFICA TIPI DI PAGAMENTO
        this.menAnagTipiPaga.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_TIPI_PAGAMENTO, Permesso.PERMESSO_TIPO_LETTURA));

        // ANAGRAFICA CODICI IVA
        this.menAnagTipiIva.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_CODICI_IVA, Permesso.PERMESSO_TIPO_LETTURA));

        // ALTRE ANAGRAFICHE
        this.menAnagAzienda.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagBancAbi.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagDatiAzieBanc.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagVettori.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagPorti.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagCausaliTrasporto.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagraficaStatiOrdine.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));
        menAnagAspettoEsterioreBeni.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));
        menAnagConsegna.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));
        menAnagScarico.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));
        menAnagComuni.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));
        menAnagNazioni.setEnabled(utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ALTRE, Permesso.PERMESSO_TIPO_LETTURA));

        // IMPOSTAZIONI -- LETTURA
        this.menUtilImpo.setEnabled(utente.getPermesso(Permesso.PERMESSO_IMPOSTAZIONI, Permesso.PERMESSO_TIPO_LETTURA));
        this.btnPlugins.setEnabled(utente.getPermesso(Permesso.PERMESSO_IMPOSTAZIONI, Permesso.PERMESSO_TIPO_LETTURA));

        // IMPOSTAZIONI -- SCRITTURA
        this.menUtilRestore.setEnabled(utente.getPermesso(Permesso.PERMESSO_IMPOSTAZIONI, Permesso.PERMESSO_TIPO_SCRITTURA));
        this.menUtilRestoreOnline.setEnabled(utente.getPermesso(Permesso.PERMESSO_IMPOSTAZIONI, Permesso.PERMESSO_TIPO_SCRITTURA));
        this.menUtilCheck.setEnabled(utente.getPermesso(Permesso.PERMESSO_IMPOSTAZIONI, Permesso.PERMESSO_TIPO_SCRITTURA));
        this.menUtilCambiaPassword.setEnabled(utente.getPermesso(Permesso.PERMESSO_IMPOSTAZIONI, Permesso.PERMESSO_TIPO_SCRITTURA));
        this.menUtilPlugins.setEnabled(utente.getPermesso(Permesso.PERMESSO_IMPOSTAZIONI, Permesso.PERMESSO_TIPO_SCRITTURA));
        this.menUtilDb.setEnabled(utente.getPermesso(Permesso.PERMESSO_IMPOSTAZIONI, Permesso.PERMESSO_TIPO_SCRITTURA));
        this.menManutezione.setEnabled(utente.getPermesso(Permesso.PERMESSO_IMPOSTAZIONI, Permesso.PERMESSO_TIPO_SCRITTURA));
        this.menFunzioniManutenzione.setEnabled(utente.getPermesso(Permesso.PERMESSO_IMPOSTAZIONI, Permesso.PERMESSO_TIPO_SCRITTURA));

        // GESTIONE UTENTI
//        this.menGestUtenti.setEnabled(utente.getPermesso(Permesso.PERMESSO_GESTIONE_UTENTI, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagraficaUsers.setEnabled(utente.getPermesso(Permesso.PERMESSO_GESTIONE_UTENTI, Permesso.PERMESSO_TIPO_LETTURA));
        this.menAnagraficaRuoli.setEnabled(utente.getPermesso(Permesso.PERMESSO_GESTIONE_UTENTI, Permesso.PERMESSO_TIPO_LETTURA));

        // GESTIONE MAGAZZINO
        this.menMagazzino.setEnabled(utente.getPermesso(Permesso.PERMESSO_MAGAZZINO, Permesso.PERMESSO_TIPO_LETTURA));

        // LETTURA STATISTICHE
        this.menStatistiche.setEnabled(utente.getPermesso(Permesso.PERMESSO_STATISTICHE, Permesso.PERMESSO_TIPO_LETTURA));

        // GESTIONE AGENTI
        this.menAgenti.setEnabled(utente.getPermesso(Permesso.PERMESSO_AGENTI, Permesso.PERMESSO_TIPO_LETTURA));

        // GESTIONE PREVENTIVI E ORDINI DI VENDITA
        this.menGestOrdini.setEnabled(utente.getPermesso(Permesso.PERMESSO_ORDINI_VENDITA, Permesso.PERMESSO_TIPO_LETTURA));
        this.butOrdini.setEnabled(utente.getPermesso(Permesso.PERMESSO_ORDINI_VENDITA, Permesso.PERMESSO_TIPO_LETTURA));

        // GESTIONE DDT DI VENDITA
        this.menGestDdt.setEnabled(utente.getPermesso(Permesso.PERMESSO_DDT_VENDITA, Permesso.PERMESSO_TIPO_LETTURA));
        this.butDdt.setEnabled(utente.getPermesso(Permesso.PERMESSO_DDT_VENDITA, Permesso.PERMESSO_TIPO_LETTURA));

        // GESTIONE FATTURE DI VENDITA
        this.menGestFatture.setEnabled(utente.getPermesso(Permesso.PERMESSO_FATTURE_VENDITA, Permesso.PERMESSO_TIPO_LETTURA));
        this.butFatture.setEnabled(utente.getPermesso(Permesso.PERMESSO_FATTURE_VENDITA, Permesso.PERMESSO_TIPO_LETTURA));

        // GESTIONE PREVENTIVI E ORDINI ACQUISTO
        this.menGestOrdiniAcquisto.setEnabled(utente.getPermesso(Permesso.PERMESSO_ORDINI_ACQUISTO, Permesso.PERMESSO_TIPO_LETTURA));
        this.butOrdiniAcquisto.setEnabled(utente.getPermesso(Permesso.PERMESSO_ORDINI_ACQUISTO, Permesso.PERMESSO_TIPO_LETTURA));

        // GESTIONE DDT DI ACQUISTO
        this.menGestDdtAcquisto.setEnabled(utente.getPermesso(Permesso.PERMESSO_DDT_ACQUISTO, Permesso.PERMESSO_TIPO_LETTURA));
        this.butDdtAcquisto.setEnabled(utente.getPermesso(Permesso.PERMESSO_DDT_ACQUISTO, Permesso.PERMESSO_TIPO_LETTURA));

        // GESTIONE FATTURE DI ACQUISTO
        this.menGestFattureAcquisto.setEnabled(utente.getPermesso(Permesso.PERMESSO_FATTURE_ACQUISTO, Permesso.PERMESSO_TIPO_LETTURA));
        this.butFattureAcquisto.setEnabled(utente.getPermesso(Permesso.PERMESSO_FATTURE_ACQUISTO, Permesso.PERMESSO_TIPO_LETTURA));

        // GESTIONE SCADENZARIO
        this.menGestRistDistRiba.setEnabled(utente.getPermesso(Permesso.PERMESSO_SCADENZARIO, Permesso.PERMESSO_TIPO_LETTURA));
        this.menGestSituClie.setEnabled(utente.getPermesso(Permesso.PERMESSO_SCADENZARIO, Permesso.PERMESSO_TIPO_LETTURA));
        this.butSituClie.setEnabled(utente.getPermesso(Permesso.PERMESSO_SCADENZARIO, Permesso.PERMESSO_TIPO_LETTURA));

        // GESTIONE IVA
        this.menIva.setEnabled(utente.getPermesso(Permesso.PERMESSO_GESTIONE_IVA, Permesso.PERMESSO_TIPO_LETTURA));

    }

    private void initAzioni() {
        Action a = new AbstractAction("installa pluginEmail") {
            public void actionPerformed(ActionEvent e) {
                Thread t0 = new Thread("azione installa pluginEmail") {
                    public void run() {
                        btnPluginsActionPerformed(null);
                    }
                };
                t0.start();

                Thread t = new Thread("azione installa pluginEmail") {
                    public void run() {
                        JDialogPlugins dialog = null;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            return;
                        }
                        for (int i = 0; i < 5; i++) {
                            if (main.dialogPlugins != null) {
                                dialog = main.dialogPlugins;
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                                return;
                            }
                        }
                        if (dialog != null) {
                            boolean ok1 = false;
                            for (int i = 0; i < 10; i++) {
                                if (!dialog.loading.isVisible()) {
                                    ok1 = true;
                                    break;
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                    return;
                                }
                            }
                            if (ok1) {
                                //scorro la tabella per il pluginEmail
                                JTable t = dialog.tabPlugins;
                                for (int i = 0; i < t.getRowCount(); i++) {
                                    try {
                                        String nome = (String) t.getValueAt(i, 0);
                                        if (nome.equalsIgnoreCase("pluginEmail")) {
                                            t.getSelectionModel().setSelectionInterval(i, i);
                                            boolean installato = (Boolean) t.getValueAt(i, t.getColumn("Installato").getModelIndex());
                                            if (!installato) {
                                                ActionEvent e = new ActionEvent(this, 1, null);
                                                dialog.actionAttivaPlugin.actionPerformed(e);
                                            } else {
                                                SwingUtils.showInfoMessage(main.getPadreWindow(), "Il plugin è già installato");
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Thread.dumpStack();
                            }
                        } else {
                            Thread.dumpStack();
                        }
                    }
                };
                t.start();

            }
        };

        azioni.put("installa pluginEmail", a);
        azioni.put("apri note rilascio", new AbstractAction("installa pluginEmail") {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        visualizzaNote();
                    }
                });
            }
        });

    }

    public void postInit() {
        System.out.println("post init");
//        getFrame().pack();
        getFrame().setSize(1024, 600);

        if (Toolkit.getDefaultToolkit().isFrameStateSupported(java.awt.Frame.MAXIMIZED_BOTH)) {
            getFrame().setMaximizedBounds(null);
            getFrame().setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        } else {
            System.out.println("Maximized non supportato!");
            Rectangle r = InvoicexUtil.getDesktopSize();
            Point topleft = InvoicexUtil.getDesktopTopLeft();
            getFrame().setBounds(topleft.x, topleft.y, r.width, r.height);
        }

        //this.menBar.add(new WindowMenu(this.desktop));
        try {
            getFrame().setIconImage(main.getLogoIcon());
        } catch (Exception err) {
            err.printStackTrace();
        }

        if (main.isPos()) {
            System.out.println("non attivato menu per pos in accesso_utenti");
            panBarr.setVisible(false);
            panBarr2.setVisible(false);
            menBar.setVisible(false);
            menBar.setEnabled(false);
        }

        //controllo versione
        try {
            main.versione = main.fileIni.getValue("cache", "versione", "Base");
            main.getPadreFrame().aggiornaTitle();
            Thread tv = new Thread("check_ver") {
                @Override
                public void run() {
                    try {
                        //ricontrollare versione (TODO)
                        String oldv = main.versione;
                        String vl = main.version + " (" + main.build + ")";

                        String ver = null;
                        try {
                            ver = cu.toString(DbUtils.getObject(Db.getConn(), "select versione from attivazione"));
                            if (StringUtils.isNotBlank(ver)) {
                                ver = StringUtils.remove(ver, "Invoicex ");
                            }
                        } catch (Exception e) {
                            if (!e.getMessage().startsWith("record non trovato")) {
                                e.printStackTrace();
                            }
                        }

                        System.out.println("ver = " + ver);
                        if (ver != null && ver.length() > 50) {
                            System.out.println("ignoro problema su server:\n");
                            System.out.println("ver = " + ver);
                        } else {
                            if (ver != null && ver.length() > 0) {
                                main.versione = ver;
                                main.fileIni.setValue("cache", "versione", ver);
                            } else {
                                main.versione = "Base";
                                main.fileIni.setValue("cache", "versione", "Base");
                            }
                            System.out.println("main.versione = " + main.versione);
                            boolean controllato = false;
                            if (!main.versione.equalsIgnoreCase("Base")) {
                                main.fileIni.setValue("cache", "versioneu", main.versione);
                                if (!main.getPersonalContain("no_controllo_plugin_auto")) {
                                    RiscattaAcquisto.controllaPluginPerVersione(main.versione);
                                    controllato = true;
                                }
                            }
                            //faccio controllo con versione delle icone salvata nel txt
                            File logovertxt = new File("img/logover.txt");
                            boolean controllologovertxt = false;
                            String versionetxt = null;
                            if (logovertxt.exists()) {
                                try {
                                    FileReader frlogovertxt = new FileReader(logovertxt);
                                    versionetxt = IOUtils.toString(frlogovertxt);
                                    frlogovertxt.close();
                                    if (versionetxt != null && main.versione != null && main.versione.equals(versionetxt)) {
                                        controllologovertxt = true;     //sono uguali
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (!controllato && !controllologovertxt) {
                                RiscattaAcquisto.cambiaIcone(main.versione);
                            }

                            if (!oldv.equals(main.versione)) {
                                main.getPadreFrame().aggiornaTitle();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            tv.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //TODO
        try {
            main.getPadreFrame().aggiornaTitle();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menBar = new MyJMenuBar();
        menAnagrafiche = new javax.swing.JMenu();
        menAnagAzienda = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        menAnagClie = new javax.swing.JMenuItem();
        menAnagCatCli = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        menAnagArti = new javax.swing.JMenuItem();
        menAnagCategorieArticoli = new javax.swing.JMenuItem();
        menAnagSottoCategorieArticoli = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        menAnagTipiIva = new javax.swing.JMenuItem();
        menAnagBancAbi = new javax.swing.JMenuItem();
        menAnagDatiAzieBanc = new javax.swing.JMenuItem();
        menAnagListini = new javax.swing.JMenuItem();
        menAnagTipiPaga = new javax.swing.JMenuItem();
        menAnagraficaStatiOrdine = new javax.swing.JMenuItem();
        menAnagAspettoEsterioreBeni = new javax.swing.JMenuItem();
        menAnagCausaliTrasporto = new javax.swing.JMenuItem();
        menAnagConsegna = new javax.swing.JMenuItem();
        menAnagScarico = new javax.swing.JMenuItem();
        menAnagPorti = new javax.swing.JMenuItem();
        menAnagVettori = new javax.swing.JMenuItem();
        menAnagComuni = new javax.swing.JMenuItem();
        menAnagNazioni = new javax.swing.JMenuItem();
        menGest = new javax.swing.JMenu();
        menDocVendita = new javax.swing.JMenu();
        menGestOrdini = new javax.swing.JMenuItem();
        menGestDdt = new javax.swing.JMenuItem();
        menGestFatture = new javax.swing.JMenuItem();
        menDocAcquisto = new javax.swing.JMenu();
        menGestOrdiniAcquisto = new javax.swing.JMenuItem();
        menGestDdtAcquisto = new javax.swing.JMenuItem();
        menGestFattureAcquisto = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        menGestSituClie = new javax.swing.JMenuItem();
        menGestRistDistRiba = new javax.swing.JMenuItem();
        menGestImpegnato = new javax.swing.JMenuItem();
        menGestImpegnatoProduttore = new javax.swing.JMenuItem();
        menGestEvadibileCliente = new javax.swing.JMenuItem();
        menContab = new javax.swing.JMenu();
        menMagazzino = new javax.swing.JMenu();
        menMagazzinoMovimenti = new javax.swing.JMenuItem();
        menMagazzinoStampaMovimenti = new javax.swing.JMenuItem();
        menMagazzinoStampaGiacenze = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        menMagazzinoStampaGiacenzeMatricole = new javax.swing.JMenuItem();
        menMagazzinoStampaGiacenzeLotti = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        menMagazzinoUltimiPrezzi = new javax.swing.JMenuItem();
        menMagazzinoOrdinato = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        menMagazzinoDepositi = new javax.swing.JMenuItem();
        menMagazzinoDepositoStandard = new javax.swing.JMenuItem();
        menIva = new javax.swing.JMenu();
        menGestioneIvaStampaRegistro = new javax.swing.JMenuItem();
        menStatistiche = new javax.swing.JMenu();
        menStatisticheOrdBolFat = new javax.swing.JMenuItem();
        menExpAcqVend = new javax.swing.JMenuItem();
        menTotFatture = new javax.swing.JMenuItem();
        menExpVendPeroni = new javax.swing.JMenuItem();
        menStatChiCosa = new javax.swing.JMenuItem();
        menExpElencoDdt = new javax.swing.JMenuItem();
        menUtil = new javax.swing.JMenu();
        menUtilImpo = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        menUtilAggi = new javax.swing.JMenuItem();
        sepAggBackup = new javax.swing.JPopupMenu.Separator();
        menUtilBackup = new javax.swing.JMenuItem();
        menUtilRestore = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        menUtilBackupOnline = new javax.swing.JMenuItem();
        menUtilRestoreOnline = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        menBackupDropbox = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        menUtilCheck = new javax.swing.JMenuItem();
        menUtilCambiaFont = new javax.swing.JMenuItem();
        menUtilCambiaFont.setVisible(false);
        menUtilPlugins = new javax.swing.JMenuItem();
        menUtilCambiaPassword = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        menUtilDb = new javax.swing.JMenuItem();
        menManutezione = new javax.swing.JMenuItem();
        menFunzioniManutenzione = new javax.swing.JCheckBoxMenuItem();
        menAgenti = new javax.swing.JMenu();
        menAgentiAnagrafica = new javax.swing.JMenuItem();
        menAgentiSituazione = new javax.swing.JMenuItem();
        menAchievo = new javax.swing.JMenu();
        menAchievo.setVisible(false);
        menAchievoOre = new javax.swing.JMenuItem();
        menFine = new javax.swing.JMenu();
        //menFine.setVisible(false);
        menAiuto = new javax.swing.JMenu();
        condivisione = new javax.swing.JMenuItem();
        menGuida = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        menVnc = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        menAiutoInfo = new javax.swing.JMenuItem();
        menAiutoInfoLicenza = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        menTestInternet = new javax.swing.JMenuItem();
        menSnj = new javax.swing.JMenu();
        menAnagraficaEmissioneFatture = new javax.swing.JMenuItem();
        menAnagraficaDurataContratto = new javax.swing.JMenuItem();
        menAnagraficaDurataConsulenza = new javax.swing.JMenuItem();
        menGestUtenti = new javax.swing.JMenu();
        menAnagraficaUsers = new javax.swing.JMenuItem();
        menAnagraficaRuoli = new javax.swing.JMenuItem();
        menCambiaPassUtente = new javax.swing.JMenuItem();
        panBarr = new javax.swing.JPanel();
        panBarr.setVisible(true);
        popbarr = new javax.swing.JPopupMenu();
        popnascondi = new javax.swing.JCheckBoxMenuItem();
        popMenuFrame = new javax.swing.JPopupMenu();
        popMenuFrameSalvaDimensioni = new javax.swing.JMenuItem();
        popMenuFrameResetDimensioni = new javax.swing.JMenuItem();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        panSfuma = new MyJPanel();
        panBarr2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        butOrdini = new javax.swing.JButton();
        butDdt = new javax.swing.JButton();
        butFatture = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel2 = new javax.swing.JLabel();
        butOrdiniAcquisto = new javax.swing.JButton();
        butDdtAcquisto = new javax.swing.JButton();
        butFattureAcquisto = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        butClieForn = new javax.swing.JButton();
        butSituClie = new javax.swing.JButton();
        butArticoli = new javax.swing.JButton();
        btnPlugins = new javax.swing.JButton();
        butVnc = new javax.swing.JButton();
        butRiscattaAcquisto = new javax.swing.JButton();
        scrollDesktop = new javax.swing.JScrollPane();
        desktop = new javax.swing.JDesktopPane();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        lblInfoLoading2 = new org.jdesktop.swingx.JXBusyLabel();
        labmess2 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        sync = new javax.swing.JButton();
        frame_debug = new javax.swing.JInternalFrame();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        text_debug = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        autoUpdateTableSync = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabsync = new javax.swing.JTable();
        sync_wiz = new javax.swing.JButton();
        ricalcolo_totali_fatture = new javax.swing.JButton();

        menBar.setDoubleBuffered(true);

        menAnagrafiche.setText("Anagrafiche");

        menAnagAzienda.setText("Azienda");
        menAnagAzienda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagAziendaActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagAzienda);
        menAnagrafiche.add(jSeparator7);

        menAnagClie.setText("Clienti/Fornitori");
        menAnagClie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagClieActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagClie);

        menAnagCatCli.setText("Categorie Clienti/Fornitori");
        menAnagCatCli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagCatCliActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagCatCli);
        menAnagrafiche.add(jSeparator9);

        menAnagArti.setText("Articoli");
        menAnagArti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagArtiActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagArti);

        menAnagCategorieArticoli.setText("Categorie articoli");
        menAnagCategorieArticoli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagCategorieArticoliActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagCategorieArticoli);

        menAnagSottoCategorieArticoli.setText("Sottocategorie articoli");
        menAnagSottoCategorieArticoli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagSottoCategorieArticoliActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagSottoCategorieArticoli);
        menAnagrafiche.add(jSeparator8);

        menAnagTipiIva.setText("Codici IVA");
        menAnagTipiIva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagTipiIvaActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagTipiIva);

        menAnagBancAbi.setText("Banche e Filiali");
        menAnagBancAbi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagBancAbiActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagBancAbi);

        menAnagDatiAzieBanc.setText("Conti correnti aziendali");
        menAnagDatiAzieBanc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagDatiAzieBancActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagDatiAzieBanc);

        menAnagListini.setText("Listini");
        menAnagListini.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagListiniActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagListini);

        menAnagTipiPaga.setText("Pagamenti");
        menAnagTipiPaga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagTipiPagaActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagTipiPaga);

        menAnagraficaStatiOrdine.setText("Stati Preventivi/Ordini");
        menAnagraficaStatiOrdine.setActionCommand("Anagrafica Durata Contratto");
        menAnagraficaStatiOrdine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagraficaStatiOrdineActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagraficaStatiOrdine);

        menAnagAspettoEsterioreBeni.setText("Aspetto esteriore dei beni");
        menAnagAspettoEsterioreBeni.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagAspettoEsterioreBeniActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagAspettoEsterioreBeni);

        menAnagCausaliTrasporto.setText("Causali di Trasporto");
        menAnagCausaliTrasporto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagCausaliTrasportoActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagCausaliTrasporto);

        menAnagConsegna.setText("Modalità di consegna");
        menAnagConsegna.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagConsegnaActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagConsegna);

        menAnagScarico.setText("Modalità di scarico");
        menAnagScarico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagScaricoActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagScarico);

        menAnagPorti.setText("Porti");
        menAnagPorti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagPortiActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagPorti);

        menAnagVettori.setText("Vettori");
        menAnagVettori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagVettoriActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagVettori);

        menAnagComuni.setText("Comuni");
        menAnagComuni.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagComuniActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagComuni);

        menAnagNazioni.setText("Nazioni");
        menAnagNazioni.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagNazioniActionPerformed(evt);
            }
        });
        menAnagrafiche.add(menAnagNazioni);

        menBar.add(menAnagrafiche);

        menGest.setText("Gestione");
        menGest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestActionPerformed(evt);
            }
        });

        menDocVendita.setText("Documenti di Vendita");

        menGestOrdini.setText("Preventivi e Ordini");
        menGestOrdini.setToolTipText("");
        menGestOrdini.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestOrdiniActionPerformed(evt);
            }
        });
        menDocVendita.add(menGestOrdini);

        menGestDdt.setText("DDT");
        menGestDdt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestDdt_actionPerformed(evt);
            }
        });
        menDocVendita.add(menGestDdt);

        menGestFatture.setText("Fatture");
        menGestFatture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestFattureActionPerformed(evt);
            }
        });
        menDocVendita.add(menGestFatture);

        menGest.add(menDocVendita);

        menDocAcquisto.setText("Documenti di Acquisto");

        menGestOrdiniAcquisto.setText("Preventivi e Ordini");
        menGestOrdiniAcquisto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestOrdiniAcquistoActionPerformed(evt);
            }
        });
        menDocAcquisto.add(menGestOrdiniAcquisto);

        menGestDdtAcquisto.setText("DDT");
        menGestDdtAcquisto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestDdtAcquistoActionPerformed(evt);
            }
        });
        menDocAcquisto.add(menGestDdtAcquisto);

        menGestFattureAcquisto.setText("Fatture");
        menGestFattureAcquisto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestFattureAcquistoActionPerformed(evt);
            }
        });
        menDocAcquisto.add(menGestFattureAcquisto);

        menGest.add(menDocAcquisto);
        menGest.add(jSeparator5);

        menGestSituClie.setText("Scadenzario");
        menGestSituClie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestSituClieActionPerformed(evt);
            }
        });
        menGest.add(menGestSituClie);

        menGestRistDistRiba.setText("Ristampa distinte Ri.Ba.");
        menGestRistDistRiba.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestRistDistRibaActionPerformed(evt);
            }
        });
        menGest.add(menGestRistDistRiba);

        menGestImpegnato.setText("Report quantità impegnate per Fornitore");
        menGestImpegnato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestImpegnatoActionPerformed(evt);
            }
        });
        menGest.add(menGestImpegnato);

        menGestImpegnatoProduttore.setText("Report quantità impegnate per Produttore");
        menGestImpegnatoProduttore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestImpegnatoProduttoreActionPerformed(evt);
            }
        });
        menGest.add(menGestImpegnatoProduttore);

        menGestEvadibileCliente.setText("Report valore evadibile per Cliente");
        menGestEvadibileCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestEvadibileClienteActionPerformed(evt);
            }
        });
        menGest.add(menGestEvadibileCliente);

        menBar.add(menGest);

        menContab.setText("Contabilità");
        menBar.add(menContab);

        menMagazzino.setText("Magazzino");

        menMagazzinoMovimenti.setText("Gestione Movimenti");
        menMagazzinoMovimenti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menMagazzinoMovimentiActionPerformed(evt);
            }
        });
        menMagazzino.add(menMagazzinoMovimenti);

        menMagazzinoStampaMovimenti.setText("Stampa movimenti");
        menMagazzinoStampaMovimenti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menMagazzinoStampaMovimentiActionPerformed(evt);
            }
        });
        menMagazzino.add(menMagazzinoStampaMovimenti);

        menMagazzinoStampaGiacenze.setText("Giacenze");
        menMagazzinoStampaGiacenze.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menMagazzinoStampaGiacenzeActionPerformed(evt);
            }
        });
        menMagazzino.add(menMagazzinoStampaGiacenze);
        menMagazzino.add(jSeparator16);

        menMagazzinoStampaGiacenzeMatricole.setText("Giacenze per Matricole");
        menMagazzinoStampaGiacenzeMatricole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menMagazzinoStampaGiacenzeMatricoleActionPerformed(evt);
            }
        });
        menMagazzino.add(menMagazzinoStampaGiacenzeMatricole);

        menMagazzinoStampaGiacenzeLotti.setText("Giacenze per Lotti");
        menMagazzinoStampaGiacenzeLotti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menMagazzinoStampaGiacenzeLottiActionPerformed(evt);
            }
        });
        menMagazzino.add(menMagazzinoStampaGiacenzeLotti);
        menMagazzino.add(jSeparator6);

        menMagazzinoUltimiPrezzi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/ico_pro.png"))); // NOI18N
        menMagazzinoUltimiPrezzi.setText("Ultimi prezzi");
        menMagazzinoUltimiPrezzi.setToolTipText("Disponibile solo nelle versioni Professional ed Enterprise");
        menMagazzinoUltimiPrezzi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menMagazzinoUltimiPrezziActionPerformed(evt);
            }
        });
        menMagazzino.add(menMagazzinoUltimiPrezzi);

        menMagazzinoOrdinato.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/ico_pro.png"))); // NOI18N
        menMagazzinoOrdinato.setText("Report Ordinato");
        menMagazzinoOrdinato.setToolTipText("Disponibile solo nelle versioni Professional ed Enterprise");
        menMagazzinoOrdinato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menMagazzinoOrdinatoActionPerformed(evt);
            }
        });
        menMagazzino.add(menMagazzinoOrdinato);
        menMagazzino.add(jSeparator11);

        menMagazzinoDepositi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/ico_pro_plus.png"))); // NOI18N
        menMagazzinoDepositi.setText("Anagrafica Depositi");
        menMagazzinoDepositi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menMagazzinoDepositiActionPerformed(evt);
            }
        });
        menMagazzino.add(menMagazzinoDepositi);

        menMagazzinoDepositoStandard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/ico_pro_plus.png"))); // NOI18N
        menMagazzinoDepositoStandard.setText("Impostazione Deposito Predefinito");
        menMagazzinoDepositoStandard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menMagazzinoDepositoStandardActionPerformed(evt);
            }
        });
        menMagazzino.add(menMagazzinoDepositoStandard);

        menBar.add(menMagazzino);

        menIva.setText("Gestione IVA");

        menGestioneIvaStampaRegistro.setText("Stampa Registro Iva");
        menGestioneIvaStampaRegistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGestioneIvaStampaRegistroActionPerformed(evt);
            }
        });
        menIva.add(menGestioneIvaStampaRegistro);

        menBar.add(menIva);

        menStatistiche.setText("Statistiche");

        menStatisticheOrdBolFat.setText("Ordinato / Bollettato / Fatturato");
        menStatisticheOrdBolFat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menStatisticheOrdBolFatActionPerformed(evt);
            }
        });
        menStatistiche.add(menStatisticheOrdBolFat);

        menExpAcqVend.setText("Export Acquisti / Vendite");
        menExpAcqVend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menExpAcqVendActionPerformed1(evt);
            }
        });
        menStatistiche.add(menExpAcqVend);

        menTotFatture.setText("Totale Fatture");
        menTotFatture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menTotFattureActionPerformed(evt);
            }
        });
        menStatistiche.add(menTotFatture);

        menExpVendPeroni.setText("Export Vendite Peroni");
        menExpVendPeroni.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menExpVendPeroniActionPerformed1(evt);
            }
        });
        menStatistiche.add(menExpVendPeroni);

        menStatChiCosa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/ico_pro.png"))); // NOI18N
        menStatChiCosa.setText("Chi / Cosa");
        menStatChiCosa.setToolTipText("Disponibile solo nelle versioni Professional ed Enterprise");
        menStatChiCosa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menStatChiCosaActionPerformed(evt);
            }
        });
        menStatistiche.add(menStatChiCosa);

        menExpElencoDdt.setText("Elenco DDT");
        menExpElencoDdt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menExpElencoDdtActionPerformed(evt);
            }
        });
        menStatistiche.add(menExpElencoDdt);

        menBar.add(menStatistiche);

        menUtil.setText("Utilità");

        menUtilImpo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Gear-icon16.png"))); // NOI18N
        menUtilImpo.setText("Impostazioni");
        menUtilImpo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menUtilImpoActionPerformed(evt);
            }
        });
        menUtil.add(menUtilImpo);
        menUtil.add(jSeparator10);

        menUtilAggi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Download-icon16.png"))); // NOI18N
        menUtilAggi.setText("Controlla aggiornamenti Invoicex");
        menUtilAggi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menUtilAggiActionPerformed(evt);
            }
        });
        menUtil.add(menUtilAggi);
        menUtil.add(sepAggBackup);

        menUtilBackup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Data-Upload-icon16.png"))); // NOI18N
        menUtilBackup.setText("Copia di sicurezza");
        menUtilBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menUtilBackupActionPerformed(evt);
            }
        });
        menUtil.add(menUtilBackup);

        menUtilRestore.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Data-Download-icon16.png"))); // NOI18N
        menUtilRestore.setText("Recupero Database");
        menUtilRestore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menUtilRestoreActionPerformed(evt);
            }
        });
        menUtil.add(menUtilRestore);
        menUtil.add(jSeparator13);

        menUtilBackupOnline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Very-Basic-Upload-To-Cloud-icon16.png"))); // NOI18N
        menUtilBackupOnline.setText("Copia di sicurezza ed invio al server TNX");
        menUtilBackupOnline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menUtilBackupOnlineActionPerformed(evt);
            }
        });
        menUtil.add(menUtilBackupOnline);

        menUtilRestoreOnline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Very-Basic-Download-From-Cloud-icon16.png"))); // NOI18N
        menUtilRestoreOnline.setText("Recupero Database da server TNX");
        menUtilRestoreOnline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menUtilRestoreOnlineActionPerformed(evt);
            }
        });
        menUtil.add(menUtilRestoreOnline);
        menUtil.add(jSeparator14);

        menBackupDropbox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/pro_plus_ent_dropbox16.png"))); // NOI18N
        menBackupDropbox.setText("Backup e Ripristino con Dropbox");
        menBackupDropbox.setToolTipText("Disponibile solo nelle versioni Professional ed Enterprise");
        menBackupDropbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menBackupDropboxActionPerformed(evt);
            }
        });
        menUtil.add(menBackupDropbox);
        menUtil.add(jSeparator12);

        menUtilCheck.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Data-Yes-icon16.png"))); // NOI18N
        menUtilCheck.setText("Controlla integrita' dei dati");
        menUtilCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menUtilCheckActionPerformed(evt);
            }
        });
        menUtil.add(menUtilCheck);

        menUtilCambiaFont.setText("Imposta tipo e dimensione carattere");
        menUtilCambiaFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menUtilCambiaFontActionPerformed(evt);
            }
        });
        menUtil.add(menUtilCambiaFont);

        menUtilPlugins.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Box-Open-icon16.png"))); // NOI18N
        menUtilPlugins.setText("Plugins");
        menUtilPlugins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menUtilPluginsActionPerformed(evt);
            }
        });
        menUtil.add(menUtilPlugins);

        menUtilCambiaPassword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Lock-2-icon16.png"))); // NOI18N
        menUtilCambiaPassword.setText("Cambia password Database");
        menUtilCambiaPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menUtilCambiaPasswordActionPerformed(evt);
            }
        });
        menUtil.add(menUtilCambiaPassword);
        menUtil.add(jSeparator3);

        menUtilDb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Data-Database-icon16.png"))); // NOI18N
        menUtilDb.setText("Database");
        menUtilDb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menUtilDbActionPerformed(evt);
            }
        });
        menUtil.add(menUtilDb);

        menManutezione.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Settings Wrench-16.png"))); // NOI18N
        menManutezione.setText("Manutenzione");
        menManutezione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menManutezioneActionPerformed(evt);
            }
        });
        menUtil.add(menManutezione);

        menFunzioniManutenzione.setText("Funzioni di manutenzione");
        menFunzioniManutenzione.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Stethoscope-16.png"))); // NOI18N
        menFunzioniManutenzione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menFunzioniManutenzioneActionPerformed(evt);
            }
        });
        menUtil.add(menFunzioniManutenzione);

        menBar.add(menUtil);

        menAgenti.setText("Agenti");
        //if (!(main.getPersonal().equalsIgnoreCase(main.PERSONAL_TLZ))) this.menAgenti.setVisible(false);
        //if (main.getPersonal().equalsIgnoreCase(main.PERSONAL_TLZ)
            //      || main.getPersonal().equalsIgnoreCase(main.PERSONAL_GIANNI)) {
            //  this.menAgenti.setVisible(true);
            //} else {
            //  this.menAgenti.setVisible(false);
            //}

        menAgentiAnagrafica.setText("Anagrafica Agenti");
        menAgentiAnagrafica.setName("anagrafica_agenti"); // NOI18N
        menAgentiAnagrafica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAgentiAnagraficaActionPerformed(evt);
            }
        });
        menAgenti.add(menAgentiAnagrafica);

        menAgentiSituazione.setText("Situazione Agenti");
        menAgentiSituazione.setToolTipText("");
        menAgentiSituazione.setName("situazione_agenti"); // NOI18N
        menAgentiSituazione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAgentiSituazioneActionPerformed(evt);
            }
        });
        menAgenti.add(menAgentiSituazione);

        menBar.add(menAgenti);

        menAchievo.setText("Achievo");

        menAchievoOre.setText("Ore Achievo");
        menAchievoOre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAchievoOreActionPerformed(evt);
            }
        });
        menAchievo.add(menAchievoOre);

        menBar.add(menAchievo);

        menFine.setText("Finestre");
        menFine.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menFineMenuSelected(evt);
            }
        });
        menFine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menFineActionPerformed(evt);
            }
        });
        menBar.add(menFine);

        menAiuto.setText("Aiuto");

        condivisione.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Group-16.png"))); // NOI18N
        condivisione.setText("Condivisione Desktop");
        condivisione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                condivisioneActionPerformed(evt);
            }
        });
        menAiuto.add(condivisione);

        menGuida.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/PDF-16.png"))); // NOI18N
        menGuida.setText("Manuale di Invoicex");
        menGuida.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menGuidaActionPerformed(evt);
            }
        });
        menAiuto.add(menGuida);

        jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Faq-16.png"))); // NOI18N
        jMenuItem3.setText("Domande frequenti");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        menAiuto.add(jMenuItem3);

        menVnc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Question-16.png"))); // NOI18N
        menVnc.setText("Richiedi Assistenza");
        menVnc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menVncActionPerformed(evt);
            }
        });
        menAiuto.add(menVnc);
        menAiuto.add(jSeparator4);

        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Purchase Order-16.png"))); // NOI18N
        jMenuItem4.setText("Note di rilascio");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        menAiuto.add(jMenuItem4);

        menAiutoInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Help-16.png"))); // NOI18N
        menAiutoInfo.setText("Informazioni sul programma");
        menAiutoInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAiutoInfoActionPerformed(evt);
            }
        });
        menAiuto.add(menAiutoInfo);

        menAiutoInfoLicenza.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Info-16.png"))); // NOI18N
        menAiutoInfoLicenza.setText("Informazioni sulla tua licenza");
        menAiutoInfoLicenza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAiutoInfoLicenzaActionPerformed(evt);
            }
        });
        menAiuto.add(menAiutoInfoLicenza);
        menAiuto.add(jSeparator15);

        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Opened Folder-16.png"))); // NOI18N
        jMenuItem1.setText("Apri cartella dati utente");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        menAiuto.add(jMenuItem1);

        jMenuItem7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Opened Folder-16.png"))); // NOI18N
        jMenuItem7.setText("Apri cartella programma");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        menAiuto.add(jMenuItem7);

        menTestInternet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Speedometer-16.png"))); // NOI18N
        menTestInternet.setText("Test velocità internet");
        menAiuto.add(menTestInternet);

        menBar.add(menAiuto);

        menSnj.setText("SNJ");

        menAnagraficaEmissioneFatture.setText("Anagrafica Emissione Fatture");
        menAnagraficaEmissioneFatture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagraficaEmissioneFattureActionPerformed(evt);
            }
        });
        menSnj.add(menAnagraficaEmissioneFatture);

        menAnagraficaDurataContratto.setText("Anagrafica Durata Contratto");
        menAnagraficaDurataContratto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagraficaDurataContrattoActionPerformed(evt);
            }
        });
        menSnj.add(menAnagraficaDurataContratto);

        menAnagraficaDurataConsulenza.setText("Anagrafica Durata Consulenza");
        menAnagraficaDurataConsulenza.setActionCommand("Anagrafica Durata Contratto");
        menAnagraficaDurataConsulenza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagraficaDurataConsulenzaActionPerformed(evt);
            }
        });
        menSnj.add(menAnagraficaDurataConsulenza);

        menBar.add(menSnj);

        menGestUtenti.setText("Gestione Utenti");

        menAnagraficaUsers.setText("Anagrafica Utenti");
        menAnagraficaUsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagraficaUsersActionPerformed(evt);
            }
        });
        menGestUtenti.add(menAnagraficaUsers);

        menAnagraficaRuoli.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/ico_pro.png"))); // NOI18N
        menAnagraficaRuoli.setText("Anagrafica Tipi Utente");
        menAnagraficaRuoli.setToolTipText("Disponibile solo nelle versioni Professional ed Enterprise");
        menAnagraficaRuoli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAnagraficaRuoliActionPerformed(evt);
            }
        });
        menGestUtenti.add(menAnagraficaRuoli);

        menCambiaPassUtente.setText("Cambia Password");
        menCambiaPassUtente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menCambiaPassUtenteActionPerformed(evt);
            }
        });
        menGestUtenti.add(menCambiaPassUtente);

        menBar.add(menGestUtenti);

        panBarr.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panBarrComponentResized(evt);
            }
        });
        panBarr.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                panBarrComponentAdded(evt);
            }
        });
        panBarr.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                panBarrKeyPressed(evt);
            }
        });
        panBarr.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        popnascondi.setSelected(true);
        popnascondi.setText("Nascondi automaticamente");
        popnascondi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popnascondiActionPerformed(evt);
            }
        });
        popbarr.add(popnascondi);

        popMenuFrameSalvaDimensioni.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Surface-check-16.png"))); // NOI18N
        popMenuFrameSalvaDimensioni.setText("Salva dimensioni finestra");
        popMenuFrameSalvaDimensioni.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popMenuFrameSalvaDimensioniActionPerformed(evt);
            }
        });
        popMenuFrame.add(popMenuFrameSalvaDimensioni);

        popMenuFrameResetDimensioni.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Surface-delete-16.png"))); // NOI18N
        popMenuFrameResetDimensioni.setText("Reset dimensioni finestra");
        popMenuFrameResetDimensioni.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popMenuFrameResetDimensioniActionPerformed(evt);
            }
        });
        popMenuFrame.add(popMenuFrameResetDimensioni);

        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        jLayeredPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jLayeredPane1ComponentResized(evt);
            }
        });
        jLayeredPane1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jLayeredPane1MouseMoved(evt);
            }
        });

        panSfuma.setOpaque(false);
        panSfuma.setLayout(null);
        jLayeredPane1.setLayer(panSfuma, javax.swing.JLayeredPane.POPUP_LAYER);
        jLayeredPane1.add(panSfuma);
        panSfuma.setBounds(0, 0, 455, 10);

        panBarr2.setComponentPopupMenu(popbarr);
        panBarr2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                panBarr2MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panBarr2MousePressed(evt);
            }
        });
        panBarr2.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                panBarr2ComponentMoved(evt);
            }
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panBarr2ComponentResized(evt);
            }
        });
        panBarr2.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                panBarr2ComponentAdded(evt);
            }
        });
        panBarr2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 4));

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()-1f));
        jLabel1.setText("Vendita");
        panBarr2.add(jLabel1);

        butOrdini.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/document-new-vendita.png"))); // NOI18N
        butOrdini.setText("PREV. e ORDINI");
        butOrdini.setToolTipText("di Vendita");
        butOrdini.setFocusable(false);
        butOrdini.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        butOrdini.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butOrdini.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butOrdini.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butOrdiniActionPerformed(evt);
            }
        });
        panBarr2.add(butOrdini);

        butDdt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/document-new-vendita.png"))); // NOI18N
        butDdt.setText("DDT");
        butDdt.setToolTipText("di Vendita");
        butDdt.setFocusable(false);
        butDdt.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        butDdt.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butDdt.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butDdt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDdtActionPerformed(evt);
            }
        });
        panBarr2.add(butDdt);

        butFatture.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/document-new-vendita.png"))); // NOI18N
        butFatture.setText("FATTURE");
        butFatture.setToolTipText("di Vendita");
        butFatture.setFocusable(false);
        butFatture.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        butFatture.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butFatture.setName("fatture_vendita"); // NOI18N
        butFatture.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butFatture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butFattureActionPerformed(evt);
            }
        });
        panBarr2.add(butFatture);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(6, 30));
        panBarr2.add(jSeparator1);

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getSize()-1f));
        jLabel2.setText("Acquisti");
        panBarr2.add(jLabel2);

        butOrdiniAcquisto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/document-new-acquista.png"))); // NOI18N
        butOrdiniAcquisto.setText("PREV. e ORDINI");
        butOrdiniAcquisto.setToolTipText("di Acquisto");
        butOrdiniAcquisto.setFocusable(false);
        butOrdiniAcquisto.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        butOrdiniAcquisto.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butOrdiniAcquisto.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butOrdiniAcquisto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butOrdiniAcquistoActionPerformed(evt);
            }
        });
        panBarr2.add(butOrdiniAcquisto);

        butDdtAcquisto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/document-new-acquista.png"))); // NOI18N
        butDdtAcquisto.setText("DDT");
        butDdtAcquisto.setToolTipText("di Acquisto");
        butDdtAcquisto.setFocusable(false);
        butDdtAcquisto.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        butDdtAcquisto.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butDdtAcquisto.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butDdtAcquisto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDdtAcquistoActionPerformed(evt);
            }
        });
        panBarr2.add(butDdtAcquisto);

        butFattureAcquisto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/document-new-acquista.png"))); // NOI18N
        butFattureAcquisto.setText("FATTURE");
        butFattureAcquisto.setToolTipText("di Acquisto");
        butFattureAcquisto.setFocusable(false);
        butFattureAcquisto.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        butFattureAcquisto.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butFattureAcquisto.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butFattureAcquisto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butFattureAcquistoActionPerformed(evt);
            }
        });
        panBarr2.add(butFattureAcquisto);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setPreferredSize(new java.awt.Dimension(6, 30));
        panBarr2.add(jSeparator2);

        butClieForn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/contact-new.png"))); // NOI18N
        butClieForn.setText("CLIENTI/FORNITORI");
        butClieForn.setFocusable(false);
        butClieForn.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        butClieForn.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butClieForn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butClieForn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butClieFornActionPerformed(evt);
            }
        });
        panBarr2.add(butClieForn);

        butSituClie.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/scadenzario2.png"))); // NOI18N
        butSituClie.setText("SCADENZARIO");
        butSituClie.setFocusable(false);
        butSituClie.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        butSituClie.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butSituClie.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butSituClie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSituClieActionPerformed(evt);
            }
        });
        panBarr2.add(butSituClie);

        butArticoli.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/categories/applications-office.png"))); // NOI18N
        butArticoli.setText("ARTICOLI");
        butArticoli.setFocusable(false);
        butArticoli.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        butArticoli.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butArticoli.setName("articoli"); // NOI18N
        butArticoli.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butArticoli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butArticoliActionPerformed(evt);
            }
        });
        panBarr2.add(butArticoli);

        btnPlugins.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/mimetypes/package-x-generic.png"))); // NOI18N
        btnPlugins.setText("Plugins");
        btnPlugins.setFocusable(false);
        btnPlugins.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnPlugins.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnPlugins.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnPlugins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPluginsActionPerformed(evt);
            }
        });
        panBarr2.add(btnPlugins);

        butVnc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/tnx_ar.png"))); // NOI18N
        butVnc.setText("Richiedi Assistenza");
        butVnc.setFocusable(false);
        butVnc.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        butVnc.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butVnc.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butVnc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butVncActionPerformed(evt);
            }
        });
        panBarr2.add(butVnc);

        butRiscattaAcquisto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/mimetypes/application-certificate.png"))); // NOI18N
        butRiscattaAcquisto.setText("Attivazione");
        butRiscattaAcquisto.setFocusable(false);
        butRiscattaAcquisto.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        butRiscattaAcquisto.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butRiscattaAcquisto.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butRiscattaAcquisto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butRiscattaAcquistoActionPerformed(evt);
            }
        });
        panBarr2.add(butRiscattaAcquisto);

        jLayeredPane1.setLayer(panBarr2, javax.swing.JLayeredPane.POPUP_LAYER);
        jLayeredPane1.add(panBarr2);
        panBarr2.setBounds(0, 0, 1184, 38);

        scrollDesktop.setBorder(null);
        scrollDesktop.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDesktop.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        desktop.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                desktopComponentResized(evt);
            }
        });

        jButton4.setText("robot");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        desktop.add(jButton4);
        jButton4.setBounds(20, 80, 70, 20);

        jButton5.setText("res");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        desktop.add(jButton5);
        jButton5.setBounds(20, 10, 70, 20);

        jButton6.setText("mem");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        desktop.add(jButton6);
        jButton6.setBounds(20, 50, 70, 20);

        lblInfoLoading2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        lblInfoLoading2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblInfoLoading2.setText("...");
        lblInfoLoading2.setBusy(true);
        lblInfoLoading2.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        desktop.add(lblInfoLoading2);
        lblInfoLoading2.setBounds(-500, 25, 500, 40);

        labmess2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labmess2.setText("labmess2");
        labmess2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labmess2.setName("labmess2"); // NOI18N
        desktop.add(labmess2);
        labmess2.setBounds(685, 45, 70, 20);

        jButton7.setText("risoluzione 1024x600");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        desktop.add(jButton7);
        jButton7.setBounds(20, 110, 220, 20);

        jButton1.setText("test vari");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        desktop.add(jButton1);
        jButton1.setBounds(20, 140, 220, 23);

        sync.setText("sync");
        sync.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncActionPerformed(evt);
            }
        });
        desktop.add(sync);
        sync.setBounds(20, 260, 73, 23);

        frame_debug.setClosable(true);
        frame_debug.setIconifiable(true);
        frame_debug.setResizable(true);
        frame_debug.setVisible(true);

        jSplitPane1.setDividerLocation(100);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        text_debug.setColumns(20);
        text_debug.setRows(1);
        jScrollPane1.setViewportView(text_debug);

        jSplitPane1.setLeftComponent(jScrollPane1);

        autoUpdateTableSync.setSelected(true);
        autoUpdateTableSync.setText("Auto");

        jButton2.setText("aggiorna");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        tabsync.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "id", "id_master", "sync_date", "client_id", "type", "table_name", "field_name", "primary_field", "primary_value", "old_value", "new_value"
            }
        ));
        jScrollPane2.setViewportView(tabsync);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(autoUpdateTableSync)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton2)
                .add(167, 167, 167))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 752, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton2)
                    .add(autoUpdateTableSync))
                .add(5, 5, 5)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel1);

        frame_debug.getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        desktop.add(frame_debug);
        frame_debug.setBounds(15, 295, 770, 270);

        sync_wiz.setText("sync wiz");
        sync_wiz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sync_wizActionPerformed(evt);
            }
        });
        desktop.add(sync_wiz);
        sync_wiz.setBounds(100, 260, 73, 23);

        ricalcolo_totali_fatture.setText("ricalcolo totali fatture");
        ricalcolo_totali_fatture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ricalcolo_totali_fattureActionPerformed(evt);
            }
        });
        desktop.add(ricalcolo_totali_fatture);
        ricalcolo_totali_fatture.setBounds(20, 180, 220, 23);

        scrollDesktop.setViewportView(desktop);

        jLayeredPane1.add(scrollDesktop);
        scrollDesktop.setBounds(0, 40, 1545, 580);

        add(jLayeredPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void butOrdiniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butOrdiniActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        frmElenOrdini frm = new frmElenOrdini();
        openFrame(frm, 870, InvoicexUtil.getHeightIntFrame(750));
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_butOrdiniActionPerformed

    private void butDdtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDdtActionPerformed
        menGestDdt_actionPerformed(null);
}//GEN-LAST:event_butDdtActionPerformed

    private void butFattureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butFattureActionPerformed
        menGestFattureActionPerformed(null);
}//GEN-LAST:event_butFattureActionPerformed

    private void butClieFornActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butClieFornActionPerformed
        menAnagClieActionPerformed(null);
}//GEN-LAST:event_butClieFornActionPerformed

    private void butSituClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSituClieActionPerformed
        menGestSituClieActionPerformed(null);
}//GEN-LAST:event_butSituClieActionPerformed

    private void butArticoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butArticoliActionPerformed
        menAnagArtiActionPerformed(null);
}//GEN-LAST:event_butArticoliActionPerformed

    private void butFattureAcquistoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butFattureAcquistoActionPerformed
        menGestFattureAcquistoActionPerformed(null);
}//GEN-LAST:event_butFattureAcquistoActionPerformed

    private void btnPluginsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPluginsActionPerformed
        if (main.attivazione.getIdRegistrazione() == null) {
            if (SwingUtils.showYesNoMessage(main.getPadreWindow(), "Per utilizzare le funzionalità aggiuntive devi prima registrare il programma,\nclicca su Sì per proseguire nella registrazione")) {
                main.getPadrePanel().apridatiazienda();
                if (main.attivazione.getIdRegistrazione() != null) {
                    showplugins(false);
                }
            }
        } else {
            showplugins(false);
        }
}//GEN-LAST:event_btnPluginsActionPerformed

    private void butVncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butVncActionPerformed
        try {

            JDialogHelpAss d = new JDialogHelpAss(main.getPadreWindow(), true);
            d.setLocationRelativeTo(null);
            d.setVisible(true);
            if (d.si) {
                SwingUtils.mouse_wait(main.getPadreWindow());

                InvoicexUtil.lanciaTeamViewer();

                SwingUtils.mouse_def(main.getPadreWindow());
            }
        } catch (Exception err) {
            SwingUtils.mouse_def(main.getPadreWindow());
            JOptionPane.showMessageDialog(null, err.toString());
        }
}//GEN-LAST:event_butVncActionPerformed

    private void butRiscattaAcquistoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butRiscattaAcquistoActionPerformed
        clickAttivazione();
}//GEN-LAST:event_butRiscattaAcquistoActionPerformed

    private void panBarrComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panBarrComponentResized
        //    double m = 15;
        //    System.out.println("butVnc Y:" + butVnc.getY() + " butvnc2:" + butVnc.getLocation());
        //    if (butVnc.getY() > 10) {
        //        System.out.println("setto panbarr 1");
        //        panBarr.setPreferredSize(new Dimension(getWidth(), (int) ((jButton21.getHeight() * 2) + (m * 1.5))));
        //        panBarr.setMinimumSize(new Dimension(getWidth(), (int) ((jButton21.getHeight() * 2) + (m * 1.5))));
        //    } else {
        //        System.out.println("setto panbarr 2");
        //        panBarr.setPreferredSize(new Dimension(getWidth(), (int) (jButton21.getHeight() + m)));
        //    }
        //    System.out.println("panbarr:" + panBarr.getSize());
}//GEN-LAST:event_panBarrComponentResized

    private void panBarrComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_panBarrComponentAdded
        //                            menu.panBarr.setPreferredSize(new Dimension(menu.getWidth(), (butRicerca.getHeight() * 2) + 16));
        //                            menu.panBarr.revalidate();
        checkPanBarr();
}//GEN-LAST:event_panBarrComponentAdded

    private void panBarrKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_panBarrKeyPressed
        // TODO add your handling code here:
}//GEN-LAST:event_panBarrKeyPressed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        Thread t = new Thread("test111") {
            @Override
            public void run() {
                super.run();
                MicroBench mb = new MicroBench(true);
                try {
                    Robot r = new Robot();
                    Component[] comps = new Component[7];
                    comps[0] = butFatture;
//                    comps[1] = butDdt;
//                    comps[2] = butClieForn;
//                    comps[3] = butArticoli;
//                    comps[4] = butSituClie;
//                    comps[5] = butFattureAcquisto;
                    int j = 0;
                    for (int i = 0; i < 10; i++) {
//                        if (j == 2) {
//                            j = 0;
//                        }
                        int x = comps[j].getLocationOnScreen().x;
                        int y = comps[j].getLocationOnScreen().y;
                        r.mouseMove(x + 10, y + 3);

//                        System.gc();
                        Thread.sleep(100);
                        r.mousePress(InputEvent.BUTTON1_MASK);
                        Thread.sleep(15);
                        r.mouseRelease(InputEvent.BUTTON1_MASK);
                        Thread.sleep(200);
//                        System.gc();
//                        DebugUtils.dumpMem();
//                        System.runFinalization();
//                        DebugUtils.dumpMem();
                        Thread.sleep(100);

//                        if (j == 0) {
//                            //attendo il caricamento
//                            boolean frmnotvis = true;
//                            JInternalFrame[] ifs = desktop.getAllFrames();
//                            while (frmnotvis) {
//                                ifs = desktop.getAllFrames();
//                                 System.out.println("ifs.length:" + ifs.length);
//                                if (ifs.length > 0) {
//                                    break;
//                                }
//                                Thread.sleep(100);
//                            }
//                        }
                        if (j == 0) {
//                            int x = comps[j].getLocationOnScreen().x;
//                            int y = comps[j].getLocationOnScreen().y;
//                            r.mouseMove(x + 10, y + 3);
                            JInternalFrame[] ifs = desktop.getAllFrames();

                            boolean frmnotvis = true;
                            while (frmnotvis) {
                                ifs = desktop.getAllFrames();
                                System.out.println("ifs.length:" + ifs.length);
                                if (ifs.length > 1) {
                                    break;
                                }
                                Thread.sleep(100);
                            }

                            Thread.sleep(200);

                            System.out.println("ifs[0]:" + ifs[0]);
                            frmElenFatt elen = null;
                            if (ifs[0] instanceof frmElenFatt) {
                                elen = (frmElenFatt) ifs[0];
                            } else if (ifs[1] instanceof frmElenFatt) {
                                elen = (frmElenFatt) ifs[1];
                            } else {
                                SwingUtils.showErrorMessage(null, "non trovata form elen fatt");
                            }

                            //new fattura
                            JComponent comp = elen.butNew;
                            x = comp.getLocationOnScreen().x;
                            y = comp.getLocationOnScreen().y;
                            r.mouseMove(x + 10, y + 3);
                            Thread.sleep(100);
                            r.mousePress(InputEvent.BUTTON1_MASK);
                            Thread.sleep(15);
                            r.mouseRelease(InputEvent.BUTTON1_MASK);
                            Thread.sleep(200);

                            frmnotvis = true;
                            while (frmnotvis) {
                                ifs = desktop.getAllFrames();
                                System.out.println("ifs.length:" + ifs.length);
                                if (ifs.length > 2) {
                                    break;
                                }
                                Thread.sleep(100);
                            }

                            System.out.println("ifs = " + ifs);

                            System.out.println("ifs[0]:" + ifs[0]);
                            System.out.println("ifs[1]:" + ifs[1]);
                            frmTestFatt testfatt = null;
                            if (ifs[0] instanceof frmTestFatt) {
                                testfatt = (frmTestFatt) ifs[0];
                            } else if (ifs[1] instanceof frmTestFatt) {
                                testfatt = (frmTestFatt) ifs[1];
                            } else if (ifs[2] instanceof frmTestFatt) {
                                testfatt = (frmTestFatt) ifs[2];
                            } else {
                                SwingUtils.showErrorMessage(null, "non trovata frmtestfatt");
                            }

                            Thread.sleep(100);
                            while (testfatt.in_apertura) {
                                Thread.sleep(100);
                            }

                            //aspetto e chiudo
                            comp = testfatt.butUndo;
                            Thread.sleep(200);
                            x = comp.getLocationOnScreen().x;
                            y = comp.getLocationOnScreen().y;
                            r.mouseMove(x + 10, y + 3);
                            Thread.sleep(100);
                            r.mousePress(InputEvent.BUTTON1_MASK);
                            Thread.sleep(15);
                            r.mouseRelease(InputEvent.BUTTON1_MASK);
                            Thread.sleep(100);

                            /*                            
                             //modifica fattura
                             comp = elen.butModi;
                             x = comp.getLocationOnScreen().x;
                             y = comp.getLocationOnScreen().y;
                             r.mouseMove(x + 10, y + 3);
                             Thread.sleep(100);
                             r.mousePress(InputEvent.BUTTON1_MASK);
                             Thread.sleep(15);
                             r.mouseRelease(InputEvent.BUTTON1_MASK);
                             Thread.sleep(200);

                             frmnotvis = true;
                             while (frmnotvis) {
                             ifs = desktop.getAllFrames();
                             System.out.println("ifs.length:" + ifs.length);
                             if (ifs.length > 2) {
                             break;
                             }
                             Thread.sleep(100);
                             }

                             System.out.println("ifs = " + ifs);

                             System.out.println("ifs[0]:" + ifs[0]);
                             System.out.println("ifs[1]:" + ifs[1]);
                             testfatt = null;
                             if (ifs[0] instanceof frmTestFatt) {
                             testfatt = (frmTestFatt) ifs[0];
                             } else if (ifs[1] instanceof frmTestFatt) {
                             testfatt = (frmTestFatt) ifs[1];
                             } else {
                             SwingUtils.showErrorMessage(null, "aaaaaaaaaaaa");
                             }
                            
                             while (testfatt.in_apertura) {
                             Thread.sleep(100);
                             }
                            
                             //aspetto e chiudo
                             comp = testfatt.butUndo;
                             Thread.sleep(200);
                             x = comp.getLocationOnScreen().x;
                             y = comp.getLocationOnScreen().y;
                             r.mouseMove(x + 10, y + 3);
                             Thread.sleep(100);
                             r.mousePress(InputEvent.BUTTON1_MASK);
                             Thread.sleep(15);
                             r.mouseRelease(InputEvent.BUTTON1_MASK);
                             Thread.sleep(100);
                             */
 /*
                             comp = testfatt.griglia;
                             Thread.sleep(200);
                             x = comp.getLocationOnScreen().x;
                             y = comp.getLocationOnScreen().y;
                             r.mouseMove(x + 10, y + 3);
                             Thread.sleep(100);
                             r.mousePress(InputEvent.BUTTON1_MASK);
                             Thread.sleep(15);
                             r.mouseRelease(InputEvent.BUTTON1_MASK);
                             Thread.sleep(200);
                             r.mousePress(InputEvent.BUTTON1_MASK);
                             Thread.sleep(15);
                             r.mouseRelease(InputEvent.BUTTON1_MASK);
                             Thread.sleep(200);

                             System.out.println("arriva ???");

                             frmnotvis = true;
                             while (frmnotvis) {
                             ifs = desktop.getAllFrames();
                             System.out.println("ifs.length:" + ifs.length);
                             if (ifs.length > 2) {
                             break;
                             }
                             Thread.sleep(100);
                             }*/
                        }

                        JInternalFrame[] ifs = desktop.getAllFrames();
                        for (JInternalFrame ifn : ifs) {
                            if (ifn instanceof frmElenFatt) {
                                ifn.dispose();
                            }
                        }
                        System.out.println("conta:" + i);
//                        j++;
                        Thread.sleep(100);

                        System.gc();
                        DebugUtils.dumpMem();
                        System.runFinalization();
                        System.gc();
                        DebugUtils.dumpMem();
                    }
                    mb.out("************************************ bench robot");

                    TreeMap top = (TreeMap) DbUtils.getTopQuery();
                    System.out.println(top.get(top.lastKey()));
                } catch (Exception ex) {
                    ex.printStackTrace();
//                    main.exitMain();
                }
            }
        };
        t.start();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        Thread t = new Thread("test111") {
            @Override
            public void run() {
                super.run();
                try {
                    for (int i = 0; i < 10000; i++) {
                        //                    Thread.sleep(10);
                        ResultSet r = Db.openResultSet("select * from articoli");
                        r.next();
                        System.out.println(r.getString(1));
                        System.out.println("conta:" + i);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        t.start();
}//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        System.gc();
        DebugUtils.dumpMem();
        System.runFinalization();
        DebugUtils.dumpMem();
}//GEN-LAST:event_jButton6ActionPerformed

    private void menAnagAziendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagAziendaActionPerformed
        apridatiazienda();
}//GEN-LAST:event_menAnagAziendaActionPerformed

    private void menAnagClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagClieActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        JInternalFrame frm = null;

//        if ((main.isPluginContabilitaAttivo() || main.pluginAttivi.contains("pluginToysforyou") || main.getPersonalContain("nuova_clifor")) && !main.getPersonalContain("vecchia_clifor")) {
//            frm = new JInternalFrameClientiFornitori();
//        } else {
//            frm = new frmClie();
//        }
        if (main.getPersonalContain("vecchia_clifor")) {
            frm = new frmClie();
        } else {
            frm = new JInternalFrameClientiFornitori();
        }

        openFrame(frm, 800, InvoicexUtil.getHeightIntFrame(660));

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_menAnagClieActionPerformed

    private void menAnagArtiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagArtiActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        JInternalFrame frm;

        if (main.pluginClientManager) {
            frm = new frmArtiCM();
        } else if (ObjectUtils.toString(main.fileIni.getValue("db", "nome_database", "")).indexOf("perle") >= 0
                || main.getPersonalContain("dev")) {
            frm = new frmArtiConListinoPerle();
        } else {
            frm = new frmArtiConListino();
        }

//        openFrame(frm, 730, 600);
//        openFrame(frm, 900, 600);
        openFrame(frm, 850, InvoicexUtil.getHeightIntFrame(750));
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_menAnagArtiActionPerformed

    private void menAnagBancAbiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagBancAbiActionPerformed
        frmBancAbi frm = new frmBancAbi();
        openFrame(frm, 700, InvoicexUtil.getHeightIntFrame(600));
}//GEN-LAST:event_menAnagBancAbiActionPerformed

    private void menAnagDatiAzieBancActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagDatiAzieBancActionPerformed
        frmDatiAzieBanc frm = new frmDatiAzieBanc();
        openFrame(frm, 700, InvoicexUtil.getHeightIntFrame(500));
}//GEN-LAST:event_menAnagDatiAzieBancActionPerformed

    private void menAnagTipiPagaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagTipiPagaActionPerformed
        frmTipiPaga frm = new frmTipiPaga();
        openFrame(frm, 700, InvoicexUtil.getHeightIntFrame(600));
}//GEN-LAST:event_menAnagTipiPagaActionPerformed

    private void menAnagTipiIvaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagTipiIvaActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        frmCodiciIva frm = new frmCodiciIva();
        openFrame(frm, 600, 400);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_menAnagTipiIvaActionPerformed

    private void menAnagListiniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagListiniActionPerformed

        frmListini frm = new frmListini();
        openFrame(frm, 800, 600);
}//GEN-LAST:event_menAnagListiniActionPerformed

    private void menAnagVettoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagVettoriActionPerformed
        frmVettori frm = new frmVettori();
        openFrame(frm, 500, 400);
}//GEN-LAST:event_menAnagVettoriActionPerformed

    private void menAnagPortiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagPortiActionPerformed
        frmPorti frm = new frmPorti();
        openFrame(frm, 500, 400);
}//GEN-LAST:event_menAnagPortiActionPerformed

    private void menAnagCausaliTrasportoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagCausaliTrasportoActionPerformed
        frmTipiCausaliTrasporto frm = new frmTipiCausaliTrasporto();
        openFrame(frm, 700, 400);
}//GEN-LAST:event_menAnagCausaliTrasportoActionPerformed

    private void menGestOrdiniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestOrdiniActionPerformed
        butOrdiniActionPerformed(null);
}//GEN-LAST:event_menGestOrdiniActionPerformed

    private void menGestDdt_actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestDdt_actionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        frmElenDDT frm = new frmElenDDT();

        openFrame(frm, 870, InvoicexUtil.getHeightIntFrame(750));

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_menGestDdt_actionPerformed

    private void menGestFattureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestFattureActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        frmElenFatt frm = new frmElenFatt();

        openFrame(frm, 870, InvoicexUtil.getHeightIntFrame(750));

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_menGestFattureActionPerformed

    private void menGestSituClieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestSituClieActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        JInternalFrameScadenzario frame = new JInternalFrameScadenzario();
        openFrame(frame, 800, InvoicexUtil.getHeightIntFrame(750));

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menGestSituClieActionPerformed

    private void menGestRistDistRibaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestRistDistRibaActionPerformed
        frmListDistRiba frm = new frmListDistRiba();
        openFrame(frm, 400, 400);
}//GEN-LAST:event_menGestRistDistRibaActionPerformed

    private void menGestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_menGestActionPerformed

    private void menMagazzinoMovimentiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menMagazzinoMovimentiActionPerformed
        gestioneFatture.magazzino.frmMovimenti frm = new gestioneFatture.magazzino.frmMovimenti();
        openFrame(frm, 700, 550);
}//GEN-LAST:event_menMagazzinoMovimentiActionPerformed

    private void menMagazzinoStampaMovimentiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menMagazzinoStampaMovimentiActionPerformed
        gestioneFatture.magazzino.JInternalFrameStampaMovimenti frm = new gestioneFatture.magazzino.JInternalFrameStampaMovimenti();
        frm.pack();
        openFrame(frm);
}//GEN-LAST:event_menMagazzinoStampaMovimentiActionPerformed

    private void menMagazzinoStampaGiacenzeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menMagazzinoStampaGiacenzeActionPerformed
        try {
            JInternalFrameGiacenze frame = new JInternalFrameGiacenze(false);
            openFrame(frame, 700, 600);
            frame.aggiorna();
            frame.initFilters();
        } catch (Exception err) {
            err.printStackTrace();
        }
}//GEN-LAST:event_menMagazzinoStampaGiacenzeActionPerformed

    private void menMagazzinoStampaGiacenzeMatricoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menMagazzinoStampaGiacenzeMatricoleActionPerformed
        try {
            JInternalFrameGiacenze frame = new JInternalFrameGiacenze(true);
            frame.setTitle("Giacenza per Matricole");
            openFrame(frame, 700, 600);
            frame.aggiorna();
            frame.initFilters();
        } catch (Exception err) {
            err.printStackTrace();
        }
}//GEN-LAST:event_menMagazzinoStampaGiacenzeMatricoleActionPerformed

    private void menMagazzinoStampaGiacenzeLottiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menMagazzinoStampaGiacenzeLottiActionPerformed
        try {
            JInternalFrameGiacenze frame = new JInternalFrameGiacenze(false, true);
            frame.setTitle("Giacenza per Lotti");
            openFrame(frame, 700, 600);
            frame.aggiorna();
            frame.initFilters();
        } catch (Exception err) {
            err.printStackTrace();
        }
}//GEN-LAST:event_menMagazzinoStampaGiacenzeLottiActionPerformed

    private void menGestioneIvaStampaRegistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestioneIvaStampaRegistroActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        gestioneFatture.primaNota.frmStampaRegistroIva frm = new gestioneFatture.primaNota.frmStampaRegistroIva();
//        openFrame(frm, 500, 300);
        openFrame(frm);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_menGestioneIvaStampaRegistroActionPerformed

    private void menStatisticheOrdBolFatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menStatisticheOrdBolFatActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        //controllo se presente quercus
        File ftestquercus = new File("lib/quercus.jar");
        if (!ftestquercus.exists()) {
            JWindow w = SwingUtils.showFlashMessage2("Attendere download librerie aggiuntive", 60, null, Color.red, null, true);
            try {
                HttpUtils.saveBigFile("http://server.invoicex.it/download/invoicex/lib/quercus.jar", ftestquercus.getAbsolutePath());
                ClassPathHacker.addFile(ftestquercus);
            } catch (Exception ex) {
                SwingUtils.showExceptionMessage(this, ex);
            }
            w.dispose();
        }
        File ftestphp = new File("php");
        File ftestphpMustacheZip = new File("php/Mustache.zip");
        File ftestphpstats = new File("php/stats.php");
        File ftestphptpl = new File("php/templates");
        File ftestphptplagente = new File("php/templates/stats_agente.html");
        if (!ftestphp.exists() || !ftestphpstats.exists() || !ftestphptplagente.exists() || ftestphpstats.length() == 0 || ftestphptplagente.length() == 0) {
            try {
                ftestphp.mkdir();
            } catch (Exception e) {
            }
            try {
                ftestphptpl.mkdir();
            } catch (Exception e) {
            }
            JWindow w = SwingUtils.showFlashMessage2("Attendere download librerie aggiuntive", 60, null, Color.red, null, true);
            try {
                HttpUtils.saveFile("http://server.invoicex.it/download/invoicex/php/Mustache.zip", ftestphpMustacheZip.getAbsolutePath());
                UnZip.unzip(ftestphpMustacheZip, ftestphp);
                HttpUtils.saveFile("http://server.invoicex.it/download/invoicex/php/stats.php", ftestphpstats.getAbsolutePath());
                HttpUtils.saveFile("http://server.invoicex.it/download/invoicex/php/templates/stats_agente.html", ftestphptplagente.getAbsolutePath());
            } catch (Exception ex) {
                SwingUtils.showExceptionMessage(this, ex);
            }
            w.dispose();
        }

        gestioneFatture.stats.frmStatOrdiniBolleFatture frm = new gestioneFatture.stats.frmStatOrdiniBolleFatture();
        frm.pack();
        openFrame(frm);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_menStatisticheOrdBolFatActionPerformed

    private void menExpAcqVendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menExpAcqVendActionPerformed
        JDialogExportAcquistiVendite dialog = new JDialogExportAcquistiVendite(main.getPadreWindow(), true);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
}//GEN-LAST:event_menExpAcqVendActionPerformed

    private void menUtilImpoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menUtilImpoActionPerformed
//        new JDialogImpostazioni(main.getPadre(), true);
        SwingUtils.mouse_wait(this);
        new JDialogImpostazioni(main.getPadreWindow(), true);
        SwingUtils.mouse_def(this);
}//GEN-LAST:event_menUtilImpoActionPerformed

    private void menUtilAggiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menUtilAggiActionPerformed
        String vl = main.version + " (" + main.build + ")";
        try {
            String url = main.baseurlserver + "/v.php?v=" + URLEncoder.encode(vl, "ISO-8859-1") + (main.attivazione.getIdRegistrazione() != null ? "&i=" + main.attivazione.getIdRegistrazione() : "") + "&so=" + URLEncoder.encode(System.getProperty("os.name") + ":" + System.getProperty("os.version"), "ISO-8859-1");
            System.out.println("url: " + url);
            String v = main.getURL(url);
//            if (pluginAutoUpdate == false && fileIni.getValueBoolean("pref", "msg_plugins_upd", true)) {
            if (v != null && !vl.equalsIgnoreCase(v)) {
                JDialogUpd d = new JDialogUpd(main.getPadreWindow(), true, v);
                d.pack();
                d.setLocationRelativeTo(null);
                d.setVisible(true);
            } else {
                SwingUtils.showInfoMessage(main.getPadreWindow(), "La tua versione è la più recente");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
}//GEN-LAST:event_menUtilAggiActionPerformed

    private void menUtilBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menUtilBackupActionPerformed

        //chiedo conferma e avverto di non usare il rpogramma
        int ret = javax.swing.JOptionPane.showConfirmDialog(this, "Sicuri di eseguire la copia di sicurezza dei dati?\nPrima di iniziare assicurarsi che nessuno\nutilizzi il programma durante la copia.", "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);

        if (ret == javax.swing.JOptionPane.YES_OPTION) {

            //eseguo un dump del database
            System.out.println("inizio backup");

            //visualizzo frame con log processo
            it.tnx.JFrameMessage mess = new it.tnx.JFrameMessage();
            try {
                mess.setIconImage(main.getLogoIcon());
            } catch (Exception err) {
                err.printStackTrace();
            }
            mess.setBounds(100, 100, 600, 400);
            mess.show();

            DumpThread dump = new DumpThread(mess);
            dump.start();
        }
}//GEN-LAST:event_menUtilBackupActionPerformed

    private void menUtilRestoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menUtilRestoreActionPerformed

        SwingWorker work1 = new SwingWorker() {
            JDialogWait wait = new JDialogWait(main.getPadreFrame(), false);

            @Override
            protected void done() {

                wait.labStato.setText("finito");
                System.out.println("REC:finito");
                SwingUtils.mouse_def(main.getPadreWindow());
                wait.setVisible(false);

            }

            @Override
            protected void process(List chunks) {
                for (Object chunk : chunks) {
                    if (chunk instanceof int[]) {
                        int[] vals = (int[]) chunk;
                        wait.progress.setMaximum(vals[1]);
                        wait.progress.setValue(vals[0]);
                        if (wait.progress.isIndeterminate()) {
                            wait.progress.setIndeterminate(false);
                        }
                    } else {
                        wait.labStato.setText(chunk.toString());
                    }
                }
            }

            @Override
            protected Object doInBackground() throws Exception {

                SwingUtils.mouse_wait(main.getPadreWindow());

                //faccio selezionare il file
                File dirBackup = new File(main.wd + "backup");
                JFileChooser fileChooser = SwingUtils.getFileOpen(null, dirBackup);

                int ret = fileChooser.showOpenDialog(main.getPadreWindow());
                File f = fileChooser.getSelectedFile();

                if (ret == JFileChooser.CANCEL_OPTION) {
                    return null;
                }

                if (JOptionPane.showConfirmDialog(main.getPadreWindow(), "Sicuro di eliminare tutti i dati e ripristinarli dal backup selezionato ?",
                        "Attenzione", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return null;
                }

                wait.setLocationRelativeTo(null);
                wait.setVisible(true);

                //eseguo backup prima di ripristino //se non via internet
                if (!main.via_internet) {
                    it.tnx.JFrameMessage mess = new it.tnx.JFrameMessage();
                    publish("Backup prima di ripristino...");
                    DumpThread dump = new DumpThread(mess);
                    dump.start();
                    synchronized (dump) {
                        dump.wait();
                    }
                }

                //controllo zip
                if (f.getName().endsWith(".zip")) {
                    //unzippo
                    gestioneFatture.UnZip.main(new String[]{f.getAbsolutePath()});
                    f = new File(main.wd + gestioneFatture.UnZip.firstfile);
                    publish("Backup decompresso");
                }

                //restoro su db
                try {

                    publish("Ripristino in corso");

                    InvoicexUtil.ripristinaDump(f, this);

                    publish("Ripristino completato");
                    JOptionPane.showMessageDialog(main.getPadreWindow(), "Recupero terminato\nSi consiglia di riavviare Invoicex", "Attenzione", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(main.getPadreWindow(), "Errore:" + ex.toString());
                    ex.printStackTrace();
                }

                return null;

            }

        };
        work1.execute();
    }//GEN-LAST:event_menUtilRestoreActionPerformed

    private void menUtilBackupOnlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menUtilBackupOnlineActionPerformed
        System.out.println("menUtilBackupOnlineActionPerformed");
        if (!main.pluginBackupTnx) {
            int ret = JOptionPane.showConfirmDialog(this, "Per questa operazione e' necessario installare il plugin 'Backup TNX', vuoi andare alla gestione plugins ?", "Informazione", JOptionPane.YES_NO_OPTION);
            if (ret == JOptionPane.YES_OPTION) {
                menUtilPluginsActionPerformed(null);
            }
        }
}//GEN-LAST:event_menUtilBackupOnlineActionPerformed

    private void menUtilRestoreOnlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menUtilRestoreOnlineActionPerformed
        if (!main.pluginBackupTnx) {
            int ret = JOptionPane.showConfirmDialog(this, "Per questa operazione è necessario installare il plugin 'Backup TNX', vuoi andare alla gestione plugins ?", "Informazione", JOptionPane.YES_NO_OPTION);
            if (ret == JOptionPane.YES_OPTION) {
                menUtilPluginsActionPerformed(null);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Funzione da implementare", "Informazione", JOptionPane.INFORMATION_MESSAGE);
        }
}//GEN-LAST:event_menUtilRestoreOnlineActionPerformed

    private void menUtilCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menUtilCheckActionPerformed

        //chiedo conferma e avverto di non usare il rpogramma
        int ret = javax.swing.JOptionPane.showConfirmDialog(this, "Sicuri di eseguire il controllo dei dati?\nPrima di iniziare assicurarsi che nessuno\nutilizzi il programma durante la procedura.", "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);

        if (ret == javax.swing.JOptionPane.YES_OPTION) {

            //eseguo un check del database di tutte le tabelle
            System.out.println("inizio check");

            //visualizzo frame con log processo
            it.tnx.JFrameMessage mess = new it.tnx.JFrameMessage();
            mess.setBounds(100, 100, 500, 200);
            mess.show();

            CheckThread check = new CheckThread(mess);
            check.start();
            System.out.println("check completato");
        }
}//GEN-LAST:event_menUtilCheckActionPerformed

    private void menUtilCambiaFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menUtilCambiaFontActionPerformed

        JDialogFont df = new JDialogFont(main.getPadreWindow(), true);
        df.setBounds(150, 200, 350, 150);
        df.show();
}//GEN-LAST:event_menUtilCambiaFontActionPerformed

    private void menUtilPluginsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menUtilPluginsActionPerformed

        showplugins(false);
    }//GEN-LAST:event_menUtilPluginsActionPerformed

    private void menUtilCambiaPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menUtilCambiaPasswordActionPerformed
        if (main.startConDbCheck) {
            JOptionPane pane = new JOptionPane("Inserisci la nuova password", JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            pane.setWantsInput(true);
            pane.setInitialValue(null);
            pane.setInputValue(null);
            JDialog dialog = pane.createDialog(this, "Cambio password database interno");
            dialog.setVisible(true);
            if (((Integer) pane.getValue()).equals(JOptionPane.OK_OPTION)) {
                String input = (String) pane.getInputValue();
                System.out.println("nuova password:" + input);
                Db.executeSql("SET PASSWORD FOR 'root'@'localhost' = PASSWORD('" + input + "')");
                Db.executeSql("FLUSH PRIVILEGES");
                main.fileIni.setValueCifrato("db", "pwd", input);
                main.fileIni.setValueCifrato("db", "pwd_interno", input);
                JOptionPane.showMessageDialog(this, "La password e' stata cambiata, dovresti riavviare l'applicazione per rendere effettiva la modifica", "Informazione", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            try {
                ResultSet r = DbUtils.tryOpenResultSet(Db.getConn(), "SELECT CURRENT_USER()");
                if (r.next()) {
                    String user = r.getString(1);
                    String host = "%";
                    System.err.println("user = " + user);
                    if (user.indexOf("@") >= 0) {
                        host = StringUtils.split(user, "@")[1];
                        user = StringUtils.split(user, "@")[0];
                    }
                    if (SwingUtils.showYesNoMessage(this, "<html>Sicuro di cambiare la password per l'utente: <b>" + user + " (host:" + host + ")</b> ?</html>", "Attenzione")) {
                        JOptionPane pane = new JOptionPane("Inserisci la nuova password", JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                        pane.setWantsInput(true);
                        pane.setInitialValue(null);
                        pane.setInputValue(null);
                        JDialog dialog = pane.createDialog(this, "Cambio password database interno");
                        dialog.setVisible(true);
                        if (((Integer) pane.getValue()).equals(JOptionPane.OK_OPTION)) {
                            String input = (String) pane.getInputValue();
                            System.out.println("nuova password:" + input);
                            Db.executeSql("SET PASSWORD FOR '" + user + "'@'" + host + "' = PASSWORD('" + input + "')");
                            Db.executeSql("FLUSH PRIVILEGES");
                            main.fileIni.setValueCifrato("db", "pwd", input);
                            main.fileIni.setValueCifrato("db", "pwd_esterno", input);
                            JOptionPane.showMessageDialog(this, "La password e' stata cambiata, dovresti riavviare l'applicazione per rendere effettiva la modifica", "Informazione", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}//GEN-LAST:event_menUtilCambiaPasswordActionPerformed

    private void menUtilDbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menUtilDbActionPerformed
        JOptionPane.showMessageDialog(this, "Attenzione, questa funzione permette di modificare direttamente i dati senza nessun controllo", "Attenzione", JOptionPane.WARNING_MESSAGE);
        JFrameDb frame = new JFrameDb();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
}//GEN-LAST:event_menUtilDbActionPerformed

    private void menManutezioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menManutezioneActionPerformed
        JDialogManutenzione dialog = new JDialogManutenzione(main.getPadreWindow(), true);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
}//GEN-LAST:event_menManutezioneActionPerformed

    private void menAgentiAnagraficaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAgentiAnagraficaActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        gestioneFatture.frmAgenti frm = new gestioneFatture.frmAgenti();
        openFrame(frm, 500, 400);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_menAgentiAnagraficaActionPerformed

    private void menAgentiSituazioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAgentiSituazioneActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        gestioneFatture.logic.provvigioni.frmSituazioneAgenti frm = new gestioneFatture.logic.provvigioni.frmSituazioneAgenti();

        if (Util.getScreenResolution() == Util.SCREEN_RES_1024x768) {
            openFrame(frm, 920, InvoicexUtil.getHeightIntFrame(650));
        } else {
            openFrame(frm, 600, 400);
        }

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_menAgentiSituazioneActionPerformed

    private void menAchievoOreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAchievoOreActionPerformed
}//GEN-LAST:event_menAchievoOreActionPerformed

    private void menFineMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menFineMenuSelected
        //costrutire il menu a discesa...
        menFine.removeAll();
        JInternalFrame[] frames = desktop.getAllFrames();
        for (final JInternalFrame frame : frames) {
            if (frame.getTitle() == null || frame.getTitle().trim().length() == 0) {
                continue;
            }
            JMenuItem temp = new JMenuItem(frame.getTitle());
            if (frame.getTitle().length() == 0) {
                temp.setText("<Finestra senza titolo>");
            }
            temp.setVisible(true);
            menFine.add(temp);
            temp.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("frame selezionato:" + frame);
                    //deseleziono tutti
                    JInternalFrame[] frames2 = desktop.getAllFrames();
                    for (JInternalFrame frame2 : frames2) {
                        try {
                            frame.setSelected(false);
                        } catch (Exception e3) {
                        }
                    }
                    //seleziono il selezionato
                    frame.moveToFront();
                    frame.requestFocus();
                    //                desktop.setSelectedFrame(frame);
                    try {
                        frame.setSelected(true);
                    } catch (Exception e2) {
                    }

                    desktop.repaint();
                }
            });
        }

        try {
            InvoicexEvent event = new InvoicexEvent(this);
            event.type = InvoicexEvent.TYPE_MENUFINESTRE_POST_REMOVEALL;
            main.events.fireInvoicexEvent(event);
        } catch (Exception err) {
            err.printStackTrace();
        }


}//GEN-LAST:event_menFineMenuSelected

    private void menFineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menFineActionPerformed
}//GEN-LAST:event_menFineActionPerformed

    private void menAiutoInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAiutoInfoActionPerformed
        JDialogAbout dialog = new JDialogAbout(main.getPadreWindow(), true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
}//GEN-LAST:event_menAiutoInfoActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
//        SwingUtils.open(new File(System.getProperty("user.home") + File.separator + ".invoicex"));
        Util.start2(System.getProperty("user.home") + File.separator + ".invoicex");
}//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        try {
//            SwingUtils.openUrl(new URL("http://www.tnx.it/index.php?p=faq&l=ita"));
            SwingUtils.openUrl(new URL("http://www.invoicex.it/Supporto-e-Assistenza-FAQ/"));
        } catch (Exception e) {
        }
}//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        visualizzaNote();
}//GEN-LAST:event_jMenuItem4ActionPerformed

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
//        segnalaRapporti();
        //hack per scrollbar del jscrollpane desktop
//        Timer timer = new Timer(500, new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                validate();
//                jLayeredPane1ComponentResized(null);
//            }
//        });
//        timer.setInitialDelay(500);
//        timer.setRepeats(false);
    }//GEN-LAST:event_formComponentShown

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
//        checkPanBarr();
        checkPanBarr2();
    }//GEN-LAST:event_formComponentResized

    private void desktopComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_desktopComponentResized
        desktopResize();
    }//GEN-LAST:event_desktopComponentResized

    public void desktopResize() {
        int w = desktop.getSize().width;
        int h = desktop.getSize().height;
        lblInfoLoading2.setLocation(w - lblInfoLoading2.getWidth() - 10, 5);
        System.out.println("lblInfoLoading2.setLocation : " + (w - lblInfoLoading2.getWidth() - 10) + " w:" + w + " isEtd:" + SwingUtilities.isEventDispatchThread());

        labmess2.setLocation(w - labmess2.getWidth() - 10, lblInfoLoading2.getHeight() + lblInfoLoading2.getLocation().y + 5);

        if (labSync != null) {
            labSync.setLocation(w - 2, 2);
        }

        resizeNews();
    }

    private void butOrdiniAcquistoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butOrdiniAcquistoActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        frmElenOrdini frm = new frmElenOrdini(true);
        openFrame(frm, 870, InvoicexUtil.getHeightIntFrame(750));
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_butOrdiniAcquistoActionPerformed

    private void butDdtAcquistoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDdtAcquistoActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        frmElenDDT frm = new frmElenDDT(true);
        openFrame(frm, 870, InvoicexUtil.getHeightIntFrame(750));
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_butDdtAcquistoActionPerformed

    private void panBarr2ComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_panBarr2ComponentAdded
        checkPanBarr2();
        evt.getComponent().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                showmenu();
            }
        });
    }//GEN-LAST:event_panBarr2ComponentAdded

    private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    }//GEN-LAST:event_formAncestorAdded

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
    }//GEN-LAST:event_formMouseMoved

    private void jLayeredPane1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jLayeredPane1ComponentResized
        resize();
    }//GEN-LAST:event_jLayeredPane1ComponentResized

    public void resize() {
        panSfuma.setSize((int) jLayeredPane1.getSize().getWidth(), panSfuma.getHeight());
//        System.out.println("panSfuma = w " + panSfuma.getWidth() + " : h " + panSfuma.getHeight());
        if (nascondimenu) {
            scrollDesktop.setLocation(new Point(0, 0));
            scrollDesktop.setSize(jLayeredPane1.getSize());
        } else {
            Dimension d = jLayeredPane1.getSize();
//            d.setSize(d.getWidth(), d.getHeight() - panBarr2.getHeight() + 2);
            d.setSize(d.getWidth(), d.getHeight() - panBarr2.getHeight());
            scrollDesktop.setSize(d);
//            scrollDesktop.setLocation(new Point(0, panBarr2.getHeight() - 2));
            scrollDesktop.setLocation(new Point(0, panBarr2.getHeight()));
        }
        desktop.setSize(scrollDesktop.getViewport().getSize());
        scrollDesktop.validate();
    }

    private void jLayeredPane1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLayeredPane1MouseMoved
    }//GEN-LAST:event_jLayeredPane1MouseMoved

    private void panBarr2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panBarr2MouseClicked
    }//GEN-LAST:event_panBarr2MouseClicked

    private void panBarr2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panBarr2MousePressed
    }//GEN-LAST:event_panBarr2MousePressed

    private void panBarr2ComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panBarr2ComponentMoved

    }//GEN-LAST:event_panBarr2ComponentMoved

    private void popnascondiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popnascondiActionPerformed
        if (nascondimenu) {
            setNascondimenu(false);
        } else {
            setNascondimenu(true);
        }

    }//GEN-LAST:event_popnascondiActionPerformed

    private void menGuidaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGuidaActionPerformed
        try {
            SwingUtils.openUrl(new URL("http://www.invoicex.it/Manuale/"));
        } catch (Exception e) {
        }
    }//GEN-LAST:event_menGuidaActionPerformed

    private void menVncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menVncActionPerformed
        butVncActionPerformed(null);
    }//GEN-LAST:event_menVncActionPerformed

    private void menFunzioniManutenzioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menFunzioniManutenzioneActionPerformed
        funzioniDiManutenzione(menFunzioniManutenzione.isSelected());
    }//GEN-LAST:event_menFunzioniManutenzioneActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        Util.start2(".");
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void menTotFattureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menTotFattureActionPerformed
        JDialogTotaliFatture dialog = new JDialogTotaliFatture(main.getPadreWindow(), true);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }//GEN-LAST:event_menTotFattureActionPerformed

    private void menAnagraficaEmissioneFattureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagraficaEmissioneFattureActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        frmAnagraficaEmissioneFattura frm = new frmAnagraficaEmissioneFattura();
        openFrame(frm, 500, InvoicexUtil.getHeightIntFrame(350));
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menAnagraficaEmissioneFattureActionPerformed

    private void menAnagraficaDurataContrattoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagraficaDurataContrattoActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        frmAnagraficaDurataContratto frm = new frmAnagraficaDurataContratto();
        openFrame(frm, 500, InvoicexUtil.getHeightIntFrame(350));
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menAnagraficaDurataContrattoActionPerformed

    private void menAnagraficaDurataConsulenzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagraficaDurataConsulenzaActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        frmAnagraficaDurataConsulenza frm = new frmAnagraficaDurataConsulenza();
        openFrame(frm, 500, InvoicexUtil.getHeightIntFrame(350));
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menAnagraficaDurataConsulenzaActionPerformed

    private void menAnagraficaStatiOrdineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagraficaStatiOrdineActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        frmAnagraficaStatiOrdine frm = new frmAnagraficaStatiOrdine();
        openFrame(frm, 700, InvoicexUtil.getHeightIntFrame(350));
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menAnagraficaStatiOrdineActionPerformed

    private void menExpAcqVendActionPerformed1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menExpAcqVendActionPerformed1
        JDialogExportAcquistiVendite dialog = new JDialogExportAcquistiVendite(main.getPadreWindow(), true);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }//GEN-LAST:event_menExpAcqVendActionPerformed1

    private void menExpVendPeroniActionPerformed1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menExpVendPeroniActionPerformed1
        JDialogExportVenditePeroni dialog = new JDialogExportVenditePeroni(main.getPadreWindow(), true);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }//GEN-LAST:event_menExpVendPeroniActionPerformed1

    private void menAnagCatCliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagCatCliActionPerformed
        frmAnagraficaTipiClientiFornitori frm = new frmAnagraficaTipiClientiFornitori();
        openFrame(frm, 700, 400);
    }//GEN-LAST:event_menAnagCatCliActionPerformed

    private void menAnagraficaUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagraficaUsersActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        frmAnagraficaUsers frm = new frmAnagraficaUsers();
        openFrame(frm, 700, InvoicexUtil.getHeightIntFrame(400));
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menAnagraficaUsersActionPerformed

    private void menAnagraficaRuoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagraficaRuoliActionPerformed
        SwingUtils.showInfoMessage(this, "Questa funzione è disponibile solo nelle versioni Professional ed Enterprise");
    }//GEN-LAST:event_menAnagraficaRuoliActionPerformed

    private void menGestOrdiniAcquistoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestOrdiniAcquistoActionPerformed
        butOrdiniAcquistoActionPerformed(null);
    }//GEN-LAST:event_menGestOrdiniAcquistoActionPerformed

    private void menGestDdtAcquistoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestDdtAcquistoActionPerformed
        butDdtAcquistoActionPerformed(null);
    }//GEN-LAST:event_menGestDdtAcquistoActionPerformed

    private void menGestFattureAcquistoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestFattureAcquistoActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        gestioneFatture.frmElenFattAcquisto frm = new gestioneFatture.frmElenFattAcquisto();
        openFrame(frm, 870, InvoicexUtil.getHeightIntFrame(750));

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menGestFattureAcquistoActionPerformed

    private void menCambiaPassUtenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menCambiaPassUtenteActionPerformed
        JDialogCambiaPassword dia = new JDialogCambiaPassword();
        dia.setLocationRelativeTo(null);
        dia.setVisible(true);
    }//GEN-LAST:event_menCambiaPassUtenteActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        main.getPadreFrame().setSize(1024, 600);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
//        try {
//            String url = main.baseurlserver + "/ver.php";
//            String ver = main.getURL(url);
//            System.out.println("ver = " + ver);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//apertura delle combo                
//        try {
//            MicroBench mb1 = new MicroBench(true);
//            for (int i = 0; i < 10; i++) {
//                ArrayList a1 = DbUtils.getListArray(gestioneFatture.Db.getConn(), "select nome, codice1 from stati");
//                ArrayList a2 = DbUtils.getListArray(gestioneFatture.Db.getConn(), "select nome, id from agenti where id != 0 and IFNULL(nome,'') != '' order by nome");
//                ArrayList a3 = DbUtils.getListArray(gestioneFatture.Db.getConn(), "select ragione_sociale,codice from clie_forn order by ragione_sociale");
//                ArrayList a4 = DbUtils.getListArray(gestioneFatture.Db.getConn(), "select ragione_sociale,codice from clie_forn where codice != '0' and IFNULL(ragione_sociale,'') != '' order by ragione_sociale");
//            }
//            mb1.out("singoli");
//            for (int i = 0; i < 10; i++) {
//                Statement s = Db.getConn().createStatement();
//                String sql = "select nome, codice1 from stati;"
//                        + "select nome, id from agenti where id != 0 and IFNULL(nome,'') != '' order by nome;"
//                        + "select ragione_sociale,codice from clie_forn order by ragione_sociale;"
//                        + "select ragione_sociale,codice from clie_forn where codice != '0' and IFNULL(ragione_sociale,'') != '' order by ragione_sociale";
//                ResultSet r = s.executeQuery(sql);
//                ArrayList a1 = dbu.getListArray(r);
//                s.getMoreResults();
//                ArrayList a2 = dbu.getListArray(s.getResultSet());
//                s.getMoreResults();
//                ArrayList a3 = dbu.getListArray(s.getResultSet());
//                s.getMoreResults();
//                ArrayList a4 = dbu.getListArray(s.getResultSet());
//            }
//            mb1.out("multipli");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//test con apertura chiusura connessione o senza        
//        main.debug = false;
//        try {
//            MicroBench mb1 = new MicroBench(true);
//            for (int i = 0; i < 100; i++) {
//                Connection conn = Db.getConnection();
//                Statement stat = conn.createStatement();
//                dbu.getListMap(conn, "select SQL_NO_CACHE * from clie_forn");
//                stat.close();
//                conn.close();
//            }
//            mb1.out("con conn multiple");
//
//            Connection conn = Db.getConnection();
//            for (int i = 0; i < 100; i++) {
//                dbu.getListMap(conn, "select SQL_NO_CACHE * from clie_forn");
//            }
//            conn.close();
//            mb1.out("con conn singola");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //test groovy
        try {
            String[] roots = new String[]{"./personal_groovy_test/"};
            GroovyScriptEngine gse = new GroovyScriptEngine(roots);
            Binding binding = new Binding();
            binding.setVariable("input_param_prop", paramProp);
            File dirpersonal = new File(main.wd + "personal_groovy_test/");
            if (dirpersonal.exists()) {
                for (File fgroovy : dirpersonal.listFiles()) {
                    if (fgroovy.getAbsolutePath().endsWith(".groovy")) {
                        System.out.println("INIZIO PERSONAL " + fgroovy + " :");
                        try {
                            gse.run(fgroovy.getName(), binding);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println(binding.getVariable("output"));
//                        try {
//                            File fgroovyexecuted = new File(fgroovy.getAbsolutePath() + ".executed");
//                            try {
//                                fgroovyexecuted.delete();
//                            } catch (Exception e) {
//                            }
//                            System.out.println("renameto:" + fgroovyexecuted + " esito:" + fgroovy.renameTo(fgroovyexecuted));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        System.out.println("FINE PERSONAL " + fgroovy);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }//GEN-LAST:event_jButton1ActionPerformed

    private void menAnagAspettoEsterioreBeniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagAspettoEsterioreBeniActionPerformed
        frmTipiAspettoEsterioreBeni frm = new frmTipiAspettoEsterioreBeni();
        openFrame(frm, 700, 400);
    }//GEN-LAST:event_menAnagAspettoEsterioreBeniActionPerformed

    private void menGestImpegnatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestImpegnatoActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        JInternalFrameReportImpegnato frm = new JInternalFrameReportImpegnato();
        openFrame(frm);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menGestImpegnatoActionPerformed

    private void menGestImpegnatoProduttoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestImpegnatoProduttoreActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        JInternalFrameReportImpegnato frm = new JInternalFrameReportImpegnato(true);

        openFrame(frm);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menGestImpegnatoProduttoreActionPerformed

    private void menGestEvadibileClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menGestEvadibileClienteActionPerformed
//        setCursor(new Cursor(Cursor.WAIT_CURSOR));
//        JInternalFrameReportEvadibile frm = new JInternalFrameReportEvadibile();
//        openFrame(frm);
//        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        try {
            //aggiorno giacenze articoli
            InvoicexUtil.aggiornaGiacenzeArticoli();
            InvoicexUtil.aggiornaPrezziNettiUnitari(Db.getConn(), "righ_ordi", "test_ordi");

            //con compilazione
            //System.out.println("load jrxml");
            //JasperDesign jasperDesign = JasperManager.loadXmlDesign("/mnt/tnx/tnx/lavori/gianni/GestioneFatture/reports/stats_monthly.jrxml");
            //JasperDesign jasperDesign = JasperManager.loadXmlDesign("C:/cvs_tnx/tnx/Invoicex15/src/reports/stats_monthly.jrxml");
            //System.out.print("compilazione...");
            //JasperReport jasperReport = JasperManager.compileReport(jasperDesign);
            //System.out.println("...ok");
            //senza compilazione
            System.out.println("load jasper");
            JasperReport jasperReport = JasperManager.loadReport("reports/report_evadibile.jasper");

            // Second, create a map of parameters to pass to the report.
            java.util.Map parameters = new java.util.HashMap();
//            parameters.put("listino", cu.toString(comListino.getSelectedKey()));
//            parameters.put("periodo", "Dal " + _this.jTextField1.getText() + " al " + _this.jTextField2.getText());
//            parameters.put("stampaDettagli", new Boolean(cheDettagli.isSelected()));
//            parameters.put("stampaQta", new Boolean(cheQta.isSelected()));

            java.sql.Connection conn = it.tnx.Db.getConn();
            JasperPrint jasperPrint = JasperManager.fillReport(jasperReport, parameters, conn);
            InvoicexUtil.apriStampa(jasperPrint);
        } catch (Exception err) {
            err.printStackTrace();
            SwingUtils.showExceptionMessage(this, err);
        }

        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_menGestEvadibileClienteActionPerformed

    private void menMagazzinoUltimiPrezziActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menMagazzinoUltimiPrezziActionPerformed
        SwingUtils.showInfoMessage(this, "Questa funzione è disponibile solo nelle versioni Professional ed Enterprise");
    }//GEN-LAST:event_menMagazzinoUltimiPrezziActionPerformed

    private void menMagazzinoOrdinatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menMagazzinoOrdinatoActionPerformed
        SwingUtils.showInfoMessage(this, "Questa funzione è disponibile solo nelle versioni Professional ed Enterprise");
    }//GEN-LAST:event_menMagazzinoOrdinatoActionPerformed

    private void menAnagNazioniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagNazioniActionPerformed
        frmTipiNazioni frm = new frmTipiNazioni();
        openFrame(frm, 500, 400);
    }//GEN-LAST:event_menAnagNazioniActionPerformed

    private void menAnagConsegnaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagConsegnaActionPerformed
        frmTipiConsegna frm = new frmTipiConsegna();
        openFrame(frm, 500, 400);
    }//GEN-LAST:event_menAnagConsegnaActionPerformed

    private void menAnagScaricoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagScaricoActionPerformed
        frmTipiScarico frm = new frmTipiScarico();
        openFrame(frm, 500, 400);
    }//GEN-LAST:event_menAnagScaricoActionPerformed

    private void menStatChiCosaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menStatChiCosaActionPerformed
        SwingUtils.showInfoMessage(this, "Questa funzione è disponibile solo nelle versioni Professional ed Enterprise");
    }//GEN-LAST:event_menStatChiCosaActionPerformed

    private void menAnagComuniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagComuniActionPerformed
        frmTipiComuni frm = new frmTipiComuni();
        openFrame(frm, 500, 400);
    }//GEN-LAST:event_menAnagComuniActionPerformed

    private void menBackupDropboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menBackupDropboxActionPerformed
        SwingUtils.showInfoMessage(this, "Questa funzione è disponibile solo nelle versioni Professional ed Enterprise");
    }//GEN-LAST:event_menBackupDropboxActionPerformed

    private void menAnagCategorieArticoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagCategorieArticoliActionPerformed
        frmTipiCategorieArticoli frm = new frmTipiCategorieArticoli();
        openFrame(frm, 500, 400);
    }//GEN-LAST:event_menAnagCategorieArticoliActionPerformed

    private void menAnagSottoCategorieArticoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAnagSottoCategorieArticoliActionPerformed
        frmTipiSottoCategorieArticoli frm = new frmTipiSottoCategorieArticoli();
        openFrame(frm, 500, 400);
    }//GEN-LAST:event_menAnagSottoCategorieArticoliActionPerformed

    private void syncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncActionPerformed
        Sync.sync();
    }//GEN-LAST:event_syncActionPerformed

    private void menMagazzinoDepositiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menMagazzinoDepositiActionPerformed
        SwingUtils.showInfoMessage(this, "Questa funzione è disponibile solo nelle versioni Professional Plus ed Enterprise");

    }//GEN-LAST:event_menMagazzinoDepositiActionPerformed

    private void menMagazzinoDepositoStandardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menMagazzinoDepositoStandardActionPerformed
        SwingUtils.showInfoMessage(this, "Questa funzione è disponibile solo nelle versioni Professional Plus ed Enterprise");
    }//GEN-LAST:event_menMagazzinoDepositoStandardActionPerformed

    private void condivisioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_condivisioneActionPerformed
        InvoicexUtil.lanciaTeamViewer();
    }//GEN-LAST:event_condivisioneActionPerformed

    private void panBarr2ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panBarr2ComponentResized
        int newy = panBarr2.getLocation().y + panBarr2.getHeight();
        if (newy < 0) {
            newy = 0;
        }
        panSfuma.setLocation(0, newy);
//        System.out.println("pansfuma loc " + panSfuma.getLocation());
    }//GEN-LAST:event_panBarr2ComponentResized

    private void menExpElencoDdtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menExpElencoDdtActionPerformed
        JDialogExportElencoDdt dialog = new JDialogExportElencoDdt(main.getPadreWindow(), true);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }//GEN-LAST:event_menExpElencoDdtActionPerformed

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
    }//GEN-LAST:event_formFocusGained

    private void menAiutoInfoLicenzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAiutoInfoLicenzaActionPerformed
        //se gioni < 0 è scaduto
        String data_scadenza = null;
        Integer giorni = null;
        try {
            data_scadenza = main.fileIni.getValue("plugin_invoicex", "scadenza");
            giorni = cu.i(main.fileIni.getValue("plugin_invoicex", "scadenza_giorni"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (data_scadenza == null || data_scadenza.length() == 0) {
            SwingUtils.showInfoMessage(this, "Non abbiamo trovato informazioni sulla tua licenza");
        } else {
            JDialogInfoLicenza info = new JDialogInfoLicenza(main.getPadreFrame(), true);
            info.versione.setText(main.versione);
            String filever = "";
            if (main.versione.equalsIgnoreCase("enterprise")) {
                filever = "ent";
            }
            if (main.versione.equalsIgnoreCase("professional plus")) {
                filever = "proplus";
            }
            if (main.versione.equalsIgnoreCase("professional")) {
                filever = "pro";
            }
            System.out.println("filever = " + filever);
            info.icona.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/" + filever + ".png"))); // NOI18N
            if (giorni < 0) {
                info.scadenza.setText("Scaduta da " + giorni + " giorni, il " + data_scadenza);
            } else {
                info.scadenza.setText("Scade tra " + giorni + " giorni, il " + data_scadenza);
            }
            info.setLocationRelativeTo(null);
            info.setVisible(true);
        }
    }//GEN-LAST:event_menAiutoInfoLicenzaActionPerformed

    private void sync_wizActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sync_wizActionPerformed
        if (SwingUtils.showYesNoMessage(this, "Sicuro di azzerare ?")) {
            try {
                //killo mysqld-...
                Runtime.getRuntime().exec("taskkill /f /im mysql*");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                //elimino run_sync_1    
                org.apache.commons.io.FileUtils.deleteDirectory(new File("."));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                //copio da run_sync_1_blank a run_sync_1
                org.apache.commons.io.FileUtils.copyDirectory(new File("../run_sync_1_blank"), new File("."));
                //proseguo quasi normale
                Sync.test1 = true;
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtils.showExceptionMessage(null, e);
            }

            JFrame dummyframe = new JFrame();
            dummyframe.setIconImage(main.getLogoIcon());
            wizard = new JFrameWizardDb();
            wizard.exit = true;
            //wizard.setSize(wizard.getWidth(), 380);
            wizard.pack();
            wizard.setLocationRelativeTo(null);
            progress = new JDialogProgress(wizard, false);

            wizard.server.setText("linux");
            wizard.nomedb.setText("inv_sync_1");
            wizard.username.setText("inv_sync_1");
            wizard.password.setText("2535Lab4");

            wizard.server.setText("linux");
            wizard.nomedb.setText("inv_test_sync_master");
            wizard.username.setText("inv_sync_1");
            wizard.password.setText("aaa");

            wizard.setVisible(true);
        }
    }//GEN-LAST:event_sync_wizActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        Sync.aggiornaDebug(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void popMenuFrameSalvaDimensioniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popMenuFrameSalvaDimensioniActionPerformed
        Object source = evt.getSource();
        System.out.println("salva dimensioni frame evt:" + evt);

        try {
            Component invoker = ((JPopupMenu) ((JMenuItem) source).getParent()).getInvoker();
            if (!(invoker instanceof JInternalFrame)) {
                invoker = invoker.getParent();
            }
            if (invoker instanceof JInternalFrame) {
                JInternalFrame frame = (JInternalFrame) invoker;

                System.out.println("salva dimensioni frame");

                Properties p = new SortedProperties();
                String nomefile = System.getProperty("user.home") + "/.invoicex/finestre.txt";

                try {
                    if (new File(nomefile).exists()) {
                        p.load(new FileInputStream(nomefile));
                    }
                } catch (IOException ex) {
                    SwingUtils.showExceptionMessage(frame, ex);
                }

                String frame_name = SwingUtils.getFrameId(frame);

                p.put(frame_name + "_width", cu.s(frame.getWidth()));
                p.put(frame_name + "_height", cu.s(frame.getHeight()));

                try {
                    p.store(new FileOutputStream(nomefile), "");
                } catch (IOException ex) {
                    SwingUtils.showExceptionMessage(frame, ex);
                }

            }
        } catch (Exception e) {
            SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
        }

    }//GEN-LAST:event_popMenuFrameSalvaDimensioniActionPerformed

    private void popMenuFrameResetDimensioniActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popMenuFrameResetDimensioniActionPerformed
        Object source = evt.getSource();
        try {
            Component invoker = ((JPopupMenu) ((JMenuItem) source).getParent()).getInvoker();
            if (!(invoker instanceof JInternalFrame)) {
                invoker = invoker.getParent();
            }
            if (invoker instanceof JInternalFrame) {
                JInternalFrame frame = (JInternalFrame) invoker;

                System.out.println("reset dimensioni frame");

                Properties p = new SortedProperties();
                String nomefile = System.getProperty("user.home") + "/.invoicex/finestre.txt";

                try {
                    if (new File(nomefile).exists()) {
                        p.load(new FileInputStream(nomefile));
                    }
                } catch (IOException ex) {
                    SwingUtils.showExceptionMessage(frame, ex);
                }

                String frame_name = SwingUtils.getFrameId(frame);

                List toremove = new ArrayList();
                for (Object key : p.keySet()) {
                    if (cu.s(key).startsWith(frame_name)) {
                        toremove.add(key);
                    }
                }
                for (Object ktoremove : toremove) {
                    p.remove(ktoremove);
                }

                try {
                    p.store(new FileOutputStream(nomefile), "");
                } catch (IOException ex) {
                    SwingUtils.showExceptionMessage(frame, ex);
                }

                Map dimSalvate = getDimensioniFrame(frame, (Map) oldFramesWidth.get(frame_name), null, null);
                if (dimSalvate != null) {
                    int w = cu.i0(dimSalvate.get("width"));
                    int h = cu.i0(dimSalvate.get("height"));
                    if (w > 0 && h > 0) {
                        frame.setSize(w, h);
                    }
                }

            }
        } catch (Exception e) {
            SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
        }
    }//GEN-LAST:event_popMenuFrameResetDimensioniActionPerformed

    private void ricalcolo_totali_fattureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ricalcolo_totali_fattureActionPerformed
        try {
            List<Map> list = dbu.getListMap(Db.getConn(), "select id from test_fatt where data >= '2016-06-01'");
            for (Map m : list) {
                Integer id = cu.i(m.get("id"));

                Map testa = dbu.getListMap(Db.getConn(), "select * from test_fatt where id = " + id).get(0);
                List<Map> righe = dbu.getListMap(Db.getConn(), "select * from righ_fatt where id_padre = " + id);
                Documento2 d = new Documento2(testa, righe);
                d.setPrezziIvati(true);
                d.calcolaTotali();
                String sql = "update test_fatt set totale_imponibile = " + dbu.sql(d.getTotaleImponibile()) + ", totale_iva = " + dbu.sql(d.getTotaleIva()) + " where id = " + id;
                System.out.println("sql = " + sql);
                dbu.tryExecQuery(Db.getConn(), sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_ricalcolo_totali_fattureActionPerformed

    public void setNascondimenu(boolean value) {
        main.fileIni.setValue("pref", "nascondi_menu", value);
        nascondimenu = value;
        popnascondi.setSelected(value);
        showmenu();
        checkPanBarr2();
        jLayeredPane1ComponentResized(null);
    }

    public void setPosBarr2(float value) {
        posBarr2 = value;
//        panBarr2.setLocation(0, (int) (value * panBarr2.getHeight()) - panBarr2.getHeight() - 1);
//        System.out.println("setPosBarr2 value = " + value + " nuovay:" + ((int) (value * panBarr2.getHeight()) - panBarr2.getHeight() - 1));
        panBarr2.setLocation(0, (int) (value * panBarr2.getHeight()) - panBarr2.getHeight() - 1);
        panSfuma.setLocation(0, (int) ((value * panBarr2.getHeight()) - panBarr2.getHeight() - 1) + panBarr2.getHeight());
    }

    private void hidemenu() {
        if (!nascondimenu) {
            return;
        }
        if (menuvis) {
            timeline.replayReverse();
        }
        menuvis = false;
    }

    private void showmenu() {
        lastshow = System.currentTimeMillis();
        if (menuvis == false) {
            menuvis = true;
            timeline.play();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBox autoUpdateTableSync;
    private javax.swing.JButton btnPlugins;
    private javax.swing.JButton butArticoli;
    private javax.swing.JButton butClieForn;
    private javax.swing.JButton butDdt;
    private javax.swing.JButton butDdtAcquisto;
    private javax.swing.JButton butFatture;
    private javax.swing.JButton butFattureAcquisto;
    private javax.swing.JButton butOrdini;
    private javax.swing.JButton butOrdiniAcquisto;
    private javax.swing.JButton butRiscattaAcquisto;
    private javax.swing.JButton butSituClie;
    private javax.swing.JButton butVnc;
    private javax.swing.JMenuItem condivisione;
    public javax.swing.JDesktopPane desktop;
    public javax.swing.JInternalFrame frame_debug;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JLabel labmess2;
    public org.jdesktop.swingx.JXBusyLabel lblInfoLoading2;
    public javax.swing.JMenu menAchievo;
    public javax.swing.JMenuItem menAchievoOre;
    public javax.swing.JMenu menAgenti;
    private javax.swing.JMenuItem menAgentiAnagrafica;
    private javax.swing.JMenuItem menAgentiSituazione;
    public javax.swing.JMenu menAiuto;
    private javax.swing.JMenuItem menAiutoInfo;
    private javax.swing.JMenuItem menAiutoInfoLicenza;
    private javax.swing.JMenuItem menAnagArti;
    private javax.swing.JMenuItem menAnagAspettoEsterioreBeni;
    private javax.swing.JMenuItem menAnagAzienda;
    private javax.swing.JMenuItem menAnagBancAbi;
    private javax.swing.JMenuItem menAnagCatCli;
    private javax.swing.JMenuItem menAnagCategorieArticoli;
    private javax.swing.JMenuItem menAnagCausaliTrasporto;
    private javax.swing.JMenuItem menAnagClie;
    private javax.swing.JMenuItem menAnagComuni;
    private javax.swing.JMenuItem menAnagConsegna;
    private javax.swing.JMenuItem menAnagDatiAzieBanc;
    private javax.swing.JMenuItem menAnagListini;
    private javax.swing.JMenuItem menAnagNazioni;
    private javax.swing.JMenuItem menAnagPorti;
    private javax.swing.JMenuItem menAnagScarico;
    private javax.swing.JMenuItem menAnagSottoCategorieArticoli;
    private javax.swing.JMenuItem menAnagTipiIva;
    private javax.swing.JMenuItem menAnagTipiPaga;
    private javax.swing.JMenuItem menAnagVettori;
    private javax.swing.JMenuItem menAnagraficaDurataConsulenza;
    private javax.swing.JMenuItem menAnagraficaDurataContratto;
    private javax.swing.JMenuItem menAnagraficaEmissioneFatture;
    public javax.swing.JMenuItem menAnagraficaRuoli;
    private javax.swing.JMenuItem menAnagraficaStatiOrdine;
    private javax.swing.JMenuItem menAnagraficaUsers;
    public javax.swing.JMenu menAnagrafiche;
    public javax.swing.JMenuItem menBackupDropbox;
    public javax.swing.JMenuBar menBar;
    private javax.swing.JMenuItem menCambiaPassUtente;
    public javax.swing.JMenu menContab;
    private javax.swing.JMenu menDocAcquisto;
    private javax.swing.JMenu menDocVendita;
    private javax.swing.JMenuItem menExpAcqVend;
    private javax.swing.JMenuItem menExpElencoDdt;
    private javax.swing.JMenuItem menExpVendPeroni;
    public javax.swing.JMenu menFine;
    public javax.swing.JCheckBoxMenuItem menFunzioniManutenzione;
    public javax.swing.JMenu menGest;
    private javax.swing.JMenuItem menGestDdt;
    private javax.swing.JMenuItem menGestDdtAcquisto;
    public javax.swing.JMenuItem menGestEvadibileCliente;
    private javax.swing.JMenuItem menGestFatture;
    private javax.swing.JMenuItem menGestFattureAcquisto;
    public javax.swing.JMenuItem menGestImpegnato;
    public javax.swing.JMenuItem menGestImpegnatoProduttore;
    private javax.swing.JMenuItem menGestOrdini;
    private javax.swing.JMenuItem menGestOrdiniAcquisto;
    private javax.swing.JMenuItem menGestRistDistRiba;
    private javax.swing.JMenuItem menGestSituClie;
    public javax.swing.JMenu menGestUtenti;
    private javax.swing.JMenuItem menGestioneIvaStampaRegistro;
    private javax.swing.JMenuItem menGuida;
    public javax.swing.JMenu menIva;
    public javax.swing.JMenu menMagazzino;
    public javax.swing.JMenuItem menMagazzinoDepositi;
    public javax.swing.JMenuItem menMagazzinoDepositoStandard;
    private javax.swing.JMenuItem menMagazzinoMovimenti;
    public javax.swing.JMenuItem menMagazzinoOrdinato;
    private javax.swing.JMenuItem menMagazzinoStampaGiacenze;
    private javax.swing.JMenuItem menMagazzinoStampaGiacenzeLotti;
    private javax.swing.JMenuItem menMagazzinoStampaGiacenzeMatricole;
    private javax.swing.JMenuItem menMagazzinoStampaMovimenti;
    public javax.swing.JMenuItem menMagazzinoUltimiPrezzi;
    private javax.swing.JMenuItem menManutezione;
    public javax.swing.JMenu menSnj;
    public javax.swing.JMenuItem menStatChiCosa;
    public javax.swing.JMenu menStatistiche;
    private javax.swing.JMenuItem menStatisticheOrdBolFat;
    public javax.swing.JMenuItem menTestInternet;
    private javax.swing.JMenuItem menTotFatture;
    public javax.swing.JMenu menUtil;
    public javax.swing.JMenuItem menUtilAggi;
    private javax.swing.JMenuItem menUtilBackup;
    public javax.swing.JMenuItem menUtilBackupOnline;
    private javax.swing.JMenuItem menUtilCambiaFont;
    private javax.swing.JMenuItem menUtilCambiaPassword;
    private javax.swing.JMenuItem menUtilCheck;
    private javax.swing.JMenuItem menUtilDb;
    private javax.swing.JMenuItem menUtilImpo;
    private javax.swing.JMenuItem menUtilPlugins;
    private javax.swing.JMenuItem menUtilRestore;
    public javax.swing.JMenuItem menUtilRestoreOnline;
    private javax.swing.JMenuItem menVnc;
    public javax.swing.JPanel panBarr;
    public javax.swing.JPanel panBarr2;
    private javax.swing.JPanel panSfuma;
    private javax.swing.JPopupMenu popMenuFrame;
    private javax.swing.JMenuItem popMenuFrameResetDimensioni;
    private javax.swing.JMenuItem popMenuFrameSalvaDimensioni;
    private javax.swing.JPopupMenu popbarr;
    private javax.swing.JCheckBoxMenuItem popnascondi;
    private javax.swing.JButton ricalcolo_totali_fatture;
    public javax.swing.JScrollPane scrollDesktop;
    public javax.swing.JPopupMenu.Separator sepAggBackup;
    private javax.swing.JButton sync;
    private javax.swing.JButton sync_wiz;
    public javax.swing.JTable tabsync;
    public javax.swing.JTextArea text_debug;
    // End of variables declaration//GEN-END:variables
    private int framesCount = 0;

    public void openFrame(JInternalFrame frame) {
        openFrameInteger(frame, null, null, null, null);
    }

    public void openFrame(JInternalFrame frame, int larg, int alte) {
        openFrameInteger(frame, larg, alte, null, null);
    }

    public void openFrameInteger(JInternalFrame frame, Integer larg, Integer alte) {
        openFrameInteger(frame, larg, alte, null, null);
    }

    public void openFrame(JInternalFrame frame, int larg, int alte, int top, int left) {
        openFrameInteger(frame, larg, alte, top, left);
    }

    public void openFrameInteger(JInternalFrame frame, Integer larg, Integer alte, Integer top, Integer left) {

        //sovrascirvo larg
        if (frame instanceof frmTestOrdine) {
            larg = 800;
        } else if (frame instanceof frmTestFatt) {
            larg = 800;
        } else if (frame instanceof frmTestDocu) {
            larg = 800;
        } else if (frame instanceof frmTestFattAcquisto) {
            larg = 800;
        }

        //aumento in percentuale in base al font
        //schermate basate su font 11
        if (larg != null) {
            Integer font_size = CastUtils.toInteger0(main.fileIni.getValue("pref", "font_size", CastUtils.toString(main.def_font.getSize())));
            double coeff = (double) font_size / 11d;
            larg = (int) (larg * coeff);
            //alte = (int)(alte * coeff);   //l'altezza di solito viene fatta per differenza dall'alatezza del desktop
        }

        //se presenti dimensioni salvate carico quelle
        Map dimSalvate = getDimensioniFrame(frame, null, larg, alte);
        if (dimSalvate != null) {
            int w = cu.i0(dimSalvate.get("width"));
            if (w > 0) {
                larg = w;
            }
            int h = cu.i0(dimSalvate.get("height"));
            if (h > 0) {
                alte = h;
            }
        }

        if (controllaPermesso(frame)) {
            framesCount++;

            frame.setTitle(frame.getTitle());

            if (PlatformUtils.isMac()) {
                String osxv = System.getProperty("os.version");
                if (osxv.startsWith("10.10") || osxv.startsWith("10.11")) {
                    frame.putClientProperty("JInternalFrame.frameType", "normal");
                }
            }

            frame.addComponentListener(new ComponentListener() {

                public void componentMoved(ComponentEvent e) {
                    if (e.getComponent().getLocation().y < 2) {
                        e.getComponent().setLocation(e.getComponent().getLocation().x, 2);
                        //per annullare il drag and drop
//                        try {
//                            Robot robot = new Robot();
//                            robot.mouseRelease(InputEvent.BUTTON1_MASK);
//                        } catch (AWTException er) {
//                            er.printStackTrace();
//                        }
                    }
                }

                public void componentShown(ComponentEvent e) {
                }

                public void componentHidden(ComponentEvent e) {
                }

                public void componentResized(ComponentEvent e) {
                    if (e.getComponent().getLocation().y < 2) {
                        e.getComponent().setLocation(e.getComponent().getLocation().x, 2);
                    }
                }
            });

            this.desktop.add(frame);

            if (main.iniFinestreGrandi != true) {

                if (left == null || top == null) {
                    left = 20 * (framesCount % 5);
                    top = 20 * (framesCount % 5);
                } else {
                    if (left < 0) {
                        left = 0;
                    }
                    if (top < 0) {
                        top = 0;
                    }
                }
                if (larg == null) {
                    frame.setLocation(left, top);
                } else {
                    frame.setBounds(left, top, larg, alte);
                }
            }

            frame.setVisible(true);

            if (main.iniFinestreGrandi == true) {
                try {
                    frame.setMaximum(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    private boolean controllaPermesso(JInternalFrame frame) {
        if (frame instanceof frmTestFatt) {
            if (!main.utente.getPermesso(Permesso.PERMESSO_FATTURE_VENDITA, Permesso.PERMESSO_TIPO_SCRITTURA)) {
                SwingUtils.showErrorMessage(main.getPadrePanel(), "Non hai i permessi per accedere a questa funzionalità", "Impossibile accedere", true);
                return false;
            }
        } else if (frame instanceof frmTestFattAcquisto) {
            if (!main.utente.getPermesso(Permesso.PERMESSO_FATTURE_ACQUISTO, Permesso.PERMESSO_TIPO_SCRITTURA)) {
                SwingUtils.showErrorMessage(main.getPadrePanel(), "Non hai i permessi per accedere a questa funzionalità", "Impossibile accedere", true);
                return false;
            }
        } else if (frame instanceof frmTestDocu) {
            frmTestDocu frm = (frmTestDocu) frame;
            int permesso = frm.acquisto ? Permesso.PERMESSO_DDT_ACQUISTO : Permesso.PERMESSO_DDT_VENDITA;
            if (!main.utente.getPermesso(permesso, Permesso.PERMESSO_TIPO_SCRITTURA)) {
                SwingUtils.showErrorMessage(main.getPadrePanel(), "Non hai i permessi per accedere a questa funzionalità", "Impossibile accedere", true);
                return false;
            }
        } else if (frame instanceof frmTestOrdine) {
            frmTestOrdine frm = (frmTestOrdine) frame;
            int permesso = frm.acquisto ? Permesso.PERMESSO_ORDINI_ACQUISTO : Permesso.PERMESSO_ORDINI_VENDITA;
            if (!main.utente.getPermesso(permesso, Permesso.PERMESSO_TIPO_SCRITTURA)) {
                SwingUtils.showErrorMessage(main.getPadrePanel(), "Non hai i permessi per accedere a questa funzionalità", "Impossibile accedere", true);
                return false;
            }
        } else if (frame instanceof frmArtiCM || frame instanceof frmArtiConListino || frame instanceof frmArtiConListinoCM || frame instanceof frmArtiConListinoPerle) {
            if (!main.utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_ARTICOLI, Permesso.PERMESSO_TIPO_LETTURA)) {
                SwingUtils.showErrorMessage(main.getPadrePanel(), "Non hai i permessi per accedere a questa funzionalità", "Impossibile accedere", true);
                return false;
            }
        } else if (frame instanceof frmClie) {
            if (!main.utente.getPermesso(Permesso.PERMESSO_ANAGRAFICA_CLIENTI, Permesso.PERMESSO_TIPO_LETTURA)) {
                SwingUtils.showErrorMessage(main.getPadrePanel(), "Non hai i permessi per accedere a questa funzionalità", "Impossibile accedere", true);
                return false;
            }
        }

        return true;
    }

    public int getNextFrameTop() {
        return 20 * ((framesCount + 1) % 5);
    }


    /*-----------------------------------*/
    public JDialogPlugins showplugins(boolean scaricatutti) {
        final JDialogPlugins plugins = new JDialogPlugins(main.getPadreWindow(), true, scaricatutti);
        plugins.setLocationRelativeTo(null);
        main.dialogPlugins = plugins;
        plugins.setVisible(true);

        return plugins;
    }

    private void checkPanBarr() {
//        Component[] comps = panBarr.getComponents();
//        int maxy = 0;
//        int h = 0;
//        for (Component comp : comps) {
//            if (comp.getLocation().getY() > maxy) {
//                maxy = (int) comp.getLocation().getY();
//                h = comp.getHeight();
//            }
//        }
////        System.err.println("checkPanBarr maxy:" + maxy + " h:" + h);
//        int hei = maxy + h + 4;
//        if (hei < butOrdini.getHeight() + 10) {
//            hei = butOrdini.getHeight() + 10;
//        }
//        panBarr.setPreferredSize(new Dimension(panBarr.getWidth(), hei));
//        panBarr.revalidate();
//        panBarr.validate();
    }
    Timeline t = new Timeline(panBarr2);
    Timeline t2 = new Timeline(scrollDesktop);
    DelayedExecutor delay_checkPanBarr2 = new DelayedExecutor(new Runnable() {
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    System.err.println("!!! delay_checkPanBarr2 ");
                    if (!isVisible()) {
                        System.err.println("!!! delay_checkPanBarr2 return !visible");
                        return;
                    }

                    if (!panBarr2.isVisible()) {
                        panBarr2.setSize(0, 0);
                        return;
                    }

//                    if (bcheckPanBarr2) {
//                        return;
//                    }
                    System.out.println("t.getState():" + t.getState());
                    if (t != null && (t.getState() == TimelineState.READY || t.getState() == TimelineState.PLAYING_FORWARD)) {
                        //devo annullare quelli prima e rifare
                        try {
                            t.end();
                        } catch (Exception e) {
                        }
                        t = null;
                    }
                    if (t2 != null && (t2.getState() == TimelineState.READY || t2.getState() == TimelineState.PLAYING_FORWARD)) {
                        //devo annullare quelli prima e rifare
                        try {
                            t2.end();
                        } catch (Exception e) {
                        }
                        t2 = null;
                    }

                    bcheckPanBarr2 = true;
                    panBarr2.setSize(getWidth(), panBarr2.getHeight());
                    Component[] comps = panBarr2.getComponents();
                    panBarr2.validate();
                    panBarr2.revalidate();
//                    scrollDesktop.validate();
//                    desktop.validate();
                    int oldh = panBarr2.getHeight();
//SwingUtils.showInfoMessage(main.getPadreFrame(), "oldh " + oldh);
                    //SwingUtils.showInfoMessage(main.getPadreFrame(), "oldh:" + oldh);
                    int maxy = 0;
                    int h = 0;
                    for (Component comp : comps) {
                        if (comp.getLocation().getY() > maxy) {
                            maxy = (int) comp.getLocation().getY();
                            h = comp.getHeight();
                        }
                    }

                    int hei = maxy + h + 4;
                    if (hei < butOrdini.getHeight() + 10) {
                        hei = butOrdini.getHeight() + 10;
                    }
                    try {
                        System.err.println("checkPanBarr2 timeline -> hei:" + hei);
                        if (!menuvis) {
                            panBarr2.setLocation(0, -500);
                        }

                        t = new Timeline(panBarr2);
                        t2 = new Timeline(scrollDesktop);

                        t.setEase(new Spline(0.8f));
                        t2.setEase(new Spline(0.8f));
                        t.setInitialDelay(100);
                        t.setDuration(250);
                        t2.setInitialDelay(100);
                        t2.setDuration(250);

//                        if (panBarr2.getHeight() > hei) {
//                            System.err.println("!!! timeline pre, faccio resize pre edt:" + SwingUtilities.isEventDispatchThread());
//                            scrollDesktop.setSize(scrollDesktop.getWidth(), scrollDesktop.getHeight() + (panBarr2.getHeight() - hei));
////                            scrollDesktop.setSize(scrollDesktop.getWidth(), scrollDesktop.getHeight() + (panBarr2.getHeight()));
//                            scrollDesktop.setLocation(0, hei);
//                        }
                        System.out.println("delay_checkPanBarr2 size: " + panBarr2.getSize().height + " tosize:" + hei + " location:" + panBarr2.getLocation());
                        t.addPropertyToInterpolate("size", panBarr2.getSize(), new Dimension(panBarr2.getWidth(), hei));
//                        t.addCallback(new TimelineCallbackAdapter() {
//
//                            @Override
//                            public void onTimelinePulse(float durationFraction, float timelinePosition) {
//                                SwingUtilities.invokeLater(new Runnable() {
//                                    public void run() {
//                                        panSfuma.setLocation(0, panBarr2.getLocation().y + panBarr2.getHeight() + 1);
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float durationFraction, float timelinePosition) {
//                                super.onTimelineStateChanged(oldState, newState, durationFraction, timelinePosition); //To change body of generated methods, choose Tools | Templates.
//                            }
//                            
//                        });
                        int hei2 = scrollDesktop.getHeight() + (panBarr2.getHeight() - hei);
                        int top2 = scrollDesktop.getLocation().y - (panBarr2.getHeight() - hei);
                        t2.addPropertyToInterpolate("size", new Dimension(getWidth(), scrollDesktop.getHeight()), new Dimension(getWidth(), hei2));
                        System.out.println("scroll da hei: " + scrollDesktop.getHeight() + " a hei:" + hei2);
                        t2.addPropertyToInterpolate("location", scrollDesktop.getLocation(), new Point(scrollDesktop.getLocation().x, top2));

                        t.addCallback(new TimelineCallbackAdapter() {
                            @Override
                            public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float durationFraction, float timelinePosition) {
                                if (newState == TimelineState.DONE) {
                                    SwingUtils.inEdt(new Runnable() {
                                        public void run() {
                                            panBarr2.validate();
                                            validate();
                                            desktop.validate();
                                            scrollDesktop.validate();
                                            scrollDesktop.validate();
                                            jLayeredPane1ComponentResized(null);
                                            repaint();
                                        }
                                    });
                                }
                            }
                        });
                        t.play();
                        if (!nascondimenu) {
                            t2.play();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //        panBarr2.setSize(new Dimension(panBarr2.getWidth(), hei));
                    //        panBarr2.validate();
                    bcheckPanBarr2 = false;
                }
            });
        }
    }, 250);

    public void checkPanBarr2() {
        if (initcomps) {
            System.out.println("checkPanBarr2 return initcomps");
            return;
        }
        if (getTopLevelAncestor() == null) {
            System.out.println("checkPanBarr2 return getTopLevelAncestor = null");
            return;
        }
        if (!getTopLevelAncestor().isVisible()) {
            System.out.println("checkPanBarr2 return getTopLevelAncestor !visible");
            return;
        }
        System.out.println("checkPanBarr2 update");
        delay_checkPanBarr2.update();
    }

    public void apridatiazienda() {

        //controllo se ha inserito i dati azienda
        String datiPrima = "";
        String datiDopo = "";
        ResultSetMetaData m;

        String sql = "select " + main.campiDatiAzienda + " from dati_azienda";
        ResultSet r = Db.openResultSet(sql);
        try {
            m = r.getMetaData();
            if (r.next()) {
                for (int i = 1; i <= m.getColumnCount(); i++) {
                    datiPrima += r.getString(i);
                }
            }
            r.close();
        } catch (Exception err) {
            err.printStackTrace();
        }

        JDialogDatiAzienda datiAzienda = new JDialogDatiAzienda(main.getPadreWindow(), true);
        datiAzienda.setLocationRelativeTo(null);
        datiAzienda.setVisible(true);

        if (datiAzienda.annullato) {
            return;
        }

        r = Db.openResultSet(sql);
        try {
            m = r.getMetaData();
            if (r.next()) {
                for (int i = 1; i <= m.getColumnCount(); i++) {
                    datiDopo += r.getString(i);
                }
            }
            r.close();
        } catch (Exception err) {
            err.printStackTrace();
        }

        //controllo se sono cambiati i dati / non più, richiedo sempre l'invio
//        if (!datiPrima.equalsIgnoreCase(datiDopo)) {
        main.attivazione.setFlagDatiModificati(true);
        main.attivazione.setDatiAziendaInseriti(true);
        main.attivazione.setFlagDatiInviatiSuModifica(false);

        //nuovo controllo registrazione
        if (!main.attivazione.isFlagDatiInviatiSuModifica()) {
            if (main.attivazione.isFlagDatiModificati()) {
                //chiedo se vuol inviare i dati a tnx
//                if (JOptionPane.showConfirmDialog(main.getPadreWindow(), "Acconsenti all'invio dell' Anagrafica Aziendale (Ragione Sociale, Partita Iva, ecc..) a TNX snc ?\nI dati verranno utilizzati soltanto a scopo statistico,\nse non vengono inviati puoi comunque continuare ad usare il programma.\nPer utilizzare i Plugin in prova o gia' acquistati e' necessario l'invio dei dati.", "Registrazione", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION) {
                Boolean registraini = main.fileIni.getValueBoolean("varie", "dati_azienda_registra_invoicex", true);
                if (registraini) {
                    System.out.println("attivazione: invio dati modifcati...");
                    boolean ret = main.attivazione.registra();
                    if (ret) {
                        main.attivazione.setDatiAziendaInseriti(true);
                        main.attivazione.setFlagDatiInviatiPrimaVolta(true);
                        main.attivazione.setFlagDatiModificati(false);
                        main.attivazione.setFlagDatiInviatiSuModifica(true);
                        Runnable run = new Runnable() {
                            public void run() {
//                                JOptionPane.showMessageDialog(main.getPadreWindow(), "Invio completato.\nGrazie", "Informazioni", JOptionPane.INFORMATION_MESSAGE);
                                main.getPadre().aggiornaTitle();
                            }
                        };
                        if (SwingUtilities.isEventDispatchThread()) {
                            SwingUtilities.invokeLater(run);
                        } else {
                            run.run();
                        }
                    }
                    System.out.println("attivazione: dati modifica ri-registrazione " + ret);
                    System.out.println("attvazione: invio dati modifcati...finito");
                }
            } else {
                System.out.println("attivazione: non richiedo invio dati perche' ancora non inseriti");
            }
        }
//        }
    }

    public void exitForm(java.awt.event.WindowEvent evt) {

        java.awt.Component[] arrComp = this.desktop.getComponents();
        int flag_aperta = 0;

        for (int i = 0; i < arrComp.length; i++) {
            System.out.println("arrComp[i].getClass().getName() = " + arrComp[i].getClass().getName());

//            if (arrComp[i].getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFatt")) {
            if (arrComp[i].getClass().getName().startsWith("gestioneFatture.frmTest")) {
                flag_aperta = 1;
            }
        }

        if (flag_aperta == 0) {
            main.exitMain();
        } else //            javax.swing.JOptionPane.showMessageDialog(main.getPadre(), "<html><b>Ci sono dei documenti aperti, vuoi comunque chiudere ?</b><br><small>Le eventuali modifiche non verranno salvate</small></html>", "Attenzione", javax.swing.JOptionPane.YES_OPTION);
        {
            if (SwingUtils.showYesNoMessage(main.getPadreWindow(), "<html><b>Ci sono dei documenti aperti, vuoi comunque chiudere ?</b><br><small>Le eventuali modifiche non verranno salvate</small></html>", "Attenzione")) {
                main.exitMain();
            }
        }

    }

    //-------------------------------------------------------------------------------
    public JDesktopPane getDesktopPane() {
        return desktop;
    }

    static public JInternalFrame getCurrenWindow() {
        return INSTANCE.desktop.getSelectedFrame();
    }

    static public String getBuildH(String build) {
        return StringUtils.left(build, 4) + "-" + StringUtils.substring(build, 4, 6) + "-" + StringUtils.right(build, 2);
    }

    public void callBackupOnline() {
        menUtilBackupOnline.getActionListeners()[0].actionPerformed(null);
    }

    public void segnalaRapporti() {
        if (main.getPersonalContain("adesivi") || main.getPersonalContain("noteclienti")) {
            try {
                String sql = "select * from clie_forn_rapporti where segnalato = 'N' having data_avviso <= CURDATE() order by data_avviso";
                ResultSet avvisi = Db.openResultSet(sql);

                if (avvisi.next()) {
                    frmSegnalaRapporti frm = new frmSegnalaRapporti();
                    openFrame(frm, 590, 500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public Frame topFrame = null;

    public Frame getFrame() {
        if (topFrame != null) {
            return topFrame;
        } else {
            Component c = this;
            while ((c != null) && !(c instanceof Frame)) {
                System.out.println("getFrame: c:" + c);
                c = c.getParent();
            }
            topFrame = (Frame) c;
            return (Frame) c;
        }
    }

    private void linkDonazioneActionPerformed(java.awt.event.ActionEvent evt) {

    }

    public void visualizzaNote() {
        //controllo se far vedere note di rilascio
        //    File fnote = new File("note_rilascio.html");
        File fnote = new File(main.wd + "note_rilascio.inc.php");
        JDialogNoteRilascio note = new JDialogNoteRilascio(main.getPadreWindow(), true);
        try {
            String snote = FileUtils.readContentCp1252(fnote);

            snote = "<html>"
                    + "<head>"
                    + "</head>"
                    + "<body>"
                    + "<div style='font-family: Courier New, monospace; font-size: 9px;'>"
                    + snote;
            snote = StringUtils.replace(snote, "<br />", "<br>");
            snote = StringUtils.replace(snote, "<br/>", "<br>");
            snote += "</div>"
                    + "</body>"
                    + "</html>";
            note.jTextPane1.setText(snote);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        note.jTextPane1.setCaretPosition(0);
        //        note.jTextPane1.sc
        note.setLocationRelativeTo(null);
        note.setVisible(true);
    }

    private void funzioniDiManutenzione(boolean selected) {
        if (selected) {
            frame_debug.setVisible(true);
            try {
                main.timerMem.schedule(main.timerMemTask, 1000 * 1, 1000 * 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            tail = new JLogTailerFrame("Log", 800, 600);
            try {
                tail.startLogging(new File(System.getProperty("user.home") + "/.invoicex/invoicex.log"), null, new Rectangle(600, 400));
            } catch (Exception e) {
                e.printStackTrace();
            }

            tnxDbPanel.debug = true;
        }
    }
    List<MenuPanelNews> news = new ArrayList();

    public void showNews(JSONObject n) {
        String id = (String) n.get("id");
        String autoclose = CastUtils.toString(n.get("autoclose"));
        String autoclose_dopo_visualizzazioni = CastUtils.toString(n.get("autoclose_dopo_visualizzazioni"));
        String titolo = (String) n.get("titolo");
        String corpo = StringUtils.replace((String) n.get("corpo"), "\n", "");
        String data = (String) n.get("data");
        String chiudibile = CastUtils.toString(n.get("chiudibile"));
        String non_visualizzare_piu = CastUtils.toString(n.get("non_visualizzare_piu"));
        String testo_link = (String) n.get("testo_link");
        String chiudi_su_click = (String) n.get("chiudi_su_click");
        final String azione = (String) n.get("azione");
        String stipo = (String) n.get("tipo");
        TipoNews tipo = TipoNews.LAMPADINA;
        if (stipo.equals("SOLE")) {
            tipo = TipoNews.SOLE;
        }
        if (stipo.equals("LAMPADINA")) {
            tipo = TipoNews.LAMPADINA;
        }
        showNews(id, titolo, corpo, data, azione, testo_link, tipo, autoclose, chiudibile, non_visualizzare_piu, autoclose_dopo_visualizzazioni, chiudi_su_click);
    }

    public void showNews(final String id, final String titolo, final String corpo, final String data, final String azione, final String testo_link, final TipoNews tipo, final String autoclose, final String chiudibile, final String non_visualizzare_piu, final String autoclose_dopo_visualizzazioni, final String chiudi_su_click) {
        SwingUtils.inEdt(new Runnable() {
            public void run() {
                final MenuPanelNews p = new MenuPanelNews();
                final JInternalFrame fp = new JInternalFrame("News", false, false, false, false);
                p.padre = fp;
                fp.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
                fp.setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/apps/internet-news-reader.png")));
                BasicInternalFrameUI ui = (BasicInternalFrameUI) fp.getUI();
                try {
                    DebugUtils.dump(ui);
                    Component north = ui.getNorthPane();
                    MouseMotionListener[] actions = (MouseMotionListener[]) north.getListeners(MouseMotionListener.class);
                    for (int i = 0; i < actions.length; i++) {
                        north.removeMouseMotionListener(actions[i]);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
//                ui.getNorthPane().setPrefrredSize(new Dimension(0,0));

                p.setSize(600, 1);

                if (!StringUtils.isBlank(testo_link)) {
                    p.link.setText(testo_link);
                    p.link.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (azione.startsWith("url:")) {
                                try {
                                    Thread t = new Thread("apro link " + azione) {
                                        @Override
                                        public void run() {
                                            try {
                                                SwingUtils.openUrl(new URL(azione.substring(4)));
                                            } catch (Exception e) {
                                            }
                                        }
                                    };
                                    t.start();
                                } catch (Exception err) {
                                    err.printStackTrace();
                                }
                            } else if (azione.startsWith("action:")) {
                                try {
                                    azioni.get(azione.substring(7)).actionPerformed(null);
                                } catch (NullPointerException ex) {
                                    ex.printStackTrace();
                                    SwingUtils.showErrorMessage(main.getPadreWindow(), "Errore azione '" + azione.substring(7) + "' non trovata");
                                }
                            }
                            if (chiudi_su_click != null && chiudi_su_click.equals("true")) {
                                chiudiNews(p, false, p.non.isSelected());
                            }
                        }
                    });
                } else {
                    p.link.setVisible(false);
                }
                switch (tipo) {
                    case SOLE:
                        p.icona.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/22x22/status/weather-clear.png"))); // NOI18N
                        break;
                    case LAMPADINA:
                        p.icona.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/22x22/status/dialog-information.png"))); // NOI18N
                        break;
                }
                p.id = id;
//                p.data.setText(DateUtils.formatDate(data));
                p.data.setText(data);
                p.titolo.setText(titolo);

                if (chiudibile != null && chiudibile.equals("false")) {
                    p.chiudi.setVisible(false);
                }
                if (non_visualizzare_piu != null && non_visualizzare_piu.equals("false")) {
                    p.non.setVisible(false);
                }

                p.corpo.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        p.setSize(p.getWidth(), p.link.getY() + p.link.getHeight() + 4);
                        try {
                            BasicInternalFrameUI ui = (BasicInternalFrameUI) fp.getUI();
                            Component north = ui.getNorthPane();
                            fp.setSize(p.getSize().width, p.getSize().height + north.getHeight());
                            System.out.println("set size fp: " + p.getSize());
                        } catch (Exception ex) {
                            //ex.printStackTrace();
                            fp.setSize(p.getSize().width, p.getSize().height);
                        }
                    }
                });

//                p.corpo_old.setText(corpo);
                try {
                    HTMLDocument doc = (HTMLDocument) p.corpo.getDocument();
                    StyleSheet styleSheet = doc.getStyleSheet();
                    styleSheet.addRule("body {color:#000000; font-family:Arial; margin: 2px; }");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                p.corpo.setText(corpo);

                Color sfondo = desktop.getBackground();
                Color panel = p.getBackground();
                Color mix = SwingUtils.mixColours(sfondo, panel);
                System.out.println("mix = " + mix);
                p.setBorder(BorderFactory.createLineBorder(mix));

                fp.setLayout(null);
                fp.getContentPane().add(p);

                System.out.println("p.size " + p.getSize());
                fp.setBorder(null);
                fp.setVisible(true);

                desktop.add(fp, JLayeredPane.POPUP_LAYER);
                desktop.validate();

                fp.setLocation((getWidth() / 2) - (fp.getWidth() / 2), -600);
                Timeline t = new Timeline(p);
                t.setEase(new Spline(0.9f));
                t.setInitialDelay(500);
                t.setDuration(500);
                int toy = 0;
                for (MenuPanelNews mpn : news) {
                    toy = toy + mpn.getHeight();
                }
                final int ftoy = toy;
                t.addCallback(new TimelineCallback() {
                    public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float durationFraction, float timelinePosition) {
                    }

                    public void onTimelinePulse(float durationFraction, float timelinePosition) {
                        int y = (int) ((1 - timelinePosition * (-fp.getHeight())) + (timelinePosition * ftoy)) - fp.getHeight();
                        fp.setLocation((getWidth() / 2) - (fp.getWidth() / 2), (int) y);
                    }
                });
                t.play();

//                Timeline t2 = new Timeline(p.icona);
//                t2.setEase(new Sine());
//                t2.setInitialDelay(3000);
//                t2.setDuration(500);
//                t2.addPropertyToInterpolate("location", p.icona.getLocation(), new Point(p.icona.getLocation().x, 0));
//                t2.playLoop(Timeline.RepeatBehavior.REVERSE);
                if ((autoclose == null || autoclose.length() == 0 || autoclose.equals("true") || CastUtils.toInteger0(autoclose) > 0) && (chiudibile == null || chiudibile.length() == 0 || chiudibile.equals("true"))) {
                    if (!autoclose_dopo_visualizzazioni.equals("false")) {
                        int nvis = 3;
                        if (autoclose_dopo_visualizzazioni.length() > 0) {
                            nvis = CastUtils.toInteger0(autoclose_dopo_visualizzazioni);
                        }
                        if (CastUtils.toInteger0(main.fileIni.getValue("news", "visualizzata_" + p.id, "0")) >= nvis - 1) {
                            p.non.setSelected(true);
                        }
                    }
                    Thread ttimer = new Thread("timer news") {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                sleep(1000);
                            } catch (Exception e) {
                                return;
                            }
                            for (int i = (CastUtils.toInteger0(autoclose) == 0 ? 60 : CastUtils.toInteger0(autoclose)); i >= 0; i--) {
                                final int fi = i;
                                try {
                                    sleep(1000);
                                } catch (Exception e) {
                                    return;
                                }
                                SwingUtils.inEdt(new Runnable() {
                                    public void run() {
                                        p.autochiusura.setText("(chiusura fra " + fi + " secondi)");
                                    }
                                });
                            }
                            System.err.println("chiudo news " + p.id + " da " + this);
                            chiudiNews(p, false, p.non.isSelected());
                        }
                    };
                    ttimer.start();
                    p.thread_auto_chiusura = ttimer;
                }
                p.chiudi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        chiudiNews(p, true, p.non.isSelected());
                    }
                });

                main.fileIni.setValue("news", "visualizzata_" + p.id, CastUtils.toInteger0(main.fileIni.getValue("news", "visualizzata_" + p.id, "0")) + 1);

                news.add(p);
            }
        });
    }

    public void chiudiNews(MenuPanelNews mpn, boolean chiedi, boolean non_visualizzare) {
        final MenuPanelNews p = mpn;
        JInternalFrame fp = (JInternalFrame) p.padre;

        if (!fp.isVisible()) {
            return;//già chiusa
        }
        Timeline t = new Timeline(fp);
        t.setEase(new Spline(0.8f));
        t.setInitialDelay(100);
        t.setDuration(500);

        System.out.println("chiusura mpn da: " + fp.getLocation() + " a: " + (-fp.getHeight() - 20));

        t.addPropertyToInterpolate("location", fp.getLocation(), new Point(fp.getLocation().x, -fp.getHeight() - 20));
        t.addCallback(new TimelineCallback() {
            public void onTimelineStateChanged(TimelineState oldState, TimelineState newState, float durationFraction, float timelinePosition) {
                System.out.println("newState:" + newState);
                if (newState == TimelineState.DONE) {
                    p.padre.setVisible(false);
                    desktop.remove(p.padre);
                }
            }

            public void onTimelinePulse(float durationFraction, float timelinePosition) {
            }
        });
        t.play();

        //faccio scorrere le altre
        int i = news.indexOf(p);
        for (int n = i + 1; n < news.size(); n++) {
            System.out.println("sposto " + n);
            MenuPanelNews pn = news.get(n);
            JInternalFrame fpn = (JInternalFrame) pn.padre;
            Timeline tn = new Timeline(fpn);
            tn.setEase(new Spline(0.8f));
            tn.setInitialDelay(100);
            tn.setDuration(500);
            tn.addPropertyToInterpolate("location", fpn.getLocation(), new Point(fpn.getLocation().x, fpn.getLocation().y - fp.getHeight()));
            tn.play();
        }

        news.remove(p);

        if (non_visualizzare) {
            main.fileIni.setValue("news", "non_visualizzare", main.fileIni.getValue("news", "non_visualizzare", "") + mpn.id + "|");
        }
    }

    public void resizeNews() {
        for (MenuPanelNews mpn : news) {
            JInternalFrame fp = (JInternalFrame) mpn.padre;
            fp.setLocation((getWidth() / 2) - (fp.getWidth() / 2), fp.getLocation().y);
        }
    }

    public void clickAttivazione() {
        try {
            if (main.attivazione.getIdRegistrazione() == null) {
                if (SwingUtils.showYesNoMessage(main.getPadreWindow(), "Per utilizzare le funzionalità aggiuntive devi prima registrare il programma,\nclicca su Sì per proseguire nella registrazione")) {
                    apridatiazienda();
                    if (main.attivazione.getIdRegistrazione() != null) {
                        RiscattaAcquisto.riscattaAcquisto();
                    }
                }
            } else {
                RiscattaAcquisto.riscattaAcquisto();
            }
        } catch (Exception e) {
            JDialogExc.showExc(main.getPadreWindow(), true, e);
        }
    }

    public static class MyJPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.

            if (MenuPanel.sfuma) {
                Graphics2D g2 = (Graphics2D) g;

                int w = getWidth();
                int h = getHeight();

//                Area outside = new Area(new Rectangle2D.Double(0, 0, w, h));
//                for (JInternalFrame ifr : main.getPadrePanel().getDesktopPane().getAllFrames()) {
//                    outside.subtract(new Area(ifr.getBounds()));
//                }
//                
//                g2.clip(outside);
                g2.setColor(new Color(0, 0, 0, 60));
                g.drawLine(0, 0, w, 0);
                g2.setColor(new Color(0, 0, 0, 50));
                g.drawLine(0, 1, w, 1);
                g2.setColor(new Color(0, 0, 0, 40));
                g.drawLine(0, 2, w, 2);
                g2.setColor(new Color(0, 0, 0, 30));
                g.drawLine(0, 3, w, 3);
                g2.setColor(new Color(0, 0, 0, 20));
                g.drawLine(0, 4, w, 4);
                g2.setColor(new Color(0, 0, 0, 10));
                g.drawLine(0, 5, w, 5);
            }
        }

    }

}
