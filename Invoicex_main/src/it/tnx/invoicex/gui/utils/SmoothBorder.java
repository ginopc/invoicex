/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import javax.swing.border.AbstractBorder;

/**
 *
 * @author Marco
 */
public class SmoothBorder extends AbstractBorder {

    private Color borderColour;
    private int gap;

    public SmoothBorder(Color colour, int g) {
        borderColour = colour;
        gap = g;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);
        Graphics2D g2d = null;
        if (g instanceof Graphics2D) {
            g2d = (Graphics2D) g;
            
            //Left Border    
            g2d.setColor(borderColour);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            g2d.drawRoundRect(x+1, y+1, width-2, height-2, 15, 15);
            
//            g2d.draw(new Line2D.Double((double) x + 10, (double) y + 10, (double) x + 10, (double) y + 20));
//            g2d.draw(new Line2D.Double((double) x + 10, (double) y + 10, (double) x + 20, (double) y + 10));
//            // Right Border
//            g2d.draw(new Line2D.Double((double) width - 10, (double) y + 10, (double) width - 10, (double) y + 20));
//            g2d.draw(new Line2D.Double((double) width - 10, (double) y + 10, (double) width - 20, (double) y + 10));
//            // Lower Left Border
//            g2d.draw(new Line2D.Double((double) x + 10, (double) height - 10, (double) x + 20, (double) height - 10));
//            g2d.draw(new Line2D.Double((double) x + 10, (double) height - 10, (double) x + 10, (double) height - 20));
//            // Lower Right Border
//            g2d.draw(new Line2D.Double((double) width - 10, (double) height - 10, (double) width - 20, (double) height - 10));
//            g2d.draw(new Line2D.Double((double) width - 10, (double) height - 10, (double) width - 10, (double) height - 20));
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return (getBorderInsets(c, new Insets(gap, gap, gap, gap)));
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = gap;
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}
