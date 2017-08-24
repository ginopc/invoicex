/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui.utils;

import gestioneFatture.main;
import it.tnx.Db;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.MicroBench;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.table.TableUtils;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingworker.SwingWorker;

/**
 *
 * @author mceccarelli
 */
public class JPanelStandardList extends javax.swing.JPanel {
    Object ritorno_id = null;
    String ritorno_nome = null;
    boolean selezionato = false;
    String campo_id = null;
    String campo_nome = null;
    String sql = "";
    String sql_filtrata = "";
    String nome_tab = null;
    String[] campi_filtrabili = null;
    Map<String,Integer> campi_filtrabili_maptipi = new HashMap();
    Map<String,String> campi_filtrabili_maptipis = new HashMap();

    /**
     * Creates new form JPanelStandardList
     */
    public JPanelStandardList() {
        initComponents();
    }

    public void init(final String sql, String campo_id, String campo_nome, String nome_tab) {
        init(sql, campo_id, campo_nome, nome_tab, null);
    }
    
    public void init(final String sql, String campo_id, String campo_nome, String nome_tab, String[] campi_filtrabili) {
        SwingUtils.mouse_wait(this);
        this.campo_id = campo_id;
        this.campo_nome = campo_nome;
        this.sql = sql;
        this.nome_tab = nome_tab;
        this.campi_filtrabili = campi_filtrabili;
        SwingWorker w = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                MicroBench mb = new MicroBench(true);
                TableUtils.loadData(Db.getConn(), sql, tab);
                mb.out("JPanelStandardList tempo init sql: " + sql);
                
                //elenco i campi filtrabili
                String sql = JPanelStandardList.this.sql + " limit 0";
                ResultSet r = null;
                ResultSetMetaData m = null;
                try {
                    r = DbUtils.tryOpenResultSet(Db.getConn(), sql);
                    m = r.getMetaData();
                    JPanelStandardList t = JPanelStandardList.this;
                    t.campi_filtrabili_maptipi = new HashMap();
                    t.campi_filtrabili_maptipis = new HashMap();
                    if (t.campi_filtrabili == null) {
                        t.campi_filtrabili = new String[m.getColumnCount()];
                        for (int i = 0; i < m.getColumnCount(); i++) {
                            String campo = m.getColumnName(i+1);
                            t.campi_filtrabili[i] = campo;
                        }                        
                    }
                    for (String campo : t.campi_filtrabili) {
                        Integer index = DbUtils.getColumnIndex(m, campo);
                        if (index != null) {
                            t.campi_filtrabili_maptipi.put(campo, m.getColumnType(index));
                            t.campi_filtrabili_maptipis.put(campo, m.getColumnTypeName(index));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        r.close();
                    } catch (Exception e) {
                    }
                }
                
                return null;
            }

            @Override
            protected void process(List chunks) {
                super.process(chunks);
            }

            @Override
            protected void done() {
                super.done();
                //carico larghezza colonne
                try {
                    for (int i = 0; i < tab.getColumnCount(); i++) {
                        String col_name = CastUtils.toString(tab.getColumnModel().getColumn(i).getIdentifier());
                        String k = "colwidth_JPanelStandardList_tab_" + JPanelStandardList.this.nome_tab + "_" + col_name;
                        int w = main.prefs.getInt(k, -1);
                        if (w >= 0) {
                            System.out.println("JPanelStandardList carico col width k:" + k + " w:" + w);
                            tab.getColumnModel().getColumn(i).setWidth(w);
                            tab.getColumnModel().getColumn(i).setPreferredWidth(w);
//                            tab.getColumnModel().getColumn(i).setMinWidth(w);
//                            tab.getColumnModel().getColumn(i).setMaxWidth(w);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                SwingUtils.mouse_def(JPanelStandardList.this);
            }
        };
        w.execute();

        getTopLevelAncestor().addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                //salvo larghezza colonne
                try {
                    for (int i = 0; i < tab.getColumnCount(); i++) {
                        String col_name = CastUtils.toString(tab.getColumnModel().getColumn(i).getIdentifier());
                        int w = tab.getColumnModel().getColumn(i).getWidth();
                        String k = "colwidth_JPanelStandardList_tab_" + JPanelStandardList.this.nome_tab + "_" + col_name;
                        System.out.println("JPanelStandardList salvo col width k:" + k + " w:" + w);
                        main.prefs.putInt(k, w);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tab = new NonEditableJTable();
        jLabel1 = new javax.swing.JLabel();
        filtro = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        butFiltra = new javax.swing.JButton();

        tab.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tab);

        jLabel1.setText("Filtra per");

        filtro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filtroActionPerformed(evt);
            }
        });

        jLabel2.setFont(jLabel2.getFont().deriveFont((jLabel2.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel2.setText("doppio click sulla riga per selezionare");

        butFiltra.setText("Filtra");
        butFiltra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butFiltraActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filtro)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(butFiltra))
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(filtro, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(butFiltra))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void butFiltraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butFiltraActionPerformed
        String sql = this.sql;
        sql = StringUtils.substringBefore(sql, this.nome_tab);
        sql += " " + this.nome_tab;
        String where = "";
        String testo = filtro.getText();
        for (String campo : campi_filtrabili) {
            where += (where.length() == 0 ? " where " : " or ") + campo + " like '%" + DbUtils.aa(testo) +  "%' ";
        }
        sql += where;
        String order = StringUtils.substringAfterLast(this.sql, "order by ");
        if (StringUtils.isNotBlank(order)) {
            sql += " order by " + order;
        }
        System.out.println("sql butFiltraActionPerformed = " + sql);
        try {
            ((DefaultTableModel)tab.getModel()).setRowCount(0);
            TableUtils.loadData(Db.getConn(), sql, (DefaultTableModel)tab.getModel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }//GEN-LAST:event_butFiltraActionPerformed

    private void filtroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filtroActionPerformed
        butFiltraActionPerformed(null);
    }//GEN-LAST:event_filtroActionPerformed

    private void tabMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabMouseClicked
        if (evt.getClickCount() >= 2) {
            selezionato = true;
            int col = tab.getColumnModel().getColumnIndex(this.campo_id);
            ritorno_id = tab.getValueAt(tab.getSelectedRow(), col);
            col = tab.getColumnModel().getColumnIndex(this.campo_nome);
            ritorno_nome = CastUtils.toString(tab.getValueAt(tab.getSelectedRow(), col));
            try {
                ((Window)getTopLevelAncestor()).dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_tabMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton butFiltra;
    private javax.swing.JTextField filtro;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JTable tab;
    // End of variables declaration//GEN-END:variables

    public static class NonEditableJTable extends JTable {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
