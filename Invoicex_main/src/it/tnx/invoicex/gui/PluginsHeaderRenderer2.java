/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Marco
 */
public class PluginsHeaderRenderer2 implements TableCellRenderer {
    
    TableCellRenderer delegate = null;
    PanelPackHeaderRenderer panel = new PanelPackHeaderRenderer();
    TableCellRenderer default_render = null;

    PluginsHeaderRenderer2(TableCellRenderer defaultRenderer) {
        delegate = defaultRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (default_render == null) {
            default_render = jtable.getTableHeader().getDefaultRenderer();
        }
        JComponent comp = (JComponent) default_render.getTableCellRendererComponent(jtable, value, isSelected, hasFocus, row, column);
        //se metto bordo su mac con java 7 non funziona, glitch grafici
//        try {
//            panel.setBorder(comp.getBorder());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return panel;
    }
        
    
}
