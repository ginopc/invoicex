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
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import org.apache.commons.lang.StringUtils;
/**
 *
 * @author mceccarelli
 */
public class TableCellEditorKeyValue extends AbstractCellEditor implements TableCellEditor {

    JPanelCellKeyValue cell;

    public TableCellEditorKeyValue(JPanelCellKeyValue panel) {
        cell = panel;
    }

    public Object getCellEditorValue() {
        if (StringUtils.isBlank(cell.id.getText())) return null;
        return cell.id.getText();
        
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
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
            cell.desc.setForeground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveForeground"));
            cell.id.setForeground(table.getForeground());
            cell.setBackground(background);
            cell.desc.setBackground(background);
            cell.id.setBackground(background);
        }

        cell.id.setText((String) value);
        cell.id.selectAll();
        try {
            cell.desc.setText(cu.toString(DbUtils.getObject(Db.getConn(), "select " + cell.campo_descrizione + " from " + cell.tabella + " where " + cell.campo_id + " = " + DbUtils.sql(value))));
        } catch (Exception ex) {
            cell.desc.setText("");
        }

//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                cell.desc.requestFocus();
//            }
//        });

        return cell;
    }

}
