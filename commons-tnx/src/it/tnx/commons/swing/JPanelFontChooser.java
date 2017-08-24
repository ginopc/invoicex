/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanelFontChooser.java
 *
 * Created on 9-mar-2011, 10.34.46
 */
package it.tnx.commons.swing;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicMenuUI.ChangeHandler;

/**
 *
 * @author mceccarelli
 */
public class JPanelFontChooser extends javax.swing.JPanel {

    Font def = null;

    /** Creates new form JPanelFontChooser */
    public JPanelFontChooser() {
        initComponents();

        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = e.getAllFonts(); // Get the fonts
        DefaultComboBoxModel fontsm = (DefaultComboBoxModel) font_names.getModel();
        for (Font f : fonts) {
            if (fontsm.getIndexOf(f.getFamily()) < 0) {
                fontsm.addElement(f.getFamily());
            }
        }

        font_size.setValue(10);
        font_size.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                aggiornascritta();
            }
        });

        setDefFont(UIManager.getDefaults().getFont("Label.font"), true);
    }

    private void aggiornascritta() {
        Font f = new Font((String)font_names.getSelectedItem(), Font.PLAIN, (Integer)font_size.getValue());
        esempio.setFont(f);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        font_names = new javax.swing.JComboBox();
        font_size = new javax.swing.JSpinner();
        esempio = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        carattere_standard = new javax.swing.JLabel();

        jLabel3.setText("jLabel3");

        font_names.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                font_namesItemStateChanged(evt);
            }
        });

        esempio.setText("Esempio di scritta del carattere selezionato");

        jLabel2.setForeground(new java.awt.Color(102, 102, 102));
        jLabel2.setText("Carattere di sistema:");

        carattere_standard.setForeground(new java.awt.Color(102, 102, 102));
        carattere_standard.setText("...");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(esempio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(carattere_standard))
                    .add(layout.createSequentialGroup()
                        .add(font_names, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(font_size, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(esempio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(font_names, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(font_size, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(carattere_standard))
                .addContainerGap(113, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void font_namesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_font_namesItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            aggiornascritta();
        }
    }//GEN-LAST:event_font_namesItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel carattere_standard;
    private javax.swing.JLabel esempio;
    public javax.swing.JComboBox font_names;
    public javax.swing.JSpinner font_size;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    // End of variables declaration//GEN-END:variables

    private Font getDefFont() {
        return def;
    }

    public void setDefFont(Font f, boolean select) {
        carattere_standard.setText(f.getFamily() + " " + f.getSize());
        carattere_standard.setFont(f);
        if (select) {
            font_size.setValue(f.getSize());
            font_names.setSelectedItem(f.getFamily());
        }
    }

    public static void main(String[] args) {
        JDialog d = new JDialog();
        d.getContentPane().add(new JPanelFontChooser());
        d.pack();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
    }
}