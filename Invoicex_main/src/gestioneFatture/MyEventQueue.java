package gestioneFatture;

import it.tnx.commons.DbUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.dbeans.ResultSet.LazyResultSetModel;
import it.tnx.dbeans.pdfPrint.PrintSimpleTable;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.PlatformUtils;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import org.jdesktop.swingworker.SwingWorker;
import tnxbeans.tnxDbGrid;
import tnxbeans.tnxDbPanel;

/*
 * This source file is based on example by Kumar Santhosh 
 * http://www.jroller.com/santhosh/entry/implementing_undo_redo_in_right
 */
public class MyEventQueue extends EventQueue {

    public static Map oldColsWidth = new HashMap();

    protected void dispatchEvent(AWTEvent event) {
        try {
            super.dispatchEvent(event);
        } catch (java.lang.ArrayIndexOutOfBoundsException ei1) {
            ei1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object compo = event.getSource();
//        if (compo instanceof Container) {
//            System.out.println("awt event " + event + " compo:" + compo.getClass());
//        }
        if (event instanceof ComponentEvent) {
            ComponentEvent cevent = (ComponentEvent) event;
            if (cevent.getID() == ComponentEvent.COMPONENT_SHOWN) {
                Component comp = cevent.getComponent();

//                System.out.println("comp:" + comp.getClass() + " " + comp);
//                if (comp instanceof JInternalFrame) {
//                    //tutti i textfield
//                    abilitaUndo((JInternalFrame) comp);
//                }
                if (comp instanceof Container) {

                    List<Component> comps = SwingUtils.getAllComponents((Container) comp);
                    for (Component compn : comps) {
                        if (compn instanceof JTable) {

                            caricaColonne((JTable) compn);

                            ((JTable) compn).addPropertyChangeListener("model", new PropertyChangeListener() {
                                public void propertyChange(PropertyChangeEvent evt) {
                                    System.out.println("JTable model evt = " + evt);

                                    caricaColonne((JTable) evt.getSource());

                                }
                            });
                        }
                    }

//                    ((Container)comp).addContainerListener(new ContainerListener() {
//                        public void componentAdded(ContainerEvent e) {
//                            if (e.getComponent() instanceof JTable) {
//                                System.out.println("aaaaaaaaa");
//                            }
//                        }
//                        public void componentRemoved(ContainerEvent e) {
//                        }
//                    });
                }

            }
        }

        if (event instanceof KeyEvent) {
            if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_F1 && event.getID() == KeyEvent.KEY_PRESSED) {
                System.out.println("f1 event = " + event);
                if (main.getPadrePanel() != null) {
                    JInternalFrame ifr = main.getPadrePanel().getDesktopPane().getSelectedFrame();
                    if (ifr != null) {
                        System.out.println("f1 ifr = " + ifr.getTitle());
                        Component comp = ifr.getFocusOwner();
                        System.out.println("f1 comp internal = " + comp);
                    } else {
                        Component comp = main.getPadreWindow().getFocusOwner();
                        System.out.println("f1 comp main = " + comp);
                    }
                }
            }
        }

        if (!(event instanceof MouseEvent)) {
            return;
        }

        MouseEvent me = (MouseEvent) event;
        if (!me.isPopupTrigger()) {
            return;
        }
        Component comp = SwingUtilities.getDeepestComponentAt(me.getComponent(), me.getX(), me.getY());

        JPopupMenu menu_p = null;
        if (MenuSelectionManager.defaultManager().getSelectedPath().length > 0) {
            System.out.println("MenuSelectionManager.defaultManager().getSelectedPath().length > 0");
            try {
                System.out.println(MenuSelectionManager.defaultManager().getSelectedPath()[0]);
                Object menu = MenuSelectionManager.defaultManager().getSelectedPath()[0];
                if (menu instanceof JPopupMenu) {
                    menu_p = (JPopupMenu) menu;
                    comp = menu_p.getInvoker();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if ((comp instanceof JTextComponent)) {
            JTextComponent tc = (JTextComponent) comp;
            if (menu_p == null) {
                menu_p = new JPopupMenu();
            } else if (!cu.s(menu_p.getName()).equals("init")) {
                menu_p.addSeparator();
            }
            if (!cu.s(menu_p.getName()).equals("init")) {
                menu_p.add(new CutAction(tc));
                menu_p.add(new CopyAction(tc));
                menu_p.add(new PasteAction(tc));
                menu_p.add(new DeleteAction(tc));
                menu_p.addSeparator();
                menu_p.add(new SelectAllAction(tc));
                menu_p.addSeparator();
                menu_p.add(new UndoAction(tc));
                menu_p.add(new RedoAction(tc));
            }
            Point pt = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), tc);
            menu_p.pack();
            menu_p.show(tc, pt.x, pt.y);
            menu_p.setName("init");
        }

        boolean aggiungoEsportaTab = false;
//        if (PlatformUtils.isMac()) {
//            if (comp instanceof JTable && (!(comp instanceof JXTreeTable))) {
//                aggiungoEsportaTab = true;
//            }
//        } else {
        if (comp instanceof JTable) {
            aggiungoEsportaTab = true;
        }
//        }
        if (aggiungoEsportaTab) {

            JTable table = (JTable) comp;

            Point pt = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), table);
            if (table.getSelectedRowCount() <= 1) {
                int r = table.rowAtPoint(pt);
                if (r >= 0 && r < table.getRowCount()) {
                    table.setRowSelectionInterval(r, r);
                }
            }

            JPopupMenu menu = new JPopupMenu();
            if (menu_p == null) {
                menu_p = menu;
            } else {
//                menu_p.setVisible(false);
            }
            boolean presente = false;
            for (MenuElement elem : menu_p.getSubElements()) {
                try {
//                    System.out.println("elem:" + elem);
//                    System.out.println("(MenuItem)elem).getLabel():" + ((JMenuItem)elem).getText());
                    if (((JMenuItem) elem).getAction() instanceof EsportaAction) {
                        presente = true;
                    }
//                    if (((JMenuItem)elem).getText().equalsIgnoreCase("Esporta in Excel")) presente = true;
                } catch (Exception e) {
                }
            }
            if (!presente) {
                if (menu_p.getSubElements().length > 0) {
                    menu_p.addSeparator();
                }
                menu_p.add(new EsportaAction(table));

                //aggiungo definizione colonne
                menu_p.add(new SalvaColonneAction(table));
//                menu_p.add(new CaricaColonneAction(table));
                menu_p.add(new ResetColonneAction(table));

            }

            menu_p.pack();
            menu_p.show(table, pt.x, pt.y);
        }

    }

    static public void abilitaUndo(Component comp) {
        if (comp instanceof JTextComponent) {
//            System.out.println("abilito undo su:" + comp.getName() + " : " + comp.getClass() + " : " + comp);
            JTextComponent tf = (JTextComponent) comp;
            final UndoManager undoManagertf = new UndoManager();
            tf.getActionMap().put("Undo", new AbstractAction("Undo") {

                public void actionPerformed(ActionEvent e) {
                    try {
                        undoManagertf.undo();
                    } catch (Exception ex) {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            });
            tf.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
            if (PlatformUtils.isMac()) {
                tf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_DOWN_MASK), "Undo");
            }
            tf.getActionMap().put("Redo", new AbstractAction("Redo") {

                public void actionPerformed(ActionEvent e) {
                    try {
                        undoManagertf.redo();
                    } catch (Exception ex) {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            });
            tf.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
            if (PlatformUtils.isMac()) {
                tf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.META_DOWN_MASK), "Redo");
            }
            MyUndoableEditListener myundo = new MyUndoableEditListener();
            myundo.comp = tf;
            myundo.undoManager = undoManagertf;
            tf.getDocument().addUndoableEditListener(myundo);
        } else if (comp instanceof Container) {
            Container cont = (Container) comp;
            for (Component comp2 : cont.getComponents()) {
                abilitaUndo(comp2);
            }
        }
    }

    synchronized static void caricaColonne(JTable comp) {
        caricaColonne(comp, null);
    }

    synchronized static void caricaColonne(JTable comp, Object oldCols) {
        //oldCols serve per il Reset e quindi rimettere le dimensioni originali

        if (comp.getColumnModel().getColumnCount() == 0) {
            return;
        }

        Properties p = new SortedProperties();
        String nomefile = System.getProperty("user.home") + "/.invoicex/colonne.txt";

        try {
            if (new File(nomefile).exists()) {
                p.load(new FileInputStream(nomefile));
            }
        } catch (IOException ex) {
            SwingUtils.showExceptionMessage(comp, ex);
        }

        Object parent = SwingUtils.getParentJInternalFrame(comp);
        if (parent == null) {
            parent = SwingUtils.getActiveFrame();
        }
        String parent_name = SwingUtils.getFrameId(parent);
        String table_id = SwingUtils.getTableId(comp, parent);
        String k = parent_name + "_" + table_id;

//        System.out.println("carica colonne " + k);        
        Map oldSizeJTable = new HashMap();
        if (oldCols == null) {
            if (!oldColsWidth.containsKey(k)) {
                if (comp instanceof tnxDbGrid && ((tnxDbGrid) comp).columnsSizePerc != null) {
                    oldColsWidth.put(k, ((tnxDbGrid) comp).columnsSizePercOrig != null ? ((tnxDbGrid) comp).columnsSizePercOrig : ((tnxDbGrid) comp).columnsSizePerc);
                }
            }
        }

        Integer wold = cu.i(p.getProperty(k + "_width"));
        int wora = comp.getWidth();
//        System.out.println("w ora = " + wora + " / w old = " + wold);

        double rapporto = 1d;

// AUTO_RESIZE_OFF, AUTO_RESIZE_NEXT_COLUMN, AUTO_RESIZE_SUBSEQUENT_COLUMNS, AUTO_RESIZE_LAST_COLUMN, AUTO_RESIZE_ALL_COLUMNS        
        if (comp.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF) {
//            System.out.println("AUTO_RESIZE_OFF");
            if (wold != null) {
                rapporto = (double) wora / (double) wold;
            }
        } else if (comp.getAutoResizeMode() == JTable.AUTO_RESIZE_NEXT_COLUMN) {
//            System.out.println("AUTO_RESIZE_NEXT_COLUMN");
        } else if (comp.getAutoResizeMode() == JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS) {
//            System.out.println("AUTO_RESIZE_SUBSEQUENT_COLUMNS");
        } else if (comp.getAutoResizeMode() == JTable.AUTO_RESIZE_LAST_COLUMN) {
//            System.out.println("AUTO_RESIZE_LAST_COLUMN");
        } else if (comp.getAutoResizeMode() == JTable.AUTO_RESIZE_ALL_COLUMNS) {
//            System.out.println("AUTO_RESIZE_ALL_COLUMNS");
        }

//        if (wold != null) {
//            rapporto = (double)wora / (double)wold;
//        }
        if (comp instanceof tnxDbGrid) {
            tnxDbGrid tgrid = (tnxDbGrid) comp;
            System.out.println("tgrid.dbNomeTabella : " + tgrid.dbNomeTabella + " - " + tgrid.oldSql);
        }

        TableColumnModel tcm = comp.getColumnModel();
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            String colid = cu.s(tcm.getColumn(i).getIdentifier());
            String kcol = k + "_col_" + cu.s(tcm.getColumn(i).getIdentifier());

            if (oldCols == null) {
                if (!oldColsWidth.containsKey(k)) {
                    oldSizeJTable.put(colid + "_pw", tcm.getColumn(i).getPreferredWidth());
                    oldSizeJTable.put(colid + "_w", tcm.getColumn(i).getWidth());
                }
            }

            Integer wcol = cu.i(p.get(kcol + "_width"));

            if (wcol == null && oldCols == null) {
//                System.out.println("w col " + colid + " null");
            } else //                tcm.getColumn(i).setMaxWidth(cu.i(wcol));
            //                tcm.getColumn(i).setMinWidth(cu.i(wcol));
             if (comp instanceof tnxDbGrid && ((tnxDbGrid) comp).columnsSizePerc != null) {
                    tnxDbGrid tgrid = (tnxDbGrid) comp;
                    double w = (double) (cu.d0(wcol) * rapporto);
                    if (oldCols != null) {
                        Hashtable holdCols = (Hashtable) oldCols;
                        w = cu.d(holdCols.get(colid));
                    }
//                    System.out.println("w col " + colid + " ora = " + tcm.getColumn(i).getWidth() + " / w col old = " + wcol + " / imposto a " + w);
                    tgrid.columnsSizePerc.put(colid, w);
                } else {
                    int pw = (int) (cu.i0(wcol) * rapporto);
                    int w = pw;
                    if (oldCols != null) {
                        Map moldCols = (Map) oldCols;
                        pw = cu.i(moldCols.get(colid + "_pw"));
                        w = cu.i(moldCols.get(colid + "_w"));
                    }
                    tcm.getColumn(i).setPreferredWidth(pw);
                    tcm.getColumn(i).setWidth(w);
                }
        }
        if (comp instanceof tnxDbGrid && ((tnxDbGrid) comp).columnsSizePerc != null) {
            tnxDbGrid tgrid = (tnxDbGrid) comp;
            tgrid.columnsSizePercOrig = null;
            tgrid.calcolaColSizePerc();
            tgrid.resizeColumnsPerc(true);
        } else if (!oldColsWidth.containsKey(k) && oldCols == null) {
            oldColsWidth.put(k, oldSizeJTable);
        }
    }

}

