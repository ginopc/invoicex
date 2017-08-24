/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author test1
 */
public class VerySimpleFormatter extends Formatter {

    private static final DateFormat format = new SimpleDateFormat("dd/MM/yy h:mm:ss");
    private static final String lineSep = System.getProperty("line.separator");

    public String format(LogRecord record) {
        String loggerName = record.getLoggerName();
        if (loggerName == null) {
            loggerName = "root";
        }
        StringBuilder output = new StringBuilder().append(loggerName).append("[").append(record.getLevel()).append('|').append(Thread.currentThread().getName()).append('|').append(format.format(new Date(record.getMillis()))).append("]: ").append(record.getMessage()).append(' ').append(lineSep);
        return output.toString();
    }
}
