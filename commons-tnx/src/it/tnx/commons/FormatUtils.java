/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author test2
 */
public class FormatUtils {

    public static NumberFormat nf1;
    public static NumberFormat nf2;
    public static NumberFormat nf3;
    public static NumberFormat nf4;
    public static NumberFormat nf04;
    public static NumberFormat nf5;
    public static NumberFormat nf1d;
    public static NumberFormat nfeng22;
    public static NumberFormat nfeng25;
    public static NumberFormat nfeng02;
    public static NumberFormat nfeng05;
    public static NumberFormat nfnogroupnodec;

    public static SimpleDateFormat mysqlf1 = new SimpleDateFormat("yyyy-MM-dd");
//    public static SimpleDateFormat mysqlf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S");
    public static SimpleDateFormat mysqlf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static SimpleDateFormat accf1 = new SimpleDateFormat("MM/dd/yyyy");
//    public static SimpleDateFormat accf2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static SimpleDateFormat accf2 = new SimpleDateFormat("MM/dd/yyyy");

    static {
        nf1 = NumberFormat.getNumberInstance(Locale.ITALIAN);
        nf1.setMaximumFractionDigits(2);
        nf1.setMinimumFractionDigits(2);

        nf2 = new DecimalFormat("0.##");

        nf3 = NumberFormat.getNumberInstance(Locale.ITALIAN);
        nf3.setMaximumFractionDigits(5);
        nf3.setMinimumFractionDigits(2);

        nf4 = NumberFormat.getNumberInstance(Locale.ITALIAN);
        nf4.setMaximumFractionDigits(0);

        nf04 = NumberFormat.getNumberInstance(Locale.ITALIAN);
        nf04.setMinimumFractionDigits(0);
        nf04.setMaximumFractionDigits(4);

        nf5 = NumberFormat.getNumberInstance(Locale.ITALIAN);
        nf5.setMinimumFractionDigits(0);
        nf5.setMaximumFractionDigits(5);

        nfeng22 = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nfeng22.setMinimumFractionDigits(2);
        nfeng22.setMaximumFractionDigits(2);
        nfeng22.setGroupingUsed(false);

        nfeng02 = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nfeng02.setMinimumFractionDigits(0);
        nfeng02.setMaximumFractionDigits(2);
        nfeng02.setGroupingUsed(false);

        nfeng25 = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nfeng25.setMinimumFractionDigits(2);
        nfeng25.setMaximumFractionDigits(5);
        nfeng25.setGroupingUsed(false);

        nfeng05 = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nfeng05.setMinimumFractionDigits(0);
        nfeng05.setMaximumFractionDigits(5);
        nfeng05.setGroupingUsed(false);

        nf1d = NumberFormat.getNumberInstance(Locale.ITALIAN);
        nf1d.setMaximumFractionDigits(1);
        
        nfnogroupnodec = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nfnogroupnodec.setGroupingUsed(false);
        nfnogroupnodec.setMaximumFractionDigits(0);
        nfnogroupnodec.setMinimumFractionDigits(0);

    }

    static public String formatParametr(double value, int decimali, String virgola) {

        virgola = virgola.equals("") ? "," : virgola;
        NumberFormat nfp;
        if (virgola.equals(",")) {
            nfp = NumberFormat.getNumberInstance(Locale.ITALIAN);
            nfp.setMaximumFractionDigits(decimali);
            nfp.setMinimumFractionDigits(decimali);
        } else {
            nfp = NumberFormat.getNumberInstance();
            nfp.setMaximumFractionDigits(decimali);
            nfp.setMinimumFractionDigits(decimali);
        }

        value = round(value, decimali);
        String ret = nfp.format(value);

        if (virgola.equals(",")) {
            ret = ret.replace(".", "");
        } else {
            ret = ret.replace(",", "");
        }
        return ret;
    }

    static public String formatEuroIta(double value) {
        value = round(value, 2);
        return nf1.format(value);
    }

    static public String formatEuroItaMax5(double value) {
        value = round(value, 5);
        return nf3.format(value);
    }

    static public String formatPerc(double value) {
        value = round(value, 2);
        return nf2.format(value);
    }

    static public String formatPerc(Object value, boolean seNullVuoto) {
        if (seNullVuoto && value == null) {
            return "";
        }
        value = round(CastUtils.toDouble0(value), 2);
        return nf2.format(value);
    }

    static public String format1Dec(double value) {
        return nf1d.format(value);
    }

    static public String formatNumNoDec(double value) {
        return nf4.format(value);
    }

    static public String formatNum0_4Dec(double value) {
        return nf04.format(value);
    }

    static public String formatNum0_5Dec(double value) {
        return nf5.format(value);
    }

    static public String formatEngNum02Dec(double value) {
        return nfeng02.format(value);
    }

    static public String formatEngNum05Dec(double value) {
        return nfeng05.format(value);
    }

    static public String formatEngNum22Dec(double value) {
        return nfeng22.format(value);
    }

    static public String formatEngNum25Dec(double value) {
        return nfeng25.format(value);
    }

    static public String formatHHMMIta(long millis) {
        int ore = (int) (millis / 1000 / 60 / 60);
        int min = (int) (millis / 1000 / 60) - (ore * 60);
        return ore + ":" + (String.valueOf(min).length() == 1 ? "0" + min : min);
    }

    static public String formatHHMMSSIta(long millis) {
        String segno = "";
        if (millis < 0) {
            segno = "-";
            millis = Math.abs(millis);
        }
        int ore = Math.abs((int) (millis / 1000 / 60 / 60));
        int min = Math.abs((int) (millis / 1000 / 60) - (ore * 60));
        int ss = Math.abs((int) (millis / 1000) - (ore * 60 * 60) - (min * 60));
        return segno + ore + ":" + (String.valueOf(min).length() == 1 ? "0" + min : min) + ":" + (String.valueOf(ss).length() == 1 ? "0" + ss : ss);
    }

