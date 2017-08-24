/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * JDialogLotti.java
 *
 * Created on 5-gen-2010, 17.42.40
 */
package it.tnx.invoicex.gui;

import gestioneFatture.main;
import it.tnx.Db;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.KeyValuePair;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.commons.table.EditorUtils;
import it.tnx.invoicex.Magazzino;
import it.tnx.invoicex.data.Giacenza;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.EdgedBalloonStyle;
import net.java.balloontip.styles.IsometricBalloonStyle;
import net.java.balloontip.styles.ModernBalloonStyle;
import net.java.balloontip.utils.ToolTipUtils;
import org.apache.commons.lang.StringUtils;
import tnxbeans.HeaderListener2;
import tnxbeans.SortButtonRenderer2;
import tnxbeans.SortableTableModel;

/**
 *
 * @author mceccarelli
 */
public class JDialogLotti extends javax.swing.JDialog {

    public boolean ret = false;
    String tipo /* C carica, S scarica*/;
    Double qta_originale;
    Double qta;
    String articolo;
    String tabella;
    Integer id;
    boolean inserimento = false;
    boolean da_conversione_doc = false;
    boolean qta_diversa = false;

    KeyValuePair kvd1 = new KeyValuePair(Magazzino.Depositi.TUTTI_DETTAGLIO, "<tutti i depositi>");

    int num_righe = 30;

