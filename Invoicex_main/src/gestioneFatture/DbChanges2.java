/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gestioneFatture;

import com.mysql.jdbc.MysqlErrorNumbers;
import static gestioneFatture.main.padre;
import it.tnx.Db;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.FileUtils;
import it.tnx.commons.HttpUtils;
import it.tnx.commons.MicroBench;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.SystemUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.invoicex.Attivazione;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.sync.Sync;
import it.tnx.invoicex.data.DatiAzienda;
import it.tnx.shell.CurrentDir;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author mceccarelli
 */
public class DbChanges2 {

    Statement statLog = null;
    public List logs = null;
    Integer max = null;
    List<Map> aggs = new ArrayList();
    boolean exist_ricevute = true;
    String nome_ricevute = "_ricevute";
    String testate_ricevute = "fatture_ricevute_teste";

    public DbChanges2() {
        //controllo esistenza tabelle proc

        try {
            checkLogExtra();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            statLog = Db.getConn().createStatement();
            logs = DbUtils.getList(Db.getConn(), "select cast(concat(id_log,'|',IFNULL(id_plugin,''),'|',id_email) as CHAR) as mykey from log2");
//            System.out.println("logs = " + logs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (DbUtils.existTable(Db.getConn(), "righ_fatt_acquisto")) {
                exist_ricevute = false;
                nome_ricevute = "_acquisto";
                testate_ricevute = "test_fatt_acquisto";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkLogExtra() {
        //controlla la presenza della tabella log2 e se non c'è la crea
        ArrayList<Map> log2;
        try {
            log2 = DbUtils.getListMap(Db.getConn(), "select * from log2");
            //System.out.println("log2 = " + log2);
        } catch (Exception ex) {
            System.out.println("log2 non esiste");
            creaLog2();
        }
    }

    private void creaLog2() {
        String sql = "create TABLE log2 (`id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,  `id_log` INTEGER UNSIGNED NOT NULL,  `id_plugin` VARCHAR(45),  `id_email` VARCHAR(45) NOT NULL,  `data` TIMESTAMP NOT NULL default current_timestamp,  `note` VARCHAR(45) ) ENGINE=MyISAM";
        System.out.println("sql = " + sql);
        String sql2 = "alter table log2 ADD UNIQUE INDEX `Index_2`(`id_log`, `id_email`, `id_plugin`);";
        System.out.println("sql2 = " + sql2);
        try {
            DbUtils.tryExecQuery(Db.getConn(), sql);
            DbUtils.tryExecQuery(Db.getConn(), sql2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean agg(int id_log, String id_plugin, String id_email, String sql, String note) {
        return agg(id_log, id_plugin, id_email, sql, note, false);
    }

    private boolean agg(int id_log, String id_plugin, String id_email, String sql, String note, boolean scriviOkComunque) {
        HashMap m = new HashMap();
        m.put("id_log", id_log);
        m.put("id_plugin", id_plugin);
        m.put("id_email", id_email);
        m.put("sql", sql);
        m.put("note", note);
        m.put("okcomunque", scriviOkComunque);
        aggs.add(m);
        max = aggs.size();
        return true;
    }

    private void esegui_aggs() {
        int i = 0;
        for (Map m : aggs) {
            i++;
            int id_log = (Integer) m.get("id_log");
            String id_plugin = (String) m.get("id_plugin");
            String id_email = (String) m.get("id_email");
            String sql = (String) m.get("sql");
            String note = (String) m.get("note");
            boolean okcomunque = (Boolean) m.get("okcomunque");

//            main.splash("aggiornamenti struttura database: [2/2] " + i + "/" + max, i * 100 / max);
            main.splash("aggiornamenti struttura database: [2/2] " + i + "/" + max, 50 + (i * 25 / max));
            if (id_plugin == null) {
                id_plugin = "";
            }
            pre_check(id_log, id_plugin, id_email);

            if (sql.toLowerCase().startsWith("insert ")) {
                okcomunque = true;
            }

            if (checkLog(id_log, id_plugin, id_email) == false) {
                post_check(id_log, id_plugin, id_email);
                try {
                    if (Db.executeSql(sql)) {
                        post_execute_ok(id_log, id_plugin, id_email, sql);
                        writeLog(id_log, id_plugin, id_email, note);
                    } else {
                        if (okcomunque) {
                            writeLog(id_log, id_plugin, id_email, note);
                        }
                        post_execute_ko(id_log, id_plugin, id_email, sql);
                        System.out.println("!!! Errore in agg. db !!! " + id_log + "," + id_plugin + "," + id_email + ", sql: " + sql);
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                    if (okcomunque) {
                        writeLog(id_log, id_plugin, id_email, note);
                    }
                    post_execute_ko(id_log, id_plugin, id_email, sql, err);
                    if (err.toString().indexOf("Duplicate column name") >= 0) {
//                        return true;
                    } else {
                        System.out.println("!!! Errore in agg. db !!! " + id_log + "," + id_plugin + "," + id_email + ", sql: " + sql);
                        err.printStackTrace();
//                        return false;
                    }
                }
            } else {
                post_check_ok(id_log, id_plugin, id_email);
            }
        }
    }

    private boolean aggMysql(int id_log, String id_plugin, String id_email, String sql, String note) {
        if (id_plugin == null) {
            id_plugin = "";
        }
        if (checkLog(id_log, id_plugin, id_email) == false) {
            try {
                Db.executeSql("USE mysql");
                if (Db.executeSql(sql)) {
                    Db.executeSql("USE " + Db.dbNameDB);
                    writeLog(id_log, id_plugin, id_email, note);
                } else {
                    System.out.println("!!! Errore in agg. db !!!");
                }
                Db.executeSql("USE " + Db.dbNameDB);
            } catch (Exception err) {
                Db.executeSql("USE " + Db.dbNameDB);
                if (err.toString().indexOf("Duplicate column name") >= 0) {
                    return true;
                } else {
                    System.out.println("!!! Errore in agg. db !!!");
                    err.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    boolean writeLog(String fileName, String desc) {
        try {
            DataOutputStream outFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
            outFile.writeBytes(desc + "\r\n");
            outFile.writeBytes("ok");
            outFile.close();
            return (true);
        } catch (Exception err) {
            err.printStackTrace();

            return (false);
        }
    }

    boolean writeLog(int id_log, String id_plugin, String id_email, String desc) {
        try {
            statLog.execute("insert into log2 (id_log, id_plugin, id_email, note) values (" + id_log + "," + Db.pc(id_plugin, "VARCHAR") + "," + Db.pc(id_email, "VARCHAR") + "," + Db.pc(desc, "VARCHAR") + ")");
            return (true);
        } catch (Exception err) {
            err.printStackTrace();
            return (false);
        }
    }

    boolean checkLog(int id_log, String id_plugin, String id_email) {
        return (logs.contains(id_log + "|" + id_plugin + "|" + id_email));
    }

    static public String readfrom(String string) {
        InputStream is = null;
        try {
            is = DbChanges.class.getResourceAsStream("/it/tnx/invoicex/res/" + string);
            return FileUtils.readContent(is);
        } catch (Exception e) {
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public void esegui_aggiornamenti() {
        MicroBench mb = new MicroBench();
        mb.start();

        System.out.println("inizio controllo aggiornamenti db log2 " + this.getClass().toString());

        try {
            Map running = dbu.getListMap(Db.getConn(), "select *, now() as adesso from log2_running").get(0);
            System.out.println("running = " + running);
            //attendere max 1 minuto poi chiedere cosa fare
            boolean ancorarunning = true;
            while (1 == 1) {
                String chi = cu.s(running.get("chi"));
                Date adessoserver = cu.toDate(running.get("adesso"));
                Date quando = cu.toDate(running.get("quando"));
                long temposecondi = (adessoserver.getTime() - quando.getTime()) / 1000;
                main.splash("attendo aggiornamento db da altra postazione (" + temposecondi + " sec, " + chi + ")", true);
                Thread.sleep(1000);
                try {
                    List<Map> listrunning = dbu.getListMap(Db.getConn(), "select *, now() as adesso from log2_running");
                    System.out.println("attesa listrunning = " + listrunning);
                    if (listrunning.size() == 0) {
                        break;
                    } else {
                        running = listrunning.get(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (temposecondi > 60 * 3) {
                    ancorarunning = true;
                    break;
                }
            }
            if (ancorarunning) {
                System.out.println("*** log2_running ancorarunning : true ***");
                try {
                    String sqlrun = "delete from log2_running";
                    dbu.tryExecQuery(Db.getConn(), sqlrun);
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtils.showExceptionMessage(main.splash, e);
                }
            }
        } catch (Exception e) {
            if (e instanceof java.lang.IndexOutOfBoundsException) {
                //ignoro perchè vuol dire che la query ha funzionato ma non ci sono risultati
            } else if (e instanceof SQLException) {
                SQLException sqle = (SQLException) e;
                System.out.println(sqle.getErrorCode() + " - " + sqle.getSQLState());
                if (sqle.getErrorCode() == MysqlErrorNumbers.ER_NO_SUCH_TABLE) {
                    //creo tabella
                    String sqlcreate = "CREATE TABLE `log2_running` (\n"
                            + "	`chi` VARCHAR(100) NOT NULL,\n"
                            + "	`quando` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n"
                            + "	PRIMARY KEY (`chi`)\n"
                            + ") ENGINE=MyISAM";
                    try {
                        dbu.tryExecQuery(Db.getConn(), sqlcreate);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        SwingUtils.showExceptionMessage(main.splash, e2);
                    }
                }
            } else {
                e.printStackTrace();
            }
        }

        try {
            String hostname = SystemUtils.getHostname();
            System.out.println("hostname = " + hostname);
            String sqlrun = "insert into log2_running set chi = " + dbu.sql(hostname);
            dbu.tryExecQuery(Db.getConn(), sqlrun);
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showExceptionMessage(main.splash, e);
        }

        try {
            //agg(1, "scontrini", "cecca@tnx.it", "ALTER TABLE fatture_ricevute_teste MODIFY COLUMN numero_doc bigint(15)", "Aumentata capienza campo numero fattura esterna");
            agg(1, "", "m.ceccarelli@tnx.it", readfrom("agg_1_mc.sql"), "nuove tabelle per ordini acquisto");
            agg(2, "", "m.ceccarelli@tnx.it", readfrom("agg_2_mc.sql"), "nuove tabelle per ordini acquisto - righe");
            agg(3, "", "m.ceccarelli@tnx.it", "alter table login modify column username varchar(250)", "username piccolo soprattutto su mac");
            agg(4, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_temp modify column username varchar(250)", "username piccolo soprattutto su mac");
            agg(5, "", "m.ceccarelli@tnx.it", "alter table righ_fatt" + nome_ricevute + "_temp modify column username varchar(250)", "username piccolo soprattutto su mac");
            agg(6, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_temp modify column username varchar(250)", "username piccolo soprattutto su mac");
            agg(7, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto_temp modify column username varchar(250)", "username piccolo soprattutto su mac");
            agg(8, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_temp modify column username varchar(250)", "username piccolo soprattutto su mac");
            agg(9, "", "m.ceccarelli@tnx.it", "alter table scadenze_sel modify column username varchar(250)", "username piccolo soprattutto su mac");

            agg(10, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add column quantita_evasa DECIMAL(8,2) NULL DEFAULT NULL", "qta evasa su ordini acquisto");
            agg(11, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add column quantita_evasa DECIMAL(8,2) NULL DEFAULT NULL", "qta evasa su ordini");
            agg(12, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add column evaso CHAR(1) NULL DEFAULT NULL", "ordine acq evaso"); //flag per evaso completamente
            agg(13, "", "m.ceccarelli@tnx.it", "alter table test_ordi add column evaso CHAR(1) NULL DEFAULT NULL", "ordine evaso"); //flag per evaso completamente

            agg(14, "", "m.ceccarelli@tnx.it", readfrom("agg_14_mc.sql"), "nuove tabelle per ddt acquisto");
            agg(15, "", "m.ceccarelli@tnx.it", readfrom("agg_15_mc.sql"), "nuove tabelle per ddt acquisto");
            agg(16, "", "m.ceccarelli@tnx.it", readfrom("agg_16_mc.sql"), "nuove tabelle per ddt acquisto");
            agg(17, "", "m.ceccarelli@tnx.it", readfrom("agg_17_mc.sql"), "nuove tabelle per ddt acquisto");
            //correzione movimenti ddt
            agg18();
            agg(19, "", "m.ceccarelli@tnx.it", "alter table " + testate_ricevute + " add tipo_fattura TINYINT(4) NULL DEFAULT NULL;", "conf ddt acq");
            agg(20, "", "m.ceccarelli@tnx.it", "alter table " + testate_ricevute + " add codice_listino TINYINT(3) UNSIGNED NULL DEFAULT '1';", "conf ddt acq");
            agg(21, "", "m.ceccarelli@tnx.it", "alter table " + testate_ricevute + " add banca_iban VARCHAR(100) NULL DEFAULT NULL;", "conf ddt acq");
            agg(22, "", "m.ceccarelli@tnx.it", "alter table " + testate_ricevute + " add banca_abi VARCHAR(5) NULL DEFAULT NULL", "conf ddt acq");
            agg(23, "", "m.ceccarelli@tnx.it", "alter table " + testate_ricevute + " add banca_cab VARCHAR(5) NULL DEFAULT NULL", "conf ddt acq");
            agg(24, "", "m.ceccarelli@tnx.it", "alter table " + testate_ricevute + " add banca_cc VARCHAR(35) NULL DEFAULT NULL", "conf ddt acq");
            agg(25, "", "m.ceccarelli@tnx.it", "alter table " + testate_ricevute + " add note_pagamento TEXT NULL", "conf ddt acq");

            agg(26, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add in_ddt INT NULL", "campi per giac. virtuale");
            agg(27, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add in_fatt INT NULL", "campi per giac. virtuale");
            agg(28, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add da_ordi INT NULL", "campi per giac. virtuale");
            agg(29, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add in_fatt INT NULL", "campi per giac. virtuale");
            agg(30, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add da_ordi INT NULL", "campi per giac. virtuale");
            agg(31, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add da_ddt INT NULL", "campi per giac. virtuale");

            agg(32, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add in_ddt INT NULL", "campi per giac. virtuale");
            agg(33, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add in_fatt INT NULL", "campi per giac. virtuale");
            agg(34, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add da_ordi INT NULL", "campi per giac. virtuale");
            agg(35, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add in_fatt INT NULL", "campi per giac. virtuale");
            agg(36, "", "m.ceccarelli@tnx.it", "alter table righ_fatt" + nome_ricevute + " add da_ordi INT NULL", "campi per giac. virtuale");
            agg(37, "", "m.ceccarelli@tnx.it", "alter table righ_fatt" + nome_ricevute + " add da_ddt INT NULL", "campi per giac. virtuale");

            agg(38, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add in_ddt_riga INT NULL", "campi per giac. virtuale");
            agg(39, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add in_fatt_riga INT NULL", "campi per giac. virtuale");
            agg(40, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add da_ordi_riga INT NULL", "campi per giac. virtuale");
            agg(41, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add in_fatt_riga INT NULL", "campi per giac. virtuale");
            agg(42, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add da_ordi_riga INT NULL", "campi per giac. virtuale");
            agg(43, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add da_ddt_riga INT NULL", "campi per giac. virtuale");

            agg(44, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add in_ddt_riga INT NULL", "campi per giac. virtuale");
            agg(45, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add in_fatt_riga INT NULL", "campi per giac. virtuale");
            agg(46, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add da_ordi_riga INT NULL", "campi per giac. virtuale");
            agg(47, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add in_fatt_riga INT NULL", "campi per giac. virtuale");
            agg(48, "", "m.ceccarelli@tnx.it", "alter table righ_fatt" + nome_ricevute + " add da_ordi_riga INT NULL", "campi per giac. virtuale");
            agg(49, "", "m.ceccarelli@tnx.it", "alter table righ_fatt" + nome_ricevute + " add da_ddt_riga INT NULL", "campi per giac. virtuale");

            agg(50, "", "m.ceccarelli@tnx.it", "alter table test_ordi add convertito VARCHAR(250) NULL", "conversione multipla");
            agg(51, "", "m.ceccarelli@tnx.it", "alter table test_ddt add convertito VARCHAR(250) NULL", "conversione multipla");
            agg(52, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add convertito VARCHAR(250) NULL", "conversione multipla");
            agg(53, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add convertito VARCHAR(250) NULL", "conversione multipla");

            agg(54, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add column quantita_evasa DECIMAL(8,2) NULL DEFAULT NULL", "qta evasa su ddt acquisto");
            agg(55, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add column quantita_evasa DECIMAL(8,2) NULL DEFAULT NULL", "qta evasa su ddt");
            agg(56, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add column evaso CHAR(1) NULL DEFAULT NULL", "ddt acq evaso"); //flag per evaso completamente
            agg(57, "", "m.ceccarelli@tnx.it", "alter table test_ddt add column evaso CHAR(1) NULL DEFAULT NULL", "ddt evaso"); //flag per evaso completamente

            agg(58, "", "m.ceccarelli@tnx.it", "update test_ordi set evaso = 'S' where IFNULL(doc_tipo,'') != ''", "evasione");
            agg(59, "", "m.ceccarelli@tnx.it", "update test_ordi t, righ_ordi r set r.quantita_evasa = quantita where r.id_padre = t.id and IFNULL(t.doc_tipo,'') != ''", "evasione");
            agg(60, "", "m.ceccarelli@tnx.it", "update test_ddt set evaso = 'S' where IFNULL(fattura_numero,'') != ''", "evasione");
            agg(61, "", "m.ceccarelli@tnx.it", "update test_ddt t, righ_ddt r set r.quantita_evasa = quantita where r.id_padre = t.id and IFNULL(t.fattura_numero,'') != ''", "evasione");
            agg(62, "", "a.toce@tnx.it", "alter table righ_fatt" + nome_ricevute + " add column iva_deducibile DOUBLE(5,2) NOT NULL DEFAULT 100.00", "iva deducibile su riga fatture acquisto");
            agg(63, "", "a.toce@tnx.it", "alter table stampa_iva_semplice add column imp_deducibile DOUBLE(15,5) NULL DEFAULT NULL", "imponibile deducibile su stampa registro iva");
            agg(64, "", "a.toce@tnx.it", "alter table stampa_iva_semplice add column iva_deducibile DOUBLE(15,5) NULL DEFAULT NULL", "imponibile deducibile su stampa registro iva");

//            agg(61, "", "m.ceccarelli@tnx.it", "update test_ddt t, righ_ddt r set r.quantita_evasa = quantita where r.id_padre = t.id and IFNULL(t.fattura_numero,'') != ''", "evasione");
            agg(62, "", "m.ceccarelli@tnx.it", "alter table scadenze_parziali add tipo_pagamento varchar(35) NULL", "scadenze parziali client manager");
            agg(63, "", "m.ceccarelli@tnx.it", "alter table scadenze_parziali add note TEXT NULL", "scadenze parziali client manager");

            boolean mysqlproc = false;
            try {
                if (DbUtils.tryExecQuery(Db.getConn(), "select * from mysql.proc limit 0")) {
                    mysqlproc = true;
                }
            } catch (Exception e) {
            }
            if (!mysqlproc) {
                aggMysql(64, "", "m.ceccarelli@tnx.it", readfrom("mysql_fix_privilege_tables.sql"), "aggiornamento tabelle mysql");
            }

            //non server più calcola_importo_netto
//            agg(65, "", "m.ceccarelli@tnx.it", "drop function if exists calcola_importo_netto", "funzione per calcolo importo netto riga");
//            agg(66, "", "m.ceccarelli@tnx.it", readfrom("agg_66_mc.sql"), "funzione per calcolo importo netto riga");
            agg(67, "", "m.ceccarelli@tnx.it", "alter table temp_stampa_stat_ord_bol_fat add qta DECIMAL(15,5) NULL", "qta in statistiche");

            agg(67, "", "a.toce@tnx.it", "CREATE TABLE IF NOT EXISTS categorie (`id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY) ENGINE=MyISAM", "Creata tabella categorie per evitare errori");

            agg(68, "", "m.ceccarelli@tnx.it", "ALTER TABLE categorie ADD COLUMN descrizione VARCHAR(250) NULL DEFAULT NULL, ADD COLUMN id_padre INT(11) NULL DEFAULT NULL, ADD COLUMN livello INT(11) NULL DEFAULT NULL", "categorie");
            agg(69, "", "m.ceccarelli@tnx.it", "alter table articoli add cat1 INT NULL, add cat2 INT NULL, add cat3 INT NULL, add cat4 INT NULL, add cat5 INT NULL", "categorie");

            agg(70, "", "m.ceccarelli@tnx.it", "alter table articoli add modalita_vendita varchar(250) NULL, add dimensioni varchar(250) NULL, add colore varchar(250) NULL", "caratteristiche articoli");

            agg(71, "", "m.ceccarelli@tnx.it", "alter table articoli CHANGE COLUMN um_eng um_en CHAR(3) NULL DEFAULT NULL, CHANGE COLUMN descrizione_eng descrizione_en VARCHAR(255) NULL DEFAULT NULL", "internaz");

            agg(72, "", "m.ceccarelli@tnx.it", "alter table categorie add descrizione_en VARCHAR(250) NULL after descrizione, add descrizione_fr VARCHAR(250) after descrizione", "categorie");
            agg(73, "", "m.ceccarelli@tnx.it", "ALTER TABLE categorie ADD COLUMN immagine1 varchar(250) default ''", "articoli immagine");
            agg(74, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD COLUMN visibile_online char(1) DEFAULT 'S'", "articoli");

            agg(75, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD COLUMN codice_fornitore2 varchar(100) DEFAULT ''", "articoli codice fornitore");
            agg(76, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD COLUMN codice_fornitore3 varchar(100) DEFAULT ''", "articoli codice fornitore");
            agg(77, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD COLUMN codice_fornitore4 varchar(100) DEFAULT ''", "articoli codice fornitore");
            agg(78, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD COLUMN codice_fornitore5 varchar(100) DEFAULT ''", "articoli codice fornitore");
            agg(75, "", "a.toce@tnx.it", "ALTER TABLE test_ordi ADD COLUMN id_ordine_ipsoa INT NULL DEFAULT NULL", "ordine ipsoa");
            agg(76, "", "a.toce@tnx.it", "ALTER TABLE clie_forn ADD COLUMN perc_sconto DECIMAL(5,2) NULL DEFAULT NULL", "percentuale di sconto prefissata");
            agg(77, "", "a.toce@tnx.it", "ALTER TABLE clie_forn ADD COLUMN flag_email INT NOT NULL DEFAULT 0", "flag per ricezione email");
            agg(78, "", "a.toce@tnx.it", "ALTER TABLE clie_forn ADD COLUMN id_cliente_ipsoa INT NULL DEFAULT NULL", "id cliente per import ipsoa");
            agg(79, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD COLUMN codice_fornitore6 varchar(100) DEFAULT ''", "articoli codice fornitore");
            agg(80, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD INDEX index_codice_fornitore2(codice_fornitore2)", "articoli indice codice fornitore");
            agg(81, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD INDEX index_codice_fornitore3(codice_fornitore3)", "articoli indice codice fornitore");
            agg(82, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD INDEX index_codice_fornitore4(codice_fornitore4)", "articoli indice codice fornitore");
            agg(83, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD INDEX index_codice_fornitore5(codice_fornitore5)", "articoli indice codice fornitore");
            agg(84, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD INDEX index_codice_fornitore6(codice_fornitore6)", "articoli indice codice fornitore");

            agg(85, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD posizione_magazzino varchar(250)", "articoli posizione magazzino");

            agg(86, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add column iva_deducibile DOUBLE(5,2) NOT NULL DEFAULT 100.00", "iva deducibile su altre tabelle");
            agg(87, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add column iva_deducibile DOUBLE(5,2) NOT NULL DEFAULT 100.00", "iva deducibile su altre tabelle");
            agg(88, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add column iva_deducibile DOUBLE(5,2) NOT NULL DEFAULT 100.00", "iva deducibile su altre tabelle");
            agg(89, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add column iva_deducibile DOUBLE(5,2) NOT NULL DEFAULT 100.00", "iva deducibile su altre tabelle");
            agg(90, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add column iva_deducibile DOUBLE(5,2) NOT NULL DEFAULT 100.00", "iva deducibile su altre tabelle");

            if (exist_ricevute) {
                agg(91, "", "m.ceccarelli@tnx.it", "RENAME TABLE fatture_ricevute_teste TO test_fatt_acquisto", "fatture ricevute test_fatt_acquisto");
                agg(92, "", "m.ceccarelli@tnx.it", "RENAME TABLE righ_fatt_ricevute TO righ_fatt_acquisto", "fatture ricevute test_fatt_acquisto");
                agg(93, "", "m.ceccarelli@tnx.it", "RENAME TABLE righ_fatt_ricevute_temp TO righ_fatt_acquisto_temp", "fatture ricevute test_fatt_acquisto");
                agg(94, "", "m.ceccarelli@tnx.it", "RENAME TABLE righ_fatt_ricevute_lotti TO righ_fatt_acquisto_lotti", "fatture ricevute test_fatt_acquisto");
                agg(95, "", "m.ceccarelli@tnx.it", "RENAME TABLE righ_fatt_ricevute_matricole TO righ_fatt_acquisto_matricole", "fatture ricevute test_fatt_acquisto");
            }

            agg(96, "", "m.ceccarelli@tnx.it", "update movimenti_magazzino set da_tabella = 'test_fatt_acquisto' where da_tabella = 'test_fatt_ricevute'", "fatture ricevute test_fatt_acquisto");

            agg(97, "", "m.ceccarelli@tnx.it", "alter table categorie CHANGE COLUMN descrizione nome VARCHAR(250) NULL, CHANGE COLUMN descrizione_en nome_en VARCHAR(250) NULL, CHANGE COLUMN descrizione_fr nome_fr VARCHAR(250) NULL", "categorie");

            agg(98, "", "m.ceccarelli@tnx.it", "alter table categorie add descrizione TEXT NULL, add descrizione_en TEXT NULL, add descrizione_fr TEXT NULL", "categorie");
            agg(99, "", "m.ceccarelli@tnx.it", "alter table categorie add visibile_se_registrato CHAR(1) NULL", "categorie");
            agg(100, "", "m.ceccarelli@tnx.it", "alter table articoli add visibile_se_registrato CHAR(1) NULL", "articoli web");
            agg(101, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD COLUMN immagine1_web varchar(250) default '' after immagine1", "articoli immagine");

            agg(102, "", "a.toce@tnx.it", "ALTER TABLE pagamenti ADD COLUMN id_pagamento_ipsoa INT NULL DEFAULT NULL", "id tipo di pagamento Ipsoa");

            agg(103, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto modify column iva_deducibile DOUBLE(5,2) NULL DEFAULT 100.00", "possibilita di null");
            agg(104, "", "m.ceccarelli@tnx.it", "alter table righ_fatt modify column iva_deducibile DOUBLE(5,2) NULL DEFAULT 100.00", "possibilita di null");
            agg(105, "", "m.ceccarelli@tnx.it", "alter table righ_ddt modify column iva_deducibile DOUBLE(5,2) NULL DEFAULT 100.00", "possibilita di null");
            agg(106, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto modify column iva_deducibile DOUBLE(5,2) NULL DEFAULT 100.00", "possibilita di null");
            agg(107, "", "m.ceccarelli@tnx.it", "alter table righ_ordi modify column iva_deducibile DOUBLE(5,2) NULL DEFAULT 100.00", "possibilita di null");
            agg(108, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto modify column iva_deducibile DOUBLE(5,2) NULL DEFAULT 100.00", "possibilita di null");
            agg(109, "", "m.ceccarelli@tnx.it", "update righ_fatt_acquisto set iva_deducibile = null where iva_deducibile = 0", "possibilita di null");

            agg(110, "", "m.ceccarelli@tnx.it", "alter table test_ddt add column listino_consigliato varchar(10) null", "listino consigliato");
            agg(111, "", "m.ceccarelli@tnx.it", "alter table test_ordi add column listino_consigliato varchar(10) null", "listino consigliato");
            agg(112, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column listino_consigliato varchar(10) null", "listino consigliato");
            agg(113, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add column listino_consigliato varchar(10) null", "listino consigliato");
            agg(114, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add column listino_consigliato varchar(10) null", "listino consigliato");
            agg(115, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add column listino_consigliato varchar(10) null", "listino consigliato");

            agg(116, "", "a.toce@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN testo_piede_fatt_v TEXT", "Aggiunte note a piede pagina su report fatture vendita");
            agg(117, "", "a.toce@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN testo_piede_docu_v TEXT", "Aggiunte note a piede pagina su report ddt vendita");
            agg(118, "", "a.toce@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN testo_piede_ordi_v TEXT", "Aggiunte note a piede pagina su report ordini vendita");
            agg(119, "", "a.toce@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN testo_piede_fatt_a TEXT", "Aggiunte note a piede pagina su report fatture acquisto");
            agg(120, "", "a.toce@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN testo_piede_docu_a TEXT", "Aggiunte note a piede pagina su report ddt acquisto");
            agg(121, "", "a.toce@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN testo_piede_ordi_a TEXT", "Aggiunte note a piede pagina su report ordini acquisto");
            agg(122, "", "a.toce@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN stampa_riga_invoicex INTEGER NOT NULL DEFAULT 1", "Flag per togliere riga a fondo report");

            agg(123, "", "a.toce@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN immagine_firma_ordine MEDIUMBLOB NULL DEFAULT NULL", "Immagine per firma immagine");

//            agg(124, "", "test@tnx.it", "ALTER TABLE agenti ADD COLUMN test1 varchar(100) NULL DEFAULT NULL", "test");
//            agg(125, "", "test@tnx.it", "ALTER TABLE agenti ADD COLUMN test2 varchar(100) NULL DEFAULT NULL", "test");
            agg(126, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `logo_email` mediumblob NULL", "logo email in dati azienda");
            agg(127, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `sfondo` mediumblob NULL", "sfondo in dati azienda");
            agg(128, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `sfondo_email` mediumblob NULL", "sfondo email in dati azienda");

            agg(129, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `logo_nome_file` varchar(250) NULL", "logo");
            agg(130, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `logo_data_modifica` bigint NULL", "logo");
            agg(131, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `logo_dimensione` bigint NULL", "logo");

            agg(132, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `logo_email_nome_file` varchar(250) NULL", "logo");
            agg(133, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `logo_email_data_modifica` bigint NULL", "logo");
            agg(134, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `logo_email_dimensione` bigint NULL", "logo");

            agg(135, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `sfondo_nome_file` varchar(250) NULL", "sfondo");
            agg(136, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `sfondo_data_modifica` bigint NULL", "sfondo");
            agg(137, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `sfondo_dimensione` bigint NULL", "sfondo");

            agg(138, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `sfondo_email_nome_file` varchar(250) NULL", "sfondo");
            agg(139, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `sfondo_email_data_modifica` bigint NULL", "sfondo");
            agg(140, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `sfondo_email_dimensione` bigint NULL", "sfondo");

            agg(141, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `logo_in_db` char(1) NULL", "sfondo");

            agg(142, "", "a.toce@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `label_cliente` VARCHAR(100) DEFAULT 'Cliente'", "Etichetta cliente");
            agg(143, "", "a.toce@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `label_destinazione` VARCHAR(100) DEFAULT 'Destinazione Merce'", "Etichetta destinazione");

            agg(142, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` drop primary key", "dati azienda pk", true);
            agg(143, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` CHANGE COLUMN `id` `id` INT(11) NULL AUTO_INCREMENT,  ADD PRIMARY KEY (`id`)", "dati azienda pk", true);

            agg(144, "", "a.toce@tnx.it", "ALTER TABLE `clie_forn` ADD COLUMN `flag_update_listino` CHAR NOT NULL DEFAULT 'N'", "Aggiunto flag per aggiornamento automatico listino");
            agg(144, "", "m.ceccarelli@tnx.it", "ALTER TABLE `pagamenti` add COLUMN `iva30gg` char(1) NULL default 'N'", "iva30gg su pagamenti");
            agg(145, "", "m.ceccarelli@tnx.it", "update tipi_fatture set descrizione = 'FATTURA' where descrizione_breve = 'FI'", "tolto immediata da fattura");

            agg(145, "", "a.toce@tnx.it", "ALTER TABLE `test_fatt` ADD COLUMN `color` VARCHAR(20) DEFAULT ''", "Aggiunta colonna per segnalazione colore");

            //modifiche tux
            agg(146, "", "a.toce@tnx.it", "ALTER TABLE `righ_fatt` ADD COLUMN `arrotondamento_parametro` VARCHAR(4) DEFAULT '0'", "Aggiunta colonna per salvataggio parametro di arrotondamento su righe fattura");
            agg(147, "", "a.toce@tnx.it", "ALTER TABLE `righ_fatt` ADD COLUMN `arrotondamento_tipo` VARCHAR(4) DEFAULT ''", "Aggiunta colonna per salvataggio tipo di arrotondamento su righe fattura");
            agg(148, "", "a.toce@tnx.it", "ALTER TABLE `righ_fatt` ADD COLUMN `totale_ivato` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe fattura");
            agg(149, "", "a.toce@tnx.it", "ALTER TABLE `righ_fatt` ADD COLUMN `totale_imponibile` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe fattura");

            agg(150, "", "a.toce@tnx.it", "ALTER TABLE `righ_ordi` ADD COLUMN `arrotondamento_parametro` VARCHAR(4) DEFAULT '0'", "Aggiunta colonna per salvataggio parametro di arrotondamento su righe ordini");
            agg(151, "", "a.toce@tnx.it", "ALTER TABLE `righ_ordi` ADD COLUMN `arrotondamento_tipo` VARCHAR(4) DEFAULT ''", "Aggiunta colonna per salvataggio tipo di arrotondamento su righe ordini");
            agg(152, "", "a.toce@tnx.it", "ALTER TABLE `righ_ordi` ADD COLUMN `totale_ivato` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe ordini");
            agg(153, "", "a.toce@tnx.it", "ALTER TABLE `righ_ordi` ADD COLUMN `totale_imponibile` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe ordini");

            agg(154, "", "a.toce@tnx.it", "ALTER TABLE `righ_ddt` ADD COLUMN `arrotondamento_parametro` VARCHAR(4) DEFAULT '0'", "Aggiunta colonna per salvataggio parametro di arrotondamento su righe ddt");
            agg(155, "", "a.toce@tnx.it", "ALTER TABLE `righ_ddt` ADD COLUMN `arrotondamento_tipo` VARCHAR(4) DEFAULT ''", "Aggiunta colonna per salvataggio tipo di arrotondamento su righe ddt");
            agg(156, "", "a.toce@tnx.it", "ALTER TABLE `righ_ddt` ADD COLUMN `totale_ivato` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe ddt");
            agg(157, "", "a.toce@tnx.it", "ALTER TABLE `righ_ddt` ADD COLUMN `totale_imponibile` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe ddt");

            agg(158, "", "a.toce@tnx.it", "ALTER TABLE `righ_fatt_acquisto` ADD COLUMN `arrotondamento_parametro` VARCHAR(4) DEFAULT '0'", "Aggiunta colonna per salvataggio parametro di arrotondamento su righe fattura_acquisto");
            agg(159, "", "a.toce@tnx.it", "ALTER TABLE `righ_fatt_acquisto` ADD COLUMN `arrotondamento_tipo` VARCHAR(4) DEFAULT ''", "Aggiunta colonna per salvataggio tipo di arrotondamento su righe fattura_acquisto");
            agg(160, "", "a.toce@tnx.it", "ALTER TABLE `righ_fatt_acquisto` ADD COLUMN `totale_ivato` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe fattura_acquisto");
            agg(161, "", "a.toce@tnx.it", "ALTER TABLE `righ_fatt_acquisto` ADD COLUMN `totale_imponibile` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe fattura_acquisto");

            agg(162, "", "a.toce@tnx.it", "ALTER TABLE `righ_ordi_acquisto` ADD COLUMN `arrotondamento_parametro` VARCHAR(4) DEFAULT '0'", "Aggiunta colonna per salvataggio parametro di arrotondamento su righe ordini_acquisto");
            agg(163, "", "a.toce@tnx.it", "ALTER TABLE `righ_ordi_acquisto` ADD COLUMN `arrotondamento_tipo` VARCHAR(4) DEFAULT ''", "Aggiunta colonna per salvataggio tipo di arrotondamento su righe ordini_acquisto");
            agg(164, "", "a.toce@tnx.it", "ALTER TABLE `righ_ordi_acquisto` ADD COLUMN `totale_ivato` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe ordini_acquisto");
            agg(165, "", "a.toce@tnx.it", "ALTER TABLE `righ_ordi_acquisto` ADD COLUMN `totale_imponibile` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe ordini_acquisto");

            agg(166, "", "a.toce@tnx.it", "ALTER TABLE `righ_ddt_acquisto` ADD COLUMN `arrotondamento_parametro` VARCHAR(4) DEFAULT '0'", "Aggiunta colonna per salvataggio parametro di arrotondamento su righe ddt_acquisto");
            agg(167, "", "a.toce@tnx.it", "ALTER TABLE `righ_ddt_acquisto` ADD COLUMN `arrotondamento_tipo` VARCHAR(4) DEFAULT ''", "Aggiunta colonna per salvataggio tipo di arrotondamento su righe ddt_acquisto");
            agg(168, "", "a.toce@tnx.it", "ALTER TABLE `righ_ddt_acquisto` ADD COLUMN `totale_ivato` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe ddt_acquisto");
            agg(169, "", "a.toce@tnx.it", "ALTER TABLE `righ_ddt_acquisto` ADD COLUMN `totale_imponibile` DECIMAL(15,5) NOT NULL DEFAULT '0.00000'", "Aggiunto totale iva arrotondato su righe ddt_acquisto");

            agg(170, "", "a.toce@tnx.it", "UPDATE righ_fatt set totale_imponibile = CAST(IF(arrotondamento_parametro = '0', (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.', FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2))", "Aggiornamento totali imponibili di riga fattura");

            agg(171, "", "a.toce@tnx.it", "UPDATE righ_ordi set totale_imponibile = CAST(IF(arrotondamento_parametro = '0', (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.', FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2))", "Aggiornamento totali imponibili di riga ordine");
            agg(172, "", "a.toce@tnx.it", "UPDATE righ_ordi r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = totale_imponibile + (totale_imponibile * i.percentuale/100)", "Aggiornati totali iva righe ordine");

            agg(173, "", "a.toce@tnx.it", "UPDATE righ_ddt set totale_imponibile = CAST(IF(arrotondamento_parametro = '0', (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.', FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2))", "Aggiornamento totali imponibili di riga ddt");
            agg(174, "", "a.toce@tnx.it", "UPDATE righ_ddt r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = totale_imponibile + (totale_imponibile * i.percentuale/100)", "Aggiornati totali iva righe ddt");

            agg(175, "", "a.toce@tnx.it", "UPDATE righ_fatt_acquisto set totale_imponibile = CAST(IF(arrotondamento_parametro = '0', (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.', FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2))", "Aggiornamento totali imponibili di riga fattura acquisto");
            agg(176, "", "a.toce@tnx.it", "UPDATE righ_fatt_acquisto r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = totale_imponibile + (totale_imponibile * i.percentuale/100)", "Aggiornati totali iva righe fattura acquisto");

            agg(177, "", "a.toce@tnx.it", "UPDATE righ_ordi_acquisto set totale_imponibile = CAST(IF(arrotondamento_parametro = '0', (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.', FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2))", "Aggiornamento totali imponibili di riga ordine acquisto");
            agg(178, "", "a.toce@tnx.it", "UPDATE righ_ordi_acquisto r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = totale_imponibile + (totale_imponibile * i.percentuale/100)", "Aggiornati totali iva righe ordine acquisto");

            agg(179, "", "a.toce@tnx.it", "UPDATE righ_ddt_acquisto set totale_imponibile = CAST(IF(arrotondamento_parametro = '0', (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.', FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - (prezzo*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2))", "Aggiornamento totali imponibili di riga ddt acquisto");
            agg(180, "", "a.toce@tnx.it", "UPDATE righ_ddt_acquisto r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = totale_imponibile + (totale_imponibile * i.percentuale/100)", "Aggiornati totali iva righe ddt acquisto");

            agg(181, "", "a.toce@tnx.it", "ALTER TABLE articoli ADD COLUMN is_descrizione CHAR NOT NULL DEFAULT 'N'", "Aggiunta colonna per descrizioni in articoli");
            agg(182, "", "a.toce@tnx.it", "ALTER TABLE righ_fatt ADD COLUMN is_descrizione CHAR NOT NULL DEFAULT 'N'", "Aggiunta colonna per descrizioni in righe fatture");
            agg(183, "", "a.toce@tnx.it", "ALTER TABLE righ_ordi ADD COLUMN is_descrizione CHAR NOT NULL DEFAULT 'N'", "Aggiunta colonna per descrizioni in righe ordini");
            agg(184, "", "a.toce@tnx.it", "ALTER TABLE righ_ddt ADD COLUMN is_descrizione CHAR NOT NULL DEFAULT 'N'", "Aggiunta colonna per descrizioni in righe ddt");
            agg(185, "", "a.toce@tnx.it", "ALTER TABLE righ_fatt_acquisto ADD COLUMN is_descrizione CHAR NOT NULL DEFAULT 'N'", "Aggiunta colonna per descrizioni in righe fatture di acquistO");
            agg(186, "", "a.toce@tnx.it", "ALTER TABLE righ_ordi_acquisto ADD COLUMN is_descrizione CHAR NOT NULL DEFAULT 'N'", "Aggiunta colonna per descrizioni in righe ordini di acquistO");
            agg(187, "", "a.toce@tnx.it", "ALTER TABLE righ_ddt_acquisto ADD COLUMN is_descrizione CHAR NOT NULL DEFAULT 'N'", "Aggiunta colonna per descrizioni in righe ddt di acquistO");

            agg(188, "", "a.toce@tnx.it", "UPDATE righ_fatt r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = totale_imponibile + (totale_imponibile * i.percentuale/100)", "Aggiornati totali iva righe fattura");

            //modifiche cecca
            agg(146, "", "m.ceccarelli@tnx.it", "alter table movimenti_magazzino add column da_tipo_fattura tinyint", "movimenti magazzino per riconoscere scontrini");

            agg(147, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `codiceIvaDefault` VARCHAR(10)", "codiceIvaDefault");
            agg(148, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `codiceIvaSpese` VARCHAR(10)", "codiceIvaSpese");
            agg(149, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `iva21eseguito` CHAR(1)", "iva21eseguito");
            agg(150, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `iva21a20eseguito` CHAR(1)", "iva21a20eseguito");

            //porto impostazioni iva su db
            String codiceIvaDefault = null;
            String codiceIvaSpese = null;
            try {
                codiceIvaDefault = main.fileIni.getValue("iva", "codiceIvaDefault", null);
                codiceIvaSpese = main.fileIni.getValue("iva", "codiceIvaSpese", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            agg(151, "", "m.ceccarelli@tnx.it", "update dati_azienda set codiceIvaDefault = " + Db.pcs(codiceIvaDefault), "codiceIvaDefault");
            agg(152, "", "m.ceccarelli@tnx.it", "update dati_azienda set codiceIvaSpese = " + Db.pcs(codiceIvaSpese), "codiceIvaSpese");

            if (!(DbUtils.containRows(Db.getConn(), "select * from codici_iva where codice = '21'"))) {
                agg(153, "", "m.ceccarelli@tnx.it", "insert into codici_iva set codice = '21', percentuale = 21, descrizione = 'Iva 21%', descrizione_breve = 'Iva 21%'", "iva21");
            }

            agg(154, "", "m.ceccarelli@tnx.it", "CREATE TABLE versioni_clients (hostname VARCHAR(250) not NULL primary key,versione VARCHAR(20) NULL, pacchetto VARCHAR(100) NULL, tempo TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=MyISAM", "tabella per versioni clients di invoicex");
            agg(155, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli_prezzi ADD COLUMN sconto1 DECIMAL(5,2) NULL, ADD COLUMN sconto2 DECIMAL(5,2) NULL;", "sconti su articoli");
            agg(156, "", "m.ceccarelli@tnx.it", "ALTER TABLE clie_forn ADD COLUMN sconto1t DECIMAL(5,2) NULL, ADD COLUMN sconto2t DECIMAL(5,2) NULL, ADD COLUMN sconto3t DECIMAL(5,2) NULL, ADD COLUMN sconto1r DECIMAL(5,2) NULL, ADD COLUMN sconto2r DECIMAL(5,2) NULL;", "sconti su cliente");

            agg(157, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD INDEX descrizione_25 (descrizione(25))", "indicie su descrizione articoli");

            //progblema aggiornamento totali righe
            agg(158, "", "m.ceccarelli@tnx.it", "UPDATE righ_fatt set totale_imponibile = ROUND(CAST(  IF(arrotondamento_parametro = '0',  (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.',   FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2)),2)", "agg2 totali");
            agg(159, "", "m.ceccarelli@tnx.it", "UPDATE righ_ddt set totale_imponibile = ROUND(CAST(  IF(arrotondamento_parametro = '0',  (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.',   FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2)),2)", "agg2 totali");
            agg(160, "", "m.ceccarelli@tnx.it", "UPDATE righ_ordi set totale_imponibile = ROUND(CAST(  IF(arrotondamento_parametro = '0',  (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.',   FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2)),2)", "agg2 totali");
            agg(161, "", "m.ceccarelli@tnx.it", "UPDATE righ_fatt_acquisto set totale_imponibile = ROUND(CAST(  IF(arrotondamento_parametro = '0',  (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.',   FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2)),2)", "agg2 totali");
            agg(162, "", "m.ceccarelli@tnx.it", "UPDATE righ_ddt_acquisto set totale_imponibile = ROUND(CAST(  IF(arrotondamento_parametro = '0',  (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.',   FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2)),2)", "agg2 totali");
            agg(163, "", "m.ceccarelli@tnx.it", "UPDATE righ_ordi_acquisto set totale_imponibile = ROUND(CAST(  IF(arrotondamento_parametro = '0',  (prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) * IFNULL(quantita, 0.00), IF(arrotondamento_tipo = 'Inf.',   FLOOR(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00), CEIL(((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)) - ((prezzo - (prezzo*(IFNULL(sconto1, 0.00)/100)))*(IFNULL(sconto2, 0.00)/100))) )/CAST(arrotondamento_parametro AS DECIMAL(5,2))) * CAST(arrotondamento_parametro AS DECIMAL(5,2)) * IFNULL(quantita, 0.00))) AS DECIMAL(10,2)),2)", "agg2 totali");

            agg(164, "", "m.ceccarelli@tnx.it", "UPDATE righ_fatt r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = round(totale_imponibile + (totale_imponibile * i.percentuale/100),2)", "agg2 totali righe");
            agg(165, "", "m.ceccarelli@tnx.it", "UPDATE righ_ddt r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = round(totale_imponibile + (totale_imponibile * i.percentuale/100),2)", "agg2 totali righe");
            agg(166, "", "m.ceccarelli@tnx.it", "UPDATE righ_ordi r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = round(totale_imponibile + (totale_imponibile * i.percentuale/100),2)", "agg2 totali righe");
            agg(167, "", "m.ceccarelli@tnx.it", "UPDATE righ_fatt_acquisto r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = round(totale_imponibile + (totale_imponibile * i.percentuale/100),2)", "agg2 totali righe");
            agg(168, "", "m.ceccarelli@tnx.it", "UPDATE righ_ddt_acquisto r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = round(totale_imponibile + (totale_imponibile * i.percentuale/100),2)", "agg2 totali righe");
            agg(169, "", "m.ceccarelli@tnx.it", "UPDATE righ_ordi_acquisto r LEFT JOIN codici_iva i ON r.iva = i.codice SET totale_ivato = round(totale_imponibile + (totale_imponibile * i.percentuale/100),2)", "agg2 totali righe");

            // Creo tabella di dettaglio per personalizzazione SNJ
            // Campi per tipo ordine A
            agg(194, "", "a.toce@tnx.it", "ALTER TABLE righ_ordi ADD COLUMN percentuale INTEGER NULL DEFAULT NULL", "Aggiunto campo percentuale");
            agg(195, "", "a.toce@tnx.it", "ALTER TABLE righ_ordi ADD COLUMN emissione_fattura INTEGER UNSIGNED NULL DEFAULT NULL", "Aggiunto campo emissione_fattura");
            agg(196, "", "a.toce@tnx.it", "ALTER TABLE righ_ordi ADD COLUMN termini_pagamento VARCHAR(35) NULL DEFAULT NULL", "Aggiunto campo tipo di pagamento");
            agg(197, "", "a.toce@tnx.it", "ALTER TABLE righ_ordi ADD COLUMN imponibile DECIMAL(12,5) NULL DEFAULT NULL", "Aggiunto campo imponibile");
            // Campi per tipo ordine B
            agg(198, "", "a.toce@tnx.it", "ALTER TABLE righ_ordi ADD COLUMN costo_giornaliero DECIMAL(12,5) NULL DEFAULT NULL", "Aggiunto campo imponibile");
            agg(199, "", "a.toce@tnx.it", "ALTER TABLE righ_ordi ADD COLUMN costo_mensile DECIMAL(12,5) NULL DEFAULT NULL", "Aggiunto campo imponibile");
            agg(200, "", "a.toce@tnx.it", "ALTER TABLE righ_ordi ADD COLUMN durata_consulenza INTEGER UNSIGNED NULL DEFAULT NULL", "Aggiunto campo imponibile");
            agg(201, "", "a.toce@tnx.it", "ALTER TABLE righ_ordi ADD COLUMN durata_contratto INTEGER UNSIGNED NULL DEFAULT NULL", "Aggiunto campo imponibile");

            // Tabelle per le anagrafiche di SNJ
            agg(202, "", "a.toce@tnx.it", "CREATE TABLE tipi_durata_consulenza (`id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,  `descrizione` VARCHAR(255) NOT NULL) ENGINE=MyISAM", "Creazione tabella per anagrafica durata consulenza");
            agg(203, "", "a.toce@tnx.it", "CREATE TABLE tipi_durata_contratto (`id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,  `descrizione` VARCHAR(255) NOT NULL) ENGINE=MyISAM", "Creazione tabella per anagrafica durata contratto");
            agg(204, "", "a.toce@tnx.it", "CREATE TABLE tipi_emissione_fattura (`id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,  `descrizione` VARCHAR(255) NOT NULL) ENGINE=MyISAM", "Creazione tabella per anagrafica emissione fattura");
            agg(205, "", "a.toce@tnx.it", "CREATE TABLE stati_preventivo_ordine (`id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,  `descrizione` VARCHAR(255) NOT NULL) ENGINE=MyISAM", "Creazione tabella per anagrafica stati prev./ordine");

            agg(207, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Preventivo'", "Inserimento Stati Preventivo/Ordine");
            agg(208, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Preventivo Inviato'", "Inserimento Stati Preventivo/Ordine");
            agg(209, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Preventivo Accettato'", "Inserimento Stati Preventivo/Ordine");
            agg(210, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Conferma d\\'Ordine'", "Inserimento Stati Preventivo/Ordine");
            agg(211, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Ordine'", "Inserimento Stati Preventivo/Ordine");
            agg(212, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Ordine Inviato'", "Inserimento Stati Preventivo/Ordine");
            agg(213, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Ordine Accettato'", "Inserimento Stati Preventivo/Ordine");
            agg(214, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Ordine in lavorazione - 0%'", "Inserimento Stati Preventivo/Ordine");
            agg(215, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Ordine in lavorazione - 25%'", "Inserimento Stati Preventivo/Ordine");
            agg(216, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Ordine in lavorazione - 50%'", "Inserimento Stati Preventivo/Ordine");
            agg(217, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Ordine in lavorazione - 75%'", "Inserimento Stati Preventivo/Ordine");
            agg(218, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Ordine in lavorazione - Sospeso'", "Inserimento Stati Preventivo/Ordine");
            agg(219, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Ordine Completato'", "Inserimento Stati Preventivo/Ordine");
            agg(220, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Ordine Fatturato'", "Inserimento Stati Preventivo/Ordine");
            agg(221, "", "a.toce@tnx.it", "INSERT INTO stati_preventivo_ordine SET descrizione = 'Consuntivo in lavorazione'", "Inserimento Stati Preventivo/Ordine");

            agg(222, "", "a.toce@tnx.it", "INSERT INTO tipi_emissione_fattura SET descrizione = 'All\\'Ordine'", "Inserimento Tipi di Emissione Fattura");
            agg(223, "", "a.toce@tnx.it", "INSERT INTO tipi_emissione_fattura SET descrizione = 'Alla Consegna'", "Inserimento Tipi di Emissione Fattura");
            agg(224, "", "a.toce@tnx.it", "INSERT INTO tipi_emissione_fattura SET descrizione = 'Al Completamento'", "Inserimento Tipi di Emissione Fattura");
            agg(225, "", "a.toce@tnx.it", "INSERT INTO tipi_emissione_fattura SET descrizione = 'Consuntivo Fine Mese'", "Inserimento Tipi di Emissione Fattura");
            agg(226, "", "a.toce@tnx.it", "INSERT INTO tipi_emissione_fattura SET descrizione = 'A Fine Evento'", "Inserimento Tipi di Emissione Fattura");
            agg(227, "", "a.toce@tnx.it", "INSERT INTO tipi_emissione_fattura SET descrizione = 'Accettazione Grafica'", "Inserimento Tipi di Emissione Fattura");
            agg(228, "", "a.toce@tnx.it", "INSERT INTO tipi_emissione_fattura SET descrizione = 'Mensile anticipata'", "Inserimento Tipi di Emissione Fattura");
            agg(229, "", "a.toce@tnx.it", "INSERT INTO tipi_emissione_fattura SET descrizione = 'Bimestrale Anticpata'", "Inserimento Tipi di Emissione Fattura");
            agg(230, "", "a.toce@tnx.it", "INSERT INTO tipi_emissione_fattura SET descrizione = 'Ogni Fine Mese'", "Inserimento Tipi di Emissione Fattura");
            agg(231, "", "a.toce@tnx.it", "INSERT INTO tipi_emissione_fattura SET descrizione = 'Ogni Fine Bimestre'", "Inserimento Tipi di Emissione Fattura");

            agg(232, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = 'Primi 5 gg'", "Inserimento Tipi di Durata Consulenza");
            agg(233, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = 'Primi 10 gg'", "Inserimento Tipi di Durata Consulenza");
            agg(234, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = 'Dal 6 gg al 15gg'", "Inserimento Tipi di Durata Consulenza");
            agg(235, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = 'Dal 15 gg in poi'", "Inserimento Tipi di Durata Consulenza");
            agg(236, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = 'Dal 5 gg in poi'", "Inserimento Tipi di Durata Consulenza");
            agg(237, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = 'Dal 10 gg in poi'", "Inserimento Tipi di Durata Consulenza");
            agg(238, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = 'Giornaliera'", "Inserimento Tipi di Durata Consulenza");
            agg(239, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = '4 gg al mese x 6 mesi'", "Inserimento Tipi di Durata Consulenza");
            agg(240, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = '8 gg al mese x 6 mesi'", "Inserimento Tipi di Durata Consulenza");
            agg(241, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = '12 gg al mese x 6 mesi'", "Inserimento Tipi di Durata Consulenza");
            agg(242, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = '4 gg al mese x 12 mesi'", "Inserimento Tipi di Durata Consulenza");
            agg(243, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = '8 gg al mese x 12 mesi'", "Inserimento Tipi di Durata Consulenza");
            agg(244, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = '12 gg al mese x 12 mesi'", "Inserimento Tipi di Durata Consulenza");
            agg(245, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_consulenza SET descrizione = 'Fino a Termine Progetto'", "Inserimento Tipi di Durata Consulenza");

            agg(246, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_contratto SET descrizione = 'X 2 Mesi'", "Inserimento Tipi di Durata Contratto");
            agg(247, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_contratto SET descrizione = 'X 3 Mesi'", "Inserimento Tipi di Durata Contratto");
            agg(248, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_contratto SET descrizione = 'X 4 Mesi'", "Inserimento Tipi di Durata Contratto");
            agg(249, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_contratto SET descrizione = 'X 6 Mesi'", "Inserimento Tipi di Durata Contratto");
            agg(250, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_contratto SET descrizione = 'X 8 Mesi'", "Inserimento Tipi di Durata Contratto");
            agg(251, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_contratto SET descrizione = 'X 12 Mesi'", "Inserimento Tipi di Durata Contratto");
            agg(252, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_contratto SET descrizione = 'X 18 Mesi'", "Inserimento Tipi di Durata Contratto");
            agg(253, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_contratto SET descrizione = 'X 24 Mesi'", "Inserimento Tipi di Durata Contratto");
            agg(254, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_contratto SET descrizione = 'X 32 Mesi'", "Inserimento Tipi di Durata Contratto");
            agg(255, "", "a.toce@tnx.it", "INSERT INTO tipi_durata_contratto SET descrizione = 'X 36 Mesi'", "Inserimento Tipi di Durata Contratto");

            agg(256, "", "a.toce@tnx.it", "ALTER TABLE test_ordi ADD COLUMN tipo_snj VARCHAR(1) NULL DEFAULT NULL", "Aggiunto campo tipo ordine SNJ");
            agg(257, "", "a.toce@tnx.it", "ALTER TABLE test_ordi_acquisto ADD COLUMN tipo_snj VARCHAR(1) NULL DEFAULT NULL", "Aggiunto campo tipo ordine SNJ");

            agg(258, "", "m.ceccarelli@tnx.it", "ALTER TABLE " + testate_ricevute + " MODIFY COLUMN numero_doc varchar(50)", "cambiato campo numero doc esterno in varchar!");

            agg(258, "", "a.toce@tnx.it", "ALTER TABLE codici_iva ADD COLUMN codice_readytec VARCHAR(3) NULL DEFAULT NULL", "Aggiunto campo di collegamento codice iva readytec");

            agg(259, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt add column prezzi_ivati char(1) not null default 'N'", "aggiunta gestione prezzi ivati");
            agg(260, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_fatt add column prezzo_ivato DECIMAL(15,5) NOT NULL DEFAULT 0", "aggiunta gestione prezzi ivati");
            agg(261, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt add column prezzi_ivati char(1) not null default 'N'", "aggiunta gestione prezzi ivati");
            agg(262, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt add column prezzo_ivato DECIMAL(15,5) NOT NULL DEFAULT 0", "aggiunta gestione prezzi ivati");
            agg(263, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi add column prezzi_ivati char(1) not null default 'N'", "aggiunta gestione prezzi ivati");
            agg(264, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ordi add column prezzo_ivato DECIMAL(15,5) NOT NULL DEFAULT 0", "aggiunta gestione prezzi ivati");
            agg(265, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt_acquisto add column prezzi_ivati char(1) not null default 'N'", "aggiunta gestione prezzi ivati");
            agg(266, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_fatt_acquisto add column prezzo_ivato DECIMAL(15,5) NOT NULL DEFAULT 0", "aggiunta gestione prezzi ivati");
            agg(267, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt_acquisto add column prezzi_ivati char(1) not null default 'N'", "aggiunta gestione prezzi ivati");
            agg(268, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt_acquisto add column prezzo_ivato DECIMAL(15,5) NOT NULL DEFAULT 0", "aggiunta gestione prezzi ivati");
            agg(269, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto add column prezzi_ivati char(1) not null default 'N'", "aggiunta gestione prezzi ivati");
            agg(270, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ordi_acquisto add column prezzo_ivato DECIMAL(15,5) NOT NULL DEFAULT 0", "aggiunta gestione prezzi ivati");
            agg(271, "", "m.ceccarelli@tnx.it", "ALTER TABLE tipi_listino add column prezzi_ivati char(1) NOT NULL DEFAULT 'N'", "aggiunta gestione prezzi ivati");

            agg(272, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt add column sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(273, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt add column sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(274, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi add column sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(275, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt_acquisto add column sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(276, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt_acquisto add column sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(277, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto add column sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");

            agg(278, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt add column totale_imponibile_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(279, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt add column totale_imponibile_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(280, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi add column totale_imponibile_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(281, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt_acquisto add column totale_imponibile_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(282, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt_acquisto add column totale_imponibile_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(283, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto add column totale_imponibile_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");

            agg(284, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt add column totale_ivato_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(285, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt add column totale_ivato_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(286, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi add column totale_ivato_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(287, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt_acquisto add column totale_ivato_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(288, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt_acquisto add column totale_ivato_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");
            agg(289, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto add column totale_ivato_pre_sconto  DECIMAL(15,5) not null default 0", "gestione sconto a importo");

            agg(290, "", "m.ceccarelli@tnx.it", "update righ_fatt r join codici_iva i on r.iva = i.codice set r.prezzo_ivato = r.prezzo * ((i.percentuale / 100) + 1)", "prezzi ivati");
            agg(291, "", "m.ceccarelli@tnx.it", "update righ_ddt r join codici_iva i on r.iva = i.codice set r.prezzo_ivato = r.prezzo * ((i.percentuale / 100) + 1)", "prezzi ivati");
            agg(292, "", "m.ceccarelli@tnx.it", "update righ_ordi r join codici_iva i on r.iva = i.codice set r.prezzo_ivato = r.prezzo * ((i.percentuale / 100) + 1)", "prezzi ivati");
            agg(293, "", "m.ceccarelli@tnx.it", "update righ_fatt_acquisto r join codici_iva i on r.iva = i.codice set r.prezzo_ivato = r.prezzo * ((i.percentuale / 100) + 1)", "prezzi ivati");
            agg(294, "", "m.ceccarelli@tnx.it", "update righ_ddt_acquisto r join codici_iva i on r.iva = i.codice set r.prezzo_ivato = r.prezzo * ((i.percentuale / 100) + 1)", "prezzi ivati");
            agg(295, "", "m.ceccarelli@tnx.it", "update righ_ordi_acquisto r join codici_iva i on r.iva = i.codice set r.prezzo_ivato = r.prezzo * ((i.percentuale / 100) + 1)", "prezzi ivati");

            agg(296, "", "m.ceccarelli@tnx.it", "update test_fatt set totale_imponibile_pre_sconto = totale_imponibile, totale_ivato_pre_sconto = totale", "gestione sconto a importo");
            agg(297, "", "m.ceccarelli@tnx.it", "update test_ddt set totale_imponibile_pre_sconto = totale_imponibile, totale_ivato_pre_sconto = totale", "gestione sconto a importo");
            agg(298, "", "m.ceccarelli@tnx.it", "update test_ordi set totale_imponibile_pre_sconto = totale_imponibile, totale_ivato_pre_sconto = totale", "gestione sconto a importo");
            agg(299, "", "m.ceccarelli@tnx.it", "update test_fatt_acquisto set totale_imponibile_pre_sconto = imponibile, totale_ivato_pre_sconto = importo", "gestione sconto a importo");
            agg(300, "", "m.ceccarelli@tnx.it", "update test_ddt_acquisto set totale_imponibile_pre_sconto = totale_imponibile, totale_ivato_pre_sconto = totale", "gestione sconto a importo");
            agg(301, "", "m.ceccarelli@tnx.it", "update test_ordi_acquisto set totale_imponibile_pre_sconto = totale_imponibile, totale_ivato_pre_sconto = totale", "gestione sconto a importo");

            if (main.getPersonalContain("peroni")) {
                agg(302, "", "m.ceccarelli@tnx.it", "alter table articoli add tipo_birra char(1) NULL DEFAULT ''", "peroni");
            }
            agg(303, "", "m.ceccarelli@tnx.it", "CREATE TABLE tipi_clie_forn (id varchar(10) NOT NULL PRIMARY KEY,  descrizione VARCHAR(255) NOT NULL default '') ENGINE=MyISAM", "Creazione tabella per tipi clie forn");
            if (main.getPersonalContain("peroni")) {
                agg(304, "", "m.ceccarelli@tnx.it", "INSERT INTO tipi_clie_forn SET id = 'RIS', descrizione = 'Ristorante'", "tipi clie forn");
                agg(305, "", "m.ceccarelli@tnx.it", "INSERT INTO tipi_clie_forn SET id = 'PIZ', descrizione = 'Pizzeria'", "tipi clie forn");
                agg(306, "", "m.ceccarelli@tnx.it", "INSERT INTO tipi_clie_forn SET id = 'BAR', descrizione = 'Bar tradizionale'", "tipi clie forn");
                agg(307, "", "m.ceccarelli@tnx.it", "INSERT INTO tipi_clie_forn SET id = 'BAR2', descrizione = 'Bar altro'", "tipi clie forn");
                agg(308, "", "m.ceccarelli@tnx.it", "INSERT INTO tipi_clie_forn SET id = 'PUB', descrizione = 'Pub/Birreria'", "tipi clie forn");
                agg(309, "", "m.ceccarelli@tnx.it", "INSERT INTO tipi_clie_forn SET id = 'HOT', descrizione = 'Hotel'", "tipi clie forn");
                agg(310, "", "m.ceccarelli@tnx.it", "INSERT INTO tipi_clie_forn SET id = '', descrizione = 'Altro'", "tipi clie forn");
            }
            agg(311, "", "m.ceccarelli@tnx.it", "alter table clie_forn add tipo2 varchar(10) NOT NULL DEFAULT ''", "tipo2 su clienti fornitori");

            /* non serve più funziona calcola_importo_netto
            try {
                if (!DbUtils.containRows(Db.getConn(), "show function status  where Db = '" + Db.dbNameDB + "' and Name = 'calcola_importo_netto'")) {
                    agg(312, "", "m.ceccarelli@tnx.it", "drop function if exists calcola_importo_netto", "funzione per calcolo importo netto riga");
                    agg(313, "", "m.ceccarelli@tnx.it", readfrom("agg_66_mc.sql"), "funzione per calcolo importo netto riga");
                }
            } catch (Exception e) {
                if (e.getMessage().indexOf("mysql.proc") >= 0) {
                    e.printStackTrace();
                    //SwingUtils.showErrorMessage(main.splash, "proc:" + e.getMessage(), "Errore", true);
                    //se in locale gli scarico i file necessari
                    if (!main.db_in_rete) {
                        String url = main.baseurlserver + "/download/invoicex/mysql/data/mysql";
                        String file = "proc.MYI";
                        System.out.println("scarico " + file);
                        HttpUtils.saveFile(url + "/" + file, "mysql/data/mysql/" + file);
                        file = "proc.MYD";
                        System.out.println("scarico " + file);
                        HttpUtils.saveFile(url + "/" + file, "mysql/data/mysql/" + file);
                        file = "proc.frm";
                        System.out.println("scarico " + file);
                        HttpUtils.saveFile(url + "/" + file, "mysql/data/mysql/" + file);
                        file = "procs_priv.MYI";
                        System.out.println("scarico " + file);
                        HttpUtils.saveFile(url + "/" + file, "mysql/data/mysql/" + file);
                        file = "procs_priv.MYD";
                        System.out.println("scarico " + file);
                        HttpUtils.saveFile(url + "/" + file, "mysql/data/mysql/" + file);
                        file = "procs_priv.frm";
                        System.out.println("scarico " + file);
                        HttpUtils.saveFile(url + "/" + file, "mysql/data/mysql/" + file);
                        main.fileIni.setValue("varie", "eseguito_download_mysql_proc", DateUtils.formatDateTime(new Date()));
                    }
                } else {
                    e.printStackTrace();
                }
            }
             */
            //lotti su ordini...
            agg(314, "", "m.ceccarelli@tnx.it", "CREATE TABLE righ_ordi_lotti (id_padre INT(11) NULL DEFAULT NULL, id INT(11) NOT NULL AUTO_INCREMENT, lotto VARCHAR(200) NULL DEFAULT NULL, codice_articolo VARCHAR(20) NULL DEFAULT NULL, qta DECIMAL(8,2) NULL DEFAULT NULL, matricola VARCHAR(255) NULL DEFAULT NULL, PRIMARY KEY (id)) ENGINE=MyISAM", "righ_ordi_lotti");
            agg(315, "", "m.ceccarelli@tnx.it", "CREATE TABLE righ_ordi_acquisto_lotti (id_padre INT(11) NULL DEFAULT NULL, id INT(11) NOT NULL AUTO_INCREMENT, lotto VARCHAR(200) NULL DEFAULT NULL, codice_articolo VARCHAR(20) NULL DEFAULT NULL, qta DECIMAL(8,2) NULL DEFAULT NULL, matricola VARCHAR(255) NULL DEFAULT NULL, PRIMARY KEY (id)) ENGINE=MyISAM", "righ_ordi_acquisto_lotti");

            //problema rivalsa!!!
            if (!main.pluginRitenute) {
                agg(316, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_fatt ADD COLUMN flag_rivalsa char(1) DEFAULT 'S'", "flag_rivalsa");
                agg(317, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_fatt" + nome_ricevute + " ADD COLUMN flag_rivalsa char(1) DEFAULT 'S'", "flag_rivalsa");
                agg(318, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt ADD COLUMN flag_rivalsa char(1) DEFAULT 'S'", "flag_rivalsa");
                agg(319, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt_acquisto ADD COLUMN flag_rivalsa char(1) DEFAULT 'S'", "flag_rivalsa");
                agg(320, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ordi ADD COLUMN flag_rivalsa char(1) DEFAULT 'S'", "flag_rivalsa");
                agg(321, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ordi_acquisto ADD COLUMN flag_rivalsa char(1) DEFAULT 'S'", "flag_rivalsa");
                agg(322, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt ADD COLUMN rivalsa int NULL", "rivalsa fatture vendita");
                agg(323, "", "m.ceccarelli@tnx.it", "ALTER TABLE " + testate_ricevute + " ADD COLUMN rivalsa int NULL", "rivalsa fatture acquisto");
            }
            agg(324, "", "m.ceccarelli@tnx.it", "ALTER TABLE `pagamenti`  ADD COLUMN `240` CHAR(1) NOT NULL DEFAULT '' AFTER `210`", "Aggiungo colonna 240gg");
            agg(325, "", "m.ceccarelli@tnx.it", "ALTER TABLE `pagamenti`  ADD COLUMN `270` CHAR(1) NOT NULL DEFAULT '' AFTER `240`", "Aggiungo colonna 270gg");
            agg(326, "", "m.ceccarelli@tnx.it", "ALTER TABLE `pagamenti`  ADD COLUMN `300` CHAR(1) NOT NULL DEFAULT '' AFTER `270`", "Aggiungo colonna 300gg");
            agg(327, "", "m.ceccarelli@tnx.it", "ALTER TABLE `pagamenti`  ADD COLUMN `330` CHAR(1) NOT NULL DEFAULT '' AFTER `300`", "Aggiungo colonna 330gg");
            agg(328, "", "m.ceccarelli@tnx.it", "ALTER TABLE `pagamenti`  ADD COLUMN `360` CHAR(1) NOT NULL DEFAULT '' AFTER `330`", "Aggiungo colonna 360gg");

            /* INIZIO MODIFICHE DB PER ACCESSO MULTIUTENTE */
            agg(324, "", "a.toce@tnx.it", "CREATE TABLE `accessi_utenti` (`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, `username` VARCHAR(150) NOT NULL, `password` VARCHAR(32) NOT NULL, `id_role` INT UNSIGNED NOT NULL, PRIMARY KEY (`id`)) ENGINE=MyISAM", "Aggiunta tabella utenti");
            agg(325, "", "a.toce@tnx.it", "CREATE TABLE `accessi_ruoli` (`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, `descrizione` VARCHAR(255) NOT NULL, PRIMARY KEY (`id`)) ENGINE=MyISAM", "Aggiunta tabella ruoli");
            agg(326, "", "a.toce@tnx.it", "CREATE TABLE `accessi_tipi_permessi` (`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, `descrizione` VARCHAR(255) NOT NULL, `lettura` INT(10) UNSIGNED NOT NULL DEFAULT '1', `scrittura` INT(10) UNSIGNED NOT NULL DEFAULT '1', `cancella` INT(10) UNSIGNED NOT NULL DEFAULT '1', PRIMARY KEY (`id`)) ENGINE=MyISAM", "Aggiunta tabella privilegi");
            agg(327, "", "a.toce@tnx.it", "CREATE TABLE `accessi_ruoli_permessi` (`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, `id_role` INT(10) UNSIGNED NOT NULL, `id_privilegio` INT(10) UNSIGNED NOT NULL, `lettura` INT(10) UNSIGNED NOT NULL DEFAULT '0', `scrittura` INT(10) UNSIGNED NOT NULL DEFAULT '0', `cancella` INT(10) UNSIGNED NOT NULL DEFAULT '0', PRIMARY KEY (`id`), UNIQUE INDEX `id_role_id_privilegio` (`id_role`, `id_privilegio`)) ENGINE=MyISAM", "Aggiunta tabella di collegamento fra ruoli e privilegi");

            agg(328, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 1, descrizione = 'Anagrafica Clienti'", "Aggiungo voci iniziali su gestione privilegi");
            agg(329, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 2, descrizione = 'Anagrafica Articoli e Listini'", "Aggiungo voci iniziali su gestione privilegi");
            agg(330, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 3, descrizione = 'Anagrafica Tipi di Pagamento'", "Aggiungo voci iniziali su gestione privilegi");
            agg(331, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 4, descrizione = 'Anagrafica Codici IVA'", "Aggiungo voci iniziali su gestione privilegi");
            agg(332, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 5, descrizione = 'Altre Anagrafiche'", "Aggiungo voci iniziali su gestione privilegi");
            agg(333, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 6, descrizione = 'Impostazioni', cancella = 0", "Aggiungo voci iniziali su gestione privilegi");
            agg(334, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 7, descrizione = 'Gestione Accesso Utenti'", "Aggiungo voci iniziali su gestione privilegi");
            agg(335, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 8, descrizione = 'Magazzino'", "Aggiungo voci iniziali su gestione privilegi");
            agg(336, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 9, descrizione = 'Statistiche', scrittura = 0, cancella = 0", "Aggiungo voci iniziali su gestione privilegi");
            agg(337, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 10, descrizione = 'Agente'", "Aggiungo voci iniziali su gestione privilegi");
            agg(338, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 11, descrizione = 'Preventivi/Ordini di Vendita'", "Aggiungo voci iniziali su gestione privilegi");
            agg(339, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 12, descrizione = 'DDT di Vendita'", "Aggiungo voci iniziali su gestione privilegi");
            agg(340, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 13, descrizione = 'Fatture di Vendita'", "Aggiungo voci iniziali su gestione privilegi");
            agg(341, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 14, descrizione = 'Preventivi/Ordini di Acquisto'", "Aggiungo voci iniziali su gestione privilegi");
            agg(342, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 15, descrizione = 'DDT di Acquisto'", "Aggiungo voci iniziali su gestione privilegi");
            agg(343, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 16, descrizione = 'Fatture di Acquisto'", "Aggiungo voci iniziali su gestione privilegi");

            agg(344, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli SET id = 1, descrizione = 'Administrator'", "Aggiungo ruolo amministratore");
            agg(345, "", "a.toce@tnx.it", "INSERT INTO accessi_utenti SET id = 1, username = 'admin', password = " + Db.pc(InvoicexUtil.md5("admin"), Types.VARCHAR) + ", id_role = '1'", "Aggiungo utente amministratore con password admin");

            agg(346, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 1, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(347, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 2, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(348, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 3, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(349, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 4, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(350, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 5, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(351, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 6, lettura = '1', scrittura = '1', cancella = '0'", "Aggiungo privilegi completi per utente admin");
            agg(352, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 7, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(353, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 8, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(354, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 9, lettura = '1', scrittura = '0', cancella = '0'", "Aggiungo privilegi completi per utente admin");
            agg(355, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 10, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(356, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 11, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(357, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 12, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(358, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 13, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(359, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 14, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(360, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 15, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(361, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 16, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");

            agg(362, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 17, descrizione = 'Gestione Pagamenti e Scadenzario'", "Aggiungo voci iniziali su gestione privilegi");
            agg(363, "", "a.toce@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 18, descrizione = 'Gestione Iva', scrittura = 0, cancella = 0", "Aggiungo voci iniziali su gestione privilegi");
            agg(364, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 17, lettura = '1', scrittura = '1', cancella = '1'", "Aggiungo privilegi completi per utente admin");
            agg(365, "", "a.toce@tnx.it", "INSERT INTO accessi_ruoli_permessi SET id_role = 1, id_privilegio = 18, lettura = '1', scrittura = '0', cancella = '0'", "Aggiungo privilegi completi per utente admin");

            agg(366, "", "a.toce@tnx.it", "CREATE TABLE accessi_log (id int(10) unsigned NOT NULL AUTO_INCREMENT, utente varchar(255) NOT NULL, timestamp_login timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (id)) ENGINE=MyISAM", "Creo tabella di log per accessi");

            agg(329, "", "m.ceccarelli@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN provvigioni_tipo_data varchar(20) NOT NULL DEFAULT 'data_scadenza'", "tipo di generazione delle provvigioni se con data scadenza o data fattura");
            agg(330, "", "m.ceccarelli@tnx.it", "alter table stampa_iva_semplice add column data_doc DATE NULL DEFAULT NULL", "data documento su stampa reg. iva");
            agg(331, "", "m.ceccarelli@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN gestione_utenti int NULL DEFAULT 0", "gestione utenti");

            agg(332, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli` (`id`, `descrizione`) VALUES (2, 'Standard (accesso completo eccetto impostazioni)')", "gestione utenti");
            agg(333, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli` (`id`, `descrizione`) VALUES (3, 'Sola lettura')", "gestione utenti");
            agg(334, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli` (`id`, `descrizione`) VALUES (4, 'DDT e Magazzino')", "gestione utenti");

            agg(335, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 1, 1, 1, 1);", "gestione utenti");
            agg(336, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 2, 1, 1, 1);", "gestione utenti");
            agg(337, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 3, 1, 1, 1);", "gestione utenti");
            agg(338, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 4, 1, 1, 1);", "gestione utenti");
            agg(339, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 5, 1, 1, 1);", "gestione utenti");
            agg(340, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 6, 1, 0, 0);", "gestione utenti");
            agg(341, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 7, 1, 0, 0);", "gestione utenti");
            agg(342, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 8, 1, 1, 1);", "gestione utenti");
            agg(343, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 9, 1, 0, 0);", "gestione utenti");
            agg(344, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 10, 1, 1, 1);", "gestione utenti");
            agg(345, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 11, 1, 1, 1);", "gestione utenti");
            agg(346, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 12, 1, 1, 1);", "gestione utenti");
            agg(347, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 13, 1, 1, 1);", "gestione utenti");
            agg(348, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 14, 1, 1, 1);", "gestione utenti");
            agg(349, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 15, 1, 1, 1);", "gestione utenti");
            agg(350, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 16, 1, 1, 1);", "gestione utenti");
            agg(351, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 17, 1, 1, 1);", "gestione utenti");
            agg(352, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 2, 18, 1, 0, 0);", "gestione utenti");
            agg(353, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 1, 1, 0, 0);", "gestione utenti");
            agg(354, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 2, 1, 0, 0);", "gestione utenti");
            agg(355, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 3, 1, 0, 0);", "gestione utenti");
            agg(356, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 4, 1, 0, 0);", "gestione utenti");
            agg(357, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 5, 1, 0, 0);", "gestione utenti");
            agg(358, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 6, 1, 0, 0);", "gestione utenti");
            agg(359, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 7, 1, 0, 0);", "gestione utenti");
            agg(360, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 8, 1, 0, 0);", "gestione utenti");
            agg(361, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 9, 1, 0, 0);", "gestione utenti");
            agg(362, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 10, 1, 0, 0);", "gestione utenti");
            agg(363, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 11, 1, 0, 0);", "gestione utenti");
            agg(364, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 12, 1, 0, 0);", "gestione utenti");
            agg(365, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 13, 1, 0, 0);", "gestione utenti");
            agg(366, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 14, 1, 0, 0);", "gestione utenti");
            agg(367, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 15, 1, 0, 0);", "gestione utenti");
            agg(368, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 16, 1, 0, 0);", "gestione utenti");
            agg(369, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 17, 1, 0, 0);", "gestione utenti");
            agg(370, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 3, 18, 1, 0, 0);", "gestione utenti");
            agg(371, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 1, 0, 0, 0);", "gestione utenti");
            agg(372, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 2, 0, 0, 0);", "gestione utenti");
            agg(373, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 3, 0, 0, 0);", "gestione utenti");
            agg(374, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 4, 0, 0, 0);", "gestione utenti");
            agg(375, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 5, 0, 0, 0);", "gestione utenti");
            agg(376, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 6, 0, 0, 0);", "gestione utenti");
            agg(377, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 7, 0, 0, 0);", "gestione utenti");
            agg(378, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 8, 1, 1, 1);", "gestione utenti");
            agg(379, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 9, 0, 0, 0);", "gestione utenti");
            agg(380, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 10, 0, 0, 0);", "gestione utenti");
            agg(381, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 11, 0, 0, 0);", "gestione utenti");
            agg(382, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 12, 1, 1, 1);", "gestione utenti");
            agg(383, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 13, 0, 0, 0);", "gestione utenti");
            agg(384, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 14, 0, 0, 0);", "gestione utenti");
            agg(385, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 15, 1, 1, 1);", "gestione utenti");
            agg(386, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 16, 0, 0, 0);", "gestione utenti");
            agg(387, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 17, 0, 0, 0);", "gestione utenti");
            agg(388, "", "m.ceccarelli@tnx.it", "INSERT INTO `accessi_ruoli_permessi` (`id_role`, `id_privilegio`, `lettura`, `scrittura`, `cancella`) VALUES ( 4, 18, 0, 0, 0);", "gestione utenti");

            agg(389, "", "a.toce@tnx.it", "ALTER TABLE righ_ddt ADD COLUMN numero_casse INT NULL DEFAULT NULL", "Aggiunto numero casse su righe DDT");
            agg(390, "", "a.toce@tnx.it", "ALTER TABLE righ_fatt ADD COLUMN numero_casse INT NULL DEFAULT NULL", "Aggiunto numero casse su righe DDT");
            agg(391, "", "a.toce@tnx.it", "ALTER TABLE righ_ordi ADD COLUMN numero_casse INT NULL DEFAULT NULL", "Aggiunto numero casse su righe DDT");

            agg(392, "", "a.toce@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `label_cliente_eng` VARCHAR(100) DEFAULT 'Dear'", "Etichetta cliente");
            agg(393, "", "a.toce@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `label_destinazione_eng` VARCHAR(100) DEFAULT 'Destination'", "Etichetta destinazione");

            agg(389, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `stampare_timbro_firma` VARCHAR(100) DEFAULT 'Non stampare mai'", "timbro firma");
            agg(390, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `testo_timbro_firma` TEXT", "timbro firma");
            agg(391, "", "m.ceccarelli@tnx.it", "update `dati_azienda` set testo_timbro_firma = '<html>\\n<center>\\n<b>\\n      <br>\\n           Data                                         Timbro e Firma<br>\\n          _________                                ____________________<br>\\n<br>\\n</center>\\n</html>'", "timbro firma");

            agg(392, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt CHANGE COLUMN quantita_evasa quantita_evasa DECIMAL(15,5) NULL DEFAULT NULL;", "qta evasa decimali");
            agg(393, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt_acquisto CHANGE COLUMN quantita_evasa quantita_evasa DECIMAL(15,5) NULL DEFAULT NULL;", "qta evasa decimali");
            agg(394, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ordi CHANGE COLUMN quantita_evasa quantita_evasa DECIMAL(15,5) NULL DEFAULT NULL;", "qta evasa decimali");
            agg(395, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ordi_acquisto CHANGE COLUMN quantita_evasa quantita_evasa DECIMAL(15,5) NULL DEFAULT NULL;", "qta evasa decimali");

            agg(396, "", "m.ceccarelli@tnx.it", "create table tipi_aspetto_esteriore_beni (id int auto_increment not null primary key, nome varchar(100)) ENGINE=MyISAM", "tabella tipi_aspetto_esteriore_beni");
            agg(397, "", "m.ceccarelli@tnx.it", "insert into tipi_aspetto_esteriore_beni (nome) values ('SCATOLA')", "tabella tipi_aspetto_esteriore_beni");
            agg(398, "", "m.ceccarelli@tnx.it", "insert into tipi_aspetto_esteriore_beni (nome) values ('A VISTA')", "tabella tipi_aspetto_esteriore_beni");
            agg(399, "", "m.ceccarelli@tnx.it", "insert into tipi_aspetto_esteriore_beni (nome) values ('SCATOLA IN PANCALE')", "tabella tipi_aspetto_esteriore_beni");
            agg(400, "", "m.ceccarelli@tnx.it", "insert into tipi_aspetto_esteriore_beni (nome) values ('BUSTA')", "tabella tipi_aspetto_esteriore_beni");
            agg(401, "", "m.ceccarelli@tnx.it", "insert into tipi_aspetto_esteriore_beni (nome) values ('CARTONE')", "tabella tipi_aspetto_esteriore_beni");

            agg(402, "", "m.ceccarelli@tnx.it", "alter table dati_azienda add column tipo_numerazione int not null default 0", "dati azienda tipo numerazione");
            agg(403, "", "m.ceccarelli@tnx.it", "alter table dati_azienda add column tipo_numerazione_confermata int not null default 0", "dati azienda tipo numerazione");
            agg(404, "", "m.ceccarelli@tnx.it", "alter table dati_azienda add column tipo_numerazione_confermata2 int not null default 0", "dati azienda tipo numerazione");

            //test debug per errore colonna già presente
//            agg(405, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `testo_timbro_firma` TEXT", "timbro firma");
            agg(406, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt_acquisto add index serie_numero_anno (serie, numero, anno)", "indici per scadenze");
            agg(407, "", "m.ceccarelli@tnx.it", "ALTER TABLE scadenze add index serie_numero_anno (documento_serie, documento_numero, documento_anno)", "indici per scadenze");
            agg(408, "", "m.ceccarelli@tnx.it", "alter table scadenze_parziali add index id_scadenza (id_scadenza)", "indici per scadenze");
            agg(409, "", "m.ceccarelli@tnx.it", "alter table scadenze add index documento_tipo (documento_tipo)", "indici per scadenze");
            agg(410, "", "m.ceccarelli@tnx.it", "alter table scadenze add index data_scadenza (data_scadenza)", "indici per scadenze");
            agg(411, "", "m.ceccarelli@tnx.it", "alter table clie_forn add index ragione_sociale (ragione_sociale)", "indici per scadenze");

            agg(412, "", "m.ceccarelli@tnx.it", "ALTER TABLE pagamenti ADD COLUMN id_pagamento_teamsystem INT NULL DEFAULT NULL", "Aggiunto campo di collegamento com teamsystem");
            agg(413, "", "m.ceccarelli@tnx.it", "ALTER TABLE pagamenti ADD COLUMN tipo_effetto_teamsystem INT NULL DEFAULT NULL", "Aggiunto campo di collegamento com teamsystem");

            agg(414, "", "m.ceccarelli@tnx.it", "alter table pacchetti_articoli CHANGE COLUMN quantita quantita DECIMAL(15,5) NOT NULL", "qta kit");

            agg(415, "", "m.ceccarelli@tnx.it", "ALTER TABLE clie_forn CHANGE COLUMN email email TEXT NULL DEFAULT NULL", "campo email più lungo");

            agg(416, "", "m.ceccarelli@tnx.it", "ALTER TABLE clie_forn ADD COLUMN `provvigione_predefinita_cliente` DECIMAL(5,2) NULL DEFAULT NULL AFTER `agente`", "provvigioni agenti");
            agg(417, "", "m.ceccarelli@tnx.it", "ALTER TABLE clie_forn ADD COLUMN `provvigione_predefinita_fornitore` DECIMAL(5,2) NULL DEFAULT NULL AFTER `provvigione_predefinita_cliente`", "provvigioni agenti");

            agg(418, "", "m.ceccarelli@tnx.it", "ALTER TABLE `articoli` CHANGE COLUMN `fornitore` `fornitore_old` VARCHAR(10) NULL DEFAULT NULL", "provvigioni agenti per articolo fornitore");
            agg(419, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD COLUMN `fornitore` INT UNSIGNED NULL DEFAULT NULL AFTER `fornitore_old`", "provvigioni agenti per articolo fornitore");
            agg(420, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD COLUMN `disponibilita_reale` decimal(15,5) NULL DEFAULT NULL", "giacenza articolo");
            agg(421, "", "m.ceccarelli@tnx.it", "ALTER TABLE articoli ADD COLUMN `disponibilita_reale_ts` TIMESTAMP NULL DEFAULT NULL AFTER disponibilita_reale", "giacenza articolo");
            agg(422, "", "m.ceccarelli@tnx.it", "ALTER TABLE movimenti_magazzino ADD COLUMN `modificato_ts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP", "giacenza articolo");

            agg(423, "", "m.ceccarelli@tnx.it", "CREATE TABLE movimenti_magazzino_eliminati LIKE movimenti_magazzino ENGINE=MyISAM", "giacenza articolo");

            agg(424, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add column prezzo_netto_unitario decimal(15,5)", "prezzo netto unitario");
            agg(425, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add column prezzo_netto_unitario decimal(15,5)", "prezzo netto unitario");
            agg(426, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add column prezzo_netto_unitario decimal(15,5)", "prezzo netto unitario");
            agg(427, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add column prezzo_netto_unitario decimal(15,5)", "prezzo netto unitario");
            agg(428, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add column prezzo_netto_unitario decimal(15,5)", "prezzo netto unitario");
            agg(429, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto add column prezzo_netto_unitario decimal(15,5)", "prezzo netto unitario");

            agg(430, "", "m.ceccarelli@tnx.it", "alter table clie_forn add column produttore int null", "produttore");

            String sql = "select export_fatture_estrai_scadenze from dati_azienda";
            try {
                DbUtils.tryOpenResultSet(Db.getConn(), sql);
            } catch (Exception e) {
                try {
                    DbUtils.tryExecQuery(Db.getConn(), "ALTER TABLE `dati_azienda` ADD COLUMN `export_fatture_codice_iva` varchar(50) NULL");
                } catch (Exception e2) {
                }
                try {
                    DbUtils.tryExecQuery(Db.getConn(), "ALTER TABLE `dati_azienda` ADD COLUMN `export_fatture_conto_ricavi` varchar(50) NULL");
                } catch (Exception e2) {
                }
                try {
                    DbUtils.tryExecQuery(Db.getConn(), "ALTER TABLE `dati_azienda` ADD COLUMN `export_fatture_estrai_scadenze` char(1) NULL");
                } catch (Exception e2) {
                }

                //riporto i dati dal file ini
                try {
                    sql = "update dati_azienda set export_fatture_codice_iva = " + Db.pc(main.fileIni.getValue("readytec", "codiceIvaDefault", ""), Types.VARCHAR);
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                } catch (Exception e2) {
                }
                try {
                    sql = "update dati_azienda set export_fatture_conto_ricavi = " + Db.pc(main.fileIni.getValue("readytec", "codiceContoDefault", ""), Types.VARCHAR);
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                } catch (Exception e2) {
                }
                try {
                    sql = "update dati_azienda set export_fatture_estrai_scadenze = 'S'";
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                } catch (Exception e2) {
                }
            }

            agg(431, "", "m.ceccarelli@tnx.it", "update test_fatt set totale_da_pagare = totale where tipo_fattura = 7", "update per scontrini");

            String view1 = "create view v_righ_tutte as "
                    + " SELECT 'v' as tabella, r.id, r.id_padre, t.data, t.anno, t.numero, t.serie, r.riga, r.codice_articolo, r.descrizione\n"
                    + ", r.quantita, r.prezzo, r.prezzo_netto_unitario, r.sconto1, r.sconto2, t.sconto1 as sconto1t, t.sconto2 as sconto2t, t.sconto3 as sconto3t\n"
                    + ", c.codice as clifor, c.ragione_sociale\n"
                    + "from righ_fatt r \n"
                    + "join test_fatt t on r.id_padre = t.id\n"
                    + "join clie_forn c on t.cliente = c.codice\n"
                    + "union all \n"
                    + "SELECT 'a' as tabella, r.id, r.id_padre, t.data, t.anno, t.numero, t.serie, r.riga, r.codice_articolo, r.descrizione\n"
                    + ", r.quantita, r.prezzo, r.prezzo_netto_unitario, r.sconto1, r.sconto2, t.sconto1 as sconto1t, t.sconto2 as sconto2t, t.sconto3 as sconto3t\n"
                    + ", c.codice as clifor, c.ragione_sociale\n"
                    + "from righ_fatt_acquisto r \n"
                    + "join test_fatt_acquisto t on r.id_padre = t.id\n"
                    + "join clie_forn c on t.fornitore = c.codice";

            agg(432, "", "m.ceccarelli@tnx.it", view1, "vista per ultimi prezzi");

            agg(433, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add column data_consegna_prevista date", "data consegna prevista");
            agg(434, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add column data_consegna_prevista date", "data consegna prevista");
            agg(435, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add column data_consegna_prevista date", "data consegna prevista");
            agg(436, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add column data_consegna_prevista date", "data consegna prevista");
            agg(437, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add column data_consegna_prevista date", "data consegna prevista");
            agg(438, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto add column data_consegna_prevista date", "data consegna prevista");

            agg(439, "", "m.ceccarelli@tnx.it", "alter table test_ordi add column data_consegna_prevista date", "data consegna prevista");
            agg(440, "", "m.ceccarelli@tnx.it", "alter table test_ddt add column data_consegna_prevista date", "data consegna prevista");
            agg(441, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column data_consegna_prevista date", "data consegna prevista");
            agg(442, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add column data_consegna_prevista date", "data consegna prevista");
            agg(443, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add column data_consegna_prevista date", "data consegna prevista");
            agg(444, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add column data_consegna_prevista date", "data consegna prevista");

            agg(445, "", "m.ceccarelli@tnx.it", "create table tipi_consegna (id int auto_increment not null primary key, nome varchar(100)) ENGINE=MyISAM", "tabella tipi consegna");
            agg(446, "", "m.ceccarelli@tnx.it", "insert into tipi_consegna (nome) values ('MITTENTE')", "tabella tipi consegna");
            agg(447, "", "m.ceccarelli@tnx.it", "insert into tipi_consegna (nome) values ('VETTORE')", "tabella tipi consegna");
            agg(448, "", "m.ceccarelli@tnx.it", "insert into tipi_consegna (nome) values ('DESTINATARIO')", "tabella tipi consegna");

            agg(449, "", "m.ceccarelli@tnx.it", "create table tipi_scarico (id int auto_increment not null primary key, nome varchar(100)) ENGINE=MyISAM", "tabella tipi scarico");
            agg(450, "", "m.ceccarelli@tnx.it", "insert into tipi_scarico (nome) values ('CON MULETTO')", "tabella tipi scarico");
            agg(451, "", "m.ceccarelli@tnx.it", "insert into tipi_scarico (nome) values ('A MANO')", "tabella tipi scarico");

            agg(452, "", "m.ceccarelli@tnx.it", "ALTER TABLE stati ADD UNIQUE INDEX indice_codice1 (codice1)", "indice univoco per nazioni");

            agg(453, "", "m.ceccarelli@tnx.it", "ALTER TABLE clie_forn ADD modalita_consegna int", "consegna");
            agg(454, "", "m.ceccarelli@tnx.it", "ALTER TABLE clie_forn ADD modalita_scarico int", "scarico");

            agg(455, "", "m.ceccarelli@tnx.it", "alter table test_ordi add column modalita_consegna int", "consegna");
            agg(456, "", "m.ceccarelli@tnx.it", "alter table test_ddt add column modalita_consegna int", "consegna");
            agg(457, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column modalita_consegna int", "consegna");
            agg(458, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add column modalita_consegna int", "consegna");
            agg(459, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add column modalita_consegna int", "consegna");
            agg(460, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add column modalita_consegna int", "consegna");

            agg(461, "", "m.ceccarelli@tnx.it", "alter table test_ordi add column modalita_scarico int", "consegna");
            agg(462, "", "m.ceccarelli@tnx.it", "alter table test_ddt add column modalita_scarico int", "consegna");
            agg(463, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column modalita_scarico int", "consegna");
            agg(464, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add column modalita_scarico int", "consegna");
            agg(465, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add column modalita_scarico int", "consegna");
            agg(466, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add column modalita_scarico int", "consegna");

            //numerazione, se hanno scelto bene, altrimenti forzo a numerazione come prima
            if (!InvoicexUtil.isSceltaTipoNumerazioneEseguita()) {
                agg(467, "", "m.ceccarelli@tnx.it", "update dati_azienda set tipo_numerazione = " + InvoicexUtil.TIPO_NUMERAZIONE_ANNO_SOLO_NUMERO + ", tipo_numerazione_confermata = 1", "numerazione");
            }

            agg(468, "", "m.ceccarelli@tnx.it", "update test_ordi set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(469, "", "m.ceccarelli@tnx.it", "update righ_ordi r join test_ordi t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");
            agg(470, "", "m.ceccarelli@tnx.it", "update test_ddt set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(471, "", "m.ceccarelli@tnx.it", "update righ_ddt r join test_ddt t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");
            agg(472, "", "m.ceccarelli@tnx.it", "update test_fatt set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(473, "", "m.ceccarelli@tnx.it", "update righ_fatt r join test_fatt t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");

            agg(474, "", "m.ceccarelli@tnx.it", "update test_ordi_acquisto set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(475, "", "m.ceccarelli@tnx.it", "update righ_ordi_acquisto r join test_ordi_acquisto t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");
            agg(476, "", "m.ceccarelli@tnx.it", "update test_ddt_acquisto set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(477, "", "m.ceccarelli@tnx.it", "update righ_ddt_acquisto r join test_ddt_acquisto t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");
            agg(478, "", "m.ceccarelli@tnx.it", "update test_fatt_acquisto set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(479, "", "m.ceccarelli@tnx.it", "update righ_fatt_acquisto r join test_fatt_acquisto t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");
            agg(480, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_fatt_acquisto` ADD COLUMN `color` VARCHAR(20) DEFAULT ''", "Aggiunta colonna per segnalazione colore");
            agg(481, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ordi` ADD COLUMN `color` VARCHAR(20) DEFAULT ''", "Aggiunta colonna per segnalazione colore");
            agg(482, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ordi_acquisto` ADD COLUMN `color` VARCHAR(20) DEFAULT ''", "Aggiunta colonna per segnalazione colore");
            agg(483, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ddt` ADD COLUMN `color` VARCHAR(20) DEFAULT ''", "Aggiunta colonna per segnalazione colore");
            agg(484, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ddt_acquisto` ADD COLUMN `color` VARCHAR(20) DEFAULT ''", "Aggiunta colonna per segnalazione colore");

            //forzo passaggio iva
            if (!InvoicexUtil.isPassaggio21eseguito()) {
                try {
                    if (!(DbUtils.containRows(gestioneFatture.Db.getConn(), "select * from codici_iva where codice = '21'"))) {
                        DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), "insert into codici_iva set codice = '21', percentuale = 21, descrizione = 'Iva 21%', descrizione_breve = 'Iva 21%'");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (DbUtils.containRows(gestioneFatture.Db.getConn(), "select * from codici_iva where codice = '21'")) {
                        //articoli
                        try {
                            DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), "update articoli set iva = 21 where iva = 20");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //clienti e fornitori
                        try {
                            DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), "update clie_forn set iva_standard = 21 where iva_standard = 20");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //impostazioni
                        try {
                            if (CastUtils.toString(InvoicexUtil.getIvaDefault()).equals("20")) {
                                DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), "update dati_azienda set codiceIvaDefault = '21'");
                                main.fileIni.setValue("iva", "codiceIvaDefault", "21");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if (CastUtils.toString(InvoicexUtil.getIvaSpese()).equals("20")) {
                                DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), "update dati_azienda set codiceIvaSpese = '21'");
                                main.fileIni.setValue("iva", "codiceIvaSpese", "21");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                main.fileIni.setValue("iva21", "eseguito", true);
                try {
                    DbUtils.tryExecQuery(gestioneFatture.Db.getConn(), "update dati_azienda set iva21eseguito = 'S'");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            agg(485, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add column totale_imponibile_netto decimal(15,5)", "prezzo netto unitario");
            agg(486, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add column totale_imponibile_netto decimal(15,5)", "prezzo netto unitario");
            agg(487, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add column totale_imponibile_netto decimal(15,5)", "prezzo netto unitario");
            agg(488, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add column totale_imponibile_netto decimal(15,5)", "prezzo netto unitario");
            agg(489, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add column totale_imponibile_netto decimal(15,5)", "prezzo netto unitario");
            agg(490, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto add column totale_imponibile_netto decimal(15,5)", "prezzo netto unitario");

            agg(491, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add column totale_iva_netto decimal(15,5)", "prezzo netto unitario");
            agg(492, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add column totale_iva_netto decimal(15,5)", "prezzo netto unitario");
            agg(493, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add column totale_iva_netto decimal(15,5)", "prezzo netto unitario");
            agg(494, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add column totale_iva_netto decimal(15,5)", "prezzo netto unitario");
            agg(495, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add column totale_iva_netto decimal(15,5)", "prezzo netto unitario");
            agg(496, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto add column totale_iva_netto decimal(15,5)", "prezzo netto unitario");

            agg(497, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add column totale_ivato_netto decimal(15,5)", "prezzo netto unitario");
            agg(498, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add column totale_ivato_netto decimal(15,5)", "prezzo netto unitario");
            agg(499, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add column totale_ivato_netto decimal(15,5)", "prezzo netto unitario");
            agg(500, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add column totale_ivato_netto decimal(15,5)", "prezzo netto unitario");
            agg(501, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add column totale_ivato_netto decimal(15,5)", "prezzo netto unitario");
            agg(502, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto add column totale_ivato_netto decimal(15,5)", "prezzo netto unitario");

            agg(503, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add column prezzo_ivato_netto_unitario decimal(15,5)", "prezzo netto unitario");
            agg(504, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add column prezzo_ivato_netto_unitario decimal(15,5)", "prezzo netto unitario");
            agg(505, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add column prezzo_ivato_netto_unitario decimal(15,5)", "prezzo netto unitario");
            agg(506, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add column prezzo_ivato_netto_unitario decimal(15,5)", "prezzo netto unitario");
            agg(507, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add column prezzo_ivato_netto_unitario decimal(15,5)", "prezzo netto unitario");
            agg(508, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto add column prezzo_ivato_netto_unitario decimal(15,5)", "prezzo netto unitario");

            agg(509, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add column prezzo_netto_totale decimal(15,5)", "prezzo netto totale");
            agg(510, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add column prezzo_netto_totale decimal(15,5)", "prezzo netto totale");
            agg(511, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add column prezzo_netto_totale decimal(15,5)", "prezzo netto totale");
            agg(512, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add column prezzo_netto_totale decimal(15,5)", "prezzo netto totale");
            agg(513, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add column prezzo_netto_totale decimal(15,5)", "prezzo netto totale");
            agg(514, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto add column prezzo_netto_totale decimal(15,5)", "prezzo netto totale");

            agg(515, "", "m.ceccarelli@tnx.it", "drop view if exists v_righ_tutte", "vista per ultimi prezzi");
            agg(516, "", "m.ceccarelli@tnx.it", view1, "vista per ultimi prezzi");

            agg(517, "", "m.ceccarelli@tnx.it", "update clie_forn set provvigione_predefinita_cliente = null where provvigione_predefinita_cliente = 0", "update prov a 0");
            agg(518, "", "m.ceccarelli@tnx.it", "update clie_forn set provvigione_predefinita_fornitore = null where provvigione_predefinita_fornitore = 0", "update prov a 0");
            agg(519, "", "m.ceccarelli@tnx.it", "CREATE TABLE `attivazione` (\n"
                    + "	`codice` VARCHAR(50) NOT NULL,\n"
                    + "	`versione` VARCHAR(50) NULL DEFAULT NULL,\n"
                    + "	`esito_log` VARCHAR(250) NULL DEFAULT NULL,\n"
                    + "	`ts` TIMESTAMP NULL DEFAULT NULL,\n"
                    + "	PRIMARY KEY (`codice`)\n"
                    + ") ENGINE=MyISAM\n", "attivazione");

            //il 520 è servito per l'attivazione, vedi dopo esegui_aggs
            agg(521, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `iva22eseguito` CHAR(1)", "iva22eseguito");
            agg(522, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `iva22a21eseguito` CHAR(1)", "iva22a21eseguito");

            //tornato fuori il problema dell'anno in alcune righe, replico update dopo aver messo ulteriore controllo in salvataggio documenti
            agg(523, "", "m.ceccarelli@tnx.it", "update test_ordi set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(524, "", "m.ceccarelli@tnx.it", "update righ_ordi r join test_ordi t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");
            agg(525, "", "m.ceccarelli@tnx.it", "update test_ddt set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(526, "", "m.ceccarelli@tnx.it", "update righ_ddt r join test_ddt t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");
            agg(527, "", "m.ceccarelli@tnx.it", "update test_fatt set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(528, "", "m.ceccarelli@tnx.it", "update righ_fatt r join test_fatt t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");

            agg(529, "", "m.ceccarelli@tnx.it", "update test_ordi_acquisto set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(530, "", "m.ceccarelli@tnx.it", "update righ_ordi_acquisto r join test_ordi_acquisto t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");
            agg(531, "", "m.ceccarelli@tnx.it", "update test_ddt_acquisto set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(532, "", "m.ceccarelli@tnx.it", "update righ_ddt_acquisto r join test_ddt_acquisto t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");
            agg(533, "", "m.ceccarelli@tnx.it", "update test_fatt_acquisto set anno = year(data) where anno is null or anno != year(data)", "anno in data");
            agg(534, "", "m.ceccarelli@tnx.it", "update righ_fatt_acquisto r join test_fatt_acquisto t on r.id_padre = t.id set r.anno = year(t.data) where r.anno is null or r.anno != year(t.data)", "anno in data");

            agg(535, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add column prezzo_ivato_netto_totale decimal(15,5)", "prezzo netto totale");
            agg(536, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add column prezzo_ivato_netto_totale decimal(15,5)", "prezzo netto totale");
            agg(537, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add column prezzo_ivato_netto_totale decimal(15,5)", "prezzo netto totale");
            agg(538, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add column prezzo_ivato_netto_totale decimal(15,5)", "prezzo netto totale");
            agg(539, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add column prezzo_ivato_netto_totale decimal(15,5)", "prezzo netto totale");
            agg(540, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto add column prezzo_ivato_netto_totale decimal(15,5)", "prezzo netto totale");

            agg(541, "", "m.ceccarelli@tnx.it", "CREATE TABLE `categorie_articoli` (\n"
                    + "	`id` INT(11) NOT NULL AUTO_INCREMENT,\n"
                    + "	`categoria` VARCHAR(250) NULL DEFAULT NULL,\n"
                    + "	PRIMARY KEY (`id`), UNIQUE INDEX `categoria` (`categoria`)) ENGINE=MyISAM", "categorie articoli");
            agg(542, "", "m.ceccarelli@tnx.it", "CREATE TABLE `sottocategorie_articoli` (\n"
                    + "	`id` INT(11) NOT NULL AUTO_INCREMENT,\n"
                    + "	`id_padre` INT(11) NULL DEFAULT NULL,\n"
                    + "	`sottocategoria` VARCHAR(250) NULL DEFAULT NULL,\n"
                    + "	PRIMARY KEY (`id`), UNIQUE INDEX `id_padre_sottocategoria` (`id_padre`, `sottocategoria`)) ENGINE=MyISAM", "sottocategorie articoli");
            agg(543, "", "m.ceccarelli@tnx.it", "alter table articoli add column categoria int null", "categeroia");
            agg(544, "", "m.ceccarelli@tnx.it", "alter table articoli add column sottocategoria int null", "sottocategeroia");
            agg(545, "", "m.ceccarelli@tnx.it", "CREATE TABLE `clie_forn_agenti` (\n"
                    + "	`id` INT(11) NOT NULL AUTO_INCREMENT,\n"
                    + "	`id_clifor` INT(11) NULL DEFAULT NULL,\n"
                    + "	`id_agente` INT(11) NULL DEFAULT NULL,\n"
                    + "	provvigione DECIMAL(5,2) NULL,\n"
                    + "	PRIMARY KEY (`id`),\n"
                    + "	UNIQUE INDEX `id_clifor_id_agente` (`id_clifor`, `id_agente`)) ENGINE=MyISAM", "clifor agenti");

            agg(546, "", "m.ceccarelli@tnx.it", "alter table stampa_iva_semplice add column id_fattura int NULL DEFAULT NULL", "id del documento");
            agg(547, "", "m.ceccarelli@tnx.it", "alter table stampa_iva_semplice add column id_clifor int NULL DEFAULT NULL", "id del clifor");
            agg(548, "", "m.ceccarelli@tnx.it", "alter table stampa_iva_semplice add column piva_cfiscale varchar(20) NULL DEFAULT NULL", "piva o cfiscale");
            //549 prenotato per provvigioni, vedi sotto
            agg(550, "", "m.ceccarelli@tnx.it", "alter table movimenti_magazzino add prezzo_medio decimal(15,5) null", "nuovi campi mov mag");
            agg(551, "", "m.ceccarelli@tnx.it", "alter table movimenti_magazzino add da_id_riga int null", "nuovi campi mov mag");
            agg(552, "", "m.ceccarelli@tnx.it", "alter table movimenti_magazzino_eliminati add prezzo_medio decimal(15,5) null", "nuovi campi mov mag");
            agg(553, "", "m.ceccarelli@tnx.it", "alter table movimenti_magazzino_eliminati add da_id_riga int null", "nuovi campi mov mag");

            agg(554, "", "m.ceccarelli@tnx.it", "alter table tipi_fatture add segno tinyint null", "segno per note di credito");
            agg(555, "", "m.ceccarelli@tnx.it", "update tipi_fatture set segno = 1", "segno per note di credito");
            agg(556, "", "m.ceccarelli@tnx.it", "update tipi_fatture set segno = -1 where tipo = " + dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO, "segno per note di credito");
            agg(557, "", "m.ceccarelli@tnx.it", "update test_fatt_acquisto set tipo_fattura = " + dbFattura.TIPO_FATTURA_ACQUISTO, "segno per note di credito");
            agg(558, "", "m.ceccarelli@tnx.it", "CREATE TABLE `tipi_fatture_acquisto` ( "
                    + " `tipo` TINYINT(4) NOT NULL DEFAULT '0',"
                    + " `descrizione_breve` CHAR(3) NULL DEFAULT NULL, "
                    + " `descrizione` VARCHAR(100) NULL DEFAULT NULL, "
                    + " `segno` TINYINT(4) NULL DEFAULT NULL, "
                    + " PRIMARY KEY (`tipo`) "
                    + " ) ENGINE=MyISAM", "nuova tab tipi_fatture_acquisto");
            agg(559, "", "m.ceccarelli@tnx.it", "insert into tipi_fatture_acquisto set tipo = " + dbFattura.TIPO_FATTURA_ACQUISTO + " , descrizione_breve = 'F', descrizione = 'FATTURA', segno = 1", "segno per note di credito");
            agg(560, "", "m.ceccarelli@tnx.it", "insert into tipi_fatture_acquisto set tipo = " + dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO_ACQUISTO + " , descrizione_breve = 'NC', descrizione = 'NOTA DI CREDITO', segno = -1", "segno per note di credito");

            //multimagazzino
            agg(561, "", "m.ceccarelli@tnx.it", "CREATE TABLE `depositi` ( "
                    + "`id` SMALLINT(11) NOT NULL AUTO_INCREMENT, "
                    + "`nome` VARCHAR(255) NULL DEFAULT NULL, "
                    + "`indirizzo` VARCHAR(100) NULL DEFAULT NULL, "
                    + "`cap` VARCHAR(10) NULL DEFAULT NULL, "
                    + "`localita` VARCHAR(100) NULL DEFAULT NULL, "
                    + "`provincia` VARCHAR(5) NULL DEFAULT NULL, "
                    + "`paese` CHAR(2) NULL DEFAULT NULL, "
                    + "`intestazione_personalizzata` CHAR(1) NULL DEFAULT NULL, "
                    + "`logo` MEDIUMBLOB NULL, "
                    + "`logo_nome_file` VARCHAR(255) NULL DEFAULT NULL, "
                    + "`logo_data_modifica` BIGINT(20) NULL DEFAULT NULL, "
                    + "`logo_dimensione` BIGINT(20) NULL DEFAULT NULL, "
                    + "`intestazione_riga1` VARCHAR(255) NULL DEFAULT NULL, "
                    + "`intestazione_riga2` VARCHAR(255) NULL DEFAULT NULL, "
                    + "`intestazione_riga3` VARCHAR(255) NULL DEFAULT NULL, "
                    + "`intestazione_riga4` VARCHAR(255) NULL DEFAULT NULL, "
                    + "`intestazione_riga5` VARCHAR(255) NULL DEFAULT NULL, "
                    + "`intestazione_riga6` VARCHAR(255) NULL DEFAULT NULL, "
                    + "PRIMARY KEY (`id`) "
                    + ") ENGINE=MyISAM", "multimagazzino");
            agg(562, "", "m.ceccarelli@tnx.it", "INSERT INTO `depositi` (`id`, `nome`) VALUES (1, 'Deposito predefinito');", "multimagazzino");
            agg(563, "", "m.ceccarelli@tnx.it", "update depositi set id = 0 where id = 1", "multimagazzino");

            agg(564, "", "m.ceccarelli@tnx.it", "alter table test_ddt add column deposito smallint not null default '0'", "multimagazzino");
            agg(565, "", "m.ceccarelli@tnx.it", "alter table test_ordi add column deposito smallint not null default '0'", "multimagazzino");
            agg(566, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column deposito smallint not null default '0'", "multimagazzino");
            agg(567, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add column deposito smallint not null default '0'", "multimagazzino");
            agg(568, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add column deposito smallint not null default '0'", "multimagazzino");
            agg(569, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add column deposito smallint not null default '0'", "multimagazzino");

            agg(570, "", "m.ceccarelli@tnx.it", "alter table test_ddt add column deposito_arrivo smallint null", "multimagazzino");
            agg(571, "", "m.ceccarelli@tnx.it", "alter table test_ordi add column deposito_arrivo smallint null", "multimagazzino");
            agg(572, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column deposito_arrivo smallint null", "multimagazzino");
            agg(573, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add column deposito_arrivo smallint null", "multimagazzino");
            agg(574, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add column deposito_arrivo smallint null", "multimagazzino");
            agg(575, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add column deposito_arrivo smallint null", "multimagazzino");

            agg(576, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_fatt	ADD INDEX `codice_articolo` (`codice_articolo`);", "miglioria giacenze");
            agg(577, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_fatt_acquisto	ADD INDEX `codice_articolo` (`codice_articolo`);", "miglioria giacenze");
            agg(578, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt	ADD INDEX `codice_articolo` (`codice_articolo`);", "miglioria giacenze");
            agg(579, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt_acquisto	ADD INDEX `codice_articolo` (`codice_articolo`);", "miglioria giacenze");
            agg(580, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ordi	ADD INDEX `codice_articolo` (`codice_articolo`);", "miglioria giacenze");
            agg(581, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ordi_acquisto	ADD INDEX `codice_articolo` (`codice_articolo`);", "miglioria giacenze");

            agg(582, "", "m.ceccarelli@tnx.it", "ALTER TABLE scadenze add column importo_pagato DECIMAL(15,5)", "migliorie scadenze");
            agg(583, "", "m.ceccarelli@tnx.it", "ALTER TABLE scadenze add column importo_da_pagare DECIMAL(15,5)", "migliorie scadenze");
            agg(584, "", "m.ceccarelli@tnx.it", "ALTER TABLE scadenze add column ultimo_pagamento DATE", "migliorie scadenze");
            //585 prnotato per dopo
            agg(586, "", "m.ceccarelli@tnx.it", "ALTER TABLE scadenze add column numero_totale int", "migliorie scadenze");
            //587 prnotato per dopo
            agg(588, "", "m.ceccarelli@tnx.it", "alter table articoli add prezzo_medio decimal(15,5) null", "nuovi campi mov mag");

            String view2 = "create view v_righ_tutte as "
                    + " SELECT 'v' as tabella, r.id, r.id_padre, t.data, t.anno, t.numero, t.serie, r.riga, r.codice_articolo, r.descrizione\n"
                    + ", r.quantita, r.prezzo, r.prezzo_netto_unitario, r.sconto1, r.sconto2, t.sconto1 as sconto1t, t.sconto2 as sconto2t, t.sconto3 as sconto3t\n"
                    + ", c.codice as clifor, c.ragione_sociale, t.tipo_fattura, tf.segno, tf.descrizione as tf_descrizione, tf.descrizione_breve as tf_descrizione_breve \n"
                    + "from righ_fatt r \n"
                    + "join test_fatt t on r.id_padre = t.id\n"
                    + "join tipi_fatture tf on t.tipo_fattura = tf.tipo \n"
                    + "join clie_forn c on t.cliente = c.codice\n"
                    + "union all \n"
                    + "SELECT 'a' as tabella, r.id, r.id_padre, t.data, t.anno, t.numero, t.serie, r.riga, r.codice_articolo, r.descrizione\n"
                    + ", r.quantita, r.prezzo, r.prezzo_netto_unitario, r.sconto1, r.sconto2, t.sconto1 as sconto1t, t.sconto2 as sconto2t, t.sconto3 as sconto3t\n"
                    + ", c.codice as clifor, c.ragione_sociale, t.tipo_fattura, tf.segno, tf.descrizione as tf_descrizione, tf.descrizione_breve as tf_descrizione_breve \n"
                    + "from righ_fatt_acquisto r \n"
                    + "join test_fatt_acquisto t on r.id_padre = t.id\n"
                    + "join tipi_fatture_acquisto tf on t.tipo_fattura = tf.tipo \n"
                    + "join clie_forn c on t.fornitore = c.codice";
            agg(589, "", "m.ceccarelli@tnx.it", "drop view if exists v_righ_tutte", "vista per ultimi prezzi");
            agg(590, "", "m.ceccarelli@tnx.it", view2, "vista per ultimi prezzi");

            //sistemo struttura tabelle _matricole che erano diverse fra fatt e altri doc
            agg(591, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_fatt_acquisto_matricole ADD COLUMN (`id` INTEGER NOT NULL AUTO_INCREMENT , `id_padre` INTEGER NOT NULL), DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`, `riga`, `matricola`)", "Aggiungo campo id a righ_fatt_matricole per la generazione dei movimenti");
            agg(592, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt_matricole ADD COLUMN (`id` INTEGER NOT NULL AUTO_INCREMENT , `id_padre` INTEGER NOT NULL), DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`, `riga`, `matricola`)", "Aggiungo campo id a righ_fatt_matricole per la generazione dei movimenti");
            agg(593, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt_acquisto_matricole ADD COLUMN (`id` INTEGER NOT NULL AUTO_INCREMENT , `id_padre` INTEGER NOT NULL), DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`, `riga`, `matricola`)", "Aggiungo campo id a righ_fatt_matricole per la generazione dei movimenti");
            agg(594, "", "m.ceccarelli@tnx.it", "update righ_fatt_acquisto_matricole m, test_fatt_acquisto t set m.id_padre = t.id where m.serie = t.serie and m.numero = t.numero and m.anno = t.anno", "correggo mancanza id_padre per matricole");
            agg(595, "", "m.ceccarelli@tnx.it", "update righ_ddt_matricole m, test_ddt t set m.id_padre = t.id where m.serie = t.serie and m.numero = t.numero and m.anno = t.anno", "correggo mancanza id_padre per matricole");
            agg(596, "", "m.ceccarelli@tnx.it", "update righ_ddt_acquisto_matricole m, test_ddt_acquisto t set m.id_padre = t.id where m.serie = t.serie and m.numero = t.numero and m.anno = t.anno", "correggo mancanza id_padre per matricole");
            //597 vedi dopo
            agg(598, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `label_fornitore` VARCHAR(100) DEFAULT 'Fornitore'", "Etichetta fornitore");
            agg(599, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `label_fornitore_eng` VARCHAR(100) DEFAULT 'Supplier'", "Etichetta fornitore");
            agg(600, "", "m.ceccarelli@tnx.it", "update dati_azienda set label_fornitore = 'Fornitore', label_fornitore_eng = 'Supplier'", "etichetta fornitore");

            agg(601, "", "m.ceccarelli@tnx.it", "CREATE TABLE movimenti_magazzino_storico LIKE movimenti_magazzino ENGINE=MyISAM", "storico movimenti");
//            agg(602, "", "m.ceccarelli@tnx.it", "ALTER TABLE movimenti_magazzino_storico add data_storicizzazione timestamp", "aggiungo data storicizzazione"); //commento perchè uso id storicizzazione
            agg(603, "", "m.ceccarelli@tnx.it", "CREATE TABLE `storicizzazioni` ("
                    + "`id` INT NOT NULL AUTO_INCREMENT,"
                    + "`tabella` VARCHAR(50) NULL DEFAULT NULL,"
                    + "`inizio` TIMESTAMP NULL DEFAULT NULL,"
                    + "`fine` TIMESTAMP NULL DEFAULT NULL,"
                    + "PRIMARY KEY (`id`)"
                    + ") ENGINE=MyISAM", "tabella storicizzazioni");
            agg(604, "", "m.ceccarelli@tnx.it", "ALTER TABLE movimenti_magazzino add id_storicizzazione INT", "aggiungo id_storicizzazione");
            agg(605, "", "m.ceccarelli@tnx.it", "ALTER TABLE movimenti_magazzino_eliminati add id_storicizzazione INT", "aggiungo id_storicizzazione");
            agg(606, "", "m.ceccarelli@tnx.it", "ALTER TABLE movimenti_magazzino_storico add id_storicizzazione INT", "aggiungo id_storicizzazione");

            agg(607, "", "m.ceccarelli@tnx.it", "CREATE TABLE `test_fatt_iva` ( "
                    + "`id_padre` INT(10) UNSIGNED NOT NULL DEFAULT '0', "
                    + "`codice_iva` CHAR(3) NOT NULL DEFAULT '', "
                    + "`perc_iva` DECIMAL(5,2) NOT NULL DEFAULT '0.00', "
                    + "`imponibile` DECIMAL(15,5) NOT NULL DEFAULT '0.00000', "
                    + "`iva` DECIMAL(15,5) NOT NULL DEFAULT '0.00000', "
                    + "`totale` DECIMAL(15,5) NOT NULL DEFAULT '0.00000', "
                    + "PRIMARY KEY (`id_padre`, `codice_iva`) "
                    + ") ENGINE=MyISAM", "tabelle per export fatseq");

            agg(608, "", "m.ceccarelli@tnx.it", "CREATE TABLE `test_fatt_acquisto_iva` ( "
                    + "`id_padre` INT(10) UNSIGNED NOT NULL DEFAULT '0', "
                    + "`codice_iva` CHAR(3) NOT NULL DEFAULT '', "
                    + "`perc_iva` DECIMAL(5,2) NOT NULL DEFAULT '0.00', "
                    + "`imponibile` DECIMAL(15,5) NOT NULL DEFAULT '0.00000', "
                    + "`iva` DECIMAL(15,5) NOT NULL DEFAULT '0.00000', "
                    + "`totale` DECIMAL(15,5) NOT NULL DEFAULT '0.00000', "
                    + "PRIMARY KEY (`id_padre`, `codice_iva`) "
                    + ") ENGINE=MyISAM", "tabelle per export fatseq");

            agg(609, "", "m.ceccarelli@tnx.it", "alter table test_fatt add `ts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", "ts gen totali");
            agg(610, "", "m.ceccarelli@tnx.it", "alter table test_fatt add `ts_gen_totali` TIMESTAMP NULL DEFAULT NULL", "ts gen totali");
            agg(611, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add `ts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", "ts gen totali");
            agg(612, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add `ts_gen_totali` TIMESTAMP NULL DEFAULT NULL", "ts gen totali");

            agg(613, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add `ts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", "ts gen totali");
            agg(614, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add `ts_gen_totali` TIMESTAMP NULL DEFAULT NULL", "ts gen totali");
            agg(615, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto add `ts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", "ts gen totali");
            agg(616, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto add `ts_gen_totali` TIMESTAMP NULL DEFAULT NULL", "ts gen totali");

            agg(617, "", "m.ceccarelli@tnx.it", "alter table clie_forn add note_consegna TEXT NULL", "note consegna");
            agg(618, "", "m.ceccarelli@tnx.it", "alter table test_ordi add note_consegna TEXT NULL", "consegna");
            agg(619, "", "m.ceccarelli@tnx.it", "alter table test_ddt add note_consegna TEXT NULL", "consegna");
            agg(620, "", "m.ceccarelli@tnx.it", "alter table test_fatt add note_consegna TEXT NULL", "consegna");
            agg(621, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add note_consegna TEXT NULL", "consegna");
            agg(622, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add note_consegna TEXT NULL", "consegna");
            agg(623, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add note_consegna TEXT NULL", "consegna");

            agg(624, "", "m.ceccarelli@tnx.it", "CREATE TABLE `files` (`id` INT(11) NOT NULL AUTO_INCREMENT,`bytes` LONGBLOB NULL,	PRIMARY KEY (`id`)) ENGINE=MyISAM;", "tabella files");
            agg(625, "", "m.ceccarelli@tnx.it", "ALTER TABLE `files` ADD COLUMN `filename` VARCHAR(1000) NULL AFTER `bytes`, ADD COLUMN `lastmodified` TIMESTAMP NULL AFTER `filename`,	ADD COLUMN `type` CHAR(3) NULL AFTER `lastmodified`,	ADD COLUMN `size` INT NULL AFTER `type`", "aggiunte a files");
            agg(626, "", "m.ceccarelli@tnx.it", "ALTER TABLE `files` ADD COLUMN `md5` VARCHAR(32) NULL", "aggiunte a files");
            agg(627, "", "m.ceccarelli@tnx.it", "ALTER TABLE `files` ADD UNIQUE INDEX `md5` (`md5`);", "aggiunte a files");
            agg(628, "", "m.ceccarelli@tnx.it", "CREATE TABLE `files_documenti` (\n"
                    + "	`id` INT(11) NOT NULL AUTO_INCREMENT,\n"
                    + "	`tabella_doc` CHAR(20) NULL DEFAULT NULL,\n"
                    + "	`id_doc` INT(11) NULL DEFAULT NULL,\n"
                    + "	`id_file` INT(11) NULL DEFAULT NULL,\n"
                    + "	PRIMARY KEY (`id`),\n"
                    + "	UNIQUE INDEX `tabella_doc_id_doc_id_files` (`tabella_doc`, `id_doc`, `id_file`)\n"
                    + ") ENGINE=MyISAM", "files_documenti");

            agg(629, "", "m.ceccarelli@tnx.it", "ALTER TABLE `clie_forn_dest`\n"
                    + "	ADD COLUMN `id` INT NOT NULL AUTO_INCREMENT FIRST,\n"
                    + "	DROP PRIMARY KEY,\n"
                    + "	ADD PRIMARY KEY (`id`);", "cambio chiave dest diverse clienti");
            agg(630, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ddt`	ADD COLUMN `id_cliente_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `cliente_destinazione`;", "id per dest div");
            agg(631, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_fatt`	ADD COLUMN `id_cliente_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `cliente_destinazione`;", "id per dest div");
            agg(632, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ordi`	ADD COLUMN `id_cliente_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `cliente_destinazione`;", "id per dest div");
            agg(633, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ddt_acquisto`	ADD COLUMN `id_fornitore_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `fornitore_destinazione`;", "id per dest div");
            agg(635, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ordi_acquisto`	ADD COLUMN `id_fornitore_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `fornitore_destinazione`;", "id per dest div");
            agg(636, "", "m.ceccarelli@tnx.it", "UPDATE test_ddt t INNER JOIN clie_forn_dest d on d.codice_cliente = t.cliente and d.codice = t.cliente_destinazione SET t.id_cliente_destinazione = d.id", "id per dest div");
            agg(637, "", "m.ceccarelli@tnx.it", "UPDATE test_ordi t INNER JOIN clie_forn_dest d on d.codice_cliente = t.cliente and d.codice = t.cliente_destinazione SET t.id_cliente_destinazione = d.id", "id per dest div");
            agg(638, "", "m.ceccarelli@tnx.it", "UPDATE test_fatt t INNER JOIN clie_forn_dest d on d.codice_cliente = t.cliente and d.codice = t.cliente_destinazione SET t.id_cliente_destinazione = d.id", "id per dest div");
            agg(639, "", "m.ceccarelli@tnx.it", "UPDATE test_ddt_acquisto t INNER JOIN clie_forn_dest d on d.codice_cliente = t.fornitore and d.codice = t.fornitore_destinazione SET t.id_fornitore_destinazione = d.id", "id per dest div");
            agg(640, "", "m.ceccarelli@tnx.it", "UPDATE test_ordi_acquisto t INNER JOIN clie_forn_dest d on d.codice_cliente = t.fornitore and d.codice = t.fornitore_destinazione SET t.id_fornitore_destinazione = d.id", "id per dest div");
            agg(642, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ddt` CHANGE COLUMN `cliente_destinazione` `old_cliente_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL", "id dest div");
            agg(643, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ordi` CHANGE COLUMN `cliente_destinazione` `old_cliente_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL", "id dest div");
            agg(644, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_fatt` CHANGE COLUMN `cliente_destinazione` `old_cliente_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL", "id dest div");
            agg(645, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ddt_acquisto` CHANGE COLUMN `fornitore_destinazione` `old_fornitore_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL", "id dest div");
            agg(646, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ordi_acquisto` CHANGE COLUMN `fornitore_destinazione` `old_fornitore_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL", "id dest div");
            agg(647, "", "m.ceccarelli@tnx.it", "ALTER TABLE `clie_forn_dest` CHANGE COLUMN `codice` `old_codice` INT(10) UNSIGNED NULL DEFAULT NULL", "id dest div");

            //ricreo per compatibilità i campi dest diversa
            agg(648, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ddt` add column `cliente_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `id_cliente_destinazione`", "id dest div");
            agg(649, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ordi` add column `cliente_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `id_cliente_destinazione`", "id dest div");
            agg(650, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_fatt` add column `cliente_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `id_cliente_destinazione`", "id dest div");
            agg(651, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ddt_acquisto` add column `fornitore_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `id_fornitore_destinazione`", "id dest div");
            agg(652, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ordi_acquisto` add column `fornitore_destinazione` INT(10) UNSIGNED NULL DEFAULT NULL AFTER `id_fornitore_destinazione`", "id dest div");

            //trim spacesssss
            agg(653, "", "m.ceccarelli@tnx.it", "update articoli set codice = rtrim(codice)", "rtrim");
            agg(654, "", "m.ceccarelli@tnx.it", "update articoli_prezzi set articolo = rtrim(articolo)", "rtrim");
            agg(655, "", "m.ceccarelli@tnx.it", "UPDATE righ_fatt set codice_articolo = rtrim(codice_articolo)", "rtrim");
            agg(656, "", "m.ceccarelli@tnx.it", "UPDATE righ_fatt_acquisto set codice_articolo = rtrim(codice_articolo)", "rtrim");
            agg(657, "", "m.ceccarelli@tnx.it", "UPDATE righ_ddt set codice_articolo = rtrim(codice_articolo)", "rtrim");
            agg(658, "", "m.ceccarelli@tnx.it", "UPDATE righ_ddt_acquisto set codice_articolo = rtrim(codice_articolo)", "rtrim");
            agg(659, "", "m.ceccarelli@tnx.it", "UPDATE righ_ordi set codice_articolo = rtrim(codice_articolo)", "rtrim");
            agg(660, "", "m.ceccarelli@tnx.it", "UPDATE righ_ordi_acquisto set codice_articolo = rtrim(codice_articolo)", "rtrim");
            agg(661, "", "m.ceccarelli@tnx.it", "UPDATE movimenti_magazzino set articolo = rtrim(articolo)", "rtrim");

            agg(662, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto CHANGE COLUMN spese_varie spese_varie DECIMAL(15,5) NULL DEFAULT NULL", "decimal ordi acquisto");
            agg(663, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto CHANGE COLUMN totale_imponibile totale_imponibile DECIMAL(15,5) NULL DEFAULT NULL", "decimal ordi acquisto");
            agg(664, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto CHANGE COLUMN totale_iva totale_iva DECIMAL(15,5) NULL DEFAULT NULL", "decimal ordi acquisto");
            agg(665, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto CHANGE COLUMN totale totale DECIMAL(15,5) NULL DEFAULT NULL", "decimal ordi acquisto");
            agg(666, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto CHANGE COLUMN sconto1 sconto1 DECIMAL(5,2) NULL DEFAULT NULL", "decimal ordi acquisto");
            agg(667, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto CHANGE COLUMN sconto2 sconto2 DECIMAL(5,2) NULL DEFAULT NULL", "decimal ordi acquisto");
            agg(668, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto CHANGE COLUMN sconto3 sconto3 DECIMAL(5,2) NULL DEFAULT NULL", "decimal ordi acquisto");
            agg(669, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto CHANGE COLUMN spese_trasporto spese_trasporto DECIMAL(15,5) NULL DEFAULT NULL", "decimal ordi acquisto");
            agg(670, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto CHANGE COLUMN spese_incasso spese_incasso DECIMAL(15,5) NULL DEFAULT NULL", "decimal ordi acquisto");

            //export xml pa
            agg(671, "", "m.ceccarelli@tnx.it", "ALTER TABLE codici_iva add codice_natura_xmlpa VARCHAR(10) NULL", "export xml pa iva");
            agg(672, "", "m.ceccarelli@tnx.it", "update codici_iva set codice_natura_xmlpa = 'N4' where descrizione like '%esent%'", "preimposto codice xmlpa");
            agg(673, "", "m.ceccarelli@tnx.it", "update codici_iva set codice_natura_xmlpa = 'N1' where descrizione like '%art.15%'", "preimposto codice xmlpa");
            agg(674, "", "m.ceccarelli@tnx.it", "update codici_iva set codice_natura_xmlpa = 'N1' where descrizione like '%art. 15%'", "preimposto codice xmlpa");
            agg(675, "", "m.ceccarelli@tnx.it", "update codici_iva set codice_natura_xmlpa = 'N2' where descrizione like '%Non soggett%'", "preimposto codice xmlpa");
            agg(676, "", "m.ceccarelli@tnx.it", "update codici_iva set codice_natura_xmlpa = 'N3' where descrizione like '%Non imponibil%'", "preimposto codice xmlpa");
            agg(677, "", "m.ceccarelli@tnx.it", "ALTER TABLE pagamenti add codice_xmlpa VARCHAR(10) NULL", "export xml pa pagamento");
            agg(678, "", "m.ceccarelli@tnx.it", "update pagamenti set codice_xmlpa = 'MP12' where codice like 'RB %'", "preimposto codice xmlpa");
            agg(679, "", "m.ceccarelli@tnx.it", "update pagamenti set codice_xmlpa = 'MP12' where codice like 'R.B.%'", "preimposto codice xmlpa");
            agg(680, "", "m.ceccarelli@tnx.it", "update pagamenti set codice_xmlpa = 'MP12' where codice like 'RIBA %'", "preimposto codice xmlpa");
            agg(681, "", "m.ceccarelli@tnx.it", "update pagamenti set codice_xmlpa = 'MP01' where codice like 'R.D.'", "preimposto codice xmlpa");
            agg(682, "", "m.ceccarelli@tnx.it", "update pagamenti set codice_xmlpa = 'MP01' where descrizione like '%contant%'", "preimposto codice xmlpa");
            agg(683, "", "m.ceccarelli@tnx.it", "update pagamenti set codice_xmlpa = 'MP05' where codice like 'BONIFICO%'", "preimposto codice xmlpa");
            agg(684, "", "m.ceccarelli@tnx.it", "update pagamenti set codice_xmlpa = 'MP05' where descrizione like '%BONIFICO%'", "preimposto codice xmlpa");

            //valute
            agg(685, "", "m.ceccarelli@tnx.it", "CREATE TABLE `valute` ( "
                    + "`codice` CHAR(3) NOT NULL, "
                    + "`simbolo` VARCHAR(5) NOT NULL,"
                    + "`descrizione` VARCHAR(100) NOT NULL,"
                    + "PRIMARY KEY (`codice`)"
                    + ") ENGINE=MyISAM", "valute");

            agg(686, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('EUR', '€', 'Euro')", "valute");
            agg(687, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('USD', '$', 'Dollaro USA')", "valute");
            agg(688, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('GBP', '£', 'Sterlina Inglese')", "valute");
            agg(689, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('CHF', 'CHF', 'Franco Svizzero')", "valute");
            agg(690, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('JPY', '¥', 'Yen Giapponese')", "valute");
            agg(691, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('CNY', '¥', 'Yuan Cinese')", "valute");
            agg(692, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('NZD', '$', 'Dollaro Neozelandese')", "valute");
            agg(693, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('INR', '₨', 'Rupia Indiana')", "valute");
            agg(694, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('RUB', 'руб', 'Rublo Russo')", "valute");
            agg(695, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('AUD', '$', 'Dollaro Australiano')", "valute");
            agg(696, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('ALL', 'Lek', 'Lek Albanese')", "valute");
            agg(697, "", "m.ceccarelli@tnx.it", "INSERT INTO `valute` (`codice`, `simbolo`, `descrizione`) VALUES ('BRL', 'R$', 'Real Brasiliano')", "valute");

            agg(698, "", "m.ceccarelli@tnx.it", "alter table test_ddt add column valuta char(3) null", "valuta");
            agg(699, "", "m.ceccarelli@tnx.it", "alter table test_ordi add column valuta char(3) null", "valuta");
            agg(700, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column valuta char(3) null", "valuta");
            agg(701, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add column valuta char(3) null", "valuta");
            agg(702, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add column valuta char(3) null", "valuta");
            agg(703, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add column valuta char(3) null", "valuta");

            agg(704, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column valuta char(3) null", "valuta");

            agg(705, "", "m.ceccarelli@tnx.it", "CREATE TABLE `test_fatt_xmlpa` (\n"
                    + "	`id_fattura` INT(11) NOT NULL,\n"
                    + "	`dg_doa_riferimentonumerolinea` VARCHAR(4) NULL DEFAULT NULL,\n"
                    + "	`dg_doa_iddocumento` VARCHAR(20) NULL DEFAULT NULL,\n"
                    + "	`dg_doa_data` DATE NULL DEFAULT NULL,\n"
                    + "	`dg_doa_numitem` VARCHAR(20) NULL DEFAULT NULL,\n"
                    + "	`dg_doa_codicecommessaconvenzione` VARCHAR(100) NULL DEFAULT NULL,\n"
                    + "	`dg_doa_codicecup` VARCHAR(15) NULL DEFAULT NULL,\n"
                    + "	`dg_doa_codicecig` VARCHAR(15) NULL DEFAULT NULL,\n"
                    + "	`dg_dc_riferimentonumerolinea` VARCHAR(4) NULL DEFAULT NULL,\n"
                    + "	`dg_dc_iddocumento` VARCHAR(20) NULL DEFAULT NULL,\n"
                    + "	`dg_dc_data` DATE NULL DEFAULT NULL,\n"
                    + "	`dg_dc_numitem` VARCHAR(20) NULL DEFAULT NULL,\n"
                    + "	`dg_dc_codicecommessaconvenzione` VARCHAR(100) NULL DEFAULT NULL,\n"
                    + "	`dg_dc_codicecup` VARCHAR(15) NULL DEFAULT NULL,\n"
                    + "	`dg_dc_codicecig` VARCHAR(15) NULL DEFAULT NULL,\n"
                    + "	PRIMARY KEY (`id_fattura`)\n"
                    + ") ENGINE=MyISAM", "xmlpa");
            agg(706, "", "m.ceccarelli@tnx.it", "alter table test_fatt_xmlpa add column dp_iban varchar(34) null", "xmlpa");
            agg(707, "", "m.ceccarelli@tnx.it", "alter table test_fatt_xmlpa add column dp_istituto_finanziario varchar(80) null", "xmlpa");

            //campi per export e marcatura
            agg(708, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column ts_export timestamp null", "ts export");
            agg(709, "", "m.ceccarelli@tnx.it", "alter table test_ddt add column ts_export timestamp null", "ts export");
            agg(710, "", "m.ceccarelli@tnx.it", "alter table test_ordi add column ts_export timestamp null", "ts export");
            agg(711, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add column ts_export timestamp null", "ts export");
            agg(712, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add column ts_export timestamp null", "ts export");
            agg(713, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add column ts_export timestamp null", "ts export");
            agg(714, "", "m.ceccarelli@tnx.it", "alter table clie_forn add column ts_export timestamp null", "ts export");

            //quantità nei lotti
            agg(715, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt_acquisto_lotti CHANGE COLUMN qta qta DECIMAL(15,5) NULL DEFAULT NULL", "qta lotti");
            agg(716, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt_lotti CHANGE COLUMN qta qta DECIMAL(15,5) NULL DEFAULT NULL", "qta lotti");
            agg(717, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_fatt_acquisto_lotti CHANGE COLUMN qta qta DECIMAL(15,5) NULL DEFAULT NULL", "qta lotti");
            agg(718, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_fatt_lotti CHANGE COLUMN qta qta DECIMAL(15,5) NULL DEFAULT NULL", "qta lotti");
            agg(719, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ordi_acquisto_lotti CHANGE COLUMN qta qta DECIMAL(15,5) NULL DEFAULT NULL", "qta lotti");
            agg(720, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ordi_lotti CHANGE COLUMN qta qta DECIMAL(15,5) NULL DEFAULT NULL", "qta lotti");

            //bug massimale rivalsa
            agg(721, "", "m.ceccarelli@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN flag_bug_massimale_rivalsa char(1) NULL DEFAULT NULL", "bug massimale rivalsa");

            //dati ritenuta e rivalsa su xml pa
            agg(722, "", "m.ceccarelli@tnx.it", "alter table test_fatt_xmlpa add column dg_dr_tipo_ritenuta varchar(4) null", "xmlpa");
            agg(723, "", "m.ceccarelli@tnx.it", "alter table test_fatt_xmlpa add column dg_dr_causale_pagamento char(1) null", "xmlpa");
            agg(724, "", "m.ceccarelli@tnx.it", "alter table test_fatt_xmlpa add column dg_dcp_tipo_cassa varchar(4) null", "xmlpa");

            //flag consegnato
            agg(725, "", "m.ceccarelli@tnx.it", "alter table righ_ddt add column flag_consegnato char(1) null", "flag consegna");
            agg(726, "", "m.ceccarelli@tnx.it", "alter table righ_ordi add column flag_consegnato char(1) null", "flag consegnato");
            agg(727, "", "m.ceccarelli@tnx.it", "alter table righ_fatt add column flag_consegnato char(1) null", "flag_consegnato");
            agg(728, "", "m.ceccarelli@tnx.it", "alter table righ_ddt_acquisto add column flag_consegnato char(1) null", "flag consegnato");
            agg(729, "", "m.ceccarelli@tnx.it", "alter table righ_ordi_acquisto add column flag_consegnato char(1) null", "flag consegnato");
            agg(730, "", "m.ceccarelli@tnx.it", "alter table righ_fatt_acquisto add column flag_consegnato char(1) null", "flag consegnato");

            agg(731, "", "m.ceccarelli@tnx.it", "alter table storico add column hostname varchar(255) null", "dati per storico");
            agg(732, "", "m.ceccarelli@tnx.it", "alter table storico add column login_so varchar(255) null", "dati per storico");
            agg(733, "", "m.ceccarelli@tnx.it", "alter table storico add column username_id int null", "dati per storico");
            agg(734, "", "m.ceccarelli@tnx.it", "alter table storico add column username varchar(255) null", "dati per storico");

            agg(735, "", "m.ceccarelli@tnx.it", "CREATE TABLE `accessi_utenti_online` (\n"
                    + "	`utente_key` VARCHAR(255) NOT NULL COMMENT '|hostname|login_so|idutente|username|',\n"
                    + "	`ts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n"
                    + "	PRIMARY KEY (`utente_key`)\n"
                    + ") ENGINE=MyISAM", "utenti online");

            agg(736, "", "m.ceccarelli@tnx.it", "CREATE TABLE `locks` (\n"
                    + "	`id` INT(11) NOT NULL AUTO_INCREMENT,\n"
                    + "	`username_id` INT(11) NOT NULL DEFAULT '0',\n"
                    + "	`username` VARCHAR(250) NOT NULL DEFAULT '0',\n"
                    + "	`hostname` VARCHAR(250) NOT NULL DEFAULT '0',\n"
                    + "	`login_so` VARCHAR(250) NOT NULL DEFAULT '0',\n"
                    + "	`ts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n"
                    + "	`ts_timer` TIMESTAMP NULL DEFAULT NULL,\n"
                    + "	`lock_tabella` VARCHAR(50) NOT NULL DEFAULT '0',\n"
                    + "	`lock_id` INT(11) NOT NULL DEFAULT '0',\n"
                    + "	PRIMARY KEY (`id`),\n"
                    + "	UNIQUE INDEX `univoco1` (`lock_tabella`, `lock_id`)\n"
                    + ") ENGINE=MyISAM", "locks documenti");

            agg(737, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column acconto decimal(15,5) null", "acconto");
            agg(738, "", "m.ceccarelli@tnx.it", "alter table test_ddt add column acconto decimal(15,5) null", "acconto");
            agg(739, "", "m.ceccarelli@tnx.it", "alter table test_ordi add column acconto decimal(15,5) null", "acconto");
            agg(740, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add column acconto decimal(15,5) null", "acconto");
            agg(741, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add column acconto decimal(15,5) null", "acconto");
            agg(742, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add column acconto decimal(15,5) null", "acconto");

            agg(743, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column totale_da_pagare_finale decimal(15,5) null", "totale_da_pagare_finale");
            agg(744, "", "m.ceccarelli@tnx.it", "alter table test_ddt add column totale_da_pagare_finale decimal(15,5) null", "totale_da_pagare_finale");
            agg(745, "", "m.ceccarelli@tnx.it", "alter table test_ordi add column totale_da_pagare_finale decimal(15,5) null", "totale_da_pagare_finale");
            agg(746, "", "m.ceccarelli@tnx.it", "alter table test_fatt_acquisto add column totale_da_pagare_finale decimal(15,5) null", "totale_da_pagare_finale");
            agg(747, "", "m.ceccarelli@tnx.it", "alter table test_ddt_acquisto add column totale_da_pagare_finale decimal(15,5) null", "totale_da_pagare_finale");
            agg(748, "", "m.ceccarelli@tnx.it", "alter table test_ordi_acquisto add column totale_da_pagare_finale decimal(15,5) null", "totale_da_pagare_finale");

            agg(749, "", "m.ceccarelli@tnx.it", "ALTER TABLE scadenze ALTER numero DROP DEFAULT;", "scadenze per acconti");
            agg(750, "", "m.ceccarelli@tnx.it", "ALTER TABLE scadenze ALTER numero_totale DROP DEFAULT;", "scadenze per acconti");
            agg(751, "", "m.ceccarelli@tnx.it", "ALTER TABLE scadenze CHANGE COLUMN numero numero TINYINT(4) NULL;", "scadenze per acconti");
            agg(752, "", "m.ceccarelli@tnx.it", "ALTER TABLE scadenze CHANGE COLUMN numero_totale numero_totale TINYINT(4) NULL;", "scadenze per acconti");
            agg(753, "", "m.ceccarelli@tnx.it", "ALTER TABLE scadenze ADD COLUMN flag_acconto CHAR(1) DEFAULT 'N';", "scadenze per acconti");

            agg(754, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt add index id_padre (id_padre)", "problema raggruppamento ddt");
            agg(755, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt add index per_raggr (codice_articolo, um, prezzo, iva, sconto1, sconto2)", "problema raggruppamento ddt");
            agg(756, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt add index um (um)", "problema raggruppamento ddt");
            agg(757, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt add index prezzo (prezzo)", "problema raggruppamento ddt");
            agg(758, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt add index iva (iva)", "problema raggruppamento ddt");
            agg(759, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt add index sconto1 (sconto1)", "problema raggruppamento ddt");
            agg(760, "", "m.ceccarelli@tnx.it", "ALTER TABLE righ_ddt add index sconto2 (sconto2)", "problema raggruppamento ddt");

            agg(761, "", "m.ceccarelli@tnx.it", "alter table test_fatt_xmlpa add column dg_dr_totale_da_esportare varchar(30) null", "xmlpa");

            agg(762, "", "m.ceccarelli@tnx.it", "alter table test_fatt_xmlpa add column dg_causale varchar(200) null", "xmlpa");
            agg(763, "", "m.ceccarelli@tnx.it", "alter table clie_forn add column xmlpa_codice_ufficio varchar(6) null", "xmlpa");
            agg(764, "", "m.ceccarelli@tnx.it", "alter table clie_forn add column xmlpa_riferimento varchar(20) null", "xmlpa");
            agg(765, "", "m.ceccarelli@tnx.it", "ALTER TABLE `files_documenti` ADD COLUMN `allega_email` char(1) NULL", "aggiunte a files");
            agg(766, "", "m.ceccarelli@tnx.it", "ALTER TABLE `files_documenti` ADD COLUMN `allega_fatturapa` char(1) NULL", "aggiunte a files");

            agg(767, "", "m.ceccarelli@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN provvigioni_tipo_calcolo tinyint NULL", "tipo calcolo base per provvigioni");

            //cambio base imponibile per provvigioni, salvo i totali in tabella apposita provvigioni            
            agg(768, "", "m.ceccarelli@tnx.it", "CREATE TABLE `provvigioni_dettagli` ( "
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT, "
                    + "`id_test_fatt` INT NOT NULL, "
                    + "`tipo_calcolo` TINYINT NOT NULL, "
                    + "`id_riga` INT NOT NULL, "
                    + "`base_provvigione` DECIMAL(15,5) NOT NULL, "
                    + "`perc_provvigione` DECIMAL(5,2) NOT NULL, "
                    + "`importo_provvigione` DECIMAL(15,5) NOT NULL, "
                    + " PRIMARY KEY (`id`) "
                    + ") ENGINE=MyISAM", "provvigioni_dettagli");

            //bug massimale rivalsa
            agg(769, "", "m.ceccarelli@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN flag_bug_provvigioni char(1) NULL DEFAULT NULL", "bug provvigioni");

            //split payment
            agg(770, "", "m.ceccarelli@tnx.it", "alter table test_fatt_xmlpa add column split_payment char(1) null", "xmlpa split payment");
            agg(771, "", "m.ceccarelli@tnx.it", "alter table clie_forn add column split_payment char(1) null", "clie_forn split payment");
            agg(772, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt_xmlpa CHANGE COLUMN dg_dr_causale_pagamento dg_dr_causale_pagamento CHAR(2) NULL DEFAULT NULL", "xmlpa");

            agg(773, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_fatt` CHANGE COLUMN `totaleRivalsa` `totaleRivalsa` DECIMAL(15,5) NULL DEFAULT '0.00'", "totale rivalsa troppo piccolo");
            agg(774, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_fatt_acquisto` CHANGE COLUMN `totaleRivalsa` `totaleRivalsa` DECIMAL(15,5) NULL DEFAULT '0.00'", "totale rivalsa troppo piccolo");

            agg(775, "", "m.ceccarelli@tnx.it", "ALTER TABLE `provvigioni` ADD COLUMN `id_doc` INT NULL, ADD COLUMN `id_scadenza`  BIGINT(20) NULL", "aggancio migliore fra provvigioni e scadenze");
            //776 usato sotto per nuova struttura per id di provvigioni e scadenze
            //777 usato sotto per nuova struttura per id di provvigioni e scadenze

            /* commento perchè fatto in altro modo vedi magazzino toys
             agg(778, "", "m.ceccarelli@tnx.it", "alter table movimenti_magazzino add prezzo_unitario decimal(15,5) null", "per costo medio ponderato");
             agg(779, "", "m.ceccarelli@tnx.it", "alter table movimenti_magazzino add prezzo_unitario_netto decimal(15,5) null", "per costo medio ponderato");
             agg(780, "", "m.ceccarelli@tnx.it", "alter table movimenti_magazzino_eliminati add prezzo_unitario decimal(15,5) null", "per costo medio ponderato");
             agg(781, "", "m.ceccarelli@tnx.it", "alter table movimenti_magazzino_eliminati add prezzo_unitario_netto decimal(15,5) null", "per costo medio ponderato");
             agg(782, "", "m.ceccarelli@tnx.it", "alter table movimenti_magazzino_storico add prezzo_unitario decimal(15,5) null", "per costo medio ponderato");
             agg(783, "", "m.ceccarelli@tnx.it", "alter table movimenti_magazzino_storico add prezzo_unitario_netto decimal(15,5) null", "per costo medio ponderato");
             //784 usato sotto per costo medi oponderato
             */
            agg(790, "", "m.ceccarelli@tnx.it", "ALTER TABLE clie_forn CHANGE COLUMN `email` `email` VARCHAR(1000) NULL", "email da longtext a varchar(1000) base ecommerce");
            agg(791, "", "m.ceccarelli@tnx.it", "ALTER TABLE `clie_forn`\n"
                    + "	ALTER `banca_abi` DROP DEFAULT,\n"
                    + "	ALTER `banca_cab` DROP DEFAULT,\n"
                    + "	ALTER `banca_cc` DROP DEFAULT;", "campi null base ecommerce");
            agg(792, "", "m.ceccarelli@tnx.it", "ALTER TABLE `clie_forn`\n"
                    + "	CHANGE COLUMN `banca_abi` `banca_abi` VARCHAR(5) NULL AFTER `codice_listino`,\n"
                    + "	CHANGE COLUMN `banca_cab` `banca_cab` VARCHAR(5) NULL AFTER `banca_abi`,\n"
                    + "	CHANGE COLUMN `banca_cc` `banca_cc` VARCHAR(35) NULL AFTER `banca_cab`,\n"
                    + "	CHANGE COLUMN `tipo2` `tipo2` VARCHAR(10) NULL DEFAULT '';", "campi null base ecommerce");

            agg(793, "", "m.ceccarelli@tnx.it", "ALTER TABLE `articoli`\n"
                    + "	ADD COLUMN `prezzo_medio_data` DATE NULL DEFAULT NULL AFTER `prezzo_medio`,\n"
                    + "	ADD COLUMN `prezzo_medio_ts` TIMESTAMP NULL DEFAULT NULL AFTER `prezzo_medio_data`,\n"
                    + "	ADD COLUMN `costo_medio_ponderato` DECIMAL(15,5) NULL DEFAULT NULL,\n"
                    + "	ADD COLUMN `costo_medio_ponderato_data` DATE NULL DEFAULT NULL,\n"
                    + "	ADD COLUMN `costo_medio_ponderato_ts` TIMESTAMP NULL DEFAULT NULL;", "cmp");

            agg(794, "", "m.ceccarelli@tnx.it", "alter table articoli modify column descrizione_en text", "articoli descrizione_en text");
            agg(795, "", "a.toce@tnx.it", "ALTER TABLE test_fatt ADD COLUMN bloccata TIMESTAMP NULL DEFAULT NULL", "aggiunta colonna bloccaggio su fatture di vendita");
            agg(796, "", "a.toce@tnx.it", "ALTER TABLE test_fatt_acquisto ADD COLUMN bloccata TIMESTAMP NULL DEFAULT NULL", "aggiunta colonna bloccaggio su fatture di vendita");

            agg(795, "", "m.ceccarelli@tnx.it", "ALTER TABLE dati_azienda ADD COLUMN nomi_pdf text NULL", "nomi file pdf");
            agg(796, "", "m.ceccarelli@tnx.it", "ALTER TABLE `movimenti_magazzino` CHANGE COLUMN `modificato_ts` `modificato_ts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", "giacenza articolo on update"); //altrimenti non sentiva i movimenti modificati

            agg(797, "", "a.toce@tnx.it", "ALTER TABLE test_fatt ADD COLUMN anticipata CHAR(1) NOT NULL DEFAULT 'N'", "Aggiunta colonna per fattura anticipata si o no (default no)");
            agg(798, "", "a.toce@tnx.it", "ALTER TABLE test_fatt ADD COLUMN banca_di_anticipazione INT NULL DEFAULT NULL", "Aggiunta colonna per segnalare conto corrente su cui viene anticipata la fattura");

//            agg(797, "", "m.ceccarelli@tnx.it", "", "");
            agg(799, "", "a.toce@tnx.it", "ALTER TABLE clie_forn ADD COLUMN note_docu TEXT NULL", "Aggiunta colonna note per ddt di vendita");
            agg(800, "", "a.toce@tnx.it", "ALTER TABLE clie_forn ADD COLUMN note_prev TEXT NULL", "Aggiunta colonna note per preventivi di vendita");
            agg(801, "", "a.toce@tnx.it", "ALTER TABLE clie_forn ADD COLUMN note_ordi TEXT NULL", "Aggiunta colonna note per ordini di vendita");
            agg(802, "", "a.toce@tnx.it", "ALTER TABLE clie_forn ADD COLUMN note_fatt_acq TEXT NULL", "Aggiunta colonna note per fattue di acquisto");
            agg(803, "", "a.toce@tnx.it", "ALTER TABLE clie_forn ADD COLUMN note_docu_acq TEXT NULL", "Aggiunta colonna note per ddt di acquisto");
            agg(804, "", "a.toce@tnx.it", "ALTER TABLE clie_forn ADD COLUMN note_prev_acq TEXT NULL", "Aggiunta colonna note per preventivi di acquisto");
            agg(805, "", "a.toce@tnx.it", "ALTER TABLE clie_forn ADD COLUMN note_ordi_acq TEXT NULL", "Aggiunta colonna note per ordini di acquisto");

            //sync
            agg(797, "", "m.ceccarelli@tnx.it", "ALTER TABLE `clie_forn` CHANGE COLUMN `codice` `codice` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT FIRST;", "sync");
            agg(798, "", "m.ceccarelli@tnx.it", "ALTER TABLE `clie_forn` CHANGE COLUMN `tipo2` `tipo2` VARCHAR(10) NULL DEFAULT NULL;", "sync");

            agg(799, "", "m.ceccarelli@tnx.it", "ALTER TABLE `articoli_prezzi`"
                    + " ADD COLUMN `id` INT NOT NULL AUTO_INCREMENT FIRST, "
                    + " DROP PRIMARY KEY, "
                    + " ADD PRIMARY KEY (`id`), "
                    + " ADD UNIQUE INDEX `articolo_listino` (`articolo`, `listino`), "
                    + " ADD INDEX `articolo` (`articolo`), "
                    + " ADD INDEX `listino` (`listino`);", "sync");

            agg(800, "", "m.ceccarelli@tnx.it", "ALTER TABLE codici_iva ADD COLUMN conto_readytec VARCHAR(10) NULL DEFAULT NULL", "Aggiunto campo di collegamento codice iva readytec");    //premio celeste moss

            agg(801, "", "m.ceccarelli@tnx.it", "update test_fatt set marca_da_bollo = null where IFNULL(marca_da_bollo,0) = 0", "bollo");
            agg(802, "", "m.ceccarelli@tnx.it", "alter table test_fatt add column bollo_presente char(1) not null default 'N'", "bollo");
            agg(803, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda` ADD COLUMN `codiceIvaBollo` VARCHAR(10)", "bollo");
            agg(804, "", "m.ceccarelli@tnx.it", "update dati_azienda set codiceIvaBollo = '15'", "bollo");

            agg(805, "", "m.ceccarelli@tnx.it", "ALTER TABLE `temp_stampa_stat_ord_bol_fat`"
                    + " ADD COLUMN `agente_codice` INT NULL AFTER `qta`,"
                    + " ADD COLUMN `agente_nome` VARCHAR(255) NULL AFTER `agente_codice`;", "stats agente");

            agg(806, "", "m.ceccarelli@tnx.it", "ALTER TABLE clie_forn ADD COLUMN note_fatt TEXT NULL", "Aggiunta colonna note per fattue di vendita");
            agg(807, "", "m.ceccarelli@tnx.it", "update clie_forn set note_fatt = note, note_docu = note, note_ordi = note, note_prev = note where ifnull(note_automatiche,'N') = 'S'", "Aggiunta colonna note per fattue di vendita");

            agg(808, "", "m.ceccarelli@tnx.it", "CREATE TABLE `clie_forn_contatti` (\n"
                    + "	`id` INT(11) NOT NULL AUTO_INCREMENT,\n"
                    + "	`id_clifor` INT(11) NOT NULL,\n"
                    + "	`tipo` VARCHAR(50) NULL DEFAULT NULL,\n"
                    + "	`nome` VARCHAR(255) NULL DEFAULT NULL,\n"
                    + "	`telefono` VARCHAR(255) NULL DEFAULT NULL,\n"
                    + "	`email` VARCHAR(255) NULL DEFAULT NULL,\n"
                    + "	`note` TEXT NULL,\n"
                    + "	`invia_email_prev_ven` CHAR(1) NULL DEFAULT NULL,\n"
                    + "	`invia_email_ordi_ven` CHAR(1) NULL DEFAULT NULL,\n"
                    + "	`invia_email_ddt_ven` CHAR(1) NULL DEFAULT NULL,\n"
                    + "	`invia_email_fatt_ven` CHAR(1) NULL DEFAULT NULL,\n"
                    + "	`invia_email_prev_acq` CHAR(1) NULL DEFAULT NULL,\n"
                    + "	`invia_email_ordi_acq` CHAR(1) NULL DEFAULT NULL,\n"
                    + "	`invia_email_ddt_acq` CHAR(1) NULL DEFAULT NULL,\n"
                    + "	`invia_email_fatt_acq` CHAR(1) NULL DEFAULT NULL,\n"
                    + "	`invia_email_sollecito` CHAR(1) NULL DEFAULT NULL,\n"
                    + "	PRIMARY KEY (`id`)\n"
                    + ")\n"
                    + "ENGINE=MyISAM", "nuova tabella contatti");

//!!!! saltare 809 perchè usato sotto
            agg(810, "", "m.ceccarelli@tnx.it", "alter table clie_forn add includi_invio_email char(1) default 'S'", "Aggiunta colonna note per fattue di vendita");
            agg(811, "", "m.ceccarelli@tnx.it", "alter table clie_forn_dest add includi_invio_email char(1) default 'S'", "Aggiunta colonna note per fattue di vendita");

            agg(812, "", "m.ceccarelli@tnx.it", "update stati set nome = 'TAIWAN' where codice1 = 'TW'", "cambiato nome a taiwan"); //prima era TAIWAN, PROVINCE OF CHINA

            agg(813, "", "m.ceccarelli@tnx.it", "ALTER TABLE `clie_forn`\n"
                    + "	ADD COLUMN `fido_importo` DECIMAL(15,5) NULL AFTER `includi_invio_email`,\n"
                    + "	ADD COLUMN `fido_importo_assic` DECIMAL(15,5) NULL AFTER `fido_importo`,\n"
                    + "	ADD COLUMN `fido_grado` DOUBLE NULL AFTER `fido_importo_assic`,\n"
                    + "	ADD COLUMN `fido_grado_data` DATE NULL AFTER `fido_grado`,\n"
                    + "	ADD COLUMN `fido_obbligatorio` CHAR(1) NULL AFTER `fido_grado_data`;", "campi per gestione fido");

            agg(814, "", "m.ceccarelli@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 20, descrizione = 'Gestione Fido', lettura = 0, scrittura = 1, cancella = 0", "permessi fido");
            agg(815, "", "m.ceccarelli@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 20, descrizione = 'Gestione Fido', lettura = 0, scrittura = 1, cancella = 0", "permessi fido");
            agg(816, "", "m.ceccarelli@tnx.it", "INSERT INTO accessi_ruoli_permessi set id_role = 1, id_privilegio = 20, scrittura = 1;", "nuovo ruolo fido per admin");
            agg(817, "", "m.ceccarelli@tnx.it", "INSERT INTO accessi_ruoli_permessi set id_role = 2, id_privilegio = 20, scrittura = 1;", "nuovo ruolo fido per utente standard");

            agg(818, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi ADD COLUMN nome_contatto_riferimento VARCHAR(50) NULL DEFAULT NULL;", "contatto riferimento");
            agg(819, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt ADD COLUMN nome_contatto_riferimento VARCHAR(50) NULL DEFAULT NULL;", "contatto riferimento");
            agg(820, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt ADD COLUMN nome_contatto_riferimento VARCHAR(50) NULL DEFAULT NULL;", "contatto riferimento");
            agg(821, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto ADD COLUMN nome_contatto_riferimento VARCHAR(50) NULL DEFAULT NULL;", "contatto riferimento");
            agg(822, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt_acquisto ADD COLUMN nome_contatto_riferimento VARCHAR(50) NULL DEFAULT NULL;", "contatto riferimento");
            agg(823, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt_acquisto ADD COLUMN nome_contatto_riferimento VARCHAR(50) NULL DEFAULT NULL;", "contatto riferimento");

            agg(824, "", "m.ceccarelli@tnx.it", "ALTER TABLE movimenti_magazzino add index articolo (articolo);", "movimenti magazzino indici");
            agg(825, "", "m.ceccarelli@tnx.it", "ALTER TABLE movimenti_magazzino add index causale (causale);", "movimenti magazzino indici");
            agg(826, "", "m.ceccarelli@tnx.it", "ALTER TABLE movimenti_magazzino add index deposito (deposito);", "movimenti magazzino indici");

            agg(827, "", "m.ceccarelli@tnx.it", "INSERT INTO accessi_tipi_permessi SET id = 21, descrizione = 'Gestione Ritenute e Rivalse', lettura = 1, scrittura = 1, cancella = 1", "permessi ritenute");
            agg(829, "", "m.ceccarelli@tnx.it", "INSERT INTO accessi_ruoli_permessi set id_role = 1, id_privilegio = 21, scrittura = 1, lettura = 1, cancella = 1;", "nuovo ruolo fido per admin");
            agg(830, "", "m.ceccarelli@tnx.it", "INSERT INTO accessi_ruoli_permessi set id_role = 2, id_privilegio = 21, scrittura = 1, lettura = 1, cancella = 1;", "nuovo ruolo fido per utente standard");

            agg(831, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt ADD COLUMN da_ddt_raggr VARCHAR(150) NULL DEFAULT NULL;", "memorizzo gli id dei ddt dal quale è stata fatta la conversione ragguppando");
            agg(832, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt ADD COLUMN da_ordi_raggr VARCHAR(150) NULL DEFAULT NULL;", "memorizzo gli id dei ddt dal quale è stata fatta la conversione ragguppando");
            agg(833, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt ADD COLUMN da_ordi_raggr VARCHAR(150) NULL DEFAULT NULL;", "memorizzo gli id dei ddt dal quale è stata fatta la conversione ragguppando");
            agg(834, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt_acquisto ADD COLUMN da_ddt_raggr VARCHAR(150) NULL DEFAULT NULL;", "memorizzo gli id dei ddt dal quale è stata fatta la conversione ragguppando");
            agg(835, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt_acquisto ADD COLUMN da_ordi_raggr VARCHAR(150) NULL DEFAULT NULL;", "memorizzo gli id dei ddt dal quale è stata fatta la conversione ragguppando");
            agg(836, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt_acquisto ADD COLUMN da_ordi_raggr VARCHAR(150) NULL DEFAULT NULL;", "memorizzo gli id dei ddt dal quale è stata fatta la conversione ragguppando");

            agg(837, "", "m.ceccarelli@tnx.it", "ALTER TABLE dati_azienda "
                    + " ADD COLUMN stato_def_pre_acq VARCHAR(255) NULL"
                    + ", ADD COLUMN stato_def_ord_acq VARCHAR(255) NULL"
                    + ", ADD COLUMN stato_def_pre_ven VARCHAR(255) NULL"
                    + ", ADD COLUMN stato_def_ord_ven VARCHAR(255) NULL"
                    + "", "stato def");
            agg(838, "", "m.ceccarelli@tnx.it", "update dati_azienda set stato_def_pre_acq = 'Preventivo', stato_def_ord_acq = 'Ordine', stato_def_pre_ven = 'Preventivo', stato_def_ord_ven = 'Ordine'", "stato def");

            agg(837, "", "a.toce@tnx.it", "ALTER TABLE clie_forn_dest ADD COLUMN cod_filconad VARCHAR(150) NULL DEFAULT NULL;", "inserisco il codice filconad nelle destinazioni del cliente");

            agg(839, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi ADD COLUMN campo_libero_1 VARCHAR(100) NULL DEFAULT NULL;", "aggiungo area manager");
            agg(840, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt ADD COLUMN campo_libero_1 VARCHAR(100) NULL DEFAULT NULL;", "aggiungo area manager");
            agg(841, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt ADD COLUMN campo_libero_1 VARCHAR(100) NULL DEFAULT NULL;", "aggiungo area manager");
            agg(842, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ordi_acquisto ADD COLUMN campo_libero_1 VARCHAR(100) NULL DEFAULT NULL;", "aggiungo area manager");
            agg(843, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_ddt_acquisto ADD COLUMN campo_libero_1 VARCHAR(100) NULL DEFAULT NULL;", "aggiungo area manager");
            agg(844, "", "m.ceccarelli@tnx.it", "ALTER TABLE test_fatt_acquisto ADD COLUMN campo_libero_1 VARCHAR(100) NULL DEFAULT NULL;", "aggiungo area manager");
            agg(845, "", "m.ceccarelli@tnx.it", "CREATE TABLE `campi_liberi` (\n"
                    + "	`id` VARCHAR(50) NOT NULL,\n"
                    + "	`nome` VARCHAR(50) NULL,\n"
                    + "	PRIMARY KEY (`id`)\n"
                    + ")\n"
                    + "ENGINE=MyISAM;", "tabella campi liberi");

            agg(795, "", "emanuele.fia@gmail.com", "ALTER TABLE clie_forn ADD COLUMN tipo_causale_trasporto INT(11) UNSIGNED DEFAULT NULL", "Aggiunto campo causale_trasporto per cliente");
            agg(796, "", "emanuele.fia@gmail.com", "ALTER TABLE clie_forn ADD COLUMN tipo_consegna INT(11) UNSIGNED DEFAULT NULL", "Aggiunto campo causale_trasporto per cliente");

            agg(846, "", "m.ceccarelli@tnx.it", "ALTER TABLE `movimenti_magazzino`\n"
                    + "	ADD INDEX `matricola` (`matricola`),\n"
                    + "	ADD INDEX `lotto` (`lotto`);", "indice matricole e lotto su movimenti di magazzino");

            agg(847, "", "m.ceccarelli@tnx.it", "ALTER TABLE `articoli`\n"
                    + "	ADD COLUMN `xmlpa_disp_med_tipo` VARCHAR(35) NULL,\n"
                    + "	ADD COLUMN `xmlpa_disp_med_valore` VARCHAR(35) NULL", "tipo e valore disp medico xmlpa");

            agg(848, "", "m.ceccarelli@tnx.it", "ALTER TABLE `articoli` \n"
                    + "	ADD INDEX `categoria` (`categoria`),\n"
                    + "	ADD INDEX `sottocategoria` (`sottocategoria`);", "indici articoli");

            agg(849, "", "m.ceccarelli@tnx.it", "ALTER TABLE `righ_ordi` "
                    + " CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT FIRST, "
                    + " CHANGE COLUMN `id_padre` `id_padre` INT(11) NOT NULL AFTER `id`;", "id primo campo righ");
            agg(850, "", "m.ceccarelli@tnx.it", "ALTER TABLE `righ_ordi_acquisto` "
                    + " CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT FIRST, "
                    + " CHANGE COLUMN `id_padre` `id_padre` INT(11) NOT NULL AFTER `id`;", "id primo campo righ");
            agg(851, "", "m.ceccarelli@tnx.it", "ALTER TABLE `righ_ddt` "
                    + " CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT FIRST, "
                    + " CHANGE COLUMN `id_padre` `id_padre` INT(11) NOT NULL AFTER `id`;", "id primo campo righ");
            agg(852, "", "m.ceccarelli@tnx.it", "ALTER TABLE `righ_ddt_acquisto` "
                    + " CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT FIRST, "
                    + " CHANGE COLUMN `id_padre` `id_padre` INT(11) NOT NULL AFTER `id`;", "id primo campo righ");
            agg(853, "", "m.ceccarelli@tnx.it", "ALTER TABLE `righ_fatt` "
                    + " CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT FIRST, "
                    + " CHANGE COLUMN `id_padre` `id_padre` INT(11) NOT NULL AFTER `id`;", "id primo campo righ");
            agg(854, "", "m.ceccarelli@tnx.it", "ALTER TABLE `righ_fatt_acquisto` "
                    + " CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT FIRST, "
                    + " CHANGE COLUMN `id_padre` `id_padre` INT(11) NOT NULL AFTER `id`;", "id primo campo righ");

            //sistemazioni pre mobile
            agg(855, "", "m.ceccarelli@tnx.it", "ALTER TABLE `articoli_confezione` \n"
                    + "ADD COLUMN `id` INT NOT NULL AUTO_INCREMENT FIRST, \n"
                    + " DROP PRIMARY KEY,\n"
                    + " ADD PRIMARY KEY (`id`),\n"
                    + " ADD UNIQUE INDEX `articolo_padre_articolo_figlio` (`articolo_padre`, `articolo_figlio`);\n", "premobile");

            agg(856, "", "m.ceccarelli@tnx.it", "ALTER TABLE `banche_cab`\n"
                    + " ADD COLUMN `id` INT NOT NULL AUTO_INCREMENT FIRST,\n"
                    + " DROP PRIMARY KEY,\n"
                    + " ADD PRIMARY KEY (`id`),\n"
                    + " ADD UNIQUE INDEX `abi_cab` (`abi`, `cab`);\n", "premobile");

//TODO spostare su plugin ritenute
//            agg(857, "", "m.ceccarelli@tnx.it", " ALTER TABLE `certificati`\n"
//                    + " ADD COLUMN `id_pk` INT NOT NULL AUTO_INCREMENT FIRST,\n"
//                    + " DROP PRIMARY KEY,\n"
//                    + " ADD PRIMARY KEY (`id_pk`),\n"
//                    + " ADD UNIQUE INDEX `certificati` (`id`, `cliente`, `anno`);\n", "premobile");
//
//            agg(858, "", "m.ceccarelli@tnx.it", " ALTER TABLE `certificati_acq`\n"
//                    + " ADD COLUMN `id_pk` INT NOT NULL AUTO_INCREMENT FIRST,\n"
//                    + " DROP PRIMARY KEY,\n"
//                    + " ADD PRIMARY KEY (`id_pk`),\n"
//                    + " ADD UNIQUE INDEX `certificati_acq` (`id`, `fornitore`, `anno`);\n", "premobile");

            agg(859, "", "m.ceccarelli@tnx.it", "ALTER TABLE `dati_azienda`\n"
                    + " CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT FIRST;\n", "premobile");

            agg(860, "", "m.ceccarelli@tnx.it", "ALTER TABLE `stati`\n"
                    + " DROP INDEX `indice_codice1`,\n"
                    + " ADD PRIMARY KEY (`codice1`);\n", "premobile");

            agg(861, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_fatt_acquisto_iva`\n"
                    + " ADD COLUMN `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT FIRST,\n"
                    + " DROP PRIMARY KEY,\n"
                    + " ADD PRIMARY KEY (`id`),\n"
                    + " ADD UNIQUE INDEX `id_padre_codice_iva` (`id_padre`, `codice_iva`);\n", "premobile");

            agg(862, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_fatt_iva`\n"
                    + " ADD COLUMN `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT FIRST,\n"
                    + " DROP PRIMARY KEY,\n"
                    + " ADD PRIMARY KEY (`id`),\n"
                    + " ADD UNIQUE INDEX `id_padre_codice_iva` (`id_padre`, `codice_iva`);\n", "premobile");
            
            agg(863, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ddt` CHANGE COLUMN `stato` `stato` CHAR(1) NULL;", "premobile");
            agg(864, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ddt_acquisto` CHANGE COLUMN `stato` `stato` CHAR(1) NULL;", "premobile");
            agg(865, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_fatt` CHANGE COLUMN `stato` `stato` CHAR(1) NULL;", "premobile");
            agg(866, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ordi` CHANGE COLUMN `stato` `stato` CHAR(1) NULL;", "premobile");
            agg(867, "", "m.ceccarelli@tnx.it", "ALTER TABLE `test_ordi_acquisto` CHANGE COLUMN `stato` `stato` CHAR(1) NULL;", "premobile");

            mb.out("fine inserimento log");

            esegui_aggs();

            mb.out("fine esecuzione log");

            //attivazione
            if (checkLog(520, "", "m.ceccarelli@tnx.it") == false) {
                List<Map> list = DbUtils.getListMap(Db.getConn(), "select * from attivazioni where msg like '%ESITO:Attivato%' "
                        + " and (msg like '%Professional%' "
                        + " or msg like '%Enterprise%'"
                        + " ) "
                        + "order by data desc limit 1");
                //Attivazione ID:XXX ORDINE:XXX ESITO:Attivato: Invoicex Enterprise
                if (list.size() > 0) {
                    try {
                        String log = cu.toString(list.get(0).get("msg"));
                        Date ts = cu.toDate(list.get(0).get("data"));
                        String codice = "";
                        String versione = "";
                        codice = StringUtils.substringBefore(StringUtils.substringAfter(log, "ORDINE:"), " ESITO:");
                        versione = StringUtils.substringAfter(log, "Attivato: ");
                        sql = "delete from attivazione";
                        DbUtils.tryExecQuery(Db.getConn(), sql);
                        sql = "insert into attivazione (codice, versione, esito_log, ts) values (" + Db.pc(codice, Types.VARCHAR) + ", " + Db.pc(versione, Types.VARCHAR) + ", " + Db.pc(log, Types.VARCHAR) + ", " + Db.pc(ts, Types.TIMESTAMP) + ")";
                        DbUtils.tryExecQuery(Db.getConn(), sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                writeLog(520, "", "m.ceccarelli@tnx.it", "");
            }

            //scadenze
            int id = 585;
            if (checkLog(id, "", "m.ceccarelli@tnx.it") == false) {
                try {
                    InvoicexUtil.ricalcolaPagatoDaPagare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                writeLog(id, "", "m.ceccarelli@tnx.it", "aggiornamento campi pagato da pagare in scadenze");
            }

            id = 587;
            if (checkLog(id, "", "m.ceccarelli@tnx.it") == false) {
                try {
                    InvoicexUtil.ricalcolaTotaleScadenze();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                writeLog(id, "", "m.ceccarelli@tnx.it", "aggiornamento campo numero_totale scadenze");
            }

            id = 597;
            if (checkLog(id, "", "m.ceccarelli@tnx.it") == false) {
                sql = "select export_fatture_estrai_acconti from dati_azienda";
                try {
                    DbUtils.tryOpenResultSet(Db.getConn(), sql);
                } catch (Exception e) {
                    DbUtils.tryExecQuery(Db.getConn(), "ALTER TABLE `dati_azienda` ADD COLUMN `export_fatture_estrai_acconti` char(1) NULL");
                    sql = "update dati_azienda set export_fatture_estrai_acconti = 'N'";
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                }
                writeLog(id, "", "m.ceccarelli@tnx.it", "flag estrazione acconti ready tec");
            }

            //provvigioni collegate ad id_doc e id_scadenza
            id = 776;
            if (checkLog(id, "", "m.ceccarelli@tnx.it") == false) {
                try {
                    writeLog(id, "", "m.ceccarelli@tnx.it", "aggiornamento id_doc e id_scadenza delle provvigioni");
                    main.splash("aggiornamenti struttura database: struttura scadenze e provvigioni", true);
                    if (main.db_in_rete) {
                        SwingUtils.showInfoMessage(padre, "Attenzione, è necessario aggiornare la struttura del database.\nSe altri utenti stanno utilizzando Invoicex\ndovrebbero uscire prima di chiudere questo avviso !");
                    }
                    InvoicexUtil.aggiornaStrutturaScadenzeProvvigioniPerId();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //provvigioni collegate ad id_doc e id_scadenza
            id = 777;
            if (checkLog(id, "", "m.ceccarelli@tnx.it") == false) {
                try {
                    //controllo se eseguirlo o meno
                    //eseguirlo quando c'è provvigioni_copia_pre_id, l'id log 776 ma non c'è scadenze_copia_pre_id
                    if (dbu.existTable(Db.getConn(), "provvigioni_copia_pre_id") && checkLog(776, "", "m.ceccarelli@tnx.it") && !dbu.existTable(Db.getConn(), "scadenze_copia_pre_id")) {
                        main.splash("aggiornamenti struttura database: struttura scadenze e provvigioni", true);
                        if (main.db_in_rete) {
                            SwingUtils.showInfoMessage(padre, "Attenzione, è necessario aggiornare la struttura del database.\nSe altri utenti stanno utilizzando Invoicex\ndovrebbero uscire prima di chiudere questo avviso !");
                        }
                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                        }
                        if (dbu.existTable(Db.getConn(), "provvigioni_copia_pre_id") && checkLog(776, "", "m.ceccarelli@tnx.it") && !dbu.existTable(Db.getConn(), "scadenze_copia_pre_id")) {
                            writeLog(id, "", "m.ceccarelli@tnx.it", "aggiornamento id_doc e id_scadenza delle provvigioni");
                            dbu.tryExecQuery(Db.getConn(), "RENAME TABLE `provvigioni_copia_pre_id` TO `provvigioni_copia_pre_id_crash`");
                            InvoicexUtil.aggiornaStrutturaScadenzeProvvigioniPerId();
                        }
                    } else if (dbu.existTable(Db.getConn(), "provvigioni_copia_pre_id") && checkLog(776, "", "m.ceccarelli@tnx.it") && dbu.existTable(Db.getConn(), "scadenze_copia_pre_id")) {
                        writeLog(id, "", "m.ceccarelli@tnx.it", "aggiornamento id_doc e id_scadenza delle provvigioni");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            /* commento perchè fatto in altro modo vedi magazzino toys
             //costo medio ponderato
             //non da ddt solo da fatture            
             //come fare per codici assortimento ?            
             id = 784;
             if (checkLog(id, "", "m.ceccarelli@tnx.it") == false) {
             try {
             writeLog(id, "", "m.ceccarelli@tnx.it", "aggiornamento costo medio ponderato");
             main.splash("aggiornamenti struttura database: costo medio ponderato", true);
             InvoicexUtil.aggiornaCostoMedioPonderato(Db.getConn());
             } catch (Exception e) {
             e.printStackTrace();
             }
             }
             */
            id = 809;
            if (checkLog(id, "", "m.ceccarelli@tnx.it") == false) {
                try {
                    List<Map> list = dbu.getListMap(Db.getConn(), "select codice, persona_riferimento, telefono_riferimento from clie_forn where ifnull(persona_riferimento,'') != '' or ifnull(telefono_riferimento,'') != ''");
                    int i = 0;
                    String sqltoexec = "";
                    for (Map cf : list) {
                        try {
                            Map contatto = new HashMap();
                            contatto.put("id_clifor", cf.get("codice"));
                            contatto.put("tipo", "Principale");
                            contatto.put("nome", cf.get("persona_riferimento"));
                            contatto.put("telefono", cf.get("telefono_riferimento"));
                            sql = "insert into clie_forn_contatti set " + dbu.prepareSqlFromMap(contatto);
                            System.out.println("sql = " + sql);

                            sqltoexec += sql + ";\n";
                            //dbu.tryExecQuery(Db.getConn(), sql);

                            if (i % 100 == 0) {
                                main.splash("aggiornamenti struttura database: conversione contatti " + ((int) ((double) i * 100d / (double) list.size())) + "%", true);
                                dbu.tryExecQuery(Db.getConn(), sqltoexec);
                                sqltoexec = "";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        i++;
                    }

                    if (i % 100 != 0) {
                        dbu.tryExecQuery(Db.getConn(), sqltoexec);
                    }

                    writeLog(id, "", "m.ceccarelli@tnx.it", "portato contatto principale");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            id = 1000;
            if (checkLog(id, "", "m.ceccarelli@tnx.it") == false) {
                sql = "CREATE TABLE `db_version` (\n"
                        + "	`modulo` VARCHAR(150) NOT NULL,\n"
                        + "	`versione` INT NULL,\n"
                        + "	`ts` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n"
                        + "     `running_chi` VARCHAR(100) NULL,\n"
                        + "     `running_quando` TIMESTAMP NULL\n"
                        + ", PRIMARY KEY (`modulo`))\n"
                        + "COLLATE='utf8_general_ci'\n"
                        + "ENGINE=InnoDB;";
                try {
                    dbu.tryExecQuery(Db.getConn(), sql, false, true);
                    sql = "insert into db_version set modulo = 'invoicex', versione = 1";
                    dbu.tryExecQuery(Db.getConn(), sql, false, true);
                    writeLog(id, "", "m.ceccarelli@tnx.it", "passati a db_version");
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore nell'aggiornamento della struttura del database (db_version):\n" + e.getMessage(), true);
                }
            }

            mb.out("fine check table attivazione log");

            //controllo engine delle tabelle
            //24/05/2016 non faccio più il controllo, le tabelle devono essere create con il giusto motore di database
//            checkTableEngineInno();
            //rimuovo lock dbchanges
            try {
                String sqlrun = "delete from log2_running";
                dbu.tryExecQuery(Db.getConn(), sqlrun);
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtils.showExceptionMessage(main.splash, e);
            }

            mb.out("fine check engine log");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                statLog.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(mb.getDiff("fine agg db log2 " + this.getClass().toString()));
    }

    private void agg18() {
        int id_log = 18;
        String id_plugin = "";
        String id_email = "m.ceccarelli@tnx.it";
        String note = "sistemazione movimenti ddt di acquisto per bug ver 2010-10";

        if (checkLog(id_log, id_plugin, id_email) == false) {
            try {
                List<Map> list = DbUtils.getListMap(Db.getConn(), "select serie, numero, anno, id from test_ddt_acquisto order by id");
                for (Map m : list) {
                    try {
                        dbDocumento prev = new dbDocumento();
                        prev.serie = (String) m.get("serie");
                        prev.numero = CastUtils.toInteger(m.get("numero"));
                        prev.anno = CastUtils.toInteger(m.get("anno"));
                        prev.setId(CastUtils.toInteger(m.get("id")));
                        prev.tipoDocumento = Db.TIPO_DOCUMENTO_DDT_ACQUISTO;
                        prev.acquisto = true;
                        if (prev.generaMovimentiMagazzino() == false) {
                            System.out.println("agg18: !!! problema: " + prev.serie + " " + prev.numero + " " + prev.anno + " " + prev.getId());
                        } else {
                            System.out.println("agg18: ok: " + prev.serie + " " + prev.numero + " " + prev.anno + " " + prev.getId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
            writeLog(id_log, id_plugin, id_email, note);
        }
    }

    public void pre_check(int id_log, String id_plugin, String id_email) {
    }

    public void post_check(int id_log, String id_plugin, String id_email) {
    }

    public void post_execute_ok(int id_log, String id_plugin, String id_email, String sql) {
    }

    public void post_execute_ko(int id_log, String id_plugin, String id_email, String sql) {
    }

    public void post_execute_ko(int id_log, String id_plugin, String id_email, String sql, Exception err) {
    }

    public void post_check_ok(int id_log, String id_plugin, String id_email) {
    }

    private void checkTableEngineInno() {
        //DISATTIVATO
        try {
            if (Sync.isActive() || dbu.existTable(Db.getConn(), "sync_config")) {

                //controllo le foreign key
                /*
                 try {
                 List<Map> status = DbUtils.getListMap(Db.getConn(true), "SHOW TABLE STATUS");
                 for (Map rec : status) {
                 System.out.println("tab = " + rec.get("Name") + " engine: " + rec.get("Engine"));
                 if (rec != null && rec.get("Name") != null && rec.get("Engine") != null && rec.get("Engine").toString().equalsIgnoreCase("InnoDB")) {
                 try {
                 String name = (String) rec.get("Name");
                 System.out.println("controllo foreign key per tabella " + rec.get("Name") + " con engine " + rec.get("Engine"));

                 String sql = "SELECT * FROM information_schema.TABLE_CONSTRAINTS \n"
                 + "WHERE information_schema.TABLE_CONSTRAINTS.CONSTRAINT_TYPE = 'FOREIGN KEY' \n"
                 + "AND information_schema.TABLE_CONSTRAINTS.TABLE_SCHEMA = DATABASE() \n"
                 + "AND information_schema.TABLE_CONSTRAINTS.TABLE_NAME = '" + name + "'";
                 List<Map> list = DbUtils.getListMap(Db.getConn(), sql);
                 if (list != null && list.size() > 0) {
                 for (Map m : list) {
                 if (cu.s(m.get("CONSTRAINT_TYPE")).equals("FOREIGN KEY")) {
                 sql = "alter table " + name + " drop FOREIGN KEY " + m.get("CONSTRAINT_NAME");
                 System.out.println(sql);
                 dbu.tryExecQuery(Db.getConn(), sql);
                 }
                 }
                 }

                 } catch (Exception ex) {
                 ex.printStackTrace();

                 }
                 }
                 }
                 } catch (Exception e) {
                 e.printStackTrace();
                 }
                 */
                return;
            }
            main.splash("aggiornamenti struttura database: controllo engine tabelle", 50);

            List<Map> status = DbUtils.getListMap(Db.getConn(true), "SHOW TABLE STATUS");
            for (Map rec : status) {
                if (cu.toString(rec.get("Name")).startsWith("pn_")) {
                    System.out.println("ignoro tabelle prima nota:" + rec.get("Name"));
                    continue;
                }
                System.out.println("tab = " + rec.get("Name") + " engine: " + rec.get("Engine"));
                if (rec != null && rec.get("Name") != null && rec.get("Engine") != null && !rec.get("Engine").toString().equalsIgnoreCase("MyISAM")) {
                    try {
                        String name = (String) rec.get("Name");
                        System.out.println("la tabella " + rec.get("Name") + " è con engine " + rec.get("Engine") + ", cambio in MyISAM");
                        //prima faccio dump
                        String nomeFileDump = CurrentDir.getCurrentDir() + "/backup/dump_per_engine_";
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
                        Date d = new Date();
                        nomeFileDump += name + "_" + sdf.format(d) + ".txt";
                        FileOutputStream fos = new FileOutputStream(nomeFileDump, false);
                        it.tnx.Util.dumpTable(name, Db.getConn(), fos);
                        System.out.println(name + " : dumped");
                        fos.close();
                        //poi cambio
                        DbUtils.tryExecQuery(Db.getConn(true), "alter table `" + name + "` ENGINE=MyISAM");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        main.splash("aggiornamenti struttura database: controllo engine tabelle completato", 100);
    }

}
