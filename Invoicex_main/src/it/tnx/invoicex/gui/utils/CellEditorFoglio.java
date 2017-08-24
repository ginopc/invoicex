/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui.utils;

import gestioneFatture.Menu;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;

/**
 *
 * @author mceccarelli
 */
public class CellEditorFoglio extends javax.swing.DefaultCellEditor {

    javax.swing.JTable table;
    java.awt.Component editComp;

    public CellEditorFoglio(javax.swing.JTextField textField) {
        super(textField);
        textField.setMargin(new Insets(1, 1, 1, 1));
        textField.setBorder(BorderFactory.createEmptyBorder());
    }

    public java.awt.Component getTableCellEditorComponent(javax.swing.JTable jTable, Object obj, boolean param, int param3, int param4) {

        final java.awt.Component edit;
        final java.awt.Component areaEdit;
        table = jTable;
        edit = super.getTableCellEditorComponent(jTable, obj, param, param3, param4);
        editComp = edit;
        edit.addFocusListener(new FocusListener() {
            public void focusGained(java.awt.event.FocusEvent evt) {

                javax.swing.JTextField textEdit = (javax.swing.JTextField) edit;
                textEdit.selectAll();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
            }
        });
        edit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editMouseClicked(evt);
            }
        });
        edit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                editKeyPressed(evt);
            }
        });

        //provare con key listener
        return edit;
    }

    public boolean shouldSelectCell(java.util.EventObject eventObject) {

        return true;
    }

    public boolean isCellEditable(java.util.EventObject eventObject) {

        return true;
    }

    public void editMouseClicked(java.awt.event.MouseEvent evt) {

        if (evt.getClickCount() == 2) {
            showZoom();
        }
    }

    public void editKeyPressed(java.awt.event.KeyEvent evt) {

        if (evt.getKeyCode() == evt.VK_F4) {
            showZoom();
        }
    }

    private void showZoom() {

        Frame[] frames = Menu.getFrames();

        for (int i = 0; i < frames.length; i++) {

            Frame f = (Frame) frames[i];

            if (f.getTitle().equalsIgnoreCase("zoom")) {
                f.setVisible(true);

                break;
            }
        }
    }
}