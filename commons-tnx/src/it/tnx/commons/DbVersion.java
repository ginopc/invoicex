/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Marco
 */
public class DbVersion {

    String modulo = null;
    Integer versione = null;
    Date ts = null;
    RunnableWithArgs showAttesa = null;

    public DbVersion(Connection conn, String modulo) throws Exception {
        this(conn, modulo, null);
    }
    public DbVersion(Connection conn, String modulo, RunnableWithArgs showAttesa) throws Exception {
        this.modulo = modulo;
        this.showAttesa = showAttesa;
        List<Map> ret = dbu.getListMap(conn, "select * from db_version where modulo = " + dbu.sql(modulo));
        if (ret.isEmpty()) {
            versione = 0;
            String sql = "insert into db_version set modulo = " + dbu.sql(modulo) + ", versione = 0";
            dbu.tryExecQuery(conn, sql);
        } else {
            versione = cu.i(ret.get(0).get("versione"));
            Object o = ret.get(0).get("ts");
            System.out.println("o = " + o);
        }
    }

    public Integer getVersion() {
        return versione;
    }

    public void setVersion(Connection conn, Integer versione) throws Exception {
        String sql = "update db_version set modulo = " + dbu.sql(modulo) + ", versione = " + dbu.sql(versione)
                + " where modulo = " + dbu.sql(modulo);
        dbu.tryExecQuery(conn, sql);
    }

    public Map running = null;

    public boolean prenotaChanges(Connection conn) {
        return prenotaChanges(conn, 180);
    }
    
    public boolean prenotaChanges(Connection conn, int timeout_sec) {
        try {
            boolean ancora = true;
            boolean timeout = false;
            while (ancora) {
                running = dbu.getListMap(conn, "select *, now() as adesso from db_version where modulo = " + dbu.sql(modulo)).get(0);
                System.out.println("running = " + running);
                //attendere max 1 minuto poi chiedere cosa fare
                boolean ancorarunning = true;

                if (running.get("running_quando") != null) {
                    String chi = cu.s(running.get("running_chi"));
                    Date adessoserver = cu.toDate(running.get("adesso"));
                    Date quando = cu.toDate(running.get("running_quando"));
                    long temposecondi = (adessoserver.getTime() - quando.getTime()) / 1000;
                    if (temposecondi < timeout_sec) {
                        String msg = modulo + ": attendo aggiornamento db da altra postazione (" + (timeout_sec - temposecondi) + " sec, " + chi + ")";
                        System.out.println(msg);
                        if (this.showAttesa != null) this.showAttesa.run(msg);
                        Thread.sleep(1000);
                    } else {
                        System.out.println(modulo + ": sovrascrvio running lock per timeout");
                        ancora = false;
                        timeout = true;
                    }
                } else {
                    ancora = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showExceptionMessage(null, e);
            return false;
        }

        try {
            String hostname = SystemUtils.getHostname();
            System.out.println("prenotaChanges " + modulo + ", hostname = " + hostname);
            String sqlrun = "update db_version set running_chi = " + dbu.sql(hostname) + ", running_quando = NOW()"
                    + " where modulo = " + dbu.sql(modulo);
            dbu.tryExecQuery(conn, sqlrun);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showExceptionMessage(null, e);
        }

        return false;

    }

    public boolean rimuoviPrenotazioneChanges(Connection conn) {
        try {
            String sql = "update db_version set running_chi = null, running_quando = null"
                    + " where modulo = " + dbu.sql(modulo);
            dbu.tryExecQuery(conn, sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showExceptionMessage(null, e);
        }
        return false;
    }
}
