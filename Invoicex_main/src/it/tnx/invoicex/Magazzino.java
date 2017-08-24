/**
 * Invoicex Copyright (c) 2005-2016 Marco Ceccarelli, Tnx srl
 *
 * Questo software è soggetto, e deve essere distribuito con la licenza GNU
 * General Public License, Version 2. La licenza accompagna il software o potete
 * trovarne una copia alla Free Software Foundation http://www.fsf.org .
 *
 * This software is subject to, and may be distributed under, the GNU General
 * Public License, Version 2. The license should have accompanied the software
 * or you may obtain a copy of the license from the Free Software Foundation at
 * http://www.fsf.org .
 *
 * -- Marco Ceccarelli (m.ceccarelli@tnx.it) Tnx snc (http://www.tnx.it)
 *
 */
/*
 * Magazzino.java
 *
 * Created on 24 maggio 2007, 14.29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package it.tnx.invoicex;

import gestioneFatture.main;
import static gestioneFatture.main.fileIni;
import it.tnx.Db;
import it.tnx.commons.CallableWithArgs;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.FormatUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.invoicex.data.Giacenza;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author mceccarelli
 */
public class Magazzino {

    public static final int ESISTENZA_INIZIALE = 1;
    public static final int CARICO = 2;
    public static final int SCARICO = 3;

    private Connection conn = null;
    public Integer filtro_fornitore_abituale;
    public Integer filtro_categoria;
    public Integer filtro_sottocategoria;

    public static enum Depositi {

        TUTTI_RIEPILOGATIVO, TUTTI_DETTAGLIO, TUTTI
    }

    /**
     * Creates a new instance of Magazzino
     */
    public Magazzino() {
    }

    public Magazzino(Connection conn) {
        this.conn = conn;
    }

    private Connection getconn() {
        if (conn == null) {
            return Db.getConn();
        }
        return conn;
    }

    public static boolean isMultiDeposito() throws Exception {
        if (cu.i0(dbu.getObject(Db.getConn(), "select count(*) as depositi from depositi", false)) > 1) {
            return true;
        }
        return false;
    }

    public ArrayList getGiacenza(Object listino) throws Exception {
//        return getGiacenza(true, listino);
        return getGiacenza(false, listino);
    }

    public ArrayList getGiacenzaPerLotti(Object listino) throws Exception {
        return getGiacenza(false, null, listino, null, true, false, Depositi.TUTTI_RIEPILOGATIVO);
    }

    public ArrayList getGiacenzaPerLotti(String articolo, Object listino, Date data, boolean comprendereQtaZero, Object deposito_key) throws Exception {
        return getGiacenza(false, articolo, listino, data, true, comprendereQtaZero, deposito_key);
    }

    public ArrayList getGiacenza(boolean perMatricola, Object listino) throws Exception {
        return getGiacenza(perMatricola, null, listino);
    }

    public ArrayList getGiacenza(boolean perMatricola, String articolo, Object listino) throws Exception {
        return getGiacenza(perMatricola, articolo, listino, null);
    }

    public ArrayList getGiacenza(boolean perMatricola, String articolo, Object listino, Date data) throws Exception {
        return getGiacenza(perMatricola, articolo, listino, data, false, false, Depositi.TUTTI_RIEPILOGATIVO);
    }

    public ArrayList getGiacenza(boolean perMatricola, String articolo, Object listino, Date data, boolean perLotti, boolean comprendereQtaZero, Object deposito_key) throws Exception {
        return getGiacenza(perMatricola, articolo, listino, data, perLotti, comprendereQtaZero, false, deposito_key == null ? Depositi.TUTTI_RIEPILOGATIVO : deposito_key);
    }

    public ArrayList getGiacenza(boolean perMatricola, String articolo, Object listino, Date data, boolean perLotti, boolean comprendereQtaZero, boolean forza_no_lotti) throws Exception {
        return getGiacenza(perMatricola, articolo, listino, data, perLotti, comprendereQtaZero, forza_no_lotti, Depositi.TUTTI_RIEPILOGATIVO);
    }

    public ArrayList getGiacenza(boolean perMatricola, String articolo, Object listino, Date data, boolean perLotti, boolean comprendereQtaZero, boolean forza_no_lotti, Object deposito) throws Exception {
        return getGiacenza(perMatricola, articolo, listino, data, perLotti, comprendereQtaZero, forza_no_lotti, deposito, -1);
    }

    static public CallableWithArgs pregiacenzacmpcustom = null;

