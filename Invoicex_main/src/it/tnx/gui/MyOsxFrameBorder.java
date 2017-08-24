/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.Border;


/**
 *
 * @author Marco
 */
public class MyOsxFrameBorder implements Border {

    Insets i = new Insets(1, 1, 1, 1);

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int marg = 7;
        
        g.setColor(Color.lightGray);
        
        System.out.println("border x:" + x + " y:" + y + " w:" + width + " h:" + height);
        //g.drawLine(x + marg, y, x + width - marg * 2, y);
        
        g.drawLine(x, y, x, y + height);
        g.drawLine(x + width - 1, y, x + width - 1, y + height - 1 - marg);

        g.drawLine(x, y + height - 1, x + width - 1 - marg, y + height - 1);
    }

    public Insets getBorderInsets(Component c) {
        return i;
    }

    public boolean isBorderOpaque() {
        return true;
    }
}