class EsportaAction extends AbstractAction {

    JTable comp;

    public EsportaAction(JTable comp) {
        super("Esporta tabella in Excel", new ImageIcon(CutAction.class.getResource("/res/icons/tango-icon-theme-080/16x16/mimetypes/x-office-spreadsheet.png")));
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {

        System.out.println("esporta");

        PrintSimpleTable print = new PrintSimpleTable(comp);
        ResultSet rs = null;
        if (comp instanceof JTable) {
            if (((JTable)comp).getModel() instanceof LazyResultSetModel) {
                String sql = ((LazyResultSetModel)((JTable)comp).getModel()).sql;
                rs = Db.openResultSet(sql);
                print = new PrintSimpleTable(rs);
            }
        }
        
        String file = print.printExcel("Export", null, "", "");
        if (rs != null) {
            dbu.close(rs);
        }

        System.out.println("esporta finito " + file);
        Util.start2(file);

    }

    public boolean isEnabled() {
        return true;
    }
}

class CutAction extends AbstractAction {

    JTextComponent comp;

    public CutAction(JTextComponent comp) {
        super("Taglia", new ImageIcon(CutAction.class.getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-cut.png")));
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.cut();
        if (InvoicexUtil.getDbPanel(comp) != null) {
            InvoicexUtil.getDbPanel(comp).dbForzaModificati();
        }
    }

    public boolean isEnabled() {
        return comp.isEditable()
                && comp.isEnabled()
                && comp.getSelectedText() != null;
    }
}

class PasteAction extends AbstractAction {

    JTextComponent comp;

    public PasteAction(JTextComponent comp) {
        super("Incolla", new ImageIcon(CutAction.class.getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-paste.png")));
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.paste();
        if (InvoicexUtil.getDbPanel(comp) != null) {
            InvoicexUtil.getDbPanel(comp).dbForzaModificati();
        }
    }

    public boolean isEnabled() {
        if (comp.isEditable() && comp.isEnabled()) {
            Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
            return contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        } else {
            return false;
        }
    }
}

class DeleteAction extends AbstractAction {

    JTextComponent comp;

    public DeleteAction(JTextComponent comp) {
        super("Cancella", new ImageIcon(CutAction.class.getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-delete.png")));
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.replaceSelection(null);
        if (InvoicexUtil.getDbPanel(comp) != null) {
            InvoicexUtil.getDbPanel(comp).dbForzaModificati();
        }
    }

    public boolean isEnabled() {
        return comp.isEditable()
                && comp.isEnabled()
                && comp.getSelectedText() != null;
    }
}

class CopyAction extends AbstractAction {

    JTextComponent comp;

    public CopyAction(JTextComponent comp) {
        super("Copia", new ImageIcon(CutAction.class.getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-copy.png")));
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.copy();
    }

    public boolean isEnabled() {
        return comp.isEnabled()
                && comp.getSelectedText() != null;
    }
}

class SelectAllAction extends AbstractAction {

    JTextComponent comp;

    public SelectAllAction(JTextComponent comp) {
        super("Seleziona tutto", new ImageIcon(CutAction.class.getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-select-all.png")));
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.selectAll();
    }

    public boolean isEnabled() {
        return comp.isEnabled()
                && comp.getText().length() > 0;
    }
}

class UndoAction extends AbstractAction {

    JTextComponent comp;

    public UndoAction(JTextComponent comp) {
        super("Annulla", new ImageIcon(CutAction.class.getResource("/it/tnx/invoicex/res/Delete Sign-16.png")));
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.getActionMap().get("Undo").actionPerformed(null);
    }

    public boolean isEnabled() {
        UndoableEditListener[] listeners = ((AbstractDocument) comp.getDocument()).getListeners(UndoableEditListener.class);
//        System.out.println("listeners:" + listeners.length + ":" + listeners);
        for (UndoableEditListener uel : listeners) {
            if (uel instanceof MyUndoableEditListener) {
                UndoManager um = ((MyUndoableEditListener) uel).undoManager;
                if (um.canUndo()) {
                    return true;
                }
            }
        }
        return false;
    }
}

class RedoAction extends AbstractAction {

    JTextComponent comp;

    public RedoAction(JTextComponent comp) {
        super("Ripeti", new ImageIcon(CutAction.class.getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-redo.png")));
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {
        comp.getActionMap().get("Redo").actionPerformed(null);
    }

    public boolean isEnabled() {
        UndoableEditListener[] listeners = ((AbstractDocument) comp.getDocument()).getListeners(UndoableEditListener.class);
//        System.out.println("listeners:" + listeners.length + ":" + listeners);
        for (UndoableEditListener uel : listeners) {
            if (uel instanceof MyUndoableEditListener) {
                UndoManager um = ((MyUndoableEditListener) uel).undoManager;
                if (um.canRedo()) {
                    return true;
                }
            }
        }
        return false;
    }
}

class MyUndoableEditListener implements UndoableEditListener {

    public JTextComponent comp;
    public UndoManager undoManager;

    public void undoableEditHappened(UndoableEditEvent e) {
        undoManager.addEdit(e.getEdit());
    }
}

class SalvaColonneAction extends AbstractAction {

    JTable comp;

    public SalvaColonneAction(JTable comp) {
//        super("Salva colonne", new ImageIcon(CutAction.class.getResource("/res/icons/tango-icon-theme-080/16x16/mimetypes/x-office-spreadsheet.png")));
        super("Salva larghezza colonne", new ImageIcon(SalvaColonneAction.class.getResource("/it/tnx/invoicex/res/Grid-check-16.png")));
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {

        System.out.println("salva colonne");

        Properties p = new SortedProperties();
        String nomefile = System.getProperty("user.home") + "/.invoicex/colonne.txt";

        try {
            if (new File(nomefile).exists()) {
                p.load(new FileInputStream(nomefile));
            }
        } catch (IOException ex) {
            SwingUtils.showExceptionMessage(comp, ex);
        }

        Object parent = SwingUtils.getParentJInternalFrame(comp);
        if (parent == null) {
            parent = SwingUtils.getActiveFrame();
        }
        String parent_name = SwingUtils.getFrameId(parent);
        String table_id = SwingUtils.getTableId(comp, parent);

        String k = parent_name + "_" + table_id;

        p.put(k + "_width", cu.s(comp.getWidth()));

        TableColumnModel tcm = comp.getColumnModel();
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            String kcol = k + "_col_" + cu.s(tcm.getColumn(i).getIdentifier());
            p.put(kcol + "_width", cu.s(tcm.getColumn(i).getWidth()));
        }

        try {
            p.store(new FileOutputStream(nomefile), "");
        } catch (IOException ex) {
            SwingUtils.showExceptionMessage(comp, ex);
        }

    }

    public boolean isEnabled() {
        return true;
    }

}

class CaricaColonneAction extends AbstractAction {

    JTable comp;

    public CaricaColonneAction(JTable comp) {
//        super("Salva colonne", new ImageIcon(CutAction.class.getResource("/res/icons/tango-icon-theme-080/16x16/mimetypes/x-office-spreadsheet.png")));
        super("Carica colonne", new ImageIcon(CaricaColonneAction.class.getResource("/it/tnx/invoicex/res/Grid-check-16.png")));
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {

        MyEventQueue.caricaColonne(comp);

    }

    public boolean isEnabled() {
        return true;
    }

}

class ResetColonneAction extends AbstractAction {

    JTable comp;

    public ResetColonneAction(JTable comp) {
//        super("Salva colonne", new ImageIcon(CutAction.class.getResource("/res/icons/tango-icon-theme-080/16x16/mimetypes/x-office-spreadsheet.png")));
        super("Reset larghezza colonne", new ImageIcon(ResetColonneAction.class.getResource("/it/tnx/invoicex/res/Grid-delete-16.png")));
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e) {

        System.out.println("reset colonne");

        Properties p = new SortedProperties();
        String nomefile = System.getProperty("user.home") + "/.invoicex/colonne.txt";

        try {
            if (new File(nomefile).exists()) {
                p.load(new FileInputStream(nomefile));
            }
        } catch (IOException ex) {
            SwingUtils.showExceptionMessage(comp, ex);
        }

        Object parent = SwingUtils.getParentJInternalFrame(comp);
        if (parent == null) {
            parent = SwingUtils.getActiveFrame();
        }
        String parent_name = SwingUtils.getFrameId(parent);
        String table_id = SwingUtils.getTableId(comp, parent);

        String k = parent_name + "_" + table_id;

        List toremove = new ArrayList();
        for (Object key : p.keySet()) {
            if (cu.s(key).startsWith(k)) {
                toremove.add(key);
            }
        }
        for (Object ktoremove : toremove) {
            p.remove(ktoremove);
        }

        try {
            p.store(new FileOutputStream(nomefile), "");
        } catch (IOException ex) {
            SwingUtils.showExceptionMessage(comp, ex);
        }

        //rimetto originali
        if (MyEventQueue.oldColsWidth.containsKey(k)) {
            MyEventQueue.caricaColonne(comp, MyEventQueue.oldColsWidth.get(k));
        }

    }

    public boolean isEnabled() {
        return true;
    }

}
