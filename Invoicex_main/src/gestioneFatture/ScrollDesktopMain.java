/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gestioneFatture;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

class ScrollDesktop extends JDesktopPane implements Scrollable {

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle r, int axis, int dir) {
        return 50;
    }

    public int getScrollableBlockIncrement(Rectangle r, int axis, int dir) {
        return 200;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}

public class ScrollDesktopMain extends JFrame {

    public ScrollDesktopMain() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JDesktopPane desk = new ScrollDesktop();
        desk.setPreferredSize(new Dimension(1000, 1000));
        getContentPane().add(new JScrollPane(desk), "Center");
        JInternalFrame f1 = new JInternalFrame("Frame 1");
        f1.getContentPane().add(new JLabel("This is frame f1"));
        f1.setResizable(true);
        f1.pack();
        f1.setVisible(true);
        desk.add(f1, new Integer(10));

        JInternalFrame f2 = new JInternalFrame("Frame 2");
        f2.getContentPane().add(new JLabel("Content for f2"));
        f2.setResizable(true);
        f2.pack();
        f2.setVisible(true);
        desk.add(f2, new Integer(20));

        JInternalFrame f3 = new JInternalFrame("Frame 3");
        f3.getContentPane().add(new JLabel("Content for f3"));
        f3.setResizable(true);
        f3.pack();
        f3.setVisible(true);
        desk.add(f3, new Integer(20));

        f3.toFront();
        try {
            f3.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {
        }

        pack();
        setSize(300, 300);
        setVisible(true);
    }

    public static void main(String arg[]) {
        new ScrollDesktopMain();
    }
}
