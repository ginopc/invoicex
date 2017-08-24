/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/**
 *
 * @author mceccarelli
 */
public class StyledComboBoxUI extends BasicComboBoxUI {

    
    @Override
    protected Dimension getDisplaySize() {
        return super.getDisplaySize();
    }

    @Override
    protected Dimension getDefaultSize() {
        return super.getDefaultSize(); //To change body of generated methods, choose Tools | Templates.
    }

    protected ComboPopup createPopup() {
        BasicComboPopup popup = new BasicComboPopup(comboBox) {

            @Override
            protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
//                return super.computePopupBounds(px, py, Math.max(comboBox.getPreferredSize().width, pw), ph);
//                return super.computePopupBounds(px, py, 300, ph);

                //calcolo in base al contenuto
                Dimension result = new Dimension();

                ListCellRenderer renderer = comboBox.getRenderer();
                if (renderer == null) {
                    renderer = new DefaultListCellRenderer();
                }

                boolean sameBaseline = true;

                ComboBoxModel model = comboBox.getModel();
                int modelSize = model.getSize();
                int baseline = -1;
                Dimension d;

                Component cpn;

                if (modelSize > 0) {
                    for (int i = 0; i < modelSize; i++) {
                        // Calculates the maximum height and width based on the largest
                        // element
                        Object value = model.getElementAt(i);
                        Component c = renderer.getListCellRendererComponent(
                                listBox, value, -1, false, false);
                        d = mygetSizeForComponent(c);
                        result.width = Math.max(result.width, d.width);
                        result.height = Math.max(result.height, d.height);
                    }
                    result.width += 20;
                } else {
                    result = getDefaultSize();
                    if (comboBox.isEditable()) {
                        result.width = 100;
                    }
                }

                if (result.width > 600) {
                    result.width = 600;
                }
                return super.computePopupBounds(px, py, Math.max(result.width, pw), ph);
            }
        };
        popup.getAccessibleContext().setAccessibleParent(comboBox);
        return popup;
    }

    protected Dimension mygetSizeForComponent(Component comp) {
        // This has been refactored out in hopes that it may be investigated and
        // simplified for the next major release. adding/removing
        // the component to the currentValuePane and changing the font may be
        // redundant operations.
        currentValuePane.add(comp);
        comp.setFont(comboBox.getFont());
        Dimension d = comp.getPreferredSize();
        currentValuePane.remove(comp);
        return d;
    }

    protected JButton createArrowButton() {

        JButton button = new MyBasicArrowButton(BasicArrowButton.SOUTH);
        
        button.setName("ComboBox.arrowButton");
        return button;
    }

}
