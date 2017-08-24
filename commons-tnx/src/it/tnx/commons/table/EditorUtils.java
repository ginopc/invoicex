/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons.table;

import it.tnx.commons.CastUtils;
import it.tnx.commons.cu;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.tree.TreeCellEditor;

/**
 *
 * @author test1
 */
public class EditorUtils {

    static public class DateEditor extends DefaultCellEditor {

        JTextField tf = null;
        DateFormat df = DateFormat.getDateInstance();

        public DateEditor(final JTextField tf) {
            super(tf);
            this.tf = tf;
            init();
        }

        public DateEditor(final JTextField tf, DateFormat df) {
            super(tf);
            this.tf = tf;
            this.df = df;
            init();
        }

        private void init() {
            tf.setBorder(null);

            delegate = new EditorDelegate() {
                public void setValue(Object param) {
                    Date _value = (Date) param;
                    if (_value == null) {
                        tf.setText("");
                    } else {
                        tf.setText(df.format(_value));
                    }
                }

                public Object getCellEditorValue() {
                    try {
                        return ((Date) df.parse(tf.getText()));
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            };
        }
    }

    static public class CurrencyEditor extends DefaultCellEditor {

        JTextField tf = null;
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        public boolean returnNull = false;

        public CurrencyEditor() {
            super(new JTextField());
            this.tf = (JTextField) editorComponent;
            init();
        }

        public CurrencyEditor(int min, int max) {
            super(new JTextField());
            this.tf = (JTextField) editorComponent;
            nf.setMinimumFractionDigits(min);
            nf.setMaximumFractionDigits(max);
            init();
        }

        public CurrencyEditor(final JTextField tf) {
            super(tf);
            this.tf = tf;
            init();
        }

        public CurrencyEditor(final JTextField tf, NumberFormat nf) {
            super(tf);
            this.tf = tf;
            this.nf = nf;
            init();
        }

        private void init() {
            tf.setHorizontalAlignment(SwingConstants.RIGHT);
            tf.setBorder(null);

            tf.setDocument(new PlainDocument() {
                @Override
                public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                    if (str.equals(".")) {
                        str = ",";
                    }
                    super.insertString(offs, str, a);
                }
            });

            delegate = new EditorDelegate() {
                public void setValue(Object param) {
                    Double _value = null;
                    if (param instanceof BigDecimal) {
                        _value = ((BigDecimal) param).doubleValue();
                    } else if (param instanceof Integer) {
                        _value = CastUtils.toDouble(param);
                    } else if (param instanceof Double) {
                        _value = (Double) param;
                    } else {
                        _value = CastUtils.toDouble(param);
                    }
                    if (_value == null) {
                        tf.setText("");
                    } else {
                        tf.setText(nf.format(_value));
                    }
                }

                public Object getCellEditorValue() {
                    try {
                        return ((Number) nf.parse(tf.getText())).doubleValue();
                    } catch (ParseException ex) {
                        if (returnNull) {
                            return CastUtils.toDouble(tf.getText());
                        } else {
                            return CastUtils.toDouble0(tf.getText());
                        }
                    }
                }
            };
        }

        public void setBorder(Border border) {
            editorComponent.setBorder(border);
        }
    }

    static public class ComboEditor extends AbstractCellEditor
            implements TableCellEditor, TreeCellEditor {

        protected JComponent editorComponent;
        JComboBox comboBox = null;
        protected EditorDelegate delegate;
        protected int clickCountToStart = 1;
        int lastkey = 0;

        public ComboEditor(final JComboBox comboBox) {
            editorComponent = comboBox;
            this.comboBox = comboBox;
            comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

            comboBox.getEditor().getEditorComponent().addKeyListener(new KeyListener() {
                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                    System.out.println(e);
                    ComboEditor.this.lastkey = e.getKeyCode();
                }

                public void keyReleased(KeyEvent e) {
                }
            });
            delegate = new EditorDelegate() {
                public void setValue(Object value) {
                    comboBox.setSelectedItem(value);
                }

                public Object getCellEditorValue() {
                    return comboBox.getSelectedItem();
                }

                public boolean shouldSelectCell(EventObject anEvent) {
                    if (anEvent instanceof MouseEvent) {
                        MouseEvent e = (MouseEvent) anEvent;
                        return e.getID() != MouseEvent.MOUSE_DRAGGED;
                    }
                    return true;
                }

                public boolean stopCellEditing() {
                    //controllare se viene da keyboard tab o invio

//                    if (comboBox.isEditable()) {
//                        // Commit edited value.
//                        comboBox.actionPerformed(new ActionEvent(ComboEditor.this, 0, ""));
//                    }
//                    return super.stopCellEditing();
                    System.out.println("stopcellediting");
                    return true;
                }
            };
            comboBox.addActionListener(delegate);
        }

        public Object getCellEditorValue() {
            return comboBox.getSelectedItem();
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            delegate.setValue(value);
            return editorComponent;
        }

        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            String stringValue = tree.convertValueToText(value, isSelected,
                    expanded, leaf, row, false);
            delegate.setValue(stringValue);
            return editorComponent;
        }

        protected class EditorDelegate implements ActionListener, ItemListener, Serializable {

            /**
             * The value of this cell.
             */
            protected Object value;

            /**
             * Returns the value of this cell.
             *
             * @return the value of this cell
             */
            public Object getCellEditorValue() {
                return value;
            }

            /**
             * Sets the value of this cell.
             *
             * @param value the new value of this cell
             */
            public void setValue(Object value) {
                this.value = value;
            }

