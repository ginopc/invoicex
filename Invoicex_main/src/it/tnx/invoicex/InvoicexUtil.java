/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex;

import com.Ostermiller.util.CSVParser;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.AcroFields.Item;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfStamper;

import gestioneFatture.ArticoloHint;
import gestioneFatture.ClienteHint;
import gestioneFatture.GenericFrmTest;
import gestioneFatture.InvoicexEvent;
import gestioneFatture.JDialogImpostazioni;
import gestioneFatture.JDialogWait;
import gestioneFatture.MenuPanel;
import gestioneFatture.SqlLineIterator;
import gestioneFatture.UnZip;
import gestioneFatture.Util;
import gestioneFatture.dbFattura;
import gestioneFatture.frmClie;
import gestioneFatture.frmElenDDT;
import gestioneFatture.frmElenFatt;
import gestioneFatture.frmElenOrdini;
import gestioneFatture.frmNuovRigaDescrizioneMultiRigaNew;
import gestioneFatture.frmTestDocu;
import gestioneFatture.frmTestFatt;
import gestioneFatture.frmTestFattAcquisto;
import gestioneFatture.frmTestOrdine;
import gestioneFatture.iniFileProp;
import gestioneFatture.logic.clienti.Cliente;
import gestioneFatture.logic.documenti.DettaglioIva;
import gestioneFatture.logic.documenti.Documento;
import gestioneFatture.logic.documenti.Documento2;

import gestioneFatture.logic.documenti.IvaDed;
import gestioneFatture.main;
import static gestioneFatture.main.fileIni;
import it.tnx.Db;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugFastUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.FxUtils;
import it.tnx.commons.HttpUtils;
import it.tnx.commons.MicroBench;
import it.tnx.commons.RunnableWithArgs;
import it.tnx.commons.StringUtilsTnx;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.SystemUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.commons.swing.DelayedExecutor;
import it.tnx.gui.DisabledGlassPane;
import it.tnx.invoicex.data.DatiAzienda;
import it.tnx.invoicex.data.Giacenza;
import it.tnx.invoicex.gui.JDialogLotti;
import it.tnx.invoicex.gui.JFrameDb;
import it.tnx.invoicex.gui.JInternalFrameClientiFornitori;
import it.tnx.invoicex.gui.JInternalFrameScadenzario;
import it.tnx.invoicex.gui.utils.SmoothBorder;
import it.tnx.invoicex.sync.Sync;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.Double;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXHyperlink;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jvnet.substance.SubstanceDefaultTableCellRenderer;
import org.mozilla.universalchardet.UniversalDetector;
import sas.swing.plaf.MultiLineLabelUI;
import tnxbeans.SeparatorComboBoxRenderer;
import tnxbeans.tnxComboField;
import tnxbeans.tnxDbGrid;
import tnxbeans.tnxDbPanel;
import tnxbeans.tnxTextField;

/**
 *
 * @author test1
 */
public class InvoicexUtil {

    static public Boolean substance = false;
    static public TableCellRenderer numberRenderer0_5 = null;
    static public TableCellRenderer numberRenderer0_5_0rosso = null;
    static public Integer tipoNumerazione = null;
    static public final int TIPO_NUMERAZIONE_ANNO = 0;
    static public final int TIPO_NUMERAZIONE_ANNO_2CIFRE = 1;
    static public final int TIPO_NUMERAZIONE_ANNO_SOLO_NUMERO = 2;
    static public final int TIPO_NUMERAZIONE_ANNO_INFINITA = 3;

