package dnl.utils.text.table;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

//import javax.swing.RowSorter;
//import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
//import javax.swing.table.TableRowSorter;


/**
 * 
 * @author Daniel Orr
 * 
 */
public class TextTable {

	protected TableModel tableModel;
	protected List<SeparatorPolicy> separatorPolicies = new ArrayList<SeparatorPolicy>();

	protected boolean addRowNumbering;

	protected boolean headless;

	public TextTable(TableModel tableModel) {
		this.tableModel = tableModel;
	}

	public TextTable(TableModel tableModel, boolean addNumbering) {
		this.tableModel = tableModel;
		this.addRowNumbering = addNumbering;
	}

	public TextTable(String[] columnNames, Object[][] data) {
		this.tableModel = new DefaultTableModel(data, columnNames);
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public void setAddRowNumbering(boolean addNumbering) {
		this.addRowNumbering = addNumbering;
	}

	public void addSeparatorPolicy(SeparatorPolicy separatorPolicy) {
		separatorPolicies.add(separatorPolicy);
		separatorPolicy.setTableModel(tableModel);
	}

	public void printTable() {
		printTable(System.out, 0);
	}

	public void printTable(PrintStream ps, int indent) {
		TextTableRenderer renderer = new TextTableRenderer(this);
		renderer.render(ps, indent);
	}


	protected Object getValueAt(int row, int column) {
		int rowIndex = row;
		return tableModel.getValueAt(rowIndex, column);
	}

	protected boolean hasSeparatorAt(int row) {
		for (SeparatorPolicy separatorPolicy : separatorPolicies) {
			if (separatorPolicy.hasSeparatorAt(row)) {
				return true;
			}
		}
		return false;
	}

}
