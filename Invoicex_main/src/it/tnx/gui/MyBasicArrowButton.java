/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.gui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 *
 * @author Marco
 */
public class MyBasicArrowButton extends BasicArrowButton {

    Color f_shadow = UIManager.getColor("ComboBox.buttonShadow");
    Color f_highlight = UIManager.getColor("ComboBox.buttonHighlight");
    Color f_darkShadow = UIManager.getColor("ComboBox.buttonDarkShadow");

    public MyBasicArrowButton(int direction, Color background, Color shadow, Color darkShadow, Color highlight) {
        super(direction, background, shadow, darkShadow, highlight);
    }

    public MyBasicArrowButton(int direction) {
        super(direction);
    }

    @Override
    public void paintTriangle(Graphics g, int x, int y, int size, int direction, boolean isEnabled) {
        super.paintTriangle(g, x, y, size, direction, isEnabled); //To change body of generated methods, choose Tools | Templates.
    }

    public void paint(Graphics g) {
        Color origColor;
        boolean isPressed, isEnabled;
        int w, h, size;

        w = getSize().width;
        h = getSize().height;
        origColor = g.getColor();
        isPressed = getModel().isPressed();
        isEnabled = isEnabled();

        g.setColor(getBackground());
        g.fillRect(0, 0, w - 1, h - 1);

        /// Draw the proper Border
        if (getBorder() != null && !(getBorder() instanceof UIResource)) {
            paintBorder(g);
        } else if (isPressed) {
            g.setColor(f_shadow);
            g.fillRect(0, 0, w - 1, h - 1);

            g.setColor(f_darkShadow);
            g.drawRect(0, 0, w - 1, h - 1);
        } else {
            // Using the background color set above
            g.setColor(f_highlight);
            g.drawRect(0, 0, w - 1, h - 1);
            g.setColor(f_darkShadow);     // black drop shadow  __|
        }

        // If there's no room to draw arrow, bail
        if (h < 5 || w < 5) {
            g.setColor(origColor);
            return;
        }

        g.translate(1, 1);

        // Draw the arrow
        size = Math.min((h - 4) / 3, (w - 4) / 3);
        size = Math.max(size, 2);
        paintTriangle(g, (w - size) / 2, (h - size) / 2,
                size, direction, isEnabled);

        // Reset the Graphics back to it's original settings
        if (isPressed) {
            g.translate(-1, -1);
        }
        g.setColor(origColor);

    }

}
