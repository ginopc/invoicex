/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tnxbeans;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author mceccarelli
 */

public class SeparatorComboBoxRenderer extends DefaultListCellRenderer {

    JSeparator separator;
    static public final String SEPARATOR = "*SEPARATOR*";

    public SeparatorComboBoxRenderer() {
        separator = new JSeparator(JSeparator.HORIZONTAL);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        JLabel lab = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        lab.setMinimumSize(new Dimension(20, 20));
        lab.setPreferredSize(new Dimension(20,20));
        String str = (value == null) ? "" : value.toString();
        if (SEPARATOR.equals(str)) {
            return separator;
        } else {
            return lab;
        }
    }
}