/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.gui;

import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author Marco
 */
public class JXTableSs extends JXTable {
    
    public JXTableSs() {
        super();
//        JTextField tf = new JTextField();
//        tf.setBorder(BorderFactory.createEmptyBorder());
//        setDefaultEditor(Object.class, new DefaultCellEditor(tf));
//        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    @Override
    public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component comp = super.prepareEditor(editor, row, column);
        if (comp instanceof JTextField) {
            ((JTextField)comp).setFont(getFont());
        }
        return comp;
    }
    
    //  Place cell in edit mode when it 'gains focus'
    public void changeSelection(final int row, final int column, boolean toggle, boolean extend) {
        super.changeSelection(row, column, toggle, extend);
        System.out.println("changeSel:" + row + " " + column);        
        TableCellEditor celledit = getCellEditor();
        if (celledit != null) {
            celledit.cancelCellEditing();
        }
        if (editCellAt(row, column)) {
            Component comp = getEditorComponent();
            comp.requestFocusInWindow();
            if (comp instanceof JTextField) {
                JTextField textComp = (JTextField)comp;
                textComp.selectAll();
            }
        }
    }    
    
}