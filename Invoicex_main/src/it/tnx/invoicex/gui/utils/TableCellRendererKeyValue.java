/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui.utils;

import it.tnx.Db;
import it.tnx.commons.DbUtils;
import it.tnx.commons.cu;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author mceccarelli
 */
public class TableCellRendererKeyValue implements TableCellRenderer {

    JPanelCellKeyValue cell;
    JTable table;

    public TableCellRendererKeyValue(JTable table, JPanelCellKeyValue panel) {
        this.table = table;
        cell = panel;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            cell.setForeground(table.getSelectionForeground());
            cell.desc.setForeground(table.getSelectionForeground());
            cell.id.setForeground(table.getSelectionForeground());
            cell.setBackground(table.getSelectionBackground());
            cell.desc.setBackground(table.getSelectionBackground());
            cell.id.setBackground(table.getSelectionBackground());
        } else {
            Color background = table.getBackground();
            cell.setForeground(table.getForeground());
//            conto.desc.setForeground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveForeground"));
            cell.desc.setForeground(table.getForeground());
            cell.id.setForeground(table.getForeground());
            cell.setBackground(background);
            cell.desc.setBackground(background);
            cell.id.setBackground(background);
        }
        if (hasFocus) {
            Border border = null;
            if (isSelected) {
                border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = UIManager.getBorder("Table.focusCellHighlightBorder");
            }
            cell.setBorder(border);
        } else {
            cell.setBorder(null);
        }
        
        cell.id.setText(cu.toString(value));
        try {
            cell.desc.setText(cu.toString(DbUtils.getObject(Db.getConn(), "select " + cell.campo_descrizione + " from " + cell.tabella + " where " + cell.campo_id + " = " + DbUtils.sql(value))));
        } catch (Exception ex) {
            cell.desc.setText("");
        }
        return cell;
    }
}