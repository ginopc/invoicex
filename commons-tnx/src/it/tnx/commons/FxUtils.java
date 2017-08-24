/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author mceccarelli
 */
public class FxUtils {

    static public void fadeBackground(JComponent comp, Color c) {
        fadeBackground(comp, c, 25);
    }
    
    static public void fadeBackground(JComponent comp, Color c, int delay) {
        fadeBackground(comp, c, comp.getBackground(), delay);
    }
    
    static public void fadeBackground(JComponent comp, Color colore_effetto, Color colore_start, int delay) {
        final FxUtilsFade fx = new FxUtilsFade();
        fx.colore_effetto = colore_effetto;
        fx.colore_start = colore_start;
        fx.comp = comp;
        Timer timerfx = new Timer(delay, new ActionListener() {
            int pass = 0;
            public void actionPerformed(final ActionEvent e) {
                pass++;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (fx.comp == null) {
                            ((Timer) e.getSource()).stop();
                            return;
                        }
                        fx.comp.setBackground(fx.getColor(pass));
                        if (pass >= 100) {
                            ((Timer) e.getSource()).stop();
                        }
                    }
                });
            }
        });
        timerfx.start();
    }    

    static public void fadeForeground(JComponent comp, Color c) {
        final FxUtilsFade fx = new FxUtilsFade();
        fx.colore_effetto = c;
        fx.colore_start = comp.getForeground();
        fx.comp = comp;
        Timer timerfx = new Timer(25, new ActionListener() {
            int pass = 0;
            public void actionPerformed(final ActionEvent e) {
                pass++;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (fx.comp == null) {
                            ((Timer) e.getSource()).stop();
                            return;
                        }
                        fx.comp.setForeground(fx.getColor(pass));
                        if (pass >= 100) {
                            ((Timer) e.getSource()).stop();
                        }
                    }
                });
            }
        });
        timerfx.start();
    }

    static class FxUtilsFade {

        Color colore_effetto = null;
        Color colore_start = null;
        JComponent comp;

        Color getColor(int pass) {
            int r = (int) ((double) Math.abs(colore_start.getRed() - colore_effetto.getRed()) / 100d * (double) pass);
            int g = (int) ((double) Math.abs(colore_start.getGreen() - colore_effetto.getGreen()) / 100d * (double) pass);
            int b = (int) ((double) Math.abs(colore_start.getBlue() - colore_effetto.getBlue()) / 100d * (double) pass);
            int r2 = 0;
            int g2 = 0;
            int b2 = 0;
            if (colore_effetto.getRed() > colore_start.getRed()) {
                r2 = colore_effetto.getRed() - r;
            } else {
                r2 = colore_effetto.getRed() + r;
            }
            if (colore_effetto.getGreen() > colore_start.getGreen()) {
                g2 = colore_effetto.getGreen() - g;
            } else {
                g2 = colore_effetto.getGreen() + g;
            }
            if (colore_effetto.getBlue() > colore_start.getBlue()) {
                b2 = colore_effetto.getBlue() - b;
            } else {
                b2 = colore_effetto.getBlue() + b;
            }
            Color color_new = new Color(r2, g2, b2);
            return color_new;
        }
    }
}
