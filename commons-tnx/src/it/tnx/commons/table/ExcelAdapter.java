/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons.table;

import it.tnx.commons.SwingUtils;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

/**
 *
 * @author mceccarelli
 */
public class ExcelAdapter implements ActionListener {

    private String rowstring, value;
    private Clipboard system;
    private StringSelection stsel;
    private JTable jTable1;

    /**
     * The Excel Adapter is constructed with a
     * JTable on which it enables Copy-Paste and acts
     * as a Clipboard listener.
     */
    public ExcelAdapter(JTable myJTable) {
        jTable1 = myJTable;
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        // Identifying the copy KeyStroke user can modify this
        // to copy on some other Key combination.
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        // Identifying the Paste KeyStroke user can modify this
        //to copy on some other Key combination.
        jTable1.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
        jTable1.registerKeyboardAction(this, "Paste", paste, JComponent.WHEN_FOCUSED);
        system = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /**
     * Public Accessor methods for the Table on which this adapter acts.
     */
    public JTable getJTable() {
        return jTable1;
    }

    public void setJTable(JTable jTable1) {
        this.jTable1 = jTable1;
    }

    /**
     * This method is activated on the Keystrokes we are listening to
     * in this implementation. Here it listens for Copy and Paste ActionCommands.
     * Selections comprising non-adjacent cells result in invalid selection and
     * then copy action cannot be performed.
     * Paste is done by aligning the upper left corner of the selection with the
     * 1st element in the current selection of the JTable.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareTo("Copy") == 0) {
            StringBuffer sbf = new StringBuffer();
            // Check to ensure we have selected only a contiguous block of
            // cells
            int numcols = jTable1.getSelectedColumnCount();
            int numrows = jTable1.getSelectedRowCount();
            int[] rowsselected = jTable1.getSelectedRows();
            int[] colsselected = jTable1.getSelectedColumns();
            if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] &&
                    numrows == rowsselected.length) &&
                    (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] &&
                    numcols == colsselected.length))) {
                JOptionPane.showMessageDialog(null, "Invalid Copy Selection",
                        "Invalid Copy Selection",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (int i = 0; i < numrows; i++) {
                for (int j = 0; j < numcols; j++) {
                    sbf.append(jTable1.getValueAt(rowsselected[i], colsselected[j]));
                    if (j < numcols - 1) {
                        sbf.append("\t");
                    }
                }
                sbf.append("\n");
            }
            stsel = new StringSelection(sbf.toString());
            system = Toolkit.getDefaultToolkit().getSystemClipboard();
            system.setContents(stsel, stsel);
        }
        if (e.getActionCommand().compareTo("Paste") == 0) {
            startPaste();
            System.out.println("Trying to Paste");
            int startRow = (jTable1.getSelectedRows())[0];
            int startCol = (jTable1.getSelectedColumns())[0];
            try {
//                String trstring = (String) (system.getContents(this).getTransferData(DataFlavor.stringFlavor));
                String trstring = (String) system.getData(DataFlavor.stringFlavor);

                StringTokenizer st1 = new StringTokenizer(trstring, "\n");
                int pasterows = 0;
                for (int i = 0; st1.hasMoreTokens(); i++) {
                    st1.nextToken();
                    pasterows++;
                }
                if (pasterows > jTable1.getRowCount()) {
                    ensureRows(pasterows);
                }
//                System.out.println("String is:" + trstring);
                st1 = new StringTokenizer(trstring, "\n");
                for (int i = 0; st1.hasMoreTokens(); i++) {
                    rowstring = st1.nextToken();
                    String[] splits = rowstring.split("\\t");
                    for (int j = 0; j < splits.length; j++) {
                        value = splits[j];
//                        System.out.println("Putting '" + value + "' at row = " + startRow + i + " column =" + startCol + j);
                        if (startRow + i < jTable1.getRowCount() &&
                                startCol + j < jTable1.getColumnCount()) {
                            jTable1.setValueAt(value, startRow + i, startCol + j);
                        }
                    }
                }
                endPaste();
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtils.showErrorMessage(FocusManager.getCurrentManager().getActiveWindow(), ex.toString());
            }
        }
    }

    public void ensureRows(int pasterows) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void endPaste() {
    }

    public void startPaste() {
    }
}
