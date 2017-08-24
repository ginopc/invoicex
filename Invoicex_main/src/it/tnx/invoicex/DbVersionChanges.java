/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex;

import it.tnx.Db;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DbVersion;
import it.tnx.commons.DebugFastUtils;
import it.tnx.commons.RunnableWithArgs;
import it.tnx.commons.dbu;
import it.tnx.shell.CurrentDir;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Marco
 */
public class DbVersionChanges {

    DbVersion dbv = null;
    Exception excLock = null;
    
    public DbVersionChanges(String modulo) throws Exception {
        this(modulo, null);
    }
    public DbVersionChanges(String modulo, RunnableWithArgs showAttesa) throws Exception {
        dbv = new DbVersion(Db.getConn(), modulo, showAttesa);
        excLock = new Exception("Impossibile apportare le modifiche\n" + DebugFastUtils.dumpAsString(dbv.running));
    }

    public boolean esegui() throws Exception {
        Connection conn = null;
        try {
            conn = Db.getConnection();
            
            //Tenere a mente che:
            //-le myisam non supportano le transazioni solo innodb
            //-la transazione non gestisce il cambio di struttura ma solo i dati
            //  quindi se creo una tabella e poi faccio rollback la tabella rimane creata
            
            Integer id = 2;
            String sql = null;
            if (dbv.getVersion() < id) {
                if (dbv.prenotaChanges(conn, 60 * 5)) {
                    conn.setAutoCommit(false);
                    System.out.println("DbVersionChanges " + DebugFastUtils.dumpAsString(dbv.running));
                    //cambio struttura tabella matricole da id_padre (di testata) + riga a id_padre_righe
                    String[] tabs = new String[]{"righ_ddt_acquisto_matricole", "righ_ddt_matricole", "righ_fatt_acquisto_matricole", "righ_fatt_matricole"};
                    for (String tab : tabs) {
                        //backup tabella                        
                        String nomeFileDump = CurrentDir.getCurrentDir() + "/backup/tab_" + tab;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
                        Date d = new Date();
                        nomeFileDump += "_" + sdf.format(d) + ".txt";
                        FileOutputStream fos = new FileOutputStream(nomeFileDump, false);
                        it.tnx.Util.dumpTable(tab, Db.getConn(), fos);
                        System.out.println(tab + " : dumped");
                        fos.close();

                        //aggiungo nuovo campo id_padre_righe
                        dbu.tryExecQuery(conn, "ALTER TABLE `" + tab + "`\n"
                                + "	ADD COLUMN `id_padre_righe` INT NULL DEFAULT NULL", false);
                        //aggiorno con join da vecchia chiave
                        dbu.tryExecQuery(conn, "update " + tab + " m join " + StringUtils.substringBefore(tab, "_matricole") + " r on m.id_padre = r.id_padre and m.riga = r.riga set m.id_padre_righe = r.id", false);
                        //elimino record vecchi
                        dbu.tryExecQuery(conn, "delete from `" + tab + "` where id_padre_righe is null", false);
                        //rinomino vecchi campi
                        dbu.tryExecQuery(conn, "ALTER TABLE `" + tab + "`\n"
                                + "	CHANGE COLUMN `serie` `serie_old` CHAR(1),\n"
                                + "	CHANGE COLUMN `numero` `numero_old` INT,\n"
                                + "	CHANGE COLUMN `anno` `anno_old` INT,\n"
                                + "	CHANGE COLUMN `riga` `riga_old` INT,\n"
                                + "	CHANGE COLUMN `id_padre` `id_padre_old` INT", false);

                    }
                    dbv.setVersion(conn, id);
                    dbv.rimuoviPrenotazioneChanges(conn);
                    conn.commit();
                } else {
                    throw excLock;
                }
            }
            
            id = 3;
            if (dbv.getVersion() < id) {
                if (dbv.prenotaChanges(conn, 60 * 5)) {
                    conn.setAutoCommit(false);
                    System.out.println("DbVersionChanges " + DebugFastUtils.dumpAsString(dbv.running));
                    
                    dbu.tryExecQuery(conn, "ALTER TABLE `dati_azienda` ADD COLUMN `export_fatture_esig_diff_iva` char(1) NULL NULL", false);
                    
                    dbv.setVersion(conn, id);
                    dbv.rimuoviPrenotazioneChanges(conn);
                    conn.commit();
                } else {
                    throw excLock;
                }
            }
            
            sql(conn, 4, 120, "ALTER TABLE `pagamenti`	ADD COLUMN `rid` CHAR(1) NULL DEFAULT 'N' AFTER `riba`;");
            
            sql(conn, 5, 30, "CREATE TABLE `test_fatt_acquisto_iva_ded` (\n"
                            + "	`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,\n"
                            + "	`id_padre` INT(10) UNSIGNED NOT NULL DEFAULT '0',\n"
                            + "	`codice_iva` CHAR(3) NOT NULL DEFAULT '',\n"
                            + "	`perc_deducibile` DOUBLE NULL DEFAULT NULL,\n"
                            + "	`perc_iva` DECIMAL(5,2) NOT NULL DEFAULT '0.00',\n"
                            + "	`imponibile` DECIMAL(15,5) NOT NULL DEFAULT '0.00000',\n"
                            + "	`iva` DECIMAL(15,5) NOT NULL DEFAULT '0.00000',\n"
                            + "	`totale` DECIMAL(15,5) NOT NULL DEFAULT '0.00000',\n"
                            + "	PRIMARY KEY (`id`),\n"
                            + "	UNIQUE INDEX `id_padre_codice_iva` (`id_padre`, `codice_iva`, `perc_deducibile`)\n"
                            + ")\n"
                            + "COLLATE='utf8_general_ci'\n"
                            + "ENGINE=MyISAM;");

            sql(conn, 6, 30, "update test_fatt_acquisto set ts_gen_totali = null");

                        
            conn.close();
            return true;
        } catch (Exception ex) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
                dbv.rimuoviPrenotazioneChanges(conn);
            } catch (Exception e) {
            }
            throw ex;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        }
    }
    
    private void sql(Connection conn, int id, int timeout, String... sql) throws SQLException, Exception {
        if (dbv.getVersion() < id) {
            if (dbv.prenotaChanges(conn, 60 * 5)) {
                conn.setAutoCommit(false);
                System.out.println("DbVersionChanges " + DebugFastUtils.dumpAsString(dbv.running));

                for (String nsql : sql) {
                    dbu.tryExecQuery(conn, nsql, false);
                }

                dbv.setVersion(conn, id);
                dbv.rimuoviPrenotazioneChanges(conn);
                conn.commit();
            } else {
                throw excLock;
            }
        }
    }
}
