/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui.utils;

import it.tnx.commons.CastUtils;
import it.tnx.invoicex.utils.IvaCache;
import java.awt.Color;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author mceccarelli
 */
public class TableCellEditorIva extends AbstractCellEditor implements TableCellEditor {

    JPanelCellIva iva;

    public TableCellEditorIva() {
        iva = new JPanelCellIva();
    }

    public Object getCellEditorValue() {
        return iva.codice.getText();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//        if (isSelected) {            
//            clifor.setForeground(table.getSelectionForeground());
//            clifor.ragione_sociale.setForeground(table.getForeground());
//            clifor.codice.setForeground(table.getForeground());
//            clifor.setBackground(table.getSelectionBackground());
//            clifor.ragione_sociale.setBackground(table.getBackground());
//            clifor.codice.setBackground(table.getBackground());
//        } else {
//            Color background = table.getBackground();
//            clifor.setForeground(table.getForeground());
//            clifor.ragione_sociale.setForeground(table.getForeground());
//            clifor.codice.setForeground(table.getForeground());
//            clifor.setBackground(background);
//            clifor.ragione_sociale.setBackground(background);
//            clifor.codice.setBackground(background);
//        }

        iva.codice.setText((String) value);
        iva.codice.selectAll();
        try {
            iva.descrizione.setText(IvaCache.getDescrizione(CastUtils.toString(value)));
            iva.descrizione.setCaretPosition(0);
        } catch (Exception ex) {
            iva.descrizione.setText("");
        }

        
        Point loc = MouseInfo.getPointerInfo().getLocation();        
        SwingUtilities.convertPointFromScreen(loc, table);
        
        Rectangle rect = table.getCellRect(row, column, true);
        Point p = rect.getLocation();
        if (iva.codice.getWidth() != 0) {
            p.x = p.x + iva.codice.getWidth();
        } else {
            p.x = (int) (p.x + iva.codice.getPreferredSize().getWidth());
        }
        if (loc.getX() >= p.getX()) {        
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    iva.descrizione.requestFocus();
                }
            });
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    iva.codice.requestFocus();
                }
            });            
        }
        
        return iva;
    }

}