    static {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                System.out.println("verify = " + hostname + " sessione:" + session);
                return true;
            }
        });
    }
    public static String urlwsd1 = "https://secure.tnx.it/invoicex/index.php?p=wsd";
    //public static String urlwsd2 = "https://due.tnx.it/invoicex/wsd.inc.php?p=wsd";
    public static String urlwsd2 = "https://server.invoicex.it/wsd.inc.php?p=wsd";
    public static String urlwsd_locale = "https://demo.tnx.it/invoicex/index.php?p=wsd";

    static public Map<JInternalFrame, LockKey> lock_ts_timer = new HashMap();

    public static void calcolaColli(tnxDbGrid griglia, tnxTextField texNumeroColli) {
        if (!fileIni.getValue("pref", "colli_automatici", "0").equals("0")) {
            int colli = 0;
            for (int i = 0; i < griglia.getRowCount(); i++) {
                String desc = cu.s(griglia.getValueAt(i, griglia.getColumnByName("descrizione")));
                if (desc.indexOf("Numero colli: ") >= 0) {
                    String scolli = StringUtils.substringAfterLast(desc, "\nNumero colli: ");
                    scolli = StringUtils.substringBefore(scolli, "\n");
                    System.out.println("scolli = " + scolli);
                    colli += cu.i0(scolli);
                } else if (fileIni.getValue("pref", "colli_automatici", "0").equals("1")) {
                    Object qta = griglia.getValueAt(i, griglia.getColumnByName("quantita"));
                    colli += cu.i0(Math.floor(cu.d0(qta)));
                } else {
                    colli++;
                }
            }
            if (cu.i0(texNumeroColli.getText()) != colli) {
                texNumeroColli.setText(cu.s(colli));
                FxUtils.fadeBackground(texNumeroColli, Color.green);
            }
        }
    }

    public static void aggiornaStrutturaScadenzeProvvigioniPerId() {
        //per id_doc è sufficiente una query
        try {

            System.out.println("inizio aggiornaStrutturaScadenzeProvvigioniPerId " + new Date());

            Connection conn = Db.getConn();

            String sql = "CREATE TABLE provvigioni_copia_pre_id LIKE provvigioni;";
            dbu.tryExecQuery(conn, sql);

            sql = "LOCK TABLES provvigioni WRITE, provvigioni_copia_pre_id WRITE";
            dbu.tryExecQuery(conn, sql);

            sql = "INSERT provvigioni_copia_pre_id SELECT * FROM provvigioni;";
            dbu.tryExecQuery(conn, sql);

            sql = "UNLOCK TABLES";
            dbu.tryExecQuery(conn, sql);

            //aggiungere indici per tipo serie numero anno su test_fatt, test_ordi, test_ddt, test_fatt_acquisto, test_ddt_acquisto, test_ordi_acquisto
            addindex("ALTER TABLE `test_fatt` ADD INDEX `serie` (`serie`)");
            addindex("ALTER TABLE `test_fatt` ADD INDEX `numero` (`numero`)");
            addindex("ALTER TABLE `test_fatt` ADD INDEX `anno` (`anno`)");
            addindex("ALTER TABLE `test_ddt` ADD INDEX `serie` (`serie`)");
            addindex("ALTER TABLE `test_ddt` ADD INDEX `numero` (`numero`)");
            addindex("ALTER TABLE `test_ddt` ADD INDEX `anno` (`anno`)");
            addindex("ALTER TABLE `test_ordi` ADD INDEX `serie` (`serie`)");
            addindex("ALTER TABLE `test_ordi` ADD INDEX `numero` (`numero`)");
            addindex("ALTER TABLE `test_ordi` ADD INDEX `anno` (`anno`)");
            addindex("ALTER TABLE `test_fatt_acquisto` ADD INDEX `serie` (`serie`)");
            addindex("ALTER TABLE `test_fatt_acquisto` ADD INDEX `numero` (`numero`)");
            addindex("ALTER TABLE `test_fatt_acquisto` ADD INDEX `anno` (`anno`)");
            addindex("ALTER TABLE `test_ddt_acquisto` ADD INDEX `serie` (`serie`)");
            addindex("ALTER TABLE `test_ddt_acquisto` ADD INDEX `numero` (`numero`)");
            addindex("ALTER TABLE `test_ddt_acquisto` ADD INDEX `anno` (`anno`)");
            addindex("ALTER TABLE `test_ordi_acquisto` ADD INDEX `serie` (`serie`)");
            addindex("ALTER TABLE `test_ordi_acquisto` ADD INDEX `numero` (`numero`)");
            addindex("ALTER TABLE `test_ordi_acquisto` ADD INDEX `anno` (`anno`)");

            //aggiungere indici per tipo serie numero anno data_scadenza
            sql = "ALTER TABLE `provvigioni` ADD INDEX `documento_serie` (`documento_serie`)";
            try {
                dbu.tryExecQuery(conn, sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sql = "ALTER TABLE `provvigioni` ADD INDEX `documento_numero` (`documento_numero`)";
            try {
                dbu.tryExecQuery(conn, sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sql = "ALTER TABLE `provvigioni` ADD INDEX `documento_anno` (`documento_anno`)";
            try {
                dbu.tryExecQuery(conn, sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sql = "ALTER TABLE `provvigioni` ADD INDEX `data_scadenza` (`data_scadenza`)";
            try {
                dbu.tryExecQuery(conn, sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sql = "ALTER TABLE `scadenze` ADD INDEX `documento_serie` (`documento_serie`)";
            try {
                dbu.tryExecQuery(conn, sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sql = "ALTER TABLE `scadenze` ADD INDEX `documento_numero` (`documento_numero`)";
            try {
                dbu.tryExecQuery(conn, sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sql = "ALTER TABLE `scadenze` ADD INDEX `documento_anno` (`documento_anno`)";
            try {
                dbu.tryExecQuery(conn, sql);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //aggancio id_doc
            sql = "update provvigioni p  join  test_fatt tf \n"
                    + "    on p.documento_tipo = 'FA' \n"
                    + "     and tf.serie = p.documento_serie\n"
                    + "     and tf.numero = p.documento_numero\n"
                    + "     and tf.anno = p.documento_anno\n"
                    + "    set p.id_doc = tf.id";
            dbu.tryExecQuery(conn, sql);

            sql = "ALTER TABLE `provvigioni` ADD INDEX `id_doc` (`id_doc`)";
            try {
                dbu.tryExecQuery(conn, sql);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //eliminare provvigioni con id_doc = null
            sql = "delete from provvigioni where id_doc is null";
            dbu.tryExecQuery(conn, sql);

            //per id_scadenza se per data ne trova più di una si deve andare rec per rec 
            //prima update generale e poi rec per rec per i count > 1
            sql = "update provvigioni p  join  scadenze s \n"
                    + "    on s.documento_tipo = 'FA' \n"
                    + "     and s.documento_serie = p.documento_serie\n"
                    + "     and s.documento_numero = p.documento_numero\n"
                    + "     and s.documento_anno = p.documento_anno\n"
                    + "     and s.data_scadenza = p.data_scadenza\n"
                    + "    set p.id_scadenza = s.id";
            dbu.tryExecQuery(conn, sql);
            //rec per rec
            sql = "select s.id as s_id, p.id as p_id, count(p.id) as conta_p_id from scadenze s\n"
                    + " left join provvigioni p on s.documento_tipo = 'FA' \n"
                    + "  and s.documento_serie = p.documento_serie \n"
                    + "  and s.documento_numero = p.documento_numero\n"
                    + "  and s.documento_anno = p.documento_anno\n"
                    + "  and s.data_scadenza = p.data_scadenza\n"
                    + " group by s.id\n"
                    + " having count(p.id) > 1"
                    + " order by p.id";
            List<Map> list = dbu.getListMap(conn, sql);

            try {
                Integer old_p_id = null;
                ArrayList list_s = new ArrayList();
                ArrayList list_p = new ArrayList();
                if (list != null && list.size() > 0) {
                    old_p_id = cu.i(list.get(0).get("p_id"));
                    int ilist = 0;
                    for (Map m : list) {
                        ilist++;
                        if (!old_p_id.equals(cu.i(m.get("p_id"))) || ilist == list.size()) {
                            if (ilist == list.size()) {
                                list_s.add(m.get("s_id"));
                            }
                            //rottura di codice di p.id  
                            sql = "select s.id as s_id, p.id as p_id from scadenze s\n"
                                    + "left join provvigioni p on s.documento_tipo = 'FA' \n"
                                    + " and s.documento_serie = p.documento_serie \n"
                                    + " and s.documento_numero = p.documento_numero\n"
                                    + " and s.documento_anno = p.documento_anno\n"
                                    + " and s.data_scadenza = p.data_scadenza\n"
                                    + " where s.id = " + list_s.get(0);
                            List<Map> list2 = dbu.getListMap(conn, sql);
                            for (Map m2 : list2) {
                                list_p.add(m2.get("p_id"));
                            }
                            //fare query di update di p set id_scadenza = id nell'array di s
                            if (list_s.size() == list_p.size()) {
                                for (int i = 0; i < list_s.size(); i++) {
                                    sql = "update provvigioni set id_scadenza = " + list_s.get(i) + " where id = " + list_p.get(i);
                                    System.out.println("sql = " + sql);
                                    dbu.tryExecQuery(conn, sql);
                                }
                            } else {
                                System.out.println("!!! agganciaProvvigioni size diverse per " + list_s.get(0));
                            }

                            //azzero liste
                            list_s.clear();
                            list_p.clear();
                        }
                        list_s.add(m.get("s_id"));

                        old_p_id = cu.i(m.get("p_id"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            sql = "ALTER TABLE `provvigioni`\n"
                    + "	CHANGE COLUMN `documento_tipo` `documento_tipo` CHAR(2) NULL DEFAULT NULL AFTER `id`,\n"
                    + "	CHANGE COLUMN `documento_serie` `documento_serie` CHAR(1) NULL DEFAULT NULL AFTER `documento_tipo`,\n"
                    + "	CHANGE COLUMN `documento_numero` `documento_numero` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `documento_serie`,\n"
                    + "	CHANGE COLUMN `documento_anno` `documento_anno` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `documento_numero`;";
            dbu.tryExecQuery(conn, sql);

            sql = "update provvigioni set documento_tipo = null, documento_serie = null, documento_numero = null, documento_anno = null";
            dbu.tryExecQuery(conn, sql);

//--------------------------------------------------
            //cambio Scadenze
            sql = "CREATE TABLE scadenze_copia_pre_id LIKE scadenze;";
            dbu.tryExecQuery(conn, sql);

            sql = "LOCK TABLES scadenze WRITE, scadenze_copia_pre_id WRITE";
            dbu.tryExecQuery(conn, sql);

            sql = "INSERT scadenze_copia_pre_id SELECT * FROM scadenze;";
            dbu.tryExecQuery(conn, sql);

            sql = "UNLOCK TABLES";
            dbu.tryExecQuery(conn, sql);

            sql = "ALTER TABLE `scadenze`\n"
                    + "	ADD COLUMN `id_doc` INT NULL AFTER `documento_tipo`,\n"
                    + "	CHANGE COLUMN `documento_serie` `documento_serie` CHAR(1) NULL DEFAULT NULL AFTER `id_doc`,\n"
                    + "	CHANGE COLUMN `documento_numero` `documento_numero` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `documento_serie`,\n"
                    + "	CHANGE COLUMN `documento_anno` `documento_anno` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `documento_numero`";
            dbu.tryExecQuery(conn, sql);

            sql = "ALTER TABLE `scadenze` ADD INDEX `id_doc` (`id_doc`)";
            try {
                dbu.tryExecQuery(conn, sql);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //aggancio id documento
            //per test_fatt
            sql = "update scadenze s  join  test_fatt tf \n"
                    + "    on s.documento_tipo = 'FA' \n"
                    + "     and tf.serie = s.documento_serie\n"
                    + "     and tf.numero = s.documento_numero\n"
                    + "     and tf.anno = s.documento_anno\n"
                    + "    set s.id_doc = tf.id";
            dbu.tryExecQuery(conn, sql);
            //per test_fatt_acquisto
            sql = "update scadenze s  join  test_fatt_acquisto tf \n"
                    + "    on s.documento_tipo = 'FR' \n"
                    + "     and tf.serie = s.documento_serie\n"
                    + "     and tf.numero = s.documento_numero\n"
                    + "     and tf.anno = s.documento_anno\n"
                    + "    set s.id_doc = tf.id";
            dbu.tryExecQuery(conn, sql);
            //per test_ordi
            sql = "update scadenze s  join  test_ordi t \n"
                    + "    on s.documento_tipo = 'OR' \n"
                    + "     and t.serie = s.documento_serie\n"
                    + "     and t.numero = s.documento_numero\n"
                    + "     and t.anno = s.documento_anno\n"
                    + "    set s.id_doc = t.id";
            dbu.tryExecQuery(conn, sql);

            sql = "delete from scadenze where id_doc is null";
            dbu.tryExecQuery(conn, sql);

            sql = "update scadenze set documento_serie = null, documento_numero = null, documento_anno = null";
            dbu.tryExecQuery(conn, sql);

            System.out.println("fine ok aggiornaStrutturaScadenzeProvvigioniPerId " + new Date());

        } catch (Exception e) {
            System.out.println("fine KO!!! aggiornaStrutturaScadenzeProvvigioniPerId " + new Date());
            SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
        }

    }

    private static void addindex(String sql) {
        try {
            dbu.tryExecQuery(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void aggiornaCostoMedioPonderato(Connection conn) throws Exception {
        aggiornaPrezziNettiUnitari(conn, "righ_fatt_acquisto", "test_fatt_acquisto");
        aggiornaPrezziNettiUnitari(conn, "righ_ddt_acquisto", "test_ddt_acquisto");

        String sql = "update movimenti_magazzino m join righ_fatt_acquisto r on r.id = m.da_id_riga and m.da_tabella = 'test_fatt_acquisto'";
        sql += "set m.prezzo_unitario = r.prezzo, m.prezzo_unitario_netto = r.prezzo_netto_unitario";
        dbu.tryExecQuery(conn, sql, false);

        sql = "update movimenti_magazzino m join righ_ddt_acquisto r on r.id = m.da_id_riga and m.da_tabella = 'test_ddt_acquisto'";
        sql += "set m.prezzo_unitario = r.prezzo, m.prezzo_unitario_netto = r.prezzo_netto_unitario";
        dbu.tryExecQuery(conn, sql, false);

        if (fileIni.getValue("db", "nome_database", "").indexOf("toysforyou") >= 0) {
            //per toys for you aggancio prezzo a listino di acquisto per movimenti storicizzati
            sql = "update movimenti_magazzino m \n"
                    + "        join articoli_prezzi ap on m.articolo = ap.articolo and ap.listino = 'ACQUISTO'\n"
                    + "        set m.prezzo_unitario = ap.prezzo, m.prezzo_unitario_netto = ap.prezzo\n"
                    + "        where id_storicizzazione is not null";
            dbu.tryExecQuery(conn, sql, false);
        }
    }

    public static void aggiungiFiltroSerie(JComboBox filtroTipo, String nometab) {
        filtroTipo.setName("init");
        try {

            for (int i = filtroTipo.getItemCount(); i >= 0; i--) {
                if (cu.s(filtroTipo.getItemAt(i)).startsWith("Solo serie ")) {
                    filtroTipo.removeItemAt(i);
                }
            }

            List list_serie = dbu.getList(Db.getConn(), "select serie from " + nometab + " group by serie");
            if (list_serie.size() > 1) {
                for (Object serie : list_serie) {
                    filtroTipo.addItem("Solo serie " + serie);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        filtroTipo.setName("");
    }

    public static Component getActiveJInternalFrame() {
        return main.getPadrePanel().getDesktopPane().getSelectedFrame();
    }

    public static String getNomeFileDoc(String tipo_doc, String serie, String numero, String nomeCliente, boolean eng) {
        return getNomeFileDoc(tipo_doc, serie, numero, nomeCliente, eng, null);
    }

    public static String getNomeFileDoc(String tipo_doc, String serie, String numero, String nomeCliente, boolean eng, String tipo_fattura) {
        String nomeFile = Db.getDescTipoDocNomeFile(tipo_doc, eng, tipo_fattura) + "_" + serie + numero + "_" + nomeCliente;
//        17/09/2015 rimetto normalizeFileName perchè con Dir ed il cliente con / o \ dava errore!
//        nomeFile = it.tnx.commons.FileUtils.normalizeFileNameDir(nomeFile);
        nomeFile = it.tnx.commons.FileUtils.normalizeFileName(nomeFile);
        return nomeFile;
    }

    static private int contatemptables = 0;

    public static String getTempTableName(String nome) {
        contatemptables++;
        return nome + "_temp_" + contatemptables;
    }

    public static void aggiornaFiltri(JPanel contenitore, List<Map> filters, JButton aggiungi, final RunnableWithArgs azioneModifica, final RunnableWithArgs azioneRimuovi) {
        if (filters == null) {
            return;
        }

        contenitore.remove(aggiungi);

        for (Component comp : contenitore.getComponents()) {
            if (comp != aggiungi) {
                contenitore.remove(comp);
            }
        }

        for (final Map m : filters) {
            System.out.println("aggiungo comp filtro " + DebugFastUtils.dumpAsString(m));

            JPanel p = new JPanel();
            p.setBorder(new SmoothBorder(new Color(67, 135, 239), 1));

            JXHyperlink link = new JXHyperlink();
            String campo = cu.s(m.get("campo"));
            String nome_carino = cu.s(m.get("nome_carino"));
            String valore = cu.s(m.get("valore"));
            if (m.get("descrizione") != null) {
                valore = cu.s(m.get("descrizione"));
            }
            String text = nome_carino + " = " + valore;
            if (campo.equals("note") || campo.equals("riferimento") || campo.equals("dest_ragione_sociale")) {
                text = nome_carino + " contiene " + valore;
            }
            link.setText(text);
            link.setUnclickedColor(Color.black);
            link.setClickedColor(Color.black);
            link.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    azioneModifica.run(m, e);
                }
            });

            JXHyperlink linkX = new JXHyperlink();
            linkX.setText("X");
            linkX.setToolTipText("rimuovi");
            linkX.setUnclickedColor(Color.red);
            linkX.setClickedColor(Color.red);
            linkX.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    azioneRimuovi.run(m, e);
                }
            });

            p.add(link);

            JSeparator sep = new JSeparator(JSeparator.VERTICAL);
            sep.setPreferredSize(new Dimension(2, 14));
            p.add(sep);

            p.add(linkX);

            contenitore.add(p);
        }
        contenitore.add(aggiungi);

        if (contenitore.getTopLevelAncestor() != null) {
            contenitore.getTopLevelAncestor().validate();
            contenitore.getTopLevelAncestor().repaint();
        }
    }

    public static void mettiSotto(Window finestra, JComponent riferimento) {
        Point p = riferimento.getLocationOnScreen();
        finestra.setLocationRelativeTo(riferimento);
        finestra.setLocation((int) finestra.getX(), (int) p.getY() + riferimento.getHeight());
    }

    public static void resizePanelFlow(final JPanel panel) {
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e); //To change body of generated methods, choose Tools | Templates.
                Component last = panel.getComponents()[panel.getComponentCount() - 1];
                //controllo per ingrandire
                if (last.getLocation().y + last.getSize().height > panel.getHeight()) {
                    System.out.println("resizePanelFlow ingrandisco");
                    int newh = last.getLocation().y + last.getSize().height + 15;
                    panel.setMinimumSize(new Dimension(100, newh));
                    panel.setPreferredSize(new Dimension(100, newh));
                } else if (panel.getSize().height > (last.getLocation().y + last.getSize().height) + 15) {
                    System.out.println("resizePanelFlow diminuisco");
                    int newh = last.getLocation().y + last.getSize().height + 15;
                    panel.setMinimumSize(new Dimension(100, newh));
                    panel.setPreferredSize(new Dimension(100, newh));
                }
            }
        });
    }

    public static JDialog getDialogFiltri(JInternalFrame parent, boolean modal, boolean acquisto, List<Map> filters) {
        try {
            Class cl = main.pf.classloader.loadClass("invoicexplugininvoicex.filtri.JDialogFiltriDocumenti");
            Constructor constr = cl.getConstructor(JInternalFrame.class, boolean.class, boolean.class, List.class);
            return (JDialog) constr.newInstance(parent, modal, acquisto, filters);
        } catch (Throwable t) {
            t.printStackTrace();
            SwingUtils.showErrorMessage(parent, "Errore getDialogFiltri: " + t.toString());
        }
        return null;
    }

    public static JDialog getDialogFiltriClieForn(JInternalFrame parent, boolean modal, List<Map> filters) {
        try {
            Class cl = main.pf.classloader.loadClass("invoicexplugininvoicex.filtri.JDialogFiltriClieForn");
            Constructor constr = cl.getConstructor(JInternalFrame.class, boolean.class, List.class);
            return (JDialog) constr.newInstance(parent, modal, filters);
        } catch (Throwable t) {
            t.printStackTrace();
            SwingUtils.showErrorMessage(parent, "Errore getDialogFiltri: " + t.toString());
        }
        return null;
    }

    public static JDialog getDialogFiltriArticoli(JInternalFrame parent, boolean modal, List<Map> filters) {
        try {
            Class cl = main.pf.classloader.loadClass("invoicexplugininvoicex.filtri.JDialogFiltriArticoli");
            Constructor constr = cl.getConstructor(JInternalFrame.class, boolean.class, List.class);
            return (JDialog) constr.newInstance(parent, modal, filters);
        } catch (Throwable t) {
            t.printStackTrace();
            SwingUtils.showErrorMessage(parent, "Errore getDialogFiltri: " + t.toString());
        }
        return null;
    }

    public static List<Map> caricaFiltri(String name) {
        try {
            Class mainFiltriCl = main.pf.classloader.loadClass("invoicexplugininvoicex.filtri.MainFiltri");
            return (List<Map>) mainFiltriCl.getMethod("caricaFiltri", String.class).invoke(null, name);
        } catch (InvocationTargetException ite) {
            ite.getTargetException().printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void salvaFiltri(List<Map> filters, String name) {
        try {
            Class mainFiltriCl = main.pf.classloader.loadClass("invoicexplugininvoicex.filtri.MainFiltri");
            mainFiltriCl.getMethod("salvaFiltri", List.class, String.class).invoke(null, filters, name);
        } catch (InvocationTargetException ite) {
            ite.getTargetException().printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static tnxDbPanel getDbPanel(JComponent comp) {
        if (comp.getParent() instanceof tnxDbPanel) {
            tnxDbPanel dbp = (tnxDbPanel) comp.getParent();
            if (dbp.getParentPanel() != null) {
                return dbp.getParentPanel();
            }
            return dbp;
        }
        return null;
    }

    public static boolean isDbPanelRefreshing(JComponent comp) {
        tnxDbPanel panel = getDbPanel(comp);
        if (panel != null && panel.isRefreshing) {
            return true;
        }
        return false;
    }

    public static void aggiornaConvertito(String check, String tab, String convertito, Integer id) {
        convertito = convertito.replaceAll(" " + check + "\n", "");
        convertito = convertito.replaceAll("\n " + check + "$", "");
        convertito = convertito.replaceAll(" " + check + "$", "");
        String sql = "UPDATE " + tab + " SET ";
        if (convertito.equals("")) {
            sql += "convertito = NULL";
            sql += ", evaso = '', fattura_serie = NULL, fattura_numero = NULL, fattura_anno = NULL";
        } else {
            sql += "convertito = '" + convertito + "'";
            sql += ", evaso = 'P'";
        }
        sql += " WHERE id = " + dbu.sql(id);
        Db.executeSql(sql);
    }

    public static int getWidhtIntFrame(int i, JInternalFrame frm) {
        return i;
    }

    public static void caricaComboTestateCampoLibero1(tnxComboField comCampoLibero1) {
        String oldtext = comCampoLibero1.getText();
        comCampoLibero1.dbClearList();
        comCampoLibero1.dbAddElement("", null);
        String sql = "select * from (\n"
                + "	select campo_libero_1, campo_libero_1 as nome  from test_ordi \n"
                + "	where ifnull(campo_libero_1,'') != ''\n"
                + "union \n"
                + "	select campo_libero_1, campo_libero_1 as nome from test_ordi_acquisto \n"
                + "	where ifnull(campo_libero_1,'') != ''\n"
                + "union \n"
                + "	select campo_libero_1, campo_libero_1 as nome from test_ddt\n"
                + "	where ifnull(campo_libero_1,'') != ''\n"
                + "union \n"
                + "	select campo_libero_1, campo_libero_1 as nome from test_ddt_acquisto \n"
                + "	where ifnull(campo_libero_1,'') != ''\n"
                + "union \n"
                + "	select campo_libero_1, campo_libero_1 as nome from test_fatt\n"
                + "	where ifnull(campo_libero_1,'') != ''\n"
                + "union \n"
                + "	select campo_libero_1, campo_libero_1 as nome from test_fatt_acquisto \n"
                + "	where ifnull(campo_libero_1,'') != ''\n"
                + ") a \n"
                + "group by campo_libero_1\n"
                + "order by campo_libero_1";
        comCampoLibero1.dbOpenList(Db.getConn(), sql);
        comCampoLibero1.setText(oldtext);
    }

    public static void initCampiLiberiTestate(GenericFrmTest gentest) {
        try {
            HashMap list = dbu.getListMapMap(Db.getConn(), "select * from campi_liberi where id like 'testate_%'", "id");
            if (list.containsKey("testate_1")) {
                String nome = cu.s(((Map) list.get("testate_1")).get("nome"));
                gentest.getCampoLibero1Label().setText(nome);
            } else {
                gentest.getCampoLibero1Label().setVisible(false);
                gentest.getCampoLibero1Combo().setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean controlloGiornoPagamento(String codicePagamento, String texGiornoPagamento, JInternalFrame frame) {
        try {
            ResultSet r = gestioneFatture.Db.lookUp(codicePagamento, "codice", "pagamenti");
            if (gestioneFatture.Db.nz(r.getString("flag_richiedi_giorno"), "N").equalsIgnoreCase("S")) {
                //deve essere specificato il giorno e deve essere fra 1 e 31
                int i = Integer.parseInt(texGiornoPagamento);
                if (i < 1 || i > 31) {
                    JOptionPane.showMessageDialog(frame, "E' obbligatorio specificare il giorno del mese per questo tipo di pagamento" + "\nDeve essere compreso fra 1 e 31", "Attenzione", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
            }
        } catch (Exception err) {
            JOptionPane.showMessageDialog(frame, "E' obbligatorio specificare il giorno del mese per questo tipo di pagamento" + "\nDeve essere compreso fra 1 e 31", "Attenzione", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    public static void createTempTable(String dbStato, String table_righe_temp, String righ, Integer id) throws Exception {
        dbu.tryExecQuery(Db.getConn(), "drop table if exists " + table_righe_temp);
        String sqlcreatetmp = "CREATE TABLE " + table_righe_temp + " ";
        if (dbStato.equals(tnxDbPanel.DB_MODIFICA)) {
            if (table_righe_temp.indexOf("_lotti_") >= 0) {
                String tab_righe = StringUtils.substringBefore(righ, "_lotti");
                sqlcreatetmp += " select * from " + righ + " where id_padre in (select id from " + tab_righe + " where id_padre = " + id + ")";
            } else {
                sqlcreatetmp += " select * from " + righ + " where id_padre = " + id;
            }
        } else {
//            sqlcreatetmp += " select * from " + righ + " where id_padre = -1";
            sqlcreatetmp += " like " + righ;
        }
        System.out.println("sqlcreatetmp = " + sqlcreatetmp);
        dbu.tryExecQuery(Db.getConn(), sqlcreatetmp);
        if (dbStato.equals(tnxDbPanel.DB_MODIFICA)) {
            dbu.tryExecQuery(Db.getConn(), "ALTER TABLE " + table_righe_temp + " CHANGE COLUMN id id INT(11) NOT NULL AUTO_INCREMENT, ADD PRIMARY KEY (`id`);", false, true);
        }
    }

    public static String getTipoIvaPaese(String paese) {
        if (paese != null && StringUtils.isNotBlank(paese)) {
            if (paese.equalsIgnoreCase("IT") || paese.trim().length() == 0) {
                return Cliente.TIPO_IVA_ITALIA;
            } else if (Cliente.TIPO_IVA_PAESI_CEE.indexOf(paese) >= 0) {
                return Cliente.TIPO_IVA_CEE;
            } else {
                return Cliente.TIPO_IVA_ALTRO;
            }
        } else {
            return Cliente.TIPO_IVA_ITALIA;
        }
    }

    public static String getIva(String codice_articolo, String paese, String iva_standard_cliente) {
        String codice_iva = null;

        //carico da impostazioni
        String tipo_iva = getTipoIvaPaese(paese);
        if (tipo_iva.equals(Cliente.TIPO_IVA_ALTRO) && main.fileIni.getValueBoolean("pref", "controlliIva", true)) {
            codice_iva = "8";
        } else if (tipo_iva.equals(Cliente.TIPO_IVA_CEE) && main.fileIni.getValueBoolean("pref", "controlliIva", true)) {
            codice_iva = "41";
        } else {
            codice_iva = InvoicexUtil.getIvaDefaultPassaggio();
        }

        //carico da articolo se trovato
        if (codice_articolo != null) {
            String sql = "select iva from articoli";
            sql += " where codice = " + dbu.sql(codice_articolo);
            try {
                String iva_art = cu.s(DbUtils.getObject(Db.getConn(), sql));
                if (StringUtils.isNotBlank(iva_art) && !iva_art.equals("-1")) {
                    codice_iva = iva_art;
                }
            } catch (Exception e) {
            }
        }

        // IVA standard in base al cliente se impostata
        if (iva_standard_cliente != null && StringUtils.isNotBlank(iva_standard_cliente)) {
            codice_iva = iva_standard_cliente;
        }

        return codice_iva;

    }

    public static double getTotaleRigaSenzaIva(double qta, double importo_senza_iva, double sconto1, double sconto2) {
        double tot_senza_iva = importo_senza_iva - (importo_senza_iva / 100 * sconto1);
        tot_senza_iva = tot_senza_iva - (tot_senza_iva / 100 * sconto2);

        double tot_senza_iva1 = FormatUtils.round(tot_senza_iva * qta, 2);
        String tot_senza_iva2 = String.format("%.3f", tot_senza_iva * qta, 2);
        String tot_senza_iva3 = String.valueOf(tot_senza_iva);
        tot_senza_iva = it.tnx.Util.round(tot_senza_iva * qta, 2);
        return tot_senza_iva;
    }

    public static double getTotaleRigaConIva(double qta, double importo_con_iva, double sconto1, double sconto2) {
        double tot_con_iva = importo_con_iva - (importo_con_iva / 100 * sconto1);
        tot_con_iva = tot_con_iva - (tot_con_iva / 100 * sconto2);
        tot_con_iva = FormatUtils.round(tot_con_iva * qta, 2);
        return tot_con_iva;
    }

    public static int getRigaInc() {
        String num = main.fileIni.getValue("pref", "numerazione_righe", "1,2,3...");
        if (num.startsWith("1,")) return 1;
        if (num.startsWith("10,")) return 10;
        if (num.startsWith("100,")) return 100;
        return 1;
    }

    public static Boolean udev = null;
    synchronized public static boolean isUdev() {
        if (udev == null) {
            try {
                String vl = main.version + " (" + main.build + ")";
                DatiAzienda azienda = main.attivazione.getDatiAzienda();
                String udevurl = main.baseurlserver + "/udev.php?v=" + URLEncoder.encode(vl, "ISO-8859-1")
                        + (azienda.getRagione_sociale() != null ? "&rs=" + URLEncoder.encode(azienda.getRagione_sociale(), "ISO-8859-1") : "")
                        + (azienda.getPartita_iva() != null ? "&pi=" + URLEncoder.encode(azienda.getPartita_iva(), "ISO-8859-1") : "")
                        + (azienda.getCodice_fiscale() != null ? "&cf=" + URLEncoder.encode(azienda.getCodice_fiscale(), "ISO-8859-1") : "");
                System.out.println("udevurl = " + udevurl);
                String ret = HttpUtils.getUrlToStringUTF8(udevurl);
                System.out.println("ret udev = " + ret);
                if (ret != null && ret.equals("true")) {
                    udev = true;
                } else {
                    udev = false;
                }
                return udev;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return udev;
        }
    }
    synchronized public static String getDownloadDir() {
        if (isUdev()) {
            return "download/invoicex_dev";
        } else {
            return "download/invoicex";
        }
    }

    static public class LockKey {

        public Integer id;
        public String tab;

        private LockKey(String tab, int id) {
            this.tab = tab;
            this.id = id;
        }
    }

    static public String getUrlWsd() {
        String dbserver = main.fileIni.getValue("db", "server");
        if (main.fileIni.getValue("db", "ssh_hostname", "").length() > 0) {
            dbserver = main.fileIni.getValue("db", "ssh_hostname", "");
        }
        if (dbserver.equals("linux")) {
            return urlwsd_locale;
        }
        if (dbserver.indexOf("due.") >= 0) {
            return urlwsd2;
        }
        return urlwsd1;
    }

    static public void fireEvent(Object source, int eventType, Object... args) {
        try {
            InvoicexEvent event = new InvoicexEvent(source);
            event.type = eventType;
            event.args = args;
            main.events.fireInvoicexEvent(event);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    static public int generaProssimoNumeroDocumento(String tipo, int dbAnno, String dbSerie, boolean vendita) {
        try {
            String table = "test_fatt";
            if (tipo.equals("ordine")) {
                table = "test_ordi";
            }
            if (tipo.equals("ddt")) {
                table = "test_ddt";
            }
            if (vendita) {
                table += "_acquisto";
            }

            ResultSet rs = Db.openResultSet("select max(numero) as new from " + table + " where serie = '" + Db.aa(dbSerie) + "' and anno = '" + dbAnno + "'");
            BigInteger res = BigInteger.ZERO;
            if (rs.next()) {
                res = BigInteger.valueOf(rs.getLong("new"));
            }

            return res.intValue() + 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    static public void importCSV(String tipoDoc, File f, String dbSerie, int dbNumero, int dbAnno, int dbIdPadre) throws SQLException {
        importCSV(tipoDoc, f, dbSerie, dbNumero, dbAnno, dbIdPadre, "FromFile");
    }

    static public void importCSV(String tipoDoc, File f, String dbSerie, int dbNumero, int dbAnno, int dbIdPadre, String nomeListino) throws SQLException {
        CSVParser cp;
        String nomeTabella = "";
        boolean ricarico = false;
        double percRicarico = 0.00;
        String listinoRicarico = "";

        if (tipoDoc.equals(Db.TIPO_DOCUMENTO_DDT)) {
            nomeTabella = "righ_ddt";
        } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
            nomeTabella = "righ_ddt_acquisto";
        } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_FATTURA)) {
            nomeTabella = "righ_fatt";
        } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            nomeTabella = "righ_fatt_acquisto";
        } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_ORDINE)) {
            nomeTabella = "righ_ordi";
        } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
            nomeTabella = "righ_ordi_acquisto";
        }

        if (!nomeListino.equals("FromFile")) {
            ResultSet rs = Db.openResultSet("SELECT ricarico_listino, ricarico_flag, ricarico_perc FROM tipi_listino WHERE codice = '" + nomeListino + "'");
            if (rs.next()) {
                String flag = rs.getString("ricarico_flag");

                if (flag != null && flag.equals("S")) {
                    ricarico = true;
                    percRicarico = rs.getDouble("ricarico_perc");
                    listinoRicarico = rs.getString("ricarico_listino");
                }
            }
        }

        int col = 0;
        String sql = "SELECT * FROM " + nomeTabella + " LIMIT 1";
        ResultSet righe = Db.openResultSet(sql);
        Vector field = new Vector();
        try {
            ResultSetMetaData rsmd = righe.getMetaData();
            col = rsmd.getColumnCount();

            for (int i = 1; i <= col; i++) {
                field.add(rsmd.getColumnName(i));
            }

            //controllo file encoding
            byte[] buf = new byte[4096];
            String fileName = f.getAbsolutePath();
            java.io.FileInputStream fis = new java.io.FileInputStream(fileName);
            UniversalDetector detector = new UniversalDetector(null);
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            String encoding = detector.getDetectedCharset();
            if (encoding != null) {
                System.out.println("Detected encoding = " + encoding);
            } else {
                System.out.println("No encoding detected.");
            }
            detector.reset();
            //---------------------

            try {
                cp = new CSVParser(new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding)));
            } catch (Exception e) {
                cp = new CSVParser(new FileInputStream(f));
            }

            cp.changeDelimiter(';');
            String[] csv = cp.getLine();

            Vector chiavi = new Vector();
            for (int i = 0; i < csv.length; i++) {
                chiavi.add(csv[i]);
            }

            Hashtable dati = new Hashtable();
            Vector<String> nonCaricati = new Vector();
            int contarighecsv = 0;
            while ((csv = cp.getLine()) != null) {
                contarighecsv++;
                for (int i = 0; i < chiavi.size(); i++) {
                    dati.put(chiavi.get(i), csv[i]);
                }

                sql = "SELECT max(riga) as last FROM " + nomeTabella + " WHERE id_padre = '" + dbIdPadre + "'";
                ResultSet contaRighe = Db.openResultSet(sql);
                int riga = 0;

                if (contaRighe.next()) {
                    riga = contaRighe.getInt("last") + getRigaInc();
                } else {
                    riga = getRigaInc();
                }

                Vector values = new Vector();
                for (Object campo : field) {
                    String chiave = String.valueOf(campo);

                    if (chiave.equals("serie")) {
                        values.add(dbSerie);
                    } else if (chiave.equals("numero")) {
                        values.add(dbNumero);
                    } else if (chiave.equals("anno")) {
                        values.add(dbAnno);
                    } else if (chiave.equals("id_padre")) {
                        values.add(dbIdPadre);
                    } else if (chiave.equals("prezzo")) {
                        if (!nomeListino.equals("FromFile")) {
                            String sqlart = "";

                            if (ricarico) {
                                sqlart = "SELECT (prezzo + (prezzo * " + percRicarico + ")/100) as prezzo FROM articoli_prezzi WHERE articolo = '" + Db.nz(dati.get("codice_articolo"), "") + "' AND listino = '" + listinoRicarico + "'";
                            } else {
                                sqlart = "SELECT prezzo FROM articoli_prezzi WHERE articolo = '" + Db.nz(dati.get("codice_articolo"), "") + "' AND listino = '" + nomeListino + "'";
                            }

                            ResultSet rs = Db.openResultSet(sqlart);
                            if (rs.next()) {
                                values.add(String.valueOf(rs.getDouble("prezzo")));
                            } else {
                                values.add(Db.nz(dati.get(chiave), ""));
                                nonCaricati.add(Db.nz(dati.get("codice_articolo"), "") + ": " + Db.nz(dati.get("descrizione"), ""));
                            }
                        } else {
                            values.add(Db.nz(dati.get(chiave), ""));
                        }
                    } else if (chiave.equals("prezzo_ivato")
                            || chiave.equals("totale_ivato")
                            || chiave.equals("totale_imponibile")) {
                        Double valore = cu.d0(dati.get(chiave));
                        values.add(valore);
                    } else if (chiave.equals("riga")) {
                        values.add(riga);
                        riga++;
                    } else if (chiave.equals("iva")) {
                        if (StringUtils.isBlank(cu.s(dati.get(chiave)))) {
                            values.add(getIvaDefaultPassaggio());
                        } else {
                            values.add(Db.nz(dati.get(chiave), ""));
                        }
                    } else {
                        values.add(Db.nz(dati.get(chiave), ""));
                    }
                }

                sql = "INSERT INTO " + nomeTabella + " SET ";

                //controllo nullabile del campo di destinazione
                Map desctab = dbu.getListMapMap(Db.getConn(), "describe " + nomeTabella, "Field");

                for (int i = 0; i < field.size(); i++) {
                    String campo = cu.s(field.get(i));

                    if (!String.valueOf(field.get(i)).equals("id")) {
                        if (campo.equalsIgnoreCase("prezzo")) {
                            System.out.println("prezzo");
                        }

//                        sql += field.get(i) + " = '" + Db.aa(cu.toString(values.get(i))) + "'";
                        Object valore = values.get(i);
                        int tipoCampo = rsmd.getColumnType(i + 1);
                        if (tipoCampo == java.sql.Types.BIGINT || tipoCampo == Types.DECIMAL || tipoCampo == Types.DOUBLE || tipoCampo == Types.FLOAT || tipoCampo == Types.INTEGER || tipoCampo == Types.REAL || tipoCampo == Types.SMALLINT || tipoCampo == Types.TINYINT) {
                            valore = cu.toDoubleAll(valore);
                        }
                        String fieldname = cu.s(field.get(i));
                        String valore_str = Db.pc(valore, rsmd.getColumnType(i + 1));
                        //controllo il nullable
                        if (valore_str != null && valore_str.equals("null") && desctab.get(fieldname) != null && ((Map) desctab.get(fieldname)).get("Null").equals("NO")) {
                            //in questo caso non passo il valore perchè non può essere nullo
                            System.out.println("non passo campo '" + fieldname + "' perchè non può essere null (valore_str:" + valore_str + ")");
                        } else {
                            sql += fieldname + " = " + valore_str + ",";
                        }
                    }
                }
                sql = StringUtils.chop(sql);
                System.out.println("sql: " + sql);
                try {
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                } catch (Exception e) {
                    SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore alla riga " + contarighecsv + ":\n" + e.getMessage() + "\nL'import viene interrotto");
                    break;
                }
            }
            if (!nonCaricati.isEmpty()) {
                String list = "<html>Articoli non caricati (manca prezzo nel listino prescelto):<br>";
                for (String value : nonCaricati) {
                    list += "- " + value + "<br>";
                }
                list += "<br>Questi articoli verranno ricaricati con il prezzo inserito nel csv importato</html>";
                JOptionPane.showMessageDialog(main.getPadre(), list, "ELENCO ARTICOLI NON CARICATI", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
        }
    }

    public static void importXls(String tipoDoc, File f, String serie, int numero, int anno, int idPadre) {
        //import da xls cc proskin
        try {
            final InputStream inp = new FileInputStream(f);
            HSSFWorkbook wb = new HSSFWorkbook(inp);
            final HSSFSheet sheet = wb.getSheetAt(1);
            int contaok = 0;
            int contako = 0;
            int contaconcodice = 0;

            String nomeTabella = "";
            if (tipoDoc.equals(Db.TIPO_DOCUMENTO_DDT)) {
                nomeTabella = "righ_ddt";
            } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
                nomeTabella = "righ_ddt_acquisto";
            } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_FATTURA)) {
                nomeTabella = "righ_fatt";
            } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
                nomeTabella = "righ_fatt_acquisto";
            } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_ORDINE)) {
                nomeTabella = "righ_ordi";
            } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
                nomeTabella = "righ_ordi_acquisto";
            }

            int col = 0;
            String sql = "SELECT * FROM " + nomeTabella + " LIMIT 1";
            ResultSet righe = Db.openResultSet(sql);
            Vector field = new Vector();
            ResultSetMetaData rsmd = righe.getMetaData();
            col = rsmd.getColumnCount();
            for (int i = 1; i <= col; i++) {
                field.add(rsmd.getColumnName(i));
            }
            Vector<String> nonCaricati = new Vector();

            ResultSet resu = null;
            String[] colonne = new String[]{"codice", "pz", "13me", "descrizione", "linea", "ml", "totale", "prezzo_unitario", "totale_omaggio", "totale_vendita"};

            sql = "SELECT max(riga) as last FROM " + nomeTabella + " WHERE id_padre = '" + idPadre + "'";
            ResultSet contaRighe = Db.openResultSet(sql);

            int riga = 0;
            if (contaRighe.next()) {
                riga = contaRighe.getInt("last") + 1;
            } else {
                riga = 1;
            }

            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                HSSFRow row = sheet.getRow(i);
                try {
                    HashMap recxls = getMap(row, colonne, false);
                    sql = "";
                    System.out.println("riga " + (i + 1));
                    DebugFastUtils.dump(recxls);
                    String codice = CastUtils.toString(recxls.get("codice"));
                    if (codice.equalsIgnoreCase("CODICE")) {
                        continue;
                    }
                    Object pz = recxls.get("pz");
                    //Object pztot = recxls.get("totale");
                    Double dpz = CastUtils.toDouble0(pz);
                    //Double dpztot = CastUtils.toDouble0(pztot);
                    //Double omaggi = dpztot - dpz;
                    Double omaggi = CastUtils.toDouble0(recxls.get("13me"));
                    if (dpz != 0 || omaggi != 0) {
                        Vector values = new Vector();
                        for (Object campo : field) {
                            String chiave = String.valueOf(campo);

                            if (chiave.equals("serie")) {
                                values.add(serie);
                            } else if (chiave.equals("numero")) {
                                values.add(numero);
                            } else if (chiave.equals("anno")) {
                                values.add(anno);
                            } else if (chiave.equals("id_padre")) {
                                values.add(idPadre);
                            } else if (chiave.equals("codice_articolo")) {
                                values.add(Db.nz(recxls.get("codice"), ""));
                            } else if (chiave.equals("descrizione")) {
                                values.add(Db.nz(recxls.get("descrizione"), ""));
                            } else if (chiave.equals("quantita")) {
                                values.add(Db.nz(recxls.get("pz"), ""));
                            } else if (chiave.equals("prezzo")) {
                                values.add(Db.nz(recxls.get("prezzo_unitario"), ""));
                            } else if (chiave.equals("iva")) {
                                values.add("22");
                            } else if (chiave.equals("riga")) {
                                values.add(riga);
//                                riga++;
                            } else {
                                values.add("");
                            }
                        }

                        sql = "INSERT INTO " + nomeTabella + " SET ";
                        for (int i2 = 0; i2 < field.size(); i2++) {
                            if (!String.valueOf(field.get(i2)).equals("id")) {
                                if (i2 == field.size() - 1) {
                                    sql += field.get(i2) + " = '" + Db.aa(CastUtils.toString(values.get(i2))) + "'";
                                } else {
                                    sql += field.get(i2) + " = '" + Db.aa(CastUtils.toString(values.get(i2))) + "', ";
                                }
                            }
                        }
                        System.out.println("sql: " + sql);
                        Db.executeSql(sql);
                        riga++;

                        //controllo omaggi
                        if (omaggi > 0) {
                            values = new Vector();
                            for (Object campo : field) {
                                String chiave = String.valueOf(campo);
                                if (chiave.equals("serie")) {
                                    values.add(serie);
                                } else if (chiave.equals("numero")) {
                                    values.add(numero);
                                } else if (chiave.equals("anno")) {
                                    values.add(anno);
                                } else if (chiave.equals("id_padre")) {
                                    values.add(idPadre);
                                } else if (chiave.equals("codice_articolo")) {
                                    values.add(Db.nz(recxls.get("codice"), ""));
                                } else if (chiave.equals("descrizione")) {
                                    values.add("Omaggio: " + Db.nz(recxls.get("descrizione"), ""));
                                } else if (chiave.equals("quantita")) {
                                    values.add(omaggi);
                                } else if (chiave.equals("prezzo")) {
                                    values.add(0d);
                                } else if (chiave.equals("iva")) {
                                    values.add("22");
                                } else if (chiave.equals("riga")) {
                                    values.add(riga);
//                                    riga++;
                                } else {
                                    values.add("");
                                }
                            }

                            sql = "INSERT INTO " + nomeTabella + " SET ";
                            for (int i2 = 0; i2 < field.size(); i2++) {
                                if (!String.valueOf(field.get(i2)).equals("id")) {
                                    if (i2 == field.size() - 1) {
                                        sql += field.get(i2) + " = '" + Db.aa(CastUtils.toString(values.get(i2))) + "'";
                                    } else {
                                        sql += field.get(i2) + " = '" + Db.aa(CastUtils.toString(values.get(i2))) + "', ";
                                    }
                                }
                            }
                            System.out.println("sql: " + sql);
                            Db.executeSql(sql);
                            riga++;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            InvoicexUtil.aggiornaTotaliRighe(tipoDoc, idPadre);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Object importXlsCirri(String tipoDoc, File f, int idPadre) {

        //import da xls cirri
        try {
            final InputStream inp = new FileInputStream(f);
            HSSFWorkbook wb = new HSSFWorkbook(inp);
            final HSSFSheet sheet = wb.getSheetAt(0);
            int contaok = 0;
            int contako = 0;
            int contaconcodice = 0;

            String nomeTabella = "";
            if (tipoDoc.equals(Db.TIPO_DOCUMENTO_DDT)) {
                nomeTabella = "righ_ddt";
            } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
                nomeTabella = "righ_ddt_acquisto";
            } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_FATTURA)) {
                nomeTabella = "righ_fatt";
            } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
                nomeTabella = "righ_fatt_acquisto";
            } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_ORDINE)) {
                nomeTabella = "righ_ordi";
            } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
                nomeTabella = "righ_ordi_acquisto";
            }

            int col = 0;
            String sql = "SELECT * FROM " + nomeTabella + " LIMIT 1";
            ResultSet righe = Db.openResultSet(sql);
            Vector field = new Vector();
            ResultSetMetaData rsmd = righe.getMetaData();
            col = rsmd.getColumnCount();
            for (int i = 1; i <= col; i++) {
                field.add(rsmd.getColumnName(i));
            }
            Vector<String> nonCaricati = new Vector();

//cercare prima riga e ultima riga
            int riga_start = 3;
            //cerco formula del totale (I:49)
            int riga_stop = 47;
            try {
                for (int irow = riga_start; irow <= sheet.getLastRowNum(); irow++) {
                    HSSFRow row = sheet.getRow(irow);
                    for (int icol = 0; icol <= 20; icol++) {
                        HSSFCell cell = row.getCell(icol);
                        try {
                            String stringvalue = cu.toString(cell.getStringCellValue());
                            if (stringvalue.equals("TOT.")) {
                                riga_stop = irow - 1;
                                break;
                            }
                            String formula = cell.getCellFormula();
                            if (formula.startsWith("SUM(")) {
                                riga_stop = irow - 1;
                                break;
                            }
                        } catch (Exception e2) {
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Errore nell'analisi delle righe:\n" + e.getMessage();
            }

//controllo che nelle celle da importare non ci siano formule
//            try {
//                for (int irow = riga_start; irow <= riga_stop; irow++) {
//                    HSSFRow row = sheet.getRow(irow);
//                    for (int icol = 0; icol <= 20; icol++) {
//                        HSSFCell cell = row.getCell(icol);
//                            try {
//                            String formula = cu.toString(cell.getCellFormula());
//                            if (StringUtils.isNotBlank(formula)) {
//                                return "Non posso importare il file perchè contiente delle formule al posto dei valori (colonna " + (icol+1) + " riga " + (irow+1) + ")";
//                            }                            
//                        } catch (Exception e2) {
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                return "Errore nell'analisi delle righe:\n" + e.getMessage();
//            }            
//trovare serie numero anno
            Map sna = InvoicexUtil.getSerieNumeroAnno(tipoDoc, idPadre);
            String serie = cu.toString(sna.get("serie"));
            Integer numero = cu.toInteger(sna.get("numero"));
            Integer anno = cu.toInteger(sna.get("anno"));

            ResultSet resu = null;
            String[] colonne = new String[]{"Data", "Macchina", "Codice", "Descrizione", "um", "Q.ta", "Prezzo", "Sc.", "Totale", "Iva"};

            sql = "SELECT max(riga) as last FROM " + nomeTabella + " WHERE id_padre = '" + idPadre + "'";
            ResultSet contaRighe = Db.openResultSet(sql);
            int riga = 0;
            if (contaRighe.next()) {
                riga = contaRighe.getInt("last") + 10;
            } else {
                riga = 10;
            }

            for (int i = riga_start; i <= riga_stop; i++) {
                HSSFRow row = sheet.getRow(i);
                try {
                    HashMap recxls = getMap(row, colonne, false);
                    sql = "";
                    System.out.println("riga " + (i + 1));
                    DebugFastUtils.dump(recxls);
                    String codice = CastUtils.toString(recxls.get("Codice"));
                    String um = cu.s(recxls.get("um"));
                    String iva = cu.s(recxls.get("Iva"));
                    Object pz = recxls.get("Q.ta");
                    Double dpz = CastUtils.toDouble0(pz);
                    String descrizione = CastUtils.toString(recxls.get("Descrizione"));
                    Double prezzo = cu.toDouble(recxls.get("Prezzo"));
                    Double sconto = CastUtils.toDouble0(recxls.get("Sc."));
                    if (StringUtils.isNotBlank(codice) || StringUtils.isNotBlank(descrizione) || dpz != 0) {
                        Vector values = new Vector();
                        for (Object campo : field) {
                            String chiave = String.valueOf(campo);
                            if (chiave.equals("serie")) {
                                values.add(serie);
                            } else if (chiave.equals("numero")) {
                                values.add(numero);
                            } else if (chiave.equals("anno")) {
                                values.add(anno);
                            } else if (chiave.equals("id_padre")) {
                                values.add(idPadre);
                            } else if (chiave.equals("codice_articolo")) {
                                values.add(codice);
                            } else if (chiave.equals("descrizione")) {
                                values.add(descrizione);
                            } else if (chiave.equals("quantita")) {
                                values.add(pz);
                            } else if (chiave.equals("um")) {
                                values.add(um);
                            } else if (chiave.equals("prezzo")) {
                                values.add(prezzo);
                            } else if (chiave.equals("sconto1")) {
                                values.add(sconto);
                            } else if (chiave.equals("iva")) {
                                //values.add(getIvaDefault());
                                values.add(iva);
                            } else if (chiave.equals("riga")) {
                                values.add(riga);
                            } else {
                                values.add("");
                            }
                        }

                        sql = "INSERT INTO " + nomeTabella + " SET ";
                        for (int i2 = 0; i2 < field.size(); i2++) {
                            if (!String.valueOf(field.get(i2)).equals("id")) {
                                if (i2 == field.size() - 1) {
                                    sql += field.get(i2) + " = '" + Db.aa(CastUtils.toString(values.get(i2))) + "'";
                                } else {
                                    sql += field.get(i2) + " = '" + Db.aa(CastUtils.toString(values.get(i2))) + "', ";
                                }
                            }
                        }
                        System.out.println("sql: " + sql);
                        Db.executeSql(sql);
                        riga = riga + 10;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            InvoicexUtil.aggiornaTotaliRighe(tipoDoc, idPadre);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public static HashMap getMap(HSSFRow row, String[] colonne, boolean nonazzerare) {
        int i = 0;
        HashMap m = new HashMap();
        final NumberFormat nf1 = new DecimalFormat("0");
        for (String colonna : colonne) {
            try {
                String temp = row.getCell((short) i).getStringCellValue().trim();
                if (temp != null) {
                    temp = temp.trim();
                }
                if (nonazzerare) {
                    if (temp != null && temp.length() > 0) {
                        m.put(colonne[i], temp);
                    } else {
                        //non metto
                    }
                } else {
                    m.put(colonne[i], temp);
                }
            } catch (Exception e) {
                try {
                    Double d = row.getCell((short) i).getNumericCellValue();
                    //se non ci sono decimali passo come stringa altrimenti come double
                    if (d.intValue() == d.doubleValue()) {
                        m.put(colonne[i], nf1.format(row.getCell((short) i).getNumericCellValue()));
                    } else {
                        m.put(colonne[i], row.getCell((short) i).getNumericCellValue());
                    }
                } catch (Exception e2) {
                    System.out.println("col:" + colonna + " e2:" + e2.getMessage());
                    if (!nonazzerare) {
                        m.put(colonne[i], "");
                    }
                }
            }
            i++;
        }
        return m;
    }

    static public void exportCSV(String tipoDoc, int[] ids, String nomeFile) {
        String nomeTabella = "";
        if (tipoDoc.equals(Db.TIPO_DOCUMENTO_DDT)) {
            nomeTabella = "righ_ddt";
        } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
            nomeTabella = "righ_ddt_acquisto";
        } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_FATTURA)) {
            nomeTabella = "righ_fatt";
        } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            nomeTabella = "righ_fatt_acquisto";
        } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_ORDINE)) {
            nomeTabella = "righ_ordi";
        } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
            nomeTabella = "righ_ordi_acquisto";
        }
        String intestazioni = "";
        String riga = "";
        FileOutputStream fcsv = null;
        String sql = "SELECT * FROM " + nomeTabella + " LIMIT 1";
        ResultSet righe = Db.openResultSet(sql);
        ArrayList field = new ArrayList();

        try {
            String dir = System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "Export" + File.separator;
            File startDir = new File(dir);
            if (!startDir.exists()) {
                startDir.mkdir();
            }

            nomeFile = dir + nomeFile + ".csv";

            fcsv = new FileOutputStream(nomeFile);
            System.out.println("Debug: cvs creato");

            int col = 0;
            if (righe.next()) {
                ResultSetMetaData rsmd = righe.getMetaData();
                col = rsmd.getColumnCount();

                for (int i = 1; i <= col; i++) {
                    field.add(rsmd.getColumnName(i));
                }
                intestazioni = StringUtils.join(field, ";");
                riga = intestazioni + "\n";
                fcsv.write(riga.getBytes());
                System.out.println("Debug: intestazione creata");
            }
            for (int id : ids) {
                sql = "SELECT * FROM " + nomeTabella + " WHERE id_padre = (" + id + ") order by riga";
                System.out.println("sql: " + sql);
                righe = Db.openResultSet(sql);
                ResultSetMetaData rsmd = righe.getMetaData();
                col = rsmd.getColumnCount();

                righe.beforeFirst();
                while (righe.next()) {
                    Hashtable dati = new Hashtable();

                    for (int i = 0; i < col; i++) {
                        String chiave = String.valueOf(field.get(i));
                        try {
                            dati.put(chiave, Db.nz(righe.getString(chiave), ""));
                        } catch (Exception e) {
                            continue;
                        }

                    }

                    riga = getRiga(dati, intestazioni);
                    fcsv.write(riga.getBytes());
                }
            }
            fcsv.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            it.tnx.commons.SwingUtils.open(new File(System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "Export" + File.separator));
            Util.start2(System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "Export" + File.separator);
        }
    }

    private static String getRiga(Hashtable<String, String> dati, String colonne) {
        String[] cols = StringUtils.split(colonne, ";");
        String riga = "";
        for (int i = 0; i < cols.length; i++) {
            riga += "\"" + StringUtils.replace(StringUtils.defaultString(dati.get(cols[i])), "\"", "") + "\"" + ";";
        }
        riga += "\r\n";
        return riga;
    }

    static public boolean esportaInExcel(ResultSet rs, String nomeFile, String title, String note_testa, String note_piede, Map colonne) {
        return esportaInExcel_HSSF(rs, nomeFile, title, note_testa, note_piede, colonne, false);
    }

    static public boolean esportaInExcel(ResultSet rs, String nomeFile, String title, String note_testa, String note_piede, Map colonne, boolean formule_totali) {
        return esportaInExcel_HSSF(rs, nomeFile, title, note_testa, note_piede, colonne, formule_totali);
    }

    static public boolean esportaInExcel_XSSF(ResultSet rs, String nomeFile, String title, String note_testa, String note_piede, Map colonne, boolean formule_totali) {
        //New, generic SS Usermodel Code
        //http://poi.apache.org/spreadsheet/converting.html

        //da completare, il file si deve chiamare xlsx
        Connection connection;
        java.sql.Statement stat;
        ResultSet resu;
        String nomeFileXls = nomeFile;

        try {

            Workbook wb = new XSSFWorkbook();
            DataFormat format = wb.createDataFormat();

            if (title == null || title.length() == 0) {
                title = "export";
            }

            Sheet sheet = wb.createSheet(title);
            sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);

            int contarows = 0;
            Row row = sheet.createRow(contarows);
            contarows++;
            row.createCell((short) 0).setCellValue(title);

            if (note_testa != null && note_testa.length() > 0) {
                row = sheet.createRow(contarows);
                contarows++;
                row = sheet.createRow(contarows);
                contarows++;
                row.createCell((short) 0).setCellValue(note_testa);
            }

            row = sheet.createRow(contarows);
            contarows++;
            //colonne
            row = sheet.createRow(contarows);
            contarows++;
            int columns = 0;
            columns = rs.getMetaData().getColumnCount();

            if (colonne == null) {
                colonne = Collections.synchronizedMap(new LinkedHashMap<String, String>());
                for (int i = 0; i < columns; i++) {
                    colonne.put(rs.getMetaData().getColumnLabel(i + 1), "");
                }
            }

            ArrayList<String> tipi = new ArrayList();
            for (int i = 0; i < columns; i++) {
                System.out.println("tipo: " + rs.getMetaData().getColumnLabel(i + 1) + " : " + rs.getMetaData().getColumnTypeName(i + 1));
                tipi.add(rs.getMetaData().getColumnTypeName(i + 1));
            }

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
                row.createCell((short) i).setCellValue(col);
                i++;
                //sheet.setColumnWidth((short) i, (short) (headerWidth[i] * 300));
            }

            //stili
            CellStyle styledouble = wb.createCellStyle();
            styledouble.setDataFormat(format.getFormat("#,##0.00###"));

            CellStyle styleint = wb.createCellStyle();
            styleint.setDataFormat(format.getFormat("###0"));

            CellStyle styledata = wb.createCellStyle();
            styledata.setDataFormat(format.getFormat("dd/MM/yy"));

            //righe
            int rowcount = 0;
            rs.last();
            rowcount = rs.getRow();
            rs.beforeFirst();

//            //ciclo resultset e lo chiudo
//            List<Object[]> list = new ArrayList();
//            while (rs.next()) {
//                //colonne
//                iter = colonne.keySet().iterator();
//                while (iter.hasNext()) {
//                    String key = (String) iter.next();
//                    String value = (String) colonne.get(key);
//                    m.put(value, rs.getObject(key));
//                }
//            }
            for (int j = 0; j < rowcount; j++) {
                row = sheet.createRow(contarows);
                contarows++;
                //colonne
                iter = colonne.keySet().iterator();
                i = 0;
                Cell cell = null;
                while (iter.hasNext()) {
                    String key = (String) iter.next();
//+
                    String value = (String) colonne.get(key);

                    //controllo tipo di campo
                    Object o = null;
                    rs.absolute(j + 1);
                    o = rs.getObject(key);
                    cell = null;
                    if (o instanceof Double) {
                        cell = row.createCell((short) i);
                        cell.setCellValue((Double) o);
                        cell.setCellStyle(styledouble);
                    } else if (o instanceof BigDecimal) {
                        cell = row.createCell((short) i);
                        cell.setCellValue(((BigDecimal) o).doubleValue());
                        cell.setCellStyle(styledouble);
                    } else if (o instanceof Integer) {
                        cell = row.createCell((short) i);
                        cell.setCellValue(((Integer) o).intValue());
                        cell.setCellStyle(styleint);
                    } else if (o instanceof java.sql.Date) {
                        cell = row.createCell((short) i);
                        cell.setCellValue(((java.sql.Date) o));
                        cell.setCellStyle(styledata);
                    } else if (o instanceof byte[]) {
                        cell = row.createCell((short) i);
                        cell.setCellValue(new String((byte[]) o));
                        cell.setCellStyle(styleint);
                        row.createCell((short) i).setCellValue(new String((byte[]) o));
                    } else if (o instanceof Long) {
                        cell = row.createCell((short) i);
                        cell.setCellValue(((Long) o).longValue());
                        cell.setCellStyle(styleint);
                    } else {
                        if (!(o instanceof String)) {
                            if (o != null) {
                                System.out.println(o.getClass());
                            }
                        }
                        row.createCell((short) i).setCellValue(CastUtils.toString(o));
                    }
                    i++;
                }
            }

            if (formule_totali) {
                row = sheet.createRow(contarows);

//                row.createCell((short) 5).setCellFormula("SUM(F1:F" + contarows + ")");
//                row.createCell((short) 6).setCellFormula("SUM(G1:G" + contarows + ")");
//                row.createCell((short) 7).setCellFormula("SUM(H1:H" + contarows + ")");
                //per gli altri campi
                for (int i2 = 0; i2 < columns; i2++) {
                    String tipo = tipi.get(i2);
                    if (tipo.equalsIgnoreCase("DECIMAL") || tipo.equalsIgnoreCase("DOUBLE")) {
                        row.createCell((short) i2).setCellFormula("SUM(" + (char) (64 + i2 + 1) + "1:" + (char) (64 + i2 + 1) + contarows + ")");
                    }
                }

                contarows++;
            }

            for (int i2 = 0; i2 < columns; i2++) {
                sheet.autoSizeColumn(i2);
            }

            if (note_piede != null && note_piede.length() > 0) {
                row = sheet.createRow(contarows);
                contarows++;
                row = sheet.createRow(contarows);
                contarows++;
                row.createCell((short) 0).setCellValue(note_piede);
            }

            FileOutputStream fileOut = new FileOutputStream(nomeFileXls);
            wb.write(fileOut);
            fileOut.close();

            return true;
        } catch (Exception err) {
            javax.swing.JOptionPane.showMessageDialog(null, err.toString());
            err.printStackTrace();
            return false;
        }
    }

    static public boolean esportaInExcel_HSSF(ResultSet rs, String nomeFile, String title, String note_testa, String note_piede, Map colonne, boolean formule_totali) {
        Connection connection;
        java.sql.Statement stat;
        ResultSet resu;
        String nomeFileXls = nomeFile;

        try {

            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFDataFormat format = wb.createDataFormat();

            if (title == null || title.length() == 0) {
                title = "export";
            }

            HSSFSheet sheet = wb.createSheet(title);
            sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);

            int contarows = 0;
            HSSFRow row = sheet.createRow(contarows);
            contarows++;
            row.createCell((short) 0).setCellValue(title);

            if (note_testa != null && note_testa.length() > 0) {
                row = sheet.createRow(contarows);
                contarows++;
                row = sheet.createRow(contarows);
                contarows++;
                row.createCell((short) 0).setCellValue(note_testa);
            }

            row = sheet.createRow(contarows);
            contarows++;
            //colonne
            row = sheet.createRow(contarows);
            contarows++;
            int columns = 0;
            columns = rs.getMetaData().getColumnCount();

            if (colonne == null) {
                colonne = Collections.synchronizedMap(new LinkedHashMap<String, String>());
                for (int i = 0; i < columns; i++) {
                    colonne.put(rs.getMetaData().getColumnLabel(i + 1), "");
                }
            }

            ArrayList<String> tipi = new ArrayList();
            for (int i = 0; i < columns; i++) {
                System.out.println("tipo: " + rs.getMetaData().getColumnLabel(i + 1) + " : " + rs.getMetaData().getColumnTypeName(i + 1));
                tipi.add(rs.getMetaData().getColumnTypeName(i + 1));
            }

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
                row.createCell((short) i).setCellValue(col);
                i++;
                //sheet.setColumnWidth((short) i, (short) (headerWidth[i] * 300));
            }

            //stili
            HSSFCellStyle styledouble = wb.createCellStyle();
            styledouble.setDataFormat(format.getFormat("#,##0.00###"));

            HSSFCellStyle styleint = wb.createCellStyle();
            styleint.setDataFormat(format.getFormat("###0"));

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

                if (contarows == 10) {
                    for (int i2 = 0; i2 < columns; i2++) {
                        sheet.autoSizeColumn(i2);
                    }
                }

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
                        HSSFCell cell = row.createCell((short) i);
                        cell.setCellValue((Double) o);
                        cell.setCellStyle(styledouble);
                    } else if (o instanceof BigDecimal) {
                        HSSFCell cell = row.createCell((short) i);
                        cell.setCellValue(((BigDecimal) o).doubleValue());
                        cell.setCellStyle(styledouble);
                    } else if (o instanceof Integer) {
                        HSSFCell cell = row.createCell((short) i);
                        cell.setCellValue(((Integer) o).intValue());
                        cell.setCellStyle(styleint);
                    } else if (o instanceof java.sql.Date) {
                        HSSFCell cell = row.createCell((short) i);
                        cell.setCellValue(((java.sql.Date) o));
                        cell.setCellStyle(styledata);
                    } else if (o instanceof byte[]) {
                        HSSFCell cell = row.createCell((short) i);
                        cell.setCellValue(new String((byte[]) o));
                        cell.setCellStyle(styleint);
                        row.createCell((short) i).setCellValue(new String((byte[]) o));
                    } else if (o instanceof Long) {
                        HSSFCell cell = row.createCell((short) i);
                        cell.setCellValue(((Long) o).longValue());
                        cell.setCellStyle(styleint);
                    } else {
                        if (!(o instanceof String)) {
                            if (o != null) {
                                System.out.println(o.getClass());
                            }
                        }
                        row.createCell((short) i).setCellValue(CastUtils.toString(o));
                    }
                    i++;
                }
            }

            if (formule_totali) {
                row = sheet.createRow(contarows);

//                row.createCell((short) 5).setCellFormula("SUM(F1:F" + contarows + ")");
//                row.createCell((short) 6).setCellFormula("SUM(G1:G" + contarows + ")");
//                row.createCell((short) 7).setCellFormula("SUM(H1:H" + contarows + ")");
                //per gli altri campi
                for (int i2 = 0; i2 < columns; i2++) {
                    String tipo = tipi.get(i2);
                    if (tipo.equalsIgnoreCase("DECIMAL") || tipo.equalsIgnoreCase("DOUBLE")) {
                        row.createCell((short) i2).setCellFormula("SUM(" + (char) (64 + i2 + 1) + "1:" + (char) (64 + i2 + 1) + contarows + ")");
                    }
                }

                contarows++;
            }

            if (note_piede != null && note_piede.length() > 0) {
                row = sheet.createRow(contarows);
                contarows++;
                row = sheet.createRow(contarows);
                contarows++;
                row.createCell((short) 0).setCellValue(note_piede);
            }

            FileOutputStream fileOut = new FileOutputStream(nomeFileXls);
            wb.write(fileOut);
            fileOut.close();

            return true;
        } catch (Exception err) {
            javax.swing.JOptionPane.showMessageDialog(null, err.toString());
            err.printStackTrace();
            return false;
        }
    }

    public static void esportaInExcel_XSSF(List<Object[]> list, String nomeFile, String title, String note_testa, String note_piede, Map colonne) {
        String nomeFileXls = nomeFile;
        try {
            Workbook wb = new XSSFWorkbook();
            DataFormat format = wb.createDataFormat();

            if ((title == null) || (title.length() == 0)) {
                title = "export";
            }

            Sheet sheet = wb.createSheet(title);
            sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);

            int contarows = 0;
            Row row = sheet.createRow(contarows);
            contarows = (contarows + 1);
            row.createCell(0).setCellValue(title);

            if ((note_testa != null) && (note_testa.length() > 0)) {
                row = sheet.createRow(contarows);
                contarows = (contarows + 1);
                row = sheet.createRow(contarows);
                contarows = (contarows + 1);
                row.createCell(0).setCellValue(note_testa);
            }

            row = sheet.createRow(contarows);
            contarows = (contarows + 1);

            row = sheet.createRow(contarows);
            contarows = (contarows + 1);
            int columns = 0;
            columns = ((Object[]) list.get(0)).length;

            Iterator iter = colonne.keySet().iterator();
            short i = 0;
            while (iter.hasNext()) {
                Object key = iter.next();
                Object value = colonne.get(key);
                String col = "";

                col = (String) value;
                if ((col == null) || (col.length() == 0)) {
                    col = (String) key;
                }
                row.createCell(i).setCellValue(col);
                i = (short) (i + 1);
            }

            CellStyle styledouble = wb.createCellStyle();
            styledouble.setDataFormat(format.getFormat("#,##0.00###"));

            CellStyle styleint = wb.createCellStyle();
            styleint.setDataFormat(format.getFormat("#,##0"));

            CellStyle styledata = wb.createCellStyle();
            styledata.setDataFormat(format.getFormat("dd/MM/yy"));

            int rowcount = 0;
            rowcount = list.size();
            for (int j = 0; j < rowcount; j++) {
                row = sheet.createRow(contarows);
                contarows = (contarows + 1);

                iter = colonne.keySet().iterator();
                i = 0;
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String value = (String) colonne.get(key);

                    Object o = null;
                    o = ((Object[]) list.get(j))[i];
                    if ((o instanceof Double)) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(((Double) o).doubleValue());
                        cell.setCellStyle(styledouble);
                    } else if ((o instanceof BigDecimal)) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(((BigDecimal) o).doubleValue());
                        cell.setCellStyle(styledouble);
                    } else if ((o instanceof Integer)) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(((Integer) o).intValue());
                        cell.setCellStyle(styleint);
                    } else if ((o instanceof Date)) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue((Date) o);
                        cell.setCellStyle(styledata);
                    } else if ((o instanceof byte[])) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(new String((byte[]) (byte[]) o));
                        cell.setCellStyle(styleint);
                        row.createCell(i).setCellValue(new String((byte[]) (byte[]) o));
                    } else if ((o instanceof Long)) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(((Long) o).longValue());
                        cell.setCellStyle(styleint);
                    } else {
                        if ((!(o instanceof String))
                                && (o != null)) {
                            System.out.println(o.getClass());
                        }

                        row.createCell(i).setCellValue(CastUtils.toString(o));
                    }
                    i = (short) (i + 1);
                }
            }

            for (int i2 = 0; i2 < columns; i2++) {
                sheet.autoSizeColumn(i2);
            }

            if ((note_piede != null) && (note_piede.length() > 0)) {
                row = sheet.createRow(contarows);
                contarows = (contarows + 1);
                row = sheet.createRow(contarows);
                contarows = (contarows + 1);
                row.createCell(0).setCellValue(note_piede);
            }

            FileOutputStream fileOut = new FileOutputStream(nomeFileXls);
            wb.write(fileOut);
            fileOut.close();

            return;
        } catch (Exception err) {
            JOptionPane.showMessageDialog(null, err.toString());
            err.printStackTrace();
        }
    }

    public static void esportaInExcel_HSSF(List<Object[]> list, String nomeFile, String title, String note_testa, String note_piede, Map colonne) {
        String nomeFileXls = nomeFile;
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFDataFormat format = wb.createDataFormat();

            if ((title == null) || (title.length() == 0)) {
                title = "export";
            }

            HSSFSheet sheet = wb.createSheet(title);
            sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);

            short contarows = 0;
            HSSFRow row = sheet.createRow(contarows);
            contarows = (short) (contarows + 1);
            row.createCell(0).setCellValue(title);

            if ((note_testa != null) && (note_testa.length() > 0)) {
                row = sheet.createRow(contarows);
                contarows = (short) (contarows + 1);
                row = sheet.createRow(contarows);
                contarows = (short) (contarows + 1);
                row.createCell(0).setCellValue(note_testa);
            }

            row = sheet.createRow(contarows);
            contarows = (short) (contarows + 1);

            row = sheet.createRow(contarows);
            contarows = (short) (contarows + 1);
            int columns = 0;
            columns = ((Object[]) list.get(0)).length;

            Iterator iter = colonne.keySet().iterator();
            short i = 0;
            while (iter.hasNext()) {
                Object key = iter.next();
                Object value = colonne.get(key);
                String col = "";

                col = (String) value;
                if ((col == null) || (col.length() == 0)) {
                    col = (String) key;
                }
                row.createCell(i).setCellValue(col);
                i = (short) (i + 1);
            }

            HSSFCellStyle styledouble = wb.createCellStyle();
            styledouble.setDataFormat(format.getFormat("#,##0.00###"));

            HSSFCellStyle styleint = wb.createCellStyle();
            styleint.setDataFormat(format.getFormat("#,##0"));

            HSSFCellStyle styledata = wb.createCellStyle();
            styledata.setDataFormat(format.getFormat("dd/MM/yy"));

            int rowcount = 0;
            rowcount = list.size();
            for (int j = 0; j < rowcount; j++) {
                row = sheet.createRow(contarows);
                contarows = (short) (contarows + 1);

                iter = colonne.keySet().iterator();
                i = 0;
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String value = (String) colonne.get(key);

                    Object o = null;
                    o = ((Object[]) list.get(j))[i];
                    if ((o instanceof Double)) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue(((Double) o).doubleValue());
                        cell.setCellStyle(styledouble);
                    } else if ((o instanceof BigDecimal)) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue(((BigDecimal) o).doubleValue());
                        cell.setCellStyle(styledouble);
                    } else if ((o instanceof Integer)) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue(((Integer) o).intValue());
                        cell.setCellStyle(styleint);
                    } else if ((o instanceof Date)) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue((Date) o);
                        cell.setCellStyle(styledata);
                    } else if ((o instanceof byte[])) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue(new String((byte[]) (byte[]) o));
                        cell.setCellStyle(styleint);
                        row.createCell(i).setCellValue(new String((byte[]) (byte[]) o));
                    } else if ((o instanceof Long)) {
                        HSSFCell cell = row.createCell(i);
                        cell.setCellValue(((Long) o).longValue());
                        cell.setCellStyle(styleint);
                    } else {
                        if ((!(o instanceof String))
                                && (o != null)) {
                            System.out.println(o.getClass());
                        }

                        row.createCell(i).setCellValue(CastUtils.toString(o));
                    }
                    i = (short) (i + 1);
                }
            }

            if ((note_piede != null) && (note_piede.length() > 0)) {
                row = sheet.createRow(contarows);
                contarows = (short) (contarows + 1);
                row = sheet.createRow(contarows);
                contarows = (short) (contarows + 1);
                row.createCell(0).setCellValue(note_piede);
            }

            FileOutputStream fileOut = new FileOutputStream(nomeFileXls);
            wb.write(fileOut);
            fileOut.close();

            return;
        } catch (Exception err) {
            JOptionPane.showMessageDialog(null, err.toString());
            err.printStackTrace();
        }
    }

    //stampe..
    public static String controllaPosizioneLogoSuffisso() {
        if (main.fileIni.existKey("varie", "logo_x") && main.fileIni.getValue("varie", "logo_disabilita", "N").equalsIgnoreCase("N")) {
            int x = CastUtils.toInteger0(main.fileIni.getValue("varie", "logo_x", "0"));
            int y = CastUtils.toInteger0(main.fileIni.getValue("varie", "logo_y", "0"));
            int w = CastUtils.toInteger0(main.fileIni.getValue("varie", "logo_w", "100"));
            int h = CastUtils.toInteger0(main.fileIni.getValue("varie", "logo_h", "100"));
            return "_x" + x + "_y" + y + "_w" + w + "_h" + h;
        }
        return "";
    }

    public static JasperDesign controllaLogo(File freport, JasperReport rep, JasperDesign repdes) throws JRException {
        //controllo il logo
        JRDesignBand header = (JRDesignBand) repdes.getPageHeader();
        if (main.fileIni.existKey("varie", "logo_x") && main.fileIni.getValue("varie", "logo_disabilita", "N").equalsIgnoreCase("N")) {
            for (JRElement el : header.getElements()) {
                if (el instanceof JRDesignImage) {
                    JRDesignImage image = (JRDesignImage) el;

                    System.out.println("image x " + image.getX() + " y " + image.getY() + " w " + image.getWidth() + " h " + image.getHeight());
                    int x = CastUtils.toInteger0(main.fileIni.getValue("varie", "logo_x", "0"));
                    int y = CastUtils.toInteger0(main.fileIni.getValue("varie", "logo_y", "0"));
                    int w = CastUtils.toInteger0(main.fileIni.getValue("varie", "logo_w", "100"));
                    int h = CastUtils.toInteger0(main.fileIni.getValue("varie", "logo_h", "100"));

                    x = check(x, 0);
                    y = check(y, 0);
                    w = checks(w, 100);
                    h = checks(h, 100);

                    System.out.println("logo x " + x + " y " + y + " sw " + w + " sh " + h);

                    image.setVerticalAlignment(JRDesignImage.VERTICAL_ALIGN_TOP);
                    int w2 = image.getWidth();
                    int h2 = image.getHeight();
                    x = x * w2 / 100;
                    y = y * h2 / 100;
                    w = w * w2 / 100;
                    h = h * h2 / 100;
                    image.setX(image.getX() + x);
                    image.setY(image.getY() + y);
                    image.setWidth(w);
                    image.setHeight(h);
                }
            }
        }

        return repdes;
    }

    static private int check(int x, int def) {
        if (x < 0 || x > 100) {
            return def;
        }
        return x;
    }

    static private int checks(int x, int def) {
        if (x <= 0 || x > 100) {
            return def;
        }
        return x;
    }

    public static void storicizza(String nota, String doc, int id, boolean acquisto) {
        //Creazione dati
        MicroBench mb = new MicroBench();
        mb.start();

        String tabt = "test_fatt";
        String tabr = "righ_fatt";
        String campoclifor = "cliente";
        if (acquisto) {
            campoclifor = "fornitore";
        }
        if (doc.equals("fattura") && acquisto) {
            tabt = "test_fatt_acquisto";
            tabr = "righ_fatt_acquisto";
        } else if (doc.equals("ddt")) {
            tabt = "test_ddt" + (acquisto ? "_acquisto" : "");
            tabr = "righ_ddt" + (acquisto ? "_acquisto" : "");
        } else if (doc.equals("ordine")) {
            tabt = "test_ordi" + (acquisto ? "_acquisto" : "");
            tabr = "righ_ordi" + (acquisto ? "_acquisto" : "");
        }

        HashMap dati = new HashMap();
        if (doc.equals("fattura")) {
            try {
                ArrayList testa = DbUtils.getListMap(Db.getConn(), "select * from " + tabt + " where id = " + id);
                dati.put("testa", testa);
                ArrayList righe = DbUtils.getListMap(Db.getConn(), "select * from " + tabr + " where id_padre = " + id);
                dati.put("righe", righe);
                ArrayList cliente = DbUtils.getListMap(Db.getConn(), "select c.* from clie_forn c join " + tabt + " t on c.codice = t." + campoclifor + " where t.id = " + id);
                dati.put("cliente", cliente);
                ArrayList movimenti = DbUtils.getListMap(Db.getConn(), "select * from movimenti_magazzino where da_tabella = '" + tabt + "' and da_id = " + id);
                dati.put("movimenti", movimenti);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (doc.equals("ddt")) {
            try {
                ArrayList testa = DbUtils.getListMap(Db.getConn(), "select * from " + tabt + " where id = " + id);
                dati.put("testa", testa);
                ArrayList righe = DbUtils.getListMap(Db.getConn(), "select * from " + tabr + " where id_padre = " + id);
                dati.put("righe", righe);
                ArrayList cliente = DbUtils.getListMap(Db.getConn(), "select c.* from clie_forn c join " + tabt + " t on c.codice = t." + campoclifor + " where t.id = " + id);
                dati.put("cliente", cliente);
                ArrayList movimenti = DbUtils.getListMap(Db.getConn(), "select * from movimenti_magazzino where da_tabella = '" + tabt + "' and da_id = " + id);
                dati.put("movimenti", movimenti);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (doc.equals("ordine")) {
            try {
                ArrayList testa = DbUtils.getListMap(Db.getConn(), "select * from " + tabt + " where id = " + id);
                dati.put("testa", testa);
                ArrayList righe = DbUtils.getListMap(Db.getConn(), "select * from " + tabr + " where id_padre = " + id);
                dati.put("righe", righe);
                ArrayList cliente = DbUtils.getListMap(Db.getConn(), "select c.* from clie_forn c join " + tabt + " t on c.codice = t." + campoclifor + " where t.id = " + id);
                dati.put("cliente", cliente);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.err.println("doc:" + doc + " non roconosciuto");
            Thread.dumpStack();
            return;
        }

        try {
//            ByteArrayOutputStream bout = new ByteArrayOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(bout);
//            oos.writeObject(dati);

            //hessian serialization
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Hessian2Output out = new Hessian2Output(bout);
            out.startMessage();
            out.writeObject(dati);
            out.completeMessage();
            out.close();

            //codifico base64
            byte[] bytes64 = Base64.encodeBase64(bout.toByteArray());

//            ResultSet r = DbUtils.tryOpenResultSetEditable(Db.getConn(), "select * from storico limit 0");
//            r.moveToInsertRow();
//            r.updateString("nota", nota);
//            r.updateBytes("dati", bytes64);
//            r.updateString("hostname", SystemUtils.getHostname());
//            r.updateString("login_so", main.login);
//            r.updateInt("username_id", main.utente.getIdUtente());
//            r.updateString("username", main.utente.getNomeUtente());
//            r.insertRow();
            
            Map m = new HashMap();
            m.put("nota", nota);
            m.put("dati", bytes64);
            m.put("hostname", SystemUtils.getHostname());
            m.put("login_so", main.login);
            m.put("username_id", main.utente.getIdUtente());
            m.put("username", main.utente.getNomeUtente());
            String sql = "insert into storico set " + dbu.prepareSqlFromMap(m);
            dbu.tryExecQuery(Db.getConn(), sql);
            
            int idstorico = CastUtils.toInteger0(DbUtils.getObject(Db.getConn(), "select LAST_INSERT_ID()")).intValue();
            System.out.println("id storico:" + idstorico);

//            //test di rilettura
//            ResultSet rtest = DbUtils.tryOpenResultSetEditable(Db.getConn(), "select * from storico where id = " + idstorico);
//            if (rtest.next()) {
////                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(rtest.getBytes("dati")));
////                Object oread = ois.readObject();
//
//                //hessian serialization
//                ByteArrayInputStream bin = new ByteArrayInputStream(rtest.getBytes("dati"));
//                Hessian2Input in = new Hessian2Input(bin);
//                in.startMessage();
//                Object oread = in.readObject();
//                in.completeMessage();
//                in.close();
//                bin.close();
//
//                System.out.println("test rilettura storico:");
//                DebugUtils.dump(oread);
//            }
//            try {
//                rtest.getStatement().close();
//                rtest.close();
//            } catch (Exception e) {
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Hessian2Output out = new Hessian2Output(bos);
            out.startMessage();
            out.writeObject(dati);
            out.completeMessage();
            out.close();
            byte[] data = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(mb.getDiff("storico"));
    }

    public static void leggiDaStorico(String cosa, int id) {
        List<Map> list;
        String msg = "";
        try {
            String sql = "select id, data from storico where nota like 'modifica " + cosa + "  id:" + id + " %' or nota like 'modifica " + cosa + " id:" + id + " %'";

            System.out.println("sql = " + sql);
            list = DbUtils.getListMap(Db.getConn(), sql);
            System.out.println("list = " + list);
            for (Map rec : list) {
                msg += leggiDaStorico(CastUtils.toInteger(rec.get("id")));
            }
        } catch (Exception ex) {
            Logger.getLogger(InvoicexUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        JFrameDb frame = new JFrameDb();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.sqlarea.setText(msg);
    }

    public static String leggiDaStorico(int idStorico) throws Exception {
        //test di rilettura
        String msg = "";
        System.out.println("lettura sotrico id:" + idStorico);
        ResultSet rtest = DbUtils.tryOpenResultSetEditable(Db.getConn(), "select * from storico where id = " + idStorico);
        if (rtest.next()) {
            try {
                //prima provo serilalizzazione java (vecchi ostorico ma problemi con class version)
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(rtest.getBytes("dati")));
                Object oread = ois.readObject();
                System.out.println("rilettura storico:");
                DebugUtils.dump(oread);
                String sqlrighe = stampaSqlRighe(oread);
                if (sqlrighe.length() > 0) {
                    msg += "da storico " + rtest.getString("id") + " data " + rtest.getString("data") + "\n" + sqlrighe + "\n\n";
                }
            } catch (Exception e) {
                System.out.println("rileggo con hessian per err:" + e.getMessage());
                //provo con nuovo sistema hessian
                try {
                    byte[] bytes64dec = Base64.decodeBase64(rtest.getBytes("dati"));
                    ByteArrayInputStream bin = new ByteArrayInputStream(bytes64dec);
                    Hessian2Input in = new Hessian2Input(bin);
                    in.startMessage();
                    Object oread = in.readObject();
                    in.completeMessage();
                    in.close();
                    bin.close();
                    DebugUtils.dump(oread);

                    String sqlrighe = stampaSqlRighe(oread);
                    if (sqlrighe.length() > 0) {
                        msg += "da storico " + rtest.getString("id") + " data " + rtest.getString("data") + "\n" + sqlrighe + "\n\n";
                    }
                } catch (Exception e2) {
                    System.out.println("impossibile leggere storico " + idStorico + " err:" + e2.getMessage());
                }
            }
        }
        try {
            rtest.getStatement().close();
            rtest.close();
        } catch (Exception e) {
        }
        return msg;
    }

    public static String stampaSqlRighe(Object o) {
        String sql = "";
        try {
            Map m = (Map) o;
            List<Map> l = (List<Map>) m.get("righe");
            for (Map ml : l) {
                sql += "insert ??? set " + DbUtils.prepareSqlFromMap(ml) + ";\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sql;
    }

    public static Integer getIdFattura(String serie, int numero, int anno) {
        return getIdFattura(Db.getConn(), serie, numero, anno);
    }

    public static Integer getIdFattura(Connection conn, String serie, int numero, int anno) {
        try {
            return (Integer) DbUtils.getObject(conn, "select id from test_fatt where tipo_fattura != 7 and serie = '" + Db.aa(serie) + "' and numero = " + numero + " and anno = " + anno);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getIdFatturaAcquisto(String serie, int numero, int anno) {
        try {
            return (Integer) DbUtils.getObject(Db.getConn(), "select id from test_fatt_acquisto where serie = '" + Db.aa(serie) + "' and numero = " + numero + " and anno = " + anno);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getIdDdt(String serie, int numero, int anno) {
        try {
            return (Integer) DbUtils.getObject(Db.getConn(), "select id from test_ddt where serie = '" + Db.aa(serie) + "' and numero = " + numero + " and anno = " + anno);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getIdDdtAcquisto(String serie, int numero, int anno) {
        try {
            return (Integer) DbUtils.getObject(Db.getConn(), "select id from test_ddt_acquisto where serie = '" + Db.aa(serie) + "' and numero = " + numero + " and anno = " + anno);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getIdOrdine(String serie, int numero, int anno) {
        try {
            return (Integer) DbUtils.getObject(Db.getConn(), "select id from test_ordi where serie = '" + Db.aa(serie) + "' and numero = " + numero + " and anno = " + anno);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getIdOrdineAcquisto(String serie, int numero, int anno) {
        try {
            return (Integer) DbUtils.getObject(Db.getConn(), "select id from test_ordi_acquisto where serie = '" + Db.aa(serie) + "' and numero = " + numero + " and anno = " + anno);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getNumeroScadenze(Integer id_doc) {
        try {
            return ((Long) DbUtils.getObject(Db.getConn(), "select count(*) from scadenze where documento_tipo = 'FA' and id_doc = " + id_doc)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void controlloProvvigioniAutomatiche(tnxComboField comAgente, tnxTextField texProvvigione, tnxTextField texScon1, JInternalFrame from, Integer clifor) {
        //trovo la percentuale di questo agente
        try {
            ResultSet r = Db.lookUp(comAgente.getSelectedKey().toString(), "id", "agenti");
            if (r == null) {
                texProvvigione.setText("");
                return;
            }
            Double provvigione_predefinita_cliente = null;
            if (clifor != null) {
                provvigione_predefinita_cliente = cu.toDouble(DbUtils.getObject(Db.getConn(), "select provvigione_predefinita_cliente from clie_forn where codice = " + clifor.toString(), false));
            }
            Double provvigione_agente_clifor = null;
            if (main.getPersonalContain("medcomp")) {
                provvigione_agente_clifor = cu.toDouble(DbUtils.getObject(Db.getConn(), "select provvigione from clie_forn_agenti where id_clifor = " + dbu.sql(clifor) + " and id_agente = " + dbu.sql(comAgente.getSelectedKey()), false));
            }

            iniFileProp fileIni = main.fileIni;

            double percentualeTabella = 0d;
            double percentualeForm = 0d;

            if (fileIni.getValueBoolean("pref", "provvigioniAutomatiche", false)) {
                System.out.println("SELECT (sconto_soglia) FROM soglie_provvigioni WHERE sconto_soglia <= " + Db.pc(texScon1.getText(), Types.DECIMAL));
                BigDecimal scontoMinTmp = (BigDecimal) DbUtils.getObject(Db.getConn(), "SELECT (sconto_soglia) FROM soglie_provvigioni WHERE sconto_soglia < " + Db.pc(texScon1.getText(), Types.DECIMAL) + " order by sconto_soglia desc", false);
                BigDecimal scontoMaxTmp = (BigDecimal) DbUtils.getObject(Db.getConn(), "SELECT (sconto_soglia) FROM soglie_provvigioni WHERE sconto_soglia >= " + Db.pc(texScon1.getText(), Types.DECIMAL) + " order by sconto_soglia", false);

                BigDecimal provvMinTmp = BigDecimal.valueOf(0d);
                BigDecimal provvMaxTmp = BigDecimal.valueOf(0d);

                scontoMinTmp = scontoMinTmp == null ? BigDecimal.valueOf(0d) : scontoMinTmp;
                scontoMaxTmp = scontoMaxTmp == null ? BigDecimal.valueOf(100d) : scontoMaxTmp;

                if (fileIni.getValueBoolean("pref", "provvigioniPercentualeAuto", false)) {
                    provvMinTmp = (BigDecimal) DbUtils.getObject(Db.getConn(), "SELECT (percentuale) FROM soglie_provvigioni WHERE sconto_soglia < " + Db.pc(texScon1.getText(), Types.DECIMAL) + " order by sconto_soglia desc", false);
                    provvMaxTmp = (BigDecimal) DbUtils.getObject(Db.getConn(), "SELECT (percentuale) FROM soglie_provvigioni WHERE sconto_soglia >= " + Db.pc(texScon1.getText(), Types.DECIMAL) + " order by sconto_soglia", false);

                    provvMinTmp = provvMinTmp == null ? (BigDecimal) DbUtils.getObject(Db.getConn(), "SELECT (percentuale) FROM soglie_provvigioni order by sconto_soglia") : provvMinTmp;
                    provvMaxTmp = provvMaxTmp == null ? (BigDecimal) DbUtils.getObject(Db.getConn(), "SELECT (percentuale) FROM soglie_provvigioni order by sconto_soglia desc") : provvMaxTmp;
                } else {
                    Integer sogliaMinTmp = (Integer) DbUtils.getObject(Db.getConn(), "SELECT (soglia) FROM soglie_provvigioni WHERE sconto_soglia < " + Db.pc(texScon1.getText(), Types.DECIMAL) + " order by sconto_soglia desc", false);
                    Integer sogliaMaxTmp = (Integer) DbUtils.getObject(Db.getConn(), "SELECT (soglia) FROM soglie_provvigioni WHERE sconto_soglia >= " + Db.pc(texScon1.getText(), Types.DECIMAL) + " order by sconto_soglia", false);

                    sogliaMinTmp = sogliaMinTmp == null ? 0 : sogliaMinTmp;
                    sogliaMaxTmp = sogliaMaxTmp == null ? 5 : sogliaMaxTmp;

                    if (sogliaMinTmp != 0) {
                        provvMinTmp = (BigDecimal) DbUtils.getObject(Db.getConn(), "SELECT percentuale_soglia_" + sogliaMinTmp + " FROM agenti WHERE id = " + Db.pc(comAgente.getSelectedKey(), Types.INTEGER), false);
                    } else {
                        provvMinTmp = BigDecimal.valueOf(0d);
                    }
                    provvMaxTmp = (BigDecimal) DbUtils.getObject(Db.getConn(), "SELECT percentuale_soglia_" + sogliaMaxTmp + " FROM agenti WHERE id = " + Db.pc(comAgente.getSelectedKey(), Types.INTEGER), false);

                    provvMinTmp = provvMinTmp == null ? BigDecimal.valueOf(0d) : provvMinTmp;
                    provvMaxTmp = provvMaxTmp == null ? (BigDecimal) DbUtils.getObject(Db.getConn(), "SELECT percentuale_soglia_5 FROM agenti WHERE id = " + Db.pc(comAgente.getSelectedKey(), Types.INTEGER), false) : provvMaxTmp;
                }

                Double provvigione = 0d;
                Double sconto = cu.toDouble0All(texScon1.getText());
                Double scontoMin = scontoMinTmp.doubleValue();
                Double scontoMax = scontoMaxTmp.doubleValue();
                Double provvMin = provvMinTmp.doubleValue();
                Double provvMax = provvMaxTmp.doubleValue();

                if (fileIni.getValueBoolean("pref", "provvigioniPercMedia", false)) {
                    provvigione = ((((sconto - scontoMin) / (scontoMax - scontoMin)) * (provvMax - provvMin)) + provvMin);
                } else {
//                    provvigione = provvMin;
                    provvigione = provvMax;
                }

                percentualeTabella = provvigione == null ? 0 : provvigione;
                percentualeForm = Db.getDouble(texProvvigione.getText());
            } else {
                percentualeTabella = r.getObject("percentuale") == null ? 0 : r.getDouble("percentuale");
                percentualeForm = Db.getDouble(texProvvigione.getText());
                if (provvigione_predefinita_cliente != null && provvigione_predefinita_cliente != 0) {
                    percentualeTabella = provvigione_predefinita_cliente;
                }
                if (provvigione_agente_clifor != null && provvigione_agente_clifor != 0) {
                    percentualeTabella = provvigione_agente_clifor;
                }
            }

            double prov_prima = Db.getDouble(texProvvigione.getText());
            if (texProvvigione.getText().length() == 0 || percentualeForm == percentualeTabella || percentualeForm == 0) {
//                if (!fileIni.getValueBoolean("pref", "provvigioniAutomatiche", false)) {
//                    texProvvigione.setText(Db.formatValuta(r.getDouble("percentuale")));
//                } else {
//                    texProvvigione.setText(Db.formatValuta(percentualeTabella));
//                }
                texProvvigione.setText(Db.formatNumero(percentualeTabella));
            } else if (!fileIni.getValueBoolean("pref", "provvigioniAutomatiche", false)) {
                int ret = javax.swing.JOptionPane.showConfirmDialog(from, "La percentuale di provvigione dell'agente selezionato (" + Db.formatNumero(percentualeTabella) + "%) differisce da quella impostata (" + Db.formatNumero(percentualeForm) + "%).\nPer modificarla premere 'Si'", "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION);

                if (ret == javax.swing.JOptionPane.YES_OPTION) {
//                        texProvvigione.setText(Db.formatValuta(r.getDouble("percentuale")));
                    texProvvigione.setText(Db.formatNumero(percentualeTabella));
                }
            } else {
                texProvvigione.setText(Db.formatNumero(percentualeTabella));
            }
            double prov_dopo = Db.getDouble(texProvvigione.getText());
            if (prov_prima != prov_dopo && ((GenericFrmTest) from).getGrid().getRowCount() > 0) {
                ((GenericFrmTest) from).aggiornareProvvigioni();
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    static public String getStatoEvasione(JTable tab, String qta, String qta_evasa) {
        String evaso = "";
        try {
            double sq = 0;
            double sqn = 0;
            double sqe = 0;
            double sqen = 0;
            boolean evasa = true;
            for (int row = 0; row < tab.getRowCount(); row++) {
                sqn = CastUtils.toDouble0(tab.getValueAt(row, tab.getColumn(qta).getModelIndex()));
                sqen = CastUtils.toDouble0(tab.getValueAt(row, tab.getColumn(qta_evasa).getModelIndex()));
                sq += sqn;
                sqe += sqen;
                if (sqn > 0 && sqen < sqn) {
                    evasa = false;
                }
            }
            if (evasa) {
                evaso = "S";
            } else if (sqe > 0) {
                evaso = "P";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return evaso;
    }

    static public void aggiornaStatoEvasione(String tipoDoc, Integer id) {
        String sql = "select IFNULL(quantita, 0) as sq, IFNULL(quantita_evasa,0) as sqe from " + Db.getNomeTabR(tipoDoc);
        sql += " where id_padre = " + id;
        System.out.println("sql = " + sql);
        String evaso = "";
        try {
            List<Map> list = DbUtils.getListMap(Db.getConn(), sql);
            double sq = 0;
            double sqn = 0;
            double sqe = 0;
            double sqen = 0;
            boolean evasa = true;
            for (int row = 0; row < list.size(); row++) {
                Map m = list.get(row);
                sqn = CastUtils.toDouble0(m.get("sq"));
                sqen = CastUtils.toDouble0(m.get("sqe"));
                sq += sqn;
                sqe += sqen;
                if (sqn > 0 && sqen < sqn) {
                    evasa = false;
                }
            }
            if (evasa) {
                evaso = "S";
            } else if (sqe > 0) {
                evaso = "P";
            }
            sql = "update " + Db.getNomeTabT(tipoDoc) + " set evaso = '" + evaso + "' where id = " + id;
            System.out.println("sql = " + sql);
            DbUtils.tryExecQuery(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Integer getIdDaNumero(String tipoDoc, String serie, Integer numero, Integer anno) {
        try {
            String sql = "select id from " + Db.getNomeTabT(tipoDoc);
            sql += " where serie = '" + Db.aa(serie) + "' and numero = " + numero + " and anno = " + anno;
            if (tipoDoc.equals(Db.TIPO_DOCUMENTO_SCONTRINO)) {
                sql += " and tipo_fattura = 7";
            } else if (tipoDoc.equals(Db.TIPO_DOCUMENTO_FATTURA)) {
                sql += " and tipo_fattura != 7";
            }
            return (Integer) DbUtils.getObject(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getNumeroDaId(String tipoDocDest, Integer id) {
        return getNumeroDaId(tipoDocDest, id, true);
    }

    public static String getNumeroDaId(String tipoDocDest, Integer id, boolean includiTipoDoc) {
        try {
            String sql = "select numero, serie, anno from " + Db.getNomeTabT(tipoDocDest) + " where id = " + id;
            if (tipoDocDest.equals(Db.TIPO_DOCUMENTO_SCONTRINO)) {
                sql += " and tipo_fattura = 7";
            } else if (tipoDocDest.equals(Db.TIPO_DOCUMENTO_FATTURA)) {
                sql += " and tipo_fattura != 7";
            }
            System.out.println("sql = " + sql);
            List<Map> list = DbUtils.getListMap(Db.getConn(), sql);
            Map m = list.get(0);
            String ret = "";
            if (includiTipoDoc) {
                ret += Db.getDescTipoDocBrevissima(tipoDocDest);
            }
            if (CastUtils.toString(m.get("serie")).trim().equals("")) {
                ret += " " + m.get("numero");
            } else {
                ret += " " + m.get("serie") + "/" + m.get("numero");
            }
            return ret;
        } catch (Exception e) {
            System.out.println("errore in getNumeroDaId : " + tipoDocDest + " " + id);
            e.printStackTrace();
            return null;
        }
    }

    static public DefaultTableCellRenderer getFlagRender() {
        return new DefaultTableCellRenderer() {
            Color green = new java.awt.Color(200, 255, 200);
            Color dback = UIManager.getColor("Table.background");
            Color bmix = SwingUtils.mixColours(dback, green);
            DefaultTableCellRenderer r2 = new DefaultTableCellRenderer();
            Font font1 = UIManager.getFont("TextField.font");
            Font font2 = UIManager.getFont("TextField.font").deriveFont(UIManager.getFont("TextField.font").getSize() - 2f);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Component c2 = r2.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                dback = c2.getBackground();
                ((JLabel) c).setFont(font1);
                String conv = CastUtils.toString(value);
                String conv2 = CastUtils.toString(table.getValueAt(row, table.getColumn("convertito2").getModelIndex()));
                if (value.toString().length() > 0 || conv.trim().length() > 0 || conv2.trim().length() > 0) {
                    c.setBackground(SwingUtils.mixColours(dback, green));
                    if (!conv2.trim().equals("")) {
                        ((JLabel) c).setFont(font2);
                        ((JLabel) c).setText(StringUtils.replace(conv2, "\n", " - "));
                    } else {
                        ((JLabel) c).setText(conv);
                    }
                } else {
                    c.setBackground(dback);
                    ((JLabel) c).setText("");
                }
                return c;
            }
        };
    }

    static public SubstanceDefaultTableCellRenderer getFlagRenderSubstance() {
        return new org.jvnet.substance.SubstanceDefaultTableCellRenderer() {
            Font font1 = UIManager.getFont("TextField.font");
            Font font2 = UIManager.getFont("TextField.font").deriveFont(UIManager.getFont("TextField.font").getSize() - 2f);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ((JLabel) c).setFont(font1);
                String conv = CastUtils.toString(value);
                String conv2 = CastUtils.toString(table.getValueAt(row, table.getColumn("convertito2").getModelIndex()));
                if (value.toString().length() > 0 || conv.trim().length() > 0 || conv2.trim().length() > 0) {
                    c.setBackground(SwingUtils.mixColours(c.getBackground(), new java.awt.Color(200, 255, 200)));
                    if (conv2.trim().equals("")) {
                        ((JLabel) c).setText(conv);
                    } else {
                        ((JLabel) c).setFont(font2);
                        ((JLabel) c).setText(StringUtils.replace(conv2, "\n", " - "));
                    }
                }
                return c;
            }
        };
    }

    public static DefaultTableCellRenderer getEvasoRender() {
        return new DefaultTableCellRenderer() {
            DefaultTableCellRenderer r2 = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lab = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Component c2 = r2.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lab.setHorizontalAlignment(JLabel.CENTER);
                lab.setFont(UIManager.getFont("Label.font"));
                if (value == null) {
                    value = "";
                }
                try {
                    if (value.toString().equalsIgnoreCase("S")) {
                        lab.setText("Sì");
                        lab.setBackground(SwingUtils.mixColours(new java.awt.Color(200, 255, 200), c2.getBackground()));
                    } else if (value.toString().equalsIgnoreCase("P")) {
                        lab.setText("Parziale");
                        lab.setBackground(SwingUtils.mixColours(new java.awt.Color(255, 255, 200), c2.getBackground()));
                        lab.setFont(lab.getFont().deriveFont((float) lab.getFont().getSize() - 2f));
                    } else {
                        lab.setText("");
                        lab.setBackground(SwingUtils.mixColours(new java.awt.Color(255, 255, 255), c2.getBackground()));
                    }
                } catch (java.lang.NullPointerException errNull) {
                }
                return lab;
            }
        };
    }

    public static DefaultTableCellRenderer getEvasoRenderSubstance() {
        return new org.jvnet.substance.SubstanceDefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lab = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lab.setHorizontalAlignment(JLabel.CENTER);
                lab.setFont(UIManager.getFont("Label.font"));
                if (value == null) {
                    value = "";
                }
                try {
                    if (value.toString().equalsIgnoreCase("S")) {
                        lab.setText("Sì");
                        lab.setBackground(SwingUtils.mixColours(new java.awt.Color(200, 255, 200), lab.getBackground()));
                    } else if (value.toString().equalsIgnoreCase("P")) {
                        lab.setText("Parziale");
                        lab.setBackground(SwingUtils.mixColours(new java.awt.Color(255, 255, 200), lab.getBackground()));
                        lab.setFont(lab.getFont().deriveFont((float) lab.getFont().getSize() - 2f));
                    } else {
                        lab.setText("");
                        lab.setBackground(SwingUtils.mixColours(new java.awt.Color(255, 255, 255), lab.getBackground()));
                    }
                } catch (java.lang.NullPointerException errNull) {
                }
                return lab;
            }
        };
    }

    static public boolean isFunzioniManutenzione() {
        return main.getPadrePanel().menFunzioniManutenzione.isSelected();
    }

    public static void aggiornaElenchiFatture() {
        JInternalFrame[] iframes = main.getPadre().getDesktopPane().getAllFrames();
        for (JInternalFrame f : iframes) {
            if (f instanceof frmElenFatt) {
                ((frmElenFatt) f).dbRefresh();
            }
        }
    }

    public static void aggiornaElenchi() {
        JInternalFrame f = (JInternalFrame) InvoicexUtil.getActiveJInternalFrame();
        if (f instanceof frmElenOrdini) {
            ((frmElenOrdini) f).dbRefresh();
        }
        if (f instanceof frmElenDDT) {
            ((frmElenDDT) f).dbRefresh();
        }
        if (f instanceof frmElenFatt) {
            ((frmElenFatt) f).dbRefresh();
        }
        if (f instanceof JInternalFrameScadenzario) {
            ((JInternalFrameScadenzario) f).selezionaSituazione();
        }
    }

    public static void aggiornaListini() {
        System.out.println("aggiornaListini");
        try {
            List<Map> listini = DbUtils.getListMap(Db.getConn(), "select * from tipi_listino");
            for (Map rec : listini) {
                if (Sync.isActive()) {
                    String listino = cu.s(rec.get("codice"));
                    String sql = "select codice "
                            + " from articoli a left join articoli_prezzi ap on a.codice = ap.articolo "
                            + " where ap.articolo is null";
                    List<Map> list = dbu.getListMap(Db.getConn(), sql);
                    for (Map m : list) {
                        sql = "insert into articoli_prezzi set articolo = " + dbu.sql(m.get("codice")) + ", listino = " + dbu.sql(listino) + ", prezzo = 0";
                        dbu.tryExecQuery(Db.getConn(), sql);
                    }
                } else {
                    String sql = "insert ignore into articoli_prezzi (articolo, listino, prezzo) select codice, " + Db.pcs((String) rec.get("codice")) + " as listino, 0 as prezzo from articoli";
                    System.out.println("aggiornaListini sql: " + sql);
                    dbu.tryExecQuery(Db.getConn(), sql);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static InputStream caricaLogoDaDb(Connection conn) {
        return caricaLogoDaDb(conn, "logo");
    }

    public static InputStream caricaLogoDaDb(Connection conn, String campo) {
        return caricaLogoDaDb(conn, campo, "dati_azienda", "");
    }

    public static InputStream caricaLogoDaDb(Connection conn, String campo, String tabella, String where) {
        ResultSet r = null;
        try {
            controllaAggiornamentoFileLogo(campo, tabella, where);
            r = DbUtils.tryOpenResultSet(conn, "select id, " + campo + " from " + tabella + " " + where);
            if (r.next()) {
                Blob blob = r.getBlob(campo);
                if (blob == null) {
                    return null;
                }
                return blob.getBinaryStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.close(r);
        }
        return null;
    }

    public static byte[] caricaLogoDaDbBytes(Connection conn, String campo) {
        ResultSet r = null;
        try {
            controllaAggiornamentoFileLogo(campo);
            r = DbUtils.tryOpenResultSet(conn, "select id, " + campo + " from dati_azienda");
            if (r.next()) {
                Blob blob = r.getBlob(campo);
                if (blob == null) {
                    return null;
                }
                return IOUtils.toByteArray(blob.getBinaryStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.close(r);
        }
        return null;
    }

    public static void salvaImgInDb(String file) {
        salvaImgInDb(file, "logo");
    }

    public static void salvaImgInDb(String file, String campo) {
        salvaImgInDb(file, campo, "dati_azienda", "");
    }

    public static void salvaImgInDb(String file, String campo, String table, String where) {
        try {
            File fileLogo = new File(file);
            boolean stampareLogo = true;

            if (file != null && fileLogo.exists()) {
                System.out.println("salvo logo in db per file:" + file + " fileLogo.exist:" + fileLogo.exists());
                FileInputStream is = new FileInputStream(fileLogo);
                byte[] bb = new byte[(int) fileLogo.length()];
                is.read(bb);
                Statement s = Db.getConn().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet r = s.executeQuery("select id," + campo + ", " + campo + "_nome_file, " + campo + "_data_modifica, " + campo + "_dimensione from " + table + " " + where);
                if (r.next()) {
                    r.updateObject(campo, bb);
                    r.updateObject(campo + "_nome_file", file);
                    r.updateObject(campo + "_data_modifica", fileLogo.lastModified());
                    r.updateObject(campo + "_dimensione", fileLogo.length());
                    r.updateRow();
                }
                r.close();
                s.close();
            } else {
                System.out.println("azzero logo in db per file:" + file + " fileLogo.exist:" + fileLogo.exists());
                Statement s = Db.getConn().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet r = s.executeQuery("select id," + campo + ", " + campo + "_nome_file, " + campo + "_data_modifica, " + campo + "_dimensione from " + table + " " + where);
                if (r.next()) {
                    r.updateObject(campo, null);
                    r.updateObject(campo + "_nome_file", null);
                    r.updateObject(campo + "_data_modifica", null);
                    r.updateObject(campo + "_dimensione", null);
                    r.updateRow();
                }
                r.close();
                s.close();
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }

    private static void controllaAggiornamentoFileLogo(String campo) {
        controllaAggiornamentoFileLogo(campo, "dati_azienda", "");
    }

    private static void controllaAggiornamentoFileLogo(String campo, String tabella, String where) {
        //logo_nome_file
        try {
            List<Map> lm = DbUtils.getListMap(Db.getConn(true), "select id, " + campo + "_nome_file as nome_file, " + campo + "_data_modifica as data_modifica, " + campo + "_dimensione as dimensione from " + tabella + " " + where);
            Map m = lm.get(0);
            String nomefile = (String) m.get("nome_file");
            if (StringUtils.isNotBlank(nomefile)) {
                File test = new File(nomefile);
                if (test.exists()) {
                    //se trovo il file controllo se è cambiato
                    Long data_modifica = (Long) m.get("data_modifica");
                    Long dimensione = (Long) m.get("dimensione");
                    if (test.lastModified() > data_modifica || test.length() != dimensione) {
                        //aggiornare
                        InvoicexUtil.salvaImgInDb(test.getAbsolutePath(), campo, tabella, where);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String aggiungi_recapiti(String recapito_prec, boolean dest_diversa, ResultSet rCliente, ResultSet rDocu) {
        String pre = dest_diversa ? "dest_" : "";
        ResultSet r = dest_diversa ? rDocu : rCliente;
        try {
            String recapito = "";
            if (main.fileIni.getValueBoolean("pref", "stampaTelefono", false)) {
                if (it.tnx.Db.nz(r.getString(pre + "telefono"), "").length() > 0) {
                    recapito += "Tel. " + r.getString(pre + "telefono");
                }
            }

            if (main.fileIni.getValueBoolean("pref", "stampaCellulare", false)) {
                if (it.tnx.Db.nz(r.getString(pre + "cellulare"), "").length() > 0) {
                    if (recapito.length() > 0) {
                        recapito += " ";
                    }
                    recapito += "Cell. " + r.getString(pre + "cellulare");
                }
            }

            if (main.fileIni.getValueBoolean("pref", "stampaFax", false)) {
                try {
                    if (it.tnx.Db.nz(r.getString(pre + "fax"), "").length() > 0) {
                        if (recapito.length() > 0) {
                            recapito += " ";
                        }
                        recapito += "Fax " + r.getString(pre + "fax");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            recapito = recapito_prec + (StringUtils.isBlank(recapito_prec) ? "" : "<br>") + recapito;
            return recapito;
        } catch (Exception e) {
            e.printStackTrace();
            return recapito_prec;
        }

    }

    public static void attendiCaricamentoPluginRitenute(final Runnable run) {
        if (main.fine_init_plugin) {
            run.run();
        } else {
            final JWindow flash = SwingUtils.showFlashMessage2("Attendere caricamento plugins", 5, null, Color.red, new Font(null, Font.BOLD, 16), true);
            Thread t = new Thread("attendere caricamento plugins") {
                @Override
                public void run() {
                    while (!main.fine_init_plugin) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(InvoicexUtil.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    run.run();
                    flash.dispose();
                }
            };
            t.start();
        }
    }

    public static String getIvaDefault() {
        try {
            return (String) DbUtils.getObject(Db.getConn(), "select codiceIvaDefault from dati_azienda limit 1");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getIvaSpese() {
        try {
            return (String) DbUtils.getObject(Db.getConn(), "select codiceIvaSpese from dati_azienda limit 1");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isPassaggio21eseguito() {
        try {
            String eseguito = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select iva21eseguito from dati_azienda limit 1"));
            if (eseguito.equalsIgnoreCase("S")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isPassaggio22eseguito() {
        try {
            String eseguito = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select iva22eseguito from dati_azienda limit 1"));
            if (eseguito.equalsIgnoreCase("S")) {
                return true;
            }
            if (DbUtils.containRows(Db.getConn(), "select codice from codici_iva where codice = '22'")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getIvaDefaultPassaggio() {
        String iva = getIvaDefault();
        if (StringUtils.isBlank(iva)) {
            if (isPassaggio22eseguito()) {
                return "22";
            } else {
                return "21";
            }
        } else {
            return iva;
        }
    }

    static public void checkSize(Window w) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension ss = toolkit.getScreenSize();
        System.out.println("checkSize ss = " + ss);
        Insets ds = toolkit.getScreenInsets(w.getGraphicsConfiguration());
        System.out.println("ds = " + ds);
        ss.setSize(ss.getWidth() - ds.left - ds.right, ss.getHeight() - ds.top - ds.bottom);
//        ss.setSize(1024, 600);
        if (w.getHeight() > ss.getHeight()) {
            w.setSize(w.getWidth(), (int) ss.getHeight());
        }
    }

    public static int getHeightIntFrame() {
        MenuPanel m = main.getPadrePanel();
        return (int) m.getDesktopPane().getVisibleRect().getHeight() - m.getNextFrameTop();
    }

    public static int getHeightIntFrame(int h) {
        MenuPanel m = main.getPadrePanel();
        if ((m.getDesktopPane().getVisibleRect().getHeight() - m.getNextFrameTop()) < h) {
            return (int) m.getDesktopPane().getVisibleRect().getHeight() - m.getNextFrameTop();
        } else {
            return h;
        }
    }

    public static Rectangle getDesktopSize() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension ss = toolkit.getScreenSize();
        Insets ds = toolkit.getScreenInsets(new Frame().getGraphicsConfiguration());
        return new Rectangle((int) ss.getWidth() - ds.left - ds.right, (int) ss.getHeight() - ds.top - ds.bottom);
    }

    public static Point getDesktopTopLeft() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Insets ds = toolkit.getScreenInsets(new Frame().getGraphicsConfiguration());
        return new Point(ds.top, ds.left);
    }

    static public double calcolaPrezzoArrotondato(double prezzo, double parametro, boolean inf) {
        double res = 0d;
        if (parametro == 0d) {
            return prezzo;
        } else if (inf) {
            res = (Math.floor(prezzo / parametro)) * parametro;
        } else {
            res = (Math.ceil(prezzo / parametro)) * parametro;
        }

        return res;
    }

    public static void aggiornaTotaliRighe(String tipoDoc, int id) {
        aggiornaTotaliRighe(tipoDoc, id, null);
    }

    public static void aggiornaTotaliRighe(String tipoDoc, int id, Boolean is_prezzi_ivati) {
        String tabt = Db.getNomeTabT(tipoDoc);
        String tabr = Db.getNomeTabR(tipoDoc);
        aggiornaTotaliRighe(tipoDoc, id, is_prezzi_ivati, tabt, tabr);
    }

    public static void aggiornaTotaliRighe(String tipoDoc, int id, Boolean is_prezzi_ivati, String tabt, String tabr) {
        System.out.println("aggiornaTotaliRighe tipoDoc:" + tipoDoc + " id:" + id);

        String sql0 = "UPDATE " + tabr + " set arrotondamento_parametro = '0' where arrotondamento_parametro is null or arrotondamento_parametro = ''";
        System.out.println("sql0 = " + sql0);
        try {
            DbUtils.tryExecQuery(Db.getConn(), sql0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (is_prezzi_ivati == null) {
            try {
                String prezzi_ivati = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select prezzi_ivati from " + tabt + " where id = " + id));
                is_prezzi_ivati = !prezzi_ivati.equalsIgnoreCase("N");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!is_prezzi_ivati) {
            String sql1 = "UPDATE " + tabr + " set totale_imponibile = ROUND(CAST(  IF(arrotondamento_parametro = '0',  (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.',   FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2)),2)";
            sql1 += " where id_padre = " + id;
            System.out.println("sql1 = " + sql1);
            try {
                DbUtils.tryExecQuery(Db.getConn(), sql1);
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(InvoicexUtil.getActiveJInternalFrame(), e);
            }

            if (!Sync.isActive()) {
                String sql2 = "UPDATE " + tabr + " r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = round(totale_imponibile + (totale_imponibile * i.percentuale/100),2)";
                sql2 += " where id_padre = " + id;
                System.out.println("sql2 = " + sql2);
                try {
                    DbUtils.tryExecQuery(Db.getConn(), sql2);
                } catch (Exception e) {
                    SwingUtils.showExceptionMessage(InvoicexUtil.getActiveJInternalFrame(), e);
                }
            } else {
                try {
                    List<Map> list = dbu.getListMap(Db.getConn(), "select r.id, round(totale_imponibile + (totale_imponibile * i.percentuale/100),2) as totale_ivato from " + tabr + " r LEFT JOIN codici_iva i ON r.iva = i.codice where id_padre = " + id);
                    for (Map m : list) {
                        String sql2 = "UPDATE " + tabr + " r SET totale_ivato = " + dbu.sql(m.get("totale_ivato")) + " where id = " + dbu.sql(m.get("id"));
                        dbu.tryExecQuery(Db.getConn(), sql2, false);
                    }
                } catch (Exception e) {
                    SwingUtils.showExceptionMessage(InvoicexUtil.getActiveJInternalFrame(), e);
                }
            }
        } else {
            String sql1 = "UPDATE " + tabr + " set totale_ivato = ROUND(CAST(  IF(arrotondamento_parametro = '0',  (prezzo_ivato - (prezzo_ivato*(IFNULL(sconto1, 0.00)/100)) - ((prezzo_ivato - (prezzo_ivato*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.',   FLOOR(((prezzo_ivato - (prezzo_ivato*(IFNULL(sconto1, 0.00)/100)) - ((prezzo_ivato - (prezzo_ivato*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo_ivato - (prezzo_ivato*(IFNULL(sconto1, 0.00)/100)) - ((prezzo_ivato - (prezzo_ivato*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2)),2)";
            sql1 += " where id_padre = " + id;
            System.out.println("sql1 = " + sql1);
            try {
                DbUtils.tryExecQuery(Db.getConn(), sql1);
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(InvoicexUtil.getActiveJInternalFrame(), e);
            }

            String sql2 = "UPDATE " + tabr + " r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_imponibile = round(totale_ivato - round(totale_ivato * i.percentuale / (100 + i.percentuale),2),2)";
            sql2 += " where id_padre = " + id;
            System.out.println("sql2 = " + sql2);
            try {
                DbUtils.tryExecQuery(Db.getConn(), sql2);
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(InvoicexUtil.getActiveJInternalFrame(), e);
            }
        }
    }

    public static List<JInternalFrame> getFrames(Class aClass) {
        JInternalFrame[] frames = main.getPadre().getDesktopPane().getAllFrames();
        List list = new ArrayList();
        for (JInternalFrame f : frames) {
            if (f.getClass().getName().equals(aClass.getName())) {
                list.add(f);
            }
        }
        return list;
    }

    public static void macButtonSmall(JButton but) {
        but.putClientProperty("JComponent.sizeVariant", "small");
        but.putClientProperty("JButton.buttonType", "textured");
    }

    public static void macButtonRegular(JButton but) {
        but.putClientProperty("JComponent.sizeVariant", "regular");
        but.putClientProperty("JButton.buttonType", "textured");
    }

    public static void macButtonGradient(JButton but) {
        but.putClientProperty("JButton.buttonType", "gradient");
    }

    public static void riportaLotti(JTable table, String tabellaDest, String tabellaProvRighe, String tipodoc_a, JFrame parent, Integer codice_deposito) {
        JTable tab = table;
        String sql = "";
        int col_lotti = tab.getColumn("gestione_lotti").getModelIndex();
        int col_art = tab.getColumn("articolo").getModelIndex();
        int col_qta_conf = tab.getColumn("quantità confermata").getModelIndex();
        col_lotti = tab.convertColumnIndexToView(col_lotti);
        col_art = tab.convertColumnIndexToView(col_art);
        col_qta_conf = tab.convertColumnIndexToView(col_qta_conf);
        String toadd = "";
        String tabd = "";
        String tipo_mov = "S";
        if (tipodoc_a.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT)) {
            tabd = "righ_ddt";
        } else if (tipodoc_a.equalsIgnoreCase(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
            tipo_mov = "C";
            tabd = "righ_ddt_acquisto";
        } else if (tipodoc_a.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA)) {
            tabd = "righ_fatt";
        } else if (tipodoc_a.equalsIgnoreCase(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            tipo_mov = "C";
            tabd = "righ_fatt_acquisto";
        }
        for (int i = 0; i < table.getRowCount(); i++) {
            toadd = "";
            //modificare le quantita sul documento generato
            Integer tid_prov = CastUtils.toInteger(table.getValueAt(i, table.getColumn("prov_id").getModelIndex()));
            Integer rid_prov = CastUtils.toInteger(table.getValueAt(i, table.getColumn("prov_id_riga").getModelIndex()));
            Integer tid_dest = CastUtils.toInteger(table.getValueAt(i, table.getColumn("dest_id").getModelIndex()));
            Integer rid_dest = CastUtils.toInteger(table.getValueAt(i, table.getColumn("dest_id_riga").getModelIndex()));

            double qtaconf = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("quantità confermata").getModelIndex()));
            double qta = CastUtils.toDouble0(table.getValueAt(i, table.getColumn("quantità").getModelIndex()));
            String codart = CastUtils.toString(table.getValueAt(i, table.getColumn("articolo").getModelIndex()));
            //azzero eventuali
            sql = "delete from righ_" + tabellaDest + "_lotti where id_padre = " + rid_dest;
            //porto controllando le quantità (per ora non possibile evasioe parziale di articoli con lotti, o completa o zero
            if (qtaconf > 0) {
                double qta_da_togliere = qtaconf;
                //nelle tabelle _lotti il campo id_padre si riferisce all' id della RIGA del documento padre
                sql = "select * from " + tabellaProvRighe + "_lotti where id_padre = " + rid_prov;
                try {
                    List<Map> lotti_prov = DbUtils.getListMap(Db.getConn(), sql);
                    DebugFastUtils.dump(lotti_prov);
                    for (Map rec : lotti_prov) {
                        Map m = new HashMap();
                        m.put("id_padre", rid_dest);
                        m.put("lotto", rec.get("lotto"));
                        m.put("codice_articolo", rec.get("codice_articolo"));
                        double qta_orig = CastUtils.toDouble0(rec.get("qta"));
                        if (qta_orig <= qta_da_togliere) {
                            m.put("qta", qta_orig);
                            qta_da_togliere -= qta_orig;
                        } else {
                            m.put("qta", qta_da_togliere);
                            qta_da_togliere = 0;
                        }
                        sql = "insert into righ_" + tabellaDest + "_lotti set " + DbUtils.prepareSqlFromMap(m);
                        try {
                            DbUtils.tryExecQuery(Db.getConn(), sql);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                        if (qta_da_togliere <= 0) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //chiedo quantità
                //controllo lotti
                Integer id_riga = rid_dest;
                String codice = (String) tab.getValueAt(i, col_art);
                /*
                 10/07/2013
                 * per la trasformazione da prev/ord a DDT prima chiedeva i lotti solo quando la qta ceonfermata era minore dalla quantià
                 * da adesso si mette che se c'è i lotti la richiede sempre
                 */
                if (CastUtils.toString(tab.getValueAt(i, col_lotti)).equalsIgnoreCase("S")
                        && qta > 0 && qtaconf > 0 && (qtaconf != qta || tabellaProvRighe.indexOf("ordi") >= 0)) {
                    //chiedere lotti
                    SwingUtils.showInfoMessage(parent, "Definire i lotti per l'articolo '" + codice + "' qta confermata " + FormatUtils.formatNum0_5Dec(qtaconf) + " su " + FormatUtils.formatNum0_5Dec(qta));

                    ArrayList<String> alotti = null;
                    ArrayList<Double> alottiqta = null;

//                    JDialogLotti dialog = new JDialogLotti(main.getPadreFrame(), true, true);
                    JDialogLotti dialog = new JDialogLotti(main.getPadreFrame(), true, false);
                    dialog.setLocationRelativeTo(null);
                    dialog.init(tipo_mov, CastUtils.toDouble0(qtaconf), codice, tabd + "_lotti", id_riga, null, codice_deposito, true);
                    dialog.setVisible(true);
                    System.out.println("lotti ok");

                    //aggiungo lotti in descrizione riga
                    System.out.println("id_riga : " + id_riga);
                    alotti = dialog.getLotti();
                    alottiqta = dialog.getLottiQta();
                    int ilotti = 0;
                    for (String m : alotti) {
//                        toadd += "\nLotto: " + m;
                        toadd += "\nLotto: " + m + " (" + FormatUtils.formatNum0_5Dec(alottiqta.get(ilotti)) + ")";
                        ilotti++;
                    }
                    frmNuovRigaDescrizioneMultiRigaNew.toglieLotti(tabd, id_riga);
                    sql = "update " + tabd + " set descrizione = CONCAT(descrizione,'" + StringUtils.replace(toadd, "'", "\\'") + "') where id = " + id_riga;
                    System.out.println("sql = " + sql);
                    Db.executeSql(sql, true);

                }

            }
        }
    }

    public static void deactivateComponent(Component comp) {
        if (comp instanceof JScrollPane) {
            JScrollPane pane = (JScrollPane) comp;
            for (Component compPane : pane.getComponents()) {
                deactivateComponent(compPane);
            }
        } else if (comp instanceof JViewport) {
            JViewport viewPort = (JViewport) comp;
            for (Component compPane : viewPort.getComponents()) {
                deactivateComponent(compPane);
            }
        } else if (comp instanceof JTabbedPane) {
            JTabbedPane tpane = (JTabbedPane) comp;
            for (Component compPane : tpane.getComponents()) {
                deactivateComponent(compPane);
            }
        } else if (comp instanceof JPanel) {
            JPanel panel = (JPanel) comp;
            for (Component compPane : panel.getComponents()) {
                deactivateComponent(compPane);
            }
        } else {
            comp.setEnabled(false);
        }
    }

    public static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5"); //or "SHA-1"
            md.update(s.getBytes());
            BigInteger hash = new BigInteger(1, md.digest());
            String md5 = hash.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<ArrayList> checkBarcodeList(final int idDocumento, final String tipoDocumento, final boolean acquisto) {
        try {
            String table = "";
            if (tipoDocumento.equals(Db.TIPO_DOCUMENTO_DDT)) {
                table = "righ_ddt";
                table = acquisto ? table + "_acquisto" : table;
            } else if (tipoDocumento.equals(Db.TIPO_DOCUMENTO_FATTURA)) {
                table = "righ_fatt";
                table = acquisto ? table + "_acquisto" : table;
            } else if (tipoDocumento.equals(Db.TIPO_DOCUMENTO_ORDINE)) {
                table = "righ_ordi";
                table = acquisto ? table + "_acquisto" : table;
            } else if (tipoDocumento.equals(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
                table = "righ_ordi_acquisto";
            } else if (tipoDocumento.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
                table = "righ_ddt_acquisto";
            } else if (tipoDocumento.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
                table = "righ_fatt_acquisto";
            }

            String campi = "";
            if (main.fileIni.getValueBoolean("barcode", "stampa_cod_articolo", true)) {
                campi += "art.codice,";
            }
            if (main.fileIni.getValueBoolean("barcode", "stampa_prezzo_articolo", true)) {
                if (main.fileIni.getValueBoolean("barcode", "iva_inclusa", true)) {
                    campi += "rig.prezzo_ivato as prezzo,";
                } else {
                    campi += "rig.prezzo as prezzo,";
                }
            }

            String sql = "SELECT " + campi + " art.codice_a_barre as barcode, SUM(rig.quantita) as quantita FROM " + table + " rig LEFT JOIN articoli art ON rig.codice_articolo = art.codice WHERE rig.id_padre = " + Db.pc(idDocumento, Types.INTEGER) + " AND art.codice_a_barre != '' GROUP BY art.codice_a_barre";
            ResultSet codici = Db.openResultSet(Db.getConn(), sql);

            int conta = 0;
            ArrayList<ArrayList> res = new ArrayList<ArrayList>();
            while (codici.next()) {
                String barcode = codici.getString("barcode");
                Double qtaDb = codici.getDouble("quantita");

                String codiceArticolo = "";

                if (!main.fileIni.getValueBoolean("barcode", "per_quantita", false)) {
                    if (main.fileIni.getValueBoolean("barcode", "stampa_qta_articolo", true)) {
                        String qta = String.valueOf(CastUtils.toInteger(codici.getDouble("quantita")));
                        codiceArticolo = qta + " x ";
                    }
                }

                if (main.fileIni.getValueBoolean("barcode", "stampa_cod_articolo", true)) {
                    codiceArticolo += codici.getString("codice");
                }

                if (main.fileIni.getValueBoolean("barcode", "stampa_prezzo_articolo", true)) {
                    String prezzo = "€ " + String.valueOf(codici.getDouble("prezzo"));
                    codiceArticolo += codiceArticolo.equals("") ? prezzo : " - " + prezzo;
                }

                ArrayList temp = new ArrayList();

                temp.add(codiceArticolo);
                temp.add(barcode);
                temp.add(qtaDb);

                res.add(temp);
            }

            return res.size() > 0 ? res : null;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void msgNew(final JInternalFrame frame, final tnxDbPanel dati, final IFunction fun, final JComponent compfocus, final String messaggio) {
        if (dati.isOnSomeRecord == false) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    long ms = System.currentTimeMillis();
                    while (!frame.isVisible() || System.currentTimeMillis() - ms > 5000) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception ex) {
                        }
                    }
                    if (frame.isVisible()) {
                        SwingUtils.inEdt(new Runnable() {
                            public void run() {
                                JComponent compfocus2 = null;
                                javax.swing.JOptionPane.showMessageDialog(frame, messaggio, "Attenzione", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                                if (fun != null) {
                                    fun.run();
                                } else //cerco l'action del pulsante new
                                 if (dati.butNew != null) {
                                        ActionListener[] lists = dati.butNew.getActionListeners();
                                        for (ActionListener l : lists) {
                                            l.actionPerformed(null);
                                        }
                                    }
                                if (compfocus == null) {
                                    //cerco id o codice
                                    Component[] comps = dati.getComponents();
                                    for (Component c : comps) {
                                        if (c instanceof tnxTextField) {
                                            String campo = ((tnxTextField) c).getDbNomeCampo();
                                            if (campo.equalsIgnoreCase("data")
                                                    || campo.equalsIgnoreCase("codice")
                                                    || campo.equalsIgnoreCase("id")
                                                    || campo.equalsIgnoreCase("codice_articolo")
                                                    || campo.equalsIgnoreCase("abi")
                                                    || campo.equalsIgnoreCase("descrizione")) {
                                                if (((tnxTextField) c).isEditable() && ((tnxTextField) c).isEnabled()) {
                                                    compfocus2 = (JComponent) c;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    compfocus2 = compfocus;
                                }
                                if (compfocus2 != null) {
                                    compfocus2.requestFocus();
                                    FxUtils.fadeBackground(compfocus2, Color.red);
                                }
                            }
                        });
                    }
                }
            };
            t.start();
        }

    }

    static public TableCellRenderer getNumber0_5Renderer() {
        if (numberRenderer0_5 == null) {
            if (!isSubstance()) {
                numberRenderer0_5 = new DefaultTableCellRenderer() {
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (comp instanceof JLabel) {
                            JLabel lab = (JLabel) comp;
                            lab.setForeground(UIManager.getColor("Text.foreground"));
                            Color csel = UIManager.getColor("Table.selectionForeground");
                            Color cnosel = UIManager.getColor("Table.foreground");
                            Color c = null;
                            if (isSelected) {
                                c = csel;
                            } else {
                                c = cnosel;
                            }
                            lab.setForeground(c);
                            if (value != null) {
                                try {
                                    //double d = (Double.valueOf(value.toString())).doubleValue();
                                    double d = CastUtils.toDouble0All(value);
                                    NumberFormat form = DecimalFormat.getInstance(Locale.ITALIAN);
                                    form.setGroupingUsed(true);
                                    form.setMaximumFractionDigits(5);
                                    form.setMinimumFractionDigits(0);
                                    lab.setHorizontalAlignment(SwingConstants.RIGHT);
                                    lab.setText(form.format(d));
                                    if (d < 0) {
                                        if ((c.getRed() + c.getGreen() + c.getBlue()) / 3 > 125) {
                                            lab.setForeground(new Color(255, 180, 180));
                                        } else {
                                            lab.setForeground(Color.RED);
                                        }
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                lab.setText("");
                            }
                            return lab;
                        }
                        return comp;
                    }
                };
            } else {
                numberRenderer0_5 = new org.jvnet.substance.SubstanceDefaultTableCellRenderer() {
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (comp instanceof JLabel) {
                            JLabel lab = (JLabel) comp;
                            if (value != null) {
                                try {
                                    double d = (Double.valueOf(value.toString())).doubleValue();
                                    NumberFormat form = DecimalFormat.getInstance(Locale.ITALIAN);
                                    form.setGroupingUsed(true);
                                    form.setMaximumFractionDigits(5);
                                    form.setMinimumFractionDigits(0);
                                    lab.setHorizontalAlignment(SwingConstants.RIGHT);
                                    lab.setText(form.format(d));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                lab.setText("");
                            }
                            return lab;
                        }
                        return comp;
                    }
                };

            }
        }
        return numberRenderer0_5;
    }

    static public boolean isSubstance() {
        if (substance == null) {
            if (UIManager.getLookAndFeel().getName().toLowerCase().indexOf("substance") >= 0) {
                substance = true;
            } else {
                substance = false;
            }
        }
        return substance;
    }

    public static void aggiornaRiferimentoDocumenti(String tipo_doc, Integer id) {
        if (id == null) {
            System.out.println("non aggiornaRiferimentoDocumenti perchè id:" + id);
            return;
        }
        //!!!!! riportare in invoicexUtil e farlo anche per ddt collegati, poi su ddt farlo per le fatture collegate e su ordini farlo per ddt e fatture collegate        
        String sql = null;
        String nome_tab_righ_origine = Db.getNomeTabR(tipo_doc);
        boolean acquisto = nome_tab_righ_origine.endsWith("_acquisto") ? true : false;
        String suffisso = acquisto ? "_acquisto" : "";
        if (tipo_doc.equals(Db.TIPO_DOCUMENTO_DDT) || tipo_doc.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)
                || tipo_doc.equals(Db.TIPO_DOCUMENTO_FATTURA) || tipo_doc.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            try {
                List<Map> list1 = DbUtils.getListMap(Db.getConn(), "select da_ordi from " + nome_tab_righ_origine + " where id_padre = " + id + " and da_ordi is not null group by da_ordi");
                for (Map m1 : list1) {
                    //"select anno, numero, serie, id, id_padre, da_ordi from righ_fatt where da_ordi = 22"
                    System.out.println("devo aggiornare l'ordine id:" + m1.get("da_ordi"));
                    String convertito = "";
                    List<Map> list2 = DbUtils.getListMap(Db.getConn(), "select anno, numero, serie, id, id_padre, da_ordi from righ_fatt" + suffisso + " where da_ordi = " + m1.get("da_ordi") + " group by id_padre");
                    for (Map m2 : list2) {
                        System.out.println("l'ordine id:" + m1.get("da_ordi") + " è in questa fattura:" + m2.get("anno") + "/" + m2.get("numero") + "/" + m2.get("serie") + " id:" + m2.get("id_padre"));
                        convertito += (convertito.length() > 0 ? "\n" : "") + InvoicexUtil.getNumeroDaId(acquisto ? Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA : Db.TIPO_DOCUMENTO_FATTURA, CastUtils.toInteger(m2.get("id_padre")), true);
                    }
                    list2 = DbUtils.getListMap(Db.getConn(), "select anno, numero, serie, id, id_padre, da_ordi from righ_ddt" + suffisso + " where da_ordi = " + m1.get("da_ordi") + " group by id_padre");
                    for (Map m2 : list2) {
                        System.out.println("l'ordine id:" + m1.get("da_ordi") + " è in questo ddt:" + m2.get("anno") + "/" + m2.get("numero") + "/" + m2.get("serie") + " id:" + m2.get("id_padre"));
                        convertito += (convertito.length() > 0 ? "\n" : "") + InvoicexUtil.getNumeroDaId(acquisto ? Db.TIPO_DOCUMENTO_DDT_ACQUISTO : Db.TIPO_DOCUMENTO_DDT, CastUtils.toInteger(m2.get("id_padre")), true);
                    }
                    sql = "update test_ordi" + suffisso + " t";
                    sql += " set convertito = " + Db.pc(convertito, "VARCHAR");
                    sql += " where id = " + m1.get("da_ordi");
                    System.out.println("sql = " + sql);
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (tipo_doc.equals(Db.TIPO_DOCUMENTO_FATTURA) || tipo_doc.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            try {
                List<Map> list1 = DbUtils.getListMap(Db.getConn(), "select da_ddt from " + nome_tab_righ_origine + " where id_padre = " + id + " and da_ddt is not null group by da_ddt");
                for (Map m1 : list1) {
                    System.out.println("devo aggiornare il ddt id:" + m1.get("da_ddt"));
                    String convertito = "";
                    List<Map> list2 = DbUtils.getListMap(Db.getConn(), "select anno, numero, serie, id, id_padre, da_ddt from " + nome_tab_righ_origine + " where da_ddt = " + m1.get("da_ddt") + " group by id_padre");
                    for (Map m2 : list2) {
                        System.out.println("il ddt id:" + m1.get("da_ddt") + " è in questa fattura:" + m2.get("anno") + "/" + m2.get("numero") + "/" + m2.get("serie") + " id:" + m2.get("id_padre"));
                        convertito += (convertito.length() > 0 ? "\n" : "") + InvoicexUtil.getNumeroDaId(tipo_doc, CastUtils.toInteger(m2.get("id_padre")), false);
                    }
                    sql = "update test_ddt" + suffisso + " t";
                    sql += " set convertito = " + Db.pc(convertito, "VARCHAR");
                    sql += " where id = " + m1.get("da_ddt");
                    System.out.println("sql = " + sql);
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getTipoNumerazione() {
        if (tipoNumerazione == null) {
            try {
                tipoNumerazione = CastUtils.toInteger0(DbUtils.getObject(Db.getConn(), "select tipo_numerazione from dati_azienda"));
            } catch (Exception ex) {
                tipoNumerazione = 0;
                ex.printStackTrace();
            }
        }
        return tipoNumerazione;
    }

    public static boolean isSceltaTipoNumerazioneEseguita() {
        try {
            int eseguito = CastUtils.toInteger0(DbUtils.getObject(Db.getConn(), "select tipo_numerazione_confermata from dati_azienda limit 1"));
            if (eseguito == 1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean generareMovimenti(int tipo_fattura, Component parent) {
        boolean genera = false;

        int generazione_movimenti = Integer.parseInt(main.fileIni.getValue("pref", "generazione_movimenti", "0"));

        if (main.isBatch) {
            generazione_movimenti = Integer.parseInt(main.fileIni.getValue("pref", "generazione_movimenti", "1"));
        }

        if (tipo_fattura == dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO) {
            if (SwingUtils.showYesNoMessage(parent, "Vuoi generare i movimenti di carico magazzino ?")) {
                genera = true;
            }
        } else if (generazione_movimenti == 0) {
            //standard genera sempre
            //su proforma no..
            if (tipo_fattura == dbFattura.TIPO_FATTURA_IMMEDIATA || tipo_fattura == dbFattura.TIPO_FATTURA_ACCOMPAGNATORIA) {
                genera = true;
            }
            if (main.getPersonalContain("movimenti_su_proforma") && tipo_fattura == dbFattura.TIPO_FATTURA_PROFORMA) {
                genera = true;
            }
        } else if (generazione_movimenti == 1) {
            //genera solo per accompagnatoria
            if (tipo_fattura == dbFattura.TIPO_FATTURA_ACCOMPAGNATORIA) {
                genera = true;
            }
            if (main.getPersonalContain("movimenti_su_proforma") && tipo_fattura == dbFattura.TIPO_FATTURA_PROFORMA) {
                genera = true;
            }
        } else {
            //genera solo per accompagnatoria ma chiede per immediata
            if (tipo_fattura == dbFattura.TIPO_FATTURA_IMMEDIATA) {
                if (SwingUtils.showYesNoMessage(parent, "Vuoi generare i movimenti di magazzino ?")) {
                    genera = true;
                }
            } else if (tipo_fattura == dbFattura.TIPO_FATTURA_ACCOMPAGNATORIA) {
                genera = true;
            }
            if (main.getPersonalContain("movimenti_su_proforma") && tipo_fattura == dbFattura.TIPO_FATTURA_PROFORMA) {
                genera = true;
            }
        }

        return genera;
    }

    static public Runnable aggiornaGiacenzeArticoliCustom = null;

    synchronized public static void aggiornaGiacenzeArticoli() {
        if (aggiornaGiacenzeArticoliCustom != null) {
            System.out.println("aggiornaGiacenzeArticoli eseguo custom : " + aggiornaGiacenzeArticoliCustom);
            aggiornaGiacenzeArticoliCustom.run();
        } else {
            MicroBench mb = new MicroBench(true);
            //lock tabelle
            Connection conn = null;
            Statement stat = null;
            try {
                conn = Db.getConnection();
                stat = conn.createStatement();
                String sql = "LOCK TABLES articoli WRITE, movimenti_magazzino WRITE, tipi_causali_magazzino READ, depositi as dep READ, categorie_articoli as cat READ, sottocategorie_articoli as scat READ";
                System.out.println("sql = " + sql);
                stat.execute(sql);

                Magazzino m = new Magazzino(conn);

                List<Giacenza> giacenze = null;
                if (main.disp_articoli_da_deposito == null) {
                    giacenze = m.getGiacenza(null);
                } else {
                    giacenze = m.getGiacenza(false, null, null, null, false, true, main.disp_articoli_da_deposito);
                }

                //prendo elenco articoli con disponibilita per confronto ed aggiornare solo le diverse
                Map disp_articoli = dbu.getListMapKV(conn, "select ucase(codice) as codice, disponibilita_reale from articoli");

                sql = "";
                int conta = 0;
                DbUtils.debug = false;
                Map<String, Double> giac_per_codice = new HashMap();
                for (Giacenza g : giacenze) {
                    if (g.getCodice_articolo().equalsIgnoreCase("SL0185")) {
                        System.out.println("debug");
                    }

                    giac_per_codice.put(cu.toString(g.getCodice_articolo()).toUpperCase(), g.getGiacenza());

                    double disp_art = 0;
                    try {
                        disp_art = cu.d0(disp_articoli.get(cu.s(g.getCodice_articolo()).toUpperCase()));
                    } catch (Exception e) {
                    }

                    if (g.getGiacenza() != disp_art) {
                        sql += "update articoli set disponibilita_reale = " + Db.pc(g.getGiacenza(), Types.DOUBLE) + ", disponibilita_reale_ts = NOW() where codice = " + Db.pc(g.getCodice_articolo(), Types.VARCHAR) + ";\n";
                        conta++;
                        System.out.println("articolo " + cu.s(g.getCodice_articolo()).toUpperCase() + " disp art.:" + disp_art + " giac.:" + g.getGiacenza());
                    } else {
                        //System.out.println("articolo " + cu.s(g.getCodice_articolo()).toUpperCase() + " disp art.:" + disp_art + " giac.:" + g.getGiacenza());
                    }

                    if (conta == 100) {
                        try {
                            //DbUtils.tryExecQuery(Db.getConn(), sql);
                            stat.execute(sql);
                            System.out.println("sql = " + sql);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        conta = 0;
                        sql = "";
                    }
                }
                if (conta != 100 && StringUtils.isNotBlank(sql)) {
                    try {
                        //DbUtils.tryExecQuery(conn, sql);
                        stat.execute(sql);
                        System.out.println("sql = " + sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //controllo articoli con giacenza diversa da zero
                try {
                    List<Map> elenco = DbUtils.getListMap(conn, "select ucase(codice) as codice, disponibilita_reale from articoli where IFNULL(disponibilita_reale,0) != 0");
                    for (Map rec : elenco) {
                        try {
                            String codice = cu.toString(rec.get("codice"));

                            if (codice.equalsIgnoreCase("SL0185")) {
                                System.out.println("debug");
                            }

                            double giac_db = cu.toDouble0(rec.get("disponibilita_reale"));
                            //se non trovo in giacenze calcolate prima la metto a zero
                            if (!giac_per_codice.keySet().contains(codice)) {
                                System.out.println("!!! giacenza in db: " + giac_db + " ma non trovato nelle giacenze calcolate quindi metto a zero");
                                sql = "update articoli set disponibilita_reale = 0, disponibilita_reale_ts = NOW() where codice = " + Db.pc(codice, Types.VARCHAR);
                                //DbUtils.tryExecQuery(Db.getConn(), sql);
                                stat.execute(sql);
                                System.out.println("sql = " + sql);
                            } else {
                                //se la trovo la confronto, se diversa la reimposto
                                Double giac_calc = giac_per_codice.get(codice);
                                if (giac_db != giac_calc) {
                                    System.out.println("!!! giacenza in db: " + giac_db + " diversa da calcolata: " + giac_calc);
                                    sql = "update articoli set disponibilita_reale = " + Db.pc(giac_calc, Types.DOUBLE) + ", disponibilita_reale_ts = NOW() where codice = " + Db.pc(codice, Types.VARCHAR);
                                    //DbUtils.tryExecQuery(Db.getConn(), sql);
                                    stat.execute(sql);
                                    System.out.println("sql = " + sql);
                                }
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mb.out("locale");
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtils.showExceptionMessage(main.getPadreFrame(), ex);
            } finally {
                if (stat != null) {
                    try {
                        String sql = "UNLOCK TABLES";
                        stat.execute(sql);
                        System.out.println("sql:" + sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        stat.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        conn.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        /*
         //via wsd
         TrustManager[] trustAllCerts = new TrustManager[]{
         new X509TrustManager() {
         public java.security.cert.X509Certificate[] getAcceptedIssuers() {
         return null;
         }

         public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
         }

         public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
         }

         public boolean isClientTrusted(X509Certificate[] xcs) {
         return true;
         }

         public boolean isServerTrusted(X509Certificate[] xcs) {
         return true;
         }
         }
         };
         try {
         SSLContext sc = SSLContext.getInstance("SSL");
         sc.init(null, trustAllCerts, new java.security.SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
         } catch (Exception e) {
         System.out.println(e);
         }


         URLCodec u = new URLCodec();
         String url = getUrlWsd();
         url += "&f=aggiorna_disponibilita_reale";
         System.out.println("url = " + url);

         try {
         HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
         conn.setDoOutput(true);
         conn.setRequestMethod("POST");
         String post = "server=" + u.encode(main.fileIni.getValue("db", "server"));
         post += "&user=" + u.encode(main.fileIni.getValue("db", "user"));
         post += "&pass=" + u.encode(main.fileIni.getValueCifrato("db", "pwd"));
         post += "&database=" + u.encode(main.fileIni.getValue("db", "nome_database"));
         post += "&ssh=" + u.encode(main.fileIni.getValue("db", "ssh"));
         post += "&ssh_server=" + u.encode(main.fileIni.getValue("db", "ssh_hostname"));
         //conversione da java a php via json
         JSONArray list = new JSONArray();
         for (Giacenza g : giacenze) {
         //                JSONObject jo = new JSONObject();
         HashMap jo = new HashMap();
         jo.put("c", g.getCodice_articolo());
         jo.put("q", g.getGiacenza());
         list.add(jo);
         }
         System.out.println("list = " + list);
         System.out.println("list encode = " + u.encode(list.toString()));
         post += "&lista=" + u.encode(list.toString());

         conn.setRequestProperty("Content-Length", "" + post.length());

         OutputStreamWriter outputWriter = new OutputStreamWriter(conn.getOutputStream());
         outputWriter.write(post.toString());
         outputWriter.flush();
         outputWriter.close();

         int retcode = conn.getResponseCode();
         long lastm = conn.getLastModified();
         int size = conn.getContentLength();
         System.out.println(conn.getContentType());
         String xinvoicex = conn.getHeaderField("X-Invoicex");
         System.out.println("xinvoicex = " + xinvoicex);

         if (retcode != 200) {
         System.out.println("getURL: errore retcode:" + retcode + " resp:" + conn.getResponseMessage());
         return;
         }
         InputStream is = new BufferedInputStream(conn.getInputStream());
         int readed = 0;
         int read = 0;
         byte[] buff = new byte[10000];
         String out = "";
         boolean errors = false;
         while ((read = is.read(buff)) > 0) {
         String tmp = new String(buff, 0, read);
         System.out.println("tmp = " + tmp);
         out += tmp;
         readed += read;
         try {
         String[] tmps = StringUtils.split(tmp, '\n');
         tmp = tmps[0];
         if (tmp.startsWith("p:")) {
         final int progresso = CastUtils.toInteger0(StringUtils.substringAfter(tmp, "p:"));
         SwingUtils.inEdt(new Runnable() {
         public void run() {
         System.out.println("aggiornamento in corso " + progresso + "%");
         //                                panelnews_f.link.setText("aggiornamento in corso " + progresso + "%");
         }
         });
         } else {
         mb.out("remoto");
         if (xinvoicex == null) {
         //errore php
         System.out.println("out = " + out);
         String noHTMLString = out.replaceAll("\\<.*?\\>", "");
         System.out.println("noHTMLString = " + noHTMLString);
         SwingUtils.showErrorMessage(main.getPadreWindow(), "Errore durante l'aggiornamento:\n" + noHTMLString);
         } else {
         SwingUtils.showErrorMessage(main.getPadreWindow(), "Errore durante l'aggiornamento:\n" + tmp);
         }

         errors = true;
         }
         } catch (Exception e) {
         e.printStackTrace();
         }
         }
         is.close();
         mb.out("remoto");

         if (!errors) {
         SwingUtils.inEdt(new Runnable() {
         public void run() {
         System.out.println("aggiornamento completato");
         //                        panelnews_f.link.setText("aggiornamento completato");
         }
         });
         }
         } catch (Exception e) {
         e.printStackTrace();
         }
         */
    }

    public static void aggiornaPrezziNettiUnitari(String tabrighe, String tabtest) {
        aggiornaPrezziNettiUnitari(Db.getConn(), tabrighe, tabtest);
    }

    public static void aggiornaPrezziNettiUnitari(Connection conn, String tabrighe, String tabtest) {
        aggiornaPrezziNettiUnitari(conn, tabrighe, tabtest, null);
    }

    public static void aggiornaPrezziNettiUnitari(String tabrighe, String tabtest, Integer id) {
        aggiornaPrezziNettiUnitari(Db.getConn(), tabrighe, tabtest, id);
    }

    public static void aggiornaPrezziNettiUnitari(Connection conn, String tabrighe, String tabtest, Integer id) {
        if (conn == null) {
            conn = Db.getConn();
        }

        String sql = null;

        if (!Sync.isActive()) {
            sql = "update " + tabrighe + " r join " + tabtest + " t on r.id_padre = t.id "
                    + "set r.prezzo_netto_unitario = IFNULL(r.prezzo,0) * ((100 - IFNULL(r.sconto1,0)) / 100) * ((100 - IFNULL(r.sconto2,0)) / 100) * ((100 - IFNULL(t.sconto1,0)) / 100) * ((100 - IFNULL(t.sconto2,0)) / 100) * ((100 - IFNULL(t.sconto3,0)) / 100)";
            if (id != null) {
                sql += " where t.id = " + id;
            }
        } else {
            if (id == null) {
                SwingUtils.showErrorMessage(InvoicexUtil.getActiveJInternalFrame(), "Sync: aggiornare chiamata aggiornaPrezziNettiUnitari senza id");
                return;
            }
            try {
                Map m = dbu.getListMap(conn, "select sconto1, sconto2, sconto3 from " + tabtest + " where id = " + id).get(0);
                sql = "update " + tabrighe + " "
                        + " set prezzo_netto_unitario = IFNULL(prezzo,0) * ((100 - IFNULL(sconto1,0)) / 100) * ((100 - IFNULL(sconto2,0)) / 100) * ((100 - " + cu.d0(m.get("sconto1")) + ") / 100) * ((100 - " + cu.d0(m.get("sconto2")) + ") / 100) * ((100 - " + cu.d0(m.get("sconto3")) + ") / 100)"
                        + " where id_padre = " + id;
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(InvoicexUtil.getActiveJInternalFrame(), e);
            }
        }
        System.out.println("sql aggiornaPrezziNettiUnitari : " + sql);
        try {
            DbUtils.tryExecQuery(conn, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!Sync.isActive()) {
            sql = "update " + tabrighe + " r join " + tabtest + " t on r.id_padre = t.id "
                    + "set r.prezzo_ivato_netto_unitario = IFNULL(r.prezzo_ivato,0) * ((100 - IFNULL(r.sconto1,0)) / 100) * ((100 - IFNULL(r.sconto2,0)) / 100) * ((100 - IFNULL(t.sconto1,0)) / 100) * ((100 - IFNULL(t.sconto2,0)) / 100) * ((100 - IFNULL(t.sconto3,0)) / 100)";
            if (id != null) {
                sql += " where t.id = " + id;
            }
        } else {
            try {
                Map m = dbu.getListMap(conn, "select sconto1, sconto2, sconto3 from " + tabtest + " where id = " + id).get(0);
                sql = "update " + tabrighe + " "
                        + " set prezzo_ivato_netto_unitario = IFNULL(prezzo_ivato,0) * ((100 - IFNULL(sconto1,0)) / 100) * ((100 - IFNULL(sconto2,0)) / 100) * ((100 - " + cu.d0(m.get("sconto1")) + ") / 100) * ((100 - " + cu.d0(m.get("sconto2")) + ") / 100) * ((100 - " + cu.d0(m.get("sconto3")) + ") / 100)"
                        + " where id_padre = " + id;
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(InvoicexUtil.getActiveJInternalFrame(), e);
            }
        }
        System.out.println("sql aggiornaPrezziNettiUnitari : " + sql);
        try {
            DbUtils.tryExecQuery(conn, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!Sync.isActive()) {
            sql = "update " + tabrighe + " r join " + tabtest + " t on r.id_padre = t.id "
                    + "set r.prezzo_netto_totale = r.prezzo_netto_unitario * r.quantita";
            if (id != null) {
                sql += " where t.id = " + id;
            }
        } else {
            try {
                sql = "update " + tabrighe + " "
                        + " set prezzo_netto_totale = prezzo_netto_unitario * quantita"
                        + " where id_padre = " + id;
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(InvoicexUtil.getActiveJInternalFrame(), e);
            }
        }
        System.out.println("sql aggiornaPrezziNettiUnitari : " + sql);
        try {
            DbUtils.tryExecQuery(conn, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //per prezzi ivati
        if (!Sync.isActive()) {
            sql = "update " + tabrighe + " r join " + tabtest + " t on r.id_padre = t.id "
                    + "set r.prezzo_ivato_netto_totale = r.prezzo_ivato_netto_unitario * r.quantita";
            if (id != null) {
                sql += " where t.id = " + id;
            }
        } else {
            try {
                sql = "update " + tabrighe + " "
                        + " set prezzo_ivato_netto_totale = prezzo_ivato_netto_unitario * quantita"
                        + " where id_padre = " + id;
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(InvoicexUtil.getActiveJInternalFrame(), e);
            }
        }
        System.out.println("sql aggiornaPrezziNettiUnitari : " + sql);
        try {
            DbUtils.tryExecQuery(conn, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void aggiornaPrezziNettiTotali(String tabrighe, String tabtest) {
        String sql = "update " + tabrighe + " r join " + tabtest + " t on r.id_padre = t.id "
                + "set r.totale_imponibile_netto = (prezzo_netto_unitario * quantita) * t." + getCampo(tabtest, "totale_imponibile") + " / t.totale_imponibile_pre_sconto";
        System.out.println("sql aggiornaPrezziNettiTotali : " + sql);
        try {
            DbUtils.tryExecQuery(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sql = "update " + tabrighe + " r join " + tabtest + " t on r.id_padre = t.id "
                + "set r.totale_ivato_netto = (prezzo_ivato_netto_unitario * quantita) * t." + getCampo(tabtest, "totale") + " / t.totale_ivato_pre_sconto";
        System.out.println("sql aggiornaPrezziNettiTotali : " + sql);
        try {
            DbUtils.tryExecQuery(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sql = "update " + tabrighe + " r join " + tabtest + " t on r.id_padre = t.id "
                + "set r.totale_iva_netto = ((prezzo_ivato_netto_unitario - prezzo_netto_unitario) * quantita) * (t." + getCampo(tabtest, "totale") + " - t." + getCampo(tabtest, "totale_imponibile") + ") / (t.totale_ivato_pre_sconto - t.totale_imponibile_pre_sconto)";
        System.out.println("sql aggiornaPrezziNettiTotali : " + sql);
        try {
            DbUtils.tryExecQuery(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sql = "update " + tabrighe + " r "
                + " set r.totale_iva_netto = IFNULL(totale_iva_netto, 0), r.totale_ivato_netto = IFNULL(totale_ivato_netto, 0), r.totale_imponibile_netto = IFNULL(totale_imponibile_netto, 0)";
        System.out.println("sql aggiornaPrezziNettiTotali : " + sql);
        try {
            DbUtils.tryExecQuery(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkSqlBlob(String sql) {
        String key = "insert into dati_azienda values (";
        if (!sql.startsWith(key)) {
            return false;
        }
        Map<String, String> fields = new HashMap<String, String>();
        ArrayList<String> field_list = new ArrayList();
        ArrayList<String> field_list_apici = new ArrayList();
        StringBuffer sb = new StringBuffer();
        sb = new StringBuffer();
        boolean inizio_campo = true;
        boolean inizio_apice = false;
        char oldc = (char) -1;
        String nuovasql = key;
        //analizzo la query carattere per carattere per splittare i campi
        for (int i = key.length(); i < sql.length(); i++) {
//            if (i == 784) {
//                System.out.println("stop");
//            }
            char c = sql.charAt(i);
//            System.out.println("colonna: " + (i+1) + " carattere:" + c + " oldc:" + oldc + " sb:" + sb.toString() + " inizio_campo:" + inizio_campo + " inizio_apice:" + inizio_apice);
            if (c == '\'' && !inizio_apice && inizio_campo) {
                inizio_apice = true;
                inizio_campo = false;
            } else if ((c == '\'' && inizio_apice && oldc != '\\' && !inizio_campo)
                    || (c == ',' && !inizio_apice) || i == (sql.length() - 2)) {
                //se arrivo qui per fine apice vado a cerca la prima virgola per spostare l'offset
                if (c == '\'' && inizio_apice && oldc != '\\' && !inizio_campo) {
                    for (int i2 = i; i2 < sql.length(); i2++) {
                        char c2 = sql.charAt(i2);
                        if (c2 == ',') {
                            i = i2;
                            break;
                        }
                    }
                }
                if (inizio_apice) {
                    field_list_apici.add("'" + sb.toString() + "'");
                } else {
                    field_list_apici.add(sb.toString());
                }
                field_list.add(sb.toString());
                inizio_apice = false;
                inizio_campo = true;
//                System.out.println("AGGIUNTO FIELD: " + sb.toString());
                sb.setLength(0);
                if (i == (sql.length() - 2)) {
                    break;
                }
            } else {
                sb.append(c);
            }
            oldc = c;
//            System.out.println("colonna: " + (i+1) + " carattere:" + c + " oldc:" + oldc + " sb:" + sb.toString() + " inizio_campo:" + inizio_campo + " inizio_apice:" + inizio_apice);
        }

        //prendo i nomi dei campi per capire il field che campo è
        List<Map> listfieldtab = null;
        try {
            listfieldtab = DbUtils.getListMap(Db.getConn(), "describe dati_azienda");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //debug
        System.out.println("listfieldtab.size: " + listfieldtab.size());
        System.out.println("field_list_apici.size: " + field_list_apici.size());
        System.out.println("----------");
        int i = 0;
        for (Map m : listfieldtab) {
            System.out.println("listfieldtab " + i + " : " + m);
            i++;
        }
        System.out.println("----------");
        i = 0;
        for (String s : field_list_apici) {
            System.out.println("field_list_apici " + i + " : " + StringUtils.abbreviate(s, 50));
            i++;
        }
        System.out.println("----------");

        //inserisco la riga di data azienda
        for (int i2 = 0; i2 < listfieldtab.size(); i2++) {
            String field = field_list_apici.get(i2);
            String fieldname = cu.toString(listfieldtab.get(i2).get("Field"));
            System.out.println("fieldname: " + fieldname + " value:" + StringUtils.abbreviate(field, 100));
            String virgola = ",";
            if (i2 == 0) {
                virgola = "";
            }
            if (field.startsWith("0x")) {
                nuovasql += virgola + "null";
            } else {
                nuovasql += virgola + field;
            }
        }
        nuovasql += ")";
        System.out.println("nuovasql:" + nuovasql);
        try {
            DbUtils.tryExecQuery(Db.getConn(), nuovasql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //salvo i loghi blob
        for (int i3 = 0; i3 < listfieldtab.size(); i3++) {
            String field = field_list.get(i3);
            String fieldname = cu.toString(listfieldtab.get(i3).get("Field"));
            System.out.println("fieldname: " + fieldname + " value:" + StringUtils.abbreviate(field, 100));
            if (field.startsWith("0x") && field.length() > 2) {
                int limit = 1024 * 512;
                String field2 = field.substring(2, field.length());
                int quanti = (field2.length() / limit) + 1;
                sql = "update dati_azienda set " + fieldname + " = null";
                try {
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                    for (int iq = 0; iq < quanti; iq++) {
                        String parte = field2.substring(iq * limit, Math.min((iq * limit) + limit, field2.length()));
                        sql = "update dati_azienda set " + fieldname + " = CONCAT(IFNULL(" + fieldname + ",''), 0x" + parte + ")";
                        DbUtils.debug = false;
                        DbUtils.tryExecQuery(Db.getConn(), sql);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    static public MyAbstractListIntelliHints getArticoloIntelliHints(final JTextField articolo, final JComponent frame, final AtomicReference articolo_selezionato, final DelayedExecutor delay_aggiornamento, final JComponent next) {
        return new MyAbstractListIntelliHints(articolo) {
            String current_search = "";
            Border myborder = new LineBorder(Color.lightGray) {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Color oldColor = g.getColor();
                    g.setColor(Color.lightGray);
                    g.drawLine(x, height - 1, width, height - 1);
                    g.setColor(oldColor);
                }
            };

            @Override
            protected JList createList() {
                final JList list = new JList() {
                    @Override
                    public int getVisibleRowCount() {
                        int size = getModel().getSize();
                        System.out.println("createList getVisibleRowCount " + size);
                        return size < super.getVisibleRowCount() ? size : super.getVisibleRowCount();
                    }
                };

                System.err.println("getArticoloIntelliHints... createList setfixedcellwidth " + (frame.getWidth() - 50));
                list.setFixedCellWidth(frame.getWidth() - 50);

                list.setCellRenderer(new DefaultListCellRenderer() {
                    {
                        setUI(new MultiLineLabelUI());
                    }

                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        String img, tipo;
                        JLabel lab = null;
                        if (value instanceof ArticoloHint) {
                            tipo = ((ArticoloHint) value).toString();
                            lab = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                            MultiLineLabelUI ui = (MultiLineLabelUI) lab.getUI();
                            ui.tohighlight = current_search;
                            lab.setBorder(myborder);
                            System.err.println("getListCellRendererComponent w:" + list.getWidth());
                        } else {
                            lab = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        }
                        return lab;
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                    }
                });
                return list;
            }
            SwingWorker lastw = null;

            public boolean updateHints(Object arg0) {
                if (arg0.toString().trim().length() <= 0) {
                    articolo_selezionato.set(null);
                    setSelezionato(null);
                    if (delay_aggiornamento != null) {
                        delay_aggiornamento.update(this);
                    }
                    return false;
                }

                setListData(new String[]{"... in ricerca ..."});
                current_search = arg0.toString();

                if (lastw != null) {
                    if (!lastw.isDone()) {
                        lastw.cancel(true);
                    }
                }

                SwingWorker w = new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        Connection conn;
                        try {
                            conn = Db.getConn();

                            //Thread.sleep((long) (Math.random() * 5000));
                            //prima cerco in articoli e poi (se non trovo abbastanza) in movimenti magazzino
                            String sql1 = ""
                                    + "SELECT a.codice, a.descrizione, IFNULL(a.codice_a_barre,''), IFNULL(a.codice_fornitore,''), IFNULL(a.codice_fornitore2,''), IFNULL(a.codice_fornitore3,''), IFNULL(a.codice_fornitore4,''), IFNULL(a.codice_fornitore5,''), IFNULL(a.codice_fornitore6,'') FROM articoli a"
                                    + " where codice like '" + Db.aa(current_search) + "%'"
                                    + " or descrizione like '" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore like '" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore2 like '" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore3 like '" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore4 like '" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore5 like '" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore6 like '" + Db.aa(current_search) + "%'"
                                    + " or codice_a_barre like '" + Db.aa(current_search) + "%'"
                                    + " order by descrizione, codice limit 100";
                            System.out.println("sql ricerca1:" + sql1);
                            ResultSet rs = DbUtils.tryOpenResultSet(conn, sql1);
                            ArrayList pk = new ArrayList();
                            Vector v = new Vector();
                            while (rs.next()) {
                                ArticoloHint art = new ArticoloHint();
                                art.codice = rs.getString(1);
                                art.descrizione = rs.getString(2);
                                art.codice_a_barre = rs.getString(3);
                                art.codice_fornitore = codice_fornitore(rs, current_search);
                                v.add(art);
                                pk.add(art.codice);
                            }
                            DbUtils.close(rs);
                            //poi provo 'per contiene' sempre su articoli
                            sql1 = ""
                                    + "SELECT a.codice, a.descrizione, IFNULL(a.codice_a_barre,''), IFNULL(a.codice_fornitore,''), IFNULL(a.codice_fornitore2,''), IFNULL(a.codice_fornitore3,''), IFNULL(a.codice_fornitore4,''), IFNULL(a.codice_fornitore5,''), IFNULL(a.codice_fornitore6,'') FROM articoli a"
                                    + " where codice like '%" + Db.aa(current_search) + "%'"
                                    + " or descrizione like '%" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore like '%" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore2 like '%" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore3 like '%" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore4 like '%" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore5 like '%" + Db.aa(current_search) + "%'"
                                    + " or codice_fornitore6 like '%" + Db.aa(current_search) + "%'"
                                    + " or codice_a_barre like '%" + Db.aa(current_search) + "%'"
                                    + " order by descrizione, codice limit 100";

                            System.out.println("sql ricerca2:" + sql1);
                            rs = DbUtils.tryOpenResultSet(conn, sql1);
                            while (rs.next()) {
                                if (!pk.contains(rs.getString(1))) {
                                    ArticoloHint art = new ArticoloHint();
                                    art.codice = rs.getString(1);
                                    art.descrizione = rs.getString(2);
                                    art.codice_a_barre = rs.getString(3);
                                    art.codice_fornitore = codice_fornitore(rs, current_search);
                                    v.add(art);
                                    pk.add(art.codice);
                                }
                            }
                            DbUtils.close(rs);
                            //se non trova quasi niente...
                            if (v.size() < 3) {
                                String sql2 = ""
                                        + "SELECT m.articolo, IFNULL(m.matricola,''), IFNULL(m.lotto,'') FROM movimenti_magazzino m"
                                        + " where articolo like '%" + Db.aa(current_search) + "%'"
                                        + " or matricola like '%" + Db.aa(current_search) + "%'"
                                        + " or lotto like '%" + Db.aa(current_search) + "%'"
                                        + " group by m.articolo, m.matricola, m.lotto"
                                        + " order by articolo limit 50";
                                System.out.println("sql2:" + sql2);
                                rs = DbUtils.tryOpenResultSet(conn, sql2);
                                while (rs.next()) {
                                    try {
                                        if (!pk.contains(rs.getString(1))) {
                                            ArticoloHint art = new ArticoloHint();
                                            art.codice = rs.getString(1);
                                            art.matricola = rs.getString(2);
                                            art.lotto = rs.getString(3);
                                            try {
                                                art.descrizione = (String) DbUtils.getObject(Db.getConn(), "select descrizione from articoli where codice = " + Db.pc(art.codice, Types.VARCHAR));
                                            } catch (Exception e) {
                                            }
                                            //art.descrizione = rs.getString(3);
                                            //art.codice_fornitore = codice_fornitore(rs, current_search);
                                            //art.codice_a_barre = rs.getString(5);
                                            v.add(art);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                rs.getStatement().close();
                                rs.close();
                            }
                            //setListData(v);
                            return v;
                        } catch (InterruptedException iex) {
                            return null;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return new String[]{ex.getMessage()};
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            Object get = get();
                            if (get != null) {
                                //System.err.println("set listdata " + DebugFastUtils.dumpAsString(get));
                                if (get instanceof String[]) {
                                    setListData((String[]) get);
                                } else {
                                    setListData((Vector) get);
                                }
                                showHints();
                            }
                        } catch (CancellationException cex) {
                        } catch (Exception ex) {
                            System.err.println("set listdata ex " + ex.getMessage());
                            setListData(new String[]{ex.getMessage()});
                            showHints();
                        }
                    }
                };

                w.execute();
                lastw = w;

                return true;
            }

            @Override
            public void acceptHint(Object arg0) {
                super.acceptHint(arg0);
                try {
                    articolo.setText(((ArticoloHint) arg0).descrizione + " [" + ((ArticoloHint) arg0).codice + "]");
                    articolo_selezionato.set((ArticoloHint) arg0);
                    setSelezionato((ArticoloHint) arg0);
                    if (next != null) {
                        next.requestFocus();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (delay_aggiornamento != null) {
                    delay_aggiornamento.update(this);
                }
            }

            private String codice_fornitore(ResultSet rs, String cosa) {
                String ret = "";
                Integer[] ids = new Integer[]{4, 5, 6, 7, 8, 9};
                for (int i : ids) {
                    try {
                        if (rs.getString(i) != null && rs.getString(i).toLowerCase().indexOf(cosa.toLowerCase()) >= 0) {
                            if (!ret.equals("")) {
                                ret += " - ";
                            }
                            ret += rs.getString(i);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return ret;
            }
        };
    }

    static public enum CliforTipo {
        Tutti,
        Solo_Clienti_Entrambi_Provvisori,
        Solo_Fornitori_Entrambi_Provvisori
    }

    static public MyAbstractListIntelliHints getCliforIntelliHints(final JTextField clifor, final JComponent frame, final AtomicReference clifor_selezionato, final DelayedExecutor delay_aggiornamento, final JComponent next) {
        return getCliforIntelliHints(clifor, frame, clifor_selezionato, delay_aggiornamento, next, CliforTipo.Tutti);
    }

    static public MyAbstractListIntelliHints getCliforIntelliHints(final JTextField clifor, final JComponent frame, final AtomicReference clifor_selezionato, final DelayedExecutor delay_aggiornamento, final JComponent next, final CliforTipo tipo) {
        return new MyAbstractListIntelliHints(clifor) {
            String current_search = "";

            @Override
            protected JList createList() {
                final JList list = super.createList();
                list.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        String img, tipo;
                        tipo = ((ClienteHint) value).toString();
                        JLabel lab = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        String word = current_search;
                        String content = tipo;
                        Color c = null;
                        if (!isSelected) {
                            c = new Color(240, 240, 100);
                        } else {
                            c = new Color(100, 100, 40);
                        }
                        String rgb = Integer.toHexString(c.getRGB());
                        rgb = rgb.substring(2, rgb.length());

                        content = StringUtilsTnx.highlightWord(content, word, "<span style='background-color: " + rgb + "'>", "</span>");

                        if (((ClienteHint) value).obsoleto) {
                            content = "<span style='color: FF0000'>" + content + " (Obsoleto)</span>";
                        }
                        lab.setText("<html>" + content + "</html>");
                        System.out.println(index + ":" + content);
                        return lab;

                    }
                });
                return list;
            }

            public boolean updateHints(Object arg0) {
                if (arg0 != null && arg0.toString().trim().length() <= 0) {
                    clifor_selezionato.set(null);
                    setSelezionato(null);
                    if (delay_aggiornamento != null) {
                        delay_aggiornamento.update(this);
                    }
                    return false;
                }

                SwingUtils.mouse_wait();
                current_search = arg0 != null ? arg0.toString() : "";
                Connection conn;
                try {
                    conn = gestioneFatture.Db.getConn();

                    String sql = ""
                            + "SELECT codice, ragione_sociale, obsoleto FROM clie_forn"
                            + " where (codice like '%" + gestioneFatture.Db.aa(current_search) + "%'"
                            + " or ragione_sociale like '%" + gestioneFatture.Db.aa(current_search) + "%'"
                            + " ) and ragione_sociale != ''";
                    if (tipo == CliforTipo.Solo_Clienti_Entrambi_Provvisori) {
                        sql += " and IFNULL(tipo,'') != 'F'";
                    } else if (tipo == CliforTipo.Solo_Fornitori_Entrambi_Provvisori) {
                        sql += " and IFNULL(tipo,'') != 'C'";
                    }
                    sql += " order by ragione_sociale, codice limit 50";

                    System.out.println("sql ricerca:" + sql);
                    ResultSet rs = DbUtils.tryOpenResultSet(conn, sql);
                    Vector v = new Vector();

                    while (rs.next()) {
                        ClienteHint cliente = new ClienteHint();
                        cliente.codice = rs.getString(1);
                        cliente.ragione_sociale = rs.getString(2);
                        cliente.obsoleto = rs.getBoolean(3);
                        v.add(cliente);
                    }
                    setListData(v);
                    rs.getStatement().close();
                    rs.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                SwingUtils.mouse_def();
                return true;
            }

            @Override
            public void acceptHint(Object arg0) {
                super.acceptHint(arg0);
                try {
                    clifor.setText(((ClienteHint) arg0).ragione_sociale + " [" + ((ClienteHint) arg0).codice + "]");
                    clifor_selezionato.set((ClienteHint) arg0);
                    setSelezionato((ClienteHint) arg0);
                    next.requestFocus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (delay_aggiornamento != null) {
                    delay_aggiornamento.update(this);
                }
            }
        };
    }

    static public MyAbstractListIntelliHints getFornitoriIntelliHints(final JTextField clifor, final JComponent frame, final AtomicReference clifor_selezionato, final DelayedExecutor delay_aggiornamento, final JComponent next) {
        return new MyAbstractListIntelliHints(clifor) {
            String current_search = "";

            @Override
            protected JList createList() {
                final JList list = super.createList();
                list.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        String img, tipo;
                        tipo = ((ClienteHint) value).toString();
                        JLabel lab = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        String word = current_search;
                        String content = tipo;
                        Color c = null;
                        if (!isSelected) {
                            c = new Color(240, 240, 100);
                        } else {
                            c = new Color(100, 100, 40);
                        }
                        String rgb = Integer.toHexString(c.getRGB());
                        rgb = rgb.substring(2, rgb.length());

                        content = StringUtilsTnx.highlightWord(content, word, "<span style='background-color: " + rgb + "'>", "</span>");

                        if (((ClienteHint) value).obsoleto) {
                            content = "<span style='color: FF0000'>" + content + " (Obsoleto)</span>";
                        }
                        lab.setText("<html>" + content + "</html>");
                        System.out.println(index + ":" + content);
                        return lab;
                    }
                });
                return list;
            }

            public boolean updateHints(Object arg0) {
                if (arg0 != null && arg0.toString().trim().length() <= 0) {
                    clifor_selezionato.set(null);
                    setSelezionato(null);
                    if (delay_aggiornamento != null) {
                        delay_aggiornamento.update(this);
                    }
                    return false;
                }

                SwingUtils.mouse_wait();
                current_search = arg0 != null ? arg0.toString() : "";
                Connection conn;
                try {
                    conn = gestioneFatture.Db.getConn();

                    String sql = ""
                            + "SELECT codice, ragione_sociale, obsoleto FROM clie_forn"
                            + " where (codice like '%" + gestioneFatture.Db.aa(current_search) + "%'"
                            + " or ragione_sociale like '%" + gestioneFatture.Db.aa(current_search) + "%'"
                            + " ) and ragione_sociale != ''"
                            + " and (tipo is null or tipo = 'F' or tipo = 'E')"
                            + " order by ragione_sociale, codice limit 50";

                    System.out.println("sql ricerca:" + sql);
                    ResultSet rs = DbUtils.tryOpenResultSet(conn, sql);
                    Vector v = new Vector();

                    while (rs.next()) {
                        ClienteHint cliente = new ClienteHint();
                        cliente.codice = rs.getString(1);
                        cliente.ragione_sociale = rs.getString(2);
                        cliente.obsoleto = rs.getBoolean(3);
                        v.add(cliente);
                    }
                    setListData(v);
                    rs.getStatement().close();
                    rs.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                SwingUtils.mouse_def();
                return true;
            }

            @Override
            public void acceptHint(Object arg0) {
                super.acceptHint(arg0);
                try {
                    clifor.setText(((ClienteHint) arg0).ragione_sociale + " [" + ((ClienteHint) arg0).codice + "]");
                    clifor_selezionato.set((ClienteHint) arg0);
                    setSelezionato((ClienteHint) arg0);
                    next.requestFocus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (delay_aggiornamento != null) {
                    delay_aggiornamento.update(this);
                }
            }
        };
    }

    public static Color getColorePerMarcatura(String colore) {
        if (colore.equalsIgnoreCase("rosso")) {
            return new Color(255, 100, 100);
        } else if (colore.equalsIgnoreCase("rosso2")) {
            return new Color(255, 175, 175);
        } else if (colore.equalsIgnoreCase("giallo")) {
            return new Color(255, 255, 100);
        } else if (colore.equalsIgnoreCase("giallo2")) {
            return new Color(255, 255, 175);
        } else if (colore.equalsIgnoreCase("blu")) {
            return new Color(100, 100, 255);
        } else if (colore.equalsIgnoreCase("blu2")) {
            return new Color(175, 175, 255);
        } else if (colore.equalsIgnoreCase("celeste")) {
            return new Color(100, 255, 255);
        } else if (colore.equalsIgnoreCase("celeste2")) {
            return new Color(175, 255, 255);
        } else if (colore.equalsIgnoreCase("verde")) {
            return new Color(100, 255, 100);
        } else if (colore.equalsIgnoreCase("verde2")) {
            return new Color(175, 255, 175);
        } else if (colore.equalsIgnoreCase("arancione")) {
            return new Color(255, 100, 50);
        } else if (colore.equalsIgnoreCase("arancione2")) {
            return new Color(255, 200, 100);
        }
        return null;
    }

    static public void salvaColoreRiga(String colore, String tab, tnxDbGrid griglia) {
        int[] righe = griglia.getSelectedRows();
        for (int riga : righe) {
            int id = (Integer) griglia.getValueAt(riga, griglia.getColumnByName("id"));
            String query = "UPDATE " + tab + " SET color = " + Db.pc(colore, Types.VARCHAR) + " WHERE id = " + Db.pc(id, Types.INTEGER);
            Db.executeSql(query);
        }
        try {
            JInternalFrame frame = (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, griglia);
            frame.getClass().getDeclaredMethod("dbRefresh").invoke(frame);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static public void creaPdf(String tipo_doc, Integer[] id, boolean apriDirDopoStampa, boolean apriPdf) throws Exception {
        creaPdf(tipo_doc, id, apriDirDopoStampa, apriPdf, null);
    }

    static public void creaPdf(String tipo_doc, Integer[] id, boolean apriDirDopoStampa, boolean apriPdf, JDialogWait dialog) throws Exception {
        Object ret_stampa = null;
        //creo la cartella, se non esistesse!
        File fdDir = new File(main.wd + "tempEmail");
        try {
            fdDir.mkdir();
        } catch (Exception e) {
        }
        try {
            //elimino i precedenti files
            File d = new File(main.wd + "tempEmail");
            Util.deleteFilesFromDir(d);
        } catch (Exception e) {
        }

        boolean wasindeterm = true;
        if (dialog != null) {
            wasindeterm = dialog.progress.isIndeterminate();
            dialog.progress.setMinimum(0);
            dialog.progress.setMaximum(id.length);
            dialog.progress.setIndeterminate(false);
        }

        if (tipo_doc.equals(Db.TIPO_DOCUMENTO_ORDINE) || tipo_doc.equals(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
            boolean acquisto = false;
            if (tipo_doc.equals((Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO))) {
                acquisto = true;
            }
            main.loadIni();
            File f1 = null;
            File f2 = null;
            ArrayList<String> file_list = new ArrayList<String>();
            boolean continua = false;
            if (main.getPersonalContain("emicad") && main.fileIni.getValueBoolean("emicad", "attiva", false) && !acquisto) {
                if (main.fileIni.getValueBoolean("emicad", "richiedi", true)) {
                    JFileChooser fileChoose = new JFileChooser();
                    FileFilter filter1 = new FileFilter() {
                        public boolean accept(File pathname) {
                            if (pathname.getAbsolutePath().endsWith(".pdf") || pathname.isDirectory()) {
                                return true;
                            } else {
                                return false;
                            }
                        }

                        @Override
                        public String getDescription() {
                            return "File PDF (*.pdf)";
                        }
                    };
                    fileChoose.setFileFilter(filter1);
                    int ret = fileChoose.showDialog(main.getPadreFrame(), "Imposta");
                    if (ret == JFileChooser.APPROVE_OPTION) {
                        f1 = fileChoose.getSelectedFile();
                        file_list.add(0, f1.getAbsolutePath());
                    } else {
                        file_list.add(0, "");
                    }
                    ret = fileChoose.showDialog(main.getPadreFrame(), "Imposta");
                    if (ret == JFileChooser.APPROVE_OPTION) {
                        f2 = fileChoose.getSelectedFile();
                        file_list.add(1, f2.getAbsolutePath());
                    } else {
                        file_list.add(1, "");
                    }
                } else {
                    String path_pre = main.fileIni.getValue("emicad", "file_pre", "");
                    String path_post = main.fileIni.getValue("emicad", "file_post", "");
                    if (!path_pre.equals("")) {
                        f1 = new File(path_pre);
                        file_list.add(0, f1.getAbsolutePath());
                    } else {
                        file_list.add(0, "");
                    }
                    if (!path_post.equals("")) {
                        f2 = new File(path_post);
                        file_list.add(1, f2.getAbsolutePath());
                    } else {
                        file_list.add(1, "");
                    }
                }
            }

            SwingUtils.mouse_wait(main.getPadreFrame());
            final ArrayList<String> list = file_list;
            for (int i = 0; i < id.length; i++) {
                int idn = id[i];
                List<Map> ret = DbUtils.getListMap(Db.getConn(), "select serie, numero, anno from " + Db.getNomeTabT(tipo_doc) + " where id = " + idn);
                final String dbSerie = cu.toString(ret.get(0).get("serie"));
                final int dbNumero = cu.toInteger(ret.get(0).get("numero"));
                final int dbAnno = cu.toInteger(ret.get(0).get("anno"));

                try {
                    InvoicexUtil.aggiornaTotaliRighe(tipo_doc, idn);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (main.getPersonalContain("emicad") && !acquisto && list != null) {
                    frmElenOrdini.stampa(null, dbSerie, dbNumero, dbAnno, true, false, true, acquisto, list, idn);
                } else if (apriDirDopoStampa && !apriPdf) {    //non attende completamento
                    ret_stampa = frmElenOrdini.stampa(null, dbSerie, dbNumero, dbAnno, true, false, true, acquisto, idn);
                } else {
                    ret_stampa = frmElenOrdini.stampa(null, dbSerie, dbNumero, dbAnno, true, true, true, acquisto, idn);
                }
                if (dialog != null) {
                    dialog.progress.setValue(i + 1);
                }
            }
        } else if (tipo_doc.equals(Db.TIPO_DOCUMENTO_FATTURA)) {
            if (main.getPersonalContain(main.PERSONAL_LUXURY)) {
                //chiedo se stampare in nero
                int ret = javax.swing.JOptionPane.showConfirmDialog(main.getPadreFrame(), "Vuoi stampare con lo sfondo nero ?", "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION);
                if (ret == javax.swing.JOptionPane.YES_OPTION) {
                    main.luxStampaNera = true;
                } else {
                    main.luxStampaNera = false;
                }
                //euro o dollari
                int ret2 = javax.swing.JOptionPane.showConfirmDialog(main.getPadreFrame(), "Vuoi stampare in Dollari ?", "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION);
                if (ret2 == javax.swing.JOptionPane.YES_OPTION) {
                    main.luxStampaValuta = "\u0024";
                } else {
                    main.luxStampaValuta = "\u20ac";
                }
            }

            SwingUtils.mouse_wait(main.getPadreFrame());

            for (int i = 0; i < id.length; i++) {
                int idn = id[i];
                List<Map> ret = DbUtils.getListMap(Db.getConn(), "select tf.descrizione_breve AS tipo, serie, numero, anno from " + Db.getNomeTabT(tipo_doc) + " t left join tipi_fatture tf on t.tipo_fattura = tf.tipo where id = " + idn);
                final String tipoFattura = cu.toString(ret.get(0).get("tipo"));
                final String dbSerie = cu.toString(ret.get(0).get("serie"));
                final int dbNumero = cu.toInteger(ret.get(0).get("numero"));
                final int dbAnno = cu.toInteger(ret.get(0).get("anno"));

                try {
                    InvoicexUtil.aggiornaTotaliRighe(Db.TIPO_DOCUMENTO_FATTURA, idn);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (apriDirDopoStampa && !apriPdf) {    //non attende completamento
                    ret_stampa = frmElenFatt.stampa(tipoFattura, dbSerie, dbNumero, dbAnno, true, false, true, idn);
                } else {
                    ret_stampa = frmElenFatt.stampa(tipoFattura, dbSerie, dbNumero, dbAnno, true, true, true, idn);
                }
                if (dialog != null) {
                    dialog.progress.setValue(i + 1);
                }
            }
        } else if (tipo_doc.equals(Db.TIPO_DOCUMENTO_DDT) || tipo_doc.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
            SwingUtils.mouse_wait(main.getPadreFrame());
            boolean acquisto = false;
            if (tipo_doc.equals((Db.TIPO_DOCUMENTO_DDT_ACQUISTO))) {
                acquisto = true;
            }

            for (int i = 0; i < id.length; i++) {
                int idn = id[i];

                List<Map> ret = DbUtils.getListMap(Db.getConn(), "select serie, numero, anno from " + Db.getNomeTabT(tipo_doc) + " where id = " + idn);
                final String dbSerie = cu.toString(ret.get(0).get("serie"));
                final int dbNumero = cu.toInteger(ret.get(0).get("numero"));
                final int dbAnno = cu.toInteger(ret.get(0).get("anno"));

                try {
                    InvoicexUtil.aggiornaTotaliRighe(tipo_doc, idn);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (apriDirDopoStampa && !apriPdf) {    //non attende completamento
                    ret_stampa = frmElenDDT.stampa(null, dbSerie, dbNumero, dbAnno, true, false, true, acquisto, idn);
                } else {
                    ret_stampa = frmElenDDT.stampa(null, dbSerie, dbNumero, dbAnno, true, true, true, acquisto, idn);
                }
                if (dialog != null) {
                    dialog.progress.setValue(i + 1);
                }
            }
        }

        if (apriDirDopoStampa) {
            try {
                Util.start(fdDir.getAbsolutePath());
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
        if (apriPdf && ret_stampa != null && ret_stampa instanceof String) {
            try {
                Util.start(ret_stampa.toString());
            } catch (Exception err) {
                err.printStackTrace();
            }
        }

        SwingUtils.mouse_def(main.getPadreFrame());

    }

    static public void ripristinaDump(File f, SwingWorker w) throws SQLException, IOException {
        try {
            InvoicexEvent event = new InvoicexEvent(f);
            event.type = InvoicexEvent.TYPE_PRE_RESTORE_DB;
            main.events.fireInvoicexEvent(event);
        } catch (Exception err) {
            err.printStackTrace();
        }

        Statement stat;
        stat = Db.getConn().createStatement();
        try {
            stat.execute("SET foreign_key_checks = 0;");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        //droppo tutte le tabelle
//        try {
//            ArrayList<Object[]> tables = DbUtils.getListArray(Db.getConn(), "show full tables");
//            for (Object[] a : tables) {
//                String t = (String) a[0];
//                String tipo = (String) a[1];
//                if (tipo == null || !tipo.equalsIgnoreCase("VIEW")) {
//                    try {
//                        stat.execute("DROP TABLE IF EXISTS " + t);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    try {
//                        stat.execute("DROP VIEW IF EXISTS " + t);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        String mysql_storage_engine = "storage_engine";
        boolean mysql_new = false;
        try {
            String mysql_ver = cu.s(dbu.getListMap(Db.getConn(), "SHOW VARIABLES LIKE \"version\";").get(0).get("Value"));
            String[] mysql_ver_split = StringUtils.split(mysql_ver, ".");
            int mysql_ver_maj = cu.i0(mysql_ver_split[0]);
            int mysql_ver_min = cu.i0(mysql_ver_split[1]);
            if (mysql_ver_maj > 5 || (mysql_ver_maj == 5 && mysql_ver_min >= 6)) {
                mysql_new = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mysql_new) {
            mysql_storage_engine = "default_storage_engine";
        }

        if (!Sync.isActive()) {
            stat.execute("SET " + mysql_storage_engine + "=MYISAM;");
        } else {
            stat.execute("SET " + mysql_storage_engine + "=INNODB;");
        }

        String sqlc = null;

        int tot = (int) f.length();
        FileInputStream fis = new FileInputStream(f);
        String sql = "";

        //detect encoding -----
        FileInputStream fisdet = new FileInputStream(f);
        byte[] buf = new byte[4096];
        UniversalDetector detector = new UniversalDetector(null);
        int nread;
        while ((nread = fisdet.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        if (encoding != null) {
            System.out.println("Detected encoding = " + encoding);
        } else {
            System.out.println("No encoding detected.");
            encoding = "";
        }
        detector.reset();
        //----------------------

        SqlLineIterator liter = new SqlLineIterator(fis);
        if (encoding.startsWith("UTF")) {
            liter.utf8 = true;
        }
        int linen = 0;

        JWindow werr = null;
        while (liter.hasNext()) {
            linen++;
            if (linen % 50 == 0) {
                w.publish(new int[]{(int) liter.bytes_processed, (int) tot});
                w.publish("Ripristino in corso " + FileUtils.byteCountToDisplaySize(liter.bytes_processed) + "/" + FileUtils.byteCountToDisplaySize(tot));
            }
            sqlc = liter.nextLine();
            System.out.println("sqlc = " + sqlc);

            if (sqlc.startsWith("--")) {
                continue;
            }
            if (sqlc.startsWith("insert into v_righ_tutte ")) {
                continue;
            }
            if (sqlc.startsWith("CREATE ALGORITHM=UNDEFINED")) {
                //CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`192.168.0.%` SQL SECURITY DEFINER VIEW `v_righ_tutte` AS select 'v' AS `tabella`,`r`.`id` AS `id`,`r`.`id_padre` AS `id_padre`,`t`.`data` AS `data`,`t`.`anno` AS `anno`,`t`.`numero` AS `numero`,`t`.`serie` AS `serie`,`r`.`riga` AS `riga`,`r`.`codice_articolo` AS `codice_articolo`,`r`.`descrizione` AS `descrizione`,`r`.`quantita` AS `quantita`,`r`.`prezzo` AS `prezzo`,`r`.`prezzo_netto_unitario` AS `prezzo_netto_unitario`,`r`.`sconto1` AS `sconto1`,`r`.`sconto2` AS `sconto2`,`t`.`sconto1` AS `sconto1t`,`t`.`sconto2` AS `sconto2t`,`t`.`sconto3` AS `sconto3t`,`c`.`codice` AS `clifor`,`c`.`ragione_sociale` AS `ragione_sociale` from ((`righ_fatt` `r` join `test_fatt` `t` on((`r`.`id_padre` = `t`.`id`))) join `clie_forn` `c` on((`t`.`cliente` = `c`.`codice`))) union all select 'a' AS `tabella`,`r`.`id` AS `id`,`r`.`id_padre` AS `id_padre`,`t`.`data` AS `data`,`t`.`anno` AS `anno`,`t`.`numero` AS `numero`,`t`.`serie` AS `serie`,`r`.`riga` AS `riga`,`r`.`codice_articolo` AS `codice_articolo`,`r`.`descrizione` AS `descrizione`,`r`.`quantita` AS `quantita`,`r`.`prezzo` AS `prezzo`,`r`.`prezzo_netto_unitario` AS `prezzo_netto_unitario`,`r`.`sconto1` AS `sconto1`,`r`.`sconto2` AS `sconto2`,`t`.`sconto1` AS `sconto1t`,`t`.`sconto2` AS `sconto2t`,`t`.`sconto3` AS `sconto3t`,`c`.`codice` AS `clifor`,`c`.`ragione_sociale` AS `ragione_sociale` from ((`righ_fatt_acquisto` `r` join `test_fatt_acquisto` `t` on((`r`.`id_padre` = `t`.`id`))) join `clie_forn` `c` on((`t`.`fornitore` = `c`.`codice`)));
                System.out.println("sqlc prima = " + sqlc);
                sqlc = "create view " + StringUtils.substringAfter(sqlc, " VIEW ");
                System.out.println("sqlc dopo  = " + sqlc);
            }
            if (StringUtils.isBlank(sqlc)) {
                continue;
            }

            sqlc = StringUtils.replace(sqlc, "0x,", "null,");
            sqlc = StringUtils.replace(sqlc, "0x)", "null)");

            if (Sync.test1) {
                System.out.println("sqlc: " + sqlc);
            }

            if (sqlc.length() > 0) {
                try {
                    sqlc = StringUtils.replace(sqlc, "USING BTREE", "");

                    //controllo per logo in dati azienda
                    boolean fatto_da_checkblob = false;
                    try {
                        fatto_da_checkblob = InvoicexUtil.checkSqlBlob(sqlc);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!fatto_da_checkblob) {
//                        System.out.println("sqlc = " + sqlc);
                        stat.execute(sqlc);
                    }
                } catch (com.mysql.jdbc.PacketTooBigException toobig) {
                    //controllo se è dati azienda provo a maetterlo a pezzi
                    if (werr == null || !werr.isVisible()) {
                        werr = SwingUtils.showFlashMessage2("Errore durante il ripristino: " + toobig.getMessage(), 5, null, Color.RED);
                    }
                    toobig.printStackTrace();
                    System.out.println("toobig sql di errore:" + sql);
                } catch (Exception err) {
                    if (werr == null || !werr.isVisible()) {
                        if (err.getMessage().indexOf("character_set_client") < 0) {
                            werr = SwingUtils.showFlashMessage2("Errore durante il ripristino: " + err.getMessage(), 5, null, Color.RED);
                        }
                    }
                    err.printStackTrace();
                    System.out.println("sql di errore:" + sqlc);
                }
            }
        }

        try {
            stat.execute("SET foreign_key_checks = 1;");
        } catch (Exception e) {
            e.printStackTrace();
        }

        stat.close();

        try {
            System.out.println("InvoicexUtil invio TYPE_POST_RESTORE_DB");
            InvoicexEvent event = new InvoicexEvent(f);
            event.type = InvoicexEvent.TYPE_POST_RESTORE_DB;
            main.events.fireInvoicexEvent(event);
            System.out.println("InvoicexUtil invio TYPE_POST_RESTORE_DB inviato");
        } catch (Exception err) {
            err.printStackTrace();
        }

    }

    public static void importRigheXlsCirri(JInternalFrame form) {
        form.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String lastdir = main.fileIni.getValue("varie", "last_dir_import_cirri");
        JFileChooser fileChoose = new JFileChooser(lastdir);
        FileFilter filter1 = new FileFilter() {
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
                return "File Excel 97 compatibili (*.xls)";
            }
        };

        fileChoose.addChoosableFileFilter(filter1);
        fileChoose.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int ret = fileChoose.showOpenDialog(form);

        if (ret == javax.swing.JFileChooser.APPROVE_OPTION) {
            main.fileIni.setValue("varie", "last_dir_import_cirri", fileChoose.getSelectedFile().getParent());
            String nomeListino = "FromFile";
            try {
                //apro il file
                File f = fileChoose.getSelectedFile();
                int idPadre = ((GenericFrmTest) form).getId();
                String tipo_doc = getTipoDocFromFrame(form);
                Object ret2 = InvoicexUtil.importXlsCirri(tipo_doc, f, idPadre);
                if (ret2 instanceof Boolean && (Boolean) ret2 == true) {
                    InvoicexUtil.aggiornaTotaliRighe(tipo_doc, idPadre, ((GenericFrmTest) form).isPrezziIvati());
                    ((tnxDbGrid) ((GenericFrmTest) form).getGrid()).dbRefresh();
                    ((GenericFrmTest) form).ricalcolaTotali();
                    SwingUtils.showInfoMessage(form, "Righe importate");
                } else {
                    SwingUtils.showErrorMessage(form, "Errore durante l'import:\n" + cu.toString(ret2));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        form.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public static Map getSerieNumeroAnno(String tipo_doc, Integer id) throws Exception {
        String sql = "select serie, numero, anno from " + getTabTestateFromTipoDoc(tipo_doc) + " where id = " + Db.pc(id, Types.INTEGER);
        List<Map> list = DbUtils.getListMap(Db.getConn(), sql);
        if (list.size() != 1) {
            return null;
        }
        return list.get(0);
    }

    public static Map getSerieNumeroAnnoDataIntEst(String tipo_doc, Integer id) throws Exception {
        String sql = "select serie, numero, anno, data "
                + " " + (tipo_doc.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA) ? ", numero_doc, serie_doc, data_doc" : "")
                + " from " + getTabTestateFromTipoDoc(tipo_doc) + " where id = " + Db.pc(id, Types.INTEGER);
        List<Map> list = DbUtils.getListMap(Db.getConn(), sql);
        if (list.size() != 1) {
            return null;
        }
        return list.get(0);
    }

    public static String getTabTestateFromTipoDoc(String tipodoc) {
        if (tipodoc.equals(Db.TIPO_DOCUMENTO_FATTURA)) {
            return "test_fatt";
        } else if (tipodoc.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            return "test_fatt_acquisto";
        } else if (tipodoc.equals(Db.TIPO_DOCUMENTO_DDT)) {
            return "test_ddt";
        } else if (tipodoc.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
            return "test_ddt_acquisto";
        } else if (tipodoc.equals(Db.TIPO_DOCUMENTO_ORDINE)) {
            return "test_ordi";
        } else if (tipodoc.equals(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
            return "test_ordi_acquisto";
        }
        return null;
    }

    public static String getTabTestateFromTabRighe(String tabrighe) {
        if (tabrighe.equals("righ_fatt")) {
            return "test_fatt";
        } else if (tabrighe.equals("righ_fatt_acquisto")) {
            return "test_fatt_acquisto";
        } else if (tabrighe.equals("righ_ddt")) {
            return "test_ddt";
        } else if (tabrighe.equals("righ_ddt_acquisto")) {
            return "test_ddt_acquisto";
        } else if (tabrighe.equals("righ_ordi")) {
            return "test_ordi";
        } else if (tabrighe.equals("righ_ordi_acquisto")) {
            return "test_ordi_acquisto";
        }
        return null;
    }

    public static String getTabRigheFromTabTestate(String tab) {
        if (tab.equals("test_fatt")) {
            return "righ_fatt";
        } else if (tab.equals("test_fatt_acquisto")) {
            return "righ_fatt_acquisto";
        } else if (tab.equals("test_ddt")) {
            return "righ_ddt";
        } else if (tab.equals("test_ddt_acquisto")) {
            return "righ_ddt_acquisto";
        } else if (tab.equals("test_ordi")) {
            return "righ_ordi";
        } else if (tab.equals("test_ordi_acquisto")) {
            return "righ_ordi_acquisto";
        }
        return null;
    }

    public static String getTabRigheFromTipoDoc(String tipodoc) {
        if (tipodoc.equals(Db.TIPO_DOCUMENTO_FATTURA)) {
            return "righ_fatt";
        } else if (tipodoc.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA)) {
            return "righ_fatt_acquisto";
        } else if (tipodoc.equals(Db.TIPO_DOCUMENTO_DDT)) {
            return "righ_ddt";
        } else if (tipodoc.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO)) {
            return "righ_ddt_acquisto";
        } else if (tipodoc.equals(Db.TIPO_DOCUMENTO_ORDINE)) {
            return "righ_ordi";
        } else if (tipodoc.equals(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
            return "righ_ordi_acquisto";
        }
        return null;
    }

    public static String getTipoDocFromFrame(JInternalFrame frame) {
        if (frame instanceof frmTestDocu) {
            return ((frmTestDocu) frame).acquisto ? Db.TIPO_DOCUMENTO_DDT_ACQUISTO : Db.TIPO_DOCUMENTO_DDT;
        } else if (frame instanceof frmTestFatt) {
            return Db.TIPO_DOCUMENTO_FATTURA;
        } else if (frame instanceof frmTestFattAcquisto) {
            return Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA;
        } else if (frame instanceof frmTestOrdine) {
            return ((frmTestOrdine) frame).acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE;
        }
        return null;
    }

    public static void aggiornaAnnoDaData(String tipodoc, Integer id) {
        try {
            String sql = "update " + getTabTestateFromTipoDoc(tipodoc) + " set anno = year(data) where id = " + id;
            DbUtils.tryExecQuery(Db.getConn(), sql);
            sql = "update " + getTabRigheFromTipoDoc(tipodoc) + " r join " + getTabTestateFromTipoDoc(tipodoc) + " t on r.id_padre = t.id set r.anno = year(t.data) where r.id_padre = " + id;
            DbUtils.tryExecQuery(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getIvaSpesePerData(Date data) {
        //passaggo iva 21 dal 17/09/2011 compreso
        if (data != null && data.before(DateUtils.getOnlyDate(2011, 9, 17))) {
            //se antecedente metto fisso codice 20 se è avvalorato il codice iva spese
            if (!StringUtils.isBlank(InvoicexUtil.getIvaSpese()) && InvoicexUtil.getIvaSpese().equals("21")) {
                return "20";
            }
        } else if (data != null && data.before(DateUtils.getOnlyDate(2013, 10, 1))) {
            //se antecedente metto fisso codice 20 se è avvalorato il codice iva spese
            if (!StringUtils.isBlank(InvoicexUtil.getIvaSpese()) && InvoicexUtil.getIvaSpese().equals("22")) {
                return "21";
            } else {
                return InvoicexUtil.getIvaSpese();
            }
        } else {
            return InvoicexUtil.getIvaSpese();
        }
        return null;
    }

    public static void caricaComboAgentiCliFor(tnxComboField combo, Integer cliente) {
        combo.dbClearList();
        combo.dbAddElement(null, "");
        Integer id_agente_pred = null;
        try {
            List<Map> list = dbu.getListMap(Db.getConn(), "select a.nome, a.id from agenti a join clie_forn cf on a.id = cf.agente and cf.codice = " + dbu.sql(cliente));
            if (list != null && list.size() > 0) {
                Map m = list.get(0);
                id_agente_pred = cu.toInteger(m.get("id"));
                combo.dbAddElement(m.get("nome"), m.get("id"));
                combo.dbAddElement(SeparatorComboBoxRenderer.SEPARATOR);
                combo.setSelectedIndex(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            List<Map> agenti = DbUtils.getListMap(Db.getConn(), "select a.nome, a.id from agenti a join clie_forn_agenti cfa on a.id = cfa.id_agente and cfa.id_clifor = " + dbu.sql(cliente));
            if (agenti != null) {
                for (Map m : agenti) {
                    Integer id_agente = cu.toInteger(m.get("id"));
                    if (id_agente != id_agente_pred) {
                        combo.dbAddElement(m.get("nome"), m.get("id"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Vector<DettaglioIva> getIveVector(Map<String, DettaglioIva> dettagliIva) {
        Vector ive = new Vector();
        Iterator<String> ivaiter = dettagliIva.keySet().iterator();
        while (ivaiter.hasNext()) {
            String ivakey = ivaiter.next();
            ive.add(dettagliIva.get(ivakey));
        }
        return ive;
    }

    public static Vector<DettaglioIva> getIveDedVector(Map<IvaDed, DettaglioIva> dettagliIva) {
        Vector ive = new Vector();
        Iterator<IvaDed> ivaiter = dettagliIva.keySet().iterator();
        while (ivaiter.hasNext()) {
            IvaDed ivakey = ivaiter.next();
            ive.add(dettagliIva.get(ivakey));
        }
        return ive;
    }

    public static int ricalcolaPagatoDaPagare() throws Exception {
        return ricalcolaPagatoDaPagare(Db.getConn(), null);
    }

    public static int ricalcolaPagatoDaPagare(Connection conn, Integer id_scadenza) throws Exception {
        if (Sync.isActive() && id_scadenza == null) {
            SwingUtils.showErrorMessage(InvoicexUtil.getActiveJInternalFrame(), "Sync: aggiornare chiamata a ricalcolaPagatoDaPagare");
            return 0;
        }
        if (!Sync.isActive()) {
            String sql = "update scadenze s \n"
                    + " set  s.importo_pagato = \n"
                    + " IFNULL((select sum(sp.importo) from scadenze_parziali sp where sp.id_scadenza = s.id), IF(IFNULL(s.pagata,'N') = 'S', s.importo, 0))\n"
                    + " , s.importo_da_pagare = (s.importo - IFNULL((select sum(sp.importo) from scadenze_parziali sp where sp.id_scadenza = s.id), IF(IFNULL(s.pagata,'N') = 'S', s.importo, 0)))\n"
                    + " , s.ultimo_pagamento = IFNULL((select MAX(sp.data) from scadenze_parziali sp where sp.id_scadenza = s.id), IF(IFNULL(s.pagata,'N') = 'S', s.data_scadenza, NULL))";
            return DbUtils.tryExecQueryWithResult(conn, sql);
        } else {
            String sql_importo_pagato = "select IFNULL((select sum(sp.importo) from scadenze_parziali sp where sp.id_scadenza = s.id), IF(IFNULL(s.pagata,'N') = 'S', s.importo, 0)) from scadenze s where id = " + id_scadenza;
            Double importo_pagato = cu.d(dbu.getObject(conn, sql_importo_pagato));
            System.out.println("importo_pagato = " + importo_pagato);
            String sql_importo_da_pagare = "select (s.importo - IFNULL((select sum(sp.importo) from scadenze_parziali sp where sp.id_scadenza = s.id), IF(IFNULL(s.pagata,'N') = 'S', s.importo, 0))) from scadenze s where id = " + id_scadenza;
            Double importo_da_pagare = cu.d(dbu.getObject(conn, sql_importo_da_pagare));
            System.out.println("importo_da_pagare = " + importo_da_pagare);
            String sql_ultimo_pagamento = "select IFNULL((select MAX(sp.data) from scadenze_parziali sp where sp.id_scadenza = s.id), IF(IFNULL(s.pagata,'N') = 'S', s.data_scadenza, NULL)) from scadenze s where id = " + id_scadenza;
            Date ultimo_pagamento = cu.toDate(dbu.getObject(conn, sql_ultimo_pagamento));
            System.out.println("ultimo_pagamento = " + ultimo_pagamento);

            String sql = "update scadenze set "
                    + " importo_pagato = " + dbu.sql(importo_pagato)
                    + " , importo_da_pagare = " + dbu.sql(importo_da_pagare)
                    + " , ultimo_pagamento = " + dbu.sql(ultimo_pagamento)
                    + " where id = " + id_scadenza;
            System.out.println("sql = " + sql);
            return DbUtils.tryExecQueryWithResult(conn, sql);
        }
    }

    public static int ricalcolaTotaleScadenze() throws Exception {
        return ricalcolaTotaleScadenze(Db.getConn(), null, null);
    }

    public static int ricalcolaTotaleScadenze(Connection conn, String tipo_doc, Integer id_doc) throws Exception {
        if (Sync.isActive() && id_doc == null) {
            SwingUtils.showErrorMessage(InvoicexUtil.getActiveJInternalFrame(), "Sync: aggiornare chiamata a ricalcolaTotaleScadenze");
            return 0;
        }
        if (!Sync.isActive()) {
            String sql = "UPDATE scadenze s, (\n"
                    + "    SELECT max(s2.numero) as tot, s2.documento_tipo, s2.id_doc\n"
                    + "    FROM scadenze s2 group by s2.documento_tipo, s2.id_doc) temp\n"
                    + " SET s.numero_totale = temp.tot\n"
                    + " where s.documento_tipo = temp.documento_tipo \n"
                    + " and s.id_doc = temp.id_doc\n"
                    + " and IFNULL(s.flag_acconto,'') != 'S'";
            try {
                dbu.getObject(conn, "select id_doc from scadenze limit 1", true);
            } catch (Exception e) {
                //se non c'è id vado per vecchi campi
                sql = "UPDATE scadenze s, (\n"
                        + "    SELECT max(s2.numero) as tot, s2.documento_tipo, s2.documento_serie, s2.documento_numero, s2.documento_anno\n"
                        + "    FROM scadenze s2 group by s2.documento_tipo, s2.documento_serie, s2.documento_numero, s2.documento_anno) temp\n"
                        + " SET s.numero_totale = temp.tot\n"
                        + " where s.documento_tipo = temp.documento_tipo \n"
                        + " and s.documento_serie = temp.documento_serie\n"
                        + " and s.documento_numero = temp.documento_numero\n"
                        + " and s.documento_anno = temp.documento_anno"
                        + " and IFNULL(s.flag_acconto,'') != 'S'";
            }
            return DbUtils.tryExecQueryWithResult(conn, sql);
        } else {
            String sql = "SELECT max(numero) as tot \n"
                    + " FROM scadenze "
                    + " where documento_tipo = " + dbu.sql(tipo_doc) + " and id_doc = " + id_doc + " and IFNULL(flag_acconto,'') != 'S'";
            Integer max = cu.i(dbu.getObject(conn, sql));
            sql = "update scadenze set "
                    + " numero_totale = " + dbu.sql(max)
                    + " where documento_tipo = " + dbu.sql(tipo_doc) + " and id_doc = " + id_doc + " and IFNULL(flag_acconto,'') != 'S'";
            System.out.println("sql = " + sql);
            return DbUtils.tryExecQueryWithResult(conn, sql);
        }
    }

    public static void lanciaTeamViewer() {
        if (PlatformUtils.isWindows() || PlatformUtils.isMac()) {
            File filevncd_dir = new File(System.getProperty("user.home") + "\\.invoicex\\tv\\");
            File filevnc_tmp = new File(System.getProperty("user.home") + "\\.invoicex\\tv\\tv.exe");
            if (PlatformUtils.isMac()) {
                filevncd_dir = new File(System.getProperty("user.home") + "/.invoicex/tv/");
                filevnc_tmp = new File(System.getProperty("user.home") + "/.invoicex/tv/tv.zip");
            }
            filevncd_dir.mkdirs();
            final File filevnc = filevnc_tmp;

            try {
                System.out.println("delete " + filevnc + ":" + filevnc.delete());
            } catch (Exception e) {
                e.printStackTrace();
            }

            final JDialogWait wait = new JDialogWait(SwingUtils.getActiveFrame() instanceof JDialogImpostazioni ? null : main.getPadreFrame(), false);
            wait.setLocationRelativeTo(null);
            wait.setVisible(true);
            SwingUtils.mouse_wait(wait);
            wait.progress.setIndeterminate(false);
            wait.progress.setMinimum(0);
            wait.progress.setMaximum(100);
            wait.labStato.setText("scaricamento del programma di assistenza");

            SwingWorker w = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        String downloadfile = "http://www.invoicex.it/files/TeamViewerQS.exe";
                        if (PlatformUtils.isMac()) {
                            downloadfile = "http://www.invoicex.it/files/TeamViewerQS.zip";
                            String osxv = System.getProperty("os.version");
                            if (osxv.startsWith("10.5") || osxv.startsWith("10.6") || osxv.startsWith("10.7") || osxv.startsWith("10.10")) {
                                downloadfile = "http://www.invoicex.it/files/TeamViewerQS7.zip";
                            }
                        }
                        HttpUtils.saveBigFile(downloadfile, filevnc.getAbsolutePath(), new HttpUtils.SaveFileEventListener() {
                            public void event(float progression) {
                                publish(new Float(progression));
                            }
                        });
                    } catch (Exception ex) {
                        Logger.getLogger(MenuPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return null;
                }

                @Override
                protected void process(List chunks) {
                    try {
                        wait.progress.setValue(((Float) chunks.get(chunks.size() - 1)).intValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (PlatformUtils.isWindows()) {
                            ProcessBuilder pb = new ProcessBuilder(filevnc.getAbsolutePath());
                            Process p = pb.start();
                        } else {
                            System.out.println("filevnc:" + filevnc);
                            UnZip.unzip(filevnc, filevnc.getParentFile());
                            System.out.println("unzippato");
                            File filevncappexe = new File(System.getProperty("user.home") + "/.invoicex/tv/TeamViewerQS.app/Contents/MacOS/TeamViewerQS");
                            Runtime.getRuntime().exec(new String[]{"/bin/chmod", "+x", filevncappexe.getAbsolutePath()});

                            ProcessBuilder pb1 = new ProcessBuilder("/bin/sh", "-c", "/bin/chmod +wx " + System.getProperty("user.home") + "/.invoicex/tv/TeamViewerQS.app/Contents/Resources/*");
                            pb1.start();

                            File filevncapp = new File(System.getProperty("user.home") + "/.invoicex/tv/TeamViewerQS.app");
                            System.out.println("open " + filevncapp.getAbsolutePath());
                            ProcessBuilder pb = new ProcessBuilder(new String[]{"open", filevncapp.getAbsolutePath()});
                            Process p = pb.start();
                        }
                        wait.setVisible(false);
                        SwingUtils.mouse_def(wait);
                    } catch (Exception ex) {
                        Logger.getLogger(MenuPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            w.execute();

        } else {
            try {
                SwingUtils.openUrl(new URL("https://www.teamviewer.com/it/download/index.aspx"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void aggiornaScadenze(String tipo_documento, Integer id_doc) {
        MicroBench mb = new MicroBench(true);
        try {
            Connection conn = Db.getConn();
            List<Map> id_scadenze = dbu.getListMap(Db.getConn(), "select id from scadenze where documento_tipo = " + dbu.sql(tipo_documento) + " and id_doc = " + id_doc);
            for (Map m : id_scadenze) {
                System.out.println("scadenze aggiornate da ricalcolaPagatoDaPagare : " + InvoicexUtil.ricalcolaPagatoDaPagare(conn, cu.i(m.get("id"))));
            }
            System.out.println("scadenze aggiornate da ricalcolaTotaleScadenze : " + InvoicexUtil.ricalcolaTotaleScadenze(conn, tipo_documento, id_doc));
            mb.out("ricalcolaPagatoDaPagare e ricalcolaTotaleScadenze");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTmpDir() {
        if (!main.isBatch) {
            return System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "tmp" + File.separator;
        } else {
            return "tmp" + File.separator;
        }
    }

    private static String getCampo(String tab, String campo) {
        Map<String, String> m = new HashMap();

        m.put("test_fatt.totale", "totale");
        m.put("test_ddt.totale", "totale");
        m.put("test_ordi.totale", "totale");
        m.put("test_fatt_acquisto.totale", "importo");
        m.put("test_ddt_acquisto.totale", "totale");
        m.put("test_ordi_acquisto.totale", "totale");

        m.put("test_fatt.totale_imponibile", "totale_imponibile");
        m.put("test_ddt.totale_imponibile", "totale_imponibile");
        m.put("test_ordi.totale_imponibile", "totale_imponibile");
        m.put("test_fatt_acquisto.totale_imponibile", "imponibile");
        m.put("test_ddt_acquisto.totale_imponibile", "totale_imponibile");
        m.put("test_ordi_acquisto.totale_imponibile", "totale_imponibile");

        return m.get(tab + "." + campo);
    }

    static public String getTipoIva(String paese) {
        if (paese == null) {
            return Cliente.TIPO_IVA_ITALIA;
        }
        if (paese.equalsIgnoreCase("IT") || paese.trim().length() == 0) {
            return Cliente.TIPO_IVA_ITALIA;
        } else if (Cliente.TIPO_IVA_PAESI_CEE.indexOf(paese) >= 0) {
            return Cliente.TIPO_IVA_CEE;
        } else {
            return Cliente.TIPO_IVA_ALTRO;
        }
    }

    static String getIvaRigaArticolo(Integer codiceCliFor, String paeseCliFor, String codiceArticolo) {
        String ret = null;

        String tipoIva = getTipoIva(paeseCliFor);
        if (tipoIva.equals(Cliente.TIPO_IVA_ALTRO) && main.fileIni.getValueBoolean("pref", "controlliIva", true)) {
            ret = "8";
        } else if (tipoIva.equals(Cliente.TIPO_IVA_CEE) && main.fileIni.getValueBoolean("pref", "controlliIva", true)) {
            ret = "41";
        } else {
            ret = InvoicexUtil.getIvaDefaultPassaggio();
        }

        //carico da articolo se trovato
        String sql = "select iva from articoli";
        sql += " where codice = " + Db.pc(codiceArticolo, "VARCHAR");
        try {
            ret = (String) DbUtils.getObject(Db.getConn(), sql);
        } catch (Exception e) {
        }

        // IVA standard in base al cliente se impostata
        try {
            String sqlIva = "SELECT i.codice FROM clie_forn c JOIN codici_iva i ON c.iva_standard = i.codice WHERE c.codice =" + Db.pc(codiceCliFor, Types.INTEGER);
            ResultSet ivaStandard = Db.openResultSet(sqlIva);
            if (ivaStandard.next()) {
                String ivaSel = ivaStandard.getString("codice");
                if (ivaSel != null && !ivaSel.equals("")) {
                    ret = ivaSel;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (StringUtils.isBlank(ret)) {
            ret = InvoicexUtil.getIvaDefaultPassaggio();
        }
        return ret;
    }

    static public Map getTotaliRiga(Double qta, Double importo_senza_iva, Double importo_con_iva, Double sconto1, Double sconto2, String codiceIva, boolean prezzi_ivati) {
        Map ret = new HashMap();

        double tot_senza_iva = 0;
        double tot_con_iva = 0;
        double totale_imponibile;
        double totale_ivato;

        if (!prezzi_ivati) {
            tot_senza_iva = importo_senza_iva - (importo_senza_iva / 100 * sconto1);
            tot_senza_iva = tot_senza_iva - (tot_senza_iva / 100 * sconto2);
//            if (main.fileIni.getValueBoolean("pref", "attivaArrotondamento", false)) {
//                double parametro = Double.parseDouble(String.valueOf(comArrotondamento.getSelectedItem()));
//                boolean perDifetto = String.valueOf(comTipoArr.getSelectedItem()).equals("Inf.");
//                tot_senza_iva = InvoicexUtil.calcolaPrezzoArrotondato(tot_senza_iva, parametro, perDifetto);
//                this.texTotArrotondato.setText(FormatUtils.formatEuroIta(tot_senza_iva));
//            }
            tot_senza_iva = FormatUtils.round(tot_senza_iva * qta, 2);
            totale_imponibile = tot_senza_iva;

            double iva_prezz = 100d;
            try {
                iva_prezz = 100d + ((BigDecimal) DbUtils.getObject(Db.getConn(), "select percentuale from codici_iva where codice = " + Db.pc(codiceIva, Types.VARCHAR))).doubleValue();
            } catch (Exception ex) {
                System.out.println("iva non trovata:" + codiceIva);
            }
            tot_con_iva = FormatUtils.round((tot_senza_iva / 100d) * iva_prezz, 2);
            totale_ivato = tot_con_iva;
        } else {
            tot_con_iva = importo_con_iva - (importo_con_iva / 100 * sconto1);
            tot_con_iva = tot_con_iva - (tot_con_iva / 100 * sconto2);
            tot_con_iva = FormatUtils.round(tot_con_iva * qta, 2);
            totale_ivato = tot_con_iva;

            double iva_prezz = 100d;
            try {
                iva_prezz = 100d + ((BigDecimal) DbUtils.getObject(Db.getConn(), "select percentuale from codici_iva where codice = " + Db.pc(codiceIva, Types.VARCHAR))).doubleValue();
            } catch (Exception ex) {
                System.out.println("iva non trovata:" + codiceIva);
            }
            tot_senza_iva = FormatUtils.round((tot_con_iva / iva_prezz) * 100d, 2);
            totale_imponibile = tot_senza_iva;
        }

        ret.put("tot_senza_iva", tot_senza_iva);
        ret.put("tot_con_iva", tot_con_iva);
        return ret;
    }

    public static void apriStampa(JasperPrint jasperPrint) throws IOException, JRException {
        if (main.fileIni.getValueBoolean("pref", "stampaPdf", false)) {
            File dirtmp = new File(System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "tmp" + File.separator);
            System.out.println("dirtmp = " + dirtmp);
            dirtmp.mkdirs();
            File file = File.createTempFile("print_", ".pdf", dirtmp);
            System.out.println("file = " + file);
            JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsolutePath());
            Util.start2(file.getAbsolutePath());
        } else {
            JasperViewer.viewReport(jasperPrint, false);
        }
    }

    static public void altricampipdf(PdfStamper stamp, Map r) {
        AcroFields form = null;
        try {
            form = stamp.getAcroFields();
            for (Object o : r.keySet()) {
                String k = cu.s(o);
                String v = cu.s(r.get(o));
                form.setField(k, v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Set<String> fields = form.getFields().keySet();
            PdfDictionary dict;
            for (String key : fields) {
                try {
                    Item item = form.getFieldItem(key);
                    dict = (PdfDictionary) item.merged.get(0);
                    String val = dict.getAsString(PdfName.V).toString();
                    System.out.println("key:" + key + " val:" + val);
                } catch (Exception e2) {
                    System.out.println("key:" + key + " errore:" + e2.toString());
                    e2.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void aggiornaCodiciIvaRigheDescrittive(String tabt, String tabr, Integer id) {
        System.out.println("aggiornaCodiciIvaRigheDescrittive:" + tabt + ":" + tabr + ":" + id);
        try {
            List list = DbUtils.getList(Db.getConn(), "select iva from " + tabr + " where id_padre = " + id + " and ltrim(rtrim(IFNULL(iva,''))) != '' group by iva order by iva desc");
            if (list.size() > 0) {
                String iva = null;
                if (list.size() == 1) {
                    iva = cu.s(list.get(0));
                } else if (StringUtils.isBlank(getIvaDefault())) {
                    //prendo il primo codice iva della fattura
                    iva = cu.s(list.get(0));
                } else {
                    iva = cu.s(getIvaDefault());
                }
                if (iva != null) {
                    DbUtils.tryExecQuery(Db.getConn(), "update " + tabr + " set iva = " + Db.pcs(iva) + " where id_padre = " + id + " and ltrim(rtrim(IFNULL(iva,''))) = ''");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkLockAddFrame(JInternalFrame frame, String tabella, Integer id) {
        lock_ts_timer.put(frame, new LockKey(tabella, id));
    }

    public static boolean checkLock(String tabella, int id, boolean visualizza_msg, JInternalFrame parent) {
        //elimino locks persi
        String sql = "delete from locks where TIMESTAMPDIFF(SECOND, IFNULL(ts_timer, ts), NOW()) > " + (main.tempo_online * 1.25);
        System.out.println("locks elimino locks perduti sql = " + sql);
        try {
            dbu.tryExecQuery(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map m = new HashMap();
        m.put("username_id", main.utente.getIdUtente());
        m.put("username", main.utente.getIdUtente());
        m.put("hostname", SystemUtils.getHostname());
        m.put("login_so", main.login);
        m.put("lock_tabella", tabella);
        m.put("lock_id", id);
        sql = "insert into locks set " + dbu.prepareSqlFromMap(m) + ", ts = NOW()";
        System.out.println("locks sql = " + sql);
        try {
            dbu.tryExecQuery(Db.getConn(), sql);
            return true;
        } catch (SQLException sqlex) {
            if (sqlex.getErrorCode() == 1062) {
                sql = "select * from locks where lock_tabella = " + Db.pcs(tabella) + " and lock_id = " + id;
                System.out.println("locks sql = " + sql);
                try {
                    List<Map> list = dbu.getListMap(Db.getConn(), sql);
                    if (list != null && list.size() > 0) {
                        Map rec = list.get(0);
                        String utente = cu.s(rec.get("login_so"));
                        if (main.utenti == 1) {
                            utente = cu.s(rec.get("username"));
                        }
                        DateFormat f1 = new SimpleDateFormat("HH:mm:ss");
                        String dalle = cu.toDate(rec.get("ts")) == null ? "" : f1.format(cu.toDate(rec.get("ts")));
                        SwingUtils.showInfoMessage(parent, "Il documento è aperto in modifica dall'utente '" + utente + "' sul computer '" + cu.s(rec.get("hostname")) + "' dalle " + dalle
                                + "\nPuoi vedere il documento cliccando su 'Stampa' o su 'Crea Pdf'");
                    } else {
                        SwingUtils.showInfoMessage(parent, "Il documento è aperto in modifica da un altro utente");
                    }
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                    SwingUtils.showExceptionMessage(parent, ex1);
                }
            } else {
                sqlex.printStackTrace();
                SwingUtils.showExceptionMessage(parent, sqlex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SwingUtils.showExceptionMessage(parent, ex);
        }
        return false;
    }

    public static void removeLock(String tabella, Integer id, JInternalFrame frame) {
        try {
            String sql = "delete from locks where lock_tabella = " + Db.pcs(tabella) + " and lock_id = " + id;
            System.out.println("lock remove sql = " + sql);
            dbu.tryExecQuery(Db.getConn(), sql);
            lock_ts_timer.remove(frame);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //da plugin Contabilità
    public static class EsitoRegistrazione {

        public boolean esito;
        public String messaggio_errore;
        public String anteprima;
        public List<Map> ritorno_righe;
        public Map ritorno_testata;
        public Map ritorno_clifor;
    }

    public static class EsitoGeneraTotali {

        public boolean esito;
        public String anomalia;
    }

    public static void generaTotaliDocumentoDate(Date dal, Date al, MySwingWorker worker) throws Exception {
        //controllo se ci sono testate o righe con ts_gen_totali < di ts
        List<Integer> fatture = getListDaGenerare("test_fatt", dal, al);
        List<Integer> fatture_acq = getListDaGenerare("test_fatt_acquisto", dal, al);
        int tot = fatture.size() + fatture_acq.size();
        int conta = 0;
        System.out.println("fatture di vendita da rigenerare totali = " + fatture);
        for (Integer id : fatture) {
            conta++;
            generaTotaliDocumento("test_fatt", id);
            if (worker != null) {
                worker.mypublish("status|Preparazione dati in corso... " + conta + "/" + tot);
            }
        }

        fatture_acq = getListDaGenerare("test_fatt_acquisto", dal, al);
        System.out.println("fatture di acquisto da rigenerare totali = " + fatture_acq);
        for (Integer id : fatture_acq) {
            conta++;
            generaTotaliDocumento("test_fatt_acquisto", id);
            if (worker != null) {
                worker.mypublish("status|Preparazione dati in corso... " + conta + "/" + tot);
            }
        }
    }

    public static EsitoGeneraTotali generaTotaliDocumento(String nome_tab, Integer id) throws Exception {
        //aggiorna totali fatture

        if (id == 115) {
            System.out.println("debug");
        }

        EsitoGeneraTotali ret = new EsitoGeneraTotali();
        ret.esito = false;

        boolean acquisto = "test_fatt".equals(nome_tab) ? false : true;        
        
        Documento doc;
        doc = new Documento();
//        String tipo_doc = dbFattura.
        doc.load(gestioneFatture.Db.INSTANCE, 0, null, 0, gestioneFatture.Db.getTipoDocDaNomeTabT(nome_tab), id);
        
//        Map testa = dbu.getListMap(Db.getConn(), "select * from " + nome_tab + " where id = " + id).get(0);
//        List<Map> righe = dbu.getListMap(Db.getConn(), "select * from " + InvoicexUtil.getTabRigheFromTabTestate(nome_tab) + " where id = " + id);
//        Documento2 doc2 = new Documento2(testa, righe);
//        doc2.cliente = dbu.getListMap(Db.getConn(), "select * from clie_forn where codice = " + (acquisto ? testa.get("fornitore") : testa.get("cliente"))).get(0);
//        doc2.iva = dbu.getListMapMap(Db.getConn(), "select * from codici_iva order by codice", "codice");
//        doc2.calcolaTotali();
        
        String campo_totale = acquisto ? "importo" : "totale";
        ResultSet r = DbUtils.tryOpenResultSet(gestioneFatture.Db.getConn(), "select id, " + campo_totale + ", serie, numero, anno from " + nome_tab + " where id = " + id);
        if (r.next()) {
            doc.calcolaTotali();

            if ((it.tnx.Util.round(r.getDouble(campo_totale), 2) != it.tnx.Util.round(doc.getTotale(), 2)) && (Math.abs(it.tnx.Util.round(r.getDouble(campo_totale), 2) - it.tnx.Util.round(doc.getTotale(), 2)) >= 1.0D)) {
                ret.anomalia = "Totale registrato (" + it.tnx.Util.round(r.getDouble(campo_totale), 2) + ") diverso da totale calcolato (" + (it.tnx.Util.round(doc.getTotale(), 2)) + ")";
                System.out.println("ret.anomalia = " + ret.anomalia);
            }
            
//            if ((it.tnx.Util.round(r.getDouble(campo_totale), 2) != it.tnx.Util.round(doc2.getTotale(), 2)) && (Math.abs(it.tnx.Util.round(r.getDouble(campo_totale), 2) - it.tnx.Util.round(doc2.getTotale(), 2)) >= 1.0D)) {
//                ret.anomalia = "Totale registrato (" + it.tnx.Util.round(r.getDouble(campo_totale), 2) + ") diverso da totale calcolato doc2 (" + (it.tnx.Util.round(doc2.getTotale(), 2)) + ")";
//                System.out.println("ret.anomalia = " + ret.anomalia);
//            }

            //memorizzo in test_fatt_iva
            String sql = "delete from " + nome_tab + "_iva where id_padre = " + id;
            DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), sql);
            for (Object odet : doc.dettagliIva) {
                DettaglioIva diva = (DettaglioIva) odet;
                Map m = new HashMap();
                m.put("id_padre", id);
                m.put("codice_iva", diva.getCodice());
                m.put("perc_iva", diva.getPercentuale());
                m.put("imponibile", diva.getImponibile());
                m.put("iva", diva.getImposta());
                m.put("totale", it.tnx.Util.round(diva.getImponibile() + diva.getImposta(), 2));
                sql = "insert into " + nome_tab + "_iva set " + DbUtils.prepareSqlFromMap(m);
                System.out.println("sql = " + sql);
                DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), sql);
            }
            
            if (acquisto) {
                //memorizzo in test_fatt_iva_ded
                sql = "delete from " + nome_tab + "_iva_ded where id_padre = " + id;
                DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), sql);
                for (IvaDed ivaded : doc.dettagliIvaDedMap.keySet()) {
                    System.out.println("ivaded = " + ivaded);
                    DettaglioIva diva = doc.dettagliIvaDedMap.get(ivaded);

                    Map m = new HashMap();
                    m.put("id_padre", id);
                    m.put("codice_iva", diva.getCodice());
                    m.put("perc_iva", diva.getPercentuale());
                    m.put("perc_deducibile", diva.getPerc_deducibilita());
                    m.put("imponibile", diva.getImponibile());
                    m.put("iva", diva.getImposta());
                    m.put("totale", it.tnx.Util.round(diva.getImponibile() + diva.getImposta(), 2));
                    
                    sql = "insert into " + nome_tab + "_iva_ded set " + DbUtils.prepareSqlFromMap(m);
                    System.out.println("sql = " + sql);
                    DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), sql);
                }            
            }
        } else {
            System.out.println("!!! non trovato documento da " + nome_tab + " con id " + id);
        }

        String tab_righe = gestioneFatture.Db.getNomeTabRdaTabT(nome_tab);
        String sql = "update " + tab_righe + " set ts_gen_totali = CURRENT_TIMESTAMP where id_padre = " + id;
        DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), sql);
        sql = "update " + nome_tab + " set ts_gen_totali = CURRENT_TIMESTAMP where id = " + id;
        DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), sql);

        try {
            DbUtils.close(r);
        } catch (Exception e) {
        }

        ret.esito = true;

        return ret;
    }

    private static List<Integer> getListDaGenerare(String tab, Date dal, Date al) {
        List<Integer> ret = new ArrayList();
        String sql = "select id from " + tab + " where (ts > IFNULL(ts_gen_totali,0) or ts is null) ";
        if (dal != null) {
            sql += " and data >= " + gestioneFatture.Db.pcdate(dal);
        }
        if (al != null) {
            sql += " and data <= " + gestioneFatture.Db.pcdate(al);
        }
//        String sql = "select id from " + tab + " where data >= '2013-01-01'";
        System.out.println("sql da rigenerare = " + sql);
        try {
            List list = DbUtils.getList(gestioneFatture.Db.getConn(), sql);
            if (list.size() > 0) {
                ret.addAll(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sql = "select id_padre from " + gestioneFatture.Db.getNomeTabRdaTabT(tab) + " r join " + tab + " t on r.id_padre = t.id where (r.ts > IFNULL(r.ts_gen_totali,0) or r.ts is null) ";
        if (dal != null) {
            sql += " and t.data >= " + gestioneFatture.Db.pcdate(dal);
        }
        if (al != null) {
            sql += " and t.data <= " + gestioneFatture.Db.pcdate(al);
        }
        sql += " group by id_padre";
        System.out.println("sql da rigenerare = " + sql);
        try {
            List<Integer> list = DbUtils.getList(gestioneFatture.Db.getConn(), sql);
            if (list.size() > 0) {
                for (Integer id : list) {
                    if (!ret.contains(id)) {
                        ret.add(id);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    

    static abstract public class MySwingWorker extends SwingWorker<Object, Object> {

        public void mypublish(Object... chunks) {
            publish(chunks);
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        //controllo file encoding
//        File f = new File("C:\\Users\\Marco\\Documents\\Invoicex\\backup\\backup-26-02-2014_14-30.txt");
        //
        File f = new File("C:\\Users\\Marco\\Documents\\Invoicex\\backup\\backup-26-02-2014_14-41.txt");
        //UTF-8
        byte[] buf = new byte[4096];
        String fileName = f.getAbsolutePath();
        java.io.FileInputStream fis = new java.io.FileInputStream(fileName);
        UniversalDetector detector = new UniversalDetector(null);
        int nread;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        if (encoding != null) {
            System.out.println("Detected encoding = " + encoding);
        } else {
            System.out.println("No encoding detected.");
        }
        detector.reset();
    }

//    public static void main(String[] args) throws FileNotFoundException, IOException {
//        File f = new File("C:\\lavori\\tnx\\private\\Invoicex_altro\\run\\backup\\dump_20130522_1703.txt");
//        FileInputStream fis = new FileInputStream(f);
//        String sql = "";
//        String sqlc = "";
//        int tot = (int) f.length();
//        SqlLineIterator liter = new SqlLineIterator(fis);
//        int linen = 0;
//
//        while (liter.hasNext()) {
//            linen++;
//            sqlc = liter.nextLine();
//            checkSqlBlob(sqlc);
//        }
//    }
    static public List<Plugin> getPlugins(boolean solopubblici, boolean pluginInvoicex) throws Exception {
        List list = new ArrayList<Plugin>();

        String ret_old = HttpUtils.getUrlToStringUTF8(main.baseurlserver + "/plugins.php?solopubblici=" + solopubblici + "&pluginInvoicex=" + pluginInvoicex);
        System.out.println("ret_old: " + ret_old);

        String ret = HttpUtils.getUrlToStringUTF8(main.baseurlserver + "/plugins.php?solopubblici=" + solopubblici + "&pluginInvoicex=" + pluginInvoicex + (iu.isUdev() ? "&d=1" : ""));
        System.out.println("ret    : " + ret);

        JSONArray jsa = (JSONArray) new JSONParser().parse(ret);
        for (Object o : jsa) {
            JSONObject jso = (JSONObject) o;

            Plugin2 p = new Plugin2();
            p.setNome_breve(cu.s(jso.get("name")));
            p.setNome_lungo(cu.s(jso.get("tips")));
            p.setVersione(cu.s(jso.get("ver")));
            p.setData_creazione(null);
            p.setData_ultima_modifica(cu.toDateIta(cu.s(jso.get("data"))));
            p.props = new HashMap();
            if (jso.containsKey("privato")) {
                p.props.put("privato", cu.s(jso.get("privato")).equalsIgnoreCase("true") ? true : false);
            }
            p.props.put("ordinamento", cu.i0(jso.get("ordinamento")));
            if (cu.i0(p.props.get("ordinamento")) == 0) {
                p.props.put("ordinamento", 1000);
            }
            p.props.put("pack", cu.s(jso.get("pack")));

            list.add(p);
        }

        return list;
    }

    public static boolean isWindowsNetworkDrive(String drive) {
        //drive ad esempio: "C:"
        boolean ret = false;
        try {
            File file = File.createTempFile("invoicex-drivetype", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);
            String vbs = IOUtils.toString(SystemUtils.class.getClass().getResourceAsStream("/it/tnx/invoicex/drivetype.vbs"));
            vbs = StringUtils.replace(vbs, "{drive}", drive);
            fw.write(vbs);
            fw.close();
            String cmd = "cscript //NoLogo " + file.getPath();
            System.out.println("cmd = " + cmd);
            ProcessBuilder pb = new ProcessBuilder("cscript", "//NoLogo", file.getPath());
            Process p = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                System.out.println("line = " + line);
                if (line.indexOf("DeviceID") >= 0 && line.indexOf("=network") >= 0) {
                    ret = true;
                    break;
                }
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static class AllegatiCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setHorizontalAlignment(SwingConstants.CENTER);
            if (CastUtils.toInteger0(value) > 0) {
                c.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/attach.png")));
            } else {
                c.setIcon(null);
            }
            c.setText("");
            return c;
        }
    }

    public static String getWhereFiltri(List<Map> filters, String tipoDoc) {
        String w = "";
        if (filters == null) {
            return "";
        }
        for (Map m : filters) {
            String campo = cu.s(m.get("campo"));
            String valore = cu.s(m.get("valore"));

            if (campo.equals("cons_prev_dal")) {
                w += " and t.data_consegna_prevista >= " + dbu.sql(cu.toDateIta(valore));
            } else if (campo.equals("cons_prev_al")) {
                w += " and t.data_consegna_prevista <= " + dbu.sql(cu.toDateIta(valore));
            } else if (campo.equals("doc_est_dal")) {
                w += " and t.data_doc >= " + dbu.sql(cu.toDateIta(valore));
            } else if (campo.equals("doc_est_al")) {
                w += " and t.data_doc <= " + dbu.sql(cu.toDateIta(valore));
            } else if (campo.equals("note") || campo.equals("riferimento") || campo.equals("dest_ragione_sociale")) {
                w += " and t." + campo + " like " + dbu.sql("%" + valore + "%");
            } else if (campo.equals("categoria_cliente")) {
                w += " and clie_forn.tipo2 = " + dbu.sql(valore);
            } else if (campo.equals("cap")) {
                w += " and clie_forn.cap = " + dbu.sql(valore);
            } else if (campo.equals("prov")) {
                w += " and clie_forn.provincia = " + dbu.sql(valore);
            } else if (campo.equals("paese")) {
                w += " and clie_forn.paese = " + dbu.sql(valore);
            } else {
                w += " and t." + campo + " = " + dbu.sql(valore);
            }
        }
        System.out.println("sql where filtri = " + w);
        return w;
    }

    public static String getWhereFiltriClieForn(List<Map> filters) {
        String w = "";
        if (filters == null) {
            return "";
        }
        for (Map m : filters) {
            String campo = cu.s(m.get("campo"));
            String valore = cu.s(m.get("valore"));

            if (campo.equals("categoria")) {
                w += " and cf.tipo2 = " + dbu.sql(valore);
            } else if (campo.equals("piva_cfiscale")
                    || campo.equals("cfiscale")
                    || campo.equals("ragione_sociale")
                    || campo.equals("indirizzo")
                    || campo.equals("cfiscale")
                    || campo.equals("cap")
                    || campo.equals("localita")
                    || campo.equals("email")
                    || campo.equals("note")) {
                w += " and cf." + campo + " like " + dbu.sql("%" + valore + "%");
            } else {
                w += " and cf." + campo + " = " + dbu.sql(valore);
            }
        }
        System.out.println("sql where filtri = " + w);
        return w;
    }

    public static String getWhereFiltriArticoli(List<Map> filters) {
        String w = "";
        if (filters == null) {
            return "";
        }
        for (Map m : filters) {
            String campo = cu.s(m.get("campo"));
            String valore = cu.s(m.get("valore"));

            if (campo.equals("descrizione")
                    || campo.equals("descrizione_en")
                    || campo.equals("posizione_magazzino")) {
                w += " and art." + campo + " like " + dbu.sql("%" + valore + "%");
            } else if (campo.equals("flag_kit")
                    || campo.equals("gestione_matricola")
                    || campo.equals("gestione_lotti")
                    || campo.equals("is_descrizione")
                    || campo.equals("servizio")) {
                w += " and IFNULL(art." + campo + ",'') " + (cu.toBoolean(m.get("valore")) ? " = 'S'" : " != 'S'");
            } else if (campo.equals("tipo")) {
                w += " and (IFNULL(art." + campo + ",'') = " + dbu.sql(valore) + " or IFNULL(art." + campo + ",'') = 'E')";
            } else if (campo.equals("codice_fornitore")) {
                w += " and (";
                w += " IFNULL(art." + campo + ",'') = " + dbu.sql(valore);
                w += " or IFNULL(art.codice_fornitore2,'') = " + dbu.sql(valore);
                w += " or IFNULL(art.codice_fornitore3,'') = " + dbu.sql(valore);
                w += " or IFNULL(art.codice_fornitore4,'') = " + dbu.sql(valore);
                w += " or IFNULL(art.codice_fornitore5,'') = " + dbu.sql(valore);
                w += " or IFNULL(art.codice_fornitore6,'') = " + dbu.sql(valore);
                w += ")";
            } else {
                w += " and art." + campo + " = " + dbu.sql(valore);
            }
        }
        System.out.println("sql where filtri = " + w);
        return w;
    }

    public static void checkGTKLookAndFeel() throws Exception {
        LookAndFeel look = UIManager.getLookAndFeel();
        if (!look.getID().equals("GTK")) {
            return;
        }

        new JFrame();
        new JButton();
        new JComboBox();
        new JRadioButton();
        new JCheckBox();
        new JTextArea();
        new JTextField();
        new JTable();
        new JToggleButton();
        new JSpinner();
        new JSlider();
        new JTabbedPane();
        new JMenu();
        new JMenuBar();
        new JMenuItem();

        Object styleFactory;
        Field styleFactoryField = look.getClass().getDeclaredField("styleFactory");
        styleFactoryField.setAccessible(true);
        styleFactory = styleFactoryField.get(look);

        Field defaultFontField = styleFactory.getClass().getDeclaredField("defaultFont");
        defaultFontField.setAccessible(true);
        Font defaultFont = (Font) defaultFontField.get(styleFactory);
        FontUIResource newFontUI;
        newFontUI = new FontUIResource(defaultFont.deriveFont((float) (defaultFont.getSize() - 2f)));
        defaultFontField.set(styleFactory, newFontUI);

        Field stylesCacheField = styleFactory.getClass().getDeclaredField("stylesCache");
        stylesCacheField.setAccessible(true);
        Object stylesCache = stylesCacheField.get(styleFactory);
        Map stylesMap = (Map) stylesCache;
        for (Object mo : stylesMap.values()) {
            Field f = mo.getClass().getDeclaredField("font");
            f.setAccessible(true);
            Font fo = (Font) f.get(mo);
            f.set(mo, fo.deriveFont((float) (fo.getSize() - 2f)));
        }
    }

    public static void genericFormAddCliente(GenericFrmTest parent) {
        JInternalFrame frm = null;
        if (main.getPersonalContain("vecchia_clifor")) {
            frm = new frmClie();
        } else {
            frm = new JInternalFrameClientiFornitori();
        }
        main.getPadrePanel().openFrame(frm, 800, InvoicexUtil.getHeightIntFrame(660));
        try {
            Method m = frm.getClass().getDeclaredMethod("addNew", GenericFrmTest.class);
            m.invoke(frm, parent);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void genericFormEditCliente(GenericFrmTest parent, String clie_forn) {
        JInternalFrame frm = null;
        if (main.getPersonalContain("vecchia_clifor")) {
            frm = new frmClie();
        } else {
            frm = new JInternalFrameClientiFornitori();
        }

        main.getPadrePanel().openFrame(frm, 800, InvoicexUtil.getHeightIntFrame(660));

        try {
            Method m = frm.getClass().getDeclaredMethod("edit", GenericFrmTest.class, String.class);
            m.invoke(frm, parent, clie_forn);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static String[] getPaesiEU() {
        //http://europa.eu/about-eu/countries/index_it.htm
        return new String[]{"IT", "AT", "BE", "BG", "CY", "HR", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IE", "LV", "LT", "LU", "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE", "GB"};
    }

    public static String getPaesiEUSqlIn() {
        String ret = "";
        for (String s : getPaesiEU()) {
            ret += "'" + s + "',";
        }
        ret = StringUtils.chop(ret);
        return ret;
    }

    public static void segnala(final String segnalazione) {
        Thread tsegnala = new Thread("segnala") {

            @Override
            public void run() {
                String versione = main.fileIni.getValue("cache", "versione", "Base");
                try {
                    Attivazione attivazione = new Attivazione();
                    DatiAzienda dati = attivazione.getDatiAzienda();
                    String surl = main.baseurlserver + "/m.php?";
                    surl += "&ver=" + URLEncoder.encode(main.version.toString()) + "";
                    surl += "&ver2=" + URLEncoder.encode(main.version.toString() + " " + main.build.toString()) + "";
                    surl += "&email=" + URLEncoder.encode("m.ceccarelli@tnx.it") + "";
                    surl += "&id_registrazione=" + URLEncoder.encode(it.tnx.Util.nz(main.attivazione.getIdRegistrazione())) + "";
                    surl += "&ragione_sociale=" + URLEncoder.encode(dati.getRagione_sociale(), "ISO-8859-1") + "";
                    surl += "&telefono=" + URLEncoder.encode(dati.getTelefono()) + "";
                    surl += "&os=" + URLEncoder.encode(System.getProperty("os.name"));
                    surl += "&versione=debug";
                    surl += "&msg=" + URLEncoder.encode(segnalazione, "ISO-8859-1");
                    URL url = new URL(surl);
                    System.out.println("url:" + url);

                    String content = HttpUtils.getUrlToStringUTF8(surl);

                    System.out.println("m: response: " + content);
                    if (content.equals("OK")) {
                        System.out.println("segnalazione inviata");
                    } else {
                        System.out.println("errore nell'invio della segnalazione: " + content);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        tsegnala.start();

    }

    static public void aggiornaSplit(JPanel panel, JSplitPane split) {
        panel.validate();

        int maxh = 0;
        Component cmax = null;
        for (Component c : panel.getComponents()) {
            if (c.isVisible() && (c.getLocation().getY() + c.getSize().getHeight()) > maxh && !(c instanceof JSeparator)) {
                cmax = c;
                maxh = (int) (c.getLocation().getY() + c.getSize().getHeight());
            }
        }
        Point p = new Point(0, 0);
        Point p2 = SwingUtilities.convertPoint(panel, p, split);
        maxh += p2.y;

        split.setDividerLocation(maxh + 6);
    }

    static public DisabledGlassPane disabilitaFrame(Component frame, String titolo) {
        DisabledGlassPane glassPane = new DisabledGlassPane();
        JRootPane rootPane = SwingUtilities.getRootPane(frame);
        rootPane.setGlassPane(glassPane);
        glassPane.activate(titolo != null ? titolo : "Attendere prego...");

        return glassPane;
    }

    static public void abilitaFrame(Component frame) {
        JRootPane rootPane = SwingUtilities.getRootPane(frame);
        Object oglass = rootPane.getGlassPane();
        if (oglass instanceof DisabledGlassPane) {
            ((DisabledGlassPane) oglass).deactivate();
        }
    }
    
    public static void impostaRigaSopraSotto(tnxDbGrid griglia, JMenuItem popGridAdd, JPopupMenu popGrig, MouseEvent evt) {
        impostaRigaSopraSotto(griglia, popGridAdd, popGrig, evt, null);
    }
    public static void impostaRigaSopraSotto(tnxDbGrid griglia, JMenuItem popGridAdd, JPopupMenu popGrig, MouseEvent evt, JMenuItem popGridAddSub) {
        int numRow = griglia.getSelectedRow();
        if (numRow != 0) {
            int rigasopra = cu.i0(griglia.getValueAt(griglia.getSelectedRow()-1, griglia.getColumnByName("riga")));
            int rigasotto = cu.i0(griglia.getValueAt(griglia.getSelectedRow(), griglia.getColumnByName("riga")));
            popGridAdd.setText("Inserisci nuova fra riga " + rigasopra + " e riga " + rigasotto);
            if (popGridAddSub != null) {
                popGridAddSub.setEnabled(true);
                popGridAddSub.setText("Inserisci Sub-Totale fra riga " + rigasopra + " e riga " + rigasotto);
            }
        } else {
            popGridAdd.setText("Inserisci nuova riga all'inizio");
            if (popGridAddSub != null) {
                popGridAddSub.setEnabled(false);
                popGridAddSub.setText("Non puoi inserire Sub-Totale come prima riga");
            }
        }
        popGrig.show(griglia, evt.getX(), evt.getY());
    }

    public static String getNomeUtente(int id_utente) throws Exception {
        return cu.s(dbu.getObject(Db.getConn(), "select username from accessi_utenti where id = " + dbu.sql(id_utente)));
    }

}
