/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Marco
 */
public class PluginsRenderer2 extends DefaultTableCellRenderer {

    PanelPackRenderer panel = new PanelPackRenderer();

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object o, boolean bln, boolean bln1, int i, int i1) {
        JLabel l = (JLabel) super.getTableCellRendererComponent(jtable, o, bln, bln1, i, i1);
        //return c;
        panel.setBorder(l.getBorder());
        panel.setBackground(l.getBackground());
        String pack = (String) o;
        String[] packs = StringUtils.split(pack, '|');
        List lpacks = Arrays.asList(packs);
        panel.base.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/no.png")));
        if (lpacks.contains("pro")) {
            panel.pro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/yes.png")));
        } else {
            panel.pro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/no.png")));
        }
        if (lpacks.contains("proplus")) {
            panel.proplus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/yes.png")));
        } else {
            panel.proplus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/no.png")));
        }
        if (lpacks.contains("ent")) {
            panel.ent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/yes.png")));
        } else {
            panel.ent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/no.png")));
        }

        return panel;
    }

}