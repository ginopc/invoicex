/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.tnx.commons;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author test1
 */
public class DateUtils {
    
    public static final long MILLISECS_PER_MINUTE = 60*1000;
    public static final long MILLISECS_PER_HOUR   = 60*MILLISECS_PER_MINUTE;
    protected static final long MILLISECS_PER_DAY = 24*MILLISECS_PER_HOUR;
    
    static public int getCurrentYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }
    
    static public int getYear(Date data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        return cal.get(Calendar.YEAR);
    }

    static public Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        return cal.getTime();
    }
    
    static public String formatDateTimeFromMillis(Long millis) {
        if (millis == null) return "";
        DateFormat f1 = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return f1.format(new Date(millis));
    }
    
    static public String formatDateTime(Date date) {
        if (date == null) return "";
        DateFormat f1 = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return f1.format(date);
    }
    
    static public String formatDateTime(Long millis) {
        if (millis == null) return "";
        return formatDateTimeFromMillis(millis);
    }

    static public String formatTime(Date date) {
        if (date == null) return "";
        DateFormat f1 = new SimpleDateFormat("HH:mm");
        return f1.format(date);
    }

    static public String formatDate(Date date) {
        if (date == null) return "";
        DateFormat f1 = DateFormat.getDateInstance(DateFormat.SHORT);
        return f1.format(date);
    }

    static public String formatDateIta(Date date) {
        if (date == null) return "";
        DateFormat f1 = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALIAN);
        return f1.format(date);
    }

    static public boolean dateInRange(Date date, Date start, Date end) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
        
        end = cal.getTime();
        
        if ((start.before(date) || start.equals(date)) 
                && (end.after(date) || end.equals(date))) {
              return true;
        }
        return false;
    }
    
    static public int getDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }
    
    static public int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    static public Date getOnlyDate(int year, int month, int day) {
        return getOnlyDate(getDate(year, month, day));
    }

    static public Date getOnlyDate(Date value) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(value);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    static public long daysDifference(Date start, Date end) {
        Calendar cals = Calendar.getInstance();
        cals.setTime(start);
        cals.set(Calendar.HOUR_OF_DAY, 0);
        cals.set(Calendar.MINUTE, 0);
        cals.set(Calendar.SECOND, 0);
        cals.set(Calendar.MILLISECOND, 0);
        
        Calendar cale = Calendar.getInstance();
        cale.setTime(end);
        cale.set(Calendar.HOUR_OF_DAY, 0);
        cale.set(Calendar.MINUTE, 0);
        cale.set(Calendar.SECOND, 0);
        cale.set(Calendar.MILLISECOND, 0);
        
        long endL = cale.getTimeInMillis() + cale.getTimeZone().getOffset( cale.getTimeInMillis() );
        long startL = cals.getTimeInMillis() + cals.getTimeZone().getOffset( cals.getTimeInMillis() );
        return (endL - startL) / MILLISECS_PER_DAY;
    }

    static public String getDateStartYear() {
        return formatDate(getDate(getCurrentYear(), 1, 1));
    }


    public static boolean isDate(Object val) {
        if (val == null) {
            return false;
        }
        if (val instanceof String) {
            DateFormat f = DateFormat.getDateInstance(DateFormat.SHORT);
            try {
                f.parse((String) val);
                return true;
            } catch (ParseException e1) {
                return false;
            }
        } else {
            if (val instanceof Date) {
                return true;
            }
        }
        return false;
    }

    public static Date getCurDate(int calendar_field, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.add(calendar_field, amount);
        return cal.getTime();
    }
}
