/**
 * Invoicex Copyright (c) 2005,2006,2007,2008,2009 Marco Ceccarelli, Tnx snc
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
package it.tnx.invoicex.gui;

import it.tnx.Db;
import gestioneFatture.WizardDb;
import gestioneFatture.main;
import it.tnx.commons.DbUtils;
import it.tnx.commons.HttpUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.UnZip;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.sync.Sync;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingworker.SwingWorker;

/**
 *
 * @author test1
 */
public class JFrameWizardDb extends javax.swing.JFrame {

    public static boolean exit = true;
    public SwingWorker worker;

    /**
     * Creates new form JDialogWizardDb
     */
    public JFrameWizardDb() {
        super();
        initComponents();
        
        if (!main.debug) {
            sync.setSelected(false);
            sync.setVisible(false);
            pan3.setVisible(false);
        }
        
        try {
            setIconImage(main.getLogoIcon());
        } catch (Exception err) {
            err.printStackTrace();
        }
        toggleMono.setSelected(true);
        setComps();

        DocumentListener doclist = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                checksync();
            }

            public void removeUpdate(DocumentEvent e) {
                checksync();
            }

            public void changedUpdate(DocumentEvent e) {
                checksync();
            }
        };

        if (Sync.isActive()) {
            server.getDocument().addDocumentListener(doclist);
            nomedb.getDocument().addDocumentListener(doclist);
            username.getDocument().addDocumentListener(doclist);
            password.getDocument().addDocumentListener(doclist);
        }

    }

    
    final class CheckWorker extends SwingWorker {

        @Override
        protected Object doInBackground() throws Exception {
            //nomedb - "inv_test_sync_master"
            return Sync.checkLogin(server.getText(), nomedb.getText(), username.getText(), password.getText());
        }

        @Override
        protected void done() {
            try {
                if (cu.toBoolean(get())) {
                    lab_sync.setText("sync attivo");
                } else {
                    lab_sync.setText("sync non attivo");
                }                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    
    };
    
    CheckWorker wcheck = null;
    
    private void checksync() {
        lab_sync.setText(" ");
        if (StringUtils.isNotBlank(server.getText())
                && StringUtils.isNotBlank(nomedb.getText())
                && StringUtils.isNotBlank(username.getText())
                && StringUtils.isNotBlank(password.getText())) {
            
            //controllare se db sync
            if (wcheck != null && !wcheck.isDone()) {
                wcheck.cancel(true);
            }
            lab_sync.setText("...");
            wcheck = new CheckWorker();
            wcheck.execute();

        }
    }

    private boolean creaDbEsterno() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String params = "&jdbcCompliantTruncation=false&zeroDateTimeBehavior=round&emptyStringsConvertToZero=true&autoReconnect=true&sessionVariables=sql_mode='ALLOW_INVALID_DATES'&allowMultiQueries=true&useCompression=false";
            String url = "jdbc:mysql://" + server.getText() + "/?user=" + username.getText() + "&password=" + password.getText() + params;
            Connection conn = DriverManager.getConnection(url, username.getText(), password.getText());

            //salvo impostazioni
            main.startDbCheck = false;
            main.startConDbCheck = false;

            Statement stat = conn.createStatement();

            //controllo se esiste già non lo creo
            boolean esiste = false;
            try {
                ResultSet r = stat.executeQuery("select * from " + nomedb.getText() + ".clie_forn");
                r.close();
                esiste = true;
            } catch (SQLException sqlerr) {
                System.out.println(sqlerr + " / " + sqlerr.getStackTrace()[0]);
            }

            if (!esiste) {
                if (JOptionPane.showConfirmDialog(this, "Il database " + nomedb.getText() + " non esiste, proseguendo verrà creato", "Attenzione", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    salvaDatiEsterno();
                    //creo il db
                    try {
                        stat.execute("create database IF NOT EXISTS " + nomedb.getText() + " CHARACTER SET utf8 COLLATE utf8_general_ci");
                        stat.execute("use " + nomedb.getText());

                        WizardDb.progress.setLocationRelativeTo(this);
                        WizardDb.progress.setAlwaysOnTop(true);
                        WizardDb.progress.setVisible(true);
                        WizardDb.progress.labStatus.setText("creazione database iniziale");

                        boolean ret_creadb = creazioneDb(conn);
                        return ret_creadb;
                    } catch (Throwable ex) {
                        JDialogExc exc = new JDialogExc(this, true, ex);
                        exc.setVisible(true);
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Il database " + nomedb.getText() + " esiste già e verrà utilizzato da questa postazione", "Informazione", JOptionPane.INFORMATION_MESSAGE);
                salvaDatiEsterno();
            }
        } catch (Exception e) {
            e.printStackTrace();
            String problema = "";
            if (e.getCause() != null) {
                String causa = e.getCause().getClass().getSimpleName();
                String dettaglio = e.getCause().getLocalizedMessage();
                problema = causa + " " + dettaglio;
                if (causa.equalsIgnoreCase("UnknownHostException")) {
                    problema += "<br><br><b>Hai un problema di rete e/o firewall per il quale non si riesce a raggiungere il server <br>Se stai provando a collegarti su un server in hosting probabilmente il fornitore blocca la porta dall'esterno</b>";
                }
            } else {
                problema = e.getLocalizedMessage();
                if (problema.toLowerCase().indexOf("using password: yes") >= 0) {
                    problema += "<br><br><b>Il server è raggiungibile ma il nome utente o la password sono errati";
                    problema += "<br>Oppure l'utente non il permesso per accedere dall'esterno";
                    problema += "</b>";
                }
            }
            JOptionPane.showMessageDialog(this, "<html>Impossibile collegarsi al server <b>" + server.getText() + "</b><br>Controlla i dati di connessione e riprova<br>Problema: " + problema + "</html>", "Attenzione", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean creaDbLocale() {
        return creaDbLocale(false);
    }
    
    private boolean creaDbLocale(boolean perSync) {

        System.out.println("creaDbLocale");
        
        if (perSync) {
            System.out.println("creaDbLocale perSync:true");
            
            //registro il client id
            Integer idclient = Sync.presentaRegistrazioneClient();
            if (idclient != null) {
                System.out.println("idclient = " + idclient);
//                    SwingUtils.showInfoMessage(this, "Registrato id client: " + idclient);
            } else {
                SwingUtils.showErrorMessage(this, "Errore nella registrazione del client");
                main.exitMain();
            }
            
        }
        
        main.startDbCheck = true;

//        SwingUtils.showFlashMessage("...avvio database locale...", 3);
        WizardDb.progress.setLocationRelativeTo(null);
//        WizardDb.progress.setAlwaysOnTop(true);
        WizardDb.progress.setVisible(true);
        WizardDb.progress.progressbar.setIndeterminate(true);
        WizardDb.progress.labStatus.setText("...avvio database locale..");

        if (startDb(perSync)) {

            System.out.println("creazione db");

            try {
                if (DbUtils.containRows(Db.getConn(true), "show databases like 'invoicex_default'")) {
                    //il database esiste già
                    try {
                        WizardDb.progress.setVisible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        main.splash.setVisible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    SwingUtils.showErrorMessage(WizardDb.wizard, "E' già presente un database invoicex_default, non è possibile continuare\nPer forzare l'installazione devi spostare la cartella Invoicex\\mysql\\data\\invoicex_default", true);
                    if (exit) {
                        System.exit(1);
                    }
                    dispose();
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Db.executeSql("create database IF NOT EXISTS invoicex_default CHARACTER SET utf8 COLLATE utf8_general_ci");
            Db.executeSql("use invoicex_default");

            WizardDb.progress.progressbar.setIndeterminate(false);
            WizardDb.progress.labStatus.setText("Creazione database iniziale");

            boolean ret_creadb = creazioneDb(Db.getConn(), perSync);
            
            if (perSync) {
                if (Sync.dati_registrazione == null) {
                    SwingUtils.showErrorMessage(WizardDb.wizard, "Errore: dati di registrazione client nulli", true);
                    main.exitMain();
                } else {
                    //memorizzo su db
                    String sql = "delete from sync_config";
                    try {
                        dbu.tryExecQuery(Db.getConn(), sql);
                        Map rec = new HashMap();
                        rec.put("client_id", Sync.dati_registrazione.id);
                        rec.put("client_name", Sync.dati_registrazione.client_name);
                        sql = "insert into sync_config set " + dbu.prepareSqlFromMap(rec);
                        dbu.tryExecQuery(Db.getConn(), sql);                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        SwingUtils.showExceptionMessage(WizardDb.wizard, e);
                        main.exitMain();
                    }
                }
            }
            
            main.db_gia_avviato_da_wizard = true;
            
            return ret_creadb;
        } else {
            try {
                WizardDb.progress.setVisible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                main.splash.setVisible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                JDialogExc de = new JDialogExc(WizardDb.wizard, true, null);
                de.labInt.setFont(de.labInt.getFont().deriveFont(Font.BOLD, 14));
                de.labInt.setText("Errore nell'avvio del database locale");
                de.labe.setFont(de.labInt.getFont().deriveFont(Font.PLAIN, 12));
                de.setLocationRelativeTo(null);
                de.pack();
                de.setVisible(true);
                if (exit) {
                    System.exit(1);
                }
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean creazioneDb(final Connection conn) {
        return creazioneDb(conn, false);
    }
    
    private boolean creazioneDb(final Connection conn, boolean perSync) {
        boolean ok = false;
        
        if (!perSync) {
            FileInputStream fis = null;
            try {

//                File fe = new File(main.wd + "mysql/createdb_1_8_7.sql");
                File fe = new File(main.wd + "mysql/create_dbv1.sql");
                if (!fe.exists()) {
                    //scarico se non presente
                    SwingUtils.showFlashMessage2("Scaricamento in corso di " + fe.getName(), 3, null, Color.red, new Font(null, Font.BOLD, 16), true);
                    String nomefile = fe.getName();
                    String nomefilezip = StringUtils.substringBeforeLast(nomefile, ".") + ".zip";
                    try {
                        File filezip = new File(nomefilezip);
                        if (filezip.exists()) {
                            filezip.delete();
                        }
                        String url = main.baseurlserver + "/download/invoicex/mysql/" + nomefilezip;
                        String filelocale = "mysql/" + nomefilezip;
                        HttpUtils.saveBigFile(url, filelocale);
                        UnZip.unzip(new File(filelocale), "mysql/");
                    } catch (Exception e) {
                        e.printStackTrace();
                        SwingUtils.showErrorMessage(this, e.getMessage());
                        System.exit(1);
                    }
                }
                fis = new FileInputStream(fe);
                String sql = "";
                byte[] buff2 = new byte[(int) fe.length()];
                fis.read(buff2);
                sql = new String(buff2);

                String mysqlversion = "5.1";
                try {
                    mysqlversion = cu.s(dbu.getListMap(Db.getConn(), "SHOW VARIABLES LIKE \"version\";").get(0).get("Value"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Integer mysqlmajorver = cu.i(StringUtils.substringBefore(mysqlversion, "."));
                Integer mysqlminorver = cu.i(StringUtils.substringBefore(StringUtils.substringAfter(mysqlversion, "."), "."));
                if (mysqlmajorver == null) mysqlmajorver = 5;
                if (mysqlminorver == null) mysqlmajorver = 1;
                
                Statement stat;
                try {
                    stat = conn.createStatement();
                    String[] sqls = sql.split(";\\r\\n");
                    int conta = 0;
                    WizardDb.progress.progressbar.setIndeterminate(false);
                    WizardDb.progress.progressbar.setMinimum(0);
                    WizardDb.progress.progressbar.setMaximum(100);
                    for (int i = 0; i < sqls.length; i++) {
                        conta += sqls[i].length();
                        String sqlc = sqls[i];
                        
                        if (sqlc.toLowerCase().startsWith("set storage_engine") && ((mysqlmajorver == 5 && mysqlminorver >= 5) || mysqlmajorver >= 6)) {
                            sqlc = StringUtils.replace(sqlc, "storage_engine", "default_storage_engine");
                        }
                        try {
                            stat.execute(sqlc);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("sqlc di errore:" + sqlc);
                        }
                        if (conta % 100 == 0) {
                            int perc = conta * 100 / buff2.length;
                            WizardDb.progress.labStatus.setText("Creazione database iniziale " + perc + "%");
                            WizardDb.progress.progressbar.setValue(perc);
                        }
                    }
                    stat.close();
                } catch (Exception err) {
                    err.printStackTrace();
                }

                System.out.println("fine creazione db");

//                main.stopdb(false);   //non stoppo più per db_gia_avviato_da_wizard

                ok = true;
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(JFrameWizardDb.class.getName()).log(Level.SEVERE, null, ex);
                JDialogExc exc = new JDialogExc(WizardDb.wizard, true, ex);
                exc.setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(JFrameWizardDb.class.getName()).log(Level.SEVERE, null, ex);
                JDialogExc exc = new JDialogExc(WizardDb.wizard, true, ex);
                exc.setVisible(true);
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(JFrameWizardDb.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            //con Sync, scarico dump dal sync master
            try {
                //scarico dump 
                String filename = Sync.scaricaDump();
                final File f = new File(filename);
                SwingWorker w = new SwingWorker() {

                    @Override
                    protected Object doInBackground() throws Exception {
                        InvoicexUtil.ripristinaDump(f, this);
                        return null;
                    }

                    @Override
                    protected void process(List chunks) {
                        for (Object chunk : chunks) {
                            if (chunk instanceof int[]) {
                                int[] vals = (int[]) chunk;
                                WizardDb.progress.progressbar.setMaximum(vals[1]);
                                WizardDb.progress.progressbar.setValue(vals[0]);
                                if (WizardDb.progress.progressbar.isIndeterminate()) {
                                    WizardDb.progress.progressbar.setIndeterminate(false);
                                }
                            } else {
                                WizardDb.progress.labStatus.setText(chunk.toString());
                            }
                        }
                    }
                    
                };
                WizardDb.progress.progressbar.setIndeterminate(false);
                w.execute();
                w.get();
                System.out.println("ripristino sync finito");
                
//                main.stopdb(false);
                ok = true;
//                Thread.sleep(2000);
                
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(this, e);
                main.exitMain();
            }
            
            
        }
        return ok;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        panCentrale = new javax.swing.JPanel();
        pan1 = new javax.swing.JPanel();
        toggleMono = new javax.swing.JToggleButton();
        pan2 = new javax.swing.JPanel();
        toggleRete = new javax.swing.JToggleButton();
        panRete = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        server = new javax.swing.JTextField();
        nomedb = new javax.swing.JTextField();
        username = new javax.swing.JTextField();
        password = new javax.swing.JPasswordField();
        sync = new javax.swing.JCheckBox();
        lab_sync = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        pan3 = new javax.swing.JPanel();
        toggleCloud = new javax.swing.JToggleButton();
        panCloud = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        sync_email = new javax.swing.JTextField();
        sync_pass = new javax.swing.JPasswordField();
        jXHyperlink12 = new org.jdesktop.swingx.JXHyperlink();
        jSeparator2 = new javax.swing.JSeparator();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Invoicex - primo avvio");
        setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        addWindowListener(formListener);
        getContentPane().setLayout(new java.awt.BorderLayout(5, 5));

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD, jLabel1.getFont().getSize()+3));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("E' la prima volta che esegui Invoicex, scegli come utilizzarlo");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setPreferredSize(new java.awt.Dimension(402, 40));
        getContentPane().add(jLabel1, java.awt.BorderLayout.NORTH);

        jPanel2.setPreferredSize(new java.awt.Dimension(255, 60));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 15));

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/process-stop.png"))); // NOI18N
        jButton2.setText("Chiudi");
        jButton2.setPreferredSize(new java.awt.Dimension(110, 35));
        jButton2.addActionListener(formListener);
        jPanel2.add(jButton2);

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-redo.png"))); // NOI18N
        jButton1.setText("Prosegui");
        jButton1.setPreferredSize(new java.awt.Dimension(130, 35));
        jButton1.addActionListener(formListener);
        jPanel2.add(jButton1);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        panCentrale.setLayout(new java.awt.GridBagLayout());

        toggleMono.setFont(toggleMono.getFont().deriveFont(toggleMono.getFont().getStyle() | java.awt.Font.BOLD, toggleMono.getFont().getSize()+1));
        toggleMono.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/22x22/places/network-server.png"))); // NOI18N
        toggleMono.setText("<html>Usa Invoicex solo<br> su questo computer</html>");
        toggleMono.setToolTipText("Viene utilizzato un database interno al programma");
        toggleMono.setPreferredSize(new java.awt.Dimension(183, 45));
        toggleMono.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout pan1Layout = new org.jdesktop.layout.GroupLayout(pan1);
        pan1.setLayout(pan1Layout);
        pan1Layout.setHorizontalGroup(
            pan1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan1Layout.createSequentialGroup()
                .addContainerGap()
                .add(toggleMono, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );
        pan1Layout.setVerticalGroup(
            pan1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan1Layout.createSequentialGroup()
                .addContainerGap()
                .add(toggleMono, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(182, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        panCentrale.add(pan1, gridBagConstraints);

        toggleRete.setFont(toggleRete.getFont().deriveFont(toggleRete.getFont().getStyle() | java.awt.Font.BOLD, toggleRete.getFont().getSize()+1));
        toggleRete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/22x22/places/network-workgroup.png"))); // NOI18N
        toggleRete.setText("Usa Invoicex in rete");
        toggleRete.setToolTipText("Viene utilizzato un database Mysql 5.x esterno");
        toggleRete.setMinimumSize(new java.awt.Dimension(300, 51));
        toggleRete.setPreferredSize(new java.awt.Dimension(179, 45));
        toggleRete.addActionListener(formListener);

        panRete.setBorder(javax.swing.BorderFactory.createTitledBorder("Impostazioni Mysql esterno"));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Server");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Nome db");
        jLabel3.setToolTipText("Attenzione, se il db sul server esiste verrà usato da questa postazione di Invoicex, se non esiste verrà creato");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Username");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Password");

        server.setColumns(15);

        nomedb.setColumns(15);
        nomedb.setToolTipText("Attenzione, se il db sul server esiste verrà usato da questa postazione di Invoicex, se non esiste verrà creato");

        username.setColumns(15);

        password.setColumns(15);

        org.jdesktop.layout.GroupLayout panReteLayout = new org.jdesktop.layout.GroupLayout(panRete);
        panRete.setLayout(panReteLayout);
        panReteLayout.setHorizontalGroup(
            panReteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panReteLayout.createSequentialGroup()
                .addContainerGap()
                .add(panReteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panReteLayout.createSequentialGroup()
                        .add(panReteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(panReteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, nomedb)
                            .add(server)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, panReteLayout.createSequentialGroup()
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(username))
                    .add(panReteLayout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(password)))
                .addContainerGap())
        );

        panReteLayout.linkSize(new java.awt.Component[] {jLabel2, jLabel3, jLabel4, jLabel5}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        panReteLayout.setVerticalGroup(
            panReteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panReteLayout.createSequentialGroup()
                .addContainerGap()
                .add(panReteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(server, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panReteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(nomedb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panReteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panReteLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sync.setText("sync");
        sync.addItemListener(formListener);
        sync.addActionListener(formListener);

        lab_sync.setText(" ");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        org.jdesktop.layout.GroupLayout pan2Layout = new org.jdesktop.layout.GroupLayout(pan2);
        pan2.setLayout(pan2Layout);
        pan2Layout.setHorizontalGroup(
            pan2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan2Layout.createSequentialGroup()
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8)
                .add(pan2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pan2Layout.createSequentialGroup()
                        .add(sync)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lab_sync, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(toggleRete, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(panRete, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pan2Layout.linkSize(new java.awt.Component[] {panRete, toggleRete}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        pan2Layout.setVerticalGroup(
            pan2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan2Layout.createSequentialGroup()
                .add(11, 11, 11)
                .add(toggleRete, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panRete, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pan2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sync)
                    .add(lab_sync))
                .addContainerGap(8, Short.MAX_VALUE))
            .add(pan2Layout.createSequentialGroup()
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 230, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        panCentrale.add(pan2, gridBagConstraints);

        toggleCloud.setFont(toggleCloud.getFont().deriveFont(toggleCloud.getFont().getStyle() | java.awt.Font.BOLD, toggleCloud.getFont().getSize()+1));
        toggleCloud.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Shared-25.png"))); // NOI18N
        toggleCloud.setText("Usa Invoicex in Cloud");
        toggleCloud.setMinimumSize(new java.awt.Dimension(300, 39));
        toggleCloud.setPreferredSize(new java.awt.Dimension(191, 45));
        toggleCloud.addActionListener(formListener);

        panCloud.setBorder(javax.swing.BorderFactory.createTitledBorder("Accesso"));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("E-mail");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Password");

        sync_email.setColumns(15);

        sync_pass.setColumns(15);

        jXHyperlink12.setText("Password dimenticata ?");
        jXHyperlink12.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout panCloudLayout = new org.jdesktop.layout.GroupLayout(panCloud);
        panCloud.setLayout(panCloudLayout);
        panCloudLayout.setHorizontalGroup(
            panCloudLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panCloudLayout.createSequentialGroup()
                .addContainerGap()
                .add(panCloudLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panCloudLayout.createSequentialGroup()
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sync_email))
                    .add(panCloudLayout.createSequentialGroup()
                        .add(jLabel9)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sync_pass))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, panCloudLayout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(jXHyperlink12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        panCloudLayout.linkSize(new java.awt.Component[] {jLabel6, jLabel9}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        panCloudLayout.setVerticalGroup(
            panCloudLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panCloudLayout.createSequentialGroup()
                .addContainerGap()
                .add(panCloudLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(sync_email, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panCloudLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(sync_pass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jXHyperlink12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        org.jdesktop.layout.GroupLayout pan3Layout = new org.jdesktop.layout.GroupLayout(pan3);
        pan3.setLayout(pan3Layout);
        pan3Layout.setHorizontalGroup(
            pan3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan3Layout.createSequentialGroup()
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8)
                .add(pan3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(toggleCloud, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(panCloud, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pan3Layout.linkSize(new java.awt.Component[] {panCloud, toggleCloud}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        pan3Layout.setVerticalGroup(
            pan3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pan3Layout.createSequentialGroup()
                .add(pan3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pan3Layout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(toggleCloud, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(panCloud, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(38, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        panCentrale.add(pan3, gridBagConstraints);

        getContentPane().add(panCentrale, java.awt.BorderLayout.CENTER);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.ItemListener, java.awt.event.WindowListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == jButton2) {
                JFrameWizardDb.this.jButton2ActionPerformed(evt);
            }
            else if (evt.getSource() == jButton1) {
                JFrameWizardDb.this.jButton1ActionPerformed(evt);
            }
            else if (evt.getSource() == toggleMono) {
                JFrameWizardDb.this.toggleMonoActionPerformed(evt);
            }
            else if (evt.getSource() == toggleRete) {
                JFrameWizardDb.this.toggleReteActionPerformed(evt);
            }
            else if (evt.getSource() == toggleCloud) {
                JFrameWizardDb.this.toggleCloudActionPerformed(evt);
            }
            else if (evt.getSource() == jXHyperlink12) {
                JFrameWizardDb.this.jXHyperlink12ActionPerformed(evt);
            }
            else if (evt.getSource() == sync) {
                JFrameWizardDb.this.syncActionPerformed(evt);
            }
        }

        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            if (evt.getSource() == sync) {
                JFrameWizardDb.this.syncItemStateChanged(evt);
            }
        }

        public void windowActivated(java.awt.event.WindowEvent evt) {
        }

        public void windowClosed(java.awt.event.WindowEvent evt) {
        }

        public void windowClosing(java.awt.event.WindowEvent evt) {
            if (evt.getSource() == JFrameWizardDb.this) {
                JFrameWizardDb.this.formWindowClosing(evt);
            }
        }

        public void windowDeactivated(java.awt.event.WindowEvent evt) {
        }

        public void windowDeiconified(java.awt.event.WindowEvent evt) {
        }

        public void windowIconified(java.awt.event.WindowEvent evt) {
        }

        public void windowOpened(java.awt.event.WindowEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setta(false);
        SwingWorker work1 = new SwingWorker() {

            @Override
            protected void done() {
                super.done();
                setta(true);
            }

            @Override
            protected Object doInBackground() throws Exception {
                //controlli
                boolean ret = false;
                if (toggleMono.isSelected()) {
                    //creo il db in locale
                    ret = creaDbLocale();
                } else {
                    if (sync.isSelected()) {
                        if (Sync.checkLogin(server.getText(), nomedb.getText(), username.getText(), password.getText())) {
                            //vado avanti con db locale in sync remoto
                            main.fileIni.setValue("sync", "attivo", true);
                            Sync.setActive(true);
                            ret = creaDbLocale(true);
                            if (ret) {
                                main.fileIni.setValue("sync", "server", Sync.server);
                                main.fileIni.setValue("sync", "nomedb", Sync.nomedb);
                                main.fileIni.setValue("sync", "username", Sync.username);
                                main.fileIni.setValueCifrato("sync", "password", Sync.password);
                            }
                        } else {
                            JOptionPane.showMessageDialog(JFrameWizardDb.this, "Impossibile collegarsi al server", "Attenzione", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        //controllo se si collega, altrimenti creo il db esterno e riprovo a collegarsi
                        if (server.getText().trim().length() == 0 || nomedb.getText().trim().length() == 0 || username.getText().trim().length() == 0) {
                            JOptionPane.showMessageDialog(JFrameWizardDb.this, "Inserire i dati necessari per il collegamento al Mysql esterno", "Attenzione", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            ret = creaDbEsterno();
                        }
                    }
                }

                if (!ret) {
                    //faccio rimanere in attesa che si riprovi altrimenti chiuderanno via annulla
//                    JOptionPane.showMessageDialog(null, "Per avviare il programma è necessario terminare la procedura di configurazione database", "Errore", JOptionPane.INFORMATION_MESSAGE);
//                    System.exit(0);
                } else {
                    main.fileIni.setValue("wizard", "eseguito", "S");
                    main.fileIni.saveFile();
                    WizardDb.ok = true;
                    JFrameWizardDb.this.dispose();
                    main.INSTANCE.post_wizard();
                    main.wizard_in_corso = false;
                }

                return null;
            }
        };
        work1.execute();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        System.out.println("exit?" + exit);
        if (exit) {
            System.exit(0);
        }
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        jButton2ActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    private void jXHyperlink12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jXHyperlink12ActionPerformed
        try {
            SwingUtils.openUrl(new URL("http://www.invoicex.it/Recupera-password/"));
        } catch (Exception err) {
            err.printStackTrace();
        }
    }//GEN-LAST:event_jXHyperlink12ActionPerformed

    private void toggleCloudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleCloudActionPerformed
        toggleMono.getModel().setSelected(false);
        toggleRete.getModel().setSelected(false);
        toggleCloud.getModel().setSelected(true);
        setComps();
    }//GEN-LAST:event_toggleCloudActionPerformed

    private void syncItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_syncItemStateChanged
        if (sync.isSelected()) {
//            username.setEnabled(false);
//            username.setText(nomedb.getText());
        } else {
//            username.setEnabled(true);
        }
    }//GEN-LAST:event_syncItemStateChanged

    private void toggleReteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleReteActionPerformed
        toggleMono.getModel().setSelected(false);
        toggleRete.getModel().setSelected(true);
        toggleCloud.getModel().setSelected(false);
        setComps();
    }//GEN-LAST:event_toggleReteActionPerformed

    private void toggleMonoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleMonoActionPerformed
        toggleMono.getModel().setSelected(true);
        toggleRete.getModel().setSelected(false);
        toggleCloud.getModel().setSelected(false);
        setComps();
    }//GEN-LAST:event_toggleMonoActionPerformed

    private void syncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_syncActionPerformed

    private void setta(boolean val) {
        jButton1.setEnabled(val);
        jButton2.setEnabled(val);
        toggleMono.setEnabled(val);
        toggleRete.setEnabled(val);
    }

    private void setComps() {
        panRete.setEnabled(toggleRete.getModel().isSelected());
        server.setEnabled(toggleRete.getModel().isSelected());
        nomedb.setEnabled(toggleRete.getModel().isSelected());
        username.setEnabled(toggleRete.getModel().isSelected());
        password.setEnabled(toggleRete.getModel().isSelected());
        
        sync_pass.setEnabled(toggleCloud.getModel().isSelected());
        sync_email.setEnabled(toggleCloud.getModel().isSelected());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton jButton1;
    public javax.swing.JButton jButton2;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JLabel jLabel4;
    public javax.swing.JLabel jLabel5;
    public javax.swing.JLabel jLabel6;
    public javax.swing.JLabel jLabel9;
    public javax.swing.JPanel jPanel2;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.JSeparator jSeparator2;
    public org.jdesktop.swingx.JXHyperlink jXHyperlink12;
    public javax.swing.JLabel lab_sync;
    public javax.swing.JTextField nomedb;
    public javax.swing.JPanel pan1;
    public javax.swing.JPanel pan2;
    public javax.swing.JPanel pan3;
    public javax.swing.JPanel panCentrale;
    public javax.swing.JPanel panCloud;
    public javax.swing.JPanel panRete;
    public javax.swing.JPasswordField password;
    public javax.swing.JTextField server;
    public javax.swing.JCheckBox sync;
    public javax.swing.JTextField sync_email;
    public javax.swing.JPasswordField sync_pass;
    public javax.swing.JToggleButton toggleCloud;
    public javax.swing.JToggleButton toggleMono;
    public javax.swing.JToggleButton toggleRete;
    public javax.swing.JTextField username;
    // End of variables declaration//GEN-END:variables

    private boolean startDb() {
        return startDb(false);
    }
    
    private boolean startDb(boolean perSync) {
        String oldNameDb = Db.dbNameDB;
        try {
            Db.dbNameDB = "mysql";
            if (System.getProperty("os.name").toLowerCase().indexOf("win") < 0) {
                System.out.println("creaDbLocale:startDb:non win");
                //con windows utilizzo named pipes e non ho bisogno di porte aperte
                //test porte db
                //trovo una porta libera per mysql
                Db connessioneTest = new Db();
                if (main.startDbCheck) {
                    System.out.println("creaDbLocale:startDb:non win:test porte");
                    int portaMin = 3306;
                    int portaMax = portaMin + 10;
                    int portaProva = portaMin;
                    boolean portaOk = false;
                    while (portaOk == false) {
                        //primo test con socket
                        try {
                            Socket client = new Socket();
                            client.connect(new InetSocketAddress("127.0.0.1", portaProva), 2000);
                            portaProva++;
                            client.close();
                        } catch (IOException ioexp) {
                            //errore quindi dovrebbe essere libera..
                            //secondo test con server socket
                            try {
                                ServerSocket socket = new ServerSocket(portaProva);
                                portaOk = true;
                                Db.dbPort = portaProva;
                                main.dbPortaOk = portaProva;
                                socket.close();
                            } catch (IOException ioexp2) {
                                portaProva++;
                            }
                        }
                    }
                    if (portaOk == false) {
                        JOptionPane.showMessageDialog(null, "Impossibile attivare il database: nessuna porta libera da " + portaMin + " a " + portaMax, "Errore", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    System.out.println("creaDbLocale:startDb:non win:test porte:" + Db.dbPort);
                } else {
                    if (Db.dbPort == 0) {
                        Db.dbPort = 3306;
                    }
                }
            }

            try {
                if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                    System.out.println("startdb per win");
                    main.startDb = main.win_startDb;
                } else if (System.getProperty("os.name").toLowerCase().indexOf("lin") >= 0) {
                    System.out.println("startdb per lin");
                    main.startDb = StringUtils.replace(main.lin_startDb, "{port}", String.valueOf(Db.dbPort));
                } else if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
                    System.out.println("startdb per mac");
                    main.startDb = StringUtils.replace(main.mac_startDb, "{port}", String.valueOf(Db.dbPort));
                }
                main.fileIni.setValue("db", "startdb", main.startDb);
            } catch (Exception err) {
                err.printStackTrace();
            }

            //controllo se non fosse già in esecuzione altro mysql
            Db connessione = new Db();
            if (main.startDbCheck) {
                Db.dbServ = "127.0.0.1:" + main.dbPortaOk;
                Db.dbPort = main.dbPortaOk;
                if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                    Db.useNamedPipes = true;
                }
                
                if (perSync) {
                    Db.dbName = "root";
                    if (main.fileIni.existKey("db", "pwd_interno")) {
                        Db.dbPass = main.fileIni.getValueCifrato("db", "pwd_interno");
                    } else {
                        Db.dbPass = "ohfgfesmmc666";
                    }                    
                }
            }
            if (connessione.dbConnect(true)) {
                System.out.println("prova connessione pre:ok");
                try {
                    String datadir = (String) DbUtils.getListArray(connessione.conn, "show variables like 'datadir'").get(0)[1];
                    System.out.println("datadir  = " + datadir);
                    File datadir2f = new File("");
                    String datadir2 = datadir2f.getAbsolutePath() + File.separator + "mysql" + File.separator + "data";
                    System.out.println("datadir2 = " + datadir2);
                    if (!datadir.equalsIgnoreCase(datadir2)) {
                        SwingUtils.showErrorMessage(this, "Invoicex sembra già in esecuzione e non puoi continuare\nPer continuare chiudi Invoicex o altre istanze di mysql", true);
                        if (exit) {
                            System.exit(1);
                        }
                        dispose();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (main.startDbCheck) {
                if (main.flagWebStart == false) {
                    main.startdb();
                }
            }

            if (main.flagWebStart == false) {
                //faccio test di connessione
                int proveConn = 0;
                boolean connOk = false;
                while (!connOk && proveConn < 2) {
                    System.out.println("prova connessione:" + proveConn);
                    if (connessione.dbConnect(true)) {
                        connOk = true;
                        System.out.println("prova connessione:ok");
                    } else {
                        System.out.println("prova connessione:ko aspetto...");
                        try {
                            Thread.sleep(3000);
                            if (main.startDbCheck) {
                                //tento stop e ritento start
                                main.stopdb(false);
                                Thread.sleep(3000);
                                main.startdb();
                                Thread.sleep(3000);
                            }
                        } catch (InterruptedException ierr) {
                        }
                    }
                    proveConn++;
                }

                if (!connOk && !connessione.dbConnect(true)) {
                    return false;
                }
            }

        } finally {
            Db.dbNameDB = oldNameDb;
        }

        return true;
    }

    private void salvaDatiEsterno() {
        main.fileIni.setValue("db", "server", server.getText());
        main.fileIni.setValue("db", "nome_database", nomedb.getText());
        main.fileIni.setValue("db", "user", username.getText());
        main.fileIni.setValueCifrato("db", "pwd", password.getText());
        main.fileIni.setValue("db", "startdbcheck", "N");
        main.fileIni.saveFile();
        main.loadIni();
        Db.useNamedPipes = false;
    }
}
