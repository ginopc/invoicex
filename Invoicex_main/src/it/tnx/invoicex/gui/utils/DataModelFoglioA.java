/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui.utils;

import gestioneFatture.Db;
import gestioneFatture.frmTestOrdine;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.invoicex.InvoicexUtil;
import java.sql.Types;
import java.util.HashMap;

/**
 *
 * @author mceccarelli
 */
public class DataModelFoglioA extends DataModelFoglioOrdine {

    public DataModelFoglioA(int rowCount, int columnCount, frmTestOrdine form) {
        super(rowCount, columnCount, form);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 0 || column == 9) {
            return false;
        }
        return super.isCellEditable(row, column);
    }

    @Override
    public void setValueAt(Object obj, int row, int col) {
        if (col == 1) {
            super.setValueAt(obj, row, col);
            String codice = String.valueOf(Db.nz(obj, ""));
            if (codice.trim().length() > 0) {
                recuperaDatiArticolo(String.valueOf(obj), row);
            }
        } else if (col == 3 || col == 5) {
//            try {                
//                double val = CastUtils.toDouble0(String.valueOf(obj));
//                super.setValueAt(formatDouble(val), row, col);
//            } catch (Exception e) {
//            } finally {
//                if (!form.loadingFoglio) {
//                    setValueAt(formatDouble(calcolaTotaleRiga(row)), row, 9);
//                }
//            }
            super.setValueAt(obj, row, col);
            if (!form.loadingFoglio) {
                setValueAt(formatDouble(calcolaTotaleRiga(row)), row, 9);
            }
        } else {
            super.setValueAt(obj, row, col);
        }

        if (!form.loadingFoglio && col >= 1 && col <= 9) {
            String sql = "";
            String sqlv = "";
            String sqlc = "";

            String codice = "";
            String desc = "";
            codice = String.valueOf(Db.nz(getValueAt(row, 1), ""));
            desc = String.valueOf(Db.nz(getValueAt(row, 2), ""));

            sql = "SELECT id FROM test_ordi WHERE ";
            sql += "serie = " + Db.pc(form.texSeri.getText(), Types.VARCHAR);
            sql += " and numero = " + form.texNumeOrdine.getText();
            sql += " and anno = " + form.texAnno.getText();
            Integer id = 0;
            try {
                id = (Integer) DbUtils.getObject(Db.getConn(), sql);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (codice.trim().length() > 0 || desc.trim().length() > 0) {
                
                /*
                sql = "delete from righ_ordi where ";
                sql += "serie = " + Db.pc(form.texSeri.getText(), Types.VARCHAR);
                sql += " and numero = " + form.texNumeOrdine.getText();
                sql += " and anno = " + form.texAnno.getText();
                sql += " and riga = " + Db.pc(getValueAt(row, 0), Types.INTEGER);
                Db.executeSql(sql);

                double totale_imponibile = calcolaTotaleRiga(row);
                double totale_iva = (totale_imponibile * 21d) / 100d;
                String val = "";
                HashMap map = null;
                sql = "insert into righ_ordi (";
                sqlc = "serie";
                sqlv = Db.pc(form.texSeri.getText(), Types.VARCHAR);
                sqlc += ", numero";
                sqlv += ", " + form.texNumeOrdine.getText();
                sqlc += ", id_padre";
                sqlv += ", " + Db.pc(id, Types.INTEGER);
                sqlc += ", anno";
                sqlv += ", " + form.texAnno.getText();
                sqlc += ", riga";
                sqlv += ", " + Db.pc(getValueAt(row, 0), Types.INTEGER);
                sqlc += ", codice_articolo";
                sqlv += ", " + Db.pc(getValueAt(row, 1), Types.VARCHAR);
                sqlc += ", descrizione";
                sqlv += ", " + Db.pc(getValueAt(row, 2), Types.VARCHAR);
                sqlc += ", um";
                try {
                    map = (HashMap) getValueAt(row, 4);
                    val = String.valueOf(Db.nz(map.get("d"), "0"));
                } catch (Exception e) {
                    val = "";
                }
                sqlv += ", " + Db.pc(val, Types.VARCHAR);
                sqlc += ", quantita";
                sqlv += ", " + getDouble(Db.nz(getValueAt(row, 3), "").replace('.', ','));
                sqlc += ", prezzo";
                sqlv += ", " + getDouble(getValueAt(row, 5));
                sqlc += ", iva";
                sqlv += ", '21'";
                sqlc += ", sconto1";
                sqlv += ", " + getDouble("0");
                sqlc += ", sconto2";
                sqlv += ", " + getDouble("0");
                sqlc += ", stato";
                sqlv += ", 'P'";
                sqlc += ", percentuale";
                try {
                    map = (HashMap) getValueAt(row, 6);
                    val = String.valueOf(Db.nz(map.get("k"), "0"));
                    val = String.valueOf(val.replace('.', ','));
                    val = val.equals("") ? "0" : val;
                } catch (Exception e) {
                    val = "0";
                }
                sqlv += ", " + Db.pc(Integer.parseInt(val), Types.INTEGER);
                sqlc += ", emissione_fattura";
                try {
                    map = (HashMap) getValueAt(row, 7);
                    val = String.valueOf(Db.nz(map.get("k"), "0"));
                    val = String.valueOf(val.replace('.', ','));
                    val = val.equals("") ? "0" : val;
                } catch (Exception e) {
                    val = "0";
                }

                sqlv += ", " + Db.pc(Integer.parseInt(val), Types.INTEGER);
                sqlc += ", termini_pagamento";
                try {
                    map = (HashMap) getValueAt(row, 8);
                    val = String.valueOf(Db.nz(map.get("k"), "0"));
                } catch (Exception e) {
                    val = "";
                }
                sqlv += ", " + Db.pc(val, Types.VARCHAR);
                sqlc += ", totale_ivato";
                sqlv += ", " + getDouble(Db.nz(totale_imponibile + totale_iva, "0").replace('.', ','));
                sqlc += ", totale_imponibile";
                sqlv += ", " + getDouble(Db.nz(totale_imponibile, "0").replace('.', ','));
                sql = sql + sqlc + ") values (" + sqlv + ")";
                System.out.println("sql update values: " + sql);
                Db.executeSql(sql);
                * */ 
                //invece di eliminare e reinserire faccio update
                int col_id = 10;
                Integer id_riga = CastUtils.toInteger(getValueAt(row, col_id));

                if (id_riga == null) {
                    sql = "insert into righ_ordi set ";
                } else {
                    sql = "update righ_ordi set ";
                }                    
                HashMap c = new HashMap();
                
                double totale_imponibile = calcolaTotaleRiga(row);
                double totale_iva = (totale_imponibile * 21d) / 100d;
                String val = "";
                HashMap map = null;
                if (id_riga == null) {
                    c.put("id_padre", id);
                    c.put("serie", form.texSeri.getText());
                    c.put("numero", form.texNumeOrdine.getText());
                    c.put("anno", form.texAnno.getText());
                    c.put("riga", getValueAt(row, 0));
                    c.put("iva", InvoicexUtil.getIvaDefaultPassaggio());
                    c.put("sconto1", "0");
                    c.put("sconto2", "0");
                }
                c.put("totale_ivato", totale_imponibile + totale_iva);
                c.put("totale_imponibile", totale_imponibile);
                if (col == 1) {
                    c.put("codice_articolo", getValueAt(row, 1));
                } else if (col == 2) {
                    c.put("descrizione", getValueAt(row, 2));
                } else if (col == 3) {
                    c.put("quantita", CastUtils.toDouble(getValueAt(row, 3)));
                } else if (col == 4) {
                    try {
                        map = (HashMap) getValueAt(row, 4);
                        val = String.valueOf(Db.nz(map.get("d"), "0"));
                    } catch (Exception e) {
                        val = "";
                    }
                    c.put("um", val);
                } else if (col == 5) {
                    c.put("prezzo", CastUtils.toDouble(getValueAt(row, 5)));
                } else if (col == 6) {
                    try {
                        map = (HashMap) getValueAt(row, 6);
                        val = String.valueOf(Db.nz(map.get("k"), "0"));
                        val = String.valueOf(val.replace('.', ','));
                        val = val.equals("") ? "0" : val;
                    } catch (Exception e) {
                        val = "0";
                    }
                    c.put("percentuale", val);
                } else if (col == 7) {
                    try {
                        map = (HashMap) getValueAt(row, 7);
                        val = String.valueOf(Db.nz(map.get("k"), "0"));
                        val = String.valueOf(val.replace('.', ','));
                        val = val.equals("") ? "0" : val;
                    } catch (Exception e) {
                        val = "0";
                    }
                    c.put("emissione_fattura", val);
                } else if (col == 8) {
                    try {
                        map = (HashMap) getValueAt(row, 8);
                        val = String.valueOf(Db.nz(map.get("k"), "0"));
                    } catch (Exception e) {
                        val = "";
                    }
                    c.put("termini_pagamento", val);
                }
                
                sql = sql + DbUtils.prepareSqlFromMap(c);
                if (id_riga != null) {
                    sql += " where id = " + id_riga;
                }
                System.out.println("sql foglio a: " + sql);
                try {
                    DbUtils.tryExecQuery(Db.getConn(), sql);
                    //se id_riga nullo prendo il nuovo id e lo metto altrimenti facci oaltre insert
                    if (id_riga == null) {
                        id_riga = CastUtils.toInteger(DbUtils.getObject(Db.getConn(), "select LAST_INSERT_ID()"));
                        if (id_riga == null) {
                            SwingUtils.showErrorMessage(form, "Errore nel recupero di LAST_INSERT_ID");
                        } else {
                            System.out.println("riga inserita: " + id_riga);
                            setValueAt(id_riga, row, col_id);
                        }
                    }                    
                } catch (Exception e) {
                    SwingUtils.showExceptionMessage(form, e);
                }
            } else {
                System.out.println("elimino riga");

                if (form.texNumeOrdine.getText().length() > 0 && form.texAnno.getText().length() > 0 && getValueAt(row, 0).toString().length() > 0) {
                    System.out.println("elimino riga 2");
                    sql = "delete from righ_ordi where ";
                    sql += "serie = " + Db.pc(form.texSeri.getText(), Types.VARCHAR);
                    sql += " and numero = " + form.texNumeOrdine.getText();
                    sql += " and anno = " + form.texAnno.getText();
                    sql += " and riga = " + Db.pc(getValueAt(row, 0), Types.INTEGER);
                    Db.executeSql(sql);
                }
            }

            form.dbAssociaGrigliaRighe();
        }
    }

    private double calcolaTotaleRiga(int row) {
        double totale = 0d;
        double qta = 0d;
        double importo = 0d;

        try {
//            String val = Db.nz(getValueAt(row, 3), "0");
//            qta = CastUtils.toDouble0(val);
            qta = CastUtils.toDouble0(getValueAt(row, 3));
        } catch (Exception e) {
            qta = 0d;
        }
        try {
//            String val = Db.nz(getValueAt(row, 5), "0");
//            importo = CastUtils.toDouble0(val);
            importo = CastUtils.toDouble0(getValueAt(row, 5));
        } catch (Exception e) {
            importo = 0d;
        }

        totale = qta * importo;
        return totale;
    }
}
