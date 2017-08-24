/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.tnx.commons.table;

import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.KeyValuePair;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author mceccarelli
 */
public class TableUtils {
    public static void addAutoNewRow(final JTable table) throws Exception {
        if (!(table.getModel() instanceof DefaultTableModel)) {
            throw new Exception("Table model not compatible with addAutoNewRow");
        }
        table.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getLastRow() == (table.getRowCount()-1)) {
                    if (table.getModel() instanceof DefaultTableModel) {
                        ((DefaultTableModel)table.getModel()).addRow(new Object[table.getColumnCount()]);
                    }
                }
            }
        });
    }

    public static void loadData(Connection conn, String sql, DefaultTableModel model) throws Exception {
        ResultSet r = null;
        try {
            r = DbUtils.tryOpenResultSet(conn, sql);
            loadData(r, model);
        } finally {
            if (r != null) {
                r.getStatement().close();
                r.close();
            }
        }
    }
    
    public static void loadData(ResultSet r, DefaultTableModel model) throws Exception {
        while (r.next()) {
            model.addRow(DbUtils.getRow(r));
        }
    }    

    public static void loadData(Connection conn, String sql, JTable tab) throws Exception {
        ResultSet r = null;
        try {
            r = DbUtils.tryOpenResultSet(conn, sql);
            ResultSetMetaData m = r.getMetaData();
            Object[] cols = new Object[m.getColumnCount()];
            for (int i = 0; i < m.getColumnCount(); i++) {
                cols[i] = m.getColumnName(i+1);
            }
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            loadData(r, model);
            tab.setModel(model);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (r != null) {
                r.getStatement().close();
                r.close();
            }
        }
    }    

    public static Object NUMERO_RIGA = new Object();
    public static void saveData(Connection conn, JTable tab, String nome_tab, Map<String,Object> campi_default, Map<String,String> nomi_campi) throws Exception {
        //init tipo campi
        String sql = "select * from " + nome_tab + " limit 0";
        ResultSet r = null;
        ResultSetMetaData m = null;

        r = DbUtils.tryOpenResultSet(conn, sql);
        m = r.getMetaData();
        String[] campi = null;
        Map<String,Integer> campi_maptipi = new HashMap();
        Map<String,String> campi_maptipis = new HashMap();
        
        campi_maptipi = new HashMap();
        campi_maptipis = new HashMap();
        if (campi == null) {
            campi = new String[m.getColumnCount()];
            for (int i = 0; i < m.getColumnCount(); i++) {
                String campo = m.getColumnName(i+1);
                campi[i] = campo;
            }                        
        }
        for (String campo : campi) {
            Integer index = DbUtils.getColumnIndex(m, campo);
            if (index != null) {
                campi_maptipi.put(campo, m.getColumnType(index));
                campi_maptipis.put(campo, m.getColumnTypeName(index));
            }
        }
                        
        int rows = tab.getRowCount();        
        int cols = tab.getColumnCount();
        for (int i = 0; i < rows; i++) {
            System.out.println("tableutils savedata row = " + i);
            sql = "insert into " + nome_tab + " set ";
            for (int c = 0; c < cols; c++) {
                String campotab = CastUtils.toString(tab.getColumnModel().getColumn(c).getIdentifier());
                if (nomi_campi.containsKey(campotab)) {
                    campotab = nomi_campi.get(campotab);
                }
                String tipocampo = campi_maptipis.get(campotab);
                Object value = null;
                if (campi_default != null && campi_default.containsKey(campotab)) {
                    value = campi_default.get(campotab);
                    if (value == NUMERO_RIGA) {
                        value = i+1;
                    }
                } else {
                    value = tab.getValueAt(i, c);
                }
                String tipocampoobj = "";
                if (value != null) {
                    tipocampoobj = value.getClass().getName();
                }
                System.out.println(" col:" + c + " campo:" + campotab + " tipocampo:" + tipocampo + " value:" + value + " tipocampoobject:" + tipocampoobj);
                sql += campotab + " = "  + DbUtils.sql(value);
                if (c < cols-1) {
                    sql += ", ";
                }
            }
            System.out.println("tableutils savedata sql = " + sql);
            DbUtils.tryExecQuery(conn, sql);
        }
    }

    public static Map getMap(JTable tab, int row) {
        Map m = new HashMap();
        for (int icol = 0; icol < tab.getColumnCount(); icol++) {
            m.put(tab.getColumnName(icol), tab.getValueAt(row, icol));
        }
        return m;
    }
    
    public static void hideColumn(JTable tab, String columnTitle) {
        tab.getColumn(columnTitle).setMinWidth(0);
        tab.getColumn(columnTitle).setMaxWidth(0);
        tab.getColumn(columnTitle).setPreferredWidth(0);
        tab.getColumn(columnTitle).setWidth(0);
    }

    public static void hideColumnExcept(JTable tab, String... columns) {
        List<String> list = Arrays.asList(columns);
        for (int icol = 0; icol < tab.getColumnCount(); icol++) {
            String colTitle = tab.getColumnName(icol);
            if (!list.contains(colTitle)) {
                hideColumn(tab, colTitle);
            }
        }
    }
    
}
