/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gestioneFatture;

import it.tnx.Db;
import it.tnx.commons.DbUtils;
import it.tnx.commons.MicroBench;
import it.tnx.invoicex.gui.JFrameIntro2;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 *
 * @author mceccarelli
 */
public class DbChanges {
//    public frmIntro splash = null;
    public JFrameIntro2 splash = null;
    public iniFileProp fileIni;
    Statement statLog = null;
    public List logs = null;
    int max = 276;
    boolean exist_ricevute = true;
    String nome_ricevute = "_ricevute";
    String testate_ricevute = "fatture_ricevute_teste";
    
    Exception excostr = null;

    public DbChanges() throws Exception {
        statLog = Db.getConn().createStatement();
        this.logs = DbUtils.getListInt(Db.getConn(), "select id from log");
    }

    private boolean agg(int idAggiornamento, String sql, String note) {
//        main.splash("aggiornamenti struttura database: [1/2] " + idAggiornamento + "/" + max, idAggiornamento * 100 / max);
        main.splash("aggiornamenti struttura database: [1/2] " + idAggiornamento + "/" + max, 25 + (idAggiornamento * 25 / max));

        if (checkLog(idAggiornamento) == false) {
            try {
                if (Db.executeSql(sql)) {
                    writeLog(idAggiornamento, note);
                } else {
                    System.out.println("!!! Errore in agg. db !!!");
                }
            } catch (Exception err) {
                if (err.toString().indexOf("Duplicate column name") >= 0) {
                    //se il campo c'è già'
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

    boolean writeLog(int id, String desc) {

        try {
            statLog.execute("insert into log (id,descr) values (" + id + "," + Db.pc(desc, "VARCHAR") + ")");
            return (true);
        } catch (Exception err) {
            err.printStackTrace();
            return (false);
        }
    }

    boolean checkLog(int id) {
//        try {
//            ResultSet temp = statLog.executeQuery("select * from log where id = " + id);
//            if (temp.next() == true) {
//                //trovato il log desiderato
//                return (true);
//            } else {
//                return (false);
//            }
//        } catch (Exception err) {
//            err.printStackTrace();
//            //System.exit(1);
//            return (false);
//        }
        return (logs.contains(Integer.valueOf(id)));
    }

    private String aggiornamento2_1() {

        String temp = "alter table clie_forn ";
        temp += "add cfiscale varchar(16),";
        temp += "add pagamento varchar(20),";
        temp += "add banca varchar(30),";
        temp += "add cantiere_indirizzo varchar(50),";
        temp += "add cantiere_cap varchar(10),";
        temp += "add cantiere_localita varchar(30),";
        temp += "add cantiere_provincia varchar(2),";
        temp += "add finanziamento char(1),";
        temp += "add documenti_iva_agevolata char(1),";
        temp += "add aliquota varchar(5)";

        return (temp);
    }

    private String aggiornamento2_2() {

        String temp = "alter table prev_test ";
        temp += "add cantiere_indirizzo varchar(50),";
        temp += "add cantiere_cap varchar(10),";
        temp += "add cantiere_localita varchar(30),";
        temp += "add cantiere_provincia varchar(2),";
        temp += "add finanziamento char(1),";
        temp += "add documenti_iva_agevolata char(1),";
        temp += "add aliquota varchar(5),";
        temp += "add stato char(1) default 'P' not null ";

        return (temp);
    }

    private String aggiornamento4_1() {

        String temp = "create table log (";
        temp += "id int not null,";
        temp += "descr varchar(255),";
        temp += "data timestamp not null, PRIMARY KEY (id)) ENGINE=MyISAM";

        return (temp);
    }

    private String aggiornamento4_2() {

        String temp = "alter table prev_test ";
        temp += "add cantiere_nome varchar(100),";
        temp += "add fatturazione_nome varchar(100),";
        temp += "add fatturazione_cfiscale varchar(16),";
        temp += "add fatturazione_piva varchar(16),";
        temp += "add fatturazione_indirizzo varchar(50),";
        temp += "add fatturazione_cap varchar(10),";
        temp += "add fatturazione_localita varchar(30),";
        temp += "add fatturazione_provincia varchar(2)";

        return (temp);
    }

    private String aggiornamento4_3() {

        String temp = "alter table clie_forn ";
        temp += "add cantiere_nome varchar(100)";

        return (temp);
    }

    private String aggiornamento6_1() {

        String temp = "alter table prev_righ ";
        temp += "add stato char(1) default 'P' not null";

        return (temp);
    }

    private String aggiornamento6_2() {

        String temp = "alter table prev_righ ";
        temp += "drop primary key";

        return (temp);
    }

    private String aggiornamento6_3() {

        String temp = "alter table prev_righ ";
        temp += "add primary key (serie,numero,riga,riga_variante,stato)";

        return (temp);
    }

    private String aggiornamento8_1() {

        String temp = "alter table prev_test ";
        temp += "add data_ordine date";

        return (temp);
    }

    private String aggiornamento24_1() {

        String temp = "ALTER TABLE prev_righ ";
        temp += "modify f_tipo_legno varchar(100)";

        return (temp);
    }

    private String aggiornamento25_1() {

        String temp = "ALTER TABLE prev_righ ";
        temp += "add f_vetro varchar(50)";

        return (temp);
    }

    private String aggiornamento26_1() {

        String temp = "alter table portoni ";
        temp += "drop primary key";

        return (temp);
    }

    private String aggiornamento26_2() {

        String temp = "alter table portoni ";
        temp += "add primary key (tipo_articolo, tipo_legno, tipo_laccatura, tipo_portone, sp, ante)";

        return (temp);
    }

    private String aggiornamento27_1() {

        String temp = "ALTER TABLE portoni ";
        temp += "modify tipo_portone varchar(10) not null";

        return (temp);
    }

    private String aggiornamento27_2() {

        String temp = "ALTER TABLE portoni_varianti ";
        temp += "modify tipo_portone varchar(10)";

        return (temp);
    }

    private String aggiornamento28_1() {

        String temp = "ALTER TABLE prev_righ ";
        temp += "add f_vetro_pendenza varchar(1),";
        temp += "add f_vetro_centinato varchar(1)";

        return (temp);
    }

    private String aggiornamento29_1() {

        String temp = "ALTER TABLE prev_test ";
        temp += "add prn_prezzi_lordi varchar(1)";

        return (temp);
    }

    private String aggiornamento47_1() {

        String sql = "" + "CREATE TABLE `test_ordi` (" + "`serie` char(1) NOT NULL default ''," + "`numero` int(10) unsigned NOT NULL default '0'," + "`anno` int(10) unsigned NOT NULL default '0'," + "`cliente` int(10) unsigned NOT NULL default '0'," + "`cliente_destinazione` int(10) unsigned default NULL," + "`data` date NOT NULL default '0000-00-00'," + "`data_consegna` varchar(255) default NULL," + "`pagamento` varchar(35) default NULL," + "`banca_abi` varchar(5) default NULL," + "`banca_cab` varchar(5) default NULL," + "`banca_cc` varchar(35) default NULL," + "`spese_varie` decimal(12,2) default NULL," + "`note` text," + "`note_testa` text," + "`note_corpo` text," + "`note_piede` text," + "`totale_imponibile` decimal(12,2) default NULL," + "`totale_iva` decimal(12,2) default NULL," + "`totale` decimal(12,2) default NULL," + "`sconto1` decimal(4,2) default NULL," + "`sconto2` decimal(4,2) default NULL," + "`riferimento` varchar(255) default NULL," + "`sconto3` decimal(4,2) default NULL," + "`stato` char(1) NOT NULL default ''," + "`codice_listino` tinyint(3) unsigned NOT NULL default '1'," + "`stampato` datetime NOT NULL default '0000-00-00 00:00:00'," + "`spese_trasporto` decimal(10,2) default NULL," + "`spese_incasso` decimal(10,5) default NULL," + "`dest_ragione_sociale` varchar(100) default NULL," + "`dest_indirizzo` varchar(50) default NULL," + "`dest_cap` varchar(10) default NULL," + "`dest_localita` varchar(30) default NULL," + "`dest_provincia` char(2) default NULL," + "`dest_telefono` varchar(20) default NULL," + "`dest_cellulare` varchar(20) default NULL," + "`tipo_fattura` tinyint(4) default NULL," + "`aspetto_esteriore_beni` varchar(255) default NULL," + "`numero_colli` varchar(30) default NULL," + "`peso_lordo` varchar(20) default NULL," + "`peso_netto` varchar(20) default NULL," + "`vettore1` varchar(255) default NULL," + "`porto` varchar(100) default NULL," + "`causale_trasporto` varchar(100) default NULL," + "`mezzo_consegna` varchar(25) default NULL," + "`opzione_riba_dest_diversa` char(1) default 'N'," + "`note_pagamento` text," + "`agente_codice` int(11) default NULL," + "`agente_percentuale` decimal(3,2) default '0.00'," + "PRIMARY KEY  (`serie`,`numero`,`anno`)" + ") ENGINE=MyISAM";

        return sql;
    }

    private String aggiornamento47_2() {

        String sql = "" + "CREATE TABLE `righ_ordi` (" + "`serie` char(1) NOT NULL default ''," + "`numero` int(10) unsigned NOT NULL default '0'," + "`anno` int(10) unsigned NOT NULL default '0'," + "`riga` int(10) unsigned NOT NULL default '0'," + "`codice_articolo` varchar(20) default NULL," + "`descrizione` varchar(255) default NULL," + "`um` char(3) default NULL," + "`quantita` decimal(8,2) default NULL," + "`prezzo` decimal(12,5) NOT NULL default '0.00000'," + "`iva` decimal(4,2) default NULL," + "`sconto1` decimal(4,2) default NULL," + "`sconto2` decimal(4,2) default NULL," + "`stato` char(1) NOT NULL default ''," + "`ddt_serie` char(1) NOT NULL default ''," + "`ddt_numero` int(10) unsigned NOT NULL default '0'," + "`ddt_anno` int(10) unsigned NOT NULL default '0'," + "`ddt_riga` int(10) unsigned NOT NULL default '0'," + "`riga_speciale` char(1) default NULL," + "PRIMARY KEY  (`serie`,`numero`,`riga`,`anno`)," + "FULLTEXT KEY `codice_articolo` (`codice_articolo`)" + ") ENGINE=MyISAM";

        return sql;
    }

    private void cambioCampoIva(String tab) {
        String sql = "";
        sql = "alter table " + tab + " add column iva_conv varchar(100)";
        Db.executeSql(sql);

        sql = "update " + tab + " set iva_conv = cast(iva as UNSIGNED)";
        Db.executeSql(sql);

        sql = "update " + tab + " set iva = null";
        Db.executeSql(sql);

        sql = "alter table " + tab + " modify column iva char(3) null";
        Db.executeSql(sql);

        sql = "update " + tab + " set iva = iva_conv";
        Db.executeSql(sql);
    }


    public void esegui_aggiornamenti() {
        MicroBench mb = new MicroBench();
        mb.start();

        System.out.println("inizio controllo aggiornamenti db");

        if (splash != null) {
            splash.jProgressBar1.setIndeterminate(true);
        }

        int idAggiornamento = 0;

        try {
            
            idAggiornamento = 84;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "ALTER TABLE `dati_azienda` ADD COLUMN `flag_dati_inseriti` char(1) NULL";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiugno campi alla dati_azienda");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 85;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "ALTER TABLE `dati_azienda` ADD COLUMN `sito_web` varchar(200) NULL";
                String sql2 = "ALTER TABLE `dati_azienda` ADD COLUMN `email` varchar(200) NULL";
                try {
                    
                    Db.executeSql(sql1);
                    Db.executeSql(sql2);
                    writeLog(idAggiornamento, "aggiugno campi alla dati_azienda");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 86;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "ALTER TABLE `clie_forn` ADD COLUMN `banca_cc_iban` varchar(100) NULL";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiugno iban a clie_forn");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 87;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "ALTER TABLE `dati_azienda_banche` ADD COLUMN `cc_iban` varchar(100) NULL";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiugno iban a dati_azienda_banche");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 88;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "ALTER TABLE `test_fatt` ADD COLUMN `banca_iban` varchar(100) NULL";
                String sql2 = "ALTER TABLE `test_ddt` ADD COLUMN `banca_iban` varchar(100) NULL";
                String sql3 = "ALTER TABLE `test_ordi` ADD COLUMN `banca_iban` varchar(100) NULL";
                try {
                    
                    Db.executeSql(sql1);
                    Db.executeSql(sql2);
                    Db.executeSql(sql3);
                    writeLog(idAggiornamento, "aggiugno iban a test_");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 89;
            if (checkLog(idAggiornamento) == false) {
                
    //            if (JOptionPane.showConfirmDialog(splash, "Nella nuova versione c'è un nuovo modo di stampa, lo vuoi attivare ?\nUna volta attivato puoi reimpostarlo dalle Impostazioni", "Attenzione", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    //param def
                    try {
                        java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);
                        preferences.put("tipoStampa", "fattura_mod2_default.jrxml");
                        preferences.put("tipoStampaFA", "fattura_acc_mod2_default.jrxml");
                        preferences.put("tipoStampaDDT", "ddt_mod2_default.jrxml");
                        preferences.put("tipoStampaOrdine", "ordine_default.jrxml");
                        preferences.sync();
                        fileIni.setValue("pref", "tipoStampa", "fattura_mod2_default.jrxml");
                        fileIni.setValue("pref", "tipoStampaFA", "fattura_acc_mod2_default.jrxml");
                        fileIni.setValue("pref", "tipoStampaDDT", "ddt_mod2_default.jrxml");
                        fileIni.setValue("pref", "tipoStampaOrdine", "ordine_default.jrxml");
                    } catch (Exception ex1) {
                        ex1.printStackTrace();
                    }
    //            }
                
                String sql1 = "ALTER TABLE `test_fatt` ADD COLUMN `ritenuta` int NULL";
                String sql2 = "ALTER TABLE `clie_forn` ADD COLUMN `ritenuta` int NULL";
                String sql3 = "alter table test_fatt add column totale_ritenuta decimal(15,5)";
                String sql4 = "alter table test_fatt add column totale_da_pagare decimal(15,5)";
                try {
                    
                    Db.executeSql(sql1);
                    Db.executeSql(sql2);
                    Db.executeSql(sql3);
                    Db.executeSql(sql4);
                    writeLog(idAggiornamento, "ritenute");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 90;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "alter table test_fatt add column dataoraddt varchar(50)";
                String sql2 = "alter table test_ddt add column dataoraddt varchar(50)";
                try {
                    
                    Db.executeSql(sql1);
                    Db.executeSql(sql2);
                    writeLog(idAggiornamento, "data ora ddt");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 91;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "alter table articoli add peso_kg decimal(15,5)";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiungo il peso sugli articoli");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            if (!DbUtils.existTable(Db.getConn(), "righ_fatt_ricevute")) {
                exist_ricevute = false;
                nome_ricevute = "_acquisto";
                testate_ricevute = "test_fatt_acquisto";
            }
            
            idAggiornamento = 92;
            if (checkLog(idAggiornamento) == false) {
                try {
                    
                    cambioCampoIva("righ_fatt");
                    cambioCampoIva("righ_fatt" + nome_ricevute);
                    cambioCampoIva("righ_ddt");
                    cambioCampoIva("righ_ordi");
                    writeLog(idAggiornamento, "codice iva a char di 3 invece che numerico con deicmali");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 93;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "ALTER TABLE righ_fatt MODIFY COLUMN ddt_serie CHAR(1) NOT NULL DEFAULT ''";
                String sql2 = "ALTER TABLE righ_fatt" + nome_ricevute + " MODIFY COLUMN ddt_serie CHAR(1) NOT NULL DEFAULT ''";
                try {
                    
                    Db.executeSql(sql1);
                    Db.executeSql(sql2);
                    writeLog(idAggiornamento, "problemi mysql 5.0.51a");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 94;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "ALTER TABLE test_ordi ADD COLUMN stato_ordine varchar(200) NULL DEFAULT ''";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiungo stato_ordine");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 95;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "ALTER TABLE clie_forn ADD COLUMN campo1 varchar(200) NULL DEFAULT ''";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiungo campo libero 1");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 96;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "ALTER TABLE distinte_riba AUTO_INCREMENT = 0";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "resetto il numero dist riba per bug su 175");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 97;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "insert into tipi_fatture values (6, 'FP', 'FATTURA PRO-FORMA')";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiungo il tipo fattura pro forma");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 98;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "alter table righ_fatt modify column descrizione text";
                String sql2 = "alter table righ_fatt" + nome_ricevute + " modify column descrizione text";
                String sql3 = "alter table righ_ddt modify column descrizione text";
                String sql4 = "alter table righ_ordi modify column descrizione text";
                String sql5 = "alter table articoli modify column descrizione text";
                try {
                    
                    Db.executeSql(sql1);
                    Db.executeSql(sql2);
                    Db.executeSql(sql3);
                    Db.executeSql(sql4);
                    Db.executeSql(sql5);
                    writeLog(idAggiornamento, "modifico la descrizione in text");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 99;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "alter table articoli add peso_kg_collo decimal(15,5)";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiungo il peso sugli articoli per collo");

                    Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);
                    preferences.putBoolean("soloItaliano", true);

                    fileIni.setValue("pref", "soloItaliano", "true");

                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 100;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "alter table codici_iva modify column percentuale decimal(5,2)";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiorno perc iva");

                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
            idAggiornamento = 101;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "update codici_iva set percentuale = 20 where codice = '20'";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiorno perc iva");

                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 102;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "alter table " + testate_ricevute + " add column giorno_pagamento tinyint(4) default NULL";
                String sql2 = "alter table " + testate_ricevute + " add column pagamento varchar(35) default NULL";
                try {
                    Db.executeSql(sql1);
                    Db.executeSql(sql2);
                    writeLog(idAggiornamento, "aggiorno fatture ricevute teste x pagamento");
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 103;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "alter table test_fatt modify column stato char(1) NULL";
                String sql2 = "alter table test_ddt modify column stato char(1) NULL";
                String sql3 = "alter table test_ordi modify column stato char(1) NULL";
                try {
                    
                    Db.executeSql(sql1);
                    Db.executeSql(sql2);
                    Db.executeSql(sql3);
                    writeLog(idAggiornamento, "aggiorno campo stato nullable");

                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 104;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "alter table codici_iva modify column descrizione varchar(250) NULL";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiorno campo iva descrizione piu lunga");

                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 105;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "ALTER TABLE `dati_azienda_banche` ADD COLUMN `id` INTEGER NOT NULL AUTO_INCREMENT AFTER `cc_iban`, DROP PRIMARY KEY, ADD PRIMARY KEY  (`id`);";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiorno campo id ai conti correnti aziendali");

                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            idAggiornamento = 106;
            if (checkLog(idAggiornamento) == false) {
                String sql1 = "ALTER TABLE pagamenti ADD COLUMN id_conto INTEGER NULL";
                try {
                    
                    Db.executeSql(sql1);
                    writeLog(idAggiornamento, "aggiorno campo conto ai pagamenti");
                    
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            agg(107, "ALTER TABLE tipi_listino ADD COLUMN tipo varchar(50) NULL", "agg campo tipo listino");
            agg(108, "ALTER TABLE tipi_listino ADD COLUMN ricarico_flag char(1) NULL", "agg listino");
            agg(109, "ALTER TABLE tipi_listino ADD COLUMN ricarico_perc decimal(6,2) NULL", "agg listino");
            agg(110, "ALTER TABLE tipi_listino ADD COLUMN ricarico_listino varchar(10) NULL", "agg listino");

            agg(111, "ALTER TABLE articoli ADD COLUMN servizio CHAR(1) NULL", "agg articoli");

            agg(112, "ALTER TABLE clie_forn MODIFY COLUMN persona_riferimento VARCHAR(200) NULL", "agg persona riferimento");

            agg(113, "ALTER TABLE righ_ddt MODIFY COLUMN sconto1 DECIMAL(5,2) NULL", "agg sconti 100");
            agg(114, "ALTER TABLE righ_ddt MODIFY COLUMN sconto2 DECIMAL(5,2) NULL", "agg sconti 100");
            agg(115, "ALTER TABLE righ_fatt MODIFY COLUMN sconto1 DECIMAL(5,2) NULL", "agg sconti 100");
            agg(116, "ALTER TABLE righ_fatt MODIFY COLUMN sconto2 DECIMAL(5,2) NULL", "agg sconti 100");
            agg(117, "ALTER TABLE righ_ordi MODIFY COLUMN sconto1 DECIMAL(5,2) NULL", "agg sconti 100");
            agg(118, "ALTER TABLE righ_ordi MODIFY COLUMN sconto2 DECIMAL(5,2) NULL", "agg sconti 100");
            agg(119, "ALTER TABLE righ_fatt" + nome_ricevute + " MODIFY COLUMN sconto1 DECIMAL(5,2) NULL", "agg sconti 100");
            agg(120, "ALTER TABLE righ_fatt" + nome_ricevute + " MODIFY COLUMN sconto2 DECIMAL(5,2) NULL", "agg sconti 100");
            agg(121, "ALTER TABLE agenti MODIFY COLUMN percentuale DECIMAL(5,2) default 0 NULL ", "agg perc agenti");
            agg(122, "ALTER TABLE test_fatt MODIFY COLUMN agente_percentuale DECIMAL(5,2) default 0 NULL", "agg perc agenti");
            agg(123, "ALTER TABLE test_ordi MODIFY COLUMN agente_percentuale DECIMAL(5,2) default 0 NULL", "agg perc agenti");
            agg(124, "ALTER TABLE fatture_ricevute_iva MODIFY COLUMN percentuale_iva DECIMAL(5,2) default 0 NULL", "agg iva");
            agg(125, "ALTER TABLE prima_nota_iva MODIFY COLUMN percentuale_iva DECIMAL(5,2) default 0 NULL", "agg iva");

            agg(126, "insert into codici_iva values ('41',(0.000),'Non imponibile art.41 D.L 513','Non Imp. art.41')", "agg iva");

            agg(127, "ALTER TABLE clie_forn_dest add paese varchar(2) NULL", "aggiungo paese su dest diversa");
            agg(128, "ALTER TABLE test_fatt add dest_paese varchar(2) NULL", "aggiungo paese su dest diversa");
            agg(129, "ALTER TABLE test_ddt add dest_paese varchar(2) NULL", "aggiungo paese su dest diversa");
            agg(130, "ALTER TABLE test_ordi add dest_paese varchar(2) NULL", "aggiungo paese su dest diversa");

            agg(131, "ALTER TABLE clie_forn add opzione_raggruppa_ddt char(1) NULL default 'N'", "aggiungo opzione riba raggruppate");
            agg(132, "ALTER TABLE clie_forn add opzione_prezzi_ddt char(1) NULL default 'N'", "aggiungo opzione prezzi ddt");
            agg(133, "ALTER TABLE test_ddt add opzione_prezzi_ddt char(1) NULL default 'N'", "aggiungo opzione prezzi ddt testddt");

            agg(134, "create TABLE scadenze_parziali (id bigint not null auto_increment, id_scadenza bigint not null, data date default '0000-00-00', importo decimal (15,5) default 0, PRIMARY KEY (id)) ENGINE=MyISAM", "scadenze con paga. parziale");
            agg(135, "insert into scadenze_parziali (id_scadenza, data, importo) select id, data_scadenza, importo from scadenze where pagata = 'S'", "scadenze con paga. parziale 2");

            agg(136, "alter table articoli add tipo char(1)", "aggiunto campo tipo su articoli");
            agg(137, "ALTER TABLE `test_fatt` ADD COLUMN `totaleRivalsa` decimal(5,2) DEFAULT 0","Aggiungo Rivalsa");
            agg(138, "ALTER TABLE `scadenze` ADD COLUMN `flag_file` VARCHAR(1) DEFAULT 'N'","Aggiungo Flag stampa per riba");

            agg(139, "ALTER TABLE `test_ddt` ADD COLUMN `banca_abi` varchar(5) default NULL","Aggiungo campi mancanti in ddt per conversione prev ddt");
            agg(140, "ALTER TABLE `test_ddt` ADD COLUMN `banca_cab` varchar(5) default NULL","Aggiungo campi mancanti in ddt per conversione prev ddt");
            agg(141, "ALTER TABLE `test_ddt` ADD COLUMN `banca_cc` varchar(35) default NULL","Aggiungo campi mancanti in ddt per conversione prev ddt");
            agg(142, "ALTER TABLE `test_ddt` ADD COLUMN `agente_codice` int(11) default NULL","Aggiungo campi mancanti in ddt per conversione prev ddt");
            agg(143, "ALTER TABLE `test_ddt` ADD COLUMN `agente_percentuale` decimal(5,2) default '0.00'","Aggiungo campi mancanti in ddt per conversione prev ddt");
            agg(144, "ALTER TABLE `test_ddt` ADD COLUMN `note_pagamento` text","Aggiungo campi mancanti in ddt per conversione prev ddt");
            agg(145, "ALTER TABLE `clie_forn` ADD COLUMN `obsoleto` VARCHAR(1) default 0","Aggiungo campo per cliente/fornitore obsoleto");
            agg(146, "ALTER TABLE `articoli` ADD COLUMN `flag_kit` VARCHAR(1) default 'N'","Aggiungo campo per articolo composto");
            agg(147, "CREATE TABLE pacchetti_articoli (pacchetto varchar(20) not null, articolo varchar(20) not null, quantita int not null) ENGINE=MyISAM", "Creo tabella relazione pacchetti articoli");
            agg(148, "ALTER TABLE `articoli` ADD COLUMN `flag_kit` VARCHAR(1) default 'N'","Aggiungo campo per articolo composto2..");
            agg(149, "ALTER TABLE `clie_forn` ADD COLUMN `logo` VARCHAR(100)","Aggiungo campo logo in anagrafica clienti");
            agg(150, "ALTER TABLE `test_fatt` ADD COLUMN `fornitore` int(10) default NULL","Aggiungo campo fornitore in testata fatture");
            agg(151, "ALTER TABLE `test_ordi` ADD COLUMN `fornitore` int(10) default NULL","Aggiungo campo fornitore in testata ordini preventivi");

            agg(152, "ALTER TABLE `agenti` ADD COLUMN `email` varchar(200) default NULL", "Aggiungo campo email in agenti");

            agg(153, "alter table clie_forn modify indirizzo varchar(200)", "allargo indirizzo...");
            agg(154, "alter table clie_forn modify localita varchar(200)", "allargo indirizzo...");

            //salto...
            
            agg(161, "ALTER TABLE `test_fatt` ADD COLUMN `id` INTEGER NOT NULL AUTO_INCREMENT FIRST, DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`);","Aggiungo campo id a test_fatt");
            agg(162, "ALTER TABLE `righ_fatt` ADD COLUMN (`id` INTEGER NOT NULL AUTO_INCREMENT , `id_padre` INTEGER NOT NULL), DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`)","Aggiungo campo id a righ_fatt per collegamento a test_fatt");
            agg(163, "ALTER TABLE `test_ddt` ADD COLUMN `id` INTEGER NOT NULL AUTO_INCREMENT FIRST, DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`);","Aggiungo campo id a test_ddt");
            agg(164, "ALTER TABLE `righ_ddt` ADD COLUMN (`id` INTEGER NOT NULL AUTO_INCREMENT , `id_padre` INTEGER NOT NULL), DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`)","Aggiungo campo id a righ_ddt per collegamento a test_ddt");
            agg(165, "ALTER TABLE `test_ordi` ADD COLUMN `id` INTEGER NOT NULL AUTO_INCREMENT FIRST, DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`);","Aggiungo campo id a test_ordi");
            agg(166, "ALTER TABLE `righ_ordi` ADD COLUMN (`id` INTEGER NOT NULL AUTO_INCREMENT , `id_padre` INTEGER NOT NULL), DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`)","Aggiungo campo id a righ_ordi per collegamento a test_ordi");
            agg(167, "ALTER TABLE `" + testate_ricevute + "` ADD COLUMN `id` INTEGER NOT NULL AUTO_INCREMENT FIRST, DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`);","Aggiungo campo id a fatture_ricevute_teste");
            agg(168, "ALTER TABLE `righ_fatt" + nome_ricevute + "` ADD COLUMN (`id` INTEGER NOT NULL AUTO_INCREMENT , `id_padre` INTEGER NOT NULL), DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`)","Aggiungo campo id a righ_fatt_ricevute per collegamento a fatture_ricevute_teste");
            agg(169, "UPDATE righ_fatt r left join test_fatt t on r.serie = t.serie and r.anno = t.anno and r.numero = t.numero set r.id_padre = t.id","Aggiorno campo id per righ_fatt con l'equivalente in test_fatt");
            agg(170, "UPDATE righ_ddt r left join test_ddt t on r.serie = t.serie and r.anno = t.anno and r.numero = t.numero set r.id_padre = t.id","Aggiorno campo id per righ_ddt con l'equivalente in test_fatt");
            agg(171, "UPDATE righ_ordi r left join test_ordi t on r.serie = t.serie and r.anno = t.anno and r.numero = t.numero set r.id_padre = t.id","Aggiorno campo id per righ_ordi con l'equivalente in test_fatt");
            agg(172, "UPDATE righ_fatt" + nome_ricevute + " r left join " + testate_ricevute + " t on r.serie = t.serie and r.anno = t.anno and r.numero = t.numero set r.id_padre = t.id","Aggiorno campo id per righ_fatt_ricevute con l'equivalente in fatture_ricevute_teste");
            agg(173, "INSERT INTO tipi_fatture (tipo, descrizione_breve, descrizione) values (7, 'SC', 'SCONTRINO');","Aggiorno gli scontrini ai tipi di fattura");
            agg(174, "ALTER TABLE `righ_fatt_matricole` ADD COLUMN (`id` INTEGER NOT NULL AUTO_INCREMENT , `id_padre` INTEGER NOT NULL), DROP PRIMARY KEY, ADD PRIMARY KEY (`id`), ADD INDEX `INDEX` (`serie`, `numero`, `anno`, `riga`, `matricola`)","Aggiungo campo id a righ_fatt_matricole per la generazione dei movimenti");
            agg(175, "ALTER TABLE `movimenti_magazzino` ADD COLUMN `da_id` INTEGER","Aggiungo campo da_id a movimenti magazzino per la generazione dei movimenti con gli scontrini");
            agg(176, "ALTER TABLE `test_fatt` ADD COLUMN `scontrino_importo_pagato` decimal(12,2);","Aggiungo campo importo pagato a test_fatt");

            agg(177, "alter table articoli add gestione_matricola char(1) not null default 'N'", "aggiungo campo gestione matricola in articoli");
            
            agg(178, "ALTER TABLE movimenti_magazzino ADD INDEX ind_articolo (articolo)", "indicizzazione per ricerca articoli");

            agg(179, "ALTER TABLE righ_fatt ADD COLUMN flag_ritenuta char(1) DEFAULT 'S'", "righe ritenuta");
            agg(180, "ALTER TABLE righ_fatt" + nome_ricevute + " ADD COLUMN flag_ritenuta char(1) DEFAULT 'S'", "righe ritenuta");
            agg(181, "ALTER TABLE righ_ddt ADD COLUMN flag_ritenuta char(1) DEFAULT 'S'", "righe ritenuta");
            agg(182, "ALTER TABLE righ_ordi ADD COLUMN flag_ritenuta char(1) DEFAULT 'S'", "righe ritenuta");

            agg(183, "ALTER TABLE articoli ADD COLUMN codice_fornitore varchar(100) DEFAULT ''", "articoli codice fornitore");
            agg(184, "ALTER TABLE articoli ADD COLUMN codice_a_barre varchar(100) DEFAULT ''", "articoli codice a barre");
            agg(185, "ALTER TABLE articoli ADD INDEX index_codice_fornitore(codice_fornitore)", "articoli indice codice fornitore");
            agg(186, "ALTER TABLE articoli ADD INDEX index_codice_a_barre(codice_a_barre)", "articoli indice codice a barre");

            agg(187, "ALTER TABLE articoli ADD COLUMN immagine1 varchar(250) default ''", "articoli immagine");

            agg(188, "ALTER TABLE righ_ddt ADD COLUMN bolla_cliente varchar(50) default ''", "aggiunta bolla cliente per ddt");
            agg(189, "ALTER TABLE righ_ddt ADD COLUMN misura varchar(20) default ''", "aggiunta misura pezzo sy ddt");
            agg(190, "ALTER TABLE righ_ddt ADD COLUMN disegno varchar(50) default ''", "aggiunta bolla cliente per ddt");
            agg(191, "ALTER TABLE righ_ddt ADD COLUMN var varchar(15) default ''", "aggiunta bolla cliente per ddt");

            agg(192, "ALTER TABLE righ_fatt ADD COLUMN bolla_cliente varchar(50) default ''", "aggiunta bolla cliente per fattura");
            agg(193, "ALTER TABLE righ_fatt ADD COLUMN misura varchar(20) default ''", "aggiunta misura pezzo sy fattura");
            agg(194, "ALTER TABLE righ_fatt ADD COLUMN disegno varchar(50) default ''", "aggiunta bolla cliente per fattura");
            agg(195, "ALTER TABLE righ_fatt ADD COLUMN var varchar(15) default ''", "aggiunta bolla cliente per fattura");

            agg(196, "ALTER TABLE clie_forn ADD COLUMN nota_cliente text", "aggiunto campo nota cliente");
            agg(197, "CREATE TABLE clie_forn_rapporti (id int(11) not null AUTO_INCREMENT PRIMARY KEY, cliente int(10) not null, data date, data_avviso date, testo text) ENGINE=MyISAM", "Creo tabella relazione clienti rapporti");
            agg(198, "ALTER TABLE clie_forn_rapporti ADD COLUMN segnalato varchar(1) default 'N'", "aggiunto campo notifica rapporti");

            agg(199, "ALTER TABLE articoli ADD COLUMN fornitore varchar(10)", "aggiunto campo fornitore su articoli");

            agg(200, "CREATE TABLE plugin_ricerca_add (id int(11) not null AUTO_INCREMENT PRIMARY KEY, data_ora datetime) ENGINE=MyISAM", "plugin ricerca");
            agg(201, "UPDATE righ_fatt" + nome_ricevute + " r left join " + testate_ricevute + " t on r.serie = t.serie and r.anno = t.anno and r.numero = t.numero set r.id_padre = t.id","Aggiorno campo id per righ_fatt" + nome_ricevute + " con l'equivalente in fatture_ricevute_teste");

            agg(202, "ALTER TABLE test_fatt MODIFY COLUMN cliente INT(10) UNSIGNED DEFAULT NULL", "cliente default null");
            agg(203, "ALTER TABLE test_ddt MODIFY COLUMN cliente INT(10) UNSIGNED DEFAULT NULL", "cliente default null");
            agg(204, "ALTER TABLE test_ordi MODIFY COLUMN cliente INT(10) UNSIGNED DEFAULT NULL", "cliente default null");

            agg(205, "create TABLE attivazioni (id int unsigned NOT NULL auto_increment, data TIMESTAMP DEFAULT CURRENT_TIMESTAMP, msg varchar(255) NULL, PRIMARY KEY (id)) ENGINE=MyISAM", "tabella attivazioni");

            agg(206, "alter table articoli add gestione_lotti char(1) not null default 'N'", "aggiungo campo gestione lotti in articoli");
            agg(207, "ALTER TABLE movimenti_magazzino ADD COLUMN lotto VARCHAR(200)", "Aggiungo campo lotto movimenti magazzino");
            agg(208, "CREATE TABLE righ_fatt_lotti (id_padre int, id int not null auto_increment, lotto varchar(200), codice_articolo VARCHAR(20), qta decimal(8,2), PRIMARY KEY(id)) ENGINE=MyISAM", "Aggiungo campo lotto movimenti magazzino fatt");
            agg(209, "CREATE TABLE righ_fatt" + nome_ricevute + "_lotti (id_padre int, id int not null auto_increment, lotto varchar(200), codice_articolo VARCHAR(20), qta decimal(8,2), PRIMARY KEY(id)) ENGINE=MyISAM", "Aggiungo campo lotto movimenti magazzino fatt acq");
            agg(210, "CREATE TABLE righ_ddt_lotti (id_padre int, id int not null auto_increment, lotto varchar(200), codice_articolo VARCHAR(20), qta decimal(8,2), PRIMARY KEY(id)) ENGINE=MyISAM", "Aggiungo campo lotto movimenti magazzino ddt");
            agg(211, "ALTER TABLE movimenti_magazzino ADD INDEX ind_articolo (articolo)", "indicizzazione per ricerca articoli 2");

            agg(212, "UPDATE righ_fatt r left join test_fatt t on r.serie = t.serie and r.anno = t.anno and r.numero = t.numero and t.tipo_fattura != 7 set r.id_padre = t.id", "Aggiorno campo id per righ_fatt con l'equivalente in test_fatt");
            agg(213, "ALTER TABLE test_fatt ADD COLUMN marca_da_bollo DECIMAL(8,2)", "Aggiunto importo marche da bollo su testate fatture");

            agg(214, "ALTER TABLE clie_forn ADD COLUMN note_automatiche char(1) DEFAULT 'N'", "Aggiunto campo flag note automatiche su clienti");
            agg(215, "ALTER TABLE clie_forn ADD COLUMN iva_standard char(3)", "Aggiunto campo iva standre su clienti");
            agg(216, "ALTER TABLE " + testate_ricevute + " MODIFY COLUMN numero_doc bigint(15)", "Aumentata capienza campo numero fattura esterna");
            
            agg(217, "create table tipi_porto (id int auto_increment not null primary key, porto varchar(100)) ENGINE=MyISAM", "tabella tipi_porto");
            agg(218, "insert into tipi_porto (porto) values ('PORTO ASSEGNATO')", "tabella tipi_porto");
            agg(219, "insert into tipi_porto (porto) values ('PORTO FRANCO')", "tabella tipi_porto");

            agg(220, "create table tipi_causali_trasporto (id int auto_increment not null primary key, nome varchar(100)) ENGINE=MyISAM", "tabella tipi_causali_trasporto");
            agg(221, "insert into tipi_causali_trasporto (nome) values ('VENDITA')", "tabella tipi_causali_trasporto");
            agg(222, "insert into tipi_causali_trasporto (nome) values ('TENTATA VENDITA')", "tabella tipi_causali_trasporto");
            agg(223, "insert into tipi_causali_trasporto (nome) values ('LAVORAZIONE')", "tabella tipi_causali_trasporto");
            agg(224, "insert into tipi_causali_trasporto (nome) values ('C/LAVORAZIONE')", "tabella tipi_causali_trasporto");
            agg(225, "insert into tipi_causali_trasporto (nome) values ('C/VISIONE')", "tabella tipi_causali_trasporto");
            agg(226, "insert into tipi_causali_trasporto (nome) values ('C/RIPARAZIONE')", "tabella tipi_causali_trasporto");
            agg(227, "insert into tipi_causali_trasporto (nome) values ('RESO C/LAVORAZIONE')", "tabella tipi_causali_trasporto");
            agg(228, "insert into tipi_causali_trasporto (nome) values ('RESO SCARTO INUTILIZZABILE')", "tabella tipi_causali_trasporto");
            agg(229, "insert into tipi_causali_trasporto (nome) values ('RESO C/VISIONE')", "tabella tipi_causali_trasporto");
            agg(230, "insert into tipi_causali_trasporto (nome) values ('RESO C/RIPARAZIONE')", "tabella tipi_causali_trasporto");
            agg(231, "insert into tipi_causali_trasporto (nome) values ('RIPARAZIONE')", "tabella tipi_causali_trasporto");
            agg(232, "insert into tipi_causali_trasporto (nome) values ('CONSEGNA C/TERZI')", "tabella tipi_causali_trasporto");
            agg(233, "insert into tipi_causali_trasporto (nome) values ('OMAGGIO')", "tabella tipi_causali_trasporto");
            agg(234, "insert into tipi_causali_trasporto (nome) values ('RESO')", "tabella tipi_causali_trasporto");
            agg(235, "insert into tipi_causali_trasporto (nome) values ('CESSIONE')", "tabella tipi_causali_trasporto");

            agg(236, "alter TABLE righ_fatt_lotti add matricola varchar(255)", "Aggiungo matricola nei lotti");
            agg(237, "alter TABLE righ_fatt" + nome_ricevute + "_lotti add matricola varchar(255)", "Aggiungo matricola nei lotti");
            agg(238, "alter TABLE righ_ddt_lotti add matricola varchar(255)", "Aggiungo matricola nei lotti");

            agg(239, "alter TABLE storico add dati LONGTEXT", "Aggiungo colonna dati allo storico");

            agg(242, "ALTER TABLE agenti ADD COLUMN (percentuale_soglia_1 decimal(5,2) DEFAULT '0.00', percentuale_soglia_2 decimal(5,2) DEFAULT '0.00', percentuale_soglia_3 decimal(5,2) DEFAULT '0.00', percentuale_soglia_4 decimal(5,2) DEFAULT '0.00', percentuale_soglia_5 decimal(5,2) DEFAULT '0.00')","Aggiunte percentuali personalizzate in base alle soglie sugli agenti");

            agg(243, "ALTER TABLE agenti MODIFY COLUMN cap VARCHAR(5) DEFAULT ''", "modificato campo cap su agenti");

            agg(250, "ALTER TABLE `pagamenti`  ADD COLUMN `210` CHAR(1) NOT NULL DEFAULT '' AFTER `180`", "Aggiungo colonna 210gg");

            agg(251, "alter table " + testate_ricevute + " add column totale_ritenuta decimal(15,5)", "Aggiungo totale_ritenuta");
            agg(252, "alter table " + testate_ricevute + " add column totale_da_pagare decimal(15,5)", "Aggiungo totale_ritenuta");
            agg(253, "ALTER TABLE " + testate_ricevute + " ADD COLUMN totaleRivalsa decimal(5,2) DEFAULT 0","Aggiungo Rivalsa");

            idAggiornamento = 254;
            if (DbUtils.getCreateTable("articoli", Db.getConn()).indexOf("`iva` decimal") >= 0 && checkLog(idAggiornamento) == false) {
                try {
                    cambioCampoIva("articoli");
                    writeLog(idAggiornamento, "codice iva a char di 3 invece che numerico con deicmali");
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

            agg(255, "ALTER TABLE articoli ADD COLUMN non_applicare_percentuale char(1)","aggiungo paramentro per non applicare prcentuale di ricarico");

            //spostate per problemi modifica doppia
            if (!DbUtils.existTable(Db.getConn(), "soglie_provvigioni")) {
                agg(260, "CREATE TABLE soglie_provvigioni (soglia int not null auto_increment, min_soglia decimal (5,2), max_soglia decimal (5,2), PRIMARY KEY(soglia)) ENGINE=MyISAM", "Aggiungo tabella soglie per provvigioni agenti nesocell");
                agg(261, "ALTER TABLE soglie_provvigioni ADD COLUMN percentuale decimal (5,2)", "Aggiunto a tabella soglie per provvigioni agenti percentuale della soglia");
                agg(262, "INSERT INTO soglie_provvigioni (soglia) VALUES (1)", "Preparo riga 1 per soglie");
                agg(263, "INSERT INTO soglie_provvigioni (soglia) VALUES (2)", "Preparo riga 2 per soglie");
                agg(264, "INSERT INTO soglie_provvigioni (soglia) VALUES (3)", "Preparo riga 3 per soglie");
                agg(265, "INSERT INTO soglie_provvigioni (soglia) VALUES (4)", "Preparo riga 4 per soglie");
                agg(266, "INSERT INTO soglie_provvigioni (soglia) VALUES (5)", "Preparo riga 5 per soglie");
                agg(270, "ALTER TABLE soglie_provvigioni DROP min_soglia, CHANGE max_soglia sconto_soglia  decimal (5,2)", "Modificata tabella soglie per provvigioni");
            }

            agg(271, "ALTER TABLE righ_ddt add provvigione decimal(5,2)", "provvigioni per riga");
            agg(272, "ALTER TABLE righ_ordi add provvigione decimal(5,2)", "provvigioni per riga");
            agg(273, "ALTER TABLE righ_fatt add provvigione decimal(5,2)", "provvigioni per riga");

            agg(274, "update test_fatt t join righ_fatt r on t.id = r.id_padre set r.provvigione = t.agente_percentuale", "provvigioni per riga");
            agg(275, "update test_ddt t join righ_ddt r on t.id = r.id_padre set r.provvigione = t.agente_percentuale", "provvigioni per riga");
            agg(276, "update test_ordi t join righ_ordi r on t.id = r.id_padre set r.provvigione = t.agente_percentuale", "provvigioni per riga");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                statLog.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (splash != null) {
            splash.jProgressBar1.setIndeterminate(false);        
        }

        System.out.println(mb.getDiff("fine agg db"));
    }

}
