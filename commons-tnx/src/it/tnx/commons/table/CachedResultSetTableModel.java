/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons.table;

import it.tnx.commons.DbUtils;
import it.tnx.commons.SwingUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingworker.SwingWorker;

/**
 *
 * @author mceccarelli
 */
public class CachedResultSetTableModel extends AbstractTableModel {

    javax.swing.table.DefaultTableModel mod = null;
    Connection conn;
    private String sql;
    ResultSet r;
    ResultSetMetaData m;
    int offsetcol = 1;
    public Exception ex = null;
    public boolean debug = false;
    public String debug_campo = null;
    private JTable table = null;
    TreeMap<Integer, Object[]> cache = new TreeMap<Integer, Object[]>();
    int cachesize = 50;
    boolean loading = false;
    String[] col_names;
    String[] col_classes;
    int index = 0;
    int rows = 0;
    int cols = 0;
    List<String> pks = new ArrayList();

    public CachedResultSetTableModel(String sql, Connection conn) {
        this(sql, conn, 1);
    }

    public CachedResultSetTableModel(String sql, Connection conn, int offsetcol) {
        this(sql, conn, offsetcol, null);
    }

    public CachedResultSetTableModel(String sql, Connection conn, JTable table) {
        this(sql, conn, 1, table);
    }

