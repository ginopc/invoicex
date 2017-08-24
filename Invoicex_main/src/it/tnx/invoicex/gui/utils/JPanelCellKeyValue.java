/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanelCellConto.java
 *
 * Created on 24-ago-2011, 16.38.37
 */
package it.tnx.invoicex.gui.utils;

import com.jidesoft.hints.AbstractListIntelliHints;
import gestioneFatture.main;
import it.tnx.Db;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.StringUtilsTnx;
import it.tnx.commons.SwingUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.Border;

/**
 *
 * @author mceccarelli
 */
public class JPanelCellKeyValue extends javax.swing.JPanel {

    Stroke stroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0);
    AbstractListIntelliHints alRicerca = null;
    
    public String campo_id = null;
    public String campo_descrizione = null;
    public String tabella = null;
    public String titolo = null;

    /**
     * Creates new form JPanelCellConto
     */
    public JPanelCellKeyValue() {
        initComponents();
        
        alRicerca = new AbstractListIntelliHints(desc) {
            String current_search = "";

            @Override
            protected JList createList() {
                final JList list = super.createList();
                list.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        String img, tipo;
                        tipo = ((StringKeyValueHint) value).toString();
                        JLabel lab = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        String word = current_search;
                        String content = tipo;
                        Color c = null;
                        if (!isSelected) {
                            c = new Color(240, 240, 100);
                        } else {
                            c = new Color(100, 100, 40);
                        }
                        String rgb = Integer.toHexString(c.getRGB());
                        rgb = rgb.substring(2, rgb.length());

                        content = StringUtilsTnx.highlightWord(content, word, "<span style='background-color: " + rgb + "'>", "</span>");

                        lab.setText("<html>" + content + "</html>");
                        System.out.println(index + ":" + content);
                        return lab;
                    }
                });
                return list;
            }

            public boolean updateHints(Object arg0) {
//                if (arg0.toString().trim().length() <= 1) return false;
                SwingUtils.mouse_wait();
                current_search = arg0.toString();
                Connection conn;
                try {
                    conn = gestioneFatture.Db.getConn();

                    String sql = ""
                            + "SELECT " + campo_id + ", " + campo_descrizione + " FROM " + tabella
                            + " where (" + campo_id + " like '%" + Db.aa(current_search) + "%'"
                            + " or " + campo_descrizione + " like '%" + Db.aa(current_search) + "%'"
                            + " ) and " + campo_descrizione + " != ''"
                            + " order by " + campo_descrizione + ", " + campo_id + " limit 50";

                    System.out.println("sql ricerca:" + sql);
                    ResultSet rs = DbUtils.tryOpenResultSet(conn, sql);
                    Vector v = new Vector();

                    while (rs.next()) {
                        StringKeyValueHint kv = new StringKeyValueHint();
                        kv.key = rs.getString(1);
                        kv.value = rs.getString(2);
                        v.add(kv);
                    }

                    setListData(v);
                    rs.getStatement().close();
                    rs.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                SwingUtils.mouse_def();
                return true;
            }

            @Override
            public void acceptHint(Object arg0) {
                super.acceptHint(arg0);
                try {
                    id.setText(((StringKeyValueHint) arg0).key);
                    desc.setText(((StringKeyValueHint) arg0).value);
                } catch (Exception e) {
                    e.printStackTrace();
                    desc.setText(String.valueOf(arg0));
                }
            }
        };
        
        id.setBorder(new Border() {
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

//    public void addNotify() {
//        super.addNotify();
//        text.requestFocus();
//    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        id = new javax.swing.JTextField();
        link = new org.jdesktop.swingx.JXHyperlink();
        desc = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        id.setColumns(10);
        id.setBorder(null);
        add(id, java.awt.BorderLayout.WEST);

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

        desc.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 2, 0, 2));
        add(desc, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void linkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkActionPerformed
        JDialog dialog = new JDialog(main.getPadreFrame(), "Seleziona " + titolo, true);
        JPanelStandardList list = new JPanelStandardList();
        dialog.add(list);
        String sql = "select " + campo_id + ", " + campo_descrizione + " from " + tabella + " order by " + campo_descrizione;
        list.init(sql, campo_id, campo_descrizione, tabella);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        //if (frm.conto_selezionato != null) {
//            codice.setText((String) frm.conto_selezionato.get("id"));
//            ragione_sociale.setText((String) frm.conto_selezionato.get("descrizione"));
        //}
        if (list.selezionato) {
            id.setText(CastUtils.toString(list.ritorno_id));
            desc.setText(list.ritorno_nome);
        } else {
            //non selezionato
        }

    }//GEN-LAST:event_linkActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JTextField desc;
    public javax.swing.JTextField id;
    public org.jdesktop.swingx.JXHyperlink link;
    // End of variables declaration//GEN-END:variables
}
