package tnxbeans;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * @version 1.0 02/25/99
 */
public class SortButtonRenderer2 extends JLabel implements TableCellRenderer {

    public static final int NONE = 0;
    public static final int DOWN = 1;
    public static final int UP = 2;
    int pushedColumn;
    Hashtable state;

    public SortButtonRenderer2() {
        super();
        pushedColumn = -1;
        state = new Hashtable();
        setToolTipText("Doppio clic per cambiare l'ordinamento");
        setHorizontalTextPosition(JLabel.LEFT);
        setPreferredSize(new Dimension(getPreferredSize().width, 22));
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    }

    @Override
    public Insets getInsets() {
        return new Insets(2, 2, 2, 2);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        Object obj = state.get(new Integer(column));
        setIcon(null);
        if (obj != null) {
            if (((Integer) obj).intValue() == DOWN) {
                setIcon(new ImageIcon(getClass().getResource("/tnxbeans/go-down.png")));
            } else {
                setIcon(new ImageIcon(getClass().getResource("/tnxbeans/go-up.png")));
            }
        }
        
        String todisplay = value.toString();
        if (table instanceof tnxDbGrid) {
            tnxDbGrid grid = (tnxDbGrid)table;
            String tmp = (String)grid.getColumnsTitlesDisplayed().get(column);
            if (tmp == null) {
                tmp = (String)grid.getColumnsTitlesDisplayed().get(value);
            }
            if (tmp != null) todisplay = tmp;
        }
        
        setText(todisplay);
        
        return this;
    }

    public void setPressedColumn(int col) {
        pushedColumn = col;
    }

    public void setSelectedColumn(int col) {
        if (col < 0) {
            return;
        }
        Integer value = null;
        Object obj = state.get(new Integer(col));
        if (obj == null) {
            value = new Integer(DOWN);
        } else {
            if (((Integer) obj).intValue() == DOWN) {
                value = new Integer(UP);
            } else {
                value = new Integer(DOWN);
            }
        }
        state.clear();
        state.put(new Integer(col), value);
    }

    public int getState(int col) {
        int retValue;
        Object obj = state.get(new Integer(col));
        if (obj == null) {
            retValue = NONE;
        } else {
            if (((Integer) obj).intValue() == DOWN) {
                retValue = DOWN;
            } else {
                retValue = UP;
            }
        }
        return retValue;
    }
}