    /**
     * Creates new form JDialogLotti
     */
    public JDialogLotti(java.awt.Frame parent, boolean modal, boolean inserimento) {
        super(parent, modal);
        this.inserimento = inserimento;

        initComponents();

        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                if (!main.fileIni.getValueBoolean("pref", "balloon_lotti_visto", false)) {
                    BalloonTip tab_balloon = new BalloonTip(tab.getTableHeader(), "<html>Adesso puoi <b>ordinare</b> le righe facendo doppio click sull'intestazione della colonna</html>");
                    tab_balloon.setCloseButton(BalloonTip.getDefaultCloseButton(), false);
                    tab_balloon.addComponentListener(new ComponentAdapter() {
                        public void componentHidden(ComponentEvent e) {
                            main.fileIni.setValue("pref", "balloon_lotti_visto", true);
                        }
                    });
                    tab_balloon.setVisible(true);
                }
            }
        });

        tab.setModel(new SortableTableModel(new String[]{"Lotto", "Quantita", "Giacenza", "Ultima movimentazione"}, num_righe, null, null, null, true) {
            Class[] types = new Class[]{
                java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.util.Date.class
            };
            boolean[] canEdit = new boolean[]{
                true, true, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }

            public Class getColumnClassSql(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public Object getValueAt(int row, int col) {
                Object o = super.getValueAt(row, col); //To change body of generated methods, choose Tools | Templates.
                return o;
            }
        });
        if (tab.getColumnModel().getColumnCount() > 0) {
            tab.getColumnModel().getColumn(0).setPreferredWidth(200);
            tab.getColumnModel().getColumn(1).setPreferredWidth(50);
            tab.getColumnModel().getColumn(2).setPreferredWidth(50);
            tab.getColumnModel().getColumn(2).setPreferredWidth(50);
        }

        SortButtonRenderer2 renderer = new SortButtonRenderer2();
        TableColumnModel model = tab.getColumnModel();
        int n = model.getColumnCount();
        for (int i = 0; i < n; i++) {
            model.getColumn(i).setHeaderRenderer(renderer);
        }
        JTableHeader header = tab.getTableHeader();
        HeaderListener2 headerListener2 = new HeaderListener2(header, renderer);
        header.addMouseListener(headerListener2);

        tab.setSurrendersFocusOnKeystroke(true);
        tab.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        tab.getColumn("Quantita").setCellEditor(new EditorUtils.NumberEditor(new JTextField()));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labmsg = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tab = new MyTable();
        conferma = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        deposito = new javax.swing.JComboBox();
        giacenze_da = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Gestione Lotti");

        labmsg.setText("...");

        tab.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Lotto", "Quantita", "Giacenza"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tab.setRowHeight(20);
        jScrollPane1.setViewportView(tab);
        if (tab.getColumnModel().getColumnCount() > 0) {
            tab.getColumnModel().getColumn(0).setPreferredWidth(200);
            tab.getColumnModel().getColumn(1).setPreferredWidth(50);
            tab.getColumnModel().getColumn(2).setPreferredWidth(50);
        }

        conferma.setText("Conferma");
        conferma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confermaActionPerformed(evt);
            }
        });

        jButton2.setText("Annulla");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        deposito.setToolTipText("Da qui puoi scegliere da quale deposito controllare le giacenze ma non cambia il deposito da movimentare");
        deposito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                depositoActionPerformed(evt);
            }
        });

        giacenze_da.setText("Giacenze da");
        giacenze_da.setToolTipText("Da qui puoi scegliere da quale deposito controllare le giacenze ma non cambia il deposito da movimentare");

        jLabel1.setText(" ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(labmsg, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(jButton2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(conferma))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(giacenze_da)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, 0)
                                .add(jLabel1)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(labmsg)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(deposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(giacenze_da)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(conferma)
                    .add(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void confermaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confermaActionPerformed
        try {
            //controlli...
            //almeno un codice lotto
            boolean ok = true;

            int contalotti = 0;
            for (int i = 0; i < tab.getRowCount(); i++) {
                Object lotto = tab.getValueAt(i, 0);
                if (lotto != null && lotto.toString().trim().length() > 0) {
                    contalotti++;
                }
            }
            if (contalotti == 0) {
                if (!SwingUtils.showYesNoMessage(this, "Sembra che non hai inserito nessun lotto, confermi comunque ?")) {
                    ok = false;
                }
            }

            //controllo totale quantità originale
            //in conversione documento è fondamentale che scelgano la stessa quantità confermata prima
            if (da_conversione_doc) {
                double nuova_qta = 0;
                for (int i = 0; i < tab.getRowCount(); i++) {
                    nuova_qta += cu.d0(tab.getValueAt(i, 1));
                }
                if (qta_originale != nuova_qta) {
                    SwingUtils.showErrorMessage(this, "Devi scegliere un totale quantità pari a " + FormatUtils.formatNum0_5Dec(qta_originale) + "\nInvece hai inserito lotti per un totale di " + FormatUtils.formatNum0_5Dec(nuova_qta));
                    return;
                }
            }

            if (ok) {
                if (tabella.equals("movimenti_magazzino")) {
                    //riprendo movimento e lo splitto per i lotti
                    ArrayList<Map> list = DbUtils.getListMap(Db.getConn(), "select * from " + tabella + " where id = " + id);
                    System.out.println("id da rimuovere = " + list.get(0).get("id"));
                    String sql = "delete from " + tabella + " where id = " + list.get(0).get("id");
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                    for (int i = 0; i < tab.getRowCount(); i++) {
                        Object lotto = tab.getValueAt(i, 0);
                        if (tab.getValueAt(i, 1) != null && (Double) tab.getValueAt(i, 1) > 0) {
                            System.out.println("aggiungere lotto:" + lotto);
                            HashMap m = (HashMap) list.get(0);
                            m.put("lotto", tab.getValueAt(i, 0));
                            m.put("quantita", tab.getValueAt(i, 1));
                            m.remove("id");
                            sql = "insert into " + tabella + " set " + DbUtils.prepareSqlFromMap(m);
                            System.out.println("sql:" + sql);
                            DbUtils.tryExecQuery(Db.getConn(), sql);
                        }
                    }
                } else {
                    String sql = "delete from " + tabella + " where id_padre = " + id;
                    System.out.println("elimino i records precedenti da " + tabella + " sql:" + " esito:" + DbUtils.tryExecQuery(Db.getConn(), sql));
                    for (int i = 0; i < tab.getRowCount(); i++) {
                        Object lotto = tab.getValueAt(i, 0);
                        if (tab.getValueAt(i, 1) != null && (Double) tab.getValueAt(i, 1) > 0) {
                            System.out.println("aggiungere lotto:" + lotto);

                            HashMap r = new HashMap();
                            r.put("id_padre", id);
                            r.put("codice_articolo", articolo);
                            r.put("lotto", lotto);
                            r.put("qta", tab.getValueAt(i, 1));
                            sql = "insert into " + tabella + " set " + DbUtils.prepareSqlFromMap(r);
                            System.out.println("aggiungo i lotti:" + sql);
                            DbUtils.tryExecQuery(Db.getConn(), sql);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showExceptionMessage(this, e);
        }
        ret = true;
        dispose();
    }//GEN-LAST:event_confermaActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void depositoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_depositoActionPerformed
//        ArrayList list = null;
//        if (!deposito.isVisible() || deposito.getSelectedItem() == kvd1) {
//            list = main.magazzino.getGiacenza(false, articolo, null, null, true, false, null);
//        } else {
//            list = main.magazzino.getGiacenza(false, articolo, null, null, true, false, ((KeyValuePair) deposito.getSelectedItem()).getKey());
//        }
//
//        for (int i = 0; i < tab.getRowCount(); i++) {
//            String lotto = cu.s(tab.getValueAt(i, tab.getColumn("Lotto").getModelIndex()));
//            if (StringUtils.isBlank(lotto) && i >= 1) {
//                break;
//            }
//            double giac = getGiacenza(lotto, list);
//            tab.setValueAt(giac, i, tab.getColumnModel().getColumnIndex("Giacenza"));
//        }

        if (inserimento || qta_diversa) {
            caricaLotti();
        } else {
            caricaLottiInModifica();
        }

    }//GEN-LAST:event_depositoActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton conferma;
    private javax.swing.JComboBox deposito;
    private javax.swing.JLabel giacenze_da;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JLabel labmsg;
    private javax.swing.JTable tab;
    // End of variables declaration//GEN-END:variables

    private double getGiacenza(String lotto, List giacenze) {
        for (Object g : giacenze) {
            Giacenza m = (Giacenza) g;
            if (cu.s(m.getLotto()).equals(lotto)) {
                return m.getGiacenza();
            }
        }
        return 0d;
    }

    public void init(String tipo /* C carica, S scarica*/, Double qta, String articolo, String tabella, Integer id, String lotto_iniziale, Integer codice_deposito, boolean da_conversione_doc) {
        this.tipo = tipo;
        this.qta = qta;
        this.qta_originale = qta;
        this.articolo = articolo;
        this.tabella = tabella;
        this.id = id;
        this.da_conversione_doc = da_conversione_doc;

        try {
            if (!Magazzino.isMultiDeposito()) {
                giacenze_da.setVisible(false);
                deposito.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (tipo.equals("S")) {
            labmsg.setText("Indica i lotti e le quantita da scaricare");
        } else {
            labmsg.setText("Indica i lotti e le quantita da caricare");
        }
        labmsg.setText(labmsg.getText() + " dell'articolo " + articolo);

        //controllo se in modifica ma quantità diversa da quella precedente allora ripropongo da zero
        qta_diversa = false;
        try {
            String sql = null;
            if (!tabella.equalsIgnoreCase("movimenti_magazzino")) {
                sql = "select sum(qta) from " + tabella + " where id_padre = " + id;
                System.out.println("sql = " + sql);
                Double tot_qta_lotti = cu.d(DbUtils.getObject(Db.getConn(), sql));
                System.out.println("qta = " + qta + " qta_lotti = " + tot_qta_lotti);
                if (!cu.d0(qta).equals(tot_qta_lotti)) {
                    qta_diversa = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtils.initJComboFromDb(deposito, Db.getConn(), "select id, nome from depositi order by nome", "id", "nome", kvd1);
        SwingUtils.findJComboKV(deposito, codice_deposito);

//        if (tipo.equals("C")) {
//            if (lotto_iniziale != null) {
//                tab.setValueAt(lotto_iniziale, 0, 0);
//            }
//            tab.setValueAt(qta, 0, 1);
//            boolean success = tab.editCellAt(0, 0);
//            if (success) {
//                tab.changeSelection(0, 0, false, false);
//            }
//        } else {
        if (inserimento || qta_diversa) {
            caricaLotti();
        } else {
            caricaLottiInModifica();
        }

    }

    private void checkrows(int row) {
        if (tab.getRowCount() <= row) {
            ((DefaultTableModel) tab.getModel()).setRowCount(row + 1);
        }
    }

    public ArrayList<String> getLotti() {
        ArrayList lotti = new ArrayList();
        for (int row = 0; row < tab.getRowCount(); row++) {
            if (tab.getValueAt(row, 0) != null && CastUtils.toDouble0(tab.getValueAt(row, 1)) > 0) {
                lotti.add((String) tab.getValueAt(row, 0));
            }
        }
        return lotti;
    }

    public ArrayList<Double> getLottiQta() {
        ArrayList lotti = new ArrayList();
        for (int row = 0; row < tab.getRowCount(); row++) {
            if (tab.getValueAt(row, 0) != null && CastUtils.toDouble0(tab.getValueAt(row, 1)) > 0) {
                lotti.add(CastUtils.toDouble0(tab.getValueAt(row, 1)));
            }
        }
        return lotti;
    }

    private Giacenza trovaGiacenzaDeposito(ArrayList<Giacenza> giacenza_totale, ArrayList<Giacenza> giacenza_deposito, String lotto) {
        for (Giacenza g : giacenza_totale) {
            if (cu.s(g.getLotto()).equals(lotto)) {
                for (Giacenza g2 : giacenza_deposito) {
                    if (cu.s(g.getLotto()).equals(cu.s(g2.getLotto()))) {
                        return g2;
                    }
                }
            }
        }
        Giacenza gblank = new Giacenza();
        gblank.setCodice_articolo(giacenza_totale.get(0).getCodice_articolo());
        gblank.setLotto(lotto);
        gblank.setGiacenza(0);
        return gblank;
    }

    private void caricaLotti() {
//        ArrayList<Giacenza> giacenza_totale = null;
//        ArrayList<Giacenza> giacenza_deposito = null;
//        giacenza_totale = main.magazzino.getGiacenza(false, articolo, null, null, true, true, null);
//        if (!deposito.isVisible() || deposito.getSelectedItem() == kvd1) {
//            giacenza_deposito = giacenza_totale;
//        } else {
//            giacenza_deposito = main.magazzino.getGiacenza(false, articolo, null, null, true, true, ((KeyValuePair) deposito.getSelectedItem()).getKey());
//        }
//        //ordino giacenza totale
//
//        int row = 0;
//        if (tipo.equals("S")) {
//            for (Giacenza g : giacenza_totale) {
//                //trovare giacenza deposito
//                Giacenza gdep = trovaGiacenzaDeposito(giacenza_totale, giacenza_deposito, g.getLotto());
//                double qtan = qta;
//                if (gdep.getGiacenza() <= 0) {
//                    qtan = 0;
//                } else if (qtan > gdep.getGiacenza()) {
//                    qtan = gdep.getGiacenza();
//                }
//                qta -= qtan;
//                chekrows(row);
//                tab.setValueAt(g.getLotto(), row, 0);
//                tab.setValueAt(qtan, row, 1);
//                tab.setValueAt(gdep.getGiacenza(), row, 2);
//                row++;
//            }
//            if (qta > 0) {
//                //se presente trovo riga con lotto vuoto
//                if (row > 0) {
//                    for (int i = 0; i < row; i++) {
//                        if (StringUtils.isBlank(CastUtils.toString(tab.getValueAt(i, 0)))) {
//                            row = i;
//                            break;
//                        }
//                    }
//                }
//                chekrows(row);
//                tab.setValueAt("", row, 0);
//                tab.setValueAt(qta, row, 1);
//                tab.setValueAt(0, row, 2);
//            }
//        } else {
//            //in carico è inutile far vedere gli altri lotti
//            chekrows(row);
//            tab.setValueAt("", row, 0);
//            tab.setValueAt(qta, row, 1);
//        }

        //azzero
        ((DefaultTableModel) tab.getModel()).setRowCount(0);

        qta = qta_originale;

        if (tipo.equals("S")) {

            ArrayList<Giacenza> giacenza = null;
            try {
                if (!deposito.isVisible() || deposito.getSelectedItem() == kvd1) {
                    giacenza = main.magazzino.getGiacenza(false, articolo, null, null, true, false, null);
                } else {
                    giacenza = main.magazzino.getGiacenza(false, articolo, null, null, true, false, ((KeyValuePair) deposito.getSelectedItem()).getKey());
                }
            } catch (Exception e) {
                SwingUtils.showExceptionMessage(this, e);
                return;
            }

            //ordino giacenza totale
            Collections.sort(giacenza, new Comparator<Giacenza>() {
                public int compare(Giacenza g1, Giacenza g2) {
                    return Double.compare(cu.d0(g2.getGiacenza()), cu.d0(g1.getGiacenza()));
                }
            });

            //prendo ultima data movimentazione
            Map map_date = getDateMovimentazione(giacenza);

            int row = 0;

            for (Giacenza g : giacenza) {
                //trovare giacenza deposito
                double qtan = qta;
                if (g.getGiacenza() <= 0) {
                    qtan = 0;
                } else if (qtan > g.getGiacenza()) {
                    qtan = g.getGiacenza();
                }
                qta -= qtan;
                checkrows(row);
                tab.setValueAt(g.getLotto(), row, 0);
                tab.setValueAt(qtan, row, 1);
                tab.setValueAt(g.getGiacenza(), row, 2);
                try {
                    tab.setValueAt(((Map) map_date.get(g.getLotto())).get("ultima_data"), row, 3);
                } catch (Exception e) {
                }
                row++;
            }
            if (qta > 0) {
                //se ancora qta da scegliere aggiungo riga con lotto vuoto
                if (row > 0) {
                    for (int i = 0; i < row; i++) {
                        if (StringUtils.isBlank(CastUtils.toString(tab.getValueAt(i, 0)))) {
                            row = i;
                            break;
                        }
                    }
                }
                checkrows(row);
                tab.setValueAt("", row, 0);
                tab.setValueAt(qta, row, 1);
                tab.setValueAt(0, row, 2);
                try {
                    tab.setValueAt(((Map) map_date.get("")).get("ultima_data"), row, 3);
                } catch (Exception e) {
                }
            }
        } else {
            //in carico è inutile far vedere gli altri lotti
            int row = 0;
            checkrows(row);
            tab.setValueAt("", row, 0);
            tab.setValueAt(qta_originale, row, 1);
        }

        checkrows(num_righe);
    }

    private void caricaLottiInModifica() {

        //azzero
        ((DefaultTableModel) tab.getModel()).setRowCount(0);

        String sql = null;
        if (tabella.equalsIgnoreCase("movimenti_magazzino")) {
            sql = "select * from " + tabella + " where id = " + id;
        } else {
            sql = "select * from " + tabella + " where id_padre = " + id;
        }
        System.out.println("sql = " + sql);
        try {
            if (tipo.equals("S")) {
                ArrayList<Giacenza> giacenza_totale = null;
                ArrayList<Giacenza> giacenza_deposito = null;
                giacenza_totale = main.magazzino.getGiacenza(false, articolo, null, null, true, false, null);

                //prendo ultima data movimentazione
                Map map_date = getDateMovimentazione(giacenza_totale);

                if (!deposito.isVisible() || deposito.getSelectedItem() == kvd1) {
                    giacenza_deposito = giacenza_totale;
                } else {
                    giacenza_deposito = main.magazzino.getGiacenza(false, articolo, null, null, false, true, ((KeyValuePair) deposito.getSelectedItem()).getKey());
                }

                HashMap mlotti = DbUtils.getListMapMap(Db.getConn(), sql, "lotto");
                DebugUtils.dump(mlotti);
                Iterator ilotti = mlotti.keySet().iterator();
                int row = 0;
                if (mlotti.keySet().isEmpty()) {
                    //vuoto
                    checkrows(row);
                    tab.setValueAt("", row, 0);
                    tab.setValueAt(CastUtils.toDouble0(qta), row, 1);
                    try {
                        tab.setValueAt(((Map) map_date.get("")).get("ultima_data"), row, 3);
                    } catch (Exception e) {
                    }
                    for (Giacenza g : giacenza_totale) {
                        System.out.println("g.getLotto = " + g.getLotto());
                        if (g.getLotto() == null || g.getLotto().equals("")) {
                            Giacenza gdep = trovaGiacenzaDeposito(giacenza_totale, giacenza_deposito, g.getLotto());
                            tab.setValueAt(gdep.getGiacenza(), row, 2);

                            break;
                        }
                    }
                    row++;
                } else {
                    while (ilotti.hasNext()) {
                        String lotto = (String) ilotti.next();
                        HashMap rlotto = (HashMap) mlotti.get(lotto);
                        checkrows(row);
                        tab.setValueAt(lotto, row, 0);
                        if (tabella.equalsIgnoreCase("movimenti_magazzino")) {
                            tab.setValueAt(CastUtils.toDouble0(rlotto.get("quantita")), row, 1);
                        } else {
                            tab.setValueAt(CastUtils.toDouble0(rlotto.get("qta")), row, 1);
                        }
                        try {
                            tab.setValueAt(((Map) map_date.get(lotto)).get("ultima_data"), row, 3);
                        } catch (Exception e) {
                        }
                        for (Giacenza g : giacenza_totale) {
                            if (g.getLotto().equalsIgnoreCase(lotto)) {
                                Giacenza gdep = trovaGiacenzaDeposito(giacenza_totale, giacenza_deposito, g.getLotto());
                                tab.setValueAt(gdep.getGiacenza(), row, 2);
                                break;
                            }
                        }
                        row++;
                    }
                }

                for (Giacenza g : giacenza_totale) {
                    if (!mlotti.containsKey(g.getLotto())) {
                        if (!(StringUtils.isBlank(g.getLotto()) && mlotti.keySet().isEmpty())) {
                            checkrows(row);
                            tab.setValueAt(g.getLotto(), row, 0);
                            tab.setValueAt(0d, row, 1);
                            Giacenza gdep = trovaGiacenzaDeposito(giacenza_totale, giacenza_deposito, g.getLotto());
                            tab.setValueAt(gdep.getGiacenza(), row, 2);
                            try {
                                tab.setValueAt(((Map) map_date.get(g.getLotto())).get("ultima_data"), row, 3);
                            } catch (Exception e) {
                            }
                            row++;
                        }
                    }
                }
            } else {
                //in carico presento solo la riga già presente
                HashMap mlotti = DbUtils.getListMapMap(Db.getConn(), sql, "lotto");
                DebugUtils.dump(mlotti);
                Iterator ilotti = mlotti.keySet().iterator();
                int row = 0;
                if (mlotti.keySet().isEmpty()) {
                    //vuoto
                    checkrows(row);
                    tab.setValueAt("", row, 0);
                    tab.setValueAt(CastUtils.toDouble0(qta), row, 1);
                    row++;
                } else {
                    while (ilotti.hasNext()) {
                        String lotto = (String) ilotti.next();
                        HashMap rlotto = (HashMap) mlotti.get(lotto);
                        checkrows(row);
                        tab.setValueAt(lotto, row, 0);
                        if (tabella.equalsIgnoreCase("movimenti_magazzino")) {
                            tab.setValueAt(CastUtils.toDouble0(rlotto.get("quantita")), row, 1);
                        } else {
                            tab.setValueAt(CastUtils.toDouble0(rlotto.get("qta")), row, 1);
                        }
                        row++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        checkrows(num_righe);
    }

    private Map getDateMovimentazione(ArrayList<Giacenza> giacenza) {
        if (giacenza == null || giacenza.size() == 0) return null;
        String sql = "select lotto, max(data) as ultima_data from movimenti_magazzino where ";
        for (Giacenza g : giacenza) {
            sql += " (articolo = " + dbu.sql(g.getCodice_articolo()) + " and lotto = " + dbu.sql(g.getLotto()) + ") or ";
        }
        sql = StringUtils.removeEnd(sql, " or ");
        sql += " group by lotto";
        System.out.println("sql = " + sql);
        Map map_date = null;
        try {
            map_date = dbu.getListMapMap(Db.getConn(), sql, "lotto");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("map_date = " + map_date);
        return map_date;
    }
}

class MyTable extends JTable {
//  Place cell in edit mode when it 'gains focus'

    public void changeSelection(
            int row, int column, boolean toggle, boolean extend) {
        super.changeSelection(row, column, toggle, extend);

        if (editCellAt(row, column)) {
            getEditorComponent().requestFocusInWindow();
        }
    }

    //  Select the text when the cell starts editing
    //  a) text will be replaced when you start typing in a cell
    //  b) text will be selected when you use F2 to start editing
    //  c) text will be selected when double clicking to start editing
    public boolean editCellAt(int row, int column, EventObject e) {
        boolean result = super.editCellAt(row, column, e);
        final Component editor = getEditorComponent();

        if (editor != null && editor instanceof JTextComponent) {
            if (e == null) {
                ((JTextComponent) editor).selectAll();
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        ((JTextComponent) editor).selectAll();
                    }
                });
            }
        }

        return result;
    }

    //  Select the text when the cell starts editing
    //  a) text will be replaced when you start typing in a cell
    //  b) text will be selected when you use F2 to start editing
    //  c) caret is placed at end of text when double clicking to start editing
    public Component prepareEditor(
            TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);

        if (c instanceof JTextComponent) {
            System.out.println("prepare " + ((JTextField) c).getText());
            ((JTextField) c).selectAll();
        }

        return c;
    }
}
