/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.FocusManager;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.View;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author test1
 */
public class SwingUtils {

    public static JWindow lastFlash2 = null;
    public static Timer lastTimerFlash2 = null;

    public static void openUrl(URL uRL) {
        //provo con desktop di java 6
        //java.awt.Desktop
        //public void browse(URI uri) throws IOException
        try {
            Class c = Class.forName("java.awt.Desktop");
            Method m1 = c.getMethod("getDesktop");
            Object desktop = m1.invoke(c);
            Method m2 = c.getMethod("browse", URI.class);
            m2.invoke(desktop, uRL.toURI());
        } catch (Throwable t) {
            t.printStackTrace();
            //provo con jdic...
            try {
                Class c = Class.forName("org.jdesktop.jdic.desktop.Desktop");
                Method m = c.getMethod("browse", URL.class);
                m.invoke(null, uRL);
            } catch (Throwable t2) {
                t2.printStackTrace();
                //provo a mano
                String os = System.getProperty("os.name").toLowerCase();
                if (os.startsWith("mac")) {
                    try {
                        Runtime.getRuntime().exec("open " + uRL.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (os.equals("windows 95") || os.equals("windows 98") || os.equals("windows me")) {
                    try {
                        Runtime.getRuntime().exec("start " + uRL.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (os.equals("windows nt") || os.equals("windows 2000") || os.equals("windows xp") || os.equals("windows vista") || os.startsWith("windows")) {
                    try {
                        Runtime.getRuntime().exec("cmd /C \"start " + uRL.toString() + "\"");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    //linux dividere fra kde e gnome
                    //gvfs-open ...
                    //gnome-open ...
                    //kfmclient openURL ...
                    try {                        
                        Runtime.getRuntime().exec("gvfs-open " + uRL.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        try {
                            Runtime.getRuntime().exec("gnome-open " + uRL.toString());                               
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                            try {
                                Runtime.getRuntime().exec("kfmclient openURL " + uRL.toString());   
                            } catch (Exception ex3) {
                                ex3.printStackTrace();
                            }                            
                        }
                    }
                }
            }
        }
    }

    public static void open(File file) {
        //provo con desktop di java 6
        //java.awt.Desktop
        //public void browse(URI uri) throws IOException
        try {
            if (file.getName().equals(".")) {
                file = file.getAbsoluteFile().getParentFile();
            }
            System.out.println("SwingUtils.open file = " + file);
            Class c = Class.forName("java.awt.Desktop");
            Method m1 = c.getMethod("getDesktop");
            Object desktop = m1.invoke(c);
            Method m2 = c.getMethod("open", File.class);
            m2.invoke(desktop, file);
        } catch (Throwable t) {
            System.out.println("SwingUtils.open 1 " + t);
            //provo con jdic...
            String os = System.getProperty("os.name").toLowerCase();
            try {
                if (os.startsWith("mac")) {
                    throw new Throwable("non provo su mac perche' puo' crashare");
                }
                Class c = Class.forName("org.jdesktop.jdic.desktop.Desktop");
                Method m = c.getMethod("open", File.class);
                m.invoke(null, file);
            } catch (Throwable t2) {
                System.out.println("SwingUtils.open 2 " + t2);
                //provo a mano
                if (os.startsWith("mac")) {
                    try {
                        Runtime.getRuntime().exec("open " + file.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (os.equals("windows 95") || os.equals("windows 98") || os.equals("windows me")) {
                    try {
                        Runtime.getRuntime().exec("start " + file.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (os.equals("windows nt") || os.equals("windows 2000") || os.equals("windows xp") || os.equals("windows vista") || os.startsWith("windows")) {
                    try {
                        //Runtime.getRuntime().exec("cmd /C \"start " + file.toString() + "\"");
                        Runtime.getRuntime().exec("cmd /C \"" + file.toString() + "\"");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    //linux dividere fra kde e gnome
                    //gvfs-open ...
                    //gnome-open ...
                    //kfmclient openURL ...
                    try {                        
                        Runtime.getRuntime().exec("gvfs-open " + file.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        try {
                            Runtime.getRuntime().exec("gnome-open " + file.toString());                               
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                            try {
                                Runtime.getRuntime().exec("kfmclient openURL " + file.toString());   
                            } catch (Exception ex3) {
                                ex3.printStackTrace();
                            }                            
                        }
                    }
                }
            }
        }
    }

    public static void openFolderSelect(File file) {
        //provo con desktop di java 6
        //java.awt.Desktop
        //public void browse(URI uri) throws IOException

        //prima provo con explorer
        /*
         Explorer [/n] [/e] [(,)/root,<oggetto>] [/select,<oggetto>]

         /n                Consente di aprire una nuova finestra con un unico riquadro per la
         selezione predefinita. Solitamente si tratta della directory
         principale dell'unità in cui è installato Windows. Se la finestra
         è già aperta, verrà aperto un duplicato.

         /e                Consente di aprire Esplora risorse utilizzando la visualizzazione predefinita.

         /root,<oggetto>   Consente di aprire una visualizzazione finestra dell'oggetto specificato.


         /select,<oggetto> Consente di aprire una visualizzazione finestra con la cartella, il file o
         l'applicazione selezionata.

         Esempi:

         Esempio 1:     Explorer /select,C:\TestDir\TestApp.exe

         Consente di aprire una visualizzazione finestra con TestApp selezionata.

         Esempio 2:     Explorer /e,/root,C:\TestDir\TestApp.exe

         Consente di aprire Esplora risorse con l'unità C: espansa e TestApp selezionata.

         Esempio 3:     Explorer /root,\\TestSvr\TestCondiv

         Consente di aprire una visualizzazione finestra della condivisione specificata.

         Esempio 4:     Explorer /root,\\TestSvr\TestCondiv,select,TestApp.exe

         Consente di aprire una visualizzazione finestra della condivisione specific
         * */
        try {
            ProcessBuilder pb = new ProcessBuilder("explorer", "/select," + file.getCanonicalPath());
            pb.start();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                file = file.getCanonicalFile().getParentFile();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        try {
            Class c = Class.forName("java.awt.Desktop");
            Method m1 = c.getMethod("getDesktop");
            Object desktop = m1.invoke(c);
            Method m2 = c.getMethod("open", File.class);
            m2.invoke(desktop, file);
        } catch (Throwable t) {
            System.out.println("SwingUtils.open 1 " + t);
            //provo con jdic...
            String os = System.getProperty("os.name").toLowerCase();
            try {
                if (os.startsWith("mac")) {
                    throw new Throwable("non provo su mac perche' puo' crashare");
                }
                Class c = Class.forName("org.jdesktop.jdic.desktop.Desktop");
                Method m = c.getMethod("open", File.class);
                m.invoke(null, file);
            } catch (Throwable t2) {
                System.out.println("SwingUtils.open 2 " + t2);
                //provo a mano

                if (os.startsWith("mac")) {
                    try {
                        Runtime.getRuntime().exec("open " + file.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (os.equals("windows 95") || os.equals("windows 98") || os.equals("windows me")) {
                    try {
                        Runtime.getRuntime().exec("start " + file.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (os.equals("windows nt") || os.equals("windows 2000") || os.equals("windows xp") || os.equals("windows vista") || os.startsWith("windows")) {
                    try {
                        Runtime.getRuntime().exec("cmd /C \"start " + file.toString() + "\"");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    //linux dividere fra kde e gnome
                    //gvfs-open ...
                    //gnome-open ...
                    //kfmclient openURL ...
                    try {                        
                        Runtime.getRuntime().exec("gvfs-open " + file.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        try {
                            Runtime.getRuntime().exec("gnome-open " + file.toString());                               
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                            try {
                                Runtime.getRuntime().exec("kfmclient openURL " + file.toString());   
                            } catch (Exception ex3) {
                                ex3.printStackTrace();
                            }                            
                        }
                    }
                }
            }
        }
    }

    static public void showImage(Image img) {
        JFrame frame = new JFrame();
        JLabel lab = new JLabel();
        lab.setIcon(new ImageIcon(img));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(lab, BorderLayout.CENTER);
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
    }

    static public void showFlashMessage(final String msg, final int secondi) {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setResizable(false);
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.CENTER);
        layout.setHgap(30);
        layout.setVgap(30);
        frame.setLayout(layout);

        JLabel lab = new JLabel(msg);
        frame.getContentPane().add(lab);

        frame.pack();
        frame.setLocationRelativeTo(null);

        Timer timer = new Timer(secondi * 1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();

        frame.setVisible(true);
    }

    static public JWindow showFlashMessage2(final String msg, final int secondi) {
        return showFlashMessage2(msg, secondi, null, null);
    }

    static public JWindow showFlashMessage2Comp(final String msg, final int secondi, JComponent comp, final Color borderColor) {
        Point p = comp.getLocationOnScreen();
        int w = comp.getGraphics().getFontMetrics().stringWidth(msg);
        int h = comp.getHeight();
        p.translate(-(((w + 40) / 2) - (comp.getWidth() / 2)), h);
        return showFlashMessage2(msg, secondi, p, borderColor, null, false);
    }

    static public JWindow showFlashMessage2(final String msg, final int secondi, Point location, final Color borderColor) {
        return showFlashMessage2(msg, secondi, location, borderColor, null, false);
    }

    static public JWindow showFlashMessage2(final String msg, final int secondi, Point location, final Color borderColor, Font font, boolean centerScreen) {
        return showFlashMessage2(msg, secondi, location, borderColor, font, centerScreen, null);
    }

    static public JWindow showFlashMessage2(final String msg, final int secondi, Point location, final Color borderColor, Font font, boolean centerScreen, Integer opacity) {
        if (lastFlash2 != null) {
            lastFlash2.dispose();
            lastFlash2 = null;
        }
        if (lastTimerFlash2 != null) {
            lastTimerFlash2.stop();
            lastTimerFlash2 = null;
        }

        lastFlash2 = new JWindow();
        //"com.sun.awt.AWTUtilities"
        try {
//            ReflectUtils.runMethod("com.sun.awt.AWTUtilities", "setWindowOpacity", new Object[] {lastFlash2, 0.5f});
            ReflectUtils.runMethod("com.sun.awt.AWTUtilities", "setWindowOpaque", new Object[]{lastFlash2, false});
        } catch (Exception e) {
            e.printStackTrace();
        }

        lastFlash2.setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(getBackground());
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (borderColor == null) {
                    g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                    g2d.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                    g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 14, 14);
                    g2d.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 12, 12);
                    g2d.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 10, 10);
                    g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 8, 8);
                } else {
                    g2d.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 12, 12);
                    g2d.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 10, 10);
                    g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 8, 8);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.setColor(borderColor);
                    g2d.drawRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 10, 10);
                }

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//                super.paintComponent(g2d);
            }
        });
        if (opacity == null) {
            opacity = 125;
        }
        lastFlash2.getContentPane().setBackground(new Color(255, 255, 255, opacity));
        lastFlash2.setAlwaysOnTop(true);

        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.CENTER);
        layout.setHgap(10);
        layout.setVgap(10);
        lastFlash2.setLayout(layout);

        JLabel lab = new JLabel(msg);
        if (font != null) {
            lab.setFont(font);
        }
        lastFlash2.getContentPane().add(lab);

        JButton close = new JButton("x");
        close.setMargin(new Insets(1, 1, 1, 1));
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    lastFlash2.dispose();
                    lastFlash2 = null;
                } catch (Exception ex) {
                }
                if (lastTimerFlash2 != null) {
                    lastTimerFlash2.stop();
                    lastTimerFlash2 = null;
                }
            }
        });
        lastFlash2.getContentPane().add(close);

        lastFlash2.pack();

        if (location == null) {
            if (centerScreen) {
                lastFlash2.setLocationRelativeTo(null);
            } else {
                Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                lastFlash2.setLocation(d.width - lastFlash2.getWidth() - 20, 20);
            }
        } else {
            lastFlash2.setLocation(location);
        }

        lastTimerFlash2 = new Timer(secondi * 1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    lastFlash2.dispose();
                    lastFlash2 = null;
                } catch (Exception ex) {
                }
            }
        });
        lastTimerFlash2.setRepeats(false);
        lastTimerFlash2.start();

        lastFlash2.setVisible(true);

        return lastFlash2;
    }

    static public void mouse_wait() {
        try {
            Component comp = FocusManager.getCurrentManager().getFocusOwner();
            if (comp == null) {
                return;
            }
            comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            //fare a ritroso dal component al parent ...
            boolean ok = true;
            while (ok) {
                Object p = comp.getParent();
                if (p == null) {
                    ok = false;
                } else {
                    if (p instanceof Component) {
                        comp = (Component) p;
                        if (p instanceof JFrame || p instanceof Frame || p instanceof JInternalFrame) {
                            comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        }
                    } else {
                        ok = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void mouse_def() {
        try {
            Component comp = FocusManager.getCurrentManager().getFocusOwner();
            if (comp == null) {
                return;
            }
            comp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            //fare a ritroso dal component al parent ...
            boolean ok = true;
            while (ok) {
                Object p = comp.getParent();
                if (p == null) {
                    ok = false;
                } else {
                    if (p instanceof Component) {
                        comp = (Component) p;
                        if (p instanceof JFrame || p instanceof Frame || p instanceof JInternalFrame) {
                            comp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                    } else {
                        ok = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void mouse_wait(Component comp) {
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    static public void mouse_def(Component comp) {
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     *
     * @param combo Combo da riempire
     * @param conn Connessione sql
     * @param sql Query da cui prendere i dati
     * @param keyField nome campo chiave
     * @param displayField nome campo da visualizzare
     * @param firstItem eventuale primo elemento
     */
    static public void initJComboFromDb(JComboBox combo, Connection conn, String sql, String keyField, String displayField, KeyValuePair... item) {
        ResultSet r = null;
        try {
            r = DbUtils.tryOpenResultSet(conn, sql);
            Vector<KeyValuePair> list = new Vector<KeyValuePair>();

            if (item != null) {
                for (KeyValuePair i : item) {
                    list.add(i);
                }
            }
            while (r.next()) {
                KeyValuePair kv = new KeyValuePair(r.getObject(keyField), r.getObject(displayField));
                list.add(kv);
            }
            DefaultComboBoxModel model = new DefaultComboBoxModel(list);
            combo.setModel(model);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (r != null) {
                try {
                    r.getStatement().close();
                    r.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    static public void initJComboFromKVList(JComboBox combo, Vector<KeyValuePair> list) {
        DefaultComboBoxModel model = new DefaultComboBoxModel(list);
        combo.setModel(model);
    }

    static public boolean findJComboKV(JComboBox combo, Object key) {
        return findJComboKV(combo, key, false);
    }
    
    static public boolean findJComboKV(JComboBox combo, Object key, boolean forceKeyString) {
        try {
            for (int i = 0; i < combo.getItemCount(); i++) {
                Object o = combo.getItemAt(i);
                if (forceKeyString) {
                    String keys = cu.s(key);
                    if (o != null && o instanceof KeyValuePair && ((KeyValuePair) o).getKey() != null && cu.s(((KeyValuePair) o).getKey()).equals(keys)) {
                        combo.setSelectedIndex(i);
                        return true;
                    }
                } else {
                    if (o != null && o instanceof KeyValuePair && ((KeyValuePair) o).getKey() != null && ((KeyValuePair) o).getKey().equals(key)) {
                        combo.setSelectedIndex(i);
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    static public void showWarningMessage(Component parent, String message) {
        showWarningMessage(parent, message, "Attenzione");
    }

    static public void showWarningMessage(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    static public void showInfoMessage(Component parent, String message) {
        showInfoMessage(parent, message, "Attenzione");
    }

    static public void showInfoMessage(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    static public void showErrorMessage(Component parent, String message) {
        showErrorMessage(parent, message, "Errore", false);
    }

    static public void showErrorMessage(Component parent, String message, boolean wait) {
        showErrorMessage(parent, message, "Errore", wait);
    }

    static public void showErrorMessage(Component parent, String message, final String title) {
        showErrorMessage(parent, message, title, false);
    }

    static public void showErrorMessage(final Component parent, final String message, final String title, boolean wait) {
        if (System.getProperty("java.awt.headless", "false").equalsIgnoreCase("true")) {
            System.out.println(title + ":" + message);
            return;
        }
        
        if (wait) {
            JOptionPane.showMessageDialog(parent == null ? getActiveFrame() : parent, message, title, JOptionPane.ERROR_MESSAGE);
        } else {
            inEdt(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(parent == null ? getActiveFrame() : parent, message, title, JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    static public Window getActiveFrame() {
        Frame[] frames = Frame.getFrames();
        Window result = null;
        for (Frame frame : frames) {
            if (frame.isActive()) {
                result = frame;
                break;
            } else {
                Window[] windows = frame.getOwnedWindows();
                for (Window w : windows) {
                    if (w.isActive()) {
                        result = w;
                        break;
                    }
                }
            }
        }
        if (result == null) {
            for (Frame frame : frames) {
                if (frame.isShowing()) {
                    result = frame;
                }
            }
        }
        if (result == null && frames.length > 0) {
            result = frames[0];
        }
        try {
            System.out.println("debug getActiveFrame:" + result.getClass().getName());
        } catch (Exception e) {
        }
        return result;
    }

    static public void showExceptionMessage(Component parent, Exception ex) {
        showExceptionMessage(parent, ex, null);
    }
    static public void showExceptionMessage(Component parent, Exception ex, String messaggio) {
        ex.printStackTrace();
        StackTraceElement[] stes = ex.getStackTrace();
        String stack = "";
        for (StackTraceElement st : stes) {
            if (st.getClassName().startsWith("gestioneFatture") || st.getClassName().startsWith("it.tnx") || st.getClassName().startsWith("invoicex")) {
                if (!st.getFileName().equalsIgnoreCase("SwingUtils.java")) {
                    stack += " at " + st.getClassName()  + "." + st.getMethodName() + "(" + st.getFileName() + ":" + st.getLineNumber() + ")\n";
                }
            }
        }
        JOptionPane.showMessageDialog(parent, (messaggio == null ? "" : messaggio + "\n\n") + (StringUtils.isBlank(ex.getMessage()) ? ex.toString() : ex.getMessage()) + "\n\n" + stack, "Errore", JOptionPane.ERROR_MESSAGE);
    }
    
    static public void showThrowableMessage(Component parent, Throwable ex) {
        ex.printStackTrace();
        StackTraceElement[] stes = ex.getStackTrace();
        String stack = "";
        for (StackTraceElement st : stes) {
            if (st.getClassName().startsWith("gestioneFatture") || st.getClassName().startsWith("it.tnx") || st.getClassName().startsWith("invoicex")) {
                if (!st.getFileName().equalsIgnoreCase("SwingUtils.java")) {
                    stack += " at " + st.getClassName()  + "." + st.getMethodName() + "(" + st.getFileName() + ":" + st.getLineNumber() + ")\n";
                }
            }
        }
        JOptionPane.showMessageDialog(parent, (StringUtils.isBlank(ex.getMessage()) ? ex.toString() : ex.getMessage()) + "\n\n" + stack, "Errore", JOptionPane.ERROR_MESSAGE);
    }    

    static public boolean showYesNoMessage(Component parent, String message) {
        return showYesNoMessage(parent, message, "Attenzione");
    }

    static public boolean showYesNoMessage(Component parent, String message, String title) {
        int ret = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
        if (ret == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }

    static public JFileChooser getFileOpen(Component parent, File dir) {
        JFileChooser filechooser = new JFileChooser(dir) {
            @Override
            public void updateUI() {
                putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
                super.updateUI();
            }
        };
        return filechooser;
    }

    static public JFileChooser getFileOpen(Component parent) {
        JFileChooser filechooser = new JFileChooser() {
            @Override
            public void updateUI() {
                putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
                super.updateUI();
            }
        };
        return filechooser;
    }

    static public File showFileOpen(Component parent) {
        JFileChooser filechooser = new JFileChooser() {
            @Override
            public void updateUI() {
                putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
                super.updateUI();
            }
        };
        int ret = filechooser.showOpenDialog(parent);
        return filechooser.getSelectedFile();
    }

    static public String showInputPassword(Component parent, String title) {
        final JPasswordField jpf = new JPasswordField();
        JOptionPane jop = new JOptionPane(jpf,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = jop.createDialog(parent, "Password:");
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                jpf.requestFocusInWindow();
            }
        });
        dialog.setTitle(title);
        dialog.setVisible(true);
        try {
            int result = (Integer) jop.getValue();
            dialog.dispose();
            char[] password = null;
            if (result == JOptionPane.OK_OPTION) {
                password = jpf.getPassword();
                return new String(password);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    public static Component getCompByName(JComponent container, String name) {
        for (Component comp : container.getComponents()) {
            if (comp.getName() != null && comp.getName().equalsIgnoreCase(name)) {
                return comp;
            }
        }
        return null;
    }

    public static Component getCompByNameRicorsivo(JComponent container, String name) {
        for (Component comp : container.getComponents()) {
            if (comp.getName() != null && StringUtils.isNotBlank(comp.getName().toString())) {
                System.out.println("comp.getName() = " + comp.getName());
            }
            if (comp.getName() != null && comp.getName().equalsIgnoreCase(name)) {
                return comp;
            }
            if (comp instanceof JComponent && comp instanceof Container) {
                Component ret = getCompByNameRicorsivo((JComponent) comp, name);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    public static Color mixColours(Color... colors) {
        int red = 0;
        int green = 0;
        int blue = 0;
        int alpha = 0;
        for (Color c : colors) {
            red += c.getRed();
            green += c.getGreen();
            blue += c.getBlue();
            alpha += c.getAlpha();
        }
        return new Color(red / colors.length, green / colors.length, blue / colors.length, alpha / colors.length);
    }

    static public JInternalFrame getParentJInternalFrame(Component comp) {
        if (comp == null) {
            return null;
        }
        if (comp.getParent() == null) {
            return null;
        }
        if (comp.getParent() instanceof JInternalFrame) {
            return (JInternalFrame) comp.getParent();
        }
        return getParentJInternalFrame(comp.getParent());
    }

    public static void inEdt(Runnable run) {
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }

    public static void inEdtWait(Runnable run) throws InterruptedException, InvocationTargetException {
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeAndWait(run);
        }
    }

    public static void dopo(Runnable run) {
        Thread t = new Thread(run);
        t.start();
    }

    public static void dopoInEdt(final Runnable run) {
        Thread t = new Thread("dopo in edt") {
            @Override
            public void run() {
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException ex) {
//                }
                inEdt(run);
            }
        };
        t.start();
    }
    private static JLabel resizer = null;

    public static java.awt.Dimension getPreferredSize(String html, boolean width, int prefSize) {
        try {
            if (resizer == null) {
                SwingUtils.inEdtWait(new Runnable() {
                    public void run() {
                        resizer = new JLabel();
                    }
                });
            }
            resizer.setText(html);
            View view = (View) resizer.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
            view.setSize(width ? prefSize : 0, width ? 0 : prefSize);
            float w = view.getPreferredSpan(View.X_AXIS);
            float h = view.getPreferredSpan(View.Y_AXIS);
            return new java.awt.Dimension((int) Math.ceil(w), (int) Math.ceil(h));            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getColumnLocationX(JTable table, int columnIndex) {
        int totx = 0;
        for (int i = 0; i < columnIndex; i++) {
            totx += table.getColumnModel().getColumn(i).getWidth();
        }
        return totx;
    }

    public static boolean listContain(JList list, Object value) {
        for (int i = 0; i < list.getModel().getSize(); i++) {
            Object n = list.getModel().getElementAt(i);
            if (n != null && n.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static List<Component> getAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container) {
                compList.addAll(getAllComponents((Container) comp));
            }
        }
        return compList;
    }
    
    public static String getFrameId(Object parent) {
        String parent_name = parent.getClass().getName();
        if (parent_name.endsWith("JInternalFrame")) {
            parent_name += "_" + ((JInternalFrame) parent).getTitle();
        }
        if (parent_name.endsWith("JDialog")) {
            parent_name += "_" + ((JDialog) parent).getTitle();
        }
        return parent_name;
    }


    public static String getTableId(JTable tab, Object parent) {
        int i = 0;
        List<Component> comps = SwingUtils.getAllComponents((Container) parent);
        for (Component comp : comps) {
            if (comp instanceof JTable) {
                i++;
            }
            if (comp == tab) {
                return cu.s(i);
            }
        }
        return null;
    }

    public static int getScreenMaxWidth() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Rectangle r = focusOwner.getGraphicsConfiguration().getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(focusOwner.getGraphicsConfiguration());
        return r.width;
    }
    
    public static int getScreenMaxHeight() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Rectangle r = focusOwner.getGraphicsConfiguration().getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(focusOwner.getGraphicsConfiguration());
        return r.height;
    }    
    
    public static int getScreenAvailableMaxWidth() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Rectangle r = focusOwner.getGraphicsConfiguration().getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(focusOwner.getGraphicsConfiguration());
        return r.width - (screenInsets.right + screenInsets.left);
    }
    
    public static int getScreenAvailableMaxHeight() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Rectangle r = focusOwner.getGraphicsConfiguration().getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(focusOwner.getGraphicsConfiguration());
        return r.height - (screenInsets.top + screenInsets.bottom);
    }        

}
