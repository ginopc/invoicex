/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons.table;

import it.tnx.commons.SwingUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author mceccarelli
 */
public class ExcelLikeJTable extends JTable {

    JPopupMenu pop = new JPopupMenu();
    JMenuItem miDelete = null;
    JMenuItem miNew = null;

    public ExcelLikeJTable(Object[][] rowData, Object[] columnNames) {
        super(rowData, columnNames);
        init();
    }

    public ExcelLikeJTable(Vector rowData, Vector columnNames) {
        super(rowData, columnNames);
        init();
    }

    public ExcelLikeJTable(int numRows, int numColumns) {
        super(numRows, numColumns);
        init();
    }

    public ExcelLikeJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super(dm, cm, sm);
        init();
    }

    public ExcelLikeJTable(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
        init();
    }

    public ExcelLikeJTable(TableModel dm) {
        super(dm);
        init();
    }

    public ExcelLikeJTable() {
        super();
        init();
    }

//    /**
//     * Overrides the editCellAt function to allow one click editing
//     * as opposed to the apending of cell edits that is default in
//     * JTable
//     */
//    public boolean editCellAt(int row, int column, EventObject e) {
//        boolean result = super.editCellAt(row, column, e);
//        final Component editor = getEditorComponent();
//
//        if (editor != null && editor instanceof JTextComponent) {
//            if (e == null) {
//                ((JTextComponent) editor).selectAll();
//            } else {
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        ((JTextComponent) editor).selectAll();
//                    }
//                });
//            }
//        }
//        return result;
//    }

    private void init() {
        //setSurrendersFocusOnKeystroke(true);
        try {
            miDelete = new JMenuItem("Cancella riga", new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/edit-cut.png")));
        } catch (Exception e) {
            miDelete = new JMenuItem("Cancella riga");
        }
        try {
            miNew = new JMenuItem("Aggiungi riga", new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png")));
        } catch (Exception e) {
            miNew = new JMenuItem("Aggiungi riga");
        }
        miDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (SwingUtils.showYesNoMessage(ExcelLikeJTable.this, "Sicuro di eliminare ?", "Attenzione")) {
                    if (getModel() instanceof DefaultTableModel) {
                        for (int i = getSelectedRowCount()-1; i >= 0; i--) {
                            ((DefaultTableModel)getModel()).removeRow( getSelectedRows()[i] );
                        }
                    }
                }
            }
        });
        miNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (getModel() instanceof DefaultTableModel) {
                    ((DefaultTableModel)getModel()).addRow(new Object[getColumnCount()]);
                }
            }
        });

        pop.add(miNew);
        pop.add(miDelete);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isPopupTrigger() || (e.getClickCount() == 1 && (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3))) {
                    pop.show(ExcelLikeJTable.this, e.getX(), e.getY());
                }
            }
        });
    }
}
