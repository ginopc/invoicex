/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons.swing;

import java.awt.Toolkit;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author Marco
 */
public class LimitedUCasePlainDocument extends PlainDocument {

    JTextField text;
    int maxC;

    public LimitedUCasePlainDocument(JTextField text, int max) {
        this.text = text;
        this.maxC = max;
    }

    public void insertString(int arg0, String arg1, AttributeSet arg2) throws BadLocationException {
        if ((text.getText().length() + arg1.length()) > this.maxC) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        super.insertString(arg0, arg1.toUpperCase(), arg2);
    }
    
}