    public ArrayList<Giacenza> getGiacenza(boolean perMatricola, String articolo, Object listino, Date data, boolean perLotti, boolean comprendereQtaZero, boolean forza_no_lotti, Object deposito, Integer fornitore) throws Exception {
        return getGiacenza(perMatricola, articolo, listino, data, perLotti, comprendereQtaZero, forza_no_lotti, deposito, fornitore, false);
    }

    public ArrayList<Giacenza> getGiacenza(boolean perMatricola, String articolo, Object listino, Date data, boolean perLotti, boolean comprendereQtaZero, boolean forza_no_lotti, Object deposito, Integer fornitore, boolean forza_no_esplosione_kit) throws Exception {
        String sql = null;
        boolean addPrezzo = true;
        ArrayList<Giacenza> ret = new ArrayList();

        if (listino != null && listino instanceof Integer && cu.i0(listino) != 0) {
            /* calcolo a partire dai movimenti */
 /*
             InvoicexUtil.aggiornaPrezziNettiUnitari("righ_fatt_acquisto", "test_fatt_acquisto");
             InvoicexUtil.aggiornaPrezziNettiUnitari("righ_fatt", "test_fatt");
             InvoicexUtil.aggiornaPrezziNettiUnitari("righ_ddt_acquisto", "test_ddt_acquisto");
             InvoicexUtil.aggiornaPrezziNettiUnitari("righ_ddt", "test_ddt");
            
             //aggiorno i prezzi medi su movimenti magazzino
             if (cu.i0(listino) == 5) {
             sql = "update movimenti_magazzino m ";
             sql += " join tipi_causali_magazzino c on m.causale = c.codice";
             sql += " set prezzo_medio = (select sum(prezzo_netto_unitario*quantita) / sum(quantita) as pr_qt from righ_fatt_acquisto r where r.id_padre = m.da_id and r.codice_articolo = m.articolo)";
             sql += " where c.segno > 0 and m.da_tabella = 'test_fatt_acquisto'";
             try {
             DbUtils.tryExecQuery(getconn(), sql);
             } catch (Exception e) {
             e.printStackTrace();
             }
             sql = "update movimenti_magazzino m ";
             sql += " join tipi_causali_magazzino c on m.causale = c.codice";
             sql += " set prezzo_medio = (select sum(prezzo_netto_unitario*quantita) / sum(quantita) as pr_qt from righ_ddt_acquisto r where r.id_padre = m.da_id and r.codice_articolo = m.articolo)";
             sql += " where c.segno > 0 and m.da_tabella = 'test_ddt_acquisto'";
             try {
             DbUtils.tryExecQuery(getconn(), sql);
             } catch (Exception e) {
             e.printStackTrace();
             }                

             */

            //aggiorno i prezzi medi su movimenti magazzino
            if (cu.i0(listino) == 5) {
                if (pregiacenzacmpcustom != null) {
                    try {
                        boolean esito = (Boolean) pregiacenzacmpcustom.call(data);
                        if (!esito) {
                            SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore durante il calcolo del costo medio");
                            return null;
                        }
                    } catch (Exception e) {
                        SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
                        return null;
                    }
                } else {
                    // costo medio a partire dalle fatture di acquisto
                    InvoicexUtil.aggiornaPrezziNettiUnitari(getconn(), "righ_fatt_acquisto", "test_fatt_acquisto");
                    //calcolo prezzo medio
                    sql = "UPDATE articoli a \n"
                            + "set a.prezzo_medio = (\n"
                            + "  select round((sum(r.prezzo_netto_unitario * r.quantita) / sum(r.quantita)),5) from righ_fatt_acquisto r"
                            + "  join test_fatt_acquisto tf on r.id_padre = tf.id "
                            + "  where r.codice_articolo = a.codice " + (data != null ? " and tf.data <= '" + FormatUtils.formatMysqlDate(data) + "'" : "")
                            + " group by r.codice_articolo\n"
                            + ")";
                    System.out.println("sql = " + sql);
                    try {
                        DbUtils.tryExecQuery(getconn(), sql);
                    } catch (Exception e) {
                        SwingUtils.showExceptionMessage(main.getPadreFrame(), e);
                        e.printStackTrace();
                    }
                }
            }
        }

        boolean flag_kit = false;
        if (articolo != null) {
            sql = "select flag_kit from articoli where codice = " + Db.pc(articolo, Types.VARCHAR);
            try {
                if (StringUtils.equalsIgnoreCase("S", (String) DbUtils.getObject(getconn(), sql))) {
                    flag_kit = true;
                }
            } catch (Exception ex) {
            }
        }

        boolean flag_lotti = false;
        if (perLotti) {
            flag_lotti = true;
        } else if (!forza_no_lotti) {
            if (articolo != null) {
                sql = "select gestione_lotti from articoli where codice = " + Db.pc(articolo, Types.VARCHAR);
                try {
                    if (StringUtils.equalsIgnoreCase("S", (String) DbUtils.getObject(getconn(), sql))) {
                        flag_lotti = true;
                    }
                } catch (Exception ex) {
                }
            }
        }

//11/11/2013 in caso di scelta di articolo kit con lotti non propone le giacenze giuste        
//        if (flag_kit) {
        if (flag_kit && (!flag_lotti) && !forza_no_esplosione_kit) {
            //esplodo il pacchetto..
            if (articolo != null) {
                sql = "select * from pacchetti_articoli where pacchetto = " + Db.pc(articolo, Types.VARCHAR) + " order by articolo";
                ResultSet rp;
                try {
                    rp = DbUtils.tryOpenResultSet(getconn(), sql);
                    Double min_qta = null;
                    while (rp.next()) {
                        ArrayList ret1 = getGiacenza(perMatricola, rp.getString("articolo"), listino, data);
                        double qta_richiesta = rp.getDouble("quantita");
                        double giacenza = ((Giacenza) ret1.get(0)).getGiacenza() / qta_richiesta;
                        if (min_qta == null || min_qta > giacenza) {
                            min_qta = giacenza;
                        }
                    }
                    System.out.println("qta min:" + min_qta);
                    rp.getStatement().close();
                    rp.close();

                    Giacenza g = new Giacenza();
                    g.setCodice_articolo(articolo);
                    g.setGiacenza(cu.i0(min_qta));
                    ret.add(g);
                } catch (Exception ex) {
                    Logger.getLogger(Magazzino.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            if (perMatricola || flag_lotti) {
                String campo_group = "ltrim(rtrim(ifnull(matricola,''))) as matricola";
                String campo_group_per_by = "ltrim(rtrim(ifnull(matricola,'')))";
                if (flag_lotti) {
                    campo_group = "ltrim(rtrim(ifnull(lotto,''))) as lotto, ltrim(rtrim(ifnull(matricola,''))) as matricola";
                    campo_group_per_by = "ltrim(rtrim(ifnull(lotto,''))), ltrim(rtrim(ifnull(matricola,'')))";
                }
                if (deposito == Depositi.TUTTI_DETTAGLIO) {
                    campo_group += ",CONCAT(dep.nome, ' [', dep.id, ']') as deposito";
                    campo_group += ",movimenti_magazzino.deposito as deposito_id";
                    campo_group_per_by += ",movimenti_magazzino.deposito";
                }
                sql = "SELECT "
                        + " movimenti_magazzino.articolo"
                        + " ," + campo_group
                        + " ,articoli.descrizione"
                        + " ,Sum(quantita * segno) as giac"
                        + " , cat.id as categoria"
                        + " , scat.id as sottocategoria"
                        + " ,cat.categoria as categoria_desc"
                        + " ,scat.sottocategoria as sottocategoria_desc";
                if (listino != null) {
                    addPrezzo = true;
                    if (listino instanceof Integer) {
                        Integer control = (Integer) listino;
                        if (control == 1) {
                            sql += ", (select prezzo from righ_fatt where righ_fatt.codice_articolo = movimenti_magazzino.articolo order by anno desc, numero desc limit 1) as prezzo";
                        } else if (control == 2) {
                            sql += ", (select prezzo from righ_fatt_acquisto where righ_fatt_acquisto.codice_articolo = movimenti_magazzino.articolo order by anno desc, numero desc limit 1) as prezzo";
                        } else if (control == 3) {
                            sql += ", (select prezzo_netto_unitario from righ_fatt where righ_fatt.codice_articolo = movimenti_magazzino.articolo order by anno desc, numero desc limit 1) as prezzo";
                        } else if (control == 4) {
                            sql += ", (select prezzo_netto_unitario from righ_fatt_acquisto where righ_fatt_acquisto.codice_articolo = movimenti_magazzino.articolo order by anno desc, numero desc limit 1) as prezzo";
                        } else if (control == 5) {
                            //medio
                            //con calcolo a partire dai movimenti
                            //sql += ", (select sum(prezzo_medio * quantita) / sum(quantita) from movimenti_magazzino m2 left join tipi_causali_magazzino c2 on m2.causale = c2.codice where m2.articolo = movimenti_magazzino.articolo and c2.segno > 0) as prezzo";
                            sql += ", articoli.prezzo_medio as prezzo";
                        } else {
                            addPrezzo = false;
                        }
                    } else {
                        sql += ", IF(tl.ricarico_flag = 'S', (ap2.prezzo + (ap2.prezzo / 100 * tl.ricarico_perc)), ap.prezzo) as prezzo";
                    }
                }
                sql += " FROM"
                        + " articoli "
                        + " Left Join categorie_articoli cat ON articoli.categoria = cat.id"
                        + " Left Join sottocategorie_articoli scat ON articoli.sottocategoria = scat.id"
                        + " Left Join `movimenti_magazzino` ON `movimenti_magazzino`.`articolo` = `articoli`.`codice`"
                        + " Left Join `tipi_causali_magazzino` ON `movimenti_magazzino`.`causale` = `tipi_causali_magazzino`.`codice`"
                        + " Left Join depositi dep ON `movimenti_magazzino`.`deposito` = dep.id";
                if (listino instanceof String) {
                    sql += " left Join articoli_prezzi ap on movimenti_magazzino.articolo = ap.articolo"
                            + " left join tipi_listino tl on ap.listino = tl.codice"
                            + " left join articoli_prezzi ap2 on movimenti_magazzino.articolo = ap2.articolo and ap2.listino = tl.ricarico_listino";
                }
                sql += " where 1 = 1";
                if (articolo != null) {
                    sql += " and articolo = " + Db.pc(articolo, Types.VARCHAR);
                }
                if (filtro_fornitore_abituale != null) {
                    sql += " and articoli.fornitore = " + Db.pc(filtro_fornitore_abituale, Types.INTEGER);
                }
                if (filtro_categoria != null) {
                    sql += " and articoli.categoria = " + Db.pc(filtro_categoria, Types.INTEGER);
                }
                if (filtro_sottocategoria != null) {
                    sql += " and articoli.sottocategoria = " + Db.pc(filtro_sottocategoria, Types.INTEGER);
                }
                if (listino instanceof String) {
                    String val = (String) listino;
                    sql += " and ap.listino = '" + val + "'";
                }
                if (data != null) {
                    sql += " and data <= '" + FormatUtils.formatMysqlDate(data) + "'";
                }
                if (deposito instanceof Integer) {
                    sql += " and movimenti_magazzino.deposito = " + deposito;
                }
                sql += " GROUP BY"
                        + " `movimenti_magazzino`.`articolo`, "
                        + campo_group_per_by;
                System.out.println("sql: " + sql);
            } else {
                sql = "SELECT "
                        + " movimenti_magazzino.articolo"
                        + " ,articoli.descrizione"
                        + " ,Sum(quantita * segno) as giac"
                        + " , cat.id as categoria"
                        + " , scat.id as sottocategoria"
                        + " ,cat.categoria as categoria_desc"
                        + " ,scat.sottocategoria as sottocategoria_desc";
                if (deposito == Depositi.TUTTI_DETTAGLIO) {
                    sql += ",CONCAT(dep.nome, ' [', dep.id, ']') as deposito";
                    sql += ",movimenti_magazzino.deposito as deposito_id";
                }
                if (listino != null) {
                    addPrezzo = true;
                    if (listino instanceof Integer) {
                        Integer control = (Integer) listino;
                        if (control == 1) {
                            sql += ", (select prezzo from righ_fatt where righ_fatt.codice_articolo = movimenti_magazzino.articolo order by anno desc, numero desc limit 1) as prezzo";
                        } else if (control == 2) {
                            sql += ", (select prezzo from righ_fatt_acquisto where righ_fatt_acquisto.codice_articolo = movimenti_magazzino.articolo order by anno desc, numero desc limit 1) as prezzo";
                        } else if (control == 3) {
                            sql += ", (select prezzo_netto_unitario from righ_fatt where righ_fatt.codice_articolo = movimenti_magazzino.articolo order by anno desc, numero desc limit 1) as prezzo";
                        } else if (control == 4) {
                            sql += ", (select prezzo_netto_unitario from righ_fatt_acquisto where righ_fatt_acquisto.codice_articolo = movimenti_magazzino.articolo order by anno desc, numero desc limit 1) as prezzo";
                        } else if (control == 5) {
                            //medio
                            //con calcolo a partire dai movimenti
                            //sql += ", (select sum(prezzo_medio * quantita) / sum(quantita) from movimenti_magazzino m2 left join tipi_causali_magazzino c2 on m2.causale = c2.codice where m2.articolo = movimenti_magazzino.articolo and c2.segno > 0) as prezzo";
                            sql += ", articoli.prezzo_medio as prezzo";
                        } else {
                            addPrezzo = false;
                        }
                    } else {
                        sql += ", IF(tl.ricarico_flag = 'S', (ap2.prezzo + (ap2.prezzo / 100 * tl.ricarico_perc)), ap.prezzo) as prezzo";
                    }
                }
                sql += " FROM"
                        + " articoli"
                        + " Left Join categorie_articoli cat ON articoli.categoria = cat.id"
                        + " Left Join sottocategorie_articoli scat ON articoli.sottocategoria = scat.id"
                        + " Left Join `movimenti_magazzino` ON `movimenti_magazzino`.`articolo` = `articoli`.`codice`"
                        + " Left Join `tipi_causali_magazzino` ON `movimenti_magazzino`.`causale` = `tipi_causali_magazzino`.`codice`"
                        + " Left Join depositi dep ON `movimenti_magazzino`.`deposito` = dep.id";
                if (listino instanceof String) {
                    sql += " left Join articoli_prezzi ap on movimenti_magazzino.articolo = ap.articolo"
                            + " left join tipi_listino tl on ap.listino = tl.codice"
                            + " left join articoli_prezzi ap2 on movimenti_magazzino.articolo = ap2.articolo and ap2.listino = tl.ricarico_listino";
                }
                sql += " where 1 = 1";
                if (articolo != null) {
                    sql += " and movimenti_magazzino.articolo = " + Db.pc(articolo, Types.VARCHAR);
                }
                if (filtro_fornitore_abituale != null) {
                    sql += " and articoli.fornitore = " + Db.pc(filtro_fornitore_abituale, Types.INTEGER);
                }
                if (filtro_categoria != null) {
                    sql += " and articoli.categoria = " + Db.pc(filtro_categoria, Types.INTEGER);
                }
                if (filtro_sottocategoria != null) {
                    sql += " and articoli.sottocategoria = " + Db.pc(filtro_sottocategoria, Types.INTEGER);
                }
                if (listino instanceof String) {
                    String val = (String) listino;
                    sql += " and ap.listino = '" + val + "'";
                }
                if (data != null) {
                    sql += " and data <= '" + FormatUtils.formatMysqlDate(data) + "'";
                }
                if (deposito instanceof Integer) {
                    sql += " and movimenti_magazzino.deposito = " + deposito;
                }

                if (fornitore != null && fornitore > 0) {
                    sql += " and articoli.produttore_id IN (select produttore_id from fornitori_produttori where cliente_id = " + Db.pc(fornitore, Types.INTEGER) + ")";
                }

                sql += " GROUP BY"
                        + " `movimenti_magazzino`.`articolo`";
                if (deposito == Depositi.TUTTI_DETTAGLIO) {
                    sql += ", movimenti_magazzino.deposito";
                }
                System.out.println("sql:" + sql);
            }
            ResultSet r = null;
//            Statement s = null;
            try {
//                s = getconn().createStatement();
//                s.executeQuery(sql);
                System.out.println("giac pre");
                r = DbUtils.tryOpenResultSet(getconn(), sql);
                System.out.println("giac post");
                while (r.next()) {
                    try {

                        if (!(!comprendereQtaZero && r.getDouble("giac") == 0) && StringUtils.isNotBlank(r.getString("articolo"))) {
                            Giacenza g = new Giacenza();
                            g.setCodice_articolo(r.getString("articolo"));
                            g.setCategoria_id(cu.i(r.getObject("categoria")));
                            g.setCategoria(r.getString("categoria_desc"));
                            g.setSottocategoria_id(cu.i(r.getObject("sottocategoria")));
                            g.setSottocategoria(r.getString("sottocategoria_desc"));
                            g.setDescrizione_articolo(r.getString("descrizione"));
                            g.setGiacenza(r.getDouble("giac"));
                            if (addPrezzo && listino != null) {
                                g.setPrezzo(r.getDouble("prezzo"));
                            }
                            if (perMatricola) {
                                g.setMatricola(r.getString("matricola"));
                            }
                            if (flag_lotti) {
                                g.setLotto(r.getString("lotto"));
                            }
                            if (deposito == Depositi.TUTTI_DETTAGLIO) {
                                g.setDeposito(r.getString("deposito"));
                                g.setDeposito_id(r.getInt("deposito_id"));
                            }
                            ret.add(g);
                        }
                    } catch (Exception err) {
                        System.out.println(err);
                    }
                }
            } catch (SQLException sqlerr) {
                sqlerr.printStackTrace();
                throw sqlerr;
            } catch (Exception err) {
                err.printStackTrace();
                throw err;
            } finally {
                DbUtils.close(r);
            }

        }

        return ret;
    }

    public double getInArrivo(String codart) {
        double diff_ordi = 0;
        double diff_ddt = 0;
        try {
            String sql = "select sum(IFNULL(quantita,0) - IFNULL(quantita_evasa,0)) as diff from righ_ordi_acquisto r ";
            sql += " left join test_ordi_acquisto t on r.id_padre = t.id";
            sql += " where t.data >= '2010-11-02'";
            sql += " and t.stato_ordine like '%Ordine%'";
            sql += " and codice_articolo = " + Db.pc(codart, Types.VARCHAR);
            System.out.println("sql = " + sql);
            List list = DbUtils.getListMap(getconn(), sql);
            if (list.size() > 0) {
                diff_ordi = CastUtils.toDouble0(((Map) list.get(0)).get("diff"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        try {
//            String sql = "select sum(IFNULL(quantita,0) - IFNULL(quantita_evasa,0)) as diff from righ_ddt_acquisto r ";
//            sql += " left join test_ddt_acquisto t on r.id_padre = t.id";
//            sql += " where t.data >= '2010-11-02'";
//            sql += " and codice_articolo = " + Db.pc(codart, Types.VARCHAR);
//            System.out.println("sql = " + sql);
//            List list = DbUtils.getListMap(getconn(), sql);
//            if (list.size() > 0) {
//                diff_ddt = CastUtils.toDouble0(((Map)list.get(0)).get("diff"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return diff_ordi + diff_ddt;
    }

    public double getInUscita(String codart) {
        double diff_ordi = 0;
        double diff_ddt = 0;
        try {
            String sql = "select sum(IFNULL(quantita,0) - IFNULL(quantita_evasa,0)) as diff from righ_ordi r ";
            sql += " left join test_ordi t on r.id_padre = t.id";
            sql += " where t.data >= '2010-11-02'";
            sql += " and t.stato_ordine like '%Ordine%'";
            sql += " and codice_articolo = " + Db.pc(codart, Types.VARCHAR);
            System.out.println("sql = " + sql);
            List list = DbUtils.getListMap(getconn(), sql);
            if (list.size() > 0) {
                diff_ordi = CastUtils.toDouble0(((Map) list.get(0)).get("diff"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        try {
//            String sql = "select sum(IFNULL(quantita,0) - IFNULL(quantita_evasa,0)) as diff from righ_ddt r ";
//            sql += " left join test_ddt t on r.id_padre = t.id";
//            sql += " where t.data >= '2010-11-02'";
//            sql += " and codice_articolo = " + Db.pc(codart, Types.VARCHAR);
//            System.out.println("sql = " + sql);
//            List list = DbUtils.getListMap(getconn(), sql);
//            if (list.size() > 0) {
//                diff_ddt = CastUtils.toDouble0(((Map)list.get(0)).get("diff"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return diff_ordi + diff_ddt;
    }

    public void preDelete(String sql) {
        String sqlpredel = "delete from movimenti_magazzino_eliminati " + sql;
        try {
            dbu.tryExecQuery(getconn(), sqlpredel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sqlpre = "insert into movimenti_magazzino_eliminati select * from movimenti_magazzino " + sql;
        try {
            dbu.tryExecQuery(getconn(), sqlpre);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sqlpre = "update movimenti_magazzino_eliminati set modificato_ts = CURRENT_TIMESTAMP " + sql;
        try {
            dbu.tryExecQuery(getconn(), sqlpre);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getDepositoStandard() {
        if (!main.GLOB.containsKey("deposito_standard")) {
            main.GLOB.put("deposito_standard", cu.i0(main.fileIni.getValue("depositi", "predefinito", "0")));
            //se non esiste prendo il primo deposito da tabella
            try {
                if (!dbu.containRows(Db.getConn(), "select id from depositi where id = " + dbu.sql(cu.i0(main.GLOB.get("deposito_standard"))))) {
                    main.GLOB.put("deposito_standard", cu.i0( dbu.getObject(Db.getConn(), "select id from depositi order by id limit 1") ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cu.i0(main.GLOB.get("deposito_standard"));
    }
}
