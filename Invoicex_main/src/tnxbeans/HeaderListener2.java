/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tnxbeans;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.table.JTableHeader;

/**
 *
 * @author Marco
 */
public class HeaderListener2 extends MouseAdapter {

    JTableHeader header;
    SortButtonRenderer2 renderer;
    public int oldSortedColumn = -1;
    boolean oldIsAscent = false;

    public HeaderListener2(JTableHeader header, SortButtonRenderer2 renderer) {
        this.header = header;
        this.renderer = renderer;
    }

    public void resort() {
        if (oldSortedColumn >= 0) {
            System.out.println("resort: column:" + oldSortedColumn);
            if (header.getTable().isEditing()) {
                header.getTable().getCellEditor().stopCellEditing();
            }
            ((SortableTableModel) header.getTable().getModel()).sortByColumn(oldSortedColumn, oldIsAscent);
        }
    }

    public void mousePressed(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int col = header.columnAtPoint(e.getPoint());
            boolean isAscent;
            if (SortButtonRenderer2.DOWN == renderer.getState(col)) {
                isAscent = true;
            } else {
                isAscent = false;
            }            
            sort(col, isAscent);
        }
    }

    public void sort(int col, boolean isAscent) {
        int sortCol = header.getTable().convertColumnIndexToModel(col);
        oldSortedColumn = sortCol;
        renderer.setPressedColumn(col);
        renderer.setSelectedColumn(col);

        if (header.getTable().isEditing()) {
            header.getTable().getCellEditor().stopCellEditing();
        }

        oldIsAscent = isAscent;
        System.out.println("resort: column:" + sortCol);
        ((SortableTableModel) header.getTable().getModel()).sortByColumn(sortCol, isAscent);

        header.repaint();
    }

}
