/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanelCellConto.java
 *
 * Created on 24-ago-2011, 16.38.37
 */
package it.tnx.invoicex.gui;

import com.jidesoft.hints.AbstractListIntelliHints;
import it.tnx.commons.SwingUtils;
import it.tnx.invoicex.gui.utils.StringKeyValueHint;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 *
 * @author mceccarelli
 */
public class JPanelCellMatricola extends javax.swing.JPanel {

    Stroke stroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0);
    AbstractListIntelliHints alRicerca = null;
    Vector beans;

    /**
     * Creates new form JPanelCellConto
     */
    public JPanelCellMatricola(Vector beans) {
        initComponents();

        this.beans = beans;

        alRicerca = new AbstractListIntelliHints(text) {
            String current_search = "";

            @Override
            protected JList createList() {
                final JList list = new JList() {
                    @Override
                    public int getVisibleRowCount() {
                        int size = getModel().getSize();
                        return size < super.getVisibleRowCount() ? size : super.getVisibleRowCount();
                    }

                    @Override
                    public Dimension getPreferredScrollableViewportSize() {
                        if (getModel().getSize() == 0) {
                            return new Dimension(0, 0);
                        } else {
//                            return super.getPreferredScrollableViewportSize();
                            return new Dimension(300, 300);
                        }
                    }
                };
                list.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        String img, tipo;
                        tipo = ((StringKeyValueHint) value).toString();
                        JLabel lab = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                        return lab;

//                        String word = current_search;
//                        String content = tipo;
//                        Color c = null;
//                        if (!isSelected) {
//                            c = new Color(240, 240, 100);
//                        } else {
//                            c = new Color(100, 100, 40);
//                        }
//                        String rgb = Integer.toHexString(c.getRGB());
//                        rgb = rgb.substring(2, rgb.length());
//
//                        content = StringUtilsTnx.highlightWord(content, word, "<span style='background-color: " + rgb + "'>", "</span>");
//
//                        lab.setText("<html>" + content + "</html>");
//                        System.out.println(index + ":" + content);
//                        return lab;
                    }
                });
                return list;
            }

            public boolean updateHints(Object arg0) {
//                if (arg0.toString().trim().length() <= 1) return false;
                SwingUtils.mouse_wait();
                current_search = arg0.toString();

//                setListData(new Vector(JPanelCellMatricola.this.beans));                
                //filtrare
                Vector fb = new Vector();
                for (Object o : JPanelCellMatricola.this.beans) {
                    String os = String.valueOf(o);
                    if (os.indexOf(current_search) >= 0) {
                        fb.add(o);
                    }
                }
                setListData(fb);

                SwingUtils.mouse_def();
                return true;
            }

            @Override
            public void acceptHint(Object arg0) {
                super.acceptHint(arg0);
                try {
                    text.setText(((StringKeyValueHint) arg0).key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        text.setBorder(new Border() {
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(stroke);
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawLine(width - 1, 0, width - 1, height);
            }

            public Insets getBorderInsets(Component c) {
                return new Insets(0, 2, 0, 2);
            }

            public boolean isBorderOpaque() {
                return true;
            }
        });

    }

//    @Override
//    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
//        System.out.println("ks:" + ks);
//        return text.processKeyBinding(ks, e, condition, pressed); //To change body of generated methods, choose Tools | Templates.
//        //return super.processKeyBinding(ks, e, condition, pressed); //To change body of generated methods, choose Tools | Templates.
//    }
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        InputMap map = text.getInputMap(condition);
        ActionMap am = text.getActionMap();

        if (map != null && am != null && isEnabled()) {
            Object binding = map.get(ks);
            Action action = (binding == null) ? null : am.get(binding);
            if (action != null) {
                return SwingUtilities.notifyAction(action, ks, e, text,
                        e.getModifiers());
            }
        }
        return false;
    }

    public void addNotify() {
        super.addNotify();
        text.requestFocus();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        text = new javax.swing.JTextField();
        link = new org.jdesktop.swingx.JXHyperlink();

        setLayout(new java.awt.BorderLayout());

        text.setColumns(10);
        text.setBorder(null);
        add(text, java.awt.BorderLayout.CENTER);

        link.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        link.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/places/folder-saved-search.png"))); // NOI18N
        link.setText("");
        link.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        link.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        link.setMargin(new java.awt.Insets(2, 2, 2, 2));
        link.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkActionPerformed(evt);
            }
        });
        add(link, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void linkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        alRicerca.showHints();
    }//GEN-LAST:event_linkActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public org.jdesktop.swingx.JXHyperlink link;
    public javax.swing.JTextField text;
    // End of variables declaration//GEN-END:variables
}
