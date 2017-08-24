/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gestioneFatture;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JTextField;

/**
 *
 * @author Marco
 */
public class JTextFieldRicerca extends JTextField {

    BufferedImage imageSearch;

    public JTextFieldRicerca() {
        super();
        try {
            imageSearch = ImageIO.read(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/system-search.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int y = (getHeight() - imageSearch.getHeight()) / 2;
        g.drawImage(imageSearch, 8, y, this);
    }

    @Override
    public Insets getMargin() {
        if (imageSearch == null) {
            return new Insets(4, 4, 4, 4);
        } else {
            return new Insets(4, 6 + imageSearch.getWidth(), 4, 4);
        }
    }

    @Override
    public Insets getInsets() {
        if (imageSearch == null) {
            return new Insets(8, 8, 8, 8);
        } else {
            return new Insets(8, 10 + imageSearch.getWidth(), 8, 8);
        }
    }
}
