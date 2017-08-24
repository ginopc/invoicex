/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author mceccarelli
 */
public class CastUtils {

    public static boolean toBoolean(Object val) {
        if (val == null) {
            return false;
        }
        if (val instanceof Boolean) {
            return ((Boolean)val).booleanValue();
        } else if (val instanceof Number) {
            int ival = ((Number) val).intValue();
            if (ival == 1) {
                return true;
            }
            if (ival == -1) {
                return true;
            }
        } else {
            if (val.toString().equalsIgnoreCase("true")) {
                return true;
            }            
            if (val.toString().equals("S")) {
                return true;
            }
            if (val.toString().equals("1")) {
                return true;
            }
            if (val.toString().equals("-1")) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static Integer toInteger(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            try {
                String s = (String) val;
                if (s.indexOf(".") >= 0 && s.indexOf(",") >= 0) {
                    if (s.indexOf(".") > s.indexOf(",")) {
                        s = s.replace(",", "");
                        s = s.substring(0, s.indexOf("."));
                    } else {
                        s = s.replace(".", "");
                        s = s.substring(0, s.indexOf(","));
                    }
                } else if (s.indexOf(".") >= 0) {
                    s = s.substring(0, s.indexOf("."));
                } else if (s.indexOf(",") >= 0) {
                    s = s.substring(0, s.indexOf(","));
                }
                return Integer.parseInt((String) s);
            } catch (Exception e) {
                return null;
            }
        } else if (val instanceof Number) {
            try {
                return ((Number) val).intValue();
            } catch (Exception e) {
                return null;
            }
        } else {
            try {
                return (Integer) val;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static Integer toInteger0(Object val) {
        Integer ret = 0;
        if(val instanceof Boolean){
            Boolean valore = (Boolean) val;
            ret = 0;
            if(valore) ret = 1;
        } else {
            ret = toInteger(val);
            if (ret == null) {
                return 0;
            }
        }
        
        return ret;
    }

    public static Long toLong(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            try {
                String s = (String) val;
                if (s.indexOf(".") >= 0 && s.indexOf(",") >= 0) {
                    if (s.indexOf(".") > s.indexOf(",")) {
                        s = s.replace(",", "");
                        s = s.substring(0, s.indexOf("."));
                    } else {
                        s = s.replace(".", "");
                        s = s.substring(0, s.indexOf(","));
                    }
                } else if (s.indexOf(".") >= 0) {
                    s = s.substring(0, s.indexOf("."));
                } else if (s.indexOf(",") >= 0) {
                    s = s.substring(0, s.indexOf(","));
                }
                return Long.parseLong((String) s);
            } catch (Exception e) {
                return null;
            }
        } else if (val instanceof Number) {
            try {
                return ((Number) val).longValue();
            } catch (Exception e) {
                return null;
            }
        } else {
            try {
                return (Long) val;
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    public static Long toLong0(Object val) {
        Long ret = 0l;
        if(val instanceof Boolean){
            Boolean valore = (Boolean) val;
            ret = 0l;
            if(valore) ret = 1l;
        } else {
            ret = toLong(val);
            if (ret == null) {
                return 0l;
            }
        }
        
        return ret;
    }
    


    public static Double toDouble(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            String sval = (String) val;
            NumberFormat f = null;
            f = DecimalFormat.getInstance();
            if (sval.indexOf(".") >= 0 && sval.indexOf(",") >= 0) {
                if (sval.lastIndexOf(".") > sval.lastIndexOf(",")) {
                    f = DecimalFormat.getInstance(Locale.ENGLISH);
                }
            }
            try {
                return (f.parse((String) val)).doubleValue();
            } catch (ParseException e1) {
                try {
                    return Double.parseDouble((String) val);
                } catch (Exception e2) {
                    return null;
                }
            }
        } else if (val instanceof BigDecimal) {
            try {
                return ((BigDecimal) val).doubleValue();
            } catch (Exception e) {
                return null;
            }
        } else if (val instanceof Integer) {
            try {
                return ((Integer) val).doubleValue();
            } catch (Exception e) {
                return null;
            }
        } else if (val instanceof Long) {
            try {
                return ((Long) val).doubleValue();
            } catch (Exception e) {
                return null;
            }            
        } else {
            try {
                return (Double) val;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static Double toDoubleAll(Object val) {
        //converte un numero in double, che ci sia il . o la ,
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            String sval = (String) val;
            NumberFormat f = null;
            f = DecimalFormat.getInstance();
            if (sval.indexOf(".") >= 0 && sval.indexOf(",") >= 0) {
                if (sval.lastIndexOf(".") > sval.lastIndexOf(",")) {
                    f = DecimalFormat.getInstance(Locale.ENGLISH);
                }
            } else if (sval.indexOf(".") >= 0) {
                f = DecimalFormat.getInstance(Locale.ENGLISH);
            } else if (sval.indexOf(",") >= 0) {
                f = DecimalFormat.getInstance(Locale.ITALIAN);
            }
            try {
                return (f.parse((String) val)).doubleValue();
            } catch (ParseException e1) {
                try {
                    return Double.parseDouble((String) val);
                } catch (Exception e2) {
                    return null;
                }
            }
        } else if (val instanceof BigDecimal) {
            try {
                return ((BigDecimal) val).doubleValue();
            } catch (Exception e) {
                return null;
            }
        } else if (val instanceof Integer) {
            try {
                return ((Integer) val).doubleValue();
            } catch (Exception e) {
                return null;
            }
        } else if (val instanceof Long) {
            try {
                return ((Long) val).doubleValue();
            } catch (Exception e) {
                return null;
            }            
        } else {
            try {
                return (Double) val;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static Double toDoubleEng(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            String sval = (String) val;
            NumberFormat f = null;
            f = DecimalFormat.getInstance(Locale.ENGLISH);
            try {
                return (f.parse((String) val)).doubleValue();
            } catch (ParseException e1) {
                try {
                    return Double.parseDouble((String) val);
                } catch (Exception e2) {
                    return 0d;
                }
            }
        } else if (val instanceof BigDecimal) {
            try {
                return ((BigDecimal) val).doubleValue();
            } catch (Exception e) {
                return null;
            }
        } else if (val instanceof Integer) {
            try {
                return ((Integer) val).doubleValue();
            } catch (Exception e) {
                return null;
            }
        } else if (val instanceof Long) {
            try {
                return ((Long) val).doubleValue();
            } catch (Exception e) {
                return null;
            }            
        } else {
            try {
                return (Double) val;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static Double toDouble0(Object val) {
        Double ret = toDouble(val);
        if (ret == null) {
            return 0d;
        }
        return ret;
    }

    public static Double toDouble0All(Object val) {
        Double ret = toDoubleAll(val);
        if (ret == null) {
            return 0d;
        }
        return ret;
    }

    public static Double toDouble0Eng(Object val) {
        Double ret = toDoubleEng(val);
        if (ret == null) {
            return 0d;
        }
        return ret;
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return "";
        }
        return String.valueOf(obj);
    }

    public static Date toDate(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof byte[]) {
            //da mysql
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return (f.parse(new String((byte[])val)));
            } catch (ParseException e1) {
                return null;
            }            
        } else if (val instanceof String) {
            String str = (String)val;
            DateFormat f = null;
            if (str.length() > 9 && str.substring(4,5).equals("-") && str.substring(7,8).equals("-")) {
                //formato mysql
                f = new SimpleDateFormat("yyyy-MM-dd");
            } else {
                f = DateFormat.getDateInstance(DateFormat.SHORT);
            }
            try {
                return (f.parse((String) val));
            } catch (ParseException e1) {
                return null;
            }
        } else {
            try {
                return (Date) val;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static Date toDateIta(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            DateFormat f = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALIAN);
            try {
                return (f.parse((String) val));
            } catch (ParseException e1) {
                return null;
            }
        } else {
            try {
                return (Date) val;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static String getOnlyNumerics(String str) {
        if (str == null) {
            return null;
        }

        StringBuffer strBuff = new StringBuffer();
        char c;

        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);

            if (Character.isDigit(c)) {
                strBuff.append(c);
            }
        }
        return strBuff.toString();
    }

    public static void main(String[] args) {
        System.out.println(toInteger0("123"));
        System.out.println(toInteger0("123.12"));
        System.out.println(toInteger0("123,12"));
        System.out.println(toInteger0("1.023,12"));
        System.out.println(toInteger0("1,023.12"));
        System.out.println(toInteger0("1,023"));
        System.out.println(toInteger0("1.023"));
    }
}
