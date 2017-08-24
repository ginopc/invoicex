/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons.table;

import it.tnx.commons.BidiMap;
import it.tnx.commons.cu;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Marco
 */
public class ListMapTableModel extends AbstractTableModel {
    List<Map> list = null;
    List columns = null;
    Map editableColumns = null;

    public ListMapTableModel(List<Map> list, List columns) {
        this.list = list;
        this.columns = columns;
    }
    
    public List<Map> getList() {
        return list;
    }
    
    public int getRowCount() {
        if (list == null) return 0;
        return list.size();
    }

    @Override
    public String getColumnName(int column) {
        if (columns.get(column) instanceof CustomColumn) {
            return ((CustomColumn)columns.get(column)).getName();
        } else {
            return cu.s(columns.get(column));
        }
    }
    
    public int getColumnCount() {
        if (columns != null) return columns.size();
        if (list != null && list.size() > 0 && list.get(0) != null) list.get(0).size();
        return 0;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (editableColumns == null) {
            return true;
        } else {
            if (editableColumns.containsKey( getColumnName(columnIndex) )) return true;
        }
        return false;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (list == null) return null;
        try {
            return list.get(rowIndex).get(columns.get(columnIndex));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        try {
            list.get(rowIndex).put(columns.get(columnIndex), aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        } catch (Exception e) {
        }        
    }
    
    public void addRow(Map row) {
        list.add(row);
        fireTableRowsInserted(list.size(), list.size());
    }

    public void removeRow(int row) {
        list.remove(row);
        fireTableRowsDeleted(row, row);
    }

}
