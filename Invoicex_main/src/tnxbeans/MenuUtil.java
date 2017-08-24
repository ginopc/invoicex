/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tnxbeans;

import java.awt.Cursor;
import javax.swing.JPopupMenu;

/**
 *
 * @author Marco
 */
public class MenuUtil extends JPopupMenu {

    public MenuUtil() {
        javax.swing.JMenuItem menItem1;
        javax.swing.JMenuItem menItem2;
        menItem1 = this.add("Ricarica");
        menItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/Refresh-16.png")));
        //menItem2 = this.add("Vai a ->");

        menItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                action(evt);
            }
        });
    }

    private void action(java.awt.event.ActionEvent evt) {
        //System.out.println("refresh della combo");

        try {
            tnxComboField temp = (tnxComboField) this.getInvoker();
            //Connection connection,String sql, String valoreSelezionato, boolean usa_thread
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            temp.dbClearList();
            temp.dbOpenList(null, null, null, false);
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        } catch (Exception err) {
            err.printStackTrace();
        }
        
    }
}
