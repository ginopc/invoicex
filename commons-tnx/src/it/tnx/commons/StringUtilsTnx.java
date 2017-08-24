/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 *
 * @author mceccarelli
 */
public class StringUtilsTnx {

    static public boolean isNumber(String text) {
        return isNumber(text, Locale.ITALIAN);
    }

    static public boolean isNumber(String text, Locale locale) {
        NumberFormat f = DecimalFormat.getInstance(Locale.ITALIAN);
        try {
            f.parse(text);
        } catch (ParseException ex) {
            return false;
        }
        return true;
    }

    static public double parseDoubleOrZero(String text) {
        return parseDoubleOrZero(text, Locale.ITALIAN);
    }

    static public double parseDoubleOrZero(String text, Locale locale) {
        NumberFormat f = DecimalFormat.getInstance(Locale.ITALIAN);
        try {
            return f.parse(text).doubleValue();
        } catch (ParseException ex) {
            return 0d;
        }
    }

    static public String addslashes(String str) {
        return str.replaceAll("\\\\", "\\\\");
    }

    public static final String highlightWord(String string, String word, String startHighlight, String endHighlight) {
        if (string == null || word == null || startHighlight == null || endHighlight == null) {
            return null;
        }

        if (word.length() == 0) return string;

        String word_lc = word.toLowerCase();
        String string_lc = string.toLowerCase();
        StringBuffer sb = new StringBuffer();
        int ind = string_lc.indexOf(word_lc);
        if (ind >= 0) {
            sb.append(string.substring(0, ind));
            sb.append(startHighlight);
            sb.append(string.substring(ind, ind + word.length()));
            sb.append(endHighlight);
            sb.append(string.substring(ind + word.length()));
        }

        return sb.toString();

    }
}
