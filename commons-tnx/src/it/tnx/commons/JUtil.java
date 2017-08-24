/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EventListener;
import javax.swing.JOptionPane;


/**
 *
 * @author mceccarelli
 */
public class JUtil {

    static public boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null) {
            return false;
        }
        if (o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    static public boolean equalsToString(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null) {
            if (o2 instanceof Number) {
                o1 = "0";
            } else {
                o1 = "";
            }
        }
        if (o2 == null) {
            if (o1 instanceof Number) {
                o2 = "0";
            } else {
                o2 = "";
            }
        }
        return o1.toString().equalsIgnoreCase(o2.toString());
    }

    static public boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof String) {
            if (((String) o).trim().length() == 0) {
                return true;
            }
        } else if (String.valueOf(o).length() == 0) {
            return true;
        }
        return false;
    }

    static public String ControllaCF(String cf) {
        int i, s, c;
        String cf2;
        int setdisp[] = {1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20,
            11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23};
        if (cf.length() == 0) {
            return "";
        }
        if (cf.length() != 16) {
            return "La lunghezza del codice fiscale non e' corretta: il codice fiscale dovrebbe essere lungo 16 caratteri.";
        }
        cf2 = cf.toUpperCase();
        for (i = 0; i < 16; i++) {
            c = cf2.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'Z')) {
                return "Il codice fiscale contiene dei caratteri non validi: i soli caratteri validi sono le lettere e le cifre.";
            }
        }
        s = 0;
        for (i = 1; i <= 13; i += 2) {
            c = cf2.charAt(i);
            if (c >= '0' && c <= '9') {
                s = s + c - '0';
            } else {
                s = s + c - 'A';
            }
        }
        for (i = 0; i <= 14; i += 2) {
            c = cf2.charAt(i);
            if (c >= '0' && c <= '9') {
                c = c - '0' + 'A';
            }
            s = s + setdisp[c - 'A'];
        }
        if (s % 26 + 'A' != cf2.charAt(15)) {
            return "Il codice fiscale non e' corretto: il codice di controllo non corrisponde.";
        }
        return "";
    }

    static public String ControllaPIVA(String pi) {
        int i, c, s;
        if (pi.length() == 0) {
            return "";
        }
        if (pi.length() != 11) {
            return "La lunghezza della partita IVA non e' corretta: la partita IVA dovrebbe essere lunga 11 caratteri.";
        }
        for (i = 0; i < 11; i++) {
            if (pi.charAt(i) < '0' || pi.charAt(i) > '9') {
                return "La partita IVA contiene dei caratteri non ammessi: la partita IVA dovrebbe contenere solo cifre.";
            }
        }
        s = 0;
        for (i = 0; i <= 9; i += 2) {
            s += pi.charAt(i) - '0';
        }
        for (i = 1; i <= 9; i += 2) {
            c = 2 * (pi.charAt(i) - '0');
            if (c > 9) {
                c = c - 9;
            }
            s += c;
        }
        if ((10 - s % 10) % 10 != pi.charAt(10) - '0') {
            return "La partita IVA non e' valida: il codice di controllo non corrisponde.";
        }
        return "";
    }

    static public Object ifNull(Object test, Object ifNull) {
        if (test == null) {
            return ifNull;
        }
        return test;
    }
    
    public static boolean hasListenerClass(EventListener[] listeners, Class _class) {
        for (EventListener l : listeners) {
            if (l.getClass().equals(_class)) return true;
        }
        return false;
    }
    
    public static boolean isValidDateIta(String data) {
        try {
            SimpleDateFormat datef = null;
            if (data.length() == 8 || data.length() == 7) {
                datef = new SimpleDateFormat("dd/MM/yy");
            } else if (data.length() == 10 || data.length() == 9) {
                datef = new SimpleDateFormat("dd/MM/yyyy");
            } else {
                return false;
            }
            Calendar cal = Calendar.getInstance();
            datef.setLenient(true);
            cal.setTime(datef.parse(data));
        } catch (Exception err) {
            return false;
        }
        
        return true;
    }
}