    public CachedResultSetTableModel(String sql, Connection conn, int offsetcol, JTable table) {
        this.sql = sql;
        this.conn = conn;
        this.offsetcol = offsetcol;

        try {
            String sql2 = StringUtils.replace(sql.toLowerCase(), "select ", "select SQL_CALC_FOUND_ROWS ");
            sql2 = sql2 + " limit " + (cachesize * 2);
            ResultSet r = conn.createStatement().executeQuery(sql2);
            ResultSetMetaData m = r.getMetaData();
            cols = m.getColumnCount();
            col_names = new String[cols];
            col_classes = new String[cols];
            for (int i = 0; i < cols; i++) {
                col_names[i] = m.getColumnName(i + 1);
                col_classes[i] = m.getColumnClassName(i + 1);
            }
            ResultSet r2 = conn.createStatement().executeQuery("SELECT FOUND_ROWS()");
            if (r2.next()) {
                rows = r2.getInt(1);
            }
            //pks
            DatabaseMetaData dm = conn.getMetaData();
            System.out.println("pks: " + m.getCatalogName(1) + "|" + m.getSchemaName(1) + "|" + m.getTableName(1));
            ResultSet rpks = dm.getPrimaryKeys(m.getCatalogName(1), m.getSchemaName(1), m.getTableName(1));
            while (rpks.next()) {
                String primaryKeyColumn = rpks.getString("COLUMN_NAME");
                System.out.println("Primary Key Column: " + primaryKeyColumn);
                pks.add(primaryKeyColumn);
            }

            prendi(r, 0);
            r2.getStatement().close();
            r2.close();
            r.getStatement().close();
            r.close();

//            r = DbUtils.tryOpenResultSetEditable(conn, sql);
//            m = r.getMetaData();
//            r.last();
//            rowcount = r.getRow();
//            r.beforeFirst();
        } catch (Exception ex) {
            this.ex = ex;
            ex.printStackTrace();
        }

        if (table != null) {
            this.table = table;
            final JPopupMenu pop = new JPopupMenu("Tabella");
            JMenuItem elimina = new JMenuItem("elimina");
            elimina.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("elimina:" + CachedResultSetTableModel.this.table.getSelectedRow());
                    deleteRow(CachedResultSetTableModel.this.table.getSelectedRow());
                }
            });
            pop.add(elimina);
            table.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        pop.show(CachedResultSetTableModel.this.table, e.getX(), e.getY());
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        pop.show(CachedResultSetTableModel.this.table, e.getX(), e.getY());
                    }
                }
            });
        }
    }

    synchronized private void prendi(ResultSet r, int i) throws SQLException {
        int li = i - cachesize;
        if (li < 0) {
            li = 0;
        }
        int ls = i + cachesize;
        if (ls > rows) {
            ls = rows;
        }
        System.out.println("aggiungo: da " + li + " a " + ls);
        for (int conta = 0; conta < ls - li; conta++) {
            r.next();
            Object[] row = new Object[cols];
            for (int icol = 0; icol < cols; icol++) {
                row[icol] = r.getObject(icol + 1);
            }
            cache.put(li + conta, row);
        }
    }

    public void refresh() {
//        try {
//            r = DbUtils.tryOpenResultSetEditable(conn, getSql());
//            m = r.getMetaData();
//            r.last();
//            rows = r.getRow();
//            r.beforeFirst();
//            fireTableDataChanged();
//        } catch (Exception ex) {
//            this.ex = ex;
//            ex.printStackTrace();
//        }


        try {
            try {
                DbUtils.tryExecQuery(conn, "select 1");
            } catch (Exception e) {
                setConn(getConnection());
            }

            String sql2 = StringUtils.replace(sql.toLowerCase(), "select ", "select SQL_CALC_FOUND_ROWS ");
            sql2 = sql2 + " limit " + (cachesize * 2);
            ResultSet r = conn.createStatement().executeQuery(sql2);
            ResultSetMetaData m = r.getMetaData();
            cols = m.getColumnCount();
            col_names = new String[cols];
            col_classes = new String[cols];
            for (int i = 0; i < cols; i++) {
                col_names[i] = m.getColumnName(i + 1);
                col_classes[i] = m.getColumnClassName(i + 1);
            }
            ResultSet r2 = conn.createStatement().executeQuery("SELECT FOUND_ROWS()");
            if (r2.next()) {
                rows = r2.getInt(1);
            }

            cache.clear();
            //prendi(r, 0);

            r2.getStatement().close();
            r2.close();
            r.getStatement().close();
            r.close();

            fireTableDataChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() {
        return null;
    };

    public int getRowCount() {
        return rows;
//        return rows + 1;
    }

    public int getColumnCount() {
        return cols;
    }

    public Object getValueAt(final int rowIndex, int columnIndex) {
//        try {
//            if (rowIndex < rows) {
//                r.absolute(rowIndex + 1);
//                return r.getObject(columnIndex + 1);
//            } else {
//                return null;
//            }
//        } catch (SQLException ex) {
//            System.err.println(getClass() +  ":getValueAt(" + rowIndex + "," + columnIndex + "):" + ex.getMessage());
////            ex.printStackTrace();
//        }
//        return null;

//        System.out.println("get rowIndex:" + rowIndex + " index:" + index + " cond:" + rowIndex + " <= " + (index - 500) + " || " + rowIndex + " >= " + (index + 500));
        if (rowIndex <= (index - cachesize) || rowIndex >= (index + cachesize) || cache == null || cache.size() == 0) {

            if (loading) {
                return null;
            }
            loading = true;
            SwingWorker w = new SwingWorker() {

                @Override
                protected Object doInBackground() throws Exception {
                    String sql2 = sql + " limit " + ((rowIndex < cachesize ? cachesize : rowIndex) - cachesize) + ", " + (cachesize * 2);
                    System.out.println("getValueAt: " + rowIndex + " index: " + index + " sql2 = " + sql2);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            SwingUtils.mouse_wait();
                        }
                    });
                    try {
                        ResultSet r = conn.createStatement().executeQuery(sql2);
                        cache.clear();
                        prendi(r, rowIndex);
                        index = rowIndex;
                        System.out.println("nuovo index:" + index);
                        r.getStatement().close();
                        r.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            SwingUtils.mouse_def();
                        }
                    });
                    return null;
                }

                @Override
                protected void done() {
                    int min = rowIndex - cachesize;
                    int max = rowIndex + cachesize;
//per InvoicexAdmin                    
//                    SwingUtilities.invokeLater(new Runnable() {
//                        fireTableRowsUpdated(min < 0 ? 0 : min, max > rows - 1 ? rows - 1 : max);
//                    });
                    fireTableRowsUpdated(min < 0 ? 0 : min, max > rows - 1 ? rows - 1 : max);
                    loading = false;
                }
            };
            w.execute();
            return null;
        } else {
            try {
                return cache.get(rowIndex)[columnIndex];
            } catch (Exception e0) {
                System.err.println(getClass() + " getValueAt " + e0.getMessage());
                System.out.println("get rowIndex:" + rowIndex + " index:" + index + " cond:" + rowIndex + " <= " + (index - cachesize) + " || " + rowIndex + " >= " + (index + cachesize));
                try {
                    System.out.println("cache:" + cache);
                } catch (Exception e) {
                }
                try {
                    System.out.println("cache.get(rowIndex):" + cache.get(rowIndex));
                } catch (Exception e) {
                }
                try {
                    System.out.println("cache.get(rowIndex)[columnIndex]:" + cache.get(rowIndex)[columnIndex]);
                } catch (Exception e) {
                }
                return "?";
            }
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        try {
            return Class.forName(col_classes[columnIndex]);
        } catch (ClassNotFoundException ex) {
            return Object.class;
        } catch (Exception ex) {
            return Object.class;
        }
    }

    @Override
    public String getColumnName(int column) {
        try {
            return col_names[column];
        } catch (Exception e) {
            return e.getMessage();
        }
    }

