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
package it.tnx.dbeans.ResultSet;

import it.tnx.commons.DbUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author marco
 */
/*
 This class caches the result set data; it can be used
 if scrolling cursors are not supported
 */
public class CachingResultSetTableModel extends ResultSetTableModel {

    public Map<String, Integer> colonne = new HashMap();

    public CachingResultSetTableModel(ResultSet aResultSet) {
        super(aResultSet);
        try {
            cache = new ArrayList();
            int cols = aResultSet.getMetaData().getColumnCount();
            for (int i = 0; i < cols; i++) {
                colonne.put(aResultSet.getMetaData().getColumnName(i + 1), i);
            }
            ResultSet rs = getResultSet();
            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int j = 0; j < row.length; j++) {
                    row[j] = rs.getObject(j + 1);
                }
                cache.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error " + e);
        } finally {
            try {
                DbUtils.close(aResultSet);
            } catch (Exception e) {
            }
        }
    }

    public CachingResultSetTableModel(ArrayList rows) {
        super(null);
        cache = rows;
    }

    public Object getValueAt(int r, int c) {
        if (r < cache.size()) {
            return ((Object[]) cache.get(r))[c];
        } else {
            return null;
        }
    }

    public void setValueAt(Object value, int r, int c) {
        try {
            Object[] row = (Object[]) cache.get(r);
            row[c] = value;
            cache.set(r, row);
            super.setValueAt(value, r, c);
        } catch (Exception e) {
            e.printStackTrace();
            super.setValueAt(null, r, c);
        }
    }

    public int getRowCount() {
        return cache.size();
    }

    public void addRow() {
        Object[] row = new Object[colonne.size()];
        cache.add(row);
        fireTableRowsInserted(cache.size(), cache.size());
    }
    
    public void deleteRow(int id_row) {
        cache.remove(id_row);
        fireTableRowsDeleted(id_row, id_row);
    }

    public ArrayList cache;
}
