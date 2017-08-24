/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui.utils;

import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JInternalFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class NumeroRigaCellEditor extends CellEditorFoglio {
    public Integer colonna = null;
    public JInternalFrame frame = null;
    
    public NumeroRigaCellEditor(JTextField textField) {
        super(textField);
    }

    @Override
    public boolean stopCellEditing() {
        if (table != null) {
            //ciclo per cerca numero riga uguale
            for (int i = 0; i < table.getRowCount(); i++) {
                if (i != table.getSelectedRow()) {
                    Integer riga = cu.toInteger0(table.getValueAt(i, colonna));
                    Integer riga_edit = cu.toInteger0(getCellEditorValue());
                    if (riga == riga_edit) {
                        Rectangle rect = table.getCellRect(table.getSelectedRow(), colonna, true);
                        Point loc = rect.getLocation();
                        loc.translate(0, (int) rect.getHeight());
                        SwingUtilities.convertPointToScreen(loc, table);
                        SwingUtils.showFlashMessage2("Non puoi usare questo numero perchè già inserito in altra riga", 3, loc, Color.red);
                        return false;
                    }
                }
            }
        }
        return super.stopCellEditing();
    }
    
    
}
