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

import com.jidesoft.hints.AbstractListIntelliHints;
import com.jidesoft.swing.SelectAllUtils;
import gestioneFatture.logic.*;

import gestioneFatture.logic.clienti.Cliente;
import it.tnx.Db;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.FxUtils;
import it.tnx.commons.KeyValuePair;
import it.tnx.commons.MicroBench;
import it.tnx.commons.StringUtilsTnx;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.gui.DateDocument;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.Magazzino;
import it.tnx.invoicex.data.Giacenza;
import it.tnx.invoicex.gui.JDialogLotti;
import it.tnx.invoicex.gui.JDialogMatricole;
import it.tnx.invoicex.gui.JDialogMatricoleLotti;
import it.tnx.invoicex.gui.JDialogPrezzi;
import it.tnx.invoicex.gui.JPanelScatole;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingworker.SwingWorker;
import sas.swing.plaf.MultiLineLabelUI;
import tnxbeans.tnxComboField;
import tnxbeans.tnxDbPanel;

public class frmNuovRigaDescrizioneMultiRigaNew
        extends javax.swing.JInternalFrame {

    public Object from;
    public String dbStato;
    public String dbSerie;
    public String prevStato;
    public int dbNumero;
    public int dbRiga;
    public int dbRigaVariante;
    public int dbAnno;
    String codiceListino; //indica il listino da usare (per ora 1 o 2)
    String listinoUpdate; // indica il listino da aggiornare
    public int codiceCliente; //indica il cliente a cui si sta facendo il documento
    Object comCodiArti_old = null;
    double peso_kg_collo = 0;
    boolean f4 = false;
    AggiornaResiduaWorker aggiornaResidua = null;
    AbstractListIntelliHints alRicerca = null;
    Integer id_padre = null;
    public Integer id_riga = null;
    boolean recuperando = false;
    boolean acquisto = false;
    double totale_ivato = 0d;
    double totale_imponibile = 0d;
    double cliente_sconto1r = 0;
    double cliente_sconto2r = 0;
    boolean prezzi_ivati = false;
    frmTestFatt from_frmTestFatt = null;
    frmTestFattAcquisto from_frmTestFattAcquisto = null;
    frmTestDocu from_frmTestDocu = null;
    frmTestOrdine from_frmTestOrdine = null;
    public String tipoDocumento = null;
    String litristring = "\n\001litri";
    boolean caricamento = true;

    double tot_senza_iva = 0;
    double tot_con_iva = 0;

    public Integer deposito = null;

    /**
     * Creates new form frmNuovRiga
     */
//    public frmNuovRigaDescrizioneMultiRigaNew(Object from, String dbStato, String dbSerie, int dbNumero, String stato, int dbRiga, int dbAnno, String codiceListino, int codiceCliente) throws IOException {
//        this(from, dbStato, dbSerie, dbNumero, stato, dbRiga, dbAnno, codiceListino, codiceCliente, null, null);
//    }
    public void aggiornaLabConta() {
        labConta.setText("(" + cu.s(texDescrizione.getText().length()) + " caratteri)");
    }

    public frmNuovRigaDescrizioneMultiRigaNew(Object from, String dbStato, String dbSerie, int dbNumero, String stato, int dbRiga, int dbAnno, String codiceListino, int codiceCliente, Integer id_riga, Integer id_padre, String table_righe_temp) throws IOException {
        this.from = from;
        this.dbStato = dbStato;
        this.dbSerie = dbSerie;
        this.dbNumero = dbNumero;
        this.dbRiga = dbRiga;
        this.dbRigaVariante = dbRigaVariante;
        this.prevStato = stato;
        this.dbAnno = dbAnno;
        this.codiceListino = codiceListino;
        this.listinoUpdate = codiceListino;
        this.codiceCliente = codiceCliente;

        //texRicerca
        initComponents();

        texDescrizione.getJTextArea().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                aggiornaLabConta();
            }

            public void removeUpdate(DocumentEvent e) {
                aggiornaLabConta();
            }

            public void changedUpdate(DocumentEvent e) {
                aggiornaLabConta();
            }
        });

        butCalcolaSconto.putClientProperty("JButton.buttonType", "textured");
        comIva.putClientProperty("JButton.buttonType", "textured");
        scatole.putClientProperty("JButton.buttonType", "textured");
        prezzi_prec.putClientProperty("JButton.buttonType", "textured");

        DateDocument.installDateDocument(consegna_prevista.getEditor());

        boolean attiva_carica = false;
        if (from instanceof frmTestDocu) {
            from_frmTestDocu = (frmTestDocu) from;
            tipoDocumento = from_frmTestDocu.acquisto ? Db.TIPO_DOCUMENTO_DDT_ACQUISTO : Db.TIPO_DOCUMENTO_DDT;
            if (from_frmTestDocu.prezzi_ivati.isSelected()) {
                prezzi_ivati = true;
            }
            if (tipoDocumento.equals(Db.TIPO_DOCUMENTO_DDT)) {
                attiva_carica = true;
            }
            try {
                tnxComboField combo_deposito = (tnxComboField) SwingUtils.getCompByNameRicorsivo(from_frmTestDocu.datiAltro, "deposito");
                if (combo_deposito != null) {
                    deposito = cu.i(combo_deposito.getSelectedKey());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (from instanceof frmTestFatt) {
            from_frmTestFatt = (frmTestFatt) from;
            tipoDocumento = Db.TIPO_DOCUMENTO_FATTURA;
            if (from_frmTestFatt.prezzi_ivati.isSelected()) {
                prezzi_ivati = true;
            }
            if (from_frmTestFatt.dbdoc.tipoFattura == dbFattura.TIPO_FATTURA_ACCOMPAGNATORIA) {
                attiva_carica = true;
            }
            try {
                tnxComboField combo_deposito = (tnxComboField) SwingUtils.getCompByNameRicorsivo(from_frmTestFatt.datiAltro, "deposito");
                if (combo_deposito != null) {
                    deposito = cu.i(combo_deposito.getSelectedKey());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (from instanceof frmTestFattAcquisto) {
            from_frmTestFattAcquisto = (frmTestFattAcquisto) from;
            tipoDocumento = Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA;
            if (from_frmTestFattAcquisto.prezzi_ivati.isSelected()) {
                prezzi_ivati = true;
            }
            try {
                tnxComboField combo_deposito = (tnxComboField) SwingUtils.getCompByNameRicorsivo(from_frmTestFattAcquisto.datiAltro, "deposito");
                if (combo_deposito != null) {
                    deposito = cu.i(combo_deposito.getSelectedKey());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (from instanceof frmTestOrdine) {
            from_frmTestOrdine = (frmTestOrdine) from;
            tipoDocumento = from_frmTestOrdine.acquisto ? Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO : Db.TIPO_DOCUMENTO_ORDINE;
            if (from_frmTestOrdine.prezzi_ivati.isSelected()) {
                prezzi_ivati = true;
            }
            try {
                tnxComboField combo_deposito = (tnxComboField) SwingUtils.getCompByNameRicorsivo(from_frmTestOrdine.datiAltro, "deposito");
                if (combo_deposito != null) {
                    deposito = cu.i(combo_deposito.getSelectedKey());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (main.getPersonalContain("autocarico") && attiva_carica) {
            butCaricaESalva.setVisible(true);
        } else {
            butCaricaESalva.setVisible(false);
        }

        if (!main.edit_doc_in_temp) {
            if (id_padre == null || id_padre == -1) {
                id_padre = InvoicexUtil.getIdDaNumero(tipoDocumento, dbSerie, dbNumero, dbAnno);
            }
        }
        this.id_padre = id_padre;
        this.id_riga = id_riga;

        if (!main.getPersonalContain("no-colori-iva")) {
            Color newc = SwingUtils.mixColours(javax.swing.UIManager.getDefaults().getColor("TextField.foreground"), new Color(120, 120, 120));
            if (prezzi_ivati) {
                texPrez.setForeground(newc);
            } else {
                texPrezIvato.setForeground(newc);
            }
        }

        if (!main.fileIni.getValueBoolean("pref", "attivaArrotondamento", false)) {
            labArrotondamento.setVisible(false);
            comArrotondamento.setVisible(false);
            comTipoArr.setVisible(false);
            labTotArr.setVisible(false);
            texTotArrotondato.setVisible(false);
        }
        // Attivo/Disattivo flag per aggiornamento automatico listini
        boolean updateListino = main.fileIni.getValueBoolean("pref", "updateListini", false);
        if (updateListino) {
            cheUpdateListino.setSelected(true);
        } else {
            String sqlFlagListino = "SELECT flag_update_listino FROM clie_forn WHERE codice = '" + codiceCliente + "'";
            String flag = "N";
            try {
                flag = CastUtils.toString(DbUtils.getObject(Db.getConn(), sqlFlagListino));
                cheUpdateListino.setSelected(flag.equals("S"));
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

        scatole.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/mimetypes/package-x-generic.png")));
        texIvaDeducibile.setVisible(false);
        labIvaDeducibile.setVisible(false);
        labPercentualeIvaDeducibile.setVisible(false);

        comArrotondamento.dbAddElement("0");
        comArrotondamento.dbAddElement("0.10");
        comArrotondamento.dbAddElement("0.50");
        comArrotondamento.dbAddElement("1");
        comArrotondamento.dbAddElement("5");
        comArrotondamento.dbAddElement("10");

        comTipoArr.dbAddElement("Inf.");
        comTipoArr.dbAddElement("Sup.");

        if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestDocu")) {
        } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFatt")) {
        } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestOrdine")) {
        } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFattAcquisto")) {
            texIvaDeducibile.setVisible(true);
            labIvaDeducibile.setVisible(true);
            labPercentualeIvaDeducibile.setVisible(true);
            dati.remove(texProvvigione);
            dati.remove(jLabel23);
            dati.remove(jLabel24);
        }

        if (!main.getPersonalContain("unifish") || tipoDocumento.equals(Db.TIPO_DOCUMENTO_DDT_ACQUISTO) || tipoDocumento.equals(Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA) || tipoDocumento.equals(Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO)) {
            dati.remove(labNumeroScatole);
            dati.remove(texNumeroScatole);
        }

        main.events.fireInvoicexEvent(new InvoicexEvent(this, InvoicexEvent.TYPE_FRMNuovRigaDescrizioneMultiRigaNew_POST_INIT_COMPS));

        //test con attesa
        alRicerca = new AbstractListIntelliHints(texRicerca) {
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
                        return size < super.getVisibleRowCount() ? size : super.getVisibleRowCount();
                    }

                    @Override
                    public Dimension getPreferredScrollableViewportSize() {
//                        if (getModel().getSize() == 0) {
//                            System.err.println("getPreferredScrollableViewportSize dim 0");
//                            return new Dimension(0, 0);
//                        }
//                        else {
//                            System.err.println("getPreferredScrollableViewportSize " + super.getPreferredScrollableViewportSize());
//                            return super.getPreferredScrollableViewportSize();
//                        }

                        Insets insets = getInsets();
                        int dx = insets.left + insets.right;
                        int dy = insets.top + insets.bottom;

                        int visibleRowCount = getVisibleRowCount();
                        int fixedCellWidth = getFixedCellWidth();
                        int fixedCellHeight = getFixedCellHeight();

                        if ((fixedCellWidth > 0) && (fixedCellHeight > 0)) {
                            int width = fixedCellWidth + dx;
                            int height = (visibleRowCount * fixedCellHeight) + dy;
                            return new Dimension(width, height);
                        } else if (getModel().getSize() > 0) {
                            int width = getPreferredSize().width;
                            int height, height_frame;
//                            java.awt.Rectangle r = getCellBounds(0, 0);
//                            if (r != null) {
//                                height = (visibleRowCount * r.height) + dy;
//                            } else {
//                                // Will only happen if UI null, shouldn't matter what we return
//                                height = 1;
//                            }
                            int size = getModel().getSize();
                            height_frame = frmNuovRigaDescrizioneMultiRigaNew.this.getHeight() - 100;
                            if (size < super.getVisibleRowCount()) {
                                java.awt.Rectangle r = getCellBounds(0, visibleRowCount - 1);
                                height = r.height;
                                if (height > height_frame) {
                                    height = height_frame;
                                }
                            } else {
                                height = height_frame;
                            }
                            return new Dimension(width, height);
                        } else {
                            fixedCellWidth = (fixedCellWidth > 0) ? fixedCellWidth : 256;
                            fixedCellHeight = (fixedCellHeight > 0) ? fixedCellHeight : 16;
                            return new Dimension(fixedCellWidth, fixedCellHeight * visibleRowCount);
                        }
                    }
                };

                System.err.println("frmNuovRiga... createList setfixedcellwidth " + (getWidth() - 50));
                list.setFixedCellWidth(getWidth() - 50);

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

//                            StyledLabel lab2 = StyledLabelBuilder.createStyledLabel(StyledLabelBuilder.parseToVoidStyledTextConfusion(tipo) + " @rows");
//                            lab2.setBorder(myborder);
//                            lab2.setBackground(lab.getBackground());
//                            lab2.setForeground(lab.getForeground());
//                            lab2.setOpaque(lab.isOpaque());
//                            lab2.setFont(lab.getFont());
//                            return lab2;
                        } else {
                            lab = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        }

//                        String word = current_search.toLowerCase();
//                        String content = tipo.toLowerCase();
//                        Color c = lab.getBackground();
//                        c = c.darker();
//                        String rgb = Integer.toHexString(c.getRGB());
//                        rgb = rgb.substring(2, rgb.length());
//                        content = StringUtils.replace(content, word, "<span style='background-color: " + rgb + "'>" + word + "</span>");
//                        lab.setText("<html>" + content + "</html>");
                        return lab;
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);

//			String filter = current_search.toLowerCase();
//			FontMetrics fm = getFontMetrics( getFont() );
//			Rectangle2D bounds = fm.getStringBounds( getText().toLowerCase().substring( 0, getText().toLowerCase().indexOf( filter ) ), g );
//			Rectangle2D filterBounds = fm.getStringBounds( filter, g );
//
//			int x = (int) ( bounds.getX() + bounds.getWidth() );
//			Dimension d = getPreferredSize();
//                        g.setColor(Color.red);
//			g.drawLine( x, fm.getHeight()-2, (int) ( x + filterBounds.getWidth() ), fm.getHeight()-2 );
//                        g.drawLine( x, fm.getHeight()-1, (int) ( x + filterBounds.getWidth() ), fm.getHeight()-1 );
                    }
                });
                return list;
            }
            SwingWorker lastw = null;

            public boolean updateHints(Object arg0) {
                if (arg0.toString().trim().length() <= 0) {
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
                                showHints2();
                            }
                        } catch (CancellationException cex) {
                        } catch (Exception ex) {
                            System.err.println("set listdata ex " + ex.getMessage());
                            setListData(new String[]{ex.getMessage()});
                            showHints2();
                        }
                    }
                };

                w.execute();
                lastw = w;

                return true;
            }

            @Override
            public void acceptHint(Object arg0) {
//                super.acceptHint(arg0);
                try {
                    texCodiArti.setText(((ArticoloHint) arg0).codice);
                    recuperaDatiArticolo();
                    texQta.requestFocus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private String codice_fornitore(ResultSet rs, String cosa) {
                String ret = "";
//                Integer[] ids = new Integer[]{4, 6, 7, 8, 9, 10};
                //+ "SELECT a.codice, a.descrizione, IFNULL(a.codice_fornitore,''), IFNULL(a.codice_a_barre,''), IFNULL(a.codice_fornitore2,''), IFNULL(a.codice_fornitore3,''), IFNULL(a.codice_fornitore4,''), IFNULL(a.codice_fornitore5,''), IFNULL(a.codice_fornitore6,'') FROM articoli a"
                //              1          2                      3                            4                             5                               6                                    7                        8                            9
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

//        comCodiArti.setAzzeraTextAbbinato(false);
        try {
            UIDefaults uiDefaults = UIManager.getDefaults();
            texDescrizione.getJTextArea().setFont((Font) uiDefaults.get("TextField.font"));
            if (main.getPersonalContain("conenna") && from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFatt")) {
                texDescrizione.getJTextArea().setFont(new Font("Courier New", 0, 11));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        JTextField textCodiArti = (JTextField) comCodiArti.getComponent(2);
//        textCodiArti.addFocusListener(new FocusListener() {
//
//            public void focusGained(FocusEvent e) {
//                comCodiArtiFocusGained(e);
//            }
//
//            public void focusLost(FocusEvent e) {
//            }
//        });
        //this.texPrezNett.setFont(new java.awt.Font(texPrezNett.getFont().getFamily(), texPrezNett.getFont().getSize(), java.awt.Font.ITALIC));
//        this.comCodiArti.setDbRiempireForceText(true);
        //associo il panel ai dati
        if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestDocu")) {
            frmTestDocu prov = (frmTestDocu) from;
            dati.dbNomeTabella = "righ_ddt" + prov.suff;
        } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFatt")) {
            dati.dbNomeTabella = "righ_fatt";
        } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestOrdine")) {
            frmTestOrdine prov = (frmTestOrdine) from;
            dati.dbNomeTabella = "righ_ordi" + prov.suff;
        } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFattAcquisto")) {
            dati.dbNomeTabella = "righ_fatt_acquisto";
            acquisto = true;
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "non trovata form partenza");
        }

        if (table_righe_temp != null) {
            dati.dbNomeTabella = table_righe_temp;
        }

        dati.ignora_select_single_dopo_save = true;

        String wta = "";
        if (acquisto) {
            wta = " where tipo = 'A' or tipo = '' or tipo is null";
        } else {
            wta = " where tipo = 'V' or tipo = '' or tipo is null";
        }

        Vector chiave = new Vector();
        if (id_riga != null || dbStato.equals(tnxDbPanel.DB_INSERIMENTO)) {
            chiave.add("id");
            this.dati.dbChiaveAutoInc = true;
        } else {
            System.out.println("****************** frmNuovRiga chiave senza id");
            chiave.add("serie");
            chiave.add("numero");
            chiave.add("anno");
            chiave.add("riga");
            chiave.add("stato");
        }
        this.dati.dbChiave = chiave;

        this.dati.butSave = this.butSave;

        //this.dati.butUndo = this.butUndo;
        //this.dati.butFind = this.butFind;
        //109 faccio per lingua
        //carico elenchi unit??? di misura e articoli
        boolean eng = false;
        if (this.codiceCliente >= 0) {
            Cliente cliente = new Cliente(this.codiceCliente);
            cliente_sconto1r = CastUtils.toDouble0(cliente.getObject("sconto1r"));
            cliente_sconto2r = CastUtils.toDouble0(cliente.getObject("sconto2r"));
            if (cliente.isItalian() == true) {
                eng = false;
            } else if (!main.fileIni.getValueBoolean("pref", "soloItaliano", true)) {
                eng = true;
            }
        }
        if (eng == true) {
            this.comUm.dbOpenList(Db.getConn(), "select um_en from articoli group by um_en", null, false);
        } else {
            this.comUm.dbOpenList(Db.getConn(), "select um from articoli group by um", null, false);
        }

        this.texAnno.setText(String.valueOf(dbAnno));

        //mette il focus dopo tutti gli eventi awt
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
//                texCodiArti.requestFocus();
                texRicerca.requestFocus();
                SelectAllUtils.install(texRicerca);
            }
        });

        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                if (aComponent == comUm) {
                    return texDescrizione.getJTextArea();
                }
                if (aComponent.getParent() == comUm) {
                    return texDescrizione.getJTextArea();
                }
                if (aComponent == texDescrizione) {
                    return texRicerca;
                }
                if (aComponent == texDescrizione.getJTextArea()) {
                    return texRicerca;
                }

                if (aComponent == texPrezNett) {
                    if (prezzi_ivati) {
                        return texPrezIvato;
                    } else {
                        return texPrez;
                    }
                }
                return super.getComponentBefore(aContainer, aComponent);
            }

            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                System.out.println("getComponentAfter " + aContainer + " " + aComponent);
