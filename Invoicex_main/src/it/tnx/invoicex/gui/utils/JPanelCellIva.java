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
import gestioneFatture.IvaHint;
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
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 *
 * @author mceccarelli
 */
public class JPanelCellIva extends javax.swing.JPanel {

    Stroke stroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0);
    AbstractListIntelliHints alRicercaIva = null;

    /**
     * Creates new form JPanelCellConto
     */
    public JPanelCellIva() {
        initComponents();

        alRicercaIva = new AbstractListIntelliHints(descrizione) {
            String current_search = "";

            @Override
            protected JList createList() {
                final JList list = super.createList();
                list.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        String img, tipo;
                        tipo = ((IvaHint) value).toString();
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

//                        if (((ClienteHint) value).obsoleto) {
//                            content = "<span style='color: FF0000'>" + content + " (Obsoleto)</span>";
//                        }
                        lab.setText("<html>" + content + "</html>");
                        System.out.println(index + ":" + content);
                        return lab;
                    }
                });
                return list;
            }

            public boolean updateHints(Object arg0) {
                SwingUtils.mouse_wait();
                current_search = arg0.toString();
                Connection conn;
                try {
                    conn = Db.getConn();

                    String sql = ""
                            + "SELECT codice, descrizione, percentuale FROM codici_iva"
                            + " where (codice like '%" + Db.aa(current_search) + "%'"
                            + " or descrizione like '%" + Db.aa(current_search) + "%'"
                            + " or percentuale like '%" + Db.aa(current_search) + "%'"
                            + " ) and descrizione != ''"
                            + " order by codice limit 50";

                    System.out.println("sql ricerca:" + sql);
                    ResultSet rs = DbUtils.tryOpenResultSet(conn, sql);
                    Vector v = new Vector();

                    while (rs.next()) {
                        IvaHint iva = new IvaHint();
                        iva.codice = rs.getString(1);
                        iva.descrizione = rs.getString(2);
                        iva.percentuale = rs.getDouble(3);
                        v.add(iva);
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
                    if (((IvaHint) arg0).codice.equals("*")) {
                        codice.setText("");
                    } else {
                        codice.setText(((IvaHint) arg0).codice);
                    }
                    descrizione.setText(((IvaHint) arg0).descrizione);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        

        codice.setBorder(new Border() {
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
//        ragione_sociale.requestFocus();
//    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        codice = new javax.swing.JTextField();
        link = new org.jdesktop.swingx.JXHyperlink();
        descrizione = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        codice.setColumns(3);
        codice.setBorder(null);
        add(codice, java.awt.BorderLayout.WEST);

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

        descrizione.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 2, 0, 2));
        add(descrizione, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void linkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkActionPerformed
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        final JDialog dialog = new JDialog(main.getPadreFrame(), "Seleziona il codice IVA", true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JPanelStandardList list = new JPanelStandardList();
                dialog.add(list);
                list.init("select codice, descrizione, percentuale from codici_iva order by codice", "codice", "descrizione", "codici_iva");
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                if (list.selezionato) {
                    codice.setText(CastUtils.toString(list.ritorno_id));
                    descrizione.setText(list.ritorno_nome);
                } else {
                    //non selezionato
                }
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

    }//GEN-LAST:event_linkActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JTextField codice;
    public javax.swing.JTextField descrizione;
    public org.jdesktop.swingx.JXHyperlink link;
    // End of variables declaration//GEN-END:variables
}