            /**
             * Returns true if <code>anEvent</code> is <b>not</b> a
             * <code>MouseEvent</code>. Otherwise, it returns true if the
             * necessary number of clicks have occurred, and returns false
             * otherwise.
             *
             * @param anEvent the event
             * @return true if cell is ready for editing, false otherwise
             * @see #setClickCountToStart
             * @see #shouldSelectCell
             */
            public boolean isCellEditable(EventObject anEvent) {
                if (anEvent instanceof MouseEvent) {
                    return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
                }
                return true;
            }

            /**
             * Returns true to indicate that the editing cell may be selected.
             *
             * @param anEvent the event
             * @return true
             * @see #isCellEditable
             */
            public boolean shouldSelectCell(EventObject anEvent) {
                return true;
            }

            /**
             * Returns true to indicate that editing has begun.
             *
             * @param anEvent the event
             */
            public boolean startCellEditing(EventObject anEvent) {
                return true;
            }

            /**
             * Stops editing and returns true to indicate that editing has
             * stopped. This method calls <code>fireEditingStopped</code>.
             *
             * @return true
             */
            public boolean stopCellEditing() {
                System.out.println("stopcellediting2");
                fireEditingStopped();
                return true;
            }

            /**
             * Cancels editing. This method calls
             * <code>fireEditingCanceled</code>.
             */
            public void cancelCellEditing() {
                fireEditingCanceled();
            }

            /**
             * When an action is performed, editing is ended.
             *
             * @param e the action event
             * @see #stopCellEditing
             */
            public void actionPerformed(ActionEvent e) {
                System.out.println("lastkey:" + ComboEditor.this.lastkey);
                if (ComboEditor.this.lastkey == 10) {
                    ComboEditor.this.stopCellEditing();
                }
            }

            /**
             * When an item's state changes, editing is ended.
             *
             * @param e the action event
             * @see #stopCellEditing
             */
            public void itemStateChanged(ItemEvent e) {
                ComboEditor.this.stopCellEditing();
            }
        }
    }

    static public class NumberEditor extends DefaultCellEditor {

        JTextField tf = null;
        NumberFormat nf = NumberFormat.getNumberInstance();
        public boolean returnNull = false;

        public NumberEditor() {
            super(new JTextField());
            this.tf = (JTextField) editorComponent;
            init();
        }

        public NumberEditor(final JTextField tf) {
            super(tf);
            this.tf = tf;
            init();
        }

        public NumberEditor(final JTextField tf, NumberFormat nf) {
            super(tf);
            this.tf = tf;
            this.nf = nf;
            init();
        }

        private void init() {
            tf.setHorizontalAlignment(SwingConstants.RIGHT);
            tf.setBorder(null);

            tf.setDocument(new PlainDocument() {
                @Override
                public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                    if (str.equals(".")) {
                        str = ",";
                    }
                    super.insertString(offs, str, a);
                }
            });

            delegate = new EditorDelegate() {
                public void setValue(Object param) {
                    Double _value = null;
                    if (param instanceof BigDecimal) {
                        _value = ((BigDecimal) param).doubleValue();
                    } else if (param instanceof Integer) {
                        _value = CastUtils.toDouble(param);
                    } else if (param instanceof Double) {
                        _value = (Double) param;
                    } else {
                        _value = CastUtils.toDouble(param);
                    }
                    if (_value == null) {
                        tf.setText("");
                    } else {
                        tf.setText(nf.format(_value));
                    }
                }

                public Object getCellEditorValue() {
                    if (returnNull) {
                        return cu.toDoubleAll(tf.getText());
                    } else {
                        return cu.toDouble0All(tf.getText());
                    }
                }
            };
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return super.getTableCellEditorComponent(table, value, isSelected, row, column); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object getCellEditorValue() {
            return super.getCellEditorValue(); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public static class EliminaEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

        int current;
        JButton button;
        JLabel label = new JLabel("");
        JDialog dialog;
        JTable table;
        protected static final String ELIMINA = "elimina";

        ImageIcon icona = new ImageIcon(getClass().getResource("/it/tnx/commons/res/Delete Sign-16.png"));

        public EliminaEditor() {
            button = new JButton();
            button.setText("");
            button.setMargin(new Insets(1, 1, 1, 1));
            button.setHorizontalAlignment(JButton.CENTER);
            button.setHorizontalTextPosition(JButton.CENTER);
            button.setActionCommand(ELIMINA);
            button.addActionListener(this);
//                button.setBorderPainted(false);
//                button.setOpaque(false);
        }

        public void actionPerformed(ActionEvent e) {
            if (ELIMINA.equals(e.getActionCommand())) {
                
                int row = table.getSelectedRow();
                System.out.println("eliminare:" + row);

                if (table.getModel() instanceof DefaultTableModel) {
                    DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                    tableModel.removeRow(row);
                } else if (table.getModel() instanceof ListMapTableModel) {
                    ListMapTableModel tableModel = (ListMapTableModel)table.getModel();
                    tableModel.removeRow(row);
                }
                
                fireEditingStopped();

            } else {
                current = 0;
            }
        }

        public Object getCellEditorValue() {
            return new Integer(current);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            current = 0;
            this.table = table;
            //controlla che ci sia un valore nella colonna 1
            Object val = table.getValueAt(row, 1);
            if (val != null) {
                button.setIcon(icona);
                return button;
            }
            return label;
        }


    }

}
