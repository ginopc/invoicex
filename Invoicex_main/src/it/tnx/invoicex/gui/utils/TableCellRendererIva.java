/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui.utils;

import it.tnx.commons.CastUtils;
import it.tnx.invoicex.utils.IvaCache;
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
public class TableCellRendererIva implements TableCellRenderer {

    JPanelCellIva iva;
    JTable table;

    public TableCellRendererIva(JTable table) {
        this.table = table;
        iva = new JPanelCellIva();
        iva.link.setVisible(false);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            iva.setForeground(table.getSelectionForeground());
            iva.descrizione.setForeground(table.getSelectionForeground());
            iva.codice.setForeground(table.getSelectionForeground());
            iva.setBackground(table.getSelectionBackground());
            iva.descrizione.setBackground(table.getSelectionBackground());
            iva.codice.setBackground(table.getSelectionBackground());
        } else {
            Color background = table.getBackground();
            iva.setForeground(table.getForeground());
            iva.descrizione.setForeground(table.getForeground());
            iva.codice.setForeground(table.getForeground());
            iva.setBackground(background);
            iva.descrizione.setBackground(background);
            iva.codice.setBackground(background);
        }
        if (hasFocus) {
            Border border = null;
            if (isSelected) {
                border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = UIManager.getBorder("Table.focusCellHighlightBorder");
            }
            iva.setBorder(border);
        } else {
            iva.setBorder(null);
        }
        
        iva.codice.setText((String) value);
        try {
            iva.descrizione.setText(IvaCache.getDescrizione(CastUtils.toString(value)));
            iva.descrizione.setCaretPosition(0);
        } catch (Exception ex) {
            iva.descrizione.setText("");
        }
        return iva;
    }
}
