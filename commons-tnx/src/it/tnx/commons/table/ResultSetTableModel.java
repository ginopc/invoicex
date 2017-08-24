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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;


/**
 *
 * @author mceccarelli
 */
public class ResultSetTableModel extends AbstractTableModel {
    javax.swing.table.DefaultTableModel mod = null;
    Connection conn;
    private String sql;
    ResultSet r;
    ResultSetMetaData m;
    int rowcount = 0;
    int offsetcol = 1;
    public Exception ex = null;

    public boolean debug = false;
    public String debug_campo = null;

    private JTable table = null;

    public ResultSetTableModel(String sql, Connection conn) {
        this(sql, conn, 1);
    }

    public ResultSetTableModel(String sql, Connection conn, int offsetcol) {
        this(sql, conn, offsetcol, null);
    }

    public ResultSetTableModel(String sql, Connection conn, JTable table) {
        this(sql, conn, 1, table);
    }

    public ResultSetTableModel(String sql, Connection conn, int offsetcol, JTable table) {
        this.sql = sql;
        this.conn = conn;
        this.offsetcol = offsetcol;
        try {
            r = DbUtils.tryOpenResultSetEditable(conn, sql);
            m = r.getMetaData();
            r.last();
            rowcount = r.getRow();
            r.beforeFirst();
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
                    System.out.println("elimina:" + ResultSetTableModel.this.table.getSelectedRow());
                    deleteRow(ResultSetTableModel.this.table.getSelectedRow());
                }

            });
            pop.add(elimina);
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) pop.show(ResultSetTableModel.this.table, e.getX(), e.getY());
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) pop.show(ResultSetTableModel.this.table, e.getX(), e.getY());
                }
            });
        }
    }

    public void refresh() {
        try {
            r = DbUtils.tryOpenResultSetEditable(conn, getSql());
            m = r.getMetaData();
            r.last();
            rowcount = r.getRow();
            r.beforeFirst();
            fireTableDataChanged();
        } catch (Exception ex) {
            this.ex = ex;
            ex.printStackTrace();
        }
    }

    public int getRowCount() {
        return rowcount;
//        return rowcount + 1;
    }

    public int getColumnCount() {
        try {
            return m.getColumnCount();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            if (rowIndex < rowcount) {
                r.absolute(rowIndex + 1);
                return r.getObject(columnIndex + 1);
            } else {
                return null;
            }
        } catch (SQLException ex) {
            System.err.println(getClass() +  ":getValueAt(" + rowIndex + "," + columnIndex + "):" + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        try {
//            System.out.println("offsetcol:" + offsetcol);
            return m.getColumnName(column + offsetcol);
        } catch (SQLException ex) {
//            System.out.println("column:" + column);
            ex.printStackTrace();
            return "";
        }
    }

    public Object getFromResultSet(String column, int rowIndex) {
        try {
            r.absolute(rowIndex + 1);
            return r.getObject(column);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        System.out.println("setValue:" + aValue + " : " + rowIndex + " " + columnIndex);
        if (rowIndex == rowcount) {
            //new
            try {
                r.moveToInsertRow();
                r.updateObject(columnIndex + 1, aValue);
                r.insertRow();
                rowcount++;
                fireTableRowsInserted(rowcount, rowcount);
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtils.showErrorMessage(table, "Errore nel salvataggio\n" + e.getMessage());
            }
        } else {
            //update
            try {
                r.absolute(rowIndex + 1);
                r.updateObject(columnIndex + 1, aValue);
                r.updateRow();
                if (debug) SwingUtils.showFlashMessage2("setValue row:" + (rowIndex + 1) + " col:" + (columnIndex + 1) + " value:" + aValue + " (campo debug:" + r.getString(debug_campo) + ")", 10);
            } catch (Exception e) {
                e.printStackTrace();
                if (debug) SwingUtils.showFlashMessage2(e.getLocalizedMessage(), 10);
                SwingUtils.showErrorMessage(table, "Errore nel salvataggio\n" + e.getMessage());
            }
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void deleteRow(int rowindex) {
        try {
            r.absolute(rowindex+1);
            r.deleteRow();
            rowcount--;
            fireTableRowsDeleted(rowindex+1, rowindex+1);
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



}