/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.dbeans.ResultSet;

import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.Overlayable;
import com.jidesoft.swing.OverlayableUtils;
import com.jidesoft.swing.StyledLabelBuilder;
import it.tnx.Db;
import it.tnx.DbI;
import it.tnx.commons.MicroBench;
import it.tnx.commons.dbu;
import it.tnx.commons.swing.DelayedExecutor;
import java.awt.BorderLayout;
import java.awt.Container;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXBusyLabel;

/**
 *
 * @author mceccarelli
 */
public class LazyResultSetModel extends DefaultTableModel {

    public String sql;
    DbI db;
    final TreeMap<Integer, Object[]> cache = new TreeMap<Integer, Object[]>();
    String[] col_names;
    String[] col_classes;
    int index = 0;
    int rows = 0;
    int cols = 0;

    static int buffersize = 2000;

    JTable table = null;

    public LazyResultSetModel(String sql, DbI db) throws SQLException {
        this(sql, db, null, null);
    }

    public LazyResultSetModel(String sql, DbI db, JTable table, String[] colonne) throws SQLException {
        this.sql = sql;
        this.db = db;
        this.table = table;
        
        if (colonne != null) {
            super.setColumnIdentifiers(colonne);
        }

        if (table != null) {
            try {
                if (!(table.getParent().getParent().getParent() instanceof Overlayable)) {
                    JScrollPane scroll = (JScrollPane) table.getParent().getParent();
                    JPanel panel = (JPanel) scroll.getParent();
                    JXBusyLabel jxbusy = new JXBusyLabel();
                    jxbusy.setText("...caricamento...");
                    jxbusy.setBusy(true);
                    DefaultOverlayable over = new DefaultOverlayable(new JScrollPane(table));
                    panel.add(over, BorderLayout.CENTER);
                    panel.revalidate();
                    over.addOverlayComponent(jxbusy);
                    over.setOverlayVisible(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MicroBench mb = new MicroBench();
        mb.start();

//        String sql2 = StringUtils.replaceOnce(sql.toLowerCase(), "select ", "select SQL_CALC_FOUND_ROWS ");
//il lowercase non faceva trovare le dimensioni delle colonne nel resize columns perc
        String sql2 = sql.replaceFirst("(?i)select ", "select SQL_CALC_FOUND_ROWS ");
        
        sql2 = sql2 + " limit " + (buffersize * 2);
        System.out.println("LazyResultSetModel sql2 = " + sql2);
        Statement stat = db.getDbConn().createStatement();
        ResultSet r = stat.executeQuery(sql2);
        ResultSetMetaData m = r.getMetaData();
        cols = m.getColumnCount();
        col_names = new String[cols];
        col_classes = new String[cols];
        for (int i = 0; i < cols; i++) {
            //ripreso da apertura griglia senza lazy altrimenti non trovava le larghezze delle colonne quando la colonna si dava un alias
//            col_names[i] = m.getColumnName(i + 1);
            col_names[i] = m.getColumnLabel(i + 1);
            col_classes[i] = m.getColumnClassName(i + 1);
        }

        System.out.println("aggiungo: da open");
        int conta = 0;
        while (r.next()) {
            Object[] row = new Object[cols];
            for (int icol = 0; icol < cols; icol++) {
                row[icol] = r.getObject(icol + 1);
            }
            cache.put(conta, row);
            conta++;
        }
        r.close();

        ResultSet r2 = stat.executeQuery("SELECT FOUND_ROWS()");
        if (r2.next()) {
            rows = r2.getInt(1);
            System.out.println("LazyResultSetModel rows = " + rows);
        }

        System.out.println(mb.getDiff("open 1"));

        r2.close();
        stat.close();

        System.out.println(mb.getDiff("open 2"));
    }

    public int getRowCount() {
        return rows;
    }

    public int getColumnCount() {
        return cols;
    }

    class PrendiSwingWorker extends SwingWorker {

        String sql = null;
        int row;

        private PrendiSwingWorker(String sql, int row) {
            super();
            this.sql = sql;
            this.row = row;
            System.out.println("new PrendiSwingWorker row = " + row);
            setIsLoading(true);
        }

        @Override
        protected Object doInBackground() throws Exception {
            String sql2 = sql + " limit " + ((row < buffersize ? buffersize : row) - buffersize) + ", " + (buffersize * 2);
            try {
                if (isCancelled()) {
                    return null;
                }
                ResultSet r = Db.openResultSet(Db.getConn(), sql2);
                if (isCancelled()) {
                    return null;
                }
                cache.clear();
                prendi(r, row, this);
                index = row;
                dbu.close(r);
            } catch (Exception e) {
                return null;
            }
            return null;
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                fireTableRowsUpdated(row - buffersize, row + buffersize);
            }
            setIsLoading(false);
        }

    }
    PrendiSwingWorker prendi = null;

    int rowIndex;
    private boolean loading = false;

    DelayedExecutor prendidelayed = new DelayedExecutor(new Runnable() {
        public void run() {
            prendi.cancel(true);
            prendi = new PrendiSwingWorker(sql, rowIndex);
            prendi.execute();
        }
    }, 150) {
        @Override
        public void update() {
            super.update(); //To change body of generated methods, choose Tools | Templates.
        }

    };

    private void setIsLoading(boolean val) {
        System.out.println(this + " setIsLoading " + val);
        if (table != null && loading == false && val == true) {
            OverlayableUtils.getOverlayable(table).setOverlayVisible(true);
        } else if (table != null && loading == true && val == false) {
            OverlayableUtils.getOverlayable(table).setOverlayVisible(false);
        }
        loading = val;

    }

    public boolean isLoading() {
        return loading;
    }

    synchronized public Object getValueAt(int rowIndex, int columnIndex) {
        this.rowIndex = rowIndex;
        if (rowIndex <= (index - buffersize) || rowIndex >= (index + buffersize)) {

            if (prendi == null) {
                prendi = new PrendiSwingWorker(sql, rowIndex);
                prendi.execute();
            } else if (rowIndex <= (prendi.row - buffersize) || rowIndex >= (prendi.row + buffersize)) {
                prendidelayed.update();
            }

            return null;
        }
        try {
            return cache.get(rowIndex)[columnIndex];
        } catch (Exception e0) {
            e0.printStackTrace();
            System.out.println("get rowIndex:" + rowIndex + " index:" + index + " cond:" + rowIndex + " <= " + (index - buffersize) + " || " + rowIndex + " >= " + (index + buffersize));
            try {
                System.out.println("rows:" + rows);
            } catch (Exception e) {
            }
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

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        try {
            return Class.forName(col_classes[columnIndex]);
        } catch (ClassNotFoundException ex) {
            return Object.class;
        }
    }

    @Override
    public String getColumnName(int column) {
        return col_names[column];
    }

    private void prendi(ResultSet r, int i, PrendiSwingWorker worker) throws SQLException {
        int li = i - buffersize;
        if (li < 0) {
            li = 0;
        }
        int ls = i + buffersize;
        if (ls > rows) {
            ls = rows;
        }
        System.out.println("aggiungo: da " + li + " a " + ls);
        for (int conta = 0; conta < ls - li; conta++) {
            if (worker.isCancelled()) {
                return;
            }
            r.next();
            Object[] row = new Object[cols];
            for (int icol = 0; icol < cols; icol++) {
                row[icol] = r.getObject(icol + 1);
            }
            cache.put(li + conta, row);
        }
    }

    public void removeRow(int row) {
        rows--;
        cache.remove(row);
        fireTableRowsDeleted(row, row);
    }

    @Override
    public void addRow(Object[] rowData) {
        rows++;
        Integer max = Collections.max(cache.keySet());
        cache.put(max + 1, rowData);
        fireTableRowsInserted(max + 1, max + 1);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

//    @Override
//    public void insertRow(int row, Object[] rowData) {
//        rows++;
//        cache.put(row, rowData)
//        fireTableRowsInserted(row, row);
//    }
}