//    public Object getFromResultSet(String column, int rowIndex) {
//        try {
//            r.absolute(rowIndex + 1);
//            return r.getObject(column);
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//            return null;
//        }
//    }
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        System.out.println("setValue:" + aValue + " : " + rowIndex + " " + columnIndex);
        if (rowIndex == rows) {
            //new
            try {
                r.moveToInsertRow();
                r.updateObject(columnIndex + 1, aValue);
                r.insertRow();
                rows++;
                fireTableRowsInserted(rows, rows);
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtils.showErrorMessage(table, "Errore nel salvataggio\n" + e.getMessage());
            }
        } else {
            //update
            try {
                //apro resultset su pk
//                r.absolute(rowIndex + 1);

                String sql2 = StringUtils.substringBefore(sql.toLowerCase(), " where");
                sql2 = StringUtils.substringBefore(sql2.toLowerCase(), " order by");
                int i;
                for (i = 0; i < col_names.length; i++) {
                    if (col_names[i].equalsIgnoreCase(pks.get(0))) break;
                }
                sql2 = sql2 + " where " + pks.get(0) + " = '" + getValueAt(rowIndex, i) + "'";
                System.out.println("sql2:" + sql2);
                r = DbUtils.tryOpenResultSetEditable(conn, sql2);
                if (r.next()) {
                    r.updateObject(columnIndex + 1, aValue);
                    r.updateRow();
                    if (debug) {
                        SwingUtils.showFlashMessage2("setValue row:" + (rowIndex + 1) + " col:" + (columnIndex + 1) + " value:" + aValue + " (campo debug:" + r.getString(debug_campo) + ")", 10);
                    }
                    //metto in cache
                    Object[] row = cache.get(rowIndex);
                    row[columnIndex] = aValue;
                    cache.put(rowIndex, row);
                } else {
                    if (debug) {
                        SwingUtils.showFlashMessage2("Non trovato il record !!!", 10);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (debug) {
                    SwingUtils.showFlashMessage2(e.getLocalizedMessage(), 10);
                }
                SwingUtils.showErrorMessage(table, "Errore nel salvataggio\n" + e.getMessage());
            }
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void deleteRow(int rowindex) {
        try {
            r.absolute(rowindex + 1);
            r.deleteRow();
            rows--;
            fireTableRowsDeleted(rowindex + 1, rowindex + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the sql
     */
    public String getSql() {
        return sql;
    }

    /**
     * @param sql the sql to set
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
    
}
