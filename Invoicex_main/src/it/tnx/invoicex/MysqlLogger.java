/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Marco
 */
public class MysqlLogger implements com.mysql.jdbc.log.Log {
    private static final Logger logger = LogManager.getLogger(MysqlLogger.class);
    private String instance = null;
    
    public MysqlLogger() {
    }

    public MysqlLogger(String instance) {
        System.out.println("MysqlLogger instance = " + instance);
        this.instance = instance;
    }
    
    public boolean isDebugEnabled() {
        return false;
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public boolean isFatalEnabled() {
        return true;
    }

    public boolean isInfoEnabled() {
        return false;
    }

    public boolean isTraceEnabled() {
        return false;
    }

    public boolean isWarnEnabled() {
        return false;
    }

    public void logDebug(Object o) {
        logger.debug(o);
    }

    public void logDebug(Object o, Throwable thrwbl) {
        logger.debug(o, thrwbl);
    }

    public void logError(Object o) {
        logger.error(o);
    }

    public void logError(Object o, Throwable thrwbl) {
        logger.error(o, thrwbl);
    }

    public void logFatal(Object o) {
        logger.fatal(o);
    }

    public void logFatal(Object o, Throwable thrwbl) {
        logger.fatal(o, thrwbl);
    }

    public void logInfo(Object o) {
        logger.info(o);
    }

    public void logInfo(Object o, Throwable thrwbl) {
        logger.info(o, thrwbl);
    }

    public void logTrace(Object o) {
        logger.debug(o);
    }

    public void logTrace(Object o, Throwable thrwbl) {
        logger.debug(o, thrwbl);
    }

    public void logWarn(Object o) {
        logger.warn(o);
    }

    public void logWarn(Object o, Throwable thrwbl) {
        logger.warn(o, thrwbl);
    }
    
}
