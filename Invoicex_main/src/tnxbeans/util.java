/**
 * Invoicex
 * Copyright (c) 2005-2016 Marco Ceccarelli, Tnx srl
 *
 * Questo software è soggetto, e deve essere distribuito con la licenza
 * GNU General Public License, Version 2. La licenza accompagna il software
 * o potete trovarne una copia alla Free Software Foundation http://www.fsf.org .
 *
 * This software is subject to, and may be distributed under, the
 * GNU General Public License, Version 2. The license should have
 * accompanied the software or you may obtain a copy of the license
 * from the Free Software Foundation at http://www.fsf.org .
 *
 * --
 * Marco Ceccarelli (m.ceccarelli@tnx.it)
 * Tnx snc (http://www.tnx.it)
 *
 */
package tnxbeans;

import java.util.*;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

public abstract class util {

    public util() {
    }

    public static String getDataFromItaToMysql(String data) {
        String giorno = "";
        String mese = "";
        String anno = "";
        Vector temp = splitString(data, "/");
        if (temp.size() < 3) {
            return ("");
        } else {
            giorno = String.valueOf(temp.get(0));
            mese = String.valueOf(temp.get(1));
            anno = String.valueOf(temp.get(2));
            return (anno + "-" + mese + "-" + giorno);
        }
    }

    public static Vector splitString(String stringa, String conCosa) {
        Vector temp = new Vector();
        StringTokenizer s = new StringTokenizer(stringa, conCosa);
        while (s.hasMoreTokens()) {
            temp.add(s.nextToken());
        }
        return (temp);
    }
    
    static public void checkModificati(JComponent comp) {
        if (comp instanceof BasicField && ((BasicField)comp).getParentPanel() != null) ((BasicField)comp).getParentPanel().dbCheckModificati();
        if (comp.getParent() instanceof tnxDbPanel) ((tnxDbPanel)comp.getParent()).dbCheckModificati();
        if (comp.getParent() instanceof tnxDbPanel && ((tnxDbPanel)comp.getParent()).getParentPanel() != null) ((tnxDbPanel)comp.getParent()).getParentPanel().dbCheckModificati();
    }
    
    static public void ultimoCampo(JComponent comp) {
        try {
            BasicField bf = (BasicField)comp;
            JTextComponent tc = null;
            if (comp instanceof JTextComponent) {
                tc = (JTextComponent)comp;
            } else if (comp instanceof tnxMemoField) {
                tc = ((tnxMemoField)comp).getJTextArea();
            } else {
                return;
            }
            if (comp instanceof BasicField && ((BasicField)comp).getParentPanel() != null) settaUltimoCampo(bf.getParentPanel(), bf.getDbNomeCampo(), tc.getText());
            if (comp.getParent() instanceof tnxDbPanel) settaUltimoCampo((tnxDbPanel)comp.getParent(), bf.getDbNomeCampo(), tc.getText());
            if (comp.getParent() instanceof tnxDbPanel && ((tnxDbPanel)comp.getParent()).getParentPanel() != null) settaUltimoCampo(((tnxDbPanel)comp.getParent()).getParentPanel(), bf.getDbNomeCampo(), tc.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void settaUltimoCampo(tnxDbPanel parentPanel, String dbNomeCampo, String text) {
        parentPanel.ultimoCampo = dbNomeCampo;
        parentPanel.ultimoValore = text;
    }

}
