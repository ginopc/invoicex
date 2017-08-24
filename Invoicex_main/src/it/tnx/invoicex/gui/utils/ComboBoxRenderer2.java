/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui.utils;

import java.awt.Component;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author mceccarelli
 */
public class ComboBoxRenderer2 extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        try {
            if (value instanceof Map) {
                label.setText((String) ((Map) value).get("d"));
            } else {
                label.setText((String) value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            label.setText("");
        }
        return label;
    }
}