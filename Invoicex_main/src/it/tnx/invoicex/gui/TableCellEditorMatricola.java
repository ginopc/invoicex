/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.DefaultCellEditor;
import org.apache.commons.lang.StringUtils;
/**
 *
 * @author mceccarelli
 */
public class TableCellEditorMatricola extends AbstractCellEditor implements TableCellEditor {

    JPanelCellMatricola cell;

    public TableCellEditorMatricola(JPanelCellMatricola cell) {
        this.cell = cell;
    }

    public Object getCellEditorValue() {
        if (StringUtils.isBlank(cell.text.getText())) return null;
        return cell.text.getText();
        
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//        if (isSelected) {            
//            cell.setForeground(table.getSelectionForeground());
//            cell.desc.setForeground(table.getSelectionForeground());
//            cell.text.setForeground(table.getSelectionForeground());
//            cell.setBackground(table.getSelectionBackground());
//            cell.desc.setBackground(table.getSelectionBackground());
//            cell.text.setBackground(table.getSelectionBackground());
//        } else {
//            Color background = table.getBackground();
//            cell.setForeground(table.getForeground());
//            cell.desc.setForeground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveForeground"));
//            cell.text.setForeground(table.getForeground());
//            cell.setBackground(background);
//            cell.desc.setBackground(background);
//            cell.text.setBackground(background);
//        }

        System.out.println("getTableCellEditorComponent value = " + value);
        cell.text.setText((String) value);
        cell.text.requestFocus();
        cell.text.requestFocusInWindow();
        
//        cell.text.selectAll();
//
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                cell.text.requestFocus();
//            }
//        });

        return cell;
    }

}
