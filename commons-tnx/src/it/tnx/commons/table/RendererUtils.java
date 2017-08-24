/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons.table;

import it.tnx.commons.CastUtils;
import java.awt.Color;
import java.awt.Component;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author test1
 */
public class RendererUtils {

    static public class DateRenderer extends DefaultTableCellRenderer {

        DateFormat formatter;

        public DateRenderer() {
            this.formatter = DateFormat.getDateInstance();
        }

        public DateRenderer(DateFormat formatter) {
            this.formatter = formatter;
        }

        @Override
        protected void setValue(Object value) {
            try {
                if (value instanceof Date) {
                    setText((value == null) ? "" : formatter.format(value));
                } else {
                    setText((value == null) ? "" : String.valueOf(value));
                }
            } catch (Exception e) {
                System.out.println(e.toString() + " value:" + value);
                e.printStackTrace();
            }
        }
    }

    static public class CurrencyRenderer extends DefaultTableCellRenderer {

        NumberFormat formatter = NumberFormat.getCurrencyInstance();

        public CurrencyRenderer() {
            init();
        }

        public CurrencyRenderer(NumberFormat formatter) {
            this.formatter = formatter;
            init();
        }

        public CurrencyRenderer(int mindec, int maxdec) {
            formatter.setMinimumFractionDigits(mindec);
            formatter.setMaximumFractionDigits(maxdec);
            init();
        }

        @Override
        protected void setValue(Object value) {
//            if (value != null) {
//                System.out.println("value: " + value + " class:" + value.getClass().getName());
//            }
            if (value instanceof String && CastUtils.toString(value).equals("")) {
                setText("");
                return;
            }
            setText((value == null) ? "" : formatter.format(CastUtils.toDouble0All(value)));
        }

        private void init() {
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(null);
        }
    }

    static public class NumberRenderer extends DefaultTableCellRenderer {

        NumberFormat formatter = NumberFormat.getNumberInstance();

        public NumberRenderer() {
            init();
        }

        public NumberRenderer(NumberFormat formatter) {
            this.formatter = formatter;
            init();
        }

        public NumberRenderer(int mindec, int maxdec) {
            formatter.setMinimumFractionDigits(mindec);
            formatter.setMaximumFractionDigits(maxdec);
            init();
        }

        @Override
        protected void setValue(Object value) {
            setText((value == null) ? "" : formatter.format(CastUtils.toDouble0All(value)));
        }

        private void init() {
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(null);
        }
    }

    public static class EliminaRenderer extends DefaultTableCellRenderer {

        ImageIcon icona = new ImageIcon(getClass().getResource("/it/tnx/commons/res/Delete Sign-16.png"));

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lab = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lab.setText("");
            Object val = table.getValueAt(row, 1);
            if (val != null) {
                lab.setHorizontalAlignment(JLabel.CENTER);
                lab.setHorizontalTextPosition(JLabel.CENTER);
                lab.setIcon(icona);
                lab.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.lightGray));
                return lab;
            } else {
                lab.setIcon(null);
                lab.setBorder(null);
                return lab;
            }
        }
    }

}