//                if (aComponent == texCodiArti) {
//                    return comCodiArti;
//                }
//                if (aComponent == comCodiArti) {
//                    return texDescrizione.getJTextArea();
//                }
//                if (aComponent.getParent() == comCodiArti) {
//                    return texDescrizione.getJTextArea();
//                }
                if (aComponent == texRicerca) {
                    return texDescrizione.getJTextArea();
                }
                if (aComponent == texDescrizione) {
                    return comUm;
                }
                if (aComponent == texDescrizione.getJTextArea()) {
                    return comUm;
                }
                if (aComponent == comUm) {
                    return texQta;
                }
                if (aComponent.getParent() == comUm) {
                    return texQta;
                }
                if (aComponent == texQta) {
                    if (prezzi_ivati) {
                        return texPrezIvato;
                    } else {
                        return texPrez;
                    }
                }
                if (aComponent == texPrez) {
                    if (prezzi_ivati) {
                        return texPrezIvato;
                    } else {
                        return texPrezNett;
                    }
                }
                if (aComponent == texPrezIvato) {
                    return texPrezNett;
                }
                if (aComponent == texPrezNett) {
                    return texScon1;
                }
                if (aComponent == texScon1) {
                    return texScon2;
                }
                if (aComponent == texScon2) {
                    return texIva;
                }
                if (aComponent == texIva) {
                    return butAnnulla;
                }
                if (aComponent == butAnnulla) {
                    return butSave;
                }
                return super.getComponentAfter(aContainer, aComponent);
            }
        });

        getRootPane().setDefaultButton(butSave);

    }

    private void calcolaSconto() {
        if (CastUtils.toDouble0(texPrez.getText()) == 0 && CastUtils.toDouble0(texPrezNett.getText()) != 0) {
            texPrez.setText(texPrezNett.getText());
        }
        if (StringUtilsTnx.isNumber(texPrez.getText()) && StringUtilsTnx.isNumber(texPrezNett.getText())) {
            //calcola lo sconto
            try {
                java.text.NumberFormat form = java.text.NumberFormat.getInstance();
                this.texScon1.setText(Db.formatNumero(Util.getSconto(form.parse(texPrez.getText()).doubleValue(), form.parse(texPrezNett.getText()).doubleValue())));
                if (cu.i0(texScon2.getText()) != 0) {
                    texScon2.setText("0");
                }
            } catch (Exception err) {
            }
        }
    }

    private void calcolaScontoDaPercSconto() {
        if (StringUtilsTnx.isNumber(texPrez.getText()) && (StringUtilsTnx.isNumber(texScon1.getText()) || StringUtilsTnx.isNumber(texScon2.getText()))) {
            //calcola lo sconto
            try {
                double prez = StringUtilsTnx.parseDoubleOrZero(texPrez.getText());
                double sco1 = StringUtilsTnx.parseDoubleOrZero(texScon1.getText());
                double sco2 = StringUtilsTnx.parseDoubleOrZero(texScon2.getText());
                double val = prez - (prez / 100d * sco1) - ((prez - (prez / 100d * sco1)) / 100 * sco2);
                texPrezNett.setText(FormatUtils.formatEuroItaMax5(val));
            } catch (Exception err) {
            }
        }
    }

    private void showPrezziFatture() {
        String codiceArticolo = this.texCodiArti.getText();
        if (this.codiceCliente >= 0) {
            try {
                String tipo = null;
                if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestDocu")) {
                    tipo = Db.TIPO_DOCUMENTO_DDT;
                    try {
                        if (((GenericFrmTest) from).isAcquisto()) {
                            tipo = Db.TIPO_DOCUMENTO_DDT_ACQUISTO;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFatt")) {
                    tipo = Db.TIPO_DOCUMENTO_FATTURA;
                } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestOrdine")) {
                    tipo = Db.TIPO_DOCUMENTO_ORDINE;
                    try {
                        if (((GenericFrmTest) from).isAcquisto()) {
                            tipo = Db.TIPO_DOCUMENTO_ORDINE_ACQUISTO;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFattAcquisto")) {
                    tipo = Db.TIPO_DOCUMENTO_FATTURA_RICEVUTA;
                } else {
                    SwingUtils.showInfoMessage(this, "Tipo '" + from + "' non gestito");
                }
                frmPrezziFatturePrecedenti form = new frmPrezziFatturePrecedenti(this.codiceCliente, codiceArticolo, tipo);
                main.getPadre().openFrame(form, this.getWidth(), 200, getY() + (getHeight() / 2), getX());
            } catch (Exception err) {
                err.printStackTrace();
                Util.showErrorMsg(this, err);
            }
        } else {
            System.err.println("errore codice cliente = " + this.codiceCliente);
        }
    }

    /**
     * This method is called from within the constructor to
     *
     * initialize the form.
     *
     * WARNING: Do NOT modify this code. The content of this method is
     *
     * always regenerated by the Form Editor.
     *
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        dati = new tnxbeans.tnxDbPanel();
        texScon1 = new tnxbeans.tnxTextField();
        texSeri = new tnxbeans.tnxTextField();
        texCodiArti = new tnxbeans.tnxTextField();
        texQta = new tnxbeans.tnxTextField();
        texPrezIvato = new tnxbeans.tnxTextField();
        texIva = new tnxbeans.tnxTextField();
        texScon2 = new tnxbeans.tnxTextField();
        texStat = new tnxbeans.tnxTextField();
        texStat.setVisible(false);
        comUm = new tnxbeans.tnxComboField();
        texNume = new tnxbeans.tnxTextField();
        texRiga = new tnxbeans.tnxTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel110 = new javax.swing.JLabel();
        texAnno = new tnxbeans.tnxTextField();
        texAnno.setVisible(false);
        texPrezNett = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        prezzi_prec = new javax.swing.JButton();
        labPercentualeIva = new javax.swing.JLabel();
        comIva = new javax.swing.JButton();
        butCalcolaSconto = new javax.swing.JButton();
        texDescrizione = new tnxbeans.tnxMemoField();
        jLabel20 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        texPrez = new tnxbeans.tnxTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        labResidua = new javax.swing.JLabel();
        jLabel114 = new javax.swing.JLabel();
        texQtaOmaggio = new tnxbeans.tnxTextField();
        texId = new tnxbeans.tnxTextField();
        texId.setVisible(false);
        texRicerca = new JTextFieldRicerca();
        panLibero = new javax.swing.JPanel();
        lab_prezzosenzaiva = new org.jdesktop.swingx.JXHyperlink();
        texProvvigione = new tnxbeans.tnxTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        texIvaDeducibile = new tnxbeans.tnxTextField();
        labIvaDeducibile = new javax.swing.JLabel();
        labPercentualeIvaDeducibile = new javax.swing.JLabel();
        scatole = new javax.swing.JButton();
        cheUpdateListino = new javax.swing.JCheckBox();
        labArrotondamento = new javax.swing.JLabel();
        labTotArr = new javax.swing.JLabel();
        texTotArrotondato = new javax.swing.JTextField();
        comArrotondamento = new tnxbeans.tnxComboField();
        comTipoArr = new tnxbeans.tnxComboField();
        cheIsDescrizione = new tnxbeans.tnxCheckBox();
        lab_prezzoivainclusa = new org.jdesktop.swingx.JXHyperlink();
        labNumeroScatole = new javax.swing.JLabel();
        texNumeroScatole = new tnxbeans.tnxTextField();
        panLibero2 = new javax.swing.JPanel();
        consegna_prevista = new org.jdesktop.swingx.JXDatePicker();
        labConsegna = new javax.swing.JLabel();
        labConta = new javax.swing.JLabel();
        segnaposto_note = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        butAnnulla = new javax.swing.JButton();
        butSave = new javax.swing.JButton();
        butCaricaESalva = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        labTotale = new javax.swing.JLabel();

        setMaximizable(true);
        setResizable(true);
        setTitle("Dettaglio riga");
        setMinimumSize(new java.awt.Dimension(630, 34));
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

        jPanel1.setMinimumSize(new java.awt.Dimension(630, 35));
        jPanel1.setLayout(new java.awt.BorderLayout());

        dati.setEnabled(false);

        texScon1.setColumns(6);
        texScon1.setText("sconto1");
        texScon1.setDbDescCampo("");
        texScon1.setDbNomeCampo("sconto1");
        texScon1.setDbTipoCampo("numerico");
        texScon1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texScon1ActionPerformed(evt);
            }
        });
        texScon1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texScon1FocusLost(evt);
            }
        });
        texScon1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                texScon1InputMethodTextChanged(evt);
            }
        });
        texScon1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texScon1KeyReleased(evt);
            }
        });

        texSeri.setEditable(false);
        texSeri.setBackground(new java.awt.Color(204, 204, 204));
        texSeri.setColumns(1);
        texSeri.setText("serie");
        texSeri.setToolTipText("Serie");
        texSeri.setDbDescCampo("");
        texSeri.setDbNomeCampo("serie");
        texSeri.setDbTipoCampo("");

        texCodiArti.setText("codice_articolo");
        texCodiArti.setToolTipText("Codice Articolo");
        texCodiArti.setDbDescCampo("");
        texCodiArti.setDbNomeCampo("codice_articolo");
        texCodiArti.setDbTipoCampo("");
        texCodiArti.setPreferredSize(new java.awt.Dimension(150, 20));
        texCodiArti.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                texCodiArtiFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                texCodiArtiFocusLost(evt);
            }
        });
        texCodiArti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texCodiArtiActionPerformed(evt);
            }
        });
        texCodiArti.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texCodiArtiKeyPressed(evt);
            }
        });

        texQta.setColumns(6);
        texQta.setText("quantita");
        texQta.setDbDecimaliMax(new java.lang.Integer(5));
        texQta.setDbDescCampo("");
        texQta.setDbNomeCampo("quantita");
        texQta.setDbTipoCampo("numerico");
        texQta.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texQtaFocusLost(evt);
            }
        });
        texQta.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                texQtaInputMethodTextChanged(evt);
            }
        });
        texQta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texQtaActionPerformed(evt);
            }
        });
        texQta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texQtaKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texQtaKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                texQtaKeyTyped(evt);
            }
        });

        texPrezIvato.setColumns(10);
        texPrezIvato.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texPrezIvato.setText("prezzo lordo");
        texPrezIvato.setDbDescCampo("");
        texPrezIvato.setDbNomeCampo("prezzo_ivato");
        texPrezIvato.setDbTipoCampo("valuta");
        texPrezIvato.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texPrezIvatoFocusLost(evt);
            }
        });
        texPrezIvato.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                texPrezIvatoInputMethodTextChanged(evt);
            }
        });
        texPrezIvato.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texPrezIvatoKeyReleased(evt);
            }
        });

        texIva.setColumns(3);
        texIva.setText("iva");
        texIva.setToolTipText("premere F4 per avere la lista dei codici iva");
        texIva.setDbDescCampo("");
        texIva.setDbNomeCampo("iva");
        texIva.setDbTipoCampo("");
        texIva.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texIvaFocusLost(evt);
            }
        });
        texIva.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texIvaKeyPressed(evt);
            }
        });

        texScon2.setColumns(6);
        texScon2.setText("sconto2");
        texScon2.setDbDescCampo("");
        texScon2.setDbNomeCampo("sconto2");
        texScon2.setDbTipoCampo("numerico");
        texScon2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texScon2ActionPerformed(evt);
            }
        });
        texScon2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texScon2FocusLost(evt);
            }
        });
        texScon2.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                texScon2InputMethodTextChanged(evt);
            }
        });
        texScon2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texScon2KeyReleased(evt);
            }
        });

        texStat.setBackground(new java.awt.Color(255, 200, 200));
        texStat.setColumns(1);
        texStat.setText("stato");
        texStat.setDbDescCampo("");
        texStat.setDbNomeCampo("stato");
        texStat.setDbTipoCampo("");

        comUm.setDbDescCampo("unità di misura");
        comUm.setDbNomeCampo("um");
        comUm.setDbRiempireForceText(true);
        comUm.setDbSalvaKey(false);
        comUm.setDbTipoCampo("");

        texNume.setEditable(false);
        texNume.setColumns(4);
        texNume.setText("numero");
        texNume.setToolTipText("Numero");
        texNume.setDbDescCampo("");
        texNume.setDbNomeCampo("numero");
        texNume.setDbTipoCampo("testo");
        texNume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texNumeActionPerformed(evt);
            }
        });

        texRiga.setColumns(4);
        texRiga.setText("riga");
        texRiga.setToolTipText("Riga");
        texRiga.setDbDescCampo("");
        texRiga.setDbNomeCampo("riga");
        texRiga.setDbTipoCampo("");

        jLabel1.setText("Iva");

        jLabel15.setText("Ricerca articolo");

        jLabel17.setText("Unità di mis.");

        jLabel110.setText("Quantita");

        texAnno.setBackground(new java.awt.Color(255, 200, 200));
        texAnno.setColumns(1);
        texAnno.setText("anno");
        texAnno.setDbDescCampo("");
        texAnno.setDbNomeCampo("anno");
        texAnno.setDbRiempire(false);
        texAnno.setDbTipoCampo("");

        texPrezNett.setBackground(new java.awt.Color(255, 204, 204));
        texPrezNett.setColumns(10);
        texPrezNett.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texPrezNett.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                texPrezNettFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                texPrezNettFocusLost(evt);
            }
        });
        texPrezNett.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texPrezNettKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texPrezNettKeyReleased(evt);
            }
        });

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getSize()-1f));
        jLabel2.setForeground(new java.awt.Color(102, 102, 102));
        jLabel2.setText("- premere F4 sul codice articolo o sulla descrizione per avere la lista articoli filtrata");

        jLabel3.setFont(jLabel3.getFont().deriveFont(jLabel3.getFont().getSize()-1f));
        jLabel3.setForeground(new java.awt.Color(102, 102, 102));
        jLabel3.setText("- inserire nella casella prezzo netto il prezzo a cui si desidera arrivare, premendo invio calcola lo sconto da applicare");

        jLabel4.setText("Prezzo netto");

        prezzi_prec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-find.png"))); // NOI18N
        prezzi_prec.setText("Visualizza prezzi precedenti");
        prezzi_prec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prezzi_precActionPerformed(evt);
            }
        });

        labPercentualeIva.setFont(labPercentualeIva.getFont().deriveFont(labPercentualeIva.getFont().getSize()-2f));
        labPercentualeIva.setText("%");

        comIva.setText("...");
        comIva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comIvaActionPerformed(evt);
            }
        });

        butCalcolaSconto.setText("C");
        butCalcolaSconto.setToolTipText("Calcola sconto");
        butCalcolaSconto.setMargin(new java.awt.Insets(2, 2, 2, 2));
        butCalcolaSconto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCalcolaScontoActionPerformed(evt);
            }
        });

        texDescrizione.setDbNomeCampo("descrizione");

        jLabel20.setText("Descrizione articolo/riga");

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/apps/accessories-text-editor.png"))); // NOI18N
        jButton2.setText("Aggiungi numero colli");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel18.setText("Sco/Ric 1");
        jLabel18.setToolTipText("Sconto o ricarico (in negativo)");

        jLabel19.setText("Sco/Ric 2");
        jLabel19.setToolTipText("Sconto o ricarico (in negativo)");

        texPrez.setColumns(10);
        texPrez.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texPrez.setText("prezzo");
        texPrez.setDbDescCampo("");
        texPrez.setDbNomeCampo("prezzo");
        texPrez.setDbTipoCampo("valuta");
        texPrez.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texPrezActionPerformed(evt);
            }
        });
        texPrez.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texPrezFocusLost(evt);
            }
        });
        texPrez.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                texPrezInputMethodTextChanged(evt);
            }
        });
        texPrez.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texPrezKeyReleased(evt);
            }
        });

        jLabel21.setFont(jLabel21.getFont().deriveFont(jLabel21.getFont().getSize()-2f));
        jLabel21.setText("%");
        jLabel21.setToolTipText("Sconto o ricarico (in negativo)");
        jLabel21.setIconTextGap(0);
        jLabel21.setMinimumSize(new java.awt.Dimension(9, 11));

        jLabel22.setFont(jLabel22.getFont().deriveFont(jLabel22.getFont().getSize()-2f));
        jLabel22.setText("%");
        jLabel22.setToolTipText("Sconto o ricarico (in negativo)");
        jLabel22.setIconTextGap(0);
        jLabel22.setMinimumSize(new java.awt.Dimension(9, 11));

        labResidua.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labResidua.setToolTipText("<html>La quantità residua è la attuale quantità in magazzino.<br>\nLa quantità teorica è calcolata a partire dalla quantità residua sommando<br>\n le quantità da preventivi e ordini e sottraendo da preventivi e ordini di vendita\n</html>");

        jLabel114.setText("In omaggio");
        jLabel114.setToolTipText("Inserendo una quantità verrà generata un ulteriore riga con la quantità desiderata ed il prezzo 0");

        texQtaOmaggio.setColumns(6);
        texQtaOmaggio.setText("0");
        texQtaOmaggio.setDbDescCampo("");
        texQtaOmaggio.setDbNomeCampo("quantita_omaggio");
        texQtaOmaggio.setDbRiempire(false);
        texQtaOmaggio.setDbSalvare(false);
        texQtaOmaggio.setDbTipoCampo("numerico");
        texQtaOmaggio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texQtaOmaggioActionPerformed(evt);
            }
        });
        texQtaOmaggio.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texQtaOmaggioFocusLost(evt);
            }
        });

        texId.setBackground(new java.awt.Color(255, 200, 200));
        texId.setColumns(1);
        texId.setText("id_padre");
        texId.setDbDescCampo("");
        texId.setDbNomeCampo("id_padre");
        texId.setDbTipoCampo("");

        texRicerca.setForeground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveForeground"));
        texRicerca.setText("... digita qui per cercare l'articolo tramite codice o descrizione");
        texRicerca.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                texRicercaComponentResized(evt);
            }
        });
        texRicerca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texRicercaActionPerformed(evt);
            }
        });
        texRicerca.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texRicercaFocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                texRicercaFocusGained(evt);
            }
        });
        texRicerca.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                texRicercaKeyPressed(evt);
            }
        });

        panLibero.setLayout(new java.awt.GridLayout(0, 1));

        lab_prezzosenzaiva.setText("Prezzo senza iva");
        lab_prezzosenzaiva.setToolTipText("Clicca per vedere i prezzi di listino dell'articolo");
        lab_prezzosenzaiva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lab_prezzosenzaivaActionPerformed(evt);
            }
        });

        texProvvigione.setColumns(4);
        texProvvigione.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        texProvvigione.setText("provvigione");
        texProvvigione.setDbDescCampo("");
        texProvvigione.setDbNomeCampo("provvigione");
        texProvvigione.setDbTipoCampo("numerico");
        texProvvigione.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                texProvvigioneFocusLost(evt);
            }
        });
        texProvvigione.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                texProvvigioneInputMethodTextChanged(evt);
            }
        });
        texProvvigione.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                texProvvigioneKeyReleased(evt);
            }
        });

        jLabel23.setText("Provvigione");
        jLabel23.setToolTipText("Sconto o ricarico (in negativo)");

        jLabel24.setFont(jLabel24.getFont().deriveFont(jLabel24.getFont().getSize()-2f));
        jLabel24.setText("%");
        jLabel24.setToolTipText("Sconto o ricarico (in negativo)");
        jLabel24.setIconTextGap(0);
        jLabel24.setMinimumSize(new java.awt.Dimension(9, 11));

        jLabel25.setText("Codice articolo");
        jLabel25.setToolTipText("Sconto o ricarico (in negativo)");

        texIvaDeducibile.setColumns(5);
        texIvaDeducibile.setText("deducibile");
        texIvaDeducibile.setDbDefault("vuoto");
        texIvaDeducibile.setDbNomeCampo("iva_deducibile");
        texIvaDeducibile.setDbNullSeVuoto(true);

        labIvaDeducibile.setText("Deducibile");

        labPercentualeIvaDeducibile.setFont(labPercentualeIvaDeducibile.getFont().deriveFont(labPercentualeIvaDeducibile.getFont().getSize()-2f));
        labPercentualeIvaDeducibile.setText("%");

        scatole.setToolTipText("Clicca per calcolare la quantità in base alle scatole");
        scatole.setIconTextGap(2);
        scatole.setMargin(new java.awt.Insets(2, 2, 2, 2));
        scatole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scatoleActionPerformed(evt);
            }
        });

        cheUpdateListino.setText("Aggiorna Prezzo Listino");

        labArrotondamento.setText("Arrotondamento");

        labTotArr.setText("Prezzo Arr.");
        labTotArr.setToolTipText("Sconto o ricarico (in negativo)");

        texTotArrotondato.setEditable(false);
        texTotArrotondato.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        texTotArrotondato.setEnabled(false);

        comArrotondamento.setDbNomeCampo("arrotondamento_parametro");
        comArrotondamento.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comArrotondamentoItemStateChanged(evt);
            }
        });
        comArrotondamento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comArrotondamentoActionPerformed(evt);
            }
        });

        comTipoArr.setDbNomeCampo("arrotondamento_tipo");
        comTipoArr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comTipoArrActionPerformed(evt);
            }
        });

        cheIsDescrizione.setText("Descrizione");
        cheIsDescrizione.setToolTipText("Nel caso di articolo di tipo Descrizione verrà visualizzato come descrizione per l'intera riga in stampa");
        cheIsDescrizione.setDbDescCampo("");
        cheIsDescrizione.setDbNomeCampo("is_descrizione");
        cheIsDescrizione.setDbTipoCampo("");

        lab_prezzoivainclusa.setText("Prezzo iva inclusa");
        lab_prezzoivainclusa.setToolTipText("Clicca per vedere i prezzi di listino dell'articolo");
        lab_prezzoivainclusa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lab_prezzoivainclusaActionPerformed(evt);
            }
        });

        labNumeroScatole.setText("Numero Casse");

        texNumeroScatole.setColumns(6);
        texNumeroScatole.setDbNomeCampo("numero_casse");
        texNumeroScatole.setDbNullSeVuoto(true);

        panLibero2.setLayout(new java.awt.BorderLayout());

        labConsegna.setText("Consegna prevista");

        labConta.setForeground(javax.swing.UIManager.getDefaults().getColor("Label.disabledForeground"));
        labConta.setToolTipText("Qui puoi controllare quanti caratteri sono inseriti, per l'export della fattura elettronica sono massimo 1000 caratteri");

        jLabel5.setText("riga");

        org.jdesktop.layout.GroupLayout datiLayout = new org.jdesktop.layout.GroupLayout(dati);
        dati.setLayout(datiLayout);
        datiLayout.setHorizontalGroup(
            datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiLayout.createSequentialGroup()
                .addContainerGap()
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(datiLayout.createSequentialGroup()
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(texDescrizione, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel20)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(labConta)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(labResidua, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 439, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(datiLayout.createSequentialGroup()
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(datiLayout.createSequentialGroup()
                                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(jLabel17)
                                            .add(comUm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(labNumeroScatole)
                                            .add(texNumeroScatole, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabel110)
                                            .add(jLabel114)
                                            .add(texQtaOmaggio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(datiLayout.createSequentialGroup()
                                                .add(texQta, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(1, 1, 1)
                                                .add(scatole)))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(lab_prezzoivainclusa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(lab_prezzosenzaiva, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(texPrezIvato, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(texPrez, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(datiLayout.createSequentialGroup()
                                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(jLabel4)
                                                    .add(texPrezNett, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                .add(1, 1, 1)
                                                .add(butCalcolaSconto)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(datiLayout.createSequentialGroup()
                                                        .add(texScon1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .add(1, 1, 1)
                                                        .add(jLabel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                    .add(jLabel18))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(datiLayout.createSequentialGroup()
                                                        .add(texScon2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .add(1, 1, 1)
                                                        .add(jLabel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                    .add(jLabel19))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(jLabel1)
                                                    .add(datiLayout.createSequentialGroup()
                                                        .add(texIva, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .add(1, 1, 1)
                                                        .add(comIva, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .add(1, 1, 1)
                                                        .add(labPercentualeIva))))
                                            .add(datiLayout.createSequentialGroup()
                                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                                    .add(datiLayout.createSequentialGroup()
                                                        .add(comArrotondamento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                        .add(comTipoArr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                    .add(labArrotondamento, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(datiLayout.createSequentialGroup()
                                                        .add(labTotArr)
                                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                        .add(jLabel23))
                                                    .add(datiLayout.createSequentialGroup()
                                                        .add(texTotArrotondato, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                        .add(texProvvigione, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .add(1, 1, 1)
                                                        .add(jLabel24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                                .add(4, 4, 4)
                                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(labIvaDeducibile)
                                                    .add(datiLayout.createSequentialGroup()
                                                        .add(texIvaDeducibile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .add(1, 1, 1)
                                                        .add(labPercentualeIvaDeducibile))))))
                                    .add(datiLayout.createSequentialGroup()
                                        .add(cheIsDescrizione, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(cheUpdateListino)
                                        .add(18, 18, 18)
                                        .add(labConsegna)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(consegna_prevista, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(panLibero, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(panLibero2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(datiLayout.createSequentialGroup()
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel15)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texRicerca))
                            .add(datiLayout.createSequentialGroup()
                                .add(prezzi_prec)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texStat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, 0)
                                .add(texAnno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, 0)
                                .add(texId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(texSeri, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, 0)
                                .add(texNume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(jLabel5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(texRiga, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel25)
                            .add(texCodiArti, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, datiLayout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(segnaposto_note))))
        );
        datiLayout.setVerticalGroup(
            datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(datiLayout.createSequentialGroup()
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(datiLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(datiLayout.createSequentialGroup()
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(prezzi_prec)
                                    .add(jButton2)
                                    .add(texStat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texAnno, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texSeri, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texNume, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texRiga, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel5))
                                .add(5, 5, 5))
                            .add(datiLayout.createSequentialGroup()
                                .add(jLabel25)
                                .add(2, 2, 2))))
                    .add(datiLayout.createSequentialGroup()
                        .add(segnaposto_note)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(texRicerca, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel15)
                    .add(texCodiArti, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(labResidua)
                    .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel20)
                        .add(labConta)))
                .add(1, 1, 1)
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panLibero2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                    .add(texDescrizione, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(datiLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel110)
                            .add(jLabel18)
                            .add(jLabel19)
                            .add(jLabel1)
                            .add(lab_prezzosenzaiva, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel4))
                        .add(1, 1, 1)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(scatole)
                            .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(comUm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(texQta, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(texPrez, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(texPrezNett, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(butCalcolaSconto)
                                .add(texScon1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(jLabel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(texScon2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(jLabel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(texIva, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(comIva)
                                .add(labPercentualeIva)))
                        .add(5, 5, 5)
                        .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(datiLayout.createSequentialGroup()
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(jLabel114, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(labArrotondamento, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(labTotArr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(lab_prezzoivainclusa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(jLabel23, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(labIvaDeducibile)))
                                .add(1, 1, 1)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE, false)
                                    .add(texQtaOmaggio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texIvaDeducibile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(labPercentualeIvaDeducibile)
                                    .add(texPrezIvato, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texProvvigione, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(texTotArrotondato, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(comArrotondamento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(comTipoArr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(datiLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(cheUpdateListino)
                                    .add(cheIsDescrizione, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(consegna_prevista, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(labConsegna)))
                            .add(datiLayout.createSequentialGroup()
                                .add(labNumeroScatole)
                                .add(1, 1, 1)
                                .add(texNumeroScatole, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(panLibero, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .add(2, 2, 2)
                .add(jLabel3))
        );

        datiLayout.linkSize(new java.awt.Component[] {butCalcolaSconto, comIva, comUm, jLabel21, jLabel22, labPercentualeIva, scatole, texIva, texPrez, texPrezNett, texQta, texScon1, texScon2}, org.jdesktop.layout.GroupLayout.VERTICAL);

        datiLayout.linkSize(new java.awt.Component[] {comArrotondamento, comTipoArr, jLabel24, labPercentualeIvaDeducibile, texIvaDeducibile, texNumeroScatole, texPrezIvato, texProvvigione, texQtaOmaggio, texTotArrotondato}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jPanel1.add(dati, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        butAnnulla.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-undo.png"))); // NOI18N
        butAnnulla.setText("Annulla");
        butAnnulla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butAnnullaActionPerformed(evt);
            }
        });

        butSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/devices/media-floppy.png"))); // NOI18N
        butSave.setText("Salva");
        butSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSaveActionPerformed(evt);
            }
        });

        butCaricaESalva.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/devices/media-floppy.png"))); // NOI18N
        butCaricaESalva.setText("Carica & Salva");
        butCaricaESalva.setToolTipText("Carica a magazzino e salva la riga");
        butCaricaESalva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCaricaESalvaActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(304, Short.MAX_VALUE)
                .add(butAnnulla)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(butCaricaESalva)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(butSave)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(butSave)
                    .add(butAnnulla)
                    .add(butCaricaESalva))
                .addContainerGap())
        );

        jPanel2.add(jPanel3, java.awt.BorderLayout.CENTER);

        labTotale.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labTotale.setText("...");
        labTotale.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        labTotale.setPreferredSize(new java.awt.Dimension(200, 50));
        jPanel4.add(labTotale);

        jPanel2.add(jPanel4, java.awt.BorderLayout.EAST);

        jPanel1.add(jPanel2, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void texCodiArtiFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texCodiArtiFocusGained
        comCodiArti_old = texCodiArti.getText();
    }//GEN-LAST:event_texCodiArtiFocusGained

    private void butCalcolaScontoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCalcolaScontoActionPerformed
        calcolaSconto();
    }//GEN-LAST:event_butCalcolaScontoActionPerformed

    private void texIvaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texIvaFocusLost

        //trova la percentuale dell'iva
        Iva iva = new Iva();
        iva.load(Db.INSTANCE, this.texIva.getText());

        java.text.DecimalFormat decformat = new java.text.DecimalFormat("##0");
        this.labPercentualeIva.setText(decformat.format(iva.getPercentuale()) + "%");

        aggiorna_iva();

        aggiornaTotale();

    }//GEN-LAST:event_texIvaFocusLost

    private void comIvaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comIvaActionPerformed

        frmListIva frm = new frmListIva(this.texIva, this.labPercentualeIva, this);

        main.getPadre().openFrame(frm, 400, 200, texIva.getLocationOnScreen().y, texIva.getLocationOnScreen().x);
    }//GEN-LAST:event_comIvaActionPerformed

    private void texIvaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texIvaKeyPressed

        if (evt.getKeyCode() == evt.VK_F4) {
            comIvaActionPerformed(null);
        }
    }//GEN-LAST:event_texIvaKeyPressed

    private void prezzi_precActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prezzi_precActionPerformed
        showPrezziFatture();
    }//GEN-LAST:event_prezzi_precActionPerformed

    private void texPrezNettKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texPrezNettKeyPressed

        if (evt.getKeyCode() == 10) {
            calcolaSconto();
        }
    }//GEN-LAST:event_texPrezNettKeyPressed

    private void texCodiArtiFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texCodiArtiFocusLost
        if (comCodiArti_old != null && !comCodiArti_old.equals(texCodiArti.getText())) {
            if (!f4) {
                recuperaDatiArticolo();
            }
        }
    }//GEN-LAST:event_texCodiArtiFocusLost

    private void texCodiArtiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texCodiArtiKeyPressed

        // Add your handling code here:
        if (evt.getKeyCode() == evt.VK_ENTER) {
            recuperaDatiArticolo();
        } else if (evt.getKeyCode() == evt.VK_F4) {
            f4 = true;
            java.util.Hashtable colsWidthPerc = new java.util.Hashtable();
            colsWidthPerc.put("codice", new Double(20));
            colsWidthPerc.put("descrizione", new Double(80));

            String sql = "select codice, descrizione from articoli" + " where codice like '" + Db.aa(this.texCodiArti.getText()) + "%'" + " order by codice, descrizione";
            frmDbListSmall temp = new frmDbListSmall(main.getPadre(), true, sql, this.texCodiArti, 0, colsWidthPerc, 50, 200, 900, 500);
            this.texCodiArti.requestFocus();
            recuperaDatiArticolo();
//            this.comCodiArti.dbTrovaKey(this.texCodiArti.getText());
//            this.comCodiArti.requestFocus();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    texQta.requestFocus();
                }
            });
            f4 = false;
        }
    }//GEN-LAST:event_texCodiArtiKeyPressed

    private void texCodiArtiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texCodiArtiActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_texCodiArtiActionPerformed

    private void comCodiArtiItemStateChanged(java.awt.event.ItemEvent evt) {
    }

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosed
        main.fileIni.setValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_w", getSize().width);
        main.fileIni.setValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_h", getSize().height);
        main.fileIni.setValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_top", getLocation().y);
        main.fileIni.setValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_left", getLocation().x);
        // Add your handling code here:
        main.getPadre().closeFrame(this);
    }//GEN-LAST:event_formInternalFrameClosed

    private void butAnnullaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butAnnullaActionPerformed
        this.dispose();
        //riattivo form di provenienza
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    main.getPadre().getDesktopPane().getDesktopManager().activateFrame((JInternalFrame) from);
                    ((JInternalFrame) from).setSelected(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }//GEN-LAST:event_butAnnullaActionPerformed

    private void butSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSaveActionPerformed
        salva();
    }//GEN-LAST:event_butSaveActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    if (peso_kg_collo != 0) {
        double qta = it.tnx.Util.getDouble(texQta.getText()) + it.tnx.Util.getDouble(texQtaOmaggio.getText());
        double collid = qta / peso_kg_collo;
        int colli = ((Double) Math.ceil(collid)).intValue();
        texDescrizione.setText(texDescrizione.getText() + "\n" + "Numero colli: " + colli);
    } else {
        JOptionPane.showMessageDialog(this, "Non è inserito il peso per collo in anagrafica articoli", "Attenzione", JOptionPane.INFORMATION_MESSAGE);
    }
}//GEN-LAST:event_jButton2ActionPerformed

private void texQtaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texQtaActionPerformed
}//GEN-LAST:event_texQtaActionPerformed

    public void aggiorna_iva() {
        aggiorna_iva(false, false);
    }

    public void aggiorna_iva(boolean forza_senza_iva, boolean forza_con_iva) {
        try {
            double iva_prezz = 100d;
            try {
                iva_prezz = 100d + ((BigDecimal) DbUtils.getObject(Db.getConn(), "select percentuale from codici_iva where codice = " + Db.pc(texIva.getText(), Types.VARCHAR))).doubleValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ((!prezzi_ivati || forza_senza_iva) && !forza_con_iva) {
                double new_prezz_lordo = 0;
                double prezz_netto = Db.getDouble(this.texPrez.getText());
                new_prezz_lordo = (double) (prezz_netto / 100d) * iva_prezz;
                texPrezIvato.setText(Db.formatDecimal5(new_prezz_lordo));
            } else {
                double new_prezz_netto = 0;
                double prezz_lordo = Db.getDouble(this.texPrezIvato.getText());
                new_prezz_netto = (double) (prezz_lordo * 100d) / iva_prezz;
                texPrez.setText(Db.formatDecimal5(new_prezz_netto));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

private void texPrezIvatoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texPrezIvatoKeyReleased
    try {
        double iva_prezz = 100d + ((BigDecimal) DbUtils.getObject(Db.getConn(), "select percentuale from codici_iva where codice = " + Db.pc(texIva.getText(), Types.VARCHAR))).doubleValue();

        double new_prezz_netto = 0;
        double prezz_lordo = Db.getDouble(texPrezIvato.getText());

        new_prezz_netto = (double) (prezz_lordo * 100d) / iva_prezz;
        this.texPrez.setText(Db.formatDecimal5(new_prezz_netto));
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    aggiornaTotale();
}//GEN-LAST:event_texPrezIvatoKeyReleased

private void texPrezIvatoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texPrezIvatoFocusLost
    try {
        double iva_prezz = 100d + ((BigDecimal) DbUtils.getObject(Db.getConn(), "select percentuale from codici_iva where codice = " + Db.pc(texIva.getText(), Types.VARCHAR))).doubleValue();

        double new_prezz_netto = 0;
        double prezz_lordo = Db.getDouble(texPrezIvato.getText());

        new_prezz_netto = (double) (prezz_lordo * 100d) / iva_prezz;
        this.texPrez.setText(Db.formatDecimal5(new_prezz_netto));
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    calcolaScontoDaPercSconto();
    aggiornaTotale();
}//GEN-LAST:event_texPrezIvatoFocusLost

private void texPrezFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texPrezFocusLost
    // TODO add your handling code here:   
    aggiorna_iva(true, false);
    calcolaScontoDaPercSconto();
    aggiornaTotale();
}//GEN-LAST:event_texPrezFocusLost

private void texPrezKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texPrezKeyReleased
    // TODO add your handling code here:
    aggiorna_iva(true, false);
    aggiornaTotale();
}//GEN-LAST:event_texPrezKeyReleased

private void texQtaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texQtaFocusLost
    double qta = CastUtils.toDouble0(texQta.getText());
    texQta.setText(FormatUtils.formatNum0_5Dec(qta));
    aggiornaTotale();
}//GEN-LAST:event_texQtaFocusLost

private void texNumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texNumeActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_texNumeActionPerformed

private void texScon1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texScon1ActionPerformed
    calcolaScontoDaPercSconto();
}//GEN-LAST:event_texScon1ActionPerformed

private void texScon1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texScon1FocusLost
    calcolaScontoDaPercSconto();
    aggiornaTotale();
}//GEN-LAST:event_texScon1FocusLost

private void texScon2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texScon2ActionPerformed
    calcolaScontoDaPercSconto();
}//GEN-LAST:event_texScon2ActionPerformed

private void texScon2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texScon2FocusLost
    calcolaScontoDaPercSconto();
    aggiornaTotale();
}//GEN-LAST:event_texScon2FocusLost

private void texPrezNettFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texPrezNettFocusGained
    getRootPane().setDefaultButton(butCalcolaSconto);
    texPrezNett.selectAll();
}//GEN-LAST:event_texPrezNettFocusGained

private void texPrezNettFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texPrezNettFocusLost
    if (CastUtils.toDouble0(texPrez.getText()) == 0 && CastUtils.toDouble0(texPrezNett.getText()) != 0) {
        texPrez.setText(texPrezNett.getText());
    }
    getRootPane().setDefaultButton(butSave);
}//GEN-LAST:event_texPrezNettFocusLost

private void texPrezActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texPrezActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_texPrezActionPerformed

private void texQtaOmaggioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texQtaOmaggioActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_texQtaOmaggioActionPerformed

private void texQtaOmaggioFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texQtaOmaggioFocusLost
    // TODO add your handling code here:
}//GEN-LAST:event_texQtaOmaggioFocusLost

private void texQtaInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_texQtaInputMethodTextChanged
    aggiornaTotale();
}//GEN-LAST:event_texQtaInputMethodTextChanged

private void texPrezInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_texPrezInputMethodTextChanged
    aggiornaTotale();
}//GEN-LAST:event_texPrezInputMethodTextChanged

private void texPrezIvatoInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_texPrezIvatoInputMethodTextChanged
    aggiornaTotale();
}//GEN-LAST:event_texPrezIvatoInputMethodTextChanged

private void texScon1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_texScon1InputMethodTextChanged
    aggiornaTotale();
}//GEN-LAST:event_texScon1InputMethodTextChanged

private void texScon2InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_texScon2InputMethodTextChanged
    aggiornaTotale();
}//GEN-LAST:event_texScon2InputMethodTextChanged

private void texQtaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texQtaKeyReleased
    aggiornaTotale();
}//GEN-LAST:event_texQtaKeyReleased

private void texPrezNettKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texPrezNettKeyReleased
    aggiornaTotale();
}//GEN-LAST:event_texPrezNettKeyReleased

private void texScon1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon1KeyReleased
    aggiornaTotale();
}//GEN-LAST:event_texScon1KeyReleased

private void texScon2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texScon2KeyReleased
    aggiornaTotale();
}//GEN-LAST:event_texScon2KeyReleased

private void texRicercaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texRicercaFocusGained
    getRootPane().setDefaultButton(null);
    //    if (texRicerca.getText().equalsIgnoreCase("... digita qui per cercare l'articolo tramite codice o descrizione")) {
    //        texRicerca.setText("");
    //        texRicerca.setForeground(javax.swing.UIManager.getDefaults().getColor("TextField.foreground"));
    //    } else {
    //    }
}//GEN-LAST:event_texRicercaFocusGained

private void texRicercaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texRicercaKeyPressed
    if (texRicerca.getText().equalsIgnoreCase("... digita qui per cercare l'articolo tramite codice o descrizione")) {
        texRicerca.setText("");
        texRicerca.setForeground(javax.swing.UIManager.getDefaults().getColor("TextField.foreground"));
    }

    String key = "";
    try {
        key = ((ArticoloHint) alRicerca.getSelectedHint()).codice;
    } catch (Exception e) {
//        e.printStackTrace();
    }
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        //per codice a barre, controllo che esista il codice, se non esiste cerco per codice a barre
        if (!alRicerca.isHintsPopupVisible()) {
            alRicerca.setAutoPopup(false);
            System.out.println("auto OFF");
            String codice = texRicerca.getText();
            try {
                if (!DbUtils.containRows(Db.getConn(), "select codice from articoli where codice = " + Db.pc(codice, "VARCHAR"))) {
                    //cerco codice da codice a barre
                    codice = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select codice from articoli where codice_a_barre = " + Db.pc(codice, "VARCHAR")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            texCodiArti.setText(codice);
            recuperaDatiArticolo();
            Timer timer = new Timer(alRicerca.getShowHintsDelay() + 250, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("auto ON");
                    alRicerca.setAutoPopup(true);
                }
            });
            timer.setRepeats(false);
            timer.setInitialDelay(alRicerca.getShowHintsDelay() + 250);
            timer.setDelay(alRicerca.getShowHintsDelay() + 250);
            timer.start();

            texQta.requestFocus();
        }
    } else if (evt.getKeyCode() == KeyEvent.VK_F4) {
        java.util.Hashtable colsWidthPerc = new java.util.Hashtable();
        colsWidthPerc.put("codice", new Double(20));
        colsWidthPerc.put("descrizione", new Double(80));
        String sql = "select codice, descrizione from articoli";
        if (texRicerca.getText().length() > 0) {
            sql += " where descrizione like '" + Db.aa(texRicerca.getText()) + "%'";
        }
        sql += " order by descrizione, codice";
        frmDbListSmall temp = new frmDbListSmall(main.getPadre(), true, sql, this.texCodiArti, 0, colsWidthPerc, 50, 200, 900, 500);
        this.texCodiArti.requestFocus();
        recuperaDatiArticolo();
        this.requestFocus();
    }
}//GEN-LAST:event_texRicercaKeyPressed

private void lab_prezzosenzaivaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lab_prezzosenzaivaActionPerformed
    if (StringUtils.isEmpty(texCodiArti.getText())) {
        SwingUtils.showInfoMessage(this, "Seleziona prima un articolo");
    } else {
        JDialogPrezzi prezzi = new JDialogPrezzi(main.getPadre(), true);
        prezzi.setLocationRelativeTo(this);
        prezzi.pack();
        prezzi.loadArticolo(texCodiArti.getText(), codiceListino);
        prezzi.senzaIva = true;
        prezzi.setVisible(true);
        try {
            if (prezzi.prezzi.getSelectedRow() == -1) {
                return;
            }
            texPrez.setText(Db.formatDecimal5((Double) prezzi.prezzi.getValueAt(prezzi.prezzi.getSelectedRow(), 2)));
            this.listinoUpdate = String.valueOf(prezzi.prezzi.getValueAt(prezzi.prezzi.getSelectedRow(), 0));

            try {
                // Recupero sconto1
                String query = "SELECT sconto1 FROM articoli_prezzi WHERE ";
                query += "articolo = " + Db.pc(texCodiArti.getText(), Types.VARCHAR);
                query += " AND listino = " + Db.pc(listinoUpdate, Types.VARCHAR);
                double sconto1 = ((BigDecimal) DbUtils.getObject(Db.getConn(), query)).doubleValue();
                texScon1.setText(Db.formatDecimal(sconto1));
                // Recupero sconto2
                query = "SELECT sconto2 FROM articoli_prezzi WHERE ";
                query += "articolo = " + Db.pc(texCodiArti.getText(), Types.VARCHAR);
                query += " AND listino = " + Db.pc(listinoUpdate, Types.VARCHAR);
                double sconto2 = ((BigDecimal) DbUtils.getObject(Db.getConn(), query)).doubleValue();
                texScon2.setText(Db.formatDecimal(sconto2));
            } catch (Exception e) {
                e.printStackTrace();
            }

            aggiorna_iva(true, false);
            calcolaScontoDaPercSconto();
            aggiornaTotale();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}//GEN-LAST:event_lab_prezzosenzaivaActionPerformed

private void texProvvigioneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texProvvigioneFocusLost
    // TODO add your handling code here:
}//GEN-LAST:event_texProvvigioneFocusLost

private void texProvvigioneInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_texProvvigioneInputMethodTextChanged
    // TODO add your handling code here:
}//GEN-LAST:event_texProvvigioneInputMethodTextChanged

private void texProvvigioneKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texProvvigioneKeyReleased
    // TODO add your handling code here:
}//GEN-LAST:event_texProvvigioneKeyReleased

private void texRicercaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_texRicercaFocusLost
    getRootPane().setDefaultButton(butSave);
}//GEN-LAST:event_texRicercaFocusLost

private void scatoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scatoleActionPerformed
    final JDialog dialog = new JDialog(main.getPadreFrame(), "Calcola la quantità", true) {
        @Override
        protected JRootPane createRootPane() {
            JRootPane rootPane = new JRootPane();
            KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
            Action actionListener = new AbstractAction() {
                public void actionPerformed(ActionEvent actionEvent) {
                    setVisible(false);
                }
            };
            InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(stroke, "ESCAPE");
            rootPane.getActionMap().put("ESCAPE", actionListener);
            return rootPane;
        }
    };
    JPanelScatole pscatole = new JPanelScatole() {
        @Override
        public void annulla() {
            dialog.dispose();
        }

        @Override
        public void conferma() {
            double qta = CastUtils.toDouble0(totale.getText());
            frmNuovRigaDescrizioneMultiRigaNew.this.texQta.setText(FormatUtils.formatNum0_5Dec(qta));
            frmNuovRigaDescrizioneMultiRigaNew.this.aggiornaTotale();
            dialog.dispose();
        }
    };
    dialog.getContentPane().add(pscatole);
    dialog.pack();
    Point p = scatole.getLocationOnScreen();
    p.translate(0, 20);
    dialog.setLocation(p);
    dialog.setVisible(true);
}//GEN-LAST:event_scatoleActionPerformed

private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameOpened
    scatole.setSize(scatole.getWidth(), texQta.getHeight());
    scatole.setPreferredSize(new Dimension(scatole.getWidth(), texQta.getHeight()));
}//GEN-LAST:event_formInternalFrameOpened

private void texRicercaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texRicercaActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_texRicercaActionPerformed

private void comArrotondamentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comArrotondamentoActionPerformed
    calcolaScontoDaPercSconto();
    aggiornaTotale();
}//GEN-LAST:event_comArrotondamentoActionPerformed

private void comTipoArrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comTipoArrActionPerformed
    calcolaScontoDaPercSconto();
    aggiornaTotale();
}//GEN-LAST:event_comTipoArrActionPerformed

private void comArrotondamentoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comArrotondamentoItemStateChanged
    System.out.println("Evento:" + evt);
}//GEN-LAST:event_comArrotondamentoItemStateChanged

private void texRicercaComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_texRicercaComponentResized
    try {
        if (alRicerca.getDelegateComponent() != null) {
            JList list = (JList) alRicerca.getDelegateComponent();
            list.setFixedCellWidth(getWidth() - 50);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}//GEN-LAST:event_texRicercaComponentResized

private void lab_prezzoivainclusaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lab_prezzoivainclusaActionPerformed
    if (StringUtils.isEmpty(texCodiArti.getText())) {
        SwingUtils.showInfoMessage(this, "Seleziona prima un articolo");
    } else {
        JDialogPrezzi prezzi = new JDialogPrezzi(main.getPadre(), true);
        prezzi.setLocationRelativeTo(this);
        prezzi.pack();
        prezzi.loadArticolo(texCodiArti.getText(), codiceListino);
        prezzi.conIva = true;
        prezzi.setVisible(true);
        try {
            texPrezIvato.setText(Db.formatDecimal5((Double) prezzi.prezzi.getValueAt(prezzi.prezzi.getSelectedRow(), 2)));
            this.listinoUpdate = String.valueOf(prezzi.prezzi.getValueAt(prezzi.prezzi.getSelectedRow(), 0));

            // Recupero sconto1
            String query = "SELECT sconto1 FROM articoli_prezzi WHERE ";
            query += "articolo = " + Db.pc(texCodiArti.getText(), Types.VARCHAR);
            query += " AND listino = " + Db.pc(listinoUpdate, Types.VARCHAR);
            double sconto1 = ((BigDecimal) DbUtils.getObject(Db.getConn(), query)).doubleValue();
            texScon1.setText(Db.formatDecimal(sconto1));
            // Recupero sconto2
            query = "SELECT sconto2 FROM articoli_prezzi WHERE ";
            query += "articolo = " + Db.pc(texCodiArti.getText(), Types.VARCHAR);
            query += " AND listino = " + Db.pc(listinoUpdate, Types.VARCHAR);
            double sconto2 = ((BigDecimal) DbUtils.getObject(Db.getConn(), query)).doubleValue();
            texScon2.setText(Db.formatDecimal(sconto2));

            aggiorna_iva(false, true);
            calcolaScontoDaPercSconto();
            aggiornaTotale();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}//GEN-LAST:event_lab_prezzoivainclusaActionPerformed

    private void butCaricaESalvaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCaricaESalvaActionPerformed
        salva(true);
    }//GEN-LAST:event_butCaricaESalvaActionPerformed

    private void texQtaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texQtaKeyPressed
        System.out.println(evt.getKeyCode());
        aggiornaTotale();
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            butSaveActionPerformed(null);
        }
    }//GEN-LAST:event_texQtaKeyPressed

    private void texQtaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_texQtaKeyTyped
        System.out.println(evt.getKeyCode());
    }//GEN-LAST:event_texQtaKeyTyped

    public void aggiornaTotale() {
        if (caricamento) {
            return;
        }
        try {
            double qta = CastUtils.toDouble0(texQta.getText());
            double importo_senza_iva = CastUtils.toDouble0(texPrez.getText());
            double importo_con_iva = CastUtils.toDouble0(texPrezIvato.getText());
            double sconto1 = CastUtils.toDouble0(texScon1.getText());
            double sconto2 = CastUtils.toDouble0(texScon2.getText());

            tot_senza_iva = 0;
            tot_con_iva = 0;

            if (!prezzi_ivati) {
                tot_senza_iva = InvoicexUtil.getTotaleRigaSenzaIva(qta, importo_senza_iva, sconto1, sconto2);

                if (main.fileIni.getValueBoolean("pref", "attivaArrotondamento", false)) {
                    double parametro = Double.parseDouble(String.valueOf(comArrotondamento.getSelectedItem()));
                    boolean perDifetto = String.valueOf(comTipoArr.getSelectedItem()).equals("Inf.");
                    tot_senza_iva = InvoicexUtil.calcolaPrezzoArrotondato(tot_senza_iva, parametro, perDifetto);
                    this.texTotArrotondato.setText(FormatUtils.formatEuroIta(tot_senza_iva));
                }

                totale_imponibile = tot_senza_iva;
                double iva_prezz = 100d;
                try {
                    iva_prezz = 100d + ((BigDecimal) DbUtils.getObject(Db.getConn(), "select percentuale from codici_iva where codice = " + Db.pc(texIva.getText(), Types.VARCHAR))).doubleValue();
                } catch (Exception ex) {
                    System.out.println("iva non trovata:" + texIva.getText());
                }
                tot_con_iva = FormatUtils.round((tot_senza_iva / 100d) * iva_prezz, 2);
                totale_ivato = tot_con_iva;
            } else {
                tot_con_iva = InvoicexUtil.getTotaleRigaConIva(qta, importo_con_iva, sconto1, sconto2);
                totale_ivato = tot_con_iva;

                double iva_prezz = 100d;
                try {
                    iva_prezz = 100d + ((BigDecimal) DbUtils.getObject(Db.getConn(), "select percentuale from codici_iva where codice = " + Db.pc(texIva.getText(), Types.VARCHAR))).doubleValue();
                } catch (Exception ex) {
                    System.out.println("iva non trovata:" + texIva.getText());
                }
                tot_senza_iva = FormatUtils.round((tot_con_iva / iva_prezz) * 100d, 2);
                totale_imponibile = tot_senza_iva;
            }

            //personal litri
            Double totale_litri = null;
            String addhtml = "";
            if (main.getPersonalContain("litri")) {
                String sql = "select peso_kg from articoli";
                sql += " where codice = " + Db.pc(this.texCodiArti.getText(), "VARCHAR");
                try {
                    double litri = CastUtils.toDouble0(DbUtils.getObject(Db.getConn(), sql));
                    if (litri > 0) {
                        totale_litri = litri * qta;
                        addhtml = "<tr><td align='right'>Totale litri: <b>" + FormatUtils.formatEuroIta(totale_litri) + "</b></td></tr>";
                    }
                } catch (Exception e) {
                }
            }

            labTotale.setText("<html><table border='0' padding='0' margin='0'><tr><td align='right'>Totale senza iva: <b>" + FormatUtils.formatEuroIta(tot_senza_iva) + "</b></td></tr><tr><td align='right'>Totale con iva: <b>" + FormatUtils.formatEuroIta(tot_con_iva) + "</b></td></td></tr>" + addhtml + "</table></html>");
        } catch (Exception ex2) {
            labTotale.setText("");
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butAnnulla;
    private javax.swing.JButton butCalcolaSconto;
    private javax.swing.JButton butCaricaESalva;
    private javax.swing.JButton butSave;
    private tnxbeans.tnxCheckBox cheIsDescrizione;
    private javax.swing.JCheckBox cheUpdateListino;
    private tnxbeans.tnxComboField comArrotondamento;
    private javax.swing.JButton comIva;
    private tnxbeans.tnxComboField comTipoArr;
    private tnxbeans.tnxComboField comUm;
    public org.jdesktop.swingx.JXDatePicker consegna_prevista;
    public tnxbeans.tnxDbPanel dati;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel110;
    private javax.swing.JLabel jLabel114;
    private javax.swing.JLabel jLabel15;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel labArrotondamento;
    public javax.swing.JLabel labConsegna;
    private javax.swing.JLabel labConta;
    private javax.swing.JLabel labIvaDeducibile;
    private javax.swing.JLabel labNumeroScatole;
    private javax.swing.JLabel labPercentualeIva;
    private javax.swing.JLabel labPercentualeIvaDeducibile;
    private javax.swing.JLabel labResidua;
    private javax.swing.JLabel labTotArr;
    private javax.swing.JLabel labTotale;
    private org.jdesktop.swingx.JXHyperlink lab_prezzoivainclusa;
    private org.jdesktop.swingx.JXHyperlink lab_prezzosenzaiva;
    public javax.swing.JPanel panLibero;
    public javax.swing.JPanel panLibero2;
    private javax.swing.JButton prezzi_prec;
    private javax.swing.JButton scatole;
    public javax.swing.JLabel segnaposto_note;
    private tnxbeans.tnxTextField texAnno;
    public tnxbeans.tnxTextField texCodiArti;
    public tnxbeans.tnxMemoField texDescrizione;
    private tnxbeans.tnxTextField texId;
    private tnxbeans.tnxTextField texIva;
    private tnxbeans.tnxTextField texIvaDeducibile;
    private tnxbeans.tnxTextField texNume;
    private tnxbeans.tnxTextField texNumeroScatole;
    private tnxbeans.tnxTextField texPrez;
    private tnxbeans.tnxTextField texPrezIvato;
    private javax.swing.JTextField texPrezNett;
    public tnxbeans.tnxTextField texProvvigione;
    private tnxbeans.tnxTextField texQta;
    private tnxbeans.tnxTextField texQtaOmaggio;
    private javax.swing.JTextField texRicerca;
    private tnxbeans.tnxTextField texRiga;
    private tnxbeans.tnxTextField texScon1;
    private tnxbeans.tnxTextField texScon2;
    private tnxbeans.tnxTextField texSeri;
    private tnxbeans.tnxTextField texStat;
    private javax.swing.JTextField texTotArrotondato;
    // End of variables declaration//GEN-END:variables

    public void setStato() {
        caricamento = true;

        //controllo se inserimento o modifica
        MicroBench mb2 = new MicroBench();
        mb2.start();

        dati.messaggio_nuovo_manuale = true;
        if (dbStato == this.dati.DB_INSERIMENTO) {
            this.dati.dbOpen(Db.getConn(), "select * from " + this.dati.dbNomeTabella + " limit 0");
        } else {
            String sql = "select * from " + this.dati.dbNomeTabella;
            if (id_riga != null && id_riga >= 0) {
                sql += " where id = " + id_riga;
            } else if (id_padre != null && id_padre >= 0) {
                sql += " where id_padre = " + id_padre;
                sql += " and riga = " + dbRiga;
            } else {
                sql += " where serie = " + Db.pc(dbSerie, "VARCHAR");
                sql += " and numero = " + dbNumero;
                sql += " and anno = " + dbAnno;
                sql += " and riga = " + dbRiga;
            }
            this.dati.dbOpen(Db.getConn(), sql);
        }

//        dati.dbChiaveAutoInc = true;
        mb2.out("  post open");

        this.dati.dbRefresh();

        if (texIva.getText().equals("") || texIva.getText().equals("-1")) {
//            texIva.setText("20");
            texIva.setText(InvoicexUtil.getIvaDefaultPassaggio());
            texIvaFocusLost(null);
        }

        mb2.out("  post refresh");

        //controllo come devo aprire
        if (dbStato == this.dati.DB_INSERIMENTO) {
            inserimento();
        } else {
//            comCodiArti_old = comCodiArti.getSelectedKey();
            comCodiArti_old = texCodiArti.getText();
            consegna_prevista.setDate(cu.toDate(dati.dbGetField("data_consegna_prevista")));

            //forzo serie/numero/anno perchè si va per id_padre
            texSeri.setText(dbSerie);
            texNume.setText(String.valueOf(dbNumero));
            texAnno.setText(String.valueOf(dbAnno));

            //carico peso collo
            if (texCodiArti.getText().length() > 0) {
                try {
                    peso_kg_collo = cu.d(DbUtils.getObject(Db.getConn(), "select peso_kg_collo from articoli where codice = " + Db.pc(texCodiArti.getText(), "VARCHAR"), false));
                } catch (Exception e) {
                }
            }
        }

        //personal litri
        if (main.getPersonalContain("litri")) {
            if (dbStato.equals(tnxDbPanel.DB_MODIFICA)) {
                String desc = texDescrizione.getText();
                if (desc.indexOf(litristring) >= 0) {
                    desc = StringUtils.substringBefore(desc, litristring);
                    texDescrizione.setText(desc);
                }
            }
        }

        mb2.out("  post inserimento");
        aggiorna_iva();
        calcolaScontoDaPercSconto();

        caricamento = false;

        aggiornaTotale();

        mb2.out("  post tutto");

        butAnnulla.setEnabled(true);
        butSave.setEnabled(true);

        System.out.println("texId id padre:" + texId.getText());
        
        main.events.fireInvoicexEvent(new InvoicexEvent(this, InvoicexEvent.TYPE_GENERIC_PostSetupDati));
    }

    private void inserimento() {
        this.dati.dbNew();
        this.texSeri.setText(this.dbSerie);
        this.texNume.setText(String.valueOf(dbNumero));
        this.texStat.setText(this.prevStato);
        comUm.setText(main.fileIni.getValue("varie", "umpred"));

        settaIva();

        this.texAnno.setText(String.valueOf(dbAnno));

        //apre il resultset per ultimo +1
        java.sql.Statement stat;
        ResultSet resu;

        try {
            stat = Db.getConn().createStatement();
            if (dbRiga != 0) {
                this.texRiga.setText(String.valueOf(dbRiga));
            } else {
                String sql = "select riga from " + this.dati.dbNomeTabella;
                if (id_padre != null) {
                    sql += " where id_padre = " + id_padre;
                } else {
                    SwingUtils.showErrorMessage(this, "Errore in inserimento riga, assente id_padre");
                }
                sql += " order by riga desc limit 1";
                resu = stat.executeQuery(sql);

                if (resu.next() == true) {
//                    this.texRiga.setText(String.valueOf(resu.getInt(1) + 1));
                    this.texRiga.setText(String.valueOf(resu.getInt(1) + InvoicexUtil.getRigaInc()));
                } else {
//                    this.texRiga.setText("1");
                    this.texRiga.setText(cu.s(InvoicexUtil.getRigaInc()));
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

        texId.setText(String.valueOf(id_padre));

        texScon1.setText(Db.formatDecimal(cliente_sconto1r));
        texScon2.setText(Db.formatDecimal(cliente_sconto2r));

        comArrotondamento.setSelectedIndex(0);
        comTipoArr.setSelectedIndex(0);

        main.events.fireInvoicexEvent(new InvoicexEvent(this, InvoicexEvent.TYPE_FRMNuovRigaDescrizioneMultiRigaNew_INSERIMENTO));
    }

    private void settaIva() {
        //carico da impostazioni
        Cliente cliente = new Cliente(this.codiceCliente);
        if (cliente.getTipoIva2().equals(Cliente.TIPO_IVA_ALTRO) && main.fileIni.getValueBoolean("pref", "controlliIva", true)) {
            this.texIva.setText("8");
        } else if (cliente.getTipoIva2().equals(Cliente.TIPO_IVA_CEE) && main.fileIni.getValueBoolean("pref", "controlliIva", true)) {
            this.texIva.setText("41");
        } else {
            texIva.setText(InvoicexUtil.getIvaDefaultPassaggio());
        }

        //carico da articolo se trovato
        if (recuperando) {
            String sql = "select iva from articoli";
            sql += " where codice = " + Db.pc(this.texCodiArti.getText(), "VARCHAR");
            try {
                String iva_art = cu.s(DbUtils.getObject(Db.getConn(), sql));
                if (StringUtils.isNotBlank(iva_art) && !iva_art.equals("-1")) {
                    texIva.setText(iva_art);
                }
            } catch (Exception e) {
            }
        }

        // IVA standard in base al cliente se impostata
        try {
            String sqlIva = "SELECT i.codice FROM clie_forn c JOIN codici_iva i ON c.iva_standard = i.codice WHERE c.codice =" + Db.pc(codiceCliente, Types.INTEGER);
            ResultSet ivaStandard = Db.openResultSet(sqlIva);
            if (ivaStandard.next()) {
                String ivaSel = ivaStandard.getString("codice");
                if (ivaSel != null && !ivaSel.equals("")) {
                    texIva.setText(ivaSel);
                }
                texIvaFocusLost(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (texIva.getText().equals("")) {
            texIva.setText(InvoicexUtil.getIvaDefaultPassaggio());
        }
        texIvaFocusLost(null);
    }

    private void toglieSN(String tab, Integer id_riga) {
        try {
            String descr = (String) DbUtils.getObject(Db.getConn(), "select descrizione from " + tab + " where id = " + id_riga);
            if (descr.indexOf("\nS/N") > 0) {
                //togliere
                String descr2 = StringUtils.substringBefore(descr, "\nS/N");
                System.out.println("descr:" + descr + " \n-> descr2:" + descr2);
                DbUtils.tryExecQuery(Db.getConn(), "update " + tab + " set descrizione = " + Db.pc(descr2, Types.VARCHAR) + " where id = " + id_riga);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toglieLotti(String tab, Integer id_riga) {
        try {
            String descr = (String) DbUtils.getObject(Db.getConn(), "select descrizione from " + tab + " where id = " + id_riga);
            if (descr.indexOf("\nLotto") >= 0) {
                //togliere
                String descr2 = StringUtils.substringBefore(descr, "\nLotto");
                System.out.println("descr:" + descr + " \n-> descr2:" + descr2);
                DbUtils.tryExecQuery(Db.getConn(), "update " + tab + " set descrizione = " + Db.pc(descr2, Types.VARCHAR) + " where id = " + id_riga);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void salva() {
        salva(false);
    }

    private void salva(boolean carica_magazzino) {
        MicroBench mb = new MicroBench(true);

        String nomeTabMatricole = ((GenericFrmTest) from).getNomeTabRigheMatricole();

        //debug
//        System.out.println(this.comCodiArti.getText());
        boolean aprireMatricolePre = false;
        String dbStatoOrig = dati.dbStato;

        String tab = dati.dbNomeTabella;
        String tipo_mov = "S";
        if (tab.indexOf("acquisto") >= 0) {
            tipo_mov = "C";
        }

        if (controlli()) {
            mb.out("SALVA RIGA : CONTROLLI");
            SwingUtils.mouse_wait(this);
            Integer numFatt = Integer.parseInt(this.texNume.getText());
            Integer anno = Integer.parseInt(this.texAnno.getText());
            if (this.dbStato.equals("I") && (this.dbRiga != 0)) {
                try {
                    Statement stat = Db.getConn().createStatement();
                    String sql = "update " + tab + " set riga = riga + " + InvoicexUtil.getRigaInc() + " where riga >= " + dbRiga;
                    if (id_padre != null) {
                        sql += " and id_padre = " + id_padre;
                    } else {
                        SwingUtils.showErrorMessage(this, "Attenzione, id documento nullo");
                        sql += " and serie = " + Db.pc(dbSerie, "VARCHAR");
                        sql += " and numero = " + dbNumero;
                        sql += " and anno = " + dbAnno;
                    }
                    sql += " order by riga DESC";
                    Db.executeSql(sql);
                } catch (SQLException sqlerr) {
                    sqlerr.printStackTrace();
                }
            }

            if (dati.getCampiAggiuntivi() == null) {
                dati.setCampiAggiuntivi(new Hashtable());
            }

            dati.getCampiAggiuntivi().put("totale_ivato", totale_ivato);
            dati.getCampiAggiuntivi().put("totale_imponibile", totale_imponibile);
            dati.getCampiAggiuntivi().put("data_consegna_prevista", Db.pc(consegna_prevista.getDate(), Types.DATE));

            //cambio descrizione per iltri
            Double totale_litri = null;
            String adddesc = "";
            if (main.getPersonalContain("litri")) {
                String sql = "select peso_kg from articoli";
                sql += " where codice = " + Db.pc(this.texCodiArti.getText(), "VARCHAR");
                try {
                    double litri = CastUtils.toDouble0(DbUtils.getObject(Db.getConn(), sql));
                    double qta = CastUtils.toDouble0(texQta.getText());
                    if (litri > 0) {
                        totale_litri = litri * qta;
                        adddesc = litristring + " " + FormatUtils.formatEuroIta(totale_litri);
                        texDescrizione.setText(texDescrizione.getText() + adddesc);
                    }
                } catch (Exception e) {
                }
            }

            mb.out("SALVA RIGA : PRIMa DI SAVE");
            if (!dati.dbSave()) {
                SwingUtils.mouse_def(this);
                return;
            }

            mb.out("SALVA RIGA : DOPO SAVE");

            id_riga = -1;
            if (dbStato == this.dati.DB_INSERIMENTO) {
                try {
                    id_riga = dati.last_inserted_id;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                id_riga = CastUtils.toInteger(dati.dbGetField("id"));
            }
            System.out.println("id_riga:" + id_riga);

            //eventuale carico a magazzino
            if (carica_magazzino) {
                //con stato = A verranno generati movimenti di carico in automatico
                String sql = "update " + tab + " set stato = 'A' where id = " + id_riga;
                System.out.println("sql = " + sql);
                try {
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (main.getPersonalContain("autocarico")) {
                String sql = "update " + tab + " set stato = '' where id = " + id_riga;
                System.out.println("sql = " + sql);
                try {
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //inserisco omaggi se presenti
            if (CastUtils.toDouble0(texQtaOmaggio.getText()) > 0) {
                String oldriga = texRiga.getText();
                String oldqta = texQta.getText();
                String olddescart = texDescrizione.getText();
                String oldprez = texPrez.getText();
                String oldprezivato = texPrezIvato.getText();
                String oldpreznett = texPrezNett.getText();

                String newriga = String.valueOf(CastUtils.toInteger0(texRiga.getText()) + InvoicexUtil.getRigaInc());
                String newqta = texQtaOmaggio.getText();
                String newcodart = texCodiArti.getText();
                String newdescart = "Omaggio: " + texDescrizione.getText();
                String newum = comUm.getText();

                inserimento();

                texRiga.setText(newriga);
                texCodiArti.setText(newcodart);
                texDescrizione.setText(newdescart);
                texQta.setText(newqta);
                texPrez.setText("0");
                comUm.setText(newum);
                dati.getCampiAggiuntivi().put("totale_ivato", 0);
                dati.getCampiAggiuntivi().put("totale_imponibile", 0);
                try {
                    //controllo se è in modifica e se la riga successiva è la vecchia riga di omaggio la elimino
                    if (dbStatoOrig.equals(tnxDbPanel.DB_MODIFICA)) {
                        Integer nriga = cu.i(texRiga.getText());
                        String articolo = cu.s(texCodiArti.getText());
                        if (nriga != null) {
                            String sql = "select id from " + tab + " "
                                    + " where id_padre = " + id_padre + " "
                                    + " and (riga = " + nriga + " or riga = " + (nriga - 1) + ") "
                                    + " and codice_articolo = " + Db.pcs(articolo)
                                    + " and descrizione like 'Omaggio:%'";
                            System.out.println("sql = " + sql);
                            Object ret = dbu.getObject(Db.getConn(), sql, false);
                            System.out.println("ret = " + ret);
                            if (ret != null) {
                                try {
                                    sql = "delete from " + tab + " where id = " + cu.i(ret);
                                    dbu.tryExecQuery(Db.getConn(), sql);
                                } catch (SQLException sqlerr3) {
                                    sqlerr3.printStackTrace();
                                }
                            }
                        }
                    }

                    //sposto righe successive altrimenti prende stessa riga della riga dopo
                    Integer nriga = cu.i(texRiga.getText());
                    if (nriga != null) {
                        Object ret = dbu.getObject(Db.getConn(), "select riga from " + tab + " where id_padre = " + id_padre + " and riga = " + nriga, false);
                        System.out.println("ret = " + ret);
                        if (ret != null) {
                            try {
                                String sql = "update " + tab + " set riga = riga + " + InvoicexUtil.getRigaInc() + " where riga >= " + nriga;
                                if (id_padre != null) {
                                    sql += " and id_padre = " + id_padre;
                                } else {
                                    SwingUtils.showErrorMessage(this, "Attenzione, id documento nullo");
                                    sql += " and serie = " + Db.pc(dbSerie, "VARCHAR");
                                    sql += " and numero = " + dbNumero;
                                    sql += " and anno = " + dbAnno;
                                }
                                dbu.tryExecQuery(Db.getConn(), sql);
                            } catch (SQLException sqlerr2) {
                                sqlerr2.printStackTrace();
                            }
                        }
                    }

                    dati.dbSave();
                    //eventuale carico a magazzino
                    if (main.getPersonalContain("autocarico")) {
                        int id_riga2 = dati.last_inserted_id;
                        String sql = "";
                        if (carica_magazzino) {
                            //con stato = A verranno generati movimenti di carico in automatico
                            sql = "update " + tab + " set stato = 'A' where id = " + id_riga2;
                        } else {
                            sql = "update " + tab + " set stato = '' where id = " + id_riga2;
                        }
                        System.out.println("sql = " + sql);
                        try {
                            DbUtils.tryExecQuery(Db.getConn(), sql);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //rimetto i campi come prima
                texRiga.setText(oldriga);
                texQta.setText(oldqta);
                texDescrizione.setText(olddescart);
                texPrez.setText(oldprez);
                texPrezIvato.setText(oldprezivato);
                texPrezNett.setText(oldpreznett);
                aggiornaTotale();
            }

            //forzo l'aggiorna righe per evitare problemi di arrotondamento diversi
            try {
                InvoicexUtil.aggiornaTotaliRighe(tipoDocumento, id_padre, prezzi_ivati, Db.getNomeTabT(tipoDocumento), tab);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestOrdine")) {
                from_frmTestOrdine.ricalcolaSubTotaliOrdine();
            }

            if (from.getClass().getName().equalsIgnoreCase("gestionepreventivi.frmOrdiTest")) {
                javax.swing.JOptionPane.showMessageDialog(this, "non trovato form di partenza");
            } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestDocu")) {
                frmTestDocu tempFrom = (frmTestDocu) from;
                tempFrom.griglia.dbRefresh();
//                tempFrom.prev.dbRefresh();
                tempFrom.ricalcolaTotali();
                aprireMatricolePre = true;
            } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFatt")) {
                frmTestFatt tempFrom = (frmTestFatt) from;
                tempFrom.griglia.dbRefresh();
//                tempFrom.prev.dbRefresh();
                tempFrom.ricalcolaTotali();

                if (tempFrom.tabDocumento.getSelectedIndex() == 1) {
                    tempFrom.aggiornaFoglioRighe();
                }

//                //vado a cercare nei ddt se ce ne è almeno uno attaccato a questa fattura
//                int conta = 0;
//                try {
//                    String sql = "select count(*) from test_ddt"
//                            + " where fattura_serie = '" + texSeri.getText() + "'"
//                            + " and fattura_numero = " + texNume.getText()
//                            + " and fattura_anno = " + texAnno.getText();
//                    ResultSet r = Db.openResultSet(sql);
//                    if (r.next()) {
//                        conta = r.getInt(1);
//                    }
//                } catch (SQLException sqlerr) {
//                    sqlerr.printStackTrace();
//                }
//                if (conta == 0) {
//                    aprireMatricolePre = true;
//                } else {
//                    if (gestioneFatture.main.pluginClientManager) {
//                        JOptionPane.showMessageDialog(this, "La fattura proviene da uno o più ddt e non verranno creati o rigenerati i movimenti");
//                    }
//                }
                //vado a cercare nei ddt se ce ne è almeno uno attaccato a questa fattura
                //non genero i movimenti ed è inutile richiedere quindi la matricola
                if (id_padre != null) {
                    int conta = 0;
                    try {
                        String sql = "select count(*) from righ_ddt "
                                + " where in_fatt = " + id_padre;
                        ResultSet r = Db.openResultSet(sql);
                        if (r.next()) {
                            conta = r.getInt(1);
                        }
                    } catch (SQLException sqlerr) {
                        sqlerr.printStackTrace();
                    }
                    if (conta == 0) {
                        aprireMatricolePre = true;
                    } else if (gestioneFatture.main.pluginClientManager) {
                        JOptionPane.showMessageDialog(this, "La fattura proviene da uno o più ddt e non verranno creati o rigenerati i movimenti");
                    }
                }
            } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFattAcquisto")) {
                frmTestFattAcquisto tempFrom = (frmTestFattAcquisto) from;
                tempFrom.griglia.dbRefresh();
//                tempFrom.prev.dbRefresh();
                tempFrom.ricalcolaTotali();
                aprireMatricolePre = true;
            } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestOrdine")) {
                frmTestOrdine tempFrom = (frmTestOrdine) from;
                tempFrom.griglia.dbRefresh();
                tempFrom.dbdoc.dbRefresh();
                tempFrom.ricalcolaTotali();
            } else {
                SwingUtils.mouse_def(this);
                javax.swing.JOptionPane.showMessageDialog(this, "non trovato form di partenza");
                return;
            }

            mb.out("SALVA RIGA : RICALCOLI");

            //chiedo lotti ?
            boolean apertoLotti = false;
            String codice = texCodiArti.getText();
            double qta = CastUtils.toDouble0(texQta.getText());
            double qtaomaggio = CastUtils.toDouble0(texQtaOmaggio.getText());

            if (qta != 0 || qtaomaggio != 0) {
                try {
                    String lotti = (String) DbUtils.getObject(Db.getConn(), "select gestione_lotti from articoli where codice = '" + Db.aa(codice) + "'", false);
                    String matricole = (String) DbUtils.getObject(Db.getConn(), "select gestione_matricola from articoli where codice = '" + Db.aa(codice) + "'", false);
                    if (lotti == null) {
                        lotti = "N";
                    }
                    if (matricole == null) {
                        matricole = "N";
                    }
                    if (lotti.equalsIgnoreCase("S") && matricole.equalsIgnoreCase("S")) {
                        apertoLotti = true;

//                        Integer id_riga = 0;
//                        id_riga = (Integer) dati.dbGetField("id");
                        if (qta != 0 && qtaomaggio != 0) {
                            SwingUtils.mouse_def(this);
                            SwingUtils.showInfoMessage(main.getPadreFrame(), "Avendo inserito sia la quantità che la quantità in omaggio verranno presentate due richieste di scelta lotti\nLa prima per i lotti delle quantità con prezzo\nLa seconda per i lotti in omaggio");
                        } else if (qta == 0 && qtaomaggio != 0) {
                            SwingUtils.mouse_def(this);
                            SwingUtils.showInfoMessage(main.getPadreFrame(), "Avendo inserito solo la quantità in omaggio verrano chiesti soltanto i lotti delle quantità in omaggio");
                        }

                        JDialogMatricoleLotti dialog = null;
                        String toadd = "";
                        String sql = null;
                        ArrayList<HashMap<String, String>> aml = null;
                        if (qta != 0) {
                            dialog = new JDialogMatricoleLotti(main.getPadre(), true, dbStato == this.dati.DB_INSERIMENTO ? true : false);
                            dialog.setLocationRelativeTo(null);
                            dialog.init(tipo_mov, CastUtils.toDouble0(texQta.getText()), codice, tab + "_lotti", id_riga, null);
                            dialog.setVisible(true);
                            System.out.println("lotti e matricole ok");

                            //aggiungo lotti in descrizione riga
                            System.out.println("id_riga : " + id_riga);

                            aml = dialog.getMatricoleLotti();
                            for (HashMap<String, String> m : aml) {
                                toadd += "\nS/N: " + m.get("matricola") + " - Lotto: " + m.get("lotto");
                            }
                            toglieSN(tab, id_riga);
                            sql = "update " + tab + " set descrizione = CONCAT(descrizione,'" + StringUtils.replace(toadd, "'", "\\'") + "') where id = " + id_riga;
                            System.out.println("sql = " + sql);
                            Db.executeSql(sql, true);
                        }

                        if (CastUtils.toDouble0(texQtaOmaggio.getText()) > 0) {
                            //lotti per gli omaggi
//                            id_riga = 0;
//                            id_riga = (Integer) dati.dbGetField("id");
                            id_riga++;

                            if (qta != 0) {
                                SwingUtils.showInfoMessage(main.getPadreFrame(), "Inserisci adesso i lotti per le quantità in omaggio");
                            }

                            dialog = new JDialogMatricoleLotti(main.getPadre(), true, dbStato == this.dati.DB_INSERIMENTO ? true : false);
                            dialog.setLocationRelativeTo(null);
                            dialog.init(tipo_mov, CastUtils.toDouble0(texQtaOmaggio.getText()), codice, tab + "_lotti", id_riga, null);
                            dialog.setVisible(true);
                            System.out.println("lotti e matricole ok");

                            //aggiungo lotti in descrizione riga
                            System.out.println("id_riga : " + id_riga);
                            toadd = "";
                            aml = dialog.getMatricoleLotti();
                            for (HashMap<String, String> m : aml) {
                                toadd += "\nS/N: " + m.get("matricola") + " - Lotto: " + m.get("lotto");
                            }
                            toglieSN(tab, id_riga);
                            sql = "update " + tab + " set descrizione = CONCAT(descrizione,'" + StringUtils.replace(toadd, "'", "\\'") + "') where id = " + id_riga;
                            System.out.println("sql = " + sql);
                            Db.executeSql(sql, true);
                        }
                    } else if (lotti.equalsIgnoreCase("S")) {
                        apertoLotti = true;
//                        Integer id_riga = 0;
//                        id_riga = (Integer) dati.dbGetField("id");

                        if (qta != 0 && qtaomaggio != 0) {
                            SwingUtils.showInfoMessage(main.getPadreFrame(), "Avendo inserito sia la quantità che la quantità in omaggio verranno presentate due richieste di scelta lotti\nLa prima per i lotti delle quantità con prezzo\nLa seconda per i lotti in omaggio");
                        } else if (qta == 0 && qtaomaggio != 0) {
                            SwingUtils.showInfoMessage(main.getPadreFrame(), "Avendo inserito solo la quantità in omaggio verrano chiesti soltanto i lotti delle quantità in omaggio");
                        }

                        String toadd = "";
                        JDialogLotti dialog = null;
                        String sql = null;
                        ArrayList<String> alotti = null;
                        ArrayList<Double> alottiqta = null;
                        if (qta != 0) {
                            dialog = new JDialogLotti(main.getPadreFrame(), true, dbStato == this.dati.DB_INSERIMENTO ? true : false);
                            dialog.setLocationRelativeTo(null);

                            //dialog.init(tipo_mov, CastUtils.toDouble0(texQta.getText()), codice, tab + "_lotti", id_riga, null, deposito, false);
                            dialog.init(tipo_mov, CastUtils.toDouble0(texQta.getText()), codice, ((GenericFrmTest) from).getNomeTabRigheLotti(), id_riga, null, deposito, false);
                            dialog.setVisible(true);

                            if (dialog.ret) {
                                System.out.println("lotti ok");

                                //aggiungo lotti in descrizione riga
                                System.out.println("id_riga : " + id_riga);
                                alotti = dialog.getLotti();
                                alottiqta = dialog.getLottiQta();
                                double totqta = 0;
                                int i = 0;
                                for (String m : alotti) {
                                    toadd += "\nLotto: " + m + " (" + FormatUtils.formatNum0_5Dec(alottiqta.get(i)) + ")";
                                    totqta += alottiqta.get(i);
                                    i++;
                                }
                                toglieLotti(tab, id_riga);

                                //ricalcolare totale in base a nuova quantita
                                texQta.setText(FormatUtils.formatNum0_5Dec(totqta));
                                aggiornaTotale();
                                sql = "update " + tab + " set quantita = " + dbu.sql(totqta) + ""
                                        + ", totale_ivato = " + dbu.sql(totale_ivato) + ""
                                        + ", totale_imponibile = " + dbu.sql(totale_imponibile) + ""
                                        + ", descrizione = CONCAT(descrizione,'" + StringUtils.replace(toadd, "'", "\\'") + "') where id = " + id_riga;
                                System.out.println("sql = " + sql);
                                Db.executeSql(sql, true);
                            } else {
                                //non ha confermato i lotti
                            }
                        }

                        if (CastUtils.toDouble0(texQtaOmaggio.getText()) > 0) {
                            //lotti per gli omaggi
//                            id_riga = 0;
//                            id_riga = (Integer) dati.dbGetField("id");
                            id_riga++;

                            if (qta != 0) {
                                SwingUtils.showInfoMessage(main.getPadreFrame(), "Inserisci adesso i lotti per le quantità in omaggio");
                            }

                            dialog = new JDialogLotti(main.getPadre(), true, dbStato == this.dati.DB_INSERIMENTO ? true : false);
                            dialog.setLocationRelativeTo(null);
                            dialog.init(tipo_mov, CastUtils.toDouble0(texQtaOmaggio.getText()), codice, ((GenericFrmTest) from).getNomeTabRigheLotti(), id_riga, null, deposito, false);
                            dialog.setVisible(true);

                            if (dialog.ret) {
                                System.out.println("lotti ok");

                                //aggiungo lotti in descrizione riga
                                System.out.println("id_riga : " + id_riga);
                                toadd = "";
                                alotti = dialog.getLotti();
                                alottiqta = dialog.getLottiQta();
                                double totqta = 0;
                                int i = 0;
                                for (String m : alotti) {
                                    toadd += "\nLotto: " + m + " (" + FormatUtils.formatNum0_5Dec(alottiqta.get(i)) + ")";
                                    totqta += alottiqta.get(i);
                                    i++;
                                }
                                toglieLotti(tab, id_riga);
                                sql = "update " + tab + " set quantita = " + dbu.sql(totqta) + ""
                                        + ", totale_ivato = 0"
                                        + ", totale_imponibile = 0"
                                        + ", descrizione = CONCAT(descrizione,'" + StringUtils.replace(toadd, "'", "\\'") + "') where id = " + id_riga;
                                System.out.println("sql = " + sql);
                                Db.executeSql(sql, true);
                            } else {
                                //non ha confermato i lotti
                            }
                        }

                        //aggiorno griglia form prec
                        if (from.getClass().getName().equalsIgnoreCase("gestionepreventivi.frmOrdiTest")) {
                            //vecchia form non + gestita
                        } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestDocu")) {
                            frmTestDocu tempFrom = (frmTestDocu) from;
                            tempFrom.griglia.dbRefresh();
//                            tempFrom.prev.dbRefresh();
                            tempFrom.ricalcolaTotali();
                            tempFrom.butNuovArti.requestFocus();
                            aprireMatricolePre = true;
                        } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFatt")) {
                            frmTestFatt tempFrom = (frmTestFatt) from;
                            tempFrom.griglia.dbRefresh();
//                            tempFrom.prev.dbRefresh();
                            tempFrom.ricalcolaTotali();
                            tempFrom.butNuovArti.requestFocus();
                        } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFattAcquisto")) {
                            frmTestFattAcquisto tempFrom = (frmTestFattAcquisto) from;
                            tempFrom.griglia.dbRefresh();
//                            tempFrom.prev.dbRefresh();
                            tempFrom.ricalcolaTotali();
                            tempFrom.butNuovArti.requestFocus();
                        } else if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestOrdine")) {
                            frmTestOrdine tempFrom = (frmTestOrdine) from;
                            tempFrom.griglia.dbRefresh();
                            tempFrom.dbdoc.dbRefresh();
                            tempFrom.ricalcolaTotali();
                            tempFrom.butNuovArti.requestFocus();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            SwingUtils.mouse_def(this);
            this.dispose();

            //apro gestione matricole
            boolean aprireMatricole = false;
            String sqlm = "select gestione_matricola from articoli where codice = " + Db.pc(texCodiArti.getText(), Types.VARCHAR);
            String sret = null;
            try {
                sret = cu.toString(DbUtils.getObject(Db.getConn(), sqlm, false));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (aprireMatricolePre && !apertoLotti) {
                if (sret != null && sret.equalsIgnoreCase("S")) {
                    aprireMatricole = true;
                }
            }

            if (aprireMatricole && !apertoLotti) {
                JDialogMatricole dialogMatricole = new JDialogMatricole(main.getPadre(), true, CastUtils.toInteger0(texQta.getText()), Integer.parseInt(texRiga.getText()), texCodiArti.getText(), texSeri.getText(), texNume.getText(), texAnno.getText(), nomeTabMatricole, id_riga, id_padre);
                boolean acq = false;
                if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFattAcquisto")) {
                    acq = true;
                }
                if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestDocu")) {
                    frmTestDocu prov = (frmTestDocu) from;
                    acq = prov.acquisto;
                }
                if (acq) {
                    dialogMatricole.setMatricoleDaInserire(true);
                }

                dialogMatricole.setLocationRelativeTo(null);
                dialogMatricole.setVisible(true);
                //salvare insieme alla riga
                //inserisco le matricole
                ArrayList<String> matrs = new ArrayList();
                for (int i = 0; i < dialogMatricole.model.getRowCount(); i++) {
                    String matr = "";
                    if (dialogMatricole.model.getValueAt(i, 0) instanceof Giacenza) {
                        Giacenza giac = (Giacenza) dialogMatricole.model.getValueAt(i, 0);
                        matr = giac.getMatricola();
                    } else {
                        matr = (String) dialogMatricole.model.getValueAt(i, 0);
                    }
                    matrs.add(matr);
                    
                    String sql = "";
                    
                    //id_padre dentro le matricole è l'id della fattura
//                    sql = "insert into " + nomeTabMatricole + " (serie, numero, anno, riga, matricola, id_padre) values (";
//                    sql += " " + Db.pc(texSeri.getText(), Types.VARCHAR);
//                    sql += " , " + Db.pc(texNume.getText(), Types.INTEGER);
//                    sql += " , " + Db.pc(texAnno.getText(), Types.INTEGER);
//                    sql += " , " + Db.pc(texRiga.getText(), Types.INTEGER);
//                    sql += " , " + Db.pc(matr, Types.VARCHAR);
//                    sql += " , " + Db.pc(id_padre, Types.VARCHAR);
//                    sql += ")";
//                    Db.executeSql(sql);
                    
                    Map m = new HashMap();
                    m.put("matricola", matr);
                    m.put("id_padre_righe", id_riga);
                    sql = "insert into " + nomeTabMatricole + " set " + dbu.prepareSqlFromMap(m);
                    try {
                        dbu.tryExecQuery(Db.getConn(), sql);
                    } catch (Exception ex) {
                        SwingUtils.showExceptionMessage(this, ex);
                    }
                }
                System.out.println("id_riga : " + id_riga);
                String toadd = "";
                for (String m : matrs) {
                    toadd += "\nS/N: " + m;
                }
                if (from.getClass().getName().equalsIgnoreCase("gestioneFatture.frmTestFatt")) {
                    toglieSN(tab, id_riga);
                    String sql = "update righ_fatt set descrizione = CONCAT(descrizione,'" + StringUtils.replace(toadd, "'", "\\'") + "') where id = " + id_riga;
                    Db.executeSql(sql, true);
                } else if (id_riga != null) {
                    toglieSN(tab, id_riga);
                    String sql = "update " + tab + " set descrizione = CONCAT(descrizione,'" + StringUtils.replace(toadd, "'", "\\'") + "') where id = " + id_riga;
                    System.out.println("sql = " + sql);
                    Db.executeSql(sql, true);
                }
            }

            if (this.cheUpdateListino.isSelected()) {
                System.out.println("Codice Listino: " + this.codiceListino);
                System.out.println("Nuovo prezzo: " + this.texPrez.getText());
                System.out.println("prevvi iva inclusa: " + prezzi_ivati);
                System.out.println("Codice Articolo: " + this.texCodiArti.getText());
                System.out.println("Sconto1: " + this.texScon1.getText());
                System.out.println("Sconto2: " + this.texScon2.getText());

                String codListino = this.listinoUpdate;

                Double prezzo = Db.getDouble(this.texPrez.getText());
                boolean listino_prezzi_ivati = false;
                try {
                    listino_prezzi_ivati = cu.toBoolean(DbUtils.getObject(Db.getConn(), "select prezzi_ivati from tipi_listino where codice = " + Db.pc(codListino, Types.VARCHAR)));
                    if (listino_prezzi_ivati) {
                        prezzo = Db.getDouble(this.texPrezIvato.getText());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String codArticolo = this.texCodiArti.getText();
                Double sc1 = Db.getDouble(this.texScon1.getText());
                Double sc2 = Db.getDouble(this.texScon2.getText());

                String sqlControllo = "SELECT * FROM articoli_prezzi";
                sqlControllo += " WHERE articolo = " + Db.pc(codArticolo, Types.VARCHAR) + " AND listino = " + Db.pc(codListino, Types.VARCHAR);

                try {

                    ResultSet rs = Db.openResultSet(Db.getConn(), sqlControllo);
                    String sql = "";
                    if (rs.next()) {
                        sql = "UPDATE articoli_prezzi SET ";
                        sql += "prezzo = " + Db.pc(prezzo, Types.DOUBLE) + ", ";
                        sql += "sconto1 = " + Db.pc(sc1, Types.DECIMAL) + ", ";
                        sql += "sconto2 = " + Db.pc(sc2, Types.DECIMAL) + " ";
                        sql += "WHERE articolo = " + Db.pc(codArticolo, Types.VARCHAR) + " ";
                        sql += "AND listino = " + Db.pc(codListino, Types.VARCHAR);
                    } else {
                        ResultSet articolo = Db.openResultSet(Db.getConn(), "SELECT * FROM articoli WHERE codice = " + Db.pc(codArticolo, Types.VARCHAR));

                        if (articolo.next()) {
                            sql = "INSERT INTO articoli_prezzi SET ";
                            sql += "prezzo = " + Db.pc(prezzo, Types.DOUBLE) + ", ";
                            sql += "sconto1 = " + Db.pc(sc1, Types.DECIMAL) + ", ";
                            sql += "sconto2 = " + Db.pc(sc2, Types.DECIMAL) + ", ";
                            sql += "articolo = " + Db.pc(codArticolo, Types.VARCHAR) + ", ";
                            sql += "listino = " + Db.pc(codListino, Types.VARCHAR);
                        }
                    }

                    if (!sql.equals("")) {
                        Db.executeSql(sql);
                    }

                } catch (Exception e) {
                    SwingUtils.showErrorMessage(this, "Impossibile aggiornare prezzi di listino");
                }
            }

            //riattivo form di provenienza
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        main.getPadre().getDesktopPane().getDesktopManager().activateFrame((JInternalFrame) from);
                        ((JInternalFrame) from).setSelected(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } else {
            //controllo non ok
        }

    }

    class AggiornaResiduaWorker extends SwingWorker {

        public String codart;
        public String ret = null;

        @Override
        protected Object doInBackground() throws Exception {
            ret = "";
            //calcolo quantità residua...
//            ArrayList giacenza = Magazzino.getGiacenza(false, codart, null);
//            ArrayList giacenza = main.magazzino.getGiacenza(false, codart, null, null, false, true, true, deposito, null, true);
            ArrayList giacenza = main.magazzino.getGiacenza(false, codart, null, null, false, true, true, Magazzino.Depositi.TUTTI_RIEPILOGATIVO, null, true);
            double inarrivo = main.magazzino.getInArrivo(codart);
            double inuscita = main.magazzino.getInUscita(codart);
            double giacv = 0;
            DebugUtils.dump(giacenza);
            double giac = 0;
            try {
                giac = ((Giacenza) giacenza.get(0)).getGiacenza();
                giacv = giac + inarrivo - inuscita;
                String giac2 = " / qta in arrivo +" + b(FormatUtils.formatPerc(inarrivo)) + ", qta in uscita -" + b(FormatUtils.formatPerc(inuscita)) + ", qta teorica " + b(FormatUtils.formatPerc(giacv));
                DebugUtils.dump(giac);
                if (giac > 0) {
                    ret = "<html>Quantità residua: " + b(FormatUtils.formatPerc(giac)) + giac2 + "</html>";
                } else {
                    ret = "<html><font color='red'><b>Quantità residua: " + FormatUtils.formatPerc(giac) + "</b></font>" + giac2 + "</html>";
                }
            } catch (Exception e) {
                giacv = inarrivo - inuscita;
                String giac2 = " / qta in arrivo +" + b(FormatUtils.formatPerc(inarrivo)) + ", qta in uscita -" + b(FormatUtils.formatPerc(inuscita)) + ", qta teorica " + b(FormatUtils.formatPerc(giacv));
                ret = "<html>Quantità residua: 0" + giac2 + "</html>";
            }
            return ret;
        }

        @Override
        protected void done() {
            System.out.println("labResidua.setText(ret):" + ret);
            labResidua.setText(ret);
        }

        private String b(String val) {
            if (val.equals("0")) {
                return val;
            }
            return "<b>" + val + "</b>";
        }
    }

    private void recuperaDatiArticolo() {
        boolean servizio = false;
        boolean non_applicare_percentuale = false;
        recuperando = true;

        aggiornaResidua = new AggiornaResiduaWorker();
        aggiornaResidua.codart = texCodiArti.getText();
        System.out.println("... aggiornamento quantità residua per " + aggiornaResidua.codart);
        labResidua.setText("... aggiornamento quantità residua per " + aggiornaResidua.codart);
        aggiornaResidua.execute();

        if (this.texCodiArti.getText().length() > 0) {

            //li recupero dal cliente
            ResultSet temp;
            String sql = "select * from articoli";
            sql += " where codice = " + Db.pc(this.texCodiArti.getText(), "VARCHAR");
            temp = Db.openResultSet(sql);

            try {

                if (temp.next() == true) {

                    peso_kg_collo = temp.getDouble("peso_kg_collo");
                    servizio = CastUtils.toBoolean(temp.getString("servizio"));
                    non_applicare_percentuale = CastUtils.toBoolean(temp.getString("non_applicare_percentuale"));

                    boolean eng = false;
                    this.cheIsDescrizione.setSelected(Db.nz(temp.getString("is_descrizione"), "N").equals("S"));

                    if (this.codiceCliente >= 0) {

                        Cliente cliente = new Cliente(this.codiceCliente);

                        if (cliente.isItalian() == true) {
                            eng = false;
                        } else if (!main.fileIni.getValueBoolean("pref", "soloItaliano", true)) {
                            eng = true;
                        }
                    }

                    String um = null;
                    if (eng == true) {
                        um = Db.nz(temp.getString("um_en"), "");
                        this.texDescrizione.setText(Db.nz(temp.getString("descrizione_en"), ""));
                    } else {
                        um = Db.nz(temp.getString("um"), "");
                        this.texDescrizione.setText(Db.nz(temp.getString("descrizione"), ""));
                    }
                    if (StringUtils.isNotBlank(um)) {
                        comUm.setText(um);
                    }
                    //aggiungo codice fornite (codice assortimento)
                    if (main.pluginAttivi.contains("pluginToysforyou")) {
                        String cod_ass = cu.toString(temp.getString("codice_fornitore"));
                        if (StringUtils.isNotBlank(cod_ass)) {
                            texDescrizione.setText(texDescrizione.getText() + " (ass.to " + cod_ass + ")");
                        }
                    }

                    settaIva();

                    if (main.pluginClientManager) {
                        String campoPrezzo = "prezzo2";
                        if (acquisto) {
                            campoPrezzo = "prezzo1";
                        }
                        texPrez.setText(Db.formatDecimal5(temp.getDouble(campoPrezzo)));
                    } else {
                        boolean listino_a_ricarico = false;
                        Double ricarico_perc = null;
                        String ricarico_listino = null;
                        if (StringUtils.isBlank(codiceListino)) {
                            //prendo listino base
                            codiceListino = cu.toString(DbUtils.getObject(Db.getConn(), "select listino_base from dati_azienda"));
                        }
                        sql = "select ricarico_flag, ricarico_perc, ricarico_listino from tipi_listino where codice = " + Db.pc(codiceListino, Types.VARCHAR);
                        try {
                            List<Map> ret = DbUtils.getListMap(Db.getConn(), sql);
                            if (ret != null && ret.size() > 0) {
                                if (cu.toString(ret.get(0).get("ricarico_flag")).equalsIgnoreCase("S")) {
                                    listino_a_ricarico = true;
                                    ricarico_perc = cu.toDouble0(ret.get(0).get("ricarico_perc"));
                                    ricarico_listino = cu.toString(ret.get(0).get("ricarico_listino"));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        sql = "select prezzo, tipi_listino.*, sconto1, sconto2 from articoli_prezzi left join tipi_listino on articoli_prezzi.listino = tipi_listino.codice";
                        sql += " where articolo = " + Db.pc(this.texCodiArti.getText(), "VARCHAR");
                        sql += " and listino = " + Db.pc(this.codiceListino, java.sql.Types.VARCHAR);

                        ResultSet prezzi = Db.openResultSet(sql);

                        JTextField texQualePrezzo = texPrez;
                        if (prezzi_ivati) {
                            texQualePrezzo = texPrezIvato;
                        }
                        boolean prendere_prezzo_base = false;
                        boolean prezzi_trovati = false;
                        if (prezzi.next() == true) {
                            prezzi_trovati = true;
                        }
                        if (prezzi_trovati || listino_a_ricarico) {
                            if (prezzi_trovati) {
                                texScon1.setText(Db.formatDecimal(Math.max(prezzi.getDouble("sconto1"), cliente_sconto1r)));
                                texScon2.setText(Db.formatDecimal(Math.max(prezzi.getDouble("sconto2"), cliente_sconto2r)));
                                texQualePrezzo.setText(Db.formatDecimal5(temp.getDouble("prezzo1")));
                            }
                            if (listino_a_ricarico) {
                                double nuovo_prezzo = 0;
                                sql = "select prezzo from articoli_prezzi";
                                sql += " where articolo = " + Db.pc(texCodiArti.getText(), "VARCHAR");
                                sql += " and listino = " + Db.pc(ricarico_listino, java.sql.Types.VARCHAR);
                                ResultSet prezzi2 = Db.openResultSet(sql);
                                prezzi2.next();
                                if (non_applicare_percentuale) {
                                    nuovo_prezzo = prezzi2.getDouble("prezzo");
                                } else {
                                    nuovo_prezzo = prezzi2.getDouble("prezzo") * ((ricarico_perc + 100d) / 100d);
                                }
                                texQualePrezzo.setText(Db.formatDecimal5(nuovo_prezzo));
                            } else {
                                texQualePrezzo.setText(Db.formatDecimal5(prezzi.getDouble(1)));
                            }
                        } else {
                            prendere_prezzo_base = true;
                        }
                        if (main.pluginAttivi.contains("pluginToysforyou")) {
                            if (cu.toDouble0(texQualePrezzo.getText()) == 0) {
                                prendere_prezzo_base = true;
                            }
                        }
                        if (prendere_prezzo_base) {
                            sql = "select prezzo, tipi_listino.*, sconto1, sconto2 from articoli_prezzi left join tipi_listino on articoli_prezzi.listino = tipi_listino.codice";
                            sql += " where articolo = " + Db.pc(this.texCodiArti.getText(), "VARCHAR");
                            sql += " and listino = " + Db.pc(main.getListinoBase(), java.sql.Types.VARCHAR);
                            prezzi = Db.openResultSet(sql);

                            if (prezzi.next() == true) {
                                texScon1.setText(Db.formatDecimal(Math.max(prezzi.getDouble("sconto1"), cliente_sconto1r)));
                                texScon2.setText(Db.formatDecimal(Math.max(prezzi.getDouble("sconto2"), cliente_sconto2r)));
                                texQualePrezzo.setText(Db.formatDecimal5(temp.getDouble("prezzo1")));
                                if (prezzi.getString("ricarico_flag") != null && prezzi.getString("ricarico_flag").equals("S")) {
                                    double perc = prezzi.getDouble("ricarico_perc");
                                    double nuovo_prezzo = 0;
                                    sql = "select prezzo from articoli_prezzi";
                                    sql += " where articolo = " + Db.pc(this.texCodiArti.getText(), "VARCHAR");
                                    sql += " and listino = " + Db.pc(prezzi.getString("ricarico_listino"), java.sql.Types.VARCHAR);
                                    ResultSet prezzi2 = Db.openResultSet(sql);
                                    prezzi2.next();
                                    if (non_applicare_percentuale) {
                                        nuovo_prezzo = prezzi2.getDouble("prezzo");
                                    } else {
                                        nuovo_prezzo = prezzi2.getDouble("prezzo") * ((perc + 100d) / 100d);
                                    }
                                    texQualePrezzo.setText(Db.formatDecimal5(nuovo_prezzo));
                                } else {
                                    texQualePrezzo.setText(Db.formatDecimal5(prezzi.getDouble(1)));
                                }
                            }
                        }
                        if (main.pluginAttivi.contains("pluginToysforyou")) {
                            if (cu.toDouble0(texQualePrezzo.getText()) == 0) {
                                SwingUtils.showErrorMessage(this, "Attenzione non trovato prezzo !");
                            } else if (prendere_prezzo_base && !main.getListinoBase().equals(codiceListino)) {
                                SwingUtils.showInfoMessage(this, "Non è stato trovato il prezzo dal listino '" + codiceListino + "' ed è stato impostato il prezzo dal listino base '" + main.getListinoBase() + "'");
                            }
                        }

                    }
                    aggiorna_iva();
                    calcolaScontoDaPercSconto();

                    //controllo provvigione agente in base al fornitore
                    System.out.println("controllo se prendere provvigione da articolo/fornitore");
                    if (main.pluginAttivi.contains("pluginToysforyou")) {
                        Integer fornitore = cu.toInteger(temp.getObject("fornitore"));
                        Integer produttore = null;
                        try {
                            produttore = cu.toInteger(DbUtils.getObject(Db.getConn(), "select produttore_id from articoli where codice = " + Db.pc(texCodiArti.getText(), "VARCHAR")));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (produttore != null) {
                            //selezioni tutti i fornitori collegati a questo produttore
                            sql = "select c.codice, fp.cliente_id, fp.produttore_id, p.id, c.provvigione_predefinita_fornitore, c.ragione_sociale, l.ita   "
                                    + " from clie_forn c  "
                                    + " left join fornitori_produttori fp on c.codice = fp.cliente_id "
                                    + " left join produttori p on p.id = fp.produttore_id left join labels l on p.nome_lbl_id = l.id "
                                    + " where p.id = " + Db.pc(produttore, Types.INTEGER)
                                    + " and c.provvigione_predefinita_fornitore is not null";
                            System.out.println("sql = " + sql);
                            List<Map> listprods = DbUtils.getListMap(Db.getConn(), sql);
                            System.out.println("listprods = " + listprods);
                            Double provvigione = null;
                            if (listprods.size() == 0) {
                                //nessun produttore trovato
                            } else if (listprods.size() == 1) {
                                provvigione = cu.toDouble(listprods.get(0).get("provvigione_predefinita_fornitore"));
                            } else {
                                //più di un produttore far scegliere
                                KeyValuePair[] lista = new KeyValuePair[listprods.size()];
                                int i = 0;
                                for (Map m : listprods) {
                                    KeyValuePair kv = new KeyValuePair(i, m.get("provvigione_predefinita_fornitore") + "% per il produttore '" + m.get("ita") + "' dal fornitore '" + m.get("ragione_sociale") + "'");
                                    lista[i] = kv;
                                    i++;
                                }
                                KeyValuePair provscelta = (KeyValuePair) JOptionPane.showInputDialog(this,
                                        "Ci sono più fornitori per questo articolo\nScegli la provvigione da utilizzare",
                                        "Più fornitori/produttori per la selezione della provvigione",
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        lista,
                                        lista[0]);
                                System.out.println("provscelta = " + provscelta);
                                if (provscelta != null) {
                                    Map m = listprods.get(cu.toInteger(provscelta.key));
                                    provvigione = cu.toDouble(m.get("provvigione_predefinita_fornitore"));
                                }
                            }
                            if (provvigione != null) {
                                texProvvigione.setText(FormatUtils.formatPerc(provvigione, true));
                                Point loc = texProvvigione.getLocationOnScreen();
                                SwingUtils.showFlashMessage2("Ripresa provvigione agente del " + FormatUtils.formatPerc(provvigione, true) + "% in base al produttore articolo", 3, loc, Color.yellow);
                            }
                        }
                    } else {
                        Integer fornitore = cu.toInteger(temp.getObject("fornitore"));
                        if (fornitore != null) {
                            Double provvigione = cu.toDouble(DbUtils.getObject(Db.getConn(), "select provvigione_predefinita_fornitore from clie_forn where codice = '" + fornitore.toString() + "'"));
                            if (provvigione != null) {
                                texProvvigione.setText(FormatUtils.formatPerc(provvigione, true));
                                Point loc = texProvvigione.getLocationOnScreen();
                                SwingUtils.showFlashMessage2("Ripresa provvigione agente del " + FormatUtils.formatPerc(provvigione, true) + "% in base al fornitore articolo", 3, loc, Color.yellow);
                            }
                        }
                    }
                } else {
//                    javax.swing.JOptionPane.showMessageDialog(this, "Il codice articolo specificato non esiste in anagrafica !");
                    Point p = null;
                    if (texRicerca.hasFocus()) {
                        p = texRicerca.getLocationOnScreen();
                        p.translate(0, texRicerca.getHeight());
                    } else {
                        p = texCodiArti.getLocationOnScreen();
                        p.translate(0, texCodiArti.getHeight());
                    }
                    SwingUtils.showFlashMessage2("Il codice articolo specificato non esiste in anagrafica !", 2, p, new Color(230, 200, 100));
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

            main.events.fireInvoicexEvent(new InvoicexEvent(this, InvoicexEvent.TYPE_FRMNuovRigaDescrizioneMultiRigaNew_recuperaDatiArticoli_fine));
        }

        recuperando = false;
    }

    private boolean controlli() {
        if (main.getPersonalContain("erika") && from_frmTestDocu != null) {
            if (StringUtils.isBlank(texCodiArti.getText())) {
                FxUtils.fadeBackground(texCodiArti, Color.red);
                SwingUtils.showErrorMessage(this, "Inserire obbligatoriamente il codice articolo", "Attenzione", true);
                return false;
            }
            try {
                if (!DbUtils.containRows(Db.getConn(), "select codice from articoli where codice = " + Db.pc(texCodiArti.getText(), Types.VARCHAR))) {
                    FxUtils.fadeBackground(texCodiArti, Color.red);
                    SwingUtils.showErrorMessage(this, "Il codice articolo inserito non esiste", "Attenzione", true);
                    return false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (cu.d0(texQta.getText()) != 0 && cu.d0(texPrez.getText()) == 0) {
                FxUtils.fadeBackground(texPrez, Color.red);
                SwingUtils.showErrorMessage(this, "Inserire obbligatoriamente il prezzo", "Attenzione", true);
                return false;
            }
        }

        ResultSet temp = Db.lookUp(this.texIva.getText(), "codice", "codici_iva");
        try {
            if (temp == null) {
                if (this.texQta.getText().length() == 0 || this.texQta.getText().equals("0")) {
                    return true;
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Attenzione, CODICE IVA non presente", "Attenzione", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        } catch (Exception err) {
            javax.swing.JOptionPane.showMessageDialog(this, "Errore:" + err.toString());
            err.printStackTrace();
            return false;
        }

        //controllo che se in modifica non vadano ad inserire un numero riga già presente
        int riga = CastUtils.toInteger0(texRiga.getText());
        if (riga != dbRiga) {
            String sql = "";
            if (id_padre != null) {
                sql += "select riga from " + dati.dbNomeTabella + " where id_padre = " + id_padre + " and riga = " + riga;
            } else {
                System.err.println("non trovato id padre");
            }
            try {
                if (DbUtils.containRows(Db.getConn(), sql)) {
                    SwingUtils.showErrorMessage(this, "Riga " + riga + " già presente nel documento !");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
