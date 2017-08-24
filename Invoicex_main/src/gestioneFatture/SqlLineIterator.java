/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gestioneFatture;

import it.tnx.commons.DebugUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author mceccarelli
 */
public class SqlLineIterator {

    InputStream is = null;
    byte[] buffer = new byte[100000];
    int readed = 0;
    int pos = 0;
    byte b = 0;
    byte oldb = 0;
    byte oldoldb = 0;
    boolean next = true;
    boolean eof = false;
    public long bytes_processed = 0;
    public boolean utf8 = false;

    public SqlLineIterator(InputStream is) throws IOException {
        this.is = is;
        readed = is.read(buffer);

        if (main.getPersonalContain("restore-utf8")) {
            utf8 = true;
        }
    }

    public boolean hasNext() {
        return next;
    }

    public String nextLine() throws IOException {
//        StringBuilder sb = new StringBuilder();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        boolean reading = true;
        boolean stringaincorso = false;
        while (reading) {
            oldoldb = oldb;
            oldb = b;
            b = getB();
            switch (b) {
                case '\n':
                    if (!stringaincorso) {
                        if (oldb == ';') {
                            reading = false;
                        } else if (oldb == '\r' && oldoldb == ';') {
                            //ignoro
                        }
                        break;
                    } else {
                        bout.write(b);
                    }
                case '\r':
                    if (!stringaincorso) {
                        if (oldb == ';' && !stringaincorso) {
                            reading = false;
                        }
                        break;
                    } else {
                        bout.write(b);
                    }
                case -1:    //fine file
                    if (eof) {
                        reading = false;
                        next = false;
                    } else {
//                        sb.append((char)b);
                        bout.write(b);
                    }
                    break;
                default:
//                    sb.append((char)b);
                    if (b == '\'' && oldb != '\\' && !stringaincorso) {
                        stringaincorso = true;
                    } else if (b == '\'' && (oldb != '\\' || (oldb == '\\' && oldoldb == '\\')) && stringaincorso) {
                        stringaincorso = false;
                    }                    
                    bout.write(b);
                    break;
            }

//            if (pos % 50000 == 0) DebugUtils.dumpMem();
//            if (sb.toString().startsWith("insert into righ_ddt_acquisto values ('',(222),(2012),(4),'UAMCP6021_")) {
//                System.out.println("rrr");
//            }

            pos++;
        }

//        return sb.toString();
//        return new String(bout.toByteArray());
        
        String test = "";
        if (utf8) {
            test = new String(bout.toByteArray(), "UTF8");
        } else {
            test = new String(bout.toByteArray(), "ISO-8859-1");
        }
        if (test.indexOf("righ_ddt set `serie` = '',`numero` = (35),`anno` = (2014),`riga` = (2)") >= 0) {
            System.out.println("test");
        }
        
        if (utf8) {
            return new String(bout.toByteArray(), "UTF8");
        } else {
            return new String(bout.toByteArray(), "ISO-8859-1");
        }

    }

    private byte getB() throws IOException {
        bytes_processed++;
        if (pos < readed) {
            return buffer[pos];
        } else {
            pos = 0;
            readed = is.read(buffer);
            if (readed == -1) {
                eof = true;
                return -1;
            } else {
                return buffer[pos];
            }
        }
    }
}