    static public String formatHHMMSSmsIta(long millis) {
        String segno = "";
        if (millis < 0) {
            segno = "-";
            millis = Math.abs(millis);
        }
        int ore = (int) (millis / 1000 / 60 / 60);
        int min = (int) (millis / 1000 / 60) - (ore * 60);
        int ss = (int) (millis / 1000) - (ore * 60 * 60) - (min * 60);
        int ms = (int) (millis) - (ore * 60 * 60 * 1000) - (min * 60 * 1000) - (ss * 1000);
        return segno + ore + ":" + (String.valueOf(min).length() == 1 ? "0" + min : min) + ":" + (String.valueOf(ss).length() == 1 ? "0" + ss : ss) + "." + StringUtils.left(String.valueOf(ms), 1);
    }

    static public String formatMysqlDate(Object date) {
        return mysqlf1.format(CastUtils.toDate(date));
    }

    static public String formatAccessDate(Object date) {
        return accf1.format(CastUtils.toDate(date));
    }

    static public String formatMysqlTimestamp(Date date) {
        return mysqlf2.format(date);
    }

    static public String formatMysqlTimestamp(Timestamp timestamp) {
        return mysqlf2.format(timestamp);
    }

    static public String formatAccessTimestamp(Date date) {
        return accf2.format(date);
    }

    static public String formatAccessTimestamp(Timestamp timestamp) {
        return accf2.format(timestamp);
    }

    public static double round(double x, int decimals) {  // rounds to the  nearest integer
        try {
            if (((Double) x).isNaN()) {
                x = 0d;
            }
            int decimali_aggiuntivi_affidabili = 4;//non devo mai raggiungere l'ultima cifra del float
            double numero_che_arrotondo = Math.round(x * Math.pow(10, decimals + decimali_aggiuntivi_affidabili));
            return Math.round(numero_che_arrotondo / Math.pow(10, decimali_aggiuntivi_affidabili)) / Math.pow(10, decimals);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public static double round_old(double x, int decimals) {  // rounds to the  nearest integer
        try {
            if (((Double) x).isNaN()) {
                x = 0d;
            }
            BigDecimal bd = new BigDecimal(String.valueOf(x));
            bd = bd.setScale(decimals, BigDecimal.ROUND_HALF_UP);
            return bd.doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }    

//    public static double round(double x, int decimals) {  // rounds to the  nearest integer
//        int factor = 1;
//        x = Double.parseDouble( String.format(Locale.ENGLISH, "%f", x) );
//        for (int i = 0; i < Math.abs(decimals); i++) factor *= 10;
//        if (decimals < 0) x = factor * Math.round(x / factor);
//        else x = (double)Math.round(factor * x) / (double)factor;
//        return x;
//    }
//
//    public static double round_old(double x, int decimals) {  // rounds to the  nearest integer
//        int factor = 1;
//        decimals += 1;
//        for (int i = 0; i < Math.abs(decimals); i++) factor *= 10;
//        if (decimals < 0) x = factor * Math.round(x / factor);
//        else x = (double)Math.round(factor * x) / (double)factor;
//
//        decimals -= 1;
//        factor = 1;
//        for (int i = 0; i < Math.abs(decimals); i++) factor *= 10;
//        if (decimals < 0) return factor * Math.round(x / factor);
//        else return (double)Math.round(factor * x) / (double)factor;
//    }
    public static String fill(String str, int chars) {
        if (str == null) {
            str = "";
        }
        try {
            if (str.length() > chars) {
                str = str.substring(0, chars);
            }
        } catch (Exception e) {
        }
        int strlen = str.length();
        int fillerlen = chars;
        if (strlen <= chars) {
            fillerlen = chars - strlen;
        }
        char[] filler = new char[fillerlen];
        Arrays.fill(filler, ' ');
        return str + new String(filler);
    }

    public static String zeroFill(Number number, int size) {
        return String.format("%0" + size + "d", number);
    }

    public static String zeroFill(String number, int size) {
        return String.format("%0" + size + "d", CastUtils.toInteger0(number));
    }

    public static String formatHHMMFromMinutes(int minutes) {
        float hours = ((float) minutes / 60f);
        int hoursi = (minutes / 60);
        float minf = hours - hoursi;
        return hoursi + ":" + zeroFill(Math.round(minf * 60f), 2);
    }
    
    public static String formatReadableHHMMFromMinutesIta(int minutes) {
        float hours = ((float) minutes / 60f);
        int hoursi = (minutes / 60);
        float minf = hours - hoursi;
        int mini = (int) Math.ceil(minf * 60f);
        if (hoursi == 0 && mini == 0) return "meno di un minuto";
        return (hoursi == 0 ? "" : (hoursi == 1 ? "1 ora e " : (hoursi + " ore e "))) + mini + " minut" + (mini == 1 ? "o" : "i");
    }    

    public static void main(String[] args) {
        //System.out.println(formatHHMMFromMinutes(126) );
        //System.out.println(formatHHMMSSmsIta(-(1000 * 60 * 6 + 1500)));
        System.out.println(formatEngNum22Dec(22.12345));
        System.out.println(formatEngNum22Dec(22.12545));
        System.out.println(formatEngNum22Dec(1022.12345));
        System.out.println(formatReadableByteCount(1095));
        
        System.out.println(formatReadableHHMMFromMinutesIta(61));
    }

    public static String formatReadableByteCount(long bytes) {
        return formatReadableByteCount(bytes, true);
    }    
    public static String formatReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    public static String formatNoGroupNoDec(Object val) {
        return nfnogroupnodec.format(val);
    }
}
