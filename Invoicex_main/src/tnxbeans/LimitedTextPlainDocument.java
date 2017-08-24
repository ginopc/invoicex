/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tnxbeans;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author mceccarelli
 */
public class LimitedTextPlainDocument extends PlainDocument {

    private int max;
    private boolean uppercase = false;

    public LimitedTextPlainDocument(int max) {
        super();
        this.max = max;
    }

    public LimitedTextPlainDocument(int max, boolean uppercase) {
        super();
        this.max = max;
        this.uppercase = uppercase;
    }

    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str != null && (getLength() + str.length()) <= max) {
            super.insertString(offset, uppercase ? str.toUpperCase() : str, attr);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
